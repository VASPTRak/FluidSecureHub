package com.TrakEngineering.FluidSecureHub;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.TrakEngineering.FluidSecureHub.offline.OffBackgroundService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class BackgroundWorker extends Worker {

    Context context;
    WorkerParameters workerParameters;
    private static final String TAG = AppConstants.LOG_MAINTAIN + "-" + "WorkManager ";

    public BackgroundWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        this.workerParameters = workerParams;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork called.");
        //startProcess();
        return Result.success();
    }

    private void startProcess() {
        try {
            // Checking if the HUB app is running or not
            boolean isForeground = new ForegroundCheckTask().execute(context).get();
            Log.d(TAG, "startProcess: <The HUB App is running: " + ((isForeground) ? "Yes" : "No") + ">");
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "<The HUB App is running: " + ((isForeground) ? "Yes" : "No") + ">");
            if (isForeground) {
                Date date = new Date();   // given date
                Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
                calendar.setTime(date);   // assigns calendar to given date
                int currentHour24 = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format

                if (currentHour24 >= 0 && currentHour24 <= 3) { // Checking the time is in between 12 AM to 4 AM
                    if (checkLastOfflineBSStartDateTime()) { // (CurrDateTime - LastOfflineBSStartDateTime) > 10 min [To reduce repetitive calls]
                        saveLastOfflineBSStartDateTimeInSharedPref();
                        boolean isBSRunning = CommonUtils.checkServiceRunning(context, AppConstants.PACKAGE_BS_OffDataDownload);
                        if (!isBSRunning) {
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + "<Starting the Offline Background Service.>");
                            context.startService(new Intent(context, OffBackgroundService.class));
                        } else {
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + "<Offline Background Service is running.>");
                        }
                    } else {
                        Log.d(TAG, "startProcess: Skipped.");
                    }
                }
            } /*else {
                if (!AppConstants.IS_APP_RELAUNCHED) {
                    AppConstants.IS_APP_RELAUNCHED = true;
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + "<The HUB App is in the background. Launching the HUB App.>");
                    *//*Intent activity = new Intent(context, SplashActivity.class);
                    activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(activity);*//*
                    *//*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(new Intent(context, AppLaunchForegroundService.class));
                    }*//*
                    //showFullScreenNotification(context);
                } else {
                    Log.d(TAG, "startProcess: Skipped.");
                }
            }*/
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Exception in startProcess. Exception: " + e.getMessage());
        }
    }

    private void showFullScreenNotification(Context context) {
        AppConstants.writeInFile(TAG + "<in showFullScreenNotification...>");
        Intent fullScreenIntent = new Intent(context, SplashActivity.class);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                0,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "alarm_channel1")
                .setSmallIcon(R.drawable.fstest)
                .setContentTitle("Alarm")
                .setContentText("Wake up!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android 8+ requires a channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "alarm_channel1",
                    "Alarm Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setDescription("Used for alarm fullscreen");
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(1002, builder.build());
    }

    public void saveLastOfflineBSStartDateTimeInSharedPref() {
        SharedPreferences sharedPref = context.getSharedPreferences("WorkManagerExecutionInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("LastOfflineBSStartDateTime", AppConstants.currentDateFormat("yyyy-MM-dd HH:mm:ss"));
        editor.apply();
    }

    public boolean checkLastOfflineBSStartDateTime() {
        String CurrDateTime = CommonUtils.getTodaysDateTemp();
        SharedPreferences sharedPref = context.getSharedPreferences("WorkManagerExecutionInfo", Context.MODE_PRIVATE);
        String LastOfflineBSStartDateTime = sharedPref.getString("LastOfflineBSStartDateTime", "");
        if (LastOfflineBSStartDateTime.isEmpty()) {
            return true;
        } else {
            int diffMin = getDiffInMinutes(CurrDateTime, LastOfflineBSStartDateTime);
            return diffMin >= 10;
        }
    }

    private int getDiffInMinutes(String CurrDateTime, String LastOfflineBSStartDateTime) {
        int DiffTime = 0;
        Date date1, date2;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date1 = sdf.parse(CurrDateTime);
            date2 = sdf.parse(LastOfflineBSStartDateTime);
            long diff = date1.getTime() - date2.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            DiffTime = (int) minutes;
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
        }
        return DiffTime;
    }

    @Override
    public void onStopped() {
        super.onStopped();
        if (AppConstants.GENERATE_LOGS)
            AppConstants.writeInFile(TAG + "<Worker has been cancelled.>");
    }

    public class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

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
                    //if (appProcess.processName.equals(packageName)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "Exception in isAppOnForeground. Exception: " + e.getMessage());
            }
            return false;
        }
    }
}
