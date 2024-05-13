package com.example.mygarden.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.mygarden.R;
import com.example.mygarden.dto.GardenResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationService extends Service {

    private static final String TAG = NotificationService.class.getSimpleName();
    private ExecutorService executorService;

    public NotificationService(){
        executorService = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String CHANNEL_ID = "HumidityNotification";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "fore", NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);

            startForeground(1001, notificationBuilder.build());

            String URL_LAST_DATA_GARDEN = "http://103.117.57.94:3000/api/garden/last";

            executorService.execute(() -> {
                while (true) {
                    try {
                        URL url = new URL(URL_LAST_DATA_GARDEN);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");

                        InputStream inputStream = connection.getInputStream();
                        String response = convertStreamToString(inputStream);
                        GardenResponse gardenResponse = GardenResponse.parseJson(response);
                        Log.i(TAG, "onStartCommand: " + gardenResponse.getData().getKelembapan());
                        Float kelembapan = gardenResponse.getData().getKelembapan();

                        if (kelembapan <= 40){
                            Log.i(TAG, "onStartCommand: Kelembapan Rendah");
                            notificationBuilder
                                    .setSmallIcon(R.drawable.baseline_local_florist_24)
                                    .setContentTitle("Kelembapan Tanah Rendah")
                                    .setContentText("Kelembapan Tanah: " + kelembapan);
                            notificationManager.notify(1001, notificationBuilder.build());
//                            startForeground(1001, notificationBuilder.build());
                        } else {
                            Log.i(TAG, "onStartCommand: Kelembapan Normal");
                            notificationManager.cancel(1001);
                            stopSelf();
                        }

                        Thread.sleep(3000);
                    } catch (Exception e) {
                        Log.e(TAG, "onStartCommand: " + e.getMessage());
                    }
                }
            });

        }
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
