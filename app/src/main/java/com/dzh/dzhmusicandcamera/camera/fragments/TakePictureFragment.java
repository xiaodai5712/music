package com.dzh.dzhmusicandcamera.camera.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.app.App;
import com.dzh.dzhmusicandcamera.camera.contracts.ITakePictureContract;
import com.dzh.dzhmusicandcamera.util.ScreenUtil;

public class TakePictureFragment extends Fragment implements ITakePictureContract.View {

  private static final String TAG = "DzhTakePictureFragment";
  private static final int DOUBLE_CLICK_DURATION = 1500;
  private static final int ANIMATION_DURATION_MS = 300;
  private static int SCROLL_DISTANCE_PX = ScreenUtil.dip2px(App.getContext(), 180f);

  private SurfaceView mSurfaceView;
  private Context mContext;
  private ViewGroup mScrollMenu;
  private ViewGroup mHighPixelsRootBar;
  private ITakePictureContract.Presenter mPresenter;

  private int mLastY;

  private long mLastClickTime = 0;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container
      , @Nullable Bundle savedInstanceState) {
    Log.d(TAG, "onCreateView: ");
    mContext = getContext();
    View view = inflater.inflate(R.layout.fragment_take_picture, container, false);
    mSurfaceView = view.findViewById(R.id.surface);
    mScrollMenu = view.findViewById(R.id.scroll_menu);
    mHighPixelsRootBar = view.findViewById(R.id.high_pixels_bar);
    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mPresenter = new TakePicturePresenter(this, mContext);
    mPresenter.addCallBackForSurfaceHolder(mSurfaceView.getHolder());
    mSurfaceView.setOnClickListener(v -> onSurfaceViewClick());
    initClickAndTouch();
    Log.d(TAG, "onViewCreated: ");
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    Log.d(TAG, "onDestroyView: ");
    mPresenter.releaseCamera();
    mPresenter = null;
    mContext = null;
  }

  @Override
  public void updateAlbumIcon(Bitmap bitmap) {

  }

  @Override
  public void onClickShutter() {

  }

  @Override
  public void onFinishCapture() {

  }

  private void onSurfaceViewClick() { // 这里要注意防止快速连续点击
    Log.d(TAG, "onSurfaceViewClick: ");
    long curTime = System.currentTimeMillis();
    if (curTime - mLastClickTime >= DOUBLE_CLICK_DURATION) {
      mPresenter.takePicture();
    } else {
      mPresenter.changeCamera();
    }
    mLastClickTime = curTime;
    // TODO: 2021/1/19  这里是有问题的，没有处理防快速连续点击的处理
  }

  private void initClickAndTouch() {
    mScrollMenu.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "onTouch: ");
        int y = (int) event.getRawY();
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            mLastY = y;
            break;
          case MotionEvent.ACTION_MOVE:
            int offsetY = y - mLastY;
            v.setTranslationY(Math.min(offsetY + v.getTranslationY(), SCROLL_DISTANCE_PX));
            mLastY = y;
            break;
          case MotionEvent.ACTION_UP:
            int translationY = (int) v.getTranslationY();
            ObjectAnimator animator = ObjectAnimator.ofFloat(v, "translationY"
                , v.getTranslationY(), translationY >= SCROLL_DISTANCE_PX / 2 ? SCROLL_DISTANCE_PX : 0);
            animator.setDuration(ANIMATION_DURATION_MS);
            animator.addListener(new AnimatorListenerAdapter() {
              @Override
              public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                v.setTranslationY(translationY >= SCROLL_DISTANCE_PX / 2 ? SCROLL_DISTANCE_PX : 0);
                if (v.getTranslationY() == 0) {
                  mHighPixelsRootBar.setVisibility(View.INVISIBLE);
                } else {
                  mHighPixelsRootBar.setVisibility(View.VISIBLE);
                }
              }
            });
            animator.start();
            break;
        }
        return true;
      }
    });
  }
}
