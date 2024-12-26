package com.TrakEngineering.FluidSecureHub;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.IBinder;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class BatteryBackgroundService extends Service {


    String TAG = "BatteryBackgroundService";
    int BATTERY_LEVEL_THRESHOLD = 30;

    public BatteryBackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        System.out.println("BatteryBackgroundService- "+new Date());

        try {
            BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
            int level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

            System.out.println(String.valueOf(level) + "%");

            if (Constants.FS_1_STATUS.equalsIgnoreCase("FREE") && Constants.FS_2_STATUS.equalsIgnoreCase("FREE") && Constants.FS_3_STATUS.equalsIgnoreCase("FREE") && Constants.FS_4_STATUS.equalsIgnoreCase("FREE") && Constants.FS_5_STATUS.equalsIgnoreCase("FREE") && Constants.FS_6_STATUS.equalsIgnoreCase("FREE")) {
                if (level < BATTERY_LEVEL_THRESHOLD) {

                    System.out.println("BatteryReceiver :: " + String.valueOf(level) + "%");

                    if (checkSharedPrefBatteryDate(BatteryBackgroundService.this)) {
                        setSharedPrefBatteryDate(BatteryBackgroundService.this, "wait");
                        //System.out.println("BatteryReceiver MANGESH");
                        new CallSureMDMRebootDevice(BatteryBackgroundService.this).execute();
                    }
                }
            } else {
                System.out.println("BatteryReceiver :: Transaction is ongoing");
            }

        } catch (Exception e) {
            AppConstants.writeInFile("BatteryBackgroundService- " + e.getMessage());
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private void setSharedPrefBatteryDate(Context myctx, String flag) {
        SharedPreferences sharedPref = myctx.getSharedPreferences("suremdm", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("last_date", AppConstants.currentDateFormat("dd/MM/yyyy"));
        editor.putString("flag", flag);
        editor.apply();

    }

    private boolean checkSharedPrefBatteryDate(Context myctx) {
        SharedPreferences sharedPrefODO = myctx.getSharedPreferences("suremdm", Context.MODE_PRIVATE);
        String last_date = sharedPrefODO.getString("last_date", "");
        String flag = sharedPrefODO.getString("flag", "");

        String curr_date = AppConstants.currentDateFormat("dd/MM/yyyy");

        System.out.println(last_date + "  -" + flag + "-  " + curr_date);

        if (curr_date.trim().equalsIgnoreCase(last_date.trim())) {
            /*
            if (flag.equalsIgnoreCase("go") || flag.isEmpty())
                return false;
            else
                return true;
            */
                return false;
        } else
            return true;

    }


    public class CallSureMDMRebootDevice extends AsyncTask<Void, Void, String> {

        private Context classContext;

        private CallSureMDMRebootDevice(Context ctx) {
            classContext = ctx;
        }


        protected String doInBackground(Void... arg0) {
            String resp = "";

            try {

                SharedPreferences sharedPref = classContext.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                String PersonEmail = sharedPref.getString(AppConstants.USER_EMAIL, "");

                String parm1 = AppConstants.getIMEI(classContext) + ":" + PersonEmail + ":" + "SureMDMRebootDevice" + AppConstants.LANG_PARAM;


                System.out.println("parm1----" + parm1);

                String authString = "Basic " + AppConstants.convertStingToBase64(parm1);

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(4, TimeUnit.SECONDS);
                client.setReadTimeout(4, TimeUnit.SECONDS);
                client.setWriteTimeout(4, TimeUnit.SECONDS);


                Request request = new Request.Builder()
                        .url(AppConstants.WEB_URL)
                        .addHeader("Authorization", authString)
                        .addHeader("ReqType", "battery")
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();


            } catch (Exception e) {

                System.out.println("Ex" + e.getMessage());
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "  CallSureMDMRebootDevice  --Exception " + e);
            }

            return resp;
        }


        @Override
        protected void onPostExecute(String result) {

            System.out.println("CallSureMDMRebootDevice = " + result);

            if (result.toLowerCase().contains("success"))
                setSharedPrefBatteryDate(classContext, "go");

            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "  CallSureMDMRebootDevice onPostExecute " + result);


        }
    }
}