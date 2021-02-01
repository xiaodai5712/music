package com.dzh.dzhmusicandcamera.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;

public class BitmapUtils {

  // bitmap 顺时针旋转
  public static Bitmap rotate(Bitmap bitmap, float degree) {
    Matrix matrix = new Matrix();
    matrix.postRotate(degree);
    return Bitmap.createBitmap(bitmap, 0, 0
        , bitmap.getWidth(), bitmap.getHeight(), matrix, true);
  }

  // 对 bitmap 做镜像
  public static Bitmap mirror(Bitmap rawBitmap) {
    Matrix matrix = new Matrix();
    matrix.postScale(-1f, 1f);
    return Bitmap.createBitmap(rawBitmap, 0, 0
        , rawBitmap.getWidth(), rawBitmap.getHeight(), matrix, true);
  }
  public static byte[] toByteArray(Bitmap bitmap) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
    return outputStream.toByteArray();
  }

}
