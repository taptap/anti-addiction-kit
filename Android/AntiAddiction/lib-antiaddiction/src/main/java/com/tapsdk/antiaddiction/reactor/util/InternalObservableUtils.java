package com.tapsdk.antiaddiction.reactor.util;

import com.tapsdk.antiaddiction.reactor.exceptions.OnErrorNotImplementedException;
import com.tapsdk.antiaddiction.reactor.functions.Action1;

public enum InternalObservableUtils {
    ;
    /**
     * Throws an OnErrorNotImplementedException when called.
     */
    public static final Action1<Throwable> ERROR_NOT_IMPLEMENTED = new ErrorNotImplementedAction();

    static final class ErrorNotImplementedAction implements Action1<Throwable> {
        @Override
        public void call(Throwable t) {
            throw new OnErrorNotImplementedException(t);
        }
    }
}
