package com.tapsdk.antiaddiction;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tapsdk.antiaddiction.config.AntiAddictionFunctionConfig;
import com.tapsdk.antiaddiction.constants.Constants;
import com.tapsdk.antiaddiction.entities.CommonConfig;
import com.tapsdk.antiaddiction.entities.IdentificationInfo;
import com.tapsdk.antiaddiction.entities.SubmitPlayLogResult;
import com.tapsdk.antiaddiction.entities.TwoTuple;
import com.tapsdk.antiaddiction.entities.UserInfo;
import com.tapsdk.antiaddiction.entities.request.PlayLogRequestParams;
import com.tapsdk.antiaddiction.entities.response.CheckPayResult;
import com.tapsdk.antiaddiction.entities.response.IdentifyResult;
import com.tapsdk.antiaddiction.entities.response.SubmitPayResult;
import com.tapsdk.antiaddiction.enums.AccountLimitTipEnum;
import com.tapsdk.antiaddiction.models.AntiAddictionLimitInfoAction;
import com.tapsdk.antiaddiction.models.ConfigModel;
import com.tapsdk.antiaddiction.models.IdentityModel;
import com.tapsdk.antiaddiction.models.PaymentModel;
import com.tapsdk.antiaddiction.models.PlayLogModel;
import com.tapsdk.antiaddiction.models.PromptType;
import com.tapsdk.antiaddiction.models.StrictType;
import com.tapsdk.antiaddiction.models.TimeModel;
import com.tapsdk.antiaddiction.models.TimingModel;
import com.tapsdk.antiaddiction.models.UpdateAccountAction;
import com.tapsdk.antiaddiction.models.UserModel;
import com.tapsdk.antiaddiction.reactor.Observable;
import com.tapsdk.antiaddiction.reactor.RxBus;
import com.tapsdk.antiaddiction.reactor.Subscriber;
import com.tapsdk.antiaddiction.reactor.Subscription;
import com.tapsdk.antiaddiction.reactor.functions.Action1;
import com.tapsdk.antiaddiction.reactor.functions.Func1;
import com.tapsdk.antiaddiction.reactor.rxandroid.schedulers.AndroidSchedulers;
import com.tapsdk.antiaddiction.reactor.schedulers.Schedulers;
import com.tapsdk.antiaddiction.rest.utils.HttpUtil;
import com.tapsdk.antiaddiction.rest.utils.SignUtil;
import com.tapsdk.antiaddiction.settings.AntiAddictionSettings;
import com.tapsdk.antiaddiction.skynet.Skynet;
import com.tapsdk.antiaddiction.skynet.logging.HttpLoggingInterceptor;
import com.tapsdk.antiaddiction.skynet.okhttp3.HttpUrl;
import com.tapsdk.antiaddiction.skynet.okhttp3.Interceptor;
import com.tapsdk.antiaddiction.skynet.okhttp3.OkHttpClient;
import com.tapsdk.antiaddiction.skynet.okhttp3.Request;
import com.tapsdk.antiaddiction.skynet.okhttp3.Response;
import com.tapsdk.antiaddiction.skynet.retrofit2.Retrofit;
import com.tapsdk.antiaddiction.skynet.retrofit2.adapter.converter.gson.GsonConverterFactory;
import com.tapsdk.antiaddiction.skynet.retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import com.tapsdk.antiaddiction.utils.AntiAddictionLogger;
import com.tapsdk.antiaddiction.utils.DeviceUtil;
import com.tapsdk.antiaddiction.utils.TimeUtil;
import com.tapsdk.antiaddiction.ws.IReceiveMessage;
import com.tapsdk.antiaddiction.ws.WebSocketManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.tapsdk.antiaddiction.constants.Constants.API.ACCESS_TOKEN_TYPE_BEARER;

public class AntiAddictionImpl implements IAntiAddiction {

    private boolean initialized = false;
    private Context applicationContext;
    private String gameIdentifier;
    private AntiAddictionFunctionConfig antiAddictionFunctionConfig;
    private AntiAddictionCallback antiAddictionCallback;
    private final Handler mainLooperHandler = new Handler(Looper.getMainLooper());
    private boolean canPlay = false;

    private final UserModel userModel = new UserModel();
    private final IdentityModel identityModel = new IdentityModel();
    private final ConfigModel configModel = new ConfigModel();
    private TimingModel timingModel;
    private final PaymentModel paymentModel = new PaymentModel();

    private final Gson gson = new GsonBuilder().create();

    private volatile Subscription loginSubscription = null;

    private void initSkynet() {
        OkHttpClient.Builder antiAddictionOkhttpBuilder = new OkHttpClient.Builder();
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        final Interceptor authInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                UserInfo currentUser = userModel.getCurrentUser();
                if (null == currentUser || TextUtils.isEmpty(currentUser.accessToken))
                    return chain.proceed(chain.request());
                Request newRequest = chain.request().newBuilder()
                        .header("Authorization", ACCESS_TOKEN_TYPE_BEARER + " " + currentUser.accessToken)
                        .build();
                return chain.proceed(newRequest);
            }
        };
        antiAddictionOkhttpBuilder
                .addInterceptor(authInterceptor)
                .addInterceptor(logInterceptor);

        Retrofit antiAddictionRetrofit = new Retrofit.Builder()
                .baseUrl(Constants.API.ANTI_ADDICTION_BASE_URL)
                .client(antiAddictionOkhttpBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        Skynet.getInstance().registerRetrofit(Skynet.RETROFIT_FOR_ANTI_ADDICTION, antiAddictionRetrofit);

        final Interceptor identifyInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {

                Request originRequest = chain.request();
                StringBuilder extraBuilder = new StringBuilder();
                String signCode = Constants.IdentificationConfig.SECRET_KEY;

                switch (originRequest.method()) {
                    case "GET":
                        HttpUrl httpUrl = originRequest.url();
                        Set<String> queryParameterNamesTreeSet = new TreeSet<>(httpUrl.queryParameterNames());
                        for (String queryParameterName : queryParameterNamesTreeSet) {
                            List<String> queryValues = httpUrl.queryParameterValues(queryParameterName);
                            if (queryValues.size() > 0 && !TextUtils.isEmpty(queryValues.get(0))) {
                                extraBuilder.append(queryParameterName);
                                extraBuilder.append(queryValues.get(0));
                            }
                        }
                        break;
                    case "POST":
                        try {
                            String postBodyString = HttpUtil.bodyToString(originRequest.body());
                            Log.d("OkHttp", postBodyString);
                            extraBuilder.append(postBodyString);
                        } catch (Exception e) {
                            throw new IOException(e.getMessage());
                        }
                        break;
                    default:
                        throw new IOException("Unsupported http method");
                }
                if (!TextUtils.isEmpty(extraBuilder.toString())) {
                    signCode += extraBuilder.toString();
                }
                Log.d("OkHttp", "signCode before:" +signCode);
                signCode = SignUtil.getSignCode(signCode);
                Log.d("OkHttp", "signCode after:" + signCode);
                Request.Builder newRequestBuilder = chain.request().newBuilder();
                if (!TextUtils.isEmpty(signCode)) {
                    newRequestBuilder.addHeader("sign", signCode);
                }
                return chain.proceed(newRequestBuilder.build());
            }
        };

        OkHttpClient.Builder identifyOkhttpBuilder = new OkHttpClient.Builder();
        identifyOkhttpBuilder.addInterceptor(logInterceptor)
                .addInterceptor(identifyInterceptor);

        Retrofit identifyRetrofit = new Retrofit.Builder()
                .baseUrl(Constants.API.IDENTIFY_BASE_URL)
                .client(identifyOkhttpBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        Skynet.getInstance().registerRetrofit(Skynet.RETROFIT_FOR_IDENTIFY, identifyRetrofit);
    }

    @Override
    public synchronized void init(Context context, String gameIdentifier
            , AntiAddictionFunctionConfig antiAddictionFunctionConfig, AntiAddictionCallback antiAddictionCallback) {
        if (initialized) return;
        this.applicationContext = context.getApplicationContext();
        this.gameIdentifier = gameIdentifier;
        this.antiAddictionFunctionConfig = antiAddictionFunctionConfig;
        this.antiAddictionCallback = antiAddictionCallback;
        this.timingModel = new TimingModel(userModel, applicationContext, gameIdentifier, new TimingModel.TimingMessageListener() {
            @Override
            public void onMessage(int type, Map<String, Object> extras) {
                notifyAntiAddictionMessage(type, extras);
            }
        });
        initSkynet();
        initialized = true;
    }

    @Override
    public void login(final String userId) {
        if (loginSubscription != null && !loginSubscription.isUnsubscribed()) {
            AntiAddictionLogger.w("login in progress, call it later");
            return;
        }
        loginSubscription = identityModel.inquireState(userId)
                .map(new Func1<IdentificationInfo, IdentificationInfo>() {
                    @Override
                    public IdentificationInfo call(IdentificationInfo identificationInfo) {
                        AntiAddictionLogger.d("fetch identificationInfo");
                        userModel.setIdentificationInfo(identificationInfo);
                        return identificationInfo;
                    }
                })
                .flatMap(new Func1<IdentificationInfo, Observable<UserInfo>>() {
                    @Override
                    public Observable<UserInfo> call(IdentificationInfo identificationInfo) {
                        AntiAddictionLogger.d("user authenticate");
                        return userModel.authenticate(identificationInfo.antiAddictionToken
                                , gameIdentifier, DeviceUtil.getOperatorInfo(applicationContext.getApplicationContext()));
                    }
                })
                .map(new Func1<UserInfo, TwoTuple<Boolean, SubmitPlayLogResult>>() {
                    @Override
                    public TwoTuple<Boolean, SubmitPlayLogResult> call(UserInfo userInfo) {
                        AntiAddictionLogger.d("check user state");
                        userModel.setCurrentUser(userInfo);
                        Context context = applicationContext.getApplicationContext();
                        CommonConfig defaultConfig = AntiAddictionSettings.getInstance().getCommonDefaultConfig(context);
                        AntiAddictionLogger.d("------fetch config------");
                        CommonConfig config = configModel.fetchCommonConfig(gameIdentifier);
                        if (config == null) {
                            AntiAddictionLogger.d("fetch system config fail use default config");
                            config = defaultConfig;
                        }
                        AntiAddictionLogger.d(gson.toJson(config));
                        AntiAddictionSettings.getInstance().setCommonConfig(config);

                        long serverTimeInSecond = TimeModel.getServerTimeSync();
                        AntiAddictionLogger.d("------sync server time------");
                        if (serverTimeInSecond == -1L) {
                            AntiAddictionLogger.d("fetch server time fail");
                            serverTimeInSecond = System.currentTimeMillis() / 1000;
                        } else {
                            AntiAddictionLogger.d("fetch server time success:" + serverTimeInSecond);
                        }
                        timingModel.setRecentServerTimeInSecond(serverTimeInSecond);

                        AntiAddictionLogger.d("------get player left time------");
                        PlayLogRequestParams playLogRequestParams
                                = PlayLogModel.getPlayLog(context
                                , userInfo
                                , gameIdentifier
                                , serverTimeInSecond, serverTimeInSecond, serverTimeInSecond
                                , serverTimeInSecond, serverTimeInSecond);
                        com.tapsdk.antiaddiction.skynet.retrofit2.Response<SubmitPlayLogResult> response
                                = PlayLogModel.checkUserStateSync(playLogRequestParams);
                        if (response.code() == 200) {
                            UserInfo currentUser = userModel.getCurrentUser();
                            userModel.getCurrentUser().resetRemainTime(response.body().remainTime);
                            AntiAddictionSettings.getInstance().clearHistoricalData(context, userModel.getCurrentUser().userId);
                            AntiAddictionLogger.d("player left time:" + response.body().remainTime);
                            if (currentUser.accountType == Constants.UserType.USER_TYPE_ADULT) {
                                AntiAddictionLogger.d("player's type is adult");
                                return TwoTuple.create(false, null);
                            } else {
                                AntiAddictionLogger.d("player's type is others");
                                return TwoTuple.create(true, response.body());
                            }
                        }
                        return TwoTuple.create(false, null);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TwoTuple<Boolean, SubmitPlayLogResult>>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        AntiAddictionLogger.e("login error");
                        AntiAddictionLogger.printStackTrace(e);
                        notifyAntiAddictionMessage(Constants.ANTI_ADDICTION_CALLBACK_CODE.LOGIN_SUCCESS, null);
                    }

                    @Override
                    public void onNext(TwoTuple<Boolean, SubmitPlayLogResult> result) {
                        UserInfo currentUser = userModel.getCurrentUser();

                        if (BuildConfig.DEBUG) {
                            if (currentUser != null)
                                RxBus.getInstance().send(new UpdateAccountAction(userModel.getCurrentUser()
                                        , userModel.getIdentificationInfo()));
                        }

                        if (currentUser == null) {
                            notifyAntiAddictionMessage(Constants.ANTI_ADDICTION_CALLBACK_CODE.LOGIN_SUCCESS, null);
                            return;
                        }
                        // firstParam -> needAntiAddiction
                        if (result.firstParam) {
                            if (currentUser.accountType == Constants.UserType.USER_TYPE_UNREALNAME
                                    || currentUser.accountType == Constants.UserType.USER_TYPE_UNKNOWN) {
                                processGuest(result.secondParam, currentUser);
                            } else {
                                processNonage(result.secondParam);
                            }
                        } else {
                            notifyAntiAddictionMessage(Constants.ANTI_ADDICTION_CALLBACK_CODE.LOGIN_SUCCESS, null);
                        }
                    }
                });
    }

    private void processGuest(SubmitPlayLogResult result, UserInfo userInfo) {
        AntiAddictionLogger.d("processGuest:" + userInfo.userId);
        AccountLimitTipEnum limitTipEnum;
        AntiAddictionSettings settings = AntiAddictionSettings.getInstance();
        TwoTuple<String, String> tipContent;
        int strictType = StrictType.NONE;
        if (result.remainTime == 0) {
            limitTipEnum = AccountLimitTipEnum.STATE_ENTER_LIMIT;
            if (result.restrictType == StrictType.NIGHT) {
                // 线上版未实名和游客没有宵禁类型
                // so只有版暑版会出现此条逻辑
                tipContent = settings.getPromptInfo(userInfo.accountType, PromptType.LOGIN_IN_NIGHT_WITH_NO_REMAINING_TIME);
                if (tipContent == null) {
                    tipContent = settings.getPromptInfo(userInfo.accountType, PromptType.LOGIN_WITH_NO_REMAINING_TIME);
                }
            } else {
                tipContent = settings.getPromptInfo(userInfo.accountType, PromptType.LOGIN_WITH_NO_REMAINING_TIME);
            }
            strictType = result.restrictType;
            if (antiAddictionFunctionConfig.onLineTimeLimitEnabled()) {
                int type = strictType == 1 ? Constants.ANTI_ADDICTION_CALLBACK_CODE.NIGHT_STRICT
                        : Constants.ANTI_ADDICTION_CALLBACK_CODE.TIME_LIMIT;
                notifyAntiAddictionMessage(type
                        , AntiAddictionSettings.getInstance().generateAlertMessage(""
                                , "", AccountLimitTipEnum.STATE_ENTER_LIMIT, strictType));
            } else {
                notifyAntiAddictionMessage(Constants.ANTI_ADDICTION_CALLBACK_CODE.LOGIN_SUCCESS, null);
            }
        } else if (result.costTime == 0) {
            limitTipEnum = AccountLimitTipEnum.STATE_ENTER_NO_LIMIT;
            tipContent = settings.getPromptInfo(userInfo.accountType, PromptType.DAILY_FIRST_LOGIN);
            notifyAntiAddictionMessage(Constants.ANTI_ADDICTION_CALLBACK_CODE.LOGIN_SUCCESS, null);
        } else {
            limitTipEnum = AccountLimitTipEnum.STATE_ENTER_NO_LIMIT;
            tipContent = settings.getPromptInfo(userInfo.accountType, PromptType.DAILY_FIRST_LOGIN_IN_NIGHT);
            notifyAntiAddictionMessage(Constants.ANTI_ADDICTION_CALLBACK_CODE.LOGIN_SUCCESS, null);
        }
        String description = tipContent.secondParam.replace("${remaining}", String.valueOf(TimeUtil.getMinute(result.remainTime)));
        notifyAntiAddictionMessage(Constants.ANTI_ADDICTION_CALLBACK_CODE.OPEN_ALERT_TIP
                , AntiAddictionSettings.getInstance().generateAlertMessage(tipContent.firstParam
                        , description, limitTipEnum, strictType));
    }

    private void processNonage(SubmitPlayLogResult result) {
        if (result.restrictType != StrictType.NONE) {
            AccountLimitTipEnum limitTipEnum;
            if (result.restrictType == StrictType.NIGHT) {
                if (result.remainTime <= 0) {
                    limitTipEnum = AccountLimitTipEnum.STATE_CHILD_ENTER_STRICT;
                    notifyAntiAddictionMessage(Constants.ANTI_ADDICTION_CALLBACK_CODE.NIGHT_STRICT
                            , AntiAddictionSettings.getInstance().generateAlertMessage(""
                                    , "", limitTipEnum, result.restrictType));
                } else {
                    limitTipEnum = AccountLimitTipEnum.STATE_CHILD_ENTER_NO_LIMIT;
                    notifyAntiAddictionMessage(Constants.ANTI_ADDICTION_CALLBACK_CODE.LOGIN_SUCCESS, null);
                }
            } else {
                if (result.remainTime <= 0) {
                    limitTipEnum = AccountLimitTipEnum.STATE_CHILD_ENTER_STRICT;
                    notifyAntiAddictionMessage(Constants.ANTI_ADDICTION_CALLBACK_CODE.TIME_LIMIT
                            , AntiAddictionSettings.getInstance().generateAlertMessage(""
                                    , "", limitTipEnum, result.restrictType));
                } else {
                    limitTipEnum = AccountLimitTipEnum.STATE_CHILD_ENTER_NO_LIMIT;
                    notifyAntiAddictionMessage(Constants.ANTI_ADDICTION_CALLBACK_CODE.LOGIN_SUCCESS, null);
                }
            }
            notifyAntiAddictionMessage(Constants.ANTI_ADDICTION_CALLBACK_CODE.OPEN_ALERT_TIP
                    , AntiAddictionSettings.getInstance().generateAlertMessage(result.title
                            , result.description, limitTipEnum, result.restrictType));
        } else {
            notifyAntiAddictionMessage(Constants.ANTI_ADDICTION_CALLBACK_CODE.LOGIN_SUCCESS, null);
        }
    }

    public void notifyAntiAddictionMessage(int type, Map<String, Object> extras) {
        switch (type) {
            case Constants.ANTI_ADDICTION_CALLBACK_CODE.LOGIN_SUCCESS:
                canPlay = true;
                break;
            case Constants.ANTI_ADDICTION_CALLBACK_CODE.TIME_LIMIT:
            case Constants.ANTI_ADDICTION_CALLBACK_CODE.NIGHT_STRICT:
            case Constants.ANTI_ADDICTION_CALLBACK_CODE.LOGOUT:
                canPlay = false;
                break;
            default:
                break;
        }
        if (BuildConfig.DEBUG
                && (Constants.ANTI_ADDICTION_CALLBACK_CODE.TIME_LIMIT == type
                || Constants.ANTI_ADDICTION_CALLBACK_CODE.NIGHT_STRICT == type
                || Constants.ANTI_ADDICTION_CALLBACK_CODE.LOGIN_SUCCESS == type
                || Constants.ANTI_ADDICTION_CALLBACK_CODE.LOGOUT == type
        )
        ) {
            boolean loggedIn = userModel.getCurrentUser() != null;
            if (Constants.ANTI_ADDICTION_CALLBACK_CODE.LOGOUT == type) {
                loggedIn = false;
            }
            int strictType = (Constants.ANTI_ADDICTION_CALLBACK_CODE.TIME_LIMIT == type)
                    ? StrictType.TIME_LIMIT : StrictType.NIGHT;
            RxBus.getInstance().send(new AntiAddictionLimitInfoAction(canPlay
                    , strictType
                    , loggedIn));
        }
        mainLooperHandler.post(new Runnable() {
            @Override
            public void run() {
                antiAddictionCallback.onCallback(type, extras);
            }
        });
    }

    @Override
    public void logout() {
        notifyAntiAddictionMessage(Constants.ANTI_ADDICTION_CALLBACK_CODE.LOGOUT, null);
        WebSocketManager.getInstance().close();
        userModel.logout();
        canPlay = false;
    }

    private final IReceiveMessage receiveMessageImpl = new IReceiveMessage() {
        @Override
        public void onConnectSuccess() {
            AntiAddictionLogger.d("webSocket onConnectSuccess");
        }

        @Override
        public void onConnectFailed() {
            AntiAddictionLogger.d("webSocket onConnectFailed");
        }

        @Override
        public void onClose() {
            AntiAddictionLogger.d("webSocket onClose");
        }

        @Override
        public void onMessage(String text) {
            AntiAddictionLogger.d("webSocket onMessage:" + text);
        }
    };

    @Override
    public void enterGame() {
        if (!antiAddictionFunctionConfig.onLineTimeLimitEnabled()
                || userModel.getCurrentUser() == null
                || !initialized
                || !canPlay
                || (!AntiAddictionSettings.getInstance().needUploadAllData()
                && userModel.getCurrentUser().accountType == Constants.UserType.USER_TYPE_ADULT)
                || timingModel.inTiming
        ) return;
        timingModel.bind();
        WebSocketManager.getInstance().init(userModel.getCurrentUser().accessToken, gameIdentifier, receiveMessageImpl, Constants.API.WEB_SOCKET_HOST);
    }

    @Override
    public void leaveGame() {
        if (userModel.getCurrentUser() == null
                || !initialized
                || !canPlay) return;
        timingModel.unbind();
        WebSocketManager.getInstance().close();
    }

    @Override
    public void checkPayLimit(long amount, Callback<CheckPayResult> callback) {
        if (!antiAddictionFunctionConfig.paymentLimitEnabled()
                || userModel.getCurrentUser() == null
                || !initialized) return;
        paymentModel.checkPay(amount, gameIdentifier)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CheckPayResult>() {
                    @Override
                    public void call(CheckPayResult checkPayResult) {
                        callback.onSuccess(checkPayResult);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        callback.onError(throwable);
                    }
                });
    }

    @Override
    public void paySuccess(long amount, Callback<SubmitPayResult> callback) {
        if (!antiAddictionFunctionConfig.paymentLimitEnabled()
                || userModel.getCurrentUser() == null
                || !initialized) return;
        paymentModel.paySuccess(amount, gameIdentifier)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<SubmitPayResult>() {
                    @Override
                    public void call(SubmitPayResult submitPayResult) {
                        callback.onSuccess(submitPayResult);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        callback.onError(throwable);
                    }
                });
    }

    @Override
    public void authIdentity(String token, String name, String idCard, String phoneNumber, Callback<IdentifyResult> callback) {
        identityModel.identifyUser(token, name, idCard)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<IdentifyResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onError(e);
                    }

                    @Override
                    public void onNext(IdentifyResult identifyResult) {
                        callback.onSuccess(identifyResult);
                    }
                });
    }

    @Override
    public void fetchUserIdentifyInfo(String token, Callback<IdentificationInfo> callback) {
        identityModel.inquireState(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<IdentificationInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onError(e);
                    }

                    @Override
                    public void onNext(IdentificationInfo identificationInfo) {
                        callback.onSuccess(identificationInfo);
                    }
                });
        ;
    }

    @Override
    public String currentToken() {
        UserInfo userInfo = userModel.getCurrentUser();
        if (userInfo != null) return userInfo.accessToken;
        return "";
    }

    @Override
    public int currentUserType() {
        UserInfo userInfo = userModel.getCurrentUser();
        if (userInfo != null) return userInfo.accountType;
        return -1;
    }

    @Override
    public int currentUserRemainTime() {
        UserInfo userInfo = userModel.getCurrentUser();
        if (userInfo != null) return userInfo.remainTime;
        return -1;
    }
}
