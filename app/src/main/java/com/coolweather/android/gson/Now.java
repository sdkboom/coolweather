package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by uidq1246 on 2018-3-23.
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    public class More{

        @SerializedName("txt")
        public String info;

    }

    @SerializedName("cond")
    public More more;

}
