package com.zhangshun.coolweather.activity;

import android.app.ProgressDialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import com.zhangshun.coolweather.model.City;
import com.zhangshun.coolweather.model.CoolWeatherDB;
import com.zhangshun.coolweather.model.County;
import com.zhangshun.coolweather.model.Province;

import com.zhangshun.coolweather.R;
import com.zhangshun.coolweather.util.HttpCallbackListener;
import com.zhangshun.coolweather.util.HttpUtil;
import com.zhangshun.coolweather.util.Utility;

import java.util.ArrayList;

public class ChooseAreaActivity extends AppCompatActivity {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY     = 1;
    public static final int LEVEL_COUNTY   = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private List<String> dataList = new ArrayList<String>();

    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中省份
     */
    private Province selectedProvince;

    /*
     * 选中城市
     */
    private City selectedCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("city_selected", false)) {
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();

            return;
        }


        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.choose_area);

        listView = (ListView) findViewById(R.id.list_view);

        titleText = (TextView) findViewById(R.id.title_text);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        coolWeatherDB = CoolWeatherDB.getInstance(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }
                else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCountries();
                }
                else if (currentLevel == LEVEL_COUNTY) {
                    String countyCode = countyList.get(position).getCountyCode();
                    Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                    intent.putExtra("county_code", countyCode);

                    startActivity(intent);

                    finish();
                }
            }
        });
        queryProvince();
    }

    /**
     *   查询所有的省， 优先从数据库查询, 如果没有查询到再从服务器上查询
     */
    private  void queryProvince() {
        provinceList = coolWeatherDB.loadProvince();

        if (provinceList.size() > 0) {

            dataList.clear();

            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }

            adapter.notifyDataSetChanged();

            listView.setSelection(0);
            titleText.setText("中国");

            currentLevel = LEVEL_PROVINCE;

        } else {
            queryFromServer(null, "Province");
        }
    }

    /**
     *   查询所有的城市， 优先从数据库查询, 如果没有查询到再从服务器上查询
     */
    private void queryCities() {

        cityList = coolWeatherDB.loadCity(selectedProvince.getId());

        if (cityList.size() > 0) {

            dataList.clear();

            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();

            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());

            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer(selectedProvince.getProvinceCode(), "City");
        }
    }

    /**
     *   查询所有的县， 优先从数据库查询, 如果没有查询到再从服务器上查询
     */
    private void queryCountries() {
        countyList = coolWeatherDB.loadCounty(selectedCity.getId());

        if (countyList.size() > 0) {

            dataList.clear();

            for (County county: countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();

            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());

            currentLevel = LEVEL_COUNTY;
        } else {
            queryFromServer(selectedCity.getCityCode(), "Country");
        }
    }

    /**
     * 从服务器上查询省市县的数据
     * @param code 代号
     * @param type 类型
     */
    private void queryFromServer(final String code, final String type) {
        String address;
        if (!TextUtils.isEmpty(code)) {
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }

        showProgressDialog();

        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;

                if ("Province".equals(type)) {
                    result = Utility.handleProvincesResponse(coolWeatherDB, response);
                } else if ("City".equals(type)) {
                    result = Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
                } else if ("Country".equals(type)) {
                    result = Utility.handleCoutiesResponse(coolWeatherDB, response, selectedCity.getId());
                }

                if (result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                                if ("Province".equals(type)) {
                                    queryProvince();
                                } else if ("City".equals(type)) {
                                    queryCities();
                                } else if ("Country".equals(type)) {
                                    queryCountries();
                                }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();

                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private  void showProgressDialog() {
        if (progressDialog == null ) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY) {
            queryCities();
        } else if (currentLevel == LEVEL_CITY) {
            queryProvince();
        } else {
            //queryCountries();
            finish();
        }
    }
}


