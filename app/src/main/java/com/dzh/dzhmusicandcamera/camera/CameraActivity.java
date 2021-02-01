package com.dzh.dzhmusicandcamera.camera;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.camera.fragments.TakePictureFragment;
import com.dzh.dzhmusicandcamera.camera.fragments.TestFragment;

import java.util.ArrayList;
import java.util.List;


public class CameraActivity extends AppCompatActivity {

  private static final String TAG = "DzhCameraActivity";

  private ViewPager mViewPager;
  private List<Fragment> mFragmentList;
  private PagerAdapter mPagerAdapter;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate: ");
    setContentView(R.layout.activity_camera);
    requestPermission();
    initViews();
  }

  private void initViews() {
    Log.d(TAG, "initViews: ");
    mFragmentList = new ArrayList<>();
    mFragmentList.add(new TakePictureFragment());
    mFragmentList.add(new TestFragment());
    mViewPager = findViewById(R.id.view_pager);
    mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
    mViewPager.setAdapter(mPagerAdapter);
  }


  private void requestPermission() {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this
          , new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE
              , Manifest.permission.CAMERA}, 0);
    }
  }

  private class ViewPagerAdapter extends FragmentPagerAdapter {

    public ViewPagerAdapter(@NonNull FragmentManager fm) {
      super(fm);
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
  }
}