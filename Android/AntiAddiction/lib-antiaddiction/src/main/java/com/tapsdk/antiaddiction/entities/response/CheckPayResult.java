package com.tapsdk.antiaddiction.entities.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CheckPayResult implements Serializable {

    @SerializedName("status")
    public boolean status;

    @SerializedName("title")
    public String title;

    @SerializedName("description")
    public String description;
}
