package com.taptap.tds.registration.server.domain;

import com.taptap.tds.registration.server.core.domain.BaseEntity;
import com.taptap.tds.registration.server.enums.ActionType;
import com.taptap.tds.registration.server.enums.UserType;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.Instant;

@Entity
public class UserAction extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private UserType userType;

    private String sessionId;

    @Column(nullable = false)
    private ActionType actionType;

    private String deviceId;

    private String pi;

    private boolean pushSuccess = false;

    @Column(nullable = false)
    private Instant actionTime;

    public UserType getUserType() {
        return this.userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public ActionType getActionType() {
        return this.actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public boolean isPushSuccess() {
        return this.pushSuccess;
    }

    public void setPushSuccess(boolean pushSuccess) {
        this.pushSuccess = pushSuccess;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Instant getActionTime() {
        return this.actionTime;
    }

    public void setActionTime(Instant actionTime) {
        this.actionTime = actionTime;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getPi() {
        return pi;
    }

    public void setPi(String pi) {
        this.pi = pi;
    }

}
