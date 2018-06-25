package com.haha.zy.util;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.haha.zy.ZYApplication;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 31/05/2018
 */

public class FileUtil {

    /**
     * 歌曲目录
     */
    public final static String PATH_AUDIO = "zymusic" + File.separator + "download";

    /**
     * 序列化对象保存路径
     */
    public final static String PATH_CACHE_SERIALIZABLE = "serializable";

    /**
     * 歌词目录
     */
    public final static String PATH_LYRICS = "lyrics";

    private static final long SIZE_KB = 1024L;
    private static final long SIZE_MB = 1024L * SIZE_KB;
    private static final long SIZE_GB = 1024L * SIZE_MB;

    private static final String FLAG_BYTE = "B";
    private static final String FLAG_K_BYTE = "K";
    private static final String FLAG_M_BYTE = "M";
    private static final String FLAG_G_BYTE = "G";

    private static final char DOT_CHAR = '.';
    private static final String EMPTY_STRING = "";
    private static final String TRACK_TIME_SEPARATOR = ":";

    public static String getFileSize(long fileSizeInByte) {// 转换文件大小
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileSizeInByte < SIZE_KB) {
            fileSizeString = df.format((double) fileSizeInByte) + FLAG_BYTE;
        } else if (fileSizeInByte < SIZE_MB) {
            fileSizeString = df.format((double) fileSizeInByte / SIZE_KB) + FLAG_K_BYTE;
        } else if (fileSizeInByte < SIZE_GB) {
            fileSizeString = df.format((double) fileSizeInByte / SIZE_MB) + FLAG_M_BYTE;
        } else {
            fileSizeString = df.format((double) fileSizeInByte / SIZE_GB) + FLAG_G_BYTE;
        }
        return fileSizeString;
    }

    /**
     * 整数时间转换成字符串
     * @param time 时间值，毫秒单位
     * @return mm:ss 格式时间，如 3：15
     */
    public static String parseTime2TrackLength(long time) {
        long timeInSecond = time / 1000;
        int minute = (int)(timeInSecond / 60);
        int second = (int)(timeInSecond % 60);
        return String.format("%02d" + TRACK_TIME_SEPARATOR + "%02d", minute, second);
    }

    /**
     * 获取歌曲时间长度值
     * @param trackLengthString 音频文件长度，mm:ss 格式，如 3：15（3分15秒）
     * @return 音频文件时间长度数值，毫秒单位
     */
    private static long getTimeFromTrackLength(String trackLengthString) {
        if (trackLengthString.contains(TRACK_TIME_SEPARATOR)) {
            String temp[] = trackLengthString.split(TRACK_TIME_SEPARATOR);
            if (temp.length == 2) {
                int m = Integer.parseInt(temp[0]);// 分
                int s = Integer.parseInt(temp[1]);// 秒
                int currTime = (m * 60 + s) * 1000;
                return currTime;
            }
        }
        return 0;
    }

    /**
     * 获取文件后缀名
     */
    public static String getFileExt(String filePath) {
        int pos = filePath.lastIndexOf(DOT_CHAR);
        return (pos == -1) ? EMPTY_STRING : filePath.substring(pos + 1).toLowerCase();
    }

    /**
     * 获取不带后缀名的文件名
     */
    public static String getFileNameWithoutExt(String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            int dot = fileName.lastIndexOf('.');
            if ((dot > -1) && (dot < (fileName.length()))) {
                return fileName.substring(0, dot);
            }
        }
        return fileName;
    }

    public static String getFilePath(Context context, String basePath, String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            fileName = "";
        }

        if (TextUtils.isEmpty(basePath)){
            basePath = getDiskCacheDir(context);
        }

        String filePath = basePath + File.separator + fileName;

        File file = new File(filePath);
        if(!fileName.equals("")){
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
        }else{
            if(!file.exists()){
                file.mkdirs();
            }
        }

        return filePath;
    }

    public static String getAppPublicDirectory(Context context, @Nullable String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            fileName = ZYApplication.APP_NAME;
        }

        String publicAppPath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            publicAppPath = Environment.getExternalStoragePublicDirectory(fileName).getPath();
        } else {
            publicAppPath = context.getFilesDir().getPath();
        }
        return publicAppPath;

    }

    public static String getDiskCacheDir(Context context) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }
}
