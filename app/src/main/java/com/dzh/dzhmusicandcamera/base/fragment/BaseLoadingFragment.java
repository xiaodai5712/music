package com.dzh.dzhmusicandcamera.base.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.app.Constant;
import com.dzh.dzhmusicandcamera.base.presenter.IPresenter;
import com.wang.avi.AVLoadingIndicatorView;

import org.litepal.util.Const;

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
    if (!(mNormalView.getParent() instanceof ViewGroup)) {
      throw new IllegalStateException("mNormalView's parentView should be a ViewGroup");
    }
    ViewGroup parentPanel = (ViewGroup) mNormalView.getParent();
    View.inflate(mActivity, R.layout.error_view, parentPanel); //加载错误布局
    View.inflate(mActivity, R.layout.loading_view, parentPanel); // 加载loading布局
    mLoadingView = parentPanel.findViewById(R.id.loadingView);
    avLoadingView = parentPanel.findViewById(R.id.avLoading);
    mErrorView = parentPanel.findViewById(R.id.errorView);
    TextView reloadBtn = parentPanel.findViewById(R.id.reloadBtn);
    reloadBtn.setOnClickListener(v -> reload()); // 重新加载
    mNormalView.setVisibility(View.VISIBLE);
    mErrorView.setVisibility(View.GONE);
    mLoadingView.setVisibility(View.GONE);
  }

  @Override
  public void showNormalVIew() {
    super.showNormalVIew();
    if (mCurrentState == Constant.ERROR_STATE) {
      return;
    }
    hideViewByState(mCurrentState);
    mCurrentState = Constant.NORMAL_STATE;
    showViewByState(mCurrentState);
  }

  @Override
  public void showErrorView() {
    super.showErrorView();
    if (mCurrentState == Constant.ERROR_STATE) {
      return;
    }
    hideViewByState(mCurrentState);
    mCurrentState = Constant.ERROR_STATE;
    showViewByState(mCurrentState);
  }

  @Override
  public void showLoading() {
    super.showLoading();
    if (mCurrentState == Constant.LOADING_STATE) {
      return;
    }
    hideViewByState(mCurrentState);
    mCurrentState = Constant.LOADING_STATE;
    showViewByState(mCurrentState);
  }

  private void hideViewByState(int state) {
    if (state == Constant.NORMAL_STATE) {
      if (mNormalView == null) {
        return;
      }
      mNormalView.setVisibility(View.GONE);
    } else if (state == Constant.LOADING_STATE) {
      if (mLoadingView == null || avLoadingView == null) {
        return;
      }
      mLoadingView.setVisibility(View.GONE);
    } else {
      if (mErrorView == null) {
        return;
      }
      mErrorView.setVisibility(View.GONE);
    }
  }

  private void showViewByState(int state){
    if(state == Constant.NORMAL_STATE){
      if(mNormalView == null) return;
      mNormalView.setVisibility(View.VISIBLE);
    }else if(state == Constant.LOADING_STATE){
      if(mLoadingView == null||avLoadingView == null) return;
      mLoadingView.setVisibility(View.VISIBLE);
      avLoadingView.show();
    }else {
      if(mErrorView == null ) return;
      mErrorView.setVisibility(View.VISIBLE);
    }
  }
}