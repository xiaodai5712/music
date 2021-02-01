package com.dzh.dzhmusicandcamera.camera.contracts;

import android.graphics.Bitmap;
import android.view.SurfaceHolder;

public interface ITakePictureContract {
  interface Presenter {
    void takePicture();
    void changeCamera();
    void releaseCamera();
    void addCallBackForSurfaceHolder(SurfaceHolder holder);
  }

  interface View {
    void updateAlbumIcon(Bitmap bitmap);
    void onClickShutter();
    void onFinishCapture();
  }
}
