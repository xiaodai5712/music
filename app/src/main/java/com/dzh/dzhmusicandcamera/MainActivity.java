package com.dzh.dzhmusicandcamera;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import com.andexert.library.RippleView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dzh.dzhmusicandcamera.base.activity.BaseActivity;
import com.dzh.dzhmusicandcamera.base.entity.Song;
import com.dzh.dzhmusicandcamera.base.view.MainFragment;
import com.dzh.dzhmusicandcamera.event.OnlineSongErrorEvent;
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
    mCircleAnimator.setDuration(3000);
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

  }


  private void seekBarStart() {
    mSeekBarThread = new Thread(new SeekBarThread() );
    mSeekBarThread.start();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onOnlineSongErrorEvent(OnlineSongErrorEvent event) {
    showToast(getString(R.string.error_out_of_copyright));
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