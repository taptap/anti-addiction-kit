package com.taptap.tds.registration.server.core.jackson;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.*;
import com.taptap.tds.registration.server.core.enums.EnumValue;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class JacksonUtils {

    private static final ConcurrentMap<Class<?>, ObjectReader> OBJECT_READER_CACHE = new ConcurrentHashMap<>();

    private static final ConcurrentMap<JavaType, ObjectReader> JAVA_TYPE_OBJECT_READER_CACHE = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<?>, ObjectWriter> OBJECT_WRITER_CACHE = new ConcurrentHashMap<>();

    private static final Jackson2ObjectMapperBuilder BUILDER = Jackson2ObjectMapperBuilder.json()
            .serializationInclusion(Include.NON_EMPTY).failOnUnknownProperties(false).timeZone(TimeZone.getDefault())
            .featuresToEnable(MapperFeature.PROPAGATE_TRANSIENT_MARKER)
            .featuresToEnable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            .featuresToEnable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .serializerByType(EnumValue.class, new EnumValueSerializer());

    private JacksonUtils() {
        throw new AssertionError();
    }

    public static Jackson2ObjectMapperBuilder getDefaultJackson2ObjectMapperBuilder() {
        return BUILDER;
    }

    public static <T extends ObjectMapper> T configureDefault(T mapper) {
        BUILDER.configure(mapper);
        return mapper;
    }

    public static ObjectWriter getObjectWriter(ObjectMapper mapper, Class<?> type) {
        return OBJECT_WRITER_CACHE.computeIfAbsent(type, mapper::writerFor);
    }

    public static ObjectReader getObjectReader(ObjectMapper mapper, Class<?> type) {
        return OBJECT_READER_CACHE.computeIfAbsent(type, mapper::readerFor);
    }

    public static ObjectReader getObjectReader(ObjectMapper mapper, JavaType type) {
        return JAVA_TYPE_OBJECT_READER_CACHE.computeIfAbsent(type, mapper::readerFor);
    }
}
