package com.TrakEngineering.FluidSecureHub.BTBLE;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.TrakEngineering.FluidSecureHub.AppConstants;
import com.TrakEngineering.FluidSecureHub.BTBLE.BTBLE_LinkFour.BLEServiceCodeFour;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTConstants;
import com.TrakEngineering.FluidSecureHub.BTSPP.ClientSendAndListenUDPFour;
import com.TrakEngineering.FluidSecureHub.BackgroundService;
import com.TrakEngineering.FluidSecureHub.CommonUtils;
import com.TrakEngineering.FluidSecureHub.ConnectionDetector;
import com.TrakEngineering.FluidSecureHub.Constants;
import com.TrakEngineering.FluidSecureHub.DBController;
import com.TrakEngineering.FluidSecureHub.R;
import com.TrakEngineering.FluidSecureHub.WelcomeActivity;
import com.TrakEngineering.FluidSecureHub.entity.RenameHose;
import com.TrakEngineering.FluidSecureHub.entity.SwitchTimeBounce;
import com.TrakEngineering.FluidSecureHub.entity.TrazComp;
import com.TrakEngineering.FluidSecureHub.entity.UpdatePulserTypeOfLINK_entity;
import com.TrakEngineering.FluidSecureHub.entity.UpgradeVersionEntity;
import com.TrakEngineering.FluidSecureHub.offline.EntityOffTranz;
import com.TrakEngineering.FluidSecureHub.offline.OffDBController;
import com.TrakEngineering.FluidSecureHub.WifiHotspot.WifiApManager;
import com.TrakEngineering.FluidSecureHub.offline.OffTranzSyncService;
import com.TrakEngineering.FluidSecureHub.offline.OfflineConstants;
import com.TrakEngineering.FluidSecureHub.server.ServerHandler;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class BS_BLE_BTFour extends Service {
    private static final String TAG = AppConstants.LOG_TXTN_BT + "- BLE_Link 4:";
    private BLEServiceCodeFour mBluetoothLeService;

    public long sqlite_id = 0;
    String TransactionId, VehicleId, PhoneNumber, PersonId, PulseRatio, MinLimit, FuelTypeId, ServerDate, IntervalToStopFuel, IsTLDCall, EnablePrinter, PumpOnTime, VehicleNumber, TransactionDateWithFormat;

    public int CountBeforeReconnectRelay4 = 0;
    String Response = ""; //Request = ""
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
    Timer timerBt4;
    List<Timer> TimerList_ReadpulseBT4 = new ArrayList<Timer>();
    DBController controller = new DBController(BS_BLE_BTFour.this);
    Boolean IsThisBTTrnx;

    String OffLastTXNid = "0";
    ConnectionDetector cd = new ConnectionDetector(BS_BLE_BTFour.this);
    OffDBController offlineController = new OffDBController(BS_BLE_BTFour.this);
    String ipForUDP = "192.168.4.1";
    public int infoCommandAttempt = 0;
    public boolean isConnected = false;
    public boolean isHotspotDisabled = false;
    public boolean isOnlineTxn = true;
    public int versionNumberOfLinkFour = 0;
    public String PulserTimingAdjust;
    public String IsResetSwitchTimeBounce;

    SimpleDateFormat sdformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    ArrayList<HashMap<String, String>> quantityRecords = new ArrayList<>();

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
                //Request = "";
                stopCount = 0;
                Log.i(TAG, "-Started-");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " -Started-");

                Constants.FS_4STATUS = "BUSY";

                SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
                TransactionId = sharedPref.getString("TransactionId_FS4", "");
                VehicleId = sharedPref.getString("VehicleId_FS4", "");
                VehicleNumber = sharedPref.getString("VehicleNumber_FS4", "");
                PhoneNumber = sharedPref.getString("PhoneNumber_FS4", "");
                PersonId = sharedPref.getString("PersonId_FS4", "");
                PulseRatio = sharedPref.getString("PulseRatio_FS4", "1");
                MinLimit = sharedPref.getString("MinLimit_FS4", "0");
                FuelTypeId = sharedPref.getString("FuelTypeId_FS4", "");
                ServerDate = sharedPref.getString("ServerDate_FS4", "");
                TransactionDateWithFormat = sharedPref.getString("TransactionDateWithFormat_FS4", "");
                IntervalToStopFuel = sharedPref.getString("IntervalToStopFuel_FS4", "0");
                IsTLDCall = sharedPref.getString("IsTLDCall_FS4", "False");
                EnablePrinter = sharedPref.getString("EnablePrinter_FS4", "False");
                PumpOnTime = sharedPref.getString("PumpOnTime_FS4", "0");

                numPulseRatio = Double.parseDouble(PulseRatio);
                minFuelLimit = Double.parseDouble(MinLimit);
                stopAutoFuelSeconds = Long.parseLong(IntervalToStopFuel);

                SharedPreferences calibrationPref = this.getSharedPreferences(Constants.PREF_CalibrationDetails, Context.MODE_PRIVATE);
                PulserTimingAdjust = calibrationPref.getString("PulserTimingAdjust_FS4", "");
                IsResetSwitchTimeBounce = calibrationPref.getString("IsResetSwitchTimeBounce_FS4", "0");

                if (VehicleNumber.length() > 20) {
                    VehicleNumber = VehicleNumber.substring(VehicleNumber.length() - 20);
                }

                //UDP Connection..!!
                if (WelcomeActivity.serverSSIDList != null && WelcomeActivity.serverSSIDList.size() > 0) {
                    LinkCommunicationType = WelcomeActivity.serverSSIDList.get(WelcomeActivity.SelectedItemPos).get("LinkCommunicationType");
                    CurrentLinkMac = WelcomeActivity.serverSSIDList.get(WelcomeActivity.SelectedItemPos).get("MacAddress");
                }

                // Offline functionality
                if (!cd.isConnectingToInternet()) {
                    isOnlineTxn = false;
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " --Offline mode--");
                    offlineLogicBT4();
                } else {
                    isOnlineTxn = true;
                }

                BT_BLE_Constants.isLinkFourNotifyEnabled = false;
                Intent gattServiceIntent = new Intent(this, BLEServiceCodeFour.class);
                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

                registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

                Thread.sleep(2000);
                AppConstants.isRelayON_fs4 = false;
                LinkName = CommonUtils.getlinkName(3);
                if (LinkCommunicationType.equalsIgnoreCase("BT")) {
                    IsThisBTTrnx = true;
                    BT_BLE_Constants.BTBLELinkFourStatus = false;
                    BT_BLE_Constants.BTBLEStatusStrFour = "";
                    checkBTLinkStatus("info"); // Changed from "upgrade" to "info" as per #1657

                } else if (LinkCommunicationType.equalsIgnoreCase("UDP")) {
                    IsThisBTTrnx = false;
                    infoCommand();
                    //BeginProcessUsingUDP();
                } else {
                    //Something went Wrong in hose selection.
                    IsThisBTTrnx = false;
                    Log.i(TAG, " Something went Wrong in hose selection.");
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " Something went wrong in hose selection.");
                    CloseTransaction(false);
                    this.stopSelf();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Service.START_NOT_STICKY;
    }

    public void offlineLogicBT4() {

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

            EnablePrinter = offlineController.getOfflineHubDetails(BS_BLE_BTFour.this).EnablePrinter;

            minFuelLimit = OfflineConstants.getFuelLimit(BS_BLE_BTFour.this);
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " <Fuel Limit: " + minFuelLimit + ">");
            numPulseRatio = Double.parseDouble(PulseRatio);

            stopAutoFuelSeconds = Long.parseLong(IntervalToStopFuel);

            Calendar calendar = Calendar.getInstance();
            TransactionDateWithFormat = BTConstants.dateFormatForOldVersion.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BLEServiceCodeFour.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");

            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(BTConstants.deviceAddress4);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            String BTLinkResponseFormatOld = "LinkBlue notify enabled";
            String BTLinkResponseFormatNew = "{notify : enabled}";
            String res = "";

            try {
                res = intent.getStringExtra(BLEServiceCodeFour.EXTRA_DATA);
                if (res != null) {
                    res = res.replaceAll("\"", "");
                    res = res.trim();

                    if (res.toUpperCase().contains(BTLinkResponseFormatOld.toUpperCase())) {
                        BT_BLE_Constants.isLinkFourNotifyEnabled = true;
                        BT_BLE_Constants.isNewVersionLinkFour = false;
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " <Found BT LINK (OLD)> ");
                    } else if (res.toUpperCase().contains(BTLinkResponseFormatNew.toUpperCase())) {
                        BT_BLE_Constants.isLinkFourNotifyEnabled = true;
                        BT_BLE_Constants.isNewVersionLinkFour = true;
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " <Found BT LINK (New)> ");
                    }
                }

                if (BLEServiceCodeFour.ACTION_GATT_CONNECTED.equals(action)) {
                    System.out.println("ACTION_GATT_QR_CONNECTED");
                } else if (BLEServiceCodeFour.ACTION_GATT_DISCONNECTED.equals(action)) {
                    System.out.println("ACTION_GATT_QR_DISCONNECTED");
                } else if (BLEServiceCodeFour.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    System.out.println("ACTION_GATT_QR_SERVICES_DISCOVERED");
                } else if (BLEServiceCodeFour.ACTION_DATA_AVAILABLE.equals(action)) {
                    System.out.println("ACTION_DATA_AVAILABLE");
                    displayData(intent.getStringExtra(BLEServiceCodeFour.EXTRA_DATA));
                } else {
                    System.out.println("ACTION_GATT_QR_DISCONNECTED");
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " <onReceive Exception: " + e.getMessage() + ">");
            }
        }
    };

    private void displayData(String data) {
        if (data != null) {
            try {

                Response = data;

                //Set Relay status.
                if (BT_BLE_Constants.CurrentCommand_LinkFour.equalsIgnoreCase(BTConstants.relay_off_cmd) && Response.contains("OFF")) {
                    RelayStatus = false;
                } else if (BT_BLE_Constants.CurrentCommand_LinkFour.equalsIgnoreCase(BTConstants.relay_on_cmd) && Response.contains("ON")) {
                    RelayStatus = true;
                    AppConstants.isRelayON_fs4 = true;
                    if (!redpulseloop_on) {
                        ReadPulse();
                    }
                }

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " <Callback BT Resp~~  " + Response.trim() + ">");

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " displayData Exception:" + ex.getMessage());
            }
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

                Log.i(TAG, "Timer count..");

                String checkPulses;
                if (BT_BLE_Constants.isNewVersionLinkFour) {
                    checkPulses = "pulse";
                } else {
                    checkPulses = "pulse:";
                }

                if (!BT_BLE_Constants.BTBLELinkFourStatus && AppConstants.isRelayON_fs4 && !BTConstants.SwitchedBTToUDP4) {
                    if (CountBeforeReconnectRelay4 >= 1) {
                        if (BT_BLE_Constants.BTBLEStatusStrFour.equalsIgnoreCase("Disconnect")) {
                            SaveLastQtyInSharedPref(Constants.FS_4Pulse);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " Retrying to Connect");
                            BTConstants.isRelayOnAfterReconnect4 = false;
                            //Retrying to connect to link
                            LinkReconnectionAttempt();
                            BTConstants.isReconnectCalled4 = true;
                        }
                    } else {
                        CountBeforeReconnectRelay4++;
                    }
                }

                if (BTConstants.isReconnectCalled4 && !BTConstants.isRelayOnAfterReconnect4) {
                    CancelTimer();
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkBTLinkStatus("relay");
                        }
                    }, 100);
                    return;
                }

                CheckResponse(checkPulses);

                if (Response.contains(checkPulses) && RelayStatus) {
                    pulseCount = 0;
                    pulseCount();

                    if (BT_BLE_Constants.isStopButtonPressed4) {
                        BT_BLE_Constants.isStopButtonPressed4 = false;
                        relayOffCommand();
                    }

                } else if (!RelayStatus) {
                    if (pulseCount > 1) { // pulseCount > 4
                        //Stop transaction
                        pulseCount();

                        int delay = 100;
                        cancel();
                        if (BTConstants.SwitchedBTToUDP4) {
                            DisableWifiConnection();
                            BTConstants.SwitchedBTToUDP4 = false;
                            delay = 1000;
                        }
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                TransactionCompleteFunction();
                            }
                        }, delay);

                    } else {
                        pulseCount++;
                        pulseCount();
                        Log.i(TAG, "Check pulse");
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Check pulse >> Response: " + Response.trim());
                    }
                }
            }
        };
        timerBt4.schedule(tt, 1000, 1000);
    }

    public void SaveLastQtyInSharedPref(String Pulses) {
        SharedPreferences sharedPrefLastQty4 = this.getSharedPreferences("LastQuantity_BT4", Context.MODE_PRIVATE);
        long current_count4 = Long.parseLong(String.valueOf(Pulses));
        SharedPreferences.Editor editorQty4 = sharedPrefLastQty4.edit();
        editorQty4.putLong("Last_Quantity", current_count4);
        editorQty4.commit();
    }

    private void pulseCount() {

        try {
            pumpTimingsOnOffFunction();//PumpOn/PumpOff functionality
            String outputQuantity;

            if (BT_BLE_Constants.isNewVersionLinkFour) {
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
            Constants.FS_4Gallons = (precision.format(fillqty));
            Constants.FS_4Pulse = outputQuantity;

            if (isOnlineTxn || BTConstants.SwitchedBTToUDP4) { //cd.isConnectingToInternet()
                UpdateTransactionToSqlite(outputQuantity);
            } else {
                if (fillqty > 0) {
                    offlineController.updateOfflinePulsesQuantity(sqlite_id + "", outputQuantity, fillqty + "", OffLastTXNid);
                }
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Offline >> LINK:" + LinkName + "; P:" + Integer.parseInt(outputQuantity) + "; Q:" + fillqty);
            }

            reachMaxLimit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void UpdateTransactionToSqlite(String outputQuantity) {

        ////////////////////////////////////-Update transaction ---
        TrazComp authEntityClass = new TrazComp();
        authEntityClass.TransactionId = TransactionId;
        authEntityClass.FuelQuantity = fillqty;
        authEntityClass.AppInfo = " Version:" + CommonUtils.getVersionCode(BS_BLE_BTFour.this) + " " + AppConstants.getDeviceName() + " Android " + Build.VERSION.RELEASE + " " + "--Main Transaction--";
        authEntityClass.TransactionFrom = "A";
        authEntityClass.Pulses = Integer.parseInt(outputQuantity);
        authEntityClass.IsFuelingStop = IsFuelingStop;
        authEntityClass.IsLastTransaction = IsLastTransaction;
        authEntityClass.OverrideQuantity = OverrideQuantity;
        authEntityClass.OverridePulse = OverridePulse;

        Gson gson = new Gson();
        String jsonData = gson.toJson(authEntityClass);

        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + " ID:" + TransactionId + "; LINK:" + LinkName + "; Pulses:" + Integer.parseInt(outputQuantity) + "; Qty:" + fillqty);

        String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BS_BLE_BTFour.this).PersonEmail;
        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BS_BLE_BTFour.this) + ":" + userEmail + ":" + "TransactionComplete" + AppConstants.LANG_PARAM);


        HashMap<String, String> imap = new HashMap<>();
        imap.put("jsonData", jsonData);
        imap.put("authString", authString);
        imap.put("sqliteId", sqliteID + "");

        if (fillqty > 0) {

            //in progress (transaction recently started, no new information): Transaction ongoing = 8  --non zero qty
            CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "8", BS_BLE_BTFour.this);
            int rowseffected = controller.updateTransactions(imap);
            System.out.println("rowseffected-" + rowseffected);
            if (rowseffected == 0) {
                controller.insertTransactions(imap);
            }
        }
    }

    private String addStoredQtyToCurrentQty(String outputQuantity) {
        String newQty = outputQuantity;
        try {

            if (BTConstants.isRelayOnAfterReconnect4) {
                SharedPreferences sharedPrefLastQty = this.getSharedPreferences("LastQuantity_BT4", Context.MODE_PRIVATE);
                long storedPulsesCount = sharedPrefLastQty.getLong("Last_Quantity", 0);

                long quantity = Integer.parseInt(outputQuantity);

                long add_count = storedPulsesCount + quantity;

                outputQuantity = Long.toString(add_count);

                newQty = outputQuantity;
            }
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " addStoredQtyToCurrentQty Exception:" + ex.getMessage());
        }
        return newQty;
    }

    private void reachMaxLimit() {

        //if quantity reach max limit
        if (minFuelLimit > 0 && fillqty >= minFuelLimit) {
            Log.i(TAG, "Auto Stop Hit>> You reached MAX fuel limit.");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " Auto Stop Hit>> You reached MAX fuel limit.");
            relayOffCommand(); //RelayOff
            TransactionCompleteFunction();
        }

    }

    private void pumpTimingsOnOffFunction() {

        try {
            int pumpOnpoint = Integer.parseInt(PumpOnTime);

            if (Pulses <= 0) {//PumpOn Time logic
                stopCount++;
                if (stopCount >= pumpOnpoint) {
                    //Timed out (Start was pressed, and pump on timer hit): Pump Time On limit reached* = 4
                    CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "4", BS_BLE_BTFour.this);
                    Log.i(TAG, " PumpOnTime Hit>>" + stopCount);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " PumpOnTime Hit.");
                    relayOffCommand(); //RelayOff
                    TransactionCompleteFunction();
                }
            } else {//PumpOff Time logic

                if (!Pulses.equals(pre_pulse)) {
                    stopCount = 0;
                    pre_pulse = Pulses;
                } else {
                    stopCount++;
                }

                if (stopCount >= stopAutoFuelSeconds) {
                    Log.i(TAG, " PumpOffTime Hit>>" + stopCount);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " PumpOffTime Hit.");
                    relayOffCommand(); //RelayOff
                    TransactionCompleteFunction();
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
                if (RelayStatus && !BT_BLE_Constants.CurrentCommand_LinkFour.contains(BTConstants.relay_off_cmd)) {
                    if (RespCount < 4) {
                        RespCount++;
                    } else {
                        RespCount = 0;
                    }

                    if (RespCount == 4) {
                        RespCount = 0;
                        //Execute fdcheck counter
                        Log.i(TAG, "Execute FD Check..>>");

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

            if (!Response.contains(checkPulses)) {
                stopCount++;
                //int pumpOnpoint = Integer.parseInt(PumpOnTime);
                long autoStopSeconds = 0;
                if (pre_pulse == 0) {
                    autoStopSeconds = Long.parseLong(PumpOnTime);
                } else {
                    autoStopSeconds = stopAutoFuelSeconds;
                }

                if (stopCount >= autoStopSeconds) {
                    if (Pulses <= 0) {
                        CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "4", BS_BLE_BTFour.this);
                    }
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " Auto Stop Hit. Response >> " + Response.trim());
                    stopCount = 0;
                    relayOffCommand(); //RelayOff
                    TransactionCompleteFunction();
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
                Response = "";
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Sending FD_check command to Link: " + LinkName);
                mBluetoothLeService.writeCustomCharacteristic(BTConstants.fdcheckcommand);
            }
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " FD_check command Exception:>>" + ex.getMessage());
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEServiceCodeFour.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEServiceCodeFour.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEServiceCodeFour.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEServiceCodeFour.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void checkBTLinkStatus(String nextAction) {
        try {
            new CountDownTimer(10000, 2000) {
                public void onTick(long millisUntilFinished) {
                    if (BT_BLE_Constants.BTBLEStatusStrFour.equalsIgnoreCase("Connected") && (BT_BLE_Constants.isLinkFourNotifyEnabled)) {
                        BT_BLE_Constants.isLinkFourNotifyEnabled = false;
                        isConnected = true;
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Link is connected.");
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
                            AppConstants.WriteinFile(TAG + " Checking Connection Status...");
                    }
                }

                public void onFinish() {

                    if (BT_BLE_Constants.BTBLEStatusStrFour.equalsIgnoreCase("Connected")) {
                        isConnected = true;
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Link is connected.");
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
                            UDPFunctionalityAfterBTFailure(); //TerminateBTTransaction();
                        } else if (nextAction.equalsIgnoreCase("relay")) { // Terminate BT Txn After Interruption
                            TerminateBTTxnAfterInterruption();
                        }
                    }
                }
            }.start();
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " checkBTLinkStatus Exception:>>" + e.getMessage());
            if (nextAction.equalsIgnoreCase("info")) { // Terminate BT Transaction
                TerminateBTTransaction();
            } else if (nextAction.equalsIgnoreCase("relay")) { // Terminate BT Txn After Interruption
                TerminateBTTxnAfterInterruption();
            }
        }
    }

    private void UDPFunctionalityAfterBTFailure() {
        try {
            if (CommonUtils.CheckAllHTTPLinksAreFree()) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Link not connected. Switching to UDP connection...");

                if (CommonUtils.isHotspotEnabled(BS_BLE_BTFour.this)) {
                    // Disable Hotspot
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "<Disabling hotspot.>");
                    WifiApManager wifiApManager = new WifiApManager(BS_BLE_BTFour.this);
                    wifiApManager.setWifiApEnabled(null, false);
                    isHotspotDisabled = true;
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // Enable Wi-Fi
                WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                wifiManagerMM.setWifiEnabled(true);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        IsThisBTTrnx = false;
                        BTConstants.SwitchedBTToUDP4 = true;
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
            Toast.makeText(BS_BLE_BTFour.this, getResources().getString(R.string.PleaseWaitForWifiConnect), Toast.LENGTH_SHORT).show();

            new CountDownTimer(12000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " Connecting to WiFi...");
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    String ssid = "";
                    if (wifiManager.isWifiEnabled()) {
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        ssid = wifiInfo.getSSID();
                    }

                    ssid = ssid.replace("\"", "");

                    if (ssid.equalsIgnoreCase(LinkName)) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Connected to " + ssid + " via WiFi.");
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
                            AppConstants.WriteinFile(TAG + " Connected to " + ssid + " via WiFi.");
                        proceedToInfoCommand();
                        //loading.cancel();
                        cancel();
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Unable to connect to " + LinkName + " via WiFi.");
                        TerminateBTTransaction();
                    }
                }
            }.start();
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " Exception in BeginProcessUsingUDP: " + e.getMessage());
            TerminateBTTransaction();
            e.printStackTrace();
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

    private void TerminateBTTransaction() {
        try {
            IsThisBTTrnx = false;
            CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "6", BS_BLE_BTFour.this);
            Log.i(TAG, " Link not connected. Please try again!");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " Link not connected.");
            AppConstants.TxnFailedCount4++;
            AppConstants.IsTransactionFailed4 = true;
            CloseTransaction(true);
            this.stopSelf();
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " Exception in TerminateBTTransaction: " + e.getMessage());
        }
    }

    private void TerminateBTTxnAfterInterruption() {
        try {
            IsThisBTTrnx = false;
            if (isOnlineTxn) {
                CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "10", BS_BLE_BTFour.this);
            } else {
                offlineController.updateOfflineTransactionStatus(sqlite_id + "", "10");
            }
            Log.i(TAG, " Link not connected. Please try again!");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " Link not connected.");
            BTConstants.isReconnectCalled4 = false;
            AppConstants.TxnFailedCount4++;
            AppConstants.IsTransactionFailed4 = true;
            CloseTransaction(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void infoCommand() {

        try {
            AppConstants.TxnFailedCount4 = 0;
            AppConstants.isInfoCommandSuccess_fs4 = false;
            //Execute info command
            Response = "";
            if (IsThisBTTrnx) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Sending Info command to Link: " + LinkName);
                mBluetoothLeService.writeCustomCharacteristic(BTConstants.info_cmd);
            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Sending Info command (UDP) to Link: " + LinkName);
                new Thread(new ClientSendAndListenUDPFour(BTConstants.info_cmd, ipForUDP, this)).start();
            }
            //Thread.sleep(1000);
            new CountDownTimer(5000, 1000) {

                public void onTick(long millisUntilFinished) {
                    long attempt = (5 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (BT_BLE_Constants.CurrentCommand_LinkFour.equalsIgnoreCase(BTConstants.info_cmd) && !Response.equalsIgnoreCase("")) {
                            //Info command success.
                            Log.i(TAG, " InfoCommand Response success 1:>>" + Response);

                            if (!TransactionId.isEmpty()) {
                                if (Response.contains("mac_address")) {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " Checking Info command response. Response: true");
                                    parseInfoCommandResponse(Response);
                                    Response = "";
                                } else {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " Checking Info command response. Response:>>" + Response.trim());
                                    parseInfoCommandResponseForLast10txtn(Response.trim()); // parse last 10 Txtn
                                }
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        AppConstants.isInfoCommandSuccess_fs4 = true;
                                        if (IsThisBTTrnx && BT_BLE_Constants.isNewVersionLinkFour && (versionNumberOfLinkFour >= 145)) {
                                            P_Type_Command();
                                        } else {
                                            transactionIdCommand(TransactionId);
                                        }
                                    }
                                }, 1000);
                            } else {
                                Log.i(TAG, " TransactionId is empty.");
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " TransactionId is empty.");
                                CloseTransaction(false);
                            }
                            cancel();
                        } else {
                            Log.i(TAG, " Waiting for infoCommand Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " Checking Info command response. Response: false");
                        }
                    }
                }

                public void onFinish() {

                    if (BT_BLE_Constants.CurrentCommand_LinkFour.equalsIgnoreCase(BTConstants.info_cmd) && !Response.equalsIgnoreCase("")) {
                        //Info command success.
                        Log.i(TAG, " InfoCommand Response success 2:>>" + Response);

                        if (!TransactionId.isEmpty()) {
                            if (Response.contains("mac_address")) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " Checking Info command response. Response: true");
                                parseInfoCommandResponse(Response);
                                Response = "";
                            } else {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " Checking Info command response. Response:>>" + Response.trim());
                                parseInfoCommandResponseForLast10txtn(Response.trim()); // parse last 10 Txtn
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    AppConstants.isInfoCommandSuccess_fs4 = true;
                                    if (IsThisBTTrnx && BT_BLE_Constants.isNewVersionLinkFour && (versionNumberOfLinkFour >= 145)) {
                                        P_Type_Command();
                                    } else {
                                        transactionIdCommand(TransactionId);
                                    }
                                }
                            }, 1000);
                        } else {
                            Log.i(TAG, " TransactionId is empty.");
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " TransactionId is empty.");
                            CloseTransaction(false);
                        }
                    } else {

                        if (infoCommandAttempt > 0) {
                            //UpgradeTransaction Status info command fail.
                            CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "6", BS_BLE_BTFour.this);
                            Log.i(TAG, " Failed to get infoCommand Response:>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " Checking Info command response. Response: false");
                            AppConstants.TxnFailedCount4++;
                            AppConstants.IsTransactionFailed4 = true;
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
                AppConstants.WriteinFile(TAG + " infoCommand Exception:>>" + e.getMessage());
        }
    }

    private void P_Type_Command() {
        try {
            if (IsResetSwitchTimeBounce != null) {
                if (IsResetSwitchTimeBounce.trim().equalsIgnoreCase("1") && !PulserTimingAdjust.isEmpty() && Arrays.asList(BTConstants.p_types).contains(PulserTimingAdjust) && !CommonUtils.CheckDataStoredInSharedPref(BS_BLE_BTFour.this, "storeSwitchTimeBounceFlag4")) {
                    //Execute p_type Command
                    Response = "";

                    if (IsThisBTTrnx) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Sending p_type command to Link: " + LinkName);
                        mBluetoothLeService.writeCustomCharacteristic(BTConstants.p_type_command + PulserTimingAdjust);
                    }

                    new CountDownTimer(4000, 1000) {

                        public void onTick(long millisUntilFinished) {

                            long attempt = (4 - (millisUntilFinished / 1000));
                            if (attempt > 0) {
                                if (BT_BLE_Constants.CurrentCommand_LinkFour.contains(BTConstants.p_type_command) && Response.contains("pulser_type")) {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " Checking p_type command response:>> " + Response);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (BT_BLE_Constants.BTBLEStatusStrFour.equalsIgnoreCase("Disconnect")) {
                                                LinkReconnectionAttempt();
                                            }
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
                                        AppConstants.WriteinFile(TAG + " Checking p_type command response. Response: false");
                                }
                            }
                        }

                        public void onFinish() {

                            if (BT_BLE_Constants.CurrentCommand_LinkFour.contains(BTConstants.p_type_command) && Response.contains("pulser_type")) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " Checking p_type command response:>> " + Response);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (BT_BLE_Constants.BTBLEStatusStrFour.equalsIgnoreCase("Disconnect")) {
                                            LinkReconnectionAttempt();
                                        }
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
                                ContinueToNextCommand();
                            }
                        }
                    }.start();
                } else {
                    ContinueToNextCommand(); //GetPulserTypeCommand(); // Commented get p_type as per #2437 - Nov 17th
                }
            } else {
                ContinueToNextCommand(); //GetPulserTypeCommand();
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " P_Type_Command Exception:>>" + e.getMessage());
            ContinueToNextCommand();
        }
    }

    private void GetPulserTypeCommand() {
        try {
            //Execute p_type Command (to get the pulser type from LINK)
            Response = "";

            if (IsThisBTTrnx) {
                mBluetoothLeService.writeCustomCharacteristic(BTConstants.get_p_type_command);
            }

            new CountDownTimer(4000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long attempt = (4 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (BT_BLE_Constants.CurrentCommand_LinkFour.contains(BTConstants.get_p_type_command) && Response.contains("pulser_type")) {
                            ParsePulserTypeCommandResponse(Response.trim());
                            ContinueToNextCommand();
                            cancel();
                        }
                    }
                }

                public void onFinish() {
                    if (BT_BLE_Constants.CurrentCommand_LinkFour.contains(BTConstants.get_p_type_command) && Response.contains("pulser_type")) {
                        ParsePulserTypeCommandResponse(Response.trim());
                    }
                    ContinueToNextCommand();
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " P_Type_Command (to get the pulser type from LINK) Exception:>>" + e.getMessage());
            ContinueToNextCommand();
        }
    }

    public void ContinueToNextCommand() {
        //if (versionNumberOfLinkFour >= 148) { // Bypass pump reset supported from this version onwards
        //    BypassPumpResetCommand();
        //} else {
        //    // Continue to transactionId Command
        //    transactionIdCommand(TransactionId);
        //}
        if (IsThisBTTrnx && BT_BLE_Constants.isNewVersionLinkFour) {
            last1Command();
        } else {
            // Continue to transactionId Command
            transactionIdCommand(TransactionId);
        }
    }

    private void last1Command() {

        try {
            //Execute last1 Command
            Response = "";

            if (IsThisBTTrnx) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Sending last1 command to Link: " + LinkName);
                mBluetoothLeService.writeCustomCharacteristic(BTConstants.last1_cmd);
            }
            Thread.sleep(500);
            new CountDownTimer(4000, 1000) {

                public void onTick(long millisUntilFinished) {
                    long attempt = (4 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        try {
                            if (BT_BLE_Constants.CurrentCommand_LinkFour.contains(BTConstants.last1_cmd) && Response.contains("records")) {
                                //last1 command success.
                                Log.i(TAG, " last1 Command Response success 1:>>" + Response);
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " Checking last1 command response. Response: true");
                                parseLast1CommandResponse(Response);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        transactionIdCommand(TransactionId);
                                    }
                                }, 1000);
                                cancel();
                            } else {
                                Log.i(TAG, " Waiting for last1 Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " Checking last1 command response. Response: false");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " last1 command Exception. Exception: " + e.getMessage());
                        }
                    }
                }

                public void onFinish() {
                    if (BT_BLE_Constants.CurrentCommand_LinkFour.contains(BTConstants.last1_cmd) && Response.contains("records")) {
                        //last1 command success.
                        Log.i(TAG, " last1 Command Response success 2:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Checking last1 command response. Response: true");
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
                AppConstants.WriteinFile(TAG + " last1Command Exception:>>" + e.getMessage());
            transactionIdCommand(TransactionId);
        }
    }

    private void transactionIdCommand(String transactionId) {

        try {
            //Execute transactionId Command
            Response = "";

            String transaction_id_cmd = BTConstants.transaction_id_cmd; //LK_COMM=txtnid:

            if (BT_BLE_Constants.isNewVersionLinkFour) {
                TransactionDateWithFormat = BTConstants.parseDateForNewVersion(TransactionDateWithFormat);
                transaction_id_cmd = transaction_id_cmd.replace("txtnid:", ""); // For New version LK_COMM=T:XXXXX;D:XXXXX;V:XXXXXXXX;
                transaction_id_cmd = transaction_id_cmd + "T:" + transactionId + ";D:" + TransactionDateWithFormat + ";V:" + VehicleNumber + ";";
            } else {
                transaction_id_cmd = transaction_id_cmd + transactionId;
            }

            if (IsThisBTTrnx) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Sending transactionId command to Link: " + LinkName);
                mBluetoothLeService.writeCustomCharacteristic(transaction_id_cmd);
            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Sending transactionId command (UDP) to Link: " + LinkName);
                new Thread(new ClientSendAndListenUDPFour(transaction_id_cmd, ipForUDP, this)).start();
            }
            Thread.sleep(500);
            new CountDownTimer(4000, 1000) {

                public void onTick(long millisUntilFinished) {
                    long attempt = (4 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        try {
                            if (BT_BLE_Constants.CurrentCommand_LinkFour.contains(transactionId) && Response.contains(transactionId)) {
                                //transactionId command success.
                                Log.i(TAG, " transactionId Command Response success 1:>>" + Response);
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " Checking transactionId command response. Response:>>" + Response.trim());
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        relayOnCommand(false); //RelayOn
                                    }
                                }, 1000);
                                cancel();
                            } else {
                                Log.i(TAG, " Waiting for transactionId Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " Checking transactionId command response. Response: false");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " transactionId command Exception. Exception: " + e.getMessage());
                        }
                    }
                }

                public void onFinish() {

                    if (BT_BLE_Constants.CurrentCommand_LinkFour.contains(transactionId) && Response.contains(transactionId)) {
                        //transactionId command success.
                        Log.i(TAG, " transactionId Command Response success 2:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Checking transactionId command response. Response:>>" + Response.trim());
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                relayOnCommand(false); //RelayOn
                            }
                        }, 1000);
                    } else {

                        //UpgradeTransaction Status Transactionid command fail.
                        CommonUtils.UpgradeTransactionStatusToSqlite(transactionId, "6", BS_BLE_BTFour.this);
                        Log.i(TAG, " Failed to get transactionId Command Response:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Checking transactionId command response. Response: false");
                        CloseTransaction(true);
                    }
                }
            }.start();

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " transactionIdCommand Exception:>>" + e.getMessage());
        }
    }

    private void relayOnCommand(boolean isAfterReconnect) {
        try {
            if (isAfterReconnect) {
                BTConstants.isReconnectCalled4 = false;
            }
            //Execute relayOn Command
            Response = "";

            if (IsThisBTTrnx) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Sending relayOn command to Link: " + LinkName);
                mBluetoothLeService.writeCustomCharacteristic(BTConstants.relay_on_cmd);
            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Sending relayOn command (UDP) to Link: " + LinkName);
                new Thread(new ClientSendAndListenUDPFour(BTConstants.relay_on_cmd, ipForUDP, this)).start();
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
                            BTConstants.isRelayOnAfterReconnect4 = isAfterReconnect;
                            //relayOn command success.
                            Log.i(TAG, " relayOn Command Response success 1:>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " Checking relayOn command response. Response: ON");
                            cancel();
                        } else {
                            Log.i(TAG, " Waiting for relayOn Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " Checking relayOn command response. Response: false");
                        }
                    }
                }

                public void onFinish() {

                    if (RelayStatus) {
                        BTConstants.isRelayOnAfterReconnect4 = isAfterReconnect;
                        //relayOn command success.
                        Log.i(TAG, " relayOn Command Response success 2:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Checking relayOn command response. Response: ON");
                    } else {

                        //UpgradeTransaction Status RelayON command fail.
                        if (isAfterReconnect && (fillqty > 0)) {
                            if (isOnlineTxn) {
                                CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "10", BS_BLE_BTFour.this);
                            } else {
                                offlineController.updateOfflineTransactionStatus(sqlite_id + "", "10");
                            }
                        } else {
                            CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId, "6", BS_BLE_BTFour.this);
                        }
                        Log.i(TAG, " Failed to get relayOn Command Response:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Checking relayOn command response. Response: false");
                        relayOffCommand(); //RelayOff
                        TransactionCompleteFunction();
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " relayOnCommand Exception:>>" + e.getMessage());
        }
    }

    private void relayOffCommand() {
        try {
            //Execute relayOff Command
            Response = "";
            if (IsThisBTTrnx) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Sending relayOff command to Link: " + LinkName);
                mBluetoothLeService.writeCustomCharacteristic(BTConstants.relay_off_cmd);
            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Sending relayOff command (UDP) to Link: " + LinkName);
                new Thread(new ClientSendAndListenUDPFour(BTConstants.relay_off_cmd, ipForUDP, this)).start();
            }

            new CountDownTimer(4000, 1000) {

                public void onTick(long millisUntilFinished) {
                    long attempt = (4 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (!RelayStatus) {
                            //relayOff command success.
                            Log.i(TAG, " relayOff Command Response success 1:>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " Checking relayOff command response. Response:>>" + Response.trim());
                            cancel();
                        } else {
                            Log.i(TAG, " Waiting for relayOff Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " Checking relayOff command response. Response: false");
                        }
                    }
                }

                public void onFinish() {

                    if (!RelayStatus) {
                        Log.i(TAG, " relayOff Command Response success 2:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Checking relayOff command response. Response:>>" + Response.trim());
                    } else {
                        Log.i(TAG, " Failed to get relayOff Command Response:>>" + Response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Checking relayOff command response. Response: false");
                        PostTransactionBackgroundTasks();
                        //CloseTransaction();
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void InsertInitialTransactionToSqlite() {

        String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BS_BLE_BTFour.this).PersonEmail;
        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BS_BLE_BTFour.this) + ":" + userEmail + ":" + "TransactionComplete" + AppConstants.LANG_PARAM);

        HashMap<String, String> imap = new HashMap<>();
        imap.put("jsonData", "");
        imap.put("authString", authString);

        sqliteID = controller.insertTransactions(imap);
        CommonUtils.AddRemovecurrentTransactionList(true, TransactionId);//Add transaction Id to list

    }

    private void TransactionCompleteFunction() {

        if (cd.isConnectingToInternet()) {
            if (BTConstants.BT4REPLACEBLE_WIFI_NAME == null) {
                BTConstants.BT4REPLACEBLE_WIFI_NAME = "";
            }
            //BTLink Rename functionality
            if (BTConstants.BT4NeedRename && !BTConstants.BT4REPLACEBLE_WIFI_NAME.isEmpty()) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        renameOnCommand();
                    }
                }, 1000);
            } else {
                CloseTransaction(true);
            }
        } else {
            CloseTransaction(true);
        }
    }

    private void renameOnCommand() {
        try {
            //Execute rename Command
            Response = "";

            if (IsThisBTTrnx) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Sending rename command to Link: " + LinkName + " (New Name: " + BTConstants.BT4REPLACEBLE_WIFI_NAME + ")");
                mBluetoothLeService.writeCustomCharacteristic(BTConstants.namecommand + BTConstants.BT4REPLACEBLE_WIFI_NAME);
            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Sending rename command (UDP) to Link: " + LinkName + " (New Name: " + BTConstants.BT4REPLACEBLE_WIFI_NAME + ")");
                new Thread(new ClientSendAndListenUDPFour(BTConstants.namecommand + BTConstants.BT4REPLACEBLE_WIFI_NAME, ipForUDP, this)).start();
            }

            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BS_BLE_BTFour.this).PersonEmail;
            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(this) + ":" + userEmail + ":" + "SetHoseNameReplacedFlag" + AppConstants.LANG_PARAM);

            RenameHose rhose = new RenameHose();
            rhose.SiteId = BTConstants.BT4SITE_ID;
            rhose.HoseId = BTConstants.BT4HOSE_ID;
            rhose.IsHoseNameReplaced = "Y";

            Gson gson = new Gson();
            String jsonData = gson.toJson(rhose);

            storeIsRenameFlag(this, BTConstants.BT4NeedRename, jsonData, authString);

            Thread.sleep(1000);
            CloseTransaction(true);

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " renameCommand Exception:>>" + e.getMessage());
        }
    }

    public void storeIsRenameFlag(Context context, boolean flag, String jsonData, String authString) {
        SharedPreferences pref;

        SharedPreferences.Editor editor;
        pref = context.getSharedPreferences("storeIsRenameFlagFS4", 0);
        editor = pref.edit();

        // Storing
        editor.putBoolean("flag", flag);
        editor.putString("jsonData", jsonData);
        editor.putString("authString", authString);

        // commit changes
        editor.commit();
    }

    public void WaitForReconnectToLink() {
        try {
            new CountDownTimer(10000, 1000) {

                public void onTick(long millisUntilFinished) {

                    long attempt = (10 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (BT_BLE_Constants.BTBLEStatusStrFour.equalsIgnoreCase("Connected")) {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " Connected to Link: " + LinkName);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    GetPulserTypeCommand();
                                }
                            }, 500);
                            cancel();
                        } else {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " Waiting for Reconnect to Link: " + LinkName);
                        }
                    }
                }

                public void onFinish() {

                    if (BT_BLE_Constants.BTBLEStatusStrFour.equalsIgnoreCase("Connected")) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Connected to Link: " + LinkName);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                GetPulserTypeCommand();
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
                AppConstants.WriteinFile(TAG + " WaitForReconnectToLink Exception:>>" + e.getMessage());
        }
    }

    private void parseInfoCommandResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);

            JSONObject versionJsonObj = jsonObject.getJSONObject("version");
            String version = versionJsonObj.getString("version");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " LINK Version >> " + version);
            storeUpgradeFSVersion(BS_BLE_BTFour.this, AppConstants.UP_HoseId_fs4, version);
            versionNumberOfLinkFour = CommonUtils.GetVersionNumberFromLink(version);

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " Exception in parseInfoCommandResponse. response>> " + response + "; Exception>>" + e.getMessage());
        }
    }

    public void storeUpgradeFSVersion(Context context, String hoseid, String fsversion) {

        SharedPreferences sharedPref = context.getSharedPreferences(Constants.PREF_FS_UPGRADE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("hoseid_bt4", hoseid);
        editor.putString("fsversion_bt4", fsversion);
        editor.commit();
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
                                    AppConstants.WriteinFile(TAG + " Last10 txtn parsing exception:>>" + e.getMessage());
                            }
                        } else {

                            if (res.contains("version:")) {
                                version = res.substring(res.indexOf(":") + 1).trim();
                            }
                            if (!version.isEmpty()) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " LINK Version >> " + version);
                                storeUpgradeFSVersion(BS_BLE_BTFour.this, AppConstants.UP_HoseId_fs4, version);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " Exception in parseInfoCommandResponseForLast10txtn. response>> " + response + "; Exception>>" + e.getMessage());
        }
    }

    private String removeLastChar(String s) {

        if (s.isEmpty())
            return "";

        return s.substring(0, s.length() - 1);
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
            authEntityClass.AppInfo = " Version:" + CommonUtils.getVersionCode(BS_BLE_BTFour.this) + " " + AppConstants.getDeviceName() + " Android " + android.os.Build.VERSION.RELEASE + " " + "--Last Transaction--";
            authEntityClass.TransactionFrom = "A";
            authEntityClass.Pulses = Integer.parseInt(counts);
            authEntityClass.IsFuelingStop = IsFuelingStop;
            authEntityClass.IsLastTransaction = "1";

            Gson gson = new Gson();
            String jsonData = gson.toJson(authEntityClass);

            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " <Last Transaction saved in local DB. LastTXNid:" + txnId + "; LINK:" + LinkName + "; Pulses:" + Integer.parseInt(counts) + "; Qty:" + Lastqty + ">");

            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BS_BLE_BTFour.this).PersonEmail;
            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BS_BLE_BTFour.this) + ":" + userEmail + ":" + "TransactionComplete" + AppConstants.LANG_PARAM);

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
                AppConstants.WriteinFile(TAG + " SaveLastBTTransactionToServer Exception: " + e.getMessage());
        }
    }

    private void parseLast1CommandResponse(String response) {
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

            SharedPreferences sharedPref = BS_BLE_BTFour.this.getSharedPreferences("storeCmtxtnid_20_record", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("BLE_LINK4", json20txn);
            editor.apply();

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " Exception in parseLast1CommandResponse. response>> " + response + "; Exception>>" + e.getMessage());
        }
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

    public class EntityCmd20Txn {
        ArrayList cmtxtnid_20_record;
        String jsonfromLink;
    }

    private void ParsePulserTypeCommandResponse(String response) {
        try {
            String pulserType;

            if (response.contains("pulser_type")) {
                JSONObject jsonObj = new JSONObject(response);
                pulserType = jsonObj.getString("pulser_type");

                if (!pulserType.isEmpty() && Arrays.asList(BTConstants.p_types).contains(pulserType)) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " Pulser Type from Link >> " + pulserType);
                    // Create object and save data to upload
                    String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BS_BLE_BTFour.this).PersonEmail;

                    String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BS_BLE_BTFour.this) + ":" + userEmail + ":" + "UpdatePulserTypeOfLINK" + AppConstants.LANG_PARAM);

                    UpdatePulserTypeOfLINK_entity updatePulserTypeOfLINK = new UpdatePulserTypeOfLINK_entity();
                    updatePulserTypeOfLINK.IMEIUDID = AppConstants.getIMEI(BS_BLE_BTFour.this);
                    updatePulserTypeOfLINK.Email = userEmail;
                    updatePulserTypeOfLINK.SiteId = BTConstants.BT4SITE_ID;
                    updatePulserTypeOfLINK.PulserType = pulserType;
                    updatePulserTypeOfLINK.DateTimeFromApp = AppConstants.currentDateFormat("MM/dd/yyyy HH:mm:ss");

                    Gson gson = new Gson();
                    String jsonData = gson.toJson(updatePulserTypeOfLINK);

                    storePulserTypeDetails(BS_BLE_BTFour.this, jsonData, authString);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " Exception in ParsePulserTypeCommandResponse. response>> " + response + "; Exception>>" + e.getMessage());
        }
    }

    private void storePulserTypeDetails(Context context, String jsonData, String authString) {
        try {
            SharedPreferences pref;
            SharedPreferences.Editor editor;

            pref = context.getSharedPreferences("UpdatePulserType4", 0);
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

    private void LinkReconnectionAttempt() {
        unbindService(mServiceConnection);
        unregisterReceiver(mGattUpdateReceiver);

        Intent gattServiceIntent = new Intent(this, BLEServiceCodeFour.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    private void UpdateSwitchTimeBounceForLink() {
        try {
            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BS_BLE_BTFour.this).PersonEmail;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BS_BLE_BTFour.this) + ":" + userEmail + ":" + "UpdateSwitchTimeBounceForLink" + AppConstants.LANG_PARAM);

            SwitchTimeBounce switchTimeBounce = new SwitchTimeBounce();
            switchTimeBounce.SiteId = BTConstants.BT4SITE_ID;
            switchTimeBounce.IsResetSwitchTimeBounce = "0";

            Gson gson = new Gson();
            String jsonData = gson.toJson(switchTimeBounce);

            storeSwitchTimeBounceFlag(BS_BLE_BTFour.this, jsonData, authString);

        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " UpdateSwitchTimeBounceForLink Exception: " + ex.getMessage());
        }
    }

    public void storeSwitchTimeBounceFlag(Context context, String jsonData, String authString) {
        try {
            SharedPreferences pref;
            SharedPreferences.Editor editor;

            pref = context.getSharedPreferences("storeSwitchTimeBounceFlag4", 0);
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

    private void CloseTransaction(boolean startBackgroundServices) {

        try {
            clearEditTextFields();
            try {
                unbindService(mServiceConnection);
                unregisterReceiver(mGattUpdateReceiver);
            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " <Exception occurred while unregistering receiver: " + e.getMessage() + ">");
            }
            stopTxtprocess = true;
            BTConstants.isRelayOnAfterReconnect4 = false;
            AppConstants.clearSharedPrefByName(BS_BLE_BTFour.this, "LastQuantity_BT4");
            CommonUtils.AddRemovecurrentTransactionList(false, TransactionId);
            Constants.FS_4STATUS = "FREE";
            CountBeforeReconnectRelay4 = 0;
            Constants.FS_4Pulse = "00";
            AppConstants.GoButtonAlreadyClicked = false;
            AppConstants.IsTransactionCompleted = true;
            AppConstants.isInfoCommandSuccess_fs4 = false;
            BTConstants.SwitchedBTToUDP4 = false;
            DisableWifiConnection();
            CancelTimer();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " Transaction stopped.");
            if (startBackgroundServices) {
                PostTransactionBackgroundTasks();
            }
            this.stopSelf();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " CloseTransaction Exception:>>" + e.getMessage());
        }
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
    private void PostTransactionBackgroundTasks() {
        try {
            if (cd.isConnectingToInternet()) {
                // Save upgrade details to cloud
                SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_FS_UPGRADE, Context.MODE_PRIVATE);
                String hoseid = sharedPref.getString("hoseid_bt4", "");
                String fsversion = sharedPref.getString("fsversion_bt4", "");

                UpgradeVersionEntity objEntityClass = new UpgradeVersionEntity();
                objEntityClass.IMEIUDID = AppConstants.getIMEI(BS_BLE_BTFour.this);
                objEntityClass.Email = CommonUtils.getCustomerDetails_backgroundServiceBT(BS_BLE_BTFour.this).PersonEmail;
                objEntityClass.HoseId = hoseid;
                objEntityClass.Version = fsversion;

                if (hoseid != null && !hoseid.trim().isEmpty()) {
                    new BS_BLE_BTFour.UpgradeCurrentVersionWithUpgradableVersion(objEntityClass).execute();

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

                boolean BSRunning = CommonUtils.checkServiceRunning(BS_BLE_BTFour.this, AppConstants.PACKAGE_BACKGROUND_SERVICE);
                if (!BSRunning) {
                    startService(new Intent(this, BackgroundService.class));
                }
            }

            // Offline transaction data sync
            if (OfflineConstants.isOfflineAccess(BS_BLE_BTFour.this))
                SyncOfflineData();

        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BackgroundTasksPostTransaction Exception: " + e.getMessage());
        }
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
                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(objUpgrade.IMEIUDID + ":" + objUpgrade.Email + ":" + "UpgradeCurrentVersionWithUgradableVersion" + AppConstants.LANG_PARAM);
                response = serverHandler.PostTextData(BS_BLE_BTFour.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " UpgradeCurrentVersionWithUpgradableVersion Exception: " + ex.getMessage());
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
                    // Saving empty value to clear sharedPref
                    storeUpgradeFSVersion(BS_BLE_BTFour.this, "", "");
                }
            } catch (Exception e) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " UpgradeCurrentVersionWithUpgradableVersion onPostExecute Exception: " + e.getMessage());
            }
        }
    }

    private void SyncOfflineData() {

        if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE") && Constants.FS_5STATUS.equalsIgnoreCase("FREE") && Constants.FS_6STATUS.equalsIgnoreCase("FREE")) {

            if (cd.isConnecting()) {

                try {
                    //sync offline transactions
                    String off_json = offlineController.getAllOfflineTransactionJSON(BS_BLE_BTFour.this);
                    JSONObject jsonObj = new JSONObject(off_json);
                    String offTransactionArray = jsonObj.getString("TransactionsModelsObj");
                    JSONArray jArray = new JSONArray(offTransactionArray);

                    if (jArray.length() > 0) {
                        startService(new Intent(BS_BLE_BTFour.this, OffTranzSyncService.class));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void clearEditTextFields() {

        Constants.AccVehicleNumber_FS4 = "";
        Constants.AccOdoMeter_FS4 = 0;
        Constants.AccDepartmentNumber_FS4 = "";
        Constants.AccPersonnelPIN_FS4 = "";
        Constants.AccOther_FS4 = "";
        Constants.AccVehicleOther_FS4 = "";
        Constants.AccHours_FS4 = 0;
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
                    if (isHotspotDisabled) {
                        //Enable Hotspot
                        WifiApManager wifiApManager = new WifiApManager(BS_BLE_BTFour.this);
                        if (!CommonUtils.isHotspotEnabled(BS_BLE_BTFour.this) && !AppConstants.isAllLinksAreBTLinks) {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "<Enabling hotspot.>");
                            wifiApManager.setWifiApEnabled(null, true);
                        }
                        isHotspotDisabled = false;
                    }
                    if (cd.isConnectingToInternet()) {
                        boolean BSRunning = CommonUtils.checkServiceRunning(BS_BLE_BTFour.this, AppConstants.PACKAGE_BACKGROUND_SERVICE);
                        if (!BSRunning) {
                            startService(new Intent(BS_BLE_BTFour.this, BackgroundService.class));
                        }
                    }
                }
            }, 2000);
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " DisableWifiConnection Exception>> " + e.getMessage());
        }
    }
}
