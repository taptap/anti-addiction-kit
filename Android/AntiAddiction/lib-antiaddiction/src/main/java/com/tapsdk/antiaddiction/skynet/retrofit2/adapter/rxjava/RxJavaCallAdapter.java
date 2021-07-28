/*
 * Copyright (C) 2016 Jake Wharton
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
package com.tapsdk.antiaddiction.skynet.retrofit2.adapter.rxjava;

import com.tapsdk.antiaddiction.reactor.Observable;
import com.tapsdk.antiaddiction.reactor.schedulers.Scheduler;
import com.tapsdk.antiaddiction.skynet.retrofit2.Call;
import com.tapsdk.antiaddiction.skynet.retrofit2.CallAdapter;
import com.tapsdk.antiaddiction.skynet.retrofit2.Response;

import java.lang.reflect.Type;


final class RxJavaCallAdapter<R> implements CallAdapter<R, Object> {
  private final Type responseType;
  private final Scheduler scheduler;
  private final boolean isAsync;
  private final boolean isResult;
  private final boolean isBody;

  RxJavaCallAdapter(Type responseType, Scheduler scheduler, boolean isAsync,
      boolean isResult, boolean isBody) {
    this.responseType = responseType;
    this.scheduler = scheduler;
    this.isAsync = isAsync;
    this.isResult = isResult;
    this.isBody = isBody;
  }

  @Override public Type responseType() {
    return responseType;
  }

  @Override public Object adapt(Call<R> call) {
    Observable.OnSubscribe<Response<R>> callFunc = isAsync
        ? new CallEnqueueOnSubscribe<>(call)
        : new com.tapsdk.antiaddiction.skynet.retrofit2.adapter.rxjava.CallExecuteOnSubscribe<>(call);

    Observable.OnSubscribe<?> func;
    if (isResult) {
      func = new com.tapsdk.antiaddiction.skynet.retrofit2.adapter.rxjava.ResultOnSubscribe<>(callFunc);
    } else if (isBody) {
      func = new com.tapsdk.antiaddiction.skynet.retrofit2.adapter.rxjava.BodyOnSubscribe<>(callFunc);
    } else {
      func = callFunc;
    }
    Observable<?> observable = Observable.create(func);

    if (scheduler != null) {
      observable = observable.subscribeOn(scheduler);
    }

    return observable;
  }
}
