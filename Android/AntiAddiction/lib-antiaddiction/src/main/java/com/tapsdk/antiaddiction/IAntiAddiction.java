package com.tapsdk.antiaddiction;

import android.content.Context;

import com.tapsdk.antiaddiction.config.AntiAddictionFunctionConfig;
import com.tapsdk.antiaddiction.entities.IdentificationInfo;
import com.tapsdk.antiaddiction.entities.response.CheckPayResult;
import com.tapsdk.antiaddiction.entities.response.IdentifyResult;
import com.tapsdk.antiaddiction.entities.response.SubmitPayResult;

public interface IAntiAddiction {

    /**
     * anti-addiction init
     * @param context
     * @param gameIdentifier
     * @param antiAddictionFunctionConfig
     * @param callback
     */
    void init(Context context, String gameIdentifier
            , AntiAddictionFunctionConfig antiAddictionFunctionConfig, AntiAddictionCallback callback);

    /**
     * unique identity of the user
     * @param userId
     */
    void login(String userId);

    void logout();

    void enterGame();

    void leaveGame();

    /**
     * check consumption limit
     * @param amount amount of consumption (ps.currency unit cent)
     */
    void checkPayLimit(long amount, Callback<CheckPayResult> callback);

    /**
     * callback for anti-addiction after successful consumption
     * @param amount
     */
    void paySuccess(long amount, Callback<SubmitPayResult> callback);

    void authIdentity(String token, String name, String idCard, String phoneNumber, Callback<IdentifyResult> callback);

    void fetchUserIdentifyInfo(String token, Callback<IdentificationInfo> callback);
}
