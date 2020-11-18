package com.dzh.dzhmusicandcamera.model.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.dzh.dzhmusicandcamera.app.App;
import com.dzh.dzhmusicandcamera.app.Constant;

import org.litepal.util.Const;

/**
 * Date: 2020/11/15
 * author: Dzh
 */
public class PreferencesHelperImpl implements PreferencesHelper {
  private SharedPreferences mPreferences;

  public PreferencesHelperImpl() {
    mPreferences = App.getContext().getSharedPreferences(Constant.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
  }
  @Override
  public void setPlayMode(int mode) {
    mPreferences.edit().putInt(Constant.PREFS_PLAY_MODE, mode).apply();
  }

  @Override
  public int getPlayMode() {
    return mPreferences.getInt(Constant.PREFS_PLAY_MODE, 0);
  }
}