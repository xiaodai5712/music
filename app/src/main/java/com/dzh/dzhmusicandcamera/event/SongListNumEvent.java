package com.dzh.dzhmusicandcamera.event;

/**
 * Date: 2020/10/15
 * author: Dzh
 */
public class SongListNumEvent {
  private int type;
  public SongListNumEvent(int type){
    this.type = type;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }
}