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
package com.tapsdk.antiaddiction.reactor.schedulers;

import com.tapsdk.antiaddiction.reactor.Subscription;
import com.tapsdk.antiaddiction.reactor.functions.Action0;

import java.util.concurrent.TimeUnit;

public abstract class Scheduler {

    /**
     * Retrieves or creates a new {@link Worker} that represents serial execution of actions.
     * <p>
     * When work is completed it should be unsubscribed using {@link Worker#unsubscribe()}.
     * <p>
     * Work on a {@link Worker} is guaranteed to be sequential.
     *
     * @return a Worker representing a serial queue of actions to be executed
     */
    public abstract Worker createWorker();

    /**
     * Sequential Scheduler for executing actions on a single thread or event loop.
     * <p>
     * Unsubscribing the {@link Worker} cancels all outstanding work and allows resources cleanup.
     */
    public abstract static class Worker implements Subscription {

        /**
         * Schedules an Action for execution.
         *
         * @param action
         *            Action to schedule
         * @return a subscription to be able to prevent or cancel the execution of the action
         */
        public abstract Subscription schedule(Action0 action);

        /**
         * Schedules an Action for execution at some point in the future.
         * <p>
         * Note to implementors: non-positive {@code delayTime} should be regarded as non-delayed schedule, i.e.,
         * as if the {@link #schedule(rx.functions.Action0)} was called.
         *
         * @param action
         *            the Action to schedule
         * @param delayTime
         *            time to wait before executing the action; non-positive values indicate an non-delayed
         *            schedule
         * @param unit
         *            the time unit of {@code delayTime}
         * @return a subscription to be able to prevent or cancel the execution of the action
         */
        public abstract Subscription schedule(final Action0 action, final long delayTime, final TimeUnit unit);



        /**
         * Gets the current time, in milliseconds, according to this Scheduler.
         *
         * @return the scheduler's notion of current absolute time in milliseconds
         */
        public long now() {
            return System.currentTimeMillis();
        }
    }

    /**
     * Gets the current time, in milliseconds, according to this Scheduler.
     *
     * @return the scheduler's notion of current absolute time in milliseconds
     */
    public long now() {
        return System.currentTimeMillis();
    }

}