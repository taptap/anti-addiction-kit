package com.tapsdk.antiaddiction;

import android.app.Activity;

import com.tapsdk.antiaddiction.config.AntiAddictionFunctionConfig;
import com.tapsdk.antiaddiction.entities.AuthIdentityResult;
import com.tapsdk.antiaddiction.entities.IdentificationInfo;
import com.tapsdk.antiaddiction.entities.response.CheckPayResult;
import com.tapsdk.antiaddiction.entities.response.IdentifyResult;
import com.tapsdk.antiaddiction.entities.response.SubmitPayResult;

public class AntiAddictionKit {

    public final static int CALLBACK_CODE_TIME_LIMIT_NONE = 100;
    public final static int CALLBACK_CODE_LOGIN_SUCCESS = 500;
    public final static int CALLBACK_CODE_LOGOUT = 1000;
    public final static int CALLBACK_CODE_PAY_NO_LIMIT = 1020;
    public final static int CALLBACK_CODE_PAY_LIMIT = 1025;
    public final static int CALLBACK_CODE_TIME_LIMIT = 1030;
    public final static int CALLBACK_CODE_NIGHT_STRICT = 1050;
    public final static int CALLBACK_CODE_OPEN_ALERT_TIP = 1095;

    private static final IAntiAddiction antiAddiction = new AntiAddictionImpl();

    private static boolean isDebug = false;

    public static void init(Activity activity, String gameIdentifier
            ,AntiAddictionFunctionConfig antiAddictionFunctionConfig, AntiAddictionCallback callback) {
        antiAddiction.init(activity, gameIdentifier, antiAddictionFunctionConfig, callback);
    }

    public static void login(String userId) {
        antiAddiction.login(userId);
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

    public static void authIdentity(String token, String name, String idCard, String phoneNumber
            , Callback<IdentifyResult> callback) {
        antiAddiction.authIdentity(token, name, idCard, phoneNumber, callback);
    }

    public static void fetchUserIdentifyInfo(String token, Callback<IdentificationInfo> callback) {
        antiAddiction.fetchUserIdentifyInfo(token, callback);
    }

    public static void setDebug(boolean debuggable) {
        isDebug = debuggable;
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static void checkPayLimit(long amount, Callback<CheckPayResult> callback) {
        antiAddiction.checkPayLimit(amount, callback);
    }

    public static void paySuccess(long amount, Callback<SubmitPayResult> callback) {
        antiAddiction.paySuccess(amount, callback);
    }
}
