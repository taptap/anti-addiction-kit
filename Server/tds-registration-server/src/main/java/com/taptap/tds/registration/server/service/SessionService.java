package com.taptap.tds.registration.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taptap.tds.registration.server.core.datastore.DataStore;
import com.taptap.tds.registration.server.domain.TdsMessage;
import com.taptap.tds.registration.server.domain.UserAction;
import com.taptap.tds.registration.server.enums.ActionType;
import com.taptap.tds.registration.server.session.GameUserPrinciple;
import com.taptap.tds.registration.server.session.LocalPrincipleSocketManager;
import com.taptap.tds.registration.server.session.ReactiveRedisSessionManager;
import com.taptap.tds.registration.server.session.TdsSession;
import com.taptap.tds.registration.server.util.Constants;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * @Author guyu
 * @create 2021/1/13 9:15 下午
 */
@Service
@Log4j2
public class SessionService implements SmartLifecycle {

    @Autowired
    private PublicityService publicityService;

    @Autowired
    private DataStore<UserAction> dataStore;

    @Autowired
    private ReactiveRedisSessionManager reactiveRedisSessionManager;

    @Autowired
    private LocalPrincipleSocketManager localPrincipleSocketManager;

    private volatile boolean isRunning;

    @Autowired
    private ObjectMapper objectMapper;

    @Scheduled(fixedRate = 24*60*60*1000, initialDelayString = "${random.int(1800000)}")
    public void touchAll(){
        log.info("touch all session start : count is {}" , localPrincipleSocketManager.principleSocketConcurrentHashMap.size());
        Flux.fromIterable(localPrincipleSocketManager.principleSocketConcurrentHashMap.values())
                .flatMap(session-> {
                    TdsSession tdsSession = (TdsSession) session.getAttributes().get(Constants.TDS_SESSION_KEY);
                    return reactiveRedisSessionManager.touch(tdsSession, Duration.ofDays(1))
                            .onErrorResume(e -> localPrincipleSocketManager.removeAndClose(tdsSession.getGameUserPrinciple()));
                })
                .doAfterTerminate(() -> log.info("touch all session finished"))
                .subscribe();
    }

    public Mono<Void> sendMessage(GameUserPrinciple gameUserPrinciple, TdsMessage tdsMessage){
        String s;
        try{
            s = objectMapper.writeValueAsString(tdsMessage);
        }catch (JsonProcessingException e) {
            return Mono.error(e);
        }
        WebSocketSession webSocketSession = localPrincipleSocketManager
                .get(gameUserPrinciple);

        return webSocketSession == null ? Mono.empty() : webSocketSession.send(Mono.just(webSocketSession.textMessage(s)));
    }

    public Flux<Void> sendMessage(List<GameUserPrinciple> gameUserPrinciples, TdsMessage tdsMessage){
        String s;
        try{
            s = objectMapper.writeValueAsString(tdsMessage);
        }catch (JsonProcessingException e) {
            return Flux.error(e);
        }
        return Flux.fromIterable(gameUserPrinciples)
                .concatMap(principle->Mono.justOrEmpty(localPrincipleSocketManager.get(principle)))
                .concatMap(webSocketSession->
                        webSocketSession.send(Mono.just(webSocketSession.textMessage(s))));
    }


    public Mono<Void> removeWithoutClose(TdsSession tdsSession){
        WebSocketSession webSocketSession = localPrincipleSocketManager.get(tdsSession.getGameUserPrinciple());
        if(webSocketSession==null){
            return Mono.empty();
        }

        UserAction userAction = new UserAction();
        userAction.setUserId(tdsSession.getGameUserPrinciple().getUserId());
        userAction.setActionType(ActionType.LOGOUT);
        userAction.setSessionId(tdsSession.getId().replace("-", ""));
        userAction.setActionTime(Instant.now());
        dataStore.store(userAction);

        localPrincipleSocketManager
                .remove(tdsSession.getGameUserPrinciple());
        return reactiveRedisSessionManager.deleteTdsSession(tdsSession);
    }

    public Mono<Void> close(TdsSession tdsSession){
        WebSocketSession webSocketSession = localPrincipleSocketManager.get(tdsSession.getGameUserPrinciple());

        if(webSocketSession==null){
            return Mono.empty();
        }

        UserAction userAction = new UserAction();
        userAction.setUserId(tdsSession.getGameUserPrinciple().getUserId());
        userAction.setActionType(ActionType.LOGOUT);
        userAction.setSessionId(tdsSession.getId().replace("-", ""));
        userAction.setActionTime(Instant.now());
        dataStore.store(userAction);

        return localPrincipleSocketManager
                .removeAndClose(tdsSession.getGameUserPrinciple())
                .and(reactiveRedisSessionManager.deleteTdsSession(tdsSession));
    }

    public Mono<Void> close(GameUserPrinciple gameUserPrinciple){

        WebSocketSession webSocketSession = localPrincipleSocketManager.get(gameUserPrinciple);

        if(webSocketSession == null){
            return Mono.empty();
        }
        TdsSession existingTdsSession = (TdsSession) webSocketSession.getAttributes().get(Constants.TDS_SESSION_KEY);
        return close(existingTdsSession);
    }

    public Flux<Void> sendMessageAndCloseMany(List<GameUserPrinciple> gameUserPrinciples, TdsMessage tdsMessage){
        return Flux.fromIterable(gameUserPrinciples)
                // 处理为空的情况
                .flatMap(principle->Mono.justOrEmpty(localPrincipleSocketManager.get(principle)))
                .flatMap(webSocketSession->{
                    String s;
                    try{
                        s = objectMapper.writeValueAsString(tdsMessage);
                    }catch (JsonProcessingException e) {
                        return Flux.error(e);
                    }
                    TdsSession tdsSession = (TdsSession)webSocketSession.getAttributes().get(Constants.TDS_SESSION_KEY);
                    return webSocketSession.send(Mono.just(webSocketSession.textMessage(s)))
                            .onErrorResume(ex -> Mono.empty())
                            .then(close(tdsSession));
                });
    }

    @Override
    public void start() {
        isRunning = true;
    }

    @Override
    public void stop() {
        this.isRunning = false;
        try {
            TimeUnit.SECONDS.sleep(2);
            log.info("睡两秒等原来连接事件做完");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.info("退出前关闭所有session");
        Flux.fromIterable(localPrincipleSocketManager.principleSocketConcurrentHashMap.values())
                .flatMap(webSocketSession->{
                    TdsSession existingTdsSession = (TdsSession) webSocketSession.getAttributes().get(Constants.TDS_SESSION_KEY);
                    return close(existingTdsSession);
                })
                .onErrorContinue((e, data)->log.info("close failed .error is {}", e.getMessage()))
                // TODO 最多做30秒？ 待压测。此外这里的停顿时间中healthcheck还认为是活得会不会有问题
                .blockLast(Duration.ofSeconds(30));
        log.info("关闭所有session完成");

        // 关的时候把时间都落库
        publicityService.bulkSave();
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }
}
