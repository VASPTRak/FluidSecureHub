package com.TrakEngineering.FluidSecureHub;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.TrakEngineering.FluidSecureHub.offline.OffBackgroundService;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class AppLaunchReceiver extends BroadcastReceiver {
    private static final String TAG = AppConstants.LOG_MAINTAIN + "-" + "AppLaunchReceiver ";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (AppConstants.isAllHosesAreFree()) {
                boolean isForeground = new ForegroundCheckTask().execute(context).get();
                /*if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "<The HUB App is running: " + ((isForeground) ? "Yes" : "No") + ">");*/
                if (!isForeground) {
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + "<The HUB App is in the background. Launching the HUB App.>");

                    Intent launchIntent = new Intent(context, SplashActivity.class);
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(launchIntent);

                } else {
                    Date date = new Date();   // given date
                    Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
                    calendar.setTime(date);   // assigns calendar to given date
                    int currentHour24 = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
                    if (currentHour24 >= 0 && currentHour24 <= 3) { // Checking the time is in between 12 AM to 4 AM
                        context.startService(new Intent(context, OffBackgroundService.class));
                    }
                }
            } /*else {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "<One of the link is busy.>");
            }*/
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Exception in onReceive. Exception: " + e.getMessage());
        }
    }
}

class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Context... params) {
        final Context context = params[0].getApplicationContext();
        return isAppOnForeground(context);
    }

    private boolean isAppOnForeground(Context context) {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager == null) {
                return false;
            }
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                return false;
            }
            final String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile("Exception while checking isAppOnForeground in AppLaunchReceiver. Exception: " + e.getMessage());
        }
        return false;
    }
}