package com.tapsdk.antiaddiction.models;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import com.tapsdk.antiaddiction.constants.Constants;
import com.tapsdk.antiaddiction.entities.ChildProtectedConfig;
import com.tapsdk.antiaddiction.entities.SubmitPlayLogResult;
import com.tapsdk.antiaddiction.entities.ThreeTuple;
import com.tapsdk.antiaddiction.entities.TwoTuple;
import com.tapsdk.antiaddiction.entities.UserInfo;
import com.tapsdk.antiaddiction.entities.request.PlayLogRequestParams;
import com.tapsdk.antiaddiction.models.internal.TransactionHandler;
import com.tapsdk.antiaddiction.settings.AntiAddictionSettings;
import com.tapsdk.antiaddiction.skynet.okhttp3.internal.http.RealResponseBody;
import com.tapsdk.antiaddiction.skynet.retrofit2.Response;
import com.tapsdk.antiaddiction.utils.AntiAddictionLogger;
import com.tapsdk.antiaddiction.utils.TimeUtil;
import java.util.Date;

public class CountTimeModel {

    private final UserModel userModel;
    private final Context context;
    private final String game;
    private final Handler mainLooperHandler = new Handler(Looper.getMainLooper());

    private CountTimeModel() {
        userModel = null;
        context = null;
        game = null;
    }

    public CountTimeModel(UserModel userModel, Context context, String game) {
        this.userModel = userModel;
        this.context = context;
        this.game = game;
    }

    private volatile long lastProcessTime = -1L;
    private long recentServerTime = -1L;

    private boolean isCountDown1 = false;
    private boolean isCountDown2 = false;

    private int countDownRemainTime = 0;
    private int remainTime = 0;

    public void bind() {
        mainLooperHandler.post(new Runnable() {
            @Override
            public void run() {
                unbind();
                if (userModel == null || userModel.getCurrentUser() == null) return;
                if (!AntiAddictionSettings.getInstance().needUploadAllData()
                        && userModel.getCurrentUser().accountType == Constants.UserType.USER_TYPE_ADULT) return;

                mHandlerThread = new HandlerThread("AntiAddictionMonitor", Process.THREAD_PRIORITY_BACKGROUND);
                mHandlerThread.start();
                mHandler = new TransactionHandler(mHandlerThread.getLooper(), interactiveOperation);
                Message msg = mHandler.obtainMessage();
                msg.what = TransactionHandler.MESSAGE_COUNT_TIME;
                mHandler.sendMessage(msg);
            }
        });
    }

    public void unbind() {
        mainLooperHandler.post(new Runnable() {
            @Override
            public void run() {
                reset();
                if (mHandlerThread != null) {
                    mHandlerThread.quit();
                    mHandlerThread = null;
                }
                if (mHandler != null) {
                    mHandler.removeCallbacksAndMessages(null);
                    mHandler = null;
                }
            }
        });
    }


    public void setRecentServerTime(long serverTime) {
        recentServerTime = serverTime;
    }

    private HandlerThread mHandlerThread = null;
    private CountTimeInteractiveOperation interactiveOperation = new CountTimeInteractiveOperation();
    private TransactionHandler mHandler = null;

    private Response<SubmitPlayLogResult> sendGameTimeToServerSync() throws Throwable {
        if (userModel == null || userModel.getCurrentUser() == null) throw new Exception("sendGameTimeToServerSync exception");
        UserInfo userInfo = userModel.getCurrentUser().clone();

        AntiAddictionLogger.d("-------sendGameTimeToServerSync-------");
        long localStartSeconds, localEndSeconds, serverStartSeconds, serverEndSeconds;
        AntiAddictionLogger.d("elapsedRealtime:" + SystemClock.elapsedRealtime() + " lastProcessGameTimeStamp:" + lastProcessTime);
        long cur = SystemClock.elapsedRealtime();
        long diff = 0;
        if (lastProcessTime == -1L) {
            localStartSeconds = recentServerTime;
            localEndSeconds = recentServerTime;
            serverStartSeconds = recentServerTime;
            serverEndSeconds = recentServerTime;
        } else {
            diff = cur - lastProcessTime;
            localStartSeconds = recentServerTime ;
            localEndSeconds = Math.round(recentServerTime + diff * 1.0 / 1000);
            serverStartSeconds = recentServerTime / 1000;
            serverEndSeconds = Math.round(recentServerTime + diff * 1.0/ 1000);
        }

        PlayLogRequestParams playLogRequestParams = PlayLogModel.getPlayLog(context, userInfo
                , game, serverStartSeconds,serverEndSeconds, localStartSeconds, localEndSeconds
                , recentServerTime);
//        Response<SubmitPlayLogResult> response = PlayLogModel.uploadPlayLogSync(playLogRequestParams, false);
//        if (response.code() == 200) {
//            setRecentServerTime(recentServerTime + diff);
//            lastProcessTime = cur;
//        }
        Response<SubmitPlayLogResult> response = Response.error(400, new RealResponseBody("", 0, null));

        return response;
    }

    private SubmitPlayLogResult syncTime() throws Throwable {
        if (userModel == null || userModel.getCurrentUser() == null) throw new Exception("syncTime exception");

        Response<SubmitPlayLogResult> response = sendGameTimeToServerSync();
        UserInfo userInfo = userModel.getCurrentUser().clone();
        SubmitPlayLogResult result = response.body();
        if (result != null && response.code() == 200) {
            AntiAddictionSettings.getInstance().clearCountTime(context, userInfo.userId);
            if (mHandler != null) userInfo.resetRemainTime(result.remainTime);
            AntiAddictionLogger.d("local left time:" + result.remainTime);
        } else {
            // 使用本地时间计算
            result = handleLocalePlayLog(userInfo);
        }
//        if (AntiAddictionKit.isDebug()) {
//
//        }
        remainTime = result.remainTime;
        return result;
    }

    private void reset() {
        AntiAddictionLogger.d("reset:" + TimeUtil.getFullTime(recentServerTime));
        if (recentServerTime != -1L && lastProcessTime != -1) {
            setRecentServerTime(recentServerTime + (SystemClock.elapsedRealtime() - lastProcessTime));

            AntiAddictionLogger.d("reset:" + TimeUtil.getFullTime(recentServerTime * 1000));
        }
        lastProcessTime = -1L;
        isCountDown1 = false;
        isCountDown2 = false;
    }

    private SubmitPlayLogResult handleLocalePlayLog(UserInfo userInfo) throws Exception {

        long localStartSeconds, localEndSeconds, serverStartSeconds, serverEndSeconds;
        long cur = SystemClock.elapsedRealtime();
        if (lastProcessTime == -1L) {
            AntiAddictionLogger.d("handleLocalePlayLog first time");
            localStartSeconds = recentServerTime;
            localEndSeconds = recentServerTime;
            serverStartSeconds = recentServerTime;
            serverEndSeconds = recentServerTime;
        } else {
            AntiAddictionLogger.d("handlePlayLogLocale from ");
            long diff = cur - lastProcessTime;
            localStartSeconds = recentServerTime;
            localEndSeconds = Math.round(recentServerTime + diff * 1.0 / 1000);
            serverStartSeconds = recentServerTime;
            serverEndSeconds = Math.round(recentServerTime + diff * 1.0 / 1000);
            setRecentServerTime(recentServerTime + diff);
        }
        lastProcessTime = cur;
        print(localStartSeconds
                , localEndSeconds
                , serverStartSeconds
                , serverEndSeconds);
        saveLostTimestamp(userInfo, serverStartSeconds, serverEndSeconds, localStartSeconds, localEndSeconds);
        if (userModel != null) userModel.getCurrentUser().updateRemainTime((int) (localEndSeconds - localStartSeconds));
        return generateLocalPlayLogResult(userInfo);
    }

    private static void print(long serverStartSeconds, long serverEndSeconds, long localStartSeconds, long localEndSeconds) {
        AntiAddictionLogger.d("print local process result");
        AntiAddictionLogger.d("server start time:" + TimeUtil.getFullTime(serverStartSeconds * 1000) + ","
                + "server end time:" + TimeUtil.getFullTime(serverEndSeconds * 1000) + ","
                + "local start time:" + TimeUtil.getFullTime(localStartSeconds * 1000) + ","
                + "local end time:" + TimeUtil.getFullTime(localEndSeconds * 1000)
        );
    }

    private SubmitPlayLogResult generateLocalPlayLogResult(UserInfo userInfo) {

        int restrictType = 0; //1 宵禁 2 在线时长限制
        int remainTime = 0;
        SubmitPlayLogResult result = new SubmitPlayLogResult();
        // 成年人不需要防沉迷
        if (userInfo.accountType == Constants.UserType.USER_TYPE_ADULT) {
            result.restrictType = 0;
            return result;
        }
        AntiAddictionLogger.d("generateLocalPlayLogResult [serverTime]:" + TimeUtil.getFullTime(recentServerTime * 1000));
        ChildProtectedConfig config = AntiAddictionSettings.getInstance().getCommonConfig().childProtectedConfig;
        int toNightTime = TimeUtil.getTimeToNightStrict(config.nightStrictStart, config.nightStrictEnd, recentServerTime * 1000);
        int toLimitTime = userInfo.remainTime;
        restrictType = toNightTime > toLimitTime ? 2 : 1;
        remainTime = Math.min(Math.max(toLimitTime, 0), Math.max(toNightTime, 0));
        AntiAddictionLogger.d("toNightTime:" + toNightTime + " toLimitTime:" + toLimitTime);

        if (userModel != null) userModel.getCurrentUser().resetRemainTime(remainTime);

        result.restrictType = restrictType;
        result.remainTime = remainTime;
        if (remainTime <= 0) {
            int type;
            if (userInfo.accountType == Constants.UserType.USER_TYPE_UNKNOWN
                    || userInfo.accountType == Constants.UserType.USER_TYPE_UNREALNAME) {
                type = 6;
            } else {
                if (restrictType == 1) {
                    type = 5;
                } else {
                    type = 6;
                }
            }
            int costTime = 0;
            if (userInfo.accountType == 5) {
                costTime = config.noIdentifyTime;
            } else if (TimeUtil.isHoliday(new Date().getTime())) {
                costTime = config.childHolidayTime;
            } else {
                costTime = config.childCommonTime;
            }
            TwoTuple<String, String> tipInfo = AntiAddictionSettings.getInstance().getAntiAddictionFeedBack(userInfo.accountType, type);
            result.title = tipInfo.firstParam;
            result.description = tipInfo.secondParam.replace("${remaining}", String.valueOf(costTime / 60));
        }
        return result;
    }


    private void saveLostTimestamp(UserInfo userInfo, long start, long end, long localStart, long localEnd) {
        AntiAddictionSettings.getInstance().saveLatestData(context, userInfo.userId
                , start, end, localStart, localEnd);
    }

    private void setTimerForTip(SubmitPlayLogResult result) {
        int seconds = result.remainTime;
        if ((seconds >= 15 * 60 && seconds <= 17 * 60)) {
            countDownRemainTime = seconds;
            if (!isCountDown1) {
                Message msg = mHandler.obtainMessage();
                msg.what = TransactionHandler.MESSAGE_COUNT_DOWN;
                msg.obj = ThreeTuple.create(result.title, result.description, result.restrictType);
                mHandler.sendMessage(msg);
                isCountDown1 = true;
            }
        } else if (seconds >= 0 && seconds <= 3 * 60) {
            countDownRemainTime = seconds;
            if (!isCountDown2) {
                if (mHandler != null) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = TransactionHandler.MESSAGE_COUNT_DOWN;
                    msg.obj = ThreeTuple.create(result.title, result.description, result.restrictType);
                    mHandler.sendMessage(msg);
                    isCountDown2 = true;
                }
            }
        }
    }

    class CountTimeInteractiveOperation implements TransactionHandler.InteractiveOperation{

        @Override
        public void countTime() {
            if (userModel == null) return;
            long serverTime = TimeModel.getServerTimeSync();
            if (serverTime != -1L) {
                setRecentServerTime(serverTime);
            }
            try {
                SubmitPlayLogResult result = syncTime();
                if (result.restrictType > 0) {
                    setTimerForTip(result);
                }
            } catch (Throwable e) {
                AntiAddictionLogger.printStackTrace(e);
            }
        }

        @Override
        public boolean countDown(String title, String description, int restrictType) {
            countDownRemainTime--;
            // 在15分钟左右 或者 小于 60 秒
            if (countDownRemainTime == 15 * 60 || countDownRemainTime <= 60) {
                int seconds = countDownRemainTime;

                if (countDownRemainTime > 60) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = TransactionHandler.MESSAGE_SEND_TIME;
                    mHandler.sendMessage(msg);
                } else {
                    int updateTime = countDownRemainTime;
                    int accum = 1;
                    while (updateTime > 10) {
                        Message msg = mHandler.obtainMessage();
                        msg.what = TransactionHandler.MESSAGE_SEND_TIME;
                        mHandler.sendMessageDelayed(msg, accum * 10 * 1000);
                        updateTime -= 10;
                        accum++;
                    }
                }

                if (countDownRemainTime <= 60) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = TransactionHandler.MESSAGE_CHILD_TIME_RUN_OUT;
                    msg.obj = restrictType;
                    AntiAddictionLogger.d("remain time:" + remainTime);
                    AntiAddictionLogger.d("count down time:" + countDownRemainTime);
                    // 延迟1秒发送
                    mHandler.sendMessageDelayed(msg, countDownRemainTime * 1000 + 800);
                }

                if (title == null) title = "";
                if (description == null) description = "";
                // todo according to title && description && seconds && restrictType to make a prompt
                return true;
            }
            return false;
        }

        @Override
        public void updateServerTime() {

        }

        @Override
        public void stopCountDownTimerAndUpdateServerTime() {

        }

        @Override
        public void childTimeRunout(int strictType) {

        }

        @Override
        public void logout() {

        }
    }
}
