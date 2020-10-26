package com.dzh.dzhmusicandcamera.download;

import com.dzh.dzhmusicandcamera.base.entity.DownloadInfo;

/**
 * Date: 2020/10/25
 * author: Dzh
 */
public interface DownloadListener {
  void onProgress(DownloadInfo downloadInfo); // 进度
  void onSuccess(); // 成功
  void onDownloaded(); // 已经下载过的歌曲
  void onFailed(); // 失败
  void onPaused(); // 暂停
  void onCanceled(); // 取消
}
