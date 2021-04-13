/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.TrakEngineering.FluidSecureHub.TLD_GattServer;

import android.app.Service;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;
import android.widget.ExpandableListView;

import com.TrakEngineering.FluidSecureHub.AppConstants;
import com.TrakEngineering.FluidSecureHub.CommonUtils;
import com.TrakEngineering.FluidSecureHub.Constants;
import com.TrakEngineering.FluidSecureHub.R;

import java.util.ArrayList;
import java.util.List;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity_tld extends Service {
    private final static String TAG = DeviceControlActivity_tld.class.getSimpleName();


    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService_tld mBluetoothLeService_tld;
    private boolean mConnected = false;
    private String IsTLDCall = "",IsTLDFirmwareUpgrade = "", TLDFirmwareFilePath = "", TLDFIrmwareVersion = "", TLDBEMacAddress = "", LinkMacAddress = "";


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService_tld = ((BluetoothLeService_tld.LocalBinder) service).getService();
            if (!mBluetoothLeService_tld.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                //finish();
                //Stop this background service and return
            }
            // Automatically connects to the device upon successful start-up initialization.
            if (mDeviceAddress != null && mDeviceAddress.contains(":")) {
                mBluetoothLeService_tld.connect(mDeviceAddress);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService_tld = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService_tld.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "TLD Gatt-server connected");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Read Characteristic here
                if (mBluetoothLeService_tld != null) {
                    // mBluetoothLeServiceVehicle.readCharacteristic(characteristic);
                    mBluetoothLeService_tld.readCustomCharacteristic();
                }

            } else if (BluetoothLeService_tld.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "TLD Gatt-server Disconnected");
                //clearUI here
            } else if (BluetoothLeService_tld.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService_tld.getSupportedGattServices());
            } else if (BluetoothLeService_tld.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService_tld.EXTRA_DATA));
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        System.out.println("TLD_ConnectionService start---------");

        SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_TldDetails, Context.MODE_PRIVATE);
        IsTLDCall = sharedPref.getString("IsTLDCall", "");
        IsTLDFirmwareUpgrade = sharedPref.getString("IsTLDFirmwareUpgrade", "");
        TLDFirmwareFilePath = sharedPref.getString("TLDFirmwareFilePath", "");
        TLDFIrmwareVersion = sharedPref.getString("TLDFirmwareFilePath", "");
        LinkMacAddress = sharedPref.getString("selMacAddress", "");
        String PROBEMacAddress = sharedPref.getString("PROBEMacAddress", "");

        TLDBEMacAddress =  ConvertToTLDMacAddress(PROBEMacAddress.replaceAll(":",""));

        //Oncreate code below
        mDeviceName = "LTLD";
        mDeviceAddress = TLDBEMacAddress.toUpperCase().trim();// "88:4A:EA:85:85:FB";
        //mDeviceAddress = PROBEMacAddress.toUpperCase().trim();// "88:4A:EA:85:85:FB";

        Intent gattServiceIntent = new Intent(this, BluetoothLeService_tld.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        if (mBluetoothLeService_tld != null) {

            if (AppConstants.GenerateLogs)AppConstants.WriteinFile("ConfigureTLD " + "MacAddress:"+mDeviceAddress);
            final boolean result = mBluetoothLeService_tld.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }

        //this.stopSelf();

        return super.onStartCommand(intent, flags, startId);
    }


    private void displayData(String data) {
        if (data != null) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (mBluetoothLeService_tld != null) {
                mBluetoothLeService_tld.writeCustomCharacteristic(0x0001, LinkMacAddress);
                //bleCommand mac == WRITE to this characteristic a byte string that begins with 0x0001 followed by the mac address of the target link.

            }
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService_tld.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService_tld.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService_tld.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService_tld.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    public String ConvertToTLDMacAddress(String ProbeAddrAsString) {

        String str = GetProbeOffByOne(ProbeAddrAsString);
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

    private String GetProbeOffByOne(String ProbeStr){

        String FinalStr = "";
        try {

            if (ProbeStr.length() == 12) {

                String Split1 = ProbeStr.substring(0, 8);
                String Split2 = ProbeStr.substring(8, 12);
                String hexNumber = Split2;

                int decimal = Integer.parseInt(hexNumber, 16);
                System.out.println("Hex value is " + decimal);
                String hexx = CommonUtils.decimal2hex(decimal + 1);

                FinalStr = Split1 + hexx;
                System.out.println("Final string" + FinalStr);

            } else {
                Log.i(TAG, "Probe mac address wrong");
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return FinalStr;
    }


    //OnResume
    /*registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService_tld != null) {
        final boolean result = mBluetoothLeService_tld.connect(mDeviceAddress);
        Log.d(TAG, "Connect request result=" + result);
    }*/

    //OnPause
    //unregisterReceiver(mGattUpdateReceiver);

    //OnDestroy
    //unbindService(mServiceConnection);
    //mBluetoothLeService_tld = null;

}
