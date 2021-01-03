package com.dzh.dzhmusicandcamera.base.entity;

import org.litepal.crud.LitePalSupport;

/**
 * Date: 2020/12/25
 * author: Dzh
 */
public class SearchHistory extends LitePalSupport {
  String history;
  int id;

  public String getHistory() {
    return history;
  }

  public SearchHistory setHistory(String history) {
    this.history = history;
    return this;
  }

  public int getId() {
    return id;
  }

  public SearchHistory setId(int id) {
    this.id = id;
    return this;
  }
}