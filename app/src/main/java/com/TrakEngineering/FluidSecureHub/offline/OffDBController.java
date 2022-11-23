package com.TrakEngineering.FluidSecureHub.offline;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.AppConstants;
import com.TrakEngineering.FluidSecureHub.CommonUtils;
import com.TrakEngineering.FluidSecureHub.enity.TankMonitorEntity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class OffDBController extends SQLiteOpenHelper {
    private static final String LOGCAT = null;
    private static final String TAG = AppConstants.LOG_BACKGROUND + "-" + "OffDBController ";

    public static String TBL_LINK = "tbl_off_link";
    public static String TBL_FUEL_TIMING = "tbl_off_fuel_timings";
    public static String TBL_VEHICLE = "tbl_off_vehicle";
    public static String TBL_PERSONNEL = "tbl_off_personnel";
    public static String TBL_TRANSACTION = "tbl_off_transaction";
    public static String TBL_OFF_TLD = "tbl_off_tld";

    public OffDBController(Context applicationcontext) {
        super(applicationcontext, "FSHubOffline.db", null, 7);
        Log.d(LOGCAT, "Created");
    }

    @Override
    public void onCreate(SQLiteDatabase database) {

        Log.i(TAG, "InOnCreate.");
        String query2 = "CREATE TABLE " + TBL_LINK + " ( Id INTEGER PRIMARY KEY, SiteId INTEGER, WifiSSId TEXT, PumpOnTime TEXT, PumpOffTime TEXT, AuthorizedFuelingDays TEXT, Pulserratio TEXT, MacAddress TEXT, IsTLDCall TEXT,LinkCommunicationType TEXT,APMacAddress TEXT,BTMacAddress TEXT)";
        database.execSQL(query2);

        String query21 = "CREATE TABLE " + TBL_FUEL_TIMING + " ( Id INTEGER PRIMARY KEY, SiteId INTEGER, PersonId INTEGER, FromTime TEXT, ToTime TEXT)";
        database.execSQL(query21);

        String query3 = "CREATE TABLE " + TBL_VEHICLE + " ( Id INTEGER PRIMARY KEY, VehicleId INTEGER, VehicleNumber TEXT,CurrentOdometer TEXT,CurrentHours TEXT,RequireOdometerEntry TEXT,RequireHours TEXT,FuelLimitPerTxn TEXT,FuelLimitPerDay TEXT,FOBNumber TEXT,AllowedLinks TEXT,Active TEXT, CheckOdometerReasonable TEXT,OdometerReasonabilityConditions TEXT,OdoLimit TEXT,HoursLimit TEXT,BarcodeNumber TEXT,IsExtraOther TEXT,ExtraOtherLabel TEXT,MagneticCardReaderNumber TEXT, CheckFuelLimitPerMonth TEXT, FuelLimitPerMonth TEXT, FuelQuantityOfVehiclePerMonth TEXT)";
        database.execSQL(query3);

        String query4 = "CREATE TABLE " + TBL_PERSONNEL + " ( Id INTEGER PRIMARY KEY, PersonId INTEGER, PinNumber TEXT, FuelLimitPerTxn TEXT,FuelLimitPerDay TEXT,FOBNumber TEXT,Authorizedlinks TEXT,AssignedVehicles TEXT,MagneticCardReaderNumber TEXT,Barcode TEXT)";
        database.execSQL(query4);

        String query5 = "CREATE TABLE " + TBL_TRANSACTION + " ( Id INTEGER PRIMARY KEY, HubId TEXT, SiteId TEXT, VehicleId INTEGER, CurrentOdometer TEXT, CurrentHours TEXT, PersonId TEXT, PersonPin TEXT, FuelQuantity TEXT, Pulses TEXT,TransactionDateTime TEXT,OfflineFakeTransactionId TEXT, VehicleNumber TEXT, Other TEXT)";
        database.execSQL(query5);

        String query6 = "CREATE TABLE " + TBL_OFF_TLD + " ( Id INTEGER PRIMARY KEY, PROBEMacAddress TEXT, Level TEXT, selSiteId INTEGER, TLDFirmwareVersion TEXT, IMEI_UDID TEXT, LSB TEXT, MSB TEXT, TLDTemperature TEXT, ReadingDateTime TEXT, Response_code TEXT, FromDirectTLD TEXT)";
        database.execSQL(query6);

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {

        Log.i(TAG,"InOnUpgrade.");
        AppConstants.WriteinFile(TAG + "onUpgrade called.");
        // If you need to add a column
        if (current_version > version_old) {
            try {
                database.execSQL("ALTER TABLE " + TBL_PERSONNEL + " ADD COLUMN MagneticCardReaderNumber TEXT");
            } catch (Exception ex) {
                Log.w(TAG, " Altering " + TBL_PERSONNEL + " for (MagneticCardReaderNumber) column: " + ex.getMessage());
            }
            try {
                database.execSQL("ALTER TABLE " + TBL_PERSONNEL + " ADD COLUMN Barcode TEXT");
            } catch (Exception ex) {
                Log.w(TAG, " Altering " + TBL_PERSONNEL + " for (Barcode) column: " + ex.getMessage());
            }
            try {
                database.execSQL("ALTER TABLE " + TBL_VEHICLE + " ADD COLUMN MagneticCardReaderNumber TEXT");
            } catch (Exception ex) {
                Log.w(TAG, " Altering " + TBL_VEHICLE + " for (MagneticCardReaderNumber) column: " + ex.getMessage());
            }
            try {
                database.execSQL("ALTER TABLE " + TBL_VEHICLE + " ADD COLUMN CheckFuelLimitPerMonth TEXT");
            } catch (Exception ex) {
                Log.w(TAG, " Altering " + TBL_VEHICLE + " for (CheckFuelLimitPerMonth) column: " + ex.getMessage());
            }
            try {
                database.execSQL("ALTER TABLE " + TBL_VEHICLE + " ADD COLUMN FuelLimitPerMonth TEXT");
            } catch (Exception ex) {
                Log.w(TAG, " Altering " + TBL_VEHICLE + " for (FuelLimitPerMonth) column: " + ex.getMessage());
            }
            try {
                database.execSQL("ALTER TABLE " + TBL_VEHICLE + " ADD COLUMN FuelQuantityOfVehiclePerMonth TEXT");
            } catch (Exception ex) {
                Log.w(TAG, " Altering " + TBL_VEHICLE + " for (FuelQuantityOfVehiclePerMonth) column: " + ex.getMessage());
            }
            try {
                database.execSQL("ALTER TABLE " + TBL_TRANSACTION + " ADD COLUMN VehicleNumber TEXT");
            } catch (Exception ex) {
                Log.w(TAG, " Altering " + TBL_TRANSACTION + " for (VehicleNumber) column: " + ex.getMessage());
            }
            try {
                database.execSQL("ALTER TABLE " + TBL_TRANSACTION + " ADD COLUMN Other TEXT");
            } catch (Exception ex) {
                Log.w(TAG, " Altering " + TBL_TRANSACTION + " for (Other) column: " + ex.getMessage());
            }

            ////
            String query6 = "CREATE TABLE IF NOT EXISTS " + TBL_OFF_TLD + " ( Id INTEGER PRIMARY KEY, PROBEMacAddress TEXT, Level TEXT, selSiteId INTEGER, TLDFirmwareVersion TEXT, IMEI_UDID TEXT, LSB TEXT, MSB TEXT, TLDTemperature TEXT, ReadingDateTime TEXT, Response_code TEXT, FromDirectTLD TEXT)";
            database.execSQL(query6);
        }

    }

    public void storeOfflineToken(Context ctx, String access_token, String token_type, String expires_in, String refresh_token) {
        SharedPreferences pref = ctx.getSharedPreferences("storeOfflineToken", 0);
        SharedPreferences.Editor editor = pref.edit();

        // Storing
        editor.putString("access_token", access_token);
        editor.putString("token_type", token_type);
        editor.putString("expires_in", expires_in);
        editor.putString("refresh_token", refresh_token);

        // commit changes
        editor.apply();
    }

    public String getOfflineToken(Context ctx) {
        SharedPreferences sharedPref = ctx.getSharedPreferences("storeOfflineToken", Context.MODE_PRIVATE);

        String access_token = sharedPref.getString("access_token", "");

        return access_token;

    }

    public void storeOfflineHubDetails(Context ctx, String HubId, String AllowedLinks, String PersonnelPINNumberRequired, String VehicleNumberRequired, String PersonhasFOB,
                                       String VehiclehasFOB, String WiFiChannel, String BluetoothCardReader, String BluetoothCardReaderMacAddress, String LFBluetoothCardReader,
                                       String LFBluetoothCardReaderMacAddress, String PrinterMacAddress, String PrinterName, String EnablePrinter, String VehicleDataFilePath,
                                       String PersonnelDataFilePath, String LinkDataFilePath, String IsNonValidateVehicle, String IsNonValidatePerson, String IsNonValidateODOM,
                                       String IsOtherRequire, String OtherLabel, String HUBType) {

        try {

            SharedPreferences pref = ctx.getSharedPreferences("storeOfflineHubDetails", 0);
            SharedPreferences.Editor editor = pref.edit();

            // Storing
            editor.putString("HubId", HubId);
            editor.putString("AllowedLinks", AllowedLinks);
            editor.putString("PersonnelPINNumberRequired", PersonnelPINNumberRequired);
            editor.putString("VehicleNumberRequired", VehicleNumberRequired);
            editor.putString("PersonhasFOB", PersonhasFOB);
            editor.putString("VehiclehasFOB", VehiclehasFOB);
            editor.putString("WiFiChannel", WiFiChannel);
            editor.putString("BluetoothCardReader", BluetoothCardReader);
            editor.putString("BluetoothCardReaderMacAddress", BluetoothCardReaderMacAddress);
            editor.putString("LFBluetoothCardReader", LFBluetoothCardReader);
            editor.putString("LFBluetoothCardReaderMacAddress", LFBluetoothCardReaderMacAddress);
            editor.putString("PrinterMacAddress", PrinterMacAddress);
            editor.putString("PrinterName", PrinterName);
            editor.putString("EnablePrinter", EnablePrinter);
            editor.putString("VehicleDataFilePath", VehicleDataFilePath);
            editor.putString("PersonnelDataFilePath", PersonnelDataFilePath);
            editor.putString("LinkDataFilePath", LinkDataFilePath);
            editor.putString("IsNonValidateVehicle", IsNonValidateVehicle);
            editor.putString("IsNonValidatePerson", IsNonValidatePerson);
            editor.putString("IsNonValidateODOM", IsNonValidateODOM);
            editor.putString("IsOtherRequire", IsOtherRequire);
            editor.putString("OtherLabel", OtherLabel);
            editor.putString("HUBType", HUBType);

            // commit changes
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "storeOfflineHubDetails Exception: " + e.getMessage());
        }
    }

    public EntityHub getOfflineHubDetails(Context ctx) {
        SharedPreferences sharedPref = ctx.getSharedPreferences("storeOfflineHubDetails", Context.MODE_PRIVATE);

        EntityHub hub = new EntityHub();

        try {
            hub.HubId = sharedPref.getString("HubId", "");
            hub.AllowedLinks = sharedPref.getString("AllowedLinks", "");
            hub.PersonnelPINNumberRequired = sharedPref.getString("PersonnelPINNumberRequired", "");
            hub.VehicleNumberRequired = sharedPref.getString("VehicleNumberRequired", "");
            hub.PersonhasFOB = sharedPref.getString("PersonhasFOB", "");
            hub.VehiclehasFOB = sharedPref.getString("VehiclehasFOB", "");
            hub.WiFiChannel = sharedPref.getString("WiFiChannel", "");
            hub.BluetoothCardReader = sharedPref.getString("BluetoothCardReader", "");
            hub.BluetoothCardReaderMacAddress = sharedPref.getString("BluetoothCardReaderMacAddress", "");
            hub.LFBluetoothCardReader = sharedPref.getString("LFBluetoothCardReader", "");
            hub.LFBluetoothCardReaderMacAddress = sharedPref.getString("LFBluetoothCardReaderMacAddress", "");
            hub.PrinterMacAddress = sharedPref.getString("PrinterMacAddress", "");
            hub.PrinterName = sharedPref.getString("PrinterName", "");
            hub.EnablePrinter = sharedPref.getString("EnablePrinter", "");

            hub.VehicleDataFilePath = sharedPref.getString("VehicleDataFilePath", "");
            hub.PersonnelDataFilePath = sharedPref.getString("PersonnelDataFilePath", "");
            hub.LinkDataFilePath = sharedPref.getString("LinkDataFilePath", "");
            hub.IsNonValidateVehicle = sharedPref.getString("IsNonValidateVehicle", "");
            hub.IsNonValidatePerson = sharedPref.getString("IsNonValidatePerson", "");
            hub.IsNonValidateODOM = sharedPref.getString("IsNonValidateODOM", "");

            hub.IsOtherRequire = sharedPref.getString("IsOtherRequire", "");
            hub.OtherLabel = sharedPref.getString("OtherLabel", "Other");
            hub.HUBType = sharedPref.getString("HUBType", "N");

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "getOfflineHubDetails Exception: " + e.getMessage());
        }
        return hub;
    }

    public long insertTLDReadings(String PROBEMacAddress, String Level, String selSiteId, String TLDFirmwareVersion,
                                  String IMEI_UDID, String LSB, String MSB, String TLDTemperature, String ReadingDateTime, String Response_code, String FromDirectTLD) {

        long insertedID = 0;
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("PROBEMacAddress", PROBEMacAddress);
            values.put("Level", Level);
            values.put("selSiteId", selSiteId);
            values.put("TLDFirmwareVersion", TLDFirmwareVersion);
            values.put("IMEI_UDID", IMEI_UDID);
            values.put("LSB", LSB);
            values.put("MSB", MSB);
            values.put("TLDTemperature", TLDTemperature);
            values.put("ReadingDateTime", ReadingDateTime);
            values.put("Response_code", Response_code);
            values.put("FromDirectTLD", FromDirectTLD);

            insertedID = database.insert(TBL_OFF_TLD, null, values);
            database.close();

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "insertTLDReadings Exception: " + e.getMessage());
        }
        return insertedID;
    }

    public long insertLinkDetails(String SiteId, String WifiSSId, String PumpOnTime, String PumpOffTime, String AuthorizedFuelingDays, String Pulserratio, String MacAddress, String IsTLDCall,String LinkCommunicationType,String APMacAddress,String BTMacAddress) {

        long insertedID = 0;
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SiteId", SiteId);
            values.put("WifiSSId", WifiSSId);
            values.put("PumpOnTime", PumpOnTime);
            values.put("PumpOffTime", PumpOffTime);
            values.put("AuthorizedFuelingDays", AuthorizedFuelingDays);
            values.put("Pulserratio", Pulserratio);
            values.put("MacAddress", MacAddress);
            values.put("IsTLDCall", IsTLDCall);
            values.put("LinkCommunicationType", LinkCommunicationType);
            values.put("APMacAddress", APMacAddress);
            values.put("BTMacAddress", BTMacAddress);

            insertedID = database.insert(TBL_LINK, null, values);
            database.close();

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "insertLinkDetails Exception: " + e.getMessage());
        }
        return insertedID;
    }


    public long insertVehicleDetails(String VehicleId, String VehicleNumber, String CurrentOdometer, String CurrentHours, String RequireOdometerEntry, String RequireHours,
                                     String FuelLimitPerTxn, String FuelLimitPerDay, String FOBNumber, String AllowedLinks, String Active, String CheckOdometerReasonable,
                                     String OdometerReasonabilityConditions, String OdoLimit, String HoursLimit, String BarcodeNumber, String IsExtraOther,
                                     String ExtraOtherLabel, String MagneticCardReaderNumber, String CheckFuelLimitPerMonth, String FuelLimitPerMonth,
                                     String FuelQuantityOfVehiclePerMonth) {
        long insertedID = 0;
        try {

            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("VehicleId", VehicleId);
            values.put("VehicleNumber", VehicleNumber);
            values.put("CurrentOdometer", CurrentOdometer);
            values.put("CurrentHours", CurrentHours);
            values.put("RequireOdometerEntry", RequireOdometerEntry);
            values.put("RequireHours", RequireHours);
            values.put("FuelLimitPerTxn", FuelLimitPerTxn);
            values.put("FuelLimitPerDay", FuelLimitPerDay);
            values.put("FOBNumber", FOBNumber);
            values.put("AllowedLinks", AllowedLinks);
            values.put("Active", Active);
            values.put("CheckOdometerReasonable", CheckOdometerReasonable);
            values.put("OdometerReasonabilityConditions", OdometerReasonabilityConditions);
            values.put("OdoLimit", OdoLimit);
            values.put("HoursLimit", HoursLimit);
            values.put("BarcodeNumber", BarcodeNumber);
            values.put("IsExtraOther", IsExtraOther);
            values.put("ExtraOtherLabel", ExtraOtherLabel);
            values.put("MagneticCardReaderNumber", MagneticCardReaderNumber);
            values.put("CheckFuelLimitPerMonth", CheckFuelLimitPerMonth);
            values.put("FuelLimitPerMonth", FuelLimitPerMonth);
            values.put("FuelQuantityOfVehiclePerMonth", FuelQuantityOfVehiclePerMonth);

            insertedID = database.insert(TBL_VEHICLE, null, values);
            database.close();

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "insertVehicleDetails Exception: " + e.getMessage());
        }
        return insertedID;
    }

    public long insertPersonnelPinDetails(String PersonId, String PinNumber, String FuelLimitPerTxn, String FuelLimitPerDay, String FOBNumber, String Authorizedlinks,
                                          String AssignedVehicles, String MagneticCardReaderNumber, String Barcode) {

        long insertedID = 0;
        try {

            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("PersonId", PersonId);
            values.put("PinNumber", PinNumber);
            values.put("FuelLimitPerTxn", FuelLimitPerTxn);
            values.put("FuelLimitPerDay", FuelLimitPerDay);
            values.put("FOBNumber", FOBNumber);
            values.put("Authorizedlinks", Authorizedlinks);
            values.put("AssignedVehicles", AssignedVehicles);
            values.put("MagneticCardReaderNumber", MagneticCardReaderNumber);
            values.put("Barcode", Barcode);

            insertedID = database.insert(TBL_PERSONNEL, null, values);
            database.close();

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "insertPersonnelPinDetails Exception: " + e.getMessage());
        }
        return insertedID;
    }

    public long insertFuelTimings(String SiteId, String PersonId, String FromTime, String ToTime) {

        long insertedID = 0;
        try {

            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SiteId", SiteId);
            values.put("PersonId", PersonId);
            values.put("FromTime", FromTime);
            values.put("ToTime", ToTime);

            insertedID = database.insert(TBL_FUEL_TIMING, null, values);
            database.close();

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "insertFuelTimings Exception: " + e.getMessage());
        }
        return insertedID;
    }


    public long insertOfflineTransactions(EntityOffTranz eot, String LinkCommunicationType) {
        long insertedID = 0;
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("HubId", eot.HubId);
            values.put("SiteId", eot.SiteId);
            values.put("VehicleId", eot.VehicleId);
            values.put("CurrentOdometer", eot.CurrentOdometer);
            values.put("CurrentHours", eot.CurrentHours);
            values.put("PersonId", eot.PersonId);
            values.put("PersonPin", eot.PersonPin);
            values.put("FuelQuantity", eot.FuelQuantity);
            values.put("Pulses", eot.Pulses);
            values.put("TransactionDateTime", eot.TransactionDateTime);
            values.put("OfflineFakeTransactionId", eot.OfflineFakeTransactionId);
            values.put("VehicleNumber", eot.VehicleNumber);
            values.put("Other", eot.Other);

            insertedID = database.insert(TBL_TRANSACTION, null, values);
            database.close();
            if (LinkCommunicationType.isEmpty()) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "<Transaction saved in local DB.>");
            } else {

                String txtnTypeForLog = "";
                if (LinkCommunicationType.equalsIgnoreCase("BT")) {
                    txtnTypeForLog = AppConstants.LOG_TXTN_BT;
                } else {
                    txtnTypeForLog = AppConstants.LOG_TXTN_HTTP;
                }

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(txtnTypeForLog + "- <Transaction saved in local DB.>");
            }

        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "insertOfflineTransactions Exception: " + e.getMessage());
        }
        return insertedID;
    }

    public int updateOfflinePulsesQuantity(String sqlite_id, String Pulses, String Quantity, String OfflineFakeTransactionId) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Pulses", Pulses);
        values.put("FuelQuantity", Quantity);
        values.put("OfflineFakeTransactionId", OfflineFakeTransactionId);

        return database.update(TBL_TRANSACTION, values, "Id" + " = ?", new String[]{sqlite_id});
    }


    public void deleteTableData(String tablename) {
        Log.d(LOGCAT, "delete");
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM  " + tablename;
        Log.d("query", deleteQuery);
        database.execSQL(deleteQuery);
    }

    public void deleteTransactionIfNotEmpty() {
        Log.d(LOGCAT, "delete");
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM  " + TBL_TRANSACTION + " where FuelQuantity != ''";
        Log.d("query", deleteQuery);
        database.execSQL(deleteQuery);
    }

    public void deleteTransactionsByIDs(String Ids) {
        Log.d(LOGCAT, "delete");
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM  " + TBL_TRANSACTION + " where Id IN (" + Ids + ")";
        Log.d("query", deleteQuery);
        database.execSQL(deleteQuery);
    }

    public void deleteLastTransactionIfNotEmpty() {
        Log.d(LOGCAT, "delete");
        SQLiteDatabase database = this.getWritableDatabase();
        String delete1 = "SELECT  id  FROM " + TBL_TRANSACTION + " where FuelQuantity == '' AND pulses == null ORDER BY ID  DESC LIMIT 8";
        String deleteQuery = "DELETE FROM  " + TBL_TRANSACTION + " where FuelQuantity == '' AND id  NOT IN (" + delete1 + ")";
        Log.d("query", deleteQuery);
        database.execSQL(deleteQuery);
    }

    public void deleteLast4TransactionIfNotEmpty() {
        Log.d(LOGCAT, "delete");
        SQLiteDatabase database = this.getWritableDatabase();
        String delete1 = "SELECT  id  FROM " + TBL_TRANSACTION + " where FuelQuantity == '' ORDER BY ID  DESC LIMIT 8";
        String deleteQuery = "DELETE FROM  " + TBL_TRANSACTION + " where FuelQuantity == '' AND id  NOT IN (" + delete1 + ")";
        Log.d("query", deleteQuery);
        database.execSQL(deleteQuery);
    }

    public ArrayList<HashMap<String, String>> getLast4() {

        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String a = "4";
        String selectQuery = "SELECT  id  FROM " + TBL_TRANSACTION + " where FuelQuantity == '' ORDER BY ID DESC";


        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                System.out.println("OOO***" + cursor.getString(0));


            } while (cursor.moveToNext());
        }
        return wordList;
    }


    public ArrayList<HashMap<String, String>> getAllLinks() {

        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        try {

            String selectQuery = "SELECT * FROM " + TBL_LINK;
            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("Id", cursor.getString(0));
                    map.put("SiteId", cursor.getString(1));
                    map.put("WifiSSId", cursor.getString(2));
                    map.put("item", cursor.getString(2));
                    map.put("PumpOnTime", cursor.getString(3));
                    map.put("PumpOffTime", cursor.getString(4));
                    map.put("AuthorizedFuelingDays", cursor.getString(5));
                    map.put("Pulserratio", cursor.getString(6));
                    map.put("MacAddress", cursor.getString(7));
                    map.put("IsTLDCall", cursor.getString(8));
                    map.put("LinkCommunicationType", cursor.getString(9));
                    map.put("APMacAddress", cursor.getString(10));
                    map.put("BTMacAddress", cursor.getString(11));

                    System.out.println("***" + cursor.getString(2));

                    wordList.add(map);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "getAllLinks Exception: " + e.getMessage());
        }
        return wordList;
    }

    public HashMap<String, String> getLinksDetailsBySiteId(String SiteId) {

        getAllLinksDetails();

        HashMap<String, String> hmObj = new HashMap<String, String>();
        try {
            String selectQuery = "SELECT * FROM " + TBL_LINK + " WHERE SiteId=" + SiteId;
            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {

                hmObj.put("Id", cursor.getString(0));
                hmObj.put("SiteId", cursor.getString(1));
                hmObj.put("WifiSSId", cursor.getString(2));

                hmObj.put("PumpOnTime", cursor.getString(3));
                hmObj.put("PumpOffTime", cursor.getString(4));
                hmObj.put("AuthorizedFuelingDays", cursor.getString(5));
                hmObj.put("Pulserratio", cursor.getString(6));
                hmObj.put("MacAddress", cursor.getString(7));
                hmObj.put("IsTLDCall", cursor.getString(8));

            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "getLinksDetailsBySiteId Exception: " + e.getMessage());
        }
        return hmObj;
    }

    public HashMap<String, String> getAllLinksDetails() {

        HashMap<String, String> hmObj = new HashMap<String, String>();
        try {
            String selectQuery = "SELECT * FROM " + TBL_LINK;
            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {

                hmObj.put("Id", cursor.getString(0));
                hmObj.put("SiteId", cursor.getString(1));
                hmObj.put("WifiSSId", cursor.getString(2));

                hmObj.put("PumpOnTime", cursor.getString(3));
                hmObj.put("PumpOffTime", cursor.getString(4));
                hmObj.put("AuthorizedFuelingDays", cursor.getString(5));
                hmObj.put("Pulserratio", cursor.getString(6));
                hmObj.put("MacAddress", cursor.getString(7));
                hmObj.put("IsTLDCall", cursor.getString(8));

                System.out.println("wwwwww" + cursor.getString(1));

            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "getAllLinksDetails Exception: " + e.getMessage());
        }
        return hmObj;
    }

    public EntityOffTranz getTransactionDetailsBySqliteId(long SqliteId) {

        EntityOffTranz hmObj = new EntityOffTranz();

        try {
            String selectQuery = "SELECT * FROM " + TBL_TRANSACTION + " WHERE Id=" + SqliteId;

            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {

                hmObj.Id = cursor.getString(0);
                hmObj.HubId = cursor.getString(1);
                hmObj.SiteId = cursor.getString(2);
                hmObj.VehicleId = cursor.getString(3);
                hmObj.CurrentOdometer = cursor.getString(4);
                hmObj.CurrentHours = cursor.getString(5);
                hmObj.PersonId = cursor.getString(6);
                hmObj.PersonPin = cursor.getString(7);
                hmObj.FuelQuantity = cursor.getString(8);
                hmObj.Pulses = cursor.getString(9);
                hmObj.TransactionDateTime = cursor.getString(10);
                hmObj.OfflineFakeTransactionId = isNULL(cursor.getString(11));
                hmObj.VehicleNumber = isNULL(cursor.getString(12));
                hmObj.Other = isNULL(cursor.getString(13));

            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "getTransactionDetailsBySqliteId Exception: " + e.getMessage());
        }
        return hmObj;
    }

    public String getAllOfflineTransactionJSON(Context ctx) {

        String apiJSON = "";
        ArrayList<EntityOffTranz> allData = new ArrayList<>();

        try {
            String selectQuery = "SELECT * FROM " + TBL_TRANSACTION;

            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {

                do {
                    EntityOffTranz hmObj = new EntityOffTranz();
                    hmObj.Id = isNULL(cursor.getString(0));
                    hmObj.HubId = isNULL(cursor.getString(1));
                    hmObj.SiteId = isNULL(cursor.getString(2));
                    hmObj.VehicleId = isNULL(cursor.getString(3));
                    hmObj.CurrentOdometer = isNULL(cursor.getString(4));
                    hmObj.CurrentHours = isNULL(cursor.getString(5));
                    hmObj.PersonId = isNULL(cursor.getString(6));
                    hmObj.PersonPin = isNULL(cursor.getString(7));
                    hmObj.FuelQuantity = isNULL(cursor.getString(8));
                    hmObj.Pulses = isNULL(cursor.getString(9));
                    hmObj.TransactionDateTime = isNULL(cursor.getString(10));
                    hmObj.TransactionFrom = "AP";
                    hmObj.AppInfo = " Version:" + CommonUtils.getVersionCode(ctx) + " " + AppConstants.getDeviceName() + " Android " + Build.VERSION.RELEASE + " ";
                    hmObj.OfflineFakeTransactionId = isNULL(cursor.getString(11));
                    hmObj.VehicleNumber = isNULL(cursor.getString(12));
                    hmObj.Other = isNULL(cursor.getString(13));

                    // if(!hmObj.FuelQuantity.trim().isEmpty())
                    allData.add(hmObj);

                } while (cursor.moveToNext());
            }

            EnityTranzSync ets = new EnityTranzSync();
            ets.TransactionsModelsObj = allData;
            Gson gson = new Gson();
            apiJSON = gson.toJson(ets);

            System.out.println("OfflineJSON-" + apiJSON);

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "getAllOfflineTransactionJSON Exception: " + e.getMessage());
        }
        return apiJSON;
    }


    public String getTop10OfflineTransactionJSON(Context ctx) {

        String apiJSON = "";
        try {

            ArrayList<EntityOffTranz> allData = new ArrayList<>();

            String selectQuery = "SELECT * FROM " + TBL_TRANSACTION;

            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            int counter = 0;
            if (cursor.moveToFirst()) {

                do {
                    EntityOffTranz hmObj = new EntityOffTranz();
                    hmObj.Id = isNULL(cursor.getString(0));
                    hmObj.HubId = isNULL(cursor.getString(1));
                    hmObj.SiteId = isNULL(cursor.getString(2));
                    hmObj.VehicleId = isNULL(cursor.getString(3));
                    hmObj.CurrentOdometer = isNULL(cursor.getString(4));
                    hmObj.CurrentHours = isNULL(cursor.getString(5));
                    hmObj.PersonId = isNULL(cursor.getString(6));
                    hmObj.PersonPin = isNULL(cursor.getString(7));
                    hmObj.FuelQuantity = isNULL(cursor.getString(8));
                    hmObj.Pulses = isNULL(cursor.getString(9));
                    hmObj.TransactionDateTime = isNULL(cursor.getString(10));
                    hmObj.TransactionFrom = "AP";
                    hmObj.AppInfo = " Version:" + CommonUtils.getVersionCode(ctx) + " " + AppConstants.getDeviceName() + " Android " + Build.VERSION.RELEASE + " ";
                    hmObj.OnlineTransactionId = isNULL(cursor.getString(11));
                    hmObj.VehicleNumber = isNULL(cursor.getString(12));
                    hmObj.Other = isNULL(cursor.getString(13));

                    String pulses = isNULL(cursor.getString(9));

                    //To get only nonempty transactions
                    if (!pulses.trim().isEmpty() && Integer.parseInt(pulses) > 0) {
                        counter++;
                        allData.add(hmObj);
                    }

                    System.out.println("Counter of azure queue msg-" + counter);

                    if (counter >= 10) {
                        System.out.println("Size of azure queue msg-" + allData.size());
                        break;
                    }

                } while (cursor.moveToNext());
            }

            EnityTranzSync ets = new EnityTranzSync();
            ets.TransactionsModelsObj = allData;
            Gson gson = new Gson();
            apiJSON = gson.toJson(ets);

            System.out.println("OfflineJSON-" + apiJSON);

        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "getTop10OfflineTransactionJSON Exception: " + e.getMessage());
        }
        return apiJSON;
    }


    public String getTLDOfflineTransactionJSON(Context ctx) {

        String apiJSON = "";
        ArrayList<TankMonitorEntity> allData = new ArrayList<>();

        try {
            String selectQuery = "SELECT * FROM " + TBL_OFF_TLD;

            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            int counter = 0;
            if (cursor.moveToFirst()) {

                do {
                    TankMonitorEntity obj_entity = new TankMonitorEntity();
                    obj_entity.TLD = isNULL(cursor.getString(1));
                    obj_entity.Level = isNULL(cursor.getString(2));
                    obj_entity.FromSiteId = Integer.parseInt(isNULL(cursor.getString(3)));
                    obj_entity.CurrentTLDVersion = isNULL(cursor.getString(4));
                    obj_entity.IMEI_UDID = isNULL(cursor.getString(5));
                    obj_entity.LSB = isNULL(cursor.getString(6));
                    obj_entity.MSB = isNULL(cursor.getString(7));
                    obj_entity.TLDTemperature = isNULL(cursor.getString(8));
                    obj_entity.ReadingDateTime = isNULL(cursor.getString(9));
                    obj_entity.Response_code = isNULL(cursor.getString(10));
                    obj_entity.FromDirectTLD = isNULL(cursor.getString(11));

                    allData.add(obj_entity);

                } while (cursor.moveToNext());
            }

            Gson gson = new Gson();
            apiJSON = gson.toJson(allData);

            System.out.println("TLD-OfflineJSON-" + apiJSON);

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "getTLDOfflineTransactionJSON Exception: " + e.getMessage());
        }
        return apiJSON;
    }

    public String isNULL(String val) {
        if (val == null)
            return "";
        else
            return val;
    }

    public HashMap<String, String> getVehicleDetailsByBarcodeNumber(String BarcodeNumber) {

        HashMap<String, String> wordList = new HashMap<String, String>();

        try {
            String selectQuery = "SELECT * FROM " + TBL_VEHICLE + " WHERE lower(BarcodeNumber)='" + BarcodeNumber.toLowerCase().trim() + "'";
            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("Id", cursor.getString(0));
                    map.put("VehicleId", cursor.getString(1));
                    map.put("VehicleNumber", cursor.getString(2));
                    map.put("CurrentOdometer", cursor.getString(3));
                    map.put("CurrentHours", cursor.getString(4));
                    map.put("RequireOdometerEntry", cursor.getString(5));
                    map.put("RequireHours", cursor.getString(6));
                    map.put("FuelLimitPerTxn", cursor.getString(7));
                    map.put("FuelLimitPerDay", cursor.getString(8));
                    map.put("FOBNumber", cursor.getString(9));
                    map.put("AllowedLinks", cursor.getString(10));
                    map.put("Active", cursor.getString(11));
                    map.put("CheckOdometerReasonable", cursor.getString(12));
                    map.put("OdometerReasonabilityConditions", cursor.getString(13));
                    map.put("OdoLimit", cursor.getString(14));
                    map.put("HoursLimit", cursor.getString(15));
                    map.put("BarcodeNumber", cursor.getString(16));
                    map.put("IsExtraOther", cursor.getString(17));
                    map.put("ExtraOtherLabel", cursor.getString(18));
                    map.put("MagneticCardReaderNumber", cursor.getString(19));
                    map.put("CheckFuelLimitPerMonth", cursor.getString(20));
                    map.put("FuelLimitPerMonth", cursor.getString(21));
                    map.put("FuelQuantityOfVehiclePerMonth", cursor.getString(22));

                    System.out.println("***" + cursor.getString(1));

                    wordList = map;

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "getVehicleDetailsByBarcodeNumber Exception: " + e.getMessage());
        }
        return wordList;
    }

    public HashMap<String, String> getVehicleDetailsByVehicleNumber(String VehicleNumber) {
        HashMap<String, String> wordList = new HashMap<String, String>();
        try {

            String selectQuery = "SELECT * FROM " + TBL_VEHICLE + " WHERE VehicleNumber COLLATE NOCASE='" + VehicleNumber.trim() + "'";

            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("Id", cursor.getString(0));
                    map.put("VehicleId", cursor.getString(1));
                    map.put("VehicleNumber", cursor.getString(2));
                    map.put("CurrentOdometer", cursor.getString(3));
                    map.put("CurrentHours", cursor.getString(4));
                    map.put("RequireOdometerEntry", cursor.getString(5));
                    map.put("RequireHours", cursor.getString(6));
                    map.put("FuelLimitPerTxn", cursor.getString(7));
                    map.put("FuelLimitPerDay", cursor.getString(8));
                    map.put("FOBNumber", cursor.getString(9));
                    map.put("AllowedLinks", cursor.getString(10));
                    map.put("Active", cursor.getString(11));
                    map.put("CheckOdometerReasonable", cursor.getString(12));
                    map.put("OdometerReasonabilityConditions", cursor.getString(13));
                    map.put("OdoLimit", cursor.getString(14));
                    map.put("HoursLimit", cursor.getString(15));
                    map.put("BarcodeNumber", cursor.getString(16));
                    map.put("IsExtraOther", cursor.getString(17));
                    map.put("ExtraOtherLabel", cursor.getString(18));
                    map.put("MagneticCardReaderNumber", cursor.getString(19));
                    map.put("CheckFuelLimitPerMonth", cursor.getString(20));
                    map.put("FuelLimitPerMonth", cursor.getString(21));
                    map.put("FuelQuantityOfVehiclePerMonth", cursor.getString(22));

                    System.out.println("***" + cursor.getString(1));

                    wordList = map;

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "getVehicleDetailsByVehicleNumber Exception: " + e.getMessage());
        }
        return wordList;
    }

    public HashMap<String, String> getVehicleDetailsByFOBNumber(String FOBNumber) {

        HashMap<String, String> wordList = new HashMap<String, String>();

        try {
            String dummyFOB = FOBNumber;
            String asd = dummyFOB.substring(dummyFOB.length() - 4, dummyFOB.length());
            if (asd.equalsIgnoreCase("9000")) {
                dummyFOB = dummyFOB.substring(0, dummyFOB.length() - 4);
            }

            String selectQuery = "SELECT * FROM tbl_off_vehicle WHERE FOBNumber <> ''  AND LOWER( " +
                    " case when substr(FOBNumber, length(FOBNumber)-3, length(FOBNumber)) = '9000' then substr(FOBNumber, 0, length(FOBNumber)-3) " +
                    " else FOBNumber end " +
                    ")='" + dummyFOB.toLowerCase() + "'";

            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("Id", cursor.getString(0));
                    map.put("VehicleId", cursor.getString(1));
                    map.put("VehicleNumber", cursor.getString(2));
                    map.put("CurrentOdometer", cursor.getString(3));
                    map.put("CurrentHours", cursor.getString(4));
                    map.put("RequireOdometerEntry", cursor.getString(5));
                    map.put("RequireHours", cursor.getString(6));
                    map.put("FuelLimitPerTxn", cursor.getString(7));
                    map.put("FuelLimitPerDay", cursor.getString(8));
                    map.put("FOBNumber", cursor.getString(9));
                    map.put("AllowedLinks", cursor.getString(10));
                    map.put("Active", cursor.getString(11));
                    map.put("CheckOdometerReasonable", cursor.getString(12));
                    map.put("OdometerReasonabilityConditions", cursor.getString(13));
                    map.put("OdoLimit", cursor.getString(14));
                    map.put("HoursLimit", cursor.getString(15));
                    map.put("BarcodeNumber", cursor.getString(16));
                    map.put("IsExtraOther", cursor.getString(17));
                    map.put("ExtraOtherLabel", cursor.getString(18));
                    map.put("MagneticCardReaderNumber", cursor.getString(19));
                    map.put("CheckFuelLimitPerMonth", cursor.getString(20));
                    map.put("FuelLimitPerMonth", cursor.getString(21));
                    map.put("FuelQuantityOfVehiclePerMonth", cursor.getString(22));

                    System.out.println("***" + cursor.getString(1));

                    wordList = map;

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "getVehicleDetailsByFOBNumber Exception: " + e.getMessage());
        }
        return wordList;
    }

    public HashMap<String, String> getVehicleDetailsByMagNumber(String MagneticCardReaderNumber) {

        HashMap<String, String> wordList = new HashMap<String, String>();

        try {
            String dummyFOB = MagneticCardReaderNumber;
            String asd = dummyFOB.substring(dummyFOB.length() - 4, dummyFOB.length());
            if (asd.equalsIgnoreCase("9000")) {
                dummyFOB = dummyFOB.substring(0, dummyFOB.length() - 4);
            }

            String selectQuery = "SELECT * FROM tbl_off_vehicle WHERE MagneticCardReaderNumber <> ''  AND LOWER( " +
                    " case when substr(MagneticCardReaderNumber, length(MagneticCardReaderNumber)-3, length(MagneticCardReaderNumber)) = '9000' then substr(MagneticCardReaderNumber, 0, length(MagneticCardReaderNumber)-3) " +
                    " else MagneticCardReaderNumber end " +
                    ")='" + dummyFOB.toLowerCase() + "'";

            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("Id", cursor.getString(0));
                    map.put("VehicleId", cursor.getString(1));
                    map.put("VehicleNumber", cursor.getString(2));
                    map.put("CurrentOdometer", cursor.getString(3));
                    map.put("CurrentHours", cursor.getString(4));
                    map.put("RequireOdometerEntry", cursor.getString(5));
                    map.put("RequireHours", cursor.getString(6));
                    map.put("FuelLimitPerTxn", cursor.getString(7));
                    map.put("FuelLimitPerDay", cursor.getString(8));
                    map.put("FOBNumber", cursor.getString(9));
                    map.put("AllowedLinks", cursor.getString(10));
                    map.put("Active", cursor.getString(11));
                    map.put("CheckOdometerReasonable", cursor.getString(12));
                    map.put("OdometerReasonabilityConditions", cursor.getString(13));
                    map.put("OdoLimit", cursor.getString(14));
                    map.put("HoursLimit", cursor.getString(15));
                    map.put("BarcodeNumber", cursor.getString(16));
                    map.put("IsExtraOther", cursor.getString(17));
                    map.put("ExtraOtherLabel", cursor.getString(18));
                    map.put("MagneticCardReaderNumber", cursor.getString(19));
                    map.put("CheckFuelLimitPerMonth", cursor.getString(20));
                    map.put("FuelLimitPerMonth", cursor.getString(21));
                    map.put("FuelQuantityOfVehiclePerMonth", cursor.getString(22));

                    System.out.println("***" + cursor.getString(1));

                    wordList = map;

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "getVehicleDetailsByMagNumber Exception: " + e.getMessage());
        }
        return wordList;
    }

    public HashMap<String, String> getPersonnelDetailsByPIN(String PIN) {

        HashMap<String, String> wordList = new HashMap<String, String>();

        try {
            String selectQuery = "SELECT * FROM " + TBL_PERSONNEL + " WHERE PinNumber COLLATE NOCASE='" + PIN.trim() + "'";

            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("Id", cursor.getString(0));
                    map.put("PersonId", cursor.getString(1));
                    map.put("PinNumber", cursor.getString(2));
                    map.put("FuelLimitPerTxn", cursor.getString(3));
                    map.put("FuelLimitPerDay", cursor.getString(4));
                    map.put("FOBNumber", cursor.getString(5));
                    map.put("Authorizedlinks", cursor.getString(6));
                    map.put("AssignedVehicles", cursor.getString(7));
                    map.put("MagneticCardReaderNumber", cursor.getString(8));
                    map.put("Barcode", cursor.getString(9));

                    System.out.println("***" + cursor.getString(1));

                    wordList = map;

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "getPersonnelDetailsByPIN Exception: " + e.getMessage());
        }
        return wordList;
    }

    public HashMap<String, String> getPersonnelDetailsByFOBnumber(String FOB) {

        HashMap<String, String> wordList = new HashMap<String, String>();

        try {
            String dummyFOB = FOB;
            String asd = dummyFOB.substring(dummyFOB.length() - 4, dummyFOB.length());
            if (asd.equalsIgnoreCase("9000")) {
                dummyFOB = dummyFOB.substring(0, dummyFOB.length() - 4);
            }

            String selectQuery = "SELECT * FROM " + TBL_PERSONNEL + " WHERE FOBNumber <> ''  AND LOWER( " +
                    " case when substr(FOBNumber, length(FOBNumber)-3, length(FOBNumber)) = '9000' then substr(FOBNumber, 0, length(FOBNumber)-3) " +
                    " else FOBNumber end " +
                    ")='" + dummyFOB.toLowerCase() + "'";

            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("Id", cursor.getString(0));
                    map.put("PersonId", cursor.getString(1));
                    map.put("PinNumber", cursor.getString(2));
                    map.put("FuelLimitPerTxn", cursor.getString(3));
                    map.put("FuelLimitPerDay", cursor.getString(4));
                    map.put("FOBNumber", cursor.getString(5));
                    map.put("Authorizedlinks", cursor.getString(6));
                    map.put("AssignedVehicles", cursor.getString(7));
                    map.put("MagneticCardReaderNumber", cursor.getString(8));
                    map.put("Barcode", cursor.getString(9));

                    System.out.println("***" + cursor.getString(1));

                    wordList = map;

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "getPersonnelDetailsByFOBnumber Exception: " + e.getMessage());
        }
        return wordList;
    }

    public HashMap<String, String> getPersonnelDetailsByMagCardnumber(String FOB) {

        HashMap<String, String> wordList = new HashMap<String, String>();
        try {

            String dummyFOB = FOB;
            String asd = dummyFOB.substring(dummyFOB.length() - 4, dummyFOB.length());
            if (asd.equalsIgnoreCase("9000")) {
                dummyFOB = dummyFOB.substring(0, dummyFOB.length() - 4);
            }

            String selectQuery = "SELECT * FROM " + TBL_PERSONNEL + " WHERE MagneticCardReaderNumber <> ''  AND LOWER( " +
                    " case when substr(MagneticCardReaderNumber, length(MagneticCardReaderNumber)-3, length(MagneticCardReaderNumber)) = '9000' then substr(MagneticCardReaderNumber, 0, length(MagneticCardReaderNumber)-3) " +
                    " else FOBNumber end " +
                    ")='" + dummyFOB.toLowerCase() + "'";

            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("Id", cursor.getString(0));
                    map.put("PersonId", cursor.getString(1));
                    map.put("PinNumber", cursor.getString(2));
                    map.put("FuelLimitPerTxn", cursor.getString(3));
                    map.put("FuelLimitPerDay", cursor.getString(4));
                    map.put("FOBNumber", cursor.getString(5));
                    map.put("Authorizedlinks", cursor.getString(6));
                    map.put("AssignedVehicles", cursor.getString(7));
                    map.put("MagneticCardReaderNumber", cursor.getString(8));
                    map.put("Barcode", cursor.getString(9));

                    System.out.println("***" + cursor.getString(1));

                    wordList = map;

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "getPersonnelDetailsByMagCardnumber Exception: " + e.getMessage());
        }
        return wordList;
    }

    public HashMap<String, String> getPersonnelDetailsALLL() {

        HashMap<String, String> wordList = new HashMap<String, String>();

        try {
            String selectQuery = "SELECT * FROM " + TBL_PERSONNEL;
            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("Id", cursor.getString(0));
                    map.put("PersonId", cursor.getString(1));
                    map.put("PinNumber", cursor.getString(2));
                    map.put("FuelLimitPerTxn", cursor.getString(3));
                    map.put("FuelLimitPerDay", cursor.getString(4));
                    map.put("FOBNumber", cursor.getString(5));
                    map.put("Authorizedlinks", cursor.getString(6));
                    map.put("AssignedVehicles", cursor.getString(7));
                    map.put("MagneticCardReaderNumber", cursor.getString(8));
                    map.put("Barcode", cursor.getString(9));

                    System.out.println("***" + cursor.getString(1));

                    wordList = map;

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "getPersonnelDetailsALLL Exception: " + e.getMessage());
        }
        return wordList;
    }

    public ArrayList<HashMap<String, String>> getFuelTimingsBySiteId(String siteid) {

        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();

        try {
            String selectQuery = "SELECT * FROM " + TBL_FUEL_TIMING + " where SiteId=" + siteid;
            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("Id", cursor.getString(0));
                    map.put("SiteId", cursor.getString(1));
                    map.put("PersonId", cursor.getString(2));
                    map.put("FromTime", cursor.getString(3));
                    map.put("ToTime", cursor.getString(4));

                    System.out.println("***" + cursor.getString(2));

                    wordList.add(map);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "getFuelTimingsBySiteId Exception: " + e.getMessage());
        }
        return wordList;
    }

    public String selectRowCountOfDatabase() {

        String rowDetails = "";

        try {
            SQLiteDatabase database = this.getWritableDatabase();

            String selectQuery = "SELECT COUNT(*) FROM " + TBL_LINK;
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                String rowCount = cursor.getString(0);
                rowDetails += "LINKS:" + rowCount + "; ";
            }

            String selectQuery2 = "SELECT COUNT(*) FROM " + TBL_VEHICLE;
            Cursor cursor2 = database.rawQuery(selectQuery2, null);
            if (cursor2.moveToFirst()) {
                String rowCount = cursor2.getString(0);
                rowDetails += "Vehicle:" + rowCount + "; ";
            }

            String selectQuery3 = "SELECT COUNT(*) FROM " + TBL_PERSONNEL;
            Cursor cursor3 = database.rawQuery(selectQuery3, null);
            if (cursor3.moveToFirst()) {
                String rowCount = cursor3.getString(0);
                rowDetails += "Personnel:" + rowCount + "; ";
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "selectRowCountOfDatabase Exception: " + e.getMessage());
        }
        return rowDetails;
    }

    public int updateOdometerByVehicleId(String VehicleId, String CurrentOdometer) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("CurrentOdometer", CurrentOdometer);

        return database.update(TBL_VEHICLE, values, "VehicleId" + " = ?", new String[]{VehicleId});
    }

    public int updateHoursByVehicleId(String VehicleId, String CurrentHours) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("CurrentHours", CurrentHours);

        return database.update(TBL_VEHICLE, values, "VehicleId" + " = ?", new String[]{VehicleId});
    }


}