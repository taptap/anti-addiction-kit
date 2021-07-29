package com.taptap.tds.registration.server.dto.rpc;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class TdsIdentificationRequest {

    @Size(max = 32)
    @NotBlank
    private String userId;

    @NotBlank
    private String name;

    @NotBlank
    @Size(min = 18, max = 18)
    private String idCard;

}