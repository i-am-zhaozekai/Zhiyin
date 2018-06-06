package com.haha.zy.constant;

import java.io.File;

public class ResourceConstants {

    public final static String PATH_TEMP = "zy.data";

    /**
     * 歌曲目录
     */
    public final static String PATH_AUDIO = PATH_TEMP + File.separator + "audio";

    /**
     * 序列化对象保存路径
     */
    public final static String PATH_CACHE_SERIALIZABLE = PATH_TEMP + File.separator
            + "cache" + File.separator + "serializable";

}
