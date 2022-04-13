package com.TrakEngineering.FluidSecureHub.EddystoneScanner;

import android.app.Service;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.RequiresApi;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.AcceptVehicleActivity_new;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FStagScannerService extends Service {

    private static final String TAG = FStagScannerService.class.getSimpleName();

    // …if you feel like making the log a bit noisier…
    private static boolean DEBUG_SCAN = true;

    //Callback interface for the UI
    public interface OnBeaconEventListener {
        void onBeaconIdentifier(String deviceAddress, int rssi, String instanceId);

        void onBeaconTelemetry(String deviceAddress, float battery, float temperature);
    }

    private BluetoothLeScanner mBluetoothLeScanner;
    private FStagScannerService.OnBeaconEventListener mBeaconEventListener;
    Thread t;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        super.onCreate();

        BluetoothManager manager =
                (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothLeScanner = manager.getAdapter().getBluetoothLeScanner();

        startScanning();

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopScanning();
    }

    public void setBeaconEventListener(FStagScannerService.OnBeaconEventListener listener) {
        mBeaconEventListener = listener;
    }

    /* Using as a bound service to allow event callbacks */
    private FStagScannerService.LocalBinder mBinder = new FStagScannerService.LocalBinder();

    public class LocalBinder extends Binder {
        public FStagScannerService getService() {
            return FStagScannerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /* Being scanning for Eddystone advertisers */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startScanning() {

        ScanFilter beaconFilter = new ScanFilter.Builder()
                .setDeviceName("FSTag")
                .build();

        List<ScanFilter> filters = new ArrayList<>();
        filters.add(beaconFilter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();

        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
        if (DEBUG_SCAN) Log.d(TAG, "Scanning started…");
    }

    /* Terminate scanning */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void stopScanning() {
        mBluetoothLeScanner.stopScan(mScanCallback);
        if (DEBUG_SCAN) Log.d(TAG, "Scanning stopped…");
    }

    /* Handle UID packet discovery on the main thread */
    private void processUidPacket(String deviceAddress, int rssi, String id) {

        if (DEBUG_SCAN)Log.d(TAG, "Eddystone--(" + deviceAddress + ") id = " + id);

        if (mBeaconEventListener != null) {
            mBeaconEventListener
                    .onBeaconIdentifier(deviceAddress, rssi, id);
        }
    }

    /* Handle TLM packet discovery on the main thread */
    private void processTlmPacket(String deviceAddress, float battery, float temp) {
        if (DEBUG_SCAN) {
            Log.d(TAG, "Eddystone(" + deviceAddress + ") battery = " + battery
                    + ", temp = " + temp);
        }

        if (mBeaconEventListener != null) {
            mBeaconEventListener
                    .onBeaconTelemetry(deviceAddress, battery, temp);
        }
    }

    /* Process each unique BLE scan result */
    private ScanCallback mScanCallback = new ScanCallback() {
        private Handler mCallbackHandler =
                new Handler(Looper.getMainLooper());

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            processResult(result);


            //Execute this function from DeviceControlActivity_Vehicle
            /*mCallbackHandler.post(new Runnable() {
                @Override
                public void run() {

                    PostResult();
                }
            });*/

        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.w(TAG, "Scan Error Code: " + errorCode);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                processResult(result);
            }

        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void processResult(ScanResult result) {

            try {

                boolean AlreadyPresent = true;
                HashMap<String, String> map = new HashMap<>();
                map.put("BleName", result.getDevice().getName());
                map.put("BleMacAddress", result.getDevice().getAddress());
                map.put("BleRssi", String.valueOf(result.getRssi()));

                Log.i(TAG, "BleName:" + result.getDevice().getName() + " BleMacAddress:" + result.getDevice().getAddress());

                if ((result.getDevice().getName() != null) && result.getDevice().getName().equalsIgnoreCase("FSTag")) {

                    //  Log.i(TAG, "Ble device name: ~~FSTag~~" + device.getName());

                    if (AcceptVehicleActivity_new.ListOfBleDevices.size() != 0) {

                        for (int p = 0; p < AcceptVehicleActivity_new.ListOfBleDevices.size(); p++) {

                            String BleMacAddr = AcceptVehicleActivity_new.ListOfBleDevices.get(p).get("BleMacAddress");
                            if (result.getDevice().getAddress().equalsIgnoreCase(BleMacAddr)) {
                                AlreadyPresent = false;
                            }
                        }

                    } else {
                        Log.i(TAG, "List not empty");
                    }


                    if (AlreadyPresent) {
                        AcceptVehicleActivity_new.ListOfBleDevices.add(map);
                    }

                } else {
                    Log.i(TAG, "Ble device name: ~~No Tag~~" + result.getDevice().getName());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }




        }
    };

    private void PostResult(){

        //-----------------------------------------------------------------------------
        int InstanceId = 0;
        String rssi_id = "test_rssi";
        String FSTagMacAddress = "";

        if (AcceptVehicleActivity_new.ListOfBleDevices != null || AcceptVehicleActivity_new.ListOfBleDevices.size() != 0){

            FSTagMacAddress = GetClosestBleDevice();

            if (mBeaconEventListener != null) {
                mBeaconEventListener
                        .onBeaconIdentifier(FSTagMacAddress, InstanceId, rssi_id);
            }

        }else{

            if (mBeaconEventListener != null) {
                mBeaconEventListener
                        .onBeaconIdentifier(FSTagMacAddress, InstanceId, rssi_id);
            }
        }



    }

    public String GetClosestBleDevice() {

        String BleName = "", BleMacAddress = "";
        Integer BleRssi = null;

        if (AcceptVehicleActivity_new.ListOfBleDevices.size() != 0) {

            for (int i = 0; i < AcceptVehicleActivity_new.ListOfBleDevices.size(); i++) {

                Integer bleValue = Integer.valueOf(AcceptVehicleActivity_new.ListOfBleDevices.get(i).get("BleRssi"));

                if (BleRssi == null || BleRssi < bleValue) {
                    BleRssi = bleValue;
                    BleName = AcceptVehicleActivity_new.ListOfBleDevices.get(i).get("BleName");
                    BleMacAddress = AcceptVehicleActivity_new.ListOfBleDevices.get(i).get("BleMacAddress");
                }

            }

        } else {
            Log.i(TAG, "Near-by BLE list empty");
        }


        return BleMacAddress;
    }

}
