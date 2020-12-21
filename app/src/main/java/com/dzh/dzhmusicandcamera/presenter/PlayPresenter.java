package com.dzh.dzhmusicandcamera.presenter;

import android.util.Log;

import com.dzh.dzhmusicandcamera.app.Constant;
import com.dzh.dzhmusicandcamera.base.entity.OnlineSongLrc;
import com.dzh.dzhmusicandcamera.base.entity.SearchSong;
import com.dzh.dzhmusicandcamera.base.entity.SingerImg;
import com.dzh.dzhmusicandcamera.base.entity.Song;
import com.dzh.dzhmusicandcamera.base.observer.BaseObserver;
import com.dzh.dzhmusicandcamera.base.presenter.BasePresenter;
import com.dzh.dzhmusicandcamera.contract.IPlayContract;
import com.dzh.dzhmusicandcamera.model.https.api.RetrofitFactory;

import java.util.List;

import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Date: 2020/11/28
 * author: Dzh
 */
public class PlayPresenter extends BasePresenter<IPlayContract.View>
    implements IPlayContract.Presenter {
  private static final String TAG = "DzhPlayPresenter";
  @Override
  public void getSingerImg(String singer, String song, long duration) {
    Log.d(TAG, "getSingerImg: ");
    addRxSubscribe(
        RetrofitFactory.createRequestOfSinger().getSingerImg(singer)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
//            .subscribeWith()
            .doOnNext(singerImg ->
                mView.setSingerImg(singerImg.getResult().getArtists().get(0).getImg1v1Url()))
            .doOnError(SingerImg -> mView.showToast("获取不到歌手图片"))
            .observeOn(Schedulers.io())
            .flatMap((Function<SingerImg, ObservableSource<SearchSong>>)
                singerImg -> RetrofitFactory.createRequest().search(song, 1))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new BaseObserver<SearchSong>(mView) {
              @Override
              public void onNext(SearchSong value) {
                super.onNext(value);
                if (value.getCode() == 0) {
                  matchLrc(value.getData().getSong().getList(), duration);
                } else {
                  mView.getLrcError(null);
                }
              }

              @Override
              public void onError(Throwable e) {
                super.onError(e);
                mView.getLrcError(null);
              }
            })
    );
  }

  @Override
  public void getLrc(String songId, int type) {
    mModel.getOnlineSongLrc(songId)
        .subscribeOn(Schedulers.io())
        .subscribeWith(new BaseObserver<OnlineSongLrc>(mView, false, false) {
          @Override
          public void onNext(OnlineSongLrc value) {
            if (value.getCode() == 0) {
              String lrc = value.getLyric();
              // 如果是本地音乐， 就下了歌词保存起来
              if (type == Constant.SONG_LOCAL) {
                mView.saveLrc(lrc);
              } else {
                mView.getLrcError(null);
              }
            }
          }
        });
  }

  @Override
  public void getSongId(String song, long duration) {
    addRxSubscribe(
        mModel.search(song, 1)
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribeWith(new BaseObserver<SearchSong>(mView, true, true) {
                @Override
                public void onNext(SearchSong value) {
                  super.onNext(value);
                  if (value.getCode() == 0) {
                    matchSong(value.getData().getSong().getList(), duration);
                  } else {
                    mView.getLrcError(null);
                  }
                }
              }));

  }

  @Override
  public void setPlayMode(int mode) {
    mModel.setPlayMode(mode);
  }

  @Override
  public int getPlayMode() {
    return mModel.getPlayMode();
  }

  @Override
  public void queryLove(String songId) {
    mView.showLove(mModel.queryLove(songId));
  }

  @Override
  public void saveToLove(Song song) {
    if (mModel.saveToLove(song)) {
      mView.saveToLoveSuccess();
    }
  }

  @Override
  public void deleteFromLove(String songId) {
    if (mModel.deleteFromLove(songId)) {
      mView.sendUpdateCollection();
    }
  }

  // 匹配歌词
  private void matchLrc(List<SearchSong.DataBean.SongBean.ListBean> listBeans, long duration) {
    boolean isFind = false;
    for (SearchSong.DataBean.SongBean.ListBean listBean : listBeans) {
      if (duration == listBean.getInterval()) {
        isFind = true;
        mView.setLocalSongId(listBean.getSongmid());
      }
    }
    // 如果找不到歌曲id ，就传入找不到歌曲的消息
    if (!isFind) {
      mView.getLrcError(Constant.SONG_ID_UNFIND);
    }
  }

  private void matchSong(List<SearchSong.DataBean.SongBean.ListBean> listBeans, long duration) {
    boolean isFind = false;
    for (SearchSong.DataBean.SongBean.ListBean listBean : listBeans) {
      if (duration == listBean.getInterval()) {
        isFind = true;
        mView.getSongIdSuccess(listBean.getSongmid());
      }
    }
    // 如果找不到歌曲id,就传输找不到歌曲消息
    if (!isFind) {
      mView.getLrcError(Constant.SONG_ID_UNFIND);
    }
  }
}