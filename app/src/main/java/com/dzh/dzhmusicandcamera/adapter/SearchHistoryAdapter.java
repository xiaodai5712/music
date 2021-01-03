package com.dzh.dzhmusicandcamera.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andexert.library.RippleView;
import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.base.entity.SearchHistory;
import com.dzh.dzhmusicandcamera.callback.OnDeleteClickListener;
import com.dzh.dzhmusicandcamera.callback.OnFooterClickListener;
import com.dzh.dzhmusicandcamera.callback.OnItemClickListener;

import java.util.List;

/**
 * Date: 2020/12/29
 * author: Dzh
 */
public class SearchHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int mHistoryType = 0;
  private static final int mFooterType = 1;

  private List<SearchHistory> mSearchHistoryList;
  private OnItemClickListener mOnItemClickListener;
  private OnDeleteClickListener mOnDeleteClickListener;
  private OnFooterClickListener mOnFooterClickListener;

  public SearchHistoryAdapter(List<SearchHistory> list) {
    mSearchHistoryList = list;
  }
  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (viewType == mHistoryType) {
      View view = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.recycler_seek_history_item, parent, false);
      return new HistoryHolder(view);
    } else {
      View view = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.footer_delete_all_history_item, parent, false);
      return new FooterHolder(view);
    }
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    if (holder instanceof HistoryHolder) {
      HistoryHolder historyHolder = (HistoryHolder) holder;
      historyHolder.historyTv.setText(mSearchHistoryList.get(position).getHistory());
      historyHolder.deleteIv.setOnClickListener(v -> mOnDeleteClickListener.onClick(position));
      historyHolder.mItemView.setOnClickListener(rippleView -> mOnItemClickListener.onClick(position));
    } else {
      FooterHolder footerHolder = (FooterHolder) holder;
      footerHolder.deleteView.setOnClickListener(v -> mOnFooterClickListener.onClick());
    }
  }

  @Override
  public int getItemCount() {
    return mSearchHistoryList.size() + 1; // dzh? 这里 .size() 为什么要 + 1?
  }

  @Override
  public int getItemViewType(int position) {
    return position + 1 == getItemCount() ? mFooterType : mHistoryType;
  }

  public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    mOnItemClickListener = onItemClickListener;
  }

  public void setOnDeleteClickListener(OnDeleteClickListener onDeleteClickListener) {
    mOnDeleteClickListener = onDeleteClickListener;
  }

  public void setFooterClickListener(OnFooterClickListener onFooterClickListener) {
    mOnFooterClickListener = onFooterClickListener;
  }

  private class FooterHolder extends RecyclerView.ViewHolder {

    View deleteView;
    public FooterHolder(@NonNull View itemView) {
      super(itemView);
      deleteView = itemView;
    }
  }

  private class HistoryHolder extends RecyclerView.ViewHolder {
    TextView historyTv;
    ImageView deleteIv;
    RippleView mItemView;
    public HistoryHolder(@NonNull View itemView) {
      super(itemView);
      historyTv = itemView.findViewById(R.id.tv_seek_history);
      deleteIv = itemView.findViewById(R.id.iv_history_delete);
      mItemView = itemView.findViewById(R.id.ripple);
    }

  }
}