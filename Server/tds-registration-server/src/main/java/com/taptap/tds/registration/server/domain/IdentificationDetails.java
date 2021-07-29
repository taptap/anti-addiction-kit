package com.taptap.tds.registration.server.domain;

import com.taptap.tds.registration.server.core.domain.BaseEntity;
import com.taptap.tds.registration.server.enums.IdentificationStatus;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class IdentificationDetails extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String ai;

    private String pi;

    private String name;

    private String idCard;

    @Column(nullable = false)
    private IdentificationStatus status;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAi() {
        return ai;
    }

    public void setAi(String ai) {
        this.ai = ai;
    }

    public String getPi() {
        return pi;
    }

    public void setPi(String pi) {
        this.pi = pi;
    }

    public IdentificationStatus getStatus() {
        return status;
    }

    public void setStatus(IdentificationStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

}
