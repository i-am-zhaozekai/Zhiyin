package com.haha.zy.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.haha.zy.audio.AudioInfo;
import com.haha.zy.pinyin.PinyinConverter;
import com.haha.zy.util.DateUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.haha.zy.db.DatabaseConstant.ORDER_BY_ASC;
import static com.haha.zy.db.DatabaseConstant.TABLE_NAME_AUDIO_INFO;

public class DatabaseHelper extends SQLiteOpenHelper {

    private final Context mContext;

    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_ARTIST = "artist";
    public static final String COLUMN_NAME_HASH = "hash";
    public static final String COLUMN_NAME_EXTENSION = "file_extension";
    public static final String COLUMN_NAME_SIZE = "file_size";
    public static final String COLUMN_NAME_SIZE_TEXT = "size_text";
    public static final String COLUMN_NAME_PATH = "file_path";
    public static final String COLUMN_NAME_DURATION = "duration";
    public static final String COLUMN_NAME_DURATION_TEXT = "duration_text";
    public static final String COLUMN_NAME_DOWNLOAD_URL = "download_url";
    public static final String COLUMN_NAME_CREATE_TIME = "create_time";
    public static final String COLUMN_NAME_STATUS = "status";
    public static final String COLUMN_NAME_TYPE = "type";
    public static final String COLUMN_NAME_CATEGORY = "category";
    public static final String COLUMN_NAME_CATEGORY_SECONDARY = "secondary_category";

    private static final String CREATE_TABLE = "create table " + TABLE_NAME_AUDIO_INFO
            + "("
            + COLUMN_NAME_TITLE + " text,"
            + COLUMN_NAME_ARTIST + " text,"
            + COLUMN_NAME_HASH + " text,"
            + COLUMN_NAME_EXTENSION + " text,"
            + COLUMN_NAME_SIZE + " long,"
            + COLUMN_NAME_SIZE_TEXT + " text,"
            + COLUMN_NAME_PATH + " text,"
            + COLUMN_NAME_DURATION + " long,"
            + COLUMN_NAME_DURATION_TEXT + " text,"
            + COLUMN_NAME_DOWNLOAD_URL + " text,"
            + COLUMN_NAME_CREATE_TIME + " text,"
            + COLUMN_NAME_STATUS + " long,"
            + COLUMN_NAME_TYPE + " long,"
            + COLUMN_NAME_CATEGORY + " text,"
            + COLUMN_NAME_CATEGORY_SECONDARY + " text"
            + ")";

    private static volatile DatabaseHelper sInstance = null;

    private DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DatabaseConstant.DATABASE_FILE_NAME, null, 1);
        mContext = context.getApplicationContext();
    }

    public static DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DatabaseHelper.class) {
                if (sInstance == null){
                    sInstance = new DatabaseHelper(context);
                }
            }
        }
        return sInstance;
    }

    /**
     * 获取ContentValues
     *
     * @param audioInfo
     */
    private ContentValues getContentValues(AudioInfo audioInfo) {

        ContentValues values = new ContentValues();
        //
        values.put(COLUMN_NAME_TITLE, audioInfo.getTitle());
        values.put(COLUMN_NAME_ARTIST, audioInfo.getArtist());
        values.put(COLUMN_NAME_HASH, audioInfo.getHash());
        values.put(COLUMN_NAME_EXTENSION, audioInfo.getFileExt());
        values.put(COLUMN_NAME_SIZE, audioInfo.getFileSize());
        values.put(COLUMN_NAME_SIZE_TEXT, audioInfo.getFileSizeText());
        values.put(COLUMN_NAME_PATH, audioInfo.getFilePath());
        values.put(COLUMN_NAME_DURATION, audioInfo.getDuration());
        values.put(COLUMN_NAME_DURATION_TEXT, audioInfo.getDurationText());
        values.put(COLUMN_NAME_DOWNLOAD_URL, audioInfo.getDownloadUrl());
        values.put(COLUMN_NAME_CREATE_TIME, audioInfo.getCreateTime());
        values.put(COLUMN_NAME_STATUS, audioInfo.getStatus());
        values.put(COLUMN_NAME_TYPE, audioInfo.getType());


        PinyinConverter pyConverter = new PinyinConverter();
        //获取索引
        String category = pyConverter.getPinyin(audioInfo.getArtist()).toUpperCase();
        char cat = category.charAt(0);
        if (cat <= 'Z' && cat >= 'A') {
            audioInfo.setCategory(cat + "");
            audioInfo.setChildCategory(category);
        } else {
            audioInfo.setCategory("^");
            audioInfo.setChildCategory(category);
        }

        values.put(COLUMN_NAME_CATEGORY, audioInfo.getCategory());
        values.put(COLUMN_NAME_CATEGORY_SECONDARY, audioInfo.getChildCategory());

        return values;
    }

    public boolean add(AudioInfo audioInfo) {

        List<ContentValues> values = new ArrayList<ContentValues>();
        ContentValues value = getContentValues(audioInfo);
        values.add(value);

        return insert(values);
    }

    public boolean add(List<AudioInfo> audioInfos) {
        List<ContentValues> values = new ArrayList<ContentValues>();
        for (AudioInfo audioInfo : audioInfos) {
            values.add(getContentValues(audioInfo));
        }
        return insert(values);
    }

    private boolean insert(List<ContentValues> values) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            for (ContentValues value : values) {
                db.insert(TABLE_NAME_AUDIO_INFO, null, value);
            }
            db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return false;
    }

    public void delete(String hash) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            StringBuilder whereClause = new StringBuilder();
            whereClause.append(COLUMN_NAME_HASH).append("=?");
            db.delete(TABLE_NAME_AUDIO_INFO, whereClause.toString(), new String[]{hash});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isExists(String hash) {
        SQLiteDatabase db = getReadableDatabase();

        StringBuilder selection = new StringBuilder();
        selection.append(" ").append(COLUMN_NAME_HASH).append("=?");

        Cursor cursor = db.query(TABLE_NAME_AUDIO_INFO, new String[]{},
                selection.toString(), new String[]{hash}, null, null, null);
        if (!cursor.moveToNext()) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public void deleteTab() {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.execSQL("drop table if exists " + TABLE_NAME_AUDIO_INFO);
            db.execSQL(CREATE_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getLocalAudioCount() {
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {AudioInfo.LOCAL + "", AudioInfo.DOWNLOAD + "", AudioInfo.FINISH + ""};

        StringBuilder sqlRaw = new StringBuilder();
        sqlRaw.append("select count(*) from ").append(TABLE_NAME_AUDIO_INFO)
                .append(" WHERE ").append(COLUMN_NAME_TYPE).append("=? or ( ")
                .append(COLUMN_NAME_TYPE).append("=? and ")
                .append(COLUMN_NAME_STATUS).append("=?)");

        Cursor cursor = db.rawQuery(sqlRaw.toString(), args);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    /**
     * 获取所有本地歌曲分类
     *
     * @return
     */
    public List<String> getAllLocalCategory() {

        // 第一个参数String：表名
        // 第二个参数String[]:要查询的列名
        // 第三个参数String：查询条件
        // 第四个参数String[]：查询条件的参数
        // 第五个参数String:对查询的结果进行分组
        // 第六个参数String：对分组的结果进行限制
        // 第七个参数String：对查询的结果进行排序
        List<String> list = new ArrayList<String>();
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {AudioInfo.LOCAL + "", AudioInfo.DOWNLOAD + "", AudioInfo.FINISH + ""};

        StringBuilder selection = new StringBuilder();
        selection.append(COLUMN_NAME_TYPE).append("=? or ( ")
                .append(COLUMN_NAME_TYPE).append("=? and ")
                .append(COLUMN_NAME_STATUS).append("=? )");

        StringBuilder orderBy = new StringBuilder();
        orderBy.append(COLUMN_NAME_CATEGORY).append(ORDER_BY_ASC)
                .append(" , ")
                .append(COLUMN_NAME_CATEGORY_SECONDARY).append(ORDER_BY_ASC);

        Cursor cursor = db.query(true, TABLE_NAME_AUDIO_INFO, new String[]{COLUMN_NAME_CATEGORY},
                selection.toString(), args,
                null, null, orderBy.toString(), null);
        while (cursor.moveToNext()) {
            list.add(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_CATEGORY)));
        }
        cursor.close();
        String baseCategory = "^";
        if (!list.contains(baseCategory)) {
            list.add(baseCategory);
        }
        return list;
    }

    /**
     * 获取分类下的歌曲
     *
     * @param category
     * @return
     */
    public List<Object> getLocalAudio(String category) {
        List<Object> list = new ArrayList<Object>();
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {category, AudioInfo.LOCAL + "", AudioInfo.DOWNLOAD + "", AudioInfo.FINISH + ""};
        Cursor cursor = db.query(TABLE_NAME_AUDIO_INFO, null,
                "category= ? and (type=? or ( type=? and status=? ))", args, null, null,
                "secondary_category asc", null);
        while (cursor.moveToNext()) {
            AudioInfo audioInfo = getAudioInfoFrom(cursor);
            list.add(audioInfo);
        }
        cursor.close();
        return list;
    }

    /**
     * 获取所有本地歌曲
     *
     * @return
     */
    public List<AudioInfo> getAllLocalAudio() {
        List<AudioInfo> list = new ArrayList<AudioInfo>();
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {AudioInfo.LOCAL + "", AudioInfo.DOWNLOAD + "", AudioInfo.FINISH + ""};
        Cursor cursor = db.query(TABLE_NAME_AUDIO_INFO, null,
                "type=? or ( type=? and status=? )", args, null, null,
                "category asc ,secondary_category asc", null);
        while (cursor.moveToNext()) {
            AudioInfo audioInfo = getAudioInfoFrom(cursor);
            File audioFile = new File(audioInfo.getFilePath());
            if (!audioFile.exists()) {
                continue;
            }
            list.add(audioInfo);
        }
        cursor.close();
        return list;
    }

    /**
     * 通过hash获取歌曲
     *
     * @param hash
     * @return
     */
    public AudioInfo getAudioInfoByHash(String hash) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME_AUDIO_INFO
                + " where hash=?", new String[]{hash + ""});
        if (!cursor.moveToNext()) {
            return null;
        }
        AudioInfo audioInfo = getAudioInfoFrom(cursor);
        cursor.close();
        return audioInfo;
    }

    public AudioInfo getAudioInfoFrom(Cursor cursor) {
        AudioInfo audioInfo = new AudioInfo();

        audioInfo.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TITLE)));
        audioInfo.setArtist(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ARTIST)));
        audioInfo.setHash(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_HASH)));
        audioInfo.setFilePath(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PATH)));
        audioInfo.setFileExt(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_EXTENSION)));
        audioInfo.setFileSize(cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_SIZE)));
        audioInfo.setFileSizeText(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_SIZE_TEXT)));
        audioInfo.setDuration(cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_DURATION)));
        audioInfo.setDurationText(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_DURATION_TEXT)));
        audioInfo.setDownloadUrl(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_DOWNLOAD_URL)));
        audioInfo.setCreateTime(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_CREATE_TIME)));
        audioInfo.setStatus(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_STATUS)));
        audioInfo.setType(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_TYPE)));
        audioInfo.setCategory(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_CATEGORY)));
        audioInfo.setChildCategory(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_CATEGORY_SECONDARY)));

        return audioInfo;
    }


    //、、、、、、、、、、、、、、、、、、、、、、、、、、、下载、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、

    /**
     * 判断网络歌曲是否在本地
     *
     * @param hash
     * @return
     */
    public boolean isNetAudioExists(String hash) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME_AUDIO_INFO, new String[]{},
                " hash=? and status=?", new String[]{hash, AudioInfo.FINISH + ""}, null, null, null);
        if (!cursor.moveToNext()) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    /**
     * 获取正在下载任务
     *
     * @return
     */
    public List<Object> getDownloadingAudio() {
        List<Object> list = new ArrayList<Object>();
        Cursor cursor = null;
        /*try {
            SQLiteDatabase db = getReadableDatabase();
            String args[] = {AudioInfo.DOWNLOAD + "", AudioInfo.INIT + "", AudioInfo.DOWNLOADING + ""};
            cursor = db.query(TABLE_NAME_AUDIO_INFO, null,
                    "type=? and (status=? or status=?)", args, null, null,
                    "create_time desc", null);
            while (cursor.moveToNext()) {
                AudioInfo audioInfo = getAudioInfoFrom(cursor);
                File audioFile = new File(audioInfo.getFilePath());
                if (!audioFile.exists() && audioInfo.getStatus() == AudioInfo.FINISH) {
                    continue;
                }
                //
                DownloadInfo downloadInfo = new DownloadInfo();
                downloadInfo.setmAudioInfo(audioInfo);
                //获取下载进度
                DownloadInfoDB.getAudioInfoDB(mContext).getDownloadInfoByHash(downloadInfo, audioInfo.getHash());

                list.add(downloadInfo);
            }

        } catch (Exception e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }*/

        return list;
    }

    /**
     * 获取已下载
     *
     * @return
     */
    public List<Object> getDownloadedAudio() {
        List<Object> list = new ArrayList<Object>();
        /*SQLiteDatabase db = getReadableDatabase();
        String args[] = {AudioInfo.DOWNLOAD + "", AudioInfo.FINISH + ""};
        Cursor cursor = db.query(TABLE_NAME_AUDIO_INFO, null,
                "type=? and status=?", args, null, null,
                "create_time desc", null);
        while (cursor.moveToNext()) {
            AudioInfo audioInfo = getAudioInfoFrom(cursor);
            File audioFile = new File(audioInfo.getFilePath());
            if (!audioFile.exists() && audioInfo.getStatus() == AudioInfo.FINISH) {
                continue;
            }
            //
            DownloadInfo downloadInfo = new DownloadInfo();
            downloadInfo.setmAudioInfo(audioInfo);
            //获取下载进度
            DownloadInfoDB.getAudioInfoDB(mContext).getDownloadInfoByHash(downloadInfo, audioInfo.getHash());

            list.add(downloadInfo);
        }
        cursor.close();*/
        return list;
    }

    /**
     * 更新
     *
     * @param hash
     */
    public void updateDonwloadInfo(String hash, int status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_STATUS, status);

        try {
            db.update(TABLE_NAME_AUDIO_INFO, values,
                    "type=? and hash=? ",
                    new String[]{AudioInfo.DOWNLOAD + "", hash});
        } catch (SQLException e) {
            Log.i("error", "update failed");
        }
    }

    /**
     * 获取下载歌曲个数
     *
     * @return
     */
    public int getDonwloadAudioCount() {
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {AudioInfo.DOWNLOAD + ""};
        Cursor cursor = db.rawQuery("select count(*)from " + TABLE_NAME_AUDIO_INFO
                + " WHERE type=? ", args);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }


    /**
     * 删除hash对应的数据
     */
    public void deleteDonwloadAudio(String hash) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete(TABLE_NAME_AUDIO_INFO, "hash=? and type=?", new String[]{hash, AudioInfo.DOWNLOAD + ""});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加
     *
     * @param audioInfo
     */
    public boolean addDonwloadAudio(AudioInfo audioInfo) {

        List<ContentValues> values = new ArrayList<ContentValues>();
        ContentValues value = getContentValues(audioInfo);

        value.put(COLUMN_NAME_TYPE, AudioInfo.DOWNLOAD);
        value.put(COLUMN_NAME_STATUS, AudioInfo.INIT);

        values.add(value);

        return insert(values);
    }

    /////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\///最近、喜欢///////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\/////////////////////

    /**
     * 添加
     *
     * @param audioInfo
     */

    public boolean addRecentOrLikeAudio(AudioInfo audioInfo, boolean isRecent) {
        int type = audioInfo.getType();
        if (type == AudioInfo.NET) {
            if (isRecent)
                type = AudioInfo.RECENT_NET;
            else type = AudioInfo.LIKE_NET;
        } else {
            if (isRecent)
                type = AudioInfo.RECENT_LOCAL;
            else type = AudioInfo.LIKE_LOCAL;
        }

        //更新创建时间
        audioInfo.setCreateTime(DateUtil.parseDateToString(new Date()));
        List<ContentValues> values = new ArrayList<ContentValues>();
        ContentValues value = getContentValues(audioInfo);
        value.put(COLUMN_NAME_TYPE, type);

        values.add(value);

        return insert(values);
    }

    /**
     * 是否存在
     *
     * @param hash
     * @return
     */
    public boolean isRecentOrLikeExists(String hash, int type, boolean isRecent) {

        String typeString = "";
        if (type == AudioInfo.NET) {
            if (isRecent)
                typeString = AudioInfo.RECENT_NET + "";
            else typeString = AudioInfo.LIKE_NET + "";
        } else {
            if (isRecent)
                typeString = AudioInfo.RECENT_LOCAL + "";
            else typeString = AudioInfo.LIKE_LOCAL + "";
        }

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME_AUDIO_INFO, new String[]{},
                " hash=? and type=?", new String[]{hash, typeString}, null, null, null);
        if (!cursor.moveToNext()) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    /**
     * 删除hash对应的数据
     */
    public void deleteRecentOrLikeAudio(String hash, int type, boolean isRecent) {
        String typeString = "";
        if (type == AudioInfo.NET) {
            if (isRecent)
                typeString = AudioInfo.RECENT_NET + "";
            else typeString = AudioInfo.LIKE_NET + "";
        } else {
            if (isRecent)
                typeString = AudioInfo.RECENT_LOCAL + "";
            else typeString = AudioInfo.LIKE_LOCAL + "";
        }
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete(TABLE_NAME_AUDIO_INFO, "hash=? and type=?", new String[]{hash, typeString});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新最近歌曲数据
     *
     * @param hash
     * @return
     */
    public boolean updateRecentAudio(String hash, int type, boolean isRecent) {
        String typeString = "";
        if (type == AudioInfo.NET) {
            if (isRecent)
                typeString = AudioInfo.RECENT_NET + "";
            else typeString = AudioInfo.LIKE_NET + "";
        } else {
            if (isRecent)
                typeString = AudioInfo.RECENT_LOCAL + "";
            else typeString = AudioInfo.LIKE_LOCAL + "";
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_CREATE_TIME, DateUtil.parseDateToString(new Date()));

        try {
            db.update(TABLE_NAME_AUDIO_INFO, values, "hash=? and type=?",
                    new String[]{hash, typeString});
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取最近歌曲总数
     *
     * @return
     */
    public int getRecentAudioCount() {
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {AudioInfo.RECENT_LOCAL + "", AudioInfo.RECENT_NET + ""};
        Cursor cursor = db.rawQuery("select count(*)from " + TABLE_NAME_AUDIO_INFO
                + " WHERE type=? or type=? ", args);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    /**
     * 获取喜欢歌曲总数
     *
     * @return
     */
    public int getLikeAudioCount() {
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {AudioInfo.LIKE_LOCAL + "", AudioInfo.LIKE_NET + ""};
        Cursor cursor = db.rawQuery("select count(*)from " + TABLE_NAME_AUDIO_INFO
                + " WHERE type=? or type=? ", args);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    /**
     * 获取最近所有歌曲
     *
     * @return
     */
    public List<AudioInfo> getAllRecentAudio() {
        List<AudioInfo> list = new ArrayList<AudioInfo>();
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {AudioInfo.RECENT_LOCAL + "", AudioInfo.RECENT_NET + ""};
        Cursor cursor = db.query(TABLE_NAME_AUDIO_INFO, null,
                "type=? or type=?", args, null, null,
                "create_time desc", null);
        while (cursor.moveToNext()) {
            AudioInfo audioInfo = getAudioInfoFrom(cursor);
            //
            if (audioInfo.getType() == AudioInfo.RECENT_LOCAL) {
                audioInfo.setType(AudioInfo.LOCAL);
            } else {
                audioInfo.setType(AudioInfo.NET);
            }
            list.add(audioInfo);
        }
        cursor.close();
        return list;
    }

    /**
     * 获取所有喜欢的歌曲列表
     *
     * @return
     */
    public List<AudioInfo> getAllLikeAudio() {
        List<AudioInfo> list = new ArrayList<AudioInfo>();
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {AudioInfo.LIKE_LOCAL + "", AudioInfo.LIKE_NET + ""};
        Cursor cursor = db.query(TABLE_NAME_AUDIO_INFO, null,
                "type=? or type=?", args, null, null,
                "create_time desc", null);
        while (cursor.moveToNext()) {
            AudioInfo audioInfo = getAudioInfoFrom(cursor);
            //
            if (audioInfo.getType() == AudioInfo.LIKE_LOCAL) {
                audioInfo.setType(AudioInfo.LOCAL);
            } else {
                audioInfo.setType(AudioInfo.NET);
            }
            list.add(audioInfo);
        }
        cursor.close();
        return list;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE);
        } catch (SQLException e) {
            Log.i("error", "create table failed");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        try {
            db.execSQL("drop table if exists " + TABLE_NAME_AUDIO_INFO);
        } catch (SQLException e) {
            Log.i("error", "drop table failed");
        }
        onCreate(db);
    }
}
