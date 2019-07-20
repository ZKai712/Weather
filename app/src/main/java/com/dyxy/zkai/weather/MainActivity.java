package com.dyxy.zkai.weather;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.title_city)
    TextView titleCity;
    @BindView(R.id.title_update_time)
    TextView titleUpdateTime;
    @BindView(R.id.degree_text)
    TextView degreeText;
    @BindView(R.id.forecast_layout)
    LinearLayout forecastLayout;
    @BindView(R.id.aqi_text)
    TextView aqiText;
    @BindView(R.id.pm25_text)
    TextView pm25Text;
    @BindView(R.id.comfort_text)
    TextView comfortText;
    @BindView(R.id.car_wash_text)
    TextView carWashText;
    @BindView(R.id.sport_text)
    TextView sportText;
    @BindView(R.id.weather_layout)
    ScrollView weatherLayout;
    @BindView(R.id.weather_info_text)
    TextView weatherInfoText;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.bg_image)
    ImageView bgImage;
    private String TAG = "MainActivity";
    //测试github

    private MyService.GetWeather getWeather;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            getWeather = (MyService.GetWeather) service;
            getWeather.regCallBack(new WeatherInfoCallBack() {
                @Override
                public void onResult(String data) {
                    Message msg = handler.obtainMessage();
                    msg.what = 1;
                    msg.obj = data;
                    handler.sendMessage(msg);
                }
            });
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String data = msg.obj.toString();
                    Gson gson = new Gson();
                    //将data转化成weatherInfoBean实体类
                    WeatherInfoBean weatherInfoBean = gson.fromJson(data, WeatherInfoBean.class);
                    Log.d(TAG, "handleMessage:" + weatherInfoBean.getHeWeather().get(0).getNow().getTmp());

                    showWeather(weatherInfoBean.getHeWeather().get(0));

                    break;
                default:
                    break;
            }


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });

        getData();
        String path = "http://pics.sc.chinaz.com/files/pic/pic9/201701/fpic10256.jpg";
        //Glide加载网络图片
        Glide.with(this).load(path).into(bgImage);

        //开启服务
        Intent intent = new Intent(this,MyService.class);
        startService(intent);
        //绑定服务
        bindService(intent,connection,BIND_AUTO_CREATE);


    }

    private void showWeather(WeatherInfoBean.HeWeatherBean heWeatherBean) {
        titleCity.setText(heWeatherBean.getBasic().getCity());
        titleUpdateTime.setText(heWeatherBean.getBasic().getUpdate().getLoc());
        degreeText.setText(heWeatherBean.getNow().getTmp());
        weatherInfoText.setText(heWeatherBean.getNow().getCond_txt());
        aqiText.setText(heWeatherBean.getAqi().getCity().getAqi());
        pm25Text.setText(heWeatherBean.getAqi().getCity().getPm25());
        comfortText.setText(heWeatherBean.getSuggestion().getComf().getTxt());
        carWashText.setText(heWeatherBean.getSuggestion().getCw().getTxt());
        sportText.setText(heWeatherBean.getSuggestion().getSport().getTxt());

        forecastLayout.removeAllViews();

        for (WeatherInfoBean.HeWeatherBean.DailyForecastBean dailyForecastBean : heWeatherBean.getDaily_forecast()) {
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dataText = view.findViewById(R.id.data_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);

            dataText.setText(dailyForecastBean.getDate());
            infoText.setText(dailyForecastBean.getCond().getTxt_d());
            maxText.setText(dailyForecastBean.getTmp().getMax());
            minText.setText(dailyForecastBean.getTmp().getMin());

            forecastLayout.addView(view);

        }
    }

    private void getData() {
        String url = "http://guolin.tech/api/weather?cityid=CN101121201&key=bc041b57b2d4918819d3974ac1285d9";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get().build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "错误");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                Log.d(TAG, "onResponse：" + data);
                //获取msg对象
                Message msg = handler.obtainMessage();
                msg.what = 1;
                msg.obj = data;
                //交给handler处理
                handler.sendMessage(msg);
                refreshLayout.setRefreshing(false);
            }
        });
    }
}
