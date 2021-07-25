/**
 * Copyright (c) 2016-present, RxJava Contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package com.tapsdk.antiaddiction.reactor;
import android.util.Log;

import com.tapsdk.antiaddiction.reactor.exceptions.Exceptions;
import com.tapsdk.antiaddiction.reactor.exceptions.OnErrorFailedException;
import com.tapsdk.antiaddiction.reactor.functions.Action0;
import com.tapsdk.antiaddiction.reactor.functions.Action1;
import com.tapsdk.antiaddiction.reactor.functions.Actions;
import com.tapsdk.antiaddiction.reactor.functions.Func1;
import com.tapsdk.antiaddiction.reactor.internal.operators.EmptyObservableHolder;
import com.tapsdk.antiaddiction.reactor.internal.operators.OnSubscribeLift;
import com.tapsdk.antiaddiction.reactor.internal.operators.OnSubscribeMap;
import com.tapsdk.antiaddiction.reactor.internal.operators.OnSubscribeThrow;
import com.tapsdk.antiaddiction.reactor.internal.operators.OperatorMerge;
import com.tapsdk.antiaddiction.reactor.internal.operators.OperatorObserveOn;
import com.tapsdk.antiaddiction.reactor.internal.operators.OperatorSubscribeOn;
import com.tapsdk.antiaddiction.reactor.observers.SafeSubscriber;
import com.tapsdk.antiaddiction.reactor.operators.OnSubscribeCreate;
import com.tapsdk.antiaddiction.reactor.plugins.RxJavaHooks;
import com.tapsdk.antiaddiction.reactor.schedulers.Scheduler;
import com.tapsdk.antiaddiction.reactor.subscriptions.Subscriptions;
import com.tapsdk.antiaddiction.reactor.util.ActionSubscriber;
import com.tapsdk.antiaddiction.reactor.util.InternalObservableUtils;

public class Observable<T> {

    final OnSubscribe<T> onSubscribe;

    /**
     * Invoked when Observable.subscribe is called.
     *
     * @param <T> the output value type
     */
    public interface OnSubscribe<T> extends Action1<Subscriber<? super T>> {
        // cover for generics insanity
    }

    public interface Operator<R, T> extends Func1<Subscriber<? super R>, Subscriber<? super T>> {
        // cover for generics insanity
    }

    public Observable(OnSubscribe<T> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }

    public static <T> Observable<T> create(OnSubscribe<T> onSubscribe) {
        return new Observable<>(onSubscribe);
    }

    public final Subscription subscribe(Subscriber<? super T> subscriber) {
        return Observable.subscribe(subscriber, this);
    }

    static <T> Subscription subscribe(Subscriber<? super T> subscriber, Observable<T> observable) {
        // validate and proceed
        if (subscriber == null) {
            throw new IllegalArgumentException("subscriber can not be null");
        }
        if (observable.onSubscribe == null) {
            throw new IllegalStateException("onSubscribe function can not be null.");
            /*
             * the subscribe function can also be overridden but generally that's not the appropriate approach
             * so I won't mention that in the exception
             */
        }

        // new Subscriber so onStart it
        subscriber.onStart();

        /*
         * See https://github.com/ReactiveX/RxJava/issues/216 for discussion on "Guideline 6.4: Protect calls
         * to user code from within an Observer"
         */
        // if not already wrapped
        if (!(subscriber instanceof SafeSubscriber)) {
            // assign to `observer` so we return the protected version
            subscriber = new SafeSubscriber<T>(subscriber);
        }

        // The code below is exactly the same an unsafeSubscribe but not used because it would
        // add a significant depth to already huge call stacks.
        try {
            // allow the hook to intercept and/or decorate
            RxJavaHooks.onObservableStart(observable, observable.onSubscribe).call(subscriber);
            return RxJavaHooks.onObservableReturn(subscriber);
        } catch (Throwable e) {
            // special handling for certain Throwable/Error/Exception types
            Exceptions.throwIfFatal(e);
            // in case the subscriber can't listen to exceptions anymore
            if (subscriber.isUnsubscribed()) {
                RxJavaHooks.onError(RxJavaHooks.onObservableError(e));
            } else {
                // if an unhandled error occurs executing the onSubscribe we will propagate it
                try {
                    subscriber.onError(RxJavaHooks.onObservableError(e));
                } catch (Throwable e2) {
                    Exceptions.throwIfFatal(e2);
                    // if this happens it means the onError itself failed (perhaps an invalid function implementation)
                    // so we are unable to propagate the error correctly and will just throw
                    RuntimeException r = new OnErrorFailedException("Error occurred attempting to subscribe [" + e.getMessage() + "] and then again while trying to pass to onError.", e2);
                    // TODO could the hook be the cause of the error in the on error handling.
                    RxJavaHooks.onObservableError(r);
                    // TODO why aren't we throwing the hook's return value.
                    throw r; // NOPMD
                }
            }
            return Subscriptions.unsubscribed();
        }
    }

    public final void subscribe() {
        Action1<T> onNext = Actions.empty();
        Action1<Throwable> onError = InternalObservableUtils.ERROR_NOT_IMPLEMENTED;
        Action0 onCompleted = Actions.empty();
        this.subscribe(new ActionSubscriber(onNext, onError, onCompleted));
    }

    public final void subscribe(final Action1<? super T> onNext) {
        if (onNext == null) {
            throw new IllegalArgumentException("onNext can not be null");
        }

        Action1<Throwable> onError = InternalObservableUtils.ERROR_NOT_IMPLEMENTED;
        Action0 onCompleted = Actions.empty();
        subscribe(new ActionSubscriber<T>(onNext, onError, onCompleted));
    }

    public final void subscribe(final Action1<? super T> onNext, final Action1<Throwable> onError) {
        if (onNext == null) {
            throw new IllegalArgumentException("onNext can not be null");
        }
        if (onError == null) {
            throw new IllegalArgumentException("onError can not be null");
        }

        Action0 onCompleted = Actions.empty();
        subscribe(new ActionSubscriber<T>(onNext, onError, onCompleted));
    }

    public final void subscribe(final Action1<? super T> onNext, final Action1<Throwable> onError, final Action0 onCompleted) {
        if (onNext == null) {
            throw new IllegalArgumentException("onNext can not be null");
        }
        if (onError == null) {
            throw new IllegalArgumentException("onError can not be null");
        }
        if (onCompleted == null) {
            throw new IllegalArgumentException("onComplete can not be null");
        }

        subscribe(new ActionSubscriber<T>(onNext, onError, onCompleted));
    }

    /**
     * Asynchronously subscribes Observers to this Observable on the specified {@link Scheduler}.
     * <p>
     * If there is a type source up in the
     * chain, it is recommended to use {@code subscribeOn(scheduler, false)} instead
     * to avoid same-pool deadlock because requests pile up behind a eager/blocking emitter.
     * <p>
     * <img width="640" height="305" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/subscribeOn.png" alt="">
     * <dl>
     *  <dt><b>Backpressure:</b></dt>
     *  <dd>The operator doesn't interfere with backpressure amount which is determined by the source {@code Observable}'s backpressure
     *  behavior. However, the upstream is requested from the given scheduler thread.</dd>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>you specify which {@link Scheduler} this operator will use</dd>
     * </dl>
     *
     * @param scheduler the {@link Scheduler} to perform subscription actions on
     * @return the source Observable modified so that its subscriptions happen on the
     * specified {@link Scheduler}
     * @see <a href="http://reactivex.io/documentation/operators/subscribeon.html">ReactiveX operators documentation: SubscribeOn</a>
     * @see <a href="http://www.grahamlea.com/2014/07/rxjava-threading-examples/">RxJava Threading Examples</a>
     * @see #observeOn
     */
    public final Observable<T> subscribeOn(final Scheduler scheduler) {
        Log.d("test", "subscribeOn 111");
        return subscribeOn(scheduler, !(this.onSubscribe instanceof OnSubscribeCreate));
    }

    public final Observable<T> subscribeOn(Scheduler scheduler, boolean requestOn) {
        Log.d("test", "subscribeOn 222");
        return unsafeCreate(new OperatorSubscribeOn<T>(this, scheduler, requestOn));
    }

    public static <T> Observable<T> unsafeCreate(OnSubscribe<T> f) {
        return new Observable<T>(RxJavaHooks.onCreate(f));
    }

    public final Subscription unsafeSubscribe(Subscriber<? super T> subscriber) {
        try {
            // new Subscriber so onStart it
            subscriber.onStart();
            // allow the hook to intercept and/or decorate
            RxJavaHooks.onObservableStart(this, onSubscribe).call(subscriber);
            return RxJavaHooks.onObservableReturn(subscriber);
        } catch (Throwable e) {
            // special handling for certain Throwable/Error/Exception types
            Exceptions.throwIfFatal(e);
            // if an unhandled error occurs executing the onSubscribe we will propagate it
            try {
                subscriber.onError(RxJavaHooks.onObservableError(e));
            } catch (Throwable e2) {
                Exceptions.throwIfFatal(e2);
                // if this happens it means the onError itself failed (perhaps an invalid function implementation)
                // so we are unable to propagate the error correctly and will just throw
                RuntimeException r = new OnErrorFailedException("Error occurred attempting to subscribe [" + e.getMessage() + "] and then again while trying to pass to onError.", e2);
                // TODO could the hook be the cause of the error in the on error handling.
                RxJavaHooks.onObservableError(r);
                // TODO why aren't we throwing the hook's return value.
                throw r; // NOPMD
            }
            return Subscriptions.unsubscribed();
        }
    }
    /**
     * Modifies an Observable to perform its emissions and notifications on a specified {@link Scheduler},
     * asynchronously with a bounded buffer of slots.
     *
     * <p>Note that onError notifications will cut ahead of onNext notifications on the emission thread if Scheduler is truly
     * asynchronous. If strict event ordering is required, consider using the observeOn(Scheduler, boolean) overload.
     * <p>
     * <img width="640" height="308" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/observeOn.png" alt="">
     * <dl>
     *  <dt><b>Backpressure:</b></dt>
     *  <dd>This operator honors backpressure from downstream and expects it from the source {@code Observable}. Violating this
     *  expectation will lead to {@code MissingBackpressureException}. This is the most common operator where the exception
     *  pops up; look for sources up the chain that don't support backpressure,
     *  such as {@code interval}, {@code timer}, {code PublishSubject} or {@code BehaviorSubject} and apply any
     *  of the {@code onBackpressureXXX} operators <strong>before</strong> applying {@code observeOn} itself.</dd>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>you specify which {@link Scheduler} this operator will use</dd>
     * </dl>
     *
     * @param scheduler
     *            the {@link Scheduler} to notify {@link Observer}s on
     * @return the source Observable modified so that its {@link Observer}s are notified on the specified
     *         {@link Scheduler}
     * @see <a href="http://reactivex.io/documentation/operators/observeon.html">ReactiveX operators documentation: ObserveOn</a>
     * @see <a href="http://www.grahamlea.com/2014/07/rxjava-threading-examples/">RxJava Threading Examples</a>
     * @see #subscribeOn
     */
    public Observable<T> observeOn(final Scheduler scheduler) {
        return observeOn(scheduler, 128);
    }

    public final Observable<T> observeOn(Scheduler scheduler, int bufferSize) {
        return observeOn(scheduler, false, bufferSize);
    }

    public final Observable<T> observeOn(Scheduler scheduler, boolean delayError, int bufferSize) {
        return lift(new OperatorObserveOn<T>(scheduler, delayError, bufferSize));
    }

    public final <R> Observable<R> lift(final Operator<? extends R, ? super T> operator) {
        return unsafeCreate(new OnSubscribeLift<T, R>(onSubscribe, operator));
    }

    public static <T> Observable<T> error(Throwable exception) {
        return unsafeCreate(new OnSubscribeThrow<T>(exception));
    }

    public final <R> Observable<R> flatMap(Func1<? super T, ? extends Observable<? extends R>> func) {

        return merge(map(func));
    }

    public final <R> Observable<R> map(Func1<? super T, ? extends R> func) {
        return unsafeCreate(new OnSubscribeMap<T, R>(this, func));
    }

    public static <T> Observable<T> merge(Observable<? extends Observable<? extends T>> source) {

        return source.lift(OperatorMerge.<T>instance(false));
    }

    public static <T> Observable<T> empty() {
        return EmptyObservableHolder.instance();
    }

}
