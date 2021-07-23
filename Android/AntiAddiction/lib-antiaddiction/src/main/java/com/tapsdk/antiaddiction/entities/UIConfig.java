package com.tapsdk.antiaddiction.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UIConfig {

    @SerializedName("health_reminder_words")
    public List<HealthPromptGroup> healthPromptGroups;
}
