package com.dzh.dzhmusicandcamera.camera.fragments;

import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dzh.dzhmusicandcamera.R;

import java.io.IOException;


public class TakePictureFragment extends Fragment {

  private static final String TAG = "DzhTakePictureFragment";

  private Camera mCamera;

  private SurfaceView mSurfaceView;
  private SurfaceHolder.Callback mCallBack = new SurfaceHolder.Callback() {
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
      Log.d(TAG, "surfaceCreated: ");
      try {
        mCamera.setPreviewDisplay(holder);
        mCamera.startPreview();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
      mCamera.release();
    }
  };



  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container
      , @Nullable Bundle savedInstanceState) {
    Log.d(TAG, "onCreateView: ");
    View view = inflater.inflate(R.layout.fragment_take_picture, container, false);
    mSurfaceView = view.findViewById(R.id.surface);
    mSurfaceView.getHolder().addCallback(mCallBack);
    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Log.d(TAG, "onViewCreated: ");
    // 在这里打开相机
    openCamera();
  }

  private void initSurfaceView() {

  }

  private void openCamera() {
    Log.d(TAG, "openCamera: ");
    if (mCamera != null) {
      return;
    }
    mCamera = Camera.open(0);
    mCamera.setDisplayOrientation(90);
  }

  class TakePictureThread extends Thread  {
    @Override
    public void run() {
      super.run();
    }
  }
}
