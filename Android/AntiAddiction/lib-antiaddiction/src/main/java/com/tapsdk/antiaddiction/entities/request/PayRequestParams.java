package com.tapsdk.antiaddiction.entities.request;

public class PayRequestParams {

    public final String game;

    public final long amount;

    public PayRequestParams(long amount, String game) {
        this.game = game;
        this.amount = amount;
    }
}
