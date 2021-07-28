package com.tapsdk.antiaddiction.reactor.functions;

public final class Actions {
    @SuppressWarnings("rawtypes")
    private static final EmptyAction EMPTY_ACTION = new EmptyAction();

    private Actions() {
        throw new IllegalStateException("No instances!");
    }

    static final class EmptyAction<T0, T1, T2> implements
            Action0,
            Action1<T0>,
            Action2<T0, T1>,
            Action3<T0, T1, T2> {
        @Override
        public void call() {

        }

        @Override
        public void call(T0 t0) {

        }

        @Override
        public void call(T0 t0, T1 t1) {

        }

        @Override
        public void call(T0 t0, T1 t1, T2 t2) {

        }
    }

    @SuppressWarnings("unchecked")
    public static <T0, T1, T2> EmptyAction<T0, T1, T2> empty() {
        return EMPTY_ACTION;
    }
}
