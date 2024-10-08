package com.TrakEngineering.FluidSecureHub.BTSPP;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.AppConstants;
import com.TrakEngineering.FluidSecureHub.BackgroundService;
import com.TrakEngineering.FluidSecureHub.CommonUtils;
import com.TrakEngineering.FluidSecureHub.ConnectionDetector;
import com.TrakEngineering.FluidSecureHub.Constants;
import com.TrakEngineering.FluidSecureHub.DBController;
import com.TrakEngineering.FluidSecureHub.WelcomeActivity;
import com.TrakEngineering.FluidSecureHub.entity.ManualOverrideStatus;
import com.TrakEngineering.FluidSecureHub.entity.RenameHose;
import com.TrakEngineering.FluidSecureHub.entity.SwitchTimeBounce;
import com.TrakEngineering.FluidSecureHub.entity.TrazComp;
import com.TrakEngineering.FluidSecureHub.entity.UpdatePulserTypeOfLINK_entity;
import com.TrakEngineering.FluidSecureHub.entity.UpgradeVersionEntity;
import com.TrakEngineering.FluidSecureHub.offline.EntityOffTranz;
import com.TrakEngineering.FluidSecureHub.offline.OffDBController;
import com.TrakEngineering.FluidSecureHub.offline.OffTranzSyncService;
import com.TrakEngineering.FluidSecureHub.offline.OfflineConstants;
import com.TrakEngineering.FluidSecureHub.server.ServerHandler;
import com.google.gson.Gson;

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

public class BackgroundService_BTThree extends Service {

    private static final String TAG = AppConstants.LOG_TXTN_BT + "-"; // + BackgroundService_BTThree.class.getSimpleName();
    public long sqlite_id = 0;
    String TransactionId, VehicleId, PhoneNumber, PersonId, PulseRatio, MinLimit, FuelTypeId, ServerDate, IntervalToStopFuel, IsTLDCall, EnablePrinter, PumpOnTime, LimitReachedMessage, VehicleNumber, TransactionDateWithFormat;
    public BroadcastBlueLinkThreeData broadcastBlueLinkThreeData = null;
    String Request = "", Response = "";
    String FDRequest = "", FDResponse = "";
    String upgradeResponse = "";
    int PreviousRes = 0;
    boolean redpulseloop_on, RelayStatus;
    int pulseCount = 0;
    int stopCount = 0;
    int RespCount = 0; //, LinkResponseCount = 0;
    long stopAutoFuelSeconds = 0;
    Integer Pulses = 0;
    Integer pre_pulse = 0;
    double fillqty = 0, numPulseRatio = 0, minFuelLimit = 0;
    long sqliteID = 0;
    String CurrentLinkMac = "", LinkCommunicationType = "", SERVER_IP = "", LinkName = "", printReceipt = "", IsFuelingStop = "0", IsLastTransaction = "0", OverrideQuantity = "0", OverridePulse = "0";
    Timer timerBt3;
    List<Timer> TimerList_ReadpulseBT3 = new ArrayList<Timer>();
    DBController controller = new DBController(BackgroundService_BTThree.this);
    Boolean IsThisBTTrnx;
    boolean isBroadcastReceiverRegistered = false;
    String OffLastTXNid = "0";
    ConnectionDetector cd = new ConnectionDetector(BackgroundService_BTThree.this);
    OffDBController offlineController = new OffDBController(BackgroundService_BTThree.this);
    //String ipForUDP = "192.168.4.1"; // Removed UDP code as per #2603
    public int infoCommandAttempt = 0;
    public boolean isConnected = false;
    public boolean isHotspotDisabled = false;
    public boolean isOnlineTxn = true;
    public String versionNumberOfLinkThree = "";
    public String PulserTimingAdjust, IsResetSwitchTimeBounce, IsBypassPumpReset, GetPulserTypeFromLINK;
    public boolean IsAnyPostTxnCommandExecuted = false;
    public boolean isTxnLimitReached = false;
    public String MOStatusCheckFlag, IsCheckMOStatus, IsResetMOCheckFlag;
    public boolean isManualOverrideDetected = false;
    //public int relayOffAttemptCount = 0;

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
                StopTransaction(false, true); // extras == null
            } else {
                sqlite_id = (long) extras.get("sqlite_id");
                SERVER_IP = String.valueOf(extras.get("SERVER_IP"));
                Request = "";
                Request = "";
                stopCount = 0;
                Log.i(TAG, "-Started-");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink_3: -Started-");

                Constants.FS_3STATUS = "BUSY";

                SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
                TransactionId = sharedPref.getString("TransactionId_FS3", "");
                VehicleId = sharedPref.getString("VehicleId_FS3", "");
                VehicleNumber = sharedPref.getString("VehicleNumber_FS3", "");
                PhoneNumber = sharedPref.getString("PhoneNumber_FS3", "");
                PersonId = sharedPref.getString("PersonId_FS3", "");
                PulseRatio = sharedPref.getString("PulseRatio_FS3", "1");
                MinLimit = sharedPref.getString("MinLimit_FS3", "0");
                FuelTypeId = sharedPref.getString("FuelTypeId_FS3", "");
                ServerDate = sharedPref.getString("ServerDate_FS3", "");
                TransactionDateWithFormat = sharedPref.getString("TransactionDateWithFormat_FS3", "");
                IntervalToStopFuel = sharedPref.getString("IntervalToStopFuel_FS3", "0");
                IsTLDCall = sharedPref.getString("IsTLDCall_FS3", "False");
                EnablePrinter = sharedPref.getString("EnablePrinter_FS3", "False");
                PumpOnTime = sharedPref.getString("PumpOnTime_FS3", "0");
                LimitReachedMessage = sharedPref.getString("LimitReachedMessage_FS3", "");

                numPulseRatio = Double.parseDouble(PulseRatio);
                minFuelLimit = Double.parseDouble(MinLimit);
                stopAutoFuelSeconds = Long.parseLong(IntervalToStopFuel);

                SharedPreferences calibrationPref = this.getSharedPreferences(Constants.PREF_CalibrationDetails, Context.MODE_PRIVATE);
                PulserTimingAdjust = calibrationPref.getString("PulserTimingAdjust_FS3", "");
                IsResetSwitchTimeBounce = calibrationPref.getString("IsResetSwitchTimeBounce_FS3", "0");
                GetPulserTypeFromLINK = calibrationPref.getString("GetPulserTypeFromLINK_FS3", "False");

                SharedPreferences moStatusPref = this.getSharedPreferences(Constants.PREF_MOStatusDetails, Context.MODE_PRIVATE);
                MOStatusCheckFlag = moStatusPref.getString("MOStatusCheckFlag_FS3", "OFF");
                IsCheckMOStatus = moStatusPref.getString("IsCheckMOStatus_FS3", "False");
                IsResetMOCheckFlag = moStatusPref.getString("IsResetMOCheckFlag_FS3", "False");

                if (VehicleNumber.length() > 20) {
                    VehicleNumber = VehicleNumber.substring(VehicleNumber.length() - 20);
                }

                if (WelcomeActivity.serverSSIDList != null && WelcomeActivity.serverSSIDList.size() > 0) {
                    LinkCommunicationType = WelcomeActivity.serverSSIDList.get(WelcomeActivity.SelectedItemPos).get("LinkCommunicationType");
                    //CurrentLinkMac = WelcomeActivity.serverSSIDList.get(WelcomeActivity.SelectedItemPos).get("MacAddress");
                }

                // Offline functionality
                if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {
                    isOnlineTxn = true;
                } else {
                    isOnlineTxn = false;
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink_3:-Offline mode--");
                    offlineLogicBT3();
                }

                //Register Broadcast receiver
                broadcastBlueLinkThreeData = new BroadcastBlueLinkThreeData();
                IntentFilter intentFilter = new IntentFilter("BroadcastBlueLinkThreeData");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink_3: <Registering Broadcast Receiver.>");
                registerReceiver(broadcastBlueLinkThreeData, intentFilter);
                isBroadcastReceiverRegistered = true;
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink_3: <Registered successfully. (" + broadcastBlueLinkThreeData + ")>");

                AppConstants.isRelayON_fs3 = false;
                LinkName = CommonUtils.getLinkName(2);
                if (LinkCommunicationType.equalsIgnoreCase("BT")) {
                    IsThisBTTrnx = true;

                    checkBTLinkStatus("info"); // Changed from "upgrade" to "info" as per #1657
                /*} else if (LinkCommunicationType.equalsIgnoreCase("UDP")) {
                    IsThisBTTrnx = false;
                    infoCommand();
                    //BeginProcessUsingUDP();*/
                } else {
                    //Something went Wrong in hose selection.
                    IsThisBTTrnx = false;
                    Log.i(TAG, " BTLink_3: Something went Wrong in hose selection.");
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink_3: Something went wrong in hose selection. (Link CommType: " + LinkCommunicationType + ")");
                    StopTransaction(false, true); // Link CommType unknown
                    this.stopSelf();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Service.START_NOT_STICKY;
    }

    /*private void UDPFunctionalityAfterBTFailure() {
        try {
            if (CommonUtils.CheckAllHTTPLinksAreFree()) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink_3: Link not connected. Switching to UDP connection...");

                // Disable Hotspot
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " <Turning OFF the Hotspot. (if enabled)>");
                WifiApManager wifiApManager = new WifiApManager(BackgroundService_BTThree.this);
                wifiApManager.setWifiApEnabled(null, false);
                isHotspotDisabled = true;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Enable Wi-Fi
                WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                wifiManagerMM.setWifiEnabled(true);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        IsThisBTTrnx = false;
                        BTConstants.SwitchedBTToUDP3 = true;
                        BeginProcessUsingUDP();
                    }
                }, 5000);
            } else {
                TerminateBTTransaction();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    /*private void BeginProcessUsingUDP() {
        try {
            Toast.makeText(BackgroundService_BTThree.this, getResources().getString(R.string.PleaseWaitForWifiConnect), Toast.LENGTH_SHORT).show();

            new CountDownTimer(12000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink_3: Connecting to WiFi...");
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    String ssid = "";
                    if (wifiManager.isWifiEnabled()) {
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        ssid = wifiInfo.getSSID();
                    }

                    ssid = ssid.replace("\"", "");

                    if (ssid.equalsIgnoreCase(LinkName)) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink_3: Connected to " + ssid + " via WiFi.");
                        proceedToInfoCommand();
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
                            AppConstants.WriteinFile(TAG + " BTLink_3: Connected to " + ssid + " via WiFi.");
                        proceedToInfoCommand();
                        //loading.cancel();
                        cancel();
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink_3: Unable to connect to " + LinkName + " via WiFi.");
                        TerminateBTTransaction();
                    }
                }
            }.start();
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: Exception in BeginProcessUsingUDP: " + e.getMessage());
            TerminateBTTransaction();
            e.printStackTrace();
        }
    }*/

    private void TerminateBTTransaction() {
        try {
            IsThisBTTrnx = false;
            CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "6", BackgroundService_BTThree.this);
            Log.i(TAG, " BTLink_3: Link not connected. Please try again!");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: Link not connected.");
            AppConstants.TxnFailedCount3++;
            AppConstants.IsTransactionFailed3 = true;
            StopTransaction(true, true); // TerminateBTTransaction
            this.stopSelf();
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: Exception in TerminateBTTransaction: " + e.getMessage());
        }
    }

    public void proceedToInfoCommand() {
        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    infoCommand();
                }
            }, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkBTLinkStatus(String nextAction) {
        try {
            new CountDownTimer(10000, 2000) {
                public void onTick(long millisUntilFinished) {
                    if (BTConstants.BTStatusStrThree.equalsIgnoreCase("Connected")) {
                        isConnected = true;
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink_3: Link is connected.");
                        if (nextAction.equalsIgnoreCase("info")) {
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    infoCommand();
                                }
                            }, 1000);
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
                            AppConstants.WriteinFile(TAG + " BTLink_3: Checking Connection Status...");
                    }
                }

                public void onFinish() {
                    if (BTConstants.BTStatusStrThree.equalsIgnoreCase("Connected")) {
                        isConnected = true;
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink_3: Link is connected.");
                        if (nextAction.equalsIgnoreCase("info")) {
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    infoCommand();
                                }
                            }, 1000);
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
                        if (nextAction.equalsIgnoreCase("info")) { // Terminate BT Transaction
                            TerminateBTTransaction(); //UDPFunctionalityAfterBTFailure();
                        } else if (nextAction.equalsIgnoreCase("relay")) { // Terminate BT Txn After Interruption
                            TerminateBTTxnAfterInterruption();
                        }
                    }
                }
            }.start();
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: checkBTLinkStatus Exception:>>" + e.getMessage());
            if (nextAction.equalsIgnoreCase("info")) { // Terminate BT Transaction
                TerminateBTTransaction();
            } else if (nextAction.equalsIgnoreCase("relay")) { // Terminate BT Txn After Interruption
                TerminateBTTxnAfterInterruption();
            }
        }
    }

    //region Info Command
    private void infoCommand() {
        try {
            BTConstants.isNewVersionLinkThree = false;
            AppConstants.TxnFailedCount3 = 0;
            AppConstants.isInfoCommandSuccess_fs3 = false;
            //Execute info command
            Request = "";
            Response = "";
            if (IsThisBTTrnx) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink_3: Sending Info command to Link: " + LinkName);
                BTSPPMain btspp = new BTSPPMain();
                btspp.send3(BTConstants.info_cmd);
            }
            /*else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink_3: Sending Info command (UDP) to Link: " + LinkName);
                new Thread(new ClientSendAndListenUDPThree(BTConstants.info_cmd, ipForUDP, this)).start();
            }*/
            //Thread.sleep(1000);
            new CountDownTimer(5000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long attempt = (5 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (Request.equalsIgnoreCase(BTConstants.info_cmd) && !Response.equalsIgnoreCase("")) {
                            //Info command success.
                            Log.i(TAG, "BTLink_3: InfoCommand Response success 1:>>" + Response);

                            if (!TransactionId.isEmpty()) {
                                if (Response.contains("mac_address")) {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " BTLink_3: Checking Info command response. Response: true");
                                    BTConstants.isNewVersionLinkThree = true;
                                    parseInfoCommandResponse(Response); // parse info command response
                                    Response = "";
                                } else {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " BTLink_3: Checking Info command response. Response:>>" + Response.trim());
                                    parseInfoCommandResponseForLast10txtn(Response.trim()); // parse last 10 Txtn
                                }
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        AppConstants.isInfoCommandSuccess_fs3 = true;
                                        if (IsThisBTTrnx && BTConstants.isNewVersionLinkThree && CommonUtils.checkBTVersionCompatibility(versionNumberOfLinkThree, BTConstants.supportedLinkVersionForLast1)) {
                                            last1Command();
                                        } else {
                                            transactionIdCommand(TransactionId);
                                        }
                                    }
                                }, 1000);
                            } else {
                                Log.i(TAG, "BTLink_3: TransactionId is empty.");
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BTLink_3: TransactionId is empty.");
                                StopTransaction(false, true); // TransactionId is empty in infoCommand
                            }
                            cancel();
                        } else {
                            Log.i(TAG, "BTLink_3: Waiting for infoCommand Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink_3: Checking Info command response. Response: false");
                        }
                    }
                }

                public void onFinish() {
                    if (Request.equalsIgnoreCase(BTConstants.info_cmd) && !Response.equalsIgnoreCase("")) {
                        //Info command success.
                        Log.i(TAG, "BTLink_3: InfoCommand Response success 2:>>" + Response);

                        if (!TransactionId.isEmpty()) {
                            if (Response.contains("mac_address")) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BTLink_3: Checking Info command response. Response: true");
                                BTConstants.isNewVersionLinkThree = true;
                                parseInfoCommandResponse(Response); // parse info command response
                                Response = "";
                            } else {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BTLink_3: Checking Info command response. Response:>>" + Response.trim());
                                parseInfoCommandResponseForLast10txtn(Response.trim()); // parse last 10 Txtn
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    AppConstants.isInfoCommandSuccess_fs3 = true;
                                    if (IsThisBTTrnx && BTConstants.isNewVersionLinkThree && CommonUtils.checkBTVersionCompatibility(versionNumberOfLinkThree, BTConstants.supportedLinkVersionForLast1)) {
                                        last1Command();
                                    } else {
                                        transactionIdCommand(TransactionId);
                                    }
                                }
                            }, 1000);
                        } else {
                            Log.i(TAG, "BTLink_3: TransactionId is empty.");
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink_3: TransactionId is empty.");
                            StopTransaction(false, true); // TransactionId is empty in infoCommand onFinish
                        }
                    } else {
                        if (infoCommandAttempt > 0) {
                            //UpgradeTransaction Status info command fail.
                            CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "6", BackgroundService_BTThree.this);
                            Log.i(TAG, "BTLink_3: Failed to get infoCommand Response:>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink_3: Checking Info command response. Response: false");
                            AppConstants.TxnFailedCount3++;
                            AppConstants.IsTransactionFailed3 = true;
                            StopTransaction(true, true); // Info command Response: false
                        } else {
                            infoCommandAttempt++;
                            if (BTConstants.BTStatusStrThree.equalsIgnoreCase("Connected")) {
                                infoCommand(); // Retried one more time after failed to receive response from info command
                            } else {
                                BTConstants.retryConnForInfoCommand3 = true;
                                WaitForReconnectToLink();
                            }
                        }
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: infoCommand Exception:>>" + e.getMessage());
            StopTransaction(true, true); // Info command Exception
        }
    }

    public void WaitForReconnectToLink() {
        try {
            new CountDownTimer(10000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long attempt = (10 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (BTConstants.BTStatusStrThree.equalsIgnoreCase("Connected")) {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink_3: Connected to Link: " + LinkName);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    infoCommand(); // Retried one more time after failed to receive response from info command
                                }
                            }, 500);
                            cancel();
                        } else {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink_3: Waiting for Reconnect to Link: " + LinkName);
                        }
                    }
                }

                public void onFinish() {
                    if (BTConstants.BTStatusStrThree.equalsIgnoreCase("Connected")) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink_3: Connected to Link: " + LinkName);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                infoCommand(); // Retried one more time after failed to receive response from info command
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
                AppConstants.WriteinFile(TAG + " BTLink_3: WaitForReconnectToLink Exception:>>" + e.getMessage());
            TerminateBTTransaction();
        }
    }
    //endregion

    //region Last1 Command
    private void last1Command() {
        try {
            //Execute last1 Command
            Request = "";
            Response = "";

            if (IsThisBTTrnx) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink_3: Sending last1 command to Link: " + LinkName);
                BTSPPMain btspp = new BTSPPMain();
                btspp.send3(BTConstants.last1_cmd);
            }

            new CountDownTimer(4000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long attempt = (4 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (Request.equalsIgnoreCase(BTConstants.last1_cmd) && Response.contains("records")) {
                            //last1 command success.
                            Log.i(TAG, "BTLink_3: last1 Command Response success 1:>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink_3: Checking last1 command response. Response:>>" + Response.trim());
                            parseLast1CommandResponse(Response);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    transactionIdCommand(TransactionId);
                                }
                            }, 1000);
                            cancel();
                        } else {
                            Log.i(TAG, "BTLink_3: Waiting for last1 Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink_3: Checking last1 command response. Response: false");
                        }
                    }
                }

                public void onFinish() {
                    if (Request.equalsIgnoreCase(BTConstants.last1_cmd) && Response.contains("records")) {
                        //last1 command success.
                        Log.i(TAG, "BTLink_3: last1 Command Response success 2:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink_3: Checking last1 command response. Response:>>" + Response.trim());
                        parseLast1CommandResponse(Response);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                transactionIdCommand(TransactionId);
                            }
                        }, 1000);
                    } else {
                        transactionIdCommand(TransactionId);
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: last1 Command Exception:>>" + e.getMessage());
            transactionIdCommand(TransactionId);
        }
    }
    //endregion

    //region TransactionId (TDV) Command
    private void transactionIdCommand(String transactionId) {
        try {
            //Execute transactionId Command
            Request = "";
            Response = "";

            String transaction_id_cmd = BTConstants.transaction_id_cmd; //LK_COMM=txtnid:

            if (BTConstants.isNewVersionLinkThree) {
                TransactionDateWithFormat = BTConstants.parseDateForNewVersion(TransactionDateWithFormat);
                transaction_id_cmd = transaction_id_cmd.replace("txtnid:", ""); // For New version LK_COMM=T:XXXXX;D:XXXXX;V:XXXXXXXX;
                transaction_id_cmd = transaction_id_cmd + "T:" + transactionId + ";D:" + TransactionDateWithFormat + ";V:" + VehicleNumber + ";";
            } else {
                transaction_id_cmd = transaction_id_cmd + transactionId;
            }

            if (IsThisBTTrnx) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink_3: Sending transactionId command to Link: " + LinkName);
                BTSPPMain btspp = new BTSPPMain();
                btspp.send3(transaction_id_cmd);
            }
            /*else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink_3: Sending transactionId command (UDP) to Link: " + LinkName);
                new Thread(new ClientSendAndListenUDPThree(transaction_id_cmd, ipForUDP, this)).start();
            }*/
            Thread.sleep(500);
            new CountDownTimer(4000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long attempt = (4 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (Request.contains(transactionId) && Response.contains(transactionId)) {
                            //transactionId command success.
                            Log.i(TAG, "BTLink_3: transactionId Command Response success 1:>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink_3: Checking transactionId command response. Response:>>" + Response.trim());
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    relayOnCommand(false); //RelayOn
                                }
                            }, 1000);
                            cancel();
                        } else {
                            Log.i(TAG, "BTLink_3: Waiting for transactionId Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink_3: Checking transactionId command response. Response: false");
                        }
                    }
                }

                public void onFinish() {
                    if (Request.contains(transactionId) && Response.contains(transactionId)) {
                        //transactionId command success.
                        Log.i(TAG, "BTLink_3: transactionId Command Response success 2:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink_3: Checking transactionId command response. Response:>>" + Response.trim());
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                relayOnCommand(false); //RelayOn
                            }
                        }, 1000);
                    } else {
                        //UpgradeTransaction Status Transactionid command fail.
                        CommonUtils.UpgradeTransactionStatusToSqlite(transactionId, "6", BackgroundService_BTThree.this);
                        Log.i(TAG, "BTLink_3: Failed to get transactionId Command Response:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink_3: Checking transactionId command response. Response: false");
                        StopTransaction(true, true); // transactionId command Response: false
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: transactionId Command Exception:>>" + e.getMessage());
            StopTransaction(true, true); // transactionId Command Exception
        }
    }
    //endregion

    //region Relay ON Command
    private void relayOnCommand(boolean isAfterReconnect) {
        try {
            if (isAfterReconnect) {
                BTConstants.isReconnectCalled3 = false;
            }
            //Execute relayOn Command
            Request = "";
            Response = "";

            if (IsThisBTTrnx) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink_3: Sending relayOn command to Link: " + LinkName);
                BTSPPMain btspp = new BTSPPMain();
                btspp.send3(BTConstants.relay_on_cmd);
            }
            /*else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink_3: Sending relayOn command (UDP) to Link: " + LinkName);
                new Thread(new ClientSendAndListenUDPThree(BTConstants.relay_on_cmd, ipForUDP, this)).start();
            }*/

            if (!isAfterReconnect) {
                InsertInitialTransactionToSqlite();//Insert empty transaction into sqlite
            }

            Thread.sleep(500);
            new CountDownTimer(4000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long attempt = (4 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (RelayStatus) {
                            BTConstants.isRelayOnAfterReconnect3 = isAfterReconnect;
                            //relayOn command success.
                            Log.i(TAG, "BTLink_3: relayOn Command Response success 1:>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink_3: Checking relayOn command response. Response: ON");
                            cancel();
                        } else {
                            Log.i(TAG, "BTLink_3: Waiting for relayOn Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink_3: Checking relayOn command response. Response: false");
                        }
                    }
                }

                public void onFinish() {
                    if (RelayStatus) {
                        BTConstants.isRelayOnAfterReconnect3 = isAfterReconnect;
                        //relayOn command success.
                        Log.i(TAG, "BTLink_3: relayOn Command Response success 2:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink_3: Checking relayOn command response. Response: ON");
                    } else {
                        //UpgradeTransaction Status RelayON command fail.
                        if (isAfterReconnect && (Pulses > 0 || fillqty > 0)) {
                            if (isOnlineTxn) {
                                CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "10", BackgroundService_BTThree.this);
                            } else {
                                offlineController.updateOfflineTransactionStatus(sqlite_id + "", "10");
                            }
                        } else {
                            CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "6", BackgroundService_BTThree.this);
                        }
                        Log.i(TAG, "BTLink_3: Failed to get relayOn Command Response:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink_3: Checking relayOn command response. Response: false");
                        relayOffCommand(); //RelayOff
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: relayOn Command Exception:>>" + e.getMessage());
            relayOffCommand(); //RelayOff
        }
    }
    //endregion

    //region Relay OFF Command
    private void relayOffCommand() {
        try {
            //Execute relayOff Command
            Request = "";
            Response = "";
            //relayOffAttemptCount++;
            if (IsThisBTTrnx) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink_3: Sending relayOff command to Link: " + LinkName);
                BTSPPMain btspp = new BTSPPMain();
                btspp.send3(BTConstants.relay_off_cmd);
            }
            /*else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink_3: Sending relayOff command (UDP) to Link: " + LinkName);
                new Thread(new ClientSendAndListenUDPThree(BTConstants.relay_off_cmd, ipForUDP, this)).start();
            }*/

            new CountDownTimer(4000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long attempt = (4 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (!RelayStatus) {
                            //relayOff command success.
                            Log.i(TAG, "BTLink_3: relayOff Command Response success 1:>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink_3: Checking relayOff command response. Response:>>" + Response.trim());
                            if (!AppConstants.isRelayON_fs3) {
                                TransactionCompleteFunction();
                            }
                            cancel();
                        } else {
                            Log.i(TAG, "BTLink_3: Waiting for relayOff Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink_3: Checking relayOff command response. Response: false");
                        }
                    }
                }

                public void onFinish() {
                    if (!RelayStatus) {
                        Log.i(TAG, "BTLink_3: relayOff Command Response success 2:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink_3: Checking relayOff command response. Response:>>" + Response.trim());
                    } else {
                        Log.i(TAG, "BTLink_3: Failed to get relayOff Command Response:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink_3: Checking relayOff command response. Response: false");
                        if (BTConstants.isRelayOnAfterReconnect3) {
                            if (Pulses > 0 || fillqty > 0) {
                                if (isOnlineTxn) {
                                    CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "10", BackgroundService_BTThree.this);
                                } else {
                                    offlineController.updateOfflineTransactionStatus(sqlite_id + "", "10");
                                }
                            }
                        }
                        StopTransaction(true, true);
                    }
                    if (!AppConstants.isRelayON_fs3) {
                        TransactionCompleteFunction();
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: relayOff Command Exception:>>" + e.getMessage());
            if (!AppConstants.isRelayON_fs3) {
                TransactionCompleteFunction();
            }
        }
    }
    //endregion

    private void TransactionCompleteFunction() {

        if (isOnlineTxn) {
            if (BTConstants.BT3REPLACEBLE_WIFI_NAME == null) {
                BTConstants.BT3REPLACEBLE_WIFI_NAME = "";
            }
            //BTLink Rename functionality
            if (BTConstants.BT3NeedRename && !BTConstants.BT3REPLACEBLE_WIFI_NAME.isEmpty()) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        renameCommand();
                    }
                }, 1000);
            } else {
                ProceedToPostTransactionCommands();
            }
        } else {
            ProceedToPostTransactionCommands();
        }
    }

    public void ProceedToPostTransactionCommands() {
        // Free the link and continue to post transaction commands
        StopTransaction(true, false); // Free the link
        if (CommonUtils.checkBTVersionCompatibility(versionNumberOfLinkThree, BTConstants.supportedLinkVersionForLast20)) { // Last20 command supported from this version onwards
            last20Command();
        } else {
            ProceedToNextCommand();
        }
    }

    //region Rename Command
    private void renameCommand() {
        try {
            //Execute rename Command
            Request = "";
            Response = "";

            if (IsThisBTTrnx) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink_3: Sending rename command to Link: " + LinkName + " (New Name: " + BTConstants.BT3REPLACEBLE_WIFI_NAME + ")");
                BTSPPMain btspp = new BTSPPMain();
                btspp.send3(BTConstants.namecommand + BTConstants.BT3REPLACEBLE_WIFI_NAME);
            }
            /*else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink_3: Sending rename command (UDP) to Link: " + LinkName + " (New Name: " + BTConstants.BT3REPLACEBLE_WIFI_NAME + ")");
                new Thread(new ClientSendAndListenUDPThree(BTConstants.namecommand + BTConstants.BT3REPLACEBLE_WIFI_NAME, ipForUDP, this)).start();
            }*/

            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTThree.this).PersonEmail;
            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(this) + ":" + userEmail + ":" + "SetHoseNameReplacedFlag" + AppConstants.LANG_PARAM);

            RenameHose rhose = new RenameHose();
            rhose.SiteId = BTConstants.BT3SITE_ID;
            rhose.HoseId = BTConstants.BT3HOSE_ID;
            rhose.IsHoseNameReplaced = "Y";

            Gson gson = new Gson();
            String jsonData = gson.toJson(rhose);

            storeIsRenameFlag(this, BTConstants.BT3NeedRename, jsonData, authString);

            Thread.sleep(1000);
            ProceedToPostTransactionCommands();

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: rename Command Exception:>>" + e.getMessage());
            ProceedToPostTransactionCommands();
        }
    }
    //endregion

    //region Last20 Command
    private void last20Command() {
        try {
            //Execute last20 Command
            Request = "";
            Response = "";
            IsAnyPostTxnCommandExecuted = true;

            if (IsThisBTTrnx) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink_3: Sending last20 command to Link: " + LinkName);
                BTSPPMain btspp = new BTSPPMain();
                btspp.send3(BTConstants.last20_cmd);
            }

            new CountDownTimer(4000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long attempt = (4 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (Request.equalsIgnoreCase(BTConstants.last20_cmd) && Response.contains("records")) {
                            //last20 command success.
                            Log.i(TAG, "BTLink_3: last20 Command Response success 1:>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink_3: Checking last20 command response. Response: true"); //>>" + Response.trim()
                            parseLast20CommandResponse(Response.trim());
                            ProceedToNextCommand();
                            cancel();
                        } else {
                            Log.i(TAG, "BTLink_3: Waiting for last20 Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink_3: Checking last20 command response. Response: false");
                        }
                    }
                }

                public void onFinish() {
                    if (Request.equalsIgnoreCase(BTConstants.last20_cmd) && Response.contains("records")) {
                        //last20 command success.
                        Log.i(TAG, "BTLink_3: last20 Command Response success 2:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink_3: Checking last20 command response. Response: true"); //>>" + Response.trim()
                        parseLast20CommandResponse(Response.trim());
                    }
                    ProceedToNextCommand();
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: last20 Command Exception:>>" + e.getMessage());
            ProceedToNextCommand();
        }
    }
    //endregion

    private void ProceedToNextCommand() {
        if (CommonUtils.checkBTVersionCompatibility(versionNumberOfLinkThree, BTConstants.supportedLinkVersionForMOStatus)) { // CheckMOStatus command supported from this version onwards
            CheckMOStatusCommand();
        } else if (CommonUtils.checkBTVersionCompatibility(versionNumberOfLinkThree, BTConstants.supportedLinkVersionForP_Type)) { // Set P_Type command supported from this version onwards
            P_Type_Command();
        } else {
            CloseTransaction(false); // ProceedToNextCommand
        }
    }

    //region CheckMOStatus Command
    private void CheckMOStatusCommand() {
        try {
            if (IsCheckMOStatus != null) {
                if (IsCheckMOStatus.trim().equalsIgnoreCase("True") && !MOStatusCheckFlag.isEmpty() && !CommonUtils.CheckDataStoredInSharedPref(BackgroundService_BTThree.this, "storeCheckMOStatusFlag3")) {
                    //Execute CheckMOStatus Command
                    Request = "";
                    Response = "";
                    IsAnyPostTxnCommandExecuted = true;

                    if (IsThisBTTrnx) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink_3: Sending (Check MO Status: " + MOStatusCheckFlag + ") command to Link: " + LinkName);
                        BTSPPMain btspp = new BTSPPMain();
                        btspp.send3(BTConstants.checkMOStatus_command + MOStatusCheckFlag);
                    }

                    new CountDownTimer(4000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            long attempt = (4 - (millisUntilFinished / 1000));
                            if (attempt > 0) {
                                if (Request.contains(BTConstants.checkMOStatus_command) && Response.contains("if_check_mo_status")) {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " BTLink_3: Checking (Check MO Status) command response:>> " + Response);
                                    UpdateCheckMOStatusFlagOfLink();
                                    ResetMOCheckFlagCommand();
                                    cancel();
                                } else {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " BTLink_3: Checking (Check MO Status) command response. Response: false");
                                }
                            }
                        }

                        public void onFinish() {
                            if (Request.contains(BTConstants.checkMOStatus_command) && Response.contains("if_check_mo_status")) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BTLink_3: Checking (Check MO Status) command response:>> " + Response);
                                UpdateCheckMOStatusFlagOfLink();
                            }
                            ResetMOCheckFlagCommand();
                        }
                    }.start();
                } else {
                    ResetMOCheckFlagCommand();
                }
            } else {
                ResetMOCheckFlagCommand();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: Check MO Status Command Exception:>>" + e.getMessage());
            ResetMOCheckFlagCommand();
        }
    }
    //endregion

    //region ResetMOCheckFlag Command
    private void ResetMOCheckFlagCommand() {
        try {
            if (IsResetMOCheckFlag != null) {
                if (IsResetMOCheckFlag.trim().equalsIgnoreCase("True") && !CommonUtils.CheckDataStoredInSharedPref(BackgroundService_BTThree.this, "storeResetMOCheckFlag3")) {
                    //Execute ResetMOCheckFlag Command
                    Request = "";
                    Response = "";
                    IsAnyPostTxnCommandExecuted = true;

                    if (IsThisBTTrnx) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink_3: Sending Reset MO Check Flag command to Link: " + LinkName);
                        BTSPPMain btspp = new BTSPPMain();
                        btspp.send3(BTConstants.resetMOCheckFlag_command);
                    }

                    new CountDownTimer(4000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            long attempt = (4 - (millisUntilFinished / 1000));
                            if (attempt > 0) {
                                if (Request.contains(BTConstants.resetMOCheckFlag_command) && Response.contains("mo_check_flag")) {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " BTLink_3: Checking Reset MO Check Flag command response:>> " + Response);
                                    UpdateResetMOCheckFlagOfLink();
                                    P_Type_Command();
                                    cancel();
                                } else {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " BTLink_3: Checking Reset MO Check Flag command response. Response: false");
                                }
                            }
                        }

                        public void onFinish() {
                            if (Request.contains(BTConstants.resetMOCheckFlag_command) && Response.contains("mo_check_flag")) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BTLink_3: Checking Reset MO Check Flag command response:>> " + Response);
                                UpdateResetMOCheckFlagOfLink();
                            }
                            P_Type_Command();
                        }
                    }.start();
                } else {
                    P_Type_Command();
                }
            } else {
                P_Type_Command();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: Reset MO Check Flag Command Exception:>>" + e.getMessage());
            P_Type_Command();
        }
    }
    //endregion

    //region P_Type Command
    private void P_Type_Command() {
        boolean isSetPTypeCommandSent = false;
        try {
            if (IsResetSwitchTimeBounce != null) {
                if (IsResetSwitchTimeBounce.trim().equalsIgnoreCase("1") && !PulserTimingAdjust.isEmpty() && Arrays.asList(BTConstants.p_types).contains(PulserTimingAdjust) && !CommonUtils.CheckDataStoredInSharedPref(BackgroundService_BTThree.this, "storeSwitchTimeBounceFlag3")) {
                    //Execute p_type Command
                    Request = "";
                    Response = "";
                    IsAnyPostTxnCommandExecuted = true;

                    if (IsThisBTTrnx) {
                        isSetPTypeCommandSent = true;
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink_3: Sending set p_type command to Link: " + LinkName);
                        BTSPPMain btspp = new BTSPPMain();
                        btspp.send3(BTConstants.p_type_command + PulserTimingAdjust);
                    }

                    new CountDownTimer(4000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            long attempt = (4 - (millisUntilFinished / 1000));
                            if (attempt > 0) {
                                if (Request.contains(BTConstants.p_type_command) && Response.contains("pulser_type")) {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " BTLink_3: Checking set p_type command response:>> " + Response);
                                    //BTConstants.isPTypeCommandExecuted3 = true;
                                    UpdateSwitchTimeBounceForLink();
                                    CloseTransaction(true); // set p_type command success
                                    cancel();
                                } else {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " BTLink_3: Checking set p_type command response. Response: false");
                                }
                            }
                        }

                        public void onFinish() {
                            if (Request.contains(BTConstants.p_type_command) && Response.contains("pulser_type")) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BTLink_3: Checking set p_type command response:>> " + Response);
                                //BTConstants.isPTypeCommandExecuted3 = true;
                                UpdateSwitchTimeBounceForLink();
                            }
                            CloseTransaction(true); // set p_type command finish
                        }
                    }.start();
                } else {
                    GetPulserTypeCommand();
                }
            } else {
                GetPulserTypeCommand();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: Set P_Type Command Exception:>>" + e.getMessage());
            if (isSetPTypeCommandSent) {
                CloseTransaction(true); // Set P_Type Command Exception
            } else {
                GetPulserTypeCommand();
            }
        }
    }
    //endregion

    //region Get P_Type Command
    private void GetPulserTypeCommand() {
        try {
            if (GetPulserTypeFromLINK != null) {
                if (GetPulserTypeFromLINK.trim().equalsIgnoreCase("True") && !CommonUtils.CheckDataStoredInSharedPref(BackgroundService_BTThree.this, "UpdatePulserType3")) {
                    //Execute get p_type Command (to get the pulser type from LINK)
                    Request = "";
                    Response = "";
                    IsAnyPostTxnCommandExecuted = true;

                    if (IsThisBTTrnx) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink_3: Sending get p_type command to Link: " + LinkName);
                        BTSPPMain btspp = new BTSPPMain();
                        btspp.send3(BTConstants.get_p_type_command);
                    }

                    new CountDownTimer(4000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            long attempt = (4 - (millisUntilFinished / 1000));
                            if (attempt > 0) {
                                if (Request.contains(BTConstants.get_p_type_command) && Response.contains("pulser_type")) {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " BTLink_3: Checking get p_type command response:>> " + Response);
                                    ParseGetPulserTypeCommandResponse(Response.trim());
                                    CloseTransaction(true); // get p_type command success
                                    cancel();
                                }
                            }
                        }

                        public void onFinish() {
                            if (Request.contains(BTConstants.get_p_type_command) && Response.contains("pulser_type")) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BTLink_3: Checking get p_type command response:>> " + Response);
                                ParseGetPulserTypeCommandResponse(Response.trim());
                            }
                            CloseTransaction(true); // get p_type command finish
                        }
                    }.start();
                } else {
                    CloseTransaction(true); // after checking GetPulserTypeFromLINK
                }
            } else {
                CloseTransaction(true); // GetPulserTypeFromLINK flag is null
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: Get P_Type Command (to get the pulser type from LINK) Exception:>>" + e.getMessage());
            CloseTransaction(true); // Get P_Type Command Exception
        }
    }
    //endregion

    private void StopTransaction(boolean startBackgroundServices, boolean isTransactionCompleted) {
        try {
            AppConstants.IsTransactionCompleted3 = false;
            BTConstants.isRelayOnAfterReconnect3 = false;
            AppConstants.clearSharedPrefByName(BackgroundService_BTThree.this, "LastQuantity_BT3");
            CommonUtils.AddRemovecurrentTransactionList(false, TransactionId);
            Constants.FS_3STATUS = "FREE";
            Constants.FS_3Pulse = "00";
            AppConstants.GoButtonAlreadyClicked = false;
            AppConstants.isInfoCommandSuccess_fs3 = false;
            //BTConstants.SwitchedBTToUDP3 = false;
            //DisableWifiConnection();
            CancelTimer();
            IsAnyPostTxnCommandExecuted = true;
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: Transaction stopped.");
            if (isTransactionCompleted) {
                CloseTransaction(startBackgroundServices); // from StopTransaction
            } else if (startBackgroundServices) {
                PostTransactionBackgroundTasks(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: StopTransaction Exception:>>" + e.getMessage());
            CloseTransaction(startBackgroundServices); // from StopTransaction exception
        }
    }

    private void CloseTransaction(boolean startBackgroundServices) {
        clearEditTextFields();
        AppConstants.IsTransactionCompleted3 = true;
        try {
            try {
                if (isBroadcastReceiverRegistered) {
                    unregisterReceiver(broadcastBlueLinkThreeData);
                    isBroadcastReceiverRegistered = false;
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink_3: <Receiver unregistered successfully. (" + broadcastBlueLinkThreeData + ")>");
                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink_3: <Receiver is not registered. (" + broadcastBlueLinkThreeData + ")>");
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink_3: <Exception occurred while unregistering receiver: " + e.getMessage() + " (" + broadcastBlueLinkThreeData + ")>");
            }
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: Transaction Completed. \n==============================================================================");
            if (startBackgroundServices) {
                PostTransactionBackgroundTasks(true);
            }
            this.stopSelf();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: CloseTransaction Exception:>>" + e.getMessage());
        }
    }

    private void ParseGetPulserTypeCommandResponse(String response) {
        try {
            String pulserType;

            if (response.contains("pulser_type")) {
                JSONObject jsonObj = new JSONObject(response);
                pulserType = jsonObj.getString("pulser_type");

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink_3: Pulser Type from Link >> " + pulserType);
                if (!pulserType.isEmpty() && Arrays.asList(BTConstants.p_types).contains(pulserType)) {
                    // Create object and save data to upload
                    String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTThree.this).PersonEmail;

                    String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_BTThree.this) + ":" + userEmail + ":" + "UpdatePulserTypeOfLINK" + AppConstants.LANG_PARAM);

                    UpdatePulserTypeOfLINK_entity updatePulserTypeOfLINK = new UpdatePulserTypeOfLINK_entity();
                    updatePulserTypeOfLINK.IMEIUDID = AppConstants.getIMEI(BackgroundService_BTThree.this);
                    updatePulserTypeOfLINK.Email = userEmail;
                    updatePulserTypeOfLINK.SiteId = BTConstants.BT3SITE_ID;
                    updatePulserTypeOfLINK.PulserType = pulserType;
                    updatePulserTypeOfLINK.DateTimeFromApp = AppConstants.currentDateFormat("MM/dd/yyyy HH:mm:ss");

                    Gson gson = new Gson();
                    String jsonData = gson.toJson(updatePulserTypeOfLINK);

                    storePulserTypeDetails(BackgroundService_BTThree.this, jsonData, authString);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: Exception in ParseGetPulserTypeCommandResponse. response>> " + response + "; Exception>>" + e.getMessage());
        }
    }

    private void storePulserTypeDetails(Context context, String jsonData, String authString) {
        try {
            SharedPreferences pref;
            SharedPreferences.Editor editor;

            pref = context.getSharedPreferences("UpdatePulserType3", 0);
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

    private void clearEditTextFields() {
        Constants.AccVehicleNumber_FS3 = "";
        Constants.AccOdoMeter_FS3 = 0;
        Constants.AccDepartmentNumber_FS3 = "";
        Constants.AccPersonnelPIN_FS3 = "";
        Constants.AccOther_FS3 = "";
        Constants.AccVehicleOther_FS3 = "";
        Constants.AccHours_FS3 = 0;
    }

    public void storeIsRenameFlag(Context context, boolean flag, String jsonData, String authString) {
        SharedPreferences pref;

        SharedPreferences.Editor editor;
        pref = context.getSharedPreferences("storeIsRenameFlagFS3", 0);
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
            for (int i = 0; i < TimerList_ReadpulseBT3.size(); i++) {
                TimerList_ReadpulseBT3.get(i).cancel();
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
        timerBt3 = new Timer();
        TimerList_ReadpulseBT3.add(timerBt3);
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                //Repaeting code..
                //CancelTimer(); cancel all once done.

                Log.i(TAG, "BTLink_3: Timer count..");

                String checkPulses;
                if (BTConstants.isNewVersionLinkThree) {
                    checkPulses = "pulse";
                } else {
                    checkPulses = "pulse:";
                }

                if (BTConstants.isReconnectCalled3 && !BTConstants.isRelayOnAfterReconnect3) {
                    CancelTimer();
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkBTLinkStatus("relay");
                        }
                    }, 100);
                    return;
                }

                CheckResponse();

                if (Response.contains(checkPulses) && RelayStatus) {
                    pulseCount = 0;
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pulseCount();
                        }
                    }, 100);

                } else if (!RelayStatus) {
                    if (pulseCount > 1) { // pulseCount > 4
                        //Stop transaction
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                pulseCount();
                            }
                        }, 100);

                        int delay = 100;
                        cancel();
                        /*if (BTConstants.SwitchedBTToUDP3) {
                            DisableWifiConnection();
                            BTConstants.SwitchedBTToUDP3 = false;
                            delay = 1000;
                        }*/
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                TransactionCompleteFunction();
                            }
                        }, delay);

                    } else {
                        pulseCount++;
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                pulseCount();
                            }
                        }, 100);
                        Log.i(TAG, "BTLink_3: Check pulse");
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink_3: Check pulse >> Response: " + Response.trim());
                    }
                } else if (!Response.contains(checkPulses)) {
                    stopCount++;

                    long autoStopSeconds = 0;
                    if (pre_pulse == 0) {
                        autoStopSeconds = Long.parseLong(PumpOnTime);
                    } else {
                        autoStopSeconds = stopAutoFuelSeconds;
                    }

                    if (stopCount >= autoStopSeconds) {
                        if (Pulses <= 0) {
                            CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "4", BackgroundService_BTThree.this);
                        }
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink_3: Auto Stop Hit. Response >> " + Response.trim());
                        stopCount = 0;
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                relayOffCommand();
                            }
                        }, 100);
                    }
                }
            }
        };
        timerBt3.schedule(tt, 1000, 1000);
    }

    private void TerminateBTTxnAfterInterruption() {
        try {
            IsThisBTTrnx = false;
            if (Pulses > 0 || fillqty > 0) {
                if (isOnlineTxn) {
                    CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "10", BackgroundService_BTThree.this);
                } else {
                    offlineController.updateOfflineTransactionStatus(sqlite_id + "", "10");
                }
            } else {
                CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "6", BackgroundService_BTThree.this);
            }
            Log.i(TAG, " BTLink_3: Link not connected. Please try again!");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: Link not connected.");
            BTConstants.isReconnectCalled3 = false;
            AppConstants.TxnFailedCount3++;
            AppConstants.IsTransactionFailed3 = true;
            StopTransaction(true, true); // TerminateBTTxnAfterInterruption
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*private void DisableWifiConnection() {
        try {
            //Disable wifi connection
            WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            if (wifiManagerMM.isWifiEnabled()) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " <Turning OFF the Wifi.>");
                wifiManagerMM.setWifiEnabled(false);
            }
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isHotspotDisabled) {
                        //Enable Hotspot
                        WifiApManager wifiApManager = new WifiApManager(BackgroundService_BTThree.this);
                        if (!CommonUtils.isHotspotEnabled(BackgroundService_BTThree.this) && !AppConstants.isAllLinksAreBTLinks) {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " <Turning ON the Hotspot.>");
                            wifiApManager.setWifiApEnabled(null, true);
                        }
                        isHotspotDisabled = false;
                    }
                    if (isOnlineTxn) {
                        boolean BSRunning = CommonUtils.checkServiceRunning(BackgroundService_BTThree.this, AppConstants.PACKAGE_BACKGROUND_SERVICE);
                        if (!BSRunning) {
                            startService(new Intent(BackgroundService_BTThree.this, BackgroundService.class));
                        }
                    }
                }
            }, 2000);
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: DisableWifiConnection Exception>> " + e.getMessage());
        }
    }*/

    private void pulseCount() {
        try {
            pumpTimingsOnOffFunction();//PumpOn/PumpOff functionality
            String outputQuantity;

            if (BTConstants.isNewVersionLinkThree) {
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
            Constants.FS_3Gallons = (precision.format(fillqty));
            Constants.FS_3Pulse = outputQuantity;

            if (isOnlineTxn) { // || BTConstants.SwitchedBTToUDP3
                UpdateTransactionToSqlite(outputQuantity);
            } else {
                if (Pulses > 0 || fillqty > 0) {
                    offlineController.updateOfflinePulsesQuantity(sqlite_id + "", outputQuantity, fillqty + "", OffLastTXNid);
                }
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Offline >> BTLink_3: LINK:" + LinkName + "; P:" + Integer.parseInt(outputQuantity) + "; Q:" + fillqty);
            }

            reachMaxLimit();

        } catch (Exception e) {
            e.printStackTrace();
            //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " BTLink_3: pulse count Exception>>" + e.getMessage());
        }
    }

    private String addStoredQtyToCurrentQty(String outputQuantity) {
        String newQty = outputQuantity;
        try {

            if (BTConstants.isRelayOnAfterReconnect3) {
                SharedPreferences sharedPrefLastQty = this.getSharedPreferences("LastQuantity_BT3", Context.MODE_PRIVATE);
                long storedPulsesCount = sharedPrefLastQty.getLong("Last_Quantity", 0);

                long quantity = Integer.parseInt(outputQuantity);

                long add_count = storedPulsesCount + quantity;

                outputQuantity = Long.toString(add_count);

                newQty = outputQuantity;
            }
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: addStoredQtyToCurrentQty Exception:" + ex.getMessage());
        }
        return newQty;
    }

    public class BroadcastBlueLinkThreeData extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                Bundle notificationData = intent.getExtras();
                String Action = notificationData.getString("Action");
                if (Action.equalsIgnoreCase("BlueLinkThree")) {
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

                    if (Request.contains(BTConstants.info_cmd)) {
                        if (Response.contains("records")) {
                            JSONObject jsonObject = new JSONObject(Response);
                            jsonObject.remove("records"); // As per #2357
                            Response = jsonObject.toString();
                        }
                    }
                    //Used only for debug
                    Log.i(TAG, "BTLink_3: Link Request>>" + Request);
                    Log.i(TAG, "BTLink_3: Link Response>>" + Response);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink_3: Response from Link >>" + Response);

                    //Set Relay status.
                    if (Request.contains(BTConstants.relay_off_cmd) && Response.contains("OFF")) {
                        RelayStatus = false;
                    } else if (Request.contains(BTConstants.relay_on_cmd) && Response.contains("ON")) {
                        RelayStatus = true;
                        AppConstants.isRelayON_fs3 = true;
                        if (!redpulseloop_on) {
                            ReadPulse();
                        }
                    }

                    if (MOStatusCheckFlag.equalsIgnoreCase("ON")) {
                        if (Response.contains("**")) {
                            if (!isManualOverrideDetected) {
                                isManualOverrideDetected = true;
                                UpdateManualOverrideStatusOfLink();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink_3: <onReceive Exception: " + e.getMessage() + ">");
            }
        }
    }

    //Sqlite code
    private void InsertInitialTransactionToSqlite() {
        String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTThree.this).PersonEmail;
        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_BTThree.this) + ":" + userEmail + ":" + "TransactionComplete" + AppConstants.LANG_PARAM);

        HashMap<String, String> imap = new HashMap<>();
        imap.put("jsonData", "");
        imap.put("authString", authString);

        sqliteID = controller.insertTransactions(imap);
        CommonUtils.AddRemovecurrentTransactionList(true, TransactionId);//Add transaction Id to list
    }

    private void UpdateTransactionToSqlite(String outputQuantity) {
        ////////////////////////////////////-Update transaction ---
        TrazComp authEntityClass = new TrazComp();
        authEntityClass.TransactionId = TransactionId;
        authEntityClass.FuelQuantity = fillqty;
        authEntityClass.AppInfo = " Version:" + CommonUtils.getVersionCode(BackgroundService_BTThree.this) + " " + AppConstants.getDeviceName() + " Android " + Build.VERSION.RELEASE + " " + "--Main Transaction--";
        authEntityClass.TransactionFrom = "A";
        authEntityClass.Pulses = Integer.parseInt(outputQuantity);
        authEntityClass.IsFuelingStop = IsFuelingStop;
        authEntityClass.IsLastTransaction = IsLastTransaction;
        authEntityClass.OverrideQuantity = OverrideQuantity;
        authEntityClass.OverridePulse = OverridePulse;

        Gson gson = new Gson();
        String jsonData = gson.toJson(authEntityClass);

        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + " BTLink_3: ID:" + TransactionId + "; LINK:" + LinkName + "; Pulses:" + Integer.parseInt(outputQuantity) + "; Qty:" + fillqty);

        String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTThree.this).PersonEmail;
        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_BTThree.this) + ":" + userEmail + ":" + "TransactionComplete" + AppConstants.LANG_PARAM);


        HashMap<String, String> imap = new HashMap<>();
        imap.put("jsonData", jsonData);
        imap.put("authString", authString);
        imap.put("sqliteId", sqliteID + "");

        if (Pulses > 0 || fillqty > 0) {

            //in progress (transaction recently started, no new information): Transaction ongoing = 8  --non zero qty
            CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "8", BackgroundService_BTThree.this);
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
            authEntityClass.AppInfo = " Version:" + CommonUtils.getVersionCode(BackgroundService_BTThree.this) + " " + AppConstants.getDeviceName() + " Android " + android.os.Build.VERSION.RELEASE + " " + "--Last Transaction--";
            authEntityClass.TransactionFrom = "A";
            authEntityClass.Pulses = Integer.parseInt(counts);
            authEntityClass.IsFuelingStop = IsFuelingStop;
            authEntityClass.IsLastTransaction = "1";

            Gson gson = new Gson();
            String jsonData = gson.toJson(authEntityClass);

            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: <Last Transaction saved in local DB. LastTXNid:" + txnId + "; LINK:" + LinkName + "; Pulses:" + Integer.parseInt(counts) + "; Qty:" + Lastqty + ">");

            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTThree.this).PersonEmail;
            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_BTThree.this) + ":" + userEmail + ":" + "TransactionComplete" + AppConstants.LANG_PARAM);

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
                AppConstants.WriteinFile(TAG + " BTLink_3: SaveLastBTTransactionInLocalDB Exception: " + e.getMessage());
        }
    }

    private void PostTransactionBackgroundTasks(boolean isTransactionCompleted) {
        try {
            if (isOnlineTxn) {
                if (!isTransactionCompleted) {
                    // Save upgrade details to cloud
                    SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_FS_UPGRADE, Context.MODE_PRIVATE);
                    String hoseid = sharedPref.getString("hoseid_bt3", "");
                    String fsversion = sharedPref.getString("fsversion_bt3", "");

                    UpgradeVersionEntity objEntityClass = new UpgradeVersionEntity();
                    objEntityClass.IMEIUDID = AppConstants.getIMEI(BackgroundService_BTThree.this);
                    objEntityClass.Email = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTThree.this).PersonEmail;
                    objEntityClass.HoseId = hoseid;
                    objEntityClass.Version = fsversion;

                    if (hoseid != null && !hoseid.trim().isEmpty()) {
                        new UpgradeCurrentVersionWithUpgradableVersion(objEntityClass).execute();
                    }
                    //=============================================================
                }

                //boolean BSRunning = CommonUtils.checkServiceRunning(BackgroundService_BTThree.this, AppConstants.PACKAGE_BACKGROUND_SERVICE);
                //if (!BSRunning) {
                if (IsAnyPostTxnCommandExecuted) {
                    IsAnyPostTxnCommandExecuted = false;
                    startService(new Intent(this, BackgroundService.class));
                }
                //}
            }

            if (!isTransactionCompleted) {
                // Offline transaction data sync
                if (OfflineConstants.isOfflineAccess(BackgroundService_BTThree.this)) {
                    SyncOfflineData();
                }
            }
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: PostTransactionBackgroundTasks Exception: " + e.getMessage());
        }
    }

    private void reachMaxLimit() {
        //if quantity reach max limit
        if (minFuelLimit > 0 && fillqty >= minFuelLimit && !isTxnLimitReached) {
            isTxnLimitReached = true;
            Log.i(TAG, "BTLink_3: Auto Stop Hit>> You reached MAX fuel limit.");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: Auto Stop Hit>> " + LimitReachedMessage);
            AppConstants.DisplayToastmaxlimit = true;
            AppConstants.MaxlimitMessage = LimitReachedMessage;
            relayOffCommand(); //RelayOff
        }
    }

    private void pumpTimingsOnOffFunction() {
        try {
            int pumpOnpoint = Integer.parseInt(PumpOnTime);

            if (Pulses <= 0) {//PumpOn Time logic
                stopCount++;
                if (stopCount >= pumpOnpoint) {
                    //Timed out (Start was pressed, and pump on timer hit): Pump Time On limit reached* = 4
                    CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "4", BackgroundService_BTThree.this);
                    Log.i(TAG, " BTLink_3: PumpOnTime Hit>>" + stopCount);
                    stopCount = 0;
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink_3: PumpOnTime Hit.");
                    relayOffCommand(); //RelayOff
                }
            } else {//PumpOff Time logic

                if (!Pulses.equals(pre_pulse)) {
                    stopCount = 0;
                    pre_pulse = Pulses;
                } else {
                    stopCount++;
                }

                if (stopCount >= stopAutoFuelSeconds) {
                    Log.i(TAG, " BTLink_3: PumpOffTime Hit>>" + stopCount);
                    stopCount = 0;
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink_3: PumpOffTime Hit.");
                    relayOffCommand(); //RelayOff
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void CheckResponse() {
        try {
            if (RelayStatus && !BTConstants.CurrentCommand_LinkThree.contains(BTConstants.relay_off_cmd)) {
                if (RespCount < 4) {
                    RespCount++;
                } else {
                    RespCount = 0;
                }

                if (RespCount == 4) {
                    RespCount = 0;
                    //Execute fdcheck counter
                    Log.i(TAG, "BTLink_3: Execute FD Check..>>");

                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        getMainExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                fdCheckCommand();
                            }
                        });
                    } else {
                        fdCheckCommand();
                    }
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
                    AppConstants.WriteinFile(TAG + " BTLink_3: Sending FD_check command to Link: " + LinkName);
                BTSPPMain btspp = new BTSPPMain();
                btspp.send3(BTConstants.fdcheckcommand);
            }
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: FD_check command Exception:>>" + ex.getMessage());
        }
    }

    private void parseInfoCommandResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject versionJsonObj = jsonObject.getJSONObject("version");
            String version = versionJsonObj.getString("version");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: LINK Version >> " + version);
            storeUpgradeFSVersion(BackgroundService_BTThree.this, AppConstants.UP_HoseId_fs3, version);
            versionNumberOfLinkThree = CommonUtils.getVersionFromLink(version);
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: Exception in parseInfoCommandResponse. response>> " + response + "; Exception>>" + e.getMessage());
        }
    }

    public class EntityCmd20Txn {
        ArrayList cmtxtnid_20_record;
        String jsonfromLink;
    }

    private String ReturnQty(String outputQuantity) {
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
            } else {
                return_qty = "0";
            }

        } catch (Exception e) {
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
                                    AppConstants.WriteinFile(TAG + " BTLink_3: Last10 txtn parsing exception:>>" + e.getMessage());
                            }
                        } else {

                            if (res.contains("version:")) {
                                version = res.substring(res.indexOf(":") + 1).trim();
                            }
                            if (!version.isEmpty()) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BTLink_3: LINK Version >> " + version);
                                storeUpgradeFSVersion(BackgroundService_BTThree.this, AppConstants.UP_HoseId_fs3, version);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: Exception in parseInfoCommandResponseForLast10txtn. response>> " + response + "; Exception>>" + e.getMessage());
        }
    }

    public void offlineLogicBT3() {
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

            EnablePrinter = offlineController.getOfflineHubDetails(BackgroundService_BTThree.this).EnablePrinter;

            minFuelLimit = OfflineConstants.getFuelLimit(BackgroundService_BTThree.this);
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: <Fuel Limit: " + minFuelLimit + ">");
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

            if (isOnlineTxn) {
                try {
                    //sync offline transactions
                    String off_json = offlineController.getAllOfflineTransactionJSON(BackgroundService_BTThree.this);
                    JSONObject jsonObj = new JSONObject(off_json);
                    String offTransactionArray = jsonObj.getString("TransactionsModelsObj");
                    JSONArray jArray = new JSONArray(offTransactionArray);

                    if (jArray.length() > 0) {
                        startService(new Intent(BackgroundService_BTThree.this, OffTranzSyncService.class));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void storeUpgradeFSVersion(Context context, String hoseid, String fsversion) {
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.PREF_FS_UPGRADE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("hoseid_bt3", hoseid);
        editor.putString("fsversion_bt3", fsversion);
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
                //AppConstants.WriteinFile(TAG + " BTLink_3: UpgradeCurrentVersionWithUpgradableVersion (" + jsonData + ")");

                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(objUpgrade.IMEIUDID + ":" + objUpgrade.Email + ":" + "UpgradeCurrentVersionWithUgradableVersion" + AppConstants.LANG_PARAM);
                response = serverHandler.PostTextData(BackgroundService_BTThree.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink_3: UpgradeCurrentVersionWithUpgradableVersion Exception: " + ex.getMessage());
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
                    //AppConstants.clearSharedPrefByName(BackgroundService_BTThree.this, Constants.PREF_FS_UPGRADE);
                    // Saving empty value to clear sharedPref
                    storeUpgradeFSVersion(BackgroundService_BTThree.this, "", "");
                }
            } catch (Exception e) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink_3: UpgradeCurrentVersionWithUpgradableVersion onPostExecute Exception: " + e.getMessage());
            }
        }
    }

    private void UpdateSwitchTimeBounceForLink() {
        try {
            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTThree.this).PersonEmail;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_BTThree.this) + ":" + userEmail + ":" + "UpdateSwitchTimeBounceForLink" + AppConstants.LANG_PARAM);

            SwitchTimeBounce switchTimeBounce = new SwitchTimeBounce();
            switchTimeBounce.SiteId = BTConstants.BT3SITE_ID;
            switchTimeBounce.IsResetSwitchTimeBounce = "0";

            Gson gson = new Gson();
            String jsonData = gson.toJson(switchTimeBounce);

            storeSwitchTimeBounceFlag(BackgroundService_BTThree.this, jsonData, authString);

        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: UpdateSwitchTimeBounceForLink Exception: " + ex.getMessage());
        }
    }

    public void storeSwitchTimeBounceFlag(Context context, String jsonData, String authString) {
        try {
            SharedPreferences pref;
            SharedPreferences.Editor editor;

            pref = context.getSharedPreferences("storeSwitchTimeBounceFlag3", 0);
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

    private void parseLast1CommandResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("records");
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject j = jsonArray.getJSONObject(i);
                String txtn = j.getString("txtn");
                String pulse = j.getString("pulse");

                if (!txtn.equalsIgnoreCase("N/A") && !pulse.equalsIgnoreCase("-1")) {
                    SaveLastBTTransactionInLocalDB(txtn, pulse);
                }
            }
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: Exception in parseLast1CommandResponse. response>> " + response + "; Exception>>" + e.getMessage());
        }
    }

    private void parseLast20CommandResponse(String response) {
        try {
            ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
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
                    Log.i(TAG, " Exception while parsing date format.>> " + e.getMessage());
                }

                HashMap<String, String> Hmap = new HashMap<>();
                Hmap.put("TransactionID", txtn);//TransactionID
                Hmap.put("Pulses", pulse);//Pulses
                Hmap.put("FuelQuantity", ReturnQty(pulse));//FuelQuantity
                Hmap.put("TransactionDateTime", date); //TransactionDateTime
                Hmap.put("VehicleId", vehicle); //VehicleId
                Hmap.put("dflag", dflag);

                arrayList.add(Hmap);
            }

            Gson gs = new Gson();
            EntityCmd20Txn ety = new EntityCmd20Txn();
            ety.cmtxtnid_20_record = arrayList;

            String json20txn = gs.toJson(ety);

            SharedPreferences sharedPref = this.getSharedPreferences("storeCmtxtnid_20_record", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("LINK3", json20txn);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: Exception in parseLast20CommandResponse. response>> " + response + "; Exception>>" + e.getMessage());
        }
    }

    private void UpdateCheckMOStatusFlagOfLink() {
        try {
            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTThree.this).PersonEmail;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_BTThree.this) + ":" + userEmail + ":" + "UpdateCheckMOStatusFlagOfLink" + AppConstants.LANG_PARAM);

            ManualOverrideStatus manualOverrideStatus = new ManualOverrideStatus();
            manualOverrideStatus.SiteId = BTConstants.BT3SITE_ID;

            Gson gson = new Gson();
            String jsonData = gson.toJson(manualOverrideStatus);

            SharedPreferences pref;
            SharedPreferences.Editor editor;

            pref = BackgroundService_BTThree.this.getSharedPreferences("storeCheckMOStatusFlag3", 0);
            editor = pref.edit();

            // Storing
            editor.putString("jsonData", jsonData);
            editor.putString("authString", authString);

            // commit changes
            editor.commit();
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: UpdateCheckMOStatusFlagOfLink Exception: " + ex.getMessage());
        }
    }

    private void UpdateResetMOCheckFlagOfLink() {
        try {
            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTThree.this).PersonEmail;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_BTThree.this) + ":" + userEmail + ":" + "ResetManualOverrideStatusOfLink" + AppConstants.LANG_PARAM);

            ManualOverrideStatus manualOverrideStatus = new ManualOverrideStatus();
            manualOverrideStatus.SiteId = BTConstants.BT3SITE_ID;

            Gson gson = new Gson();
            String jsonData = gson.toJson(manualOverrideStatus);

            SharedPreferences pref;
            SharedPreferences.Editor editor;

            pref = BackgroundService_BTThree.this.getSharedPreferences("storeResetMOCheckFlag3", 0);
            editor = pref.edit();

            // Storing
            editor.putString("jsonData", jsonData);
            editor.putString("authString", authString);

            // commit changes
            editor.commit();
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: UpdateResetMOCheckFlagOfLink Exception: " + ex.getMessage());
        }
    }

    private void UpdateManualOverrideStatusOfLink() {
        try {
            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTThree.this).PersonEmail;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_BTThree.this) + ":" + userEmail + ":" + "UpdateManualOverrideStatusOfLink" + AppConstants.LANG_PARAM);

            ManualOverrideStatus manualOverrideStatus = new ManualOverrideStatus();
            manualOverrideStatus.SiteId = BTConstants.BT3SITE_ID;

            Gson gson = new Gson();
            String jsonData = gson.toJson(manualOverrideStatus);

            SharedPreferences pref;
            SharedPreferences.Editor editor;

            pref = BackgroundService_BTThree.this.getSharedPreferences("storeManualOverrideStatus3", 0);
            editor = pref.edit();

            // Storing
            editor.putString("jsonData", jsonData);
            editor.putString("authString", authString);
            editor.putString("isServerCallInProgress", "false");

            // commit changes
            editor.commit();
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink_3: UpdateManualOverrideStatusOfLink Exception: " + ex.getMessage());
        }
    }

}
