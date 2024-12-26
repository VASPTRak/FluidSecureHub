package com.TrakEngineering.FluidSecureHub.BTSPP;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.AppConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CommonFunctions {

public static final String TAG = CommonFunctions.class.getSimpleName();

public static boolean CheckIfPresentInPairedDeviceList(String SelMac){

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    // Get paired devices.
    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
    if (pairedDevices.size() > 0) {
        // There are paired devices. Get the name and address of each paired device.
        for (BluetoothDevice device : pairedDevices) {
            String deviceName = device.getName();
            String deviceHardwareAddress = device.getAddress(); // MAC address
            if (deviceHardwareAddress.equalsIgnoreCase(SelMac)){
                //BTConstants.DEVICE_ADDRESS_1 = deviceHardwareAddress;
                device.createBond();
                return true;
            }
        }
    }
    Log.i(TAG,"Selected link not in bluetooth pair devices list. HardwareAddress:"+SelMac);
    if (AppConstants.GENERATE_LOGS) AppConstants.writeInFile(TAG+ " Selected link not in bluetooth pair devices list. HardwareAddress:"+SelMac);
    return false;
}

    public static String ShiftMacAddress(String macAddress,String mtd,int val){

    String fhex = "";
    try {

        String str1 ="";
        String str2 ="";//lastOctet
        String mac = macAddress.replace(":", "").trim();
        int tlen = mac.length();
        int ilen = mac.length() - 8;
        for (int i = 0; i < tlen ; i++) {
            if (i<ilen){
                str1 = str1 + mac.charAt(i);
            }else{
                str2 = str2 + mac.charAt(i);
            }
        }

        //Conversion
        if (!str2.isEmpty() && str2.length() == 8) {

            long dec = hex2decimal(str2);
            if (mtd.equalsIgnoreCase("A")){
                dec = dec + val;
            }else{
                dec = dec - val;
            }

            String hex = decimal2hex(dec);
            Log.i(TAG, "Int value:" + dec);
            fhex = ConvertToMacAddressFormat(str1+hex);

        }

    }catch (Exception e){
        e.printStackTrace();
        if (AppConstants.GENERATE_LOGS)
            AppConstants.writeInFile(TAG+" ShiftMacAddress Exception:"+e.toString());
    }
    return  fhex;
}

    public static long hex2decimal(String s) {
        String digits = "0123456789ABCDEF";
        s = s.toUpperCase();
        long val = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = digits.indexOf(c);
            val = 16*val + d;
        }
        return val;
    }

    public static String decimal2hex(long d) {
        String digits = "0123456789ABCDEF";
        if (d <= 0) return "0";
        int base = 16;   // flexible to change in any base under 16
        String hex = "";
        while (d > 0) {
            int digit = (int) (d % base);              // rightmost digit
            hex = digits.charAt(digit) + hex;  // string concatenation
            d = d / base;
        }
        return hex;
    }

    public static String ConvertToMacAddressFormat(String mac_str) {

        String str = mac_str;
        String mac_address = "";

        List<String> strings = new ArrayList<String>();
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

        return mac_address.toUpperCase();
    }

}
