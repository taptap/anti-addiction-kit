package com.tapsdk.antiaddiction.utils;

import android.text.format.DateFormat;

import com.tapsdk.antiaddiction.constants.Constants;
import com.tapsdk.antiaddiction.entities.ChildProtectedConfig;
import com.tapsdk.antiaddiction.settings.AntiAddictionSettings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtil {

    public static final int SECONDS_IN_DAY = 60 * 60 * 24;
    public static final long MILLIS_IN_DAY = 1000L * SECONDS_IN_DAY;

    public static int getDefaultRemainTime(int accountType, long serverTime, ChildProtectedConfig childProtectedConfig) {
        if (accountType == Constants.UserType.USER_TYPE_UNKNOWN) return childProtectedConfig.guestTime;
        if (isHoliday(serverTime)) {
            return childProtectedConfig.childHolidayTime;
        } else {
            return childProtectedConfig.childCommonTime;
        }
    }

    public static boolean isHoliday(long time) {
        long currentTime = time * 1000;
        SimpleDateFormat formatter = new SimpleDateFormat("MM:dd", Locale.getDefault());
        String dateString = formatter.format(currentTime);
        int month = Integer.parseInt(dropZero(dateString.substring(0, 2)));
        int day = Integer.parseInt(dropZero(dateString.substring(3)));
        String current = month + "." + (day > 9 ? day : "0" + day);
        //考虑到单机游戏，暂时假日写死
        String days = "1.01,2.12,2.13,2.14,4.04,4.05,4.06,5.01,5.02,5.03,5.04,5.05" +
                "6.25,10.01,10.02,10.03";
        if (days.contains(current)) {
            return true;
        }
        return false;
    }

    private static String dropZero(String str) {
        if (str != null && str.length() > 1) {
            if (str.startsWith("0")) {
                return str.substring(1);
            } else {
                return str;
            }
        } else {
            return str;
        }
    }

    //返回分钟,例如22：10返回22*3600 + 10 * 60
    public static int getTimeByClock(String clock) {
        if (clock == null || clock.length() == 0) {
            return 0;
        }
        int hour = Integer.parseInt(dropZero(clock.substring(0, 2)));
        int min = Integer.parseInt(dropZero(clock.substring(3)));
        return hour * 3600 + min * 60;
    }

    public static boolean isSameDayOfMillis(final long ms1, final long ms2) {
        final long interval = ms1 - ms2;
        return interval < MILLIS_IN_DAY
                && interval > -1L * MILLIS_IN_DAY
                && toDay(ms1) == toDay(ms2);
    }

    private static long toDay(long millis) {
        return (millis + TimeZone.getDefault().getOffset(millis)) / MILLIS_IN_DAY;
    }

    public static Date tomorrow(Date today) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1);
        return calendar.getTime();
    }

    /**
     * 计算和当前时间的差值
     *
     * @param dateStr
     * @return
     */
    public static int diffNow(String dateStr, long serverTime) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = null;
        try {
            date = fmt.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date == null) return Integer.MAX_VALUE;
        Date curDate = new Date(serverTime);
        return (int) ((date.getTime() - curDate.getTime()) / 1000);
    }

    public static int getTimeToNightStrict(String nightStrictStartTime, String nightStrictEndTime, long serverTime) {
        Date now = new Date(serverTime);
        SimpleDateFormat ft = new SimpleDateFormat("HH:mm");
        String nowStr = ft.format(now);
        long comp = nowStr.compareTo(nightStrictStartTime) * nowStr.compareTo(nightStrictEndTime);
        if (comp <= 0L && nowStr.compareTo(nightStrictEndTime) < 0) {
            return 0;
        }
        SimpleDateFormat ftd = new SimpleDateFormat("yyyy-MM-dd");
        String dStr = "";
        // 同一天
        if (nowStr.compareTo(nightStrictStartTime) < 0) {
            dStr = ftd.format(now);
        }
        // 非同一天
        else {
            Date tomm = tomorrow(new Date());
            dStr = ftd.format(tomm);
        }
        String strictStartStr = dStr + " " + nightStrictStartTime;
        return diffNow(strictStartStr, serverTime);
    }

    public static String getFullTime(long timeInMillis) {
        try {
            CharSequence val = DateFormat.format("yyyy-MM-dd HH:mm:ss", new Date(timeInMillis));
            return val.toString();
        } catch (NumberFormatException e) {
            return "";
        }
    }

    public static int getAntiAddictionTime(int accountType, ChildProtectedConfig config, long serverTime) {
        int gameTime = 0;
        if (accountType == 5 || accountType == 0) {
            gameTime = config.noIdentifyTime;
        } else if (AntiAddictionSettings.getInstance().isHolidayInMillis(new Date(serverTime).getTime())) {
            gameTime = config.childHolidayTime;
        } else {
            gameTime = config.childCommonTime;
        }
        return gameTime;
    }

    public static int getMinute(int remainTime) {
        int min = remainTime / 60;
        if (remainTime % 60 != 0) {
            min += 1;
        }
        return min;
    }
}
