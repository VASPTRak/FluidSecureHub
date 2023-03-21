package com.TrakEngineering.FluidSecureHub;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
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
import com.TrakEngineering.FluidSecureHub.HFCardGAtt.ServiceHFCard;
import com.TrakEngineering.FluidSecureHub.LFCardGAtt.ServiceLFCard;
import com.TrakEngineering.FluidSecureHub.MagCardGAtt.ServiceMagCard;
import com.TrakEngineering.FluidSecureHub.enity.CheckPinFobEntity;
import com.TrakEngineering.FluidSecureHub.enity.VehicleRequireEntity;
import com.TrakEngineering.FluidSecureHub.offline.EntityHub;
import com.TrakEngineering.FluidSecureHub.offline.OffDBController;
import com.TrakEngineering.FluidSecureHub.offline.OfflineConstants;
import com.TrakEngineering.FluidSecureHub.server.ServerHandler;
import com.example.barcodeml.LivePreviewActivity;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
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
public class AcceptPinActivity_new extends AppCompatActivity {

    public int cnt123 = 0;

    OffDBController controller = new OffDBController(AcceptPinActivity_new.this);

    private NetworkReceiver receiver = new NetworkReceiver();
    private TextView tv_fobkey;
    private String mDisableFOBReadingForPin;
    private String mDeviceName;
    private String mDeviceAddress;
    private String mDeviceName_hf_trak;
    private String mDeviceAddress_hf_trak;
    private String HFDeviceName;
    private String HFDeviceAddress;
    private String mMagCardDeviceName;
    private String mMagCardDeviceAddress;
    private EditText etInput;
    String Barcode_pin_val = "", LF_FobKey = "", ScreenNameForPersonnel = "PERSONNEL", ScreenNameForVehicle = "VEHICLE", KeyboardType = "2", MagCard_personnel = "";
    int Count = 1, LF_ReaderConnectionCountPin = 0, sec_count = 0;
    private boolean barcodeReaderCall = true;

    private static final String TAG = "Pin_Activity ";
    public static double CurrentLat = 0, CurrentLng = 0;
    RelativeLayout footer_keybord;
    LinearLayout Linear_layout_Save_back_buttons;
    TextView tv_fob_Reader, tv_or, tv_return, tv_swipekeybord, tv_fob_number, tv_vehicle_no_below, tv_dont_have_fob, tv_enter_vehicle_no, tv_title;
    LinearLayout Linear_layout_vehicleNumber;
    boolean Istimeout_Sec = true;
    public AcceptPinActivity_new.BroadcastCardReader_dataFromServiceToUI ServiceCardReader_pin = null;
    //LinearLayout linearBarcode;
    Button btnSaveBar;
    EditText etPersonnelPin, etBarcode;
    private LinearLayout layout_reader_status;
    TextView tv_enter_pin_no, tv_ok, tv_hf_status, tv_lf_status, tv_mag_status, tv_reader_status;
    Button btnSave, btnCancel, btn_ReadFobAgain, btn_barcode;
    String IsPersonHasFob = "", IsOdoMeterRequire = "", IsDepartmentRequire = "", IsPersonnelPINRequire = "", IsOtherRequire = "", IsVehicleNumberRequire = "", IsStayOpenGate = "", IsGateHub, IsOffvehicleScreenRequired = "", IsPersonPinAndFOBRequire = "", AllowAccessDeviceORManualEntry = "";
    String TimeOutinMinute;
    Timer t, ScreenOutTime;
    String IsNonValidatePerson = "", IsNonValidateVehicle = "";

    ConnectionDetector cd = new ConnectionDetector(AcceptPinActivity_new.this);
    List<Timer> TimerList = new ArrayList<Timer>();
    List<Timer> ScreenTimerList = new ArrayList<Timer>();

    //BLE Upgrade
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
    boolean bleLFUpdateSuccessFlag = false;
    boolean bleHFUpdateSuccessFlag = false;
    boolean InScrverCall = false;

    String FOLDER_PATH_BLE = null;
    HashMap<String, String> hmapSwitchOfflinepin = new HashMap<>();
    String LFReaderStatus = "", HFReaderStatus = "", MagReaderStatus = "";
    public boolean PersonValidationInProgress = false;

    private static final int EXPIRE_TIMEOUT = 5000;
    private static final int EXPIRE_TASK_PERIOD = 1000;
    private static final int RC_BARCODE_CAPTURE = 9001;

    private void clearUI() {

        tv_enter_pin_no.setText("Please wait, processing");
        tv_fobkey.setText("");
        tv_fob_number.setText("");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control_pin);

        SharedPreferences sharedPre2 = AcceptPinActivity_new.this.getSharedPreferences("storeBT_FOBDetails", Context.MODE_PRIVATE);
        mDisableFOBReadingForPin = sharedPre2.getString("DisableFOBReadingForPin", "");
        mDeviceName = sharedPre2.getString("LFBluetoothCardReader", "");
        mDeviceAddress = sharedPre2.getString("LFBluetoothCardReaderMacAddress", "");
        HFDeviceName = sharedPre2.getString("BluetoothCardReader", "");
        HFDeviceAddress = sharedPre2.getString("BTMacAddress", "");
        mMagCardDeviceName = sharedPre2.getString("MagneticCardReader", ""); //
        mMagCardDeviceAddress = sharedPre2.getString("MagneticCardReaderMacAddress", ""); //

        mDeviceName_hf_trak = sharedPre2.getString("HFTrakCardReader", ""); //
        mDeviceAddress_hf_trak = sharedPre2.getString("HFTrakCardReaderMacAddress", ""); //
        AppConstants.ACS_READER = sharedPre2.getBoolean("ACS_Reader", false);

        SharedPreferences sharedPrefODO = AcceptPinActivity_new.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsPersonPinAndFOBRequire = sharedPrefODO.getString(AppConstants.IsPersonPinAndFOBRequire, "");
        AllowAccessDeviceORManualEntry = sharedPrefODO.getString(AppConstants.AllowAccessDeviceORManualEntry, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
        IsVehicleNumberRequire = sharedPrefODO.getString(AppConstants.IsVehicleNumberRequire, "");

        SharedPreferences sharedPrefGatehub = AcceptPinActivity_new.this.getSharedPreferences(Constants.PREF_COLUMN_GATE_HUB, Context.MODE_PRIVATE);
        IsGateHub = sharedPrefGatehub.getString(AppConstants.IsGateHub, "");
        IsStayOpenGate = sharedPrefGatehub.getString(AppConstants.IsStayOpenGate, "");

        PersonValidationInProgress = false;
        AppConstants.serverCallInProgressForPin = false;

        /* site id is mismatching
        SharedPreferences sharedPref = AcceptPinActivity_new.this.getSharedPreferences(Constants.PREF_COLUMN_SITE, Context.MODE_PRIVATE);
        String dataSite = sharedPref.getString(Constants.PREF_COLUMN_SITE, "");
        SITE_ID = parseSiteData(dataSite);
        AppConstants.SITE_ID = SITE_ID;
        */

        // Sets up UI references.
        tv_fobkey = (TextView) findViewById(R.id.tv_fobkey);
        etInput = (EditText) findViewById(R.id.etInput);

        getSupportActionBar().setTitle(AppConstants.BrandName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        InItGUI();

        SharedPreferences myPrefkb = this.getSharedPreferences(AppConstants.sharedPref_KeyboardType, 0);
        KeyboardType = myPrefkb.getString("KeyboardTypePerson", "2");
        ScreenNameForPersonnel = myPrefkb.getString("ScreenNameForPersonnel", "Personnel");
        ScreenNameForVehicle = myPrefkb.getString("ScreenNameForVehicle", "Vehicle");

        if (ScreenNameForPersonnel.trim().isEmpty())
            ScreenNameForPersonnel = "Personnel";

        //linearBarcode = (LinearLayout) findViewById(R.id.linearBarcode);
        btnSaveBar = (Button) findViewById(R.id.btnSaveBar);
        etBarcode = (EditText) findViewById(R.id.etBarcode);

        etPersonnelPin = (EditText) findViewById(R.id.etPersonnelPin);
        tv_fob_number = (TextView) findViewById(R.id.tv_fob_number);
        tv_enter_pin_no = (TextView) findViewById(R.id.tv_enter_pin_no);
        footer_keybord = (RelativeLayout) findViewById(R.id.footer_keybord);
        Linear_layout_Save_back_buttons = (LinearLayout) findViewById(R.id.Linear_layout_Save_back_buttons);
        tv_return = (TextView) findViewById(R.id.tv_return);
        tv_swipekeybord = (TextView) findViewById(R.id.tv_swipekeybord);
        tv_ok = (TextView) findViewById(R.id.tv_ok);
        tv_dont_have_fob = (TextView) findViewById(R.id.tv_dont_have_fob);//Enter your PERSONNEL ID in the green box below
        btn_ReadFobAgain = (Button) findViewById(R.id.btn_ReadFobAgain);
        btnSave = (Button) findViewById(R.id.btnSave);
        btn_barcode = (Button) findViewById(R.id.btn_barcode);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        tv_fob_Reader = (TextView) findViewById(R.id.tv_fob_Reader);
        tv_or = (TextView) findViewById(R.id.tv_or);

        tv_title.setText(getResources().getString(R.string.PersonnelIdentification).replace("PERSONNEL", ScreenNameForPersonnel.toUpperCase()));
        tv_fob_Reader.setText(getResources().getString(R.string.PresentPersonAccessDevice).replace("personnel", ScreenNameForPersonnel));
        //String content = "Enter your<br> <b>" + ScreenNameForPersonnel + "</b> in<br> the green box below";
        String content = getResources().getString(R.string.EnterPersonnelId).replace("PERSONNEL", "<br><b>" + ScreenNameForPersonnel + "</b><br>");
        etPersonnelPin.setText("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tv_dont_have_fob.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
            System.out.println(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tv_dont_have_fob.setText(Html.fromHtml(content));
            System.out.println(Html.fromHtml(content));
        }

        tv_enter_pin_no.setText(getResources().getString(R.string.PersonnelNumberHeading).replace("Number", "").replace("Personnel", ScreenNameForPersonnel));

        //BLE upgrade
        SharedPreferences myPrefslo = this.getSharedPreferences("BLEUpgradeInfo", 0);
        BLEType = myPrefslo.getString("BLEType", "");
        BLEFileLocation = myPrefslo.getString("BLEFileLocation", "");
        IsLFUpdate = myPrefslo.getString("IsLFUpdate", "");
        IsHFUpdate = myPrefslo.getString("IsHFUpdate", "");
        FOLDER_PATH_BLE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FSCardReader_" + BLEType + "/";

        if (!IsVehicleNumberRequire.equalsIgnoreCase("True")) {
            CheckForFirmwareUpgrade();
            CommonUtils.LogReaderDetails(AcceptPinActivity_new.this);
        }

        getSupportActionBar().setTitle(R.string.fs_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        etPersonnelPin.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boolean ps = isKeyboardShown(etPersonnelPin.getRootView());
                if (ps) {
                    footer_keybord.setEnabled(true);
                    footer_keybord.setVisibility(View.VISIBLE);
                } else {
                    footer_keybord.setEnabled(false);
                    footer_keybord.setVisibility(View.INVISIBLE);
                }

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeybord();
                onBackPressed();

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {


                String pin = etPersonnelPin.getText().toString().trim();
                String FKey = AppConstants.APDU_FOB_KEY;

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Entered PIN num: " + pin + "; Scanned Barcode:" + Barcode_pin_val);

                //////////common for online offline///////////////////////////////
                HashMap<String, String> hmap = new HashMap<>();

                if (pin != null && !pin.trim().isEmpty()) {
                    hmap = controller.getPersonnelDetailsByPIN(pin);
                    hmapSwitchOfflinepin = hmap;
                    offlinePersonInitialization(hmap);

                } else if (FKey != null && !FKey.trim().isEmpty()) {

                    String fob = AppConstants.APDU_FOB_KEY.replace(":", "");
                    hmap = controller.getPersonnelDetailsByFOBnumber(fob);
                    hmapSwitchOfflinepin = hmap;
                    offlinePersonInitialization(hmap);

                }
                ///////////////////////////////

                if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {

                    if (!MagCard_personnel.isEmpty() || !Barcode_pin_val.isEmpty()) {

                        if (cd.isConnectingToInternet()) {
                            if (!isFinishing()) {
                                // if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + " templog Onclick 1 ");
                                new GetPinNuOnFobKeyDetection().execute();
                            }
                        }


                    } else if (FKey.equalsIgnoreCase("")) {
                        if (cd.isConnectingToInternet()) {
                            if (!isFinishing()) {

                                String epin = etPersonnelPin.getText().toString().trim();
                                if (!epin.isEmpty()) {
                                    new CallSaveButtonFunctionality().execute();//Press Enter fun
                                } else {
                                    if (mDisableFOBReadingForPin.equalsIgnoreCase("Y")) {
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + "Please enter " + ScreenNameForPersonnel + ". If you still have issues, please contact your Manager.");
                                        CommonUtils.showCustomMessageDilaog(AcceptPinActivity_new.this, "Error Message", getResources().getString(R.string.pePersonnel).replace("Personnel", ScreenNameForPersonnel));
                                    } else {
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + "Please enter " + ScreenNameForPersonnel + " or present an Access Device. If you still have issues, please contact your Manager.");
                                        CommonUtils.showCustomMessageDilaog(AcceptPinActivity_new.this, "Error Message", getResources().getString(R.string.pePersonnelOrAccessDevice).replace("Personnel", ScreenNameForPersonnel));
                                    }
                                }
                            }
                        } else {
                            AppConstants.colorToastBigFont(getApplicationContext(), getResources().getString(R.string.CheckInternet), Color.BLUE);
                        }

                    } else if (pin.equalsIgnoreCase("") && !FKey.equalsIgnoreCase("")) {
                        if (cd.isConnectingToInternet()) {
                            if (!isFinishing()) {
                                //if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + " templog Onclick 2");
                                new GetPinNuOnFobKeyDetection().execute();
                            }
                        } else {
                            AppConstants.colorToastBigFont(getApplicationContext(), getResources().getString(R.string.CheckInternet), Color.BLUE);
                        }

                    } else if (!pin.equalsIgnoreCase("") && !FKey.equalsIgnoreCase("")) {

                        if (cd.isConnectingToInternet()) {
                            if (!isFinishing()) {
                                //if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + " templog Onclick 3 ");
                                new GetPinNuOnFobKeyDetection().execute();
                            }
                        } else {
                            AppConstants.colorToastBigFont(getApplicationContext(), getResources().getString(R.string.CheckInternet), Color.BLUE);
                        }
                    }
                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Internet Connection: " + cd.isConnectingToInternet() + "; NETWORK_STRENGTH: " + AppConstants.NETWORK_STRENGTH);
                    AppConstants.AUTH_CALL_SUCCESS = false;
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Offline Pin : " + pin);

                    if (OfflineConstants.isOfflineAccess(AcceptPinActivity_new.this)) {
                        //offline----------
                        if (!pin.isEmpty()) {
                            checkPINvalidation(hmap);
                        } else {
                            checkPINvalidation(hmap);
                        }
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Offline Access not granted to this HUB.");
                        //CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", "Unable to connect server");
                    }

                }
            }
        });

        btnSaveBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Barcode_pin_val = etBarcode.getText().toString().trim();
                etPersonnelPin.setText("");
                AppConstants.APDU_FOB_KEY = "";
                MagCard_personnel = "";

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Manually entered Barcode value: " + Barcode_pin_val);

                if (cd.isConnectingToInternet()) {
                    //if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + " templog Onclick btnSavebar ");
                    new GetPinNuOnFobKeyDetection().execute();
                }
            }
        });

        btn_barcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Intent intent = new Intent(AcceptPinActivity_new.this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
                intent.putExtra("ForScreen","Pin");
                startActivityForResult(intent, RC_BARCODE_CAPTURE);
                */

                Intent intent = new Intent(AcceptPinActivity_new.this, LivePreviewActivity.class);
                startActivityForResult(intent, RC_BARCODE_CAPTURE);
            }
        });

        try {
            etPersonnelPin.setInputType(Integer.parseInt(KeyboardType));
        } catch (Exception e) {
            System.out.println("keyboard exception");
            etPersonnelPin.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        tv_swipekeybord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int InputTyp = etPersonnelPin.getInputType();
                if (InputTyp == 2) {
                    etBarcode.setInputType(InputType.TYPE_CLASS_TEXT);
                    etPersonnelPin.setInputType(InputType.TYPE_CLASS_TEXT);
                    tv_swipekeybord.setText(getResources().getString(R.string.PressFor123));
                } else {

                    etBarcode.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_TEXT);
                    etPersonnelPin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_TEXT);
                    tv_swipekeybord.setText(getResources().getString(R.string.PressForABC));
                }

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
        barcodeReaderCall = true;
        InScrverCall = false;
        resetReaderStatus();//BLE reader status reset
        RegisterBroadcastForReader();//BroadcastReciver for MagCard,HF and LF Readers

        Count = 1;
        LF_ReaderConnectionCountPin = 0;
        //Toast.makeText(getApplicationContext(), "FOK_KEY" + AppConstants.APDU_FOB_KEY, Toast.LENGTH_SHORT).show();
        //showKeybord();
        etPersonnelPin.setText("");
        AppConstants.APDU_FOB_KEY = "";
        AppConstants.PinLocal_FOB_KEY = "";
        if (IsGateHub.equalsIgnoreCase("True")) {
            Istimeout_Sec = false;
        } else {
            Istimeout_Sec = true;
        }

        TimeoutPinScreen();

        tv_fobkey.setText("");
        LF_FobKey = "";

        btnSave.setClickable(true);
        //Set/Reset EnterPin text
        etPersonnelPin.setText("");

        DisplayScreenInit();


        t = new Timer();
        TimerList.add(t);
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {

                sec_count++;
                invalidateOptionsMenu();
                UpdateReaderStatusToUI();

                if (!AppConstants.PinLocal_FOB_KEY.equalsIgnoreCase("")) {

                    CancelTimer();
                    System.out.println("Pin FOK_KEY" + AppConstants.APDU_FOB_KEY);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //tv_enter_pin_no.setText("Fob Read Successfully");
                            FobreadSuccess();
                        }
                    });

                } else {

                    checkFor10Seconds();

                }

            }
        };
        t.schedule(tt, 1000, 1000);

    }

    @Override
    protected void onPause() {
        super.onPause();

        UnRegisterBroadcastForReader();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        CancelTimer();

        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        AppConstants.PinLocal_FOB_KEY = "";
        //  AppConstants.APDU_FOB_KEY = "";
        CancelTimer();
        ScreenTimer();

    }

    private void CancelTimer() {

        for (int i = 0; i < TimerList.size(); i++) {
            TimerList.get(i).cancel();
        }

    }


    private void ScreenTimer() {

        for (int i = 0; i < ScreenTimerList.size(); i++) {
            ScreenTimerList.get(i).cancel();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reader, menu);

        menu.findItem(R.id.mreboot_reader).setVisible(true);
        //menu.findItem(R.id.mconfigure_tld).setVisible(false);
        menu.findItem(R.id.madd_link).setVisible(false);
        menu.findItem(R.id.mupgrade_normal_link).setVisible(false);
        menu.findItem(R.id.enable_debug_window).setVisible(false);
        menu.findItem(R.id.mclose).setVisible(false);
        menu.findItem(R.id.mreconnect_ble_readers).setVisible(true);
        menu.findItem(R.id.mshow_reader_status).setVisible(true);
        menu.findItem(R.id.mcamera_back).setVisible(false);
        menu.findItem(R.id.mcamera_front).setVisible(false);

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
                //Connect Ble readers
                return true;
            case R.id.menu_disconnect:
                //Disconnect Ble readers
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
            case R.id.mreboot_reader:
                AppConstants.showReaderStatus = true;
                CustomDilaogForRebootCmd(AcceptPinActivity_new.this, "Please enter a code to continue.", "Message");
                return true;
            case R.id.mshow_reader_status:
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "<Show Reader Status>");
                AppConstants.showReaderStatus = true;
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void displayData_LF(String data) {

        if (data != null || !data.isEmpty()) {

            String Str_data = data.toString().trim();
            System.out.println("FOK_KEY Pin " + Str_data);
            //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Response LF: " + Str_data);
            String Str_check = Str_data.replace(" ", "");

            if (Str_data.contains("FFFFFFFFFFFFFFFFFFFF") || Str_data.contains("FF FF FF FF FF FF FF FF FF FF")) {

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Unable to read fob: " + Str_data);
                CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", "Unable to read fob.  Please Try again..");

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

                    if (!LF_FobKey.equalsIgnoreCase("") && LF_FobKey.length() > 5) {//
                        //tv_enter_pin_no.setText("Fob Read Successfully");
                        tv_fob_number.setText("");//"Fob No: " + LF_FobKey
                        AppConstants.PinLocal_FOB_KEY = LF_FobKey;
                        AppConstants.APDU_FOB_KEY = LF_FobKey;
                    }

                } catch (Exception ex) {
                    System.out.println(ex);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "  displayData --Exception " + ex);
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
                            System.out.println("IsLFUpdateServer.." + IsLFUpdateServer);
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

        if (data != null && !data.isEmpty()) {

            String Str_data = data.toString().trim();
            System.out.println("FOK_KEY pIN " + Str_data);
            //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " Response HF: " + Str_data);
            String Str_check = Str_data.replace(" ", "");

            if (Str_data.contains("FFFFFFFFFFFFFFFFFFFF") || Str_data.contains("FF FF FF FF FF FF FF FF FF FF")) {

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Unable to read fob: " + Str_data);
                CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", "Unable to read fob.  Please Try again..");

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


                    if (!LF_FobKey.equalsIgnoreCase("") && LF_FobKey.length() > 5) {//
                        //tv_enter_pin_no.setText("Fob Read Successfully");
                        tv_fob_number.setText("");//"Fob No: " + LF_FobKey
                        AppConstants.PinLocal_FOB_KEY = LF_FobKey;
                        AppConstants.APDU_FOB_KEY = LF_FobKey;

                    }

                } catch (Exception ex) {
                    System.out.println(ex);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "  displayData_HF --Exception " + ex);
                }

            }
            //BLE Upgrade
            else if (!Str_data.trim().equals("00 00 00 00 00 00 00 00 00 00")) {
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
                            System.out.println("IsLFUpdateServer.." + IsLFUpdateServer);
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
        ///if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " displayData_MagCard " + data);

        if (data != null && !data.isEmpty()) {

            String Str_data = data.toString().trim();
            Log.i(TAG, "displayData MagCard:" + Str_data);
            // if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  displayData MagCard: " + Str_data);

            String Str_check = Str_data.replace(" ", "");
            if (!CommonUtils.ValidateFobkey(Str_check) || Str_data.contains("FFFFFFFFFFFFFFFFFFFF") || Str_data.contains("FF FF FF FF FF FF FF FF FF FF")) {

                MagCard_personnel = "";
                //CommonUtils.AutoCloseCustomMessageDilaog(DeviceControlActivity_Pin.this, "Message", "Unable to read MagCard.  Please Try again..");

            } else if (Str_check.length() > 5) {

                try {
                    MagCard_personnel = Str_check;
                    tv_fob_number.setText("");//"Fob No: " + LF_FobKey
                    AppConstants.PinLocal_FOB_KEY = MagCard_personnel;
                    //AppConstants.APDU_FOB_KEY = MagCard_personnel;

                } catch (Exception ex) {
                    MagCard_personnel = "";
                    System.out.println(ex);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "  displayData Split MagCard  --Exception " + ex);
                }

            }

        } else {
            MagCard_personnel = "";
        }
    }

    private String sendVersionToServer(String bleVersion, String bletype) {
        SharedPreferences sharedPrefODO = this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String PersonId = sharedPrefODO.getString(AppConstants.HubId, "");
        BLEType = bletype;

        SharedPreferences sharedPref = this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String userEmail = sharedPref.getString(AppConstants.USER_EMAIL, "");

        String authStringDefTire = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(this) + ":" + userEmail + ":" + "CheckCurrentBLEVersionOnDemand" + AppConstants.LANG_PARAM);
        BleVersionData bleVersionData = new BleVersionData();
        bleVersionData.BLEType = BLEType;
        if (BLEType.equals("HF"))
            bleVersionData.VersionHF = bleVersion;
        else
            bleVersionData.VersionHF = "";
        if (BLEType.equals("LF"))
            bleVersionData.VersionLF = bleVersion;
        else
            bleVersionData.VersionLF = "";
        bleVersionData.PersonId = PersonId;
        Gson gson = new Gson();
        final String jsonDataDefTire = gson.toJson(bleVersionData);
        String response = "";
        try {
            if (cd.isConnecting())
                response = new AcceptPinActivity_new.sendBleVersionData().execute(jsonDataDefTire, authStringDefTire).get();

        } catch (Exception e) {
            System.out.println(e);
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "  response BLE LF version number  --Exception " + e);
        }

        return response;

    }

    public void FobreadSuccess() {

        runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {

                AppConstants.PinLocal_FOB_KEY = "";
                ScreenTimer();

                SharedPreferences sharedPrefODO = AcceptPinActivity_new.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                IsVehicleNumberRequire = sharedPrefODO.getString(AppConstants.IsVehicleNumberRequire, "");

                if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {
                    IsNonValidatePerson = sharedPrefODO.getString(AppConstants.IsNonValidatePerson, "");
                } else {
                    IsNonValidateVehicle = controller.getOfflineHubDetails(AcceptPinActivity_new.this).IsNonValidateVehicle;
                    IsNonValidatePerson = controller.getOfflineHubDetails(AcceptPinActivity_new.this).IsNonValidatePerson;
                }

                if (MagCard_personnel != null && !MagCard_personnel.isEmpty()) {

                    String fob = MagCard_personnel.replace(":", "").trim();

                    HashMap<String, String> hmap = getMagneticCardKey(MagCard_personnel.trim());
                    hmapSwitchOfflinepin = hmap;
                    offlinePersonInitialization(hmap);

                    tv_fobkey.setText(fob);
                    CommonUtils.PlayBeep(AcceptPinActivity_new.this);

                    if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {
                        if (!isFinishing()) {
                            //if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + " templog Fob success: MAG " + MagCard_personnel);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "MagCard read success: " + fob);
                            new GetPinNuOnFobKeyDetection().execute();
                        }
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Internet Connection: " + cd.isConnectingToInternet() + "; NETWORK_STRENGTH: " + AppConstants.NETWORK_STRENGTH);
                        //offline---------------
                        if (OfflineConstants.isOfflineAccess(AcceptPinActivity_new.this)) {
                            checkPINvalidation(hmap);
                            String PinNumber = hmap.get("PinNumber");
                            AppConstants.OFF_PERSON_PIN = PinNumber;
                            etPersonnelPin.setText(PinNumber);
                        } else {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Offline Access not granted to this HUB.");
                            //CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", "Unable to connect server");
                        }
                    }


                } else if (AppConstants.APDU_FOB_KEY != null) {

                    String test = AppConstants.APDU_FOB_KEY;
                    tv_fob_number.setText("Access Device No: " + test);

                    try {

                        String fob = AppConstants.APDU_FOB_KEY.replace(":", "").trim();
                        HashMap<String, String> hmap = controller.getPersonnelDetailsByFOBnumber(fob);
                        hmapSwitchOfflinepin = hmap;
                        offlinePersonInitialization(hmap);

                        tv_fobkey.setText(fob);
                        CommonUtils.PlayBeep(AcceptPinActivity_new.this);

                        if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {
                            if (!isFinishing()) {
                                // if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + " templog Fob success: APDU " + AppConstants.APDU_FOB_KEY);
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "FOB read success: " + fob);
                                new GetPinNuOnFobKeyDetection().execute();
                            }
                        } else {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Internet Connection: " + cd.isConnectingToInternet() + "; NETWORK_STRENGTH: " + AppConstants.NETWORK_STRENGTH);
                            if (OfflineConstants.isOfflineAccess(AcceptPinActivity_new.this)) {
                                checkPINvalidation(hmap);
                                String PinNumber = hmap.get("PinNumber");
                                AppConstants.OFF_PERSON_PIN = PinNumber;
                                etPersonnelPin.setText(PinNumber);
                            } else {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "Offline Access not granted to this HUB.");
                                //CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", "Unable to connect server");
                            }
                        }
                        tv_fob_number.setText("Access Device No: " + test);

                    } catch (Exception e) {
                        System.out.println("Pin FobreadSuccess-" + e.getMessage());
                    }

                } else {
                    AppConstants.colorToastBigFont(getApplicationContext(), "Access Device not found", Color.BLUE);
                }

            }
        });

    }

    private void InItGUI() {

        Linear_layout_vehicleNumber = (LinearLayout) findViewById(R.id.Linear_layout_vehicleNumber);
        layout_reader_status = (LinearLayout) findViewById(R.id.layout_reader_status);
        tv_fob_Reader = (TextView) findViewById(R.id.tv_fob_Reader);
        tv_or = (TextView) findViewById(R.id.tv_or);
        btnSave = (Button) findViewById(R.id.btnSave);
        footer_keybord = (RelativeLayout) findViewById(R.id.footer_keybord);
        Linear_layout_Save_back_buttons = (LinearLayout) findViewById(R.id.Linear_layout_Save_back_buttons);
        tv_return = (TextView) findViewById(R.id.tv_return);
        tv_swipekeybord = (TextView) findViewById(R.id.tv_swipekeybord);
        tv_fob_number = (TextView) findViewById(R.id.tv_fob_number);
        tv_vehicle_no_below = (TextView) findViewById(R.id.tv_vehicle_no_below);
        tv_enter_vehicle_no = (TextView) findViewById(R.id.tv_enter_vehicle_no);
        tv_dont_have_fob = (TextView) findViewById(R.id.tv_dont_have_fob);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_hf_status = (TextView) findViewById(R.id.tv_hf_status);
        tv_lf_status = (TextView) findViewById(R.id.tv_lf_status);
        tv_mag_status = (TextView) findViewById(R.id.tv_mag_status);
        tv_reader_status = (TextView) findViewById(R.id.tv_reader_status);

    }

    //============SoftKeyboard enable/disable Detection======
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

        Log.d(TAG, "isKeyboardShown ? " + isKeyboardShown + ", heightDiff:" + heightDiff + ", density:" + dm.density
                + "root view height:" + rootView.getHeight() + ", rect:" + r);

        return isKeyboardShown;
    }

    public void cancelAction(View v) {

        hideKeybord();
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (IsGateHub.equalsIgnoreCase("True")) {
            AppConstants.GoButtonAlreadyClicked = false;
        }
        // ActivityHandler.removeActivity(3);

        AppConstants.serverCallInProgressForPin = false;
        AppConstants.serverCallInProgressForVehicle = false;
        Istimeout_Sec = false;
        //AppConstants.ClearEdittextFielsOnBack(AcceptPinActivity.this); //Clear EditText on move to welcome activity.
        finish();
    }

    public void DisplayScreenInit() {

        SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_FA_Data, Context.MODE_PRIVATE);
        boolean BarcodeStatus = sharedPref.getBoolean(AppConstants.UseBarcodeForPersonnel, false);

        if (BarcodeStatus) {
            btn_barcode.setVisibility(View.VISIBLE);

        } else {
            btn_barcode.setVisibility(View.GONE);

        }

        if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {
            SharedPreferences sharedPrefODO = AcceptPinActivity_new.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
            IsPersonHasFob = sharedPrefODO.getString(AppConstants.IsPersonHasFob, "false");
        } else {
            IsPersonHasFob = controller.getOfflineHubDetails(AcceptPinActivity_new.this).PersonhasFOB;

            if (IsPersonHasFob.trim().equalsIgnoreCase("y"))
                IsPersonHasFob = "true";

            IsNonValidateVehicle = controller.getOfflineHubDetails(AcceptPinActivity_new.this).IsNonValidateVehicle;
            IsNonValidatePerson = controller.getOfflineHubDetails(AcceptPinActivity_new.this).IsNonValidatePerson;
        }

        /*
        if (IsPersonHasFob.equalsIgnoreCase("true")  || !BarcodeStatus) {
            linearBarcode.setVisibility(View.GONE);
        } else {
            linearBarcode.setVisibility(View.VISIBLE);
        }*/

        if (IsPersonPinAndFOBRequire.equalsIgnoreCase("true") || AllowAccessDeviceORManualEntry.equalsIgnoreCase("true")) {

            btnCancel.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.VISIBLE);
            mDisableFOBReadingForPin = "N";

            int width = ActionBar.LayoutParams.MATCH_PARENT;
            int height = ActionBar.LayoutParams.WRAP_CONTENT;
            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width, height);
            parms.gravity = Gravity.CENTER;
            etPersonnelPin.setLayoutParams(parms);

            Linear_layout_Save_back_buttons.setVisibility(View.VISIBLE);
            tv_fob_Reader.setVisibility(View.VISIBLE);
            tv_dont_have_fob.setVisibility(View.VISIBLE);
            tv_or.setVisibility(View.VISIBLE);

        } else if (IsPersonHasFob.equalsIgnoreCase("true")) {


            btnCancel.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.GONE);

            int widthi = 0;
            int heighti = 0;
            LinearLayout.LayoutParams parmsi = new LinearLayout.LayoutParams(widthi, heighti);
            parmsi.weight = 0;
            btnSave.setLayoutParams(parmsi);

            int widthp = ActionBar.LayoutParams.MATCH_PARENT;
            int heightp = 0;
            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(widthp, heightp);
            parms.gravity = Gravity.CENTER;
            etPersonnelPin.setLayoutParams(parms);
            tv_dont_have_fob.setLayoutParams(parms);
            tv_or.setLayoutParams(parms);

            Linear_layout_Save_back_buttons.setVisibility(View.VISIBLE);
            tv_fob_Reader.setVisibility(View.INVISIBLE);
            tv_dont_have_fob.setVisibility(View.INVISIBLE);
            tv_or.setVisibility(View.INVISIBLE);

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


            btnCancel.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.VISIBLE);

            int width = ActionBar.LayoutParams.MATCH_PARENT;
            int height = ActionBar.LayoutParams.WRAP_CONTENT;
            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width, height);
            parms.gravity = Gravity.CENTER;
            etPersonnelPin.setLayoutParams(parms);

            Linear_layout_Save_back_buttons.setVisibility(View.VISIBLE);
            tv_fob_Reader.setVisibility(View.VISIBLE);
            tv_dont_have_fob.setVisibility(View.VISIBLE);
            tv_or.setVisibility(View.VISIBLE);

        }

        int width = 0;
        int height = 0;
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width, height);
        parms.gravity = Gravity.CENTER;
        tv_ok.setLayoutParams(parms);

        etPersonnelPin.setEnabled(true);
        btnSave.setEnabled(true);
        tv_fob_number.setText("");
        tv_enter_pin_no.setVisibility(View.INVISIBLE);
        tv_fob_Reader.setVisibility(View.VISIBLE);
        tv_fob_Reader.setVisibility(View.VISIBLE);
        Linear_layout_Save_back_buttons.setVisibility(View.VISIBLE);
        etPersonnelPin.setVisibility(View.VISIBLE);

        int widthi = ActionBar.LayoutParams.WRAP_CONTENT;
        int heighti = 0;
        LinearLayout.LayoutParams parmsi = new LinearLayout.LayoutParams(widthi, heighti);
        tv_enter_pin_no.setLayoutParams(parmsi);

        if (mDisableFOBReadingForPin.equalsIgnoreCase("Y")) {
            tv_fob_Reader.setVisibility(View.GONE);
            tv_or.setVisibility(View.GONE);
        }


    }

    public void DisplayScreenFobReadSuccess() {

        int width = ActionBar.LayoutParams.WRAP_CONTENT;
        int height = ActionBar.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width, height);
        parms.gravity = Gravity.CENTER;
        tv_ok.setLayoutParams(parms);

        int widthi = ActionBar.LayoutParams.WRAP_CONTENT;
        int heighti = ActionBar.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams parmsi = new LinearLayout.LayoutParams(widthi, heighti);
        tv_enter_pin_no.setLayoutParams(parmsi);

        //Display on success
        tv_fob_number.setVisibility(View.GONE);
        tv_fob_Reader.setVisibility(View.GONE);
        tv_or.setVisibility(View.GONE);
        tv_enter_pin_no.setVisibility(View.VISIBLE);
        tv_ok.setText("");//Access Device read successfully
        tv_dont_have_fob.setVisibility(View.GONE);
        etPersonnelPin.setVisibility(View.GONE);
        btn_barcode.setVisibility(View.GONE);
        //linearBarcode.setVisibility(View.GONE);
        Linear_layout_Save_back_buttons.setVisibility(View.VISIBLE);


        btnCancel.setVisibility(View.GONE);
        btnSave.setVisibility(View.GONE);


    }

    public class CallSaveButtonFunctionality extends AsyncTask<Void, Void, String> {


        ProgressDialog pd;

        @Override
        protected void onPreExecute() {

            String s = getResources().getString(R.string.PleaseWait);
            SpannableString ss2 = new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            pd = new ProgressDialog(AcceptPinActivity_new.this);
            pd.setMessage(ss2);
            pd.setCancelable(true);
            pd.show();

        }

        protected String doInBackground(Void... arg0) {

            String resp = "";
            String vehicleNumber = "";
            try {

                if (!etPersonnelPin.getText().toString().trim().isEmpty()) {

                    if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
                        Constants.AccPersonnelPIN_FS1 = etPersonnelPin.getText().toString().trim();

                        vehicleNumber = Constants.AccVehicleNumber_FS1;

                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
                        Constants.AccPersonnelPIN = etPersonnelPin.getText().toString().trim();

                        vehicleNumber = Constants.AccVehicleNumber;

                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
                        Constants.AccPersonnelPIN_FS3 = etPersonnelPin.getText().toString().trim();

                        vehicleNumber = Constants.AccVehicleNumber_FS3;
                        Log.i("ps_Vechile no", "Step 3:" + vehicleNumber);
                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS4")) {
                        Constants.AccPersonnelPIN_FS4 = etPersonnelPin.getText().toString().trim();
                        vehicleNumber = Constants.AccVehicleNumber_FS4;
                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS5")) {
                        Constants.AccPersonnelPIN_FS5 = etPersonnelPin.getText().toString().trim();
                        vehicleNumber = Constants.AccVehicleNumber_FS5;
                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS6")) {
                        Constants.AccPersonnelPIN_FS6 = etPersonnelPin.getText().toString().trim();
                        vehicleNumber = Constants.AccVehicleNumber_FS6;
                    }


                    Istimeout_Sec = false;

                    VehicleRequireEntity objEntityClass = new VehicleRequireEntity();
                    objEntityClass.IMEIUDID = AppConstants.getIMEI(AcceptPinActivity_new.this);
                    objEntityClass.VehicleNumber = vehicleNumber;
                    objEntityClass.WifiSSId = AppConstants.LAST_CONNECTED_SSID;
                    objEntityClass.SiteId = Integer.parseInt(AppConstants.SITE_ID);
                    objEntityClass.PersonnelPIN = etPersonnelPin.getText().toString().trim();
                    objEntityClass.RequestFromAPP = "AP";
                    objEntityClass.FromNewFOBChange = "Y";
                    objEntityClass.FOBNumber = AppConstants.APDU_FOB_KEY;
                    objEntityClass.IsVehicleNumberRequire = IsVehicleNumberRequire;
                    objEntityClass.Barcode = Barcode_pin_val;

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

                    /*objEntityClass.HFVersion = "123456";
                    objEntityClass.LFVersion = "456789";*/

                    AppConstants.FOB_KEY_VEHICLE = AppConstants.APDU_FOB_KEY;

                    Log.i(TAG, "VehicleNumber: " + vehicleNumber);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "VehicleNumber: " + vehicleNumber);

                    if (AppConstants.APDU_FOB_KEY.equalsIgnoreCase("")) {
                        Log.i(TAG, "PIN EN Manually: " + etPersonnelPin.getText().toString().trim() + "  Fob:" + AppConstants.APDU_FOB_KEY);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "PIN EN Manually: " + etPersonnelPin.getText().toString().trim() + "; Fob:" + AppConstants.APDU_FOB_KEY);
                    } else {
                        Log.i(TAG, "PIN FOB:" + AppConstants.APDU_FOB_KEY + "  PIN No: " + String.valueOf(etPersonnelPin.getText()));
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "PIN FOB:" + AppConstants.APDU_FOB_KEY + "; PIN No: " + String.valueOf(etPersonnelPin.getText()));
                    }


                    Gson gson = new Gson();
                    String jsonData = gson.toJson(objEntityClass);
                    String userEmail = CommonUtils.getCustomerDetails(AcceptPinActivity_new.this).PersonEmail;

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

                }

            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " CallSaveButtonFunctionality  STE2 " + e);
                if (OfflineConstants.isOfflineAccess(AcceptPinActivity_new.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
                GetBackToWelcomeActivity();


            } catch (Exception e) {
                e.printStackTrace();
                if (OfflineConstants.isOfflineAccess(AcceptPinActivity_new.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }

            }
            return resp;
        }


        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(String serverRes) {

            if (!PersonValidationInProgress) {
                pd.dismiss();
            }

            if (serverRes != null && !serverRes.isEmpty()) {

                try {

                    JSONObject jsonObject = new JSONObject(serverRes);

                    String ResponceMessage = jsonObject.getString("ResponceMessage");

                    System.out.println("ResponceMessage.." + ResponceMessage);

                    if (ResponceMessage.equalsIgnoreCase("success")) {


                        //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG +" PIN Accepted:" + etPersonnelPin.getText().toString().trim());

                        btnSave.setClickable(false);

                        SharedPreferences sharedPrefODO = AcceptPinActivity_new.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                        String IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
                        String IsHoursRequire = sharedPrefODO.getString(AppConstants.IsHoursRequire, "");
                        String IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
                        String IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");


                        if (IsDepartmentRequire.equalsIgnoreCase("True") && !IsGateHub.equalsIgnoreCase("True")) {

                            Intent intent = new Intent(AcceptPinActivity_new.this, AcceptDeptActivity.class);
                            startActivity(intent);

                        } else if (IsOtherRequire.equalsIgnoreCase("True") && !IsGateHub.equalsIgnoreCase("True")) {

                            Intent intent = new Intent(AcceptPinActivity_new.this, AcceptOtherActivity.class);
                            startActivity(intent);

                        } else {

                            AppConstants.serverCallInProgressForPin = true;
                            barcodeReaderCall = true;
                            PersonValidationInProgress = true;
                            AcceptServiceCall asc = new AcceptServiceCall();
                            asc.activity = AcceptPinActivity_new.this;
                            asc.checkAllFields();
                        }
                    } else {

                        String ResponceText = jsonObject.getString("ResponceText");

                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "PIN rejected. Error: " + ResponceText);

                        String ValidationFailFor = jsonObject.getString("ValidationFailFor");

                        if (ValidationFailFor.equalsIgnoreCase("PinWithFob")) {

                            CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", ResponceText);

                        } else if (ValidationFailFor.equalsIgnoreCase("Pin")) {

                            //Clear Pin edit text
                            if (Constants.CurrentSelectedHose.equals("FS1")) {
                                Constants.AccPersonnelPIN_FS1 = "";
                            } else if (Constants.CurrentSelectedHose.equals("FS2")) {
                                Constants.AccPersonnelPIN = "";
                            } else if (Constants.CurrentSelectedHose.equals("FS3")) {
                                Constants.AccPersonnelPIN_FS3 = "";
                            } else if (Constants.CurrentSelectedHose.equals("FS4")) {
                                Constants.AccPersonnelPIN_FS4 = "";
                            } else if (Constants.CurrentSelectedHose.equals("FS5")) {
                                Constants.AccPersonnelPIN_FS5 = "";
                            } else if (Constants.CurrentSelectedHose.equals("FS6")) {
                                Constants.AccPersonnelPIN_FS6 = "";
                            }

                            //AppConstants.colorToastBigFont(AcceptPinActivity_new.this, ResponceText, Color.BLUE);
                            //CommonUtils.AlertDialogAutoClose(AcceptPinActivity_new.this, "Message", ResponceText);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "ValidateFor Pin: " + ResponceText);

                            DialogRecreate(AcceptPinActivity_new.this, "Message", ResponceText);

                        } else if (ValidationFailFor.equalsIgnoreCase("Vehicle")) {

                            AppConstants.colorToastBigFont(AcceptPinActivity_new.this, ResponceText, Color.BLUE);
                            //CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", ResponceText);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "ValidateFor Vehicle: " + ResponceText);

                            AppConstants.ClearEdittextFielsOnBack(AcceptPinActivity_new.this); //Clear EditText on move to welcome activity.
                            Intent intent = new Intent(AcceptPinActivity_new.this, WelcomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        } else {

                            //AppConstants.colorToastBigFont(AcceptPinActivity_new.this, ResponceText, Color.BLUE);
                            CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", ResponceText);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "ValidateFor Else: " + ResponceText);

                            /*AppConstants.colorToastBigFont(this, "Some thing went wrong Please try again..\n"+ResponceText, Color.BLUE);
                             if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG+" Some thing went wrong Please try again..(~else~)\n"+ResponceText);
                            AppConstants.ClearEdittextFielsOnBack(DeviceControlActivity_fsnp.this); //Clear EditText on move to welcome activity.
                            Intent intent = new Intent(DeviceControlActivity_fsnp.this, WelcomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);*/
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (OfflineConstants.isOfflineAccess(AcceptPinActivity_new.this)) {
                        AppConstants.NETWORK_STRENGTH = false;
                    }

                }
            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Internet Connection: " + cd.isConnectingToInternet());
                if (OfflineConstants.isOfflineAccess(AcceptPinActivity_new.this)) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "CallSaveButtonFunctionality Temporary loss of cell service ~Switching to offline mode!!");
                    AppConstants.NETWORK_STRENGTH = false;
                }
                if (OfflineConstants.isOfflineAccess(AcceptPinActivity_new.this)) {
                    //offline----------
                    checkPINvalidation(hmapSwitchOfflinepin);

                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Offline Access not granted to this HUB.");
                    //CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", "Unable to connect server");
                }

            }

        }
    }

    public class GetPinNuOnFobKeyDetection extends AsyncTask<Void, Void, String> {


        @Override
        protected void onPreExecute() {

            InScrverCall = true;

            String text = getResources().getString(R.string.PleaseWait);
            SpannableStringBuilder biggerText = new SpannableStringBuilder(text);
            biggerText.setSpan(new RelativeSizeSpan(2.00f), 0, text.length(), 0);
            Toast.makeText(getApplicationContext(), biggerText, Toast.LENGTH_LONG).show();

        }

        protected String doInBackground(Void... arg0) {

            String resp = "";

            try {

                CheckPinFobEntity objEntityClass = new CheckPinFobEntity();
                objEntityClass.IMEIUDID = AppConstants.getIMEI(AcceptPinActivity_new.this);
                objEntityClass.PersonPIN = String.valueOf(etPersonnelPin.getText());
                objEntityClass.PersonFOBNumber = AppConstants.APDU_FOB_KEY;
                objEntityClass.FromNewFOBChange = "Y";
                objEntityClass.MagneticCardNumber = MagCard_personnel;
                objEntityClass.Barcode = Barcode_pin_val;

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

            /*objEntityClass.HFVersion = "pin799";
            objEntityClass.LFVersion = "pin456";*/

                //objEntityClass.IsBothFobAndPinRequired = IsBothFobAndPinRequired_flag;

                System.out.println(TAG + "Personnel PIN: Read FOB:" + AppConstants.APDU_FOB_KEY + "  PIN Number: " + String.valueOf(etPersonnelPin.getText()));
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Personnel FOB No:" + AppConstants.APDU_FOB_KEY + "; PIN Number:" + String.valueOf(etPersonnelPin.getText()) + "; Barcode_val:" + Barcode_pin_val + "; MagCard_personnel:" + MagCard_personnel);

                ServerHandler serverHandler = new ServerHandler();

                Gson gson = new Gson();
                String jsonData = gson.toJson(objEntityClass);
                String userEmail = CommonUtils.getCustomerDetails(AcceptPinActivity_new.this).PersonEmail;

                System.out.println("jsonData123" + jsonData);
                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(objEntityClass.IMEIUDID + ":" + userEmail + ":" + "CheckValidPinOrFOBNUmber" + AppConstants.LANG_PARAM);

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

                InScrverCall = false;
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " GetPinNuOnFobKeyDetection  STEP1 " + e);
                if (OfflineConstants.isOfflineAccess(AcceptPinActivity_new.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
                GetBackToWelcomeActivity();


            } catch (Exception ex) {
                InScrverCall = false;
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " GetPinNuOnFobKeyDetection  STEP2 " + ex);
                CommonUtils.LogMessage("TAG", "CheckValidPinOrFOBNUmber ", ex);
                if (OfflineConstants.isOfflineAccess(AcceptPinActivity_new.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }

            }

            return resp;
        }


        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(String serverRes) {


            try {
                InScrverCall = false;

                if (serverRes != null && !serverRes.isEmpty()) {

                    JSONObject jsonObject = new JSONObject(serverRes);

                    String ResponceMessage = jsonObject.getString("ResponceMessage");
                    System.out.println("ResponceMessage..dt.." + ResponceMessage);
                    String PersonFOBNumber = jsonObject.getString("PersonFOBNumber");
                    String PersonPIN = jsonObject.getString("PersonPIN");
                    String IsNewFob = jsonObject.getString("IsNewFob");
                    String IsBothFobAndPinRequired = jsonObject.getString("IsBothFobAndPinRequired");
                    String IsNewMagneticCardReaderNumber = jsonObject.getString("IsNewMagneticCardReaderNumber");
                    String IsNewBarcode = jsonObject.getString("IsNewBarcode");

                    if (ResponceMessage.equalsIgnoreCase("success")) {

                        DisplayScreenFobReadSuccess();
                        tv_enter_pin_no.setText(getResources().getString(R.string.PersonnelNumberHeading).replace("Number", "").replace("Personnel", ScreenNameForPersonnel) + " ****");//PersonPIN fob replace by asterisk for password
                        System.out.println("PersonFOBNumber.." + PersonFOBNumber + "PersonPin" + PersonPIN);
                        AppConstants.OFF_PERSON_PIN = PersonPIN;
                        etPersonnelPin.setText(PersonPIN);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                InCaseOfGateHub();
                            }
                        }, 1000);


                    } else {

                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Person Fob Read Fail. Error: " + ResponceMessage);

                        ////////////////

                        if (IsNonValidatePerson.equalsIgnoreCase("True")) { //&& IsNewFob.equalsIgnoreCase("No")

                            if (MagCard_personnel != null && !MagCard_personnel.isEmpty()) {
                                if (MagCard_personnel.equalsIgnoreCase(AppConstants.NonValidateVehicle_FOB_KEY)) {
                                    //error message
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "Wrong access device is presented. Please present different access device.");
                                    AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", "Wrong access device is presented. Please present different access device.");
                                } else {
                                    if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
                                        Constants.AccPersonnelPIN_FS1 = MagCard_personnel;
                                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
                                        Constants.AccPersonnelPIN = MagCard_personnel;
                                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
                                        Constants.AccPersonnelPIN_FS3 = MagCard_personnel;
                                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS4")) {
                                        Constants.AccPersonnelPIN_FS4 = MagCard_personnel;
                                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS5")) {
                                        Constants.AccPersonnelPIN_FS5 = MagCard_personnel;
                                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS6")) {
                                        Constants.AccPersonnelPIN_FS6 = MagCard_personnel;
                                    }
                                }
                            } else if (AppConstants.APDU_FOB_KEY != null) {


                                String fob = AppConstants.APDU_FOB_KEY.replace(":", "").trim();
                                if (fob.equalsIgnoreCase(AppConstants.NonValidateVehicle_FOB_KEY)) {
                                    //error message
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "Same access device is scanned again. Please check.");
                                    CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", "Same access device is scanned again. Please check.");
                                } else {

                                    tv_enter_pin_no.setText(getResources().getString(R.string.PersonnelNumberHeading).replace("Number", "").replace("Personnel", ScreenNameForPersonnel) + " ****");//fob replace by asterisk for password
                                    etPersonnelPin.setText(fob);

                                    if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
                                        Constants.AccPersonnelPIN_FS1 = etPersonnelPin.getText().toString().trim();
                                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
                                        Constants.AccPersonnelPIN = etPersonnelPin.getText().toString().trim();
                                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
                                        Constants.AccPersonnelPIN_FS3 = etPersonnelPin.getText().toString().trim();
                                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS4")) {
                                        Constants.AccPersonnelPIN_FS4 = etPersonnelPin.getText().toString().trim();
                                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS5")) {
                                        Constants.AccPersonnelPIN_FS5 = etPersonnelPin.getText().toString().trim();
                                    } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS6")) {
                                        Constants.AccPersonnelPIN_FS6 = etPersonnelPin.getText().toString().trim();
                                    }

                                }
                            }

                            DisplayScreenFobReadSuccess();
                            //etPersonnelPin.setText(fob);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    AppConstants.serverCallInProgressForPin = true;
                                    barcodeReaderCall = true;
                                    PersonValidationInProgress = true;
                                    AcceptServiceCall asc = new AcceptServiceCall();
                                    asc.activity = AcceptPinActivity_new.this;
                                    asc.checkAllFields();

                                }
                            }, 1000);

                        } else if (IsNewBarcode.equalsIgnoreCase("No")) {

                            AppConstants.APDU_FOB_KEY = "";
                            Barcode_pin_val = "";
                            ResetTimeoutPinScreen();
                            CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", ResponceMessage);

                        } else if (IsBothFobAndPinRequired.equalsIgnoreCase("yes")) {

                            AcceptPinNumber();

                            InputMethodManager inputMethodManager = (InputMethodManager) etPersonnelPin.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            etPersonnelPin.requestFocus();
                            inputMethodManager.showSoftInput(etPersonnelPin, 0);

                            if (IsPersonHasFob.equalsIgnoreCase("true")) {
                                CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", ResponceMessage);
                            } else {
                                ResetTimeoutPinScreen();
                                CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", ResponceMessage);

                            }


                        } else if (IsNewFob.equalsIgnoreCase("No")) {
                            AppConstants.APDU_FOB_KEY = "";
                            tv_fob_Reader.setVisibility(View.GONE);
                            onResume();

                            /*int width = ActionBar.LayoutParams.WRAP_CONTENT;
                            int height = ActionBar.LayoutParams.WRAP_CONTENT;
                            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width, height);
                            parms.gravity = Gravity.CENTER;
                            tv_ok.setLayoutParams(parms);
                            tv_ok.setText("Invalid Access Device or Unassigned FOB");/
*/
                            ResetTimeoutPinScreen();
                            CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", ResponceMessage);

                        } else if (IsNewMagneticCardReaderNumber.equalsIgnoreCase("yes")) {

                            AcceptPinNumber();

                            InputMethodManager inputMethodManager = (InputMethodManager) etPersonnelPin.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            etPersonnelPin.requestFocus();
                            inputMethodManager.showSoftInput(etPersonnelPin, 0);

                            if (IsPersonHasFob.equalsIgnoreCase("true")) {

                                int w = ActionBar.LayoutParams.WRAP_CONTENT;
                                int h = ActionBar.LayoutParams.WRAP_CONTENT;
                                LinearLayout.LayoutParams par = new LinearLayout.LayoutParams(w, h);
                                par.weight = 1;
                                btnCancel.setTextSize(18);
                                btnCancel.setLayoutParams(par);
                                btnSave.setVisibility(View.VISIBLE);
                                CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", ResponceMessage);
                            } else {
                                ResetTimeoutPinScreen();
                                CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", ResponceMessage);
                            }

                        } else if (IsNewFob.equalsIgnoreCase("Yes") && IsPersonHasFob.equalsIgnoreCase("true")) {

                            AcceptPinNumber();

                            InputMethodManager inputMethodManager = (InputMethodManager) etPersonnelPin.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            etPersonnelPin.requestFocus();
                            inputMethodManager.showSoftInput(etPersonnelPin, 0);

                            int w = ActionBar.LayoutParams.WRAP_CONTENT;
                            int h = ActionBar.LayoutParams.WRAP_CONTENT;
                            LinearLayout.LayoutParams par = new LinearLayout.LayoutParams(w, h);
                            par.weight = 1;
                            par.rightMargin = 20;
                            btnCancel.setTextSize(18);
                            btnCancel.setLayoutParams(par);
                            btnSave.setVisibility(View.VISIBLE);
                            CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", ResponceMessage);

                        } else {

                            AcceptPinNumber();

                            InputMethodManager inputMethodManager = (InputMethodManager) etPersonnelPin.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            etPersonnelPin.requestFocus();
                            inputMethodManager.showSoftInput(etPersonnelPin, 0);

                            if (IsPersonHasFob.equalsIgnoreCase("true")) {

                                CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", ResponceMessage);
                            } else {
                                ResetTimeoutPinScreen();
                                CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", Barcode_pin_val + " - " + ResponceMessage);
                            }
                        }

                        if (IsGateHub.equalsIgnoreCase("True")) {
                            Istimeout_Sec = false;
                        } else {
                            Istimeout_Sec = true;
                        }
                        TimeoutPinScreen();
                        btnSave.setEnabled(true);
                        tv_fob_number.setText("");
                        tv_fob_number.setVisibility(View.GONE);
                        tv_or.setVisibility(View.GONE);
                        tv_dont_have_fob.setVisibility(View.VISIBLE);
                        //String content = "Enter your<br> <b>" + ScreenNameForPersonnel + "</b> in<br> the green box below";
                        String content = getResources().getString(R.string.EnterPersonnelId).replace("PERSONNEL", "<br><b>" + ScreenNameForPersonnel + "</b><br>");

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            tv_dont_have_fob.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
                            System.out.println(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
                        } else {
                            tv_dont_have_fob.setText(Html.fromHtml(content));
                            System.out.println(Html.fromHtml(content));
                        }

                        Linear_layout_Save_back_buttons.setVisibility(View.VISIBLE);
                        etPersonnelPin.setVisibility(View.VISIBLE);
                        etPersonnelPin.setText("");
                    }

                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Internet Connection: " + cd.isConnectingToInternet());
                    if (OfflineConstants.isOfflineAccess(AcceptPinActivity_new.this)) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "GetPinNuOnFobKeyDetection Temporary loss of cell service ~Switching to offline mode!!");
                        AppConstants.NETWORK_STRENGTH = false;
                    }
                    if (OfflineConstants.isOfflineAccess(AcceptPinActivity_new.this)) {
                        //offline----------
                        checkPINvalidation(hmapSwitchOfflinepin);

                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Offline Access not granted to this HUB.");
                        //CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", "Unable to connect server");
                    }
                }

            } catch (Exception ex) {
                InScrverCall = false;
                Log.e("TAG", ex.getMessage());
                if (OfflineConstants.isOfflineAccess(AcceptPinActivity_new.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }

            }

        }
    }

    public void TimeoutPinScreen() {
        Log.i("TimeoutPinScreen", "Start");
        SharedPreferences sharedPrefODO = AcceptPinActivity_new.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        IsOdoMeterRequire = sharedPrefODO.getString(AppConstants.IsOdoMeterRequire, "");
        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
        AppConstants.HUB_ID = sharedPrefODO.getString(AppConstants.HubId, "");

        TimeOutinMinute = sharedPrefODO.getString(AppConstants.TimeOut, "1");
        //long screenTimeOut= (long) (Double.parseDouble(TimeOutinMinute) *60000);
        long screenTimeOut = Integer.parseInt(TimeOutinMinute) * 60000;

        ScreenOutTime = new Timer();
        ScreenTimerList.add(ScreenOutTime);
        TimerTask ttt = new TimerTask() {
            @Override
            public void run() {
                Log.i("TimeoutPinScreen", "Running..");
                if (Istimeout_Sec) {

                    try {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideKeybord();
                                Istimeout_Sec = false;
                                AppConstants.ClearEdittextFielsOnBack(AcceptPinActivity_new.this);


                                Intent i = new Intent(AcceptPinActivity_new.this, WelcomeActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                            }
                        });

                        ScreenTimer();
                    } catch (Exception e) {

                        System.out.println(e);
                    }

                }

            }

            ;
        };
        ScreenOutTime.schedule(ttt, screenTimeOut, 500);


    }

    public void ResetTimeoutPinScreen() {


        ScreenTimer();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        TimeoutPinScreen();
    }

    public void hideKeybord() {

        InputMethodManager imm = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void showKeybord() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void AcceptPinNumber() {

        tv_fob_Reader.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.VISIBLE);

        int width = ActionBar.LayoutParams.MATCH_PARENT;
        int height = ActionBar.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width, height);
        parms.gravity = Gravity.CENTER;
        etPersonnelPin.setLayoutParams(parms);

        int widthi = ActionBar.LayoutParams.WRAP_CONTENT;
        int heighti = ActionBar.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams parmsi = new LinearLayout.LayoutParams(widthi, heighti);
        parmsi.weight = 1;
        btnSave.setLayoutParams(parmsi);


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

    public void InCaseOfGateHub() {

        //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG +" InCaseOfGateHub PIN Accepted:" + etPersonnelPin.getText().toString().trim());

        String vehicleNumber = "";

        if (!etPersonnelPin.getText().toString().trim().isEmpty()) {

            if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
                Constants.AccPersonnelPIN_FS1 = etPersonnelPin.getText().toString().trim();

                vehicleNumber = Constants.AccVehicleNumber_FS1;

            } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
                Constants.AccPersonnelPIN = etPersonnelPin.getText().toString().trim();

                vehicleNumber = Constants.AccVehicleNumber;

            } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
                Constants.AccPersonnelPIN_FS3 = etPersonnelPin.getText().toString().trim();

                vehicleNumber = Constants.AccVehicleNumber_FS3;
            } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS4")) {
                Constants.AccPersonnelPIN_FS4 = etPersonnelPin.getText().toString().trim();
                vehicleNumber = Constants.AccVehicleNumber_FS4;
            } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS5")) {
                Constants.AccPersonnelPIN_FS5 = etPersonnelPin.getText().toString().trim();
                vehicleNumber = Constants.AccVehicleNumber_FS5;
            } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS6")) {
                Constants.AccPersonnelPIN_FS6 = etPersonnelPin.getText().toString().trim();
                vehicleNumber = Constants.AccVehicleNumber_FS6;
            }
        }

        Istimeout_Sec = false;


        btnSave.setClickable(false);

        SharedPreferences sharedPrefODO = AcceptPinActivity_new.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        String IsHoursRequire = sharedPrefODO.getString(AppConstants.IsHoursRequire, "");
        String IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        String IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");


        if (IsDepartmentRequire.equalsIgnoreCase("True") && !IsGateHub.equalsIgnoreCase("True")) {

            Intent intent = new Intent(AcceptPinActivity_new.this, AcceptDeptActivity.class);
            startActivity(intent);

        } else if (IsOtherRequire.equalsIgnoreCase("True") && !IsGateHub.equalsIgnoreCase("True")) {

            Intent intent = new Intent(AcceptPinActivity_new.this, AcceptOtherActivity.class);
            startActivity(intent);

        } else {

            AppConstants.serverCallInProgressForPin = true;
            barcodeReaderCall = true;
            PersonValidationInProgress = true;
            AcceptServiceCall asc = new AcceptServiceCall();
            asc.activity = AcceptPinActivity_new.this;
            asc.checkAllFields();
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void checkPINvalidation(HashMap<String, String> hmap) {
        if (hmap.size() > 0) {

            offlinePersonInitialization(hmap);

            String Authorizedlinks = hmap.get("Authorizedlinks");
            String AssignedVehicles = hmap.get("AssignedVehicles");
            String FuelLimitPerDay = hmap.get("FuelLimitPerDay");

            EntityHub obj = controller.getOfflineHubDetails(AcceptPinActivity_new.this);
            IsOffvehicleScreenRequired = obj.VehicleNumberRequired;
            IsNonValidateVehicle = obj.IsNonValidateVehicle;

            try {
                if (FuelLimitPerDay != null) {
                    double personLimitPerDay = Double.parseDouble(FuelLimitPerDay);
                    if (personLimitPerDay > 0) {
                        double remainingLimitPerDay = OfflineConstants.getPersonFuelLimitPerDay(AcceptPinActivity_new.this);
                        if (remainingLimitPerDay <= 0) {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "You have exceeded your fuel limit for the day.");
                            CommonUtils.showCustomMessageDilaog(AcceptPinActivity_new.this, "Error", getResources().getString(R.string.PerDayLimitExceeded));
                            return;
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (!Authorizedlinks.isEmpty() || Authorizedlinks.contains(",")) {
                boolean isAllowed = false;

                String parts[] = Authorizedlinks.split(",");
                for (String allowedId : parts) {
                    if (AppConstants.R_SITE_ID.equalsIgnoreCase(allowedId)) {
                        isAllowed = true;
                        break;
                    }
                }

                if (isAllowed) {

                    boolean isAssigned = false;

                    if (obj.IsDepartmentRequire.equalsIgnoreCase("true") && !obj.HUBType.equalsIgnoreCase("G")) {
                        Intent intent = new Intent(AcceptPinActivity_new.this, AcceptDeptActivity.class);
                        startActivity(intent);
                    } else if (obj.IsOtherRequire.equalsIgnoreCase("True") && !obj.HUBType.equalsIgnoreCase("G")) {
                        Intent intent = new Intent(AcceptPinActivity_new.this, AcceptOtherActivity.class);
                        startActivity(intent);

                    } else if (IsNonValidateVehicle.equalsIgnoreCase("True")) { // Do not validate vehicles

                        Intent ii = new Intent(AcceptPinActivity_new.this, DisplayMeterActivity.class);
                        startActivity(ii);

                    } else if (IsOffvehicleScreenRequired.equalsIgnoreCase("N")) {

                        //#1550 (Eva) Issue with offline mode If vehicle screen is disable Don't check AssignedVehicles
                        Intent ii = new Intent(AcceptPinActivity_new.this, DisplayMeterActivity.class);
                        startActivity(ii);

                    } else if (!AssignedVehicles.isEmpty() || AssignedVehicles.contains(",")) {

                        if (AssignedVehicles.trim().equalsIgnoreCase("all")) {
                            isAssigned = true;
                        } else {
                            String parts2[] = AssignedVehicles.split(",");
                            for (String allowedId : parts2) {
                                if (AppConstants.OFF_VEHICLE_ID.equalsIgnoreCase(allowedId)) {
                                    isAssigned = true;
                                    break;
                                }
                            }
                        }

                        if (isAssigned) {
                            Intent ii = new Intent(AcceptPinActivity_new.this, DisplayMeterActivity.class);
                            startActivity(ii);
                        } else {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Vehicle is not assigned for this PIN");
                            CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", ScreenNameForVehicle + " not assigned for this PIN");
                            //AppConstants.colorToastBigFont(getApplicationContext(), "Vehicle is not assigned for this PIN", Color.BLUE);
                        }


                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Personnel is not allowed for selected Vehicle.");
                        CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", ScreenNameForPersonnel + " is not allowed for selected " + ScreenNameForVehicle);
                    }


                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Personnel is not allowed for selected Link");
                    CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", ScreenNameForPersonnel + " not allowed for selected Link");
                }

            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "not assigned any links.");
            }

        } else {

            String validatePersonnel = "Yes";
            if (IsNonValidatePerson.equalsIgnoreCase("True")) {
                validatePersonnel = "No";
            }
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Validate person => " + validatePersonnel);
            if (IsNonValidatePerson.equalsIgnoreCase("True")) {
                Intent ii = new Intent(AcceptPinActivity_new.this, DisplayMeterActivity.class);
                startActivity(ii);

            } else {
                if (AppConstants.APDU_FOB_KEY != null && !AppConstants.APDU_FOB_KEY.isEmpty()) {
                    String fob = AppConstants.APDU_FOB_KEY.replace(":", "");
                    HashMap<String, String> VehicleMap = controller.getVehicleDetailsByFOBNumber(fob.trim());
                    if (VehicleMap.size() > 0) {
                        //vehicle fob please present pin fob
                        String msg = "This is " + ScreenNameForVehicle + " Access Device. Please use your " + ScreenNameForPersonnel + " Access Device";
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Personnel is not found in offline db. Access Device (" + fob + ") is vehicle access device.");
                        CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", msg);
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Personnel is not found in offline db");
                        CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", "Invalid Access Device");
                    }

                } else if (etPersonnelPin.getText().toString().trim() != null && !etPersonnelPin.getText().toString().trim().isEmpty()) {

                    String V_Number = etPersonnelPin.getText().toString().trim();
                    HashMap<String, String> VehicleMap = controller.getVehicleDetailsByVehicleNumber(V_Number);
                    if (VehicleMap.size() > 0) {
                        //vehicle fob please present pin fob
                        String msg = "This is " + ScreenNameForVehicle + ". Please use your " + ScreenNameForPersonnel + ".";
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Personnel is not found in offline db. This (" + V_Number + ") is vehicle number.");
                        CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", msg);
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Personnel is not found in offline db.");
                        CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_new.this, "Message", "Invalid " + ScreenNameForPersonnel);
                    }
                }
            }

            onResume();
        }
    }

    public void offlinePersonInitialization(HashMap<String, String> hmap) {
        try {
            if (hmap != null && hmap.size() > 0) {
                String PersonId = hmap.get("PersonId");
                String PinNumber = hmap.get("PinNumber");
                String FuelLimitPerTxn = hmap.get("FuelLimitPerTxn");
                String FuelLimitPerDay = hmap.get("FuelLimitPerDay");
                String FOBNumber = hmap.get("FOBNumber");
                String RequireHours = hmap.get("RequireHours");

                AppConstants.OFF_PERSON_PIN = PinNumber;

                if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
                    Constants.AccPersonnelPIN_FS1 = PinNumber;
                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
                    Constants.AccPersonnelPIN = PinNumber;
                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
                    Constants.AccPersonnelPIN_FS3 = PinNumber;
                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS4")) {
                    Constants.AccPersonnelPIN_FS4 = PinNumber;
                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS5")) {
                    Constants.AccPersonnelPIN_FS5 = PinNumber;
                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS6")) {
                    Constants.AccPersonnelPIN_FS6 = PinNumber;
                }

                OfflineConstants.storeCurrentTransaction(AcceptPinActivity_new.this, "", "", "", "", "", PersonId, "", "", "", "", "", "");

                OfflineConstants.storeFuelLimit(AcceptPinActivity_new.this, "", "", "", "", "", "", PersonId, FuelLimitPerTxn, FuelLimitPerDay);
            } else {

                String PinNumber = "";
                try {
                    String pin = etPersonnelPin.getText().toString().trim();
                    if (!pin.isEmpty()) {
                        PinNumber = pin.trim();
                    } else if (!AppConstants.APDU_FOB_KEY.isEmpty()) {
                        PinNumber = AppConstants.APDU_FOB_KEY.trim();
                    } else if (!Barcode_pin_val.isEmpty()) {
                        PinNumber = Barcode_pin_val.trim();
                    } else if (!MagCard_personnel.isEmpty()) {
                        PinNumber = MagCard_personnel.trim();
                    }
                } catch (Exception ex) {
                    PinNumber = "";
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Exception while getting entered pin number. " + ex.getMessage());
                }

                if (IsNonValidatePerson.equalsIgnoreCase("True")) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Pin Number (Non-validate): " + PinNumber);
                    if (!PinNumber.isEmpty()) {

                        AppConstants.OFF_PERSON_PIN = PinNumber;

                        if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
                            Constants.AccPersonnelPIN_FS1 = PinNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
                            Constants.AccPersonnelPIN = PinNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
                            Constants.AccPersonnelPIN_FS3 = PinNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS4")) {
                            Constants.AccPersonnelPIN_FS4 = PinNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS5")) {
                            Constants.AccPersonnelPIN_FS5 = PinNumber;
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS6")) {
                            Constants.AccPersonnelPIN_FS6 = PinNumber;
                        }

                        OfflineConstants.storeCurrentTransaction(AcceptPinActivity_new.this, "", "", "", "", "", "0", "", "", "", "", "", "");

                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Pin Number is empty.");
                    }
                } else {
                    if (!cd.isConnectingToInternet()) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Pin Number (" + PinNumber + ") not found in offline db.");
                    }
                }
            }
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " Exception in offlinePersonInitialization. " + ex.getMessage());
        }
    }

    public void GetBackToWelcomeActivity() {


        //AppConstants.colorToast(getApplicationContext(), "Something went wrong, Please try again", Color.BLUE);

        Istimeout_Sec = false;
        AppConstants.ClearEdittextFielsOnBack(AcceptPinActivity_new.this);

        Intent i = new Intent(AcceptPinActivity_new.this, WelcomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

    }

    public void DialogRecreate(final Activity context, final String title, final String message) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                new AlertDialog.Builder(context)
                        .setTitle(title)
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Whatever...
                                recreate();
                            }
                        }).show();
            }

        });

    }

    private class sendBleVersionData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String resp = "";

            System.out.println("Inside sendBleVersionData");
            try {
                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(15, TimeUnit.SECONDS);
                client.setReadTimeout(15, TimeUnit.SECONDS);
                client.setWriteTimeout(15, TimeUnit.SECONDS);

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
                if (OfflineConstants.isOfflineAccess(AcceptPinActivity_new.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }

            }


            return resp;
        }

    }

    private void CheckForFirmwareUpgrade() {

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

            /*if (BTConstants.CurrentTransactionIsBT) {
                AppConstants.UP_Upgrade_File_name = "BT_" + AppConstants.UP_Upgrade_File_name;
            }*/
            String LocalPath = binFolderPath + "/" + AppConstants.UP_Upgrade_File_name;
            File f = new File(LocalPath);
            if (f.exists()) {
                Log.e(TAG, "Link upgrade firmware file already exist. Skip download");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Link upgrade firmware file (" + AppConstants.UP_Upgrade_File_name + ") already exist. Skip download");
            } else {
                if (AppConstants.UP_FilePath != null) {
                    //new BackgroundServiceDownloadFirmware.DownloadLinkAndReaderFirmware().execute(AppConstants.UP_FilePath, AppConstants.UP_Upgrade_File_name, "UP_Upgrade");
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Downloading link upgrade firmware file (" + AppConstants.UP_Upgrade_File_name + ")");
                    new DownloadFileFromURL().execute(AppConstants.UP_FilePath, binFolderPath, AppConstants.UP_Upgrade_File_name);
                } else {
                    Log.e(TAG, "Link upgrade File path null");
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " Link upgrade File path null");
                }
            }
        }
    }

    public class DownloadFileFromURL extends AsyncTask<String, String, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(AcceptPinActivity_new.this);
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
    }

    @SuppressLint("ResourceAsColor")
    private void UpdateReaderStatusToUI() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                boolean ReaderStatusUI = false;

                //LF reader status on UI
                if (mDeviceName.length() > 0 && !mDeviceAddress.isEmpty() && mDisableFOBReadingForPin.equalsIgnoreCase("N")) {
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
                if (HFDeviceName.length() > 0 && !HFDeviceAddress.isEmpty() && !AppConstants.ACS_READER && mDisableFOBReadingForPin.equalsIgnoreCase("N")) {
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
                if (mMagCardDeviceAddress.length() > 0 && !mMagCardDeviceAddress.isEmpty() && mDisableFOBReadingForPin.equalsIgnoreCase("N")) {
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

                //layout_reader_status.setVisibility(View.GONE);


            }
        });

    }

    private void UnRegisterBroadcastForReader() {

        try {


            if (ServiceCardReader_pin != null)
                unregisterReceiver(ServiceCardReader_pin);
            ServiceCardReader_pin = null;

            if (mDeviceName.length() > 0 && !mDeviceAddress.isEmpty() && mDisableFOBReadingForPin.equalsIgnoreCase("N"))
                stopService(new Intent(AcceptPinActivity_new.this, ServiceLFCard.class));

            if (HFDeviceName.length() > 0 && !HFDeviceAddress.isEmpty() && mDisableFOBReadingForPin.equalsIgnoreCase("N"))
                stopService(new Intent(AcceptPinActivity_new.this, ServiceHFCard.class));

            if (mMagCardDeviceAddress.length() > 0 && !mMagCardDeviceAddress.isEmpty() && mDisableFOBReadingForPin.equalsIgnoreCase("N") && !mMagCardDeviceName.contains("MAGCARD_READERV2"))
                stopService(new Intent(AcceptPinActivity_new.this, ServiceMagCard.class));


        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "UnRegisterBroadcastForReader Exception:" + e.toString());
        }

    }

    private void RegisterBroadcastForReader() {


        try {

            if (ServiceCardReader_pin == null) {
                ServiceCardReader_pin = new BroadcastCardReader_dataFromServiceToUI();
                IntentFilter intentSFilterPIN = new IntentFilter("ServiceToActivityMagCard");
                registerReceiver(ServiceCardReader_pin, intentSFilterPIN);

                if (mDeviceName.length() > 0 && !mDeviceAddress.isEmpty() && mDisableFOBReadingForPin.equalsIgnoreCase("N"))
                    startService(new Intent(AcceptPinActivity_new.this, ServiceLFCard.class));

                if (HFDeviceName.length() > 0 && !HFDeviceAddress.isEmpty() && mDisableFOBReadingForPin.equalsIgnoreCase("N"))
                    startService(new Intent(AcceptPinActivity_new.this, ServiceHFCard.class));

                if (mMagCardDeviceAddress.length() > 0 && !mMagCardDeviceAddress.isEmpty() && mDisableFOBReadingForPin.equalsIgnoreCase("N") && !mMagCardDeviceName.contains("MAGCARD_READERV2"))
                    startService(new Intent(AcceptPinActivity_new.this, ServiceMagCard.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class BroadcastCardReader_dataFromServiceToUI extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle notificationData = intent.getExtras();

            try {
                String Action = notificationData.getString("Action");
                if (Action.equals("HFReader")) {

                    String newData = notificationData.getString("HFCardValue");
                    System.out.println("HFCard data 001 pin----" + newData);
                    // if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " " + Action + " Raw data:" + newData);
                    displayData_HF(newData);

                } else if (Action.equals("LFReader")) {

                    String newData = notificationData.getString("LFCardValue");
                    System.out.println("LFCard data 001 pin----" + newData);
                    // if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + " " + Action + " Raw data:" + newData);
                    displayData_LF(newData);

                } else if (Action.equals("MagReader")) {

                    String newData = notificationData.getString("MagCardValue");
                    System.out.println("MagCard data 002----" + newData);
                    // if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " " + Action + " Raw data:" + newData);
                    MagCard_personnel = "";
                    displayData_MagCard(newData);

                } else if (Action.equals("QRReader")) {

                    String newData = notificationData.getString("QRCodeValue");
                    System.out.println("QRCode data 001 pin----" + newData);
                    //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " " + Action + " Raw data:" + newData);

                    if (newData != null) {
                        //Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                        Barcode_pin_val = newData.trim();

                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "QR scan value: " + Barcode_pin_val);
                        if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {
                            if (!isFinishing() && barcodeReaderCall) {

                                barcodeReaderCall = false;

                                if (!Barcode_pin_val.isEmpty()) {
                                    if (!AppConstants.serverCallInProgressForPin) {
                                        AppConstants.serverCallInProgressForPin = true;
                                        new GetPinNuOnFobKeyDetection().execute();
                                    } else {
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + "<Previous server call is in queue. Skipped QR validation.>");
                                    }
                                }
                            }
                        } else {
                            //offline---------------
                            if (InScrverCall) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "Previous call in queue Skip QRcode status:" + InScrverCall);
                            } else {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "Offline Barcode Read: " + Barcode_pin_val);
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
                    AppConstants.WriteinFile(TAG + " ReconnectBleReaders Exception: " + e.toString());
            }

            return null;
        }
    }

    private void checkFor10Seconds() {

        runOnUiThread(new Runnable() {
            public void run() {
                if (sec_count == 10) {

                    if (!Constants.HF_ReaderStatus.equals("HF Connected") && !HFDeviceAddress.isEmpty() && !AppConstants.ACS_READER && mDisableFOBReadingForPin.equalsIgnoreCase("N")) {
                        new ReconnectBleReaders().execute();
                        //Toast.makeText(getApplicationContext(), "Reconnecting HF reader please wait.."+sec_count, Toast.LENGTH_SHORT).show();
                    } else if (!Constants.LF_ReaderStatus.equals("LF Connected") && !mDeviceAddress.isEmpty() && mDisableFOBReadingForPin.equalsIgnoreCase("N")) {
                        new ReconnectBleReaders().execute();
                        //Toast.makeText(getApplicationContext(), "Reconnecting LF reader please wait.."+sec_count, Toast.LENGTH_SHORT).show();
                    } else if (!Constants.Mag_ReaderStatus.equals("Mag Connected") && !mMagCardDeviceAddress.isEmpty() && mDisableFOBReadingForPin.equalsIgnoreCase("N")) {
                        new ReconnectBleReaders().execute();
                        //Toast.makeText(getApplicationContext(), "Reconnecting Mag reader please wait.."+sec_count, Toast.LENGTH_SHORT).show();
                    } else {
                        // Toast.makeText(getApplicationContext(), "Reader connected " + sec_count, Toast.LENGTH_SHORT).show();
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
                    //Toast.makeText(AcceptPinActivity_new.this, "Done", Toast.LENGTH_SHORT).show();
                    dialogBus.dismiss();
                } else {
                    if (!code.equals(AppConstants.AccessCode)) {
                        Toast.makeText(AcceptPinActivity_new.this, "Code did not match. Please try again", Toast.LENGTH_SHORT).show();
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    //Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    Barcode_pin_val = data.getStringExtra("Barcode").trim();

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Barcode scan value: " + Barcode_pin_val);

                    if (cd.isConnectingToInternet()) {
                        new GetPinNuOnFobKeyDetection().execute();
                    } else {
                        //offline---------------
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Offline Barcode Read: " + Barcode_pin_val);
                    }

                } else {

                    Barcode_pin_val = "";
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                Barcode_pin_val = "";
                Log.d(TAG, "barcode captured failed");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void AutoCloseCustomMessageDilaog(final Activity context, String title, String message) {

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
                onResume();
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
                onResume();
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

                        InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        onResume();
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
                InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
                onResume();
            }
        }, 4000);

        alertDialog.show();
    }

    private void retryConnect() {

        if (sec_count > 20 && IsGateHub.equalsIgnoreCase("True") && !PersonValidationInProgress) {
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
            hmap = controller.getPersonnelDetailsByMagCardnumber(RawString1);
            if (hmap.size() <= 0) {
                hmap = controller.getPersonnelDetailsByMagCardnumber(GetStringNormalLogic(true, RawString1));
                if (hmap.size() <= 0) {
                    hmap = controller.getPersonnelDetailsByMagCardnumber(GetStringNormalLogic(false, RawString1));
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