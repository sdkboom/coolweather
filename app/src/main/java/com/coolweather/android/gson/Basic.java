package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by uidq1246 on 2018-3-23.
 */

public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public class Update{

        @SerializedName("loc")
        public String updateTime;

    }

    public Update update;

}
