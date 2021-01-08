package com.dzh.dzhmusicandcamera.base.observer;

import android.net.ParseException;
import android.os.Handler;
import android.util.Log;

import com.dzh.dzhmusicandcamera.base.view.BaseView;
import com.google.gson.JsonParseException;

import org.json.JSONException;

import java.net.UnknownHostException;

import io.reactivex.observers.ResourceObserver;
import retrofit2.HttpException;

import static com.dzh.dzhmusicandcamera.app.Constant.TAG_ERROR;

/**
 * Date: 2020/11/28
 * author: Dzh
 */
public class BaseObserver<T> extends ResourceObserver<T> {

  private static final String TAG = "BaseObserver";
  private static final int DELAY_DURATION_IN_MILLS = 500;

  private boolean mIsShowLoadingView = true;
  private boolean mIsShowErrorView = true;

  private BaseView mBaseView;

  private BaseObserver() {};

  protected BaseObserver(BaseView baseView) {
    this(baseView, false, false);
  }

  protected BaseObserver(BaseView baseView, boolean isShowLoadingView, boolean iShowErrorView) {
    this.mBaseView = baseView;
    this.mIsShowErrorView = iShowErrorView;
    this.mIsShowLoadingView = isShowLoadingView;
  }

  @Override
  protected void onStart() {
    if (mIsShowLoadingView) {
      mBaseView.showLoading();
    }
  }

  @Override
  public void onNext(T value) {
    new Handler().postDelayed(() -> {
      mBaseView.showNormalVIew();
    }, DELAY_DURATION_IN_MILLS);
  }

  @Override
  public void onError(Throwable e) {
    new Handler().postDelayed(() -> {
      if (mIsShowErrorView) {
        mBaseView.showNormalVIew();
      }
    }, DELAY_DURATION_IN_MILLS);
    e.printStackTrace();
    if (e instanceof UnknownHostException) {
      Log.e(TAG_ERROR, "networkError：" + e.getMessage());
      networkError();
    } else if (e instanceof InterruptedException) {
      Log.e(TAG_ERROR, "timeout：" + e.getMessage());
      timeoutError();
    } else if (e instanceof HttpException) {
      Log.e(TAG_ERROR, "http错误：" + e.getMessage());
      httpError();
    } else if (e instanceof JsonParseException || e instanceof JSONException
        || e instanceof ParseException) {
      Log.e(TAG_ERROR, "解析错误：" + e.getMessage());
      parseError();
    }else {
      Log.e(TAG_ERROR, "未知错误：" + e.getMessage());
      unknown();
    }
  }

  @Override
  public void onComplete() {

  }

  // 未知错误
  protected void unknown() {
    mBaseView.showToast("未知错误");
  }

  // 解析错误
  protected void parseError() {
    mBaseView.showToast("解析错误");
  }

  // http 错误
  protected void httpError() {
    mBaseView.showToast("网络错误");
  }

  // 网络股超时异常
  protected void timeoutError() {
    mBaseView.showToast("链接超时，请重试");
  }

  //网络不可用
  protected  void networkError() {
    mBaseView.showToast("网络不可用");
  }
}