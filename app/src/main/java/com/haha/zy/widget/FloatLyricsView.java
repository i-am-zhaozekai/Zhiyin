package com.haha.zy.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.haha.zy.R;
import com.haha.zy.lyric.model.LyricsLineInfo;
import com.haha.zy.lyric.utils.LyricsUtil;

import java.util.TreeMap;

public class FloatLyricsView extends View {
    private Context mContext;
    /**
     * 默认提示文本
     */
    private String mDefText;

    /**
     * 默认歌词画笔
     */
    private Paint mPaint;
    /**
     * 高亮歌词画笔
     */
    private Paint mPaintHL;

    /**
     * 空行高度
     */
    private int mSpaceLineHeight = 0;
    /**
     * 歌词字体大小
     */
    private int mFontSize = 0;

    /**
     * 歌词解析
     */
    private LyricsUtil mLyricsUtil;

    /**
     * 歌词列表
     */
    private TreeMap<Integer, LyricsLineInfo> mLyricsLineTreeMap;
    /**
     * 左右间隔距离
     */
    private int mPaddingLeftOrRight = 10;

    /**
     * 当前歌词的所在行数
     */
    private int mLyricsLineNum = -1;

    /**
     * 当前歌词的第几个字
     */
    private int mLyricsWordIndex = -1;

    /**
     * 当前歌词第几个字 已经播放的长度
     */
    private float mLineLyricsHLWidth = 0;
    /**
     * 当前歌词第几个字 已经播放的时间
     */
    private float mLyricsWordHLTime = 0;

    public FloatLyricsView(Context context) {
        super(context);
    }

    public FloatLyricsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FloatLyricsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public FloatLyricsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context) {
        mDefText = context.getString(R.string.def_text);
        //默认画笔
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);

        //歌词高亮画笔
        mPaintHL = new Paint();
        mPaintHL.setDither(true);
        mPaintHL.setAntiAlias(true);

        //初始化画笔颜色
        initColor();
    }

    /**
     * 初始化画笔颜色
     */
    private void initColor() {
        mPaint.setColor(getContext().getResources().getColor(R.color.black_default));
        mPaintHL.setColor(getContext().getResources().getColor(R.color.blue_default));
    }

    /**
     * 初始化字体大小
     */
    private void initFontSize() {

        mSpaceLineHeight = getHeight() / 7;
        //字体大小
        mFontSize = 2 * mSpaceLineHeight;
        //
        mPaint.setTextSize(mFontSize);
        mPaintHL.setTextSize(mFontSize);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mFontSize == 0) {
            initFontSize();
        }
        if (mLyricsLineTreeMap == null || mLyricsLineTreeMap.size() == 0) {
            drawDefText(canvas);
        } else {
            drawLrcText(canvas);
        }
    }

    /**
     * 画默认字体
     *
     * @param canvas
     */
    private void drawDefText(Canvas canvas) {

        //
        float textWidth = getTextWidth(mPaint, mDefText);
        int textHeight = getTextHeight(mPaint);

        canvas.save();

        float leftX = (getWidth() - textWidth) / 2;
        float heightY = (getHeight() + textHeight) / 2;

        canvas.drawText(mDefText, leftX, heightY, mPaint);

        // 设置过渡的颜色和进度
        canvas.clipRect(leftX, heightY - getClipTextHeight(mPaint), leftX + textWidth / 2,
                heightY + getRealTextHeight(mPaint));

        canvas.drawText(mDefText, leftX, heightY, mPaintHL);
        canvas.restore();

    }

    /**
     * 画歌词
     *
     * @param canvas
     */
    private void drawLrcText(Canvas canvas) {
        // 先设置当前歌词，之后再根据索引判断是否放在左边还是右边
        LyricsLineInfo lyricsLineInfo = mLyricsLineTreeMap.get(mLyricsLineNum);
        // 当行歌词
        String curLyrics = lyricsLineInfo.getLineLyrics();
        float curLrcTextWidth = getTextWidth(mPaint, curLyrics);
        int curLrcTextHeight = getTextHeight(mPaint);
        if (mLyricsWordIndex != -1) {
            String lyricsWords[] = lyricsLineInfo.getLyricsWords();
            int wordsDisInterval[] = lyricsLineInfo
                    .getWordsDisInterval();
            // 当前歌词之前的歌词
            String lyricsBeforeWord = "";
            for (int i = 0; i < mLyricsWordIndex; i++) {
                lyricsBeforeWord += lyricsWords[i];
            }
            // 当前歌词字
            String lrcNowWord = lyricsWords[mLyricsWordIndex].trim();// 去掉空格
            // 当前歌词之前的歌词长度
            float lyricsBeforeWordWidth = mPaint
                    .measureText(lyricsBeforeWord);

            // 当前歌词长度
            float lyricsNowWordWidth = mPaint.measureText(lrcNowWord);

            float len = lyricsNowWordWidth
                    / wordsDisInterval[mLyricsWordIndex]
                    * mLyricsWordHLTime;
            mLineLyricsHLWidth = lyricsBeforeWordWidth + len;
        } else {
            // 整行歌词
            mLineLyricsHLWidth = curLrcTextWidth;
        }
        //
        // 当前歌词行的x坐标
        float textX = 0;

        // 当前歌词行的y坐标
        float textY = 0;
        if (mLyricsLineNum % 2 == 0) {

            textX = mPaddingLeftOrRight;
            textY = mSpaceLineHeight + getTextHeight(mPaint);

            // 画下一句的歌词
            if (mLyricsLineNum + 1 < mLyricsLineTreeMap.size()) {
                String lrcRightText = mLyricsLineTreeMap.get(
                        mLyricsLineNum + 1).getLineLyrics();
                float lrcRightTextWidth = mPaint
                        .measureText(lrcRightText);
                float textRightX = getWidth() - lrcRightTextWidth - mPaddingLeftOrRight;
                canvas.drawText(lrcRightText, textRightX,
                        (mSpaceLineHeight + getTextHeight(mPaint)) * 2 + mSpaceLineHeight, mPaint);
            }
        } else {

            textX = getWidth() - curLrcTextWidth - mPaddingLeftOrRight;
            textY = (mSpaceLineHeight + getTextHeight(mPaint)) * 2 + mSpaceLineHeight;

            // 画下一句的歌词
            if (mLyricsLineNum + 1 < mLyricsLineTreeMap.size()) {
                String lrcLeftText = mLyricsLineTreeMap.get(
                        mLyricsLineNum + 1).getLineLyrics();
                canvas.drawText(lrcLeftText, mPaddingLeftOrRight,
                        mSpaceLineHeight + getTextHeight(mPaint), mPaint);
            } else {
                //因为当前是最后一句了，这里画上一句
                String lrcLeftText = mLyricsLineTreeMap.get(
                        mLyricsLineNum - 1).getLineLyrics();
                canvas.drawText(lrcLeftText, mPaddingLeftOrRight,
                        mSpaceLineHeight + getTextHeight(mPaintHL), mPaintHL);
            }
        }
        // save和restore是为了剪切操作不影响画布的其它元素
        canvas.save();

        // 画当前歌词
        canvas.drawText(curLyrics, textX, textY, mPaint);
        canvas.clipRect(textX, textY - getClipTextHeight(mPaint), textX
                + mLineLyricsHLWidth, textY + getRealTextHeight(mPaint));
        canvas.drawText(curLyrics, textX, textY, mPaintHL);

        canvas.restore();
    }


    public LyricsUtil getLyricsUtil() {
        return mLyricsUtil;
    }

    public void setLyricsUtil(LyricsUtil mLyricsUtil) {
        this.mLyricsUtil = mLyricsUtil;
        if (mLyricsUtil != null && getWidth() != 0) {
            mLyricsLineTreeMap = mLyricsUtil.getReconstructLyrics(getWidth() / 4 * 3, mPaint);
        } else {
            mLyricsLineTreeMap = null;
        }
        resetData();
        invalidateView();
    }

    /**
     * 重置数据
     */
    private void resetData() {
        mLyricsLineNum = -1;
        mLyricsWordIndex = -1;
        mLyricsWordHLTime = 0;
        mLineLyricsHLWidth = 0;
    }
    ///////////////////////////////

    /**
     * 更新歌词
     */
    public void updateView(int playProgress) {
        if (mLyricsUtil == null || mLyricsLineTreeMap == null) return;

        int newLyricsLineNum = mLyricsUtil.getLineNumber(mLyricsLineTreeMap, playProgress);
        if (newLyricsLineNum != mLyricsLineNum) {
            mLyricsLineNum = newLyricsLineNum;
        }
        mLyricsWordIndex = mLyricsUtil.getDisWordsIndex(mLyricsLineTreeMap, mLyricsLineNum, playProgress);
        mLyricsWordHLTime = mLyricsUtil.getDisWordsIndexLenTime(mLyricsLineTreeMap, mLyricsLineNum, playProgress);

        invalidateView();
    }

    /**
     * 刷新View
     */
    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            //  当前线程是主UI线程，直接刷新。
            invalidate();
        } else {
            //  当前线程是非UI线程，post刷新。
            postInvalidate();
        }
    }

    /**
     * 获取行歌词高度。用于y轴位置计算
     *
     * @param paint
     * @return
     */
    private int getTextHeight(Paint paint) {
//        Rect rect = new Rect();
//        paint.getTextBounds(text, 0, text.length(), rect);
//        return rect.height();

        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) -(fm.ascent + fm.descent);
    }

    /**
     * 获取真实的歌词高度
     *
     * @param paint
     * @return
     */
    private int getRealTextHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) (-fm.leading - fm.ascent + fm.descent);
    }

    /**
     * 获取切割的高度.用于y轴位置计算
     *
     * @param paint
     * @return
     */
    private int getClipTextHeight(Paint paint) {
//        Paint.FontMetrics fm = paint.getFontMetrics();
//        return (int) (-fm.leading - fm.ascent);
        return getRealTextHeight(paint);
    }


    /**
     * 获取文本宽度
     *
     * @param paint
     * @param text
     * @return
     */
    private float getTextWidth(Paint paint, String text) {
        return paint.measureText(text);
    }

    ////////////////////

    public TreeMap<Integer, LyricsLineInfo> getLyricsLineTreeMap() {
        return mLyricsLineTreeMap;
    }
}
