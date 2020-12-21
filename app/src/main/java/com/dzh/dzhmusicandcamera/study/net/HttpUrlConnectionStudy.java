package com.dzh.dzhmusicandcamera.study.net;

import android.util.Log;

import com.dzh.dzhmusicandcamera.study.IO.NameValuePair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 2020/12/15
 * author: Dzh
 */
public class HttpUrlConnectionStudy {

  final static String TAG = "DzhHttpUrlConnectionStudy";
  private void executeHttpUrlClientRequest() {
    new Thread (() -> {
      Log.d(TAG, "onCreate: 线程启动" );
      useUrlConnectionPost("https://baidu.com");
    }).start();
  }


  private void useUrlConnectionPost(String url) {
    InputStream inputStream = null;
    HttpURLConnection connection = getHttpUrlConnection(url);
    try {
      List<NameValuePair> postParams = new ArrayList<>();
      postParams.add(new NameValuePair("ip", "59.108.54.37"));
      postParams(connection.getOutputStream(), postParams);
      connection.connect();
      inputStream = connection.getInputStream();
      int code = connection.getResponseCode();
      String response = convertStreamToString(inputStream);
      Log.d(TAG, "useUrlConnectionPost: code = " + code + "\n 请求结果 \n" + response );
      inputStream.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  private HttpURLConnection getHttpUrlConnection(String urlStr) {
    HttpURLConnection connection = null;
    try {
      URL url = new URL(urlStr);
      connection = (HttpURLConnection) url.openConnection();
      // 连接超时时间
      connection.setConnectTimeout(15000);
      // 读取超时时间
      connection.setReadTimeout(15000);
      // 设置请求参数
      connection.setRequestMethod("GET");
      // header
      connection.setRequestProperty("Connection", "Keep-Alive");
      // 接收输入流
      connection.setDoInput(true);
      // 传递参数时开启
      connection.setDoOutput(true);

    } catch (IOException e) {
      e.printStackTrace();
    }
    return  connection;
  }

  private void postParams(OutputStream outputStream, List<NameValuePair> paramList) throws IOException {
    StringBuilder sb = new StringBuilder();
    for (NameValuePair pair : paramList) {
      if (stringIsEmpty(sb)) {
        sb.append("&");
      }
      sb.append(URLEncoder.encode(pair.getName(), "UTF-8"));
      sb.append("=");
      sb.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
    }
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
    writer.write(sb.toString());
    writer.flush();
    writer.close();
  }

  private String convertStreamToString(InputStream inputStream) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    StringBuffer sb = new StringBuffer();
    String line = null;
    while ((line = reader.readLine()) != null) {
      sb.append(line);
    }
    return sb.toString();
  }

  private boolean stringIsEmpty(StringBuilder stringBuilder) {
    return stringBuilder == null || stringBuilder.length() < 1;
  }
}