package com.haha.zy.storage;

import android.os.Environment;

import java.io.Serializable;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 31/05/2018
 */

public class StorageVolumeInfo implements Serializable {

    public String mPath;

    public String mState;

    public boolean mRemovable;

    public StorageVolumeInfo(String path) {
        mPath = path;
    }

    public boolean isMounted() {
        return Environment.MEDIA_MOUNTED.equals(mState);
    }

    @Override
    public String toString() {
        return "StorageVolumeInfo{" +
                "path='" + mPath + '\'' +
                ", state='" + mState + '\'' +
                ", removable=" + mRemovable +
                '}';
    }
}
