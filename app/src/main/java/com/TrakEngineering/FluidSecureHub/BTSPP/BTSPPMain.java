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
    //private String DEVICE_ADDRESS_1 = ""; //80:7D:3A:A4:67:22
    //private String DEVICE_ADDRESS_2 = "";
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
            //DEVICE_ADDRESS_1 = edt_mac_address.getText().toString().trim();

        } else {
            //edt_mac_address.setText("");
            //tv_mac_address.setText("Mac Address:");
        }

    }

    //region Link One code
    //Link One code begins....
    @Override
    public void onSerialConnectOne() {
        BTConstants.BT_LINK_ONE_STATUS = true;
        status1("Connected");
    }

    @Override
    public void onSerialConnectErrorOne(Exception e) {
        BTConstants.BT_LINK_ONE_STATUS = false;
        status1("Disconnect");
        e.printStackTrace();
        if (AppConstants.GENERATE_LOGS)
            AppConstants.writeInFile(TAG + "BTSPPLink_1: <SerialConnectError: " + e.getMessage() + ">");
    }

    @Override
    public void onSerialReadOne(byte[] data) {
        receive1(data);
    }

    @Override
    public void onSerialIoErrorOne(Exception e, Integer fromCode) {
        BTConstants.BT_LINK_ONE_STATUS = false;
        status1("Disconnect");
        e.printStackTrace();
        if (AppConstants.GENERATE_LOGS)
            AppConstants.writeInFile(TAG + "BTSPPLink_1: <SerialIoError: " + e.getMessage() + "; ErrorCode: " + fromCode + ">");
    }

    public void connect1() {
        try {

            if (BTConstants.DEVICE_ADDRESS_1 != null && !BTConstants.DEVICE_ADDRESS_1.isEmpty()) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(BTConstants.DEVICE_ADDRESS_1);
                status1("Connecting...");
                //BTConstants.BT_LINK_ONE_STATUS = false;
                SerialSocketOne socket = new SerialSocketOne(activity.getApplicationContext(), device);
                service1.connect(socket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send1(String str) {
        if (!BTConstants.BT_LINK_ONE_STATUS) {
            BTConstants.CURRENT_COMMAND_LINK_ONE = "";
            Log.i(TAG, "BTSPPLink_1: Link not connected");
            //Toast.makeText(activity, "BTSPPLink_1: Link not connected", Toast.LENGTH_SHORT).show();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "BTSPPLink_1: Link not connected");
            return;
        }
        try {
            //Log command sent:str
            if (!str.equalsIgnoreCase(BTConstants.FD_CHECK_COMMAND)) {
                BTConstants.CURRENT_COMMAND_LINK_ONE = str;
            }
            Log.i(TAG, "BTSPPLink_1: Requesting..." + str);
            byte[] data = (str + newline).getBytes();
            service1.write(data);
        } catch (Exception e) {
            onSerialIoErrorOne(e, 1);
        }
    }

    public void sendBytes1(byte[] data) {
        if (!BTConstants.BT_LINK_ONE_STATUS) {
            BTConstants.CURRENT_COMMAND_LINK_ONE = "";
            Log.i(TAG, "BTSPPLink_1: Link not connected");
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "BTSPPLink_1: Link not connected");
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
        Log.i(TAG, "BTSPPLink_1: Request>>" + BTConstants.CURRENT_COMMAND_LINK_ONE);
        Log.i(TAG, "BTSPPLink_1: Response>>" + spn.toString());

        //==========================================
        if (BTConstants.CURRENT_COMMAND_LINK_ONE.equalsIgnoreCase(BTConstants.INFO_COMMAND) && Response.contains("mac_address")) {
            BTConstants.IS_NEW_VERSION_LINK_ONE = true;
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
            if (BTConstants.IS_NEW_VERSION_LINK_ONE || BTConstants.FOR_OSCILLOSCOPE || (BTConstants.CURRENT_COMMAND_LINK_ONE.equalsIgnoreCase(BTConstants.INFO_COMMAND) && !Response.contains("BTMAC")) || BTConstants.CURRENT_COMMAND_LINK_ONE.contains(BTConstants.P_TYPE_COMMAND)) {
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
        broadcastIntent.putExtra("Request", BTConstants.CURRENT_COMMAND_LINK_ONE);
        broadcastIntent.putExtra("Response", spn.trim());
        broadcastIntent.putExtra("Action", "BlueLinkOne");
        activity.sendBroadcast(broadcastIntent);
    }

    public void status1(String str) {
        Log.i(TAG, "Status1:" + str);
        BTConstants.BT_STATUS_STR_ONE = str;
    }
    //Link one code ends.......
    //endregion

    //region Link Two code
    //Link Two code begins..
    @Override
    public void onSerialConnectTwo() {
        BTConstants.BT_LINK_TWO_STATUS = true;
        status2("Connected");
    }

    @Override
    public void onSerialConnectErrorTwo(Exception e) {
        BTConstants.BT_LINK_TWO_STATUS = false;
        status2("Disconnect");
        e.printStackTrace();
        if (AppConstants.GENERATE_LOGS)
            AppConstants.writeInFile(TAG + "BTSPPLink_2: <SerialConnectError: " + e.getMessage() + ">");
    }

    @Override
    public void onSerialReadTwo(byte[] data) {
        receive2(data);
    }

    @Override
    public void onSerialIoErrorTwo(Exception e, Integer fromCode) {
        BTConstants.BT_LINK_TWO_STATUS = false;
        status2("Disconnect");
        e.printStackTrace();
        if (AppConstants.GENERATE_LOGS)
            AppConstants.writeInFile(TAG + "BTSPPLink_2: <SerialIoError: " + e.getMessage() + "; ErrorCode: " + fromCode + ">");
    }

    public void connect2() {
        try {

            if (BTConstants.DEVICE_ADDRESS_2 != null && !BTConstants.DEVICE_ADDRESS_2.isEmpty()) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(BTConstants.DEVICE_ADDRESS_2);
                status2("Connecting...");
                //BTConstants.BT_LINK_TWO_STATUS = false;
                SerialSocketTwo socket = new SerialSocketTwo(activity.getApplicationContext(), device);
                service2.connect(socket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send2(String str) {
        if (!BTConstants.BT_LINK_TWO_STATUS) {
            BTConstants.CURRENT_COMMAND_LINK_TWO = "";
            Log.i(TAG, "BTSPPLink_2: Link not connected");
            //Toast.makeText(activity, "BTSPPLink_2: Link not connected", Toast.LENGTH_SHORT).show();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "BTSPPLink_2: Link not connected");
            return;
        }
        try {
            //Log command sent:str
            if (!str.equalsIgnoreCase(BTConstants.FD_CHECK_COMMAND)) {
                BTConstants.CURRENT_COMMAND_LINK_TWO = str;
            }
            Log.i(TAG, "BTSPPLink_2: Requesting..." + str);
            byte[] data = (str + newline).getBytes();
            service2.write(data);
        } catch (Exception e) {
            onSerialIoErrorTwo(e, 1);
        }
    }

    public void sendBytes2(byte[] data) {
        if (!BTConstants.BT_LINK_TWO_STATUS) {
            BTConstants.CURRENT_COMMAND_LINK_TWO = "";
            Log.i(TAG, "BTSPPLink_2: Link not connected");
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "BTSPPLink_2: Link not connected");
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
        Log.i(TAG, "BTSPPLink_2: Request>>" + BTConstants.CURRENT_COMMAND_LINK_TWO);
        Log.i(TAG, "BTSPPLink_2: Response>>" + spn.toString());

        //==========================================
        if (BTConstants.CURRENT_COMMAND_LINK_TWO.equalsIgnoreCase(BTConstants.INFO_COMMAND) && Response.contains("mac_address")) {
            BTConstants.IS_NEW_VERSION_LINK_TWO = true;
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
            if (BTConstants.IS_NEW_VERSION_LINK_TWO || BTConstants.FOR_OSCILLOSCOPE || (BTConstants.CURRENT_COMMAND_LINK_TWO.equalsIgnoreCase(BTConstants.INFO_COMMAND) && !Response.contains("BTMAC")) || BTConstants.CURRENT_COMMAND_LINK_TWO.contains(BTConstants.P_TYPE_COMMAND)) {
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
        broadcastIntent.putExtra("Request", BTConstants.CURRENT_COMMAND_LINK_TWO);
        broadcastIntent.putExtra("Response", spn.trim());
        broadcastIntent.putExtra("Action", "BlueLinkTwo");
        activity.sendBroadcast(broadcastIntent);
    }

    public void status2(String str) {
        Log.i(TAG, "Status2:" + str);
        BTConstants.BT_STATUS_STR_TWO = str;
    }
    //endregion

    //region Link Three code
    //Link Three code begins..
    @Override
    public void onSerialConnectThree() {
        BTConstants.BT_LINK_THREE_STATUS = true;
        status3("Connected");
    }

    @Override
    public void onSerialConnectErrorThree(Exception e) {
        BTConstants.BT_LINK_THREE_STATUS = false;
        status3("Disconnect");
        e.printStackTrace();
        if (AppConstants.GENERATE_LOGS)
            AppConstants.writeInFile(TAG + "BTSPPLink_3: <SerialConnectError: " + e.getMessage() + ">");
    }

    @Override
    public void onSerialReadThree(byte[] data) {
        receive3(data);
    }

    @Override
    public void onSerialIoErrorThree(Exception e, Integer fromCode) {
        BTConstants.BT_LINK_THREE_STATUS = false;
        status3("Disconnect");
        e.printStackTrace();
        if (AppConstants.GENERATE_LOGS)
            AppConstants.writeInFile(TAG + "BTSPPLink_3: <SerialIoError: " + e.getMessage() + "; ErrorCode: " + fromCode + ">");
    }

    public void connect3() {
        try {

            if (BTConstants.DEVICE_ADDRESS_3 != null && !BTConstants.DEVICE_ADDRESS_3.isEmpty()) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(BTConstants.DEVICE_ADDRESS_3);
                status3("Connecting...");
                //BTConstants.BT_LINK_THREE_STATUS = false;
                SerialSocketThree socket = new SerialSocketThree(activity.getApplicationContext(), device);
                service3.connect(socket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send3(String str) {
        if (!BTConstants.BT_LINK_THREE_STATUS) {
            BTConstants.CURRENT_COMMAND_LINK_THREE = "";
            Log.i(TAG, "BTSPPLink_3: Link not connected");
            //Toast.makeText(activity, "BTSPPLink_3: Link not connected", Toast.LENGTH_SHORT).show();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "BTSPPLink_3: Link not connected");
            return;
        }
        try {
            //Log command sent:str
            if (!str.equalsIgnoreCase(BTConstants.FD_CHECK_COMMAND)) {
                BTConstants.CURRENT_COMMAND_LINK_THREE = str;
            }
            Log.i(TAG, "BTSPPLink_3: Requesting..." + str);
            byte[] data = (str + newline).getBytes();
            service3.write(data);
        } catch (Exception e) {
            onSerialIoErrorThree(e, 1);
        }
    }

    public void sendBytes3(byte[] data) {
        if (!BTConstants.BT_LINK_THREE_STATUS) {
            BTConstants.CURRENT_COMMAND_LINK_THREE = "";
            Log.i(TAG, "BTSPPLink_3: Link not connected");
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "BTSPPLink_3: Link not connected");
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
        Log.i(TAG, "BTSPPLink_3: Request>>" + BTConstants.CURRENT_COMMAND_LINK_THREE);
        Log.i(TAG, "BTSPPLink_3: Response>>" + spn.toString());

        //==========================================
        if (BTConstants.CURRENT_COMMAND_LINK_THREE.equalsIgnoreCase(BTConstants.INFO_COMMAND) && Response.contains("mac_address")) {
            BTConstants.IS_NEW_VERSION_LINK_THREE = true;
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
            if (BTConstants.IS_NEW_VERSION_LINK_THREE || BTConstants.FOR_OSCILLOSCOPE || (BTConstants.CURRENT_COMMAND_LINK_THREE.equalsIgnoreCase(BTConstants.INFO_COMMAND) && !Response.contains("BTMAC")) || BTConstants.CURRENT_COMMAND_LINK_THREE.contains(BTConstants.P_TYPE_COMMAND)) {
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
        broadcastIntent.putExtra("Request", BTConstants.CURRENT_COMMAND_LINK_THREE);
        broadcastIntent.putExtra("Response", spn.trim());
        broadcastIntent.putExtra("Action", "BlueLinkThree");
        activity.sendBroadcast(broadcastIntent);
    }

    public void status3(String str) {
        Log.i(TAG, "Status3:" + str);
        BTConstants.BT_STATUS_STR_THREE = str;
    }
    //Link Three code ends...
    //endregion

    //region Link Four code
    //Link Four code begins..
    @Override
    public void onSerialConnectFour() {
        BTConstants.BT_LINK_FOUR_STATUS = true;
        status4("Connected");
    }

    @Override
    public void onSerialConnectErrorFour(Exception e) {
        BTConstants.BT_LINK_FOUR_STATUS = false;
        status4("Disconnect");
        e.printStackTrace();
        if (AppConstants.GENERATE_LOGS)
            AppConstants.writeInFile(TAG + "BTSPPLink_4: <SerialConnectError: " + e.getMessage() + ">");
    }

    @Override
    public void onSerialReadFour(byte[] data) {
        receive4(data);
    }

    @Override
    public void onSerialIoErrorFour(Exception e, Integer fromCode) {
        BTConstants.BT_LINK_FOUR_STATUS = false;
        status4("Disconnect");
        e.printStackTrace();
        if (AppConstants.GENERATE_LOGS)
            AppConstants.writeInFile(TAG + "BTSPPLink_4: <SerialIoError: " + e.getMessage() + "; ErrorCode: " + fromCode + ">");
    }

    public void connect4() {
        try {

            if (BTConstants.DEVICE_ADDRESS_4 != null && !BTConstants.DEVICE_ADDRESS_4.isEmpty()) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(BTConstants.DEVICE_ADDRESS_4);
                status4("Connecting...");
                //BTConstants.BT_LINK_FOUR_STATUS = false;
                SerialSocketFour socket = new SerialSocketFour(activity.getApplicationContext(), device);
                service4.connect(socket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send4(String str) {
        if (!BTConstants.BT_LINK_FOUR_STATUS) {
            BTConstants.CURRENT_COMMAND_LINK_FOUR = "";
            Log.i(TAG, "BTSPPLink_4: Link not connected");
            //Toast.makeText(activity, "BTSPPLink_4: Link not connected", Toast.LENGTH_SHORT).show();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "BTSPPLink_4: Link not connected");
            return;
        }
        try {
            //Log command sent:str
            if (!str.equalsIgnoreCase(BTConstants.FD_CHECK_COMMAND)) {
                BTConstants.CURRENT_COMMAND_LINK_FOUR = str;
            }
            Log.i(TAG, "BTSPPLink_4: Requesting..." + str);
            byte[] data = (str + newline).getBytes();
            service4.write(data);
        } catch (Exception e) {
            onSerialIoErrorFour(e, 1);
        }
    }

    public void sendBytes4(byte[] data) {
        if (!BTConstants.BT_LINK_FOUR_STATUS) {
            BTConstants.CURRENT_COMMAND_LINK_FOUR = "";
            Log.i(TAG, "BTSPPLink_4: Link not connected");
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "BTSPPLink_4: Link not connected");
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
        Log.i(TAG, "BTSPPLink_4: Request>>" + BTConstants.CURRENT_COMMAND_LINK_FOUR);
        Log.i(TAG, "BTSPPLink_4: Response>>" + spn.toString());

        //==========================================
        if (BTConstants.CURRENT_COMMAND_LINK_FOUR.equalsIgnoreCase(BTConstants.INFO_COMMAND) && Response.contains("mac_address")) {
            BTConstants.IS_NEW_VERSION_LINK_FOUR = true;
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
            if (BTConstants.IS_NEW_VERSION_LINK_FOUR || BTConstants.FOR_OSCILLOSCOPE || (BTConstants.CURRENT_COMMAND_LINK_FOUR.equalsIgnoreCase(BTConstants.INFO_COMMAND) && !Response.contains("BTMAC")) || BTConstants.CURRENT_COMMAND_LINK_FOUR.contains(BTConstants.P_TYPE_COMMAND)) {
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
        broadcastIntent.putExtra("Request", BTConstants.CURRENT_COMMAND_LINK_FOUR);
        broadcastIntent.putExtra("Response", spn.trim());
        broadcastIntent.putExtra("Action", "BlueLinkFour");
        activity.sendBroadcast(broadcastIntent);
    }

    public void status4(String str) {
        Log.i(TAG, "Status4:" + str);
        BTConstants.BT_STATUS_STR_FOUR = str;
    }
    //endregion

    //region Link Five code
    //Link Five code begins....
    @Override
    public void onSerialConnectFive() {
        BTConstants.BT_LINK_FIVE_STATUS = true;
        status5("Connected");
    }

    @Override
    public void onSerialConnectErrorFive(Exception e) {
        BTConstants.BT_LINK_FIVE_STATUS = false;
        status5("Disconnect");
        e.printStackTrace();
        if (AppConstants.GENERATE_LOGS)
            AppConstants.writeInFile(TAG + "BTSPPLink_5: <SerialConnectError: " + e.getMessage() + ">");
    }

    @Override
    public void onSerialReadFive(byte[] data) {
        receive5(data);
    }

    @Override
    public void onSerialIoErrorFive(Exception e, Integer fromCode) {
        BTConstants.BT_LINK_FIVE_STATUS = false;
        status5("Disconnect");
        e.printStackTrace();
        if (AppConstants.GENERATE_LOGS)
            AppConstants.writeInFile(TAG + "BTSPPLink_5: <SerialIoError: " + e.getMessage() + "; ErrorCode: " + fromCode + ">");
    }

    public void connect5() {
        try {

            if (BTConstants.DEVICE_ADDRESS_5 != null && !BTConstants.DEVICE_ADDRESS_5.isEmpty()) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(BTConstants.DEVICE_ADDRESS_5);
                status5("Connecting...");
                //BTConstants.BT_LINK_FIVE_STATUS = false;
                SerialSocketFive socket = new SerialSocketFive(activity.getApplicationContext(), device);
                service5.connect(socket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send5(String str) {
        if (!BTConstants.BT_LINK_FIVE_STATUS) {
            BTConstants.CURRENT_COMMAND_LINK_FIVE = "";
            Log.i(TAG, "BTSPPLink_5: Link not connected");
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "BTSPPLink_5: Link not connected");
            return;
        }
        try {
            //Log command sent:str
            if (!str.equalsIgnoreCase(BTConstants.FD_CHECK_COMMAND)) {
                BTConstants.CURRENT_COMMAND_LINK_FIVE = str;
            }
            Log.i(TAG, "BTSPPLink_5: Requesting..." + str);
            byte[] data = (str + newline).getBytes();
            service5.write(data);
        } catch (Exception e) {
            onSerialIoErrorFive(e, 1);
        }
    }

    public void sendBytes5(byte[] data) {
        if (!BTConstants.BT_LINK_FIVE_STATUS) {
            BTConstants.CURRENT_COMMAND_LINK_FIVE = "";
            Log.i(TAG, "BTSPPLink_5: Link not connected");
            //Toast.makeText(activity, "BTSPPLink_5: Link not connected", Toast.LENGTH_SHORT).show();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "BTSPPLink_5: Link not connected");
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
        Log.i(TAG, "BTSPPLink_5: Request>>" + BTConstants.CURRENT_COMMAND_LINK_FIVE);
        Log.i(TAG, "BTSPPLink_5: Response>>" + spn.toString());

        //==========================================
        if (BTConstants.CURRENT_COMMAND_LINK_FIVE.equalsIgnoreCase(BTConstants.INFO_COMMAND) && Response.contains("mac_address")) {
            BTConstants.IS_NEW_VERSION_LINK_FIVE = true;
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
            if (BTConstants.IS_NEW_VERSION_LINK_FIVE || BTConstants.FOR_OSCILLOSCOPE || (BTConstants.CURRENT_COMMAND_LINK_FIVE.equalsIgnoreCase(BTConstants.INFO_COMMAND) && !Response.contains("BTMAC")) || BTConstants.CURRENT_COMMAND_LINK_FIVE.contains(BTConstants.P_TYPE_COMMAND)) {
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
        broadcastIntent.putExtra("Request", BTConstants.CURRENT_COMMAND_LINK_FIVE);
        broadcastIntent.putExtra("Response", spn.trim());
        broadcastIntent.putExtra("Action", "BlueLinkFive");
        activity.sendBroadcast(broadcastIntent);
    }

    public void status5(String str) {
        Log.i(TAG, "Status5:" + str);
        BTConstants.BT_STATUS_STR_FIVE = str;
    }
    //Link Five code ends.......
    //endregion

    //region Link Six code
    //Link Six code begins....
    @Override
    public void onSerialConnectSix() {
        BTConstants.BT_LINK_SIX_STATUS = true;
        status6("Connected");
    }

    @Override
    public void onSerialConnectErrorSix(Exception e) {
        BTConstants.BT_LINK_SIX_STATUS = false;
        status6("Disconnect");
        e.printStackTrace();
        if (AppConstants.GENERATE_LOGS)
            AppConstants.writeInFile(TAG + "BTSPPLink_6: <SerialConnectError: " + e.getMessage() + ">");
    }

    @Override
    public void onSerialReadSix(byte[] data) {
        receive6(data);
    }

    @Override
    public void onSerialIoErrorSix(Exception e, Integer fromCode) {
        BTConstants.BT_LINK_SIX_STATUS = false;
        status6("Disconnect");
        e.printStackTrace();
        if (AppConstants.GENERATE_LOGS)
            AppConstants.writeInFile(TAG + "BTSPPLink_6: <SerialIoError: " + e.getMessage() + "; ErrorCode: " + fromCode + ">");
    }

    public void connect6() {
        try {

            if (BTConstants.DEVICE_ADDRESS_6 != null && !BTConstants.DEVICE_ADDRESS_6.isEmpty()) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(BTConstants.DEVICE_ADDRESS_6);
                status6("Connecting...");
                //BTConstants.BT_LINK_SIX_STATUS = false;
                SerialSocketSix socket = new SerialSocketSix(activity.getApplicationContext(), device);
                service6.connect(socket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send6(String str) {
        if (!BTConstants.BT_LINK_SIX_STATUS) {
            BTConstants.CURRENT_COMMAND_LINK_SIX = "";
            Log.i(TAG, "BTSPPLink_6: Link not connected");
            //Toast.makeText(activity, "BTSPPLink_6: Link not connected", Toast.LENGTH_SHORT).show();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "BTSPPLink_6: Link not connected");
            return;
        }
        try {
            //Log command sent:str
            if (!str.equalsIgnoreCase(BTConstants.FD_CHECK_COMMAND)) {
                BTConstants.CURRENT_COMMAND_LINK_SIX = str;
            }
            Log.i(TAG, "BTSPPLink_6: Requesting..." + str);
            byte[] data = (str + newline).getBytes();
            service6.write(data);
        } catch (Exception e) {
            onSerialIoErrorSix(e, 1);
        }
    }

    public void sendBytes6(byte[] data) {
        if (!BTConstants.BT_LINK_SIX_STATUS) {
            BTConstants.CURRENT_COMMAND_LINK_SIX = "";
            Log.i(TAG, "BTSPPLink_6: Link not connected");
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "BTSPPLink_6: Link not connected");
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
        Log.i(TAG, "BTSPPLink_6: Request>>" + BTConstants.CURRENT_COMMAND_LINK_SIX);
        Log.i(TAG, "BTSPPLink_6: Response>>" + spn.toString());

        //==========================================
        if (BTConstants.CURRENT_COMMAND_LINK_SIX.equalsIgnoreCase(BTConstants.INFO_COMMAND) && Response.contains("mac_address")) {
            BTConstants.IS_NEW_VERSION_LINK_SIX = true;
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
            if (BTConstants.IS_NEW_VERSION_LINK_SIX || BTConstants.FOR_OSCILLOSCOPE || (BTConstants.CURRENT_COMMAND_LINK_SIX.equalsIgnoreCase(BTConstants.INFO_COMMAND) && !Response.contains("BTMAC")) || BTConstants.CURRENT_COMMAND_LINK_SIX.contains(BTConstants.P_TYPE_COMMAND)) {
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
        broadcastIntent.putExtra("Request", BTConstants.CURRENT_COMMAND_LINK_SIX);
        broadcastIntent.putExtra("Response", spn.trim());
        broadcastIntent.putExtra("Action", "BlueLinkSix");
        activity.sendBroadcast(broadcastIntent);
    }

    public void status6(String str) {
        Log.i(TAG, "Status6:" + str);
        BTConstants.BT_STATUS_STR_SIX = str;
    }
    //Link Six code ends.......
    //endregion

}
