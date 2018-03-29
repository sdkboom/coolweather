package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by uidq1246 on 2018-3-23.
 */

public class Suggestion {

    public class Comfort{

        @SerializedName("txt")
        public String info;

    }

    public class CarWash{

        @SerializedName("txt")
        public String info;

    }

    public class Sport{

        @SerializedName("txt")
        public String info;

    }

    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CarWash carWash;

    public Sport sport;

}
