package com.tapsdk.antiaddiction.constants;

import android.os.Build;

import com.tapsdk.antiaddiction.BuildConfig;

public class Constants {

    public static class API {
        public static final String ANTI_ADDICTION_BASE_URL = BuildConfig.ANTI_ADDICTION_HOST;
        public static final String IDENTIFY_BASE_URL = BuildConfig.IDENTIFICATION_HOST;
        public static final String WEB_SOCKET_HOST = BuildConfig.WEB_SOCKET_HOST;
        public static final String ACCESS_TOKEN_TYPE_BEARER = "Bearer";
    }

    public static class IdentifyState {
        public static final int SUCCESS = 0;
        public static final int VERIFYING = 1;
        public static final int FAIL = 2;
        public static final int UNDEFINED = -1;
    }

    public static class UserType {
        public static final int USER_TYPE_UNKNOWN = 0;
        public static final int USER_TYPE_CHILD = 1;
        public static final int USER_TYPE_TEEN = 2;
        public static final int USER_TYPE_YOUNG = 3;
        public static final int USER_TYPE_ADULT = 4;
        public static final int USER_TYPE_UNREALNAME = 5;
    }

    public static class IdentificationConfig {
        public static final String SECRET_KEY = BuildConfig.IDENTIFICATION_SECRET_KEY;
    }

    public static class CacheData {
        public static final String TIMING_FILE_SUFFIX = "timing_file_suffix";
        public static final String TIMING_SHARED_PREFERENCE_NAME = "timing_shared_preference_name";
    }
}