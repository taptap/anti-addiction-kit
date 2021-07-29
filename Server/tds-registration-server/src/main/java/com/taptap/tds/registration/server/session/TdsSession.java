package com.taptap.tds.registration.server.session;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class TdsSession {

    private GameUserPrinciple gameUserPrinciple;

    private String id;

    private Long creationTime;

    private String serverId;

    public TdsSession(GameUserPrinciple gameUserPrinciple){
        this.gameUserPrinciple = gameUserPrinciple;
    }

    public TdsSession generateId() {
        this.id = UUID.randomUUID().toString();
        return this;
    }

    public static TdsSession create(GameUserPrinciple gameUserPrinciple){
        return new TdsSession(gameUserPrinciple);
    }

    public static TdsSession createWithUUID(GameUserPrinciple gameUserPrinciple){
        return create(gameUserPrinciple).generateId();
    }


}