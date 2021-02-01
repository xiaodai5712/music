package com.dzh.dzhmusicandcamera.camera.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

public class CustomView extends View {

  private static final String TAG = "DzhCustomView";

  private int mLastX;
  private int mLastY;

  public CustomView(Context context) {
    super(context);
  }

  public CustomView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    // 获取手指触摸点的 横坐标 纵坐标
    int x = (int) event.getRawX();
    int y = (int) event.getRawY();
    Log.d(TAG, "onTouchEvent: Y = " + y);
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        mLastX = x;
        mLastY = y;
        Log.d(TAG, "onTouchEvent: ActionDown : translationY = " + getTranslationY() + "lastY = " + mLastY);
        break;
      case MotionEvent.ACTION_MOVE:
        int offsetX = x - mLastX;
        int offsetY = y - mLastY;
////        layout(getLeft() + offsetX, getTop() + offsetY, getRight() + offsetX
////            , getBottom() + offsetY);
////        offsetLeftAndRight(offsetX);
////        offsetTopAndBottom(offsetY);
////        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getLayoutParams();
////        params.leftMargin = offsetX + getLeft();
////        params.topMargin = offsetY + getTop();
////        setLayoutParams(params);
//        if (y < getTranslationY()) break;
        Log.d(TAG, "onTouchEvent 执行move 前： y = " + y + ", translationY = " + getTranslationY());
        int preTranslationY = (int) getTranslationY();
        int preTranslationX = (int) getTranslationX();

        setTranslationY(offsetY + preTranslationY);
        setTranslationX(offsetX + preTranslationX);
        Log.d(TAG, "onTouchEvent 执行move 后 y = " + y + ", translationY = " + getTranslationY());
        Log.d(TAG, "onTouchEvent: =================================================================");

        mLastX = x;
        mLastY = y;
        break;
//      case MotionEvent.ACTION_UP:
//        for (int i = 0; i < 500; i++) {
//          setTranslationX(i);
//        }
    }
    performClick();
    return true;
  }
}
