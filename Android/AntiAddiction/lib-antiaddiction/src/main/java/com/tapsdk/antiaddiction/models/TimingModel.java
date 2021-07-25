package com.tapsdk.antiaddiction.models;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import java.util.Date;

import com.tapsdk.antiaddiction.BuildConfig;
import com.tapsdk.antiaddiction.constants.Constants;
import com.tapsdk.antiaddiction.entities.ChildProtectedConfig;
import com.tapsdk.antiaddiction.entities.SubmitPlayLogResult;
import com.tapsdk.antiaddiction.entities.ThreeTuple;
import com.tapsdk.antiaddiction.entities.TwoTuple;
import com.tapsdk.antiaddiction.entities.UserInfo;
import com.tapsdk.antiaddiction.entities.request.PlayLogRequestParams;
import com.tapsdk.antiaddiction.models.internal.TransactionHandler;
import com.tapsdk.antiaddiction.reactor.functions.Action1;
import com.tapsdk.antiaddiction.reactor.rxandroid.schedulers.AndroidSchedulers;
import com.tapsdk.antiaddiction.settings.AntiAddictionSettings;
import com.tapsdk.antiaddiction.skynet.retrofit2.Response;
import com.tapsdk.antiaddiction.utils.AntiAddictionLogger;
import com.tapsdk.antiaddiction.utils.TimeUtil;

public class TimingModel {

    private final UserModel userModel;
    private final Context context;
    private final String game;
    private final Handler mainLooperHandler = new Handler(Looper.getMainLooper());

    private volatile long lastProcessTimeInSecond = -1L;
    private long recentServerTimeInSecond = -1L;

    private boolean isCountDown1 = false;
    private boolean isCountDown2 = false;

    private int countDownRemainTime = 0;
    private int remainTime = 0;

    public TimingModel(UserModel userModel, Context context, String game) {
        this.userModel = userModel;
        this.context = context;
        this.game = game;
        initLoginStatusChangedListener();
    }

    private void initLoginStatusChangedListener() {
        userModel.getUserLoginStatusChangedObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean logged) {
                        if (!logged) {
                            unbind();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (BuildConfig.DEBUG) {
                            throw new RuntimeException("userStateChangeListener unexpected error");
                        }
                    }
                });
    }

    public void bind() {
        AntiAddictionLogger.d("bind");
        unbind();
        mainLooperHandler.post(new Runnable() {
            @Override
            public void run() {
                if (userModel == null || userModel.getCurrentUser() == null) return;
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
        AntiAddictionLogger.d("unbind");
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

    public void setRecentServerTimeInSecond(long serverTimeInSeconds) {
        recentServerTimeInSecond = serverTimeInSeconds;
    }

    private HandlerThread mHandlerThread = null;
    private final CountTimeInteractiveOperation interactiveOperation = new CountTimeInteractiveOperation();
    private TransactionHandler mHandler = null;

    private Response<SubmitPlayLogResult> sendGameTimeToServerSync() throws Throwable {
        if (userModel == null || userModel.getCurrentUser() == null)
            throw new Exception("sendGameTimeToServerSync exception");
        UserInfo userInfo = userModel.getCurrentUser().clone();

        AntiAddictionLogger.d("-------sendGameTimeToServerSync-------");
        long localStartSeconds, localEndSeconds, serverStartSeconds, serverEndSeconds;

        long curTimeInSecond = SystemClock.elapsedRealtime() / 1000;
        AntiAddictionLogger.d("elapsedRealTimeInSecond:" + curTimeInSecond + " lastProcessGameTimeInSeconds:" + lastProcessTimeInSecond);
        long diff = 0;
        if (lastProcessTimeInSecond == -1L) {
            localStartSeconds = recentServerTimeInSecond;
            localEndSeconds = recentServerTimeInSecond;
            serverStartSeconds = recentServerTimeInSecond;
            serverEndSeconds = recentServerTimeInSecond;
        } else {
            diff = curTimeInSecond - lastProcessTimeInSecond;
            localStartSeconds = recentServerTimeInSecond;
            localEndSeconds = Math.round(recentServerTimeInSecond + diff);
            serverStartSeconds = recentServerTimeInSecond;
            serverEndSeconds = Math.round(recentServerTimeInSecond + diff);
        }

        PlayLogRequestParams playLogRequestParams = PlayLogModel.getPlayLog(context, userInfo
                , game, serverStartSeconds, serverEndSeconds, localStartSeconds, localEndSeconds
                , recentServerTimeInSecond);
        Response<SubmitPlayLogResult> response = PlayLogModel.uploadPlayLogSync(playLogRequestParams, false);
        if (response.code() == 200) {
            setRecentServerTimeInSecond(recentServerTimeInSecond + diff);
            AntiAddictionLogger.d("after update elapsedRealtime:" + recentServerTimeInSecond);
        }
        lastProcessTimeInSecond = curTimeInSecond;

        return response;
    }

    private SubmitPlayLogResult syncTime() throws Throwable {
        if (userModel == null || userModel.getCurrentUser() == null)
            throw new Exception("syncTime exception");

        UserInfo userInfo = userModel.getCurrentUser().clone();
        Response<SubmitPlayLogResult> response = sendGameTimeToServerSync();
        SubmitPlayLogResult result = response.body();
        if (result != null && response.code() == 200) {
            AntiAddictionSettings.getInstance().clearHistoricalData(context, userInfo.userId);
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
        AntiAddictionLogger.d("reset:" + TimeUtil.getFullTime(recentServerTimeInSecond));
        if (recentServerTimeInSecond != -1L && lastProcessTimeInSecond != -1) {
            setRecentServerTimeInSecond(recentServerTimeInSecond + (SystemClock.elapsedRealtime() - lastProcessTimeInSecond));

            AntiAddictionLogger.d("reset:" + TimeUtil.getFullTime(recentServerTimeInSecond * 1000));
        }
        lastProcessTimeInSecond = -1L;
        isCountDown1 = false;
        isCountDown2 = false;
    }

    private SubmitPlayLogResult handleLocalePlayLog(UserInfo userInfo) {

        long localStartSeconds, localEndSeconds, serverStartSeconds, serverEndSeconds;
        long curTimeInSecond = SystemClock.elapsedRealtime() / 1000;
        if (lastProcessTimeInSecond == -1L) {
            localStartSeconds = recentServerTimeInSecond;
            localEndSeconds = recentServerTimeInSecond;
            serverStartSeconds = recentServerTimeInSecond;
            serverEndSeconds = recentServerTimeInSecond;
        } else {
            long diffInSecond = curTimeInSecond - lastProcessTimeInSecond;
            localStartSeconds = recentServerTimeInSecond;
            localEndSeconds = Math.round(recentServerTimeInSecond + diffInSecond);
            serverStartSeconds = recentServerTimeInSecond;
            serverEndSeconds = Math.round(recentServerTimeInSecond + diffInSecond);
            setRecentServerTimeInSecond(recentServerTimeInSecond + diffInSecond);
        }

        lastProcessTimeInSecond = curTimeInSecond;
        print(localStartSeconds
                , localEndSeconds
                , serverStartSeconds
                , serverEndSeconds);
        saveLostTimestamp(userInfo, serverStartSeconds, serverEndSeconds, localStartSeconds, localEndSeconds);
        if (userModel != null)
            userModel.getCurrentUser().updateRemainTime((int) (localEndSeconds - localStartSeconds));
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
        AntiAddictionLogger.d("generateLocalPlayLogResult [serverTime]:" + TimeUtil.getFullTime(recentServerTimeInSecond * 1000));
        ChildProtectedConfig config = AntiAddictionSettings.getInstance().getCommonConfig().childProtectedConfig;
        int toNightTime = TimeUtil.getTimeToNightStrict(config.nightStrictStart, config.nightStrictEnd, recentServerTimeInSecond * 1000);
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
            TwoTuple<String, String> tipInfo = AntiAddictionSettings.getInstance().getPromptInfo(userInfo.accountType, type);
            result.title = tipInfo.firstParam;
            result.description = tipInfo.secondParam.replace("${remaining}", String.valueOf(costTime / 60));
        }
        return result;
    }


    private void saveLostTimestamp(UserInfo userInfo, long start, long end, long localStart, long localEnd) {
        AntiAddictionSettings.getInstance().saveLatestData(context, userInfo.userId
                , start, end, localStart, localEnd);
    }

    private void setTimerForPrompt(SubmitPlayLogResult result) {
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

    class CountTimeInteractiveOperation implements TransactionHandler.InteractiveOperation {

        @Override
        public void countTime() {
            if (userModel == null) return;

            if (recentServerTimeInSecond == -1L) {
                recentServerTimeInSecond = TimeModel.getServerTimeSync();
            }

            try {
                SubmitPlayLogResult result = syncTime();
                if (result.restrictType > 0) {
                    setTimerForPrompt(result);
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
        public void childTimeRunOut(int strictType) {

        }

        @Override
        public void logout() {

        }
    }
}
