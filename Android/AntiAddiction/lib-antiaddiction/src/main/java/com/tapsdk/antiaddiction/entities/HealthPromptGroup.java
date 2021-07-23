package com.tapsdk.antiaddiction.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HealthPromptGroup {

    @SerializedName("account_type")
    public int accountType;

    @SerializedName("tips")
    public List<Prompt> promptList;
}
