package com.TrakEngineering.FluidSecureHub.BTSPP;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.TrakEngineering.FluidSecureHub.AppConstants;
import com.TrakEngineering.FluidSecureHub.BackgroundService;
import com.TrakEngineering.FluidSecureHub.CommonUtils;
import com.TrakEngineering.FluidSecureHub.ConnectionDetector;
import com.TrakEngineering.FluidSecureHub.Constants;
import com.TrakEngineering.FluidSecureHub.DBController;
import com.TrakEngineering.FluidSecureHub.R;
import com.TrakEngineering.FluidSecureHub.WelcomeActivity;
import com.TrakEngineering.FluidSecureHub.enity.RenameHose;
import com.TrakEngineering.FluidSecureHub.enity.SwitchTimeBounce;
import com.TrakEngineering.FluidSecureHub.enity.TrazComp;
import com.TrakEngineering.FluidSecureHub.enity.UpgradeVersionEntity;
import com.TrakEngineering.FluidSecureHub.offline.EntityOffTranz;
import com.TrakEngineering.FluidSecureHub.offline.OffDBController;
import com.TrakEngineering.FluidSecureHub.offline.OffTranzSyncService;
import com.TrakEngineering.FluidSecureHub.offline.OfflineConstants;
import com.TrakEngineering.FluidSecureHub.server.ServerHandler;
import com.TrakEngineering.FluidSecureHub.WifiHotspot.WifiApManager;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BackgroundService_BTOne extends Service {

    private static final String TAG = AppConstants.LOG_TXTN_BT + "-"; // + BackgroundService_BTOne.class.getSimpleName();
    public long sqlite_id = 0;
    String TransactionId, VehicleId, PhoneNumber, PersonId, PulseRatio, MinLimit, FuelTypeId, ServerDate, IntervalToStopFuel, IsTLDCall, EnablePrinter, PumpOnTime,VehicleNumber,TransactionDateWithFormat;
    public BroadcastBlueLinkOneData broadcastBlueLinkOneData = null;
    String Request = "", Response = "";
    String FDRequest = "", FDResponse = "";
    String upgradeResponse = "";
    int PreviousRes = 0;
    boolean stopTxtprocess, redpulseloop_on, RelayStatus;
    int pulseCount = 0;
    int stopCount = 0;
    int RespCount = 0; //, LinkResponseCount = 0;
    int fdCheckCount = 0;
    long stopAutoFuelSeconds = 0;
    Integer Pulses = 0;
    Integer pre_pulse = 0;
    double fillqty = 0, numPulseRatio = 0, minFuelLimit = 0;
    long sqliteID = 0;
    String CurrentLinkMac = "", LinkCommunicationType = "", SERVER_IP = "", LinkName = "", printReceipt = "", IsFuelingStop = "0", IsLastTransaction = "0", OverrideQuantity = "0", OverridePulse = "0";
    Timer timerBt1;
    List<Timer> TimerList_ReadpulseBT1 = new ArrayList<Timer>();
    DBController controller = new DBController(BackgroundService_BTOne.this);
    Boolean IsThisBTTrnx;
    boolean isBroadcastReceiverRegistered = false;
    String OffLastTXNid = "0";
    ConnectionDetector cd = new ConnectionDetector(BackgroundService_BTOne.this);
    OffDBController offlineController = new OffDBController(BackgroundService_BTOne.this);
    String ipForUDP = "192.168.4.1";
    public int infoCommandAttempt = 0;
    public boolean isConnected = false;

    SimpleDateFormat sdformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    ArrayList<HashMap<String, String>> quantityRecords = new ArrayList<>();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            Bundle extras = intent.getExtras();
            if (extras == null) {
                this.stopSelf();
                CloseTransaction(false);
            } else {
                sqlite_id = (long) extras.get("sqlite_id");
                SERVER_IP = String.valueOf(extras.get("SERVER_IP"));
                Request = "";
                Request = "";
                stopCount = 0;
                Log.i(TAG, "-Started-");
                if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + " BTLink 1: -Started-");

                Constants.FS_1STATUS = "BUSY";

                SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
                TransactionId = sharedPref.getString("TransactionId_FS1", "");
                VehicleId = sharedPref.getString("VehicleId_FS1", "");
                VehicleNumber = sharedPref.getString("VehicleNumber_FS1", "");
                PhoneNumber = sharedPref.getString("PhoneNumber_FS1", "");
                PersonId = sharedPref.getString("PersonId_FS1", "");
                PulseRatio = sharedPref.getString("PulseRatio_FS1", "1");
                MinLimit = sharedPref.getString("MinLimit_FS1", "0");
                FuelTypeId = sharedPref.getString("FuelTypeId_FS1", "");
                ServerDate = sharedPref.getString("ServerDate_FS1", "");
                TransactionDateWithFormat = sharedPref.getString("TransactionDateWithFormat_FS1", "");
                IntervalToStopFuel = sharedPref.getString("IntervalToStopFuel_FS1", "0");
                IsTLDCall = sharedPref.getString("IsTLDCall_FS1", "False");
                EnablePrinter = sharedPref.getString("EnablePrinter_FS1", "False");
                PumpOnTime = sharedPref.getString("PumpOnTime_FS1", "0");

                numPulseRatio = Double.parseDouble(PulseRatio);
                minFuelLimit = Double.parseDouble(MinLimit);
                stopAutoFuelSeconds = Long.parseLong(IntervalToStopFuel);

                //UDP Connection..!!
                if (WelcomeActivity.serverSSIDList != null && WelcomeActivity.serverSSIDList.size() > 0) {
                    LinkCommunicationType = WelcomeActivity.serverSSIDList.get(WelcomeActivity.SelectedItemPos).get("LinkCommunicationType");
                    CurrentLinkMac = WelcomeActivity.serverSSIDList.get(WelcomeActivity.SelectedItemPos).get("MacAddress");
                }

                // Offline functionality
                if (!cd.isConnectingToInternet()) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink 1:-Offline mode--");
                    offlineLogicBT1();
                }

                //Register Broadcast receiver
                broadcastBlueLinkOneData = new BroadcastBlueLinkOneData();
                IntentFilter intentFilter = new IntentFilter("BroadcastBlueLinkOneData");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1: <Registering Broadcast Receiver.>");
                registerReceiver(broadcastBlueLinkOneData, intentFilter);
                isBroadcastReceiverRegistered = true;
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1: <Registered successfully. (" + broadcastBlueLinkOneData + ")>");

                AppConstants.isRelayON_fs1 = false;
                LinkName = CommonUtils.getlinkName(0);
                if (LinkCommunicationType.equalsIgnoreCase("BT")) {
                    IsThisBTTrnx = true;

                    checkBTLinkStatus("upgrade");

                } else if (LinkCommunicationType.equalsIgnoreCase("UDP")) {
                    IsThisBTTrnx = false;
                    infoCommand();
                    //BeginProcessUsingUDP();
                } else {
                    //Something went Wrong in hose selection.
                    IsThisBTTrnx = false;
                    Log.i(TAG, " BTLink 1: Something went Wrong in hose selection.");
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink 1: Something went wrong in hose selection.");
                    CloseTransaction(false);
                    this.stopSelf();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Service.START_NOT_STICKY;
    }

    private void UDPFunctionalityAfterBTFailure() {
        try {
            if (CommonUtils.CheckAllHTTPLinksAreFree()) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1: Link not connected. Switching to UDP connection...");

                // Enable Wi-Fi
                WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                wifiManagerMM.setWifiEnabled(true);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        IsThisBTTrnx = false;
                        BTConstants.SwitchedBTToUDP1 = true;
                        BeginProcessUsingUDP();
                    }
                }, 5000);
            } else {
                TerminateBTTransaction();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void BeginProcessUsingUDP() {
        try {
            Toast.makeText(BackgroundService_BTOne.this, getResources().getString(R.string.PleaseWaitForWifiConnect), Toast.LENGTH_SHORT).show();

            new CountDownTimer(12000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink 1: Connecting to WiFi...");
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    String ssid = "";
                    if (wifiManager.isWifiEnabled()) {
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        ssid = wifiInfo.getSSID();
                    }

                    ssid = ssid.replace("\"", "");

                    if (ssid.equalsIgnoreCase(LinkName)) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 1: Connected to " + ssid + " via WiFi.");
                        proceedToInfoCommand(false);
                        //loading.cancel();
                        cancel();
                    }
                }

                @Override
                public void onFinish() {
                    //loading.dismiss();
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String ssid = wifiInfo.getSSID();

                    ssid = ssid.replace("\"", "");
                    if (ssid.equalsIgnoreCase(LinkName)) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 1: Connected to " + ssid + " via WiFi.");
                        proceedToInfoCommand(false);
                        //loading.cancel();
                        cancel();
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 1: Unable to connect to " + LinkName + " via WiFi.");
                        TerminateBTTransaction();
                    }
                }
            }.start();
            //proceedToInfoCommand(false);
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: Exception in BeginProcessUsingUDP: " + e.getMessage());
            TerminateBTTransaction();
            e.printStackTrace();
        }
    }

    private void TerminateBTTransaction() {
        try {
            IsThisBTTrnx = false;
            CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "6", BackgroundService_BTOne.this);
            Log.i(TAG, " BTLink 1: Link not connected. Please try again!");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: Link not connected.");
            AppConstants.TxnFailedCount1++;
            AppConstants.IsTransactionFailed1 = true;
            CloseTransaction(true);
            this.stopSelf();
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: Exception in TerminateBTTransaction: " + e.getMessage());
        }
    }

    public void proceedToInfoCommand(boolean proceedAfterUpgrade) {
        try {
            if (proceedAfterUpgrade) {
                checkBTLinkStatus("info");
                /*if (checkBTLinkStatus(false)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            infoCommand();
                        }
                    }, 2000);
                } else {
                    TerminateBTTransaction();
                }*/
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        infoCommand();
                    }
                }, 2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkBTLinkStatus(String nextAction) {
        try {
            new CountDownTimer(10000, 2000) {
                public void onTick(long millisUntilFinished) {
                    if (BTConstants.BTStatusStrOne.equalsIgnoreCase("Connected")) {
                        isConnected = true;
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 1: Link is connected.");
                        if (nextAction.equalsIgnoreCase("upgrade")) { // Proceed to upgrade check
                            BTLinkUpgradeCheck();
                        } else if (nextAction.equalsIgnoreCase("info")) { // proceed to info command after upgrade is done
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    infoCommand();
                                }
                            }, 2000);
                        } else if (nextAction.equalsIgnoreCase("relay")) { // proceed to relayOn command after reconnect
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    stopCount = 0;
                                    relayOnCommand(true);
                                }
                            }, 2000);
                        }
                        cancel();
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 1: Checking Connection Status...");
                    }
                }

                public void onFinish() {

                    if (BTConstants.BTStatusStrOne.equalsIgnoreCase("Connected")) {
                        isConnected = true;
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 1: Link is connected.");
                        if (nextAction.equalsIgnoreCase("upgrade")) { // Proceed to upgrade check
                            BTLinkUpgradeCheck();
                        } else if (nextAction.equalsIgnoreCase("info")) { // proceed to info command after upgrade is done
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    infoCommand();
                                }
                            }, 2000);
                        } else if (nextAction.equalsIgnoreCase("relay")) { // proceed to relayOn command after reconnect
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    stopCount = 0;
                                    relayOnCommand(true);
                                }
                            }, 2000);
                        }
                    } else {
                        isConnected = false;
                        if (nextAction.equalsIgnoreCase("upgrade")) { // Proceed to UDP functionality
                            UDPFunctionalityAfterBTFailure();
                        } else if (nextAction.equalsIgnoreCase("info")) { // Terminate BT Transaction
                            TerminateBTTransaction();
                        } else if (nextAction.equalsIgnoreCase("relay")) { // Terminate BT Txn After Interruption
                            TerminateBTTxnAfterInterruption();
                        }
                    }
                }
            }.start();

            /*if (BTConstants.BTStatusStrOne.equalsIgnoreCase("Connected")) {
                isConnected = true;
            } else {
                Thread.sleep(1000);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1: Checking Connection Status...");
                if (BTConstants.BTStatusStrOne.equalsIgnoreCase("Connected")) {
                    isConnected = true;
                } else {
                    Thread.sleep(2000);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink 1: Checking Connection Status...");
                    if (BTConstants.BTStatusStrOne.equalsIgnoreCase("Connected")) {
                        isConnected = true;
                    } else {
                        Thread.sleep(2000);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 1: Checking Connection Status...");
                        if (BTConstants.BTStatusStrOne.equalsIgnoreCase("Connected")) {
                            isConnected = true;
                        } else if (isAfterRelayOn) {
                            Thread.sleep(2000);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink 1: Checking Connection Status...");
                            if (BTConstants.BTStatusStrOne.equalsIgnoreCase("Connected")) {
                                isConnected = true;
                            }
                        }
                    }
                }
            }
            if (isConnected) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1: Link is connected.");
            }*/
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: checkBTLinkStatus Exception:>>" + e.getMessage());
            if (nextAction.equalsIgnoreCase("info")) { // Terminate BT Transaction
                TerminateBTTransaction();
            } else if (nextAction.equalsIgnoreCase("relay")) { // Terminate BT Txn After Interruption
                TerminateBTTxnAfterInterruption();
            }
        }
        //return isConnected;
    }

    private void infoCommand() {

        try {
            if (BTConstants.IsFileUploadCompleted) {
                BTConstants.IsFileUploadCompleted = false;
            }
            BTConstants.isNewVersionLinkOne = false;
            AppConstants.TxnFailedCount1 = 0;
            //Execute info command
            Request = "";
            Response = "";
            if (IsThisBTTrnx) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1: Sending Info command to Link: " + LinkName);
                BTSPPMain btspp = new BTSPPMain();
                btspp.send1(BTConstants.info_cmd);
            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1: Sending Info command (UDP) to Link: " + LinkName);
                new Thread(new ClientSendAndListenUDPOne(BTConstants.info_cmd, ipForUDP, this)).start();
            }
            //Thread.sleep(1000);
            new CountDownTimer(5000, 1000) {

                public void onTick(long millisUntilFinished) {
                    long attempt = (5 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (Request.equalsIgnoreCase(BTConstants.info_cmd) && !Response.equalsIgnoreCase("")) {
                            //Info command success.
                            Log.i(TAG, "BTLink 1: InfoCommand Response success 1:>>" + Response);

                            if (!TransactionId.isEmpty()) {
                                if (Response.contains("records") && Response.contains("mac_address")) {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " BTLink 1: Checking Info command response. Response: true");
                                    BTConstants.isNewVersionLinkOne = true;
                                    parseInfoCommandResponseForLast20txtn(Response); // parse last 20 Txtn
                                    Response = "";
                                } else {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " BTLink 1: Checking Info command response. Response:>>" + Response.trim());
                                    parseInfoCommandResponseForLast10txtn(Response.trim()); // parse last 10 Txtn
                                }
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (IsThisBTTrnx && BTConstants.isNewVersionLinkOne) {
                                            P_Type_Command();
                                        } else {
                                            transactionIdCommand(TransactionId);
                                        }
                                    }
                                }, 1000);
                            } else {
                                Log.i(TAG, "BTLink 1: TransactionId is empty.");
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BTLink 1: TransactionId is empty.");
                            }
                            cancel();
                        } else {
                            Log.i(TAG, "BTLink 1: Waiting for infoCommand Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink 1: Checking Info command response. Response: false");
                        }
                    }
                }

                public void onFinish() {

                    if (Request.equalsIgnoreCase(BTConstants.info_cmd) && !Response.equalsIgnoreCase("")) {
                        //Info command success.
                        Log.i(TAG, "BTLink 1: InfoCommand Response success 2:>>" + Response);

                        if (!TransactionId.isEmpty()) {
                            if (Response.contains("records") && Response.contains("mac_address")) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BTLink 1: Checking Info command response. Response: true");
                                BTConstants.isNewVersionLinkOne = true;
                                parseInfoCommandResponseForLast20txtn(Response); // parse last 20 Txtn
                                Response = "";
                            } else {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BTLink 1: Checking Info command response. Response:>>" + Response.trim());
                                parseInfoCommandResponseForLast10txtn(Response.trim()); // parse last 10 Txtn
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (IsThisBTTrnx && BTConstants.isNewVersionLinkOne) {
                                        P_Type_Command();
                                    } else {
                                        transactionIdCommand(TransactionId);
                                    }
                                }
                            }, 1000);
                        } else {
                            Log.i(TAG, "BTLink 1: TransactionId is empty.");
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink 1: TransactionId is empty.");
                            CloseTransaction(false);
                        }
                    } else {

                        if (infoCommandAttempt > 0) {
                            //UpgradeTransaction Status info command fail.
                            CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "6", BackgroundService_BTOne.this);
                            Log.i(TAG, "BTLink 1: Failed to get infoCommand Response:>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink 1: Checking Info command response. Response: false");
                            AppConstants.TxnFailedCount1++;
                            AppConstants.IsTransactionFailed1 = true;
                            CloseTransaction(true);
                        } else {
                            infoCommandAttempt++;
                            infoCommand(); // Retried one more time after failed to receive response from info command
                        }
                    }
                }
            }.start();

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: infoCommand Exception:>>" + e.getMessage());
        }
    }

    private void P_Type_Command() {
        try {
            if (AppConstants.IsResetSwitchTimeBounce != null) {
                if (AppConstants.IsResetSwitchTimeBounce.trim().equalsIgnoreCase("1") && !AppConstants.PulserTimingAdjust.isEmpty() && Arrays.asList(BTConstants.p_types).contains(AppConstants.PulserTimingAdjust) && !CommonUtils.CheckP_TypeCommandIsSent(BackgroundService_BTOne.this, "storeSwitchTimeBounceFlag1")) {
                    //Execute p_type Command
                    Request = "";
                    Response = "";

                    if (IsThisBTTrnx) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 1: Sending p_type command to Link: " + LinkName);
                        BTSPPMain btspp = new BTSPPMain();
                        btspp.send1(BTConstants.p_type_command + AppConstants.PulserTimingAdjust);
                    }

                    new CountDownTimer(4000, 1000) {

                        public void onTick(long millisUntilFinished) {

                            long attempt = (4 - (millisUntilFinished / 1000));
                            if (attempt > 0) {
                                if (Request.contains(BTConstants.p_type_command) && Response.contains("pulser_type")) {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " BTLink 1: Checking p_type command response:>> " + Response);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            BTConstants.isPTypeCommandExecuted1 = true;
                                            AppConstants.IsResetSwitchTimeBounce = "0";
                                            UpdateSwitchTimeBounceForLink();
                                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    WaitForReconnectToLink();
                                                }
                                            }, 1000);
                                        }
                                    }, 8000); // Tried to reconnect and continue after 8 seconds because the link disconnects after 8 seconds.
                                    cancel();
                                } else {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " BTLink 1: Checking p_type command response. Response: false");
                                }
                            }
                        }

                        public void onFinish() {

                            if (Request.contains(BTConstants.p_type_command) && Response.contains("pulser_type")) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BTLink 1: Checking p_type command response:>> " + Response);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        BTConstants.isPTypeCommandExecuted1 = true;
                                        AppConstants.IsResetSwitchTimeBounce = "0";
                                        UpdateSwitchTimeBounceForLink();
                                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                WaitForReconnectToLink();
                                            }
                                        }, 1000);
                                    }
                                }, 8000); // Tried to reconnect and continue after 8 seconds because the link disconnects after 8 seconds.
                            } else {
                                transactionIdCommand(TransactionId); // Continue to transactionId Command
                            }
                        }
                    }.start();
                } else {
                    transactionIdCommand(TransactionId); // Continue to transactionId Command
                }
            } else {
                transactionIdCommand(TransactionId); // Continue to transactionId Command
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: P_Type_Command Exception:>>" + e.getMessage());
            transactionIdCommand(TransactionId); // Continue to transactionId Command
        }
    }

    public void WaitForReconnectToLink() {
        try {
            new CountDownTimer(10000, 1000) {

                public void onTick(long millisUntilFinished) {

                    long attempt = (10 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (BTConstants.BTStatusStrOne.equalsIgnoreCase("Connected")) {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink 1: Connected to Link: " + LinkName);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    transactionIdCommand(TransactionId); // Continue to transactionId Command
                                }
                            }, 500);
                            cancel();
                        } else {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink 1: Waiting for Reconnect to Link: " + LinkName);
                        }
                    }
                }

                public void onFinish() {

                    if (BTConstants.BTStatusStrOne.equalsIgnoreCase("Connected")) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 1: Connected to Link: " + LinkName);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                transactionIdCommand(TransactionId); // Continue to transactionId Command
                            }
                        }, 500);
                    } else {
                        TerminateBTTransaction();
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: WaitForReconnectToLink Exception:>>" + e.getMessage());
        }
    }

    private void transactionIdCommand(String transactionId) {

        try {
            //Execute transactionId Command
            Request = "";
            Response = "";

            String transaction_id_cmd = BTConstants.transaction_id_cmd; //LK_COMM=txtnid:

            if (BTConstants.isNewVersionLinkOne) {
                TransactionDateWithFormat = BTConstants.parseDateForNewVersion(TransactionDateWithFormat);
                transaction_id_cmd = transaction_id_cmd.replace("txtnid:", ""); // For New version LK_COMM=T:XXXXX;D:XXXXX;V:XXXXXXXX;
                transaction_id_cmd = transaction_id_cmd + "T:" + transactionId + ";D:" + TransactionDateWithFormat + ";V:" + VehicleNumber + ";";
            } else {
                transaction_id_cmd = transaction_id_cmd + transactionId;
            }

            if (IsThisBTTrnx) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1: Sending transactionId command to Link: " + LinkName);
                BTSPPMain btspp = new BTSPPMain();
                btspp.send1(transaction_id_cmd);
            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1: Sending transactionId command (UDP) to Link: " + LinkName);
                new Thread(new ClientSendAndListenUDPOne(transaction_id_cmd, ipForUDP, this)).start();
            }
            Thread.sleep(1000);
            new CountDownTimer(4000, 1000) {

                public void onTick(long millisUntilFinished) {
                    long attempt = (4 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        try {

                            if (Request.contains(transactionId) && Response.contains(transactionId)) {
                                //transactionId command success.
                                Log.i(TAG, "BTLink 1: transactionId Command Response success 1:>>" + Response);
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BTLink 1: Checking transactionId command response. Response:>>" + Response.trim());
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        relayOnCommand(false); //RelayOn
                                    }
                                }, 1000);
                                cancel();
                            } else {
                                Log.i(TAG, "BTLink 1: Waiting for transactionId Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BTLink 1: Checking transactionId command response. Response: false");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink 1: transactionId command Exception. Exception: " + e.getMessage());
                        }
                    }
                }

                public void onFinish() {

                    if (Request.contains(transactionId) && Response.contains(transactionId)) {
                        //transactionId command success.
                        Log.i(TAG, "BTLink 1: transactionId Command Response success 2:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 1: Checking transactionId command response. Response:>>" + Response.trim());
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                relayOnCommand(false); //RelayOn
                            }
                        }, 1000);
                    } else {

                        //UpgradeTransaction Status Transactionid command fail.
                        CommonUtils.UpgradeTransactionStatusToSqlite(transactionId, "6", BackgroundService_BTOne.this);
                        Log.i(TAG, "BTLink 1: Failed to get transactionId Command Response:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 1: Checking transactionId command response. Response: false");
                        CloseTransaction(true);
                    }
                }
            }.start();

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: transactionIdCommand Exception:>>" + e.getMessage());
        }
    }

    private void relayOnCommand(boolean isAfterReconnect) {
        try {
            if (isAfterReconnect) {
                BTConstants.isReconnectCalled1 = false;
            }
            //Execute relayOn Command
            Request = "";
            Response = "";

            if (IsThisBTTrnx) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1: Sending relayOn command to Link: " + LinkName);
                BTSPPMain btspp = new BTSPPMain();
                btspp.send1(BTConstants.relay_on_cmd);
            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1: Sending relayOn command (UDP) to Link: " + LinkName);
                new Thread(new ClientSendAndListenUDPOne(BTConstants.relay_on_cmd, ipForUDP, this)).start();
            }

            if (!isAfterReconnect) {
                InsertInitialTransactionToSqlite();//Insert empty transaction into sqlite
            }

            Thread.sleep(1000);
            new CountDownTimer(4000, 1000) {

                public void onTick(long millisUntilFinished) {

                    long attempt = (4 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (RelayStatus) {
                            BTConstants.isRelayOnAfterReconnect1 = isAfterReconnect;
                            //relayOn command success.
                            Log.i(TAG, "BTLink 1: relayOn Command Response success 1:>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink 1: Checking relayOn command response. Response: ON");
                            cancel();
                        } else {
                            Log.i(TAG, "BTLink 1: Waiting for relayOn Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink 1: Checking relayOn command response. Response: false");
                        }
                    }
                }

                public void onFinish() {

                    if (RelayStatus) {
                        BTConstants.isRelayOnAfterReconnect1 = isAfterReconnect;
                        //relayOn command success.
                        Log.i(TAG, "BTLink 1: relayOn Command Response success 2:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 1: Checking relayOn command response. Response: ON");
                    } else {

                        //UpgradeTransaction Status RelayON command fail.
                        CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "6", BackgroundService_BTOne.this);
                        Log.i(TAG, "BTLink 1: Failed to get relayOn Command Response:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 1: Checking relayOn command response. Response: false");
                        relayOffCommand(); //RelayOff
                        TransactionCompleteFunction();
                        CloseTransaction(false);
                    }
                }

            }.start();

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: relayOnCommand Exception:>>" + e.getMessage());
        }
    }

    private void CloseFDcheck() {

        try {
            unregisterReceiver(broadcastBlueLinkOneData);
            stopTxtprocess = true;
            Constants.FS_1STATUS = "FREE";
            Constants.FS_1Pulse = "00";
            CancelTimer();
            this.stopSelf();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void relayOffCommand() {

        try {
            //Execute relayOff Command
            Request = "";
            Response = "";
            if (IsThisBTTrnx) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1: Sending relayOff command to Link: " + LinkName);
                BTSPPMain btspp = new BTSPPMain();
                btspp.send1(BTConstants.relay_off_cmd);
            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1: Sending relayOff command (UDP) to Link: " + LinkName);
                new Thread(new ClientSendAndListenUDPOne(BTConstants.relay_off_cmd, ipForUDP, this)).start();
            }

            new CountDownTimer(4000, 1000) {

                public void onTick(long millisUntilFinished) {
                    long attempt = (4 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (!RelayStatus) {
                            //relayOff command success.
                            Log.i(TAG, "BTLink 1: relayOff Command Response success 1:>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink 1: Checking relayOff command response. Response:>>" + Response.trim());
                            cancel();
                        } else {
                            Log.i(TAG, "BTLink 1: Waiting for relayOff Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink 1: Checking relayOff command response. Response: false");
                        }
                    }
                }

                public void onFinish() {

                    if (!RelayStatus) {
                        Log.i(TAG, "BTLink 1: relayOff Command Response success 2:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 1: Checking relayOff command response. Response:>>" + Response.trim());
                    } else {
                        Log.i(TAG, "BTLink 1: Failed to get relayOff Command Response:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 1: Checking relayOff command response. Response: false");
                        PostTransactionBackgroundTasks();
                        //CloseTransaction();
                    }
                }
            }.start();

        } catch (Exception e) {
            e.printStackTrace();
            //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "BTLink 1: relayOffCommand Exception:>>" + e.getMessage());
        }
    }

    private void CloseTransaction(boolean startBackgroundServices) {

        try {
            clearEditTextFields();
            try {
                if (isBroadcastReceiverRegistered) {
                    unregisterReceiver(broadcastBlueLinkOneData);
                    isBroadcastReceiverRegistered = false;
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink 1: <Receiver unregistered successfully. (" + broadcastBlueLinkOneData + ")>");
                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink 1: <Receiver is not registered. (" + broadcastBlueLinkOneData + ")>");
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1: <Exception occurred while unregistering receiver: " + e.getMessage() + " (" + broadcastBlueLinkOneData + ")>");
            }
            stopTxtprocess = true;
            BTConstants.isRelayOnAfterReconnect1 = false;
            AppConstants.clearSharedPrefByName(BackgroundService_BTOne.this, "LastQuantity_BT1");
            CommonUtils.AddRemovecurrentTransactionList(false, TransactionId);
            Constants.FS_1STATUS = "FREE";
            Constants.FS_1Pulse = "00";
            AppConstants.GoButtonAlreadyClicked = false;
            AppConstants.IsTransactionCompleted = true;
            BTConstants.SwitchedBTToUDP1 = false;
            DisableWifiConnection();
            CancelTimer();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: Transaction stopped.");
            if (startBackgroundServices) {
                PostTransactionBackgroundTasks();
            }
            this.stopSelf();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: CloseTransaction Exception:>>" + e.getMessage());
        }
    }

    private void clearEditTextFields() {

        Constants.AccVehicleNumber_FS1 = "";
        Constants.AccOdoMeter_FS1 = 0;
        Constants.AccDepartmentNumber_FS1 = "";
        Constants.AccPersonnelPIN_FS1 = "";
        Constants.AccOther_FS1 = "";
        Constants.AccVehicleOther_FS1 = "";
        Constants.AccHours_FS1 = 0;

    }

    private void renameOnCommand() {
        try {
            //Execute rename Command
            Request = "";
            Response = "";

            if (IsThisBTTrnx) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1: Sending rename command to Link: " + LinkName + " (New Name: " + BTConstants.BT1REPLACEBLE_WIFI_NAME + ")");
                BTSPPMain btspp = new BTSPPMain();
                btspp.send1(BTConstants.namecommand + BTConstants.BT1REPLACEBLE_WIFI_NAME);
            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1: Sending rename command (UDP) to Link: " + LinkName + " (New Name: " + BTConstants.BT1REPLACEBLE_WIFI_NAME + ")");
                new Thread(new ClientSendAndListenUDPOne(BTConstants.namecommand + BTConstants.BT1REPLACEBLE_WIFI_NAME, ipForUDP, this)).start();
            }

            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTOne.this).PersonEmail;
            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(this) + ":" + userEmail + ":" + "SetHoseNameReplacedFlag" + AppConstants.LANG_PARAM);

            RenameHose rhose = new RenameHose();
            rhose.SiteId = BTConstants.BT1SITE_ID;
            rhose.HoseId = BTConstants.BT1HOSE_ID;
            rhose.IsHoseNameReplaced = "Y";

            Gson gson = new Gson();
            String jsonData = gson.toJson(rhose);

            storeIsRenameFlag(this, BTConstants.BT1NeedRename, jsonData, authString);

            Thread.sleep(1000);
            PostTransactionBackgroundTasks();

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: renameCommand Exception:>>" + e.getMessage());
        }
    }

    public void storeIsRenameFlag(Context context, boolean flag, String jsonData, String authString) {
        SharedPreferences pref;

        SharedPreferences.Editor editor;
        pref = context.getSharedPreferences("storeIsRenameFlagFS1", 0);
        editor = pref.edit();

        // Storing
        editor.putBoolean("flag", flag);
        editor.putString("jsonData", jsonData);
        editor.putString("authString", authString);

        // commit changes
        editor.commit();

    }

    private void CancelTimer() {

        try {
            for (int i = 0; i < TimerList_ReadpulseBT1.size(); i++) {
                TimerList_ReadpulseBT1.get(i).cancel();
            }
            redpulseloop_on = false;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void ReadPulse() {

        //Record pulse start time..for puls
        Date currDT = new Date();
        String strCurDT = sdformat.format(currDT);
        HashMap<String, String> hmap = new HashMap<>();
        hmap.put("a", "outputQuantity");
        hmap.put("b", strCurDT);
        quantityRecords.add(hmap);
        PreviousRes = 0;
        redpulseloop_on = true;
        timerBt1 = new Timer();
        TimerList_ReadpulseBT1.add(timerBt1);
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                //Repaeting code..
                //CancelTimer(); cancel all once done.

                Log.i(TAG, "BTLink 1: Timer count..");

                String checkPulses;
                if (BTConstants.isNewVersionLinkOne) {
                    checkPulses = "pulse";
                } else {
                    checkPulses = "pulse:";
                }

                if (BTConstants.isReconnectCalled1 && !BTConstants.isRelayOnAfterReconnect1) {
                    CancelTimer();
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkBTLinkStatus("relay");
                        }
                    }, 100);
                    /*if (checkBTLinkStatus(true)) {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                stopCount = 0;
                                relayOnCommand(true);
                            }
                        }, 2000);
                    } else {
                        TerminateBTTxnAfterInterruption();
                    }*/
                    return;
                }

                CheckResponse(checkPulses);

                if (Response.contains(checkPulses) && RelayStatus) {
                    pulseCount = 0;
                    pulseCount();

                } else if (!RelayStatus) {
                    if (pulseCount > 1) { // pulseCount > 4
                        //Stop transaction
                        pulseCount();
                        /*Log.i(TAG, "BTLink 1: Transaction stopped.");
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 1: Transaction stopped.");*/

                        int delay = 100;
                        cancel();
                        if (BTConstants.SwitchedBTToUDP1) {
                            DisableWifiConnection();
                            BTConstants.SwitchedBTToUDP1 = false;
                            delay = 1000;
                        }
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                TransactionCompleteFunction();
                                CloseTransaction(false);
                            }
                        }, delay);

                    } else {
                        pulseCount++;
                        pulseCount();
                        Log.i(TAG, "BTLink 1: Check pulse");
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 1: Check pulse >> Response: " + Response.trim());
                    }
                }
            }
        };
        timerBt1.schedule(tt, 1000, 1000);
    }

    private void TerminateBTTxnAfterInterruption() {
        try {
            IsThisBTTrnx = false;
            CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "6", BackgroundService_BTOne.this);
            Log.i(TAG, " BTLink 1: Link not connected. Please try again!");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: Link not connected.");
            BTConstants.isReconnectCalled1 = false;
            AppConstants.TxnFailedCount1++;
            AppConstants.IsTransactionFailed1 = true;
            CloseTransaction(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void DisableWifiConnection() {
        try {
            //Disable wifi connection
            WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            if (wifiManagerMM.isWifiEnabled()) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " <Disabling wifi.>");
                wifiManagerMM.setWifiEnabled(false);
            }
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (BTConstants.isHotspotDisabled) {
                        //Enable Hotspot
                        WifiApManager wifiApManager = new WifiApManager(BackgroundService_BTOne.this);
                        if (!CommonUtils.isHotspotEnabled(BackgroundService_BTOne.this) && !AppConstants.isAllLinksAreBTLinks) {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "<Enabling hotspot.>");
                            wifiApManager.setWifiApEnabled(null, true);
                        }
                        BTConstants.isHotspotDisabled = false;
                    }
                    if (cd.isConnectingToInternet()) {
                        boolean BSRunning = CommonUtils.checkServiceRunning(BackgroundService_BTOne.this, AppConstants.PACKAGE_BACKGROUND_SERVICE);
                        if (!BSRunning) {
                            startService(new Intent(BackgroundService_BTOne.this, BackgroundService.class));
                        }
                    }
                }
            }, 2000);
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: DisableWifiConnection Exception>> " + e.getMessage());
        }
    }

    private void pulseCount() {

        try {
            pumpTimingsOnOffFunction();//PumpOn/PumpOff functionality
            String outputQuantity;

            if (BTConstants.isNewVersionLinkOne) {
                if (Response.contains("pulse")) {
                    JSONObject jsonObj = new JSONObject(Response);
                    outputQuantity = jsonObj.getString("pulse");
                } else {
                    return;
                }
            } else {
                String[] items = Response.trim().split(":");
                if (items.length > 1) {
                    outputQuantity = items[1].replaceAll("\"", "").trim();
                } else {
                    // response is "OFF" after relay_off_cmd
                    return;
                }
            }

            outputQuantity = addStoredQtyToCurrentQty(outputQuantity);

            Pulses = Integer.parseInt(outputQuantity);
            fillqty = Double.parseDouble(outputQuantity);
            fillqty = fillqty / numPulseRatio;//convert to gallons
            fillqty = AppConstants.roundNumber(fillqty, 2);
            DecimalFormat precision = new DecimalFormat("0.00");
            Constants.FS_1Gallons = (precision.format(fillqty));
            Constants.FS_1Pulse = outputQuantity;

            if (cd.isConnectingToInternet() || BTConstants.SwitchedBTToUDP1) {
                UpdatetransactionToSqlite(outputQuantity);
            } else {
                if (fillqty > 0) {
                    offlineController.updateOfflinePulsesQuantity(sqlite_id + "", outputQuantity, fillqty + "", OffLastTXNid);
                }
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Offline >> BTLink 1:" + LinkName + "; P:" + Integer.parseInt(outputQuantity) + "; Q:" + fillqty);
            }

            reachMaxLimit();

        } catch (Exception e) {
            e.printStackTrace();
            //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "BTLink 1: pulse count Exception>>" + e.getMessage());
        }
    }

    private String addStoredQtyToCurrentQty(String outputQuantity) {
        String newQty = outputQuantity;
        try {

            if (BTConstants.isRelayOnAfterReconnect1) {
                SharedPreferences sharedPrefLastQty = this.getSharedPreferences("LastQuantity_BT1", Context.MODE_PRIVATE);
                long storedPulsesCount = sharedPrefLastQty.getLong("Last_Quantity", 0);

                long quantity = Integer.parseInt(outputQuantity);

                long add_count = storedPulsesCount + quantity;

                outputQuantity = Long.toString(add_count);

                newQty = outputQuantity;

            }

        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: addStoredQtyToCurrentQty Exception:" + ex.getMessage());
        }
        return newQty;
    }

    public class BroadcastBlueLinkOneData extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                Bundle notificationData = intent.getExtras();
                String Action = notificationData.getString("Action");
                if (Action.equalsIgnoreCase("BlueLinkOne")) {
                    boolean ts = RelayStatus;
                    Request = notificationData.getString("Request");
                    Response = notificationData.getString("Response");

                    if (Request.equalsIgnoreCase(BTConstants.fdcheckcommand)) {
                        FDRequest = Request;
                        FDResponse = Response;
                    }
                    if (Request.contains(BTConstants.linkUpgrade_cmd) && upgradeResponse.isEmpty()) {
                        upgradeResponse = Response;
                    }
                    /*if (AppConstants.isRelayON_fs1 && Response.trim().isEmpty()) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 1: No Response from Broadcast.");
                    }*/

                    //Used only for debug
                    Log.i(TAG, "BTLink 1: Link Request>>" + Request);
                    Log.i(TAG, "BTLink 1: Link Response>>" + Response);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink 1: Response from Link >>" + Response);

                    //Set Relay status.
                    if (Response.contains("OFF")) {
                        RelayStatus = false;
                    } else if (Response.contains("ON")) {
                        //AppConstants.WriteinFile(TAG + " BTLink 1: onReceive Response:" + Response.trim() + "; ReadPulse: " + redpulseloop_on);
                        RelayStatus = true;
                        AppConstants.isRelayON_fs1 = true;
                        if (!redpulseloop_on) {
                            ReadPulse();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1:onReceive Exception:" + e.toString());
            }
        }
    }

    //Sqlite code
    private void InsertInitialTransactionToSqlite() {

        String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTOne.this).PersonEmail;
        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_BTOne.this) + ":" + userEmail + ":" + "TransactionComplete" + AppConstants.LANG_PARAM);

        HashMap<String, String> imap = new HashMap<>();
        imap.put("jsonData", "");
        imap.put("authString", authString);

        sqliteID = controller.insertTransactions(imap);
        CommonUtils.AddRemovecurrentTransactionList(true, TransactionId);//Add transaction Id to list

    }

    private void UpdatetransactionToSqlite(String outputQuantity) {

        ////////////////////////////////////-Update transaction ---
        TrazComp authEntityClass = new TrazComp();
        authEntityClass.TransactionId = TransactionId;
        authEntityClass.FuelQuantity = fillqty;
        authEntityClass.AppInfo = " Version:" + CommonUtils.getVersionCode(BackgroundService_BTOne.this) + " " + AppConstants.getDeviceName() + " Android " + Build.VERSION.RELEASE + " " + "--Main Transaction--";
        authEntityClass.TransactionFrom = "A";
        authEntityClass.Pulses = Integer.parseInt(outputQuantity);
        authEntityClass.IsFuelingStop = IsFuelingStop;
        authEntityClass.IsLastTransaction = IsLastTransaction;
        authEntityClass.OverrideQuantity = OverrideQuantity;
        authEntityClass.OverridePulse = OverridePulse;

        Gson gson = new Gson();
        String jsonData = gson.toJson(authEntityClass);

        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + " BTLink 1: ID:" + TransactionId + "; LINK:" + LinkName + "; Pulses:" + Integer.parseInt(outputQuantity) + "; Qty:" + fillqty);

        String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTOne.this).PersonEmail;
        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_BTOne.this) + ":" + userEmail + ":" + "TransactionComplete" + AppConstants.LANG_PARAM);


        HashMap<String, String> imap = new HashMap<>();
        imap.put("jsonData", jsonData);
        imap.put("authString", authString);
        imap.put("sqliteId", sqliteID + "");

        if (fillqty > 0) {

            //in progress (transaction recently started, no new information): Transaction ongoing = 8  --non zero qty
            CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "8", BackgroundService_BTOne.this);
            int rowseffected = controller.updateTransactions(imap);
            System.out.println("rowseffected-" + rowseffected);
            if (rowseffected == 0) {
                controller.insertTransactions(imap);
            }
        }
    }

    private void SaveLastBTTransactionInLocalDB(String txnId, String counts) {

        try {
            double lastCnt = Double.parseDouble(counts);
            double Lastqty = lastCnt / numPulseRatio; //convert to gallons
            Lastqty = AppConstants.roundNumber(Lastqty, 2);

            ////////////////////////////////////-Update transaction ---
            TrazComp authEntityClass = new TrazComp();
            authEntityClass.TransactionId = txnId;
            authEntityClass.FuelQuantity = Lastqty;
            authEntityClass.AppInfo = " Version:" + CommonUtils.getVersionCode(BackgroundService_BTOne.this) + " " + AppConstants.getDeviceName() + " Android " + android.os.Build.VERSION.RELEASE + " " + "--Last Transaction--";
            authEntityClass.TransactionFrom = "A";
            authEntityClass.Pulses = Integer.parseInt(counts);
            authEntityClass.IsFuelingStop = IsFuelingStop;
            authEntityClass.IsLastTransaction = "1";

            Gson gson = new Gson();
            String jsonData = gson.toJson(authEntityClass);

            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: <Last Transaction saved in local DB. LastTXNid:" + txnId + "; LINK:" + LinkName + "; Pulses:" + Integer.parseInt(counts) + "; Qty:" + Lastqty + ">");

            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTOne.this).PersonEmail;
            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_BTOne.this) + ":" + userEmail + ":" + "TransactionComplete" + AppConstants.LANG_PARAM);

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

            if (isInsert && Lastqty > 0) {
                controller.insertTransactions(imap);
            }

        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: SaveLastBTTransactionToServer Exception: " + e.getMessage());
        }
    }

    private void TransactionCompleteFunction() {

        if (cd.isConnectingToInternet()) {
            //BTLink Rename functionality
            if (BTConstants.BT1NeedRename && !BTConstants.BT1REPLACEBLE_WIFI_NAME.isEmpty()) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        renameOnCommand();
                    }
                }, 1000);
            } else {
                PostTransactionBackgroundTasks();
            }
        } else {
            PostTransactionBackgroundTasks();
        }
    }

    private void PostTransactionBackgroundTasks() {
        try {
            if (cd.isConnectingToInternet()) {

                // Save upgrade details to cloud
                SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_FS_UPGRADE, Context.MODE_PRIVATE);
                String hoseid = sharedPref.getString("hoseid_bt1", "");
                String fsversion = sharedPref.getString("fsversion_bt1", "");

                UpgradeVersionEntity objEntityClass = new UpgradeVersionEntity();
                objEntityClass.IMEIUDID = AppConstants.getIMEI(BackgroundService_BTOne.this);
                objEntityClass.Email = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTOne.this).PersonEmail;
                objEntityClass.HoseId = hoseid;
                objEntityClass.Version = fsversion;

                if (hoseid != null && !hoseid.trim().isEmpty()) {
                    new UpgradeCurrentVersionWithUpgradableVersion(objEntityClass).execute();

                    // Update upgrade details into serverSSIDList
                    if (AppConstants.IsSingleLink) {
                        HashMap<String, String> selSSid = WelcomeActivity.serverSSIDList.get(0);
                        selSSid.put("IsUpgrade", "N");
                        selSSid.put("FirmwareVersion", fsversion);
                        WelcomeActivity.serverSSIDList.set(0, selSSid);
                    }
                    //=============================================================
                }
                //=============================================================

                boolean BSRunning = CommonUtils.checkServiceRunning(BackgroundService_BTOne.this, AppConstants.PACKAGE_BACKGROUND_SERVICE);
                if (!BSRunning) {
                    startService(new Intent(this, BackgroundService.class));
                }
            }

            // Offline transaction data sync
            if (OfflineConstants.isOfflineAccess(BackgroundService_BTOne.this))
                SyncOfflineData();

        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: BackgroundTasksPostTransaction Exception: " + e.getMessage());
        }
    }

    private void reachMaxLimit() {

        //if quantity reach max limit
        if (minFuelLimit > 0 && fillqty >= minFuelLimit) {
            Log.i(TAG, "BTLink 1: Auto Stop Hit>> You reached MAX fuel limit.");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: Auto Stop Hit>> You reached MAX fuel limit.");
            relayOffCommand(); //RelayOff
            TransactionCompleteFunction();
            CloseTransaction(true);
        }

    }

    private void pumpTimingsOnOffFunction() {

        try {
            int pumpOnpoint = Integer.parseInt(PumpOnTime);

            if (Pulses <= 0) {//PumpOn Time logic
                stopCount++;
                if (stopCount >= pumpOnpoint) {

                    //Timed out (Start was pressed, and pump on timer hit): Pump Time On limit reached* = 4
                    CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "4", BackgroundService_BTOne.this);
                    Log.i(TAG, " BTLink 1: PumpOnTime Hit>>" + stopCount);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink 1: PumpOnTime Hit.");
                    relayOffCommand(); //RelayOff
                    TransactionCompleteFunction();
                    CloseTransaction(true);
                }
            } else {//PumpOff Time logic

                if (!Pulses.equals(pre_pulse)) {
                    stopCount = 0;
                    pre_pulse = Pulses;
                } else {
                    stopCount++;
                }

                if (stopCount >= stopAutoFuelSeconds) {
                    Log.i(TAG, " BTLink 1: PumpOffTime Hit>>" + stopCount);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink 1: PumpOffTime Hit.");
                    relayOffCommand(); //RelayOff
                    TransactionCompleteFunction();
                    CloseTransaction(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void CheckResponse(String checkPulses) {

        try {
            try {
                if (RelayStatus) {
                    if (RespCount < 4) {
                        RespCount++;
                    } else {
                        RespCount = 0;
                    }

                    if (RespCount == 4) {
                        RespCount = 0;
                        //Execute fdcheck counter
                        Log.i(TAG, "BTLink 1: Execute FD Check..>>");

                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                            getMainExecutor().execute(new Runnable() {
                                @Override public void run() {
                                    fdCheckCommand();
                                }
                            });
                        } else{
                            fdCheckCommand();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!Response.contains(checkPulses)) {
                stopCount++;
                /*if (!Response.contains("ON") && !Response.contains("OFF")) {
                    Log.i(TAG, " BTLink 1: No response from link>>" + stopCount);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink 1: No response from link. Response >> " + Response.trim());
                }*/
                //int pumpOnpoint = Integer.parseInt(PumpOnTime);
                if (stopCount >= stopAutoFuelSeconds) {
                    if (Pulses <= 0) {
                        CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "4", BackgroundService_BTOne.this);
                    }
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink 1: Auto Stop Hit. Response >> " + Response.trim());
                    stopCount = 0;
                    relayOffCommand(); //RelayOff
                    TransactionCompleteFunction();
                    CloseTransaction(true);
                    this.stopSelf();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fdCheckCommand() {
        try {
            //Execute FD_check Command
            if (IsThisBTTrnx) {
                Request = "";
                Response = "";
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1: Sending FD_check command to Link: " + LinkName);
                BTSPPMain btspp = new BTSPPMain();
                btspp.send1(BTConstants.fdcheckcommand);
            }
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: FD_check command Exception:>>" + ex.getMessage());
        }
    }

    private void parseInfoCommandResponseForLast20txtn(String response) {

        try{

            ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();

            JSONObject jsonObject = new JSONObject(response);

            JSONArray jsonArray = jsonObject.getJSONArray("records");
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject j = jsonArray.getJSONObject(i);
                String txtn = j.getString("txtn");
                String date = j.getString("date");
                String vehicle = j.getString("vehicle");
                String pulse = j.getString("pulse");
                String dflag = j.getString("dflag");

                try {
                    if (!date.contains("-") && date.length() == 12) { // change date format from "yyMMddHHmmss" to "yyyy-MM-dd HH:mm:ss"
                        date = BTConstants.parseDateForOldVersion(date);
                    }
                } catch (Exception e) {
                    Log.i(TAG, " BTLink 1: Exception while parsing date format.>> " + e.getMessage());
                }

                HashMap<String, String> Hmap = new HashMap<>();
                Hmap.put("TransactionID", txtn);//TransactionID
                Hmap.put("Pulses", pulse);//Pulses
                Hmap.put("FuelQuantity", ReturnQty(pulse));//FuelQuantity
                Hmap.put("TransactionDateTime", date); //TransactionDateTime
                Hmap.put("VehicleId", vehicle); //VehicleId
                Hmap.put("dflag", dflag);

                ReturnQty(pulse);

                arrayList.add(Hmap);
            }

            Gson gs = new Gson();
            EntityCmd20Txn ety = new EntityCmd20Txn();
            ety.cmtxtnid_20_record = arrayList;

            String json20txn = gs.toJson(ety);
            /*if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: parseInfoCommandResponseForLast20txtn json20txn>>" + json20txn);*/
            //Log.i(TAG, "BTLink 1: parseInfoCommandResponseForLast20txtn json20txn>>" + json20txn);

            SharedPreferences sharedPref = BackgroundService_BTOne.this.getSharedPreferences("storeCmtxtnid_20_record", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("LINK1", json20txn);
            editor.apply();

            JSONObject versionJsonArray = jsonObject.getJSONObject("version");
            String version = versionJsonArray.getString("version");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: LINK Version >> " + version);
            storeUpgradeFSVersion(BackgroundService_BTOne.this, AppConstants.UP_HoseId_fs1, version);

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: Exception in parseInfoCommandResponseForLast20txtn. response>> " + response + "; Exception>>" + e.getMessage());
        }
    }

    public class EntityCmd20Txn {
        ArrayList cmtxtnid_20_record;
        String jsonfromLink;
    }

    private String ReturnQty(String outputQuantity){

        String return_qty = "";
        try {

            double fillqty = 0;
            Integer Pulses = Integer.parseInt(outputQuantity);
            if (Pulses > 0) {
                fillqty = Double.parseDouble(outputQuantity);
                fillqty = fillqty / numPulseRatio;//convert to gallons

                fillqty = AppConstants.roundNumber(fillqty, 2);

                DecimalFormat precision = new DecimalFormat("0.00");
                return_qty = (precision.format(fillqty));
            }else{
                return_qty = "0";
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return return_qty;
    }

    private String removeLastChar(String s) {

        if (s.isEmpty())
            return "";

        return s.substring(0, s.length() - 1);
    }

    public void parseInfoCommandResponseForLast10txtn(String response) {
        try {
            String version = "";

            if (response.contains("BTMAC")) {
                String[] split_res = response.split("\n");

                if (split_res.length > 10) {
                    for (int i = 0; i < split_res.length; i++) {
                        String res = split_res[i];

                        if (i == 1 && res.contains("-")) { // Only get first transaction
                            try {
                                String[] split = res.split("-");

                                if (split.length == 2) {
                                    String txn_id = split[0].trim();
                                    String pulse = split[1];

                                    pulse = removeLastChar(pulse.trim());

                                    if (!txn_id.isEmpty() && !txn_id.equalsIgnoreCase("0")) {
                                        SaveLastBTTransactionInLocalDB(txn_id, pulse);
                                    }
                                }
                            } catch (Exception e) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BTLink 1: Last10 txtn parsing exception:>>" + e.getMessage());
                            }
                        } else {

                            if (res.contains("version:")) {
                                version = res.substring(res.indexOf(":") + 1).trim();
                            }
                            if (!version.isEmpty()) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BTLink 1: LINK Version >> " + version);
                                storeUpgradeFSVersion(BackgroundService_BTOne.this, AppConstants.UP_HoseId_fs1, version);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: Exception in parseInfoCommandResponseForLast10txtn. response>> " + response + "; Exception>>" + e.getMessage());
        }
    }

    public void offlineLogicBT1() {

        try {

            TransactionId = "0";
            PhoneNumber = "0";
            FuelTypeId = "0";
            ServerDate = "0";

            //set transactionID
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    OffLastTXNid = "99999999";
                }
            }, 1500);

            EntityOffTranz tzc = offlineController.getTransactionDetailsBySqliteId(sqlite_id);

            VehicleId = tzc.VehicleId;
            PersonId = tzc.PersonId;
            String siteid = tzc.SiteId;

            HashMap<String, String> linkmap = offlineController.getLinksDetailsBySiteId(siteid);
            PumpOnTime = linkmap.get("PumpOnTime");
            IntervalToStopFuel = linkmap.get("PumpOffTime");
            PulseRatio = linkmap.get("Pulserratio");

            EnablePrinter = offlineController.getOfflineHubDetails(BackgroundService_BTOne.this).EnablePrinter;

            minFuelLimit = OfflineConstants.getFuelLimit(BackgroundService_BTOne.this);
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: <Fuel Limit: " + minFuelLimit + ">");
            numPulseRatio = Double.parseDouble(PulseRatio);

            stopAutoFuelSeconds = Long.parseLong(IntervalToStopFuel);

            Calendar calendar = Calendar.getInstance();
            TransactionDateWithFormat = BTConstants.dateFormatForOldVersion.format(calendar.getTime());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void SyncOfflineData() {

        if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE") && Constants.FS_5STATUS.equalsIgnoreCase("FREE") && Constants.FS_6STATUS.equalsIgnoreCase("FREE")) {

            if (cd.isConnecting()) {

                try {
                    //sync offline transactions
                    String off_json = offlineController.getAllOfflineTransactionJSON(BackgroundService_BTOne.this);
                    JSONObject jsonObj = new JSONObject(off_json);
                    String offTransactionArray = jsonObj.getString("TransactionsModelsObj");
                    JSONArray jArray = new JSONArray(offTransactionArray);

                    if (jArray.length() > 0) {
                        startService(new Intent(BackgroundService_BTOne.this, OffTranzSyncService.class));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void BTLinkUpgradeCheck() {
        try {
            boolean isUpgrade = false;

            if (BTConstants.CurrentTransactionIsBT) {
                if (BTConstants.CurrentSelectedLinkBT == 1) {
                    if (AppConstants.UP_Upgrade_fs1) {
                        isUpgrade = true;
                    }
                }
            }

            if (isUpgrade) {

                String LocalPath = getApplicationContext().getExternalFilesDir(AppConstants.FOLDER_BIN) + "/" + AppConstants.UP_Upgrade_File_name;
                File file = new File(LocalPath);
                if (file.exists()) { // && AppConstants.UP_Upgrade_File_name.startsWith("BT_")
                    // Sending info command to check link version
                    BTConstants.UpgradeStatusBT1 = "Started";
                    infoCommandBeforeUpgrade();
                    //upgradeCommand();

                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink 1: BTLinkUpgradeCommand - File (" + AppConstants.UP_Upgrade_File_name + ") Not found.");
                    proceedToInfoCommand(false);
                }
            } else {
                proceedToInfoCommand(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: BTLinkUpgradeCommand Exception:>>" + e.getMessage());
            proceedToInfoCommand(false);
        }
    }

    private void infoCommandBeforeUpgrade() {
        try {
            //Execute info command before upgrade to get link version
            Request = "";
            Response = "";
            BTConstants.isNewVersionLinkOne = false;

            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: Sending Info command (before upgrade) to Link: " + LinkName);
            BTSPPMain btspp = new BTSPPMain();
            btspp.send1(BTConstants.info_cmd);

            //Thread.sleep(1000);
            new CountDownTimer(5000, 1000) {

                public void onTick(long millisUntilFinished) {
                    long attempt = (5 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (Request.equalsIgnoreCase(BTConstants.info_cmd) && !Response.equalsIgnoreCase("")) {
                            //Info command (before upgrade) success.
                            if (Response.contains("records") && Response.contains("mac_address")) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BTLink 1: Checking Info command response (before upgrade). Response: true");
                                BTConstants.isNewVersionLinkOne = true;
                                getVersionBeforeUpgrade(Response.trim(), true);
                                Response = "";
                            } else {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BTLink 1: Checking Info command response (before upgrade). Response:>>" + Response.trim());
                                BTConstants.isNewVersionLinkOne = false;
                                getVersionBeforeUpgrade(Response.trim(), false);
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    upgradeCommand();
                                }
                            }, 1000);
                            cancel();
                        } else {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink 1: Checking Info command response (before upgrade). Response: false");
                        }
                    }
                }

                public void onFinish() {

                    if (Request.equalsIgnoreCase(BTConstants.info_cmd) && !Response.equalsIgnoreCase("")) {
                        //Info command (before upgrade) success.
                        if (Response.contains("records") && Response.contains("mac_address")) {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink 1: Checking Info command response (before upgrade). Response: true");
                            BTConstants.isNewVersionLinkOne = true;
                            getVersionBeforeUpgrade(Response.trim(), true);
                            Response = "";
                        } else {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink 1: Checking Info command response (before upgrade). Response:>>" + Response.trim());
                            BTConstants.isNewVersionLinkOne = false;
                            getVersionBeforeUpgrade(Response.trim(), false);
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                upgradeCommand();
                            }
                        }, 1000);
                    } else {
                        //UpgradeTransaction Status info command fail.
                        CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "6", BackgroundService_BTOne.this);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 1: Checking Info command response (before upgrade). Response: false");
                        AppConstants.TxnFailedCount1++;
                        AppConstants.IsTransactionFailed1 = true;
                        BTConstants.UpgradeStatusBT1 = "Incomplete";
                        CloseTransaction(true);
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: infoCommandBeforeUpgrade Exception:>>" + e.getMessage());
        }
    }

    public void getVersionBeforeUpgrade(String response, boolean isNewLink) {
        try {
            if (isNewLink) {
                // New Link version
                JSONObject jsonObject = new JSONObject(response);

                JSONObject versionJsonArray = jsonObject.getJSONObject("version");
                String version = versionJsonArray.getString("version");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1: LINK Version (Before Upgrade) >> " + version);
            } else {
                // Old Link version
                String version = "";
                if (response.contains("BTMAC")) {
                    String[] split_res = response.split("\n");

                    if (split_res.length > 10) {
                        for (int i = 0; i < split_res.length; i++) {
                            String res = split_res[i];

                            if (res.contains("version:")) {
                                version = res.substring(res.indexOf(":") + 1).trim();
                            }
                            if (!version.isEmpty()) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BTLink 1: LINK Version (Before Upgrade) >> " + version);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: getVersionBeforeUpgrade Exception:>>" + e.getMessage());
        }
    }

    private void upgradeCommand() {
        try {
            //Execute upgrade Command
            Request = "";
            upgradeResponse = "";

            String LocalPath = getApplicationContext().getExternalFilesDir(AppConstants.FOLDER_BIN) + "/" + AppConstants.UP_Upgrade_File_name;
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: BTLinkUpgradeFunctionality file name: " + AppConstants.UP_Upgrade_File_name);

            File file = new File(LocalPath);
            long file_size = file.length();

            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: Sending upgrade command to Link: " + LinkName);
            BTSPPMain btspp = new BTSPPMain();
            btspp.send1(BTConstants.linkUpgrade_cmd + file_size);

            new CountDownTimer(10000, 2000) {

                public void onTick(long millisUntilFinished) {
                    if (Request.contains(BTConstants.linkUpgrade_cmd) && !upgradeResponse.isEmpty()) {
                        //upgrade command success.
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 1: Checking upgrade command response. Response:>>" + upgradeResponse.trim());
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                BTConstants.UpgradeStatusBT1 = "Started";
                                BTConstants.isUpgradeInProgress_BT1 = true;
                                new UpgradeFileUploadFunctionality().execute();
                            }
                        }, 1000);
                        cancel();
                    }
                }

                public void onFinish() {

                    if ((Request.contains(BTConstants.linkUpgrade_cmd) && !upgradeResponse.isEmpty()) || (BTConstants.CurrentCommand_LinkOne.contains(BTConstants.linkUpgrade_cmd) && !BTConstants.isNewVersionLinkOne)) {
                        //upgrade command success.
                        if (BTConstants.isNewVersionLinkOne) {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink 1: Checking upgrade command response. Response:>>" + upgradeResponse.trim());
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                BTConstants.UpgradeStatusBT1 = "Started";
                                BTConstants.isUpgradeInProgress_BT1 = true;
                                new UpgradeFileUploadFunctionality().execute();
                            }
                        }, 1000);
                    } else {
                        // Terminating transaction as per Bolong's comment in #2120 => DO NOT send any command after sending upgrade command.
                        //UpgradeTransaction Status upgrade command fail.
                        CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "6", BackgroundService_BTOne.this);
                        Log.i(TAG, "BTLink 1: Failed to get upgrade command Response:>>" + upgradeResponse);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 1: Checking upgrade command response. Response: false");
                        AppConstants.TxnFailedCount1++;
                        AppConstants.IsTransactionFailed1 = true;
                        BTConstants.UpgradeStatusBT1 = "Incomplete";
                        CloseTransaction(true);
                    }
                }
            }.start();

        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: upgradeCommand Exception:>>" + e.getMessage());
        }
    }

    public class UpgradeFileUploadFunctionality extends AsyncTask<String, String, String> {

        //ProgressDialog pd;
        int counter = 0;

        @Override
        protected void onPreExecute() {
            /*pd = new ProgressDialog(DisplayMeterActivity.this);
            pd.setMessage("Software update in progress.\nPlease wait several seconds....");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();*/
        }

        @Override
        protected String doInBackground(String... f_url) {

            try {
                String LocalPath = getApplicationContext().getExternalFilesDir(AppConstants.FOLDER_BIN) + "/" + AppConstants.UP_Upgrade_File_name;

                File file = new File(LocalPath);

                long file_size = file.length();
                long tempFileSize = file_size;

                BTSPPMain btspp = new BTSPPMain();

                InputStream inputStream = new FileInputStream(file);

                int BUFFER_SIZE = 256; //490; //8192;
                byte[] bufferBytes = new byte[BUFFER_SIZE];

                if (inputStream != null) {
                    long bytesWritten = 0;
                    int amountOfBytesRead;
                    //BufferedInputStream bufferedReader = new BufferedInputStream(inputStream);
                    //while ((amountOfBytesRead = bufferedReader.read(bufferBytes, 0, bufferBytes.length)) != -1) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink 1: Upload (" + AppConstants.UP_Upgrade_File_name + ") started...");
                    while ((amountOfBytesRead = inputStream.read(bufferBytes)) != -1) {

                        bytesWritten += amountOfBytesRead;
                        String progressValue = (int) (100 * ((double) bytesWritten) / ((double) file_size)) + " %";
                        //AppConstants.WriteinFile(TAG + " ~~~~~~~~ Progress : " + progressValue);
                        BTConstants.upgradeProgress = progressValue;

                        if (BTConstants.BTStatusStrOne.equalsIgnoreCase("Connected")) {
                            btspp.sendBytes1(bufferBytes);

                            tempFileSize = tempFileSize - BUFFER_SIZE;
                            if (tempFileSize < BUFFER_SIZE){
                                int i = (int) (long) tempFileSize;
                                if (i > 0) {
                                    //i = i + BUFFER_SIZE;
                                    bufferBytes = new byte[i];
                                }
                            }

                            Thread.sleep(10);
                        } else {
                            BTConstants.IsFileUploadCompleted = false;
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink 1: After upgrade command (Link is not connected): Progress: " + progressValue);
                            BTConstants.UpgradeStatusBT1 = "Incomplete";
                            break;
                        }
                    }
                    inputStream.close();
                    if (BTConstants.UpgradeStatusBT1.isEmpty() || BTConstants.UpgradeStatusBT1.equalsIgnoreCase("Started")) {
                        BTConstants.UpgradeStatusBT1 = "Completed";
                    }
                }

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1: UpgradeFileUploadFunctionality InBackground Exception: " + e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            //pd.dismiss();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: LINK Status: " + BTConstants.BTStatusStrOne);
            BTConstants.upgradeProgress = "0 %";
            if (BTConstants.UpgradeStatusBT1.equalsIgnoreCase("Completed")) {
                BTConstants.IsFileUploadCompleted = true;
                storeUpgradeFSVersion(BackgroundService_BTOne.this, AppConstants.UP_HoseId_fs1, AppConstants.UP_FirmwareVersion);

                Handler handler = new Handler();
                int delay = 10000;

                handler.postDelayed(new Runnable() {
                    public void run() {
                        if (BTConstants.BTStatusStrOne.equalsIgnoreCase("Connected")) {
                            counter = 0;
                            handler.removeCallbacksAndMessages(null);
                            proceedToInfoCommand(true);
                        } else {
                            counter++;
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink 1: Waiting to reconnect... (Attempt: " + counter + ")");
                            if (counter < 3) {
                                handler.postDelayed(this, delay);
                            } else {
                                CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "6", BackgroundService_BTOne.this);
                                Log.i(TAG, "BTLink 1: Failed to connect to the link.");
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BTLink 1: Failed to connect to the link. (Status: " + BTConstants.BTStatusStrOne + ")");
                                IsThisBTTrnx = false;
                                AppConstants.TxnFailedCount1++;
                                AppConstants.IsTransactionFailed1 = true;
                                CloseTransaction(true);
                            }
                        }
                    }
                }, delay);

            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        proceedToInfoCommand(true);
                    }
                }, 3000);
            }
        }
    }

    public void storeUpgradeFSVersion(Context context, String hoseid, String fsversion) {

        SharedPreferences sharedPref = context.getSharedPreferences(Constants.PREF_FS_UPGRADE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("hoseid_bt1", hoseid);
        editor.putString("fsversion_bt1", fsversion);
        /*if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + " Upgrade details saved locally. (Version => " + fsversion + ")");*/
        editor.commit();
    }

    public class UpgradeCurrentVersionWithUpgradableVersion extends AsyncTask<Void, Void, String> {
        UpgradeVersionEntity objUpgrade;
        public String response = null;

        public UpgradeCurrentVersionWithUpgradableVersion(UpgradeVersionEntity objUpgrade) {
            this.objUpgrade = objUpgrade;
        }

        @Override
        protected String doInBackground(Void... voids) {

            try {
                ServerHandler serverHandler = new ServerHandler();

                Gson gson = new Gson();
                String jsonData = gson.toJson(objUpgrade);
                //AppConstants.WriteinFile(TAG + " BTLink 1: UpgradeCurrentVersionWithUpgradableVersion (" + jsonData + ")");

                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(objUpgrade.IMEIUDID + ":" + objUpgrade.Email + ":" + "UpgradeCurrentVersionWithUgradableVersion" + AppConstants.LANG_PARAM);
                response = serverHandler.PostTextData(BackgroundService_BTOne.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1: UpgradeCurrentVersionWithUpgradableVersion Exception: " + ex.getMessage());
            }
            return response;
        }

        @Override
        protected void onPostExecute(String aVoid) {
            try {
                JSONObject jsonObject = new JSONObject(aVoid);
                String ResponceMessage = jsonObject.getString("ResponceMessage");
                String ResponceText = jsonObject.getString("ResponceText");

                if (ResponceMessage.equalsIgnoreCase("success")) {
                    AppConstants.clearSharedPrefByName(BackgroundService_BTOne.this, Constants.PREF_FS_UPGRADE);
                }
            } catch (Exception e) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 1: UpgradeCurrentVersionWithUpgradableVersion onPostExecute Exception: " + e.getMessage());
            }
        }
    }

    private void UpdateSwitchTimeBounceForLink() {
        try {
            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTOne.this).PersonEmail;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_BTOne.this) + ":" + userEmail + ":" + "UpdateSwitchTimeBounceForLink" + AppConstants.LANG_PARAM);

            SwitchTimeBounce switchTimeBounce = new SwitchTimeBounce();
            switchTimeBounce.SiteId = BTConstants.BT1SITE_ID;
            switchTimeBounce.IsResetSwitchTimeBounce = "0";

            Gson gson = new Gson();
            String jsonData = gson.toJson(switchTimeBounce);

            storeSwitchTimeBounceFlag(BackgroundService_BTOne.this, jsonData, authString);

        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: UpdateSwitchTimeBounceForLink Exception: " + ex.getMessage());
        }
    }

    public void storeSwitchTimeBounceFlag(Context context, String jsonData, String authString) {
        try {
            SharedPreferences pref;
            SharedPreferences.Editor editor;

            pref = context.getSharedPreferences("storeSwitchTimeBounceFlag1", 0);
            editor = pref.edit();

            // Storing
            editor.putString("jsonData", jsonData);
            editor.putString("authString", authString);

            // commit changes
            editor.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
