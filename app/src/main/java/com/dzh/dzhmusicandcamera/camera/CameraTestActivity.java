package com.dzh.dzhmusicandcamera.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dzh.dzhmusicandcamera.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraTestActivity extends AppCompatActivity {

  private static final String TAG = "DzhCameraTest";
  private static final int REQ_TAKE_PHOTO = 1;
  Button mCameraButton;
  Button mDeleteButton;
  Uri mPicUri;
  String mFilePath;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera_test);
    mCameraButton = findViewById(R.id.btn_take_photo);
    mDeleteButton = findViewById(R.id.btn_delete_photo);
    requestPermission();
    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
    builder.detectAll();
    StrictMode.setVmPolicy(builder.build());
    initClick();
  }

  private void initClick() {
    mCameraButton.setOnClickListener(v -> {
      DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
      String uri = dateFormat.format(new Date());
      Log.d(TAG, "initClick: " + uri);
      File fileUri = new File(Environment.getExternalStorageDirectory(),   uri + ".jpg");
      mFilePath = fileUri.getAbsolutePath();
      Log.d(TAG, "initClick: filePath" + fileUri.getAbsolutePath());
      mPicUri = Uri.fromFile(fileUri);
      takePicture(com.dzh.dzhmusicandcamera.camera.CameraTestActivity.this, mPicUri, REQ_TAKE_PHOTO);
    });

    mDeleteButton.setOnClickListener(v -> {
      deletePhoto(mFilePath);
    });

  }

  private void deletePhoto(String uri) {
    File file = new File(uri);
    Log.d(TAG, "deletePhoto: uri = " + uri);
    Log.d(TAG, "deletePhoto: file.exists " + file.exists());
    if (file.exists()) {
      file.delete();
      Log.d(TAG, "deletePhoto: file.exists" + file.exists());
    }
  }

  private void takePicture(Activity activity, Uri imageUri, int requestCode) {
    //调用系统相机
    Intent takePictureIntent = new Intent();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
    }
    takePictureIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
    //将拍照结果保存至photo_file的Uri中，不保留在相册中
    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
    if (activity!=null){

      activity.startActivityForResult(takePictureIntent, requestCode);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQ_TAKE_PHOTO) {
      if (resultCode == RESULT_OK) {
        Log.d(TAG, "onActivityResult: data == null ? " + (data == null));
        Log.d(TAG, "onActivityResult: 拍照成功");
        Log.d(TAG, "onActivityResult: uri = " + mPicUri.toString());
        Bitmap bitmap = BitmapFactory.decodeFile(mFilePath);
        compressImageByQuality(bitmap,mFilePath);
      } else {
        Log.d(TAG, "onActivityResult: 拍照失败");
      }
    }
  }

  private void compressImageByQuality(Bitmap image, String filepath) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
      int options = 100;
      while (baos.toByteArray().length / 1024 > 500) {    //循环判断如果压缩后图片是否大于100kb,大于继续压缩
        baos.reset();//重置baos即清空baos
        options -= 10;//每次都减少10
        image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中

      }
      //压缩好后写入文件中
      FileOutputStream fos = new FileOutputStream(filepath);
      fos.write(baos.toByteArray());
      fos.flush();
      fos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Bitmap compressImage(Bitmap image, String filepath) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
      int options = 100;
      while (baos.toByteArray().length / 1024 > 100) {    //循环判断如果压缩后图片是否大于100kb,大于继续压缩
        baos.reset();//重置baos即清空baos
        options -= 10;//每次都减少10
        image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中

      }
      //压缩好后写入文件中
      FileOutputStream fos = new FileOutputStream(filepath);
      fos.write(baos.toByteArray());
      fos.flush();
      fos.close();
      return image;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private void requestPermission() {
    if (ContextCompat.checkSelfPermission(CameraTestActivity.this
        , Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(CameraTestActivity.this
          , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }
  }


}