package com.dzh.dzhmusicandcamera.model.prefs;

/**
 * Date: 2020/11/5
 * author: Dzh
 */
public interface PreferencesHelper {
  void setPlayMode(int mode); // 保存播放转台
  int getPlayMode(); // 得到播放状态
}