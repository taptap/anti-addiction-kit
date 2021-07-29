package com.taptap.tds.registration.server.dto;

import com.taptap.tds.registration.server.enums.ActionType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class UserActionRequest {

    @Size(max =32)
    @NotBlank
    private String userId;

//    @Size(max =32)
//    @NotBlank
//    private String gameId;

    @NotNull
    private ActionType loginflag;

    @Size(max = 32)
    @NotNull
    private String sessionId;

}
