package com.tapsdk.antiaddiction.demo.utils;

import android.content.Context;


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
