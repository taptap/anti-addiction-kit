package com.tapsdk.antiaddiction.models;

public class AntiAddictionLimitInfoAction {

    public final boolean canPlay;

    public final int strictType;

    public final boolean loggedIn;

    public AntiAddictionLimitInfoAction(boolean canPlay, int strictType, boolean loggedIn) {
        this.canPlay = canPlay;
        this.strictType = strictType;
        this.loggedIn = loggedIn;
    }
}
