package com.tapsdk.antiaddiction.reactor.schedulers;

import com.tapsdk.antiaddiction.reactor.plugins.RxJavaHooks;
import com.tapsdk.antiaddiction.reactor.plugins.RxJavaPlugins;
import com.tapsdk.antiaddiction.reactor.plugins.RxJavaSchedulersHook;
import com.tapsdk.antiaddiction.reactor.schedulers.Scheduler;
import com.tapsdk.antiaddiction.reactor.schedulers.SchedulerLifecycle;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

public class Schedulers {

    private final com.tapsdk.antiaddiction.reactor.schedulers.Scheduler computationScheduler;
    private final com.tapsdk.antiaddiction.reactor.schedulers.Scheduler ioScheduler;
    private final com.tapsdk.antiaddiction.reactor.schedulers.Scheduler newThreadScheduler;

    private static final AtomicReference<Schedulers> INSTANCE = new AtomicReference<Schedulers>();

    private static Schedulers getInstance() {
        for (;;) {
            Schedulers current = INSTANCE.get();
            if (current != null) {
                return current;
            }
            current = new Schedulers();
            if (INSTANCE.compareAndSet(null, current)) {
                return current;
            } else {
                current.shutdownInstance();
            }
        }
    }

    private Schedulers() {
        @SuppressWarnings("deprecation")
        RxJavaSchedulersHook hook = RxJavaPlugins.getInstance().getSchedulersHook();

        com.tapsdk.antiaddiction.reactor.schedulers.Scheduler c = hook.getComputationScheduler();
        if (c != null) {
            computationScheduler = c;
        } else {
            computationScheduler = RxJavaSchedulersHook.createComputationScheduler();
        }

        com.tapsdk.antiaddiction.reactor.schedulers.Scheduler io = hook.getIOScheduler();
        if (io != null) {
            ioScheduler = io;
        } else {
            ioScheduler = RxJavaSchedulersHook.createIoScheduler();
        }

        com.tapsdk.antiaddiction.reactor.schedulers.Scheduler nt = hook.getNewThreadScheduler();
        if (nt != null) {
            newThreadScheduler = nt;
        } else {
            newThreadScheduler = RxJavaSchedulersHook.createNewThreadScheduler();
        }
    }



    /**
     * Creates and returns a {@link com.tapsdk.antiaddiction.reactor.schedulers.Scheduler} that creates a new {@link Thread} for each unit of work.
     * <p>
     * Unhandled errors will be delivered to the scheduler Thread's {@link Thread.UncaughtExceptionHandler}.
     *
     * @return a {@link com.tapsdk.antiaddiction.reactor.schedulers.Scheduler} that creates new threads
     */
    public static com.tapsdk.antiaddiction.reactor.schedulers.Scheduler newThread() {
        return RxJavaHooks.onNewThreadScheduler(getInstance().newThreadScheduler);
    }

    /**
     * Creates and returns a {@link com.tapsdk.antiaddiction.reactor.schedulers.Scheduler} intended for computational work.
     * <p>
     * This can be used for event-loops, processing callbacks and other computational work.
     * <p>
     * Do not perform IO-bound work on this scheduler. Use {@link #io()} instead.
     * <p>
     * Unhandled errors will be delivered to the scheduler Thread's {@link Thread.UncaughtExceptionHandler}.
     *
     * @return a {@link com.tapsdk.antiaddiction.reactor.schedulers.Scheduler} meant for computation-bound work
     */
    public static com.tapsdk.antiaddiction.reactor.schedulers.Scheduler computation() {
        return RxJavaHooks.onComputationScheduler(getInstance().computationScheduler);
    }

    /**
     * Creates and returns a {@link com.tapsdk.antiaddiction.reactor.schedulers.Scheduler} intended for IO-bound work.
     * <p>
     * The implementation is backed by an {@link Executor} thread-pool that will grow as needed.
     * <p>
     * This can be used for asynchronously performing blocking IO.
     * <p>
     * Do not perform computational work on this scheduler. Use {@link #computation()} instead.
     * <p>
     * Unhandled errors will be delivered to the scheduler Thread's {@link Thread.UncaughtExceptionHandler}.
     *
     * @return a {@link com.tapsdk.antiaddiction.reactor.schedulers.Scheduler} meant for IO-bound work
     */
    public static Scheduler io() {
        return RxJavaHooks.onIOScheduler(getInstance().ioScheduler);
    }

    /**
     * Resets the current {@link Schedulers} instance.
     * This will re-init the cached schedulers on the next usage,
     * which can be useful in testing.
     * @since 1.3
     */
    public static void reset() {
        Schedulers s = INSTANCE.getAndSet(null);
        if (s != null) {
            s.shutdownInstance();
        }
    }

    /**
     * Start the instance-specific schedulers.
     */
    synchronized void startInstance() { // NOPMD
        if (computationScheduler instanceof com.tapsdk.antiaddiction.reactor.schedulers.SchedulerLifecycle) {
            ((com.tapsdk.antiaddiction.reactor.schedulers.SchedulerLifecycle) computationScheduler).start();
        }
        if (ioScheduler instanceof com.tapsdk.antiaddiction.reactor.schedulers.SchedulerLifecycle) {
            ((com.tapsdk.antiaddiction.reactor.schedulers.SchedulerLifecycle) ioScheduler).start();
        }
        if (newThreadScheduler instanceof com.tapsdk.antiaddiction.reactor.schedulers.SchedulerLifecycle) {
            ((com.tapsdk.antiaddiction.reactor.schedulers.SchedulerLifecycle) newThreadScheduler).start();
        }
    }

    /**
     * Start the instance-specific schedulers.
     */
    synchronized void shutdownInstance() { // NOPMD
        if (computationScheduler instanceof com.tapsdk.antiaddiction.reactor.schedulers.SchedulerLifecycle) {
            ((com.tapsdk.antiaddiction.reactor.schedulers.SchedulerLifecycle) computationScheduler).shutdown();
        }
        if (ioScheduler instanceof com.tapsdk.antiaddiction.reactor.schedulers.SchedulerLifecycle) {
            ((com.tapsdk.antiaddiction.reactor.schedulers.SchedulerLifecycle) ioScheduler).shutdown();
        }
        if (newThreadScheduler instanceof com.tapsdk.antiaddiction.reactor.schedulers.SchedulerLifecycle) {
            ((SchedulerLifecycle) newThreadScheduler).shutdown();
        }
    }
}
