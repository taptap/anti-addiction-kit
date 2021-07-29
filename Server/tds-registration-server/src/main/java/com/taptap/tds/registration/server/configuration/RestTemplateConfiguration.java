package com.taptap.tds.registration.server.configuration;

import com.taptap.tds.registration.server.SignInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateRequestCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Configuration
public class RestTemplateConfiguration {

    @Autowired
    private PublicityProperties properties;

    @Autowired
    private SignInterceptor signInterceptor;


    @Bean
    public RestTemplateBuilder restTemplateBuilder(ObjectProvider<HttpMessageConverters> messageConverters,
                                                   ObjectProvider<RestTemplateCustomizer> restTemplateCustomizers,
                                                   ObjectProvider<RestTemplateRequestCustomizer<?>> restTemplateRequestCustomizers) {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        HttpMessageConverters converters = messageConverters.getIfUnique();
        if (converters != null) {
            builder = builder.messageConverters(converters.getConverters());
        }
        builder = addCustomizers(builder, restTemplateCustomizers, RestTemplateBuilder::customizers);
        builder = addCustomizers(builder, restTemplateRequestCustomizers, RestTemplateBuilder::requestCustomizers);
        return builder;
    }

    private <T> RestTemplateBuilder addCustomizers(RestTemplateBuilder builder, ObjectProvider<T> objectProvider,
                                                   BiFunction<RestTemplateBuilder, Collection<T>, RestTemplateBuilder> method) {
        List<T> customizers = objectProvider.orderedStream().collect(Collectors.toList());
        if (!customizers.isEmpty()) {
            return method.apply(builder, customizers);
        }
        return builder;
    }

    @Bean
    public RestTemplate userActionRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        RestTemplate restTemplate = restTemplateBuilder.rootUri(properties.getUserActionRootRui()).build();
        restTemplate.getInterceptors().add(this.signInterceptor);
        return restTemplate;
    }

    @Bean
    public RestTemplate identificationRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        RestTemplate restTemplate = restTemplateBuilder.rootUri(properties.getIdentificationRootUri()).build();
        restTemplate.getInterceptors().add(this.signInterceptor);
        return restTemplate;
    }

    @Bean
    public RestTemplate checkRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        RestTemplate restTemplate = restTemplateBuilder.rootUri(properties.getIdentificationCheckRootUri()).build();
        restTemplate.getInterceptors().add(this.signInterceptor);
        return restTemplate;
    }

}
