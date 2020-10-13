package com.dzh.dzhmusicandcamera.base.view;

/**
 * Date: 2020/10/12
 * author: Dzh
 */
public interface BaseView {
  void showNormalVIew(); // 正常布局
  void showErrorView(); // 错误布局
  void showLoading(); // 加载布局
  void reload(); // 重新加载
  void showToast(String message); // 显示Toast
}
