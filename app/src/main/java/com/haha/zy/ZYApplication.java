package com.haha.zy;

import android.app.Application;

import com.haha.zy.preference.PreferenceConstants;
import com.haha.zy.preference.PreferenceManager;

import java.io.File;
import java.util.List;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 06/05/2018
 */

public class ZYApplication extends Application {

    private static ZYApplication instance;
    public static ZYApplication getInstance() {
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
    }

}
