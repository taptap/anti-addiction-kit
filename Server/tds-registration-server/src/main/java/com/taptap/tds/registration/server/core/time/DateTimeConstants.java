package com.taptap.tds.registration.server.core.time;

public final class DateTimeConstants {

    /** Milliseconds in one second (1000) (ISO) */
    public static final long MILLIS_PER_SECOND = 1000;

    /** Seconds in one minute (60) (ISO) */
    public static final long SECONDS_PER_MINUTE = 60;

    /** Milliseconds in one minute (ISO) */
    public static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * SECONDS_PER_MINUTE;

    /** Minutes in one hour (ISO) */
    public static final long MINUTES_PER_HOUR = 60;

    /** Seconds in one hour (ISO) */
    public static final long SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;

    /** Milliseconds in one hour (ISO) */
    public static final long MILLIS_PER_HOUR = MILLIS_PER_MINUTE * MINUTES_PER_HOUR;

    /** Hours in a typical day (24) (ISO). Due to time zone offset changes, the
     * number of hours per day can vary. */
    public static final long HOURS_PER_DAY = 24;

    /** Minutes in a typical day (ISO). Due to time zone offset changes, the number
     * of minutes per day can vary. */
    public static final long MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY;

    /** Seconds in a typical day (ISO). Due to time zone offset changes, the number
     * of seconds per day can vary. */
    public static final long SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;

    /** Milliseconds in a typical day (ISO). Due to time zone offset changes, the
     * number of milliseconds per day can vary. */
    public static final long MILLIS_PER_DAY = MILLIS_PER_HOUR * HOURS_PER_DAY;

    /** Days in one week (7) (ISO) */
    public static final long DAYS_PER_WEEK = 7;

    /** Hours in a typical week. Due to time zone offset changes, the number of
     * hours per week can vary. */
    public static final long HOURS_PER_WEEK = HOURS_PER_DAY * DAYS_PER_WEEK;

    /** Minutes in a typical week (ISO). Due to time zone offset changes, the number
     * of minutes per week can vary. */
    public static final long MINUTES_PER_WEEK = MINUTES_PER_DAY * DAYS_PER_WEEK;

    /** Seconds in a typical week (ISO). Due to time zone offset changes, the number
     * of seconds per week can vary. */
    public static final long SECONDS_PER_WEEK = SECONDS_PER_DAY * DAYS_PER_WEEK;

    /** Milliseconds in a typical week (ISO). Due to time zone offset changes, the
     * number of milliseconds per week can vary. */
    public static final long MILLIS_PER_WEEK = MILLIS_PER_DAY * DAYS_PER_WEEK;

    private DateTimeConstants() {
        throw new AssertionError();
    }
}
