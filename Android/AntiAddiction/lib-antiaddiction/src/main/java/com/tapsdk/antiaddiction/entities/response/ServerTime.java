package com.tapsdk.antiaddiction.entities.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ServerTime implements Serializable {
    @SerializedName("timestamp")
    public long timestamp;
}
