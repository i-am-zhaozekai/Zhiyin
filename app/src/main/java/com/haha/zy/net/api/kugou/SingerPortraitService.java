package com.haha.zy.net.api.kugou;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 歌手写真 HTTP 请求
 */
public interface SingerPortraitService {

    static final String BASE_URL_SINGER_IMAGE = "http://artistpicserver.kuwo.cn/";

    @GET("pic.web?type=big_artist_pic&pictype=url&content=list&id=0&json=1&version=1")
    Call<ResponseBody> getSingerPortraitUrl(@Query("name") String singerName, @Query("from") String from,
                                         @Query("width") int width, @Query("height") int height);
}
