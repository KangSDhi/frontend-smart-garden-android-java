package com.example.mygarden.config;

import android.os.Handler;
import android.util.Log;

import com.example.mygarden.dto.GardenResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpHandler {

    private static final String TAG = HttpHandler.class.getSimpleName();

    public interface HttpHandlerListener {
        void onResponse(GardenResponse response);
        void onError(String error);
    }

    private HttpHandlerListener mListener;
    private ExecutorService mExecutorService;
    private Handler mHandler;

    public HttpHandler(HttpHandlerListener listener) {
        this.mListener = listener;
        mExecutorService = Executors.newSingleThreadExecutor();
        mHandler = new Handler();
    }

    public void startFetchingData(final String reqUrl, final long interval) {
        mHandler.post(() -> mExecutorService.execute(() -> {
            while (true) {
                try {
                    URL url = new URL(reqUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    InputStream in = conn.getInputStream();
                    String response = convertStreamToString(in);

                    GardenResponse gardenResponse = GardenResponse.parseJson(response);
                    mListener.onResponse(gardenResponse);
                    Thread.sleep(interval);
                }catch (Exception e){
                    Log.e(TAG, "Exception: " + e.getMessage());
                    mListener.onError(e.getMessage());
                }
            }
        }));
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
