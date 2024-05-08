package com.example.mygarden;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mygarden.config.HttpHandler;
import com.example.mygarden.dto.GardenResponse;
import com.example.mygarden.services.NotificationForegroundService;

public class MainActivity extends AppCompatActivity implements HttpHandler.HttpHandlerListener {

    private TextView mTextView;
    private HttpHandler mHttpHandler;

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final long INTERVAL = 2000;

    private static final int RC_NOTIFICATION = 99;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, RC_NOTIFICATION);
        }

        mTextView = (TextView) findViewById(R.id.persenKelembapanTanah);

        String url = "http://103.117.57.94:3000/api/garden/last";

        mHttpHandler = new HttpHandler(this);

        mHttpHandler.startFetchingData(url, INTERVAL);

        Intent serviceIntent = new Intent(this, NotificationForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RC_NOTIFICATION){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Notification Allowed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification Denied", Toast.LENGTH_SHORT).show();
            }
        }
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