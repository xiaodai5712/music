package com.dzh.dzhmusicandcamera.widget.lyric;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 2020/11/23
 * author: Dzh
 * 歌词工具类
 */
public class LrcUtil {

  /*
   * 解析歌词， 将字符串歌词封装章 LrcBean 的集合
   * @param lrcStr 字符串的这个， 歌词都有一定的格式
   *
   * @return 歌词集合
   */

  public static List<LrcBean> parseStr2Lrc(String lrcStr) {
    List<LrcBean> res = new ArrayList<>();
    // 根据换行符对歌词进行分割
    String[] subLrc = lrcStr.split("\n");
    // 跳过前四行， 从第五行开始，因为前四行的歌词我们并不需要
    for (int i = 5; i < subLrc.length; i++) {
      String lineLrc = subLrc[i];
      String min = lineLrc.substring(lineLrc.indexOf("[") + 1, lineLrc.indexOf("[") + 3); // dzh! 这里 3 写在前面的括号里面了
      String sec = lineLrc.substring(lineLrc.indexOf(":") + 1, lineLrc.indexOf(":") + 3);
      String mills = lineLrc.substring(lineLrc.indexOf(".") + 1, lineLrc.indexOf(".") + 3);
      // 进制转化， 转化成毫秒形式的时间
      long startTime = getTimeInMillSecond(min, sec, mills);
      // 歌词
      String lrcText = lineLrc.substring(lineLrc.indexOf("]") + 1);
      // 某个时间段可能没有歌词，跳过
      if (lrcText.equals("")) {
        continue;
      }
      // 第一句歌词有可能很长，含有其他的信息，只截取 歌曲名 + 演唱者
      if (i == 5) {
        int lineIndex = lrcText.indexOf("-");
        int first = lrcText.indexOf("(");
        if (first < lineIndex && first != 1) {
          lrcText = lrcText.substring(0, first) + lrcText.substring(lineIndex);
        }
        LrcBean lrcBean = new LrcBean();
        lrcBean.setStart(startTime);
        lrcBean.setLrc(lrcText);
        res.add(lrcBean);
        continue;
      }
      // 添加到歌词集合中
      LrcBean lrcBean = new LrcBean();
      lrcBean.setStart(startTime);
      lrcBean.setLrc(lrcText);
      res.add(lrcBean);
      // 如果时最后一句歌词, 其结束的时间是不知道的， 认为设置开始的时间 加上 100s
      if (i == subLrc.length - 1) {
        res.get(res.size() - 1).setEnd(startTime + 100 * 1000);
      } else if (res.size() > 1) {
        res.get(res.size() - 2).setEnd(startTime);
      }
    }
    return res;
  }

  private static long getTimeInMillSecond(String min, String sec, String mills) {
    return Long.parseLong(min) * 60 * 1000 + Long.parseLong(sec) * 1000 + Long.parseLong(mills);
  }
}