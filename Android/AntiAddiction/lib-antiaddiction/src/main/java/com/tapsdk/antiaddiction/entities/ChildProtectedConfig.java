package com.tapsdk.antiaddiction.entities;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ChildProtectedConfig implements Serializable {

    @SerializedName("child_common_time")
    public int childCommonTime;

    @SerializedName("child_holiday_time")
    public int childHolidayTime;

    @SerializedName("guest_time")
    public int guestTime;

    @SerializedName("night_strict_start")
    public String nightStrictStart;

    @SerializedName("night_strict_end")
    public String nightStrictEnd;

    @SerializedName("no_identify_time")
    public int noIdentifyTime;

    @SerializedName("remain_time_warn")
    public int remainTimeWarn;

    @SerializedName("night_strict_warn")
    public int nightStrictWarn;

    @SerializedName("upload_all_data")
    public int uploadAllData;
}
