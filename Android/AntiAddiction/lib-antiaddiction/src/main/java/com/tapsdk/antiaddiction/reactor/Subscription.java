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
package com.tapsdk.antiaddiction.reactor;

/**
 * Subscription returns from {@link Observable#subscribe(com.tapsdk.antiaddiction.reactor.Subscriber)} to allow unsubscribing.
 * <p>
 * See the utilities in {@link Subscriptions} and the implementations in the {@code rx.subscriptions} package.
 * <p>
 * This interface is the RxJava equivalent of {@code IDisposable} in Microsoft's Rx implementation.
 */
public interface Subscription {
    /**
     * Stops the receipt of notifications on the {@link com.tapsdk.antiaddiction.reactor.Subscriber} that was registered when this Subscription
     * was received.
     * <p>
     * This allows deregistering an {@link Subscriber} before it has finished receiving all events (i.e. before
     * onCompleted is called).
     */
    void unsubscribe();

    /**
     * Indicates whether this {@code Subscription} is currently unsubscribed.
     *
     * @return {@code true} if this {@code Subscription} is currently unsubscribed, {@code false} otherwise
     */
    boolean isUnsubscribed();
}


