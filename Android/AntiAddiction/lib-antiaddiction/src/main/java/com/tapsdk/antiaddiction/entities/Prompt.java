package com.tapsdk.antiaddiction.entities;

import com.google.gson.annotations.SerializedName;

public class Prompt {

    @SerializedName("type")
    public int type;

    @SerializedName("title")
    public String title;

    @SerializedName("description")
    public String description;
}
