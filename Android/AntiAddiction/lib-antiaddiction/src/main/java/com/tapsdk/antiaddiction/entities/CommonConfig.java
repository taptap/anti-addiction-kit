package com.tapsdk.antiaddiction.entities;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CommonConfig implements Serializable {

    @SerializedName("child_protected_config")
    public ChildProtectedConfig childProtectedConfig;

    @SerializedName("ui_config")
    public UIConfig uiConfig;

    @SerializedName("holiday")
    public List<String> holidayList;
}
