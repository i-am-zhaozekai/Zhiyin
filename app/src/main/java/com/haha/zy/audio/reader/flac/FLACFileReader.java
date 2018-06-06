package com.haha.zy.audio.reader.flac;

import com.haha.zy.audio.reader.AudioFileReader;
import com.haha.zy.audio.TrackInfo;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.flac.FlacFileReader;
import org.jaudiotagger.audio.generic.GenericAudioHeader;

public class FLACFileReader extends AudioFileReader {
    public TrackInfo readSingle(TrackInfo trackInfo) {
        try {
            FlacFileReader reader = new FlacFileReader();
            AudioFile af1 = reader.read(trackInfo.getFile());

            GenericAudioHeader audioHeader = (GenericAudioHeader) af1
                    .getAudioHeader();
            copyHeaderFields(audioHeader, trackInfo);
        } catch (Exception e) {
            System.out.println("Couldn't read file: " + trackInfo.getFile());
        }
        return trackInfo;
    }

    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("flac");
    }
}
