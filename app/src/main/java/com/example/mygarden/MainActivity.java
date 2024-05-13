package com.example.mygarden;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mygarden.config.HttpHandler;
import com.example.mygarden.dto.GardenResponse;
import com.example.mygarden.service.NotificationService;

public class MainActivity extends AppCompatActivity implements HttpHandler.HttpHandlerListener {

    private TextView mTextView;

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final long INTERVAL = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mTextView = (TextView) findViewById(R.id.persenKelembapanTanah);

        HttpHandler mHttpHandler = new HttpHandler(this);

        String URL_LAST_DATA_GARDEN = "http://103.117.57.94:3000/api/garden/last";
        mHttpHandler.startFetchingData(URL_LAST_DATA_GARDEN, INTERVAL);

        if (!foregroundServiceRunning()){
            Intent serviceIntent = new Intent(this, NotificationService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            }
        }
    }

    public boolean foregroundServiceRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (NotificationService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResponse(GardenResponse response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "get response: " + response.getData().getKelembapan().toString());
                mTextView.setText(response.getData().getKelembapan().toString()+"%");
            }
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Error : " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}