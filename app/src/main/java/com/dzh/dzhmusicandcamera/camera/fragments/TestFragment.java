package com.dzh.dzhmusicandcamera.camera.fragments;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dzh.dzhmusicandcamera.R;

public class TestFragment extends Fragment {
  private static final String TAG = "DzhTestFragment";

  private Button mScrollUp;
  private Button mScrollDown;
  private Button mTranslationUp;
  private Button mTranslationDown;
  private View mTarget;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    Log.d(TAG, "onCreateView: ");
    View view =  inflater.inflate(R.layout.fragment_test, container, false);

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Log.d(TAG, "onViewCreated: ");
    initClick();
  }

  private void initClick() {


  }

  private void test() {
    Log.d(TAG, "test: ");
    Log.d(TAG, "test: widthPixels = " + getContext().getResources().getDisplayMetrics().widthPixels);
    Log.d(TAG, "test: highPixels = " +  getContext().getResources().getDisplayMetrics().heightPixels);
    Log.d(TAG, "test: density = " +  getContext().getResources().getDisplayMetrics().density);
    Log.d(TAG, "test: scaleDensity = " +  getContext().getResources().getDisplayMetrics().scaledDensity);
    Log.d(TAG, "test: xdpi = " +  getContext().getResources().getDisplayMetrics().xdpi);
    Log.d(TAG, "test: ydpi = " +  getContext().getResources().getDisplayMetrics().ydpi);
    Log.d(TAG, "test: densityDpi = " +  getContext().getResources().getDisplayMetrics().densityDpi);

    Display display = getActivity().getWindowManager().getDefaultDisplay();
    Log.d(TAG, "test: rotation = " + display.getRotation());
    DisplayMetrics metrics = new DisplayMetrics();
    display.getMetrics(metrics);
    Log.d(TAG, "test: metrics = " + metrics + "");
  }
}
