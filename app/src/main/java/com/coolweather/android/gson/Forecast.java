package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by uidq1246 on 2018-3-23.
 */

public class Forecast {

    public String date;

    @SerializedName("cond")
    public More more;

    @SerializedName("tmp")
    public Temperature temperature;

    public class More{

        @SerializedName("txt_d")
        public String info;

    }

    public class Temperature{

        public String min;

        public String max;

    }

}
