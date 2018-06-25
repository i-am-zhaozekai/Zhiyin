package com.haha.zy.glide;

import android.support.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 19/06/2018
 */

public class SingerFetcher implements DataFetcher<InputStream> {

    // 检查是否取消任务的标识
    private volatile boolean mIsCancelled;

    private final SingerModel mSinger;
    private Call mFetchStreamCall;
    private InputStream mInputStream;


    public SingerFetcher(SingerModel singer) {
        mSinger = singer;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        String portraitUrl = mSinger.getPortraitUrl();
        if (portraitUrl == null) {
            if (mIsCancelled) {
                callback.onLoadFailed(new IllegalStateException("Request has been canceled"));
                return;
            }

            // 从网络上查询对应的的头像地址
            portraitUrl = mSinger.getPortraitUrlGetter().getUrl(mSinger.getName());
            if (portraitUrl == null) {
                callback.onLoadFailed(new IllegalStateException("Get fail from Server failed"));
                return;
            }

            // 存储获取到的url，以供缓存使用
            mSinger.setPortraitUrl(portraitUrl);
        }

        if (mIsCancelled) {
            callback.onLoadFailed(new IllegalStateException("Request has been canceled"));
            return;
        }

        // 真正获取头像数据
        mInputStream = fetchStream(portraitUrl);
        callback.onDataReady(mInputStream);
    }

    @Override
    public void cleanup() {
        if (mInputStream != null){
            try {
                mInputStream.close();
            } catch (IOException e) {
                //e.printStackTrace();
            } finally {
                mInputStream = null;
            }

        }

    }

    @Override
    public void cancel() {
        mIsCancelled = true;

        if (mFetchStreamCall != null) {
            mFetchStreamCall.cancel();
        }
    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.REMOTE;
    }

    private InputStream fetchStream(String url) {
        // 缓存请求，用来及时取消连接
        mFetchStreamCall = syncGet(url);
        try {
            return mFetchStreamCall.execute().body().byteStream();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return null;
    }

    /**
     * 同步的http get请求
     *
     * @param url 要访问的url
     * @return
     */
    private Call syncGet(String url) {
        Request request = new Request.Builder().url(url).get().build();
        return new OkHttpClient().newCall(request);
    }
}
