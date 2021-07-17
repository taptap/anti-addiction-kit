package com.tapsdk.antiaddiction.reactor.exceptions;

public class OnErrorNotImplementedException extends RuntimeException {
    private static final long serialVersionUID = -6298857009889503852L;

    /**
     * Customizes the {@code Throwable} with a custom message and wraps it before it is to be re-thrown as an
     * {@code OnErrorNotImplementedException}.
     *
     * @param message
     *          the message to assign to the {@code Throwable} to re-throw
     * @param e
     *          the {@code Throwable} to re-throw; if null, a NullPointerException is constructed
     */
    public OnErrorNotImplementedException(String message, Throwable e) {
        super(message, e != null ? e : new NullPointerException());
    }

    /**
     * Wraps the {@code Throwable} before it is to be re-thrown as an {@code OnErrorNotImplementedException}.
     *
     * @param e
     *          the {@code Throwable} to re-throw; if null, a NullPointerException is constructed
     */
    public OnErrorNotImplementedException(Throwable e) {
        super(e != null ? e.getMessage() : null, e != null ? e : new NullPointerException());
    }
}
