package com.taptap.tds.registration.server.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @Author guyu
 * @create 2020/12/31 11:09 上午
 */
@Configuration
public class RedisConfiguration {

    @Bean
    public ReactiveRedisTemplate reactiveRedisTemplate(
            ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
        RedisSerializationContext serializationContext = RedisSerializationContext
                .<String, Object>newSerializationContext().key(RedisSerializer.string()).value(RedisSerializer.json()).hashKey(RedisSerializer.string())
                .hashValue(RedisSerializer.json()).build();
        return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, serializationContext);
    }

}
