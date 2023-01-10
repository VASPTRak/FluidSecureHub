package com.TrakEngineering.FluidSecureHub.BTSPP;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.AppConstants;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkOne.SerialListenerOne;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkOne.SerialSocketOne;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkTwo.SerialListenerTwo;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkTwo.SerialSocketTwo;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkThree.SerialListenerThree;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkThree.SerialSocketThree;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkFour.SerialListenerFour;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkFour.SerialSocketFour;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkFive.SerialListenerFive;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkFive.SerialSocketFive;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkSix.SerialListenerSix;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkSix.SerialSocketSix;

import static com.TrakEngineering.FluidSecureHub.WelcomeActivity.service1;
import static com.TrakEngineering.FluidSecureHub.WelcomeActivity.service2;
import static com.TrakEngineering.FluidSecureHub.WelcomeActivity.service3;
import static com.TrakEngineering.FluidSecureHub.WelcomeActivity.service4;
import static com.TrakEngineering.FluidSecureHub.WelcomeActivity.service5;
import static com.TrakEngineering.FluidSecureHub.WelcomeActivity.service6;

public class BTSPPMain implements SerialListenerOne, SerialListenerTwo, SerialListenerThree , SerialListenerFour, SerialListenerFive, SerialListenerSix {

    public Activity activity;
    private static final String TAG = AppConstants.LOG_TXTN_BT + "-"; //BTSPPMain.class.getSimpleName();
    private String newline = "\r\n";
    //private String deviceAddress1 = ""; //80:7D:3A:A4:67:22
    //private String deviceAddress2 = "";
    StringBuilder sb1 = new StringBuilder();
    StringBuilder sb2 = new StringBuilder();
    StringBuilder sb3 = new StringBuilder();
    StringBuilder sb4 = new StringBuilder();
    StringBuilder sb5 = new StringBuilder();
    StringBuilder sb6 = new StringBuilder();

    public void CheckForStoredMacAddress() {

        SharedPreferences sharedPref = activity.getSharedPreferences("StoreBTDeviceInfo", Context.MODE_PRIVATE);
        String device1_name = sharedPref.getString("device1_name", "");
        String device1_mac = sharedPref.getString("device1_mac", "");
        String device2_name = sharedPref.getString("device2_name", "");
        String device2_mac = sharedPref.getString("device2_mac", "");

        //Link one
        if (device1_mac != null || !device1_mac.isEmpty()) {

            //edt_mac_address.setText(device1_mac);
            //tv_mac_address.setText("Mac Address:");
            //deviceAddress1 = edt_mac_address.getText().toString().trim();

        } else {
            //edt_mac_address.setText("");
            //tv_mac_address.setText("Mac Address:");
        }

    }

    //region Link One code
    //Link One code begins....
    @Override
    public void onSerialConnectOne() {
        BTConstants.BTLinkOneStatus = true;
        status1("Connected");
    }

    @Override
    public void onSerialConnectErrorOne(Exception e) {
        BTConstants.BTLinkOneStatus = false;
        status1("Disconnect");
        e.printStackTrace();
        if (BTConstants.IsFileUploadCompleted) {
            try {
                connect1();
            } catch (Exception ex) {
                Log.e("Error: ", ex.getMessage());
            }
        }
        //AppConstants.WriteinFile(TAG + " onSerialConnectErrorOne Status: " + e.getMessage());
    }

    @Override
    public void onSerialReadOne(byte[] data) {
        receive1(data);
    }

    @Override
    public void onSerialIoErrorOne(Exception e) {
        BTConstants.BTLinkOneStatus = false;
        status1("Disconnect");
        e.printStackTrace();
        if (BTConstants.IsFileUploadCompleted) {
            try {
                connect1();
            } catch (Exception ex) {
                Log.e("Error: ", ex.getMessage());
            }
        }
        //AppConstants.WriteinFile(TAG + " onSerialIoErrorOne Status: " + e.getMessage());
    }

    public void connect1() {
        try {

            if (BTConstants.deviceAddress1 != null && !BTConstants.deviceAddress1.isEmpty()) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(BTConstants.deviceAddress1);
                status1("Connecting...");
                //BTConstants.BTLinkOneStatus = false;
                SerialSocketOne socket = new SerialSocketOne(activity.getApplicationContext(), device);
                service1.connect(socket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send1(String str) {
        if (!BTConstants.BTLinkOneStatus) {
            BTConstants.CurrentCommand_LinkOne = "";
            Log.i(TAG, "BTLink 1: Link not connected");
            //Toast.makeText(activity, "BTLink 1: Link not connected", Toast.LENGTH_SHORT).show();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: Link not connected");
            return;
        }
        try {
            //Log command sent:str
            BTConstants.CurrentCommand_LinkOne = str;
            Log.i(TAG, "BTLink 1: Requesting..." + str);
            byte[] data = (str + newline).getBytes();
            service1.write(data);
        } catch (Exception e) {
            onSerialIoErrorOne(e);
        }
    }

    public void sendBytes1(byte[] data) {
        if (!BTConstants.BTLinkOneStatus) {
            BTConstants.CurrentCommand_LinkOne = "";
            Log.i(TAG, "BTLink 1: Link not connected");
            //Toast.makeText(activity, "BTLink 1: Link not connected", Toast.LENGTH_SHORT).show();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 1: Link not connected");
            return;
        }
        try {
            service1.write(data);
        } catch (Exception e) {
            onSerialIoErrorOne(e);
        }
    }

    public void readPulse1() {
        if (!BTConstants.BTLinkOneStatus) {
            BTConstants.CurrentCommand_LinkOne = "";
            Log.i(TAG, "BTLink 1: Link not connected");
            //Toast.makeText(activity, "BTLink 1: Link not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            service1.readPulse();
        } catch (Exception e) {
            onSerialIoErrorOne(e);
        }
    }

    public void receive1(byte[] data) {
        String Response = new String(data);
        SpannableStringBuilder spn = new SpannableStringBuilder(Response + '\n');
        Log.i(TAG, "BTLink 1: Request>>" + BTConstants.CurrentCommand_LinkOne);
        Log.i(TAG, "BTLink 1: Response>>" + spn.toString());

        //==========================================
        if (BTConstants.CurrentCommand_LinkOne.equalsIgnoreCase(BTConstants.info_cmd) && Response.contains("records")) {
            BTConstants.isNewVersionLinkOne = true;
        }
        if (Response.contains("$$")) {
            String res = Response.replace("$$", "");
            try {
                if (res.contains("}")) {
                    res = res.substring(0, (res.lastIndexOf("}") + 1)); // To remove extra characters after the last curly bracket (if any)
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!res.trim().isEmpty()) {
                sb1.append(res.trim());
            }
            sendBroadcastIntentFromLinkOne(sb1.toString());
            sb1.setLength(0);
        } else {
            if (BTConstants.isNewVersionLinkOne || BTConstants.CurrentCommand_LinkOne.contains(BTConstants.p_type_command)) {
                sb1.append(Response);
            } else {
                // For old version Link response
                sb1.setLength(0);
                sendBroadcastIntentFromLinkOne(spn.toString());
            }
        }
    }

    public void sendBroadcastIntentFromLinkOne(String spn) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("BroadcastBlueLinkOneData");
        broadcastIntent.putExtra("Request", BTConstants.CurrentCommand_LinkOne);
        broadcastIntent.putExtra("Response", spn.trim());
        broadcastIntent.putExtra("Action", "BlueLinkOne");
        activity.sendBroadcast(broadcastIntent);
    }

    public void status1(String str) {
        Log.i(TAG, "Status1:" + str);
        BTConstants.BTStatusStrOne = str;
    }
    //Link one code ends.......
    //endregion

    //region Link Two code
    //Link Two code begins..
    @Override
    public void onSerialConnectTwo() {
        BTConstants.BTLinkTwoStatus = true;
        status2("Connected");
    }

    @Override
    public void onSerialConnectErrorTwo(Exception e) {
        BTConstants.BTLinkTwoStatus = false;
        status2("Disconnect");
        e.printStackTrace();
        if (BTConstants.IsFileUploadCompleted) {
            try {
                connect2();
            } catch (Exception ex) {
                Log.e("Error: ", ex.getMessage());
            }
        }
        //AppConstants.WriteinFile(TAG + " onSerialConnectErrorTwo Status: " + e.getMessage());
    }

    @Override
    public void onSerialReadTwo(byte[] data) {
        receive2(data);
    }

    @Override
    public void onSerialIoErrorTwo(Exception e) {
        BTConstants.BTLinkTwoStatus = false;
        status2("Disconnect");
        e.printStackTrace();
        if (BTConstants.IsFileUploadCompleted) {
            try {
                connect2();
            } catch (Exception ex) {
                Log.e("Error: ", ex.getMessage());
            }
        }
        //AppConstants.WriteinFile(TAG + " onSerialIoErrorTwo Status: " + e.getMessage());
    }

    public void connect2() {
        try {

            if (BTConstants.deviceAddress2 != null && !BTConstants.deviceAddress2.isEmpty()) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(BTConstants.deviceAddress2);
                status2("Connecting...");
                //BTConstants.BTLinkTwoStatus = false;
                SerialSocketTwo socket = new SerialSocketTwo(activity.getApplicationContext(), device);
                service2.connect(socket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send2(String str) {
        if (!BTConstants.BTLinkTwoStatus) {
            BTConstants.CurrentCommand_LinkTwo = "";
            Log.i(TAG, "BTLink 2: Link not connected");
            //Toast.makeText(activity, "BTLink 2: Link not connected", Toast.LENGTH_SHORT).show();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 2: Link not connected");
            return;
        }
        try {
            //Log command sent:str
            BTConstants.CurrentCommand_LinkTwo = str;
            Log.i(TAG, "BTLink 2: Requesting..." + str);
            byte[] data = (str + newline).getBytes();
            service2.write(data);
        } catch (Exception e) {
            onSerialIoErrorTwo(e);
        }
    }

    public void sendBytes2(byte[] data) {
        if (!BTConstants.BTLinkTwoStatus) {
            BTConstants.CurrentCommand_LinkTwo = "";
            Log.i(TAG, "BTLink 2: Link not connected");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 2: Link not connected");
            return;
        }
        try {
            service2.write(data);
        } catch (Exception e) {
            onSerialIoErrorTwo(e);
        }
    }

    public void receive2(byte[] data) {
        String Response = new String(data);
        SpannableStringBuilder spn = new SpannableStringBuilder(Response + '\n');
        Log.i(TAG, "BTLink 2: Request>>" + BTConstants.CurrentCommand_LinkTwo);
        Log.i(TAG, "BTLink 2: Response>>" + spn.toString());

        //==========================================
        if (BTConstants.CurrentCommand_LinkTwo.equalsIgnoreCase(BTConstants.info_cmd) && Response.contains("records")) {
            BTConstants.isNewVersionLinkTwo = true;
        }
        if (Response.contains("$$")) {
            String res = Response.replace("$$", "");
            try {
                if (res.contains("}")) {
                    res = res.substring(0, (res.lastIndexOf("}") + 1)); // To remove extra characters after the last curly bracket (if any)
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!res.trim().isEmpty()) {
                sb2.append(res.trim());
            }
            sendBroadcastIntentFromLinkTwo(sb2.toString());
            sb2.setLength(0);
        } else {
            if (BTConstants.isNewVersionLinkTwo || BTConstants.CurrentCommand_LinkTwo.contains(BTConstants.p_type_command)) {
                sb2.append(Response);
            } else {
                // For old version Link response
                sb2.setLength(0);
                sendBroadcastIntentFromLinkTwo(spn.toString());
            }
        }
    }

    public void sendBroadcastIntentFromLinkTwo(String spn) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("BroadcastBlueLinkTwoData");
        broadcastIntent.putExtra("Request", BTConstants.CurrentCommand_LinkTwo);
        broadcastIntent.putExtra("Response", spn.trim());
        broadcastIntent.putExtra("Action", "BlueLinkTwo");
        activity.sendBroadcast(broadcastIntent);
    }

    public void status2(String str) {
        Log.i(TAG, "Status2:" + str);
        BTConstants.BTStatusStrTwo = str;
    }
    //endregion

    //region Link Three code
    //Link Three code begins..
    @Override
    public void onSerialConnectThree() {
        BTConstants.BTLinkThreeStatus = true;
        status3("Connected");
    }

    @Override
    public void onSerialConnectErrorThree(Exception e) {
        BTConstants.BTLinkThreeStatus = false;
        status3("Disconnect");
        e.printStackTrace();
        if (BTConstants.IsFileUploadCompleted) {
            try {
                connect3();
            } catch (Exception ex) {
                Log.e("Error: ", ex.getMessage());
            }
        }
        //AppConstants.WriteinFile(TAG + " onSerialConnectErrorThree Status: " + e.getMessage());
    }

    @Override
    public void onSerialReadThree(byte[] data) {
        receive3(data);
    }

    @Override
    public void onSerialIoErrorThree(Exception e) {
        BTConstants.BTLinkThreeStatus = false;
        status3("Disconnect");
        e.printStackTrace();
        if (BTConstants.IsFileUploadCompleted) {
            try {
                connect3();
            } catch (Exception ex) {
                Log.e("Error: ", ex.getMessage());
            }
        }
        //AppConstants.WriteinFile(TAG + " onSerialIoErrorThree Status: " + e.getMessage());
    }

    public void connect3() {
        try {

            if (BTConstants.deviceAddress3 != null && !BTConstants.deviceAddress3.isEmpty()) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(BTConstants.deviceAddress3);
                status3("Connecting...");
                //BTConstants.BTLinkTwoStatus = false;
                SerialSocketThree socket = new SerialSocketThree(activity.getApplicationContext(), device);
                service3.connect(socket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send3(String str) {
        if (!BTConstants.BTLinkThreeStatus) {
            BTConstants.CurrentCommand_LinkThree = "";
            Log.i(TAG, "BTLink 3: Link not connected");
            //Toast.makeText(activity, "BTLink 3: Link not connected", Toast.LENGTH_SHORT).show();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 3: Link not connected");
            return;
        }
        try {
            //Log command sent:str
            BTConstants.CurrentCommand_LinkThree = str;
            Log.i(TAG, "BTLink 3: Requesting..." + str);
            byte[] data = (str + newline).getBytes();
            service3.write(data);
        } catch (Exception e) {
            onSerialIoErrorThree(e);
        }
    }

    public void sendBytes3(byte[] data) {
        if (!BTConstants.BTLinkThreeStatus) {
            BTConstants.CurrentCommand_LinkThree = "";
            Log.i(TAG, "BTLink 3: Link not connected");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 3: Link not connected");
            return;
        }
        try {
            service3.write(data);
        } catch (Exception e) {
            onSerialIoErrorThree(e);
        }
    }

    public void receive3(byte[] data) {
        String Response = new String(data);
        SpannableStringBuilder spn = new SpannableStringBuilder(Response + '\n');
        Log.i(TAG, "BTLink 3: Request>>" + BTConstants.CurrentCommand_LinkThree);
        Log.i(TAG, "BTLink 3: Response>>" + spn.toString());

        //==========================================
        if (BTConstants.CurrentCommand_LinkThree.equalsIgnoreCase(BTConstants.info_cmd) && Response.contains("records")) {
            BTConstants.isNewVersionLinkThree = true;
        }
        if (Response.contains("$$")) {
            String res = Response.replace("$$", "");
            try {
                if (res.contains("}")) {
                    res = res.substring(0, (res.lastIndexOf("}") + 1)); // To remove extra characters after the last curly bracket (if any)
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!res.trim().isEmpty()) {
                sb3.append(res.trim());
            }
            sendBroadcastIntentFromLinkThree(sb3.toString());
            sb3.setLength(0);
        } else {
            if (BTConstants.isNewVersionLinkThree || BTConstants.CurrentCommand_LinkThree.contains(BTConstants.p_type_command)) {
                sb3.append(Response);
            } else {
                // For old version Link response
                sb3.setLength(0);
                sendBroadcastIntentFromLinkThree(spn.toString());
            }
        }
    }

    public void sendBroadcastIntentFromLinkThree(String spn) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("BroadcastBlueLinkThreeData");
        broadcastIntent.putExtra("Request", BTConstants.CurrentCommand_LinkThree);
        broadcastIntent.putExtra("Response", spn.trim());
        broadcastIntent.putExtra("Action", "BlueLinkThree");
        activity.sendBroadcast(broadcastIntent);
    }

    public void status3(String str) {
        Log.i(TAG, "Status3:" + str);
        BTConstants.BTStatusStrThree = str;
    }
    //Link Three code ends...
    //endregion

    //region Link Four code
    //Link Four code begins..
    @Override
    public void onSerialConnectFour() {
        BTConstants.BTLinkFourStatus = true;
        status4("Connected");
    }

    @Override
    public void onSerialConnectErrorFour(Exception e) {
        BTConstants.BTLinkFourStatus = false;
        status4("Disconnect");
        e.printStackTrace();
        if (BTConstants.IsFileUploadCompleted) {
            try {
                connect4();
            } catch (Exception ex) {
                Log.e("Error: ", ex.getMessage());
            }
        }
        //AppConstants.WriteinFile(TAG + " onSerialConnectErrorFour Status: " + e.getMessage());
    }

    @Override
    public void onSerialReadFour(byte[] data) {
        receive4(data);
    }

    @Override
    public void onSerialIoErrorFour(Exception e) {
        BTConstants.BTLinkFourStatus = false;
        status4("Disconnect");
        e.printStackTrace();
        if (BTConstants.IsFileUploadCompleted) {
            try {
                connect4();
            } catch (Exception ex) {
                Log.e("Error: ", ex.getMessage());
            }
        }
        //AppConstants.WriteinFile(TAG + " onSerialIoErrorFour Status: " + e.getMessage());
    }

    public void connect4() {
        try {

            if (BTConstants.deviceAddress4 != null && !BTConstants.deviceAddress4.isEmpty()) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(BTConstants.deviceAddress4);
                status4("Connecting...");
                //BTConstants.BTLinkTwoStatus = false;
                SerialSocketFour socket = new SerialSocketFour(activity.getApplicationContext(), device);
                service4.connect(socket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send4(String str) {
        if (!BTConstants.BTLinkFourStatus) {
            BTConstants.CurrentCommand_LinkFour = "";
            Log.i(TAG, "BTLink 4: Link not connected");
            //Toast.makeText(activity, "BTLink 4: Link not connected", Toast.LENGTH_SHORT).show();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 4: Link not connected");
            return;
        }
        try {
            //Log command sent:str
            BTConstants.CurrentCommand_LinkFour = str;
            Log.i(TAG, "BTLink 4: Requesting..." + str);
            byte[] data = (str + newline).getBytes();
            service4.write(data);
        } catch (Exception e) {
            onSerialIoErrorFour(e);
        }
    }

    public void sendBytes4(byte[] data) {
        if (!BTConstants.BTLinkFourStatus) {
            BTConstants.CurrentCommand_LinkFour = "";
            Log.i(TAG, "BTLink 4: Link not connected");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 4: Link not connected");
            return;
        }
        try {
            service4.write(data);
        } catch (Exception e) {
            onSerialIoErrorFour(e);
        }
    }

    public void receive4(byte[] data) {
        String Response = new String(data);
        SpannableStringBuilder spn = new SpannableStringBuilder(Response + '\n');
        Log.i(TAG, "BTLink 4: Request>>" + BTConstants.CurrentCommand_LinkFour);
        Log.i(TAG, "BTLink 4: Response>>" + spn.toString());

        //==========================================
        if (BTConstants.CurrentCommand_LinkFour.equalsIgnoreCase(BTConstants.info_cmd) && Response.contains("records")) {
            BTConstants.isNewVersionLinkFour = true;
        }
        if (Response.contains("$$")) {
            String res = Response.replace("$$", "");
            try {
                if (res.contains("}")) {
                    res = res.substring(0, (res.lastIndexOf("}") + 1)); // To remove extra characters after the last curly bracket (if any)
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!res.trim().isEmpty()) {
                sb4.append(res.trim());
            }
            sendBroadcastIntentFromLinkFour(sb4.toString());
            sb4.setLength(0);
        } else {
            if (BTConstants.isNewVersionLinkFour || BTConstants.CurrentCommand_LinkFour.contains(BTConstants.p_type_command)) {
                sb4.append(Response);
            } else {
                // For old version Link response
                sb4.setLength(0);
                sendBroadcastIntentFromLinkFour(spn.toString());
            }
        }
    }

    public void sendBroadcastIntentFromLinkFour(String spn) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("BroadcastBlueLinkFourData");
        broadcastIntent.putExtra("Request", BTConstants.CurrentCommand_LinkFour);
        broadcastIntent.putExtra("Response", spn.trim());
        broadcastIntent.putExtra("Action", "BlueLinkFour");
        activity.sendBroadcast(broadcastIntent);
    }

    public void status4(String str) {
        Log.i(TAG, "Status4:" + str);
        BTConstants.BTStatusStrFour = str;
    }
    //endregion

    //region Link Five code
    //Link Five code begins....
    @Override
    public void onSerialConnectFive() {
        BTConstants.BTLinkFiveStatus = true;
        status5("Connected");
    }

    @Override
    public void onSerialConnectErrorFive(Exception e) {
        BTConstants.BTLinkFiveStatus = false;
        status5("Disconnect");
        e.printStackTrace();
        if (BTConstants.IsFileUploadCompleted) {
            try {
                connect5();
            } catch (Exception ex) {
                Log.e("Error: ", ex.getMessage());
            }
        }
        //AppConstants.WriteinFile(TAG + " onSerialConnectErrorFive Status: " + e.getMessage());
    }

    @Override
    public void onSerialReadFive(byte[] data) {
        receive5(data);
    }

    @Override
    public void onSerialIoErrorFive(Exception e) {
        BTConstants.BTLinkFiveStatus = false;
        status5("Disconnect");
        e.printStackTrace();
        if (BTConstants.IsFileUploadCompleted) {
            try {
                connect5();
            } catch (Exception ex) {
                Log.e("Error: ", ex.getMessage());
            }
        }
        //AppConstants.WriteinFile(TAG + " onSerialIoErrorFive Status: " + e.getMessage());
    }

    public void connect5() {
        try {

            if (BTConstants.deviceAddress5 != null && !BTConstants.deviceAddress5.isEmpty()) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(BTConstants.deviceAddress5);
                status5("Connecting...");
                //BTConstants.BTLinkFiveStatus = false;
                SerialSocketFive socket = new SerialSocketFive(activity.getApplicationContext(), device);
                service5.connect(socket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send5(String str) {
        if (!BTConstants.BTLinkFiveStatus) {
            BTConstants.CurrentCommand_LinkFive = "";
            Log.i(TAG, "BTLink 5: Link not connected");
            //Toast.makeText(activity, "BTLink 5: Link not connected", Toast.LENGTH_SHORT).show();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 5: Link not connected");
            return;
        }
        try {
            //Log command sent:str
            BTConstants.CurrentCommand_LinkFive = str;
            Log.i(TAG, "BTLink 5: Requesting..." + str);
            byte[] data = (str + newline).getBytes();
            service5.write(data);
        } catch (Exception e) {
            onSerialIoErrorFive(e);
        }
    }

    public void sendBytes5(byte[] data) {
        if (!BTConstants.BTLinkFiveStatus) {
            BTConstants.CurrentCommand_LinkFive = "";
            Log.i(TAG, "BTLink 5: Link not connected");
            //Toast.makeText(activity, "BTLink 5: Link not connected", Toast.LENGTH_SHORT).show();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 5: Link not connected");
            return;
        }
        try {
            service5.write(data);
        } catch (Exception e) {
            onSerialIoErrorFive(e);
        }
    }

    public void readPulse5() {
        if (!BTConstants.BTLinkFiveStatus) {
            BTConstants.CurrentCommand_LinkFive = "";
            Log.i(TAG, "BTLink 5: Link not connected");
            //Toast.makeText(activity, "BTLink 5: Link not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            service5.readPulse();
        } catch (Exception e) {
            onSerialIoErrorFive(e);
        }
    }

    public void receive5(byte[] data) {
        String Response = new String(data);
        SpannableStringBuilder spn = new SpannableStringBuilder(Response + '\n');
        Log.i(TAG, "BTLink 5: Request>>" + BTConstants.CurrentCommand_LinkFive);
        Log.i(TAG, "BTLink 5: Response>>" + spn.toString());

        //==========================================
        if (BTConstants.CurrentCommand_LinkFive.equalsIgnoreCase(BTConstants.info_cmd) && Response.contains("records")) {
            BTConstants.isNewVersionLinkFive = true;
        }
        if (Response.contains("$$")) {
            String res = Response.replace("$$", "");
            try {
                if (res.contains("}")) {
                    res = res.substring(0, (res.lastIndexOf("}") + 1)); // To remove extra characters after the last curly bracket (if any)
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!res.trim().isEmpty()) {
                sb5.append(res.trim());
            }
            sendBroadcastIntentFromLinkFive(sb5.toString());
            sb5.setLength(0);
        } else {
            if (BTConstants.isNewVersionLinkFive || BTConstants.CurrentCommand_LinkFive.contains(BTConstants.p_type_command)) {
                sb5.append(Response);
            } else {
                // For old version Link response
                sb5.setLength(0);
                sendBroadcastIntentFromLinkFive(spn.toString());
            }
        }
    }

    public void sendBroadcastIntentFromLinkFive(String spn) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("BroadcastBlueLinkFiveData");
        broadcastIntent.putExtra("Request", BTConstants.CurrentCommand_LinkFive);
        broadcastIntent.putExtra("Response", spn.trim());
        broadcastIntent.putExtra("Action", "BlueLinkFive");
        activity.sendBroadcast(broadcastIntent);
    }

    public void status5(String str) {
        Log.i(TAG, "Status5:" + str);
        BTConstants.BTStatusStrFive = str;
    }
    //Link Five code ends.......
    //endregion

    //region Link Six code
    //Link Six code begins....
    @Override
    public void onSerialConnectSix() {
        BTConstants.BTLinkSixStatus = true;
        status6("Connected");
    }

    @Override
    public void onSerialConnectErrorSix(Exception e) {
        BTConstants.BTLinkSixStatus = false;
        status6("Disconnect");
        e.printStackTrace();
        if (BTConstants.IsFileUploadCompleted) {
            try {
                connect6();
            } catch (Exception ex) {
                Log.e("Error: ", ex.getMessage());
            }
        }
        //AppConstants.WriteinFile(TAG + " onSerialConnectErrorSix Status: " + e.getMessage());
    }

    @Override
    public void onSerialReadSix(byte[] data) {
        receive6(data);
    }

    @Override
    public void onSerialIoErrorSix(Exception e) {
        BTConstants.BTLinkSixStatus = false;
        status6("Disconnect");
        e.printStackTrace();
        if (BTConstants.IsFileUploadCompleted) {
            try {
                connect6();
            } catch (Exception ex) {
                Log.e("Error: ", ex.getMessage());
            }
        }
        //AppConstants.WriteinFile(TAG + " onSerialIoErrorSix Status: " + e.getMessage());
    }

    public void connect6() {
        try {

            if (BTConstants.deviceAddress6 != null && !BTConstants.deviceAddress6.isEmpty()) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(BTConstants.deviceAddress6);
                status6("Connecting...");
                //BTConstants.BTLinkSixStatus = false;
                SerialSocketSix socket = new SerialSocketSix(activity.getApplicationContext(), device);
                service6.connect(socket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send6(String str) {
        if (!BTConstants.BTLinkSixStatus) {
            BTConstants.CurrentCommand_LinkSix = "";
            Log.i(TAG, "BTLink 6: Link not connected");
            //Toast.makeText(activity, "BTLink 6: Link not connected", Toast.LENGTH_SHORT).show();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 6: Link not connected");
            return;
        }
        try {
            //Log command sent:str
            BTConstants.CurrentCommand_LinkSix = str;
            Log.i(TAG, "BTLink 6: Requesting..." + str);
            byte[] data = (str + newline).getBytes();
            service6.write(data);
        } catch (Exception e) {
            onSerialIoErrorSix(e);
        }
    }

    public void sendBytes6(byte[] data) {
        if (!BTConstants.BTLinkSixStatus) {
            BTConstants.CurrentCommand_LinkSix = "";
            Log.i(TAG, "BTLink 6: Link not connected");
            //Toast.makeText(activity, "BTLink 6: Link not connected", Toast.LENGTH_SHORT).show();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " BTLink 6: Link not connected");
            return;
        }
        try {
            service6.write(data);
        } catch (Exception e) {
            onSerialIoErrorSix(e);
        }
    }

    public void readPulse6() {
        if (!BTConstants.BTLinkSixStatus) {
            BTConstants.CurrentCommand_LinkSix = "";
            Log.i(TAG, "BTLink 6: Link not connected");
            //Toast.makeText(activity, "BTLink 6: Link not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            service6.readPulse();
        } catch (Exception e) {
            onSerialIoErrorSix(e);
        }
    }

    public void receive6(byte[] data) {
        String Response = new String(data);
        SpannableStringBuilder spn = new SpannableStringBuilder(Response + '\n');
        Log.i(TAG, "BTLink 6: Request>>" + BTConstants.CurrentCommand_LinkSix);
        Log.i(TAG, "BTLink 6: Response>>" + spn.toString());

        //==========================================
        if (BTConstants.CurrentCommand_LinkSix.equalsIgnoreCase(BTConstants.info_cmd) && Response.contains("records")) {
            BTConstants.isNewVersionLinkSix = true;
        }
        if (Response.contains("$$")) {
            String res = Response.replace("$$", "");
            try {
                if (res.contains("}")) {
                    res = res.substring(0, (res.lastIndexOf("}") + 1)); // To remove extra characters after the last curly bracket (if any)
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!res.trim().isEmpty()) {
                sb6.append(res.trim());
            }
            sendBroadcastIntentFromLinkSix(sb6.toString());
            sb6.setLength(0);
        } else {
            if (BTConstants.isNewVersionLinkSix || BTConstants.CurrentCommand_LinkSix.contains(BTConstants.p_type_command)) {
                sb6.append(Response);
            } else {
                // For old version Link response
                sb6.setLength(0);
                sendBroadcastIntentFromLinkSix(spn.toString());
            }
        }
    }

    public void sendBroadcastIntentFromLinkSix(String spn) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("BroadcastBlueLinkSixData");
        broadcastIntent.putExtra("Request", BTConstants.CurrentCommand_LinkSix);
        broadcastIntent.putExtra("Response", spn.trim());
        broadcastIntent.putExtra("Action", "BlueLinkSix");
        activity.sendBroadcast(broadcastIntent);
    }

    public void status6(String str) {
        Log.i(TAG, "Status6:" + str);
        BTConstants.BTStatusStrSix = str;
    }
    //Link Six code ends.......
    //endregion

}
