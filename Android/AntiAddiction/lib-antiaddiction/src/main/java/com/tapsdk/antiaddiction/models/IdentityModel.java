package com.tapsdk.antiaddiction.models;

import android.text.TextUtils;

import com.tapsdk.antiaddiction.Callback;
import com.tapsdk.antiaddiction.entities.AuthIdentityResult;
import com.tapsdk.antiaddiction.entities.IdentificationInfo;
import com.tapsdk.antiaddiction.entities.request.IdentifyRequestParams;
import com.tapsdk.antiaddiction.entities.response.IdentifyResult;
import com.tapsdk.antiaddiction.reactor.Observable;
import com.tapsdk.antiaddiction.rest.api.IdentificationApi;
import com.tapsdk.antiaddiction.skynet.Skynet;

public class IdentityModel {

    public Observable<IdentificationInfo> inquireState(String token) {
        IdentificationApi api = Skynet.getService(Skynet.RETROFIT_FOR_IDENTIFY, IdentificationApi.class);
        return api.inquireIdentificationInfo(token);
    }

    public Observable<IdentifyResult> identifyUser(String token, String name, String idCard) {
        String illegalArgument = "";
        if (TextUtils.isEmpty(token)) {
            illegalArgument = "token";
        } else if (TextUtils.isEmpty(idCard)) {
            illegalArgument = "id card";
        } else if (TextUtils.isEmpty(name)) {
            illegalArgument = "name";
        }

        if (!TextUtils.isEmpty(illegalArgument)) {
            return Observable.error(new Throwable(illegalArgument + " param is empty"));
        }

        IdentificationApi api = Skynet.getService(Skynet.RETROFIT_FOR_IDENTIFY, IdentificationApi.class);
        IdentifyRequestParams identifyRequestParams = new IdentifyRequestParams();
        identifyRequestParams.token = token;
        identifyRequestParams.idCard = idCard;
        identifyRequestParams.name = name;
        return api.identifyUser(identifyRequestParams);
    }
}
