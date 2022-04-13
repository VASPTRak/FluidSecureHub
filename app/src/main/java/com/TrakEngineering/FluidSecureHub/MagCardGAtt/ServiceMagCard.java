package com.TrakEngineering.FluidSecureHub.MagCardGAtt;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.AppConstants;
import com.TrakEngineering.FluidSecureHub.CommonUtils;
import com.TrakEngineering.FluidSecureHub.Constants;

import java.util.Timer;
import java.util.TimerTask;

public class ServiceMagCard extends Service {

    private final static String TAG = ServiceMagCard.class.getSimpleName();
    private LeServiceMagCard mBluetoothLeService;
    private boolean mConnected = false;
    private String mDeviceAddress = "", mDeviceName = "";
    Timer timerMag;

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences sharedPre2 = ServiceMagCard.this.getSharedPreferences("storeBT_FOBDetails", Context.MODE_PRIVATE);
        mDeviceName = sharedPre2.getString("MagneticCardReader", ""); //
        mDeviceAddress = sharedPre2.getString("MagneticCardReaderMacAddress", ""); //

        Intent gattServiceIntent = new Intent(this, LeServiceMagCard.class);
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

        AppConstants.RebootHF_reader = false;
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((LeServiceMagCard.LocalBinder) service).getService();
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
            if (LeServiceMagCard.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                Constants.Mag_ReaderStatus = "Mag Connected";
                System.out.println("ACTION_GATT_Mag_CONNECTED");

                timerMag = new Timer();

                TimerTask tt = new TimerTask() {
                    @Override
                    public void run() {

                        //Execute below code only if Mag reader is  connected
                        if (Constants.Mag_ReaderStatus.equalsIgnoreCase("Mag Connected") || Constants.Mag_ReaderStatus.equalsIgnoreCase("Mag Discovered")) {
                            if (AppConstants.RebootHF_reader) {
                                System.out.println("ACTION_GATT_Mag_Reboot cmd");
                                mBluetoothLeService.writeRebootCharacteristic();
                                AppConstants.RebootHF_reader = false;
                            } else {
                                AppConstants.RebootHF_reader = false;
                                mBluetoothLeService.readCustomCharacteristic();
                            }

                        }
                    }

                };

                timerMag.schedule(tt, 0, 1000);


            } else if (LeServiceMagCard.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Constants.Mag_ReaderStatus = "Mag Disconnected";
                System.out.println("ACTION_GATT_Mag_DISCONNECTED");


            } else if (LeServiceMagCard.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Constants.Mag_ReaderStatus = "Mag Discovered";
                System.out.println("ACTION_GATT_Mag_DISCOVERED");

            } else if (LeServiceMagCard.ACTION_DATA_AVAILABLE.equals(action)) {
                Constants.Mag_ReaderStatus = "Mag Connected";
                System.out.println("ACTION_GATT_Mag_DATA_AVAILABLE");

                displayData(intent.getStringExtra(LeServiceMagCard.EXTRA_DATA));
            } else {
                Constants.Mag_ReaderStatus = "Mag Disconnected";
            }
        }
    };


    private void displayData(String data) {

        if (data != null) {

            //if(AppConstants.GenerateLogs)
            //AppConstants.WriteinFile("TRANSIT- HF Raw data"+data);

            try {
                String[] Seperate = data.split("\n");

                String last_val = "";
                if (Seperate.length > 1) {
                    last_val = Seperate[Seperate.length - 1];
                }

                last_val = last_val.replace("\\n", "");

                last_val = last_val.replace(" ","").trim();

                if (CommonUtils.ValidateFobkey(last_val)) {
                    sendHFDetailsToActivity(last_val);
                    mBluetoothLeService.writeCustomCharacteristic(0x01, "");
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

        if (timerMag != null)
            timerMag.cancel();

        unregisterReceiver(mGattUpdateReceiver);

    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LeServiceMagCard.ACTION_GATT_CONNECTED);
        intentFilter.addAction(LeServiceMagCard.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(LeServiceMagCard.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(LeServiceMagCard.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    private void sendHFDetailsToActivity(String newData) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("ServiceToActivityMagCard");
        broadcastIntent.putExtra("MagCardValue", newData);
        broadcastIntent.putExtra("Action", "MagReader");
        sendBroadcast(broadcastIntent);
    }


}
