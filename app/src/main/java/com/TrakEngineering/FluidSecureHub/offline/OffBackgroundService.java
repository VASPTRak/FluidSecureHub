package com.TrakEngineering.FluidSecureHub.offline;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.Aes_Encryption;
import com.TrakEngineering.FluidSecureHub.AppConstants;
import com.TrakEngineering.FluidSecureHub.CommonUtils;
import com.TrakEngineering.FluidSecureHub.ConnectionDetector;
import com.TrakEngineering.FluidSecureHub.Constants;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListener;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class OffBackgroundService extends Service {

    //https://github.com/smanikandan14/ThinDownloadManager

    OffDBController controller = new OffDBController(OffBackgroundService.this);

    ConnectionDetector cd = new ConnectionDetector(OffBackgroundService.this);
    private static final String TAG = AppConstants.LOG_BACKGROUND + "-" + OffBackgroundService.class.getSimpleName();

    Timer timer;
    TimerTask repeatedTask;
    SimpleDateFormat timeParser = new SimpleDateFormat("HH:mm");

    public OffBackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {

            if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE") && Constants.FS_5STATUS.equalsIgnoreCase("FREE") && Constants.FS_6STATUS.equalsIgnoreCase("FREE")) {
                AppConstants.selectHosePressed = false;
                Log.i(TAG, " onStartCommand -------------- _templog");
                /*if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Started.");*/

                OffDBController offcontroller = new OffDBController(this);
                //HashMap<String, String> linkmap = offcontroller.getAllLinksDetails();

                //if (linkmap.size() > 0) {

                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("storeOfflineAccess", Context.MODE_PRIVATE);
                String isOffline = sharedPref.getString("isOffline", "");
                String OFFLineDataDwnldFreq = sharedPref.getString("OFFLineDataDwnldFreq", "Weekly");
                int WeekDay = sharedPref.getInt("DayOfWeek", 2);
                int SavedOfflineHourOfDay = sharedPref.getInt("HourOfDay", 2);
                int SavedOfflineMinuteOfHour = sharedPref.getInt("MinuteOfHour", 22);

                Date date = new Date();   // given date
                Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
                calendar.setTime(date);   // assigns calendar to given date
                int CurrentDay = calendar.get(Calendar.DAY_OF_WEEK);
                int CurrentHour24 = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
                int CurrentHour12 = calendar.get(Calendar.HOUR);
                int CurrentMinutes = calendar.get(Calendar.MINUTE);

                if (cd.isConnecting() && isOffline.equalsIgnoreCase("True")) {
                    if (checkSharedPrefOfflineData()) {
                        if (checkOfflineDataTime(CurrentHour24, CurrentMinutes, SavedOfflineHourOfDay, SavedOfflineMinuteOfHour)) {
                            if (OFFLineDataDwnldFreq.equalsIgnoreCase("Weekly") && WeekDay == CurrentDay) {
                                //Weekly logic
                                Log.i(TAG, " Started Offline data download Frequency>>" + OFFLineDataDwnldFreq);
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " Started Offline data download Frequency>>" + OFFLineDataDwnldFreq);
                                deleteAllDownloadedFiles();

                                new GetAPIToken().execute();

                            } else if (OFFLineDataDwnldFreq.equalsIgnoreCase("Daily")) {
                                //Everyday logic
                                Log.i(TAG, " Started Offline data download Frequency>>" + OFFLineDataDwnldFreq);
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " Started Offline data download Frequency>>" + OFFLineDataDwnldFreq);
                                deleteAllDownloadedFiles();

                                new GetAPIToken().execute();

                            } else {
                                //WeekDay did not match
                                Log.i(TAG, " Skip download offline data scheduled on Weekday>>" + WeekDay + " CurrentWeekDay>>" + CurrentDay);
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " Skip download offline data scheduled on Weekday>>" + WeekDay + " CurrentWeekDay>>" + CurrentDay);
                            }
                        }
                    }
                } else {
                    //NO internet connection 0r  Offline status False
                    Log.i(TAG, " Internet connection status>>" + cd.isConnecting() + " Offline status>>" + isOffline);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " Internet connection status>>" + cd.isConnecting() + " Offline status>>" + isOffline);
                }

                /*if (cd.isConnecting() && isOffline.equalsIgnoreCase("True") && checkSharedPrefOfflineData()) {

                    if (OFFLineDataDwnldFreq.equalsIgnoreCase("Weekly") && WeekDay == CurrentDay) {
                        //Weekly logic
                        Log.i(TAG, " Started Offline data download Frequency>>" + OFFLineDataDwnldFreq);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Started Offline data download Frequency>>" + OFFLineDataDwnldFreq);
                        deleteAllDownloadedFiles();

                        new GetAPIToken().execute();

                    } else if (OFFLineDataDwnldFreq.equalsIgnoreCase("Daily")) {
                        //Everyday logic
                        Log.i(TAG, " Started Offline data download Frequency>>" + OFFLineDataDwnldFreq);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Started Offline data download Frequency>>" + OFFLineDataDwnldFreq);
                        deleteAllDownloadedFiles();

                        new GetAPIToken().execute();

                    } else {
                        //WeekDay did not match
                        Log.i(TAG, " Skip download offline data scheduled on Weekday>>" + WeekDay + " CurrentWeekDay>>" + CurrentDay);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Skip download offline data scheduled on Weekday>>" + WeekDay + " CurrentWeekDay>>" + CurrentDay);
                    }

                } else {
                    //NO internet connection 0r  Offline status False
                    Log.i(TAG, " Internet connection status>>" + cd.isConnecting() + " Offline status>>" + isOffline);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " Internet connection status>>" + cd.isConnecting() + " Offline status>>" + isOffline);
                }*/
                //} else {

                //    if (AppConstants.GenerateLogs)
                //        AppConstants.WriteinFile(TAG + " No previous offline data.");

                    /*if (!AppConstants.selectHosePressed) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " No previous offline data hence start offline data download.");

                        deleteAllDownloadedFiles();

                        new GetAPIToken().execute();
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " No previous offline data but select hose is pressed.");

                    }*/
                //}
            } else {
                Log.i(TAG, " onStartCommand -------------- One of the hose is busy, Skip offline data download");
                cancelThinDownloadManager();
            }
            stopSelf();

        } catch (Exception e) {

            Log.i(TAG, " onStartCommand Exception:" + e.toString());
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " onStartCommand Exception:" + e.toString());
            stopSelf();
        }

        try {
            if (!OfflineConstants.isOfflineAccess(OffBackgroundService.this)) {
                ThinDownloadManager downloadManager = new ThinDownloadManager();
                if (AppConstants.offlineDownloadIds != null && AppConstants.offlineDownloadIds.size() > 0) {
                    downloadManager.cancelAll();
                    AppConstants.offlineDownloadIds.clear();
                }
            }
        } catch (Exception e) {
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
                        .url(AppConstants.API_URL_HUB + "?Email=" + Email + "&IMEI=" + IMEI +"&AcceptBlankPath=y")
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

                    System.out.println("ResponseMessage:" + ResponceMessage);

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
                        String IsNonValidateVehicle = HubDataObj.getString("IsNonValidateVehicle");
                        String IsNonValidatePerson = HubDataObj.getString("IsNonValidatePerson");
                        String IsNonValidateODOM = HubDataObj.getString("IsNonValidateODOM");
                        String IsOtherRequire = HubDataObj.getString("IsOtherRequire");
                        String OtherLabel = HubDataObj.getString("OtherLabel");
                        String HUBType = HubDataObj.getString("HUBType");

                        controller.storeOfflineHubDetails(OffBackgroundService.this, HubId, AllowedLinks, PersonnelPINNumberRequired, VehicleNumberRequired, PersonhasFOB,
                                VehiclehasFOB, WiFiChannel, BluetoothCardReader, BluetoothCardReaderMacAddress, LFBluetoothCardReader, LFBluetoothCardReaderMacAddress,
                                PrinterMacAddress, PrinterName, EnablePrinter, VehicleDataFilePath, PersonnelDataFilePath, LinkDataFilePath, IsNonValidateVehicle,
                                IsNonValidatePerson, IsNonValidateODOM, IsOtherRequire, OtherLabel, HUBType);

                        if (cd.isConnecting()) {

                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " Offline data Link,Vehicle,Pin Start ");

                                /*new GetAPILinkDetails().execute();

                                new GetAPIVehicleDetails().execute();

                                new GetAPIPersonnelPinDetails().execute();*/

                                new GenerateFilesAPI().execute();


                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!AppConstants.selectHosePressed) {
                                        AppConstants.clearSharedPrefByName(OffBackgroundService.this, "DownloadFileStatus");
                                        startDownloadTimerTask();
                                    } else {
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + " Offline download canceled.");
                                        stopSelf();
                                    }
                                }
                            }, 60000 * 3);


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

        try {

            EntityHub obj = controller.getOfflineHubDetails(OffBackgroundService.this);

            String VehicleDataFilePath = obj.VehicleDataFilePath;
            String PersonnelDataFilePath = obj.PersonnelDataFilePath;
            String LinkDataFilePath = obj.LinkDataFilePath;

            repeatedTask = new TimerTask() {
                public void run() {
                    if (AppConstants.selectHosePressed) {
                        repeatedTask.cancel();
                    }

                    System.out.println("startDownloadTimerTask**********");

                    String status_v = getDownloadFileStatus("Vehicle");

                    if ((status_v.isEmpty() || status_v.equalsIgnoreCase("2")) && !AppConstants.selectHosePressed) {
                        if (!VehicleDataFilePath.equalsIgnoreCase(""))
                            downloadLibrary(VehicleDataFilePath, "Vehicle");
                    }


                    String status_p = getDownloadFileStatus("Personnel");

                    if ((status_p.isEmpty() || status_p.equalsIgnoreCase("2")) && !AppConstants.selectHosePressed) {
                        if (!PersonnelDataFilePath.equalsIgnoreCase(""))
                            downloadLibrary(PersonnelDataFilePath, "Personnel");
                    }


                    String status_l = getDownloadFileStatus("Link");

                    if ((status_l.isEmpty() || status_l.equalsIgnoreCase("2")) && !AppConstants.selectHosePressed) {
                        if (!LinkDataFilePath.equalsIgnoreCase(""))
                            downloadLibrary(LinkDataFilePath, "Link");
                    }


                    if (status_v.equalsIgnoreCase("1") && status_p.equalsIgnoreCase("1") && status_l.equalsIgnoreCase("1") && !AppConstants.selectHosePressed) {

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class GetAPILinkDetails extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... param) {
            String resp = "";


            try {

                String api_token = controller.getOfflineToken(OffBackgroundService.this);
                String Email = CommonUtils.getCustomerDetailsCC(OffBackgroundService.this).PersonEmail;
                String IMEI = AppConstants.getIMEI(OffBackgroundService.this);

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(30, TimeUnit.SECONDS);
                client.setReadTimeout(30, TimeUnit.SECONDS);
                client.setWriteTimeout(30, TimeUnit.SECONDS);

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

                System.out.println("ResponseMessage:" + ResponceMessage);

                if (ResponceMessage.equalsIgnoreCase("success")) {

                    controller.deleteTableData(OffDBController.TBL_LINK);

                    JSONArray jsonArr = jsonObject.getJSONArray("LinkDataObj");

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " GetAPILinkDetails Json length = " + jsonArr.length());

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
                            String LinkCommunicationType = jsonObj.getString("HubLinkCommunication");//LinkCommunicationType
                            String APMacAddress = jsonObj.getString("APMacAddress");
                            String BluetoothMacAddress = jsonObj.getString("BluetoothMacAddress");

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

                            InsetLD = controller.insertLinkDetails(SiteId, WifiSSId, PumpOnTime, PumpOffTime, AuthorizedFuelingDays, Pulserratio, MacAddress, IsTLDCall,LinkCommunicationType,APMacAddress,BluetoothMacAddress);

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
                client.setConnectTimeout(30, TimeUnit.SECONDS);
                client.setReadTimeout(30, TimeUnit.SECONDS);
                client.setWriteTimeout(30, TimeUnit.SECONDS);

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

                System.out.println("ResponseMessage:" + ResponceMessage);

                if (ResponceMessage.equalsIgnoreCase("success")) {

                    controller.deleteTableData(OffDBController.TBL_VEHICLE);

                    JSONArray jsonArr = jsonObject.getJSONArray("VehicleDataObj");

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " GetAPIVehicleDetails Json length = " + jsonArr.length());

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
                            String MagneticCardReaderNumber = jsonObj.getString("MagneticCardReaderNumber");
                            String AllowedLinks = jsonObj.getString("AllowedLinks");
                            String Active = jsonObj.getString("Active");

                            String CheckOdometerReasonable = jsonObj.getString("CheckOdometerReasonable");
                            String OdometerReasonabilityConditions = jsonObj.getString("OdometerReasonabilityConditions");
                            String OdoLimit = jsonObj.getString("OdoLimit");
                            String HoursLimit = jsonObj.getString("HoursLimit");
                            String BarcodeNumber = jsonObj.getString("Barcode");
                            String IsExtraOther = jsonObj.getString("IsExtraOther");
                            String ExtraOtherLabel = jsonObj.getString("ExtraOtherLabel");
                            String CheckFuelLimitPerMonth = jsonObj.getString("CheckFuelLimitPerMonth");
                            String FuelLimitPerMonth = jsonObj.getString("FuelLimitPerMonth");
                            String FuelQuantityOfVehiclePerMonth = jsonObj.getString("FuelQuantityOfVehiclePerMonth");

                            InsertVD = controller.insertVehicleDetails(VehicleId, VehicleNumber, CurrentOdometer, CurrentHours, RequireOdometerEntry, RequireHours,
                                    FuelLimitPerTxn, FuelLimitPerDay, FOBNumber, AllowedLinks, Active, CheckOdometerReasonable, OdometerReasonabilityConditions,
                                    OdoLimit, HoursLimit, BarcodeNumber, IsExtraOther, ExtraOtherLabel, MagneticCardReaderNumber, CheckFuelLimitPerMonth, FuelLimitPerMonth,
                                    FuelQuantityOfVehiclePerMonth);

                            if (InsertVD == -1)
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " GetAPIVehicleDetails Something went wrong inserting VehicleDetails ");

                        }
                    }

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " Offline data Vehicle download process completed Successfully");

                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " GetAPIVehicleDetails InPost Response fail" + result);
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
                client.setConnectTimeout(30, TimeUnit.SECONDS);
                client.setReadTimeout(30, TimeUnit.SECONDS);
                client.setWriteTimeout(30, TimeUnit.SECONDS);

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

                System.out.println("ResponseMessage:" + ResponceMessage);

                if (ResponceMessage.equalsIgnoreCase("success")) {

                    controller.deleteTableData(OffDBController.TBL_PERSONNEL);

                    JSONArray jsonArr = jsonObject.getJSONArray("PersonDataObj");

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " GetAPIPersonnelDetails Json length = " + jsonArr.length());

                    if (jsonArr != null && jsonArr.length() > 0) {
                        for (int j = 0; j < jsonArr.length(); j++) {
                            JSONObject jsonObj = (JSONObject) jsonArr.get(j);

                            String PersonId = jsonObj.getString("PersonId");
                            String PinNumber = jsonObj.getString("PinNumber");
                            String FuelLimitPerTxn = jsonObj.getString("FuelLimitPerTxn");
                            String FuelLimitPerDay = jsonObj.getString("FuelLimitPerDay");
                            String FOBNumber = jsonObj.getString("FOBNumber");
                            String MagneticCardReaderNumber = jsonObj.getString("MagneticCardReaderNumber");
                            String Barcode = jsonObj.getString("Barcode");
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

                            InsertPD = controller.insertPersonnelPinDetails(PersonId, PinNumber, FuelLimitPerTxn, FuelLimitPerDay, FOBNumber, Authorizedlinks,
                                    AssignedVehicles, MagneticCardReaderNumber, Barcode);

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

    public boolean checkSharedPrefOfflineData() {

        SharedPreferences sharedPrefODO = getApplicationContext().getSharedPreferences("OfflineData", Context.MODE_PRIVATE);
        String last_date = sharedPrefODO.getString("last_date", "");
        String curr_date = AppConstants.currentDateFormat("dd/MM/yyyy");

        if (curr_date.trim().equalsIgnoreCase(last_date.trim())) {
            //Offline data already downloaded in last 24 hours therefor skip offline data downloading process
            Log.i(TAG, " Already downloaded Offline data on:" + last_date.trim() + " >>Skip Downloading.");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " Already downloaded Offline data on:" + last_date.trim() + " >>Skip Downloading.");
            return false;
        } else
            return true;

    }

    public boolean checkOfflineDataTime(int CurrentHour, int CurrentMinutes, int HourOfDay, int MinuteOfHour) {

        Date currentDate = parseTime(CurrentHour + ":" + CurrentMinutes);
        Date savedOfflineDate = parseTime(HourOfDay + ":" + MinuteOfHour);

        if (CurrentHour >= 0 && CurrentHour <= 3) { // Checking the time is in between 12 AM to 4 AM
            if (savedOfflineDate.equals(currentDate) || savedOfflineDate.before(currentDate)) { // checking offline access time is same or less than current time or not.
                return true;
            } else {
                Log.i(TAG, " Offline data download time is set as :" + (HourOfDay + ":" + MinuteOfHour).trim() + "; Current time is "+ (CurrentHour + ":" + CurrentMinutes).trim() + " >>Skip Downloading.");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Offline data download time is set as :" + (HourOfDay + ":" + MinuteOfHour).trim() + "; Current time is "+ (CurrentHour + ":" + CurrentMinutes).trim() + " >>Skip Downloading.");
                return false;
            }
        } else {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " Skip Downloading.");
            return false;
        }
    }

    private Date parseTime(String date) {

        try {
            return timeParser.parse(date);
        } catch (Exception e) {
            return new Date(0);
        }
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
        //Uri destinationUri = Uri.parse(Environment.getExternalStorageDirectory() + "/FSdata/" + fileName + ".txt");
        Uri destinationUri = Uri.parse(getApplicationContext().getExternalFilesDir(AppConstants.OfflineDataFolderName) + "/" + fileName + ".txt");
        DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
                //.addCustomHeader("Auth-Token", "YourTokenApiKey")
                .setRetryPolicy(new DefaultRetryPolicy())
                .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                .setDownloadResumable(true)
                //.setDownloadContext(downloadContextObject)//Optional
                .setStatusListener(new DownloadStatusListenerV1() {
                    @Override
                    public void onDownloadComplete(DownloadRequest downloadRequest) {
                        AppConstants.WriteinFile("download-Complete--" + fileName);

                        insertDownloadFileStatus(fileName, "1");

                        if (!AppConstants.selectHosePressed) {
                            readEncryptedFileParseJsonInSqlite(fileName);
                        }
                    }

                    @Override
                    public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {
                        AppConstants.WriteinFile("download-Failed--" + fileName + " " + errorCode + " " + errorMessage);

                        insertDownloadFileStatus(fileName, "2");

                        if (errorCode == 416) {
                            insertDownloadFileStatus(fileName, "1");

                            readEncryptedFileParseJsonInSqlite(fileName);
                        }
                    }

                    @Override
                    public void onProgress(DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {
                        insertDownloadFileStatus(fileName, "3");
                        //AppConstants.WriteinFile("download-onProgress--" + fileName + " " + totalBytes + " " + downloadedBytes + " " + progress);
                    }
                });
                /*.setDownloadListener(new DownloadStatusListener() {
                    @Override
                    public void onDownloadComplete(int id) {
                        AppConstants.WriteinFile("download-Complete--" + fileName);

                        insertDownloadFileStatus(fileName, "1");

                        if (!AppConstants.selectHosePressed)
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
                });*/


        int downloadId = downloadManager.add(downloadRequest);

        try {
            AppConstants.offlineDownloadIds.add(downloadId + "");
        } catch (Exception e) {
        }


    }

    public void readEncryptedFileParseJsonInSqlite(String file_name) {

        //File file = new File(Environment.getExternalStorageDirectory() + "/FSdata/" + file_name + ".txt");
        File file = new File(getApplicationContext().getExternalFilesDir(AppConstants.OfflineDataFolderName) + "/" + file_name + ".txt");

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
            //File dir = new File(Environment.getExternalStorageDirectory() + "/FSdata");
            File dir = new File(String.valueOf(getApplicationContext().getExternalFilesDir(AppConstants.OfflineDataFolderName)));
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

    public void cancelThinDownloadManager() {
        try {
            ThinDownloadManager downloadManager = new ThinDownloadManager();
            downloadManager.cancelAll();
            if (AppConstants.offlineDownloadIds != null && AppConstants.offlineDownloadIds.size() > 0) {
                AppConstants.offlineDownloadIds.clear();
            }
            deleteIncompleteOfflineDataFiles();
            //AppConstants.WriteinFile("WelAct- cancel offline Download...");

        } catch (Exception e) {
        }
    }

    public void deleteIncompleteOfflineDataFiles() {
        //File dir = new File(Environment.getExternalStorageDirectory() + "/FSdata");
        File dir = new File(String.valueOf(getApplicationContext().getExternalFilesDir(AppConstants.OfflineDataFolderName)));
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                System.out.println("Deleted file...." + children[i]);
                new File(dir, children[i]).delete();
            }
        }
    }

    public class GenerateFilesAPI extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... param) {
            String resp = "";

            try {

                String api_token = controller.getOfflineToken(OffBackgroundService.this);
                String Email = CommonUtils.getCustomerDetailsCC(OffBackgroundService.this).PersonEmail;
                String IMEI = AppConstants.getIMEI(OffBackgroundService.this);

                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(AppConstants.API_URL_GENERATEFILES + "?Email=" + Email + "&IMEI=" + IMEI)
                        .addHeader("Authorization", "bearer " + api_token)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

                //------------------------------

            } catch (Exception e) {

                System.out.println("Ex" + e.getMessage());
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " GenerateFilesAPI InBackG Ex:" + e.getMessage());

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


                        if (cd.isConnecting()) {

                            /*if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " Offline data Link,Vehicle,Pin Start ");

                            AppConstants.clearSharedPrefByName(OffBackgroundService.this, "DownloadFileStatus");

                            startDownloadTimerTask();*/

                        } else {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " GenerateFilesAPI InPost NoInternet");
                        }

                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " GenerateFilesAPI InPost Response fail" + result);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " GenerateFilesAPI InPost Ex:" + e.getMessage());
                }

            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " GenerateFilesAPI InPost Response err:" + result);
            }

        }
    }

}