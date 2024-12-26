package com.TrakEngineering.FluidSecureHub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenReceiver extends BroadcastReceiver {

    public static boolean screenOff;
    private String TAG = ScreenReceiver.class.getSimpleName();
    Date date1, date2;
    String ScreenOnTime = "",ScreenOffTime = "";

    @Override
    public void onReceive(Context context, Intent intent) {

        System.out.println("onReceive ");
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            screenOff = true;
            ScreenOffTime = CommonUtils.getTodaysDateTemp();
            Log.i(TAG, "SCREEN TURNED OFF --> BroadcastReceiver");

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            screenOff = false;
            Log.i(TAG, "SCREEN TURNED ON --> BroadcastReceiver");

            ScreenOnTime = CommonUtils.getTodaysDateTemp();
            int diff = getDate(ScreenOnTime);
            if (getDate(ScreenOnTime) > 10){

                /*
                Log.i(TAG,"Screen off time Exceded Restart app:");
                Intent mStartActivity = new Intent(context, SplashActivity.class);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                System.exit(0);*/

            }

        }

        if (AppConstants.DISABLE_ALL_REBOOT_OPTIONS.equalsIgnoreCase("N")) {
            Intent i = new Intent(context, BackgroundServiceHotspotCheck.class);
            i.putExtra("screen_state", screenOff);
            context.startService(i);
        }

    }

    public int getDate(String CurrentTime) {

        int DiffTime = 0;
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date1 = sdf.parse(CurrentTime);
            date2 = sdf.parse(ScreenOffTime);

            long diff = date1.getTime() - date2.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            DiffTime = (int) minutes;
            //System.out.println("~~~Difference~~~" + minutes);

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (NullPointerException n) {
            n.printStackTrace();
        }

        return DiffTime;
    }

}