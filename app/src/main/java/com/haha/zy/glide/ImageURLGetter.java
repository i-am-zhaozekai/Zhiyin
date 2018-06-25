package com.haha.zy.glide;


/**
 * 获取图片 URL 请求接口
 * @param <Param> 请求参数
 * @param <Response> 请求结果
 */
public interface ImageURLGetter<Param, Response> {

    Response getUrl(Param... params);
}
