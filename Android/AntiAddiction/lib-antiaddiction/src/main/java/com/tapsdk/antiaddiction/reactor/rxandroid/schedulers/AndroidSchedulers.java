package com.tapsdk.antiaddiction.reactor.rxandroid.schedulers;

import android.os.Looper;

import com.tapsdk.antiaddiction.reactor.rxandroid.plugins.RxAndroidPlugins;
import com.tapsdk.antiaddiction.reactor.rxandroid.plugins.RxAndroidSchedulersHook;
import com.tapsdk.antiaddiction.reactor.schedulers.Scheduler;

import java.util.concurrent.atomic.AtomicReference;

public class AndroidSchedulers {
    private static final AtomicReference<AndroidSchedulers> INSTANCE = new AtomicReference<>();

    private final Scheduler mainThreadScheduler;

    private static AndroidSchedulers getInstance() {
        for (;;) {
            AndroidSchedulers current = INSTANCE.get();
            if (current != null) {
                return current;
            }
            current = new AndroidSchedulers();
            if (INSTANCE.compareAndSet(null, current)) {
                return current;
            }
        }
    }

    private AndroidSchedulers() {
        RxAndroidSchedulersHook hook = RxAndroidPlugins.getInstance().getSchedulersHook();

        Scheduler main = hook.getMainThreadScheduler();
        if (main != null) {
            mainThreadScheduler = main;
        } else {
            mainThreadScheduler = new com.tapsdk.antiaddiction.reactor.rxandroid.schedulers.LooperScheduler(Looper.getMainLooper());
        }
    }

    /** A {@link Scheduler} which executes actions on the Android UI thread. */
    public static Scheduler mainThread() {
        return getInstance().mainThreadScheduler;
    }

    /** A {@link Scheduler} which executes actions on {@code looper}. */
    public static Scheduler from(Looper looper) {
        if (looper == null) throw new NullPointerException("looper == null");
        return new com.tapsdk.antiaddiction.reactor.rxandroid.schedulers.LooperScheduler(looper);
    }

    /**
     * Resets the current {@link AndroidSchedulers} instance.
     * This will re-init the cached schedulers on the next usage,
     * which can be useful in testing.
     */
    public static void reset() {
        INSTANCE.set(null);
    }
}
