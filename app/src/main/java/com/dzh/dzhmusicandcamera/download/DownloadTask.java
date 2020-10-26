package com.dzh.dzhmusicandcamera.download;

import android.os.AsyncTask;

import com.dzh.dzhmusicandcamera.app.Api;
import com.dzh.dzhmusicandcamera.app.Constant;
import com.dzh.dzhmusicandcamera.base.entity.DownloadInfo;
import com.dzh.dzhmusicandcamera.util.DownloadUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Date: 2020/10/25
 * author: Dzh
 */
public class DownloadTask extends AsyncTask<DownloadInfo, DownloadInfo, Integer> {

  private DownloadListener mDownloadListener;
  private boolean mIsCanceled = false;
  private boolean mIsPaused = false;
  private long mLastProgress;

  public DownloadTask(DownloadListener downloadListener) {
    mDownloadListener = downloadListener;
  }

  @Override
  protected Integer doInBackground(DownloadInfo... downloadInfos) {
    InputStream is = null;
    RandomAccessFile saveFile = null;
    File file = null;
    DownloadInfo downloadInfo = downloadInfos[0];
    try {
      long downloadedLength = 0; // 记录已下载文件的长度
      String downloadUrl = downloadInfo.getUrl();

      File downloadFile = new File(Api.STORAGE_SONG_FILE);
      if (downloadFile.exists()) {
        downloadFile.mkdirs();
      }
      // 传过来的下载地址
      // http://ws.stream.qqmusic.qq.com/C400001DI2Jj3Jqve9.m4a?guid=358840384&vkey=2B9BF114492F203C3943D8AE38C83DD8FEEA5E628B18F7F4455CA9B5059040266D74EBD43E09627AA4419D379B6A9E1FC1E5D2104AC7BB50&uin=0&fromtag=66
      long contentLength = getContentLength(downloadUrl); // 实际文件长度
      String fileName = DownloadUtil.getSaveSongFile(downloadInfo.getSinger()
          , downloadInfo.getSongName(), downloadInfo.getDuration(), downloadInfo.getSongId()
          , contentLength);
      file = new File(downloadFile, fileName);
      if (file.exists()) {
        downloadedLength = file.length();
      }
      if (contentLength == 0) {
        return Constant.TYPE_DOWNLOAD_FAILED;
      } else if (contentLength == downloadedLength) {
        return Constant.TYPE_DOWNLOADED;
      }

      OkHttpClient client = new OkHttpClient();
      Request request = new Request.Builder()
          // 断点下载， 指定从哪个位置开始下载
          .addHeader("RANGE", "bytes=" + downloadedLength + "-")
          .url(downloadUrl).build();
      Response response = client.newCall(request).execute();

      if (request != null) {
        is = response.body().byteStream();
        saveFile = new RandomAccessFile(file, "rw");
        saveFile.seek(downloadedLength);
        byte[] b = new byte[1024];
        int total = 0;
        int len;
        while ((len = is.read(b)) != -1) {
          if (mIsCanceled) {
            return Constant.TYPE_DOWNLOAD_CANCELED;
          } else if(mIsPaused) {
            return Constant.TYPE_DOWNLOAD_PAUSED;
          } else {
            total += len;
            saveFile.write(b, 0, len);
            int progress = (int) ((total + downloadedLength ) * 100 / contentLength);
            downloadInfo.setProgress(progress);
            downloadInfo.setTotalSize(contentLength);
            downloadInfo.setCurrentSize(total + downloadedLength);
            publishProgress(downloadInfo);
          }
        }
        response.body().close();
        return Constant.TYPE_DOWNLOAD_SUCCESS;
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (is != null) {
          is.close();
        }
        if (saveFile != null) {
          saveFile.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return Constant.TYPE_DOWNLOAD_FAILED;
  }

  @Override
  protected void onProgressUpdate(DownloadInfo... values) {
    DownloadInfo downloadInfo = values[0];
    int progress = downloadInfo.getProgress();
    if (progress > mLastProgress) {
      mDownloadListener.onProgress(downloadInfo);
      mLastProgress = progress;
    }
  }

  @Override
  protected void onPostExecute(Integer status) {
    switch (status) {
      case Constant.TYPE_DOWNLOAD_SUCCESS:
        mDownloadListener.onSuccess();
        break;
      case Constant.TYPE_DOWNLOAD_FAILED:
        mDownloadListener.onFailed();
        break;
      case Constant.TYPE_DOWNLOAD_PAUSED:
        mDownloadListener.onPaused();
        break;
      case Constant.TYPE_DOWNLOAD_CANCELED:
        mDownloadListener.onCanceled();
        break;
      case Constant.TYPE_DOWNLOADED:
        mDownloadListener.onDownloaded();
        break;
      default:
        break;
    }
  }

  public void pauseDownload() {
    mIsPaused = true;
  }
  public void cancelDownload() {
    mIsCanceled = true;
  }
  private long getContentLength(String downloadUrl) throws IOException {
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url(downloadUrl).build();
    Response response = client.newCall(request).execute();
    if (response != null && response.isSuccessful()) {
      long contentLength = response.body().contentLength();
      response.body().close();
      return contentLength;
    }
    return 0;
  }

}