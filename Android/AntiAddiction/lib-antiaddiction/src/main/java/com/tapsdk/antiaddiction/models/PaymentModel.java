package com.tapsdk.antiaddiction.models;

import com.tapsdk.antiaddiction.entities.request.PayRequestParams;
import com.tapsdk.antiaddiction.entities.response.CheckPayResult;
import com.tapsdk.antiaddiction.entities.response.SubmitPayResult;
import com.tapsdk.antiaddiction.reactor.Observable;
import com.tapsdk.antiaddiction.rest.api.AntiAddictionApi;
import com.tapsdk.antiaddiction.skynet.Skynet;

public class PaymentModel {

    public Observable<CheckPayResult> checkPay(long amount, String game) {
        if (amount < 0) return Observable.error(new Throwable("The amount cannot be negative"));

        AntiAddictionApi api = Skynet.getService(Skynet.RETROFIT_FOR_ANTI_ADDICTION
                , AntiAddictionApi.class);
        PayRequestParams payRequestParams = new PayRequestParams(amount * 100, game);
        return api.checkPay(payRequestParams);
    }

    public Observable<SubmitPayResult> paySuccess(long amount, String game) {
        if (amount < 0) return Observable.error(new Throwable("The amount cannot be negative"));

        AntiAddictionApi api = Skynet.getService(Skynet.RETROFIT_FOR_ANTI_ADDICTION
                , AntiAddictionApi.class);
        PayRequestParams payRequestParams = new PayRequestParams(amount * 100, game);
        return api.submitPayResult(payRequestParams);
    }
}
