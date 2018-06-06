package com.haha.zy.player;

import com.haha.zy.audio.AudioInfo;

import java.io.Serializable;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 02/06/2018
 */

public class PlaybackInfo implements Serializable {

    public static final String KEY = "com.haha.zy.player.playInfo";

    /**
     * 错误信息
     */
    private String mErrorMessage;
    /**
     * 播放进度
     */
    private long mProgress;
    /**
     * 音频信息
     */
    private AudioInfo mAudioInfo;
    /**
     *
     */
    private String mHash;

    public PlaybackInfo() {
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    public void setErrorMessage(String msg) {
        mErrorMessage = msg;
    }

    public long getProgress() {
        return mProgress;
    }

    public void setProgress(long progress) {
        mProgress = progress;
    }

    public AudioInfo getAudioInfo() {
        return mAudioInfo;
    }

    public void setAudioInfo(AudioInfo audio) {
        mAudioInfo = audio;
    }

    public String getHash() {
        return mHash;
    }

    public void setHash(String hash) {
        mHash = hash;
    }

    @Override
    public String toString() {
        return "PlaybackInfo{" +
                "mErrorMessage='" + mErrorMessage + '\'' +
                ", mProgress=" + mProgress +
                ", mAudioInfo=" + mAudioInfo +
                ", hash='" + mHash + '\'' +
                '}';
    }

}
