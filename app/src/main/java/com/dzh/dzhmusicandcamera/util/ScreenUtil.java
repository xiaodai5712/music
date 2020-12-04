package com.dzh.dzhmusicandcamera.util;

import android.content.Context;

/**
 * Date: 2020/12/2
 * author: Dzh
 */
public class ScreenUtil {

  // 根据手机的分辨率 从 dp 转换到 px
  public static int dip2px(Context context, float dpValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dpValue * scale + 0.5f);
  }

  // 根据手机的分辨率从 px 转到 dp
  public static int px2dp(Context context, float pxValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (pxValue / scale + 0.5f);
  }
}