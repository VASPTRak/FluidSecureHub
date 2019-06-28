package com.TrakEngineering.FluidSecureHub;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.server.ServerHandler;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import static com.TrakEngineering.FluidSecureHub.server.MyServer.ctx;
import static com.TrakEngineering.FluidSecureHub.server.ServerHandler.TEXT;

public class BackgroundServiceDownloadFirmware extends BackgroundService {

    private static String TAG = "BS_DFirmware";
    static ServerHandler serverHandler = new ServerHandler();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStart(intent, startId);
        Bundle extras = intent.getExtras();
        if (extras == null) {
            Log.i(TAG," Service ~null");
            this.stopSelf();
        }

        return Service.START_STICKY;
    }


    public static void FsvmDataAsyncCall(String jsonData, String authString){
        RequestBody body = RequestBody.create(TEXT, jsonData);
        com.squareup.okhttp.OkHttpClient httpClient = new com.squareup.okhttp.OkHttpClient();
        com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                .post(body)
                .addHeader("Authorization", authString)
                .url(AppConstants.webURL)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(com.squareup.okhttp.Request request, IOException e) {
                Log.e(TAG, "error in getting response using async okhttp call");
                if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  error in getting response using async okhttp call");
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(com.squareup.okhttp.Response response) throws IOException {

                ResponseBody responseBody = response.body();
                //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  FsvmDataAsyncCall "+response);
                if (!response.isSuccessful()) {
                    throw new IOException("Error response " + response);
                }else {

                    String result = responseBody.string();
                    Log.i(TAG," Result" + result);

                }

            }

        });


    }

    public static class DownloadFileFromURL extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                // getting file length
                int lenghtOfFile = conection.getContentLength();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file
                OutputStream output = new FileOutputStream(CommonUtils.FOLDER_PATH_FSVM_Firmware + f_url[1]);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }


        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            //pDialog.setProgress(Integer.parseInt(progress[0]));
        }


    }

    public static class DownloadTLDFileFromURL extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... f_url) {
            int count;

            try {

                File folder = new File(CommonUtils.FOLDER_PATH_TLD_Firmware);
                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdirs();
                }

                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                // getting file length
                int lenghtOfFile = conection.getContentLength();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file
                OutputStream output = new FileOutputStream(CommonUtils.FOLDER_PATH_TLD_Firmware + f_url[1]);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }


        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            //pDialog.setProgress(Integer.parseInt(progress[0]));
        }


    }

    public static class SaveTLDDataToServer extends AsyncTask<String, Void, String> {

        String jsonData;
        String authString;


        @SuppressLint("LongLogTag")
        @Override
        protected String doInBackground(String... params) {

            String response = "";
            try {

                jsonData = params[0];
                authString = params[1];

                response = serverHandler.PostTextData(ctx, AppConstants.webURL, jsonData, authString);


            } catch (Exception e) {
                Log.i(TAG, " SaveTLDDataToServer doInBackground " + e);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  SaveTLDDataToServer doInBackground " + e);
            }

            return response;
        }

        @SuppressLint("LongLogTag")
        @Override
        protected void onPostExecute(String resp) {

            Log.i(TAG, " SaveTLDDataToServer Response: " + resp);
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " SaveTLDDataToServer Response: " + resp);

        }
    }

}