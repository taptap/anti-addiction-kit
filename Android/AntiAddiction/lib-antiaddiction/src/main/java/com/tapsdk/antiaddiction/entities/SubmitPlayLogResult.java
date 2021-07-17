package com.tapsdk.antiaddiction.entities;

import com.google.gson.annotations.SerializedName;

public class SubmitPlayLogResult {

    @SerializedName("restrictType")
    public int restrictType;

    @SerializedName("remainTime")
    public int remainTime;

    @SerializedName("code")
    public int code;

    @SerializedName("title")
    public String title;

    @SerializedName("description")
    public String description;

    @SerializedName("costTime")
    public int costTime;

    /**
     * 用来区分是用户检测还是上报时长
     */
    public boolean checkUser;

}
