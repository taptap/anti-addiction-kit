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

package com.tapsdk.antiaddiction.reactor.plugins;

import com.tapsdk.antiaddiction.reactor.functions.Action0;
import com.tapsdk.antiaddiction.reactor.internal.schedulers.NewThreadScheduler;
import com.tapsdk.antiaddiction.reactor.plugins.RxJavaPlugins;
import com.tapsdk.antiaddiction.reactor.schedulers.CachedThreadScheduler;
import com.tapsdk.antiaddiction.reactor.schedulers.EventLoopsScheduler;
import com.tapsdk.antiaddiction.reactor.schedulers.Scheduler;
import com.tapsdk.antiaddiction.reactor.util.RxThreadFactory;

import java.util.concurrent.ThreadFactory;



/**
 * This plugin class provides 2 ways to customize {@link Scheduler} functionality
 * 1.  You may redefine entire schedulers, if you so choose.  To do so, override
 * the 3 methods that return Scheduler (io(), computation(), newThread()).
 * 2.  You may wrap/decorate an {@link Action0}, before it is handed off to a Scheduler.  The system-
 * supplied Schedulers (Schedulers.ioScheduler, Schedulers.computationScheduler,
 * Scheduler.newThreadScheduler) all use this hook, so it's a convenient way to
 * modify Scheduler functionality without redefining Schedulers wholesale.
 *
 * Also, when redefining Schedulers, you are free to use/not use the onSchedule decoration hook.
 * <p>
 * See {@link RxJavaPlugins} or the RxJava GitHub Wiki for information on configuring plugins:
 * <a href="https://github.com/ReactiveX/RxJava/wiki/Plugins">https://github.com/ReactiveX/RxJava/wiki/Plugins</a>.
 */
public class RxJavaSchedulersHook {

    private final static RxJavaSchedulersHook DEFAULT_INSTANCE = new RxJavaSchedulersHook();


    public static Scheduler createComputationScheduler() {
        return createComputationScheduler(new RxThreadFactory("RxComputationScheduler-"));
    }

    public static Scheduler createComputationScheduler(ThreadFactory threadFactory) {
        if (threadFactory == null) {
            throw new NullPointerException("threadFactory == null");
        }
        return new EventLoopsScheduler(threadFactory);
    }


    public static Scheduler createIoScheduler() {
        return createIoScheduler(new RxThreadFactory("RxIoScheduler-"));
    }

    public static Scheduler createIoScheduler(ThreadFactory threadFactory) {
        if (threadFactory == null) {
            throw new NullPointerException("threadFactory == null");
        }
        return new CachedThreadScheduler(threadFactory);
    }

    public Scheduler getComputationScheduler() {
        return null;
    }

    public Scheduler getIOScheduler() {
        return null;
    }

    public Scheduler getNewThreadScheduler() {
        return null;
    }

    public static Scheduler createNewThreadScheduler() {
        return createNewThreadScheduler(new RxThreadFactory("RxNewThreadScheduler-"));
    }

    public static Scheduler createNewThreadScheduler(ThreadFactory threadFactory) {
        if (threadFactory == null) {
            throw new NullPointerException("threadFactory == null");
        }
        return new NewThreadScheduler(threadFactory);
    }

    /**
     * Invoked before the Action is handed over to the scheduler.  Can be used for wrapping/decorating/logging.
     * The default is just a pass through.
     * @param action action to schedule
     * @return wrapped action to schedule
     */
    @Deprecated
    public Action0 onSchedule(Action0 action) {
        return action;
    }

    public static RxJavaSchedulersHook getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }
}
