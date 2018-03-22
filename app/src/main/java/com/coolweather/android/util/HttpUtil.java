package com.coolweather.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by uidq1246 on 2018-3-22.
 * 发送okhttp请求
 */

public class HttpUtil {

    public static void sendOkHttpRequest(String address , okhttp3.Callback callback){

        //创建请求客户端对象
        OkHttpClient client = new OkHttpClient();
        //常见请求对象
        Request request = new Request.Builder()
                .url(address)
                .build();
        //将应答内容在回调函数中处理
        client.newCall(request).enqueue(callback);

    }

}
