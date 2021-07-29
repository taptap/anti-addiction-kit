package com.taptap.tds.registration.server.core.time;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DateTimeFormatterCache {

    private static final ConcurrentMap<String, DateTimeFormatter> CACHE = new ConcurrentHashMap<>();

    public static DateTimeFormatter ofPattern(String format) {
        return CACHE.computeIfAbsent(format, DateTimeFormatter::ofPattern);
    }
}
