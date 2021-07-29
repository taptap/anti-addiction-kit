package com.taptap.tds.registration.server.session;

import com.taptap.tds.registration.server.PushGatewayRegister;
import com.taptap.tds.registration.server.domain.KickInfo;
import com.taptap.tds.registration.server.service.SessionService;
import com.taptap.tds.registration.server.util.PushGatewayMessageBuilder;
import com.taptap.tds.registration.server.util.Constants;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * @Author guyu
 * @create 2021/1/11 5:01 下午
 */
@Log4j2
@Component
public class DoubleKickStrategy implements SessionKickStrategy{

    @Autowired
    private PushGatewayRegister pushGatewayRegister;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PushGatewayMessageBuilder pushGatewayMessageBuilder;

    @Autowired
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @Override
    public Mono<Void> onKick(GameUserPrinciple gameUserPrinciple, String lastServerId) {

        log.info("on kick.principle is {}", gameUserPrinciple);
        if(pushGatewayRegister.getPushGatewayMetadata().getServerId().equals(lastServerId)){
            return sessionService.sendMessage(gameUserPrinciple, pushGatewayMessageBuilder.buildCloseMessage(false, "/session/kick"))
                    .then(sessionService.close(gameUserPrinciple))
                    .onErrorResume(ex -> Mono.empty());
        }

        // otherwise pub command to other instances
        KickInfo ki = new KickInfo();
        ki.setServerId(lastServerId);
        ki.setGameUserPrinciple(gameUserPrinciple);
        return reactiveRedisTemplate.convertAndSend(Constants.KICK_TOPIC, ki).then();

    }

}
