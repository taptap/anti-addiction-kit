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

import com.tapsdk.antiaddiction.skynet.okhttp3.MediaType;
import com.tapsdk.antiaddiction.skynet.okhttp3.Request;
import com.tapsdk.antiaddiction.skynet.okhttp3.ResponseBody;
import com.tapsdk.antiaddiction.skynet.okio.Buffer;
import com.tapsdk.antiaddiction.skynet.okio.BufferedSource;
import com.tapsdk.antiaddiction.skynet.okio.ForwardingSource;
import com.tapsdk.antiaddiction.skynet.okio.Okio;
import com.tapsdk.antiaddiction.skynet.retrofit2.Call;
import com.tapsdk.antiaddiction.skynet.retrofit2.Callback;
import com.tapsdk.antiaddiction.skynet.retrofit2.Response;
import com.tapsdk.antiaddiction.skynet.retrofit2.ServiceMethod;
import com.tapsdk.antiaddiction.skynet.retrofit2.Utils;

import java.io.IOException;

import static com.tapsdk.antiaddiction.skynet.retrofit2.Utils.checkNotNull;
import static com.tapsdk.antiaddiction.skynet.retrofit2.Utils.throwIfFatal;

final class OkHttpCall<T> implements Call<T> {
    private final ServiceMethod<T, ?> serviceMethod;
    private final Object[] args;

    private volatile boolean canceled;

    private com.tapsdk.antiaddiction.skynet.okhttp3.Call rawCall;
    private Throwable creationFailure;
    private boolean executed;

    OkHttpCall(ServiceMethod<T, ?> serviceMethod, Object[] args) {
        this.serviceMethod = serviceMethod;
        this.args = args;
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    // We are a final type & this saves clearing state.
    @Override
    public OkHttpCall<T> clone() {
        return new OkHttpCall<>(serviceMethod, args);
    }

    @Override
    public synchronized Request request() {
        com.tapsdk.antiaddiction.skynet.okhttp3.Call call = rawCall;
        if (call != null) {
            return call.request();
        }
        if (creationFailure != null) {
            if (creationFailure instanceof IOException) {
                throw new RuntimeException("Unable to create request.", creationFailure);
            } else if (creationFailure instanceof RuntimeException) {
                throw (RuntimeException) creationFailure;
            } else {
                throw (Error) creationFailure;
            }
        }
        try {
            return (rawCall = createRawCall()).request();
        } catch (RuntimeException | Error e) {
            throwIfFatal(e); // Do not assign a fatal error to creationFailure.
            creationFailure = e;
            throw e;
        } catch (IOException e) {
            creationFailure = e;
            throw new RuntimeException("Unable to create request.", e);
        }
    }

    @Override
    public void enqueue(final Callback<T> callback) {
        checkNotNull(callback, "callback == null");

        com.tapsdk.antiaddiction.skynet.okhttp3.Call call;
        Throwable failure;

        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed.");
            executed = true;

            call = rawCall;
            failure = creationFailure;
            if (call == null && failure == null) {
                try {
                    call = rawCall = createRawCall();
                } catch (Throwable t) {
                    throwIfFatal(t);
                    failure = creationFailure = t;
                }
            }
        }

        if (failure != null) {
            callback.onFailure(this, failure);
            return;
        }

        if (canceled) {
            call.cancel();
        }

        call.enqueue(new com.tapsdk.antiaddiction.skynet.okhttp3.Callback() {
            @Override
            public void onResponse(com.tapsdk.antiaddiction.skynet.okhttp3.Call call, com.tapsdk.antiaddiction.skynet.okhttp3.Response rawResponse) {
                Response<T> response;
                try {
                    response = parseResponse(rawResponse);
                } catch (Throwable e) {
                    callFailure(e);
                    return;
                }

                try {
                    callback.onResponse(OkHttpCall.this, response);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            @Override
            public void onFailure(com.tapsdk.antiaddiction.skynet.okhttp3.Call call, IOException e) {
                callFailure(e);
            }

            private void callFailure(Throwable e) {
                try {
                    callback.onFailure(OkHttpCall.this, e);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
    }

    @Override
    public synchronized boolean isExecuted() {
        return executed;
    }

    @Override
    public Response<T> execute() throws IOException {
        com.tapsdk.antiaddiction.skynet.okhttp3.Call call;

        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed.");
            executed = true;

            if (creationFailure != null) {
                if (creationFailure instanceof IOException) {
                    throw (IOException) creationFailure;
                } else if (creationFailure instanceof RuntimeException) {
                    throw (RuntimeException) creationFailure;
                } else {
                    throw (Error) creationFailure;
                }
            }

            call = rawCall;
            if (call == null) {
                try {
                    call = rawCall = createRawCall();
                } catch (IOException | RuntimeException | Error e) {
                    throwIfFatal(e); //  Do not assign a fatal error to creationFailure.
                    creationFailure = e;
                    throw e;
                }
            }
        }

        if (canceled) {
            call.cancel();
        }

        return parseResponse(call.execute());
    }

    private com.tapsdk.antiaddiction.skynet.okhttp3.Call createRawCall() throws IOException {
        com.tapsdk.antiaddiction.skynet.okhttp3.Call call = serviceMethod.toCall(args);
        if (call == null) {
            throw new NullPointerException("Call.Factory returned null.");
        }
        return call;
    }

    Response<T> parseResponse(com.tapsdk.antiaddiction.skynet.okhttp3.Response rawResponse) throws IOException {
        ResponseBody rawBody = rawResponse.body();

        // Remove the body's source (the only stateful object) so we can pass the response along.
        rawResponse = rawResponse.newBuilder()
                .body(new NoContentResponseBody(rawBody.contentType(), rawBody.contentLength()))
                .build();

        int code = rawResponse.code();
        if (code < 200 || code >= 300) {
            try {
                // Buffer the entire body to avoid future I/O.
                ResponseBody bufferedBody = Utils.buffer(rawBody);
                return Response.error(bufferedBody, rawResponse);
            } finally {
                rawBody.close();
            }
        }

        if (code == 204 || code == 205) {
            rawBody.close();
            return Response.success(null, rawResponse);
        }

        ExceptionCatchingRequestBody catchingBody = new ExceptionCatchingRequestBody(rawBody);
        try {
            T body = serviceMethod.toResponse(catchingBody);
            return Response.success(body, rawResponse);
        } catch (RuntimeException e) {
            // If the underlying source threw an exception, propagate that rather than indicating it was
            // a runtime exception.
            catchingBody.throwIfCaught();
            throw e;
        }
    }

    public void cancel() {
        canceled = true;

        com.tapsdk.antiaddiction.skynet.okhttp3.Call call;
        synchronized (this) {
            call = rawCall;
        }
        if (call != null) {
            call.cancel();
        }
    }

    @Override
    public boolean isCanceled() {
        if (canceled) {
            return true;
        }
        synchronized (this) {
            return rawCall != null && rawCall.isCanceled();
        }
    }

    static final class NoContentResponseBody extends ResponseBody {
        private final MediaType contentType;
        private final long contentLength;

        NoContentResponseBody(MediaType contentType, long contentLength) {
            this.contentType = contentType;
            this.contentLength = contentLength;
        }

        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public long contentLength() {
            return contentLength;
        }

        @Override
        public BufferedSource source() {
            throw new IllegalStateException("Cannot read raw response body of a converted body.");
        }
    }

    static final class ExceptionCatchingRequestBody extends ResponseBody {
        private final ResponseBody delegate;
        IOException thrownException;

        ExceptionCatchingRequestBody(ResponseBody delegate) {
            this.delegate = delegate;
        }

        @Override
        public MediaType contentType() {
            return delegate.contentType();
        }

        @Override
        public long contentLength() {
            return delegate.contentLength();
        }

        @Override
        public BufferedSource source() {
            return Okio.buffer(new ForwardingSource(delegate.source()) {
                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    try {
                        return super.read(sink, byteCount);
                    } catch (IOException e) {
                        thrownException = e;
                        throw e;
                    }
                }
            });
        }

        @Override
        public void close() {
            delegate.close();
        }

        void throwIfCaught() throws IOException {
            if (thrownException != null) {
                throw thrownException;
            }
        }
    }
}
