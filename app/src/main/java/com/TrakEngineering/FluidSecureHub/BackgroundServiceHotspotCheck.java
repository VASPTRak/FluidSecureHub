package com.TrakEngineering.FluidSecureHub;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;


import com.TrakEngineering.FluidSecureHub.enity.StatusForUpgradeVersionEntity;
import com.TrakEngineering.FluidSecureHub.enity.UserInfoEntity;
import com.TrakEngineering.FluidSecureHub.offline.OffDBController;
import com.TrakEngineering.FluidSecureHub.offline.OffTranzSyncService;
import com.TrakEngineering.FluidSecureHub.offline.OfflineConstants;
import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.TrakEngineering.FluidSecureHub.WelcomeActivity.wifiApManager;
import static com.TrakEngineering.FluidSecureHub.server.ServerHandler.TEXT;

/**
 * Created by User on 11/8/2017.
 */

public class BackgroundServiceHotspotCheck extends BackgroundService {

    private String TAG = AppConstants.LOG_BACKGROUND + " BS_HotspotCheck ";
    ConnectionDetector cd = new ConnectionDetector(BackgroundServiceHotspotCheck.this);
    OffDBController offcontroller = new OffDBController(BackgroundServiceHotspotCheck.this);


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ChangeWifiState();

        SharedPreferences sharedPrefOAS = BackgroundServiceHotspotCheck.this.getSharedPreferences(AppConstants.sharedPref_OfflineAzureSync, Context.MODE_PRIVATE);
        String datetimeOffline = sharedPrefOAS.getString("datetime", "");
        String dtOfflineFormat="yyyy-MM-dd HH:mm";


        if(datetimeOffline.trim().isEmpty())
        {
            SharedPreferences pref = BackgroundServiceHotspotCheck.this.getSharedPreferences(AppConstants.sharedPref_OfflineAzureSync, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("datetime", AppConstants.currentDateFormat(dtOfflineFormat));
            editor.commit();

        }else
        {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(dtOfflineFormat);
                Date savedDT = sdf.parse(datetimeOffline);
                Date currentDT= new Date();

                long diff =  currentDT.getTime() - savedDT.getTime();
                int numOfDays = (int) (diff / (1000 * 60 * 60 * 24));
                int hours = (int) (diff / (1000 * 60 * 60));
                int minutes = (int) (diff / (1000 * 60));
                int seconds = (int) (diff / (1000));



                if(minutes>=15)
                {
                    SharedPreferences pref = BackgroundServiceHotspotCheck.this.getSharedPreferences(AppConstants.sharedPref_OfflineAzureSync, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("datetime", AppConstants.currentDateFormat(dtOfflineFormat));
                    editor.commit();

                   // SyncSqliteData();

                }

            }catch (Exception e)
            {

            }

        }

        try {
            super.onStart(intent, startId);
            Bundle extras = intent.getExtras();
            boolean screenOff = intent.getBooleanExtra("screen_state", true);
            if (extras == null) {
                Log.d(TAG, "null");
                this.stopSelf();
            } else {

                /*//Enable bluetooth
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                mBluetoothAdapter.enable();*/

                if(CommonUtils.isHotspotEnabled(BackgroundServiceHotspotCheck.this))
                {
                    AppConstants.COUNT_HOTSPOT_MAIL=0;
                    System.out.println("HOT SPOT--ENAbled");
                }else {
                    AppConstants.COUNT_HOTSPOT_MAIL++;
                    System.out.println("HOT SPOT--no enabled");
                }

                System.out.println("HOT SPOT--COUNT: "+AppConstants.COUNT_HOTSPOT_MAIL);

                if(AppConstants.COUNT_HOTSPOT_MAIL>14)
                {
                    AppConstants.COUNT_HOTSPOT_MAIL=0;

                    //AppConstants.WriteinFile(TAG+" enableMobileHotspotmanuallyStartTimer1");
                    //if (!CommonUtils.isHotspotEnabled(BackgroundServiceHotspotCheck.this) && Constants.hotspotstayOn && !AppConstants.IsBTLinkSelectedCurrently){
                    //    CommonUtils.enableMobileHotspotmanuallyStartTimer(this);
                }


                }


                //Enable hotspot Logic
                if (AppConstants.FlickeringScreenOff) {

                    Log.i(TAG, "Dont do anything Screen off to overcome Flickering issue");
                    AppConstants.FlickeringScreenOff = false; //Do not disable hotspot

                } else if (!screenOff && !CommonUtils.isHotspotEnabled(BackgroundServiceHotspotCheck.this) && Constants.hotspotstayOn) {

                    if (CheckAllHosesAreFree() && !AppConstants.isAllLinksAreBTLinks) {
                        wifiApManager.setWifiApEnabled(null, true);  //Hotspot enabled
                        Log.i(TAG, "Connecting to hotspot, please wait....");
                        //if (AppConstants.GenerateLogs)AppConstants.WriteinFile( TAG+"  Hotspot ON--1");
                    }

                } else if (screenOff) {

                    if (isScreenOn(this) && !CommonUtils.isHotspotEnabled(BackgroundServiceHotspotCheck.this) && Constants.hotspotstayOn) {

                        if (CheckAllHosesAreFree() && !AppConstants.isAllLinksAreBTLinks) {
                            wifiApManager.setWifiApEnabled(null, true);  //Hotspot enabled
                            Log.i(TAG, "Connecting to hotspot, please wait....");
                        }

                        //if (AppConstants.GenerateLogs)AppConstants.WriteinFile( TAG+"  Hotspot ON--2");

                    } /*else if (!isScreenOn(this) && CommonUtils.isHotspotEnabled(BackgroundServiceHotspotCheck.this)) {


                        if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE")) {
                            wifiApManager.setWifiApEnabled(null, false);  //Hotspot disable
                            Log.i(TAG, "Disable hotspot, please wait....");
                            if (AppConstants.GenerateLogs)AppConstants.WriteinFile("BackgroundServiceHotspotCheck~~~~~~~~~" + "Hotspot OFF");
                        } else {
                            Log.i(TAG, "Can not disable hotspot, One of the link is busy...");
                        }

                    }*/ else {

                        Log.i(TAG, "Dont do anything");

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

    public void SendEmailMobileHotspotErrorEmail() {

        UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetailsCC(BackgroundServiceHotspotCheck.this);

        StatusForUpgradeVersionEntity objEntityClass2 = new StatusForUpgradeVersionEntity();
        objEntityClass2.IMEIUDID = AppConstants.getIMEI(BackgroundServiceHotspotCheck.this);
        objEntityClass2.HubName = userInfoEntity.PersonName;
        objEntityClass2.SiteName = userInfoEntity.FluidSecureSiteName;

        Gson gson = new Gson();
        String parm2 = gson.toJson(objEntityClass2);

        String userEmail = CommonUtils.getCustomerDetailsCC(BackgroundServiceHotspotCheck.this).PersonEmail;
        //----------------------------------------------------------------------------------
        String parm1 = AppConstants.getIMEI(BackgroundServiceHotspotCheck.this) + ":" + userEmail + ":" + "MobileHotspotErrorEmail" + AppConstants.LANG_PARAM;
        String authString = "Basic " + AppConstants.convertStingToBase64(parm1);


        RequestBody body = RequestBody.create(TEXT, parm2);
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(AppConstants.webURL)
                .post(body)
                .addHeader("Authorization", authString)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "error in getting response");
                System.out.println("HOT SPOT---ERRR");
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(Response response) throws IOException {

                ResponseBody responseBody = response.body();
                if (!response.isSuccessful()) {
                    throw new IOException("Error response " + response);
                } else {

                    String result = responseBody.string();
                    System.out.println("HOT SPOT-" + result);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " SendEmailReaderNotConnectedAsyncCall ~Result\n" + result);

                    try {

                        JSONObject jsonObjectSite = null;
                        jsonObjectSite = new JSONObject(result);

                        String ResponseMessageSite = jsonObjectSite.getString(AppConstants.RES_MESSAGE);

                        if (ResponseMessageSite.equalsIgnoreCase("success")) {
                            SharedPreferences pref = BackgroundServiceHotspotCheck.this.getSharedPreferences(AppConstants.sharedPref_HotSpotEmail, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("date", AppConstants.currentDateFormat("yyyy-MM-dd"));
                            editor.commit();

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

        });
    }


    public boolean CheckAllHosesAreFree() {
        if (WelcomeActivity.OnWelcomeActivity && Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE") && Constants.FS_5STATUS.equalsIgnoreCase("FREE") && Constants.FS_6STATUS.equalsIgnoreCase("FREE")) {
            return true;
        } else {
            return false;
        }
    }

    public void SyncSqliteData() {

        if (CheckAllHosesAreFree()) {

            if (cd.isConnecting()) {

                try {
                    //sync offline transactions
                    String off_json = offcontroller.getAllOfflineTransactionJSON(BackgroundServiceHotspotCheck.this);
                    JSONObject jobj = new JSONObject(off_json);
                    String offtransactionArray = jobj.getString("TransactionsModelsObj");
                    JSONArray jarrsy = new JSONArray(offtransactionArray);

                    if (jarrsy.length() > 0 && OfflineConstants.isOfflineAccess(BackgroundServiceHotspotCheck.this)) {
                        boolean BSRunning = CommonUtils.checkServiceRunning(BackgroundServiceHotspotCheck.this, AppConstants.PACKAGE_BS_OffTransSync);
                        if (!BSRunning) {
                            startService(new Intent(BackgroundServiceHotspotCheck.this, OffTranzSyncService.class));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void ChangeWifiState() {

        WifiManager wifiManager = (WifiManager) BackgroundServiceHotspotCheck.this.getSystemService(WIFI_SERVICE);
        if (wifiManager.isWifiEnabled() && Constants.hotspotstayOn) {
            //Disable wifi
            wifiManager.setWifiEnabled(false);
            Log.i(TAG,"Disabling wifi..");
        }

    }
}