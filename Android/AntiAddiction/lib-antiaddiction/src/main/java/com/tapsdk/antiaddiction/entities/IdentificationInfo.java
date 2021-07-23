package com.tapsdk.antiaddiction.entities;

import com.google.gson.annotations.SerializedName;

public class IdentificationInfo {

    public int authState;

    public String idCard = "";

    public String name = "";

    public String phoneNumber = "";

    @SerializedName("anti_addiction_token")
    public String antiAddictionToken = "";

    @Override
    public String toString() {
        return "IdentificationInfo{" +
                "authState=" + authState +
                ", idCard='" + idCard + '\'' +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", antiAddictionToken='" + antiAddictionToken + '\'' +
                '}';
    }
}
