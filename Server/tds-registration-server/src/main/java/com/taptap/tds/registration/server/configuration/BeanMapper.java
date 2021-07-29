package com.taptap.tds.registration.server.configuration;

import com.taptap.tds.registration.server.core.orika.EnumValueConverter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.converter.builtin.PassThroughConverter;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import java.time.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * 简单封装orika, 实现深度转换Bean<->Bean的Mapper.
 */
public class BeanMapper {

    private final MapperFacade mapper = createDefaultMapperFactory().getMapperFacade();

    public static MapperFactory createDefaultMapperFactory() {

        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().mapNulls(false).build();
        ConverterFactory converterFactory = mapperFactory.getConverterFactory();
        converterFactory.registerConverter(new PassThroughConverter(ZonedDateTime.class, OffsetDateTime.class, OffsetTime.class,
                LocalDateTime.class, LocalDate.class, LocalTime.class, Year.class, YearMonth.class, MonthDay.class, Instant.class,
                Period.class, Duration.class));
        converterFactory.registerConverter(new EnumValueConverter());

        return mapperFactory;
    }

    public <S, D> D map(S source, Class<D> destinationClass) {
        return mapper.map(source, destinationClass);
    }

    public <S, D> void map(S source, D destination) {
        if (source == null || destination == null) {
            return;
        }
        mapper.map(source, destination);
    }

    public <S, D> D map(S source, Class<D> destinationClass, BiConsumer<S, D> customizer) {
        if (source == null) {
            return null;
        }
        D result = map(source, destinationClass);
        if (customizer != null) {
            customizer.accept(source, result);
        }
        return result;
    }

    public <S, D> List<D> mapList(Iterable<S> sourceList, Class<D> destinationClass) {
        return mapper.mapAsList(sourceList, destinationClass);
    }

    public <S, D> List<D> mapList(Iterable<S> sourceList, Class<D> destinationClass, BiConsumer<S, D> customizer) {
        if (sourceList == null || !sourceList.iterator().hasNext()) {
            return Collections.emptyList();
        }
        if (customizer == null) {
            return mapList(sourceList, destinationClass);
        }
        List<D> results;
        if (sourceList instanceof Collection) {
            Collection<S> collection = (Collection<S>) sourceList;
            results = new ArrayList<>(collection.size());
        } else {
            results = new ArrayList<>();
        }
        for (S source : sourceList) {
            D result = map(source, destinationClass, customizer);
            results.add(result);
        }
        return results;
    }
}
