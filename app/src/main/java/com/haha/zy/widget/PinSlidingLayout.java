package com.haha.zy.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.haha.zy.R;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 30/05/2018
 */

public class PinSlidingLayout extends LinearLayout{

    private Context mContext;

    private Scroller mScroller;

    private View mPrimaryView;
    private View mSecondaryView;
    private View mPinView;

    private boolean isDraggingMode;

    /**
     * 判断是点击还是移动的距离
     */
    private int mTouchSlop;

    /**
     * 用于判定是否是flying动作
     */
    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private int mMinimumVelocity;

    /**
     * 触摸最后一次的x坐标
     */
    private float mLastX;
    /**
     * 触摸第一次的x坐标
     */
    private float mFirstX;

    private int mDuration = 800;

    public PinSlidingLayout(Context context) {
        this(context, null);
    }

    public PinSlidingLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PinSlidingLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
        mScroller = new Scroller(mContext);

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        setOrientation(LinearLayout.HORIZONTAL);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (mPrimaryView == null){
            mPrimaryView = findViewById(R.id.primary_item);
        }

        if (mSecondaryView == null) {
            mSecondaryView = findViewById(R.id.secondary_item);
        }

        if (mPinView == null) {
            mPinView = mPrimaryView.findViewById(R.id.pin_item);
        }

        int width = getWidth();
        int pinWidth = mPinView.getWidth();

        LayoutParams secondaryLayoutParams = new LayoutParams(width - pinWidth, LayoutParams.MATCH_PARENT);
        secondaryLayoutParams.leftMargin = -(width - pinWidth);
        // 重新 layout 使 secondaryView 被隐藏
        mSecondaryView.setLayoutParams(secondaryLayoutParams);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int actionId = event.getAction();
        if (actionId == MotionEvent.ACTION_DOWN) {

            int[] location = new int[2];
            mPinView.getLocationOnScreen(location);
            int mPinViewLeft = location[0];
            int mPinViewRight = mPinViewLeft + mPinView.getWidth();

            //按下焦点在 pinView 里面则自己处理 touchEvent
            if (/*isMenuViewShow() ||*/ (mPinViewLeft <= event.getRawX() && event.getRawX() <= mPinViewRight)) {
                isDraggingMode = true;
            }
        }
        return isDraggingMode;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        try {
            obtainVelocityTracker(event);
            int actionId = event.getAction();
            switch (actionId) {
                case MotionEvent.ACTION_DOWN:

                    mFirstX = event.getX();
                    mLastX = mFirstX;
                    if (!mScroller.isFinished()) {
                        mScroller.abortAnimation();
                    }

                case MotionEvent.ACTION_MOVE:
                    float curX = event.getX();
                    int deltaX = (int) (mLastX - curX);
                    if (isDraggingMode) {

                        if (!mScroller.isFinished()) {
                            mScroller.abortAnimation();
                        }
                        mScroller.startScroll(mScroller.getFinalX(), 0, deltaX, 0, 0);
                        invalidate();
                    }
                    mLastX = curX;
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    int tDeltaX = (int) (mLastX - mFirstX);
                    mLastX = 0;
                    mFirstX = 0;

                    if (Math.abs(tDeltaX) < mTouchSlop) {
                            if (mOnClickListener != null) {
                                mOnClickListener.onClick(this);
                            }
                    }

                    if (!isDraggingMode) {
                        break;
                    }

                    isDraggingMode = false;

                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int xVelocity = (int) velocityTracker.getXVelocity();

                    if (Math.abs(xVelocity) > mMinimumVelocity) {
                        int scrollDeltaX = -1;
                        int scrollX = mScroller.getFinalX();

                        // 如果是 flying 滑动动作，则不管滑动到哪里抬起手指，都需要把滑动动作做完，
                        // 此时我们只需要判定flying速度的方向即可
                        if (xVelocity < 0f) {
                            scrollDeltaX = 0 - scrollX;
                        } else {
                            scrollDeltaX = -mSecondaryView.getWidth() - scrollX;
                        }
                        if (scrollDeltaX != -1) {
                            if (!mScroller.isFinished()) {
                                mScroller.abortAnimation();
                            }
                            mScroller.startScroll(mScroller.getFinalX(), 0, scrollDeltaX, 0, mDuration);
                            invalidate();
                        }

                    } else {
                        int scrollX = mScroller.getFinalX();
                        int scrollDeltaX = -1;

                        // 对于非flying的滑动动作，我们需要根据ActionUp时的位置来判定是要继续往左/右滑还是收缩回去
                        if (scrollX <= -mSecondaryView.getWidth()) {
                            //右越界
                            scrollDeltaX = -(mSecondaryView.getWidth()) - scrollX;
                        } else if (scrollX > 0) {
                            //左越界
                            scrollDeltaX = 0 - scrollX;

                        } else if (scrollX <= -getWidth() / 2) {
                            //已移动到右边
                            scrollDeltaX = -mSecondaryView.getWidth() - scrollX;
                        } else {
                            //已移动到左边
                            scrollDeltaX = 0 - scrollX;
                        }

                        if (scrollDeltaX != -1) {
                            if (!mScroller.isFinished()) {
                                mScroller.abortAnimation();
                            }
                            mScroller.startScroll(mScroller.getFinalX(), 0, scrollDeltaX, 0, mDuration);
                            invalidate();
                        }
                    }

                    // 记得释放 VelocityTracker
                    releaseVelocityTracker();
                    break;
                default:

            }
        } catch (Exception e) {
        }
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        // 更新当前的X轴偏移量
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            invalidate();
        } else {
            int scrollX = mScroller.getFinalX();
            if (scrollX <= -mSecondaryView.getWidth()) {
                /*mCurrentView = mMenuView;
                if (playerBarOnCloseListener != null) {
                    playerBarOnCloseListener.onOpen();
                }*/
            } else if (scrollX >= 0) {
                /*mCurrentView = mContentView;
                if (playerBarOnCloseListener != null) {
                    playerBarOnCloseListener.onClose();
                }*/
            }
        }
    }

    private void obtainVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }

        mVelocityTracker.addMovement(event);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private OnClickListener mOnClickListener = null;

    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    public interface OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        void onClick(View v);
    }
}
