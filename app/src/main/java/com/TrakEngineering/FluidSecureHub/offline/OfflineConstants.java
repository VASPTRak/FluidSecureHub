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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class OfflineConstants {

    public static final String TAG = AppConstants.LOG_BACKGROUND + "-" + OfflineConstants.class.getSimpleName();

    public static void storeCurrentTransaction(Context ctx, String HubId, String SiteId, String VehicleId, String CurrentOdometer, String CurrentHours, String PersonId,
                                               String FuelQuantity, String TransactionDateTime, String VehicleNumber, String Other) {

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

        if (!Other.trim().isEmpty())
            editor.putString("Other", Other);

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
        eot.Other = sharedPref.getString("Other", "");

        return eot;
    }


    public static void storeFuelLimit(Context ctx, String vehicleId, String vehicleFuelLimitPerTxn, String vehicleFuelLimitPerDay, String CheckFuelLimitPerMonth,
                                      String vehicleFuelLimitPerMonth, String FuelQuantityOfVehiclePerMonth, String personId, String personFuelLimitPerTxn,
                                      String personFuelLimitPerDay) {

        SharedPreferences pref = ctx.getSharedPreferences("storeFuelLimit", 0);
        SharedPreferences.Editor editor = pref.edit();

        // Storing
        if (!vehicleId.trim().isEmpty())
            editor.putString("vehicleId", vehicleId);

        if (!vehicleFuelLimitPerTxn.trim().isEmpty())
            editor.putString("vehicleFuelLimitPerTxn", vehicleFuelLimitPerTxn);

        if (!vehicleFuelLimitPerDay.trim().isEmpty())
            editor.putString("vehicleFuelLimitPerDay", vehicleFuelLimitPerDay);

        if (!CheckFuelLimitPerMonth.trim().isEmpty())
            editor.putString("CheckFuelLimitPerMonth", CheckFuelLimitPerMonth);

        if (!vehicleFuelLimitPerMonth.trim().isEmpty())
            editor.putString("vehicleFuelLimitPerMonth", vehicleFuelLimitPerMonth);

        if (!FuelQuantityOfVehiclePerMonth.trim().isEmpty())
            editor.putString("FuelQuantityOfVehiclePerMonth", FuelQuantityOfVehiclePerMonth);


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
        double FinalCalculatedFuelLimit = 0;

        try {
            SharedPreferences sharedPref = ctx.getSharedPreferences("storeFuelLimit", Context.MODE_PRIVATE);

            EntityFuelLimit efl = new EntityFuelLimit();

            efl.vehicleId = sharedPref.getString("vehicleId", "");
            efl.vehicleFuelLimitPerTxn = sharedPref.getString("vehicleFuelLimitPerTxn", "");
            efl.vehicleFuelLimitPerDay = sharedPref.getString("vehicleFuelLimitPerDay", "");
            efl.CheckFuelLimitPerMonth = sharedPref.getString("CheckFuelLimitPerMonth", "");
            efl.vehicleFuelLimitPerMonth = sharedPref.getString("vehicleFuelLimitPerMonth", "");
            efl.FuelQuantityOfVehiclePerMonth = sharedPref.getString("FuelQuantityOfVehiclePerMonth", "");
            efl.personId = sharedPref.getString("personId", "");
            efl.personFuelLimitPerTxn = sharedPref.getString("personFuelLimitPerTxn", "");
            efl.personFuelLimitPerDay = sharedPref.getString("personFuelLimitPerDay", "");

            double minVehiclePerTxn = 0, minPersonPerTxn = 0, limitPerMonth = 0;
            double limitPerDay = getCommonFuelLimitPerDay(ctx);
            if (efl.CheckFuelLimitPerMonth.trim().equalsIgnoreCase("true")) {
                limitPerMonth = getFuelLimitPerMonth(ctx);
            }

            if (!efl.vehicleFuelLimitPerTxn.trim().isEmpty()) {
                minVehiclePerTxn = Double.parseDouble(efl.vehicleFuelLimitPerTxn);
            }
            if (!efl.personFuelLimitPerTxn.trim().isEmpty()) {
                minPersonPerTxn = Double.parseDouble(efl.personFuelLimitPerTxn);
            }

            /*if (minVehicle < minPerson) {  // OLD code
                calculatedFuelLimit = minPerson;
            } else {
                calculatedFuelLimit = minVehicle;
            }

            if (limitPerDay == -1) {
                FinalCalculatedFuelLimit = limitPerDay;
            } else if (calculatedFuelLimit < limitPerDay) {
                FinalCalculatedFuelLimit = limitPerDay;
            } else {
                FinalCalculatedFuelLimit = calculatedFuelLimit;
            }*/

            if (minVehiclePerTxn < minPersonPerTxn) {
                if (minVehiclePerTxn > 0) {
                    calculatedFuelLimit = minVehiclePerTxn;
                } else {
                    calculatedFuelLimit = minPersonPerTxn;
                }
            } else {
                if (minPersonPerTxn > 0) {
                    calculatedFuelLimit = minPersonPerTxn;
                } else {
                    calculatedFuelLimit = minVehiclePerTxn;
                }
            }

            if (calculatedFuelLimit < limitPerDay) {
                if (calculatedFuelLimit > 0) {
                    FinalCalculatedFuelLimit = calculatedFuelLimit;
                } else {
                    FinalCalculatedFuelLimit = limitPerDay;
                }
            } else {
                if (limitPerDay > 0) {
                    FinalCalculatedFuelLimit = limitPerDay;
                } else {
                    FinalCalculatedFuelLimit = calculatedFuelLimit;
                }
            }

            if (limitPerMonth > 0) {
                if (limitPerMonth < FinalCalculatedFuelLimit) {
                    FinalCalculatedFuelLimit = limitPerMonth;
                }
            }
            //AppConstants.WriteinFile(TAG + " limitPerMonth: " + limitPerMonth + "; FinalCalculatedFuelLimit: " + FinalCalculatedFuelLimit);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " getFuelLimit Exception: " + ex.getMessage());
        }
        return FinalCalculatedFuelLimit;
    }

    public static double getFuelLimitPerMonth(Context ctx) {
        double FinalMonthLimit = 0;
        try {

            SharedPreferences sharedPref = ctx.getSharedPreferences("storeFuelLimit", Context.MODE_PRIVATE);
            String vehicleId = sharedPref.getString("vehicleId", "");
            String CheckFuelLimitPerMonth = sharedPref.getString("CheckFuelLimitPerMonth", "");
            double InitVehicleFuelLimitPerMonth = Double.parseDouble(sharedPref.getString("vehicleFuelLimitPerMonth", "0"));
            double FuelQuantityOfVehiclePerMonth = Double.parseDouble(sharedPref.getString("FuelQuantityOfVehiclePerMonth", "0"));
            double vehicleFuelLimitPerMonth = InitVehicleFuelLimitPerMonth - FuelQuantityOfVehiclePerMonth;

            if (CheckFuelLimitPerMonth != null) {
                if (CheckFuelLimitPerMonth.trim().equalsIgnoreCase("true")) {
                    //sync offline transactions
                    OffDBController offcontroller = new OffDBController(ctx);
                    String off_json = offcontroller.getAllOfflineTransactionJSON(ctx);
                    JSONObject jObj = new JSONObject(off_json);
                    String offTransactionArray = jObj.getString("TransactionsModelsObj");

                    JSONArray jArray = new JSONArray(offTransactionArray);
                    for (int i = 0; i < jArray.length(); i++) {
                        //double Pulses = 0;
                        double Quantity = 0;
                        String TransactionDateTime = jArray.getJSONObject(i).getString("TransactionDateTime");
                        String VehicleId = jArray.getJSONObject(i).getString("VehicleId");
                        //String storedPulses = jArray.getJSONObject(i).getString("Pulses");
                        String storedQty = jArray.getJSONObject(i).getString("FuelQuantity");
                        /*if (!storedPulses.isEmpty())
                        Pulses = Double.parseDouble(storedPulses);*/
                        if (!storedQty.isEmpty())
                            Quantity = Double.parseDouble(storedQty);
                        String Tdate = AppConstants.currentDateFormat("yyyy-MM");

                        //count for vehicleid fuel consumed
                        if (VehicleId.equalsIgnoreCase(vehicleId) && Quantity > 0 && TransactionDateTime.contains(Tdate) && InitVehicleFuelLimitPerMonth > 0) {
                            vehicleFuelLimitPerMonth = vehicleFuelLimitPerMonth - Quantity;
                        }
                    }

                    FinalMonthLimit = vehicleFuelLimitPerMonth;
                }
            }
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " getFuelLimitPerMonth Exception: " + ex.getMessage());
        }
        return FinalMonthLimit;
    }

    public static double getVehicleFuelLimitPerDay(Context ctx) {
        double FinalDayLimit = 0;

        try {
            SharedPreferences sharedPref = ctx.getSharedPreferences("storeFuelLimit", Context.MODE_PRIVATE);
            String vehicleId = sharedPref.getString("vehicleId", "");
            double InitVehicleFuelLimitPerDay = Double.parseDouble(sharedPref.getString("vehicleFuelLimitPerDay", "0"));
            double vehicleFuelLimitPerDay = InitVehicleFuelLimitPerDay;

            //sync offline transactions
            OffDBController offcontroller = new OffDBController(ctx);
            String off_json = offcontroller.getAllOfflineTransactionJSON(ctx);
            JSONObject jObj = new JSONObject(off_json);
            String offTransactionArray = jObj.getString("TransactionsModelsObj");

            JSONArray jArray = new JSONArray(offTransactionArray);
            for (int i = 0; i < jArray.length(); i++) {
                //double Pulses = 0;
                double Quantity = 0;
                String TransactionDateTime = jArray.getJSONObject(i).getString("TransactionDateTime");
                String VehicleId = jArray.getJSONObject(i).getString("VehicleId");
                //String pp = jArray.getJSONObject(i).getString("Pulses");
                String qty = jArray.getJSONObject(i).getString("FuelQuantity");
                /*if (!pp.isEmpty())
                    Pulses = Double.parseDouble(pp);*/
                if (!qty.isEmpty())
                    Quantity = Double.parseDouble(qty);
                String Tdate = AppConstants.currentDateFormat("yyyy-MM-dd");

                //count for vehicleid fuel consumed
                if (VehicleId.equalsIgnoreCase(vehicleId) && Quantity > 0 && TransactionDateTime.contains(Tdate) && InitVehicleFuelLimitPerDay > 0) {
                    vehicleFuelLimitPerDay = vehicleFuelLimitPerDay - Quantity;
                }
            }

            FinalDayLimit = vehicleFuelLimitPerDay;
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " getVehicleFuelLimitPerDay Exception: " + ex.getMessage());
        }
        return FinalDayLimit;
    }

    public static double getPersonFuelLimitPerDay(Context ctx) {
        double FinalDayLimit = 0;

        try {
            SharedPreferences sharedPref = ctx.getSharedPreferences("storeFuelLimit", Context.MODE_PRIVATE);
            String PersonId = sharedPref.getString("personId", "");
            double InitPersonFuelLimitPerDay = Double.parseDouble(sharedPref.getString("personFuelLimitPerDay", "0"));
            double personFuelLimitPerDay = InitPersonFuelLimitPerDay;

            //sync offline transactions
            OffDBController offcontroller = new OffDBController(ctx);
            String off_json = offcontroller.getAllOfflineTransactionJSON(ctx);
            JSONObject jObj = new JSONObject(off_json);
            String offTransactionArray = jObj.getString("TransactionsModelsObj");

            JSONArray jArray = new JSONArray(offTransactionArray);
            for (int i = 0; i < jArray.length(); i++) {
                //double Pulses = 0;
                double Quantity = 0;
                String TransactionDateTime = jArray.getJSONObject(i).getString("TransactionDateTime");
                String personId = jArray.getJSONObject(i).getString("PersonId");
                //String pp = jArray.getJSONObject(i).getString("Pulses");
                String qty = jArray.getJSONObject(i).getString("FuelQuantity");
                /*if (!pp.isEmpty())
                    Pulses = Double.parseDouble(pp);*/
                if (!qty.isEmpty())
                    Quantity = Double.parseDouble(qty);
                String Tdate = AppConstants.currentDateFormat("yyyy-MM-dd");

                //count for personid fuel consumed
                if (PersonId.equalsIgnoreCase(personId) && Quantity > 0 && TransactionDateTime.contains(Tdate) && InitPersonFuelLimitPerDay > 0) {
                    personFuelLimitPerDay = personFuelLimitPerDay - Quantity;
                }
            }

            FinalDayLimit = personFuelLimitPerDay;
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " getPersonFuelLimitPerDay Exception: " + ex.getMessage());
        }
        return FinalDayLimit;
    }

    public static double getCommonFuelLimitPerDay(Context ctx) {
        double FinalDayLimit = 0;

        try {
            SharedPreferences sharedPref = ctx.getSharedPreferences("storeFuelLimit", Context.MODE_PRIVATE);
            String vehicleId = sharedPref.getString("vehicleId", "");
            String PersonId = sharedPref.getString("personId", "");
            double InitVehicleFuelLimitPerDay = Double.parseDouble(sharedPref.getString("vehicleFuelLimitPerDay", "0"));
            double InitPersonFuelLimitPerDay = Double.parseDouble(sharedPref.getString("personFuelLimitPerDay", "0"));
            double vehicleFuelLimitPerDay = InitVehicleFuelLimitPerDay;
            double personFuelLimitPerDay = InitPersonFuelLimitPerDay;

            //sync offline transactions
            OffDBController offcontroller = new OffDBController(ctx);
            String off_json = offcontroller.getAllOfflineTransactionJSON(ctx);
            JSONObject jObj = new JSONObject(off_json);
            String offTransactionArray = jObj.getString("TransactionsModelsObj");

            JSONArray jArray = new JSONArray(offTransactionArray);
            for (int i = 0; i < jArray.length(); i++) {
                //double Pulses = 0;
                double Quantity = 0;
                String TransactionDateTime = jArray.getJSONObject(i).getString("TransactionDateTime");
                String VehicleId = jArray.getJSONObject(i).getString("VehicleId");
                String personId = jArray.getJSONObject(i).getString("PersonId");
                //String pp = jArray.getJSONObject(i).getString("Pulses");
                String qty = jArray.getJSONObject(i).getString("FuelQuantity");
                /*if (!pp.isEmpty())
                    Pulses = Double.parseDouble(pp);*/
                if (!qty.isEmpty())
                    Quantity = Double.parseDouble(qty);
                String Tdate = AppConstants.currentDateFormat("yyyy-MM-dd");

                //count for vehicleid fuel consumed
                if (VehicleId.equalsIgnoreCase(vehicleId) && Quantity > 0 && TransactionDateTime.contains(Tdate) && InitVehicleFuelLimitPerDay > 0) {
                    vehicleFuelLimitPerDay = vehicleFuelLimitPerDay - Quantity;
                }

                //count for personid fuel consumed
                if (PersonId.equalsIgnoreCase(personId) && Quantity > 0 && TransactionDateTime.contains(Tdate) && InitPersonFuelLimitPerDay > 0) {
                    personFuelLimitPerDay = personFuelLimitPerDay - Quantity;
                }
            }

            /*if (vehicleFuelLimitPerDay < 0 || personFuelLimitPerDay < 0) {  // OLD code
                FinalDayLimit = -1;
            } else if (vehicleFuelLimitPerDay == 0 && personFuelLimitPerDay == 0) {
                FinalDayLimit = 0;
            } else if (vehicleFuelLimitPerDay < personFuelLimitPerDay) {
                FinalDayLimit = personFuelLimitPerDay;
            } else if (personFuelLimitPerDay < vehicleFuelLimitPerDay) {
                FinalDayLimit = vehicleFuelLimitPerDay;
            } else {
                Log.i(TAG, "Something went wrong..");
            }*/

            if (vehicleFuelLimitPerDay < personFuelLimitPerDay) {
                if (InitVehicleFuelLimitPerDay > 0) {
                    FinalDayLimit = vehicleFuelLimitPerDay;
                } else {
                    FinalDayLimit = personFuelLimitPerDay;
                }
            } else {
                if (InitPersonFuelLimitPerDay > 0) {
                    FinalDayLimit = personFuelLimitPerDay;
                } else {
                    FinalDayLimit = vehicleFuelLimitPerDay;
                }
            }
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " getCommonFuelLimitPerDay Exception: " + ex.getMessage());
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
            /*if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " setAlarmManagerToStartDownloadOfflineData _templog");*/

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
