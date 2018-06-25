package com.haha.zy.player;

import android.content.Context;
import android.content.Intent;

import com.haha.zy.audio.AudioInfo;
import com.haha.zy.db.DatabaseHelper;
import com.haha.zy.preference.PreferenceManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 02/06/2018
 */

public class PlayerManager {

    private Context mContext;
    private PreferenceManager mPrefMgr;

    public static final class Status {
        /**
         * 正在播放
         */
        public static final int PLAYING = 0;
        /**
         * 暂停
         */
        public static final int PAUSE = 1;
        /**
         * 停止
         */
        public static final int STOP = 2;
        /**
         * 播放在线音乐
         */
        public static final int PLAYNET = 3;

    }

    public AudioInfo getPreviousSong(int playMode) {
        return PlayMode.getMode(playMode).getPreviousSong(mContext);
    }

    public AudioInfo getNextSong(int playMode) {
        return PlayMode.getMode(playMode).getNextSong(mContext);
    }

    private static volatile PlayerManager sInstance = null;
    private PlayerManager(Context context) {
        mContext = context.getApplicationContext();
        mPrefMgr = PreferenceManager.getInstance(mContext);
    }

    public static PlayerManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (PlayerManager.class) {
                if (sInstance == null) {
                    sInstance = new PlayerManager(context);
                }
            }
        }

        return sInstance;
    }

    public void initSongInfoData() {
        //从本地文件中获取上次的播放歌曲列表
        List<AudioInfo> playList = mPrefMgr.getCurrentPlaylist();

        if (playList != null && playList.size() > 0) {
            String playInfoHashID = mPrefMgr.getCurrentAudioHash();
            //
            if (playInfoHashID == null || playInfoHashID.equals("")) {

                resetData();

                //发送空数据广播
                Intent nullIntent = new Intent(EventManager.ACTION_NULLMUSIC);
                nullIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(nullIntent);

                return;
            }
            boolean flag = false;
            for (int i = 0; i < playList.size(); i++) {
                AudioInfo temp = playList.get(i);
                if (temp.getHash().equals(playInfoHashID)) {
                    flag = true;

                    //发送init的广播
                    PlaybackInfo playbackInfo = new PlaybackInfo();
                    playbackInfo.setAudioInfo(temp);

                    mPrefMgr.setPlaybackInfo(playbackInfo);
                    mPrefMgr.setCurrentAudio(temp);

                    Intent initIntent = new Intent(EventManager.ACTION_INITMUSIC);
                    initIntent.putExtra(PlaybackInfo.KEY, playbackInfo);
                    initIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mContext.sendBroadcast(initIntent);
                }
            }
            if (!flag) {
                resetData();
            }
        } else {
            resetData();
            //发送空数据广播
            Intent nullIntent = new Intent(EventManager.ACTION_NULLMUSIC);
            nullIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            mContext.sendBroadcast(nullIntent);
        }
    }

    private void resetData() {
        //清空之前的播放数据
        mPrefMgr.setPlayStatus(Status.STOP);
        mPrefMgr.setCurrentAudioHash("-1");
        mPrefMgr.setCurrentPlaylist(null);
        mPrefMgr.setCurrentAudio(null);
        mPrefMgr.setPlaybackInfo(null);
    }

}
