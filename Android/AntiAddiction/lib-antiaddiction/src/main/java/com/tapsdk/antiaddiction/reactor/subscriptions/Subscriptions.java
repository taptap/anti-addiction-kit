/**
 * Copyright 2014 Netflix, Inc.
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
package com.tapsdk.antiaddiction.reactor.subscriptions;

import com.tapsdk.antiaddiction.reactor.Subscription;
import com.tapsdk.antiaddiction.reactor.functions.Action0;

import java.util.concurrent.Future;

/**
 * Helper methods and utilities for creating and working with {@link Subscription} objects
 */
public final class Subscriptions {
    /**
     * A {@link Subscription} that does nothing when its unsubscribe method is called.
     */
    private static final Unsubscribed UNSUBSCRIBED = new Unsubscribed();

    private Subscriptions() {
        throw new IllegalStateException("No instances!");
    }

    public static Subscription empty() {
        return com.tapsdk.antiaddiction.reactor.subscriptions.BooleanSubscription.create();
    }

    /**
     * Returns a {@link Subscription} to which {@code unsubscribe} does nothing, as it is already unsubscribed.
     * Its {@code isUnsubscribed} always returns {@code true}, which is different from {@link #empty()}.
     *
     * <pre><code>
     * Subscription unsubscribed = Subscriptions.unsubscribed();
     * System.out.println(unsubscribed.isUnsubscribed()); // true
     * </code></pre>
     *
     * @return a {@link Subscription} to which {@code unsubscribe} does nothing, as it is already unsubscribed
     * @since 1.1.0
     */
    public static Subscription unsubscribed() {
        return UNSUBSCRIBED;
    }

    /**
     * Creates and returns a {@link Subscription} that invokes the given {@link Action0} when unsubscribed.
     *
     * @param unsubscribe
     *            Action to invoke on unsubscribe.
     * @return {@link Subscription}
     */
    public static Subscription create(final Action0 unsubscribe) {
        return BooleanSubscription.create(unsubscribe);
    }

    /**
     * Converts a {@link Future} into a {@link Subscription} and cancels it when unsubscribed.
     *
     * @param f
     *            the {@link Future} to convert
     * @return a {@link Subscription} that wraps {@code f}
     */
    public static Subscription from(final Future<?> f) {
        return new FutureSubscription(f);
    }

        /** Naming classes helps with debugging. */
    static final class FutureSubscription implements Subscription {
        final Future<?> f;

        public FutureSubscription(Future<?> f) {
            this.f = f;
        }
        @Override
        public void unsubscribe() {
            f.cancel(true);
        }

        @Override
        public boolean isUnsubscribed() {
            return f.isCancelled();
        }
    }

    /**
     * Converts a set of {@link Subscription}s into a {@link com.tapsdk.antiaddiction.reactor.subscriptions.CompositeSubscription} that groups the multiple
     * Subscriptions together and unsubscribes from all of them together.
     *
     * @param subscriptions
     *            the Subscriptions to group together
     * @return a {@link com.tapsdk.antiaddiction.reactor.subscriptions.CompositeSubscription} representing the {@code subscriptions} set
     */

    public static com.tapsdk.antiaddiction.reactor.subscriptions.CompositeSubscription from(Subscription... subscriptions) {
        return new CompositeSubscription(subscriptions);
    }

        /** Naming classes helps with debugging. */
    static final class Unsubscribed implements Subscription {
        @Override
        public void unsubscribe() {
            // deliberately ignored
        }

        @Override
        public boolean isUnsubscribed() {
            return true;
        }
    }
}
