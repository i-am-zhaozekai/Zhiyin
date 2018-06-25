package com.haha.zy.net.api.kugou;

import com.haha.zy.glide.ImageURLGetter;
import com.haha.zy.net.api.kugou.SingerAvatorService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 19/06/2018
 */

public class SingerAvatorURLGetter implements ImageURLGetter<String, String> {

    @Override
    public String getUrl(String... strings) {
        return getSingerImageUrl(strings[0]);
    }

    private String getSingerImageUrl(String singerName) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(SingerAvatorService.BASE_URL_SINGER_IMAGE).build();

        SingerAvatorService singerService = retrofit.create(SingerAvatorService.class);
        retrofit2.Call<ResponseBody> call = singerService.getSingerImageUrl(singerName, 400, 104, "softhead");
        try {
            Response<ResponseBody> response = call.execute();

            if (response.body() != null) {
                String responseStr = response.body().string();
                JSONObject jsonNode = new JSONObject(responseStr);
                int status = jsonNode.getInt("status");
                if (status == 1) {
                    return jsonNode.getString("url");
                }
            }
            return response.body().toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
