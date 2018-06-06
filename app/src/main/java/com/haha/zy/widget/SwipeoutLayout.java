package com.haha.zy.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.haha.zy.R;


public class SwipeoutLayout extends LinearLayout {
    /**
     * 主内容界面
     */
    private ViewGroup mContentView;
    /**
     * 菜单界面
     */
    private ViewGroup mMenuView;
    /**
     * 可拖动的view
     */
    private View mDragView;
    /**
     * 当前显示的视图
     */
    private View mCurrentView;
    //
    private Context mContext;

    /**
     * 菜单相隔距离
     */
    private int mMenuPaddingRithtWidth;

    /**
     * 标记view的宽度
     */
    private int barFlagViewWidth;


    /**
     * 屏幕宽度
     */
    private int mScreensWidth;

    /**
     * 触摸最后一次的x坐标
     */
    private float mLastX;
    /**
     * 触摸第一次的x坐标
     */
    private float mFristX;

    /**
     *
     */
    private Scroller mScroller;

    private Interpolator sInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };


    private int mDuration = 800;

    /**
     * 是否正在拖动
     */
    private boolean isDraggingMode = false;

    /**
     * 判断view是点击还是移动的距离
     */
    private int mTouchSlop;

    /**
     * 记录手势
     */
    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private int mMinimumVelocity;

    /**
     * 点击事件
     */
    private PlayerBarOnClickListener playerBarOnClickListener;
//    /**
//     * 点击事件
//     */
//    private DragViewOnClickListener dragViewOnClickListener;

    /**
     * 打开事件
     */
    private PlayerBarOnCloseListener playerBarOnCloseListener;


    public SwipeoutLayout(Context context) {
        super(context);
        init(context);
    }


    public SwipeoutLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SwipeoutLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public SwipeoutLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context) {
        this.mContext = context;
        mScroller = new Scroller(context, sInterpolator);
        //设置布局为水平
        this.setOrientation(LinearLayout.HORIZONTAL);

        final ViewConfiguration configuration = ViewConfiguration
                .get(getContext());
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /**
     * 初始化界面视图并显示主界面
     *
     * @param contentView 主界面
     * @param menuView    菜单界面
     * @param dragView    拖动view
     */
    public void initViewAndShowContentView(ViewGroup contentView, ViewGroup menuView, View dragView) {
        initView(contentView, menuView, dragView, true);
    }

    /**
     * 初始化界面视图并显示菜单界面
     *
     * @param contentView 主界面
     * @param menuView    菜单界面
     * @param dragView    拖动view
     */
    public void initViewAndShowMenuView(ViewGroup contentView, ViewGroup menuView, View dragView) {
        initView(contentView, menuView, dragView, false);
    }

    /**
     * 初始化界面
     *
     * @param contentView  主界面
     * @param menuView     菜单界面
     * @param dragView     拖动view
     * @param showContView 是否显示主界面
     */
    private void initView(ViewGroup contentView, ViewGroup menuView, View dragView, boolean showContView) {
        this.mContentView = contentView;
        this.mMenuView = menuView;
        this.mDragView = dragView;

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        mScreensWidth = display.getWidth();
        //
        barFlagViewWidth = (int) mContext.getResources().getDimension(R.dimen.bar_dragflag_size);
        mMenuPaddingRithtWidth = (int) mContext.getResources().getDimension(R.dimen.player_bar_height) + barFlagViewWidth / 2;
        //menu菜单栏先添加
        LayoutParams menuLayout = new LayoutParams((mScreensWidth - mMenuPaddingRithtWidth), LayoutParams.MATCH_PARENT);
        //添加主界面
        LayoutParams conLayout = new LayoutParams(mScreensWidth, LayoutParams.MATCH_PARENT);

        menuLayout.leftMargin = -(mScreensWidth - mMenuPaddingRithtWidth);//隐藏菜单栏
        mCurrentView = mContentView;

        addView(mMenuView, menuLayout);
        addView(mContentView, conLayout);
        //
        if (!showContView) {
            showMenuView(0);
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
            int actionId = event.getAction();
            if (actionId == MotionEvent.ACTION_DOWN) {

                int[] location = new int[2];
                mDragView.getLocationOnScreen(location);
                int mDragViewLeftX = location[0] - barFlagViewWidth;
                int mDragViewRightX = mDragViewLeftX + mDragView.getWidth();

                // logger.e("mDragViewLeftX=" + mDragViewLeftX + "  mDragViewRightX=" + mDragViewRightX);
                //  logger.e("event.getRawX() = " + event.getRawX());
                //按下焦点在手动view里面
                if (isMenuViewShow() || (mDragViewLeftX <= event.getRawX() && event.getRawX() <= mDragViewRightX)) {
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

                    mFristX = event.getX();
                    mLastX = mFristX;
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

                    int tDeltaX = (int) (mLastX - mFristX);

                    mLastX = 0;
                    mFristX = 0;

                    if (isDraggingMode) {

//                    if (Math.abs(tDeltaX) < mTouchSlop) {
//
//                        if (dragViewOnClickListener != null) {
//                            dragViewOnClickListener.onClick();
//                        }
//
//                        //拖动view点击事件
//                        //logger.e("拖动view点击事件");
//
//                    }
                        if (Math.abs(tDeltaX) < mTouchSlop) {
                            if (playerBarOnClickListener != null) {
                                playerBarOnClickListener.onClick();
                            }
                        }

                    } else {


                        if (Math.abs(tDeltaX) < mTouchSlop) {

                            if (playerBarOnClickListener != null) {
                                playerBarOnClickListener.onClick();
                            }

                            //拖动view点击事件
                            //logger.e("拖动view点击事件");

                        }


                        // logger.e("布局view点击事件");

                        break;
                    }

                    isDraggingMode = false;


                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);

                    int xVelocity = (int) velocityTracker.getXVelocity();


                    if (Math.abs(xVelocity) > mMinimumVelocity) {


                        int scrollDeltaX = -1;
                        int scrollX = mScroller.getFinalX();
                        if (xVelocity < 0f) {

                            scrollDeltaX = 0 - scrollX;


                        } else {

                            scrollDeltaX = -mMenuView.getWidth() - scrollX;

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

                        if (scrollX <= -mMenuView.getWidth()) {
                            //右越界
                            scrollDeltaX = -(mMenuView.getWidth()) - scrollX;

                        } else if (scrollX > 0) {
                            //左越界
                            scrollDeltaX = 0 - scrollX;


                        } else if (scrollX <= -mScreensWidth / 2) {
                            //已移动到右边
                            scrollDeltaX = -mMenuView.getWidth() - scrollX;

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


                    releaseVelocityTracker();
                    break;
                default:

            }
        } catch (Exception e) {
        }
        return true;
    }

    /**
     * @param event
     */
    private void obtainVelocityTracker(MotionEvent event) {

        if (mVelocityTracker == null) {

            mVelocityTracker = VelocityTracker.obtain();

        }

        mVelocityTracker.addMovement(event);

    }


    /**
     * 释放
     */
    private void releaseVelocityTracker() {

        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;

        }

    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        // 更新当前的X轴偏移量
        if (mScroller.computeScrollOffset()) { // 返回true代表正在模拟数据，false 已经停止模拟数据
            scrollTo(mScroller.getCurrX(), 0); // 更新X轴的偏移量
            invalidate();
        } else {
            int scrollX = mScroller.getFinalX();
            if (scrollX <= -mMenuView.getWidth()) {
                mCurrentView = mMenuView;
                if (playerBarOnCloseListener != null) {
                    playerBarOnCloseListener.onOpen();
                }
            } else if (scrollX >= 0) {
                mCurrentView = mContentView;
                if (playerBarOnCloseListener != null) {
                    playerBarOnCloseListener.onClose();
                }
            }
        }
    }

    /**
     * 显示view
     */
    private void showMenuView(int duration) {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        int scrollX = mScroller.getFinalX();
        int scrollDeltaX = -(mScreensWidth - mMenuPaddingRithtWidth) - scrollX;
        Log.d("XXX", "scrollX: " + scrollX + " --- deltaX: " + scrollDeltaX);
        mScroller.startScroll(mScroller.getFinalX(), 0, scrollDeltaX, 0, duration);
        invalidate();
    }

    /**
     * 隐藏view
     */
    public void hideMenuView() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        int scrollX = mScroller.getFinalX();
        int scrollDeltaX = 0 - scrollX;
        mScroller.startScroll(mScroller.getFinalX(), 0, scrollDeltaX, 0, Math.abs(scrollDeltaX) * 5);
        invalidate();
    }

    /**
     * 是否是菜单界面正在显示
     *
     * @return
     */
    public boolean isMenuViewShow() {

        int scrollX = mScroller.getFinalX();
        if (scrollX <= -mMenuView.getWidth()) {
            mCurrentView = mMenuView;
        } else if (scrollX >= 0) {
            mCurrentView = mContentView;
        }
        return mCurrentView == mMenuView;
    }

    public void setPlayerBarOnClickListener(PlayerBarOnClickListener playerBarOnClickListener) {
        this.playerBarOnClickListener = playerBarOnClickListener;
    }


//    public interface DragViewOnClickListener {
//        public void onClick();
//    }

    //////

    public interface PlayerBarOnClickListener {
        void onClick();
    }

    public interface PlayerBarOnCloseListener {
        void onClose();

        void onOpen();
    }

    public void setPlayerBarOnCloseListener(PlayerBarOnCloseListener playerBarOnCloseListener) {
        this.playerBarOnCloseListener = playerBarOnCloseListener;
    }

    //    public void setDragViewOnClickListener(DragViewOnClickListener dragViewOnClickListener) {
//        this.dragViewOnClickListener = dragViewOnClickListener;
//    }
}
