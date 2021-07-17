package com.tapsdk.antiaddiction;

public interface Callback<T> {

    void onSuccess(T result);
    void onError(Throwable throwable);
}
