package com.tapsdk.antiaddiction.models;

public class UpdateAntiAddictionInfoAction {

    public final long serverTimeInSeconds;

    public final long remainTime;

    public final boolean playing;

    public UpdateAntiAddictionInfoAction(long serverTimeInSeconds, long remainTime, boolean playing) {
        this.serverTimeInSeconds = serverTimeInSeconds;
        this.remainTime = remainTime;
        this.playing = playing;
    }
}
