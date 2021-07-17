package com.tapsdk.antiaddiction.skynet;

import com.tapsdk.antiaddiction.skynet.okhttp3.OkHttpClient;
import com.tapsdk.antiaddiction.skynet.retrofit2.Retrofit;

import java.util.HashMap;
import java.util.Map;

/**
 * 该类主要作用是集中管理retrofit和okhttp的配置
 *
 *
 */
public class Skynet {

    /**
     * 访问主业务使用的retrofit.
     */
    public static String RETROFIT_FOR_ANTI_ADDICTION = "anti-addiction";

    public static String RETROFIT_FOR_IDENTIFY = "identify";

    private static class Holder {
        static Skynet INSTANCE = new Skynet();
    }

    private Skynet() {}

    public static Skynet getInstance() {
        return Holder.INSTANCE;
    }

    final Map<String, Retrofit> retrofitMap = new HashMap<>();
    final Map<String, OkHttpClient> okHttpClientMap = new HashMap<>();

    /**
     * 不同sdk使用的域名和配置会有所不同，在sdk初始化的时候请务必先注册网络服务
     * @param sdkName
     * @param retrofit
     */
    public void registerRetrofit(String sdkName, Retrofit retrofit) {
        retrofitMap.put(sdkName, retrofit);
    }

    public Retrofit getRetrofit(String sdkName) {
        return retrofitMap.get(sdkName);
    }

    public static <T> T getService(String retrofitName, Class<T> service) {
        return getInstance().getRetrofit(retrofitName).create(service);
    }
}
