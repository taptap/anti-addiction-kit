package com.tapsdk.antiaddiction;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tapsdk.antiaddiction.config.AntiAddictionFunctionConfig;
import com.tapsdk.antiaddiction.constants.Constants;
import com.tapsdk.antiaddiction.entities.AuthIdentityResult;
import com.tapsdk.antiaddiction.entities.IdentificationInfo;
import com.tapsdk.antiaddiction.entities.UserInfo;
import com.tapsdk.antiaddiction.entities.response.IdentifyResult;
import com.tapsdk.antiaddiction.models.IdentityModel;
import com.tapsdk.antiaddiction.models.UserModel;
import com.tapsdk.antiaddiction.reactor.Observable;
import com.tapsdk.antiaddiction.reactor.Subscriber;
import com.tapsdk.antiaddiction.reactor.Subscription;
import com.tapsdk.antiaddiction.reactor.functions.Func1;
import com.tapsdk.antiaddiction.reactor.rxandroid.schedulers.AndroidSchedulers;
import com.tapsdk.antiaddiction.reactor.schedulers.Schedulers;
import com.tapsdk.antiaddiction.rest.utils.HttpUtil;
import com.tapsdk.antiaddiction.rest.utils.SignUtil;
import com.tapsdk.antiaddiction.skynet.Skynet;
import com.tapsdk.antiaddiction.skynet.logging.HttpLoggingInterceptor;
import com.tapsdk.antiaddiction.skynet.okhttp3.HttpUrl;
import com.tapsdk.antiaddiction.skynet.okhttp3.Interceptor;
import com.tapsdk.antiaddiction.skynet.okhttp3.OkHttpClient;
import com.tapsdk.antiaddiction.skynet.okhttp3.Request;
import com.tapsdk.antiaddiction.skynet.okhttp3.RequestBody;
import com.tapsdk.antiaddiction.skynet.okhttp3.Response;
import com.tapsdk.antiaddiction.skynet.retrofit2.Retrofit;
import com.tapsdk.antiaddiction.skynet.retrofit2.adapter.converter.gson.GsonConverterFactory;
import com.tapsdk.antiaddiction.skynet.retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import com.tapsdk.antiaddiction.utils.AntiAddictionLogger;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static com.tapsdk.antiaddiction.constants.Constants.API.ACCESS_TOKEN_TYPE_BEARER;

public class AntiAddictionImpl implements IAntiAddiction {

    private boolean initialized = false;
    private WeakReference<Activity> activityWeakReference;
    private String gameIdentifier;
    private AntiAddictionFunctionConfig antiAddictionFunctionConfig;
    private AntiAddictionCallback antiAddictionCallback;

    private UserModel userModel = new UserModel();
    private IdentityModel identityModel = new IdentityModel();

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
                .addInterceptor(logInterceptor)
                .addInterceptor(authInterceptor);

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
                            AntiAddictionLogger.d("postBodyString:" + postBodyString);
                            extraBuilder.append(postBodyString);
//                            Gson gson = new GsonBuilder().create();
//                            Map<String, String> srcMap = gson.fromJson(postBodyString
//                                    , new TypeToken<Map<String, String>>() {}.getType());
//                            TreeMap<String, String> treeMap = new TreeMap<>(new Comparator<String>() {
//                                @Override
//                                public int compare(String o1, String o2) {
//                                    return o1.compareTo(o2);
//                                }
//                            });
//                            treeMap.putAll(srcMap);
//                            for (Map.Entry<String, String> entry:treeMap.entrySet()) {
//                                extraBuilder.append(entry.getKey());
//                                extraBuilder.append(entry.getValue());
//                            }
                        } catch (Exception e) {
                            throw new IOException(e.getMessage());
                        }
                        break;
                    default:
                        throw new IOException("Unsupported http method");
                }
                if (!TextUtils.isEmpty(extraBuilder.toString())) {
                    signCode += extraBuilder.toString();
                    AntiAddictionLogger.d("signCode:" + signCode);
                }
                signCode = SignUtil.getSignCode(signCode);
                AntiAddictionLogger.d("signCode:" + signCode);
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
    public void init(Activity activity, String gameIdentifier
            , AntiAddictionFunctionConfig antiAddictionFunctionConfig, AntiAddictionCallback antiAddictionCallback) {
        if (initialized) return;
        this.activityWeakReference = new WeakReference<>(activity);
        this.gameIdentifier = gameIdentifier;
        this.antiAddictionFunctionConfig = antiAddictionFunctionConfig;
        this.antiAddictionCallback = antiAddictionCallback;
        initSkynet();
        initialized = true;
    }

    @Override
    public void login(final String gameToken) {
        if (loginSubscription != null && !loginSubscription.isUnsubscribed()) {
            AntiAddictionLogger.w("login in progress, call it later");
            return;
        }
        loginSubscription = identityModel.inquireState(gameToken)
                .map(new Func1<IdentificationInfo, IdentificationInfo>(){
                    @Override
                    public IdentificationInfo call(IdentificationInfo identificationInfo) {
                        userModel.setIdentificationInfo(identificationInfo);
                        return identificationInfo;
                    }
                })
                .flatMap(new Func1<IdentificationInfo, Observable<UserInfo>>() {
                    @Override
                    public Observable<UserInfo> call(IdentificationInfo identificationInfo) {
                        return userModel.authenticate(identificationInfo.antiAddictionToken, gameIdentifier);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserInfo>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(UserInfo userInfo) {
                        AntiAddictionLogger.d("AntiAddiction login successfully" + userInfo.toString());
                    }
                });
    }

    @Override
    public void logout() {

    }

    @Override
    public void enterGame() {

    }

    @Override
    public void leaveGame() {

    }

    @Override
    public void checkPayLimit(long amount) {

    }

    @Override
    public void paySuccess(long amount) {

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
}
