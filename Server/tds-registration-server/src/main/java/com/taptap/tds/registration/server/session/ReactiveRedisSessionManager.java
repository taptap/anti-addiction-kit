package com.taptap.tds.registration.server.session;

import com.taptap.tds.registration.server.PushGatewayRegister;
import com.taptap.tds.registration.server.ReactiveServerHealthChecker;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @Author guyu
 * @create 2020/12/30 3:22 下午
 */
@Log4j2
@Component
public class ReactiveRedisSessionManager {

    private TdsSessionMapper tdsSessionMapper = new TdsSessionMapper();

    @Autowired
    private ReactiveServerHealthChecker reactiveServerHealthChecker;

    @Autowired
    private SessionKickStrategy sessionKickStrategy;

    private static String sessionRedisPrefix = "tds:gw:client-info-s:gid:";

    // 参数， arg1 老sessionId(只当查的sessionId和老的一样时才set，可为空 表示以前没有), arg2 ttl
    // arg3 sessionId arg4 serverId arg5 creationTime
    @Resource
    private RedisScript<List> casSaveScript;

    @Autowired
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @Autowired
    private PushGatewayRegister pushGatewayRegister;

    // 1。脚本先查，没有直接插入。有就返回老sessionId + serverId。再查服务器状态存活则双踢（本地自己处理，非本地发命令）
    // 2。不活带着老sessionId进去cas，被别人抢先了直接双踢 (因为大概率在十秒内）
    public Mono<Boolean> saveSession(TdsSession tdsSession, long ttl){
        return casSaveSession(tdsSession, ttl, null).flatMap(result->{
                    if((Boolean) result.get(0)){
                        // cas null success
                        return Mono.just(true);
                    } else{
                        String lastSessionId = (String)result.get(1);
                        String lastServerId = (String)result.get(2);
                        return reactiveServerHealthChecker.isServerAlive(lastServerId).flatMap(alive->{
                            if(alive){
                                // double kick
                                return sessionKickStrategy.onKick(tdsSession.getGameUserPrinciple(), lastServerId)
                                        .thenReturn(false);
                            }else {
                                return casSaveSession(tdsSession, ttl, lastSessionId).flatMap(result2->{
                                    if((Boolean) result2.get(0)){
                                        // cas with lastSessionId success;
                                        return Mono.just(true);
                                    }else {
                                        // double kick directly
                                        return sessionKickStrategy.onKick(tdsSession.getGameUserPrinciple(), (String) result2.get(2))
                                                .thenReturn(false);
                                    }
                                });
                            }
                        });
                    }
                });
    }

    private Mono<List<Object>> casSaveSession(TdsSession tdsSession, long ttl , String lastSessionId) {

        List<Object> args = Arrays.asList(tdsSession.getId(), tdsSession.getServerId(), tdsSession.getCreationTime(), ttl, lastSessionId);

        return reactiveRedisTemplate.execute(casSaveScript, Arrays.asList(getKey(tdsSession)), args)
                .reduce(new ArrayList<>(), (objects, list) -> {
                    objects.addAll(list);
                    return objects;
                });
    }


    // to Refresh ttl to redis in local
    // 先查再refresh，需要lua/watch么？ 有可能刷到其他的么？ 如果其他登陆想要在get和touch中间踢掉他，前提是他在这期间挂了10秒。
    // 可能性太小了
    public Mono<Void> touch(TdsSession tdsSession, Duration ttl){
        return findByPrinciple(tdsSession.getGameUserPrinciple())
                .filter(redisSession -> redisSession.getId().equals(tdsSession.getId()))
                .switchIfEmpty(Mono.error(new IllegalStateException("session is kicked")))
                .flatMap(session ->
                        reactiveRedisTemplate.expire(getKey(session), ttl).then());
    }


    public Mono<TdsSession> findByPrinciple(GameUserPrinciple gameUserPrinciple) {
        String sessionKey = getKey(gameUserPrinciple);

        return reactiveRedisTemplate.opsForHash().entries(sessionKey)
                .collectMap((e) -> e.getKey().toString(), Map.Entry::getValue)
                .filter((map) -> !map.isEmpty())
                .map(tdsSessionMapper.andThen(tdsSession -> {
                    tdsSession.setGameUserPrinciple(gameUserPrinciple);
                    return tdsSession;
                }));
    }

    // 先查再删，是自己的才删.需要lua/watch么？ 有可能删到别人的么？
    // 如果其他登陆想要在get和delete中间踢掉他，也是需要他在这期间挂10秒。。可能性几乎不存在 不处理了
    public Mono<Void> deleteTdsSession(TdsSession tdsSession){
        return findByPrinciple(tdsSession.getGameUserPrinciple())
              .filter(redisSession -> redisSession.getId().equals(tdsSession.getId()))
              .flatMap(existTdsSession -> reactiveRedisTemplate.opsForHash().delete(getKey(tdsSession))).then();
    }

    // 强制删
    public Mono<Void> forceDeleteTdsSession(GameUserPrinciple gameUserPrinciple){
        return reactiveRedisTemplate.opsForHash().delete(getKey(gameUserPrinciple)).then();
    }

    protected static String getKey(TdsSession tdsSession){
        return getKey(tdsSession.getGameUserPrinciple());
    }

    protected static String getKey(GameUserPrinciple gameUserPrinciple){
        return getKey(gameUserPrinciple.getGameId(), gameUserPrinciple.getUserId());
    }

    protected static String getKey(String gameId, String userId) {
        StringBuilder sb = new StringBuilder(gameId);
        sb.append(":uid:").append(userId);
        return sessionRedisPrefix + sb.toString();
    }

    public TdsSession createSession(GameUserPrinciple gameUserPrinciple){
        TdsSession tdsSession = TdsSession.createWithUUID(gameUserPrinciple);
        tdsSession.setCreationTime(System.currentTimeMillis());
        tdsSession.setServerId(pushGatewayRegister.getPushGatewayMetadata().getServerId());
        return tdsSession;
    }

    public static class TdsSessionMapper implements Function<Map<String, Object>, TdsSession> {

        static final String CREATION_TIME_KEY = "createdAt";

        static final String SERVER_ID_KEY = "serverId";

        static final String SESSION_ID_KEY = "id";


//        static final String ATTRIBUTE_PREFIX = "a:";

        @Override
        public TdsSession apply(Map<String, Object> map) {
            Assert.notEmpty(map, "map must not be empty");
            TdsSession session = new TdsSession();
            Long creationTime = (Long) map.get(CREATION_TIME_KEY);
            String serverId = (String) map.get(SERVER_ID_KEY);
            String sessionId = (String) map.get(SESSION_ID_KEY);

            if (StringUtils.isBlank(sessionId)) {
                handleMissingKey(SESSION_ID_KEY);
            }

            if (StringUtils.isBlank(serverId)) {
                handleMissingKey(SERVER_ID_KEY);
            }

            if (creationTime == null) {
                handleMissingKey(CREATION_TIME_KEY);
            }
            session.setId(sessionId);
            session.setServerId(serverId);
            session.setCreationTime(creationTime);
            return session;
        }

        private static void handleMissingKey(String key) {
            throw new IllegalStateException(key + " key must not be null");
        }
    }


}
