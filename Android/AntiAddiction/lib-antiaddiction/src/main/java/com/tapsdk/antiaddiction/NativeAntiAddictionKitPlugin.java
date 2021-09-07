package com.tapsdk.antiaddiction;


import android.app.Activity;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.tapsdk.antiaddiction.config.AntiAddictionFunctionConfig;
import com.tapsdk.antiaddiction.entities.IdentificationInfo;
import com.tapsdk.antiaddiction.entities.response.CheckPayResult;
import com.tapsdk.antiaddiction.entities.response.IdentifyResult;
import com.tapsdk.antiaddiction.entities.response.SubmitPayResult;
import com.tapsdk.antiaddiction.utils.AntiAddictionLogger;
import com.unity3d.player.UnityPlayer;

import java.util.Map;

interface INativeAntiAddictionPlugin {
    void initSDK(Activity activity, String gameIdentifier
            , boolean useTimeLimit
            , boolean usePaymentLimit
            , String antiServerUrl
            , String identifyServerUrl
            , String departmentSocketUrl
            , String antiSecretKey
    );
}

public class NativeAntiAddictionKitPlugin implements INativeAntiAddictionPlugin {

    private static final String TAG = "NativeAntiAddictionKitPlugin";

    private static final String GAME_OBJECT_NAME = "PluginBridge";

    public NativeAntiAddictionKitPlugin() {
    }

    public void initSDK(Activity activity, String gameIdentifier
            , boolean useTimeLimit
            , boolean usePaymentLimit
            , String antiServerUrl
            , String identifyServerUrl
            , String departmentSocketUrl
            , String antiSecretKey
    ) {
        AntiAddictionLogger.d("Bridge init:["
                + "gameIdentifier:" + gameIdentifier
                + ",useTimeLimit" + useTimeLimit
                + ",usePaymentLimit" + usePaymentLimit
                + "]"
        );
        AntiAddictionFunctionConfig antiAddictionFunctionConfig
                = new AntiAddictionFunctionConfig.Builder()
                .enableOnLineTimeLimit(useTimeLimit)
                .enablePaymentLimit(usePaymentLimit)
                .withAntiAddictionServerUrl(antiServerUrl)
                .withIdentifyVerifiedServerUrl(identifyServerUrl)
                .withDepartmentSocketUrl(departmentSocketUrl)
                .withAntiAddictionSecretKey(antiSecretKey)
                .build();
        AntiAddictionKit.init(activity, gameIdentifier, antiAddictionFunctionConfig, new AntiAddictionCallback() {
            @Override
            public void onCallback(int code, Map<String, Object> extras) {
                try {
                    JsonObject extrasJSONObject = new JsonObject();
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("code", code);
                    if (extras != null) {
                        for (Map.Entry<String, Object> entry : extras.entrySet()) {
                            extrasJSONObject.addProperty(entry.getKey(), String.valueOf(entry.getValue()));
                        }
                        jsonObject.addProperty("extras", extrasJSONObject.toString());
                    }

                    String antiAddictionCallbackDataStr = jsonObject.toString();
                    AntiAddictionLogger.d("onCallback:" + antiAddictionCallbackDataStr);
                    UnityPlayer.UnitySendMessage(GAME_OBJECT_NAME, "HandleAntiAddictionCallbackMsg", antiAddictionCallbackDataStr);
                } catch (Exception e) {
                    String errorMsg;
                    if (!TextUtils.isEmpty(e.getMessage())) {
                        errorMsg = e.getMessage();
                    } else {
                        errorMsg = e.toString();
                    }
                    UnityPlayer.UnitySendMessage(GAME_OBJECT_NAME, "HandleException", errorMsg);
                }
            }
        });
    }

    public void login(String userId) {
        AntiAddictionLogger.d("Bridge login:["
                + "userId:" + userId
                + "]");
        AntiAddictionKit.login(userId);
    }

    public void enterGame() {
        AntiAddictionLogger.d("Bridge enterGame");
        AntiAddictionKit.enterGame();
    }

    public void leaveGame() {
        AntiAddictionLogger.d("Bridge leaveGame");
        AntiAddictionKit.leaveGame();
    }

    public void logout() {
        AntiAddictionLogger.d("Bridge logout");
        AntiAddictionKit.logout();
    }

    public String getCurrentToken() {
//        AntiAddictionLogger.d("Bridge getCurrent Token");
        return AntiAddictionKit.currentToken();
    }

    public int getCurrentUserType() {
//        AntiAddictionLogger.d("Bridge getCurrent UserType");
        return AntiAddictionKit.currentUserType();
    }

    public int getCurrentUserRemainTime() {
//        AntiAddictionLogger.d("Bridge getCurrentUserRemainTime");
        return AntiAddictionKit.currentUserRemainTime();
    }

    public void fetchIdentificationInfo(String userId) {
        AntiAddictionLogger.d("Bridge fetchIdentificationInfo");
        AntiAddictionKit.fetchUserIdentifyInfo(userId, new Callback<IdentificationInfo>() {
            @Override
            public void onSuccess(IdentificationInfo identificationInfo) {
                try {
                    JsonObject identificationInfoJSONObject = new JsonObject();
                    identificationInfoJSONObject.addProperty("authState", identificationInfo.authState);
                    identificationInfoJSONObject.addProperty("idCard", identificationInfo.idCard);
                    identificationInfoJSONObject.addProperty("name", identificationInfo.name);
                    identificationInfoJSONObject.addProperty("phoneNumber", identificationInfo.phoneNumber);
                    identificationInfoJSONObject.addProperty("antiAddictionToken", identificationInfo.antiAddictionToken);
                    UnityPlayer.UnitySendMessage(GAME_OBJECT_NAME, "HandleFetchIdentificationInfo", identificationInfoJSONObject.toString());
                } catch (Exception e) {
                    throw e;
                }
            }

            @Override
            public void onError(Throwable throwable) {
                String errorMsg;
                if (!TextUtils.isEmpty(throwable.getMessage())) {
                    errorMsg = throwable.getMessage();
                } else {
                    errorMsg = throwable.toString();
                }
                UnityPlayer.UnitySendMessage(GAME_OBJECT_NAME, "HandleFetchIdentificationException", errorMsg);
            }
        });
    }

    public void authIdentity(String userId, String name, String idCard) {
        AntiAddictionLogger.d("Bridge authIdentity[userId:" + userId + ",name:" + name
                + ",idCard:" + idCard + "]");
        AntiAddictionKit.authIdentity(userId, name, idCard, ""
                , new Callback<IdentifyResult>() {
                    @Override
                    public void onSuccess(IdentifyResult identifyResult) {
                        try {
                            JsonObject identificationInfoJSONObject = new JsonObject();
                            identificationInfoJSONObject.addProperty("identifyState", identifyResult.identifyState);
                            UnityPlayer.UnitySendMessage(GAME_OBJECT_NAME, "HandleAuthIdentity", identificationInfoJSONObject.toString());
                        } catch (Exception e) {
                            throw e;
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        String errorMsg;
                        if (!TextUtils.isEmpty(throwable.getMessage())) {
                            errorMsg = throwable.getMessage();
                        } else {
                            errorMsg = throwable.toString();
                        }
                        UnityPlayer.UnitySendMessage(GAME_OBJECT_NAME, "HandleAuthIdentityException", errorMsg);
                    }
                });
    }

    public void checkPayLimit(long amount) {
        AntiAddictionLogger.d("Bridge checkPayLimit[amount:" + amount + "]");
        AntiAddictionKit.checkPayLimit(amount, new Callback<CheckPayResult>() {
            @Override
            public void onSuccess(CheckPayResult result) {
                try {
                    JsonObject checkPayLimitJSONObject = new JsonObject();
                    checkPayLimitJSONObject.addProperty("status", result.status);
                    checkPayLimitJSONObject.addProperty("title", result.title);
                    checkPayLimitJSONObject.addProperty("description", result.description);
                    UnityPlayer.UnitySendMessage(GAME_OBJECT_NAME, "HandleCheckPayLimit", checkPayLimitJSONObject.toString());
                } catch (Exception e) {
                    throw e;
                }
            }

            @Override
            public void onError(Throwable throwable) {
                String errorMsg;
                if (!TextUtils.isEmpty(throwable.getMessage())) {
                    errorMsg = throwable.getMessage();
                } else {
                    errorMsg = throwable.toString();
                }
                UnityPlayer.UnitySendMessage(GAME_OBJECT_NAME, "HandleCheckPayLimitException", errorMsg);
            }
        });
    }

    public void submitPayResult(long amount) {
        AntiAddictionLogger.d("Bridge submitPayResult[amount:" + amount + "]");
        AntiAddictionKit.paySuccess(amount, new Callback<SubmitPayResult>() {
            @Override
            public void onSuccess(SubmitPayResult result) {
                try {
                    UnityPlayer.UnitySendMessage(GAME_OBJECT_NAME, "HandleSubmitPayResult", "");
                } catch (Exception e) {
                    throw e;
                }
            }

            @Override
            public void onError(Throwable throwable) {
                String errorMsg;
                if (!TextUtils.isEmpty(throwable.getMessage())) {
                    errorMsg = throwable.getMessage();
                } else {
                    errorMsg = throwable.toString();
                }
                UnityPlayer.UnitySendMessage(GAME_OBJECT_NAME, "HandleSubmitPayResultException", errorMsg);
            }
        });
    }
}
