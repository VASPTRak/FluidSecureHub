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

import com.TrakEngineering.FluidSecureHub.enity.TransactionStatus;
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
    public static String TAG = AppConstants.LOG_MAINTAIN + "-" + "BackgroundService ";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "BackgroundService is on....");
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "Started.");
        //Log.e("Totaloffline_check","Online data BackgroundService");
        //If all hoses are free cleare
        if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE") && Constants.FS_5STATUS.equalsIgnoreCase("FREE") && Constants.FS_6STATUS.equalsIgnoreCase("FREE")) {
            AppConstants.ListOfRunningTransactiins.clear();
        }

        //---UpdateTransaction Status to server------
        ArrayList<HashMap<String, String>> stsData = controller.getAllTransStatus();

        if (stsData != null && stsData.size() > 0) {

            for (int i = 0; i < stsData.size(); i++) {

                String Id = stsData.get(i).get("Id");
                String transId = stsData.get(i).get("transId");
                String transStatus = stsData.get(i).get("transStatus");
                System.out.println("resp...Transstatus transId:" + transId + " :"+transStatus);
                new SetTransactionStatus().execute(Id, transId, transStatus);

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
                    //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Transaction UploadTask Id:" + Id + " jsonData:" + jsonData + " authString:" + authString);

                    if (!jsonData.equals("")) {

                        JSONObject jsonObject = new JSONObject(jsonData);
                        String txn = String.valueOf(jsonObject.get("TransactionId"));
                        if (AppConstants.ListOfRunningTransactiins.contains(txn)) {
                            //transaction running
                            Log.i(TAG, "Transaction is in progress" + txn);
                            //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "TempLog  Transaction is in progress: " + txn);

                        } else {
                            //transaction completed
                            //new UploadTask().execute(Id, jsonData, authString);
                            UploadTaskRetroFit(Id, jsonData, authString);
                            //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "TempLog  Transaction UploadTask Id:" + Id + " jsonData:" + jsonData);
                        }

                    } else {

                        controller.deleteTransactions(Id);
                        System.out.println("deleteTransactions..." + Id);
                        Log.i(TAG, " Empty json Id:" + Id + " jsonData:" + jsonData + " authString:" + authString);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "  Transaction is in progress UploadTask--JSONException " + e);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "  Transaction is in progress UploadTask--Exception " + ex);
                }

            }

        }

        if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE") && Constants.FS_5STATUS.equalsIgnoreCase("FREE") && Constants.FS_6STATUS.equalsIgnoreCase("FREE")) {
            ReplaceHoseNameFlagSynToServer();
        }

        uploadLast20TransactionOnce(); // last 20 trxn

        uploadConnectionissueLogtoserver();// upload connection issue log.

        //Clear shared preferance regarding Last 20 trxn
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AppConstants.clearSharedPrefByName(BackgroundService.this,"storeCmtxtnid_20_record");
                AppConstants.clearSharedPrefByName(BackgroundService.this,AppConstants.LinkConnectionIssuePref);
                stopSelf();
            }
        },10000);


        return super.onStartCommand(intent, flags, startId);
    }

   void ReplaceHoseNameFlagSynToServer(){
        try {
            //For Hose One......1
            SharedPreferences FS1Pref = this.getSharedPreferences("storeIsRenameFlagFS1", 0);
            boolean pflag1 = FS1Pref.getBoolean("flag", false);
            String jsonData1 = FS1Pref.getString("jsonData", "");
            String authString1 = FS1Pref.getString("authString", "");

            if (pflag1 && !jsonData1.trim().isEmpty() && !authString1.trim().isEmpty()) {
                Log.i("SharedPreferences_check1 ",pflag1+">>"+jsonData1);
                new SetHoseNameReplacedFlag().execute(jsonData1, authString1,"storeIsRenameFlagFS1");
            }

            //For Hose Two....2
            SharedPreferences FS2Pref = this.getSharedPreferences("storeIsRenameFlagFS2", 0);
            boolean pflag2 = FS2Pref.getBoolean("flag", false);
            String jsonData2 = FS2Pref.getString("jsonData", "");
            String authString2 = FS2Pref.getString("authString", "");

            if (pflag2 && !jsonData2.trim().isEmpty() && !authString2.trim().isEmpty()) {
                Log.i("SharedPreferences_check2 ",pflag2+">>"+jsonData2);
                new SetHoseNameReplacedFlag().execute(jsonData2, authString2,"storeIsRenameFlagFS2");
            }

            //For Hose Three..3
            SharedPreferences FS3Pref = this.getSharedPreferences("storeIsRenameFlagFS3", 0);
            boolean pflag3 = FS3Pref.getBoolean("flag", false);
            String jsonData3 = FS3Pref.getString("jsonData", "");
            String authString3 = FS3Pref.getString("authString", "");

            if (pflag3 && !jsonData3.trim().isEmpty() && !authString3.trim().isEmpty()) {
                Log.i("SharedPreferences_check3 ",pflag3+">>"+jsonData3);
                new SetHoseNameReplacedFlag().execute(jsonData3, authString3,"storeIsRenameFlagFS3");
            }


            //For Hose 4
            SharedPreferences FS4Pref = this.getSharedPreferences("storeIsRenameFlagFS4", 0);
            boolean pflag4 = FS4Pref.getBoolean("flag", false);
            String jsonData4 = FS4Pref.getString("jsonData", "");
            String authString4 = FS4Pref.getString("authString", "");

            if (pflag4 && !jsonData4.trim().isEmpty() && !authString4.trim().isEmpty()) {
                Log.i("SharedPreferences_check4 ",pflag4+">>"+jsonData4);
                new SetHoseNameReplacedFlag().execute(jsonData4, authString4,"storeIsRenameFlagFS4");
            }


            //For Hose 5
            SharedPreferences FS5Pref = this.getSharedPreferences("storeIsRenameFlagFS5", 0);
            boolean pflag5 = FS5Pref.getBoolean("flag", false);
            String jsonData5 = FS5Pref.getString("jsonData", "");
            String authString5 = FS5Pref.getString("authString", "");

            if (pflag5 && !jsonData5.trim().isEmpty() && !authString5.trim().isEmpty()) {
                new SetHoseNameReplacedFlag().execute(jsonData5, authString5,"storeIsRenameFlagFS5");
            }

            //For Hose 6
            SharedPreferences FS6Pref = this.getSharedPreferences("storeIsRenameFlagFS6", 0);
            boolean pflag6 = FS6Pref.getBoolean("flag", false);
            String jsonData6 = FS6Pref.getString("jsonData", "");
            String authString6 = FS6Pref.getString("authString", "");

            if (pflag6 && !jsonData6.trim().isEmpty() && !authString6.trim().isEmpty()) {
                new SetHoseNameReplacedFlag().execute(jsonData6, authString6,"storeIsRenameFlagFS6");
            }
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "ReplaceHoseNameFlagSynToServer Exception: " + e.getMessage());
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
                        .url(AppConstants.webURL)
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
        for (int i = 1; i < 7 ; i++) {
            String linkJsonData = sharedPref.getString("LINK"+i, "");
            if (!linkJsonData.equals("")){
                SaveMultipleTransactionsRetroFit(linkJsonData);
            }
        }


        /*//Below code is to merge all links data.
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);

        String Merged_jsonData = "{\"cmtxtnid_20_record\":"+json+"}";
        Log.i(TAG,"Merged_json"+Merged_jsonData);*/


    }

    private void uploadConnectionissueLogtoserver(){

        SharedPreferences sharedPref = BackgroundService.this.getSharedPreferences(AppConstants.LinkConnectionIssuePref, Context.MODE_PRIVATE);
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

    public void UploadTaskRetroFit(String Id, String jsonData, String authString) {

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
                .baseUrl(AppConstants.webIP)
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
                Log.i(TAG, "UploadTaskRetroFit ResponceMessage:"+ResponceMessage+ " ResponceText:"+ResponceText);

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

                            //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " TransactionId not found. deleted from Sqlite -json:"+jsonData);
                            controller.deleteTransactions(Id);

                        }
                    }

                    ArrayList<HashMap<String, String>> uData = controller.getAllTransaction();

                    if (uData != null && uData.size() == 0) {
                        stopSelf();
                    }

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " UploadTaskRetroFit onResponse Exception: " + e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                // handle execution failures like no internet connectivity
                BusProvider.getInstance().post(new ErrorEvent(-2, t.getMessage()));
                Log.i(TAG, "Something went wrong in UploadTaskRetroFit call No internet connectivity or server connection fail.");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " UploadTaskRetroFit onFailure: " + t.getMessage());
            }
        });

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
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService.this) + ":" + userEmail + ":" + "LINKDisconnectionErrorLog");
                response = serverHandler.PostTextData(BackgroundService.this, AppConstants.webURL, jsonData, authString);
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

                        //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "success LINKDisconnectionErrorLog");

                    } else if (ResponseMessage.equalsIgnoreCase("fail")) {

                        //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " Fail LINKDisconnectionErrorLog");

                    }

                }

            } catch (Exception e) {

                if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  LINKDisconnectionErrorLog onPostExecute--Exception " + e);

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
                .baseUrl(AppConstants.webIP)
                .build();
        Interface service = retrofit.create(Interface.class);

        SharedPreferences sharedPref = BackgroundService.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String userEmail = sharedPref.getString(AppConstants.USER_EMAIL, "");
        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService.this) + ":" + userEmail + ":" + "SaveMultipleTransactions");

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
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " SaveMultipleTransactionsRetroFit onFailure: " + t.getMessage());
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


                response = serverHandler.PostTextData(BackgroundService.this, AppConstants.webURL, jsonData, authString);

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

                        if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " TransactionId not found. deleted from Sqlite -json:"+jsonData);
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
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService.this) + ":" + userEmail + ":" + "SaveMultipleTransactions");
                if (jsonData.trim().length() > 2)
                    response = serverHandler.PostTextData(BackgroundService.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {
                //AppConstants.WriteinFile(getApplicationContext(), this.getClass().getSimpleName() + "~~~" + ex.getMessage());
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
                // AppConstants.WriteinFile(getApplicationContext(), this.getClass().getSimpleName() + "~~~" + e.getMessage());
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
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService.this) + ":" + userEmail + ":" + "UpgradeTransactionStatus");

                TransactionStatus objTS = new TransactionStatus();
                objTS.TransactionId = transId;
                objTS.Status = transStatus;
                Gson gson = new Gson();
                String jsonData = gson.toJson(objTS);
                System.out.println("TransactionStatus......" + jsonData);


                response = serverHandler.PostTextData(BackgroundService.this, AppConstants.webURL, jsonData, authString);

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

}
