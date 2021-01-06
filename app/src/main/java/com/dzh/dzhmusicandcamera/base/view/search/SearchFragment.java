package com.dzh.dzhmusicandcamera.base.view.search;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.andexert.library.RippleView;
import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.base.entity.SearchHistory;
import com.dzh.dzhmusicandcamera.util.CommonUtil;

import org.litepal.LitePal;

import java.util.List;

/**
 * Date: 2020/12/25
 * author: Dzh
 */
public class SearchFragment extends Fragment {

  private static final String TAG = "DzhSearchFragment";
  private EditText mSeekEdit;
  private RippleView mSeekTv;
  private RippleView mBackIv;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container
      , @Nullable Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_search, container, false);
    mSeekEdit = view.findViewById(R.id.edit_seek);
    mSeekTv = view.findViewById(R.id.tv_search);
    mBackIv = view.findViewById(R.id.iv_back);
    replaceFragment(new SearchHistoryFragment());
    return view;
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    CommonUtil.showKeyboard(mSeekEdit, getActivity());

    mSeekTv.setOnClickListener(v -> {
      CommonUtil.closeKeyboard(mSeekEdit, getActivity());
      mSeekEdit.setCursorVisible(false); // 隐藏光标
      if (mSeekEdit.getText().toString().trim().length() == 0) {
        mSeekEdit.setText(mSeekEdit.getHint().toString().trim());
      }
      saveDataBase(mSeekEdit.getText().toString());
      replaceFragment(ContentFragment.newInstance(mSeekEdit.getText().toString()));
    });

    mSeekEdit.setOnTouchListener((v, e) -> {
      if (e.getAction() == MotionEvent.ACTION_DOWN) {
        mSeekEdit.setCursorVisible(true);
      }
      return true;
    });

    mBackIv.setOnClickListener(v -> {
      CommonUtil.closeKeyboard(mSeekEdit, getActivity());
      getActivity().getSupportFragmentManager().popBackStack();
    });
  }

  public void setSeekEdit(String seek) {
    mSeekEdit.setText(seek);
    mSeekEdit.setCursorVisible(false); // 隐藏光标
    mSeekEdit.setSelection(seek.length());
    CommonUtil.closeKeyboard(mSeekEdit, getActivity());
    saveDataBase(seek);
    replaceFragment(ContentFragment.newInstance(mSeekEdit.getText().toString()));
  }

  // 搜索后的页面
  private void replaceFragment(Fragment fragment) {
    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
    transaction.replace(R.id.container, fragment).commit();
  }

  private void saveDataBase(String seekHistory) {
    List<SearchHistory> searchHistoryList = LitePal.where("history=?", seekHistory)
        .find(SearchHistory.class);
    if (searchHistoryList.size() == 1) {
      LitePal.delete(SearchHistory.class, searchHistoryList.get(0).getId());
      SearchHistory searchHistory = new SearchHistory();
      searchHistory.setHistory(seekHistory);
      searchHistory.save();
    }
  }
}