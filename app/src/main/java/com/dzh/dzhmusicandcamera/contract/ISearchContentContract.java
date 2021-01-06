package com.dzh.dzhmusicandcamera.contract;

import com.dzh.dzhmusicandcamera.base.entity.Album;
import com.dzh.dzhmusicandcamera.base.entity.SearchSong;
import com.dzh.dzhmusicandcamera.base.entity.Song;
import com.dzh.dzhmusicandcamera.base.presenter.IPresenter;
import com.dzh.dzhmusicandcamera.base.view.BaseView;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceConfigurationError;

/**
 * Date: 2021/1/4
 * author: Dzh
 */
public interface ISearchContentContract {
  interface View extends BaseView {
    void setSongList(ArrayList<SearchSong .DataBean.SongBean.ListBean> songListBeans); // 显示歌曲列表
    void searchMoreSuccess(ArrayList<SearchSong.DataBean.SongBean.ListBean> songListBeans); // 搜索更多内容成功
    void searchMoreError(); // 搜索更多内容失败
    void searchMore(); // 搜索更多
    void showSearchMoreNetworkError(); // 下拉刷新网络错误
    void searchAlbumSuccess(List<Album .DataBean.AlbumBean.ListBean> albumList); // 搜索专辑成功
    void searchAlbumMoreSuccess(List<Album.DataBean.AlbumBean.ListBean> songLIstBeans); // 搜索更多内容成功
    void searchAlbumError(); // 获取专辑失败
    void getSongUrlSuccess(Song song, String urlStr); // 成功获取歌曲 url
  }

  interface Presenter extends IPresenter<View> {
    void search(String seek, int offset); // 搜索
    void searchMore(String seek, int offset); // 搜索更多
    void searchAlbum(String seek, int offset); // 搜索专辑
    void searchAlbumMore(String seek, int offset); // 搜索更多专辑
    void getSongUrl(Song song); // 得到歌曲播放的URL
  }
}
