package com.tapsdk.antiaddiction.skynet.retrofit2.adapter.rxjava;

import com.tapsdk.antiaddiction.reactor.Observable;
import com.tapsdk.antiaddiction.reactor.Subscriber;
import com.tapsdk.antiaddiction.skynet.retrofit2.Response;


final class BodyOnSubscribe<T> implements Observable.OnSubscribe<T> {
    private final Observable.OnSubscribe<Response<T>> upstream;

    BodyOnSubscribe(Observable.OnSubscribe<Response<T>> upstream) {
        this.upstream = upstream;
    }

    @Override public void call(Subscriber<? super T> subscriber) {
        upstream.call(new BodySubscriber<T>(subscriber));
    }

    private static class BodySubscriber<R> extends Subscriber<Response<R>> {
        private final Subscriber<? super R> subscriber;
        /** Indicates whether a terminal event has been sent to {@link #subscriber}. */
        private boolean subscriberTerminated;

        BodySubscriber(Subscriber<? super R> subscriber) {
            super(subscriber);
            this.subscriber = subscriber;
        }

        @Override public void onNext(Response<R> response) {
            if (response.isSuccessful()) {
                subscriber.onNext(response.body());
            } else {
                subscriberTerminated = true;
//                Throwable t = new HttpException(response);
                try {
                    subscriber.onError(new Exception(response.toString()));
                } catch (Exception e) {
                    // todo
                }
            }
        }

        @Override public void onError(Throwable throwable) {
            if (!subscriberTerminated) {
                subscriber.onError(throwable);
            } else {
                // This should never happen! onNext handles and forwards errors automatically.
//                Throwable broken = new AssertionError(
//                        "This should never happen! Report as a Retrofit bug with the full stacktrace.");
//                //noinspection UnnecessaryInitCause Two-arg AssertionError constructor is 1.7+ only.
//                broken.initCause(throwable);
//                RxJavaPlugins.getInstance().getErrorHandler().handleError(broken);
            }
        }

        @Override public void onCompleted() {
            if (!subscriberTerminated) {
                subscriber.onCompleted();
            }
        }
    }
}
