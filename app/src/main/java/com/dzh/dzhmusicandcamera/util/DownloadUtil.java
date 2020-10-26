package com.dzh.dzhmusicandcamera.util;

import androidx.savedstate.SavedStateRegistryOwner;

import com.dzh.dzhmusicandcamera.app.Api;
import com.dzh.dzhmusicandcamera.base.entity.DownloadSong;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 2020/10/14
 * author: Dzh
 */
public class DownloadUtil {

  public static List<DownloadSong> getSongFromFile(String fileName) {
    // .m4a 截取掉得到singer-songName-duration-songID-size
    List<DownloadSong> res = new ArrayList<>();
    File file = new File(fileName);
    if (!file.exists()) {
      file.mkdirs();
      return res;
    }
    File[] subFile = file.listFiles();
    for (File value : subFile) {
      String songFileName = value.getName();
      String songFile = songFileName.substring(0, songFileName.lastIndexOf("."));
      String[] songValue = songFile.split("-");
      long size = Long.valueOf(songValue[4]);
      // 如果文件的大小不等于实际大小，则表示该歌曲还未下载完成，被认为暂停，故跳过该歌曲，不加入到已下载集合
      if (size != value.length()) {
        continue;
      }
      DownloadSong downloadSong = new DownloadSong();
      downloadSong.setSinger(songValue[0]);
      downloadSong.setName(songValue[1]);
      downloadSong.setDuration(Long.valueOf(songValue[2]));
      downloadSong.setSongId(songValue[3]);
      downloadSong.setUrl(fileName + songFileName);
      res.add(downloadSong);
    }
    return res;
  }

  public static boolean isExistOfDownloadSong(String songId) {
    File file = new File(Api.STORAGE_SONG_FILE);
    if (!file.exists()) {
      file.mkdirs();
      return false;
    }
    File[] subFile = file.listFiles();
    for (File value : subFile) {
      String songFileName = value.getName();
      String songFile = songFileName.substring(0, songFileName.lastIndexOf("."));
      String[] songValue = songFile.split("-");
      // 如果文件的大小不等于实际大小，则表示歌曲还没下载完成就被认为暂停，故跳过该歌曲不加入到已经下载的集合
      if (songValue[3].equals(songId)) {
        long size = Long.valueOf(songValue[4]);
        return size == value.length();
      }

    }
    return false;
  }

  // 组装下载歌曲的名字
  public static String getSaveSongFile(String singer, String songName, long duration, String songId
      , long size) {
    return singer + "-" + songName + "-" + duration + "-" + songId + "-" + size + ".m4a";
  }
}
