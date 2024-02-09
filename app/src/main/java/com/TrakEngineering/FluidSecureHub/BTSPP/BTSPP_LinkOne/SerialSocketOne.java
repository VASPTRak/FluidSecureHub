package com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkOne;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.AppConstants;
import com.TrakEngineering.FluidSecureHub.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Executors;

public class SerialSocketOne implements Runnable {

    static final String INTENT_ACTION_DISCONNECT = BuildConfig.APPLICATION_ID + ".Disconnect";
    private static final UUID BLUETOOTH_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BroadcastReceiver disconnectBroadcastReceiver;
    private static final String TAG = SerialSocketOne.class.getSimpleName();

    private Context context;
    private SerialListenerOne listener;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private boolean connected;

    public SerialSocketOne(Context context, BluetoothDevice device) {
        if(context instanceof Activity)
            throw new InvalidParameterException("expected non UI context");
        this.context = context;
        this.device = device;
        disconnectBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(listener != null)
                    listener.onSerialIoErrorOne(new IOException("background disconnect"), 5);
                disconnect(); // disconnect now, else would be queued until UI re-attached
            }
        };
    }

    String getName() {
        return device.getName() != null ? device.getName() : device.getAddress();
    }

    /**
     * connect-success and most connect-errors are returned asynchronously to listener
     */
    void connect(SerialListenerOne listener) throws IOException {
        this.listener = listener;
        context.registerReceiver(disconnectBroadcastReceiver, new IntentFilter(INTENT_ACTION_DISCONNECT));
        Executors.newSingleThreadExecutor().submit(this);
    }

    void disconnect() {
        listener = null; // ignore remaining data and errors
        // connected = false; // run loop will reset connected
        if(socket != null) {
            try {
                socket.close();
            } catch (Exception ignored) {
            }
            socket = null;
        }
        try {
            context.unregisterReceiver(disconnectBroadcastReceiver);
        } catch (Exception ignored) {
        }
    }

    void write(byte[] data) throws IOException {
        if (!connected)
            throw new IOException("not connected");
        socket.getOutputStream().write(data);
        socket.getOutputStream().flush();

    }

    void readPulse() throws IOException {
        if (!connected)
            throw new IOException("not connected");
        try {
            Log.i(TAG, "BTLink_1:InreadPulse:");
            if (socket.isConnected())
            Log.i(TAG, "BTLink_1:InreadPulse:socket connected");

            try {
                InputStream socketInputStream = socket.getInputStream();
                if (socketInputStream.available() > 0){
                    byte[] buffer = new byte[1024];
                    int len;
                    //noinspection InfiniteLoopStatement
                    while (true) {
                        len = socket.getInputStream().read(buffer);
                        byte[] data = Arrays.copyOf(buffer, len);
                        if(listener != null)
                            listener.onSerialReadOne(data);
                        Log.i(TAG, "BTLink_1:InreadPulse data: "+data.toString());
                        if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "BTLink_1:InreadPulse data: "+data.toString());
                    }

                }else{
                    Log.i(TAG, "BTLink_1:InreadPulse socketInputStream not avilable ");
                    if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "BTLink_1:InreadPulse socketInputStream not avilable ");
                }

            }catch (Exception e){
                e.printStackTrace();
                Log.i(TAG, "BTLink_1:InreadPulse:Exception:"+e.toString());
                if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "BTLink_1:InreadPulse:Exception:"+e.toString());
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() { // connect & read

        try {
            socket = device.createRfcommSocketToServiceRecord(BLUETOOTH_SPP);
            socket.connect();
            if (listener != null)
                listener.onSerialConnectOne();
        } catch (Exception e) {
            if (listener != null) {
                listener.onSerialConnectErrorOne(e);
            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile("<" + TAG + " Connect Exception: " + e.getMessage() + ">");
            }
            try {
                socket.close();
            } catch (Exception ignored) {
            }
            socket = null;
            return;
        }
        connected = true;
        try {
            byte[] buffer = new byte[1024];
            int len;
            //noinspection InfiniteLoopStatement
            while (true) {
                len = socket.getInputStream().read(buffer);
                byte[] data = Arrays.copyOf(buffer, len);
                if (listener != null)
                    listener.onSerialReadOne(data);
            }
        } catch (Exception e) {
            connected = false;
            if (listener != null) {
                listener.onSerialIoErrorOne(e, 6);
            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile("<" + TAG + " Serial IO Exception: " + e.getMessage() + ">");
            }
            try {
                socket.close();
            } catch (Exception ignored) {
            }
            socket = null;
        }
    }

}
