package com.TrakEngineering.FluidSecureHub.QRCodeGAtt;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.TrakEngineering.FluidSecureHub.AppConstants;
import com.TrakEngineering.FluidSecureHub.BuildConfig;

import java.util.List;
import java.util.UUID;

public class LeServiceQRCode extends Service {
    private final static String TAG = LeServiceQRCode.class.getSimpleName();

    public int cnt123 = 0;

    private String BLE_Service = "0000ee00-0000-1000-8000-00805f9b34fb";
    private String BLE_char = "000000ee-0000-1000-8000-00805f9b34fb";

    private String bolong_UUID_service = "0000ee00-0000-1000-8000-00805f9b34fb";
    private String bolong_UUID_char = "000000ee-0000-1000-8000-00805f9b34fb";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private int gt_notify_status = 0;
    BluetoothGatt gatt_notify;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;


    public final static String ACTION_GATT_CONNECTED = BuildConfig.APPLICATION_ID + ".QRLe.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = BuildConfig.APPLICATION_ID + ".QRLe.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = BuildConfig.APPLICATION_ID + ".QRLe.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = BuildConfig.APPLICATION_ID + ".QRLe.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = BuildConfig.APPLICATION_ID + ".QRLe.EXTRA_DATA";

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            gt_notify_status = status;
            gatt_notify = gatt;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.requestMtu(512);
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            gt_notify_status = status;
            gatt_notify = gatt;
            System.out.println("ACTION_GATT onServicesDiscovered");

            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService service : services) {
                   String suuid = String.valueOf(service.getUuid());
                    if (!suuid.equals(bolong_UUID_service))
                        continue;

                    List<BluetoothGattCharacteristic> gattCharacteristics =
                            service.getCharacteristics();

                    // Loops through available Characteristics.
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        String cuuid = String.valueOf(gattCharacteristic.getUuid());
                        if (!cuuid.equals(bolong_UUID_char))
                            continue;

                        final int charaProp = gattCharacteristic.getProperties();

                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                setCharacteristicNotification(gattCharacteristic, true);

                            BluetoothGattCharacteristic init_gatt = gatt.getService(UUID.fromString(bolong_UUID_service)).getCharacteristic(UUID.fromString(bolong_UUID_char));
                            for (BluetoothGattDescriptor descriptor:init_gatt.getDescriptors()){
                                Log.e(TAG, "BluetoothGattDescriptor: "+descriptor.getUuid().toString());
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gatt.writeDescriptor(descriptor);
                            }

                        } else {
                            Log.w(TAG, "Characteristic does not support notify");
                        }
                    }
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            gt_notify_status = status;
            gatt_notify = gatt;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.i(TAG, "New MTU is " + mtu);
            Log.i(TAG, "Attempting to start service discovery:" +
                    mBluetoothGatt.discoverServices());
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            gt_notify_status = status;
            gatt_notify = gatt;
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();

        String str1 = bytesToHex(data);

        System.out.println("QR data1----"+str1);

        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);

            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));

            System.out.println("QR data2----"+stringBuilder.toString());
            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
        }

        sendBroadcast(intent);
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public class LocalBinder extends Binder {
        public LeServiceQRCode getService() {
            return LeServiceQRCode.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LeServiceQRCode.LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {

        try {

            if (mBluetoothAdapter == null || address == null) {
                Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
                return false;
            }

            // Previously connected device.  Try to reconnect.
            if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                    && mBluetoothGatt != null) {
                Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
                if (mBluetoothGatt.connect()) {
                    mConnectionState = STATE_CONNECTING;
                    return true;
                } else {
                    return false;
                }
            }

            final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            if (device == null) {
                Log.w(TAG, "Device not found.  Unable to connect.");
                return false;
            }
            // We want to directly connect to the device, so we are setting the autoConnect
            // parameter to false.
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
            Log.d(TAG, "Trying to create a new connection.");
            mBluetoothDeviceAddress = address;
            mConnectionState = STATE_CONNECTING;


        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " Exception:" + e.toString());
            return false;
        }
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    public void readCustomCharacteristic(boolean bleLFUpdateFlag) {
        try {

            if (mBluetoothAdapter == null || mBluetoothGatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return;
            }
            BluetoothGattService mCustomService;
            /*check if the service is available on the device*/
            if (!bleLFUpdateFlag)
                mCustomService = mBluetoothGatt.getService(UUID.fromString(bolong_UUID_service));
            else {
                System.out.println("BLE_Service:" + BLE_Service);
                mCustomService = mBluetoothGatt.getService(UUID.fromString(BLE_Service));
            }
            if (mCustomService == null) {
                Log.w(TAG, "Custom BLE Service not found");
                //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile("LeServiceQRCard ~~~~~~~~~" + "readCustomCharacteristic Custom BLE Service not found. bleLFUpdateFlag"+bleLFUpdateFlag);
                //  Toast.makeText(getApplicationContext(),"Not found: "+bolong_UUID_char, Toast.LENGTH_LONG).show();
                return;
            }
            /*get the read characteristic from the service*/
            BluetoothGattCharacteristic mReadCharacteristic = null;
            if (!bleLFUpdateFlag)
                mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString(bolong_UUID_char));
            else
                mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString(BLE_char));
            if (mBluetoothGatt.readCharacteristic(mReadCharacteristic) == false) {
                Log.w(TAG, "Failed to read characteristic");
                //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile("LeServiceQRCard ~~~~~~~~~" + "readCustomCharacteristic Failed to read characteristic");
                // Toast.makeText(getApplicationContext(),"Failed to Read Characteristics: ", Toast.LENGTH_LONG).show();


            } else {
                Log.w(TAG, "Read Characteristics successfully");
                //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile("LeServiceQRCard ~~~~~~~~~" + "Read Characteristics successfully");
                //  Toast.makeText(getApplicationContext(),"Read Characteristics successfully!", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile("LeServiceQRCard_Vehicle ~~~~~~~~~" + "Read Characteristics Ex-" + e.getMessage());

        }
    }

    public void writeCustomCharacteristic(int value, String bleCommand, boolean bleLFUpdateFlag) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/
        BluetoothGattService mCustomService = null;

        mCustomService = mBluetoothGatt.getService(UUID.fromString(bolong_UUID_service));//"00001110-0000-1000-8000-00805f9b34fb"

        if (mCustomService == null) {
            if (AppConstants.GENERATE_LOGS) AppConstants.writeInFile("LeServiceQRCard ~~~~~~~~~" + "writeCustomCharacteristic Char Not found:" + bolong_UUID_char);
            return;
        }

        BluetoothGattCharacteristic mWriteCharacteristic = null;
        mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString(bolong_UUID_char));
        mWriteCharacteristic.setValue(value, BluetoothGattCharacteristic.FORMAT_UINT8, 0);

        if (mBluetoothGatt.writeCharacteristic(mWriteCharacteristic)) {
            Log.w(TAG, "Write Characteristics successfully!");
           // if (AppConstants.GENERATE_LOGS) AppConstants.writeInFile("LeServiceQRCard ~~~~~~~~~" + "Write Characteristics successfully!");
        } else {
            Log.w(TAG, "Failed to write Characteristics");
            //if (AppConstants.GENERATE_LOGS) AppConstants.writeInFile("LeServiceQRCard ~~~~~~~~~" + "Failed to write Characteristics");
        }
    }


    public void writeRebootCharacteristic() {

        byte value[] = {0x72, 0x62};

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/
        BluetoothGattService mCustomService = null;

        mCustomService = mBluetoothGatt.getService(UUID.fromString(BLE_Service));

        if (mCustomService == null) {
            Toast.makeText(getApplicationContext(), "Not found: " + BLE_Service, Toast.LENGTH_LONG).show();
            return;
        }


        BluetoothGattCharacteristic mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString(BLE_char));
        //mWriteCharacteristic.setValue("rb");
        mWriteCharacteristic.setValue(value);


        if (mBluetoothGatt.writeCharacteristic(mWriteCharacteristic)) {
            // Toast.makeText(getApplicationContext(),"Write Characteristics successfully!", Toast.LENGTH_LONG).show();
            //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile("LeServiceQRCard ~~~~~~~~~" + "Write Characteristics successfully!");
            //Toast.makeText(getApplicationContext(), "Reboot success", Toast.LENGTH_SHORT).show();


            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //Restart QR Reader service
            if (!mBluetoothDeviceAddress.isEmpty())
                connect(mBluetoothDeviceAddress);

        } else {
            // Toast.makeText(getApplicationContext(),"Failed to write Characteristics", Toast.LENGTH_LONG).show();
            //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile("LeServiceQRCard ~~~~~~~~~" + "Failed to write Characteristics");
            //Toast.makeText(getApplicationContext(), "Reboot fail", Toast.LENGTH_SHORT).show();
        }
    }

    public void notifyfun(boolean b) {

        try {
            List<BluetoothGattService> services = getSupportedGattServices();
            for (BluetoothGattService service : services) {
                String suuid = String.valueOf(service.getUuid());
                if (!suuid.equals(bolong_UUID_service))
                    continue;

                List<BluetoothGattCharacteristic> gattCharacteristics =
                        service.getCharacteristics();

                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    String cuuid = String.valueOf(gattCharacteristic.getUuid());
                    if (!cuuid.equals(bolong_UUID_char))
                        continue;

                    final int charaProp = gattCharacteristic.getProperties();

                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        setCharacteristicNotification(gattCharacteristic, b);

                        BluetoothGattCharacteristic init_gatt = gatt_notify.getService(UUID.fromString(bolong_UUID_service)).getCharacteristic(UUID.fromString(bolong_UUID_char));
                        for (BluetoothGattDescriptor descriptor : init_gatt.getDescriptors()) {
                            Log.e(TAG, "BluetoothGattDescriptor: " + descriptor.getUuid().toString());
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt_notify.writeDescriptor(descriptor);
                        }

                    } else {
                        Log.w(TAG, "Characteristic does not support notify");
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void notifyClear() {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        /*check if the service is available on the device*/
        BluetoothGattService mCustomService = null;

        mCustomService = mBluetoothGatt.getService(UUID.fromString(bolong_UUID_service));

        if (mCustomService == null) {
            if (AppConstants.GENERATE_LOGS) AppConstants.writeInFile("LeServiceQRCard ~~~~~~~~~" + "writeCustomCharacteristic Char Not found:" + bolong_UUID_char);
            return;
        }

        BluetoothGattCharacteristic mWriteCharacteristic = null;
        mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString(bolong_UUID_char));

        final int charaProp = mWriteCharacteristic.getProperties();

        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            setCharacteristicNotification(mWriteCharacteristic, false);

            BluetoothGattCharacteristic init_gatt = mBluetoothGatt.getService(UUID.fromString(bolong_UUID_service)).getCharacteristic(UUID.fromString(bolong_UUID_char));
            for (BluetoothGattDescriptor descriptor : init_gatt.getDescriptors()) {
                Log.e(TAG, "BluetoothGattDescriptor: " + descriptor.getUuid().toString());
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }

        } else {
            Log.w(TAG, "Characteristic does not support notify");
        }
    }

}

