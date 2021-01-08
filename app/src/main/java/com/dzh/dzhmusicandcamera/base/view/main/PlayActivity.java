package com.dzh.dzhmusicandcamera.base.view.main;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.transition.Slide;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.app.Api;
import com.dzh.dzhmusicandcamera.app.Constant;
import com.dzh.dzhmusicandcamera.base.activity.BaseMvpActivity;
import com.dzh.dzhmusicandcamera.base.entity.DownloadInfo;
import com.dzh.dzhmusicandcamera.base.entity.DownloadSong;
import com.dzh.dzhmusicandcamera.base.entity.LocalSong;
import com.dzh.dzhmusicandcamera.base.entity.Song;
import com.dzh.dzhmusicandcamera.contract.IPlayContract;
import com.dzh.dzhmusicandcamera.event.DownloadEvent;
import com.dzh.dzhmusicandcamera.event.SongCollectionEvent;
import com.dzh.dzhmusicandcamera.event.SongStatusEvent;
import com.dzh.dzhmusicandcamera.presenter.PlayPresenter;
import com.dzh.dzhmusicandcamera.service.DownloadService;
import com.dzh.dzhmusicandcamera.service.PlayerService;
import com.dzh.dzhmusicandcamera.util.CommonUtil;
import com.dzh.dzhmusicandcamera.util.DisplayUtil;
import com.dzh.dzhmusicandcamera.util.FastBlurUtil;
import com.dzh.dzhmusicandcamera.util.FileUtil;
import com.dzh.dzhmusicandcamera.util.MediaUtil;
import com.dzh.dzhmusicandcamera.util.ScreenUtil;
import com.dzh.dzhmusicandcamera.widget.BackgroundAnimationRelativeLayout;
import com.dzh.dzhmusicandcamera.widget.DiscView;
import com.dzh.dzhmusicandcamera.widget.lyric.LrcView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.util.List;
import java.util.SimpleTimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayActivity extends BaseMvpActivity<PlayPresenter> implements IPlayContract.View {
  private static final String TAG = "DzhPlayActivity";
  @BindView(R.id.iv_back)
  ImageView mBackIv;
  @BindView(R.id.tv_song)
  TextView mSongTv;
  @BindView(R.id.tv_singer)
  TextView mSingerTv;
  @BindView(R.id.relative_song)
  RelativeLayout relativeSong;
  @BindView(R.id.lrcView)
  LrcView mLrcView;
  @BindView(R.id.iv_disc_background)
  ImageView mDiscImg;
  @BindView(R.id.iv_needle)
  ImageView ivNeedle;
  @BindView(R.id.btn_get_img_lrc)
  Button mGetImgAndLrcBtn;
  @BindView(R.id.downloadIv)
  ImageView mDownloadIv;
  @BindView(R.id.tv_current_time)
  TextView mCurrentTimeTv;
  @BindView(R.id.seek)
  SeekBar mSeekBar;
  @BindView(R.id.tv_duration_time)
  TextView mDurationTimeTv;
  @BindView(R.id.relative_music_time)
  RelativeLayout relativeMusicTime;
  @BindView(R.id.btn_order)
  Button mPlayModeBtn;
  @BindView(R.id.btn_last)
  Button mLastBtn;
  @BindView(R.id.linear_control)
  LinearLayout linearControl;
  @BindView(R.id.relative_control)
  RelativeLayout relativeControl;
  @BindView(R.id.relative_root)
  BackgroundAnimationRelativeLayout mRootLayout;

  private DiscView mDisc;
  private Button mNextBtn;
  private Button mLoveBtn;
  private Button mPlayBtn;

  private PlayPresenter mPresenter;

  private boolean mIsOnline; // 判断是否为网络
  private int mListType; // 列表类型
  private int mPlayStatus;

  private int mPlayMode; // 播放模式

  private boolean mIsChange; // 拖动进度条
  private boolean mIsSeek; // 标记是否在暂停的时候拖动进度条
  private boolean mFlag; // 用作暂停的标记
  private int mTime; // 记录暂停的时间
  private boolean mIsPlaying;
  private Song mSong;
  private MediaPlayer mMediaPlayer;

  private RelativeLayout mPlayRelative;

  private String mLrc = null;

  private boolean mIsLove; // 是否已经在我喜欢的列表中
  private Bitmap mImgBmp;
  private List<LocalSong> mLocalSong; // 用来判断是否有本地照片
  // 服务
  private PlayerService.PlayStatusBinder mPlayStatusBinder;
  private DownloadService.DownloadBinder mDownloadBinder;

  @SuppressLint("HandlerLeak")
  private Handler mMusicHandler = new Handler() {
    @Override
    public void handleMessage(@NonNull Message msg) {
      super.handleMessage(msg);
      if (!mIsChange) {
        mSeekBar.setProgress((int) mPlayStatusBinder.getCurrentTime());
        mCurrentTimeTv.setText(MediaUtil.formatTime(mSeekBar.getProgress()));
        startUpdateSeekBarProgress();
      }
    }
  };
  // 播放
  private ServiceConnection mPlayConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      mPlayStatusBinder = (PlayerService.PlayStatusBinder) service;
      // 播放模式
      mPlayMode = mPresenter.getPlayMode(); // 获取播放模式
      mPlayStatusBinder.setPlayMode(mPlayMode);
      mIsOnline = FileUtil.getSong().isOnline();
      if (mIsOnline) {
        mGetImgAndLrcBtn.setVisibility(View.GONE);
        setSingerImg(FileUtil.getSong().getImgUrl());
        if (mPlayStatus == Constant.SONG_PLAY) {
          mDisc.play();
          mPlayBtn.setSelected(true);
          startUpdateSeekBarProgress();
        }
      } else {
        setLocalImg(mSong.getSinger());
      }
    }
    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
  };

  private ServiceConnection mDownloadConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      mDownloadBinder = (DownloadService.DownloadBinder) service;
      showToast("onServiceConnected");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      showToast("onServiceDisconnected");
    }
  };


  @Override
  protected int getLayoutId() {
    return R.layout.activity_play;
  }

  @Override
  protected void initData() {
    mPresenter.queryLove(mSong.getSongId()); // 查找歌曲是否为我喜欢的歌曲
    if (mPlayStatus == Constant.SONG_PLAY) {
      mDisc.play();
      mPlayBtn.setSelected(true);
      startUpdateSeekBarProgress();
    }
  }

  @Override
  protected void initView() {
    super.initView();
    mNextBtn = findViewById(R.id.next);
    mDisc = findViewById(R.id.disc_view);
    mLoveBtn = findViewById(R.id.btn_love);
    mPlayBtn = findViewById(R.id.btn_player);
    EventBus.getDefault().register(this);
    // 设置进入 退出动画
    getWindow().setEnterTransition(new Slide());
    getWindow().setExitTransition(new Slide());
    // 判断播放状态
    mPlayStatus = getIntent().getIntExtra(Constant.PLAYER_STATUS, 2);
    // 绑定服务
    Intent playIntent = new Intent(PlayActivity.this, PlayerService.class);
    Intent downloadIntent = new Intent(PlayActivity.this, DownloadService.class);
    bindService(playIntent, mPlayConnection, Context.BIND_AUTO_CREATE);
    bindService(downloadIntent, mDownloadConnection, Context.BIND_AUTO_CREATE);

    // 界面填充
    mSong = FileUtil.getSong();
    mListType = mSong.getListType();
    mSingerTv.setText(mSong.getSinger());
    mSongTv.setText(mSong.getSongName());
    mCurrentTimeTv.setText(MediaUtil.formatTime(mSong.getCurrentTime()));
    mSeekBar.setMax((int) mSong.getDuration());
    mSeekBar.setProgress((int) mSong.getCurrentTime());
    mDownloadIv.setVisibility(mSong.isOnline() ? View.VISIBLE : View.GONE);
    mDownloadIv.setImageDrawable(mSong.isDownload() ? getDrawable(R.drawable.download)
        : getDrawable(R.drawable.download_song));

    mPlayMode = mPresenter.getPlayMode(); //得到播放模式
    if (mPlayMode == Constant.PLAY_ORDER) {
      mPlayModeBtn.setBackground(getDrawable(R.drawable.play_mode_order));
    } else if (mPlayMode == Constant.PLAY_RANDOM) {
      mPlayModeBtn.setBackground(getDrawable(R.drawable.play_mode_random));
    } else {
      mPlayModeBtn.setBackground(getDrawable(R.drawable.play_mode_single));
    }
  }

  @Override
  protected void onClick() {
    Log.d(TAG, "onClick: 开始 ");
    // 返回按钮
    mBackIv.setOnClickListener(v -> finish());
    // 获取本地音乐的图片和歌词
    mGetImgAndLrcBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.d(TAG, "onClick:  ");
        getSingerAndLrc();
      }
    });
    Log.d(TAG, "onClick: mGetImgAndLrcBtn.id =  " + mGetImgAndLrcBtn.getId() );
    // 进度条的监听事件
    mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        mIsChange = true;
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        if (mPlayStatusBinder.isPlaying()) {
          mMediaPlayer = mPlayStatusBinder.getMediaPlayer();
          mMediaPlayer.seekTo(seekBar.getProgress() * 1000);
          startUpdateSeekBarProgress();
        } else {
          mTime = seekBar.getProgress();
          mIsSeek = true;
        }
        mIsChange = false;
        mCurrentTimeTv.setText(MediaUtil.formatTime(seekBar.getProgress()));
      }
    });

    mPlayModeBtn.setOnClickListener(v -> changePlayMode());

    // 播放 暂停的实现
    mPlayBtn.setOnClickListener(v -> {
      Log.d(TAG, "onClick: mPlayBtn");
      mMediaPlayer = mPlayStatusBinder.getMediaPlayer();
      if (mPlayStatusBinder.isPlaying()) {
        mPlayStatusBinder.pause();
        stopUpdateSeekBarProgress();
        mFlag = true;
        mPlayBtn.setSelected(false);
        mDisc.pause();
      } else if (mFlag) {
        mPlayStatusBinder.resume();
        mFlag = false;
        if (mIsSeek) {
          Log.d(TAG, "onClick: " + mTime);
          mMediaPlayer.seekTo(mTime * 1000);
          mIsSeek = false;
        }
        mDisc.play();
        mPlayBtn.setSelected(true);
        startUpdateSeekBarProgress();
      } else {
        if (mIsOnline) {
          mPlayStatusBinder.playOnline();
        } else {
          mPlayStatusBinder.play(mListType);
        }
        mMediaPlayer.seekTo((int) mSong.getCurrentTime() * 1000);
        mDisc.play();
        mPlayBtn.setSelected(true);
        startUpdateSeekBarProgress();
      }
    });

    mNextBtn.setOnClickListener(v -> {
      mPlayStatusBinder.next();
      if (mPlayStatusBinder.isPlaying()) {
       mPlayBtn.setSelected(true);
      } else {
        mPlayBtn.setSelected(false);
      }
      mDisc.next();
    });
    mLastBtn.setOnClickListener(v -> {
      mPlayStatusBinder.next();
      mPlayBtn.setSelected(true);
      mDisc.last();
    });
    mLoveBtn.setOnClickListener(v -> {
      showLoveAnim();
      if (mIsLove) {
        mLoveBtn.setSelected(false);
        mPresenter.deleteFromLove(FileUtil.getSong().getSongId());
      } else {
        mLoveBtn.setSelected(true);
        mPresenter.saveToLove(FileUtil.getSong());
      }
      mIsLove = !mIsLove;
    });
    // 唱碟点击效果
    mDisc.setOnClickListener(v -> {
      if (!mIsOnline) {
        String lrc = FileUtil.getLrcFromNative(mSong.getSongName());
        if (lrc == null) {
          String qqId = mSong.getQqId();
          if (Constant.SONG_ID_UNFIND.equals(qqId)) {
            getLrcError(null);
          } else if (qqId == null) {
            mPresenter.getSongId(mSong.getSongName(), mSong.getDuration());
          } else {
            mPresenter.getLrc(qqId, Constant.SONG_LOCAL);
          }
        } else {
          showLrc(lrc);
        }
      } else {
        mPresenter.getLrc(mSong.getSongId(), Constant.SONG_ONLINE);
      }
    });

    // 歌词点击效果
    mLrcView.setOnClickListener(v -> {
      mLrcView.setVisibility(View.GONE);
      mDisc.setVisibility(View.VISIBLE);
    });
    // 歌曲下载
    mDownloadIv.setOnClickListener(v -> {
      if (mSong.isDownload()) {
        showToast("歌曲已经下载");
      } else {
        mDownloadBinder.startDownload(getDownloadInfoFromSong(mSong));
      }
    });
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initClick();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    unbindService(mPlayConnection);
    unbindService(mDownloadConnection);
    EventBus.getDefault().unregister(this);
    stopUpdateSeekBarProgress();
    // 避免内存泄漏
    mMusicHandler.removeCallbacksAndMessages(null);
  }

  @Override
  protected PlayPresenter getPresenter() {
    mPresenter = new PlayPresenter();
    return mPresenter;
  }

  @Override
  public String getSingerName() {
    Song song = FileUtil.getSong();
    if (song.getSinger().contains("/")) {
      String[] s = song.getSinger().split("/");
      return s[0];
    } else {
      return song.getSinger().trim();
    }
  }

  @Override
  public void getSingerAndLrc() {
    Log.d(TAG, "getSingerAndLrc: " + "singerName : " + getSingerName() + ", songName : " + getSongName() + ", duration : " + mSong.getDuration() );
    mGetImgAndLrcBtn.setText("正在获取……");
    mPresenter.getSingerImg(getSingerName(), getSongName(), mSong.getDuration());
  }

  @Override
  public void setSingerImg(String ImgUrl) {
    Glide.with(this)
        .load(ImgUrl)
        .apply(RequestOptions.placeholderOf(R.drawable.welcome))
        .apply(RequestOptions.errorOf(R.drawable.welcome))
        .into(new SimpleTarget<Drawable>() {

          @Override
          public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
            mImgBmp = ((BitmapDrawable) resource).getBitmap();
            // 如果是本地音乐
            if (!mIsOnline) {
              // 保存图片到本地
              FileUtil.saveImgToNative(PlayActivity.this, mImgBmp, getSingerName());
              // 将封面地址放到数据库中
              LocalSong localSong = new LocalSong();
              localSong.setPic(Api.STORAGE_IMG_FILE + FileUtil.getSong().getSinger() + ".jpg");
              localSong.updateAll("songId=?", FileUtil.getSong().getSongId());
            }
            try2UpdateMusicPicBackground(mImgBmp);
            setDiscImg(mImgBmp);
            mGetImgAndLrcBtn.setVisibility(View.GONE);
          }
        });
  }

  @Override
  public void showLove(boolean love) {
    mIsLove = love;
    runOnUiThread(() -> {
      mLoveBtn.setSelected(mIsLove);
    });
  }

  @Override
  public void showLoveAnim() {
    AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater
        .loadAnimator(PlayActivity.this, R.animator.favorites_anim);
    animatorSet.setTarget(mLoveBtn);
    animatorSet.start();
  }

  @Override
  public void saveToLoveSuccess() {
    EventBus.getDefault().post(new SongCollectionEvent(true));
    CommonUtil.showToast(PlayActivity.this, "收藏成功");
  }

  @Override
  public void sendUpdateCollection() {
    EventBus.getDefault().post(new SongCollectionEvent(false));
  }

  @Override
  public void showLrc(String lrc) {
    mDisc.setVisibility(View.GONE);
    mLrcView.setVisibility(View.VISIBLE);
    Log.d(TAG, "showLrc: " + mPlayStatusBinder.getMediaPlayer().getCurrentPosition());
    mLrcView.setLrc(lrc).setPlayer(mPlayStatusBinder.getMediaPlayer()).draw();
  }

  @Override
  public void getLrcError(String content) {
    showToast("抱歉，获取不到该歌曲的信息");
    mSong.setQqId(content);
    FileUtil.saveSong(mSong);
  }

  @Override
  public void setLocalSongId(String songId) {
    mSong.setQqId(songId);
    FileUtil.saveSong(mSong);
  }

  @Override
  public void getSongIdSuccess(String songId) {
    setLocalSongId(songId);
    mPresenter.getLrc(songId, Constant.SONG_LOCAL);
  }

  @Override
  public void saveLrc(String lrc) {
    FileUtil.saveLrcToNative(lrc,mSong.getSongName());
  }

  private void startUpdateSeekBarProgress() {
    // 避免重复发送Message
    stopUpdateSeekBarProgress();
    mMusicHandler.sendEmptyMessageDelayed(0, 1000);
  }

  private void stopUpdateSeekBarProgress() {
    mMusicHandler.removeMessages(0);
  }

  private void setLocalImg(String singer) {
    String imgUrl = Api.STORAGE_IMG_FILE + MediaUtil.formatSinger(singer) + ".jpg";
    Glide.with(this)
        .load(imgUrl)
        .listener(new RequestListener<Drawable>() {
          @Override
          public boolean onLoadFailed(@Nullable GlideException e, Object model
              , Target<Drawable> target, boolean isFirstResource) {
            mGetImgAndLrcBtn.setVisibility(View.VISIBLE);
            mGetImgAndLrcBtn.setText("获取封面歌词");
            setDiscImg(BitmapFactory.decodeResource(getResources(), R.drawable.default_disc));
            mRootLayout.setBackgroundResource(R.drawable.background);
            return true;
          }

          @Override
          public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target
              , DataSource dataSource, boolean isFirstResource) {
            return false;
          }
        })
        .apply(RequestOptions.placeholderOf(R.drawable.background))
        .apply(RequestOptions.errorOf(R.drawable.background))
        .into(new SimpleTarget<Drawable>() {
          @Override
          public void onResourceReady(@NonNull Drawable resource,
                                      @Nullable Transition<? super Drawable> transition) {
            mGetImgAndLrcBtn.setVisibility(View.GONE);
            mImgBmp = ((BitmapDrawable) resource).getBitmap();
            try2UpdateMusicPicBackground(mImgBmp);
            setDiscImg(mImgBmp);
          }
        });
  }

  // 设置唱碟中的歌手头像
  private void setDiscImg(Bitmap bitmap) {
    mDiscImg.setImageDrawable(mDisc.getDiscDrawable(bitmap));
    int marginTop
        = (int) (DisplayUtil.SCALE_DISC_MARGIN_TOP * CommonUtil.getScreenHeight(this));
    RelativeLayout.LayoutParams layoutParams
        = (RelativeLayout.LayoutParams) mDiscImg.getLayoutParams();
    layoutParams.setMargins(0, marginTop, 0, 0);
    mDiscImg.setLayoutParams(layoutParams);
  }

  private void try2UpdateMusicPicBackground(Bitmap bitmap) {
    new Thread(() -> {
      Drawable drawable = getForegroundDrawable(bitmap);
      runOnUiThread(() -> {
        mRootLayout.setForeground(drawable);
        mRootLayout.beginAnimation();
      });
    }).start();
  }

  private Drawable getForegroundDrawable(Bitmap bitmap) {
    // 得到屏幕的宽高比，以便按比例切割图片的一部分
    float widthHeightRatio = (float) (DisplayUtil.getScreenWidth(PlayActivity.this) * 1.0
        / DisplayUtil.getScreenHeight(PlayActivity.this) * 1.0);
    int cropBitmapWidth = (int) (widthHeightRatio * bitmap.getHeight());
    int cropBitmapWidthX = (int) ((bitmap.getWidth() - cropBitmapWidth) / 2.0);
    // 切割部分图片
    Bitmap cropBitmap = Bitmap.createBitmap(bitmap, cropBitmapWidthX, 0, cropBitmapWidth
        , bitmap.getHeight());
    // 缩小图片
    Bitmap scaleBitmap = Bitmap.createScaledBitmap(cropBitmap, bitmap.getWidth() / 50,
        bitmap.getHeight() / 50, false);
    // 模糊化
    Bitmap blurBitmap = FastBlurUtil.doBlur(scaleBitmap, 8, true);
    Drawable foregroundDrawable = new BitmapDrawable(blurBitmap);
    // 加入灰色遮罩层， 避免图片过量影响其他控件
    foregroundDrawable.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    return foregroundDrawable;
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onDownloadSuccessEvent(DownloadEvent event) {
    if (event.getDownloadStatus() == Constant.TYPE_DOWNLOAD_SUCCESS) {
      mDownloadIv.setImageDrawable(
          LitePal.where("songId=?", mSong.getSongId())
              .find(DownloadSong.class).size() != 0
              ? getDrawable(R.drawable.downloaded) : getDrawable(R.drawable.download_song));
    }
  }

  @Subscribe (threadMode = ThreadMode.MAIN)
  public void onSongChangeEvent(SongStatusEvent event) {
    if (event.getSongStatus() == Constant.SONG_CHANGE) {
      mDisc.setVisibility(View.VISIBLE);
      mLrcView.setVisibility(View.VISIBLE);
      mSong = FileUtil.getSong();
      mSongTv.setText(mSong.getSongName());
      mSingerTv.setText(mSong.getSinger());
      mDurationTimeTv.setText(MediaUtil.formatTime(mSong.getDuration()));
      mPlayBtn.setSelected(true);
      mSeekBar.setMax((int) mSong.getDuration());
      startUpdateSeekBarProgress();
      // 缓存进度条
      mPlayStatusBinder.getMediaPlayer().setOnBufferingUpdateListener((mp, percent) -> {
        mSeekBar.setSecondaryProgress(percent * mSeekBar.getProgress());
      });
    }

    mPresenter.queryLove(mSong.getSongId()); // 查找歌曲是否为我喜欢的歌曲
    if (mSong.isOnline()) {
      setSingerImg(mSong.getImgUrl());
    } else {
      setLocalImg(mSong.getSinger());
    }
  }
  // 改变播放模式
  private void changePlayMode() {
    View playModeView = LayoutInflater.from(this).inflate(R.layout.play_mode, null);
    ConstraintLayout orderLayout = playModeView.findViewById(R.id.orderLayout);
    ConstraintLayout randomLayout = playModeView.findViewById(R.id.randomLayout);
    ConstraintLayout singleLayout = playModeView.findViewById(R.id.singleLayout);
    TextView orderTv = playModeView.findViewById(R.id.orderTv);
    TextView randomTv = playModeView.findViewById(R.id.randomTv);
    TextView singleTv = playModeView.findViewById(R.id.singleTv);

    // 显示弹窗
    PopupWindow popupWindow = new PopupWindow(playModeView
        , ScreenUtil.dip2px(this, 130)
        , ScreenUtil.dip2px(this, 150));

    // 设置背景色
    popupWindow.setBackgroundDrawable(getDrawable(R.color.transparent));
    // 设置焦点
    popupWindow.setFocusable(true);
    // 设置可以触摸框以外的地方
    popupWindow.setOutsideTouchable(true);
    popupWindow.update();
    // 设置弹出的位置
    popupWindow.showAsDropDown(mPlayModeBtn, 0, -50);

    // 显示播放模式
    int mode = mPresenter.getPlayMode();
    if (mode == Constant.PLAY_ORDER) {
      orderTv.setSelected(true);
      randomTv.setSelected(false);
      singleTv.setSelected(false);
    } else if (mode == Constant.PLAY_RANDOM) {
      randomTv.setSelected(true);
      orderTv.setSelected(false);
      singleTv.setSelected(false);
    } else {
      singleTv.setSelected(true);
      randomTv.setSelected(false);
      orderTv.setSelected(false);
    }

    //顺序播放
    orderLayout.setOnClickListener(view -> {
      mPlayStatusBinder.setPlayMode(Constant.PLAY_ORDER); //通知服务
      mPresenter.setPlayMode(Constant.PLAY_ORDER);
      popupWindow.dismiss();
      mPlayModeBtn.setBackground(getDrawable(R.drawable.play_mode_order));

    });
    //随机播放
    randomLayout.setOnClickListener(view -> {
      mPlayStatusBinder.setPlayMode(Constant.PLAY_RANDOM);
      mPresenter.setPlayMode(Constant.PLAY_RANDOM);
      popupWindow.dismiss();
      mPlayModeBtn.setBackground(getDrawable(R.drawable.play_mode_random));
    });
    //单曲循环
    singleLayout.setOnClickListener(view -> {
      mPlayStatusBinder.setPlayMode(Constant.PLAY_SINGLE);
      mPresenter.setPlayMode(Constant.PLAY_SINGLE);
      popupWindow.dismiss();
      mPlayModeBtn.setBackground(getDrawable(R.drawable.play_mode_single));
    });
  }

  private String getSongName() {
    Song song = FileUtil.getSong();
    return song != null ? song.getSongName().trim() : "";
  }

  private DownloadInfo getDownloadInfoFromSong(Song song) {
    DownloadInfo downloadInfo = new DownloadInfo();
    downloadInfo.setSinger(song.getSinger());
    downloadInfo.setProgress(0);
    downloadInfo.setSongId(song.getSongId());
    downloadInfo.setUrl(song.getUrl());
    downloadInfo.setSongName(song.getSongName());
    downloadInfo.setDuration(song.getDuration());
    return downloadInfo;
  }
}