package com.TrakEngineering.FluidSecureHub.offline;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.AppConstants;
import com.TrakEngineering.FluidSecureHub.Constants;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;



public class StopRunningTransactionBackgroundService extends Service {

    public StopRunningTransactionBackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        System.out.println("StopRunningTransactionBackgroundService started ");
        StopRunningTransaction();


        return super.onStartCommand(intent, flags, startId);
    }

    public void StopRunningTransaction() {


        if (Constants.FS_1_STATUS.equalsIgnoreCase("BUSY") && Constants.FS_2_STATUS.equalsIgnoreCase("BUSY") && Constants.FS_3_STATUS.equalsIgnoreCase("BUSY") && Constants.FS_4_STATUS.equalsIgnoreCase("BUSY")) {
            //Stop All 4 transaction
            StopTxn(0);
            StopTxn(1);
            StopTxn(2);
            StopTxn(3);
        } else if (Constants.FS_1_STATUS.equalsIgnoreCase("BUSY") && Constants.FS_2_STATUS.equalsIgnoreCase("BUSY") && Constants.FS_3_STATUS.equalsIgnoreCase("BUSY")) {
            //Stop 1,2,3
            StopTxn(0);
            StopTxn(1);
            StopTxn(2);
        } else if (Constants.FS_1_STATUS.equalsIgnoreCase("BUSY") && Constants.FS_2_STATUS.equalsIgnoreCase("BUSY")) {
            //Stop 1,2
            StopTxn(0);
            StopTxn(1);

        } else if (Constants.FS_1_STATUS.equalsIgnoreCase("BUSY")) {
            //Stop only 1
            StopTxn(0);
        } else if (Constants.FS_2_STATUS.equalsIgnoreCase("BUSY")) {
            //Stop only 2
            StopTxn(1);
        } else if (Constants.FS_3_STATUS.equalsIgnoreCase("BUSY")) {
            //Stop only 3
            StopTxn(2);
        } else if (Constants.FS_4_STATUS.equalsIgnoreCase("BUSY")) {
            //Stop only 4
            StopTxn(3);
        }

    }

    public void StopTxn(int p){

        try {

            for (int i = 0; i < AppConstants.DETAILS_LIST_OF_CONNECTED_DEVICES.size(); i++) {

                String Mac_Address = AppConstants.DETAILS_LIST_OF_CONNECTED_DEVICES.get(i).get("macAddress");
                String IpAddress = AppConstants.DETAILS_LIST_OF_CONNECTED_DEVICES.get(i).get("ipAddress");

                //List of Near-by FSNP/Ble mac address list
                if (AppConstants.DETAILS_SERVER_SSID_LIST != null && !AppConstants.DETAILS_SERVER_SSID_LIST.isEmpty()) {

                    String MacAddress = AppConstants.DETAILS_SERVER_SSID_LIST.get(p).get("MacAddress");
                    String fsnpAddress = AppConstants.DETAILS_SERVER_SSID_LIST.get(p).get("FSNPMacAddress");
                    String fsnpName = AppConstants.DETAILS_SERVER_SSID_LIST.get(p).get("FSAntenna2");

                    if (MacAddress.equalsIgnoreCase(Mac_Address)) {
                        String HTTP_URL = "http://" + IpAddress + ":80/";
                        String jsonRelayOff = "{\"relay_request\":{\"Password\":\"12345678\",\"Status\":0}}";
                        String URL_RELAY_FS1 = HTTP_URL + "config?command=relay";
                        new CommandsPOST().execute(URL_RELAY_FS1, jsonRelayOff);
                        break;

                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile( "  StopTxn" + e);
        }

    }

    public class CommandsPOST extends AsyncTask<String, Void, String> {

        public String resp = "";


        protected String doInBackground(String... param) {

            System.out.println("url" + param[0]);
            try {


                MediaType JSON = MediaType.parse("application/json");

                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(JSON, param[1]);

                Request request = new Request.Builder()
                        .url(param[0])
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {


            try {
                System.out.println(result);

            } catch (Exception e) {

                System.out.println(e);
            }

        }
    }

}