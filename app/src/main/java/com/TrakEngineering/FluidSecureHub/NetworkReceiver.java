package com.TrakEngineering.FluidSecureHub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

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

            if (Constants.FS_1_STATUS.equalsIgnoreCase("FREE") && Constants.FS_2_STATUS.equalsIgnoreCase("FREE") && Constants.FS_3_STATUS.equalsIgnoreCase("FREE") && Constants.FS_4_STATUS.equalsIgnoreCase("FREE") && Constants.FS_5_STATUS.equalsIgnoreCase("FREE") && Constants.FS_6_STATUS.equalsIgnoreCase("FREE")) {

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

        boolean PreviousState = AppConstants.PRE_STATE_MOBILE_DATA;

        if (PreviousState == CurrentState) {
            //NoSwitch
            System.out.println("Network not switched");
        } else {
            //NetworkSwitched
            String MobileDataStatus = "";
            if (AppConstants.IS_MOBILE_ON) {
                AppConstants.NETWORK_STRENGTH = true;
                MobileDataStatus = "ON";
            } else {
                MobileDataStatus = "OFF";
            }
            AppConstants.PRE_STATE_MOBILE_DATA = CurrentState;
            Log.i(TAG, "Network Switched:" + AppConstants.IS_MOBILE_ON + " CURRENT_NETWORK_TYPE: " + Constants.CURRENT_NETWORK_TYPE + "~~~" + Constants.CURRENT_SIGNAL_STRENGTH);
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Status >> Mobile_Data:" + MobileDataStatus + "; CURRENT_NETWORK_TYPE: " + Constants.CURRENT_NETWORK_TYPE + "~~~" + Constants.CURRENT_SIGNAL_STRENGTH);
            //AppConstants.colorToastBigFont(context, "Network Switched", Color.BLUE);
            //context.startService(new Intent(context, StopRunningTransactionBackgroundService.class));
        }

    }

    private void UpdateMacToServer(Context context) {

        SharedPreferences sharedPref = context.getSharedPreferences(Constants.MAC_ADDRESS_RECONFIGURE, Context.MODE_PRIVATE);
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
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(ctx) + ":" + userEmail + ":" + "UpdateMACAddress" + AppConstants.LANG_PARAM);
                response = serverHandler.PostTextData(ctx, AppConstants.WEB_URL, jsonData, authString);
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
                        AppConstants.clearSharedPrefByName(ctx, Constants.MAC_ADDRESS_RECONFIGURE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

