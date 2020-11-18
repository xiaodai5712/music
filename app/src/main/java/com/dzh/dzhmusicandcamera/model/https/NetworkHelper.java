package com.dzh.dzhmusicandcamera.model.https;

import com.dzh.dzhmusicandcamera.base.entity.Album;
import com.dzh.dzhmusicandcamera.base.entity.AlbumSong;
import com.dzh.dzhmusicandcamera.base.entity.OnlineSongLrc;
import com.dzh.dzhmusicandcamera.base.entity.SearchSong;
import com.dzh.dzhmusicandcamera.base.entity.SingerImg;
import com.dzh.dzhmusicandcamera.base.entity.SongLrc;
import com.dzh.dzhmusicandcamera.base.entity.SongUrl;

import io.reactivex.Observable;

/**
 * Date: 2020/11/5
 * author: Dzh
 */
public interface NetworkHelper {
  Observable<AlbumSong> getAlbumSong(String id); // 得到专辑
  Observable<SearchSong> search(String seek, int offset); // 搜索歌曲
  Observable<Album> searchAlbum(String seek, int offset); // 搜索照片
  Observable<SongLrc> getLrc(String seek); // 获取歌词
  Observable<OnlineSongLrc> getOnlineSongLrc(String songId); // 获取网络股歌曲的歌词
  Observable<SingerImg> getSingerImg(String singer); // 获取歌手头像
  Observable<SongUrl> getSongUrl(String data); // 获取播放地址
}
