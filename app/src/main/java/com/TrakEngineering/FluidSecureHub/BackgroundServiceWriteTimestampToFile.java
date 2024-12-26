package com.TrakEngineering.FluidSecureHub;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;



/**
 * Created by root on 4/9/18.
 */

public class BackgroundServiceWriteTimestampToFile extends BackgroundService{


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            super.onStart(intent, startId);
            Bundle extras = intent.getExtras();
            if (extras == null) {
                Log.d("Service", "null");
                this.stopSelf();
            } else {

                System.out.println("Write TimeStamp to file Service is on...");
                AppConstants.writeTimeStamp("BackgroundServiceWriteTimestampToFile ~~~~~~~~~");

            }

        } catch (NullPointerException e) {
            System.out.println(e);
        }
        return Service.START_STICKY;
    }
}
