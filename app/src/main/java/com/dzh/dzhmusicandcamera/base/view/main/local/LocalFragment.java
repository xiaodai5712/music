package com.dzh.dzhmusicandcamera.base.view.main.local;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.adapter.SongAdapter;
import com.dzh.dzhmusicandcamera.app.Constant;
import com.dzh.dzhmusicandcamera.base.fragment.BaseMvpFragment;
import com.dzh.dzhmusicandcamera.base.entity.LocalSong;
import com.dzh.dzhmusicandcamera.base.entity.Song;
import com.dzh.dzhmusicandcamera.base.presenter.LocalPresenter;
import com.dzh.dzhmusicandcamera.contract.ILocalContract;
import com.dzh.dzhmusicandcamera.event.SongLocalEvent;
import com.dzh.dzhmusicandcamera.service.PlayerService;
import com.dzh.dzhmusicandcamera.util.FileUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Date: 2020/11/4
 * author: Dzh
 */
public class LocalFragment extends BaseMvpFragment<LocalPresenter> implements ILocalContract.View {
  private static final String TAG = "LocalFragment";
  @BindView(R.id.iv_back)
  ImageView mBackIv;
  @BindView(R.id.iv_find_local_song)
  ImageView mFindLocalMusicIv;
  @BindView(R.id.normal_view)
  RecyclerView mRecycler;
  @BindView(R.id.linear_empty)
  RelativeLayout mEmptyViewLinear;

  private List<LocalSong> mLocalSongsList;
  private LocalPresenter mPresenter;
  private SongAdapter mSongAdapter;
  private LinearLayoutManager mLayoutManager;

  // 在 onServiceConnected 中获取 PlayStatusBinder 的实例
  private PlayerService.PlayStatusBinder mPlayStatusBinder;

  private ServiceConnection mConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      mPlayStatusBinder = (PlayerService.PlayStatusBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
  };

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    onClick();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mActivity.unbindService(mConnection);
    EventBus.getDefault().unregister(this);
  }

  @Override
  protected LocalPresenter getPresenter() {
    mPresenter = new LocalPresenter();
    return mPresenter;
  }

  @Override
  protected void initView() {
    super.initView();
    EventBus.getDefault().register(this);
    registerAndBindService();
    initLocalRecycler();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(SongLocalEvent event) {
    mSongAdapter.notifyDataSetChanged();
    if (FileUtil.getSong() != null) {
      mLayoutManager.scrollToPositionWithOffset(FileUtil.getSong().getPosition() + 4
          , mRecycler.getHeight());
    }
  }

  // 注册服务
  private void registerAndBindService() {
    // 启动服务
    Intent playIntent = new Intent(getActivity(), PlayerService.class);
    mActivity.bindService(playIntent, mConnection, Context.BIND_AUTO_CREATE);
  }

  private void initLocalRecycler() {
    mLocalSongsList = new ArrayList<>();
    mLayoutManager = new LinearLayoutManager(getActivity());
    mLocalSongsList.clear();
    mLocalSongsList.addAll(LitePal.findAll(LocalSong.class));
    if (mLocalSongsList.size() == 0) {
      mRecycler.setVisibility(View.GONE);
      mEmptyViewLinear.setVisibility(View.VISIBLE);
    } else {
      mEmptyViewLinear.setVisibility(View.GONE);
      mRecycler.setVisibility(View.VISIBLE);
      mRecycler.setLayoutManager(mLayoutManager);
      // 令 recyclerView 定位到当前位置
      mSongAdapter = new SongAdapter(mActivity, mLocalSongsList);
      mRecycler.setAdapter(mSongAdapter);
      if (FileUtil.getSong() != null) {
        mLayoutManager.scrollToPositionWithOffset(FileUtil.getSong().getPosition() - 4
            , mRecycler.getHeight());
      }
      mSongAdapter.setOnItemClickListener(position -> {
        // 将点击的序列化到本地
        LocalSong mp3Info = mLocalSongsList.get(position);
        Song song = new Song();
        song.setSongName(mp3Info.getName());
        song.setSinger(mp3Info.getSinger());
        song.setUrl(mp3Info.getUrl());
        song.setDuration(mp3Info.getDuration());
        song.setPosition(position);
        song.setOnline(false);
        song.setSongId(mp3Info.getSongId());
        song.setListType(Constant.LIST_TYPE_LOCAL);
        FileUtil.saveSong(song);
        mPlayStatusBinder.play(Constant.LIST_TYPE_LOCAL);
      });
    }
  }

  @Override
  protected void loadData() {

  }

  @Override
  protected int getLayoutId() {
    return R.layout.fragment_local;
  }

  // 展示本地音乐列表
  @Override
  public void showMusicList(List<LocalSong> mp3InfoList) {
    mLocalSongsList.clear();
    mLocalSongsList.addAll(mp3InfoList);
    mRecycler.setVisibility(View.VISIBLE);
    mEmptyViewLinear.setVisibility(View.GONE);
    mRecycler.setLayoutManager(mLayoutManager);
    //  零 recyclerView 定位到当前的位置
    mSongAdapter = new SongAdapter(mActivity, mLocalSongsList);
    mRecycler.setAdapter(mSongAdapter);
    mSongAdapter.setOnItemClickListener(position -> {
      //将点击的序列化到本地
      LocalSong mp3Info = mLocalSongsList.get(position);
      Song song = new Song();
      song.setSongName(mp3Info.getName());
      song.setSinger(mp3Info.getSinger());
      song.setUrl(mp3Info.getUrl());
      song.setDuration(mp3Info.getDuration());
      song.setPosition(position);
      song.setOnline(false);
      song.setSongId(mp3Info.getSongId());
      song.setListType(Constant.LIST_TYPE_LOCAL);
      FileUtil.saveSong(song);
      mPlayStatusBinder.play(Constant.LIST_TYPE_LOCAL);
    });
  }

  @Override
  public void showErrorView() {
    showToast("本地音乐为空");
    mRecycler.setVisibility(View.GONE);
    mEmptyViewLinear.setVisibility(View.VISIBLE);
  }

  // 按钮事件
  private void onClick() {
    mFindLocalMusicIv.setOnClickListener(v -> {
      mPresenter.getLocalMp3Info(); // 得到本地列表
    });
    mBackIv.setOnClickListener(v -> {
      getActivity().getSupportFragmentManager().popBackStack(); // 返回
    });
  }
}