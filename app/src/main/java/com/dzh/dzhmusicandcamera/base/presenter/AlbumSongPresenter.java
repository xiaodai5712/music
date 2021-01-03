package com.dzh.dzhmusicandcamera.base.presenter;

import android.util.Log;

import com.dzh.dzhmusicandcamera.app.Constant;
import com.dzh.dzhmusicandcamera.base.entity.AlbumSong;
import com.dzh.dzhmusicandcamera.base.observer.BaseObserver;
import com.dzh.dzhmusicandcamera.base.view.search.AlbumSongFragment;
import com.dzh.dzhmusicandcamera.contract.IAlbumSongContract;

import java.net.UnknownHostException;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Date: 2020/12/23
 * author: Dzh
 */
public class AlbumSongPresenter extends BasePresenter<IAlbumSongContract.View>
    implements IAlbumSongContract.Presenter{

  private static final String TAG = "DzhAlbumSongPresenter";
  @Override
  public void getAlbumDetail(String id, int type) {
    addRxSubscribe(
        mModel.getAlbumSong(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new BaseObserver<AlbumSong>(mView) {
              @Override
              protected void onStart() {
                mView.showLoading();
              }

              @Override
              public void onNext(AlbumSong value) {
                super.onNext(value);
                mView.hideLoading();
                if (value.getCode() == 0) {
                  if (type == AlbumSongFragment.ALBUM_SONG) {
                    insertAllAlbumSong(value.getData().getList());
                  } else {
                    mView.showAlbumMessage(
                        value.getData().getName()
                        , value.getData().getLan()
                        , value.getData().getCompany()
                        , value.getData().getGenre()
                        , value.getData().getDesc()
                    );
                  }
                } else {
                  mView.showAlbumSongError();
                }
              }

              @Override
              public void onError(Throwable e) {
                e.printStackTrace();
                Log.d(TAG, "onError: " + e.toString());
                mView.hideLoading();
                if (e instanceof UnknownHostException && type == AlbumSongFragment.ALBUM_SONG) {
                  mView.showNetError();
                } else {
                  mView.showAlbumSongError();
                }
              }
            })
    );
  }

  @Override
  public void insertAllAlbumSong(List<AlbumSong.DataBean.ListBean> songList) {
    mModel.insertAllAlbumSong(songList);
    mView.setAlbumSongList(songList);
  }
}