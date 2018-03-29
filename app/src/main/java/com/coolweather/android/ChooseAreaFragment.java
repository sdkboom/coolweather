package com.coolweather.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by uidq1246 on 2018-3-22.
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0 ;

    public static final int LEVEL_CITY = 1 ;

    public static final int LEVEL_COUNTY = 2 ;

    private ProgressDialog progressDialog ;

    private TextView titleView ;

    private Button backButton ;

    private ListView listView ;

    private ArrayAdapter<String> adapter ;

    private List<String> dataList = new ArrayList<>();

    /**
     * 省级列表
     */
    private List<Province> provinceList ;

    /**
     * 市级列表
     */
    private List<City> cityList;

    /**
     * 县级列表
     */
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince ;

    /**
     * 选中的市区
     */
    private City selectedCity ;

    /**
     * 当前选中的级别
     */
    private int currentLevel ;
    private String TAG = "ChooseAreaFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //加载fragment对应的布局
        View view = inflater.inflate(R.layout.choose_area,container,false);
        //获取视图对象
        titleView = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);
        //实例化listview的适配器
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //设置选项选中监听
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG,"列表视图选项点击事件执行");
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if(currentLevel == LEVEL_COUNTY){
                    Toast.makeText(getContext(),countyList.get(position).getWeatherId().toString(),Toast.LENGTH_LONG).show();
                    //若点击县级目录时   启动WeatherActivity
                    Intent intent = new Intent(getActivity(),WeatherActivity.class);
                    intent.putExtra("weather_id",countyList.get(position).getWeatherId());
                    startActivity(intent);
                    //关闭当前活动
                    getActivity().finish();
                    Log.e(TAG,"县级目录点击事件执行完毕");
                }
            }
        });
        //设置返回按钮监听
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel == LEVEL_COUNTY){
                    //当前为县级目录时  显示市级目录
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    //当前为市级目录时 显示省级目录
                    queryProvinces();
                }
            }
        });
        Log.e(TAG,"fragment初始化成功");
        //第一次创建时显示省级目录
        queryProvinces();
    }

    /**
     * 创建省级目录
     */
    private void queryProvinces() {

        Log.e(TAG,"省级目录创建开始...");

        titleView.setText("中国");
        backButton.setVisibility(View.GONE);
        //查找所有数据
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size() > 0){
            //数据库中查找成功  则获取列表元素名称  并进行显示于listView中
            dataList.clear();
            //遍历省级列表集合
            for(Province province:provinceList){
                //将省名称添加到dataList中  用于listView显示
                dataList.add(province.getProvinceName());
            }
            //更新列表视图
            adapter.notifyDataSetChanged();
            //设置默认选中位置
            listView.setSelection(0);
            //更新当前级别
            currentLevel = LEVEL_PROVINCE;
        }else{
            //数据库中查找失败  则通过url查找
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }

        Log.e(TAG,"省级目录创建成功！");

    }

    /**
     * 创建市级目录
     */
    private void queryCities() {

        Log.e(TAG,"市级目录创建开始...");

        //标题文本设置为当前省份名
        titleView.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        //从数据库中条件查找  该省的城市列表
        cityList = DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size() > 0){
            dataList.clear();
            for(City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }

        Log.e(TAG,"市级目录创建成功！");
    }

    /**
     * 创建县级目录
     */
    private void queryCounties() {

        Log.e(TAG,"县级目录创建开始...");

        //标题文本设置为当前城市名
        titleView.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        //从数据库中条件查找  该市的县级目录
        countyList = DataSupport.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size() > 0){
            dataList.clear();
            for(County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+selectedProvince.getProvinceCode()+"/"+cityCode;
            queryFromServer(address,"county");
        }

        Log.e(TAG,"县级目录创建成功！");
    }

    /**
     * 根据传入的地址和级别从服务器上查找省市县数据
     * @param address
     * @param level
     */
    private void queryFromServer(String address, final String level) {

        Log.e(TAG,"从服务器获取数据开始...");

        //从服务器加载数据时显示 让用户等待
        showProgressDialog();
        //发送请求
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            //此方法是在分线程中进行的
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //请求失败 文本提示“加载失败”
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                        closeProgressDialog();
                    }
                });
            }

            //请求成功
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //获取应答对象的字符串形式数据
                String responseText = response.body().string();
                boolean result = false;
                //判断查询类型  调用不同的方法处理数据存入数据库中
                if("province".equals(level)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if("city".equals(level)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("county".equals(level)){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            //判断级别  调用相应的方法  显示当前列表
                            if("province".equals(level)){
                                queryProvinces();
                            }else if("city".equals(level)){
                                queryCities();
                            }else if("county".equals(level)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
        Log.e(TAG,"服务器获取数据成功！");
    }

    private void showProgressDialog() {
        //显示progressDialog
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog(){
        //关闭进度条显示
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
