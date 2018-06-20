package com.TrakEngineering.FluidSecureHubTest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;


import static com.TrakEngineering.FluidSecureHubTest.WelcomeActivity.wifiApManager;

/**
 * Created by User on 11/8/2017.
 */

public class BackgroundServiceHotspotCheck extends BackgroundService {

    private String TAG = "BackgroundServiceHotspotCheck";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        try {
            super.onStart(intent, startId);
            String From_BroadcastReceiver = "NO";
            Bundle extras = intent.getExtras();
            boolean screenOff = intent.getBooleanExtra("screen_state", true);
            Log.i(TAG, "Service is on==" + screenOff);
            if (extras == null) {
                Log.d(TAG, "null");
                this.stopSelf();
            } else {

                if (!screenOff && !CommonUtils.isHotspotEnabled(BackgroundServiceHotspotCheck.this) && Constants.hotspotstayOn) {

                    wifiApManager.setWifiApEnabled(null, true);  //Hotspot enabled
                    Log.i(TAG, "Connecting to hotspot, please wait....");
                    AppConstants.WriteinFile("BackgroundServiceHotspotCheck~~~~~~~~~" + "Hotspot ON");

                } else if (screenOff) {


                    if (isScreenOn(this) && !CommonUtils.isHotspotEnabled(BackgroundServiceHotspotCheck.this)) {

                        wifiApManager.setWifiApEnabled(null, true);  //Hotspot enabled
                        Log.i(TAG, "Connecting to hotspot, please wait....");
                        AppConstants.WriteinFile("BackgroundServiceHotspotCheck~~~~~~~~~" + "Hotspot ON");


                    } else if (!isScreenOn(this) && CommonUtils.isHotspotEnabled(BackgroundServiceHotspotCheck.this)){

                        wifiApManager.setWifiApEnabled(null, false);  //Hotspot disable
                        Log.i(TAG, "Disable hotspot, please wait....");
                        AppConstants.WriteinFile("BackgroundServiceHotspotCheck~~~~~~~~~" + "Hotspot OFF");

                    } else{

                        Log.i(TAG, "Dont do anything");

                    }
                }

            }

        } catch (NullPointerException e) {
            System.out.println(e);
        }
        return Service.START_STICKY;
    }

    public boolean isScreenOn(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn = true;
                }
            }
            return screenOn;
        } else {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            return pm.isScreenOn();
        }
    }
}
