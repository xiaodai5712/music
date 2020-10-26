package com.dzh.dzhmusicandcamera;

import android.animation.ObjectAnimator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.andexert.library.RippleView;
import com.dzh.dzhmusicandcamera.base.activity.BaseActivity;
import com.dzh.dzhmusicandcamera.base.entity.Song;
import com.dzh.dzhmusicandcamera.service.PlayerService;

import butterknife.BindView;
import butterknife.ButterKnife;
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
  private PlayerService.PlayStatusBinder mPlayerServiceBinder;
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
  protected int getLayoutId() {
    Log.d(TAG, "getLayoutId: " + R.layout.activity_main);
    return R.layout.activity_main;
  }

  @Override
  protected void initView() {

  }

  @Override
  protected void initData() {

  }

  @Override
  protected void onClick() {

  }

}