package com.dzh.dzhmusicandcamera.base.activity;

import com.dzh.dzhmusicandcamera.base.presenter.IPresenter;

/**
 * Date: 2020/11/30
 * author: Dzh
 */
public abstract class BaseMvpActivity<T extends IPresenter> extends BaseActivity {

  protected abstract T getPresenter();
  protected T mPresenter;

  @Override
  protected void initView() {
    mPresenter = getPresenter();
    mPresenter.attachView(this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mPresenter != null) {
      mPresenter.detachView();
      mPresenter = null;
    }
  }
}