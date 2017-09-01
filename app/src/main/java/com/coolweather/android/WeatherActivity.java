package com.coolweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private LinearLayout ll_forecast;
    private ScrollView sv_veather;
    private TextView tv_titleCity;
    private TextView tv_titleUpdateTime;
    private TextView tv_degree;
    private TextView tv_weatherInfor;
    private TextView tv_aqi;
    private TextView tv_pm25;
    private TextView tv_comfort;
    private TextView tv_carWash;
    private TextView tv_sport;
    private ImageView iv_bingPic;
    private SharedPreferences prefs;
    private String weatherId;
    private SwipeRefreshLayout swipeRefresh;
    private DrawerLayout drawerLayout;
    private Button btn_nav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSystemUiShow();
        setContentView(R.layout.activity_weather);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        initView();

        initWeatherInfo();

        initBingPic();

        setRefreshListener();
    }

    private void setRefreshListener(){
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
    }
    private void setSystemUiShow(){
        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }
    private void initWeatherInfo(){
        String weatherString = prefs.getString("weather",null);
        if(weatherString != null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else{
            weatherId = getIntent().getStringExtra("weather_id");
            sv_veather.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
    }

    private void initBingPic(){
        String bingPic = prefs.getString("bing_pic",null);
        if(bingPic != null){
            Glide.with(this).load(bingPic).into(iv_bingPic);
        }else{
            loadBingPic();
        }
    }
    private void initView(){
        ll_forecast = (LinearLayout) findViewById(R.id.forecast_layout);
        sv_veather = (ScrollView) findViewById(R.id.weather_layout);
        tv_titleCity = (TextView) findViewById(R.id.title_city);
        tv_titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        tv_degree = (TextView) findViewById(R.id.degree_text);
        tv_weatherInfor = (TextView) findViewById(R.id.weather_info_text);
        tv_aqi = (TextView) findViewById(R.id.aqi_text);
        tv_pm25 = (TextView) findViewById(R.id.pm25_text);
        tv_comfort = (TextView) findViewById(R.id.comfort_text);
        tv_carWash = (TextView) findViewById(R.id.car_wash_text);
        tv_sport = (TextView) findViewById(R.id.sport_text);
        iv_bingPic = (ImageView) findViewById(R.id.bing_pic_img);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        btn_nav = (Button) findViewById(R.id.nav_button);
        btn_nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    public void requestWeather(final String weatherId){
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                weatherId+"&key=bc0418b57b2d4918819d3974ac1285d9";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    public void showWeatherInfo(Weather weather){

        if(weather == null || "ok".equals(weather.status) == false) {
            return;
        }

        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;

        tv_titleCity.setText(cityName);
        tv_titleUpdateTime.setText(updateTime);
        tv_degree.setText(degree);
        tv_weatherInfor.setText(weatherInfo);
        ll_forecast.removeAllViews();

        for(Forecast forecast : weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, ll_forecast,false);
            TextView tv_date = (TextView) view.findViewById(R.id.date_text);
            TextView tv_info = (TextView) view.findViewById(R.id.info_text);
            TextView tv_max = (TextView) view.findViewById(R.id.max_text);
            TextView tv_min = (TextView) view.findViewById(R.id.min_text);
            
            tv_date.setText(forecast.date);
            tv_info.setText(forecast.more.info);
            tv_max.setText(forecast.temperature.max);
            tv_min.setText(forecast.temperature.min);
            ll_forecast.addView(view);
        }
        
        if(weather.aqi != null){
            tv_aqi.setText(weather.aqi.city.aqi);
            tv_pm25.setText(weather.aqi.city.pm25);
        }
        
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        
        tv_comfort.setText(comfort);
        tv_carWash.setText(carWash);
        tv_sport.setText(sport);
        
        sv_veather.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }


    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
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
                        Glide.with(WeatherActivity.this).load(bingPic).into(iv_bingPic);
                    }
                });
            }
        });
    }

    public void closeDrawers(){
        drawerLayout.closeDrawers();
    }

    public void startRefreshing(){
        swipeRefresh.setRefreshing(true);
    }
}
