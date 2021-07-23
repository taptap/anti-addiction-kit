package com.tapsdk.antiaddiction.skynet.retrofit2.adapter.rxjava;

import com.tapsdk.antiaddiction.reactor.Observable;
import com.tapsdk.antiaddiction.reactor.Subscriber;
import com.tapsdk.antiaddiction.skynet.retrofit2.Call;
import com.tapsdk.antiaddiction.skynet.retrofit2.Callback;
import com.tapsdk.antiaddiction.skynet.retrofit2.Response;
import com.tapsdk.antiaddiction.skynet.retrofit2.adapter.rxjava.CallArbiter;

public class CallEnqueueOnSubscribe<T> implements Observable.OnSubscribe<Response<T>> {
    private final Call<T> originalCall;

    CallEnqueueOnSubscribe(Call<T> originalCall) {
        this.originalCall = originalCall;
    }

    @Override public void call(Subscriber<? super Response<T>> subscriber) {
        // Since Call is a one-shot type, clone it for each new subscriber.
        Call<T> call = originalCall.clone();
        final com.tapsdk.antiaddiction.skynet.retrofit2.adapter.rxjava.CallArbiter<T> arbiter = new CallArbiter<>(call, subscriber);
//        subscriber.add(arbiter);
        subscriber.setProducer(arbiter);

        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                arbiter.emitResponse(response);
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
//                Exceptions.throwIfFatal(t);
                arbiter.emitError(t);
            }
        });
    }
}
