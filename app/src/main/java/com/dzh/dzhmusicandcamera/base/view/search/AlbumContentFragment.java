package com.dzh.dzhmusicandcamera.base.view.search;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.app.Constant;
import com.dzh.dzhmusicandcamera.base.entity.AlbumCollection;
import com.dzh.dzhmusicandcamera.event.AlbumCollectionEvent;
import com.dzh.dzhmusicandcamera.util.CommonUtil;
import com.github.florent37.materialviewpager.MaterialViewPager;

import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;

/**
 * Date: 2020/12/22
 * author: Dzh
 */
public class AlbumContentFragment extends Fragment {
  private static final String TAG = "DzhAlbumContentFragment";
  private String mAlbumName, mSingerName, mAlbumPic, mPublicTime, mId;
  private MaterialViewPager mViewPager;
  private Toolbar mToolbar;
  private RelativeLayout mAlbumBackground;
  private TextView mSingerNameTv;
  private TextView mPublicTimeTv;
  private ImageView mAlbumPicIv;
  private MenuItem mLoveBtn;
  private boolean mLove;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate: ");
    setHasOptionsMenu(true); // 加上这句话，menu才会显示出来
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container
      , @Nullable Bundle savedInstanceState) {
    getBundle();
    View view = inflater.inflate(R.layout.fragment_album_content, container, false);
    mViewPager = view.findViewById(R.id.material_view_pager);
    mToolbar = mViewPager.getToolbar();
    mAlbumBackground = mViewPager.getHeaderBackgroundContainer().findViewById(R.id.relative_album);
    mAlbumPicIv = mViewPager.getHeaderBackgroundContainer().findViewById(R.id.iv_album);
    mSingerNameTv = mViewPager.getHeaderBackgroundContainer().findViewById(R.id.tv_singer_name);
    mPublicTimeTv = mViewPager.getHeaderBackgroundContainer().findViewById(R.id.tv_public_time);
    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    initView();
  }

  private void initView() {
    mToolbar.setTitle(mAlbumName);
    CustomTarget<Drawable> target = new CustomTarget<Drawable>() {
      @Override
      public void onResourceReady(@NonNull Drawable resource
          , @Nullable Transition<? super Drawable> transition) {
        Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
        mAlbumBackground.setBackground(CommonUtil.getForegroundDrawable(bitmap));
        mAlbumPicIv.setImageBitmap(bitmap);
      }

      @Override
      public void onLoadCleared(@Nullable Drawable placeholder) {

      }
    };
    Glide.with(getActivity())
        .load(mAlbumPic)
        .apply(RequestOptions.placeholderOf(R.drawable.welcome))
        .apply(RequestOptions.errorOf(R.drawable.welcome))
        .into(target);

    mSingerNameTv.setText("歌手 " + mSingerName);
    mPublicTimeTv.setText("发行时间 " + mPublicTime);
    mToolbar.setTitleTextColor(getActivity().getResources().getColor(R.color.white, null));
    if (mToolbar != null) {
      ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
      final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
      if (actionBar != null) {
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
      }
    }

    // 返回键的监听
    mToolbar
        .setNavigationOnClickListener( v -> getActivity().getSupportFragmentManager().popBackStack());
    mViewPager.getViewPager().setAdapter(new FragmentStatePagerAdapter() {
      @NonNull
      @Override
      public Fragment getItem(int position) {
        switch (position) {
          case 0:
            return AlbumSongFragment.newInstance(AlbumSongFagment.ALBUM_SONG, mId, mPublicTime);
          case 1:
            return AlbumSongFragment.newInstance(AlbumSongFragment.ALBUM_INFORMATION, mId, mPublicTime);
          default:
            return null;
        }
      }

      @Override
      public int getCount() {
        return 2;
      }

      @Nullable
      @Override
      public CharSequence getPageTitle(int position) {
        switch (position) {
          case 0:
            return "歌曲列表";
          case 1:
            return "专辑信息";
        }
        return "";
      }
    });

    mViewPager.getPagerTitleStrip().setViewPager(mViewPager.getViewPager());
    mViewPager.getPagerTitleStrip().setIndicatorColorResource(R.color.yellow);
    mViewPager.getPagerTitleStrip().setTabBackground(R.color.tab);
    mViewPager.getPagerTitleStrip().setTextColorStateListResource(R.color.white);
   }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.btn_love:
        if (mLove) {
          LitePal.deleteAllAsync(AlbumCollection.class, "albumId=?", mId).listen(i -> {
            mLoveBtn.setIcon(R.drawable.favorites);
            CommonUtil.showToast(getActivity(), "你已取消收藏该专辑");
          });
        } else {
          AlbumCollection albumCollection = new AlbumCollection();
          albumCollection.setAlbumId(mId);
          albumCollection.setAlbumName(mAlbumName);
          albumCollection.setAlbumPic(mAlbumPic);
          albumCollection.setPublicTime(mPublicTime);
          albumCollection.setSingerName(mSingerName);
          albumCollection.saveAsync().listen(success -> {
            mLoveBtn.setIcon(R.drawable.favorites_selected);
            CommonUtil.showToast(getActivity(), "专辑收藏成功");
          });
        }
        mLove = !mLove;
        // 发送收藏改变事件
        EventBus.getDefault().post(new AlbumCollectionEvent());
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  private void showLove() {
    if (LitePal.where("album=?", mId).find(AlbumCollection.class).size() != 0) {
      mLove = true;
      mLoveBtn.setIcon(R.drawable.favorites_selected);
    } else {
      mLove = false;
      mLoveBtn.setIcon(R.drawable.favorites);
    }
  }

  public static Fragment newInstance(String id, String albumName, String albumPic, String singerName
      , String publicTime) {
    AlbumContentFragment albumContentFragment = new AlbumContentFragment();
    Bundle bundle = new Bundle();
    bundle.putString(Constant.ALBUM_ID_KEY, id);
    bundle.putString(Constant.ALBUM_NAME_KEY, albumName);
    bundle.putString(Constant.ALBUM_PIC_KEY, albumPic);
    bundle.putString(Constant.SINGER_NAME_KEY, singerName);
    bundle.putString(Constant.PUBLIC_TIME_KEY, publicTime);
    albumContentFragment.setArguments(bundle);
    return albumContentFragment;
  }

  private void getBundle() {
    Bundle bundle = getArguments();
    if (bundle != null) {
      mId = bundle.getString(Constant.ALBUM_ID_KEY);
      mAlbumName = bundle.getString(Constant.ALBUM_NAME_KEY);
      mAlbumPic = bundle.getString(Constant.ALBUM_PIC_KEY);
      mSingerName = bundle.getString(Constant.SINGER_NAME_KEY);
      mPublicTime = bundle.getString(Constant.PUBLIC_TIME_KEY);
    }
  }


}