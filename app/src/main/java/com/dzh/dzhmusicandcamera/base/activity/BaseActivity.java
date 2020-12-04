package com.dzh.dzhmusicandcamera.base.activity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dzh.dzhmusicandcamera.base.view.BaseView;
import com.dzh.dzhmusicandcamera.util.CommonUtil;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Date: 2020/10/12
 * author: Dzh
 */
public abstract class BaseActivity extends AppCompatActivity implements BaseView {

  private final static String TAG = "DzhBaseActivity";
  private Unbinder mBinder;

  protected abstract int getLayoutId();
  protected abstract void initView();
  protected abstract void initData();
  protected abstract void onClick();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate: ");
    setContentView(getLayoutId());
    ButterKnife.bind(this);
    initView();
    initData();
    onClick();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mBinder != null && mBinder != mBinder.EMPTY) {
      mBinder.unbind();
      mBinder = null;
    }
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
    CommonUtil.showToast(this, message);
  }

  protected void initClick() {}
}
