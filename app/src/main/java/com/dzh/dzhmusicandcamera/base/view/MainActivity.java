package com.dzh.dzhmusicandcamera.base.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.andexert.library.RippleView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.app.Constant;
import com.dzh.dzhmusicandcamera.base.activity.BaseActivity;
import com.dzh.dzhmusicandcamera.base.entity.Song;
import com.dzh.dzhmusicandcamera.base.view.main.PlayActivity;
import com.dzh.dzhmusicandcamera.event.OnlineSongErrorEvent;
import com.dzh.dzhmusicandcamera.event.SongStatusEvent;
import com.dzh.dzhmusicandcamera.service.DownloadService;
import com.dzh.dzhmusicandcamera.service.PlayerService;
import com.dzh.dzhmusicandcamera.util.CommonUtil;
import com.dzh.dzhmusicandcamera.util.FileUtil;
import com.dzh.dzhmusicandcamera.util.ServiceUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends BaseActivity {
  private static final String TAG = "DzhMainActivity";

  @BindView(R.id.sb_progress)
  SeekBar mSeekBar;
  @BindView(R.id.tv_song_name)
  TextView mSongNameTv;
  @BindView(R.id.song_next)
  RippleView mNextIv;
  @BindView(R.id.circle_img)
  CircleImageView mCoverIv;
  @BindView(R.id.linear_player)
  LinearLayout mLinear;
  @BindView(R.id.tv_singer)
  TextView mSingerTv;
  @BindView(R.id.btn_player)
  Button mPlayerBtn;

  private boolean isChange;
  private boolean isSeek;
  private boolean flag;
  private int time;

  private boolean isExistService;

  private ObjectAnimator mCircleAnimator;
  private Song mSong;
  private MediaPlayer mMediaPlayer;
  private Thread mSeekBarThread;
  private PlayerService.PlayStatusBinder mPlayStatusBinder;
  private DownloadService.DownloadBinder mDownloadBinder;

  private ServiceConnection mConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      mPlayStatusBinder = (PlayerService.PlayStatusBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }
  };

  // 绑定下载服务， dzh:这里面有个，我觉得绑定服务的代码不应该写在Activity里
  private ServiceConnection mDownloadConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      mDownloadBinder = (DownloadService.DownloadBinder) service;
      if (isExistService) {
        seekBarStart();
      }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
  };
  @Override
  protected void onStart() {
    super.onStart();
    Log.d(TAG, "onStart: ");
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.d(TAG, "onResume: ");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // 解绑 service
    unbindService(mConnection);
    unbindService(mDownloadConnection);
    // 将播放的服务提升至前台服务
    Intent playIntent = new Intent(MainActivity.this, PlayerService.class);
    // Android 8.0 以上
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      startForegroundService(playIntent);
    } else {
      startService(playIntent);
    }

    EventBus.getDefault().unregister(this);
    if (mSeekBarThread != null || mSeekBarThread.isAlive()) {
      mSeekBarThread.interrupt();
      Song song = FileUtil.getSong();
      song.setCurrentTime(mPlayStatusBinder.getCurrentTime());
      FileUtil.saveSong(song);
    }
  }

  @Override
  protected int getLayoutId() {
    Log.d(TAG, "getLayoutId: " + R.layout.activity_main);
    return R.layout.activity_main;
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  protected void initView() {
    Log.d(TAG, "initView: ");
    EventBus.getDefault().register(this);
    LitePal.getDatabase();

    // 设置属性动画
    mCircleAnimator =
        ObjectAnimator.ofFloat(mCoverIv, "rotation", 0.0f, 360.0f);
    mCircleAnimator.setDuration(30000);
    mCircleAnimator.setInterpolator(new LinearInterpolator()); // 这句要是不写的话，好像默认是线性插值器
    mCircleAnimator.setRepeatCount(ValueAnimator.INFINITE);
    mCircleAnimator.setRepeatMode(ValueAnimator.RESTART);

    mSong = FileUtil.getSong();
    if (mSong.getSongName() != null) {
      Log.d(TAG, "initView: initView: " + mSong.toString());
      mLinear.setVisibility(View.VISIBLE);
      mSongNameTv.setText(mSong.getSongName());
      mSingerTv.setText(mSong.getSinger());
      mSeekBar.setMax((int) mSong.getDuration());
      mSeekBar.setProgress((int) mSong.getCurrentTime());
      if(mSong.getImgUrl() == null) {
        CommonUtil.setSingerImg(MainActivity.this, mSong.getSinger(), mCoverIv);
      } else {
        Glide.with(this)
            .load(mSong.getImgUrl())
            .apply(RequestOptions.placeholderOf(R.drawable.welcome))
            .into(mCoverIv);
      }
    } else {
      mSongNameTv.setText("随心音乐");
      mSingerTv.setText("随心跳动， 开启你的音乐旅程");
      mCoverIv.setImageResource(R.drawable.jay);
    }
    // 如果播放服务还存活
    if (ServiceUtil.isServiceRunning(this, PlayerService.class.getName())) {
      mPlayerBtn.setSelected(true);
      mCircleAnimator.start();
      isExistService = true;
    }
    // 处理服务
    initService();
    addMainFragment();
  }

  @Override
  protected void initData() {

  }

  @Override
  protected void onClick() {
    // 进度条的时间监听
    mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        // 防止在拖动进度条进行进度设置时与Thread更新播放进度条冲突
        isChange = true;
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        if (mPlayStatusBinder.isPlaying()) {
          mPlayStatusBinder.getMediaPlayer().seekTo(seekBar.getProgress() * 1000);
        } else {
          time = seekBar.getProgress();
          isSeek = true;
        }
        isChange = false;
        seekBarStart();
      }
    });

    // 控制按钮，播放，暂停
    mPlayerBtn.setOnClickListener(v -> {
      mMediaPlayer = mPlayStatusBinder.getMediaPlayer();
      if (mPlayStatusBinder.isPlaying()) {
        time = mMediaPlayer.getCurrentPosition();
        mPlayStatusBinder.pause();
        flag = true; // 这个变量命取得不好， 没有意义，不知道是干啥的
      } else if (flag) {
        mPlayStatusBinder.resume();
        if (isSeek) {
          mMediaPlayer.seekTo(time * 1000);
          isSeek = false;
        }
      } else {  // 退出程序后重新打开的情况
        if (FileUtil.getSong().isOnline()) {
          mPlayStatusBinder.playOnline();
        } else {
          mPlayStatusBinder.play(FileUtil.getSong().getListType());
        }
        mMediaPlayer = mPlayStatusBinder.getMediaPlayer();
        mMediaPlayer.seekTo((int) mSong.getCurrentTime() * 1000);
      }
    });

    // 下一首
    mNextIv.setOnClickListener(v -> {
      if (FileUtil.getSong().getSongName() != null) {
        mPlayStatusBinder.next();
      }
      if (mPlayStatusBinder.isPlaying()) {
        mPlayerBtn.setSelected(true);
      } else {
        mPlayerBtn.setSelected(false);
      }
    });

    // 点击播放栏, 跳转到播放的主界面
    mLinear.setOnClickListener(v -> {
      if (FileUtil.getSong() != null) {
        Intent toPlayActivityIntent
            = new Intent(MainActivity.this, PlayActivity.class);
        // 播放情况
        if (mPlayStatusBinder.isPlaying()) {
          Song song = FileUtil.getSong();
          song.setCurrentTime(mPlayStatusBinder.getCurrentTime());
          FileUtil.saveSong(song);
          toPlayActivityIntent.putExtra(Constant.PLAYER_STATUS, Constant.SONG_PLAY);
        } else {
          // 暂停情况
          Song song = FileUtil.getSong();
          song.setCurrentTime(mSeekBar.getProgress());
          FileUtil.saveSong(song);
        }
        if (FileUtil.getSong().getImgUrl() != null) {
//          toPlayActivityIntent.putExtra()
        }
        // 如果版本大于21
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          startActivity(toPlayActivityIntent, ActivityOptions
              .makeSceneTransitionAnimation(MainActivity.this).toBundle());
        } else {
          startActivity(toPlayActivityIntent);
        }
      } else {
        showToast("随心跳动， 开启你的音乐旅程");
      }
    });
  }


  private void seekBarStart() {
    mSeekBarThread = new Thread(new SeekBarThread() );
    mSeekBarThread.start();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onOnlineSongErrorEvent(OnlineSongErrorEvent event) {
    showToast(getString(R.string.error_out_of_copyright));
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onSongStatusEvent(SongStatusEvent event) {
    int status = event.getSongStatus();
    if (status == Constant.SONG_RESUME) {
      mPlayerBtn.setSelected(true);
      mCircleAnimator.removeAllListeners();
      seekBarStart();
    } else if (status == Constant.SONG_PAUSE) {
      mPlayerBtn.setSelected(false);
      mCircleAnimator.pause();
    } else if (status == Constant.SONG_CHANGE) {
      mSong = FileUtil.getSong();
      mSongNameTv.setText(mSong.getSongName());
      mSingerTv.setText(mSong.getSinger());
      mSeekBar.setMax((int) mSong.getDuration());
      mPlayerBtn.setSelected(true);
      mCircleAnimator.start();
      seekBarStart();
      if (!mSong.isOnline()) {
        CommonUtil.setSingerImg(MainActivity.this, mSong.getSinger(), mCoverIv);
      } else {
        Glide.with(MainActivity.this)
            .load(mSong.getImgUrl())
            .apply(RequestOptions.placeholderOf(R.drawable.welcome))
            .apply(RequestOptions.errorOf(R.drawable.welcome))
            .into(mCoverIv);
      }
    }

  }
  private void initService() {
    // 启动服务
    Intent playIntent = new Intent(MainActivity.this, PlayerService.class);
    Intent downIntent = new Intent(MainActivity.this, DownloadService.class);

    // 退出程序后依然能播放
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      startForegroundService(playIntent);
    } else {
      startService(playIntent);
    }
    bindService(playIntent, mConnection, Context.BIND_AUTO_CREATE);
    bindService(downIntent, mDownloadConnection, Context.BIND_AUTO_CREATE);
  }

  private void addMainFragment() {
    Log.d(TAG, "addMainFragment: ");
    MainFragment mainFragment = new MainFragment();
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction transaction = fragmentManager.beginTransaction();
    transaction.add(R.id.fragment_container, mainFragment);
    transaction.commit();
  }


  class SeekBarThread implements Runnable {

    @Override
    public void run() {
      if (mPlayStatusBinder != null) {
        while (!isChange && mPlayStatusBinder.isPlaying()) {
          mSeekBar.setProgress((int) mPlayStatusBinder.getCurrentTime());
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

}