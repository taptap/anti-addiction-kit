package com.taptap.tds.registration.server.core.time;

import org.springframework.core.convert.converter.Converter;

import java.time.ZonedDateTime;
import java.util.Date;

public enum ZonedDateTimeToDateConverter implements Converter<ZonedDateTime, Date> {

    INSTANCE;

    @Override
    public Date convert(ZonedDateTime source) {
        return source == null ? null : Date.from(source.toInstant());
    }
}
