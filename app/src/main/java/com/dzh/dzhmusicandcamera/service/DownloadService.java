package com.dzh.dzhmusicandcamera.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.dzh.dzhmusicandcamera.MainActivity;
import com.dzh.dzhmusicandcamera.R;
import com.dzh.dzhmusicandcamera.app.Api;
import com.dzh.dzhmusicandcamera.app.Constant;
import com.dzh.dzhmusicandcamera.base.entity.DownloadInfo;
import com.dzh.dzhmusicandcamera.download.DownloadListener;
import com.dzh.dzhmusicandcamera.download.DownloadTask;
import com.dzh.dzhmusicandcamera.event.DownloadEvent;
import com.dzh.dzhmusicandcamera.event.SongListNumEvent;
import com.dzh.dzhmusicandcamera.util.CommonUtil;
import com.dzh.dzhmusicandcamera.util.DownloadUtil;

import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Date: 2020/10/25
 * author: Dzh
 */
public class DownloadService extends Service {
  private static final String TAG = "DownloadService";
  private DownloadTask mDownloadTask;
  private String mDownloadUrl;
  private DownloadBinder mDownloadBinder = new DownloadBinder();
  private LinkedList<DownloadInfo> mDownloadQueue = new LinkedList<>(); // 等待队列
  private int mPosition = 0; // 下载歌曲在下载歌曲列表的位置
  private DownloadListener mListener = new DownloadListener() {
    @Override
    public void onProgress(DownloadInfo downloadInfo) {
      downloadInfo.setStatus(Constant.DOWNLOAD_ING);
      EventBus.getDefault().post(new DownloadEvent(Constant.TYPE_DOWNLOADING)); // 通知下载模块
      if (downloadInfo.getProgress() != 100) {
        getNotificationManager().notify(1, getNotification("正在下载： "
            + downloadInfo.getSongName(), downloadInfo.getProgress()));
      } else {
        if (mDownloadQueue.isEmpty()) {
          getNotificationManager().notify(1, getNotification("下载成功", -1));
        }
      }
    }

    @Override
    public void onSuccess() {
      mDownloadTask = null;
      DownloadInfo downloadInfo = mDownloadQueue.poll();
      operateDb(downloadInfo); // 操作数据库
      start();
    }

    @Override
    public void onDownloaded() {

    }

    @Override
    public void onFailed() {

    }

    @Override
    public void onPaused() {

    }

    @Override
    public void onCanceled() {

    }
  };

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  private void start() {
    if (mDownloadTask == null && !mDownloadQueue.isEmpty()) {
      DownloadInfo downloadInfo = mDownloadQueue.peek();
      List<DownloadInfo> songList =
          LitePal.where("songId = ?", downloadInfo.getSongId()).find(DownloadInfo.class);
      DownloadInfo currentDownloadInfo = songList.get(0);
      currentDownloadInfo.setStatus(Constant.DOWNLOAD_READY);
      EventBus.getDefault().post(new DownloadEvent(Constant.TYPE_DOWNLOADING, currentDownloadInfo));
      mDownloadUrl = currentDownloadInfo.getUrl();
      mDownloadTask = new DownloadTask(mListener);
      mDownloadTask.execute(currentDownloadInfo);
      getNotificationManager().
          notify(1, getNotification("正在下载" + downloadInfo.getSongName(), 0));
    }
  }

  public class DownloadBinder extends Binder {
    public void startDownload(DownloadInfo song) {
      try {
        postDownloadEvent(song); // 通知正在下载界面
      } catch(Exception e) {
        e.printStackTrace();
      }
      if (mDownloadTask != null) {
        CommonUtil.showToast(DownloadService.this, "已经加入下载队列");
      } else {
        CommonUtil.showToast(DownloadService.this, "开始下载");
        start();
      }
    }
  }

  private Notification getNotification(String title, int progress) {
    Intent intent = new Intent(this, MainActivity.class);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent
        , 0);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      String id = "channel_001";
      String name = "下载通知";
      NotificationChannel channel = new NotificationChannel(id, name
          , NotificationManager.IMPORTANCE_LOW);
      getNotificationManager().createNotificationChannel(channel);

      Notification.Builder builder = new Notification.Builder(this, id);
      builder.setSmallIcon(R.mipmap.icon);
      builder.setContentIntent(pendingIntent);
      builder.setContentTitle(title);
      if (progress > 0) {
        builder.setContentText(progress + "%");
        builder.setProgress(100, progress, false);
      }
      return builder.build();
    } else {
      NotificationCompat.Builder builder = new NotificationCompat.Builder(this
          , "default");
      builder.setSmallIcon(R.mipmap.icon);
      builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.icon));
      builder.setContentIntent(pendingIntent);
      builder.setContentTitle(title);
      if (progress > 0) {
        builder.setContentTitle(progress + "%");
        builder.setProgress(100, progress, false);
      }
      return builder.build();
    }
  }

  private void operateDb(DownloadInfo downloadInfo) {
    updateDb(downloadInfo.getSongId());
    deleteDb(downloadInfo.getSongId());
    EventBus.getDefault().post(new DownloadEvent(Constant.TYPE_DOWNLOAD_SUCCESS)); // 通知已下载列表
    EventBus.getDefault().post(new SongListNumEvent(Constant.LIST_TYPE_DOWNLOAD));   // 通知主界面的下载个数需要改变
  }

  // 更新数据库中歌曲列表的位置，即下载完成后的位置要减去1
  private void updateDb(String songId) {
    long id = LitePal.select("id").where("songId = ?", songId)
        .find(DownloadInfo.class).get(0).getId();
    List<DownloadInfo> songIdList = LitePal.where("id > ?", id + "")
        .find(DownloadInfo.class);
    for (DownloadInfo song : songIdList) {
      song.setPosition(song.getPosition() - 1);
      song.save();
    }
  }

  // 暂时更新列表歌曲状态
  private void updateDbOfPause(String songId) {
    List<DownloadInfo> statusList
        = LitePal.where("songId = ? ", songId).find(DownloadInfo.class, true);
    DownloadInfo downloadInfo = statusList.get(0);
    downloadInfo.setStatus(Constant.DOWNLOAD_PAUSED);
    downloadInfo.save();
  }

  // 下载完成是要删除下载歌曲表中的数据以及关联表中的数据
  private void deleteDb(String songId) {
    LitePal.deleteAll(DownloadInfo.class, "songId=?", songId); // 删除已下载歌曲的相关列
  }

  private void postDownloadEvent(DownloadInfo downloadInfo) {
    // 如果需要下载的表中有该条件， 则添加在下载队列后跳过
    List<DownloadInfo> downloadInfoList =
        LitePal.where("songId = ? ", downloadInfo.getSongId())
            .find(DownloadInfo.class, true);
    if (downloadInfoList.size() != 0) {
      DownloadInfo historyDownloadInfo = downloadInfoList.get(0);
      historyDownloadInfo.setStatus(Constant.DOWNLOAD_WAIT);
      historyDownloadInfo.save();
      EventBus.getDefault().post(new DownloadEvent(Constant.DOWNLOAD_PAUSED, historyDownloadInfo));
      mDownloadQueue.offer(historyDownloadInfo);
      return;
    }
    mPosition = LitePal.findAll(DownloadInfo.class).size();
    downloadInfo.setPosition(mPosition);
    downloadInfo.setStatus(Constant.DOWNLOAD_WAIT);
    downloadInfo.save();
    mDownloadQueue.offer(downloadInfo); // 将歌曲放到等待队列中
    EventBus.getDefault().post(new DownloadEvent(Constant.TYPE_DOWNLOAD_ADD));
  }

  // 获取歌曲的实际大小，然后判断是否存在文件中
  public void checkoutFile(DownloadInfo song, String downloadUrl) {
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url(downloadUrl).build();
    Call call = client.newCall(request);
    call.enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {

      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
          long size = response.body().contentLength();
          String fileName = DownloadUtil.getSaveSongFile(song.getSinger(), song.getSongName()
              , song.getDuration(), song.getSongId(), size);
          File downloadFile = new File(Api.STORAGE_SONG_FILE);
          String directory = String.valueOf(downloadFile);
          File file = new File(fileName, directory);
          if (file.exists()) {
            file.delete();
          }
          getNotificationManager().cancel(1);
          stopForeground(true);
        }
      }
    });
  }
  private NotificationManager getNotificationManager() {
    return (NotificationManager) (getSystemService(NOTIFICATION_SERVICE));
  }

}