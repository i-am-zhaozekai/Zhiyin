package com.haha.zy.lyric;

import com.haha.zy.lyric.model.LyricsInfo;

import java.nio.charset.Charset;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 06/06/2018
 */

public interface LyricWriter {

    /**
     * 默认编码
     */
    Charset defaultCharset = Charset.forName("utf-8");

    /**
     * 支持文件格式
     *
     * @param ext
     *            文件后缀名
     * @return
     */
    public abstract boolean isFileSupported(String ext);

    /**
     * 获取支持的文件后缀名
     *
     * @return
     */
    public abstract String getSupportFileExt();

    /**
     * 保存歌词文件
     *
     * @param lyricsIfno
     *            歌词数据
     * @param lyricsFilePath
     *            歌词文件路径
     */
    public abstract boolean writer(LyricsInfo lyricsIfno, String lyricsFilePath)
            throws Exception;

}
