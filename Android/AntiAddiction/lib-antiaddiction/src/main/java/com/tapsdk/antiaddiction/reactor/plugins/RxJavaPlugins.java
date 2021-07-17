package com.tapsdk.antiaddiction.reactor.plugins;

import com.tapsdk.antiaddiction.reactor.plugins.RxJavaErrorHandler;
import com.tapsdk.antiaddiction.reactor.plugins.RxJavaObservableExecutionHook;
import com.tapsdk.antiaddiction.reactor.plugins.RxJavaObservableExecutionHookDefault;
import com.tapsdk.antiaddiction.reactor.plugins.RxJavaSchedulersHook;

import java.util.concurrent.atomic.AtomicReference;


public class RxJavaPlugins {
    private final static RxJavaPlugins INSTANCE = new RxJavaPlugins();

    private final AtomicReference<com.tapsdk.antiaddiction.reactor.plugins.RxJavaErrorHandler> errorHandler = new AtomicReference<com.tapsdk.antiaddiction.reactor.plugins.RxJavaErrorHandler>();
    private final AtomicReference<com.tapsdk.antiaddiction.reactor.plugins.RxJavaObservableExecutionHook> observableExecutionHook = new AtomicReference<com.tapsdk.antiaddiction.reactor.plugins.RxJavaObservableExecutionHook>();
    private final AtomicReference<RxJavaSchedulersHook> schedulersHook = new AtomicReference<RxJavaSchedulersHook>();

    static final com.tapsdk.antiaddiction.reactor.plugins.RxJavaErrorHandler DEFAULT_ERROR_HANDLER = new com.tapsdk.antiaddiction.reactor.plugins.RxJavaErrorHandler() {
    };

    public void reset() {
        INSTANCE.errorHandler.set(null);
        INSTANCE.observableExecutionHook.set(null);
        INSTANCE.schedulersHook.set(null);
    }

    static {
    }

    @Deprecated
    public static RxJavaPlugins getInstance() {
        return INSTANCE;
    }

    public RxJavaErrorHandler getErrorHandler() {
        if (errorHandler.get() == null) {
            errorHandler.compareAndSet(null, DEFAULT_ERROR_HANDLER);
        }
        return errorHandler.get();
    }

    public RxJavaObservableExecutionHook getObservableExecutionHook() {
        if (observableExecutionHook.get() == null) {
            observableExecutionHook.compareAndSet(null, com.tapsdk.antiaddiction.reactor.plugins.RxJavaObservableExecutionHookDefault.getInstance());
        }
        return observableExecutionHook.get();
    }

    public RxJavaSchedulersHook getSchedulersHook() {
        if (schedulersHook.get() == null) {
            schedulersHook.compareAndSet(null, RxJavaSchedulersHook.getDefaultInstance());
        }
        return schedulersHook.get();
    }

}
