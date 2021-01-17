package com.dzh.dzhmusicandcamera.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import java.util.List;

public class PreView extends ViewGroup implements SurfaceHolder.Callback {

  private static final String TAG = "DzhPreView";

  private Context mContext;
  private SurfaceHolder mHolder;
  private SurfaceView mSurfaceView;
  private List<Camera.Size> supportPreviewSize;
  private Camera mCamera;


  public PreView(Context context) {
    this(context, null);
  }

  public PreView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PreView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mSurfaceView = new SurfaceView(context);
    mHolder = mSurfaceView.getHolder();
    mHolder.addCallback(this);
//    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 现在是这个方法 deprecated
    mContext = context;
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {

  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    Camera.Parameters parameters = mCamera.getParameters();
    parameters.setPreviewSize(0, 0);
    requestLayout();
    mCamera.setParameters(parameters);
    mCamera.startPreview();
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    if (mCamera != null) {
      mCamera.stopPreview();
    }
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {

  }


  public void setCamera(Camera camera) {
    if (mCamera == camera) {
      return;
    }
    stopPreViewAndFreeCamera();
    mCamera = camera;
    if (mCamera == null) {
      List<Camera.Size> localSize = mCamera.getParameters().getSupportedPreviewSizes();
      supportPreviewSize = localSize;
      try {
        mCamera.setPreviewDisplay(mHolder);
      } catch (Exception e) {
        e.printStackTrace();
      }
      mCamera.startPreview();
    }
  }

  private void stopPreViewAndFreeCamera() {
    if (mCamera != null) {
      mCamera.stopPreview();
      mCamera.release();
      mCamera = null;
    }
  }
}
