package com.TrakEngineering.FluidSecureHub.offline;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.Aes_Encryption;
import com.TrakEngineering.FluidSecureHub.AppConstants;
import com.TrakEngineering.FluidSecureHub.CommonUtils;
import com.TrakEngineering.FluidSecureHub.ConnectionDetector;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListener;
import com.thin.downloadmanager.ThinDownloadManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class OffBackgroundService extends Service {

    //https://github.com/smanikandan14/ThinDownloadManager

    OffDBController controller = new OffDBController(OffBackgroundService.this);

    ConnectionDetector cd = new ConnectionDetector(OffBackgroundService.this);
    private static final String TAG = OffBackgroundService.class.getSimpleName();

    Timer timer;
    TimerTask repeatedTask;

    public OffBackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        deleteAllDownloadedFiles();

        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + " Started offline data downloading..onStartCommand");

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("storeOfflineAccess", Context.MODE_PRIVATE);
        String isOffline = sharedPref.getString("isOffline", "");
        String OFFLineDataDwnldFreq = sharedPref.getString("OFFLineDataDwnldFreq", "Weekly");
        int DayOfWeek = sharedPref.getInt("DayOfWeek", 2);
        int HourOfDay = sharedPref.getInt("HourOfDay", 2);


        //Check step 1
        if (cd.isConnecting() && isOffline.equalsIgnoreCase("True") && checkSharedPrefOfflineData(getApplicationContext())) {

            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " Started offline data downloading.all true.Day-" + OFFLineDataDwnldFreq + " " + DayOfWeek + ":Hr-" + HourOfDay);

            new GetAPIToken().execute();

        } else {

            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " Started offline data downloading..else condition");

            stopSelf();

        }

        return super.onStartCommand(intent, flags, startId);
    }

    public class GetAPIToken extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... param) {
            String resp = "";


            try {

                String Email = CommonUtils.getCustomerDetailsCC(OffBackgroundService.this).PersonEmail;

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
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " GetAPIToken InBackG Ex:" + e.getMessage());

            }


            return resp;
        }


        @Override
        protected void onPostExecute(String result) {

            if (result != null && !result.isEmpty()) {

                try {

                    JSONObject jsonObject = new JSONObject(result);

                    String access_token = jsonObject.getString("access_token");
                    String token_type = jsonObject.getString("token_type");
                    String expires_in = jsonObject.getString("expires_in");
                    String refresh_token = jsonObject.getString("refresh_token");

                    AppConstants.WriteinFile(TAG + " Started offline data downloading..API token success");

                    controller.storeOfflineToken(OffBackgroundService.this, access_token, token_type, expires_in, refresh_token);


                    if (cd.isConnecting()) {
                        Log.e("Totaloffline_check", "Offline data Download 2");


                        new GetAPIHubDetails().execute();

                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " GetAPIToken InPost NoInternet");
                    }


                } catch (JSONException e) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " GetAPIToken InPost Ex:" + e.getMessage());
                }

            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " GetAPIToken InPost Result err:" + result);
            }

        }


    }

    public class GetAPIHubDetails extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... param) {
            String resp = "";

            try {

                String api_token = controller.getOfflineToken(OffBackgroundService.this);
                String Email = CommonUtils.getCustomerDetailsCC(OffBackgroundService.this).PersonEmail;
                String IMEI = AppConstants.getIMEI(OffBackgroundService.this);

                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(AppConstants.API_URL_HUB + "?Email=" + Email + "&IMEI=" + IMEI)
                        .addHeader("Authorization", "bearer " + api_token)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

                //------------------------------

            } catch (Exception e) {

                System.out.println("Ex" + e.getMessage());
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " GetAPIHubDetails InBackG Ex:" + e.getMessage());

            }


            return resp;
        }


        @Override
        protected void onPostExecute(String result) {

            if (result != null && !result.isEmpty()) {


                try {

                    JSONObject jsonObject = new JSONObject(result);

                    String ResponceMessage = jsonObject.getString("ResponceMessage");

                    System.out.println("ResponceMessage:" + ResponceMessage);

                    if (ResponceMessage.equalsIgnoreCase("success")) {


                        JSONObject HubDataObj = jsonObject.getJSONObject("HubDataObj");

                        String AllowedLinks = HubDataObj.getString("AllowedLinks");
                        String PersonnelPINNumberRequired = HubDataObj.getString("PersonnelPINNumberRequired");
                        String VehicleNumberRequired = HubDataObj.getString("VehicleNumberRequired");
                        String PersonhasFOB = HubDataObj.getString("PersonhasFOB");
                        String VehiclehasFOB = HubDataObj.getString("VehiclehasFOB");
                        String WiFiChannel = HubDataObj.getString("WiFiChannel");
                        String BluetoothCardReader = HubDataObj.getString("BluetoothCardReader");
                        String BluetoothCardReaderMacAddress = HubDataObj.getString("BluetoothCardReaderMacAddress");
                        String LFBluetoothCardReader = HubDataObj.getString("LFBluetoothCardReader");
                        String LFBluetoothCardReaderMacAddress = HubDataObj.getString("LFBluetoothCardReaderMacAddress");
                        String PrinterMacAddress = HubDataObj.getString("PrinterMacAddress");
                        String PrinterName = HubDataObj.getString("PrinterName");

                        String HubId = HubDataObj.getString("HubId");
                        String EnablePrinter = HubDataObj.getString("EnablePrinter");

                        String VehicleDataFilePath = HubDataObj.getString("VehicleDataFilePath");
                        String PersonnelDataFilePath = HubDataObj.getString("PersonnelDataFilePath");
                        String LinkDataFilePath = HubDataObj.getString("LinkDataFilePath");

                        controller.storeOfflineHubDetails(OffBackgroundService.this, HubId, AllowedLinks, PersonnelPINNumberRequired, VehicleNumberRequired, PersonhasFOB, VehiclehasFOB, WiFiChannel,
                                BluetoothCardReader, BluetoothCardReaderMacAddress, LFBluetoothCardReader, LFBluetoothCardReaderMacAddress,
                                PrinterMacAddress, PrinterName, EnablePrinter, VehicleDataFilePath, PersonnelDataFilePath, LinkDataFilePath);

                        if (cd.isConnecting()) {

                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " Offline data Link,Vehicle,Pin Start ");


                            new GetAPILinkDetails().execute();

                            new GetAPIVehicleDetails().execute();

                            new GetAPIPersonnelPinDetails().execute();


                            AppConstants.clearSharedPrefByName(OffBackgroundService.this, "DownloadFileStatus");

                            startDownloadTimerTask();

                        } else {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " GetAPIHubDetails InPost NoInternet");
                        }

                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " GetAPIHubDetails InPost Response fail" + result);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " GetAPIHubDetails InPost Ex:" + e.getMessage());
                }

            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " GetAPIHubDetails InPost Response err:" + result);
            }

        }
    }

    public void startDownloadTimerTask() {

        EntityHub obj = controller.getOfflineHubDetails(OffBackgroundService.this);

        String VehicleDataFilePath = obj.VehicleDataFilePath;
        String PersonnelDataFilePath = obj.PersonnelDataFilePath;
        String LinkDataFilePath = obj.LinkDataFilePath;


        repeatedTask = new TimerTask() {
            public void run() {

                System.out.println("startDownloadTimerTask**********");

                String status_v = getDownloadFileStatus("Vehicle");

                if (status_v.isEmpty() || status_v.equalsIgnoreCase("2"))
                    downloadLibrary(VehicleDataFilePath, "Vehicle");


                String status_p = getDownloadFileStatus("Personnel");

                if (status_p.isEmpty() || status_p.equalsIgnoreCase("2"))
                    downloadLibrary(PersonnelDataFilePath, "Personnel");


                String status_l = getDownloadFileStatus("Link");

                if (status_l.isEmpty() || status_l.equalsIgnoreCase("2"))
                    downloadLibrary(LinkDataFilePath, "Link");


                if (status_v.equalsIgnoreCase("1") && status_p.equalsIgnoreCase("1") && status_l.equalsIgnoreCase("1")) {

                    setSharedPrefOfflineData(getApplicationContext());

                    if (timer != null)
                        timer.cancel();

                    AppConstants.WriteinFile("All 3 files downloaded successfully.");

                }


            }
        };

        long delay = 5000L;
        long period = 60000L;
        timer = new Timer("TimerOffDownload");

        timer.scheduleAtFixedRate(repeatedTask, delay, period);

    }


    public class GetAPILinkDetails extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... param) {
            String resp = "";


            try {

                String api_token = controller.getOfflineToken(OffBackgroundService.this);
                String Email = CommonUtils.getCustomerDetailsCC(OffBackgroundService.this).PersonEmail;
                String IMEI = AppConstants.getIMEI(OffBackgroundService.this);

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(4, TimeUnit.SECONDS);
                client.setReadTimeout(4, TimeUnit.SECONDS);
                client.setWriteTimeout(4, TimeUnit.SECONDS);

                Request request = new Request.Builder()
                        .url(AppConstants.API_URL_LINK + "?Email=" + Email + "&IMEI=" + IMEI)
                        .addHeader("Authorization", "bearer " + api_token)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

                //------------------------------

            } catch (Exception e) {

                System.out.println("Ex" + e.getMessage());
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " GetAPILinkDetails InBackG Ex:" + e.toString());

            }


            return resp;
        }


        @Override
        protected void onPostExecute(String result) {

            return;

        }
    }

    public void linkJsonParsing(String result) {

        if (result != null && !result.isEmpty()) {

            try {

                long InsetFT = -1, InsetLD = -1;
                JSONObject jsonObject = new JSONObject(result);

                String ResponceMessage = jsonObject.getString("ResponceMessage");

                System.out.println("ResponceMessage:" + ResponceMessage);

                if (ResponceMessage.equalsIgnoreCase("success")) {


                    controller.deleteTableData(OffDBController.TBL_LINK);

                    JSONArray jsonArr = jsonObject.getJSONArray("LinkDataObj");

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " GetAPILinkDetails Json length=" + jsonArr.length());

                    if (jsonArr != null && jsonArr.length() > 0) {
                        for (int j = 0; j < jsonArr.length(); j++) {
                            JSONObject jsonObj = (JSONObject) jsonArr.get(j);

                            String SiteId = jsonObj.getString("SiteId");
                            String WifiSSId = jsonObj.getString("WifiSSId");
                            String PumpOnTime = jsonObj.getString("PumpOnTime");
                            String PumpOffTime = jsonObj.getString("PumpOffTime");
                            String AuthorizedFuelingDays = jsonObj.getString("AuthorizedFuelingDays");
                            String Pulserratio = jsonObj.getString("Pulserratio");
                            String MacAddress = jsonObj.getString("MacAddress");
                            String IsTLDCall = jsonObj.getString("IsTLDCall");

                            JSONArray FuelingTimesObj = jsonObj.getJSONArray("FuelingTimesObj");

                            if (FuelingTimesObj != null & FuelingTimesObj.length() > 0) {

                                for (int i = 0; i < FuelingTimesObj.length(); i++) {

                                    JSONObject oj = (JSONObject) FuelingTimesObj.get(i);
                                    String FromTime = oj.getString("FromTime");
                                    String ToTime = oj.getString("ToTime");

                                    InsetFT = controller.insertFuelTimings(SiteId, "", FromTime, ToTime);

                                    if (InsetFT == -1)
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + " GetAPILinkDetails Something went wrong inserting FuelTimings");

                                }
                            }

                            InsetLD = controller.insertLinkDetails(SiteId, WifiSSId, PumpOnTime, PumpOffTime, AuthorizedFuelingDays, Pulserratio, MacAddress, IsTLDCall);

                            if (InsetLD == -1)
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " GetAPILinkDetails Something went wrong inserting LinkDetails");

                        }
                    }

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " Offline data Link download process completed Successfully");

                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " GetAPILinkDetails InPost Response fail" + result);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " GetAPILinkDetails InPost Ex:" + e.toString());
            }

        } else {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " GetAPILinkDetails InPost Result err" + result);
        }
    }

    public class GetAPIVehicleDetails extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... param) {
            String resp = "";


            try {

                String api_token = controller.getOfflineToken(OffBackgroundService.this);
                String Email = CommonUtils.getCustomerDetailsCC(OffBackgroundService.this).PersonEmail;
                String IMEI = AppConstants.getIMEI(OffBackgroundService.this);

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(4, TimeUnit.SECONDS);
                client.setReadTimeout(4, TimeUnit.SECONDS);
                client.setWriteTimeout(4, TimeUnit.SECONDS);

                Request request = new Request.Builder()
                        .url(AppConstants.API_URL_VEHICLE + "?Email=" + Email + "&IMEI=" + IMEI)
                        .addHeader("Authorization", "bearer " + api_token)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

                //------------------------------

            } catch (Exception e) {
                System.out.println("Ex" + e.getMessage());
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " GetAPIVehicleDetails InBack Ex:" + e.toString());

            }


            return resp;
        }


        @Override
        protected void onPostExecute(String result) {

            return;
        }
    }


    public void vehicleJsonParsing(String result) {
        if (result != null && !result.isEmpty()) {

            try {
                long InsertVD = -1;
                JSONObject jsonObject = new JSONObject(result);

                String ResponceMessage = jsonObject.getString("ResponceMessage");

                System.out.println("ResponceMessage:" + ResponceMessage);

                if (ResponceMessage.equalsIgnoreCase("success")) {


                    controller.deleteTableData(OffDBController.TBL_VEHICLE);

                    JSONArray jsonArr = jsonObject.getJSONArray("VehicleDataObj");

                    if (jsonArr != null && jsonArr.length() > 0) {
                        for (int j = 0; j < jsonArr.length(); j++) {
                            JSONObject jsonObj = (JSONObject) jsonArr.get(j);

                            String VehicleId = jsonObj.getString("VehicleId");
                            String VehicleNumber = jsonObj.getString("VehicleNumber");
                            String CurrentOdometer = jsonObj.getString("CurrentOdometer");
                            String CurrentHours = jsonObj.getString("CurrentHours");
                            String RequireOdometerEntry = jsonObj.getString("RequireOdometerEntry");
                            String RequireHours = jsonObj.getString("RequireHours");
                            String FuelLimitPerTxn = jsonObj.getString("FuelLimitPerTxn");
                            String FuelLimitPerDay = jsonObj.getString("FuelLimitPerDay");
                            String FOBNumber = jsonObj.getString("FOBNumber");
                            String AllowedLinks = jsonObj.getString("AllowedLinks");
                            String Active = jsonObj.getString("Active");

                            String CheckOdometerReasonable = jsonObj.getString("CheckOdometerReasonable");
                            String OdometerReasonabilityConditions = jsonObj.getString("OdometerReasonabilityConditions");
                            String OdoLimit = jsonObj.getString("OdoLimit");
                            String HoursLimit = jsonObj.getString("HoursLimit");
                            String BarcodeNumber = jsonObj.getString("Barcode");
                            String IsExtraOther = jsonObj.getString("IsExtraOther");
                            String ExtraOtherLabel = jsonObj.getString("ExtraOtherLabel");


                            InsertVD = controller.insertVehicleDetails(VehicleId, VehicleNumber, CurrentOdometer, CurrentHours, RequireOdometerEntry, RequireHours, FuelLimitPerTxn, FuelLimitPerDay, FOBNumber, AllowedLinks, Active,
                                    CheckOdometerReasonable, OdometerReasonabilityConditions, OdoLimit, HoursLimit, BarcodeNumber, IsExtraOther, ExtraOtherLabel);

                            if (InsertVD == -1)
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " GetAPIVehicleDetails Something went wrong inserting VehicleDetails ");

                        }
                    }

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " Offline data Vehicle download process completed Successfully");

                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " GetAPIVehicleDetails InPost Responce fail" + result);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " GetAPIVehicleDetails InPost Ex:" + e.toString());
            }

        } else {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " GetAPIVehicleDetails InPost Result err:" + result);
        }
    }

    public class GetAPIPersonnelPinDetails extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... param) {
            String resp = "";


            try {

                String api_token = controller.getOfflineToken(OffBackgroundService.this);
                String Email = CommonUtils.getCustomerDetailsCC(OffBackgroundService.this).PersonEmail;
                String IMEI = AppConstants.getIMEI(OffBackgroundService.this);

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(4, TimeUnit.SECONDS);
                client.setReadTimeout(4, TimeUnit.SECONDS);
                client.setWriteTimeout(4, TimeUnit.SECONDS);

                Request request = new Request.Builder()
                        .url(AppConstants.API_URL_PERSONNEL + "?Email=" + Email + "&IMEI=" + IMEI)
                        .addHeader("Authorization", "bearer " + api_token)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

                //------------------------------

            } catch (Exception e) {
                System.out.println("Ex" + e.getMessage());
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " GetAPIPersonnelPinDetails InBack Ex:" + e.toString());

            }


            return resp;
        }


        @Override
        protected void onPostExecute(String result) {

            return;
        }

    }

    public void personnelJsonParsing(String result) {

        if (result != null && !result.isEmpty()) {

            try {

                long InsertPD = -1;
                JSONObject jsonObject = new JSONObject(result);

                String ResponceMessage = jsonObject.getString("ResponceMessage");

                System.out.println("ResponceMessage:" + ResponceMessage);

                if (ResponceMessage.equalsIgnoreCase("success")) {


                    controller.deleteTableData(OffDBController.TBL_PERSONNEL);

                    JSONArray jsonArr = jsonObject.getJSONArray("PersonDataObj");

                    if (jsonArr != null && jsonArr.length() > 0) {
                        for (int j = 0; j < jsonArr.length(); j++) {
                            JSONObject jsonObj = (JSONObject) jsonArr.get(j);

                            String PersonId = jsonObj.getString("PersonId");
                            String PinNumber = jsonObj.getString("PinNumber");
                            String FuelLimitPerTxn = jsonObj.getString("FuelLimitPerTxn");
                            String FuelLimitPerDay = jsonObj.getString("FuelLimitPerDay");
                            String FOBNumber = jsonObj.getString("FOBNumber");
                            String Authorizedlinks = jsonObj.getString("Authorizedlinks");
                            String AssignedVehicles = jsonObj.getString("AssignedVehicles");

                            JSONArray FuelingTimesObj = jsonObj.getJSONArray("FuelingTimesObj");

                            if (FuelingTimesObj != null & FuelingTimesObj.length() > 0) {

                                for (int i = 0; i < FuelingTimesObj.length(); i++) {

                                    JSONObject oj = (JSONObject) FuelingTimesObj.get(i);
                                    String FromTime = oj.getString("FromTime");
                                    String ToTime = oj.getString("ToTime");

                                    controller.insertFuelTimings("", PersonId, FromTime, ToTime);
                                }
                            }

                            InsertPD = controller.insertPersonnelPinDetails(PersonId, PinNumber, FuelLimitPerTxn, FuelLimitPerDay, FOBNumber, Authorizedlinks, AssignedVehicles);

                            if (InsertPD == -1)
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " GetAPIPersonnelPinDetails Something went wrong inserting PersonnelDetails");

                        }

                        String SaveDate = CommonUtils.getDateInString();
                        CommonUtils.SaveOfflineDbSizeDateTime(OffBackgroundService.this, SaveDate);


                    }


                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " GetAPIPersonnelPinDetails InPost Response fail:" + result);
                }


                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Offline data Personnel download process completed Successfully");

            } catch (JSONException e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " GetAPIPersonnelPinDetails InPost Ex:" + e.toString());
            }

        } else {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " GetAPIPersonnelPinDetails InPost Result err:" + result);
        }
    }

    public static boolean checkSharedPrefOfflineData(Context myctx) {
        SharedPreferences sharedPrefODO = myctx.getSharedPreferences("OfflineData", Context.MODE_PRIVATE);
        String last_date = sharedPrefODO.getString("last_date", "");


        String curr_date = AppConstants.currentDateFormat("dd/MM/yyyy");

        System.out.println(last_date + "  -" + "-  " + curr_date);

        if (curr_date.trim().equalsIgnoreCase(last_date.trim())) {
            return false;
        } else
            return true;

    }

    private static void setSharedPrefOfflineData(Context myctx) {
        SharedPreferences sharedPref = myctx.getSharedPreferences("OfflineData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("last_date", AppConstants.currentDateFormat("dd/MM/yyyy"));
        editor.apply();

    }

    public void insertDownloadFileStatus(String filename, String status) {

        SharedPreferences sharedPref = OffBackgroundService.this.getSharedPreferences("DownloadFileStatus", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(filename, status);
        editor.commit();

    }

    public String getDownloadFileStatus(String filename) {

        SharedPreferences sharedPrefODO = OffBackgroundService.this.getSharedPreferences("DownloadFileStatus", Context.MODE_PRIVATE);
        String status = sharedPrefODO.getString(filename, "");

        return status;
    }


    public void downloadLibrary(String downloadUrl, String fileName) {
        //https://github.com/smanikandan14/ThinDownloadManager

        ThinDownloadManager downloadManager = new ThinDownloadManager();


        Uri downloadUri = Uri.parse(downloadUrl);
        Uri destinationUri = Uri.parse(Environment.getExternalStorageDirectory() + "/FSdata/" + fileName + ".txt");
        DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
                //.addCustomHeader("Auth-Token", "YourTokenApiKey")
                .setRetryPolicy(new DefaultRetryPolicy())
                .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                .setDownloadResumable(true)
                //.setDownloadContext(downloadContextObject)//Optional
                .setDownloadListener(new DownloadStatusListener() {
                    @Override
                    public void onDownloadComplete(int id) {
                        AppConstants.WriteinFile("download-Complete--" + fileName);

                        insertDownloadFileStatus(fileName, "1");

                        readEncryptedFileParseJsonInSqlite(fileName);


                    }

                    @Override
                    public void onDownloadFailed(int id, int errorCode, String errorMessage) {
                        AppConstants.WriteinFile("download-Failed--" + fileName + " " + errorCode + " " + errorMessage);

                        insertDownloadFileStatus(fileName, "2");

                        if (errorCode == 416) {
                            insertDownloadFileStatus(fileName, "1");

                            readEncryptedFileParseJsonInSqlite(fileName);
                        }

                    }

                    @Override
                    public void onProgress(int id, long totalBytes, long downlaodedBytes, int progress) {


                        insertDownloadFileStatus(fileName, "3");
                        //AppConstants.WriteinFile("download-onProgress--" + fileName + " " + totalBytes + " " + downlaodedBytes + " " + progress);
                    }
                });


        int downloadId = downloadManager.add(downloadRequest);
    }

    public void readEncryptedFileParseJsonInSqlite(String file_name) {

        File file = new File(Environment.getExternalStorageDirectory() + "/FSdata/" + file_name + ".txt");

        //File file = new File(file_pathrul);

        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {

        }

        final String secretKey = "(fs@!<(t!8*N+^e9";
        String decryptedJson = "";

        try {

            String normal = text.toString();
            String withoutFirstCharacter = normal.substring(1);

            if (withoutFirstCharacter != null && !withoutFirstCharacter.trim().isEmpty()) {

                byte[] base64normal = Base64.decode(withoutFirstCharacter, Base64.DEFAULT);

                Aes_Encryption as = new Aes_Encryption();
                byte[] dsd = as.decrypt(base64normal, secretKey.getBytes(), secretKey.getBytes());
                decryptedJson = new String(dsd);

                if (file_name.equalsIgnoreCase("Vehicle")) {

                    vehicleJsonParsing(decryptedJson);

                } else if (file_name.equalsIgnoreCase("Personnel")) {

                    personnelJsonParsing(decryptedJson);

                } else if (file_name.equalsIgnoreCase("Link")) {

                    linkJsonParsing(decryptedJson);

                }

            }
        } catch (Exception e) {
            System.out.println(e);
        }


    }

    public void deleteAllDownloadedFiles() {
        try {
            File dir = new File(Environment.getExternalStorageDirectory() + "/FSdata");
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    new File(dir, children[i]).delete();
                }
            }
        } catch (Exception e) {
            AppConstants.WriteinFile("deleteAllDownloadedFiles-" + e.getMessage());
        }
    }

}