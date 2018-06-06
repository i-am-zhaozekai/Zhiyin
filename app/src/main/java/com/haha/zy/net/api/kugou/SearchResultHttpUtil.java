package com.haha.zy.net.api.kugou;

import android.content.Context;

import com.haha.zy.audio.AudioInfo;
import com.haha.zy.net.ConnectivityUtil;
import com.haha.zy.net.HttpClientUtil;
import com.haha.zy.net.entity.SongInfoResult;
import com.haha.zy.net.model.HttpResult;
import com.haha.zy.util.FileUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索
 * Created by zhangliangming on 2017/8/2.
 */
public class SearchResultHttpUtil {

    public static HttpResult search(Context context, String keyword, String page, String pagesize) {
        HttpResult httpResult = new HttpResult();

        if (!ConnectivityUtil.ensureConnectivityState(context, httpResult)) {
            return httpResult;
        }

        try {
            Map<String, Object> returnResult = new HashMap<String, Object>();

            String url = "http://mobilecdn.kugou.com/api/v3/search/song";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("format", "json");
            params.put("keyword", keyword);
            params.put("page", page);
            params.put("pagesize", pagesize);
            // 获取数据
            String result = HttpClientUtil.httpGetRequest(url, null, params);
            if (result != null) {

                JSONObject jsonNode = new JSONObject(result);
                int status = jsonNode.getInt("status");
                if (status == 1) {
                    httpResult.setStatus(HttpResult.STATUS_SUCCESS);

                    JSONObject dataJsonNode = jsonNode.getJSONObject("data");
                    returnResult.put("total", dataJsonNode.getInt("total"));
                    JSONArray infoJsonNode = dataJsonNode.getJSONArray("info");
                    List<AudioInfo> lists = new ArrayList<AudioInfo>();
                    for (int i = 0; i < infoJsonNode.length(); i++) {
                        JSONObject infoDataNode = infoJsonNode.getJSONObject(i);
                        AudioInfo audioInfo = new AudioInfo();
                        audioInfo.setDuration(infoDataNode.getInt("duration") * 1000);
                        audioInfo.setDurationText(FileUtil.parseTime2TrackLength(audioInfo.getDuration()));
                        audioInfo.setType(AudioInfo.NET);
                        audioInfo.setStatus(AudioInfo.INIT);
                        audioInfo.setFileExt(infoDataNode.getString("extname"));
                        audioInfo.setFileSize(infoDataNode.getLong("filesize"));
                        audioInfo.setFileSizeText(FileUtil.getFileSize(audioInfo.getFileSize()));
                        audioInfo.setHash(infoDataNode.getString("hash").toLowerCase());

                        String singerName = infoDataNode.getString("singername");
                        audioInfo.setArtist(singerName.equals("")?"未知":singerName);

                        audioInfo.setTitle(infoDataNode.getString("songname"));

                        SongInfoResult songInfoResult = SongInfoHttpUtil.songInfo(context, audioInfo.getHash());
                        if (songInfoResult != null) {
                            audioInfo.setDownloadUrl(songInfoResult.getUrl());
                            //audioInfo.setAlbumUrl(songInfoResult.getImgUrl());
                        }

                        lists.add(audioInfo);

                    }
                    returnResult.put("rows", lists);
                    httpResult.setResult(returnResult);

                } else {
                    httpResult.setStatus(HttpResult.STATUS_ERROR);
                    httpResult.setErrorMsg("请求出错!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            httpResult.setStatus(HttpResult.STATUS_ERROR);
            httpResult.setErrorMsg(e.getMessage());
        }
        return httpResult;
    }
}
