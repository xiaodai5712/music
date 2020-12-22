package com.dzh.dzhmusicandcamera.util;

import android.annotation.SuppressLint;

/**
 * Date: 2020/11/30
 * author: Dzh
 */
public class MediaUtil {

  public static String formatTime(long time) {
    String min = time / 60 + "";
    String sec = time % 60 + "";  // dzh! 这里把 % 写成了 / 导致seekBar的秒数显示错误
    if (sec.length() < 2) {
      sec = "0" + sec;
    }
    return min + ":" + sec;
  }

  public static String formatSinger(String singer) {
    if (singer.contains("/")) {
      String [] s = singer.split("/");
      singer = s[0];
    }
    return singer.trim();
  }

  @SuppressLint("DefaultLocale")
  public static String formatSize(long size) {
    double d = (double) size / (1024 << 10);
    return String.format("%.1f", d);
  }
}