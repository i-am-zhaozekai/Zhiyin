package com.haha.zy.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 04/06/2018
 */

public class HttpClientUtil {

    private static final String DEFAULT_URL_ENCODER = "UTF-8";
    private static final char PARAM_SEPARATOR_CHAR = '&';

    /**
     * 连接超时时间
     */
    public final static int CONNECT_TIMEOUT = 60;
    /**
     * 读取超时时间
     */
    public final static int READ_TIMEOUT = 100;
    /**
     * 写超时时间
     */
    public final static int WRITE_TIMEOUT = 60;

    private static final int METHOD_GET = 0;
    private static final int METHOD_POST = 1;

    public interface Callback {
        void onFailure(Call call, IOException e);
        void onResponse(Call call, String response);
    }

    public static String httpGetRequest(String url, Map<String, String> headers, Map<String, Object> params)
            throws Exception {
        return request(url, headers, params, METHOD_GET);
    }

    public static String httpPostRequest(String url, Map<String, String> headers, Map<String, Object> params)
            throws Exception {
        return request(url, headers, params, METHOD_POST);
    }

    public static void httpGetRequestAsync(String url, Map<String, String> headers, Map<String, Object> params,
                                             Callback callback) throws Exception {
        requestAsync(url, headers, params, METHOD_GET, callback);
    }

    public static void httpPostRequestAsync(String url, Map<String, String> headers, Map<String, Object> params,
                                              Callback callback) throws Exception {
        requestAsync(url, headers, params, METHOD_POST, callback);
    }

    private static String request(String url, Map<String, String> headers, Map<String, Object> params,
                                  int httpMethod) throws Exception {
        OkHttpClient client = createClient();

        Request request = null;
        if (httpMethod == METHOD_POST) {
            request = createGetRequest(url, headers, params);
        } else if (httpMethod == METHOD_GET) {
            request = createPostRequest(url, headers, params);
        }

        Call call = client.newCall(request);

        Response response = call.execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            return null;
        }
    }

    private static void requestAsync(String url, Map<String, String> headers, Map<String, Object> params,
                                     int httpMethod, final HttpClientUtil.Callback callback) throws UnsupportedEncodingException {
        OkHttpClient client = createClient();

        Request request = null;
        if (httpMethod == METHOD_POST) {
            request = createGetRequest(url, headers, params);
        } else if (httpMethod == METHOD_GET) {
            request = createPostRequest(url, headers, params);
        }

        Call call = client.newCall(request);

        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (response.isSuccessful()) {
                        callback.onResponse(call, responseBody.string());
                    } else {
                        callback.onResponse(call, null);
                    }
                }
            }
        });
    }

    private static OkHttpClient createClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .build();

        return client;
    }


    private static Request createPostRequest(String url, Map<String, String> headers, Map<String, Object> params) {
        Request.Builder builder = new Request.Builder();

        // 添加 header
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                builder.addHeader(header.getKey(), header.getValue());
            }
        }

        // 添加 param
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, Object> param : params.entrySet()) {
                formBodyBuilder.add(param.getKey(), param.getValue().toString());
            }
        }

        return builder.post(formBodyBuilder.build()).url(url).build();
    }

    private static Request createGetRequest(String url, Map<String, String> headers, Map<String, Object> params)
            throws UnsupportedEncodingException {
        Request.Builder builder = new Request.Builder();

        // 添加 header
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                builder.addHeader(header.getKey(), header.getValue());
            }
        }

        StringBuilder paramSB = new StringBuilder();
        // 添加参数
        if (params != null && !params.isEmpty()) {
            boolean isFirst = false;
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (!isFirst) {
                    isFirst = true;
                } else {
                    paramSB.append(PARAM_SEPARATOR_CHAR);
                }

                // 对参数进行 URLEncoder
                paramSB.append(String.format("%s=%s", param.getKey(),
                        URLEncoder.encode(param.getValue().toString(), DEFAULT_URL_ENCODER)));

            }
        }
        String requestUrl = String.format("%s?%s", url, paramSB.toString());

        return builder.get().url(requestUrl).build();
    }
}
