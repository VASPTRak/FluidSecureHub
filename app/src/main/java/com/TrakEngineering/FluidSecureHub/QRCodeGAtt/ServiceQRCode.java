package com.TrakEngineering.FluidSecureHub.QRCodeGAtt;

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

import com.TrakEngineering.FluidSecureHub.AppConstants;
import com.TrakEngineering.FluidSecureHub.BackgroundServiceDownloadFirmware;
import com.TrakEngineering.FluidSecureHub.CommonUtils;
import com.TrakEngineering.FluidSecureHub.Constants;

import java.io.File;
import java.util.Timer;

public class ServiceQRCode extends Service {

    private final static String TAG = ServiceQRCode.class.getSimpleName();
    private LeServiceQRCode mBluetoothLeService;
    private boolean mConnected = false;
    private String mDeviceAddress = "", mDeviceName = "";
    //BLE Upgrade
    String BLEType;
    String BLEFileLocation;
    String BLEVersion;
    String IsQRUpdate = "N";
    String FOLDER_PATH_BLE = null;
    private int bleVersionCallCount = 0;
    Timer timerQR;

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences sharedPre2 = ServiceQRCode.this.getSharedPreferences("storeBT_FOBDetails", Context.MODE_PRIVATE);
        mDeviceName = sharedPre2.getString("QRCodeReaderForBarcode", ""); //
        mDeviceAddress = sharedPre2.getString("QRCodeBluetoothMacAddressForBarcode", ""); //

        //CheckForFirmwareUpgrade();

        Intent gattServiceIntent = new Intent(this, LeServiceQRCode.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());


    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        AppConstants.REBOOT_QR_READER = false;
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
            mBluetoothLeService = ((LeServiceQRCode.LocalBinder) service).getService();
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
            if (LeServiceQRCode.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;

                Constants.QR_READER_STATUS = "QR Connected";
                System.out.println("ACTION_GATT_QR_CONNECTED");

                /*timerQR = new Timer();

                TimerTask tt = new TimerTask() {
                    @Override
                    public void run() {

                        //Execute below code only if QR reader is  connected
                        if (Constants.QR_READER_STATUS.equalsIgnoreCase("QR Connected") || Constants.QR_READER_STATUS.equalsIgnoreCase("QR Discovered")) {
                            //BLE Upgrade
                            if ((IsQRUpdate.trim().equalsIgnoreCase("Y")) && bleVersionCallCount == 0) {
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
                            if (IsQRUpdate.trim().equalsIgnoreCase("Y")) {
                                if (bleVersionCallCount != 0) {
                                    readFobKey();
                                }
                            } else {
                                readFobKey();
                            }

                        }
                    }

                };

                timerQR.schedule(tt, 0, 1000);*/


            } else if (LeServiceQRCode.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Constants.QR_READER_STATUS = "QR Disconnected";
                System.out.println("ACTION_GATT_QR_DISCONNECTED");

            } else if (LeServiceQRCode.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                System.out.println("ACTION_GATT_QR_SERVICES_DISCOVERED");
                Constants.QR_READER_STATUS = "QR Discovered";
            } else if (LeServiceQRCode.ACTION_DATA_AVAILABLE.equals(action)) {
                System.out.println("ACTION_GATT_QR_AVAILABLE");
                System.out.println("ACTION_DATA_AVAILABLE");
                Constants.QR_READER_STATUS = "QR Connected";
                displayData(intent.getStringExtra(LeServiceQRCode.EXTRA_DATA));
            } else {
                Constants.QR_READER_STATUS = "QR Disconnected";
            }
        }
    };

    public static String unHex(String arg) {

        String str = "";
        for(int i=0;i<arg.length();i+=2)
        {
            String s = arg.substring(i, (i + 2));
            int decimal = Integer.parseInt(s, 16);
            str = str + (char) decimal;
        }
        return str;
    }

    private void displayData(String data) {
        if (data != null) {
            try {

                Log.e("Qrcode data>>", data);
                String[] Seperate = data.split("\n");

                String last_val = "";
                if (Seperate.length > 1) {
                    last_val = Seperate[Seperate.length - 1];
                }

               String java_string_value = unHex(last_val.replace(" ","").trim());

                if (!java_string_value.equals("00 00 00 ") && !java_string_value.equalsIgnoreCase("00 00 00 00 00 00 00 00 00") && !java_string_value.equalsIgnoreCase("...")) {
                    sendQRDetailsToActivity(java_string_value);
                }

                /*SharedPreferences sharedPre = ServiceQRCode.this.getSharedPreferences("BLEUpgradeFlag", Context.MODE_PRIVATE);
                String SRUdate = sharedPre.getString("bleQRUpdateSuccessFlag", "N");
                if (SRUdate.equalsIgnoreCase("Y")) {
                    mBluetoothLeService.writeCustomCharacteristic(0x01, "", false);
                }*/

              //  mBluetoothLeService.writeCustomCharacteristic(0x01, "", false);

            } catch (Exception ex) {
                System.out.println(ex);

            }

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);

        if (timerQR != null)
            timerQR.cancel();

        unregisterReceiver(mGattUpdateReceiver);

    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LeServiceQRCode.ACTION_GATT_CONNECTED);
        intentFilter.addAction(LeServiceQRCode.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(LeServiceQRCode.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(LeServiceQRCode.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    private void sendQRDetailsToActivity(String newData) {

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("ServiceToActivityMagCard");
        broadcastIntent.putExtra("QRCodeValue", newData);
        broadcastIntent.putExtra("Action", "QRReader");
        sendBroadcast(broadcastIntent);
    }

    private void CheckForFirmwareUpgrade() {

        //BLE upgrade
        SharedPreferences myPrefslo = this.getSharedPreferences("BLEUpgradeInfo", 0);
        BLEType = myPrefslo.getString("BLEType", "");
        BLEFileLocation = myPrefslo.getString("BLEFileLocation", "");
        IsQRUpdate = myPrefslo.getString("IsQRUpdate", "");
        BLEVersion = myPrefslo.getString("BLEVersion", "");
        //FOLDER_PATH_BLE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/www/FSCardReader_" + BLEType + "/";
        FOLDER_PATH_BLE = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/www/FSCardReader_" + BLEType + "/";
        String CheckVersionFileLocation = FOLDER_PATH_BLE + BLEVersion + "_check.txt";

        if (IsQRUpdate.trim().equalsIgnoreCase("Y")) {

            DeleteOldVersionTxtFiles(FOLDER_PATH_BLE);
            //File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "/www/FSCardReader_" + BLEType);
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/www/FSCardReader_" + BLEType);
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdirs();
            }
            if (!success) {
                AppConstants.alertDialogBox(ServiceQRCode.this, "Please check File is present in FSCardReader_QR Folder in Internal(Device) Storage");
            }

            if (BLEFileLocation != null) {

                //Check Version File present or not
                File f = new File(CheckVersionFileLocation);
                if (f.exists()) {
                    Log.e(TAG, " BLF Upgrade File already downloaded. skip downloading..");
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + " File already downloaded. skip downloading..");
                } else {
                    new BackgroundServiceDownloadFirmware.DownloadLinkAndReaderFirmware().execute(BLEFileLocation, "FSCardReader_" + BLEType + ".bin", "BLEUpdate");
                }

            } else {
                Log.e(TAG, "BLE reader upgrade File path null");
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " BLE reader upgrade File path null");
            }


        } else {
            SharedPreferences sharedPre = getSharedPreferences("BLEUpgradeFlag", 0);
            SharedPreferences.Editor editor = sharedPre.edit();
            editor.putString("bleQRUpdateSuccessFlag", "N");
            editor.putString("bleLFUpdateSuccessFlag", "N");
            editor.commit();
        }

    }

    private void readBLEVersion() {

        System.out.println("Inside readBLEVersion mBluetoothLeServiceVehicle");
        mBluetoothLeService.readCustomCharacteristic(false);
        bleVersionCallCount++;

    }

    private void readFobKey() {

        if (AppConstants.REBOOT_QR_READER) {
            System.out.println("ACTION_GATT_QR_Reboot cmd");
            mBluetoothLeService.writeRebootCharacteristic();
            AppConstants.REBOOT_QR_READER = false;
        } else {
            AppConstants.REBOOT_QR_READER = false;
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

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
