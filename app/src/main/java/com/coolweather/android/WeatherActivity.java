package com.coolweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.UiThread;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
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

    private ImageView bingPicImg;

    public SwipeRefreshLayout swipeFresh ;

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

    private FrameLayout flRootView;

    public DrawerLayout drawerLayout ;

    private Button navButton ;

    private String TAG = "WeatherActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //判断SDK版本进行视图与状态栏的融合
        if(Build.VERSION.SDK_INT > 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        //初始化视图对象
        bingPicImg = findViewById(R.id.bing_pic_img);
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
        flRootView = findViewById(R.id.fl_root);
        swipeFresh = findViewById(R.id.swipe_refresh);
        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);

        final String weatherId ;

        //设置导航按钮监听
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击导航栏后打开滑动布局
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        //获取SharePreference对象
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //获取weather字符串
        String weatherContent = prefs.getString("weather",null);
        //获取背景图片内容
        String bingPic = prefs.getString("bing_pic",null);

        //判断图片内容是否存在
        if(bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
            /*Glide.with(this).load(bingPic).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    flRootView.setBackground(new BitmapDrawable(resource));
                }
            });*/

        }else {
            loadBingPic();
        }

        //判断内容是否存在
        if(weatherContent == null){
            //无缓存数据  则去服务器查询
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);

        }else {
            //如果有缓存数据 则调用工具类中的处理天气数据的方法 获取Weather对象
            Log.e(TAG,weatherContent);
            Weather weather = Utility.handleWeatherResponse(weatherContent);
            weatherId = weather.basic.weatherId;
            //显示天气信息
            showWeatherInfo(weather);
        }

        //设置刷新监听
        swipeFresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });


        Log.e(TAG, "onCreate: " + Thread.currentThread());
    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                       /* Glide.with(WeatherActivity.this).load(bingPic).asBitmap().into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                flRootView.setBackground(new BitmapDrawable(resource));
                            }
                        });*/
                    }
                });
            }
        });
    }

    /**
     * 服务器请求天气信息
     * @param weatherId
     */
    public void requestWeather(final String weatherId) {

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
                        swipeFresh.setRefreshing(false);
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
                        swipeFresh.setRefreshing(false);
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

        Intent intent = new Intent(this,AutoUpdateService.class);
        startService(intent);
    }
}
