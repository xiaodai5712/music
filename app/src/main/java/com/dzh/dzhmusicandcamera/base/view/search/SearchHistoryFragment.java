package com.dzh.dzhmusicandcamera.base.view.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.adapter.SearchHistoryAdapter;
import com.dzh.dzhmusicandcamera.base.entity.SearchHistory;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 2020/12/29
 * author: Dzh
 */
public class SearchHistoryFragment extends Fragment {
  private static final String TAG = "SearchHistoryFragment";

  private RecyclerView mRecycler;
  private SearchHistoryAdapter mAdapter;
  private LinearLayoutManager mLayoutManager;
  private List<SearchHistory> mSearchHistoryList;
  private List<SearchHistory> mTempList;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container
      , @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_search_history, container, false);
    mRecycler = view.findViewById(R.id.recycler_seek_history);
    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    showHistory();
    onClick();
  }

  private void showHistory() {
    mSearchHistoryList = new ArrayList<>();
    mTempList = new ArrayList<>();
    changeList();
    mAdapter = new SearchHistoryAdapter(mSearchHistoryList);
    mLayoutManager = new LinearLayoutManager(getActivity());
    mRecycler.setLayoutManager(mLayoutManager);
    mRecycler.setAdapter(mAdapter);
  }

  private void onClick() {
    mAdapter.setFooterClickListener(() -> {
      LitePal.deleteAll(SearchHistory.class);
      mRecycler.setVisibility(View.GONE);
    });

    mAdapter.setOnDeleteClickListener(position -> {
      SearchHistory searchHistory = mSearchHistoryList.get(position);
      if (searchHistory.isSaved()) {
        searchHistory.delete();
      }
      mTempList = LitePal.findAll(SearchHistory.class);
      changeList();
      mAdapter.notifyDataSetChanged();
    });

    mAdapter.setOnItemClickListener(position -> ((SearchFragment) getParentFragment())
        .setSeekEdit(mSearchHistoryList.get(position).getHistory()));
  }

  private void changeList() {
    mSearchHistoryList.clear();
    mTempList = LitePal.findAll(SearchHistory.class);
    if (mTempList.size() == 0) {
      mRecycler.setVisibility(View.INVISIBLE);
    } else {
      mRecycler.setVisibility(View.VISIBLE);
    }
    for (int i = mTempList.size(); i >= 0; i--) {
      SearchHistory searchHistory = mTempList.get(i);
      mSearchHistoryList.add(searchHistory);
    }
  }
}