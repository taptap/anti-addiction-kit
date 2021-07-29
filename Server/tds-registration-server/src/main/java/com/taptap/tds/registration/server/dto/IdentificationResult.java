package com.taptap.tds.registration.server.dto;

import com.taptap.tds.registration.server.enums.IdentificationStatus;
import lombok.Data;

@Data
public class IdentificationResult {

    private String pi;

    private IdentificationStatus status;

}
