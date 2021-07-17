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

import com.tapsdk.antiaddiction.reactor.exceptions.Exceptions;
import com.tapsdk.antiaddiction.reactor.plugins.RxJavaPlugins;

/**
 * Abstract class for defining error handling logic in addition to the normal
 * <p>
 * For example, all {@code Exception}s can be logged using this handler even if
 * <p>
 * This plugin is also responsible for augmenting rendering of {@code OnErrorThrowable.OnNextValue}.
 * <p>
 * See {@link RxJavaPlugins} or the RxJava GitHub Wiki for information on configuring plugins: <a
 * href="https://github.com/ReactiveX/RxJava/wiki/Plugins">https://github.com/ReactiveX/RxJava/wiki/Plugins</a>.
 */
public class RxJavaErrorHandler {

    protected static final String ERROR_IN_RENDERING_SUFFIX = ".errorRendering";

    @Deprecated
    public void handleError(Throwable e) {
        // do nothing by default
    }

    public final String handleOnNextValueRendering(Object item) {

        try {
            return render(item);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Throwable t) {
            Exceptions.throwIfFatal(t);
        }
        return item.getClass().getName() + ERROR_IN_RENDERING_SUFFIX;
    }

    protected String render (Object item) throws InterruptedException {
        //do nothing by default
        return null;
    }
}
