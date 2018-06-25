package com.haha.zy.player;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.haha.zy.R;
import com.haha.zy.audio.AudioInfo;
import com.haha.zy.preference.PreferenceManager;
import com.haha.zy.util.FileUtil;
import com.haha.zy.util.ToastUtil;

import java.io.File;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 02/06/2018
 */

public class MediaPlaybackService extends Service {

    private Context mContext;
    private PreferenceManager mPrefMgr;

    private IjkMediaPlayer mMediaPlayer;

    private EventManager mEventManager;
    private EventManager.EventListener mEventListener = new EventManager.EventListener() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doAudioReceive(context, intent);
        }
    };

    /**
     * 是否正在快进
     */
    private boolean isSeekTo = false;

    private Thread mPlayerThread = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();
        mPrefMgr = PreferenceManager.getInstance(mContext);

        //
        mEventManager = new EventManager(mContext);
        mEventManager.setEventListener(mEventListener);
        mEventManager.init();

        /*
        //注册通知栏广播
        mNotificationReceiver = new NotificationReceiver(getApplicationContext(), mHPApplication);
        mNotificationReceiver.setNotificationReceiverListener(mNotificationReceiverListener);
        mNotificationReceiver.registerReceiver(getApplicationContext());*/


        //初始化通知栏
        //initNotificationView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mEventManager.release();
        releasePlayer();
    }

    private void doAudioReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (EventManager.ACTION_NULLMUSIC.equals(action)) {
            releasePlayer();
            resetPlayData();

        } else if (action.equals(EventManager.ACTION_PLAYMUSIC)) {
            //播放歌曲
            playMusic((PlaybackInfo) intent.getSerializableExtra(PlaybackInfo.KEY));

        } else if (action.equals(EventManager.ACTION_PAUSEMUSIC)) {
            //暂停歌曲
            pauseMusic();
        } else if (action.equals(EventManager.ACTION_RESUMEMUSIC)) {
            //唤醒歌曲
            resumeMusic((PlaybackInfo) intent.getSerializableExtra(PlaybackInfo.KEY));
        } else if (action.equals(EventManager.ACTION_SEEKTOMUSIC)) {
            //歌曲快进
            seekToMusic((PlaybackInfo) intent.getSerializableExtra(PlaybackInfo.KEY));
        } else if (action.equals(EventManager.ACTION_NEXTMUSIC)) {
            //下一首
            nextMusic();
        } else if (action.equals(EventManager.ACTION_PREMUSIC)) {
            //上一首
            preMusic();
        }

        if (action.equals(EventManager.ACTION_NULLMUSIC)
                || action.equals(EventManager.ACTION_INITMUSIC)
                || action.equals(EventManager.ACTION_SINGERPICLOADED)
                || action.equals(EventManager.ACTION_SERVICE_PLAYMUSIC)
                || action.equals(EventManager.ACTION_SERVICE_RESUMEMUSIC)
                || action.equals(EventManager.ACTION_SERVICE_PAUSEMUSIC)) {

            //处理通知栏数据
            Message msg = new Message();
            msg.obj = intent;
            msg.what = 0;
            //TODO:
            //mNotificationHandler.sendMessage(msg);
        }

    }

    private void playMusic(PlaybackInfo playbackInfo) {
        releasePlayer();
        // resetPlayData();

        AudioInfo audioInfo = playbackInfo.getAudioInfo();
        AudioInfo audioSaved = mPrefMgr.getCurrentAudio();

        if (audioSaved != null && audioSaved.getHash().equals(audioInfo.getHash())) {
            // 当前播放的歌曲与记录的一致，不需要更新数据
        } else {
            // 其他情况更新一下当前播放记录
            mPrefMgr.setPlaybackInfo(playbackInfo);
            //设置当前正在播放的歌曲数据
            mPrefMgr.setCurrentAudio(audioInfo);
            //设置当前的播放索引
            mPrefMgr.setCurrentAudioHash(audioInfo.getHash());
        }

        //发送init的广播
        Intent initIntent = new Intent(EventManager.ACTION_INITMUSIC);
        //initIntent.putExtra(PlaybackInfo.KEY, audioMessage);
        initIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(initIntent);

        if (audioInfo.getType() == AudioInfo.LOCAL) {
            //播放本地歌曲
            playLocalMusic(playbackInfo);
        } else {
            String fileName = audioInfo.getArtist() + " - " + audioInfo.getTitle();
            String filePath = FileUtil.getFilePath(mContext,
                    FileUtil.getAppPublicDirectory(mContext, FileUtil.PATH_AUDIO),
                    fileName + "." + audioInfo.getFileExt());

            //设置文件路径
            audioInfo.setFilePath(filePath);
            File audioFile = new File(filePath);
            if (audioFile.exists()) {
                //播放本地歌曲
                playLocalMusic(playbackInfo);
            } else {
                //播放网络歌曲
                doNetMusic();
            }
        }
    }

    private void playLocalMusic(PlaybackInfo playbackInfo) {

        try {
            mMediaPlayer = new IjkMediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(playbackInfo.getAudioInfo().getFilePath());
            mMediaPlayer.prepareAsync();
            //
            mMediaPlayer.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(IMediaPlayer mp) {
                    mMediaPlayer.start();
                    if (mPrefMgr.isLrcSeekTo()) {

                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mPrefMgr.setLrcSeekTo(false);
                    }
                    isSeekTo = false;

                }
            });
            mMediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer mp) {

                    //播放完成，执行下一首操作
                    nextMusic();

                }
            });
            mMediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(IMediaPlayer mp, int what, int extra) {

                    //发送播放错误广播
                    Intent errorIntent = new Intent(EventManager.ACTION_SERVICE_PLAYERRORMUSIC);
                    errorIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(errorIntent);

                    ToastUtil.show(mContext, R.string.error_play);


                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                                //播放下一首
                                nextMusic();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();

                    return false;
                }
            });
            mMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer mp) {

                    PlaybackInfo info = mPrefMgr.getPlaybackInfo();
                    if (info != null) {

                        if (info.getProgress() > 0) {
                            isSeekTo = true;
                            mMediaPlayer.seekTo(info.getProgress());
                        } else {
                            mMediaPlayer.start();
                        }


                        //设置当前播放的状态
                        mPrefMgr.setPlayStatus(PlayerManager.Status.PLAYING);

                        //发送play的广播
                        Intent playIntent = new Intent(EventManager.ACTION_SERVICE_PLAYMUSIC);
                        //playIntent.putExtra(PlaybackInfo.KEY, audioMessage);
                        playIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        sendBroadcast(playIntent);
                    }
                }
            });

            if (mPlayerThread == null) {
                mPlayerThread = new Thread(new PlayerRunnable());
                mPlayerThread.start();
            }

        } catch (Exception e) {

            //发送播放错误广播
            Intent errorIntent = new Intent(EventManager.ACTION_SERVICE_PLAYERRORMUSIC);
            playbackInfo.setErrorMessage(e.getMessage());
            errorIntent.putExtra(PlaybackInfo.KEY, playbackInfo);
            errorIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            sendBroadcast(errorIntent);

            ToastUtil.show(mContext, R.string.error_play);
            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        //播放下一首
                        nextMusic();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    private void pauseMusic() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }

        mPrefMgr.setPlayStatus(PlayerManager.Status.PAUSE);

        Intent nextIntent = new Intent(EventManager.ACTION_SERVICE_PAUSEMUSIC);
        nextIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(nextIntent);
    }

    private void resumeMusic(PlaybackInfo playbackInfo) {

        AudioInfo currentAudio = mPrefMgr.getCurrentAudio();

        //如果是网络歌曲，先进行下载，再进行播放
        if (currentAudio != null && currentAudio.getType() == AudioInfo.NET) {
            // TODO:
            //如果进度为0，表示上一次下载直接错误。
            /*int downloadedSize = DownloadThreadDB.getDownloadThreadDB(getApplicationContext()).getDownloadedSize(mHPApplication.getPlayIndexHashID(), OnLineAudioManager.threadNum);
            if (downloadedSize == 0) {
                //发送init的广播
                Intent initIntent = new Intent(AudioBroadcastReceiver.ACTION_INITMUSIC);
                //initIntent.putExtra(AudioMessage.KEY, audioMessage);
                initIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(initIntent);
            }
            doNetMusic();*/
        } else {
            if (mMediaPlayer != null) {
                isSeekTo = true;
                mMediaPlayer.seekTo(playbackInfo.getProgress());
            }

            mPrefMgr.setPlayStatus(PlayerManager.Status.PLAYING);
        }

        Intent nextIntent = new Intent(EventManager.ACTION_SERVICE_RESUMEMUSIC);
        nextIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(nextIntent);
    }

    private void preMusic() {

        int playMode = mPrefMgr.getPlayMode();
        AudioInfo audioInfo = PlayerManager.getInstance(mContext).getPreviousSong(playMode);
        if (audioInfo == null) {
            releasePlayer();
            resetPlayData();

            //发送空数据广播
            Intent nullIntent = new Intent(EventManager.ACTION_NULLMUSIC);
            nullIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            sendBroadcast(nullIntent);

            return;
        }

        PlaybackInfo playbackInfo = new PlaybackInfo();
        playbackInfo.setAudioInfo(audioInfo);
        playMusic(playbackInfo);
    }

    private void nextMusic() {
        int playMode = mPrefMgr.getPlayMode();
        AudioInfo audioInfo = PlayerManager.getInstance(mContext).getNextSong(playMode);
        if (audioInfo == null) {
            releasePlayer();
            resetPlayData();

            //发送空数据广播
            Intent nullIntent = new Intent(EventManager.ACTION_NULLMUSIC);
            nullIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            sendBroadcast(nullIntent);

            return;
        }

        PlaybackInfo playbackInfo = new PlaybackInfo();
        playbackInfo.setAudioInfo(audioInfo);
        playMusic(playbackInfo);
    }

    private void seekToMusic(PlaybackInfo playbackInfo) {
        if (mMediaPlayer != null) {
            isSeekTo = true;
            mMediaPlayer.seekTo(playbackInfo.getProgress());
        }
    }

    //TODO:
    private void doNetMusic() {
        /*AudioInfo audioInfo = mHPApplication.getCurAudioInfo();
        mDownloadHandler.removeCallbacks(mDownloadCheckRunnable);
        //设置当前的播放状态
        mHPApplication.setPlayStatus(AudioPlayerManager.PLAYNET);

        //下载
        if (!OnLineAudioManager.getOnLineAudioManager(mHPApplication, getApplicationContext()).taskIsExists(audioInfo.getHash())) {
            OnLineAudioManager.getOnLineAudioManager(mHPApplication, getApplicationContext()).addTask(audioInfo);
            mDownloadHandler.postAtTime(mDownloadCheckRunnable, 1000);
            logger.e("准备播放在线歌曲：" + audioInfo.getSongName());
        }*/

    }

    //TODO:
    private void playNetMusic() {
        /*if (mHPApplication.getCurAudioMessage() != null && mHPApplication.getCurAudioInfo() != null) {
            String filePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_CACHE_AUDIO, mHPApplication.getCurAudioInfo().getHash() + ".temp");
            try {
                mMediaPlayer = new IjkMediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(filePath);
                mMediaPlayer.prepareAsync();
                //
                mMediaPlayer.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(IMediaPlayer mp) {
                        mMediaPlayer.start();
                        if (mHPApplication.isLrcSeekTo()) {

                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            mHPApplication.setLrcSeekTo(false);
                        }
                        isSeekTo = false;

                    }
                });
                mMediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(IMediaPlayer mp) {

                        if (mMediaPlayer.getCurrentPosition() < (mHPApplication.getCurAudioInfo().getDuration() - 2 * 1000)) {
                            playNetMusic();
                        } else {
                            //播放完成，执行下一首操作
                            nextMusic();
                        }

                    }
                });
                mMediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(IMediaPlayer mp, int what, int extra) {

                        //发送播放错误广播
                        Intent errorIntent = new Intent(AudioBroadcastReceiver.ACTION_SERVICE_PLAYERRORMUSIC);
                        errorIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        sendBroadcast(errorIntent);

                        ToastUtil.showTextToast(getApplicationContext(), "播放歌曲出错，1秒后播放下一首");


                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000);
                                    //播放下一首
                                    nextMusic();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();

                        return false;
                    }
                });
                mMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(IMediaPlayer mp) {

                        if (mHPApplication.getCurAudioMessage() != null) {
                            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();

                            if (audioMessage.getPlayProgress() > 0) {
                                isSeekTo = true;
                                mMediaPlayer.seekTo(audioMessage.getPlayProgress());
                            } else {
                                mMediaPlayer.start();
                            }


                            //设置当前播放的状态
                            mHPApplication.setPlayStatus(AudioPlayerManager.PLAYING);

                            //发送play的广播
                            Intent playIntent = new Intent(AudioBroadcastReceiver.ACTION_SERVICE_PLAYMUSIC);
                            playIntent.putExtra(AudioMessage.KEY, audioMessage);
                            playIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            sendBroadcast(playIntent);
                        }
                    }
                });

                if (mPlayerThread == null) {
                    mPlayerThread = new Thread(new PlayerRunnable());
                    mPlayerThread.start();
                }

            } catch (Exception e) {
                e.printStackTrace();
                logger.e(e.getMessage());

                //发送播放错误广播
                Intent errorIntent = new Intent(AudioBroadcastReceiver.ACTION_SERVICE_PLAYERRORMUSIC);
                mHPApplication.getCurAudioMessage().setErrorMsg(e.getMessage());
                errorIntent.putExtra(AudioMessage.KEY, mHPApplication.getCurAudioMessage());
                errorIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(errorIntent);

                ToastUtil.showTextToast(getApplicationContext(), "播放歌曲出错，1秒后播放下一首");
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            //播放下一首
                            nextMusic();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }


        }*/
    }

    private class PlayerRunnable implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(100);//方便后面用来刷新歌词
                    if (!isSeekTo && mMediaPlayer != null && mMediaPlayer.isPlaying()) {

                        PlaybackInfo playbackInfo = mPrefMgr.getPlaybackInfo();
                        if (playbackInfo != null) {
                            playbackInfo.setProgress(mMediaPlayer.getCurrentPosition());

                            //发送正在播放中的广播
                            Intent playingIntent = new Intent(EventManager.ACTION_SERVICE_PLAYINGMUSIC);
                            //playingIntent.putExtra(PlaybackInfo.KEY, mHPApplication.getCurAudioMessage());
                            playingIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            sendBroadcast(playingIntent);

                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void releasePlayer() {
        mPrefMgr.setPlayStatus(PlayerManager.Status.STOP);

        if (mPlayerThread != null) {
            mPlayerThread = null;
        }
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        //System.gc();
    }

    /**
     * 重置播放数据
     */
    private void resetPlayData() {
        mPrefMgr.setPlaybackInfo(null);
        mPrefMgr.setCurrentAudio(null);
        mPrefMgr.setCurrentAudioHash("");
    }
}
