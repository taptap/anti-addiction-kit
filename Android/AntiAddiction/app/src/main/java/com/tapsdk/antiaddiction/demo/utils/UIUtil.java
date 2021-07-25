package com.tapsdk.antiaddiction.demo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;


public class UIUtil {

  public static int dp2px(Context context, float dipValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dipValue * scale);
  }

  public static int dp2pxWithScale(float scale, float dipValue) {
    return (int) (dipValue * scale);
  }

  public static int sp2px(Context context, float spValue) {
    float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
    return (int) (spValue * fontScale + 0.5f);
  }
}
