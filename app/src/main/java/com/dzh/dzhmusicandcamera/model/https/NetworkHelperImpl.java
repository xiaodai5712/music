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
 * 网络操作实现类
 */
public class NetworkHelperImpl implements NetworkHelper {

  private RetrofitService mRetrofitService;

  public NetworkHelperImpl(RetrofitService retrofitService) {
    mRetrofitService = retrofitService;
  }
  @Override
  public Observable<AlbumSong> getAlbumSong(String id) {
    return mRetrofitService.getAlbumSong(id);
  }

  @Override
  public Observable<SearchSong> search(String seek, int offset) {
    return mRetrofitService.search(seek, offset);
  }

  @Override
  public Observable<Album> searchAlbum(String seek, int offset) {
    return mRetrofitService.searchAlbum(seek, offset);
  }

  @Override
  public Observable<SongLrc> getLrc(String seek) {
    return mRetrofitService.getLrc(seek);
  }

  @Override
  public Observable<OnlineSongLrc> getOnlineSongLrc(String songId) {
    return mRetrofitService.getOnlineSongLrc(songId);
  }

  @Override
  public Observable<SingerImg> getSingerImg(String singer) {
    return mRetrofitService.getSingerImg(singer);
  }

  @Override
  public Observable<SongUrl> getSongUrl(String data) {
    return mRetrofitService.getSongUrl(data);
  }
}