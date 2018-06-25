package com.haha.zy.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haha.zy.R;
import com.haha.zy.audio.AudioInfo;
import com.haha.zy.db.DatabaseHelper;
import com.haha.zy.player.EventManager;
import com.haha.zy.preference.PreferenceManager;
import com.haha.zy.widget.RingBackgroundLayout;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 07/06/2018
 */

public class ListenFragment extends BaseFragment {

    private PreferenceManager mPrefMgr;
    private DatabaseHelper mDbHelper;

    /**
     * 更新本地音乐
     */
    private final int UPDATELOCALCOUNT = 0;
    /**
     * 更新最近音乐
     */
    private final int UPDATERECENTCOUNT = 1;

    /**
     * 更新喜欢音乐
     */
    private final int UPDATELIKECOUNT = 2;
    /**
     * 更新下载音乐
     */
    private final int UPDATEDOWNLOADCOUNT = 3;


    /**
     * 本地音乐
     */
    private LinearLayout mLocalMusic;

    /**
     * 本地音乐个数
     */
    private TextView mLocalCountTv;
    /**
     * 本地音乐个数
     */
    private int mLocalCount = 0;

    //////////////////////////////////////////////

    /**
     * 喜欢音乐
     */
    private LinearLayout mLikeMusic;
    /**
     * 喜欢音乐个数
     */
    private TextView mLikeCountTv;
    /**
     * 喜欢音乐个数
     */
    private int mLikeCount = 0;

    /**
     * wifi设置按钮
     */
    private RingBackgroundLayout mWifiRingBackgroundLayout;
    /**
     * 问候语按钮
     */
    private RingBackgroundLayout mSayHelloRingBackgroundLayout;

    /**
     * 线控
     */
    private RingBackgroundLayout mWireRingBackgroundLayout;

    /**
     * 退出设置按钮
     */
    private RingBackgroundLayout mExitRingBackgroundLayout;


    /////////////////////////////////////////////////////////

    /**
     * 最近音乐
     */
    private LinearLayout mRecentMusic;

    /**
     * 最近音乐个数
     */
    private TextView mRecentCountTv;
    /**
     * 最近音乐个数
     */
    private int mRecentCount = 0;

    ////////////////////////////////////////////////////////

    /**
     * 下载音乐
     */
    private LinearLayout mDownloadMusic;
    /**
     * 下载音乐个数
     */
    private TextView mDownloadCountTv;
    /**
     * 下载音乐个数
     */
    private int mDownloadCount = 0;

    @Override
    protected int setContentViewId() {
        return R.layout.layout_fragment_my;
    }

    @Override
    protected void init() {
        super.init();

        mPrefMgr = PreferenceManager.getInstance(mActivity.getApplicationContext());
        mDbHelper = DatabaseHelper.getInstance(mActivity.getApplicationContext());
    }

    @Override
    protected void initViews(Bundle savedInstanceState, View mainView) {
        super.initViews(savedInstanceState, mainView);

        //本地音乐
        mLocalMusic = mainView.findViewById(R.id.tab_local_music);
        mLocalMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //打开
                /*Intent openIntent = new Intent(FragmentReceiver.ACTION_OPENLOCALMUSICFRAGMENT);
                openIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mActivity.sendBroadcast(openIntent);*/
            }
        });
        mLocalCountTv = mainView.findViewById(R.id.local_music_count);


        //喜欢的音乐
        mLikeMusic = mainView.findViewById(R.id.tab_like_music);
        mLikeMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //打开
                /*Intent openIntent = new Intent(FragmentReceiver.ACTION_OPENLIKEMUSICFRAGMENT);
                openIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mActivity.sendBroadcast(openIntent);*/

            }
        });
        mLikeCountTv = mainView.findViewById(R.id.like_music_count);

        //下载音乐
        mDownloadMusic = mainView.findViewById(R.id.tab_download_music);
        mDownloadMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //打开
                /*Intent openIntent = new Intent(FragmentReceiver.ACTION_OPENDOWNLOADMUSICFRAGMENT);
                openIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mActivity.sendBroadcast(openIntent);*/
            }
        });
        mDownloadCountTv = mainView.findViewById(R.id.download_music_count);

        //最近音乐
        mRecentMusic = mainView.findViewById(R.id.tab_centent_music);
        mRecentMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //打开
                /*Intent openIntent = new Intent(FragmentReceiver.ACTION_OPENRECENTMUSICFRAGMENT);
                openIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mActivity.sendBroadcast(openIntent);*/

            }
        });
        mRecentCountTv = mainView.findViewById(R.id.recent_music_count);

        showContentView();

        mAudioBroadcastReceiver = new EventManager(mActivity.getApplicationContext());
        mAudioBroadcastReceiver.setEventListener(mAudioReceiverListener);
        mAudioBroadcastReceiver.init();
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        super.loadData(isRestoreInstance);

        loadLocalCount();
        loadRecentCount();
        loadLikeCount();
        loadDownloadCount();
    }

    @Override
    public void onDestroy() {
        mAudioBroadcastReceiver.release();
        super.onDestroy();
    }

    /**
     * 加载喜欢歌曲列表
     */
    private void loadDownloadCount() {
        new AsyncTask<String, Integer, Void>() {
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                mHandler.sendEmptyMessage(UPDATEDOWNLOADCOUNT);
            }

            @Override
            protected Void doInBackground(String... strings) {

                mDownloadCount = DatabaseHelper.getInstance(mActivity.getApplicationContext()).getDonwloadAudioCount();

                return null;
            }
        }.execute("");
    }

    /**
     * 加载喜欢歌曲列表
     */
    private void loadLikeCount() {
        new AsyncTask<String, Integer, Void>() {
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                mHandler.sendEmptyMessage(UPDATELIKECOUNT);
            }

            @Override
            protected Void doInBackground(String... strings) {

                mLikeCount = DatabaseHelper.getInstance(mActivity.getApplicationContext()).getLikeAudioCount();

                return null;
            }
        }.execute("");
    }

    /**
     * 加载本地音乐个数
     */
    private void loadLocalCount() {
        new AsyncTask<String, Integer, Void>() {
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                mHandler.sendEmptyMessage(UPDATELOCALCOUNT);
            }

            @Override
            protected Void doInBackground(String... strings) {

                mLocalCount = DatabaseHelper.getInstance(mActivity.getApplicationContext()).getLocalAudioCount();

                return null;
            }
        }.execute("");
    }

    /**
     * 获取最近音乐个数
     */
    private void loadRecentCount() {
        new AsyncTask<String, Integer, Void>() {
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                mHandler.sendEmptyMessage(UPDATERECENTCOUNT);
            }

            @Override
            protected Void doInBackground(String... strings) {

                mRecentCount = DatabaseHelper.getInstance(mActivity.getApplicationContext()).getRecentAudioCount();

                return null;
            }
        }.execute("");
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATELOCALCOUNT:
                    mLocalCountTv.setText(mLocalCount + "");
                    break;
                case UPDATERECENTCOUNT:
                    mRecentCountTv.setText(mRecentCount + "");
                    break;
                case UPDATELIKECOUNT:
                    mLikeCountTv.setText(mLikeCount + "");
                    break;
                case UPDATEDOWNLOADCOUNT:
                    mDownloadCountTv.setText(mDownloadCount + "");
                    break;
            }
        }
    };


    private EventManager mAudioBroadcastReceiver;

    /**
     * 广播监听
     */
    private EventManager.EventListener mAudioReceiverListener = new EventManager.EventListener() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doAudioReceive(context, intent);
        }
    };

    private void doAudioReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(EventManager.ACTION_LOCALUPDATE)) {
            loadLocalCount();
        } else if (action.equals(EventManager.ACTION_RECENTUPDATE)) {
            loadRecentCount();
        } else if (action.equals(EventManager.ACTION_LIKEUPDATE)) {
            loadLikeCount();
        } else if (action.equals(EventManager.ACTION_SERVICE_PLAYMUSIC)) {
            //将正在播放的歌曲加入最近播放列表中
            AudioInfo audioInfo = mPrefMgr.getCurrentAudio();
            if (audioInfo != null) {
                if (mDbHelper.isRecentOrLikeExists(audioInfo.getHash(), audioInfo.getType(), true)) {
                    mDbHelper.updateRecentAudio(audioInfo.getHash(), audioInfo.getType(), true);
                } else {
                    mDbHelper.addRecentOrLikeAudio(audioInfo, true);
                }
                loadRecentCount();
            }
        } else if (action.equals(EventManager.ACTION_DOWNLOADUPDATE)) {
            loadDownloadCount();
        } else if (action.equals(EventManager.ACTION_LIKEADD)) {
            //添加喜欢
            AudioInfo audioInfo = (AudioInfo) intent.getSerializableExtra(AudioInfo.KEY);
            mDbHelper.addRecentOrLikeAudio(audioInfo, false);
            loadLikeCount();
        } else if (action.equals(EventManager.ACTION_LIKEDELETE)) {
            //删除喜欢
            AudioInfo audioInfo = (AudioInfo) intent.getSerializableExtra(AudioInfo.KEY);
            mDbHelper.deleteRecentOrLikeAudio(audioInfo.getHash(), audioInfo.getType(), false);
            loadLikeCount();
        }
    }
}
