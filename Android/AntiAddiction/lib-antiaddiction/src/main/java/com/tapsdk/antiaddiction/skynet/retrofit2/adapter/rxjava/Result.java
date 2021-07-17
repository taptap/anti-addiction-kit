package com.tapsdk.antiaddiction.skynet.retrofit2.adapter.rxjava;

import com.tapsdk.antiaddiction.skynet.retrofit2.Response;

import java.io.IOException;

public final class Result<T> {
    @SuppressWarnings("ConstantConditions") // Guarding public API nullability.
    public static <T> Result<T> error(Throwable error) {
        if (error == null) throw new NullPointerException("error == null");
        return new Result<>(null, error);
    }

    @SuppressWarnings("ConstantConditions") // Guarding public API nullability.
    public static <T> Result<T> response(Response<T> response) {
        if (response == null) throw new NullPointerException("response == null");
        return new Result<>(response, null);
    }

    private final Response<T> response;
    private final Throwable error;

    private Result(Response<T> response, Throwable error) {
        this.response = response;
        this.error = error;
    }

    /**
     * The response received from executing an HTTP request. Only present when {@link #isError()} is
     * false, null otherwise.
     */
    public Response<T> response() {
        return response;
    }

    /**
     * The error experienced while attempting to execute an HTTP request. Only present when {@link
     * #isError()} is true, null otherwise.
     * <p>
     * If the error is an {@link IOException} then there was a problem with the transport to the
     * remote server. Any other exception type indicates an unexpected failure and should be
     * considered fatal (configuration error, programming error, etc.).
     */
    public Throwable error() {
        return error;
    }

    /** {@code true} if the request resulted in an error. See {@link #error()} for the cause. */
    public boolean isError() {
        return error != null;
    }

    @Override public String toString() {
        if (error != null) {
            return "Result{isError=true, error=\"" + error + "\"}";
        }
        return "Result{isError=false, response=" + response + '}';
    }
}
