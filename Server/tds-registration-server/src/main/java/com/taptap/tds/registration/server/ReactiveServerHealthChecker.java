package com.taptap.tds.registration.server;

import com.taptap.tds.registration.server.configuration.TdsPushProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


/**
 * @Author guyu
 * @create 2021/1/11 5:55 下午
 */
@Log4j2
@Component
public class ReactiveServerHealthChecker {

    @Autowired
    private TdsPushProperties properties;

    public static final String SERVER_PREFIX = "push:gateway:health:";// push:gateway:health

    @Autowired
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    public Mono<Boolean> isServerAlive(String serverId){
        return reactiveRedisTemplate.opsForValue().get(SERVER_PREFIX + serverId).flatMap(
            str -> {
                long lastAccessTime = (Long) str;
                return Mono.just(healthCheck(lastAccessTime));
            }
        ).switchIfEmpty(Mono.just(false));
    }

    public Mono<Boolean> isServerDie(String serverId){
        return reactiveRedisTemplate.opsForValue().get(SERVER_PREFIX + serverId).flatMap(
                str -> {
                    long lastAccessTime = (Long) str;
                    return Mono.just(!healthCheck(lastAccessTime));
                }
        ).switchIfEmpty(Mono.just(true));
    }

    private boolean healthCheck(long lastAccessTime){
        return System.currentTimeMillis() - lastAccessTime < properties.getHealthCheckIdle();
    }

}
