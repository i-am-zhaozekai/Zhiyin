package com.haha.zy.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haha.zy.R;
import com.haha.zy.adapter.MainPopPlayListAdapter;
import com.haha.zy.audio.AudioInfo;
import com.haha.zy.player.EventManager;
import com.haha.zy.player.MediaPlaybackService;
import com.haha.zy.player.PlayMode;
import com.haha.zy.player.PlaybackInfo;
import com.haha.zy.player.PlayerManager;
import com.haha.zy.preference.PreferenceManager;
import com.haha.zy.util.FileUtil;
import com.haha.zy.util.ToastUtil;
import com.haha.zy.widget.LinearLayoutRecyclerView;
import com.haha.zy.widget.LrcSeekBar;
import com.jaeger.library.StatusBarUtil;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 06/05/2018
 */

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,
        BaseActivity.StatusBarDecor {

    private Context mContext;
    private PreferenceManager mPrefMgr;

    private DrawerLayout mDrawer;
    private Toolbar mToolbar;

    @Override
    protected void init() {
        mContext = getApplicationContext();
        mPrefMgr = PreferenceManager.getInstance(mContext);
    }

    @Override
    protected int setContentViewId() {
        return R.layout.act_main;
    }

    @Override
    protected void initViews(Bundle savedInstanceState, final View contentRoot) {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mDrawer = findViewById(R.id.drawer_layout);
        mDrawer.setScrimColor(Color.TRANSPARENT);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        setStatusBarDecor(this);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initPlayerViews();

        initListPopView();

        initService();
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        super.loadData(isRestoreInstance);

        PlayerManager.getInstance(getApplicationContext()).initSongInfoData();


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void decorStatusBar() {
        int statusBarColor = getResources().getColor(R.color.colorStatusBarDefault);
        StatusBarUtil.setColor(this, statusBarColor, 0);
        StatusBarUtil.setColorForDrawerLayout(this, mDrawer, statusBarColor, 0);
    }


    private View mPlayerBarContainer = null;
    private RoundedImageView mSingerImageView = null;
    private ImageView mPlayButton = null;
    private ImageView mPauseButton = null;

    private ImageView mNextButton;
    private LrcSeekBar mLrcSeekBar;

    private void initPlayerViews() {

        //
        mPlayerBarContainer = findViewById(R.id.player_bar_container);

        mArtistNameTV = mPlayerBarContainer.findViewById(R.id.artist_name_tv);
        mAudioTitleTV = mPlayerBarContainer.findViewById(R.id.audio_name_iv);
        mSingerImageView = mPlayerBarContainer.findViewById(R.id.artist_portrait_iv);

        /*mSwipeoutLayout = findViewById(R.id.playerBar);
        mSwipeoutLayout.setBackgroundColor(ColorUtil.parserColor("#ffffff", 245));
        ViewGroup barContentView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.layout_main_player_content, null);
        barContentView.setBackgroundColor(Color.TRANSPARENT);

        ViewGroup barMenuView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.layout_main_player_menu, null);
        barMenuView.setBackgroundColor(Color.TRANSPARENT);
        //
        mFloatLyricsView = barMenuView.findViewById(R.id.floatLyricsView);

        //歌手头像
        mSingerImg = barContentView.findViewById(R.id.play_bar_artist);
        mSingerImg.setTag(null);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.singer_def);
        mSingerImg.setImageDrawable(new BitmapDrawable(bitmap));


        mBarCloseFlagView = barContentView.findViewById(R.id.bar_dragflagClosed);
        mBarOpenFlagView = barContentView.findViewById(R.id.bar_dragflagOpen);*/

        /*mSwipeoutLayout.setPlayerBarOnClickListener(new SwipeoutLayout.PlayerBarOnClickListener() {
            @Override
            public void onClick() {

                if (isPopViewShow) {
                    hidePopView();
                    return;
                }

                //设置底部点击后，下沉动画
                TranslateAnimation transAnim = new TranslateAnimation(0, 0, 0, mPlayerBarParentLinearLayout.getHeight());
                transAnim.setDuration(500);
                transAnim.setFillAfter(true);
                mPlayerBarParentLinearLayout.setAnimation(transAnim);
                mPlayerBarParentLinearLayout.startAnimation(transAnim);


                //
                Intent intent = new Intent(MainActivity.this, LrcActivity.class);
                startActivityForResult(intent, MAINTOLRCRESULTCODE);
                //去掉动画
                overridePendingTransition(0, 0);
            }
        });

        */

        //播放
        mPlayButton = findViewById(R.id.play_iv);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferenceManager pm = PreferenceManager.getInstance(getApplicationContext());
                int playStatus = pm.getPlayStatus();
                AudioInfo audioInfo = pm.getCurrentAudio();
                PlaybackInfo playbackInfo = pm.getPlaybackInfo();

                if (playStatus == PlayerManager.Status.PAUSE) {
                    if (audioInfo != null) {
                        Intent resumeIntent = new Intent(EventManager.ACTION_RESUMEMUSIC);
                        resumeIntent.putExtra(PlaybackInfo.KEY, playbackInfo);
                        resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        sendBroadcast(resumeIntent);
                    }
                } else {
                    if (playbackInfo != null) {
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
        mPauseButton = findViewById(R.id.pause_iv);
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int playStatus = PreferenceManager.getInstance(getApplicationContext()).getPlayStatus();
                if (playStatus == PlayerManager.Status.PLAYING) {
                    Intent resumeIntent = new Intent(EventManager.ACTION_PAUSEMUSIC);
                    resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(resumeIntent);

                }
            }
        });

        // 下一首
        mNextButton = findViewById(R.id.next_iv);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                Intent nextIntent = new Intent(EventManager.ACTION_NEXTMUSIC);
                nextIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(nextIntent);
            }
        });

        // 播放进度
        mLrcSeekBar = findViewById(R.id.lrc_seek_bar);
        mLrcSeekBar.setOnChangeListener(new LrcSeekBar.OnChangeListener() {

            @Override
            public void onProgressChanged() {

            }

            @Override
            public String getTimeText() {
                return FileUtil.parseTime2TrackLength(mLrcSeekBar.getProgress());
            }

            @Override
            public String getLrcText() {

                //获取行歌词
                //TODO:
                /*if (mFloatLyricsView.getLyricsUtil() != null && mFloatLyricsView.getLyricsUtil().getHash().equals(mHPApplication.getCurAudioMessage().getAudioInfo().getHash())) {
                    return mFloatLyricsView.getLyricsUtil().getLineLrc(mFloatLyricsView.getLyricsLineTreeMap(), mLrcSeekBar.getProgress());
                }*/

                return null;
            }

            @Override
            public void dragFinish() {
                PreferenceManager prefMgr = PreferenceManager.getInstance(getApplicationContext());
                if (PlayerManager.Status.PLAYING == prefMgr.getPlayStatus()) {
                    //正在播放
                    PlaybackInfo playbackInfo = prefMgr.getPlaybackInfo();
                    if (playbackInfo != null) {
                        playbackInfo.setProgress(mLrcSeekBar.getProgress());
                        Intent resumeIntent = new Intent(EventManager.ACTION_SEEKTOMUSIC);
                        resumeIntent.putExtra(PlaybackInfo.KEY, playbackInfo);
                        resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        sendBroadcast(resumeIntent);
                    }
                } else {
                    PlaybackInfo playbackInfo = prefMgr.getPlaybackInfo();
                    if (playbackInfo != null) {
                        playbackInfo.setProgress(mLrcSeekBar.getProgress());
                    }

                    //歌词快进
                    Intent lrcSeektoIntent = new Intent(EventManager.ACTION_LRCSEEKTO);
                    lrcSeektoIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(lrcSeektoIntent);
                }
            }
        });

        // 播放列表菜单
        ImageView listMenuImg = findViewById(R.id.play_list_menu_iv);
        listMenuImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPopViewShow) {
                    hidePopView();
                } else {
                    showPopView();
                }
            }
        });
    }

    private EventManager mEventManager;
    private EventManager.EventListener mEventListener = new EventManager.EventListener() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doAudioReceive(context, intent);
        }
    };

    private TextView mArtistNameTV = null;
    private TextView mAudioTitleTV = null;

    private String mCurPlayIndexHash = "";

    private void doAudioReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(EventManager.ACTION_NULLMUSIC)) {
            //空数据
            mAudioTitleTV.setText(R.string.def_songName);
            mArtistNameTV.setText(R.string.def_artist);
            mPauseButton.setVisibility(View.INVISIBLE);
            mPlayButton.setVisibility(View.VISIBLE);

            //
            mLrcSeekBar.setEnabled(false);
            mLrcSeekBar.setProgress(0);
            mLrcSeekBar.setSecondaryProgress(0);
            mLrcSeekBar.setMax(0);

            //隐藏
            //mSingerImg.setTag(null);

            //
            /*Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.singer_def);
            mSingerImg.setImageDrawable(new BitmapDrawable(bitmap));

            //
            mFloatLyricsView.setLyricsUtil(null);

            //重置弹出窗口播放列表
            */
            if (isPopViewShow) {
                if (mPopPlayListAdapter != null) {
                    mPopPlayListAdapter.refreshViewHolder(null);
                }
            }

        } else if (action.equals(EventManager.ACTION_INITMUSIC)) {
            //初始化
            PlaybackInfo playbackInfo = PreferenceManager.getInstance(getApplicationContext()).getPlaybackInfo();//(AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
            AudioInfo audioInfo = PreferenceManager.getInstance(getApplicationContext()).getCurrentAudio();

            mCurPlayIndexHash = audioInfo.getHash();

            mAudioTitleTV.setText(audioInfo.getTitle());
            mArtistNameTV.setText(audioInfo.getArtist());
            mPauseButton.setVisibility(View.INVISIBLE);
            mPlayButton.setVisibility(View.VISIBLE);

            mLrcSeekBar.setEnabled(true);
            mLrcSeekBar.setMax((int) audioInfo.getDuration());
            mLrcSeekBar.setProgress((int) playbackInfo.getProgress());
            mLrcSeekBar.setSecondaryProgress(0);
            /*
            //加载歌手图片
            ImageUtil.loadSingerImage(mHPApplication, getApplicationContext(), mSingerImg, audioInfo.getSingerName());

            //加载歌词
            String keyWords = "";
            if (audioInfo.getSingerName().equals("未知")) {
                keyWords = audioInfo.getSongName();
            } else {
                keyWords = audioInfo.getSingerName() + " - " + audioInfo.getSongName();
            }
            LyricsManager.getLyricsManager(mHPApplication, getApplicationContext()).loadLyricsUtil(keyWords, keyWords, audioInfo.getDuration() + "", audioInfo.getHash());

            //
            mFloatLyricsView.setLyricsUtil(null);
            */
            //设置弹出窗口播放列表
            if (isPopViewShow) {
                if (mPopPlayListAdapter != null) {
                    mPopPlayListAdapter.refreshViewHolder(audioInfo);
                }
            }

        } else if (action.equals(EventManager.ACTION_SERVICE_PLAYMUSIC)) {
            //播放
            PlaybackInfo playbackInfo = PreferenceManager.getInstance(getApplicationContext()).getPlaybackInfo();//(AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);

            mPauseButton.setVisibility(View.VISIBLE);
            mPlayButton.setVisibility(View.INVISIBLE);
            mLrcSeekBar.setProgress((int) playbackInfo.getProgress());
        } else if (action.equals(EventManager.ACTION_SERVICE_PAUSEMUSIC)) {
            //暂停完成
            mPauseButton.setVisibility(View.INVISIBLE);
            mPlayButton.setVisibility(View.VISIBLE);

        } else if (action.equals(EventManager.ACTION_SERVICE_RESUMEMUSIC)) {
            //唤醒完成
            mPauseButton.setVisibility(View.VISIBLE);
            mPlayButton.setVisibility(View.INVISIBLE);

        } else if (action.equals(EventManager.ACTION_SERVICE_PLAYINGMUSIC)) {
            //播放中
            PlaybackInfo playbackInfo = PreferenceManager.getInstance(getApplicationContext()).getPlaybackInfo();
            if (playbackInfo != null) {
                mLrcSeekBar.setProgress((int) playbackInfo.getProgress());
                AudioInfo audioInfo = PreferenceManager.getInstance(getApplicationContext()).getCurrentAudio();
                if (audioInfo != null) {
                    //更新歌词
                    /*if (mFloatLyricsView.getLyricsUtil() != null && mFloatLyricsView.getLyricsUtil().getHash().equals(audioInfo.getHash())) {
                        mFloatLyricsView.updateView((int) playbackInfo.getProgress());
                    }*/
                }

            }

        } else if (action.equals(EventManager.ACTION_LOCALUPDATE)) {
            //
            //更新当前的播放列表
//            List<AudioInfo> data = AudioInfoDB.getAudioInfoDB(getApplicationContext()).getAllLocalAudio();
//            mHPApplication.setCurAudioInfos(data);

        }
//        else if (action.equals(EventManager.ACTION_MUSICRESTART)) {
        //重新启动播放服务
//            Intent playerServiceIntent = new Intent(this, AudioPlayerService.class);
//            mHPApplication.startService(playerServiceIntent);
//            logger.e("接收广播并且重新启动音频播放服务");

//        }
        else if (action.equals(EventManager.ACTION_LRCLOADED)) {
            //TODO:
            /*if (mHPApplication.getCurAudioMessage() != null && mHPApplication.getCurAudioInfo() != null) {
                //歌词加载完成
                AudioMessage curAudioMessage = mHPApplication.getCurAudioMessage();
                AudioMessage audioMessage = (AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
                String hash = audioMessage.getHash();
                if (hash.equals(mHPApplication.getCurAudioInfo().getHash())) {
                    //
                    LyricsUtil lyricsUtil = LyricsManager.getLyricsManager(mHPApplication, getApplicationContext()).getLyricsUtil(hash);
                    if (lyricsUtil != null) {
                        if (lyricsUtil.getHash() != null && lyricsUtil.getHash().equals(hash) && mFloatLyricsView.getLyricsUtil() != null) {
                            //已加载歌词，不用重新加载
                        } else {
                            lyricsUtil.setHash(hash);
                            mFloatLyricsView.setLyricsUtil(lyricsUtil);
                            mFloatLyricsView.updateView((int) curAudioMessage.getPlayProgress());
                        }
                    }
                }
            }*/
        } else if (action.equals(EventManager.ACTION_LRCSEEKTO)) {
            PreferenceManager prefMgr = PreferenceManager.getInstance(getApplicationContext());
            PlaybackInfo playbackInfo = prefMgr.getPlaybackInfo();
            if (playbackInfo != null) {
                mLrcSeekBar.setProgress((int) playbackInfo.getProgress());

                AudioInfo currentAudio = prefMgr.getCurrentAudio();
                if (currentAudio != null) {
                    /*if (mFloatLyricsView.getLyricsUtil() != null && mFloatLyricsView.getLyricsUtil().getHash().equals(currentAudio.getHash())) {
                        mFloatLyricsView.updateView((int) playbackInfo.getProgress());
                    }*/
                }
            }

        }

    }

    private void initService() {
        Intent playerServiceIntent = new Intent(this, MediaPlaybackService.class);
        startService(playerServiceIntent);

        //注册接收音频播放广播
        mEventManager = new EventManager(getApplicationContext());
        mEventManager.setEventListener(mEventListener);
        mEventManager.init();

       /* //在线音乐广播
        mOnLineAudioReceiver = new OnLineAudioReceiver(getApplicationContext(), mHPApplication);
        mOnLineAudioReceiver.setOnlineAudioReceiverListener(mOnlineAudioReceiverListener);
        mOnLineAudioReceiver.registerReceiver(getApplicationContext());

        //系统广播
        mSystemReceiver = new SystemReceiver(getApplicationContext(), mHPApplication);
        mSystemReceiver.setSystemReceiverListener(mSystemReceiverListener);
        mSystemReceiver.registerReceiver(getApplicationContext());

        //耳机广播
        mPhoneReceiver = new PhoneReceiver(getApplicationContext(), mHPApplication);
        if (mHPApplication.isWire()) {
            mPhoneReceiver.registerReceiver(getApplicationContext());
        }

        //电话监听
        mMobliePhoneReceiver = new MobliePhoneReceiver(getApplicationContext(), mHPApplication);
        mMobliePhoneReceiver.registerReceiver(getApplicationContext());

        //mFragment广播
        mFragmentReceiver = new FragmentReceiver(getApplicationContext(), mHPApplication);
        mFragmentReceiver.setFragmentReceiverListener(mFragmentReceiverListener);
        mFragmentReceiver.registerReceiver(getApplicationContext());

        //
        mCheckServiceHandler.postDelayed(mCheckServiceRunnable, mCheckServiceTime);*/
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
                int currentModeValue = mPrefMgr.getPlayMode();
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

        updatePlayModeView(mPrefMgr.getPlayMode(),false, false);
    }

    /**
     * 初始化播放列表播放模式
     */
    private void updatePlayModeView(int playModeValue, boolean isTipShow, boolean updatePreference) {
        PlayMode mode = PlayMode.getMode(playModeValue);
        mPopPlayModeIv.setImageResource(mode.getIconResId());

        String modeName = mode.getName(mContext);
        List<AudioInfo> currentPlaylist = mPrefMgr.getCurrentPlaylist();
        int size = currentPlaylist == null ? 0 : currentPlaylist.size();
        mPopPlayModeTipsTv.setText(modeName + " (" + size + ")");
        if (isTipShow) {
            ToastUtil.show(mContext, modeName);
        }

        if (updatePreference) {
            mPrefMgr.setPlayMode(playModeValue);
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
        List<AudioInfo> currentPlaylist = mPrefMgr.getCurrentPlaylist();
        if (currentPlaylist == null) {
            currentPlaylist = new ArrayList<AudioInfo>();
        }
        //mCurPLSizeTv.setText(currentPlaylist.size() + "");
        mPopPlayListAdapter = new MainPopPlayListAdapter(getApplicationContext(), currentPlaylist);
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
                int position = mPopPlayListAdapter.getPlayIndexPosition(mPrefMgr.getCurrentAudio());
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

    private static final long DOUBLE_CLICK_BACK_INTERVAL = 2000L;
    private long mFirstBackClickTime = 0L;

    @Override
    public void onBackPressed() {
        if (isPopViewShow) {
            hidePopView();
            return;
        }

        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
            return;
        }

        if ((System.currentTimeMillis() - mFirstBackClickTime) > 2000) {
            ToastUtil.show(mContext, R.string.tips_double_click_back);
            mFirstBackClickTime = System.currentTimeMillis();
        } else {
            // 跳转到桌面
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
