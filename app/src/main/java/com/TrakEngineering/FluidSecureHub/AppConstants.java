package com.TrakEngineering.FluidSecureHub;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;

import androidx.core.app.NotificationCompat;
import androidx.appcompat.app.AlertDialog;

import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import static com.TrakEngineering.FluidSecureHub.CommonUtils.GetDateString;

/**
 * Created by Administrator on 5/19/2016.
 */
public class AppConstants {

    public static String AddVehicle = "", AddPin = "",AddFobKey = "", AddMagCard_FobKey = "", AddBarcode_val = ""; //Additional value

    public static boolean selectHosePressed;
    public static ArrayList<String> offlineDownloadIds = new ArrayList<>();
    public static int CONNECTION_TIMEOUT_SEC = 2;
    public static int READ_TIMEOUT_SEC = 2;
    public static int WRITE_TIMEOUT_SEC = 2;

    public static int ScreenResolutionYOffSet = 0;
    public static int COUNT_HOTSPOT_MAIL;
    public static String sharedPref_AzureQueueDetails = "AzureQueueDetails";
    public static String sharedPref_KeyboardType = "KeyboardType";
    public static String sharedPref_HotSpotEmail = "HotSpotEmail";
    public static String sharedPref_OfflineAzureSync = "OfflineAzureSync";
    public static String IsFirstTimeUse = "false";
    public static boolean enableHotspotManuallyWindow = false;
    public static boolean busyWithHotspotToggle = false;
    public static boolean showReaderStatus = false;
    public static boolean IsBTLinkSelectedCurrently = false;
    public static boolean IsProblemWhileEnableHotspot = false;

    public static final String DEVICE_TYPE = "A";
    public static final String USER_NAME = "userName";
    public static final String USER_MOBILE = "userMobile";
    public static final String USER_EMAIL = "userEmail";
    public static final String IsOdoMeterRequire = "IsOdoMeterRequire";
    public static final String IsDepartmentRequire = "IsDepartmentRequire";
    public static final String IsPersonnelPINRequire = "IsPersonnelPINRequire";
    public static final String IsPersonnelPINRequireForHub = "IsPersonnelPINRequireForHub";
    public static final String FluidSecureSiteName = "FluidSecureSiteName";
    public static final String ISVehicleHasFob = "ISVehicleHasFob";
    public static final String IsPersonHasFob = "IsPersonHasFob";
    public static final String IsPersonPinAndFOBRequire = "IsPersonPinAndFOBRequire";
    public static final String AllowAccessDeviceORManualEntry = "AllowAccessDeviceORManualEntry";
    public static final String AllowAccessDeviceORManualEntryForVehicle = "AllowAccessDeviceORManualEntryForVehicle";
    public static final String IsOtherRequire = "IsOtherRequire";
    public static final String IsHoursRequire = "IsHoursRequire";
    public static final String ExtraOtherLabel = "ExtraOtherLabel";
    public static final String IsExtraOther = "IsExtraOther";
    public static final String OtherLabel = "OtherLabel";
    public static final String TimeOut = "TimeOut";
    public static final String HubId = "HubId";
    public static final String HubType = "HubType";
    public static final String IsNonValidateVehicle = "IsNonValidateVehicle";
    public static final String IsNonValidatePerson = "IsNonValidatePerson";
    public static final String IsVehicleNumberRequire = "IsVehicleNumberRequire";
    public static final String WifiChannelToUse = "0";

    public static boolean isRelayON_fs1 = false;
    public static boolean isRelayON_fs2 = false;
    public static boolean isRelayON_fs3 = false;
    public static boolean isRelayON_fs4 = false;
    public static boolean isRelayON_fs5 = false;
    public static boolean isRelayON_fs6 = false;

    public static final String LinkConnectionIssuePref = "LinkConnectionIssuePref";

    public static final String IsGateHub = "IsGateHub";
    public static final String IsStayOpenGate = "IsStayOpenGate";

    //public static String FOLDER_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FSBin/";
    public static String FOLDER_BIN = "FSBin";
    public static String UP_Upgrade_File_name = "user1.2048.new.5.bin";

    public static String OfflineDataFolderName = "FSdata";

    public static final String OfflineDataBaseSize = "OfflineDataBaseSize";
    public static final String DbUpdateTime = "DbUpdateTime";

    public static final String IsEnableServerForTLD = "IsEnableServerForTLD";
    public static final String FAData = "FAData";
    public static final String UseBarcode = "UseBarcode";
    public static final String UseBarcodeForPersonnel = "UseBarcodeForPersonnel";
    public static final String CAMERA_FACING = "CAMERA_FACING";

    public static final String LogRequiredFlag = "LogRequiredFlag";
    public static final String CompanyBrandName = "CompanyBrandName";
    public static final String CompanyBrandLogoLink = "CompanyBrandLogoLink";
    public static final String SupportEmail = "SupportEmail";
    public static final String SupportPhonenumber = "SupportPhonenumber";
    public static String BrandName = "FluidSecure";
    public static String LANG_PARAM;

    public static final String PACKAGE_BACKGROUND_SERVICE = "com.TrakEngineering.FluidSecureHub.BackgroundService";

    public static ArrayList<HashMap<String, String>> temp_serverSSIDList;

   // public static String webIP = "http://sierravistatest.cloudapp.net/";
    public static String webIP = "https://www.fluidsecure.net/";


    public static String webURL = webIP + "HandlerTrak.ashx";
    public static String LoginURL = webIP + "LoginHandler.ashx";

    public static String API_URL_TOKEN = webIP + "token";
    public static String API_URL_HUB = webIP + "api/Offline/GetHub";
    public static String API_URL_GENERATEFILES = webIP + "api/Offline/GenerateFiles";
    public static String API_URL_LINK = webIP + "api/Offline/GetLinks";
    public static String API_URL_VEHICLE = webIP + "api/Offline/GetVehicles";
    public static String API_URL_PERSONNEL = webIP + "api/Offline/GetPersonnel";
    public static String API_URL_SYNC_TRANS = webIP + "api/Offline/OFFLineImportTransactions";


    public static String OFF_VEHICLE_ID;
    public static String OFF_PERSON_PIN;
    public static String OFF_ODO_REQUIRED;
    public static String OFF_HOUR_REQUIRED;
    public static String OFF_CURRENT_ODO;
    public static String OFF_CURRENT_HOUR;
    public static String OFF_ODO_Reasonable;
    public static String OFF_ODO_Conditions;
    public static String OFF_ODO_Limit;
    public static String OFF_HRS_Limit;


    public static String ESP32_update = "NO";
    public static String PIC_update = "NO";
    public static boolean GenerateLogs;
    public static boolean ServerCallLogs;
    public static boolean EnableFA;
    public static boolean EnableServerForTLD;
    public static boolean RebootHF_reader = false;
    public static boolean RebootQR_reader = false;

    public static String OFF1 = "Please check your Internet Data";
    public static String error_msg = "Something went wrong. Please try again.";

    public static boolean excption_caught = false;
    public static boolean NETWORK_STRENGTH;
    public static boolean IS_MOBILE_ON;
    public static boolean IS_MOBILE_MSG;
    public static boolean PRE_STATE_MOBILEDATA;
    public static boolean CURRENT_STATE_MOBILEDATA;
    public static boolean AUTH_CALL_SUCCESS = false;
    public static boolean serverCallInProgress;

    public static String LOG_FluidSecure_Auto = "";
    public static String Server_mesage = "Server Not Connected..!!!";
    public static String Server_Request;
    public static String Server_Response;
    public static String Header_data;
    public static String OdoErrorCode = "0";

    public static boolean RefreshSingleHose = false;
    public static boolean DisplayToastmaxlimit = false;
    public static String MaxlimitMessage = "";
    public static boolean IsSingleLink = false;
    public static boolean goButtonClicked = false;

    public static boolean FlickeringScreenOff;
    public static String NoSleepRespTime = "";
    public static String NoSleepCurrentTime = "";

    public static String APDU_FOB_KEY = "";
    public static String NonValidateVehicle_FOB_KEY = "";
    public static String VehicleLocal_FOB_KEY = "";
    public static String PinLocal_FOB_KEY = "";
    public static String FS_selected;
    public static String LastSelectedHose;
    public static String BLUETOOTH_PRINTER_NAME;
    public static String PrinterMacAddress;
    public static String BT_READER_NAME;
    public static String PulserTimingAdjust;

    public static String UP_FirmwareVersion;
    public static String UP_FilePath;
    public static boolean UP_Upgrade;
    public static boolean UP_Upgrade_fs1;
    public static boolean UP_Upgrade_fs2;
    public static boolean UP_Upgrade_fs3;
    public static boolean UP_Upgrade_fs4;
    public static boolean UP_Upgrade_fs5;
    public static boolean UP_Upgrade_fs6;


    public static String UP_HoseId_fs1;
    public static String UP_HoseId_fs2;
    public static String UP_HoseId_fs3;
    public static String UP_HoseId_fs4;
    public static String UP_HoseId_fs5;
    public static String UP_HoseId_fs6;


    public static String UP_SiteId_fs1;
    public static String UP_SiteId_fs2;
    public static String UP_SiteId_fs3;
    public static String UP_SiteId_fs4;
    public static String UP_SiteId_fs5;
    public static String UP_SiteId_fs6;


    public static String Title = "";
    public static String SiteName = "";
    public static String HubName;
    public static String HubGeneratedpassword;
    public static String Login_Email;
    public static String Login_IMEI;
    public static String AccessCode = "2901";
    public static String DisableAllRebootOptions = "";

    public static String RES_MESSAGE = "ResponceMessage";
    public static String RES_DATA = "ResponceData";
    public static String RES_DATA_SSID = "SSIDDataObj";
    public static String RES_DATA_USER = "objUserData";
    public static String RES_TANK_DATA = "tanksForLinksObj";
    public static String RES_TEXT = "ResponceText";
    public static String VALIDATION_FOR_TEXT = "ValidationFailFor";

    public static String FOB_KEY_PERSON = "";
    public static String FOB_KEY_VEHICLE = "";
    public static String HUB_ID = "";


    public static String FS1_CONNECTED_SSID;
    public static String FS2_CONNECTED_SSID;
    public static String FS3_CONNECTED_SSID;
    public static String FS4_CONNECTED_SSID;
    public static String FS5_CONNECTED_SSID;
    public static String FS6_CONNECTED_SSID;

    public static String REPLACEBLE_WIFI_NAME_FS_ON_UPDATE_MAC;
    public static String REPLACEBLE_WIFI_NAME_FS1;
    public static String REPLACEBLE_WIFI_NAME_FS2;
    public static String REPLACEBLE_WIFI_NAME_FS3;
    public static String REPLACEBLE_WIFI_NAME_FS4;
    public static String REPLACEBLE_WIFI_NAME_FS5;
    public static String REPLACEBLE_WIFI_NAME_FS6;

    public static boolean NeedToRenameFS_ON_UPDATE_MAC;
    public static boolean NeedToRenameFS1;
    public static boolean NeedToRenameFS2;
    public static boolean NeedToRenameFS3;
    public static boolean NeedToRenameFS4;
    public static boolean NeedToRenameFS5;
    public static boolean NeedToRenameFS6;

    public static String DownloadFileHttpServer = "";
    public static boolean ManuallReconfigure;
    public static String SELECTED_SSID_FOR_MANUALL;

    public static String REPLACEBLE_WIFI_NAME;
    public static String LAST_CONNECTED_SSID;
    public static String SELECTED_MACADDRESS;
    public static String CURRENT_SELECTED_SSID;
    public static String CURRENT_SELECTED_SSID_ReqTLDCall;
    public static String CURRENT_HOSE_SSID;
    public static String CURRENT_SELECTED_SITEID;
    public static String UPDATE_MACADDRESS;
    public static String R_HOSE_ID;
    public static String R_SITE_ID;
    public static String SITE_ID;


    public static String WIFI_PASSWORD = "";

    public static boolean NeedToRename;
    public static boolean BUSY_STATUS;

    public static boolean ACS_READER;
    public static boolean IS_WIFI_ON;
    public static boolean IS_DATA_ON;
    public static boolean IS_HOTSPOT_ON;

    public static ArrayList<HashMap<String, String>> DetailsServerSSIDList;
    public static ArrayList<HashMap<String, String>> DetailsListOfConnectedDevices;
    public static ArrayList<HashMap<String, String>> test;

    public static ArrayList<String> ListOfRunningTransactiins = new ArrayList<>();

    public static double roundNumber(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


    public static String convertStingToBase64(String text) {
        String base64 = "";
        try {
            byte[] data = text.getBytes("UTF-8");
            base64 = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (Exception e) {
            System.out.println(e);
        }

        base64 = base64.replaceAll("\\n", "");

        return base64;
    }

    public static String getIMEI(Context ctx) {

        String storedUUIDIMEI = "";
        try {

            String encryptedIMEI = SplashActivity.readIMEIMobileNumFromFile(ctx).trim();
            storedUUIDIMEI = AES.decrypt(encryptedIMEI, AES.credential);
            if (storedUUIDIMEI == null || storedUUIDIMEI.isEmpty() || storedUUIDIMEI.equalsIgnoreCase("null")) {
                storedUUIDIMEI = "";
            }

        } catch (Exception e) {

        }

        return storedUUIDIMEI;
    }

    public static void disconnectWiFi(Context ctx) {


        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);

        if (wifiManager.isWifiEnabled()) {

            //wifiManager.disconnect();

            wifiManager.setWifiEnabled(false);
        }

    }


    public static void dontConnectWiFi(Context ctx) {


        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);

        if (wifiManager.isWifiEnabled()) {

            wifiManager.disconnect();

            wifiManager.setWifiEnabled(true);

        }

    }

    public static void forgetWiFi(Context ctx) {
        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {

            int nwID = i.networkId;
            String ssID = i.SSID;
            ssID = ssID.replace("\"", "");
            System.out.println("sssss--" + nwID);
            System.out.println("sssss--" + ssID);

            if (AppConstants.LAST_CONNECTED_SSID.equalsIgnoreCase(ssID)) {
                wifiManager.removeNetwork(nwID);
                wifiManager.saveConfiguration();

            }
        }
    }

    public static String getConnectedWifiName(Context context) {
        String name = "";
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        name = wifiInfo.getSSID();

        System.out.println("connected ssid--" + name);

        return name;
    }


    public static void AlertDialogBox(final Context ctx, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
        alertDialogBuilder.setMessage(message);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();


                    }
                }


        );

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
        textView.setTextSize(35);
    }

    public static void AlertDialogBoxCanecl(final Context ctx, int message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
        alertDialogBuilder.setMessage(message);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();


                    }
                }


        );

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
        textView.setTextSize(35);
    }

    public static void alertBigFinishActivity(final Activity ctx, String msg) {
        Dialog dialogObj;
        dialogObj = new Dialog(ctx);
        dialogObj.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogObj.setContentView(R.layout.dialog_alert_big_finish);
        dialogObj.setCancelable(false);

        TextView tvAlertMsg = (TextView) dialogObj.findViewById(R.id.tvAlertMsg);
        Button btnDialogOk = (Button) dialogObj.findViewById(R.id.btnDailogOk);


        tvAlertMsg.setText(msg);

        btnDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ctx.finish();
            }
        });


        dialogObj.show();
    }

    public static void alertBigActivity(final Activity ctx, String msg) {
        final Dialog dialogObj;
        dialogObj = new Dialog(ctx);
        dialogObj.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogObj.setContentView(R.layout.dialog_alert_big_finish);
        dialogObj.setCancelable(false);

        Window window = dialogObj.getWindow();

        WindowManager.LayoutParams param = window.getAttributes();
        param.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        param.y = 280;
        window.setAttributes(param);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);


        TextView tvAlertMsg = (TextView) dialogObj.findViewById(R.id.tvAlertMsg);
        Button btnDialogOk = (Button) dialogObj.findViewById(R.id.btnDailogOk);


        tvAlertMsg.setText(msg);

        btnDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogObj.dismiss();
            }
        });


        dialogObj.show();
    }


    public static void AlertDialogFinish(final Activity ctx, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(true);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                        ctx.finish();

                    }
                }

        );

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    public static void AlertDialogFinishWithTitle(final Activity ctx, String title, String message
    ) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(true);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                        ctx.finish();

                    }
                }

        );

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static void colorToast(Context ctx, String msg, int colr) {
        Toast toast = Toast.makeText(ctx, " " + msg + " ", Toast.LENGTH_LONG);
        toast.getView().setBackgroundColor(colr);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

    }


    public static void colorToastBigFont(Context ctx, String msg, int colr) {
        Toast toast = Toast.makeText(ctx, " " + msg + " ", Toast.LENGTH_LONG);
        toast.getView().setBackgroundColor(colr);
        toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 280);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setTextSize(25);
        toast.show();

    }

    public static void colorToastHotspotOn(Context ctx, String msg, int backColor, int textColor) {

        Toast toast = Toast.makeText(ctx, " " + msg + " ", Toast.LENGTH_SHORT);
        toast.getView().setBackgroundColor(backColor);
        toast.setGravity(Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK | Gravity.TOP, 0, AppConstants.ScreenResolutionYOffSet);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setShadowLayer(1, 1, 1, backColor);
        messageTextView.setTextColor(textColor);
        messageTextView.setTextSize(40);
        toast.show();
    }


    public static int GetYOffsetFromScreenResolution(final Activity ctx) {

        try {

            Rect r = new Rect();
            ctx.getWindow().getDecorView().getRootView().getWindowVisibleDisplayFrame(r);
            int screenHeight = r.bottom - r.top;
            return screenHeight / 6;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    public static void notificationAlert(Context context) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String message = "Successfully completed Transaction.";
        String title = "FluidSecure";
        int icon = R.mipmap.ic_launcher;
        long when = System.currentTimeMillis();
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), icon);

        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(icon)
                .setLargeIcon(largeIcon)
                .setWhen(when)
                .setAutoCancel(true)
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notificationManager.notify(0, notification);


    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }

    /*public static String getConnectedWiFidsdsdsd(Context ctx) {
        String wifiname = "";

        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo info = wifiManager.getConnectionInfo();
            String ssid = info.getSSID();

            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }

            wifiname = ssid;
        }

        return wifiname;
    }*/

    public static void WriteTimeStamp(String str) {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/FSTimeStamp");

            if (!file.exists()) {
                if (file.mkdirs()) {
                    //System.out.println("Create FSLog Folder");
                } else {
                    // System.out.println("Fail to create KavachLog folder");
                }
            }

            String dt = GetDateString(System.currentTimeMillis());
            File gpxfile = new File(file + "/CurrentTimeStamp.txt");
            if (!gpxfile.exists()) {
                gpxfile.createNewFile();
            }


            FileWriter fileWritter = new FileWriter(gpxfile, false);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(Calendar.getInstance().getTime() + "--" + str + " ");
            bufferWritter.close();

        } catch (IOException e) {
            WriteinFile("WriteTimeStamp Exception" + e);

        }
    }

    public static void WriteinFile(String str) {
        try {

            if (str.contains("Responce"))
                str = str.replace("Responce", "Response");

            System.out.println(str);

            File file = new File(Environment.getExternalStorageDirectory() + "/FSLog");

            if (!file.exists()) {
                if (file.mkdirs()) {
                    //System.out.println("Create FSLog Folder");
                } else {
                    // System.out.println("Fail to create KavachLog folder");
                }
            }

            String dt = GetDateString(System.currentTimeMillis());
            File gpxfile = new File(file + "/Log_" + dt + ".txt");
            if (!gpxfile.exists()) {
                gpxfile.createNewFile();
            }

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss");
            String UseDate = dateFormat.format(cal.getTime());

            FileWriter fileWritter = new FileWriter(gpxfile, true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write("\n" + UseDate + "--" + str + " ");
            bufferWritter.close();

        } catch (IOException e) {
            WriteinFile("WriteinFile Exception" + e);

        }
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
                if (aFileList.isDirectory()) {
                    dirlist.push(aFileList);
                } else {
                    //files.add(aFileList);
                    if (aFileList.exists()) {
                        Calendar time = Calendar.getInstance();
                        time.add(Calendar.DAY_OF_YEAR, -30);
                        //I store the required attributes here and delete them
                        Date lastModified = new Date(aFileList.lastModified());
                        if (lastModified.before(time.getTime())) {
                            //file is older than a week
                            aFileList.delete();
                        }

                    } else {
                        files.add(aFileList);
                    }


                }

            }
        }

        return files;
    }

    public static void startWelcomeActivity(Context ctx) {
        Intent i = new Intent(ctx, WelcomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ctx.startActivity(i);
    }


    public static void ClearEdittextFielsOnBack(Context ctx) {

        if (Constants.CurrentSelectedHose.equals("FS1")) {
            Constants.AccVehicleNumber_FS1 = "";
            Constants.AccOdoMeter_FS1 = 0;
            Constants.AccDepartmentNumber_FS1 = "";
            Constants.AccPersonnelPIN_FS1 = "";
            Constants.AccOther_FS1 = "";
            Constants.AccVehicleOther_FS1 = "";
            Constants.AccHours_FS1 = 0;

        } else if (Constants.CurrentSelectedHose.equals("FS2")) {

            Constants.AccVehicleNumber = "";
            Constants.AccOdoMeter = 0;
            Constants.AccDepartmentNumber = "";
            Constants.AccPersonnelPIN = "";
            Constants.AccOther = "";
            Constants.AccVehicleOther = "";
            Constants.AccHours = 0;

        } else if (Constants.CurrentSelectedHose.equals("FS3")) {

            Constants.AccVehicleNumber_FS3 = "";
            Constants.AccOdoMeter_FS3 = 0;
            Constants.AccDepartmentNumber_FS3 = "";
            Constants.AccPersonnelPIN_FS3 = "";
            Constants.AccVehicleOther_FS3 = "";
            Constants.AccHours_FS3 = 0;

        } else {

            Constants.AccVehicleNumber_FS4 = "";
            Constants.AccOdoMeter_FS4 = 0;
            Constants.AccDepartmentNumber_FS4 = "";
            Constants.AccPersonnelPIN_FS4 = "";
            Constants.AccOther_FS4 = "";
            Constants.AccVehicleOther_FS4 = "";
            Constants.AccHours_FS4 = 0;

        }
    }

    public static void showHideActivityBySharedPref(Activity actctx) {
        SharedPreferences sharedPrefODO = actctx.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        String IsHoursRequire = sharedPrefODO.getString(AppConstants.IsHoursRequire, "");
        String IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        String IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");

        if (IsPersonnelPINRequire.equalsIgnoreCase("True")) {
            if (actctx instanceof AcceptPinActivity) {

                AcceptServiceCall asc = new AcceptServiceCall();
                asc.activity = actctx;
                asc.checkAllFields();
            } else {
                Intent intent = new Intent(actctx, AcceptPinActivity.class);
                actctx.startActivity(intent);
            }
        } else if (IsHoursRequire.equalsIgnoreCase("True")) {
            if (actctx instanceof AcceptHoursAcitvity) {
                AcceptServiceCall asc = new AcceptServiceCall();
                asc.activity = actctx;
                asc.checkAllFields();
            } else {
                Intent intent = new Intent(actctx, AcceptHoursAcitvity.class);
                actctx.startActivity(intent);
            }
        } else if (IsDepartmentRequire.equalsIgnoreCase("True")) {

            if (actctx instanceof AcceptDeptActivity) {
                AcceptServiceCall asc = new AcceptServiceCall();
                asc.activity = actctx;
                asc.checkAllFields();
            } else {
                Intent intent = new Intent(actctx, AcceptDeptActivity.class);
                actctx.startActivity(intent);
            }
        } else if (IsOtherRequire.equalsIgnoreCase("True")) {
            if (actctx instanceof AcceptOtherActivity) {
                AcceptServiceCall asc = new AcceptServiceCall();
                asc.activity = actctx;
                asc.checkAllFields();
            } else {
                Intent intent = new Intent(actctx, AcceptOtherActivity.class);
                actctx.startActivity(intent);
            }
        } else {

            AcceptServiceCall asc = new AcceptServiceCall();
            asc.activity = actctx;
            asc.checkAllFields();
        }

    }

    public static void clearSharedPrefByName(Context ctx, String spName) {
        SharedPreferences preferences = ctx.getSharedPreferences(spName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    public static String currentDateFormat(String formatpattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatpattern);
        String dateString = sdf.format(new Date());
        return dateString;
    }

    public static String getIMEIOnlyForBelowOS10(Context ctx) {

        String storedIMEI = "";
        try {

            TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            storedIMEI = telephonyManager.getDeviceId();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return storedIMEI;
    }

}
