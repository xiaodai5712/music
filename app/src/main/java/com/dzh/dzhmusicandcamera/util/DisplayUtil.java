package com.dzh.dzhmusicandcamera.util;

import android.content.Context;

/**
 * Date: 2020/11/24
 * author: Dzh
 */
public class DisplayUtil {
  // 手柄起始角度
  public static final float ROTATION_INIT_NEEDLE = -30;

  // 截图屏幕宽高
  private static final float BASE_SCREEN_WIDTH = 1080.0f;
  private static final float BASE_SCREEN_HEIGHT = 1920.0f;

  // 唱针宽高 距离等比例
  public static final float SCALE_NEEDLE_WIDTH = (276.0f / BASE_SCREEN_WIDTH);
  public static final float SCALE_NEEDLE_MARGIN_LEFT = (500.0f / BASE_SCREEN_WIDTH);
  public static final float SCALE_NEEDLE_PIVOT_X = (43.0f / BASE_SCREEN_WIDTH);
  public static final float SCALE_NEEDLE_PIVOT_Y = (43.0f / BASE_SCREEN_WIDTH);
  public static final float SCALE_NEEDLE_HEIGHT = (413.0f / BASE_SCREEN_HEIGHT);
  public static final float SCALE_NEEDLE_MARGIN_TOP = (43.0f / BASE_SCREEN_HEIGHT);

  //  唱盘比例
  public static final float SCALE_DISC_SIZE = (813.0f / BASE_SCREEN_WIDTH);
  public static final float SCALE_DISC_MARGIN_TOP = (190f / BASE_SCREEN_HEIGHT);

  // 专辑图片比例
  public static final float SCALE_MUSIC_PIC_SIZE = (533.0f / BASE_SCREEN_WIDTH);

  //设备屏幕宽度
  public static int getScreenWidth(Context context) {
    return context.getResources().getDisplayMetrics().widthPixels;
  }

  //*设备屏幕高度
  public static int getScreenHeight(Context context) {
    return context.getResources().getDisplayMetrics().heightPixels;
  }
}