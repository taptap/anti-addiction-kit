package com.tapsdk.antiaddiction.reactor.rxandroid.plugins;

import java.util.concurrent.atomic.AtomicReference;

public class RxAndroidPlugins {
    private static final RxAndroidPlugins INSTANCE = new RxAndroidPlugins();

    public static RxAndroidPlugins getInstance() {
        return INSTANCE;
    }

    private final AtomicReference<com.tapsdk.antiaddiction.reactor.rxandroid.plugins.RxAndroidSchedulersHook> schedulersHook = new AtomicReference<>();

    RxAndroidPlugins() {
    }

    /**
     * Reset any explicit or default-set hooks.
     * <p>
     * Note: This should only be used for testing purposes.
     */
    public void reset() {
        schedulersHook.set(null);
    }

    /**
     * Retrieves the instance of {@link RxAndroidSchedulersHook} to use based on order of
     * precedence as defined in the {@link RxAndroidPlugins} class header.
     * <p>
     * Override the default by calling {@link #registerSchedulersHook(RxAndroidSchedulersHook)} or by
     * setting the property {@code rxandroid.plugin.RxAndroidSchedulersHook.implementation} with the
     * full classname to load.
     */
    public com.tapsdk.antiaddiction.reactor.rxandroid.plugins.RxAndroidSchedulersHook getSchedulersHook() {
        if (schedulersHook.get() == null) {
            schedulersHook.compareAndSet(null, com.tapsdk.antiaddiction.reactor.rxandroid.plugins.RxAndroidSchedulersHook.getDefaultInstance());
            // We don't return from here but call get() again in case of thread-race so the winner will
            // always get returned.
        }
        return schedulersHook.get();
    }

    /**
     * Registers an {@link com.tapsdk.antiaddiction.reactor.rxandroid.plugins.RxAndroidSchedulersHook} implementation as a global override of any
     * injected or default implementations.
     *
     * @throws IllegalStateException if called more than once or after the default was initialized
     * (if usage occurs before trying to register)
     */
    public void registerSchedulersHook(com.tapsdk.antiaddiction.reactor.rxandroid.plugins.RxAndroidSchedulersHook impl) {
        if (!schedulersHook.compareAndSet(null, impl)) {
            throw new IllegalStateException(
                    "Another strategy was already registered: " + schedulersHook.get());
        }
    }
}
