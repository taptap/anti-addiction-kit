package com.taptap.tds.registration.server.core.time;

import org.springframework.core.convert.converter.Converter;

import java.time.ZonedDateTime;
import java.util.Date;

import static java.time.ZoneId.systemDefault;

public enum DateToZonedDateTimeConverter implements Converter<Date, ZonedDateTime> {

    INSTANCE;

    @Override
    public ZonedDateTime convert(Date source) {
        return source == null ? null : ZonedDateTime.ofInstant(source.toInstant(), systemDefault());
    }
}
