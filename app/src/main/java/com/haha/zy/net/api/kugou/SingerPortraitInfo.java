package com.haha.zy.net.api.kugou;

import android.support.annotation.NonNull;

/**
 * 歌手写真信息
 */

public class SingerPortraitInfo implements Comparable<SingerPortraitInfo> {

    public final String mSingerName;
    public final String mPortraitUrl;

    public SingerPortraitInfo(@NonNull String singerName, String portraitUrl) {
        mSingerName = singerName;
        mPortraitUrl = portraitUrl;
    }

    @Override
    public int compareTo(@NonNull SingerPortraitInfo o) {
        return mPortraitUrl.compareTo(o.mPortraitUrl);
    }
}
