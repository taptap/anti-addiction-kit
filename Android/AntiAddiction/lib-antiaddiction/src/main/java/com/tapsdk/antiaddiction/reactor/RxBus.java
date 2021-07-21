package com.tapsdk.antiaddiction.reactor;

import com.tapsdk.antiaddiction.reactor.subjects.PublishSubject;
import com.tapsdk.antiaddiction.reactor.subjects.SerializedSubject;
import com.tapsdk.antiaddiction.reactor.subjects.Subject;

public class RxBus {

    private RxBus() {
    }

    static class Holder {
        public static RxBus INSTANCE = new RxBus();
    }

    public static RxBus getInstance() {
        return Holder.INSTANCE;
    }

    private final Subject<Object, Object> bus = new SerializedSubject<>(PublishSubject.create());

    public void send(final Object event) {
        bus.onNext(event);
    }

    public Observable<Object> toObservable() {
        return bus;
    }

    public boolean hasObservers() {
        return bus.hasObservers();
    }
}
