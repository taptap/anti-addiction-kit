package com.tapsdk.antiaddiction.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tapsdk.antiaddiction.entities.response.ServerTime;
import com.tapsdk.antiaddiction.rest.api.AntiAddictionApi;
import com.tapsdk.antiaddiction.skynet.Skynet;
import com.tapsdk.antiaddiction.skynet.retrofit2.Call;
import com.tapsdk.antiaddiction.skynet.retrofit2.Response;
import com.tapsdk.antiaddiction.utils.AntiAddictionLogger;

public class TimeModel {

    public static Long getServerTimeSync() {
        AntiAddictionApi api = Skynet.getService(Skynet.RETROFIT_FOR_ANTI_ADDICTION, AntiAddictionApi.class);
        Gson gson = new GsonBuilder().create();
        Call<String> call = api.fetchServerTimeSync();
        try {
            Response<String> response = call.execute();
            if (response.isSuccessful()) {
                return gson.fromJson(response.body(), ServerTime.class).timestamp;
            }
        } catch (Exception e) {
            AntiAddictionLogger.printStackTrace(e);
        }
        return -1L;
    }
}
