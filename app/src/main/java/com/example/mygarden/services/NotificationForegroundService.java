package com.example.mygarden.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.mygarden.R;
import com.example.mygarden.config.HttpHandler;
import com.example.mygarden.dto.GardenResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationForegroundService extends Service {

    private static final String TAG = NotificationForegroundService.class.getSimpleName();

    private ExecutorService mExecutorService;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBulider;

    private final String CHANNEL_ID = "notification_garden";

    public NotificationForegroundService(){
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Notifikasi Garden", NotificationManager.IMPORTANCE_LOW);
            mNotificationManager = getSystemService(NotificationManager.class);
            mNotificationManager.createNotificationChannel(channel);
        }

        String reqUrl = "http://103.117.57.94:3000/api/garden/last";

        mNotificationBulider = new NotificationCompat.Builder(this, CHANNEL_ID);

        startForeground(1, mNotificationBulider.build());

        mExecutorService.execute(() -> {
            while (true){
                try {
                    URL url = new URL(reqUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    InputStream inputStream = connection.getInputStream();
                    String response = convertStreamToString(inputStream);
                    GardenResponse gardenResponse = GardenResponse.parseJson(response);
                    Log.i(TAG, "onStartCommand: " + gardenResponse.getData().getKelembapan());
                    Float kelembapan = gardenResponse.getData().getKelembapan();

                    if (kelembapan <= 40){
                        mNotificationBulider.setSmallIcon(R.drawable.baseline_yard_24)
                                .setContentTitle("Kelembapan Tanah Rendah")
                                .setContentText("Kelembapan Tanah: " + kelembapan + "%");
                        startForeground(1, mNotificationBulider.build());
                    } else {
                        stopForeground(true);
                    }

                    Thread.sleep(3000);
                } catch (Exception e){
                    Log.e(TAG, "onStartCommand: " + e.getMessage());
                }
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}
