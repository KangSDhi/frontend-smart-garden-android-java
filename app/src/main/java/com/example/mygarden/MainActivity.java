package com.example.mygarden;

import android.content.Intent;
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

public class MainActivity extends AppCompatActivity implements HttpHandler.HttpHandlerListener {

    private TextView mTextView;
    private HttpHandler mHttpHandler;

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

        String url = "http://103.117.57.94:3000/api/garden/last";

        mHttpHandler = new HttpHandler(this);

        mHttpHandler.startFetchingData(url, INTERVAL);

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