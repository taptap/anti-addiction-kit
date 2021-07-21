package com.tapsdk.antiaddiction.entities.request;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class PlayLogTimesRequestParams {
    @SerializedName("server_times")
    public List<List<Long>> serverTimes = new ArrayList<>();

    @SerializedName("local_times")
    public List<List<Long>> localTimes = new ArrayList<>();
}
