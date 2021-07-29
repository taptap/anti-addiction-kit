package com.taptap.tds.registration.server.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurationSupport;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver;
import org.springframework.web.server.i18n.LocaleContextResolver;

import java.util.Locale;

/**
 * @Author guyu
 * @create 2020/11/25 4:50 下午
 */
@Configuration
public class WebfluxCustomConfiguration extends WebFluxConfigurationSupport {


    private final ServerProperties serverProperties;
    private final MessageSource messageSource;

    public WebfluxCustomConfiguration(ServerProperties serverProperties, MessageSource messageSource) {
        this.serverProperties = serverProperties;
        this.messageSource = messageSource;
    }

    // 改webflux Handlermapping顺序 放到gateway之后
    @Override
    public RequestMappingHandlerMapping requestMappingHandlerMapping(
            @Qualifier("webFluxContentTypeResolver") RequestedContentTypeResolver contentTypeResolver) {
        RequestMappingHandlerMapping requestMappingHandlerMapping = super.requestMappingHandlerMapping(contentTypeResolver);
        requestMappingHandlerMapping.setOrder(2);
        return requestMappingHandlerMapping;
    }

    @Autowired
    private Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;

    @SuppressWarnings("unchecked")
    @Bean
    public ObjectMapper objectMapper() {
        return jackson2ObjectMapperBuilder
                .featuresToDisable(
                        JsonGenerator.Feature.IGNORE_UNKNOWN,
                        MapperFeature.DEFAULT_VIEW_INCLUSION,
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
                )
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .propertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                .build();
    }

    @Override
    protected void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        ObjectMapper objectMapper = objectMapper();
        configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
        configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
    }

//    @Bean
//    @Order(-1)
//    public ErrorWebExceptionHandler jsonExceptionHandler(ErrorAttributes errorAttributes,
//                                                         ResourceProperties resourceProperties, ObjectProvider<ViewResolver> viewResolvers,
//                                                         ServerCodecConfigurer serverCodecConfigurer, ApplicationContext applicationContext) {
//        JsonExceptionHandler exceptionHandler = new JsonExceptionHandler(errorAttributes,
//                resourceProperties, this.serverProperties.getError(), applicationContext);
//        exceptionHandler.setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()));
//        exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
//        exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
//        return exceptionHandler;
//    }

    // 这个相当于RestControllerAdvice
//    @Bean
//    public ResponseBodyResultHandler responseBodyResultHandler(
//            @Qualifier("webFluxAdapterRegistry") ReactiveAdapterRegistry reactiveAdapterRegistry,
//            ServerCodecConfigurer serverCodecConfigurer,
//            @Qualifier("webFluxContentTypeResolver") RequestedContentTypeResolver contentTypeResolver) {
//        return new ApiResponseResultHandler(serverCodecConfigurer.getWriters(),
//                contentTypeResolver, reactiveAdapterRegistry);
//    }

    @Override
    public Validator webFluxValidator() {
        LocalValidatorFactoryBean validator = (LocalValidatorFactoryBean) super.webFluxValidator();
        validator.setValidationMessageSource(messageSource);
        return validator;
    }

    @Override
    protected LocaleContextResolver createLocaleContextResolver() {
        AcceptHeaderLocaleContextResolver acceptHeaderLocaleContextResolver = new AcceptHeaderLocaleContextResolver();
        acceptHeaderLocaleContextResolver.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        return acceptHeaderLocaleContextResolver;
    }

//    // TODO CHECK
//    @Override
//    protected void configureContentTypeResolver(RequestedContentTypeResolverBuilder builder) {
//        builder.fixedResolver(MediaType.APPLICATION_JSON);
//    }

    @Override
    protected void addCorsMappings(CorsRegistry registry) {
        long maxAge = 30 * 24 * 60 * 60;
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedHeaders("*")
                .allowedMethods("*")
                .maxAge(maxAge);
    }

}
