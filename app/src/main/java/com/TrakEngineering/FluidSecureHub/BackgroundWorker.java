package com.TrakEngineering.FluidSecureHub;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.TrakEngineering.FluidSecureHub.offline.OffBackgroundService;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
        Date date = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        int CurrentHour24 = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format

        if (CurrentHour24 >= 0 && CurrentHour24 <= 3) { // Checking the time is in between 12 AM to 4 AM
            startProcess();
        } else {
            Log.d(TAG, "doWork: Skipped.");
        }
        return Result.success();
    }

    private void startProcess() {
        try {
            boolean isBSRunning = CommonUtils.checkServiceRunning(context, AppConstants.PACKAGE_BS_OffDataDownload);
            if (!isBSRunning) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "<Starting the Offline Background Service.>");
                context.startService(new Intent(context, OffBackgroundService.class));
            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "<Offline Background Service is running.>");
            }
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Exception in startProcess. Exception: " + e.getMessage());
        }
    }

    @Override
    public void onStopped() {
        super.onStopped();
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "<Worker has been cancelled.>");
    }
}
