package com.tapsdk.antiaddiction.skynet.retrofit2.adapter.rxjava;

import com.tapsdk.antiaddiction.reactor.Observable;
import com.tapsdk.antiaddiction.reactor.Subscriber;
import com.tapsdk.antiaddiction.skynet.retrofit2.Call;
import com.tapsdk.antiaddiction.skynet.retrofit2.Response;
import com.tapsdk.antiaddiction.skynet.retrofit2.adapter.rxjava.CallArbiter;

final class CallExecuteOnSubscribe<T> implements Observable.OnSubscribe<Response<T>> {
    private final Call<T> originalCall;

    CallExecuteOnSubscribe(Call<T> originalCall) {
        this.originalCall = originalCall;
    }

    @Override public void call(Subscriber<? super Response<T>> subscriber) {
        // Since Call is a one-shot type, clone it for each new subscriber.
        Call<T> call = originalCall.clone();
        com.tapsdk.antiaddiction.skynet.retrofit2.adapter.rxjava.CallArbiter<T> arbiter = new CallArbiter<>(call, subscriber);
//        subscriber.add(arbiter);
        subscriber.setProducer(arbiter);

        Response<T> response;
        try {
            response = call.execute();
        } catch (Throwable t) {
//            Exceptions.throwIfFatal(t);
            arbiter.emitError(t);
            return;
        }
        arbiter.emitResponse(response);
    }
}
