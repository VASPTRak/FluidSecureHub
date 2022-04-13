package com.TrakEngineering.FluidSecureHub;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class BackgroundServiceScheduleReboot extends Service {
    String TAG = "BackgroundServiceScheduleReboot";
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            super.onStart(intent, startId);
            Log.e(TAG, "~~~~~start into BackgroundServiceScheduleReboot~~~~~");
            if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE") && Constants.FS_5STATUS.equalsIgnoreCase("FREE") && Constants.FS_6STATUS.equalsIgnoreCase("FREE")) {
                new CallSureMDMRebootDevice(BackgroundServiceScheduleReboot.this).execute();
            }
            else{
                Calendar cur_cal = new GregorianCalendar();
                cur_cal.setTimeInMillis(System.currentTimeMillis());//set the current time and date for this calendar
                Calendar cal = new GregorianCalendar();
                //cal.add(Calendar.DAY_OF_YEAR, cur_cal.get(Calendar.DAY_OF_YEAR));
                //cal.add(Calendar.DAY_OF_WEEK, cur_cal.get(Calendar.DAY_OF_MONTH));
                cal.set(Calendar.HOUR_OF_DAY, cur_cal.get(Calendar.HOUR));
                cal.set(Calendar.MINUTE, cur_cal.get(Calendar.MINUTE)+30);
                cal.set(Calendar.SECOND, cur_cal.get(Calendar.SECOND));
                cal.set(Calendar.MILLISECOND, cur_cal.get(Calendar.MILLISECOND));
                cal.set(Calendar.DATE, cur_cal.get(Calendar.DATE));
                cal.set(Calendar.MONTH, cur_cal.get(Calendar.MONTH));
                Intent name = new Intent(BackgroundServiceScheduleReboot.this, BackgroundServiceScheduleReboot.class);
                PendingIntent pintent = PendingIntent.getService(getApplicationContext(), WelcomeActivity.REBOOT_INTENT_ID, name, 0);
                AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                alarm.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pintent);
            }

            Log.e(TAG, "~~~~~stop BackgroundServiceScheduleReboot~~~~~");


        } catch (NullPointerException e) {
            if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  onStartCommand Execption " + e);
            Log.d("Ex", e.getMessage());
            this.stopSelf();
        }

        // return super.onStartCommand(intent, flags, startId);
        return Service.START_NOT_STICKY;

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

                String parm1 = AppConstants.getIMEI(classContext) + ":" + PersonEmail + ":" + "SureMDMRebootDevice";


                System.out.println("parm1----" + parm1);

                String authString = "Basic " + AppConstants.convertStingToBase64(parm1);

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(4, TimeUnit.SECONDS);
                client.setReadTimeout(4, TimeUnit.SECONDS);
                client.setWriteTimeout(4, TimeUnit.SECONDS);


                Request request = new Request.Builder()
                        .url(AppConstants.webURL)
                        .addHeader("Authorization", authString)
                        .addHeader("ReqType", "Normal")
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

                System.out.println(resp+"  ---------- Resopnse ------------------");


            } catch (Exception e) {

                System.out.println("Ex" + e.getMessage());
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  CallSureMDMRebootDevice  --Exception " + e);
            }

            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            System.out.println("CallSureMDMRebootDevice = " + result);

            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "  CallSureMDMRebootDevice onPostExecute " + result);


        }
    }


}

