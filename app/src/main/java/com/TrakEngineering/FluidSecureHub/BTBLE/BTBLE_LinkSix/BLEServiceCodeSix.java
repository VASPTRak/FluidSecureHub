package com.TrakEngineering.FluidSecureHub.BTBLE.BTBLE_LinkSix;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

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
import android.os.Build;
import android.os.IBinder;
import android.text.SpannableStringBuilder;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.AppConstants;
import com.TrakEngineering.FluidSecureHub.BTBLE.BT_BLE_Constants;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTConstants;
import com.TrakEngineering.FluidSecureHub.ConnectionDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public class BLEServiceCodeSix extends Service {
    private static final String TAG = "BLEService_Six";
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    StringBuilder sb6 = new StringBuilder();
    private int gt_notify_status = 0;
    BluetoothGatt gatt_notify;
    public String UUID_SERVICE = "725e0bc8-6f00-4d2d-a4af-96138ce599b6";
    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    public ConnectionDetector cd = new ConnectionDetector(BLEServiceCodeSix.this);

    public final static String ACTION_GATT_CONNECTED = TAG + ".ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = TAG + ".ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = TAG + ".ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = TAG + ".ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = TAG + ".EXTRA_DATA";

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            gt_notify_status = status;
            gatt_notify = gatt;
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " <onConnectionStateChange: status => " + status + "; newState => " + newState + ">");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.requestMtu(512);
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                BT_BLE_Constants.BT_BLE_LINK_SIX_STATUS = true;
                BT_BLE_Constants.BT_BLE_STATUS_STR_SIX = "Connected";
                //broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                BT_BLE_Constants.BT_BLE_LINK_SIX_STATUS = false;
                BT_BLE_Constants.BT_BLE_STATUS_STR_SIX = "Disconnect";
                //broadcastUpdate(intentAction);

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            gt_notify_status = status;
            gatt_notify = gatt;
            System.out.println("ACTION_GATT onServicesDiscovered");

            try {
                Thread.sleep(2000);
            } catch (Exception e) {
            }

            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + " <Finding BT service " + BT_BLE_Constants.UUID_SERVICE + " OR " + BT_BLE_Constants.UUID_SERVICE_BT + ">");
                    List<BluetoothGattService> services = gatt.getServices();
                    if (services != null) {
                        for (BluetoothGattService service : services) {
                            String suuid = String.valueOf(service.getUuid());
                            //AppConstants.writeInFile(BTLinkLeServiceCode.this, "Service -> " + suuid );
                            if (!suuid.equalsIgnoreCase(BT_BLE_Constants.UUID_SERVICE) && !suuid.equalsIgnoreCase(BT_BLE_Constants.UUID_SERVICE_BT) && suuid.equalsIgnoreCase(BT_BLE_Constants.UUID_SERVICE_FILE))
                                continue;

                            if (suuid.equalsIgnoreCase(BT_BLE_Constants.UUID_SERVICE)) {
                                UUID_SERVICE = BT_BLE_Constants.UUID_SERVICE;
                                //BT_BLE_Constants.IS_NEW_VERSION_LINK_SIX = false;
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + " <Found BT LINK (OLD)>");
                            }
                            if (suuid.equalsIgnoreCase(BT_BLE_Constants.UUID_SERVICE_BT)) {
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + " <Found BT LINK (New)>");
                                UUID_SERVICE = BT_BLE_Constants.UUID_SERVICE_BT;
                                //BT_BLE_Constants.IS_NEW_VERSION_LINK_SIX = true;
                            }
                            List<BluetoothGattCharacteristic> gattCharacteristics =
                                    service.getCharacteristics();

                            if (gattCharacteristics != null) {
                                // Loops through available Characteristics.
                                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                                    String cuuid = String.valueOf(gattCharacteristic.getUuid());
                                    if (!cuuid.equals(BT_BLE_Constants.UUID_CHAR) && !cuuid.equals(BT_BLE_Constants.UUID_CHAR_FILE))
                                        continue;

                                    final int charaProp = gattCharacteristic.getProperties();

                                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                        setCharacteristicNotification(gattCharacteristic, true);

                                        BluetoothGattCharacteristic init_gatt = gatt.getService(UUID.fromString(UUID_SERVICE)).getCharacteristic(UUID.fromString(BT_BLE_Constants.UUID_CHAR));
                                        for (BluetoothGattDescriptor descriptor : init_gatt.getDescriptors()) {
                                            Log.e(TAG, "BluetoothGattDescriptor 1: " + descriptor.getUuid().toString());
                                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                            gatt.writeDescriptor(descriptor);
                                            gatt.readRemoteRssi();
                                        }

                                    } else {
                                        Log.w(TAG, "Characteristic does not support notify");
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            gt_notify_status = status;
            gatt_notify = gatt;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            gt_notify_status = status;
            gatt_notify = gatt;
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " <RSSI: " + rssi + " dBm>");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            mBluetoothGatt.discoverServices();
        }
    };

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();

        //String str1 = bytesToHex(data);

        if (data != null && data.length > 0) {
            String Response = new String(data);
            SpannableStringBuilder spn = new SpannableStringBuilder(Response + '\n');

            // ======== Sometimes response of TDV command contains single '$' ===========
            //BLEService_Six broadcastUpdate ==> Response:>> {"T":"77637","D":"23
            //BLEService_Six broadcastUpdate ==> Response:>> 1013165559","V":""}$
            //BLEService_Six broadcastUpdate ==> Response:>> $ �?   )^����?���?
            // ===========================================================================

            if (Response.contains("$$") || sb6.toString().trim().contains("$$")) {
                if (sb6.toString().trim().contains("$$")) {
                    Response = sb6.toString().trim();
                }
                String res = Response.replace("$$", "");

                if (!res.trim().isEmpty()) {
                    if (sb6.toString().trim().contains("$$")) {
                        sb6.setLength(0);
                    }
                    sb6.append(res.trim());
                }

                String finalResp = sb6.toString().trim();
                try {
                    if (finalResp.contains("{")) {
                        finalResp = finalResp.substring(finalResp.indexOf("{")); // To remove extra characters before the first curly bracket (if any)
                    }
                    if (finalResp.contains("}")) {
                        finalResp = finalResp.substring(0, (finalResp.lastIndexOf("}") + 1)); // To remove extra characters after the last curly bracket (if any)
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                intent.putExtra(EXTRA_DATA, finalResp);
                sendBroadcast(intent);
                sb6.setLength(0);
            } else {
                if (BT_BLE_Constants.IS_NEW_VERSION_LINK_SIX || BTConstants.FOR_OSCILLOSCOPE || BT_BLE_Constants.CURRENT_COMMAND_LINK_SIX.contains(BTConstants.P_TYPE_COMMAND)) {
                    sb6.append(Response);
                } else {
                    // For old version Link response
                    sb6.setLength(0);
                    intent.putExtra(EXTRA_DATA, spn.toString());
                    sendBroadcast(intent);
                }
            }
        }
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
        public BLEServiceCodeSix getService() {
            return BLEServiceCodeSix.this;
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

    private final IBinder mBinder = new BLEServiceCodeSix.LocalBinder();

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


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mBluetoothGatt = device.connectGatt(this, false, mGattCallback, TRANSPORT_LE);
            } else {
                mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
            }

            Log.d(TAG, "Trying to create a new connection.");

            mBluetoothDeviceAddress = address;
            mConnectionState = STATE_CONNECTING;


        } catch (Exception e) {
            e.printStackTrace();
            BT_BLE_Constants.BT_BLE_LINK_SIX_STATUS = false;
            BT_BLE_Constants.BT_BLE_STATUS_STR_SIX = "Disconnect";
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
        //mBluetoothGatt = null;
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

    public void writeCustomCharacteristic(String bleCommand) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            BT_BLE_Constants.CURRENT_COMMAND_LINK_SIX = "";
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString(UUID_SERVICE));

        if (mCustomService == null) {
            BT_BLE_Constants.CURRENT_COMMAND_LINK_SIX = "";
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " LeServiceCode ~~~~~~~~~" + bleCommand + " writeCustomCharacteristic Char Not found:" + BT_BLE_Constants.UUID_CHAR);
            return;
        }
        /*if (AppConstants.GENERATE_LOGS)
            AppConstants.writeInFile(TAG + " writeCustomCharacteristic ~~~~~~~~~ " + bleCommand);*/
        if (!bleCommand.equalsIgnoreCase(BTConstants.FD_CHECK_COMMAND)) {
            BT_BLE_Constants.CURRENT_COMMAND_LINK_SIX = bleCommand;
        }
        byte[] strBytes = bleCommand.getBytes();

        BluetoothGattCharacteristic mWriteCharacteristic = null;
        mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString(BT_BLE_Constants.UUID_CHAR));
        mWriteCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mWriteCharacteristic.setValue(strBytes);

        if (mBluetoothGatt != null && mBluetoothGatt.writeCharacteristic(mWriteCharacteristic)) {
            //AppConstants.writeInFile(BTLinkLeServiceCode.this,"LeServiceCode ~~~~~~~~~"+bleCommand  + " Write Characteristics successfully!");

            if (bleCommand.contains("OFF")) {
                //BTLinkLeServiceCode.count_relayOff=0;
            }

            if (bleCommand.contains("info")) {
                //BTLinkLeServiceCode.count_infocmd=0;
            }

        } else {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " LeServiceCode ~~~~~~~~~" + bleCommand + " Failed to write Characteristics");

            if(bleCommand.contains("OFF"))
            {
                //count_relayOff--;
            }

            if(bleCommand.contains("info"))
            {
                //count_infocmd--;
            }

        }
        mBluetoothGatt.readRemoteRssi();
    }

    public boolean writeFileCharacteristic() {
        boolean result = false;
        int BUFFER_SIZE = 256; //497; //256; // 490 // mtu

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }

        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString(BT_BLE_Constants.UUID_SERVICE_FILE));

        if (mCustomService == null) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(AppConstants.LOG_UPGRADE_BT_BLE + "-" + TAG + " getService Not found: " + BT_BLE_Constants.UUID_SERVICE_FILE);
            return false;
        }

        try {
            String LocalPath = getApplicationContext().getExternalFilesDir(AppConstants.FOLDER_BIN) + "/" + AppConstants.UP_UPGRADE_FILE_NAME;

            File file = new File(LocalPath);

            long file_size = file.length();
            long tempFileSize = file_size;

            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(AppConstants.LOG_UPGRADE_BT_BLE + "-" + TAG + " <File Size: " + file_size + ">");

            InputStream inputStream = new FileInputStream(file);
            byte[] bufferBytes = new byte[BUFFER_SIZE];

            //Thread.sleep(5000);

            if (inputStream != null) {
                long bytesWritten = 0;
                int amountOfBytesRead;

                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(AppConstants.LOG_UPGRADE_BT_BLE + "-" + TAG + " Upload (" + AppConstants.UP_UPGRADE_FILE_NAME + ") started...");

                while ((amountOfBytesRead = inputStream.read(bufferBytes)) != -1) {

                    bytesWritten += amountOfBytesRead;
                    int progressValue = (int) (100 * ((double) bytesWritten) / ((double) file_size));
                    BT_BLE_Constants.BT_BLE_UPGRADE_PROGRESS_VALUE = String.valueOf(progressValue);

                    BluetoothGattCharacteristic mWriteCharacteristic = null;
                    mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString(BT_BLE_Constants.UUID_CHAR_FILE));
                    mWriteCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                    mWriteCharacteristic.setValue(bufferBytes);

                    if (mBluetoothGatt != null && mBluetoothGatt.writeCharacteristic(mWriteCharacteristic)) {
                        Log.w(TAG, String.valueOf(tempFileSize));
                    } else {
                        break;
                    }

                    tempFileSize = tempFileSize - BUFFER_SIZE;
                    if (tempFileSize < BUFFER_SIZE) {
                        int i = (int) (long) tempFileSize;
                        if (i > 0) {
                            bufferBytes = new byte[i];
                        }
                        result = true;
                    }

                    try {
                        Thread.sleep(10);
                    } catch (Exception e) {
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(AppConstants.LOG_UPGRADE_BT_BLE + "-" + TAG + " Thread exception: " + e.getMessage() + " (Progress: " + progressValue + ")");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(AppConstants.LOG_UPGRADE_BT_BLE + "-" + TAG + " writeFileCharacteristic Exception: " + e.getMessage());
        }
        return result;
    }
}
