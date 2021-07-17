package com.tapsdk.antiaddiction;

import android.app.Activity;

import com.tapsdk.antiaddiction.config.AntiAddictionFunctionConfig;
import com.tapsdk.antiaddiction.entities.AuthIdentityResult;
import com.tapsdk.antiaddiction.entities.IdentificationInfo;
import com.tapsdk.antiaddiction.entities.response.IdentifyResult;

public class AntiAddictionKit {

    private static IAntiAddiction antiAddiction = new AntiAddictionImpl();

    public static void init(Activity activity, String gameIdentifier
            ,AntiAddictionFunctionConfig antiAddictionFunctionConfig, AntiAddictionCallback callback) {
        antiAddiction.init(activity, gameIdentifier, antiAddictionFunctionConfig, callback);
    }

    public static void login(String gameToken) {
        antiAddiction.login(gameToken);
    }

    public static void logout() {
        antiAddiction.logout();
    }

    public static void enterGame() {
        antiAddiction.enterGame();
    }

    public static void leaveGame() {
        antiAddiction.leaveGame();
    }

    public static void checkPayLimit(long amount) {
        antiAddiction.checkPayLimit(amount);
    }

    public static void paySuccess(long amount) {
        antiAddiction.paySuccess(amount);
    }

    public static void authIdentity(String token, String name, String idCard, String phoneNumber
            , Callback<IdentifyResult> callback) {
        antiAddiction.authIdentity(token, name, idCard, phoneNumber, callback);
    }

    public static void fetchUserIdentifyInfo(String token, Callback<IdentificationInfo> callback) {
        antiAddiction.fetchUserIdentifyInfo(token, callback);
    }
}
