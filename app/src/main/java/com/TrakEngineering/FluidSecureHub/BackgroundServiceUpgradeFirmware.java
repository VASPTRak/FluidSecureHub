package com.TrakEngineering.FluidSecureHub;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class BackgroundServiceUpgradeFirmware extends BackgroundService {


    String FUpgrade_URL = "";
    String URL_UPGRADE_START = FUpgrade_URL + "upgrade?command=start";
    String URL_RESET = FUpgrade_URL + "upgrade?command=reset";
    private static final String TAG = "BackgroundService_UpgradeFirmware";
    //public static String FOLDER_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FSBin/";
    public static String FOLDER_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/FSBin/";
    public static String PATH_BIN_FILE1 = "user1.2048.new.5.bin";


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            super.onStart(intent, startId);
            Bundle extras = intent.getExtras();
            if (extras == null) {
                Log.d("Service", "null");
                this.stopSelf();


            } else {

                FUpgrade_URL = (String) extras.get("FUpgrade_URL");
                URL_UPGRADE_START = FUpgrade_URL + "upgrade?command=start";
                URL_RESET = FUpgrade_URL + "upgrade?command=reset";

                System.out.println("FUpgrade_URL"+FUpgrade_URL);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        new CommandsPOST().execute(URL_UPGRADE_START, "");
                        System.out.println("tesssss" + URL_UPGRADE_START);

                        //upgrade bin
                        String LocalPath = FOLDER_PATH + PATH_BIN_FILE1;

                        File f = new File(LocalPath);

                        if (f.exists()) {

                            new OkHttpFileUpload().execute(LocalPath, "application/binary");

                        } else {
                            Toast.makeText(getApplicationContext(), "File Not found " + LocalPath, Toast.LENGTH_LONG).show();
                        }


                    }

                }, 3000);

                ChangeUpgradeProcessFlag();
            }
        } catch (NullPointerException e) {
            if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "onStartCommand Execption " + e);
            Log.d("Ex", e.getMessage());
            this.stopSelf();
        }

        // return super.onStartCommand(intent, flags, startId);
        return Service.START_NOT_STICKY;
    }

    public class CommandsPOST extends AsyncTask<String, Void, String> {

        public String resp = "";


        protected String doInBackground(String... param) {


            try {


                MediaType JSON = MediaType.parse("application/json");

                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(JSON, param[1]);

                Request request = new Request.Builder()
                        .url(param[0])
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
                if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG+ "CommandsPOST doInBackground Execption " + e);
                stopSelf();
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {


            try {

                System.out.println("APFS_PIPE OUTPUT" + result);

            } catch (Exception e) {

                if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "CommandsPOST onPostExecute Execption " + e);
                System.out.println(e);
                stopSelf();
            }

        }
    }

    public class OkHttpFileUpload extends AsyncTask<String, Void, String> {

        public String resp = "";

        //ProgressDialog pd;

        @Override
        protected void onPreExecute() {
                /*
                pd = new ProgressDialog(DisplayMeterActivity.this);
                pd.setMessage("Upgrading FS unit...\nIt takes a minute.");
                pd.setCancelable(false);
                pd.show();
                */


        }

        protected String doInBackground(String... param) {


            try {
                String LocalPath = param[0];
                String Localcontenttype = param[1];

                MediaType contentype = MediaType.parse(Localcontenttype);

                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(contentype, readBytesFromFile(LocalPath));
                Request request = new Request.Builder()
                        .url(FUpgrade_URL)//HTTP_URL  192.168.43.210
                        .post(body)
                        .build();


                Response response = client.newCall(request).execute();
                System.out.println("tesssss1" + response);
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                return response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println(" resp......." + result);


            // pd.dismiss();
            try {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new CommandsPOST().execute(URL_RESET, "");

                        System.out.println("AFTER SECONDS 5");
                    }
                }, 5000);


            } catch (Exception e) {

                System.out.println(e);
            }

        }
    }

    private static byte[] readBytesFromFile(String filePath) {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {

            File file = new File(filePath);
            bytesArray = new byte[(int) file.length()];

            //read file into bytes[]
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return bytesArray;

    }


    public void ChangeUpgradeProcessFlag(){

        WelcomeActivity.IsUpgradeInprogress_FS1 = false;
        WelcomeActivity.IsUpgradeInprogress_FS1 = false;
        WelcomeActivity.IsUpgradeInprogress_FS1 = false;
        WelcomeActivity.IsUpgradeInprogress_FS1 = false;

    }


}
