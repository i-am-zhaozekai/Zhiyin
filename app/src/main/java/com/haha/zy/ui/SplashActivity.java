package com.haha.zy.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.haha.zy.R;
import com.haha.zy.audio.AudioInfo;
import com.haha.zy.audio.AudioParser;
import com.haha.zy.audio.TrackInfo;
import com.haha.zy.db.DatabaseHelper;
import com.haha.zy.player.PlayerManager;
import com.haha.zy.preference.PreferenceManager;
import com.haha.zy.storage.StorageManagerReflection;
import com.haha.zy.storage.StorageVolumeInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


public class SplashActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks,
        EasyPermissions.RationaleCallbacks {

    private static final String[] PERMS_STORAGE = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int REQUEST_CODE_PERMS_STORAGE = 1;

    private Context mContext;
    private PreferenceManager mPrefMgr;
    private Handler mHandler;
    private Runnable mRunnable;
    private long mDelayTime = 1000L;

    @Override
    protected void loadData(boolean isRestoreInstance) {
        super.loadData(isRestoreInstance);

        mContext = getApplicationContext();
        mPrefMgr = PreferenceManager.getInstance(mContext);

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                showAds();
            }
        };

        requestPerms(PERMS_STORAGE, REQUEST_CODE_PERMS_STORAGE);
    }

    private void showAds() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.zoomin, R.anim.zoomout);

        finish();
    }

    private void initAppData() {

        boolean isFirst = mPrefMgr.isFirst();
        if (isFirst) {

            final List<AudioInfo> audioInfos = new ArrayList<>();

            AudioParser parser = new AudioParser();
            parser.setFilter(new AudioParser.Filter() {
                @Override
                public boolean filterWithFileInfo(File audioFile, String hash) {
                    if (audioFile.length() < 1024 * 1024) {
                        return true;
                    }

                    if (DatabaseHelper.getInstance(mContext).isExists(hash)) {
                        return true;
                    }

                    return false;
                }

                @Override
                public boolean filterWithTrackInfo(TrackInfo trackInfo) {
                    return trackInfo.getDuration() < 5000L;
                }
            });

            List<StorageVolumeInfo> list = StorageManagerReflection.getStorageVolumes(mContext);
            for (int i = 0; i < list.size(); i++) {
                StorageVolumeInfo storageInfo = list.get(i);
                scanLocalAudioFile(storageInfo.mPath, parser, audioInfos);
            }

            if (audioInfos.size() > 0) {
                DatabaseHelper.getInstance(getApplicationContext()).add(audioInfos);
            }

            mDelayTime = 2000L;

            mPrefMgr.setFirst(false);
        } else {
            mDelayTime = 3000L;
        }

        //初始化配置数据
        initPreferencesData();

        //初始化上次的播放数据
        List<AudioInfo> playList = mPrefMgr.getCurrentPlaylist();
        if (playList == null || playList.size() == 0) {
            Log.d("XXX", "playlist 0");
            List<AudioInfo> data = DatabaseHelper.getInstance(getApplicationContext()).getAllLocalAudio();
            if (data != null && data.size() > 0) {
                PreferenceManager.getInstance(getApplicationContext()).setCurrentPlaylist(data);
                PreferenceManager.getInstance(mContext).setCurrentAudio(data.get(0));
                PreferenceManager.getInstance(mContext).setCurrentAudioHash(data.get(0).getHash());
            }

        }

        //loadSplashMusic();
        mHandler.postDelayed(mRunnable, mDelayTime);
    }

    private static void scanLocalAudioFile(String path, AudioParser parser, List<AudioInfo> audioInfos) {
        String filterFormats = "ape,flac,mp3,wav";
        File[] files = new File(path).listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                File temp = files[i];
                if (temp.isFile()) {

                    String fileName = temp.getName();
                    String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
                    if (filterFormats.indexOf(fileExt) == -1) {
                        continue;
                    }

                    AudioInfo audioInfo = parser.parse(temp);
                    if (audioInfo != null) {
                        audioInfos.add(audioInfo);
                    }

                } else if (temp.isDirectory() && temp.getPath().indexOf("/.") == -1) {
                    scanLocalAudioFile(temp.getPath(), parser, audioInfos);
                }
            }
        }

    }

    private void initPreferencesData() {
        mPrefMgr.setPlayStatus(PlayerManager.Status.STOP);
        mPrefMgr.setWire(false);
        mPrefMgr.setWifiOnly(true);
        mPrefMgr.setCurrentAudioHash("");
        mPrefMgr.setPlayMode(0);
        mPrefMgr.setLrcColorIndex(0);
        mPrefMgr.setLrcFontSize(50);
        mPrefMgr.setLrcMultiLine(true);
    }

    private void requestPerms(String[] permissions, int requestCode) {
        if (!EasyPermissions.hasPermissions(this, permissions)) {
            EasyPermissions.requestPermissions(this, getString(R.string.perms_request_rationale_storage), requestCode, permissions);
        } else {
            initAppData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // 交给 EasyPermissions 处理，各个回调的作用请参考官方文档 https://github.com/googlesamples/easypermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        initAppData();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (REQUEST_CODE_PERMS_STORAGE == requestCode) {
            if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                new AppSettingsDialog.Builder(this)
                        .setRationale(getString(R.string.perms_request_appsetting_storage))
                        .setPositiveButton(android.R.string.yes)
                        .setNegativeButton(android.R.string.no)
                        .build()
                        .show();
            }
        }
    }

    @AfterPermissionGranted(REQUEST_CODE_PERMS_STORAGE)
    private void methodRequiresTwoPermission() {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            if (!EasyPermissions.hasPermissions(this, PERMS_STORAGE)) {
                finish();
            } else {

            }
        }
    }

    @Override
    public void onRationaleAccepted(int requestCode) {
        initAppData();
    }

    @Override
    public void onRationaleDenied(int requestCode) {
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            finish();
        }
    }
}
