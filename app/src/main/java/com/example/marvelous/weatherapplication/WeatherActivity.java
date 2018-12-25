package com.example.marvelous.weatherapplication;

import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.marvelous.weatherapplication.service.AutoUpdateService;
import com.example.marvelous.weatherapplication.util.HttpUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.air.now.AirNow;
import interfaces.heweather.com.interfacesmodule.bean.weather.Weather;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.Forecast;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.ForecastBase;
import interfaces.heweather.com.interfacesmodule.bean.weather.lifestyle.LifestyleBase;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

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
    private TextView fengLi;
    private TextView tiGanWenDu;
    private TextView shiDu;
    private TextView nengJianDu;
    private TextView ziWaiXian;
    private TextView qiYa;

    private ImageView weatherInfoImg;
    private ImageView bingPicImg;

    public SwipeRefreshLayout swipeRefreshLayout;
    private String mWeatherId;

    public DrawerLayout drawerLayout;
    private Button navButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_weather);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);


        bingPicImg = findViewById(R.id.bing_pic_img);

        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);
        weatherInfoImg = findViewById(R.id.weather_info_img);
        fengLi = findViewById(R.id.fengli);
        tiGanWenDu = findViewById(R.id.tiganwendu);
        shiDu = findViewById(R.id.shidu);
        nengJianDu = findViewById(R.id.nengjiandu);
        ziWaiXian = findViewById(R.id.ziwaixian);
        qiYa = findViewById(R.id.qiya);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String bingPic = prefs.getString("bing_pic",null);
        if(bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }

        HeConfig.init("HE1812221300471524", "2ad26945ae204aba8e71ba4d313b8b5c");
        HeConfig.switchToFreeServerNode();

        String weatherId = getIntent().getStringExtra("weather_id");
        mWeatherId = getIntent().getStringExtra("weather_id");

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
                requestAir(mWeatherId);
            }
        });

        requestWeather(weatherId);
        requestAir(weatherId);
    }

    public void requestWeather(String weatherId) {
        HeWeather.getWeather(this, weatherId, new HeWeather.OnResultWeatherDataListBeansListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.i("Log", "error", throwable);
                //swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onSuccess(List<Weather> list) {
                //Log.i("Log", "onSuccess: " + new Gson().toJson(list));
                showWeatherInfo(list.get(0));
                //swipeRefreshLayout.setRefreshing(false);
            }
        });
        //loadBingPic();
    }

    public void requestAir(String weatherId) {
        HeWeather.getAirNow(this, weatherId, new HeWeather.OnResultAirNowBeansListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.i("Log", "error", throwable);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onSuccess(List<AirNow> list) {
                showAirInfo(list.get(0));
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.getBasic().getLocation();
        //Log.i("Tag", cityName);
        titleCity.setText(cityName);
        String updateTime = weather.getUpdate().getLoc();
        titleUpdateTime.setText(updateTime);
        String degree = weather.getNow().getTmp() + "℃";
        degreeText.setText(degree);
        String weatherInfo = weather.getNow().getCond_txt();
        fengLi.setText(weather.getNow().getWind_dir()+"\n"+weather.getNow().getWind_spd()+"km/h");
        tiGanWenDu.setText("体感温度" +"\n"+weather.getNow().getFl()+"℃");
        shiDu.setText("湿度"+"\n"+weather.getNow().getHum()+"%");
        nengJianDu.setText("能见度"+"\n"+weather.getNow().getVis()+"千米");
        ziWaiXian.setText("降水量"+"\n"+weather.getNow().getPcpn()+"mm");
        qiYa.setText("气压"+"\n"+weather.getNow().getPres()+"百帕");
        //Log.i("Tag", weatherInfo);
        //System.out.println(weatherInfo + "??????");
        weatherInfoText.setText(weatherInfo);
        if (weatherInfo.equals("晴")) {
            weatherInfoImg.setImageResource(R.drawable.qing);
        } else if (weatherInfo.equals("大雨")) {
            weatherInfoImg.setImageResource(R.drawable.dayu);
        } else if (weatherInfo.equals("多云")) {
            weatherInfoImg.setImageResource(R.drawable.duoyun);
        } else if (weatherInfo.equals("小雨")) {
            weatherInfoImg.setImageResource(R.drawable.xiaoyu);
        } else if (weatherInfo.equals("阴")) {
            weatherInfoImg.setImageResource(R.drawable.yintian);
        }
        forecastLayout.removeAllViews();
        for (ForecastBase forecastBase : weather.getDaily_forecast()) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            ImageView infoImg = view.findViewById(R.id.info_img);
            String foreText = forecastBase.getCond_txt_d();
            if (foreText.equals("晴")) {
                infoImg.setImageResource(R.drawable.qing);
            } else if (foreText.equals("大雨")) {
                infoImg.setImageResource(R.drawable.dayu);
            } else if (foreText.equals("多云")) {
                infoImg.setImageResource(R.drawable.duoyun);
            } else if (foreText.equals("小雨")) {
                infoImg.setImageResource(R.drawable.xiaoyu);
            } else if (foreText.equals("阴")) {
                infoImg.setImageResource(R.drawable.yintian);
            }
            dateText.setText(forecastBase.getDate());
            maxText.setText("最高："+forecastBase.getTmp_max() + "℃");
            minText.setText("最高："+forecastBase.getTmp_min() + "℃");
            forecastLayout.addView(view);
        }
        for (LifestyleBase lifestyleBase : weather.getLifestyle()) {
            if (lifestyleBase.getType().equals("comf")) {
                comfortText.setText("舒适度: " + lifestyleBase.getBrf() + "\n\n" + lifestyleBase.getTxt());
            }
            if (lifestyleBase.getType().equals("cw")) {
                carWashText.setText("洗车指数: " + lifestyleBase.getBrf() + "\n\n" + lifestyleBase.getTxt());
            }
            if (lifestyleBase.getType().equals("sport")) {
                sportText.setText("运动建议: " + lifestyleBase.getBrf() + "\n\n" + lifestyleBase.getTxt());
            }
        }
        //weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    private void showAirInfo(AirNow airNow) {
        aqiText.setText(airNow.getAir_now_city().getAqi());
        pm25Text.setText(airNow.getAir_now_city().getPm25());
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
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }
}
