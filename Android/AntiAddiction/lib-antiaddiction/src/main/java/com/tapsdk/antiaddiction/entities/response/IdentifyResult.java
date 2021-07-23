package com.tapsdk.antiaddiction.entities.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class IdentifyResult implements Serializable {

    @SerializedName("identify_state")
    public int identifyState;

    @Override
    public String toString() {
        return "IdentifyResult{" +
                "identifyState=" + identifyState +
                '}';
    }
}
