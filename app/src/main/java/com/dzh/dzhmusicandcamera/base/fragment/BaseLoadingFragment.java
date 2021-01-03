package com.dzh.dzhmusicandcamera.base.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.app.Constant;
import com.dzh.dzhmusicandcamera.base.presenter.IPresenter;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;

/**
 * Date: 2020/12/30
 * author: Dzh
 */
public abstract class BaseLoadingFragment<T extends IPresenter> extends BaseMvpFragment<T> {

  private View mNormalView; // 正常布局
  private View mErrorView; // 错误布局
  private View mLoadingView; // 加载布局
  private AVLoadingIndicatorView avLoadingView;

  private int mCurrentState = Constant.NORMAL_STATE; // 当前布局状态

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    if (getView() == null) {
      return;
    }
    mNormalView = view.findViewById(R.id.normalView);
    if (mNormalView == null) {
      throw new IllegalStateException
          ("The subclass of BaseLoadFragment must contain a View it's id named normal_view");
    }
  }
}