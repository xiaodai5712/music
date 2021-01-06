package com.dzh.dzhmusicandcamera.contract;

import com.dzh.dzhmusicandcamera.base.entity.AlbumSong;
import com.dzh.dzhmusicandcamera.base.presenter.IPresenter;
import com.dzh.dzhmusicandcamera.base.view.BaseView;

import java.util.List;

/**
 * Date: 2020/12/23
 * author: Dzh
 */
public interface IAlbumSongContract {
  interface View extends BaseView {
    void setAlbumSongList(List<AlbumSong.DataBean.ListBean> songList); // 成功获取专辑歌曲后填充列表
    void showAlbumSongError(); // 获取专辑失败
    void showAlbumMessage(String name, String language, String company, String albumType
        , String desc); // 展示专辑详情
    void showLoading(); // 显示进度
    void hideLoading(); // 隐藏进度
    void showNetError(); // 显示网络错误
  }

  interface Presenter extends IPresenter<View> {
    void getAlbumDetail(String id, int type); // 获取更多的专辑信息
    void insertAllAlbumSong(List<AlbumSong.DataBean.ListBean> dadaBean); // 讲专辑歌曲添加到曲库
  }
}
