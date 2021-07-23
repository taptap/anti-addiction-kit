package com.tapsdk.antiaddiction.entities.request;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class IdentifyRequestParams implements Serializable {

    @SerializedName("id_card")
    public String idCard = "";

    @SerializedName("user_id")
    public String token = "";

    @SerializedName("name")
    public String name = "";
}
