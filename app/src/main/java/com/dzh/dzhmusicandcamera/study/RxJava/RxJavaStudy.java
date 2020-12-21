package com.dzh.dzhmusicandcamera.study.RxJava;


import android.app.ActivityOptions;
import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;

/**
 * Date: 2020/12/15
 * author: Dzh
 */
public class RxJavaStudy {
  final static String TAG = "DzhRxJavaStudy";

  public static <T> Observer<T> createObserver() {
    return new Observer<T>() {
      @Override
      public void onSubscribe(Disposable d) {
        Log.d(TAG, "onSubscribe: ");
      }

      @Override
      public void onNext(T value) {
        Log.d(TAG, "onNext: value = " + value);
      }

      @Override
      public void onError(Throwable e) {
        Log.d(TAG, "onError: e = " + e);
      }

      @Override
      public void onComplete() {
        Log.d(TAG, "onComplete: ");
      }
    };
  }


  public static <T> Observable<T> createObservable(final T t) {
    return Observable.create((ObservableEmitter<T> e) -> {
      Log.d(TAG, "createObservable: ");
      e.onNext(t);

      e.onComplete();
    });
  }

  public static interface Action0 extends Action {
    void call();
  }
  public static interface Action1<T> extends Action {
    void call(T t);
  }

  // 变换操作符
  public void map() {
    Function<String, Integer> f = new Function<String, Integer>() {
      @Override
      public Integer apply(String s) throws Exception {
        return null;
      }
    };
  }
}