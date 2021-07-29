package com.taptap.tds.registration.server.dto.rpc;

import com.taptap.tds.registration.server.enums.IdentificationStatus;
import lombok.Data;

@Data
public class IdentificationInfo {

    private IdentificationStatus identifyState;

    private String userId;

    private String idCard;

    private String name;

    private String antiAddictionToken;

}
