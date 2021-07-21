package com.tapsdk.antiaddiction.entities;

public class TwoTuple<U,V> {

    public final U firstParam;
    public final V secondParam;

    public TwoTuple(U firstParam, V secondParam) {
        this.firstParam = firstParam;
        this.secondParam = secondParam;
    }

    public static <U, V> TwoTuple<U, V> create(U u, V v) {
        return new TwoTuple<>(u, v);
    }
}
