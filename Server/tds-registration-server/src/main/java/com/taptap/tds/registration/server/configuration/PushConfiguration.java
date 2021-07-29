package com.taptap.tds.registration.server.configuration;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.taptap.tds.registration.server.domain.KickInfo;
import com.taptap.tds.registration.server.domain.TdsServiceMessage;
import com.taptap.tds.registration.server.service.SessionService;
import com.taptap.tds.registration.server.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.redis.inbound.RedisInboundChannelAdapter;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Arrays;
import java.util.List;

/**
 * @Author guyu
 * @create 2020/12/30 4:46 下午
 */
@EnableConfigurationProperties(TdsPushProperties.class)
@Configuration
public class PushConfiguration {

    @Autowired
    private TdsPushProperties tdsPushProperties;

    @Autowired
    private SessionService sessionService;

    @Bean
    public JWTVerifier jwtVerifier(){
        return JWT.require(Algorithm.HMAC256(tdsPushProperties.getJws())).build();
    }

    @Bean
    @SuppressWarnings("unchecked")
    public RedisScript casSaveScript() {
        DefaultRedisScript redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(
                new ClassPathResource("redis/save.lua")));
        redisScript.setResultType(List.class);
        return redisScript;
    }

    @Bean
    public RedisInboundChannelAdapter redisInboundChannelAdapter(RedisConnectionFactory connectionFactory){
        RedisInboundChannelAdapter redisInboundChannelAdapter = new RedisInboundChannelAdapter(connectionFactory);
        redisInboundChannelAdapter.setTopics(Constants.KICK_TOPIC);
        redisInboundChannelAdapter.setSerializer(RedisSerializer.json());
        redisInboundChannelAdapter.setOutputChannelName(KICK_CHANNEL);
        return redisInboundChannelAdapter;
    }

    private final static String KICK_CHANNEL = "kickChannel";

    @ServiceActivator(inputChannel = KICK_CHANNEL)
    public void kick(KickInfo kickInfo){
        TdsServiceMessage tdsMessage = new TdsServiceMessage();
        tdsMessage.getHeader().put("path", "/session/kick");
        tdsMessage.setBody(Constants.KAFKA_CLOSE_COMMAND_BODY);
        sessionService.sendMessageAndCloseMany(Arrays.asList(kickInfo.getGameUserPrinciple()), tdsMessage).subscribe();
    }


}
