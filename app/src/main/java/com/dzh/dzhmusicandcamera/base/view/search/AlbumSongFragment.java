package com.dzh.dzhmusicandcamera.base.view.search;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dzh.dzhmusicandcamera.adapter.AlbumSongAdapter;
import com.dzh.dzhmusicandcamera.base.entity.AlbumSong;
import com.dzh.dzhmusicandcamera.base.fragment.BaseMvpFragment;
import com.dzh.dzhmusicandcamera.base.presenter.AlbumSongPresenter;
import com.dzh.dzhmusicandcamera.contract.IAlbumSongContract;
import com.wang.avi.AVLoadingIndicatorView;

import java.lang.invoke.CallSite;
import java.util.List;

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
  private RecyclerView mRecycle;
  private LinearLayoutManager mLinearManager;
  private AlbumSongAdapter mAdapter;

  @Override
  protected AlbumSongPresenter getPresenter() {
    return null;
  }

  @Override
  protected void loadData() {

  }

  @Override
  protected int getLayoutId() {
    return 0;
  }

  @Override
  public void setAlbumSongList(List<AlbumSong.DataBean.ListBean> dataBean) {

  }

  @Override
  public void showAlbumSongError() {

  }

  @Override
  public void showAlbumMessage(String name, String language, String company, String albumType, String desc) {

  }

  @Override
  public void hideLoading() {

  }

  @Override
  public void showNetError() {

  }
}