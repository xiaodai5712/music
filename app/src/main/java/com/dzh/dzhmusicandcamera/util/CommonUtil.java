package com.dzh.dzhmusicandcamera.util;

import android.content.Context;
import android.media.ImageReader;
import android.widget.Toast;

/**
 * Date: 2020/10/12
 * author: Dzh
 */
public class CommonUtil {
  private static Toast sToast;

  public static void showToast(Context context, String message) {
    if (context == null) {
      sToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
    } else {
      sToast.setText(message);
    }
    sToast.show();
  }
}
