package com.dzh.dzhmusicandcamera.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.dzh.dzhmusicandcamera.app.Api;
import com.dzh.dzhmusicandcamera.app.App;
import com.dzh.dzhmusicandcamera.app.Constant;
import com.dzh.dzhmusicandcamera.base.entity.Song;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Date: 2020/10/14
 * author: Dzh
 */
public class FileUtil {
  private static final String TAG = "FileUtil";
  private static final String SONG_FILE_PATH = "/yuanmusic/song.text";

  public static void saveSong(Song song) {
    File file = new File(App.getContext().getExternalFilesDir("yuanmusic").getAbsolutePath());
    if (!file.exists()) {
      file.mkdirs();
    }

    // 写流对象的对象
    File userFile = new File(file, "song.text");
    ObjectOutputStream oos = null;
    try {
      oos = new ObjectOutputStream(new FileOutputStream(userFile));
      oos.writeObject(song);
      oos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static Song getSong() {
    try {
      ObjectInputStream ois =
          new ObjectInputStream(
              new FileInputStream(App.getContext().
                  getExternalFilesDir("") + SONG_FILE_PATH));
      Song song = (Song) ois.readObject();
      return song;
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
      return new Song();
    }
    return null;
  }

  // 保存图片到本地
  public static void saveImgToNative(Context context, Bitmap bitmap, String singer) {

    File file = new File(Api.STORAGE_IMG_FILE);
    if (!file.exists()) {
      file.mkdirs();
    }
    File singerImgFile = new File(file, singer + ".jpg");
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(singerImgFile);
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
      fos.flush();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      Log.d(TAG, "saveImgToNative: file not found");
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (fos != null) {
          fos.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  // 保存歌词到本地
  public static void saveLrcToNative(String lrc, String songName) {
    new Thread(() -> {
      File file = new File(Api.STORAGE_LRC_FILE);
      if (!file.exists()) {
        file.mkdirs();
      }
      try {
        File lrcFile = new File(file, songName + Constant.LRC);
        FileWriter fileWriter = new FileWriter(lrcFile);
        fileWriter.write(lrc);
        ;
        fileWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }).start();
  }

  public static String getLrcFromNative(String songName) {
    try {
      FileReader fileReader = new FileReader(Api.STORAGE_LRC_FILE
          + songName + Constant.LRC);
      BufferedReader bufferedReader = new BufferedReader(fileReader);
      StringBuilder lrc = new StringBuilder();
      while (true) {
        String s = bufferedReader.readLine();
        if (s == null) {
          break;
        }
        lrc.append(s).append("\n");
      }
      fileReader.close();
      Log.d(TAG, "getLrcFromNative: lrc == " + lrc.toString());
      return lrc.toString();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

}
