package com.haha.zy.net.api.kugou;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


/**
 * 歌手头像 HTTP 请求
 */
public interface SingerAvatorService {

    static final String BASE_URL_SINGER_IMAGE = "http://mobilecdn.kugou.com/";

    @GET("new/app/i/yueku.php")
    Call<ResponseBody> getSingerImageUrl(@Query("singer") String singerName, @Query("size") int size,
                                         @Query("cmd") int cmd, @Query("type") String type);
}
