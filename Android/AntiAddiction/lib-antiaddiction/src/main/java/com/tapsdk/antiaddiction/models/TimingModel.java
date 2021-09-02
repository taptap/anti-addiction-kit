package com.tapsdk.antiaddiction.models;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;

import com.tapsdk.antiaddiction.BuildConfig;
import com.tapsdk.antiaddiction.constants.Constants;
import com.tapsdk.antiaddiction.entities.ChildProtectedConfig;
import com.tapsdk.antiaddiction.entities.SubmitPlayLogResult;
import com.tapsdk.antiaddiction.entities.ThreeTuple;
import com.tapsdk.antiaddiction.entities.TwoTuple;
import com.tapsdk.antiaddiction.entities.UserInfo;
import com.tapsdk.antiaddiction.entities.request.PlayLogRequestParams;
import com.tapsdk.antiaddiction.enums.AccountLimitTipEnum;
import com.tapsdk.antiaddiction.models.internal.TimingHandler;
import com.tapsdk.antiaddiction.reactor.RxBus;
import com.tapsdk.antiaddiction.reactor.functions.Action1;
import com.tapsdk.antiaddiction.reactor.rxandroid.schedulers.AndroidSchedulers;
import com.tapsdk.antiaddiction.settings.AntiAddictionSettings;
import com.tapsdk.antiaddiction.skynet.retrofit2.Response;
import com.tapsdk.antiaddiction.utils.AntiAddictionLogger;
import com.tapsdk.antiaddiction.utils.TimeUtil;

import java.util.Date;
import java.util.Map;

public class TimingModel {

    private final UserModel userModel;
    private final Context context;
    private final String game;
    private final Handler mainLooperHandler = new Handler(Looper.getMainLooper());

    private long lastProcessTimeInSecond = -1L;
    private long recentServerTimeInSecond = -1L;
    private boolean isCountDown1 = false;
    private boolean isCountDown2 = false;
    private int countDownRemainTime = 0;
    private int remainTime = 0;
    public boolean inTiming = false;

    public interface TimingMessageListener {
        void onMessage(int type, Map<String, Object> extras);
    }

    private final TimingMessageListener timingMessageListener;

    public TimingModel(UserModel userModel, Context context, String game, TimingMessageListener timingMessageListener) {
        this.userModel = userModel;
        this.context = context;
        this.game = game;
        this.timingMessageListener = timingMessageListener;
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
                mHandler = new TimingHandler(mHandlerThread.getLooper(), interactiveOperation);
                Message msg = mHandler.obtainMessage();
                msg.what = TimingHandler.MESSAGE_COUNT_TIME;
                mHandler.sendMessage(msg);
                inTiming = true;
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
                inTiming = false;
                if (BuildConfig.DEBUG) {
                    RxBus.getInstance().send(new UpdateAntiAddictionInfoAction(recentServerTimeInSecond, remainTime, false));
                }
            }
        });
    }

    public void setRecentServerTimeInSecond(long serverTimeInSeconds) {
        recentServerTimeInSecond = serverTimeInSeconds;
    }

    private HandlerThread mHandlerThread = null;

    private TimingHandler mHandler = null;

    private final TimingHandler.InteractiveOperation interactiveOperation = new TimingHandler.InteractiveOperation() {
        @Override
        public void countTime() {
            if (userModel == null) return;

            if (recentServerTimeInSecond == -1L) {
                recentServerTimeInSecond = TimeModel.getServerTimeSync();
            }

            try {
                SubmitPlayLogResult result = syncTime();
                if (result.restrictType != StrictType.NONE) {
                    setTimerForPrompt(result);
                }
            } catch (Throwable e) {
                AntiAddictionLogger.printStackTrace(e);
            }
        }

        @Override
        public boolean countDown(String title, String description, int restrictType) {
            int localCountDownRemainTime = countDownRemainTime--;
            // 在15分钟左右 或者 小于 60 秒
            if (localCountDownRemainTime == 15 * 60 || localCountDownRemainTime <= 60) {
                if (localCountDownRemainTime > 60) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = TimingHandler.MESSAGE_SEND_TIME;
                    mHandler.sendMessage(msg);
                } else {
                    int updateTime = localCountDownRemainTime;
                    int accum = 1;
                    while (updateTime > 10) {
                        Message msg = mHandler.obtainMessage();
                        msg.what = TimingHandler.MESSAGE_SEND_TIME;
                        mHandler.sendMessageDelayed(msg, accum * 10 * 1000);
                        updateTime -= 10;
                        accum++;
                    }
                }
                UserInfo userInfo = userModel.getCurrentUser().clone();
                if (localCountDownRemainTime <= 60) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = TimingHandler.MESSAGE_CHILD_TIME_RUN_OUT;
                    msg.obj = restrictType;
                    AntiAddictionLogger.d("remain time:" + remainTime);
                    AntiAddictionLogger.d("count down time:" + localCountDownRemainTime);
                    // 延迟1秒发送
                    mHandler.sendMessageDelayed(msg, localCountDownRemainTime * 1000 + 800);
                    title = AntiAddictionSettings.getInstance().getPromptInfo(userInfo.accountType
                            , (restrictType == StrictType.TIME_LIMIT) ? PromptType.TIME_LIMIT_BUBBLE : PromptType.NIGHT_STRICT_BUBBLE).secondParam
                            .replace("分钟", "秒");
                } else {
                    title = AntiAddictionSettings.getInstance().getPromptInfo(userInfo.accountType
                            , (restrictType == StrictType.TIME_LIMIT) ? PromptType.TIME_LIMIT_BUBBLE : PromptType.NIGHT_STRICT_BUBBLE).secondParam;
                }
                description = "";
                AntiAddictionLogger.d("count down popup:" + title);
                final String targetTitle = title;
                final String targetDescription = description;
                mainLooperHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        String remainTimeString = "";
                        if (localCountDownRemainTime <= 60) {
                            remainTimeString = String.valueOf(localCountDownRemainTime);
                        } else {
                            remainTimeString = String.valueOf(TimeUtil.getMinute(localCountDownRemainTime));
                        }
                        if (timingMessageListener != null) timingMessageListener.onMessage(
                                Constants.ANTI_ADDICTION_CALLBACK_CODE.OPEN_ALERT_TIP
                                , AntiAddictionSettings.getInstance().generateAlertMessage(targetTitle
                                        , targetDescription, AccountLimitTipEnum.STATE_COUNT_DOWN_POPUP, restrictType, remainTimeString)
                        );
                    }
                });
                return true;
            }
            return false;
        }

        @Override
        public void updateServerTime() {
            try {
                syncTime();
            } catch (Throwable throwable) {
                AntiAddictionLogger.printStackTrace(throwable);
            }
        }

        @Override
        public void stopCountDownTimerAndUpdateServerTime() {
            try {
                syncTime();
            } catch (Throwable throwable) {
                AntiAddictionLogger.printStackTrace(throwable);
            }
            unbind();
        }

        @Override
        public void childTimeRunOut(final int strictType) throws Throwable {
            SubmitPlayLogResult result = syncTime();
            if (result.remainTime != 0)
                AntiAddictionLogger.w("childTimeRunOut wrong ?:" + result.remainTime);
            UserInfo userInfo = userModel.getCurrentUser().clone();

            TwoTuple<String, String> tuple;
            if (strictType == StrictType.NIGHT) {
                // 只有类型1有宵禁信息 -> 线上版只有1有宵禁信息
                tuple = AntiAddictionSettings.getInstance().getPromptInfo(userInfo.accountType
                        , PromptType.IN_NIGHT_STRICT);
                if (TextUtils.isEmpty(tuple.firstParam)) {
                    tuple = AntiAddictionSettings.getInstance().getPromptInfo(Constants.UserType.USER_TYPE_CHILD
                            , PromptType.IN_NIGHT_STRICT);
                }
            } else {
                tuple = AntiAddictionSettings.getInstance().getPromptInfo(userInfo.accountType
                        , PromptType.TIME_EXHAUSTED);
            }
            int costTime = TimeUtil.getAntiAddictionTime(userInfo.accountType
                    , AntiAddictionSettings.getInstance().getCommonConfig().childProtectedConfig
                    , recentServerTimeInSecond * 1000);
            String title = tuple.firstParam;
            String description = tuple.secondParam.replace("${remaining}", String.valueOf(costTime / 60));
            AccountLimitTipEnum limitTipEnum;
            if (userInfo.accountType == Constants.UserType.USER_TYPE_UNREALNAME || userInfo.accountType == Constants.UserType.USER_TYPE_UNKNOWN) {
                limitTipEnum = AccountLimitTipEnum.STATE_QUIT_TIP;
            } else {
                limitTipEnum = AccountLimitTipEnum.STATE_CHILD_QUIT_TIP;
            }
            mainLooperHandler.post(new Runnable() {
                @Override
                public void run() {
                    int msgType = strictType == StrictType.TIME_LIMIT
                            ? Constants.ANTI_ADDICTION_CALLBACK_CODE.TIME_LIMIT
                            : Constants.ANTI_ADDICTION_CALLBACK_CODE.NIGHT_STRICT;
                    timingMessageListener.onMessage(msgType, AntiAddictionSettings.getInstance().generateAlertMessage(title
                            , description, AccountLimitTipEnum.STATE_COUNT_DOWN_POPUP, strictType));
                    timingMessageListener.onMessage(
                            Constants.ANTI_ADDICTION_CALLBACK_CODE.OPEN_ALERT_TIP
                            , AntiAddictionSettings.getInstance().generateAlertMessage(title
                                    , description, AccountLimitTipEnum.STATE_COUNT_DOWN_POPUP, strictType)
                    );
                }
            });
            unbind();
        }

        @Override
        public void logout() {
            try {
                syncTime();
            } catch (Throwable throwable) {
                AntiAddictionLogger.printStackTrace(throwable);
            }
            unbind();
            userModel.logout();
        }
    };

    private Response<SubmitPlayLogResult> sendGameTimeToServerSync() throws Throwable {
        if (userModel == null || userModel.getCurrentUser() == null)
            throw new Exception("sendGameTimeToServerSync exception");
        UserInfo userInfo = userModel.getCurrentUser().clone();

        AntiAddictionLogger.d("-------sendGameTimeToServerSync-------");
        long localStartSeconds, localEndSeconds, serverStartSeconds, serverEndSeconds;

        long curTimeInSecond = SystemClock.elapsedRealtime() / 1000;
        AntiAddictionLogger.d("elapsedRealTimeInSecond:" + curTimeInSecond + " lastProcessGameTimeInSeconds:" + lastProcessTimeInSecond);
        long diffInSeconds = 0;
        if (lastProcessTimeInSecond == -1L) {
            localStartSeconds = recentServerTimeInSecond;
            localEndSeconds = recentServerTimeInSecond;
            serverStartSeconds = recentServerTimeInSecond;
            serverEndSeconds = recentServerTimeInSecond;
        } else {
            diffInSeconds = curTimeInSecond - lastProcessTimeInSecond;
            localStartSeconds = recentServerTimeInSecond;
            localEndSeconds = recentServerTimeInSecond + diffInSeconds;
            serverStartSeconds = recentServerTimeInSecond;
            serverEndSeconds = recentServerTimeInSecond + diffInSeconds;
        }

        PlayLogRequestParams playLogRequestParams = PlayLogModel.getPlayLog(context, userInfo
                , game, serverStartSeconds, serverEndSeconds, localStartSeconds, localEndSeconds
                , recentServerTimeInSecond);
        Response<SubmitPlayLogResult> response = PlayLogModel.uploadPlayLogSync(playLogRequestParams, false);

        if (response.code() == 200) {
            lastProcessTimeInSecond = curTimeInSecond;
            setRecentServerTimeInSecond(recentServerTimeInSecond + diffInSeconds);
        }
        AntiAddictionLogger.d("after update elapsedRealtime:" + recentServerTimeInSecond);
        AntiAddictionLogger.d("after update serverTime:" + TimeUtil.getFullTime(recentServerTimeInSecond * 1000));

        return response;
    }

    private SubmitPlayLogResult syncTime() throws Throwable {
        if (userModel == null || userModel.getCurrentUser() == null)
            throw new Exception("syncTime exception");
        UserInfo userInfo = userModel.getCurrentUser();
        Response<SubmitPlayLogResult> response = sendGameTimeToServerSync();
        SubmitPlayLogResult result = response.body();
        if (result != null && response.code() == 200) {
            AntiAddictionSettings.getInstance().clearHistoricalData(context, userInfo.userId);
        } else {
            // 使用本地时间计算
            result = handleLocalePlayLog(userInfo);
        }
        remainTime = result.remainTime;
        AntiAddictionLogger.d("local left time:" + remainTime);
        userModel.getCurrentUser().resetRemainTime(remainTime);
        if (BuildConfig.DEBUG) {
            RxBus.getInstance().send(new UpdateAntiAddictionInfoAction(recentServerTimeInSecond, remainTime, true));
        }

        return result;
    }

    private void reset() {
        if (recentServerTimeInSecond != -1L && lastProcessTimeInSecond != -1) {
            setRecentServerTimeInSecond(recentServerTimeInSecond + (SystemClock.elapsedRealtime() / 1000 - lastProcessTimeInSecond));

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
            AntiAddictionLogger.d("diffInSecond:" + diffInSecond);
            localStartSeconds = recentServerTimeInSecond;
            localEndSeconds = recentServerTimeInSecond + diffInSecond;
            serverStartSeconds = recentServerTimeInSecond;
            serverEndSeconds = recentServerTimeInSecond + diffInSecond;
        }
        saveLostTimestamp(userInfo, serverStartSeconds, serverEndSeconds, localStartSeconds, localEndSeconds);
        lastProcessTimeInSecond = curTimeInSecond;
        setRecentServerTimeInSecond(serverEndSeconds);

        if (userModel != null) {
            userModel.getCurrentUser().updateRemainTime((int) (localEndSeconds - localStartSeconds));
        }

        return generateLocalPlayLogResult(userInfo);
    }

    private SubmitPlayLogResult generateLocalPlayLogResult(UserInfo userInfo) {

        int restrictType; //1 宵禁 2 在线时长限制
        int remainTime;
        SubmitPlayLogResult result = new SubmitPlayLogResult();
        // 成年人不需要防沉迷
        if (userInfo.accountType == Constants.UserType.USER_TYPE_ADULT) {
            result.restrictType = StrictType.NONE;
            return result;
        }
        AntiAddictionLogger.d("generateLocalPlayLogResult [serverTime]:" + TimeUtil.getFullTime(recentServerTimeInSecond * 1000));
        ChildProtectedConfig config = AntiAddictionSettings.getInstance().getCommonConfig().childProtectedConfig;
        int toNightTime = TimeUtil.getTimeToNightStrict(config.nightStrictStart, config.nightStrictEnd, recentServerTimeInSecond * 1000);
        int toLimitTime = userInfo.remainTime;
        restrictType = toNightTime > toLimitTime ? StrictType.TIME_LIMIT : StrictType.NIGHT;
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
                if (restrictType == StrictType.NIGHT) {
                    type = 5;
                } else {
                    type = 6;
                }
            }
            int costTime;
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
                msg.what = TimingHandler.MESSAGE_COUNT_DOWN;
                msg.obj = ThreeTuple.create(result.title, result.description, result.restrictType);
                mHandler.sendMessage(msg);
                isCountDown1 = true;
            }
        } else if (seconds >= 0 && seconds <= 3 * 60) {
            countDownRemainTime = seconds;
            if (!isCountDown2) {
                if (mHandler != null) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = TimingHandler.MESSAGE_COUNT_DOWN;
                    msg.obj = ThreeTuple.create(result.title, result.description, result.restrictType);
                    mHandler.sendMessage(msg);
                    isCountDown2 = true;
                }
            }
        }
    }

}
