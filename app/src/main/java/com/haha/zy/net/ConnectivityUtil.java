package com.haha.zy.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.haha.zy.net.model.HttpResult;
import com.haha.zy.preference.PreferenceManager;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 04/06/2018
 */

public class ConnectivityUtil {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }

        NetworkInfo[] info = cm.getAllNetworkInfo();
        if (info == null) {
            return false;
        }

        for (int i = 0; i < info.length; i++) {
            if (NetworkInfo.State.CONNECTED == info[i].getState()) {
                return true;
            }
        }

        return false;
    }

    public static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    public static boolean ensureConnectivityState(Context context) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            return false;
        }

        if (PreferenceManager.getInstance(context).isWifiOnly() && !ConnectivityUtil.isWifi(context)) {
            return false;
        }

        return true;
    }

    public static boolean ensureConnectivityState(Context context, HttpResult result) {
        if (!ConnectivityUtil.isNetworkAvailable(context)) {
            result.setStatus(HttpResult.STATUS_NONET);
            result.setErrorMsg("当前网络不可用");
            return false;
        }

        if (PreferenceManager.getInstance(context).isWifiOnly() && !ConnectivityUtil.isWifi(context)) {
            result.setStatus(HttpResult.STATUS_NOWIFI);
            result.setErrorMsg("当前网络不是wifi");
            return false;
        }

        return true;
    }

}
