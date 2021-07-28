package com.tapsdk.antiaddiction.skynet.retrofit2.adapter.rxjava;

import com.tapsdk.antiaddiction.reactor.Observable;
import com.tapsdk.antiaddiction.reactor.Subscriber;
import com.tapsdk.antiaddiction.skynet.retrofit2.Response;

final class ResultOnSubscribe<T> implements Observable.OnSubscribe<com.tapsdk.antiaddiction.skynet.retrofit2.adapter.rxjava.Result<T>> {
    private final Observable.OnSubscribe<Response<T>> upstream;

    ResultOnSubscribe(Observable.OnSubscribe<Response<T>> upstream) {
        this.upstream = upstream;
    }

    @Override
    public void call(Subscriber<? super com.tapsdk.antiaddiction.skynet.retrofit2.adapter.rxjava.Result<T>> subscriber) {
        upstream.call(new ResultSubscriber<T>(subscriber));
    }

    private static class ResultSubscriber<R> extends Subscriber<Response<R>> {
        private final Subscriber<? super com.tapsdk.antiaddiction.skynet.retrofit2.adapter.rxjava.Result<R>> subscriber;

        ResultSubscriber(Subscriber<? super com.tapsdk.antiaddiction.skynet.retrofit2.adapter.rxjava.Result<R>> subscriber) {
            super(subscriber);
            this.subscriber = subscriber;
        }

        @Override
        public void onNext(Response<R> response) {
            subscriber.onNext(com.tapsdk.antiaddiction.skynet.retrofit2.adapter.rxjava.Result.response(response));
        }

        @Override
        public void onError(Throwable throwable) {
            try {
                subscriber.onNext(Result.<R>error(throwable));
            } catch (Throwable t) {
                try {
                    subscriber.onError(t);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
            subscriber.onCompleted();
        }

        @Override
        public void onCompleted() {
            subscriber.onCompleted();
        }
    }
}
