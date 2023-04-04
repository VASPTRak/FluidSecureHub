package com.TrakEngineering.FluidSecureHub;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.TrakEngineering.FluidSecureHub.BTSPP.BTConstants;
import com.TrakEngineering.FluidSecureHub.EddystoneScanner.FStagScannerService;
import com.TrakEngineering.FluidSecureHub.EddystoneScanner.SampleBeacon;
import com.TrakEngineering.FluidSecureHub.HFCardGAtt.ServiceHFCard;
import com.TrakEngineering.FluidSecureHub.LFCardGAtt.ServiceLFCard;
import com.TrakEngineering.FluidSecureHub.MagCardGAtt.ServiceMagCard;
import com.TrakEngineering.FluidSecureHub.enity.UpgradeVersionEntity;
import com.TrakEngineering.FluidSecureHub.enity.VehicleRequireEntity;
import com.TrakEngineering.FluidSecureHub.offline.EntityHub;
import com.TrakEngineering.FluidSecureHub.offline.OffDBController;
import com.TrakEngineering.FluidSecureHub.offline.OfflineConstants;
import com.TrakEngineering.FluidSecureHub.server.ServerHandler;
import com.example.barcodeml.LivePreviewActivity;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.TrakEngineering.FluidSecureHub.server.ServerHandler.TEXT;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService_Pin}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class AcceptVehicleActivity_new extends AppCompatActivity implements ServiceConnection, FStagScannerService.OnBeaconEventListener {

    OffDBController controller = new OffDBController(AcceptVehicleActivity_new.this);

    private NetworkReceiver receiver = new NetworkReceiver();

    private TextView tv_fobkey, tv_hf_status, tv_lf_status, tv_mag_status, tv_reader_status;
    private LinearLayout layout_reader_status;
    private String mDeviceName;
    private String mDisableFOBReadingForVehicle;
    private String mDeviceAddress;
    private String mMagCardDeviceName;
    private String mMagCardDeviceAddress;
    private String mDeviceName_hf_trak, QRCodeReaderForBarcode, QRCodeBluetoothMacAddressForBarcode;
    private String mDeviceAddress_hf_trak;
    private String HFDeviceName;
    private String HFDeviceAddress;
    InputMethodManager imm;
    public BroadcastMagCard_dataFromServiceToUI ServiceCardReader_vehicle = null;

    private EditText etInput;
    String LF_FobKey = "";
    int Count = 1, LF_ReaderConnectionCount = 0, sec_count = 0;
    boolean IsNewFobVar = true;
    private Handler mHandler;
    public static ArrayList<HashMap<String, String>> ListOfBleDevices = new ArrayList<>();

    private static final int EXPIRE_TIMEOUT = 5000;
    private static final int EXPIRE_TASK_PERIOD = 1000;
    private static final int RC_BARCODE_CAPTURE = 9001;
    public String Barcode_val = "", MagCard_vehicle = "", ScreenNameForVehicle = "VEHICLE", ScreenNameForPersonnel = "PERSONNEL", KeyboardType = "2";

    //EddystoneScannerService
    private FStagScannerService mService;
    //--------------------------

    private static final String TAG = "Vehicle_Activity ";

    private EditText editVehicleNumber;
    String IsExtraOther = "", ExtraOtherLabel = "", FSTagMacAddress = "", IsVehicleHasFob = "", IsOdoMeterRequire = "", IsDepartmentRequire = "", IsPersonnelPINRequire = "", IsPersonnelPINRequireForHub = "", IsOtherRequire = "", IsVehicleNumberRequire = "", IsStayOpenGate = "", IsGateHub = "", IsHoursRequire = "", AllowAccessDeviceORManualEntryForVehicle = "";
    String IsNonValidateVehicle = "", IsNonValidateODOM = "";
    Button btnCancel, btnSave, btn_ReadFobAgain, btnFStag, btn_barcode;
    GoogleApiClient mGoogleApiClient;
    public static double CurrentLat = 0, CurrentLng = 0;
    RelativeLayout footer_keybord;
    LinearLayout Linear_layout_Save_back_buttons;
    TextView tv_fob_Reader, tv_or, tv_return, tv_swipekeybord, tv_fob_number, tv_vehicle_no_below, tv_dont_have_fob, tv_enter_vehicle_no, tv_title;
    LinearLayout Linear_layout_vehicleNumber;
    EditText editFobNumber;
    String TimeOutinMinute;
    boolean Istimeout_Sec = true;
    int FobReadingCount = 0;
    int FobRetryCount = 0;
    long screenTimeOut;
    private static Timer t, ScreenOutTimeVehicle;

    String FOLDER_PATH_BLE = null;
    List<Timer> Timerlist = new ArrayList<Timer>();
    List<Timer> ScreeTimerlist = new ArrayList<Timer>();

    //BLE Upgrade
    String BLEVersion;
    String BLEType = "";
    String BLEFileLocation;
    String IsLFUpdate = "N";
    String IsHFUpdate = "N";

    String HFVersion = "";
    String LFVersion = "";

    String BLEVersionLFServer;
    String BLEVersionHFServer;
    String IsHFUpdateServer = "N";
    String IsLFUpdateServer = "N";
    private int bleVersionCallCount = 0;
    boolean bleLFUpdateSuccessFlag = false;
    boolean bleHFUpdateSuccessFlag = false;
    HashMap<String, String> hmapSwitchOffline = new HashMap<>();
    String LFReaderStatus = "", HFReaderStatus = "", MagReaderStatus = "";
    public boolean VehicleValidationInProgress = false;

    //-------------------------
    ConnectionDetector cd = new ConnectionDetector(AcceptVehicleActivity_new.this);


    private void clearUI() {

        tv_fobkey.setText("");

        int widthi = ActionBar.LayoutParams.WRAP_CONTENT;
        int heighti = ActionBar.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams parmsi = new LinearLayout.LayoutParams(widthi, heighti);
        tv_enter_vehicle_no.setLayoutParams(parmsi);
        tv_enter_vehicle_no.setVisibility(View.VISIBLE);
        tv_enter_vehicle_no.setText("   Please wait, processing");
        tv_fob_number.setText("Access Device No: ");
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control_vehicle);

        DisplayMeterActivity.setHttpTransportToDefaultNetwork(AcceptVehicleActivity_new.this);

        SharedPreferences sharedPre2 = AcceptVehicleActivity_new.this.getSharedPreferences("storeBT_FOBDetails", Context.MODE_PRIVATE);

        mDisableFOBReadingForVehicle = sharedPre2.getString("DisableFOBReadingForVehicle", "");
        mDeviceName = sharedPre2.getString("LFBluetoothCardReader", "");
        mDeviceAddress = sharedPre2.getString("LFBluetoothCardReaderMacAddress", "");
        HFDeviceName = sharedPre2.getString("BluetoothCardReader", "");
        HFDeviceAddress = sharedPre2.getString("BTMacAddress", "");
        mDeviceName_hf_trak = sharedPre2.getString("HFTrakCardReader", ""); //
        mDeviceAddress_hf_trak = sharedPre2.getString("HFTrakCardReaderMacAddress", ""); //
        AppConstants.ACS_READER = sharedPre2.getBoolean("ACS_Reader", false);
        mMagCardDeviceName = sharedPre2.getString("MagneticCardReader", ""); //
        mMagCardDeviceAddress = sharedPre2.getString("MagneticCardReaderMacAddress", ""); //
        QRCodeReaderForBarcode = sharedPre2.getString("QRCodeReaderForBarcode", ""); //
        QRCodeBluetoothMacAddressForBarcode = sharedPre2.getString("QRCodeBluetoothMacAddressForBarcode", ""); //

        CommonUtils.LogReaderDetails(AcceptVehicleActivity_new.this);

        // Sets up UI references.
        tv_fobkey = (TextView) findViewById(R.id.tv_fobkey);
        etInput = (EditText) findViewById(R.id.etInput);

        getSupportActionBar().setTitle(AppConstants.BrandName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences myPrefkb = this.getSharedPreferences(AppConstants.sharedPref_KeyboardType, 0);
        KeyboardType = myPrefkb.getString("KeyboardTypeVehicle", "2");
        ScreenNameForVehicle = myPrefkb.getString("ScreenNameForVehicle", "Vehicle");
        ScreenNameForPersonnel = myPrefkb.getString("ScreenNameForPersonnel", "Personnel");

        if (ScreenNameForVehicle.trim().isEmpty())
            ScreenNameForVehicle = "Vehicle";

        InItGUI();

        SharedPreferences sharedPrefODO = AcceptVehicleActivity_new.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
        TimeOutinMinute = sharedPrefODO.getString(AppConstants.TimeOut, "1");
        IsVehicleNumberRequire = sharedPrefODO.getString(AppConstants.IsVehicleNumberRequire, "");
        AllowAccessDeviceORManualEntryForVehicle = sharedPrefODO.getString(AppConstants.AllowAccessDeviceORManualEntryForVehicle, "");
        AppConstants.HUB_ID = sharedPrefODO.getString(AppConstants.HubId, "");

        SharedPreferences sharedPrefGatehub = AcceptVehicleActivity_new.this.getSharedPreferences(Constants.PREF_COLUMN_GATE_HUB, Context.MODE_PRIVATE);
        IsGateHub = sharedPrefGatehub.getString(AppConstants.IsGateHub, "");
        IsStayOpenGate = sharedPrefGatehub.getString(AppConstants.IsStayOpenGate, "");

        SharedPreferences sharedPref = AcceptVehicleActivity_new.this.getSharedPreferences(Constants.PREF_COLUMN_SITE, Context.MODE_PRIVATE);
        String dataSite = sharedPref.getString(Constants.PREF_COLUMN_SITE, "");

        VehicleValidationInProgress = false;
        AppConstants.serverCallInProgressForVehicle = false;

        //enable hotspot.
        Constants.hotspotstayOn = true;

        mHandler = new Handler();

        //Check Selected FS and  change accordingly
        //Constants.AccVehicleNumber = "";
        //Constants.AccOdoMeter = 0;
        //Constants.AccHours = 0;
        //Constants.AccDepartmentNumber = "";
        //Constants.AccPersonnelPIN = "";
        //Constants.AccOther = "";
        //AppConstants.UP_Upgrade= true;

        //CheckForFirmwareUpgrade(); //BLE reader upgrade and link firmware download

        editVehicleNumber.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boolean ps = isKeyboardShown(editVehicleNumber.getRootView());
                if (ps == true) {
                    footer_keybord.setEnabled(true);
                    footer_keybord.setVisibility(View.VISIBLE);
                } else {
                    footer_keybord.setEnabled(false);
                    footer_keybord.setVisibility(View.INVISIBLE);
                }

            }
        });

        try {
            editVehicleNumber.setInputType(Integer.parseInt(KeyboardType));
        } catch (Exception e) {
            System.out.println("keyboard exception");
            editVehicleNumber.setInputType(InputType.TYPE_CLASS_TEXT);
        }


        tv_swipekeybord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int InputTyp = editVehicleNumber.getInputType();
                if (InputTyp == 2) {
                    editVehicleNumber.setInputType(InputType.TYPE_CLASS_TEXT);
                    tv_swipekeybord.setText(getResources().getString(R.string.PressFor123));
                } else {

                    editVehicleNumber.setInputType(InputType.TYPE_CLASS_NUMBER);//| InputType.TYPE_CLASS_TEXT
                    tv_swipekeybord.setText(getResources().getString(R.string.PressForABC));
                }

            }
        });

        btnFStag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Execute TagReader Code Here
                new TagReaderFun().execute();

            }
        });

        btn_barcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*
                // launch barcode activity.
                Intent intent = new Intent(AcceptVehicleActivity_new.this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
                intent.putExtra("ForScreen","Vehicle");
                startActivityForResult(intent, RC_BARCODE_CAPTURE);
                */

                Intent intent = new Intent(AcceptVehicleActivity_new.this, LivePreviewActivity.class);
                startActivityForResult(intent, RC_BARCODE_CAPTURE);

            }
        });

        btn_ReadFobAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onResume();
            }
        });

        tv_return.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                hideKeybord();
            }
        });


        // Registers BroadcastReceiver to track network connection changes.
        IntentFilter ifilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver();
        this.registerReceiver(receiver, ifilter);


    }

    @Override
    protected void onResume() {
        super.onResume();

        if (AppConstants.serverAuthCallCompleted) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "<onResume skipped.>");
            return;
        }

        AppConstants.AUTH_CALL_SUCCESS = false;
        resetReaderStatus();//BLE reader status reset

        if (AppConstants.EnableFA) {

            btnFStag.setVisibility(View.VISIBLE);
            btnFStag.setEnabled(true);

        } else {

            btnFStag.setVisibility(View.GONE);
            btnFStag.setEnabled(true);
        }

        Count = 1;
        LF_ReaderConnectionCount = 0;
        AppConstants.VehicleLocal_FOB_KEY = "";
        AppConstants.APDU_FOB_KEY = "";
        AppConstants.NonValidateVehicle_FOB_KEY = "";
        Log.i(TAG, "Bacode value on resume" + Barcode_val);

        editVehicleNumber.setText("");

        DisplayScreenInit();
        if (IsGateHub.equalsIgnoreCase("True")) {
            Istimeout_Sec = false;
        } else {
            Istimeout_Sec = true;
        }

        RegisterBroadcastForReader();//BroadcastReciver for MagCard,HF and LF Readers

        TimeoutVehicleScreen();
        Log.i("TimeoutVehicleScreen", "TimeOut_Start");

        tv_fobkey.setText("");
        tv_fob_number.setText("Access Device No: ");
        LF_FobKey = "";
        t = new Timer();
        Timerlist.add(t);
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {

                sec_count++;
                invalidateOptionsMenu();
                UpdateReaderStatusToUI();

                if (!AppConstants.VehicleLocal_FOB_KEY.equalsIgnoreCase("")) {

                    CancelTimer();
                    // AppConstants.VehicleLocal_FOB_KEY = AppConstants.APDU_FOB_KEY;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            FobreadSuccess();
                        }
                    });

                } else {

                    if (IsGateHub.equalsIgnoreCase("False")) {
                        checkFor10Seconds();
                    }
                }
            }

        };
        t.schedule(tt, 1000, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {

            System.out.println("~~~~~~Onpause~~~~");
            CancelTimer();

            UnRegisterBroadcastForReader();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("~~~~~~OnDestroy~~~~");
        CancelTimer();

        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("~~~~~~OnStop~~~~");
        AppConstants.APDU_FOB_KEY = "";
        AppConstants.VehicleLocal_FOB_KEY = "";
        CancelTimer();
        CancelTimerScreenOut();
    }

    private void CancelTimer() {

        for (int i = 0; i < Timerlist.size(); i++) {
            Timerlist.get(i).cancel();
        }

    }

    private void CancelTimerScreenOut() {

        for (int i = 0; i < ScreeTimerlist.size(); i++) {
            ScreeTimerlist.get(i).cancel();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reader, menu);

        menu.findItem(R.id.mreboot_reader).setVisible(true);
        //menu.findItem(R.id.mconfigure_tld).setVisible(false);
        menu.findItem(R.id.madd_link).setVisible(false);
        menu.findItem(R.id.mupgrade_normal_link).setVisible(false);
        menu.findItem(R.id.mclose).setVisible(false);
        menu.findItem(R.id.mcamera_back).setVisible(false);
        menu.findItem(R.id.mcamera_front).setVisible(false);
        menu.findItem(R.id.mreconnect_ble_readers).setVisible(true);
        menu.findItem(R.id.mshow_reader_status).setVisible(true);
        menu.findItem(R.id.mcamera_back).setVisible(false);
        menu.findItem(R.id.mcamera_front).setVisible(false);
        menu.findItem(R.id.mreload).setVisible(false);

        if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {

            menu.findItem(R.id.monline).setVisible(true);
            menu.findItem(R.id.mofline).setVisible(false);

        } else {
            menu.findItem(R.id.monline).setVisible(false);
            menu.findItem(R.id.mofline).setVisible(true);
        }

        MenuItem itemSp = menu.findItem(R.id.menuSpanish);
        MenuItem itemEng = menu.findItem(R.id.menuEnglish);
        itemSp.setVisible(false);
        itemEng.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:

                //connect readers code
                return true;

            case R.id.mreboot_reader:
                AppConstants.showReaderStatus = true;
                CustomDilaogForRebootCmd(AcceptVehicleActivity_new.this, "Please enter a code to continue.", "Message");
                return true;
            case R.id.menu_disconnect:
                //mBluetoothLeServiceVehicle.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.mreconnect_ble_readers:
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "<Reconnect BLE Readers>");
                AppConstants.showReaderStatus = true;
                new ReconnectBleReaders().execute();
                return true;

            case R.id.mshow_reader_status:
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "<Show Reader Status>");
                AppConstants.showReaderStatus = true;
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void displayData_LF(String data) {

        //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "mGattUpdateReceiver LF data " + data);

        if (data != null || !data.isEmpty()) {

            String Str_data = data.toString().trim();
            Log.i(TAG, "Response LF:" + Str_data);
            //if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + "Response LF: " + Str_data);
            String Str_check = Str_data.replace(" ", "").trim();

            if (Str_data.contains("FFFFFFFFFFFFFFFFFFFF") || Str_data.contains("FF FF FF FF FF FF FF FF FF FF")) {

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Unable to read fob: " + Str_data);
                CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", "Unable to read fob.  Please Try again");

            } else if (CommonUtils.ValidateFobkey(Str_check) && Str_check.length() > 4) {


                try {

                    if (Str_check.contains("\n")) {

                        String last_val = "";
                        String[] Seperate = Str_data.split("\n");
                        if (Seperate.length > 1) {
                            last_val = Seperate[Seperate.length - 1];
                        }
                        LF_FobKey = last_val.replaceAll("\\s", "");
                        tv_fobkey.setText(last_val.replace(" ", ""));


                    } else {

                        LF_FobKey = Str_check.replaceAll("\\s", "");
                        tv_fobkey.setText(Str_check.replace(" ", ""));
                    }

                    if (!LF_FobKey.equalsIgnoreCase("") && LF_FobKey.length() > 5) {
                        tv_fob_number.setText("Access Device No: " + LF_FobKey);
                        AppConstants.APDU_FOB_KEY = LF_FobKey;
                        System.out.println("Vehicle fob value" + AppConstants.APDU_FOB_KEY);
                        Log.i(TAG, "Vehi fob:" + AppConstants.APDU_FOB_KEY);
                        AppConstants.VehicleLocal_FOB_KEY = LF_FobKey;
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Local_FOB_KEY" + AppConstants.VehicleLocal_FOB_KEY);
                        //On LF Fob read success
                        // editVehicleNumber.setText(""); //#1145
                        Istimeout_Sec = false;
                        CancelTimerScreenOut();
                    }

                } catch (Exception ex) {
                    System.out.println(ex);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "displayData Fob_Key  --Exception " + ex);
                }

            }
            //BLE Upgrade
            else if (!Str_data.equals("00 00 00")) {
                LFVersion = Str_data;
                SharedPreferences sharedPref = getSharedPreferences("LFVersionInfo", 0);
                SharedPreferences.Editor editor1 = sharedPref.edit();
                editor1.putString("LFVersion", LFVersion);
                editor1.commit();
                System.out.println("BLEVERSION: " + LFVersion);
                String serverRes = sendVersionToServer(LFVersion, "LF");
                try {
                    if (serverRes != null && !serverRes.equals("")) {

                        JSONObject jsonObject = new JSONObject(serverRes);

                        String ResponceMessage = jsonObject.getString("ResponceMessage");


                        System.out.println("ResponceMessage.." + ResponceMessage);

                        if (ResponceMessage.equalsIgnoreCase("Success!")) {
                            BLEVersionLFServer = jsonObject.getString("BLEVersionLF");
                            IsLFUpdateServer = jsonObject.getString("IsLFUpdate");
                        }
                    }
                } catch (Exception e) {
                    Log.d("Ex", e.getMessage());
                }
                if (IsLFUpdateServer.trim().equalsIgnoreCase("Y")) {
                    bleLFUpdateSuccessFlag = true;
                    SharedPreferences sharedPre = getSharedPreferences("BLEUpgradeFlag", 0);
                    SharedPreferences.Editor editor = sharedPre.edit();
                    editor.putString("bleLFUpdateSuccessFlag", "Y");
                    editor.commit();
                } else {
                    bleLFUpdateSuccessFlag = false;
                    SharedPreferences sharedPre = getSharedPreferences("BLEUpgradeFlag", 0);
                    SharedPreferences.Editor editor = sharedPre.edit();
                    editor.putString("bleLFUpdateSuccessFlag", "N");
                    editor.commit();
                }
            }
        }
    }

    private void displayData_HF(String data) {

        //print raw reader data in log file
        //if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + "  BroadcastReceiver HF displayData_HF " + data);

        if (data != null && !data.isEmpty()) {

            String Str_data = data.toString().trim();
            Log.i(TAG, "Response HF:" + Str_data);
            // if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + "Response HF: " + Str_data);
            String Str_check = Str_data.replace(" ", "");

            if (Str_data.contains("FFFFFFFFFFFFFFFFFFFF") || Str_data.contains("FF FF FF FF FF FF FF FF FF FF")) {

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Unable to read fob: " + Str_data);
                CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", "Unable to read fob.  Please Try again..");

            } else if (CommonUtils.ValidateFobkey(Str_check) && Str_check.length() > 4) {

                try {

                    if (Str_check.contains("\n")) {

                        String last_val = "";
                        String[] Seperate = Str_data.split("\n");
                        if (Seperate.length > 1) {
                            last_val = Seperate[Seperate.length - 1];
                        }
                        LF_FobKey = last_val.replaceAll("\\s", "");
                        tv_fobkey.setText(last_val.replace(" ", ""));


                    } else {

                        LF_FobKey = Str_data.replaceAll("\\s", "");
                        tv_fobkey.setText(Str_data.replace(" ", ""));
                    }

                    if (!LF_FobKey.equalsIgnoreCase("") && LF_FobKey.length() > 5) {
                        tv_fob_number.setText("Access Device No: " + LF_FobKey);
                        AppConstants.APDU_FOB_KEY = LF_FobKey;
                        System.out.println("Vehicle HF value" + AppConstants.APDU_FOB_KEY);
                        Log.i(TAG, "Vehi HF:" + AppConstants.APDU_FOB_KEY);
                        AppConstants.VehicleLocal_FOB_KEY = LF_FobKey;
                        //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Local_HF_KEY" + AppConstants.VehicleLocal_FOB_KEY);

                        //On HF Fob read success
                        //  editVehicleNumber.setText(""); #1145
                        Istimeout_Sec = false;
                        CancelTimerScreenOut();
                    }

                } catch (Exception ex) {
                    System.out.println(ex);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "displayData HF  --Exception " + ex);
                }

            }
            //BLE Upgrade
            else if (!Str_data.equals("00 00 00 00 00 00 00 00 00 00")) {
                HFVersion = Str_data;
                SharedPreferences sharedPref = getSharedPreferences("HFVersionInfo", 0);
                SharedPreferences.Editor editor1 = sharedPref.edit();
                editor1.putString("HFVersion", HFVersion);
                editor1.commit();

                System.out.println("BLEVERSION: " + HFVersion);
                String serverRes = sendVersionToServer(HFVersion, "HF");
                try {
                    if (serverRes != null && !serverRes.equals("")) {

                        JSONObject jsonObject = new JSONObject(serverRes);

                        String ResponceMessage = jsonObject.getString("ResponceMessage");


                        System.out.println("ResponceMessage.." + ResponceMessage);

                        if (ResponceMessage.equalsIgnoreCase("Success!")) {
                            BLEVersionHFServer = jsonObject.getString("BLEVersionHF");
                            IsHFUpdateServer = jsonObject.getString("IsHFUpdate");
                        }
                    }
                } catch (Exception e) {
                    Log.d("Ex", e.getMessage());
                }
                if (IsHFUpdateServer.trim().equalsIgnoreCase("Y")) {
                    bleHFUpdateSuccessFlag = true;
                    SharedPreferences sharedPre = getSharedPreferences("BLEUpgradeFlag", 0);
                    SharedPreferences.Editor editor = sharedPre.edit();
                    editor.putString("bleHFUpdateSuccessFlag", "Y");
                    editor.commit();
                } else {
                    bleHFUpdateSuccessFlag = false;
                    SharedPreferences sharedPre = getSharedPreferences("BLEUpgradeFlag", 0);
                    SharedPreferences.Editor editor = sharedPre.edit();
                    editor.putString("bleHFUpdateSuccessFlag", "N");
                    editor.commit();
                }
            }

        }
    }

    private void displayData_MagCard(String data) {

        System.out.println("MagCard data 002----" + data);
        //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " displayData_MagCard " + data);


        if (data != null && !data.isEmpty()) {

            String Str_data = data.toString().trim();
            Log.i(TAG, "displayData MagCard:" + Str_data);
            //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  displayData MagCard: " + Str_data);

            String Str_check = Str_data.replace(" ", "");
            if (!CommonUtils.ValidateFobkey(Str_check) || Str_data.contains("FFFFFFFFFFFFFFFFFFFF") || Str_data.contains("FF FF FF FF FF FF FF FF FF FF")) {

                MagCard_vehicle = "";
                // CommonUtils.AutoCloseCustomMessageDilaog(DeviceControlActivity_vehicle.this, "Message", "Unable to read MagCard.  Please Try again..");

            } else if (Str_check.length() > 5) {

                try {

                    MagCard_vehicle = Str_check;
                    tv_fobkey.setText(Str_check.replace(" ", ""));
                    tv_fob_number.setText("Access Device No: " + MagCard_vehicle);
                    //AppConstants.APDU_FOB_KEY = MagCard_vehicle;
                    AppConstants.VehicleLocal_FOB_KEY = MagCard_vehicle;
                    //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Local_MagCard_KEY" + AppConstants.VehicleLocal_FOB_KEY);
                    //On Mag Card read success
                    Istimeout_Sec = false;
                    CancelTimerScreenOut();

                } catch (Exception ex) {
                    MagCard_vehicle = "";
                    System.out.println(ex);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "displayData Split MagCard  --Exception " + ex);
                }

            }

        } else {
            MagCard_vehicle = "";
        }
    }

    private String sendVersionToServer(String bleVersion, String bleType) {
        SharedPreferences sharedPrefODO = this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String PersonId = sharedPrefODO.getString(AppConstants.HubId, "");

        SharedPreferences sharedPref = this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String userEmail = sharedPref.getString(AppConstants.USER_EMAIL, "");

        String authStringDefTire = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(this) + ":" + userEmail + ":" + "CheckCurrentBLEVersionOnDemand" + AppConstants.LANG_PARAM);
        BleVersionData bleVersionData = new BleVersionData();
        bleVersionData.BLEType = bleType;
        if (bleType.equals("HF")) {
            bleVersionData.VersionHF = bleVersion;
        } else {
            bleVersionData.VersionHF = "";
        }

        if (bleType.equals("LF")) {
            bleVersionData.VersionLF = bleVersion;
        } else {
            bleVersionData.VersionLF = "";
        }

        bleVersionData.PersonId = PersonId;
        Gson gson = new Gson();
        final String jsonDataDefTire = gson.toJson(bleVersionData);
        String response = "";
        try {
            if (cd.isConnecting())
                response = new sendBleVersionData().execute(jsonDataDefTire, authStringDefTire).get();
        } catch (Exception e) {
            System.out.println(e);
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "response BLE LF version number  --Exception " + e);
        }

        return response;

    }

    public void FobreadSuccess() {

        AppConstants.VehicleLocal_FOB_KEY = "";
        SharedPreferences sharedPrefODO = AcceptVehicleActivity_new.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        IsPersonnelPINRequireForHub = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequireForHub, "");
        IsVehicleNumberRequire = sharedPrefODO.getString(AppConstants.IsVehicleNumberRequire, "");

        if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {
            IsNonValidateVehicle = sharedPrefODO.getString(AppConstants.IsNonValidateVehicle, "");
        } else {

            IsNonValidateVehicle = controller.getOfflineHubDetails(AcceptVehicleActivity_new.this).IsNonValidateVehicle;
            IsNonValidateODOM = controller.getOfflineHubDetails(AcceptVehicleActivity_new.this).IsNonValidateODOM;
        }

        if (MagCard_vehicle != null && !MagCard_vehicle.isEmpty()) {

            String fob = MagCard_vehicle.replace(":", "");
            tv_fobkey.setText(fob);
            CommonUtils.PlayBeep(this);

            HashMap<String, String> hmap = new HashMap<>();
            if (!IsNonValidateVehicle.equalsIgnoreCase("True")) {
                hmap = getMagneticCardKey(MagCard_vehicle.trim());
            }

            hmapSwitchOffline = hmap;
            offlineVehicleInitialization(hmap);
            AppConstants.NonValidateVehicle_FOB_KEY = fob;

            if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {
                if (!isFinishing()) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "MagCard read success: " + fob);
                    new GetVehicleNuOnFobKeyDetection().execute();
                }
            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Internet Connection: " + cd.isConnectingToInternet() + "; NETWORK_STRENGTH: " + AppConstants.NETWORK_STRENGTH);
                //offline---------------Codehereorequest #1729 (Eva) New Boston Concrete.
                AppConstants.AUTH_CALL_SUCCESS = false;
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Offline Vehicle FOB: " + MagCard_vehicle);

                editVehicleNumber.setText(hmap.get("VehicleNumber"));
                tv_vehicle_no_below.setText(ScreenNameForVehicle + " : " + hmap.get("VehicleNumber"));
                tv_fob_number.setText("Access Device No: " + MagCard_vehicle);
                tv_fob_number.setVisibility(View.GONE);//VISIBLE

                if (OfflineConstants.isOfflineAccess(AcceptVehicleActivity_new.this)) {
                    //checkVehicleOFFLINEvalidation(hmap);
                    WaitAndRedirectToOFFLINEvalidation(hmap);

                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Offline Access not granted to this HUB.");
                    //CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", "Unable to connect server"); // Removed from all places as per #1899 => Eva's comment (Aug 4th)
                }
            }


        } else if (AppConstants.APDU_FOB_KEY != null) {

            String fob = AppConstants.APDU_FOB_KEY.replace(":", "");
            tv_fobkey.setText(fob);
            CommonUtils.PlayBeep(this);

            HashMap<String, String> hmap = new HashMap<>();
            if (!IsNonValidateVehicle.equalsIgnoreCase("True")) {
                hmap = controller.getVehicleDetailsByFOBNumber(fob.trim());
            }
            hmapSwitchOffline = hmap;
            offlineVehicleInitialization(hmap);
            AppConstants.NonValidateVehicle_FOB_KEY = fob;

            if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {
                if (!isFinishing()) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "FOB read success: " + fob);
                    new GetVehicleNuOnFobKeyDetection().execute();
                }
            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Internet Connection: " + cd.isConnectingToInternet() + "; NETWORK_STRENGTH: " + AppConstants.NETWORK_STRENGTH);
                ///offlline-------------------
                AppConstants.AUTH_CALL_SUCCESS = false;
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Offline Vehicle FOB: " + AppConstants.APDU_FOB_KEY);

                editVehicleNumber.setText(hmap.get("VehicleNumber"));
                tv_vehicle_no_below.setText(ScreenNameForVehicle + " : " + hmap.get("VehicleNumber"));
                tv_fob_number.setText("Access Device No: " + AppConstants.APDU_FOB_KEY);
                tv_fob_number.setVisibility(View.GONE);//VISIBLE

                if (OfflineConstants.isOfflineAccess(AcceptVehicleActivity_new.this)) {
                    //checkVehicleOFFLINEvalidation(hmap);
                    WaitAndRedirectToOFFLINEvalidation(hmap);

                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Offline Access not granted to this HUB.");
                    //CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", "Unable to connect server");
                }
            }

        } else {
            AppConstants.colorToastBigFont(AcceptVehicleActivity_new.this, "Access Device not found", Color.BLUE);
        }
    }

    public void WaitAndRedirectToOFFLINEvalidation(HashMap<String, String> hmap) {
        try {
            btnSave.setEnabled(false);
            btnCancel.setEnabled(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    btnSave.setEnabled(true);
                    btnCancel.setEnabled(true);
                    checkVehicleOFFLINEvalidation(hmap);
                }
            }, 3000);
        } catch (Exception ex) {
            CommonUtils.LogMessage(TAG, "", ex);
            checkVehicleOFFLINEvalidation(hmap);
        }
    }

    public String parseSiteData(String dataSite) {
        String ssiteId = "";
        try {
            if (dataSite != null) {
                JSONArray jsonArray = new JSONArray(dataSite);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);

                    String SiteId = jo.getString("SiteId");
                    String SiteNumber = jo.getString("SiteNumber");
                    String SiteName = jo.getString("SiteName");
                    String SiteAddress = jo.getString("SiteAddress");
                    String Latitude = jo.getString("Latitude");
                    String Longitude = jo.getString("Longitude");
                    String HoseId = jo.getString("HoseId");
                    String HoseNumber = jo.getString("HoseNumber");
                    String WifiSSId = jo.getString("WifiSSId");
                    String UserName = jo.getString("UserName");
                    String Password = jo.getString("Password");

                    System.out.println("Wifi Password...." + Password);

                    //AppConstants.WIFI_PASSWORD = "";

                    ssiteId = SiteId;
                }
            }
        } catch (Exception ex) {

            CommonUtils.LogMessage(TAG, "", ex);
        }

        return ssiteId;
    }

    private void InItGUI() {

        Linear_layout_vehicleNumber = (LinearLayout) findViewById(R.id.Linear_layout_vehicleNumber);
        layout_reader_status = (LinearLayout) findViewById(R.id.layout_reader_status);
        tv_hf_status = (TextView) findViewById(R.id.tv_hf_status);
        tv_lf_status = (TextView) findViewById(R.id.tv_lf_status);
        tv_mag_status = (TextView) findViewById(R.id.tv_mag_status);
        tv_reader_status = (TextView) findViewById(R.id.tv_reader_status);
        tv_fob_Reader = (TextView) findViewById(R.id.tv_fob_Reader);
        tv_or = (TextView) findViewById(R.id.tv_or);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnFStag = (Button) findViewById(R.id.btnFStag);
        btn_barcode = (Button) findViewById(R.id.btn_barcode);
        btn_ReadFobAgain = (Button) findViewById(R.id.btn_ReadFobAgain);
        footer_keybord = (RelativeLayout) findViewById(R.id.footer_keybord);
        Linear_layout_Save_back_buttons = (LinearLayout) findViewById(R.id.Linear_layout_Save_back_buttons);
        tv_return = (TextView) findViewById(R.id.tv_return);
        tv_swipekeybord = (TextView) findViewById(R.id.tv_swipekeybord);
        tv_fob_number = (TextView) findViewById(R.id.tv_fob_number);
        tv_vehicle_no_below = (TextView) findViewById(R.id.tv_vehicle_no_below);
        tv_enter_vehicle_no = (TextView) findViewById(R.id.tv_enter_vehicle_no);
        tv_dont_have_fob = (TextView) findViewById(R.id.tv_dont_have_fob);

        //String content = "Enter your <br><b>" + ScreenNameForVehicle + "</b> in<br> the green box below";
        String content = getResources().getString(R.string.EnterVehicleId).replace("vehicle", "<br><b>" + ScreenNameForVehicle + "</b><br>");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tv_dont_have_fob.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
            System.out.println(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tv_dont_have_fob.setText(Html.fromHtml(content));
            System.out.println(Html.fromHtml(content));
        }

        try {
            btnCancel = (Button) findViewById(R.id.btnCancel);
            editVehicleNumber = (EditText) findViewById(R.id.editVehicleNumber);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }

        tv_title = (TextView) findViewById(R.id.tv_title);

        tv_title.setText(getResources().getString(R.string.vehicleIdentification).replace("VEHICLE", ScreenNameForVehicle.toUpperCase()));
        tv_fob_Reader.setText(getResources().getString(R.string.PresentVehicleAccessDevice).replace("vehicle", ScreenNameForVehicle));
        tv_vehicle_no_below.setText(getResources().getString(R.string.VehicleNumberHeading).replace("Number", "").replace("Vehicle", ScreenNameForVehicle));

    }

    //============SoftKeyboard enable/disable Detection======
    @SuppressLint("LongLogTag")
    private boolean isKeyboardShown(View rootView) {
        /* 128dp = 32dp * 4, minimum button height 32dp and generic 4 rows soft keyboard */
        final int SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD = 128;

        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
        /* heightDiff = rootView height - status bar height (r.top) - visible frame height (r.bottom - r.top) */
        int heightDiff = rootView.getBottom() - r.bottom;
        /* Threshold size: dp to pixels, multiply with display density */
        boolean isKeyboardShown = heightDiff > SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD * dm.density;

        return isKeyboardShown;
    }

    public void cancelAction(View v) {

        onBackPressed();
    }

    @Override
    public void onBeaconTelemetry(String deviceAddress, float battery, float temperature) {

    }


    public class ServerCallFirst extends AsyncTask<Void, Void, String> {

        ProgressDialog pd;
        String resp = "";

        @Override
        protected void onPreExecute() {

            try {
                String s = getResources().getString(R.string.PleaseWait);
                SpannableString ss2 = new SpannableString(s);
                ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
                ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
                pd = new ProgressDialog(AcceptVehicleActivity_new.this);
                pd.setMessage(ss2);
                pd.setCancelable(true);
                pd.show();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        protected String doInBackground(Void... arg0) {

            AppConstants.VehicleLocal_FOB_KEY = "";

            try {
                String V_Number = editVehicleNumber.getText().toString().trim();


                if (!V_Number.isEmpty() || !AppConstants.APDU_FOB_KEY.isEmpty() || !Barcode_val.isEmpty() || !MagCard_vehicle.isEmpty()) {


                    String vehicleNumber = "";
                    String pinNumber = "";

                    if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
                        //pinNumber = Constants.AccPersonnelPIN_FS1;
                        vehicleNumber = editVehicleNumber.getText().toString().trim();
                        Constants.AccVehicleNumber_FS1 = vehicleNumber;


                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
                        //pinNumber = Constants.AccPersonnelPIN;
                        vehicleNumber = editVehicleNumber.getText().toString().trim();
                        Constants.AccVehicleNumber = vehicleNumber;

                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
                        //pinNumber = Constants.AccPersonnelPIN_FS3;
                        vehicleNumber = editVehicleNumber.getText().toString().trim();
                        Constants.AccVehicleNumber_FS3 = vehicleNumber;
                        Log.i("ps_Vechile no", "Step 2:" + vehicleNumber);

                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS4")) {

                        vehicleNumber = editVehicleNumber.getText().toString().trim();
                        Constants.AccVehicleNumber_FS4 = vehicleNumber;

                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS5")) {

                        vehicleNumber = editVehicleNumber.getText().toString().trim();
                        Constants.AccVehicleNumber_FS5 = vehicleNumber;

                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS6")) {

                        vehicleNumber = editVehicleNumber.getText().toString().trim();
                        Constants.AccVehicleNumber_FS6 = vehicleNumber;

                    }


                    VehicleRequireEntity objEntityClass = new VehicleRequireEntity();
                    objEntityClass.IMEIUDID = AppConstants.getIMEI(AcceptVehicleActivity_new.this);
                    objEntityClass.VehicleNumber = vehicleNumber;
                    objEntityClass.WifiSSId = AppConstants.LAST_CONNECTED_SSID;
                    objEntityClass.SiteId = Integer.parseInt(AppConstants.SITE_ID);
                    objEntityClass.PersonnelPIN = pinNumber;
                    objEntityClass.RequestFromAPP = "AP";
                    objEntityClass.FromNewFOBChange = "Y";
                    objEntityClass.FOBNumber = AppConstants.APDU_FOB_KEY;
                    objEntityClass.IsVehicleNumberRequire = IsVehicleNumberRequire;
                    objEntityClass.Barcode = Barcode_val;
                    objEntityClass.MagneticCardNumber = MagCard_vehicle;

                    SharedPreferences pref1 = getSharedPreferences("LFVersionInfo", 0);
                    LFVersion = pref1.getString("LFVersion", "");

                    SharedPreferences pref2 = getSharedPreferences("HFVersionInfo", 0);
                    HFVersion = pref2.getString("HFVersion", "");

                    if (HFVersion != "") {
                        if (IsHFUpdateServer.trim().equalsIgnoreCase("Y") || IsHFUpdateServer == "Y") {
                            objEntityClass.HFVersion = HFVersion;
                        } else {
                            objEntityClass.HFVersion = "";
                        }
                    } else {
                        objEntityClass.HFVersion = "";
                    }
                    if (LFVersion != "") {
                        if (IsLFUpdateServer.trim().equalsIgnoreCase("Y") || IsHFUpdateServer == "Y") {
                            objEntityClass.LFVersion = LFVersion;
                        } else {
                            objEntityClass.LFVersion = "";
                        }
                    } else {
                        objEntityClass.LFVersion = "";
                    }

                    /*objEntityClass.HFVersion = "tyuti";
                    objEntityClass.LFVersion = "lhjkh";*/

                    if (AppConstants.APDU_FOB_KEY.equalsIgnoreCase("")) {

                        Log.i(TAG, " vehicle EN Manually: " + vehicleNumber + "  Fob: " + AppConstants.APDU_FOB_KEY + " Barcode_val:" + Barcode_val);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Vehicle entered manually: " + vehicleNumber + "; Fob: " + AppConstants.APDU_FOB_KEY + "; Barcode_val:" + Barcode_val);
                    } else {
                        System.out.println(TAG + "Vehicle FOB No:" + AppConstants.APDU_FOB_KEY + "  VNo:" + vehicleNumber + " Barcode_val:" + Barcode_val);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Vehicle FOB No:" + AppConstants.APDU_FOB_KEY + "; VNo:" + vehicleNumber + "; Barcode_val:" + Barcode_val);
                    }


                    Gson gson = new Gson();
                    String jsonData = gson.toJson(objEntityClass);
                    String userEmail = CommonUtils.getCustomerDetails(AcceptVehicleActivity_new.this).PersonEmail;

                    System.out.println("jsonDatajsonDatajsonData" + jsonData);
                    //----------------------------------------------------------------------------------
                    String authString = "Basic " + AppConstants.convertStingToBase64(objEntityClass.IMEIUDID + ":" + userEmail + ":" + "CheckVehicleRequireOdometerEntryAndRequireHourEntry" + AppConstants.LANG_PARAM);

                    OkHttpClient client = new OkHttpClient();
                    client.setConnectTimeout(4, TimeUnit.SECONDS);
                    client.setReadTimeout(4, TimeUnit.SECONDS);
                    client.setWriteTimeout(4, TimeUnit.SECONDS);


                    RequestBody body = RequestBody.create(TEXT, jsonData);
                    Request request = new Request.Builder()
                            .url(AppConstants.webURL)
                            .post(body)
                            .addHeader("Authorization", authString)
                            .build();


                    Response response = null;
                    response = client.newCall(request).execute();
                    resp = response.body().string();

                    System.out.println("response server call one ------------------------" + resp);
                }

            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "ServerCallFirst  STE1 " + e);
                if (OfflineConstants.isOfflineAccess(AcceptVehicleActivity_new.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
                GetBackToWelcomeActivity();

            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "ServerCallFirst InBG Ex:" + e);
                if (OfflineConstants.isOfflineAccess(AcceptVehicleActivity_new.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }
            return resp;
        }


        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(String serverRes) {

            String VehicleNumber = "";
            try {

                if (serverRes != null && !serverRes.equals("")) {

                    JSONObject jsonObject = new JSONObject(serverRes);

                    String ResponceMessage = jsonObject.getString("ResponceMessage");

                    System.out.println("ResponceMessage.." + ResponceMessage);

                    if (ResponceMessage.equalsIgnoreCase("success")) {

                        btnSave.setClickable(false);
                        IsNewFobVar = true;

                        SharedPreferences sharedPrefODO = AcceptVehicleActivity_new.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

                        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
                        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
                        IsPersonnelPINRequireForHub = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequireForHub, "");
                        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");

                        IsOdoMeterRequire = jsonObject.getString("IsOdoMeterRequire");
                        IsExtraOther = jsonObject.getString("IsExtraOther");
                        ExtraOtherLabel = jsonObject.getString("ExtraOtherLabel");
                        String IsHoursRequire = jsonObject.getString("IsHoursRequire");
                        VehicleNumber = jsonObject.getString("VehicleNumber");
                        String PreviousOdo = jsonObject.getString("PreviousOdo");
                        String OdoLimit = jsonObject.getString("OdoLimit");
                        String OdometerReasonabilityConditions = jsonObject.getString("OdometerReasonabilityConditions");
                        String CheckOdometerReasonable = jsonObject.getString("CheckOdometerReasonable");
                        String PreviousHours = jsonObject.getString("PreviousHours");
                        String HoursLimit = jsonObject.getString("HoursLimit");
                        String LastTransactionFuelQuantity = jsonObject.getString("LastTransactionFuelQuantity").replace(",", ".");
                        if (LastTransactionFuelQuantity.trim().isEmpty() || LastTransactionFuelQuantity.equalsIgnoreCase("null")) {
                            LastTransactionFuelQuantity = "0";
                        }

                        editVehicleNumber.setText(VehicleNumber);
                        Log.i(TAG, "Server Response:" + VehicleNumber);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Server Response:" + VehicleNumber);

                        //Added code to fix Inalid vehicle on pin screen
                        if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
                            Constants.AccVehicleNumber_FS1 = VehicleNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
                            Constants.AccVehicleNumber = VehicleNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
                            Constants.AccVehicleNumber_FS3 = VehicleNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS4")) {
                            Constants.AccVehicleNumber_FS4 = VehicleNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS5")) {
                            Constants.AccVehicleNumber_FS5 = VehicleNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS6")) {
                            Constants.AccVehicleNumber_FS6 = VehicleNumber;
                        } else {
                            Log.i(TAG, "Something went wrong in hose selection");
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Something went wrong in hose selection");
                        }


                        SharedPreferences sharedPref = AcceptVehicleActivity_new.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(AppConstants.IsOdoMeterRequire, IsOdoMeterRequire);
                        editor.putString(AppConstants.IsExtraOther, IsExtraOther);
                        editor.putString(AppConstants.ExtraOtherLabel, ExtraOtherLabel);
                        editor.putString(AppConstants.IsHoursRequire, IsHoursRequire);
                        editor.putString("PreviousOdo", PreviousOdo);
                        editor.putString("OdoLimit", OdoLimit);
                        editor.putString("OdometerReasonabilityConditions", OdometerReasonabilityConditions);
                        editor.putString("CheckOdometerReasonable", CheckOdometerReasonable);
                        editor.putString("PreviousHours", PreviousHours);
                        editor.putString("HoursLimit", HoursLimit);
                        editor.putString("LastTransactionFuelQuantity", LastTransactionFuelQuantity);
                        editor.commit();


                        if (IsOdoMeterRequire.equalsIgnoreCase("True") && !IsGateHub.equalsIgnoreCase("True")) {

                            Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptOdoActivity.class);//AcceptPinActivity
                            startActivity(intent);

                        } else if (IsHoursRequire.equalsIgnoreCase("True") && !IsGateHub.equalsIgnoreCase("True")) {

                            Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptHoursAcitvity.class);
                            startActivity(intent);

                        } else if (IsExtraOther.equalsIgnoreCase("True") && !IsGateHub.equalsIgnoreCase("True")) {

                            Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptVehicleOtherInfo.class);
                            startActivity(intent);

                        } else if (IsPersonnelPINRequireForHub.equalsIgnoreCase("True")) {

                            Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptPinActivity_new.class);//AcceptPinActivity
                            startActivity(intent);

                        } else if (IsDepartmentRequire.equalsIgnoreCase("True") && !IsGateHub.equalsIgnoreCase("True")) {


                            Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptDeptActivity.class);
                            startActivity(intent);

                        } else if (IsOtherRequire.equalsIgnoreCase("True") && !IsGateHub.equalsIgnoreCase("True")) {

                            Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptOtherActivity.class);
                            startActivity(intent);

                        } else {
                            AppConstants.serverCallInProgressForVehicle = true;
                            VehicleValidationInProgress = true;
                            AcceptServiceCall asc = new AcceptServiceCall();
                            asc.activity = AcceptVehicleActivity_new.this;
                            asc.checkAllFields();
                        }

                    } else {

                        String ResponceText = jsonObject.getString("ResponceText");

                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Vehicle rejected. Error: " + ResponceText);

                        String ValidationFailFor = jsonObject.getString("ValidationFailFor");
                        String IsNewFob = jsonObject.getString("IsNewFob");

                        if (ResponceText.equalsIgnoreCase("New Barcode detected, please enter vehicle number.")) {

                            AcceptVehicleNumber();//Enable edittext field and Enter button
                            IsNewFobVar = false;

                            // AppConstants.APDU_FOB_KEY = "";
                            AppConstants.VehicleLocal_FOB_KEY = "";
                            tv_vehicle_no_below.setText(getResources().getString(R.string.EnterHeading) + " " + ScreenNameForVehicle + ":");

                            InputMethodManager inputMethodManager = (InputMethodManager) editVehicleNumber.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            editVehicleNumber.requestFocus();
                            inputMethodManager.showSoftInput(editVehicleNumber, 0);

                            if (IsVehicleHasFob.equalsIgnoreCase("true")) {
                                CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", ResponceText);
                            } else {
                                RestTimeoutVehicleScreen();
                                CommonUtils.showCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", ResponceText);
                            }


                        } else if (ValidationFailFor.equalsIgnoreCase("Pin")) {

                            //AppConstants.colorToastBigFont(AcceptVehicleActivity_new.this, ResponceText, Color.BLUE);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Vehicle Activity ValidationFor Pin" + ResponceText);

                            CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", ResponceText);

                            IsNewFobVar = true;
                            Thread.sleep(1000);
                            AppConstants.ClearEdittextFielsOnBack(AcceptVehicleActivity_new.this); //Clear EditText on move to welcome activity.
                            Intent intent = new Intent(AcceptVehicleActivity_new.this, WelcomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        } else if (IsNewFob.equalsIgnoreCase("Yes")) {

                            AcceptVehicleNumber();//Enable edittext field and Enter button
                            IsNewFobVar = false;

                            // AppConstants.APDU_FOB_KEY = "";
                            AppConstants.VehicleLocal_FOB_KEY = "";
                            tv_vehicle_no_below.setText(getResources().getString(R.string.EnterHeading) + " " + ScreenNameForVehicle + ":");

                            InputMethodManager inputMethodManager = (InputMethodManager) editVehicleNumber.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            editVehicleNumber.requestFocus();
                            inputMethodManager.showSoftInput(editVehicleNumber, 0);

                            if (IsVehicleHasFob.equalsIgnoreCase("true")) {
                                CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", ResponceText);
                            } else {
                                RestTimeoutVehicleScreen();
                                CommonUtils.showCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", ResponceText);
                            }

                        } else {

                            //Here Onresume and Appconstants.APDU_FOB_KEY uncomment
                            IsNewFobVar = true;
                            btnSave.setEnabled(true);
                            AppConstants.APDU_FOB_KEY = "";
                            Barcode_val = "";
                            MagCard_vehicle = "";
                            onResume();
                            //Empty Fob key & enable edit text and Enter button
                            // AppConstants.FOB_KEY_VEHICLE = "";
                            editVehicleNumber.setEnabled(true);
                            editVehicleNumber.setFocusable(true);
                            tv_vehicle_no_below.setText(getResources().getString(R.string.EnterHeading) + " " + ScreenNameForVehicle + ":");
                            RestTimeoutVehicleScreen();
                            CommonUtils.showCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", ResponceText);
                        }

                    }

                } else {
                    //Empty Fob key & enable edit text and Enter button
                    // AppConstants.APDU_FOB_KEY = "";
                    AppConstants.VehicleLocal_FOB_KEY = "";
                    editVehicleNumber.setEnabled(true);
                    editVehicleNumber.setFocusable(true);
                    btnSave.setEnabled(true);

                    AppConstants.AUTH_CALL_SUCCESS = false;
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Internet Connection: " + cd.isConnectingToInternet());
                    if (OfflineConstants.isOfflineAccess(AcceptVehicleActivity_new.this)) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "ServerCallFirst  Temporary loss of cell service ~Switching to offline mode!!");
                        checkVehicleOFFLINEvalidation(hmapSwitchOffline);
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Offline Access not granted to this HUB.");
                        //CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", "Unable to connect server");
                    }
                }

                if (!VehicleValidationInProgress) {
                    pd.dismiss();
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "ServerCallFirst OnPost Exception: " + e);
                if (OfflineConstants.isOfflineAccess(AcceptVehicleActivity_new.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }

            }

        }
    }

    public void CallSaveButtonFunctionality() {

        AppConstants.VehicleLocal_FOB_KEY = "";

        try {
            String V_Number = editVehicleNumber.getText().toString().trim();
            //////////common for online offline///////////////////////////////
            HashMap<String, String> hmap = new HashMap<>();
            if (!IsNonValidateVehicle.equalsIgnoreCase("True")) {
                hmap = controller.getVehicleDetailsByVehicleNumber(V_Number);
            }
            hmapSwitchOffline = hmap;
            offlineVehicleInitialization(hmap);

            if (!V_Number.isEmpty() || !AppConstants.APDU_FOB_KEY.isEmpty() || !Barcode_val.isEmpty() || !MagCard_vehicle.isEmpty()) {

                if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {
                    if (!isFinishing()) {
                        new ServerCallFirst().execute();
                    }
                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Internet Connection: " + cd.isConnectingToInternet() + "; NETWORK_STRENGTH: " + AppConstants.NETWORK_STRENGTH);
                    AppConstants.AUTH_CALL_SUCCESS = false;
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Offline Vehicle No.: " + V_Number);

                    if (OfflineConstants.isOfflineAccess(AcceptVehicleActivity_new.this)) {
                        checkVehicleOFFLINEvalidation(hmap);
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Offline Access not granted to this HUB.");
                        //CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", "Unable to connect server");
                    }

                }

            } else {
                //Empty Fob key & enable edit text and Enter button
                // AppConstants.APDU_FOB_KEY = "";
                AppConstants.VehicleLocal_FOB_KEY = "";
                if (IsVehicleHasFob.equalsIgnoreCase("true")) {
                    //editVehicleNumber.setEnabled(false);
                } else {
                    //editVehicleNumber.setEnabled(true);
                }

                btnSave.setEnabled(true);
                if (mDisableFOBReadingForVehicle.equalsIgnoreCase("y")) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Please enter " + ScreenNameForVehicle + ". If you still have issues, please contact your Manager.");
                    CommonUtils.showCustomMessageDilaog(AcceptVehicleActivity_new.this, "Error Message", getResources().getString(R.string.peVehicle).replace("Vehicle", ScreenNameForVehicle));
                    //showMessageDilaog
                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Please enter " + ScreenNameForVehicle + " or present an Access Device. If you still have issues, please contact your Manager.");
                    CommonUtils.showCustomMessageDilaog(AcceptVehicleActivity_new.this, "Error Message", getResources().getString(R.string.peVehicleOrAccessDevice).replace("Vehicle", ScreenNameForVehicle));
                }
            }


        } catch (Exception ex) {
            ex.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "CallSaveButtonFunctionality Ex:" + ex);
        }
    }

    public void NoServerCall() {

        AppConstants.VehicleLocal_FOB_KEY = "";

        try {
            SharedPreferences sharedPrefODO = AcceptVehicleActivity_new.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

            IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
            IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
            IsPersonnelPINRequireForHub = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequireForHub, "");
            IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
            IsOdoMeterRequire = sharedPrefODO.getString(AppConstants.IsOdoMeterRequire, "");
            IsHoursRequire = sharedPrefODO.getString(AppConstants.IsHoursRequire, "");
            IsExtraOther = sharedPrefODO.getString(AppConstants.IsExtraOther, "");
            String V_Number = editVehicleNumber.getText().toString().trim();
            HashMap<String, String> hmap = new HashMap<>();
            if (!IsNonValidateVehicle.equalsIgnoreCase("True")) {
                hmap = controller.getVehicleDetailsByVehicleNumber(V_Number);
            }
            offlineVehicleInitialization(hmap);

            if (!V_Number.isEmpty() || !AppConstants.APDU_FOB_KEY.isEmpty() || !Barcode_val.isEmpty() || !MagCard_vehicle.isEmpty()) {

                if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {
                    //Move to next screen
                    if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
                        //pinNumber = Constants.AccPersonnelPIN_FS1;
                        Constants.AccVehicleNumber_FS1 = V_Number;


                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
                        //pinNumber = Constants.AccPersonnelPIN;
                        Constants.AccVehicleNumber = V_Number;

                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
                        //pinNumber = Constants.AccPersonnelPIN_FS3;
                        Constants.AccVehicleNumber_FS3 = V_Number;

                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS4")) {
                        Constants.AccVehicleNumber_FS4 = V_Number;
                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS5")) {
                        Constants.AccVehicleNumber_FS5 = V_Number;
                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS6")) {
                        Constants.AccVehicleNumber_FS6 = V_Number;
                    }

                    if (IsOdoMeterRequire.equalsIgnoreCase("True") && !IsGateHub.equalsIgnoreCase("True")) {

                        Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptOdoActivity.class);//AcceptPinActivity
                        startActivity(intent);

                    } else if (IsHoursRequire.equalsIgnoreCase("True") && !IsGateHub.equalsIgnoreCase("True")) {

                        Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptHoursAcitvity.class);
                        startActivity(intent);

                    } else if (IsExtraOther.equalsIgnoreCase("True") && !IsGateHub.equalsIgnoreCase("True")) {

                        Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptVehicleOtherInfo.class);
                        startActivity(intent);

                    } else if (IsPersonnelPINRequireForHub.equalsIgnoreCase("True")) {

                        Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptPinActivity_new.class);//AcceptPinActivity
                        startActivity(intent);

                    } else if (IsDepartmentRequire.equalsIgnoreCase("True") && !IsGateHub.equalsIgnoreCase("True")) {


                        Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptDeptActivity.class);
                        startActivity(intent);

                    } else if (IsOtherRequire.equalsIgnoreCase("True") && !IsGateHub.equalsIgnoreCase("True")) {

                        Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptOtherActivity.class);
                        startActivity(intent);

                    } else {
                        AppConstants.serverCallInProgressForVehicle = true;
                        VehicleValidationInProgress = true;
                        AcceptServiceCall asc = new AcceptServiceCall();
                        asc.activity = AcceptVehicleActivity_new.this;
                        asc.checkAllFields();
                    }


                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Internet Connection: " + cd.isConnectingToInternet() + "; NETWORK_STRENGTH: " + AppConstants.NETWORK_STRENGTH);
                    //offline---------------
                    AppConstants.AUTH_CALL_SUCCESS = false;
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Offline Vehicle No.: " + V_Number);

                    if (OfflineConstants.isOfflineAccess(AcceptVehicleActivity_new.this)) {
                        checkVehicleOFFLINEvalidation(hmap);
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Offline Access not granted to this HUB.");
                        //CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", "Unable to connect server");
                    }

                }

            } else {
                //Empty Fob key & enable edit text and Enter button
                // AppConstants.APDU_FOB_KEY = "";
                AppConstants.VehicleLocal_FOB_KEY = "";
                if (IsVehicleHasFob.equalsIgnoreCase("true")) {
                    //editVehicleNumber.setEnabled(false);
                } else {
                    //editVehicleNumber.setEnabled(true);
                }

                btnSave.setEnabled(true);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Please enter " + ScreenNameForVehicle + " or use fob key.");
                CommonUtils.showMessageDilaog(AcceptVehicleActivity_new.this, "Error Message", getResources().getString(R.string.peVehicleOrUseFobKey).replace("Vehicle", ScreenNameForVehicle));
            }


        } catch (Exception ex) {
            ex.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "NoServerCall Ex:" + ex);
        }

    }

    public class GetVehicleNuOnFobKeyDetection extends AsyncTask<Void, Void, String> {


        //ProgressDialog pd;
        String resp = "";

        @Override
        protected void onPreExecute() {

            String text = getResources().getString(R.string.PleaseWait);
            SpannableStringBuilder biggerText = new SpannableStringBuilder(text);
            biggerText.setSpan(new RelativeSizeSpan(2.00f), 0, text.length(), 0);
            Toast.makeText(getApplicationContext(), biggerText, Toast.LENGTH_LONG).show();

        }

        protected String doInBackground(Void... arg0) {

            try {
                String vehicleNumber = "";
                String pinNumber = "";

                VehicleRequireEntity objEntityClass = new VehicleRequireEntity();
                objEntityClass.IMEIUDID = AppConstants.getIMEI(AcceptVehicleActivity_new.this);
                objEntityClass.VehicleNumber = vehicleNumber;
                objEntityClass.WifiSSId = AppConstants.LAST_CONNECTED_SSID;
                objEntityClass.SiteId = Integer.parseInt(AppConstants.SITE_ID);
                objEntityClass.PersonnelPIN = pinNumber;
                objEntityClass.RequestFromAPP = "AP";
                objEntityClass.FOBNumber = AppConstants.APDU_FOB_KEY;
                objEntityClass.IsVehicleNumberRequire = IsVehicleNumberRequire;
                objEntityClass.Barcode = Barcode_val;
                objEntityClass.MagneticCardNumber = MagCard_vehicle;

                SharedPreferences pref1 = getSharedPreferences("LFVersionInfo", 0);
                LFVersion = pref1.getString("LFVersion", "");

                SharedPreferences pref2 = getSharedPreferences("HFVersionInfo", 0);
                HFVersion = pref2.getString("HFVersion", "");

                if (HFVersion != "") {
                    if (IsHFUpdateServer.trim().equalsIgnoreCase("Y") || IsHFUpdateServer == "Y") {
                        objEntityClass.HFVersion = HFVersion;
                    } else {
                        objEntityClass.HFVersion = "";
                    }
                } else {
                    objEntityClass.HFVersion = "";
                }
                if (LFVersion != "") {
                    if (IsLFUpdateServer.trim().equalsIgnoreCase("Y") || IsLFUpdateServer == "Y") {
                        objEntityClass.LFVersion = LFVersion;
                    } else {
                        objEntityClass.LFVersion = "";
                    }
                } else {
                    objEntityClass.LFVersion = "";
                }

                /*objEntityClass.HFVersion = "asdfg";
                objEntityClass.LFVersion = "sgdhfg";*/

                Log.i(TAG, " vehicle FOB No:" + AppConstants.APDU_FOB_KEY + " VNo:" + vehicleNumber + " Barcode value:" + Barcode_val + "MagCard_vehicle:" + MagCard_vehicle);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Vehicle FOB No:" + AppConstants.APDU_FOB_KEY + "; VNo:" + vehicleNumber + "; Barcode_val:" + Barcode_val + "; MagCard_vehicle:" + MagCard_vehicle);

                Gson gson = new Gson();
                String jsonData = gson.toJson(objEntityClass);
                String userEmail = CommonUtils.getCustomerDetails(AcceptVehicleActivity_new.this).PersonEmail;

                System.out.println("jsonDatajsonDatajsonData" + jsonData);
                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(objEntityClass.IMEIUDID + ":" + userEmail + ":" + "CheckVehicleRequireOdometerEntryAndRequireHourEntry" + AppConstants.LANG_PARAM);

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(10, TimeUnit.SECONDS);
                client.setReadTimeout(10, TimeUnit.SECONDS);
                client.setWriteTimeout(10, TimeUnit.SECONDS);


                RequestBody body = RequestBody.create(TEXT, jsonData);
                Request request = new Request.Builder()
                        .url(AppConstants.webURL)
                        .post(body)
                        .addHeader("Authorization", authString)
                        .build();


                Response response = null;
                response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "GetVehicleNuOnFobKeyDetection  STE1 " + e);
                GetBackToWelcomeActivity();

            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "GetVehicleNuOnFobKeyDetection DoInBG Ex:" + e.getMessage() + " ");
                if (OfflineConstants.isOfflineAccess(AcceptVehicleActivity_new.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }
            return resp;
        }


        @Override
        protected void onPostExecute(String serverRes) {

            //pd.dismiss();
            try {

                if (serverRes != null && !serverRes.isEmpty()) {

                    JSONObject jsonObject = new JSONObject(serverRes);

                    String ResponceMessage = jsonObject.getString("ResponceMessage");
                    System.out.println("ResponceMessage...." + ResponceMessage);

                    if (ResponceMessage.equalsIgnoreCase("success")) {

                        IsOdoMeterRequire = jsonObject.getString("IsOdoMeterRequire");
                        IsExtraOther = jsonObject.getString("IsExtraOther");
                        ExtraOtherLabel = jsonObject.getString("ExtraOtherLabel");
                        IsHoursRequire = jsonObject.getString("IsHoursRequire");
                        String VehicleNumber = jsonObject.getString("VehicleNumber");
                        String PreviousOdo = jsonObject.getString("PreviousOdo");
                        String OdoLimit = jsonObject.getString("OdoLimit");
                        String OdometerReasonabilityConditions = jsonObject.getString("OdometerReasonabilityConditions");
                        String CheckOdometerReasonable = jsonObject.getString("CheckOdometerReasonable");
                        String PreviousHours = jsonObject.getString("PreviousHours");
                        String HoursLimit = jsonObject.getString("HoursLimit");
                        String LastTransactionFuelQuantity = jsonObject.getString("LastTransactionFuelQuantity").replace(",", ".");
                        if (LastTransactionFuelQuantity.trim().isEmpty() || LastTransactionFuelQuantity.equalsIgnoreCase("null")) {
                            LastTransactionFuelQuantity = "0";
                        }

                        SharedPreferences sharedPref = AcceptVehicleActivity_new.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(AppConstants.IsOdoMeterRequire, IsOdoMeterRequire);
                        editor.putString(AppConstants.IsExtraOther, IsExtraOther);
                        editor.putString(AppConstants.ExtraOtherLabel, ExtraOtherLabel);
                        editor.putString(AppConstants.IsHoursRequire, IsHoursRequire);
                        editor.putString("PreviousOdo", PreviousOdo);
                        editor.putString("OdoLimit", OdoLimit);
                        editor.putString("OdometerReasonabilityConditions", OdometerReasonabilityConditions);
                        editor.putString("CheckOdometerReasonable", CheckOdometerReasonable);
                        editor.putString("PreviousHours", PreviousHours);
                        editor.putString("HoursLimit", HoursLimit);
                        editor.putString("LastTransactionFuelQuantity", LastTransactionFuelQuantity);
                        editor.commit();

                        editVehicleNumber.setText(VehicleNumber);
                        Log.i(TAG, "Vehicle Number Returned by server: " + VehicleNumber);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Vehicle Number Returned by server: " + VehicleNumber);

                        //Added code to fix Inalid vehicle on pin screen
                        if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
                            Constants.AccVehicleNumber_FS1 = VehicleNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
                            Constants.AccVehicleNumber = VehicleNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
                            Constants.AccVehicleNumber_FS3 = VehicleNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS4")) {
                            Constants.AccVehicleNumber_FS4 = VehicleNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS5")) {
                            Constants.AccVehicleNumber_FS5 = VehicleNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS6")) {
                            Constants.AccVehicleNumber_FS6 = VehicleNumber;
                        } else {
                            Log.i(TAG, "Something went wrong in hose selection");
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Something went wrong in hose selection");
                        }


                        tv_vehicle_no_below.setText(ScreenNameForVehicle + ": " + VehicleNumber);
                        if (!AppConstants.APDU_FOB_KEY.isEmpty()) {
                            tv_fob_number.setText("Access Device No: " + AppConstants.APDU_FOB_KEY);
                        } else if (!Barcode_val.isEmpty()) {
                            tv_fob_number.setText("Barcode No: " + Barcode_val);
                        } else if (!MagCard_vehicle.isEmpty()) {
                            tv_fob_number.setText("MagCard_No: " + MagCard_vehicle);
                        }

                        Log.i("ps_Vechile no", "Step 1:" + VehicleNumber);

                        DisplayScreenFobReadSuccess();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                NoServerCall();
                                //CallSaveButtonFunctionality();//Press Enter fun
                            }
                        }, 1500);


                    } else {

                        String ResponceText = jsonObject.getString("ResponceText");
                        String ValidationFailFor = jsonObject.getString("ValidationFailFor");
                        String IsNewFob = jsonObject.getString("IsNewFob");

                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Vehicle Fob Read Fail. Error: " + ResponceText);

                       /* if (ValidationFailFor.equalsIgnoreCase("Pin")) {

                            AppConstants.colorToastBigFont(AcceptVehicleActivity_new.this, ResponceText, Color.BLUE);
                            Intent i = new Intent(AcceptVehicleActivity_new.this, WelcomeActivity.class);
                            startActivity(i);

                        } else if (ValidationFailFor.equalsIgnoreCase("invalidfob")) {

                            AppConstants.colorToastBigFont(AcceptVehicleActivity_new.this, ResponceText, Color.BLUE);
                            Intent i = new Intent(AcceptVehicleActivity_new.this, WelcomeActivity.class);
                            startActivity(i);

                        } else*/

                        //////////////////////////////

                        if (IsNonValidateVehicle.equalsIgnoreCase("True")) {  // && IsNewFob.equalsIgnoreCase("No")

                            if (MagCard_vehicle != null && !MagCard_vehicle.isEmpty()) {

                                AppConstants.NonValidateVehicle_FOB_KEY = MagCard_vehicle;

                                //Added code to fix Invalid vehicle on pin screen
                                if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
                                    Constants.AccVehicleNumber_FS1 = MagCard_vehicle;
                                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
                                    Constants.AccVehicleNumber = MagCard_vehicle;
                                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
                                    Constants.AccVehicleNumber_FS3 = MagCard_vehicle;
                                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS4")) {
                                    Constants.AccVehicleNumber_FS4 = MagCard_vehicle;
                                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS5")) {
                                    Constants.AccVehicleNumber_FS5 = MagCard_vehicle;
                                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS6")) {
                                    Constants.AccVehicleNumber_FS6 = MagCard_vehicle;
                                } else {
                                    Log.i(TAG, "Something went wrong in hose selection");
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "Something went wrong in hose selection");
                                }

                            } else if (AppConstants.APDU_FOB_KEY != null) {

                                String fob = AppConstants.APDU_FOB_KEY.replace(":", "");
                                AppConstants.NonValidateVehicle_FOB_KEY = fob;

                                //Added code to fix Inalid vehicle on pin screen
                                if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
                                    Constants.AccVehicleNumber_FS1 = fob;
                                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
                                    Constants.AccVehicleNumber = fob;
                                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
                                    Constants.AccVehicleNumber_FS3 = fob;
                                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS4")) {
                                    Constants.AccVehicleNumber_FS4 = fob;
                                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS5")) {
                                    Constants.AccVehicleNumber_FS5 = fob;
                                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS6")) {
                                    Constants.AccVehicleNumber_FS6 = fob;
                                } else {
                                    Log.i(TAG, "Something went wrong in hose selection");
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "Something went wrong in hose selection");
                                }

                            }

                            //tv_vehicle_no_below.setText(ScreenNameForVehicle + ": " + fob);
                            if (!AppConstants.APDU_FOB_KEY.isEmpty()) {
                                tv_fob_number.setText("Access Device No: " + AppConstants.APDU_FOB_KEY);
                            } else if (!Barcode_val.isEmpty()) {
                                tv_fob_number.setText("Barcode No: " + Barcode_val);
                            } else if (!MagCard_vehicle.isEmpty()) {
                                tv_fob_number.setText("MagCard_No: " + MagCard_vehicle);
                            }

                            DisplayScreenFobReadSuccess();

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    if (IsPersonnelPINRequireForHub.equalsIgnoreCase("True") && !IsGateHub.equalsIgnoreCase("True")) {

                                        Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptPinActivity_new.class);
                                        startActivity(intent);

                                    } else {
                                        AppConstants.serverCallInProgressForVehicle = true;
                                        VehicleValidationInProgress = true;
                                        AcceptServiceCall asc = new AcceptServiceCall();
                                        asc.activity = AcceptVehicleActivity_new.this;
                                        asc.checkAllFields();
                                    }
                                }
                            }, 1500);

                        } else if (IsNewFob.equalsIgnoreCase("Yes")) {


                            AcceptVehicleNumber();//Enable edittext field and Enter button
                            IsNewFobVar = false;

                            // AppConstants.APDU_FOB_KEY = "";
                            AppConstants.VehicleLocal_FOB_KEY = "";
                            tv_vehicle_no_below.setText(getResources().getString(R.string.EnterHeading) + " " + ScreenNameForVehicle + ":");

                            InputMethodManager inputMethodManager = (InputMethodManager) editVehicleNumber.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            editVehicleNumber.requestFocus();
                            inputMethodManager.showSoftInput(editVehicleNumber, 0);

                            if (IsVehicleHasFob.equalsIgnoreCase("true")) { //check this one..

                                int w = ActionBar.LayoutParams.WRAP_CONTENT;
                                int h = ActionBar.LayoutParams.WRAP_CONTENT;
                                LinearLayout.LayoutParams par = new LinearLayout.LayoutParams(w, h);
                                par.weight = 1;
                                par.rightMargin = 20;
                                btnCancel.setTextSize(18);
                                btnCancel.setLayoutParams(par);

                                btnSave.setVisibility(View.VISIBLE);
                                CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", ResponceText);
                            } else {
                                CommonUtils.showCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", ResponceText);
                            }
                            //Reset screen timeout
                            Istimeout_Sec = true;
                            TimeoutVehicleScreen();

                        } else {

                            if (IsGateHub.equalsIgnoreCase("True")) {
                                Istimeout_Sec = false;
                            } else {
                                Istimeout_Sec = true;
                            }
                            TimeoutVehicleScreen();
                            tv_enter_vehicle_no.setText("Invalid FOB or Unassigned FOB");
                            int widthi = ActionBar.LayoutParams.WRAP_CONTENT;
                            int heighti = ActionBar.LayoutParams.WRAP_CONTENT;
                            LinearLayout.LayoutParams parmsi = new LinearLayout.LayoutParams(widthi, heighti);
                            tv_enter_vehicle_no.setLayoutParams(parmsi);

                            tv_fob_number.setVisibility(View.GONE);
                            tv_fob_Reader.setVisibility(View.GONE);
                            tv_or.setVisibility(View.GONE);

                            // tv_vehicle_no_below.setVisibility(View.GONE);

                            tv_dont_have_fob.setVisibility(View.VISIBLE);
                            btnSave.setVisibility(View.VISIBLE);
                            //String content = "Enter your <br><b>" + ScreenNameForVehicle + "</b> in<br> the green box below";
                            String content = getResources().getString(R.string.EnterVehicleId).replace("vehicle", "<br><b>" + ScreenNameForVehicle + "</b><br>");

                            int width = ActionBar.LayoutParams.MATCH_PARENT;
                            int height = ActionBar.LayoutParams.WRAP_CONTENT;
                            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width, height);
                            parms.gravity = Gravity.CENTER;
                            editVehicleNumber.setLayoutParams(parms);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                tv_dont_have_fob.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
                                System.out.println(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
                            } else {
                                tv_dont_have_fob.setText(Html.fromHtml(content));
                                System.out.println(Html.fromHtml(content));
                            }

                            editVehicleNumber.setVisibility(View.VISIBLE);
                            Linear_layout_Save_back_buttons.setVisibility(View.VISIBLE);
                            // CommonUtils.showMessageDilaog(AcceptVehicleActivity.this, "Message", ResponceText);

                            //Here Onresume and Appconstants.APDU_FOB_KEY uncomment
                            IsNewFobVar = true;
                            btnSave.setEnabled(true);
                            AppConstants.APDU_FOB_KEY = "";
                            Barcode_val = "";
                            MagCard_vehicle = "";
                            onResume();
                            //Empty Fob key & enable edit text and Enter button
                            // AppConstants.FOB_KEY_VEHICLE = "";
                            editVehicleNumber.setEnabled(true);
                            editVehicleNumber.setFocusable(true);
                            tv_vehicle_no_below.setText(getResources().getString(R.string.EnterHeading) + " " + ScreenNameForVehicle + ":");
                            CommonUtils.showCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", ResponceText);


                        }


                        /*new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //NoServerCall();
                                // CallSaveButtonFunctionality();//Press Enter fun
                            }
                        }, 1000);*/

                    }

                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Internet Connection: " + cd.isConnectingToInternet());
                    if (OfflineConstants.isOfflineAccess(AcceptVehicleActivity_new.this)) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "GetVehicleNuOnFobKeyDetection  Temporary loss of cell service ~Switching to offline mode!!");
                        AppConstants.NETWORK_STRENGTH = false;
                    }

                    if (OfflineConstants.isOfflineAccess(AcceptVehicleActivity_new.this)) {
                        checkVehicleOFFLINEvalidation(hmapSwitchOffline);
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Offline Access not granted to this HUB.");
                        //CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", "Unable to connect server");
                    }


                }

            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "GetVehicleNuOnFobKeyDetection OnPost Ex:" + e.getMessage() + " ");
                if (OfflineConstants.isOfflineAccess(AcceptVehicleActivity_new.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
                GetBackToWelcomeActivity();
            }

        }
    }

    @Override
    public void onBackPressed() {

        // ActivityHandler.removeActivity(1);
        AppConstants.ClearEdittextFielsOnBack(AcceptVehicleActivity_new.this);
        Istimeout_Sec = false;
        AppConstants.APDU_FOB_KEY = "";
        Barcode_val = "";
        MagCard_vehicle = "";
        AppConstants.NonValidateVehicle_FOB_KEY = "";
        AppConstants.VehicleLocal_FOB_KEY = "";
        if (IsGateHub.equalsIgnoreCase("True")) {
            AppConstants.GoButtonAlreadyClicked = false;
        }
        finish();
    }

    public void TimeoutVehicleScreen() {

        SharedPreferences sharedPrefODO = AcceptVehicleActivity_new.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
        TimeOutinMinute = sharedPrefODO.getString(AppConstants.TimeOut, "1");

        screenTimeOut = Integer.parseInt(TimeOutinMinute) * 60000;
        System.out.println("ScreenOutTimeVehicle" + screenTimeOut);

        ScreenOutTimeVehicle = new Timer();
        ScreeTimerlist.add(ScreenOutTimeVehicle);
        TimerTask tttt = new TimerTask() {
            @Override
            public void run() {
                Log.i("TimeoutVehicleScreen", "Running..");
                if (Istimeout_Sec) {

                    try {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Istimeout_Sec = false;
                                AppConstants.ClearEdittextFielsOnBack(AcceptVehicleActivity_new.this);

                                // ActivityHandler.GetBacktoWelcomeActivity();
                                Intent i = new Intent(AcceptVehicleActivity_new.this, WelcomeActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                            }
                        });

                        CancelTimerScreenOut();
                    } catch (Exception e) {

                        e.printStackTrace();
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "TimeoutVehicleScreen Ex:" + e.getMessage() + " ");
                    }

                }

            }
        };
        ScreenOutTimeVehicle.schedule(tttt, screenTimeOut, 500);


    }

    private void RestTimeoutVehicleScreen() {


        CancelTimerScreenOut();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        TimeoutVehicleScreen();
    }

    public void DisplayScreenInit() {

        SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_FA_Data, Context.MODE_PRIVATE);
        boolean FAStatus = sharedPref.getBoolean(AppConstants.FAData, false);
        boolean BarcodeStatus = sharedPref.getBoolean(AppConstants.UseBarcode, false);

        if (FAStatus) {
            btnFStag.setVisibility(View.VISIBLE);
        } else {
            btnFStag.setVisibility(View.GONE);
        }

        if (BarcodeStatus) {
            btn_barcode.setVisibility(View.VISIBLE);
        } else {
            btn_barcode.setVisibility(View.GONE);
        }


        if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {
            SharedPreferences sharedPrefODO = AcceptVehicleActivity_new.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
            IsVehicleHasFob = sharedPrefODO.getString(AppConstants.ISVehicleHasFob, "false");
        } else {
            IsVehicleHasFob = controller.getOfflineHubDetails(AcceptVehicleActivity_new.this).VehiclehasFOB;

            if (IsVehicleHasFob.trim().equalsIgnoreCase("y"))
                IsVehicleHasFob = "true";

            IsNonValidateVehicle = controller.getOfflineHubDetails(AcceptVehicleActivity_new.this).IsNonValidateVehicle;
            IsNonValidateODOM = controller.getOfflineHubDetails(AcceptVehicleActivity_new.this).IsNonValidateODOM;
        }

        if (AllowAccessDeviceORManualEntryForVehicle.equalsIgnoreCase("true")) {

            mDisableFOBReadingForVehicle = "N";
            tv_enter_vehicle_no.setVisibility(View.GONE);
            int widthii = 0;
            int heightii = 0;
            LinearLayout.LayoutParams parmsii = new LinearLayout.LayoutParams(widthii, heightii);
            tv_enter_vehicle_no.setLayoutParams(parmsii);

            // AppConstants.APDU_FOB_KEY = "";
            AppConstants.VehicleLocal_FOB_KEY = "";
            tv_enter_vehicle_no.setVisibility(View.INVISIBLE);
            tv_vehicle_no_below.setVisibility(View.GONE);
            tv_fob_number.setVisibility(View.GONE);
            editVehicleNumber.setVisibility(View.VISIBLE);
            tv_fob_Reader.setVisibility(View.VISIBLE);
            tv_or.setVisibility(View.VISIBLE);
            tv_dont_have_fob.setVisibility(View.VISIBLE);
            Linear_layout_Save_back_buttons.setVisibility(View.VISIBLE);

            btnSave.setClickable(true);
            btnSave.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);

            int width = ActionBar.LayoutParams.MATCH_PARENT;
            int height = ActionBar.LayoutParams.WRAP_CONTENT;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
            params.gravity = Gravity.CENTER;
            editVehicleNumber.setLayoutParams(params);

        } else if (IsVehicleHasFob.equalsIgnoreCase("true")) { //IsNewFobVar
            tv_enter_vehicle_no.setText("Present Access Device to reader");
            int widthi = ActionBar.LayoutParams.WRAP_CONTENT;
            int heighti = 0;
            LinearLayout.LayoutParams parmsi = new LinearLayout.LayoutParams(widthi, heighti);
            tv_enter_vehicle_no.setLayoutParams(parmsi);

            tv_fob_Reader.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.GONE);
            btnSave.setClickable(false);

            tv_or.setVisibility(View.GONE);
            tv_dont_have_fob.setVisibility(View.GONE);
            tv_vehicle_no_below.setVisibility(View.GONE);
            tv_fob_number.setVisibility(View.GONE);
            Linear_layout_Save_back_buttons.setVisibility(View.VISIBLE);

            int width = ActionBar.LayoutParams.MATCH_PARENT;//0; ////temp
            int height = 0;
            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width, height);
            editVehicleNumber.setLayoutParams(parms);
            editVehicleNumber.setText("");

            if (!BarcodeStatus) {
                int w = ActionBar.LayoutParams.MATCH_PARENT;
                int h = ActionBar.LayoutParams.WRAP_CONTENT;
                LinearLayout.LayoutParams par = new LinearLayout.LayoutParams(w, h);
                par.topMargin = 700;
                btnCancel.setTextSize(18);
                btnCancel.setLayoutParams(par);
            }

            hideKeybord();


        } else {

            tv_enter_vehicle_no.setVisibility(View.GONE);
            int widthii = 0;
            int heightii = 0;
            LinearLayout.LayoutParams parmsii = new LinearLayout.LayoutParams(widthii, heightii);
            tv_enter_vehicle_no.setLayoutParams(parmsii);

            // AppConstants.APDU_FOB_KEY = "";
            AppConstants.VehicleLocal_FOB_KEY = "";
            tv_enter_vehicle_no.setVisibility(View.INVISIBLE);
            tv_vehicle_no_below.setVisibility(View.GONE);
            tv_fob_number.setVisibility(View.GONE);
            editVehicleNumber.setVisibility(View.VISIBLE);
            tv_fob_Reader.setVisibility(View.VISIBLE);
            tv_or.setVisibility(View.VISIBLE);
            tv_dont_have_fob.setVisibility(View.VISIBLE);
            Linear_layout_Save_back_buttons.setVisibility(View.VISIBLE);

            btnSave.setClickable(true);
            btnSave.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);

            int width = ActionBar.LayoutParams.MATCH_PARENT;
            int height = ActionBar.LayoutParams.WRAP_CONTENT;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
            params.gravity = Gravity.CENTER;
            editVehicleNumber.setLayoutParams(params);

        }

        if (mDisableFOBReadingForVehicle.equalsIgnoreCase("y")) {
            tv_fob_Reader.setVisibility(View.GONE);
            tv_or.setVisibility(View.GONE);
        }

    }

    public void DisplayScreenFobReadSuccess() {

        tv_enter_vehicle_no.setText("");//Access Device read successfully
        tv_enter_vehicle_no.setVisibility(View.VISIBLE);
        int widthi = ActionBar.LayoutParams.WRAP_CONTENT;
        int heighti = ActionBar.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams parmsi = new LinearLayout.LayoutParams(widthi, heighti);
        tv_enter_vehicle_no.setLayoutParams(parmsi);

        tv_fob_number.setVisibility(View.GONE);//VISIBLE
        tv_vehicle_no_below.setVisibility(View.VISIBLE);
        tv_dont_have_fob.setVisibility(View.GONE);
        editVehicleNumber.setVisibility(View.GONE);
        Linear_layout_Save_back_buttons.setVisibility(View.GONE);
        tv_fob_Reader.setVisibility(View.GONE);
        btn_barcode.setVisibility(View.GONE);
        btnFStag.setVisibility(View.GONE);
        tv_or.setVisibility(View.GONE);

    }

    public void hideKeybord() {


        imm = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    public void showKeybord() {

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void AcceptVehicleNumber() {


        //Enable EditText
        int width = ActionBar.LayoutParams.MATCH_PARENT;
        int height = ActionBar.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.gravity = Gravity.CENTER;
        editVehicleNumber.setLayoutParams(params);

        //Enable Enter Button
        btnSave.setClickable(true);
        btnSave.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);
        editVehicleNumber.setVisibility(View.VISIBLE);
        editVehicleNumber.setEnabled(true);
        editVehicleNumber.setFocusable(true);


    }

    public String GetClosestBleDevice() {

        String BleName = "", BleMacAddress = "";
        Integer BleRssi = null;

        if (ListOfBleDevices.size() != 0) {

            for (int i = 0; i < ListOfBleDevices.size(); i++) {

                Integer bleValue = Integer.valueOf(ListOfBleDevices.get(i).get("BleRssi"));

                if (BleRssi == null || BleRssi < bleValue) {
                    BleRssi = bleValue;
                    BleName = ListOfBleDevices.get(i).get("BleName");
                    BleMacAddress = ListOfBleDevices.get(i).get("BleMacAddress");
                }

            }

        } else {
            Log.i(TAG, "Near-by BLE list empty");
        }


        return BleMacAddress;
    }

    public class GetVehicleByFSTagMacAddress extends AsyncTask<Void, Void, String> {


        ProgressDialog pd;

        @Override
        protected void onPreExecute() {

            String s = getResources().getString(R.string.PleaseWait);
            SpannableString ss2 = new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            pd = new ProgressDialog(AcceptVehicleActivity_new.this);
            pd.setMessage(ss2);
            pd.setCancelable(true);
            pd.setCancelable(false);
            pd.show();

        }

        protected String doInBackground(Void... Void) {
            String resp = "";


            try {

                final UpgradeVersionEntity objEntityClass = new UpgradeVersionEntity();
                objEntityClass.IMEIUDID = AppConstants.getIMEI(AcceptVehicleActivity_new.this);
                objEntityClass.Email = CommonUtils.getCustomerDetails(AcceptVehicleActivity_new.this).PersonEmail;
                objEntityClass.FSTagMacAddress = FSTagMacAddress;

                Gson gson = new Gson();
                String jsonData = gson.toJson(objEntityClass);
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(AcceptVehicleActivity_new.this) + ":" + CommonUtils.getCustomerDetails(AcceptVehicleActivity_new.this).PersonEmail + ":" + "GetVehicleByFSTagMacAddress" + AppConstants.LANG_PARAM);


                //resp = serverHandler.PostTextData(WelcomeActivity.this, AppConstants.webURL, parm2, authString);
                //----------------------------------------------------------------------------------
                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(4, TimeUnit.SECONDS);
                client.setReadTimeout(4, TimeUnit.SECONDS);
                client.setWriteTimeout(4, TimeUnit.SECONDS);

                RequestBody body = RequestBody.create(ServerHandler.TEXT, jsonData);
                Request request = new Request.Builder()
                        .url(AppConstants.webURL)
                        .post(body)
                        .addHeader("Authorization", authString)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

                //------------------------------

            } catch (SocketTimeoutException ex) {
                ex.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "GetVehicleByFSTagMacAddress  STE1 " + ex);
                if (OfflineConstants.isOfflineAccess(AcceptVehicleActivity_new.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
                GetBackToWelcomeActivity();

            } catch (Exception e) {
                pd.dismiss();
                System.out.println("Ex" + e.getMessage());
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "GetVehicleByFSTagMacAddress doInBackground --Exception " + e);
                if (OfflineConstants.isOfflineAccess(AcceptVehicleActivity_new.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }


            return resp;
        }


        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            System.out.println("GetVehicleByFSTagMacAddress...." + result);
            if (result != null && !result.isEmpty()) {


                try {

                    JSONObject jsonObjectSite = null;
                    jsonObjectSite = new JSONObject(result);

                    String ResponseMessageSite = jsonObjectSite.getString(AppConstants.RES_MESSAGE);
                    String ResponceText = jsonObjectSite.getString(AppConstants.RES_TEXT);
                    if (ResponseMessageSite.equalsIgnoreCase("success")) {

                        String VehicleNumber = jsonObjectSite.getString("VehicleNumber");
                        String FOBNumber = jsonObjectSite.getString("FOBNumber");
                        editVehicleNumber.setText(VehicleNumber);

                        Log.i(TAG, "Vehicle Number Returned by server -fstag: " + VehicleNumber);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Set vehicle Number -fstag: " + VehicleNumber);
                        //Added code to fix Inalid vehicle on pin screen
                        if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
                            Constants.AccVehicleNumber_FS1 = VehicleNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
                            Constants.AccVehicleNumber = VehicleNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
                            Constants.AccVehicleNumber_FS3 = VehicleNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS4")) {
                            Constants.AccVehicleNumber_FS4 = VehicleNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS5")) {
                            Constants.AccVehicleNumber_FS5 = VehicleNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS6")) {
                            Constants.AccVehicleNumber_FS6 = VehicleNumber;
                        } else {
                            Log.i(TAG, "Something went wrong in hose selection -fstag");
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Something went wrong in hose selection -fstag");
                        }
                        //AppConstants.colorToastBigFont(AcceptVehicleActivity_new.this, "VehicleNumber: "+VehicleNumber, Color.GREEN);

                        FstagCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", ScreenNameForVehicle + " Found: " + VehicleNumber);


                    } else {
                        RestTimeoutVehicleScreen();
                        CommonUtils.showCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", ResponceText);
                        //AppConstants.colorToastBigFont(AcceptVehicleActivity_new.this, ResponceText, Color.BLUE);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Log.i(TAG, "GetVehicleByFSTagMacAddress Server Response Empty!");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "GetVehicleByFSTagMacAddress  Server Response Empty!");
            }


        }

    }

    public void FstagCustomMessageDilaog(final Activity context, String title, String message) {

        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();

                        CallSaveButtonFunctionality();
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }
                }
        );

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();

                        InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }
                }
        );
        androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void InCaseOfGatehub() {

        btnSave.setClickable(false);
        IsNewFobVar = true;

        SharedPreferences sharedPrefODO = AcceptVehicleActivity_new.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsPersonnelPINRequireForHub = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequireForHub, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
        IsHoursRequire = sharedPrefODO.getString(AppConstants.IsHoursRequire, "");
        IsExtraOther = sharedPrefODO.getString(AppConstants.IsExtraOther, "");


        if (IsOdoMeterRequire.equalsIgnoreCase("True") && !IsGateHub.equalsIgnoreCase("True")) {

            Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptOdoActivity.class);//AcceptPinActivity
            startActivity(intent);

        } else if (IsHoursRequire.equalsIgnoreCase("True") && !IsGateHub.equalsIgnoreCase("True")) {

            Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptHoursAcitvity.class);
            startActivity(intent);

        } else if (IsExtraOther.equalsIgnoreCase("True") && !IsGateHub.equalsIgnoreCase("True")) {

            Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptVehicleOtherInfo.class);
            startActivity(intent);

        } else if (IsPersonnelPINRequireForHub.equalsIgnoreCase("True")) {

            Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptPinActivity_new.class);//AcceptPinActivity
            startActivity(intent);

        } else if (IsDepartmentRequire.equalsIgnoreCase("True") && !IsGateHub.equalsIgnoreCase("True")) {


            Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptDeptActivity.class);
            startActivity(intent);

        } else if (IsOtherRequire.equalsIgnoreCase("True") && !IsGateHub.equalsIgnoreCase("True")) {

            Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptOtherActivity.class);
            startActivity(intent);

        } else {
            AppConstants.serverCallInProgressForVehicle = true;
            AcceptServiceCall asc = new AcceptServiceCall();
            asc.activity = AcceptVehicleActivity_new.this;
            asc.checkAllFields();
        }

    }

    public void GetBackToWelcomeActivity() {


        //AppConstants.colorToast(getApplicationContext(), "Something went wrong, Please try again", Color.BLUE);

        Istimeout_Sec = false;
        AppConstants.ClearEdittextFielsOnBack(AcceptVehicleActivity_new.this);

        Intent i = new Intent(AcceptVehicleActivity_new.this, WelcomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

    }

    public class TagReaderFun extends AsyncTask<Void, Void, String> {


        ProgressDialog pd;

        @Override
        protected void onPreExecute() {

            String s = getResources().getString(R.string.PleaseWait);
            SpannableString ss2 = new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            pd = new ProgressDialog(AcceptVehicleActivity_new.this);
            pd.setMessage(ss2);
            pd.setCancelable(true);
            pd.setCancelable(false);
            pd.show();

        }

        protected String doInBackground(Void... arg0) {
            String resp = "";

            AcceptVehicleActivity_new.ListOfBleDevices.clear();
            if (checkBluetoothStatus()) {

                Intent intent = new Intent(AcceptVehicleActivity_new.this, FStagScannerService.class);
                bindService(intent, AcceptVehicleActivity_new.this, BIND_AUTO_CREATE);
                mHandler.post(mPruneTask);
            }

            Log.i(TAG, "ListOfBleDevices2:" + ListOfBleDevices.size());

            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return resp;
        }


        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            try {

                //StopScanning
                mHandler.removeCallbacks(mPruneTask);
                mService.setBeaconEventListener(null);
                unbindService(AcceptVehicleActivity_new.this);

                //Get closest FSTag MacAddress
                FSTagMacAddress = GetClosestBleDevice();

                if (!FSTagMacAddress.isEmpty()) {

                    if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {
                        if (!isFinishing()) {
                            new GetVehicleByFSTagMacAddress().execute();
                        }
                    } else {
                        AppConstants.colorToastBigFont(AcceptVehicleActivity_new.this, getResources().getString(R.string.CheckInternet), Color.BLUE);
                    }


                } else {
                    RestTimeoutVehicleScreen();
                    //Toast.makeText(mBluetoothLeServiceVehicle, "FStagMac Address Not found", Toast.LENGTH_SHORT).show();
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "FStagMac Address Not found");
                    CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", "FStagMac Address Not found");
                    Log.i(TAG, "FStagMac Address Empty");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /* This task checks for beacons we haven't seen in awhile */
    // private Handler mHandler = new Handler();
    private Runnable mPruneTask = new Runnable() {
        @Override
        public void run() {
            final ArrayList<SampleBeacon> expiredBeacons = new ArrayList<>();
            final long now = System.currentTimeMillis();
          /*  for (SampleBeacon beacon : mAdapterItems) {
                long delta = now - beacon.lastDetectedTimestamp;
                if (delta >= EXPIRE_TIMEOUT) {
                    expiredBeacons.add(beacon);
                }
            }*/

            if (!expiredBeacons.isEmpty()) {
                Log.d(TAG, "Found " + expiredBeacons.size() + " expired");
                /*mAdapterItems.removeAll(expiredBeacons);
                mAdapter.notifyDataSetChanged();*/
            }

            mHandler.postDelayed(this, EXPIRE_TASK_PERIOD);
        }
    };

    /* Verify Bluetooth Support */
    private boolean checkBluetoothStatus() {
        BluetoothManager manager =
                (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();
        /*
         * We need to enforce that Bluetooth is first enabled, and take the
         * user to settings to enable it if they have not done so.
         */
        if (adapter == null || !adapter.isEnabled()) {
            //Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return false;
        }

        /*
         * Check for Bluetooth LE Support.  In production, our manifest entry will keep this
         * from installing on these devices, but this will allow test devices or other
         * sideloads to report whether or not the feature exists.
         */
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            RestTimeoutVehicleScreen();
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        return true;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        Log.d(TAG, "Connected to scanner service");
        mService = ((FStagScannerService.LocalBinder) service).getService();
        mService.setBeaconEventListener(this);

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    public void saveButtonAction(View view) {
        CallSaveButtonFunctionality();
    }

    @Override
    public void onBeaconIdentifier(String deviceAddress, int rssi, String instanceId) {

        Log.i(TAG, "got beacon");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {

                    Barcode_val = data.getStringExtra("Barcode").trim();
                    AppConstants.colorToast(AcceptVehicleActivity_new.this, "Barcode Read: " + Barcode_val, Color.BLACK);
                    Log.d(TAG, "Barcode read: " + data.getStringExtra("Barcode").trim());
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Vehicle Barcode read success: " + Barcode_val);

                    HashMap<String, String> hmap = new HashMap<>();
                    if (!IsNonValidateVehicle.equalsIgnoreCase("True")) {
                        hmap = controller.getVehicleDetailsByBarcodeNumber(Barcode_val);
                    }
                    hmapSwitchOffline = hmap;
                    offlineVehicleInitialization(hmap);

                    if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {
                        if (!isFinishing()) {
                            new GetVehicleNuOnFobKeyDetection().execute();
                        }
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Internet Connection: " + cd.isConnectingToInternet() + "; NETWORK_STRENGTH: " + AppConstants.NETWORK_STRENGTH);
                        //offline---------------
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Offline Barcode Read: " + Barcode_val);

                        if (OfflineConstants.isOfflineAccess(AcceptVehicleActivity_new.this)) {
                            checkVehicleOFFLINEvalidation(hmap);
                        } else {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Offline Access not granted to this HUB.");
                            //CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", "Unable to connect server");
                        }

                    }

                } else {

                    Barcode_val = "";
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                Barcode_val = "";
                Log.d(TAG, "barcode captured failed");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void checkVehicleOFFLINEvalidation(HashMap<String, String> hmap) {

        if (hmap.size() > 0) {

            String RequireOdometerEntry = hmap.get("RequireOdometerEntry");//: "Y",
            String RequireHours = hmap.get("RequireHours");//: "N",
            String AllowedLinks = hmap.get("AllowedLinks");//: "36,38,41",
            String Active = hmap.get("Active");//: "Y"
            String IsExtraOther = hmap.get("IsExtraOther");
            String ExtraOtherLabel = hmap.get("ExtraOtherLabel");
            String FuelLimitPerDay = hmap.get("FuelLimitPerDay");
            String CheckFuelLimitPerMonth = hmap.get("CheckFuelLimitPerMonth");
            String FuelLimitPerMonth = hmap.get("FuelLimitPerMonth");

            offlineVehicleInitialization(hmap);

            SharedPreferences sharedPref = AcceptVehicleActivity_new.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(AppConstants.IsExtraOther, IsExtraOther);
            editor.putString(AppConstants.ExtraOtherLabel, ExtraOtherLabel);
            editor.commit();

            if (Active != null) {
                if (Active.trim().toLowerCase().equalsIgnoreCase("y")) {

                    try {
                        if (FuelLimitPerDay != null) {
                            double vehicleLimitPerDay = Double.parseDouble(FuelLimitPerDay);
                            if (vehicleLimitPerDay > 0) {
                                double remainingLimitPerDay = OfflineConstants.getVehicleFuelLimitPerDay(AcceptVehicleActivity_new.this);
                                if (remainingLimitPerDay <= 0) {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "This vehicle has exceeded the fuel limit for the day.");
                                    CommonUtils.showCustomMessageDilaog(AcceptVehicleActivity_new.this, "Error", getResources().getString(R.string.VehiclePerDayLimitExceeded).replace("Vehicle", ScreenNameForVehicle));
                                    return;
                                }
                            }
                        }

                        if (CheckFuelLimitPerMonth != null) {
                            if (CheckFuelLimitPerMonth.trim().equalsIgnoreCase("true")) {
                                if (FuelLimitPerMonth != null) {
                                    double vehicleLimitPerMonth = Double.parseDouble(FuelLimitPerMonth);
                                    if (vehicleLimitPerMonth > 0) {
                                        double remainingLimitPerMonth = OfflineConstants.getFuelLimitPerMonth(AcceptVehicleActivity_new.this);
                                        if (remainingLimitPerMonth <= 0) {
                                            if (AppConstants.GenerateLogs)
                                                AppConstants.WriteinFile(TAG + "This vehicle has exceeded the fuel limit for the month.");
                                            CommonUtils.showCustomMessageDilaog(AcceptVehicleActivity_new.this, "Error", getResources().getString(R.string.VehiclePerMonthLimitExceeded).replace("Vehicle", ScreenNameForVehicle));
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    if (!AllowedLinks.isEmpty() || AllowedLinks.contains(",")) {
                        boolean isAllowed = false;

                        String parts[] = AllowedLinks.split(",");
                        for (String allowedId : parts) {
                            if (AppConstants.R_SITE_ID.equalsIgnoreCase(allowedId)) {
                                isAllowed = true;
                                break;
                            }
                        }

                        /////////////////

                        if (isAllowed) {

                            if (RequireOdometerEntry.trim().toLowerCase().equalsIgnoreCase("y")) {
                                Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptOdoActivity.class);
                                startActivity(intent);
                            } else if (RequireHours.trim().toLowerCase().equalsIgnoreCase("y")) {
                                Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptHoursAcitvity.class);
                                startActivity(intent);
                            } else if (IsExtraOther.trim().toLowerCase().equalsIgnoreCase("True")) {
                                Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptVehicleOtherInfo.class);
                                startActivity(intent);
                            } else {
                                EntityHub obj = controller.getOfflineHubDetails(AcceptVehicleActivity_new.this);
                                if (obj.PersonnelPINNumberRequired.equalsIgnoreCase("Y")) {
                                    Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptPinActivity_new.class);//AcceptPinActivity
                                    startActivity(intent);
                                } else if (obj.IsDepartmentRequire.equalsIgnoreCase("true") && !obj.HUBType.equalsIgnoreCase("G")) {
                                    Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptDeptActivity.class);
                                    startActivity(intent);
                                } else if (obj.IsOtherRequire.equalsIgnoreCase("True") && !obj.HUBType.equalsIgnoreCase("G")) {
                                    Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptOtherActivity.class);
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(AcceptVehicleActivity_new.this, DisplayMeterActivity.class);
                                    startActivity(intent);
                                }
                            }
                        } else {
                            AppConstants.VehicleLocal_FOB_KEY = "";
                            AppConstants.APDU_FOB_KEY = "";
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Vehicle is not allowed for selected Link");
                            CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", ScreenNameForVehicle + " is not allowed for selected Link");
                        }

                    }
                } else {
                    AppConstants.VehicleLocal_FOB_KEY = "";
                    AppConstants.APDU_FOB_KEY = "";
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Vehicle is not active");
                    CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", ScreenNameForVehicle + " is not active");
                }
            }
        } else {

            String validateVehicles = "Yes";
            if (IsNonValidateVehicle.equalsIgnoreCase("True")) {
                validateVehicles = "No";
            }
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Validate vehicles => " + validateVehicles);
            if (IsNonValidateVehicle.equalsIgnoreCase("True")) {
                String RequireOdometerEntry = IsNonValidateODOM;
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Offline RequireOdometerEntry: " + RequireOdometerEntry);

                if (RequireOdometerEntry.trim().equalsIgnoreCase("True")) {
                    Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptOdoActivity.class);
                    startActivity(intent);
                } else {
                    EntityHub obj = controller.getOfflineHubDetails(AcceptVehicleActivity_new.this);
                    if (obj.PersonnelPINNumberRequired.equalsIgnoreCase("Y")) {
                        Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptPinActivity_new.class);//AcceptPinActivity
                        startActivity(intent);
                    } else if (obj.IsDepartmentRequire.equalsIgnoreCase("true") && !obj.HUBType.equalsIgnoreCase("G")) {
                        Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptDeptActivity.class);
                        startActivity(intent);
                    } else if (obj.IsOtherRequire.equalsIgnoreCase("True") && !obj.HUBType.equalsIgnoreCase("G")) {
                        Intent intent = new Intent(AcceptVehicleActivity_new.this, AcceptOtherActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(AcceptVehicleActivity_new.this, DisplayMeterActivity.class);
                        startActivity(intent);
                    }
                }

            } else {
                if (AppConstants.APDU_FOB_KEY != null && !AppConstants.APDU_FOB_KEY.isEmpty()) {
                    String fob = AppConstants.APDU_FOB_KEY.replace(":", "");
                    HashMap<String, String> PinMap = controller.getPersonnelDetailsByFOBnumber(fob);

                    if (PinMap.size() > 0) {
                        //Pin fob please present vehicle fob
                        String msg = "This is " + ScreenNameForPersonnel + " Access Device. Please use your " + ScreenNameForVehicle + " Access Device";
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Vehicle Number not found in offline db. Access Device (" + fob + ") is personnel access device.");
                        CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", msg);

                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Vehicle Number not found in offline db");
                        CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", "Invalid Access Device");
                    }

                } else if (editVehicleNumber.getText().toString().trim() != null && !editVehicleNumber.getText().toString().trim().isEmpty()) {

                    String pin = editVehicleNumber.getText().toString().trim();
                    HashMap<String, String> PinMap1 = controller.getPersonnelDetailsByPIN(pin);

                    if (PinMap1.size() > 0) {
                        //Pin Number please use vehicle Number
                        String msg = "This is " + ScreenNameForPersonnel + ". Please use your " + ScreenNameForVehicle + ".";
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Vehicle Number not found in offline db. This (" + pin + ") is pin number.");
                        CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", msg);

                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Vehicle Number not found in offline db.");
                        CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", "Invalid " + ScreenNameForVehicle);
                    }
                }
            }

            AppConstants.VehicleLocal_FOB_KEY = "";
            AppConstants.APDU_FOB_KEY = "";
            Barcode_val = "";
            MagCard_vehicle = "";
            onResume();
        }

    }

    private void offlineVehicleInitialization(HashMap<String, String> hmap) {
        try {
            if (hmap.size() > 0) {
                String VehicleId = hmap.get("VehicleId"); //: 249,
                String VehicleNumber = hmap.get("VehicleNumber"); //: "600",
                String CurrentOdometer = hmap.get("CurrentOdometer");//: 1,
                String CurrentHours = hmap.get("CurrentHours");//: 0,
                String RequireOdometerEntry = hmap.get("RequireOdometerEntry");//: "Y",
                String RequireHours = hmap.get("RequireHours");//: "N",
                String FuelLimitPerTxn = hmap.get("FuelLimitPerTxn");//: 0,
                String FuelLimitPerDay = hmap.get("FuelLimitPerDay");//: 0,
                String FOBNumber = hmap.get("FOBNumber");//: "",
                String MagneticCardReaderNumber = hmap.get("MagneticCardReaderNumber");//: "",
                String AllowedLinks = hmap.get("AllowedLinks");//: "36,38,41",
                String Active = hmap.get("Active");//: "Y"
                String IsExtraOther = hmap.get("IsExtraOther");
                String ExtraOtherLabel = hmap.get("ExtraOtherLabel");
                String CheckOdometerReasonable = hmap.get("CheckOdometerReasonable");
                String OdometerReasonabilityConditions = hmap.get("OdometerReasonabilityConditions");
                String OdoLimit = hmap.get("OdoLimit");
                String HoursLimit = hmap.get("HoursLimit");
                String CheckFuelLimitPerMonth = hmap.get("CheckFuelLimitPerMonth");
                String FuelLimitPerMonth = hmap.get("FuelLimitPerMonth");
                String FuelQuantityOfVehiclePerMonth = hmap.get("FuelQuantityOfVehiclePerMonth");

                OfflineConstants.storeCurrentTransaction(AcceptVehicleActivity_new.this, "", "", VehicleId, "", "", "", "", "", VehicleNumber, "", "", "");

                OfflineConstants.storeFuelLimit(AcceptVehicleActivity_new.this, VehicleId, FuelLimitPerTxn, FuelLimitPerDay, CheckFuelLimitPerMonth, FuelLimitPerMonth, FuelQuantityOfVehiclePerMonth, "", "", "");

                AppConstants.OFF_VEHICLE_ID = VehicleId;
                AppConstants.OFF_ODO_REQUIRED = RequireOdometerEntry;
                AppConstants.OFF_HOUR_REQUIRED = RequireHours;
                AppConstants.OFF_CURRENT_ODO = CurrentOdometer;
                AppConstants.OFF_CURRENT_HOUR = CurrentHours;

                AppConstants.OFF_ODO_Reasonable = CheckOdometerReasonable;
                AppConstants.OFF_ODO_Conditions = OdometerReasonabilityConditions;
                AppConstants.OFF_ODO_Limit = OdoLimit;
                AppConstants.OFF_HRS_Limit = HoursLimit;

                if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
                    Constants.AccVehicleNumber_FS1 = VehicleNumber;
                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
                    Constants.AccVehicleNumber = VehicleNumber;
                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
                    Constants.AccVehicleNumber_FS3 = VehicleNumber;
                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS4")) {
                    Constants.AccVehicleNumber_FS4 = VehicleNumber;
                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS5")) {
                    Constants.AccVehicleNumber_FS5 = VehicleNumber;
                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS6")) {
                    Constants.AccVehicleNumber_FS6 = VehicleNumber;
                }
            } else {

                String VehicleNumber = "";
                try {
                    String V_Number = editVehicleNumber.getText().toString();
                    if (!V_Number.isEmpty()) {
                        VehicleNumber = V_Number;
                    } else if (!AppConstants.APDU_FOB_KEY.isEmpty()) {
                        VehicleNumber = AppConstants.APDU_FOB_KEY;
                    } else if (!Barcode_val.isEmpty()) {
                        VehicleNumber = Barcode_val;
                    } else if (!MagCard_vehicle.isEmpty()) {
                        VehicleNumber = MagCard_vehicle;
                    }
                } catch (Exception ex) {
                    VehicleNumber = "";
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Exception while getting entered vehicle number. " + ex.getMessage());
                }

                if (IsNonValidateVehicle.equalsIgnoreCase("True")) {
                    AppConstants.OFF_VEHICLE_ID = "0";
                    AppConstants.OFF_HOUR_REQUIRED = "N";

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Vehicle Number (Non-validate): " + VehicleNumber);

                    if (!VehicleNumber.isEmpty()) {
                        OfflineConstants.storeCurrentTransaction(AcceptVehicleActivity_new.this, "", "", "", "", "", "", "", "", VehicleNumber, "", "", "");

                        if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
                            Constants.AccVehicleNumber_FS1 = VehicleNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
                            Constants.AccVehicleNumber = VehicleNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
                            Constants.AccVehicleNumber_FS3 = VehicleNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS4")) {
                            Constants.AccVehicleNumber_FS4 = VehicleNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS5")) {
                            Constants.AccVehicleNumber_FS5 = VehicleNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS6")) {
                            Constants.AccVehicleNumber_FS6 = VehicleNumber;
                        }
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Vehicle Number is empty.");
                    }
                } else {
                    if (!cd.isConnectingToInternet()) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Vehicle (" + VehicleNumber + ") not found in offline db.");
                    }
                }
            }
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Exception in offlineVehicleInitialization. " + ex.getMessage());
        }

    }

    /*private void CheckForFirmwareUpgrade() {

        //LINK UPGRADE
        if (AppConstants.UP_Upgrade) {

            //Check for /FSBin folder if not create one
            //File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "FSBin");
            String binFolderPath = String.valueOf(getApplicationContext().getExternalFilesDir(AppConstants.FOLDER_BIN));
            File folder = new File(binFolderPath);
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdirs();
            }

            *//*if (BTConstants.CurrentTransactionIsBT) {
                AppConstants.UP_Upgrade_File_name = "BT_" + AppConstants.UP_Upgrade_File_name;
            }*//*
            String LocalPath = binFolderPath + "/" + AppConstants.UP_Upgrade_File_name;

            File f = new File(LocalPath);
            if (f.exists()) {
                Log.e(TAG, "Link upgrade firmware file already exist. Skip download");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Link upgrade firmware file (" + AppConstants.UP_Upgrade_File_name + ") already exist. Skip download");
            } else {
                if (AppConstants.UP_FilePath != null) {
                    //new BackgroundServiceDownloadFirmware.DownloadLinkAndReaderFirmware().execute(AppConstants.UP_FilePath, AppConstants.UP_Upgrade_File_name, "UP_Upgrade");
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Downloading link upgrade firmware file (" + AppConstants.UP_Upgrade_File_name + ")");
                    new DownloadFileFromURL().execute(AppConstants.UP_FilePath, binFolderPath, AppConstants.UP_Upgrade_File_name);
                } else {
                    Log.e(TAG, "Link upgrade File path null");
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Link upgrade File path null");
                }
            }
        }
    }*/

    /*public class DownloadFileFromURL extends AsyncTask<String, String, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(AcceptVehicleActivity_new.this);
            String message = getResources().getString(R.string.FileDownloadInProgress) + "\n" + getResources().getString(R.string.PleaseWaitSeveralSeconds);
            SpannableString ss2 = new SpannableString(message);
            ss2.setSpan(new RelativeSizeSpan(1.2f), 0, ss2.length(), 0);
            pd.setMessage(ss2);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // getting file length
                int lenghtOfFile = connection.getContentLength();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file
                OutputStream output = new FileOutputStream(f_url[1] + "/" + f_url[2]);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pd.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String file_url) {
            pd.dismiss();
        }
    }*/

    private class sendBleVersionData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String resp = "";

            System.out.println("Inside sendBleVersionData");
            try {
                OkHttpClient client = new OkHttpClient();
                MediaType TEXT = MediaType.parse("application/text;charset=UTF-8");

                RequestBody body = RequestBody.create(TEXT, strings[0]);
                Request request = new Request.Builder()
                        .url(AppConstants.webURL)
                        .post(body)
                        .addHeader("Authorization", strings[1])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();
                System.out.println("Inside sendBleVersionData response-----" + resp);

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
                if (OfflineConstants.isOfflineAccess(AcceptVehicleActivity_new.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }


            return resp;
        }
    }

    @SuppressLint("ResourceAsColor")
    private void UpdateReaderStatusToUI() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                boolean ReaderStatusUI = false;

                //LF reader status on UI
                if (mDeviceName.length() > 0 && !mDeviceAddress.isEmpty() && mDisableFOBReadingForVehicle.equalsIgnoreCase("N")) {
                    if (!Constants.LF_ReaderStatus.equalsIgnoreCase(LFReaderStatus)) {
                        LFReaderStatus = Constants.LF_ReaderStatus;
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "<LF Reader Status: " + LFReaderStatus + ">");
                    }
                    if (Constants.LF_ReaderStatus.equals("LF Disconnected") && !AppConstants.showReaderStatus) {
                        retryConnect();
                    }
                    if (AppConstants.showReaderStatus) {
                        ReaderStatusUI = true;
                        tv_lf_status.setVisibility(View.VISIBLE);
                        if (Constants.LF_ReaderStatus.equals("LF Connected") || Constants.LF_ReaderStatus.equals("LF Discovered")) {
                            tv_lf_status.setText(Constants.LF_ReaderStatus);
                            tv_lf_status.setTextColor(Color.parseColor("#4CAF50"));
                        } else {
                            retryConnect();
                            tv_lf_status.setText(Constants.LF_ReaderStatus);
                            tv_lf_status.setTextColor(Color.parseColor("#ff0000"));
                        }
                    }
                } else {
                    tv_lf_status.setVisibility(View.GONE);
                }

                //Hf reader status on UI
                if (HFDeviceName.length() > 0 && !HFDeviceAddress.isEmpty() && !AppConstants.ACS_READER && mDisableFOBReadingForVehicle.equalsIgnoreCase("N")) {
                    if (!Constants.HF_ReaderStatus.equalsIgnoreCase(HFReaderStatus)) {
                        HFReaderStatus = Constants.HF_ReaderStatus;
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "<HF Reader Status: " + HFReaderStatus + ">");
                    }
                    if (Constants.HF_ReaderStatus.equals("HF Disconnected") && !AppConstants.showReaderStatus) {
                        retryConnect();
                    }
                    if (AppConstants.showReaderStatus) {
                        ReaderStatusUI = true;
                        tv_hf_status.setVisibility(View.VISIBLE);
                        if (Constants.HF_ReaderStatus.equals("HF Connected") || Constants.HF_ReaderStatus.equals("HF Discovered")) {
                            tv_hf_status.setText(Constants.HF_ReaderStatus);
                            tv_hf_status.setTextColor(Color.parseColor("#4CAF50"));
                        } else {
                            retryConnect();
                            tv_hf_status.setText(Constants.HF_ReaderStatus);
                            tv_hf_status.setTextColor(Color.parseColor("#ff0000"));
                        }
                    }
                } else {
                    tv_hf_status.setVisibility(View.GONE);
                }

                //Magnetic reader status on UI
                if (mMagCardDeviceAddress.length() > 0 && !mMagCardDeviceAddress.isEmpty() && mDisableFOBReadingForVehicle.equalsIgnoreCase("N")) {
                    if (!Constants.Mag_ReaderStatus.equalsIgnoreCase(MagReaderStatus)) {
                        MagReaderStatus = Constants.Mag_ReaderStatus;
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "<Mag Reader Status: " + MagReaderStatus + ">");
                    }
                    if (Constants.Mag_ReaderStatus.equals("Mag Disconnected") && !AppConstants.showReaderStatus) {
                        retryConnect();
                    }
                    if (AppConstants.showReaderStatus) {
                        ReaderStatusUI = true;
                        tv_mag_status.setVisibility(View.VISIBLE);
                        if (Constants.Mag_ReaderStatus.equals("Mag Connected") || Constants.Mag_ReaderStatus.equals("Mag Discovered")) {
                            tv_mag_status.setText(Constants.Mag_ReaderStatus);
                            tv_mag_status.setTextColor(Color.parseColor("#4CAF50"));
                        } else {
                            retryConnect();
                            tv_mag_status.setText(Constants.Mag_ReaderStatus);
                            tv_mag_status.setTextColor(Color.parseColor("#ff0000"));
                        }
                    }
                } else {
                    tv_mag_status.setVisibility(View.GONE);
                }

                if (ReaderStatusUI) {
                    tv_reader_status.setText("Reader status: ");
                    layout_reader_status.setVisibility(View.VISIBLE);

                } else {
                    layout_reader_status.setVisibility(View.GONE);
                }

                // layout_reader_status.setVisibility(View.GONE);


            }
        });

    }

    private void UnRegisterBroadcastForReader() {

        try {

            if (ServiceCardReader_vehicle != null)
                unregisterReceiver(ServiceCardReader_vehicle);
            ServiceCardReader_vehicle = null;

            if (mDeviceName.length() > 0 && !mDeviceAddress.isEmpty() && mDisableFOBReadingForVehicle.equalsIgnoreCase("N"))
                stopService(new Intent(AcceptVehicleActivity_new.this, ServiceLFCard.class));

            if (HFDeviceName.length() > 0 && !HFDeviceAddress.isEmpty() && !AppConstants.ACS_READER && mDisableFOBReadingForVehicle.equalsIgnoreCase("N"))
                stopService(new Intent(AcceptVehicleActivity_new.this, ServiceHFCard.class));

            if (mMagCardDeviceAddress.length() > 0 && !mMagCardDeviceAddress.isEmpty() && mDisableFOBReadingForVehicle.equalsIgnoreCase("N") && !mMagCardDeviceName.contains("MAGCARD_READERV2"))
                stopService(new Intent(AcceptVehicleActivity_new.this, ServiceMagCard.class));


        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "UnRegisterBroadcastForReader Exception:" + e.toString());
        }


    }

    private void RegisterBroadcastForReader() {

        try {
            if (ServiceCardReader_vehicle == null) {

                ServiceCardReader_vehicle = new BroadcastMagCard_dataFromServiceToUI();
                IntentFilter intentSFilterVEHICLE = new IntentFilter("ServiceToActivityMagCard");
                registerReceiver(ServiceCardReader_vehicle, intentSFilterVEHICLE);

                if (mDeviceName.length() > 0 && !mDeviceAddress.isEmpty() && mDisableFOBReadingForVehicle.equalsIgnoreCase("N"))
                    startService(new Intent(AcceptVehicleActivity_new.this, ServiceLFCard.class));

                if (HFDeviceName.length() > 0 && !HFDeviceAddress.isEmpty() && !AppConstants.ACS_READER && mDisableFOBReadingForVehicle.equalsIgnoreCase("N"))
                    startService(new Intent(AcceptVehicleActivity_new.this, ServiceHFCard.class));

                if (mMagCardDeviceAddress.length() > 0 && !mMagCardDeviceAddress.isEmpty() && mDisableFOBReadingForVehicle.equalsIgnoreCase("N") && !mMagCardDeviceName.contains("MAGCARD_READERV2"))
                    startService(new Intent(AcceptVehicleActivity_new.this, ServiceMagCard.class));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class BroadcastMagCard_dataFromServiceToUI extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle notificationData = intent.getExtras();

            try {

                String Action = notificationData.getString("Action");
                if (Action.equals("HFReader")) {

                    String newData = notificationData.getString("HFCardValue");
                    System.out.println("HFCard data 001 veh----" + newData);
                    //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " " + Action + " Raw data:" + newData);
                    displayData_HF(newData);

                } else if (Action.equals("LFReader")) {

                    String newData = notificationData.getString("LFCardValue");
                    System.out.println("LFCard data 001 veH----" + newData);
                    // if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " " + Action + " Raw data:" + newData);
                    displayData_LF(newData);

                } else if (Action.equals("MagReader")) {

                    String newData = notificationData.getString("MagCardValue");
                    System.out.println("MagCard data 002~----" + newData);
                    //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " " + Action + " Raw data:" + newData);
                    MagCard_vehicle = "";
                    displayData_MagCard(newData);

                } else if (Action.equals("QRReader")) {
                    String newData = notificationData.getString("QRCodeValue");
                    System.out.println("QRCode data 002~----" + newData);
                    //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " " + Action + " Raw data:" + newData);
                    Barcode_val = "";
                    if (newData != null) {

                        Barcode_val = newData.trim();

                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "QR scan value: " + Barcode_val);
                        HashMap<String, String> hmap = new HashMap<>();
                        if (!IsNonValidateVehicle.equalsIgnoreCase("True")) {
                            hmap = controller.getVehicleDetailsByBarcodeNumber(Barcode_val);
                        }
                        hmapSwitchOffline = hmap;
                        offlineVehicleInitialization(hmap);

                        if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {
                            if (!isFinishing()) {

                                if (!Barcode_val.isEmpty()) {
                                    if (!AppConstants.serverCallInProgressForVehicle) {
                                        AppConstants.serverCallInProgressForVehicle = true;
                                        new GetVehicleNuOnFobKeyDetection().execute();
                                    } else {
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + "<Previous server call is in queue. Skipped QR validation.>");
                                    }
                                }
                            }
                        } else {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Internet Connection: " + cd.isConnectingToInternet() + "; NETWORK_STRENGTH: " + AppConstants.NETWORK_STRENGTH);
                            //offline---------------
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Offline Barcode Read: " + Barcode_val);

                            if (OfflineConstants.isOfflineAccess(AcceptVehicleActivity_new.this)) {
                                checkVehicleOFFLINEvalidation(hmap);
                            } else {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "Offline Access not granted to this HUB.");
                                //CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleActivity_new.this, "Message", "Unable to connect server");
                            }

                        }

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private class ReconnectBleReaders extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {

            try {

                UnRegisterBroadcastForReader();

                Thread.sleep(2000);

                RegisterBroadcastForReader();


            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.ServerCallLogs)
                    AppConstants.WriteinFile(TAG + "ReconnectBleReaders Exception: " + e.toString());
            }

            return null;
        }
    }

    private void checkFor10Seconds() {

        runOnUiThread(new Runnable() {
            public void run() {
                if (sec_count == 10) {

                    if (!Constants.HF_ReaderStatus.equals("HF Connected") && !HFDeviceAddress.isEmpty() && !AppConstants.ACS_READER && mDisableFOBReadingForVehicle.equalsIgnoreCase("N")) {
                        new ReconnectBleReaders().execute();
                        //Toast.makeText(getApplicationContext(), "Reconnecting HF reader please wait.."+sec_count, Toast.LENGTH_SHORT).show();
                    } else if (!Constants.LF_ReaderStatus.equals("LF Connected") && !mDeviceAddress.isEmpty() && mDisableFOBReadingForVehicle.equalsIgnoreCase("N")) {
                        new ReconnectBleReaders().execute();
                        //Toast.makeText(getApplicationContext(), "Reconnecting LF reader please wait.."+sec_count, Toast.LENGTH_SHORT).show();
                    } else if (!Constants.Mag_ReaderStatus.equals("Mag Connected") && !mMagCardDeviceAddress.isEmpty() && mDisableFOBReadingForVehicle.equalsIgnoreCase("N") && !mMagCardDeviceName.contains("MAGCARD_READERV2")) {
                        new ReconnectBleReaders().execute();
                        //Toast.makeText(getApplicationContext(), "Reconnecting Mag reader please wait.."+sec_count, Toast.LENGTH_SHORT).show();
                    } else {
                        //Toast.makeText(getApplicationContext(), "Reader connected " + sec_count, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    public void resetReaderStatus() {

        sec_count = 0;
        Constants.QR_ReaderStatus = "QR Waiting..";
        Constants.HF_ReaderStatus = "HF Waiting..";
        Constants.LF_ReaderStatus = "LF Waiting..";
        Constants.Mag_ReaderStatus = "Mag Waiting..";

    }

    private void CustomDilaogForRebootCmd(final Activity context, String title, String message) {

        final Dialog dialogBus = new Dialog(context);
        dialogBus.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogBus.setCancelable(false);
        dialogBus.setContentView(R.layout.custom_alertdialouge_debugwindow);
        dialogBus.show();

        EditText edt_code = (EditText) dialogBus.findViewById(R.id.edt_code);
        TextView edt_message = (TextView) dialogBus.findViewById(R.id.edt_message);
        Button btnAllow = (Button) dialogBus.findViewById(R.id.btnAllow);
        Button btnCancel = (Button) dialogBus.findViewById(R.id.btn_cancel);
        edt_message.setText(Html.fromHtml(title));

        btnAllow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String code = edt_code.getText().toString().trim();

                if (code != null && !code.isEmpty() && code.equals(AppConstants.AccessCode)) {
                    AppConstants.RebootHF_reader = true;
                    //Toast.makeText(AcceptVehicleActivity_new.this, "Done", Toast.LENGTH_SHORT).show();
                    dialogBus.dismiss();
                } else {
                    if (!code.equals(AppConstants.AccessCode)) {
                        Toast.makeText(AcceptVehicleActivity_new.this, "Code did not match. Please try again", Toast.LENGTH_SHORT).show();
                    }
                    dialogBus.dismiss();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (dialogBus.isShowing()) {
                    dialogBus.dismiss();
                }

            }
        });


        // Hide after some seconds
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (dialogBus.isShowing()) {
                    dialogBus.dismiss();
                }
            }
        };

        dialogBus.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.removeCallbacks(runnable);
            }
        });

        handler.postDelayed(runnable, 20000);

    }

    private void retryConnect() {

        if (sec_count > 20 && IsGateHub.equalsIgnoreCase("True") && !VehicleValidationInProgress) {
            AppConstants.WriteinFile(TAG + "Retrying to connect to the reader...");
            sec_count = 0;
            //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "HF Reader reconnection attempt:");
            recreate();
            //new ReconnectBleReaders().execute();
        }
    }

    private HashMap<String, String> getMagneticCardKey(String RawString1) {

        //RawString1 = "d36a4ca21c14ec10d67f20ffd76a4ca21c14ec10d67f20ffd36a4ca21c14ec10d67f20";
        HashMap<String, String> hmap = new HashMap<>();
        try {
            hmap = controller.getVehicleDetailsByMagNumber(RawString1);
            if (hmap.size() <= 0) {
                hmap = controller.getVehicleDetailsByMagNumber(GetStringNormalLogic(true, RawString1));
                if (hmap.size() <= 0) {
                    hmap = controller.getVehicleDetailsByMagNumber(GetStringNormalLogic(false, RawString1));
                    if (hmap.size() <= 0) {
                        return hmap;
                    }
                } else {
                    return hmap;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hmap;
    }

    public String GetStringNormalLogic(boolean normal, String RawString1) {

        String Str_one = "";
        boolean Isstart = false, Isend = false;
        StringBuilder sb = new StringBuilder();

        try {
            RawString1 = RawString1.toUpperCase();
            if (RawString1.contains("FF")) {

                String[] SplitedRawStr = RawString1.split("FF");
                if (SplitedRawStr.length > 1) {

                    String raw_str = "";
                    if (normal) {
                        raw_str = SplitedRawStr[1];
                    } else {
                        StringBuilder raw_reverse = new StringBuilder();
                        raw_reverse.append(SplitedRawStr[1]);
                        raw_reverse.reverse();
                        raw_str = raw_reverse.toString();
                    }

                    // print reversed String
                    System.out.println(raw_str);

                    for (char ch : raw_str.toCharArray()) {
                        String binlen = HexToBinary(String.valueOf(ch));
                        int len = binlen.length();
                        if (len == 4) {
                            sb.append(binlen);
                        } else {
                            binlen = leftPad(binlen, 4, "0");
                            sb.append(binlen);
                        }
                    }

                    Log.i(TAG, "Check binary data:" + sb);

                    //1101011011010100100110010100010000111000001010011101100000100001101011001111111001000000
                    String CardReaderNumberInHex = "";
                    AtomicInteger splitCounter = new AtomicInteger(0);
                    Collection<String> splittedStrings = sb.toString()
                            .chars()
                            .mapToObj(_char -> String.valueOf((char) _char))
                            .collect(Collectors.groupingBy(stringChar -> splitCounter.getAndIncrement() / 5
                                    , Collectors.joining()))
                            .values();

                    for (String str : splittedStrings) {

                        if (str.equals("11010")) {
                            Isstart = true;
                            continue;
                        } else if (str.equals("10110")) {
                            Isend = true;
                            break;
                        }

                        if (Isstart) {
                            String temp = "";
                            if (str.length() <= 4) {
                                temp = leftPad(str, 4, "0");
                            } else {
                                temp = str.substring(0, 4);
                            }

                            String reverse_temp = new StringBuilder(new String(temp)).reverse().toString();
                            String hex = binaryToHex(reverse_temp);
                            if (hex.equalsIgnoreCase(""))
                                hex = "0";
                            CardReaderNumberInHex = CardReaderNumberInHex + hex;

                        }

                    }

                    Str_one = CardReaderNumberInHex;

                } else {
                    Log.i(TAG, "Incomplete Raw string");
                }

            } else {
                Log.i(TAG, "Magnetic card raw string doesn't contain FF");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return Str_one;
    }

    String HexToBinary(String Hex) {
        String bin = new BigInteger(Hex, 16).toString(2);
        int inb = Integer.parseInt(bin);
        bin = String.format(Locale.getDefault(), "%08d", inb);
        return bin;
    }

    private String binaryToHex(String binary) {
        int decimalValue = 0;
        int length = binary.length() - 1;
        for (int i = 0; i < binary.length(); i++) {
            decimalValue += Integer.parseInt(binary.charAt(i) + "") * Math.pow(2, length);
            length--;
        }
        return decimalToHex(decimalValue);
    }

    private static String decimalToHex(int decimal) {
        String hex = "";
        while (decimal != 0) {
            int hexValue = decimal % 16;
            hex = toHexChar(hexValue) + hex;
            decimal = decimal / 16;
        }
        return hex;
    }

    private static char toHexChar(int hexValue) {
        if (hexValue <= 9 && hexValue >= 0)
            return (char) (hexValue + '0');
        else
            return (char) (hexValue - 10 + 'A');
    }

    public static String leftPad(String input, int length, String fill) {
        String pad = String.format("%" + length + "s", "").replace(" ", fill) + input.trim();
        return pad.substring(pad.length() - length, pad.length());
    }
}
