package com.zhangshun.coolweather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    // http://www.weather.com.cn/data/list3/city.xml
    // http://www.weather.com.cn/data/list3/city21.xml
    // 天气详情
    // http://www.weather.com.cn/data/cityinfo/101190404.xml


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
