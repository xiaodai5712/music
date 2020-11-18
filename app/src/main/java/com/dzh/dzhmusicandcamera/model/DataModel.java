package com.dzh.dzhmusicandcamera.model;

import com.dzh.dzhmusicandcamera.base.entity.Album;
import com.dzh.dzhmusicandcamera.base.entity.AlbumSong;
import com.dzh.dzhmusicandcamera.base.entity.LocalSong;
import com.dzh.dzhmusicandcamera.base.entity.OnlineSongLrc;
import com.dzh.dzhmusicandcamera.base.entity.SearchSong;
import com.dzh.dzhmusicandcamera.base.entity.SingerImg;
import com.dzh.dzhmusicandcamera.base.entity.Song;
import com.dzh.dzhmusicandcamera.base.entity.SongLrc;
import com.dzh.dzhmusicandcamera.base.entity.SongUrl;
import com.dzh.dzhmusicandcamera.model.db.DbHelper;
import com.dzh.dzhmusicandcamera.model.db.DbHelperImpl;
import com.dzh.dzhmusicandcamera.model.https.NetworkHelper;
import com.dzh.dzhmusicandcamera.model.https.NetworkHelperImpl;
import com.dzh.dzhmusicandcamera.model.prefs.PreferencesHelper;
import com.dzh.dzhmusicandcamera.model.prefs.PreferencesHelperImpl;

import java.util.List;

import io.reactivex.Observable;

/**
 * Date: 2020/11/5
 * author: Dzh
 */
public class DataModel implements NetworkHelper, DbHelper, PreferencesHelper {

  private NetworkHelperImpl mNetworkHelper;
  private DbHelperImpl mDbHelper;
  private PreferencesHelperImpl mPreferencesHelper;

  public DataModel (NetworkHelperImpl networkHelper, DbHelperImpl dbHelper
      , PreferencesHelperImpl preferencesHelper) {
    mNetworkHelper = networkHelper;
    mDbHelper = dbHelper;
    mPreferencesHelper = preferencesHelper;
  }

  @Override
  public Observable<AlbumSong> getAlbumSong(String id) {
    return mNetworkHelper.getAlbumSong(id);
  }

  @Override
  public Observable<SearchSong> search(String seek, int offset) {
    return mNetworkHelper.search(seek, offset);
  }

  @Override
  public Observable<Album> searchAlbum(String seek, int offset) {
    return mNetworkHelper.searchAlbum(seek, offset);
  }

  @Override
  public Observable<SongLrc> getLrc(String seek) {
    return mNetworkHelper.getLrc(seek);
  }

  @Override
  public Observable<OnlineSongLrc> getOnlineSongLrc(String songId) {
    return mNetworkHelper.getOnlineSongLrc(songId);
  }

  @Override
  public Observable<SingerImg> getSingerImg(String singer) {
    return mNetworkHelper.getSingerImg(singer);
  }

  @Override
  public Observable<SongUrl> getSongUrl(String data) {
    return mNetworkHelper.getSongUrl(data);
  }

  @Override
  public void insertAllAlbumSong(List<AlbumSong.DataBean.ListBean> songList) {
    mDbHelper.insertAllAlbumSong(songList);
  }

  @Override
  public List<LocalSong> getLocalMp3Info() {
    return mDbHelper.getLocalMp3Info();
  }

  @Override
  public boolean saveSong(List<LocalSong> localSongs) {
    return mDbHelper.saveSong(localSongs);
  }

  @Override
  public boolean queryLove(String songId) {
    return mDbHelper.queryLove(songId);
  }

  @Override
  public boolean saveToLove(Song song) {
    return mDbHelper.saveToLove(song);
  }

  @Override
  public boolean deleteFromLove(String songId) {
    return mDbHelper.deleteFromLove(songId);
  }

  @Override
  public void setPlayMode(int mode) {
    mPreferencesHelper.setPlayMode(mode);
  }

  @Override
  public int getPlayMode() {
    return mPreferencesHelper.getPlayMode();
  }
}