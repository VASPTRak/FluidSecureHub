package com.TrakEngineering.FluidSecureHub.BTSPP;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.AppConstants;
import com.TrakEngineering.FluidSecureHub.BackgroundService;
import com.TrakEngineering.FluidSecureHub.CommonUtils;
import com.TrakEngineering.FluidSecureHub.Constants;
import com.TrakEngineering.FluidSecureHub.DBController;
import com.TrakEngineering.FluidSecureHub.WelcomeActivity;
import com.TrakEngineering.FluidSecureHub.enity.RenameHose;
import com.TrakEngineering.FluidSecureHub.enity.TrazComp;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.RequiresApi;

public class BackgroundService_BTFour extends Service {

    private static final String TAG = BackgroundService_BTFour.class.getSimpleName();
    public long sqlite_id = 0;
    String TransactionId, VehicleId, PhoneNumber, PersonId, PulseRatio, MinLimit, FuelTypeId, ServerDate, IntervalToStopFuel, IsTLDCall, EnablePrinter, PumpOnTime;
    public BackgroundService_BTFour.BroadcastBlueLinkFourData broadcastBlueLinkFourData = null;
    String Request = "", Response = "";
    String FDRequest = "", FDResponse = "";
    int PreviousRes = 0;
    boolean stopTxtprocess, redpulseloop_on, RelayStatus;
    int pulseCount = 0;
    int stopCount = 0;
    int sameRespCount = 0, LinkResponseCount = 0;
    int fdCheckCount = 0;
    long stopAutoFuelSeconds = 0;
    Integer Pulses = 0;
    Integer pre_pulse = 0;
    double fillqty = 0, numPulseRatio = 0, minFuelLimit = 0;
    long sqliteID = 0;
    String CurrentLinkMac = "", LinkCommunicationType = "", SERVER_IP = "", LinkName = "", printReceipt = "", IsFuelingStop = "0", IsLastTransaction = "0", OverrideQuantity = "0", OverridePulse = "0";
    Timer timerBt4;
    List<Timer> TimerList_ReadpulseBT4 = new ArrayList<Timer>();
    DBController controller = new DBController(BackgroundService_BTFour.this);
    Boolean IsThisBTTrnx;
    Boolean isBroadcastReceiverRegistered = false;

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
                CloseTransaction();
            } else {
                sqlite_id = (long) extras.get("sqlite_id");
                SERVER_IP = String.valueOf(extras.get("SERVER_IP"));
                Request = "";
                Request = "";
                stopCount = 0;
                Constants.FS_4STATUS = "BUSY";
                Log.i(TAG, "-Started-");
                if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + "  BTLink 4: -Started-");

                SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
                TransactionId = sharedPref.getString("TransactionId_FS4", "");
                VehicleId = sharedPref.getString("VehicleId_FS4", "");
                PhoneNumber = sharedPref.getString("PhoneNumber_FS4", "");
                PersonId = sharedPref.getString("PersonId_FS4", "");
                PulseRatio = sharedPref.getString("PulseRatio_FS4", "1");
                MinLimit = sharedPref.getString("MinLimit_FS4", "0");
                FuelTypeId = sharedPref.getString("FuelTypeId_FS4", "");
                ServerDate = sharedPref.getString("ServerDate_FS4", "");
                IntervalToStopFuel = sharedPref.getString("IntervalToStopFuel_FS4", "0");
                IsTLDCall = sharedPref.getString("IsTLDCall_FS4", "False");
                EnablePrinter = sharedPref.getString("EnablePrinter_FS4", "False");
                PumpOnTime = sharedPref.getString("PumpOnTime_FS4", "0");

                numPulseRatio = Double.parseDouble(PulseRatio);
                minFuelLimit = Double.parseDouble(MinLimit);
                stopAutoFuelSeconds = Long.parseLong(IntervalToStopFuel);

                //UDP Connection..!!
                if (WelcomeActivity.serverSSIDList != null && WelcomeActivity.serverSSIDList.size() > 0) {
                    LinkCommunicationType = WelcomeActivity.serverSSIDList.get(WelcomeActivity.SelectedItemPos).get("LinkCommunicationType");
                    CurrentLinkMac = WelcomeActivity.serverSSIDList.get(WelcomeActivity.SelectedItemPos).get("MacAddress");
                }

                //Register Broadcast reciever
                broadcastBlueLinkFourData = new BackgroundService_BTFour.BroadcastBlueLinkFourData();
                IntentFilter intentFilter = new IntentFilter("BroadcastBlueLinkFourData");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 4: Registering Receiver.");
                registerReceiver(broadcastBlueLinkFourData, intentFilter);
                isBroadcastReceiverRegistered = true;
                AppConstants.WriteinFile(TAG + " BTLink 4: Registered successfully. (" + broadcastBlueLinkFourData + ")");

                LinkName = CommonUtils.getlinkName(3);
                if (LinkCommunicationType.equalsIgnoreCase("BT")) {
                    IsThisBTTrnx = true;
                    infoCommand();
                } else if (LinkCommunicationType.equalsIgnoreCase("UDP")) {
                    IsThisBTTrnx = false;
                    infoCommand();
                    //BeginProcessUsingUDP();
                } else {
                    //Something went Wrong in hose selection.
                    IsThisBTTrnx = false;
                    CloseTransaction();
                    Log.i(TAG, "BTLink 4: Something went Wrong in hose selection Exit");
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink 4: Something went Wrong in hose selection Exit");
                    this.stopSelf();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Service.START_NOT_STICKY;
    }

    private void infoCommand() {

        try {
            //Execute info command
            Request = "";
            Response = "";
            if (IsThisBTTrnx) {
                BTSPPMain btspp = new BTSPPMain();
                btspp.send4(BTConstants.info_cmd);
            } else {
               // new Thread(new ClientSendAndListenUDPOne(BTConstants.info_cmd, SERVER_IP, this)).start();
            }

            new CountDownTimer(4000, 1000) {

                public void onTick(long millisUntilFinished) {

                    if (Request.equalsIgnoreCase(BTConstants.info_cmd) && !Response.equalsIgnoreCase("")) {
                        //Info command success.
                        Log.i(TAG, "BTLink 4: InfoCommand Response success 1:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 4: InfoCommand Response success 1:>>" + Response);

                        if (!TransactionId.isEmpty()) {
                            transactionIdCommand(TransactionId);
                        } else {
                            Log.i(TAG, "BTLink 4: Please check TransactionId empty>>" + TransactionId);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink 4: Please check TransactionId empty>>" + TransactionId);
                        }
                        cancel();
                    } else {
                        Log.i(TAG, "BTLink 4: Waiting for infoCommand Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 4: Waiting for infoCommand Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                    }

                }

                public void onFinish() {

                    if (Request.equalsIgnoreCase(BTConstants.info_cmd) && !Response.equalsIgnoreCase("")) {
                        //Info command success.
                        Log.i(TAG, "BTLink 4: InfoCommand Response success 2:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 4: InfoCommand Response success 2:>>" + Response);
                        if (!TransactionId.isEmpty()) {
                            transactionIdCommand(TransactionId);
                        } else {
                            Log.i(TAG, "BTLink 4: Please check TransactionId empty>>" + TransactionId);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink 4: Please check TransactionId empty>>" + TransactionId);
                            CloseTransaction();
                        }
                    } else {

                        //UpgradeTransaction Status info command fail.
                        CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "6",BackgroundService_BTFour.this);
                        Log.i(TAG, "BTLink 4: Failed to get infoCommand Response:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 4: Failed to get infoCommand Response:>>" + Response);
                        CloseTransaction();
                    }
                }

            }.start();

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 4: infoCommand Exception:>>" + e.getMessage());
        }
    }

    private void transactionIdCommand(String transactionId) {

        try {
            //Execute transactionId Command
            Request = "";
            Response = "";
            if (IsThisBTTrnx) {
                BTSPPMain btspp = new BTSPPMain();
                btspp.send4(BTConstants.transaction_id_cmd + transactionId);
            } else {
                new Thread(new ClientSendAndListenUDPOne(BTConstants.transaction_id_cmd + transactionId, SERVER_IP, this)).start();
            }
            Log.i(TAG, "BTLink 4: In Request>>" + BTConstants.transaction_id_cmd + transactionId);

            new CountDownTimer(4000, 1000) {

                public void onTick(long millisUntilFinished) {

                    try {
                        if (Request.equalsIgnoreCase(BTConstants.transaction_id_cmd + transactionId) && Response.contains(transactionId)) {
                            //Info command success.
                            Log.i(TAG, "BTLink 4: transactionId Command Response success 1:>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink 4: transactionId Command Response success 1:>>" + Response);
                            relayOnCommand(); //RelayOn
                            cancel();
                        } else {
                            Log.i(TAG, "BTLink 4: Waiting for transactionId Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " BTLink 4: Waiting for transactionId Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 4: Waiting for transactionId Command Exception: " + e.getMessage());
                    }
                }

                public void onFinish() {

                    if (Request.equalsIgnoreCase(BTConstants.transaction_id_cmd + transactionId) && Response.contains(transactionId)) {
                        //Info command success.
                        Log.i(TAG, "BTLink 4: transactionId Command Response success 2:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 4: transactionId Command Response success 2:>>" + Response);
                        relayOnCommand(); //RelayOn
                    } else {

                        //UpgradeTransaction Status TransactionId command fail.
                        CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "6",BackgroundService_BTFour.this);
                        Log.i(TAG, "BTLink 4: Failed to get transactionId Command Response:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 4: Failed to get transactionId Command Response:>>" + Response);
                        CloseTransaction();
                    }
                }

            }.start();

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 4: transactionIdCommand Exception:>>" + e.getMessage());
        }
    }

    private void relayOnCommand() {
        try {
            //Execute relayOn Command
            Request = "";
            Response = "";
            if (IsThisBTTrnx) {
                BTSPPMain btspp = new BTSPPMain();
                btspp.send4(BTConstants.relay_on_cmd);
            } else {
                new Thread(new ClientSendAndListenUDPOne(BTConstants.relay_on_cmd, SERVER_IP, this)).start();
            }

            InsertInitialTransactionToSqlite();//Insert empty transaction into sqlite

            new CountDownTimer(4000, 1000) {

                public void onTick(long millisUntilFinished) {

                    if (RelayStatus == true) {
                        //Info command success.
                        Log.i(TAG, "BTLink 4: relayOn Command Response success 1:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 4: relayOn Command Response success 1:>>" + Response);
                        cancel();
                    } else {
                        Log.i(TAG, "BTLink 4: Waiting for relayOn Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 4: Waiting for relayOn Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                    }

                }

                public void onFinish() {

                    if (RelayStatus == true) {
                        //RelayOff command success.
                        Log.i(TAG, "BTLink 4: relayOn Command Response success 2:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 4: relayOn Command Response success 2:>>" + Response);
                    } else {
                        //UpgradeTransaction Status RelayOn command fail.
                        CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "6",BackgroundService_BTFour.this);
                        Log.i(TAG, "BTLink 4: Failed to get relayOn Command Response:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 4: Failed to get relayOn Command Response:>>" + Response);
                        relayOffCommand(); //RelayOff
                    }
                }

            }.start();

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 4: relayOnCommand Exception:>>" + e.getMessage());
        }
    }

    private void CloseFDcheck() {

        try {
            unregisterReceiver(broadcastBlueLinkFourData);
            stopTxtprocess = true;
            Constants.FS_4STATUS = "FREE";
            Constants.FS_4Pulse = "00";
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
                BTSPPMain btspp = new BTSPPMain();
                btspp.send4(BTConstants.relay_off_cmd);
            } else {
                new Thread(new ClientSendAndListenUDPOne(BTConstants.relay_off_cmd, SERVER_IP, this)).start();
            }

            new CountDownTimer(4000, 1000) {

                public void onTick(long millisUntilFinished) {

                    if (RelayStatus == false) {
                        //relayOff command success.
                        Log.i(TAG, "BTLink 4: relayOff Command Response success 1:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 4: relayOff Command Response success 1:>>" + Response);
                        cancel();
                    } else {
                        Log.i(TAG, "BTLink 4: Waiting for relayOff Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 4: Waiting for relayOff Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                    }

                }

                public void onFinish() {

                    if (RelayStatus == false) {
                        //Info command success.
                        Log.i(TAG, "BTLink 4: relayOff Command Response success 2:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 4: relayOff Command Response success 2:>>" + Response);
                    } else {
                        CloseTransaction();
                        Log.i(TAG, "BTLink 4: Failed to get relayOff Command Response:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 4: Failed to get relayOff Command Response:>>" + Response);
                    }
                }

            }.start();

        } catch (Exception e) {
            e.printStackTrace();
            //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "BTLink 4: relayOffCommand Exception:>>" + e.getMessage());
        }
    }

    private void CloseTransaction() {

        try {
            clearEditTextFields();
            try {
                if (isBroadcastReceiverRegistered) {
                    unregisterReceiver(broadcastBlueLinkFourData);
                    isBroadcastReceiverRegistered = false;
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink 4: Receiver unregistered successfully. (" + broadcastBlueLinkFourData + ")");
                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink 4: Receiver is not registered. (" + broadcastBlueLinkFourData + ")");
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 4: Exception occurred while unregistering receiver:>>" + e.getMessage() + " (" + broadcastBlueLinkFourData + ")");
            }
            stopTxtprocess = true;
            Constants.FS_4STATUS = "FREE";
            Constants.FS_4Pulse = "00";
            CancelTimer();
            this.stopSelf();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 4: CloseTransaction Exception:>>" + e.getMessage());
        }
    }

    public void clearEditTextFields() {

        Constants.AccVehicleNumber_FS4 = "";
        Constants.AccOdoMeter_FS4 = 0;
        Constants.AccDepartmentNumber_FS4 = "";
        Constants.AccPersonnelPIN_FS4 = "";
        Constants.AccOther_FS4 = "";
        Constants.AccVehicleOther_FS4 = "";
        Constants.AccHours_FS4 = 0;

    }

    private void renameOnCommand() {
        try {
            //Execute rename Command
            Request = "";
            Response = "";

            if (IsThisBTTrnx) {
                BTSPPMain btspp = new BTSPPMain();
                btspp.send4(BTConstants.namecommand+BTConstants.BT4REPLACEBLE_WIFI_NAME);
            } else{
                new Thread(new ClientSendAndListenUDPOne(BTConstants.namecommand+BTConstants.BT4REPLACEBLE_WIFI_NAME, SERVER_IP, this)).start();
            }

            Log.i(TAG, "BTLink 4: rename Command>>");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 4: rename Command>>");
            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTFour.this).PersonEmail;
            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(this) + ":" + userEmail + ":" + "SetHoseNameReplacedFlag");

            RenameHose rhose = new RenameHose();
            rhose.SiteId = BTConstants.BT4SITE_ID;
            rhose.HoseId = BTConstants.BT4HOSE_ID;
            rhose.IsHoseNameReplaced = "Y";

            Gson gson = new Gson();
            String jsonData = gson.toJson(rhose);

            storeIsRenameFlag(this,BTConstants.BT4NeedRename, jsonData, authString);


        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 4: renameCommand Exception:>>" + e.getMessage());
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
            for (int i = 0; i < TimerList_ReadpulseBT4.size(); i++) {
                TimerList_ReadpulseBT4.get(i).cancel();
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
        timerBt4 = new Timer();
        TimerList_ReadpulseBT4.add(timerBt4);
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                //Repaeting code..
                //CancelTimer(); cancel all once done.

                Log.i(TAG, "BTLink 4: Timer count..");
                FdCheckFunction();//Fdcheck

                if (Response.contains("pulse:") && RelayStatus == true) {
                    pulseCount = 0;
                    pulseCount();

                } else if (RelayStatus == false) {
                    if (pulseCount > 4) {
                        //Stop transaction
                        pulseCount();
                        Log.i(TAG, "BTLink 4: Execute FD Check..>>");
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 4: Execute FD Check..>>");
                        cancel();
                        TransationCompleteFunction();
                        CloseTransaction();

                    } else {
                        pulseCount++;
                        pulseCount();
                        Log.i(TAG, "BTLink 4: Check pulse>>");
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " BTLink 4: Check pulse>>");
                    }
                }
            }
        };
        timerBt4.schedule(tt, 1000, 1000);
    }

    private void pulseCount() {

        try {
            pumpTimingsOnOffFunction();//PumpOn/PumpOff functionality

            String[] items = Response.trim().split(":");
            String outputQuantity = items[1].replaceAll("\"", "").trim();
            Pulses = Integer.parseInt(outputQuantity);
            fillqty = Double.parseDouble(outputQuantity);
            fillqty = fillqty / numPulseRatio;//convert to gallons
            fillqty = AppConstants.roundNumber(fillqty, 2);
            DecimalFormat precision = new DecimalFormat("0.00");
            Constants.FS_4Gallons = (precision.format(fillqty));
            Constants.FS_4Pulse = outputQuantity;
            UpdatetransactionToSqlite(outputQuantity);

            reachMaxLimit();

        } catch (Exception e) {
            e.printStackTrace();
            //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "BTLink 4: pulse count Exception>>" + e.toString());
        }
    }

    public class BroadcastBlueLinkFourData extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                Bundle notificationData = intent.getExtras();
                String Action = notificationData.getString("Action");
                if (Action.equalsIgnoreCase("BlueLinkFour")) {
                    boolean ts = RelayStatus;
                    Request = notificationData.getString("Request");
                    Response = notificationData.getString("Response");


                    if (Request.equalsIgnoreCase(BTConstants.fdcheckcommand)) {
                        FDRequest = Request;
                        FDResponse = Response;
                    }

                    //Used only for debug
                    Log.i(TAG, "BTLink 4: Link Request>>" + Request);
                    Log.i(TAG, "BTLink 4: Link Response>>" + Response);
                    //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "BTLink 4: Link Response>>" + Response);

                    //Set Relay status.
                    if (Response.contains("OFF")) {
                        RelayStatus = false;
                    } else if (Response.contains("ON")) {
                        RelayStatus = true;
                        if (!redpulseloop_on)
                            ReadPulse();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BTLink 4:onReceive Exception:" + e.toString());
            }
        }
    }

    //Sqlite code
    private void InsertInitialTransactionToSqlite() {

        String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTFour.this).PersonEmail;
        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_BTFour.this) + ":" + userEmail + ":" + "TransactionComplete");

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
        authEntityClass.AppInfo = " Version:" + CommonUtils.getVersionCode(BackgroundService_BTFour.this) + " " + AppConstants.getDeviceName() + " Android " + Build.VERSION.RELEASE + " " + "--Main Transaction--";
        authEntityClass.TransactionFrom = "A";
        authEntityClass.Pulses = Integer.parseInt(outputQuantity);
        authEntityClass.IsFuelingStop = IsFuelingStop;
        authEntityClass.IsLastTransaction = IsLastTransaction;
        authEntityClass.OverrideQuantity = OverrideQuantity;
        authEntityClass.OverridePulse = OverridePulse;

        Gson gson = new Gson();
        String jsonData = gson.toJson(authEntityClass);

        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "  BTLink 4:" + LinkName + " Pulses:" + Integer.parseInt(outputQuantity) + " Qty:" + fillqty + " TxnID:" + TransactionId);

        String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BackgroundService_BTFour.this).PersonEmail;
        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_BTFour.this) + ":" + userEmail + ":" + "TransactionComplete");


        HashMap<String, String> imap = new HashMap<>();
        imap.put("jsonData", jsonData);
        imap.put("authString", authString);
        imap.put("sqliteId", sqliteID + "");

        if (fillqty > 0) {

            //in progress (transaction recently started, no new information): Transaction ongoing = 8  --non zero qty
            CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "8", BackgroundService_BTFour.this);
            int rowseffected = controller.updateTransactions(imap);
            System.out.println("rowseffected-" + rowseffected);
            if (rowseffected == 0) {
                controller.insertTransactions(imap);
            }
        }
    }

    private void TransationCompleteFunction() {

        if (BTConstants.BT4NeedRename){
            renameOnCommand();
        }

        boolean BSRunning = CommonUtils.checkServiceRunning(BackgroundService_BTFour.this, AppConstants.PACKAGE_BACKGROUND_SERVICE);
        if (!BSRunning) {
            startService(new Intent(this, BackgroundService.class));
        }
    }

    private void reachMaxLimit() {

        //if quantity reach max limit
        if (minFuelLimit > 0 && fillqty >= minFuelLimit) {
            Log.i(TAG, "BTLink 4: Auto Stop Hit>> You reached MAX fuel limit.");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 4: Auto Stop Hit>> You reached MAX fuel limit.");
            relayOffCommand(); //RelayOff
            TransationCompleteFunction();
            CloseTransaction();
        }

    }

    private void pumpTimingsOnOffFunction() {

        try {
            int pumpOnpoint = Integer.parseInt(PumpOnTime);

            if (Pulses <= 0) {//PumpOn Time logic
                stopCount++;
                if (stopCount >= pumpOnpoint) {

                    //Timed out (Start was pressed, and pump on timer hit): Pump Time On limit reached* = 4
                    CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "4", BackgroundService_BTFour.this);
                    Log.i(TAG, "BTLink 4: PumpOnTime Hit>>" + stopCount);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink 4: PumpOnTime Hit>>" + stopCount);
                    relayOffCommand(); //RelayOff
                    TransationCompleteFunction();
                    CloseTransaction();
                }
            } else {//PumpOff Time logic

                if (!Pulses.equals(pre_pulse)) {
                    stopCount = 0;
                    pre_pulse = Pulses;
                } else {
                    stopCount++;
                }

                if (stopCount >= stopAutoFuelSeconds) {
                    Log.i(TAG, "BTLink 4: PumpOffTime Hit>>" + stopCount);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink 4: PumpOffTime Hit>>" + stopCount);
                    relayOffCommand(); //RelayOff
                    TransationCompleteFunction();
                    CloseTransaction();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void FdCheckFunction() {

        try {
            if (Response.contains("pulse:")) {
                try {
                    LinkResponseCount = 0;
                    if (sameRespCount < 4) {
                        sameRespCount++;
                    } else {
                        sameRespCount = 0;
                    }

                    if (sameRespCount == 4) {
                        sameRespCount = 0;
                        //Execute fdcheck counter
                        Log.i(TAG, "BTLink 4: Execute FD Check..>>");

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

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                stopCount++;
                int pumpOnpoint = Integer.parseInt(PumpOnTime);
                if (stopCount >= pumpOnpoint) {
                    Log.i(TAG, "BTLink 4: No response from link>>" + stopCount);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " BTLink 4: No response from link>>" + stopCount);
                    stopCount = 0;
                    relayOffCommand(); //RelayOff
                    TransationCompleteFunction();
                    CloseTransaction(); //temp
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fdCheckCommand() {

        //Execute relayOff Command
        Request = "";
        Response = "";
        if (IsThisBTTrnx) {
            BTSPPMain btspp = new BTSPPMain();
            btspp.send4(BTConstants.fdcheckcommand);
        } else {
            //new Thread(new ClientSendAndListenUDPOne(BTConstants.fdcheckcommand, SERVER_IP, this)).start();
        }
    }

}
