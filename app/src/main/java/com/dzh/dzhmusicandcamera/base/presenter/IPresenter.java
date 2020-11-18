package com.dzh.dzhmusicandcamera.base.presenter;

import com.dzh.dzhmusicandcamera.base.view.BaseView;

import io.reactivex.disposables.Disposable;

/**
 * Date: 2020/11/4
 * author: Dzh
 */
public interface IPresenter<T extends BaseView> {
  void attachView(T view); // 注入View
  boolean isAttachView(); // 判断是否注入View
  void detachView(); // 解除 view
  void addRxSubscribe(Disposable disposable); // 添加订阅者

}
