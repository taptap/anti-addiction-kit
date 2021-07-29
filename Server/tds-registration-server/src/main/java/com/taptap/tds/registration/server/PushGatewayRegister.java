package com.taptap.tds.registration.server;

import com.taptap.tds.registration.server.configuration.TdsPushProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @Author guyu
 * @create 2021/1/8 3:57 下午
 */
@EnableScheduling
@Component
public class PushGatewayRegister implements InitializingBean {

    @Autowired
    private TdsPushProperties pushProperties;

    private PushGatewayMetadata pushGatewayMetadata = new PushGatewayMetadata();

    @Autowired
    private ReactiveServerHealthChecker reactiveServerHealthChecker;

    private static final String HASH_KEY = "push:gateway:register";

    @Autowired
    private ReactiveRedisTemplate<String, Object>  reactiveRedisTemplate;

    public PushGatewayMetadata getPushGatewayMetadata(){
        return this.pushGatewayMetadata;
    }

    @Override
    public void afterPropertiesSet() {
        pushGatewayMetadata.setServerId(UUID.randomUUID().toString());
    }

    @Scheduled(cron = "0/1 * * * * ?")
    public void touch(){
        reactiveRedisTemplate.opsForValue().set(ReactiveServerHealthChecker.SERVER_PREFIX + pushGatewayMetadata.getServerId()
                ,System.currentTimeMillis()).subscribe();
    }

}
