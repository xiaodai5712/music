package com.dzh.dzhmusicandcamera.camera.fragments;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;

import com.dzh.dzhmusicandcamera.camera.BitmapUtils;
import com.dzh.dzhmusicandcamera.camera.contracts.BasePresenter;
import com.dzh.dzhmusicandcamera.camera.contracts.ITakePictureContract;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingDeque;

public class TakePicturePresenter extends BasePresenter<ITakePictureContract.View>
    implements ITakePictureContract.Presenter {

  private static final String TAG = "DzhTakePictureFragmentPresenter";

  private static final int PREVIEW_ROTATION_IN_DEGREE = 90;
  private Camera mCamera;
  private Context mContext;
  private SurfaceHolder mHolder;
  private boolean mCameraFacingBack = true;

  private SurfaceHolder.Callback mCallBack = new SurfaceHolder.Callback() {
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
      Log.d(TAG, "surfaceCreated: ");
      mHolder = holder;
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
      Log.d(TAG, "surfaceDestroyed: ");
      releaseCamera();
    }
  };

  protected TakePicturePresenter(ITakePictureContract.View view) {
    super(view);
  }

  public TakePicturePresenter(ITakePictureContract.View view, Context context) {
    super(view);
    mContext = context;
    openCamera();
  }

  @Override
  public void takePicture() {
    Log.d(TAG, "takePicture: ");
    mCamera.takePicture(new Camera.ShutterCallback() {
      @Override
      public void onShutter() {
        Log.d(TAG, "onShutter: 快门启动");
      }
    }, new Camera.PictureCallback() {
      @Override
      public void onPictureTaken(byte[] data, Camera camera) {
        Log.d(TAG, "onPictureTaken: raw 拍照完成");
        if (data == null || data.length < 1) {
          Log.d(TAG, "onPictureTaken: 无数据");
        } else {
          Log.d(TAG, "onPictureTaken: data = " + Arrays.toString(data));
        }
//        File photoFile = createPhotoFile();
//        try {
//          writePhotoData(data, photoFile);
//        } catch (IOException e) {
//          e.printStackTrace();
//        }
//        mCamera.startPreview();
        // TODO: 2021/1/19 这里拿不到未经压缩的数据， data 返回的是个null
      }
    }, new Camera.PictureCallback() {
      @Override
      public void onPictureTaken(byte[] data, Camera camera) {
        Log.d(TAG, "onPictureTaken: jpg 拍照完成");
        if (data == null || data.length < 1) {
          Log.d(TAG, "onPictureTaken: 无数据");
        } else {
          Log.d(TAG, "onPictureTaken: data = " + Arrays.toString(data));
        }
        File photoFile = createPhotoFile();
        try {
          writePhotoData(data, photoFile);
        } catch (IOException e) {
          e.printStackTrace();
        }
        mCamera.startPreview();
      }
    });
  }

  @Override
  public void changeCamera() {
    Log.d(TAG, "changeCamera: ");
    releaseCamera();
    if (mCameraFacingBack) {
      mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
    } else {
      mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
    }
    mCameraFacingBack = !mCameraFacingBack;
    resumeCamera();
  }


  @Override
  public void releaseCamera() {
    Log.d(TAG, "releaseCamera: ");
    if (mCamera == null) {
      return;
    }
    mCamera.stopPreview();
    mCamera.release();
    mCamera = null;
  }

  @Override
  public void addCallBackForSurfaceHolder(SurfaceHolder holder) {
    holder.addCallback(mCallBack);
  }

  private void openCamera() {
    if (mCamera != null) {
      return;
    }
    mCamera = Camera.open();
    Camera.Parameters parameters = mCamera.getParameters();
    parameters.setRotation(90);
    mCamera.setParameters(parameters);
    mCamera.setDisplayOrientation(PREVIEW_ROTATION_IN_DEGREE);
    test();
  }

  private void resumeCamera() {
    mCamera.setDisplayOrientation(PREVIEW_ROTATION_IN_DEGREE);
    try {
      mCamera.setPreviewDisplay(mHolder);
    } catch (IOException e) {
      e.printStackTrace();
    }
    Camera.Parameters parameters = mCamera.getParameters();
    parameters.setRotation(270);
    mCamera.setParameters(parameters);
    mCamera.startPreview();
  }

  private File createPhotoFile( ) {
    Log.d(TAG, "createPhotoFile: ");
    DateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
    String timeStamp = format.format(System.currentTimeMillis());
    File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    File imageFile = null;
    try {
      imageFile = File.createTempFile(timeStamp, ".jpg", storageDir);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return imageFile;
  }

  private void writePhotoData(byte[] data, File file) throws IOException {
    Log.d(TAG, "writePhotoData: ");
    OutputStream outputStream = new FileOutputStream(file);
    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
//    Bitmap rawBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//    Bitmap rotateBitmap = BitmapUtils.rotate(rawBitmap, mCameraFacingBack? 90f : 270f);
    // TODO: 2021/1/19  这里只处理了 后置摄像头的拍出来的照片的旋转，没有处理前摄像头的
    bufferedOutputStream.write(data);
    bufferedOutputStream.close();
  }

  private void test() {
    Camera.Parameters parameters = mCamera.getParameters();
    List<Camera.Size> previewSize = parameters.getSupportedPreviewSizes();
    for (Camera.Size size : previewSize) {
      Log.d(TAG, "test: preView width = " + size.width + ", height = " + size.height);
    }
    Log.d(TAG, "test: preViewSize width = " + parameters.getPreviewSize().width
        + ", height = " + parameters.getPreviewSize().height);
    List<Camera.Size> pictureSize = parameters.getSupportedPictureSizes();
    for (Camera.Size size : pictureSize) {
      Log.d(TAG, "test: pictureSize width = " + size.width + ", height = " + size.height);
    }
    Log.d(TAG, "test: pictureViewSiz: width = " + parameters.getPictureSize().width
        + ", height = " + parameters.getPictureSize().height);

    Log.d(TAG, "test: supportedPreviewFormat = " + parameters.getSupportedPreviewFormats());
    Log.d(TAG, "test: supportedPictureFormat = " + parameters.getSupportedPictureFormats());
  }

  /**
   * 找出最合适的尺寸，规则如下：
   * 1.将尺寸按比例分组，找出比例最接近屏幕比例的尺寸组
   * 2.在比例最接近的尺寸组中找出最接近屏幕尺寸且大于屏幕尺寸的尺寸
   * 3.如果没有找到，则忽略2中第二个条件再找一遍，应该是最合适的尺寸了
   */
  private static Camera.Size findProperSize(Point surfaceSize, List<Camera.Size> sizeList) {
    if (surfaceSize.x <= 0 || surfaceSize.y <= 0 || sizeList == null) {
      return null;
    }

    int surfaceWidth = surfaceSize.x;
    int surfaceHeight = surfaceSize.y;

    List<List<Camera.Size>> ratioListList = new ArrayList<>();
    for (Camera.Size size : sizeList) {
      addRatioList(ratioListList, size);
    }

    final float surfaceRatio = (float) surfaceWidth / surfaceHeight;
    List<Camera.Size> bestRatioList = null;
    float ratioDiff = Float.MAX_VALUE;
    for (List<Camera.Size> ratioList : ratioListList) {
      float ratio = (float) ratioList.get(0).width / ratioList.get(0).height;
      float newRatioDiff = Math.abs(ratio - surfaceRatio);
      if (newRatioDiff < ratioDiff) {
        bestRatioList = ratioList;
        ratioDiff = newRatioDiff;
      }
    }

    Camera.Size bestSize = null;
    int diff = Integer.MAX_VALUE;
    assert bestRatioList != null;
    for (Camera.Size size : bestRatioList) {
      int newDiff = Math.abs(size.width - surfaceWidth) + Math.abs(size.height - surfaceHeight);
      if (size.height >= surfaceHeight && newDiff < diff) {
        bestSize = size;
        diff = newDiff;
      }
    }

    if (bestSize != null) {
      return bestSize;
    }

    diff = Integer.MAX_VALUE;
    for (Camera.Size size : bestRatioList) {
      int newDiff = Math.abs(size.width - surfaceWidth) + Math.abs(size.height - surfaceHeight);
      if (newDiff < diff) {
        bestSize = size;
        diff = newDiff;
      }
    }

    return bestSize;
  }

  private static void addRatioList(List<List<Camera.Size>> ratioListList, Camera.Size size) {
    float ratio = (float) size.width / size.height;
    for (List<Camera.Size> ratioList : ratioListList) {
      float mine = (float) ratioList.get(0).width / ratioList.get(0).height;
      if (ratio == mine) {
        ratioList.add(size);
        return;
      }
    }

    List<Camera.Size> ratioList = new ArrayList<>();
    ratioList.add(size);
    ratioListList.add(ratioList);
  }

}
