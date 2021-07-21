package com.tapsdk.antiaddiction.entities;

public class ThreeTuple<U,V,W> extends TwoTuple<U,V>{
    public final W thirdParam;

    public ThreeTuple(U firstParam, V secondParam, W thirdParam) {
        super(firstParam, secondParam);
        this.thirdParam = thirdParam;
    }

    public static <U, V, W> ThreeTuple<U, V, W> create(U u, V v, W w) {
        return new ThreeTuple<>(u, v, w);
    }

}
