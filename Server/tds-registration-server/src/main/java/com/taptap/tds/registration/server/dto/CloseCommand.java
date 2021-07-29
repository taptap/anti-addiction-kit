package com.taptap.tds.registration.server.dto;

import lombok.Data;

@Data
public class CloseCommand {

    private boolean shouldRetry = false;

    public String toString() {
        return "CloseCommand(shouldRetry=" + this.isShouldRetry() + ")";
    }
}
