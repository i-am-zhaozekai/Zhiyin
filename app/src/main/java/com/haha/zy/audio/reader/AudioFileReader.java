package com.haha.zy.audio.reader;


import com.haha.zy.audio.TrackInfo;
import com.haha.zy.util.FileUtil;

import org.jaudiotagger.audio.generic.GenericAudioHeader;

import java.io.File;

public abstract class AudioFileReader {

    public TrackInfo read(File file) {

        TrackInfo trackInfo = new TrackInfo();
        trackInfo.setFileSize(file.length());
        trackInfo.setFileSizeStr(FileUtil.getFileSize(file.length()));
        String filePath = file.getPath();
        trackInfo.setFilePath(filePath);
        trackInfo.setFileExt(FileUtil.getFileExt(filePath));
        return reload(trackInfo);
    }

    private TrackInfo reload(TrackInfo trackInfo) {
        TrackInfo res = readSingle(trackInfo);

        double totalMS = AudioFileReader.samplesToMillis(trackInfo.getTotalSamples(), trackInfo.getSampleRate());
        long duration = Math.round(totalMS);

        trackInfo.setDuration(duration);

        String durationStr = FileUtil.parseTime2TrackLength((int) duration);
        trackInfo.setDurationStr(durationStr);

        return res;
    }

    protected void copyHeaderFields(GenericAudioHeader header, TrackInfo trackInfo) {
        if ((header != null) && (trackInfo != null)) {
            trackInfo.setChannels(header.getChannelNumber());
            int frameSize = trackInfo.getChannels() * 2;
            trackInfo.setFrameSize(frameSize);
            trackInfo.setTotalSamples(header.getTotalSamples().longValue());
            trackInfo.setSampleRate(header.getSampleRateAsNumber());
            trackInfo.setPlayedProgress(0L);
            trackInfo.setCodec(header.getFormat());
            trackInfo.setBitrate((int) header.getBitRateAsNumber());
        }
    }

    protected abstract TrackInfo readSingle(TrackInfo paramTrackInfo);

    public abstract boolean isFileSupported(String paramString);


    //--------------------------音频采样率的一些工具方法-------------------------
    public static long bytesToSamples(long bytes, int frameSize) {
        return Math.round((float) bytes / frameSize);
    }

    public static long samplesToBytes(long samples, int frameSize) {
        return samples * frameSize;
    }

    public static double samplesToMillis(long samples, int sampleRate) {
        return Math.round((float) samples / sampleRate * 1000.0F);
    }

    public static double bytesToMillis(long bytes, int frameSize, int sampleRate) {
        long l = bytesToSamples(bytes, frameSize);
        return samplesToMillis(l, sampleRate);
    }

    public static int convertBuffer(byte[] input, int[] output, int len, int sampleSizeInBits) {
        int bps = sampleSizeInBits / 8;
        int target = 0;
        int i = 0;
        while (target < len) {
            switch (bps) {
                case 1:
                    output[(i++)] = input[(target++)];
                    break;
                case 2:
                    output[(i++)] = ((short) (input[(target++)] & 0xFF | input[(target++)] << 8));
                    break;
                case 3:
                    output[(i++)] =
                            (input[(target++)] & 0xFF | input[(target++)] << 8 & 0xFF00 | input[(target++)] << 16);
            }
        }
        return i;
    }

    public static long millisToSamples(long millis, int sampleRate) {
        return Math.round((float) millis / 1000.0F * sampleRate);
    }
}
