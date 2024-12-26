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
import android.os.CountDownTimer;
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

    public static String ADD_VEHICLE = "", ADD_PIN = "", ADD_FOB_KEY = "", ADD_MAG_CARD_FOB_KEY = "", ADD_BARCODE_VAL = ""; //Additional value

    public static boolean SELECT_HOSE_PRESSED;
    public static ArrayList<String> OFFLINE_DOWNLOAD_IDS = new ArrayList<>();
    public static int CONNECTION_TIMEOUT_SEC = 4; //2 (refer #1935)
    public static int READ_TIMEOUT_SEC = 4; //2
    public static int WRITE_TIMEOUT_SEC = 4; //2

    public static final String LOG_TXTN_BT = "[TXTN-BT]";
    public static final String LOG_TXTN_HTTP = "[TXTN-HTTP]";
    public static final String LOG_MAINTAIN = "[MAINTAIN]";
    public static final String LOG_BACKGROUND = "[BACKGROUND]";
    public static final String LOG_RECONFIG = "[RECONFIG]";
    public static final String LOG_UPGRADE_BT = "[UPGRADE-BT]";
    public static final String LOG_UPGRADE_BT_BLE = "[UPGRADE-BT_BLE]";
    public static final String LOG_UPGRADE_HTTP = "[UPGRADE-HTTP]";

    public static int SCREEN_RESOLUTION_Y_OFFSET = 0;
    public static int COUNT_HOTSPOT_MAIL;
    public static String PREF_AZURE_QUEUE_DETAILS = "AzureQueueDetails";
    public static String PREF_KEYBOARD_TYPE = "KeyboardType";
    public static String PREF_HOTSPOT_EMAIL = "HotSpotEmail";
    public static String PREF_OFFLINE_AZURE_SYNC = "OfflineAzureSync";
    public static String PREF_AZURE_MAP_DETAILS = "AzureMapDetails";
    public static String IS_FIRST_TIME_USE = "false";
    public static boolean ENABLE_HOTSPOT_MANUALLY_WINDOW = false;
    public static boolean busyWithHotspotToggle = false;
    public static boolean SHOW_READER_STATUS = false;
    public static boolean IS_BT_LINK_SELECTED_CURRENTLY = false;
    public static boolean IS_PROBLEM_WHILE_ENABLE_HOTSPOT = false;

    public static final String DEVICE_TYPE = "A";
    public static final String USER_NAME = "userName";
    public static final String USER_MOBILE = "userMobile";
    public static final String USER_EMAIL = "userEmail";
    public static final String IS_ODO_METER_REQUIRE = "IsOdoMeterRequire";
    public static final String IS_DEPARTMENT_REQUIRE = "IsDepartmentRequire";
    public static final String IS_PERSONNEL_PIN_REQUIRE = "IsPersonnelPINRequire";
    public static final String IS_PERSONNEL_PIN_REQUIRE_FOR_HUB = "IsPersonnelPINRequireForHub";
    public static final String FS_SITE_NAME = "FluidSecureSiteName";
    public static final String IS_VEHICLE_HAS_FOB = "ISVehicleHasFob";
    public static final String IS_PERSON_HAS_FOB = "IsPersonHasFob";
    public static final String IS_PERSON_PIN_AND_FOB_REQUIRE = "IsPersonPinAndFOBRequire";
    public static final String ALLOW_ACCESS_DEVICE_OR_MANUAL_ENTRY = "AllowAccessDeviceORManualEntry";
    public static final String ALLOW_ACCESS_DEVICE_OR_MANUAL_ENTRY_FOR_VEHICLE = "AllowAccessDeviceORManualEntryForVehicle";
    public static final String IS_OTHER_REQUIRE = "IsOtherRequire";
    public static final String IS_HOURS_REQUIRE = "IsHoursRequire";
    public static final String EXTRA_OTHER_LABEL = "ExtraOtherLabel";
    public static final String IS_EXTRA_OTHER = "IsExtraOther";
    public static final String OTHER_LABEL = "OtherLabel";
    public static final String TIMEOUT = "TimeOut";
    public static final String HUBID = "HubId";
    public static final String HUB_TYPE = "HubType";
    public static final String IS_NON_VALIDATE_VEHICLE = "IsNonValidateVehicle";
    public static final String IS_NON_VALIDATE_PERSON = "IsNonValidatePerson";
    public static final String IS_VEHICLE_NUMBER_REQUIRE = "IsVehicleNumberRequire";
    public static final String WIFI_CHANNEL_TO_USE = "0";

    public static boolean IS_RELAY_ON_FS1 = false;
    public static boolean IS_RELAY_ON_FS2 = false;
    public static boolean IS_RELAY_ON_FS3 = false;
    public static boolean IS_RELAY_ON_FS4 = false;
    public static boolean IS_RELAY_ON_FS5 = false;
    public static boolean IS_RELAY_ON_FS6 = false;

    public static boolean IS_FIRST_COMMAND_SUCCESS_FS1 = false;
    public static boolean IS_FIRST_COMMAND_SUCCESS_FS2 = false;
    public static boolean IS_FIRST_COMMAND_SUCCESS_FS3 = false;
    public static boolean IS_FIRST_COMMAND_SUCCESS_FS4 = false;
    public static boolean IS_FIRST_COMMAND_SUCCESS_FS5 = false;
    public static boolean IS_FIRST_COMMAND_SUCCESS_FS6 = false;

    public static final String PREF_LINK_CONNECTION_ISSUE = "LinkConnectionIssuePref";

    public static String HS_CONNECTION_TIMEOUT = "";

    public static final String IS_GATE_HUB = "IsGateHub";
    public static final String IS_STAY_OPEN_GATE = "IsStayOpenGate";

    //public static String FOLDER_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FSBin/";
    public static String FOLDER_BIN = "FSBin";
    public static String UP_UPGRADE_FILE_NAME = "user1.2048.new.5.bin";

    public static String OFFLINE_DATA_FOLDER_NAME = "FSdata";

    public static final String OFFLINE_DATABASE_SIZE = "OfflineDataBaseSize";
    public static final String DB_UPDATE_TIME = "DbUpdateTime";

    public static final String IS_ENABLE_SERVER_FOR_TLD = "IsEnableServerForTLD";
    public static final String FA_DATA = "FAData";
    public static final String USE_BARCODE = "UseBarcode";
    public static final String USE_BARCODE_FOR_PERSONNEL = "UseBarcodeForPersonnel";
    public static final String CAMERA_FACING = "CAMERA_FACING";

    public static final String LOG_REQUIRED_FLAG = "LogRequiredFlag";
    public static final String COMPANY_BRAND_NAME = "CompanyBrandName";
    public static final String COMPANY_BRAND_LOGO_LINK = "CompanyBrandLogoLink";
    public static final String SUPPORT_EMAIL = "SupportEmail";
    public static final String SUPPORT_PHONE_NUMBER = "SupportPhonenumber";
    public static String BRAND_NAME = "FluidSecure";
    public static String LANG_PARAM = "";

    public static final String PACKAGE_BACKGROUND_SERVICE = BuildConfig.APPLICATION_ID + ".BackgroundService";
    public static final String PACKAGE_BS_OffTransSync = BuildConfig.APPLICATION_ID + ".offline.OffTranzSyncService";
    public static final String PACKAGE_BS_OffDataDownload = BuildConfig.APPLICATION_ID + ".offline.OffBackgroundService";

    public static ArrayList<HashMap<String, String>> TEMP_SERVER_SSID_LIST;

    //public static String SERVER_BASE_URL = "http://fluidsecuretest.eastus.cloudapp.azure.com/"; // OLD URL => http://sierravistatest.cloudapp.net/";
    public static String SERVER_BASE_URL = "https://www.fluidsecure.net/";


    public static String WEB_URL = SERVER_BASE_URL + "HandlerTrak.ashx";
    public static String LOGIN_URL = SERVER_BASE_URL + "LoginHandler.ashx";

    public static String API_URL_TOKEN = SERVER_BASE_URL + "token";
    public static String API_URL_HUB = SERVER_BASE_URL + "api/Offline/GetHub";
    public static String API_URL_GENERATEFILES = SERVER_BASE_URL + "api/Offline/GenerateFiles";
    public static String API_URL_LINK = SERVER_BASE_URL + "api/Offline/GetLinks";
    public static String API_URL_VEHICLE = SERVER_BASE_URL + "api/Offline/GetVehicles";
    public static String API_URL_PERSONNEL = SERVER_BASE_URL + "api/Offline/GetPersonnel";
    public static String API_URL_SYNC_TRANS = SERVER_BASE_URL + "api/Offline/OFFLineImportTransactions";
    public static String API_URL_DEPT = SERVER_BASE_URL + "api/Offline/GetDepartments";

    public static String BASE_URL_AZURE_MAP = "https://atlas.microsoft.com/";

    public static String OFF_VEHICLE_ID;
    public static String OFF_PERSON_PIN;
    public static String OFF_ODO_REQUIRED;
    public static String OFF_HOUR_REQUIRED;
    public static String OFF_CURRENT_ODO;
    public static String OFF_CURRENT_HOUR;
    public static String OFF_ODO_REASONABLE;
    public static String OFF_ODO_CONDITIONS;
    public static String OFF_ODO_LIMIT;
    public static String OFF_HRS_LIMIT;

    public static String ESP32_update = "NO";
    public static String PIC_update = "NO";
    public static boolean GENERATE_LOGS;
    public static boolean SERVER_CALL_LOGS;
    public static boolean ENABLE_FA;
    public static boolean Enable_Server_For_TLD;
    public static boolean REBOOT_HF_READER = false;
    public static boolean REBOOT_QR_READER = false;

    public static String OFF1 = "Please check your Internet Data";
    public static String error_msg = "Something went wrong. Please try again.";

    public static boolean EXCEPTION_CAUGHT = false;
    public static boolean NETWORK_STRENGTH;
    public static boolean IS_MOBILE_ON;
    public static boolean IS_MOBILE_MSG;
    public static boolean PRE_STATE_MOBILE_DATA;
    public static boolean CURRENT_STATE_MOBILE_DATA;
    public static boolean AUTH_CALL_SUCCESS = false;
    public static boolean SERVER_CALL_IN_PROGRESS;
    public static boolean SERVER_CALL_IN_PROGRESS_FOR_PIN = false;
    public static boolean SERVER_CALL_IN_PROGRESS_FOR_VEHICLE = false;
    public static boolean SERVER_AUTH_CALL_COMPLETED = false;

    public static boolean SHOW_WELCOME_DIALOG_FOR_ADD_NEW_LINK = true;
    public static ArrayList<HashMap<String, String>> NEWLY_ADDED_LINKS = new ArrayList<>();
    public static String NEWLY_ADDED_SITE_ID = "0";
    //public static boolean enableHotspotAfterNewLinkConfigure = false;

    public static String LOG_FluidSecure_Auto = "";
    public static String SERVER_MESSAGE = "Server Not Connected..!!!";
    public static String SERVER_REQUEST;
    public static String SERVER_RESPONSE;
    public static String HEADER_DATA;
    public static String ODO_ERROR_CODE = "0";

    public static boolean REFRESH_SINGLE_HOSE = false;
    public static boolean DISPLAY_TOAST_MAX_LIMIT = false;
    public static String MAX_LIMIT_MESSAGE = "";
    public static boolean IS_SINGLE_LINK = false;
    public static boolean GO_BUTTON_ALREADY_CLICKED = false;

    public static boolean IS_TRANSACTION_COMPLETED_1 = true;
    public static boolean IS_TRANSACTION_COMPLETED_2 = true;
    public static boolean IS_TRANSACTION_COMPLETED_3 = true;
    public static boolean IS_TRANSACTION_COMPLETED_4 = true;
    public static boolean IS_TRANSACTION_COMPLETED_5 = true;
    public static boolean IS_TRANSACTION_COMPLETED_6 = true;

    public static boolean IS_TRANSACTION_FAILED_1 = false;
    public static boolean IS_TRANSACTION_FAILED_2 = false;
    public static boolean IS_TRANSACTION_FAILED_3 = false;
    public static boolean IS_TRANSACTION_FAILED_4 = false;
    public static boolean IS_TRANSACTION_FAILED_5 = false;
    public static boolean IS_TRANSACTION_FAILED_6 = false;

    public static int TRANSACTION_FAILED_COUNT_1 = 0;
    public static int TRANSACTION_FAILED_COUNT_2 = 0;
    public static int TRANSACTION_FAILED_COUNT_3 = 0;
    public static int TRANSACTION_FAILED_COUNT_4 = 0;
    public static int TRANSACTION_FAILED_COUNT_5 = 0;
    public static int TRANSACTION_FAILED_COUNT_6 = 0;

    public static boolean FLICKERING_SCREEN_OFF;
    public static String NO_SLEEP_RESP_TIME = "";
    public static String NO_SLEEP_CURRENT_TIME = "";

    public static String APDU_FOB_KEY = "";
    public static String NonValidateVehicle_FOB_KEY = "";
    public static String VehicleLocal_FOB_KEY = "";
    public static String PinLocal_FOB_KEY = "";
    public static String FS_SELECTED;
    public static String LastSelectedHose;
    public static String BLUETOOTH_PRINTER_NAME;
    public static String PRINTER_MAC_ADDRESS;
    public static String BT_READER_NAME;
    //public static String PulserTimingAdjust;
    //public static String IsResetSwitchTimeBounce;

    public static String UP_FIRMWARE_VERSION;
    public static String UP_FILE_PATH;
    public static boolean UP_UPGRADE;
    public static boolean UP_UPGRADE_FS1;
    public static boolean UP_UPGRADE_FS2;
    public static boolean UP_UPGRADE_FS3;
    public static boolean UP_UPGRADE_FS4;
    public static boolean UP_UPGRADE_FS5;
    public static boolean UP_UPGRADE_FS6;

    public static String UP_HOSE_ID_FS1;
    public static String UP_HOSE_ID_FS2;
    public static String UP_HOSE_ID_FS3;
    public static String UP_HOSE_ID_FS4;
    public static String UP_HOSE_ID_FS5;
    public static String UP_HOSE_ID_FS6;

    public static String UP_SITE_ID_FS1;
    public static String UP_SITE_ID_FS2;
    public static String UP_SITE_ID_FS3;
    public static String UP_SITE_ID_FS4;
    public static String UP_SITE_ID_FS5;
    public static String UP_SITE_ID_FS6;

    public static String TITLE = "";
    public static String SITE_NAME = "";
    public static String HUB_NAME;
    public static String HUB_GENERATED_PASSWORD;
    public static String LOGIN_EMAIL;
    public static String LOGIN_IMEI;
    public static String ACCESS_CODE = "2901";
    public static String DISABLE_ALL_REBOOT_OPTIONS = "";

    public static String RES_MESSAGE = "ResponceMessage";
    public static String RES_DATA = "ResponceData";
    public static String RES_DATA_SSID = "SSIDDataObj";
    public static String RES_DATA_USER = "objUserData";
    public static String RES_TANK_DATA = "TanksForLinksObj";
    public static String RES_PRODUCT_DATA = "ProductsObj";
    public static String RES_TEXT = "ResponceText";
    public static String VALIDATION_FOR_TEXT = "ValidationFailFor";

    public static String FOB_KEY_PERSON = "";
    public static String FOB_KEY_VEHICLE = "";
    public static String HUB_ID = "";

    public static boolean IS_TEST_TRANSACTION = false;

    public static String FS1_CONNECTED_SSID;
    public static String FS2_CONNECTED_SSID;
    public static String FS3_CONNECTED_SSID;
    public static String FS4_CONNECTED_SSID;
    public static String FS5_CONNECTED_SSID;
    public static String FS6_CONNECTED_SSID;

    public static String REPLACEABLE_WIFI_NAME_FS_ON_UPDATE_MAC;
    public static String REPLACEABLE_WIFI_NAME_FS1;
    public static String REPLACEABLE_WIFI_NAME_FS2;
    public static String REPLACEABLE_WIFI_NAME_FS3;
    public static String REPLACEABLE_WIFI_NAME_FS4;
    public static String REPLACEABLE_WIFI_NAME_FS5;
    public static String REPLACEABLE_WIFI_NAME_FS6;

    public static boolean NEED_TO_RENAME_FS_ON_UPDATE_MAC;
    public static boolean NEED_TO_RENAME_FS1;
    public static boolean NEED_TO_RENAME_FS2;
    public static boolean NEED_TO_RENAME_FS3;
    public static boolean NEED_TO_RENAME_FS4;
    public static boolean NEED_TO_RENAME_FS5;
    public static boolean NEED_TO_RENAME_FS6;

    public static String DOWNLOAD_FILE_HTTP_SERVER = "";
    public static boolean MANUAL_RECONFIGURE;
    public static String SELECTED_SSID_FOR_MANUAL;

    public static String REPLACEABLE_WIFI_NAME;
    public static String LAST_CONNECTED_SSID;
    public static String SELECTED_MAC_ADDRESS;
    public static String CURRENT_SELECTED_SSID;
    public static String CURRENT_SELECTED_SSID_ReqTLDCall;
    public static String CURRENT_HOSE_SSID;
    public static String CURRENT_SELECTED_SITE_ID;
    public static String UPDATE_MAC_ADDRESS;
    public static String R_HOSE_ID;
    public static String R_SITE_ID;
    public static String SITE_ID;
    public static String CURRENT_NEW_LINK_SELECTED_FOR_CONFIGURE;
    public static String CURRENT_NEW_LINK_SITE_ID;
    public static String NEW_LINK_UPDATE_MAC_ADDRESS;

    public static String WIFI_PASSWORD = "";

    public static boolean NEED_TO_RENAME;
    public static boolean BUSY_STATUS;

    public static boolean ACS_READER;
    public static boolean IS_WIFI_ON;
    public static boolean IS_DATA_ON;
    public static boolean IS_HOTSPOT_ON;

    public static String LATITUDE = "0.00";
    public static String LONGITUDE = "0.00";

    public static ArrayList<HashMap<String, String>> DETAILS_SERVER_SSID_LIST;
    public static ArrayList<HashMap<String, String>> DETAILS_LIST_OF_CONNECTED_DEVICES;

    public static boolean IS_ALL_LINKS_ARE_BT_LINKS = true;
    public static boolean IS_HTTP_TXN_RUNNING_FS1 = false;
    public static boolean IS_HTTP_TXN_RUNNING_FS2 = false;
    public static boolean IS_HTTP_TXN_RUNNING_FS3 = false;
    public static boolean IS_HTTP_TXN_RUNNING_FS4 = false;
    public static boolean IS_HTTP_TXN_RUNNING_FS5 = false;
    public static boolean IS_HTTP_TXN_RUNNING_FS6 = false;

    public static ArrayList<String> LIST_OF_RUNNING_TRANSACTIONS = new ArrayList<>();
    public static ArrayList<String> List_Of_Uploading_Transactions = new ArrayList<String>();

    public static boolean LANGUAGE_CHANGED = false;
    public static boolean IS_OFFLINE_DOWNLOAD_STARTED = false;
    public static boolean FORCE_DOWNLOAD_OFFLINE_DATA = false;
    public static boolean IS_BT_LINK_UPGRADE_IN_PROGRESS = false;

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

    /*public static void dontConnectWiFi(Context ctx) {
        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);

        if (wifiManager.isWifiEnabled()) {
            wifiManager.disconnect();
            wifiManager.setWifiEnabled(true);
        }
    }*/

    /*public static void forgetWiFi(Context ctx) {
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
    }*/

    /*public static String getConnectedWifiName(Context context) {
        String name = "";
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        name = wifiInfo.getSSID();

        System.out.println("connected ssid--" + name);

        return name;
    }*/

    public static void alertDialogBox(final Context ctx, String message) {
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

    /*public static void AlertDialogBoxCanecl(final Context ctx, int message) {
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
    }*/

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


    public static void alertDialogFinish(final Activity ctx, String message) {
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

    public static void alertDialogFinishWithTitle(final Activity ctx, String title, String message) {
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
        /*Toast toast = Toast.makeText(ctx, " " + msg + " ", Toast.LENGTH_LONG);
        toast.getView().setBackgroundColor(colr);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();*/ // toast.getView() is deprecated. Removed toast and used custom dialog.

        final Dialog dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_toast);
        dialog.setCancelable(false);

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        window.setAttributes(wlp);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        TextView toastMessage = (TextView) dialog.findViewById(R.id.toastMessage);
        toastMessage.setText(" " + msg + " ");
        toastMessage.setBackgroundColor(colr);

        CountDownTimer cTimer = null;
        cTimer = new CountDownTimer(4000, 4000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if (dialog != null) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            }
        };
        cTimer.start();
        dialog.show();
    }

    public static Dialog colorToastBigFont(Context ctx, String msg, int colr) {
        /*Toast toast = Toast.makeText(ctx, " " + msg + " ", Toast.LENGTH_LONG);
        toast.getView().setBackgroundColor(colr);
        toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 280);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setTextSize(25);
        toast.show();*/ // toast.getView() is deprecated. Removed toast and used custom dialog.

        final Dialog dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_toast);
        dialog.setCancelable(false);

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.TOP | Gravity.CENTER;
        wlp.y = 280;
        window.setAttributes(wlp);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        TextView toastMessage = (TextView) dialog.findViewById(R.id.toastMessage);
        toastMessage.setText(" " + msg + " ");
        toastMessage.setBackgroundColor(colr);
        toastMessage.setTextSize(25);

        CountDownTimer cTimer = null;
        cTimer = new CountDownTimer(3000, 3000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if (dialog != null) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            }
        };
        cTimer.start();
        dialog.show();
        return dialog;
    }

    public static void colorToastHotspotOn(Context ctx, String msg, int backColor, int textColor) {

        Toast toast = Toast.makeText(ctx, " " + msg + " ", Toast.LENGTH_SHORT);
        toast.getView().setBackgroundColor(backColor);
        toast.setGravity(Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK | Gravity.TOP, 0, AppConstants.SCREEN_RESOLUTION_Y_OFFSET);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setShadowLayer(1, 1, 1, backColor);
        messageTextView.setTextColor(textColor);
        messageTextView.setTextSize(40);
        toast.show();
    }

    public static int getYOffsetFromScreenResolution(final Activity ctx) {
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

    /*public static void notificationAlert(Context context) {
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
    }*/

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

    public static void writeTimeStamp(String str) {
        try {
            //File file = new File(Environment.getExternalStorageDirectory() + "/FSTimeStamp");
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/FSTimeStamp");

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
            writeInFile("WriteTimeStamp Exception" + e);
        }
    }

    public static void writeInFile(String str) {
        try {
            if (str.contains("Responce"))
                str = str.replace("Responce", "Response");

            System.out.println(str);

            //File file = new File(Environment.getExternalStorageDirectory() + "/FSLog");
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/FSLog");

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
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss.SSS");
            String UseDate = dateFormat.format(cal.getTime());

            FileWriter fileWritter = new FileWriter(gpxfile, true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write("\n" + UseDate + "-" + str + " ");
            bufferWritter.close();

        } catch (IOException e) {
            //writeInFile("writeInFile Exception: " + e);
            System.out.println("writeInFile Exception: " + e);
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

    public static void clearEditTextFieldsOnBack(Context ctx) {
        if (Constants.CURRENT_SELECTED_HOSE.equals("FS1")) {
            Constants.VEHICLE_NUMBER_FS1 = "";
            Constants.ODO_METER_FS1 = 0;
            Constants.DEPARTMENT_NUMBER_FS1 = "";
            Constants.PERSONNEL_PIN_FS1 = "";
            Constants.OTHER_FS1 = "";
            Constants.VEHICLE_OTHER_FS1 = "";
            Constants.HOURS_FS1 = 0;

        } else if (Constants.CURRENT_SELECTED_HOSE.equals("FS2")) {

            Constants.VEHICLE_NUMBER_FS2 = "";
            Constants.ODO_METER_FS2 = 0;
            Constants.DEPARTMENT_NUMBER_FS2 = "";
            Constants.PERSONNEL_PIN_FS2 = "";
            Constants.OTHER_FS2 = "";
            Constants.VEHICLE_OTHER_FS2 = "";
            Constants.HOURS_FS2 = 0;

        } else if (Constants.CURRENT_SELECTED_HOSE.equals("FS3")) {

            Constants.VEHICLE_NUMBER_FS3 = "";
            Constants.ODO_METER_FS3 = 0;
            Constants.DEPARTMENT_NUMBER_FS3 = "";
            Constants.PERSONNEL_PIN_FS3 = "";
            Constants.OTHER_FS3 = "";
            Constants.VEHICLE_OTHER_FS3 = "";
            Constants.HOURS_FS3 = 0;

        } else if (Constants.CURRENT_SELECTED_HOSE.equals("FS4")) {

            Constants.VEHICLE_NUMBER_FS4 = "";
            Constants.ODO_METER_FS4 = 0;
            Constants.DEPARTMENT_NUMBER_FS4 = "";
            Constants.PERSONNEL_PIN_FS4 = "";
            Constants.OTHER_FS4 = "";
            Constants.VEHICLE_OTHER_FS4 = "";
            Constants.HOURS_FS4 = 0;

        } else if (Constants.CURRENT_SELECTED_HOSE.equals("FS5")) {

            Constants.VEHICLE_NUMBER_FS5 = "";
            Constants.ODO_METER_FS5 = 0;
            Constants.DEPARTMENT_NUMBER_FS5 = "";
            Constants.PERSONNEL_PIN_FS5 = "";
            Constants.OTHER_FS5 = "";
            Constants.VEHICLE_OTHER_FS5 = "";
            Constants.HOURS_FS5 = 0;

        } else if (Constants.CURRENT_SELECTED_HOSE.equals("FS6")) {

            Constants.VEHICLE_NUMBER_FS6 = "";
            Constants.ODO_METER_FS6 = 0;
            Constants.DEPARTMENT_NUMBER_FS6 = "";
            Constants.PERSONNEL_PIN_FS6 = "";
            Constants.OTHER_FS6 = "";
            Constants.VEHICLE_OTHER_FS6 = "";
            Constants.HOURS_FS6 = 0;

        }
    }

    /*public static void showHideActivityBySharedPref(Activity actctx) {
        SharedPreferences sharedPrefODO = actctx.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IS_PERSONNEL_PIN_REQUIRE, "");
        String IsHoursRequire = sharedPrefODO.getString(AppConstants.IS_HOURS_REQUIRE, "");
        String IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IS_DEPARTMENT_REQUIRE, "");
        String IsOtherRequire = sharedPrefODO.getString(AppConstants.IS_OTHER_REQUIRE, "");

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
    }*/

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

    public static boolean isAllHosesAreFree() {

        if (Constants.FS_1_STATUS.equalsIgnoreCase("FREE") && AppConstants.IS_TRANSACTION_COMPLETED_1 &&
                Constants.FS_2_STATUS.equalsIgnoreCase("FREE") && AppConstants.IS_TRANSACTION_COMPLETED_2 &&
                Constants.FS_3_STATUS.equalsIgnoreCase("FREE") && AppConstants.IS_TRANSACTION_COMPLETED_3 &&
                Constants.FS_4_STATUS.equalsIgnoreCase("FREE") && AppConstants.IS_TRANSACTION_COMPLETED_4 &&
                Constants.FS_5_STATUS.equalsIgnoreCase("FREE") && AppConstants.IS_TRANSACTION_COMPLETED_5 &&
                Constants.FS_6_STATUS.equalsIgnoreCase("FREE") && AppConstants.IS_TRANSACTION_COMPLETED_6) {
            return true;
        } else {
            return false;
        }
    }

    public static String spanishNumberSystem(String flQty) {
        if (AppConstants.LANG_PARAM.equals(":es-ES")) {
            return flQty.replace(".", ",");
        } else {
            return flQty;
        }
    }
}
