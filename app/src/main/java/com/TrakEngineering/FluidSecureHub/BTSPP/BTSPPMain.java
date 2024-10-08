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
    private static final String TAG = ""; //AppConstants.LOG_TXTN_BT + "-"; //BTSPPMain.class.getSimpleName();
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
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "BTSPPLink_1: <SerialConnectError: " + e.getMessage() + ">");
    }

    @Override
    public void onSerialReadOne(byte[] data) {
        receive1(data);
    }

    @Override
    public void onSerialIoErrorOne(Exception e, Integer fromCode) {
        BTConstants.BTLinkOneStatus = false;
        status1("Disconnect");
        e.printStackTrace();
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "BTSPPLink_1: <SerialIoError: " + e.getMessage() + "; ErrorCode: " + fromCode + ">");
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
            Log.i(TAG, "BTSPPLink_1: Link not connected");
            //Toast.makeText(activity, "BTSPPLink_1: Link not connected", Toast.LENGTH_SHORT).show();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "BTSPPLink_1: Link not connected");
            return;
        }
        try {
            //Log command sent:str
            if (!str.equalsIgnoreCase(BTConstants.fdcheckcommand)) {
                BTConstants.CurrentCommand_LinkOne = str;
            }
            Log.i(TAG, "BTSPPLink_1: Requesting..." + str);
            byte[] data = (str + newline).getBytes();
            service1.write(data);
        } catch (Exception e) {
            onSerialIoErrorOne(e, 1);
        }
    }

    public void sendBytes1(byte[] data) {
        if (!BTConstants.BTLinkOneStatus) {
            BTConstants.CurrentCommand_LinkOne = "";
            Log.i(TAG, "BTSPPLink_1: Link not connected");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "BTSPPLink_1: Link not connected");
            return;
        }
        try {
            service1.write(data);
        } catch (Exception e) {
            onSerialIoErrorOne(e, 2);
        }
    }

    public void receive1(byte[] data) {
        String Response = new String(data);
        SpannableStringBuilder spn = new SpannableStringBuilder(Response + '\n');
        Log.i(TAG, "BTSPPLink_1: Request>>" + BTConstants.CurrentCommand_LinkOne);
        Log.i(TAG, "BTSPPLink_1: Response>>" + spn.toString());

        //==========================================
        if (BTConstants.CurrentCommand_LinkOne.equalsIgnoreCase(BTConstants.info_cmd) && Response.contains("mac_address")) {
            BTConstants.isNewVersionLinkOne = true;
        }
        if (Response.contains("$$")) {

            sb1.append(Response.trim());

            String finalResp = sb1.toString().trim();
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

            String[] resp = finalResp.trim().split("\\$\\$"); // To split by $$
            for (String res: resp) {
                res = res.replace("$$", "");
                if (!res.trim().isEmpty()) {
                    sendBroadcastIntentFromLinkOne(res);
                }
            }
            sb1.setLength(0);
        } else {
            if (BTConstants.isNewVersionLinkOne || BTConstants.forOscilloscope || (BTConstants.CurrentCommand_LinkOne.equalsIgnoreCase(BTConstants.info_cmd) && !Response.contains("BTMAC")) || BTConstants.CurrentCommand_LinkOne.contains(BTConstants.p_type_command)) {
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
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "BTSPPLink_2: <SerialConnectError: " + e.getMessage() + ">");
    }

    @Override
    public void onSerialReadTwo(byte[] data) {
        receive2(data);
    }

    @Override
    public void onSerialIoErrorTwo(Exception e, Integer fromCode) {
        BTConstants.BTLinkTwoStatus = false;
        status2("Disconnect");
        e.printStackTrace();
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "BTSPPLink_2: <SerialIoError: " + e.getMessage() + "; ErrorCode: " + fromCode + ">");
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
            Log.i(TAG, "BTSPPLink_2: Link not connected");
            //Toast.makeText(activity, "BTSPPLink_2: Link not connected", Toast.LENGTH_SHORT).show();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "BTSPPLink_2: Link not connected");
            return;
        }
        try {
            //Log command sent:str
            if (!str.equalsIgnoreCase(BTConstants.fdcheckcommand)) {
                BTConstants.CurrentCommand_LinkTwo = str;
            }
            Log.i(TAG, "BTSPPLink_2: Requesting..." + str);
            byte[] data = (str + newline).getBytes();
            service2.write(data);
        } catch (Exception e) {
            onSerialIoErrorTwo(e, 1);
        }
    }

    public void sendBytes2(byte[] data) {
        if (!BTConstants.BTLinkTwoStatus) {
            BTConstants.CurrentCommand_LinkTwo = "";
            Log.i(TAG, "BTSPPLink_2: Link not connected");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "BTSPPLink_2: Link not connected");
            return;
        }
        try {
            service2.write(data);
        } catch (Exception e) {
            onSerialIoErrorTwo(e, 2);
        }
    }

    public void receive2(byte[] data) {
        String Response = new String(data);
        SpannableStringBuilder spn = new SpannableStringBuilder(Response + '\n');
        Log.i(TAG, "BTSPPLink_2: Request>>" + BTConstants.CurrentCommand_LinkTwo);
        Log.i(TAG, "BTSPPLink_2: Response>>" + spn.toString());

        //==========================================
        if (BTConstants.CurrentCommand_LinkTwo.equalsIgnoreCase(BTConstants.info_cmd) && Response.contains("mac_address")) {
            BTConstants.isNewVersionLinkTwo = true;
        }
        if (Response.contains("$$")) {

            sb2.append(Response.trim());

            String finalResp = sb2.toString().trim();
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

            String[] resp = finalResp.trim().split("\\$\\$"); // To split by $$
            for (String res: resp) {
                res = res.replace("$$", "");
                if (!res.trim().isEmpty()) {
                    sendBroadcastIntentFromLinkTwo(res);
                }
            }
            sb2.setLength(0);
        } else {
            if (BTConstants.isNewVersionLinkTwo || BTConstants.forOscilloscope || (BTConstants.CurrentCommand_LinkTwo.equalsIgnoreCase(BTConstants.info_cmd) && !Response.contains("BTMAC")) || BTConstants.CurrentCommand_LinkTwo.contains(BTConstants.p_type_command)) {
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
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "BTSPPLink_3: <SerialConnectError: " + e.getMessage() + ">");
    }

    @Override
    public void onSerialReadThree(byte[] data) {
        receive3(data);
    }

    @Override
    public void onSerialIoErrorThree(Exception e, Integer fromCode) {
        BTConstants.BTLinkThreeStatus = false;
        status3("Disconnect");
        e.printStackTrace();
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "BTSPPLink_3: <SerialIoError: " + e.getMessage() + "; ErrorCode: " + fromCode + ">");
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
            Log.i(TAG, "BTSPPLink_3: Link not connected");
            //Toast.makeText(activity, "BTSPPLink_3: Link not connected", Toast.LENGTH_SHORT).show();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "BTSPPLink_3: Link not connected");
            return;
        }
        try {
            //Log command sent:str
            if (!str.equalsIgnoreCase(BTConstants.fdcheckcommand)) {
                BTConstants.CurrentCommand_LinkThree = str;
            }
            Log.i(TAG, "BTSPPLink_3: Requesting..." + str);
            byte[] data = (str + newline).getBytes();
            service3.write(data);
        } catch (Exception e) {
            onSerialIoErrorThree(e, 1);
        }
    }

    public void sendBytes3(byte[] data) {
        if (!BTConstants.BTLinkThreeStatus) {
            BTConstants.CurrentCommand_LinkThree = "";
            Log.i(TAG, "BTSPPLink_3: Link not connected");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "BTSPPLink_3: Link not connected");
            return;
        }
        try {
            service3.write(data);
        } catch (Exception e) {
            onSerialIoErrorThree(e, 2);
        }
    }

    public void receive3(byte[] data) {
        String Response = new String(data);
        SpannableStringBuilder spn = new SpannableStringBuilder(Response + '\n');
        Log.i(TAG, "BTSPPLink_3: Request>>" + BTConstants.CurrentCommand_LinkThree);
        Log.i(TAG, "BTSPPLink_3: Response>>" + spn.toString());

        //==========================================
        if (BTConstants.CurrentCommand_LinkThree.equalsIgnoreCase(BTConstants.info_cmd) && Response.contains("mac_address")) {
            BTConstants.isNewVersionLinkThree = true;
        }
        if (Response.contains("$$")) {

            sb3.append(Response.trim());

            String finalResp = sb3.toString().trim();
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

            String[] resp = finalResp.trim().split("\\$\\$"); // To split by $$
            for (String res: resp) {
                res = res.replace("$$", "");
                if (!res.trim().isEmpty()) {
                    sendBroadcastIntentFromLinkThree(res);
                }
            }
            sb3.setLength(0);
        } else {
            if (BTConstants.isNewVersionLinkThree || BTConstants.forOscilloscope || (BTConstants.CurrentCommand_LinkThree.equalsIgnoreCase(BTConstants.info_cmd) && !Response.contains("BTMAC")) || BTConstants.CurrentCommand_LinkThree.contains(BTConstants.p_type_command)) {
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
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "BTSPPLink_4: <SerialConnectError: " + e.getMessage() + ">");
    }

    @Override
    public void onSerialReadFour(byte[] data) {
        receive4(data);
    }

    @Override
    public void onSerialIoErrorFour(Exception e, Integer fromCode) {
        BTConstants.BTLinkFourStatus = false;
        status4("Disconnect");
        e.printStackTrace();
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "BTSPPLink_4: <SerialIoError: " + e.getMessage() + "; ErrorCode: " + fromCode + ">");
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
            Log.i(TAG, "BTSPPLink_4: Link not connected");
            //Toast.makeText(activity, "BTSPPLink_4: Link not connected", Toast.LENGTH_SHORT).show();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "BTSPPLink_4: Link not connected");
            return;
        }
        try {
            //Log command sent:str
            if (!str.equalsIgnoreCase(BTConstants.fdcheckcommand)) {
                BTConstants.CurrentCommand_LinkFour = str;
            }
            Log.i(TAG, "BTSPPLink_4: Requesting..." + str);
            byte[] data = (str + newline).getBytes();
            service4.write(data);
        } catch (Exception e) {
            onSerialIoErrorFour(e, 1);
        }
    }

    public void sendBytes4(byte[] data) {
        if (!BTConstants.BTLinkFourStatus) {
            BTConstants.CurrentCommand_LinkFour = "";
            Log.i(TAG, "BTSPPLink_4: Link not connected");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "BTSPPLink_4: Link not connected");
            return;
        }
        try {
            service4.write(data);
        } catch (Exception e) {
            onSerialIoErrorFour(e, 2);
        }
    }

    public void receive4(byte[] data) {
        String Response = new String(data);
        SpannableStringBuilder spn = new SpannableStringBuilder(Response + '\n');
        Log.i(TAG, "BTSPPLink_4: Request>>" + BTConstants.CurrentCommand_LinkFour);
        Log.i(TAG, "BTSPPLink_4: Response>>" + spn.toString());

        //==========================================
        if (BTConstants.CurrentCommand_LinkFour.equalsIgnoreCase(BTConstants.info_cmd) && Response.contains("mac_address")) {
            BTConstants.isNewVersionLinkFour = true;
        }
        if (Response.contains("$$")) {

            sb4.append(Response.trim());

            String finalResp = sb4.toString().trim();
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

            String[] resp = finalResp.trim().split("\\$\\$"); // To split by $$
            for (String res: resp) {
                res = res.replace("$$", "");
                if (!res.trim().isEmpty()) {
                    sendBroadcastIntentFromLinkFour(res);
                }
            }
            sb4.setLength(0);
        } else {
            if (BTConstants.isNewVersionLinkFour || BTConstants.forOscilloscope || (BTConstants.CurrentCommand_LinkFour.equalsIgnoreCase(BTConstants.info_cmd) && !Response.contains("BTMAC")) || BTConstants.CurrentCommand_LinkFour.contains(BTConstants.p_type_command)) {
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
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "BTSPPLink_5: <SerialConnectError: " + e.getMessage() + ">");
    }

    @Override
    public void onSerialReadFive(byte[] data) {
        receive5(data);
    }

    @Override
    public void onSerialIoErrorFive(Exception e, Integer fromCode) {
        BTConstants.BTLinkFiveStatus = false;
        status5("Disconnect");
        e.printStackTrace();
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "BTSPPLink_5: <SerialIoError: " + e.getMessage() + "; ErrorCode: " + fromCode + ">");
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
            Log.i(TAG, "BTSPPLink_5: Link not connected");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "BTSPPLink_5: Link not connected");
            return;
        }
        try {
            //Log command sent:str
            if (!str.equalsIgnoreCase(BTConstants.fdcheckcommand)) {
                BTConstants.CurrentCommand_LinkFive = str;
            }
            Log.i(TAG, "BTSPPLink_5: Requesting..." + str);
            byte[] data = (str + newline).getBytes();
            service5.write(data);
        } catch (Exception e) {
            onSerialIoErrorFive(e, 1);
        }
    }

    public void sendBytes5(byte[] data) {
        if (!BTConstants.BTLinkFiveStatus) {
            BTConstants.CurrentCommand_LinkFive = "";
            Log.i(TAG, "BTSPPLink_5: Link not connected");
            //Toast.makeText(activity, "BTSPPLink_5: Link not connected", Toast.LENGTH_SHORT).show();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "BTSPPLink_5: Link not connected");
            return;
        }
        try {
            service5.write(data);
        } catch (Exception e) {
            onSerialIoErrorFive(e, 2);
        }
    }

    public void receive5(byte[] data) {
        String Response = new String(data);
        SpannableStringBuilder spn = new SpannableStringBuilder(Response + '\n');
        Log.i(TAG, "BTSPPLink_5: Request>>" + BTConstants.CurrentCommand_LinkFive);
        Log.i(TAG, "BTSPPLink_5: Response>>" + spn.toString());

        //==========================================
        if (BTConstants.CurrentCommand_LinkFive.equalsIgnoreCase(BTConstants.info_cmd) && Response.contains("mac_address")) {
            BTConstants.isNewVersionLinkFive = true;
        }
        if (Response.contains("$$")) {

            sb5.append(Response.trim());

            String finalResp = sb5.toString().trim();
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

            String[] resp = finalResp.trim().split("\\$\\$"); // To split by $$
            for (String res: resp) {
                res = res.replace("$$", "");
                if (!res.trim().isEmpty()) {
                    sendBroadcastIntentFromLinkFive(res);
                }
            }
            sb5.setLength(0);
        } else {
            if (BTConstants.isNewVersionLinkFive || BTConstants.forOscilloscope || (BTConstants.CurrentCommand_LinkFive.equalsIgnoreCase(BTConstants.info_cmd) && !Response.contains("BTMAC")) || BTConstants.CurrentCommand_LinkFive.contains(BTConstants.p_type_command)) {
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
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "BTSPPLink_6: <SerialConnectError: " + e.getMessage() + ">");
    }

    @Override
    public void onSerialReadSix(byte[] data) {
        receive6(data);
    }

    @Override
    public void onSerialIoErrorSix(Exception e, Integer fromCode) {
        BTConstants.BTLinkSixStatus = false;
        status6("Disconnect");
        e.printStackTrace();
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "BTSPPLink_6: <SerialIoError: " + e.getMessage() + "; ErrorCode: " + fromCode + ">");
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
            Log.i(TAG, "BTSPPLink_6: Link not connected");
            //Toast.makeText(activity, "BTSPPLink_6: Link not connected", Toast.LENGTH_SHORT).show();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "BTSPPLink_6: Link not connected");
            return;
        }
        try {
            //Log command sent:str
            if (!str.equalsIgnoreCase(BTConstants.fdcheckcommand)) {
                BTConstants.CurrentCommand_LinkSix = str;
            }
            Log.i(TAG, "BTSPPLink_6: Requesting..." + str);
            byte[] data = (str + newline).getBytes();
            service6.write(data);
        } catch (Exception e) {
            onSerialIoErrorSix(e, 1);
        }
    }

    public void sendBytes6(byte[] data) {
        if (!BTConstants.BTLinkSixStatus) {
            BTConstants.CurrentCommand_LinkSix = "";
            Log.i(TAG, "BTSPPLink_6: Link not connected");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "BTSPPLink_6: Link not connected");
            return;
        }
        try {
            service6.write(data);
        } catch (Exception e) {
            onSerialIoErrorSix(e, 2);
        }
    }

    public void receive6(byte[] data) {
        String Response = new String(data);
        SpannableStringBuilder spn = new SpannableStringBuilder(Response + '\n');
        Log.i(TAG, "BTSPPLink_6: Request>>" + BTConstants.CurrentCommand_LinkSix);
        Log.i(TAG, "BTSPPLink_6: Response>>" + spn.toString());

        //==========================================
        if (BTConstants.CurrentCommand_LinkSix.equalsIgnoreCase(BTConstants.info_cmd) && Response.contains("mac_address")) {
            BTConstants.isNewVersionLinkSix = true;
        }
        if (Response.contains("$$")) {

            sb6.append(Response.trim());

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

            String[] resp = finalResp.trim().split("\\$\\$"); // To split by $$
            for (String res: resp) {
                res = res.replace("$$", "");
                if (!res.trim().isEmpty()) {
                    sendBroadcastIntentFromLinkSix(res);
                }
            }
            sb6.setLength(0);
        } else {
            if (BTConstants.isNewVersionLinkSix || BTConstants.forOscilloscope || (BTConstants.CurrentCommand_LinkSix.equalsIgnoreCase(BTConstants.info_cmd) && !Response.contains("BTMAC")) || BTConstants.CurrentCommand_LinkSix.contains(BTConstants.p_type_command)) {
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
