package com.haha.zy.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.haha.zy.font.FontUtil;


public class IconFontImageButtonTextView extends AppCompatTextView {

    private float oldTextSize = -1f;
    /**
     * 是否倒置
     */
    private boolean convert = false;

    public IconFontImageButtonTextView(Context context) {
        super(context);
        init(context);
    }

    public IconFontImageButtonTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IconFontImageButtonTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // 设置字体图片
        Typeface iconfont = FontUtil.getInstance(context).getTypeFace();
        setTypeface(iconfont);
        setClickable(true);
    }


    public void setConvert(boolean convert) {
        this.convert = convert;
        setPressed(false);
    }

    @Override
    public void setPressed(boolean pressed) {
        int color = getCurrentTextColor();
        TextPaint paint = getPaint();

        if (oldTextSize == -1) {
            oldTextSize = getTextSize();
        }

        boolean isPressed = pressed;
        //如果倒置为true
        if (convert) {
            isPressed = !pressed;
        }

        if (isPressed) {
            int pressedColor = ColorUtils.setAlphaComponent(color, 240);
            setTextColor(pressedColor);
            paint.setFakeBoldText(true);
            paint.setTextSize(oldTextSize + 5);
        } else {
            int defColor = ColorUtils.setAlphaComponent(color, 150);
            setTextColor(defColor);
            paint.setFakeBoldText(false);
            paint.setTextSize(oldTextSize);
        }

        super.setPressed(pressed);
    }


}
