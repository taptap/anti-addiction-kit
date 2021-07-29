package com.taptap.tds.registration.server.service;

import com.taptap.tds.registration.server.session.GameUserPrinciple;
import org.springframework.web.reactive.socket.HandshakeInfo;
import reactor.core.publisher.Mono;

/**
 * @Author guyu
 * @create 2021/3/4 11:46 上午
 */
public interface AuthorizationService {

    Mono<GameUserPrinciple> getGameUserPrinciple(HandshakeInfo handshakeInfo);

}
