package com.taptap.tds.registration.server.core.domain;

import java.time.Instant;

public interface UpdateTimeAwareEntity {

    void setUpdatedAt(Instant updatedAt);
}
