package com.haha.zy.util;

import android.content.Context;

import com.haha.zy.audio.AudioParser;
import com.haha.zy.audio.TrackInfo;
import com.haha.zy.audio.AudioInfo;
import com.haha.zy.db.DatabaseHelper;
import com.haha.zy.storage.StorageManagerReflection;
import com.haha.zy.storage.StorageVolumeInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MediaUtil {

    /*public static List<AudioInfo> scanLocalMusic(Context context) {
        List<AudioInfo> audioInfos = new ArrayList<>();

        AudioParser parser = new AudioParser();
        parser.setFilter(sFilter);

        List<StorageVolumeInfo> list = StorageManagerReflection.getStorageVolumes(context);
        for (int i = 0; i < list.size(); i++) {
            StorageVolumeInfo storageInfo = list.get(i);
            scanLocalAudioFile(storageInfo.mPath, parser, audioInfos);
        }

        return audioInfos;
    }

    public static void scanLocalAudioFile(String path, AudioParser parser, List<AudioInfo> audioInfos) {
        String filterFormats = "ape,flac,mp3,wav";
        File[] files = new File(path).listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                File temp = files[i];
                if (temp.isFile()) {

                    String fileName = temp.getName();
                    String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
                    if (filterFormats.indexOf(fileExt) == -1) {
                        continue;
                    }

                    AudioInfo audioInfo = parser.parse(temp);
                    if (audioInfo != null) {
                        audioInfos.add(audioInfo);
                    }

                } else if (temp.isDirectory() && temp.getPath().indexOf("/.") == -1) {
                    scanLocalAudioFile(temp.getPath(), parser, audioInfos);
                }
            }
        }

    }

    public static final AudioParser.Filter sFilter = new AudioParser.Filter() {

        @Override
        public boolean filterWithFileInfo(File audioFile, String hash) {
            if (audioFile.length() < 1024 * 1024) {
                return true;
            }

            if (DatabaseHelper.getInstance(ZYApplication.getInstance()).isExists(hash)) {
                return true;
            }

            return false;
        }

        @Override
        public boolean filterWithTrackInfo(TrackInfo trackInfo) {
            return trackInfo.getDuration() < 5000L;
        }
    };*/
}
