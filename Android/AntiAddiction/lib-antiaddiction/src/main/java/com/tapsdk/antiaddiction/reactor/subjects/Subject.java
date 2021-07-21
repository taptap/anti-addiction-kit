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
package com.tapsdk.antiaddiction.reactor.subjects;


import com.tapsdk.antiaddiction.reactor.Observable;
import com.tapsdk.antiaddiction.reactor.Observer;

/**
 * Represents an object that is both an Observable and an Observer.
 * @param <T> the input value type
 * @param <R> the output value type
 */
public abstract class Subject<T, R> extends Observable<R> implements Observer<T> {
    public Subject(OnSubscribe<R> onSubscribe) {super(onSubscribe);}

    /**
     * Indicates whether the {@link Subject} has {@link Observer Observers} subscribed to it.
     *
     * @return true if there is at least one Observer subscribed to this Subject, false otherwise
     */
    public abstract boolean hasObservers();

    public final SerializedSubject<T, R> toSerialized() {
        if (getClass() == SerializedSubject.class) {
            return (SerializedSubject<T, R>)this;
        }
        return new SerializedSubject<T, R>(this);
    }
}
