package com.tapsdk.antiaddiction.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.GsonBuilder;
import com.tapsdk.antiaddiction.constants.Constants;
import com.tapsdk.antiaddiction.entities.AntiAddictionConfig;
import com.tapsdk.antiaddiction.entities.HealthReminderWordsGroup;
import com.tapsdk.antiaddiction.entities.Tip;
import com.tapsdk.antiaddiction.entities.TwoTuple;
import com.tapsdk.antiaddiction.skynet.okio.ByteString;
import com.tapsdk.antiaddiction.utils.AntiAddictionLogger;
import com.tapsdk.antiaddiction.utils.FileUtil;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AntiAddictionSettings {

    private static final String COUNT_TIME = "count_time";
    private static final String COUNT_TIME_KEY = "game_time_count";
    private Map<String, SharedPreferences> spCache = new HashMap<>();

    private AntiAddictionConfig commonConfig = null;
    // tuple first param is title and second param is description
    private final Map<Integer, Map<Integer, TwoTuple<String, String>>> tipDict = new HashMap<>();
    private Set<String> holidaySet = new HashSet<>();

    static class Holder {
        static AntiAddictionSettings INSTANCE = new AntiAddictionSettings();
    }

    private AntiAddictionSettings() {
    }

    public static AntiAddictionSettings getInstance() {
        return Holder.INSTANCE;
    }

    private String getSPNameByToken(String userId) {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(userId)) {
            sb.append(ByteString.encodeString(userId, StandardCharsets.UTF_8).md5()).append("_");
        }
        return sb.append(COUNT_TIME_KEY).toString();
    }

    public SharedPreferences getSpecificSharedPreference(Context context, String spName) {
        if (spCache.get(spName) != null) {
            return spCache.get(spName);
        }
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        spCache.put(spName, sp);
        return sp;
    }

    public String getCountTime(Context context, String userId) {
        return getSpecificSharedPreference(context, getSPNameByToken(userId)).getString(COUNT_TIME, "");
    }

    public void saveCountTime(Context context, String userId, long start, long end, long localStart, long localEnd) {
        SharedPreferences.Editor editor = getSpecificSharedPreference(context, getSPNameByToken(userId)).edit();
        String hasSaved = AntiAddictionSettings.getInstance().getCountTime(context, userId);
        AntiAddictionLogger.d("saveCountTime:" + hasSaved);
        String result = hasSaved +
                start +
                "," +
                end +
                "," +
                localStart +
                "," +
                localEnd +
                ";";
        editor.putString(COUNT_TIME, result);
        editor.apply();
    }

    public void clearCountTime(Context context, String userId) {
        SharedPreferences.Editor editor = getSpecificSharedPreference(context, getSPNameByToken(userId)).edit();
        editor.putString(COUNT_TIME, "");
        editor.apply();
    }

    private void initTipDict(List<HealthReminderWordsGroup> healthReminderWordsList) {
        tipDict.clear();
        for (int i = 0; i < healthReminderWordsList.size(); i++) {
            HealthReminderWordsGroup wordsGroup = healthReminderWordsList.get(i);
            int accountType = wordsGroup.accountType;
            Map<Integer, TwoTuple<String, String>> dict = new HashMap<>();
            List<Tip> tipList = wordsGroup.tipList;
            for (int j = 0 ; j < tipList.size() ; j ++) {
                Tip tip = tipList.get(j);
                dict.put(tip.type, TwoTuple.create(tip.title, tip.description));
            }
            tipDict.put(accountType, dict);
        }
    }

    public void setCommonConfig(AntiAddictionConfig antiAddictionConfig) {
        if (antiAddictionConfig == null) return;
        if (antiAddictionConfig.uiConfig != null) initTipDict(antiAddictionConfig.uiConfig.healthReminderWords);
        initHolidaySet(antiAddictionConfig.holiday);
        this.commonConfig = antiAddictionConfig;
    }

    public AntiAddictionConfig getCommonConfig() {
        return commonConfig;
    }

    /**
     * 获取默认的配置文件
     * @return
     */
    public AntiAddictionConfig getCommonDefaultConfig(Context context) {
        AntiAddictionConfig defaultConfig = null;
        try {
            defaultConfig = new GsonBuilder().create().fromJson(
                    FileUtil.getJsonFromAssetsFile(context, "default_config.json"), AntiAddictionConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultConfig;
    }

    public TwoTuple<String, String> getAntiAddictionFeedBack(int accountType, int type) {
        if (accountType != Constants.UserType.USER_TYPE_UNREALNAME && accountType != Constants.UserType.USER_TYPE_UNKNOWN) {
            accountType = Constants.UserType.USER_TYPE_CHILD;
        }

        if (tipDict != null
                && tipDict.containsKey(accountType)
                && tipDict.containsKey(accountType) && tipDict.get(accountType).containsKey(type)) return tipDict.get(accountType).get(type);
        return TwoTuple.create("", "");
    }

    public void initHolidaySet(List<String> holidays) {
        holidaySet = new HashSet<>(holidays);
    }


    public boolean isHolidayInMillis(long timeMillis) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM.dd");
        AntiAddictionLogger.d("isHolidayInMillis:" + formatter.format(timeMillis));
        return isHoliday(formatter.format(timeMillis));
    }

    public boolean isHoliday(String date) {
        AntiAddictionLogger.d("isHoliday:" + holidaySet.contains(date));
        return holidaySet.contains(date);
    }

    public boolean needUploadAllData() {
        if (commonConfig != null && commonConfig.childProtectedConfig != null) return commonConfig.childProtectedConfig.uploadAllData == 1;
        return false;
    }
}
