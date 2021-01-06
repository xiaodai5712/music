package com.dzh.dzhmusicandcamera.base.view.search;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.adapter.AlbumSongAdapter;
import com.dzh.dzhmusicandcamera.app.Constant;
import com.dzh.dzhmusicandcamera.base.entity.AlbumSong;
import com.dzh.dzhmusicandcamera.base.entity.DownloadSong;
import com.dzh.dzhmusicandcamera.base.entity.Song;
import com.dzh.dzhmusicandcamera.base.fragment.BaseMvpFragment;
import com.dzh.dzhmusicandcamera.base.presenter.AlbumSongPresenter;
import com.dzh.dzhmusicandcamera.contract.IAlbumSongContract;
import com.dzh.dzhmusicandcamera.event.SongAlbumEvent;
import com.dzh.dzhmusicandcamera.service.PlayerService;

import com.dzh.dzhmusicandcamera.util.CommonUtil;
import com.dzh.dzhmusicandcamera.util.FileUtil;
import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.header.MaterialViewPagerHeaderDecorator;
import com.wang.avi.AVLoadingIndicatorView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.util.List;
import java.util.Objects;

/**
 * Date: 2020/12/23
 * author: Dzh
 */
public class AlbumSongFragment extends BaseMvpFragment<AlbumSongPresenter>
    implements IAlbumSongContract.View {

  public static final int ALBUM_SONG = 0;
  public static final int ALBUM_INFORMATION = 1;
  private static final String TYPE_KEY = "type_key";

  private AlbumSongPresenter mPresenter;
  private String mId;

  private NestedScrollView mScrollView;
  private TextView mNameTv, mLanguageTv, mDescTv, mCompany, mPublicTimeTv, mTypeTv;
  private int mType;
  private String mPublicTime;
  private String mDesc;

  // 用来判断网络问题及加载问题
  private AVLoadingIndicatorView mLoading;
  private TextView mLoadingTv;
  private ImageView mNetworkErrorIv;

  private List<AlbumSong.DataBean.ListBean> mSongsList;
  private RecyclerView mRecycler;
  private LinearLayoutManager mLinearManager;
  private AlbumSongAdapter mAdapter;

  private Intent mPlayIntent;
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

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container
      , @Nullable Bundle savedInstanceState) {
    getBundle();
    EventBus.getDefault().register(this);
    View view = null;
    if (mType == ALBUM_SONG) {
      view = inflater.inflate(R.layout.fragment_album_recycler, container, false);
      mRecycler = view.findViewById(R.id.normalView);
      LitePal.getDatabase();
    } else {
      view = inflater.inflate(R.layout.fragment_album_song, container, false);
      mScrollView = view.findViewById(R.id.scroll_view);
      mDescTv = view.findViewById(R.id.tv_desc);
      mNameTv = view.findViewById(R.id.tv_album_name);
      mLanguageTv = view.findViewById(R.id.tv_language);
      mCompany = view.findViewById(R.id.tv_company);
      mPublicTimeTv = view.findViewById(R.id.tv_public_time);
      mTypeTv = view.findViewById(R.id.tv_album_type);
    }
    mLoading = view.findViewById(R.id.avi);
    mLoadingTv = view.findViewById(R.id.tv_loading);
    mNetworkErrorIv = view.findViewById(R.id.iv_network_error);
    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    if (mType == ALBUM_SONG) {
      // 启动服务
      mPlayIntent = new Intent(getActivity(), PlayerService.class);
      mActivity.bindService(mPlayIntent, mConnection, Context.BIND_AUTO_CREATE);
    } else {
      MaterialViewPagerHelper.registerScrollView(getActivity(), mScrollView);
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    EventBus.getDefault().unregister(this);
    if (mPlayIntent != null) {
      Objects.requireNonNull(getActivity()).unbindService(mConnection);
    }
  }

  @Override
  protected AlbumSongPresenter getPresenter() {
    return null;
  }

  @Override
  protected void loadData() {
    mPresenter = new AlbumSongPresenter();
    mPresenter.attachView(this);
    mPresenter.getAlbumDetail(mId, mType);
  }

  @Override
  protected int getLayoutId() {
    return 0;
  }

  @Override
  public void setAlbumSongList(List<AlbumSong.DataBean.ListBean> songList) {
    mLinearManager = new LinearLayoutManager(getActivity());
    mRecycler.setLayoutManager(mLinearManager);
    mAdapter = new AlbumSongAdapter(songList);
    mRecycler.addItemDecoration(new MaterialViewPagerHeaderDecorator());
    mRecycler.setAdapter(mAdapter);

    mAdapter.setSongClick(position -> {
      AlbumSong.DataBean.ListBean dataBean = songList.get(position);
      Song song = new Song();
      song.setSongId(dataBean.getSongmid());
      song.setSinger(getSinger(dataBean));
      song.setSongName(dataBean.getSongname());
      song.setPosition(position);
      song.setDuration(dataBean.getInterval());
      song.setOnline(true);
      song.setListType(Constant.LIST_TYPE_ONLINE);
      song.setUrl(null);
      song.setMediaId(dataBean.getStrMediaMid());
      // 判断是否已经下载
      song.setDownload(LitePal.where("songId=?", dataBean.getSongmid())
          .find(DownloadSong.class).size() != 0);
      FileUtil.saveSong(song);
      mPlayStatusBinder.play(Constant.LIST_TYPE_ONLINE);
    });
  }

  @Override
  public void showAlbumSongError() {
    showToast("获取专专辑信息失败");
  }

  @Override
  public void showAlbumMessage(String name, String language, String company, String albumType
      , String desc) {
    mNameTv.setText(name);
    mLanguageTv.setText(language);
    mCompany.setText(company);
    mDescTv.setText(desc);
    mPublicTimeTv.setText(mPublicTime);
    mTypeTv.setText(albumType);
  }

  @Override
  public void hideLoading() {
    mLoading.hide();
    mLoadingTv.setVisibility(View.GONE);
    if (mType == ALBUM_SONG) {
      mRecycler.setVisibility(View.VISIBLE);
    } else {
      mScrollView.setVisibility(View.VISIBLE);
    }
    mNetworkErrorIv.setVisibility(View.GONE);
  }

  @Override
  public void showNetError() {
    mLoadingTv.setVisibility(View.GONE);
    mLoading.hide();
  }

  @Override
  public void showLoading() {
    mLoading.show();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onSongAlbumEvent(SongAlbumEvent event){
    mAdapter.notifyDataSetChanged();
  }

  public static Fragment newInstance(int type, String id, String publicTime) {
    AlbumContentFragment fragment = new AlbumContentFragment();
    Bundle bundle = new Bundle();
    bundle.putInt(TYPE_KEY, type);
    bundle.putString(Constant.ALBUM_ID_KEY, id);
    bundle.putString(Constant.PUBLIC_TIME_KEY, publicTime);
    fragment.setArguments(bundle);
    return fragment;
  }

  //获取歌手，因为歌手可能有很多个
  private String getSinger(AlbumSong.DataBean.ListBean dataBean) {
    StringBuilder sb = new StringBuilder(dataBean.getSinger().get(0).getName());
    for (int i = 1; i < dataBean.getSinger().size(); i++) {
      sb.append("、").append(dataBean.getSinger().get(i).getName());
    }
    return sb.toString();
  }

  private void getBundle() {
    Bundle bundle = getArguments();
    if (bundle != null) {
      mType =  bundle.getInt(TYPE_KEY);
      mId = bundle.getString(Constant.ALBUM_ID_KEY);
      mPublicTime = bundle.getString(Constant.PUBLIC_TIME_KEY);
    }
  }
}