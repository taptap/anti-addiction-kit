package com.taptap.tds.registration.server.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taptap.tds.registration.server.AuthorizationException;
import com.taptap.tds.registration.server.configuration.TdsPushProperties;
import com.taptap.tds.registration.server.core.datastore.DataStore;
import com.taptap.tds.registration.server.domain.TdsMessage;
import com.taptap.tds.registration.server.domain.UserAction;
import com.taptap.tds.registration.server.enums.ActionType;
import com.taptap.tds.registration.server.service.AuthorizationService;
import com.taptap.tds.registration.server.service.SessionService;
import com.taptap.tds.registration.server.session.LocalPrincipleSocketManager;
import com.taptap.tds.registration.server.session.ReactiveRedisSessionManager;
import com.taptap.tds.registration.server.session.TdsSession;
import com.taptap.tds.registration.server.util.Constants;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * @Author guyu
 * @create 2020/12/24 3:59 下午
 */
@Log4j2
public class PushWebSocketHandler implements WebSocketHandler {

    @Autowired
    private DataStore<UserAction> dataStore;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private TdsPushProperties tdsPushProperties;

    @Autowired
    private ReactiveRedisSessionManager reactiveRedisSessionManager;

    @Autowired
    private LocalPrincipleSocketManager localPrincipleSocketManager;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthorizationService authorizationService;

    public PushWebSocketHandler(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        if(!sessionService.isRunning()) {
            return Mono.empty();
        }
        return authorizationService.getGameUserPrinciple(session.getHandshakeInfo())
                .doOnError(AuthorizationException.class, ex->sendLoginErrorMessage(session, ex))
                // 其他异常？ 比如grpc 直接关掉
                .doOnError(e->log.info(e.getMessage()))
                .map(gameUserPrinciple-> reactiveRedisSessionManager.createSession(gameUserPrinciple))
                .flatMap(tdsSession -> reactiveRedisSessionManager.saveSession(tdsSession, tdsPushProperties.getTtl())
                        .flatMap(isSuccess->{
                             if(isSuccess) {
                                 session.getAttributes().put(Constants.TDS_SESSION_KEY, tdsSession);
                                 localPrincipleSocketManager.saveLocal(tdsSession.getGameUserPrinciple(), session);
                                 UserAction userAction = new UserAction();
                                 userAction.setUserId(tdsSession.getGameUserPrinciple().getUserId());
                                 userAction.setActionType(ActionType.LOGIN);
                                 userAction.setSessionId(tdsSession.getId().replace("-", ""));
                                 userAction.setActionTime(Instant.now());
                                 dataStore.store(userAction);
                                return handleInbound(session, tdsSession);
                            }else{
                               return sendKickMessage(session).then(session.close());
                            }
                })).doOnError(e->log.info("login failed cause is {}, url is {}", e.getMessage(), session.getHandshakeInfo().getUri()));
    }


    private Mono<?> sendKickMessage(WebSocketSession session){
        return session.send(Mono.just(session.textMessage(Constants.CLOSE_MESSAGE))).onErrorResume(ex->Mono.empty());
    }

    private void sendLoginErrorMessage(WebSocketSession session, AuthorizationException ex){
        TdsMessage tdsMessage = new TdsMessage();
        tdsMessage.getHeader().put("path", "/session/error");
        tdsMessage.setBody(ex.getMessage());
        String s;
        try {
            s = objectMapper.writeValueAsString(tdsMessage);
        } catch(Exception ignore){
            log.error("ObjectMapper exception e :" + ignore.getMessage());
            return;
        }
        session.send(Mono.just(session.textMessage(s)))
                .onErrorResume(e->Mono.empty())
                .then(session.close()).subscribe();
    }

    private Mono<Void> handleInbound(WebSocketSession session , TdsSession tdsSession){

        // 等于说啥都不做
        Flux<WebSocketMessage> inboundFlux = session.receive()
                .doFinally(type-> sessionService.removeWithoutClose(tdsSession).subscribe());
        return inboundFlux.then();
    }

}
