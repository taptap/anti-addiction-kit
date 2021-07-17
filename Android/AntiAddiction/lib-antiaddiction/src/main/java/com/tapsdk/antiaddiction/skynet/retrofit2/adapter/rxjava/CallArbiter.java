package com.tapsdk.antiaddiction.skynet.retrofit2.adapter.rxjava;

import com.tapsdk.antiaddiction.reactor.Producer;
import com.tapsdk.antiaddiction.reactor.Subscriber;
import com.tapsdk.antiaddiction.skynet.retrofit2.Call;
import com.tapsdk.antiaddiction.skynet.retrofit2.Response;

import java.util.concurrent.atomic.AtomicInteger;

public class CallArbiter<T> extends AtomicInteger implements Producer {
    private static final int STATE_WAITING = 0;
    private static final int STATE_REQUESTED = 1;
    private static final int STATE_HAS_RESPONSE = 2;
    private static final int STATE_TERMINATED = 3;

    private final Call<T> call;
    private final Subscriber<? super Response<T>> subscriber;

    private volatile boolean unsubscribed;
    private volatile Response<T> response;

    CallArbiter(Call<T> call, Subscriber<? super Response<T>> subscriber) {

        this.call = call;
        this.subscriber = subscriber;
    }


    void emitResponse(Response<T> response) {
        while (true) {
            int state = get();
            switch (state) {
                case STATE_WAITING:
                    this.response = response;
                    if (compareAndSet(STATE_WAITING, STATE_HAS_RESPONSE)) {
                        return;
                    }
                    break; // State transition failed. Try again.

                case STATE_REQUESTED:
                    if (compareAndSet(STATE_REQUESTED, STATE_TERMINATED)) {
                        deliverResponse(response);
                        return;
                    }
                    break; // State transition failed. Try again.

                case STATE_HAS_RESPONSE:
                case STATE_TERMINATED:
                    throw new AssertionError();

                default:
                    throw new IllegalStateException("Unknown state: " + state);
            }
        }
    }

    private void deliverResponse(Response<T> response) {
        try {
            subscriber.onNext(response);
        } catch (Exception e) {
            return;
        }
        try {
            subscriber.onCompleted();
        } catch (Exception t) {
            // todo
        }
    }

    void emitError(Throwable t) {
        set(STATE_TERMINATED);

        try {
            subscriber.onError(t);
        } catch (Exception e) {
            // todo
        }
    }

    @Override
    public void request(long amount) {
        if (amount == 0) {
            return;
        }
        while (true) {
            int state = get();
            switch (state) {
                case STATE_WAITING:
                    if (compareAndSet(STATE_WAITING, STATE_REQUESTED)) {
                        return;
                    }
                    break; // State transition failed. Try again.

                case STATE_HAS_RESPONSE:
                    if (compareAndSet(STATE_HAS_RESPONSE, STATE_TERMINATED)) {
                        deliverResponse(response);
                        return;
                    }
                    break; // State transition failed. Try again.

                case STATE_REQUESTED:
                case STATE_TERMINATED:
                    return; // Nothing to do.

                default:
                    throw new IllegalStateException("Unknown state: " + state);
            }
        }
    }
}
