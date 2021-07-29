package com.taptap.tds.registration.server.core.domain;

import java.time.Instant;


public interface CreateTimeAwareEntity {

    void setCreatedAt(Instant createdAt);
}
