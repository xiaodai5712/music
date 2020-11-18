package com.dzh.dzhmusicandcamera.base;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Date: 2020/11/4
 * author: Dzh
 */
public abstract class BaseLazyFragment extends Fragment {

  private static final String TAG = "BaseLazyFragment";

  private boolean mIsViewCreated = false; // 布局是否被创建
  private boolean mIsLoadData = false; // 数据是否加载
  private boolean mIsFirstVisible = true; // 是否第一次可见

  protected abstract void lazyLoadData();

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    mIsViewCreated = true;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    if (isFragmentVisible(this)) {
      if (this.getParentFragment() == null || isFragmentVisible(this.getParentFragment())) {
        Log.d(TAG, "onActivityCreated: 加载数据");
        lazyLoadData();
        mIsLoadData = true;
        if (mIsFirstVisible) {
          mIsFirstVisible = false;
        }
      }
    }
  }

  // 在使用ViewPage时，加载数据
  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (isFragmentVisible(this) && !mIsLoadData && mIsViewCreated) {
      lazyLoadData();
      mIsLoadData = true;
    }
  }

  // 调用show方法时加载数据

  @Override
  public void onHiddenChanged(boolean hidden) {
    super.onHiddenChanged(hidden);
    // onHiddenChanged 调用在Resumed之前，所以此时可能fragment被add， 但是还没有调用show方法
    if (!hidden && !this.isResumed()) {
      return;
    }
    // 使用 hide 和 show 时， fragment的所有生命周期方法都不会调用， 除了onHiddenChanged()
    if (!hidden && mIsFirstVisible) {
      Log.d(TAG, "onHiddenChanged: 加载数据");
      lazyLoadData();
      mIsFirstVisible = false;
    }
  }

  private boolean isFragmentVisible(Fragment fragment) {
    return fragment.getUserVisibleHint() && !fragment.isHidden();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mIsFirstVisible = true;
    mIsLoadData = false;
    mIsViewCreated = false;
  }
}