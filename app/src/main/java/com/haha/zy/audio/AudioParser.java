package com.haha.zy.audio;

import android.text.TextUtils;
import android.util.Log;

import com.haha.zy.audio.reader.AudioFileReader;
import com.haha.zy.util.DateUtil;
import com.haha.zy.util.FileUtil;
import com.haha.zy.util.MD5Util;

import java.io.File;
import java.util.Date;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 31/05/2018
 */

public class AudioParser {

    private String mArtistUnknown = "未知歌手";
    private String mAudioNameUnknown = "未知歌曲";

    private Filter mFilter = DUMMY_FILTER;

    private static final Filter DUMMY_FILTER = new Filter() {
        @Override
        public boolean filterWithFileInfo(File audioFile, String hash) {
            return false;
        }

        @Override
        public boolean filterWithTrackInfo(TrackInfo trackInfo) {
            return false;
        }
    };

    public AudioParser() {}

    public AudioParser(String audioNameUnknown, String artistUnknown) {
        mArtistUnknown = artistUnknown;
        mAudioNameUnknown = audioNameUnknown;
    }

    public void setFilter(Filter filter) {
        mFilter = filter;
    }

    public AudioInfo parse(File audioFile) {
        if (!audioFile.isFile()) {
            return null;
        }

        String hash = MD5Util.getFileMd5(audioFile).toLowerCase();

        if (mFilter != null && mFilter.filterWithFileInfo(audioFile, hash)) {
            return null;
        }

        String fileName = FileUtil.getFileNameWithoutExt(audioFile.getName());
        String audioName = fileName;
        String artistName = null;
        if (fileName.contains("-")) {
            int splitIndex = fileName.lastIndexOf("-");
            artistName = fileName.substring(0, splitIndex).trim();
            audioName = fileName.substring(splitIndex+1, fileName.length()).trim();
        }

        if (TextUtils.isEmpty(audioName)) {
            audioName = mAudioNameUnknown;
        }
        if (TextUtils.isEmpty(artistName)) {
            artistName = mArtistUnknown;
        }

        String filePath = audioFile.getPath();
        String fileExt = FileUtil.getFileExt(filePath);

        AudioFileReader audioFileReader = AudioReaderManager.getInstance().getAudioFileReaderByFilePath(filePath);
        if (audioFileReader == null) {
            return null;
        }

        TrackInfo trackInfoData = audioFileReader.read(audioFile);
        if (trackInfoData == null) {
            return null;
        }

        if (mFilter != null && mFilter.filterWithTrackInfo(trackInfoData)) {
            return null;
        }

        long duration = trackInfoData.getDuration();
        String durationText = FileUtil.parseTime2TrackLength(duration);
        long fileSize = audioFile.length();
        String fileSizeText = FileUtil.getFileSize(fileSize);

        AudioInfo audioInfo = new AudioInfo();
        audioInfo.setCreateTime(DateUtil.parseDateToString(new Date()));
        audioInfo.setDuration(duration);
        audioInfo.setDurationText(durationText);
        audioInfo.setFileExt(fileExt);
        audioInfo.setFilePath(filePath);
        audioInfo.setFileSize(fileSize);
        audioInfo.setFileSizeText(fileSizeText);
        audioInfo.setHash(hash);
        audioInfo.setTitle(audioName);
        audioInfo.setArtist(artistName);
        audioInfo.setType(AudioInfo.LOCAL);
        audioInfo.setStatus(AudioInfo.FINISH);

        return audioInfo;
    }

    public interface Filter {
        /**
         * 根据文件信息过滤
         * @param audioFile
         * @param hash 文件 hash 值
         * @return 返回 true 文件将被过滤不解析
         */
        boolean filterWithFileInfo(File audioFile, String hash);

        /**
         * 根据文件的音频信息（采样率，时长等）过滤
         * @param trackInfo
         * @return 返回 true 文件将被过滤不解析
         */
        boolean filterWithTrackInfo(TrackInfo trackInfo);
    }
}
