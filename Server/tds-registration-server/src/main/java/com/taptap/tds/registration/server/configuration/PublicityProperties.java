package com.taptap.tds.registration.server.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tds.publicity")
public class PublicityProperties {

    private String bizId;
    private String secretKey = "1234567890123456";
    private int userActionBatchSize = 5;
    private String appId;
    private String signKey;
    private String identificationRootUri;
    private String identificationCheckRootUri;
    private String userActionRootRui;
//    private Map<String, String> gameIdBizIdMapping =   new ConcurrentHashMap<>();
    private boolean removeAfterUpload = false;

    public String getSignKey() {
        return signKey;
    }

    public void setSignKey(String signKey) {
        this.signKey = signKey;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getIdentificationRootUri() {
        return identificationRootUri;
    }

    public void setIdentificationRootUri(String identificationRootUri) {
        this.identificationRootUri = identificationRootUri;
    }

    public String getIdentificationCheckRootUri() {
        return identificationCheckRootUri;
    }

    public void setIdentificationCheckRootUri(String identificationCheckRootUri) {
        this.identificationCheckRootUri = identificationCheckRootUri;
    }

    public String getUserActionRootRui() {
        return userActionRootRui;
    }

    public void setUserActionRootRui(String userActionRootRui) {
        this.userActionRootRui = userActionRootRui;
    }

//    public Map<String, String> getGameIdBizIdMapping() {
//        return gameIdBizIdMapping;
//    }
//
//    public void setGameIdBizIdMapping(Map<String, String> gameIdBizIdMapping) {
//        this.gameIdBizIdMapping = gameIdBizIdMapping;
//    }
    public boolean isRemoveAfterUpload() {
        return removeAfterUpload;
    }

    public void setRemoveAfterUpload(boolean removeAfterUpload) {
        this.removeAfterUpload = removeAfterUpload;
    }

    public int getUserActionBatchSize() {
        return userActionBatchSize;
    }

    public void setUserActionBatchSize(int userActionBatchSize) {
        this.userActionBatchSize = userActionBatchSize;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }
}
