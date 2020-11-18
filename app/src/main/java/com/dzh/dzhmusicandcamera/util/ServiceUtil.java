package com.dzh.dzhmusicandcamera.util;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * Date: 2020/10/31
 * author: Dzh
 */
public class ServiceUtil {
  public static boolean isServiceRunning(Context context, String serviceName) {
    ActivityManager activityManager
        = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningServiceInfo> infoList
        = activityManager.getRunningServices(100);
    for (ActivityManager.RunningServiceInfo info : infoList) {
      String name = info.service.getClassName();
      if (name.equals(serviceName)) {
        return true;
      }
    }
    return false;
  }
}