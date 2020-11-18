package com.dzh.dzhmusicandcamera.base.presenter;

import android.provider.ContactsContract;

import androidx.recyclerview.widget.RecyclerView;

import com.dzh.dzhmusicandcamera.base.view.BaseView;
import com.dzh.dzhmusicandcamera.model.DataModel;
import com.dzh.dzhmusicandcamera.model.db.DbHelperImpl;
import com.dzh.dzhmusicandcamera.model.https.NetworkHelperImpl;
import com.dzh.dzhmusicandcamera.model.https.api.RetrofitFactory;
import com.dzh.dzhmusicandcamera.model.prefs.PreferencesHelperImpl;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Date: 2020/11/5
 * author: Dzh
 */
public class BasePresenter<T extends BaseView> implements IPresenter<T> {

  protected T mView;
  private RecyclerView recyclerView;
  protected DataModel mModel;

  private CompositeDisposable mCompositeDisposable;

  // 得到 model
  public BasePresenter() {
    if (mModel == null) {
      mModel = new DataModel(new NetworkHelperImpl(RetrofitFactory.createRequest())
          , new DbHelperImpl(), new PreferencesHelperImpl());
    }
  }
  @Override
  public void attachView(T view) {
    mView = view;
  }

  @Override
  public boolean isAttachView() {
    return mView != null;
  }

  @Override
  public void detachView() {
    mView = null;
    // 清除
    if (mCompositeDisposable != null) {
      mCompositeDisposable.clear();
    }
  }

  // 网络请求时将订阅事件添加到容器中
  @Override
  public void addRxSubscribe(Disposable disposable) {
    if (mCompositeDisposable == null) {
      mCompositeDisposable = new CompositeDisposable();
    }
    mCompositeDisposable.add(disposable);
  }
}