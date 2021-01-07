package com.dzh.dzhmusicandcamera.base.view.search;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.adapter.SearchContentAdapter;
import com.dzh.dzhmusicandcamera.app.Api;
import com.dzh.dzhmusicandcamera.app.Constant;
import com.dzh.dzhmusicandcamera.base.entity.Album;
import com.dzh.dzhmusicandcamera.base.entity.SearchSong;
import com.dzh.dzhmusicandcamera.base.entity.Song;
import com.dzh.dzhmusicandcamera.base.fragment.BaseLoadingFragment;
import com.dzh.dzhmusicandcamera.base.presenter.SearchContentPresenter;
import com.dzh.dzhmusicandcamera.contract.ISearchContentContract;
import com.dzh.dzhmusicandcamera.event.OnlineSongChangeEvent;
import com.dzh.dzhmusicandcamera.event.OnlineSongErrorEvent;
import com.dzh.dzhmusicandcamera.service.PlayerService;
import com.dzh.dzhmusicandcamera.util.CommonUtil;
import com.dzh.dzhmusicandcamera.util.DownloadUtil;
import com.dzh.dzhmusicandcamera.util.FileUtil;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 2020/12/30
 * author: Dzh
 */
public class SearchContentFragment extends BaseLoadingFragment<SearchContentPresenter>
    implements ISearchContentContract.View {

  private static final String TAG = "DzhSearchContentFragment";

  public static final String TYPE_KEY = "type";
  public static final String SEEK_KEY = "seek";
  public static final String IS_ONLINE = "online";

  private int mOffset = 1; // 用于翻页搜索

  private SearchContentPresenter mPresenter;

  private LinearLayoutManager mManager;
  private SearchContentAdapter mAdapter;
  private ArrayList<SearchSong.DataBean.SongBean.ListBean> mSongList = new ArrayList<>();
  private List<Album.DataBean.AlbumBean.ListBean> mAlbumList;
  private LRecyclerViewAdapter mLRecyclerViewAdapter; // 下拉刷新

  private LRecyclerView mRecycler;
  private ImageView mBackgroundIv;

  private Bundle mBundle;
  private String mSeek;
  private String mType;
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
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mRecycler = view.findViewById(R.id.normalView);
    mBackgroundIv = view.findViewById(R.id.iv_background);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mActivity.unbindService(mConnection);
    EventBus.getDefault().unregister(this);
  }

  @Override
  protected SearchContentPresenter getPresenter() {
    mPresenter = new SearchContentPresenter();
    return mPresenter;
  }

  @Override
  protected void loadData() {
    if (mType.equals("song")) {
      mPresenter.search(mSeek, 1);
    } else if (mType.equals("album")) {
      mPresenter.searchAlbum(mSeek, 1);
    }
    searchMore();
  }

  @Override
  public void reload() {
    super.reload();
    if (mType.equals("song")) {
      mPresenter.search(mSeek, 1);
    } else if (mType.equals("album")) {
      mPresenter.searchAlbum(mSeek, 1);
    }
  }

  @Override
  protected int getLayoutId() {
    return R.layout.fragment_search_content;
  }

  @Override
  protected void initView() {
    super.initView();
    EventBus.getDefault().register(this);
    mBundle = getArguments();
    if (mBundle != null) {
      mSeek = mBundle.getString(SEEK_KEY);
      mType = mBundle.getString(TYPE_KEY);
    }
    mManager = new LinearLayoutManager(mActivity);
    // 启动服务
    Intent playIntent = new Intent(getActivity(), PlayerService.class);
    mActivity.bindService(playIntent, mConnection, Context.BIND_AUTO_CREATE);
  }


  @Override
  public void setSongList(ArrayList<SearchSong.DataBean.SongBean.ListBean> songListBeans) {
    mSongList.addAll(songListBeans);
    mAdapter = new SearchContentAdapter(mSongList, mSeek, getContext(), Constant.TYPE_SONG);
    mLRecyclerViewAdapter = new LRecyclerViewAdapter(mAdapter);
    mRecycler.setLayoutManager(mManager);
    mRecycler.setAdapter(mLRecyclerViewAdapter);

    // 点击播放
    SearchContentAdapter.setItemClick(position -> {
      SearchSong.DataBean.SongBean.ListBean dataBean = mSongList.get(position);
      Song song = new Song();
      song.setSongId(dataBean.getSongmid());
      song.setSinger(getSinger(dataBean));
      song.setSongName(dataBean.getSongname());
      song.setUrl(Api.ALBUM_PIC + dataBean.getAlbummid() + Api.JPG);
      song.setDuration(dataBean.getInterval());
      song.setOnline(true);
      song.setMediaId(dataBean.getStrMediaMid());
      song.setDownload(DownloadUtil.isExistOfDownloadSong(dataBean.getSongmid()));
      // 网络获取歌曲地址
      mPresenter.getSongUrl(song);
    });
  }

  @Override
  public void searchMoreSuccess(ArrayList<SearchSong.DataBean.SongBean.ListBean> songListBeans) {
    mSongList.addAll(songListBeans);
    mAdapter.notifyDataSetChanged();
    mRecycler.refreshComplete(Constant.OFFSET);
  }

  @Override
  public void searchMoreError() {
    mRecycler.setNoMore(true);
  }

  @Override
  public void searchMore() {
    mRecycler.setPullRefreshEnabled(false);
    mRecycler.setOnLoadMoreListener(() -> {
      mOffset += 1;
      Log.d(TAG, "onLoadMOre: mOffset = " + mOffset);
      if (mType.equals("song")) {
        mPresenter.searchMore(mSeek, mOffset);
      } else {
        mPresenter.searchAlbumMore(mSeek, mOffset);
      }
    });
    // 设置底部加载颜色
    mRecycler.setFooterViewColor(R.color.colorAccent, R.color.musicStyle, R.color.transparent);
    // 设置底部文字加载提示
    mRecycler.setFooterViewHint("拼命加载中", "已经全部为你呈现了"
        , "网络不给力，再点击一次");
  }

  @Override
  public void showSearchMoreNetworkError() {
    mRecycler.setOnNetWorkErrorListener(() -> {
      mOffset += 1;
      mPresenter.searchMore(mSeek, mOffset);
    });
  }

  @Override
  public void searchAlbumSuccess(List<Album.DataBean.AlbumBean.ListBean> albumList) {
    mAlbumList = new ArrayList<>();
    mAlbumList.addAll(albumList);
    mAdapter = new SearchContentAdapter(mAlbumList, mSeek, getActivity(), Constant.TYPE_ALBUM);
    mLRecyclerViewAdapter = new LRecyclerViewAdapter(mAdapter);
    mRecycler.setLayoutManager(mManager);
    mRecycler.setAdapter(mLRecyclerViewAdapter);
    SearchContentAdapter.setAlbumClick(position -> toAlbumContentFragment(mAlbumList.get(position)));
  }

  @Override
  public void searchAlbumMoreSuccess(List<Album.DataBean.AlbumBean.ListBean> songLIstBeans) {
    mAlbumList.addAll(songLIstBeans);
    mAdapter.notifyDataSetChanged();
    mRecycler.refreshComplete(Constant.OFFSET);
  }

  @Override
  public void searchAlbumError() {
    CommonUtil.showToast(getActivity(), "获取专辑信息失败");
  }

  @Override
  public void getSongUrlSuccess(Song song, String urlStr) {
    song.setUrl(urlStr);
    FileUtil.saveSong(song);
    mPlayStatusBinder.playOnline();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onOnlineSongChangeEvent(OnlineSongChangeEvent event) {
    if (mAdapter != null) {
      mAdapter.notifyDataSetChanged();
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onOnlineSongErrorEvent(OnlineSongErrorEvent event) {
    showToast("抱歉，该歌曲没有版权");
  }

  public static Fragment newInstance(String seek, String type) {
    SearchContentFragment fragment = new SearchContentFragment();
    Bundle bundle = new Bundle();
    bundle.putString(TYPE_KEY, type);
    bundle.putString(SEEK_KEY, seek);
    fragment.setArguments(bundle);
    return fragment;
  }
  // 获取歌手，因为歌手有可能有很多个
  private String getSinger(SearchSong.DataBean.SongBean.ListBean dataBean) {
    StringBuilder singer = new StringBuilder(dataBean.getSinger().get(0).getName());
    for (int i = 1; i < dataBean.getSinger().size(); i++) {
      singer.append("、").append(dataBean.getSinger().get(i).getName());
    }
    return singer.toString();
  }

  public void toAlbumContentFragment(Album.DataBean.AlbumBean.ListBean album) {
    FragmentActivity activity = getActivity();
    if (activity != null) {
      FragmentManager manager = activity.getSupportFragmentManager();
      FragmentTransaction transaction = manager.beginTransaction();
      transaction.setCustomAnimations(R.anim.fragment_in, R.anim.fragment_out, R.anim.slide_in_right
          , R.anim.slide_out_right);
      transaction.add(R.id.fragment_container, AlbumContentFragment.newInstance(album.getAlbumMID()
          , album.getAlbumName(), album.getAlbumPic(), album.getSingerName(), album.getPublicTime()));
      transaction.hide(this);
      // 将事务提交给返回栈
      transaction.addToBackStack(null);
      transaction.commit();
    }
  }
}