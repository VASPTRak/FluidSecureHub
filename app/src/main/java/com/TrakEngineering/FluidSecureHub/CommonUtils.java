package com.TrakEngineering.FluidSecureHub;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.SyncStateContract;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.Html;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.TrakEngineering.FluidSecureHub.BackgroundServiceNew.MyService_FSNP;
import com.TrakEngineering.FluidSecureHub.EddystoneScanner.EddystoneScannerService;
import com.TrakEngineering.FluidSecureHub.LFBle_vehicle.DeviceControlActivity_vehicle;
import com.TrakEngineering.FluidSecureHub.enity.AuthEntityClass;
import com.TrakEngineering.FluidSecureHub.enity.UserInfoEntity;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Stack;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static android.content.Context.WIFI_SERVICE;
import static com.TrakEngineering.FluidSecureHub.AppConstants.FluidSecureSiteName;
import static com.TrakEngineering.FluidSecureHub.AppConstants.ISVehicleHasFob;
import static com.TrakEngineering.FluidSecureHub.AppConstants.IsPersonHasFob;
import static com.TrakEngineering.FluidSecureHub.Constants.PREF_COLUMN_SITE;
import static com.TrakEngineering.FluidSecureHub.server.MyServer.ctx;
import static com.google.android.gms.internal.zzid.runOnUiThread;

/**
 * Created by VASP-LAP on 08-09-2015.
 */
public class CommonUtils {
    private static String TAG = "CommonUtils";
    private static File mypath; /*'---------------------------------------------------------------------------------------- Implemet logger functionality here....*/
    public static String FOLDER_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FSBin/";
    public static String PATH_BIN_FILE1 = "user1.2048.new.5.bin";

    public static String FOLDER_PATH_TLD_Firmware = Environment.getExternalStorageDirectory().getAbsolutePath() + "/www/TLD/";
    public static String FOLDER_PATH_FSVM_Firmware = Environment.getExternalStorageDirectory().getAbsolutePath() + "/www/FSVM/";
    public static String FOLDER_PATH_FSNP_Firmware = Environment.getExternalStorageDirectory().getAbsolutePath() + "/www/FSNP/";

    public static void LogMessage(String TAG, String TheMessage, Exception ex) {
        String logmessage = getTodaysDateInString();
        try {
            File logFileFolder = new File(Constants.LogPath);
            if (!logFileFolder.exists()) logFileFolder.mkdirs(); /*Delete file if it is more than 7 days old*/
            String OldFileToDelete = logFileFolder + "/Log_" + GetDateString(System.currentTimeMillis() - 604800000) + ".txt";
            File fd = new File(OldFileToDelete);
            if (fd.exists()) {
                fd.delete();
            }
            String LogFileName = logFileFolder + "/Log_" + GetDateString(System.currentTimeMillis()) + ".txt"; /*if(!new File(LogFileName).exists()) { new File(LogFileName).createNewFile(); }*/

            if (!new File(LogFileName).exists()) {
                File newFile = new File(LogFileName);
                newFile.createNewFile();
            }

            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(LogFileName, true)));
            logmessage = logmessage + " - " + TheMessage;
            if (ex != null) logmessage = logmessage + TAG + ":" + ex.getMessage();
            out.println(logmessage);
            out.close();
        } catch (Exception e1) {
            logmessage = logmessage + e1.getMessage();
            Log.d(TAG, logmessage);
        }
    }


    public static String GetPrintReciptForOther(String CompanyName,String PrintDate,String LinkName,String Location,String VehicleNumber,String PersonName,String OtherLabel,String OtherName,String Qty,String PrintCost){

        String content = "<h1>------FluidSecure Receipt------</h1>\n\n\n" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><u>Company :</u><br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p>"+CompanyName+"<br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><b>Time/Date :</b><br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p>" + PrintDate + "<br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><b>Location  :</b><br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p>"+LinkName+", "+Location+"<br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><b>Vehicle    :</b> "+VehicleNumber+"<br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><b>Personnel   :</b> "+PersonName+"<br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><b>Quantity    :</b> "+Qty+"<br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><b>Cost ($)    :</b> "+PrintCost+"<br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p>"+OtherLabel+": \n\n\n\n" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><b>"+OtherName+" </b><br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <h2>      ---------Thank You---------</h2>\n\n"+
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" ;//blank spase to cut paper


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return String.valueOf(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
        } else {
            return String.valueOf(Html.fromHtml(content));
        }

    }

    public static String GetPrintRecipt(String CompanyName,String PrintDate,String LinkName,String Location,String VehicleNumber,String PersonName,String Qty,String PrintCost){

        String content = "<h1>------FluidSecure Receipt------</h1>\n\n\n" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><b>Company :</b><br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p>"+CompanyName+"<br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><b>Time/Date :</b><br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p>" + PrintDate + "<br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><b>Location  :</b><br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p>"+LinkName+", "+Location+"<br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><b>Vehicle    :</b> "+VehicleNumber+"<br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><b>Personnel   :</b> "+PersonName+"\n\n\n\n" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><b>Quantity    :</b> "+Qty+"<br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><b>Cost ($)   :</b> "+PrintCost+"<br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <h2>      ---------Thank You---------</h2>\n\n"+
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" ;//blank spase to cut paper


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return String.valueOf(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
        } else {
            return String.valueOf(Html.fromHtml(content));
        }

    }

    public static String getTodaysDateInString() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String CurrantDate = df.format(c.getTime());
        return (CurrantDate);
    }

    public static String getTodaysDateTemp() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String CurrantDate = df.format(c.getTime());
        return (CurrantDate);
    }

    public static String getTodaysDateInStringPrint(String ServerDate001) {

        String outputDateStr = null;
        try {
            DateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
            DateFormat outputFormat = new SimpleDateFormat("hh:mm a MMM dd,yyyy");
            Date date = inputFormat.parse(ServerDate001);
            outputDateStr = outputFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return outputDateStr;
    }

    public static ArrayList<File> getAllFilesInDir(File dir) {
        if (dir == null)
            return null;

        ArrayList<File> files = new ArrayList<File>();

        Stack<File> dirlist = new Stack<File>();
        dirlist.clear();
        dirlist.push(dir);

        while (!dirlist.isEmpty()) {
            File dirCurrent = dirlist.pop();

            File[] fileList = dirCurrent.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.isDirectory())
                    dirlist.push(aFileList);
                else {

                    Calendar time = Calendar.getInstance();
                    time.add(Calendar.DAY_OF_YEAR, -30);
                    //I store the required attributes here and delete them
                    Date lastModified = new Date(aFileList.lastModified());
                    if (lastModified.before(time.getTime())) {
                        //file is older than a week
                        aFileList.delete();
                    } else {
                        files.add(aFileList);
                    }

                }
            }
        }

        return files;
    }

    public static String GetDateString(Long dateinms) {
        try {
            Time myDate = new Time();
            myDate.set(dateinms);
            return myDate.format("%Y-%m-%d");
        } catch (Exception e1) {
            return "";
        }
    } // Create logger functionality

    //----------------------------------------------------------------------------

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void showCustomMessageDilaog(final Activity context, String title, String message) {

        final Dialog dialogBus = new Dialog(context);
        dialogBus.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogBus.setCancelable(false);
        dialogBus.setContentView(R.layout.custom_alertdialouge);
        dialogBus.show();

        String newString1 = message.replaceAll("PERSONNEL", "<font color='red'> "+"<U> PERSONNEL </U>"+" </font>");
        String newString = newString1.replaceAll("VEHICLE", "<font color='red'> "+"<U> VEHICLE </U>"+" </font>");

        TextView edt_message = (TextView) dialogBus.findViewById(R.id.edt_message);
        Button btnAllow = (Button) dialogBus.findViewById(R.id.btnAllow);
        edt_message.setText(Html.fromHtml(newString));

        btnAllow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogBus.dismiss();

//                editVehicleNumber.requestFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);


            }
        });

    }

    public static void SimpleMessageDilaog(final Activity context, final String title, final String message) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                new AlertDialog.Builder(context)
                        .setTitle(title)
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Whatever...
                            }
                        }).show();
            }

        });

    }

    public static void showMessageDilaog(final Activity context, String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        // set title

        //alertDialogBuilder.setTitle(title);
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    public static void showMessageDilaogFinish(final Activity context, String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        // set title
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        context.finish();
                        dialog.cancel();
                    }
                });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    public static void showNoInternetDialog(final Activity context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        // set title
        alertDialogBuilder.setTitle("No Internet");
        alertDialogBuilder
                .setMessage(Html.fromHtml(context.getResources().getString(R.string.no_internet)))
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        context.finish();
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    public static void setMobileDataEnabled(Context context, boolean enabled) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final Class conmanClass = Class.forName(conman.getClass().getName());
        final Field connectivityManagerField = conmanClass.getDeclaredField("mService");
        connectivityManagerField.setAccessible(true);
        final Object connectivityManager = connectivityManagerField.get(conman);
        final Class connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());


        Class[] cArg = new Class[2];
        cArg[0] = String.class;
        cArg[1] = Boolean.TYPE;
        Method setMobileDataEnabledMethod;

        setMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", cArg);

        Object[] pArg = new Object[2];
        pArg[0] = context.getPackageName();
        pArg[1] = false;

        setMobileDataEnabledMethod.setAccessible(true);

        setMobileDataEnabledMethod.invoke(connectivityManager, pArg);
    }

    public static Boolean isMobileDataEnabled(Activity activity) {
        Object connectivityService = activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        ConnectivityManager cm = (ConnectivityManager) connectivityService;

        try {
            Class<?> c = Class.forName(cm.getClass().getName());
            Method m = c.getDeclaredMethod("getMobileDataEnabled");
            m.setAccessible(true);
            return (Boolean) m.invoke(cm);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isWiFiEnabled(Context ctx) {
        WifiManager wifiManager = (WifiManager) ctx.getSystemService(WIFI_SERVICE);

        if (wifiManager.isWifiEnabled()) {
            return true;
        }

        return false;
    }

    public static boolean isHotspotEnabled(Context ctx) {

        final WifiManager wifiManager = (WifiManager) ctx.getSystemService(WIFI_SERVICE);
        final int apState;
        try {
            apState = (Integer) wifiManager.getClass().getMethod("getWifiApState").invoke(wifiManager);
            if (apState == 13) {
                return true;  // hotspot Enabled
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void SaveLogFlagInPref(Activity activity,String data,String CompanyBrandName, String CompanyBrandLogoLink,String SupportEmail, String SupportPhonenumber){

        SharedPreferences pref = activity.getSharedPreferences(Constants.PREF_Log_Data, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(AppConstants.LogRequiredFlag, data);
        editor.putString(AppConstants.CompanyBrandName, CompanyBrandName);
        editor.putString(AppConstants.CompanyBrandLogoLink, CompanyBrandLogoLink);
        editor.putString(AppConstants.SupportEmail, SupportEmail);
        editor.putString(AppConstants.SupportPhonenumber, SupportPhonenumber);
        editor.commit();


    }

    public static void FA_FlagSavePref(Activity activity,boolean data, boolean barcodedata){

        SharedPreferences pref = activity.getSharedPreferences(Constants.PREF_FA_Data, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(AppConstants.FAData,data);
        editor.putBoolean(AppConstants.UseBarcode,barcodedata);
        editor.commit();


    }

    public static void SaveDataInPrefForGatehub (Activity activity, String IsGateHub, String IsStayOpenGate) {

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.PREF_COLUMN_GATE_HUB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(AppConstants.IsGateHub, IsGateHub);
        editor.putString(AppConstants.IsStayOpenGate, IsStayOpenGate);
        editor.commit();
    }

    public static void SaveDataInPref(Activity activity, String data, String valueType) {

        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_COLUMN_SITE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(valueType, data);
        editor.commit();
    }

    public static void SaveUserInPref(Activity activity, String userName, String userMobile, String userEmail, String IsOdoMeterRequire,
                                      String IsDepartmentRequire, String IsPersonnelPINRequire, String IsOtherRequire, String IsHoursRequire, String OtherLabel, String TimeOut, String HubId, String IsPersonnelPINRequireForHub, String fluidSecureSiteName, String IsVehicleHasFob, String isPersonHasFob,String IsVehicleNumberRequire,int WifiChannelToUse) {

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(AppConstants.USER_NAME, userName);
        editor.putString(AppConstants.USER_MOBILE, userMobile);
        editor.putString(AppConstants.USER_EMAIL, userEmail);
        editor.putString(AppConstants.IsOdoMeterRequire, IsOdoMeterRequire);
        editor.putString(AppConstants.IsDepartmentRequire, IsDepartmentRequire);
        editor.putString(AppConstants.IsPersonnelPINRequire, IsPersonnelPINRequire);
        //editor.putString(AppConstants.IsPersonnelPINRequireForHub, IsPersonnelPINRequireForHub);
        editor.putString(AppConstants.IsOtherRequire, IsOtherRequire);
        editor.putString(AppConstants.IsHoursRequire, IsHoursRequire);
        editor.putString(AppConstants.OtherLabel, OtherLabel);
        editor.putString(AppConstants.TimeOut, TimeOut);
        editor.putString(AppConstants.HubId, HubId);
        editor.putString(AppConstants.IsPersonnelPINRequireForHub, IsPersonnelPINRequireForHub);
        editor.putString(ISVehicleHasFob,  IsVehicleHasFob);
        editor.putString(IsPersonHasFob,  isPersonHasFob);
        editor.putString(FluidSecureSiteName,  fluidSecureSiteName);
        editor.putString(AppConstants.IsVehicleNumberRequire,  IsVehicleNumberRequire);
        editor.putInt(AppConstants.WifiChannelToUse,  WifiChannelToUse);

        editor.commit();
    }
    public static void SaveTldDetailsInPref(Context activity,String IsTLDCall,String IsTLDFirmwareUpgrade,String TLDFirmwareFilePath,String TLDFIrmwareVersion,String PROBEMacAddress,String selMacAddress) {

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.PREF_TldDetails, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("IsTLDCall", IsTLDCall);
        editor.putString("IsTLDFirmwareUpgrade", IsTLDFirmwareUpgrade);
        editor.putString("TLDFirmwareFilePath", TLDFirmwareFilePath);
        editor.putString("TLDFIrmwareVersion", TLDFIrmwareVersion);
        editor.putString("PROBEMacAddress", PROBEMacAddress);
        editor.putString("selMacAddress", selMacAddress);
        editor.commit();
    }


    public static void SaveVehiFuelInPref_FS1(Context activity, String TransactionId_FS1,String VehicleId_FS1, String PhoneNumber_FS1, String PersonId_FS1, String PulseRatio_FS1, String MinLimit_FS1, String FuelTypeId_FS1, String ServerDate_FS1, String IntervalToStopFuel_FS1,String PrintDate_FS1,String Company_FS1,String Location_FS1,String PersonName_FS1,String PrinterMacAddress_FS1,String PrinterName_FS1,String vehicleNumber_FS1,String accOther_FS1,String VehicleSum_FS1,String DeptSum_FS1,String VehPercentage_FS1,String DeptPercentage_FS1,String SurchargeType_FS1,String ProductPrice_FS1,String IsTLDCall_FS1,String EnablePrinter_FS1) {

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("TransactionId_FS1", TransactionId_FS1);
        editor.putString("VehicleId_FS1", VehicleId_FS1);
        editor.putString("VehicleId_FS1", VehicleId_FS1);
        editor.putString("PhoneNumber_FS1", PhoneNumber_FS1);
        editor.putString("PersonId_FS1", PersonId_FS1);
        editor.putString("PulseRatio_FS1", PulseRatio_FS1);
        editor.putString("MinLimit_FS1", MinLimit_FS1);
        editor.putString("FuelTypeId_FS1", FuelTypeId_FS1);
        editor.putString("ServerDate_FS1", ServerDate_FS1);
        editor.putString("IntervalToStopFuel_FS1", IntervalToStopFuel_FS1);
        editor.putString("PrintDate_FS1", PrintDate_FS1);
        editor.putString("Company_FS1", Company_FS1);
        editor.putString("Location_FS1", Location_FS1);
        editor.putString("PersonName_FS1", PersonName_FS1);
        editor.putString("PrinterMacAddress_FS1", PrinterMacAddress_FS1);
        editor.putString("PrinterName_FS1", PrinterName_FS1);
        editor.putString("vehicleNumber_FS1", vehicleNumber_FS1);
        editor.putString("accOther_FS1", accOther_FS1);
        editor.putString("VehicleSum_FS1", VehicleSum_FS1);
        editor.putString("DeptSum_FS1", DeptSum_FS1);
        editor.putString("VehPercentage_FS1", VehPercentage_FS1);
        editor.putString("DeptPercentage_FS1", DeptPercentage_FS1);
        editor.putString("SurchargeType_FS1", SurchargeType_FS1);
        editor.putString("ProductPrice_FS1", ProductPrice_FS1);
        editor.putString("IsTLDCall_FS1", IsTLDCall_FS1);
        editor.putString("EnablePrinter_FS1", EnablePrinter_FS1);


        editor.commit();
    }

    public static void SaveVehiFuelInPref(Context activity, String TransactionId,String VehicleId, String PhoneNumber, String PersonId, String PulseRatio, String MinLimit, String FuelTypeId, String ServerDate, String IntervalToStopFuel,String PrintDate,String Company,String Location,String PersonName,String PrinterMacAddress,String PrinterName,String vehicleNumber,String accOther,String VehicleSum,String DeptSum,String VehPercentage,String DeptPercentage,String SurchargeType,String ProductPrice,String IsTLDCall1,String EnablePrinter) {

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("TransactionId", TransactionId);
        editor.putString("VehicleId", VehicleId);
        editor.putString("PhoneNumber", PhoneNumber);
        editor.putString("PersonId", PersonId);
        editor.putString("PulseRatio", PulseRatio);
        editor.putString("MinLimit", MinLimit);
        editor.putString("FuelTypeId", FuelTypeId);
        editor.putString("ServerDate", ServerDate);
        editor.putString("IntervalToStopFuel", IntervalToStopFuel);
        editor.putString("PrintDate", PrintDate);
        editor.putString("Company", Company);
        editor.putString("Location", Location);
        editor.putString("PersonName", PersonName);
        editor.putString("PrinterMacAddress", PrinterMacAddress);
        editor.putString("PrinterName", PrinterName);
        editor.putString("vehicleNumber", vehicleNumber);
        editor.putString("accOther", accOther);
        editor.putString("VehicleSum", VehicleSum);
        editor.putString("DeptSum", DeptSum);
        editor.putString("VehPercentage", VehPercentage);
        editor.putString("DeptPercentage", DeptPercentage);
        editor.putString("SurchargeType", SurchargeType);
        editor.putString("ProductPrice", ProductPrice);
        editor.putString("IsTLDCall", IsTLDCall1);
        editor.putString("EnablePrinter", EnablePrinter);


        editor.commit();
    }

    public static void SaveVehiFuelInPref_FS3(Context activity, String TransactionId_FS3,String VehicleId_FS3, String PhoneNumber_FS3, String PersonId_FS3, String PulseRatio_FS3, String MinLimit_FS3, String FuelTypeId_FS3, String ServerDate_FS3, String IntervalToStopFuel_FS3,String PrintDate_FS3,String Company_FS3,String Location_FS3,String PersonName_FS3,String PrinterMacAddress_FS3,String PrinterName_FS3,String vehicleNumber_FS3,String accOther_FS3,String VehicleSum_FS3,String DeptSum_FS3,String VehPercentage_FS3,String DeptPercentage_FS3,String SurchargeType_FS3,String ProductPrice_FS3,String IsTLDCall_FS3,String EnablePrinter_FS3) {

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("TransactionId_FS3", TransactionId_FS3);
        editor.putString("VehicleId_FS3", VehicleId_FS3);
        editor.putString("VehicleId_FS3", VehicleId_FS3);
        editor.putString("PhoneNumber_FS3", PhoneNumber_FS3);
        editor.putString("PersonId_FS3", PersonId_FS3);
        editor.putString("PulseRatio_FS3", PulseRatio_FS3);
        editor.putString("MinLimit_FS3", MinLimit_FS3);
        editor.putString("FuelTypeId_FS3", FuelTypeId_FS3);
        editor.putString("ServerDate_FS3", ServerDate_FS3);
        editor.putString("IntervalToStopFuel_FS3", IntervalToStopFuel_FS3);
        editor.putString("PrintDate_FS3", PrintDate_FS3);
        editor.putString("Company_FS3", Company_FS3);
        editor.putString("Location_FS3", Location_FS3);
        editor.putString("PersonName_FS3", PersonName_FS3);
        editor.putString("PrinterMacAddress_FS3", PrinterMacAddress_FS3);
        editor.putString("PrinterName_FS3", PrinterName_FS3);
        editor.putString("vehicleNumber_FS3", vehicleNumber_FS3);
        editor.putString("accOther_FS3", accOther_FS3);
        editor.putString("VehicleSum_FS3", VehicleSum_FS3);
        editor.putString("DeptSum_FS3", DeptSum_FS3);
        editor.putString("VehPercentage_FS3", VehPercentage_FS3);
        editor.putString("DeptPercentage_FS3", DeptPercentage_FS3);
        editor.putString("SurchargeType_FS3", SurchargeType_FS3);
        editor.putString("ProductPrice_FS3", ProductPrice_FS3);
        editor.putString("IsTLDCall_FS3", IsTLDCall_FS3);
        editor.putString("EnablePrinter_FS3", EnablePrinter_FS3);

        editor.commit();
    }

    public static void SaveVehiFuelInPref_FS4(Context activity, String TransactionId_FS4,String VehicleId_FS4, String PhoneNumber_FS4, String PersonId_FS4, String PulseRatio_FS4, String MinLimit_FS4, String FuelTypeId_FS4, String ServerDate_FS4, String IntervalToStopFuel_FS4,String PrintDate_FS4,String Company_FS4,String Location_FS4,String PersonName_FS4,String PrinterMacAddress_FS4,String PrinterName_FS4,String vehicleNumber_FS4,String accOther_FS4,String VehicleSum_FS4,String DeptSum_FS4,String VehPercentage_FS4,String DeptPercentage_FS4,String SurchargeType_FS4,String ProductPrice_FS4,String IsTLDCall_FS4,String EnablePrinter_FS4) {

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("TransactionId_FS4", TransactionId_FS4);
        editor.putString("VehicleId_FS4", VehicleId_FS4);
        editor.putString("VehicleId_FS4", VehicleId_FS4);
        editor.putString("PhoneNumber_FS4", PhoneNumber_FS4);
        editor.putString("PersonId_FS4", PersonId_FS4);
        editor.putString("PulseRatio_FS4", PulseRatio_FS4);
        editor.putString("MinLimit_FS4", MinLimit_FS4);
        editor.putString("FuelTypeId_FS4", FuelTypeId_FS4);
        editor.putString("ServerDate_FS4", ServerDate_FS4);
        editor.putString("IntervalToStopFuel_FS4", IntervalToStopFuel_FS4);
        editor.putString("PrintDate_FS4", PrintDate_FS4);
        editor.putString("Company_FS4", Company_FS4);
        editor.putString("Location_FS4", Location_FS4);
        editor.putString("PersonName_FS4", PersonName_FS4);
        editor.putString("PrinterMacAddress_FS4", PrinterMacAddress_FS4);
        editor.putString("PrinterName_FS4", PrinterName_FS4);
        editor.putString("vehicleNumber_FS4", vehicleNumber_FS4);
        editor.putString("accOther_FS4", accOther_FS4);
        editor.putString("VehicleSum_FS4", VehicleSum_FS4);
        editor.putString("DeptSum_FS4", DeptSum_FS4);
        editor.putString("VehPercentage_FS4", VehPercentage_FS4);
        editor.putString("DeptPercentage_FS4", DeptPercentage_FS4);
        editor.putString("SurchargeType_FS4", SurchargeType_FS4);
        editor.putString("ProductPrice_FS4", ProductPrice_FS4);
        editor.putString("IsTLDCall_FS4", IsTLDCall_FS4);
        editor.putString("EnablePrinter_FS4", EnablePrinter_FS4);

        editor.commit();
    }

    public static AuthEntityClass getWiFiDetails(Activity activity, String wifiSSID) {


        AuthEntityClass authEntityClass = new AuthEntityClass();

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        String dataSite = sharedPref.getString(PREF_COLUMN_SITE, "");


        try {
            if (dataSite != null) {
                JSONArray jsonArray = new JSONArray(dataSite);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    Gson gson = new Gson();
                    authEntityClass = gson.fromJson(jsonObject.toString(), AuthEntityClass.class);

                }
            }
        } catch (Exception ex) {

            CommonUtils.LogMessage(TAG, "", ex);
        }

        return authEntityClass;

    }

    public static String getVersionCode(Context ctx) {

        String versioncode = "";
        try {
            PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            versioncode = pInfo.versionName;

        } catch (Exception q) {
            System.out.println(q);
        }

        return versioncode;
    }

    public static UserInfoEntity getCustomerDetails(Activity activity) {

        UserInfoEntity userInfoEntity = new UserInfoEntity();

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        userInfoEntity.PersonName = sharedPref.getString(AppConstants.USER_NAME, "");
        userInfoEntity.PhoneNumber = sharedPref.getString(AppConstants.USER_MOBILE, "");
        userInfoEntity.PersonEmail = sharedPref.getString(AppConstants.USER_EMAIL, "");
        userInfoEntity.FluidSecureSiteName = sharedPref.getString(AppConstants.FluidSecureSiteName, "");


        return userInfoEntity;
    }

    public static UserInfoEntity getCustomerDetailsCC(Context ctx) {

        UserInfoEntity userInfoEntity = new UserInfoEntity();

        SharedPreferences sharedPref = ctx.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        userInfoEntity.PersonName = sharedPref.getString(AppConstants.USER_NAME, "");
        userInfoEntity.PhoneNumber = sharedPref.getString(AppConstants.USER_MOBILE, "");
        userInfoEntity.PersonEmail = sharedPref.getString(AppConstants.USER_EMAIL, "");
        userInfoEntity.FluidSecureSiteName = sharedPref.getString(AppConstants.FluidSecureSiteName, "");


        return userInfoEntity;
    }

    public static UserInfoEntity getCustomerDetails_backgroundServiceEddystoneScannerService(EddystoneScannerService activity) {

        UserInfoEntity userInfoEntity = new UserInfoEntity();

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        userInfoEntity.PersonName = sharedPref.getString(AppConstants.USER_NAME, "");
        userInfoEntity.PhoneNumber = sharedPref.getString(AppConstants.USER_MOBILE, "");
        userInfoEntity.PersonEmail = sharedPref.getString(AppConstants.USER_EMAIL, "");
        userInfoEntity.FluidSecureSiteName = sharedPref.getString(AppConstants.FluidSecureSiteName, "");


        return userInfoEntity;
    }

    public static UserInfoEntity getCustomerDetails_backgroundService(MyService_FSNP activity) {

        UserInfoEntity userInfoEntity = new UserInfoEntity();

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        userInfoEntity.PersonName = sharedPref.getString(AppConstants.USER_NAME, "");
        userInfoEntity.PhoneNumber = sharedPref.getString(AppConstants.USER_MOBILE, "");
        userInfoEntity.PersonEmail = sharedPref.getString(AppConstants.USER_EMAIL, "");
        userInfoEntity.FluidSecureSiteName = sharedPref.getString(AppConstants.FluidSecureSiteName, "");


        return userInfoEntity;
    }

    public static UserInfoEntity getCustomerDetails_backgroundService(BackgroundServiceFSNP activity) {

        UserInfoEntity userInfoEntity = new UserInfoEntity();

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        userInfoEntity.PersonName = sharedPref.getString(AppConstants.USER_NAME, "");
        userInfoEntity.PhoneNumber = sharedPref.getString(AppConstants.USER_MOBILE, "");
        userInfoEntity.PersonEmail = sharedPref.getString(AppConstants.USER_EMAIL, "");
        userInfoEntity.FluidSecureSiteName = sharedPref.getString(AppConstants.FluidSecureSiteName, "");


        return userInfoEntity;
    }

    public static UserInfoEntity getCustomerDetails_backgroundService(BackgroundService_AP activity) {

        UserInfoEntity userInfoEntity = new UserInfoEntity();

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        userInfoEntity.PersonName = sharedPref.getString(AppConstants.USER_NAME, "");
        userInfoEntity.PhoneNumber = sharedPref.getString(AppConstants.USER_MOBILE, "");
        userInfoEntity.PersonEmail = sharedPref.getString(AppConstants.USER_EMAIL, "");
        userInfoEntity.FluidSecureSiteName = sharedPref.getString(AppConstants.FluidSecureSiteName, "");


        return userInfoEntity;
    }

    public static UserInfoEntity getCustomerDetails_KdtAlive(BackgroundServiceKeepDataTransferAlive activity) {

        UserInfoEntity userInfoEntity = new UserInfoEntity();

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        userInfoEntity.PersonName = sharedPref.getString(AppConstants.USER_NAME, "");
        userInfoEntity.PhoneNumber = sharedPref.getString(AppConstants.USER_MOBILE, "");
        userInfoEntity.PersonEmail = sharedPref.getString(AppConstants.USER_EMAIL, "");
        userInfoEntity.FluidSecureSiteName = sharedPref.getString(AppConstants.FluidSecureSiteName, "");


        return userInfoEntity;
    }

    public static UserInfoEntity getCustomerDetails_backgroundService_PIPE(BackgroundService_AP_PIPE activity) {

        UserInfoEntity userInfoEntity = new UserInfoEntity();

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        userInfoEntity.PersonName = sharedPref.getString(AppConstants.USER_NAME, "");
        userInfoEntity.PhoneNumber = sharedPref.getString(AppConstants.USER_MOBILE, "");
        userInfoEntity.PersonEmail = sharedPref.getString(AppConstants.USER_EMAIL, "");
        userInfoEntity.FluidSecureSiteName = sharedPref.getString(AppConstants.FluidSecureSiteName, "");


        return userInfoEntity;
    }

    public static UserInfoEntity getCustomerDetails_backgroundService_FS3(BackgroundService_FS_UNIT_3 activity) {

        UserInfoEntity userInfoEntity = new UserInfoEntity();

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        userInfoEntity.PersonName = sharedPref.getString(AppConstants.USER_NAME, "");
        userInfoEntity.PhoneNumber = sharedPref.getString(AppConstants.USER_MOBILE, "");
        userInfoEntity.PersonEmail = sharedPref.getString(AppConstants.USER_EMAIL, "");
        userInfoEntity.FluidSecureSiteName = sharedPref.getString(AppConstants.FluidSecureSiteName, "");


        return userInfoEntity;
    }

    public static UserInfoEntity getCustomerDetails_backgroundService_FS4(BackgroundService_FS_UNIT_4 activity) {

        UserInfoEntity userInfoEntity = new UserInfoEntity();

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        userInfoEntity.PersonName = sharedPref.getString(AppConstants.USER_NAME, "");
        userInfoEntity.PhoneNumber = sharedPref.getString(AppConstants.USER_MOBILE, "");
        userInfoEntity.PersonEmail = sharedPref.getString(AppConstants.USER_EMAIL, "");
        userInfoEntity.FluidSecureSiteName = sharedPref.getString(AppConstants.FluidSecureSiteName, "");


        return userInfoEntity;
    }

    public static String FormatPrintRecipte(String toPad){


        //String padded = String.format(toPad,"%10s").replace(' ', '0');

        int width = 15;
        char fill = '0';

        String padded = toPad+new String(new char[width - toPad.length()]).replace('\0', fill) +"::";
        System.out.println(padded);



        return padded;
    }

    // precondition:  d is a nonnegative integer
    public static String decimal2hex(int d) {
        String digits = "0123456789ABCDEF";
        if (d <= 0) return "0";
        int base = 16;   // flexible to change in any base under 16
        String hex = "";
        while (d > 0) {
            int digit = d % base;              // rightmost digit
            hex = digits.charAt(digit) + hex;  // string concatenation
            d = d / base;
        }
        return hex;
    }

    public static int hex2decimal(String s) {
        String digits = "0123456789ABCDEF";
        s = s.toUpperCase();
        int val = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = digits.indexOf(c);
            val = 16*val + d;
        }
        return val;
    }



    /**
     * Creates a hexadecimal <code>String</code> representation of the
     * <code>byte[]</code> passed. Each element is converted to a
     * <code>String</code> via the {@link Integer#toHexString(int)} and
     * separated by <code>" "</code>. If the array is <code>null</code>, then
     * <code>""<code> is returned.
     *
     * @param array
     *            the <code>byte</code> array to convert.
     * @return the <code>String</code> representation of <code>array</code> in
     *         hexadecimal.
     */
    public static String toHexString(byte[] array) {

        String bufferString = "";

        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                String hexChar = Integer.toHexString(array[i] & 0xFF);
                if (hexChar.length() == 1) {
                    hexChar = "0" + hexChar;
                }
                bufferString += hexChar.toUpperCase(Locale.US) + " ";
            }
        }
        return bufferString;
    }

    private static boolean isHexNumber(byte value) {
        if (!(value >= '0' && value <= '9') && !(value >= 'A' && value <= 'F')
                && !(value >= 'a' && value <= 'f')) {
            return false;
        }
        return true;
    }

    /**
     * Checks a hexadecimal <code>String</code> that is contained hexadecimal
     * value or not.
     *
     * @param string
     *            the string to check.
     * @return <code>true</code> the <code>string</code> contains Hex number
     *         only, <code>false</code> otherwise.
     * @throws NullPointerException
     *             if <code>string == null</code>.
     */
    public static boolean isHexNumber(String string) {
        if (string == null)
            throw new NullPointerException("string was null");

        boolean flag = true;

        for (int i = 0; i < string.length(); i++) {
            char cc = string.charAt(i);
            if (!isHexNumber((byte) cc)) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    private static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }))
                .byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }))
                .byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }

    /**
     * Creates a <code>byte[]</code> representation of the hexadecimal
     * <code>String</code> passed.
     *
     * @param string
     *            the hexadecimal string to be converted.
     * @return the <code>array</code> representation of <code>String</code>.
     * @throws IllegalArgumentException
     *             if <code>string</code> length is not in even number.
     * @throws NullPointerException
     *             if <code>string == null</code>.
     * @throws NumberFormatException
     *             if <code>string</code> cannot be parsed as a byte value.
     */
    public static byte[] hexString2Bytes(String string) {
        if (string == null)
            throw new NullPointerException("string was null");

        int len = string.length();

        if (len == 0)
            return new byte[0];
        if (len % 2 == 1)
            throw new IllegalArgumentException(
                    "string length should be an even number");

        byte[] ret = new byte[len / 2];
        byte[] tmp = string.getBytes();

        for (int i = 0; i < len; i += 2) {
            if (!isHexNumber(tmp[i]) || !isHexNumber(tmp[i + 1])) {
                throw new NumberFormatException(
                        "string contained invalid value");
            }
            ret[i / 2] = uniteBytes(tmp[i], tmp[i + 1]);
        }
        return ret;
    }

    /**
     * Creates a <code>byte[]</code> representation of the hexadecimal
     * <code>String</code> in the EditText control.
     *
     * @param editText
     *            the EditText control which contains hexadecimal string to be
     *            converted.
     * @return the <code>array</code> representation of <code>String</code> in
     *         the EditText control. <code>null</code> if the string format is
     *         not correct.
     */
    public static byte[] getEditTextinHexBytes(EditText editText) {
        Editable edit = editText.getText();

        if (edit == null) {
            return null;
        }

        String rawdata = edit.toString();

        if (rawdata == null || rawdata.isEmpty()) {
            return null;
        }

        String command = rawdata.replace(" ", "").replace("\n", "");

        if (command.isEmpty() || command.length() % 2 != 0
                || isHexNumber(command) == false) {
            return null;
        }

        return hexString2Bytes(command);
    }


    /**
     * Converts the HEX string to byte array.
     *
     * @param hexString the HEX string.
     * @return the byte array.
     */
    public static byte[] toByteArray(String hexString) {

        byte[] byteArray = null;
        int count = 0;
        char c = 0;
        int i = 0;

        boolean first = true;
        int length = 0;
        int value = 0;

        // Count number of hex characters
        for (i = 0; i < hexString.length(); i++) {

            c = hexString.charAt(i);
            if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a'
                    && c <= 'f') {
                count++;
            }
        }

        byteArray = new byte[(count + 1) / 2];
        for (i = 0; i < hexString.length(); i++) {

            c = hexString.charAt(i);
            if (c >= '0' && c <= '9') {
                value = c - '0';
            } else if (c >= 'A' && c <= 'F') {
                value = c - 'A' + 10;
            } else if (c >= 'a' && c <= 'f') {
                value = c - 'a' + 10;
            } else {
                value = -1;
            }

            if (value >= 0) {

                if (first) {

                    byteArray[length] = (byte) (value << 4);

                } else {

                    byteArray[length] |= value;
                    length++;
                }

                first = !first;
            }
        }

        return byteArray;
    }

    public static void AddRemovecurrentTransactionList(boolean AddDel, String TxnId) {


        if (AddDel) {
            if (!AppConstants.ListOfRunningTransactiins.contains(TxnId)) {
                AppConstants.ListOfRunningTransactiins.add(TxnId);
            }
        } else {
            if (AppConstants.ListOfRunningTransactiins != null && AppConstants.ListOfRunningTransactiins.contains(TxnId)) {
                AppConstants.ListOfRunningTransactiins.remove(TxnId);

            }
        }
    }


    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager)context. getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service already","running");
                return true;
            }
        }
        Log.i("Service not","running");
        return false;
    }

    public static boolean checkServiceRunning(Context con_text,String package_name){
        ActivityManager manager = (ActivityManager) con_text.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (package_name.equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }


}