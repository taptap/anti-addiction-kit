package com.tapsdk.antiaddiction.models;

import com.tapsdk.antiaddiction.entities.IdentificationInfo;
import com.tapsdk.antiaddiction.entities.UserInfo;

public class UpdateAccountAction {

    public final UserInfo userInfo;

    public final IdentificationInfo identificationInfo;

    public UpdateAccountAction(UserInfo userInfo, IdentificationInfo identificationInfo) {
        this.userInfo = userInfo;
        this.identificationInfo = identificationInfo;
    }
}
