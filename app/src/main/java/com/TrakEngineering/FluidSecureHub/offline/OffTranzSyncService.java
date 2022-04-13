package com.TrakEngineering.FluidSecureHub.offline;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.AppConstants;
import com.TrakEngineering.FluidSecureHub.CommonUtils;
import com.TrakEngineering.FluidSecureHub.ConnectionDetector;
import com.TrakEngineering.FluidSecureHub.Constants;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import static com.TrakEngineering.FluidSecureHub.server.ServerHandler.JSON;

public class OffTranzSyncService extends Service {

    OffDBController controller = new OffDBController(OffTranzSyncService.this);
    String TAG = "OffTranzSyncService";
    ConnectionDetector cd = new ConnectionDetector(OffTranzSyncService.this);


    public OffTranzSyncService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE")) {

            System.out.println("OffTranzSyncService started " + new Date());
            if (AppConstants.GenerateLogs)AppConstants.WriteinFile("OffTranzSyncService started " + new Date());
            //if (cd.isConnectingToInternet())
            //    new GetAPIToken().execute();

            //delete empty transactions
            controller.deleteLastTransactionIfNotEmpty();

            azureQueueLogic();
            tldQueueLogic();

        }else{
            System.out.println("OffTranzSyncService Transaction In Progress, Skip " + new Date());
            //if (AppConstants.GenerateLogs)AppConstants.WriteinFile("OffTranzSyncService Transaction In Progress, Skip" + new Date());
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void azureQueueLogic() {
        if (cd.isConnecting()) {

            //Log.e("Totaloffline_check","Offline data Sync AzureCall");
            try {
                String off_json_10_trans = controller.getTop10OfflineTransactionJSON(OffTranzSyncService.this);

                JSONObject jobj = new JSONObject(off_json_10_trans);
                String offtransactionArray = jobj.getString("TransactionsModelsObj");
                JSONArray jarrsy = new JSONArray(offtransactionArray);

                if (jarrsy.length() > 0) {
                    new AzureCall().execute(off_json_10_trans, "OFF");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "SyncOffTrnx Offline data Sync AzureCall no internert:"+cd.isConnecting());
        }
    }

    public void tldQueueLogic() {
        if (cd.isConnecting()) {


            try {
                String off_json = controller.getTLDOfflineTransactionJSON(OffTranzSyncService.this);

                if (off_json!=null && !off_json.equalsIgnoreCase("[]")) {
                    new AzureCall().execute(off_json, "TLD");
                }else {
                    System.out.println("TLD json is empty");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class AzureCall extends AsyncTask<String, Void, String> {
        String responseStr = "";
        String sentJson = "";
        String queType = "";

        @Override
        protected String doInBackground(String... param) {
            try {
                sentJson = param[0];
                queType = param[1];

                QueueBasics basicSamples = new QueueBasics();
                basicSamples.addMessageOnQueue(OffTranzSyncService.this, sentJson, queType);
                responseStr = "success";

            } catch (Exception e) {
                System.out.println("Azure--" + e.getMessage());
            }
            return responseStr;
        }

        @Override
        protected void onPostExecute(String s) {

            if (s.equalsIgnoreCase("success")) {

                if (queType.equalsIgnoreCase("TLD")) {
                    controller.deleteTableData(OffDBController.TBL_OFF_TLD);

                } else {
                    //get IDs from json
                    try {

                        JSONObject jobj = new JSONObject(sentJson);
                        String offtransactionArray = jobj.getString("TransactionsModelsObj");
                        JSONArray jarrsy = new JSONArray(offtransactionArray);

                        String idsList = "";
                        for (int i = 0; i < jarrsy.length(); i++) {
                            JSONObject jo = jarrsy.getJSONObject(i);
                            String Id = jo.getString("Id");
                            idsList += Id + ",";
                        }

                        idsList = removeLastChar(idsList);

                        System.out.println("offline Ids to delete-" + idsList);
                        controller.deleteTransactionsByIDs(idsList);


                        //delete empty transactions
                        controller.deleteLastTransactionIfNotEmpty();

                        // call again for remaining transactions
                        azureQueueLogic();

                    } catch (Exception e) {
                        System.out.println("deleteTransactionsByIDs---" + e.getMessage());
                    }
                }
            }
        }
    }

    public String removeLastChar(String str) {
        if (str != null && str.length() > 0 && str.charAt(str.length() - 1) == ',') {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }


    public class GetAPIToken extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... param) {
            String resp = "";


            try {

                String Email = CommonUtils.getCustomerDetailsCC(OffTranzSyncService.this).PersonEmail;

                String formData = "username=" + Email + "&" +
                        "password=FluidSecure*123&" +
                        "grant_type=password&" +
                        "FromApp=y";


                OkHttpClient client = new OkHttpClient();


                RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), formData);


                Request request = new Request.Builder()
                        .url(AppConstants.API_URL_TOKEN)
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

                //------------------------------

            } catch (Exception e) {

                System.out.println("Ex" + e.getMessage());

            }


            return resp;
        }


        @Override
        protected void onPostExecute(String result) {

            System.out.println("result:" + result);

            if (result != null && !result.isEmpty()) {


                try {

                    JSONObject jsonObject = new JSONObject(result);

                    String access_token = jsonObject.getString("access_token");
                    String token_type = jsonObject.getString("token_type");
                    String expires_in = jsonObject.getString("expires_in");
                    String refresh_token = jsonObject.getString("refresh_token");

                    System.out.println("access_token:" + access_token);

                    controller.storeOfflineToken(OffTranzSyncService.this, access_token, token_type, expires_in, refresh_token);


                    if (cd.isConnecting()) {

                        String off_json = controller.getAllOfflineTransactionJSON(OffTranzSyncService.this);
                        JSONObject jobj = new JSONObject(off_json);
                        String offtransactionArray = jobj.getString("TransactionsModelsObj");
                        JSONArray jarrsy = new JSONArray(offtransactionArray);

                        if (jarrsy.length() > 0) {
                            //offline transaction upload
                            //new SendOfflineTransactions().execute(off_json);
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


        }


    }


    public class SendOfflineTransactions extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... param) {
            String resp = "";


            try {

                System.out.println("Offline off_json data:" + param[0]);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile("Offline Transactioms: " + param[0]);
                String api_token = controller.getOfflineToken(OffTranzSyncService.this);

                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(JSON, param[0]);
                Request request = new Request.Builder()
                        .url(AppConstants.API_URL_SYNC_TRANS)
                        .addHeader("Authorization", "bearer " + api_token)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

                //------------------------------

            } catch (Exception e) {

                System.out.println("SendOfflineTransactions" + e.getMessage());
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile("SendOfflineTransactions: " + e.getMessage());

            }


            return resp;
        }


        @Override
        protected void onPostExecute(String result) {


            System.out.println(" Offline data sync result:" + result);

            if (AppConstants.GenerateLogs) AppConstants.WriteinFile("Offline data sync-" + result);

            if (result != null && !result.isEmpty()) {


                try {

                    JSONObject jsonObject = new JSONObject(result);

                    String ResponceMessage = jsonObject.getString("ResponceMessage");
                    String ResponceText = jsonObject.getString("ResponceText");

                    if (ResponceText.equalsIgnoreCase("success")) {

                        String off_json = controller.getAllOfflineTransactionJSON(OffTranzSyncService.this);
                        System.out.println("OFFline json synced");
                        controller.deleteTransactionIfNotEmpty();

                        //eXCEPT RECENT 8 EMPTY TRANSACTION DELET REMANING EMPTY TRANSACTION
                        controller.deleteLastTransactionIfNotEmpty();

                    } else
                        System.out.println("OFFline json synced FAILEDDDD");


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


        }


    }
}
