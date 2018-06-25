package com.haha.zy.glide;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.load.Key;

import java.security.MessageDigest;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 19/06/2018
 */

public class SingerModel implements Key{

    /**
     * 歌手名字
     */
    private final String mSingerName;
    /**
     * 歌手头像URL
     */
    private String mPortraitUrl;

    private final ImageURLGetter<String, String> mPortraitUrlGetter;

    @Nullable private volatile byte[] cacheKeyBytes;

    public SingerModel(String name, ImageURLGetter<String, String> urlGetter) {
        mSingerName = name;
        mPortraitUrlGetter = urlGetter;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(getCacheKeyBytes());
    }

    @Override
    public int hashCode() {
        return mSingerName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !(o instanceof SingerModel)) {
            return false;
        }

        SingerModel other = (SingerModel) o;
        return mSingerName.equalsIgnoreCase(other.mSingerName);
    }

    @Override
    public String toString() {
        return getCacheKey();
    }

    public String getName() {
        return mSingerName;
    }


    public String getPortraitUrl() {
        return mPortraitUrl;
    }

    public void setPortraitUrl(String url) {
        mPortraitUrl = url;
    }

    public String getCacheKey() {
        return mSingerName != null ? mSingerName : "";
    }

    private byte[] getCacheKeyBytes() {
        if (cacheKeyBytes == null) {
            cacheKeyBytes = getCacheKey().getBytes(CHARSET);
        }
        return cacheKeyBytes;
    }

    public ImageURLGetter<String, String> getPortraitUrlGetter() {
        return mPortraitUrlGetter;
    }
}
