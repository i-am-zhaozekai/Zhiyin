package com.haha.zy.lyric;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.haha.zy.lyric.model.LyricsInfo;
import com.haha.zy.lyric.utils.LyricsIOUtils;
import com.haha.zy.lyric.utils.LyricsUtil;
import com.haha.zy.net.api.kugou.DownloadLyricsUtil;
import com.haha.zy.player.EventManager;
import com.haha.zy.player.PlaybackInfo;
import com.haha.zy.preference.PreferenceManager;
import com.haha.zy.util.FileUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 歌词管理器
 * Created by zhangliangming on 2017/8/13.
 */

public class LyricsManager {

    private Context mContext;
    private PreferenceManager mPrefMgr;

    private static Map<String, LyricsUtil> mLyricsUtils = new HashMap<String, LyricsUtil>();

    private static volatile LyricsManager sInstance;

    public LyricsManager(Context context) {
        mContext = context;
        mPrefMgr = PreferenceManager.getInstance(mContext);
    }

    public static LyricsManager getLyricsManager(Context context) {
        if (sInstance == null) {
            synchronized (LyricsManager.class) {
                if (sInstance == null) {
                    sInstance = new LyricsManager(context);
                }
            }
        }
        return sInstance;
    }

    /**
     * @param fileName
     * @param keyword
     * @param duration
     * @param hash
     * @return
     */
    public void loadLyricsUtil(final String fileName, final String keyword, final String duration, final String hash) {
        //1.从缓存中获取
        //2.从本地文件中获取
        //3.从网络中获取
        new AsyncTask<String, Integer, Void>() {

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                PlaybackInfo audioMessage = new PlaybackInfo();
                audioMessage.setHash(hash);
                //发送加载完成广播
                Intent loadedIntent = new Intent(EventManager.ACTION_LRCLOADED);
                loadedIntent.putExtra(PlaybackInfo.KEY, audioMessage);
                loadedIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(loadedIntent);
            }

            @Override
            protected Void doInBackground(String... strings) {

                PlaybackInfo audioMessage = new PlaybackInfo();
                audioMessage.setHash(hash);
                //发送搜索中广播
                Intent searchingIntent = new Intent(EventManager.ACTION_LRCSEARCHING);
                searchingIntent.putExtra(PlaybackInfo.KEY, audioMessage);
                searchingIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(searchingIntent);

                if (mLyricsUtils.containsKey(hash)) {
                    return null;
                }
                //
                File lrcFile = LyricsUtil.getLrcFile(mContext, fileName);
                if (lrcFile != null) {
                    LyricsUtil lyricsUtil = new LyricsUtil();
                    lyricsUtil.loadLrc(lrcFile);
                    mLyricsUtils.put(hash, lyricsUtil);
                } else {

                    //发送下载中广播
                    Intent downloadingIntent = new Intent(EventManager.ACTION_LRCDOWNLOADING);
                    downloadingIntent.putExtra(PlaybackInfo.KEY, audioMessage);
                    downloadingIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mContext.sendBroadcast(downloadingIntent);

                    //下载歌词
                    String saveFilePath = FileUtil.getAppPublicDirectory(mContext, null)
                            + File.separator + FileUtil.PATH_LYRICS + File.separator + fileName + ".krc";
                    File saveLrcFile = new File(saveFilePath);
                    byte[] base64ByteArray = DownloadLyricsUtil.downloadLyric(mContext, keyword, duration, hash);
                    if (base64ByteArray != null && base64ByteArray.length > 1024) {
                        LyricsUtil lyricsUtil = new LyricsUtil();
                        lyricsUtil.loadLrc(base64ByteArray, saveLrcFile, saveLrcFile.getName());
                        mLyricsUtils.put(hash, lyricsUtil);
                    }
                }

                return null;

            }
        }.execute("");
    }

    public LyricsUtil getLyricsUtil(String hash) {
        return mLyricsUtils.get(hash);
    }

    /**
     * 使用该歌词
     *
     * @param hash
     * @param lyricsUtil
     */
    public void setUseLrcUtil(String hash, LyricsUtil lyricsUtil) {
        PlaybackInfo audioMessage = new PlaybackInfo();
        audioMessage.setHash(hash);
        //发送搜索中广播
        Intent searchingIntent = new Intent(EventManager.ACTION_LRCSEARCHING);
        searchingIntent.putExtra(PlaybackInfo.KEY, audioMessage);
        searchingIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        mContext.sendBroadcast(searchingIntent);

        if (mLyricsUtils.containsKey(hash)) {
            mLyricsUtils.remove(hash);
        }
        mLyricsUtils.put(hash, lyricsUtil);

        //保存歌词文件
        saveLrcFile(lyricsUtil.getLrcFilePath(), lyricsUtil.getLyricInfo());

        //发送加载完成广播
        Intent loadedIntent = new Intent(EventManager.ACTION_LRCLOADED);
        loadedIntent.putExtra(PlaybackInfo.KEY, audioMessage);
        loadedIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        mContext.sendBroadcast(loadedIntent);
    }

    /**
     * 保存歌词文件
     *
     * @param lrcFilePath lrc歌词路径
     * @param lyricsInfo  lrc歌词数据
     */
    private void saveLrcFile(final String lrcFilePath, final LyricsInfo lyricsInfo) {
        new Thread() {

            @Override
            public void run() {

                //保存修改的歌词文件
                try {
                    LyricsIOUtils.getLyricsFileWriter(lrcFilePath).writer(lyricsInfo, lrcFilePath);
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

        }.start();
    }
}
