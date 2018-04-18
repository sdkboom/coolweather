package com.coolweather.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    private String TAG = getClass().getSimpleName();

    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e(TAG,"服务已开启...");
        updateWeather();
        updateBingPic();
        //获取唤醒服务的管理器对象
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8*60*60*1000;
        //唤醒时间设定
        long triggerTime = SystemClock.elapsedRealtime() + anHour;
        //创建延时意图对象
        Intent i = new Intent(this,AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        //设置意图唤醒
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateBingPic() {
        //访问地址
        String bingUrl = "http://guolin.tech/api/bing_pic";
        //发起请求
        HttpUtil.sendOkHttpRequest(bingUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                if(responseString != null){
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                    editor.putString("bing_pic",responseString);
                    editor.apply();
                }
            }
        });
    }

    private void updateWeather() {
        //获取存储的天气信息字符串
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = pref.getString("weather",null);
        //判断字符串是否为空
        if(weatherString != null){
            //获取天气对象
            final Weather weather = Utility.handleWeatherResponse(weatherString);
            //获取weatherId
            String weatherId = weather.basic.weatherId;
            //创建访问服务器的地址字符串
            String url = "http://10.0.2.2/weather.json";
            //发送http请求
            HttpUtil.sendOkHttpRequest(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //获取回应对象中的字符串
                    String responseString = response.body().string();
                    //获取Weather对象
                    Weather weather1 = Utility.handleWeatherResponse(responseString);
                    //判断获取的天气对象
                    if(weather1 != null && "ok".equals(weather1.status)){
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("weather",responseString);
                        editor.apply();
                    }
                }
            });

        }
    }
}
