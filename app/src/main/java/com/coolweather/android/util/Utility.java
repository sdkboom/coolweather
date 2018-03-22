package com.coolweather.android.util;

import android.text.TextUtils;
import android.util.Log;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by uidq1246 on 2018-3-22.
 */

public class Utility {

    private static String TAG = "Utility";

    /**
     * 解析和处理服务器返回的省级数据
     */
    public static boolean handleProvinceResponse(String response){

        Log.e(TAG,"处理省级数据开始...");

        //判断返回的数据是否为空
        if(!TextUtils.isEmpty(response)){
            try {
                //将获取的字符串型数据转为json数组
                JSONArray allProvinces = new JSONArray(response);
                //读取已JSONObject形式读取遍历数组中获取的数据
                for(int i = 0; i < allProvinces.length(); i++){
                    //获取json对象
                    JSONObject jsonObject = allProvinces.getJSONObject(i);
                    //创建Province对象存储读取的数据
                    Province province = new Province();
                    province.setProvinceCode(jsonObject.getInt("id"));
                    province.setProvinceName(jsonObject.getString("name"));
                    province.save();
                }
                Log.e(TAG,"省级数据处理成功！");
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response ,int provinceId){

        Log.e(TAG,"处理市级数据开始...");

        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCities = new JSONArray(response);
                for(int i = 0 ; i < allCities.length() ; i ++){
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityCode(cityObject.getInt("id"));
                    city.setCityName(cityObject.getString("name"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                Log.e(TAG,"市级数据处理成功！");
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountyResponse(String response , int cityId){

        Log.e(TAG,"处理县级数据开始...");

        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounties = new JSONArray(response);
                for(int i = 0 ; i < allCounties.length() ; i++){
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCountyName(countyObject.getString("name"));
                    county.setCityId(cityId);
                    county.save();
                }
            Log.e(TAG,"县级数据处理成功！");
            return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
