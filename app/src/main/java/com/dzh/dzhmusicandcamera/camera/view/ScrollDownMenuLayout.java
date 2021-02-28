package com.dzh.dzhmusicandcamera.camera.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

/**
 * Date: 2021/2/28
 * author: Dzh
 */
public class ScrollDownMenuLayout extends LinearLayout {

  private static final String TAG = "ScrollDownMenuLayout";
  public ScrollDownMenuLayout(Context context) {
    this(context, null);
  }

  public ScrollDownMenuLayout(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ScrollDownMenuLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  private int mLastY;
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    Log.d(TAG, "onTouchEvent: ");
    int curY = (int) event.getY();
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        mLastY = (int) event.getY();
      case MotionEvent.ACTION_MOVE:
        int offsetY = curY - mLastY;
        scrollBy(0, - offsetY);
        layout(getLeft(), getTop(), getRight(), getBottom() + offsetY);
      case MotionEvent.ACTION_POINTER_DOWN:
        mLastY = curY;
    }
    return true;
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    return true;
  }
}