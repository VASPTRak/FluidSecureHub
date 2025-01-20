package com.TrakEngineering.FluidSecureHub;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.entity.TransactionStatus;
import com.TrakEngineering.FluidSecureHub.retrofit.BusProvider;
import com.TrakEngineering.FluidSecureHub.retrofit.ErrorEvent;
import com.TrakEngineering.FluidSecureHub.retrofit.Interface;
import com.TrakEngineering.FluidSecureHub.retrofit.ServerEvent;
import com.TrakEngineering.FluidSecureHub.retrofit.ServerResponse;
import com.TrakEngineering.FluidSecureHub.server.ServerHandler;
import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by Administrator on 2/3/2017.
 */

public class BackgroundService extends Service {

    ServerHandler serverHandler = new ServerHandler();
    DBController controller = new DBController(BackgroundService.this);
    public static String TAG = AppConstants.LOG_BACKGROUND + "-" + "BackgroundService ";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "BackgroundService is on....");
        /*if (AppConstants.GENERATE_LOGS)
            AppConstants.writeInFile(TAG + "Started.");*/
        //Log.e("Totaloffline_check","Online data BackgroundService");
        //If all hoses are free cleare
        if (Constants.FS_1_STATUS.equalsIgnoreCase("FREE") && Constants.FS_2_STATUS.equalsIgnoreCase("FREE") && Constants.FS_3_STATUS.equalsIgnoreCase("FREE") && Constants.FS_4_STATUS.equalsIgnoreCase("FREE") && Constants.FS_5_STATUS.equalsIgnoreCase("FREE") && Constants.FS_6_STATUS.equalsIgnoreCase("FREE")) {
            AppConstants.LIST_OF_RUNNING_TRANSACTIONS.clear();
        }

        //---UpdateTransaction Status to server------
        ArrayList<HashMap<String, String>> stsData = controller.getAllTransStatus();

        if (stsData != null && stsData.size() > 0) {
            for (int i = 0; i < stsData.size(); i++) {
                String Id = stsData.get(i).get("Id");
                String transId = stsData.get(i).get("transId");
                String transStatus = stsData.get(i).get("transStatus");
                System.out.println("resp...Transstatus transId:" + transId + " :"+transStatus);
                /*if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "Update Transaction Status. TransactionId: " + transId + "; Status: " + transStatus);*/
                if (transId != null && !transId.isEmpty()) {
                    new SetTransactionStatus().execute(Id, transId, transStatus);
                }
            }

        }
        //-------------------end------------

        ArrayList<HashMap<String, String>> uData = controller.getAllTransaction();

        if (uData != null && uData.size() > 0) {
            for (int i = 0; i < uData.size(); i++) {
                String Id = uData.get(i).get("Id");
                String jsonData = uData.get(i).get("jsonData");
                String authString = uData.get(i).get("authString");

                try {
                    //Log.i(TAG, "Transaction UploadTask Id:" + Id + " jsonData:" + jsonData + " authString:" + authString);
                    //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile(TAG + "  Transaction UploadTask Id:" + Id + " jsonData:" + jsonData + " authString:" + authString);

                    if (jsonData != null) {
                        if (!jsonData.isEmpty()) {
                            JSONObject jsonObject = new JSONObject(jsonData);
                            String txnId = String.valueOf(jsonObject.get("TransactionId"));
                            String pulses = String.valueOf(jsonObject.get("Pulses"));
                            if (AppConstants.LIST_OF_RUNNING_TRANSACTIONS.contains(txnId)) {
                                //transaction running
                                Log.i(TAG, "Transaction is in progress" + txnId);
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + "Transaction is in progress. TransactionId: " + txnId);
                            } else {
                                //transaction completed
                                //new UploadTask().execute(Id, jsonData, authString);
                                if (!AppConstants.List_Of_Uploading_Transactions.contains(txnId)) {
                                    AppConstants.List_Of_Uploading_Transactions.add(txnId);
                                    UploadTaskRetroFit(Id, jsonData, authString, txnId, pulses);
                                }
                                //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile(TAG + "TempLog  Transaction UploadTask Id:" + Id + " jsonData:" + jsonData);
                            }
                        } else {
                            controller.deleteTransactions(Id);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + "<Transaction deleted from local DB. (Empty json): Id: " + Id + ">");
                        }
                    }
                } catch (JSONException e) {
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + "Transaction UploadTask--JSONException " + e);
                } catch (Exception ex) {
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + "Transaction UploadTask--Exception " + ex);
                }

            }

        }

        ReplaceHoseNameFlagSynToServer();

        updateSwitchTimeBounceForLink();

        UpdatePulserTypeOfLINKMaster();

        uploadLast20TransactionOnce(); // last 20 trxn

        uploadConnectionissueLogtoserver();// upload connection issue log.

        updateCheckMOStatusFlagOfLink();

        updateResetMOCheckFlagOfLink();

        updateManualOverrideStatusOfLink();

        //Clear shared preferance regarding Last 20 trxn
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AppConstants.clearSharedPrefByName(BackgroundService.this,"storeCmtxtnid_20_record");
                AppConstants.clearSharedPrefByName(BackgroundService.this,AppConstants.PREF_LINK_CONNECTION_ISSUE);
                AppConstants.List_Of_Uploading_Transactions.clear();
                stopSelf();
            }
        },10000);

        return super.onStartCommand(intent, flags, startId);
    }

    private void updateSwitchTimeBounceForLink() {
        try {
            //For Hose One......1
            SharedPreferences FS1Pref = this.getSharedPreferences("storeSwitchTimeBounceFlag1", 0);
            String jsonData1 = FS1Pref.getString("jsonData", "");
            String authString1 = FS1Pref.getString("authString", "");

            if (!jsonData1.trim().isEmpty() && !authString1.trim().isEmpty()) {
                new SetSwitchTimeBounceFlag().execute(jsonData1, authString1, "storeSwitchTimeBounceFlag1");
            }

            //For Hose Two....2
            SharedPreferences FS2Pref = this.getSharedPreferences("storeSwitchTimeBounceFlag2", 0);
            String jsonData2 = FS2Pref.getString("jsonData", "");
            String authString2 = FS2Pref.getString("authString", "");

            if (!jsonData2.trim().isEmpty() && !authString2.trim().isEmpty()) {
                new SetSwitchTimeBounceFlag().execute(jsonData2, authString2, "storeSwitchTimeBounceFlag2");
            }

            //For Hose Three..3
            SharedPreferences FS3Pref = this.getSharedPreferences("storeSwitchTimeBounceFlag3", 0);
            String jsonData3 = FS3Pref.getString("jsonData", "");
            String authString3 = FS3Pref.getString("authString", "");

            if (!jsonData3.trim().isEmpty() && !authString3.trim().isEmpty()) {
                new SetSwitchTimeBounceFlag().execute(jsonData3, authString3, "storeSwitchTimeBounceFlag3");
            }

            //For Hose 4
            SharedPreferences FS4Pref = this.getSharedPreferences("storeSwitchTimeBounceFlag4", 0);
            String jsonData4 = FS4Pref.getString("jsonData", "");
            String authString4 = FS4Pref.getString("authString", "");

            if (!jsonData4.trim().isEmpty() && !authString4.trim().isEmpty()) {
                new SetSwitchTimeBounceFlag().execute(jsonData4, authString4, "storeSwitchTimeBounceFlag4");
            }

            //For Hose 5
            SharedPreferences FS5Pref = this.getSharedPreferences("storeSwitchTimeBounceFlag5", 0);
            String jsonData5 = FS5Pref.getString("jsonData", "");
            String authString5 = FS5Pref.getString("authString", "");

            if (!jsonData5.trim().isEmpty() && !authString5.trim().isEmpty()) {
                new SetSwitchTimeBounceFlag().execute(jsonData5, authString5, "storeSwitchTimeBounceFlag5");
            }

            //For Hose 6
            SharedPreferences FS6Pref = this.getSharedPreferences("storeSwitchTimeBounceFlag6", 0);
            String jsonData6 = FS6Pref.getString("jsonData", "");
            String authString6 = FS6Pref.getString("authString", "");

            if (!jsonData6.trim().isEmpty() && !authString6.trim().isEmpty()) {
                new SetSwitchTimeBounceFlag().execute(jsonData6, authString6, "storeSwitchTimeBounceFlag6");
            }
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "updateSwitchTimeBounceForLink Exception: " + e.getMessage());
        }
    }

    public class SetSwitchTimeBounceFlag extends AsyncTask<String, Void, String> {

        String PrefName = "";
        protected String doInBackground(String... param) {
            String resp = "";
            PrefName = param[2];

            try {
                OkHttpClient client = new OkHttpClient();
                MediaType TEXT = MediaType.parse("application/text;charset=UTF-8");

                RequestBody body = RequestBody.create(TEXT, param[0]);
                Request request = new Request.Builder()
                        .url(AppConstants.WEB_URL)
                        .post(body)
                        .addHeader("Authorization", param[1])
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

                if (result.contains("success") && !PrefName.isEmpty()) {
                    SharedPreferences preferences = getSharedPreferences(PrefName, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.commit();

                }
            } catch (Exception e) {
                System.out.println("onPostExecute" + e);
            }
        }
    }

    private void UpdatePulserTypeOfLINKMaster() {
        try {
            //For Hose One......1
            SharedPreferences FS1Pref = this.getSharedPreferences("UpdatePulserType1", 0);
            String jsonData1 = FS1Pref.getString("jsonData", "");
            String authString1 = FS1Pref.getString("authString", "");

            if (!jsonData1.trim().isEmpty() && !authString1.trim().isEmpty()) {
                new UpdatePulserTypeOfLINK().execute(jsonData1, authString1, "UpdatePulserType1");
            }

            //For Hose Two......2
            SharedPreferences FS2Pref = this.getSharedPreferences("UpdatePulserType2", 0);
            String jsonData2 = FS2Pref.getString("jsonData", "");
            String authString2 = FS2Pref.getString("authString", "");

            if (!jsonData2.trim().isEmpty() && !authString2.trim().isEmpty()) {
                new UpdatePulserTypeOfLINK().execute(jsonData2, authString2, "UpdatePulserType2");
            }

            //For Hose Three......3
            SharedPreferences FS3Pref = this.getSharedPreferences("UpdatePulserType3", 0);
            String jsonData3 = FS3Pref.getString("jsonData", "");
            String authString3 = FS3Pref.getString("authString", "");

            if (!jsonData3.trim().isEmpty() && !authString3.trim().isEmpty()) {
                new UpdatePulserTypeOfLINK().execute(jsonData3, authString3, "UpdatePulserType3");
            }

            //For Hose Four......4
            SharedPreferences FS4Pref = this.getSharedPreferences("UpdatePulserType4", 0);
            String jsonData4 = FS4Pref.getString("jsonData", "");
            String authString4 = FS4Pref.getString("authString", "");

            if (!jsonData4.trim().isEmpty() && !authString4.trim().isEmpty()) {
                new UpdatePulserTypeOfLINK().execute(jsonData4, authString4, "UpdatePulserType4");
            }

            //For Hose Five......5
            SharedPreferences FS5Pref = this.getSharedPreferences("UpdatePulserType5", 0);
            String jsonData5 = FS5Pref.getString("jsonData", "");
            String authString5 = FS5Pref.getString("authString", "");

            if (!jsonData5.trim().isEmpty() && !authString5.trim().isEmpty()) {
                new UpdatePulserTypeOfLINK().execute(jsonData5, authString5, "UpdatePulserType5");
            }

            //For Hose Six......6
            SharedPreferences FS6Pref = this.getSharedPreferences("UpdatePulserType6", 0);
            String jsonData6 = FS6Pref.getString("jsonData", "");
            String authString6 = FS6Pref.getString("authString", "");

            if (!jsonData6.trim().isEmpty() && !authString6.trim().isEmpty()) {
                new UpdatePulserTypeOfLINK().execute(jsonData6, authString6, "UpdatePulserType6");
            }
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "UpdatePulserTypeOfLINK Exception: " + e.getMessage());
        }
    }

    public class UpdatePulserTypeOfLINK extends AsyncTask<String, Void, String> {

        String PrefName = "";
        protected String doInBackground(String... param) {
            String resp = "";
            PrefName = param[2];

            try {
                OkHttpClient client = new OkHttpClient();
                MediaType TEXT = MediaType.parse("application/text;charset=UTF-8");

                RequestBody body = RequestBody.create(TEXT, param[0]);
                Request request = new Request.Builder()
                        .url(AppConstants.WEB_URL)
                        .post(body)
                        .addHeader("Authorization", param[1])
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
                if (result.contains("success") && !PrefName.isEmpty()) {
                    SharedPreferences preferences = getSharedPreferences(PrefName, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.commit();
                }
            } catch (Exception e) {
                System.out.println("onPostExecute" + e);
            }
        }
    }

    void ReplaceHoseNameFlagSynToServer() {
        try {
            //For Hose One......1
            SharedPreferences FS1Pref = this.getSharedPreferences("storeIsRenameFlagFS1", 0);
            boolean pflag1 = FS1Pref.getBoolean("flag", false);
            String jsonData1 = FS1Pref.getString("jsonData", "");
            String authString1 = FS1Pref.getString("authString", "");

            if (pflag1 && !jsonData1.trim().isEmpty() && !authString1.trim().isEmpty()) {
                Log.i("SharedPreferences_check1 ", pflag1 + ">>" + jsonData1);
                new SetHoseNameReplacedFlag().execute(jsonData1, authString1, "storeIsRenameFlagFS1");
            }

            //For Hose Two....2
            SharedPreferences FS2Pref = this.getSharedPreferences("storeIsRenameFlagFS2", 0);
            boolean pflag2 = FS2Pref.getBoolean("flag", false);
            String jsonData2 = FS2Pref.getString("jsonData", "");
            String authString2 = FS2Pref.getString("authString", "");

            if (pflag2 && !jsonData2.trim().isEmpty() && !authString2.trim().isEmpty()) {
                Log.i("SharedPreferences_check2 ", pflag2 + ">>" + jsonData2);
                new SetHoseNameReplacedFlag().execute(jsonData2, authString2, "storeIsRenameFlagFS2");
            }

            //For Hose Three..3
            SharedPreferences FS3Pref = this.getSharedPreferences("storeIsRenameFlagFS3", 0);
            boolean pflag3 = FS3Pref.getBoolean("flag", false);
            String jsonData3 = FS3Pref.getString("jsonData", "");
            String authString3 = FS3Pref.getString("authString", "");

            if (pflag3 && !jsonData3.trim().isEmpty() && !authString3.trim().isEmpty()) {
                Log.i("SharedPreferences_check3 ", pflag3 + ">>" + jsonData3);
                new SetHoseNameReplacedFlag().execute(jsonData3, authString3, "storeIsRenameFlagFS3");
            }


            //For Hose 4
            SharedPreferences FS4Pref = this.getSharedPreferences("storeIsRenameFlagFS4", 0);
            boolean pflag4 = FS4Pref.getBoolean("flag", false);
            String jsonData4 = FS4Pref.getString("jsonData", "");
            String authString4 = FS4Pref.getString("authString", "");

            if (pflag4 && !jsonData4.trim().isEmpty() && !authString4.trim().isEmpty()) {
                Log.i("SharedPreferences_check4 ", pflag4 + ">>" + jsonData4);
                new SetHoseNameReplacedFlag().execute(jsonData4, authString4, "storeIsRenameFlagFS4");
            }


            //For Hose 5
            SharedPreferences FS5Pref = this.getSharedPreferences("storeIsRenameFlagFS5", 0);
            boolean pflag5 = FS5Pref.getBoolean("flag", false);
            String jsonData5 = FS5Pref.getString("jsonData", "");
            String authString5 = FS5Pref.getString("authString", "");

            if (pflag5 && !jsonData5.trim().isEmpty() && !authString5.trim().isEmpty()) {
                new SetHoseNameReplacedFlag().execute(jsonData5, authString5, "storeIsRenameFlagFS5");
            }

            //For Hose 6
            SharedPreferences FS6Pref = this.getSharedPreferences("storeIsRenameFlagFS6", 0);
            boolean pflag6 = FS6Pref.getBoolean("flag", false);
            String jsonData6 = FS6Pref.getString("jsonData", "");
            String authString6 = FS6Pref.getString("authString", "");

            if (pflag6 && !jsonData6.trim().isEmpty() && !authString6.trim().isEmpty()) {
                new SetHoseNameReplacedFlag().execute(jsonData6, authString6, "storeIsRenameFlagFS6");
            }
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "ReplaceHoseNameFlagSynToServer Exception: " + e.getMessage());
        }
    }

    public class SetHoseNameReplacedFlag extends AsyncTask<String, Void, String> {

        String PrefName = "";
        protected String doInBackground(String... param) {
            String resp = "";
            PrefName = param[2];

            try {
                OkHttpClient client = new OkHttpClient();
                MediaType TEXT = MediaType.parse("application/text;charset=UTF-8");

                RequestBody body = RequestBody.create(TEXT, param[0]);
                Request request = new Request.Builder()
                        .url(AppConstants.WEB_URL)
                        .post(body)
                        .addHeader("Authorization", param[1])
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

                System.out.println("Wifi renamed on server---" + result);
                Log.i("SharedPreferences_check R ",PrefName+">>"+result);
                if (result.contains("success") && !PrefName.isEmpty()) {
                    SharedPreferences preferences = getSharedPreferences(PrefName, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.commit();

                    System.out.println("SharedPreferences clear---" + result);

                }


            } catch (Exception e) {
                System.out.println("eeee" + e);
            }
        }
    }

    private void uploadLast20TransactionOnce() {
        SharedPreferences sharedPref = BackgroundService.this.getSharedPreferences("storeCmtxtnid_20_record", Context.MODE_PRIVATE);
        for (int i = 1; i < 7; i++) {
            String linkJsonData = sharedPref.getString("LINK" + i, "");
            if (!linkJsonData.equals("")) {
                SaveMultipleTransactionsRetroFit(linkJsonData);
            }
        }
    }

    private void uploadConnectionissueLogtoserver(){

        SharedPreferences sharedPref = BackgroundService.this.getSharedPreferences(AppConstants.PREF_LINK_CONNECTION_ISSUE, Context.MODE_PRIVATE);
        for (int i = 1; i < 7 ; i++) {
            String linkJsonData = sharedPref.getString("NLINK"+i, "");
            if (!linkJsonData.equals("")){
                Log.i(TAG," uploadConnectionissueLogtoserver: "+linkJsonData);
                new LINKDisconnectionErrorLog().execute(linkJsonData);
            }
        }

    }

    public class EntityCmd10Txn {
        ArrayList cmtxtnid_10_record;
    }

    public void UploadTaskRetroFit(String Id, String jsonData, String authString, String transactionId, String pulses) {

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        okhttp3.OkHttpClient.Builder httpClient = new okhttp3.OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .baseUrl(AppConstants.SERVER_BASE_URL)
                .build();
        Interface service = retrofit.create(Interface.class);


        Call<ServerResponse> call = service.postttt(authString, jsonData);

        call.enqueue(new Callback<ServerResponse>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                BusProvider.getInstance().post(new ServerEvent(response.body()));

                String ResponceMessage = response.body().getResponceMessage();
                String ResponceText = response.body().getResponceText();

                //System.out.println("resp..." + response.body().toString());
                Log.i(TAG, "UploadTaskRetroFit ResponceMessage:" + ResponceMessage + " ResponceText:" + ResponceText);
                String respText = "";
                if (ResponceMessage.equalsIgnoreCase("fail")) {
                    respText = "; ResponseText: " + ResponceText;
                }
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "Transaction UploadTask. TransactionId: " + transactionId + "; Pulses: " + pulses + "; ResponseMessage: " + ResponceMessage + respText);
                RemoveTxnFromListOfUploadingTransactions(transactionId);
                try {
                    if (ResponceMessage.equalsIgnoreCase("success") || ResponceMessage.equalsIgnoreCase("fail")) {

                        if (ResponceMessage.equalsIgnoreCase("success")) {

                            String Notify = jsonData;
                            if (Notify.contains("IsFuelingStop\":\"1")) {
                                //Notify only when IsFuelingStop = 1
                                //AppConstants.notificationAlert(BackgroundService.this);
                            } else {
                                //Skip notification
                            }

                            controller.deleteTransactions(Id);

                            System.out.println("deleteTransactions..." + Id);

                        } else if (ResponceMessage.equalsIgnoreCase("fail") && ResponceText.equalsIgnoreCase("TransactionId not found.")) {
                            //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile(TAG + " TransactionId not found. deleted from Sqlite -json:"+jsonData);
                            controller.deleteTransactions(Id);
                        }
                    }

                    ArrayList<HashMap<String, String>> uData = controller.getAllTransaction();

                    if (uData != null && uData.size() == 0) {
                        stopSelf();
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + "UploadTaskRetroFit onResponse. TransactionId: " + transactionId + "; Exception: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Log.i(TAG, "Something went wrong in UploadTaskRetroFit call No internet connectivity or server connection fail.");
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "UploadTaskRetroFit onFailure. TransactionId: " + transactionId + "; Exception: " + t.getMessage());
                RemoveTxnFromListOfUploadingTransactions(transactionId);
                // handle execution failures like no internet connectivity
                BusProvider.getInstance().post(new ErrorEvent(-2, t.getMessage()));
            }
        });
    }

    public void RemoveTxnFromListOfUploadingTransactions(String transactionId) {
        if (AppConstants.List_Of_Uploading_Transactions != null && AppConstants.List_Of_Uploading_Transactions.contains(transactionId)) {
            AppConstants.List_Of_Uploading_Transactions.remove(transactionId);
        }
    }

    public class LINKDisconnectionErrorLog extends AsyncTask<String, Void, String> {


        public String response = null;

        @Override
        protected String doInBackground(String... param) {

            try {
                ServerHandler serverHandler = new ServerHandler();
                String jsonData = param[0];
                String userEmail = CommonUtils.getCustomerDetailsCC(BackgroundService.this).PersonEmail;

                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService.this) + ":" + userEmail + ":" + "LINKDisconnectionErrorLog" + AppConstants.LANG_PARAM);
                response = serverHandler.PostTextData(BackgroundService.this, AppConstants.WEB_URL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String serverRes) {

            try {
                if (serverRes != null) {

                    JSONObject jsonObject1 = new JSONObject(serverRes);

                    String ResponseMessage = jsonObject1.getString("ResponseMessage");


                    if (ResponseMessage.equalsIgnoreCase("success")) {

                        //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile(TAG + "success LINKDisconnectionErrorLog");

                    } else if (ResponseMessage.equalsIgnoreCase("fail")) {

                        //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile(TAG + " Fail LINKDisconnectionErrorLog");

                    }

                }

            } catch (Exception e) {

                if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile(TAG + "  LINKDisconnectionErrorLog onPostExecute--Exception " + e);

            }
        }
    }

    public void SaveMultipleTransactionsRetroFit(String linkJsonData) {

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        okhttp3.OkHttpClient.Builder httpClient = new okhttp3.OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .baseUrl(AppConstants.SERVER_BASE_URL)
                .build();
        Interface service = retrofit.create(Interface.class);

        SharedPreferences sharedPref = BackgroundService.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String userEmail = sharedPref.getString(AppConstants.USER_EMAIL, "");
        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService.this) + ":" + userEmail + ":" + "SaveMultipleTransactions" + AppConstants.LANG_PARAM);

        Call<ServerResponse> call = service.postttt(authString, linkJsonData);

        call.enqueue(new Callback<ServerResponse>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                BusProvider.getInstance().post(new ServerEvent(response.body()));

                String ResponceMessage = response.body().getResponceMessage();
                String ResponceText = response.body().getResponceText();

                Log.i(TAG, "SaveMultipleTransactionsRetroFit ResponceMessage:"+ResponceMessage+ " ResponceText:"+ResponceText);

                if (ResponceMessage.equalsIgnoreCase("success")) {
                    Log.i(TAG, "SaveMultipleTransactionsRetroFit ResponceMessage:"+ResponceMessage+ " ResponceText:"+ResponceText);
                }

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                // handle execution failures like no internet connectivity
                BusProvider.getInstance().post(new ErrorEvent(-2, t.getMessage()));
                Log.i(TAG, "Something went wrong in SaveMultipleTransactionsRetroFit call No internet connectivity or server connection fail.");
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " SaveMultipleTransactionsRetroFit onFailure: " + t.getMessage());
            }
        });

    }


    //----deprecated--------------
    private void uploadLast10Transaction(String shrPrefName) {
        SharedPreferences sharedPref = BackgroundService.this.getSharedPreferences(shrPrefName, Context.MODE_PRIVATE);
        String jsonData0 = "";
        if (sharedPref != null)
            jsonData0 = sharedPref.getString("json", "");

        if (jsonData0.trim().length() > 3){
            //new SaveMultipleTransactions().execute(jsonData0, shrPrefName);
        }

    }

    public class UploadTask extends AsyncTask<String, Void, String> {

        String Id;
        String jsonData;
        String authString;


        @Override
        protected String doInBackground(String... params) {

            Log.i(TAG, "UploadTask doInBackground");
            String response = "";
            try {

                Id = params[0];
                jsonData = params[1];
                authString = params[2];

                System.out.println("jsonData--" + jsonData);
                System.out.println("authString--" + authString);


                response = serverHandler.PostTextData(BackgroundService.this, AppConstants.WEB_URL, jsonData, authString);

                System.out.println("Id..." + Id);

            } catch (Exception e) {
                System.out.println(e);
            }

            return response;
        }

        @Override
        protected void onPostExecute(String resp) {

            System.out.println("resp..." + resp);
            Log.i(TAG, "UploadTask onPostExecute resp:"+resp);

            try {
                JSONObject jsonObj = new JSONObject(resp);

                String ResponceMessage = jsonObj.getString(AppConstants.RES_MESSAGE);
                String ResponceText = jsonObj.getString(AppConstants.RES_TEXT);

                if (ResponceMessage.equalsIgnoreCase("success") || ResponceMessage.equalsIgnoreCase("fail")) {

                    if (ResponceMessage.equalsIgnoreCase("success")) {

                        String Notify = jsonData;
                        if (Notify.contains("IsFuelingStop\":\"1")) {
                            //Notify only when IsFuelingStop = 1
                           // AppConstants.notificationAlert(BackgroundService.this);
                        } else {
                            //Skip notification
                        }

                        controller.deleteTransactions(Id);

                        System.out.println("deleteTransactions..." + Id);

                    }else if (ResponceMessage.equalsIgnoreCase("fail") && ResponceText.equalsIgnoreCase("TransactionId not found.")){

                        if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile(TAG + " TransactionId not found. deleted from Sqlite -json:"+jsonData);
                        controller.deleteTransactions(Id);

                    }
                }

                ArrayList<HashMap<String, String>> uData = controller.getAllTransaction();

                if (uData != null && uData.size() == 0) {
                    stopSelf();
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public class SaveMultipleTransactions extends AsyncTask<String, Void, String> {

        String shrPrefName = "";

        @Override
        protected void onPreExecute() {


        }

        @Override
        protected String doInBackground(String... param) {

            Log.i(TAG, "SaveMultipleTransactions doInBackground");
            String response = null;
            try {

                String jsonData = param[0];
                shrPrefName = param[1];

                ServerHandler serverHandler = new ServerHandler();

                SharedPreferences sharedPref = BackgroundService.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

                String userEmail = sharedPref.getString(AppConstants.USER_EMAIL, "");


                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService.this) + ":" + userEmail + ":" + "SaveMultipleTransactions" + AppConstants.LANG_PARAM);
                if (jsonData.trim().length() > 2)
                    response = serverHandler.PostTextData(BackgroundService.this, AppConstants.WEB_URL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {
                //AppConstants.writeInFile(getApplicationContext(), this.getClass().getSimpleName() + "~~~" + ex.getMessage());
                //AppConstants.WriteInDeveloperLog(getApplicationContext(), "14BackgroundService~~~" + ex.getMessage());
            }
            return response;
        }

        @Override
        protected void onPostExecute(String resp) {

            System.out.println("SaveMultipleTransactions---" + resp);
            Log.i(TAG, "SaveMultipleTransactions onPostExecute resp:"+resp);

            try {
                if (resp.contains("success")) {
                    //delete
                    SharedPreferences preferences = getSharedPreferences(shrPrefName, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.apply();
                }
            } catch (Exception e) {
                // AppConstants.writeInFile(getApplicationContext(), this.getClass().getSimpleName() + "~~~" + e.getMessage());
                // AppConstants.WriteInDeveloperLog(getApplicationContext(), "15BackgroundService~~~" + e.getMessage());
            }


        }
    }

    public class SetTransactionStatus extends AsyncTask<String, Void, String> {

        String Id;
        String transId;
        String transStatus;


        @Override
        protected String doInBackground(String... params) {

            String response = "";
            try {

                Id = params[0];
                transId = params[1];
                transStatus = params[2];

                System.out.println("transId--" + transId);
                System.out.println("transStatus--" + transStatus);

                String userEmail = CommonUtils.getCustomerDetailsbackgroundService(BackgroundService.this).PersonEmail;
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService.this) + ":" + userEmail + ":" + "UpgradeTransactionStatus" + AppConstants.LANG_PARAM);

                TransactionStatus objTS = new TransactionStatus();
                objTS.TransactionId = transId;
                objTS.Status = transStatus;
                Gson gson = new Gson();
                String jsonData = gson.toJson(objTS);
                System.out.println("TransactionStatus......" + jsonData);


                response = serverHandler.PostTextData(BackgroundService.this, AppConstants.WEB_URL, jsonData, authString);

                System.out.println("Id..." + Id);

            } catch (Exception e) {
                System.out.println(e);
            }

            return response;
        }

        @Override
        protected void onPostExecute(String resp) {

            System.out.println("resp...Transstatus" + resp);

            try {
                JSONObject jsonObj = new JSONObject(resp);

                String ResponceMessage = jsonObj.getString(AppConstants.RES_MESSAGE);
                String ResponceText = jsonObj.getString(AppConstants.RES_TEXT);


                if (ResponceMessage.equalsIgnoreCase("success")) {

                    controller.deleteTransStatus(Id);

                    System.out.println("deleteTransStatus..." + Id);

                }


            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void updateCheckMOStatusFlagOfLink() {
        try {
            //For Hose One......1
            SharedPreferences FS1Pref = this.getSharedPreferences("storeCheckMOStatusFlag1", 0);
            String jsonData1 = FS1Pref.getString("jsonData", "");
            String authString1 = FS1Pref.getString("authString", "");

            if (!jsonData1.trim().isEmpty() && !authString1.trim().isEmpty()) {
                new UpdateMOStatusDetailsOfLink().execute(jsonData1, authString1, "storeCheckMOStatusFlag1");
            }

            //For Hose Two....2
            SharedPreferences FS2Pref = this.getSharedPreferences("storeCheckMOStatusFlag2", 0);
            String jsonData2 = FS2Pref.getString("jsonData", "");
            String authString2 = FS2Pref.getString("authString", "");

            if (!jsonData2.trim().isEmpty() && !authString2.trim().isEmpty()) {
                new UpdateMOStatusDetailsOfLink().execute(jsonData2, authString2, "storeCheckMOStatusFlag2");
            }

            //For Hose Three..3
            SharedPreferences FS3Pref = this.getSharedPreferences("storeCheckMOStatusFlag3", 0);
            String jsonData3 = FS3Pref.getString("jsonData", "");
            String authString3 = FS3Pref.getString("authString", "");

            if (!jsonData3.trim().isEmpty() && !authString3.trim().isEmpty()) {
                new UpdateMOStatusDetailsOfLink().execute(jsonData3, authString3, "storeCheckMOStatusFlag3");
            }

            //For Hose 4
            SharedPreferences FS4Pref = this.getSharedPreferences("storeCheckMOStatusFlag4", 0);
            String jsonData4 = FS4Pref.getString("jsonData", "");
            String authString4 = FS4Pref.getString("authString", "");

            if (!jsonData4.trim().isEmpty() && !authString4.trim().isEmpty()) {
                new UpdateMOStatusDetailsOfLink().execute(jsonData4, authString4, "storeCheckMOStatusFlag4");
            }

            //For Hose 5
            SharedPreferences FS5Pref = this.getSharedPreferences("storeCheckMOStatusFlag5", 0);
            String jsonData5 = FS5Pref.getString("jsonData", "");
            String authString5 = FS5Pref.getString("authString", "");

            if (!jsonData5.trim().isEmpty() && !authString5.trim().isEmpty()) {
                new UpdateMOStatusDetailsOfLink().execute(jsonData5, authString5, "storeCheckMOStatusFlag5");
            }

            //For Hose 6
            SharedPreferences FS6Pref = this.getSharedPreferences("storeCheckMOStatusFlag6", 0);
            String jsonData6 = FS6Pref.getString("jsonData", "");
            String authString6 = FS6Pref.getString("authString", "");

            if (!jsonData6.trim().isEmpty() && !authString6.trim().isEmpty()) {
                new UpdateMOStatusDetailsOfLink().execute(jsonData6, authString6, "storeCheckMOStatusFlag6");
            }
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "updateCheckMOStatusFlagOfLink Exception: " + e.getMessage());
        }
    }

    private void updateResetMOCheckFlagOfLink() {
        try {
            //For Hose One......1
            SharedPreferences FS1Pref = this.getSharedPreferences("storeResetMOCheckFlag1", 0);
            String jsonData1 = FS1Pref.getString("jsonData", "");
            String authString1 = FS1Pref.getString("authString", "");

            if (!jsonData1.trim().isEmpty() && !authString1.trim().isEmpty()) {
                new UpdateMOStatusDetailsOfLink().execute(jsonData1, authString1, "storeResetMOCheckFlag1");
            }

            //For Hose Two....2
            SharedPreferences FS2Pref = this.getSharedPreferences("storeResetMOCheckFlag2", 0);
            String jsonData2 = FS2Pref.getString("jsonData", "");
            String authString2 = FS2Pref.getString("authString", "");

            if (!jsonData2.trim().isEmpty() && !authString2.trim().isEmpty()) {
                new UpdateMOStatusDetailsOfLink().execute(jsonData2, authString2, "storeResetMOCheckFlag2");
            }

            //For Hose Three..3
            SharedPreferences FS3Pref = this.getSharedPreferences("storeResetMOCheckFlag3", 0);
            String jsonData3 = FS3Pref.getString("jsonData", "");
            String authString3 = FS3Pref.getString("authString", "");

            if (!jsonData3.trim().isEmpty() && !authString3.trim().isEmpty()) {
                new UpdateMOStatusDetailsOfLink().execute(jsonData3, authString3, "storeResetMOCheckFlag3");
            }

            //For Hose 4
            SharedPreferences FS4Pref = this.getSharedPreferences("storeResetMOCheckFlag4", 0);
            String jsonData4 = FS4Pref.getString("jsonData", "");
            String authString4 = FS4Pref.getString("authString", "");

            if (!jsonData4.trim().isEmpty() && !authString4.trim().isEmpty()) {
                new UpdateMOStatusDetailsOfLink().execute(jsonData4, authString4, "storeResetMOCheckFlag4");
            }

            //For Hose 5
            SharedPreferences FS5Pref = this.getSharedPreferences("storeResetMOCheckFlag5", 0);
            String jsonData5 = FS5Pref.getString("jsonData", "");
            String authString5 = FS5Pref.getString("authString", "");

            if (!jsonData5.trim().isEmpty() && !authString5.trim().isEmpty()) {
                new UpdateMOStatusDetailsOfLink().execute(jsonData5, authString5, "storeResetMOCheckFlag5");
            }

            //For Hose 6
            SharedPreferences FS6Pref = this.getSharedPreferences("storeResetMOCheckFlag6", 0);
            String jsonData6 = FS6Pref.getString("jsonData", "");
            String authString6 = FS6Pref.getString("authString", "");

            if (!jsonData6.trim().isEmpty() && !authString6.trim().isEmpty()) {
                new UpdateMOStatusDetailsOfLink().execute(jsonData6, authString6, "storeResetMOCheckFlag6");
            }
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "updateResetMOCheckFlagOfLink Exception: " + e.getMessage());
        }
    }

    private void updateManualOverrideStatusOfLink() {
        try {
            //For Hose One......1
            SharedPreferences FS1Pref = this.getSharedPreferences("storeManualOverrideStatus1", 0);
            String jsonData1 = FS1Pref.getString("jsonData", "");
            String authString1 = FS1Pref.getString("authString", "");
            String isServerCallInProgress1 = FS1Pref.getString("isServerCallInProgress", "false");

            if (!jsonData1.trim().isEmpty() && !authString1.trim().isEmpty() && isServerCallInProgress1.equalsIgnoreCase("false")) {
                SharedPreferences.Editor editor = FS1Pref.edit();
                editor.putString("isServerCallInProgress", "true");
                editor.apply();
                new UpdateMOStatusDetailsOfLink().execute(jsonData1, authString1, "storeManualOverrideStatus1");
            }

            //For Hose Two....2
            SharedPreferences FS2Pref = this.getSharedPreferences("storeManualOverrideStatus2", 0);
            String jsonData2 = FS2Pref.getString("jsonData", "");
            String authString2 = FS2Pref.getString("authString", "");
            String isServerCallInProgress2 = FS2Pref.getString("isServerCallInProgress", "false");

            if (!jsonData2.trim().isEmpty() && !authString2.trim().isEmpty() && isServerCallInProgress2.equalsIgnoreCase("false")) {
                SharedPreferences.Editor editor = FS2Pref.edit();
                editor.putString("isServerCallInProgress", "true");
                editor.apply();
                new UpdateMOStatusDetailsOfLink().execute(jsonData2, authString2, "storeManualOverrideStatus2");
            }

            //For Hose Three..3
            SharedPreferences FS3Pref = this.getSharedPreferences("storeManualOverrideStatus3", 0);
            String jsonData3 = FS3Pref.getString("jsonData", "");
            String authString3 = FS3Pref.getString("authString", "");
            String isServerCallInProgress3 = FS3Pref.getString("isServerCallInProgress", "false");

            if (!jsonData3.trim().isEmpty() && !authString3.trim().isEmpty() && isServerCallInProgress3.equalsIgnoreCase("false")) {
                SharedPreferences.Editor editor = FS3Pref.edit();
                editor.putString("isServerCallInProgress", "true");
                editor.apply();
                new UpdateMOStatusDetailsOfLink().execute(jsonData3, authString3, "storeManualOverrideStatus3");
            }

            //For Hose 4
            SharedPreferences FS4Pref = this.getSharedPreferences("storeManualOverrideStatus4", 0);
            String jsonData4 = FS4Pref.getString("jsonData", "");
            String authString4 = FS4Pref.getString("authString", "");
            String isServerCallInProgress4 = FS4Pref.getString("isServerCallInProgress", "false");

            if (!jsonData4.trim().isEmpty() && !authString4.trim().isEmpty() && isServerCallInProgress4.equalsIgnoreCase("false")) {
                SharedPreferences.Editor editor = FS4Pref.edit();
                editor.putString("isServerCallInProgress", "true");
                editor.apply();
                new UpdateMOStatusDetailsOfLink().execute(jsonData4, authString4, "storeManualOverrideStatus4");
            }

            //For Hose 5
            SharedPreferences FS5Pref = this.getSharedPreferences("storeManualOverrideStatus5", 0);
            String jsonData5 = FS5Pref.getString("jsonData", "");
            String authString5 = FS5Pref.getString("authString", "");
            String isServerCallInProgress5 = FS5Pref.getString("isServerCallInProgress", "false");

            if (!jsonData5.trim().isEmpty() && !authString5.trim().isEmpty() && isServerCallInProgress5.equalsIgnoreCase("false")) {
                SharedPreferences.Editor editor = FS5Pref.edit();
                editor.putString("isServerCallInProgress", "true");
                editor.apply();
                new UpdateMOStatusDetailsOfLink().execute(jsonData5, authString5, "storeManualOverrideStatus5");
            }

            //For Hose 6
            SharedPreferences FS6Pref = this.getSharedPreferences("storeManualOverrideStatus6", 0);
            String jsonData6 = FS6Pref.getString("jsonData", "");
            String authString6 = FS6Pref.getString("authString", "");
            String isServerCallInProgress6 = FS6Pref.getString("isServerCallInProgress", "false");

            if (!jsonData6.trim().isEmpty() && !authString6.trim().isEmpty() && isServerCallInProgress6.equalsIgnoreCase("false")) {
                SharedPreferences.Editor editor = FS6Pref.edit();
                editor.putString("isServerCallInProgress", "true");
                editor.apply();
                new UpdateMOStatusDetailsOfLink().execute(jsonData6, authString6, "storeManualOverrideStatus6");
            }
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "updateManualOverrideStatusOfLink Exception: " + e.getMessage());
        }
    }

    public class UpdateMOStatusDetailsOfLink extends AsyncTask<String, Void, String> {
        String PrefName = "";
        protected String doInBackground(String... param) {
            String resp = "";
            PrefName = param[2];

            try {
                OkHttpClient client = new OkHttpClient();
                MediaType TEXT = MediaType.parse("application/text;charset=UTF-8");

                RequestBody body = RequestBody.create(TEXT, param[0]);
                Request request = new Request.Builder()
                        .url(AppConstants.WEB_URL)
                        .post(body)
                        .addHeader("Authorization", param[1])
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
                if (result.contains("success") && !PrefName.isEmpty()) {
                    SharedPreferences preferences = getSharedPreferences(PrefName, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.commit();
                }
            } catch (Exception e) {
                System.out.println("onPostExecute" + e);
            }
        }
    }

}
