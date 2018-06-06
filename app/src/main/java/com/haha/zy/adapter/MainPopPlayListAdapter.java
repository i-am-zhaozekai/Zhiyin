package com.haha.zy.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.haha.zy.R;
import com.haha.zy.audio.AudioInfo;
import com.haha.zy.db.DatabaseHelper;
import com.haha.zy.player.EventManager;
import com.haha.zy.player.PlaybackInfo;
import com.haha.zy.player.PlayerManager;
import com.haha.zy.preference.PreferenceManager;
import com.haha.zy.util.ToastUtil;
import com.haha.zy.widget.ListItemRelativeLayout;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

/**
 * 主界面当前播放列表
 */
public class MainPopPlayListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /**
     * 无数据
     */
    public static final int NODATA = 1;
    /**
     * 已经到底了
     */
    public static final int NOMOREDATA = 2;
    /**
     * 内容item
     */
    public static final int OTHER = 4;
    private Context mContext;
    private List<AudioInfo> mDatas;
    private int state = NOMOREDATA;

    /**
     * 播放歌曲索引
     */
    private int playIndexPosition = -1;
    private String playIndexHash = "-1";
    private PreferenceManager mPrefMgr;


    public MainPopPlayListAdapter(Context context, List<AudioInfo> datas) {
        mContext = context;
        mPrefMgr = PreferenceManager.getInstance(mContext);
        mDatas = datas;
    }

    @Override
    public int getItemViewType(int position) {
        if (state == NODATA && mDatas.size() == position) {
            return NODATA;
        } else {
            if (mDatas.size() == position) {
                return NOMOREDATA;
            }
            return OTHER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = null;
        if (viewType == NODATA) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_nodata, null, false);
            NoDataViewHolder holder = new NoDataViewHolder(view);
            return holder;
        } else if (viewType == NOMOREDATA) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_nomoredata, null, false);
            NoDataViewHolder holder = new NoDataViewHolder(view);
            return holder;
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_main_popsong, null, false);
            PopListViewHolder holder = new PopListViewHolder(view);
            return holder;
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof PopListViewHolder && position < mDatas.size()) {
            AudioInfo audioInfo = mDatas.get(position);
            refreshViewHolder(position, (PopListViewHolder) viewHolder, audioInfo);
        }
    }

    /**
     * 刷新
     *
     * @param position
     * @param viewHolder
     * @param audioInfo
     */
    private void refreshViewHolder(final int position, final PopListViewHolder viewHolder, final AudioInfo audioInfo) {
        //判断是否已缓存到本地或者下载到本地
        if (DatabaseHelper.getInstance(mContext).isNetAudioExists(audioInfo.getHash())) {
            viewHolder.getIslocalImg().setVisibility(View.VISIBLE);
        } else {
            //TODO:
            /*int downloadSize = DownloadThreadDB.getDownloadThreadDB(mContext).getDownloadedSize(audioInfo.getHash(), OnLineAudioManager.threadNum);
            if (downloadSize >= audioInfo.getFileSize()) {
                viewHolder.getIslocalImg().setVisibility(View.VISIBLE);
            } else {
                viewHolder.getIslocalImg().setVisibility(View.GONE);
            }*/
        }

        //判断是否是喜欢歌曲
        boolean isLike = DatabaseHelper.getInstance(mContext).isRecentOrLikeExists(audioInfo.getHash(), audioInfo.getType(), false);
        viewHolder.updateLikeUI(isLike, false, false, null);

        //喜欢按钮
        viewHolder.getLikedImg().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 点击 like 将执行 unlike 操作
                viewHolder.updateLikeUI(false, true, true, audioInfo);
            }
        });
        //取消喜欢按钮
        viewHolder.getUnLikeTv().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder.updateLikeUI(true, true, true, audioInfo);
            }
        });

        //删除
        viewHolder.getDeleteImgBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        if (audioInfo.getType() == AudioInfo.NET || audioInfo.getType() == AudioInfo.DOWNLOAD) {

            //下载
            //TODO:
            /*if (DownloadInfoDB.getAudioInfoDB(mContext).isExists(audioInfo.getHash())|| AudioInfoDB.getAudioInfoDB(mContext).isNetAudioExists(audioInfo.getHash())) {

                viewHolder.getDownloadedImg().setVisibility(View.VISIBLE);
                viewHolder.getDownloadImg().setVisibility(View.INVISIBLE);
            } else {
                viewHolder.getDownloadedImg().setVisibility(View.INVISIBLE);
                viewHolder.getDownloadImg().setVisibility(View.VISIBLE);
            }*/

        } else {
            viewHolder.getDownloadedImg().setVisibility(View.INVISIBLE);
            viewHolder.getDownloadImg().setVisibility(View.INVISIBLE);
        }

        //下载按钮
        viewHolder.getDownloadImg().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO:
                /*DownloadAudioManager.getDownloadAudioManager(mHPApplication, mContext).addTask(audioInfo);
                viewHolder.getDownloadedImg().setVisibility(View.VISIBLE);
                viewHolder.getDownloadImg().setVisibility(View.INVISIBLE);*/
            }
        });

        //下载完成
        viewHolder.getDownloadedImg().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO:
                //DownloadAudioManager.getDownloadAudioManager(mHPApplication, mContext).addTask(audioInfo);
            }
        });

        // 更新当前播放歌曲
        if (audioInfo.getHash().equals(mPrefMgr.getCurrentAudioHash())) {
            playIndexPosition = position;
            playIndexHash = mPrefMgr.getCurrentAudioHash();
            //
            viewHolder.getSongIndexTv().setVisibility(View.INVISIBLE);
            viewHolder.getSingPicImg().setVisibility(View.VISIBLE);
            //
            viewHolder.getSingerNameTv().setTextColor(mContext.getResources().getColor(R.color.pop_playlist_current_singer));
            viewHolder.getSongNameTv().setTextColor(mContext.getResources().getColor(R.color.pop_playlist_current_song));

            //加载歌手图片
            //TODO:
            //ImageUtil.loadSingerImage(mHPApplication, mContext, viewHolder.getSingPicImg(), audioInfo.getSingerName());


        } else {
            viewHolder.getSongIndexTv().setVisibility(View.VISIBLE);
            viewHolder.getSingPicImg().setVisibility(View.INVISIBLE);

            viewHolder.getSingerNameTv().setTextColor(mContext.getResources().getColor(R.color.pop_playlist_singer));
            viewHolder.getSongNameTv().setTextColor(mContext.getResources().getColor(R.color.pop_playlist_song));
        }

        //显示歌曲索引
        viewHolder.getSongIndexTv().setText(((position + 1) < 10 ? "0" + (position + 1) : (position + 1) + ""));

        String singerName = audioInfo.getArtist();
        String songName = audioInfo.getTitle();

        viewHolder.getSongNameTv().setText(songName);
        viewHolder.getSingerNameTv().setText(singerName);
        //item点击事件
        viewHolder.getListItemRelativeLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playIndexPosition == position) {
                    if (mPrefMgr.getPlayStatus() == PlayerManager.Status.PLAYING) {
                        // 当前正在播放，发送暂停

                        Intent pauseIntent = new Intent(EventManager.ACTION_PAUSEMUSIC);
                        pauseIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        mContext.sendBroadcast(pauseIntent);

                        return;
                    } else if (mPrefMgr.getPlayStatus() == PlayerManager.Status.PAUSE) {
                        //当前正在暂停，发送唤醒播放

                        Intent remuseIntent = new Intent(EventManager.ACTION_RESUMEMUSIC);
                        remuseIntent.putExtra(PlaybackInfo.KEY, mPrefMgr.getPlaybackInfo());
                        remuseIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        mContext.sendBroadcast(remuseIntent);

                        return;
                    }
                }

                //设置界面ui
                viewHolder.getSongIndexTv().setVisibility(View.INVISIBLE);
                viewHolder.getSingPicImg().setVisibility(View.VISIBLE);
                //
                viewHolder.getSingerNameTv().setTextColor(mContext.getResources().getColor(R.color.pop_playlist_current_singer));
                viewHolder.getSongNameTv().setTextColor(mContext.getResources().getColor(R.color.pop_playlist_current_song));
                //加载歌手图片
                //TODO:
                //ImageUtil.loadSingerImage(mHPApplication, mContext, viewHolder.getSingPicImg(), audioInfo.getSingerName());


                //
                if (playIndexPosition != -1) {
                    notifyItemChanged(playIndexPosition);
                }

                //
                playIndexPosition = position;
                playIndexHash = audioInfo.getHash();
                mPrefMgr.setCurrentAudioHash(playIndexHash);

                //发送播放广播
                Intent playIntent = new Intent(EventManager.ACTION_PLAYMUSIC);
                PlaybackInfo playbackInfo = new PlaybackInfo();
                playbackInfo.setAudioInfo(audioInfo);
                playIntent.putExtra(PlaybackInfo.KEY, playbackInfo);
                playIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(playIntent);
            }
        });
    }


    /**
     * 刷新view
     *
     * @param audioInfo
     */
    public void refreshViewHolder(AudioInfo audioInfo) {
        if (playIndexPosition != -1) {
            notifyItemChanged(playIndexPosition);
        }
        if (audioInfo == null) {
            playIndexPosition = -1;
            playIndexHash = "-1";
            return;
        }
        //;
        playIndexPosition = getPlayIndexPosition(audioInfo);
        if (playIndexPosition != -1) {
            playIndexHash = audioInfo.getHash();
            notifyItemChanged(playIndexPosition);
        }

    }

    /**
     * 获取当前播放索引
     *
     * @param audioInfo
     * @return
     */
    public int getPlayIndexPosition(AudioInfo audioInfo) {
        if (audioInfo != null)
            for (int i = 0; i < mDatas.size(); i++) {

                if (mDatas.get(i).getHash().equals(audioInfo.getHash())) {

                    return i;
                }
            }

        return -1;
    }


    @Override
    public int getItemCount() {
        return mDatas.size() + 1;
    }

    public void setState(int state) {
        this.state = state;
    }

    /////////////////////////////////////////////////////

    class PopListViewHolder extends RecyclerView.ViewHolder {
        private View view;
        /**
         * item底部布局
         */
        private ListItemRelativeLayout listItemRelativeLayout;

        /**
         * 歌手头像按钮
         */
        private RoundedImageView singPicImg;
        /**
         * 歌曲索引
         */
        private TextView songIndexTv;

        /**
         * 歌曲名称
         */
        private TextView songNameTv;

        /**
         * 歌手名称
         */
        private TextView singerNameTv;
        /**
         * 是否存在本地
         */
        private ImageView islocalImg;
        /**
         * 下载未完成按钮
         */
        private ImageView downloadImg;
        /**
         * 添加喜欢按钮
         */
        private ImageView unlikeTv;

        /**
         * 下载完成按钮
         */
        private ImageView downloadedImg;

        /**
         * 喜欢按钮
         */
        private ImageView likeImg;


        /**
         * 删除按钮
         */
        private ImageView deleteImgBtn;

        public PopListViewHolder(View view) {
            super(view);
            this.view = view;
        }

        public ListItemRelativeLayout getListItemRelativeLayout() {
            if (listItemRelativeLayout == null) {
                listItemRelativeLayout = view.findViewById(R.id.itemBG);
            }
            return listItemRelativeLayout;
        }


        public TextView getSongNameTv() {
            if (songNameTv == null) {
                songNameTv = view.findViewById(R.id.songName);
            }
            return songNameTv;
        }

        public TextView getSingerNameTv() {
            if (singerNameTv == null) {
                singerNameTv = view.findViewById(R.id.singerName);
            }
            return singerNameTv;
        }

        public ImageView getIslocalImg() {
            if (islocalImg == null) {
                islocalImg = view.findViewById(R.id.islocal);
            }
            return islocalImg;
        }

        public RoundedImageView getSingPicImg() {
            if (singPicImg == null) {
                singPicImg = view.findViewById(R.id.singPic);
            }
            return singPicImg;
        }

        public TextView getSongIndexTv() {
            if (songIndexTv == null) {
                songIndexTv = view.findViewById(R.id.songIndex);
            }
            return songIndexTv;
        }

        public ImageView getDownloadImg() {
            if (downloadImg == null) {
                downloadImg = view.findViewById(R.id.download);
            }
            return downloadImg;
        }

        public ImageView getUnLikeTv() {
            if (unlikeTv == null) {
                unlikeTv = view.findViewById(R.id.unlike);
            }
            return unlikeTv;
        }

        public ImageView getDownloadedImg() {
            if (downloadedImg == null) {
                downloadedImg = view.findViewById(R.id.downloaded);
            }
            return downloadedImg;
        }

        public ImageView getLikedImg() {
            if (likeImg == null) {
                likeImg = view.findViewById(R.id.liked);
            }
            return likeImg;
        }

        public void updateLikeUI(boolean like, boolean showTips, boolean notify, @Nullable AudioInfo audioInfo) {
            if (like) {
                // 注意使用 get方法不要直接使用 like/unlike 成员变量，因为有可能未初始化
                getLikedImg().setVisibility(View.VISIBLE);
                getUnLikeTv().setVisibility(View.INVISIBLE);
            } else {
                getLikedImg().setVisibility(View.INVISIBLE);
                getUnLikeTv().setVisibility(View.VISIBLE);
            }

            if (showTips) {
                ToastUtil.show(mContext, like ? R.string.tips_add_like : R.string.tips_remove_like);
            }

            if (notify && null != audioInfo) {
                String action = like ? EventManager.ACTION_LIKEADD : EventManager.ACTION_LIKEDELETE;

                Intent intent = new Intent(action);
                intent.putExtra(AudioInfo.KEY, audioInfo);
                intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(intent);
            }
        }

        public ImageView getDeleteImgBtn() {
            if (deleteImgBtn == null) {
                deleteImgBtn = view.findViewById(R.id.delete);
            }
            return deleteImgBtn;
        }
    }

}
