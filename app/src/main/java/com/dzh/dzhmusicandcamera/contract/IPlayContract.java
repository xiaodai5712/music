package com.dzh.dzhmusicandcamera.contract;

import com.dzh.dzhmusicandcamera.base.entity.Song;
import com.dzh.dzhmusicandcamera.base.presenter.IPresenter;
import com.dzh.dzhmusicandcamera.base.view.BaseView;

/**
 * Date: 2020/11/28
 * author: Dzh
 */
public interface IPlayContract {

  interface View extends BaseView {

    String getSingerName();  // 获取歌手姓名
    void getSingerAndLrc(); // 按钮点击事件, 获取封面和歌词
    void setSingerImg(String ImgUrl); // 将图片设置成背景
    void showLove(boolean love);

    void showLoveAnim();
    void saveToLoveSuccess();
    void sendUpdateCollection();
    void showLrc(String lrc);

    void getLrcError(String content);
    void setLocalSongId(String songId);
    void getSongIdSuccess(String songId);
    void saveLrc(String lrc);
  }

  interface Presenter extends IPresenter<View> {
    void getSingerImg(String singer, String song, long duration);
    void getLrc(String songId, int type);
    void getSongId(String song, long duration);
    void setPlayMode(int mode);
    int getPlayMode();

    void queryLove(String songId);
    void saveToLove(Song song);
    void deleteFromLove(String songId);
  }
}
