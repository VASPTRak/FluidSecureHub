package com.TrakEngineering.FluidSecureHubTest;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.TrakEngineering.FluidSecureHubTest.enity.RenameHose;
import com.TrakEngineering.FluidSecureHubTest.enity.TankMonitorEntity;
import com.TrakEngineering.FluidSecureHubTest.enity.TrazComp;
import com.TrakEngineering.FluidSecureHubTest.enity.UpdateTransactionStatusClass;
import com.TrakEngineering.FluidSecureHubTest.enity.UpgradeVersionEntity;
import com.TrakEngineering.FluidSecureHubTest.server.ServerHandler;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import static com.TrakEngineering.FluidSecureHubTest.CommonUtils.GetPrintRecipt;
import static com.TrakEngineering.FluidSecureHubTest.CommonUtils.GetPrintReciptForOther;

/**
 * Created by VASP on 10/10/2017.
 */

public class BackgroundService_FS_UNIT_3 extends BackgroundService{


    private static final String TAG = "BackgroundService_FS_UNITE_3 :";
    String EMPTY_Val = "";
    private ConnectionDetector cd;

    //String HTTP_URL = "http://192.168.43.140:80/";//for pipe
    //String HTTP_URL = "http://192.168.43.5:80/";//Other FS
    String HTTP_URL = "";

    String URL_INFO = HTTP_URL + "client?command=info";
    String URL_STATUS = HTTP_URL + "client?command=status";
    String URL_RECORD = HTTP_URL + "client?command=record10";

    String URL_GET_PULSAR = HTTP_URL + "client?command=pulsar ";
    String URL_SET_PULSAR = HTTP_URL + "config?command=pulsar";

    String URL_WIFI = HTTP_URL + "config?command=wifi";
    String URL_RELAY = HTTP_URL + "config?command=relay";

    String URL_GET_USER = HTTP_URL + "upgrade?command=getuser";
    String URL_RESET = HTTP_URL + "upgrade?command=reset";
    String URL_FILE_UPLOAD = HTTP_URL + "upgrade?command=start";

    String URL_GET_TXNID = HTTP_URL + "client?command=lasttxtnid";
    String URL_SET_TXNID = HTTP_URL + "config?command=txtnid";

    String URL_UPGRADE_START = HTTP_URL + "upgrade?command=start";

    String URL_TDL_info = HTTP_URL + "tld?level=info";

    String jsonRename;
    String jsonConnectWifi = "{\"Request\":  {\"Station\":{\"Connect_Station\":{\"ssid\":\"tenda\",\"password\":\"1234567890\",\"token\":\"1234567890123456789012345678901234567890\"}}}}";
    String jsonRelayOn = "{\"relay_request\":{\"Password\":\"12345678\",\"Status\":1}}";
    String jsonRelayOff = "{\"relay_request\":{\"Password\":\"12345678\",\"Status\":0}}";

    String jsonPulsar = "{\"pulsar_request\":{\"counter_set\":1}}";
    String jsonPulsarOff = "{\"pulsar_request\":{\"counter_set\":0}}";

    ArrayList<HashMap<String, String>> quantityRecords = new ArrayList<>();
    ArrayList<Integer> respCounter = new ArrayList<>();

    SimpleDateFormat sdformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    private String vehicleNumber, odometerTenths = "0", dNumber = "", pNumber = "", oText = "", hNumber = "";
    String LinkName,OtherName,IsOtherRequire,OtherLabel,VehicleNumber,PrintDate,CompanyName,Location,PersonName,PrinterMacAddress,PrinterName,TransactionId,VehicleId, PhoneNumber, PersonId, PulseRatio, MinLimit, FuelTypeId, ServerDate, IntervalToStopFuel;

    public static String FOLDER_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FSBin/";
    public static String PATH_BIN_FILE1 = "user1.2048.new.5.bin";

    int timeFirst = 60;
    Timer tFirst;
    TimerTask taskFirst;
    boolean stopTimer = true;
    boolean pulsarConnected = false;
    double minFuelLimit = 0, numPulseRatio = 0;
    String consoleString = "", outputQuantity = "0";
    double CurrentLat = 0, CurrentLng = 0;
    GoogleApiClient mGoogleApiClient;
    long stopAutoFuelSeconds = 0;
    boolean isTransactionComp = false;
    double fillqty = 0;
    double Lastfillqty = 0;
    Integer Pulses = 0;
    long sqliteID = 0;
    ConnectivityManager connection_manager;
    String printReceipt = "";
    BluetoothPrinter BTprint = new BluetoothPrinter();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            super.onStart(intent, startId);
            Bundle extras = intent.getExtras();
            if (extras == null) {
                Log.d("Service", "null");
                this.stopSelf();
                Constants.FS_3STATUS = "FREE";
                clearEditTextFields();
                if (!Constants.BusyVehicleNumberList.equals(null))
                {
                    Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS3);
                }


            }

            else
            {
                Log.d("Service","not null");
                HTTP_URL = (String) extras.get("HTTP_URL");

                URL_INFO = HTTP_URL + "client?command=info";
                URL_STATUS = HTTP_URL + "client?command=status";
                URL_RECORD = HTTP_URL + "client?command=record10";

                URL_GET_PULSAR = HTTP_URL + "client?command=pulsar ";
                URL_SET_PULSAR = HTTP_URL + "config?command=pulsar";

                URL_WIFI = HTTP_URL + "config?command=wifi";
                URL_RELAY = HTTP_URL + "config?command=relay";

                URL_GET_USER = HTTP_URL + "upgrade?command=getuser";
                URL_RESET = HTTP_URL + "upgrade?command=reset";
                URL_FILE_UPLOAD = HTTP_URL + "upgrade?command=start";

                URL_GET_TXNID = HTTP_URL + "client?command=lasttxtnid";
                URL_SET_TXNID = HTTP_URL + "config?command=txtnid";

                URL_UPGRADE_START = HTTP_URL + "upgrade?command=start";

                URL_TDL_info = HTTP_URL + "tld?level=info";


                jsonConnectWifi = "{\"Request\":  {\"Station\":{\"Connect_Station\":{\"ssid\":\"tenda\",\"password\":\"1234567890\",\"token\":\"1234567890123456789012345678901234567890\"}}}}";
                jsonRelayOn = "{\"relay_request\":{\"Password\":\"12345678\",\"Status\":1}}";
                jsonRelayOff = "{\"relay_request\":{\"Password\":\"12345678\",\"Status\":0}}";

                jsonPulsar = "{\"pulsar_request\":{\"counter_set\":1}}";
                jsonPulsarOff = "{\"pulsar_request\":{\"counter_set\":0}}";

                jsonRename = "{\"Request\":{\"SoftAP\":{\"Connect_SoftAP\":{\"authmode\":\"WPAPSK/WPA2PSK\",\"channel\":6,\"ssid\":\"" + AppConstants.REPLACEBLE_WIFI_NAME_FS3 + "\",\"password\":\"123456789\"}}}}";

                System.out.println("BackgroundService is on. AP_FS_PIPE"+HTTP_URL);
                Constants.FS_3STATUS="BUSY";
                Constants.BusyVehicleNumberList.add(Constants.AccVehicleNumber_FS3);

                SharedPreferences sharedPref =  this.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
                TransactionId = sharedPref.getString("TransactionId_FS3", "");
                VehicleId = sharedPref.getString("VehicleId_FS3", "");
                PhoneNumber = sharedPref.getString("PhoneNumber_FS3", "");
                PersonId = sharedPref.getString("PersonId_FS3", "");
                PulseRatio = sharedPref.getString("PulseRatio_FS3", "1");
                MinLimit = sharedPref.getString("MinLimit_FS3", "0");
                FuelTypeId = sharedPref.getString("FuelTypeId_FS3", "");
                ServerDate = sharedPref.getString("ServerDate_FS3", "");
                IntervalToStopFuel = sharedPref.getString("IntervalToStopFuel_FS3", "0");

                LinkName = AppConstants.CURRENT_SELECTED_SSID;

                //settransactionID to FSUNIT
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        new CommandsPOST().execute(URL_SET_TXNID, "{\"txtnid\":" + TransactionId + "}");

                    }
                }, 1500);


                //Create and Empty transactiin into SQLite DB
                HashMap<String, String> mapsts = new HashMap<>();
                mapsts.put("transId", TransactionId);
                mapsts.put("transStatus", "1");

                controller.insertTransStatus(mapsts);
                ////////////////////////////////////////////
                String userEmail = CommonUtils.getCustomerDetails_backgroundService_FS3(BackgroundService_FS_UNIT_3.this).Email;
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_FS_UNIT_3.this) + ":" + userEmail + ":" + "TransactionComplete");

                HashMap<String, String> imap = new HashMap<>();
                imap.put("jsonData", "");
                imap.put("authString", authString);

                sqliteID = controller.insertTransactions(imap);

                //////////////////////////////////////////////////////////////

                //=====================UpgradeTransaction Status = 1=================
                cd = new ConnectionDetector(BackgroundService_FS_UNIT_3.this);
                if (cd.isConnectingToInternet()) {
                    try {
                        UpdateTransactionStatusClass authEntity = new UpdateTransactionStatusClass();
                        authEntity.TransactionId = TransactionId;
                        authEntity.Status = "1";
                        authEntity.IMEIUDID = AppConstants.getIMEI(BackgroundService_FS_UNIT_3.this);

                        BackgroundService_FS_UNIT_3.UpdateAsynTask authTestAsynTask = new BackgroundService_FS_UNIT_3.UpdateAsynTask(authEntity);
                        authTestAsynTask.execute();
                        authTestAsynTask.get();

                        String serverRes = authTestAsynTask.response;

                        if (serverRes != null) {
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                } else{

                    AppConstants.colorToast(BackgroundService_FS_UNIT_3.this, "Please check Internet Connection.", Color.RED);
                    UpdateTransactionStatusClass authEntity = new UpdateTransactionStatusClass();
                    authEntity.TransactionId = TransactionId;
                    authEntity.Status = "1";
                    authEntity.IMEIUDID = AppConstants.getIMEI(BackgroundService_FS_UNIT_3.this);


                    Gson gson1 = new Gson();
                    String jsonData1 = gson1.toJson(authEntity);

                    System.out.println("FS_UNIT_3 UpdatetransactionData......" + jsonData1);

                    String userEmail1 = CommonUtils.getCustomerDetails_backgroundService_FS3(this).PersonEmail;
                    String authString1 = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(this) + ":" + userEmail1 + ":" + "UpgradeTransactionStatus");

                    HashMap<String, String> imapStatus = new HashMap<>();
                    imapStatus.put("jsonData", jsonData1);
                    imapStatus.put("authString", authString1);

                    controller.insertIntoUpdateTranStatus(imapStatus);

                }


                //=========================UpgradeTransactionStatus Ends===============

                minFuelLimit = Double.parseDouble(MinLimit);

                numPulseRatio = Double.parseDouble(PulseRatio);

                stopAutoFuelSeconds = Long.parseLong(IntervalToStopFuel);


                System.out.println("iiiiii" + IntervalToStopFuel);
                System.out.println("minFuelLimit" + minFuelLimit);
                System.out.println("getDeviceName" + minFuelLimit);

                String mobDevName = AppConstants.getDeviceName().toLowerCase();
                System.out.println("oooooooooo" + mobDevName);


            }
        }catch (NullPointerException e){
            Log.d("Ex", e.getMessage());
            AppConstants.WriteinFile("BackgroundService_FS_UNIT_3 ~~~~~~~~~" + "onStartCommand Execption " + e);
            this.stopSelf();
        }



        //GetLatLng();
        new CommandsPOST().execute(URL_SET_PULSAR, jsonPulsarOff);
        //Relay On cmd
        new CommandsPOST().execute(URL_SET_PULSAR, jsonPulsar);//pulsar on swipe

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {


                new CommandsGET().execute(URL_RELAY);


                //  new CommandsPOST().execute(URL_SET_PULSAR, jsonPulsarOff);

            }
        }, 1000);

        //Pulsar On
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                new CommandsPOST().execute(URL_RELAY, jsonRelayOn);//Relay ON swipe


            }
        }, 2500);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startQuantityInterval();
            }
        }, 3000);

        // return super.onStartCommand(intent, flags, startId);
        return Service.START_NOT_STICKY;
    }



    @TargetApi(21)
    private void setGlobalWifiConnection() {

        NetworkRequest.Builder requestbuilder = new NetworkRequest.Builder();
        requestbuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);

        connection_manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);


        connection_manager.requestNetwork(requestbuilder.build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {


                System.out.println(" network......." + network);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    connection_manager.bindProcessToNetwork(network);

                }
            }
        });
    }

    public void GetLatLng() {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {

            CurrentLat = mLastLocation.getLatitude();
            CurrentLng = mLastLocation.getLongitude();

            System.out.println("CCCrrr" + CurrentLat);
            System.out.println("CCCrrr" + CurrentLng);

        }
    }

    public void stopFirstTimer(boolean flag) {
        if(flag) {
            tFirst.cancel();
            tFirst.purge();
        }
        else {
            tFirst.cancel();
            tFirst.purge();

            WelcomeActivity.SelectedItemPos = -1;
            AppConstants.BUSY_STATUS=true;

            Intent i = new Intent(this, WelcomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
    }

    public class CommandsPOST extends AsyncTask<String, Void, String> {

        public String resp = "";



        protected String doInBackground(String... param) {


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
                AppConstants.WriteinFile("BackgroundService_FS_UNIT_3 ~~~~~~~~~" + "CommandsPOST doInBackground Execption " + e);
                stopSelf();
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {


            try {

                System.out.println("APFS_3 OUTPUT"+result);

            } catch (Exception e) {
                AppConstants.WriteinFile("BackgroundService_FS_UNIT_3 ~~~~~~~~~" + "CommandsPOST OnPostExecution Execption " + e);
                System.out.println(e);
                stopSelf();
            }

        }
    }

    public class CommandsGET extends AsyncTask<String, Void, String> {

        public String resp = "";

        protected String doInBackground(String... param) {


            try {

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
                AppConstants.WriteinFile("BackgroundService_FS_UNIT_3 ~~~~~~~~~" + "CommandsGET doInBackground Execption " + e);
                stopSelf();
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            try {

                System.out.println("APFS_3 OUTPUT"+result);

            } catch (Exception e) {

                AppConstants.WriteinFile("BackgroundService_FS_UNIT_3 ~~~~~~~~~" + "CommandsGET OnPostExecution Execption " + e);
                System.out.println(e);
                stopSelf();
            }

        }
    }

    public void startQuantityInterval() {


        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                try {

                    if (stopTimer) {

                        /*
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                            setHttpTransportWifi(URL_GET_PULSAR, EMPTY_Val);

                        } else {

                        }
                        */

                        new GETPulsarQuantity().execute(URL_GET_PULSAR);

                    }

                } catch (Exception e) {
                    System.out.println(e);
                    AppConstants.WriteinFile("BackgroundService_FS_UNIT_3 ~~~~~~~~~" + "startQuantityInterval Execption " + e);
                }

            }
        }, 0, 2000);


    }

    public class GETPulsarQuantity extends AsyncTask<String, Void, String> {

        public String resp = "";


        protected String doInBackground(String... param) {


            try {

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                AppConstants.WriteinFile("BackgroundService_FS_UNIT_3 ~~~~~~~~~" + "GETPulsarQuantity doInBackground Execption " + e);
                Log.d("Ex", e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {


            try {

                if (result.equalsIgnoreCase("")) {
                    respCounter.add(0);
                    System.out.println("FR:0");
                } else {
                    respCounter.add(1);
                    System.out.println("FR:1");
                }

                if (getPulsarResponseEmptyFor3times()) {
                    // btnStop.performClick();
                    stopButtonFunctionality();

                } else {

                    System.out.println("OUTPUT"+result);

                    if (stopTimer)
                        pulsarQtyLogic(result);
                }


            } catch (Exception e) {
                AppConstants.WriteinFile("BackgroundService_FS_UNIT_3 ~~~~~~~~~" + "GETPulsarQuantity onPostExecute Execption " + e);
                System.out.println(e);
            }

        }
    }

    public void pulsarQtyLogic(String result) {

        int secure_status = 0;

        try {
            if (result.contains("4")) {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject joPulsarStat = jsonObject.getJSONObject("pulsar_status");
                String counts = joPulsarStat.getString("counts");
                String pulsar_status = joPulsarStat.getString("pulsar_status");
                String pulsar_secure_status = joPulsarStat.getString("pulsar_secure_status");


                if (pulsar_status.trim().equalsIgnoreCase("1")) {
                    pulsarConnected = true;
                } else if (pulsar_status.trim().equalsIgnoreCase("0")) {

                    pulsarConnected = false;
                    if (!pulsarConnected) {

                        this.stopSelf();

                        if (!Constants.BusyVehicleNumberList.equals(null))
                        {
                            Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS3);
                        }
                        System.out.println("APFS_3 Auto Stop! Pulsar disconnected");
                        // AppConstants.colorToastBigFont(this, AppConstants.FS1_CONNECTED_SSID+" Auto Stop!\n\nPulsar disconnected", Color.BLUE);
                        stopButtonFunctionality();
                    }
                }


                convertCountToQuantity(counts);


                if (!pulsar_secure_status.trim().isEmpty()) {
                    secure_status = Integer.parseInt(pulsar_secure_status);

                    if (secure_status == 0) {
                        //linearTimer.setVisibility(View.GONE);
                        //tvCountDownTimer.setText("-");

                    } else if (secure_status == 1) {
                        //linearTimer.setVisibility(View.VISIBLE);
                        //tvCountDownTimer.setText("5");

                    } else if (secure_status == 2) {
                        //linearTimer.setVisibility(View.VISIBLE);
                        //tvCountDownTimer.setText("4");

                    } else if (secure_status == 3) {
                        //linearTimer.setVisibility(View.VISIBLE);
                        //tvCountDownTimer.setText("3");

                    } else if (secure_status == 4) {
                        //linearTimer.setVisibility(View.VISIBLE);
                        //tvCountDownTimer.setText("2");

                    } else if (secure_status >= 5) {
                        //linearTimer.setVisibility(View.GONE);
                        //tvCountDownTimer.setText("1");

                        this.stopSelf();

                        if (!Constants.BusyVehicleNumberList.equals(null))
                        {
                            Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS3);
                        }

                        System.out.println("APFS_PIPE Auto Stop! Count down timer completed");
                        AppConstants.colorToastBigFont(this, AppConstants.FS3_CONNECTED_SSID+" Auto Stop!\n\nCount down timer completed.", Color.BLUE);
                        stopButtonFunctionality();
                    }

                }

            }
            Date currDT = new Date();
            String strCurDT = sdformat.format(currDT);

            HashMap<String, String> hmap = new HashMap<>();
            hmap.put("a", outputQuantity);
            hmap.put("b", strCurDT);
            quantityRecords.add(hmap);

            //if quantity same for some interval
            secondsTimeLogic(strCurDT);


            //if quantity reach max limit
            if (!outputQuantity.trim().isEmpty()) {
                try {


                    if (minFuelLimit > 0) {
                        if (fillqty >= minFuelLimit) {

                            this.stopSelf();
                            if (!Constants.BusyVehicleNumberList.equals(null))
                            {
                                Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS3);
                            }
                            System.out.println("APFS_PIPE Auto Stop!You reached MAX fuel limit.");
                            //AppConstants.colorToastBigFont(this, "Auto Stop!\n\nYou reached MAX fuel limit.", Color.BLUE);
                            stopButtonFunctionality();
                        }
                    }
                } catch (Exception e) {
                    AppConstants.WriteinFile("BackgroundService_FS_UNIT_3 ~~~~~~~~~" + "quantity reach max limit1 Execption " + e);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            AppConstants.WriteinFile("BackgroundService_FS_UNIT_3 ~~~~~~~~~" + "quantity reach max limit2 Execption " + e);
        }
    }

    public void stopButtonFunctionality() {


        quantityRecords.clear();

        // btnStart.setVisibility(View.GONE);
        //btnStop.setVisibility(View.GONE);
        //btnFuelHistory.setVisibility(View.VISIBLE);
        consoleString = "";
        // tvConsole.setText("");

        //it stops pulsar logic------
        stopTimer = false;


        new CommandsPOST().execute(URL_RELAY, jsonRelayOff);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    String cntA = "0", cntB = "0", cntC = "0";

                    for (int i = 0; i < 3; i++) {

                        String result = new GETFINALPulsar().execute(URL_GET_PULSAR).get();


                        if (result.contains("pulsar_status")) {

                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject joPulsarStat = jsonObject.getJSONObject("pulsar_status");
                            String counts = joPulsarStat.getString("counts");
                            //String pulsar_status = joPulsarStat.getString("pulsar_status");
                            //String pulsar_secure_status = joPulsarStat.getString("pulsar_secure_status");

                            convertCountToQuantity(counts);

                            /*
                            if (i == 0)
                                cntA = counts;
                            else if (i == 1)
                                cntB = counts;
                            else
                                cntC = counts;
                            */


                            if (i == 2) {

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finalLastStep();
                                    }
                                }, 1000);


                            }


                        }
                    }
                } catch (Exception e) {
                    AppConstants.WriteinFile("BackgroundService_FS_UNIT_3 ~~~~~~~~~" + "stopButtonFunctionality Execption " + e);
                    System.out.println(e);
                }
            }
        }, 1000);


    }

    public void finalLastStep() {


        new CommandsPOST().execute(URL_SET_PULSAR, jsonPulsarOff);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (AppConstants.NeedToRenameFS3) {

                    consoleString += "RENAME:\n" + jsonRename;

                    new CommandsPOST().execute(URL_WIFI, jsonRename);

                }
            }

        }, 2500);

        long secondsTime = 3000;

        if (AppConstants.NeedToRenameFS3) {
            secondsTime = 5000;
        }

        if (AppConstants.UP_Upgrade_fs3) {


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    new BackgroundService_FS_UNIT_3.CommandsPOST().execute(URL_UPGRADE_START, "");

                    //upgrade bin
                    String LocalPath = FOLDER_PATH + PATH_BIN_FILE1;

                    File f = new File(LocalPath);

                    if (f.exists()) {

                      new BackgroundService_FS_UNIT_3.OkHttpFileUpload().execute(LocalPath, "application/binary");

                    } else {
                        Toast.makeText(getApplicationContext(), "File Not found " + LocalPath, Toast.LENGTH_LONG).show();
                    }


                }

            }, 3000);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Constants.FS_3STATUS = "FREE";
                clearEditTextFields();
                //AppConstants.disconnectWiFi(DisplayMeterActivity.this);
                GetDetails();

                if (!AppConstants.UP_Upgrade_fs3) {
                    TransactionCompleteFunction();
                }
            }

        }, secondsTime);
    }

    public class GETFINALPulsar extends AsyncTask<String, Void, String> {

        public String resp = "";


        protected String doInBackground(String... param) {


            try {

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                AppConstants.WriteinFile("BackgroundService_FS_UNIT_3 ~~~~~~~~~" + "GETFINALPulsar doInBackground Execption " + e);
                Log.d("Ex", e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {


            try {

                consoleString += "OUTPUT- " + result + "\n";

                // tvConsole.setText(consoleString);

                System.out.println("APFS_3 OUTPUT"+result);


            } catch (Exception e) {
                AppConstants.WriteinFile("BackgroundService_FS_UNIT_3 ~~~~~~~~~" + "GETFINALPulsar onPostExecute Execption " + e);
                System.out.println(e);
            }

        }
    }

    public void secondsTimeLogic(String currentDT) {

        try {


            if (quantityRecords.size() > 0) {

                Date nowDT = sdformat.parse(currentDT);
                Date d2 = sdformat.parse(quantityRecords.get(0).get("b"));

                long seconds = (nowDT.getTime() - d2.getTime()) / 1000;


                if (stopAutoFuelSeconds > 0) {

                    if (seconds >= stopAutoFuelSeconds) {

                        if (qtyFrequencyCount()) {

                            //qty is same for some time
                            System.out.println("APFS_3 Auto Stop!Quantity is same for last");
                            //AppConstants.colorToastBigFont(this, "Auto Stop!\n\nQuantity is same for last " + stopAutoFuelSeconds + " seconds.", Color.BLUE);
                            stopButtonFunctionality();
                            stopTimer = false;
                            this.stopSelf();
                            Constants.FS_3STATUS = "FREE";
                            clearEditTextFields();
                            if (!Constants.BusyVehicleNumberList.equals(null))
                            {
                                Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS3);
                            }


                        } else {
                            quantityRecords.remove(0);
                            System.out.println("0 th pos deleted");
                            System.out.println("seconds--" + seconds);
                        }
                    }
                }

            }
        } catch (Exception e) {
            AppConstants.WriteinFile("BackgroundService_FS_UNIT_3 ~~~~~~~~~" + "secondsTimeLogic Execption " + e);
        }
    }

    public boolean qtyFrequencyCount() {


        if (quantityRecords.size() > 0) {

            ArrayList<String> data = new ArrayList<>();

            for (HashMap<String, String> hm : quantityRecords) {
                data.add(hm.get("a"));
            }

            System.out.println("\n Count all with frequency");
            Set<String> uniqueSet = new HashSet<String>(data);

            System.out.println("size--" + uniqueSet.size());

            /*for (String temp : uniqueSet) {
                System.out.println(temp + ": " + Collections.frequency(data, temp));
            }*/

            if (uniqueSet.size() == 1) {  //Autostop unique records
                return true;
            }
        }

        return false;
    }

    public void convertCountToQuantity(String counts) {
        outputQuantity = counts;

        Pulses = Integer.parseInt(outputQuantity);
        fillqty = Double.parseDouble(outputQuantity);
        fillqty = fillqty / numPulseRatio;//convert to gallons

        fillqty = AppConstants.roundNumber(fillqty, 2);

        System.out.println("APFS_3 Pulse"+outputQuantity);
        System.out.println("APFS_3 Quantity"+ (fillqty));
        DecimalFormat precision = new DecimalFormat("0.00");
        Constants.FS_3Gallons =  (precision.format(fillqty));
        Constants.FS_3Pulse  = outputQuantity;

        ////////////////////////////////////-Update transaction ---
        TrazComp authEntityClass = new TrazComp();
        authEntityClass.TransactionId = TransactionId;
        authEntityClass.FuelQuantity = fillqty;
        authEntityClass.AppInfo = " Version:" + CommonUtils.getVersionCode(BackgroundService_FS_UNIT_3.this) + " " + AppConstants.getDeviceName() + " Android " + android.os.Build.VERSION.RELEASE + " "+"--Main Transaction--";
        authEntityClass.TransactionFrom = "A";
        authEntityClass.Pulses = Integer.parseInt(counts);

        Gson gson = new Gson();
        String jsonData = gson.toJson(authEntityClass);

        AppConstants.WriteinFile("BackgroundService_FS_UNIT_3 ~~~~~~~~~" + "InConvertCountToQuantity jsonData " + jsonData);

        String userEmail = CommonUtils.getCustomerDetails_backgroundService_FS3(BackgroundService_FS_UNIT_3.this).Email;
        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_FS_UNIT_3.this) + ":" + userEmail + ":" + "TransactionComplete");


        HashMap<String, String> imap = new HashMap<>();
        imap.put("jsonData", jsonData);
        imap.put("authString", authString);
        imap.put("sqliteId", sqliteID + "");

        if (fillqty > 0) {

                int rowseffected = controller.updateTransactions(imap);
                System.out.println("rowseffected-" + rowseffected);
                if (rowseffected == 0 ){

                    controller.insertTransactions(imap);
                }

                controller.deleteTransStatusByTransID(TransactionId);


        }


    }
    public void GetDetails()
    {
        vehicleNumber = Constants.AccVehicleNumber_FS3;
        odometerTenths = Constants.AccOdoMeter_FS3 + "";
        dNumber = Constants.AccDepartmentNumber_FS3;
        pNumber = Constants.AccPersonnelPIN_FS3;
        oText = Constants.AccOther_FS3;
        hNumber = Constants.AccHours_FS3 + "";


        if (dNumber != null) {
        } else {
            dNumber = "";
        }

        if (pNumber != null) {
        } else {
            pNumber = "";
        }

        if (oText != null) {
        } else {
            oText = "";
        }
    }

    public void TransactionCompleteFunction() {

        TankMonitorReading(); //Get Tank Monitor Reading and save it to server

        ////////////////////--UpgradeCurrentVersion to server--///////////////////////////////////////////////////////

        SharedPreferences myPrefUP = this.getSharedPreferences(Constants.PREF_FS_UPGRADE, 0);
        String hoseid = myPrefUP.getString("hoseid_fs3", "");
        String fsversion = myPrefUP.getString("fsversion_fs3", "");

        UpgradeVersionEntity objEntityClass = new UpgradeVersionEntity();
        objEntityClass.IMEIUDID = AppConstants.getIMEI(BackgroundService_FS_UNIT_3.this);
        objEntityClass.Email = CommonUtils.getCustomerDetails_backgroundService_FS3(this).Email;
        objEntityClass.HoseId = hoseid;
        objEntityClass.Version = fsversion;

        if (hoseid != null && !hoseid.trim().isEmpty()) {
            BackgroundService_FS_UNIT_3.UpgradeCurrentVersionWithUgradableVersion objUP = new BackgroundService_FS_UNIT_3.UpgradeCurrentVersionWithUgradableVersion(objEntityClass);
            objUP.execute();
            System.out.println(objUP.response);

            try {
                JSONObject jsonObject = new JSONObject(objUP.response);
                String ResponceMessage = jsonObject.getString("ResponceMessage");
                String ResponceText = jsonObject.getString("ResponceText");

                if (ResponceMessage.equalsIgnoreCase("success")) {

                    // AppConstants.clearSharedPrefByName(BackgroundService_AP.this, Constants.PREF_FS_UPGRADE);
                }

            } catch (Exception e) {

            }
        }

        /////////////////////////////////////////////////////////////////////////



        SharedPreferences sharedPrefODO = this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
        OtherLabel = sharedPrefODO.getString(AppConstants.OtherLabel, "Other");

        SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
        TransactionId = sharedPref.getString("TransactionId_FS3", "");
        VehicleId = sharedPref.getString("VehicleId_FS3", "");
        PhoneNumber = sharedPref.getString("PhoneNumber_FS3", "");
        PersonId = sharedPref.getString("PersonId_FS3", "");
        PulseRatio = sharedPref.getString("PulseRatio_FS3", "1");
        MinLimit = sharedPref.getString("MinLimit_FS3", "0");
        FuelTypeId = sharedPref.getString("FuelTypeId_FS3", "");
        ServerDate = sharedPref.getString("ServerDate_FS3", "");
        IntervalToStopFuel = sharedPref.getString("IntervalToStopFuel_FS3", "0");

        PrintDate = sharedPref.getString("PrintDate_FS3", "");
        CompanyName = sharedPref.getString("Company_FS3", "");
        Location = sharedPref.getString("Location_FS3", "");
        PersonName = sharedPref.getString("PersonName_FS3", "");
        PrinterMacAddress = sharedPref.getString("PrinterMacAddress_FS3", "");
        PrinterName = sharedPref.getString("PrinterName_FS3", "");
        VehicleNumber = sharedPref.getString("vehicleNumber_FS3", "");
        OtherName = sharedPref.getString("accOther_FS3", "");

        double VehicleSum_FS3 = Double.parseDouble(sharedPref.getString("VehicleSum_FS3", ""));
        double DeptSum_FS3 = Double.parseDouble(sharedPref.getString("DeptSum_FS3", ""));
        double VehPercentage_FS3 = Double.parseDouble(sharedPref.getString("VehPercentage_FS3", ""));
        double DeptPercentage_FS3 = Double.parseDouble(sharedPref.getString("DeptPercentage_FS3", ""));
        String SurchargeType_FS3 = sharedPref.getString("SurchargeType_FS3", "");
        double ProductPrice_FS3 = Double.parseDouble(sharedPref.getString("ProductPrice_FS3", ""));

        //Print Transaction Receipt
        DecimalFormat precision = new DecimalFormat("0.00");
        String Qty = (precision.format(fillqty));

        double FuelQuantity = Double.parseDouble(Qty);
        String PrintCost = CalculatePrice( SurchargeType_FS3, FuelQuantity, ProductPrice_FS3, VehicleSum_FS3, DeptSum_FS3, VehPercentage_FS3, DeptPercentage_FS3);

        if (IsOtherRequire.equalsIgnoreCase("true")){

            printReceipt = GetPrintReciptForOther(CompanyName,PrintDate,LinkName,Location,VehicleNumber,PersonName,OtherLabel,OtherName,Qty,PrintCost);
            // printReceipt = " \n\n------FluidSecure Receipt------ \n\nCompany   : " + CompanyName +"\n\nTime/Date : "+PrintDate+"\n\nLocation  : "+LinkName+","+Location+","+"\n\nVehicle # : "+VehicleNumber+"\n\nPersonnel : "+PersonName+" \n\nQty       : " + Qty + "\n\n"+OtherLabel+":"+OtherName+ "\n\n ---------Thank You---------"+"\n\n\n\n\n\n\n\n\n\n\n\n";
        }else{
            printReceipt = GetPrintRecipt(CompanyName,PrintDate,LinkName,Location,VehicleNumber,PersonName,Qty,PrintCost);
            // printReceipt = " \n\n------FluidSecure Receipt------ \n\nCompany   : " + CompanyName +"\n\nTime/Date : "+PrintDate+"\n\nLocation  : "+LinkName+","+Location+"\n\nVehicle # : "+VehicleNumber+"\n\nPersonnel : "+PersonName+" \n\nQty       : " + Qty + "\n\n ---------Thank You---------"+"\n\n\n\n\n\n\n\n\n\n\n\n";
        }

        try {
            new  SetBTConnectionPrinter().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {

            TrazComp authEntityClass = new TrazComp();
            authEntityClass.TransactionId = TransactionId;
            authEntityClass.FuelQuantity = fillqty;
            authEntityClass.Pulses = Pulses;
            authEntityClass.TransactionFrom = "A";
            authEntityClass.AppInfo = " Version:" + CommonUtils.getVersionCode(BackgroundService_FS_UNIT_3.this) + " " + AppConstants.getDeviceName() + " Android "+android.os.Build.VERSION.RELEASE+" ";

            /*authEntityClass.PersonId = PersonId;
            authEntityClass.SiteId = AcceptVehicleActivity.SITE_ID;
            authEntityClass.VehicleId = VehicleId;
            authEntityClass.CurrentOdometer = odometerTenths;
            authEntityClass.FuelTypeId = FuelTypeId;
            authEntityClass.PhoneNumber = PhoneNumber;
            authEntityClass.WifiSSId = AppConstants.FS1_CONNECTED_SSID;//AppConstants.LAST_CONNECTED_SSID;
            authEntityClass.TransactionDate = ServerDate;
            authEntityClass.CurrentLat = "" + CurrentLat;
            authEntityClass.CurrentLng = "" + CurrentLng;
            authEntityClass.VehicleNumber = vehicleNumber;
            authEntityClass.DepartmentNumber = dNumber;
            authEntityClass.PersonnelPIN = pNumber;
            authEntityClass.Other = oText;
            authEntityClass.Hours = hNumber;*/

            Gson gson = new Gson();
            String jsonData = gson.toJson(authEntityClass);

            AppConstants.WriteinFile("BackgroundService_FS_UNIT_3 ~~~~~~~~~" + "InTransactionComplete jsonData " + jsonData);
            System.out.println("AP_FS_3 TrazComp......" + jsonData);

            String userEmail = CommonUtils.getCustomerDetails_backgroundService_FS3(this).Email;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(this) + ":" + userEmail + ":" + "TransactionComplete");

            HashMap<String, String> imap = new HashMap<>();
            imap.put("jsonData", jsonData);
            imap.put("authString", authString);

            boolean isInsert = true;
            ArrayList<HashMap<String, String>> alltranz = controller.getAllTransaction();
            if (alltranz != null && alltranz.size() > 0) {

                for (int i = 0; i < alltranz.size(); i++) {

                    if (jsonData.equalsIgnoreCase(alltranz.get(i).get("jsonData")) && authString.equalsIgnoreCase(alltranz.get(i).get("authString"))) {
                        isInsert = false;
                        break;
                    }
                }
            }


            if (isInsert && fillqty > 0) {
                // controller.insertTransactions(imap);
            }

          /*  //settransaction to FSUNIT
            //==========================

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    new CommandsPOST().execute(URL_SET_TXNID, "{\"txtnid\":" + TransactionId + "}");

                    //new CommandsPOST().execute(URL_RELAY, jsonRelayOn);
                }
            }, 1500);

            //==========================*/
            clearEditTextFields();


        } catch (Exception ex) {

            CommonUtils.LogMessage("APFS_3", "AuthTestAsyncTask ", ex);
        }


        isTransactionComp = true;

        AppConstants.BUSY_STATUS = true;


        //btnStop.setVisibility(View.GONE);
        consoleString = "";
        //tvConsole.setText("");


        if (AppConstants.NeedToRename) {
            String userEmail = CommonUtils.getCustomerDetails_backgroundService_FS3(this).Email;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(this) + ":" + userEmail + ":" + "SetHoseNameReplacedFlag");

            RenameHose rhose = new RenameHose();
            rhose.SiteId = AppConstants.R_SITE_ID;
            rhose.HoseId = AppConstants.R_HOSE_ID;
            rhose.IsHoseNameReplaced = "Y";

            Gson gson = new Gson();
            String jsonData = gson.toJson(rhose);

            storeIsRenameFlag(this, AppConstants.NeedToRename, jsonData, authString);

        }


        startService(new Intent(this, BackgroundService.class));

        //linearFuelAnother.setVisibility(View.VISIBLE);

    }

    public void TankMonitorReading() {

        String mac_address = "";
        String probe_reading = "";
        String probe_temperature = "";

        try {

            SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
            TransactionId = sharedPref.getString("TransactionId_FS1", "");

            ServerDate = sharedPref.getString("ServerDate_FS1", "");
            PrintDate = sharedPref.getString("PrintDate_FS1", "");

            //Get TankMonitoring details from FluidSecure Link
            String response1 = new CommandsGET().execute(URL_TDL_info).get();
           // String response1 = "{  \"tld\":{ \"level\":\"180, 212, 11, 34, 110, 175, 1, 47, 231, 15, 78, 65\"  }  }";

            try {
                JSONObject reader = null;
                reader = new JSONObject(response1);

                JSONObject tld = reader.getJSONObject("tld");
                String level = tld.getString("level");

                mac_address = GetMacAddressOfProbe(level);

                //Calculate probe reading
                probe_reading = GetProbeReading(level);
                probe_temperature = CalculateTemperature(level);


            } catch (JSONException e) {
                e.printStackTrace();
                AppConstants.WriteinFile(TAG + "TankMonitorReading ~~~JSONException~~" + e);
            }

            //-----------------------------------------------------------

            TankMonitorEntity obj_entity = new TankMonitorEntity();
            obj_entity.IMEI_UDID = AppConstants.getIMEI(BackgroundService_FS_UNIT_3.this);
            obj_entity.FromSiteId = Integer.parseInt(AppConstants.SITE_ID);
            obj_entity.ProbeReading = probe_reading;
            obj_entity.TLD = mac_address;
            obj_entity.ReadingDateTime = PrintDate;

            BackgroundService_FS_UNIT_3.SaveTankMonitorReadingy TestAsynTask = new BackgroundService_FS_UNIT_3.SaveTankMonitorReadingy(obj_entity);
            TestAsynTask.execute();
            TestAsynTask.get();

            String serverRes = TestAsynTask.response;

            AppConstants.WriteinFile(TAG + "TankMonitorReading ~~~serverRes~~" + serverRes);




        } catch (Exception e) {
            e.printStackTrace();
            AppConstants.WriteinFile(TAG + "TankMonitorReading ~~~Execption~~" + e);
        }


    }

    public void storeIsRenameFlag(Context context, boolean flag, String jsonData, String authString) {
        SharedPreferences pref;

        SharedPreferences.Editor editor;
        pref = context.getSharedPreferences("storeIsRenameFlag", 0);
        editor = pref.edit();


        // Storing
        editor.putBoolean("flag", flag);
        editor.putString("jsonData", jsonData);
        editor.putString("authString", authString);

        // commit changes
        editor.commit();


    }

    public class UpdateAsynTask extends AsyncTask<Void, Void, Void> {

        UpdateTransactionStatusClass authEntity = null;


        public String response = null;

        public UpdateAsynTask(UpdateTransactionStatusClass authEntity) {
            this.authEntity = authEntity;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                ServerHandler serverHandler = new ServerHandler();

                Gson gson = new Gson();
                String jsonData = gson.toJson(authEntity);
                String userEmail = CommonUtils.getCustomerDetails_backgroundService_FS3(BackgroundService_FS_UNIT_3.this).PersonEmail;


                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(authEntity.IMEIUDID + ":" + userEmail + ":" + "UpgradeTransactionStatus");
                response = serverHandler.PostTextData(BackgroundService_FS_UNIT_3.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {

                CommonUtils.LogMessage("", "UpgradeTransactionStatus ", ex);
            }
            return null;
        }

    }

    public class SetBTConnectionPrinter extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {


            try {

                BTprint.findBT();
                BTprint.openBT();

                System.out.println("printer. FindBT and OpenBT");
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            try {
                BTprint.sendData(printReceipt);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            BTprint.closeBT();
                        }catch (Exception e){

                        }
                    }
                },2000);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void clearEditTextFields(){

        Constants.AccVehicleNumber_FS3 = "";
        Constants.AccOdoMeter_FS3 = 0;
        Constants.AccDepartmentNumber_FS3 = "";
        Constants.AccPersonnelPIN_FS3 = "";
        Constants.AccOther_FS3 = "";
        Constants.AccHours_FS3 = 0;

    }


    public class OkHttpFileUpload extends AsyncTask<String, Void, String> {

        public String resp = "";

        //ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            /*
            pd = new ProgressDialog(DisplayMeterActivity.this);
            pd.setMessage("Upgrading FS unit...\nIt takes a minute.");
            pd.setCancelable(false);
            pd.show();
            */


        }

        protected String doInBackground(String... param) {


            try {
                String LocalPath = param[0];
                String Localcontenttype = param[1];

                MediaType contentype = MediaType.parse(Localcontenttype);

                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(contentype, readBytesFromFile(LocalPath));

                Request request = new Request.Builder()
                        .url(HTTP_URL)
                        .post(body)
                        .build();


                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                return response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println(" resp......." + result);


            // pd.dismiss();
            try {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new BackgroundService_FS_UNIT_3.CommandsPOST().execute(URL_RESET, "");

                        System.out.println("AFTER SECONDS 5");
                    }
                }, 5000);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AppConstants.WriteinFile("BackgroundService_AP~~~~~~~~~" + "SAVE TRANS locally");
                        TransactionCompleteFunction();

                        System.out.println("AFTER SECONDS 15");
                    }
                }, 3000);


      /*          new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            String resultinfo = new BackgroundService_FS_UNIT_3.CommandsGET().execute(URL_INFO).get();

                            if (resultinfo.trim().startsWith("{") && resultinfo.trim().contains("Version")) {

                                JSONObject jsonObj = new JSONObject(resultinfo);
                                String userData = jsonObj.getString("Version");
                                JSONObject jsonObject = new JSONObject(userData);
                                String sdk_version = jsonObject.getString("sdk_version");
                                String iot_version = jsonObject.getString("iot_version");
                                String mac_address = jsonObject.getString("mac_address");

                                storeUpgradeFSVersion(BackgroundService_FS_UNIT_3.this, AppConstants.UP_HoseId_fs3, iot_version);


                            } else {

                            }

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    AppConstants.WriteinFile("BackgroundService_FS_UNIT_3~~~~~~~~~" + "SAVE TRANS locally");
                                    TransactionCompleteFunction();

                                    System.out.println("AFTER SECONDS 15");
                                }
                            }, 3000);


                        } catch (Exception e) {
                            System.out.println(e);
                        }

                        System.out.println("AFTER SECONDS 12");

                    }
                }, 12000);*/


            } catch (Exception e) {

                System.out.println(e);
            }

        }
    }

    public void storeUpgradeFSVersion(BackgroundService_FS_UNIT_3 activity, String hoseid, String fsversion) {

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.PREF_FS_UPGRADE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("hoseid", hoseid);
        editor.putString("fsversion", fsversion);

        editor.commit();
    }


    public boolean getPulsarResponseEmptyFor3times() {

        boolean flag = false;

        if (respCounter.size() > 3) {
            for (int i = 0; i < respCounter.size() - 2; i++) {

                int r1 = respCounter.get(i);
                int r2 = respCounter.get(i + 1);
                int r3 = respCounter.get(i + 2);
                System.out.println(r1);
                System.out.println(r2);
                System.out.println(r3);
                System.out.println("respCounter----------");

                if (r1 == 0 && r1 == r2 && r2 == r3)
                    flag = true;
            }
        }
        return flag;
    }

    private static byte[] readBytesFromFile(String filePath) {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {

            File file = new File(filePath);
            bytesArray = new byte[(int) file.length()];

            //read file into bytes[]
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return bytesArray;

    }

    public class UpgradeCurrentVersionWithUgradableVersion extends AsyncTask<Void, Void, Void> {


        UpgradeVersionEntity objupgrade;
        public String response = null;

        public UpgradeCurrentVersionWithUgradableVersion(UpgradeVersionEntity objupgrade) {

            this.objupgrade = objupgrade;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                ServerHandler serverHandler = new ServerHandler();

                Gson gson = new Gson();
                String jsonData = gson.toJson(objupgrade);


                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(objupgrade.IMEIUDID + ":" + objupgrade.Email + ":" + "UpgradeCurrentVersionWithUgradableVersion");
                response = serverHandler.PostTextData(BackgroundService_FS_UNIT_3.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {

                CommonUtils.LogMessage("BS", "UpgradeCurrentVersionWithUgradableVersion ", ex);
            }
            return null;
        }

    }

    private String CalculatePrice(String SurchargeType_FS3, double FuelQuantity, double ProductPrice_FS3, double VehicleSum_FS3, double DeptSum_FS3, double VehPercentage_FS3, double DeptPercentage_FS3){

        double cost = 0.0;
        if (SurchargeType_FS3.equalsIgnoreCase("0")) {
            cost = (FuelQuantity) * (ProductPrice_FS3 + VehicleSum_FS3 + DeptSum_FS3);
        } else {

            cost = (FuelQuantity * ProductPrice_FS3) + (((FuelQuantity * ProductPrice_FS3) * VehPercentage_FS3) / 100) + (((FuelQuantity * ProductPrice_FS3) * DeptPercentage_FS3) / 100);
        }

        DecimalFormat precision = new DecimalFormat("0.000");
        String Qty = (precision.format(cost));
        double cost_prec = Double.parseDouble(Qty);

        return String.valueOf(cost_prec);

    }

    public class SaveTankMonitorReadingy extends AsyncTask<Void, Void, Void> {

        TankMonitorEntity vrentity = null;

        public String response = null;

        public SaveTankMonitorReadingy(TankMonitorEntity vrentity) {
            this.vrentity = vrentity;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                ServerHandler serverHandler = new ServerHandler();

                Gson gson = new Gson();
                String jsonData = gson.toJson(vrentity);
                String userEmail = CommonUtils.getCustomerDetails_backgroundService_FS3(BackgroundService_FS_UNIT_3.this).PersonEmail;

                System.out.println("jsonDatajsonDatajsonData" + jsonData);
                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(vrentity.IMEI_UDID + ":" + userEmail + ":" + "SaveTankMonitorReading");
                response = serverHandler.PostTextData(BackgroundService_FS_UNIT_3.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {

                CommonUtils.LogMessage("TAG", "SaveTankMonitorReadingy ", ex);
            }
            return null;
        }

    }

    public String GetMacAddressOfProbe(String level){

        String MacAddress = "";
        String[] Seperate = level.split(",");

        for (int i= 0; i< 6 ;i++){

            String pd = CommonUtils.decimal2hex(Integer.parseInt(Seperate[i].trim()));
            MacAddress  = MacAddress+pd;

        }

        System.out.println("MacAddress of probe: "+MacAddress);

        return MacAddress;
    }

    public String GetProbeReading(String level) {

        String MacAddress = "";
        String[] Seperate = level.split(",");
        double prove = 0;
        for (int i = 0; i <= Seperate.length; i++) {

            if (i == 8) {
                String pd = CommonUtils.decimal2hex(Integer.parseInt(Seperate[i].trim()));
                MacAddress = MacAddress + pd;
            } else if (i == 9) {
                String pd = CommonUtils.decimal2hex(Integer.parseInt(Seperate[i].trim()));
                MacAddress = pd + MacAddress;
            }

            int finalpd = CommonUtils.hex2decimal(MacAddress);
            prove = finalpd / 128;

        }

        return String.valueOf(prove);
    }

    public String CalculateTemperature(String level) {

        String Temperature = "";
        String[] Seperate = level.split(",");
        double Temp = 0;
        for (int i = 0; i <= Seperate.length; i++) {

            if (i == 10) {
                Temperature = CommonUtils.decimal2hex(Integer.parseInt(Seperate[i].trim()));

            }

            int finalpd = CommonUtils.hex2decimal(Temperature);
            Temp = (finalpd * 0.48876)-50;

        }

        return String.valueOf(Temp);
    }

}