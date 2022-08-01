package com.TrakEngineering.FluidSecureHub.offline;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.AppConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

public class OfflineConstants {

    public static final String TAG = OfflineConstants.class.getSimpleName();

    public static void storeCurrentTransaction(Context ctx, String HubId, String SiteId, String VehicleId, String CurrentOdometer, String CurrentHours, String PersonId,
                                               String FuelQuantity, String TransactionDateTime, String VehicleNumber) {

        SharedPreferences pref = ctx.getSharedPreferences("storeCurrentTransaction", 0);
        SharedPreferences.Editor editor = pref.edit();

        // Storing
        if (!HubId.trim().isEmpty())
            editor.putString("HubId", HubId);

        if (!SiteId.trim().isEmpty())
            editor.putString("SiteId", SiteId);

        if (!VehicleId.trim().isEmpty())
            editor.putString("VehicleId", VehicleId);

        if (!CurrentOdometer.trim().isEmpty())
            editor.putString("CurrentOdometer", CurrentOdometer);

        if (!CurrentHours.trim().isEmpty())
            editor.putString("CurrentHours", CurrentHours);

        if (!PersonId.trim().isEmpty())
            editor.putString("PersonId", PersonId);

        if (!FuelQuantity.trim().isEmpty())
            editor.putString("FuelQuantity", FuelQuantity);

        if (!TransactionDateTime.trim().isEmpty())
            editor.putString("TransactionDateTime", TransactionDateTime);

        if (!VehicleNumber.trim().isEmpty())
            editor.putString("VehicleNumber", VehicleNumber);

        // commit changes
        editor.apply();
    }


    public static EntityOffTranz getCurrentTransaction(Context ctx) {


        SharedPreferences sharedPref = ctx.getSharedPreferences("storeCurrentTransaction", Context.MODE_PRIVATE);

        EntityOffTranz eot = new EntityOffTranz();
        eot.HubId = sharedPref.getString("HubId", "");
        eot.SiteId = sharedPref.getString("SiteId", "");
        eot.VehicleId = sharedPref.getString("VehicleId", "0");
        eot.CurrentOdometer = sharedPref.getString("CurrentOdometer", "");
        eot.CurrentHours = sharedPref.getString("CurrentHours", "");
        eot.PersonId = sharedPref.getString("PersonId", "");
        eot.FuelQuantity = sharedPref.getString("FuelQuantity", "");
        eot.TransactionDateTime = sharedPref.getString("TransactionDateTime", "");
        eot.VehicleNumber = sharedPref.getString("VehicleNumber", "");

        return eot;
    }


    public static void storeFuelLimit(Context ctx, String vehicleId, String vehicleFuelLimitPerTxn, String vehicleFuelLimitPerDay,
                                      String personId, String personFuelLimitPerTxn, String personFuelLimitPerDay) {

        SharedPreferences pref = ctx.getSharedPreferences("storeFuelLimit", 0);
        SharedPreferences.Editor editor = pref.edit();

        // Storing
        if (!vehicleId.trim().isEmpty())
            editor.putString("vehicleId", vehicleId);

        if (!vehicleFuelLimitPerTxn.trim().isEmpty())
            editor.putString("vehicleFuelLimitPerTxn", vehicleFuelLimitPerTxn);

        if (!vehicleFuelLimitPerDay.trim().isEmpty())
            editor.putString("vehicleFuelLimitPerDay", vehicleFuelLimitPerDay);


        if (!personId.trim().isEmpty())
            editor.putString("personId", personId);

        if (!personFuelLimitPerTxn.trim().isEmpty())
            editor.putString("personFuelLimitPerTxn", personFuelLimitPerTxn);

        if (!personFuelLimitPerDay.trim().isEmpty())
            editor.putString("personFuelLimitPerDay", personFuelLimitPerDay);

        // commit changes
        editor.apply();
    }

    public static double getFuelLimit(Context ctx) {

        double calculatedFuelLimit = 0;
        double FinalcalculatedFuelLimit = 0;

        SharedPreferences sharedPref = ctx.getSharedPreferences("storeFuelLimit", Context.MODE_PRIVATE);

        EntityFuelLimit efl = new EntityFuelLimit();

        efl.vehicleId = sharedPref.getString("vehicleId", "");
        efl.vehicleFuelLimitPerTxn = sharedPref.getString("vehicleFuelLimitPerTxn", "");
        efl.vehicleFuelLimitPerDay = sharedPref.getString("vehicleFuelLimitPerDay", "");
        efl.personId = sharedPref.getString("personId", "");
        efl.personFuelLimitPerTxn = sharedPref.getString("personFuelLimitPerTxn", "");
        efl.personFuelLimitPerDay = sharedPref.getString("personFuelLimitPerDay", "");

        double minVehicle = 0, minPerson = 0;
        double mindaylimit = getFuelLimitPerDay(ctx);

        if (!efl.vehicleFuelLimitPerTxn.trim().isEmpty())
            minVehicle = Double.parseDouble(efl.vehicleFuelLimitPerTxn);

        if (!efl.personFuelLimitPerTxn.trim().isEmpty())
            minPerson = Double.parseDouble(efl.personFuelLimitPerTxn);

        if (minVehicle < minPerson)
            calculatedFuelLimit = minPerson;
        else
            calculatedFuelLimit = minVehicle;


        if (mindaylimit == -1){
            FinalcalculatedFuelLimit = mindaylimit;
        }else if (calculatedFuelLimit < mindaylimit){
            FinalcalculatedFuelLimit = mindaylimit;
        }else{
            FinalcalculatedFuelLimit = calculatedFuelLimit;
        }



        return FinalcalculatedFuelLimit;
    }

    public static double getFuelLimitPerDay(Context ctx){

        double FinalDayLimit =  0;

        SharedPreferences sharedPref = ctx.getSharedPreferences("storeFuelLimit", Context.MODE_PRIVATE);
        String vehicleId = sharedPref.getString("vehicleId", "");
        String PersonId = sharedPref.getString("personId", "");
        double InitVehicleFuelLimitPerDay = Double.parseDouble(sharedPref.getString("vehicleFuelLimitPerDay", "0"));
        double InitPersonFuelLimitPerDay = Double.parseDouble(sharedPref.getString("personFuelLimitPerDay", "0"));
        double vehicleFuelLimitPerDay = InitVehicleFuelLimitPerDay;
        double personFuelLimitPerDay = InitPersonFuelLimitPerDay;

        try {
            //sync offline transactions
            OffDBController offcontroller = new OffDBController(ctx);
            String off_json = offcontroller.getAllOfflineTransactionJSON(ctx);
            JSONObject jobj = new JSONObject(off_json);
            String offtransactionArray = jobj.getString("TransactionsModelsObj");

            JSONArray jarrsy = new JSONArray(offtransactionArray);
            for (int i = 0; i < jarrsy.length(); i++) {
                double Pulses = 0;
                String TransactionDateTime = jarrsy.getJSONObject(i).getString("TransactionDateTime");
                String VehicleId = jarrsy.getJSONObject(i).getString("VehicleId");
                String personId = jarrsy.getJSONObject(i).getString("PersonId");
                String pp = jarrsy.getJSONObject(i).getString("Pulses");
                if (!pp.isEmpty())
                Pulses = Double.parseDouble(pp);
                String Tdate = AppConstants.currentDateFormat("yyyy-MM-dd");

                //count for vehicleid fuel consumed
                if (VehicleId.equalsIgnoreCase(vehicleId) && Pulses > 0 && TransactionDateTime.contains(Tdate) && InitVehicleFuelLimitPerDay > 0){
                    vehicleFuelLimitPerDay = vehicleFuelLimitPerDay - Pulses;
                }

                //count for personid fuel consumed
                if (PersonId.equalsIgnoreCase(personId) && Pulses > 0 && TransactionDateTime.contains(Tdate) && InitPersonFuelLimitPerDay > 0){
                    personFuelLimitPerDay = personFuelLimitPerDay - Pulses;
                }

            }
            Log.i("tes","tedf");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (vehicleFuelLimitPerDay < 0 || personFuelLimitPerDay < 0 ){
            return FinalDayLimit = -1;
        }else if(vehicleFuelLimitPerDay == 0 && personFuelLimitPerDay == 0){
            return FinalDayLimit = 0;
        }else if (vehicleFuelLimitPerDay < personFuelLimitPerDay){
            return FinalDayLimit = personFuelLimitPerDay;
        }else if (personFuelLimitPerDay < vehicleFuelLimitPerDay){
            return FinalDayLimit = vehicleFuelLimitPerDay;
        }else{
            Log.i(TAG,"Something went wrong..");
        }

        return FinalDayLimit;
    }


    public static void storeOfflineAccess(Context ctx, String isTotalOffline, String isOffline, String OFFLineDataDwnldFreq, int OfflineDataDownloadDay, int OfflineDataDownloadTimeInHrs, int OfflineDataDownloadTimeInMin) {


        Log.i(TAG, "Scheduled offline download: " + OFFLineDataDwnldFreq + ":(" + OfflineDataDownloadDay + ") HourOfDay:" + OfflineDataDownloadTimeInHrs + " Minute:" + OfflineDataDownloadTimeInMin);

        SharedPreferences pref = ctx.getSharedPreferences("storeOfflineAccess", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("isTotalOffline", isTotalOffline);
        editor.putString("isOffline", isOffline);
        editor.putString("OFFLineDataDwnldFreq", OFFLineDataDwnldFreq);
        editor.putInt("DayOfWeek", OfflineDataDownloadDay);
        editor.putInt("HourOfDay", OfflineDataDownloadTimeInHrs);
        editor.putInt("MinuteOfHour", OfflineDataDownloadTimeInMin);
        editor.apply();
    }

    public static boolean isOfflineAccess(Context ctx) {

        SharedPreferences sharedPref = ctx.getSharedPreferences("storeOfflineAccess", Context.MODE_PRIVATE);
        String isOffline = sharedPref.getString("isOffline", "");


        if (isOffline.trim().equalsIgnoreCase("True"))
            return true;
        else
            return false;

    }

    public static boolean isTotalOfflineEnabled(Context ctx) {

        SharedPreferences sharedPref = ctx.getSharedPreferences("storeOfflineAccess", Context.MODE_PRIVATE);
        String isTotalOffline = sharedPref.getString("isTotalOffline", "");


        if (isTotalOffline.trim().equalsIgnoreCase("True"))
            return true;
        else
            return false;

    }

    public static void setAlarmManagerToStartDownloadOfflineData(Context ctx) {

        try {
            Log.i(TAG, " setAlarmManagerToStartDownloadOfflineData _templog");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " setAlarmManagerToStartDownloadOfflineData _templog");

            SharedPreferences sharedPref = ctx.getSharedPreferences("storeOfflineAccess", Context.MODE_PRIVATE);
            String isOffline = sharedPref.getString("isOffline", "");
            String OFFLineDataDwnldFreq = sharedPref.getString("OFFLineDataDwnldFreq", "Weekly");

            int DayOfWeek = sharedPref.getInt("DayOfWeek", 2);
            int HourOfDay = sharedPref.getInt("HourOfDay", 2);
            int MinuteOfHour = sharedPref.getInt("MinuteOfHour", 22);

            PendingIntent alarmIntent = PendingIntent.getService(ctx, 0,
                    new Intent(ctx, OffBackgroundService.class), 0);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, HourOfDay);
            calendar.set(Calendar.MINUTE, MinuteOfHour);

            AlarmManager alarmMgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, alarmIntent);

        } catch (Exception e) {
            Log.i(TAG, " setAlarmManagerToStartDownloadOfflineData Exception:" + e.toString());
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " setAlarmManagerToStartDownloadOfflineData Exception:" + e.toString());
        }
    }


    private static int getRandomNum(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }



    public static String GetOfflineDatabaseSize(Context myctx) {


        String Size = "";

        File f = myctx.getDatabasePath("FSHubOffline.db");
        // Get length of file in bytes
        long fileSizeInBytes = f.length();
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        long fileSizeInKB = fileSizeInBytes / 1024;
        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        long fileSizeInMB = fileSizeInKB / 1024;

        if (fileSizeInMB < 1) {
            Size = fileSizeInKB + "KB";
        } else {
            Size = fileSizeInMB + "MB";
        }

        return Size;

    }
}
