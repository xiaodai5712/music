package com.dzh.dzhmusicandcamera.camera.contracts;

public abstract class BasePresenter<T>{

  protected T mView;
  protected BasePresenter(T view) {
    mView = view;
  }
}
