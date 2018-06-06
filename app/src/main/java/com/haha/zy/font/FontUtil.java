package com.haha.zy.font;

import android.content.Context;
import android.graphics.Typeface;

/**
 * @Description: 加载字体
 * @Author: Terrence Zhao
 * @Date: 14/05/2018
 */

public class FontUtil {

    // 字体
    private static Typeface sTypeFace;

    private static FontUtil sInstance;

    public FontUtil(Context context) {
        sTypeFace = Typeface.createFromAsset(context.getAssets(),
                "fonts/iconfont.ttf");
    }

    public static FontUtil getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new FontUtil(context);
        }
        return sInstance;
    }

    public Typeface getTypeFace() {
        return sTypeFace;
    }
}
