package com.tapsdk.antiaddiction;

import java.util.Map;

public interface AntiAddictionCallback {
    void onCallback(int code, Map<String, Object> extras);
}
