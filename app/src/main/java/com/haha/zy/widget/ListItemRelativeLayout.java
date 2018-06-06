package com.haha.zy.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * listview item
 */
public class ListItemRelativeLayout extends RelativeLayout {

    private int defColor;
    private int pressColor;

    private boolean isPressed = false;
    private boolean isLoadColor = false;


    public ListItemRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public ListItemRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ListItemRelativeLayout(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        defColor = ColorUtils.setAlphaComponent(0xFFFFFF, 255);
        pressColor = ColorUtils.setAlphaComponent(0xe1e1e1, 200);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (!isLoadColor) {

            if (isPressed) {
                setBackgroundColor(pressColor);
            } else {

                setBackgroundColor(defColor);

            }
            isLoadColor = true;
        }
        super.dispatchDraw(canvas);
    }

    public void setPressed(boolean pressed) {
        isLoadColor = false;
        isPressed = pressed;
        invalidate();
        super.setPressed(pressed);
    }

}
