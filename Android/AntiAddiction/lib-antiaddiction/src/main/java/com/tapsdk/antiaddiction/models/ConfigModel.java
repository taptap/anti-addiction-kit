package com.tapsdk.antiaddiction.models;

import com.tapsdk.antiaddiction.entities.CommonConfig;
import com.tapsdk.antiaddiction.rest.api.AntiAddictionApi;
import com.tapsdk.antiaddiction.skynet.Skynet;
import com.tapsdk.antiaddiction.skynet.retrofit2.Call;
import com.tapsdk.antiaddiction.skynet.retrofit2.Response;

import java.io.IOException;

public class ConfigModel {

    private CommonConfig commonConfig;

    public CommonConfig fetchCommonConfig(String game) {
        if (commonConfig != null) return commonConfig;
        AntiAddictionApi antiAddictionApi = Skynet.getService(Skynet.RETROFIT_FOR_ANTI_ADDICTION, AntiAddictionApi.class);
        try {
            Call<CommonConfig> call = antiAddictionApi.fetchConfig(game);
            Response<CommonConfig> response = call.execute();
            if (response.isSuccessful()) {
                commonConfig = response.body();
                return commonConfig;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
