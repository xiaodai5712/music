package com.dzh.dzhmusicandcamera.widget.lyric;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.dzh.dzhmusicandcamera.R;

import java.util.List;

/**
 * Date: 2020/11/23
 * author: Dzh
 */
public class LrcView extends View {

  private static final String TAG = "LyricView";
  private static final int REFRESH_DELAY_IN_MILLS = 100;

  private List<LrcBean> mLrcBeanList; // 歌词集合
  private Paint mDefaultPaint; // 默认歌词画笔
  private Paint mHighLightPaint; // 高亮歌词画笔

  private int mDefaultTextColor; // 默认歌词颜色
  private int mHighLightTextColor; // 高亮歌词颜色
  private float mWidth, mHeight; // 屏幕的宽和高
  private int mLineSpace; // 行间距
  private int mTextSize;
  private int mCurrentPosition;
  private MediaPlayer mPlayer; // 当前的播放器
  private int mLastPosition = 0; // 上一句歌词的位置

  // 将歌词集合传到这个自定义View中
  public LrcView setLrc(String lrc) {
    mLrcBeanList = LrcUtil.parseStr2Lrc(lrc);
    return this;
  }

  // 传递mediaPlayer给自定义View中
  public LrcView setPlayer(MediaPlayer player) {
    this.mPlayer = player;
    return this;
  }

  // 当setLrcBeanList被调用时需要重新绘制
  public LrcView draw() {
    mCurrentPosition = 0;
    mLastPosition = 0;
    invalidate();
    return this;
  }
  public LrcView(Context context) {
    this(context, null);
  }

  public LrcView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public LrcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LrcView);
    mHighLightTextColor
        = typedArray.getColor(R.styleable.LrcView_highLightLineTextColor, Color.GRAY);
    mDefaultTextColor = typedArray.getColor(R.styleable.LrcView_defaultTextColor, Color.BLUE);
    float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
    float scale = context.getResources().getDisplayMetrics().density;
    // 默认字体大小 16sp
    mTextSize
        = typedArray.getDimensionPixelSize(R.styleable.LrcView_lrcTextSize, (int) (16 * fontScale));
    mLineSpace = typedArray.getDimensionPixelSize(R.styleable.LrcView_lineSapce, (int) (30 * scale));
    typedArray.recycle();
    init(); // dzh! 这里忘了
  }

  private void init() {
    // 初始化歌词和画笔
    mDefaultPaint = new Paint();
    mDefaultPaint.setStyle(Paint.Style.FILL); // 填满
    mDefaultPaint.setAntiAlias(true); // 抗锯齿
    mDefaultPaint.setTextSize(mTextSize); // 字体大小
    mDefaultPaint.setColor(mDefaultTextColor); // 字体颜色
    mDefaultPaint.setTextAlign(Paint.Align.CENTER); // 字体居中

    // 初始化高亮歌词画笔
    mHighLightPaint = new Paint();
    mHighLightPaint.setStyle(Paint.Style.FILL);
    mHighLightPaint.setAntiAlias(true);
    mHighLightPaint.setColor(mHighLightTextColor);
    mHighLightPaint.setTextSize(mTextSize);
    mHighLightPaint.setTextAlign(Paint.Align.CENTER);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    // 得到测量后的宽高
    if (mWidth == 0 || mHeight == 0) {
      mWidth = getMeasuredWidth();
      mHeight = getMeasuredHeight();
    }
    getCurrentPosition();
    drawLrc(canvas);
    scrollLrc();
    // 延迟 0.1s 刷新
    postInvalidateDelayed(REFRESH_DELAY_IN_MILLS);
  }

  // 获取当前歌词的位置
  private void getCurrentPosition() {
    int curTime = mPlayer.getCurrentPosition();
    // 如果当前的时间大于10分钟，证明歌曲未播放，
    if (curTime > mLrcBeanList.get(0).getStart() || curTime > 10 * 60 * 1000) {
      mCurrentPosition = 0;
      return;
    } else if (curTime > mLrcBeanList.get(mLrcBeanList.size() - 1).getStart()) {
      mCurrentPosition = mLrcBeanList.size() - 1;
      return;
    }
    for (int i = 0; i < mLrcBeanList.size(); i++) {
      if (curTime >= mLrcBeanList.get(i).getStart() && curTime <= mLrcBeanList.get(i).getEnd()) {
        mCurrentPosition = i;
      }
    }
  }

  // 画歌词，第一句从正中间开始，以后的歌词递增行间距，并开始画
  private void drawLrc(Canvas canvas) {
    for (int i = 0; i < mLrcBeanList.size(); i++) {
      float x = mWidth / 2;
      float y = mHeight / 2 + i * mLineSpace;
      if (mCurrentPosition == i) {
        // 使用高亮画笔画歌词
        canvas.drawText(mLrcBeanList.get(i).getLrc(), x, y, mHighLightPaint);
      } else {
        canvas.drawText(mLrcBeanList.get(i).getLrc(), x, y, mDefaultPaint);
      }
    }
  }

  // 歌词滑动
  private void scrollLrc() {
    // 下一句歌词开始的时间
    long startTime = mLrcBeanList.get(mCurrentPosition).getStart();
    long curTime = mPlayer.getCurrentPosition();

    // 判断是否换行
    float v = (curTime - startTime) > 500 ? mCurrentPosition * mLineSpace
        : mLastPosition * mLineSpace
        + (mCurrentPosition - mLastPosition) * mLineSpace * ((curTime - startTime) / 500f);
    setScrollY((int) v);
    if (getScrollY() == mCurrentPosition * mLineSpace) {
      mLastPosition = mCurrentPosition;
    }
  }
}