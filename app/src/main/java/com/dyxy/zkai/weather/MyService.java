package com.dyxy.zkai.weather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyService extends Service {

    private GetWeather getWeather = new GetWeather();

    public MyService() {
    }

    //服务启动执行
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("MyService","onStartCommand：启动成功");

        //如果接口没有初始化，则不调用getData
        if (getWeather.infoCallBack!=null) {
            getWeather.getData();
        }
        //TODO:执行定时任务
        //获取系统服务
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int time = 1 * 1000;//刷新时间
        long atTime = SystemClock.elapsedRealtime() + time;

        Intent i = new Intent(this,MyService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        alarmManager.cancel(pi);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,atTime,pi);

        return super.onStartCommand(intent, flags, startId);
    }

    //
    public class GetWeather extends Binder {

        public WeatherInfoCallBack infoCallBack;
        //注册接口  初始化接口
        public void regCallBack(WeatherInfoCallBack callBack){
            this.infoCallBack = callBack;
        }

        public void getData() {
            String url = "http://guolin.tech/api/weather?cityid=CN101121201&key=bc041b57b2d4918819d3974ac1285d9";
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .get().build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("MyService", "错误");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String data = response.body().string();
                    Log.d("MyService", "onResponse：" + data);

                    infoCallBack.onResult(data);
                }
            });
        }
    }

    @Override
    public IBinder onBind(Intent intent) {


        return getWeather;
    }
}
