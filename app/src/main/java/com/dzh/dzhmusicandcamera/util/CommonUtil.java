package com.dzh.dzhmusicandcamera.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.ImageReader;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.app.Api;
import com.dzh.dzhmusicandcamera.app.App;
import com.dzh.dzhmusicandcamera.contract.ILocalContract;

/**
 * Date: 2020/10/12
 * author: Dzh
 */
public class CommonUtil {
  private static Toast sToast;

  public static void hideStatusBar(Activity activity, boolean isHide) {
    View decorView = activity.getWindow().getDecorView();
    if (isHide) {
      if (Build.VERSION.SDK_INT >= 22) {
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
      }
    } else {
      decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
      activity.getWindow().setStatusBarColor(App.getContext().getResources()
          .getColor(R.color.actionBarColor));
    }
  }
  public static void showToast(Context context, String message) {
    if (sToast == null) {
      sToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
    } else {
      sToast.setText(message);
    }
    sToast.show();
  }

  public static void setSingerImg(Context context, String singer, ImageView view) {
    if (singer.contains("/")) {
      String[] s = singer.split("/");
      singer = s[0];
    }
    singer = singer.trim();
    String imgUrl = Api.STORAGE_IMG_FILE;
    Glide.with(context).load(imgUrl)
        .apply(RequestOptions.placeholderOf(R.drawable.welcome))
        .apply(RequestOptions.errorOf(R.drawable.welcome))
        .into(view);
  }

  public static void setImgWithGlide(Context context, String imgUrl, ImageView view) {
    Glide.with(context).load(imgUrl)
        .apply(RequestOptions.placeholderOf(R.drawable.welcome))
        .apply(RequestOptions.errorOf(R.drawable.love))
        .into(view);
  }
}
