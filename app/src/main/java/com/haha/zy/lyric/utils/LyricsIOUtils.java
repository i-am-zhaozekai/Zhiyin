package com.haha.zy.lyric.utils;

import com.haha.zy.lyric.LyricReader;
import com.haha.zy.lyric.LyricWriter;
import com.haha.zy.lyric.krc.KrcLyricReader;
import com.haha.zy.lyric.krc.KrcLyricWriter;
import com.haha.zy.util.FileUtil;

import java.util.ArrayList;
import java.util.List;


public class LyricsIOUtils {
	private static List<LyricReader> readers;
	private static List<LyricWriter> writers;

	static {
		readers = new ArrayList<>();
		readers.add(new KrcLyricReader());

		writers = new ArrayList<>();
		writers.add(new KrcLyricWriter());
	}

	/**
	 * 获取支持的歌词文件格式
	 * 
	 * @return
	 */
	public static List<String> getSupportLyricExts() {
		List<String> lrcExts = new ArrayList<String>();
		for (LyricReader reader : readers) {
			lrcExts.add(reader.getSupportFileExt());
		}
		return lrcExts;
	}

	/**
	 * 获取歌词文件读取器
	 * 
	 * @param fileName
	 * @return
	 */
	public static LyricReader getLyricsFileReader(String fileName) {
		String ext = FileUtil.getFileExt(fileName);
		for (LyricReader reader : readers) {
			if (reader.isFileSupported(ext)) {
				return reader;
			}
		}
		return null;
	}

	/**
	 * 获取歌词保存器
	 * 
	 * @param fileName
	 * @return
	 */
	public static LyricWriter getLyricsFileWriter(String fileName) {
		String ext = FileUtil.getFileExt(fileName);
		for (LyricWriter writer : writers) {
			if (writer.isFileSupported(ext)) {
				return writer;
			}
		}
		return null;
	}
}
