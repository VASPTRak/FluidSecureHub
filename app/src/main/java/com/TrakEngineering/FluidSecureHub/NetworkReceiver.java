package com.TrakEngineering.FluidSecureHub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.offline.OffTranzSyncService;
import com.TrakEngineering.FluidSecureHub.server.ServerHandler;

import org.json.JSONObject;

public class NetworkReceiver extends BroadcastReceiver {

    Context ctx;

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        ctx = context;
        boolean CurrentState;
        final String TAG = AppConstants.LOG_BACKGROUND + "-" + "NetworkReceiver ";

        NetworkInfo activeInfo = conn.getActiveNetworkInfo();
        boolean wifiConnected;
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            AppConstants.IS_MOBILE_ON = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            CurrentState = true;
            AppConstants.IS_MOBILE_MSG = false;

            //AppConstants.colorToastBigFont(context, "Online..!!", Color.BLUE);
            UpdateMacToServer(context);

            if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE") && Constants.FS_5STATUS.equalsIgnoreCase("FREE") && Constants.FS_6STATUS.equalsIgnoreCase("FREE")) {

                //sync offline transactions
                //context.startService(new Intent(context, OffTranzSyncService.class));
                //sync online transactions
                //context.startService(new Intent(context, BackgroundService.class));
            }


        } else {
            wifiConnected = false;
            AppConstants.IS_MOBILE_ON = false;
            CurrentState = false;
            if (AppConstants.IS_MOBILE_MSG) {

            } else {
                //AppConstants.colorToastBigFont(context, "Switching to OFFLINE mode", Color.BLUE);
                AppConstants.IS_MOBILE_MSG = true;
            }
        }

        boolean PreviousState = AppConstants.PRE_STATE_MOBILEDATA;

        if (PreviousState == CurrentState) {
            //NoSwitch
            System.out.println("Network not switched");
        } else {
            //NetworkSwitched
            if (AppConstants.IS_MOBILE_ON) {
                AppConstants.NETWORK_STRENGTH = true;
            }
            AppConstants.PRE_STATE_MOBILEDATA = CurrentState;
            Log.i(TAG, "Network Switched:" + AppConstants.IS_MOBILE_ON + " CurrentNetworkType: " + Constants.CurrentNetworkType + "~~~" + Constants.CurrentSignalStrength);
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Network Switched:" + AppConstants.IS_MOBILE_ON + " CurrentNetworkType: " + Constants.CurrentNetworkType + "~~~" + Constants.CurrentSignalStrength);
            //AppConstants.colorToastBigFont(context, "Network Switched", Color.RED);
            //context.startService(new Intent(context, StopRunningTransactionBackgroundService.class));
        }

    }

    private void UpdateMacToServer(Context context) {

        SharedPreferences sharedPref = context.getSharedPreferences(Constants.MAC_ADDR_RECONFIGURE, Context.MODE_PRIVATE);
        String jsonData = sharedPref.getString("jsonData", "");
        if (!jsonData.isEmpty())
            new UpdateMacAsynTask().execute(jsonData);

    }

    public class UpdateMacAsynTask extends AsyncTask<String, Void, String> {

        public String response = null;

        @Override
        protected String doInBackground(String... param) {

            try {
                ServerHandler serverHandler = new ServerHandler();
                String jsonData = param[0];
                String userEmail = CommonUtils.getCustomerDetailsCC(ctx).PersonEmail;

                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(ctx) + ":" + userEmail + ":" + "UpdateMACAddress");
                response = serverHandler.PostTextData(ctx, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String serverRes) {

            try {
                if (serverRes.equalsIgnoreCase("err")) {
                    // AppConstants.alertBigFinishActivity(WelcomeActivity.this, "Link Re-configuration is partially completed. \nPlease remove app from Recent Apps and start app again");
                } else if (serverRes != null) {

                    JSONObject jsonObject1 = new JSONObject(serverRes);
                    String ResponceMessage = jsonObject1.getString("ResponceMessage");

                    if (ResponceMessage.equalsIgnoreCase("success")) {
                        AppConstants.clearSharedPrefByName(ctx, Constants.MAC_ADDR_RECONFIGURE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

