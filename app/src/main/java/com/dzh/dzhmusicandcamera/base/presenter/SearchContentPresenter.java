package com.dzh.dzhmusicandcamera.base.presenter;

import android.util.Log;

import com.dzh.dzhmusicandcamera.app.Api;
import com.dzh.dzhmusicandcamera.base.entity.Album;
import com.dzh.dzhmusicandcamera.base.entity.SearchSong;
import com.dzh.dzhmusicandcamera.base.entity.Song;
import com.dzh.dzhmusicandcamera.base.entity.SongUrl;
import com.dzh.dzhmusicandcamera.base.observer.BaseObserver;
import com.dzh.dzhmusicandcamera.contract.ISearchContentContract;
import com.dzh.dzhmusicandcamera.model.https.api.RetrofitFactory;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Date: 2021/1/4
 * author: Dzh
 */
public class SearchContentPresenter extends BasePresenter<ISearchContentContract.View>
    implements ISearchContentContract.Presenter {

  private static final String TAG = "DzhSearchContentPresenter";

  @Override
  public void search(String seek, int offset) {
    addRxSubscribe(
        mModel.search(seek, offset)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new BaseObserver<SearchSong>(mView, true
                , true) {
              @Override
              public void onNext(SearchSong value) {
                super.onNext(value);
                if (value.getCode() == 0) {
                  mView.setSongList((ArrayList<SearchSong.DataBean.SongBean.ListBean>)
                      value.getData().getSong().getList());
                }
              }
            })
    );
  }

  @Override
  public void searchMore(String seek, int offset) {
    addRxSubscribe(
        mModel.search(seek, offset)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new BaseObserver<SearchSong>(mView, false, true) {
              @Override
              public void onNext(SearchSong searchSong) {
                super.onNext(searchSong);
                if (searchSong.getCode() == 0) {
                  ArrayList<SearchSong.DataBean.SongBean.ListBean> songListBeans =
                      (ArrayList<SearchSong.DataBean.SongBean.ListBean>) searchSong.getData().getSong().getList();
                  if (songListBeans.size() == 0) {
                    mView.searchMoreError();
                  } else {
                    mView.searchMoreSuccess(songListBeans);
                  }
                } else {
                  mView.searchMoreError();
                }
              }

              @Override
              public void onError(Throwable e) {
                super.onError(e);
                mView.showSearchMoreNetworkError();
              }
            }));
  }


  @Override
  public void searchAlbum(String seek, int offset) {
    addRxSubscribe(
        mModel.searchAlbum(seek, offset)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new BaseObserver<Album>(mView, true, true) {
              @Override
              public void onNext(Album album) {
                super.onNext(album);
                if (album.getCode() == 0) {
                  mView.searchAlbumSuccess(album.getData().getAlbum().getList());
                } else {
                  mView.searchAlbumError();
                }
              }
            }));
  }

  @Override
  public void searchAlbumMore(String seek, int offset) {
    addRxSubscribe(
        mModel.searchAlbum(seek, offset)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new BaseObserver<Album>(mView, false, true) {
              @Override
              public void onNext(Album album) {
                super.onNext(album);
                if (album.getCode() == 0) {
                  mView.searchAlbumMoreSuccess(album.getData().getAlbum().getList());
                } else {
                  mView.searchMoreError();
                }
              }

              @Override
              public void onError(Throwable e) {
                super.onError(e);
                mView.showSearchMoreNetworkError();
              }
            }));
  }

  @Override
  public void getSongUrl(Song song) {
    Log.d(TAG, "getSongUrl: " + Api.SONG_URL_DATA_LEFT + song.getSongId() + Api.SONG_URL_DATA_RIGHT);
    addRxSubscribe(
        RetrofitFactory.createRequestOfSongUrl()
            .getSongUrl(Api.SONG_URL_DATA_LEFT + song.getSongId() + Api.SONG_URL_DATA_RIGHT)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new BaseObserver<SongUrl>(mView, false, false) {
              @Override
              public void onNext(SongUrl value) {
                super.onNext(value);
                if (value.getCode() == 0) {
                  String sip = value.getReq_0().getData().getSip().get(0);
                  String purl = value.getReq_0().getData().getMidurlinfo().get(0).getPurl();
                  if (purl.equals("")) {
                    mView.showToast("该歌曲暂时没有版权， 搜索其他歌曲吧");
                  } else {
                    mView.getSongUrlSuccess(song, sip + purl);
                  }
                } else {
                  mView.showToast(value.getCode() + ": 获取不到歌曲播放地址");
                }
              }
            })
    );
  }
}