package com.dzh.dzhmusicandcamera.model.db;

import com.dzh.dzhmusicandcamera.base.entity.AlbumSong;
import com.dzh.dzhmusicandcamera.base.entity.LocalSong;
import com.dzh.dzhmusicandcamera.base.entity.Song;

import java.util.List;

/**
 * Date: 2020/11/5
 * author: Dzh
 * 数据库操作接口
 */
public interface DbHelper {
  /*
   * 将所有搜索专辑列表中的歌曲都保存到网络歌曲数据库中
   * @param songList 专辑列表
   */

  void insertAllAlbumSong(List<AlbumSong.DataBean.ListBean> songList);
  List<LocalSong> getLocalMp3Info(); // 得到本地列表
  boolean saveSong(List<LocalSong> localSongs); // 将本地音乐放到数据库中
  boolean queryLove(String songId); // 从数据库查找是否为收藏歌曲
  boolean saveToLove(Song song); // 收藏歌曲
  boolean deleteFromLove(String songId); // 取消收藏歌曲
}
