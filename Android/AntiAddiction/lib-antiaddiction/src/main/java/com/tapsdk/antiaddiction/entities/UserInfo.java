package com.tapsdk.antiaddiction.entities;

import com.google.gson.annotations.SerializedName;
import com.tapsdk.antiaddiction.constants.Constants;

import java.io.Serializable;

public class UserInfo implements Serializable {

    @SerializedName("code")
    public String code;

    @SerializedName("user_id")
    public String userId;

    @SerializedName("type")
    public int accountType = Constants.UserType.USER_TYPE_UNKNOWN;

    public int remainTime = 0;

    public long saveTimeStamp = 0L;

    @SerializedName("access_token")
    public String accessToken;

    public String game;

    public void updateRemainTime(int time){
        remainTime -=time;
    }

    public void resetRemainTime(int time){
        remainTime = time;
    }

    public UserInfo clone() {
        UserInfo userInfo = new UserInfo();
        userInfo.code = this.code;
        userInfo.userId = this.userId;
        userInfo.accountType = this.accountType;
        userInfo.remainTime = this.remainTime;
        userInfo.saveTimeStamp = this.saveTimeStamp;
        userInfo.accessToken = this.accessToken;
        userInfo.game = this.game;
        return userInfo;
    }
}
