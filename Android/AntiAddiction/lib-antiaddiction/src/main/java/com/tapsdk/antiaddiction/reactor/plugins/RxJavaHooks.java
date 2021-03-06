/**
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tapsdk.antiaddiction.reactor.plugins;

import com.tapsdk.antiaddiction.reactor.Observable;
import com.tapsdk.antiaddiction.reactor.Subscription;
import com.tapsdk.antiaddiction.reactor.functions.Action0;
import com.tapsdk.antiaddiction.reactor.functions.Action1;
import com.tapsdk.antiaddiction.reactor.functions.Func0;
import com.tapsdk.antiaddiction.reactor.functions.Func1;
import com.tapsdk.antiaddiction.reactor.functions.Func2;
import com.tapsdk.antiaddiction.reactor.operators.OnSubscribeOnAssembly;
import com.tapsdk.antiaddiction.reactor.schedulers.Scheduler;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Utility class that holds hooks for various Observable, Single and Completable lifecycle-related
 * points as well as Scheduler hooks.
 * <p>
 * The class features a lockdown state, see {@link #lockdown()} and {@link #isLockdown()}, to
 * prevent further changes to the hooks.
 * @since 1.3
 */
public final class RxJavaHooks {
    /**
     * Prevents changing the hook callbacks when set to true.
     */
    /* test */ static volatile boolean lockdown;

    static volatile Action1<Throwable> onError;

    @SuppressWarnings("rawtypes")
    static volatile Func1<Observable.Operator, Observable.Operator> onObservableLift;

    @SuppressWarnings("rawtypes")
    static volatile Func1<Observable.OnSubscribe, Observable.OnSubscribe> onObservableCreate;

    @SuppressWarnings("rawtypes")
    static volatile Func2<Observable, Observable.OnSubscribe, Observable.OnSubscribe> onObservableStart;

    static volatile Func1<Scheduler, Scheduler> onComputationScheduler;

    static volatile Func1<Scheduler, Scheduler> onIOScheduler;

    static volatile Func1<Scheduler, Scheduler> onNewThreadScheduler;

    static volatile Func1<Action0, Action0> onScheduleAction;

    static volatile Func1<Subscription, Subscription> onObservableReturn;

    static volatile Func1<Subscription, Subscription> onSingleReturn;

    static volatile Func0<? extends ScheduledExecutorService> onGenericScheduledExecutorService;

    static volatile Func1<Throwable, Throwable> onObservableSubscribeError;

    static volatile Func1<Throwable, Throwable> onSingleSubscribeError;

    static volatile Func1<Throwable, Throwable> onCompletableSubscribeError;

    /** Initialize with the default delegation to the original RxJavaPlugins. */
    static {
        init();
    }

    /** Utility class. */
    private RxJavaHooks() {
        throw new IllegalStateException("No instances!");
    }


    /**
     * Initialize the hooks via delegating to RxJavaPlugins.
     */
    @SuppressWarnings({ "rawtypes", "unchecked", "deprecation"})
    static void init() {
        onError = new Action1<Throwable>() {
            @Override
            public void call(Throwable e) {
                RxJavaPlugins.getInstance().getErrorHandler().handleError(e);
            }
        };

        onObservableStart = new Func2<Observable, Observable.OnSubscribe, Observable.OnSubscribe>() {
            @Override
            public Observable.OnSubscribe call(Observable t1, Observable.OnSubscribe t2) {
                return RxJavaPlugins.getInstance().getObservableExecutionHook().onSubscribeStart(t1, t2);
            }
        };

        onObservableReturn = new Func1<Subscription, Subscription>() {
            @Override
            public Subscription call(Subscription f) {
                return RxJavaPlugins.getInstance().getObservableExecutionHook().onSubscribeReturn(f);
            }
        };

        onScheduleAction = new Func1<Action0, Action0>() {
            @Override
            public Action0 call(Action0 a) {
                return RxJavaPlugins.getInstance().getSchedulersHook().onSchedule(a);
            }
        };

        onObservableSubscribeError = new Func1<Throwable, Throwable>() {
            @Override
            public Throwable call(Throwable t) {
                return RxJavaPlugins.getInstance().getObservableExecutionHook().onSubscribeError(t);
            }
        };

        onObservableLift = new Func1<Observable.Operator, Observable.Operator>() {
            @Override
            public Observable.Operator call(Observable.Operator t) {
                return RxJavaPlugins.getInstance().getObservableExecutionHook().onLift(t);
            }
        };


        initCreate();
    }

    @SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
    static void initCreate() {
        onObservableCreate = new Func1<Observable.OnSubscribe, Observable.OnSubscribe>() {
            @Override
            public Observable.OnSubscribe call(Observable.OnSubscribe f) {
                return RxJavaPlugins.getInstance().getObservableExecutionHook().onCreate(f);
            }
        };
    }

    /**
     * Reset all hook callbacks to those of the current RxJavaPlugins handlers.
     *
     * @see #clear()
     */
    public static void reset() {
        if (lockdown) {
            return;
        }
        init();

        onComputationScheduler = null;
        onIOScheduler = null;
        onNewThreadScheduler = null;
        onGenericScheduledExecutorService = null;
    }

    /**
     * Hook to call before the child subscriber would subscribe to an Operator.
     * @param <T> the input value type
     * @param <R> the output value type
     * @param operator the original operator
     * @return the original or alternative operator that will be subscribed to
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T, R> Observable.Operator<R, T> onObservableLift(Observable.Operator<R, T> operator) {
        Func1<Observable.Operator, Observable.Operator> f = onObservableLift;
        if (f != null) {
            return f.call(operator);
        }
        return operator;
    }

    /**
     * Clears all hooks to be no-op (and pass-through)
     * and onError hook to signal errors to the caller thread's
     * UncaughtExceptionHandler.
     *
     * @see #reset()
     */
    public static void clear() {
        if (lockdown) {
            return;
        }
        onError = null;

        onObservableCreate = null;
        onObservableStart = null;
        onObservableReturn = null;
        onObservableSubscribeError = null;

        onComputationScheduler = null;
        onIOScheduler = null;
        onNewThreadScheduler = null;

        onScheduleAction = null;
        onGenericScheduledExecutorService = null;
    }

    /**
     * Prevents changing the hooks.
     */
    public static void lockdown() {
        lockdown = true;
    }

    /**
     * Returns true if the hooks can no longer be changed.
     * @return true if the hooks can no longer be changed
     */
    public static boolean isLockdown() {
        return lockdown;
    }
    /**
     * Consume undeliverable Throwables (acts as a global catch).
     * @param ex the exception to handle
     */
    public static void onError(Throwable ex) {
        Action1<Throwable> f = onError;
        if (f != null) {
            try {
                f.call(ex);
                return;
            } catch (Throwable pluginException) {
                /*
                 * We don't want errors from the plugin to affect normal flow.
                 * Since the plugin should never throw this is a safety net
                 * and will complain loudly to System.err so it gets fixed.
                 */
                System.err.println("The onError handler threw an Exception. It shouldn't. => " + pluginException.getMessage()); // NOPMD
                pluginException.printStackTrace(); // NOPMD

                signalUncaught(pluginException);
            }
        }
        signalUncaught(ex);
    }

    static void signalUncaught(Throwable ex) {
        Thread current = Thread.currentThread();
        UncaughtExceptionHandler handler = current.getUncaughtExceptionHandler();
        handler.uncaughtException(current, ex);
    }

    /**
     * Hook to call when an Observable is created.
     * @param <T> the value type
     * @param onSubscribe the original OnSubscribe logic
     * @return the original or replacement OnSubscribe instance
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> Observable.OnSubscribe<T> onCreate(Observable.OnSubscribe<T> onSubscribe) {
        Func1<Observable.OnSubscribe, Observable.OnSubscribe> f = onObservableCreate;
        if (f != null) {
            return f.call(onSubscribe);
        }
        return onSubscribe;
    }

    /**
     * Hook to call when the Schedulers.computation() is called.
     * @param scheduler the default computation scheduler
     * @return the default of alternative scheduler
     */
    public static Scheduler onComputationScheduler(Scheduler scheduler) {
        Func1<Scheduler, Scheduler> f = onComputationScheduler;
        if (f != null) {
            return f.call(scheduler);
        }
        return scheduler;
    }

    /**
     * Hook to call when the Schedulers.io() is called.
     * @param scheduler the default io scheduler
     * @return the default of alternative scheduler
     */
    public static Scheduler onIOScheduler(Scheduler scheduler) {
        Func1<Scheduler, Scheduler> f = onIOScheduler;
        if (f != null) {
            return f.call(scheduler);
        }
        return scheduler;
    }

    /**
     * Hook to call when the Schedulers.newThread() is called.
     * @param scheduler the default new thread scheduler
     * @return the default of alternative scheduler
     */
    public static Scheduler onNewThreadScheduler(Scheduler scheduler) {
        Func1<Scheduler, Scheduler> f = onNewThreadScheduler;
        if (f != null) {
            return f.call(scheduler);
        }
        return scheduler;
    }

    /**
     * Hook to call before the action is scheduled, allows
     * decorating the original action.
     * @param action the original action
     * @return the original or alternative action
     */
    public static Action0 onScheduledAction(Action0 action) {
        Func1<Action0, Action0> f = onScheduleAction;
        if (f != null) {
            return f.call(action);
        }
        return action;
    }

    /**
     * Hook to call before the child subscriber is subscribed to the OnSubscribe action.
     * @param <T> the value type
     * @param instance the parent Observable instance
     * @param onSubscribe the original OnSubscribe action
     * @return the original or alternative action that will be subscribed to
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> Observable.OnSubscribe<T> onObservableStart(Observable<T> instance, Observable.OnSubscribe<T> onSubscribe) {
        Func2<Observable, Observable.OnSubscribe, Observable.OnSubscribe> f = onObservableStart;
        if (f != null) {
            return f.call(instance, onSubscribe);
        }
        return onSubscribe;
    }

    /**
     * Hook to call before the Observable.subscribe() method is about to return a Subscription.
     * @param subscription the original subscription
     * @return the original or alternative subscription that will be returned
     */
    public static Subscription onObservableReturn(Subscription subscription) {
        Func1<Subscription, Subscription> f = onObservableReturn;
        if (f != null) {
            return f.call(subscription);
        }
        return subscription;
    }

    /**
     * Hook to call if the Observable.subscribe() crashes for some reason.
     * @param error the error
     * @return the original error or alternative Throwable to be thrown
     */
    public static Throwable onObservableError(Throwable error) {
        Func1<Throwable, Throwable> f = onObservableSubscribeError;
        if (f != null) {
            return f.call(error);
        }
        return error;
    }

    /**
     * Sets the global error consumer action unless a lockdown is in effect.
     * <p>
     * This operation is thread-safe.
     * <p>
     * Calling with a {@code null} parameter has the effect that
     * errors are routed to the current thread's {@link UncaughtExceptionHandler}.
     * @param onError the action that will receive undeliverable Throwables
     */
    public static void setOnError(Action1<Throwable> onError) {
        if (lockdown) {
            return;
        }
        RxJavaHooks.onError = onError;
    }

    /**
     * Sets the Observable onCreate hook function unless a lockdown is in effect.
     * <p>
     * This operation is thread-safe.
     * <p>
     * Calling with a {@code null} parameter restores the default behavior:
     * the hook returns the same object.
     * @param onObservableCreate the function that takes the original OnSubscribe
     * and should return a OnSubscribe.
     */
    @SuppressWarnings("rawtypes")
    public static void setOnObservableCreate(
            Func1<Observable.OnSubscribe, Observable.OnSubscribe> onObservableCreate) {
        if (lockdown) {
            return;
        }
        RxJavaHooks.onObservableCreate = onObservableCreate;
    }

    /**
     * Sets the hook function for returning a scheduler when the Schedulers.computation() is called
     * unless a lockdown is in effect.
     * <p>
     * This operation is thread-safe.
     * <p>
     * Calling with a {@code null} parameter restores the default behavior:
     * the hook returns the same object.
     * @param onComputationScheduler the function that receives the original computation scheduler
     * and should return a scheduler.
     */
    public static void setOnComputationScheduler(Func1<Scheduler, Scheduler> onComputationScheduler) {
        if (lockdown) {
            return;
        }
        RxJavaHooks.onComputationScheduler = onComputationScheduler;
    }

    /**
     * Sets the hook function for returning a scheduler when the Schedulers.io() is called
     * unless a lockdown is in effect.
     * <p>
     * This operation is thread-safe.
     * <p>
     * Calling with a {@code null} parameter restores the default behavior:
     * the hook returns the same object.
     * @param onIOScheduler the function that receives the original io scheduler
     * and should return a scheduler.
     */
    public static void setOnIOScheduler(Func1<Scheduler, Scheduler> onIOScheduler) {
        if (lockdown) {
            return;
        }
        RxJavaHooks.onIOScheduler = onIOScheduler;
    }

    /**
     * Sets the hook function for returning a scheduler when the Schedulers.newThread() is called
     * unless a lockdown is in effect.
     * <p>
     * This operation is thread-safe.
     * <p>
     * Calling with a {@code null} parameter restores the default behavior:
     * the hook returns the same object.
     * @param onNewThreadScheduler the function that receives the original new thread scheduler
     * and should return a scheduler.
     */
    public static void setOnNewThreadScheduler(Func1<Scheduler, Scheduler> onNewThreadScheduler) {
        if (lockdown) {
            return;
        }
        RxJavaHooks.onNewThreadScheduler = onNewThreadScheduler;
    }

    /**
     * Sets the hook function that is called before an action is scheduled, allowing
     * decorating that function, unless a lockdown is in effect.
     * <p>
     * This operation is thread-safe.
     * <p>
     * Calling with a {@code null} parameter restores the default behavior:
     * the hook returns the same object.
     * @param onScheduleAction the function that receives the original action and should
     * return an Action0.
     */
    public static void setOnScheduleAction(Func1<Action0, Action0> onScheduleAction) {
        if (lockdown) {
            return;
        }
        RxJavaHooks.onScheduleAction = onScheduleAction;
    }

    /**
     * Sets the hook function that is called when a subscriber subscribes to a Observable
     * unless a lockdown is in effect.
     * <p>
     * This operation is thread-safe.
     * <p>
     * Calling with a {@code null} parameter restores the default behavior:
     * the hook returns the same OnSubscribe object.
     * @param onObservableStart the function that is called with the current Observable instance,
     * its OnSubscribe function and should return a OnSubscribe function
     * that gets actually subscribed to.
     */
    @SuppressWarnings("rawtypes")
    public static void setOnObservableStart(
            Func2<Observable, Observable.OnSubscribe, Observable.OnSubscribe> onObservableStart) {
        if (lockdown) {
            return;
        }
        RxJavaHooks.onObservableStart = onObservableStart;
    }

    /**
     * Sets a hook function that is called when the Observable.subscribe() call
     * is about to return a Subscription unless a lockdown is in effect.
     * <p>
     * This operation is thread-safe.
     * <p>
     * Calling with a {@code null} parameter restores the default behavior:
     * the hook returns the same object.
     * @param onObservableReturn the function that is called with the Subscriber that has been
     * subscribed to the OnSubscribe function and returns a Subscription that will be returned by
     * subscribe().
     */
    public static void setOnObservableReturn(Func1<Subscription, Subscription> onObservableReturn) {
        if (lockdown) {
            return;
        }
        RxJavaHooks.onObservableReturn = onObservableReturn;
    }

    /**
     * Sets a hook function that is called when the Single.subscribe() call
     * is about to return a Subscription unless a lockdown is in effect.
     * <p>
     * This operation is thread-safe.
     * <p>
     * Calling with a {@code null} parameter restores the default behavior:
     * the hook returns the same object.
     * @param onSingleReturn the function that is called with the SingleSubscriber that has been
     * subscribed to the OnSubscribe function and returns a Subscription that will be returned by
     * subscribe().
     */
    public static void setOnSingleReturn(Func1<Subscription, Subscription> onSingleReturn) {
        if (lockdown) {
            return;
        }
        RxJavaHooks.onSingleReturn = onSingleReturn;
    }

    /**
     * Sets a hook function that is called when the Single.subscribe() call
     * fails with an exception.
     * <p>
     * This operation is thread-safe.
     * <p>
     * Calling with a {@code null} parameter restores the default behavior:
     * the hook returns the same object.
     * @param onSingleSubscribeError the function that is called with the crash exception and should return
     * an exception.
     */
    public static void setOnSingleSubscribeError(Func1<Throwable, Throwable> onSingleSubscribeError) {
        if (lockdown) {
            return;
        }
        RxJavaHooks.onSingleSubscribeError = onSingleSubscribeError;
    }

    /**
     * Returns the current Single onSubscribeError hook function or null if it is
     * set to the default pass-through.
     * <p>
     * This operation is thread-safe.
     * @return the current hook function
     */
    public static Func1<Throwable, Throwable> getOnSingleSubscribeError() {
        return onSingleSubscribeError;
    }

    /**
     * Sets a hook function that is called when the Completable.subscribe() call
     * fails with an exception.
     * <p>
     * This operation is thread-safe.
     * <p>
     * Calling with a {@code null} parameter restores the default behavior:
     * the hook returns the same object.
     * @param onCompletableSubscribeError the function that is called with the crash exception and should return
     * an exception.
     */
    public static void setOnCompletableSubscribeError(Func1<Throwable, Throwable> onCompletableSubscribeError) {
        if (lockdown) {
            return;
        }
        RxJavaHooks.onCompletableSubscribeError = onCompletableSubscribeError;
    }

    /**
     * Returns the current Completable onSubscribeError hook function or null if it is
     * set to the default pass-through.
     * <p>
     * This operation is thread-safe.
     * @return the current hook function
     */
    public static Func1<Throwable, Throwable> getOnCompletableSubscribeError() {
        return onCompletableSubscribeError;
    }

    /**
     * Sets a hook function that is called when the Observable.subscribe() call
     * fails with an exception.
     * <p>
     * This operation is thread-safe.
     * <p>
     * Calling with a {@code null} parameter restores the default behavior:
     * the hook returns the same object.
     * @param onObservableSubscribeError the function that is called with the crash exception and should return
     * an exception.
     */
    public static void setOnObservableSubscribeError(Func1<Throwable, Throwable> onObservableSubscribeError) {
        if (lockdown) {
            return;
        }
        RxJavaHooks.onObservableSubscribeError = onObservableSubscribeError;
    }

    /**
     * Returns the current Observable onSubscribeError hook function or null if it is
     * set to the default pass-through.
     * <p>
     * This operation is thread-safe.
     * @return the current hook function
     */
    public static Func1<Throwable, Throwable> getOnObservableSubscribeError() {
        return onObservableSubscribeError;
    }

    /**
     * Returns the current computation scheduler hook function or null if it is
     * set to the default pass-through.
     * <p>
     * This operation is thread-safe.
     * @return the current hook function
     */
    public static Func1<Scheduler, Scheduler> getOnComputationScheduler() {
        return onComputationScheduler;
    }

    /**
     * Returns the current global error handler hook action or null if it is
     * set to the default one that signals errors to the current threads
     * UncaughtExceptionHandler.
     * <p>
     * This operation is thread-safe.
     * @return the current hook action
     */
    public static Action1<Throwable> getOnError() {
        return onError;
    }

    /**
     * Returns the current io scheduler hook function or null if it is
     * set to the default pass-through.
     * <p>
     * This operation is thread-safe.
     * @return the current hook function
     */
    public static Func1<Scheduler, Scheduler> getOnIOScheduler() {
        return onIOScheduler;
    }

    /**
     * Returns the current new thread scheduler hook function or null if it is
     * set to the default pass-through.
     * <p>
     * This operation is thread-safe.
     * @return the current hook function
     */
    public static Func1<Scheduler, Scheduler> getOnNewThreadScheduler() {
        return onNewThreadScheduler;
    }

    /**
     * Returns the current Observable onCreate hook function or null if it is
     * set to the default pass-through.
     * <p>
     * This operation is thread-safe.
     * @return the current hook function
     */
    @SuppressWarnings("rawtypes")
    public static Func1<Observable.OnSubscribe, Observable.OnSubscribe> getOnObservableCreate() {
        return onObservableCreate;
    }

    /**
     * Returns the current schedule action hook function or null if it is
     * set to the default pass-through.
     * <p>
     * This operation is thread-safe.
     * @return the current hook function
     */
    public static Func1<Action0, Action0> getOnScheduleAction() {
        return onScheduleAction;
    }

    /**
     * Returns the current Observable onStart hook function or null if it is
     * set to the default pass-through.
     * <p>
     * This operation is thread-safe.
     * @return the current hook function
     */
    @SuppressWarnings("rawtypes")
    public static Func2<Observable, Observable.OnSubscribe, Observable.OnSubscribe> getOnObservableStart() {
        return onObservableStart;
    }

    /**
     * Returns the current Observable onReturn hook function or null if it is
     * set to the default pass-through.
     * <p>
     * This operation is thread-safe.
     * @return the current hook function
     */
    public static Func1<Subscription, Subscription> getOnObservableReturn() {
        return onObservableReturn;
    }

    /**
     * Returns the current Single onReturn hook function or null if it is
     * set to the default pass-through.
     * <p>
     * This operation is thread-safe.
     * @return the current hook function
     */
    public static Func1<Subscription, Subscription> getOnSingleReturn() {
        return onSingleReturn;
    }

    /**
     * Resets the assembly tracking hooks to their default delegates to
     * RxJavaPlugins.
     */
    public static void resetAssemblyTracking() {
        if (lockdown) {
            return;
        }

        initCreate();
    }

    /**
     * Clears the assembly tracking hooks to their default pass-through behavior.
     */
    public static void clearAssemblyTracking() {
        if (lockdown) {
            return;
        }
        onObservableCreate = null;
    }

    /**
     * Sets up hooks that capture the current stacktrace when a source or an
     * operator is instantiated, keeping it in a field for debugging purposes
     * and alters exceptions passing along to hold onto this stacktrace.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void enableAssemblyTracking() {
        if (lockdown) {
            return;
        }

        onObservableCreate = new Func1<Observable.OnSubscribe, Observable.OnSubscribe>() {
            @Override
            public Observable.OnSubscribe call(Observable.OnSubscribe f) {
                return new OnSubscribeOnAssembly(f);
            }
        };
    }
    /**
     * Sets the hook function for returning a ScheduledExecutorService used
     * by the GenericScheduledExecutorService for background tasks.
     * <p>
     * This operation is thread-safe.
     * <p>
     * Calling with a {@code null} parameter restores the default behavior:
     * create the default with {@link java.util.concurrent.Executors#newScheduledThreadPool(int, java.util.concurrent.ThreadFactory)}.
     * <p>
     * For the changes to take effect, the Schedulers has to be restarted.
     * @param factory the supplier that is called when the GenericScheduledExecutorService
     * is (re)started
     */
    public static void setOnGenericScheduledExecutorService(Func0<? extends ScheduledExecutorService> factory) {
        if (lockdown) {
            return;
        }
        onGenericScheduledExecutorService = factory;
    }

    /**
     * Returns the current factory for creating ScheduledExecutorServices in
     * GenericScheduledExecutorService utility.
     * <p>
     * This operation is thread-safe.
     * @return the current factory function
     */
    public static Func0<? extends ScheduledExecutorService> getOnGenericScheduledExecutorService() {
        return onGenericScheduledExecutorService;
    }
}
