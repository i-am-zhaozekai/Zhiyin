package com.haha.zy.player;

import android.content.Context;

import com.haha.zy.R;
import com.haha.zy.audio.AudioInfo;
import com.haha.zy.preference.PreferenceManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 05/06/2018
 */

public abstract class PlayMode {

    /**
     * 顺序播放
     */
    public static final int ORDER = 0;
    /**
     * 随机播放
     */
    public static final int SHUFFLE = 1;
    /**
     * 循环播放
     */
    public static final int REPEAT_ALL = 2;
    /**
     * 单曲循环
     */
    public static final int REPEAT_ONCE = 3;


    public AudioInfo getPreviousSong(Context context) {
        PreferenceManager prefMgr = PreferenceManager.getInstance(context);
        AudioInfo currentAudio = prefMgr.getCurrentAudio();
        PlaybackInfo playbackInfo = prefMgr.getPlaybackInfo();
        List<AudioInfo> currentPlaylist = prefMgr.getCurrentPlaylist();

        if (currentAudio == null || playbackInfo == null || currentPlaylist == null) {
            return null;
        }

        int playIndex = -1;
        for (int i = 0; i < currentPlaylist.size(); i++) {
            AudioInfo audioInfo = currentPlaylist.get(i);
            if (audioInfo.getHash().equals(prefMgr.getCurrentAudioHash())) {
                playIndex = i;
                break;
            }
        }

        if (playIndex == -1) {
            return null;
        }

        return getPreviousSong(playIndex, currentPlaylist);
    }

    public AudioInfo getNextSong(Context context) {
        PreferenceManager prefMgr = PreferenceManager.getInstance(context);
        AudioInfo currentAudio = prefMgr.getCurrentAudio();
        PlaybackInfo playbackInfo = prefMgr.getPlaybackInfo();
        List<AudioInfo> currentPlaylist = prefMgr.getCurrentPlaylist();

        if (currentAudio == null || playbackInfo == null || currentPlaylist == null) {
            return null;
        }

        int playIndex = -1;
        for (int i = 0; i < currentPlaylist.size(); i++) {
            AudioInfo audioInfo = currentPlaylist.get(i);
            if (audioInfo.getHash().equals(prefMgr.getCurrentAudioHash())) {
                playIndex = i;
                break;
            }
        }

        if (playIndex == -1) {
            return null;
        }

        return getNextSong(playIndex, currentPlaylist);
    }

    protected abstract AudioInfo getPreviousSong(int currentIndex, List<AudioInfo> playlist);
    protected abstract AudioInfo getNextSong(int currentIndex, List<AudioInfo> playlist);

    public abstract String getName(Context context);
    public abstract int getValue();

    public abstract int getIconResId(boolean lightStyle);
    public abstract int getNextModeValue();

    private static final class OrderMode extends PlayMode {

        @Override
        protected AudioInfo getPreviousSong(int currentIndex, List<AudioInfo> playlist) {
            int previousIndex = currentIndex - 1;
            int listSize = playlist.size();

            if ((listSize > 0) && (previousIndex >= 0) && (listSize > previousIndex)) {
                return playlist.get(previousIndex);
            }

            return null;
        }

        @Override
        protected AudioInfo getNextSong(int currentIndex, List<AudioInfo> playlist) {
            int nextIndex = currentIndex + 1;
            int listSize = playlist.size();

            if ((listSize > 0) && (nextIndex >= 0) && (listSize > nextIndex)) {
                return playlist.get(nextIndex);
            }

            return null;
        }

        @Override
        public String getName(Context context) {
            return context.getString(R.string.play_mode_sequence);
        }

        @Override
        public int getValue() {
            return ORDER;
        }

        @Override
        public int getIconResId(boolean lightStyle) {
            return lightStyle ? R.drawable.sequence_light : R.drawable.sequence_dark;
        }

        @Override
        public int getNextModeValue() {
            return SHUFFLE;
        }

    }

    private static final class ShuffleMode extends PlayMode {

        @Override
        protected AudioInfo getPreviousSong(int currentIndex, List<AudioInfo> playlist) {
            return getShuffleSong(currentIndex, playlist);
        }

        @Override
        protected AudioInfo getNextSong(int currentIndex, List<AudioInfo> playlist) {
            return getShuffleSong(currentIndex, playlist);
        }

        @Override
        public String getName(Context context) {
            return context.getString(R.string.play_mode_shuffle);
        }

        @Override
        public int getValue() {
            return SHUFFLE;
        }

        @Override
        public int getIconResId(boolean lightStyle) {
            return lightStyle ? R.drawable.shuffle_light: R.drawable.shuffle_dark;
        }

        @Override
        public int getNextModeValue() {
            return REPEAT_ALL;
        }


        private AudioInfo getShuffleSong(int currentIndex, List<AudioInfo> playlist) {
            int listSize = playlist.size();

            if (listSize > 0) {
                return playlist.get(new Random().nextInt(listSize));
            }

            return null;
        }
    }

    private static final class RepeatAllMode extends PlayMode {

        @Override
        protected AudioInfo getPreviousSong(int currentIndex, List<AudioInfo> playlist) {
            int previousIndex = currentIndex - 1;
            int listSize = playlist.size();

            if (previousIndex < 0) {
                previousIndex = 0;
            }

            if (previousIndex >= listSize) {
                previousIndex = 0;
            }

            if ((listSize > 0) && (previousIndex >= 0) && (listSize > previousIndex)) {
                return playlist.get(previousIndex);
            }

            return null;
        }

        @Override
        protected AudioInfo getNextSong(int currentIndex, List<AudioInfo> playlist) {
            int nextIndex = currentIndex + 1;
            int listSize = playlist.size();

            if (nextIndex < 0) {
                nextIndex = 0;
            }

            if (nextIndex >= listSize) {
                nextIndex = 0;
            }

            if ((listSize > 0) && (nextIndex >= 0) && (listSize > nextIndex)) {
                return playlist.get(nextIndex);
            }

            return null;
        }

        @Override
        public String getName(Context context) {
            return context.getString(R.string.play_mode_repeat);
        }

        @Override
        public int getValue() {
            return REPEAT_ALL;
        }

        @Override
        public int getIconResId(boolean lightStyle) {
            return lightStyle ? R.drawable.repeat_light : R.drawable.repeat_dark;
        }

        @Override
        public int getNextModeValue() {
            return REPEAT_ONCE;
        }
    }

    private static final class RepeatOneMode extends PlayMode {

        @Override
        protected AudioInfo getPreviousSong(int currentIndex, List<AudioInfo> playlist) {
            int listSize = playlist.size();

            if ((listSize > 0) && (currentIndex >= 0) && (listSize > currentIndex)) {
                return playlist.get(currentIndex);
            }

            return null;
        }

        @Override
        protected AudioInfo getNextSong(int currentIndex, List<AudioInfo> playlist) {
            int listSize = playlist.size();

            if ((listSize > 0) && (currentIndex >= 0) && (listSize > currentIndex)) {
                return playlist.get(currentIndex);
            }

            return null;
        }

        @Override
        public String getName(Context context) {
            return context.getString(R.string.play_mode_repeat_one);
        }

        @Override
        public int getValue() {
            return REPEAT_ONCE;
        }

        @Override
        public int getIconResId(boolean lightStyle) {

            return lightStyle ? R.drawable.repeat_one_light : R.drawable.repeat_one_dark;
        }

        @Override
        public int getNextModeValue() {
            return ORDER;
        }
    }

    private static final Map<Integer, PlayMode> sPlayModeMap = new HashMap<>();
    static {
        sPlayModeMap.put(ORDER, new PlayMode.OrderMode());
        sPlayModeMap.put(SHUFFLE, new PlayMode.ShuffleMode());
        sPlayModeMap.put(REPEAT_ALL, new PlayMode.RepeatAllMode());
        sPlayModeMap.put(REPEAT_ONCE, new RepeatOneMode());
    }

    public static PlayMode getMode(int mode) {
        return sPlayModeMap.get(mode);
    }
}
