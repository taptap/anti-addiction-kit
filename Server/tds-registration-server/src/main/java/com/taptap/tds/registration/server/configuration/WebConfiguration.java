package com.taptap.tds.registration.server.configuration;

import com.taptap.tds.registration.server.netty.PushWebSocketHandler;
import com.taptap.tds.registration.server.service.AuthorizationService;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.Shutdown;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author guyu
 * @create 2020/12/28 11:43 上午
 */
@Configuration
public class WebConfiguration {

    @Autowired
    private TdsPushProperties tdsPushProperties;

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    public PushWebSocketHandler xdPushWebSocketHandler(AuthorizationService xdAutorizationService){
        return new PushWebSocketHandler(xdAutorizationService);
    }

    @Bean
    public HandlerMapping handlerMapping(PushWebSocketHandler xdPushWebSocketHandler) {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/ws/v1", xdPushWebSocketHandler);
        int order = -1; // before annotated controllers
        return new SimpleUrlHandlerMapping(map, order);
    }

    @Bean
    public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> idleNettyWebServerFactoryCustomizer(){
        return factory -> {
                factory.addServerCustomizers(
                    server-> server.tcpConfiguration(tcp -> tcp.doOnConnection(
                        connection -> connection.addHandlerFirst("idle",new ReadTimeoutHandler(tdsPushProperties.getPushTimeOut())))));
                factory.setShutdown(Shutdown.GRACEFUL);
        };
    }

}
