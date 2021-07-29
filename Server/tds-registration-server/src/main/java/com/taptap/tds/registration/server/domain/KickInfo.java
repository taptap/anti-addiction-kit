package com.taptap.tds.registration.server.domain;

import com.taptap.tds.registration.server.session.GameUserPrinciple;
import lombok.Data;

@Data
public class KickInfo {

    private String serverId;
    private GameUserPrinciple gameUserPrinciple;
}
