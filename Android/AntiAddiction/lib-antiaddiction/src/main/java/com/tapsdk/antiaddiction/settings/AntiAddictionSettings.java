package com.tapsdk.antiaddiction.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tapsdk.antiaddiction.constants.Constants;
import com.tapsdk.antiaddiction.entities.CommonConfig;
import com.tapsdk.antiaddiction.entities.HealthPromptGroup;
import com.tapsdk.antiaddiction.entities.Prompt;
import com.tapsdk.antiaddiction.entities.TwoTuple;
import com.tapsdk.antiaddiction.enums.AccountLimitTipEnum;
import com.tapsdk.antiaddiction.skynet.okio.ByteString;
import com.tapsdk.antiaddiction.utils.AntiAddictionLogger;
import com.tapsdk.antiaddiction.utils.FileUtil;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AntiAddictionSettings {

    private final Map<String, SharedPreferences> spCache = new HashMap<>();

    private CommonConfig commonConfig = null;
    // tuple first param is title and second param is description
    private final Map<Integer, Map<Integer, TwoTuple<String, String>>> promptDict = new HashMap<>();
    private Set<String> holidaySet = new HashSet<>();
    private Gson gson = new GsonBuilder().create();

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
        return sb.append(Constants.CacheData.TIMING_FILE_SUFFIX).toString();
    }

    public SharedPreferences getSpecificSharedPreference(Context context, String spName) {
        if (spCache.get(spName) != null) {
            return spCache.get(spName);
        }
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        spCache.put(spName, sp);
        return sp;
    }

    public String getHistoricalData(Context context, String userId) {
        return getSpecificSharedPreference(context, getSPNameByToken(userId)).getString(Constants.CacheData.TIMING_SHARED_PREFERENCE_NAME, "");
    }

    public void saveLatestData(Context context, String userId, long start, long end, long localStart, long localEnd) {
        SharedPreferences.Editor editor = getSpecificSharedPreference(context, getSPNameByToken(userId)).edit();
        String historicalData = AntiAddictionSettings.getInstance().getHistoricalData(context, userId);
        String latestData = historicalData +
                start + "," + end + "," +
                localStart + "," + localEnd + ";";
        editor.putString(Constants.CacheData.TIMING_SHARED_PREFERENCE_NAME, latestData);
        editor.apply();
    }

    public void clearHistoricalData(Context context, String userId) {
        SharedPreferences.Editor editor = getSpecificSharedPreference(context, getSPNameByToken(userId)).edit();
        editor.putString(Constants.CacheData.TIMING_SHARED_PREFERENCE_NAME, "");
        editor.apply();
    }

    private void initTipDict(List<HealthPromptGroup> healthReminderWordsList) {
        promptDict.clear();
        for (int i = 0; i < healthReminderWordsList.size(); i++) {
            HealthPromptGroup promptGroup = healthReminderWordsList.get(i);
            int accountType = promptGroup.accountType;
            Map<Integer, TwoTuple<String, String>> promptInfoMap = new HashMap<>();
            List<Prompt> promptList = promptGroup.promptList;
            for (int j = 0; j < promptList.size() ; j ++) {
                Prompt prompt = promptList.get(j);
                promptInfoMap.put(prompt.type, TwoTuple.create(prompt.title, prompt.description));
            }
            promptDict.put(accountType, promptInfoMap);
        }
    }

    public void setCommonConfig(CommonConfig commonConfig) {
        if (commonConfig == null
                || commonConfig.uiConfig == null
                || commonConfig.holidayList == null) {
            String illegalArgumentName;
            if (commonConfig == null) illegalArgumentName = "CommonConfig";
            else if (commonConfig.uiConfig == null) illegalArgumentName = "UIConfig";
            else illegalArgumentName = "HolidayList";
            AntiAddictionLogger.w("illegal arguments:" + illegalArgumentName);
            return;
        }
        initTipDict(commonConfig.uiConfig.healthPromptGroups);
        initHolidaySet(commonConfig.holidayList);
        this.commonConfig = commonConfig;
    }

    public CommonConfig getCommonConfig() {
        return commonConfig;
    }

    public CommonConfig getCommonDefaultConfig(Context context) {
        CommonConfig defaultConfig = null;
        try {
            defaultConfig = new GsonBuilder().create().fromJson(
                    FileUtil.getJsonFromAssetsFile(context, "default_config.json"), CommonConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultConfig;
    }

    public TwoTuple<String, String> getPromptInfo(int accountType, int type) {
        if (accountType != Constants.UserType.USER_TYPE_UNREALNAME && accountType != Constants.UserType.USER_TYPE_UNKNOWN) {
            accountType = Constants.UserType.USER_TYPE_CHILD;
        }
        TwoTuple<String, String> promptInfo = null;
        if (promptDict.containsKey(accountType) ) {
            Map<Integer, TwoTuple<String, String>> promptInfoMap = promptDict.get(accountType);
            if (promptInfoMap != null) {
                promptInfo = promptInfoMap.get(type);
            }
        }
        if (promptInfo != null) return promptInfo;
        return TwoTuple.create("", "");
    }

    public void initHolidaySet(List<String> holidays) {
        holidaySet = new HashSet<>(holidays);
    }

    public boolean isHolidayInMillis(long timeMillis) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM.dd", Locale.getDefault());
        return isHoliday(formatter.format(timeMillis));
    }

    public boolean isHoliday(String date) {
        return holidaySet.contains(date);
    }

    public boolean needUploadAllData() {
        if (commonConfig != null && commonConfig.childProtectedConfig != null) return commonConfig.childProtectedConfig.uploadAllData == 1;
        return false;
    }

    public Map<String, Object> generateAlertMessage(String content, String description, AccountLimitTipEnum limitTipEnum, int strictType) {
        AntiAddictionLogger.d("-------generateAlertMessage-------");
        Map<String, Object> result = new HashMap<>();
        result.put("title", content);
        result.put("description", description);
        result.put("limit_tip_type", limitTipEnum);
        result.put("strict_type", strictType);
        AntiAddictionLogger.d("generateAlertMessage:" + gson.toJson(result));
        return result;
    }
}
