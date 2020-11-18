package com.dzh.dzhmusicandcamera.base.presenter;

import com.dzh.dzhmusicandcamera.app.Constant;
import com.dzh.dzhmusicandcamera.base.entity.LocalSong;
import com.dzh.dzhmusicandcamera.contract.ILocalContract;
import com.dzh.dzhmusicandcamera.event.SongListNumEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Date: 2020/11/5
 * author: Dzh
 */
public class LocalPresenter extends BasePresenter<ILocalContract.View> implements ILocalContract.Presenter{
  @Override
  public void getLocalMp3Info() {
    List<LocalSong> localSongList = mModel.getLocalMp3Info();
    if (localSongList.size() == 0) {
      mView.showErrorView();
    } else {
      saveSong(localSongList);
    }
  }

  @Override
  public void saveSong(List<LocalSong> localSongs) {
    if (mModel.saveSong(localSongs)) {
      EventBus.getDefault().post(new SongListNumEvent(Constant.LIST_TYPE_LOCAL));
      mView.showToast("成功导入本地音乐");
      mView.showMusicList(localSongs);
    }
  }
}