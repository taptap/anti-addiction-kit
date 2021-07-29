package com.taptap.tds.registration.server.configuration;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.taptap.tds.registration.server.core.enums.EnumValue;
import com.taptap.tds.registration.server.core.jackson.EnumValueSerializer;
import com.taptap.tds.registration.server.core.time.DateTimeFormatterCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author yu.gu
 */
@Configuration
@ConditionalOnClass(ObjectMapper.class)
public class CustomJacksonConfiguration {

    public static final String DEFAULT_JACKSON_OBJECT_MAPPER_BUILDER_CUSTOMIZER_BEAN_NAME = "defaultJackson2ObjectMapperBuilderCustomizer";

    @Bean
    @ConditionalOnMissingBean(name = DEFAULT_JACKSON_OBJECT_MAPPER_BUILDER_CUSTOMIZER_BEAN_NAME)
    public Jackson2ObjectMapperBuilderCustomizer defaultJackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.serializationInclusion(Include.NON_EMPTY).failOnUnknownProperties(false)
                .featuresToEnable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .featuresToEnable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
                .featuresToEnable(MapperFeature.PROPAGATE_TRANSIENT_MARKER)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .serializerByType(EnumValue.class, new EnumValueSerializer());
    }

    @Bean
    @ConditionalOnMissingBean
    public ParameterNamesModule parameterNamesModule() {
        return new ParameterNamesModule();
    }

    @Configuration
    static class Jsr310JacksonConfiguration {

        private final JacksonProperties jacksonProperties;

        Jsr310JacksonConfiguration(JacksonProperties jacksonProperties) {
            this.jacksonProperties = jacksonProperties;
        }

        @Bean
        public SimpleModule jsr310SerializationModule() {
            SimpleModule module = new SimpleModule();
            ZoneId zone;
            if (this.jacksonProperties.getTimeZone() != null) {
                zone = this.jacksonProperties.getTimeZone().toZoneId();
            } else {
                zone = ZoneOffset.UTC;
            }
            DateTimeFormatter formatter;
            if (this.jacksonProperties.getDateFormat() != null) {
                formatter = DateTimeFormatterCache.ofPattern(this.jacksonProperties.getDateFormat()).withZone(zone);
            } else {
                formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(zone);
            }
            module.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer(formatter));
            return module;
        }
    }
}
