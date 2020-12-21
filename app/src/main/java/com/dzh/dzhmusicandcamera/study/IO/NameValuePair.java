package com.dzh.dzhmusicandcamera.study.IO;

/**
 * Date: 2020/12/14
 * author: Dzh
 */
public class NameValuePair {
  private String mName;
  private String mValue;

  public NameValuePair(String name, String value) {
    mName = name;
    mValue = value;
  }

  public String getName() {
    return mName;
  }

  public NameValuePair setName(String mName) {
    this.mName = mName;
    return this;
  }

  public String getValue() {
    return mValue;
  }

  public NameValuePair setValue(String mValue) {
    this.mValue = mValue;
    return this;
  }
}