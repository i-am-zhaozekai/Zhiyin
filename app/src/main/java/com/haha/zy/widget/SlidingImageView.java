package com.haha.zy.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;


/**
 * 具有背景图片轮询播放效果的 ImageView
 */
public class SlidingImageView extends AppCompatImageView {

    private final List<Drawable> mBgDrawables = new ArrayList<>();
    private int mCurrentIndex = -1;
    private int mDuration = 500;

    private final Handler mUIHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            int bgImageCount = mBgDrawables.size();
            if (bgImageCount <= 0) {
                mUIHandler.removeMessages(0);
                return;
            }

            if (bgImageCount == 1) {
                setBackground(mBgDrawables.get(0));
                mUIHandler.removeMessages(0);
                return;
            }

            mCurrentIndex++;

            Drawable firstLayer = mBgDrawables.get(mCurrentIndex % bgImageCount);
            Drawable secondLayer = mBgDrawables.get((mCurrentIndex + 1) % bgImageCount);
            TransitionDrawable backgroundDrawable = new TransitionDrawable(
                    new Drawable[] {firstLayer, secondLayer});
            setBackground(backgroundDrawable);

            backgroundDrawable.startTransition(mDuration);

            mUIHandler.removeMessages(0);
            mUIHandler.sendEmptyMessageDelayed(0, 5000L);
        }
    };

    @UiThread
    public void startBackgroundImageSliding(@NonNull Drawable[] drawables, long delayMillis) {
        mBgDrawables.clear();

        for (Drawable drawable : drawables) {
            if (drawable != null){
                mBgDrawables.add(drawable);
            }
        }

        mUIHandler.removeMessages(0);
        mUIHandler.sendEmptyMessageDelayed(0, delayMillis);
    }

    @UiThread
    public void stopBackgroundImageSliding() {
        mUIHandler.removeMessages(0);
    }

    public SlidingImageView(Context context) {
        this(context, null);
    }

    public SlidingImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        stopBackgroundImageSliding();
    }
}
