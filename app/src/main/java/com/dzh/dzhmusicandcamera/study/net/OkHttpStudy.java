package com.dzh.dzhmusicandcamera.study.net;

import android.util.Log;

import com.dzh.dzhmusicandcamera.app.Constant;
import com.dzh.dzhmusicandcamera.study.Constants;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Date: 2020/12/15
 * author: Dzh
 */
public class OkHttpStudy {
  private final static String TAG = "OkHttpStudy";

  public static void asyncGetRequest() {
    Log.d(TAG, "asyncRequest: ");
    Request.Builder builder = new Request.Builder();
    builder.url(Constants.BAIDU_URL);
    builder.method("GET", null);
    OkHttpClient client = new OkHttpClient();
    Call call = client.newCall(builder.build());
    call.enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        Log.d(TAG, "onFailure: ");
        Log.d(TAG, "onFailure: e" + e);
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        String s = response.body().string();
        Log.d(TAG, "onResponse: s = " + s);
      }
    });
  }

  public static void asyncPostRequest() {
    RequestBody body = new FormBody.Builder()
        .add("ip", "59.108.54.37")
        .build();
    Request request = new Request.Builder()
        .url(Constants.CSDN_URL)
        .post(body)
        .build();
    OkHttpClient client = new OkHttpClient();
    Call call = client.newCall(request);
    call.enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        Log.d(TAG, "onFailure: " + e);
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        String s = response.body().string();
        Log.d(TAG, "onResponse: s = " + s);
      }
    });
  }
}