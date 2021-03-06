package com.dzh.dzhmusicandcamera.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.dzh.dzhmusicandcamera.base.view.MainActivity;
import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.app.Api;
import com.dzh.dzhmusicandcamera.app.Constant;
import com.dzh.dzhmusicandcamera.base.entity.DownloadSong;
import com.dzh.dzhmusicandcamera.base.entity.HistorySong;
import com.dzh.dzhmusicandcamera.base.entity.LocalSong;
import com.dzh.dzhmusicandcamera.base.entity.Love;
import com.dzh.dzhmusicandcamera.base.entity.OnlineSong;
import com.dzh.dzhmusicandcamera.base.entity.Song;
import com.dzh.dzhmusicandcamera.base.entity.SongUrl;
import com.dzh.dzhmusicandcamera.event.OnlineSongChangeEvent;
import com.dzh.dzhmusicandcamera.event.OnlineSongErrorEvent;
import com.dzh.dzhmusicandcamera.event.SongAlbumEvent;
import com.dzh.dzhmusicandcamera.event.SongCollectionEvent;
import com.dzh.dzhmusicandcamera.event.SongDownloadedEvent;
import com.dzh.dzhmusicandcamera.event.SongHistoryEvent;
import com.dzh.dzhmusicandcamera.event.SongListNumEvent;
import com.dzh.dzhmusicandcamera.event.SongLocalEvent;
import com.dzh.dzhmusicandcamera.event.SongStatusEvent;
import com.dzh.dzhmusicandcamera.model.https.api.RetrofitFactory;
import com.dzh.dzhmusicandcamera.util.CommonUtil;
import com.dzh.dzhmusicandcamera.util.DownloadUtil;
import com.dzh.dzhmusicandcamera.util.FileUtil;

import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;
import org.litepal.crud.callback.SaveCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Date: 2020/10/14
 * author: Dzh
 */
public class PlayerService extends Service {
  private static final String TAG = "PlayerService";
  private static final String NOTIFICATION_BAR_TITLE = "随心跳动，开启你的音乐旅程";

  private final int NOTIFICATION_ID = 98;

  private PlayStatusBinder mPlayStatusBinder = new PlayStatusBinder();
  private MediaPlayer mediaPlayer = new MediaPlayer();

  private boolean isPause;
  private boolean isPlaying;

  private List<LocalSong> mLocalSongList;
  private List<OnlineSong> mSongList;
  private List<Love> mLoveList;
  private List<HistorySong> mHistoryList;
  private List<DownloadSong> mDownloadList;

  private int mCurrent;
  private int mListType;
  private int mPlayMode = Constant.PLAY_ORDER; // 播放模式

  @Override
  public void onCreate() {
    super.onCreate();
    Log.d(TAG, "onCreate: true");
    mListType = FileUtil.getSong().getListType();
    if (mListType == Constant.LIST_TYPE_ONLINE) {
      mSongList = LitePal.findAll(OnlineSong.class);
    } else if (mListType == Constant.LIST_TYPE_LOCAL) {
      mLocalSongList = LitePal.findAll(LocalSong.class);
    } else if (mListType == Constant.LIST_TYPE_LOVE) {
      mLoveList = LitePal.findAll(Love.class);
    } else if (mListType == Constant.LIST_TYPE_HISTORY) {
      mHistoryList = orderHistoryList(LitePal.findAll(HistorySong.class));
      // 保证最近播放列表，一开始总是第一个
      Song song = FileUtil.getSong();
      song.setPosition(0);
      FileUtil.saveSong(song);
    } else if (mListType == Constant.LIST_TYPE_DOWNLOAD) {
      mDownloadList = orderDownloadList(DownloadUtil.getSongFromFile(Api.STORAGE_SONG_FILE));
    }

    // 开启前台服务
    startForeground(NOTIFICATION_ID, getNotification(NOTIFICATION_BAR_TITLE));
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    Log.d(TAG, "onBind: jsyjst");
    mediaPlayer.setOnCompletionListener(mp -> {
      EventBus.getDefault().post(new SongStatusEvent(Constant.SONG_PAUSE));
      mCurrent = FileUtil.getSong().getPosition();
      // 将歌曲信息保存起来
      if (mListType == Constant.LIST_TYPE_LOCAL) {
        mCurrent = getNextCurrent(mCurrent, mPlayMode, mLocalSongList.size()); // 根据播放模式来播放下一曲
        saveLocalSongInfo(mCurrent);
      } else if (mListType == Constant.LIST_TYPE_ONLINE) {
        mCurrent = getNextCurrent(mCurrent, mPlayMode, mSongList.size());
        saveOnlineSongInfo(mCurrent);
      } else if (mListType == Constant.LIST_TYPE_LOVE) {
        mCurrent = getNextCurrent(mCurrent, mPlayMode, mLoveList.size());//根据播放模式来播放下一曲
        saveLoveInfo(mCurrent);
      } else if (mListType == Constant.LIST_TYPE_HISTORY) {
        mCurrent = getNextCurrent(mCurrent, mPlayMode, mHistoryList.size());//根据播放模式来播放下一曲
        saveHistoryInfo(mCurrent);
      } else if (mListType == Constant.LIST_TYPE_DOWNLOAD) {
        mCurrent = getNextCurrent(mCurrent, mPlayMode, mDownloadList.size());//根据播放模式来播放下一曲
        saveDownloadInfo(mCurrent);
      }
      if (mListType != 0) {
        mPlayStatusBinder.play(mListType);
      } else {
        mPlayStatusBinder.stop();
      }
    });

    /*
     * MediaPlayer 切歌进入setOnCompletionListener的问题
     * 因为直接切歌回发生错误，所以增加错误监听器，返回true 就不回调onCompletion方法了
     */
    mediaPlayer.setOnErrorListener((mp, what, extra) -> true);
    return mPlayStatusBinder;
  }

  private void saveLocalSongInfo(int current) {
    // 将歌曲的信息保存起来
    mLocalSongList = LitePal.findAll(LocalSong.class);
    Song song = new Song();
    LocalSong localSong = mLocalSongList.get(current);
    song.setPosition(current);
    song.setSongName(localSong.getName());
    song.setSinger(localSong.getSinger());
    song.setDuration(localSong.getDuration());
    song.setUrl(localSong.getUrl());
    song.setImgUrl(localSong.getPic());
    song.setSongId(localSong.getSongId());
    song.setQqId(localSong.getQqId());
    song.setOnline(false);
    song.setListType(Constant.LIST_TYPE_LOCAL);
    FileUtil.saveSong(song);
  }


  //保存网络专辑列表的信息
  private void saveOnlineSongInfo(int current) {
    mSongList = LitePal.findAll(OnlineSong.class);
    Song song = new Song();
    song.setPosition(current);
    song.setSongId(mSongList.get(current).getSongId());
    song.setSongName(mSongList.get(current).getName());
    song.setSinger(mSongList.get(current).getSinger());
    song.setDuration(mSongList.get(current).getDuration());
    song.setUrl(mSongList.get(current).getUrl());
    song.setImgUrl(mSongList.get(current).getPic());
    song.setOnline(true);
    song.setListType(Constant.LIST_TYPE_ONLINE);
    song.setMediaId(mSongList.get(current).getMediaId());
    FileUtil.saveSong(song);
  }

  //保存我的收藏的列表的信息
  private void saveLoveInfo(int current) {
    mLoveList = orderList(LitePal.findAll(Love.class));
    Love love = mLoveList.get(current);
    Song song = new Song();
    song.setPosition(current);
    song.setSongId(love.getSongId());
    song.setQqId(love.getQqId());
    song.setSongName(love.getName());
    song.setSinger(love.getSinger());
    song.setUrl(love.getUrl());
    song.setImgUrl(love.getPic());
    song.setListType(Constant.LIST_TYPE_LOVE);
    song.setOnline(love.isOnline());
    song.setDuration(love.getDuration());
    song.setMediaId(love.getMediaId());
    song.setDownload(love.isDownload());
    FileUtil.saveSong(song);
  }


  //保存下载列表的信息
  private void saveDownloadInfo(int current) {
    DownloadSong downloadSong = mDownloadList.get(current);
    Song song = new Song();
    song.setPosition(current);
    song.setSongId(downloadSong.getSongId());
    song.setSongName(downloadSong.getName());
    song.setSinger(downloadSong.getSinger());
    song.setUrl(downloadSong.getUrl());
    song.setImgUrl(downloadSong.getPic());
    song.setListType(Constant.LIST_TYPE_DOWNLOAD);
    song.setOnline(false);
    song.setDuration(downloadSong.getDuration());
    song.setMediaId(downloadSong.getMediaId());
    song.setDownload(true);
    FileUtil.saveSong(song);
  }

  //保存我的收藏的列表的信息
  private void saveHistoryInfo(int current) {
    HistorySong historySong = mHistoryList.get(current);
    Song song = new Song();
    song.setPosition(current);
    song.setSongId(historySong.getSongId());
    song.setQqId(historySong.getQqId());
    song.setSongName(historySong.getName());
    song.setSinger(historySong.getSinger());
    song.setUrl(historySong.getUrl());
    song.setImgUrl(historySong.getPic());
    song.setListType(Constant.LIST_TYPE_HISTORY);
    song.setOnline(historySong.isOnline());
    song.setDuration(historySong.getDuration());
    song.setMediaId(historySong.getMediaId());
    song.setDownload(historySong.isDownload());
    FileUtil.saveSong(song);
  }

  private List<HistorySong> orderHistoryList(List<HistorySong> tempList) {
    List<HistorySong> historySongList = new ArrayList<>();
    historySongList.clear();
    for (int i = tempList.size() - 1; i >= 0; i--) {
      historySongList.add(tempList.get(i));
    }
    return historySongList;
  }

  private List<DownloadSong> orderDownloadList(List<DownloadSong> tempList) {
    List<DownloadSong> downloadSongList = new ArrayList<>();
    downloadSongList.clear();
    for (int i = tempList.size() - 1; i >= 0; i--) {
      downloadSongList.add(tempList.get(i));
    }
    return downloadSongList;
  }

  public class PlayStatusBinder extends Binder {
    public void setPlayMode(int mode) {
      mPlayMode = mode;
    }

    public void getHistoryList() {
      mHistoryList = orderHistoryList(LitePal.findAll(HistorySong.class));
      // 保证最近播放列表一开始总时第一个
      Song song = FileUtil.getSong();
      song.setPosition(0);
      FileUtil.saveSong(song);
    }

    // 播放音乐
    public void play(int listType) {
      try {
        mListType = listType;
        if (mListType == Constant.LIST_TYPE_ONLINE) {
          mSongList = LitePal.findAll(OnlineSong.class);
          EventBus.getDefault().post(new SongAlbumEvent());
        } else if (mListType == Constant.LIST_TYPE_LOCAL) {
          mLocalSongList = LitePal.findAll(LocalSong.class);
          EventBus.getDefault().post(new SongLocalEvent()); // 发送本地歌曲改变事件
        } else if (mListType == Constant.LIST_TYPE_LOVE) {
          mLoveList = orderList(LitePal.findAll(Love.class));
          EventBus.getDefault().post(new SongCollectionEvent(true)); // 发送歌曲改变事件
        } else if (mListType == Constant.LIST_TYPE_HISTORY) {
          EventBus.getDefault().post(new SongHistoryEvent());
        } else if (mListType == Constant.LIST_TYPE_DOWNLOAD) {
          mDownloadList = orderDownloadList(DownloadUtil.getSongFromFile(Api.STORAGE_SONG_FILE));
          EventBus.getDefault().post(new SongDownloadedEvent()); // 发动下载歌曲改变的消息
        }
        mCurrent = FileUtil.getSong().getPosition();
        mediaPlayer.reset(); // 把各项参数恢复到初始状态
        if (mListType == Constant.LIST_TYPE_LOCAL) {
          mediaPlayer.setDataSource(mLocalSongList.get(mCurrent).getUrl());
          startPlay();
        } else if (mListType == Constant.LIST_TYPE_ONLINE) {
          getSongUrl(mSongList.get(mCurrent).getSongId());
        } else if (mListType == Constant.LIST_TYPE_LOVE) {
          mediaPlayer.setDataSource(mLoveList.get(mCurrent).getUrl());
          startPlay();
        } else if (mListType == Constant.LIST_TYPE_HISTORY) {
          mediaPlayer.setDataSource(mHistoryList.get(mCurrent).getUrl());
          startPlay();
        } else if (mListType == Constant.LIST_TYPE_DOWNLOAD) {
          Log.d(TAG, "play: " + mDownloadList.get(mCurrent).getUrl());
          mediaPlayer.setDataSource(mDownloadList.get(mCurrent).getUrl());
          startPlay();
        }
      } catch (IOException e) {
        e.printStackTrace();
        Log.e(TAG, "IOException",e);
      }
    }

    // 播放搜索歌曲
    public void playOnline() {
      try {
        mediaPlayer.reset();
        mediaPlayer.setDataSource(FileUtil.getSong().getUrl());
        mediaPlayer.prepare();
        isPlaying = true;
        saveToHistoryTable();
        mediaPlayer.start();
        EventBus.getDefault().post(new OnlineSongChangeEvent()); // 发送网络歌曲改变事件
        EventBus.getDefault().post(new SongStatusEvent(Constant.SONG_CHANGE));
        // 改变通知栏歌曲
        Song song = FileUtil.getSong();
        getNotificationManager().notify(NOTIFICATION_ID, getNotification(song.getSongName()
            + "-" + song.getSinger()));
      } catch (Exception e) {
        EventBus.getDefault().post(new OnlineSongErrorEvent());
        e.printStackTrace();
      }
    }

    // 暂停音乐
    public void pause() {
      if (mediaPlayer != null && mediaPlayer.isPlaying()) {
        isPlaying = false;
        mediaPlayer.pause();
        isPause = true;
        EventBus.getDefault().post(new SongStatusEvent(Constant.SONG_PAUSE));
      }
    }

    public void resume() {
      if (isPause) {
        mediaPlayer.start();
        isPlaying = true;  // 纠正之前的错误
        isPause = false;
        EventBus.getDefault().post(new SongStatusEvent(Constant.SONG_RESUME));
      }
    }

    public void next() {
      EventBus.getDefault().post(new SongStatusEvent(Constant.SONG_RESUME));
      mCurrent = FileUtil.getSong().getPosition();
      if (mListType == Constant.LIST_TYPE_LOCAL) {
        mCurrent = getNextCurrent(mCurrent, mPlayMode, mLocalSongList.size()); //根据播放模式来播放下一曲
        saveLocalSongInfo(mCurrent);
      } else if (mListType == Constant.LIST_TYPE_ONLINE) {
        mCurrent = getNextCurrent(mCurrent, mPlayMode, mSongList.size());//根据播放模式来播放下一曲
        saveOnlineSongInfo(mCurrent);
      } else if (mListType == Constant.LIST_TYPE_LOVE) {
        mCurrent = getNextCurrent(mCurrent, mPlayMode, mLoveList.size());//根据播放模式来播放下一曲
        saveLoveInfo(mCurrent);
      } else if (mListType == Constant.LIST_TYPE_HISTORY) {
        mCurrent = getNextCurrent(mCurrent, mPlayMode, mHistoryList.size());//根据播放模式来播放下一曲
        saveHistoryInfo(mCurrent);
      } else if (mListType == Constant.LIST_TYPE_DOWNLOAD) {
        mCurrent = getNextCurrent(mCurrent, mPlayMode, mDownloadList.size());//根据播放模式来播放下一曲
        saveDownloadInfo(mCurrent);
      }
      if (mListType != 0) mPlayStatusBinder.play(mListType);
    }

    public void last() {
      EventBus.getDefault().post(new SongStatusEvent(Constant.SONG_RESUME));//暂停广播
      mCurrent = FileUtil.getSong().getPosition();
      if (mListType == Constant.LIST_TYPE_LOCAL) {
        mCurrent = getLastCurrent(mCurrent, mPlayMode, mLocalSongList.size());
        saveLocalSongInfo(mCurrent);
      } else if (mListType == Constant.LIST_TYPE_ONLINE) {
        mCurrent = getLastCurrent(mCurrent, mPlayMode, mSongList.size());
        saveOnlineSongInfo(mCurrent);
      } else if (mListType == Constant.LIST_TYPE_LOVE) {
        mCurrent = getLastCurrent(mCurrent, mPlayMode, mLoveList.size());
        saveLoveInfo(mCurrent);
      } else if (mListType == Constant.LIST_TYPE_HISTORY) {
        mCurrent = getLastCurrent(mCurrent, mPlayMode, mHistoryList.size());
        saveHistoryInfo(mCurrent);
      } else if (mListType == Constant.LIST_TYPE_DOWNLOAD) {
        mCurrent = getLastCurrent(mCurrent, mPlayMode, mDownloadList.size());
        saveDownloadInfo(mCurrent);
      }
      if (mListType != 0) mPlayStatusBinder.play(mListType);
    }

    // 暂停音乐
    public void stop() {
      if (mediaPlayer != null) {
        isPause = false;
        mediaPlayer.stop();
        try {
          mediaPlayer.prepare(); // 在调用stop后， 如果需要再次通过start进行播放，需要在之前调用prepare函数
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    public boolean isPlaying() {
      return isPlaying;
    }

    public MediaPlayer getMediaPlayer() {
      return mediaPlayer;
    }

    public PlayerService getPlayerService() {
      return PlayerService.this;
    }

    public long getCurrentTime() {
      return mediaPlayer.getCurrentPosition() / 1000;
    }
  }

  // 设置通知标题栏
  private Notification getNotification(String title) {
    Intent intent = new Intent(this, MainActivity.class);
    PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      String id = "play";
      String name = "播放歌曲";
      NotificationChannel mChannel = new NotificationChannel(id, name
          , NotificationManager.IMPORTANCE_LOW);
      getNotificationManager().createNotificationChannel(mChannel);
      Notification.Builder builder = new Notification.Builder(this, id);
      builder.setSmallIcon(R.mipmap.icon);
      builder.setContentIntent(pi);
      builder.setContentTitle(title);
      return builder.build();
    } else {
      NotificationCompat.Builder builder = new NotificationCompat
          .Builder(this, "play");
      builder.setSmallIcon(R.mipmap.icon);
      builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.icon));
      builder.setContentIntent(pi);
      builder.setContentTitle(title);
      return builder.build();
    }
  }

  // 根据播放模式得到下一首歌曲的位置
  private int getNextCurrent(int current, int playMode, int len) {
    int res;
    if (playMode == Constant.PLAY_ORDER) {
      res = (current + 1) % len;
    } else if (playMode == Constant.PLAY_RANDOM) {
      res = (current + (int) (Math.random() * len)) % len;
    } else {
      res = current;
    }
    return res;
  }

  // 开始播放
  private void startPlay() throws IOException {
    mediaPlayer.prepare(); // 进行缓冲
    isPlaying = true;
    mediaPlayer.start();
    saveToHistoryTable();
    EventBus.getDefault().post(new SongStatusEvent(Constant.SONG_CHANGE)); // 发送所有歌曲改变事件
    EventBus.getDefault().post(new OnlineSongChangeEvent());
    // 改变通知栏歌曲
    Song song = FileUtil.getSong();
    getNotificationManager()
        .notify(NOTIFICATION_ID, getNotification(song.getSongName() + "-" + song.getSinger()));
  }

  // 将歌曲保存到最近播放的数据库中
  private void saveToHistoryTable() {

    final Song song = FileUtil.getSong();
    LitePal.where("songId=?", song.getSongId()).findAsync((HistorySong.class))
        .listen(list -> {
          if (list.size() == 1) {
            LitePal.deleteAll(HistorySong.class, "songId=?", song.getSongId());
          }
          final HistorySong history = new HistorySong();
          history.setSongId(song.getSongId());
          history.setQqId(song.getQqId());
          history.setName(song.getSongName());
          history.setSinger(song.getSinger());
          history.setUrl(song.getUrl());
          history.setPic(song.getImgUrl());
          history.setOnline(song.isOnline());
          history.setDuration(song.getDuration());
          history.setMediaId(song.getMediaId());
          history.setDownload(song.isDownload());
          history.saveAsync().listen(new SaveCallback() {

            @Override
            public void onFinish(boolean success) {
              if (success) {
                // 告诉主界面最近播放的数目需要改变
                EventBus.getDefault().post(new SongListNumEvent(Constant.LIST_TYPE_HISTORY));
                if (LitePal.findAll(HistorySong.class).size() > Constant.HISTORY_MAX_SIZE) {
                  LitePal.delete(HistorySong.class, LitePal.findFirst(HistorySong.class).getId());
                }
              }
            }
          });
        });
  }

  // 对数据库进行倒序排序
  private List orderList(List<Love> tempList) {
    List<Love> loveList = new ArrayList<>();
    loveList.clear();
    for (int i = tempList.size() - 1; i >= 0; i--) {
      loveList.add(tempList.get(i));
    }
    return loveList;
  }

  // 开启前台服务
  private NotificationManager getNotificationManager() {
    return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
  }

  // 网络请求获取播放地址
  private void getSongUrl(String songId) {
    Log.d(TAG, "getSongUrl: " + Api.SONG_URL_DATA_LEFT + songId + Api.SONG_URL_DATA_RIGHT);
    RetrofitFactory.createRequestOfSongUrl().getSongUrl(Api.SONG_URL_DATA_LEFT + songId + Api.SONG_URL_DATA_RIGHT)
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(new Observer<SongUrl>() {
          @Override
          public void onSubscribe(Disposable d) {

          }

          @Override
          public void onNext(SongUrl songUrl) {
            if (songUrl.getCode() == 0) {
              String sip = songUrl.getReq_0().getData().getSip().get(0);
              String purl = songUrl.getReq_0().getData().getMidurlinfo().get(0).getPurl();
              if (purl.equals("")) {
                CommonUtil.showToast(PlayerService.this, "该歌曲没有版权，试试汽其他的吧");
                return;
              }
              Song song = FileUtil.getSong();
              assert song != null;
              song.setUrl(sip + purl);
              FileUtil.saveSong(song);
              try {
                mediaPlayer.setDataSource(sip + purl);
                Log.d(TAG, "onNext: jsyjst = " + sip + purl);
                startPlay();
              } catch (IOException e) {
                e.printStackTrace();
              }
            } else {
              Log.d(TAG, "onNext: " + songUrl.getCode() + ": 获取不到歌曲播放地址");
            }
          }

          @Override
          public void onError(Throwable e) {
            Log.d(TAG, "onError: " + e.toString());
          }

          @Override
          public void onComplete() {

          }
        });
  }

  // 根据播放模式得到上一首歌曲的位置
  private int  getLastCurrent(int current, int playMode, int len) {
    int res;
    if (playMode == Constant.PLAY_ORDER) {
      res = current - 1 == -1 ? len - 1 : current - 1;
    } else if (playMode == Constant.PLAY_RANDOM) {
      res = (current + (int ) (Math.random() * len)) % len;
    } else {
      res = current;
    }
    return res;
  }
}
