package com.dzh.dzhmusicandcamera.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Date: 2020/12/30
 * author: Dzh
 */
public class TabAdapter extends FragmentPagerAdapter {

  private List<Fragment> mFragmentList; // 顶部导航栏的内容， 即fragment
  private List<String> mTitle; // 顶部导航栏的标题


  public TabAdapter(FragmentManager fragmentManager, List<Fragment> fragments, List<String> title) {
    super(fragmentManager);
    mFragmentList = fragments;
    mTitle = title;
  }

  @NonNull
  @Override
  public Fragment getItem(int position) {
    return mFragmentList.get(position);
  }

  @Override
  public int getCount() {
    return mFragmentList.size();
  }

  @Nullable
  @Override
  public CharSequence getPageTitle(int position) {
    return mTitle.get(position);
  }
}