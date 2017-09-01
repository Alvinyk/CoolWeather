package com.coolweather.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by alvin on 2017/8/31.
 */
public class HttpUtil {

    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        LogUtil.v("sendHttpRequest,address : "+address);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
