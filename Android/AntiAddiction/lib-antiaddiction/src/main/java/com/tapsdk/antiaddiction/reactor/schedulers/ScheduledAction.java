/**
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.tapsdk.antiaddiction.reactor.schedulers;

import com.tapsdk.antiaddiction.reactor.Subscription;
import com.tapsdk.antiaddiction.reactor.exceptions.OnErrorNotImplementedException;
import com.tapsdk.antiaddiction.reactor.functions.Action0;
import com.tapsdk.antiaddiction.reactor.plugins.RxJavaHooks;
import com.tapsdk.antiaddiction.reactor.subscriptions.CompositeSubscription;
import com.tapsdk.antiaddiction.reactor.util.SubscriptionList;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


/**
 * A {@code Runnable} that executes an {@code Action0} and can be cancelled. The analog is the
 * {@code Subscriber} in respect of an {@code Observer}.
 */
public final class ScheduledAction extends AtomicReference<Thread> implements Runnable, Subscription {
    /** */
    private static final long serialVersionUID = -3962399486978279857L;
    final SubscriptionList cancel;
    final Action0 action;

    public ScheduledAction(Action0 action) {
        this.action = action;
        this.cancel = new SubscriptionList();
    }
    public ScheduledAction(Action0 action, CompositeSubscription parent) {
        this.action = action;
        this.cancel = new SubscriptionList(new Remover(this, parent));
    }
    public ScheduledAction(Action0 action, SubscriptionList parent) {
        this.action = action;
        this.cancel = new SubscriptionList(new Remover2(this, parent));
    }

    @Override
    public void run() {
        try {
            lazySet(Thread.currentThread());
            action.call();
        } catch (OnErrorNotImplementedException e) {
            signalError(new IllegalStateException("Exception thrown on Scheduler.Worker thread. Add `onError` handling.", e));
        } catch (Throwable e) {
            signalError(new IllegalStateException("Fatal Exception thrown on Scheduler.Worker thread.", e));
        } finally {
            unsubscribe();
        }
    }

    void signalError(Throwable ie) {
        RxJavaHooks.onError(ie);
        Thread thread = Thread.currentThread();
        thread.getUncaughtExceptionHandler().uncaughtException(thread, ie);
    }

    @Override
    public boolean isUnsubscribed() {
        return cancel.isUnsubscribed();
    }

    @Override
    public void unsubscribe() {
        if (!cancel.isUnsubscribed()) {
            cancel.unsubscribe();
        }
    }

    static final class Remover extends AtomicBoolean implements Subscription {
        /** */
        private static final long serialVersionUID = 247232374289553518L;
        final ScheduledAction s;
        final CompositeSubscription parent;

        public Remover(ScheduledAction s, CompositeSubscription parent) {
            this.s = s;
            this.parent = parent;
        }

        @Override
        public boolean isUnsubscribed() {
            return s.isUnsubscribed();
        }

        @Override
        public void unsubscribe() {
            if (compareAndSet(false, true)) {
                parent.remove(s);
            }
        }

    }

    /**
     * Adds a general Subscription to this {@code ScheduledAction} that will be unsubscribed
     * if the underlying {@code action} completes or the this scheduled action is cancelled.
     *
     * @param s the Subscription to add
     */
    public void add(Subscription s) {
        cancel.add(s);
    }

    public void add(final Future<?> f) {
        cancel.add(new FutureCompleter(f));
    }

    public void addParent(SubscriptionList parent) {
        cancel.add(new Remover2(this, parent));
    }

    public void addParent(CompositeSubscription parent) {
        cancel.add(new Remover(this, parent));
    }

    /**
     * Cancels the captured future if the caller of the call method
     * is not the same as the runner of the outer ScheduledAction to
     * prevent unnecessary self-interrupting if the unsubscription
     * happens from the same thread.
     */
    final class FutureCompleter implements Subscription {
        private final Future<?> f;

        FutureCompleter(Future<?> f) {
            this.f = f;
        }

        @Override
        public void unsubscribe() {
            if (ScheduledAction.this.get() != Thread.currentThread()) {
                f.cancel(true);
            } else {
                f.cancel(false);
            }
        }
        @Override
        public boolean isUnsubscribed() {
            return f.isCancelled();
        }
    }

    /** Remove a child subscription from a composite when unsubscribing. */
    static final class Remover2 extends AtomicBoolean implements Subscription {
        /** */
        private static final long serialVersionUID = 247232374289553518L;
        final ScheduledAction s;
        final SubscriptionList parent;

        public Remover2(ScheduledAction s, SubscriptionList parent) {
            this.s = s;
            this.parent = parent;
        }

        @Override
        public boolean isUnsubscribed() {
            return s.isUnsubscribed();
        }

        @Override
        public void unsubscribe() {
            if (compareAndSet(false, true)) {
                parent.remove(s);
            }
        }
    }
}
