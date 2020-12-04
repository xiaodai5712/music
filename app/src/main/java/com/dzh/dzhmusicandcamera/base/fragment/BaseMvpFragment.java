package com.dzh.dzhmusicandcamera.base.fragment;

import com.dzh.dzhmusicandcamera.base.presenter.IPresenter;

/**
 * Date: 2020/11/4
 * autr: Dzh
 */
public abstract class BaseMvpFragment <T extends IPresenter> extends BaseFragment {

  protected abstract T getPresenter();
  protected T mPresenter;

  @Override
  protected void initView() {
    mPresenter = getPresenter();
    mPresenter.attachView(this);
  }

  @Override
  public void onDestroy() {
    if (mPresenter != null) {
      mPresenter.detachView();
      mPresenter = null;
    }
    super.onDestroy();
  }
}