package com.dzh.dzhmusicandcamera.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ImageReader;
import android.os.Build;
import android.text.Html;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
    String imgUrl = Api.STORAGE_IMG_FILE + singer + ".jpg";
    Glide.with(context)
        .load(imgUrl)
        .apply(RequestOptions.placeholderOf(R.drawable.welcome))
        .apply(RequestOptions.errorOf(R.drawable.welcome))
        .into(view);
  }

  public static void setImgWithGlide(Context context, String imgUrl, ImageView view) {
    Glide.with(context)
        .load(imgUrl)
        .apply(RequestOptions.placeholderOf(R.drawable.welcome))
        .apply(RequestOptions.errorOf(R.drawable.love))
        .into(view);
  }

  // 得到屏幕的宽度
  public static int getScreenWidth(Context context) {
    if (context == null) {
      return 0;
    }
    return context.getResources().getDisplayMetrics().widthPixels;
  }

  // 得到屏幕的高度
  public static int getScreenHeight(Context context) {
    if (context == null) {
      return 0;
    }
    return context.getResources().getDisplayMetrics().heightPixels;
  }

  // 高斯模糊
  public static Drawable getForegroundDrawable(Bitmap bitmap) {
    // 得到屏幕的宽高比
    final float widthHeightRatio = (float) (DisplayUtil.getScreenWidth(App.getContext()) * 1.0
        / DisplayUtil.getScreenHeight(App.getContext()) * 1.0);
    int cropBitmapWidth = (int) (widthHeightRatio * bitmap.getHeight());
    int cropBitmapWidthX = (int) ((bitmap.getWidth() - cropBitmapWidth) / 2.0);
    // 切割部分图片
    Bitmap cropBitmap
        = Bitmap.createBitmap(bitmap, cropBitmapWidthX, 0, cropBitmapWidth, bitmap.getHeight());
    // 缩小图片
    Bitmap scaleBitmap = Bitmap.createScaledBitmap(cropBitmap, bitmap.getWidth() / 50
        , bitmap.getHeight() / 50, false);
    // 模糊化
    final Bitmap blurBitmap = FastBlurUtil.doBlur(scaleBitmap, 3, true);
    final Drawable foregroundDrawable = new BitmapDrawable(blurBitmap);
    // 加入灰色遮罩层， 避免图标过量影响其他控件
    return foregroundDrawable;
  }

  // EditText获取焦点，并弹出软键盘
  public static void showKeyboard(EditText editText, Context context) {
    // editText为dialog中输入框的EditText
    if (editText != null) {
      // 设置可获得焦点
      editText.setFocusable(true);
      editText.setFocusableInTouchMode(true);
      // 请求获得焦点
      editText.requestFocus();
      InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
      inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }
  }

  // 关闭软键盘
  public static void closeKeyboard(EditText editText, Context context) {
    editText.clearFocus();
    InputMethodManager manager
        = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    manager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
  }

  // 使指定的字符串显示不同的颜色
  public static void showStringColor(String appointStr, String originalStr, TextView textView) {
    originalStr = originalStr.replaceAll(appointStr
        , "<font color='#FFC66D'>" + appointStr + "</font>");
    textView.setText(Html.fromHtml(originalStr));
  }
}
