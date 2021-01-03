package com.dzh.dzhmusicandcamera.base.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.adapter.ExpandableListViewAdapter;
import com.dzh.dzhmusicandcamera.app.Api;
import com.dzh.dzhmusicandcamera.app.Constant;
import com.dzh.dzhmusicandcamera.base.entity.AlbumCollection;
import com.dzh.dzhmusicandcamera.base.entity.HistorySong;
import com.dzh.dzhmusicandcamera.base.entity.LocalSong;
import com.dzh.dzhmusicandcamera.base.entity.Love;
import com.dzh.dzhmusicandcamera.base.view.main.local.LocalFragment;
import com.dzh.dzhmusicandcamera.base.view.search.AlbumContentFragment;
import com.dzh.dzhmusicandcamera.event.AlbumCollectionEvent;
import com.dzh.dzhmusicandcamera.event.SongListNumEvent;
import com.dzh.dzhmusicandcamera.util.DownloadUtil;
import com.dzh.dzhmusicandcamera.widget.MyListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 2020/10/31
 * author: Dzh
 */
public class MainFragment extends Fragment {
  private static final String TAG = "MainFragment";

  private MyListView mMyListView;
  private ExpandableListAdapter mAdapter;
  private LinearLayout mLocalMusicLinearLayout
      , mCollectionLinearLayout, mHistoryMusicLinearLayout, mDownloadLinearLayout;

  private TextView mLocalMusicNum, mLoveMusicNum, mHistoryMusicNum, mDownloadMusicNum;
  private TextView mSeekBtn;

  private List<List<AlbumCollection>> mAlbumCollectionList;
  private List<AlbumCollection> mLoveAlbumList;
  private boolean mTwoExpand;
  private String[] mGroupStrings ={"自建歌单", "收藏歌单"};

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container
      , @Nullable Bundle savedInstanceState) {
    EventBus.getDefault().register(this);
    View view = inflater.inflate(R.layout.fragment_main, container, false);
    mLocalMusicLinearLayout = view.findViewById(R.id.linear_local_music);
    mCollectionLinearLayout = view.findViewById(R.id.linear_collection);
    LinearLayout functionLinearLayout = view.findViewById(R.id.linear_function);
    mHistoryMusicLinearLayout = view.findViewById(R.id.linear_history);
    // 获取焦点
    functionLinearLayout.setFocusableInTouchMode(true);
    mMyListView = view.findViewById(R.id.expand_lv_song_list);
    mSeekBtn = view.findViewById(R.id.tv_seek);
    mLocalMusicNum = view.findViewById(R.id.tv_local_music_num);
    mLoveMusicNum = view.findViewById(R.id.tv_love_num);
    mHistoryMusicNum = view.findViewById(R.id.tv_history_num);
    mDownloadMusicNum = view.findViewById(R.id.tv_download_num);
    mDownloadLinearLayout = view.findViewById(R.id.downloadLinear);
    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    showAlbumList();
    onClick();
  }

  private void showAlbumList() {
    mLoveAlbumList = new ArrayList<>();
    mAlbumCollectionList = new ArrayList<>();
    AlbumCollection albumCollection = new AlbumCollection();
    albumCollection.setAlbumName("我喜欢");
    albumCollection.setSingerName("Neptune");
    mLoveAlbumList.add(albumCollection);
    mAlbumCollectionList.add(mLoveAlbumList);
    mAlbumCollectionList.add(orderCollection(LitePal.findAll(AlbumCollection.class)));
    mAdapter = new ExpandableListViewAdapter(getActivity(), mGroupStrings, mAlbumCollectionList);
    mMyListView.setAdapter(mAdapter);
  }

  @Override
  public void onStart() {
    super.onStart();
    showMusicNum();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(AlbumCollectionEvent event) {
    mAlbumCollectionList.clear();
    mAlbumCollectionList.add(mLoveAlbumList);
    mAlbumCollectionList.add(orderCollection(LitePal.findAll(AlbumCollection.class)));
    // 根据之前的状态， 进行展开和收缩, 从而达到更新列表的功能
    if (mTwoExpand) {
      mMyListView.collapseGroup(1);
      mMyListView.expandGroup(1);
    } else {
      mMyListView.expandGroup(1);
      mMyListView.collapseGroup(1);
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onSongListEvent(SongListNumEvent event) {
    int type = event.getType();
    if (type == Constant.LIST_TYPE_HISTORY) {
      mHistoryMusicNum.setText(String.valueOf(LitePal.findAll(HistorySong.class).size()));
    } else if(type == Constant.LIST_TYPE_LOCAL){
      mLocalMusicNum.setText(String.valueOf(LitePal.findAll(LocalSong.class).size()));
    }else if(type == Constant.LIST_TYPE_DOWNLOAD){
      mDownloadMusicNum.setText(String.valueOf(DownloadUtil.getSongFromFile(Api.STORAGE_SONG_FILE).size()));
    }
  }

  // 显示数目
  private void showMusicNum() {
    mLocalMusicNum.setText(String.valueOf(LitePal.findAll(LocalSong.class).size()));
    mLoveMusicNum.setText(String.valueOf(LitePal.findAll(Love.class).size()));
    mHistoryMusicNum.setText(String.valueOf(LitePal.findAll(HistorySong.class).size()));
    mDownloadMusicNum
        .setText(String.valueOf(DownloadUtil.getSongFromFile(Api.STORAGE_SONG_FILE).size()));
  }

  // 使数据库中的列表逆序排列
  private List<AlbumCollection> orderCollection(List<AlbumCollection> tempList) {
    List<AlbumCollection> albumCollectionList = new ArrayList<>();
    for (int i = tempList.size() - 1; i >=0 ; i--) {
      albumCollectionList.add(tempList.get(i));
    }
    return albumCollectionList;
  }

  private void onClick() {
    // 本地音乐
    mLocalMusicLinearLayout.setOnClickListener(v -> replaceFragment(new LocalFragment()));
    // 搜索
    mSeekBtn.setOnClickListener(v -> replaceFragment(new AlbumContentFragment.SearchFragment()));
  }

  private void replaceFragment(Fragment fragment) {
    FragmentActivity fragmentActivity = getActivity();
    if (fragmentActivity != null) {
      FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
      FragmentTransaction transcation = fragmentManager.beginTransaction();

      // 进入和退出动画
      transcation.setCustomAnimations(R.anim.fragment_in, R.anim.fragment_out, R.anim.slide_in_right
          , R.anim.slide_out_right);
      transcation.add(R.id.fragment_container, fragment);
      transcation.hide(this);
      // 将 transcaion 提交到返回栈
      transcation.addToBackStack(null);
      transcation.commit();
    }
  }
}