package com.dzh.dzhmusicandcamera.event;

/**
 * Date: 2020/10/15
 * author: Dzh
 */
public class SongStatusEvent {
  private int mSongStatus;

  public SongStatusEvent(int songStatus) {
    mSongStatus = songStatus;
  }

  public int getSongStatus() {
    return mSongStatus;
  }
}
