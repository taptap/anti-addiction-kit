package com.tapsdk.antiaddiction.models;

import com.tapsdk.antiaddiction.entities.AntiAddictionConfig;
import com.tapsdk.antiaddiction.rest.api.AntiAddictionApi;
import com.tapsdk.antiaddiction.skynet.Skynet;
import com.tapsdk.antiaddiction.skynet.retrofit2.Call;
import com.tapsdk.antiaddiction.skynet.retrofit2.Response;

import java.io.IOException;

public class ConfigModel {

    private AntiAddictionConfig antiAddictionConfig;

    public AntiAddictionConfig fetchCommonConfig(String game) {
        if (antiAddictionConfig != null) return antiAddictionConfig;
        AntiAddictionApi antiAddictionApi = Skynet.getService(Skynet.RETROFIT_FOR_ANTI_ADDICTION, AntiAddictionApi.class);
        try {
            Call<AntiAddictionConfig> call = antiAddictionApi.fetchConfig(game);
            Response<AntiAddictionConfig> response = call.execute();
            if (response.isSuccessful()) {
                antiAddictionConfig = response.body();
                return antiAddictionConfig;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
