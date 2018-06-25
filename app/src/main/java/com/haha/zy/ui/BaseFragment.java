package com.haha.zy.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.haha.zy.R;
import com.haha.zy.widget.IconFontTextView;
import com.jaeger.library.StatusBarUtil;


public abstract class BaseFragment extends StatedFragment {

    public Activity mActivity;

    /**
     * 内容布局
     */
    private LinearLayout mContentContainer;

    //////////////////////////////////////////////////////////////////////

    /**
     * 加载中布局
     */
    private LinearLayout mLoadingContainer;
    /**
     * 加载图标
     */
    private ImageView mLoadImgView;

    /**
     * 旋转动画
     */
    private Animation rotateAnimation;

    //////////////////////////////////////////////////////////////////////
    /**
     * 无网络
     */
    private LinearLayout mNetContainer;

    /**
     *
     */
    private RelativeLayout mNetBGLayout;


    /////////////////////////////////////////////////////////////////////

    private ViewGroup mainView;
    private final int SHOWLOADINGVIEW = 0;
    private final int SHOWCONTENTVIEW = 1;
    private final int SHOWNONETView = 2;

    private Handler mShowViewHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOWCONTENTVIEW:
                    showContentViewHandler();
                    break;
                case SHOWLOADINGVIEW:
                    showLoadingViewHandler();
                    break;
                case SHOWNONETView:
                    showNoNetViewHandler();
                    break;
            }
        }
    };

    private RefreshListener mRefreshListener;

    public BaseFragment() {
        if (getArguments() == null && !isVisible()) {
            setArguments(new Bundle());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        //
        LayoutInflater inflater = mActivity.getLayoutInflater();
        mainView = (ViewGroup) inflater.inflate(R.layout.layout_fragment_base, null, false);
        ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        //
        mContentContainer = mainView.findViewById(R.id.content_container);
        View contentView = inflater.inflate(setContentViewId(), null, false);
        //
        mLoadingContainer = mainView.findViewById(R.id.loading_container);
        View loadingView = inflater.inflate(R.layout.layout_fragment_loading, null, false);

        //
        mNetContainer = mainView.findViewById(R.id.net_container);
        View noNetView = inflater.inflate(R.layout.layout_fragment_nonet, null, false);


        //
        mNetContainer.addView(noNetView, vlp);
        mLoadingContainer.addView(loadingView, vlp);
        mContentContainer.addView(contentView, vlp);

        //初始化界面
        initView();
        initViews(savedInstanceState, mainView);

        // 设置 StatusBar 自定义实现沉浸式效果
        if (mStatusBarDecor != null) {
            mStatusBarDecor.decorStatusBar();
        } else {
            decorStatusBar();
        }

        loadData(false);
    }


    /**
     * Save Fragment's State here
     */
    @Override
    protected void onSaveState(Bundle outState) {
        super.onSaveState(outState);
        // For example:
        //outState.putString("text", tvSample.getText().toString());
    }

    /**
     * Restore Fragment's State here
     */
    @Override
    protected void onRestoreState(Bundle savedInstanceState) {
        super.onRestoreState(savedInstanceState);
        // For example:
        //tvSample.setText(savedInstanceState.getString("text"));
        loadData(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) mainView.getParent();
        if (viewGroup != null) {
            viewGroup.removeAllViewsInLayout();
        }
        return mainView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = (Activity) context;
    }

    /**
     * 初始界面
     */
    private void initView() {

        mLoadImgView = mLoadingContainer.findViewById(R.id.load_img);
        rotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_rotate);
        rotateAnimation.setInterpolator(new LinearInterpolator());// 匀速
        mLoadImgView.startAnimation(rotateAnimation);

        mNetBGLayout = mNetContainer.findViewById(R.id.net_layout);
        mNetBGLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRefreshListener != null) {
                    mRefreshListener.refresh();
                }
            }
        });
    }

    /**
     * 显示加载窗口
     */
    public void showLoadingView() {
        mShowViewHandler.sendEmptyMessage(SHOWLOADINGVIEW);
    }

    /**
     * 显示加载窗口
     */
    private void showLoadingViewHandler() {

        mNetContainer.setVisibility(View.GONE);
        mContentContainer.setVisibility(View.GONE);
        mLoadingContainer.setVisibility(View.VISIBLE);
        mLoadImgView.clearAnimation();
        mLoadImgView.startAnimation(rotateAnimation);

    }

    /**
     * 显示主界面
     */
    public void showContentView() {
        mShowViewHandler.sendEmptyMessage(SHOWCONTENTVIEW);
    }

    /**
     * 显示主界面
     */
    private void showContentViewHandler() {
        mContentContainer.setVisibility(View.VISIBLE);
        mLoadingContainer.setVisibility(View.GONE);
        mNetContainer.setVisibility(View.GONE);
        mLoadImgView.clearAnimation();
    }

    /**
     * 显示无网络界面
     */
    public void showNoNetView() {
        mShowViewHandler.sendEmptyMessage(SHOWNONETView);
    }

    /**
     * 显示无网络界面
     */
    private void showNoNetViewHandler() {
        mContentContainer.setVisibility(View.GONE);
        mLoadingContainer.setVisibility(View.GONE);
        mNetContainer.setVisibility(View.VISIBLE);
        mLoadImgView.clearAnimation();
    }


    ///////////////////////////

    public interface RefreshListener {
        void refresh();
    }

    public void setRefreshListener(RefreshListener mRefreshListener) {
        this.mRefreshListener = mRefreshListener;
    }


    private BaseActivity.StatusBarDecor mStatusBarDecor;
    public void setStatusBarDecor(BaseActivity.StatusBarDecor decor) {
        this.mStatusBarDecor = decor;
    }

    public interface StatusBarDecor {
        void decorStatusBar();
    }

    protected void init() {};

    protected int setContentViewId() {
        return -1;
    }

    protected void initViews(Bundle savedInstanceState, View contentRoot) {

    }

    protected void loadData(boolean isRestoreInstance) {

    }

    private void decorStatusBar(){
        StatusBarUtil.setColor(this.mActivity, getResources().getColor(R.color.colorStatusBarDefault), 0);
    }
}
