package com.dzh.dzhmusicandcamera.contract;

import com.dzh.dzhmusicandcamera.base.entity.LocalSong;
import com.dzh.dzhmusicandcamera.base.presenter.IPresenter;
import com.dzh.dzhmusicandcamera.base.view.BaseView;

import java.util.List;

/**
 * Date: 2020/11/5
 * author: Dzh
 */
public interface ILocalContract {

  interface View extends BaseView {
    void showMusicList(List<LocalSong> mp3InfoLIst); // 显示本地音乐
  }

  interface Presenter extends IPresenter<View> {
    void getLocalMp3Info(); // 得到本地音乐列表
    void saveSong(List<LocalSong> localSongs); // 将本地音乐放到数据库中
  }
}
