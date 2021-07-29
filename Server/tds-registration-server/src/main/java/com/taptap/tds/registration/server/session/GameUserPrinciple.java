package com.taptap.tds.registration.server.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @Author guyu
 * @create 2021/1/6 10:59 上午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameUserPrinciple {

    private String gameId;

    private String userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameUserPrinciple that = (GameUserPrinciple) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(gameId, that.gameId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, gameId);
    }
}
