package com.TrakEngineering.FluidSecureHub;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import androidx.annotation.RequiresApi;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.TrakEngineering.FluidSecureHub.BackgroundServiceNew.MyService_FSNP;
import com.TrakEngineering.FluidSecureHub.EddystoneScanner.EddystoneScannerService;
import com.TrakEngineering.FluidSecureHub.entity.AuthEntityClass;
import com.TrakEngineering.FluidSecureHub.entity.StatusForUpgradeVersionEntity;
import com.TrakEngineering.FluidSecureHub.entity.UserInfoEntity;
import com.TrakEngineering.FluidSecureHub.offline.OffDBController;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.WIFI_SERVICE;
import static com.TrakEngineering.FluidSecureHub.AppConstants.FluidSecureSiteName;
import static com.TrakEngineering.FluidSecureHub.AppConstants.ISVehicleHasFob;
import static com.TrakEngineering.FluidSecureHub.AppConstants.IsPersonHasFob;
import static com.TrakEngineering.FluidSecureHub.Constants.PREF_COLUMN_SITE;
import static com.TrakEngineering.FluidSecureHub.Constants.PREF_OFF_DB_SIZE;
import static com.TrakEngineering.FluidSecureHub.WelcomeActivity.wifiApManager;
import static com.TrakEngineering.FluidSecureHub.server.ServerHandler.TEXT;

import com.example.fs_ipneigh30.FS_ArpNDK;

/**
 * Created by VASP-LAP on 08-09-2015.
 */
public class CommonUtils {

    private static String TAG = "CommonUtils ";
    private static File mypath; /*'---------------------------------------------------------------------------------------- Implemet logger functionality here....*/
    //public static String FOLDER_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FSBin/";
    public static String FOLDER_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/FSBin/";
    public static String PATH_BIN_FILE1 = "user1.2048.new.5.bin";

    //public static String FOLDER_PATH_TLD_Firmware = Environment.getExternalStorageDirectory().getAbsolutePath() + "/www/TLD/";
    public static String FOLDER_PATH_TLD_Firmware = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/www/TLD/";
    //public static String FOLDER_PATH_FSVM_Firmware = Environment.getExternalStorageDirectory().getAbsolutePath() + "/www/FSVM/";
    public static String FOLDER_PATH_FSVM_Firmware = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/www/FSVM/";
    //public static String FOLDER_PATH_FSNP_Firmware = Environment.getExternalStorageDirectory().getAbsolutePath() + "/www/FSNP/";
    public static ArrayList<HashMap<String, String>> TankDataList = new ArrayList<>();
    public static ArrayList<HashMap<String, String>> ProductDataList = new ArrayList<>();

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


    public static String GetPrintReciptNew(String IsOtherRequire,String CompanyName, String PrintDate, String LinkName, String Location, String VehicleNumber, String PersonName, String Qty, String PrintCost, String OtherLabel, String OtherName, String Odometer, String Hours) {

        String content = "";

        String content_start = "<h1>------FluidSecure Receipt------</h1>\n\n\n" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><u>Company :</u><br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p>" + CompanyName + "<br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><b>Time/Date :</b><br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p>" + PrintDate + "<br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><b>Location  :</b><br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p>" + LinkName + ", " + Location + "<br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><b>Vehicle    :</b> " + VehicleNumber + "<br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><b>Personnel   :</b> " + PersonName + "<br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><b>Quantity    :</b> " + Qty + "<br/>" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><b>Cost ($)    :</b> " + PrintCost + "<br/>" +
                "        <p><u>\n</u><br/>";//empty line


        String con_Odo = "        <p><b>Odometer    :</b> " + Odometer + "<br/>" +
                "        <p><u>\n</u><br/>";//empty line

        String con_hours = "       <p><b>Hours       :</b> " + Hours + "<br/>" +
                "        <p><u>\n</u><br/>";//empty line

        String con_other = "<p>" + OtherLabel + ": \n\n\n\n" +
                "        <p><u>\n</u><br/>" +//empty line
                "        <p><b>" + OtherName + " </b><br/>" +
                "        <p><u>\n</u><br/>";//empty line


        String con_end = "        <h2>      ---------Thank You---------</h2>\n\n" +
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>" +//blank spase to cut paper
                "        <p><u>\n</u><br/>";//blank spase to cut paper


        content = content_start;
        if (IsOtherRequire.equalsIgnoreCase("true")) {
            content = content + con_other;
        }
       /* if (!Hours.equals("") && !Hours.equals("0")) {
            content = content + con_hours;
        }
        if (!Odometer.equalsIgnoreCase("") && !Odometer.equalsIgnoreCase("0")) {
            content = content + con_Odo;
        }*/
        content = content + con_end;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return String.valueOf(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
        } else {
            return String.valueOf(Html.fromHtml(content));
        }

    }

    public static String getdateToStoreInLink(String input){

        String DateOut = input;
        //Format of the date defined in the input String
        DateFormat df = new SimpleDateFormat("MM/DD/yyyy hh:mm:ss aaa");
        //Desired format: 24 hour format: Change the pattern as per the need
        DateFormat outputformat = new SimpleDateFormat("yyyyy-mm-dd hh:mm:ss");
        Date date = null;
        String output = null;
        try{
            //Converting the input String to Date
            date= df.parse(input);
            //Changing the format of date and storing it in String
            output = outputformat.format(date);
            DateOut =output;
            //Displaying the date
            System.out.println(output);
        }catch(ParseException pe){
            DateOut = input;
            pe.printStackTrace();
        }
        return DateOut;
    }

    public static String changeDateFormat(String input){

        /*String DateOut = date_s;
        try
        {
            //String date_s = "2011-01-18 00:00:00.0";
            SimpleDateFormat simpledateformat = new SimpleDateFormat("yyMMddHHmmss");
            Date tempDate=simpledateformat.parse(date_s);
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
            DateOut = outputDateFormat.format(tempDate).toString();
            System.out.println("Output date is = "+outputDateFormat.format(tempDate));
        } catch (ParseException ex)
        {
            DateOut = date_s;
            System.out.println("Parse Exception");
        }
        return DateOut;*/


        String DateOut = input;
        //Format of the date defined in the input String
        DateFormat df = new SimpleDateFormat("yyMMddHHmm");
        //Desired format: 24 hour format: Change the pattern as per the need
        DateFormat outputformat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");  //dd/MM/yyyy hh:mm:ss aa
        Date date = null;
        String output = null;
        try{
            //Converting the input String to Date
            date= df.parse(input);
            //Changing the format of date and storing it in String
            output = outputformat.format(date);
            DateOut =output;
            //Displaying the date
            System.out.println(output);
        }catch(ParseException pe){
            DateOut = input;
            pe.printStackTrace();
        }
        return DateOut;

    }

    public static String getDateInString() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        String CurrantDate = df.format(c.getTime());
        return (CurrantDate);
    }

    public static String getTodaysDateInString() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String CurrantDate = df.format(c.getTime());
        return (CurrantDate);
    }

    public static String getTodaysDateInStringbt() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyMMddhhmm");
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
                    time.add(Calendar.DAY_OF_YEAR, -60);
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

    public static void AutoCloseCustomMessageDialog(final Activity context, String title, String message) {

        /*//Declare timer
        CountDownTimer cTimer = null;
        final Dialog dialogBus = new Dialog(context);
        dialogBus.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogBus.setCancelable(false);
        dialogBus.setContentView(R.layout.custom_alertdialouge);
        dialogBus.show();

        String newString1 = message.replaceAll("PERSONNEL", "<font color='red'> " + "<U> PERSONNEL </U>" + " </font>");
        String newString = newString1.replaceAll("VEHICLE", "<font color='red'> " + "<U> VEHICLE </U>" + " </font>");

        TextView edt_message = (TextView) dialogBus.findViewById(R.id.edt_message);
        Button btnAllow = (Button) dialogBus.findViewById(R.id.btnAllow);
        edt_message.setText(Html.fromHtml(newString));

        cTimer = new CountDownTimer(4000, 4000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {

                dialogBus.dismiss();
                //editVehicleNumber.requestFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);

            }
        };
        cTimer.start();

        CountDownTimer finalCTimer = cTimer;
        btnAllow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogBus.dismiss();

                if (finalCTimer != null) finalCTimer.cancel();
                //editVehicleNumber.requestFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);

            }

        });*/ // Commented above code as per #1465

        final Timer timer = new Timer();
        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(context);

        String newString1 = message.replaceAll("PERSONNEL", "<font color='red'> " + "<U> PERSONNEL </U>" + " </font>");
        String newString = newString1.replaceAll("VEHICLE", "<font color='red'> " + "<U> VEHICLE </U>" + " </font>");

        alertDialogBuilder.setMessage(Html.fromHtml(newString));
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();

                        if (timer != null) {
                            timer.cancel();
                        }

                        if (!title.isEmpty()) {
                            InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        }
                    }
                }
        );

        androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                timer.cancel();
                if (!title.isEmpty()) {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            }
        }, 4000);

        alertDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void AutoCloseBTLinkMessage(final Activity context, String title, String message) {

        /*//Declare timer
        CountDownTimer cTimer = null;
        final Dialog dialogBus = new Dialog(context);
        dialogBus.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogBus.setCancelable(false);
        dialogBus.setContentView(R.layout.custom_alertdialouge);
        dialogBus.show();

        TextView edt_message = (TextView) dialogBus.findViewById(R.id.edt_message);
        Button btnAllow = (Button) dialogBus.findViewById(R.id.btnAllow);
        edt_message.setText(Html.fromHtml(message));

        cTimer = new CountDownTimer(20000, 20000) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {

                dialogBus.dismiss();
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                context.startActivity(intent);

            }
        };
        cTimer.start();

        CountDownTimer finalCTimer = cTimer;
        btnAllow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogBus.dismiss();

                if (finalCTimer != null) finalCTimer.cancel();
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                context.startActivity(intent);
            }
        });*/ // Commented above code as per #1465

        final Timer timer = new Timer();
        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();

                        if (timer != null) {
                            timer.cancel();
                        }

                        Intent intent = new Intent();
                        intent.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                        context.startActivity(intent);
                    }
                }
        );

        androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                timer.cancel();
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                context.startActivity(intent);
            }
        }, 20000);

        alertDialog.show();
    }

    public static void AlertDialogAutoClose(final Activity context, String title, String message) {

        final android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(context).setTitle(title).setMessage(message);
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                /*if (alert.isShowing()) {
                    alert.dismiss();
                }*/
            }
        });

        final android.app.AlertDialog alert = dialog.create();
        alert.show();

        // Hide after some seconds
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (alert.isShowing()) {
                    alert.dismiss();
                }
            }
        };

        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.removeCallbacks(runnable);
            }
        });

        handler.postDelayed(runnable, 4000);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void showCustomMessageDilaog(final Activity context, String title, String message) {

        String HoseUnavailableMessage = "";
        try {
            HoseUnavailableMessage = context.getResources().getString(R.string.HoseUnavailableMessage);
        } catch (Exception ex) {
            HoseUnavailableMessage = "";
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Exception in showCustomMessageDilaog: " + ex.getMessage());
        }

        /*final Dialog dialogBus = new Dialog(context);
        dialogBus.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogBus.setCancelable(false);
        dialogBus.setContentView(R.layout.custom_alertdialouge);
        dialogBus.show();

        String newString1 = message.replaceAll("PERSONNEL", "<font color='red'> " + "<U> PERSONNEL </U>" + " </font>");
        String newString = newString1.replaceAll("VEHICLE", "<font color='red'> " + "<U> VEHICLE </U>" + " </font>");

        TextView edt_message = (TextView) dialogBus.findViewById(R.id.edt_message);
        Button btnAllow = (Button) dialogBus.findViewById(R.id.btnAllow);
        edt_message.setText(Html.fromHtml(newString));

        String finalHoseUnavailableMessage = HoseUnavailableMessage;
        btnAllow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogBus.dismiss();

                if (message.equalsIgnoreCase(finalHoseUnavailableMessage)) {
                    AppConstants.GoButtonAlreadyClicked = false;
                }

                InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });*/ // Commented above code as per #1465

        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(context);

        String newString1 = message.replaceAll("PERSONNEL", "<font color='red'> " + "<U> PERSONNEL </U>" + " </font>");
        String newString = newString1.replaceAll("VEHICLE", "<font color='red'> " + "<U> VEHICLE </U>" + " </font>");

        alertDialogBuilder.setMessage(Html.fromHtml(newString));
        alertDialogBuilder.setCancelable(false);

        String finalHoseUnavailableMessage = HoseUnavailableMessage;
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();

                        if (message.equalsIgnoreCase(finalHoseUnavailableMessage)) {
                            AppConstants.GoButtonAlreadyClicked = false;
                        }

                        if (!title.isEmpty()) {
                            InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        }
                    }
                }
        );

        androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static void SimpleMessageDilaog(final Activity context, final String title, final String message) {



                new AlertDialog.Builder(context)
                        .setTitle(title)
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Whatever...
                            }
                        }).show();


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
        alertDialogBuilder.setTitle("Check Internet");
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

    public static void SaveLogFlagInPref(Activity activity, String data, String CompanyBrandName, String CompanyBrandLogoLink, String SupportEmail, String SupportPhonenumber) {

        SharedPreferences pref = activity.getSharedPreferences(Constants.PREF_Log_Data, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(AppConstants.LogRequiredFlag, data);
        editor.putString(AppConstants.CompanyBrandName, CompanyBrandName);
        editor.putString(AppConstants.CompanyBrandLogoLink, CompanyBrandLogoLink);
        editor.putString(AppConstants.SupportEmail, SupportEmail);
        editor.putString(AppConstants.SupportPhonenumber, SupportPhonenumber);
        editor.commit();


    }

    public static void FA_FlagSavePref(Activity activity, boolean data,boolean barcodedata,boolean IsEnableServerForTLD,boolean UseBarcodeForPersonnel ) {

        SharedPreferences pref = activity.getSharedPreferences(Constants.PREF_FA_Data, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(AppConstants.FAData, data);
        editor.putBoolean(AppConstants.IsEnableServerForTLD, IsEnableServerForTLD);
        editor.putBoolean(AppConstants.UseBarcode, barcodedata);
        editor.putBoolean(AppConstants.UseBarcodeForPersonnel , UseBarcodeForPersonnel );
        editor.commit();

    }

    public static void SaveDataInPrefForGatehub (Activity activity, String IsGateHub, String IsStayOpenGate) {

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.PREF_COLUMN_GATE_HUB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(AppConstants.IsGateHub, IsGateHub);
        editor.putString(AppConstants.IsStayOpenGate, IsStayOpenGate);
        editor.commit();
    }

    public static void SaveHotSpotDetailsInPref(Activity activity, String HotSpotSSID, String HotSpotPassword) {
        try {
            SharedPreferences prefHotSpot = activity.getSharedPreferences("HotSpotDetails", Context.MODE_PRIVATE);
            SharedPreferences.Editor edHotSpot = prefHotSpot.edit();
            edHotSpot.putString("HotSpotSSID", HotSpotSSID);
            edHotSpot.putString("HotSpotPassword", HotSpotPassword);
            edHotSpot.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SaveDataInPref(Activity activity, String data, String valueType) {

        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_COLUMN_SITE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(valueType, data);
        editor.commit();
    }

    public static void SaveOfflineDbSizeDateTime(Context context, String SaveDate) {

        SharedPreferences sharedPref = context.getSharedPreferences(PREF_OFF_DB_SIZE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(AppConstants.DbUpdateTime, SaveDate);
        editor.commit();
    }

    public static void SaveUserInPref(Activity activity, String userName, String userMobile, String userEmail, String IsOdoMeterRequire,
                                      String IsDepartmentRequire, String IsPersonnelPINRequire, String IsOtherRequire, String IsHoursRequire,
                                      String OtherLabel, String TimeOut, String HubId, String IsPersonnelPINRequireForHub, String fluidSecureSiteName,
                                      String IsVehicleHasFob, String isPersonHasFob, String IsVehicleNumberRequire, int WifiChannelToUse, String HubType,
                                      String IsNonValidateVehicle, String IsNonValidatePerson, String IsPersonPinAndFOBRequire, String AllowAccessDeviceORManualEntry,
                                      String AllowAccessDeviceORManualEntryForVehicle, String CompanyName) {

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
        editor.putString(AppConstants.IsPersonPinAndFOBRequire,  IsPersonPinAndFOBRequire);
        editor.putString(AppConstants.AllowAccessDeviceORManualEntry,  AllowAccessDeviceORManualEntry);
        editor.putString(FluidSecureSiteName,  fluidSecureSiteName);
        editor.putString(AppConstants.IsVehicleNumberRequire,  IsVehicleNumberRequire);
        editor.putInt(AppConstants.WifiChannelToUse,  WifiChannelToUse);
        editor.putString(AppConstants.IsNonValidateVehicle,  IsNonValidateVehicle);
        editor.putString(AppConstants.IsNonValidatePerson,  IsNonValidatePerson);
        editor.putString(AppConstants.HubType,  HubType);
        editor.putString(AppConstants.AllowAccessDeviceORManualEntryForVehicle,  AllowAccessDeviceORManualEntryForVehicle);
        editor.putString("CompanyName", CompanyName);

        editor.commit();
    }

    public static void SaveTldDetailsInPref(Context activity, String IsTLDCall, String IsTLDFirmwareUpgrade, String TLDFirmwareFilePath, String TLDFIrmwareVersion, String PROBEMacAddress, String selMacAddress) {

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


    public static void SaveVehiFuelInPref_FS1(Context activity, String TransactionId_FS1, String VehicleId_FS1, String PhoneNumber_FS1, String PersonId_FS1, String PulseRatio_FS1, String MinLimit_FS1, String FuelTypeId_FS1, String ServerDate_FS1, String IntervalToStopFuel_FS1, String PrintDate_FS1, String Company_FS1, String Location_FS1, String PersonName_FS1, String PrinterMacAddress_FS1, String PrinterName_FS1, String vehicleNumber_FS1, String accOther_FS1, String VehicleSum_FS1, String DeptSum_FS1, String VehPercentage_FS1, String DeptPercentage_FS1, String SurchargeType_FS1, String ProductPrice_FS1, String IsTLDCall_FS1, String EnablePrinter_FS1, String OdoMeter_FS1, String Hours_FS1, String PumpOnTime_FS1,String LimitReachedMessage_FS1,String VehicleNumber_FS1,String TransactionDateWithFormat_FS1,String SiteId_FS1) {

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("TransactionId_FS1", TransactionId_FS1);
        editor.putString("VehicleId_FS1", VehicleId_FS1);
        editor.putString("VehicleNumber_FS1", VehicleNumber_FS1);
        editor.putString("PhoneNumber_FS1", PhoneNumber_FS1);
        editor.putString("PersonId_FS1", PersonId_FS1);
        editor.putString("PulseRatio_FS1", PulseRatio_FS1);
        editor.putString("MinLimit_FS1", MinLimit_FS1);
        editor.putString("FuelTypeId_FS1", FuelTypeId_FS1);
        editor.putString("ServerDate_FS1", ServerDate_FS1);
        editor.putString("TransactionDateWithFormat_FS1", TransactionDateWithFormat_FS1);
        editor.putString("IntervalToStopFuel_FS1", IntervalToStopFuel_FS1);
        editor.putString("PumpOnTime_FS1", PumpOnTime_FS1);
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
        editor.putString("OdoMeter_FS1", OdoMeter_FS1);
        editor.putString("Hours_FS1", Hours_FS1);
        editor.putString("LimitReachedMessage_FS1", LimitReachedMessage_FS1);
        editor.putString("SiteId_FS1", SiteId_FS1);


        editor.commit();
    }

    public static void SaveVehiFuelInPref(Context activity, String TransactionId, String VehicleId, String PhoneNumber, String PersonId, String PulseRatio, String MinLimit, String FuelTypeId, String ServerDate, String IntervalToStopFuel, String PrintDate, String Company, String Location, String PersonName, String PrinterMacAddress, String PrinterName, String vehicleNumber, String accOther, String VehicleSum, String DeptSum, String VehPercentage, String DeptPercentage, String SurchargeType, String ProductPrice, String IsTLDCall1, String EnablePrinter, String OdoMeter, String Hours, String PumpOnTime,String LimitReachedMessage,String VehicleNumber,String TransactionDateWithFormat,String SiteId) {

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("TransactionId", TransactionId);
        editor.putString("VehicleId", VehicleId);
        editor.putString("VehicleNumber", VehicleNumber);
        editor.putString("PhoneNumber", PhoneNumber);
        editor.putString("PersonId", PersonId);
        editor.putString("PulseRatio", PulseRatio);
        editor.putString("MinLimit", MinLimit);
        editor.putString("FuelTypeId", FuelTypeId);
        editor.putString("ServerDate", ServerDate);
        editor.putString("TransactionDateWithFormat", TransactionDateWithFormat);
        editor.putString("IntervalToStopFuel", IntervalToStopFuel);
        editor.putString("PumpOnTime", PumpOnTime);
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
        editor.putString("OdoMeter", OdoMeter);
        editor.putString("Hours", Hours);
        editor.putString("LimitReachedMessage", LimitReachedMessage);
        editor.putString("SiteId", SiteId);


        editor.commit();
    }

    public static void SaveVehiFuelInPref_FS3(Context activity, String TransactionId_FS3, String VehicleId_FS3, String PhoneNumber_FS3, String PersonId_FS3, String PulseRatio_FS3, String MinLimit_FS3, String FuelTypeId_FS3, String ServerDate_FS3, String IntervalToStopFuel_FS3, String PrintDate_FS3, String Company_FS3, String Location_FS3, String PersonName_FS3, String PrinterMacAddress_FS3, String PrinterName_FS3, String vehicleNumber_FS3, String accOther_FS3, String VehicleSum_FS3, String DeptSum_FS3, String VehPercentage_FS3, String DeptPercentage_FS3, String SurchargeType_FS3, String ProductPrice_FS3, String IsTLDCall_FS3, String EnablePrinter_FS3, String OdoMeter_FS3, String Hours_FS3, String PumpOnTime_FS3,String LimitReachedMessage_FS3,String VehicleNumber_FS3,String TransactionDateWithFormat_FS3,String SiteId_FS3) {

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("TransactionId_FS3", TransactionId_FS3);
        editor.putString("VehicleId_FS3", VehicleId_FS3);
        editor.putString("VehicleNumber_FS3", VehicleNumber_FS3);
        editor.putString("PhoneNumber_FS3", PhoneNumber_FS3);
        editor.putString("PersonId_FS3", PersonId_FS3);
        editor.putString("PulseRatio_FS3", PulseRatio_FS3);
        editor.putString("MinLimit_FS3", MinLimit_FS3);
        editor.putString("FuelTypeId_FS3", FuelTypeId_FS3);
        editor.putString("ServerDate_FS3", ServerDate_FS3);
        editor.putString("TransactionDateWithFormat_FS3", TransactionDateWithFormat_FS3);
        editor.putString("IntervalToStopFuel_FS3", IntervalToStopFuel_FS3);
        editor.putString("PumpOnTime_FS3", PumpOnTime_FS3);
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
        editor.putString("OdoMeter_FS3", OdoMeter_FS3);
        editor.putString("Hours_FS3", Hours_FS3);
        editor.putString("LimitReachedMessage_FS3", LimitReachedMessage_FS3);
        editor.putString("SiteId_FS3", SiteId_FS3);

        editor.commit();
    }

    public static void SaveVehiFuelInPref_FS4(Context activity, String TransactionId_FS4, String VehicleId_FS4, String PhoneNumber_FS4, String PersonId_FS4, String PulseRatio_FS4, String MinLimit_FS4, String FuelTypeId_FS4, String ServerDate_FS4, String IntervalToStopFuel_FS4, String PrintDate_FS4, String Company_FS4, String Location_FS4, String PersonName_FS4, String PrinterMacAddress_FS4, String PrinterName_FS4, String vehicleNumber_FS4, String accOther_FS4, String VehicleSum_FS4, String DeptSum_FS4, String VehPercentage_FS4, String DeptPercentage_FS4, String SurchargeType_FS4, String ProductPrice_FS4, String IsTLDCall_FS4, String EnablePrinter_FS4, String OdoMeter_FS4, String Hours_FS4, String PumpOnTime_FS4,String LimitReachedMessage_FS4,String VehicleNumber_FS4,String TransactionDateWithFormat_FS4,String SiteId_FS4) {

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("TransactionId_FS4", TransactionId_FS4);
        editor.putString("VehicleId_FS4", VehicleId_FS4);
        editor.putString("VehicleNumber_FS4", VehicleNumber_FS4);
        editor.putString("PhoneNumber_FS4", PhoneNumber_FS4);
        editor.putString("PersonId_FS4", PersonId_FS4);
        editor.putString("PulseRatio_FS4", PulseRatio_FS4);
        editor.putString("MinLimit_FS4", MinLimit_FS4);
        editor.putString("FuelTypeId_FS4", FuelTypeId_FS4);
        editor.putString("ServerDate_FS4", ServerDate_FS4);
        editor.putString("TransactionDateWithFormat_FS4", TransactionDateWithFormat_FS4);
        editor.putString("IntervalToStopFuel_FS4", IntervalToStopFuel_FS4);
        editor.putString("PumpOnTime_FS4", PumpOnTime_FS4);
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
        editor.putString("OdoMeter_FS4", OdoMeter_FS4);
        editor.putString("Hours_FS4", Hours_FS4);
        editor.putString("LimitReachedMessage_FS4", LimitReachedMessage_FS4);
        editor.putString("SiteId_FS4", SiteId_FS4);


        editor.commit();
    }

    public static void SaveVehiFuelInPref_FS5(Context activity, String TransactionId_FS5, String VehicleId_FS5, String PhoneNumber_FS5, String PersonId_FS5, String PulseRatio_FS5, String MinLimit_FS5, String FuelTypeId_FS5, String ServerDate_FS5, String IntervalToStopFuel_FS5, String PrintDate_FS5, String Company_FS5, String Location_FS5, String PersonName_FS5, String PrinterMacAddress_FS5, String PrinterName_FS5, String vehicleNumber_FS5, String accOther_FS5, String VehicleSum_FS5, String DeptSum_FS5, String VehPercentage_FS5, String DeptPercentage_FS5, String SurchargeType_FS5, String ProductPrice_FS5, String IsTLDCall_FS5, String EnablePrinter_FS5, String OdoMeter_FS5, String Hours_FS5, String PumpOnTime_FS5,String LimitReachedMessage_FS5,String VehicleNumber_FS5,String TransactionDateWithFormat_FS5,String SiteId_FS5) {

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("TransactionId_FS5", TransactionId_FS5);
        editor.putString("VehicleId_FS5", VehicleId_FS5);
        editor.putString("VehicleNumber_FS5", VehicleNumber_FS5);
        editor.putString("PhoneNumber_FS5", PhoneNumber_FS5);
        editor.putString("PersonId_FS5", PersonId_FS5);
        editor.putString("PulseRatio_FS5", PulseRatio_FS5);
        editor.putString("MinLimit_FS5", MinLimit_FS5);
        editor.putString("FuelTypeId_FS5", FuelTypeId_FS5);
        editor.putString("ServerDate_FS5", ServerDate_FS5);
        editor.putString("TransactionDateWithFormat_FS5", TransactionDateWithFormat_FS5);
        editor.putString("IntervalToStopFuel_FS5", IntervalToStopFuel_FS5);
        editor.putString("PumpOnTime_FS5", PumpOnTime_FS5);
        editor.putString("PrintDate_FS5", PrintDate_FS5);
        editor.putString("Company_FS5", Company_FS5);
        editor.putString("Location_FS5", Location_FS5);
        editor.putString("PersonName_FS5", PersonName_FS5);
        editor.putString("PrinterMacAddress_FS5", PrinterMacAddress_FS5);
        editor.putString("PrinterName_FS5", PrinterName_FS5);
        editor.putString("vehicleNumber_FS5", vehicleNumber_FS5);
        editor.putString("accOther_FS5", accOther_FS5);
        editor.putString("VehicleSum_FS5", VehicleSum_FS5);
        editor.putString("DeptSum_FS5", DeptSum_FS5);
        editor.putString("VehPercentage_FS5", VehPercentage_FS5);
        editor.putString("DeptPercentage_FS5", DeptPercentage_FS5);
        editor.putString("SurchargeType_FS5", SurchargeType_FS5);
        editor.putString("ProductPrice_FS5", ProductPrice_FS5);
        editor.putString("IsTLDCall_FS5", IsTLDCall_FS5);
        editor.putString("EnablePrinter_FS5", EnablePrinter_FS5);
        editor.putString("OdoMeter_FS5", OdoMeter_FS5);
        editor.putString("Hours_FS5", Hours_FS5);
        editor.putString("LimitReachedMessage_FS5", LimitReachedMessage_FS5);
        editor.putString("SiteId_FS5", SiteId_FS5);


        editor.commit();
    }

    public static void SaveVehiFuelInPref_FS6(Context activity, String TransactionId_FS6, String VehicleId_FS6, String PhoneNumber_FS6, String PersonId_FS6, String PulseRatio_FS6, String MinLimit_FS6, String FuelTypeId_FS6, String ServerDate_FS6, String IntervalToStopFuel_FS6, String PrintDate_FS6, String Company_FS6, String Location_FS6, String PersonName_FS6, String PrinterMacAddress_FS6, String PrinterName_FS6, String vehicleNumber_FS6, String accOther_FS6, String VehicleSum_FS6, String DeptSum_FS6, String VehPercentage_FS6, String DeptPercentage_FS6, String SurchargeType_FS6, String ProductPrice_FS6, String IsTLDCall_FS6, String EnablePrinter_FS6, String OdoMeter_FS6, String Hours_FS6, String PumpOnTime_FS6,String LimitReachedMessage_FS6,String VehicleNumber_FS6,String TransactionDateWithFormat_FS6,String SiteId_FS6) {

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("TransactionId_FS6", TransactionId_FS6);
        editor.putString("VehicleId_FS6", VehicleId_FS6);
        editor.putString("VehicleNumber_FS6", VehicleNumber_FS6);
        editor.putString("PhoneNumber_FS6", PhoneNumber_FS6);
        editor.putString("PersonId_FS6", PersonId_FS6);
        editor.putString("PulseRatio_FS6", PulseRatio_FS6);
        editor.putString("MinLimit_FS6", MinLimit_FS6);
        editor.putString("FuelTypeId_FS6", FuelTypeId_FS6);
        editor.putString("ServerDate_FS6", ServerDate_FS6);
        editor.putString("TransactionDateWithFormat_FS6", TransactionDateWithFormat_FS6);
        editor.putString("IntervalToStopFuel_FS6", IntervalToStopFuel_FS6);
        editor.putString("PumpOnTime_FS6", PumpOnTime_FS6);
        editor.putString("PrintDate_FS6", PrintDate_FS6);
        editor.putString("Company_FS6", Company_FS6);
        editor.putString("Location_FS6", Location_FS6);
        editor.putString("PersonName_FS6", PersonName_FS6);
        editor.putString("PrinterMacAddress_FS6", PrinterMacAddress_FS6);
        editor.putString("PrinterName_FS6", PrinterName_FS6);
        editor.putString("vehicleNumber_FS6", vehicleNumber_FS6);
        editor.putString("accOther_FS6", accOther_FS6);
        editor.putString("VehicleSum_FS6", VehicleSum_FS6);
        editor.putString("DeptSum_FS6", DeptSum_FS6);
        editor.putString("VehPercentage_FS6", VehPercentage_FS6);
        editor.putString("DeptPercentage_FS6", DeptPercentage_FS6);
        editor.putString("SurchargeType_FS6", SurchargeType_FS6);
        editor.putString("ProductPrice_FS6", ProductPrice_FS6);
        editor.putString("IsTLDCall_FS6", IsTLDCall_FS6);
        editor.putString("EnablePrinter_FS6", EnablePrinter_FS6);
        editor.putString("OdoMeter_FS6", OdoMeter_FS6);
        editor.putString("Hours_FS6", Hours_FS6);
        editor.putString("LimitReachedMessage_FS6", LimitReachedMessage_FS6);
        editor.putString("SiteId_FS6", SiteId_FS6);


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

    public static UserInfoEntity getCustomerDetailsbackgroundService(BackgroundService activity) {

        UserInfoEntity userInfoEntity = new UserInfoEntity();

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

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

    public static UserInfoEntity getCustomerDetails_KeepAliveBT(BackgroundServiceKeepAliveBT activity) {

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

    public static UserInfoEntity getCustomerDetails_backgroundServiceBT(Context ctx) {

        UserInfoEntity userInfoEntity = new UserInfoEntity();
        SharedPreferences sharedPref = ctx.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        userInfoEntity.PersonName = sharedPref.getString(AppConstants.USER_NAME, "");
        userInfoEntity.PhoneNumber = sharedPref.getString(AppConstants.USER_MOBILE, "");
        userInfoEntity.PersonEmail = sharedPref.getString(AppConstants.USER_EMAIL, "");
        userInfoEntity.FluidSecureSiteName = sharedPref.getString(AppConstants.FluidSecureSiteName, "");


        return userInfoEntity;
    }

    public static UserInfoEntity getCustomerDetails_backgroundService_FS5(BackgroundService_FS_UNIT_5 activity) {

        UserInfoEntity userInfoEntity = new UserInfoEntity();

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        userInfoEntity.PersonName = sharedPref.getString(AppConstants.USER_NAME, "");
        userInfoEntity.PhoneNumber = sharedPref.getString(AppConstants.USER_MOBILE, "");
        userInfoEntity.PersonEmail = sharedPref.getString(AppConstants.USER_EMAIL, "");
        userInfoEntity.FluidSecureSiteName = sharedPref.getString(AppConstants.FluidSecureSiteName, "");


        return userInfoEntity;
    }

    public static UserInfoEntity getCustomerDetails_backgroundService_FS6(BackgroundService_FS_UNIT_6 activity) {

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
            val = 16 * val + d;
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
            if (!AppConstants.ListOfRunningTransactions.contains(TxnId)) {
                AppConstants.ListOfRunningTransactions.add(TxnId);
            }
        } else {
            if (AppConstants.ListOfRunningTransactions != null && AppConstants.ListOfRunningTransactions.contains(TxnId)) {
                AppConstants.ListOfRunningTransactions.remove(TxnId);

            }
        }
    }


    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service already", "running");
                return true;
            }
        }
        Log.i("Service not", "running");
        return false;
    }

    public static boolean checkServiceRunning(Context context, String package_name) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (package_name.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean ValidateFobkey(String s1) {

        if (s1.equals(null) || s1.isEmpty())
            return false;

        int n = s1.length();

        for (int i = 0; i < n; i++) {

            if(s1.charAt(i) != '0')
            {
                return true;
            }else{
                //return false;
            }

        }

        return false;
    }

    public static void PlayBeep(Context context) {

//        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100000);
//        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 200);

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void LogReaderDetails(Context context) {

        SharedPreferences sharedPre2 = context.getSharedPreferences("storeBT_FOBDetails", Context.MODE_PRIVATE);

        String mDeviceName = sharedPre2.getString("LFBluetoothCardReader", "");
        String mDeviceAddress = sharedPre2.getString("LFBluetoothCardReaderMacAddress", "");
        String ACSDeviceName = sharedPre2.getString("BluetoothCardReader", "");//ACR1255U-J1-006851
        String ACSDeviceAddress = sharedPre2.getString("BTMacAddress", "");
        String mDeviceName_hf_trak = sharedPre2.getString("HFTrakCardReader", ""); //
        String mDeviceAddress_hf_trak = sharedPre2.getString("HFTrakCardReaderMacAddress", ""); //
        AppConstants.ACS_READER = sharedPre2.getBoolean("ACS_Reader", false);
        String mMagCardDeviceName = sharedPre2.getString("MagneticCardReader", ""); //
        String mMagCardDeviceAddress = sharedPre2.getString("MagneticCardReaderMacAddress", ""); //
        String QRCodeReaderForBarcode = sharedPre2.getString("QRCodeReaderForBarcode", ""); //
        String QRCodeBluetoothMacAddressForBarcode = sharedPre2.getString("QRCodeBluetoothMacAddressForBarcode", ""); //


        if (AppConstants.ACS_READER) {
            if (AppConstants.GenerateLogs && ACSDeviceAddress.contains(":"))
                AppConstants.WriteinFile("ACSReader Name:" + ACSDeviceName + " MacAddress:" + ACSDeviceAddress);
        } else {
            if (AppConstants.GenerateLogs && mDeviceAddress_hf_trak.contains(":"))
                AppConstants.WriteinFile("HFReader name:" + mDeviceName_hf_trak + " MacAddress: " + mDeviceAddress_hf_trak);
        }

        //LfReader
        if (AppConstants.GenerateLogs && mDeviceAddress.contains(":"))
            AppConstants.WriteinFile("LFReader Name:" + mDeviceName + " MacAddress: " + mDeviceAddress);

        if (AppConstants.GenerateLogs && mMagCardDeviceAddress.contains(":"))
            AppConstants.WriteinFile("MagneticReader Name:" + mMagCardDeviceName + " MacAddress: " + mMagCardDeviceAddress);

        if (AppConstants.GenerateLogs && QRCodeBluetoothMacAddressForBarcode.contains(":"))
            AppConstants.WriteinFile("QRCodeReader Name:" + QRCodeReaderForBarcode + " MacAddress: " + QRCodeBluetoothMacAddressForBarcode);

    }

    public static void UpgradeTransactionStatusToSqlite(String TransactionId, String status, Context ctx) {

        try {
            if (!TransactionId.isEmpty()) {
                DBController controller = new DBController(ctx);
                HashMap<String, String> mapsts = new HashMap<>();
                mapsts.put("transId", TransactionId);
                mapsts.put("transStatus", status);

                controller.insertTransStatusWithOnConflict(mapsts);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void enableMobileHotspotmanuallyStartTimer(final Context context) {

        wifiApManager.setWifiApEnabled(null, true); //one try for auto on
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!isHotspotEnabled(context)){
            if (Build.VERSION.SDK_INT > Constants.VERSION_CODES_NINE){

                final int[] tick_count = {0};
                final boolean[] sendEmail = {true};
                TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
                String operatorName = telephonyManager.getNetworkOperatorName();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG +" Network Operator Name:"+operatorName);

                if (operatorName.contains("AT&T") && isAppInstalled(context,"com.smartcom")){

                    try {
                        Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.smartcom");
                        context.startActivity(intent);

                    }catch (Exception e){
                        e.printStackTrace();
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG +" AT&T AllAccess app launch Exception "+operatorName);
                    }

                }else{

                    //AppConstants.colorToastHotspotOn(context, "Enable Mobile Hotspot Manually..", Color.BLUE);
                    Intent tetherSettings = new Intent();//com.smartcom
                    tetherSettings.setClassName("com.android.settings", "com.android.settings.TetherSettings");
                    context.startActivity(tetherSettings);

                }

                new CountDownTimer(60000 * 60, 20000) {

                    public void onTick(long millisUntilFinished) {


                        Log.i(TAG, "Waiting to connect hotspot remaining seconds: " + millisUntilFinished / 1000);
                        if (CommonUtils.isHotspotEnabled(context)) {
                            Log.i(TAG, "Hotspot detected disable timer..");
                            cancel();
                            //BackTo Welcome Activity
                            Intent i = new Intent(context, WelcomeActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            context.startActivity(i);

                        } else {
                            //if (millisUntilFinished / 1000 <= 13)
                            //AppConstants.colorToastHotspotOn(context, "Please press  Mobile      ^     \nHotspot button. \nWaiting seconds..." + millisUntilFinished / 1000, Color.BLUE);
                            /*if (tick_count[0] > 2 && !WelcomeActivity.OnWelcomeActivity)
                                AppConstants.colorToastHotspotOn(context, "We have detected that        " + context.getString(R.string.arrow_uni_code) + "   Mobile Hotspot is off. \n\nPlease press the Hotspot Toggle above.", Color.WHITE, Color.BLUE);*/
                        }

                        tick_count[0]++;
                    }

                    public void onFinish() {

                        if (CommonUtils.isHotspotEnabled(context)) {
                            Log.i(TAG, "Hotspot detected disable timer..");

                        } else {
                            Log.i(TAG, "Hotspot disable timer finish.. send email.");
                            //Email functionality
                            boolean check_mail = sendEmail[0];
                            if (isConnecting(context) && check_mail) {
                                sendEmail[0] = false;
                                SendEmailMobileHotspotErrorEmail(context);
                            }

                        }

                        //BackTo Welcome Activity
                        Intent i = new Intent(context, WelcomeActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(i);

                    }

                }.start();

            }else{
                //Android 8 and below...
                wifiApManager.setWifiApEnabled(null, true);
            }
        }
    }

    //Below function to toggle hotspot to resolve Link unavailable issue
    public static void ToggleHotspottoRefreshNetwork(final Context context, boolean enableManually) {

        if (enableManually){

            final int[] tick_count = {0};
            final boolean[] sendEmail = {true};
            TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
            String operatorName = telephonyManager.getNetworkOperatorName();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG +" Network Operator Name:"+operatorName);

            if (operatorName.contains("AT&T") && isAppInstalled(context,"com.smartcom")){

                try {
                    Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.smartcom");
                    context.startActivity(intent);

                }catch (Exception e){
                    e.printStackTrace();
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG +" AT&T AllAccess app launch Exception "+operatorName);
                }

            }else{

                //AppConstants.colorToastHotspotOn(context, "Enable Mobile Hotspot Manually..", Color.BLUE);
                Intent tetherSettings = new Intent();//com.smartcom
                tetherSettings.setClassName("com.android.settings", "com.android.settings.TetherSettings");
                context.startActivity(tetherSettings);

            }

            new CountDownTimer(60000 * 60, 1000) {

                public void onTick(long millisUntilFinished) {


                    Log.i(TAG, "Waiting to connect hotspot remaining seconds: " + millisUntilFinished / 1000);
                    if (CommonUtils.isHotspotEnabled(context)) {
                        Log.i(TAG, "Hotspot detected disable timer..");
                        cancel();
                        //BackTo Welcome Activity
                        Intent i = new Intent(context, WelcomeActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(i);

                    } else {
                        //if (millisUntilFinished / 1000 <= 13)
                        //AppConstants.colorToastHotspotOn(context, "Please press  Mobile      ^     \nHotspot button. \nWaiting seconds..." + millisUntilFinished / 1000, Color.BLUE);
                        /*if (tick_count[0] > 2 && WelcomeActivity.OnWelcomeActivity == false)
                            AppConstants.colorToastHotspotOn(context, "We have detected that        " + context.getString(R.string.arrow_uni_code) + "   Mobile Hotspot is off. \n\nPlease press the Hotspot Toggle above.", Color.WHITE, Color.BLUE);*/
                    }

                    tick_count[0]++;
                }

                public void onFinish() {

                    if (CommonUtils.isHotspotEnabled(context)) {
                        Log.i(TAG, "Hotspot detected disable timer..");

                    } else {
                        Log.i(TAG, "Hotspot disable timer finish.. send email.");
                    }

                    //BackTo Welcome Activity
                    Intent i = new Intent(context, WelcomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(i);

                }

            }.start();

        }else{

            //Toggle hotspot programatically
            Boolean toggle_success = true;
            wifiApManager.setWifiApEnabled(null, false);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (isHotspotEnabled(context)){
               // Log.i(TAG, "ToggleHotspot Failed to disable hotspot");
            }else{
                Log.i(TAG, "ToggleHotspot hotspot OFF");
            }
            wifiApManager.setWifiApEnabled(null, true);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (isHotspotEnabled(context)){
                Log.i(TAG, "ToggleHotspot hotspot ON");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "ToggleHotspot hotspot Enabled");
            }else{
                Log.i(TAG, "ToggleHotspot failed to enable hotspot");
            }

        }

    }

    public static void SendEmailMobileHotspotErrorEmail(Context context) {


        Log.i(TAG, "Email  call..");
        UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetailsCC(context);

        StatusForUpgradeVersionEntity objEntityClass2 = new StatusForUpgradeVersionEntity();
        objEntityClass2.IMEIUDID = AppConstants.getIMEI(context);
        objEntityClass2.HubName = userInfoEntity.PersonName;
        objEntityClass2.SiteName = userInfoEntity.FluidSecureSiteName;

        Gson gson = new Gson();
        String parm2 = gson.toJson(objEntityClass2);

        String userEmail = CommonUtils.getCustomerDetailsCC(context).PersonEmail;
        //----------------------------------------------------------------------------------
        String parm1 = AppConstants.getIMEI(context) + ":" + userEmail + ":" + "MobileHotspotErrorEmail" + AppConstants.LANG_PARAM;
        String authString = "Basic " + AppConstants.convertStingToBase64(parm1);


        RequestBody body = RequestBody.create(TEXT, parm2);
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(AppConstants.webURL)
                .post(body)
                .addHeader("Authorization", authString)
                .build();

        httpClient.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "error in getting response");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " MobileHotspotErrorEmail Hotspot error in getting response");
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(com.squareup.okhttp.Response response) throws IOException {

                ResponseBody responseBody = response.body();
                if (!response.isSuccessful()) {
                    throw new IOException("Error response " + response);
                } else {

                    String result = responseBody.string();
                    Log.e(TAG, "HOTSPOT-" + result);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " MobileHotspotErrorEmail ~Result\n" + result);

                    try {

                        JSONObject jsonObjectSite = null;
                        jsonObjectSite = new JSONObject(result);

                        String ResponseMessageSite = jsonObjectSite.getString(AppConstants.RES_MESSAGE);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

        });
    }

    public static boolean isConnecting(Context context){
        boolean isConnected=false;

        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {

            NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();
            isConnected = activeNetwork != null &&	activeNetwork.isConnectedOrConnecting();
        }
        return isConnected;
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        }
        catch (PackageManager.NameNotFoundException e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG +" It seems that AT&T AllAccess app not installed. please check.");
            return false;
        }
    }



    public static void SaveTransactionInSharedPreff(Context activity, String LinkSeq,String TransactonPulses) {

        Log.i(TAG,"LinkSeq:"+LinkSeq+" TransactonPulses:"+TransactonPulses);
        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.PREF_TransactionDetails, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (LinkSeq.equals("1")){
            editor.putString("LinkSeq_one", TransactonPulses);
        }else if (LinkSeq.equals("2")){
            editor.putString("LinkSeq_two", TransactonPulses);
        }else if (LinkSeq.equals("3")){
            editor.putString("LinkSeq_three", TransactonPulses);
        }else if (LinkSeq.equals("4")){
            editor.putString("LinkSeq_four", TransactonPulses);
        }else if (LinkSeq.equals("5")){
            editor.putString("LinkSeq_five", TransactonPulses);
        }else if (LinkSeq.equals("6")){
            editor.putString("LinkSeq_six", TransactonPulses);
        }

        editor.commit();
    }

    public static String getLinkName(int linkPosition){

        String LinkName = "";
            try {
                LinkName = AppConstants.DetailsServerSSIDList.get(linkPosition).get("WifiSSId");
            } catch (Exception e) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG+ "Something went wrong please check Link name Ex:"+e.toString());
                e.printStackTrace();
            }
        return LinkName;
    }

    public static String getOriginalNamesOfLink(int linkPosition) {
        String OriginalNamesOfLink = "";
        try {
            OriginalNamesOfLink = AppConstants.DetailsServerSSIDList.get(linkPosition).get("OriginalNamesOfLink");
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Something went wrong please check Link data Ex: " + e.toString());
            e.printStackTrace();
        }
        return OriginalNamesOfLink;
    }

    public static void DebugLogTemp(String TAG, String Fun){

        //This code is to check the issue with the duplicate sqlite insert Qty
        //#1526  Winter Haven Time Issue
        Log.i(TAG, Fun);
        if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + Fun);

    }

    public static void BindTankData(String tanksForLinksObj, boolean clearTankList) {

        try {
            if (clearTankList) {
                TankDataList.clear();
            }
            JSONArray jsonArray = new JSONArray(tanksForLinksObj);
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObj = jsonArray.getJSONObject(i);
                String TankNumber = jsonObj.getString("TankNumber");
                String TankName = jsonObj.getString("TankName");
                String ScheduleTankReading = jsonObj.getString("ScheduleTankReading");
                String ReceiveDeliveryInformation = jsonObj.getString("ReceiveDeliveryInformation");
                String TankId = jsonObj.getString("TankId");
                String FuelTypeId = jsonObj.getString("FuelTypeId");

                HashMap<String, String> map = new HashMap<>();
                map.put("TankNumber", TankNumber);
                map.put("TankName", TankName);
                map.put("ScheduleTankReading", ScheduleTankReading);
                map.put("ReceiveDeliveryInformation", ReceiveDeliveryInformation);
                map.put("TankId", TankId);
                map.put("FuelTypeId", FuelTypeId);

                TankDataList.add(map);

            }
            Log.i(TAG, "TankDataList" + TankDataList.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void BindProductData(String productsForTanksObj){

        try {

            ProductDataList.clear();
            JSONArray jsonArray = new JSONArray(productsForTanksObj);
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObj = jsonArray.getJSONObject(i);
                String FuelTypeId = jsonObj.getString("FuelTypeID");
                String FuelType = jsonObj.getString("FuelType");

                HashMap<String, String> map = new HashMap<>();
                map.put("FuelTypeId", FuelTypeId);
                map.put("FuelType", FuelType);

                ProductDataList.add(map);

            }
            Log.i(TAG, "ProductDataList" + ProductDataList.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getHUBNumberByName(String hubName) {
        String HUBNumber = "";
        try {
            if (!hubName.trim().equals("")) {
                String number = hubName.substring(hubName.length() - 8);    // "HUB12345678" to "12345678"
                String strPattern = "^0+(?!$)";                             // Pattern to remove all leading zeros.
                HUBNumber = number.replaceAll(strPattern, "");   // "HUB00000123" to "123"
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Exception occurred while getting HUB Number by Name.>>" + e.getMessage());
            HUBNumber = hubName;
        }
        return HUBNumber.trim();
    }

    public static String getSpareHUBNumberByName(String hubName) {
        String HUBNumber = "";
        try {
            String name = hubName.substring(0, hubName.length() - 8);    // "SPARE12345678" to "SPARE"
            String number = hubName.substring(hubName.length() - 8);    // "SPARE12345678" to "12345678"
            String strPattern = "^0+(?!$)";                             // Pattern to remove all leading zeros.
            HUBNumber = name + number.replaceAll(strPattern, "");   // "SPARE00000123" to "SPARE123"
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Exception occurred while getting SPARE HUB Number by Name.>>" + e.getMessage());
            HUBNumber = hubName;
        }
        return HUBNumber.trim();
    }

    public static void sharedPrefTxtnInterrupted(Context activity, String txnId, boolean isInterrupted) {

        SharedPreferences.Editor editor;
        SharedPreferences pref = activity.getSharedPreferences(Constants.PREF_TXTN_INTERRUPTED, 0);
        editor = pref.edit();

        if (isInterrupted) {
            //true for interrupted
            editor.putBoolean(txnId, isInterrupted);
        } else {
            // false txn is normal
            editor.remove(txnId);
        }

        editor.commit();
    }

    public static String CheckMacAddressFromInfoCommand(String TAG, String result, String ipAddress, String selMacAddress, String MA_ConnectedDevices) {
        String validIpAddress = "";
        try {

            String mac_address = "";
            String AP_mac_address = "";
            String iot_version = "";
            if (result.startsWith("{") && result.contains("Version")) {
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    String userData = jsonObj.getString("Version");
                    JSONObject jsonObject = new JSONObject(userData);

                    mac_address = jsonObject.getString("mac_address");

                    if (result.contains("iot_version")) {
                        iot_version = jsonObject.getString("iot_version");
                    }
                    if (result.contains("AP_mac_address")) {
                        AP_mac_address = jsonObject.getString("AP_mac_address");
                    }
                } catch (JSONException e) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Error occurred while parsing response of info command. >> " + e.getMessage());
                    e.printStackTrace();
                }
            }

            if (mac_address.equalsIgnoreCase(selMacAddress) || AP_mac_address.equalsIgnoreCase(selMacAddress)) { // compare with MAC saved in cloud.
                if (mac_address.equalsIgnoreCase(MA_ConnectedDevices)) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Mac Address found from info command.\n [STA Mac Address (from info command) => " + mac_address + " <==> Connected Device Mac Address => " + MA_ConnectedDevices + "]");
                    validIpAddress = ipAddress;
                } else {
                    String staMacAddressFromLink = "";
                    if (AP_mac_address.trim().isEmpty()) {
                        staMacAddressFromLink = mac_address;
                    } else {
                        staMacAddressFromLink = AP_mac_address;
                    }

                    String APMacAddress = generateAPMacFromSTAMac(TAG, staMacAddressFromLink);
                    if (APMacAddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Mac Address found from info command.\n [STA Mac Address (from info command) ==> " + staMacAddressFromLink + "; AP Mac Address (Generated from STA Mac) ==> " + APMacAddress + "]");
                        validIpAddress = ipAddress;
                    }
                }
            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Selected Mac Address (" + selMacAddress + ") is not found in info command response.\n [Version: " + iot_version + "; STA Mac Address (from info command) ==> " + mac_address + "; AP Mac Address (from info command) ==> " + AP_mac_address + "]");
            }

        } catch (Exception e) {
            validIpAddress = "";
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "GetAndCheckMacAddressFromInfoCommand Exception >> " + e.getMessage());
            Log.d("Ex", e.getMessage());
        }
        return validIpAddress;
    }

    public static String generateAPMacFromSTAMac(String TAG, String selMacAddress) {
        String apMacAddress = "";
        try {
            if (!selMacAddress.trim().isEmpty()) {
                String staMacAddressInitials = selMacAddress.substring(0, 2); // 8e:aa:b5:04:e2:de ==> 8e
                String staMacAddressRestPart = selMacAddress.substring(2);  // 8e:aa:b5:04:e2:de ==> :aa:b5:04:e2:de

                long value = Long.parseLong(staMacAddressInitials, 16);
                value = value - 2;
                String valueInHex = Long.toHexString(value);  // 8e ==> 8c
                if (valueInHex.length() > 1) {
                    apMacAddress = valueInHex + staMacAddressRestPart;  // 8c ==> 8c:aa:b5:04:e2:de
                } else {
                    apMacAddress = selMacAddress;
                }
            }
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "(STA Mac Address: " + selMacAddress + "; AP Mac Address: " + apMacAddress + ")");

        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Exception occurred in generateAPMacFromSTAMac: " + e.getMessage());
            Log.e(TAG, "Exception occurred in generateAPMacFromSTAMac: " + e.getMessage());
            apMacAddress = selMacAddress;
        }
        return apMacAddress;
    }

    /*public static void StoreLanguageSettings(Activity activity, String language, boolean isRecreate) {
        try {
            if (language.trim().equalsIgnoreCase("es"))
                AppConstants.LANG_PARAM = ":es-ES";
            else
                AppConstants.LANG_PARAM = ":en-US";

            Resources res = activity.getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();

            if (language.trim().equalsIgnoreCase("es")) {
                conf.setLocale(new Locale("es"));
            } else if (language.trim().equalsIgnoreCase("en")) {
                conf.setLocale(new Locale("en", "US"));
            } else {
                conf.setLocale(Locale.getDefault());
            }

            res.updateConfiguration(conf, dm);

            SharedPreferences sharedPref = activity.getSharedPreferences("LanguageSettings", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("language", language.trim());
            editor.apply();

            if (isRecreate)
                activity.recreate();
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Exception occurred in StoreLanguageSettings: " + e.getMessage());
        }
    }*/

    public static boolean CheckAllHTTPLinksAreFree() {
        boolean flag = false;
        try {
            if (!AppConstants.isHTTPTxnRunningFS1 && !AppConstants.isHTTPTxnRunningFS2 && !AppConstants.isHTTPTxnRunningFS3 && !AppConstants.isHTTPTxnRunningFS4 && !AppConstants.isHTTPTxnRunningFS5 && !AppConstants.isHTTPTxnRunningFS6) {
                flag = true;
            }
        } catch (Exception e) {
            flag = false;
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Exception occurred in CheckAnyHTTPTxnIsRunning: " + e.getMessage());
        }
        return flag;
    }

    public static void saveLinkMacAddressForReconfigure(Context activity, String jsonData) {
        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.MAC_ADDR_RECONFIGURE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("jsonData", jsonData);
        editor.commit();

    }

    public static boolean CheckDataStoredInSharedPref(Context activity, String prefName) {
        boolean isDataFound = false;
        try {
            SharedPreferences FSPref = activity.getSharedPreferences(prefName, 0);
            String jsonData = FSPref.getString("jsonData", "");
            String authString = FSPref.getString("authString", "");

            if (!jsonData.trim().isEmpty() && !authString.trim().isEmpty()) {
                isDataFound = true;
            }
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Exception occurred in CheckDataStoredInSharedPref: " + ex.getMessage());
        }
        return isDataFound;
    }

    public static void ClearOfflineData(Context context) {
        try {
            OffDBController controller = new OffDBController(context);
            controller.deleteTableData(OffDBController.TBL_LINK); // Clear Link data after registration
            controller.deleteTableData(OffDBController.TBL_VEHICLE); // Clear Vehicle data after registration
            controller.deleteTableData(OffDBController.TBL_PERSONNEL); // Clear Personnel data after registration
            controller.deleteTableData(OffDBController.TBL_DEPARTMENT); // Clear Department data after registration
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Exception occurred while deleting offline data: " + ex.getMessage());
        }
    }

    public static void GetDetailsFromARP() {
        try {
            ArrayList<HashMap<String, String>> ListOfConnectedDevices = new ArrayList<>();
            String arpTable = FS_ArpNDK.getARP();
            if (!arpTable.isEmpty()) {
                String[] lines = arpTable.split("\n");
                if (lines.length > 0) {
                    for (String line : lines) {
                        if (!line.isEmpty()) {
                            String[] splitted = line.split(" ");

                            if (splitted != null && splitted.length >= 4) {

                                String ip = splitted[0];
                                String mac = splitted[4];

                                if (ip.contains(".") && mac.contains(":")) {
                                    System.out.println("***IPAddress" + ip);
                                    System.out.println("***macAddress" + mac);

                                    try {
                                        boolean isReachable = new checkIpAddressReachable().execute(ip, "80", "2000").get();
                                        if (!isReachable) {
                                            continue;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("ipAddress", ip);
                                    map.put("macAddress", mac);

                                    ListOfConnectedDevices.add(map);
                                } else {
                                    System.out.println("###IPAddress" + ip);
                                    System.out.println("###macAddress" + mac);
                                }
                            }
                        }
                    }
                }
            }
            AppConstants.DetailsListOfConnectedDevices = ListOfConnectedDevices;
            //AppConstants.WriteinFile(TAG + "GetDetailsFromARP Connected Devices: " + ListOfConnectedDevices);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static class checkIpAddressReachable extends AsyncTask<String, Void, Boolean> {
        String address;
        int port, timeout;

        protected Boolean doInBackground(String... urls) {
            try {
                Socket mSocket = new Socket();

                try {
                    address = urls[0];
                    port = Integer.parseInt(urls[1]);
                    timeout = Integer.parseInt(urls[2]);

                    // Connects this socket to the server with a specified timeout value.
                    mSocket.connect(new InetSocketAddress(address, port), timeout);
                    // Return true if connection successful
                    System.out.println(address + " is reachable");
                    return true;
                } catch (IOException exception) {
                    exception.printStackTrace();
                    // Return false if connection fails
                    System.out.println(address + " is not reachable");
                    return false;
                } finally {
                    mSocket.close();
                }
            } catch (Exception e) {
                return false;
            }
        }

        protected void onPostExecute(String res) {
        }
    }

    public static int GetVersionNumberFromLink(String versionFromLink) {
        int versionNum = 0;
        try {
            String version = versionFromLink.replaceAll("[^0-9]", "");
            versionNum = Integer.parseInt(version);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return versionNum;
    }

    public static void hideKeyboard(Activity activity) {
        try {
            View view = activity.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            } else {
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "hideKeyboard: Exception: " + ex.getMessage());
        }

    }
}