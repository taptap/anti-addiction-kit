package com.tapsdk.antiaddiction.reactor.exceptions;

import com.tapsdk.antiaddiction.reactor.Observer;
import com.tapsdk.antiaddiction.reactor.exceptions.CompositeException;
import com.tapsdk.antiaddiction.reactor.exceptions.OnCompletedFailedException;
import com.tapsdk.antiaddiction.reactor.exceptions.OnErrorFailedException;
import com.tapsdk.antiaddiction.reactor.exceptions.OnErrorNotImplementedException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Exceptions {

    private static final int MAX_DEPTH = 25;

    /** Utility class, no instances. */
    private Exceptions() {
        throw new IllegalStateException("No instances!");
    }

    public static void throwIfFatal(Throwable t) {
        if (t instanceof OnErrorNotImplementedException) {
            throw (OnErrorNotImplementedException) t;
        } else if (t instanceof OnErrorFailedException) {
            throw (OnErrorFailedException) t;
        } else if (t instanceof OnCompletedFailedException) {
            throw (OnCompletedFailedException) t;
        }
        // values here derived from https://github.com/ReactiveX/RxJava/issues/748#issuecomment-32471495
        else if (t instanceof VirtualMachineError) {
            throw (VirtualMachineError) t;
        } else if (t instanceof ThreadDeath) {
            throw (ThreadDeath) t;
        } else if (t instanceof LinkageError) {
            throw (LinkageError) t;
        }
    }

    public static void addCause(Throwable e, Throwable cause) {
        Set<Throwable> seenCauses = new HashSet<Throwable>();

        int i = 0;
        while (e.getCause() != null) {
            if (i++ >= MAX_DEPTH) {
                // stack too deep to associate cause
                return;
            }
            e = e.getCause();
            if (seenCauses.contains(e.getCause())) {
                break;
            } else {
                seenCauses.add(e.getCause());
            }
        }
        // we now have 'e' as the last in the chain
        try {
            e.initCause(cause);
        } catch (Throwable t) { // NOPMD
            // ignore
            // the javadocs say that some Throwables (depending on how they're made) will never
            // let me call initCause without blowing up even if it returns null
        }
    }

    public static void throwIfAny(List<? extends Throwable> exceptions) {
        if (exceptions != null && !exceptions.isEmpty()) {
            if (exceptions.size() == 1) {
                Throwable t = exceptions.get(0);
                // had to manually inline propagate because some tests attempt StackOverflowError
                // and can't handle it with the stack space remaining
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else if (t instanceof Error) {
                    throw (Error) t;
                } else {
                    throw new RuntimeException(t); // NOPMD
                }
            }
            throw new CompositeException(exceptions);
        }
    }

    /**
     * Get the {@code Throwable} at the end of the causality-chain for a particular {@code Throwable}
     *
     * @param e
     *         the {@code Throwable} whose final cause you are curious about
     * @return the last {@code Throwable} in the causality-chain of {@code e} (or a "Stack too deep to get
     *         final cause" {@code RuntimeException} if the chain is too long to traverse)
     */
    public static Throwable getFinalCause(Throwable e) {
        int i = 0;
        while (e.getCause() != null) {
            if (i++ >= MAX_DEPTH) {
                // stack too deep to get final cause
                return new RuntimeException("Stack too deep to get final cause");
            }
            e = e.getCause();
        }
        return e;
    }

    public static void throwOrReport(Throwable t, Observer<?> o, Object value) {
        Exceptions.throwIfFatal(t);
        o.onError(OnErrorThrowable.addValueAsLastCause(t, value));
    }

    /**
     * Forwards a fatal exception or reports it to the given Observer.
     * @param t the exception
     * @param o the observer to report to
     * @since 1.3
     */
    public static void throwOrReport(Throwable t, Observer<?> o) {
        Exceptions.throwIfFatal(t);
        o.onError(t);
    }


}
