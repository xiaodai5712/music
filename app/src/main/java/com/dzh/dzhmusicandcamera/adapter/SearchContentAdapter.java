package com.dzh.dzhmusicandcamera.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andexert.library.RippleView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.app.Constant;
import com.dzh.dzhmusicandcamera.base.entity.Album;
import com.dzh.dzhmusicandcamera.base.entity.SearchSong;
import com.dzh.dzhmusicandcamera.callback.OnAlbumItemClickListener;
import com.dzh.dzhmusicandcamera.callback.OnItemClickListener;
import com.dzh.dzhmusicandcamera.util.CommonUtil;
import com.dzh.dzhmusicandcamera.util.FileUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 2021/1/5
 * author: Dzh
 */
public class SearchContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final String TAG = "DzhSearchContentAdapter";

  private ArrayList<SearchSong .DataBean.SongBean.ListBean> mSongListBeans;
  private List<Album.DataBean.AlbumBean.ListBean> mAlbumList;

  private static OnItemClickListener mItemClick;
  private static OnAlbumItemClickListener mAlbumClick;

  private String mSeek;
  private Context  mContext;

  private int mLastPosition = -1;
  private int mType;

  public static void setItemClick(OnItemClickListener itemClick) {
    mItemClick = itemClick;
  }

  public static void setAlbumClick(OnAlbumItemClickListener albumClick) {
    mAlbumClick = albumClick;
  }

  public SearchContentAdapter (List<Album.DataBean.AlbumBean.ListBean> dataBeans, String seek
      , Context context, int type) {
    mContext = context;
    mSeek = seek;
    mAlbumList = dataBeans;
    mType = type;
  }

  public SearchContentAdapter(ArrayList<SearchSong.DataBean.SongBean.ListBean> songListBeans
      , String seek, Context context, int type) {
    mContext = context;
    mSeek = seek;
    mSongListBeans = songListBeans;
    mType = type;
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    final View view;
    if (viewType == Constant.TYPE_SONG) {
      view = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.recycler_song_search_item, parent, false);
      return new ViewHolder(view);
    } else if (viewType == Constant.TYPE_ALBUM) {
      view = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.recycler_album_item, parent, false);
      return new AlbumViewHolder(view);
    }
    return null;
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    if (holder instanceof ViewHolder) {
      ViewHolder songHolder = (ViewHolder) holder;
      SearchSong.DataBean.SongBean.ListBean songListBean = mSongListBeans.get(position);
      // 设置歌手，因为歌手可能有两个
      StringBuilder singer = new StringBuilder(songListBean.getSinger().get(0).getName());
      for (int i = 1; i < songListBean.getSinger().size(); i++) {
        singer.append("、").append(songListBean.getSinger().get(i).getName());
      }
      songHolder.artistTv.setText(singer);
      // 设置于搜索一样的String颜色
      CommonUtil.showStringColor(mSeek, singer.toString(), songHolder.artistTv);
      songHolder.titleTv.setText(songListBean.getSongname());
      CommonUtil.showStringColor(mSeek, songListBean.getSongname(), songHolder.titleTv);
      // 根据点击显示
      if (songListBean.getSongmid().equals(FileUtil.getSong().getSongId())) {
        songHolder.playLine.setVisibility(View.VISIBLE);
        mLastPosition = position;
        songHolder.mItemView.setBackgroundResource(R.color.translucent);
      } else {
        songHolder.playLine.setVisibility(View.INVISIBLE);
        songHolder.mItemView.setBackgroundResource(R.color.transparent);
      }
      songHolder.mItemView.setOnRippleCompleteListener(rippleView -> {
        mItemClick.onClick(position);
        equalPosition(position);
      });
    } else {
      AlbumViewHolder albumHolder = (AlbumViewHolder) holder;
      Album.DataBean.AlbumBean.ListBean albumList = mAlbumList.get(position);
      Glide.with(mContext).load(albumList.getAlbumPic())
          .apply(RequestOptions.errorOf(R.drawable.background)).into(albumHolder.albumIv);
      albumHolder.albumName.setText(albumList.getAlbumName());
      albumHolder.singerName.setText(albumList.getSingerName());
      albumHolder.publicTime.setText(albumList.getPublicTime());
      CommonUtil.showStringColor(mSeek, albumList.getAlbumName(), albumHolder.albumName);
      CommonUtil.showStringColor(mSeek, albumList.getSingerName(), albumHolder.singerName);
      CommonUtil.showStringColor(mSeek, albumList.getPublicTime(), albumHolder.publicTime);
      albumHolder.item.setOnRippleCompleteListener(rippleView -> {
        mAlbumClick.onClick(position);
      });
    }
  }

  @Override
  public int getItemCount() {
    if (mType == Constant.TYPE_SONG) {
        return mSongListBeans.size();
    } else if (mType == Constant.TYPE_ALBUM) {
      return mAlbumList.size();
    }
    return 0;
  }

  @Override
  public int getItemViewType(int position) {
    return mType;
  }

  public void equalPosition(int position) {
    if (position != mLastPosition) {
      if (mLastPosition != 1) {
        notifyDataSetChanged();
        mLastPosition = position;
      }
    }
    notifyItemChanged(position);
  }
  static class ViewHolder extends RecyclerView.ViewHolder {

    private final TextView titleTv;
    private TextView artistTv;
    private RippleView mItemView;
    private View playLine;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      titleTv = itemView.findViewById(R.id.tv_title);
      artistTv = itemView.findViewById(R.id.tv_artist);
      playLine = itemView.findViewById(R.id.line_play);
      mItemView = itemView.findViewById(R.id.ripple);
    }
  }

  static class AlbumViewHolder extends RecyclerView.ViewHolder {
    private ImageView albumIv;
    private TextView singerName;
    private TextView albumName;
    private RippleView item;
    private TextView publicTime;
    public AlbumViewHolder(@NonNull View itemView) {
      super(itemView);
      albumIv = itemView.findViewById(R.id.iv_album);
      singerName = itemView.findViewById(R.id.tv_singer_name);
      albumName = itemView.findViewById(R.id.tv_album_name);
      publicTime = itemView.findViewById(R.id.tv_public_time);
      item = itemView.findViewById(R.id.ripple);
    }
  }
}