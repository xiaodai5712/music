package com.dzh.dzhmusicandcamera.base.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dzh.dzhmusicandcamera.base.fragment.BaseLazyFragment;
import com.dzh.dzhmusicandcamera.base.view.BaseView;
import com.dzh.dzhmusicandcamera.util.CommonUtil;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Date: 2020/11/4
 * author: Dzh
 */
public abstract class BaseFragment extends BaseLazyFragment implements BaseView {

  private Unbinder mBinder;
  protected Activity mActivity;

  protected abstract void initView();
  protected abstract void loadData();
  protected abstract int getLayoutId();

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    mActivity = (Activity) context;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(getLayoutId(), container, false);
    mBinder = ButterKnife.bind(this, view);
    initView();
    return view;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    if (mBinder != null && mBinder != Unbinder.EMPTY) {
      mBinder.unbind();
      mBinder = null;
    }
  }

  @Override
  protected void lazyLoadData() {

  }

  @Override
  public void showNormalVIew() {

  }

  @Override
  public void showErrorView() {

  }

  @Override
  public void showLoading() {

  }

  @Override
  public void reload() {

  }

  @Override
  public void showToast(String message) {
    CommonUtil.showToast(mActivity, message);
  }
}