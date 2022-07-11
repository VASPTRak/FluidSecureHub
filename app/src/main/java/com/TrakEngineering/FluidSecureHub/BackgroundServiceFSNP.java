package com.TrakEngineering.FluidSecureHub;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.TrakEngineering.FluidSecureHub.retrofit.BusProvider;
import com.TrakEngineering.FluidSecureHub.retrofit.ErrorEvent;
import com.TrakEngineering.FluidSecureHub.retrofit.Interface;
import com.TrakEngineering.FluidSecureHub.retrofit.ServerEvent;
import com.TrakEngineering.FluidSecureHub.retrofit.ServerResponse;
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneUID;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


public class BackgroundServiceFSNP extends BackgroundService {

    private static final String TAG = "BS_FSNP ";
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    public String HTTP_URL = "", SelectedHose = "", SelectedHoseStp = "";
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 2000;
    ArrayList<HashMap<String, String>> ServerSSIDetailListFSNP = new ArrayList<>();
    ArrayList<HashMap<String, String>> ErrorMsg = new ArrayList<>();
    ArrayList<HashMap<String, String>> ListOfConnectedDevicesHotspotFSNP = new ArrayList<>();
    ArrayList<String> BlemacAddressList = new ArrayList<>();
    int ps = 1;
    public static boolean st = false;
    String InstantmacAddr = "", BLE_MacAddress = "", BLE_Name = "", BLE_MacAddressStp = "";
    int BLE_rssi = 0;
    String HTTP_URL_FS_1, URL_GET_PULSAR_FS1, URL_SET_PULSAR_FS1, URL_WIFI_FS1, URL_RELAY_FS1;
    String jsonRelayOff = "{\"relay_request\":{\"Password\":\"12345678\",\"Status\":0}}";



    //FS For Stopbutton
    String PhoneNumber;
    String consoleString = "", outputQuantity = "0";
    boolean stopTimer = true;
    double minFuelLimit = 0, numPulseRatio = 0;
    double fillqty = 0;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mHandler = new Handler();
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        Log.i(TAG, " In onStartCommand....");
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "  In onStartCommand....");


        //advertise(true);
        Thread t = new Thread() {

            @Override
            public void run() {

                while (!isInterrupted()) {

                    //IF Enable checkbox true
                    if (AppConstants.EnableFA) {

                        //Log.e(TAG, " FAEnabled  AppConstants.EnableFA:"+AppConstants.EnableFA);
                        //UPdate Connected hotspot List
                        getipOverOSVersion();

                        //List of Near-by FSNP/Ble mac address list
                        if (AppConstants.DetailsServerSSIDList != null && !AppConstants.DetailsServerSSIDList.isEmpty()) {

                            BlemacAddressList.clear();

                            for (int i = 0; i < AppConstants.DetailsServerSSIDList.size(); i++) {

                                String FSNPMacAddress = AppConstants.DetailsServerSSIDList.get(i).get("FSNPMacAddress");
                                BlemacAddressList.add(FSNPMacAddress);

                            }

                        } else {
                            Log.i(TAG, " DetailsServerSSIDList is Empty");
                            Constants.FA_Message = "Link not connected or FSNP mac address not assigned to link";
                        }

                        //temp thread sleep
                        try {
                            Thread.sleep(20000);//10000
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (!mScanning) {
                            Log.i(TAG, "scanLeDevice function is disable");
                            scanLeDevice(true); //Scan for BLE devices
                        }

                    }
                }
            }
        };

        t.start();


        Log.i(TAG, "InstantID MacAddress  scanLeDevice");
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "  scanLeDevice function started ");

        return Service.START_STICKY;
    }

    private void scanLeDevice(final boolean enable) {

        // Log.e("lecallback"," scan start");
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }

    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            (device, rssi, scanRecord) -> new Runnable() {
                @Override
                public void run() {

                    BLE_Name = device.getName();
                    BLE_MacAddress = device.getAddress(); //BLE_MacAddress == FSNPMacAddress
                    String FSTagMacAddr = device.getAddress(); //FSTag_MacAddress
                    BLE_rssi = rssi; //RSSI value  //RSSI value
                    //-----------------------------------
                    System.out.println("DeviceName:" + BLE_Name + "  MacAddress" + BLE_MacAddress);
                    // Log.e("lecallback"," checkFinish");
                    if (BlemacAddressList.contains(BLE_MacAddress)) {

                        //-----Code added to get belo values
                        // Parse the payload of the advertisement packet
                        // as a list of AD structures.
                        List<ADStructure> structures1 = ADPayloadParser.getInstance().parse(scanRecord);

                        // For each AD structure contained in the advertisement packet.
                        for (ADStructure structure : structures1) {

                            // If the AD structure represents Eddystone UID.
                            if (structure instanceof EddystoneUID) {

                                // Eddystone UID
                                EddystoneUID es = (EddystoneUID) structure;

//                                Log.d(TAG, "Length = " + es.getServiceUUID());
//                                Log.d(TAG, "Length = " + es.getLength());
//                                Log.d(TAG, "Service UUID = " + es.getServiceUUID());
//
//
//                                Log.d(TAG, "Tx Power = " + es.getTxPower());
//                                Log.d(TAG, "Namespace ID = " + es.getNamespaceIdAsString());
//                                Log.d(TAG, "Instance ID = " + es.getInstanceIdAsString()); //FSTagMacAddress == InstanceID
//                                Log.d(TAG, "Beacon ID = " + es.getBeaconIdAsString());

                                //get Instance ID to mac address
                                InstantmacAddr = ConvertInstanceIdToMacAddress(es.getInstanceIdAsString());
                                Log.i(TAG, "InstantID MacAddress " + InstantmacAddr);

                                Log.i(TAG, " BLE_MacAddress " + BLE_MacAddress);

                                //0x  is hex, convert to ascii is “sutnoz”
                                String InstanceID_cmd = ConvertHextoASCII(es.getInstanceIdAsString());
                                Log.i(TAG, " InstanceID_cmd:" + InstanceID_cmd);

                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "  InstanceID_cmd: " + InstanceID_cmd);
                                if (InstanceID_cmd.equalsIgnoreCase("sutnoz")) {//sutnoz

                                    Log.i(TAG, " Stop transaction process");
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "  Stop transaction process FSNP MacAddress " + BLE_MacAddress);
                                    //If hose is busy then Stop transaction/background service
                                    BLE_MacAddressStp = BLE_MacAddress;
                                    Constants.FA_Message = "";
                                    StopTransactionProcess();

                                } else {
                                    Log.i(TAG, " Start transaction process");
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "  Start transaction process FSNP MacAddress " + BLE_MacAddress);
                                    Constants.FA_Message = "";
                                    StartTransactionProcess();
                                }


                            } else {
                                Log.i(TAG, " AD structure dosen't represents Eddystone UID");
                                // Toast.makeText(getApplicationContext(),"AD structure dosen't represents Eddystone UID",Toast.LENGTH_SHORT).show();
                            }
                        }

                    } else {
                        Log.i(TAG, " BLE Mac Address Not associated to HUB");
                    }
                }
            };

    public void StartTransactionProcess() {

        try {

            boolean SCall = IsServerCallRequired();
            if (SCall == true) {

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  StartTransactionProcess ServerCallRequired");
                Log.i(TAG, " Server call");
                String userEmail = CommonUtils.getCustomerDetails_backgroundService(BackgroundServiceFSNP.this).PersonEmail;
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundServiceFSNP.this) + ":" + userEmail + ":" + "CheckAndValidateFSNPDetails");

                String IMEI_UDID = String.valueOf(AppConstants.getIMEI(BackgroundServiceFSNP.this));
                String Email = CommonUtils.getCustomerDetails_backgroundService(BackgroundServiceFSNP.this).PersonEmail;
                String FSNPMacAddress = BLE_MacAddress;
                String FSTagMacAddress = InstantmacAddr;

                CheckFSNPDetails(authString, IMEI_UDID, Email, FSNPMacAddress, FSTagMacAddress);

            } else {
                Log.i(TAG, " Server call Not required");
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "  StartTransactionProcess" + e);
        }

    }

    public boolean IsServerCallRequired() {

        String HoseNO = "";
        for (int p = 0; p < AppConstants.DetailsServerSSIDList.size(); p++) {

            String MacAddress = AppConstants.DetailsServerSSIDList.get(p).get("MacAddress");
            String fsnpAddress = AppConstants.DetailsServerSSIDList.get(p).get("FSNPMacAddress");

            if (BLE_MacAddress.equalsIgnoreCase(fsnpAddress)) {

                HoseNO = String.valueOf(p);
                break;

            }
        }

        if (HoseNO.equalsIgnoreCase("0") && Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_1OdoScreen.equalsIgnoreCase("FREE")) {
            return true;
        } else if (HoseNO.equalsIgnoreCase("1") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_2OdoScreen.equalsIgnoreCase("FREE")) {
            return true;
        } else if (HoseNO.equalsIgnoreCase("2") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_3OdoScreen.equalsIgnoreCase("FREE")) {
            return true;
        } else if (HoseNO.equalsIgnoreCase("3") && Constants.FS_4STATUS.equalsIgnoreCase("FREE") && Constants.FS_4OdoScreen.equalsIgnoreCase("FREE")) {
            return true;
        }

        return false;
    }

    public void StopTransactionProcess() {

        //  String HTTP_URL_FS_1, URL_GET_PULSAR_FS1, URL_SET_PULSAR_FS1, URL_WIFI_FS1, URL_RELAY_FS1;
        String IpAddress = "";
        //get ip address of current selected hose to form url
        try {

            //Thread.sleep(500);

            for (int i = 0; i < ListOfConnectedDevicesHotspotFSNP.size(); i++) {

                String Mac_Address = ListOfConnectedDevicesHotspotFSNP.get(i).get("macAddress");
                IpAddress = ListOfConnectedDevicesHotspotFSNP.get(i).get("ipAddress");


                for (int p = 0; p < AppConstants.DetailsServerSSIDList.size(); p++) {

                    String MacAddress = AppConstants.DetailsServerSSIDList.get(p).get("MacAddress");
                    String fsnpAddress = AppConstants.DetailsServerSSIDList.get(p).get("FSNPMacAddress");

                    if (MacAddress.equalsIgnoreCase(Mac_Address) && BLE_MacAddressStp.equalsIgnoreCase(fsnpAddress)) {
                        HTTP_URL_FS_1 = "http://" + IpAddress + ":80/";
                        //SelectedHoseStp = String.valueOf(AppConstants.DetailsServerSSIDList.indexOf("MacAddress"));
                        SelectedHoseStp = String.valueOf(p);
                        break;
                    }
                }

                //----------------------------------------

                URL_GET_PULSAR_FS1 = HTTP_URL_FS_1 + "client?command=pulsar ";
                URL_SET_PULSAR_FS1 = HTTP_URL_FS_1 + "config?command=pulsar";

                URL_WIFI_FS1 = HTTP_URL_FS_1 + "config?command=wifi";
                URL_RELAY_FS1 = HTTP_URL_FS_1 + "config?command=relay";


            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "  StopTransactionProcess Execption ~1 " + e);
        }


        //On Selected hose
        if (SelectedHoseStp.equalsIgnoreCase("0") && !Constants.FS_1STATUS.equalsIgnoreCase("FREE")) {

            SelectedHoseStp = "";
            if (IpAddress != "" || IpAddress != null) {

                //   if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Stop Hose: "+SelectedHoseStp+" ~BackgroundService_AP_PIPE");
                Constants.FS_1STATUS = "FREE";
                stopService(new Intent(BackgroundServiceFSNP.this, BackgroundService_AP_PIPE.class));
                stopButtonFunctionality_FS1();

            } else {
                Log.i(TAG, " Please make sure your connected to FS unit");
            }


        } else if (SelectedHoseStp.equalsIgnoreCase("1") && !Constants.FS_2STATUS.equalsIgnoreCase("FREE")) {

            SelectedHoseStp = "";
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "  Stop Hose: " + SelectedHoseStp + " ~BackgroundService_AP");
            if (IpAddress != "" || IpAddress != null) {

                Constants.FS_2STATUS = "FREE";
                stopService(new Intent(BackgroundServiceFSNP.this, BackgroundService_AP.class));
                stopButtonFunctionality_FS1();

            } else {
                Log.i(TAG, " Please make sure your connected to FS unit");
            }


        } else if (SelectedHoseStp.equalsIgnoreCase("2") && !Constants.FS_3STATUS.equalsIgnoreCase("FREE")) {

            SelectedHoseStp = "";
            if (IpAddress != "" || IpAddress != null) {

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  Stop Hose: " + SelectedHoseStp + " ~BackgroundService_FS_UNIT_3");
                Constants.FS_3STATUS = "FREE";
                stopService(new Intent(BackgroundServiceFSNP.this, BackgroundService_FS_UNIT_3.class));
                stopButtonFunctionality_FS1();

            } else {
                Log.i(TAG, " Please make sure your connected to FS unit");
            }


        } else if (SelectedHoseStp.equalsIgnoreCase("3") && !Constants.FS_4STATUS.equalsIgnoreCase("FREE")) {

            SelectedHoseStp = "";
            if (IpAddress != "" || IpAddress != null) {

                //   if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Stop Hose: "+SelectedHoseStp+" ~BackgroundService_FS_UNIT_4");
                Constants.FS_4STATUS = "FREE";
                stopService(new Intent(BackgroundServiceFSNP.this, BackgroundService_FS_UNIT_4.class));
                stopButtonFunctionality_FS1();

            } else {
                Log.i(TAG, " Please make sure your connected to FS unit");
            }


        } else {
            SelectedHoseStp = "";
        }


    }

    public String ProcessFSNPDtails(String serverRes, String RespTxt, String BLE_MacAddress, String VehicleId, String MinLimit, String SiteId, String PulseRatio, String PersonId, String FuelTypeId, String PhoneNumber, String ServerDate,
                                    String PumpOnTime, String PumpOffTime, String PulserStopTime, String TransactionId, String FirmwareVersion, String FilePath,
                                    String FOBNumber, String Company, String Location, String PersonName, String PrinterName, String PrinterMacAddress, String VehicleSum,
                                    String DeptSum, String VehPercentage, String DeptPercentage, String SurchargeType, String ProductPrice, String parameter, String FSNPMacAddress, String RequireManualOdo, String VehicleNumber, String IsTLDCall, String EnablePrinter) {


        //get ip address of current selected hose to form url
        try {

            for (int i = 0; i < ListOfConnectedDevicesHotspotFSNP.size(); i++) {

                String Mac_Address = ListOfConnectedDevicesHotspotFSNP.get(i).get("macAddress");
                String IpAddress = ListOfConnectedDevicesHotspotFSNP.get(i).get("ipAddress");

                //List of Near-by FSNP/Ble mac address list
                if (AppConstants.DetailsServerSSIDList != null && !AppConstants.DetailsServerSSIDList.isEmpty()) {

                    for (int p = 0; p < AppConstants.DetailsServerSSIDList.size(); p++) {

                        String MacAddress = AppConstants.DetailsServerSSIDList.get(p).get("MacAddress");
                        String fsnpAddress = AppConstants.DetailsServerSSIDList.get(p).get("FSNPMacAddress");
                        String fsnpName = AppConstants.DetailsServerSSIDList.get(p).get("FSAntenna2");

                        if (MacAddress.equalsIgnoreCase(Mac_Address) && FSNPMacAddress.equalsIgnoreCase(fsnpAddress)) {
                            HTTP_URL = "http://" + IpAddress + ":80/";
                            //SelectedHose = String.valueOf(AppConstants.DetailsServerSSIDList.indexOf("MacAddress"));
                            SelectedHose = String.valueOf(p);

                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "  ProcessFSNPDtails" + e);
        }

        //Save response data to sharedpreferance
        SaveDataToSharedPreferances(serverRes, RespTxt, SelectedHose, VehicleId, MinLimit, SiteId, PulseRatio, PersonId, FuelTypeId, PhoneNumber, ServerDate,
                PumpOnTime, PumpOffTime, PulserStopTime, TransactionId, FirmwareVersion, FilePath,
                FOBNumber, Company, Location, PersonName, PrinterName, PrinterMacAddress, VehicleSum,
                DeptSum, VehPercentage, DeptPercentage, SurchargeType, ProductPrice, parameter, IsTLDCall, EnablePrinter);

        if (SelectedHose.equalsIgnoreCase("0") && !HTTP_URL.equals("") && Constants.FS_1STATUS.equalsIgnoreCase("FREE")) {

            if (RequireManualOdo.equalsIgnoreCase("Y")) {

                if (Constants.ManualOdoScreenFree.equalsIgnoreCase("Yes")) {
                    Log.i(TAG, " Enter Odometer manually");
                    //    if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Enter Odometer manually");
                    Constants.FS_1OdoScreen = "BUSY";
                    Constants.ManualOdoScreenFree = "No";
                    Toast.makeText(getApplicationContext(), "Direct to AcceptManualOdoActivityFA", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(BackgroundServiceFSNP.this, AcceptManualOdoActivityFA.class);
                    intent.putExtra("VehicleID", VehicleId);
                    intent.putExtra("VehicleNumber", VehicleNumber);
                    startActivity(intent);

                } else {
                    Log.i(TAG, " Manuall Odometer screen busy");
                    //    if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Manuall Odometer screen busy");
                }


            } else {

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  Started BackgroundService_AP_PIPE for hose: " + SelectedHose);
                Constants.FS_1STATUS = "BUSY";
                Intent serviceIntent = new Intent(BackgroundServiceFSNP.this, BackgroundService_AP_PIPE.class);
                serviceIntent.putExtra("HTTP_URL", HTTP_URL);
                startService(serviceIntent);

            }

        } else if (SelectedHose.equalsIgnoreCase("1") && !HTTP_URL.equals("") && Constants.FS_2STATUS.equalsIgnoreCase("FREE")) {

            if (RequireManualOdo.equalsIgnoreCase("Y")) {

                if (Constants.ManualOdoScreenFree.equalsIgnoreCase("Yes")) {
                    Log.i(TAG, " Enter Odometer manually");
                    //   if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Enter Odometer manually");
                    Constants.FS_2OdoScreen = "BUSY";
                    Constants.ManualOdoScreenFree = "No";
                    Toast.makeText(getApplicationContext(), "Direct to AcceptManualOdoActivityFA", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(BackgroundServiceFSNP.this, AcceptManualOdoActivityFA.class);
                    intent.putExtra("VehicleID", VehicleId);
                    intent.putExtra("VehicleNumber", VehicleNumber);
                    startActivity(intent);

                } else {
                    Log.i(TAG, " Manuall Odometer screen busy");
                    //    if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Manuall Odometer screen busy");

                }


            } else {

                SelectedHose = "";
                //   if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Started BackgroundService_AP for hose: "+SelectedHose);
                Constants.FS_2STATUS = "BUSY";
                Intent serviceIntent = new Intent(BackgroundServiceFSNP.this, BackgroundService_AP.class);
                serviceIntent.putExtra("HTTP_URL", HTTP_URL);
                startService(serviceIntent);

            }


        } else if (SelectedHose.equalsIgnoreCase("2") && !HTTP_URL.equals("") && Constants.FS_3STATUS.equalsIgnoreCase("FREE")) {

            if (RequireManualOdo.equalsIgnoreCase("Y")) {

                if (Constants.ManualOdoScreenFree.equalsIgnoreCase("Yes")) {
                    Log.i(TAG, " Enter Odometer manually");
                    //    if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Enter Odometer manually");
                    Constants.FS_3OdoScreen = "BUSY";
                    Constants.ManualOdoScreenFree = "No";
                    Toast.makeText(getApplicationContext(), "Direct to AcceptManualOdoActivityFA", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(BackgroundServiceFSNP.this, AcceptManualOdoActivityFA.class);
                    intent.putExtra("VehicleID", VehicleId);
                    intent.putExtra("VehicleNumber", VehicleNumber);
                    startActivity(intent);

                } else {
                    Log.i(TAG, " Manuall Odometer screen busy");
                    //   if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Manuall Odometer screen busy");
                }

            } else {

                //    if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Started BackgroundService_FS_UNIT_3 for hose: "+SelectedHose);
                Constants.FS_3STATUS = "BUSY";
                Intent serviceIntent = new Intent(BackgroundServiceFSNP.this, BackgroundService_FS_UNIT_3.class);
                serviceIntent.putExtra("HTTP_URL", HTTP_URL);
                startService(serviceIntent);

            }


        } else if (SelectedHose.equalsIgnoreCase("3") && !HTTP_URL.equals("") && Constants.FS_4STATUS.equalsIgnoreCase("FREE")) {


            if (RequireManualOdo.equalsIgnoreCase("Y")) {

                if (Constants.ManualOdoScreenFree.equalsIgnoreCase("Yes")) {
                    Log.i(TAG, " Enter Odometer manually");
                    //     if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Enter Odometer manually");
                    Constants.FS_4OdoScreen = "BUSY";
                    Constants.ManualOdoScreenFree = "No";
                    Toast.makeText(getApplicationContext(), "Direct to AcceptManualOdoActivityFA", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(BackgroundServiceFSNP.this, AcceptManualOdoActivityFA.class);
                    intent.putExtra("VehicleID", VehicleId);
                    intent.putExtra("VehicleNumber", VehicleNumber);
                    startActivity(intent);

                } else {
                    Log.i(TAG, " Manuall Odometer screen busy");
                    //    if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Manuall Odometer screen busy");
                }


            } else {

                //   if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Started BackgroundService_FS_UNIT_4 for hose: "+SelectedHose);
                Constants.FS_4STATUS = "BUSY";
                Intent serviceIntent = new Intent(BackgroundServiceFSNP.this, BackgroundService_FS_UNIT_4.class);
                serviceIntent.putExtra("HTTP_URL", HTTP_URL);
                startService(serviceIntent);

            }

        } else {
            Log.i(TAG, " Something went wrong in FA Hose selection or Forming http url");
        }

        return null;
    }

    public String SaveDataToSharedPreferances(String serverRes, String RespTxt, String SelectedHose, String VehicleId_sp, String MinLimit_sp, String SiteId_sp, String PulseRatio_sp, String PersonId_sp, String FuelTypeId_sp, String PhoneNumber_sp, String ServerDate_sp,
                                              String PumpOnTime_sp, String PumpOffTime_sp, String PulserStopTime_sp, String TransactionId_sp, String FirmwareVersion_sp, String FilePath_sp,
                                              String FOBNumber_sp, String Company_sp, String Location_sp, String PersonName_sp, String PrinterName_sp, String PrinterMacAddress_sp, String VehicleSum_sp,
                                              String DeptSum_sp, String VehPercentage_sp, String DeptPercentage_sp, String SurchargeType_sp, String ProductPrice_sp, String parameter_sp, String IsTLDCall, String EnablePrinter) {


        //Parse server response and store it to Shared preferances to use in background services
        // serverRes = "{\"ResponceMessage\":\"success\",\"ResponceText\":\"success\",\"ResponceData\":{\"MinLimit\":0,\"PulseRatio\":10.00,\"VehicleId\":126,\"FuelTypeId\":134,\"PersonId\":1420,\"PhoneNumber\":\"234-585-6425\",\"ServerDate\":\"5/22/2018 4:20:30 PM\",\"PulserStopTime\":\"25\",\"PumpOnTime\":\"0\",\"PumpOffTime\":\"50\",\"TransactionId\":\"17839\",\"FirmwareVersion\":\"\",\"FilePath\":\"\",\"FOBNumber\":\"65040D1C9000\",\"Company\":\"\",\"Location\":\"20,Prabhat Soc,NDA RD, Near Bank of Maharashtra, Vidnyan Nagar Rd, Prabhat Housing Society, Sagar Co-Operative Housing Society, Bavdhan, Pune, Maharashtra 411021, India\",\"PersonName\":\"\",\"FluidSecureSiteName\":null,\"BluetoothCardReader\":null,\"BluetoothCardReaderMacAddress\":null,\"PrinterName\":\"\",\"PrinterMacAddress\":\"\",\"VehicleSum\":\"0\",\"DeptSum\":\"0\",\"VehPercentage\":\"True\",\"DeptPercentage\":null,\"SurchargeType\":\"0\",\"ProductPrice\":\"0\",\"VeederRootMacAddress\":null,\"LFBluetoothCardReader\":null,\"LFBluetoothCardReaderMacAddress\":null},\"SSIDDataObj\":null,\"objUserData\":null,\"ValidationFailFor\":\"\",\"PreAuthTransactionsObj\":null}";
        String pinNumber = Constants.AccPersonnelPIN_FS1;
        String vehicleNumber = Constants.AccVehicleNumber_FS1;
        String DeptNumber = Constants.AccDepartmentNumber_FS1;
        String accOther = Constants.AccOther_FS1;
        Integer accOdoMeter = Constants.AccOdoMeter_FS1;
        Integer accHours = Constants.AccHours_FS1;
        String CONNECTED_SSID = AppConstants.FS1_CONNECTED_SSID;

        if (serverRes != null) {

            try {

                if (RespTxt.equalsIgnoreCase("success")) {


                    //Check Current Selected Hose
                    if (SelectedHose.equalsIgnoreCase("0")) {

                        // String ResponceData = jsonObject.getString("ResponceData");

                        // JSONObject jsonObjectRD = new JSONObject(ResponceData);

                        String TransactionId_FS1 = TransactionId_sp;
                        String VehicleId_FS1 = VehicleId_sp;
                        String PhoneNumber_FS1 = PhoneNumber_sp;
                        String PersonId_FS1 = PersonId_sp;
                        String PulseRatio_FS1 = PulseRatio_sp;
                        String MinLimit_FS1 = MinLimit_sp;
                        String FuelTypeId_FS1 = FuelTypeId_sp;
                        String ServerDate_FS1 = ServerDate_sp;
                        String IntervalToStopFuel_FS1 = PumpOffTime_sp;
                        String PrintDate_FS1 = CommonUtils.getTodaysDateInStringPrint(ServerDate_FS1);

                        String Company_FS1 = Company_sp;
                        String CurrentString = Location_sp;
                        String Location_FS1 = "location";//
                        String PersonName_FS1 = PersonName_sp;
                        String PrinterMacAddress_FS1 = PrinterMacAddress_sp;
                        String PrinterName_FS1 = PrinterName_sp;
                        AppConstants.PrinterMacAddress = PrinterMacAddress_sp;
                        AppConstants.BLUETOOTH_PRINTER_NAME = PrinterName_sp;

                        //For Print Recipt
                        String VehicleSum_FS1 = VehicleSum_sp;
                        String DeptSum_FS1 = DeptSum_sp;
                        String VehPercentage_FS1 = VehPercentage_sp;
                        String DeptPercentage_FS1 = DeptPercentage_sp;
                        String SurchargeType_FS1 = SurchargeType_sp;
                        String ProductPrice_FS1 = ProductPrice_sp;
                        String IsTLDCall_FS1 = IsTLDCall;
                        String EnablePrinter_FS1 = EnablePrinter;
                        String OdoMeter_FS1 = "";
                        String Hours_FS1 = "";
                        String PumpOnTime_FS1 = "";


                        CommonUtils.SaveVehiFuelInPref_FS1(BackgroundServiceFSNP.this, TransactionId_FS1, VehicleId_FS1, PhoneNumber_FS1, PersonId_FS1, PulseRatio_FS1, MinLimit_FS1, FuelTypeId_FS1, ServerDate_FS1, IntervalToStopFuel_FS1, PrintDate_FS1, Company_FS1, Location_FS1, PersonName_FS1, PrinterMacAddress_FS1, PrinterName_FS1, vehicleNumber, accOther, VehicleSum_FS1, DeptSum_FS1, VehPercentage_FS1, DeptPercentage_FS1, SurchargeType_FS1, ProductPrice_FS1, IsTLDCall_FS1, EnablePrinter_FS1, OdoMeter_FS1, Hours_FS1, PumpOnTime_FS1,"","","","");


                    } else if (SelectedHose.equalsIgnoreCase("1")) {


                        String TransactionId = TransactionId_sp;
                        String VehicleId = VehicleId_sp;
                        String PhoneNumber = PhoneNumber_sp;
                        String PersonId = PersonId_sp;
                        String PulseRatio = PulseRatio_sp;
                        String MinLimit = MinLimit_sp;
                        String FuelTypeId = FuelTypeId_sp;
                        String ServerDate = ServerDate_sp;
                        String IntervalToStopFuel = PumpOffTime_sp;
                        String PrintDate = CommonUtils.getTodaysDateInStringPrint(ServerDate);

                        String Company = Company_sp;
                        String CurrentString = Location_sp;
                        String Location = "location";
                        String PersonName = PersonName_sp;
                        String PrinterMacAddress = PrinterMacAddress_sp;
                        String PrinterName = PrinterName_sp;
                        AppConstants.PrinterMacAddress = PrinterMacAddress;
                        AppConstants.BLUETOOTH_PRINTER_NAME = PrinterName;

                        //For Print Recipt
                        String VehicleSum = VehicleSum_sp;
                        String DeptSum = DeptSum_sp;
                        String VehPercentage = VehPercentage_sp;
                        String DeptPercentage = DeptPercentage_sp;
                        String SurchargeType = SurchargeType_sp;
                        String ProductPrice = ProductPrice_sp;
                        String IsTLDCall1 = IsTLDCall;
                        String EnablePrinter1 = EnablePrinter;
                        String OdoMeter = "";
                        String Hours = "";
                        String PumpOnTime = "";


                        CommonUtils.SaveVehiFuelInPref(BackgroundServiceFSNP.this, TransactionId, VehicleId, PhoneNumber, PersonId, PulseRatio, MinLimit, FuelTypeId, ServerDate, IntervalToStopFuel, PrintDate, Company, Location, PersonName, PrinterMacAddress, PrinterName, vehicleNumber, accOther, VehicleSum, DeptSum, VehPercentage, DeptPercentage, SurchargeType, ProductPrice, IsTLDCall1, EnablePrinter1, OdoMeter, Hours, PumpOnTime,"","","","");


                    } else if (SelectedHose.equalsIgnoreCase("2")) {

                        String TransactionId_FS3 = TransactionId_sp;
                        String VehicleId_FS3 = VehicleId_sp;
                        String PhoneNumber_FS3 = PhoneNumber_sp;
                        String PersonId_FS3 = PersonId_sp;
                        String PulseRatio_FS3 = PulseRatio_sp;
                        String MinLimit_FS3 = MinLimit_sp;
                        String FuelTypeId_FS3 = FuelTypeId_sp;
                        String ServerDate_FS3 = ServerDate_sp;
                        String IntervalToStopFuel_FS3 = PumpOffTime_sp;
                        String PrintDate_FS3 = CommonUtils.getTodaysDateInStringPrint(ServerDate_FS3);

                        String Company_FS3 = Company_sp;
                        String CurrentString = Location_sp;
                        String Location_FS3 = "location";
                        String PersonName_FS3 = PersonName_sp;
                        String PrinterMacAddress_FS3 = PrinterMacAddress_sp;
                        String PrinterName_FS3 = PrinterName_sp;
                        AppConstants.PrinterMacAddress = PrinterMacAddress_FS3;
                        AppConstants.BLUETOOTH_PRINTER_NAME = PrinterName_FS3;

                        //For Print Recipt
                        String VehicleSum_FS3 = VehicleSum_sp;
                        String DeptSum_FS3 = DeptSum_sp;
                        String VehPercentage_FS3 = VehPercentage_sp;
                        String DeptPercentage_FS3 = DeptPercentage_sp;
                        String SurchargeType_FS3 = SurchargeType_sp;
                        String ProductPrice_FS3 = ProductPrice_sp;
                        String IsTLDCall_FS3 = IsTLDCall;
                        String EnablePrinter_FS3 = EnablePrinter;
                        String OdoMeter_FS3 = "";
                        String Hours_FS3 = "";
                        String PumpOnTime_FS3 = "";

                        CommonUtils.SaveVehiFuelInPref_FS3(BackgroundServiceFSNP.this, TransactionId_FS3, VehicleId_FS3, PhoneNumber_FS3, PersonId_FS3, PulseRatio_FS3, MinLimit_FS3, FuelTypeId_FS3, ServerDate_FS3, IntervalToStopFuel_FS3, PrintDate_FS3, Company_FS3, Location_FS3, PersonName_FS3, PrinterMacAddress_FS3, PrinterName_FS3, vehicleNumber, accOther, VehicleSum_FS3, DeptSum_FS3, VehPercentage_FS3, DeptPercentage_FS3, SurchargeType_FS3, ProductPrice_FS3, IsTLDCall_FS3, EnablePrinter_FS3, OdoMeter_FS3, Hours_FS3, PumpOnTime_FS3,"","","","");

                    } else if (SelectedHose.equalsIgnoreCase("3")) {

                        String TransactionId_FS4 = TransactionId_sp;
                        String VehicleId_FS4 = VehicleId_sp;
                        String PhoneNumber_FS4 = PhoneNumber_sp;
                        String PersonId_FS4 = PersonId_sp;
                        String PulseRatio_FS4 = PulseRatio_sp;
                        String MinLimit_FS4 = MinLimit_sp;
                        String FuelTypeId_FS4 = FuelTypeId_sp;
                        String ServerDate_FS4 = ServerDate_sp;
                        String IntervalToStopFuel_FS4 = PumpOffTime_sp;
                        String PrintDate_FS4 = CommonUtils.getTodaysDateInStringPrint(ServerDate_FS4);

                        String Company_FS4 = Company_sp;
                        String CurrentString = Location_sp;
                        String Location_FS4 = "location";
                        String PersonName_FS4 = PersonName_sp;
                        String PrinterMacAddress_FS4 = PrinterMacAddress_sp;
                        String PrinterName_FS4 = PrinterName_sp;
                        AppConstants.PrinterMacAddress = PrinterMacAddress_FS4;
                        AppConstants.BLUETOOTH_PRINTER_NAME = PrinterName_FS4;
                        //For Print Recipt
                        String VehicleSum_FS4 = VehicleSum_sp;
                        String DeptSum_FS4 = DeptSum_sp;
                        String VehPercentage_FS4 = VehPercentage_sp;
                        String DeptPercentage_FS4 = DeptPercentage_sp;
                        String SurchargeType_FS4 = SurchargeType_sp;
                        String ProductPrice_FS4 = ProductPrice_sp;
                        String IsTLDCall_FS4 = IsTLDCall;
                        String EnablePrinter_FS4 = EnablePrinter;
                        String OdoMeter_FS4 = "";
                        String Hours_FS4 = "";
                        String PumpOnTime_FS4 = "";

                        CommonUtils.SaveVehiFuelInPref_FS4(BackgroundServiceFSNP.this, TransactionId_FS4, VehicleId_FS4, PhoneNumber_FS4, PersonId_FS4, PulseRatio_FS4, MinLimit_FS4, FuelTypeId_FS4, ServerDate_FS4, IntervalToStopFuel_FS4, PrintDate_FS4, Company_FS4, Location_FS4, PersonName_FS4, PrinterMacAddress_FS4, PrinterName_FS4, vehicleNumber, accOther, VehicleSum_FS4, DeptSum_FS4, VehPercentage_FS4, DeptPercentage_FS4, SurchargeType_FS4, ProductPrice_FS4, IsTLDCall_FS4, EnablePrinter_FS4, OdoMeter_FS4, Hours_FS4, PumpOnTime_FS4,"","","","");


                    } else {
                        Log.i(TAG, " Something went wrong In SaveDataToSharedPreferance");
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return pinNumber;
    }

    public String ConvertHextoASCII(String hex) {

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char) decimal);

            temp.append(decimal);
        }
        //System.out.println("Decimal : " + temp.toString());

        return sb.toString();
    }

    public String ConvertInstanceIdToMacAddress(String instanceIdAsString) {

        String str = instanceIdAsString;
        String mac_address = "";

        List<String> strings = new ArrayList<>();
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

    public class CheckAndValidateFSNPDetails extends AsyncTask<String, Void, String> {


        String jsonData;
        String authString;


        @Override
        protected String doInBackground(String... params) {

            String response = "";
            try {

                jsonData = params[0];
                authString = params[1];

                System.out.println("jsonData--" + jsonData);
                System.out.println("authString--" + authString);


                response = serverHandler.PostTextData(BackgroundServiceFSNP.this, AppConstants.webURL, jsonData, authString);

                System.out.println("Id..." + jsonData);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String resp) {

            //{"ResponceMessage":"success","ResponceText":"success","ResponceData":{"MinLimit":0,"PulseRatio":10.00,"VehicleId":126,"FuelTypeId":134,"PersonId":1420,"PhoneNumber":"234-585-6425","ServerDate":"5/22/2018 3:25:13 PM","PulserStopTime":"25","PumpOnTime":"0","PumpOffTime":"50","TransactionId":"13640","FirmwareVersion":"","FilePath":"","FOBNumber":"65040D1C9000","Company":"","Location":"20,Prabhat Soc,NDA RD, Near Bank of Maharashtra, Vidnyan Nagar Rd, Prabhat Housing Society, Sagar Co-Operative Housing Society, Bavdhan, Pune, Maharashtra 411021, India","PersonName":"","FluidSecureSiteName":null,"BluetoothCardReader":null,"BluetoothCardReaderMacAddress":null,"PrinterName":"","PrinterMacAddress":"","VehicleSum":"0","DeptSum":"0","VehPercentage":"True","DeptPercentage":null,"SurchargeType":"0","ProductPrice":"0","VeederRootMacAddress":null,"LFBluetoothCardReader":null,"LFBluetoothCardReaderMacAddress":null},"SSIDDataObj":null,"objUserData":null,"ValidationFailFor":"","PreAuthTransactionsObj":null}
            try {
                JSONObject jsonObj = new JSONObject(resp);
                String ResponceMessage = jsonObj.getString(AppConstants.RES_MESSAGE);
                String ResponceText = jsonObj.getString(AppConstants.RES_TEXT);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void CheckFSNPDetails(String authString, String IMEI_UDID, String Email, String FSNPMacAddress, String FSTagMacAddress) {

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        okhttp3.OkHttpClient.Builder httpClient = new okhttp3.OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .baseUrl(AppConstants.webIP)
                .build();
        Interface service = retrofit.create(Interface.class);

        try {

            JSONObject paramObject = new JSONObject();
            paramObject.put("IMEI_UDID", IMEI_UDID);
            paramObject.put("Email", Email);
            paramObject.put("FSNPMacAddress", FSNPMacAddress);
            paramObject.put("FSTagMacAddress", FSTagMacAddress);


            Call<ServerResponse> call = service.postttt(authString, paramObject.toString());

            call.enqueue(new Callback<ServerResponse>() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                    BusProvider.getInstance().post(new ServerEvent(response.body()));

                    String fsnpName = "";
                    String RespMsg = response.body().getResponceMessage();
                    String RespTxt = response.body().getResponceText();

                    if (response.body().getResponceMessage().equalsIgnoreCase("success")) {

                        String VehicleId = response.body().getVehicleId();
                        String MinLimit = response.body().getMinLimit();
                        String SiteId = response.body().getSiteId();
                        String PulseRatio = response.body().getPulseRatio();
                        String PersonId = response.body().getPersonId();
                        String FuelTypeId = response.body().getFuelTypeId();
                        String PhoneNumber = response.body().getPhoneNumber();
                        String ServerDate = response.body().getServerDate();
                        String PumpOnTime = response.body().getPumpOnTime();
                        String PumpOffTime = response.body().getPumpOffTime();
                        String PulserStopTime = response.body().getPulserStopTime();
                        String TransactionId = response.body().getTransactionId();
                        String FOBNumber = response.body().getFOBNumber();
                        String Company = response.body().getCompany();
                        String Location = response.body().getLocation();
                        String PersonName = response.body().getPersonName();
                        String PrinterName = response.body().getPrinterName();
                        String EnablePrinter = response.body().getEnablePrinter();
                        String PrinterMacAddress = response.body().getPrinterMacAddress();
                        String VehicleSum = response.body().getVehicleSum();
                        String DeptSum = response.body().getDeptSum();
                        String VehPercentage = response.body().getVehPercentage();
                        String DeptPercentage = response.body().getDeptPercentage();
                        String SurchargeType = response.body().getSurchargeType();
                        String ProductPrice = response.body().getProductPrice();
                        String VehicleNumber = response.body().getVehicleNumber();
                        String RequireManualOdo = response.body().getRequireManualOdo();
                        String parameter = response.body().getParameter();
                        String FirmwareVersion = response.body().getFirmwareVersion();
                        String FilePath = response.body().getFilePath();
                        String IsFSNPUpgradable = response.body().getIsFSNPUpgradable();
                        String IsTLDCall = response.body().getIsTLDCall();

                        try {

                           /* if (IsFSNPUpgradable.equalsIgnoreCase("Y")){
                                UpgradeNozzle(FilePath,FirmwareVersion);
                            }*/


                            String PreviousOdo = response.body().getPreviousOdo();
                            String OdoLimit = response.body().getOdoLimit();
                            String OdometerReasonabilityConditions = response.body().getOdometerReasonabilityConditions();
                            String CheckOdometerReasonable = response.body().getCheckOdometerReasonable();


                            if (RequireManualOdo.equalsIgnoreCase("Y")) {

                                SharedPreferences sharedPref = BackgroundServiceFSNP.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("PreviousOdoForFA", PreviousOdo);
                                editor.putString("OdoLimitForFA", OdoLimit);
                                editor.putString("OdometerReasonabilityConditionsForFA", OdometerReasonabilityConditions);
                                editor.putString("CheckOdometerReasonableForFA", CheckOdometerReasonable);
                                editor.commit();

                            }

                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }

                        Log.i(TAG, "\nCheckFSNPDetails Response: " + response.body().getResponceMessage() + "\nText: " + response.body().getResponceText() + "\nVehicleId: " + response.body().getVehicleId());


                        // if (response.body().getVehicleId() != null && !response.body().getVehicleId().isEmpty()) {}
                        Log.i(TAG, " CheckFSNPDetails Response success");
                        //     if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  CheckFSNPDetails Response success");

                        ProcessFSNPDtails(response.body().getResponceMessage(), response.body().getResponceText(), BLE_MacAddress, VehicleId, MinLimit, SiteId, PulseRatio, PersonId, FuelTypeId, PhoneNumber, ServerDate,
                                PumpOnTime, PumpOffTime, PulserStopTime, TransactionId, FirmwareVersion, FilePath,
                                FOBNumber, Company, Location, PersonName, PrinterName, PrinterMacAddress, VehicleSum,
                                DeptSum, VehPercentage, DeptPercentage, SurchargeType, ProductPrice, parameter, FSNPMacAddress, RequireManualOdo, VehicleNumber, IsTLDCall, EnablePrinter);
                        //Toast.makeText(getApplicationContext(),"success",Toast.LENGTH_SHORT).show();

                    } else {

                        for (int i = 0; i < ListOfConnectedDevicesHotspotFSNP.size(); i++) {

                            String Mac_Address = ListOfConnectedDevicesHotspotFSNP.get(i).get("macAddress");
                            String IpAddress = ListOfConnectedDevicesHotspotFSNP.get(i).get("ipAddress");

                            //List of Near-by FSNP/Ble mac address list
                            if (AppConstants.DetailsServerSSIDList != null && !AppConstants.DetailsServerSSIDList.isEmpty()) {

                                for (int p = 0; p < AppConstants.DetailsServerSSIDList.size(); p++) {

                                    String MacAddress = AppConstants.DetailsServerSSIDList.get(p).get("MacAddress");
                                    String fsnpAddress = AppConstants.DetailsServerSSIDList.get(p).get("FSNPMacAddress");
                                    fsnpName = AppConstants.DetailsServerSSIDList.get(p).get("WifiSSId");

                                    if (MacAddress.equalsIgnoreCase(Mac_Address) && BLE_MacAddress.equalsIgnoreCase(fsnpAddress)) {
                                        HTTP_URL = "http://" + IpAddress + ":80/";
                                        //SelectedHose = String.valueOf(AppConstants.DetailsServerSSIDList.indexOf("MacAddress"));
                                        SelectedHose = String.valueOf(p);

                                        if (!fsnpName.isEmpty()) {

                                            Constants.FA_Message = RespMsg + " " + RespTxt + " " + fsnpName;
                                            ///   if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  CheckFSNPDetails Response fail msg: "+RespMsg + " " + RespTxt + " " + fsnpName);
                                        }

                                        if (ps < 3) {
                                            AppConstants.colorToast(BackgroundServiceFSNP.this, RespMsg + " " + RespTxt + " " + fsnpName, Color.RED);
                                            ps++;
                                        }


                                    }
                                }
                            }

                        }

                    }

                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    // handle execution failures like no internet connectivity
                    BusProvider.getInstance().post(new ErrorEvent(-2, t.getMessage()));
                    Log.i(TAG, "Something went wrong in retrofit call --handle execution failures like no internet connectivity.");
                    //    if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Something went wrong in retrofit call --handle execution failures like no internet connectivity.");
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "  CheckFSNPDetails" + e);
        }
    }

    public void getipOverOSVersion(){
        if (Build.VERSION.SDK_INT >= 29) {
            new GetConnectedDevicesIPOS10().execute();
        }else{
            new GetConnectedDevicesIP().execute();
        }
    }

    public class GetConnectedDevicesIPOS10 extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... arg0) {


            ListOfConnectedDevicesHotspotFSNP.clear();

            String resp = "";

            try{
                Runtime runtime = Runtime.getRuntime();
                Process proc = runtime.exec("ip neigh show");
                proc.waitFor();
                BufferedReader br =new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line;

                while ((line = br.readLine()) != null) {

                    //System.out.println("****-"+line);

                    String[] splitted = line.split(" ");

                    if (splitted != null && splitted.length >= 4) {

                        String ipAddress = splitted[0];
                        String macAddress = splitted[4];

                        if(ipAddress.contains(".") && macAddress.contains(":")){
                            System.out.println("***IPAddress" + ipAddress);
                            System.out.println("***macAddress" + macAddress);

                            HashMap<String, String> map = new HashMap<>();
                            map.put("ipAddress", ipAddress);
                            map.put("macAddress", macAddress);

                            ListOfConnectedDevicesHotspotFSNP.add(map);
                        }else {
                            System.out.println("###IPAddress" + ipAddress);
                            System.out.println("###macAddress" + macAddress);
                        }
                    }
                }

            }catch (Exception e)
            {
                e.printStackTrace();
            }


            return resp;


        }


        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            String strJson = result;

        }

    }

    public class GetConnectedDevicesIP extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... arg0) {


            ListOfConnectedDevicesHotspotFSNP.clear();

            String resp = "";

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

                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("ipAddress", ipAddress);
                                    map.put("macAddress", macAddress);

                                    ListOfConnectedDevicesHotspotFSNP.add(map);

                                }
//                                AppConstants.DetailsListOfConnectedDevices = ListOfConnectedDevices;
//                                System.out.println("DeviceConnected" + ListOfConnectedDevices);

                            }

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "  GetConnectedDevicesIP 1 --Exception " + e);
                    } finally {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "  GetConnectedDevicesIP 2 --Exception " + e);
                        }
                    }
                }
            });
            thread.start();


            return resp;


        }


        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            String strJson = result;

        }

    }

    //=======FS UNIT 1 =========
    public void stopButtonFunctionality_FS1() {

        //it stops pulsar logic------
        stopTimer = false;


        new CommandsPOST_FS1().execute(URL_RELAY_FS1, jsonRelayOff);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    String cntA = "0", cntB = "0", cntC = "0";

                    for (int i = 0; i < 3; i++) {

                        String result = new BackgroundServiceFSNP.GETFINALPulsar_FS1().execute(URL_GET_PULSAR_FS1).get();


                        if (result.contains("pulsar_status")) {

                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject joPulsarStat = jsonObject.getJSONObject("pulsar_status");
                            String counts = joPulsarStat.getString("counts");
                            //String pulsar_status = joPulsarStat.getString("pulsar_status");
                            //String pulsar_secure_status = joPulsarStat.getString("pulsar_secure_status");

                            convertCountToQuantity_fs1(counts);


                            if (i == 2) {

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finalLastStep_fs1();
                                    }
                                }, 1000);


                            }


                        } else {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "  GETFINALPulsar_FS1 Result: " + result);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "  stopButtonFunctionality_FS1 Ex" + e);
                }
            }
        }, 1000);
    }

    public class CommandsPOST_FS1 extends AsyncTask<String, Void, String> {

        public String resp = "";

        protected String doInBackground(String... param) {

            Log.i(TAG, " url" + HTTP_URL_FS_1);
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
                e.printStackTrace();
                Log.d("Ex", e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            try {

                //consoleString += "OUTPUT- " + result + "\n";
                // tvConsole.setText(consoleString);

                System.out.println(result);


            } catch (Exception e) {

                e.printStackTrace();
            }

        }
    }

    public class GETFINALPulsar_FS1 extends AsyncTask<String, Void, String> {

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
                e.printStackTrace();
                Log.d("Ex", e.getMessage());
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  GETFINALPulsar_FS1 doInBackground Ex" + e);

            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {


            try {

                // consoleString += "OUTPUT- " + result + "\n";

                // tvConsole.setText(consoleString);

                System.out.println(result);


            } catch (Exception e) {

                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  GETFINALPulsar_FS1 onPostExecute Ex" + e);
            }

        }
    }

    public void convertCountToQuantity_fs1(String counts) {

        outputQuantity = counts;
        fillqty = Double.parseDouble(outputQuantity);
        fillqty = fillqty / numPulseRatio;//convert to gallons

        fillqty = AppConstants.roundNumber(fillqty, 2);

    }

    public void finalLastStep_fs1() {



    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void UpgradeNozzle(String filePath, String firmwareVersion) {

        String[] parts = filePath.split("/");
        String FileName = parts[5]; // FSNP.bin


        // new BackgroundServiceDownloadFirmware.DownloadFileFromURL_FSNP().execute(filePath, FileName);

        String RawUuidStr = "NOZZLEUPGRADE" + firmwareVersion;

        //Broadcast Upgrade FSNP Flag
        advertise(true, RawUuidStr);

    }

    //BELOW CODE FOR ADVERTISING EDDYSTONE FORMAT
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void advertise(boolean UpgradeFNP, String rawUuidStr) {
        //To check if Bluetooth Multiple Advertising is supported on the Device
        if (!BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
            Log.e(TAG, "Multiple advertisement not supported");
            //start.setEnabled(false);
        }

        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        //defining the settings used while advertising
        AdvertiseSettings settings = new AdvertiseSettings.Builder().setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED).setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH).setConnectable(true).build();

        //We make Parcel UUID(UUID can be generated online) and Advertise Data object
        //ParcelUuid pUuid = ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB");

        String StrUUID = GenerateUUID(rawUuidStr);
        ParcelUuid pUuid = ParcelUuid.fromString(StrUUID);

        //building servicedata
        byte txPower = (byte) -16;
        byte FrameType = 0x00;
        byte[] namespaceBytes = toByteArray("01020304050607080910"); //01020304050607080910   NOZZLEUPGRADEXXX.
        Log.e("nB", Integer.toString(namespaceBytes.length));
        byte[] instanceBytes = toByteArray("AABBCCDDEEFF");//AABBCCDDEEFF
        Log.e("instanceIdlength", Integer.toString(instanceBytes.length));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            os.write(new byte[]{FrameType, txPower});
            os.write(namespaceBytes);
            os.write(instanceBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] serviceData = os.toByteArray();
        Log.e("Service Data Length", Integer.toString(serviceData.length));
        Log.e("ServiceData", serviceData.toString());

        AdvertiseData ADdata = new AdvertiseData.Builder().addServiceData(pUuid, serviceData).addServiceUuid(pUuid).setIncludeDeviceName(false).setIncludeTxPowerLevel(false).build();

        Log.e("Data", ADdata.toString());


        //callback to check success or failure when advertising
        AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                Log.e("BLE", "Advertising");
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e("BLE", "Advertising onStartFailure: " + errorCode);
                super.onStartFailure(errorCode);
            }
        };

        if (UpgradeFNP) {
            advertiser.startAdvertising(settings, ADdata, advertisingCallback);
        } else {
            advertiser.stopAdvertising(advertisingCallback);
        }

    }

    protected String GenerateUUID(String rawUuidStr) {

        UUID uniqueId = null;
        uniqueId = UUID.nameUUIDFromBytes(rawUuidStr.getBytes());
        return uniqueId.toString();
    }

    private byte[] toByteArray(String hexString) {
// hexString guaranteed valid.
        int len = hexString.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return bytes;
    }
}