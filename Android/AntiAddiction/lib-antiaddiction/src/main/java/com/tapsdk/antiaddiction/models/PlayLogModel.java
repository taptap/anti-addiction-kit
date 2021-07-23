package com.tapsdk.antiaddiction.models;

import android.content.Context;

import com.google.gson.JsonSyntaxException;
import com.tapsdk.antiaddiction.entities.SubmitPlayLogResult;
import com.tapsdk.antiaddiction.entities.UserInfo;
import com.tapsdk.antiaddiction.entities.request.PlayLogRequestParams;
import com.tapsdk.antiaddiction.rest.api.AntiAddictionApi;
import com.tapsdk.antiaddiction.settings.AntiAddictionSettings;
import com.tapsdk.antiaddiction.skynet.Skynet;
import com.tapsdk.antiaddiction.skynet.okhttp3.internal.http.RealResponseBody;
import com.tapsdk.antiaddiction.skynet.retrofit2.Response;
import com.tapsdk.antiaddiction.utils.TimeUtil;
import java.io.IOException;
import java.util.Arrays;

public class PlayLogModel {

    public static PlayLogRequestParams getPlayLog(
            Context context
            , UserInfo userInfo
            , String game
            , long serverStartTime
            , long serverEndTime
            , long localStartTime
            , long localEndTime
            , long serverTime
    ) {
        final long[][] sendTimes;
        int timesLength;
        long[][] savedTimes = getUnSentGameTimes(context, userInfo);

        if (checkSavedTimeStamp(savedTimes, serverTime)) {
            sendTimes = new long[savedTimes.length + 1][];
            timesLength = savedTimes.length + 1;
            System.arraycopy(savedTimes, 0, sendTimes, 0, savedTimes.length);
        } else {
            timesLength = 1;
            sendTimes = new long[1][];
        }

        sendTimes[timesLength - 1] = new long[]{serverStartTime, serverEndTime, localStartTime, localEndTime};
        PlayLogRequestParams result = new PlayLogRequestParams();
        result.game = game;
        for (long[] time : sendTimes) {
            result.playLogs.serverTimes.add(Arrays.asList(time[0], time[1]));
            result.playLogs.localTimes.add(Arrays.asList(time[2], time[3]));
        }

        return result;
    }

    private static long[][] getUnSentGameTimes(Context context, UserInfo userInfo) {
        if (userInfo == null) return new long[1][];
        String savedTimes = AntiAddictionSettings.getInstance().getHistoricalData(context, userInfo.userId);
        if (savedTimes.length() == 0) return null;
        long[][] timeArray;
        String[] segments = savedTimes.split(";");
        if (segments.length == 0) return null;
        timeArray = new long[segments.length][];
        for (int i = 0; i < segments.length; i++) {
            String[] times = segments[i].split(",");
            timeArray[i] = new long[4];
            for (int j = 0; j < times.length; j++) {
                timeArray[i][j] = Long.parseLong(times[j]);
            }
        }
        return timeArray;
    }

    private static boolean checkSavedTimeStamp(long[][] saved, long serverTime) {
        if (null != saved && saved.length > 0 && saved[0] != null) {
            //保存的第一个时间戳的起始时间
            long firstStart = saved[0][0];
            return TimeUtil.isSameDayOfMillis(firstStart * 1000, serverTime);
        }
        return false;
    }

    /**
     * 检查当前用户状态（用本地时间做检查，返回的是什么？）
     * 网络同步请求不能放在主线程中执行
     *
     * @return Response<SubmitPlayLogResult> 上传结果
     */
    public static Response<SubmitPlayLogResult> checkUserStateSync(PlayLogRequestParams playLogRequestParams) {
        return uploadPlayLogSync(playLogRequestParams, true);
    }

    /**
     * 上传游戏日志
     *
     * @param playLogs 游戏日志
     * @param isLogin 是否登录
     *
     * @return Response<SubmitPlayLogResult> 上传结果
     */
    public static Response<SubmitPlayLogResult> uploadPlayLogSync(PlayLogRequestParams playLogs, boolean isLogin) {
        try {
            AntiAddictionApi api = Skynet.getService(Skynet.RETROFIT_FOR_ANTI_ADDICTION, AntiAddictionApi.class);
            playLogs.login = isLogin ? 1 : 0;
            Response<SubmitPlayLogResult> response  = api.uploadPlayLogSync(playLogs).execute();
            return response;
        } catch (IOException | NullPointerException | JsonSyntaxException e) {
            e.printStackTrace();
        }
        return Response.error(400, new RealResponseBody("", 0, null));
    }
}
