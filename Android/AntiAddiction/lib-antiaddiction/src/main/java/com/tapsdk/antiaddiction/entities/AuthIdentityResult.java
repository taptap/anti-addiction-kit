package com.tapsdk.antiaddiction.entities;

import com.google.gson.annotations.SerializedName;
import com.tapsdk.antiaddiction.constants.Constants;

public class AuthIdentityResult {

    @SerializedName("identify_state")
    public int authState = Constants.IdentifyState.UNDEFINED;
}
