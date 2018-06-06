package com.haha.zy.pinyin;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 01/06/2018
 */

public class PinyinConverter {

    private HanyuPinyinOutputFormat mFormat = null;

    public PinyinConverter() {
        mFormat = new HanyuPinyinOutputFormat();
        /*
         * 设置需要转换的拼音格式
         * 以天为例
         * HanyuPinyinToneType.WITHOUT_TONE 转换为tian
         * HanyuPinyinToneType.WITH_TONE_MARK 转换为tian1
         * HanyuPinyinVCharType.WITH_U_UNICODE 转换为tiān
         *
         */
        mFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        mFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        mFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
    }

    /**
     * 对单个字进行转换
     *
     * @param word 需转换的汉字字符串
     * @return 拼音字符串数组
     */
    public String getPinyin4SingleHanzi(char word) {
        String[] pinyin = null;
        try {
            pinyin = PinyinHelper.toHanyuPinyinStringArray(word, mFormat);
        } catch (BadHanyuPinyinOutputFormatCombination e) {
        }

        //pinyin4j规则，当转换的符串不是汉字，就返回null
        if (pinyin == null) {
            return null;
        }

        //多音字会返回一个多音字拼音的数组，pinyin4j并不能有效判断该字的读音
        return pinyin[0];
    }

    /**
     * 对单个字进行转换
     *
     * @param words
     * @return
     */
    public String getPinyin(String words) {
        /*words = words.trim();

        StringBuffer sb = new StringBuffer();
        String tempStr = null;
        //循环字符串
        for (int i = 0; i < words.length(); i++) {

            tempStr = getPinyin4SingleHanzi(words.charAt(i));
            if (tempStr == null) {
                //非汉字直接拼接
                sb.append(words.charAt(i));
            } else {
                sb.append(tempStr);
            }
        }

        return sb.toString();*/


        char[] input = words.trim().toCharArray();
        String output = "";

        try {
            for (int i = 0; i < input.length; i++) {
                if (Character.toString(input[i]).matches(
                        "[\\u4E00-\\u9FA5]+")) {
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(
                            input[i], mFormat);
                    output += temp[0];
                } else
                    output += Character.toString(input[i]);
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        return output;
    }
}
