package com.dzh.dzhmusicandcamera.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;

import com.dzh.dzhmusicandcamera.R;

/**
 * Date: 2020/11/22
 * author: Dzh
 */
public class BackgroundAnimationRelativeLayout extends RelativeLayout {

  private static final String TAG = "RelativeLayout";

  private final int ANIMATION_DURATION_IN_MILLISECOND = 500;
  private static final int INDEX_BACKGROUND = 0;
  private static final int INDEX_FOREGROUND = 1;

  private LayerDrawable mLayerDrawable;
  private ObjectAnimator mObjectAnimator;

  public BackgroundAnimationRelativeLayout(Context context) {
    this(context, null);
  }

  public BackgroundAnimationRelativeLayout(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public BackgroundAnimationRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initLayerDrawable();
    initObjectAnimator();
  }

  private void initLayerDrawable() {
    Drawable backgroundDrawable = getContext().getDrawable(R.drawable.ic_blackground);
    Drawable[] drawables = new Drawable[2];

    // 初始化时先将前景与北京颜色设为一致
    drawables[INDEX_BACKGROUND] = backgroundDrawable;
    drawables[INDEX_FOREGROUND] = backgroundDrawable;

    mLayerDrawable = new LayerDrawable(drawables);
  }

  private void initObjectAnimator() {
    mObjectAnimator = ObjectAnimator.ofFloat(this, "number", 0f, 1.0f);
    mObjectAnimator.setDuration(ANIMATION_DURATION_IN_MILLISECOND);
    mObjectAnimator.setInterpolator(new AccelerateInterpolator());
    mObjectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        int foregroundAlpha = (int) ((float) animation.getAnimatedValue() * 255);
        // 动态设置 Drawable 透明度， 让前景图片逐渐显示
        mLayerDrawable.getDrawable(INDEX_FOREGROUND).setAlpha(foregroundAlpha);
        BackgroundAnimationRelativeLayout.this.setBackground(mLayerDrawable);
      }
    });

    mObjectAnimator.addListener(new Animator.AnimatorListener() {
      @Override
      public void onAnimationStart(Animator animation) {

      }

      @Override
      public void onAnimationEnd(Animator animation) {
        mLayerDrawable.setDrawable(INDEX_BACKGROUND, mLayerDrawable.getDrawable(INDEX_FOREGROUND));
      }

      @Override
      public void onAnimationCancel(Animator animation) {

      }

      @Override
      public void onAnimationRepeat(Animator animation) {

      }
    });
  }

  @TargetApi(23)
  public void setForeground(Drawable drawable) {
    mLayerDrawable.setDrawable(INDEX_FOREGROUND, drawable);
  }

  // 对外提供方法， 用于开始渐变动画
  public void beginAnimation() {
    mObjectAnimator.start();
  }
}