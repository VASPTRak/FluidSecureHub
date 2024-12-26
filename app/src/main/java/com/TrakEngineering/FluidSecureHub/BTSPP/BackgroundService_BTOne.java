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

public class BackgroundService_BTOne extends Service {

    private static final String TAG = AppConstants.LOG_TXTN_BT + "-"; // + BackgroundService_BTOne.class.getSimpleName();
    public long sqlite_id = 0;
    String TransactionId, VehicleId, PhoneNumber, PersonId, PulseRatio, MinLimit, FuelTypeId, ServerDate, IntervalToStopFuel, IsTLDCall, EnablePrinter, PumpOnTime, LimitReachedMessage, VehicleNumber, TransactionDateWithFormat;
    public BroadcastBlueLinkOneData broadcastBlueLinkOneData = null;
    String Request = "", Response = "";
    String FDRequest = "", FDResponse = "";
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
    Timer timerBt1;
    List<Timer> timerList_ReadPulseBT1 = new ArrayList<Timer>();
    DBController controller = new DBController(BackgroundService_BTOne.this);
    Boolean IsThisBTTrnx;
    boolean isBroadcastReceiverRegistered = false;
    String OffLastTXNid = "0";
    ConnectionDetector cd = new ConnectionDetector(BackgroundService_BTOne.this);
    OffDBController offlineController = new OffDBController(BackgroundService_BTOne.this);
    //String ipForUDP = "192.168.4.1"; // Removed UDP code as per #2603
    public int infoCommandAttempt = 0;
    public boolean isConnected = false;
    public boolean isHotspotDisabled = false;
    public boolean isOnlineTxn = true;
    public String versionNumberOfLinkOne = "";
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
                stopTransaction(false, true); // extras == null
            } else {
                sqlite_id = (long) extras.get("sqlite_id");
                SERVER_IP = String.valueOf(extras.get("SERVER_IP"));
                Request = "";
                Request = "";
                stopCount = 0;
                Log.i(TAG, "-Started-");
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " BTLink_1: -Started-");

                Constants.FS_1_STATUS = "BUSY";

                SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_VEHI_FUEL, Context.MODE_PRIVATE);
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
                LimitReachedMessage = sharedPref.getString("LimitReachedMessage_FS1", "");

                numPulseRatio = Double.parseDouble(PulseRatio);
                minFuelLimit = Double.parseDouble(MinLimit);
                stopAutoFuelSeconds = Long.parseLong(IntervalToStopFuel);

                SharedPreferences calibrationPref = this.getSharedPreferences(Constants.PREF_CALIBRATION_DETAILS, Context.MODE_PRIVATE);
                PulserTimingAdjust = calibrationPref.getString("PulserTimingAdjust_FS1", "");
                IsResetSwitchTimeBounce = calibrationPref.getString("IsResetSwitchTimeBounce_FS1", "0");
                GetPulserTypeFromLINK = calibrationPref.getString("GetPulserTypeFromLINK_FS1", "False");

                SharedPreferences moStatusPref = this.getSharedPreferences(Constants.PREF_MO_STATUS_DETAILS, Context.MODE_PRIVATE);
                MOStatusCheckFlag = moStatusPref.getString("MOStatusCheckFlag_FS1", "OFF");
                IsCheckMOStatus = moStatusPref.getString("IsCheckMOStatus_FS1", "False");
                IsResetMOCheckFlag = moStatusPref.getString("IsResetMOCheckFlag_FS1", "False");

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
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + " BTLink_1:-Offline mode--");
                    offlineLogicBT1();
                }

                //Register Broadcast receiver
                broadcastBlueLinkOneData = new BroadcastBlueLinkOneData();
                IntentFilter intentFilter = new IntentFilter("BroadcastBlueLinkOneData");
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " BTLink_1: <Registering Broadcast Receiver.>");
                registerReceiver(broadcastBlueLinkOneData, intentFilter);
                isBroadcastReceiverRegistered = true;
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " BTLink_1: <Registered successfully. (" + broadcastBlueLinkOneData + ")>");

                AppConstants.IS_RELAY_ON_FS1 = false;
                LinkName = CommonUtils.getLinkName(0);
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
                    Log.i(TAG, " BTLink_1: Something went Wrong in hose selection.");
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + " BTLink_1: Something went wrong in hose selection. (Link CommType: " + LinkCommunicationType + ")");
                    stopTransaction(false, true); // Link CommType unknown
                    this.stopSelf();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Service.START_NOT_STICKY;
    }

    private void terminateBTTransaction() {
        try {
            IsThisBTTrnx = false;
            CommonUtils.upgradeTransactionStatusToSqlite(TransactionId, "6", BackgroundService_BTOne.this);
            Log.i(TAG, " BTLink_1: Link not connected. Please try again!");
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: Link not connected.");
            AppConstants.TRANSACTION_FAILED_COUNT_1++;
            AppConstants.IS_TRANSACTION_FAILED_1 = true;
            stopTransaction(true, true); // terminateBTTransaction
            this.stopSelf();
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: Exception in terminateBTTransaction: " + e.getMessage());
        }
    }

    /*public void proceedToInfoCommand() {
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
    }*/

    private void checkBTLinkStatus(String nextAction) {
        try {
            new CountDownTimer(10000, 2000) {
                public void onTick(long millisUntilFinished) {
                    if (BTConstants.BT_STATUS_STR_ONE.equalsIgnoreCase("Connected")) {
                        isConnected = true;
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " BTLink_1: Link is connected.");
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
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " BTLink_1: Checking Connection Status...");
                    }
                }

                public void onFinish() {
                    if (BTConstants.BT_STATUS_STR_ONE.equalsIgnoreCase("Connected")) {
                        isConnected = true;
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " BTLink_1: Link is connected.");
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
                            terminateBTTransaction(); //UDPFunctionalityAfterBTFailure();
                        } else if (nextAction.equalsIgnoreCase("relay")) { // Terminate BT Txn After Interruption
                            terminateBTTxnAfterInterruption();
                        }
                    }
                }
            }.start();
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: checkBTLinkStatus Exception:>>" + e.getMessage());
            if (nextAction.equalsIgnoreCase("info")) { // Terminate BT Transaction
                terminateBTTransaction();
            } else if (nextAction.equalsIgnoreCase("relay")) { // Terminate BT Txn After Interruption
                terminateBTTxnAfterInterruption();
            }
        }
    }

    //region Info Command
    private void infoCommand() {
        try {
            BTConstants.IS_NEW_VERSION_LINK_ONE = false;
            AppConstants.TRANSACTION_FAILED_COUNT_1 = 0;
            AppConstants.IS_FIRST_COMMAND_SUCCESS_FS1 = false;
            //Execute info command
            Request = "";
            Response = "";
            if (IsThisBTTrnx) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " BTLink_1: Sending Info command to Link: " + LinkName);
                BTSPPMain btspp = new BTSPPMain();
                btspp.send1(BTConstants.INFO_COMMAND);
            }

            new CountDownTimer(5000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long attempt = (5 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (Request.equalsIgnoreCase(BTConstants.INFO_COMMAND) && !Response.equalsIgnoreCase("")) {
                            //Info command success.
                            Log.i(TAG, "BTLink_1: InfoCommand Response success 1:>>" + Response);

                            if (!TransactionId.isEmpty()) {
                                if (Response.contains("mac_address")) {
                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(TAG + " BTLink_1: Checking Info command response. Response: true");
                                    BTConstants.IS_NEW_VERSION_LINK_ONE = true;
                                    parseInfoCommandResponse(Response); // parse info command response
                                    Response = "";
                                } else {
                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(TAG + " BTLink_1: Checking Info command response. Response:>>" + Response.trim());
                                    parseInfoCommandResponseForLast10txtn(Response.trim()); // parse last 10 Txtn
                                }
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        AppConstants.IS_FIRST_COMMAND_SUCCESS_FS1 = true;
                                        //if (IsThisBTTrnx && BTConstants.IS_NEW_VERSION_LINK_ONE && CommonUtils.checkBTVersionCompatibility(versionNumberOfLinkOne, BTConstants.SUPPORTED_LINK_VERSION_FOR_LAST1)) {
                                        //    last1Command();
                                        //} else {
                                        transactionIdCommand(TransactionId);
                                        //}
                                    }
                                }, 1000);
                            } else {
                                Log.i(TAG, "BTLink_1: TransactionId is empty.");
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + " BTLink_1: TransactionId is empty.");
                                stopTransaction(false, true); // TransactionId is empty in infoCommand
                            }
                            cancel();
                        } else {
                            Log.i(TAG, "BTLink_1: Waiting for infoCommand Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " BTLink_1: Checking Info command response. Response: false");
                        }
                    }
                }

                public void onFinish() {
                    if (Request.equalsIgnoreCase(BTConstants.INFO_COMMAND) && !Response.equalsIgnoreCase("")) {
                        //Info command success.
                        Log.i(TAG, "BTLink_1: InfoCommand Response success 2:>>" + Response);

                        if (!TransactionId.isEmpty()) {
                            if (Response.contains("mac_address")) {
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + " BTLink_1: Checking Info command response. Response: true");
                                BTConstants.IS_NEW_VERSION_LINK_ONE = true;
                                parseInfoCommandResponse(Response); // parse info command response
                                Response = "";
                            } else {
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + " BTLink_1: Checking Info command response. Response:>>" + Response.trim());
                                parseInfoCommandResponseForLast10txtn(Response.trim()); // parse last 10 Txtn
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    AppConstants.IS_FIRST_COMMAND_SUCCESS_FS1 = true;
                                    //if (IsThisBTTrnx && BTConstants.IS_NEW_VERSION_LINK_ONE && CommonUtils.checkBTVersionCompatibility(versionNumberOfLinkOne, BTConstants.SUPPORTED_LINK_VERSION_FOR_LAST1)) {
                                    //    last1Command();
                                    //} else {
                                    transactionIdCommand(TransactionId);
                                    //}
                                }
                            }, 1000);
                        } else {
                            Log.i(TAG, "BTLink_1: TransactionId is empty.");
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " BTLink_1: TransactionId is empty.");
                            stopTransaction(false, true); // TransactionId is empty in infoCommand onFinish
                        }
                    } else {
                        if (infoCommandAttempt > 0) {
                            //UpgradeTransaction Status info command fail.
                            CommonUtils.upgradeTransactionStatusToSqlite(TransactionId, "6", BackgroundService_BTOne.this);
                            Log.i(TAG, "BTLink_1: Failed to get infoCommand Response:>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " BTLink_1: Checking Info command response. Response: false");
                            AppConstants.TRANSACTION_FAILED_COUNT_1++;
                            AppConstants.IS_TRANSACTION_FAILED_1 = true;
                            stopTransaction(true, true); // Info command Response: false
                        } else {
                            infoCommandAttempt++;
                            if (BTConstants.BT_STATUS_STR_ONE.equalsIgnoreCase("Connected")) {
                                infoCommand(); // Retried one more time after failed to receive response from info command
                            } else {
                                BTConstants.RETRY_CONN_FOR_INFO_COMMAND1 = true;
                                waitForReconnectToLink();
                            }
                        }
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: infoCommand Exception:>>" + e.getMessage());
            stopTransaction(true, true); // Info command Exception
        }
    }

    public void waitForReconnectToLink() {
        try {
            new CountDownTimer(10000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long attempt = (10 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (BTConstants.BT_STATUS_STR_ONE.equalsIgnoreCase("Connected")) {
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " BTLink_1: Connected to Link: " + LinkName);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    infoCommand(); // Retried one more time after failed to receive response from info command
                                }
                            }, 500);
                            cancel();
                        } else {
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " BTLink_1: Waiting for Reconnect to Link: " + LinkName);
                        }
                    }
                }

                public void onFinish() {
                    if (BTConstants.BT_STATUS_STR_ONE.equalsIgnoreCase("Connected")) {
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " BTLink_1: Connected to Link: " + LinkName);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                infoCommand(); // Retried one more time after failed to receive response from info command
                            }
                        }, 500);
                    } else {
                        terminateBTTransaction();
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: waitForReconnectToLink Exception:>>" + e.getMessage());
            terminateBTTransaction();
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
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " BTLink_1: Sending last1 command to Link: " + LinkName);
                BTSPPMain btspp = new BTSPPMain();
                btspp.send1(BTConstants.LAST1_COMMAND);
            }

            new CountDownTimer(4000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long attempt = (4 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (Request.equalsIgnoreCase(BTConstants.LAST1_COMMAND) && Response.contains("records")) {
                            //last1 command success.
                            Log.i(TAG, "BTLink_1: last1 Command Response success 1:>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " BTLink_1: Checking last1 command response. Response:>>" + Response.trim());
                            parseLast1CommandResponse(Response);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    transactionIdCommand(TransactionId);
                                }
                            }, 1000);
                            cancel();
                        } else {
                            Log.i(TAG, "BTLink_1: Waiting for last1 Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " BTLink_1: Checking last1 command response. Response: false");
                        }
                    }
                }

                public void onFinish() {
                    if (Request.equalsIgnoreCase(BTConstants.LAST1_COMMAND) && Response.contains("records")) {
                        //last1 command success.
                        Log.i(TAG, "BTLink_1: last1 Command Response success 2:>>" + Response);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " BTLink_1: Checking last1 command response. Response:>>" + Response.trim());
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
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: last1 Command Exception:>>" + e.getMessage());
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

            String transaction_id_cmd = BTConstants.TRANSACTION_ID_COMMAND; //LK_COMM=txtnid:

            if (BTConstants.IS_NEW_VERSION_LINK_ONE) {
                TransactionDateWithFormat = BTConstants.parseDateForNewVersion(TransactionDateWithFormat);
                transaction_id_cmd = transaction_id_cmd.replace("txtnid:", ""); // For New version LK_COMM=T:XXXXX;D:XXXXX;V:XXXXXXXX;
                transaction_id_cmd = transaction_id_cmd + "T:" + transactionId + ";D:" + TransactionDateWithFormat + ";V:" + VehicleNumber + ";";
            } else {
                transaction_id_cmd = transaction_id_cmd + transactionId;
            }

            if (IsThisBTTrnx) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " BTLink_1: Sending transactionId command to Link: " + LinkName);
                BTSPPMain btspp = new BTSPPMain();
                btspp.send1(transaction_id_cmd);
            }

            Thread.sleep(500);
            new CountDownTimer(4000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long attempt = (4 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (Request.contains(transactionId) && Response.contains(transactionId)) {
                            //transactionId command success.
                            Log.i(TAG, "BTLink_1: transactionId Command Response success 1:>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " BTLink_1: Checking transactionId command response. Response:>>" + Response.trim());
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    relayOnCommand(false); //RelayOn
                                }
                            }, 1000);
                            cancel();
                        } else {
                            Log.i(TAG, "BTLink_1: Waiting for transactionId Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " BTLink_1: Checking transactionId command response. Response: false");
                        }
                    }
                }

                public void onFinish() {
                    if (Request.contains(transactionId) && Response.contains(transactionId)) {
                        //transactionId command success.
                        Log.i(TAG, "BTLink_1: transactionId Command Response success 2:>>" + Response);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " BTLink_1: Checking transactionId command response. Response:>>" + Response.trim());
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                relayOnCommand(false); //RelayOn
                            }
                        }, 1000);
                    } else {
                        //UpgradeTransaction Status Transactionid command fail.
                        CommonUtils.upgradeTransactionStatusToSqlite(transactionId, "6", BackgroundService_BTOne.this);
                        Log.i(TAG, "BTLink_1: Failed to get transactionId Command Response:>>" + Response);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " BTLink_1: Checking transactionId command response. Response: false");
                        stopTransaction(true, true); // transactionId command Response: false
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: transactionId Command Exception:>>" + e.getMessage());
            stopTransaction(true, true); // transactionId Command Exception
        }
    }
    //endregion

    //region Relay ON Command
    private void relayOnCommand(boolean isAfterReconnect) {
        try {
            if (isAfterReconnect) {
                BTConstants.IS_RECONNECT_CALLED_1 = false;
            }
            //Execute relayOn Command
            Request = "";
            Response = "";

            if (IsThisBTTrnx) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " BTLink_1: Sending relayOn command to Link: " + LinkName);
                BTSPPMain btspp = new BTSPPMain();
                btspp.send1(BTConstants.RELAY_ON_COMMAND);
            }

            if (!isAfterReconnect) {
                insertInitialTransactionToSqlite();//Insert empty transaction into sqlite
            }

            Thread.sleep(500);
            new CountDownTimer(4000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long attempt = (4 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (RelayStatus) {
                            BTConstants.IS_RELAY_ON_AFTER_RECONNECT_1 = isAfterReconnect;
                            //relayOn command success.
                            Log.i(TAG, "BTLink_1: relayOn Command Response success 1:>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " BTLink_1: Checking relayOn command response. Response: ON");
                            cancel();
                        } else {
                            Log.i(TAG, "BTLink_1: Waiting for relayOn Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " BTLink_1: Checking relayOn command response. Response: false");
                        }
                    }
                }

                public void onFinish() {
                    if (RelayStatus) {
                        BTConstants.IS_RELAY_ON_AFTER_RECONNECT_1 = isAfterReconnect;
                        //relayOn command success.
                        Log.i(TAG, "BTLink_1: relayOn Command Response success 2:>>" + Response);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " BTLink_1: Checking relayOn command response. Response: ON");
                    } else {
                        //UpgradeTransaction Status RelayON command fail.
                        if (isAfterReconnect && (Pulses > 0 || fillqty > 0)) {
                            if (isOnlineTxn) {
                                CommonUtils.upgradeTransactionStatusToSqlite(TransactionId, "10", BackgroundService_BTOne.this);
                            } else {
                                offlineController.updateOfflineTransactionStatus(sqlite_id + "", "10");
                            }
                        } else {
                            CommonUtils.upgradeTransactionStatusToSqlite(TransactionId, "6", BackgroundService_BTOne.this);
                        }
                        Log.i(TAG, "BTLink_1: Failed to get relayOn Command Response:>>" + Response);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " BTLink_1: Checking relayOn command response. Response: false");
                        relayOffCommand(); //RelayOff
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: relayOn Command Exception:>>" + e.getMessage());
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
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " BTLink_1: Sending relayOff command to Link: " + LinkName);
                BTSPPMain btspp = new BTSPPMain();
                btspp.send1(BTConstants.RELAY_OFF_COMMAND);
            }

            new CountDownTimer(4000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long attempt = (4 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (!RelayStatus) {
                            //relayOff command success.
                            Log.i(TAG, "BTLink_1: relayOff Command Response success 1:>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " BTLink_1: Checking relayOff command response. Response:>>" + Response.trim());
                            if (!AppConstants.IS_RELAY_ON_FS1) {
                                transactionCompleteFunction();
                            }
                            cancel();
                        } else {
                            Log.i(TAG, "BTLink_1: Waiting for relayOff Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " BTLink_1: Checking relayOff command response. Response: false");
                        }
                    }
                }

                public void onFinish() {
                    if (!RelayStatus) {
                        Log.i(TAG, "BTLink_1: relayOff Command Response success 2:>>" + Response);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " BTLink_1: Checking relayOff command response. Response:>>" + Response.trim());
                    } else {
                        Log.i(TAG, "BTLink_1: Failed to get relayOff Command Response:>>" + Response);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " BTLink_1: Checking relayOff command response. Response: false");
                        if (BTConstants.IS_RELAY_ON_AFTER_RECONNECT_1) {
                            if (Pulses > 0 || fillqty > 0) {
                                if (isOnlineTxn) {
                                    CommonUtils.upgradeTransactionStatusToSqlite(TransactionId, "10", BackgroundService_BTOne.this);
                                } else {
                                    offlineController.updateOfflineTransactionStatus(sqlite_id + "", "10");
                                }
                            }
                        }
                        stopTransaction(true, true);
                    }
                    if (!AppConstants.IS_RELAY_ON_FS1) {
                        transactionCompleteFunction();
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: relayOff Command Exception:>>" + e.getMessage());
            if (!AppConstants.IS_RELAY_ON_FS1) {
                transactionCompleteFunction();
            }
        }
    }
    //endregion

    private void transactionCompleteFunction() {

        if (isOnlineTxn) {
            if (BTConstants.BT1_REPLACEABLE_WIFI_NAME == null) {
                BTConstants.BT1_REPLACEABLE_WIFI_NAME = "";
            }
            //BTLink Rename functionality
            if (BTConstants.BT1_NEED_RENAME && !BTConstants.BT1_REPLACEABLE_WIFI_NAME.isEmpty()) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        renameCommand();
                    }
                }, 1000);
            } else {
                proceedToPostTransactionCommands();
            }
        } else {
            proceedToPostTransactionCommands();
        }
    }

    public void proceedToPostTransactionCommands() {
        // Free the link and continue to post transaction commands
        stopTransaction(true, false); // Free the link
        if (CommonUtils.checkBTVersionCompatibility(versionNumberOfLinkOne, BTConstants.SUPPORTED_LINK_VERSION_FOR_LAST20)) { // Last20 command supported from this version onwards
            last20Command();
        } else {
            proceedToNextCommand();
        }
    }

    //region Rename Command
    private void renameCommand() {
        try {
            //Execute rename Command
            Request = "";
            Response = "";

            if (IsThisBTTrnx) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " BTLink_1: Sending rename command to Link: " + LinkName + " (New Name: " + BTConstants.BT1_REPLACEABLE_WIFI_NAME + ")");
                BTSPPMain btspp = new BTSPPMain();
                btspp.send1(BTConstants.RENAME_COMMAND + BTConstants.BT1_REPLACEABLE_WIFI_NAME);
            }

            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTOne.this).PersonEmail;
            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(this) + ":" + userEmail + ":" + "SetHoseNameReplacedFlag" + AppConstants.LANG_PARAM);

            RenameHose rhose = new RenameHose();
            rhose.SiteId = BTConstants.BT1SITE_ID;
            rhose.HoseId = BTConstants.BT1HOSE_ID;
            rhose.IsHoseNameReplaced = "Y";

            Gson gson = new Gson();
            String jsonData = gson.toJson(rhose);

            storeIsRenameFlag(this, BTConstants.BT1_NEED_RENAME, jsonData, authString);

            Thread.sleep(1000);
            proceedToPostTransactionCommands();

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: rename Command Exception:>>" + e.getMessage());
            proceedToPostTransactionCommands();
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
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " BTLink_1: Sending last20 command to Link: " + LinkName);
                BTSPPMain btspp = new BTSPPMain();
                btspp.send1(BTConstants.LAST20_COMMAND);
            }

            new CountDownTimer(4000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long attempt = (4 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (Request.equalsIgnoreCase(BTConstants.LAST20_COMMAND) && Response.contains("records")) {
                            //last20 command success.
                            Log.i(TAG, "BTLink_1: last20 Command Response success 1:>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " BTLink_1: Checking last20 command response. Response: true"); //>>" + Response.trim()
                            parseLast20CommandResponse(Response.trim());
                            proceedToNextCommand();
                            cancel();
                        } else {
                            Log.i(TAG, "BTLink_1: Waiting for last20 Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " BTLink_1: Checking last20 command response. Response: false");
                        }
                    }
                }

                public void onFinish() {
                    if (Request.equalsIgnoreCase(BTConstants.LAST20_COMMAND) && Response.contains("records")) {
                        //last20 command success.
                        Log.i(TAG, "BTLink_1: last20 Command Response success 2:>>" + Response);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " BTLink_1: Checking last20 command response. Response: true"); //>>" + Response.trim()
                        parseLast20CommandResponse(Response.trim());
                    }
                    proceedToNextCommand();
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: last20 Command Exception:>>" + e.getMessage());
            proceedToNextCommand();
        }
    }
    //endregion

    private void proceedToNextCommand() {
        if (CommonUtils.checkBTVersionCompatibility(versionNumberOfLinkOne, BTConstants.SUPPORTED_LINK_VERSION_FOR_MO_STATUS)) { // CheckMOStatus command supported from this version onwards
            checkMOStatusCommand();
        } else if (CommonUtils.checkBTVersionCompatibility(versionNumberOfLinkOne, BTConstants.SUPPORTED_LINK_VERSION_FOR_P_TYPE)) { // Set P_Type command supported from this version onwards
            pTypeCommand();
        } else {
            closeTransaction(false); // proceedToNextCommand
        }
    }

    //region CheckMOStatus Command
    private void checkMOStatusCommand() {
        try {
            if (IsCheckMOStatus != null) {
                if (IsCheckMOStatus.trim().equalsIgnoreCase("True") && !MOStatusCheckFlag.isEmpty() && !CommonUtils.CheckDataStoredInSharedPref(BackgroundService_BTOne.this, "storeCheckMOStatusFlag1")) {
                    //Execute CheckMOStatus Command
                    Request = "";
                    Response = "";
                    IsAnyPostTxnCommandExecuted = true;

                    if (IsThisBTTrnx) {
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " BTLink_1: Sending (Check MO Status: " + MOStatusCheckFlag + ") command to Link: " + LinkName);
                        BTSPPMain btspp = new BTSPPMain();
                        btspp.send1(BTConstants.CHECK_MO_STATUS_COMMAND + MOStatusCheckFlag);
                    }

                    new CountDownTimer(4000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            long attempt = (4 - (millisUntilFinished / 1000));
                            if (attempt > 0) {
                                if (Request.contains(BTConstants.CHECK_MO_STATUS_COMMAND) && Response.contains("if_check_mo_status")) {
                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(TAG + " BTLink_1: Checking (Check MO Status) command response:>> " + Response);
                                    updateCheckMOStatusFlagOfLink();
                                    resetMOCheckFlagCommand();
                                    cancel();
                                } else {
                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(TAG + " BTLink_1: Checking (Check MO Status) command response. Response: false");
                                }
                            }
                        }

                        public void onFinish() {
                            if (Request.contains(BTConstants.CHECK_MO_STATUS_COMMAND) && Response.contains("if_check_mo_status")) {
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + " BTLink_1: Checking (Check MO Status) command response:>> " + Response);
                                updateCheckMOStatusFlagOfLink();
                            }
                            resetMOCheckFlagCommand();
                        }
                    }.start();
                } else {
                    resetMOCheckFlagCommand();
                }
            } else {
                resetMOCheckFlagCommand();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: Check MO Status Command Exception:>>" + e.getMessage());
            resetMOCheckFlagCommand();
        }
    }
    //endregion

    //region ResetMOCheckFlag Command
    private void resetMOCheckFlagCommand() {
        try {
            if (IsResetMOCheckFlag != null) {
                if (IsResetMOCheckFlag.trim().equalsIgnoreCase("True") && !CommonUtils.CheckDataStoredInSharedPref(BackgroundService_BTOne.this, "storeResetMOCheckFlag1")) {
                    //Execute ResetMOCheckFlag Command
                    Request = "";
                    Response = "";
                    IsAnyPostTxnCommandExecuted = true;

                    if (IsThisBTTrnx) {
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " BTLink_1: Sending Reset MO Check Flag command to Link: " + LinkName);
                        BTSPPMain btspp = new BTSPPMain();
                        btspp.send1(BTConstants.RESET_MO_CHECK_FLAG_COMMAND);
                    }

                    new CountDownTimer(4000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            long attempt = (4 - (millisUntilFinished / 1000));
                            if (attempt > 0) {
                                if (Request.contains(BTConstants.RESET_MO_CHECK_FLAG_COMMAND) && Response.contains("mo_check_flag")) {
                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(TAG + " BTLink_1: Checking Reset MO Check Flag command response:>> " + Response);
                                    updateResetMOCheckFlagOfLink();
                                    pTypeCommand();
                                    cancel();
                                } else {
                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(TAG + " BTLink_1: Checking Reset MO Check Flag command response. Response: false");
                                }
                            }
                        }

                        public void onFinish() {
                            if (Request.contains(BTConstants.RESET_MO_CHECK_FLAG_COMMAND) && Response.contains("mo_check_flag")) {
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + " BTLink_1: Checking Reset MO Check Flag command response:>> " + Response);
                                updateResetMOCheckFlagOfLink();
                            }
                            pTypeCommand();
                        }
                    }.start();
                } else {
                    pTypeCommand();
                }
            } else {
                pTypeCommand();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: Reset MO Check Flag Command Exception:>>" + e.getMessage());
            pTypeCommand();
        }
    }
    //endregion

    //region P_Type Command
    private void pTypeCommand() {
        boolean isSetPTypeCommandSent = false;
        try {
            if (IsResetSwitchTimeBounce != null) {
                if (IsResetSwitchTimeBounce.trim().equalsIgnoreCase("1") && !PulserTimingAdjust.isEmpty() && Arrays.asList(BTConstants.P_TYPES).contains(PulserTimingAdjust) && !CommonUtils.CheckDataStoredInSharedPref(BackgroundService_BTOne.this, "storeSwitchTimeBounceFlag1")) {
                    //Execute p_type Command
                    Request = "";
                    Response = "";
                    IsAnyPostTxnCommandExecuted = true;

                    if (IsThisBTTrnx) {
                        isSetPTypeCommandSent = true;
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " BTLink_1: Sending set p_type command to Link: " + LinkName);
                        BTSPPMain btspp = new BTSPPMain();
                        btspp.send1(BTConstants.P_TYPE_COMMAND + PulserTimingAdjust);
                    }

                    new CountDownTimer(4000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            long attempt = (4 - (millisUntilFinished / 1000));
                            if (attempt > 0) {
                                if (Request.contains(BTConstants.P_TYPE_COMMAND) && Response.contains("pulser_type")) {
                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(TAG + " BTLink_1: Checking set p_type command response:>> " + Response);
                                    //BTConstants.isPTypeCommandExecuted1 = true;
                                    updateSwitchTimeBounceForLink();
                                    closeTransaction(true); // set p_type command success
                                    cancel();
                                } else {
                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(TAG + " BTLink_1: Checking set p_type command response. Response: false");
                                }
                            }
                        }

                        public void onFinish() {
                            if (Request.contains(BTConstants.P_TYPE_COMMAND) && Response.contains("pulser_type")) {
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + " BTLink_1: Checking set p_type command response:>> " + Response);
                                //BTConstants.isPTypeCommandExecuted1 = true;
                                updateSwitchTimeBounceForLink();
                            }
                            closeTransaction(true); // set p_type command finish
                        }
                    }.start();
                } else {
                    getPulserTypeCommand();
                }
            } else {
                getPulserTypeCommand();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: Set P_Type Command Exception:>>" + e.getMessage());
            if (isSetPTypeCommandSent) {
                closeTransaction(true); // Set P_Type Command Exception
            } else {
                getPulserTypeCommand();
            }
        }
    }
    //endregion

    //region Get P_Type Command
    private void getPulserTypeCommand() {
        try {
            if (GetPulserTypeFromLINK != null) {
                if (GetPulserTypeFromLINK.trim().equalsIgnoreCase("True") && !CommonUtils.CheckDataStoredInSharedPref(BackgroundService_BTOne.this, "UpdatePulserType1")) {
                    //Execute get p_type Command (to get the pulser type from LINK)
                    Request = "";
                    Response = "";
                    IsAnyPostTxnCommandExecuted = true;

                    if (IsThisBTTrnx) {
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " BTLink_1: Sending get p_type command to Link: " + LinkName);
                        BTSPPMain btspp = new BTSPPMain();
                        btspp.send1(BTConstants.GET_P_TYPE_COMMAND);
                    }

                    new CountDownTimer(4000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            long attempt = (4 - (millisUntilFinished / 1000));
                            if (attempt > 0) {
                                if (Request.contains(BTConstants.GET_P_TYPE_COMMAND) && Response.contains("pulser_type")) {
                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(TAG + " BTLink_1: Checking get p_type command response:>> " + Response);
                                    parseGetPulserTypeCommandResponse(Response.trim());
                                    closeTransaction(true); // get p_type command success
                                    cancel();
                                }
                            }
                        }

                        public void onFinish() {
                            if (Request.contains(BTConstants.GET_P_TYPE_COMMAND) && Response.contains("pulser_type")) {
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + " BTLink_1: Checking get p_type command response:>> " + Response);
                                parseGetPulserTypeCommandResponse(Response.trim());
                            }
                            closeTransaction(true); // get p_type command finish
                        }
                    }.start();
                } else {
                    closeTransaction(true); // after checking GetPulserTypeFromLINK
                }
            } else {
                closeTransaction(true); // GetPulserTypeFromLINK flag is null
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: Get P_Type Command (to get the pulser type from LINK) Exception:>>" + e.getMessage());
            closeTransaction(true); // Get P_Type Command Exception
        }
    }
    //endregion

    private void stopTransaction(boolean startBackgroundServices, boolean isTransactionCompleted) {
        try {
            AppConstants.IS_TRANSACTION_COMPLETED_1 = false;
            BTConstants.IS_RELAY_ON_AFTER_RECONNECT_1 = false;
            AppConstants.clearSharedPrefByName(BackgroundService_BTOne.this, "LastQuantity_BT1");
            CommonUtils.addRemoveCurrentTransactionList(false, TransactionId);
            Constants.FS_1_STATUS = "FREE";
            Constants.FS_1_PULSE = "00";
            AppConstants.GO_BUTTON_ALREADY_CLICKED = false;
            AppConstants.IS_FIRST_COMMAND_SUCCESS_FS1 = false;
            //BTConstants.SwitchedBTToUDP1 = false;
            //DisableWifiConnection();
            cancelTimer();
            IsAnyPostTxnCommandExecuted = true;
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: Transaction stopped.");
            if (isTransactionCompleted) {
                closeTransaction(startBackgroundServices); // from stopTransaction
            } else if (startBackgroundServices) {
                postTransactionBackgroundTasks(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: stopTransaction Exception:>>" + e.getMessage());
            closeTransaction(startBackgroundServices); // from stopTransaction exception
        }
    }

    private void closeTransaction(boolean startBackgroundServices) {
        clearEditTextFields();
        AppConstants.IS_TRANSACTION_COMPLETED_1 = true;
        try {
            try {
                if (isBroadcastReceiverRegistered) {
                    unregisterReceiver(broadcastBlueLinkOneData);
                    isBroadcastReceiverRegistered = false;
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + " BTLink_1: <Receiver unregistered successfully. (" + broadcastBlueLinkOneData + ")>");
                } else {
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + " BTLink_1: <Receiver is not registered. (" + broadcastBlueLinkOneData + ")>");
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " BTLink_1: <Exception occurred while unregistering receiver: " + e.getMessage() + " (" + broadcastBlueLinkOneData + ")>");
            }
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: Transaction Completed. \n==============================================================================");
            if (startBackgroundServices) {
                postTransactionBackgroundTasks(true);
            }
            this.stopSelf();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: closeTransaction Exception:>>" + e.getMessage());
        }
    }

    private void parseGetPulserTypeCommandResponse(String response) {
        try {
            String pulserType;

            if (response.contains("pulser_type")) {
                JSONObject jsonObj = new JSONObject(response);
                pulserType = jsonObj.getString("pulser_type");

                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " BTLink_1: Pulser Type from Link >> " + pulserType);
                if (!pulserType.isEmpty() && Arrays.asList(BTConstants.P_TYPES).contains(pulserType)) {
                    // Create object and save data to upload
                    String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTOne.this).PersonEmail;

                    String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_BTOne.this) + ":" + userEmail + ":" + "UpdatePulserTypeOfLINK" + AppConstants.LANG_PARAM);

                    UpdatePulserTypeOfLINK_entity updatePulserTypeOfLINK = new UpdatePulserTypeOfLINK_entity();
                    updatePulserTypeOfLINK.IMEIUDID = AppConstants.getIMEI(BackgroundService_BTOne.this);
                    updatePulserTypeOfLINK.Email = userEmail;
                    updatePulserTypeOfLINK.SiteId = BTConstants.BT1SITE_ID;
                    updatePulserTypeOfLINK.PulserType = pulserType;
                    updatePulserTypeOfLINK.DateTimeFromApp = AppConstants.currentDateFormat("MM/dd/yyyy HH:mm:ss");

                    Gson gson = new Gson();
                    String jsonData = gson.toJson(updatePulserTypeOfLINK);

                    storePulserTypeDetails(BackgroundService_BTOne.this, jsonData, authString);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: Exception in parseGetPulserTypeCommandResponse. response>> " + response + "; Exception>>" + e.getMessage());
        }
    }

    private void storePulserTypeDetails(Context context, String jsonData, String authString) {
        try {
            SharedPreferences pref;
            SharedPreferences.Editor editor;

            pref = context.getSharedPreferences("UpdatePulserType1", 0);
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
        Constants.VEHICLE_NUMBER_FS1 = "";
        Constants.ODO_METER_FS1 = 0;
        Constants.DEPARTMENT_NUMBER_FS1 = "";
        Constants.PERSONNEL_PIN_FS1 = "";
        Constants.OTHER_FS1 = "";
        Constants.VEHICLE_OTHER_FS1 = "";
        Constants.HOURS_FS1 = 0;
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

    private void cancelTimer() {
        try {
            for (int i = 0; i < timerList_ReadPulseBT1.size(); i++) {
                timerList_ReadPulseBT1.get(i).cancel();
            }
            redpulseloop_on = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readPulse() {
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
        timerList_ReadPulseBT1.add(timerBt1);
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                //Repaeting code..
                //cancelTimer(); cancel all once done.

                Log.i(TAG, "BTLink_1: Timer count..");

                String checkPulses;
                if (BTConstants.IS_NEW_VERSION_LINK_ONE) {
                    checkPulses = "pulse";
                } else {
                    checkPulses = "pulse:";
                }

                if (BTConstants.IS_RECONNECT_CALLED_1 && !BTConstants.IS_RELAY_ON_AFTER_RECONNECT_1) {
                    cancelTimer();
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkBTLinkStatus("relay");
                        }
                    }, 100);
                    return;
                }

                checkResponse();

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
                        /*if (BTConstants.SwitchedBTToUDP1) {
                            DisableWifiConnection();
                            BTConstants.SwitchedBTToUDP1 = false;
                            delay = 1000;
                        }*/
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                transactionCompleteFunction();
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
                        Log.i(TAG, "BTLink_1: Check pulse");
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " BTLink_1: Check pulse >> Response: " + Response.trim());
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
                            CommonUtils.upgradeTransactionStatusToSqlite(TransactionId, "4", BackgroundService_BTOne.this);
                        }
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " BTLink_1: Auto Stop Hit. Response >> " + Response.trim());
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
        timerBt1.schedule(tt, 1000, 1000);
    }

    private void terminateBTTxnAfterInterruption() {
        try {
            IsThisBTTrnx = false;
            if (Pulses > 0 || fillqty > 0) {
                if (isOnlineTxn) {
                    CommonUtils.upgradeTransactionStatusToSqlite(TransactionId, "10", BackgroundService_BTOne.this);
                } else {
                    offlineController.updateOfflineTransactionStatus(sqlite_id + "", "10");
                }
            } else {
                CommonUtils.upgradeTransactionStatusToSqlite(TransactionId, "6", BackgroundService_BTOne.this);
            }
            Log.i(TAG, " BTLink_1: Link not connected. Please try again!");
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: Link not connected.");
            BTConstants.IS_RECONNECT_CALLED_1 = false;
            AppConstants.TRANSACTION_FAILED_COUNT_1++;
            AppConstants.IS_TRANSACTION_FAILED_1 = true;
            stopTransaction(true, true); // terminateBTTxnAfterInterruption
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*private void DisableWifiConnection() {
        try {
            //Disable wifi connection
            WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            if (wifiManagerMM.isWifiEnabled()) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " <Turning OFF the Wifi.>");
                wifiManagerMM.setWifiEnabled(false);
            }
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isHotspotDisabled) {
                        //Enable Hotspot
                        WifiApManager wifiApManager = new WifiApManager(BackgroundService_BTOne.this);
                        if (!CommonUtils.isHotspotEnabled(BackgroundService_BTOne.this) && !AppConstants.IS_ALL_LINKS_ARE_BT_LINKS) {
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " <Turning ON the Hotspot.>");
                            wifiApManager.setWifiApEnabled(null, true);
                        }
                        isHotspotDisabled = false;
                    }
                    if (isOnlineTxn) {
                        boolean BSRunning = CommonUtils.checkServiceRunning(BackgroundService_BTOne.this, AppConstants.PACKAGE_BACKGROUND_SERVICE);
                        if (!BSRunning) {
                            startService(new Intent(BackgroundService_BTOne.this, BackgroundService.class));
                        }
                    }
                }
            }, 2000);
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: DisableWifiConnection Exception>> " + e.getMessage());
        }
    }*/

    private void pulseCount() {
        try {
            pumpTimingsOnOffFunction();//PumpOn/PumpOff functionality
            String outputQuantity;

            if (BTConstants.IS_NEW_VERSION_LINK_ONE) {
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
            Constants.FS_1_GALLONS = (precision.format(fillqty));
            Constants.FS_1_PULSE = outputQuantity;

            if (isOnlineTxn) { // || BTConstants.SwitchedBTToUDP1
                updateTransactionToSqlite(outputQuantity);
            } else {
                if (Pulses > 0 || fillqty > 0) {
                    offlineController.updateOfflinePulsesQuantity(sqlite_id + "", outputQuantity, fillqty + "", OffLastTXNid);
                }
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " Offline >> BTLink_1: LINK:" + LinkName + "; P:" + Integer.parseInt(outputQuantity) + "; Q:" + fillqty);
            }

            reachMaxLimit();

        } catch (Exception e) {
            e.printStackTrace();
            //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile(TAG + " BTLink_1: pulse count Exception>>" + e.getMessage());
        }
    }

    private String addStoredQtyToCurrentQty(String outputQuantity) {
        String newQty = outputQuantity;
        try {

            if (BTConstants.IS_RELAY_ON_AFTER_RECONNECT_1) {
                SharedPreferences sharedPrefLastQty = this.getSharedPreferences("LastQuantity_BT1", Context.MODE_PRIVATE);
                long storedPulsesCount = sharedPrefLastQty.getLong("Last_Quantity", 0);

                long quantity = Integer.parseInt(outputQuantity);

                long add_count = storedPulsesCount + quantity;

                outputQuantity = Long.toString(add_count);

                newQty = outputQuantity;
            }
        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: addStoredQtyToCurrentQty Exception:" + ex.getMessage());
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

                    if (Request.equalsIgnoreCase(BTConstants.FD_CHECK_COMMAND)) {
                        FDRequest = Request;
                        FDResponse = Response;
                    }

                    if (Request.contains(BTConstants.INFO_COMMAND)) {
                        if (Response.contains("records")) {
                            JSONObject jsonObject = new JSONObject(Response);
                            jsonObject.remove("records"); // As per #2357
                            Response = jsonObject.toString();
                        }
                    }
                    //Used only for debug
                    Log.i(TAG, "BTLink_1: Link Request>>" + Request);
                    Log.i(TAG, "BTLink_1: Link Response>>" + Response);
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + " BTLink_1: Response from Link >>" + Response);

                    //Set Relay status.
                    if (Request.contains(BTConstants.RELAY_OFF_COMMAND) && Response.contains("OFF")) {
                        RelayStatus = false;
                    } else if (Request.contains(BTConstants.RELAY_ON_COMMAND) && Response.contains("ON")) {
                        RelayStatus = true;
                        AppConstants.IS_RELAY_ON_FS1 = true;
                        if (!redpulseloop_on) {
                            readPulse();
                        }
                    }

                    if (MOStatusCheckFlag.equalsIgnoreCase("ON")) {
                        if (Response.contains("**")) {
                            if (!isManualOverrideDetected) {
                                isManualOverrideDetected = true;
                                updateManualOverrideStatusOfLink();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " BTLink_1: <onReceive Exception: " + e.getMessage() + ">");
            }
        }
    }

    //Sqlite code
    private void insertInitialTransactionToSqlite() {
        String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTOne.this).PersonEmail;
        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_BTOne.this) + ":" + userEmail + ":" + "TransactionComplete" + AppConstants.LANG_PARAM);

        HashMap<String, String> imap = new HashMap<>();
        imap.put("jsonData", "");
        imap.put("authString", authString);

        sqliteID = controller.insertTransactions(imap);
        CommonUtils.addRemoveCurrentTransactionList(true, TransactionId);//Add transaction Id to list
    }

    private void updateTransactionToSqlite(String outputQuantity) {
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

        if (AppConstants.GENERATE_LOGS)
            AppConstants.writeInFile(TAG + " BTLink_1: ID:" + TransactionId + "; LINK:" + LinkName + "; Pulses:" + Integer.parseInt(outputQuantity) + "; Qty:" + fillqty);

        String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTOne.this).PersonEmail;
        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_BTOne.this) + ":" + userEmail + ":" + "TransactionComplete" + AppConstants.LANG_PARAM);


        HashMap<String, String> imap = new HashMap<>();
        imap.put("jsonData", jsonData);
        imap.put("authString", authString);
        imap.put("sqliteId", sqliteID + "");

        if (Pulses > 0 || fillqty > 0) {

            //in progress (transaction recently started, no new information): Transaction ongoing = 8  --non zero qty
            CommonUtils.upgradeTransactionStatusToSqlite(TransactionId, "8", BackgroundService_BTOne.this);
            int rowseffected = controller.updateTransactions(imap);
            System.out.println("rowseffected-" + rowseffected);
            if (rowseffected == 0) {
                controller.insertTransactions(imap);
            }
        }
    }

    private void saveLastBTTransactionInLocalDB(String txnId, String counts) {
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

            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: <Last Transaction saved in local DB. LastTXNid:" + txnId + "; LINK:" + LinkName + "; Pulses:" + Integer.parseInt(counts) + "; Qty:" + Lastqty + ">");

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
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: saveLastBTTransactionInLocalDB Exception: " + e.getMessage());
        }
    }

    private void postTransactionBackgroundTasks(boolean isTransactionCompleted) {
        try {
            if (isOnlineTxn) {
                if (!isTransactionCompleted) {
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
                        if (AppConstants.IS_SINGLE_LINK) {
                            HashMap<String, String> selSSid = WelcomeActivity.serverSSIDList.get(0);
                            selSSid.put("IsUpgrade", "N");
                            selSSid.put("FirmwareVersion", fsversion);
                            WelcomeActivity.serverSSIDList.set(0, selSSid);
                        }
                        //=============================================================
                    }
                    //=============================================================
                }

                //boolean BSRunning = CommonUtils.checkServiceRunning(BackgroundService_BTOne.this, AppConstants.PACKAGE_BACKGROUND_SERVICE);
                //if (!BSRunning) {
                if (IsAnyPostTxnCommandExecuted) {
                    IsAnyPostTxnCommandExecuted = false;
                    startService(new Intent(this, BackgroundService.class));
                }
                //}
            }

            if (!isTransactionCompleted) {
                // Offline transaction data sync
                if (OfflineConstants.isOfflineAccess(BackgroundService_BTOne.this)) {
                    syncOfflineData();
                }
            }
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: postTransactionBackgroundTasks Exception: " + e.getMessage());
        }
    }

    private void reachMaxLimit() {
        //if quantity reach max limit
        if (minFuelLimit > 0 && fillqty >= minFuelLimit && !isTxnLimitReached) {
            isTxnLimitReached = true;
            Log.i(TAG, "BTLink_1: Auto Stop Hit>> You reached MAX fuel limit.");
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: Auto Stop Hit>> " + LimitReachedMessage);
            AppConstants.DISPLAY_TOAST_MAX_LIMIT = true;
            AppConstants.MAX_LIMIT_MESSAGE = LimitReachedMessage;
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
                    CommonUtils.upgradeTransactionStatusToSqlite(TransactionId, "4", BackgroundService_BTOne.this);
                    Log.i(TAG, " BTLink_1: PumpOnTime Hit>>" + stopCount);
                    stopCount = 0;
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + " BTLink_1: PumpOnTime Hit.");
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
                    Log.i(TAG, " BTLink_1: PumpOffTime Hit>>" + stopCount);
                    stopCount = 0;
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + " BTLink_1: PumpOffTime Hit.");
                    relayOffCommand(); //RelayOff
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void checkResponse() {
        try {
            if (RelayStatus && !BTConstants.CURRENT_COMMAND_LINK_ONE.contains(BTConstants.RELAY_OFF_COMMAND)) {
                if (RespCount < 4) {
                    RespCount++;
                } else {
                    RespCount = 0;
                }

                if (RespCount == 4) {
                    RespCount = 0;
                    //Execute fdcheck counter
                    Log.i(TAG, "BTLink_1: Execute FD Check..>>");

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
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " BTLink_1: Sending FD_check command to Link: " + LinkName);
                BTSPPMain btspp = new BTSPPMain();
                btspp.send1(BTConstants.FD_CHECK_COMMAND);
            }
        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: FD_check command Exception:>>" + ex.getMessage());
        }
    }

    private void parseInfoCommandResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject versionJsonObj = jsonObject.getJSONObject("version");
            String version = versionJsonObj.getString("version");
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: LINK Version >> " + version);
            storeUpgradeFSVersion(BackgroundService_BTOne.this, AppConstants.UP_HOSE_ID_FS1, version);
            versionNumberOfLinkOne = CommonUtils.getVersionFromLink(version);
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: Exception in parseInfoCommandResponse. response>> " + response + "; Exception>>" + e.getMessage());
        }
    }

    public class EntityCmd20Txn {
        ArrayList cmtxtnid_20_record;
        String jsonfromLink;
    }

    private String returnQty(String outputQuantity) {
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
                                        saveLastBTTransactionInLocalDB(txn_id, pulse);
                                    }
                                }
                            } catch (Exception e) {
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + " BTLink_1: Last10 txtn parsing exception:>>" + e.getMessage());
                            }
                        } else {

                            if (res.contains("version:")) {
                                version = res.substring(res.indexOf(":") + 1).trim();
                            }
                            if (!version.isEmpty()) {
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + " BTLink_1: LINK Version >> " + version);
                                storeUpgradeFSVersion(BackgroundService_BTOne.this, AppConstants.UP_HOSE_ID_FS1, version);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: Exception in parseInfoCommandResponseForLast10txtn. response>> " + response + "; Exception>>" + e.getMessage());
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
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: <Fuel Limit: " + minFuelLimit + ">");
            numPulseRatio = Double.parseDouble(PulseRatio);

            stopAutoFuelSeconds = Long.parseLong(IntervalToStopFuel);

            Calendar calendar = Calendar.getInstance();
            TransactionDateWithFormat = BTConstants.DATE_FORMAT_FOR_OLD_VERSION.format(calendar.getTime());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void syncOfflineData() {
        if (Constants.FS_1_STATUS.equalsIgnoreCase("FREE") && Constants.FS_2_STATUS.equalsIgnoreCase("FREE") && Constants.FS_3_STATUS.equalsIgnoreCase("FREE") && Constants.FS_4_STATUS.equalsIgnoreCase("FREE") && Constants.FS_5_STATUS.equalsIgnoreCase("FREE") && Constants.FS_6_STATUS.equalsIgnoreCase("FREE")) {

            if (isOnlineTxn) {
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

    public void storeUpgradeFSVersion(Context context, String hoseid, String fsversion) {
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.PREF_FS_UPGRADE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("hoseid_bt1", hoseid);
        editor.putString("fsversion_bt1", fsversion);
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
                //AppConstants.writeInFile(TAG + " BTLink_1: UpgradeCurrentVersionWithUpgradableVersion (" + jsonData + ")");

                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(objUpgrade.IMEIUDID + ":" + objUpgrade.Email + ":" + "UpgradeCurrentVersionWithUgradableVersion" + AppConstants.LANG_PARAM);
                response = serverHandler.PostTextData(BackgroundService_BTOne.this, AppConstants.WEB_URL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " BTLink_1: UpgradeCurrentVersionWithUpgradableVersion Exception: " + ex.getMessage());
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
                    //AppConstants.clearSharedPrefByName(BackgroundService_BTOne.this, Constants.PREF_FS_UPGRADE);
                    // Saving empty value to clear sharedPref
                    storeUpgradeFSVersion(BackgroundService_BTOne.this, "", "");
                }
            } catch (Exception e) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " BTLink_1: UpgradeCurrentVersionWithUpgradableVersion onPostExecute Exception: " + e.getMessage());
            }
        }
    }

    private void updateSwitchTimeBounceForLink() {
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
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: updateSwitchTimeBounceForLink Exception: " + ex.getMessage());
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

    private void parseLast1CommandResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("records");
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject j = jsonArray.getJSONObject(i);
                String txtn = j.getString("txtn");
                String pulse = j.getString("pulse");

                if (!txtn.equalsIgnoreCase("N/A") && !txtn.isEmpty() && !pulse.equalsIgnoreCase("-1") && !pulse.isEmpty()) {
                    saveLastBTTransactionInLocalDB(txtn, pulse);
                }
            }
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: Exception in parseLast1CommandResponse. response>> " + response + "; Exception>>" + e.getMessage());
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
                Hmap.put("FuelQuantity", returnQty(pulse));//FuelQuantity
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
            editor.putString("LINK1", json20txn);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: Exception in parseLast20CommandResponse. response>> " + response + "; Exception>>" + e.getMessage());
        }
    }

    private void updateCheckMOStatusFlagOfLink() {
        try {
            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTOne.this).PersonEmail;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_BTOne.this) + ":" + userEmail + ":" + "UpdateCheckMOStatusFlagOfLink" + AppConstants.LANG_PARAM);

            ManualOverrideStatus manualOverrideStatus = new ManualOverrideStatus();
            manualOverrideStatus.SiteId = BTConstants.BT1SITE_ID;

            Gson gson = new Gson();
            String jsonData = gson.toJson(manualOverrideStatus);

            SharedPreferences pref;
            SharedPreferences.Editor editor;

            pref = BackgroundService_BTOne.this.getSharedPreferences("storeCheckMOStatusFlag1", 0);
            editor = pref.edit();

            // Storing
            editor.putString("jsonData", jsonData);
            editor.putString("authString", authString);

            // commit changes
            editor.commit();
        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: updateCheckMOStatusFlagOfLink Exception: " + ex.getMessage());
        }
    }

    private void updateResetMOCheckFlagOfLink() {
        try {
            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTOne.this).PersonEmail;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_BTOne.this) + ":" + userEmail + ":" + "ResetManualOverrideStatusOfLink" + AppConstants.LANG_PARAM);

            ManualOverrideStatus manualOverrideStatus = new ManualOverrideStatus();
            manualOverrideStatus.SiteId = BTConstants.BT1SITE_ID;

            Gson gson = new Gson();
            String jsonData = gson.toJson(manualOverrideStatus);

            SharedPreferences pref;
            SharedPreferences.Editor editor;

            pref = BackgroundService_BTOne.this.getSharedPreferences("storeResetMOCheckFlag1", 0);
            editor = pref.edit();

            // Storing
            editor.putString("jsonData", jsonData);
            editor.putString("authString", authString);

            // commit changes
            editor.commit();
        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: updateResetMOCheckFlagOfLink Exception: " + ex.getMessage());
        }
    }

    private void updateManualOverrideStatusOfLink() {
        try {
            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTOne.this).PersonEmail;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_BTOne.this) + ":" + userEmail + ":" + "UpdateManualOverrideStatusOfLink" + AppConstants.LANG_PARAM);

            ManualOverrideStatus manualOverrideStatus = new ManualOverrideStatus();
            manualOverrideStatus.SiteId = BTConstants.BT1SITE_ID;

            Gson gson = new Gson();
            String jsonData = gson.toJson(manualOverrideStatus);

            SharedPreferences pref;
            SharedPreferences.Editor editor;

            pref = BackgroundService_BTOne.this.getSharedPreferences("storeManualOverrideStatus1", 0);
            editor = pref.edit();

            // Storing
            editor.putString("jsonData", jsonData);
            editor.putString("authString", authString);
            editor.putString("isServerCallInProgress", "false");

            // commit changes
            editor.commit();
        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " BTLink_1: updateManualOverrideStatusOfLink Exception: " + ex.getMessage());
        }
    }

}
