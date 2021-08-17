package com.tapsdk.antiaddiction;

import android.content.Context;

import com.tapsdk.antiaddiction.config.AntiAddictionFunctionConfig;
import com.tapsdk.antiaddiction.entities.IdentificationInfo;
import com.tapsdk.antiaddiction.entities.response.CheckPayResult;
import com.tapsdk.antiaddiction.entities.response.IdentifyResult;
import com.tapsdk.antiaddiction.entities.response.SubmitPayResult;

public class AntiAddictionKit {

    private static final IAntiAddiction antiAddiction = new AntiAddictionImpl();

    private static boolean isDebug = false;

    public static void init(Context context, String gameIdentifier
            , AntiAddictionFunctionConfig antiAddictionFunctionConfig, AntiAddictionCallback callback) {
        antiAddiction.init(context, gameIdentifier, antiAddictionFunctionConfig, callback);
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

    public static String currentToken() {
        return antiAddiction.currentToken();
    }

    public static int currentUserType() {
        return antiAddiction.currentUserType();
    }

    public static int currentUserRemainTime() {
        return antiAddiction.currentUserRemainTime();
    }
}
