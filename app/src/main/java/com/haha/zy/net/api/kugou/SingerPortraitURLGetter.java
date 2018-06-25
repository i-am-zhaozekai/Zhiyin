package com.haha.zy.net.api.kugou;

import android.text.TextUtils;

import com.haha.zy.glide.ImageURLGetter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 20/06/2018
 */

public class SingerPortraitURLGetter implements ImageURLGetter<Object, List<SingerPortraitInfo>> {

    @Override
    public List<SingerPortraitInfo> getUrl(Object... objects) {

        String singerName = (String) objects[0];
        String from = (String) objects[1];
        int width = (int) objects[2];
        int height = (int) objects[3];


        Retrofit retrofit = new Retrofit.Builder().baseUrl(SingerPortraitService.BASE_URL_SINGER_IMAGE).build();

        SingerPortraitService singerService = retrofit.create(SingerPortraitService.class);
        retrofit2.Call<ResponseBody> call = singerService.getSingerPortraitUrl(singerName, from, width, height);
        try {
            Response<ResponseBody> response = call.execute();

            if (response.body() != null) {
                return parseResponse(singerName, response.body().string(), from);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private List<SingerPortraitInfo> parseResponse(String singerName, String responseStr, String from) throws JSONException {
        if (TextUtils.isEmpty(responseStr)){
            return null;
        }

        JSONObject jsonNode = new JSONObject(responseStr);
        JSONArray arrayJsonNode = jsonNode.getJSONArray("array");
        if (arrayJsonNode != null) {
            List<SingerPortraitInfo> lists = new ArrayList<>();
            for (int i = 0; i < arrayJsonNode.length(); i++) {
                JSONObject arrayInfo = arrayJsonNode.getJSONObject(i);

                String imageUrl = null;
                if (from.equals("app")) {
                    imageUrl = arrayInfo.getString("key");
                } else {
                    if (arrayInfo.has("bkurl")) {
                        imageUrl = arrayInfo.getString("bkurl");
                    } else {
                        continue;
                    }
                }

                lists.add(new SingerPortraitInfo(singerName, imageUrl));
            }

            Collections.sort(lists);
            return lists;
        }

        return null;
    }


}
