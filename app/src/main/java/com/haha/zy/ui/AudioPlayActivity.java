package com.haha.zy.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.haha.zy.R;
import com.haha.zy.adapter.MainPopPlayListAdapter;
import com.haha.zy.audio.AudioInfo;
import com.haha.zy.db.DatabaseHelper;
import com.haha.zy.db.SongSingerDB;
import com.haha.zy.glide.ZYGlideModule;
import com.haha.zy.lyric.ExtraLyricMode;
import com.haha.zy.lyric.ExtraLyricShowMode;
import com.haha.zy.lyric.LyricsManager;
import com.haha.zy.lyric.model.LyricTag;
import com.haha.zy.lyric.model.LyricsInfo;
import com.haha.zy.lyric.utils.LyricsIOUtils;
import com.haha.zy.lyric.utils.LyricsUtil;
import com.haha.zy.net.api.kugou.SingerPortraitInfo;
import com.haha.zy.net.api.kugou.SingerPortraitURLGetter;
import com.haha.zy.net.model.SongSingerInfo;
import com.haha.zy.player.EventManager;
import com.haha.zy.player.PlayMode;
import com.haha.zy.player.PlaybackInfo;
import com.haha.zy.player.PlayerManager;
import com.haha.zy.preference.PreferenceManager;
import com.haha.zy.util.FileUtil;
import com.haha.zy.util.ToastUtil;
import com.haha.zy.widget.LinearLayoutRecyclerView;
import com.haha.zy.widget.LrcSeekBar;
import com.haha.zy.widget.MultiplyLineLyricView;
import com.haha.zy.widget.RotatableLayout;
import com.haha.zy.widget.SlidingImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 08/06/2018
 */

public class AudioPlayActivity extends BaseActivity implements BaseActivity.StatusBarDecor {

    private PreferenceManager mPrfMgr;

    private RotatableLayout mRotateContainer;
    private LinearLayout mLrcPlaybarLinearLayout;

    private SlidingImageView mSlidingBackgroundView;

    private MultiplyLineLyricView mManyLineLyricsView;

    //翻译歌词
    private ImageView mTranslationIV;
    //音译歌词
    private ImageView mTransliterationIV;
    //翻译歌词/音译歌词
    private ImageView mMultiTranslationIV;

    private ImageView mMoreMenuImgBtn;

    private RelativeLayout mPlayBtn;
    private RelativeLayout mPauseBtn;
    private RelativeLayout mNextBtn;
    private RelativeLayout mPreBtn;

    private ImageView mPlayModeIV;

    private LrcSeekBar mLrcSeekBar;

    private TextView mSongProgressTv;
    private TextView mSongDurationTv;

    private int mScreenWidth;  // 屏幕宽度
    private int mScreenHeight; // 屏幕高度

    private final RotatableLayout.RotateListener mRotateListener = new RotatableLayout.RotateListener() {
        @Override
        public void close() {
            AudioPlayActivity.this.setResult(RESULT_CODE_TO_MAIN_ACTIVITY);
            finish();
            overridePendingTransition(0, 0);
        }

        @Override
        public void onClick() {
            if (mPrfMgr.getPlaybackInfo() != null) {
                mManyLineLyricsView.setManyLineLrc(!mManyLineLyricsView.isManyLineLrc(), (int) mPrfMgr.getPlaybackInfo().getProgress());
            } else {
                mManyLineLyricsView.setManyLineLrc(!mManyLineLyricsView.isManyLineLrc(), 0);
            }

            mPrfMgr.setLrcMultiLine(mManyLineLyricsView.isManyLineLrc());
        }
    };

    private final MultiplyLineLyricView.ExtraLyricsListener mExtraLrcListener
            = new MultiplyLineLyricView.ExtraLyricsListener() {

        @Override
        public void hasTranslateLrcCallback(ExtraLyricShowMode showType) {
            Message msg = mExtraLrcTypeHandler.obtainMessage(ExtraLyricMode.TRANSLATION.ordinal());
            msg.arg1 = showType.ordinal();
            mExtraLrcTypeHandler.sendMessage(msg);
        }

        @Override
        public void hasTransliterationLrcCallback(ExtraLyricShowMode showType) {
            Message msg = mExtraLrcTypeHandler.obtainMessage(ExtraLyricMode.TRANSLITERATION.ordinal());
            msg.arg1 = showType.ordinal();
            mExtraLrcTypeHandler.sendMessage(msg);
        }

        @Override
        public void hasTranslateAndTransliterationLrcCallback(ExtraLyricShowMode showType) {
            Message msg = mExtraLrcTypeHandler.obtainMessage(ExtraLyricMode.BOTH.ordinal());
            msg.arg1 = showType.ordinal();
            mExtraLrcTypeHandler.sendMessage(msg);
        }

        @Override
        public void noExtraLrcCallback() {
            Message msg = mExtraLrcTypeHandler.obtainMessage(ExtraLyricMode.NONE.ordinal());
            msg.arg1 = ExtraLyricShowMode.NONE.ordinal();
            mExtraLrcTypeHandler.sendMessage(msg);
        }
    };

    private final MultiplyLineLyricView.OnLrcClickListener mOnLrcClickListener
            = new MultiplyLineLyricView.OnLrcClickListener() {

        @Override
        public void onLrcPlayClicked(int progress, boolean isLrcSeekTo) {
            seekToMusic(progress, isLrcSeekTo);
        }
    };

    /**
     * 歌词界面跳转到主界面的code
     */
    private final int RESULT_CODE_TO_MAIN_ACTIVITY = 1;

    private static final int INVALID_RESOURCE_ID = -1;
    private static final int[][] EXTRA_LYRIC_RESOURCE_IDS = new int[][] {
            {R.mipmap.lyric_translation_show, INVALID_RESOURCE_ID, R.mipmap.lyric_translation_hide},
            {INVALID_RESOURCE_ID, R.mipmap.lyric_transliteration_show, R.mipmap.lyric_transliteration_hide},
            {R.mipmap.lyric_both_show_translation, R.mipmap.lyric_both_show_transliteration, R.mipmap.lyric_both_hide},
            {INVALID_RESOURCE_ID, INVALID_RESOURCE_ID, INVALID_RESOURCE_ID}
    };

    private static int getExtraLyricButtonResource(int extraLrcMode, int showMode) {
        return EXTRA_LYRIC_RESOURCE_IDS[extraLrcMode][showMode];
    }

    private Handler mExtraLrcTypeHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int extraLrcType = msg.what;
            int showType = msg.arg1;

            if (ExtraLyricMode.TRANSLATION.ordinal() == msg.what) {
                //翻译歌词
                mTranslationIV.setVisibility(View.VISIBLE);
                int resourceId = getExtraLyricButtonResource(extraLrcType, showType);
                if (INVALID_RESOURCE_ID != resourceId) {
                    mTranslationIV.setBackgroundResource(resourceId);
                }
                //音译歌词
                mTransliterationIV.setVisibility(View.INVISIBLE);
                //翻译歌词/音译歌词
                mMultiTranslationIV.setVisibility(View.INVISIBLE);

            } else if (ExtraLyricMode.TRANSLITERATION.ordinal() == msg.what) {

                //翻译歌词
                mTranslationIV.setVisibility(View.INVISIBLE);
                //音译歌词
                mTransliterationIV.setVisibility(View.VISIBLE);
                int resourceId = getExtraLyricButtonResource(extraLrcType, showType);
                if (INVALID_RESOURCE_ID != resourceId) {
                    mTransliterationIV.setBackgroundResource(resourceId);
                }
                //翻译歌词/音译歌词
                mMultiTranslationIV.setVisibility(View.INVISIBLE);

            } else if (ExtraLyricMode.BOTH.ordinal() == msg.what) {
                //翻译歌词
                mTranslationIV.setVisibility(View.INVISIBLE);
                //音译歌词
                mTransliterationIV.setVisibility(View.INVISIBLE);
                //翻译歌词/音译歌词
                mMultiTranslationIV.setVisibility(View.VISIBLE);
                int resourceId = getExtraLyricButtonResource(extraLrcType, showType);
                if (INVALID_RESOURCE_ID != resourceId) {
                    mMultiTranslationIV.setBackgroundResource(resourceId);
                }
            } else if (ExtraLyricMode.NONE.ordinal() == msg.what) {
                //翻译歌词
                mTranslationIV.setVisibility(View.INVISIBLE);
                //音译歌词
                mTransliterationIV.setVisibility(View.INVISIBLE);
                //翻译歌词/音译歌词
                mMultiTranslationIV.setVisibility(View.INVISIBLE);
            }
        }
    };

    private TextView mSongNameTextView;
    private TextView mSingerNameTextView;

    private EventManager mEventManager;
    private EventManager.EventListener mEventListener = new EventManager.EventListener() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doAudioReceive(context, intent);
        }
    };

    @Override
    protected void init() {
        mPrfMgr = PreferenceManager.getInstance(getApplicationContext());

        mEventManager = new EventManager(getApplicationContext());
        mEventManager.setEventListener(mEventListener);
        mEventManager.init();

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        mScreenWidth = display.getWidth();
        mScreenHeight = display.getHeight();

        setStatusBarDecor(this);

        mSingerImageRequestManager = Glide.with(this);
        mSingerImageOptions = new RequestOptions();
        mSingerImageOptions.signature(ZYGlideModule.obtainSignatureKey());
        mSingerImageOptions.placeholder(R.mipmap.singer_def);
    }

    @Override
    protected int setContentViewId() {
        return R.layout.audio_play_activity;
    }

    @Override
    protected void initViews(Bundle savedInstanceState, View contentRoot) {

        mContentView = contentRoot;

        //-------------------------------- INIT [歌手写真控件] --------------------------------
        mSlidingBackgroundView = contentRoot.findViewById(R.id.sliding_background);
        //-------------------------------- END_INIT [歌手写真控件] ----------------------------

        mSongNameTextView = contentRoot.findViewById(R.id.song_name_tv);
        mSingerNameTextView = contentRoot.findViewById(R.id.singer_name_tv);

        //-------------------------------- INIT [旋转背景控件] --------------------------------
        mRotateContainer = findViewById(R.id.rotateLayout);
        mRotateContainer.setRotateListener(mRotateListener);
        mRotateContainer.resetView();
        mRotateContainer.setVerticalScrollView(mManyLineLyricsView);
        //-------------------------------- END_INIT [旋转背景控件] ----------------------------

        //-------------------------------- INIT [歌词控件] --------------------------------
        mManyLineLyricsView = contentRoot.findViewById(R.id.lyric_view);

        mManyLineLyricsView.setExtraLyricsListener(mExtraLrcListener);
        mManyLineLyricsView.setOnLrcClickListener(mOnLrcClickListener);
        //设置字体大小和歌词颜色
        mManyLineLyricsView.setLrcFontSize(mPrfMgr.getLrcFontSize());
        int lrcColor = Color.parseColor(mPrfMgr.getLrcColorStr()[mPrfMgr.getLrcColorIndex()]);
        mManyLineLyricsView.setLrcColor(lrcColor);
        mManyLineLyricsView.setManyLineLrc(mPrfMgr.isLrcMultiLine(), 0);
        //-------------------------------- END_INIT [歌词控件] ----------------------------

        //-------------------------------- INIT [返回按钮] --------------------------------
        RelativeLayout backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRotateContainer.finish();
            }
        });
        //-------------------------------- END_INIT [返回按钮] ----------------------------

        mLrcPlaybarLinearLayout = findViewById(R.id.lrc_playbar);
        mRotateContainer.setIgnoreView(mLrcPlaybarLinearLayout);

        mRotateContainer.setVerticalScrollView(mManyLineLyricsView);

        //-------------------------------- INIT [歌词翻译类型按钮] --------------------------------
        mTranslationIV = findViewById(R.id.lrc_translation_btn);
        mTransliterationIV = findViewById(R.id.lrc_transliteration_btn);
        mMultiTranslationIV = findViewById(R.id.lrc_multi_translation_btn);


        mTranslationIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ExtraLyricShowMode showType = mManyLineLyricsView.getExtraLrcShowMode();
                ExtraLyricShowMode nextShowType = ExtraLyricShowMode.NONE;
                if (ExtraLyricShowMode.SHOW_TRANSLATION == showType) {
                    nextShowType = ExtraLyricShowMode.NONE;
                } else if (ExtraLyricShowMode.NONE == showType) {
                    nextShowType = ExtraLyricShowMode.SHOW_TRANSLATION;
                } else if (ExtraLyricShowMode.SHOW_TRANSLITERATION == showType ){
                    // 出错了，意译模式下不能出现显示音译要求
                }

                int resourceId = getExtraLyricButtonResource(ExtraLyricMode.TRANSLATION.ordinal(), nextShowType.ordinal());
                if (INVALID_RESOURCE_ID != resourceId) {
                    mTranslationIV.setBackgroundResource(resourceId);
                }

                if (mPrfMgr.getPlaybackInfo() != null) {
                    mManyLineLyricsView.setExtraLrcStatus(nextShowType, (int) mPrfMgr.getPlaybackInfo().getProgress());
                } else {
                    mManyLineLyricsView.setExtraLrcStatus(nextShowType, 0);
                }

                mPrfMgr.setLrcMultiLine(mManyLineLyricsView.isManyLineLrc());

            }
        });

        mTransliterationIV.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ExtraLyricShowMode showType = mManyLineLyricsView.getExtraLrcShowMode();
                ExtraLyricShowMode nextShowType = ExtraLyricShowMode.NONE;

                if (ExtraLyricShowMode.SHOW_TRANSLITERATION == showType) {
                    nextShowType = ExtraLyricShowMode.NONE;
                } else if (ExtraLyricShowMode.NONE == showType) {
                    nextShowType = ExtraLyricShowMode.SHOW_TRANSLITERATION;
                } else if (ExtraLyricShowMode.SHOW_TRANSLATION == showType ){
                    // 出错了，音译模式下不能出现显示意译要求
                }

                int resourceId = getExtraLyricButtonResource(ExtraLyricMode.TRANSLITERATION.ordinal(), nextShowType.ordinal());
                if (INVALID_RESOURCE_ID != resourceId) {
                    mTransliterationIV.setBackgroundResource(resourceId);
                }

                if (mPrfMgr.getPlaybackInfo() != null) {
                    mManyLineLyricsView.setExtraLrcStatus(nextShowType, (int) mPrfMgr.getPlaybackInfo().getProgress());
                } else {
                    mManyLineLyricsView.setExtraLrcStatus(nextShowType, 0);
                }

                mPrfMgr.setLrcMultiLine(mManyLineLyricsView.isManyLineLrc());
            }
        });

        mMultiTranslationIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExtraLyricShowMode showType = mManyLineLyricsView.getExtraLrcShowMode();
                ExtraLyricShowMode nextShowType = ExtraLyricShowMode.NONE;

                if (ExtraLyricShowMode.SHOW_TRANSLATION == showType) {
                    nextShowType = ExtraLyricShowMode.SHOW_TRANSLITERATION;
                } else if (ExtraLyricShowMode.SHOW_TRANSLITERATION == showType) {
                    nextShowType = ExtraLyricShowMode.NONE;
                } else if (ExtraLyricShowMode.NONE == showType ){
                    nextShowType = ExtraLyricShowMode.SHOW_TRANSLATION;
                }

                int resourceId = getExtraLyricButtonResource(ExtraLyricMode.BOTH.ordinal(), nextShowType.ordinal());
                if (INVALID_RESOURCE_ID != resourceId) {
                    mMultiTranslationIV.setBackgroundResource(resourceId);
                }

                if (mPrfMgr.getPlaybackInfo() != null) {
                    mManyLineLyricsView.setExtraLrcStatus(nextShowType, (int) mPrfMgr.getPlaybackInfo().getProgress());
                } else {
                    mManyLineLyricsView.setExtraLrcStatus(nextShowType, 0);
                }

                mPrfMgr.setLrcMultiLine(mManyLineLyricsView.isManyLineLrc());
            }
        });

        //-------------------------------- END_INIT [歌词翻译类型按钮] ----------------------------

        mLikeImgBtn = findViewById(R.id.like_button);
        mLikeImgBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // 点击 like 将执行 unlike 操作
                updateLikeUI(getApplicationContext(), false, mLikeImgBtn, mUnLikeImgBtn,
                        true,true, mPrfMgr.getCurrentAudio());
            }
        });
        mUnLikeImgBtn = findViewById(R.id.unlike_button);
        mUnLikeImgBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // 点击 like 将执行 unlike 操作
                updateLikeUI(getApplicationContext(), true, mLikeImgBtn, mUnLikeImgBtn,
                        true,true, mPrfMgr.getCurrentAudio());
            }
        });

        mMoreMenuImgBtn = findViewById(R.id.more_menu);
        mMoreMenuImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLrcSettingPopupShow) {
                    hideLrcSettingPopupView();
                } else {
                    showLrcSettingPopupView();
                }


            }
        });

        RelativeLayout playListMenu = findViewById(R.id.playlistmenu);
        playListMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPopViewShow){
                    hidePopView();
                } else {
                    showPopView();
                }
            }
        });

        initPlayerViews();
        initListPopView();
        initLrcSettingPopupView();

        setStatusBarDecor(this);
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        AudioInfo curAudioInfo = mPrfMgr.getCurrentAudio();
        if (curAudioInfo != null) {
            Intent initIntent = new Intent(EventManager.ACTION_INITMUSIC);
            doAudioReceive(getApplicationContext(), initIntent);
        } else {
            Intent nullIntent = new Intent(EventManager.ACTION_NULLMUSIC);
            doAudioReceive(getApplicationContext(), nullIntent);
        }
    }



    private void doAudioReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(EventManager.ACTION_NULLMUSIC)) {

            mSongNameTextView.setText(R.string.def_songName);
            mSingerNameTextView.setText(R.string.def_artist);

            mPauseBtn.setVisibility(View.INVISIBLE);
            mPlayBtn.setVisibility(View.VISIBLE);

            mSongProgressTv.setText("00:00");
            mSongDurationTv.setText("00:00");


            mLrcSeekBar.setEnabled(false);
            mLrcSeekBar.setProgress(0);
            mLrcSeekBar.setSecondaryProgress(0);
            mLrcSeekBar.setMax(0);

            mManyLineLyricsView.setLyricsUtil(null, 0,0);

            //歌手写真
            mSlidingBackgroundView.setVisibility(View.INVISIBLE);
            //mSlidingBackgroundView.setSongSingerInfos(mPrfMgr, getApplicationContext(), null);


            //重置弹出窗口播放列表
            if (isPopViewShow) {
                if (mPopPlayListAdapter != null) {
                    mPopPlayListAdapter.refreshViewHolder(null);
                }
            }

            //设置喜欢
            mUnLikeImgBtn.setVisibility(View.VISIBLE);
            mLikeImgBtn.setVisibility(View.GONE);

            /*
            //下载
            mDownloadedImgBtn.setVisibility(View.INVISIBLE);
            mDownloadImgBtn.setVisibility(View.VISIBLE);*/

        } else if (action.equals(EventManager.ACTION_INITMUSIC)) {
            //初始化
            PlaybackInfo playbackInfo = mPrfMgr.getPlaybackInfo();
            AudioInfo audioInfo = mPrfMgr.getCurrentAudio();

            mSongNameTextView.setText(audioInfo.getTitle());
            mSingerNameTextView.setText(audioInfo.getArtist());

            if (PlayerManager.Status.PLAYING == mPrfMgr.getPlayStatus()) {
                mPauseBtn.setVisibility(View.VISIBLE);
                mPlayBtn.setVisibility(View.GONE);
            } else {
                mPauseBtn.setVisibility(View.GONE);
                mPlayBtn.setVisibility(View.VISIBLE);
            }

            mSongProgressTv.setText(FileUtil.parseTime2TrackLength((int) playbackInfo.getProgress()));
            mSongDurationTv.setText(FileUtil.parseTime2TrackLength((int) audioInfo.getDuration()));

            mLrcSeekBar.setEnabled(true);
            mLrcSeekBar.setMax((int) audioInfo.getDuration());
            mLrcSeekBar.setProgress((int) playbackInfo.getProgress());
            mLrcSeekBar.setSecondaryProgress(0);

            //加载歌词
            String keyWords = "";
            if (audioInfo.getArtist().equals("未知")) {
                keyWords = audioInfo.getTitle();
            } else {
                keyWords = audioInfo.getArtist() + " - " + audioInfo.getTitle();
            }
            LyricsManager.getLyricsManager(getApplicationContext()).loadLyricsUtil(keyWords, keyWords, audioInfo.getDuration() + "", audioInfo.getHash());

            mManyLineLyricsView.setLyricsUtil(null, 0,0);

            mSlidingBackgroundView.setVisibility(View.INVISIBLE);
            //mSlidingBackgroundView.setSongSingerInfos(mPrfMgr, getApplicationContext(), null);
            //加载歌手写真
            startLoadSingerPortrait(getApplicationContext(), audioInfo.getArtist(), audioInfo.getHash());

            //设置弹出窗口播放列表
            if (isPopViewShow) {
                if (mPopPlayListAdapter != null) {
                    mPopPlayListAdapter.refreshViewHolder(audioInfo);
                }
            }

            //设置喜欢
            boolean isLike = DatabaseHelper.getInstance(getApplicationContext()).isRecentOrLikeExists(audioInfo.getHash(), audioInfo.getType(), false);
            updateLikeUI(getApplicationContext(), isLike, mLikeImgBtn, mUnLikeImgBtn, false, false, null);
            if (isLike) {
                mUnLikeImgBtn.setVisibility(View.GONE);
                mLikeImgBtn.setVisibility(View.VISIBLE);
            } else {
                mUnLikeImgBtn.setVisibility(View.VISIBLE);
                mLikeImgBtn.setVisibility(View.GONE);
            }

            /*if (audioInfo.getType() == AudioInfo.NET || audioInfo.getType() == AudioInfo.DOWNLOAD) {

                //下载
                if (DownloadInfoDB.getAudioInfoDB(getApplicationContext()).isExists(audioInfo.getHash()) || AudioInfoDB.getAudioInfoDB(getApplicationContext()).isNetAudioExists(audioInfo.getHash())) {

                    mDownloadedImgBtn.setVisibility(View.VISIBLE);
                    mDownloadImgBtn.setVisibility(View.INVISIBLE);
                } else {
                    mDownloadedImgBtn.setVisibility(View.INVISIBLE);
                    mDownloadImgBtn.setVisibility(View.VISIBLE);
                }

            } else {
                mDownloadedImgBtn.setVisibility(View.VISIBLE);
                mDownloadImgBtn.setVisibility(View.INVISIBLE);
            }*/

        } else if (action.equals(EventManager.ACTION_SERVICE_PLAYMUSIC)) {
            //播放

            PlaybackInfo playbackInfo = mPrfMgr.getPlaybackInfo();

            mPauseBtn.setVisibility(View.VISIBLE);
            mPlayBtn.setVisibility(View.INVISIBLE);


            mSongProgressTv.setText(FileUtil.parseTime2TrackLength((int) playbackInfo.getProgress()));

            mLrcSeekBar.setProgress((int) playbackInfo.getProgress());

        } else if (action.equals(EventManager.ACTION_SERVICE_PAUSEMUSIC)) {
            //暂停完成
            mPauseBtn.setVisibility(View.INVISIBLE);
            mPlayBtn.setVisibility(View.VISIBLE);
        } else if (action.equals(EventManager.ACTION_SERVICE_RESUMEMUSIC)) {
            //唤醒完成
            mPauseBtn.setVisibility(View.VISIBLE);
            mPlayBtn.setVisibility(View.INVISIBLE);

        } else if (action.equals(EventManager.ACTION_SERVICE_PLAYINGMUSIC)) {
            //播放中
            PlaybackInfo playbackInfo = mPrfMgr.getPlaybackInfo();
            if (playbackInfo != null) {
                mSongProgressTv.setText(FileUtil.parseTime2TrackLength((int) playbackInfo.getProgress()));
                mLrcSeekBar.setProgress((int) playbackInfo.getProgress());
                AudioInfo audioInfo = mPrfMgr.getCurrentAudio();
                if (audioInfo != null) {
                    //更新歌词
                    if (mManyLineLyricsView.getLyricsUtil() != null && mManyLineLyricsView.getLyricsUtil().getHash().equals(audioInfo.getHash())) {
                        mManyLineLyricsView.updateView((int) playbackInfo.getProgress());
                    }
                }

            }

        }
//        else if (action.equals(EventManager.ACTION_MUSICRESTART)) {
        //重新启动播放服务
//            Intent playerServiceIntent = new Intent(this, AudioPlayerService.class);
//            mPrfMgr.startService(playerServiceIntent);
//            logger.e("接收广播并且重新启动音频播放服务");

//        }
        else if (action.equals(EventManager.ACTION_LRCLOADED)) {
            //歌词加载完成
            PlaybackInfo curPlaybackInfo = mPrfMgr.getPlaybackInfo();
            PlaybackInfo savedPlaybackInfo = (PlaybackInfo) intent.getSerializableExtra(PlaybackInfo.KEY);
            String hash = savedPlaybackInfo.getHash();
            if (hash.equals(mPrfMgr.getCurrentAudio().getHash())) {
                //
                LyricsUtil lyricsUtil = LyricsManager.getLyricsManager(getApplicationContext()).getLyricsUtil(hash);
                if (lyricsUtil != null) {
                    lyricsUtil.setHash(hash);
                    mManyLineLyricsView.setLyricsUtil(lyricsUtil, mScreenWidth / 3 * 2,(int) curPlaybackInfo.getProgress());
                    mManyLineLyricsView.updateView((int) curPlaybackInfo.getProgress());
                }
            }

        } else if (action.equals(EventManager.ACTION_LRCSEEKTO)) {
            //歌词快进
            if (mPrfMgr.getPlaybackInfo() != null) {
                mSongProgressTv.setText(FileUtil.parseTime2TrackLength((int) mPrfMgr.getPlaybackInfo().getProgress()));
                mLrcSeekBar.setProgress((int) mPrfMgr.getPlaybackInfo().getProgress());
                if (mPrfMgr.getCurrentAudio() != null) {
                    if (mManyLineLyricsView.getLyricsUtil() != null && mManyLineLyricsView.getLyricsUtil().getHash().equals(mPrfMgr.getCurrentAudio().getHash())) {
                        mManyLineLyricsView.updateView((int) mPrfMgr.getPlaybackInfo().getProgress());
                    }
                }
            }
        } else if (action.equals(EventManager.ACTION_RELOADSINGERIMG)) {
            //重新加载歌手写真
            if (mPrfMgr.getCurrentAudio() != null) {
                String hash = intent.getStringExtra("hash");
                if (mPrfMgr.getCurrentAudio().getHash().equals(hash)) {
                    String singerName = intent.getStringExtra("singerName");
                    mSlidingBackgroundView.setVisibility(View.INVISIBLE);
                    //mSlidingBackgroundView.setSongSingerInfos(mPrfMgr, getApplicationContext(), null);
                    //加载歌手写真
                    //ImageUtil.loadSingerImg(mPrfMgr, getApplicationContext(), hash, singerName);

                }
            }

        } else if (action.equals(EventManager.ACTION_SINGERIMGLOADED)) {
            //歌手写真加载完成
            if (mPrfMgr.getCurrentAudio() != null) {
                String hash = intent.getStringExtra("hash");
                if (mPrfMgr.getCurrentAudio().getHash().equals(hash)) {
                    mSlidingBackgroundView.setVisibility(View.VISIBLE);

                    String singerName = intent.getStringExtra("singerName");
                    String[] singerNameArray = null;
                    if (singerName.contains("、")) {

                        String regex = "\\s*、\\s*";
                        singerNameArray = singerName.split(regex);


                    } else {
                        singerNameArray = new String[1];
                        singerNameArray[0] = singerName;
                    }


                    //设置数据
                    /*List<SongSingerInfo> list = SongSingerDB.getSongSingerDB(context).getAllSingerImg(singerNameArray, false);
                    mSlidingBackgroundView.setSongSingerInfos(mPrfMgr, getApplicationContext(), list);*/
                    //mSlidingBackgroundView.startBackgroundImageSliding(new Drawable[]{}, 0);
                }
            }
        }
    }

    /**
     * 快进播放
     *
     * @param progress
     * @param isLrcSeekTo
     */
    private void seekToMusic(int progress, boolean isLrcSeekTo) {
        mPrfMgr.setLrcSeekTo(isLrcSeekTo);
        //判断歌词快进时，是否超过歌曲的总时间

        long progressSaved = mPrfMgr.getCurrentAudio().getDuration();
        if (progressSaved < progress) {
            progress = (int) progressSaved;
        }

        if (mPrfMgr.getPlayStatus() == PlayerManager.Status.PLAYING) {
            //正在播放
            PlaybackInfo playbackInfo = mPrfMgr.getPlaybackInfo();
            if (playbackInfo != null) {
                playbackInfo.setProgress(progress);
                Intent resumeIntent = new Intent(EventManager.ACTION_SEEKTOMUSIC);
                resumeIntent.putExtra(PlaybackInfo.KEY, playbackInfo);
                resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(resumeIntent);
            }
        } else {
            if (mPrfMgr.getPlaybackInfo() != null) {
                mPrfMgr.getPlaybackInfo().setProgress(progress);
            }
            //歌词快进
            Intent lrcSeektoIntent = new Intent(EventManager.ACTION_LRCSEEKTO);
            lrcSeektoIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            sendBroadcast(lrcSeektoIntent);


        }
    }

    @Override
    public void onBackPressed() {

        if (isPopViewShow) {
            hidePopView();
            return;
        } else if (isLrcSettingPopupShow) {
            hideLrcSettingPopupView();
            return;
        }
        /*if (isPopViewShow) {
            hidePopView();
            return;
        } else if (isPLPopViewShow) {
            hidePlPopView();
            return;
        } else if (isSPLPopViewShow) {
            hideSPLPopView();
            return;
        } else if (isSIPopViewShow) {
            hideSIPopView();
            return;
        }*/
        mRotateContainer.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mEventManager.release();
    }

    @Override
    public void decorStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //透明状态栏
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //设置状态栏的颜色
            window.setStatusBarColor(Color.TRANSPARENT);

            View statusBarView = new View(getApplicationContext());
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(getApplicationContext()));
            statusBarView.setBackgroundColor(Color.TRANSPARENT);

            ViewGroup newlayout = mContentView.findViewById(R.id.lrc_layout);
            newlayout.addView(statusBarView, 0, lp);

        }
    }

    private int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private View mContentView;

    private void startLoadSingerPortrait(final Context context, String singerName, String hash) {

        new AsyncTask<String, Void, List<Bitmap>>() {

            @Override
            protected void onPostExecute(List<Bitmap> result) {

                Drawable[] bgData = new Drawable[result.size()];
                for (int i=0; i<result.size(); i++) {
                    bgData[i] = new BitmapDrawable(result.get(i));
                }
                AudioPlayActivity.this.mSlidingBackgroundView.setVisibility(View.VISIBLE);
                AudioPlayActivity.this.mSlidingBackgroundView.startBackgroundImageSliding(bgData, 0);
            }

            @Override
            protected List<Bitmap> doInBackground(String... strings) {
                String singerName = strings[0];
                String hash = strings[1];

                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                int screensWidth = display.getWidth();
                int screensHeight = display.getHeight();

                String[] singerNameArray = null;
                if (singerName.contains("、")) {
                    String regex = "\\s*、\\s*";
                    singerNameArray = singerName.split(regex);
                } else {
                    singerNameArray = new String[1];
                    singerNameArray[0] = singerName;
                }

                List<SongSingerInfo> list = SongSingerDB.getInstance(context).getAllSingerImg(singerNameArray, true);
                if (list == null || list.size() == 0) {

                    for (int i = 0; i < singerNameArray.length; i++) {
                        String searchSingerName = singerNameArray[i];
                        if (SongSingerDB.getInstance(context).getAllImgUrlCount(searchSingerName) > 0) {
                            continue;
                        }

                        List<SingerPortraitInfo> data = new SingerPortraitURLGetter().getUrl(searchSingerName, "app", screensWidth, screensHeight);
                        if (data != null && data.size() > 0)
                            for (int j = 0; j < data.size(); j++) {
                                if (j > 3) {
                                    break;
                                }
                                SingerPortraitInfo singerPortraitInfo = data.get(j);
                                if (!SongSingerDB.getInstance(context).isExists(hash, singerPortraitInfo.mPortraitUrl)) {

                                    SongSingerInfo songSingerInfo = new SongSingerInfo();
                                    songSingerInfo.setHash(hash);
                                    songSingerInfo.setImgUrl(singerPortraitInfo.mPortraitUrl);
                                    songSingerInfo.setSingerName(searchSingerName);

                                    SongSingerDB.getInstance(context).add(songSingerInfo);

                                    list.add(songSingerInfo);
                                }
                            }
                    }

                }

                List<Bitmap> result = new ArrayList<>();
                //预加载图片
                for (int i = 0; i < list.size(); i++) {
                    SongSingerInfo songSingerInfo = list.get(i);

                    RequestOptions options = new RequestOptions();
                    options.centerCrop();
                    try {
                        Bitmap myBitmap = Glide.with(context)
                                .asBitmap()
                                .load(songSingerInfo.getImgUrl())
                                .submit(screensWidth, screensHeight)
                                .get();

                        result.add(myBitmap);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    //getSingerImgBitmap(hPApplication, context, songSingerInfo.getHash(), songSingerInfo.getSingerName(), songSingerInfo.getImgUrl(), true);
                }

                return result;

            }
        }.execute(singerName, hash);
    }

    private void initPlayerViews() {

        mSongProgressTv = findViewById(R.id.songProgress);
        mSongDurationTv = findViewById(R.id.songDuration);

        //进度条
        mLrcSeekBar = findViewById(R.id.lrc_seek_bar);
        mLrcSeekBar.setOnChangeListener(new LrcSeekBar.OnChangeListener() {

            @Override
            public void onProgressChanged() {
                int playStatus = mPrfMgr.getPlayStatus();
                if (playStatus != PlayerManager.Status.PLAYING) {
                    mSongProgressTv.setText(FileUtil.parseTime2TrackLength((mLrcSeekBar.getProgress())));
                }
            }

            @Override
            public String getTimeText() {
                return FileUtil.parseTime2TrackLength(mLrcSeekBar.getProgress());
            }

            @Override
            public String getLrcText() {
                return null;
            }

            @Override
            public void dragFinish() {
                seekToMusic(mLrcSeekBar.getProgress(), false);
            }
        });
        //
        mLrcSeekBar.setBackgroundProgressColorColor(getResources().getColor(R.color.lrc_seek_progress_secondary_1));
        mLrcSeekBar.setSecondProgressColor(getResources().getColor(R.color.lrc_seek_progress_secondary_1));
        mLrcSeekBar.setProgressColor(getResources().getColor(R.color.lrc_seek_progress_1));
        mLrcSeekBar.setThumbColor(getResources().getColor(R.color.lrc_seek_thumb_1));
        mLrcSeekBar.setTimePopupWindowViewColor(getResources().getColor(R.color.lrc_seek_progress_1));

        //播放
        mPlayBtn = findViewById(R.id.playbtn);
        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int playStatus = mPrfMgr.getPlayStatus();
                if (playStatus == PlayerManager.Status.PAUSE) {

                    AudioInfo audioInfo = mPrfMgr.getCurrentAudio();
                    if (audioInfo != null) {

                        PlaybackInfo playbackInfo = mPrfMgr.getPlaybackInfo();
                        Intent resumeIntent = new Intent(EventManager.ACTION_RESUMEMUSIC);
                        resumeIntent.putExtra(PlaybackInfo.KEY, playbackInfo);
                        resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        sendBroadcast(resumeIntent);
                    }

                } else {
                    if (mPrfMgr.getPlaybackInfo() != null) {
                        PlaybackInfo playbackInfo = mPrfMgr.getPlaybackInfo();
                        AudioInfo audioInfo = mPrfMgr.getCurrentAudio();
                        if (audioInfo != null) {
                            playbackInfo.setAudioInfo(audioInfo);
                            Intent resumeIntent = new Intent(EventManager.ACTION_PLAYMUSIC);
                            resumeIntent.putExtra(PlaybackInfo.KEY, playbackInfo);
                            resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            sendBroadcast(resumeIntent);
                        }
                    }
                }
            }
        });
        //暂停
        mPauseBtn = findViewById(R.id.pausebtn);
        mPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int playStatus = mPrfMgr.getPlayStatus();
                if (playStatus == PlayerManager.Status.PLAYING) {

                    Intent resumeIntent = new Intent(EventManager.ACTION_PAUSEMUSIC);
                    resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(resumeIntent);

                }
            }
        });

        //下一首
        mNextBtn = findViewById(R.id.nextbtn);
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                Intent nextIntent = new Intent(EventManager.ACTION_NEXTMUSIC);
                nextIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(nextIntent);
            }
        });

        //上一首
        mPreBtn = findViewById(R.id.prebtn);
        mPreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                Intent nextIntent = new Intent(EventManager.ACTION_PREMUSIC);
                nextIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(nextIntent);
            }
        });

        /////////播放模式//////////////

        mPlayModeIV = findViewById(R.id.play_mode_iv);
        mPlayModeIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentModeValue = mPrfMgr.getPlayMode();
                updatePlayModeView(getApplicationContext(), PlayMode.getMode(currentModeValue).getNextModeValue(),
                        true, true);
            }
        });
        updatePlayModeView(getApplicationContext(), mPrfMgr.getPlayMode(), false, false);


    }

    private View mLikeImgBtn;
    private View mUnLikeImgBtn;
    private static void updateLikeUI(Context context, boolean like, View likeView, View unLikeView,
                             boolean showTips, boolean notify, @Nullable AudioInfo audioInfo) {
        if (like) {
            likeView.setVisibility(View.VISIBLE);
            unLikeView.setVisibility(View.INVISIBLE);
        } else {
            likeView.setVisibility(View.INVISIBLE);
            unLikeView.setVisibility(View.VISIBLE);
        }

        if (showTips) {
            ToastUtil.show(context, like ? R.string.tips_add_like : R.string.tips_remove_like);
        }

        if (notify && null != audioInfo) {
            String action = like ? EventManager.ACTION_LIKEADD : EventManager.ACTION_LIKEDELETE;

            Intent intent = new Intent(action);
            intent.putExtra(AudioInfo.KEY, audioInfo);
            intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            context.sendBroadcast(intent);
        }
    }

    private void updatePlayModeView(Context context, int playModeValue, boolean isTipShow, boolean updatePreference) {
        PlayMode mode = PlayMode.getMode(playModeValue);
        mPlayModeIV.setImageResource(mode.getIconResId(true));

        String modeName = mode.getName(context);
        if (isTipShow) {
            ToastUtil.show(context, modeName);
        }

        if (updatePreference) {
            mPrfMgr.setPlayMode(playModeValue);
        }
    }

    // --------------------------- Begin Playlist Popup ---------------------------

    /**
     * 弹出窗口是否显示
     */
    private boolean isPopViewShow = false;
    /**
     * 弹出窗口全屏界面
     */
    private LinearLayout mListPopLinearLayout;
    /**
     * 弹出视图
     */
    private RelativeLayout mPopMenuRelativeLayout;
    /**
     * 当前播放列表
     */
    private LinearLayoutRecyclerView mCurRecyclerView;

    private MainPopPlayListAdapter mPopPlayListAdapter;
    /**
     * 当前播放列表歌曲总数
     */
    //private TextView mCurPLSizeTv;

    private ImageView mPopPlayModeIv;
    private TextView mPopPlayModeTipsTv;
    private ImageView mPopDeleteIv;

    private RequestManager mSingerImageRequestManager;
    private RequestOptions mSingerImageOptions;

    private void initListPopView() {
        mListPopLinearLayout = findViewById(R.id.list_pop);
        mListPopLinearLayout.setVisibility(View.INVISIBLE);
        mListPopLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hidePopView();
            }
        });

        mPopMenuRelativeLayout = findViewById(R.id.pop_parent);

        // 播放模式
        mPopPlayModeIv = findViewById(R.id.popup_play_mode_iv);
        mPopPlayModeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentModeValue = mPrfMgr.getPlayMode();
                updatePlayModeView(PlayMode.getMode(currentModeValue).getNextModeValue(), true, true);
            }
        });

        mPopPlayModeTipsTv = findViewById(R.id.popup_play_mode_tv);

        //删除播放列表按钮
        mPopDeleteIv = findViewById(R.id.popup_delete_iv);
        mPopDeleteIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mCurRecyclerView = findViewById(R.id.curplaylist_recyclerView);
        mCurRecyclerView.setLinearLayoutManager(new LinearLayoutManager(getApplicationContext()));

        updatePlayModeView(mPrfMgr.getPlayMode(),false, false);
    }

    /**
     * 初始化播放列表播放模式
     */
    private void updatePlayModeView(int playModeValue, boolean isTipShow, boolean updatePreference) {
        PlayMode mode = PlayMode.getMode(playModeValue);
        mPopPlayModeIv.setImageResource(mode.getIconResId(false));

        String modeName = mode.getName(getApplicationContext());
        List<AudioInfo> currentPlaylist = mPrfMgr.getCurrentPlaylist();
        int size = currentPlaylist == null ? 0 : currentPlaylist.size();
        mPopPlayModeTipsTv.setText(modeName + " (" + size + ")");
        if (isTipShow) {
            ToastUtil.show(getApplicationContext(), modeName);
        }

        if (updatePreference) {
            mPrfMgr.setPlayMode(playModeValue);
        }
    }


    /**
     * 隐藏popview
     */
    private void hidePopView() {
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, mPopMenuRelativeLayout.getHeight());
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mListPopLinearLayout.setBackgroundColor(ColorUtils.setAlphaComponent(Color.BLACK, 0));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isPopViewShow = false;
                mListPopLinearLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mPopMenuRelativeLayout.clearAnimation();
        mPopMenuRelativeLayout.setAnimation(translateAnimation);
        translateAnimation.start();
    }

    /**
     * 显示popview
     */
    private void showPopView() {

        //updatePlayModeView(mHPApplication.getPlayModel(), modeAllTv, modeRandomTv, modeSingleTv, false);
        //加载当前播放列表数据
        List<AudioInfo> currentPlaylist = mPrfMgr.getCurrentPlaylist();
        if (currentPlaylist == null) {
            currentPlaylist = new ArrayList<AudioInfo>();
        }
        //mCurPLSizeTv.setText(currentPlaylist.size() + "");
        mPopPlayListAdapter = new MainPopPlayListAdapter(getApplicationContext(), currentPlaylist,
                mSingerImageRequestManager, mSingerImageOptions );
        mCurRecyclerView.setAdapter(mPopPlayListAdapter);


        //
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, mPopMenuRelativeLayout.getHeight(), 0);
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mListPopLinearLayout.setBackgroundColor(ColorUtils.setAlphaComponent(Color.BLACK, 0));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mListPopLinearLayout.setBackgroundColor(ColorUtils.setAlphaComponent(Color.BLACK, 120));

                //滚动到当前播放位置
                int position = mPopPlayListAdapter.getPlayIndexPosition(mPrfMgr.getCurrentAudio());
                if (position >= 0)
                    mCurRecyclerView.move(position,
                            LinearLayoutRecyclerView.smoothScroll);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mListPopLinearLayout.setVisibility(View.VISIBLE);
        mPopMenuRelativeLayout.clearAnimation();
        mPopMenuRelativeLayout.setAnimation(translateAnimation);
        translateAnimation.start();
        isPopViewShow = true;
    }

    // --------------------------- End Playlist Popup ---------------------------


    // --------------------------- Begin LyricSetting Popup ---------------------------

    private boolean isLrcSettingPopupShow = false;
    private LinearLayout mLrcSettingPopLinearLayout;
    private LinearLayout mLrcSettingMenuLayout;

    private void hideLrcSettingPopupView() {
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, mLrcSettingMenuLayout.getHeight());
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                isLrcSettingPopupShow = false;
                mLrcSettingPopLinearLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mLrcSettingMenuLayout.clearAnimation();
        mLrcSettingMenuLayout.setAnimation(translateAnimation);
        translateAnimation.start();
    }

    private void showLrcSettingPopupView() {
        mLrcSettingPopLinearLayout.setVisibility(View.VISIBLE);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, mLrcSettingMenuLayout.getHeight(), 0);
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        mLrcSettingMenuLayout.clearAnimation();
        mLrcSettingMenuLayout.setAnimation(translateAnimation);
        translateAnimation.start();
        isLrcSettingPopupShow = true;
    }

    /**
     * 初始化pop
     */
    private void initLrcSettingPopupView() {
        mLrcSettingPopLinearLayout = findViewById(R.id.lrcPopLayout);
        mLrcSettingPopLinearLayout.setVisibility(View.INVISIBLE);
        mLrcSettingPopLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLrcSettingPopupView();
            }
        });
        //
        mLrcSettingMenuLayout = findViewById(R.id.menuLayout);
        //
        LinearLayout cancelLinearLayout = findViewById(R.id.calcel);
        cancelLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLrcSettingPopupView();
            }
        });


        //搜索歌手写真
        ImageView searchSingerImg = findViewById(R.id.search_singer_pic);
        searchSingerImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPrfMgr.getCurrentAudio() == null) {
                    ToastUtil.show(getApplicationContext(), "请选择歌曲");
                } else {
                    hideLrcSettingPopupView();
                    //歌手名称
                    String singerName = mPrfMgr.getCurrentAudio().getArtist();
                    if (singerName.contains("、")) {
                        String regex = "\\s*、\\s*";
                        String[] singerNameArray = singerName.split(regex);
                        //showSPLPopView(singerNameArray);
                    } else {
                        /*Intent intent = new Intent(AudioPlayActivity.this, SearchSingerActivity.class);
                        intent.putExtra("singerName", singerName);
                        startActivity(intent);
                        //
                        overridePendingTransition(0, 0);*/
                    }
                }
            }
        });

        //搜索歌词
        ImageView searchLrcImg = findViewById(R.id.search_lrc);
        searchLrcImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPrfMgr.getCurrentAudio() == null) {
                    ToastUtil.show(getApplicationContext(), "请选择歌曲");
                } else {
                    hideLrcSettingPopupView();
                    /*Intent intent = new Intent(AudioPlayActivity.this, SearchLrcActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_bottom, 0);*/
                }

            }
        });

        //歌曲详情
        ImageView songInfoImg = findViewById(R.id.songinfo);
        songInfoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPrfMgr.getCurrentAudio() == null) {
                    ToastUtil.show(getApplicationContext(), "请选择歌曲");
                } else {
                    hideLrcSettingPopupView();
                    //showSPIPopView(mPrfMgr.getCurrentAudio());
                }
            }
        });

        //歌词进度减少按钮
        RelativeLayout lrcProgressJianBtn = findViewById(R.id.lyric_progress_jian);

        lrcProgressJianBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mManyLineLyricsView.getLyricsUtil() != null) {
                    mManyLineLyricsView.getLyricsUtil().setOffset(mManyLineLyricsView.getLyricsUtil().getOffset() + (-500));
                    ToastUtil.show(getApplicationContext(), (float) mManyLineLyricsView.getLyricsUtil().getOffset() / 1000 + "秒");
                    if (mManyLineLyricsView.getLyricsLineTreeMap() != null) {

                        //保存歌词文件
                        saveLrcFile(mManyLineLyricsView.getLyricsUtil().getLrcFilePath(), mManyLineLyricsView.getLyricsUtil().getLyricInfo(), mManyLineLyricsView.getLyricsUtil().getPlayOffset());

                    }
                }
            }
        });
        //歌词进度重置
        RelativeLayout resetProgressJianBtn = findViewById(R.id.lyric_progress_reset);
        resetProgressJianBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mManyLineLyricsView.getLyricsUtil() != null) {
                    mManyLineLyricsView.getLyricsUtil().setOffset(0);
                    ToastUtil.show(getApplicationContext(), "还原了");
                    if (mManyLineLyricsView.getLyricsLineTreeMap() != null) {

                        //保存歌词文件
                        saveLrcFile(mManyLineLyricsView.getLyricsUtil().getLrcFilePath(), mManyLineLyricsView.getLyricsUtil().getLyricInfo(), mManyLineLyricsView.getLyricsUtil().getPlayOffset());

                    }
                }
            }
        });
        //歌词进度增加
        RelativeLayout lrcProgressJiaBtn = findViewById(R.id.lyric_progress_jia);
        lrcProgressJiaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mManyLineLyricsView.getLyricsUtil() != null) {
                    mManyLineLyricsView.getLyricsUtil().setOffset(mManyLineLyricsView.getLyricsUtil().getOffset() + (500));
                    ToastUtil.show(getApplicationContext(), (float) mManyLineLyricsView.getLyricsUtil().getOffset() / 1000 + "秒");
                    if (mManyLineLyricsView.getLyricsLineTreeMap() != null) {

                        //保存歌词文件
                        saveLrcFile(mManyLineLyricsView.getLyricsUtil().getLrcFilePath(), mManyLineLyricsView.getLyricsUtil().getLyricInfo(), mManyLineLyricsView.getLyricsUtil().getPlayOffset());

                    }
                }
            }
        });


        //字体大小
        final LrcSeekBar lrcSizeLrcSeekBar = findViewById(R.id.fontSizeseekbar);
        lrcSizeLrcSeekBar.setMax(mPrfMgr.getMaxLrcFontSize() - mPrfMgr.getMinLrcFontSize());
        lrcSizeLrcSeekBar.setProgress((mPrfMgr.getLrcFontSize() - mPrfMgr.getMinLrcFontSize()));
        //lrcSizeLrcSeekBar.setBackgroundProgressColorColor(ColorUtil.parserColor(Color.WHITE, 50));
        lrcSizeLrcSeekBar.setProgressColor(Color.WHITE);
        lrcSizeLrcSeekBar.setThumbColor(Color.WHITE);
        lrcSizeLrcSeekBar.setOnChangeListener(new LrcSeekBar.OnChangeListener() {
            @Override
            public void onProgressChanged() {
                if (mManyLineLyricsView.getLyricsUtil() != null) {
                    if (mManyLineLyricsView.getLyricsLineTreeMap() != null) {
                        PlaybackInfo playbackInfo = mPrfMgr.getPlaybackInfo();
                        if (playbackInfo != null) {
                            mManyLineLyricsView.setLrcFontSize(
                                    lrcSizeLrcSeekBar.getProgress() + mPrfMgr.getMinLrcFontSize(),
                                    (int) playbackInfo.getProgress());
                        }
                    }
                } else {
                    mManyLineLyricsView.setLrcFontSize(lrcSizeLrcSeekBar.getProgress() + mPrfMgr.getMinLrcFontSize());
                }
            }

            @Override
            public String getTimeText() {
                return null;
            }

            @Override
            public String getLrcText() {
                return null;
            }

            @Override
            public void dragFinish() {
                mPrfMgr.setLrcFontSize(lrcSizeLrcSeekBar.getProgress() + mPrfMgr.getMinLrcFontSize());
            }
        });

        //字体减小
        ImageView lrcSizeDecrease = findViewById(R.id.lyric_decrease);
        lrcSizeDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int curProgress = lrcSizeLrcSeekBar.getProgress();
                curProgress -= 2;
                if (curProgress < 0) {
                    curProgress = 0;
                }
                lrcSizeLrcSeekBar.setProgress(curProgress);
            }
        });

        //字体增加
        ImageView lrcSizeIncrease = findViewById(R.id.lyric_increase);
        lrcSizeIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int curProgress = lrcSizeLrcSeekBar.getProgress();
                curProgress += 2;
                if (curProgress > lrcSizeLrcSeekBar.getMax()) {
                    curProgress = lrcSizeLrcSeekBar.getMax();
                }
                lrcSizeLrcSeekBar.setProgress(curProgress);
            }
        });

        //歌词颜色面板
        ImageView[] colorPanel = new ImageView[mPrfMgr.getLrcColorStr().length];
        final ImageView[] colorStatus = new ImageView[colorPanel.length];

        int i = 0;
        //
        colorPanel[i] = findViewById(R.id.color_panel1);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = mPrfMgr.getLrcColorIndex();
                if (index != 0) {
                    mPrfMgr.setLrcColorIndex(0);
                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[0].setVisibility(View.VISIBLE);

                    int lrcColor = Color.parseColor(mPrfMgr.getLrcColorStr()[mPrfMgr.getLrcColorIndex()]);
                    mManyLineLyricsView.setLrcColor(lrcColor);
                }
            }
        });
        colorStatus[i] = findViewById(R.id.color_status1);

        //
        i++;
        colorPanel[i] = findViewById(R.id.color_panel2);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = mPrfMgr.getLrcColorIndex();
                if (index != 1) {
                    mPrfMgr.setLrcColorIndex(1);
                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[1].setVisibility(View.VISIBLE);


                    int lrcColor = Color.parseColor(mPrfMgr.getLrcColorStr()[mPrfMgr.getLrcColorIndex()]);
                    mManyLineLyricsView.setLrcColor(lrcColor);

                }
            }
        });
        colorStatus[i] = findViewById(R.id.color_status2);

        //
        i++;
        colorPanel[i] = findViewById(R.id.color_panel3);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = mPrfMgr.getLrcColorIndex();
                if (index != 2) {
                    mPrfMgr.setLrcColorIndex(2);
                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[2].setVisibility(View.VISIBLE);

                    int lrcColor = Color.parseColor(mPrfMgr.getLrcColorStr()[mPrfMgr.getLrcColorIndex()]);
                    mManyLineLyricsView.setLrcColor(lrcColor);
                }
            }
        });
        colorStatus[i] = findViewById(R.id.color_status3);

        //
        i++;
        colorPanel[i] = findViewById(R.id.color_panel4);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = mPrfMgr.getLrcColorIndex();
                if (index != 3) {
                    mPrfMgr.setLrcColorIndex(3);
                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[3].setVisibility(View.VISIBLE);

                    int lrcColor = Color.parseColor(mPrfMgr.getLrcColorStr()[mPrfMgr.getLrcColorIndex()]);
                    mManyLineLyricsView.setLrcColor(lrcColor);
                }
            }
        });
        colorStatus[i] = findViewById(R.id.color_status4);

        //
        i++;
        colorPanel[i] = findViewById(R.id.color_panel5);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = mPrfMgr.getLrcColorIndex();
                if (index != 4) {
                    mPrfMgr.setLrcColorIndex(4);
                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[4].setVisibility(View.VISIBLE);

                    int lrcColor = Color.parseColor(mPrfMgr.getLrcColorStr()[mPrfMgr.getLrcColorIndex()]);
                    mManyLineLyricsView.setLrcColor(lrcColor);
                }
            }
        });
        colorStatus[i] = findViewById(R.id.color_status5);

        //
        i++;
        colorPanel[i] = findViewById(R.id.color_panel6);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = mPrfMgr.getLrcColorIndex();
                if (index != 5) {
                    mPrfMgr.setLrcColorIndex(5);
                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[5].setVisibility(View.VISIBLE);

                    int lrcColor = Color.parseColor(mPrfMgr.getLrcColorStr()[mPrfMgr.getLrcColorIndex()]);
                    mManyLineLyricsView.setLrcColor(lrcColor);
                }
            }
        });
        colorStatus[i] = findViewById(R.id.color_status6);

        //
        colorStatus[mPrfMgr.getLrcColorIndex()].setVisibility(View.VISIBLE);

    }

    // --------------------------- End LyricSetting Popup ---------------------------

    private void saveLrcFile(final String lrcFilePath, final LyricsInfo lyricsInfo, final int playOffset) {
        new Thread() {

            @Override
            public void run() {

                Map<String, Object> tags = lyricsInfo.getLyricsTags();

                tags.put(LyricTag.TAG_OFFSET, playOffset);
                lyricsInfo.setLyricsTags(tags);


                //保存修改的歌词文件
                try {
                    LyricsIOUtils.getLyricsFileWriter(lrcFilePath).writer(lyricsInfo, lrcFilePath);
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

        }.start();
    }
}
