/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tapsdk.antiaddiction.skynet.retrofit2;

import com.tapsdk.antiaddiction.skynet.okhttp3.Request;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

import static com.tapsdk.antiaddiction.skynet.retrofit2.Utils.checkNotNull;

final class ExecutorCallAdapterFactory extends com.tapsdk.antiaddiction.skynet.retrofit2.CallAdapter.Factory {
    final Executor callbackExecutor;

    ExecutorCallAdapterFactory(Executor callbackExecutor) {
        this.callbackExecutor = callbackExecutor;
    }

    @Override
    public com.tapsdk.antiaddiction.skynet.retrofit2.CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (getRawType(returnType) != com.tapsdk.antiaddiction.skynet.retrofit2.Call.class) {
            return null;
        }
        final Type responseType = Utils.getCallResponseType(returnType);
        return new CallAdapter<Object, com.tapsdk.antiaddiction.skynet.retrofit2.Call<?>>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public com.tapsdk.antiaddiction.skynet.retrofit2.Call<Object> adapt(com.tapsdk.antiaddiction.skynet.retrofit2.Call<Object> call) {
                return new ExecutorCallbackCall<>(callbackExecutor, call);
            }
        };
    }

    static final class ExecutorCallbackCall<T> implements com.tapsdk.antiaddiction.skynet.retrofit2.Call<T> {
        final Executor callbackExecutor;
        final com.tapsdk.antiaddiction.skynet.retrofit2.Call<T> delegate;

        ExecutorCallbackCall(Executor callbackExecutor, com.tapsdk.antiaddiction.skynet.retrofit2.Call<T> delegate) {
            this.callbackExecutor = callbackExecutor;
            this.delegate = delegate;
        }

        @Override
        public void enqueue(final com.tapsdk.antiaddiction.skynet.retrofit2.Callback<T> callback) {
            checkNotNull(callback, "callback == null");

            delegate.enqueue(new Callback<T>() {
                @Override
                public void onResponse(com.tapsdk.antiaddiction.skynet.retrofit2.Call<T> call, final Response<T> response) {
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (delegate.isCanceled()) {
                                // Emulate OkHttp's behavior of throwing/delivering an IOException on cancellation.
                                callback.onFailure(ExecutorCallbackCall.this, new IOException("Canceled"));
                            } else {
                                callback.onResponse(ExecutorCallbackCall.this, response);
                            }
                        }
                    });
                }

                @Override
                public void onFailure(com.tapsdk.antiaddiction.skynet.retrofit2.Call<T> call, final Throwable t) {
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(ExecutorCallbackCall.this, t);
                        }
                    });
                }
            });
        }

        @Override
        public boolean isExecuted() {
            return delegate.isExecuted();
        }

        @Override
        public Response<T> execute() throws IOException {
            return delegate.execute();
        }

        @Override
        public void cancel() {
            delegate.cancel();
        }

        @Override
        public boolean isCanceled() {
            return delegate.isCanceled();
        }

        @SuppressWarnings("CloneDoesntCallSuperClone") // Performing deep clone.
        @Override
        public Call<T> clone() {
            return new ExecutorCallbackCall<>(callbackExecutor, delegate.clone());
        }

        @Override
        public Request request() {
            return delegate.request();
        }
    }
}
