package com.dzh.dzhmusicandcamera.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andexert.library.RippleView;
import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.app.App;
import com.dzh.dzhmusicandcamera.base.entity.LocalSong;
import com.dzh.dzhmusicandcamera.callback.OnItemClickListener;
import com.dzh.dzhmusicandcamera.util.FileUtil;

import java.util.List;

/**
 * Date: 2020/11/16
 * author: Dzh
 */
public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final String TAG = "SongAdapter";

  private int mFooterViewType = 1;
  private int mItemViewType = 0;
  private int mLastPosition = -1;

  private List<LocalSong> mMp3InfoList;
  private Context mContext;
  private OnItemClickListener mOnItemClickListener;

  public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    mOnItemClickListener = onItemClickListener;
  }

  public SongAdapter(Context context, List<LocalSong> mp3InfoList) {
    mContext = context;
    mMp3InfoList = mp3InfoList;
  }


  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (viewType == mItemViewType) {
      View view = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.recycler_song_item, parent, false);
      ViewHolder viewHolder = new ViewHolder(view);
      return viewHolder;
    } else {
      View footerView = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.footer_local_songs_item, parent, false);
      FooterHolder footerHolder = new FooterHolder(footerView);
      return footerHolder;
    }
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
    if (viewHolder instanceof ViewHolder) {
      ViewHolder holder = (ViewHolder) viewHolder;
      final LocalSong mp3Info = mMp3InfoList.get(position);

      holder.songNameTv.setText(mp3Info.getName());
      holder.artistTv.setText(mp3Info.getSinger());

      // 根据播放的歌曲是否为当前列表的歌曲显示
      String songId = FileUtil.getSong().getSongId();
      if (songId != null && (mp3Info.getSongId().equals(songId))) {
        holder.songNameTv.setTextColor(App.getContext().getResources()
            .getColor(R.color.musicStyle_low));
        holder.artistTv.setTextColor(App.getContext().getResources()
            .getColor(R.color.musicStyle_low));
        holder.playingIv.setVisibility(View.VISIBLE);
        mLastPosition = position;
      } else {
        holder.songNameTv.setTextColor(App.getContext().getResources().getColor(R.color.white));
        holder.artistTv.setTextColor(App.getContext().getResources().getColor(R.color.white));
        holder.playingIv.setVisibility(View.GONE);
      }
      holder.songView.setOnRippleCompleteListener(rippleView -> {
        mOnItemClickListener.onClick(position);
        equalPosition(position);
      });
    } else {
      FooterHolder footerHolder = (FooterHolder) viewHolder;
      footerHolder.numTv.setText("共" +  mMp3InfoList.size() +"首音乐");
    }
  }

  @Override
  public int getItemCount() {
    return mMp3InfoList.size() + 1;  // dzh? 这个为什么要加 1
  }

  @Override
  public int getItemViewType(int position) {
    return position + 1 == getItemCount() ? mFooterViewType : mItemViewType;  // dzh? 这个方法有什么用
  }

  // 判断点击的是否为上一个点击的项目
  public void equalPosition(int position) {
    if (position != mLastPosition) {
      notifyItemChanged(mLastPosition);
      mLastPosition = position;
    }
    notifyItemChanged(position);
  }

  static class ViewHolder extends RecyclerView.ViewHolder {

    TextView songNameTv;
    TextView artistTv;
    ImageView playingIv;
    RippleView songView;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      songView = itemView.findViewById(R.id.ripple);
      songNameTv = itemView.findViewById(R.id.tv_song_name);
      artistTv = itemView.findViewById(R.id.tv_artist);
      playingIv = itemView.findViewById(R.id.iv_playing);
    }
  }

  static class FooterHolder extends RecyclerView.ViewHolder {

    TextView numTv;

    public FooterHolder(@NonNull View itemView) {
      super(itemView);
      numTv = itemView.findViewById(R.id.tv_song_num);
    }
  }
}