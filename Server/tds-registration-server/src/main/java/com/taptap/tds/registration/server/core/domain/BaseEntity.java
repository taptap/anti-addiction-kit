package com.taptap.tds.registration.server.core.domain;


import javax.persistence.Column;
import javax.persistence.Version;
import java.time.Instant;


public class BaseEntity extends IdEntity
        implements CreateTimeAwareEntity, UpdateTimeAwareEntity {

    private static final long serialVersionUID = 3244321729290025058L;

    @Column(insertable = false, updatable = false)
    private Instant createdAt;

    @Version
    @Column(insertable = false, updatable = false)
    private Instant updatedAt;

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
