package com.taptap.tds.registration.server.configuration;

import ma.glasnost.orika.MapperFacade;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(MapperFacade.class)
public class BeanMapperConfiguration {

    @Bean
    public BeanMapper beanMapper() {
        return new BeanMapper();
    }
}