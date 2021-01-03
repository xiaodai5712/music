package com.dzh.dzhmusicandcamera.base.view.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.adapter.TabAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 2020/12/30
 * author: Dzh
 */
public class ContentFragment extends Fragment {

  private List<String> mTitleList;
  private List<Fragment> mFragments;
  private ViewPager mPager;
  private TabAdapter mAdapter;
  private TabLayout mTabLayout;
  private String[] mTitles = {"歌曲", "专辑"};
  private String[] mTypes = {"song", "album"};
  private Bundle mBundle;
  private String mSeek;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container
      , @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_content, container, false);
    mBundle = getArguments();
    if (mBundle != null) {
      mSeek = mBundle.getString(SearchContentFragment.SEEK_KEY);
    }
    mPager = view.findViewById(R.id.page);
    mTabLayout = view.findViewById(R.id.tab_layout);
    mTitleList = new ArrayList<>();
    mFragments = new ArrayList<>();
    initTab();
    return view;
  }

  private void initTab() {
    for (int i = 0; i < mTitles.length; i++) {
      mTitleList.add(mTitles[i]);
      mFragments.add(SearchContentFragment.newInstance(mSeek, mTypes[i]));
    }
    mAdapter = new TabAdapter(getChildFragmentManager(), mFragments, mTitleList);
    mPager.setAdapter(mAdapter);
    mTabLayout.setupWithViewPager(mPager);
  }

  public static ContentFragment newInstance(String seek) {
    Bundle args = new Bundle();
    ContentFragment fragment = new ContentFragment();
    args.putString(SearchContentFragment.SEEK_KEY, seek);
    fragment.setArguments(args);
    return fragment;
  }
}