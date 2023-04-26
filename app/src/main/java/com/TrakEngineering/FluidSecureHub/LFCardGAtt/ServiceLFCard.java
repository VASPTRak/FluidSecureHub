package com.TrakEngineering.FluidSecureHub.LFCardGAtt;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.TrakEngineering.FluidSecureHub.AppConstants;
import com.TrakEngineering.FluidSecureHub.BackgroundServiceDownloadFirmware;
import com.TrakEngineering.FluidSecureHub.CommonUtils;
import com.TrakEngineering.FluidSecureHub.Constants;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class ServiceLFCard extends Service {

    private final static String TAG = ServiceLFCard.class.getSimpleName();
    private LeServiceLFCard mBluetoothLeService;
    private boolean mConnected = false;
    private String mDeviceAddress = "", mDeviceName = "";
    //BLE Upgrade
    String BLEType;
    String BLEFileLocation;
    String BLEVersion;
    String IsLFUpdate = "N";
    String FOLDER_PATH_BLE = null;
    private int bleVersionCallCount = 0;
    Timer timerLF;

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences sharedPre2 = ServiceLFCard.this.getSharedPreferences("storeBT_FOBDetails", Context.MODE_PRIVATE);
        mDeviceName = sharedPre2.getString("LFBluetoothCardReader", "");
        mDeviceAddress = sharedPre2.getString("LFBluetoothCardReaderMacAddress", "");

        CheckForFirmwareUpgrade();

        Intent gattServiceIntent = new Intent(this, LeServiceLFCard.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());


    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        AppConstants.RebootHF_reader = false;
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }


        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((LeServiceLFCard.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");

            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
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
            if (LeServiceLFCard.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                Constants.LF_ReaderStatus = "LF Connected";
                System.out.println("ACTION_GATT_LF_CONNECTED");
                timerLF = new Timer();

                TimerTask tt = new TimerTask() {
                    @Override
                    public void run() {

                        //Execute below code only if LF reader is  connected
                        if (Constants.LF_ReaderStatus.equalsIgnoreCase("LF Connected") || Constants.LF_ReaderStatus.equalsIgnoreCase("LF Discovered")) {
                            //BLE Upgrade
                            if ((IsLFUpdate.trim().equalsIgnoreCase("Y")) && bleVersionCallCount == 0) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                readBLEVersion();
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            //Read Fob key
                            if (IsLFUpdate.trim().equalsIgnoreCase("Y")) {
                                if (bleVersionCallCount != 0) {
                                    readFobKey();
                                }
                            } else {
                                readFobKey();
                            }

                        }
                    }

                };

                timerLF.schedule(tt, 0, 1000);

            } else if (LeServiceLFCard.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Constants.LF_ReaderStatus = "LF Disconnected";
                System.out.println("ACTION_GATT_LF_DISCONNECTED");
            } else if (LeServiceLFCard.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Constants.LF_ReaderStatus = "LF Discovered";
                System.out.println("ACTION_GATT_LF_DISCOVERED");

            } else if (LeServiceLFCard.ACTION_DATA_AVAILABLE.equals(action)) {

                System.out.println("ACTION_GATT_LF_DATA_AVAILABLE");
                Constants.LF_ReaderStatus = "LF Connected";
                displayData(intent.getStringExtra(LeServiceLFCard.EXTRA_DATA));
            } else {
                Constants.LF_ReaderStatus = "LF Disconnected";
            }
        }
    };


    private void displayData(String data) {
        if (data != null) {


            try {
                String[] Seperate = data.split("\n");

                String last_val = "";
                if (Seperate.length > 1) {
                    last_val = Seperate[Seperate.length - 1];
                }

                last_val = last_val.replace("\\n", "");

                last_val = last_val.replace(" ","").trim();

                if (!last_val.equals("00 00 00 ") && !last_val.equalsIgnoreCase("00 00 00 00 00 00 00 00 00") && CommonUtils.ValidateFobkey(last_val)) {
                    sendLFDetailsToActivity(last_val);

                    SharedPreferences sharedPre = ServiceLFCard.this.getSharedPreferences("BLEUpgradeFlag", Context.MODE_PRIVATE);
                    String SRUdate = sharedPre.getString("bleLFUpdateSuccessFlag", "N");
                    if (SRUdate.equalsIgnoreCase("Y")) {
                        mBluetoothLeService.writeCustomCharacteristic(0x01, "", true);
                    }

                    mBluetoothLeService.writeCustomCharacteristic(0x01, "", false);
                }




            } catch (Exception ex) {
                System.out.println(ex);

            }

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);

        if (timerLF != null)
            timerLF.cancel();

        unregisterReceiver(mGattUpdateReceiver);

    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LeServiceLFCard.ACTION_GATT_CONNECTED);
        intentFilter.addAction(LeServiceLFCard.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(LeServiceLFCard.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(LeServiceLFCard.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    private void sendLFDetailsToActivity(String newData) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("ServiceToActivityMagCard");
        broadcastIntent.putExtra("LFCardValue", newData);
        broadcastIntent.putExtra("Action", "LFReader");
        sendBroadcast(broadcastIntent);

    }

    private void CheckForFirmwareUpgrade() {

        //BLE upgrade
        SharedPreferences myPrefslo = this.getSharedPreferences("BLEUpgradeInfo", 0);
        BLEType = myPrefslo.getString("BLEType", "");
        BLEFileLocation = myPrefslo.getString("BLEFileLocation", "");
        BLEVersion = myPrefslo.getString("BLEVersion", "");
        IsLFUpdate = myPrefslo.getString("IsLFUpdate", "");
        //FOLDER_PATH_BLE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/www/FSCardReader/";
        FOLDER_PATH_BLE = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/www/FSCardReader/";
        String CheckVersionFileLocation = FOLDER_PATH_BLE + BLEVersion + "_check.txt";

        if (IsLFUpdate.trim().equalsIgnoreCase("Y")) {

            DeleteOldVersionTxtFiles(FOLDER_PATH_BLE);
            //File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "/www/FSCardReader");
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/www/FSCardReader");
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdirs();
            }
            if (!success) {
                AppConstants.AlertDialogBox(ServiceLFCard.this, "Please check File is present in FSCardReader_LF Folder in Internal(Device) Storage");
            }

            if (BLEFileLocation != null) {

                //Check Version File present or not
                File f = new File(CheckVersionFileLocation);
                if (f.exists()) {
                    Log.e(TAG, " BLF Upgrade File already downloaded. skip downloading..");
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " File already downloaded. skip downloading..");
                } else {
                    new BackgroundServiceDownloadFirmware.DownloadLinkAndReaderFirmware().execute(BLEFileLocation, "FSCardReader.bin", "BLEUpdate");
                }

            } else {
                Log.e(TAG, "BLE reader upgrade File path null");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " BLE reader upgrade File path null");
            }


        } else {
            SharedPreferences sharedPre = getSharedPreferences("BLEUpgradeFlag", 0);
            SharedPreferences.Editor editor = sharedPre.edit();
            editor.putString("bleLFUpdateSuccessFlag", "N");
            editor.commit();
        }

    }

    private void readBLEVersion() {

        System.out.println("Inside readBLEVersion mBluetoothLeServiceVehicle");
        mBluetoothLeService.readCustomCharacteristic(true);
        bleVersionCallCount++;

    }

    private void readFobKey() {

        if (AppConstants.RebootHF_reader) {
            System.out.println("ACTION_GATT_LF_Reboot cmd");
            mBluetoothLeService.writeRebootCharacteristic();
            AppConstants.RebootHF_reader = false;
        } else {
            AppConstants.RebootHF_reader = false;
            mBluetoothLeService.readCustomCharacteristic(false);
        }

    }

    private void DeleteOldVersionTxtFiles(String FOLDER_PATH_BLE) {

        try {

            File folder = new File(FOLDER_PATH_BLE);
            boolean exists = folder.exists();
            if (exists) {
                CommonUtils.getAllFilesInDir(folder);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}

