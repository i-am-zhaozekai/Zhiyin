package com.haha.sbc;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 07/05/2018
 */

public class StatusBarCompat {

    /**
     * Supported formats are:
     * #RRGGBB
     * #AARRGGBB
     * or one of the following names:
     * 'red', 'blue', 'green', 'black', 'white', 'gray', 'cyan', 'magenta',
     * 'yellow', 'lightgray', 'darkgray', 'grey', 'lightgrey', 'darkgrey',
     * 'aqua', 'fuchsia', 'lime', 'maroon', 'navy', 'olive', 'purple',
     * 'silver', 'teal'.
     */
    public static void setStatusBarColor(Activity activity, String colorString, ViewGroup statusBarParent) {
        int color = Color.parseColor(colorString);

        setStatusBarColor(activity, color, statusBarParent);
    }

    public static void setStatusBarColorWithRes(Activity activity, @ColorRes int colorResId, ViewGroup statusBarParent) {
        int color = -1;
        if (colorResId == -1) {
            color = ContextCompat.getColor(activity.getApplicationContext(), R.color.colorDefault);
        } else {
            color = ContextCompat.getColor(activity.getApplicationContext(), colorResId);
        }

        setStatusBarColor(activity, color, statusBarParent);

    }

    public static void setStatusBarColor(Activity activity, @ColorInt int argb, ViewGroup statusBarParent) {

        if (!isStatusBarExists(activity)) {
            return;
        }

        /*ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.setPadding(0, getStatusBarHeight(activity), 0, 0);*/
        activity.getWindow().setStatusBarColor(argb);

        /*setStatusBarTransparent(activity);

        View statusBarView = new View(activity.getApplicationContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight(activity.getApplicationContext()));
        statusBarView.setBackgroundColor(argb);

        statusBarParent.addView(statusBarView, 0, params);*/

        /*ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
        // 移除半透明矩形,以免叠加
        if (contentView.getChildCount() > 1) {
            contentView.removeViewAt(1);
        }

        contentView.addView(createStatusBarView(activity, argb));*/
    }

    public static boolean isStatusBarExists(Activity activity) {
        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
        return (params.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != WindowManager.LayoutParams.FLAG_FULLSCREEN;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    // 将状态栏设置为透明
    private static void setStatusBarTransparent(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            //window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams params = activity.getWindow().getAttributes();
            params.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | params.flags);
        }
    }

    // 绘制一个和状态栏一样高的矩形
    private static View createStatusBarView(Activity activity, int argb) {
        View statusBarView = new View(activity.getApplicationContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight(activity.getApplicationContext()));
        statusBarView.setLayoutParams(params);
        statusBarView.setBackgroundColor(argb);
        return statusBarView;
    }
}
