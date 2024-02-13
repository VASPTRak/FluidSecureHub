package com.TrakEngineering.FluidSecureHub;

import static com.TrakEngineering.FluidSecureHub.server.ServerHandler.TEXT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.BTSPP.BTConstants;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTSPPMain;
import com.TrakEngineering.FluidSecureHub.BTSPP.CommonFunctions;
import com.TrakEngineering.FluidSecureHub.entity.StatusForUpgradeVersionEntity;
import com.TrakEngineering.FluidSecureHub.entity.UserInfoEntity;
import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class BackgroundServiceKeepAliveBT extends BackgroundService {
    private static final String TAG = AppConstants.LOG_MAINTAIN + "-" + "BS_KeepAliveBT ";
    public static ArrayList<HashMap<String, String>> SSIDList = new ArrayList<>();
    public static ArrayList<HashMap<String, String>> DefectiveBTLinks = new ArrayList<>();

    Date date1, date2;
    //public boolean isRebootDeviceCalled = false; // reboot

    // ============ Bluetooth receiver for Keep Alive =========//
    public BroadcastBlueLinkData broadcastBlueLinkData = null;
    public boolean isBroadcastReceiverRegistered = false;
    public IntentFilter intentFilter;
    public int btLinkPosition = 0; //, linkPosition = 0;
    public String request = "", response = "";
    public int connectionAttemptCount = 0;
    //public boolean IsInfoCommandSuccess = false;
    public boolean IsBTLinkConnected = false;
    //public int waitCounter = 0;
    //======================================================//
    public int counter = 0;
    //public int i = 0;

    @SuppressLint("LongLogTag")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            super.onStart(intent, startId);

            if (WelcomeActivity.OnWelcomeActivity && !AppConstants.isBTLinkUpgradeInProgress) {
                boolean startProcess = false;
                if (SSIDList != null && SSIDList.size() > 0) {
                    for (int i = 0; i < SSIDList.size(); i++) {
                        String LinkCommunicationType = SSIDList.get(i).get("LinkCommunicationType");
                        if (LinkCommunicationType != null && LinkCommunicationType.equalsIgnoreCase("BT")) {
                            startProcess = true;
                            break;
                        }
                    }
                }

                if (startProcess) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "<Started>");
                    DefectiveBTLinks.clear();
                    StartProcess();
                }

            } else {
                Log.i(TAG, "Skip keepAlive not on Welcome activity or upgrade process is running.");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "<Skipped>");
            }

        } catch (NullPointerException e) {
            Log.d("Ex", e.getMessage());
            this.stopSelf();
        }

        // return super.onStartCommand(intent, flags, startId);
        return Service.START_NOT_STICKY;
    }

    @SuppressLint("LongLogTag")
    public void StartProcess() {
        try {
            if (SSIDList != null && SSIDList.size() > 0) {
                if (counter < SSIDList.size()) {
                    //int i = 0;
                    //while (i < SSIDList.size()) {
                    //for (int i = 0; i < SSIDList.size(); i++) {
                    int position = counter;
                    //boolean IsAllHosesAreFree = AppConstants.IsAllHosesAreFree();
                    String linkBusyStatus = GetBTLinkBusyStatus(position);
                    String selSSID = SSIDList.get(position).get("WifiSSId");
                    String selBTMacAddress = SSIDList.get(position).get("BTMacAddress");
                    String LinkCommunicationType = "BT";
                    try {
                        LinkCommunicationType = SSIDList.get(position).get("LinkCommunicationType");
                    } catch (Exception e) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Exception while getting LinkCommunicationType of WifiSSId (" + selSSID + "). " + e.getMessage());
                    }

                /*if (LinkCommunicationType != null) {
                    if (!LinkCommunicationType.equalsIgnoreCase("BT")) {
                        continue;
                    }
                } else {
                    continue;
                }*/
                    if (LinkCommunicationType != null && LinkCommunicationType.equalsIgnoreCase("BT")) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "==============================================================================");
                        IsBTLinkConnected = false;
                        //IsInfoCommandSuccess = false;

                        if (WelcomeActivity.OnWelcomeActivity && linkBusyStatus.equalsIgnoreCase("FREE") && !AppConstants.isBTLinkUpgradeInProgress) {
                            switch (position) {
                                case 0://Link One
                                    if (selBTMacAddress != null && !selBTMacAddress.isEmpty() && BTConstants.BTLinkOneStatus && BTConstants.BTStatusStrOne.equalsIgnoreCase("Connected")) { // && CommonFunctions.CheckIfPresentInPairedDeviceList(selBTMacAddress)) {
                                        IsBTLinkConnected = true;
                                        BTConstants.BTConnFailedCountLink1 = 0;
                                        SaveDefectiveBTLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this, "BTConnDT0");
                                    }
                                    break;
                                case 1://Link Two
                                    if (selBTMacAddress != null && !selBTMacAddress.isEmpty() && BTConstants.BTLinkTwoStatus && BTConstants.BTStatusStrTwo.equalsIgnoreCase("Connected")) { // && CommonFunctions.CheckIfPresentInPairedDeviceList(selBTMacAddress)) {
                                        IsBTLinkConnected = true;
                                        BTConstants.BTConnFailedCountLink2 = 0;
                                        SaveDefectiveBTLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this, "BTConnDT1");
                                    }
                                    break;
                                case 2://Link Three
                                    if (selBTMacAddress != null && !selBTMacAddress.isEmpty() && BTConstants.BTLinkThreeStatus && BTConstants.BTStatusStrThree.equalsIgnoreCase("Connected")) { // && CommonFunctions.CheckIfPresentInPairedDeviceList(selBTMacAddress)) {
                                        IsBTLinkConnected = true;
                                        BTConstants.BTConnFailedCountLink3 = 0;
                                        SaveDefectiveBTLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this, "BTConnDT2");
                                    }
                                    break;
                                case 3://Link Four
                                    if (selBTMacAddress != null && !selBTMacAddress.isEmpty() && BTConstants.BTLinkFourStatus && BTConstants.BTStatusStrFour.equalsIgnoreCase("Connected")) { // && CommonFunctions.CheckIfPresentInPairedDeviceList(selBTMacAddress)) {
                                        IsBTLinkConnected = true;
                                        BTConstants.BTConnFailedCountLink4 = 0;
                                        SaveDefectiveBTLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this, "BTConnDT3");
                                    }
                                    break;
                                case 4://Link Five
                                    if (selBTMacAddress != null && !selBTMacAddress.isEmpty() && BTConstants.BTLinkFiveStatus && BTConstants.BTStatusStrFive.equalsIgnoreCase("Connected")) { // && CommonFunctions.CheckIfPresentInPairedDeviceList(selBTMacAddress)) {
                                        IsBTLinkConnected = true;
                                        BTConstants.BTConnFailedCountLink5 = 0;
                                        SaveDefectiveBTLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this, "BTConnDT4");
                                    }
                                    break;
                                case 5://Link Six
                                    if (selBTMacAddress != null && !selBTMacAddress.isEmpty() && BTConstants.BTLinkSixStatus && BTConstants.BTStatusStrSix.equalsIgnoreCase("Connected")) { // && CommonFunctions.CheckIfPresentInPairedDeviceList(selBTMacAddress)) {
                                        IsBTLinkConnected = true;
                                        BTConstants.BTConnFailedCountLink6 = 0;
                                        SaveDefectiveBTLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this, "BTConnDT5");
                                    }
                                    break;
                                default://Something went wrong in link selection please try again.
                                    break;
                            }

                            try {
                                // Info command sending code.
                                if (IsBTLinkConnected) {
                                    LinkRebootFunctionality(position, selSSID);
                                } else {
                                    boolean continueToRetry = true;
                                    if (AppConstants.IsAllHosesAreFree() && !AppConstants.isBTLinkUpgradeInProgress &&
                                            (BTConstants.BTConnFailedCountLink1 == 3 || BTConstants.BTConnFailedCountLink2 == 3 ||
                                                    BTConstants.BTConnFailedCountLink3 == 3 || BTConstants.BTConnFailedCountLink4 == 3 ||
                                                    BTConstants.BTConnFailedCountLink5 == 3 || BTConstants.BTConnFailedCountLink6 == 3)) {
                                        // Reset count for all positions
                                        BTConstants.BTConnFailedCountLink1 = 0;
                                        BTConstants.BTConnFailedCountLink2 = 0;
                                        BTConstants.BTConnFailedCountLink3 = 0;
                                        BTConstants.BTConnFailedCountLink4 = 0;
                                        BTConstants.BTConnFailedCountLink5 = 0;
                                        BTConstants.BTConnFailedCountLink6 = 0;
                                        continueToRetry = false;
                                        try {
                                            //Disable BT------------
                                            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                            mBluetoothAdapter.disable();
                                            Log.i(TAG, "BTKeepAlive: BT OFF");
                                            if (AppConstants.GenerateLogs)
                                                AppConstants.WriteinFile(TAG + "<BT OFF>");

                                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    //Enable BT------------
                                                    mBluetoothAdapter.enable();
                                                    Log.i(TAG, "BTKeepAlive: BT ON");
                                                    if (AppConstants.GenerateLogs)
                                                        AppConstants.WriteinFile(TAG + "<BT ON>");
                                                    try {
                                                        Thread.sleep(1000);
                                                    } catch (Exception e) { e.printStackTrace(); }
                                                    Log.d(TAG, "BTKeepAlive: Checking CheckIfPresentInPairedDeviceList after BT ON");
                                                    if (CommonFunctions.CheckIfPresentInPairedDeviceList(selBTMacAddress)) {
                                                        BTConstants.RetryBTConnectionLinkPosition = position;
                                                    }
                                                }
                                            }, 2000);
                                        } catch (Exception e) {
                                            continueToRetry = true;
                                            if (AppConstants.GenerateLogs)
                                                AppConstants.WriteinFile(TAG + "Exception while toggling Bluetooth.");
                                        }
                                    }
                                    if (continueToRetry) {
                                        Log.d(TAG, "BTKeepAlive: Checking CheckIfPresentInPairedDeviceList inside continueToRetry");
                                        if (CommonFunctions.CheckIfPresentInPairedDeviceList(selBTMacAddress)) {
                                            BTConstants.RetryBTConnectionLinkPosition = position;
                                            Thread.sleep(1000);
                                        }
                                    }
                                    CheckInabilityToConnectLinks(position, selSSID, IsBTLinkConnected, false);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "<Skipped Keep Alive for the Link: " + selSSID + ">");
                            ContinueForNextLink(false);
                        }
                    } else {
                        ContinueForNextLink(false);
                    }
                } else {
                    counter = 0;
                }
                //i++;
                //}
            } else {
                Log.i(TAG, "SSID List Empty");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "SSID List Empty");
            }

            if (AppConstants.IsAllHosesAreFree() && !AppConstants.isBTLinkUpgradeInProgress) {
                //boolean rebootFlag = false; // reboot
                if (DefectiveBTLinks != null && DefectiveBTLinks.size() > 0) {

                    for (int p = 0; p < DefectiveBTLinks.size(); p++) {

                        String linkName = DefectiveBTLinks.get(p).get("Selected_SSID");
                        int diffMin = Integer.parseInt(DefectiveBTLinks.get(p).get("diff_min"));
                        String Message = DefectiveBTLinks.get(p).get("Message");

                        if (diffMin > 60) { // 1 hr
                            //rebootFlag = true; // reboot
                            boolean sendMail = checkSharedPrefDefectiveLink(BackgroundServiceKeepAliveBT.this, linkName);
                            if (sendMail) {
                                setSharedPrefDefectiveLink(BackgroundServiceKeepAliveBT.this, linkName);
                                Log.i(TAG, "Defective links email sent to: " + linkName + " Message: " + Message + " TDifferance: " + diffMin);
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "LINK (" + linkName + ") did not respond to KeepAlive");
                                SendDefectiveLinkInfoEmailAsyncCall(linkName);
                            }
                        }
                    }
                }

                /*if (rebootFlag) {
                    // Tablet Reboot functionality
                    if (AppConstants.IsAllHosesAreFree()) {
                        boolean sendReboot = checkSharedPrefForReboot(BackgroundServiceKeepAliveBT.this);
                        if (sendReboot && !isRebootDeviceCalled) {
                            isRebootDeviceCalled = true;
                            setSharedPrefForReboot(BackgroundServiceKeepAliveBT.this);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "<Calling SureMDM Reboot>");
                            new CallSureMDMRebootDevice(BackgroundServiceKeepAliveBT.this).execute();
                        }
                    }
                }*/ // reboot

            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Main Exception: " + e.getMessage());
        }
    }

    private void LinkRebootFunctionality(int linkPosition, String selectedSSID) {
        try {
            Log.d(TAG, "BTKeepAlive: calling RegisterBTReceiver from LinkRebootFunctionality");
            RegisterBTReceiver(linkPosition);
            //Execute reboot command
            request = "";
            response = "";
            //waitCounter = 0;

            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + getBTLinkIndexByPosition(linkPosition) + " Sending reboot command to Link: " + selectedSSID);
            SendBTCommands(linkPosition, BTConstants.reboot_cmd);

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    BTKeepAliveCompleteFunction(); // To unregister the receiver after reboot
                    BTConstants.RetryBTConnectionLinkPosition = linkPosition;
                    new CountDownTimer(10000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            //waitCounter++;
                            if (getBTStatusStr(linkPosition).equalsIgnoreCase("Connected")) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "<Connected after reboot. Link: " + selectedSSID + "; Position: " + (linkPosition + 1) + ">");
                                InfoCommand(linkPosition, selectedSSID);
                                cancel();
                            }
                            /*else {
                                if (waitCounter > 1) {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "Waiting for Reconnect after rebooting the Link: " + selectedSSID);
                                }
                            }*/
                        }

                        public void onFinish() {
                            //waitCounter = 0;
                            if (getBTStatusStr(linkPosition).equalsIgnoreCase("Connected")) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "<Connected after reboot. Link: " + selectedSSID + "; Position: " + (linkPosition + 1) + ">");
                                InfoCommand(linkPosition, selectedSSID);
                            } else {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "<Failed to reconnect after rebooting the Link: " + selectedSSID + "; Position: " + (linkPosition + 1) + ">");
                                ContinueForNextLink(true);
                            }
                        }
                    }.start();
                }
            }, 8000); // Tried to reconnect and continue after 8 seconds because the link disconnects after 6-8 seconds.

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "LinkRebootFunctionality Exception: " + e.getMessage() + "; Link: " + selectedSSID + "; Position: " + (linkPosition + 1));
            ContinueForNextLink(true);
        }
    }

    private void InfoCommand(int linkPosition, String selectedSSID) {
        try {
            Log.d(TAG, "BTKeepAlive: calling RegisterBTReceiver from InfoCommand");
            RegisterBTReceiver(linkPosition);
            try {
                //Execute info command
                request = "";
                response = "";

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + getBTLinkIndexByPosition(linkPosition) + " Sending Info command to Link: " + selectedSSID);
                SendBTCommands(linkPosition, BTConstants.info_cmd);

                new CountDownTimer(4000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        if (request.equalsIgnoreCase(BTConstants.info_cmd) && response.contains("version")) {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + getBTLinkIndexByPosition(linkPosition) + " Info command Success");
                            SaveDefectiveBTLinkInfoCmdDateTimeSharedPref(linkPosition);
                            CheckInabilityToConnectLinks(linkPosition, selectedSSID, IsBTLinkConnected, true);
                            BTKeepAliveCompleteFunction();
                            cancel();
                        }
                    }

                    public void onFinish() {
                        if (request.equalsIgnoreCase(BTConstants.info_cmd) && response.contains("version")) {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + getBTLinkIndexByPosition(linkPosition) + " Info command Success");
                            SaveDefectiveBTLinkInfoCmdDateTimeSharedPref(linkPosition);
                            CheckInabilityToConnectLinks(linkPosition, selectedSSID, IsBTLinkConnected, true);
                        } else {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + getBTLinkIndexByPosition(linkPosition) + " Info command Failed");
                            CheckInabilityToConnectLinks(linkPosition, selectedSSID, IsBTLinkConnected, false);
                        }
                        BTKeepAliveCompleteFunction();
                    }
                }.start();

            } catch (Exception e) {
                CheckInabilityToConnectLinks(linkPosition, selectedSSID, IsBTLinkConnected, false);
                BTKeepAliveCompleteFunction();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "InfoCommand Exception: " + e.getMessage() + "; Link: " + selectedSSID + "; Position: " + (linkPosition + 1));
            BTKeepAliveCompleteFunction();
        }
    }

    private void SaveDefectiveBTLinkInfoCmdDateTimeSharedPref(int linkPosition) {
        try {
            switch (linkPosition) {
                case 0://Link One
                    SaveDefectiveBTLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this, "BTInfoCmdConnDT0");
                    break;
                case 1://Link Two
                    SaveDefectiveBTLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this, "BTInfoCmdConnDT1");
                    break;
                case 2://Link Three
                    SaveDefectiveBTLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this, "BTInfoCmdConnDT2");
                    break;
                case 3://Link Four
                    SaveDefectiveBTLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this, "BTInfoCmdConnDT3");
                    break;
                case 4://Link Five
                    SaveDefectiveBTLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this, "BTInfoCmdConnDT4");
                    break;
                case 5://Link Six
                    SaveDefectiveBTLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this, "BTInfoCmdConnDT5");
                    break;
                default://Something went wrong in link selection please try again.
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void SaveDefectiveBTLinkDateTimeSharedPref(Context ctx, String linkPosition) {
        SharedPreferences sharedPref = ctx.getSharedPreferences("DefectiveBTLinkDateTime", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(linkPosition, CommonUtils.getTodaysDateTemp());
        editor.apply();
    }

    private String GetDefectiveLinkDateTimeSharedPref(Context ctx, String linkPosition) {
        SharedPreferences sharedPrefODO = ctx.getSharedPreferences("DefectiveBTLinkDateTime", Context.MODE_PRIVATE);

        return sharedPrefODO.getString(linkPosition, "");
    }

    private void setSharedPrefDefectiveLink(Context myctx, String ssid) {
        String key = "last_date" + ssid;
        SharedPreferences sharedPref = myctx.getSharedPreferences("DefectiveBTLink", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, AppConstants.currentDateFormat("dd/MM/yyyy"));
        editor.apply();
    }

    private boolean checkSharedPrefDefectiveLink(Context myctx, String ssid) {
        String key = "last_date" + ssid;
        SharedPreferences sharedPrefODO = myctx.getSharedPreferences("DefectiveBTLink", Context.MODE_PRIVATE);
        String last_date = sharedPrefODO.getString(key, "");
        String curr_date = AppConstants.currentDateFormat("dd/MM/yyyy");

        if (curr_date.trim().equalsIgnoreCase(last_date.trim())) {
            return false;
        } else {
            return true;
        }
    }

    /*private void setSharedPrefForReboot(Context myctx) {
        SharedPreferences sharedPref = myctx.getSharedPreferences("DefectiveBTLinkDateTime", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("LastRebootDateTime", AppConstants.currentDateFormat("yyyy-MM-dd HH:mm:ss"));
        editor.apply();
    }*/ // reboot

    /*private boolean checkSharedPrefForReboot(Context myctx) {
        SharedPreferences sharedPrefODO = myctx.getSharedPreferences("DefectiveBTLinkDateTime", Context.MODE_PRIVATE);
        String last_date = sharedPrefODO.getString("LastRebootDateTime", "");
        String curr_date = AppConstants.currentDateFormat("yyyy-MM-dd HH:mm:ss");
        int diff_min = 0;
        if (last_date.isEmpty()) {
            return true;
        } else {
            diff_min = getDiffInMinutes(curr_date, last_date);
        }

        if (diff_min >= 60) {
            return true;
        } else {
            return false;
        }
    }*/ // reboot

    private String getBTStatusStr(int linkPosition) {
        String BTStatusStr = "";
        try {
            switch (linkPosition) {
                case 0://Link 1
                    BTStatusStr = BTConstants.BTStatusStrOne;
                    break;
                case 1://Link 2
                    BTStatusStr = BTConstants.BTStatusStrTwo;
                    break;
                case 2://Link 3
                    BTStatusStr = BTConstants.BTStatusStrThree;
                    break;
                case 3://Link 4
                    BTStatusStr = BTConstants.BTStatusStrFour;
                    break;
                case 4://Link 5
                    BTStatusStr = BTConstants.BTStatusStrFive;
                    break;
                case 5://Link 6
                    BTStatusStr = BTConstants.BTStatusStrSix;
                    break;
            }
        } catch (Exception e) {
            BTStatusStr = "";
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + getBTLinkIndexByPosition(linkPosition) + " getBTStatusStr Exception:>>" + e.getMessage());
        }
        return BTStatusStr;
    }
    private String getBTLinkIndexByPosition(int linkPosition) {
        String BTLinkIndex = "";
        switch (linkPosition) {
            case 0://Link 1
                BTLinkIndex = "BTLink_1:";
                break;
            case 1://Link 2
                BTLinkIndex = "BTLink_2:";
                break;
            case 2://Link 3
                BTLinkIndex = "BTLink_3:";
                break;
            case 3://Link 4
                BTLinkIndex = "BTLink_4:";
                break;
            case 4://Link 5
                BTLinkIndex = "BTLink_5:";
                break;
            case 5://Link 6
                BTLinkIndex = "BTLink_6:";
                break;
        }
        return BTLinkIndex;
    }

    private void BTKeepAliveCompleteFunction() {
        try {
            Log.d(TAG, "BTKeepAlive: UnRegistering BTReceiver: " + isBroadcastReceiverRegistered);
            //if (isBroadcastReceiverRegistered) {
            isBroadcastReceiverRegistered = false;
            UnregisterReceiver();
            //}
        } catch (Exception e) {
            Log.e(TAG, "BTKeepAlive: UnRegistering BTReceiver Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void RegisterBTReceiver(int linkPosition) {
        BTKeepAliveCompleteFunction();
        btLinkPosition = linkPosition;
        broadcastBlueLinkData = new BroadcastBlueLinkData();
        switch (linkPosition) {
            case 0://Link 1
                intentFilter = new IntentFilter("BroadcastBlueLinkOneData");
                break;
            case 1://Link 2
                intentFilter = new IntentFilter("BroadcastBlueLinkTwoData");
                break;
            case 2://Link 3
                intentFilter = new IntentFilter("BroadcastBlueLinkThreeData");
                break;
            case 3://Link 4
                intentFilter = new IntentFilter("BroadcastBlueLinkFourData");
                break;
            case 4://Link 5
                intentFilter = new IntentFilter("BroadcastBlueLinkFiveData");
                break;
            case 5://Link 6
                intentFilter = new IntentFilter("BroadcastBlueLinkSixData");
                break;
        }
        /*if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "<Registering the receiver for Link: " + selectedSSID + ">");*/
        registerReceiver(broadcastBlueLinkData, intentFilter);
        isBroadcastReceiverRegistered = true;
    }

    private void UnregisterReceiver() {
        /*if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "<Unregistering the receiver>");*/
        try {
            unregisterReceiver(broadcastBlueLinkData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class BroadcastBlueLinkData extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                Bundle notificationData = intent.getExtras();
                String Action = notificationData.getString("Action");
                if (Action == null) {
                    Action = "";
                }
                String actionByPosition = "";
                switch (btLinkPosition) {
                    case 0://Link 1
                        actionByPosition = "BlueLinkOne";
                        break;
                    case 1://Link 2
                        actionByPosition = "BlueLinkTwo";
                        break;
                    case 2://Link 3
                        actionByPosition = "BlueLinkThree";
                        break;
                    case 3://Link 4
                        actionByPosition = "BlueLinkFour";
                        break;
                    case 4://Link 5
                        actionByPosition = "BlueLinkFive";
                        break;
                    case 5://Link 6
                        actionByPosition = "BlueLinkSix";
                        break;
                    default://Something went wrong in link selection please try again.
                        break;
                }

                if (Action.equalsIgnoreCase(actionByPosition)) {

                    request = notificationData.getString("Request");
                    response = notificationData.getString("Response");

                    if (response == null) {
                        response = "";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + getBTLinkIndexByPosition(btLinkPosition) + " onReceive Exception: " + e.getMessage());
            }
        }
    }

    public String GetBTLinkBusyStatus(int linkPosition) {
        String status = "";
        try {
            switch (linkPosition) {
                case 0:
                    status = Constants.FS_1STATUS;
                    break;
                case 1://Link Two
                    status = Constants.FS_2STATUS;
                    break;
                case 2://Link Three
                    status = Constants.FS_3STATUS;
                    break;
                case 3://Link Four
                    status = Constants.FS_4STATUS;
                    break;
                case 4://Link Five
                    status = Constants.FS_5STATUS;
                    break;
                case 5://Link Six
                    status = Constants.FS_6STATUS;
                    break;
                default://Something went wrong in link selection please try again.
                    break;
            }
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "GetBTLinkBusyStatus Exception: " + e.getMessage());
        }
        return status;
    }

    private void SendBTCommands(int linkPosition, String btCommand) {
        try {
            BTSPPMain btspp = new BTSPPMain();

            switch (linkPosition) {
                case 0://Link 1
                    btspp.send1(btCommand);
                    break;
                case 1://Link 2
                    btspp.send2(btCommand);
                    break;
                case 2://Link 3
                    btspp.send3(btCommand);
                    break;
                case 3://Link 4
                    btspp.send4(btCommand);
                    break;
                case 4://Link 5
                    btspp.send5(btCommand);
                    break;
                case 5://Link 6
                    btspp.send6(btCommand);
                    break;
            }
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + getBTLinkIndexByPosition(linkPosition) + " SendBTCommands Exception:>>" + e.getMessage());
        }
    }

    private void CheckInabilityToConnectLinks(int position, String selectedSSID, boolean IsBTLinkConnected, boolean IsInfoCommandSuccess) {
        //if (AppConstants.GenerateLogs)
        //    AppConstants.WriteinFile(TAG + getBTLinkIndexByPosition(position) + " [selectedSSID: " + selectedSSID + "; IsBTLinkConnected: " + IsBTLinkConnected + "; IsInfoCommandSuccess: " + IsInfoCommandSuccess + "]");
        try {
            Log.i(TAG, "CheckDetails \n------------------------------\nSelected SSID:" + selectedSSID + "\nIsBTLinkConnected:" + IsBTLinkConnected + "\nIsInfoCommandSuccess:" + IsInfoCommandSuccess + "\n------------------------");

            String CurrDate = CommonUtils.getTodaysDateTemp();
            String Message = "";
            String BTInfoCmdConnDT = "";
            String BTConnDT = "";
            int diff_min = 0;

            if (position == 0) {
                BTInfoCmdConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this,"BTInfoCmdConnDT0");
                BTConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this,"BTConnDT0");
            } else if (position == 1) {
                BTInfoCmdConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this,"BTInfoCmdConnDT1");
                BTConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this,"BTConnDT1");
            } else if (position == 2) {
                BTInfoCmdConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this,"BTInfoCmdConnDT2");
                BTConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this,"BTConnDT2");
            } else if (position == 3) {
                BTInfoCmdConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this,"BTInfoCmdConnDT3");
                BTConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this,"BTConnDT3");
            } else if (position == 4) {
                BTInfoCmdConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this,"BTInfoCmdConnDT4");
                BTConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this,"BTConnDT4");
            } else if (position == 5) {
                BTInfoCmdConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this,"BTInfoCmdConnDT5");
                BTConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepAliveBT.this,"BTConnDT5");
            }

            if (IsBTLinkConnected) {
                if (IsInfoCommandSuccess) {
                    diff_min = getDiffInMinutes(CurrDate, BTInfoCmdConnDT);
                    Message = "Link working fine";
                } else {
                    diff_min = getDiffInMinutes(CurrDate, BTInfoCmdConnDT);
                    Message = "Fail at info command";
                }
            } else {
                if (BTConnDT.equals("") || BTInfoCmdConnDT.equals("")) {
                    //link never connected.
                    Message = "Link not connected yet";
                } else {
                    //link not connected to BT from diff_minutes
                    diff_min = getDiffInMinutes(CurrDate, BTConnDT);
                    Message = "BT Link not connected.";
                }
            }

            HashMap<String, String> map = new HashMap<>();
            map.put("Selected_SSID", selectedSSID);
            map.put("diff_min", String.valueOf(diff_min));
            map.put("Message", Message);

            DefectiveBTLinks.add(map);

            ContinueForNextLink(true);
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + getBTLinkIndexByPosition(position) + " CheckInabilityToConnectLinks Exception:>>" + e.getMessage());
        }
    }

    private void ContinueForNextLink(boolean isBTLink) {
        counter++;
        BTKeepAliveCompleteFunction();
        if (isBTLink) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "==============================================================================");
        }
        if (WelcomeActivity.OnWelcomeActivity && !AppConstants.isBTLinkUpgradeInProgress) {
            StartProcess(); // Next Link
        }
    }

    private int getDiffInMinutes(String CurrentTime, String SuccessTime) {

        int DiffTime = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date1 = sdf.parse(CurrentTime);
            date2 = sdf.parse(SuccessTime);

            long diff = date1.getTime() - date2.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            DiffTime = (int) minutes;

        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
        }
        return DiffTime;
    }

    public void SendDefectiveLinkInfoEmailAsyncCall(String linkName) {

        UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetails_KeepAliveBT(BackgroundServiceKeepAliveBT.this);

        StatusForUpgradeVersionEntity objEntityClass2 = new StatusForUpgradeVersionEntity();
        objEntityClass2.HubName = userInfoEntity.PersonName;
        objEntityClass2.SiteName = userInfoEntity.FluidSecureSiteName;
        objEntityClass2.LinkName = linkName;

        Gson gson = new Gson();
        String param2 = gson.toJson(objEntityClass2);

        String userEmail = CommonUtils.getCustomerDetails_KeepAliveBT(BackgroundServiceKeepAliveBT.this).PersonEmail;
        //----------------------------------------------------------------------------------
        String parm1 = AppConstants.getIMEI(BackgroundServiceKeepAliveBT.this) + ":" + userEmail + ":" + "DefectiveLinkInfoEmail" + AppConstants.LANG_PARAM;
        String authString = "Basic " + AppConstants.convertStingToBase64(parm1);

        RequestBody body = RequestBody.create(TEXT, param2);
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(AppConstants.webURL)
                .post(body)
                .addHeader("Authorization", authString)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "error in getting response");
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(Response response) throws IOException {

                ResponseBody responseBody = response.body();
                if (!response.isSuccessful()) {
                    throw new IOException("Error response " + response);
                } else {
                    String result = responseBody.string();
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "SendDefectiveLinkInfoEmailAsyncCall ~Result\n" + result);

                    try {
                        JSONObject jsonObjectSite = null;
                        jsonObjectSite = new JSONObject(result);

                        String ResponseMessageSite = jsonObjectSite.getString(AppConstants.RES_MESSAGE);

                        if (ResponseMessageSite.equalsIgnoreCase("success")) {
                            System.out.println("SendDefectiveLinkInfoEmail send successfully ");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

        });
    }

    /*public class CallSureMDMRebootDevice extends AsyncTask<Void, Void, String> {

        private Context classContext;

        private CallSureMDMRebootDevice(Context ctx) {
            classContext = ctx;
        }

        protected String doInBackground(Void... arg0) {
            String resp = "";

            try {
                SharedPreferences sharedPref = classContext.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                String PersonEmail = sharedPref.getString(AppConstants.USER_EMAIL, "");

                String param1 = AppConstants.getIMEI(classContext) + ":" + PersonEmail + ":" + "SureMDMRebootDevice" + AppConstants.LANG_PARAM;

                String authString = "Basic " + AppConstants.convertStingToBase64(param1);

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(4, TimeUnit.SECONDS);
                client.setReadTimeout(4, TimeUnit.SECONDS);
                client.setWriteTimeout(4, TimeUnit.SECONDS);

                Request request = new Request.Builder()
                        .url(AppConstants.webURL)
                        .addHeader("Authorization", authString)
                        .addHeader("ReqType", "Normal")
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                System.out.println("Ex" + e.getMessage());
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "<CallSureMDMRebootDevice -InBackground-Exception: " + e.getMessage() + ">");
                isRebootDeviceCalled = false;
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            isRebootDeviceCalled = false;
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "<CallSureMDMRebootDevice Response: " + result + ">");
        }
    }*/ // reboot
}
