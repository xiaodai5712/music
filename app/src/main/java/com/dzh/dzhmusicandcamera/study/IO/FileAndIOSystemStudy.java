package com.dzh.dzhmusicandcamera.study.IO;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Date: 2020/12/15
 * author: Dzh
 */
public class FileAndIOSystemStudy {

  static void creteAnewFile(Context context, String fileName) {
    String parentPath = context
        .getExternalFilesDir(Environment.DIRECTORY_ALARMS/*这里填入参数之后，会在特定类型的文件中新建*/)
        .getAbsolutePath();
    File file = new File(parentPath, fileName);
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  static void newDirectory(Context context, String fileName) {
    String parentPath = context.getExternalFilesDir(null).getAbsolutePath();
    File file = new File(parentPath, fileName);
    if (!file.exists()) {
      file.mkdir();
    }
  }

  static void deleteFile(Context context, String fileName) {
    String parentPath = context.getExternalFilesDir(null).getAbsolutePath();
    File file = new File(parentPath, fileName);
    if (file.exists()) {
      file.delete();
    }
  }
}