package com.tapsdk.antiaddiction.entities.request;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PlayLogRequestParams implements Serializable {

    @SerializedName("game")
    public String game = "";

    @SerializedName("is_login")
    public int login = 0;

    @SerializedName("play_logs")
    public PlayLogTimesRequestParams playLogs = new PlayLogTimesRequestParams();
}
