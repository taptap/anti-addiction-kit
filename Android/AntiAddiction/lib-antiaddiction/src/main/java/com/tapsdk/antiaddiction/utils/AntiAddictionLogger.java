package com.tapsdk.antiaddiction.utils;

import android.util.Log;

public class AntiAddictionLogger {

    private static final String TAG = "AntiAddiction";

    private static boolean debuggable = true;

    public static void enableDebug(boolean debug) {
        debuggable = debug;
    }

    public static void d(String message) {
        if (debuggable) {
            Log.d(TAG, "debug ----- message: " + message);
        }
    }

    public static void e(String message) {
        if (debuggable) {
            Log.e(TAG, "error ----- message: " + message );
        }
    }

    public static void w(String message) {
        if (debuggable) {
            Log.w(TAG, "warning ----- message: " + message );
        }
    }

    public static void printStackTrace(Exception e, boolean downgrade) {
        if (debuggable && e != null) {
            if (downgrade) {
                w(e.getMessage());
            } else {
                e(e.getMessage());
            }
        }
    }

    public static void printStackTrace(Exception e) {
        if (debuggable && e != null) {
            printStackTrace(e, false);
        }
    }
}
