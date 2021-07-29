package com.taptap.tds.registration.server.session;

import reactor.core.publisher.Mono;

/**
 * @Author guyu
 * @create 2021/1/11 4:59 下午
 */
public interface SessionKickStrategy {

    Mono<Void> onKick(GameUserPrinciple gameUserPrinciple, String lastServerId);

}
