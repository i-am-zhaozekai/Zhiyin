package com.haha.zy.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.haha.zy.R;
import com.jaeger.library.StatusBarUtil;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 06/05/2018
 */

public class BaseActivity extends AppCompatActivity {

    private StatusBarDecor mStatusBarDecor;
    public void setStatusBarDecor(StatusBarDecor decor) {
        this.mStatusBarDecor = decor;
    }

    public interface StatusBarDecor {
        void decorStatusBar();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        // setContentView
        ViewGroup layout = null;
        int layoutId = setContentViewId();
        if (layoutId != -1) {
            layout = (ViewGroup) LayoutInflater.from(this).inflate(setContentViewId(), null);
            setContentView(layout);
        }

        initViews(savedInstanceState, layout);

        // 设置 StatusBar 自定义实现沉浸式效果
        if (mStatusBarDecor != null) {
            mStatusBarDecor.decorStatusBar();
        } else {
            decorStatusBar();
        }

        loadData(false);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        loadData(true);
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
        StatusBarUtil.setColor(this, getResources().getColor(R.color.colorStatusBarDefault), 0);
    }
}
