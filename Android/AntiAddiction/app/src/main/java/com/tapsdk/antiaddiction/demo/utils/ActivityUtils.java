package com.tapsdk.antiaddiction.demo.utils;

import android.app.Activity;

public class ActivityUtils {
    public static boolean isActivityNotAlive(Activity activity) {
        return activity == null || activity.isFinishing() || activity.isDestroyed();
    }

    public static boolean isActivityAlive(Activity activity) {
        return !isActivityNotAlive(activity);
    }
}
