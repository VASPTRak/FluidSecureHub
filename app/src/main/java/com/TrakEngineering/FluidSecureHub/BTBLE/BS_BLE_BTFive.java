package com.TrakEngineering.FluidSecureHub.BTBLE;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.TrakEngineering.FluidSecureHub.AppConstants;
import com.TrakEngineering.FluidSecureHub.BTBLE.BTBLE_LinkFive.BLEServiceCodeFive;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTConstants;
import com.TrakEngineering.FluidSecureHub.BackgroundService;
import com.TrakEngineering.FluidSecureHub.CommonUtils;
import com.TrakEngineering.FluidSecureHub.ConnectionDetector;
import com.TrakEngineering.FluidSecureHub.Constants;
import com.TrakEngineering.FluidSecureHub.DBController;
import com.TrakEngineering.FluidSecureHub.WelcomeActivity;
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

public class BS_BLE_BTFive extends Service {
    private static final String TAG = AppConstants.LOG_TXTN_BT + "- BLE_Link_5:";
    private BLEServiceCodeFive mBluetoothLeService;

    public long sqlite_id = 0;
    String TransactionId, VehicleId, PhoneNumber, PersonId, PulseRatio, MinLimit, FuelTypeId, ServerDate, IntervalToStopFuel, IsTLDCall, EnablePrinter, PumpOnTime, LimitReachedMessage, VehicleNumber, TransactionDateWithFormat;

    public int countBeforeReconnectRelay5 = 0;
    String Response = ""; //Request = ""
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
    Timer timerBt5;
    List<Timer> timerList_ReadPulseBT5 = new ArrayList<Timer>();
    DBController controller = new DBController(BS_BLE_BTFive.this);
    Boolean IsThisBTTrnx;

    String OffLastTXNid = "0";
    ConnectionDetector cd = new ConnectionDetector(BS_BLE_BTFive.this);
    OffDBController offlineController = new OffDBController(BS_BLE_BTFive.this);
    //String ipForUDP = "192.168.4.1"; // Removed UDP code as per #2603
    //public int infoCommandAttempt = 0;
    public boolean isConnected = false;
    public boolean isHotspotDisabled = false;
    public boolean isOnlineTxn = true;
    public String versionNumberOfLinkFive = "";
    public String PulserTimingAdjust, IsResetSwitchTimeBounce, GetPulserTypeFromLINK;
    public boolean IsAnyPostTxnCommandExecuted = false;
    public boolean isTxnLimitReached = false;
    //public int relayOffAttemptCount = 0;
    private boolean isInfoCMDSentAfterRelayOff = false;

    SimpleDateFormat sdformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    ArrayList<HashMap<String, String>> quantityRecords = new ArrayList<>();

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
                //Request = "";
                stopCount = 0;
                Log.i(TAG, "-Started-");
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " -Started-");

                Constants.FS_5_STATUS = "BUSY";

                SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_VEHI_FUEL, Context.MODE_PRIVATE);
                TransactionId = sharedPref.getString("TransactionId_FS5", "");
                VehicleId = sharedPref.getString("VehicleId_FS5", "");
                VehicleNumber = sharedPref.getString("VehicleNumber_FS5", "");
                PhoneNumber = sharedPref.getString("PhoneNumber_FS5", "");
                PersonId = sharedPref.getString("PersonId_FS5", "");
                PulseRatio = sharedPref.getString("PulseRatio_FS5", "1");
                MinLimit = sharedPref.getString("MinLimit_FS5", "0");
                FuelTypeId = sharedPref.getString("FuelTypeId_FS5", "");
                ServerDate = sharedPref.getString("ServerDate_FS5", "");
                TransactionDateWithFormat = sharedPref.getString("TransactionDateWithFormat_FS5", "");
                IntervalToStopFuel = sharedPref.getString("IntervalToStopFuel_FS5", "0");
                IsTLDCall = sharedPref.getString("IsTLDCall_FS5", "False");
                EnablePrinter = sharedPref.getString("EnablePrinter_FS5", "False");
                PumpOnTime = sharedPref.getString("PumpOnTime_FS5", "0");
                LimitReachedMessage = sharedPref.getString("LimitReachedMessage_FS5", "");

                numPulseRatio = Double.parseDouble(PulseRatio);
                minFuelLimit = Double.parseDouble(MinLimit);
                stopAutoFuelSeconds = Long.parseLong(IntervalToStopFuel);

                SharedPreferences calibrationPref = this.getSharedPreferences(Constants.PREF_CALIBRATION_DETAILS, Context.MODE_PRIVATE);
                PulserTimingAdjust = calibrationPref.getString("PulserTimingAdjust_FS5", "");
                IsResetSwitchTimeBounce = calibrationPref.getString("IsResetSwitchTimeBounce_FS5", "0");
                GetPulserTypeFromLINK = calibrationPref.getString("GetPulserTypeFromLINK_FS5", "False");

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
                        AppConstants.writeInFile(TAG + " --Offline mode--");
                    offlineLogicBT5();
                }

                BT_BLE_Constants.IS_LINK_FIVE_NOTIFY_ENABLED = false;
                Intent gattServiceIntent = new Intent(this, BLEServiceCodeFive.class);
                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

                registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

                Thread.sleep(2000);
                AppConstants.IS_RELAY_ON_FS5 = false;
                LinkName = CommonUtils.getLinkName(4);
                if (LinkCommunicationType.equalsIgnoreCase("BT")) {
                    IsThisBTTrnx = true;
                    BT_BLE_Constants.BT_BLE_LINK_FIVE_STATUS = false;
                    BT_BLE_Constants.BT_BLE_STATUS_STR_FIVE = "";
                    checkBTLinkStatus("last1"); // Changed from "upgrade" to "info" as per #1657, And "info" to "last1" as per #2724
                /*} else if (LinkCommunicationType.equalsIgnoreCase("UDP")) {
                    IsThisBTTrnx = false;
                    infoCommand();
                    //BeginProcessUsingUDP();*/
                } else {
                    //Something went Wrong in hose selection.
                    IsThisBTTrnx = false;
                    Log.i(TAG, " Something went Wrong in hose selection.");
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + " Something went wrong in hose selection. (Link CommType: " + LinkCommunicationType + ")");
                    stopTransaction(false, true); // Link CommType unknown
                    this.stopSelf();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Service.START_NOT_STICKY;
    }

    public void offlineLogicBT5() {
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

            EnablePrinter = offlineController.getOfflineHubDetails(BS_BLE_BTFive.this).EnablePrinter;

            minFuelLimit = OfflineConstants.getFuelLimit(BS_BLE_BTFive.this);
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " <Fuel Limit: " + minFuelLimit + ">");
            numPulseRatio = Double.parseDouble(PulseRatio);

            stopAutoFuelSeconds = Long.parseLong(IntervalToStopFuel);

            Calendar calendar = Calendar.getInstance();
            TransactionDateWithFormat = BTConstants.DATE_FORMAT_FOR_OLD_VERSION.format(calendar.getTime());
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
            mBluetoothLeService = ((BLEServiceCodeFive.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");

            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(BTConstants.DEVICE_ADDRESS_5);
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
                res = intent.getStringExtra(BLEServiceCodeFive.EXTRA_DATA);
                if (res != null) {
                    res = res.replaceAll("\"", "");
                    res = res.trim();

                    if (res.toUpperCase().contains(BTLinkResponseFormatOld.toUpperCase())) {
                        BT_BLE_Constants.IS_LINK_FIVE_NOTIFY_ENABLED = true;
                        BT_BLE_Constants.IS_NEW_VERSION_LINK_FIVE = false;
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " <Found BT LINK (OLD)> ");
                    } else if (res.toUpperCase().contains(BTLinkResponseFormatNew.toUpperCase())) {
                        BT_BLE_Constants.IS_LINK_FIVE_NOTIFY_ENABLED = true;
                        BT_BLE_Constants.IS_NEW_VERSION_LINK_FIVE = true;
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " <Found BT LINK (New)> ");
                    }
                }

                if (BLEServiceCodeFive.ACTION_GATT_CONNECTED.equals(action)) {
                    System.out.println("ACTION_GATT_QR_CONNECTED");
                } else if (BLEServiceCodeFive.ACTION_GATT_DISCONNECTED.equals(action)) {
                    System.out.println("ACTION_GATT_QR_DISCONNECTED");
                } else if (BLEServiceCodeFive.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    System.out.println("ACTION_GATT_QR_SERVICES_DISCOVERED");
                } else if (BLEServiceCodeFive.ACTION_DATA_AVAILABLE.equals(action)) {
                    System.out.println("ACTION_DATA_AVAILABLE");
                    displayData(intent.getStringExtra(BLEServiceCodeFive.EXTRA_DATA));
                } else {
                    System.out.println("ACTION_GATT_QR_DISCONNECTED");
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " <onReceive Exception: " + e.getMessage() + ">");
            }
        }
    };

    private void displayData(String data) {
        if (data != null) {
            try {
                if (isInfoCMDSentAfterRelayOff) {
                    if (data.contains("pulse") || data.contains("relay")) {
                        return;
                    }
                }
                Response = data;

                //Set Relay status.
                if (BT_BLE_Constants.CURRENT_COMMAND_LINK_FIVE.equalsIgnoreCase(BTConstants.RELAY_OFF_COMMAND) && Response.contains("OFF")) {
                    RelayStatus = false;
                } else if (BT_BLE_Constants.CURRENT_COMMAND_LINK_FIVE.equalsIgnoreCase(BTConstants.RELAY_ON_COMMAND) && Response.contains("ON")) {
                    RelayStatus = true;
                    AppConstants.IS_RELAY_ON_FS5 = true;
                    if (!redpulseloop_on) {
                        readPulse();
                    }
                }

                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " <Callback BT Resp~~  " + Response.trim() + ">");

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " displayData Exception:" + ex.getMessage());
            }
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
        timerBt5 = new Timer();
        timerList_ReadPulseBT5.add(timerBt5);
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                //Repaeting code..
                //cancelTimer(); cancel all once done.

                Log.i(TAG, "Timer count..");

                String checkPulses;
                if (BT_BLE_Constants.IS_NEW_VERSION_LINK_FIVE) {
                    checkPulses = "pulse";
                } else {
                    checkPulses = "pulse:";
                }

                if (!BT_BLE_Constants.BT_BLE_LINK_FIVE_STATUS && AppConstants.IS_RELAY_ON_FS5) { // && !BTConstants.SwitchedBTToUDP5
                    if (countBeforeReconnectRelay5 >= 1) {
                        if (BT_BLE_Constants.BT_BLE_STATUS_STR_FIVE.equalsIgnoreCase("Disconnect")) {
                            saveLastQtyInSharedPref(Constants.FS_5_PULSE);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " Retrying to Connect");
                            BTConstants.IS_RELAY_ON_AFTER_RECONNECT_5 = false;
                            //Retrying to connect to link
                            linkReconnectionAttempt();
                            BTConstants.IS_RECONNECT_CALLED_5 = true;
                        }
                    } else {
                        countBeforeReconnectRelay5++;
                    }
                }

                if (BTConstants.IS_RECONNECT_CALLED_5 && !BTConstants.IS_RELAY_ON_AFTER_RECONNECT_5) {
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

                if (RelayStatus) {
                    if (BT_BLE_Constants.IS_STOP_BUTTON_PRESSED_5) {
                        BT_BLE_Constants.IS_STOP_BUTTON_PRESSED_5 = false;
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                relayOffCommand();
                            }
                        }, 100);
                    }
                }

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

                        int delay = 1000;
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //transactionCompleteFunction();
                                isInfoCMDSentAfterRelayOff = true;
                                infoCommand();
                            }
                        }, delay);
                        cancel();
                    } else {
                        pulseCount++;
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                pulseCount();
                            }
                        }, 100);
                        Log.i(TAG, "Check pulse");
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " Check pulse >> Response: " + Response.trim());
                    }
                } else if (!Response.contains(checkPulses)) {
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
                            CommonUtils.upgradeTransactionStatusToSqlite(TransactionId, "4", BS_BLE_BTFive.this);
                        }
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " Auto Stop Hit. Response >> " + Response.trim());
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
        timerBt5.schedule(tt, 1000, 1000);
    }

    public void saveLastQtyInSharedPref(String Pulses) {
        SharedPreferences sharedPrefLastQty5 = this.getSharedPreferences("LastQuantity_BT5", Context.MODE_PRIVATE);
        long current_count5 = Long.parseLong(String.valueOf(Pulses));
        SharedPreferences.Editor editorQty5 = sharedPrefLastQty5.edit();
        editorQty5.putLong("Last_Quantity", current_count5);
        editorQty5.commit();
    }

    private void pulseCount() {
        try {
            pumpTimingsOnOffFunction();//PumpOn/PumpOff functionality
            String outputQuantity;

            if (BT_BLE_Constants.IS_NEW_VERSION_LINK_FIVE) {
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
            Constants.FS_5_GALLONS = (precision.format(fillqty));
            Constants.FS_5_PULSE = outputQuantity;

            if (isOnlineTxn) { // || BTConstants.SwitchedBTToUDP5
                updateTransactionToSqlite(outputQuantity);
            } else {
                if (Pulses > 0 || fillqty > 0) {
                    offlineController.updateOfflinePulsesQuantity(sqlite_id + "", outputQuantity, fillqty + "", OffLastTXNid);
                }
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " Offline >> LINK:" + LinkName + "; P:" + Integer.parseInt(outputQuantity) + "; Q:" + fillqty);
            }

            reachMaxLimit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTransactionToSqlite(String outputQuantity) {
        ////////////////////////////////////-Update transaction ---
        TrazComp authEntityClass = new TrazComp();
        authEntityClass.TransactionId = TransactionId;
        authEntityClass.FuelQuantity = fillqty;
        authEntityClass.AppInfo = " Version:" + CommonUtils.getVersionCode(BS_BLE_BTFive.this) + " " + AppConstants.getDeviceName() + " Android " + Build.VERSION.RELEASE + " " + "--Main Transaction--";
        authEntityClass.TransactionFrom = "A";
        authEntityClass.Pulses = Integer.parseInt(outputQuantity);
        authEntityClass.IsFuelingStop = IsFuelingStop;
        authEntityClass.IsLastTransaction = IsLastTransaction;
        authEntityClass.OverrideQuantity = OverrideQuantity;
        authEntityClass.OverridePulse = OverridePulse;

        Gson gson = new Gson();
        String jsonData = gson.toJson(authEntityClass);

        if (AppConstants.GENERATE_LOGS)
            AppConstants.writeInFile(TAG + " ID:" + TransactionId + "; LINK:" + LinkName + "; Pulses:" + Integer.parseInt(outputQuantity) + "; Qty:" + fillqty);

        String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BS_BLE_BTFive.this).PersonEmail;
        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BS_BLE_BTFive.this) + ":" + userEmail + ":" + "TransactionComplete" + AppConstants.LANG_PARAM);


        HashMap<String, String> imap = new HashMap<>();
        imap.put("jsonData", jsonData);
        imap.put("authString", authString);
        imap.put("sqliteId", sqliteID + "");

        if (Pulses > 0 || fillqty > 0) {
            //in progress (transaction recently started, no new information): Transaction ongoing = 8  --non zero qty
            CommonUtils.upgradeTransactionStatusToSqlite(TransactionId, "8", BS_BLE_BTFive.this);
            int rowsAffected = controller.updateTransactions(imap);
            System.out.println("rowsAffected-" + rowsAffected);
            if (rowsAffected == 0) {
                sqliteID = controller.insertTransactions(imap);
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " <Transaction saved in local DB. LocalTxnId: " + sqliteID + "; LINK: " + LinkName + ">");
            }
        }
    }

    private String addStoredQtyToCurrentQty(String outputQuantity) {
        String newQty = outputQuantity;
        try {

            if (BTConstants.IS_RELAY_ON_AFTER_RECONNECT_5) {
                SharedPreferences sharedPrefLastQty = this.getSharedPreferences("LastQuantity_BT5", Context.MODE_PRIVATE);
                long storedPulsesCount = sharedPrefLastQty.getLong("Last_Quantity", 0);

                long quantity = Integer.parseInt(outputQuantity);

                long add_count = storedPulsesCount + quantity;

                outputQuantity = Long.toString(add_count);

                newQty = outputQuantity;
            }
        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " addStoredQtyToCurrentQty Exception:" + ex.getMessage());
        }
        return newQty;
    }

    private void reachMaxLimit() {
        //if quantity reach max limit
        if (minFuelLimit > 0 && fillqty >= minFuelLimit && !isTxnLimitReached) {
            isTxnLimitReached = true;
            Log.i(TAG, "Auto Stop Hit>> You reached MAX fuel limit.");
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " Auto Stop Hit>> " + LimitReachedMessage);
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
                    CommonUtils.upgradeTransactionStatusToSqlite(TransactionId, "4", BS_BLE_BTFive.this);
                    Log.i(TAG, " PumpOnTime Hit>>" + stopCount);
                    stopCount = 0;
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + " PumpOnTime Hit.");
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
                    Log.i(TAG, " PumpOffTime Hit>>" + stopCount);
                    stopCount = 0;
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + " PumpOffTime Hit.");
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
            if (RelayStatus && !BT_BLE_Constants.CURRENT_COMMAND_LINK_FIVE.contains(BTConstants.RELAY_OFF_COMMAND)) {
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
    }

    private void fdCheckCommand() {
        try {
            //Execute FD_check Command
            if (IsThisBTTrnx) {
                Response = "";
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " Sending FD_check command to Link: " + LinkName);
                mBluetoothLeService.writeCustomCharacteristic(BTConstants.FD_CHECK_COMMAND);
            }
        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " FD_check command Exception:>>" + ex.getMessage());
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEServiceCodeFive.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEServiceCodeFive.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEServiceCodeFive.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEServiceCodeFive.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void checkBTLinkStatus(String nextAction) {
        try {
            new CountDownTimer(10000, 2000) {
                public void onTick(long millisUntilFinished) {
                    if (BT_BLE_Constants.BT_BLE_STATUS_STR_FIVE.equalsIgnoreCase("Connected") && (BT_BLE_Constants.IS_LINK_FIVE_NOTIFY_ENABLED)) {
                        BT_BLE_Constants.IS_LINK_FIVE_NOTIFY_ENABLED = false;
                        isConnected = true;
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " Link is connected.");
                        if (nextAction.equalsIgnoreCase("last1")) {
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //infoCommand();
                                    AppConstants.TRANSACTION_FAILED_COUNT_5 = 0;
                                    AppConstants.IS_FIRST_COMMAND_SUCCESS_FS5 = false;
                                    if (IsThisBTTrnx && BT_BLE_Constants.IS_NEW_VERSION_LINK_FIVE) {
                                        last1Command();
                                    } else {
                                        transactionIdCommand(TransactionId);
                                    }
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
                            AppConstants.writeInFile(TAG + " Checking Connection Status...");
                    }
                }

                public void onFinish() {
                    if (BT_BLE_Constants.BT_BLE_STATUS_STR_FIVE.equalsIgnoreCase("Connected")) {
                        isConnected = true;
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " Link is connected.");
                        if (nextAction.equalsIgnoreCase("info")) {
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //infoCommand();
                                    AppConstants.TRANSACTION_FAILED_COUNT_5 = 0;
                                    AppConstants.IS_FIRST_COMMAND_SUCCESS_FS5 = false;
                                    if (IsThisBTTrnx && BT_BLE_Constants.IS_NEW_VERSION_LINK_FIVE) {
                                        last1Command();
                                    } else {
                                        transactionIdCommand(TransactionId);
                                    }
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
                        if (nextAction.equalsIgnoreCase("last1")) { // Terminate BT Transaction
                            terminateBTTransaction(); //UDPFunctionalityAfterBTFailure();
                        } else if (nextAction.equalsIgnoreCase("relay")) { // Terminate BT Txn After Interruption
                            terminateBTTxnAfterInterruption();
                        }
                    }
                }
            }.start();
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " checkBTLinkStatus Exception:>>" + e.getMessage());
            if (nextAction.equalsIgnoreCase("last1")) { // Terminate BT Transaction
                terminateBTTransaction();
            } else if (nextAction.equalsIgnoreCase("relay")) { // Terminate BT Txn After Interruption
                terminateBTTxnAfterInterruption();
            }
        }
    }

    private void terminateBTTransaction() {
        try {
            IsThisBTTrnx = false;
            CommonUtils.upgradeTransactionStatusToSqlite(TransactionId, "6", BS_BLE_BTFive.this);
            Log.i(TAG, " Link not connected. Please try again!");
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " Link not connected.");
            AppConstants.TRANSACTION_FAILED_COUNT_5++;
            AppConstants.IS_TRANSACTION_FAILED_5 = true;
            stopTransaction(true, true); // terminateBTTransaction
            this.stopSelf();
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " Exception in terminateBTTransaction: " + e.getMessage());
        }
    }

    private void terminateBTTxnAfterInterruption() {
        try {
            IsThisBTTrnx = false;
            if (Pulses > 0 || fillqty > 0) {
                if (isOnlineTxn) {
                    CommonUtils.upgradeTransactionStatusToSqlite(TransactionId, "10", BS_BLE_BTFive.this);
                } else {
                    offlineController.updateOfflineTransactionStatus(sqlite_id + "", "10");
                }
            } else {
                CommonUtils.upgradeTransactionStatusToSqlite(TransactionId, "6", BS_BLE_BTFive.this);
            }
            Log.i(TAG, " Link not connected. Please try again!");
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " Link not connected.");
            BTConstants.IS_RECONNECT_CALLED_5 = false;
            AppConstants.TRANSACTION_FAILED_COUNT_5++;
            AppConstants.IS_TRANSACTION_FAILED_5 = true;
            stopTransaction(true, true); // terminateBTTxnAfterInterruption
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " terminateBTTxnAfterInterruption Exception:>>" + e.getMessage());
        }
    }

    //region Info Command
    /*private void infoCommand() {
        try {
            AppConstants.TRANSACTION_FAILED_COUNT_5 = 0;
            AppConstants.IS_FIRST_COMMAND_SUCCESS_FS5 = false;
            //Execute info command
            Response = "";
            if (IsThisBTTrnx) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " Sending Info command to Link: " + LinkName);
                mBluetoothLeService.writeCustomCharacteristic(BTConstants.INFO_COMMAND);
            }

            new CountDownTimer(5000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long attempt = (5 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (BT_BLE_Constants.CURRENT_COMMAND_LINK_FIVE.equalsIgnoreCase(BTConstants.INFO_COMMAND) && !Response.equalsIgnoreCase("")) {
                            //Info command success.
                            Log.i(TAG, " InfoCommand Response success 1:>>" + Response);

                            if (!TransactionId.isEmpty()) {
                                if (Response.contains("mac_address")) {
                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(TAG + " Checking Info command response. Response: true");
                                    parseInfoCommandResponse(Response);
                                    Response = "";
                                } else {
                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(TAG + " Checking Info command response. Response:>>" + Response.trim());
                                    parseInfoCommandResponseForLast10txtn(Response.trim()); // parse last 10 Txtn
                                }
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        AppConstants.IS_FIRST_COMMAND_SUCCESS_FS5 = true;
                                        if (IsThisBTTrnx && BT_BLE_Constants.IS_NEW_VERSION_LINK_FIVE) {
                                            last1Command();
                                        } else {
                                            transactionIdCommand(TransactionId);
                                        }
                                    }
                                }, 1000);
                            } else {
                                Log.i(TAG, " TransactionId is empty.");
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + " TransactionId is empty.");
                                stopTransaction(false, true); // TransactionId is empty in infoCommand
                            }
                            cancel();
                        } else {
                            Log.i(TAG, " Waiting for infoCommand Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " Checking Info command response. Response: false");
                        }
                    }
                }

                public void onFinish() {
                    if (BT_BLE_Constants.CURRENT_COMMAND_LINK_FIVE.equalsIgnoreCase(BTConstants.INFO_COMMAND) && !Response.equalsIgnoreCase("")) {
                        //Info command success.
                        Log.i(TAG, " InfoCommand Response success 2:>>" + Response);

                        if (!TransactionId.isEmpty()) {
                            if (Response.contains("mac_address")) {
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + " Checking Info command response. Response: true");
                                parseInfoCommandResponse(Response);
                                Response = "";
                            } else {
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + " Checking Info command response. Response:>>" + Response.trim());
                                parseInfoCommandResponseForLast10txtn(Response.trim()); // parse last 10 Txtn
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    AppConstants.IS_FIRST_COMMAND_SUCCESS_FS5 = true;
                                    if (IsThisBTTrnx && BT_BLE_Constants.IS_NEW_VERSION_LINK_FIVE) {
                                        last1Command();
                                    } else {
                                        transactionIdCommand(TransactionId);
                                    }
                                }
                            }, 1000);
                        } else {
                            Log.i(TAG, " TransactionId is empty.");
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " TransactionId is empty.");
                            stopTransaction(false, true); // TransactionId is empty in infoCommand onFinish
                        }
                    } else {
                        if (infoCommandAttempt > 0) {
                            //UpgradeTransaction Status info command fail.
                            CommonUtils.upgradeTransactionStatusToSqlite(TransactionId, "6", BS_BLE_BTFive.this);
                            Log.i(TAG, " Failed to get infoCommand Response:>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " Checking Info command response. Response: false");
                            AppConstants.TRANSACTION_FAILED_COUNT_5++;
                            AppConstants.IS_TRANSACTION_FAILED_5 = true;
                            stopTransaction(true, true); // Info command Response: false
                        } else {
                            infoCommandAttempt++;
                            if (BT_BLE_Constants.BT_BLE_STATUS_STR_FIVE.equalsIgnoreCase("Connected")) {
                                infoCommand(); // Retried one more time after failed to receive response from info command
                            } else {
                                linkReconnectionAttempt();
                                waitForReconnectToLink();
                            }
                        }
                    }
                }
            }.start();
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " infoCommand Exception:>>" + e.getMessage());
            stopTransaction(true, true); // Info command Exception
        }
    }*/

    /*public void waitForReconnectToLink() {
        try {
            new CountDownTimer(10000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long attempt = (10 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (BT_BLE_Constants.BT_BLE_STATUS_STR_FIVE.equalsIgnoreCase("Connected")) {
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " Connected to Link: " + LinkName);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    infoCommand(); // Retried one more time after failed to receive response from info command
                                }
                            }, 500);
                            cancel();
                        } else {
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " Waiting for Reconnect to Link: " + LinkName);
                        }
                    }
                }

                public void onFinish() {
                    if (BT_BLE_Constants.BT_BLE_STATUS_STR_FIVE.equalsIgnoreCase("Connected")) {
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " Connected to Link: " + LinkName);
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
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " waitForReconnectToLink Exception:>>" + e.getMessage());
            terminateBTTransaction();
        }
    }*/
    //endregion

    //region Last1 Command
    private void last1Command() {
        try {
            //Execute last1 Command
            Response = "";

            if (IsThisBTTrnx) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " Sending last1 command to Link: " + LinkName);
                mBluetoothLeService.writeCustomCharacteristic(BTConstants.LAST1_COMMAND);
            }

            new CountDownTimer(4000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long attempt = (4 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (BT_BLE_Constants.CURRENT_COMMAND_LINK_FIVE.contains(BTConstants.LAST1_COMMAND) && Response.contains("records")) {
                            //last1 command success.
                            AppConstants.IS_FIRST_COMMAND_SUCCESS_FS5 = true;
                            Log.i(TAG, " last1 Command Response success 1:>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " Checking last1 command response. Response: true");
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
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " Checking last1 command response. Response: false");
                        }
                    }
                }

                public void onFinish() {
                    if (BT_BLE_Constants.CURRENT_COMMAND_LINK_FIVE.contains(BTConstants.LAST1_COMMAND) && Response.contains("records")) {
                        //last1 command success.
                        AppConstants.IS_FIRST_COMMAND_SUCCESS_FS5 = true;
                        Log.i(TAG, " last1 Command Response success 2:>>" + Response);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " Checking last1 command response. Response: true");
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
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " last1 Command Exception:>>" + e.getMessage());
            transactionIdCommand(TransactionId);
        }
    }
    //endregion

    //region TransactionId (TDV) Command
    private void transactionIdCommand(String transactionId) {
        try {
            //Execute transactionId Command
            Response = "";

            String transaction_id_cmd = BTConstants.TRANSACTION_ID_COMMAND; //LK_COMM=txtnid:

            if (BT_BLE_Constants.IS_NEW_VERSION_LINK_FIVE) {
                TransactionDateWithFormat = BTConstants.parseDateForNewVersion(TransactionDateWithFormat);
                transaction_id_cmd = transaction_id_cmd.replace("txtnid:", ""); // For New version LK_COMM=T:XXXXX;D:XXXXX;V:XXXXXXXX;
                transaction_id_cmd = transaction_id_cmd + "T:" + transactionId + ";D:" + TransactionDateWithFormat + ";V:" + VehicleNumber + ";";
            } else {
                transaction_id_cmd = transaction_id_cmd + transactionId;
            }

            if (IsThisBTTrnx) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " Sending transactionId command to Link: " + LinkName);
                mBluetoothLeService.writeCustomCharacteristic(transaction_id_cmd);
            }

            Thread.sleep(500);
            new CountDownTimer(4000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long attempt = (4 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (BT_BLE_Constants.CURRENT_COMMAND_LINK_FIVE.contains(transactionId) && Response.contains(transactionId)) {
                            //transactionId command success.
                            AppConstants.IS_FIRST_COMMAND_SUCCESS_FS5 = true;
                            Log.i(TAG, " transactionId Command Response success 1:>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " Checking transactionId command response. Response:>>" + Response.trim());
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    relayOnCommand(false); //RelayOn
                                }
                            }, 1000);
                            cancel();
                        } else {
                            Log.i(TAG, " Waiting for transactionId Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " Checking transactionId command response. Response: false");
                        }
                    }
                }

                public void onFinish() {
                    if (BT_BLE_Constants.CURRENT_COMMAND_LINK_FIVE.contains(transactionId) && Response.contains(transactionId)) {
                        //transactionId command success.
                        AppConstants.IS_FIRST_COMMAND_SUCCESS_FS5 = true;
                        Log.i(TAG, " transactionId Command Response success 2:>>" + Response);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " Checking transactionId command response. Response:>>" + Response.trim());
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                relayOnCommand(false); //RelayOn
                            }
                        }, 1000);
                    } else {
                        //UpgradeTransaction Status Transactionid command fail.
                        CommonUtils.upgradeTransactionStatusToSqlite(transactionId, "6", BS_BLE_BTFive.this);
                        Log.i(TAG, " Failed to get transactionId Command Response:>>" + Response);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " Checking transactionId command response. Response: false");
                        AppConstants.TRANSACTION_FAILED_COUNT_5++;
                        AppConstants.IS_TRANSACTION_FAILED_5 = true;
                        stopTransaction(true, true); // transactionId command Response: false
                    }
                }
            }.start();
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " transactionId Command Exception:>>" + e.getMessage());
            stopTransaction(true, true); // transactionId Command Exception
        }
    }
    //endregion

    //region Relay ON Command
    private void relayOnCommand(boolean isAfterReconnect) {
        try {
            if (isAfterReconnect) {
                BTConstants.IS_RECONNECT_CALLED_5 = false;
            }
            //Execute relayOn Command
            Response = "";

            if (IsThisBTTrnx) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " Sending relayOn command to Link: " + LinkName);
                mBluetoothLeService.writeCustomCharacteristic(BTConstants.RELAY_ON_COMMAND);
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
                            BTConstants.IS_RELAY_ON_AFTER_RECONNECT_5 = isAfterReconnect;
                            //relayOn command success.
                            Log.i(TAG, " relayOn Command Response success 1:>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " Checking relayOn command response. Response: ON");
                            cancel();
                        } else {
                            Log.i(TAG, " Waiting for relayOn Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " Checking relayOn command response. Response: false");
                        }
                    }
                }

                public void onFinish() {
                    if (RelayStatus) {
                        BTConstants.IS_RELAY_ON_AFTER_RECONNECT_5 = isAfterReconnect;
                        //relayOn command success.
                        Log.i(TAG, " relayOn Command Response success 2:>>" + Response);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " Checking relayOn command response. Response: ON");
                    } else {
                        //UpgradeTransaction Status RelayON command fail.
                        if (isAfterReconnect && (Pulses > 0 || fillqty > 0)) {
                            if (isOnlineTxn) {
                                CommonUtils.upgradeTransactionStatusToSqlite(TransactionId, "10", BS_BLE_BTFive.this);
                            } else {
                                offlineController.updateOfflineTransactionStatus(sqlite_id + "", "10");
                            }
                        } else {
                            CommonUtils.upgradeTransactionStatusToSqlite(TransactionId, "6", BS_BLE_BTFive.this);
                        }
                        Log.i(TAG, " Failed to get relayOn Command Response:>>" + Response);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " Checking relayOn command response. Response: false");
                        relayOffCommand(); //RelayOff
                    }
                }
            }.start();
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " relayOn Command Exception:>>" + e.getMessage());
            relayOffCommand(); //RelayOff
        }
    }
    //endregion

    //region Relay OFF Command
    private void relayOffCommand() {
        try {
            //Execute relayOff Command
            Response = "";
            //relayOffAttemptCount++;
            if (IsThisBTTrnx) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " Sending relayOff command to Link: " + LinkName);
                mBluetoothLeService.writeCustomCharacteristic(BTConstants.RELAY_OFF_COMMAND);
            }

            new CountDownTimer(4000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long attempt = (4 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (!RelayStatus) {
                            //relayOff command success.
                            Log.i(TAG, " relayOff Command Response success 1:>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " Checking relayOff command response. Response:>>" + Response.trim());
                            if (!AppConstants.IS_RELAY_ON_FS5) {
                                infoCommand();
                            }
                            cancel();
                        } else {
                            Log.i(TAG, " Waiting for relayOff Command Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " Checking relayOff command response. Response: false");
                        }
                    }
                }

                public void onFinish() {
                    if (!RelayStatus) {
                        Log.i(TAG, " relayOff Command Response success 2:>>" + Response);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " Checking relayOff command response. Response:>>" + Response.trim());
                    } else {
                        Log.i(TAG, " Failed to get relayOff Command Response:>>" + Response);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " Checking relayOff command response. Response: false");
                        if (BTConstants.IS_RELAY_ON_AFTER_RECONNECT_5) {
                            if (Pulses > 0 || fillqty > 0) {
                                if (isOnlineTxn) {
                                    CommonUtils.upgradeTransactionStatusToSqlite(TransactionId, "10", BS_BLE_BTFive.this);
                                } else {
                                    offlineController.updateOfflineTransactionStatus(sqlite_id + "", "10");
                                }
                            }
                        }
                        stopTransaction(true, true);
                    }
                    if (!AppConstants.IS_RELAY_ON_FS5) {
                        //transactionCompleteFunction();
                        infoCommand();
                    }
                }
            }.start();
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " relayOff Command Exception:>>" + e.getMessage());
            if (!AppConstants.IS_RELAY_ON_FS5) {
                //transactionCompleteFunction();
                infoCommand();
            }
        }
    }
    //endregion

    private void infoCommand() {
        try {
            //Execute info command
            Response = "";
            if (IsThisBTTrnx) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " Sending Info command to Link: " + LinkName);
                mBluetoothLeService.writeCustomCharacteristic(BTConstants.INFO_COMMAND);
            }

            new CountDownTimer(5000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long attempt = (5 - (millisUntilFinished / 1000));
                    if (attempt > 0) {
                        if (BT_BLE_Constants.CURRENT_COMMAND_LINK_FIVE.equalsIgnoreCase(BTConstants.INFO_COMMAND) && !Response.equalsIgnoreCase("")) {
                            //Info command success.
                            Log.i(TAG, " InfoCommand Response success 1:>>" + Response);
                            if (Response.contains("mac_address")) {
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + " Checking Info command response. Response: true");
                                parseInfoCommandResponse(Response);
                                Response = "";
                            } else {
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + " Checking Info command response. Response:>>" + Response.trim());
                                parseInfoCommandResponseForLast10txtn(Response.trim()); // parse last 10 Txtn
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    transactionCompleteFunction();
                                }
                            }, 1000);
                            cancel();
                        } else {
                            Log.i(TAG, " Waiting for infoCommand Response: " + millisUntilFinished / 1000 + " Response>>" + Response);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " Checking Info command response. Response: false");
                        }
                    }
                }

                public void onFinish() {
                    if (BT_BLE_Constants.CURRENT_COMMAND_LINK_FIVE.equalsIgnoreCase(BTConstants.INFO_COMMAND) && !Response.equalsIgnoreCase("")) {
                        //Info command success.
                        Log.i(TAG, " InfoCommand Response success 2:>>" + Response);
                        if (Response.contains("mac_address")) {
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " Checking Info command response. Response: true");
                            parseInfoCommandResponse(Response);
                            Response = "";
                        } else {
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " Checking Info command response. Response:>>" + Response.trim());
                            parseInfoCommandResponseForLast10txtn(Response.trim()); // parse last 10 Txtn
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                transactionCompleteFunction();
                            }
                        }, 1000);
                    } else {
                        Log.i(TAG, " Failed to get infoCommand Response:>>" + Response);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " Checking Info command response. Response: false");
                        stopTransaction(true, true);
                    }
                }
            }.start();
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " infoCommand Exception:>>" + e.getMessage());
            transactionCompleteFunction();
        }
    }

    private void transactionCompleteFunction() {

        if (isOnlineTxn) {
            if (BTConstants.BT5_REPLACEABLE_WIFI_NAME == null) {
                BTConstants.BT5_REPLACEABLE_WIFI_NAME = "";
            }
            //BTLink Rename functionality
            if (BTConstants.BT5_NEED_RENAME && !BTConstants.BT5_REPLACEABLE_WIFI_NAME.isEmpty()) {
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
        if (CommonUtils.checkBTVersionCompatibility(versionNumberOfLinkFive, BTConstants.SUPPORTED_LINK_VERSION_FOR_P_TYPE)) { // Set P_Type command supported from this version onwards
            getPulserTypeCommand(); //pTypeCommand();
        } else {
            closeTransaction(false); // proceedToPostTransactionCommands
        }
    }

    //region Rename Command
    private void renameCommand() {
        try {
            //Execute rename Command
            Response = "";

            if (IsThisBTTrnx) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " Sending rename command to Link: " + LinkName + " (New Name: " + BTConstants.BT5_REPLACEABLE_WIFI_NAME + ")");
                mBluetoothLeService.writeCustomCharacteristic(BTConstants.RENAME_COMMAND + BTConstants.BT5_REPLACEABLE_WIFI_NAME);
            }

            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BS_BLE_BTFive.this).PersonEmail;
            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(this) + ":" + userEmail + ":" + "SetHoseNameReplacedFlag" + AppConstants.LANG_PARAM);

            RenameHose rhose = new RenameHose();
            rhose.SiteId = BTConstants.BT5SITE_ID;
            rhose.HoseId = BTConstants.BT5HOSE_ID;
            rhose.IsHoseNameReplaced = "Y";

            Gson gson = new Gson();
            String jsonData = gson.toJson(rhose);

            storeIsRenameFlag(this, BTConstants.BT5_NEED_RENAME, jsonData, authString);

            Thread.sleep(1000);
            proceedToPostTransactionCommands();

        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " rename Command Exception:>>" + e.getMessage());
            proceedToPostTransactionCommands();
        }
    }
    //endregion

    //region P_Type Command
    /*private void pTypeCommand() {
        boolean isSetPTypeCommandSent = false;
        try {
            if (IsResetSwitchTimeBounce != null) {
                if (IsResetSwitchTimeBounce.trim().equalsIgnoreCase("1") && !PulserTimingAdjust.isEmpty() && Arrays.asList(BTConstants.P_TYPES).contains(PulserTimingAdjust) && !CommonUtils.CheckDataStoredInSharedPref(BS_BLE_BTFive.this, "storeSwitchTimeBounceFlag5")) {
                    //Execute p_type Command
                    Response = "";
                    IsAnyPostTxnCommandExecuted = true;

                    if (IsThisBTTrnx) {
                        isSetPTypeCommandSent = true;
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " Sending set p_type command to Link: " + LinkName);
                        mBluetoothLeService.writeCustomCharacteristic(BTConstants.P_TYPE_COMMAND + PulserTimingAdjust);
                    }

                    new CountDownTimer(4000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            long attempt = (4 - (millisUntilFinished / 1000));
                            if (attempt > 0) {
                                if (BT_BLE_Constants.CURRENT_COMMAND_LINK_FIVE.contains(BTConstants.P_TYPE_COMMAND) && Response.contains("pulser_type")) {
                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(TAG + " Checking set p_type command response:>> " + Response);
                                    updateSwitchTimeBounceForLink();
                                    closeTransaction(true); // set p_type command success
                                    cancel();
                                } else {
                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(TAG + " Checking set p_type command response. Response: false");
                                }
                            }
                        }

                        public void onFinish() {
                            if (BT_BLE_Constants.CURRENT_COMMAND_LINK_FIVE.contains(BTConstants.P_TYPE_COMMAND) && Response.contains("pulser_type")) {
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + " Checking set p_type command response:>> " + Response);
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
                AppConstants.writeInFile(TAG + " Set P_Type Command Exception:>>" + e.getMessage());
            if (isSetPTypeCommandSent) {
                closeTransaction(true); // Set P_Type Command Exception
            } else {
                getPulserTypeCommand();
            }
        }
    }*/
    //endregion

    //region Get P_Type Command
    private void getPulserTypeCommand() {
        try {
            if (GetPulserTypeFromLINK != null) {
                if (GetPulserTypeFromLINK.trim().equalsIgnoreCase("True") && !CommonUtils.CheckDataStoredInSharedPref(BS_BLE_BTFive.this, "UpdatePulserType5")) {
                    //Execute get p_type Command (to get the pulser type from LINK)
                    Response = "";
                    IsAnyPostTxnCommandExecuted = true;

                    if (IsThisBTTrnx) {
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " Sending get p_type command to Link: " + LinkName);
                        mBluetoothLeService.writeCustomCharacteristic(BTConstants.GET_P_TYPE_COMMAND);
                    }

                    new CountDownTimer(4000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            long attempt = (4 - (millisUntilFinished / 1000));
                            if (attempt > 0) {
                                if (BT_BLE_Constants.CURRENT_COMMAND_LINK_FIVE.contains(BTConstants.GET_P_TYPE_COMMAND) && Response.contains("pulser_type")) {
                                    parseGetPulserTypeCommandResponse(Response.trim());
                                    if (BTConstants.IS_BT_SPP_TO_BLE_5) {
                                        rebootCommand();
                                    } else {
                                        closeTransaction(true); // get p_type command success
                                    }
                                    cancel();
                                }
                            }
                        }

                        public void onFinish() {
                            if (BT_BLE_Constants.CURRENT_COMMAND_LINK_FIVE.contains(BTConstants.GET_P_TYPE_COMMAND) && Response.contains("pulser_type")) {
                                parseGetPulserTypeCommandResponse(Response.trim());
                            }
                            if (BTConstants.IS_BT_SPP_TO_BLE_5) {
                                rebootCommand();
                            } else {
                                closeTransaction(true); // get p_type command finish
                            }
                        }
                    }.start();
                } else {
                    if (BTConstants.IS_BT_SPP_TO_BLE_5) {
                        rebootCommand();
                    } else {
                        closeTransaction(true); // after checking GetPulserTypeFromLINK
                    }
                }
            } else {
                if (BTConstants.IS_BT_SPP_TO_BLE_5) {
                    rebootCommand();
                } else {
                    closeTransaction(true); // GetPulserTypeFromLINK flag is null
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " Get P_Type Command (to get the pulser type from LINK) Exception:>>" + e.getMessage());
            if (BTConstants.IS_BT_SPP_TO_BLE_5) {
                rebootCommand();
            } else {
                closeTransaction(true); // Get P_Type Command Exception
            }
        }
    }
    //endregion

    //region Reboot Command
    private void rebootCommand() {
        try {
            //Execute Reboot Command
            Response = "";

            if (IsThisBTTrnx) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " Sending reboot command to Link: " + LinkName);
                mBluetoothLeService.writeCustomCharacteristic(BTConstants.REBOOT_COMMAND);
            }

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    BTConstants.IS_BT_SPP_TO_BLE_5 = false;
                    closeTransaction(true); // after reboot command
                }
            }, 1000);
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " Reboot Command Exception:>>" + e.getMessage());
            BTConstants.IS_BT_SPP_TO_BLE_5 = false;
            closeTransaction(true); // Reboot Command Exception
        }
    }
    //endregion

    private void stopTransaction(boolean startBackgroundServices, boolean isTransactionCompleted) {
        try {
            AppConstants.IS_TRANSACTION_COMPLETED_5 = false;
            BTConstants.IS_RELAY_ON_AFTER_RECONNECT_5 = false;
            AppConstants.clearSharedPrefByName(BS_BLE_BTFive.this, "LastQuantity_BT5");
            CommonUtils.addRemoveCurrentTransactionList(false, TransactionId);
            Constants.FS_5_STATUS = "FREE";
            Constants.FS_5_PULSE = "00";
            countBeforeReconnectRelay5 = 0;
            AppConstants.GO_BUTTON_ALREADY_CLICKED = false;
            AppConstants.IS_FIRST_COMMAND_SUCCESS_FS5 = false;
            //BTConstants.SwitchedBTToUDP5 = false;
            //DisableWifiConnection();
            cancelTimer();
            IsAnyPostTxnCommandExecuted = true;
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " Transaction stopped.");
            if (isTransactionCompleted) {
                closeTransaction(startBackgroundServices); // from stopTransaction
            } else if (startBackgroundServices) {
                postTransactionBackgroundTasks(false);
            }
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " stopTransaction Exception:>>" + e.getMessage());
            closeTransaction(startBackgroundServices); // from stopTransaction exception
        }
    }

    private void closeTransaction(boolean startBackgroundServices) {
        clearEditTextFields();
        AppConstants.IS_TRANSACTION_COMPLETED_5 = true;
        try {
            try {
                unbindService(mServiceConnection);
                unregisterReceiver(mGattUpdateReceiver);
            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " <Exception occurred while unregistering receiver: " + e.getMessage() + ">");
            }
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " Transaction Completed. \n==============================================================================");
            if (startBackgroundServices) {
                postTransactionBackgroundTasks(true);
            }
            this.stopSelf();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " closeTransaction Exception:>>" + e.getMessage());
        }
    }

    private void insertInitialTransactionToSqlite() {
        String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BS_BLE_BTFive.this).PersonEmail;
        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BS_BLE_BTFive.this) + ":" + userEmail + ":" + "TransactionComplete" + AppConstants.LANG_PARAM);

        HashMap<String, String> imap = new HashMap<>();
        imap.put("jsonData", "");
        imap.put("authString", authString);

        sqliteID = controller.insertTransactions(imap);
        if (AppConstants.GENERATE_LOGS)
            AppConstants.writeInFile(TAG + " <Transaction saved in local DB. LocalTxnId: " + sqliteID + "; LINK: " + LinkName + ">");
        CommonUtils.addRemoveCurrentTransactionList(true, TransactionId);//Add transaction Id to list
    }

    public void storeIsRenameFlag(Context context, boolean flag, String jsonData, String authString) {
        SharedPreferences pref;

        SharedPreferences.Editor editor;
        pref = context.getSharedPreferences("storeIsRenameFlagFS5", 0);
        editor = pref.edit();

        // Storing
        editor.putBoolean("flag", flag);
        editor.putString("jsonData", jsonData);
        editor.putString("authString", authString);

        // commit changes
        editor.commit();
    }

    private void parseInfoCommandResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);

            JSONObject versionJsonObj = jsonObject.getJSONObject("version");
            String version = versionJsonObj.getString("version");
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " LINK Version >> " + version);
            storeUpgradeFSVersion(BS_BLE_BTFive.this, AppConstants.UP_HOSE_ID_FS5, version);
            versionNumberOfLinkFive = CommonUtils.getVersionFromLink(version);

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " Exception in parseInfoCommandResponse. response>> " + response + "; Exception>>" + e.getMessage());
        }
    }

    public void storeUpgradeFSVersion(Context context, String hoseid, String fsversion) {
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.PREF_FS_UPGRADE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("hoseid_bt5", hoseid);
        editor.putString("fsversion_bt5", fsversion);
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
                                        saveLastBTTransactionInLocalDB(txn_id, pulse);
                                    }
                                }
                            } catch (Exception e) {
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + " Last10 txtn parsing exception:>>" + e.getMessage());
                            }
                        } else {

                            if (res.contains("version:")) {
                                version = res.substring(res.indexOf(":") + 1).trim();
                            }
                            if (!version.isEmpty()) {
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + " LINK Version >> " + version);
                                storeUpgradeFSVersion(BS_BLE_BTFive.this, AppConstants.UP_HOSE_ID_FS5, version);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " Exception in parseInfoCommandResponseForLast10txtn. response>> " + response + "; Exception>>" + e.getMessage());
        }
    }

    private String removeLastChar(String s) {
        if (s.isEmpty())
            return "";

        return s.substring(0, s.length() - 1);
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
            authEntityClass.AppInfo = " Version:" + CommonUtils.getVersionCode(BS_BLE_BTFive.this) + " " + AppConstants.getDeviceName() + " Android " + android.os.Build.VERSION.RELEASE + " " + "--Last Transaction--";
            authEntityClass.TransactionFrom = "A";
            authEntityClass.Pulses = Integer.parseInt(counts);
            authEntityClass.IsFuelingStop = IsFuelingStop;
            authEntityClass.IsLastTransaction = "1";

            Gson gson = new Gson();
            String jsonData = gson.toJson(authEntityClass);

            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " <Last Transaction saved in local DB. LastTXNid:" + txnId + "; LINK:" + LinkName + "; Pulses:" + Integer.parseInt(counts) + "; Qty:" + Lastqty + ">");

            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BS_BLE_BTFive.this).PersonEmail;
            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BS_BLE_BTFive.this) + ":" + userEmail + ":" + "TransactionComplete" + AppConstants.LANG_PARAM);

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
                AppConstants.writeInFile(TAG + " saveLastBTTransactionInLocalDB Exception: " + e.getMessage());
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
                AppConstants.writeInFile(TAG + " Exception in parseLast1CommandResponse. response>> " + response + "; Exception>>" + e.getMessage());
        }
    }

    private void parseGetPulserTypeCommandResponse(String response) {
        try {
            String pulserType;

            if (response.contains("pulser_type")) {
                JSONObject jsonObj = new JSONObject(response);
                pulserType = jsonObj.getString("pulser_type");

                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " Pulser Type from Link >> " + pulserType);
                if (!pulserType.isEmpty() && Arrays.asList(BTConstants.P_TYPES).contains(pulserType)) {
                    // Create object and save data to upload
                    String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BS_BLE_BTFive.this).PersonEmail;

                    String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BS_BLE_BTFive.this) + ":" + userEmail + ":" + "UpdatePulserTypeOfLINK" + AppConstants.LANG_PARAM);

                    UpdatePulserTypeOfLINK_entity updatePulserTypeOfLINK = new UpdatePulserTypeOfLINK_entity();
                    updatePulserTypeOfLINK.IMEIUDID = AppConstants.getIMEI(BS_BLE_BTFive.this);
                    updatePulserTypeOfLINK.Email = userEmail;
                    updatePulserTypeOfLINK.SiteId = BTConstants.BT5SITE_ID;
                    updatePulserTypeOfLINK.PulserType = pulserType;
                    updatePulserTypeOfLINK.DateTimeFromApp = AppConstants.currentDateFormat("MM/dd/yyyy HH:mm:ss");

                    Gson gson = new Gson();
                    String jsonData = gson.toJson(updatePulserTypeOfLINK);

                    storePulserTypeDetails(BS_BLE_BTFive.this, jsonData, authString);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " Exception in parseGetPulserTypeCommandResponse. response>> " + response + "; Exception>>" + e.getMessage());
        }
    }

    private void storePulserTypeDetails(Context context, String jsonData, String authString) {
        try {
            SharedPreferences pref;
            SharedPreferences.Editor editor;

            pref = context.getSharedPreferences("UpdatePulserType5", 0);
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

    private void linkReconnectionAttempt() {
        try {
            unbindService(mServiceConnection);
            unregisterReceiver(mGattUpdateReceiver);

            Intent gattServiceIntent = new Intent(this, BLEServiceCodeFive.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSwitchTimeBounceForLink() {
        try {
            String userEmail = CommonUtils.getCustomerDetails_backgroundServiceBT(BS_BLE_BTFive.this).PersonEmail;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BS_BLE_BTFive.this) + ":" + userEmail + ":" + "UpdateSwitchTimeBounceForLink" + AppConstants.LANG_PARAM);

            SwitchTimeBounce switchTimeBounce = new SwitchTimeBounce();
            switchTimeBounce.SiteId = BTConstants.BT5SITE_ID;
            switchTimeBounce.IsResetSwitchTimeBounce = "0";

            Gson gson = new Gson();
            String jsonData = gson.toJson(switchTimeBounce);

            storeSwitchTimeBounceFlag(BS_BLE_BTFive.this, jsonData, authString);

        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " updateSwitchTimeBounceForLink Exception: " + ex.getMessage());
        }
    }

    public void storeSwitchTimeBounceFlag(Context context, String jsonData, String authString) {
        try {
            SharedPreferences pref;
            SharedPreferences.Editor editor;

            pref = context.getSharedPreferences("storeSwitchTimeBounceFlag5", 0);
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

    private void cancelTimer() {
        try {
            for (int i = 0; i < timerList_ReadPulseBT5.size(); i++) {
                timerList_ReadPulseBT5.get(i).cancel();
            }
            redpulseloop_on = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void postTransactionBackgroundTasks(boolean isTransactionCompleted) {
        try {
            if (isOnlineTxn) {
                if (!isTransactionCompleted) {
                    // Save upgrade details to cloud
                    SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_FS_UPGRADE, Context.MODE_PRIVATE);
                    String hoseid = sharedPref.getString("hoseid_bt5", "");
                    String fsversion = sharedPref.getString("fsversion_bt5", "");

                    UpgradeVersionEntity objEntityClass = new UpgradeVersionEntity();
                    objEntityClass.IMEIUDID = AppConstants.getIMEI(BS_BLE_BTFive.this);
                    objEntityClass.Email = CommonUtils.getCustomerDetails_backgroundServiceBT(BS_BLE_BTFive.this).PersonEmail;
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

                //boolean BSRunning = CommonUtils.checkServiceRunning(BS_BLE_BTFive.this, AppConstants.PACKAGE_BACKGROUND_SERVICE);
                //if (!BSRunning) {
                if (IsAnyPostTxnCommandExecuted) {
                    IsAnyPostTxnCommandExecuted = false;
                    startService(new Intent(this, BackgroundService.class));
                }
                //}
            }

            if (!isTransactionCompleted) {
                // Offline transaction data sync
                if (OfflineConstants.isOfflineAccess(BS_BLE_BTFive.this)) {
                    syncOfflineData();
                }
            }
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " postTransactionBackgroundTasks Exception: " + e.getMessage());
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
                response = serverHandler.PostTextData(BS_BLE_BTFive.this, AppConstants.WEB_URL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " UpgradeCurrentVersionWithUpgradableVersion Exception: " + ex.getMessage());
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
                    storeUpgradeFSVersion(BS_BLE_BTFive.this, "", "");
                }
            } catch (Exception e) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " UpgradeCurrentVersionWithUpgradableVersion onPostExecute Exception: " + e.getMessage());
            }
        }
    }

    private void syncOfflineData() {
        if (Constants.FS_1_STATUS.equalsIgnoreCase("FREE") && Constants.FS_2_STATUS.equalsIgnoreCase("FREE") && Constants.FS_3_STATUS.equalsIgnoreCase("FREE") && Constants.FS_4_STATUS.equalsIgnoreCase("FREE") && Constants.FS_5_STATUS.equalsIgnoreCase("FREE") && Constants.FS_6_STATUS.equalsIgnoreCase("FREE")) {

            if (isOnlineTxn) {
                try {
                    //sync offline transactions
                    String off_json = offlineController.getAllOfflineTransactionJSON(BS_BLE_BTFive.this);
                    JSONObject jsonObj = new JSONObject(off_json);
                    String offTransactionArray = jsonObj.getString("TransactionsModelsObj");
                    JSONArray jArray = new JSONArray(offTransactionArray);

                    if (jArray.length() > 0) {
                        startService(new Intent(BS_BLE_BTFive.this, OffTranzSyncService.class));
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
        Constants.VEHICLE_NUMBER_FS5 = "";
        Constants.ODO_METER_FS5 = 0;
        Constants.DEPARTMENT_NUMBER_FS5 = "";
        Constants.PERSONNEL_PIN_FS5 = "";
        Constants.OTHER_FS5 = "";
        Constants.VEHICLE_OTHER_FS5 = "";
        Constants.HOURS_FS5 = 0;
    }
}
