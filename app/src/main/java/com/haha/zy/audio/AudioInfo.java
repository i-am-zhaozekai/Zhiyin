package com.haha.zy.audio;

import java.io.Serializable;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 31/05/2018
 */

public class AudioInfo implements Serializable {

    public static final String KEY = "com.haha.zy.ai.key";

    /**
     * 状态
     */
    public static final int FINISH = 0;
    public static final int DOWNLOADING = 1;
    public static final int INIT = 2;

    /**
     * 类型
     */
    public static final int LOCAL = 0;
    public static final int DOWNLOAD = 1;
    public static final int NET = 2;
    public static final int RECENT_LOCAL = 3; //最近-本地
    public static final int RECENT_NET = 4; //最近-网络
    public static final int LIKE_LOCAL = 5; //喜欢-本地
    public static final int LIKE_NET = 6; //喜欢-网络

    /**
     * 歌曲名称
     */
    private String mTitle;
    /**
     * 歌手名称
     */
    private String mArtist;
    /**
     *
     */
    private String hash;
    /**
     * 歌曲后缀名
     */
    private String fileExt;
    /**
     * 文件大小
     */
    private long fileSize;
    private String fileSizeText;
    /**
     * 文件路径
     */
    private String filePath;
    /**
     * 时长
     */
    private long duration;
    private String durationText;

    /**
     * 文件下载路径
     */
    private String downloadUrl;

    /**
     * 添加时间
     */
    private String createTime;
    /**
     * 状态：0是完成，1是未完成
     */
    private int mStatus = FINISH;

    /**
     * 类型
     */
    private int type = LOCAL;
    /**
     * 分类索引
     */
    private String category;
    private String childCategory;


    public AudioInfo() {

    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String artist) {
        this.mArtist = artist;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileSizeText() {
        return fileSizeText;
    }

    public void setFileSizeText(String fileSizeText) {
        this.fileSizeText = fileSizeText;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getDurationText() {
        return durationText;
    }

    public void setDurationText(String durationText) {
        this.durationText = durationText;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        this.mStatus = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getChildCategory() {
        return childCategory;
    }

    public void setChildCategory(String childCategory) {
        this.childCategory = childCategory;
    }

    @Override
    public String toString() {
        return "AudioInfo{" +
                "mTitle='" + mTitle + '\'' +
                ", mArtist='" + mArtist + '\'' +
                ", hash='" + hash + '\'' +
                ", fileExt='" + fileExt + '\'' +
                ", fileSize=" + fileSize +
                ", fileSizeText='" + fileSizeText + '\'' +
                ", filePath='" + filePath + '\'' +
                ", duration=" + duration +
                ", durationText='" + durationText + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", createTime='" + createTime + '\'' +
                ", mStatus=" + mStatus +
                ", type=" + type +
                ", category='" + category + '\'' +
                ", childCategory='" + childCategory + '\'' +
                '}';
    }
}
