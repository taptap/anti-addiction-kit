package com.tapsdk.antiaddiction.reactor.subjects;


import com.tapsdk.antiaddiction.reactor.Subscriber;
import com.tapsdk.antiaddiction.reactor.observers.SerializedObserver;

public class SerializedSubject<T, R> extends Subject<T, R> {
    private final SerializedObserver<T> observer;
    private final Subject<T, R> actual;

    public SerializedSubject(final Subject<T, R> actual) {
        super(new OnSubscribe<R>() {
            @Override
            public void call(Subscriber<? super R> subscriber) {
                actual.unsafeSubscribe(subscriber);
            }
        });
        this.actual = actual;
        this.observer = new SerializedObserver<>(actual);
    }

    @Override
    public void onCompleted() {
        observer.onCompleted();
    }

    @Override
    public void onError(Throwable e) {
        observer.onError(e);
    }

    @Override
    public void onNext(T t) {
        observer.onNext(t);
    }

    @Override
    public boolean hasObservers() {
        return actual.hasObservers();
    }
}
