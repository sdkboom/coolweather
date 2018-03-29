package com.coolweather.android;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 问题可能是json文件内有不明标识符 2018-3-23 21：19注
 */

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;
    private String TAG = "WeatherActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        //初始化视图对象
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_timie);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);

        //获取SharePreference对象
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //获取weather字符串
        String weatherContent = prefs.getString("weather",null);
        //判断内容是否存在
        if(weatherContent == null){
            //无缓存数据  则去服务器查询
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);

        }else {
            //如果有缓存数据 则调用工具类中的处理天气数据的方法 获取Weather对象
            Log.e(TAG,weatherContent);
            Weather weather = Utility.handleWeatherResponse(weatherContent);
            //显示天气信息
            showWeatherInfo(weather);
        }
        Log.e(TAG, "onCreate: " + Thread.currentThread());
    }

    /**
     * 服务器请求天气信息
     * @param weatherId
     */
    private void requestWeather(final String weatherId) {

        Log.e(TAG,"从服务器获取天气信息开始...");

        //创建网址信息
        String url = "http://10.0.2.2/heweather.json";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //服务器请求数据失败提示
                        Toast.makeText(WeatherActivity.this,"服务器获取天气信息失败",Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //获取字节数组
               // byte[] responseBytes = response.body().bytes();

                //获取返回的字符串数据
                //final String responseContent = new String(responseBytes,"UTF-8");
                final String responseContent = response.body().string();
                Log.e(TAG, "onResponse: " + responseContent);
                if(responseContent.isEmpty()){
                    Log.e(TAG,"服务器返回数据为空");
                }

                //将获取的数据 进行处理得到Weather对象
                final Weather weather = Utility.handleWeatherResponse(responseContent);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //判断是否获取了weather对象
                        if(weather != null && "ok".equals(weather.status)){
                            //创建SharePreferences Editor对象来写入数据
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this)
                                    .edit();
                            editor.putString("weather",responseContent);
                            //提交写入的信息
                            editor.apply();
                            Log.e(TAG,"开始显示天气信息...");
                            //显示天气信息
                            showWeatherInfo(weather);
                        }else {
                            //提示天气信息获取失败
                            Toast.makeText(WeatherActivity.this,"天气信息获取失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

    }

    /**
     * 显示天气信息
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        Log.e(TAG, "showWeatherInfo: " + Thread.currentThread());
        //社会标题
        titleCity.setText(weather.basic.cityName);
        titleUpdateTime.setText(weather.basic.update.updateTime);

        //设置now中属性 当前温度 和  天气状况
        degreeText.setText(weather.now.temperature+"℃");
        weatherInfoText.setText(weather.now.more.info);

        //设置之后几天的预报信息
        forecastLayout.removeAllViews();
        for(Forecast forecast : weather.forecastList){
            //加载视图
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,null);
            //获取视图对象
            TextView dataText = view.findViewById(R.id.data_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView minText = view.findViewById(R.id.min_text);
            TextView maxText = view.findViewById(R.id.max_text);
            //设置视图内容
            dataText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            minText.setText(forecast.temperature.min);
            maxText.setText(forecast.temperature.max);
            //添加视图进forecastLayout
            forecastLayout.addView(view);
        }

        //设置aqi指数
        if(weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }

        //设置建议
        comfortText.setText(weather.suggestion.comfort.info);
        carWashText.setText(weather.suggestion.carWash.info);
        sportText.setText(weather.suggestion.sport.info);

        weatherLayout.setVisibility(View.VISIBLE);
    }
}
