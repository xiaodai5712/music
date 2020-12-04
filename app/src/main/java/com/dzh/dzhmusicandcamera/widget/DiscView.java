package com.dzh.dzhmusicandcamera.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.util.CommonUtil;
import com.dzh.dzhmusicandcamera.util.DisplayUtil;

/**
 * Date: 2020/11/24
 * author: Dzh
 */
public class DiscView extends RelativeLayout {

  private static final String TAG = "DiscView";
  public static final int DURATION_NEEDLE_ANIMATOR = 500;

  private ImageView mIvNeedle;

  private ObjectAnimator mNeedleAnimator;
  private ObjectAnimator mObjectorAnimator;

  // 标记 ViewPager是否处于偏移的状态
  private boolean mViewPagerIsOffset = false;

  // 标记唱针复位后，是否刷要重新偏移到唱片处
  private boolean mIsNeedStartPlayAnimator = false;
  private MusicStatus mMusicStatus = MusicStatus.STOP;
  private NeedleAnimatorStatus mNeedleAnimatorStatus = NeedleAnimatorStatus.IN_FAR_END;

  private int mScreenWidth, mScreenHeight;


  public DiscView(Context context) {
    this(context, null);
  }

  public DiscView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public DiscView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mScreenWidth = CommonUtil.getScreenWidth(context);
    mScreenHeight = CommonUtil.getScreenHeight(context);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    initDiscImg();
    initNeedle();
    initObjectAnimator();
  }

  private void initDiscImg() {
    ImageView discBackground = findViewById(R.id.iv_disc_background);
    mObjectorAnimator = getDiscObjectAnimator(discBackground);
    discBackground.setImageDrawable(
        getDiscDrawable(
            BitmapFactory.decodeResource(getResources(), R.drawable.default_disc)));
    int marginTop = (int) (DisplayUtil.SCALE_DISC_MARGIN_TOP * mScreenHeight);
    LayoutParams layoutParams = (LayoutParams) discBackground.getLayoutParams();
    layoutParams.setMargins(0, marginTop, 0, 0);
    discBackground.setLayoutParams(layoutParams);
  }

  private void initNeedle() {
    mIvNeedle = findViewById(R.id.iv_needle);

    int needleWidth = (int) (DisplayUtil.SCALE_NEEDLE_WIDTH * mScreenHeight  );
    int needleHeight = (int) (DisplayUtil.SCALE_NEEDLE_HEIGHT * mScreenHeight);

    // 设置手柄的外边距为负数， 让其隐藏一部分
    int marginTop = (int) (DisplayUtil.SCALE_NEEDLE_MARGIN_TOP * mScreenHeight) * -1;
    int marginLeft = (int) (DisplayUtil.SCALE_NEEDLE_MARGIN_LEFT * mScreenWidth);

    Bitmap originBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_needle);
    Bitmap bitmap = Bitmap.createScaledBitmap(originBitmap, needleWidth, needleHeight, false);
    LayoutParams layoutParams = (LayoutParams) mIvNeedle.getLayoutParams();
    layoutParams.setMargins(marginLeft, marginTop, 0, 0);
    int pivotX = (int) (DisplayUtil.SCALE_NEEDLE_PIVOT_X * mScreenWidth);
    int pivotY = (int) (DisplayUtil.SCALE_NEEDLE_PIVOT_Y * mScreenWidth);

    mIvNeedle.setPivotX(pivotX);
    mIvNeedle.setPivotY(pivotY);
    mIvNeedle.setRotation(DisplayUtil.ROTATION_INIT_NEEDLE);
    mIvNeedle.setImageBitmap(bitmap);
    mIvNeedle.setLayoutParams(layoutParams);

  }

  private void initObjectAnimator() {
    mNeedleAnimator = ObjectAnimator.ofFloat(mIvNeedle, View.ROTATION,
        DisplayUtil.ROTATION_INIT_NEEDLE, 0);
    mNeedleAnimator.setDuration(DURATION_NEEDLE_ANIMATOR);
    mNeedleAnimator.setInterpolator(new AccelerateInterpolator());
    mNeedleAnimator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationStart(Animator animation) {
        // 根据动画开始前 NeedleAnimatorStatus的状态即可得出动画进行时NeedleAnimatorsStatus的状态
        if (mNeedleAnimatorStatus == NeedleAnimatorStatus.IN_FAR_END) {
          mNeedleAnimatorStatus = NeedleAnimatorStatus.TO_NEAR_END;
        } else if (mNeedleAnimatorStatus == NeedleAnimatorStatus.IN_NEAR_END) {
          mNeedleAnimatorStatus = NeedleAnimatorStatus.TO_FAR_END;
        }
      }

      @Override
      public void onAnimationEnd(Animator animation) {
        if (mNeedleAnimatorStatus == NeedleAnimatorStatus.TO_NEAR_END) {
          mNeedleAnimatorStatus = NeedleAnimatorStatus.IN_NEAR_END;
          playDiscAnimator();
          mMusicStatus = MusicStatus.PLAY;
        } else if (mNeedleAnimatorStatus == NeedleAnimatorStatus.TO_FAR_END) {
          mNeedleAnimatorStatus = NeedleAnimatorStatus.IN_FAR_END;
          if (mMusicStatus == MusicStatus.STOP) {
            mIsNeedStartPlayAnimator = true;
          }
        }
        if (mIsNeedStartPlayAnimator) {
          mIsNeedStartPlayAnimator = false;
          // 只有在 ViewPager 不处于偏移状态时， 唱盘才开始旋转动画
          if (!mViewPagerIsOffset) {
            DiscView.this.postDelayed(() -> {
              playAnimator();
            }, 50);
          }
        }
      }
    });

  }

  // 得到唱片图片， 常盘图片有空心圆盘及音乐专辑图标 合成 得到
  public Drawable getDiscDrawable(Bitmap bitmap) {
    int discSize = (int) (mScreenWidth * DisplayUtil.SCALE_DISC_SIZE);
    int musicPicSize = (int) (mScreenWidth * DisplayUtil.SCALE_MUSIC_PIC_SIZE);

    Bitmap bitmapDisc = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources()
        , R.drawable.ic_disc), discSize, discSize, false);
    Bitmap bitmapMusicPic = Bitmap.createScaledBitmap(bitmap, musicPicSize, musicPicSize, true);
    // dzh 这里使用了 文档建议的方法，可能会出问题
    BitmapDrawable discDrawable = new BitmapDrawable(getResources(), bitmapDisc);
    RoundedBitmapDrawable roundMusicDrawable = RoundedBitmapDrawableFactory
        .create(getResources(), bitmapDisc);

    // 抗锯齿
    discDrawable.setAntiAlias(true);
    roundMusicDrawable.setAntiAlias(true);
    Drawable[] drawables = new Drawable[2];
    drawables[0] = roundMusicDrawable;
    drawables[1] = discDrawable;

    LayerDrawable layerDrawable = new LayerDrawable(drawables);
    int musicPicMargin = (int) ((DisplayUtil.SCALE_DISC_SIZE - DisplayUtil.SCALE_MUSIC_PIC_SIZE)
        * mScreenWidth / 2);
    // 调整专辑图片的四周边距， 让其剧中显示
    layerDrawable.setLayerInset(0, musicPicMargin, musicPicMargin, musicPicMargin
        , musicPicMargin);
    return layerDrawable;
  }

  private ObjectAnimator getDiscObjectAnimator(ImageView disc) {
    ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(disc, View.ROTATION, 0, 360);
    objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
    objectAnimator.setDuration(30 * 1000);
    return objectAnimator;
  }

  // 播放唱盘动画
  private void playDiscAnimator() {
    if (mObjectorAnimator.isPaused()) {
      mObjectorAnimator.resume();
    } {
      mObjectorAnimator.start();
    }
  }

  // 播放动画
  private void playAnimator() {
    // 唱针处于远端时， 直接播放动画
    if (mNeedleAnimatorStatus == NeedleAnimatorStatus.IN_FAR_END) {
      mNeedleAnimator.start();
    }
    // 唱针处于往远端移动时， 设置标记，等动画结束画在播放动画
    else if (mNeedleAnimatorStatus == NeedleAnimatorStatus.TO_FAR_END) {
      mIsNeedStartPlayAnimator = true;
    }
  }

  // 暂停动画
  private void pauseAnimator() {
    // 播放时暂停动画
    if (mNeedleAnimatorStatus == NeedleAnimatorStatus.IN_NEAR_END) {
      pauseDiscAnimator();
    }
    // 唱针往唱盘移动时暂停动画
    else if (mNeedleAnimatorStatus == NeedleAnimatorStatus.TO_NEAR_END) {
      mNeedleAnimator.reverse();
      // 若动画在没结束时执行 reverse 方法，则不会执行，监听器的 onStart 方法，此时需要手动设置
      mNeedleAnimatorStatus = NeedleAnimatorStatus.TO_FAR_END;
    }
  }

  // 暂停唱盘动画
  private void pauseDiscAnimator() {
    mObjectorAnimator.pause();
    mNeedleAnimator.reverse(); // 逆向唱针动画
  }
  private void selectMusicWithButton() {
    if (mMusicStatus == MusicStatus.PLAY) {
      mIsNeedStartPlayAnimator = true;
      pauseAnimator();
    } else if (mMusicStatus == MusicStatus.PAUSE) {
      play();
    }
  }

  public void play() {
    playAnimator();
  }

  public void pause() {
    mMusicStatus = MusicStatus.PAUSE;
    pauseAnimator();
  }

  public void stop() {
    mMusicStatus = MusicStatus.STOP;
    pauseAnimator();
  }

  public void next() {
    playAnimator();;
    selectMusicWithButton();
  }

  public void last() {
    playAnimator();
    selectMusicWithButton();
  }

  public boolean isPlaying() {
    return mMusicStatus == MusicStatus.PLAY;
  }
  // 唱针当前所处的状态
  private enum NeedleAnimatorStatus {
    // 移动时， 从常盘往远处移动
    TO_FAR_END,
    //  移动时，从远处往常盘移动
    TO_NEAR_END,
    // 静止时， 离开唱盘
    IN_FAR_END,
    // 静止时，贴近唱盘
    IN_NEAR_END
  }

  // 音乐的状态
  public enum MusicStatus {
    PLAY, PAUSE, STOP
  }
}