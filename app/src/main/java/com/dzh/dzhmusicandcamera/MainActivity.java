package com.dzh.dzhmusicandcamera;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.SeekBar;

import com.dzh.dzhmusicandcamera.base.activity.BaseActivity;

import butterknife.BindView;

public class MainActivity extends BaseActivity {
    private static final String TAG = "DzhMainActivity";

    @BindView(R.id.sb_progress)
    SeekBar mSeekBar;

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected int getLayoutId() {
        Log.d(TAG, "getLayoutId: " + R.layout.activity_main);
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onClick() {

    }


}