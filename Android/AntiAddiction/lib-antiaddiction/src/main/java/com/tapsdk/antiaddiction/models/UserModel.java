package com.tapsdk.antiaddiction.models;

import com.tapsdk.antiaddiction.entities.IdentificationInfo;
import com.tapsdk.antiaddiction.entities.UserInfo;
import com.tapsdk.antiaddiction.entities.request.AuthenticateRequestParams;
import com.tapsdk.antiaddiction.reactor.Observable;
import com.tapsdk.antiaddiction.rest.api.AntiAddictionApi;
import com.tapsdk.antiaddiction.skynet.Skynet;
import com.tapsdk.antiaddiction.utils.DeviceUtil;

public class UserModel {

    private String gameToken = null;

    private IdentificationInfo identificationInfo = null;

    private UserInfo userInfo = null;

    public String getGameToken() {
        return gameToken;
    }

    public void setGameToken(String gameToken) {
        this.gameToken = gameToken;
    }

    public void setIdentificationInfo(IdentificationInfo identificationInfo) {
        this.identificationInfo = identificationInfo;
        UserInfo userInfo = new UserInfo();
        userInfo.accessToken = identificationInfo.antiAddictionToken;
        setCurrentUser(userInfo);
    }

    public IdentificationInfo getIdentificationInfo() {
        return identificationInfo;
    }

    public void setCurrentUser(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public UserInfo getCurrentUser() {
        return userInfo;
    }

    public Observable<UserInfo> authenticate(String token, String gameIdentify, int carrier) {
        AntiAddictionApi api = Skynet.getService(Skynet.RETROFIT_FOR_ANTI_ADDICTION, AntiAddictionApi.class);
        AuthenticateRequestParams params = new AuthenticateRequestParams(token, gameIdentify, carrier);
        return api.authenticate(params);
    }

    public void logout() {
        userInfo = null;
    }
}
