package com.dzh.dzhmusicandcamera.base.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dzh.dzhmusicandcamera.MainActivity;
import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.app.App;
import com.dzh.dzhmusicandcamera.util.CommonUtil;

import java.io.File;
import java.io.IOException;

public class WelcomeActivity extends AppCompatActivity {

    private final static String TAG = "DzhWelcomeActivity";
    private final static int REQUEST_STORAGE = 1;
    private final static int HANDLER_DELAY_DURATION = 1000;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            getHome();
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonUtil.hideStatusBar(this, true);
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_welcome);
        // 申请权限
        if (ContextCompat.checkSelfPermission(WelcomeActivity.this
            , Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(WelcomeActivity.this
                , new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE);
        } else {
            mHandler.sendEmptyMessageDelayed(0, HANDLER_DELAY_DURATION);
        }
        test();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions
        , @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mHandler.sendEmptyMessageDelayed(0, HANDLER_DELAY_DURATION);
            } else {
                Toast.makeText(this, "拒绝权限将无法使用该程序", Toast.LENGTH_SHORT)
                    .show();
            }
        }
    }

    private void playMusic() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {

            String url = "/storage/emulated/0/tempmusic/小阿七 - 不谓侠 [mqms2].flac";

            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void getHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void test() {
        Log.d(TAG, "onCreate: " + App.getContext().getExternalFilesDir(null).getAbsolutePath());
        Log.d(TAG, "onCreate: " + App.getContext().getExternalCacheDir().getAbsolutePath());
        Log.d(TAG, "onCreate: " + App.getContext().getFilesDir().getAbsolutePath());

        Log.d(TAG, "onCreate: " + App.getContext().getCacheDir().getAbsolutePath());
        File[] files = App.getContext().getExternalMediaDirs();
        for (File file : files) {
            Log.d(TAG, "onCreate: file.getAbsolutePath() : " + file.getAbsolutePath());
        }
        Log.d(TAG, "onCreate: " + Environment.getExternalStorageDirectory().getAbsolutePath());
        Log.d(TAG, "onCreate: " + Environment.getExternalStoragePublicDirectory("").getAbsolutePath());
        Log.d(TAG, "onCreate: " + getExternalFilesDir(Environment.DIRECTORY_MUSIC));

        findViewById(R.id.music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
                playMusic();
            }
        });
    }
}