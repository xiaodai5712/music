package com.dzh.dzhmusicandcamera.study.mvvm;

/**
 * Date: 2020/12/17
 * author: Dzh
 */
public class Man {
  private String name;
  private String age;

  public Man(String name, String age) {
    this.name = name;
    this.age = age;
  }

  public String getName() {
    return name;
  }

  public Man setName(String name) {
    this.name = name;
    return this;
  }

  public String getAge() {
    return age;
  }

  public Man setAge(String age) {
    this.age = age;
    return this;
  }
}