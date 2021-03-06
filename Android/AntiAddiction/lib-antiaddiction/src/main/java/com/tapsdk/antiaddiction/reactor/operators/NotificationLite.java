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
package com.tapsdk.antiaddiction.reactor.operators;

import com.tapsdk.antiaddiction.reactor.Observer;

import java.io.Serializable;


/**
 * For use in internal operators that need something like materialize and dematerialize wholly within the
 * implementation of the operator but don't want to incur the allocation cost of actually creating
 * {@link rx.Notification} objects for every {@link Observer#onNext onNext} and
 * {@link Observer#onCompleted onCompleted}.
 * <p>
 * An object is allocated inside {@link #error(Throwable)} to wrap the {@link Throwable} but this shouldn't
 * affect performance because exceptions should be exceptionally rare.
 */
public final class NotificationLite {

    private NotificationLite() {
    }

    private static final Object ON_COMPLETED_SENTINEL = new Serializable() {
        private static final long serialVersionUID = 1;

        @Override
        public String toString() {
            return "Notification=>Completed";
        }
    };

    private static final Object ON_NEXT_NULL_SENTINEL = new Serializable() {
        private static final long serialVersionUID = 2;

        @Override
        public String toString() {
            return "Notification=>NULL";
        }
    };

    static final class OnErrorSentinel implements Serializable {
        private static final long serialVersionUID = 3;
        final Throwable e;

        public OnErrorSentinel(Throwable e) {
            this.e = e;
        }

        @Override
        public String toString() {
            return "Notification=>Error:" + e;
        }
    }

    /**
     * Creates a lite {@code onNext} notification for the value passed in without doing any allocation. Can
     * be unwrapped and sent with the {@link #accept} method.
     *
     * @param <T> the value type to convert
     * @param t
     *          the item emitted to {@code onNext}
     * @return the item, or a null token representing the item if the item is {@code null}
     */
    public static <T> Object next(T t) {
        if (t == null) {
            return ON_NEXT_NULL_SENTINEL;
        } else {
            return t;
        }
    }

    /**
     * Creates a lite {@code onCompleted} notification without doing any allocation. Can be unwrapped and
     * sent with the {@link #accept} method.
     *
     * @return a completion token
     */
    public static Object completed() {
        return ON_COMPLETED_SENTINEL;
    }

    /**
     * Create a lite {@code onError} notification. This call creates an object to wrap the {@link Throwable},
     * but since there should only be one of these, the performance impact should be small. Can be unwrapped and
     * sent with the {@link #accept} method.
     *
     * @param e
     *           the {@code Throwable} in the {@code onError} notification
     * @return an object encapsulating the exception
     */
    public static Object error(Throwable e) {
        return new OnErrorSentinel(e);
    }

    /**
     * Unwraps the lite notification and calls the appropriate method on the {@link Observer}.
     *
     * @param <T> the value type to accept
     * @param o
     *            the {@link Observer} to call {@code onNext}, {@code onCompleted}, or {@code onError}.
     * @param n
     *            the lite notification
     * @return {@code true} if {@code n} represents a termination event; {@code false} otherwise
     * @throws IllegalArgumentException
     *             if the notification is null.
     * @throws NullPointerException
     *             if the {@link Observer} is null.
     */
    @SuppressWarnings("unchecked")
    public static <T> boolean accept(Observer<? super T> o, Object n) {
        if (n == ON_COMPLETED_SENTINEL) {
            o.onCompleted();
            return true;
        } else if (n == ON_NEXT_NULL_SENTINEL) {
            o.onNext(null);
            return false;
        } else if (n != null) {
            if (n.getClass() == OnErrorSentinel.class) {
                o.onError(((OnErrorSentinel) n).e);
                return true;
            }
            o.onNext((T) n);
            return false;
        } else {
            throw new IllegalArgumentException("The lite notification can not be null");
        }
    }

    /**
     * Indicates whether or not the lite notification represents an {@code onCompleted} event.
     *
     * @param n
     *            the lite notification
     * @return {@code true} if {@code n} represents an {@code onCompleted} event; {@code false} otherwise
     */
    public static boolean isCompleted(Object n) {
        return n == ON_COMPLETED_SENTINEL;
    }

    /**
     * Indicates whether or not the lite notification represents an {@code onError} event.
     *
     * @param n
     *            the lite notification
     * @return {@code true} if {@code n} represents an {@code onError} event; {@code false} otherwise
     */
    public static boolean isError(Object n) {
        return n instanceof OnErrorSentinel;
    }

    /**
     * Indicates whether or not the lite notification represents a wrapped {@code null} {@code onNext} event.
     * @param n the lite notification
     * @return {@code true} if {@code n} represents a wrapped {@code null} {@code onNext} event, {@code false} otherwise
     */
    public static boolean isNull(Object n) {
        return n == ON_NEXT_NULL_SENTINEL;
    }

    /**
     * Indicates whether or not the lite notification represents an {@code onNext} event.
     * @param n the lite notification
     * @return {@code true} if {@code n} represents an {@code onNext} event, {@code false} otherwise
     */
    public static boolean isNext(Object n) {
        return n != null && !isError(n) && !isCompleted(n);
    }

    /**
     * Returns the item corresponding to this {@code OnNext} lite notification. Bad things happen if you pass
     * this an {@code OnComplete} or {@code OnError} notification type. For performance reasons, this method
     * does not check for this, so you are expected to prevent such a mishap.
     *
     * @param <T> the value type to convert
     * @param n
     *            the lite notification (of type {@code Kind.OnNext})
     * @return the unwrapped value, which can be null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValue(Object n) {
        return n == ON_NEXT_NULL_SENTINEL ? null : (T) n;
    }

    /**
     * Returns the {@link Throwable} corresponding to this {@code OnError} lite notification. Bad things happen
     * if you pass this an {@code OnComplete} or {@code OnNext} notification type. For performance reasons, this
     * method does not check for this, so you are expected to prevent such a mishap.
     *
     * @param n
     *            the lite notification (of type {@code Kind.OnError})
     * @return the {@link Throwable} wrapped inside {@code n}
     */
    public static Throwable getError(Object n) {
        return ((OnErrorSentinel) n).e;
    }
}
