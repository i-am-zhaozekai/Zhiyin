package com.haha.zy.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.haha.zy.R;

/**
 * 带有一个圆环背景的 RelativeLayout
 */
public class RingBackgroundLayout extends RelativeLayout {

	private boolean isLoadColor = false;
	private boolean isSelect = false;
	private boolean isPressed = false;

	public RingBackgroundLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	public RingBackgroundLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RingBackgroundLayout(Context context) {
		this(context, null);
	}

	private int mFillColor = Color.TRANSPARENT;
	private int mFillColorSelected = Color.TRANSPARENT;

	private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RingBackgroundLayout, defStyle, 0);

        mFillColor = a.getColor(R.styleable.RingBackgroundLayout_fillColor, Color.TRANSPARENT);
        mFillColorSelected = a.getColor(R.styleable.RingBackgroundLayout_fillColorSelected, Color.TRANSPARENT);

        a.recycle();
    }

	@Override
	protected void dispatchDraw(Canvas canvas) {

		if (!isLoadColor) {
			int fillColor = mFillColor;
			if (isPressed || isSelect) {
				fillColor = ColorUtils.setAlphaComponent(fillColor, 60);
			} else {
			}

			LayerDrawable bg = (LayerDrawable) getBackground();
            GradientDrawable outterLayer = (GradientDrawable) bg.getDrawable(0);
            outterLayer.setColor(fillColor);
            GradientDrawable innerLayer = (GradientDrawable) bg.getDrawable(1);
            innerLayer.setColor(fillColor);


			/*GradientDrawable gd = new GradientDrawable();// 创建drawable
			gd.setColor(fillColor);
			gd.setStroke(strokeWidth, strokeColor);
			gd.setShape(GradientDrawable.OVAL);

			setBackgroundDrawable(bgDrawable);*/

			isLoadColor = true;
		}
		super.dispatchDraw(canvas);
	}

	@Override
	public void setPressed(boolean pressed) {
		isLoadColor = false;
		isPressed = pressed;
		invalidate();
		super.setPressed(pressed);
	}

	public void setSelect(boolean select) {
		isLoadColor = false;
		isSelect = select;
		invalidate();
	}

	public boolean isSelect() {
		return isSelect;
	}
}
