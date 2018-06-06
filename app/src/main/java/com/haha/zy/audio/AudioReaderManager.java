package com.haha.zy.audio;

import com.haha.zy.audio.reader.AudioFileReader;
import com.haha.zy.audio.reader.ape.APEFileReader;
import com.haha.zy.audio.reader.flac.FLACFileReader;
import com.haha.zy.audio.reader.mp3.MP3FileReader;
import com.haha.zy.audio.reader.wav.WAVFileReader;
import com.haha.zy.util.FileUtil;

import java.util.ArrayList;
import java.util.List;

public class AudioReaderManager {

    private static volatile AudioReaderManager sInstance = null;

    private List<AudioFileReader> mReaders = null;

    private AudioReaderManager() {
        mReaders = new ArrayList<>();

        mReaders.add(new MP3FileReader());
        mReaders.add(new APEFileReader());
        mReaders.add(new FLACFileReader());
        mReaders.add(new WAVFileReader());
    }

    public static AudioReaderManager getInstance() {
        if (sInstance == null) {
            synchronized (AudioReaderManager.class) {
                if (sInstance == null) {
                    sInstance = new AudioReaderManager();
                }
            }
        }

        return sInstance;
    }

    public AudioFileReader getAudioFileReaderByFilePath(String filePath) {
        String ext = FileUtil.getFileExt(filePath);
        for (AudioFileReader reader : mReaders) {
            if (reader.isFileSupported(ext)) {
                return reader;
            }
        }
        return null;
    }

    public AudioFileReader getAudioFileReaderByFileExt(String fileExt) {
        fileExt = fileExt.toLowerCase();
        for (AudioFileReader reader : mReaders) {
            if (reader.isFileSupported(fileExt)) {
                return reader;
            }
        }
        return null;
    }
}
