package com.TrakEngineering.FluidSecureHub;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.BTSPP.BTConstants;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTSPPMain;
import com.TrakEngineering.FluidSecureHub.BTSPP.BackgroundService_BTOne;
import com.TrakEngineering.FluidSecureHub.entity.AuthEntityClass;
import com.TrakEngineering.FluidSecureHub.entity.TrazComp;
import com.TrakEngineering.FluidSecureHub.offline.OfflineConstants;
import com.TrakEngineering.FluidSecureHub.server.ServerHandler;
import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.TrakEngineering.FluidSecureHub.server.ServerHandler.TEXT;

/**
 * Created by Administrator on 6/19/2017.
 */

public class AcceptServiceCall {

    private ConnectionDetector cd;
    public Activity activity;
    public String ServerCallName = "AuthorizationSequence";
    public String IsTestTransaction = "n";
    String IsOdoMeterRequire = "", IsDepartmentRequire = "", IsPersonnelPINRequire = "", IsOtherRequire = "", IsVehicleNumberRequire = "", IsStayOpenGate = "", IsGateHub = "";
    private static final String TAG = "AcceptServiceCall ";
    long stopAutoFuelSecondstemp = 0;
    long sqlite_id = 0;

    String HTTP_URL = "";
    String URL_GET_TXNID = HTTP_URL + "client?command=lasttxtnid";
    String URL_SET_TXNID = HTTP_URL + "config?command=txtnid";
    String URL_GET_PULSAR = HTTP_URL + "client?command=pulsar ";
    String URL_RECORD10_PULSAR = HTTP_URL + "client?command=record10";
    String URL_INFO = HTTP_URL + "client?command=info";
    String URL_RELAY = HTTP_URL + "config?command=relay";
    String PulserTimingAd = HTTP_URL + "config?command=pulsar";
    String URL_SET_PULSAR = HTTP_URL + "config?command=pulsar";
    String iot_version = "";
    Integer Pulses = 0;
    double minFuelLimit = 0, numPulseRatio = 0;
    String EMPTY_Val = "",IsFuelingStop = "0",IsLastTransaction = "1";
    DBController controller = new DBController(activity);
    ServerHandler serverHandler = new ServerHandler();
    private int BTConnectionCounter = 0;
    public String GateHUBTransactionId = "0";

    public void checkAllFields() {

        if (!activity.isFinishing() && !AppConstants.SERVER_CALL_IN_PROGRESS) {
               AppConstants.SERVER_CALL_IN_PROGRESS = true;
               AppConstants.AUTH_CALL_SUCCESS = false;
               AppConstants.IS_FIRST_TIME_USE = "False";
               Log.e(TAG,"Activity started");
               new ServerCall().execute();
        }else{
            Log.e(TAG,"Activity skip call..");
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "  ServerCall skip..");
        }
    }

    public class ServerCall extends AsyncTask<Void, Void, String> {

        //ProgressDialog pd;
        AlertDialog alertDialog;
        String resp = "";
        String pinNumber = "";
        String vehicleNumber = "";
        String DeptNumber = "";
        String accOther = "";
        String accVehOther = "";
        String CONNECTED_SSID = "";
        int accOdoMeter;
        int accHours;

        @Override
        protected void onPreExecute() {

            String s = activity.getResources().getString(R.string.PleaseWaitMessage);
            alertDialog = AlertDialogUtil.createAlertDialog(activity, s, true);
            alertDialog.show();

            Thread thread = new Thread() {
                @Override
                public void run() {
                    AlertDialogUtil.runAnimatedLoadingDots(activity, s, alertDialog, true);
                }
            };
            thread.start();

        }

        protected String doInBackground(Void... arg0) {

            try {

                SharedPreferences sharedPrefODO = activity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

                IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IS_DEPARTMENT_REQUIRE, "");
                IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IS_PERSONNEL_PIN_REQUIRE, "");
                IsOtherRequire = sharedPrefODO.getString(AppConstants.IS_OTHER_REQUIRE, "");
                IsVehicleNumberRequire = sharedPrefODO.getString(AppConstants.IS_VEHICLE_NUMBER_REQUIRE, "");
                IsGateHub = sharedPrefODO.getString(AppConstants.IS_GATE_HUB, "false");

                SharedPreferences sharedPrefGatehub = activity.getSharedPreferences(Constants.PREF_COLUMN_GATE_HUB, Context.MODE_PRIVATE);
                IsGateHub = sharedPrefGatehub.getString(AppConstants.IS_GATE_HUB, "false");
                IsStayOpenGate = sharedPrefGatehub.getString(AppConstants.IS_STAY_OPEN_GATE, "");

                if (IsGateHub.equalsIgnoreCase("True") && IsStayOpenGate.equalsIgnoreCase("True")){

                    //&& (Constants.GATE_HUB_PIN_NUM.equalsIgnoreCase("") || Constants.GATE_HUB_VEHICLE_NUM.equalsIgnoreCase(""))
                    try {
                        if (IsPersonnelPINRequire.equalsIgnoreCase("True") && Constants.GATE_HUB_PIN_NUM.equalsIgnoreCase("")) {

                            if (Constants.PERSONNEL_PIN_FS1 == null) {
                                Constants.GATE_HUB_PIN_NUM = "";
                            } else if (Constants.PERSONNEL_PIN_FS1.equals("")) {
                                //Do nothing
                            } else {
                                Constants.GATE_HUB_PIN_NUM = Constants.PERSONNEL_PIN_FS1;
                            }

                        } else {
                            Constants.PERSONNEL_PIN_FS1 = Constants.GATE_HUB_PIN_NUM;
                        }

                        //----------------------

                        if (IsVehicleNumberRequire.equalsIgnoreCase("True") && Constants.GATE_HUB_VEHICLE_NUM.equalsIgnoreCase("")) {

                            if (Constants.VEHICLE_NUMBER_FS1 == null) {
                                Constants.GATE_HUB_VEHICLE_NUM = "";
                            } else if (Constants.VEHICLE_NUMBER_FS1.equals("")) {
                                //Do nothing
                            } else {
                                Constants.GATE_HUB_VEHICLE_NUM = Constants.VEHICLE_NUMBER_FS1;
                            }

                        } else {
                            Constants.VEHICLE_NUMBER_FS1 = Constants.GATE_HUB_VEHICLE_NUM;
                        }

                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }

                } else if (IsGateHub.equalsIgnoreCase("True") && IsStayOpenGate.equalsIgnoreCase("True") && (!Constants.GATE_HUB_PIN_NUM.equalsIgnoreCase("") || !Constants.GATE_HUB_VEHICLE_NUM.equalsIgnoreCase(""))) {

                    Constants.PERSONNEL_PIN_FS1 = Constants.GATE_HUB_PIN_NUM;
                    Constants.VEHICLE_NUMBER_FS1 = Constants.GATE_HUB_VEHICLE_NUM;

                } else if (!IsGateHub.equalsIgnoreCase("True") && !IsStayOpenGate.equalsIgnoreCase("True")) {

                    Constants.GATE_HUB_PIN_NUM = "";
                    Constants.GATE_HUB_VEHICLE_NUM = "";
                }

                if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS1")) {
                    pinNumber = Constants.PERSONNEL_PIN_FS1;
                    vehicleNumber = Constants.VEHICLE_NUMBER_FS1;
                    DeptNumber = Constants.DEPARTMENT_NUMBER_FS1;
                    accVehOther = Constants.VEHICLE_OTHER_FS1;
                    accOther = Constants.OTHER_FS1;
                    accOdoMeter = Constants.ODO_METER_FS1;
                    accHours = Constants.HOURS_FS1;
                    CONNECTED_SSID = AppConstants.FS1_CONNECTED_SSID;

                } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS2")) {
                    pinNumber = Constants.PERSONNEL_PIN_FS2;
                    vehicleNumber = Constants.VEHICLE_NUMBER_FS2;
                    DeptNumber = Constants.DEPARTMENT_NUMBER_FS2;
                    accVehOther = Constants.VEHICLE_OTHER_FS2;
                    accOther = Constants.OTHER_FS2;
                    accOdoMeter = Constants.ODO_METER_FS2;
                    accHours = Constants.HOURS_FS2;
                    CONNECTED_SSID = AppConstants.FS2_CONNECTED_SSID;
                } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS3")) {
                    pinNumber = Constants.PERSONNEL_PIN_FS3;
                    vehicleNumber = Constants.VEHICLE_NUMBER_FS3;
                    DeptNumber = Constants.DEPARTMENT_NUMBER_FS3;
                    accVehOther = Constants.VEHICLE_OTHER_FS3;
                    accOther = Constants.OTHER_FS3;
                    accOdoMeter = Constants.ODO_METER_FS3;
                    accHours = Constants.HOURS_FS3;
                    CONNECTED_SSID = AppConstants.FS3_CONNECTED_SSID;
                    Log.i("ps_Vechile no","Step 4:"+vehicleNumber);
                }  else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS4"))  {
                    pinNumber = Constants.PERSONNEL_PIN_FS4;
                    vehicleNumber = Constants.VEHICLE_NUMBER_FS4;
                    DeptNumber = Constants.DEPARTMENT_NUMBER_FS4;
                    accVehOther = Constants.VEHICLE_OTHER_FS4;
                    accOther = Constants.OTHER_FS4;
                    accOdoMeter = Constants.ODO_METER_FS4;
                    accHours = Constants.HOURS_FS4;
                    CONNECTED_SSID = AppConstants.FS4_CONNECTED_SSID;
                }  else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS5"))  {
                    pinNumber = Constants.PERSONNEL_PIN_FS5;
                    vehicleNumber = Constants.VEHICLE_NUMBER_FS5;
                    DeptNumber = Constants.DEPARTMENT_NUMBER_FS5;
                    accVehOther = Constants.VEHICLE_OTHER_FS5;
                    accOther = Constants.OTHER_FS5;
                    accOdoMeter = Constants.ODO_METER_FS5;
                    accHours = Constants.HOURS_FS5;
                    CONNECTED_SSID = AppConstants.FS5_CONNECTED_SSID;
                }  else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS6"))  {
                    pinNumber = Constants.PERSONNEL_PIN_FS6;
                    vehicleNumber = Constants.VEHICLE_NUMBER_FS6;
                    DeptNumber = Constants.DEPARTMENT_NUMBER_FS6;
                    accVehOther = Constants.VEHICLE_OTHER_FS6;
                    accOther = Constants.OTHER_FS6;
                    accOdoMeter = Constants.ODO_METER_FS6;
                    accHours = Constants.HOURS_FS6;
                    CONNECTED_SSID = AppConstants.FS6_CONNECTED_SSID;
                }


                AuthEntityClass authEntityClass = new AuthEntityClass();

                authEntityClass.VehicleNumber = vehicleNumber;
                authEntityClass.FOBNumber = AppConstants.FOB_KEY_VEHICLE;
                authEntityClass.IMEIUDID = AppConstants.getIMEI(activity);
                authEntityClass.WifiSSId = CONNECTED_SSID;
                authEntityClass.SiteId = Integer.parseInt(AppConstants.SITE_ID);

                authEntityClass.ErrorCode = AppConstants.ODO_ERROR_CODE;
                authEntityClass.OdoMeter = accOdoMeter;
                authEntityClass.Hours = accHours;
                authEntityClass.DepartmentNumber = DeptNumber;
                authEntityClass.PersonnelPIN = pinNumber; //Constants.PERSONNEL_PIN_FS2 //Check which Fs is selected
                authEntityClass.Other = accOther;
                authEntityClass.VehicleExtraOther = accVehOther;
                authEntityClass.RequestFrom = "A";
                authEntityClass.RequestFromAPP = "AP";
                authEntityClass.HubId = AppConstants.HUB_ID;
                authEntityClass.IsVehicleNumberRequire = IsVehicleNumberRequire;

                authEntityClass.CurrentLat = "" + Constants.LATITUDE;
                authEntityClass.CurrentLng = "" + Constants.LONGITUDE;

                authEntityClass.AppInfo = " Version " + CommonUtils.getVersionCode(activity) + " " + AppConstants.getDeviceName().toLowerCase() + " ";
                authEntityClass.IsTestTransaction = IsTestTransaction;

                Gson gson = new Gson();
                String jsonData = gson.toJson(authEntityClass);

                System.out.println("Service call data--"+jsonData);

                //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile(TAG + " Authorization Sequence Data: " + jsonData);

                String authString = "Basic " + AppConstants.convertStingToBase64(authEntityClass.IMEIUDID + ":" + CommonUtils.getCustomerDetails(activity).Email + ":" + ServerCallName + AppConstants.LANG_PARAM);

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(15, TimeUnit.SECONDS);
                client.setReadTimeout(15, TimeUnit.SECONDS);
                client.setWriteTimeout(15, TimeUnit.SECONDS);

                RequestBody body = RequestBody.create(TEXT, jsonData);
                Request request = new Request.Builder()
                        .url(AppConstants.WEB_URL)
                        .post(body)
                        .addHeader("Authorization", authString)
                        .build();

                Response response = null;
                response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                AppConstants.SERVER_CALL_IN_PROGRESS =  false;
                e.printStackTrace();
                if(OfflineConstants.isOfflineAccess(activity)){AppConstants.NETWORK_STRENGTH = false;}
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String serverRes) {

            try {
                AppConstants.SERVER_CALL_IN_PROGRESS =  false;
                AppConstants.SERVER_CALL_IN_PROGRESS_FOR_PIN = false;
                AppConstants.SERVER_CALL_IN_PROGRESS_FOR_VEHICLE = false;
                Log.e(TAG,"Activity OnPostExecute");

                String LinkCommType = "";
                try {
                    LinkCommType = WelcomeActivity.serverSSIDList.get(WelcomeActivity.SelectedItemPos).get("LinkCommunicationType");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    LinkCommType = "";
                }

                String txtnTypeForLog = "";
                if (LinkCommType.equalsIgnoreCase("BT")) {
                    txtnTypeForLog = AppConstants.LOG_TXTN_BT;
                } else if (LinkCommType.equalsIgnoreCase("HTTP")) {
                    txtnTypeForLog = AppConstants.LOG_TXTN_HTTP;
                }

                if (serverRes != null && !serverRes.isEmpty()) {

                    JSONObject jsonObject = new JSONObject(serverRes);

                    String ResponceMessage = jsonObject.getString("ResponceMessage");

                    if (ResponceMessage.equalsIgnoreCase("success")) {

                        AppConstants.AUTH_CALL_SUCCESS = true;
                        AppConstants.SERVER_AUTH_CALL_COMPLETED = true;
                        //OnHose Selection
                        if (Constants.CURRENT_SELECTED_HOSE.equals("FS1")) {

                            String ResponceData = jsonObject.getString("ResponceData");

                            JSONObject jsonObjectRD = new JSONObject(ResponceData);

                            AppConstants.IS_FIRST_TIME_USE = jsonObjectRD.getString("IsFirstTimeUse");
                            String TransactionId_FS1 = jsonObjectRD.getString("TransactionId");
                            GateHUBTransactionId = TransactionId_FS1;
                            String VehicleId_FS1 = jsonObjectRD.getString("VehicleId");
                            String VehicleNumber_FS1 = jsonObjectRD.getString("VehicleNumber");
                            String PhoneNumber_FS1 = jsonObjectRD.getString("PhoneNumber");
                            String PersonId_FS1 = jsonObjectRD.getString("PersonId");
                            String PulseRatio_FS1 = jsonObjectRD.getString("PulseRatio");
                            String MinLimit_FS1 = jsonObjectRD.getString("MinLimit");
                            String FuelTypeId_FS1 = jsonObjectRD.getString("FuelTypeId");
                            String ServerDate_FS1 = jsonObjectRD.getString("ServerDate");
                            String TransactionDateWithFormat_FS1 = jsonObjectRD.getString("TransactionDateWithFormat");
                            String IsTLDCall_FS1 = jsonObjectRD.getString("IsTLDCall");
                            String IntervalToStopFuel_FS1 = jsonObjectRD.getString("PumpOffTime");
                            String PumpOnTime_FS1 = jsonObjectRD.getString("PumpOnTime");
                            String LimitReachedMessage_FS1 = jsonObjectRD.getString("LimitReachedMessage");
                            String SiteId_FS1 = jsonObjectRD.getString("SiteId");
                            String PrintDate_FS1 = CommonUtils.getTodaysDateInStringPrint(ServerDate_FS1);

                            String Company_FS1 = jsonObjectRD.getString("Company");
                            String CurrentString = jsonObjectRD.getString("Location");
                            String Location_FS1 = SplitLocation(CurrentString);
                            String PersonName_FS1 = jsonObjectRD.getString("PersonName");
                            String PrinterMacAddress_FS1 = jsonObjectRD.getString("PrinterMacAddress");
                            String PrinterName_FS1 = jsonObjectRD.getString("PrinterName");
                            String EnablePrinter_FS1 = jsonObjectRD.getString("EnablePrinter");
                            String OdoMeter_FS1 = jsonObjectRD.getString("OdoMeter");
                            String Hours_FS1 = jsonObjectRD.getString("Hours");
                            AppConstants.PRINTER_MAC_ADDRESS = PrinterMacAddress_FS1;
                            AppConstants.BLUETOOTH_PRINTER_NAME = PrinterName_FS1;

                            stopAutoFuelSecondstemp = Long.parseLong(IntervalToStopFuel_FS1);
                            numPulseRatio = Double.parseDouble(PulseRatio_FS1);

                            //For Print Recipt
                            String VehicleSum_FS1 = jsonObjectRD.getString("VehicleSum");
                            String DeptSum_FS1 = jsonObjectRD.getString("DeptSum");
                            String VehPercentage_FS1 = jsonObjectRD.getString("VehPercentage");
                            String DeptPercentage_FS1 = jsonObjectRD.getString("DeptPercentage");
                            String SurchargeType_FS1 = jsonObjectRD.getString("SurchargeType");
                            String ProductPrice_FS1 = jsonObjectRD.getString("ProductPrice");

                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(txtnTypeForLog + "-" + " " + ServerCallName + ">> TransactionId: " + TransactionId_FS1 + "; Limit: " + MinLimit_FS1);

                            CommonUtils.SaveVehiFuelInPref_FS1(activity, TransactionId_FS1, VehicleId_FS1, PhoneNumber_FS1, PersonId_FS1,
                                    PulseRatio_FS1, MinLimit_FS1, FuelTypeId_FS1, ServerDate_FS1, IntervalToStopFuel_FS1, PrintDate_FS1,
                                    Company_FS1, Location_FS1, PersonName_FS1, PrinterMacAddress_FS1, PrinterName_FS1, vehicleNumber,
                                    accOther, VehicleSum_FS1, DeptSum_FS1, VehPercentage_FS1, DeptPercentage_FS1, SurchargeType_FS1,
                                    ProductPrice_FS1, IsTLDCall_FS1, EnablePrinter_FS1, OdoMeter_FS1, Hours_FS1, PumpOnTime_FS1,
                                    LimitReachedMessage_FS1, VehicleNumber_FS1, TransactionDateWithFormat_FS1, SiteId_FS1);

                            if (IsGateHub.equalsIgnoreCase("True")) {

                                Log.e("GateSoftwareDelayIssue"," IsGateHub true");
                                //System.out.println("Gate hub true skip display meter ancivity and start transiction ");
                                //String macaddress = AppConstants.SELECTED_MAC_ADDRESS;
                                //String HTTP_URL = "";

                                if (WelcomeActivity.serverSSIDList != null && WelcomeActivity.serverSSIDList.size() == 1) {
                                    try {
                                        String IpAddress = "";
                                        String LinkCommunicationType = WelcomeActivity.serverSSIDList.get(0).get("LinkCommunicationType");
                                        String BTLinkCommType = WelcomeActivity.serverSSIDList.get(0).get("BTLinkCommType");

                                        if (LinkCommunicationType.equalsIgnoreCase("HTTP")) {
                                            try {
                                                String selSSID = WelcomeActivity.serverSSIDList.get(0).get("WifiSSId");
                                                String selMacAddress = WelcomeActivity.serverSSIDList.get(0).get("MacAddress");

                                                boolean isMacConnected = false;
                                                if (AppConstants.DETAILS_LIST_OF_CONNECTED_DEVICES != null) {
                                                    for (int i = 0; i < AppConstants.DETAILS_LIST_OF_CONNECTED_DEVICES.size(); i++) {
                                                        String MA_ConnectedDevices = AppConstants.DETAILS_LIST_OF_CONNECTED_DEVICES.get(i).get("macAddress");

                                                        if (selMacAddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                                                            if (AppConstants.GENERATE_LOGS)
                                                                AppConstants.writeInFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "Selected LINK (" + selSSID + " <==> " + selMacAddress + ") is connected to hotspot.");
                                                            IpAddress = AppConstants.DETAILS_LIST_OF_CONNECTED_DEVICES.get(i).get("ipAddress");
                                                            isMacConnected = true;
                                                            break;
                                                        }
                                                    }
                                                }

                                                if (!isMacConnected) {
                                                    if (AppConstants.GENERATE_LOGS)
                                                        AppConstants.writeInFile(TAG + "Selected LINK (" + selSSID + " <==> " + selMacAddress + ") is not found in connected devices. " + AppConstants.DETAILS_LIST_OF_CONNECTED_DEVICES);

                                                    if (AppConstants.DETAILS_LIST_OF_CONNECTED_DEVICES != null) {
                                                        for (int i = 0; i < AppConstants.DETAILS_LIST_OF_CONNECTED_DEVICES.size(); i++) {
                                                            String MA_ConnectedDevices = AppConstants.DETAILS_LIST_OF_CONNECTED_DEVICES.get(i).get("macAddress");
                                                            if (AppConstants.GENERATE_LOGS)
                                                                AppConstants.writeInFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "Checking Mac Address using info command: (" + MA_ConnectedDevices + ")");

                                                            String connectedIp = AppConstants.DETAILS_LIST_OF_CONNECTED_DEVICES.get(i).get("ipAddress");

                                                            IpAddress = GetAndCheckMacAddressFromInfoCommand(connectedIp, selMacAddress, MA_ConnectedDevices);
                                                            if (!IpAddress.trim().isEmpty()) {
                                                                if (AppConstants.GENERATE_LOGS)
                                                                    AppConstants.writeInFile("===================================================================");
                                                                break;
                                                            }
                                                            if (AppConstants.GENERATE_LOGS)
                                                                AppConstants.writeInFile("===================================================================");
                                                        }
                                                    }
                                                }
                                            } catch (Exception e) {
                                                IpAddress = "";
                                                if (AppConstants.GENERATE_LOGS)
                                                    AppConstants.writeInFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + " Exception while checking HTTP link is connected to hotspot or not. " + e.getMessage() + "; Connected devices: " + AppConstants.DETAILS_LIST_OF_CONNECTED_DEVICES);
                                            }

                                            if (!IpAddress.trim().isEmpty()) {
                                                HTTP_URL = "http://" + IpAddress + ":80/";
                                            }
                                            Log.e("GateSoftwareDelayIssue","   GateHubStartTransaction HTTP_URl");

                                            try {
                                                //Info command commented
                                                URL_INFO = HTTP_URL + "client?command=info";
                                                new CommandsGET_Info().execute(URL_INFO);

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                        } else if (LinkCommunicationType.equalsIgnoreCase("BT") && (BTLinkCommType != null && BTLinkCommType.equalsIgnoreCase("SPP"))) {
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {

                                                    // check connection status of LINK
                                                    if (BTConstants.BT_STATUS_STR_ONE.equalsIgnoreCase("Disconnect")) {
                                                        if (AppConstants.GENERATE_LOGS)
                                                            AppConstants.writeInFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "BTLink is Disconnected.");
                                                        RetryBTLinkConnection();
                                                    } else {
                                                        GateHubStartTransactionForBTLink();
                                                    }
                                                }
                                            }, 500);

                                        }
                                    } catch (Exception e) {
                                        if (AppConstants.GENERATE_LOGS)
                                            AppConstants.writeInFile(TAG + " Exception while accessing serverSSIDList from GateHub. " + e.getMessage());
                                    }
                                }

                            } else {

                                Intent intent = new Intent(activity, DisplayMeterActivity.class);
                                intent.putExtra(Constants.VEHICLE_NUMBER, Constants.VEHICLE_NUMBER_FS1);
                                intent.putExtra(Constants.ODO_METER, Constants.ODO_METER_FS1);
                                intent.putExtra(Constants.DEPT, Constants.DEPARTMENT_NUMBER_FS1);
                                intent.putExtra(Constants.PERSON_PIN, Constants.PERSONNEL_PIN_FS1);
                                intent.putExtra(Constants.OTHER, Constants.OTHER_FS1);
                                intent.putExtra(Constants.HOURS, Constants.HOURS_FS1);
                                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                activity.startActivity(intent);

                            }


                        } else if (Constants.CURRENT_SELECTED_HOSE.equals("FS2")) {

                            String ResponceData = jsonObject.getString("ResponceData");

                            JSONObject jsonObjectRD = new JSONObject(ResponceData);

                            AppConstants.IS_FIRST_TIME_USE = jsonObjectRD.getString("IsFirstTimeUse");
                            String TransactionId = jsonObjectRD.getString("TransactionId");
                            String VehicleId = jsonObjectRD.getString("VehicleId");
                            String VehicleNumber = jsonObjectRD.getString("VehicleNumber");
                            String PhoneNumber = jsonObjectRD.getString("PhoneNumber");
                            String PersonId = jsonObjectRD.getString("PersonId");
                            String PulseRatio = jsonObjectRD.getString("PulseRatio");
                            String MinLimit = jsonObjectRD.getString("MinLimit");
                            String FuelTypeId = jsonObjectRD.getString("FuelTypeId");
                            String ServerDate = jsonObjectRD.getString("ServerDate");
                            String TransactionDateWithFormat = jsonObjectRD.getString("TransactionDateWithFormat");
                            String IntervalToStopFuel = jsonObjectRD.getString("PumpOffTime");
                            String PumpOnTime = jsonObjectRD.getString("PumpOnTime");
                            String IsTLDCall1 = jsonObjectRD.getString("IsTLDCall");
                            String PrintDate = CommonUtils.getTodaysDateInStringPrint(ServerDate);
                            String Company = jsonObjectRD.getString("Company");
                            String CurrentString = jsonObjectRD.getString("Location");
                            String Location = SplitLocation(CurrentString);
                            String PersonName = jsonObjectRD.getString("PersonName");
                            String PrinterMacAddress = jsonObjectRD.getString("PrinterMacAddress");
                            String PrinterName = jsonObjectRD.getString("PrinterName");
                            String EnablePrinter = jsonObjectRD.getString("EnablePrinter");
                            String OdoMeter = jsonObjectRD.getString("OdoMeter");
                            String Hours = jsonObjectRD.getString("Hours");
                            String LimitReachedMessage = jsonObjectRD.getString("LimitReachedMessage");
                            String SiteId = jsonObjectRD.getString("SiteId");
                            AppConstants.BLUETOOTH_PRINTER_NAME = PrinterName;
                            AppConstants.PRINTER_MAC_ADDRESS = PrinterMacAddress;
                            System.out.println("iiiiii" + IntervalToStopFuel);

                            //For Print Recipt
                            String VehicleSum = jsonObjectRD.getString("VehicleSum");
                            String DeptSum = jsonObjectRD.getString("DeptSum");
                            String VehPercentage = jsonObjectRD.getString("VehPercentage");
                            String DeptPercentage = jsonObjectRD.getString("DeptPercentage");
                            String SurchargeType = jsonObjectRD.getString("SurchargeType");
                            String ProductPrice = jsonObjectRD.getString("ProductPrice");

                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(txtnTypeForLog + "-" + " " + ServerCallName + ">> TransactionId: " + TransactionId + "; Limit: " + MinLimit);

                            CommonUtils.SaveVehiFuelInPref(activity, TransactionId, VehicleId, PhoneNumber, PersonId, PulseRatio, MinLimit,
                                    FuelTypeId, ServerDate, IntervalToStopFuel, PrintDate, Company, Location, PersonName, PrinterMacAddress,
                                    PrinterName, vehicleNumber, accOther, VehicleSum, DeptSum, VehPercentage, DeptPercentage, SurchargeType,
                                    ProductPrice, IsTLDCall1, EnablePrinter, OdoMeter, Hours, PumpOnTime, LimitReachedMessage, VehicleNumber,
                                    TransactionDateWithFormat, SiteId);

                            Intent intent = new Intent(activity, DisplayMeterActivity.class);
                            intent.putExtra(Constants.VEHICLE_NUMBER, Constants.VEHICLE_NUMBER_FS2);
                            intent.putExtra(Constants.ODO_METER, Constants.ODO_METER_FS2);
                            intent.putExtra(Constants.DEPT, Constants.DEPARTMENT_NUMBER_FS2);
                            intent.putExtra(Constants.PERSON_PIN, Constants.PERSONNEL_PIN_FS2);
                            intent.putExtra(Constants.OTHER, Constants.OTHER_FS2);
                            intent.putExtra(Constants.HOURS, Constants.HOURS_FS2);

                            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            activity.startActivity(intent);

                        } else if (Constants.CURRENT_SELECTED_HOSE.equals("FS3")) {

                            String ResponceData = jsonObject.getString("ResponceData");

                            JSONObject jsonObjectRD = new JSONObject(ResponceData);

                            AppConstants.IS_FIRST_TIME_USE = jsonObjectRD.getString("IsFirstTimeUse");
                            String TransactionId_FS3 = jsonObjectRD.getString("TransactionId");
                            String VehicleId_FS3 = jsonObjectRD.getString("VehicleId");
                            String VehicleNumber_FS3 = jsonObjectRD.getString("VehicleNumber");
                            String PhoneNumber_FS3 = jsonObjectRD.getString("PhoneNumber");
                            String PersonId_FS3 = jsonObjectRD.getString("PersonId");
                            String PulseRatio_FS3 = jsonObjectRD.getString("PulseRatio");
                            String MinLimit_FS3 = jsonObjectRD.getString("MinLimit");
                            String FuelTypeId_FS3 = jsonObjectRD.getString("FuelTypeId");
                            String ServerDate_FS3 = jsonObjectRD.getString("ServerDate");
                            String TransactionDateWithFormat_FS3 = jsonObjectRD.getString("TransactionDateWithFormat");
                            String IntervalToStopFuel_FS3 = jsonObjectRD.getString("PumpOffTime");
                            String PumpOnTime_FS3 = jsonObjectRD.getString("PumpOnTime");
                            String IsTLDCall_FS3 = jsonObjectRD.getString("IsTLDCall");
                            String PrintDate_FS3 = CommonUtils.getTodaysDateInStringPrint(ServerDate_FS3);
                            String Company_FS3 = jsonObjectRD.getString("Company");
                            String CurrentString = jsonObjectRD.getString("Location");
                            String Location_FS3 = SplitLocation(CurrentString);
                            String PersonName_FS3 = jsonObjectRD.getString("PersonName");
                            String PrinterMacAddress_FS3 = jsonObjectRD.getString("PrinterMacAddress");
                            String PrinterName_FS3 = jsonObjectRD.getString("PrinterName");
                            String EnablePrinter_FS3 = jsonObjectRD.getString("EnablePrinter");
                            String OdoMeter_FS3 = jsonObjectRD.getString("OdoMeter");
                            String Hours_FS3 = jsonObjectRD.getString("Hours");
                            String LimitReachedMessage_FS3 = jsonObjectRD.getString("LimitReachedMessage");
                            String SiteId_FS3 = jsonObjectRD.getString("SiteId");
                            AppConstants.PRINTER_MAC_ADDRESS = PrinterMacAddress_FS3;
                            AppConstants.BLUETOOTH_PRINTER_NAME = PrinterName_FS3;
                            System.out.println("iiiiii" + IntervalToStopFuel_FS3);

                            //For Print Recipt
                            String VehicleSum_FS3 = jsonObjectRD.getString("VehicleSum");
                            String DeptSum_FS3 = jsonObjectRD.getString("DeptSum");
                            String VehPercentage_FS3 = jsonObjectRD.getString("VehPercentage");
                            String DeptPercentage_FS3 = jsonObjectRD.getString("DeptPercentage");
                            String SurchargeType_FS3 = jsonObjectRD.getString("SurchargeType");
                            String ProductPrice_FS3 = jsonObjectRD.getString("ProductPrice");

                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(txtnTypeForLog + "-" + " " + ServerCallName + ">> TransactionId: " + TransactionId_FS3 + "; Limit: " + MinLimit_FS3);

                            CommonUtils.SaveVehiFuelInPref_FS3(activity, TransactionId_FS3, VehicleId_FS3, PhoneNumber_FS3, PersonId_FS3,
                                    PulseRatio_FS3, MinLimit_FS3, FuelTypeId_FS3, ServerDate_FS3, IntervalToStopFuel_FS3, PrintDate_FS3,
                                    Company_FS3, Location_FS3, PersonName_FS3, PrinterMacAddress_FS3, PrinterName_FS3, vehicleNumber,
                                    accOther, VehicleSum_FS3, DeptSum_FS3, VehPercentage_FS3, DeptPercentage_FS3, SurchargeType_FS3,
                                    ProductPrice_FS3, IsTLDCall_FS3, EnablePrinter_FS3, OdoMeter_FS3, Hours_FS3, PumpOnTime_FS3,
                                    LimitReachedMessage_FS3, VehicleNumber_FS3, TransactionDateWithFormat_FS3, SiteId_FS3);

                            Intent intent = new Intent(activity, DisplayMeterActivity.class);
                            intent.putExtra(Constants.VEHICLE_NUMBER, Constants.VEHICLE_NUMBER_FS3);
                            intent.putExtra(Constants.ODO_METER, Constants.ODO_METER_FS3);
                            intent.putExtra(Constants.DEPT, Constants.DEPARTMENT_NUMBER_FS3);
                            intent.putExtra(Constants.PERSON_PIN, Constants.PERSONNEL_PIN_FS3);
                            intent.putExtra(Constants.OTHER, Constants.OTHER_FS3);
                            intent.putExtra(Constants.HOURS, Constants.HOURS_FS3);

                            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            activity.startActivity(intent);

                        }  else if (Constants.CURRENT_SELECTED_HOSE.equals("FS4")) {

                            String ResponceData = jsonObject.getString("ResponceData");

                            JSONObject jsonObjectRD = new JSONObject(ResponceData);

                            AppConstants.IS_FIRST_TIME_USE = jsonObjectRD.getString("IsFirstTimeUse");
                            String TransactionId_FS4 = jsonObjectRD.getString("TransactionId");
                            String VehicleId_FS4 = jsonObjectRD.getString("VehicleId");
                            String VehicleNumber_FS4 = jsonObjectRD.getString("VehicleNumber");
                            String PhoneNumber_FS4 = jsonObjectRD.getString("PhoneNumber");
                            String PersonId_FS4 = jsonObjectRD.getString("PersonId");
                            String PulseRatio_FS4 = jsonObjectRD.getString("PulseRatio");
                            String MinLimit_FS4 = jsonObjectRD.getString("MinLimit");
                            String FuelTypeId_FS4 = jsonObjectRD.getString("FuelTypeId");
                            String ServerDate_FS4 = jsonObjectRD.getString("ServerDate");
                            String TransactionDateWithFormat_FS4 = jsonObjectRD.getString("TransactionDateWithFormat");
                            String IntervalToStopFuel_FS4 = jsonObjectRD.getString("PumpOffTime");
                            String PumpOnTime_FS4 = jsonObjectRD.getString("PumpOnTime");
                            String IsTLDCall_FS4 = jsonObjectRD.getString("IsTLDCall");
                            String PrintDate_FS4 = CommonUtils.getTodaysDateInStringPrint(ServerDate_FS4);
                            String Company_FS4 = jsonObjectRD.getString("Company");
                            String CurrentString = jsonObjectRD.getString("Location");
                            String Location_FS4 = SplitLocation(CurrentString);
                            String PersonName_FS4 = jsonObjectRD.getString("PersonName");
                            String PrinterMacAddress_FS4 = jsonObjectRD.getString("PrinterMacAddress");
                            String PrinterName_FS4 = jsonObjectRD.getString("PrinterName");
                            String EnablePrinter_FS4 = jsonObjectRD.getString("EnablePrinter");
                            String OdoMeter_FS4 = jsonObjectRD.getString("OdoMeter");
                            String Hours_FS4 = jsonObjectRD.getString("Hours");
                            String LimitReachedMessage_FS4 = jsonObjectRD.getString("LimitReachedMessage");
                            String SiteId_FS4 = jsonObjectRD.getString("SiteId");
                            AppConstants.PRINTER_MAC_ADDRESS = PrinterMacAddress_FS4;
                            AppConstants.BLUETOOTH_PRINTER_NAME = PrinterName_FS4;
                            System.out.println("iiiiii" + IntervalToStopFuel_FS4);

                            //For Print Recipt
                            String VehicleSum_FS4 = jsonObjectRD.getString("VehicleSum");
                            String DeptSum_FS4 = jsonObjectRD.getString("DeptSum");
                            String VehPercentage_FS4 = jsonObjectRD.getString("VehPercentage");
                            String DeptPercentage_FS4 = jsonObjectRD.getString("DeptPercentage");
                            String SurchargeType_FS4 = jsonObjectRD.getString("SurchargeType");
                            String ProductPrice_FS4 = jsonObjectRD.getString("ProductPrice");

                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(txtnTypeForLog + "-" + " " + ServerCallName + ">> TransactionId: " + TransactionId_FS4 + "; Limit: " + MinLimit_FS4);

                            CommonUtils.SaveVehiFuelInPref_FS4(activity, TransactionId_FS4, VehicleId_FS4, PhoneNumber_FS4, PersonId_FS4,
                                    PulseRatio_FS4, MinLimit_FS4, FuelTypeId_FS4, ServerDate_FS4, IntervalToStopFuel_FS4, PrintDate_FS4,
                                    Company_FS4, Location_FS4, PersonName_FS4, PrinterMacAddress_FS4, PrinterName_FS4, vehicleNumber,
                                    accOther, VehicleSum_FS4, DeptSum_FS4, VehPercentage_FS4, DeptPercentage_FS4, SurchargeType_FS4,
                                    ProductPrice_FS4, IsTLDCall_FS4, EnablePrinter_FS4, OdoMeter_FS4, Hours_FS4, PumpOnTime_FS4,
                                    LimitReachedMessage_FS4, VehicleNumber_FS4, TransactionDateWithFormat_FS4, SiteId_FS4);

                            Intent intent = new Intent(activity, DisplayMeterActivity.class);
                            intent.putExtra(Constants.VEHICLE_NUMBER, Constants.VEHICLE_NUMBER_FS4);
                            intent.putExtra(Constants.ODO_METER, Constants.ODO_METER_FS4);
                            intent.putExtra(Constants.DEPT, Constants.DEPARTMENT_NUMBER_FS4);
                            intent.putExtra(Constants.PERSON_PIN, Constants.PERSONNEL_PIN_FS4);
                            intent.putExtra(Constants.OTHER, Constants.OTHER_FS4);
                            intent.putExtra(Constants.HOURS, Constants.HOURS_FS4);

                            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            activity.startActivity(intent);

                        } else if (Constants.CURRENT_SELECTED_HOSE.equals("FS5")) {

                            String ResponceData = jsonObject.getString("ResponceData");

                            JSONObject jsonObjectRD = new JSONObject(ResponceData);

                            AppConstants.IS_FIRST_TIME_USE = jsonObjectRD.getString("IsFirstTimeUse");
                            String TransactionId_FS5 = jsonObjectRD.getString("TransactionId");
                            String VehicleId_FS5 = jsonObjectRD.getString("VehicleId");
                            String VehicleNumber_FS5 = jsonObjectRD.getString("VehicleNumber");
                            String PhoneNumber_FS5 = jsonObjectRD.getString("PhoneNumber");
                            String PersonId_FS5 = jsonObjectRD.getString("PersonId");
                            String PulseRatio_FS5 = jsonObjectRD.getString("PulseRatio");
                            String MinLimit_FS5 = jsonObjectRD.getString("MinLimit");
                            String FuelTypeId_FS5 = jsonObjectRD.getString("FuelTypeId");
                            String ServerDate_FS5 = jsonObjectRD.getString("ServerDate");
                            String TransactionDateWithFormat_FS5 = jsonObjectRD.getString("TransactionDateWithFormat");
                            String IntervalToStopFuel_FS5 = jsonObjectRD.getString("PumpOffTime");
                            String PumpOnTime_FS5 = jsonObjectRD.getString("PumpOnTime");
                            String IsTLDCall_FS5 = jsonObjectRD.getString("IsTLDCall");
                            String PrintDate_FS5 = CommonUtils.getTodaysDateInStringPrint(ServerDate_FS5);
                            String Company_FS5 = jsonObjectRD.getString("Company");
                            String CurrentString = jsonObjectRD.getString("Location");
                            String Location_FS5 = SplitLocation(CurrentString);
                            String PersonName_FS5 = jsonObjectRD.getString("PersonName");
                            String PrinterMacAddress_FS5 = jsonObjectRD.getString("PrinterMacAddress");
                            String PrinterName_FS5 = jsonObjectRD.getString("PrinterName");
                            String EnablePrinter_FS5 = jsonObjectRD.getString("EnablePrinter");
                            String OdoMeter_FS5 = jsonObjectRD.getString("OdoMeter");
                            String Hours_FS5 = jsonObjectRD.getString("Hours");
                            String LimitReachedMessage_FS5 = jsonObjectRD.getString("LimitReachedMessage");
                            String SiteId_FS5 = jsonObjectRD.getString("SiteId");
                            AppConstants.PRINTER_MAC_ADDRESS = PrinterMacAddress_FS5;
                            AppConstants.BLUETOOTH_PRINTER_NAME = PrinterName_FS5;
                            System.out.println("iiiiii" + IntervalToStopFuel_FS5);

                            //For Print Recipt
                            String VehicleSum_FS5 = jsonObjectRD.getString("VehicleSum");
                            String DeptSum_FS5 = jsonObjectRD.getString("DeptSum");
                            String VehPercentage_FS5 = jsonObjectRD.getString("VehPercentage");
                            String DeptPercentage_FS5 = jsonObjectRD.getString("DeptPercentage");
                            String SurchargeType_FS5 = jsonObjectRD.getString("SurchargeType");
                            String ProductPrice_FS5 = jsonObjectRD.getString("ProductPrice");

                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(txtnTypeForLog + "-" + " " + ServerCallName + ">> TransactionId: " + TransactionId_FS5 + "; Limit: " + MinLimit_FS5);

                            CommonUtils.SaveVehiFuelInPref_FS5(activity, TransactionId_FS5, VehicleId_FS5, PhoneNumber_FS5, PersonId_FS5,
                                    PulseRatio_FS5, MinLimit_FS5, FuelTypeId_FS5, ServerDate_FS5, IntervalToStopFuel_FS5, PrintDate_FS5,
                                    Company_FS5, Location_FS5, PersonName_FS5, PrinterMacAddress_FS5, PrinterName_FS5, vehicleNumber,
                                    accOther, VehicleSum_FS5, DeptSum_FS5, VehPercentage_FS5, DeptPercentage_FS5, SurchargeType_FS5,
                                    ProductPrice_FS5, IsTLDCall_FS5, EnablePrinter_FS5, OdoMeter_FS5, Hours_FS5, PumpOnTime_FS5,
                                    LimitReachedMessage_FS5, VehicleNumber_FS5, TransactionDateWithFormat_FS5, SiteId_FS5);

                            Intent intent = new Intent(activity, DisplayMeterActivity.class);
                            intent.putExtra(Constants.VEHICLE_NUMBER, Constants.VEHICLE_NUMBER_FS5);
                            intent.putExtra(Constants.ODO_METER, Constants.ODO_METER_FS5);
                            intent.putExtra(Constants.DEPT, Constants.DEPARTMENT_NUMBER_FS5);
                            intent.putExtra(Constants.PERSON_PIN, Constants.PERSONNEL_PIN_FS5);
                            intent.putExtra(Constants.OTHER, Constants.OTHER_FS5);
                            intent.putExtra(Constants.HOURS, Constants.HOURS_FS5);

                            activity.startActivity(intent);

                        } else if (Constants.CURRENT_SELECTED_HOSE.equals("FS6")) {

                            String ResponceData = jsonObject.getString("ResponceData");

                            JSONObject jsonObjectRD = new JSONObject(ResponceData);

                            AppConstants.IS_FIRST_TIME_USE = jsonObjectRD.getString("IsFirstTimeUse");
                            String TransactionId_FS6 = jsonObjectRD.getString("TransactionId");
                            String VehicleId_FS6 = jsonObjectRD.getString("VehicleId");
                            String VehicleNumber_FS6 = jsonObjectRD.getString("VehicleNumber");
                            String PhoneNumber_FS6 = jsonObjectRD.getString("PhoneNumber");
                            String PersonId_FS6 = jsonObjectRD.getString("PersonId");
                            String PulseRatio_FS6 = jsonObjectRD.getString("PulseRatio");
                            String MinLimit_FS6 = jsonObjectRD.getString("MinLimit");
                            String FuelTypeId_FS6 = jsonObjectRD.getString("FuelTypeId");
                            String ServerDate_FS6 = jsonObjectRD.getString("ServerDate");
                            String TransactionDateWithFormat_FS6 = jsonObjectRD.getString("TransactionDateWithFormat");
                            String IntervalToStopFuel_FS6 = jsonObjectRD.getString("PumpOffTime");
                            String PumpOnTime_FS6 = jsonObjectRD.getString("PumpOnTime");
                            String IsTLDCall_FS6 = jsonObjectRD.getString("IsTLDCall");
                            String PrintDate_FS6 = CommonUtils.getTodaysDateInStringPrint(ServerDate_FS6);
                            String Company_FS6 = jsonObjectRD.getString("Company");
                            String CurrentString = jsonObjectRD.getString("Location");
                            String Location_FS6 = SplitLocation(CurrentString);
                            String PersonName_FS6 = jsonObjectRD.getString("PersonName");
                            String PrinterMacAddress_FS6 = jsonObjectRD.getString("PrinterMacAddress");
                            String PrinterName_FS6 = jsonObjectRD.getString("PrinterName");
                            String EnablePrinter_FS6 = jsonObjectRD.getString("EnablePrinter");
                            String OdoMeter_FS6 = jsonObjectRD.getString("OdoMeter");
                            String Hours_FS6 = jsonObjectRD.getString("Hours");
                            String LimitReachedMessage_FS6 = jsonObjectRD.getString("LimitReachedMessage");
                            String SiteId_FS6 = jsonObjectRD.getString("SiteId");
                            AppConstants.PRINTER_MAC_ADDRESS = PrinterMacAddress_FS6;
                            AppConstants.BLUETOOTH_PRINTER_NAME = PrinterName_FS6;
                            System.out.println("iiiiii" + IntervalToStopFuel_FS6);

                            //For Print Recipt
                            String VehicleSum_FS6 = jsonObjectRD.getString("VehicleSum");
                            String DeptSum_FS6 = jsonObjectRD.getString("DeptSum");
                            String VehPercentage_FS6 = jsonObjectRD.getString("VehPercentage");
                            String DeptPercentage_FS6 = jsonObjectRD.getString("DeptPercentage");
                            String SurchargeType_FS6 = jsonObjectRD.getString("SurchargeType");
                            String ProductPrice_FS6 = jsonObjectRD.getString("ProductPrice");

                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(txtnTypeForLog + "-" + " " + ServerCallName + ">> TransactionId: " + TransactionId_FS6 + "; Limit: " + MinLimit_FS6);

                            CommonUtils.SaveVehiFuelInPref_FS6(activity, TransactionId_FS6, VehicleId_FS6, PhoneNumber_FS6, PersonId_FS6,
                                    PulseRatio_FS6, MinLimit_FS6, FuelTypeId_FS6, ServerDate_FS6, IntervalToStopFuel_FS6, PrintDate_FS6,
                                    Company_FS6, Location_FS6, PersonName_FS6, PrinterMacAddress_FS6, PrinterName_FS6, vehicleNumber,
                                    accOther, VehicleSum_FS6, DeptSum_FS6, VehPercentage_FS6, DeptPercentage_FS6, SurchargeType_FS6,
                                    ProductPrice_FS6, IsTLDCall_FS6, EnablePrinter_FS6, OdoMeter_FS6, Hours_FS6, PumpOnTime_FS6,
                                    LimitReachedMessage_FS6, VehicleNumber_FS6, TransactionDateWithFormat_FS6, SiteId_FS6);

                            Intent intent = new Intent(activity, DisplayMeterActivity.class);
                            intent.putExtra(Constants.VEHICLE_NUMBER, Constants.VEHICLE_NUMBER_FS6);
                            intent.putExtra(Constants.ODO_METER, Constants.ODO_METER_FS6);
                            intent.putExtra(Constants.DEPT, Constants.DEPARTMENT_NUMBER_FS6);
                            intent.putExtra(Constants.PERSON_PIN, Constants.PERSONNEL_PIN_FS6);
                            intent.putExtra(Constants.OTHER, Constants.OTHER_FS6);
                            intent.putExtra(Constants.HOURS, Constants.HOURS_FS6);

                            activity.startActivity(intent);

                        }

                    } else if (ResponceMessage.equalsIgnoreCase("fail")) {
                        String ResponceText = jsonObject.getString("ResponceText");
                        String ValidationFailFor = jsonObject.getString("ValidationFailFor");
                        //CommonUtils.showMessageDilaog(activity, "Message", ResponceText);

                        AppConstants.AUTH_CALL_SUCCESS = false;

                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " ServerCall ValidationFailFor " + ValidationFailFor + " ResponceText:" + ResponceText);
                        AppConstants.colorToastBigFont(activity, ResponceText, Color.BLUE);

                        if (ValidationFailFor.equalsIgnoreCase("Vehicle")) {


                            ActivityHandler.removeActivity(2);
                            ActivityHandler.removeActivity(3);
                            ActivityHandler.removeActivity(4);
                            ActivityHandler.removeActivity(5);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(activity, WelcomeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    activity.startActivity(intent);
                                }
                            }, 3000);

                        } else if (ValidationFailFor.equalsIgnoreCase("Odo")) {

                            ActivityHandler.removeActivity(3);
                            ActivityHandler.removeActivity(4);
                            ActivityHandler.removeActivity(5);

                        } else if (ValidationFailFor.equalsIgnoreCase("Dept")) {

                            ActivityHandler.removeActivity(4);
                            ActivityHandler.removeActivity(5);

                        } else if (ValidationFailFor.equalsIgnoreCase("Pin")) {

                            ActivityHandler.removeActivity(5);
                        }

                    }

                } else {

                    if (OfflineConstants.isOfflineAccess(activity)) {
                        AppConstants.NETWORK_STRENGTH = false;
                        Log.i(TAG, "ServerCall Server Response Empty!");
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "ServerCall Response is Empty!");
                        Intent i = new Intent(activity, DisplayMeterActivity.class);
                        activity.startActivity(i);
                    } else {

                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "ServerCall Response is Empty!");
                        AppConstants.clearEditTextFieldsOnBack(activity); //Clear EditText on move to welcome activity.
                        Intent intent = new Intent(activity, WelcomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        activity.startActivity(intent);
                    }

                }

                if (alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }

            }catch (Exception e){
                AppConstants.SERVER_CALL_IN_PROGRESS =  false;
                e.printStackTrace();
                if(OfflineConstants.isOfflineAccess(activity)){AppConstants.NETWORK_STRENGTH = false;}

            }

        }
    }

    public String SplitLocation(String CurrentString) {

        String LocationStr = "";
        try {
            if (!CurrentString.equalsIgnoreCase("")) {
                String[] separated = CurrentString.split(",");
                String L1 = separated[0];
                String L2 = separated[1];
                String L3 = separated[2];

                LocationStr = L1 + "," + L2 + "," + L3 + ".";

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return LocationStr;
    }

    public void GateHubStartTransaction(String HTTP_URL) {

        URL_GET_TXNID = HTTP_URL + "client?command=lasttxtnid";
        URL_SET_TXNID = HTTP_URL + "config?command=txtnid";
        URL_GET_PULSAR = HTTP_URL + "client?command=pulsar ";
        URL_RECORD10_PULSAR = HTTP_URL + "client?command=record10";
        URL_INFO = HTTP_URL + "client?command=info";
        URL_RELAY = HTTP_URL + "config?command=relay";
        PulserTimingAd = HTTP_URL + "config?command=pulsar";
        URL_SET_PULSAR = HTTP_URL + "config?command=pulsar";
        iot_version = "";

        try {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences sharedPref = activity.getSharedPreferences("PreferanceHttpAddress", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();

                    editor.putString("HttpLinkOne", HTTP_URL);
                    editor.apply();

                    Log.e("GateSoftwareDelayIssue","   Start Background Service ");
                    //Start Background Service
                    Intent serviceIntent = new Intent(activity, BackgroundService_AP_PIPE.class);
                    serviceIntent.putExtra("HTTP_URL", HTTP_URL);
                    serviceIntent.putExtra("sqlite_id", sqlite_id);
                    activity.startService(serviceIntent);

                    BackToWelcomeActivity();
                }
            }, 500);

            // GetLastTransaction();
            // String Result_PulserTimingAdjust = new  CommandsPOST().execute(PulserTimingAd, "{\"pulsar_status\":{\"sampling_time_ms\":" + AppConstants.PulserTimingAdjust + "}}").get();

                /*String Relay_result = new CommandsGET().execute(URL_RELAY).get();
                Log.e("GateSoftwareDelayIssue","   Relay_result ");

                if (Relay_result.trim().startsWith("{") && Relay_result.trim().contains("relay_response")) {

                    try {

                        JSONObject jsonObj = new JSONObject(Relay_result);
                        String userData = jsonObj.getString("relay_response");
                        JSONObject jsonObject = new JSONObject(userData);
                        String status = jsonObject.getString("status");

                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "  Relay_result " + " Status: " + status);

                        //IF relay status zero go back to dashboard
                        if (status.equalsIgnoreCase("1")) {

                            AppConstants.colorToastBigFont(activity, "The link is busy, please try after some time.", Color.BLUE);
                            AppConstants.clearEditTextFieldsOnBack(activity); //Clear EditText on move to welcome activity.
                            Intent intent = new Intent(activity, WelcomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            activity.startActivity(intent);


                        } else {

                            //We use pumppff time insted pulseroff time
                            long pulsar_off_time = (stopAutoFuelSecondstemp * 1000) + 3000;
                            new CommandsPOST().execute(URL_SET_PULSAR, "{\"pulsar_status\":{\"pulsar_off_time\":" + pulsar_off_time + "}}");

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    //We use pumppff time insted pulseroff time
                                    long pulsar_off_time = (stopAutoFuelSecondstemp * 1000) + 3000;
                                    new CommandsPOST().execute(URL_SET_PULSAR, "{\"pulsar_status\":{\"pulsar_off_time\":" + pulsar_off_time + "}}");
                                    Log.e("GateSoftwareDelayIssue","   We use pumppff time insted pulseroff time ");
                                }
                            }, 500);



                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    Log.e("GateSoftwareDelayIssue","   Start Background Service ");
                                    //Start Background Service
                                    Intent serviceIntent = new Intent(activity, BackgroundService_AP_PIPE.class);
                                    serviceIntent.putExtra("HTTP_URL", HTTP_URL);
                                    activity.startService(serviceIntent);
                                    //get back to welcome activity

                                    Intent i = new Intent(activity, WelcomeActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    activity.startActivity(i);

                                }
                            }, 1500);

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {

                    //Relay command else commented
                    if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "  Link is unavailable relay");
                    AppConstants.colorToastBigFont(activity, " Link is unavailable", Color.BLUE);
                    AppConstants.clearEditTextFieldsOnBack(activity); //Clear EditText on move to welcome activity.
                    BackgroundServiceKeepDataTransferAlive.IstoggleRequired_DA = true;
                    Intent intent = new Intent(activity, WelcomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    activity.startActivity(intent);

                }*/


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void GetLastTransaction() {

        try {
            String LastTXNid = new CommandsGET().execute(URL_GET_TXNID).get();

            Log.e("GateSoftwareDelayIssue","   LastTXNid "+LastTXNid);

            String respp = new CommandsGET().execute(URL_RECORD10_PULSAR).get();

            Log.e("GateSoftwareDelayIssue","   LastTXNid respp");

            if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile( TAG+"  LAST TRANS RawData " + " LastTXNid" + LastTXNid + "Resp " + respp);

            if (LastTXNid.equals("-1")) {
                System.out.println(LastTXNid);
            } else {

                if (respp.contains("quantity_10_record")) {
                    JSONObject jsonObject = new JSONObject(respp);
                    JSONObject joPulsarStat = jsonObject.getJSONObject("quantity_10_record");
                    int Initialcount = Integer.parseInt(joPulsarStat.getString("1:"));
                    String counts = "";
                    if (Initialcount > 0){
                        counts = String.valueOf(Initialcount);
                    }else{
                        counts = String.valueOf(Initialcount);
                    }

                    Pulses = Integer.parseInt(counts);
                    double lastCnt = Double.parseDouble(counts);
                    double Lastqty = lastCnt / numPulseRatio; //convert to gallons
                    Lastqty = AppConstants.roundNumber(Lastqty, 2);

                    //-----------------------------------------------
                    try {

                        TrazComp authEntityClass = new TrazComp();
                        authEntityClass.TransactionId = LastTXNid;
                        authEntityClass.FuelQuantity = Lastqty;
                        authEntityClass.Pulses = Pulses;
                        authEntityClass.AppInfo = " Version:" + CommonUtils.getVersionCode(activity) + " " + AppConstants.getDeviceName() + " Android " + android.os.Build.VERSION.RELEASE + " " + "--Last Transaction--";
                        authEntityClass.TransactionFrom = "A";
                        authEntityClass.IsFuelingStop = IsFuelingStop;
                        authEntityClass.IsLastTransaction = IsLastTransaction;
                        //authEntityClass.OverrideQuantity = "0";
                        //authEntityClass.OverridePulse = "0";

                        Gson gson = new Gson();
                        String jsonData = gson.toJson(authEntityClass);

                        System.out.println("TrazComp......" + jsonData);
                        if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile( TAG+"  LAST TRANS jsonData " + jsonData);

                        String userEmail = CommonUtils.getCustomerDetails(activity).PersonEmail;

                        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(activity) + ":" + userEmail + ":" + "TransactionComplete" + AppConstants.LANG_PARAM);

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

                            if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile( TAG+"  LAST TRANS SAVED in sqlite");
                            Log.e("GateSoftwareDelayIssue","   LastTXNid saved");
                        }


                    } catch (Exception ex) {

                        if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile( TAG+"  LAST TRANS Exception " + ex.getMessage());
                    }


                }
            }

        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile( TAG+"  LastTXNid Ex:" + e.getMessage() + " ");
        }


    }

    public class CommandsGET_Info extends AsyncTask<String, Void, String> {


        ProgressDialog pd;

        @Override
        protected void onPreExecute() {

            String s = activity.getResources().getString(R.string.PleaseWait);
            SpannableString ss2=  new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            pd = new ProgressDialog(activity);
            pd.setMessage(ss2);
            pd.setCancelable(false);
            pd.show();

        }

        protected String doInBackground(String... param) {

            String resp = "";
            try {

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(15, TimeUnit.SECONDS);
                client.setReadTimeout(15, TimeUnit.SECONDS);
                client.setWriteTimeout(15, TimeUnit.SECONDS);

                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                request.urlString();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
            }

            return resp;
        }

        @Override
        protected void onPostExecute(String FSStatus) {

            try {
                pd.dismiss();

                System.out.println(FSStatus);

                if (FSStatus.startsWith("{") && FSStatus.contains("Version")) {

                    try {

                        JSONObject jsonObj = new JSONObject(FSStatus);
                        String userData = jsonObj.getString("Version");
                        JSONObject jsonObject = new JSONObject(userData);

                        String sdk_version = jsonObject.getString("sdk_version");
                        String mac_address = jsonObject.getString("mac_address");
                        iot_version = jsonObject.getString("iot_version");

                        GateHubStartTransaction(HTTP_URL);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {

                    AppConstants.colorToastBigFont(activity, "The link is busy, please try after some time.", Color.BLUE);
                    AppConstants.clearEditTextFieldsOnBack(activity); //Clear EditText on move to welcome activity.
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + " CommandsGET_Info: info command response is empty. Redirecting to welcome activity.");

                    if (IsGateHub.equalsIgnoreCase("True")) {
                        CommonUtils.upgradeTransactionStatusToSqlite(GateHUBTransactionId, "6", activity);
                    }
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(activity, WelcomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            activity.startActivity(intent);
                        }
                    }, 3000);
                }


            } catch (Exception e) {

                System.out.println(e);
            }

        }
    }

    public class CommandsGET extends AsyncTask<String, Void, String> {

        public String resp = "";

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {

            String s = activity.getResources().getString(R.string.PleaseWait);
            SpannableString ss2=  new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            pd = new ProgressDialog(activity);
            pd.setMessage(ss2);
            pd.setCancelable(false);
        }

        protected String doInBackground(String... param) {


            try {

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(15, TimeUnit.SECONDS);
                client.setReadTimeout(15, TimeUnit.SECONDS);
                client.setWriteTimeout(15, TimeUnit.SECONDS);

                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                request.urlString();
                System.out.println("urlStr" + request.urlString());
                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            try {

                System.out.println(result);

            } catch (Exception e) {

                System.out.println(e);
            }

        }
    }

    public class CommandsPOST extends AsyncTask<String, Void, String> {

        public String resp = "";

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {

            String s = activity.getResources().getString(R.string.PleaseWait);
            SpannableString ss2=  new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            pd = new ProgressDialog(activity);
            pd.setMessage(ss2);
            pd.setCancelable(false);
        }

        protected String doInBackground(String... param) {

            try {


                MediaType JSON = MediaType.parse("application/json");

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(15, TimeUnit.SECONDS);
                client.setReadTimeout(15, TimeUnit.SECONDS);
                client.setWriteTimeout(15, TimeUnit.SECONDS);

                RequestBody body = RequestBody.create(JSON, param[1]);

                Request request = new Request.Builder()
                        .url(param[0])
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            try {

                System.out.println(result);

            } catch (Exception e) {

                System.out.println(e);
            }

        }
    }

    public String GetAndCheckMacAddressFromInfoCommand(String connectedIp, String selMacAddress, String MA_ConnectedDevices) {
        String validIpAddress = "";
        try {
            HTTP_URL = "http://" + connectedIp + ":80/";
            URL_INFO = HTTP_URL + "client?command=info";
            String result = "";
            try {
                result = new Command_GET_INFO().execute(URL_INFO).get();
            } catch (Exception e) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "Error occurred while getting mac address from info command. >> " + e.getMessage());
                result = "";
                e.printStackTrace();
            }

            if (!result.trim().isEmpty()) {
                validIpAddress = CommonUtils.CheckMacAddressFromInfoCommand(TAG, result, connectedIp, selMacAddress, MA_ConnectedDevices);
            } else {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "Info command response is empty. (IP: " + connectedIp + "; MAC Address: " + MA_ConnectedDevices + ")");
            }

        } catch (Exception e) {
            validIpAddress = "";
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "GetAndCheckMacAddressFromInfoCommand Exception >> " + e.getMessage());
            Log.d("Ex", e.getMessage());
        }
        return validIpAddress;
    }

    public class Command_GET_INFO extends AsyncTask<String, Void, String> {

        public String resp = "";

        protected String doInBackground(String... param) {
            try {
                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(15, TimeUnit.SECONDS);
                client.setReadTimeout(15, TimeUnit.SECONDS);
                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();
            } catch (SocketException se) {
                Log.d("Ex", se.getMessage());
            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                System.out.println("APFS_PIPE OUTPUT" + result);
            } catch (Exception e) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "  CommandsGET onPostExecute Exception " + e.getMessage());
                System.out.println(e);
            }
        }
    }

    public void RetryBTLinkConnection() {
        try {
            Handler handler = new Handler();
            int delay = 5000;

            handler.postDelayed(new Runnable() {
                public void run() {
                    if (BTConnectionCounter == 0) {
                        BTConnectionCounter++;

                        RetryConnect();

                        handler.postDelayed(this, delay);
                    } else {

                        if (CheckBTLinkStatus()) {
                            GateHubStartTransactionForBTLink();
                        } else {
                            if (BTConnectionCounter < 2) {
                                BTConnectionCounter++;

                                RetryConnect();

                                handler.postDelayed(this, delay);
                            } else {
                                BTConnectionCounter = 0;
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "BTLink is Unavailable. Redirecting to welcome activity.");
                                CommonUtils.upgradeTransactionStatusToSqlite(GateHUBTransactionId, "6", activity);
                                /*Intent intent = new Intent(activity, WelcomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                activity.startActivity(intent);*/
                                activity.onBackPressed();
                            }
                        }
                    }
                }
            }, delay);

        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(AppConstants.LOG_TXTN_BT + "-" + TAG + " RetryBTLinkConnection Exception " + e.getMessage());
        }
    }

    public void RetryConnect() {
        try {
            if (!BTConstants.BT_STATUS_STR_ONE.equalsIgnoreCase("Connecting...")) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(AppConstants.LOG_TXTN_BT + "-" + TAG + " Retrying to Connect to LINK");
                //Retrying to connect to link
                BTSPPMain btspp = new BTSPPMain();
                btspp.activity = activity;
                btspp.connect1();
            }
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(AppConstants.LOG_TXTN_BT + "-" + TAG + " BTLink: RetryConnect Exception:>>" + e.getMessage());
        }
    }

    private boolean CheckBTLinkStatus() {
        boolean isConnected = false;
        try {
            if (BTConstants.BT_STATUS_STR_ONE.equalsIgnoreCase("Connected")) {
                isConnected = true;
            } else {
                Thread.sleep(1000);
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(AppConstants.LOG_TXTN_BT + "-" + TAG + " BTLink : Checking Connection Status...");
                if (BTConstants.BT_STATUS_STR_ONE.equalsIgnoreCase("Connected")) {
                    isConnected = true;
                } else {
                    Thread.sleep(2000);
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(AppConstants.LOG_TXTN_BT + "-" + TAG + " BTLink : Checking Connection Status...");
                    if (BTConstants.BT_STATUS_STR_ONE.equalsIgnoreCase("Connected")) {
                        isConnected = true;
                    } else {
                        Thread.sleep(2000);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(AppConstants.LOG_TXTN_BT + "-" + TAG + " BTLink : Checking Connection Status...");
                        if (BTConstants.BT_STATUS_STR_ONE.equalsIgnoreCase("Connected")) {
                            isConnected = true;
                        }
                    }
                }
            }
            if (isConnected) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(AppConstants.LOG_TXTN_BT + "-" + TAG + " Link is connected.");
            } else {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(AppConstants.LOG_TXTN_BT + "-" + TAG + " BTLink : STATUS: " + BTConstants.BT_STATUS_STR_ONE);
            }
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(AppConstants.LOG_TXTN_BT + "-" + TAG + " CheckBTLinkStatus Exception:>>" + e.getMessage());
        }
        return isConnected;
    }

    public void GateHubStartTransactionForBTLink() {
        try {

            Log.e("GateSoftware"," Start Background Service ");
            //Start Background Service
            Intent serviceIntent = new Intent(activity, BackgroundService_BTOne.class);
            serviceIntent.putExtra("SERVER_IP", "");
            serviceIntent.putExtra("sqlite_id", sqlite_id);
            activity.startService(serviceIntent);

            BackToWelcomeActivity();

        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(AppConstants.LOG_TXTN_BT + "-" + TAG + " GateHubStartTransactionForBTLink Exception " + e.getMessage());
        }
    }

    private void BackToWelcomeActivity() {
        Intent i = new Intent(activity, WelcomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(i);
    }

}