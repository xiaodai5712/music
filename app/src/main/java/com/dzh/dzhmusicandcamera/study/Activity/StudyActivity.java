package com.dzh.dzhmusicandcamera.study.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.base.view.MainActivity;
import com.dzh.dzhmusicandcamera.databinding.ActivityStudyBinding;
import com.dzh.dzhmusicandcamera.study.RxJava.RxJavaStudy;
import com.dzh.dzhmusicandcamera.study.mvvm.Man;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class StudyActivity extends AppCompatActivity {

  private static final String TAG = "DzhStudyActivity";
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ActivityStudyBinding binding
        = DataBindingUtil.setContentView(this, R.layout.activity_study);
    binding.setMan(new Man("tom", "22"));
    findViewById(R.id.layout).setOnClickListener(v -> click());

  }

  private void click() {
    Log.d(TAG, "click: ");

//    OkHttpStudy.asyncPostRequest();


    Observable.range(1,3).repeat(3).subscribe(new Observer<Integer>() {
      @Override
      public void onSubscribe(Disposable d) {
        Log.d(TAG, "onSubscribe: ");
      }

      @Override
      public void onNext(Integer value) {
        Log.d(TAG, "onNext: " + value);

      }

      @Override
      public void onError(Throwable e) {
        Log.d(TAG, "onError: ");
      }

      @Override
      public void onComplete() {
        Log.d(TAG, "onComplete: ");
      }
    });
    Observable.just("http").toList().map((s -> false)).subscribe(s -> Log.d(TAG, "consume: " + s));

  }


}