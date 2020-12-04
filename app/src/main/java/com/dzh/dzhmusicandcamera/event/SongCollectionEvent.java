package com.dzh.dzhmusicandcamera.event;

/**
 * Date: 2020/10/15
 * author: Dzh
 */
public class SongCollectionEvent {

  private boolean mIsLove;

  public SongCollectionEvent(boolean isLove) {
    mIsLove = isLove;
  }

  public boolean isLove() {
    return mIsLove;
  }
}
