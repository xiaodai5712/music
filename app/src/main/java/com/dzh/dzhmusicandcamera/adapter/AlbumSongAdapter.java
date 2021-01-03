package com.dzh.dzhmusicandcamera.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dzh.dzhmusicandcamera.base.entity.AlbumSong;
import com.dzh.dzhmusicandcamera.callback.OnItemClickListener;

import java.util.List;

/**
 * Date: 2020/12/25
 * author: Dzh
 */
public class AlbumSongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private List<AlbumSong.DataBean.ListBean> mSongBeanList;
  private int mLastPosition = -1;
  private OnItemClickListener mSongClick;
  private final int songType = 1;
  private final int footerType = 2;

  public AlbumSongAdapter(List<AlbumSong.DataBean.ListBean> songsBeans) {
    mSongBeanList = songsBeans;
  }

  public void setSongClick(OnItemClickListener songClick) {
    mSongClick = songClick;
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return null;
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

  }

  @Override
  public int getItemCount() {
    return 0;
  }

  private static class ViewHolder extends RecyclerView.ViewHolder {

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
    }
  }
}