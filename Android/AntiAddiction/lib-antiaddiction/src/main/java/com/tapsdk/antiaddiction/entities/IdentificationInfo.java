package com.tapsdk.antiaddiction.entities;

import com.google.gson.annotations.SerializedName;

public class IdentificationInfo {

    @SerializedName("identify_state")
    public int authState;

    @SerializedName("id_card")
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
