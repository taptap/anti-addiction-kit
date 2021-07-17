package com.tapsdk.antiaddiction.config;

public class AntiAddictionFunctionConfig {

    private boolean usePaymentLimit;

    private boolean useOnLineTimeLimit;

    private AntiAddictionFunctionConfig() {

    }

    public AntiAddictionFunctionConfig(Builder builder) {
        this.usePaymentLimit = builder.usePaymentLimit;
        this.useOnLineTimeLimit = builder.useOnLineTimeLimit;
    }

    public static class Builder {
        public boolean usePaymentLimit = true;
        public boolean useOnLineTimeLimit = true;

        public Builder enablePaymentLimit(boolean enabled) {
            this.useOnLineTimeLimit = enabled;
            return this;
        }

        public Builder enableOnLineTimeLimit(boolean enabled) {
            this.useOnLineTimeLimit = enabled;
            return this;
        }

        public AntiAddictionFunctionConfig build() {
            return new AntiAddictionFunctionConfig(this);
        }
    }
}
