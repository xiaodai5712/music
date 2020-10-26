package com.dzh.dzhmusicandcamera.app;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

/**
 * Date: 2020/10/14
 * author: Dzh
 */
public class App extends Application {
  private static Context context;

  @Override
  public void onCreate() {
    super.onCreate();
    context = getApplicationContext();
    LitePal.initialize(this);
  }

  public static Context getContext() {
    return context;
  }
}
