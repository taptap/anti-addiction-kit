package com.tapsdk.antiaddiction.models;

import com.tapsdk.antiaddiction.reactor.Observable;
import com.tapsdk.antiaddiction.rest.api.AntiAddictionApi;
import com.tapsdk.antiaddiction.skynet.Skynet;

public class TimeModel {

    public Observable<Long> getServerTimeSync() {
        AntiAddictionApi api = Skynet.getService(Skynet.RETROFIT_FOR_ANTI_ADDICTION, AntiAddictionApi.class);
        return api.fetchServerTime();
    }
}
