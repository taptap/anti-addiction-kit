package com.taptap.tds.registration.server.session;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;


/**
 * @Author guyu
 * @create 2021/1/11 5:28 下午
 */
@Log4j2
@Component
public class LocalPrincipleSocketManager {

    public ConcurrentHashMap<GameUserPrinciple, WebSocketSession> principleSocketConcurrentHashMap  = new ConcurrentHashMap();

    public void saveLocal(GameUserPrinciple gameUserPrinciple , WebSocketSession socket){
        principleSocketConcurrentHashMap.put(gameUserPrinciple, socket);
    }

    public Mono<Void> removeAndClose(GameUserPrinciple gameUserPrinciple){
        WebSocketSession remove = remove(gameUserPrinciple);
        return remove == null ? Mono.empty() : remove.close()
                .onErrorResume(e -> {
                            log.info("close failed, e is {}", e.getMessage());
                            return Mono.empty();
                });
    }

    public WebSocketSession remove(GameUserPrinciple gameUserPrinciple){
        return principleSocketConcurrentHashMap.remove(gameUserPrinciple);
    }


    public WebSocketSession get(GameUserPrinciple gameUserPrinciple){
        return principleSocketConcurrentHashMap.get(gameUserPrinciple);
    }
}
