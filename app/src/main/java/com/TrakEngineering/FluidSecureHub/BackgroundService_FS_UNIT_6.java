package com.TrakEngineering.FluidSecureHub;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.TrakEngineering.FluidSecureHub.entity.RenameHose;
import com.TrakEngineering.FluidSecureHub.entity.SocketErrorEntityClass;
import com.TrakEngineering.FluidSecureHub.entity.SwitchTimeBounce;
import com.TrakEngineering.FluidSecureHub.entity.TankMonitorEntity;
import com.TrakEngineering.FluidSecureHub.entity.TrazComp;
import com.TrakEngineering.FluidSecureHub.offline.EntityOffTranz;
import com.TrakEngineering.FluidSecureHub.offline.OffDBController;
import com.TrakEngineering.FluidSecureHub.offline.OffTranzSyncService;
import com.TrakEngineering.FluidSecureHub.offline.OfflineConstants;
import com.TrakEngineering.FluidSecureHub.server.ServerHandler;
import com.example.fs_ipneigh30.FS_ArpNDK;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.TrakEngineering.FluidSecureHub.CommonUtils.GetDateString;

public class BackgroundService_FS_UNIT_6 extends Service {

    private static final String TAG = AppConstants.LOG_TXTN_HTTP + "-BS_FS6 ";
    String EMPTY_Val = "";
    ConnectionDetector cd = new ConnectionDetector(BackgroundService_FS_UNIT_6.this);
    private int AttemptCount = 0;
    private int GetPulsarAttemptFailCount = 0, FailureSeconds = 0;
    public static ArrayList<String> listOfConnectedIP = new ArrayList<String>();
    private String CurrTxnMode = "online", OffLastTXNid = "0";

    //String HTTP_URL = "http://192.168.43.140:80/";//for pipe
    //String HTTP_URL = "http://192.168.43.5:80/";//Other FS
    String HTTP_URL = "";

    public long sqlite_id = 0;

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

    String jsonRename;
    String jsonConnectWifi = "{\"Request\":  {\"Station\":{\"Connect_Station\":{\"ssid\":\"tenda\",\"password\":\"1234567890\",\"token\":\"1234567890123456789012345678901234567890\"}}}}";
    String jsonRelayOn = "{\"relay_request\":{\"Password\":\"12345678\",\"Status\":1}}";
    String jsonRelayOff = "{\"relay_request\":{\"Password\":\"12345678\",\"Status\":0}}";

    String URL_UPGRADE_START = HTTP_URL + "upgrade?command=start";

    String URL_TDL_info = HTTP_URL + "tld?level=info";

    String URL_GET_TXN_LAST10 = HTTP_URL + "client?command=cmtxtnid10";

    ArrayList<HashMap<String, String>> quantityRecords = new ArrayList<>();
    ArrayList<Integer> respCounter = new ArrayList<>();

    SimpleDateFormat sdformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    private String vehicleNumber, odometerTenths = "0", dNumber = "", pNumber = "", oText = "", hNumber = "";
    String LinkName = "", OtherName, IsOtherRequire, OtherLabel, VehicleNumber, PrintDate, CompanyName, Location, PersonName, PrinterMacAddress, PrinterName, TransactionId, VehicleId, PhoneNumber, PersonId, PulseRatio, MinLimit, FuelTypeId, ServerDate, IntervalToStopFuel, IsTLDCall, EnablePrinter, _OdoMeter, _Hours, PumpOnTime,LimitReachedMessage,SiteId;

    int timeFirst = 60;
    //Timer tFirst;
    //TimerTask taskFirst;
    boolean stopTimer;
    Timer pulse_timer;
    List<Timer> TimerList = new ArrayList<Timer>();
    boolean pulsarConnected = false;
    double minFuelLimit = 0, numPulseRatio = 0;
    String consoleString = "", outputQuantity = "0";
    //double CurrentLat = 0, CurrentLng = 0;
    //GoogleApiClient mGoogleApiClient;
    long stopAutoFuelSeconds = 0;
    boolean ongoingStatusSend = true;
    double fillqty = 0;
    double Lastfillqty = 0;
    int CNT_LAST = 0;
    Integer Pulses = 0;
    long sqliteID = 0;
    boolean GETPulsarCallCompleted = false;
    public OkHttpClient client = new OkHttpClient();
    public int countForZeroPulses = 0;
    public boolean IsAnyPostTxnCommandExecuted = false;

    String printReceipt = "", IsFuelingStop = "0", IsLastTransaction = "0";
    DBController controller = new DBController(BackgroundService_FS_UNIT_6.this);
    OffDBController offcontroller = new OffDBController(BackgroundService_FS_UNIT_6.this);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            super.onStart(intent, startId);

            try {
                if (AppConstants.DetailsServerSSIDList != null) {
                    if (AppConstants.DetailsServerSSIDList.size() > 5) {
                        LinkName = AppConstants.DetailsServerSSIDList.get(5).get("WifiSSId");
                    }
                }
            } catch (Exception e) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Something went wrong. please check Link name. Ex:" + e.getMessage());
            }

            Bundle extras = intent.getExtras();
            if (extras == null) {
                Log.d("Service", "null");
                if (Constants.BusyVehicleNumberList != null) {
                    Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS6);
                }
                StopTransaction(false, true); // extras == null
            } else {
                stopTimer = true;
                AttemptCount = 0;
                IsFuelingStop = "0";
                IsLastTransaction = "0";

                sqlite_id = (long) extras.get("sqlite_id");

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

                URL_GET_TXN_LAST10 = HTTP_URL + "client?command=cmtxtnid10";

                jsonConnectWifi = "{\"Request\":  {\"Station\":{\"Connect_Station\":{\"ssid\":\"tenda\",\"password\":\"1234567890\",\"token\":\"1234567890123456789012345678901234567890\"}}}}";
                jsonRelayOn = "{\"relay_request\":{\"Password\":\"12345678\",\"Status\":1}}";
                jsonRelayOff = "{\"relay_request\":{\"Password\":\"12345678\",\"Status\":0}}";

                jsonRename = "{\"Request\":{\"Softap\":{\"Connect_Softap\":{\"authmode\":\"WPAPSK/WPA2PSK\",\"channel\":6,\"ssid\":\"" + AppConstants.REPLACEBLE_WIFI_NAME_FS6 + "\",\"password\":\"123456789\"}}}}";

                Constants.FS_6STATUS = "BUSY";
                AppConstants.isHTTPTxnRunningFS6 = true;

                Constants.BusyVehicleNumberList.add(Constants.AccVehicleNumber_FS6);

                if (cd.isConnectingToInternet() && AppConstants.AUTH_CALL_SUCCESS) {
                    CurrTxnMode = "online";
                } else {
                    if (AppConstants.AUTH_CALL_SUCCESS) {
                        SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
                        TransactionId = sharedPref.getString("TransactionId_FS6", "");
                        OffLastTXNid = TransactionId;//Set transaction id to offline
                    }
                    CurrTxnMode = "offline";
                }

                SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
                TransactionId = sharedPref.getString("TransactionId_FS6", "");
                VehicleId = sharedPref.getString("VehicleId_FS6", "");
                PhoneNumber = sharedPref.getString("PhoneNumber_FS6", "");
                PersonId = sharedPref.getString("PersonId_FS6", "");
                PulseRatio = sharedPref.getString("PulseRatio_FS6", "1");
                MinLimit = sharedPref.getString("MinLimit_FS6", "0");
                FuelTypeId = sharedPref.getString("FuelTypeId_FS6", "");
                ServerDate = sharedPref.getString("ServerDate_FS6", "");
                IntervalToStopFuel = sharedPref.getString("IntervalToStopFuel_FS6", "0");
                IsTLDCall = sharedPref.getString("IsTLDCall_FS6", "False");
                EnablePrinter = sharedPref.getString("EnablePrinter_FS6", "False");
                PumpOnTime = sharedPref.getString("PumpOnTime_FS6", "0");
                LimitReachedMessage = sharedPref.getString("LimitReachedMessage_FS6", "");
                SiteId = sharedPref.getString("SiteId_FS6", "");

                if (cd.isConnectingToInternet() && CurrTxnMode.equalsIgnoreCase("online")) {

                    getIpOverOSVersion();

                    //Set Transaction ID
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Sending SET_TXNID command to Link: " + LinkName);
                            new CommandsPOST().execute(URL_SET_TXNID, "{\"txtnid\":" + TransactionId + "}", "");
                        }
                    }, 1000);

                    ////////////////////////////////////////////
                    String userEmail = CommonUtils.getCustomerDetails_backgroundServiceHTTP(BackgroundService_FS_UNIT_6.this).PersonEmail;
                    String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_FS_UNIT_6.this) + ":" + userEmail + ":" + "TransactionComplete" + AppConstants.LANG_PARAM);

                    HashMap<String, String> imap = new HashMap<>();
                    imap.put("jsonData", "");
                    imap.put("authString", authString);

                    sqliteID = controller.insertTransactions(imap);
                    CommonUtils.AddRemovecurrentTransactionList(true, TransactionId);//Add transaction Id to list
                    //////////////////////////////////////////////////////////////

                    //UpgradeTransaction Status initial in background service
                    CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "8", BackgroundService_FS_UNIT_6.this);

                    minFuelLimit = Double.parseDouble(MinLimit);

                    numPulseRatio = Double.parseDouble(PulseRatio);

                    stopAutoFuelSeconds = Long.parseLong(IntervalToStopFuel);

                    if (AppConstants.EnableFA) {
                        stopAutoFuelSeconds = stopAutoFuelSeconds * 3;
                    }
                } else {
                    offlineLogic();
                }
            }
            quantityRecords.clear();
        } catch (NullPointerException e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "onStartCommand Exception " + e);
            Log.d("Ex", e.getMessage());
            this.stopSelf();
        }

        AppConstants.isRelayON_fs6 = false;
        if (numPulseRatio <= 0) {
            ExitServicePulsarRatioZero();
        } else {
            //Pulsar On
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Call Relay On Function
                    String IsRelayOnCheck = RelayOnThreeAttempts();
                    if (IsRelayOnCheck != null && !IsRelayOnCheck.equalsIgnoreCase("")) {
                        try {
                            JSONObject jsonObject = new JSONObject(IsRelayOnCheck);
                            String relay_status1 = null;
                            relay_status1 = jsonObject.getString("relay_response");
                            if (relay_status1.equalsIgnoreCase("{\"status\":1}")) {
                                AppConstants.isRelayON_fs6 = true;
                                startQuantityInterval();
                            } else {
                                ExitBackgroundService();
                            }
                        } catch (JSONException e) {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "RelayOnCheck Exception: " + e.getMessage());
                        }
                    } else {
                        ExitBackgroundService();
                    }
                }
            }, 1500);
        }
        GetInterruptedTxtnId();

        // return super.onStartCommand(intent, flags, startId);
        return Service.START_NOT_STICKY;
    }

    private void ExitBackgroundService() {
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "Relay status error");
        CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "6", BackgroundService_FS_UNIT_6.this);
        relayOffCommand();
        ProceedToPostTransactionCommands(); //Failed to turn ON the relay
    }

    private void ExitServicePulsarRatioZero() {
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "pulser ratio error>>" + numPulseRatio);
        CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "6", BackgroundService_FS_UNIT_6.this);
        relayOffCommand();
        ProceedToPostTransactionCommands(); // Pulsar Ratio Zero
    }

    private String RelayOnThreeAttempts() {
        String IsRelayOn = "";
        try {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Sending RELAY ON command to Link: " + LinkName + " (Attempt 1)");
            IsRelayOn = new CommandsPOST().execute(URL_RELAY, jsonRelayOn, "").get(); //Relay ON First Attempt

            if (IsRelayOn != null && !IsRelayOn.equalsIgnoreCase("")) {
                JSONObject jsonObject = new JSONObject(IsRelayOn);
                String relay_status1 = jsonObject.getString("relay_response");

                if (!relay_status1.equalsIgnoreCase("{\"status\":1}")) {

                    Thread.sleep(1000);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Sending RELAY ON command to Link: " + LinkName + " (Attempt 2)");
                    IsRelayOn = new CommandsPOST().execute(URL_RELAY, jsonRelayOn, "").get(); //Relay ON Second attempt

                    JSONObject jsonObjectSite = new JSONObject(IsRelayOn);
                    String relay_status2 = jsonObjectSite.getString("relay_response");

                    if (!relay_status2.equalsIgnoreCase("{\"status\":1}")) {

                        Thread.sleep(1000);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Sending RELAY ON command to Link: " + LinkName + " (Attempt 3)");
                        IsRelayOn = new CommandsPOST().execute(URL_RELAY, jsonRelayOn, "").get(); //Relay ON Third attempt
                    }
                }
            }
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "RelayOnThreeAttempts. --Exception " + e.getMessage());
        }
        return IsRelayOn;
    }

    /*public void stopFirstTimer(boolean flag) {
        if (flag) {
            tFirst.cancel();
            tFirst.purge();
        } else {
            tFirst.cancel();
            tFirst.purge();

            WelcomeActivity.SelectedItemPos = -1;
            AppConstants.BUSY_STATUS = true;

            Intent i = new Intent(this, WelcomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
    }*/

    public class CommandsPOST extends AsyncTask<String, Void, String> {
        public String resp = "";
        public String jsonParam = "";
        public String calledFrom = "";

        protected String doInBackground(String... param) {
            try {
                jsonParam = param[1];
                calledFrom = param[2];

                MediaType JSON = MediaType.parse("application/json");

                //OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(15, TimeUnit.SECONDS);
                client.setReadTimeout(15, TimeUnit.SECONDS);
                RequestBody body = RequestBody.create(JSON, jsonParam);

                Request request = new Request.Builder()
                        .url(param[0])
                        .header("Accept-Encoding", "identity")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (SocketException se) {
                StoreLinkDisconnectInfo(se);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "<CommandsPOST URL: " + param[0] + ">");
                StopTransaction(true, true); //CommandsPOST - SocketException
            } catch (Exception e) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "<CommandsPOST URL: " + param[0] + ">");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "CommandsPOST InBackground Exception " + e.getMessage());
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (jsonParam.equalsIgnoreCase(jsonRelayOff) && result.contains("relay_response")) {
                    System.out.println(result);
                    CommonUtils.sharedPrefTxtnInterrupted(BackgroundService_FS_UNIT_6.this, TransactionId, false);

                } else if (calledFrom.equalsIgnoreCase("sampling_time")) {
                    UpdateSwitchTimeBounceForLink();
                    CloseTransaction(true);
                }
            } catch (Exception e) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "CommandsPOST onPostExecute Exception " + e.getMessage());
            }
        }
    }

    public class CommandsGET extends AsyncTask<String, Void, String> {
        public String resp = "";

        protected String doInBackground(String... param) {
            try {
                //OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(15, TimeUnit.SECONDS);
                client.setReadTimeout(15, TimeUnit.SECONDS);
                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();
            } catch (SocketException se){
                StoreLinkDisconnectInfo(se);
            } catch (Exception e) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "CommandsGET InBackground Exception " + e.getMessage());
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                System.out.println("APFS_6 OUTPUT" + result);
            } catch (Exception e) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "CommandsGET onPostExecute Exception " + e.getMessage());
            }
        }
    }

    public void startQuantityInterval() {
        CommonUtils.sharedPrefTxtnInterrupted(BackgroundService_FS_UNIT_6.this, TransactionId, true);
        GETPulsarCallCompleted = true; // set as true for first attempt
        pulse_timer = new Timer();
        TimerList.add(pulse_timer);
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                try {
                    if (stopTimer) {
                        if (IsFsConnected(HTTP_URL)) {
                            AttemptCount = 0;

                            //Asynchronous okhttp call
                            if (GETPulsarCallCompleted) {
                                GETPulsarQuantityAsyncCall(URL_GET_PULSAR);
                            }
                        } else {
                            if (AttemptCount > 2) {
                                //FS Link DisConnected
                                System.out.println("FS Link not connected" + listOfConnectedIP);
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "Link not connected");
                                CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "6", BackgroundService_FS_UNIT_6.this);
                                StopTransaction(true, true); //Link not connected
                            } else {
                                getIpOverOSVersion();

                                Thread.sleep(2000);
                                System.out.println("FS Link not connected ~~AttemptCount:" + AttemptCount);
                                AttemptCount = AttemptCount + 1;
                            }
                        }
                    }
                } catch (Exception e) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "startQuantityInterval Exception: " + e.getMessage());
                }
            }
        };
        pulse_timer.schedule(tt, 0, 4000);
    }

    public boolean IsFsConnected(String toMatchString) {
        for (String HttpAddress : listOfConnectedIP) {
            if (HttpAddress.contains(toMatchString))
                return true;
        }
        return false;
    }

    public void GETPulsarQuantityAsyncCall(String URL_GET_PULSAR) {
        GETPulsarCallCompleted = false;
        //OkHttpClient httpClient = new OkHttpClient();
        client.setConnectTimeout(AppConstants.CONNECTION_TIMEOUT_SEC, TimeUnit.SECONDS);
        client.setReadTimeout(AppConstants.READ_TIMEOUT_SEC, TimeUnit.SECONDS);
        client.setWriteTimeout(AppConstants.WRITE_TIMEOUT_SEC, TimeUnit.SECONDS);

        Request request = new Request.Builder()
                .url(URL_GET_PULSAR)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(Request request, IOException e) {

                GETPulsarCallCompleted = true;
                Date date = new Date();   // given date
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.setTime(date);
                int CurrentSeconds = calendar.get(Calendar.SECOND);

                if (FailureSeconds != CurrentSeconds) {
                    FailureSeconds = CurrentSeconds;

                    Log.e(TAG, "error in getting response using async okhttp call");
                    GetPulsarAttemptFailCount++;
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "GETPulsarQuantity onFailure Exception: " + e.toString());
                }
                if (GetPulsarAttemptFailCount == 3) {
                    if (Pulses > 0 || fillqty > 0) {
                        if (CurrTxnMode.equalsIgnoreCase("online")) {
                            CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "10", BackgroundService_FS_UNIT_6.this);
                        } else {
                            offcontroller.updateOfflineTransactionStatus(sqlite_id + "", "10");
                        }
                    }
                    relayOffCommand();
                    ProceedToPostTransactionCommands(); //GetPulsar Attempt Failed
                }
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(Response response) throws IOException {

                GETPulsarCallCompleted = true;
                ResponseBody responseBody = response.body();
                if (!response.isSuccessful()) {
                    throw new IOException("Error response " + response);
                } else {
                    GetPulsarAttemptFailCount = 0;
                    String result = responseBody.string();

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
                            stopButtonFunctionality(); //getPulsarResponseEmptyFor3times
                        } else {
                            if (stopTimer)
                                pulsarQtyLogic(result);
                        }
                    } catch (Exception e) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "GETPulsarQuantity onPostExecute Exception " + e.getMessage());
                    }
                }
                responseBody.close();
            }
        });
    }

    public void pulsarQtyLogic(String result) {
        int secure_status = 0;

        try {
            if (result.contains("pulsar_status")) {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject joPulsarStat = jsonObject.getJSONObject("pulsar_status");
                String counts = joPulsarStat.getString("counts");
                String pulsar_status = joPulsarStat.getString("pulsar_status");
                String pulsar_secure_status = joPulsarStat.getString("pulsar_secure_status");

                if (pulsar_status.trim().equalsIgnoreCase("1")) {
                    pulsarConnected = true;
                } else if (pulsar_status.trim().equalsIgnoreCase("0")) {
                    pulsarConnected = false;

                    if (Constants.BusyVehicleNumberList != null) {
                        Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS6);
                    }

                    IsFuelingStop = "1";
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Link:" + LinkName + "; Pulsar status: " + pulsar_status); // some txns stopped unexpectedly, so added log here.

                    if (GetPulsarAttemptFailCount > 0) {
                        if (CurrTxnMode.equalsIgnoreCase("online")) {
                            CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "10", BackgroundService_FS_UNIT_6.this);
                        } else {
                            offcontroller.updateOfflineTransactionStatus(sqlite_id + "", "10");
                        }
                    }

                    stopButtonFunctionality(); //pulsar_status = 0
                    return;
                }

                //To avoid accepting Lower Quantity
                int CNT_current = Integer.parseInt(counts);

                //in progress (transaction recently started, no new information): Transaction ongoing = 8  --non zero qty
                if (CNT_current > 0 && ongoingStatusSend) {
                    ongoingStatusSend = false;
                    if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH)
                        CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "8", BackgroundService_FS_UNIT_6.this);
                }

                if (CNT_LAST > 0 && CNT_current == 0) {
                    countForZeroPulses = countForZeroPulses + 1;
                }

                if (CNT_LAST <= CNT_current) {
                    CNT_LAST = CNT_current;

                    convertCountToQuantity(counts);
                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "pulsarQtyLogic: Count from the link: " + counts + "; Last count: " + CNT_LAST);

                    if (CNT_LAST > 0 && CNT_current > 0 && CNT_LAST > CNT_current) {
                        CNT_current = CNT_LAST + CNT_current;
                        convertCountToQuantity(String.valueOf(CNT_current));
                    }
                }

                if (countForZeroPulses > 2) {
                    countForZeroPulses = 0;
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "<Auto Stop Hit (ZeroPulses).>");
                    stopTimer = false;
                    stopButtonFunctionality(); //Auto Stop Hit (ZeroPulses)
                    return;
                }

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

                        if (Constants.BusyVehicleNumberList != null) {
                            Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS6);
                        }

                        IsFuelingStop = "1";
                        System.out.println("Auto Stop! Count down timer completed");
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Link:" + LinkName + " Auto Stop! Count down timer completed");
                        //AppConstants.colorToastBigFont(BackgroundService_FS_UNIT_6.this, AppConstants.FS6_CONNECTED_SSID + " Auto Stop!\n\nCount down timer completed.", Color.BLUE);
                        stopButtonFunctionality(); //Auto Stop Hit (secure_status >= 5)
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
                            if (Constants.BusyVehicleNumberList != null) {
                                Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS6);
                            }
                            IsFuelingStop = "1";
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Link: " + LinkName + "; Auto Stop! You reached MAX fuel limit.");
                            AppConstants.DisplayToastmaxlimit = true;
                            AppConstants.MaxlimitMessage = LimitReachedMessage;
                            stopButtonFunctionality(); //Auto Stop! MAX fuel limit reached.
                        }
                    } else if (minFuelLimit == -1) {
                        if (Constants.BusyVehicleNumberList != null) {
                            Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS6);
                        }
                        IsFuelingStop = "1";
                        System.out.println("Auto Stop! You reached MAX fuel limit.");
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Link:" + LinkName + " Auto Stop! You reached MAX fuel limit.");
                        AppConstants.DisplayToastmaxlimit = true;
                        AppConstants.MaxlimitMessage = LimitReachedMessage;
                        stopButtonFunctionality(); //Auto Stop! minFuelLimit = -1
                    }
                } catch (Exception e) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "quantity reach max limit1 Exception " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "quantity reach max limit2 Exception " + e.getMessage());
        }
    }

    public void stopButtonFunctionality() {
        quantityRecords.clear();
        consoleString = "";
        //it stops pulsar logic------
        stopTimer = false;

        if (pulsarConnected) {
            //#1145 - I see Link receive relay off commands twice for every TXTN.
            relayOffCommand();
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean goToFinalStep = false;
                            String cntA = "0", cntB = "0", cntC = "0";

                            for (int i = 0; i < 2; i++) {
                                String result = new GETFINALPulsar().execute(URL_GET_PULSAR).get();

                                if (i == 1) {
                                    goToFinalStep = true;
                                }

                                if (result.contains("pulsar_status")) {
                                    JSONObject jsonObject = new JSONObject(result);
                                    JSONObject joPulsarStat = jsonObject.getJSONObject("pulsar_status");
                                    String counts = joPulsarStat.getString("counts");
                                    //String pulsar_status = joPulsarStat.getString("pulsar_status");
                                    //String pulsar_secure_status = joPulsarStat.getString("pulsar_secure_status");

                                    //To avoid accepting Lower Quantity
                                    int CNT_current = Integer.parseInt(counts);

                                    if (CNT_LAST <= CNT_current) {
                                        CNT_LAST = CNT_current;

                                        convertCountToQuantity(counts);
                                    } else {
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + "<Count from the link: " + counts + ">");
                                    }

                                    /*if (i == 0)
                                        cntA = counts;
                                    else if (i == 1)
                                        cntB = counts;
                                    else
                                        cntC = counts;*/

                                    if (i == 1) {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                finalLastStep();
                                            }
                                        }, 500); //1000
                                    }
                                    Thread.sleep(1000);
                                } else {
                                    if (goToFinalStep) { // To stop the transaction even if no response is received from the GETFINALPulsar.
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                finalLastStep();
                                            }
                                        }, 500); //1000
                                    }
                                }
                            }
                        } catch (Exception e) {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "stopButtonFunctionality Exception: " + e.getMessage());
                        }
                    }
                }, 1000);
            }
        });
    }

    public void finalLastStep() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (AppConstants.NeedToRenameFS6) {
                    consoleString += "RENAME:\n" + jsonRename;
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Sending RENAME command to Link: " + LinkName + " (New Name: " + AppConstants.REPLACEBLE_WIFI_NAME_FS6 + ")");
                    new CommandsPOST().execute(URL_WIFI, jsonRename, "");
                }
            }
        }, 1000); //2500

        long secondsTime = 500; //3000

        if (AppConstants.NeedToRenameFS6) {
            secondsTime = 2000; //5000
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                GetDetails();
                TransactionCompleteFunction();
                ProceedToPostTransactionCommands(); //finalLastStep
            }
        }, secondsTime);
    }

    public class GETFINALPulsar extends AsyncTask<String, Void, String> {
        public String resp = "";

        protected String doInBackground(String... param) {
            try {
                //OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(AppConstants.CONNECTION_TIMEOUT_SEC, TimeUnit.SECONDS);
                client.setReadTimeout(AppConstants.READ_TIMEOUT_SEC, TimeUnit.SECONDS);
                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();
            } catch (Exception e) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "GETFINALPulsar InBackground Exception " + e.getMessage());
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                consoleString += "OUTPUT- " + result + "\n";
                // tvConsole.setText(consoleString);
            } catch (Exception e) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "GETFINALPulsar onPostExecute Exception " + e.getMessage());
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

                if (Pulses <= 0) {
                    try {
                        int pont = Integer.parseInt(PumpOnTime);

                        if (seconds >= pont) {
                            //Timed out (Start was pressed, and pump on timer hit): Pump Time On limit reached* = 4
                            if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH)
                                CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "4", BackgroundService_FS_UNIT_6.this);
                            commonForAutoStopQtySameForSeconds();
                        }
                    } catch (Exception e) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "secondsTimeLogic Exception: " + e.getMessage() + "; LinkName: " + LinkName + "; PumpOnTime: " + PumpOnTime);
                    }
                } else {
                    if (stopAutoFuelSeconds > 0) {
                        if (seconds >= stopAutoFuelSeconds) {
                            if (qtyFrequencyCount()) {
                                //qty is same for some time
                                IsFuelingStop = "1";
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "Link:" + LinkName + " Auto Stop!Quantity is same for last");
                                if (Constants.BusyVehicleNumberList != null) {
                                    Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS6);
                                }
                                stopButtonFunctionality(); //Auto Stop! Same quantity (Pulses > 0)
                            } else {
                                quantityRecords.remove(0);
                                System.out.println("0 th pos deleted");
                                System.out.println("seconds--" + seconds);
                                commonForAutoStopQtySameForSeconds();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "secondsTimeLogic Exception " + e.getMessage());
        }
    }

    public void commonForAutoStopQtySameForSeconds() {
        if (qtyFrequencyCount()) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " Link:" + LinkName + " >>Auto Stop!Quantity is same for last");
            IsFuelingStop = "1";
            if (Constants.BusyVehicleNumberList != null) {
                Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS6);
            }
            stopButtonFunctionality();  //Auto Stop! Quantity is same
        } else {
            quantityRecords.remove(0);
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

        DecimalFormat precision = new DecimalFormat("0.00");
        Constants.FS_6Gallons = (precision.format(fillqty));
        Constants.FS_6Pulse = outputQuantity;

        if (CurrTxnMode.equalsIgnoreCase("online")) {

            TrazComp authEntityClass = new TrazComp();
            authEntityClass.TransactionId = TransactionId;
            authEntityClass.FuelQuantity = fillqty;
            authEntityClass.AppInfo = " Version:" + CommonUtils.getVersionCode(BackgroundService_FS_UNIT_6.this) + " " + AppConstants.getDeviceName() + " Android " + Build.VERSION.RELEASE + " " + "--Main Transaction--";
            authEntityClass.TransactionFrom = "A";
            authEntityClass.Pulses = Integer.parseInt(counts);
            authEntityClass.IsFuelingStop = IsFuelingStop;
            authEntityClass.IsLastTransaction = IsLastTransaction;

            Gson gson = new Gson();
            String jsonData = gson.toJson(authEntityClass);

            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "ID:" + TransactionId + "; LINK:" + LinkName + "; Pulses:" + Integer.parseInt(counts) + "; Qty:" + fillqty);

            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceHTTP(BackgroundService_FS_UNIT_6.this).PersonEmail;
            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_FS_UNIT_6.this) + ":" + userEmail + ":" + "TransactionComplete" + AppConstants.LANG_PARAM);

            HashMap<String, String> imap = new HashMap<>();
            imap.put("jsonData", jsonData);
            imap.put("authString", authString);
            imap.put("sqliteId", sqliteID + "");

            if (Pulses > 0 || fillqty > 0) {
                int rowseffected = controller.updateTransactions(imap);
                System.out.println("rowseffected-" + rowseffected);
                if (rowseffected == 0) {
                    sqliteID = controller.insertTransactions(imap);
                }
            }
        } else {
            if (Pulses > 0 || fillqty > 0) {
                offcontroller.updateOfflinePulsesQuantity(sqlite_id + "", counts, fillqty + "", OffLastTXNid);
            }
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Offline LINK:" + LinkName + "; Pulses:" + Integer.parseInt(counts) + "; Qty:" + fillqty);
        }
    }

    public void GetDetails() {
        vehicleNumber = Constants.AccVehicleNumber_FS6;
        odometerTenths = Constants.AccOdoMeter_FS6 + "";
        dNumber = Constants.AccDepartmentNumber_FS6;
        pNumber = Constants.AccPersonnelPIN_FS6;
        oText = Constants.AccOther_FS6;
        hNumber = Constants.AccHours_FS6 + "";

        if (dNumber == null) {
            dNumber = "";
        }

        if (pNumber == null) {
            pNumber = "";
        }

        if (oText == null) {
            oText = "";
        }
    }

    public void TransactionCompleteFunction() {
        try {
            SharedPreferences sharedPrefODO = this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
            IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
            OtherLabel = sharedPrefODO.getString(AppConstants.OtherLabel, "Other");

            SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
            TransactionId = sharedPref.getString("TransactionId_FS6", "");
            VehicleId = sharedPref.getString("VehicleId_FS6", "");
            PhoneNumber = sharedPref.getString("PhoneNumber_FS6", "");
            PersonId = sharedPref.getString("PersonId_FS6", "");
            PulseRatio = sharedPref.getString("PulseRatio_FS6", "1");
            MinLimit = sharedPref.getString("MinLimit_FS6", "0");
            FuelTypeId = sharedPref.getString("FuelTypeId_FS6", "");
            ServerDate = sharedPref.getString("ServerDate_FS6", "");
            IntervalToStopFuel = sharedPref.getString("IntervalToStopFuel_FS6", "0");

            PrintDate = sharedPref.getString("PrintDate_FS6", "");
            CompanyName = sharedPref.getString("Company_FS6", "");
            Location = sharedPref.getString("Location_FS6", "");
            PersonName = sharedPref.getString("PersonName_FS6", "");
            PrinterMacAddress = sharedPref.getString("PrinterMacAddress_FS6", "");
            PrinterName = sharedPref.getString("PrinterName_FS6", "");
            VehicleNumber = sharedPref.getString("vehicleNumber_FS6", "");
            OtherName = sharedPref.getString("accOther_FS6", "");
            _OdoMeter = sharedPref.getString("OdoMeter_FS6", "");
            _Hours = sharedPref.getString("Hours_FS6", "");

            EntityOffTranz tzc = offcontroller.getTransactionDetailsBySqliteId(sqlite_id);
            String siteid = tzc.SiteId;
            String IsTLDCallOffline = null;
            if (siteid != null && !siteid.equalsIgnoreCase("")) {
                HashMap<String, String> linkmap = offcontroller.getLinksDetailsBySiteId(siteid);
                IsTLDCallOffline = linkmap.get("IsTLDCall");
            }

            if (IsTLDCall.equalsIgnoreCase("True") || (IsTLDCallOffline != null && IsTLDCallOffline.equalsIgnoreCase("True"))) {
                TankMonitorReading(); //Get Tank Monitor Reading and save it to server
            }

            if (CurrTxnMode.equalsIgnoreCase("online")) {
                try {
                    double VehicleSum_FS6 = Double.parseDouble(sharedPref.getString("VehicleSum_FS6", "0"));
                    double DeptSum_FS6 = Double.parseDouble(sharedPref.getString("DeptSum_FS6", "0"));
                    double VehPercentage_FS6 = Double.parseDouble(sharedPref.getString("VehPercentage_FS6", "0"));
                    double DeptPercentage_FS6 = Double.parseDouble(sharedPref.getString("DeptPercentage_FS6", "0"));
                    String SurchargeType_FS6 = sharedPref.getString("SurchargeType_FS6", "");
                    double ProductPrice_FS6 = Double.parseDouble(sharedPref.getString("ProductPrice_FS6", "0"));

                    //Print Transaction Receipt
                    DecimalFormat precision = new DecimalFormat("0.00");
                    String Qty = (precision.format(fillqty));
                    double FuelQuantity = Double.parseDouble(Qty);

                    //---------print cost--------
                    String InitPrintCost = CalculatePrice(SurchargeType_FS6, FuelQuantity, ProductPrice_FS6, VehicleSum_FS6, DeptSum_FS6, VehPercentage_FS6, DeptPercentage_FS6);
                    DecimalFormat precision_cost = new DecimalFormat("0.00");
                    String PrintCost = (precision_cost.format(Double.parseDouble(InitPrintCost)));

                    if (EnablePrinter.equalsIgnoreCase("True")) {
                        printReceipt = CommonUtils.GetPrintReciptNew(IsOtherRequire, CompanyName, PrintDate, LinkName, Location, VehicleNumber, PersonName, Qty, PrintCost, OtherLabel, OtherName, _OdoMeter, _Hours);

                        //Start background Service to print receipt
                        Intent serviceIntent = new Intent(BackgroundService_FS_UNIT_6.this, BackgroundServiceBluetoothPrinter.class);
                        serviceIntent.putExtra("printReceipt", printReceipt);
                        startService(serviceIntent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                consoleString = "";

                if (AppConstants.NeedToRenameFS6) {
                    String userEmail = CommonUtils.getCustomerDetails_backgroundServiceHTTP(BackgroundService_FS_UNIT_6.this).PersonEmail;

                    String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(this) + ":" + userEmail + ":" + "SetHoseNameReplacedFlag" + AppConstants.LANG_PARAM);

                    RenameHose rhose = new RenameHose();
                    rhose.SiteId = AppConstants.UP_SiteId_fs6;//AppConstants.R_SITE_ID;
                    rhose.HoseId = AppConstants.UP_HoseId_fs6;//AppConstants.R_HOSE_ID;
                    rhose.IsHoseNameReplaced = "Y";

                    Gson gson = new Gson();
                    String jsonData = gson.toJson(rhose);

                    storeIsRenameFlag(this, AppConstants.NeedToRenameFS6, jsonData, authString);
                }
            }
            ///////////////////////////////////
            AppConstants.BUSY_STATUS = true;
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "TransactionCompleteFunction Exception: " + e.getMessage());
        }
    }

    private void PostTransactionBackgroundTasks(boolean isTransactionCompleted) {
        try {
            if (CurrTxnMode.equalsIgnoreCase("online")) {
                //boolean BSRunning = CommonUtils.checkServiceRunning(BackgroundService_FS_UNIT_6.this, AppConstants.PACKAGE_BACKGROUND_SERVICE);
                //if (!BSRunning) {
                if (IsAnyPostTxnCommandExecuted) {
                    IsAnyPostTxnCommandExecuted = false;
                    startService(new Intent(this, BackgroundService.class));
                }
                //}
            }

            if (!isTransactionCompleted) {
                if (OfflineConstants.isOfflineAccess(BackgroundService_FS_UNIT_6.this))
                    SyncOfflineData();
            }
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "PostTransactionBackgroundTasks Exception: " + e.getMessage());
        }
    }

    public void TankMonitorReading() {
        String mac_address = "";
        String probe_reading = "";
        String probe_temperature = "";
        String LSB = "";
        String Response_code = "";
        String MSB = "";
        String Tem_data = "";

        try {
            SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
            TransactionId = sharedPref.getString("TransactionId_FS6", "");

            ServerDate = sharedPref.getString("ServerDate_FS6", "");
            PrintDate = sharedPref.getString("PrintDate_FS6", "");

            //Get TankMonitoring details from FluidSecure Link
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Sending TLD_INFO command to Link: " + LinkName);
            String response1 = new CommandsGET().execute(URL_TDL_info).get();

            if (response1.equalsIgnoreCase("")) {
                System.out.println("TLD response empty");
            } else {
                try {
                    JSONObject reader = null;
                    reader = new JSONObject(response1);

                    JSONObject tld = reader.getJSONObject("tld");
                    mac_address = tld.getString("Mac_address");
                    String Sensor_ID = tld.getString("Sensor_ID");
                    Response_code = tld.getString("Response_code");
                    LSB = tld.getString("LSB");
                    MSB = tld.getString("MSB");
                    Tem_data = tld.getString("Tem_data");
                    String Checksum = tld.getString("Checksum");

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "TLD Reading:" + " Mac Address:" + mac_address + " Sensor ID:" + Sensor_ID + " Response code:" + Response_code + " LSB:" + LSB + " MSB:" + MSB + " Temperature data:" + Tem_data + " Checksum:" + Checksum);

                    //Get mac address of probe
                    //String mac_str = GetMacAddressOfProbe(level);
                    //mac_address = ConvertToMacAddressFormat(mac_str);

                    //Calculate probe reading
                    //probe_reading = GetProbeReading(LSB,MSB);

                    //probe_temperature = CalculateTemperature(Tem_data);
                } catch (JSONException e) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "TankMonitorReading ~~~JSONException~~" + e.getMessage());
                }
                //-----------------------------------------------------------
                String CurrentDeviceDate = CommonUtils.getTodaysDateInString();
                TankMonitorEntity obj_entity = new TankMonitorEntity();
                obj_entity.IMEI_UDID = AppConstants.getIMEI(BackgroundService_FS_UNIT_6.this);
                obj_entity.FromSiteId = Integer.parseInt(AppConstants.SITE_ID);
                // obj_entity.ProbeReading = probe_reading;
                obj_entity.TLD = mac_address;
                obj_entity.LSB = LSB;
                obj_entity.MSB = MSB;
                obj_entity.TLDTemperature = Tem_data;
                obj_entity.ReadingDateTime = CurrentDeviceDate;//PrintDate;
                obj_entity.Response_code = Response_code;//Response_code;
                obj_entity.Level = "";
                obj_entity.FromDirectTLD = "n";

                if (CurrTxnMode.equalsIgnoreCase("online")) {
                    SaveTankMonitorReading TestAsynTask = new SaveTankMonitorReading(obj_entity);
                    TestAsynTask.execute();
                    TestAsynTask.get();

                    String serverRes = TestAsynTask.response;

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " TankMonitorReading ~~~serverRes~~" + serverRes);
                } else {
                    offcontroller.insertTLDReadings(mac_address, "", AppConstants.SITE_ID, "", AppConstants.getIMEI(BackgroundService_FS_UNIT_6.this), LSB, MSB, Tem_data, CurrentDeviceDate, Response_code, "n");
                }
            }
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "TankMonitorReading ~~~Exception~~" + e.getMessage());
        }
    }

    public void storeIsRenameFlag(Context context, boolean flag, String jsonData, String authString) {
        SharedPreferences pref;

        SharedPreferences.Editor editor;
        pref = context.getSharedPreferences("storeIsRenameFlagFS6", 0);
        editor = pref.edit();

        // Storing
        editor.putBoolean("flag", flag);
        editor.putString("jsonData", jsonData);
        editor.putString("authString", authString);

        // commit changes
        editor.commit();
    }

    public void clearEditTextFields() {
        CNT_LAST = 0;
        Constants.AccVehicleNumber_FS6 = "";
        Constants.AccOdoMeter_FS6 = 0;
        Constants.AccDepartmentNumber_FS6 = "";
        Constants.AccPersonnelPIN_FS6 = "";
        Constants.AccOther_FS6 = "";
        Constants.AccVehicleOther_FS6 = "";
        Constants.AccHours_FS6 = 0;
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

    private String CalculatePrice(String SurchargeType_FS6, double FuelQuantity, double ProductPrice_FS6, double VehicleSum_FS6, double DeptSum_FS6, double VehPercentage_FS6, double DeptPercentage_FS6) {
        double cost = 0.0;
        if (SurchargeType_FS6.equalsIgnoreCase("0")) {
            cost = (FuelQuantity) * (ProductPrice_FS6 + VehicleSum_FS6 + DeptSum_FS6);
        } else {

            cost = (FuelQuantity * ProductPrice_FS6) + (((FuelQuantity * ProductPrice_FS6) * VehPercentage_FS6) / 100) + (((FuelQuantity * ProductPrice_FS6) * DeptPercentage_FS6) / 100);
        }

        DecimalFormat precision = new DecimalFormat("0.000");
        String Qty = (precision.format(cost));
        double cost_prec = Double.parseDouble(Qty);

        return String.valueOf(cost_prec);
    }

    public class SaveTankMonitorReading extends AsyncTask<Void, Void, Void> {
        TankMonitorEntity vrentity = null;

        public String response = null;

        public SaveTankMonitorReading(TankMonitorEntity vrentity) {
            this.vrentity = vrentity;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                ServerHandler serverHandler = new ServerHandler();

                Gson gson = new Gson();
                String jsonData = gson.toJson(vrentity);
                String userEmail = CommonUtils.getCustomerDetails_backgroundServiceHTTP(BackgroundService_FS_UNIT_6.this).PersonEmail;

                System.out.println("jsonDatajsonDatajsonData" + jsonData);
                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(vrentity.IMEI_UDID + ":" + userEmail + ":" + "SaveTankMonitorReading" + AppConstants.LANG_PARAM);
                response = serverHandler.PostTextData(BackgroundService_FS_UNIT_6.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {
                CommonUtils.LogMessage("TAG", "SaveTankMonitorReading ", ex);
            }
            return null;
        }
    }

    public String GetMacAddressOfProbe(String level) {
        String MacAddress = "";
        try {
            String[] Seperate = level.split(",");

            for (int i = 0; i < 6; i++) {
                String pd = CommonUtils.decimal2hex(Integer.parseInt(Seperate[i].trim()));
                MacAddress = MacAddress + pd;
            }
            System.out.println("MacAddress of probe: " + MacAddress);
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " GetMacAddressOfProbe ~~~Exception~~" + e);
        }
        return MacAddress;
    }

    public String ConvertToMacAddressFormat(String mac_str) {
        String str = mac_str;
        String mac_address = "";

        List<String> strings = new ArrayList<String>();
        int index = 0;

        while (index < str.length()) {
            strings.add(str.substring(index, Math.min(index + 2, str.length())));

            if (index < 2) {
                mac_address = mac_address + str.substring(index, Math.min(index + 2, str.length()));
            } else {
                mac_address = mac_address + ":" + str.substring(index, Math.min(index + 2, str.length()));
            }
            index += 2;
        }
        return mac_address;
    }

    public String GetProbeReading(String LSB, String MSB) {
        double prove = 0;
        try {
            String lsb_hex = CommonUtils.decimal2hex(Integer.parseInt(LSB));
            String msb_hex = CommonUtils.decimal2hex(Integer.parseInt(MSB));
            String Combine_hex = msb_hex + lsb_hex;
            int finalpd = CommonUtils.hex2decimal(Combine_hex);
            prove = finalpd / 128;
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "GetProbeReading ~~~Exception~~" + e.getMessage());
        }
        return String.valueOf(prove);
    }

    public String CalculateTemperature(String Tem_data) {
        int Temp = (int) ((Integer.parseInt(Tem_data) * 0.48876) - 50);
        return String.valueOf(Temp);
    }

    public void getIpOverOSVersion() {
        listOfConnectedIP.clear();
        if (Build.VERSION.SDK_INT >= 31) {
            GetDetailsFromARP();
        } else if (Build.VERSION.SDK_INT >= 29) {
            ListConnectedHotspotIPOS10_AsyncCall(); // Not working with Android 11 and sdk 31 combination
            //GetDetailsFromARP();
        } else {
            ListConnectedHotspotIP_AsyncCall();
        }
    }

    public synchronized void ListConnectedHotspotIPOS10_AsyncCall() {
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("ip neigh show");
            proc.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;

            while ((line = br.readLine()) != null) {
                //System.out.println("****-"+line);
                String[] splitted = line.split(" ");

                if (splitted != null && splitted.length >= 4) {

                    String ipAddress = splitted[0];
                    String macAddress = splitted[4];

                    if (ipAddress.contains(".") && macAddress.contains(":")) {
                        System.out.println("***IPAddress" + ipAddress);
                        System.out.println("***macAddress" + macAddress);

                        listOfConnectedIP.add("http://" + ipAddress + ":80/");
                        System.out.println("Details Of Connected HotspotIP" + listOfConnectedIP);
                    } else {
                        System.out.println("###IPAddress" + ipAddress);
                        System.out.println("###macAddress" + macAddress);
                    }
                }
            }
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "ListConnectedHotspotIPOS10_AsyncCall ~~Exception~~" + e.getMessage());
        }
    }

    public synchronized void ListConnectedHotspotIP_AsyncCall() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader br = null;
                boolean isFirstLine = true;

                try {
                    br = new BufferedReader(new FileReader("/proc/net/arp"));
                    String line;

                    while ((line = br.readLine()) != null) {
                        if (isFirstLine) {
                            isFirstLine = false;
                            continue;
                        }

                        String[] splitted = line.split(" +");

                        if (splitted != null && splitted.length >= 4) {

                            String ipAddress = splitted[0];
                            String macAddress = splitted[3];
                            System.out.println("IPAddress" + ipAddress);

                            if (ipAddress != null || macAddress != null) {
                                listOfConnectedIP.add("http://" + ipAddress + ":80/");
                                System.out.println("Details Of Connected HotspotIP" + listOfConnectedIP);
                            }
                        }
                    }
                } catch (Exception e) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "ListConnectedHotspotIP_AsyncCall ~~Exception~~" + e.getMessage());
                } finally {
                    try {
                        br.close();
                    } catch (IOException e) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "ListConnectedHotspotIP_AsyncCall (final) ~~Exception~~" + e.getMessage());
                    }
                }
            }
        });
        thread.start();
    }

    private void relayOffCommand() {
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "Sending RELAY OFF command to Link: " + LinkName);
        new CommandsPOST().execute(URL_RELAY, jsonRelayOff, "");
    }

    public void ProceedToPostTransactionCommands() {
        // Free the link and continue to post transaction commands
        StopTransaction(true, false); // Free the link
        GetLastTransaction();
    }

    private void StopTransaction(boolean startBackgroundServices, boolean isTransactionCompleted) {
        AppConstants.IsTransactionCompleted6 = false;
        CommonUtils.AddRemovecurrentTransactionList(false, TransactionId);//Remove transaction Id from list
        IsFuelingStop = "1";
        stopTimer = false;
        CancelTimer();
        Constants.FS_6STATUS = "FREE";
        Constants.FS_6Pulse = "00";
        IsAnyPostTxnCommandExecuted = true;
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "Transaction stopped.");
        if (isTransactionCompleted) {
            CloseTransaction(startBackgroundServices);
        } else {
            PostTransactionBackgroundTasks(false);
        }
    }

    private void CloseTransaction(boolean startBackgroundServices) {
        clearEditTextFields();
        AppConstants.IsTransactionCompleted6 = true;
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "Transaction Completed. \n==============================================================================");
        if (startBackgroundServices) {
            PostTransactionBackgroundTasks(true);
        }
        this.stopSelf();
    }

    private void CancelTimer() {
        try {
            for (int i = 0; i < TimerList.size(); i++) {
                TimerList.get(i).cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void offlineLogic() {
        try {
            getIpOverOSVersion();

            TransactionId = "0";
            PhoneNumber = "0";
            FuelTypeId = "0";
            ServerDate = "0";
            //IsTLDCall = "0";

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String curr_date = AppConstants.currentDateFormat("hhmmss");//"yyyyMMdd hhmmss"
                    System.out.println("curr_date" + curr_date);

                    OffLastTXNid = "99999999";//+sqlite_id+curr_date;
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Sending SET_TXNID (offline) command to Link: " + LinkName);
                    new CommandsPOST().execute(URL_SET_TXNID, "{\"txtnid\":" + OffLastTXNid + "}", "");
                }
            }, 1000);

            EntityOffTranz tzc = offcontroller.getTransactionDetailsBySqliteId(sqlite_id);

            VehicleId = tzc.VehicleId;
            PersonId = tzc.PersonId;
            String siteid = tzc.SiteId;

            HashMap<String, String> linkmap = offcontroller.getLinksDetailsBySiteId(siteid);
            PumpOnTime = linkmap.get("PumpOnTime");
            IntervalToStopFuel = linkmap.get("PumpOffTime");
            PulseRatio = linkmap.get("Pulserratio");
            SiteId = siteid;

            EnablePrinter = offcontroller.getOfflineHubDetails(BackgroundService_FS_UNIT_6.this).EnablePrinter;

            getIpOverOSVersion();

            minFuelLimit = OfflineConstants.getFuelLimit(BackgroundService_FS_UNIT_6.this);
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "<Fuel Limit: " + minFuelLimit + ">");
            numPulseRatio = Double.parseDouble(PulseRatio);

            System.out.println("numPulseRatio-" + numPulseRatio);

            stopAutoFuelSeconds = Long.parseLong(IntervalToStopFuel);

        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "offlineLogic ~~Exception: " + e.getMessage());
        }
    }

    private void SyncOfflineData() {
        if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE") && Constants.FS_5STATUS.equalsIgnoreCase("FREE") && Constants.FS_6STATUS.equalsIgnoreCase("FREE")) {

            if (CurrTxnMode.equalsIgnoreCase("online")) {
                try {
                    //sync offline transactions
                    String off_json = offcontroller.getAllOfflineTransactionJSON(BackgroundService_FS_UNIT_6.this);
                    JSONObject jobj = new JSONObject(off_json);
                    String offtransactionArray = jobj.getString("TransactionsModelsObj");
                    JSONArray jarrsy = new JSONArray(offtransactionArray);

                    if (jarrsy.length() > 0) {
                        startService(new Intent(BackgroundService_FS_UNIT_6.this, OffTranzSyncService.class));
                    }
                } catch (Exception e) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "SyncOfflineData ~~Exception: " + e.getMessage());
                }
            }
        }
    }

    private void StoreLinkDisconnectInfo(SocketException se){
        try {
            //log Date time
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String UseDate = dateFormat.format(cal.getTime());

            String dt = GetDateString(System.currentTimeMillis());
            String  errorfileame = "/Log_" + dt + ".txt";

            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "SocketException " + se);
            SocketErrorEntityClass soc_obj = new SocketErrorEntityClass();
            soc_obj.SiteId =  SiteId;
            soc_obj.LogDateTime = UseDate;
            soc_obj.ErrorLogFileName = errorfileame;///"FSLog/Log_" + dt + ".txt"
            soc_obj.TransactionId = TransactionId;

            Gson gson = new Gson();
            final String jsonData = gson.toJson(soc_obj);

            SharedPreferences sharedPref = BackgroundService_FS_UNIT_6.this.getSharedPreferences(AppConstants.LinkConnectionIssuePref, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("NLINK6", jsonData);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void GetInterruptedTxtnId() {
        ArrayList<String> normalIds = new ArrayList<>();

        String userEmail = CommonUtils.getCustomerDetailsCC(this).PersonEmail;
        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(this) + ":" + userEmail + ":" + "UpdateInterruptedTransactionFlag" + AppConstants.LANG_PARAM);

        SharedPreferences pref = BackgroundService_FS_UNIT_6.this.getSharedPreferences(Constants.PREF_TXTN_INTERRUPTED, 0);
        Map<String, ?> allEntries = pref.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String _txId = entry.getKey();
            String _val = entry.getValue().toString();

            if (_val.equalsIgnoreCase("false")) {
                normalIds.add(_txId);
                Log.d("map values false", _txId + ": " + _val);
            } else if (_val.equalsIgnoreCase("true")) {
                //interrupted txn id----
                Log.d("map values true", _txId + ": " + _val);

                InterruptedTransactions tf = new InterruptedTransactions();
                tf.TransactionId = _txId;
                Gson gson = new Gson();
                String jsonData = gson.toJson(tf);

                new UpdateInterruptedTransactionFlag().execute(_txId, authString, jsonData);
            }
        }

        //delete normal ids
        SharedPreferences.Editor editor = pref.edit();
        if (normalIds != null && normalIds.size() > 0) {
            for (int i = 0; i < normalIds.size(); i++) {
                String _nId = normalIds.get(i);
                editor.remove(_nId);
            }
        }
        editor.commit();
    }

    public class InterruptedTransactions {
        public String TransactionId;
    }

    public class UpdateInterruptedTransactionFlag extends AsyncTask<String, Void, String> {
        public String response = "";
        public String txnId = "";

        @Override
        protected String doInBackground(String... param) {
            try {
                ServerHandler serverHandler = new ServerHandler();

                txnId = param[0];
                String authString = param[1];
                String jsonData = param[2];

                response = serverHandler.PostTextData(BackgroundService_FS_UNIT_6.this, AppConstants.webURL, jsonData, authString);

            } catch (Exception ex) {
                CommonUtils.LogMessage(TAG, " UpdateInterruptedTransactionFlag>>doInBackground ", ex);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                System.out.println("mapValues " + txnId + res);

                JSONObject jsonObject = new JSONObject(res);
                String ResponseMessage = jsonObject.getString("ResponseMessage");
                String ResponseText = jsonObject.getString("ResponseText");

                if (ResponseMessage.equalsIgnoreCase("success")) {
                    deleteInterruptedTxn(txnId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteInterruptedTxn(String txnId) {
        SharedPreferences.Editor editor;
        SharedPreferences pref = BackgroundService_FS_UNIT_6.this.getSharedPreferences(Constants.PREF_TXTN_INTERRUPTED, 0);
        editor = pref.edit();
        editor.remove(txnId);
        editor.commit();
    }

    public void GetDetailsFromARP() {
        try {
            String arpTable = FS_ArpNDK.getARP();
            if (!arpTable.isEmpty()) {
                String[] lines = arpTable.split("\n");
                if (lines.length > 0) {
                    for (String line : lines) {
                        if (!line.isEmpty()) {
                            String[] splitted = line.split(" ");

                            if (splitted != null && splitted.length >= 4) {

                                String ip = splitted[0];
                                String mac = splitted[4];

                                if (ip.contains(".") && mac.contains(":")) {
                                    System.out.println("***IPAddress" + ip);
                                    System.out.println("***macAddress" + mac);

                                    try {
                                        boolean isReachable = new checkIpAddressReachable().execute(ip, "80", "2000").get();
                                        if (!isReachable) {
                                            continue;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    listOfConnectedIP.add("http://" + ip + ":80/");
                                } else {
                                    System.out.println("###IPAddress" + ip);
                                    System.out.println("###macAddress" + mac);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public class checkIpAddressReachable extends AsyncTask<String, Void, Boolean> {
        String address;
        int port, timeout;

        protected Boolean doInBackground(String... urls) {
            try {
                Socket mSocket = new Socket();

                try {
                    address = urls[0];
                    port = Integer.parseInt(urls[1]);
                    timeout = Integer.parseInt(urls[2]);

                    // Connects this socket to the server with a specified timeout value.
                    mSocket.connect(new InetSocketAddress(address, port), timeout);
                    // Return true if connection successful
                    System.out.println(address + " is reachable");
                    return true;
                } catch (IOException exception) {
                    exception.printStackTrace();
                    // Return false if connection fails
                    System.out.println(address + " is not reachable");
                    return false;
                } finally {
                    mSocket.close();
                }
            } catch (Exception e) {
                return false;
            }
        }

        protected void onPostExecute(String res) {
        }
    }

    private void GetLastTransaction() {
        try {
            Thread.sleep(500);
            String resp_value = getCMDLastSingleTXn();
            if (!resp_value.isEmpty()) {
                System.out.println("resp_value" + resp_value);
                String[] raw_string = resp_value.trim().split("-");
                String txnid = raw_string[0];
                String count = raw_string[1];

                if (count.equalsIgnoreCase("N/A")) {
                    count = "0";
                }

                if (!txnid.equals("-1") && !txnid.equals("99999999")) {
                    SaveLastTransactionInLocalDB(txnid, count);
                }
            }
            SET_PULSAR_Command();
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "GetLastTransaction ~~Exception: " + ex.getMessage());
            SET_PULSAR_Command();
        }
    }

    public String getCMDLastSingleTXn() {
        String txn1 = "";
        try {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Sending TXN_LAST10 (to get Last Single Txn) command to Link: " + LinkName);
            String resp = new CommandsGET_CmdTxt10_Single().execute(URL_GET_TXN_LAST10).get();

            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "TXN_LAST10 Response: " + resp);
            if (resp.contains("cmtxtnid_10_record")) {
                JSONObject jobj = new JSONObject(resp);
                JSONObject cm = jobj.getJSONObject("cmtxtnid_10_record");

                txn1 = cm.getString("2:TXTNINFO:"); // This is post transaction command so taking the 2nd position object
            }
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "getCMDLastSingleTXn ~~Exception: " + e.getMessage());
        }
        return txn1;
    }

    public class CommandsGET_CmdTxt10_Single extends AsyncTask<String, Void, String> {
        public String resp = "";
        @Override
        protected void onPreExecute() {
        }
        protected String doInBackground(String... param) {
            try {
                //OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(15, TimeUnit.SECONDS);
                client.setReadTimeout(15, TimeUnit.SECONDS);
                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();
            } catch (Exception e) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "CmdTxt10_Single InBackground Exception " + e.getMessage());
            }
            return resp;
        }
    }

    private void SaveLastTransactionInLocalDB(String txnid, String counts) {
        try {
            IsLastTransaction = "1";
            Pulses = Integer.parseInt(counts);
            double lastCnt = Double.parseDouble(counts);
            double Lastqty = lastCnt / numPulseRatio; //convert to gallons
            Lastqty = AppConstants.roundNumber(Lastqty, 2);

            //-----------------------------------------------
            TrazComp authEntityClass = new TrazComp();
            authEntityClass.TransactionId = txnid;
            authEntityClass.FuelQuantity = Lastqty;
            authEntityClass.Pulses = Pulses;
            authEntityClass.AppInfo = " Version:" + CommonUtils.getVersionCode(BackgroundService_FS_UNIT_6.this) + " " + AppConstants.getDeviceName() + " Android " + Build.VERSION.RELEASE + " " + "--Last Transaction--";
            authEntityClass.TransactionFrom = "A";
            authEntityClass.IsFuelingStop = IsFuelingStop;
            authEntityClass.IsLastTransaction = IsLastTransaction;

            Gson gson = new Gson();
            String jsonData = gson.toJson(authEntityClass);

            String AppInfo = " Version:" + CommonUtils.getVersionCode(BackgroundService_FS_UNIT_6.this) + " " + AppConstants.getDeviceName() + " Android " + Build.VERSION.RELEASE;
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "<Last Transaction saved in local DB. LastTXNid: " + txnid + "; Quantity: " + Lastqty + "; Pulses: " + Pulses + "; AppInfo: " + AppInfo + ">");

            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceHTTP(BackgroundService_FS_UNIT_6.this).PersonEmail;
            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_FS_UNIT_6.this) + ":" + userEmail + ":" + "TransactionComplete" + AppConstants.LANG_PARAM);

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

            if (isInsert && (Pulses > 0 || Lastqty > 0)) {
                controller.insertTransactions(imap);
            }
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "SaveLastTransactionInLocalDB Exception: " + ex.getMessage());
        }
    }

    public void SET_PULSAR_Command() {
        try {
            IsAnyPostTxnCommandExecuted = true;
            SharedPreferences calibrationPref = this.getSharedPreferences(Constants.PREF_CalibrationDetails, Context.MODE_PRIVATE);
            String PulserTimingAdjust = calibrationPref.getString("PulserTimingAdjust_FS6", "");
            String IsResetSwitchTimeBounce = calibrationPref.getString("IsResetSwitchTimeBounce_FS6", "");

            if (IsResetSwitchTimeBounce != null) {
                if (IsResetSwitchTimeBounce.trim().equalsIgnoreCase("1")) {
                    Thread.sleep(500);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Sending SET_PULSAR (sampling_time_ms) command to Link: " + LinkName);
                    new CommandsPOST().execute(URL_SET_PULSAR, "{\"pulsar_status\":{\"sampling_time_ms\":" + PulserTimingAdjust + "}}", "sampling_time");

                } else {
                    CloseTransaction(true);
                }
            } else {
                CloseTransaction(true);
            }
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "SET_PULSAR_Command Exception: " + e.getMessage());
            CloseTransaction(true);
        }
    }

    private void UpdateSwitchTimeBounceForLink() {
        try {
            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceHTTP(BackgroundService_FS_UNIT_6.this).PersonEmail;
            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_FS_UNIT_6.this) + ":" + userEmail + ":" + "UpdateSwitchTimeBounceForLink" + AppConstants.LANG_PARAM);

            SwitchTimeBounce switchTimeBounce = new SwitchTimeBounce();
            switchTimeBounce.SiteId = SiteId;
            switchTimeBounce.IsResetSwitchTimeBounce = "0";

            Gson gson = new Gson();
            String jsonData = gson.toJson(switchTimeBounce);

            SharedPreferences pref;
            SharedPreferences.Editor editor;

            pref = this.getSharedPreferences("storeSwitchTimeBounceFlag6", 0);
            editor = pref.edit();

            // Storing
            editor.putString("jsonData", jsonData);
            editor.putString("authString", authString);

            // commit changes
            editor.commit();
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "UpdateSwitchTimeBounceForLink Exception: " + ex.getMessage());
        }
    }
}
