package com.tapsdk.antiaddiction.config;

public class AntiAddictionFunctionConfig {

    public final boolean usePaymentLimit;

    public final boolean useOnLineTimeLimit;

    public final String identityVerifiedServerUrl;

    public final String antiAddictionServerUrl;

    public final String departmentWebSocketUrl;

    public boolean onLineTimeLimitEnabled() {
        return useOnLineTimeLimit;
    }

    public boolean paymentLimitEnabled() {
        return usePaymentLimit;
    }

    private AntiAddictionFunctionConfig() {
        this.usePaymentLimit = false;
        this.useOnLineTimeLimit = false;
        this.identityVerifiedServerUrl = "";
        this.antiAddictionServerUrl = "";
        this.departmentWebSocketUrl = "";
    }

    public AntiAddictionFunctionConfig(Builder builder) {
        this.usePaymentLimit = builder.usePaymentLimit;
        this.useOnLineTimeLimit = builder.useOnLineTimeLimit;
        this.identityVerifiedServerUrl = builder.identityVerifiedServerUrl;
        this.antiAddictionServerUrl = builder.antiAddictionServerUrl;
        this.departmentWebSocketUrl = builder.departmentWebSocketUrl;
    }

    public static class Builder {
        public boolean usePaymentLimit = true;
        public boolean useOnLineTimeLimit = true;
        public String identityVerifiedServerUrl = "";
        public String antiAddictionServerUrl = "";
        public String departmentWebSocketUrl = "";

        public Builder enablePaymentLimit(boolean enabled) {
            this.useOnLineTimeLimit = enabled;
            return this;
        }

        public Builder enableOnLineTimeLimit(boolean enabled) {
            this.useOnLineTimeLimit = enabled;
            return this;
        }

        public Builder withIdentifyVerifiedServerUrl(String identityVerifiedServerUrl) {
            this.identityVerifiedServerUrl = identityVerifiedServerUrl;
            return this;
        }

        public Builder withAntiAddictionServerUrl(String antiAddictionServerUrl) {
            this.antiAddictionServerUrl = antiAddictionServerUrl;
            return this;
        }

        public Builder withDepartmentSocketUrl(String departmentSocketUrl) {
            this.departmentWebSocketUrl = departmentSocketUrl;
            return this;
        }

        public AntiAddictionFunctionConfig build() {
            return new AntiAddictionFunctionConfig(this);
        }
    }
}
