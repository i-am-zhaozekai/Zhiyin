package com.haha.zy.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.haha.zy.audio.AudioInfo;
import com.haha.zy.player.PlaybackInfo;
import com.haha.zy.player.PlayerManager;
import com.haha.zy.util.FileUtil;
import com.haha.zy.util.SerializationUtil;

import java.io.File;
import java.util.List;

/**
 * @Description: Preference 处理辅助类
 * @Author: Terrence Zhao
 * @Date: 06/05/2018
 */

public class PreferenceManager {
    private static final String PREFERENCE_NAME = "com.haha.zy.main";

    private Context mContext;
    private SharedPreferences mPreferences;
    private static volatile PreferenceManager sInstance = null;

    private PreferenceManager(Context context) {
        mContext = context.getApplicationContext();
        mPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public static PreferenceManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (PreferenceManager.class) {
                if (sInstance == null) {
                    sInstance = new PreferenceManager(context);
                }
            }
        }

        return sInstance;
    }

    public boolean saveValue(String key, Object data) {
        SharedPreferences.Editor editor = mPreferences.edit();
        if (data instanceof Boolean) {
            editor.putBoolean(key, (Boolean) data);
        } else if (data instanceof Integer) {
            editor.putInt(key, (Integer) data);
        } else if (data instanceof String) {
            editor.putString(key, (String) data);
        } else if (data instanceof Float) {
            editor.putFloat(key, (Float) data);
        } else if (data instanceof Long) {
            editor.putFloat(key, (Long) data);
        }

        return editor.commit();
    }

    public Object getValue(String key, Object defData) {
        if (defData instanceof Boolean) {
            return mPreferences.getBoolean(key, (Boolean) defData);
        } else if (defData instanceof Integer) {
            return mPreferences.getInt(key, (Integer) defData);
        } else if (defData instanceof String) {
            return mPreferences.getString(key, (String) defData);
        } else if (defData instanceof Float) {
            return mPreferences.getFloat(key, (Float) defData);
        } else if (defData instanceof Long) {
            return mPreferences.getLong(key, (Long) defData);
        }

        return null;
    }

    //------------------------------- APP 相关配置项 ---------------------------
    private static final String IS_FIRST_TIME = "is_first_time";
    // 仅 WIFI 联网
    private static final String WIFI_ONLY = "wifi_only";
    // 正在播放歌曲的文件 hash
    private static final String CURRENT_AUDIO_HASH = "current_audio_hash";
    // 播放模式, 0 - 顺序播放 1 - 随机播放 2 - 循环播放 3 - 单曲播放
    private static final String PLAY_MODE = "play_mode";
    /**
     * 播放状态
     * @see {@link PlayerManager.Status#PAUSE}, {@link PlayerManager.Status#STOP},
     * {@link PlayerManager.Status#PLAYING}, {@link PlayerManager.Status#PLAYNET}
     */
    private static final String PLAY_STATUS = "play_status";
    //歌词字体大小
    private static final String LYRIC_FONT_SIZE = "lyric_font_size";
    //歌词颜色索引
    private static final String LYRIC_COLOR_INDEX = "lyric_color_index";
    //是否开启线控
    private static final String WIRE_CONTROL = "wire_control";
    //是否多行歌词
    private static final String LYRIC_MULTI_LINE = "lyric_multi_line";

    public boolean isFirst() {
        return (boolean)getValue(IS_FIRST_TIME, true);
    }

    public void setFirst(boolean flag) {
        saveValue(IS_FIRST_TIME, flag);
    }

    public boolean isWifiOnly() {
        return (boolean)getValue(WIFI_ONLY, true);
    }

    public void setWifiOnly(boolean flag) {
        saveValue(WIFI_ONLY, flag);
    }

    public String getCurrentAudioHash() {
        return (String) getValue(CURRENT_AUDIO_HASH, "");
    }

    public void setCurrentAudioHash(String hash) {
        saveValue(CURRENT_AUDIO_HASH, hash);
    }

    public int getPlayMode() {
        return (int) getValue(PLAY_MODE, 0);
    }

    public void setPlayMode(int playMode) {
        saveValue(PLAY_MODE, playMode);
    }

    public int getPlayStatus() {
        return (int) getValue(PLAY_STATUS, PlayerManager.Status.STOP);
    }

    public void setPlayStatus(int playStatus) {
        saveValue(PLAY_STATUS, playStatus);
    }


    //------------------------- 序列化对象-----------------------

    /**
     * 当前播放列表
     */
    private List<AudioInfo> mCurrentPlaylist;
    /**
     * 设置当前正在播放的歌曲
     */
    private AudioInfo mCurrentAudio;

    private PlaybackInfo mPlaybackInfo;

    public List<AudioInfo> getCurrentPlaylist() {
        if (mCurrentPlaylist == null) {
            mCurrentPlaylist = (List<AudioInfo>) read("curPlaylist.ser");
        }

        return mCurrentPlaylist;
    }

    public void setCurrentPlaylist(final List<AudioInfo> audioList) {
        mCurrentPlaylist = audioList;
        new Thread() {
            @Override
            public void run() {
                save(audioList, "curPlaylist.ser");
            }
        }.start();
    }

    public AudioInfo getCurrentAudio() {
        if (mCurrentAudio == null) {
            mCurrentAudio = (AudioInfo) read("curAudio.ser");
        }
        return mCurrentAudio;
    }

    public void setCurrentAudio(final AudioInfo currentAudio) {
        this.mCurrentAudio = currentAudio;
        new Thread() {
            @Override
            public void run() {
                save(currentAudio, "curAudio.ser");
            }
        }.start();
    }

    public PlaybackInfo getPlaybackInfo() {
        if (mPlaybackInfo == null) {
            mPlaybackInfo = (PlaybackInfo) read("playbackInfo.ser");
        }
        return mPlaybackInfo;
    }

    public void setPlaybackInfo(final PlaybackInfo playbackInfo) {
        this.mPlaybackInfo = playbackInfo;
        new Thread() {
            @Override
            public void run() {
                save(playbackInfo, "playbackInfo.ser");
            }
        }.start();
    }

    /*public RankListResult getRankListResult() {
        if (rankListResult == null) {
            logger.e("rankListResult为空，从本地获取");
            String filePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_CACHE_SERIALIZABLE, "rankListResult.ser");
            rankListResult = (RankListResult) SerializableObjUtil.readObj(filePath);
        }
        return rankListResult;
    }

    public void setRankListResult(final RankListResult rankListResult) {
        this.rankListResult = rankListResult;
        new Thread() {
            @Override
            public void run() {
                String filePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_CACHE_SERIALIZABLE, "rankListResult.ser");
                if (rankListResult != null) {
                    SerializableObjUtil.saveObj(filePath, rankListResult);
                } else {
                    File file = new File(filePath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
        }.start();
    }*/

    private void save(Object object, String fileName) {
        String filePath = FileUtil.getFilePath(mContext, FileUtil.getDiskCacheDir(mContext),
                FileUtil.PATH_CACHE_SERIALIZABLE + File.separator + fileName);
        if (object != null) {
            SerializationUtil.save(filePath, object);
        } else {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private Object read(String fileName) {
        String filePath = FileUtil.getFilePath(mContext, FileUtil.PATH_CACHE_SERIALIZABLE, fileName);
        return SerializationUtil.read(filePath);
    }

    /**
     * 最小字体大小
     */
    private int minLrcFontSize = 30;

    /**
     * 最大字体大小
     */
    private int maxLrcFontSize = 50;

    public int getMinLrcFontSize() {
        return minLrcFontSize;
    }

    public int getMaxLrcFontSize() {
        return maxLrcFontSize;
    }

    /**
     * 歌词颜色集合
     */
    private String[] lrcColorStr = {"#fada83", "#fe8db6", "#feb88e",
            "#adfe8e", "#8dc7ff", "#e69bff"};

    public String[] getLrcColorStr() {
        return lrcColorStr;
    }

    public int getLrcFontSize() {
        return (int) getValue(LYRIC_FONT_SIZE, 30);
    }

    public void setLrcFontSize(int lrcFontSize) {
        saveValue(LYRIC_FONT_SIZE, lrcFontSize);
    }

    public int getLrcColorIndex() {
        return (int) getValue(LYRIC_COLOR_INDEX, 0);

    }

    public void setLrcColorIndex(int lrcColorIndex) {
        saveValue(LYRIC_COLOR_INDEX, lrcColorIndex);
    }

    public boolean isWire() {

        return (boolean) getValue(WIRE_CONTROL, true);
    }

    public void setWire(boolean wire) {
        saveValue(WIRE_CONTROL, wire);
    }

    public boolean isLrcMultiLine() {
        return (boolean) getValue(LYRIC_MULTI_LINE, true);
    }

    public void setLrcMultiLine(boolean flag) {
        saveValue(LYRIC_MULTI_LINE, flag);
    }

    //这个应该放到别的地方
    /**
     * 是否拖动歌词快进播放
     */
    private boolean isLrcSeekTo = true;
    public boolean isLrcSeekTo() {
        return isLrcSeekTo;
    }

    public void setLrcSeekTo(boolean lrcSeekTo) {
        isLrcSeekTo = lrcSeekTo;
    }
}
