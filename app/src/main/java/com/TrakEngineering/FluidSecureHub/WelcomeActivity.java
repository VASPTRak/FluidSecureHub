package com.TrakEngineering.FluidSecureHub;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.TrakEngineering.FluidSecureHub.BTSPP.BTConstants;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTSPPMain;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkFour.SerialServiceFour;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkOne.SerialServiceOne;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkThree.SerialServiceThree;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkTwo.SerialServiceTwo;
import com.TrakEngineering.FluidSecureHub.BTSPP.BackgroundService_BTFour;
import com.TrakEngineering.FluidSecureHub.BTSPP.BackgroundService_BTOne;
import com.TrakEngineering.FluidSecureHub.BTSPP.BackgroundService_BTThree;
import com.TrakEngineering.FluidSecureHub.BTSPP.BackgroundService_BTTwo;
import com.TrakEngineering.FluidSecureHub.BTSPP.ClientSendAndListenUDPOne;
import com.TrakEngineering.FluidSecureHub.BTSPP.CommonFunctions;
import com.TrakEngineering.FluidSecureHub.EddystoneScanner.EddystoneScannerService;
import com.TrakEngineering.FluidSecureHub.EddystoneScanner.SampleBeacon;
import com.TrakEngineering.FluidSecureHub.MagV2GAtt.ServiceMagV2;
import com.TrakEngineering.FluidSecureHub.QRCodeGAtt.ServiceQRCode;
import com.TrakEngineering.FluidSecureHub.TLD_GattServer.DeviceControlActivity_tld;
import com.TrakEngineering.FluidSecureHub.WifiHotspot.WifiApManager;
import com.TrakEngineering.FluidSecureHub.enity.AuthEntityClass;
import com.TrakEngineering.FluidSecureHub.enity.RenameHose;
import com.TrakEngineering.FluidSecureHub.enity.StatusForUpgradeVersionEntity;
import com.TrakEngineering.FluidSecureHub.enity.UpdateMacAddressClass;
import com.TrakEngineering.FluidSecureHub.enity.UpgradeVersionEntity;
import com.TrakEngineering.FluidSecureHub.enity.UserInfoEntity;
import com.TrakEngineering.FluidSecureHub.offline.EntityHub;
import com.TrakEngineering.FluidSecureHub.offline.OffBackgroundService;
import com.TrakEngineering.FluidSecureHub.offline.OffDBController;
import com.TrakEngineering.FluidSecureHub.offline.OffTranzSyncService;
import com.TrakEngineering.FluidSecureHub.offline.OfflineConstants;
import com.TrakEngineering.FluidSecureHub.server.DownloadFileHttp;
import com.TrakEngineering.FluidSecureHub.server.MyServer;
import com.TrakEngineering.FluidSecureHub.server.ServerHandler;
import com.acs.bluetooth.Acr1255uj1Reader;
import com.acs.bluetooth.Acr3901us1Reader;
import com.acs.bluetooth.BluetoothReader;
import com.acs.bluetooth.BluetoothReaderGattCallback;
import com.acs.bluetooth.BluetoothReaderManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.squareup.picasso.Picasso;
import com.thanosfisherman.wifiutils.WifiUtils;
import com.thin.downloadmanager.ThinDownloadManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static android.os.Build.VERSION.SDK_INT;
import static com.TrakEngineering.FluidSecureHub.Constants.PREF_OFF_DB_SIZE;
import static com.TrakEngineering.FluidSecureHub.EddystoneScanner.EddystoneScannerService.CalledOnce;
import static com.TrakEngineering.FluidSecureHub.R.id.textView;
import static com.TrakEngineering.FluidSecureHub.server.MyServer.ctx;
import static com.TrakEngineering.FluidSecureHub.server.ServerHandler.TEXT;


public class WelcomeActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, View.OnTouchListener, ServiceConnection, EddystoneScannerService.OnBeaconEventListener {

    public boolean hoseClicked = false;


    OffDBController offcontroller = new OffDBController(WelcomeActivity.this);

    public boolean reconfigureForOnResume = false;

    public static HashMap<String, Date> lastFSNPDate = new HashMap<>();
    private ArrayList<String> NearByBTDevices = new ArrayList<>();
    public static int countFSVMUpgrade;
    public static SerialServiceOne service1;
    public static SerialServiceTwo service2;
    public static SerialServiceThree service3;
    public static SerialServiceFour service4;
    private boolean initialStart = true;
    public int count_uithread = 0;
    CountDownTimer countDownTimerForReconfigure = null;
    ConnectivityManager connection_manager;
    TelephonyManager telephonyManager;
    PhoneCustomStateListener psListener;

    String ssid_pass_success = "";

    private WifiAPReceiver wifiApReciver = new WifiAPReceiver();
    private NetworkReceiver receiver = new NetworkReceiver();
    public Activity activity;
    private String TAG = "Wel_Act ";
    private float density;
    ProgressDialog dialog1;
    public int ConnectCount = 0, countWifi = 0;
    private static final int ADMIN_INTENT = 1;
    public static final int REBOOT_INTENT_ID = 1234;
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mComponentName;

    private TextView textDateTime, tv_fs1_Qty, tv_fs2_Qty, tv_fs3_Qty, tv_fs4_Qty, tv_fs5_Qty, tv_fs6_Qty,
            tv_FS1_hoseName, tv_FS2_hoseName, tv_FS3_hoseName, tv_FS4_hoseName, tv_FS5_hoseName, tv_FS6_hoseName,
            tv_fs1_stop, tv_fs2_stop, tv_fs3_stop, tv_fs4_stop, tv_fs5_stop, tv_fs6_stop,
            tv_fs1QTN, tv_fs2QTN, tv_fs3QTN, tv_fs4QTN, tv_fs5QTN, tv_fs6QTN,
            tv_fs1_pulseTxt, tv_fs2_pulseTxt, tv_fs3_pulseTxt, tv_fs4_pulseTxt, tv_fs5_pulseTxt, tv_fs6_pulseTxt,
            tv_fs1_Pulse, tv_fs2_Pulse, tv_fs3_Pulse, tv_fs4_Pulse, tv_fs5_Pulse, tv_fs6_Pulse;
    private ImageView imgFuelLogo;
    private TextView tvTitle, tv_SiteName, Fa_log;
    private Spinner SpinBroadcastChannel;
    private Button btnGo, btnRetryWifi, btn_clear_data, btnTkPhoto;
    private ConnectionDetector cd = new ConnectionDetector(WelcomeActivity.this);
    private double latitude = 0;
    private double longitude = 0;
    ImageView FSlogo_img;
    TextView off_db_info, tvSSIDName, tv_NFS1, tv_NFS2, tv_NFS3, tv_NFS4, tv_NFS5, tv_NFS6, tv_FA_message, support_phone, support_email, tv_BTlinkconnection;//tv_fs1_pulse
    TextView tv_request, tv_response, tv_Display_msg, tv_file_address;
    LinearLayout linear_debug_window, linearHose, linear_fs_1, linear_fs_2, linear_fs_3, linear_fs_4, linear_fs_5, linear_fs_6,
            Fs1_beginFuel, Fs3_beginFuel, Fs2_beginFuel, Fs4_beginFuel, Fs5_beginFuel, Fs6_beginFuel,
            linearLayout_MainActivity, layout_support_info;
    WifiManager mainWifi;
    StringBuilder sb = new StringBuilder();
    private MyServer server;

    public static ArrayList<HashMap<String, String>> ListOfBleDevices = new ArrayList<>();
    public static ArrayList<HashMap<String, String>> serverSSIDList = new ArrayList<>();
    public static ArrayList<HashMap<String, String>> BTLinkList = new ArrayList<>();
    ArrayList<HashMap<String, String>> ListOfConnectedDevices = new ArrayList<>();
    public static int SelectedItemPos;
    public static int SelectedItemPosFor10Txn;
    GoogleApiClient mGoogleApiClient;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    TextView tvLatLng;
    static WifiApManager wifiApManager;
    boolean isTCancelled = false, flagGoBtn = true,
            FS1_Stpflag = true, FS2_Stpflag = true, FS3_Stpflag = true, FS4_Stpflag = true, FS5_Stpflag = true, FS6_Stpflag = true;
    int fs1Cnt5Sec = 0, fs2Cnt5Sec = 0, fs3Cnt5Sec = 0, fs4Cnt5Sec = 0, fs5Cnt5Sec = 0, fs6Cnt5Sec = 0;
    String ReaderFrequency = "", IsOdoMeterRequire = "", IsDepartmentRequire = "", IsPersonnelPINRequireForHub = "", IsPersonnelPINRequire = "", IsOtherRequire = "", IsGateHub = "", IsStayOpenGate = "", IsVehicleNumberRequire = "";
    int WifiChannelToUse = 11;
    BroadcastReceiver mReceiver;
    //Upgrade firmware status for each hose
    public static boolean IsUpgradeInprogress_FS1 = false;
    public static boolean IsUpgradeInprogress_FS2 = false;
    public static boolean IsUpgradeInprogress_FS3 = false;
    public static boolean IsUpgradeInprogress_FS4 = false;
    public static boolean IsUpgradeInprogress_FS5 = false;
    public static boolean IsUpgradeInprogress_FS6 = false;

    public static int CountBeforeReconnectRelay1 = 0;
    public static int CountBeforeReconnectRelay2 = 0;
    public static int CountBeforeReconnectRelay3 = 0;
    public static int CountBeforeReconnectRelay4 = 0;

    public static boolean FA_DebugWindow = false;

    private static final int EXPIRE_TIMEOUT = 5000;
    private static final int EXPIRE_TASK_PERIOD = 1000;
    private int BTL1State = 0, BTL2State = 0, BTL3State = 0, BTL4State = 0;
    private int BTL1counter = 0, BTL2counter = 0, BTL3counter = 0, BTL4counter = 0;

    //EddystoneScannerService
    private EddystoneScannerService mService;
    private ArrayAdapter<SampleBeacon> mAdapter;
    private ArrayList<SampleBeacon> mAdapterItems;

    //FS For Stopbutton
    String PhoneNumber;
    String consoleString = "", outputQuantity = "0";
    boolean stopTimer = true, fs1_5SecChk = false;
    double minFuelLimit = 0, numPulseRatio = 0;
    double fillqty = 0;
    ProgressDialog loading = null;
    String IpAddress = "", IsDefective = "False";
    Timer t, timerNoSleep = new Timer("Timer"), timerGate, time_cd, timerFSNP;
    Thread ui_thread;
    Date date1, date2;
    boolean EmailReaderNotConnected;
    boolean RestHoseinUse_FS1, RestHoseinUse_FS2, RestHoseinUse_FS3, RestHoseinUse_FS4, RestHoseinUse_FS5, RestHoseinUse_FS6;
    public static boolean OnWelcomeActivity;

    String HTTP_URL = "";//"http://192.168.43.153:80/";//for pipe
    String URL_GET_PULSAR_FS1, URL_SET_PULSAR_FS1, URL_WIFI_FS1, URL_RELAY_FS1,
            URL_GET_PULSAR_FS2, URL_SET_PULSAR_FS2, URL_WIFI_FS2, URL_RELAY_FS2,
            URL_GET_PULSAR_FS3, URL_SET_PULSAR_FS3, URL_WIFI_FS3, URL_RELAY_FS3,
            URL_GET_PULSAR_FS4, URL_SET_PULSAR_FS4, URL_WIFI_FS4, URL_RELAY_FS4,
            URL_GET_PULSAR_FS5, URL_SET_PULSAR_FS5, URL_WIFI_FS5, URL_RELAY_FS5,
            URL_GET_PULSAR_FS6, URL_SET_PULSAR_FS6, URL_WIFI_FS6, URL_RELAY_FS6;
    String HTTP_URL_FS_1 = "", HTTP_URL_FS_2 = "", HTTP_URL_FS_3 = "", HTTP_URL_FS_4 = "", HTTP_URL_FS_5 = "", HTTP_URL_FS_6 = "";

    String jsonRename;
    String jsonRelayOff = "{\"relay_request\":{\"Password\":\"12345678\",\"Status\":0}}";


    String URL_INFO = "";
    String URL_UPDATE_FS_INFO = "";
    String FOLDER_PATH = ""; //Environment.getExternalStorageDirectory().getAbsolutePath() + "/FSBin/";
    private WifiManager.LocalOnlyHotspotReservation mReservation;

    //============Bluetooth reader Gatt==============


    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    /* Default master key. */
    private static final String DEFAULT_3901_MASTER_KEY = "FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF";
    /* Get 8 bytes random number APDU. */
    private static final String DEFAULT_3901_APDU_COMMAND = "80 84 00 00 08";
    /* Get Serial Number command (0x02) escape command. */
    private static final String DEFAULT_3901_ESCAPE_COMMAND = "02";

    /* Default master key. */
    private static final String DEFAULT_1255_MASTER_KEY = "ACR1255U-J1 Auth";

    /* Read 16 bytes from the binary block 0x04 (MIFARE 1K or 4K). */
    private static final String DEFAULT_1255_APDU_COMMAND = "FF CA 00 00 00";
    /* Get firmware version escape command. */
    private static final String DEFAULT_1255_ESCAPE_COMMAND = "E0 00 00 48 04";

    private static final byte[] AUTO_POLLING_START = {(byte) 0xE0, 0x00, 0x00,
            0x40, 0x01};

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 3000;
    /* Reader to be connected. */
    private String mDeviceName, QRCodeReaderForBarcode, QRCodeBluetoothMacAddressForBarcode, mMagCardDeviceAddress, mMagCardDeviceName;
    private String mDeviceAddress, mDisableFOBReadingForPin, mDisableFOBReadingForVehicle;
    private int mConnectState = BluetoothReader.STATE_DISCONNECTED;

    /* UI control */
    private Button mClear;
    private Button mAuthentication;
    private Button mStartPolling;

    private Button mTransmitApdu;

    private TextView mTxtConnectionState;
    private TextView mTxtAuthentication;
    private TextView mTxtResponseApdu;
    private TextView mTxtEscapeResponse;
    private TextView mTxtCardStatus;
    private TextView mTxtBatteryLevel;
    private TextView mTxtBatteryStatus;


    private EditText mEditMasterKey;
    private EditText mEditApdu;
    private EditText mEditEscape;

    /* Detected reader. */
    private BluetoothReader mBluetoothReader;
    /* ACS Bluetooth reader library. */
    private BluetoothReaderManager mBluetoothReaderManager;
    private BluetoothReaderGattCallback mGattCallback;

    private ProgressDialog mProgressDialog;

    /* Bluetooth GATT client. */
    private BluetoothGatt mBluetoothGatt;

    private static final int OVERLAY_PERMISSION_CODE = 5463;

    //==========temp=================
    String URL_GET_TXNID = HTTP_URL + "client?command=lasttxtnid";
    String URL_SET_TXNID = HTTP_URL + "config?command=txtnid";
    String URL_GET_PULSAR = HTTP_URL + "client?command=pulsar ";
    String URL_RECORD10_PULSAR = HTTP_URL + "client?command=record10";
    String URL_RELAY = HTTP_URL + "config?command=relay";
    String PulserTimingAd = HTTP_URL + "config?command=pulsar";
    String URL_SET_PULSAR = HTTP_URL + "config?command=pulsar";
    String iot_version = "";
    ServerHandler serverHandler = new ServerHandler();
    public int HotspotEnableErrorCount = 0;
    public ProgressDialog pdOnResume;
    public ProgressDialog pdUpgradeProcess;
    public boolean showUpgradeSpinnerMessage = true;
    public Handler BTConnectionHandler = new Handler(Looper.getMainLooper());
    public int delayMillis = 100;
    public String st = "Connecting";
    public ProgressDialog pdP_Type;

    //============ Bluetooth reader Gatt end==============

    //============ For Schedule Reboot ==========
    int rebootDay, rebootHours, rebootMinutes;
    //========= Schedule Reboot end ==========

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onResume() {
        super.onResume();

        IsHotspotEnabled();
        AppConstants.IsBTLinkSelectedCurrently = false;

        AppConstants.showReaderStatus = false;
        //AppConstants.selectHosePressed = false;
        AppConstants.NonValidateVehicle_FOB_KEY = "";
        BTL1State = 0;
        BTL2State = 0;
        BTL3State = 0;
        BTL4State = 0;  //Bt link error messages #1242

        count_uithread = 0;
        qrcodebleServiceOn();
        //conditional execute only when android sdk version below 9
        if (Build.VERSION.SDK_INT < Constants.VERSION_CODES_NINE)
            ReconfigureManually();

        OnWelcomeActivity = true;
        //SyncServerData();//Check for pending SQLite data
        SyncSqliteData();

        if (cd.isConnectingToInternet()) {
            AppConstants.NETWORK_STRENGTH = true;
        } else {
            if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                AppConstants.NETWORK_STRENGTH = false;
            }
        }

        Fs1_beginFuel.setVisibility(View.GONE);
        Fs2_beginFuel.setVisibility(View.GONE);
        Fs3_beginFuel.setVisibility(View.GONE);
        Fs4_beginFuel.setVisibility(View.GONE);
        Fs5_beginFuel.setVisibility(View.GONE);
        Fs6_beginFuel.setVisibility(View.GONE);
        flagGoBtn = true;//Enable go button
        linearHose.setClickable(true);//Enable hose Selection
        ctx = WelcomeActivity.this;
        IsFARequired();//Enable disable FA on Checkbox on ui

        getipOverOSVersion();

        //Reconnect BT reader if disconnected
        ConnectCount = 0;
        ReConnectBTReader();

        tvSSIDName.setText("Tap here to select hose");
        SelectedItemPos = -1;

        final IntentFilter intentFilter = new IntentFilter();

        /* Start to monitor bond state change */
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver, intentFilter);

        /* Clear unused dialog.*/
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }

        //Hide keyboard
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        linear_fs_1.setVisibility(View.INVISIBLE);
        linear_fs_2.setVisibility(View.INVISIBLE);
        linear_fs_3.setVisibility(View.INVISIBLE);
        linear_fs_4.setVisibility(View.INVISIBLE);
        linear_fs_5.setVisibility(View.INVISIBLE);
        linear_fs_6.setVisibility(View.INVISIBLE);

        btnGo.setClickable(true);

        if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {

            AppConstants.CURRENT_STATE_MOBILEDATA = true;
            if (IsGateHub.equalsIgnoreCase("True")) {
                CheckForGateSoftwareTimer();//gate software timer executor
            } else {
                new GetSSIDUsingLocationOnResume().execute();
            }
        } else {

            AppConstants.CURRENT_STATE_MOBILEDATA = false;
            if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                new GetOfflineSSIDUsingLocationOnResume().execute();
            } else {
                AppConstants.colorToastBigFont(getApplicationContext(), AppConstants.OFF1, Color.RED);
            }
        }

        boolean showLoader = false;
        if (BTConstants.CurrentTransactionIsBT) {
            if (BTConstants.UpgradeStatusBT1.equalsIgnoreCase("Started")) {
                BTConstants.UpgradeStatusBT1 = "";
                showLoader = true;
            } else if (BTConstants.UpgradeStatusBT2.equalsIgnoreCase("Started")) {
                BTConstants.UpgradeStatusBT2 = "";
                showLoader = true;
            } else if (BTConstants.UpgradeStatusBT3.equalsIgnoreCase("Started")) {
                BTConstants.UpgradeStatusBT3 = "";
                showLoader = true;
            } else if (BTConstants.UpgradeStatusBT4.equalsIgnoreCase("Started")) {
                BTConstants.UpgradeStatusBT4 = "";
                showLoader = true;
            }
            if (showLoader) {
                BTLinkUpgradeProcessLoader();
            }
            showUpgradeSpinnerMessage = true;
        }
        UpdateFSUI_seconds();
        DeleteOldLogFiles();//Delete log files older than 1 month
        //Reconnect BT reader if disconnected
        //RetryHfreaderConnection();

        // only when screen turns on
        if (!ScreenReceiver.screenOff) {
            // this is when onResume() is called due to a screen state change
            Log.i(TAG, "SCREEN TURNED ON");
        } else {
            Log.i(TAG, "This is when onResume() is called when the screen state has not changed ");
        }

        //Connect to BT links
        ConnectAllAvailableBTLinks();

        DebugWindow();

    }

    @Override
    protected void onPause() {

        ui_thread.interrupt();

        if (time_cd != null)
            time_cd.cancel();

        if (timerGate != null)
            timerGate.cancel();


        OnWelcomeActivity = false;
        // when the screen is about to turn off
        if (ScreenReceiver.screenOff) {
            // this is the case when onPause() is called by the system due to a screen state change
            Log.i(TAG, "SCREEN TURNED OFF");
        } else {
            // this is when onPause() is called when the screen state has not changed
            Log.i(TAG, "this is when onPause() is called when the screen state has not changed");
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        closeBTSppMain();
        qrcodebleServiceOff();

        if (timerFSNP != null)
            timerFSNP.cancel();

        if (timerNoSleep != null)
            timerNoSleep.cancel();

        if (time_cd != null)
            time_cd.cancel();

        if (timerGate != null)
            timerGate.cancel();

        if (AppConstants.EnableFA) {


            if (mHandler != null && mPruneTask != null)
                mHandler.removeCallbacks(mPruneTask);

            if (mService != null)
                mService.setBeaconEventListener(null);


            unbindService(this);

        }

        OnWelcomeActivity = false;
        /* Stop to monitor bond state change */
        unregisterReceiver(mBroadcastReceiver);
        /* Disconnect Bluetooth reader */
        disconnectReader();

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        unregisterReceiver(mReceiver);

        unregisterReceiver(btreceiver);


        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }

        if (wifiApReciver != null) {
            this.unregisterReceiver(wifiApReciver);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SharedPreferences sharedPre2 = WelcomeActivity.this.getSharedPreferences("storeBT_FOBDetails", Context.MODE_PRIVATE);

        mDeviceName = sharedPre2.getString("BluetoothCardReader", "");
        mDeviceAddress = sharedPre2.getString("BTMacAddress", "");
        mDisableFOBReadingForPin = sharedPre2.getString("DisableFOBReadingForPin", "");
        mDisableFOBReadingForVehicle = sharedPre2.getString("DisableFOBReadingForVehicle", "");
        QRCodeReaderForBarcode = sharedPre2.getString("QRCodeReaderForBarcode", ""); //
        QRCodeBluetoothMacAddressForBarcode = sharedPre2.getString("QRCodeBluetoothMacAddressForBarcode", ""); //
        mMagCardDeviceName = sharedPre2.getString("MagneticCardReader", "");
        mMagCardDeviceAddress = sharedPre2.getString("MagneticCardReaderMacAddress", "");
        AppConstants.DisableAllRebootOptions = sharedPre2.getString("DisableAllReboots", "N");
        AppConstants.ScreenResolutionYOffSet = AppConstants.GetYOffsetFromScreenResolution(WelcomeActivity.this);
        System.out.println(mDeviceName + "####" + mDeviceAddress);

        timerFSNP = new Timer("TimerFSNP");
        timerForFSNPnoResponse();

        tvSSIDName = (TextView) findViewById(R.id.tvSSIDName);
        tvLatLng = (TextView) findViewById(R.id.tvLatLng);
        tvLatLng.setVisibility(View.GONE);
        AppConstants.Server_mesage = "Server Not Connected..!!!";
        if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {
            AppConstants.PRE_STATE_MOBILEDATA = true;
        } else {
            AppConstants.PRE_STATE_MOBILEDATA = false;
        }
        FOLDER_PATH = getApplicationContext().getExternalFilesDir(AppConstants.FOLDER_BIN) + "/";

        SelectedItemPos = -1;

        AppConstants.DetailsListOfConnectedDevices = new ArrayList<>();
        getSupportActionBar().setTitle(R.string.fs_name);
        getSupportActionBar().setIcon(R.drawable.fuel_secure_lock);

        density = getResources().getDisplayMetrics().density;

        TextView tvVersionNum = (TextView) findViewById(R.id.tvVersionNum);
        tvVersionNum.setText("Version " + CommonUtils.getVersionCode(WelcomeActivity.this));

        mHandler = new Handler();
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();

        InItGUI();


        //If checkbox is checked write logs in text file else not wite logs
        //And Set Fuiel branding Information
        IsLogRequiredAndBranding();

        SharedPreferences sharedPrefGatehub = WelcomeActivity.this.getSharedPreferences(Constants.PREF_COLUMN_GATE_HUB, Context.MODE_PRIVATE);
        IsGateHub = sharedPrefGatehub.getString(AppConstants.IsGateHub, "");
        IsStayOpenGate = sharedPrefGatehub.getString(AppConstants.IsStayOpenGate, "");

        //Cleare TLD data in SharedPreferance
        AppConstants.clearSharedPrefByName(WelcomeActivity.this, Constants.PREF_TldDetails);

        tv_request = (TextView) findViewById(R.id.tv_request);
        tv_response = (TextView) findViewById(R.id.tv_response);
        tv_response = (TextView) findViewById(R.id.tv_response);
        tv_Display_msg = (TextView) findViewById(R.id.tv_Display_msg);
        tv_file_address = (TextView) findViewById(R.id.tv_file_address);
        btn_clear_data = (Button) findViewById(R.id.btn_clear_data);
        tv_file_address.setText("File Download url: http://192.168.43.1:8550/FSVM/FileName.bin");

        //setUrlFromSharedPref(this);//Set url App Txt URL
        //UpdateServerMessages();
        DownloadFile();
        //getipOverOSVersion();
        KeepDataTransferAlive();//Check For FirmwreUpgrade & KeepDataTransferAlive

        clearOlderPictures(); //Clear pictures captured on GO button click which are older than 60 days

        if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
            MidnightTaskExecute();
        }
        //Network signal strength check
        /*
        psListener = new PhoneCustomStateListener();
        telephonyManager = (TelephonyManager)WelcomeActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(psListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        */

        /* //TODO  BackgroundServiceFSNP
         new Handler().postDelayed(new Runnable() {
             @Override
             public void run() {
                 BackgroundServiceFSNP();//FSNP
             }
         }, 1000);*/


        btn_clear_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //AppConstants.Server_mesage = "???";
                AppConstants.Header_data = "";
                AppConstants.Server_Request = "";
                AppConstants.Server_Response = "";
            }
        });

        layout_support_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String Offinfo = "";
                try {

                    String OfflineDataBaseSize = OfflineConstants.GetOfflineDatabaseSize(WelcomeActivity.this);

                    SharedPreferences sharedPref = ctx.getSharedPreferences(PREF_OFF_DB_SIZE, Context.MODE_PRIVATE);

                    String DbUpdateTime = sharedPref.getString(AppConstants.DbUpdateTime, "");
                    String AppMode = "";
                    if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {
                        AppMode = "ON";
                    } else {
                        AppMode = "OFF";
                    }

                    String rowCounts = offcontroller.selectRowCountOfDatabase();

                    Offinfo = OfflineDataBaseSize + " " + DbUpdateTime + " " + AppMode + " " + rowCounts;
                    Log.i(TAG, " Offinfo: " + Offinfo);

                } catch (Exception e) {
                    e.printStackTrace();
                    AppConstants.WriteinFile("layout_support_info Exception: " + e);
                }


                off_db_info.setText(Offinfo);
                off_db_info.setEnabled(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        off_db_info.setText("");
                        off_db_info.setEnabled(false);
                    }
                }, 5000);

            }
        });

        //------------Initialize receiver -Screen On/Off------------
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
        Log.i(TAG, "Initialize receiver -Screen On/Off");


        // Register for broadcasts when a device is discovered.
        IntentFilter ScanBTfilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(btreceiver, ScanBTfilter);


        /* Connect the reader. */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mConnectState == BluetoothReader.STATE_CONNECTED) {

                    disconnectReader();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mDeviceName != null && !mDeviceAddress.isEmpty() && AppConstants.ACS_READER) {
                                connectReader();
                            }
                        }
                    }, 2000);

                } else if (mConnectState == BluetoothReader.CARD_STATUS_ABSENT) {

                    String text = "The device not connected";
                    SpannableStringBuilder biggerText = new SpannableStringBuilder(text);
                    biggerText.setSpan(new RelativeSizeSpan(2.00f), 0, text.length(), 0);
                    Toast.makeText(WelcomeActivity.this, biggerText, Toast.LENGTH_LONG).show();
                    Log.i(TAG, "mConnectState: " + text);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "mConnectState: " + text);

                } else {

                    if (mDeviceName != null && !mDeviceAddress.isEmpty() && AppConstants.ACS_READER) {
                        connectReader();
                    }

                }
            }
        }, 2000);

        //Delete Log file Older then month
        File file = new File(Environment.getExternalStorageDirectory() + "/FSTimeStamp");
        if (file.exists()) {
            AppConstants.getAllFilesInDir(file);
        }

        //Enable Background service to check hotspot
        EnableHotspotBackgService();

        // set User Information
        UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetails(WelcomeActivity.this);

        AppConstants.Title = "HUB Number: " + CommonUtils.getHUBNumberByName(userInfoEntity.PersonName);//+ "\nMobile : " + userInfoEntity.PhoneNumber + "\nEmail : " + userInfoEntity.PersonEmail
        AppConstants.SiteName = "Site Name: " + userInfoEntity.FluidSecureSiteName;//+ "\nMobile : " + userInfoEntity.PhoneNumber + "\nEmail : " + userInfoEntity.PersonEmail
        AppConstants.HubName = userInfoEntity.PersonName;
        tvTitle = (TextView) findViewById(textView);
        tv_SiteName = (TextView) findViewById(R.id.tv_SiteName);
        Fa_log = (TextView) findViewById(R.id.Fa_log);
        tvTitle.setText(AppConstants.Title);
        tv_SiteName.setText(AppConstants.SiteName);
        AppConstants.WriteinFile(TAG + " HUB Name: " + userInfoEntity.PersonName);
        AppConstants.WriteinFile(TAG + " Site Name: " + userInfoEntity.FluidSecureSiteName);
        AppConstants.WriteinFile(TAG + " App Version: " + CommonUtils.getVersionCode(WelcomeActivity.this) + " " + AppConstants.getDeviceName() + " Android " + Build.VERSION.RELEASE + " ");

        wifiApManager = new WifiApManager(this);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            try {
                AppConstants.HubGeneratedpassword = PasswordGeneration();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            setHotspotNamePassword(this);
        }


        // Display current date time u
        Thread myThread = null;
        Runnable myRunnableThread = new CountDownRunner(this, textDateTime);
        myThread = new Thread(myRunnableThread);
        myThread.start();
        //end current date time----------------------------------------------

        if (ActivityHandler.screenStack != null)
            ActivityHandler.screenStack.clear();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH)
                    if (AppConstants.BUSY_STATUS)
                        new ChangeBusyStatus().execute();
            }
        }, 2000);


        btnRetryWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppConstants.colorToastBigFont(getApplicationContext(), "Please wait for few seconds....", Color.BLUE);
                Log.i(TAG, "Please wait for few seconds....");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Please wait for few seconds....");
                connectWiFiLibrary("1");
            }
        });


        /* Update UI. */
        findUiViews();
        updateUi(null);

        /* Set the onClick() event handlers. */
        setOnClickListener();

        if (AppConstants.ACS_READER) {
            //Repeat every 1min
            NoSleepSchedulerTimer();
        } else {
            Log.i(TAG, "ACS Reader Status: " + AppConstants.ACS_READER);
        }

        /* Initialize BluetoothReaderGattCallback. */
        mGattCallback = new BluetoothReaderGattCallback();

        /* Register BluetoothReaderGattCallback's listeners */
        mGattCallback
                .setOnConnectionStateChangeListener(new BluetoothReaderGattCallback.OnConnectionStateChangeListener() {

                    @Override
                    public void onConnectionStateChange(
                            final BluetoothGatt gatt, final int state,
                            final int newState) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (state != BluetoothGatt.GATT_SUCCESS) {
                                    /*
                                     * Show the message on fail to
                                     * connect/disconnect.
                                     */
                                    mConnectState = BluetoothReader.STATE_DISCONNECTED;

                                    if (newState == BluetoothReader.STATE_CONNECTED) {
                                        mTxtConnectionState
                                                .setText(R.string.connect_fail);
                                    } else if (newState == BluetoothReader.STATE_DISCONNECTED) {
                                        mTxtConnectionState
                                                .setText(R.string.disconnect_fail);
                                    }
                                    clearAllUi();
                                    updateUi(null);
                                    invalidateOptionsMenu();
                                    return;
                                }

                                updateConnectionState(newState);

                                if (newState == BluetoothProfile.STATE_CONNECTED) {
                                    /* Detect the connected reader. */
                                    if (mBluetoothReaderManager != null) {
                                        mBluetoothReaderManager.detectReader(
                                                gatt, mGattCallback);
                                    }
                                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                                    mBluetoothReader = null;
                                    /*
                                     * Release resources occupied by Bluetooth
                                     * GATT client.
                                     */
                                    if (mBluetoothGatt != null) {
                                        mBluetoothGatt.close();
                                        mBluetoothGatt = null;
                                    }
                                }
                            }
                        });
                    }
                });

        /* Initialize mBluetoothReaderManager. */
        mBluetoothReaderManager = new BluetoothReaderManager();

        /* Register BluetoothReaderManager's listeners */
        mBluetoothReaderManager
                .setOnReaderDetectionListener(new BluetoothReaderManager.OnReaderDetectionListener() {

                    @Override
                    public void onReaderDetection(BluetoothReader reader) {
                        updateUi(reader);

                        if (reader instanceof Acr3901us1Reader) {
                            /* The connected reader is ACR3901U-S1 reader. */
                            Log.v(TAG, "On Acr3901us1Reader Detected.");
                        } else if (reader instanceof Acr1255uj1Reader) {
                            /* The connected reader is ACR1255U-J1 reader. */
                            Log.v(TAG, "On Acr1255uj1Reader Detected.");
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(WelcomeActivity.this,
                                            "The device is not supported!",
                                            Toast.LENGTH_SHORT).show();
                                    Log.i(TAG, "The device is not supported!");
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "The device is not supported!");


                                    /* Disconnect Bluetooth reader */
                                    Log.v(TAG, "Disconnect reader!!!");
                                    disconnectReader();
                                    updateConnectionState(BluetoothReader.STATE_DISCONNECTED);
                                }
                            });
                            return;
                        }

                        mBluetoothReader = reader;
                        setListener(reader);
                        activateReader(reader);
                    }
                });

        //SureMDM call if battery less then 30%
        if (AppConstants.DisableAllRebootOptions.equalsIgnoreCase("N")) {
            BatteryPercentageService();
        }

        if (OfflineConstants.isOfflineAccess(WelcomeActivity.this))
            OfflineConstants.setAlarmManagerToStartDownloadOfflineData(WelcomeActivity.this);

        /*Intent i = new Intent(this, OffBackgroundService.class);
        this.startService(i);*/

        // Registers BroadcastReceiver to track network connection changes.
        IntentFilter ifilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver();
        this.registerReceiver(receiver, ifilter);

        // Registers BroadcastReceiver to track Hotspot connection changes.
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter = new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED");
        registerReceiver(wifiApReciver, mIntentFilter);

        //CallJobSchedular();//Job Scheduler hotspot check

        AppConstants.enableHotspotManuallyWindow = true;
        if (!CommonUtils.isHotspotEnabled(WelcomeActivity.this) && Constants.hotspotstayOn && AppConstants.enableHotspotManuallyWindow) {

            AppConstants.enableHotspotManuallyWindow = false;

            //AppConstants.WriteinFile(TAG + " enableMobileHotspotmanuallyStartTimer-3");
            //CommonUtils.enableMobileHotspotmanuallyStartTimer(this);
        }

        startBTSppMain(0); //BT link connection

        cancelThinDownloadManager();
    }

    public void cancelThinDownloadManager() {
        try {
            if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                //AppConstants.WriteinFile(TAG + " cancelThinDownloadManager Execute///");
                ThinDownloadManager downloadManager = new ThinDownloadManager();
                downloadManager.cancelAll();
                AppConstants.offlineDownloadIds.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelOfflineDownload() {
        try {
            cancelThinDownloadManager();
            stopService(new Intent(WelcomeActivity.this, OffBackgroundService.class));
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " cancelOfflineDownload Exception:>> " + e.getMessage());
        }
    }

    public void deleteIncompleteOfflineDataFiles() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/FSdata");
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                System.out.println("Deleted file...." + children[i]);
                new File(dir, children[i]).delete();
            }
        }
    }

    public void BackgroundServiceFSNP() {

        Calendar cal = Calendar.getInstance();
        Intent name = new Intent(WelcomeActivity.this, BackgroundServiceFSNP.class);
        PendingIntent pintent = PendingIntent.getService(getApplicationContext(), 0, name, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 60000, pintent);
    }

    public void EnableHotspotBackgService() {

        boolean screenOff = true;
        Calendar cal = Calendar.getInstance();
        Intent name = new Intent(WelcomeActivity.this, BackgroundServiceHotspotCheck.class);
        PendingIntent pintent = PendingIntent.getService(getApplicationContext(), 0, name, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 60000 * 60, pintent); //60000 * 60 = 1 hour
        //scan and enable hotspot if OFF
        Constants.hotspotstayOn = true;

    }

    //Calling background servince to reboot the device at scheduled time
    public void scheduleReboot(int hours, int minutes, int day) {
        Calendar cur_cal = new GregorianCalendar();
        cur_cal.setTimeInMillis(System.currentTimeMillis());//set the current time and date for this calendar
        Log.d(TAG, "Hours : " + hours + " Minutes : " + minutes);
        Calendar cal = new GregorianCalendar();
        //cal.add(Calendar.DAY_OF_YEAR, cur_cal.get(Calendar.DAY_OF_YEAR));
        //cal.add(Calendar.DAY_OF_WEEK, day);
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minutes);
        cal.set(Calendar.SECOND, cur_cal.get(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cur_cal.get(Calendar.MILLISECOND));
        cal.set(Calendar.DATE, cur_cal.get(Calendar.DATE));
        cal.set(Calendar.MONTH, cur_cal.get(Calendar.MONTH));

        Intent name = new Intent(WelcomeActivity.this, BackgroundServiceScheduleReboot.class);
        PendingIntent pintent = PendingIntent.getService(getApplicationContext(), REBOOT_INTENT_ID, name, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pintent);
        //alarm.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pintent);
    }

    public void MidnightTaskExecute() {

        Calendar cur_cal = new GregorianCalendar();
        cur_cal.setTimeInMillis(System.currentTimeMillis());
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 1);
        cal.set(Calendar.MINUTE, 1);

        Intent name = new Intent(WelcomeActivity.this, BackgroundServiceMidNightTasks.class);
        PendingIntent pintent = PendingIntent.getService(getApplicationContext(), REBOOT_INTENT_ID, name, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pintent);

    }

    //Calling background servince to clear pictures captured on GO button click which are older than 60 days
    public void clearOlderPictures() {
        Calendar cal = Calendar.getInstance();
        Intent name = new Intent(WelcomeActivity.this, BackgroundServiceClearOlderPictures.class);
        PendingIntent pintent = PendingIntent.getService(getApplicationContext(), 0, name, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 86400000, pintent); //86400000
    }

    public void KeepDataTransferAlive() {

        Calendar cal = Calendar.getInstance();
        Intent name = new Intent(WelcomeActivity.this, BackgroundServiceKeepDataTransferAlive.class);
        PendingIntent pintent = PendingIntent.getService(getApplicationContext(), 0, name, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 20000, pintent); //180000

    }

    @Override
    protected void onStop() {
        super.onStop();

        OnWelcomeActivity = false;
        if (loading != null) {
            loading.dismiss();
            if (!AppConstants.ManuallReconfigure) {
                Constants.hotspotstayOn = true;
            }
            loading = null;
        }

    }

    //Delete log files older than 1 month
    private void DeleteOldLogFiles() {

        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/FSLog");
            boolean exists = file.exists();
            if (exists) {
                CommonUtils.getAllFilesInDir(file);
            }

            //String LocalPath = getApplicationContext().getExternalFilesDir(AppConstants.FOLDER_BIN) + "/" + AppConstants.UP_Upgrade_File_name;
            File firmwareFile = new File(String.valueOf(getApplicationContext().getExternalFilesDir(AppConstants.FOLDER_BIN)));
            boolean exists1 = firmwareFile.exists();
            if (exists1) {
                CommonUtils.getAllFilesInDir(firmwareFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void UpdateFSUI_seconds() {

        ui_thread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                invalidateOptionsMenu();
                                // System.out.println("FS UI Update here");
                                int FS_Count = serverSSIDList.size();
                                if (!serverSSIDList.isEmpty()) {


                                    //FS Visibility on Dashboard
                                    if (FS_Count == 1) {
                                        tv_FS1_hoseName.setText(serverSSIDList.get(0).get("WifiSSId"));

                                        linear_fs_1.setVisibility(View.VISIBLE);
                                        linear_fs_2.setVisibility(View.INVISIBLE);
                                        linear_fs_3.setVisibility(View.INVISIBLE);
                                        linear_fs_4.setVisibility(View.INVISIBLE);
                                        linear_fs_5.setVisibility(View.INVISIBLE);
                                        linear_fs_6.setVisibility(View.INVISIBLE);

                                    } else if (FS_Count == 2) {


                                        //------------
                                        tv_FS1_hoseName.setText(serverSSIDList.get(0).get("WifiSSId"));
                                        tv_FS2_hoseName.setText(serverSSIDList.get(1).get("WifiSSId"));
                                        // System.out.println("MacAddress" + serverSSIDList.get(0).get("MacAddress").toString());


                                        linear_fs_1.setVisibility(View.VISIBLE);
                                        linear_fs_2.setVisibility(View.VISIBLE);

                                        linear_fs_3.setVisibility(View.INVISIBLE);
                                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) linear_fs_3.getLayoutParams();
                                        params.height = 0; // In dp
                                        linear_fs_3.setLayoutParams(params);

                                        linear_fs_4.setVisibility(View.INVISIBLE);
                                        LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) linear_fs_4.getLayoutParams();
                                        params1.height = 0; // In dp
                                        linear_fs_4.setLayoutParams(params1);
                                        linear_fs_5.setVisibility(View.INVISIBLE);
                                        linear_fs_6.setVisibility(View.INVISIBLE);

                                    } else if (FS_Count == 3) {

                                        tv_FS1_hoseName.setText(serverSSIDList.get(0).get("WifiSSId"));
                                        tv_FS2_hoseName.setText(serverSSIDList.get(1).get("WifiSSId"));
                                        tv_FS3_hoseName.setText(serverSSIDList.get(2).get("WifiSSId"));


                                        linear_fs_1.setVisibility(View.VISIBLE);
                                        linear_fs_2.setVisibility(View.VISIBLE);

                                        linear_fs_3.setVisibility(View.VISIBLE);
                                       /* LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) linear_fs_3.getLayoutParams();
                                        params.height = match_parent; // In dp
                                        linear_fs_3.setLayoutParams(params);*/

                                        linear_fs_4.setVisibility(View.INVISIBLE);
                                       /* LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) linear_fs_4.getLayoutParams();
                                        params1.height = 0; // In dp
                                        linear_fs_4.setLayoutParams(params1);*/
                                        linear_fs_5.setVisibility(View.INVISIBLE);
                                        linear_fs_6.setVisibility(View.INVISIBLE);

                                    } else if (FS_Count == 4) {

                                        tv_FS1_hoseName.setText(serverSSIDList.get(0).get("WifiSSId"));
                                        tv_FS2_hoseName.setText(serverSSIDList.get(1).get("WifiSSId"));
                                        tv_FS3_hoseName.setText(serverSSIDList.get(2).get("WifiSSId"));
                                        tv_FS4_hoseName.setText(serverSSIDList.get(3).get("WifiSSId"));

                                        linear_fs_1.setVisibility(View.VISIBLE);
                                        linear_fs_2.setVisibility(View.VISIBLE);
                                        linear_fs_3.setVisibility(View.VISIBLE);
                                        linear_fs_4.setVisibility(View.VISIBLE);
                                        linear_fs_5.setVisibility(View.INVISIBLE);
                                        linear_fs_6.setVisibility(View.INVISIBLE);
                                    } else if (FS_Count == 5) {

                                        tv_FS1_hoseName.setText(serverSSIDList.get(0).get("WifiSSId"));
                                        tv_FS2_hoseName.setText(serverSSIDList.get(1).get("WifiSSId"));
                                        tv_FS3_hoseName.setText(serverSSIDList.get(2).get("WifiSSId"));
                                        tv_FS4_hoseName.setText(serverSSIDList.get(3).get("WifiSSId"));
                                        tv_FS5_hoseName.setText(serverSSIDList.get(4).get("WifiSSId"));

                                        linear_fs_1.setVisibility(View.VISIBLE);
                                        linear_fs_2.setVisibility(View.VISIBLE);
                                        linear_fs_3.setVisibility(View.VISIBLE);
                                        linear_fs_4.setVisibility(View.VISIBLE);
                                        linear_fs_5.setVisibility(View.VISIBLE);
                                        linear_fs_6.setVisibility(View.INVISIBLE);
                                    } else if (FS_Count == 6) {

                                        tv_FS1_hoseName.setText(serverSSIDList.get(0).get("WifiSSId"));
                                        tv_FS2_hoseName.setText(serverSSIDList.get(1).get("WifiSSId"));
                                        tv_FS3_hoseName.setText(serverSSIDList.get(2).get("WifiSSId"));
                                        tv_FS4_hoseName.setText(serverSSIDList.get(3).get("WifiSSId"));
                                        tv_FS5_hoseName.setText(serverSSIDList.get(4).get("WifiSSId"));
                                        tv_FS6_hoseName.setText(serverSSIDList.get(5).get("WifiSSId"));

                                        linear_fs_1.setVisibility(View.VISIBLE);
                                        linear_fs_2.setVisibility(View.VISIBLE);
                                        linear_fs_3.setVisibility(View.VISIBLE);
                                        linear_fs_4.setVisibility(View.VISIBLE);
                                        linear_fs_5.setVisibility(View.VISIBLE);
                                        linear_fs_6.setVisibility(View.VISIBLE);
                                    }
                                }

                                IsSingleHoseRefreshReq(); //#1444 (Nic) FS 47.13.(7) or (8) Not Populating Hose Name in Selection Box When Only One Hose

                                //===Display Dashboard every Second=====
                                if (count_uithread < 60) {
                                    DisplayDashboardEveSecond();
                                } else {
                                    ui_thread.interrupt();
                                }

                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        ui_thread.start();
    }


    @Override
    public void onConnected(Bundle bundle) {

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            System.out.println("rrr" + String.valueOf(mLastLocation.getLatitude()));
            System.out.println("rrr" + String.valueOf(mLastLocation.getLongitude()));


            LocationManager locationManager = (LocationManager) WelcomeActivity.this.getSystemService(Context.LOCATION_SERVICE);
            boolean statusOfGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);


            if (!statusOfGPS) {
                latitude = 0;
                longitude = 0;
            } else {
                latitude = mLastLocation.getLatitude();// AcceptVehicleActivity.CurrentLat = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();// AcceptVehicleActivity.CurrentLng = mLastLocation.getLongitude();
            }

            if (latitude == 0 && longitude == 0) {
                AppConstants.AlertDialogFinish(WelcomeActivity.this, "Unable to get current location.\nPlease try again later!");
            }

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void InItGUI() {

        off_db_info = (TextView) findViewById(R.id.off_db_info);
        textDateTime = (TextView) findViewById(R.id.textDateTime);
        tv_fs1_Qty = (TextView) findViewById(R.id.tv_fs1_Qty);
        tv_fs2_Qty = (TextView) findViewById(R.id.tv_fs2_Qty);
        tv_fs3_Qty = (TextView) findViewById(R.id.tv_fs3_Qty);
        tv_fs4_Qty = (TextView) findViewById(R.id.tv_fs4_Qty);
        tv_fs5_Qty = (TextView) findViewById(R.id.tv_fs5_Qty);
        tv_fs6_Qty = (TextView) findViewById(R.id.tv_fs6_Qty);


        tv_FS2_hoseName = (TextView) findViewById(R.id.tv_FS2_hoseName);
        tv_FS1_hoseName = (TextView) findViewById(R.id.tv_FS1_hoseName);
        tv_FS3_hoseName = (TextView) findViewById(R.id.tv_FS3_hoseName);
        tv_FS4_hoseName = (TextView) findViewById(R.id.tv_FS4_hoseName);
        tv_FS5_hoseName = (TextView) findViewById(R.id.tv_FS5_hoseName);
        tv_FS6_hoseName = (TextView) findViewById(R.id.tv_FS6_hoseName);

        tv_fs1_pulseTxt = (TextView) findViewById(R.id.tv_fs1_pulseTxt);
        tv_fs2_pulseTxt = (TextView) findViewById(R.id.tv_fs2_pulseTxt);
        tv_fs3_pulseTxt = (TextView) findViewById(R.id.tv_fs3_pulseTxt);
        tv_fs4_pulseTxt = (TextView) findViewById(R.id.tv_fs4_pulseTxt);
        tv_fs5_pulseTxt = (TextView) findViewById(R.id.tv_fs5_pulseTxt);
        tv_fs6_pulseTxt = (TextView) findViewById(R.id.tv_fs6_pulseTxt);

        tv_fs1_Pulse = (TextView) findViewById(R.id.tv_fs1_Pulse);
        tv_fs2_Pulse = (TextView) findViewById(R.id.tv_fs2_Pulse);
        tv_fs3_Pulse = (TextView) findViewById(R.id.tv_fs3_Pulse);
        tv_fs4_Pulse = (TextView) findViewById(R.id.tv_fs4_Pulse);
        tv_fs5_Pulse = (TextView) findViewById(R.id.tv_fs5_Pulse);
        tv_fs6_Pulse = (TextView) findViewById(R.id.tv_fs6_Pulse);

        tv_fs1_stop = (TextView) findViewById(R.id.tv_fs1_stop);
        tv_fs2_stop = (TextView) findViewById(R.id.tv_fs2_stop);
        tv_fs3_stop = (TextView) findViewById(R.id.tv_fs3_stop);
        tv_fs4_stop = (TextView) findViewById(R.id.tv_fs4_stop);
        tv_fs5_stop = (TextView) findViewById(R.id.tv_fs5_stop);
        tv_fs6_stop = (TextView) findViewById(R.id.tv_fs6_stop);

        tv_NFS1 = (TextView) findViewById(R.id.tv_NFS1);
        tv_NFS2 = (TextView) findViewById(R.id.tv_NFS2);
        tv_NFS3 = (TextView) findViewById(R.id.tv_NFS3);
        tv_NFS4 = (TextView) findViewById(R.id.tv_NFS4);
        tv_NFS5 = (TextView) findViewById(R.id.tv_NFS5);
        tv_NFS6 = (TextView) findViewById(R.id.tv_NFS6);

        FSlogo_img = (ImageView) findViewById(R.id.FSlogo_img);
        support_phone = (TextView) findViewById(R.id.support_phone);
        support_email = (TextView) findViewById(R.id.support_email);
        tv_BTlinkconnection = (TextView) findViewById(R.id.tv_BTlinkconnection);

        tv_FA_message = (TextView) findViewById(R.id.tv_FA_message);

        tv_fs1QTN = (TextView) findViewById(R.id.tv_fs1QTN);
        tv_fs2QTN = (TextView) findViewById(R.id.tv_fs2QTN);
        tv_fs3QTN = (TextView) findViewById(R.id.tv_fs3QTN);
        tv_fs4QTN = (TextView) findViewById(R.id.tv_fs4QTN);
        tv_fs5QTN = (TextView) findViewById(R.id.tv_fs5QTN);
        tv_fs6QTN = (TextView) findViewById(R.id.tv_fs6QTN);

        imgFuelLogo = (ImageView) findViewById(R.id.imgFuelLogo);
        linear_debug_window = (LinearLayout) findViewById(R.id.linear_debug_window);
        linearHose = (LinearLayout) findViewById(R.id.linearHose);
        layout_support_info = (LinearLayout) findViewById(R.id.layout_support_info);
        linearLayout_MainActivity = (LinearLayout) findViewById(R.id.linearLayout_MainActivity);
        linear_fs_1 = (LinearLayout) findViewById(R.id.linear_fs_1);
        linear_fs_2 = (LinearLayout) findViewById(R.id.linear_fs_2);
        linear_fs_3 = (LinearLayout) findViewById(R.id.linear_fs_3);
        linear_fs_4 = (LinearLayout) findViewById(R.id.linear_fs_4);
        linear_fs_5 = (LinearLayout) findViewById(R.id.linear_fs_5);
        linear_fs_6 = (LinearLayout) findViewById(R.id.linear_fs_6);

        Fs1_beginFuel = (LinearLayout) findViewById(R.id.Fs1_beginFuel);
        Fs2_beginFuel = (LinearLayout) findViewById(R.id.Fs2_beginFuel);
        Fs3_beginFuel = (LinearLayout) findViewById(R.id.Fs3_beginFuel);
        Fs4_beginFuel = (LinearLayout) findViewById(R.id.Fs4_beginFuel);
        Fs5_beginFuel = (LinearLayout) findViewById(R.id.Fs5_beginFuel);
        Fs6_beginFuel = (LinearLayout) findViewById(R.id.Fs6_beginFuel);

        tv_fs1_stop.setOnClickListener(this);
        tv_fs2_stop.setOnClickListener(this);
        tv_fs3_stop.setOnClickListener(this);
        tv_fs4_stop.setOnClickListener(this);
        tv_fs5_stop.setOnClickListener(this);
        tv_fs6_stop.setOnClickListener(this);

        btnGo = (Button) findViewById(R.id.btnGo);
        btnRetryWifi = (Button) findViewById(R.id.btnRetryWifi);
        btn_clear_data = (Button) findViewById(R.id.btn_clear_data);
    }

    public void selectHoseAction(View v) {

        //Connect to BT links
        ConnectAllAvailableBTLinks();

        AppConstants.selectHosePressed = true;
        cancelOfflineDownload();

        cancelThinDownloadManager();

        if (!hoseClicked) {

            hoseClicked = true;

            //Reconnect BT reader if disconnected
            ConnectCount = 0;
            ReConnectBTReader();

            if (AppConstants.DetailsListOfConnectedDevices == null || AppConstants.DetailsListOfConnectedDevices.size() == 0) {
                getipOverOSVersion();//Refreshed donnected devices list on hose selection.
            }

            refreshWiFiList();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                btnTkPhoto.setEnabled(true);
            }
        }
    }


    //Method to launch camera for capturing image from front camera on GO button click
    public void launchCamera() {

        Camera camera = Camera.open(1);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPictureFormat(PixelFormat.JPEG);
        camera.setParameters(parameters);

        //SurfaceView mview = new SurfaceView(getBaseContext());
        SurfaceTexture surfaceTexture = new SurfaceTexture(0);

        try {
            //camera.setPreviewDisplay(mview.getHolder());
            camera.setPreviewTexture(surfaceTexture);
            camera.startPreview();
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {

                    // Uri uriTarget = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
                    OutputStream imageFileOS;

                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String fileName = "IMG_" + timeStamp;

                    File path = new File(Environment.getExternalStorageDirectory() + "/FSPictureData");

                    if (!path.exists()) {
                        File wallpaperDirectory = new File("/sdcard/FSPictureData/");
                        wallpaperDirectory.mkdirs();
                    }

                    File file = new File(new File("/sdcard/FSPictureData/"), fileName + ".png");

                    //To roate an image captured from camera since the image gets auto rotated
                    InputStream is = new ByteArrayInputStream(data);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);

                    int w = bitmap.getWidth();
                    int h = bitmap.getHeight();

                    Matrix mtx = new Matrix();
                    mtx.postRotate(270);
                    // Rotating Bitmap
                    Bitmap rotatedBMP = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    rotatedBMP.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    data = stream.toByteArray();

                    try {

                        //imageFileOS = getContentResolver().openOutputStream(uriTarget);
                        imageFileOS = new FileOutputStream(file);
                        imageFileOS.write(data);
                        imageFileOS.flush();
                        imageFileOS.close();

                        // Bitmap bmp = BitmapFactory.decodeByteArray(data,0,data.length);
                        // createDirectoryAndSaveFile(uriTarget,bmp,fileName);

                        // Toast.makeText(getApplicationContext(), "Image saved: " + file.toString(), Toast.LENGTH_LONG).show();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //finish();

                }
            });

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public void GoButtonFunctionalityForSingleLink(String LinkCommunicationType) {
        try {

            if (IsGateHub.equalsIgnoreCase("True") && LinkCommunicationType.equalsIgnoreCase("BT")) {

                if (AppConstants.GoButtonAlreadyClicked) {
                    return;
                } else {
                    AppConstants.GoButtonAlreadyClicked = true;
                }
            }

            String selSiteId = serverSSIDList.get(0).get("SiteId");
            String hoseID = serverSSIDList.get(0).get("HoseId");
            String IsUpgrade = serverSSIDList.get(0).get("IsUpgrade");
            String FirmwareVersion = serverSSIDList.get(0).get("FirmwareVersion");
            AppConstants.UP_FilePath = serverSSIDList.get(0).get("UPFilePath");
            String selSSID = serverSSIDList.get(0).get("WifiSSId");
            String selMacAddress = serverSSIDList.get(0).get("MacAddress");
            String BTselMacAddress = serverSSIDList.get(0).get("BTMacAddress");
            String FirmwareFileName = serverSSIDList.get(0).get("FirmwareFileName");
            AppConstants.CURRENT_SELECTED_SSID = selSSID;

            if (LinkCommunicationType.equalsIgnoreCase("BT")) {
                AppConstants.IsBTLinkSelectedCurrently = true;
                AppConstants.SELECTED_MACADDRESS = BTselMacAddress;
            } else {
                AppConstants.IsBTLinkSelectedCurrently = false;
                AppConstants.SELECTED_MACADDRESS = selMacAddress;
            }

            if (hoseID == null) {
                hoseID = "0";
            }
            if (IsUpgrade == null) {
                IsUpgrade = "";
            }
            if (FirmwareVersion == null) {
                FirmwareVersion = "";
            }
            if (FirmwareFileName == null) {
                FirmwareFileName = "";
            }

            if (!IsUpgrade.isEmpty()) {
                SetUpgradeFirmwareDetails(0, IsUpgrade, FirmwareVersion, FirmwareFileName, selSiteId, hoseID);
            }

            if (LinkCommunicationType.equalsIgnoreCase("BT")) {
                CheckBTConnection(0, selSSID, BTselMacAddress);
            }
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Exception in GoButtonFunctionalityForSingleLink: " + e.getMessage());
        }
    }

    public void goButtonAction(View view) {

        qrcodebleServiceOn();
        //launchCamera();     //Calling camera activity for image capture on GO button click

        try {
            if (cd.isConnectingToInternet() && serverSSIDList != null && serverSSIDList.size() == 1) {
                AppConstants.FS_selected = "0";
                AppConstants.selectHosePressed = true;
                cancelOfflineDownload();
                AppConstants.IsSingleLink = true;
                SelectedItemPos = 0;

                if (view != null) { // GO button clicked.
                    String LinkCommunicationType = serverSSIDList.get(0).get("LinkCommunicationType");
                    String selSSID = serverSSIDList.get(0).get("WifiSSId");

                    String txtnTypeForLog = "";
                    if (LinkCommunicationType.equalsIgnoreCase("BT")) {
                        txtnTypeForLog = AppConstants.LOG_TXTN_BT;
                    } else {
                        txtnTypeForLog = AppConstants.LOG_TXTN_HTTP;
                    }

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(txtnTypeForLog + "-" + TAG + "Customer select hose: " + selSSID);

                    GoButtonFunctionalityForSingleLink(LinkCommunicationType);
                    if (LinkCommunicationType.equalsIgnoreCase("BT")) {
                        return;
                    }
                }
            }
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " Exception in goButtonAction: single link selection. " + ex.getMessage());
        }

        ///////////////////common online offline///////////////////////////////
        EntityHub obj = offcontroller.getOfflineHubDetails(WelcomeActivity.this);

        OfflineConstants.storeCurrentTransaction(WelcomeActivity.this, obj.HubId, "", "", "", "", "", "", "", "", "");

        //////////////////////////////////////////

        if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {

            try {
                //Allow go button press only once
                if (flagGoBtn) {
                    flagGoBtn = false;

                    if (SelectedItemPos >= 0) {
                        AppConstants.selectHosePressed = true;
                        cancelOfflineDownload();

                        if (serverSSIDList.size() > 0) {

                            String ReconfigureLink = serverSSIDList.get(SelectedItemPos).get("ReconfigureLink");
                            String selectedSSID = serverSSIDList.get(SelectedItemPos).get("WifiSSId");
                            String ReplaceableHoseName = serverSSIDList.get(SelectedItemPos).get("ReplaceableHoseName");
                            String IsHoseNameReplaced = serverSSIDList.get(SelectedItemPos).get("IsHoseNameReplaced");
                            String SiteId = serverSSIDList.get(SelectedItemPos).get("SiteId");
                            String HoseId = serverSSIDList.get(SelectedItemPos).get("HoseId");
                            String IsTankEmpty = serverSSIDList.get(SelectedItemPos).get("IsTankEmpty");
                            String IsLinkFlagged = serverSSIDList.get(SelectedItemPos).get("IsLinkFlagged");
                            String LinkFlaggedMessage = serverSSIDList.get(SelectedItemPos).get("LinkFlaggedMessage");
                            String LinkCommunicationType = serverSSIDList.get(SelectedItemPos).get("LinkCommunicationType");
                            AppConstants.LAST_CONNECTED_SSID = selectedSSID;

                            if (IsTankEmpty.equalsIgnoreCase("True")) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "The system is low on fuel and must be refilled before fueling can start. Please contact your Manager.");
                                CommonUtils.AlertDialogAutoClose(WelcomeActivity.this, "", "The system is low on fuel and must be refilled before fueling can start. Please contact your Manager.");
                                tvSSIDName.setText("Tap here to select hose");
                                btnGo.setVisibility(View.GONE);

                            } else if (ReconfigureLink != null && ReconfigureLink.equalsIgnoreCase("true")) {

                                flagGoBtn = true;
                                Toast.makeText(getApplicationContext(), "Link Configuration flag true. please check", Toast.LENGTH_LONG).show();

                            } else if (IsLinkFlagged != null && IsLinkFlagged.equalsIgnoreCase("True")) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "Flagged Link: " + LinkFlaggedMessage);
                                CommonUtils.AlertDialogAutoClose(WelcomeActivity.this, "", LinkFlaggedMessage);
                                tvSSIDName.setText("Tap here to select hose");
                                btnGo.setVisibility(View.GONE);

                            } else {

                                if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {

                                    AppConstants.NeedToRename = false;
                                    AppConstants.REPLACEBLE_WIFI_NAME = "";
                                    AppConstants.R_HOSE_ID = "";
                                    AppConstants.R_SITE_ID = "";

                                } else {

                                    AppConstants.NeedToRename = true;
                                    AppConstants.REPLACEBLE_WIFI_NAME = ReplaceableHoseName;
                                    AppConstants.R_HOSE_ID = HoseId;
                                    AppConstants.R_SITE_ID = SiteId;

                                }

                                AppConstants.R_SITE_ID = SiteId;
                                AuthEntityClass authEntityClass = CommonUtils.getWiFiDetails(WelcomeActivity.this, selectedSSID);

                                if (authEntityClass != null) {

                                    cd = new ConnectionDetector(WelcomeActivity.this);
                                    if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {
                                        new ChangeBusyStatusOnGoButton().execute(LinkCommunicationType);
                                    } else {
                                        flagGoBtn = true;//Enable go button
                                        CommonUtils.showNoInternetDialog(WelcomeActivity.this);
                                    }

                                } else {
                                    flagGoBtn = true;//Enable go button
                                    Toast.makeText(WelcomeActivity.this, "Please try later.", Toast.LENGTH_SHORT).show();
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " goButtonAction Please try later.");
                                }

                        /*
                           // if (ssidList.contains(serverSSIDList.get(SelectedItemPos).get("item"))) {

                        } else {
                            AppConstants.alertBigActivity(WelcomeActivity.this, "Fuel site not available at this location\nPlease try again.");

                            scanLocalWiFi();
                        }*/
                            }

                        } else {
                            flagGoBtn = true;
                            CommonUtils.showCustomMessageDilaog(WelcomeActivity.this, "", "Unable to get hose list from server");
                        }
                    } else {
                        flagGoBtn = true;
                        CommonUtils.showCustomMessageDilaog(WelcomeActivity.this, "", "Please select Hose");
                    }


                } else {
                    flagGoBtn = true;
                    Toast.makeText(getApplicationContext(), "Already clicked, please try after some time..", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Already clicked, please try after some time..");
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            }
        } else {
            if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                try {
                    //Allow go button press only once
                    if (flagGoBtn) {
                        flagGoBtn = false;

                        if (SelectedItemPos >= 0) {
                            AppConstants.selectHosePressed = true;
                            cancelOfflineDownload();

                            if (serverSSIDList.size() > 0) {

                                String selectedSSID = serverSSIDList.get(SelectedItemPos).get("WifiSSId");
                                String SiteId = serverSSIDList.get(SelectedItemPos).get("SiteId");
                                String HoseId = SiteId;
                                AppConstants.LAST_CONNECTED_SSID = selectedSSID;

                                String AuthorizedFuelingDays = serverSSIDList.get(SelectedItemPos).get("AuthorizedFuelingDays");

                                if (checkFuelTimings(SiteId) && checkFuelingDay(AuthorizedFuelingDays)) {

                                    AppConstants.R_HOSE_ID = HoseId;
                                    AppConstants.R_SITE_ID = SiteId;

                                    if (obj.VehicleNumberRequired.equalsIgnoreCase("Y")) {

                                        btnGo.setClickable(false);
                                        Constants.GateHubPinNo = "";
                                        Constants.GateHubvehicleNo = "";
                                        Intent intent = new Intent(WelcomeActivity.this, AcceptVehicleActivity_new.class);
                                        startActivity(intent);

                                    } else if (obj.PersonnelPINNumberRequired.equalsIgnoreCase("Y")) {

                                        btnGo.setClickable(false);
                                        Constants.GateHubPinNo = "";
                                        Constants.GateHubvehicleNo = "";
                                        Intent intent = new Intent(WelcomeActivity.this, AcceptPinActivity_new.class);
                                        startActivity(intent);

                                    } else if (obj.IsOtherRequire.equalsIgnoreCase("True") && !obj.HUBType.equalsIgnoreCase("G")) {

                                        btnGo.setClickable(false);
                                        Constants.GateHubPinNo = "";
                                        Constants.GateHubvehicleNo = "";
                                        Intent intent = new Intent(WelcomeActivity.this, AcceptOtherActivity.class);
                                        startActivity(intent);

                                    } else {
                                        AppConstants.colorToastBigFont(getApplicationContext(), "Fuel screen", Color.BLUE);
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + "Fuel screen");

                                    }
                                } else {
                                    AppConstants.colorToastBigFont(getApplicationContext(), "Unauthorised day or timings", Color.RED);
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "Unauthorised day or timings");
                                }

                            } else {
                                flagGoBtn = true;
                                CommonUtils.showCustomMessageDilaog(WelcomeActivity.this, "", "Unable to get hose list from server");
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "Unable to get hose list from server");
                            }
                        } else {
                            flagGoBtn = true;
                            CommonUtils.showCustomMessageDilaog(WelcomeActivity.this, "", "Please select Hose");
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Please select Hose");
                        }

                    } else {
                        flagGoBtn = true;
                        Toast.makeText(getApplicationContext(), "Already clicked, please try after some time..", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Already clicked, please try after some time..");
                    }
                } catch (Exception ex) {
                    Log.e(TAG, ex.getMessage());
                }

            } else {
                AppConstants.colorToastBigFont(getApplicationContext(), AppConstants.OFF1, Color.RED);
            }
        }
    }

    @Override
    public void onCaptureDone(String pictureUrl, byte[] pictureData) {
        //Camera2SecretPictureTraker
    }

    public class handleGetAndroidSSID extends AsyncTask<String, Void, String> {

        ProgressDialog pd;
        public String LinkCommType = "";

        @Override
        protected void onPreExecute() {

            String s = "Please wait...";
            SpannableString ss2 = new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage(ss2);
            pd.setCancelable(true);
            pd.show();

        }

        protected String doInBackground(String... params) {

            String resp = "";
            String selectedSSID = params[0];
            LinkCommType = params[1];
            try {

                UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetails(WelcomeActivity.this);
                selectedSSID += "#:#0#:#0";

                System.out.println("selectedSSID.." + params[0]);
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(WelcomeActivity.this) + ":" + userInfoEntity.PersonEmail + ":" + "AndroidSSID");

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(4, TimeUnit.SECONDS);
                client.setReadTimeout(4, TimeUnit.SECONDS);
                client.setWriteTimeout(4, TimeUnit.SECONDS);


                RequestBody body = RequestBody.create(TEXT, selectedSSID);
                Request request = new Request.Builder()
                        .url(AppConstants.webURL)
                        .post(body)
                        .addHeader("Authorization", authString)
                        .build();


                Response response = null;
                response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                e.printStackTrace();
                if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }

            return resp;
        }

        @Override
        protected void onPostExecute(String siteResponse) {


            if (!WelcomeActivity.this.isFinishing() && pd != null) {
                pd.dismiss();
            }

            try {

                if (siteResponse != null && !siteResponse.isEmpty()) {


                    JSONObject jsonObjectSite = new JSONObject(siteResponse);
                    String ResponseMessageSite = jsonObjectSite.getString(AppConstants.RES_MESSAGE);

                    if (ResponseMessageSite.equalsIgnoreCase("success")) {

                        String dataSite = jsonObjectSite.getString(AppConstants.RES_DATA_SSID);
                        CommonUtils.SaveDataInPref(WelcomeActivity.this, dataSite, Constants.PREF_COLUMN_SITE);
                        startWelcomeActivity();

                    } else if (ResponseMessageSite.equalsIgnoreCase("fail")) {

                        flagGoBtn = true;//Enable go button
                        String ResponseTextSite = null;
                        ResponseTextSite = jsonObjectSite.getString(AppConstants.RES_TEXT);

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(WelcomeActivity.this);
                        // set title

                        alertDialogBuilder.setTitle("");
                        alertDialogBuilder
                                .setMessage(ResponseTextSite)
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
                } else {
                    Log.i(TAG, "HandleGetAndroidSSID SiteResponse Empty!");

                    String txtnTypeForLog = "";
                    if (LinkCommType.equalsIgnoreCase("BT")) {
                        txtnTypeForLog = AppConstants.LOG_TXTN_BT;
                    } else {
                        txtnTypeForLog = AppConstants.LOG_TXTN_HTTP;
                    }

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(txtnTypeForLog + "-" + TAG + "HandleGetAndroidSSID SiteResponse Empty!");
                }

            } catch (JSONException e) {
                e.printStackTrace();
                if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }


        }

    }


    private void startWelcomeActivity() {

        SharedPreferences sharedPrefODO = WelcomeActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsPersonnelPINRequireForHub = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequireForHub, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
        IsVehicleNumberRequire = sharedPrefODO.getString(AppConstants.IsVehicleNumberRequire, "");

        //Skip PinActivity and pass pin= "";
        if (Constants.CurrentSelectedHose != null)
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

        if (IsGateHub.equalsIgnoreCase("True") && IsStayOpenGate.equalsIgnoreCase("True") && (!Constants.GateHubPinNo.equalsIgnoreCase("") || !Constants.GateHubvehicleNo.equalsIgnoreCase(""))) {

            //Toast.makeText(getApplicationContext()," IsStayOpenGate True",Toast.LENGTH_LONG).show();
            AcceptServiceCall asc = new AcceptServiceCall();
            asc.activity = WelcomeActivity.this;
            asc.checkAllFields();

        } else if (IsVehicleNumberRequire.equalsIgnoreCase("True")) {

            btnGo.setClickable(false);
            Constants.GateHubPinNo = "";
            Constants.GateHubvehicleNo = "";
            Intent intent = new Intent(WelcomeActivity.this, AcceptVehicleActivity_new.class);
            startActivity(intent);

        } else {

            btnGo.setClickable(false);
            Constants.GateHubPinNo = "";
            Constants.GateHubvehicleNo = "";
            Intent intent = new Intent(WelcomeActivity.this, AcceptPinActivity_new.class);
            startActivity(intent);
        }

        /*if (ReaderFrequency.equalsIgnoreCase("hfr")) {
            Intent intent = new Intent(WelcomeActivity.this, AcceptVehicleActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(WelcomeActivity.this, DeviceControlActivity_fsnp.class);
            startActivity(intent);

        }*/

    }

    public void removeFSNPFromList(int index) {
        try {
            if (CalledOnce != null) {


                if (CalledOnce.size() == 1) {
                    CalledOnce.clear();
                } else {
                    String FSNPMacAddress = serverSSIDList.get(index).get("FSNPMacAddress");

                    CalledOnce.remove(FSNPMacAddress);
                }

            }
        } catch (Exception e) {
            AppConstants.WriteinFile(TAG + " Exception removeFSNPFromList" + e.getMessage());
        }
    }

    public void button1ClickCode() {

        removeFSNPFromList(0);

        //Clear FA vehicle number and personnel pin
        Constants.GateHubPinNo = "";
        Constants.GateHubvehicleNo = "";

        FS1_Stpflag = false;

        String selSSID = serverSSIDList.get(0).get("WifiSSId");
        String selMacAddress = serverSSIDList.get(0).get("MacAddress");

        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "LINK: " + selSSID + ". Stop button pressed");

        SharedPreferences sharedPref = this.getSharedPreferences("PreferanceHttpAddress", Context.MODE_PRIVATE);
        HTTP_URL_FS_1 = sharedPref.getString("HttpLinkOne", "");

        URL_GET_PULSAR_FS1 = HTTP_URL_FS_1 + "client?command=pulsar ";
        URL_SET_PULSAR_FS1 = HTTP_URL_FS_1 + "config?command=pulsar";

        URL_WIFI_FS1 = HTTP_URL_FS_1 + "config?command=wifi";
        URL_RELAY_FS1 = HTTP_URL_FS_1 + "config?command=relay";

        if (HTTP_URL_FS_1.isEmpty()) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "LINK: " + selSSID + ". HTTP URL is empty.");
        }

        stopService(new Intent(WelcomeActivity.this, BackgroundService_AP_PIPE.class));
        stopButtonFunctionality_FS1(selSSID);
        if (Constants.BusyVehicleNumberList != null) {
            Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS1);
        }
    }

    public void button2ClickCode() {

        removeFSNPFromList(1);

        FS2_Stpflag = false;
        String selSSID = serverSSIDList.get(1).get("WifiSSId");
        String selMacAddress = serverSSIDList.get(1).get("MacAddress");
        IpAddress = null;

        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "LINK: " + selSSID + ". Stop button pressed");

        SharedPreferences sharedPref = this.getSharedPreferences("PreferanceHttpAddress", Context.MODE_PRIVATE);
        HTTP_URL_FS_2 = sharedPref.getString("HttpLinkTwo", "");

        URL_GET_PULSAR_FS2 = HTTP_URL_FS_2 + "client?command=pulsar ";
        URL_SET_PULSAR_FS2 = HTTP_URL_FS_2 + "config?command=pulsar";
        URL_WIFI_FS2 = HTTP_URL_FS_2 + "config?command=wifi";
        URL_RELAY_FS2 = HTTP_URL_FS_2 + "config?command=relay";

        if (HTTP_URL_FS_2.isEmpty()) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "LINK: " + selSSID + ". HTTP URL is empty.");
        }

        stopService(new Intent(WelcomeActivity.this, BackgroundService_AP.class));
        stopButtonFunctionality_FS2(selSSID);
        if (Constants.BusyVehicleNumberList != null) {
            Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber);
        }
    }

    public void button3ClickCode() {

        removeFSNPFromList(2);

        FS3_Stpflag = false;
        String selSSID = serverSSIDList.get(2).get("WifiSSId");
        String selMacAddress = serverSSIDList.get(2).get("MacAddress");
        IpAddress = null;

        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "LINK: " + selSSID + ". Stop button pressed");

        SharedPreferences sharedPref = this.getSharedPreferences("PreferanceHttpAddress", Context.MODE_PRIVATE);
        HTTP_URL_FS_3 = sharedPref.getString("HttpLinkThree", "");

        URL_GET_PULSAR_FS3 = HTTP_URL_FS_3 + "client?command=pulsar ";
        URL_SET_PULSAR_FS3 = HTTP_URL_FS_3 + "config?command=pulsar";
        URL_WIFI_FS3 = HTTP_URL_FS_3 + "config?command=wifi";
        URL_RELAY_FS3 = HTTP_URL_FS_3 + "config?command=relay";

        if (HTTP_URL_FS_3.isEmpty()) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "LINK: " + selSSID + ". HTTP URL is empty.");
        }

        stopService(new Intent(WelcomeActivity.this, BackgroundService_FS_UNIT_3.class));
        stopButtonFunctionality_FS3(selSSID);
        if (!Constants.BusyVehicleNumberList.equals(null)) {
            Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS3);
        }
    }

    public void button4ClickCode() {

        removeFSNPFromList(3);

        FS4_Stpflag = false;
        String selSSID = serverSSIDList.get(3).get("WifiSSId");
        String selMacAddress = serverSSIDList.get(3).get("MacAddress");
        IpAddress = null;

        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "LINK: " + selSSID + ". Stop button pressed");

        SharedPreferences sharedPref = this.getSharedPreferences("PreferanceHttpAddress", Context.MODE_PRIVATE);
        HTTP_URL_FS_4 = sharedPref.getString("HttpLinkFour", "");

        URL_GET_PULSAR_FS4 = HTTP_URL_FS_4 + "client?command=pulsar ";
        URL_SET_PULSAR_FS4 = HTTP_URL_FS_4 + "config?command=pulsar";
        URL_WIFI_FS4 = HTTP_URL_FS_4 + "config?command=wifi";
        URL_RELAY_FS4 = HTTP_URL_FS_4 + "config?command=relay";

        if (HTTP_URL_FS_4.isEmpty()) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "LINK: " + selSSID + ". HTTP URL is empty.");
        }

        stopService(new Intent(WelcomeActivity.this, BackgroundService_FS_UNIT_4.class));
        stopButtonFunctionality_FS4(selSSID);
        if (Constants.BusyVehicleNumberList != null) {
            Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS4);
        }
    }

    public void button5ClickCode() {

        removeFSNPFromList(4);

        FS5_Stpflag = false;
        String selSSID = serverSSIDList.get(4).get("WifiSSId");
        String selMacAddress = serverSSIDList.get(4).get("MacAddress");
        IpAddress = null;

        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "LINK: " + selSSID + ". Stop button pressed");

        SharedPreferences sharedPref = this.getSharedPreferences("PreferanceHttpAddress", Context.MODE_PRIVATE);
        HTTP_URL_FS_5 = sharedPref.getString("HttpLinkFive", "");

        URL_GET_PULSAR_FS5 = HTTP_URL_FS_5 + "client?command=pulsar ";
        URL_SET_PULSAR_FS5 = HTTP_URL_FS_5 + "config?command=pulsar";
        URL_WIFI_FS5 = HTTP_URL_FS_5 + "config?command=wifi";
        URL_RELAY_FS5 = HTTP_URL_FS_5 + "config?command=relay";

        if (HTTP_URL_FS_5.isEmpty()) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "LINK: " + selSSID + ". HTTP URL is empty.");
        }

        stopService(new Intent(WelcomeActivity.this, BackgroundService_FS_UNIT_5.class));
        stopButtonFunctionality_FS5(selSSID);
        if (Constants.BusyVehicleNumberList != null) {
            Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS5);
        }
    }

    public void button6ClickCode() {

        removeFSNPFromList(5);

        FS6_Stpflag = false;
        String selSSID = serverSSIDList.get(5).get("WifiSSId");
        String selMacAddress = serverSSIDList.get(5).get("MacAddress");
        IpAddress = null;

        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "LINK: " + selSSID + ". Stop button pressed");

        SharedPreferences sharedPref = this.getSharedPreferences("PreferanceHttpAddress", Context.MODE_PRIVATE);
        HTTP_URL_FS_6 = sharedPref.getString("HttpLinkSix", "");

        URL_GET_PULSAR_FS6 = HTTP_URL_FS_6 + "client?command=pulsar ";
        URL_SET_PULSAR_FS6 = HTTP_URL_FS_6 + "config?command=pulsar";
        URL_WIFI_FS6 = HTTP_URL_FS_6 + "config?command=wifi";
        URL_RELAY_FS6 = HTTP_URL_FS_6 + "config?command=relay";

        if (HTTP_URL_FS_6.isEmpty()) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "LINK: " + selSSID + ". HTTP URL is empty.");
        }

        stopService(new Intent(WelcomeActivity.this, BackgroundService_FS_UNIT_6.class));
        stopButtonFunctionality_FS6(selSSID);
        if (Constants.BusyVehicleNumberList != null) {
            Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS6);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.tv_fs1_stop:

                String selSSID = serverSSIDList.get(0).get("WifiSSId");
                String LinkCommunicationType = serverSSIDList.get(0).get("LinkCommunicationType");
                if (Integer.parseInt(Constants.FS_1Pulse) <= 0) {
                    UpdateDiffStatusMessages("0");
                }

                if (LinkCommunicationType.equalsIgnoreCase("BT")) {

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "LINK: " + selSSID + ". Stop button pressed");
                    Constants.FS_1STATUS = "FREE";
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "Sending relayOff command to Link: " + selSSID);
                    BTSPPMain btspp = new BTSPPMain();
                    btspp.activity = WelcomeActivity.this;
                    btspp.send1(BTConstants.relay_off_cmd);

                } else if (LinkCommunicationType.equalsIgnoreCase("UDP")) {

                    try {
                        String MacAddress = WelcomeActivity.serverSSIDList.get(0).get("MacAddress");
                        String Serverip = "";

                        //boolean isMacConnected = false;
                        if (AppConstants.DetailsListOfConnectedDevices != null) {
                            for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                String ConnectedMacAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");

                                if (MacAddress.equalsIgnoreCase(ConnectedMacAddress)) {
                                    /*if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + MacAddress + ") is connected to hotspot.");*/
                                    Serverip = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                                    //isMacConnected = true;
                                    break;
                                }
                            }
                        }

                        /*if (!isMacConnected) {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + MacAddress + ") is not found in connected devices. " + AppConstants.DetailsListOfConnectedDevices);
                            for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                String ConnectedMacAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "Checking Mac Address using info command: (" + ConnectedMacAddress + ")");

                                String connectedIp = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");

                                Serverip = GetAndCheckMacAddressFromInfoCommand(connectedIp, MacAddress, ConnectedMacAddress);
                                if (!Serverip.trim().isEmpty()) {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile("===================================================================");
                                    break;
                                }
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile("===================================================================");
                            }
                        }*/
                        new Thread(new ClientSendAndListenUDPOne(BTConstants.relay_off_cmd, Serverip, this)).start();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else if (LinkCommunicationType.equalsIgnoreCase("HTTP")) {
                    Log.i(TAG, "on~Click tv_fs1_stop pressed");
                    tv_fs1_stop.setClickable(false);
                    button1ClickCode();

                } else {
                    //Something went wrong in hose selection
                }
                break;

            case R.id.tv_fs2_stop:

                String selSSID2 = serverSSIDList.get(1).get("WifiSSId");
                String LType2 = serverSSIDList.get(1).get("LinkCommunicationType");
                if (Integer.parseInt(Constants.FS_2Pulse) <= 0) {
                    UpdateDiffStatusMessages("1");
                }

                if (LType2.equalsIgnoreCase("BT")) {

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "LINK: " + selSSID2 + ". Stop button pressed");
                    Constants.FS_2STATUS = "FREE";
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "Sending relayOff command to Link: " + selSSID2);
                    BTSPPMain btspp = new BTSPPMain();
                    btspp.activity = WelcomeActivity.this;
                    btspp.send2(BTConstants.relay_off_cmd);

                } else if (LType2.equalsIgnoreCase("UDP")) {

                    /*try {
                        String MacAddress = WelcomeActivity.serverSSIDList.get(1).get("MacAddress");
                        String Serverip = "";

                        for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                            String SelectedMacAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                            if (MacAddress.equalsIgnoreCase(SelectedMacAddress)) {
                                String IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                                //HTTP_URL = "http://" + IpAddress + ":80/";
                                Serverip = IpAddress;
                            }
                        }
                        new Thread(new ClientSendAndListenUDPOne(BTConstants.relay_off_cmd, Serverip, this)).start();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/

                } else if (LType2.equalsIgnoreCase("HTTP")) {
                    Log.i(TAG, "on~Click tv_fs2_stop pressed");
                    tv_fs2_stop.setClickable(false);
                    button2ClickCode();

                } else {
                    //Something went wrong in hose selection
                }
                break;

            case R.id.tv_fs3_stop:

                String selSSID3 = serverSSIDList.get(2).get("WifiSSId");
                String LType3 = serverSSIDList.get(2).get("LinkCommunicationType");
                if (Integer.parseInt(Constants.FS_3Pulse) <= 0) {
                    UpdateDiffStatusMessages("2");
                }

                if (LType3.equalsIgnoreCase("BT")) {

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "LINK: " + selSSID3 + ". Stop button pressed");
                    Constants.FS_3STATUS = "FREE";
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "Sending relayOff command to Link: " + selSSID3);
                    BTSPPMain btspp = new BTSPPMain();
                    btspp.activity = WelcomeActivity.this;
                    btspp.send3(BTConstants.relay_off_cmd);

                } else if (LType3.equalsIgnoreCase("UDP")) {
                    //pending

                } else if (LType3.equalsIgnoreCase("HTTP")) {
                    Log.i(TAG, "on~Click tv_fs3_stop pressed");
                    tv_fs3_stop.setClickable(false);
                    button3ClickCode();
                } else {
                    //Something went wrong..
                }
                break;

            case R.id.tv_fs4_stop:

                String selSSID4 = serverSSIDList.get(3).get("WifiSSId");
                String LType4 = serverSSIDList.get(3).get("LinkCommunicationType");
                if (Integer.parseInt(Constants.FS_4Pulse) <= 0) {
                    UpdateDiffStatusMessages("3");
                }

                if (LType4.equalsIgnoreCase("BT")) {

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "LINK: " + selSSID4 + ". Stop button pressed");
                    Constants.FS_4STATUS = "FREE";
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "Sending relayOff command to Link: " + selSSID4);
                    BTSPPMain btspp = new BTSPPMain();
                    btspp.activity = WelcomeActivity.this;
                    btspp.send4(BTConstants.relay_off_cmd);

                } else if (LType4.equalsIgnoreCase("UDP")) {

                    //pending..

                } else if (LType4.equalsIgnoreCase("HTTP")) {
                    Log.i(TAG, "on~Click tv_fs4_stop pressed");
                    tv_fs4_stop.setClickable(false);
                    button4ClickCode();

                } else {
                    //Something went wrong in hose selection
                }
                break;

            case R.id.tv_fs5_stop:

                if (Integer.parseInt(Constants.FS_5Pulse) <= 0) {
                    UpdateDiffStatusMessages("4");
                }
                button5ClickCode();
                break;

            case R.id.tv_fs6_stop:

                if (Integer.parseInt(Constants.FS_6Pulse) <= 0) {
                    UpdateDiffStatusMessages("5");
                }
                button6ClickCode();
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        //Touch Event
        int ps = motionEvent.getAction();
        System.out.println("Touch Event" + ps);


        return false;
    }

    /* Handle connection events to the discovery service */
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {

         String className = componentName.getClassName();
        if (className.equalsIgnoreCase("com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkOne.SerialServiceOne")) {

            BTSPPMain btspp = new BTSPPMain();
            btspp.activity = WelcomeActivity.this;
            service1 = ((SerialServiceOne.SerialBinder) service).getService();
            service1.attach(btspp);
            initialStart = false;
            btspp.connect1();
            //WelcomeActivity.this.runOnUiThread(this::connect);

        } else if (className.equalsIgnoreCase("com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkTwo.SerialServiceTwo")) {

            BTSPPMain btspp = new BTSPPMain();
            btspp.activity = WelcomeActivity.this;
            service2 = ((SerialServiceTwo.SerialBinder) service).getService();
            service2.attach(btspp);
            initialStart = false;
            btspp.connect2();

        } else if (className.equalsIgnoreCase("com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkThree.SerialServiceThree")) {

            BTSPPMain btspp = new BTSPPMain();
            btspp.activity = WelcomeActivity.this;
            service3 = ((SerialServiceThree.SerialBinder) service).getService();
            service3.attach(btspp);
            initialStart = false;
            btspp.connect3();

        } else if (className.equalsIgnoreCase("com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkFour.SerialServiceFour")) {

            BTSPPMain btspp = new BTSPPMain();
            btspp.activity = WelcomeActivity.this;
            service4 = ((SerialServiceFour.SerialBinder) service).getService();
            service4.attach(btspp);
            initialStart = false;
            btspp.connect4();

        } else {
            Log.d(TAG, "Connected to Reader service");
            mService = ((EddystoneScannerService.LocalBinder) service).getService();
            mService.setBeaconEventListener(this);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

        String className = componentName.getClassName();
        if (className.equalsIgnoreCase("com.TrakEngineering.FluidSecureHub.BTSPP_LinkOne.SerialServiceOne")) {
            service1 = null;
        } else if (className.equalsIgnoreCase("com.TrakEngineering.FluidSecureHub.BTSPP_LinkTwo.SerialServiceTwo")) {
            service2 = null;
        } else if (className.equalsIgnoreCase("com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkThree.SerialServiceThree")) {
            service3 = null;
        } else if (className.equalsIgnoreCase("com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkFour.SerialServiceFour")) {
            service4 = null;
        } else {
            Log.d(TAG, "Disconnected from Reader service");
            mService = null;
        }
    }

    /* Handle callback events from the discovery service */
    @Override
    public void onBeaconIdentifier(String deviceAddress, int rssi, String instanceId) {

        final long now = System.currentTimeMillis();
        Log.i(TAG, "beacon" + deviceAddress + " instanceId:" + instanceId);
    }

    @Override
    public void onBeaconTelemetry(String deviceAddress, float battery, float temperature) {

    }

    public class GetAndroidSSID extends AsyncTask<Void, Void, Void> {

        String Email = null;
        String latLong = null;
        String response = null;
        ProgressDialog pd;

        public GetAndroidSSID(String Email, String latLong) {
            this.Email = Email;
            this.latLong = latLong;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            String s = "Please wait...";
            SpannableString ss2 = new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage(ss2);
            pd.setCancelable(true);
            pd.setCancelable(false);
            pd.show();

        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                ServerHandler serverHandler = new ServerHandler();
                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(WelcomeActivity.this) + ":" + Email + ":" + "AndroidSSID");
                response = serverHandler.PostTextData(WelcomeActivity.this, AppConstants.webURL, latLong, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {
                ex.printStackTrace();
                CommonUtils.LogMessage(TAG, "AuthTestAsynTask ", ex);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  GetAndroidSSID --Exception " + ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pd.dismiss();
        }
    }

    public void onChangeWifiAction(View view) {
        try {

            // refreshWiFiList();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "  onChangeWifiAction disable ");

        } catch (Exception ex) {
            CommonUtils.LogMessage(TAG, "onChangeWifiAction :", ex);
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "  onChangeWifiAction --Exception " + ex);
        }
    }

    public void refreshWiFiList() {

        if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {

            new GetSSIDUsingLocation().execute();
        } else {

            if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {

                if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + "OFFLINE MODE");

                new GetOfflineSSIDUsingLocation().execute();

            } else {
                hoseClicked = false;
                AppConstants.colorToastBigFont(getApplicationContext(), AppConstants.OFF1, Color.RED);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // If the permission has been checked
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.CONNECTION_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                String messageData = data.getStringExtra("MESSAGE");

                if (messageData.equalsIgnoreCase("true")) {
                    Intent intent = new Intent(WelcomeActivity.this, AcceptVehicleActivity.class);
                    startActivity(intent);
                }
            }
        }
        /////////////////////////////////////////////

        switch (requestCode) {

            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i("Splash", "User agreed to make required location settings changes.");

                        AppConstants.colorToast(getApplicationContext(), "Please wait...", Color.BLACK);

                        goButtonAction(null);

                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("Splash", "User chose not to make required location settings changes.");

                        AppConstants.colorToastBigFont(getApplicationContext(), "Please On GPS to connect WiFi", Color.BLUE);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Please On GPS to connect WiFi");
                        break;
                }
                break;
        }
    }

    public class GetSSIDUsingLocation extends AsyncTask<Void, Void, String> {


        ProgressDialog pd;

        @Override
        protected void onPreExecute() {

            String s = "Please wait..";
            SpannableString ss2 = new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);

            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage(ss2);
            pd.setCancelable(true);
            pd.setCancelable(false);
            pd.show();

        }

        protected String doInBackground(Void... arg0) {
            String resp = "";

            try {

                UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetails(WelcomeActivity.this);

                ServerHandler serverHandler = new ServerHandler();
                //----------------------------------------------------------------------------------
                String parm1 = AppConstants.getIMEI(WelcomeActivity.this) + ":" + userInfoEntity.PersonEmail + ":" + "Other";
                String parm2 = "Authenticate:I:" + Constants.Latitude + "," + Constants.Longitude;


                System.out.println("parm1----" + parm1);
                System.out.println("parm2----" + parm2);

                String authString = "Basic " + AppConstants.convertStingToBase64(parm1);

                //resp = serverHandler.PostTextData(WelcomeActivity.this, AppConstants.webURL, parm2, authString);
                //----------------------------------------------------------------------------------
                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(20, TimeUnit.SECONDS);
                client.setReadTimeout(20, TimeUnit.SECONDS);
                client.setWriteTimeout(20, TimeUnit.SECONDS);

                RequestBody body = RequestBody.create(ServerHandler.TEXT, parm2);
                Request request = new Request.Builder()
                        .url(AppConstants.webURL)
                        .post(body)
                        .addHeader("Authorization", authString)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

                //------------------------------

            } catch (Exception e) {
                hoseClicked = false;
                pd.dismiss();
                System.out.println("Ex" + e.getMessage());
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  GetSSIDUsingLocation doInBackground --Exception " + e);
                if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }


            return resp;
        }


        @Override
        protected void onPostExecute(String result) {

            hoseClicked = false;

            try {
                pd.dismiss();

                linearHose.setClickable(true);//Enable hose Selection
                tvLatLng.setText("Current Location :" + Constants.Latitude + "," + Constants.Longitude);
                System.out.println("GetSSIDUsingLocation...." + result);

                oo_post_getssid(result);
            } catch (Exception e) {
                e.printStackTrace();
                if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }
        }
    }

    public boolean isNotNULL(String value) {

        boolean flag = true;
        if (value == null) {
            flag = false;
        } else if (value.trim().isEmpty()) {
            flag = false;
        } else if (value != null && value.trim().equalsIgnoreCase("null")) {
            flag = false;
        }

        return flag;
    }

    public void alertSelectHoseList(String errMsg) {


        final Dialog dialog = new Dialog(WelcomeActivity.this);
        dialog.setTitle("");
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_hose_list);
        //dialog.getWindow().getAttributes().windowAnimations = R.style.DialogSlideAnimation;

        TextView tvNoFuelSites = (TextView) dialog.findViewById(R.id.tvNoFuelSites);
        ListView lvHoseNames = (ListView) dialog.findViewById(R.id.lvHoseNames);
        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);

        if (!errMsg.trim().isEmpty())
            tvNoFuelSites.setText(errMsg);

        if (serverSSIDList != null && serverSSIDList.size() > 0) {

            lvHoseNames.setVisibility(View.VISIBLE);
            tvNoFuelSites.setVisibility(View.GONE);

        } else {
            lvHoseNames.setVisibility(View.GONE);
            tvNoFuelSites.setVisibility(View.VISIBLE);
        }

        SimpleAdapter adapter = new SimpleAdapter(WelcomeActivity.this, serverSSIDList, R.layout.item_hose, new String[]{"item"}, new int[]{R.id.tvSingleItem});
        lvHoseNames.setAdapter(adapter);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        lvHoseNames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try {
                    dialog.dismiss();

                    /////////////////////common for online offline///////////////////////////
                    IpAddress = "";
                    SelectedItemPos = position;
                    SelectedItemPosFor10Txn = position;

                    String selSSID = serverSSIDList.get(SelectedItemPos).get("WifiSSId");
                    String selMacAddress = serverSSIDList.get(SelectedItemPos).get("MacAddress");
                    String BTselMacAddress = serverSSIDList.get(SelectedItemPos).get("BTMacAddress");
                    String selSiteId = serverSSIDList.get(SelectedItemPos).get("SiteId");
                    String hoseID = serverSSIDList.get(SelectedItemPos).get("HoseId"); //selSiteId;
                    String ReconfigureLink = serverSSIDList.get(SelectedItemPos).get("ReconfigureLink");
                    String LinkCommunicationType = serverSSIDList.get(SelectedItemPos).get("LinkCommunicationType");
                    String IsTankEmpty = serverSSIDList.get(SelectedItemPos).get("IsTankEmpty");
                    AppConstants.PulserTimingAdjust = serverSSIDList.get(SelectedItemPos).get("PulserTimingAdjust");
                    String IsLinkFlagged = serverSSIDList.get(SelectedItemPos).get("IsLinkFlagged");
                    String LinkFlaggedMessage = serverSSIDList.get(SelectedItemPos).get("LinkFlaggedMessage");
                    String IsUpgrade = serverSSIDList.get(SelectedItemPos).get("IsUpgrade");
                    String UPFilePath = serverSSIDList.get(SelectedItemPos).get("UPFilePath");
                    String FirmwareVersion = serverSSIDList.get(SelectedItemPos).get("FirmwareVersion");
                    String FirmwareFileName = serverSSIDList.get(SelectedItemPos).get("FirmwareFileName");
                    AppConstants.IsResetSwitchTimeBounce = serverSSIDList.get(SelectedItemPos).get("IsResetSwitchTimeBounce");
                    if (ReconfigureLink == null) {
                        ReconfigureLink = "";
                    }

                    AppConstants.CURRENT_SELECTED_SSID = selSSID;
                    AppConstants.CURRENT_HOSE_SSID = hoseID;
                    AppConstants.CURRENT_SELECTED_SITEID = selSiteId;
                    AppConstants.SELECTED_MACADDRESS = selMacAddress;
                    AppConstants.SITE_ID = selSiteId;
                    AppConstants.UP_FilePath = UPFilePath;

                    if (IsUpgrade == null) {
                        IsUpgrade = "";
                    }
                    if (FirmwareVersion == null) {
                        FirmwareVersion = "";
                    }
                    if (FirmwareFileName == null) {
                        FirmwareFileName = "";
                    }

                    String txtnTypeForLog = "";
                    if (LinkCommunicationType.equalsIgnoreCase("BT")) {
                        txtnTypeForLog = AppConstants.LOG_TXTN_BT;
                    } else {
                        txtnTypeForLog = AppConstants.LOG_TXTN_HTTP;
                    }

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(txtnTypeForLog + "-" + TAG + "Customer select hose: " + selSSID + " (position: " + (position + 1) + " of " + serverSSIDList.size() + ")");

                    if (IsTankEmpty != null && IsTankEmpty.equalsIgnoreCase("True")) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "The system is low on fuel and must be refilled before fueling can start. Please contact your Manager.");
                        CommonUtils.AlertDialogAutoClose(WelcomeActivity.this, "", "The system is low on fuel and must be refilled before fueling can start. Please contact your Manager.");
                        tvSSIDName.setText("Tap here to select hose");
                        btnGo.setVisibility(View.GONE);

                    } else if (LinkCommunicationType.equalsIgnoreCase("BT")) {
                        SetBTLinksMacAddress(SelectedItemPos, BTselMacAddress);
                        AppConstants.IsBTLinkSelectedCurrently = true;
                        if (ReconfigureLink != null && ReconfigureLink.equalsIgnoreCase("true")) {

                            Intent i = new Intent(WelcomeActivity.this, PairDeviceActivity.class);
                            i.putExtra("linkNumber", SelectedItemPos);
                            WelcomeActivity.this.startActivity(i);

                        } else if (IsLinkFlagged != null && IsLinkFlagged.equalsIgnoreCase("True")) {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Flagged Link: " + LinkFlaggedMessage);
                            CommonUtils.AlertDialogAutoClose(WelcomeActivity.this, "", LinkFlaggedMessage);
                            RestrictHoseSelection("Please try again later");

                        } else if (CommonFunctions.CheckIfPresentInPairedDeviceList(BTselMacAddress)) {
                            AppConstants.SELECTED_MACADDRESS = BTselMacAddress;
                            OfflineConstants.storeCurrentTransaction(WelcomeActivity.this, "", selSiteId, "", "", "", "", "", AppConstants.currentDateFormat("yyyy-MM-dd HH:mm"), "", "");

                            if (!IsUpgrade.isEmpty()) {
                                SetUpgradeFirmwareDetails(position, IsUpgrade, FirmwareVersion, FirmwareFileName, selSiteId, hoseID);
                            }

                            CheckBTConnection(SelectedItemPos, selSSID, BTselMacAddress);
                        } else {
                            //AppConstants.colorToastBigFont(getApplicationContext(), "Selected Link not in BT paired list", Color.RED);
                            CommonUtils.AutoCloseBTLinkMessage(WelcomeActivity.this, "", getResources().getString(R.string.BTLinkNotInPairList));
                            BTConstants.CurrentSelectedLinkBT = 0;
                            RestrictHoseSelection("Pairing Mode..."); //"Please try again later" changed as per #1899.
                        }
                    } else if (LinkCommunicationType.equalsIgnoreCase("UDP")) {

                        AppConstants.colorToastBigFont(getApplicationContext(), "UDP Link Selected", Color.RED);
                        tvSSIDName.setText("Please try again later");
                        BTConstants.CurrentSelectedLinkBT = 0;
                        btnGo.setVisibility(View.GONE);

                        /*if (ReconfigureLink != null && ReconfigureLink.equalsIgnoreCase("true")) {
                            //UDP Reconfigure process
                            UDPLinkReConfigurationProcessStep1();
                        } else {
                            //UDP transaction.
                            CheckUDPConnection(SelectedItemPos, selSSID, selMacAddress);
                        }*/

                    } else {
                        //Normal hub app code below....

                        BTConstants.CurrentTransactionIsBT = false;
                        BTConstants.CurrentSelectedLinkBT = 0;
                        OfflineConstants.storeCurrentTransaction(WelcomeActivity.this, "", selSiteId, "", "", "", "", "", AppConstants.currentDateFormat("yyyy-MM-dd HH:mm"), "", "");

                        /////////////////////////////////////////////////////
                        //Check hotspot manually
                        try {
                            if (!CommonUtils.isHotspotEnabled(WelcomeActivity.this) && !ReconfigureLink.equalsIgnoreCase("true")) {

                                Log.i(TAG, "EMobileHotspotManually");
                                //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "EMobileHotspotManually");
                                //CommonUtils.enableMobileHotspotmanuallyStartTimer(WelcomeActivity.this);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "onItemClick Check hotspot manually Exception:" + e);
                            //CommonUtils.enableMobileHotspotmanuallyStartTimer(WelcomeActivity.this);
                        }

                        if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {

                            IsDefective = "False";
                            /* IpAddress = "";
                            SelectedItemPos = position;
                            SelectedItemPosFor10Txn = position;*/

                            //String selSSID = serverSSIDList.get(SelectedItemPos).get("WifiSSId");
                            String IsTLDCall = serverSSIDList.get(SelectedItemPos).get("IsTLDCall");
                            String IsBusy = serverSSIDList.get(SelectedItemPos).get("IsBusy");

                            //String selMacAddress = serverSSIDList.get(SelectedItemPos).get("MacAddress");
                            //String selSiteId = serverSSIDList.get(SelectedItemPos).get("SiteId");
                            hoseID = serverSSIDList.get(SelectedItemPos).get("HoseId");
                            //String IsUpgrade = serverSSIDList.get(SelectedItemPos).get("IsUpgrade"); //"Y";
                            //String FirmwareVersion = serverSSIDList.get(SelectedItemPos).get("FirmwareVersion"); //"Y";
                            AppConstants.CURRENT_SELECTED_SSID_ReqTLDCall = IsTLDCall;
                            AppConstants.CURRENT_SELECTED_SSID = selSSID;
                            AppConstants.CURRENT_HOSE_SSID = hoseID;
                            AppConstants.CURRENT_SELECTED_SITEID = selSiteId;
                            AppConstants.SELECTED_MACADDRESS = selMacAddress;
                            String IsHoseNameReplaced = serverSSIDList.get(SelectedItemPos).get("IsHoseNameReplaced");
                            String ReplaceableHoseName = serverSSIDList.get(SelectedItemPos).get("ReplaceableHoseName");
                            String PulserTimingAd = serverSSIDList.get(SelectedItemPos).get("PulserTimingAdjust");
                            IsDefective = serverSSIDList.get(SelectedItemPos).get("IsDefective");
                            AppConstants.PulserTimingAdjust = PulserTimingAd;
                            AppConstants.IsResetSwitchTimeBounce = serverSSIDList.get(SelectedItemPos).get("IsResetSwitchTimeBounce");

                            //tld is upgrade
                            String IsTLDFirmwareUpgrade = serverSSIDList.get(SelectedItemPos).get("IsTLDFirmwareUpgrade");
                            String TLDFirmwareFilePath = serverSSIDList.get(SelectedItemPos).get("TLDFirmwareFilePath");
                            String TLDFIrmwareVersion = serverSSIDList.get(SelectedItemPos).get("TLDFIrmwareVersion");
                            String PROBEMacAddress = serverSSIDList.get(SelectedItemPos).get("PROBEMacAddress");

                            CommonUtils.SaveTldDetailsInPref(WelcomeActivity.this, IsTLDCall, IsTLDFirmwareUpgrade, TLDFirmwareFilePath, TLDFIrmwareVersion, PROBEMacAddress, selMacAddress);

                            /////////////////////////////////////////////////////

                            if (!IsUpgrade.isEmpty()) {
                                SetUpgradeFirmwareDetails(position, IsUpgrade, FirmwareVersion, FirmwareFileName, selSiteId, hoseID);
                            }

                            //Rename SSID while mac address updation
                            if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {
                                AppConstants.NeedToRenameFS_ON_UPDATE_MAC = false;
                                AppConstants.REPLACEBLE_WIFI_NAME_FS_ON_UPDATE_MAC = "";
                            } else {
                                AppConstants.NeedToRenameFS_ON_UPDATE_MAC = true;
                                AppConstants.REPLACEBLE_WIFI_NAME_FS_ON_UPDATE_MAC = ReplaceableHoseName;
                            }

                            //#728 Inability to Connect to Links
                            //Check for link not connected message
                            //InabilityToConnectLink(selSSID);

                            try {

                                if (IsDefective != null && IsDefective.equalsIgnoreCase("True")) {//some issue

                                    tvSSIDName.setText("Hose out of order");
                                    btnGo.setVisibility(View.GONE);

                                } else {

                                    //Link ReConfiguration process start
                                    if (ReconfigureLink != null && ReconfigureLink.equalsIgnoreCase("true")) {
                                        if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE") && Constants.FS_5STATUS.equalsIgnoreCase("FREE") && Constants.FS_6STATUS.equalsIgnoreCase("FREE")) {

                                            reconfigureProcessBelowAndroid10(); //Android Below 10

                                        } else {
                                            AppConstants.colorToastBigFont(getApplicationContext(), "Can't reconfigure hose is busy", Color.RED);
                                        }
                                    } else if (IsLinkFlagged != null && IsLinkFlagged.equalsIgnoreCase("True")) {
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + "Flagged Link: " + LinkFlaggedMessage);
                                        CommonUtils.AlertDialogAutoClose(WelcomeActivity.this, "", LinkFlaggedMessage);
                                        RestrictHoseSelection("Please try again later");

                                    } else {

                                        AppConstants.ManuallReconfigure = false;
                                        IpAddress = "";

                                        if (AppConstants.DetailsListOfConnectedDevices != null) {
                                            for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                                String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");

                                                if (selMacAddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                                                    IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                                                    break;
                                                }
                                            }
                                        }

                                        /*try {

                                            IpAddress = "";

                                            boolean isMacConnected = false;
                                            if (AppConstants.DetailsListOfConnectedDevices != null) {
                                                for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                                    String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");

                                                    if (selMacAddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                                                        if (AppConstants.GenerateLogs)
                                                            AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + selMacAddress + ") is connected to hotspot.");
                                                        IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                                                        isMacConnected = true;
                                                        break;
                                                    }
                                                }
                                            }

                                            if (!isMacConnected) {
                                                if (AppConstants.GenerateLogs)
                                                    AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + selMacAddress + ") is not found in connected devices. " + AppConstants.DetailsListOfConnectedDevices);
                                                for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                                    String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                                                    if (AppConstants.GenerateLogs)
                                                        AppConstants.WriteinFile(TAG + "Checking Mac Address using info command: (" + MA_ConnectedDevices + ")");

                                                    String connectedIp = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");

                                                    IpAddress = GetAndCheckMacAddressFromInfoCommand(connectedIp, selMacAddress, MA_ConnectedDevices);
                                                    if (!IpAddress.trim().isEmpty()) {
                                                        if (AppConstants.GenerateLogs)
                                                            AppConstants.WriteinFile("===================================================================");
                                                        break;
                                                    }
                                                    if (AppConstants.GenerateLogs)
                                                        AppConstants.WriteinFile("===================================================================");
                                                }
                                            }
                                        } catch (Exception e) {
                                            System.out.println(e);
                                            if (AppConstants.GenerateLogs)
                                                AppConstants.WriteinFile(TAG + "  DetailsListOfConnectedDevices --Empty ");
                                        }*/

                                        /*if (IpAddress.equals("")) {
                                            if (AppConstants.GenerateLogs)
                                                AppConstants.WriteinFile(TAG + " Issue #812 HNC-1 (selMacAddress:" + selMacAddress + ") " + AppConstants.DetailsListOfConnectedDevices);
                                            if (AppConstants.GenerateLogs)
                                                AppConstants.WriteinFile(TAG + " Hose not connected");
                                            RestrictHoseSelection("Hose not connected");
                                            getipOverOSVersion();

                                        } else {*/

                                        //Selected position

                                        AppConstants.FS_selected = String.valueOf(position);
                                        if (String.valueOf(position).equalsIgnoreCase("0") && !IsUpgradeInprogress_FS1) {


                                            AppConstants.LastSelectedHose = String.valueOf(position);
                                            if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && IsBusy.equalsIgnoreCase("N")) {
                                                // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

                                                //Rename SSID from cloud
                                                if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {
                                                    AppConstants.NeedToRenameFS1 = false;
                                                    AppConstants.REPLACEBLE_WIFI_NAME_FS1 = "";
                                                } else {
                                                    AppConstants.NeedToRenameFS1 = true;
                                                    AppConstants.REPLACEBLE_WIFI_NAME_FS1 = ReplaceableHoseName;
                                                }

                                                Constants.AccPersonnelPIN = "";
                                                tvSSIDName.setText(selSSID);
                                                AppConstants.FS1_CONNECTED_SSID = selSSID;
                                                Constants.CurrentSelectedHose = "FS1";
                                                btnGo.setVisibility(View.VISIBLE);
                                                goButtonAction(null);
                                            } else {
                                                RestHoseinUse_FS1 = true;
                                                RestrictHoseSelection("Hose in use.\nPlease try again later");

                                            }

                                        } else if (String.valueOf(position).equalsIgnoreCase("1") && !IsUpgradeInprogress_FS2) {

                                            AppConstants.LastSelectedHose = String.valueOf(position);
                                            if (Constants.FS_2STATUS.equalsIgnoreCase("FREE") && IsBusy.equalsIgnoreCase("N")) {
                                                // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

                                                //Rename SSID from cloud
                                                if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {
                                                    AppConstants.NeedToRenameFS2 = false;
                                                    AppConstants.REPLACEBLE_WIFI_NAME_FS2 = "";
                                                } else {
                                                    AppConstants.NeedToRenameFS2 = true;
                                                    AppConstants.REPLACEBLE_WIFI_NAME_FS2 = ReplaceableHoseName;
                                                }

                                                Constants.AccPersonnelPIN = "";
                                                tvSSIDName.setText(selSSID);
                                                AppConstants.FS2_CONNECTED_SSID = selSSID;
                                                Constants.CurrentSelectedHose = "FS2";
                                                btnGo.setVisibility(View.VISIBLE);
                                                goButtonAction(null);
                                            } else {
                                                RestHoseinUse_FS2 = true;
                                                RestrictHoseSelection("Hose in use.\nPlease try again later");
                                            }

                                        } else if (String.valueOf(position).equalsIgnoreCase("2") && !IsUpgradeInprogress_FS3) {

                                            AppConstants.LastSelectedHose = String.valueOf(position);
                                            if (Constants.FS_3STATUS.equalsIgnoreCase("FREE") && IsBusy.equalsIgnoreCase("N")) {
                                                // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

                                                //Rename SSID from cloud
                                                if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {
                                                    AppConstants.NeedToRenameFS3 = false;
                                                    AppConstants.REPLACEBLE_WIFI_NAME_FS3 = "";
                                                } else {
                                                    AppConstants.NeedToRenameFS3 = true;
                                                    AppConstants.REPLACEBLE_WIFI_NAME_FS3 = ReplaceableHoseName;
                                                }

                                                Constants.AccPersonnelPIN = "";
                                                tvSSIDName.setText(selSSID);
                                                AppConstants.FS3_CONNECTED_SSID = selSSID;
                                                Constants.CurrentSelectedHose = "FS3";
                                                btnGo.setVisibility(View.VISIBLE);
                                                goButtonAction(null);
                                            } else {
                                                RestHoseinUse_FS3 = true;
                                                RestrictHoseSelection("Hose in use.\nPlease try again later");
                                            }


                                        } else if (String.valueOf(position).equalsIgnoreCase("3") && !IsUpgradeInprogress_FS4) {

                                            AppConstants.LastSelectedHose = String.valueOf(position);
                                            if (Constants.FS_4STATUS.equalsIgnoreCase("FREE") && IsBusy.equalsIgnoreCase("N")) {
                                                // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                                                //Rename SSID from cloud
                                                if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {
                                                    AppConstants.NeedToRenameFS4 = false;
                                                    AppConstants.REPLACEBLE_WIFI_NAME_FS4 = "";
                                                } else {
                                                    AppConstants.NeedToRenameFS4 = true;
                                                    AppConstants.REPLACEBLE_WIFI_NAME_FS4 = ReplaceableHoseName;
                                                }

                                                Constants.AccPersonnelPIN = "";
                                                tvSSIDName.setText(selSSID);
                                                AppConstants.FS4_CONNECTED_SSID = selSSID;
                                                Constants.CurrentSelectedHose = "FS4";
                                                btnGo.setVisibility(View.VISIBLE);
                                                goButtonAction(null);
                                            } else {
                                                RestrictHoseSelection("Hose in use.\nPlease try again later");
                                            }
                                        } else if (String.valueOf(position).equalsIgnoreCase("4") && !IsUpgradeInprogress_FS5) {


                                            AppConstants.LastSelectedHose = String.valueOf(position);
                                            if (Constants.FS_5STATUS.equalsIgnoreCase("FREE") && IsBusy.equalsIgnoreCase("N")) {
                                                // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                                                //Rename SSID from cloud
                                                if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {
                                                    AppConstants.NeedToRenameFS5 = false;
                                                    AppConstants.REPLACEBLE_WIFI_NAME_FS5 = "";
                                                } else {
                                                    AppConstants.NeedToRenameFS5 = true;
                                                    AppConstants.REPLACEBLE_WIFI_NAME_FS5 = ReplaceableHoseName;
                                                }

                                                Constants.AccPersonnelPIN = "";
                                                tvSSIDName.setText(selSSID);
                                                AppConstants.FS5_CONNECTED_SSID = selSSID;
                                                Constants.CurrentSelectedHose = "FS5";
                                                btnGo.setVisibility(View.VISIBLE);
                                            } else {
                                                RestrictHoseSelection("Hose in use.\nPlease try again later");
                                            }

                                        } else if (String.valueOf(position).equalsIgnoreCase("5") && !IsUpgradeInprogress_FS6) {

                                            AppConstants.LastSelectedHose = String.valueOf(position);
                                            if (Constants.FS_6STATUS.equalsIgnoreCase("FREE") && IsBusy.equalsIgnoreCase("N")) {

                                                if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {
                                                    AppConstants.NeedToRenameFS6 = false;
                                                    AppConstants.REPLACEBLE_WIFI_NAME_FS6 = "";
                                                } else {
                                                    AppConstants.NeedToRenameFS6 = true;
                                                    AppConstants.REPLACEBLE_WIFI_NAME_FS6 = ReplaceableHoseName;
                                                }

                                                Constants.AccPersonnelPIN = "";
                                                tvSSIDName.setText(selSSID);
                                                AppConstants.FS6_CONNECTED_SSID = selSSID;
                                                Constants.CurrentSelectedHose = "FS6";
                                                btnGo.setVisibility(View.VISIBLE);
                                                goButtonAction(null);
                                            } else {
                                                RestrictHoseSelection("Hose in use.\nPlease try again later");
                                            }
                                        } else {

                                            tvSSIDName.setText("Please try again soon");
                                            btnGo.setVisibility(View.GONE);
                                            RestHoseinUse_FS4 = true;
                                        }
                                        //}

                                    }
                                }
                                //dialog.dismiss();
                            } catch (Exception e) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "  IsDefective-");
                                e.printStackTrace();
                            }

                        } else {

                            ///offline

                            try {
                                IpAddress = "";

                                if (AppConstants.DetailsListOfConnectedDevices != null) {
                                    for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                        String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");

                                        if (selMacAddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                                            IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                                            break;
                                        }
                                    }
                                }
                                /*try {
                                    IpAddress = "";

                                    boolean isMacConnected = false;
                                    if (AppConstants.DetailsListOfConnectedDevices != null) {
                                        for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                            String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");

                                            if (selMacAddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                                                if (AppConstants.GenerateLogs)
                                                    AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + selMacAddress + ") is connected to hotspot.");
                                                IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                                                isMacConnected = true;
                                                break;
                                            }
                                        }
                                    }

                                    if (!isMacConnected) {
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + selMacAddress + ") is not found in connected devices. " + AppConstants.DetailsListOfConnectedDevices);
                                        for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                            String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                                            if (AppConstants.GenerateLogs)
                                                AppConstants.WriteinFile(TAG + "Checking Mac Address using info command: (" + MA_ConnectedDevices + ")");

                                            String connectedIp = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");

                                            IpAddress = GetAndCheckMacAddressFromInfoCommand(connectedIp, selMacAddress, MA_ConnectedDevices);
                                            if (!IpAddress.trim().isEmpty()) {
                                                if (AppConstants.GenerateLogs)
                                                    AppConstants.WriteinFile("===================================================================");
                                                break;
                                            }
                                            if (AppConstants.GenerateLogs)
                                                AppConstants.WriteinFile("===================================================================");
                                        }
                                    }
                                } catch (Exception e) {
                                    System.out.println(e);
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " Exception while checking hotspot connected devices in Offline mode.--Exception: " + e.getMessage());
                                }*/

                                /*if (IpAddress.equals("")) {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " Issue #812 HNC-2(selMacAddress:" + selMacAddress + ") " + AppConstants.DetailsListOfConnectedDevices);
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " Hose not connected");
                                    RestrictHoseSelection("Hose not connected");

                                } else {*/

                                //Selected position
                                //Toast.makeText(getApplicationContext(), "FS Position" + position, Toast.LENGTH_SHORT).show();
                                AppConstants.FS_selected = String.valueOf(position);
                                if (String.valueOf(position).equalsIgnoreCase("0") && !IsUpgradeInprogress_FS1) {


                                    AppConstants.LastSelectedHose = String.valueOf(position);
                                    if (Constants.FS_1STATUS.equalsIgnoreCase("FREE")) {
                                        // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

                                        Constants.AccPersonnelPIN = "";
                                        tvSSIDName.setText(selSSID);
                                        AppConstants.FS1_CONNECTED_SSID = selSSID;
                                        Constants.CurrentSelectedHose = "FS1";
                                        btnGo.setVisibility(View.VISIBLE);
                                        goButtonAction(null);
                                    } else {
                                        RestrictHoseSelection("Hose in use.\nPlease try again later");

                                    }
                                } else if (String.valueOf(position).equalsIgnoreCase("1") && !IsUpgradeInprogress_FS2) {

                                    AppConstants.LastSelectedHose = String.valueOf(position);
                                    if (Constants.FS_2STATUS.equalsIgnoreCase("FREE")) {
                                        // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));


                                        Constants.AccPersonnelPIN = "";
                                        tvSSIDName.setText(selSSID);
                                        AppConstants.FS2_CONNECTED_SSID = selSSID;
                                        Constants.CurrentSelectedHose = "FS2";
                                        btnGo.setVisibility(View.VISIBLE);
                                        goButtonAction(null);
                                    } else {
                                        RestrictHoseSelection("Hose in use.\nPlease try again later");
                                    }

                                } else if (String.valueOf(position).equalsIgnoreCase("2") && !IsUpgradeInprogress_FS3) {

                                    AppConstants.LastSelectedHose = String.valueOf(position);
                                    if (Constants.FS_3STATUS.equalsIgnoreCase("FREE")) {
                                        // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));


                                        Constants.AccPersonnelPIN = "";
                                        tvSSIDName.setText(selSSID);
                                        AppConstants.FS3_CONNECTED_SSID = selSSID;
                                        Constants.CurrentSelectedHose = "FS3";
                                        btnGo.setVisibility(View.VISIBLE);
                                        goButtonAction(null);
                                    } else {
                                        RestrictHoseSelection("Hose in use.\nPlease try again later");
                                    }


                                } else if (String.valueOf(position).equalsIgnoreCase("3") && !IsUpgradeInprogress_FS4) {

                                    AppConstants.LastSelectedHose = String.valueOf(position);
                                    if (Constants.FS_4STATUS.equalsIgnoreCase("FREE")) {
                                        // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));


                                        Constants.AccPersonnelPIN = "";
                                        tvSSIDName.setText(selSSID);
                                        AppConstants.FS4_CONNECTED_SSID = selSSID;
                                        Constants.CurrentSelectedHose = "FS4";
                                        btnGo.setVisibility(View.VISIBLE);
                                        goButtonAction(null);
                                    } else {
                                        RestrictHoseSelection("Hose in use.\nPlease try again later");
                                    }
                                } else {

                                    tvSSIDName.setText("Please try again soon");
                                    btnGo.setVisibility(View.GONE);
                                }
                                //}


                            } catch (Exception e) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "  IsDefective-");
                                e.printStackTrace();
                            }

                            /////////////////////offfline///////////////////////////

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        dialog.show();

    }

    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {

            ArrayList<String> connections = new ArrayList<String>();
            ArrayList<Float> Signal_Strenth = new ArrayList<Float>();

            sb = new StringBuilder();
            List<ScanResult> wifiList;
            wifiList = mainWifi.getScanResults();
            for (int i = 0; i < wifiList.size(); i++) {
                System.out.println("SSID" + wifiList.get(i).SSID);
                connections.add(wifiList.get(i).SSID);
            }

        }
    }

    public void getipOverOSVersion() {
        if (Build.VERSION.SDK_INT >= 29) {
            new GetConnectedDevicesIPOS10().execute();
        } else {
            new GetConnectedDevicesIP().execute();
        }
    }

    public class GetConnectedDevicesIPOS10 extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... arg0) {

            ListOfConnectedDevices.clear();
            String resp = "";


            try {
                Runtime runtime = Runtime.getRuntime();
                Process proc = runtime.exec("ip neigh show");
                proc.waitFor();
                BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line;

                while ((line = br.readLine()) != null) {

                    //System.out.println("****-"+line);

                    String[] splitted = line.split(" ");

                    if (splitted != null && splitted.length >= 4) {

                        String ipAddress = splitted[0];
                        String macAddress = splitted[4];

                        if (ipAddress.contains(".") && macAddress.contains(":")) {
                            System.out.println("***IPAddress" + ipAddress);
                            System.out.println("***macAddress" + macAddress);

                            HashMap<String, String> map = new HashMap<>();
                            map.put("ipAddress", ipAddress);
                            map.put("macAddress", macAddress);

                            ListOfConnectedDevices.add(map);
                        } else {
                            System.out.println("###IPAddress" + ipAddress);
                            System.out.println("###macAddress" + macAddress);
                        }
                    }
                }
                System.out.println("Check");
                AppConstants.DetailsListOfConnectedDevices = ListOfConnectedDevices;
                System.out.println("DeviceConnected" + ListOfConnectedDevices);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resp;
        }
    }

    public class GetConnectedDevicesIP extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... arg0) {

            ListOfConnectedDevices.clear();
            String resp = "";

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    BufferedReader br = null;
                    boolean isFirstLine = true;

                    try {
                        br = new BufferedReader(new FileReader("/proc/net/arp"));
                        String line;

                        while ((line = br.readLine()) != null) {
                            if (isFirstLine) {
                                isFirstLine = false;
                                continue;
                            }

                            String[] splitted = line.split(" +");

                            if (splitted != null && splitted.length >= 4) {

                                String ipAddress = splitted[0];
                                String macAddress = splitted[3];
                                System.out.println("IPAddress" + ipAddress);


                                if (ipAddress != null || macAddress != null) {

                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("ipAddress", ipAddress);
                                    map.put("macAddress", macAddress);

                                    ListOfConnectedDevices.add(map);
                                }
                            }
                        }

                        System.out.println("Check");

                    } catch (Exception e) {
                        e.printStackTrace();
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "  GetConnectedDevicesIP 1 --Exception " + e);
                    } finally {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "  GetConnectedDevicesIP 2 --Exception " + e);
                        }
                    }

                    AppConstants.DetailsListOfConnectedDevices = ListOfConnectedDevices;
                    System.out.println("DeviceConnected" + ListOfConnectedDevices);

                }
            });
            thread.start();
            return resp;
        }
    }

    public class CommandsPOST extends AsyncTask<String, Void, String> {

        public String resp = "";


        protected String doInBackground(String... param) {

            System.out.println("url" + HTTP_URL);
            try {


                MediaType JSON = MediaType.parse("application/json");

                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(JSON, param[1]);

                Request request = new Request.Builder()
                        .url(param[0])
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  CommandsPOST  DoInBackground--Exception " + e);
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            try {

                consoleString += "OUTPUT- " + result + "\n";
                // tvConsole.setText(consoleString);

                System.out.println(result);

            } catch (Exception e) {

                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  CommandsPOST  onPostExecute --Exception " + e);
            }

        }
    }

    public class CommandsGET_INFO extends AsyncTask<String, Void, String> {

        public String resp = "";

        protected String doInBackground(String... param) {

            try {

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(15, TimeUnit.SECONDS);
                client.setReadTimeout(15, TimeUnit.SECONDS);
                client.setWriteTimeout(15, TimeUnit.SECONDS);

                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                ChangeWifiState(false);//turn wifi off
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  CommandsPOST  CommandsGET_INFO  DoInBackground --Exception " + e);
                Log.d("Ex", e.getMessage());
                Constants.hotspotstayOn = true;
                if (loading != null) {
                    loading.dismiss();
                }

            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                System.out.println(" resp......." + result);
                System.out.println("2:" + Calendar.getInstance().getTime());
            } catch (Exception e) {
                ChangeWifiState(false);//turn wifi off
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  CommandsPOST  CommandsGET_INFO  onPostExecute --Exception " + e);
                Log.d("Ex", e.getMessage());
                Constants.hotspotstayOn = true;
            }
        }
    }

    private void UpdateSSIDStatusToServer() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //Update SSID rename statu to server
                if (AppConstants.NeedToRenameFS_ON_UPDATE_MAC) {
                    String userEmail = CommonUtils.getCustomerDetails(WelcomeActivity.this).PersonEmail;

                    String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(WelcomeActivity.this) + ":" + userEmail + ":" + "SetHoseNameReplacedFlag");


                    RenameHose rhose = new RenameHose();
                    rhose.SiteId = AppConstants.CURRENT_SELECTED_SITEID;
                    rhose.HoseId = AppConstants.CURRENT_HOSE_SSID;
                    rhose.IsHoseNameReplaced = "Y";

                    Gson gson = new Gson();
                    String jsonData = gson.toJson(rhose);


                    new SetHoseNameReplacedFlagO_Mac().execute(jsonData, authString);

                }
                ;
            }
        }, 5000);


    }

    public class CommandsPOST_ChangeHotspotSettings extends AsyncTask<String, Void, String> {

        public String resp = "";

        protected String doInBackground(String... param) {

            try {

                MediaType JSON = MediaType.parse("application/json");
                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(4, TimeUnit.SECONDS);
                client.setReadTimeout(4, TimeUnit.SECONDS);
                client.setWriteTimeout(4, TimeUnit.SECONDS);

                RequestBody body = RequestBody.create(JSON, param[1]);
                Request request = new Request.Builder()
                        .url(param[0])
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {

                ChangeWifiState(false);//turn wifi off
                ssid_pass_success = "2";
                //resp = "exception";

                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Set SSID and PASS to Link (Link reset) -Exception " + e);
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            try {

                if (result.equalsIgnoreCase("exception")) {
                    ChangeWifiState(false);//turn wifi off
                    AppConstants.colorToastBigFont(getApplicationContext(), "Step2 Failed while changing Hotspot Settings Please try again..", Color.RED);
                    Log.i(TAG, "Step2 Failed while changing Hotspot Settings Please try again.. exception:" + result);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Step2 Failed while changing Hotspot Settings Please try again.. exception:" + result);

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //mj
                    Intent intent = new Intent(WelcomeActivity.this, WelcomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {
                    //mj
                    //Constants.hotspotstayOn = true;//Enable hotspot flag  temp prakash
                    //ChangeWifiState(false);//turn wifi off

                }

                System.out.println(result);
                Log.i(TAG, " Set SSID and PASS to Link (Result" + result);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Set SSID and PASS to Link Result>>" + result);

            } catch (Exception e) {

                ChangeWifiState(false);//turn wifi off
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Set SSID and PASS to Link (Link reset) -Exception " + e);
            }

        }
    }


    private class WiFiConnectTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AppConstants.WriteinFile(TAG + " Started Reconfiguration process..");
            String s = "Started Reconfiguration process. Please wait..";
            SpannableString ss2 = new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);

            loading = new ProgressDialog(WelcomeActivity.this);
            loading.setCancelable(true);
            loading.setMessage(ss2);
            loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            loading.setCancelable(true);
            loading.show();
        }

        protected String doInBackground(String... asd) {

            try {


            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  WiFiConnectTask DoInBackground -Exception " + e);
            }

            return "";
        }


        @Override
        protected void onPostExecute(String s) {


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    WifiManager wifiManager = (WifiManager) WelcomeActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                    int NetID = wifiInfo.getNetworkId();
                    String ssid = wifiInfo.getSSID();
                    if (ssid.contains(AppConstants.CURRENT_SELECTED_SSID)) {


                        setGlobalWifiConnection();

                        AppConstants.colorToastBigFont(WelcomeActivity.this, "Connected to wifi " + AppConstants.CURRENT_SELECTED_SSID, Color.parseColor("#4CAF50"));
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Connected to wifi " + AppConstants.CURRENT_SELECTED_SSID);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                //new CommandsPOST_ChangeHotspotSettings().execute("http://192.168.4.1:80/config?command=wifi", "{\"Request\":{\"Station\":{\"Connect_Station\":{\"ssid\":\"HUB00000013\",\"password\":\"HUB12344334\" ,\"sta_connect\":1 }}}}");

                                HTTP_URL = "http://192.168.4.1:80/";
                                URL_INFO = HTTP_URL + "client?command=info";
                                try {
                                    String result = new CommandsGET_INFO().execute(URL_INFO).get();
                                    String mac_address = "";

                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile("Reconfig-InfoCMD-" + ssid + "-" + result);

                                    if (result.contains("Version")) {
                                        JSONObject jsonObject = new JSONObject(result);
                                        JSONObject joPulsarStat = jsonObject.getJSONObject("Version");
                                        String sdk_version = joPulsarStat.getString("sdk_version");
                                        String iot_version = joPulsarStat.getString("iot_version");
                                        mac_address = joPulsarStat.getString("mac_address");//station_mac_address
                                        AppConstants.UPDATE_MACADDRESS = mac_address;

                                        if (mac_address.equals("")) {
                                            loading.dismiss();
                                            Constants.hotspotstayOn = true;//Enable hotspot flag
                                            AppConstants.colorToastBigFont(WelcomeActivity.this, "Reconfiguration process fail..\nCould not get mac address", Color.RED);
                                            if (AppConstants.GenerateLogs)
                                                AppConstants.WriteinFile(TAG + "Reconfiguration process fail.. Could not get mac address");

                                            //Disable wifi connection
                                            WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

                                            wifiManagerMM.setWifiEnabled(false);


                                        } else {

                                            //Set usernam and password to link
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {

                                                    /*wifiApManager.setWifiApEnabled(null, false);
                                                    WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                                                    wifiManagerMM.setWifiEnabled(true);
                                                    setGlobalWifiConnection();*/

                                                    AppConstants.colorToastBigFont(WelcomeActivity.this, "Setting SSID and PASS to Link", Color.BLUE);
                                                    if (AppConstants.GenerateLogs)
                                                        AppConstants.WriteinFile(TAG + "Setting SSID and PASS to Link");
                                                    HTTP_URL = "http://192.168.4.1:80/";
                                                    URL_UPDATE_FS_INFO = HTTP_URL + "config?command=wifi";

                                                    String jsonChangeUsernamePass = "{\"Request\":{\"Station\":{\"Connect_Station\":{\"ssid\":\"" + AppConstants.HubName + "\",\"password\":\"" + AppConstants.HubGeneratedpassword + "\" ,\"sta_connect\":1 }}}}";

                                                    System.out.println("URL_UPDATE_FS_INFO-" + URL_UPDATE_FS_INFO);
                                                    System.out.println("jsonChangeUsernamePass-" + jsonChangeUsernamePass);

                                                    try {
                                                        ssid_pass_success = "";
                                                        new CommandsPOST_ChangeHotspotSettings().execute(URL_UPDATE_FS_INFO, jsonChangeUsernamePass);

                                                    } catch (Exception e) {
                                                        AppConstants.WriteinFile("CommandsPOST_ChangeHotspotSettings-" + e.getMessage());
                                                    }

                                                    btnRetryWifi.setVisibility(View.GONE);

                                                }
                                            }, 1000);


                                            //============================================================


                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {


                                                    AppConstants.colorToastBigFont(WelcomeActivity.this, "Mac address " + AppConstants.UPDATE_MACADDRESS, Color.BLUE);
                                                    if (AppConstants.GenerateLogs)
                                                        AppConstants.WriteinFile(TAG + " Mac address " + AppConstants.UPDATE_MACADDRESS);

                                                   /* WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                                                    if (wifiManagerMM.isWifiEnabled()) {
                                                        wifiManagerMM.setWifiEnabled(false);
                                                    }*/

                                                    // wifiApManager.setWifiApEnabled(null, true);//enable hotspot


                                                    //Update mac address to server and mac address status
                                                    try {

                                                        UpdateMacAddressClass authEntityClass1 = new UpdateMacAddressClass();
                                                        authEntityClass1.SiteId = Integer.parseInt(AppConstants.CURRENT_SELECTED_SITEID);
                                                        authEntityClass1.MACAddress = AppConstants.UPDATE_MACADDRESS;
                                                        authEntityClass1.RequestFrom = "AP";
                                                        authEntityClass1.HubName = AppConstants.HubName;

                                                        //------
                                                        Gson gson = new Gson();
                                                        final String jsonData = gson.toJson(authEntityClass1);

                                                        saveLinkMacAddressForReconfigure(jsonData);

                                                        //setGlobalMobileDatConnection();
                                                        cd = new ConnectionDetector(WelcomeActivity.this);
                                                        if (cd.isConnectingToInternet()) {

                                                            new UpdateMacAsynTask().execute(jsonData);

                                                            if (loading != null)
                                                                loading.dismiss();

                                                            ssid_pass_success = "";

                                                        } else {
                                                            AppConstants.colorToast(WelcomeActivity.this, "Please check Internet Connection and retry.", Color.RED);
                                                            if (loading != null)
                                                                loading.dismiss();

                                                        }


                                                    } catch (Exception e) {
                                                        if (loading != null)
                                                            loading.dismiss();
                                                        Constants.hotspotstayOn = true;
                                                        System.out.println(e);
                                                        if (AppConstants.GenerateLogs)
                                                            AppConstants.WriteinFile(TAG + "  WiFiConnectTask  UpdateMacAddressClass --Exception " + e);
                                                    }

                                                }
                                            }, 6000);
                                        }
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    if (loading != null)
                                        loading.dismiss();
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "  WiFiConnectTask  OnPostExecution --Exception " + e);
                                }
                            }
                        }, 1000);


                    }
                }
            }, 5000);


            ///Wait temp code...
            /**/
            ///Wit temp

        }
    }


    private int getExistingNetworkId(String SSID) {

        SSID = "\"" + SSID + "\"";

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration existingConfig : configuredNetworks) {
                if (existingConfig.SSID != null && existingConfig.SSID.equals(SSID)) {
                    return existingConfig.networkId;
                }
            }
        }
        return -1;
    }

    public void AlertSettings(final Context ctx, String message) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ssid = wifiInfo.getSSID();
                if (ssid.equalsIgnoreCase("\"" + AppConstants.CURRENT_SELECTED_SSID + "\"")) {

                } else {
                    AppConstants.disconnectWiFi(WelcomeActivity.this);
                }


            }
        }, 30000);


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(true);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                }

        );

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public class UpdateMacAsynTask extends AsyncTask<String, Void, String> {


        public String response = null;

        @Override
        protected String doInBackground(String... param) {

            try {
                ServerHandler serverHandler = new ServerHandler();
                String jsonData = param[0];
                String userEmail = CommonUtils.getCustomerDetails(WelcomeActivity.this).PersonEmail;

                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(WelcomeActivity.this) + ":" + userEmail + ":" + "UpdateMACAddress");
                response = serverHandler.PostTextData(WelcomeActivity.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {
                if (loading != null)
                    loading.dismiss();
                Constants.hotspotstayOn = true;
                CommonUtils.LogMessage("", "UpdateMACAddress ", ex);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  UpdateMacAsynTask doInBackground--Exception " + ex);
                response = "err";

            }
            return response;
        }

        @Override
        protected void onPostExecute(String serverRes) {


            try {
                if (serverRes.equalsIgnoreCase("err")) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Link Re-configuration is partially completed.");
                    AppConstants.alertBigFinishActivity(WelcomeActivity.this, "Link Re-configuration is partially completed. \nPlease remove app from Recent Apps and start app again");
                } else if (serverRes != null) {


                    JSONObject jsonObject1 = new JSONObject(serverRes);

                    String ResponceMessage = jsonObject1.getString("ResponceMessage");


                    if (ResponceMessage.equalsIgnoreCase("success")) {


                        AppConstants.clearSharedPrefByName(WelcomeActivity.this, Constants.MAC_ADDR_RECONFIGURE);

                        if (loading != null)
                            loading.dismiss();
                        Constants.hotspotstayOn = true;
                        AppConstants.colorToastBigFont(WelcomeActivity.this, " Mac Address Updated ", Color.parseColor("#4CAF50"));
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Mac Address Updated");
                        wifiApManager.setWifiApEnabled(null, true);
                        ChangeWifiState(false);
                        alertHotspotOnOffAfterReconfigure();


                    } else if (ResponceMessage.equalsIgnoreCase("fail")) {
                        if (loading != null)
                            loading.dismiss();
                        Constants.hotspotstayOn = true;
                        AppConstants.colorToastBigFont(WelcomeActivity.this, " Could not Updated mac address ", Color.RED);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Could not Updated mac address");
                        wifiApManager.setWifiApEnabled(null, true);
                        ChangeWifiState(false);
                    }

                } else {
                    Log.i(TAG, "UpdateMacAsynTask Server Response Empty!");
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "HandleGetAndroidSSID UpdateMacAsynTask Server Response Empty!");
                    //CommonUtils.showNoInternetDialog(WelcomeActivity.this);
                }
            } catch (Exception e) {

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  UpdateMacAsynTask onPostExecute--Exception " + e);

            }
        }
    }

    private boolean ChangeHotspotBroadcastChannel(Context context, int i) {

        try {

            WifiManager wifiManager = (WifiManager) context.getSystemService(ctx.WIFI_SERVICE);
            Method getConfigMethod = getConfigMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
            WifiConfiguration wifiConfig = (WifiConfiguration) getConfigMethod.invoke(wifiManager);


            Method getWifiApConfigurationMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
            wifiConfig = (WifiConfiguration) getWifiApConfigurationMethod.invoke(wifiManager);

//          Log.i("Writing HotspotData", "\nSSID:" + netConfig.SSID + "\nPassword:" + netConfig.preSharedKey + "\n");

//          Field wcBand = WifiConfiguration.class.getField("apBand");
//          int vb = wcBand.getInt(wifiConfig);
//          Log.i("Band was", "val=" + vb);
//          wcBand.setInt(wifiConfig, 2); // 2Ghz

            // For Channel change
            Field wcFreq = WifiConfiguration.class.getField("apChannel");
            int val = wcFreq.getInt(wifiConfig);
            Log.i("Config was", "val=" + val);
            wcFreq.setInt(wifiConfig, i); // channel 11

            Method setWifiApConfigurationMethod = wifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            setWifiApConfigurationMethod.invoke(wifiManager, wifiConfig);

            // For Saving Data
            wifiManager.saveConfiguration();

        } catch (Exception e) {
            e.printStackTrace();
        }


        return false;
    }

    public boolean setHotspotNamePassword(Context context) {
        try {

            AppConstants.HubGeneratedpassword = PasswordGeneration();

            SharedPreferences sharedPrefODO = WelcomeActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
            IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
            WifiChannelToUse = sharedPrefODO.getInt(AppConstants.WifiChannelToUse, 11);

            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(context.WIFI_SERVICE);
            Method getConfigMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
            WifiConfiguration wifiConfig = (WifiConfiguration) getConfigMethod.invoke(wifiManager);


            String CurrentHotspotName = wifiConfig.SSID;
            String CurrentHotspotPassword = wifiConfig.preSharedKey;

            if (CurrentHotspotName.equals(AppConstants.HubName) && CurrentHotspotPassword.equals(AppConstants.HubGeneratedpassword)) {
                //No need to change hotspot username password
                //ChangeHotspotBroadcastChannel(WelcomeActivity.this,index);

                // For Channel change
                Field wcFreq = WifiConfiguration.class.getField("apChannel");
                int val = wcFreq.getInt(wifiConfig);
                Log.i("Config was", "val=" + val);
                if (WifiChannelToUse != val) {
                    wcFreq.setInt(wifiConfig, WifiChannelToUse); // channel 11
                    //Toggle Wifi..
                    wifiApManager.setWifiApEnabled(null, false);  //Disable Hotspot
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            //Enable Hotsopt
                            wifiApManager.setWifiApEnabled(null, true);

                        }
                    }, 500);

                }


            } else {

                //String uname = AppConstants.HubName.replaceAll("\"","");

                wifiConfig.SSID = AppConstants.HubName;
                wifiConfig.preSharedKey = AppConstants.HubGeneratedpassword;

                // For Channel change
                Field wcFreq = WifiConfiguration.class.getField("apChannel");
                int val = wcFreq.getInt(wifiConfig);
                Log.i("Config was", "val=" + val);
                if (WifiChannelToUse != val) {
                    wcFreq.setInt(wifiConfig, WifiChannelToUse); // channel 11
                }

                //Toggle Wifi..
                wifiApManager.setWifiApEnabled(null, false);  //Disable Hotspot
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        //Enable Hotsopt
                        wifiApManager.setWifiApEnabled(null, true);

                    }
                }, 500);

            }

            Method setConfigMethod = wifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            setConfigMethod.invoke(wifiManager, wifiConfig);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String PasswordGeneration() {

        String FinalPass;
        String hubName = AppConstants.HubName;//"HUB00000001";
        String numb = hubName.substring(hubName.length() - 8);
        String numb1 = numb.substring(0, 4);
        String numb2 = hubName.substring(hubName.length() - 4);

        String result1 = "";
        String result2 = "";

        //Result one
        for (int i = 0; i < numb1.length(); i++) {

            String xp = String.valueOf(numb1.charAt(i));
            int p = Integer.parseInt(xp);

            if (p >= 5) {
                p = p - 2;
                result1 = result1 + p;

            } else {
                p = p + i + 1;
                result1 = result1 + p;
            }

        }

        //Result Two
        String rev_numb2 = new StringBuilder(numb2).reverse().toString();
        String res = "";
        for (int j = 0; j < rev_numb2.length(); j++) {

            String xps = String.valueOf(rev_numb2.charAt(j));
            int q = Integer.parseInt(xps);

            if (q >= 5) {
                q = q - 2;
                res = res + q;

            } else {
                q = q + j + 1;
                res = res + q;
            }
            result2 = new StringBuilder(res).reverse().toString();

        }
        FinalPass = "HUB" + result1 + result2;
        System.out.println("FinalPass" + FinalPass);

        return FinalPass;
    }

    //=========================Stop button functionality for each hose==============

    //=======FS UNIT 1 =========
    public void stopButtonFunctionality_FS1(String LinkName) {

        //it stops pulsar logic------
        stopTimer = false;
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "Sending RELAY OFF command to Link: " + LinkName);
        new CommandsPOST_FS1().execute(URL_RELAY_FS1, jsonRelayOff);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AppConstants.IsTransactionCompleted = true;
                /*try {
                    String cntA = "0", cntB = "0", cntC = "0";

                    for (int i = 0; i < 2; i++) {

                        String result = new GETFINALPulsar_FS1().execute(URL_GET_PULSAR_FS1).get();

                        if (result.contains("pulsar_status")) {

                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject joPulsarStat = jsonObject.getJSONObject("pulsar_status");
                            String counts = joPulsarStat.getString("counts");
                            //String pulsar_status = joPulsarStat.getString("pulsar_status");
                            //String pulsar_secure_status = joPulsarStat.getString("pulsar_secure_status");

                            convertCountToQuantity_fs1(counts);

                            if (i == 1) {

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finalLastStep_fs1();
                                    }
                                }, 500);
                            }
                            Thread.sleep(1000);
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " stopButtonFunctionality_FS1 Exception " + e.getMessage());
                }*/
            }
        }, 1000);
    }

    public class CommandsPOST_FS1 extends AsyncTask<String, Void, String> {

        public String resp = "";
        public String jsonParam = "";

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {

            String s = "Please wait...";
            SpannableString ss2 = new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage(ss2);
            pd.setCancelable(false);
        }

        protected String doInBackground(String... param) {

            System.out.println("url" + HTTP_URL_FS_1);
            try {
                jsonParam = param[1];

                MediaType JSON = MediaType.parse("application/json");

                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(JSON, jsonParam);

                Request request = new Request.Builder()
                        .url(param[0])
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            try {

                RemoveTransactionFromInterruptedTxtnPref(jsonParam, result);

                consoleString += "OUTPUT- " + result + "\n";
                // tvConsole.setText(consoleString);

                System.out.println(result);

            } catch (Exception e) {

                System.out.println(e);
            }

        }
    }

    public class GETFINALPulsar_FS1 extends AsyncTask<String, Void, String> {

        public String resp = "";


        protected String doInBackground(String... param) {


            try {

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
                FS1_Stpflag = true;
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {


            try {

                consoleString += "OUTPUT- " + result + "\n";

                // tvConsole.setText(consoleString);

                System.out.println(result);


            } catch (Exception e) {
                FS1_Stpflag = true;
                System.out.println(e);
            }

        }
    }

    public void convertCountToQuantity_fs1(String counts) {

        outputQuantity = counts;
        fillqty = Double.parseDouble(outputQuantity);
        fillqty = fillqty / numPulseRatio;//convert to gallons

        fillqty = AppConstants.roundNumber(fillqty, 2);

    }

    public void finalLastStep_fs1() {
        AppConstants.IsTransactionCompleted = true;
    }

    //=======FS UNIT 2 =========
    public void stopButtonFunctionality_FS2(String LinkName) {

        //it stops pulsar logic------
        stopTimer = false;
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "Sending RELAY OFF command to Link: " + LinkName);
        new CommandsPOST_FS2().execute(URL_RELAY_FS2, jsonRelayOff);

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    String cntA = "0", cntB = "0", cntC = "0";

                    for (int i = 0; i < 2; i++) {

                        String result = new GETFINALPulsar_FS2().execute(URL_GET_PULSAR_FS2).get();

                        if (result.contains("pulsar_status")) {

                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject joPulsarStat = jsonObject.getJSONObject("pulsar_status");
                            String counts = joPulsarStat.getString("counts");
                            //String pulsar_status = joPulsarStat.getString("pulsar_status");
                            //String pulsar_secure_status = joPulsarStat.getString("pulsar_secure_status");

                            convertCountToQuantity_fs2(counts);

                            if (i == 2) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finalLastStep_fs2();
                                    }
                                }, 500);
                            }
                            Thread.sleep(1000);
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " stopButtonFunctionality_FS2 Exception " + e.getMessage());
                }
            }
        }, 1000);*/
    }

    public class CommandsPOST_FS2 extends AsyncTask<String, Void, String> {

        public String resp = "";
        public String jsonParam = "";

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {

            String s = "Please wait...";
            SpannableString ss2 = new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage(ss2);
            pd.setCancelable(false);
        }

        protected String doInBackground(String... param) {

            System.out.println("url" + HTTP_URL_FS_2);
            try {
                jsonParam = param[1];

                MediaType JSON = MediaType.parse("application/json");

                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(JSON, jsonParam);

                Request request = new Request.Builder()
                        .url(param[0])
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            try {

                RemoveTransactionFromInterruptedTxtnPref(jsonParam, result);

                consoleString += "OUTPUT- " + result + "\n";
                // tvConsole.setText(consoleString);

                System.out.println(result);

            } catch (Exception e) {

                System.out.println(e);
            }

        }
    }

    public class GETFINALPulsar_FS2 extends AsyncTask<String, Void, String> {

        public String resp = "";


        protected String doInBackground(String... param) {


            try {

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                FS2_Stpflag = true;
                Log.d("Ex", e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {


            try {

                consoleString += "OUTPUT- " + result + "\n";

                // tvConsole.setText(consoleString);

                System.out.println(result);


            } catch (Exception e) {
                FS2_Stpflag = true;
                System.out.println(e);
            }

        }
    }

    public void convertCountToQuantity_fs2(String counts) {

        outputQuantity = counts;
        fillqty = Double.parseDouble(outputQuantity);
        fillqty = fillqty / numPulseRatio;//convert to gallons

        fillqty = AppConstants.roundNumber(fillqty, 2);

    }

    public void finalLastStep_fs2() {


    }

    //=======FS UNIT 3 =========
    public void stopButtonFunctionality_FS3(String LinkName) {

        //it stops pulsar logic------
        stopTimer = false;
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "Sending RELAY OFF command to Link: " + LinkName);
        new CommandsPOST_FS3().execute(URL_RELAY_FS3, jsonRelayOff);

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    String cntA = "0", cntB = "0", cntC = "0";

                    for (int i = 0; i < 2; i++) {

                        String result = new GETFINALPulsar_FS3().execute(URL_GET_PULSAR_FS3).get();

                        if (result.contains("pulsar_status")) {

                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject joPulsarStat = jsonObject.getJSONObject("pulsar_status");
                            String counts = joPulsarStat.getString("counts");
                            //String pulsar_status = joPulsarStat.getString("pulsar_status");
                            //String pulsar_secure_status = joPulsarStat.getString("pulsar_secure_status");

                            convertCountToQuantity_fs3(counts);

                            if (i == 1) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finalLastStep_fs3();
                                    }
                                }, 500);
                            }
                            Thread.sleep(1000);
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " stopButtonFunctionality_FS3 Exception " + e.getMessage());
                }
            }
        }, 1000);*/
    }

    public class CommandsPOST_FS3 extends AsyncTask<String, Void, String> {

        public String resp = "";
        public String jsonParam = "";

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {

            String s = "Please wait...";
            SpannableString ss2 = new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage(ss2);
            pd.setCancelable(false);
        }

        protected String doInBackground(String... param) {

            System.out.println("url" + HTTP_URL_FS_3);
            try {
                jsonParam = param[1];

                MediaType JSON = MediaType.parse("application/json");

                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(JSON, jsonParam);

                Request request = new Request.Builder()
                        .url(param[0])
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            try {

                RemoveTransactionFromInterruptedTxtnPref(jsonParam, result);

                consoleString += "OUTPUT- " + result + "\n";
                // tvConsole.setText(consoleString);

                System.out.println(result);

            } catch (Exception e) {

                System.out.println(e);
            }

        }
    }

    public class GETFINALPulsar_FS3 extends AsyncTask<String, Void, String> {

        public String resp = "";


        protected String doInBackground(String... param) {


            try {

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
                FS3_Stpflag = true;
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {


            try {

                consoleString += "OUTPUT- " + result + "\n";

                // tvConsole.setText(consoleString);

                System.out.println(result);


            } catch (Exception e) {
                FS3_Stpflag = true;
                System.out.println(e);
            }

        }
    }

    public void convertCountToQuantity_fs3(String counts) {

        outputQuantity = counts;
        fillqty = Double.parseDouble(outputQuantity);
        fillqty = fillqty / numPulseRatio;//convert to gallons

        fillqty = AppConstants.roundNumber(fillqty, 2);

    }

    public void finalLastStep_fs3() {


    }

    //=======FS UNIT 4 =========
    public void stopButtonFunctionality_FS4(String LinkName) {

        //it stops pulsar logic------
        stopTimer = false;
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "Sending RELAY OFF command to Link: " + LinkName);
        new CommandsPOST_FS4().execute(URL_RELAY_FS4, jsonRelayOff);

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    String cntA = "0", cntB = "0", cntC = "0";

                    for (int i = 0; i < 2; i++) {

                        String result = new GETFINALPulsar_FS4().execute(URL_GET_PULSAR_FS4).get();

                        if (result.contains("pulsar_status")) {

                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject joPulsarStat = jsonObject.getJSONObject("pulsar_status");
                            String counts = joPulsarStat.getString("counts");
                            //String pulsar_status = joPulsarStat.getString("pulsar_status");
                            //String pulsar_secure_status = joPulsarStat.getString("pulsar_secure_status");

                            convertCountToQuantity_fs4(counts);

                            if (i == 1) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finalLastStep_fs4();
                                    }
                                }, 500);
                            }
                            Thread.sleep(1000);
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " stopButtonFunctionality_FS4 Exception " + e.getMessage());
                }
            }
        }, 1000);*/
    }

    public class CommandsPOST_FS4 extends AsyncTask<String, Void, String> {

        public String resp = "";
        public String jsonParam = "";

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {

            String s = "Please wait...";
            SpannableString ss2 = new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage(ss2);
            pd.setCancelable(false);
        }

        protected String doInBackground(String... param) {

            System.out.println("url-" + HTTP_URL_FS_4);
            try {
                jsonParam = param[1];

                MediaType JSON = MediaType.parse("application/json");

                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(JSON, jsonParam);

                Request request = new Request.Builder()
                        .url(param[0])
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            try {

                RemoveTransactionFromInterruptedTxtnPref(jsonParam, result);

                consoleString += "OUTPUT- " + result + "\n";
                // tvConsole.setText(consoleString);

                System.out.println(result);

            } catch (Exception e) {

                System.out.println(e);
            }

        }
    }

    public class GETFINALPulsar_FS4 extends AsyncTask<String, Void, String> {

        public String resp = "";


        protected String doInBackground(String... param) {


            try {

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
                FS4_Stpflag = true;
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {


            try {

                consoleString += "OUTPUT- " + result + "\n";

                // tvConsole.setText(consoleString);

                System.out.println(result);


            } catch (Exception e) {
                FS4_Stpflag = true;
                System.out.println(e);
            }

        }
    }

    public void convertCountToQuantity_fs4(String counts) {

        outputQuantity = counts;
        fillqty = Double.parseDouble(outputQuantity);
        fillqty = fillqty / numPulseRatio;//convert to gallons

        fillqty = AppConstants.roundNumber(fillqty, 2);

    }

    public void finalLastStep_fs4() {


    }

    //=======FS UNIT 5 =========
    public void stopButtonFunctionality_FS5(String LinkName) {

        //it stops pulsar logic------
        stopTimer = false;
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "Sending RELAY OFF command to Link: " + LinkName);
        new CommandsPOST_FS5().execute(URL_RELAY_FS5, jsonRelayOff);

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    String cntA = "0", cntB = "0", cntC = "0";

                    for (int i = 0; i < 2; i++) {

                        String result = new GETFINALPulsar_FS5().execute(URL_GET_PULSAR_FS5).get();

                        if (result.contains("pulsar_status")) {

                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject joPulsarStat = jsonObject.getJSONObject("pulsar_status");
                            String counts = joPulsarStat.getString("counts");
                            //String pulsar_status = joPulsarStat.getString("pulsar_status");
                            //String pulsar_secure_status = joPulsarStat.getString("pulsar_secure_status");

                            convertCountToQuantity_fs5(counts);

                            if (i == 1) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finalLastStep_fs5();
                                    }
                                }, 500);
                            }
                            Thread.sleep(1000);
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " stopButtonFunctionality_FS5 Exception " + e.getMessage());
                }
            }
        }, 1000);*/
    }

    public class CommandsPOST_FS5 extends AsyncTask<String, Void, String> {

        public String resp = "";
        public String jsonParam = "";

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {

            String s = "Please wait...";
            SpannableString ss2 = new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage(ss2);
            pd.setCancelable(false);
        }

        protected String doInBackground(String... param) {

            System.out.println("url-" + HTTP_URL_FS_5);
            try {
                jsonParam = param[1];

                MediaType JSON = MediaType.parse("application/json");

                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(JSON, jsonParam);

                Request request = new Request.Builder()
                        .url(param[0])
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            try {

                RemoveTransactionFromInterruptedTxtnPref(jsonParam, result);

                consoleString += "OUTPUT- " + result + "\n";
                // tvConsole.setText(consoleString);

                System.out.println(result);

            } catch (Exception e) {

                System.out.println(e);
            }

        }
    }

    public class GETFINALPulsar_FS5 extends AsyncTask<String, Void, String> {

        public String resp = "";


        protected String doInBackground(String... param) {


            try {

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
                FS4_Stpflag = true;
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {


            try {

                consoleString += "OUTPUT- " + result + "\n";

                // tvConsole.setText(consoleString);

                System.out.println(result);


            } catch (Exception e) {
                FS5_Stpflag = true;
                System.out.println(e);
            }

        }
    }

    public void convertCountToQuantity_fs5(String counts) {

        outputQuantity = counts;
        fillqty = Double.parseDouble(outputQuantity);
        fillqty = fillqty / numPulseRatio;//convert to gallons

        fillqty = AppConstants.roundNumber(fillqty, 2);

    }

    public void finalLastStep_fs5() {


    }


    //=======FS UNIT 6 =========
    public void stopButtonFunctionality_FS6(String LinkName) {

        //it stops pulsar logic------
        stopTimer = false;
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "Sending RELAY OFF command to Link: " + LinkName);
        new CommandsPOST_FS6().execute(URL_RELAY_FS6, jsonRelayOff);

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    String cntA = "0", cntB = "0", cntC = "0";

                    for (int i = 0; i < 2; i++) {

                        String result = new GETFINALPulsar_FS6().execute(URL_GET_PULSAR_FS6).get();

                        if (result.contains("pulsar_status")) {

                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject joPulsarStat = jsonObject.getJSONObject("pulsar_status");
                            String counts = joPulsarStat.getString("counts");
                            //String pulsar_status = joPulsarStat.getString("pulsar_status");
                            //String pulsar_secure_status = joPulsarStat.getString("pulsar_secure_status");

                            convertCountToQuantity_fs6(counts);

                            if (i == 1) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finalLastStep_fs6();
                                    }
                                }, 500);
                            }
                            Thread.sleep(1000);
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " stopButtonFunctionality_FS6 Exception " + e.getMessage());
                }
            }
        }, 1000);*/
    }

    public class CommandsPOST_FS6 extends AsyncTask<String, Void, String> {

        public String resp = "";
        public String jsonParam = "";

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {

            String s = "Please wait...";
            SpannableString ss2 = new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage(ss2);
            pd.setCancelable(false);
        }

        protected String doInBackground(String... param) {

            System.out.println("url-" + HTTP_URL_FS_6);
            try {
                jsonParam = param[1];

                MediaType JSON = MediaType.parse("application/json");

                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(JSON, jsonParam);

                Request request = new Request.Builder()
                        .url(param[0])
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            try {

                RemoveTransactionFromInterruptedTxtnPref(jsonParam, result);

                consoleString += "OUTPUT- " + result + "\n";
                // tvConsole.setText(consoleString);

                System.out.println(result);

            } catch (Exception e) {

                System.out.println(e);
            }

        }
    }

    public class GETFINALPulsar_FS6 extends AsyncTask<String, Void, String> {

        public String resp = "";


        protected String doInBackground(String... param) {


            try {

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
                FS4_Stpflag = true;
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {


            try {

                consoleString += "OUTPUT- " + result + "\n";

                // tvConsole.setText(consoleString);

                System.out.println(result);


            } catch (Exception e) {
                FS5_Stpflag = true;
                System.out.println(e);
            }

        }
    }

    public void convertCountToQuantity_fs6(String counts) {

        outputQuantity = counts;
        fillqty = Double.parseDouble(outputQuantity);
        fillqty = fillqty / numPulseRatio;//convert to gallons

        fillqty = AppConstants.roundNumber(fillqty, 2);

    }

    public void finalLastStep_fs6() {


    }

    public void BTLinkUpgradeProcessLoader() {

        pdUpgradeProcess = new ProgressDialog(WelcomeActivity.this);
        pdUpgradeProcess.setMessage(GetSpinnerMessage("Software update in progress.\nPlease wait several seconds...."));
        pdUpgradeProcess.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdUpgradeProcess.setCancelable(false);
        pdUpgradeProcess.show();

    }

    public void ManageBTLinkUpgrade() {
        try {

            if (BTConstants.CurrentTransactionIsBT) {
                switch (BTConstants.CurrentSelectedLinkBT) {
                    case 1://Link 1
                        if (AppConstants.UP_Upgrade_fs1) {
                            if (pdOnResume != null) {
                                pdOnResume.dismiss();
                            }
                            if (pdUpgradeProcess != null & showUpgradeSpinnerMessage) {
                                //pdUpgradeProcess.setProgress(Integer.parseInt(BTConstants.upgradeProgress));
                                pdUpgradeProcess.setMessage(GetSpinnerMessage("Software update in progress.\nPlease wait several seconds... " + BTConstants.upgradeProgress));
                            }
                        }
                        if (BTConstants.UpgradeStatusBT1.equalsIgnoreCase("Completed")) {
                            BTConstants.isUpgradeInProgress_BT1 = false;
                            showUpgradeSpinnerMessage = false;
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-"+ TAG + "BTLink 1: Upgrade Completed. Connecting to LINK: " + AppConstants.CURRENT_SELECTED_SSID + " (" + BTConstants.deviceAddress1 + ")");
                            BTConstants.UpgradeStatusBT1 = "";

                            pdUpgradeProcess.setMessage(GetSpinnerMessage("Connecting to LINK.\nPlease wait several seconds... "));

                            startBTSppMain(1);

                            if (pdUpgradeProcess != null) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        pdUpgradeProcess.dismiss();
                                    }
                                }, 10000);
                            }
                        }
                        if (BTConstants.UpgradeStatusBT1.equalsIgnoreCase("Incomplete")) {
                            BTConstants.isUpgradeInProgress_BT1 = false;
                            showUpgradeSpinnerMessage = false;
                            pdUpgradeProcess.setMessage(GetSpinnerMessage("LINK connection lost.\nPlease try again later!"));

                            startBTSppMain(1);

                            BTConstants.UpgradeStatusBT1 = "";
                            if (pdUpgradeProcess != null) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        pdUpgradeProcess.dismiss();
                                    }
                                }, 3000);
                            }
                        }
                        break;
                    case 2://Link 2

                        if (AppConstants.UP_Upgrade_fs2) {
                            if (pdOnResume != null) {
                                pdOnResume.dismiss();
                            }
                            if (pdUpgradeProcess != null & showUpgradeSpinnerMessage) {
                                //pdUpgradeProcess.setProgress(Integer.parseInt(BTConstants.upgradeProgress));
                                pdUpgradeProcess.setMessage(GetSpinnerMessage("Software update in progress.\nPlease wait several seconds... " + BTConstants.upgradeProgress));
                            }
                        }
                        if (BTConstants.UpgradeStatusBT2.equalsIgnoreCase("Completed")) {
                            BTConstants.isUpgradeInProgress_BT2 = false;
                            showUpgradeSpinnerMessage = false;
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-"+ TAG + "BTLink 2: Upgrade Completed. Connecting to LINK: " + AppConstants.CURRENT_SELECTED_SSID + " (" + BTConstants.deviceAddress2 + ")");
                            BTConstants.UpgradeStatusBT2 = "";

                            pdUpgradeProcess.setMessage(GetSpinnerMessage("Connecting to LINK.\nPlease wait several seconds... "));

                            startBTSppMain(2);

                            if (pdUpgradeProcess != null) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        pdUpgradeProcess.dismiss();
                                    }
                                }, 10000);
                            }
                        }
                        if (BTConstants.UpgradeStatusBT2.equalsIgnoreCase("Incomplete")) {
                            BTConstants.isUpgradeInProgress_BT2 = false;
                            showUpgradeSpinnerMessage = false;
                            pdUpgradeProcess.setMessage(GetSpinnerMessage("LINK connection lost.\nPlease try again later!"));
                            BTConstants.UpgradeStatusBT2 = "";

                            startBTSppMain(2);

                            if (pdUpgradeProcess != null) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        pdUpgradeProcess.dismiss();
                                    }
                                }, 3000);
                            }
                        }
                        break;
                    case 3://Link 3

                        if (AppConstants.UP_Upgrade_fs3) {
                            if (pdOnResume != null) {
                                pdOnResume.dismiss();
                            }
                            if (pdUpgradeProcess != null & showUpgradeSpinnerMessage) {
                                //pdUpgradeProcess.setProgress(Integer.parseInt(BTConstants.upgradeProgress));
                                pdUpgradeProcess.setMessage(GetSpinnerMessage("Software update in progress.\nPlease wait several seconds... " + BTConstants.upgradeProgress));
                            }
                        }
                        if (BTConstants.UpgradeStatusBT3.equalsIgnoreCase("Completed")) {
                            BTConstants.isUpgradeInProgress_BT3 = false;
                            showUpgradeSpinnerMessage = false;
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-"+ TAG + "BTLink 3: Upgrade Completed. Connecting to LINK: " + AppConstants.CURRENT_SELECTED_SSID + " (" + BTConstants.deviceAddress3 + ")");
                            BTConstants.UpgradeStatusBT3 = "";

                            pdUpgradeProcess.setMessage(GetSpinnerMessage("Connecting to LINK.\nPlease wait several seconds... "));

                            startBTSppMain(3);

                            if (pdUpgradeProcess != null) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        pdUpgradeProcess.dismiss();
                                    }
                                }, 10000);
                            }
                        }
                        if (BTConstants.UpgradeStatusBT3.equalsIgnoreCase("Incomplete")) {
                            BTConstants.isUpgradeInProgress_BT3 = false;
                            showUpgradeSpinnerMessage = false;
                            pdUpgradeProcess.setMessage(GetSpinnerMessage("LINK connection lost.\nPlease try again later!"));
                            BTConstants.UpgradeStatusBT3 = "";

                            startBTSppMain(3);

                            if (pdUpgradeProcess != null) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        pdUpgradeProcess.dismiss();
                                    }
                                }, 3000);
                            }
                        }
                        break;
                    case 4://Link 4

                        if (AppConstants.UP_Upgrade_fs4) {
                            if (pdOnResume != null) {
                                pdOnResume.dismiss();
                            }
                            if (pdUpgradeProcess != null & showUpgradeSpinnerMessage) {
                                //pdUpgradeProcess.setProgress(Integer.parseInt(BTConstants.upgradeProgress));
                                pdUpgradeProcess.setMessage(GetSpinnerMessage("Software update in progress.\nPlease wait several seconds... " + BTConstants.upgradeProgress));
                            }
                        }
                        if (BTConstants.UpgradeStatusBT4.equalsIgnoreCase("Completed")) {
                            BTConstants.isUpgradeInProgress_BT4 = false;
                            showUpgradeSpinnerMessage = false;
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-"+ TAG + "BTLink 4 Upgrade Completed. Connecting to LINK: " + AppConstants.CURRENT_SELECTED_SSID + " (" + BTConstants.deviceAddress4 + ")");
                            BTConstants.UpgradeStatusBT4 = "";

                            pdUpgradeProcess.setMessage(GetSpinnerMessage("Connecting to LINK.\nPlease wait several seconds... "));

                            startBTSppMain(4);

                            if (pdUpgradeProcess != null) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        pdUpgradeProcess.dismiss();
                                    }
                                }, 10000);
                            }
                        }
                        if (BTConstants.UpgradeStatusBT4.equalsIgnoreCase("Incomplete")) {
                            BTConstants.isUpgradeInProgress_BT4 = false;
                            showUpgradeSpinnerMessage = false;
                            pdUpgradeProcess.setMessage(GetSpinnerMessage("LINK connection lost.\nPlease try again later!"));
                            BTConstants.UpgradeStatusBT4 = "";

                            startBTSppMain(4);

                            if (pdUpgradeProcess != null) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        pdUpgradeProcess.dismiss();
                                    }
                                }, 3000);
                            }
                        }
                        break;
                    default://Something went wrong in link selection please try again.
                        break;
                }
            }
        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-"+ TAG + "Exception in ManageBTLinkUpgrade: " + e.getMessage());
            if (pdUpgradeProcess != null) {
                pdUpgradeProcess.dismiss();
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    public void DisplayDashboardEveSecond() {

        //Display MAX fuel limit message on screen
        if (AppConstants.DisplayToastmaxlimit && !AppConstants.MaxlimitMessage.isEmpty()) {
            //AppConstants.colorToastBigFont(this, AppConstants.MaxlimitMessage, Color.RED);
            CommonUtils.AutoCloseCustomMessageDilaog(WelcomeActivity.this, "Message", AppConstants.MaxlimitMessage);
            AppConstants.DisplayToastmaxlimit = false;
            AppConstants.MaxlimitMessage = "";
        }

        IsUiChangeReq();
        if (AppConstants.EnableFA || AppConstants.EnableServerForTLD) {
            UpdateServerMessages();
        }

        if (BTConstants.CurrentTransactionIsBT && showUpgradeSpinnerMessage) {
            ManageBTLinkUpgrade();
        }

        // Toast.makeText(getApplicationContext(),"FS_Count"+FS_Count,Toast.LENGTH_SHORT).show();
        if (Constants.FS_1STATUS.equalsIgnoreCase("FREE")) {
            //Set "Tap here to select hose" message
            if (RestHoseinUse_FS1) {
                RestHoseinUse_FS1 = false;
                tvSSIDName.setText("Tap here to select hose");
                SelectedItemPos = -1;
                btnGo.setVisibility(View.VISIBLE);
            }

            if (AppConstants.IsTransactionFailed1) {
                AppConstants.IsTransactionFailed1 = false;
                if (BTConstants.CurrentTransactionIsBT) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "BTLink 1: Hose is Unavailable.");
                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "BS_FS1: Hose is Unavailable.");
                }
                if (AppConstants.TxnFailedCount1 == 1) {
                    CommonUtils.showCustomMessageDilaog(WelcomeActivity.this, "Message", getResources().getString(R.string.UnableToConnectToHoseMessage));
                } else {
                    AppConstants.TxnFailedCount1 = 0;
                    CommonUtils.showCustomMessageDilaog(WelcomeActivity.this, "Message", getResources().getString(R.string.HoseUnavailableMessage));
                }
            }

            Fs1_beginFuel.setVisibility(View.GONE); //Disable begin fueling message
            fs1Cnt5Sec = 0;
            CountBeforeReconnectRelay1 = 0;

            //Update FA Message on dashboard
            tv_FA_message.setText(Constants.FA_Message);
            tv_fs1_Qty.setText(Constants.FS_1Gallons);
            tv_fs1_Pulse.setText(Constants.FS_1Pulse);
            tv_fs1_stop.setClickable(false);
            FS1_Stpflag = true;

            if (Constants.FS_1Gallons.equals("") || Constants.FS_1Gallons.equals("0.00")) {
                Constants.FS_1Gallons = String.valueOf("0.00");
                Constants.FS_1Pulse = "00";
                tv_fs1_Qty.setText("");
                tv_fs1_Pulse.setText("");
                linear_fs_1.setBackgroundResource(R.color.Dashboard_background);
                tv_fs1_stop.setBackgroundResource(R.color.Dashboard_presstostop_btn);
                tv_NFS1.setTextColor(getResources().getColor(R.color.black));
                tv_FS1_hoseName.setTextColor(getResources().getColor(R.color.black));
                tv_fs1_stop.setTextColor(getResources().getColor(R.color.black));
                tv_fs1QTN.setTextColor(getResources().getColor(R.color.black));
                tv_fs1_pulseTxt.setTextColor(getResources().getColor(R.color.black));
                tv_fs1_Qty.setTextColor(getResources().getColor(R.color.black));
                tv_fs1_Pulse.setTextColor(getResources().getColor(R.color.black));
                tv_fs1_stop.setClickable(false);

            } else {


                Constants.FS_1Gallons = String.valueOf("0.00");
                Constants.FS_1Pulse = "00";
                tv_fs1_Qty.setText("");
                tv_fs1_Pulse.setText("");
                linear_fs_1.setBackgroundResource(R.color.Dashboard_background);
                tv_fs1_stop.setBackgroundResource(R.color.Dashboard_presstostop_btn);
                tv_NFS1.setTextColor(getResources().getColor(R.color.black));
                tv_FS1_hoseName.setTextColor(getResources().getColor(R.color.black));
                tv_fs1_stop.setTextColor(getResources().getColor(R.color.black));
                tv_fs1QTN.setTextColor(getResources().getColor(R.color.black));
                tv_fs1_pulseTxt.setTextColor(getResources().getColor(R.color.black));
                tv_fs1_Qty.setTextColor(getResources().getColor(R.color.black));
                tv_fs1_Pulse.setTextColor(getResources().getColor(R.color.black));
                tv_fs1_stop.setClickable(false);

            }

        } else {

            if ((fs1Cnt5Sec >= 5 || Integer.parseInt(Constants.FS_1Pulse) >= 1) && AppConstants.isRelayON_fs1) {
                Fs1_beginFuel.setVisibility(View.GONE);
                linear_fs_1.setVisibility(View.VISIBLE);
            } else {
                Fs1_beginFuel.setVisibility(View.VISIBLE);
                linear_fs_1.setVisibility(View.GONE);
                fs1Cnt5Sec++;
            }

            if (BTConstants.CurrentTransactionIsBT && !BTConstants.BTLinkOneStatus && AppConstants.isRelayON_fs1) {
                if (CountBeforeReconnectRelay1 >= 1) {
                    if (BTConstants.BTStatusStrOne.equalsIgnoreCase("Disconnect") && !BTConstants.isUpgradeInProgress_BT1) {
                        SaveLastQtyInSharedPref(1, Constants.FS_1Pulse);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "BTLink 1: Retrying to Connect");
                        BTConstants.isRelayOnAfterReconnect1 = false;
                        //Retrying to connect to link
                        BTSPPMain btspp = new BTSPPMain();
                        btspp.activity = WelcomeActivity.this;
                        btspp.connect1();
                        BTConstants.isReconnectCalled1 = true;
                    }
                } else {
                    CountBeforeReconnectRelay1++;
                }
            }

            //----------------------------------------
            tv_fs1_Qty.setText(Constants.FS_1Gallons);
            tv_fs1_Pulse.setText(Constants.FS_1Pulse);
            linear_fs_1.setBackgroundResource(R.color.colorPrimary);
            tv_fs1_stop.setBackgroundResource(R.drawable.selector_button);
            tv_NFS1.setTextColor(getResources().getColor(R.color.white));
            tv_fs1_stop.setTextColor(getResources().getColor(R.color.white));
            tv_FS1_hoseName.setTextColor(getResources().getColor(R.color.white));
            tv_fs1QTN.setTextColor(getResources().getColor(R.color.white));
            tv_fs1_pulseTxt.setTextColor(getResources().getColor(R.color.white));
            tv_fs1_Qty.setTextColor(getResources().getColor(R.color.white));
            tv_fs1_Pulse.setTextColor(getResources().getColor(R.color.white));
            if (FS1_Stpflag) {
                tv_fs1_stop.setClickable(true);
            } else {
                tv_fs1_stop.setClickable(false);
            }
        }

        if (Constants.FS_2STATUS.equalsIgnoreCase("FREE")) {

            //Set "Tap here to select hose" message
            if (RestHoseinUse_FS2) {
                RestHoseinUse_FS2 = false;
                tvSSIDName.setText("Tap here to select hose");
                SelectedItemPos = -1;
                btnGo.setVisibility(View.VISIBLE);
            }

            if (AppConstants.IsTransactionFailed2) {
                AppConstants.IsTransactionFailed2 = false;
                if (BTConstants.CurrentTransactionIsBT) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "BTLink 2: Hose is Unavailable.");
                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "BS_FS2: Hose is Unavailable.");
                }
                if (AppConstants.TxnFailedCount2 == 1) {
                    CommonUtils.showCustomMessageDilaog(WelcomeActivity.this, "Message", getResources().getString(R.string.UnableToConnectToHoseMessage));
                } else {
                    AppConstants.TxnFailedCount2 = 0;
                    CommonUtils.showCustomMessageDilaog(WelcomeActivity.this, "Message", getResources().getString(R.string.HoseUnavailableMessage));
                }
            }

            Fs2_beginFuel.setVisibility(View.GONE); //Disable begin fueling message
            fs2Cnt5Sec = 0;
            CountBeforeReconnectRelay2 = 0;

            tv_fs2_Qty.setText(Constants.FS_2Gallons);
            tv_fs2_Pulse.setText(Constants.FS_2Pulse);
            tv_fs2_stop.setClickable(false);
            FS2_Stpflag = true;

            if (Constants.FS_2Gallons.equals("") || Constants.FS_2Gallons.equals("0.00")) {
                Constants.FS_2Gallons = String.valueOf("0.00");
                Constants.FS_2Pulse = "00";
                tv_fs2_Qty.setText("");
                tv_fs2_Pulse.setText("");
                linear_fs_2.setBackgroundResource(R.color.Dashboard_background);
                tv_fs2_stop.setBackgroundResource(R.color.Dashboard_presstostop_btn);
                tv_NFS2.setTextColor(getResources().getColor(R.color.black));
                tv_FS2_hoseName.setTextColor(getResources().getColor(R.color.black));
                tv_fs2_stop.setTextColor(getResources().getColor(R.color.black));
                tv_fs2QTN.setTextColor(getResources().getColor(R.color.black));
                tv_fs2_pulseTxt.setTextColor(getResources().getColor(R.color.black));
                tv_fs2_Qty.setTextColor(getResources().getColor(R.color.black));
                tv_fs2_Pulse.setTextColor(getResources().getColor(R.color.black));
                tv_fs2_stop.setClickable(false);

            } else {

                Constants.FS_2Gallons = String.valueOf("0.00");
                Constants.FS_2Pulse = "00";
                tv_fs2_Qty.setText("");
                tv_fs2_Pulse.setText("");
                linear_fs_2.setBackgroundResource(R.color.Dashboard_background);
                tv_fs2_stop.setBackgroundResource(R.color.Dashboard_presstostop_btn);
                tv_NFS2.setTextColor(getResources().getColor(R.color.black));
                tv_FS2_hoseName.setTextColor(getResources().getColor(R.color.black));
                tv_fs2_stop.setTextColor(getResources().getColor(R.color.black));
                tv_fs2QTN.setTextColor(getResources().getColor(R.color.black));
                tv_fs2_pulseTxt.setTextColor(getResources().getColor(R.color.black));
                tv_fs2_Qty.setTextColor(getResources().getColor(R.color.black));
                tv_fs2_Pulse.setTextColor(getResources().getColor(R.color.black));
                tv_fs2_stop.setClickable(false);

            }

        } else {

            if ((fs2Cnt5Sec >= 5 || Integer.parseInt(Constants.FS_2Pulse) >= 1) && AppConstants.isRelayON_fs2) {
                Fs2_beginFuel.setVisibility(View.GONE);
                linear_fs_2.setVisibility(View.VISIBLE);
            } else {
                Fs2_beginFuel.setVisibility(View.VISIBLE);
                linear_fs_2.setVisibility(View.GONE);
                fs2Cnt5Sec++;
            }

            if (BTConstants.CurrentTransactionIsBT && !BTConstants.BTLinkTwoStatus && AppConstants.isRelayON_fs2) {
                if (CountBeforeReconnectRelay2 >= 1) {
                    if (BTConstants.BTStatusStrTwo.equalsIgnoreCase("Disconnect") && !BTConstants.isUpgradeInProgress_BT2) {
                        SaveLastQtyInSharedPref(2, Constants.FS_2Pulse);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "BTLink 2: Retrying to Connect");
                        BTConstants.isRelayOnAfterReconnect2 = false;
                        //Retrying to connect to link
                        BTSPPMain btspp = new BTSPPMain();
                        btspp.activity = WelcomeActivity.this;
                        btspp.connect2();
                        BTConstants.isReconnectCalled2 = true;
                    }
                } else {
                    CountBeforeReconnectRelay2++;
                }
            }

            tv_fs2_Qty.setText(Constants.FS_2Gallons);
            tv_fs2_Pulse.setText(Constants.FS_2Pulse);
            linear_fs_2.setBackgroundResource(R.color.colorPrimary);
            tv_fs2_stop.setBackgroundResource(R.drawable.selector_button);
            tv_NFS2.setTextColor(getResources().getColor(R.color.white));
            tv_fs2_stop.setTextColor(getResources().getColor(R.color.white));
            tv_fs2QTN.setTextColor(getResources().getColor(R.color.white));
            tv_fs2_pulseTxt.setTextColor(getResources().getColor(R.color.white));
            tv_FS2_hoseName.setTextColor(getResources().getColor(R.color.white));
            tv_fs2_Qty.setTextColor(getResources().getColor(R.color.white));
            tv_fs2_Pulse.setTextColor(getResources().getColor(R.color.white));
            if (FS2_Stpflag) {
                tv_fs2_stop.setClickable(true);
            } else {
                tv_fs2_stop.setClickable(false);
            }
        }

        if (Constants.FS_3STATUS.equalsIgnoreCase("FREE")) {

            //Set "Tap here to select hose" message
            if (RestHoseinUse_FS3) {
                RestHoseinUse_FS3 = false;
                tvSSIDName.setText("Tap here to select hose");
                SelectedItemPos = -1;
                btnGo.setVisibility(View.VISIBLE);
            }

            if (AppConstants.IsTransactionFailed3) {
                AppConstants.IsTransactionFailed3 = false;
                if (BTConstants.CurrentTransactionIsBT) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "BTLink 3: Hose is Unavailable.");
                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "BS_FS3: Hose is Unavailable.");
                }
                if (AppConstants.TxnFailedCount3 == 1) {
                    CommonUtils.showCustomMessageDilaog(WelcomeActivity.this, "Message", getResources().getString(R.string.UnableToConnectToHoseMessage));
                } else {
                    AppConstants.TxnFailedCount3 = 0;
                    CommonUtils.showCustomMessageDilaog(WelcomeActivity.this, "Message", getResources().getString(R.string.HoseUnavailableMessage));
                }
            }

            Fs3_beginFuel.setVisibility(View.GONE); //Disable begin fueling message
            fs3Cnt5Sec = 0;
            CountBeforeReconnectRelay3 = 0;

            tv_fs3_Qty.setText(Constants.FS_3Gallons);
            tv_fs3_Pulse.setText(Constants.FS_3Pulse);
            tv_fs3_stop.setClickable(false);
            FS3_Stpflag = true;

            if (Constants.FS_3Gallons.equals("") || Constants.FS_3Gallons.equals("0.00")) {
                Constants.FS_3Gallons = String.valueOf("0.00");
                Constants.FS_3Pulse = "00";
                tv_fs3_Qty.setText("");
                tv_fs3_Pulse.setText("");
                linear_fs_3.setBackgroundResource(R.color.Dashboard_background);
                tv_fs3_stop.setBackgroundResource(R.color.Dashboard_presstostop_btn);
                tv_NFS3.setTextColor(getResources().getColor(R.color.black));
                tv_FS3_hoseName.setTextColor(getResources().getColor(R.color.black));
                tv_fs3_stop.setTextColor(getResources().getColor(R.color.black));
                tv_fs3QTN.setTextColor(getResources().getColor(R.color.black));
                tv_fs3_pulseTxt.setTextColor(getResources().getColor(R.color.black));
                tv_fs3_Qty.setTextColor(getResources().getColor(R.color.black));
                tv_fs3_Pulse.setTextColor(getResources().getColor(R.color.black));
                tv_fs3_stop.setClickable(false);

            } else {


                Constants.FS_3Gallons = String.valueOf("0.00");
                Constants.FS_3Pulse = "00";
                tv_fs3_Qty.setText("");
                tv_fs3_Pulse.setText("");
                linear_fs_3.setBackgroundResource(R.color.Dashboard_background);
                tv_fs3_stop.setBackgroundResource(R.color.Dashboard_presstostop_btn);
                tv_NFS3.setTextColor(getResources().getColor(R.color.black));
                tv_FS3_hoseName.setTextColor(getResources().getColor(R.color.black));
                tv_fs3_stop.setTextColor(getResources().getColor(R.color.black));
                tv_fs3QTN.setTextColor(getResources().getColor(R.color.black));
                tv_fs3_pulseTxt.setTextColor(getResources().getColor(R.color.black));
                tv_fs3_Qty.setTextColor(getResources().getColor(R.color.black));
                tv_fs3_Pulse.setTextColor(getResources().getColor(R.color.black));
                tv_fs3_stop.setClickable(false);


            }


        } else {

            if ((fs3Cnt5Sec >= 5 || Integer.parseInt(Constants.FS_3Pulse) >= 1) && AppConstants.isRelayON_fs3) {
                Fs3_beginFuel.setVisibility(View.GONE);
                linear_fs_3.setVisibility(View.VISIBLE);
            } else {
                Fs3_beginFuel.setVisibility(View.VISIBLE);
                linear_fs_3.setVisibility(View.GONE);
                fs3Cnt5Sec++;
            }

            if (BTConstants.CurrentTransactionIsBT && !BTConstants.BTLinkThreeStatus && AppConstants.isRelayON_fs3) {
                if (CountBeforeReconnectRelay3 >= 1) {
                    if (BTConstants.BTStatusStrThree.equalsIgnoreCase("Disconnect") && !BTConstants.isUpgradeInProgress_BT3) {
                        SaveLastQtyInSharedPref(3, Constants.FS_3Pulse);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "BTLink 3: Retrying to Connect");
                        BTConstants.isRelayOnAfterReconnect3 = false;
                        //Retrying to connect to link
                        BTSPPMain btspp = new BTSPPMain();
                        btspp.activity = WelcomeActivity.this;
                        btspp.connect3();
                        BTConstants.isReconnectCalled3 = true;
                    }
                } else {
                    CountBeforeReconnectRelay3++;
                }
            }

            tv_fs3_Qty.setText(Constants.FS_3Gallons);
            tv_fs3_Pulse.setText(Constants.FS_3Pulse);
            linear_fs_3.setBackgroundResource(R.color.colorPrimary);
            tv_fs3_stop.setBackgroundResource(R.drawable.selector_button);
            tv_NFS3.setTextColor(getResources().getColor(R.color.white));
            tv_fs3_stop.setTextColor(getResources().getColor(R.color.white));
            tv_fs3QTN.setTextColor(getResources().getColor(R.color.white));
            tv_fs3_pulseTxt.setTextColor(getResources().getColor(R.color.white));
            tv_FS3_hoseName.setTextColor(getResources().getColor(R.color.white));
            tv_fs3_Qty.setTextColor(getResources().getColor(R.color.white));
            tv_fs3_Pulse.setTextColor(getResources().getColor(R.color.white));
            if (FS3_Stpflag) {
                tv_fs3_stop.setClickable(true);
            } else {
                tv_fs3_stop.setClickable(false);
            }
        }

        if (Constants.FS_4STATUS.equalsIgnoreCase("FREE")) {

            //Set "Tap here to select hose" message
            if (RestHoseinUse_FS4) {
                RestHoseinUse_FS4 = false;
                tvSSIDName.setText("Tap here to select hose");
                SelectedItemPos = -1;
                btnGo.setVisibility(View.VISIBLE);
            }

            if (AppConstants.IsTransactionFailed4) {
                AppConstants.IsTransactionFailed4 = false;
                if (BTConstants.CurrentTransactionIsBT) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "BTLink 4: Hose is Unavailable.");
                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "BS_FS4: Hose is Unavailable.");
                }
                if (AppConstants.TxnFailedCount4 == 1) {
                    CommonUtils.showCustomMessageDilaog(WelcomeActivity.this, "Message", getResources().getString(R.string.UnableToConnectToHoseMessage));
                } else {
                    AppConstants.TxnFailedCount4 = 0;
                    CommonUtils.showCustomMessageDilaog(WelcomeActivity.this, "Message", getResources().getString(R.string.HoseUnavailableMessage));
                }
            }

            Fs4_beginFuel.setVisibility(View.GONE); //Disable begin fueling message
            fs4Cnt5Sec = 0;
            CountBeforeReconnectRelay4 = 0;

            tv_fs4_Qty.setText(Constants.FS_4Gallons);
            tv_fs4_Pulse.setText(Constants.FS_4Pulse);
            tv_fs4_stop.setClickable(false);
            FS4_Stpflag = true;

            if (Constants.FS_4Gallons.equals("") || Constants.FS_4Gallons.equals("0.00")) {
                Constants.FS_4Gallons = String.valueOf("0.00");
                Constants.FS_4Pulse = "00";
                tv_fs4_Qty.setText("");
                tv_fs4_Pulse.setText("");
                linear_fs_4.setBackgroundResource(R.color.Dashboard_background);
                tv_fs4_stop.setBackgroundResource(R.color.Dashboard_presstostop_btn);
                tv_NFS4.setTextColor(getResources().getColor(R.color.black));
                tv_FS4_hoseName.setTextColor(getResources().getColor(R.color.black));
                tv_fs4_stop.setTextColor(getResources().getColor(R.color.black));
                tv_fs4QTN.setTextColor(getResources().getColor(R.color.black));
                tv_fs4_pulseTxt.setTextColor(getResources().getColor(R.color.black));
                tv_fs4_Qty.setTextColor(getResources().getColor(R.color.black));
                tv_fs4_Pulse.setTextColor(getResources().getColor(R.color.black));
                tv_fs4_stop.setClickable(false);

            } else {


                Constants.FS_4Gallons = String.valueOf("0.00");
                Constants.FS_4Pulse = "00";
                tv_fs4_Qty.setText("");
                tv_fs4_Pulse.setText("");
                linear_fs_4.setBackgroundResource(R.color.Dashboard_background);
                tv_fs4_stop.setBackgroundResource(R.color.Dashboard_presstostop_btn);
                tv_NFS4.setTextColor(getResources().getColor(R.color.black));
                tv_FS4_hoseName.setTextColor(getResources().getColor(R.color.black));
                tv_fs4_stop.setTextColor(getResources().getColor(R.color.black));
                tv_fs4QTN.setTextColor(getResources().getColor(R.color.black));
                tv_fs4_pulseTxt.setTextColor(getResources().getColor(R.color.black));
                tv_fs4_Qty.setTextColor(getResources().getColor(R.color.black));
                tv_fs4_Pulse.setTextColor(getResources().getColor(R.color.black));
                tv_fs4_stop.setClickable(false);

            }

        } else {

            if ((fs4Cnt5Sec >= 5 || Integer.parseInt(Constants.FS_4Pulse) >= 1) && AppConstants.isRelayON_fs4) {
                Fs4_beginFuel.setVisibility(View.GONE);
                linear_fs_4.setVisibility(View.VISIBLE);
            } else {
                Fs4_beginFuel.setVisibility(View.VISIBLE);
                linear_fs_4.setVisibility(View.GONE);
                fs4Cnt5Sec++;
            }

            if (BTConstants.CurrentTransactionIsBT && !BTConstants.BTLinkFourStatus && AppConstants.isRelayON_fs4) {
                if (CountBeforeReconnectRelay4 >= 1) {
                    if (BTConstants.BTStatusStrFour.equalsIgnoreCase("Disconnect") && !BTConstants.isUpgradeInProgress_BT4) {
                        SaveLastQtyInSharedPref(4, Constants.FS_4Pulse);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "BTLink 4: Retrying to Connect");
                        BTConstants.isRelayOnAfterReconnect4 = false;
                        //Retrying to connect to link
                        BTSPPMain btspp = new BTSPPMain();
                        btspp.activity = WelcomeActivity.this;
                        btspp.connect4();
                        BTConstants.isReconnectCalled4= true;
                    }
                } else {
                    CountBeforeReconnectRelay4++;
                }
            }

            tv_fs4_Qty.setText(Constants.FS_4Gallons);
            tv_fs4_Pulse.setText(Constants.FS_4Pulse);
            linear_fs_4.setBackgroundResource(R.color.colorPrimary);
            tv_fs4_stop.setBackgroundResource(R.drawable.selector_button);
            tv_NFS4.setTextColor(getResources().getColor(R.color.white));
            tv_fs4_stop.setTextColor(getResources().getColor(R.color.white));
            tv_fs4QTN.setTextColor(getResources().getColor(R.color.white));
            tv_fs4_pulseTxt.setTextColor(getResources().getColor(R.color.white));
            tv_FS4_hoseName.setTextColor(getResources().getColor(R.color.white));
            tv_fs4_Qty.setTextColor(getResources().getColor(R.color.white));
            tv_fs4_Pulse.setTextColor(getResources().getColor(R.color.white));
            if (FS4_Stpflag) {
                tv_fs4_stop.setClickable(true);
            } else {
                tv_fs4_stop.setClickable(false);
            }
        }

        ///////
        if (Constants.FS_5STATUS.equalsIgnoreCase("FREE")) {

            //Set "Tap here to select hose" message
            if (RestHoseinUse_FS5) {
                RestHoseinUse_FS5 = false;
                tvSSIDName.setText("Tap here to select hose");
                SelectedItemPos = -1;
                btnGo.setVisibility(View.VISIBLE);
            }

            if (AppConstants.IsTransactionFailed5) {
                AppConstants.IsTransactionFailed5 = false;
                if (BTConstants.CurrentTransactionIsBT) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "BTLink 5: Hose is Unavailable.");
                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "BS_FS5: Hose is Unavailable.");
                }
                if (AppConstants.TxnFailedCount5 == 1) {
                    CommonUtils.showCustomMessageDilaog(WelcomeActivity.this, "Message", getResources().getString(R.string.UnableToConnectToHoseMessage));
                } else {
                    AppConstants.TxnFailedCount5 = 0;
                    CommonUtils.showCustomMessageDilaog(WelcomeActivity.this, "Message", getResources().getString(R.string.HoseUnavailableMessage));
                }
            }

            Fs5_beginFuel.setVisibility(View.GONE); //Disable begin fueling message
            fs5Cnt5Sec = 0;

            tv_fs5_Qty.setText(Constants.FS_5Gallons);
            tv_fs5_Pulse.setText(Constants.FS_5Pulse);
            tv_fs5_stop.setClickable(false);
            FS5_Stpflag = true;

            if (Constants.FS_5Gallons.equals("") || Constants.FS_5Gallons.equals("0.00")) {
                Constants.FS_5Gallons = String.valueOf("0.00");
                Constants.FS_5Pulse = "00";
                tv_fs5_Qty.setText("");
                tv_fs5_Pulse.setText("");
                linear_fs_5.setBackgroundResource(R.color.Dashboard_background);
                tv_fs5_stop.setBackgroundResource(R.color.Dashboard_presstostop_btn);
                tv_NFS5.setTextColor(getResources().getColor(R.color.black));
                tv_FS5_hoseName.setTextColor(getResources().getColor(R.color.black));
                tv_fs5_stop.setTextColor(getResources().getColor(R.color.black));
                tv_fs5QTN.setTextColor(getResources().getColor(R.color.black));
                tv_fs5_pulseTxt.setTextColor(getResources().getColor(R.color.black));
                tv_fs5_Qty.setTextColor(getResources().getColor(R.color.black));
                tv_fs5_Pulse.setTextColor(getResources().getColor(R.color.black));
                tv_fs5_stop.setClickable(false);

            } else {


                Constants.FS_5Gallons = String.valueOf("0.00");
                Constants.FS_5Pulse = "00";
                tv_fs5_Qty.setText("");
                tv_fs5_Pulse.setText("");
                linear_fs_5.setBackgroundResource(R.color.Dashboard_background);
                tv_fs5_stop.setBackgroundResource(R.color.Dashboard_presstostop_btn);
                tv_NFS5.setTextColor(getResources().getColor(R.color.black));
                tv_FS5_hoseName.setTextColor(getResources().getColor(R.color.black));
                tv_fs5_stop.setTextColor(getResources().getColor(R.color.black));
                tv_fs5QTN.setTextColor(getResources().getColor(R.color.black));
                tv_fs5_pulseTxt.setTextColor(getResources().getColor(R.color.black));
                tv_fs5_Qty.setTextColor(getResources().getColor(R.color.black));
                tv_fs5_Pulse.setTextColor(getResources().getColor(R.color.black));
                tv_fs5_stop.setClickable(false);

            }


        } else {

            if ((fs5Cnt5Sec >= 5 || Integer.parseInt(Constants.FS_5Pulse) >= 1) && AppConstants.isRelayON_fs5) {
                Fs5_beginFuel.setVisibility(View.GONE);
                linear_fs_5.setVisibility(View.VISIBLE);
            } else {
                Fs5_beginFuel.setVisibility(View.VISIBLE);
                linear_fs_5.setVisibility(View.GONE);
                fs5Cnt5Sec++;
            }

            tv_fs5_Qty.setText(Constants.FS_5Gallons);
            tv_fs5_Pulse.setText(Constants.FS_5Pulse);
            linear_fs_5.setBackgroundResource(R.color.colorPrimary);
            tv_fs5_stop.setBackgroundResource(R.drawable.selector_button);
            tv_NFS5.setTextColor(getResources().getColor(R.color.white));
            tv_fs5_stop.setTextColor(getResources().getColor(R.color.white));
            tv_fs5QTN.setTextColor(getResources().getColor(R.color.white));
            tv_fs5_pulseTxt.setTextColor(getResources().getColor(R.color.white));
            tv_FS5_hoseName.setTextColor(getResources().getColor(R.color.white));
            tv_fs5_Qty.setTextColor(getResources().getColor(R.color.white));
            tv_fs5_Pulse.setTextColor(getResources().getColor(R.color.white));
            if (FS5_Stpflag) {
                tv_fs5_stop.setClickable(true);
            } else {
                tv_fs5_stop.setClickable(false);
            }
        }

        if (Constants.FS_6STATUS.equalsIgnoreCase("FREE")) {

            //Set "Tap here to select hose" message
            if (RestHoseinUse_FS6) {
                RestHoseinUse_FS6 = false;
                tvSSIDName.setText("Tap here to select hose");
                SelectedItemPos = -1;
                btnGo.setVisibility(View.VISIBLE);
            }

            if (AppConstants.IsTransactionFailed6) {
                AppConstants.IsTransactionFailed6 = false;
                if (BTConstants.CurrentTransactionIsBT) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "BTLink 6: Hose is Unavailable.");
                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(AppConstants.LOG_TXTN_HTTP + "-" + TAG + "BS_FS6: Hose is Unavailable.");
                }
                if (AppConstants.TxnFailedCount6 == 1) {
                    CommonUtils.showCustomMessageDilaog(WelcomeActivity.this, "Message", getResources().getString(R.string.UnableToConnectToHoseMessage));
                } else {
                    AppConstants.TxnFailedCount6 = 0;
                    CommonUtils.showCustomMessageDilaog(WelcomeActivity.this, "Message", getResources().getString(R.string.HoseUnavailableMessage));
                }
            }

            Fs6_beginFuel.setVisibility(View.GONE); //Disable begin fueling message
            fs6Cnt5Sec = 0;

            tv_fs6_Qty.setText(Constants.FS_6Gallons);
            tv_fs6_Pulse.setText(Constants.FS_6Pulse);
            tv_fs6_stop.setClickable(false);
            FS6_Stpflag = true;

            if (Constants.FS_6Gallons.equals("") || Constants.FS_6Gallons.equals("0.00")) {
                Constants.FS_6Gallons = String.valueOf("0.00");
                Constants.FS_6Pulse = "00";
                tv_fs6_Qty.setText("");
                tv_fs6_Pulse.setText("");
                linear_fs_6.setBackgroundResource(R.color.Dashboard_background);
                tv_fs6_stop.setBackgroundResource(R.color.Dashboard_presstostop_btn);
                tv_NFS6.setTextColor(getResources().getColor(R.color.black));
                tv_FS6_hoseName.setTextColor(getResources().getColor(R.color.black));
                tv_fs6_stop.setTextColor(getResources().getColor(R.color.black));
                tv_fs6QTN.setTextColor(getResources().getColor(R.color.black));
                tv_fs6_pulseTxt.setTextColor(getResources().getColor(R.color.black));
                tv_fs6_Qty.setTextColor(getResources().getColor(R.color.black));
                tv_fs6_Pulse.setTextColor(getResources().getColor(R.color.black));
                tv_fs6_stop.setClickable(false);

            } else {


                Constants.FS_6Gallons = String.valueOf("0.00");
                Constants.FS_6Pulse = "00";
                tv_fs6_Qty.setText("");
                tv_fs6_Pulse.setText("");
                linear_fs_6.setBackgroundResource(R.color.Dashboard_background);
                tv_fs6_stop.setBackgroundResource(R.color.Dashboard_presstostop_btn);
                tv_NFS6.setTextColor(getResources().getColor(R.color.black));
                tv_FS6_hoseName.setTextColor(getResources().getColor(R.color.black));
                tv_fs6_stop.setTextColor(getResources().getColor(R.color.black));
                tv_fs6QTN.setTextColor(getResources().getColor(R.color.black));
                tv_fs6_pulseTxt.setTextColor(getResources().getColor(R.color.black));
                tv_fs6_Qty.setTextColor(getResources().getColor(R.color.black));
                tv_fs6_Pulse.setTextColor(getResources().getColor(R.color.black));
                tv_fs6_stop.setClickable(false);

            }


        } else {

            if ((fs6Cnt5Sec >= 5 || Integer.parseInt(Constants.FS_6Pulse) >= 1) && AppConstants.isRelayON_fs6) {
                Fs6_beginFuel.setVisibility(View.GONE);
                linear_fs_6.setVisibility(View.VISIBLE);
            } else {
                Fs6_beginFuel.setVisibility(View.VISIBLE);
                linear_fs_6.setVisibility(View.GONE);
                fs6Cnt5Sec++;
            }

            tv_fs6_Qty.setText(Constants.FS_6Gallons);
            tv_fs6_Pulse.setText(Constants.FS_6Pulse);
            linear_fs_6.setBackgroundResource(R.color.colorPrimary);
            tv_fs6_stop.setBackgroundResource(R.drawable.selector_button);
            tv_NFS6.setTextColor(getResources().getColor(R.color.white));
            tv_fs6_stop.setTextColor(getResources().getColor(R.color.white));
            tv_fs6QTN.setTextColor(getResources().getColor(R.color.white));
            tv_fs6_pulseTxt.setTextColor(getResources().getColor(R.color.white));
            tv_FS6_hoseName.setTextColor(getResources().getColor(R.color.white));
            tv_fs6_Qty.setTextColor(getResources().getColor(R.color.white));
            tv_fs6_Pulse.setTextColor(getResources().getColor(R.color.white));
            if (FS6_Stpflag) {
                tv_fs6_stop.setClickable(true);
            } else {
                tv_fs6_stop.setClickable(false);
            }
        }
        ///////
    }

    public void SaveLastQtyInSharedPref(int position, String Pulses) {

        switch (position) {
            case 1:
                SharedPreferences sharedPrefLastQty1 = this.getSharedPreferences("LastQuantity_BT1", Context.MODE_PRIVATE);
                long current_count1 = Long.parseLong(String.valueOf(Pulses));
                SharedPreferences.Editor editorQty1 = sharedPrefLastQty1.edit();
                editorQty1.putLong("Last_Quantity", current_count1);
                editorQty1.commit();
                break;
            case 2:
                SharedPreferences sharedPrefLastQty2 = this.getSharedPreferences("LastQuantity_BT2", Context.MODE_PRIVATE);
                long current_count2 = Long.parseLong(String.valueOf(Pulses));
                SharedPreferences.Editor editorQty2 = sharedPrefLastQty2.edit();
                editorQty2.putLong("Last_Quantity", current_count2);
                editorQty2.commit();
                break;
            case 3:
                SharedPreferences sharedPrefLastQty3 = this.getSharedPreferences("LastQuantity_BT3", Context.MODE_PRIVATE);
                long current_count3 = Long.parseLong(String.valueOf(Pulses));
                SharedPreferences.Editor editorQty3 = sharedPrefLastQty3.edit();
                editorQty3.putLong("Last_Quantity", current_count3);
                editorQty3.commit();
                break;
            case 4:
                SharedPreferences sharedPrefLastQty4 = this.getSharedPreferences("LastQuantity_BT4", Context.MODE_PRIVATE);
                long current_count4 = Long.parseLong(String.valueOf(Pulses));
                SharedPreferences.Editor editorQty4 = sharedPrefLastQty4.edit();
                editorQty4.putLong("Last_Quantity", current_count4);
                editorQty4.commit();
                break;
        }
    }

    public class ChangeBusyStatus extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... param) {
            String resp = "";

            String userEmail = CommonUtils.getCustomerDetails(WelcomeActivity.this).PersonEmail;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(WelcomeActivity.this) + ":" + userEmail + ":" + "UpgradeIsBusyStatus");

            RenameHose rhose = new RenameHose();
            rhose.SiteId = AppConstants.CURRENT_SELECTED_SITEID;


            Gson gson = new Gson();
            String jsonData = gson.toJson(rhose);


            try {
                OkHttpClient client = new OkHttpClient();
                MediaType TEXT = MediaType.parse("application/text;charset=UTF-8");

                RequestBody body = RequestBody.create(TEXT, jsonData);
                Request request = new Request.Builder()
                        .url(AppConstants.webURL)
                        .post(body)
                        .addHeader("Authorization", authString)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
                if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            try {

                // pd.dismiss();
                System.out.println("eeee" + result);

            } catch (Exception e) {
                System.out.println("eeee" + e);
                if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }
        }
    }


    public class ChangeBusyStatusOnGoButton extends AsyncTask<String, Void, String> {

        ProgressDialog pd;
        public String LinkCommType = "";

        @Override
        protected void onPreExecute() {

            String s = "Please wait...";
            SpannableString ss2 = new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage(ss2);
            pd.setCancelable(true);
            pd.show();

        }

        protected String doInBackground(String... params) {

            String resp = "";
            LinkCommType = params[0];

            String userEmail = CommonUtils.getCustomerDetails(WelcomeActivity.this).PersonEmail;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(WelcomeActivity.this) + ":" + userEmail + ":" + "UpgradeIsBusyStatus");

            RenameHose rhose = new RenameHose();
            rhose.SiteId = AppConstants.CURRENT_SELECTED_SITEID;

            Gson gson = new Gson();
            String jsonData = gson.toJson(rhose);

            try {
                OkHttpClient client = new OkHttpClient();
                MediaType TEXT = MediaType.parse("application/text;charset=UTF-8");

                RequestBody body = RequestBody.create(TEXT, jsonData);
                Request request = new Request.Builder()
                        .url(AppConstants.webURL)
                        .post(body)
                        .addHeader("Authorization", authString)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
                if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }

            return resp;
        }

        @Override
        protected void onPostExecute(String siteResponse) {

            pd.dismiss();

            try {
                //Set FluidSecure Link Busy:
                JSONObject jsonObject = null;
                jsonObject = new JSONObject(siteResponse);

                String ResponceMessage = jsonObject.getString("ResponceMessage");
                if (ResponceMessage.equalsIgnoreCase("success")) {

                    String ResponceText = jsonObject.getString("ResponceText");
                    System.out.println("eeee1" + ResponceText);
                    if (ResponceText.equalsIgnoreCase("Y")) {
                        flagGoBtn = true;//Enable go button
                        // AppConstants.colorToastBigFont(WelcomeActivity.this, "Hose in use", Color.RED);
                        CommonUtils.showCustomMessageDilaog(WelcomeActivity.this, "", "Hose In Use. Please retry once the ongoing transaction has been completed.");

                        String txtnTypeForLog = "";
                        if (LinkCommType.equalsIgnoreCase("BT")) {
                            txtnTypeForLog = AppConstants.LOG_TXTN_BT;
                        } else {
                            txtnTypeForLog = AppConstants.LOG_TXTN_HTTP;
                        }

                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(txtnTypeForLog + "-" + TAG + " Hose In Use. Please retry once the ongoing transaction has been completed.");
                    } else {
                        new handleGetAndroidSSID().execute(AppConstants.LAST_CONNECTED_SSID, LinkCommType);//AppConstants.LAST_CONNECTED_SSID = selectedSSID
                        //startWelcomeActivity();
                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
                if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }

        }

    }

    public void RenameLink() {

        HTTP_URL = "http://192.168.4.1/";
        String URL_WIFI = HTTP_URL + "config?command=wifi";
        jsonRename = "{\"Request\":{\"Softap\":{\"Connect_Softap\":{\"authmode\":\"WPAPSK/WPA2PSK\",\"channel\":6,\"ssid\":\"" + AppConstants.REPLACEBLE_WIFI_NAME_FS_ON_UPDATE_MAC + "\",\"password\":\"123456789\"}}}}";

        if (AppConstants.NeedToRenameFS_ON_UPDATE_MAC) {

            consoleString += "RENAME:\n" + jsonRename;

            new CommandsPOST().execute(URL_WIFI, jsonRename);

        }

    }

    @Override
    public void onBackPressed() {


        finish();

    }

    public class SetHoseNameReplacedFlagO_Mac extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... param) {
            String resp = "";


            try {
                OkHttpClient client = new OkHttpClient();
                MediaType TEXT = MediaType.parse("application/text;charset=UTF-8");

                RequestBody body = RequestBody.create(TEXT, param[0]);
                Request request = new Request.Builder()
                        .url(AppConstants.webURL)
                        .post(body)
                        .addHeader("Authorization", param[1])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
                if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }

            return resp;
        }


        @Override
        protected void onPostExecute(String result) {
            try {

                System.out.println("Wifi renamed on server---" + result);

            } catch (Exception e) {
                System.out.println("eeee" + e);
                if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }
        }


    }

    public void OnHoseSelected_OnClick(String position) {


        String IpAddress = "";
        SelectedItemPos = Integer.parseInt(position);
        String ReconfigureLink = serverSSIDList.get(SelectedItemPos).get("ReconfigureLink");
        String selSSID = serverSSIDList.get(SelectedItemPos).get("WifiSSId");
        String IsBusy = serverSSIDList.get(SelectedItemPos).get("IsBusy");
        String selMacAddress = serverSSIDList.get(SelectedItemPos).get("MacAddress");
        String selSiteId = serverSSIDList.get(SelectedItemPos).get("SiteId");
        String hoseID = serverSSIDList.get(SelectedItemPos).get("HoseId");
        AppConstants.CURRENT_SELECTED_SSID = selSSID;
        AppConstants.CURRENT_HOSE_SSID = hoseID;
        AppConstants.CURRENT_SELECTED_SITEID = selSiteId;
        AppConstants.SELECTED_MACADDRESS = selMacAddress;
        String IsHoseNameReplaced = serverSSIDList.get(SelectedItemPos).get("IsHoseNameReplaced");
        String ReplaceableHoseName = serverSSIDList.get(SelectedItemPos).get("ReplaceableHoseName");
        AppConstants.PulserTimingAdjust = serverSSIDList.get(SelectedItemPos).get("PulserTimingAdjust");
        String IsLinkFlagged = serverSSIDList.get(SelectedItemPos).get("IsLinkFlagged");
        String LinkFlaggedMessage = serverSSIDList.get(SelectedItemPos).get("LinkFlaggedMessage");
        String LinkCommunicationType = serverSSIDList.get(SelectedItemPos).get("LinkCommunicationType");
        AppConstants.UP_FilePath = serverSSIDList.get(SelectedItemPos).get("UPFilePath");
        AppConstants.IsResetSwitchTimeBounce = serverSSIDList.get(SelectedItemPos).get("IsResetSwitchTimeBounce");

        if (IsHoseNameReplaced == null) {
            IsHoseNameReplaced = "";
        }
        if (IsBusy == null) {
            IsBusy = "N";
        }
        //Rename SSID while mac address updation
        if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {
            AppConstants.NeedToRenameFS_ON_UPDATE_MAC = false;
            AppConstants.REPLACEBLE_WIFI_NAME_FS_ON_UPDATE_MAC = "";
        } else {
            AppConstants.NeedToRenameFS_ON_UPDATE_MAC = true;
            AppConstants.REPLACEBLE_WIFI_NAME_FS_ON_UPDATE_MAC = ReplaceableHoseName;
        }

        if (ReconfigureLink != null && ReconfigureLink.equalsIgnoreCase("true")) {

            if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE") && Constants.FS_5STATUS.equalsIgnoreCase("FREE") && Constants.FS_6STATUS.equalsIgnoreCase("FREE")) {

                //RemoveWifiNetworks();

                //Link Reconfigure process start
                ReconfigureCountdown();
                Constants.hotspotstayOn = false;//hotspot enable/disable flag
                wifiApManager.setWifiApEnabled(null, false);  //Disabled Hotspot

                //Enable wifi
                WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                if (!wifiManagerMM.isWifiEnabled()) {
                    wifiManagerMM.setWifiEnabled(true);
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        connectWiFiLibrary("3"); //connect to selected wifi
                    }
                }, 1000);


            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Can't update mac address,Hose is busy please retry later.");
                AppConstants.colorToastBigFont(WelcomeActivity.this, "Can't update mac address,Hose is busy please retry later.", Color.RED);
                btnGo.setVisibility(View.GONE);
            }

        } else if (IsLinkFlagged != null && IsLinkFlagged.equalsIgnoreCase("True")) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Flagged Link: " + LinkFlaggedMessage);
            CommonUtils.AlertDialogAutoClose(WelcomeActivity.this, "", LinkFlaggedMessage);
            RestrictHoseSelection("Please try again later");

        } else {

            if (!LinkCommunicationType.equalsIgnoreCase("BT")) {
                try {
                    IpAddress = "";

                    if (AppConstants.DetailsListOfConnectedDevices != null) {
                        for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                            String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");

                            if (selMacAddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                                IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                                break;
                            }
                        }
                    }

                    /*boolean isMacConnected = false;
                    if (AppConstants.DetailsListOfConnectedDevices != null) {
                        for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                            String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");

                            if (selMacAddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + selMacAddress + ") is connected to hotspot.");
                                IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                                isMacConnected = true;
                                break;
                            }
                        }
                    }

                    if (!isMacConnected) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + selMacAddress + ") is not found in connected devices. " + AppConstants.DetailsListOfConnectedDevices);
                        for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                            String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Checking Mac Address using info command: (" + MA_ConnectedDevices + ")");

                            String connectedIp = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");

                            IpAddress = GetAndCheckMacAddressFromInfoCommand(connectedIp, selMacAddress, MA_ConnectedDevices);
                            if (!IpAddress.trim().isEmpty()) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile("===================================================================");
                                break;
                            }
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile("===================================================================");
                        }
                    }*/
                } catch (Exception e) {
                    System.out.println(e);
                }
            }

            /*if (IpAddress.equals("") && !LinkCommunicationType.equalsIgnoreCase("BT")) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Issue #812 HNC-3(selMacAddress:" + selMacAddress + ") " + AppConstants.DetailsListOfConnectedDevices);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Hose not connected");
                RestrictHoseSelection("Hose not connected");

            } else {*/

            //Selected position

            AppConstants.FS_selected = String.valueOf(position);
            if (String.valueOf(position).equalsIgnoreCase("0")) {

                if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && IsBusy.equalsIgnoreCase("N")) {
                    // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

                    //Rename SSID from cloud
                    if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {
                        AppConstants.NeedToRenameFS1 = false;
                        AppConstants.REPLACEBLE_WIFI_NAME_FS1 = "";
                    } else {
                        AppConstants.NeedToRenameFS1 = true;
                        AppConstants.REPLACEBLE_WIFI_NAME_FS1 = ReplaceableHoseName;
                    }

                    Constants.AccPersonnelPIN = "";
                    tvSSIDName.setText(selSSID);
                    AppConstants.FS1_CONNECTED_SSID = selSSID;
                    Constants.CurrentSelectedHose = "FS1";
                    btnGo.setVisibility(View.VISIBLE);
                    //goButtonAction(null);
                } else {
                    RestrictHoseSelection("Hose in use.\nPlease try again later");

                }
            } else if (String.valueOf(position).equalsIgnoreCase("1")) {
                if (Constants.FS_2STATUS.equalsIgnoreCase("FREE") && IsBusy.equalsIgnoreCase("N")) {
                    // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

                    //Rename SSID from cloud
                    if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {
                        AppConstants.NeedToRenameFS2 = false;
                        AppConstants.REPLACEBLE_WIFI_NAME_FS2 = "";
                    } else {
                        AppConstants.NeedToRenameFS2 = true;
                        AppConstants.REPLACEBLE_WIFI_NAME_FS2 = ReplaceableHoseName;
                    }

                    Constants.AccPersonnelPIN = "";
                    tvSSIDName.setText(selSSID);
                    AppConstants.FS2_CONNECTED_SSID = selSSID;
                    Constants.CurrentSelectedHose = "FS2";
                    btnGo.setVisibility(View.VISIBLE);
                } else {
                    RestrictHoseSelection("Hose in use.\nPlease try again later");
                }

            } else if (String.valueOf(position).equalsIgnoreCase("2")) {


                if (Constants.FS_3STATUS.equalsIgnoreCase("FREE") && IsBusy.equalsIgnoreCase("N")) {
                    // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

                    //Rename SSID from cloud
                    if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {
                        AppConstants.NeedToRenameFS3 = false;
                        AppConstants.REPLACEBLE_WIFI_NAME_FS3 = "";
                    } else {
                        AppConstants.NeedToRenameFS3 = true;
                        AppConstants.REPLACEBLE_WIFI_NAME_FS3 = ReplaceableHoseName;
                    }

                    Constants.AccPersonnelPIN = "";
                    tvSSIDName.setText(selSSID);
                    AppConstants.FS3_CONNECTED_SSID = selSSID;
                    Constants.CurrentSelectedHose = "FS3";
                    btnGo.setVisibility(View.VISIBLE);
                } else {
                    RestrictHoseSelection("Hose in use.\nPlease try again later");
                }


            } else if (String.valueOf(position).equalsIgnoreCase("3")) {


                if (Constants.FS_4STATUS.equalsIgnoreCase("FREE") && IsBusy.equalsIgnoreCase("N")) {
                    // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                    //Rename SSID from cloud
                    if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {
                        AppConstants.NeedToRenameFS4 = false;
                        AppConstants.REPLACEBLE_WIFI_NAME_FS4 = "";
                    } else {
                        AppConstants.NeedToRenameFS4 = true;
                        AppConstants.REPLACEBLE_WIFI_NAME_FS4 = ReplaceableHoseName;
                    }

                    Constants.AccPersonnelPIN = "";
                    tvSSIDName.setText(selSSID);
                    AppConstants.FS4_CONNECTED_SSID = selSSID;
                    Constants.CurrentSelectedHose = "FS4";
                    btnGo.setVisibility(View.VISIBLE);
                } else {
                    RestrictHoseSelection("Hose in use.\nPlease try again later");
                }
            } else if (String.valueOf(position).equalsIgnoreCase("4")) {


                if (Constants.FS_5STATUS.equalsIgnoreCase("FREE") && IsBusy.equalsIgnoreCase("N")) {

                    if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {
                        AppConstants.NeedToRenameFS5 = false;
                        AppConstants.REPLACEBLE_WIFI_NAME_FS5 = "";
                    } else {
                        AppConstants.NeedToRenameFS5 = true;
                        AppConstants.REPLACEBLE_WIFI_NAME_FS5 = ReplaceableHoseName;
                    }

                    Constants.AccPersonnelPIN = "";
                    tvSSIDName.setText(selSSID);
                    AppConstants.FS5_CONNECTED_SSID = selSSID;
                    Constants.CurrentSelectedHose = "FS5";
                    btnGo.setVisibility(View.VISIBLE);
                } else {
                    RestrictHoseSelection("Hose in use.\nPlease try again later");
                }
            } else if (String.valueOf(position).equalsIgnoreCase("5")) {


                if (Constants.FS_6STATUS.equalsIgnoreCase("FREE") && IsBusy.equalsIgnoreCase("N")) {

                    if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {
                        AppConstants.NeedToRenameFS6 = false;
                        AppConstants.REPLACEBLE_WIFI_NAME_FS6 = "";
                    } else {
                        AppConstants.NeedToRenameFS6 = true;
                        AppConstants.REPLACEBLE_WIFI_NAME_FS6 = ReplaceableHoseName;
                    }

                    Constants.AccPersonnelPIN = "";
                    tvSSIDName.setText(selSSID);
                    AppConstants.FS6_CONNECTED_SSID = selSSID;
                    Constants.CurrentSelectedHose = "FS6";
                    btnGo.setVisibility(View.VISIBLE);
                } else {
                    RestrictHoseSelection("Hose in use.\nPlease try again later");
                }
            } else {

                tvSSIDName.setText("Can't select this Hose for current version");
                btnGo.setVisibility(View.GONE);
            }
            //}

        }
        //dialog.dismiss();

    }


    //========================ends=========================================

    //========================BT start=========================================

    /*
     * Listen to Bluetooth bond status change event. And turns on reader's
     * notifications once the card reader is bonded.
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothAdapter bluetoothAdapter = null;
            BluetoothManager bluetoothManager = null;
            final String action = intent.getAction();

            if (!(mBluetoothReader instanceof Acr3901us1Reader)) {
                /* Only ACR3901U-S1 require bonding. */
                return;
            }

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                Log.i(TAG, "ACTION_BOND_STATE_CHANGED");

                /* Get bond (pairing) state */
                if (mBluetoothReaderManager == null) {
                    Log.w(TAG, "Unable to initialize BluetoothReaderManager.");
                    return;
                }

                bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                if (bluetoothManager == null) {
                    Log.w(TAG, "Unable to initialize BluetoothManager.");
                    return;
                }

                bluetoothAdapter = bluetoothManager.getAdapter();
                if (bluetoothAdapter == null) {
                    Log.w(TAG, "Unable to initialize BluetoothAdapter.");
                    return;
                }

                final BluetoothDevice device = bluetoothAdapter
                        .getRemoteDevice(mDeviceAddress);

                if (device == null) {
                    return;
                }

                final int bondState = device.getBondState();


                Log.i(TAG, "BroadcastReceiver - getBondState. state = "
                        + getBondingStatusString(bondState));

                /* Enable notification */
                if (bondState == BluetoothDevice.BOND_BONDED) {
                    if (mBluetoothReader != null) {
                        mBluetoothReader.enableNotification(true);
                    }
                }

                /* Progress Dialog */
                if (bondState == BluetoothDevice.BOND_BONDING) {
                    mProgressDialog = ProgressDialog.show(context,
                            "ACR3901U-S1", "Bonding...");
                } else {
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                }

                /*
                 * Update bond status and show in the connection status field.
                 */
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTxtConnectionState
                                .setText(getBondingStatusString(bondState));
                    }
                });
            }
        }

    };

    /* Clear the Card reader's response and notification fields. */
    private void clearAllUi() {
        /* Clear notification fields. */
        mTxtCardStatus.setText(R.string.noData);
        mTxtBatteryLevel.setText(R.string.noData);
        mTxtBatteryStatus.setText(R.string.noData);
        mTxtAuthentication.setText(R.string.noData);

        /* Clear card reader's response fields. */
        clearResponseUi();
    }

    /* Clear the Card reader's Response field. */
    private void clearResponseUi() {
        mTxtAuthentication.setText(R.string.noData);

        mTxtResponseApdu.setText(R.string.noData);
        mTxtEscapeResponse.setText(R.string.noData);


    }

    private void findUiViews() {
        mAuthentication = (Button) findViewById(R.id.button_Authenticate);
        mStartPolling = (Button) findViewById(R.id.button_StartPolling);

        mTransmitApdu = (Button) findViewById(R.id.button_TransmitADPU);
        mClear = (Button) findViewById(R.id.button_Clear);

        mTxtConnectionState = (TextView) findViewById(R.id.textView_ReaderState);
        mTxtCardStatus = (TextView) findViewById(R.id.textView_IccState);
        mTxtAuthentication = (TextView) findViewById(R.id.textView_Authentication);
        mTxtResponseApdu = (TextView) findViewById(R.id.textView_Response);
        mTxtEscapeResponse = (TextView) findViewById(R.id.textView_EscapeResponse);
        mTxtBatteryLevel = (TextView) findViewById(R.id.textView_BatteryLevel);
        mTxtBatteryStatus = (TextView) findViewById(R.id.textView_BatteryStatus);

        mEditMasterKey = (EditText) findViewById(R.id.editText_Master_Key);
        mEditApdu = (EditText) findViewById(R.id.editText_ADPU);
        mEditEscape = (EditText) findViewById(R.id.editText_Escape);
    }

    /*
     * Update listener
     */
    private void setListener(BluetoothReader reader) {
        /* Update status change listener */
        if (mBluetoothReader instanceof Acr3901us1Reader) {
            ((Acr3901us1Reader) mBluetoothReader)
                    .setOnBatteryStatusChangeListener(new Acr3901us1Reader.OnBatteryStatusChangeListener() {

                        @Override
                        public void onBatteryStatusChange(
                                BluetoothReader bluetoothReader,
                                final int batteryStatus) {

                            Log.i(TAG, "mBatteryStatusListener data: "
                                    + batteryStatus);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTxtBatteryStatus
                                            .setText(getBatteryStatusString(batteryStatus));
                                }
                            });
                        }

                    });
        } else if (mBluetoothReader instanceof Acr1255uj1Reader) {
            ((Acr1255uj1Reader) mBluetoothReader)
                    .setOnBatteryLevelChangeListener(new Acr1255uj1Reader.OnBatteryLevelChangeListener() {

                        @Override
                        public void onBatteryLevelChange(
                                BluetoothReader bluetoothReader,
                                final int batteryLevel) {

                            Log.i(TAG, "mBatteryLevelListener data: "
                                    + batteryLevel);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTxtBatteryLevel
                                            .setText(getBatteryLevelString(batteryLevel));
                                }
                            });
                        }

                    });
        }
        mBluetoothReader
                .setOnCardStatusChangeListener(new BluetoothReader.OnCardStatusChangeListener() {

                    @Override
                    public void onCardStatusChange(
                            BluetoothReader bluetoothReader, final int sta) {

                        Log.i(TAG, "mCardStatusListener sta: " + sta);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTxtCardStatus
                                        .setText(getCardStatusString(sta));

                                if (getCardStatusString(sta).equalsIgnoreCase("Present.")) {

                                    mTransmitApdu.performClick();

                                }
                            }
                        });
                    }

                });

        /* Wait for authentication completed. */
        mBluetoothReader
                .setOnAuthenticationCompleteListener(new BluetoothReader.OnAuthenticationCompleteListener() {

                    @Override
                    public void onAuthenticationComplete(
                            BluetoothReader bluetoothReader, final int errorCode) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (errorCode == BluetoothReader.ERROR_SUCCESS) {
                                    mTxtAuthentication
                                            .setText("Authentication Success!");
                                    mAuthentication.setEnabled(false);

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mStartPolling.performClick();

                                        }
                                    }, 500);


                                    /*new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            transmitEscapeCommend();

                                        }
                                    }, 1000);*/


                                } else {
                                    mTxtAuthentication
                                            .setText("Authentication Failed!");
                                }
                            }
                        });
                    }

                });



        /* Wait for response APDU. */
        mBluetoothReader
                .setOnResponseApduAvailableListener(new BluetoothReader.OnResponseApduAvailableListener() {

                    @Override
                    public void onResponseApduAvailable(
                            BluetoothReader bluetoothReader, final byte[] apdu,
                            final int errorCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                String fobnum = getResponseString(apdu, errorCode).replace(" ", "").trim();
                                int foblen = fobnum.length();
                                if (foblen < 5) {
                                    System.out.println("Result APDU Error" + fobnum);
                                } else {

                                    AppConstants.APDU_FOB_KEY = fobnum;
                                    mTxtResponseApdu.setText(fobnum);
                                    System.out.println("Result APDU " + fobnum);

                                    if (mDisableFOBReadingForPin.equalsIgnoreCase("N")) {
                                        AppConstants.PinLocal_FOB_KEY = fobnum;
                                    }

                                    if (mDisableFOBReadingForVehicle.equalsIgnoreCase("N")) {
                                        AppConstants.VehicleLocal_FOB_KEY = fobnum;
                                    }


                                }
                            }
                        });
                    }

                });

        /* Wait for escape command response. */
        mBluetoothReader
                .setOnEscapeResponseAvailableListener(new BluetoothReader.OnEscapeResponseAvailableListener() {

                    @Override
                    public void onEscapeResponseAvailable(
                            BluetoothReader bluetoothReader,
                            final byte[] response, final int errorCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                EmailReaderNotConnected = false;
                                AppConstants.NoSleepRespTime = CommonUtils.getTodaysDateTemp();//Date Two (d2)
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "Escape cmd recived BT reader.");
                                Log.i(TAG, "Escape cmd recived BT reader." + getResponseString(response, errorCode));
                                mTxtEscapeResponse.setText(getResponseString(response, errorCode));
                            }
                        });
                    }

                });


        mBluetoothReader
                .setOnEnableNotificationCompleteListener(new BluetoothReader.OnEnableNotificationCompleteListener() {

                    @Override
                    public void onEnableNotificationComplete(
                            BluetoothReader bluetoothReader, final int result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result != BluetoothGatt.GATT_SUCCESS) {
                                    /* Fail */
                                    Toast.makeText(
                                            WelcomeActivity.this,
                                            "The device is unable to set notification!",
                                            Toast.LENGTH_SHORT).show();
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " The device is unable to set notification!");
                                } else {

                                    String text = "The device is ready to use!";
                                    SpannableStringBuilder biggerText = new SpannableStringBuilder(text);
                                    biggerText.setSpan(new RelativeSizeSpan(2.00f), 0, text.length(), 0);
                                    Toast.makeText(WelcomeActivity.this, biggerText, Toast.LENGTH_LONG).show();
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + text);
                                }
                            }
                        });
                    }

                });
    }

    /* Set Button onClick() events. */
    private void setOnClickListener() {
        /*
         * Update onClick listener.
         */

        /* Clear UI text. */
        mClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clearResponseUi();
            }
        });

        /* Authentication function, authenticate the connected card reader. */
        mAuthentication.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mBluetoothReader == null) {
                    mTxtAuthentication.setText(R.string.card_reader_not_ready);
                    return;
                }

                /* Retrieve master key from edit box. */
                byte masterKey[] = Utils.getEditTextinHexBytes(mEditMasterKey);

                if (masterKey != null && masterKey.length > 0) {
                    /* Clear response field for the result of authentication. */
                    mTxtAuthentication.setText(R.string.noData);

                    /* Start authentication. */
                    if (!mBluetoothReader.authenticate(masterKey)) {
                        mTxtAuthentication
                                .setText(R.string.card_reader_not_ready);
                    } else {
                        mTxtAuthentication.setText("Authenticating...");
                    }
                } else {
                    mTxtAuthentication.setText("Character format error!");
                }
            }
        });

        /* Start polling card. */
        mStartPolling.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mBluetoothReader == null) {
                    //mTxtATR.setText(R.string.card_reader_not_ready);
                    return;
                }
                if (!mBluetoothReader.transmitEscapeCommand(AUTO_POLLING_START)) {
                    //mTxtATR.setText(R.string.card_reader_not_ready);
                }
            }
        });







        /* Transmit ADPU. */
        mTransmitApdu.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                /* Check for detected reader. */
                if (mBluetoothReader == null) {
                    mTxtResponseApdu.setText(R.string.card_reader_not_ready);
                    return;
                }

                /* Retrieve APDU command from edit box. */
                byte apduCommand[] = Utils.getEditTextinHexBytes(mEditApdu);

                if (apduCommand != null && apduCommand.length > 0) {
                    /* Clear response field for result of APDU. */
                    mTxtResponseApdu.setText(R.string.noData);

                    /* Transmit APDU command. */
                    if (!mBluetoothReader.transmitApdu(apduCommand)) {
                        mTxtResponseApdu
                                .setText(R.string.card_reader_not_ready);
                    }
                } else {
                    mTxtResponseApdu.setText("Character format error!");
                }
            }
        });


    }

    /* Start the process to enable the reader's notifications. */
    private void activateReader(BluetoothReader reader) {
        if (reader == null) {
            return;
        }

        if (reader instanceof Acr3901us1Reader) {
            /* Start pairing to the reader. */
            ((Acr3901us1Reader) mBluetoothReader).startBonding();
        } else if (mBluetoothReader instanceof Acr1255uj1Reader) {
            /* Enable notification. */
            mBluetoothReader.enableNotification(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reader, menu);

        menu.findItem(R.id.mreboot_reader).setVisible(false);
        menu.findItem(R.id.mreconnect_ble_readers).setVisible(false);
        menu.findItem(R.id.mcamera_back).setVisible(false);
        menu.findItem(R.id.mcamera_front).setVisible(false);
        menu.findItem(R.id.mshow_reader_status).setVisible(false);
        menu.findItem(R.id.mreload).setVisible(false);
        menu.findItem(R.id.mupgrade_normal_link).setVisible(false);
        menu.findItem(R.id.m_p_type).setVisible(true);

        if (cd.isConnectingToInternet()) {

            menu.findItem(R.id.monline).setVisible(true);
            menu.findItem(R.id.mofline).setVisible(false);

        } else {

            menu.findItem(R.id.monline).setVisible(false);
            menu.findItem(R.id.mofline).setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (item.getItemId()) {

            case R.id.mclose:
                CustomDilaogExitApp(WelcomeActivity.this, "Please enter a code to continue.", "Message");
                //String Dt = CommonUtils.getTodaysDateInStringbt();
                break;
            case R.id.mconfigure_tld:
                //TLD Service
                ConfigureTld();

                break;
            case R.id.enable_debug_window:

                CustomDilaogForDebugWindow(WelcomeActivity.this, "Please enter a code to continue.", "Message");
                break;
            case R.id.mreload:
                Toast.makeText(getApplicationContext(), "Bluetooth Settings", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);

            case R.id.mrestartapp:
                Intent i = new Intent(WelcomeActivity.this, SplashActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                break;

            case R.id.mupgrade_normal_link:
                ManualLinkUpgrade();
                break;

            case R.id.m_p_type:
                Change_P_Type();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    /* Show and hide UI resources and set the default master key and commands. */
    private void updateUi(final BluetoothReader bluetoothReader) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (bluetoothReader instanceof Acr3901us1Reader) {
                    /* The connected reader is ACR3901U-S1 reader. */
                    if (mEditMasterKey.getText().length() == 0) {
                        mEditMasterKey.setText(DEFAULT_3901_MASTER_KEY);
                    }
                    if (mEditApdu.getText().length() == 0) {
                        mEditApdu.setText(DEFAULT_3901_APDU_COMMAND);
                    }
                    if (mEditEscape.getText().length() == 0) {
                        mEditEscape.setText(DEFAULT_3901_ESCAPE_COMMAND);
                    }
                    mClear.setEnabled(true);
                    mAuthentication.setEnabled(true);
                    mStartPolling.setEnabled(false);
                    mTransmitApdu.setEnabled(true);
                    mEditMasterKey.setEnabled(true);
                    mEditApdu.setEnabled(true);
                    mEditEscape.setEnabled(true);


                } else if (bluetoothReader instanceof Acr1255uj1Reader) {
                    /* The connected reader is ACR1255U-J1 reader. */
                    if (mEditMasterKey.getText().length() == 0) {
                        try {
                            mEditMasterKey.setText(Utils
                                    .toHexString(DEFAULT_1255_MASTER_KEY
                                            .getBytes("UTF-8")));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    if (mEditApdu.getText().length() == 0) {
                        mEditApdu.setText(DEFAULT_1255_APDU_COMMAND);
                    }
                    if (mEditEscape.getText().length() == 0) {
                        mEditEscape.setText(DEFAULT_1255_ESCAPE_COMMAND);
                    }
                    mClear.setEnabled(true);
                    mAuthentication.setEnabled(true);
                    mStartPolling.setEnabled(true);
                    mTransmitApdu.setEnabled(true);
                    mEditMasterKey.setEnabled(true);
                    mEditApdu.setEnabled(true);
                    mEditEscape.setEnabled(true);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAuthentication.performClick();
                        }
                    }, 1000);
                } else {
                    mEditApdu.setText(R.string.noData);
                    mEditEscape.setText(R.string.noData);
                    mClear.setEnabled(true);
                    mAuthentication.setEnabled(false);
                    mStartPolling.setEnabled(false);
                    mTransmitApdu.setEnabled(false);
                    mEditMasterKey.setEnabled(false);
                    mEditApdu.setEnabled(false);
                    mEditEscape.setEnabled(false);
                }
            }
        });
    }

    /*
     * Create a GATT connection with the reader. And detect the connected reader
     * once service list is available.
     */
    private boolean connectReader() {

        try {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                Log.w(TAG, "Unable to initialize BluetoothManager.");
                updateConnectionState(BluetoothReader.STATE_DISCONNECTED);
                return false;
            }

            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null) {
                Log.w(TAG, "Unable to obtain a BluetoothAdapter.");
                updateConnectionState(BluetoothReader.STATE_DISCONNECTED);
                return false;
            }

            /*
             * Connect Device.
             */
            /* Clear old GATT connection. */
            if (mBluetoothGatt != null) {
                Log.i(TAG, "Clear old GATT connection");
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
                mBluetoothGatt = null;
                Log.i(TAG, "Close GATT connection");
            }

            /* Create a new connection. */
            final BluetoothDevice device = bluetoothAdapter
                    .getRemoteDevice(mDeviceAddress);

            if (device == null) {
                Log.w(TAG, "Device not found. Unable to connect.");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Device not found. Unable to connect.");
                Toast.makeText(WelcomeActivity.this, "Device not found. Unable to connect.", Toast.LENGTH_SHORT).show();
                return false;
            }

            /* Connect to GATT server. */
            updateConnectionState(BluetoothReader.STATE_CONNECTING);
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        } catch (Exception e) {
            Log.i(TAG, "ACS reader Exception:" + e.toString());
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "ACS reader Exception:" + e.toString());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /* Disconnects an established connection. */
    private void disconnectReader() {
        if (mBluetoothGatt == null) {
            updateConnectionState(BluetoothReader.STATE_DISCONNECTED);
            return;
        }
        updateConnectionState(BluetoothReader.STATE_DISCONNECTING);
        mBluetoothGatt.disconnect();
    }


    /* Update the display of Connection status string. */
    private void updateConnectionState(final int connectState) {

        mConnectState = connectState;

        if (connectState == BluetoothReader.STATE_CONNECTING) {
            mTxtConnectionState.setText(R.string.connecting);
        } else if (connectState == BluetoothReader.STATE_CONNECTED) {
            //   if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " HF BT reader connected");
            mTxtConnectionState.setText(R.string.connected);
        } else if (connectState == BluetoothReader.STATE_DISCONNECTING) {
            mTxtConnectionState.setText(R.string.disconnecting);
        } else {
            mTxtConnectionState.setText(R.string.disconnected);
            clearAllUi();
            updateUi(null);
        }
        invalidateOptionsMenu();
    }


    /* Get the Bonding status string. */
    private String getBondingStatusString(int bondingStatus) {
        if (bondingStatus == BluetoothDevice.BOND_BONDED) {
            return "BOND BONDED";
        } else if (bondingStatus == BluetoothDevice.BOND_NONE) {
            return "BOND NONE";
        } else if (bondingStatus == BluetoothDevice.BOND_BONDING) {
            return "BOND BONDING";
        }
        return "BOND UNKNOWN.";
    }

    /* Get the Battery level string. */
    private String getBatteryLevelString(int batteryLevel) {
        if (batteryLevel < 0 || batteryLevel > 100) {
            return "Unknown.";
        }
        return String.valueOf(batteryLevel) + "%";
    }

    /* Get the Battery status string. */
    private String getBatteryStatusString(int batteryStatus) {
        if (batteryStatus == BluetoothReader.BATTERY_STATUS_NONE) {
            return "No battery.";
        } else if (batteryStatus == BluetoothReader.BATTERY_STATUS_FULL) {
            return "The battery is full.";
        } else if (batteryStatus == BluetoothReader.BATTERY_STATUS_USB_PLUGGED) {
            return "The USB is plugged.";
        }
        return "The battery is low.";
    }

    /* Get the Card status string. */
    private String getCardStatusString(int cardStatus) {
        if (cardStatus == BluetoothReader.CARD_STATUS_ABSENT) {
            return "Absent.";
        } else if (cardStatus == BluetoothReader.CARD_STATUS_PRESENT) {
            return "Present.";
        } else if (cardStatus == BluetoothReader.CARD_STATUS_POWERED) {
            return "Powered.";
        } else if (cardStatus == BluetoothReader.CARD_STATUS_POWER_SAVING_MODE) {
            return "Power saving mode.";
        }
        return "The card status is unknown.";
    }

    /* Get the Error string. */
    private String getErrorString(int errorCode) {
        if (errorCode == BluetoothReader.ERROR_SUCCESS) {
            return "";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_CHECKSUM) {
            return "The checksum is invalid.";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_DATA_LENGTH) {
            return "The data length is invalid.";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_COMMAND) {
            return "The command is invalid.";
        } else if (errorCode == BluetoothReader.ERROR_UNKNOWN_COMMAND_ID) {
            return "The command ID is unknown.";
        } else if (errorCode == BluetoothReader.ERROR_CARD_OPERATION) {
            return "The card operation failed.";
        } else if (errorCode == BluetoothReader.ERROR_AUTHENTICATION_REQUIRED) {
            return "Authentication is required.";
        } else if (errorCode == BluetoothReader.ERROR_LOW_BATTERY) {
            return "The battery is low.";
        } else if (errorCode == BluetoothReader.ERROR_CHARACTERISTIC_NOT_FOUND) {
            return "Error characteristic is not found.";
        } else if (errorCode == BluetoothReader.ERROR_WRITE_DATA) {
            return "Write command to reader is failed.";
        } else if (errorCode == BluetoothReader.ERROR_TIMEOUT) {
            return "Timeout.";
        } else if (errorCode == BluetoothReader.ERROR_AUTHENTICATION_FAILED) {
            return "Authentication is failed.";
        } else if (errorCode == BluetoothReader.ERROR_UNDEFINED) {
            return "Undefined error.";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_DATA) {
            return "Received data error.";
        } /*else if (errorCode == BluetoothReader.ERROR_COMMAND_FAILED)
        {
            return "The command failed.";
        }*/
        return "Unknown error.";
    }

    /* Get the Response string. */
    private String getResponseString(byte[] response, int errorCode) {
        if (errorCode == BluetoothReader.ERROR_SUCCESS) {
            if (response != null && response.length > 0) {
                return Utils.toHexString(response);
            }
            return "";
        }
        return getErrorString(errorCode);
    }

    public class CommandsGET extends AsyncTask<String, Void, String> {

        public String resp = "";


        protected String doInBackground(String... param) {


            try {

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(15, TimeUnit.SECONDS);
                client.setReadTimeout(15, TimeUnit.SECONDS);
                client.setWriteTimeout(15, TimeUnit.SECONDS);

                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            try {

                consoleString += "OUTPUT- " + result + "\n";

                System.out.println(result);

            } catch (Exception e) {

                System.out.println(e);
            }
        }
    }

    public void transmitEscapeCommend() {

        /* Check for detected reader. */
        if (mBluetoothReader == null) {
            System.out.println("card_reader_not_ready");
            return;
        }

        /* Retrieve escape command from edit box. */
        byte escapeCommand[] = CommonUtils.toByteArray(DEFAULT_1255_ESCAPE_COMMAND);

        if (escapeCommand != null && escapeCommand.length > 0) {
            /* Clear response field for result of escape command. */
            // System.out.println("No Sleep EscapeCommand");

            /* Transmit escape command. */
            if (!mBluetoothReader.transmitEscapeCommand(escapeCommand)) {
                System.out.println("card_reader_not_ready");
            }
        } else {
            System.out.println("Character format error!");
        }

    }

    /*public void DownloadFirmwareFile() {

        //File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "FSBin");
        File folder = new File(String.valueOf(getApplicationContext().getExternalFilesDir(AppConstants.FOLDER_BIN)));
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
            // Do something on success
        } else {
            AppConstants.AlertDialogBox(WelcomeActivity.this, "Please check File is present in FSBin Folder in Internal(Device) Storage");
        }

        if (AppConstants.UP_FilePath != null)
            new DownloadFileFromURL().execute(String.valueOf(getApplicationContext().getExternalFilesDir(AppConstants.FOLDER_BIN)), AppConstants.UP_Upgrade_File_name);

    }*/

    /*class DownloadFileFromURL extends AsyncTask<String, String, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage("Software update in progress.\nPlease wait several seconds....");
            pd.setCancelable(false);
            pd.show();
        }


        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                // getting file length
                int lenghtOfFile = conection.getContentLength();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file
                OutputStream output = new FileOutputStream(FOLDER_PATH + f_url[1]);

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
            //pDialog.setProgress(Integer.parseInt(progress[0]));
        }


        @Override
        protected void onPostExecute(String file_url) {
            pd.dismiss();
        }

    }*/

    public void ReConnectBTReader() {

        if (mConnectState == BluetoothReader.STATE_DISCONNECTED) {

            //If Reader Disconnected try to connection attempt.
            if (ConnectCount < 1) {

                ConnectCount += 1;
                System.out.println("Count: " + ConnectCount);

                if (mDeviceName != null && !mDeviceAddress.isEmpty() && AppConstants.ACS_READER) {
                    connectReader();
                }
            }
        }
    }

    public void NoSleepSchedulerTimer() {
        TimerTask repeatedTask = new TimerTask() {
            public void run() {
                if (mDeviceName != null && mDeviceAddress.contains(":") && AppConstants.ACS_READER) {
                    System.out.println("NoSleepAsyncTask performed on " + new Date());
                    new NoSleepAsyncTask().execute();
                } else {
                    Log.i(TAG, "NoSleepAsyncTask skip Check DeviceName & DeviceAddress\n ACS Reader Status" + AppConstants.ACS_READER);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "NoSleepAsyncTask skip Check DeviceName & DeviceAddress\n ACS Reader Status" + AppConstants.ACS_READER);
                }
            }
        };


        long delay = 60000L;
        long period = 60000L;
        timerNoSleep.scheduleAtFixedRate(repeatedTask, delay, period);
    }

    public void checkEachFSNPDateTime() {


        if (AppConstants.DetailsServerSSIDList != null & AppConstants.DetailsServerSSIDList.size() > 0) {

            for (int p = 0; p < AppConstants.DetailsServerSSIDList.size(); p++) {

                String commaFsnpLink = AppConstants.DetailsServerSSIDList.get(p).get("FSNPMacAddress");

                commaFsnpLink = commaFsnpLink.trim().toUpperCase();

                if (commaFsnpLink.contains(",")) {
                    String macs[] = commaFsnpLink.split(",");

                    if (macs.length > 0) {

                        for (int i = 0; i < macs.length; i++) {

                            String singleMac = macs[i];

                            singleMac = singleMac.trim();

                            checkLinkFSNPwithEddystone(p, singleMac);

                        }
                    }

                } else {
                    //if without comma
                    checkLinkFSNPwithEddystone(p, commaFsnpLink);
                }
            }

            System.out.println("diffSeconds ---------------------------");
        }

    }


    public void checkLinkFSNPwithEddystone(int linkIndex, String singleMac) {

        if (lastFSNPDate.containsKey(singleMac)) {

            Date curdate = new Date();

            Date fsnpDate = lastFSNPDate.get(singleMac);

            if (fsnpDate != null) {


                long diff = curdate.getTime() - fsnpDate.getTime();

                long diffSeconds = diff / 1000 % 60;
                long diffMinutes = diff / (60 * 1000) % 60;
                long diffHours = diff / (60 * 60 * 1000) % 24;
                //long diffDays = diff / (24 * 60 * 60 * 1000);

                System.out.println(singleMac + " fsnp - diffSeconds- " + diffSeconds);


                if (diffSeconds > 15 || diffMinutes > 0 || diffHours > 0) {

                    lastFSNPDate.remove(singleMac);

                    switch (linkIndex) {
                        case 0:
                            if (Constants.FS_1STATUS.equalsIgnoreCase("BUSY")) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        button1ClickCode();
                                    }
                                });

                            }
                            break;

                        case 1:
                            if (Constants.FS_2STATUS.equalsIgnoreCase("BUSY")) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        button2ClickCode();
                                    }
                                });
                            }
                            break;

                        case 2:
                            if (Constants.FS_3STATUS.equalsIgnoreCase("BUSY")) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        button3ClickCode();
                                    }
                                });
                            }
                            break;

                        case 3:
                            if (Constants.FS_4STATUS.equalsIgnoreCase("BUSY")) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        button4ClickCode();
                                    }
                                });
                            }
                            break;

                        case 4:
                            if (Constants.FS_5STATUS.equalsIgnoreCase("BUSY")) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        button5ClickCode();
                                    }
                                });
                            }
                            break;

                        case 5:
                            if (Constants.FS_6STATUS.equalsIgnoreCase("BUSY")) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        button6ClickCode();
                                    }
                                });
                            }
                            break;

                    }
                }

            }
        }
    }


    public void timerForFSNPnoResponse() {
        TimerTask repeatedTask = new TimerTask() {
            public void run() {

                try {
                    System.out.println("FSNP****************");


                    if (lastFSNPDate != null) {

                        checkEachFSNPDateTime();
                    }


                } catch (Exception e) {
                }

            }
        };


        long delay = 2000L;
        long period = 5000L;
        timerFSNP.scheduleAtFixedRate(repeatedTask, delay, period);
    }

    public void CheckForGateSoftwareTimer() {

        timerGate = new Timer("TimerGate");
        TimerTask repeatedTask = new TimerTask() {
            public void run() {
                System.out.println("Gate timer performed on " + new Date());
                Constants.FA_Message = "";//Cleare FA message
                //AutoSelect if single hose
                if (IsGateHub.equalsIgnoreCase("True")) {

                    cancelThinDownloadManager();

                    flagGoBtn = true;
                    try {

                        if (serverSSIDList != null && serverSSIDList.size() == 1 && IsGateHub.equalsIgnoreCase("True") && Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE")) {

                            String LinkCommunicationType = serverSSIDList.get(0).get("LinkCommunicationType");

                            if (LinkCommunicationType.equalsIgnoreCase("HTTP")) {

                                String selSSID = serverSSIDList.get(0).get("WifiSSId");
                                String selMacAddress = serverSSIDList.get(0).get("MacAddress");
                                String HTTP_URL = "";
                                String IpAddress = "";

                                boolean isMacConnected = false;
                                if (AppConstants.DetailsListOfConnectedDevices != null) {
                                    for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                        String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");

                                        if (selMacAddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                                            if (AppConstants.GenerateLogs)
                                                AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + selMacAddress + ") is connected to hotspot.");
                                            IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                                            isMacConnected = true;
                                            break;
                                        }
                                    }
                                }

                                if (!isMacConnected) {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + selMacAddress + ") is not found in connected devices. " + AppConstants.DetailsListOfConnectedDevices);
                                    for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                        String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + "Checking Mac Address using info command: (" + MA_ConnectedDevices + ")");

                                        String connectedIp = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");

                                        IpAddress = GetAndCheckMacAddressFromInfoCommand(connectedIp, selMacAddress, MA_ConnectedDevices);
                                        if (!IpAddress.trim().isEmpty()) {
                                            if (AppConstants.GenerateLogs)
                                                AppConstants.WriteinFile("===================================================================");
                                            break;
                                        }
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile("===================================================================");
                                    }
                                }

                                if (!IpAddress.trim().isEmpty()) {
                                    HTTP_URL = "http://" + IpAddress + ":80/";
                                }

                                AppConstants.SITE_ID = serverSSIDList.get(0).get("SiteId");

                                GateHubStartTransaction(HTTP_URL);
                                new GetSSIDUsingLocationGateHub().execute();//temp to solve crash issue

                            } else if (LinkCommunicationType.equalsIgnoreCase("BT")) {
                                new GetSSIDUsingLocationGateHub().execute();
                            }
                        } else {
                            new GetSSIDUsingLocationGateHub().execute();//temp to solve crash issue
                        }

                    } catch (Exception e) {
                        System.out.println(e);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " AutoSelect if single hose --Exception " + e);
                    }

                } else if (serverSSIDList != null && serverSSIDList.size() == 1 && Constants.FS_1STATUS.equalsIgnoreCase("FREE")) {

                    cancelThinDownloadManager();
                    try {
                        AppConstants.IsSingleLink = true;
                        String SSID_mac = serverSSIDList.get(0).get("MacAddress");
                        String ReconfigureLink = serverSSIDList.get(0).get("ReconfigureLink");
                        AppConstants.SITE_ID = serverSSIDList.get(0).get("SiteId");
                        String LinkCommunicationType = serverSSIDList.get(0).get("LinkCommunicationType");
                        String BTselMacAddress = serverSSIDList.get(0).get("BTMacAddress");
                        String selSSID = serverSSIDList.get(0).get("WifiSSId");
                        if (ReconfigureLink == null) {
                            ReconfigureLink = "";
                        }

                        if (LinkCommunicationType.equalsIgnoreCase("BT") && !ReconfigureLink.equalsIgnoreCase("true")) {
                            //tvSSIDName.setText("Tap here to select hose");
                            //btnGo.setVisibility(View.VISIBLE);
                            SingleBTLinkSelection(selSSID, BTselMacAddress);

                        } else {
                            String Chk_ip = "";
                            if (AppConstants.DetailsListOfConnectedDevices != null && AppConstants.DetailsListOfConnectedDevices.size() > 0) {
                                Chk_ip = AppConstants.DetailsListOfConnectedDevices.get(0).get("ipAddress");
                            } else {
                                getipOverOSVersion();
                            }

                            if (Chk_ip != null && Chk_ip.length() > 3 && !ReconfigureLink.equalsIgnoreCase("true")) {

                                boolean isMacConnected = false;
                                if (AppConstants.DetailsListOfConnectedDevices != null) {
                                    for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                        String Chk_mac = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");

                                        if (SSID_mac.equalsIgnoreCase(Chk_mac)) {
                                            if (AppConstants.GenerateLogs)
                                                AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + SSID_mac + ") is connected to hotspot.");
                                            IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                                            isMacConnected = true;
                                            break;
                                        }
                                    }
                                }

                                if (!isMacConnected) {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + SSID_mac + ") is not found in connected devices. " + AppConstants.DetailsListOfConnectedDevices);

                                    for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                        String Chk_mac = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + "Checking Mac Address using info command: (" + Chk_mac + ")");

                                        String connectedIp = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");

                                        IpAddress = GetAndCheckMacAddressFromInfoCommand(connectedIp, SSID_mac, Chk_mac);
                                        if (!IpAddress.trim().isEmpty()) {
                                            if (AppConstants.GenerateLogs)
                                                AppConstants.WriteinFile("===================================================================");
                                            break;
                                        }
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile("===================================================================");
                                    }
                                }

                                if (!IpAddress.trim().isEmpty()) {
                                    SelectedItemPos = 0;
                                    tvSSIDName.setText(serverSSIDList.get(0).get("WifiSSId"));
                                    OnHoseSelected_OnClick(Integer.toString(0));
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Auto select fail", Toast.LENGTH_SHORT).show();
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "Auto select fail");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    new GetSSIDUsingLocationGateHub().execute();//temp to solve crash issue
                }
            }
        };


        long delay = 500L;
        long period = 10000L;
        if (timerGate != null)
            timerGate.scheduleAtFixedRate(repeatedTask, delay, period);
    }


    public class NoSleepAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void[] objects) {

            NoSleepEscapeCommandBackground();

            return null;
        }
    }


    public void NoSleepEscapeCommandBackground() {


        try {

            if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE")) {

                transmitEscapeCommend();//No Sleep command

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        AppConstants.NoSleepCurrentTime = CommonUtils.getTodaysDateTemp();

                        if (AppConstants.NoSleepRespTime.equalsIgnoreCase("")) {
                            Log.i(TAG, "Please check if HF reader is connected");
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " Please check if HF reader is connected");

                            if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE") && Constants.FS_5STATUS.equalsIgnoreCase("FREE") && Constants.FS_6STATUS.equalsIgnoreCase("FREE")) {

                                if (mDeviceName != null && mDeviceAddress.contains(":")) {

                                    //-------------------
                                    //Disable BT------------
                                    final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                    mBluetoothAdapter.disable();
                                    Log.i(TAG, "BT OFF");
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " BT OFF");
                                    disconnectReader();

                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            //Enable BT------------
                                            mBluetoothAdapter.enable();
                                            Log.i(TAG, "BT ON");
                                            if (AppConstants.GenerateLogs)
                                                AppConstants.WriteinFile(TAG + " BT ON");
                                        }
                                    }, 4000);

                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            //-------------------
                                            Log.i(TAG, "recreate called first case");
                                            if (AppConstants.GenerateLogs)
                                                AppConstants.WriteinFile(TAG + " recreate called first case");

                                            recreate();
                                        }
                                    }, 6000);


                                } else {
                                    Log.i(TAG, "Please check DeviceName & DeviceAddress");
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " Please check DeviceName & DeviceAddress");
                                }

                            } else {
                                Log.i(TAG, "Hose busy..can not recreate");
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "Hose busy..can not recreate");
                            }

                            /*if (mDeviceName != null && mDeviceAddress.contains(":")) {
                                //Disable BT------------
                                final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                mBluetoothAdapter.disable();
                                Log.i(TAG, "BT OFF");
                                //   if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " BT OFF");
                                disconnectReader();

                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Enable BT------------
                                        mBluetoothAdapter.enable();
                                        Log.i(TAG, "BT ON");
                                        // if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " BT ON");
                                    }
                                }, 4000);

                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        //Connect Reader----------
                                        connectReader();
                                    }
                                }, 6000);

                            } else {

                                Log.i(TAG, "Please check DeviceName & DeviceAddress");
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " Please check DeviceName & DeviceAddress");

                            }*/

                        } else {

                            int diff = getDate(AppConstants.NoSleepCurrentTime);
                            if (diff >= 5) {//10
                                //Grater than 15 min no response from HF reader
                                //Send Email
                                Log.i(TAG, "HF reader response time diff is: " + diff);

                                if (EmailReaderNotConnected) {
                                    Log.i(TAG, "Email already sent");
                                    //Connect Reader
                                    if (mDeviceName != null && !mDeviceAddress.isEmpty() && AppConstants.ACS_READER) {
                                        connectReader();
                                    }
                                } else {
                                    EmailReaderNotConnected = true;
                                    Log.i(TAG, "Send Email");
                                    SendEmailReaderNotConnectedAsyncCall();
                                }

                            } else if (diff >= 2) {//5
                                //Grater than 10 min no response from HF reader
                                //Recreate main activity
                                Log.i(TAG, "HF reader response time diff is: " + diff);

                                if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE") && Constants.FS_5STATUS.equalsIgnoreCase("FREE") && Constants.FS_6STATUS.equalsIgnoreCase("FREE")) {

                                    if (mDeviceName != null && mDeviceAddress.contains(":")) {


                                        //Disable BT------------
                                        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                        mBluetoothAdapter.disable();
                                        Log.i(TAG, "BT OFF");
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + " BT OFF");
                                        disconnectReader();

                                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                //Enable BT------------
                                                mBluetoothAdapter.enable();
                                                Log.i(TAG, "BT ON");
                                                if (AppConstants.GenerateLogs)
                                                    AppConstants.WriteinFile(TAG + " BT ON");
                                            }
                                        }, 4000);

                                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                //-------------------
                                                Log.i(TAG, "recreate called second case");
                                                if (AppConstants.GenerateLogs)
                                                    AppConstants.WriteinFile(TAG + " recreate called second case");

                                                recreate();
                                            }
                                        }, 6000);

                                    } else {
                                        Log.i(TAG, "Please check DeviceName & DeviceAddress");
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + " Please check DeviceName & DeviceAddress");
                                    }

                                } else {
                                    Log.i(TAG, "Hose busy..can not recreate");
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "Hose busy..can not recreate");
                                }




                                /*if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " Retry attempt 2 reader connect");
                                //Disable BT
                                final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                mBluetoothAdapter.disable();
                                Log.i(TAG, "BT OFF");
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BT OFF");
                                //   if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " BT OFF");
                                disconnectReader();
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " disconnectReader()");

                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Enable BT
                                        mBluetoothAdapter.enable();
                                        Log.i(TAG, "BT ON");
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + " BT ON");
                                        //      if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " BT ON");
                                    }
                                }, 4000);

                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        //Connect Reader
                                        connectReader();
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + " connectReader()");
                                    }
                                }, 6000);*/


                            } else if (diff >= 1) {//1
                                //Grater than 5 min no response from HF reader
                                //Recreate main activity
                                Log.i(TAG, "HF reader response time diff is: " + diff);
                                Log.i(TAG, "Retry attempt 1 reader connect");

                                //Disable BT
                                final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                mBluetoothAdapter.disable();
                                Log.i(TAG, "BT OFF");
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " BT OFF");
                                disconnectReader();
                                Log.i(TAG, "disconnectReader()");
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " disconnectReader()");

                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Enable BT
                                        mBluetoothAdapter.enable();
                                        Log.i(TAG, "BT ON");
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + " BT ON");
                                    }
                                }, 2000);

                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        //Connect Reader
                                        if (mDeviceName != null && !mDeviceAddress.isEmpty() && AppConstants.ACS_READER) {
                                            connectReader();
                                        }
                                        Log.i(TAG, "connectReader()");
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + " connectReader()");
                                    }
                                }, 4000);


                            } else {
                                Log.i(TAG, "HF reader is working fine");
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " HF reader is working fine");
                            }
                        }

                    }
                }, 3000);
            }


        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " NoSleepEscapeCommand Exception: " + e);
        }


    }

    public int getDate(String CurrentTime) {

        int DiffTime = 0;
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date1 = sdf.parse(CurrentTime);
            date2 = sdf.parse(AppConstants.NoSleepRespTime);

            long diff = date1.getTime() - date2.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            DiffTime = (int) minutes;
            //System.out.println("~~~Difference~~~" + minutes);

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (NullPointerException n) {
            n.printStackTrace();
        }

        return DiffTime;
    }

    public void SendEmailReaderNotConnectedAsyncCall() {

        UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetails(WelcomeActivity.this);

        StatusForUpgradeVersionEntity objEntityClass2 = new StatusForUpgradeVersionEntity();
        objEntityClass2.IMEIUDID = AppConstants.getIMEI(WelcomeActivity.this);
        //objEntityClass2.Email = CommonUtils.getCustomerDetails(WelcomeActivity.this).PersonEmail;
        objEntityClass2.HubName = userInfoEntity.PersonName;
        objEntityClass2.SiteName = userInfoEntity.FluidSecureSiteName;

        Gson gson = new Gson();
        String parm2 = gson.toJson(objEntityClass2);

        String userEmail = CommonUtils.getCustomerDetails(WelcomeActivity.this).PersonEmail;
        //----------------------------------------------------------------------------------
        String parm1 = AppConstants.getIMEI(WelcomeActivity.this) + ":" + userEmail + ":" + "DefectiveBluetoothInfoEmail";
        String authString = "Basic " + AppConstants.convertStingToBase64(parm1);


        RequestBody body = RequestBody.create(TEXT, parm2);
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(AppConstants.webURL)
                .post(body)
                .addHeader("Authorization", authString)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "error in getting response");
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(Response response) throws IOException {

                ResponseBody responseBody = response.body();
                if (!response.isSuccessful()) {
                    throw new IOException("Error response " + response);
                } else {

                    String result = responseBody.string();
                    System.out.println("Result" + result);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " SendEmailReaderNotConnectedAsyncCall ~Result\n" + result);

                    try {

                        JSONObject jsonObjectSite = null;
                        jsonObjectSite = new JSONObject(result);

                        String ResponseMessageSite = jsonObjectSite.getString(AppConstants.RES_MESSAGE);

                        if (ResponseMessageSite.equalsIgnoreCase("success")) {
                            //     if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " SendEmailReaderNotConnectedAsyncCall ~success");
                            EmailReaderNotConnected = true;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

        });
    }

    public void UpdateServerMessages() {

        /*if (Fa_log.getLineCount() > 8){
            AppConstants.LOG_FluidSecure_Auto = "";
        }
        Fa_log.setText(AppConstants.LOG_FluidSecure_Auto);*/

        tv_Display_msg.setText(AppConstants.Server_mesage);
        tv_request.setText(AppConstants.Header_data + "\n\n" + AppConstants.Server_Request);
        tv_response.setText(AppConstants.Server_Response);


    }

    public static void DownloadFile() {
        File fileFsvm = new File(Environment.getExternalStorageDirectory() + "/www/FSVM/");
        File fileFsnp = new File(Environment.getExternalStorageDirectory() + "/www/FSNP/");

        if (!fileFsvm.exists()) {
            if (fileFsvm.mkdirs()) {
                System.out.println("Create FSVM");
            } else {
                System.out.println("Fail to create FSVM folder");
            }
        }

        if (!fileFsnp.exists()) {
            if (fileFsnp.mkdirs()) {
                System.out.println("Create FSNP");
            } else {
                System.out.println("Fail to create FSNP folder");
            }
        }
    }

    public static void setUrlFromSharedPref(Context ctx) {

        SharedPreferences sharedPref = ctx.getSharedPreferences("storeAppTxtURL", Context.MODE_PRIVATE);
        String appLink = sharedPref.getString("appLink", "");
        if (appLink.trim().contains("http")) {

            AppConstants.webIP = appLink.trim();
            AppConstants.webURL = AppConstants.webIP + "HandlerTrak.ashx";
            AppConstants.LoginURL = AppConstants.webIP + "LoginHandler.ashx";

        }
    }

    public static void WakeUpScreen() {

        //Enable Screen
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock screenLock = ((PowerManager) ctx.getSystemService(POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
        screenLock.acquire();

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void turnOnHotspot() {
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

                @Override
                public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                    super.onStarted(reservation);
                    Log.d(TAG, "Wifi Hotspot is on now");
                    mReservation = reservation;


                }

                @Override
                public void onStopped() {
                    super.onStopped();
                    Log.d(TAG, "onStopped: ");
                }

                @Override
                public void onFailed(int reason) {
                    super.onFailed(reason);
                    Log.d(TAG, "onFailed: ");
                }
            }, new Handler());
        }
    }


    private void turnOffHotspot() {
        if (mReservation != null) {
            mReservation.close();
        }
    }

    public void IsLogRequiredAndBranding() {

        SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_Log_Data, Context.MODE_PRIVATE);
        AppConstants.GenerateLogs = Boolean.parseBoolean(sharedPref.getString(AppConstants.LogRequiredFlag, "True"));
        String CompanyBrandName = sharedPref.getString(AppConstants.CompanyBrandName, "FluidSecure");
        String CompanyBrandLogoLink = sharedPref.getString(AppConstants.CompanyBrandLogoLink, "");
        String SupportEmail = sharedPref.getString(AppConstants.SupportEmail, "");
        String SupportPhonenumber = sharedPref.getString(AppConstants.SupportPhonenumber, "");

        AppConstants.BrandName = CompanyBrandName;
        support_email.setText(SupportEmail);
        support_phone.setText(SupportPhonenumber);

        getSupportActionBar().setTitle(AppConstants.BrandName);
        getSupportActionBar().setIcon(R.drawable.fuel_secure_lock);

        if (!CompanyBrandLogoLink.equalsIgnoreCase("")) {
            Picasso.get().load(CompanyBrandLogoLink).into((ImageView) findViewById(R.id.FSlogo_img));
        }

    }

    public void IsFARequired() {

        AppConstants.DownloadFileHttpServer = "";

        SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_FA_Data, Context.MODE_PRIVATE);
        AppConstants.EnableFA = sharedPref.getBoolean(AppConstants.FAData, false);
        AppConstants.EnableServerForTLD = sharedPref.getBoolean(AppConstants.IsEnableServerForTLD, false);

        if (AppConstants.EnableFA) {

            //if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + " FA Enabled");
            Log.e(TAG, " FA Enabled");
            AppConstants.EnableFA = true;


            //TODO EddystoneScannerService
            if (checkBluetoothStatus()) {

                Intent intent = new Intent(this, EddystoneScannerService.class);
                bindService(intent, this, BIND_AUTO_CREATE);
                mHandler.post(mPruneTask);

            } else {

                //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "Failed to start EddystoneScannerService Scanning");
                Log.e(TAG, " Failed to start EddystoneScannerService Scanning");

            }

            //TODO MyServer FSVM
            //if (AppConstants.Server_mesage.equalsIgnoreCase("Server Not Connected..!!!")){}

            try {

                server = new MyServer();
                //DownloadFileHttp.ctx= WelcomeActivity.this;
                DownloadFileHttp abc = new DownloadFileHttp();

            } catch (Exception e) {
                e.printStackTrace();
                //if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + " MyServer Ex-" + e);
            }


        } else if (AppConstants.EnableServerForTLD) {


            //TODO MyServer FSVM
            //if (AppConstants.Server_mesage.equalsIgnoreCase("Server Not Connected..!!!")){}
            ctx = WelcomeActivity.this;
            try {

                server = new MyServer();
                //DownloadFileHttp.ctx= WelcomeActivity.this;
                DownloadFileHttp abc = new DownloadFileHttp();

            } catch (Exception e) {
                e.printStackTrace();
                //if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + " MyServer Ex-" + e);
            }


        } else {


            AppConstants.EnableFA = false;
        }

    }

    public void GateHubStartTransaction(String HTTP_URL) {


        URL_GET_TXNID = HTTP_URL + "client?command=lasttxtnid";
        URL_SET_TXNID = HTTP_URL + "config?command=txtnid";
        URL_GET_PULSAR = HTTP_URL + "client?command=pulsar ";
        URL_RECORD10_PULSAR = HTTP_URL + "client?command=record10";
        URL_INFO = HTTP_URL + "client?command=info";
        URL_RELAY = HTTP_URL + "config?command=relay";
        PulserTimingAd = HTTP_URL + "config?command=pulsar";
        URL_SET_PULSAR = HTTP_URL + "config?command=pulsar";
        iot_version = "";


        try {

            //Info command commented
            String FSStatus = new CommandsGET().execute(URL_INFO).get();
            Log.e("GateSoftwareDelayIssue", " Info command ");
            if (FSStatus.startsWith("{") && FSStatus.contains("Version")) {

                try {

                    JSONObject jsonObj = new JSONObject(FSStatus);
                    String userData = jsonObj.getString("Version");
                    JSONObject jsonObject = new JSONObject(userData);

                    String sdk_version = jsonObject.getString("sdk_version");
                    String mac_address = jsonObject.getString("mac_address");
                    iot_version = jsonObject.getString("iot_version");

                    //Store Hose ID and Firmware version in sharedpreferance
                    SharedPreferences sharedPref = WelcomeActivity.this.getSharedPreferences(Constants.PREF_FS_UPGRADE, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("hoseid_fs1", AppConstants.UP_HoseId_fs1);
                    editor.putString("fsversion_fs1", iot_version);
                    editor.commit();

                    //IF upgrade firmware true check below
                    if (AppConstants.UP_Upgrade) {
                        CheckForUpdateFirmware(AppConstants.UP_HoseId_fs1, iot_version, AppConstants.FS_selected);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                //Info command else commented
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " GateHubStartTransaction: info command response is empty.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void CheckForUpdateFirmware(final String hoseid, String iot_version,
                                       final String FS_selected) {

        SharedPreferences sharedPrefODO = this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String HubId = sharedPrefODO.getString(AppConstants.HubId, "");// HubId equals to personId

        //First call which will Update Fs firmware to Server--
        final UpgradeVersionEntity objEntityClass = new UpgradeVersionEntity();
        objEntityClass.IMEIUDID = AppConstants.getIMEI(this);
        objEntityClass.Email = CommonUtils.getCustomerDetails(this).PersonEmail;
        objEntityClass.HoseId = hoseid;
        objEntityClass.Version = iot_version;

        if (hoseid != null && !hoseid.trim().isEmpty()) {

            UpgradeCurrentVersionWithUgradableVersion objUP = new UpgradeCurrentVersionWithUgradableVersion(objEntityClass);
            objUP.execute();
            System.out.println(objUP.response);

            try {
                JSONObject jsonObject = new JSONObject(objUP.response);
                String ResponceMessage = jsonObject.getString("ResponceMessage");
                String ResponceText = jsonObject.getString("ResponceText");
                Log.e("GateSoftwareDelayIssue", "   CheckForUpdateFirmware ResponceMessage" + ResponceMessage);
                if (ResponceMessage.equalsIgnoreCase("success")) {
                }

            } catch (Exception e) {

            }
        }

        //Second call will get Status for firwareupdate
        StatusForUpgradeVersionEntity objEntityClass1 = new StatusForUpgradeVersionEntity();
        objEntityClass1.IMEIUDID = AppConstants.getIMEI(this);
        objEntityClass1.Email = CommonUtils.getCustomerDetails(this).PersonEmail;
        objEntityClass1.HoseId = hoseid;
        objEntityClass1.PersonId = HubId;

        Gson gson = new Gson();
        String jsonData = gson.toJson(objEntityClass1);

        String userEmail = CommonUtils.getCustomerDetails(this).PersonEmail;
        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(this) + ":" + userEmail + ":" + "IsUpgradeCurrentVersionWithUgradableVersion");


        new GetUpgrateFirmwareStatus().execute(FS_selected, jsonData, authString);

    }

    public class GetUpgrateFirmwareStatus extends AsyncTask<String, Void, String> {

        String FS_selected;
        String jsonData;
        String authString;


        @Override
        protected String doInBackground(String... params) {

            Log.e("GateSoftwareDelayIssue", "   GetUpgrateFirmwareStatus doInBackground");
            String response = "";
            try {

                FS_selected = params[0];
                jsonData = params[1];
                authString = params[2];

                System.out.println("jsonData--" + jsonData);
                System.out.println("authString--" + authString);


                response = serverHandler.PostTextData(WelcomeActivity.this, AppConstants.webURL, jsonData, authString);

                System.out.println("Id..." + jsonData);

            } catch (Exception e) {
                System.out.println(e);
            }

            return response;
        }

        @Override
        protected void onPostExecute(String resp) {

            System.out.println("resp..." + resp);
            Log.e("GateSoftwareDelayIssue", "   GetUpgrateFirmwareStatus onPostExecute");
            try {
                JSONObject jsonObj = new JSONObject(resp);

                String ResponceMessage = jsonObj.getString(AppConstants.RES_MESSAGE);
                String ResponceText = jsonObj.getString(AppConstants.RES_TEXT);

                if (ResponceMessage.equalsIgnoreCase("success")) {

                    //--------------------------------------------
                    if (FS_selected.equalsIgnoreCase("0")) {

                        if (ResponceText.trim().equalsIgnoreCase("Y"))
                            AppConstants.UP_Upgrade_fs1 = true;
                        else
                            AppConstants.UP_Upgrade_fs1 = false;

                    } else if (FS_selected.equalsIgnoreCase("1")) {

                        if (ResponceText.trim().equalsIgnoreCase("Y"))
                            AppConstants.UP_Upgrade_fs2 = true;
                        else
                            AppConstants.UP_Upgrade_fs2 = false;

                    } else if (FS_selected.equalsIgnoreCase("2")) {

                        if (ResponceText.trim().equalsIgnoreCase("Y"))
                            AppConstants.UP_Upgrade_fs3 = true;
                        else
                            AppConstants.UP_Upgrade_fs3 = false;

                    } else if (FS_selected.equalsIgnoreCase("3")) {

                        if (ResponceText.trim().equalsIgnoreCase("Y"))
                            AppConstants.UP_Upgrade_fs4 = true;
                        else
                            AppConstants.UP_Upgrade_fs4 = false;

                    } else if (FS_selected.equalsIgnoreCase("4")) {

                        if (ResponceText.trim().equalsIgnoreCase("Y"))
                            AppConstants.UP_Upgrade_fs5 = true;
                        else
                            AppConstants.UP_Upgrade_fs5 = false;

                    } else if (FS_selected.equalsIgnoreCase("5")) {

                        if (ResponceText.trim().equalsIgnoreCase("Y"))
                            AppConstants.UP_Upgrade_fs6 = true;
                        else
                            AppConstants.UP_Upgrade_fs6 = false;

                    }
                    //--------------------------------------------

                } else {

                    System.out.println("Something went wrong");
                }


            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public class UpgradeCurrentVersionWithUgradableVersion extends AsyncTask<Void, Void, Void> {


        UpgradeVersionEntity objupgrade;
        public String response = null;

        public UpgradeCurrentVersionWithUgradableVersion(UpgradeVersionEntity objupgrade) {

            this.objupgrade = objupgrade;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                ServerHandler serverHandler = new ServerHandler();

                Gson gson = new Gson();
                String jsonData = gson.toJson(objupgrade);


                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(objupgrade.IMEIUDID + ":" + objupgrade.Email + ":" + "UpgradeCurrentVersionWithUgradableVersion");
                response = serverHandler.PostTextData(WelcomeActivity.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {

                CommonUtils.LogMessage("BS", "UpgradeCurrentVersionWithUgradableVersion ", ex);
            }
            return null;
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
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
            if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + "No LE Support.");
            finish();
            return false;
        }

        return true;
    }

    public class GetSSIDUsingLocationOnResume extends AsyncTask<Void, Void, String> {


        ProgressDialog pd;

        @Override
        protected void onPreExecute() {

            String s = "Please wait...";
            SpannableString ss2 = new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            pdOnResume = pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage(ss2);
            pd.setCancelable(true);
            pd.setCancelable(false);
            pd.show();

        }

        protected String doInBackground(Void... arg0) {
            String resp = "";

            try {

                UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetails(WelcomeActivity.this);

                ServerHandler serverHandler = new ServerHandler();
                //----------------------------------------------------------------------------------
                String parm1 = AppConstants.getIMEI(WelcomeActivity.this) + ":" + userInfoEntity.PersonEmail + ":" + "Other";
                String parm2 = "Authenticate:I:" + Constants.Latitude + "," + Constants.Longitude;


                System.out.println("parm1----" + parm1);
                System.out.println("parm2----" + parm2);

                String authString = "Basic " + AppConstants.convertStingToBase64(parm1);

                //resp = serverHandler.PostTextData(WelcomeActivity.this, AppConstants.webURL, parm2, authString);
                //----------------------------------------------------------------------------------

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(20, TimeUnit.SECONDS);
                client.setReadTimeout(20, TimeUnit.SECONDS);
                client.setWriteTimeout(20, TimeUnit.SECONDS);

                RequestBody body = RequestBody.create(ServerHandler.TEXT, parm2);
                Request request = new Request.Builder()
                        .url(AppConstants.webURL)
                        .post(body)
                        .addHeader("Authorization", authString)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

                //------------------------------

            } catch (Exception e) {
                pd.dismiss();
                System.out.println("Ex" + e.getMessage());
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  GetSSIDUsingLocationOnResume onPostExecute --Exception " + e);
                if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }

            return resp;
        }


        @Override
        protected void onPostExecute(String result) {


            try {

                pd.dismiss();
                tvLatLng.setText("Current Location :" + Constants.Latitude + "," + Constants.Longitude);

                System.out.println("GetSSIDUsingLocation...." + result);

                serverSSIDList.clear();
                // BackgroundServiceKeepDataTransferAlive.SSIDList.clear();//clear SSIDList
                //AppConstants.DetailsServerSSIDList.clear();

                String errMsg = "";
                if (result != null && !result.isEmpty()) {

                    JSONObject jsonObjectSite = new JSONObject(result);
                    String ResponseMessageSite = jsonObjectSite.getString(AppConstants.RES_MESSAGE);
                    String userData = jsonObjectSite.getString(AppConstants.RES_DATA_USER);
                    JSONObject jsonObject = new JSONObject(userData);

                    if (ResponseMessageSite.equalsIgnoreCase("success")) {

                        IsGateHub = jsonObject.getString("IsGateHub");
                        IsStayOpenGate = jsonObject.getString("StayOpenGate");
                        boolean fa_data = Boolean.parseBoolean(jsonObject.getString("EnbDisHubForFA"));
                        boolean UseBarcode = Boolean.parseBoolean(jsonObject.getString("UseBarcode"));
                        boolean UseBarcodeForPersonnel = Boolean.parseBoolean(jsonObject.getString("UseBarcodeForPersonnel"));
                        boolean IsEnableServerForTLD = Boolean.parseBoolean(jsonObject.getString("IsEnableServerForTLD"));

                        CommonUtils.SaveDataInPrefForGatehub(WelcomeActivity.this, IsGateHub, IsStayOpenGate);
                        CommonUtils.FA_FlagSavePref(WelcomeActivity.this, fa_data, UseBarcode, IsEnableServerForTLD, UseBarcodeForPersonnel);

                        if (BackgroundServiceKeepDataTransferAlive.SSIDList != null)
                            BackgroundServiceKeepDataTransferAlive.SSIDList.clear();//clear SSIDList

                        JSONArray Requests = jsonObjectSite.getJSONArray(AppConstants.RES_DATA_SSID);

                        if (Requests.length() > 0) {

                            for (int i = 0; i < Requests.length(); i++) {
                                JSONObject c = Requests.getJSONObject(i);

                                String SiteId = c.getString("SiteId");
                                String SiteNumber = c.getString("SiteNumber");
                                String SiteName = c.getString("SiteName");
                                String SiteAddress = c.getString("SiteAddress");
                                String Latitude = c.getString("Latitude");
                                String Longitude = c.getString("Longitude");
                                String HoseId = c.getString("HoseId");
                                String HoseNumber = c.getString("HoseNumber");
                                String WifiSSId = c.getString("WifiSSId");
                                String UserName = c.getString("UserName");
                                String Password = c.getString("Password");
                                String ResponceMessage = c.getString("ResponceMessage");
                                String ResponceText = c.getString("ResponceText");
                                String ReplaceableHoseName = c.getString("ReplaceableHoseName");
                                String IsHoseNameReplaced = c.getString("IsHoseNameReplaced");
                                String BTMacAddress = c.getString("BluetoothMacAddress");
                                String MacAddress = c.getString("MacAddress");
                                String IsBusy = c.getString("IsBusy");
                                String IsUpgrade = c.getString("IsUpgrade");
                                String FirmwareVersion = c.getString("FirmwareVersion");
                                String PulserTimingAdjust = c.getString("PulserTimingAdjust");
                                String BluetoothCardReaderHF = c.getString("BluetoothCardReaderHF");
                                String IsDefective = c.getString("IsDefective");
                                String FilePath = c.getString("FilePath");
                                String ReconfigureLink = c.getString("ReconfigureLink");
                                String FSNPMacAddress = c.getString("FSNPMacAddress");
                                String IsTLDCall = c.getString("IsTLDCall");
                                String TLDFirmwareFilePath = c.getString("TLDFirmwareFilePath");
                                String TLDFIrmwareVersion = c.getString("TLDFIrmwareVersion");
                                String PROBEMacAddress = c.getString("PROBEMacAddress");
                                String IsTLDFirmwareUpgrade = c.getString("IsTLDFirmwareUpgrade");
                                String ScheduleTankReading = c.getString("ScheduleTankReading");
                                String LinkCommunicationType = c.getString("HubLinkCommunication");
                                String IsTankEmpty = c.getString("IsTankEmpty");
                                String IsLinkFlagged = c.getString("IsLinkFlagged");
                                String LinkFlaggedMessage = c.getString("LinkFlaggedMessage");
                                String IsResetSwitchTimeBounce = c.getString("IsResetSwitchTimeBounce");
                                String FirmwareFileName = c.getString("FirmwareFileName");

                                SetBTLinksMacAddress(i, BTMacAddress);

                                //Check prefix required for link name
                                /*if (!ReplaceableHoseName.startsWith("FS-")) {
                                    ReplaceableHoseName = "FS-" + ReplaceableHoseName;
                                }*/

                                //BLE upgrade
                                String IsHFUpdate = jsonObject.getString("IsHFUpdate");
                                String IsLFUpdate = jsonObject.getString("IsLFUpdate");
                                String BLEVersion = jsonObject.getString("BLEVersion");
                                String BLEType = "";
                                if (IsHFUpdate.equals("Y")) {
                                    BLEType = "HF";
                                } else if (IsLFUpdate.equals("Y")) {
                                    BLEType = "LF";
                                }

                                //Enable Nano Http server if Ble reader update required
                                if (!AppConstants.DownloadFileHttpServer.equalsIgnoreCase("Started")) {

                                    AppConstants.DownloadFileHttpServer = "Started";
                                    if (IsHFUpdate.equalsIgnoreCase("Y") || IsLFUpdate.equalsIgnoreCase("Y")) {
                                        ctx = WelcomeActivity.this;
                                        try {

                                            server = new MyServer();
                                            //DownloadFileHttp.ctx= WelcomeActivity.this;
                                            DownloadFileHttp abc = new DownloadFileHttp();

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            //if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + " MyServer Ex-" + e);
                                        }

                                        Log.i(TAG, "DownloadFileHttpServer status" + AppConstants.DownloadFileHttpServer);
                                    }
                                }


                                String BLEFileLocation = jsonObject.getString("BLEFileLocation");

                                /*System.out.println("IsBLEUpdate-----BLEVersion------ " + IsBLEUpdate + " " + BLEVersion);
                                System.out.println("BLEType-----BLEFileLocation------ " + BLEType + " " + BLEFileLocation);*/

                                /*AppConstants.IsBLEUpdate = IsBLEUpdate;
                                AppConstants.BLEVersion = BLEVersion;
                                AppConstants.BLEType = BLEType;
                                AppConstants.BLEFileLocation = BLEFileLocation;*/

                                SharedPreferences sharedPref = getSharedPreferences("BLEUpgradeInfo", 0);
                                SharedPreferences.Editor editor1 = sharedPref.edit();
                                editor1.putString("IsLFUpdate", IsLFUpdate);
                                editor1.putString("IsHFUpdate", IsHFUpdate);
                                editor1.putString("BLEVersion", BLEVersion);
                                editor1.putString("BLEType", BLEType);
                                editor1.putString("BLEFileLocation", BLEFileLocation);
                                editor1.commit();


                                AppConstants.UP_FilePath = FilePath;

                                AppConstants.BT_READER_NAME = BluetoothCardReaderHF;

                                //Current Fs wifi password
                                Constants.CurrFsPass = Password;


                                //For schedule reboot
                                String dayForReboot = jsonObject.getString("RebootDay");
                                rebootDay = Integer.parseInt(dayForReboot);
                                String timeForReboot = jsonObject.getString("RebootTime");

                                String[] timeReboot = timeForReboot.split(":");
                                rebootHours = Integer.parseInt(timeReboot[0]);
                                rebootMinutes = Integer.parseInt(timeReboot[1]);
                                System.out.println("Reboot details 1----------- " + dayForReboot + " " + timeForReboot);
                                System.out.println("Reboot details 2----------- " + rebootDay + " " + rebootHours + " " + rebootMinutes);

                                HashMap<String, String> map = new HashMap<>();
                                map.put("SiteId", SiteId);
                                map.put("HoseId", HoseId);
                                map.put("WifiSSId", WifiSSId);
                                map.put("ReplaceableHoseName", ReplaceableHoseName);
                                map.put("IsHoseNameReplaced", IsHoseNameReplaced);
                                map.put("item", WifiSSId);
                                map.put("MacAddress", MacAddress);
                                map.put("BTMacAddress", BTMacAddress);
                                map.put("IsBusy", IsBusy);
                                map.put("IsUpgrade", IsUpgrade);
                                map.put("UPFilePath", FilePath);
                                map.put("FirmwareVersion", FirmwareVersion);
                                map.put("PulserTimingAdjust", PulserTimingAdjust);
                                map.put("ReconfigureLink", ReconfigureLink);
                                map.put("FSNPMacAddress", FSNPMacAddress);
                                map.put("IsTLDCall", IsTLDCall);
                                map.put("TLDFirmwareFilePath", TLDFirmwareFilePath);
                                map.put("TLDFIrmwareVersion", TLDFIrmwareVersion);
                                map.put("PROBEMacAddress", PROBEMacAddress);
                                map.put("IsTLDFirmwareUpgrade", IsTLDFirmwareUpgrade);
                                map.put("ScheduleTankReading", ScheduleTankReading);
                                map.put("LinkCommunicationType", LinkCommunicationType);
                                map.put("IsTankEmpty", IsTankEmpty);
                                map.put("IsLinkFlagged", IsLinkFlagged);
                                map.put("LinkFlaggedMessage", LinkFlaggedMessage);
                                map.put("IsResetSwitchTimeBounce", IsResetSwitchTimeBounce);
                                map.put("FirmwareFileName", FirmwareFileName);

                                if (ResponceMessage.equalsIgnoreCase("success")) {

                                    if (isNotNULL(SiteId) && isNotNULL(HoseId) && isNotNULL(WifiSSId)) {
                                        serverSSIDList.add(map);
                                        AppConstants.DetailsServerSSIDList = serverSSIDList;
                                        BackgroundServiceKeepDataTransferAlive.SSIDList = serverSSIDList;

                                        //For schedule reboot
                                        Calendar calendar = Calendar.getInstance();
                                        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
                                        int today = calendar.get(Calendar.DAY_OF_WEEK);
                                        int hours_now = calendar.get(Calendar.HOUR);
                                        int minutes_now = calendar.get(Calendar.MINUTE);

                                        SharedPreferences settings = getSharedPreferences("PREFS", 0);
                                        int lastDay = settings.getInt("day", 0);

                                        /*SharedPreferences.Editor editor = settings.edit();
                                        editor.putInt("day", 0);
                                        editor.commit();*/

                                        if (rebootDay == 0) {

                                            try {
                                                Intent name = new Intent(WelcomeActivity.this, BackgroundServiceScheduleReboot.class);
                                                PendingIntent pintent = PendingIntent.getService(getApplicationContext(), REBOOT_INTENT_ID, name, 0);
                                                AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                                                pintent.cancel();
                                                alarm.cancel(pintent);

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                        } else if (rebootDay == -1) {

                                            if (lastDay != currentDay) {
                                                SharedPreferences.Editor editor = settings.edit();
                                                editor.putInt("day", currentDay);
                                                editor.commit();

                                                //Code that runs once in a day
                                                if (AppConstants.DisableAllRebootOptions.equalsIgnoreCase("N")) {
                                                    scheduleReboot(rebootHours, rebootMinutes, rebootDay);
                                                }
                                            }

                                        } else if (today == rebootDay) {

                                            if (lastDay != currentDay) {
                                                SharedPreferences.Editor editor = settings.edit();
                                                editor.putInt("day", currentDay);
                                                editor.commit();

                                                //Code that runs once in a day
                                                if (AppConstants.DisableAllRebootOptions.equalsIgnoreCase("N")) {
                                                    scheduleReboot(rebootHours, rebootMinutes, rebootDay);
                                                }
                                            }
                                        }

                                    }

                                } else {
                                    errMsg = ResponceText;
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "GetSSIDUsingLocationOnResume SSIDData (" + WifiSSId + "): " + ResponceText);
                                    AppConstants.AlertDialogFinish(WelcomeActivity.this, ResponceText);
                                }
                            }

                            AppConstants.temp_serverSSIDList = serverSSIDList;
                        }
                        try {

                            if (serverSSIDList != null && serverSSIDList.size() == 1 && IsGateHub.equalsIgnoreCase("True") && Constants.FS_1STATUS.equalsIgnoreCase("FREE")) {

                                cancelThinDownloadManager();
                                try {
                                    String SSID_mac = serverSSIDList.get(0).get("MacAddress");
                                    String ReconfigureLink = serverSSIDList.get(0).get("ReconfigureLink");
                                    AppConstants.SITE_ID = serverSSIDList.get(0).get("SiteId");
                                    String LinkCommunicationType = serverSSIDList.get(0).get("LinkCommunicationType");
                                    String selSSID = serverSSIDList.get(0).get("WifiSSId");
                                    if (ReconfigureLink == null) {
                                        ReconfigureLink = "";
                                    }

                                    OfflineConstants.storeCurrentTransaction(WelcomeActivity.this, "", AppConstants.SITE_ID, "", "", "", "", "", AppConstants.currentDateFormat("yyyy-MM-dd HH:mm"), "", "");

                                    if (LinkCommunicationType.equalsIgnoreCase("HTTP")) {

                                        String Chk_ip = "";
                                        if (AppConstants.DetailsListOfConnectedDevices != null && AppConstants.DetailsListOfConnectedDevices.size() > 0)
                                            Chk_ip = AppConstants.DetailsListOfConnectedDevices.get(0).get("ipAddress");
                                        else {
                                            getipOverOSVersion();
                                        }

                                        if (Chk_ip != null && Chk_ip.length() > 3 && !ReconfigureLink.equalsIgnoreCase("true")) {

                                            boolean isMacConnected = false;
                                            if (AppConstants.DetailsListOfConnectedDevices != null) {
                                                for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                                    String Chk_mac = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");

                                                    if (SSID_mac.equalsIgnoreCase(Chk_mac)) {
                                                        if (AppConstants.GenerateLogs)
                                                            AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + SSID_mac + ") is connected to hotspot.");
                                                        IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                                                        isMacConnected = true;
                                                        break;
                                                    }
                                                }
                                            }

                                            if (!isMacConnected) {
                                                if (AppConstants.GenerateLogs)
                                                    AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + SSID_mac + ") is not found in connected devices. " + AppConstants.DetailsListOfConnectedDevices);

                                                for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                                    String Chk_mac = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                                                    if (AppConstants.GenerateLogs)
                                                        AppConstants.WriteinFile(TAG + "Checking Mac Address using info command: (" + Chk_mac + ")");

                                                    String connectedIp = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");

                                                    IpAddress = GetAndCheckMacAddressFromInfoCommand(connectedIp, SSID_mac, Chk_mac);
                                                    if (!IpAddress.trim().isEmpty()) {
                                                        if (AppConstants.GenerateLogs)
                                                            AppConstants.WriteinFile("===================================================================");
                                                        break;
                                                    }
                                                    if (AppConstants.GenerateLogs)
                                                        AppConstants.WriteinFile("===================================================================");
                                                }
                                            }

                                            if (!IpAddress.trim().isEmpty()) {
                                                SelectedItemPos = 0;
                                                tvSSIDName.setText(serverSSIDList.get(0).get("WifiSSId"));
                                                OnHoseSelected_OnClick(Integer.toString(0));
                                                goButtonAction(null);
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Auto select fail", Toast.LENGTH_SHORT).show();
                                            if (AppConstants.GenerateLogs)
                                                AppConstants.WriteinFile(TAG + "Auto select fail");
                                        }
                                    } else if (LinkCommunicationType.equalsIgnoreCase("BT")) {
                                        SelectedItemPos = 0;
                                        tvSSIDName.setText(selSSID);
                                        OnHoseSelected_OnClick(Integer.toString(0));
                                        GoButtonFunctionalityForSingleLink(LinkCommunicationType);
                                        //goButtonAction(null);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } else if (serverSSIDList != null && serverSSIDList.size() == 1 && Constants.FS_1STATUS.equalsIgnoreCase("FREE")) {

                                cancelThinDownloadManager();
                                try {
                                    AppConstants.IsSingleLink = true;
                                    String SSID_mac = serverSSIDList.get(0).get("MacAddress");
                                    String ReconfigureLink = serverSSIDList.get(0).get("ReconfigureLink");
                                    AppConstants.SITE_ID = serverSSIDList.get(0).get("SiteId");
                                    String LinkCommunicationType = serverSSIDList.get(0).get("LinkCommunicationType");
                                    String BTselMacAddress = serverSSIDList.get(0).get("BTMacAddress");
                                    String selSSID = serverSSIDList.get(0).get("WifiSSId");
                                    if (ReconfigureLink == null) {
                                        ReconfigureLink = "";
                                    }

                                    OfflineConstants.storeCurrentTransaction(WelcomeActivity.this, "", AppConstants.SITE_ID, "", "", "", "", "", AppConstants.currentDateFormat("yyyy-MM-dd HH:mm"), "", "");

                                    if (LinkCommunicationType.equalsIgnoreCase("BT") && !ReconfigureLink.equalsIgnoreCase("true")) {
                                        //tvSSIDName.setText("Tap here to select hose");
                                        //btnGo.setVisibility(View.VISIBLE);
                                        SingleBTLinkSelection(selSSID, BTselMacAddress);

                                    } else {
                                        String Chk_ip = "";
                                        if (AppConstants.DetailsListOfConnectedDevices != null && AppConstants.DetailsListOfConnectedDevices.size() > 0) {
                                            Chk_ip = AppConstants.DetailsListOfConnectedDevices.get(0).get("ipAddress");
                                        } else {
                                            getipOverOSVersion();
                                        }

                                        if (Chk_ip != null && Chk_ip.length() > 3 && !ReconfigureLink.equalsIgnoreCase("true")) {

                                            boolean isMacConnected = false;
                                            if (AppConstants.DetailsListOfConnectedDevices != null) {
                                                for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                                    String Chk_mac = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");

                                                    if (SSID_mac.equalsIgnoreCase(Chk_mac)) {
                                                        if (AppConstants.GenerateLogs)
                                                            AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + SSID_mac + ") is connected to hotspot.");
                                                        IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                                                        isMacConnected = true;
                                                        break;
                                                    }
                                                }
                                            }

                                            if (!isMacConnected) {
                                                if (AppConstants.GenerateLogs)
                                                    AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + SSID_mac + ") is not found in connected devices. " + AppConstants.DetailsListOfConnectedDevices);

                                                for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                                    String Chk_mac = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                                                    if (AppConstants.GenerateLogs)
                                                        AppConstants.WriteinFile(TAG + "Checking Mac Address using info command: (" + Chk_mac + ")");

                                                    String connectedIp = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");

                                                    IpAddress = GetAndCheckMacAddressFromInfoCommand(connectedIp, SSID_mac, Chk_mac);
                                                    if (!IpAddress.trim().isEmpty()) {
                                                        if (AppConstants.GenerateLogs)
                                                            AppConstants.WriteinFile("===================================================================");
                                                        break;
                                                    }
                                                    if (AppConstants.GenerateLogs)
                                                        AppConstants.WriteinFile("===================================================================");
                                                }
                                            }

                                            if (!IpAddress.trim().isEmpty()) {
                                                SelectedItemPos = 0;
                                                tvSSIDName.setText(serverSSIDList.get(0).get("WifiSSId"));
                                                OnHoseSelected_OnClick(Integer.toString(0));
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Auto select fail", Toast.LENGTH_SHORT).show();
                                            if (AppConstants.GenerateLogs)
                                                AppConstants.WriteinFile(TAG + "Auto select fail");
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } /*else if (serverSSIDList != null && AppConstants.LastSelectedHose != null && Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE")) {

                                //Thread.sleep(1000);
                                try {
                                    int num = Integer.parseInt(AppConstants.LastSelectedHose);
                                    String SSID_mac = serverSSIDList.get(num).get("MacAddress");
                                    String ReconfigureLink = serverSSIDList.get(num).get("ReconfigureLink");
                                    String Chk_ip = AppConstants.DetailsListOfConnectedDevices.get(num).get("ipAddress");


                                    if (Chk_ip != null && Chk_ip.length() > 3 && !ReconfigureLink.equalsIgnoreCase("true")) {

                                        for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {

                                            String Chk_mac = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                                            if (SSID_mac.equalsIgnoreCase(Chk_mac)) {

                                                tvSSIDName.setText(serverSSIDList.get(num).get("WifiSSId"));
                                                OnHoseSelected_OnClick(Integer.toString(num));
                                                break;
                                            }
                                        }

                                    } else {
                                        Toast.makeText(getApplicationContext(), "Auto select fail..", Toast.LENGTH_SHORT).show();
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }*/

                        } catch (Exception e) {
                            System.out.println(e);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "  GetSSIDUsingLocationOnResume if only one hose autoselect   --Exception " + e);
                        }


                    } else if (ResponseMessageSite.equalsIgnoreCase("fail")) {
                        String ResponseTextSite = jsonObjectSite.getString(AppConstants.RES_TEXT);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "GetSSIDUsingLocationOnResume SSIDData response fail. Error: " + ResponseTextSite);
                        AppConstants.AlertDialogBox(WelcomeActivity.this, ResponseTextSite);

                    }
                } else {

                    if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                        AppConstants.NETWORK_STRENGTH = false;
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Temporary loss of cell service ~Switching to offline mode!!");
                    }
                    new GetOfflineSSIDUsingLocation().execute();
                }


            } catch (Exception e) {

                CommonUtils.LogMessage(TAG, " GetSSIDUsingLocation :" + result, e);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  GetSSIDUsingLocationOnResume --Exception " + e);
                if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }

        }
    }

    public void connectWiFiLibraryCountdownTimer(String asd) {

        System.out.println("MJ- connectWiFiLibrary" + asd);

        Constants.hotspotstayOn = false; //hotspot enable/disable flag


        WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (!wifiManagerMM.isWifiEnabled()) {
            wifiManagerMM.setWifiEnabled(true);
        }

        //PrefixLink Name with "FS-"
        /*String ReconfigSSID = "";
        if (AppConstants.CURRENT_SELECTED_SSID.startsWith("FS-")) {
            ReconfigSSID = AppConstants.CURRENT_SELECTED_SSID;
        } else {
            ReconfigSSID = "FS-" + AppConstants.CURRENT_SELECTED_SSID;
        }*/

        AppConstants.SELECTED_SSID_FOR_MANUALL = AppConstants.CURRENT_SELECTED_SSID;//ReconfigSSID;

        String ote = AppConstants.SELECTED_SSID_FOR_MANUALL;
        String otePass = Constants.CurrFsPass;

        WifiUtils.withContext(WelcomeActivity.this)
                .connectWith(ote, otePass)
                .setTimeout(6000)
                .onConnectionResult(WelcomeActivity.this::checkResultCountdownTimer)
                .start();

    }

    public void connectWiFiLibrary(String asd) {

        System.out.println("MJ- connectWiFiLibrary" + asd);

        Constants.hotspotstayOn = false; //hotspot enable/disable flag
        wifiApManager.setWifiApEnabled(null, false);


        String s = "Connecting to wifi please wait..";
        SpannableString ss2 = new SpannableString(s);
        ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
        ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
        loading = new ProgressDialog(WelcomeActivity.this);
        loading.setMessage(ss2);
        loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loading.show();

        WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (!wifiManagerMM.isWifiEnabled()) {
            wifiManagerMM.setWifiEnabled(true);
        }

        //PrefixLink Name with "FS-"
        /*String ReconfigSSID = "";
        if (AppConstants.CURRENT_SELECTED_SSID.startsWith("FS-")) {
            ReconfigSSID = AppConstants.CURRENT_SELECTED_SSID;
        } else {
            ReconfigSSID = "FS-" + AppConstants.CURRENT_SELECTED_SSID;
        }*/

        AppConstants.SELECTED_SSID_FOR_MANUALL = AppConstants.CURRENT_SELECTED_SSID;

        String ote = AppConstants.SELECTED_SSID_FOR_MANUALL;
        String otePass = Constants.CurrFsPass;

        WifiUtils.withContext(WelcomeActivity.this)
                .connectWith(ote, otePass)
                .setTimeout(7000)
                .onConnectionResult(WelcomeActivity.this::checkResult)
                .start();


    }

    public void countDownTimerForReconfigureFun() {

        String s = "Connecting to wifi please wait..";
        SpannableString ss2 = new SpannableString(s);
        ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
        ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
        loading = new ProgressDialog(WelcomeActivity.this);
        loading.setMessage(ss2);

        loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loading.show();

        Constants.hotspotstayOn = false; //hotspot enable/disable flag
        wifiApManager.setWifiApEnabled(null, false);

        WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiManagerMM.setWifiEnabled(true);

        //Check for PrefixLink Name with "FS-"
        /*String ReconfigSSID = "";
        if (AppConstants.CURRENT_SELECTED_SSID.startsWith("FS-")) {
            ReconfigSSID = AppConstants.CURRENT_SELECTED_SSID;
        } else {
            ReconfigSSID = "FS-" + AppConstants.CURRENT_SELECTED_SSID;
        }*/

        AppConstants.SELECTED_SSID_FOR_MANUALL = AppConstants.CURRENT_SELECTED_SSID;//ReconfigSSID;


        if (countDownTimerForReconfigure == null) {
            countDownTimerForReconfigure = new CountDownTimer(30000, 6000) {

                public void onTick(long millisUntilFinished) {

                    WifiManager wifiManager = (WifiManager) WelcomeActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    String ssid = "";
                    if (wifiManager.isWifiEnabled()) {
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        ssid = wifiInfo.getSSID();
                    }

                    ssid = ssid.replace("\"", "");

                    System.out.println("countDownTimerFForReconfigure-" + ssid + " : " + AppConstants.SELECTED_SSID_FOR_MANUALL);

                    if (ssid.contains(AppConstants.SELECTED_SSID_FOR_MANUALL) || AppConstants.SELECTED_SSID_FOR_MANUALL.contains(ssid)) {

                        //connecting for 30 sec then on finish check connected or not

                         /*if (loading != null)
                            loading.dismiss();

                        if (countDownTimerForReconfigure != null)
                            countDownTimerForReconfigure.cancel();


                       new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                new WiFiConnectTask().execute();
                            }
                        }, 3000);*/


                    } else {
                        connectWiFiLibraryCountdownTimer("4");
                    }

                }

                public void onFinish() {
                    if (loading != null)
                        loading.dismiss();

                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String ssid = wifiInfo.getSSID();

                    ssid = ssid.replace("\"", "");

                    System.out.println("countDownTimerFForReconfigure-" + ssid + " : " + AppConstants.SELECTED_SSID_FOR_MANUALL);

                    if (ssid.contains(AppConstants.CURRENT_SELECTED_SSID) || ssid.contains(AppConstants.SELECTED_SSID_FOR_MANUALL) || AppConstants.SELECTED_SSID_FOR_MANUALL.contains(ssid)) {

                        new WiFiConnectTask().execute();//onfinish


                    } else {
                        Constants.hotspotstayOn = false;
                        AppConstants.ManuallReconfigure = true;


                        AppConstants.colorToastBigFont(WelcomeActivity.this, "Unable to auto connect. Started manual process" + AppConstants.CURRENT_SELECTED_SSID, Color.RED);
                        LinkReConfigurationProcessStep1();//onfinish


                    }
                }
            }.start();
        }
    }

    private void checkResultCountdownTimer(boolean isSuccess) {

    }

    private void checkResult(boolean isSuccess) {

        loading.dismiss();
        if (isSuccess) {


            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "  WIFI CONNECTED " + AppConstants.CURRENT_SELECTED_SSID);
            AppConstants.colorToastBigFont(WelcomeActivity.this, "CONNECTED TO:- " + AppConstants.CURRENT_SELECTED_SSID, Color.BLUE);

            WifiManager wifiManager = (WifiManager) WelcomeActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int NetID = wifiInfo.getNetworkId();
            String ssid = wifiInfo.getSSID();

            if (ssid.contains(AppConstants.SELECTED_SSID_FOR_MANUALL)) {

                //new WiFiConnectTask().execute();

            } else {

                AppConstants.colorToastBigFont(getApplicationContext(), "Connected to wrong Wifi Please try again..", Color.RED);

            }

        } else {


            AppConstants.ManuallReconfigure = false;
            AppConstants.colorToastBigFont(getApplicationContext(), "Connecting to " + AppConstants.CURRENT_SELECTED_SSID + " Attempt " + 2, Color.RED);
            connectWiFiLibrary2Attempt();


        }

    }

    public void connectWiFiLibrary2Attempt() {

        Constants.hotspotstayOn = false; //hotspot enable/disable flag
        wifiApManager.setWifiApEnabled(null, false);


        String s = "Connecting to wifi please wait..2";
        SpannableString ss2 = new SpannableString(s);
        ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
        ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
        loading = new ProgressDialog(WelcomeActivity.this);
        loading.setMessage(ss2);
        loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loading.show();

        WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiManagerMM.setWifiEnabled(true);


        //PrefixLink Name with "FS-"
        /*String ReconfigSSID = "";
        if (AppConstants.CURRENT_SELECTED_SSID.startsWith("FS-")) {
            ReconfigSSID = AppConstants.CURRENT_SELECTED_SSID;
        } else {
            ReconfigSSID = "FS-" + AppConstants.CURRENT_SELECTED_SSID;
        }*/

        AppConstants.SELECTED_SSID_FOR_MANUALL = AppConstants.CURRENT_SELECTED_SSID;//ReconfigSSID;

        String ote = AppConstants.SELECTED_SSID_FOR_MANUALL;
        String otePass = Constants.CurrFsPass;

        WifiUtils.withContext(WelcomeActivity.this)
                .connectWith(ote, otePass)
                .setTimeout(7000)
                .onConnectionResult(WelcomeActivity.this::checkResult2Attempt)
                .start();

    }

    private void checkResult2Attempt(boolean isSuccess) {

        loading.dismiss();
        if (isSuccess) {


            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "  WIFI CONNECTED " + AppConstants.CURRENT_SELECTED_SSID);
            AppConstants.colorToastBigFont(WelcomeActivity.this, "CONNECTED TO:- " + AppConstants.CURRENT_SELECTED_SSID, Color.BLUE);

            WifiManager wifiManager = (WifiManager) WelcomeActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int NetID = wifiInfo.getNetworkId();
            String ssid = wifiInfo.getSSID();

            if (ssid.contains(AppConstants.SELECTED_SSID_FOR_MANUALL)) {

                //new WiFiConnectTask().execute();

            } else {

                AppConstants.colorToastBigFont(getApplicationContext(), "Connected to wrong Wifi Please try again..", Color.RED);

            }

        } else {


            AppConstants.ManuallReconfigure = false;
            AppConstants.colorToastBigFont(getApplicationContext(), "Connecting to " + AppConstants.CURRENT_SELECTED_SSID + " Attempt 3", Color.RED);
            connectWiFiLibrary3Attempt();


        }

    }

    public void connectWiFiLibrary3Attempt() {

        Constants.hotspotstayOn = false; //hotspot enable/disable flag
        wifiApManager.setWifiApEnabled(null, false);


        String s = "Connecting to wifi please wait..3";
        SpannableString ss2 = new SpannableString(s);
        ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
        ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
        loading = new ProgressDialog(WelcomeActivity.this);
        loading.setMessage(ss2);
        loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loading.show();

        WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiManagerMM.setWifiEnabled(true);


        //PrefixLink Name with "FS-"
        /*String ReconfigSSID = "";
        if (AppConstants.CURRENT_SELECTED_SSID.startsWith("FS-")) {
            ReconfigSSID = AppConstants.CURRENT_SELECTED_SSID;
        } else {
            ReconfigSSID = "FS-" + AppConstants.CURRENT_SELECTED_SSID;
        }*/

        AppConstants.SELECTED_SSID_FOR_MANUALL = AppConstants.CURRENT_SELECTED_SSID;

        String ote = AppConstants.SELECTED_SSID_FOR_MANUALL;
        String otePass = Constants.CurrFsPass;

        WifiUtils.withContext(WelcomeActivity.this)
                .connectWith(ote, otePass)
                .setTimeout(7000)
                .onConnectionResult(WelcomeActivity.this::checkResult3Attempt)
                .start();

    }

    private void checkResult3Attempt(boolean isSuccess) {

        loading.dismiss();
        if (isSuccess) {


            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "  WIFI CONNECTED " + AppConstants.CURRENT_SELECTED_SSID);
            AppConstants.colorToastBigFont(WelcomeActivity.this, "CONNECTED TO:- " + AppConstants.CURRENT_SELECTED_SSID, Color.BLUE);

            WifiManager wifiManager = (WifiManager) WelcomeActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int NetID = wifiInfo.getNetworkId();
            String ssid = wifiInfo.getSSID();

            if (ssid.contains(AppConstants.SELECTED_SSID_FOR_MANUALL)) {

                //new WiFiConnectTask().execute();

            } else {

                AppConstants.colorToastBigFont(getApplicationContext(), "Connected to wrong Wifi Please try again..", Color.RED);

            }

        } else {


            Constants.hotspotstayOn = false;
            AppConstants.ManuallReconfigure = true;

            AppConstants.colorToastBigFont(getApplicationContext(), "Connect manually to: " + AppConstants.CURRENT_SELECTED_SSID, Color.RED);
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " Connect manually to: " + AppConstants.CURRENT_SELECTED_SSID + " and try..!! ");


            AlertSettings(WelcomeActivity.this, "Unable to connect " + AppConstants.CURRENT_SELECTED_SSID + "!\n\nPlease connect to " + AppConstants.CURRENT_SELECTED_SSID + " manually using the 'WIFI settings' screen.\nThen hit back and click on the 'START' button to continue.");


        }

    }


    public void downloadTLD_BinFile(int linkNumber, String UP_TLD_FilePath, String
            UP_TLD_Version) {

        boolean download_now = false;

        SharedPreferences sharedPref = WelcomeActivity.this.getSharedPreferences("storeTLDVersions", Context.MODE_PRIVATE);

        String link0Version = sharedPref.getString("0", "");
        String link1Version = sharedPref.getString("1", "");
        String link2Version = sharedPref.getString("2", "");
        String link3Version = sharedPref.getString("3", "");
        String link4Version = sharedPref.getString("4", "");
        String link5Version = sharedPref.getString("5", "");


        SharedPreferences.Editor editor = sharedPref.edit();

        if (linkNumber == 0 && !link0Version.trim().equalsIgnoreCase(UP_TLD_Version)) {
            download_now = true;
            editor.putString("0", UP_TLD_Version);

        } else if (linkNumber == 1 && !link1Version.trim().equalsIgnoreCase(UP_TLD_Version)) {
            download_now = true;
            editor.putString("1", UP_TLD_Version);

        } else if (linkNumber == 2 && !link2Version.trim().equalsIgnoreCase(UP_TLD_Version)) {
            download_now = true;
            editor.putString("2", UP_TLD_Version);

        } else if (linkNumber == 3 && !link3Version.trim().equalsIgnoreCase(UP_TLD_Version)) {
            download_now = true;
            editor.putString("3", UP_TLD_Version);

        } else if (linkNumber == 4 && !link3Version.trim().equalsIgnoreCase(UP_TLD_Version)) {
            download_now = true;
            editor.putString("4", UP_TLD_Version);

        } else if (linkNumber == 5 && !link3Version.trim().equalsIgnoreCase(UP_TLD_Version)) {
            download_now = true;
            editor.putString("5", UP_TLD_Version);

        }
        editor.apply(); //store pref

    }

    public class GetSSIDUsingLocationGateHub extends AsyncTask<Void, Void, String> {

        protected String doInBackground(Void... arg0) {
            String resp = "";

            try {

                UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetails(WelcomeActivity.this);

                ServerHandler serverHandler = new ServerHandler();
                //----------------------------------------------------------------------------------
                String parm1 = AppConstants.getIMEI(WelcomeActivity.this) + ":" + userInfoEntity.PersonEmail + ":" + "Other";
                String parm2 = "Authenticate:I:" + Constants.Latitude + "," + Constants.Longitude;


                System.out.println("parm1----" + parm1);
                System.out.println("parm2----" + parm2);

                String authString = "Basic " + AppConstants.convertStingToBase64(parm1);

                //resp = serverHandler.PostTextData(WelcomeActivity.this, AppConstants.webURL, parm2, authString);
                //----------------------------------------------------------------------------------

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(20, TimeUnit.SECONDS);
                client.setReadTimeout(20, TimeUnit.SECONDS);
                client.setWriteTimeout(20, TimeUnit.SECONDS);

                RequestBody body = RequestBody.create(ServerHandler.TEXT, parm2);
                Request request = new Request.Builder()
                        .url(AppConstants.webURL)
                        .post(body)
                        .addHeader("Authorization", authString)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

                //------------------------------

            } catch (Exception e) {

                System.out.println("Ex" + e.getMessage());
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " GetSSIDUsingLocationGateHub onPostExecute --Exception " + e);
                if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }

            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            try {

                serverSSIDList.clear();
                // BackgroundServiceKeepDataTransferAlive.SSIDList.clear();//clear SSIDList
                //AppConstants.DetailsServerSSIDList.clear();

                String errMsg = "";

                if (result != null && !result.isEmpty()) {

                    JSONObject jsonObjectSite = new JSONObject(result);
                    String ResponseMessageSite = jsonObjectSite.getString(AppConstants.RES_MESSAGE);
                    String userData = jsonObjectSite.getString(AppConstants.RES_DATA_USER);
                    JSONObject jsonObject = new JSONObject(userData);

                    if (ResponseMessageSite.equalsIgnoreCase("success")) {

                        IsGateHub = jsonObject.getString("IsGateHub");
                        IsStayOpenGate = jsonObject.getString("StayOpenGate");
                        boolean fa_data = Boolean.parseBoolean(jsonObject.getString("EnbDisHubForFA"));
                        boolean UseBarcode = Boolean.parseBoolean(jsonObject.getString("UseBarcode"));
                        boolean UseBarcodeForPersonnel = Boolean.parseBoolean(jsonObject.getString("UseBarcodeForPersonnel"));
                        boolean IsEnableServerForTLD = Boolean.parseBoolean(jsonObject.getString("IsEnableServerForTLD"));
                        CommonUtils.SaveDataInPrefForGatehub(WelcomeActivity.this, IsGateHub, IsStayOpenGate);
                        CommonUtils.FA_FlagSavePref(WelcomeActivity.this, fa_data, UseBarcode, IsEnableServerForTLD, UseBarcodeForPersonnel);

                        BackgroundServiceKeepDataTransferAlive.SSIDList.clear();//clear SSIDList

                        //BLE upgrade
                        String IsHFUpdate = jsonObject.getString("IsHFUpdate");
                        String IsLFUpdate = jsonObject.getString("IsLFUpdate");
                        String BLEVersion = jsonObject.getString("BLEVersion");
                        String BLEType = "";
                        if (IsHFUpdate.equals("Y")) {
                            BLEType = "HF";
                        } else if (IsLFUpdate.equals("Y")) {
                            BLEType = "LF";
                        }
                        String BLEFileLocation = jsonObject.getString("BLEFileLocation");

                        SharedPreferences sharedPref = getSharedPreferences("BLEUpgradeInfo", 0);
                        SharedPreferences.Editor editor1 = sharedPref.edit();
                        editor1.putString("IsLFUpdate", IsLFUpdate);
                        editor1.putString("IsHFUpdate", IsHFUpdate);
                        editor1.putString("BLEVersion", BLEVersion);
                        editor1.putString("BLEType", BLEType);
                        editor1.putString("BLEFileLocation", BLEFileLocation);
                        editor1.commit();

                        JSONArray Requests = jsonObjectSite.getJSONArray(AppConstants.RES_DATA_SSID);

                        if (Requests.length() > 0) {

                            for (int i = 0; i < Requests.length(); i++) {
                                JSONObject c = Requests.getJSONObject(i);

                                String SiteId = c.getString("SiteId");
                                String SiteNumber = c.getString("SiteNumber");
                                String SiteName = c.getString("SiteName");
                                String SiteAddress = c.getString("SiteAddress");
                                String Latitude = c.getString("Latitude");
                                String Longitude = c.getString("Longitude");
                                String HoseId = c.getString("HoseId");
                                String HoseNumber = c.getString("HoseNumber");
                                String WifiSSId = c.getString("WifiSSId");
                                String UserName = c.getString("UserName");
                                String Password = c.getString("Password");
                                String ResponceMessage = c.getString("ResponceMessage");
                                String ResponceText = c.getString("ResponceText");
                                String ReplaceableHoseName = c.getString("ReplaceableHoseName");
                                String IsHoseNameReplaced = c.getString("IsHoseNameReplaced");
                                String BTMacAddress = c.getString("BluetoothMacAddress");
                                String MacAddress = c.getString("MacAddress");
                                String IsBusy = c.getString("IsBusy");
                                String IsUpgrade = c.getString("IsUpgrade");
                                String FirmwareVersion = c.getString("FirmwareVersion");
                                String PulserTimingAdjust = c.getString("PulserTimingAdjust");
                                String BluetoothCardReaderHF = c.getString("BluetoothCardReaderHF");
                                String IsDefective = c.getString("IsDefective");
                                String FilePath = c.getString("FilePath");
                                String ReconfigureLink = c.getString("ReconfigureLink");
                                String FSNPMacAddress = c.getString("FSNPMacAddress");
                                String IsTLDCall = c.getString("IsTLDCall");
                                String TLDFirmwareFilePath = c.getString("TLDFirmwareFilePath");
                                String TLDFIrmwareVersion = c.getString("TLDFIrmwareVersion");
                                String PROBEMacAddress = c.getString("PROBEMacAddress");
                                String IsTLDFirmwareUpgrade = c.getString("IsTLDFirmwareUpgrade");
                                String ScheduleTankReading = c.getString("ScheduleTankReading");
                                String LinkCommunicationType = c.getString("HubLinkCommunication");
                                String IsTankEmpty = c.getString("IsTankEmpty");
                                String IsLinkFlagged = c.getString("IsLinkFlagged");
                                String LinkFlaggedMessage = c.getString("LinkFlaggedMessage");
                                String IsResetSwitchTimeBounce = c.getString("IsResetSwitchTimeBounce");
                                String FirmwareFileName = c.getString("FirmwareFileName");

                                AppConstants.UP_FilePath = FilePath;

                                AppConstants.BT_READER_NAME = BluetoothCardReaderHF;

                                SetBTLinksMacAddress(i, BTMacAddress);

                                //Check prefix required for link name
                                /*if (!ReplaceableHoseName.startsWith("FS-")) {
                                    ReplaceableHoseName = "FS-" + ReplaceableHoseName;
                                }*/

                                //Current Fs wifi password
                                Constants.CurrFsPass = Password;

                                HashMap<String, String> map = new HashMap<>();
                                map.put("SiteId", SiteId);
                                map.put("HoseId", HoseId);
                                map.put("WifiSSId", WifiSSId);
                                map.put("ReplaceableHoseName", ReplaceableHoseName);
                                map.put("IsHoseNameReplaced", IsHoseNameReplaced);
                                map.put("item", WifiSSId);
                                map.put("MacAddress", MacAddress);
                                map.put("BTMacAddress", BTMacAddress);
                                map.put("IsBusy", IsBusy);
                                map.put("IsUpgrade", IsUpgrade);
                                map.put("UPFilePath", FilePath);
                                map.put("FirmwareVersion", FirmwareVersion);
                                map.put("PulserTimingAdjust", PulserTimingAdjust);
                                map.put("ReconfigureLink", ReconfigureLink);
                                map.put("FSNPMacAddress", FSNPMacAddress);
                                map.put("IsTLDCall", IsTLDCall);
                                map.put("TLDFirmwareFilePath", TLDFirmwareFilePath);
                                map.put("TLDFIrmwareVersion", TLDFIrmwareVersion);
                                map.put("PROBEMacAddress", PROBEMacAddress);
                                map.put("IsTLDFirmwareUpgrade", IsTLDFirmwareUpgrade);
                                map.put("ScheduleTankReading", ScheduleTankReading);
                                map.put("LinkCommunicationType", LinkCommunicationType);
                                map.put("IsTankEmpty", IsTankEmpty);
                                map.put("IsLinkFlagged", IsLinkFlagged);
                                map.put("LinkFlaggedMessage", LinkFlaggedMessage);
                                map.put("IsResetSwitchTimeBounce", IsResetSwitchTimeBounce);
                                map.put("FirmwareFileName", FirmwareFileName);

                                if (ResponceMessage.equalsIgnoreCase("success")) {

                                    if (isNotNULL(SiteId) && isNotNULL(HoseId) && isNotNULL(WifiSSId)) {
                                        serverSSIDList.add(map);
                                        AppConstants.DetailsServerSSIDList = serverSSIDList;
                                        BackgroundServiceKeepDataTransferAlive.SSIDList = serverSSIDList;

                                    }
                                } else {
                                    errMsg = ResponceText;
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "GetSSIDUsingLocationGateHub SSIDData (" + WifiSSId + "): " + ResponceText);
                                    AppConstants.AlertDialogFinish(WelcomeActivity.this, ResponceText);
                                }
                            }

                        }
                        try {

                            if (serverSSIDList != null && serverSSIDList.size() == 1 && IsGateHub.equalsIgnoreCase("True") && Constants.FS_1STATUS.equalsIgnoreCase("FREE")) {

                                //Thread.sleep(1000);
                                try {
                                    String SSID_mac = serverSSIDList.get(0).get("MacAddress");
                                    String selSSID = serverSSIDList.get(0).get("WifiSSId");
                                    String BTMacAddress = serverSSIDList.get(0).get("BTMacAddress");
                                    String ReconfigureLink = serverSSIDList.get(0).get("ReconfigureLink");
                                    AppConstants.SITE_ID = serverSSIDList.get(0).get("SiteId");
                                    String LinkCommunicationType = serverSSIDList.get(0).get("LinkCommunicationType");
                                    if (ReconfigureLink == null) {
                                        ReconfigureLink = "";
                                    }

                                    if (LinkCommunicationType.equalsIgnoreCase("HTTP")) {

                                        String Chk_ip = "";
                                        if (AppConstants.DetailsListOfConnectedDevices != null && AppConstants.DetailsListOfConnectedDevices.size() > 0)
                                            Chk_ip = AppConstants.DetailsListOfConnectedDevices.get(0).get("ipAddress");
                                        else {
                                            getipOverOSVersion();
                                        }

                                        if (Chk_ip != null && Chk_ip.length() > 3 && !ReconfigureLink.equalsIgnoreCase("true")) {

                                            boolean isMacConnected = false;
                                            if (AppConstants.DetailsListOfConnectedDevices != null) {
                                                for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                                    String Chk_mac = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");

                                                    if (SSID_mac.equalsIgnoreCase(Chk_mac)) {
                                                        if (AppConstants.GenerateLogs)
                                                            AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + SSID_mac + ") is connected to hotspot.");
                                                        IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                                                        isMacConnected = true;
                                                        break;
                                                    }
                                                }
                                            }

                                            if (!isMacConnected) {
                                                if (AppConstants.GenerateLogs)
                                                    AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + SSID_mac + ") is not found in connected devices. " + AppConstants.DetailsListOfConnectedDevices);

                                                for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                                    String Chk_mac = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                                                    if (AppConstants.GenerateLogs)
                                                        AppConstants.WriteinFile(TAG + "Checking Mac Address using info command: (" + Chk_mac + ")");

                                                    String connectedIp = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");

                                                    IpAddress = GetAndCheckMacAddressFromInfoCommand(connectedIp, SSID_mac, Chk_mac);
                                                    if (!IpAddress.trim().isEmpty()) {
                                                        if (AppConstants.GenerateLogs)
                                                            AppConstants.WriteinFile("===================================================================");
                                                        break;
                                                    }
                                                    if (AppConstants.GenerateLogs)
                                                        AppConstants.WriteinFile("===================================================================");
                                                }
                                            }
                                            if (!IpAddress.trim().isEmpty()) {
                                                SelectedItemPos = 0;
                                                tvSSIDName.setText(serverSSIDList.get(0).get("WifiSSId"));
                                                OnHoseSelected_OnClick(Integer.toString(0));
                                                goButtonAction(null);
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Auto select fail", Toast.LENGTH_SHORT).show();
                                            if (AppConstants.GenerateLogs)
                                                AppConstants.WriteinFile(TAG + "Auto select fail");
                                        }
                                    } else if (LinkCommunicationType.equalsIgnoreCase("BT")) {
                                        SelectedItemPos = 0;
                                        tvSSIDName.setText(selSSID);
                                        OnHoseSelected_OnClick(Integer.toString(0));
                                        GoButtonFunctionalityForSingleLink(LinkCommunicationType);
                                        //goButtonAction(null);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } else if (serverSSIDList != null && serverSSIDList.size() == 1 && Constants.FS_1STATUS.equalsIgnoreCase("FREE")) {

                                try {
                                    AppConstants.IsSingleLink = true;
                                    String SSID_mac = serverSSIDList.get(0).get("MacAddress");
                                    String ReconfigureLink = serverSSIDList.get(0).get("ReconfigureLink");
                                    AppConstants.SITE_ID = serverSSIDList.get(0).get("SiteId");
                                    String LinkCommunicationType = serverSSIDList.get(0).get("LinkCommunicationType");
                                    String BTselMacAddress = serverSSIDList.get(0).get("BTMacAddress");
                                    String selSSID = serverSSIDList.get(0).get("WifiSSId");
                                    if (ReconfigureLink == null) {
                                        ReconfigureLink = "";
                                    }

                                    if (LinkCommunicationType.equalsIgnoreCase("BT") && !ReconfigureLink.equalsIgnoreCase("true")) {
                                        //tvSSIDName.setText("Tap here to select hose");
                                        //btnGo.setVisibility(View.VISIBLE);
                                        SingleBTLinkSelection(selSSID, BTselMacAddress);

                                    } else {
                                        String Chk_ip = "";
                                        if (AppConstants.DetailsListOfConnectedDevices != null && AppConstants.DetailsListOfConnectedDevices.size() > 0) {
                                            Chk_ip = AppConstants.DetailsListOfConnectedDevices.get(0).get("ipAddress");
                                        } else {
                                            getipOverOSVersion();
                                        }

                                        if (Chk_ip != null && Chk_ip.length() > 3 && !ReconfigureLink.equalsIgnoreCase("true")) {

                                            boolean isMacConnected = false;
                                            if (AppConstants.DetailsListOfConnectedDevices != null) {
                                                for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                                    String Chk_mac = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");

                                                    if (SSID_mac.equalsIgnoreCase(Chk_mac)) {
                                                        if (AppConstants.GenerateLogs)
                                                            AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + SSID_mac + ") is connected to hotspot.");
                                                        IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                                                        isMacConnected = true;
                                                        break;
                                                    }
                                                }
                                            }

                                            if (!isMacConnected) {
                                                if (AppConstants.GenerateLogs)
                                                    AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + SSID_mac + ") is not found in connected devices. " + AppConstants.DetailsListOfConnectedDevices);

                                                for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                                    String Chk_mac = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                                                    if (AppConstants.GenerateLogs)
                                                        AppConstants.WriteinFile(TAG + "Checking Mac Address using info command: (" + Chk_mac + ")");

                                                    String connectedIp = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");

                                                    IpAddress = GetAndCheckMacAddressFromInfoCommand(connectedIp, SSID_mac, Chk_mac);
                                                    if (!IpAddress.trim().isEmpty()) {
                                                        if (AppConstants.GenerateLogs)
                                                            AppConstants.WriteinFile("===================================================================");
                                                        break;
                                                    }
                                                    if (AppConstants.GenerateLogs)
                                                        AppConstants.WriteinFile("===================================================================");
                                                }
                                            }

                                            if (!IpAddress.trim().isEmpty()) {
                                                SelectedItemPos = 0;
                                                tvSSIDName.setText(serverSSIDList.get(0).get("WifiSSId"));
                                                OnHoseSelected_OnClick(Integer.toString(0));
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Auto select fail", Toast.LENGTH_SHORT).show();
                                            if (AppConstants.GenerateLogs)
                                                AppConstants.WriteinFile(TAG + "Auto select fail");
                                        }
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        } catch (Exception e) {
                            System.out.println(e);
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "  GetSSIDUsingLocationGateHub if only one hose autoselect   --Exception " + e);
                        }


                    } else if (ResponseMessageSite.equalsIgnoreCase("fail")) {
                        String ResponseTextSite = jsonObjectSite.getString(AppConstants.RES_TEXT);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "GetSSIDUsingLocationGateHub SSIDData response fail. Error: " + ResponseTextSite);
                        AppConstants.AlertDialogBox(WelcomeActivity.this, ResponseTextSite);

                    }
                } else {

                    if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                        AppConstants.NETWORK_STRENGTH = false;
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Temporary loss of cell service ~Switching to offline mode 2"); // today**
                    }

                    new GetOfflineSSIDUsingLocation().execute();
                }


            } catch (Exception e) {

                CommonUtils.LogMessage(TAG, "GetSSIDUsingLocationGateHub :" + result, e);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "GetSSIDUsingLocationGateHub --Exception " + e);
                if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }

            }

        }
    }

    public void SyncSqliteData() {

        if (WelcomeActivity.OnWelcomeActivity && Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE") && Constants.FS_5STATUS.equalsIgnoreCase("FREE") && Constants.FS_6STATUS.equalsIgnoreCase("FREE")) {

            if (cd.isConnecting()) {

                try {
                    //sync offline transactions
                    String off_json = offcontroller.getAllOfflineTransactionJSON(WelcomeActivity.this);
                    JSONObject jobj = new JSONObject(off_json);
                    String offtransactionArray = jobj.getString("TransactionsModelsObj");
                    JSONArray jarrsy = new JSONArray(offtransactionArray);

                    if (jarrsy.length() > 0 && OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                        startService(new Intent(WelcomeActivity.this, OffTranzSyncService.class));
                    } else {
                        stopService(new Intent(WelcomeActivity.this, OffTranzSyncService.class));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //sync online transaction
            if (cd.isConnecting()) {
                DBController controller = new DBController(WelcomeActivity.this);
                ArrayList<HashMap<String, String>> uData = controller.getAllTransaction();

                if (uData != null && uData.size() > 0) {
                    startService(new Intent(WelcomeActivity.this, BackgroundService.class));
                    System.out.println("BackgroundService Start...");
                } else {
                    stopService(new Intent(WelcomeActivity.this, BackgroundService.class));
                    System.out.println("BackgroundService STOP...");
                }

            }
        }
    }


    public void BatteryPercentageService() {

        Calendar cal = Calendar.getInstance();
        Intent name = new Intent(WelcomeActivity.this, BatteryBackgroundService.class);
        PendingIntent pintent = PendingIntent.getService(getApplicationContext(), 0, name, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 1800000, pintent);

        //1000 s
        //60000 m
        //3600000 1h
        //1800000  30min
    }

    public class GetOfflineSSIDUsingLocation extends AsyncTask<Void, Void, String> {


        ProgressDialog pd;

        @Override
        protected void onPreExecute() {


            String s = "Please wait...";
            SpannableString ss2 = new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage(ss2);
            pd.setCancelable(false);
            pd.show();


        }

        protected String doInBackground(Void... arg0) {
            String resp = "";

            try {

                serverSSIDList = offcontroller.getAllLinks();

            } catch (Exception e) {
                hoseClicked = false;
                pd.dismiss();
                System.out.println("Ex" + e.getMessage());
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  GetSSIDUsingLocation offline --Exception " + e);
            }


            return resp;
        }


        @Override
        protected void onPostExecute(String result) {

            hoseClicked = false;

            pd.dismiss();

            linearHose.setClickable(true);//Enable hose Selection

            try {

                if (serverSSIDList.size() > 0) {
                    //HoseList Alert
                    alertSelectHoseList(tvLatLng.getText().toString() + "\n" + "");
                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Offline SSIDData is Empty. Error: " + R.string.conn_error);
                    AppConstants.AlertDialogBoxCanecl(WelcomeActivity.this, R.string.conn_error);
                }

                AppConstants.temp_serverSSIDList = serverSSIDList;
            } catch (Exception e) {


                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  GetSSIDUsingLocation offline onPostExecute --Exception " + e);
            }

        }
    }


    public void oo_post_getssid(String result) {
        linearHose.setClickable(true);//Enable hose Selection
        tvLatLng.setText("Current Location :" + Constants.Latitude + "," + Constants.Longitude);
        System.out.println("GetSSIDUsingLocation...." + result);

        try {
            serverSSIDList.clear();
            //AppConstants.DetailsServerSSIDList.clear();
            String errMsg = "";
            if (result != null && !result.isEmpty()) {

                JSONObject jsonObjectSite = new JSONObject(result);
                String ResponseMessageSite = jsonObjectSite.getString(AppConstants.RES_MESSAGE);
                String userData = jsonObjectSite.getString(AppConstants.RES_DATA_USER);
                JSONObject jsonObject = new JSONObject(userData);

                if (ResponseMessageSite.equalsIgnoreCase("success")) {

                    IsGateHub = jsonObject.getString("IsGateHub");
                    IsStayOpenGate = jsonObject.getString("StayOpenGate");
                    boolean fa_data = Boolean.parseBoolean(jsonObject.getString("EnbDisHubForFA"));
                    boolean UseBarcode = Boolean.parseBoolean(jsonObject.getString("UseBarcode"));
                    boolean UseBarcodeForPersonnel = Boolean.parseBoolean(jsonObject.getString("UseBarcodeForPersonnel"));
                    boolean IsEnableServerForTLD = Boolean.parseBoolean(jsonObject.getString("IsEnableServerForTLD"));

                    CommonUtils.SaveDataInPrefForGatehub(WelcomeActivity.this, IsGateHub, IsStayOpenGate);
                    CommonUtils.FA_FlagSavePref(WelcomeActivity.this, fa_data, UseBarcode, IsEnableServerForTLD, UseBarcodeForPersonnel);

                    JSONArray Requests = jsonObjectSite.getJSONArray(AppConstants.RES_DATA_SSID);

                    if (Requests.length() > 0) {

                        for (int i = 0; i < Requests.length(); i++) {
                            JSONObject c = Requests.getJSONObject(i);

                            String SiteId = c.getString("SiteId");
                            String SiteNumber = c.getString("SiteNumber");
                            String SiteName = c.getString("SiteName");
                            String SiteAddress = c.getString("SiteAddress");
                            String Latitude = c.getString("Latitude");
                            String Longitude = c.getString("Longitude");
                            String HoseId = c.getString("HoseId");
                            String HoseNumber = c.getString("HoseNumber");
                            String WifiSSId = c.getString("WifiSSId");
                            String UserName = c.getString("UserName");
                            String Password = c.getString("Password");
                            String ResponceMessage = c.getString("ResponceMessage");
                            String ResponceText = c.getString("ResponceText");
                            String ReplaceableHoseName = c.getString("ReplaceableHoseName");
                            String IsHoseNameReplaced = c.getString("IsHoseNameReplaced");
                            String BTMacAddress = c.getString("BluetoothMacAddress");
                            String MacAddress = c.getString("MacAddress");
                            String IsBusy = c.getString("IsBusy");
                            String IsUpgrade = c.getString("IsUpgrade");
                            String FirmwareVersion = c.getString("FirmwareVersion");
                            String PulserTimingAdjust = c.getString("PulserTimingAdjust");
                            String IsDefective = c.getString("IsDefective");
                            String ReconfigureLink = c.getString("ReconfigureLink");
                            String FSNPMacAddress = c.getString("FSNPMacAddress");
                            String IsTLDCall = c.getString("IsTLDCall");
                            String PROBEMacAddress = c.getString("PROBEMacAddress");
                            String LinkCommunicationType = c.getString("HubLinkCommunication");
                            String IsTankEmpty = c.getString("IsTankEmpty");
                            String IsLinkFlagged = c.getString("IsLinkFlagged");
                            String LinkFlaggedMessage = c.getString("LinkFlaggedMessage");
                            String IsResetSwitchTimeBounce = c.getString("IsResetSwitchTimeBounce");
                            String FirmwareFileName = c.getString("FirmwareFileName");

                            ///tld upgrade
                            String IsTLDFirmwareUpgrade = c.getString("IsTLDFirmwareUpgrade");
                            String TLDFirmwareFilePath = c.getString("TLDFirmwareFilePath");
                            String TLDFIrmwareVersion = c.getString("TLDFIrmwareVersion");

                            String ScheduleTankReading = c.getString("ScheduleTankReading");

                            //Check prefix required for link name
                            /*if (!ReplaceableHoseName.startsWith("FS-")) {
                                ReplaceableHoseName = "FS-" + ReplaceableHoseName;
                            }*/

                            SetBTLinksMacAddress(i, BTMacAddress);

                            String FilePath = c.getString("FilePath");
                            AppConstants.UP_FilePath = FilePath;

                            //Current Fs wifi password
                            Constants.CurrFsPass = Password;

                            HashMap<String, String> map = new HashMap<>();
                            map.put("SiteId", SiteId);
                            map.put("HoseId", HoseId);
                            map.put("WifiSSId", WifiSSId);
                            map.put("ReplaceableHoseName", ReplaceableHoseName);
                            map.put("IsHoseNameReplaced", IsHoseNameReplaced);
                            map.put("item", WifiSSId);
                            map.put("MacAddress", MacAddress);
                            map.put("BTMacAddress", BTMacAddress);
                            map.put("IsBusy", IsBusy);
                            map.put("IsUpgrade", IsUpgrade);
                            map.put("UPFilePath", FilePath);
                            map.put("FirmwareVersion", FirmwareVersion);
                            map.put("PulserTimingAdjust", PulserTimingAdjust);
                            map.put("IsDefective", IsDefective);
                            map.put("ReconfigureLink", ReconfigureLink);
                            map.put("FSNPMacAddress", FSNPMacAddress);
                            map.put("IsTLDCall", IsTLDCall);
                            map.put("IsTLDFirmwareUpgrade", IsTLDFirmwareUpgrade);
                            map.put("TLDFirmwareFilePath", TLDFirmwareFilePath);
                            map.put("TLDFIrmwareVersion", TLDFIrmwareVersion);
                            map.put("PROBEMacAddress", PROBEMacAddress);
                            map.put("ScheduleTankReading", ScheduleTankReading);
                            map.put("LinkCommunicationType", LinkCommunicationType);
                            map.put("IsTankEmpty", IsTankEmpty);
                            map.put("IsLinkFlagged", IsLinkFlagged);
                            map.put("LinkFlaggedMessage", LinkFlaggedMessage);
                            map.put("IsResetSwitchTimeBounce", IsResetSwitchTimeBounce);
                            map.put("FirmwareFileName", FirmwareFileName);

                            System.out.println("WifiSSId-" + WifiSSId);
                            System.out.println("IsTLDFirmwareUpgrade-" + IsTLDFirmwareUpgrade);
                            System.out.println("TLDFirmwareFilePath-" + TLDFirmwareFilePath);

                            if (IsTLDFirmwareUpgrade.trim().toLowerCase().equalsIgnoreCase("y")) {
                                downloadTLD_BinFile(i, TLDFirmwareFilePath, TLDFIrmwareVersion);
                            }

                            if (ResponceMessage.equalsIgnoreCase("success")) {
                                if (isNotNULL(SiteId) && isNotNULL(HoseId) && isNotNULL(WifiSSId)) {
                                    serverSSIDList.add(map);
                                    AppConstants.DetailsServerSSIDList = serverSSIDList;

                                }
                            } else {
                                errMsg = ResponceText;
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "SSIDData (" + WifiSSId + "): " + ResponceText);
                                AppConstants.AlertDialogFinish(WelcomeActivity.this, ResponceText);
                            }
                        }

                        //HoseList Alert
                        alertSelectHoseList(tvLatLng.getText().toString() + "\n" + errMsg);
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "SSIDData is Empty. Error: " + R.string.conn_error);
                        AppConstants.AlertDialogBoxCanecl(WelcomeActivity.this, R.string.conn_error);
                    }

                    AppConstants.temp_serverSSIDList = serverSSIDList;

                } else if (ResponseMessageSite.equalsIgnoreCase("fail")) {
                    String ResponseTextSite = jsonObjectSite.getString(AppConstants.RES_TEXT);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "SSIDData response fail. Error: " + ResponseTextSite);
                    AppConstants.AlertDialogBox(WelcomeActivity.this, ResponseTextSite);
                }
            } else {

                if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Temporary loss of cell service ~Switching to offline mode 3"); //today***

                    new GetOfflineSSIDUsingLocation().execute();
                }

            }


        } catch (Exception e) {

            CommonUtils.LogMessage(TAG, "GetSSIDUsingLocation :" + result, e);
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "GetSSIDUsingLocation onPostExecute --Exception " + e);
        }

    }

    public boolean checkFuelTimings(String SiteId) {

        boolean inTime = false;

        ArrayList<HashMap<String, String>> timings = offcontroller.getFuelTimingsBySiteId(SiteId);

        if (timings != null && timings.size() > 0) {
            for (int i = 0; i < timings.size(); i++) {
                try {
                    String FromTime = timings.get(i).get("FromTime");
                    String ToTime = timings.get(i).get("ToTime");

                    FromTime = FromTime.replace(".", ":");
                    ToTime = ToTime.replace(".", ":");

                    if (FromTime.contains(":") && ToTime.contains(":")) {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

                        Date time1 = sdf.parse(FromTime);
                        Calendar calendar1 = Calendar.getInstance();
                        calendar1.setTime(time1);


                        Date time2 = sdf.parse(ToTime);
                        Calendar calendar2 = Calendar.getInstance();
                        calendar2.setTime(time2);

                        Calendar cal = Calendar.getInstance();
                        String currenthour = sdf.format(cal.getTime());

                        Date d = sdf.parse(currenthour);
                        Calendar calendar3 = Calendar.getInstance();
                        calendar3.setTime(d);

                        System.out.println(currenthour);


                        Date x = calendar3.getTime();
                        if (x.after(calendar1.getTime()) && x.before(calendar2.getTime())) {

                            inTime = true;
                            System.out.println(FromTime + " " + true + " " + ToTime);
                            break;
                        } else {
                            inTime = false;
                            System.out.println(FromTime + " " + false + " " + ToTime);
                        }

                    }


                } catch (Exception e) {
                    System.out.println("FromTime-" + e.getMessage());
                }
            }
        }

        return inTime;
    }


    public boolean checkFuelingDay(String AuthorizedFuelingDays) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");

        Calendar cal = Calendar.getInstance();
        String todayDay = sdf.format(cal.getTime());

        System.out.println("AuthorizedFuelingDays--" + AuthorizedFuelingDays);
        System.out.println("todayDay--" + todayDay);

        if (AuthorizedFuelingDays == null)
            return true;

        if (AuthorizedFuelingDays.toLowerCase().contains("select all"))
            return true;

        if (AuthorizedFuelingDays.toLowerCase().trim().contains(todayDay.toLowerCase()))
            return true;
        else
            return false;

    }


    public class GetOfflineSSIDUsingLocationOnResume extends AsyncTask<Void, Void, String> {


        ProgressDialog pd;

        @Override
        protected void onPreExecute() {

            String s = "Please wait...";
            SpannableString ss2 = new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage(ss2);
            pd.setCancelable(true);
            pd.setCancelable(false);
            pd.show();

        }

        protected String doInBackground(Void... arg0) {


            try {
                serverSSIDList = offcontroller.getAllLinks();

            } catch (Exception e) {
                pd.dismiss();

            }

            return "";
        }


        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();

            try {

                AppConstants.DetailsServerSSIDList = serverSSIDList;
                BackgroundServiceKeepDataTransferAlive.SSIDList = serverSSIDList;
                AppConstants.temp_serverSSIDList = serverSSIDList;

                /*if (serverSSIDList != null && serverSSIDList.size() == 1) {
                    SetSSIDIfSingleHose();
                }*/

            } catch (Exception e) {


                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  Offline --Exception " + e);

            }

        }
    }

    private void ConfigureTld() {

        SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_TldDetails, Context.MODE_PRIVATE);
        String LinkMacAddress = sharedPref.getString("selMacAddress", "");

        if (!LinkMacAddress.isEmpty()) {

            Intent serviceIntent = new Intent(WelcomeActivity.this, DeviceControlActivity_tld.class);
            startService(serviceIntent);

        } else {
            AppConstants.colorToastBigFont(getApplicationContext(), "Please select link and try..", Color.RED);
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " ConfigureTld Please select link and try..");
        }

    }

    private void DebugWindow() {

        if (FA_DebugWindow) {
            linear_debug_window.setVisibility(View.VISIBLE);
            new CountDownTimer(30000, 30000) {

                public void onTick(long millisUntilFinished) {
                    //here you can have your logic to set text to edittext
                }

                public void onFinish() {
                    FA_DebugWindow = false;
                    linear_debug_window.setVisibility(View.GONE);
                }

            }.start();

        } else {
            linear_debug_window.setVisibility(View.GONE);
        }
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void ReconfigureCountdown() {

        final Handler handler = new Handler();
        time_cd = new Timer("Timer");
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {

                            System.out.println("Enable flag........!!");
                            Constants.hotspotstayOn = true;
                            time_cd.cancel();

                        } catch (Exception e) {
                        }
                    }
                });
            }
        };
        time_cd.schedule(doAsynchronousTask, 0, 600000); // 600000 execute in every 10 minutes


    }

    public void CallJobSchedular() {


        /*JobScheduler mJobScheduler;
        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {


            mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(getPackageName(), JobSchedulerService.class.getName()));
            builder.setPeriodic(900000);//3600000  900000
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            builder.setPersisted(true);
            // builder.setRequiresDeviceIdle(true);
            mJobScheduler.schedule(builder.build());

        }*/
    }

    private void RemoveWifiNetworks() {

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            //int networkId = wifiManager.getConnectionInfo().getNetworkId();
            wifiManager.removeNetwork(i.networkId);
            wifiManager.saveConfiguration();
        }

    }

    private void ReconfigureManually() {

        if (AppConstants.ManuallReconfigure && Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE")) {

            WifiManager wifiManager = (WifiManager) WelcomeActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int NetID = wifiInfo.getNetworkId();
            String ssid = wifiInfo.getSSID();

            if (ssid.contains(AppConstants.SELECTED_SSID_FOR_MANUALL)) {

                AppConstants.colorToastBigFont(WelcomeActivity.this, "Manually connected ssid match", Color.BLUE);
                //new WiFiConnectTask().execute();

            } else {

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Manually connected ssid did not match " + AppConstants.CURRENT_SELECTED_SSID + " and try..!! ");

            }
        }
    }


    public void saveLinkMacAddressForReconfigure(String jsonData) {
        SharedPreferences sharedPref = WelcomeActivity.this.getSharedPreferences(Constants.MAC_ADDR_RECONFIGURE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("jsonData", jsonData);
        editor.commit();

    }

    public void clearMacAddressSharedPref() {
        SharedPreferences preferences = getSharedPreferences(Constants.MAC_ADDR_RECONFIGURE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }


    @TargetApi(21)
    public void setGlobalMobileDatConnection() {

        NetworkRequest.Builder requestbuilder = new NetworkRequest.Builder();
        requestbuilder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);

        connection_manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);


        connection_manager.requestNetwork(requestbuilder.build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {


                System.out.println(" network......." + network);

                if (SDK_INT >= Build.VERSION_CODES.M) {
                    connection_manager.bindProcessToNetwork(network);

                }
            }
        });
    }

    @TargetApi(21)
    private void setGlobalWifiConnection() {

        NetworkRequest.Builder requestbuilder = new NetworkRequest.Builder();
        requestbuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);

        connection_manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);


        connection_manager.requestNetwork(requestbuilder.build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {


                if (SDK_INT >= Build.VERSION_CODES.M) {
                    connection_manager.bindProcessToNetwork(network);

                }
            }
        });
    }

    private boolean InabilityToConnectLink(String selSSID) {

        boolean linkDefective = false;

        if (BackgroundServiceKeepDataTransferAlive.DefectiveLinks != null && BackgroundServiceKeepDataTransferAlive.DefectiveLinks.size() > 0) {

            for (int i = 0; i < BackgroundServiceKeepDataTransferAlive.DefectiveLinks.size(); i++) {

                String selssid = BackgroundServiceKeepDataTransferAlive.DefectiveLinks.get(i).get("Selected_SSID");
                int diff = Integer.parseInt(BackgroundServiceKeepDataTransferAlive.DefectiveLinks.get(i).get("diff_min"));

                if (selSSID.equals(selssid) && diff > 60) {
                    AppConstants.colorToastBigFont(WelcomeActivity.this, " Inability To Connect Link. \nPlease call customer support for assistance", Color.RED);
                    linkDefective = true;
                    break;
                } else {
                    linkDefective = false;
                    break;
                }
            }

        }

        return linkDefective;
    }


    public void alertHotspotOnOffAfterReconfigure() {
        String s = "Hotspot is turning On. Please wait...";
        SpannableString ss2 = new SpannableString(s);
        ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
        ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);

        loading = new ProgressDialog(WelcomeActivity.this);
        loading.setCancelable(false);
        loading.setMessage(ss2);
        loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loading.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                wifiApManager.setWifiApEnabled(null, false);
            }
        }, 1000);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                wifiApManager.setWifiApEnabled(null, true);
                ChangeWifiState(false);
                getipOverOSVersion();
            }
        }, 2000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                wifiApManager.setWifiApEnabled(null, false);
            }
        }, 3000);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                wifiApManager.setWifiApEnabled(null, true);
                ChangeWifiState(false);
                getipOverOSVersion();
            }
        }, 4000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                wifiApManager.setWifiApEnabled(null, false);
            }
        }, 5000);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loading.dismiss();
                wifiApManager.setWifiApEnabled(null, true);
                ChangeWifiState(false);
                getipOverOSVersion();
            }
        }, 6000);

    }

    public void CustomDilaogForDebugWindow(final Activity context, String title, String message) {

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
                    if (FA_DebugWindow) {
                        FA_DebugWindow = false;
                    } else {
                        FA_DebugWindow = true;
                        dialogBus.dismiss();
                        onResume();
                    }
                } else {
                    if (!code.equals(AppConstants.AccessCode)) {
                        Toast.makeText(WelcomeActivity.this, "Code did not match. Please try again", Toast.LENGTH_SHORT).show();
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

    private void UpdateDiffStatusMessages(String stopButtonSequence) {

        try {

            String StopPressedStatus = "5";
            String TransactionId_US = null;
            SharedPreferences sharedPref = WelcomeActivity.this.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
            if (stopButtonSequence.equalsIgnoreCase("0")) {
                TransactionId_US = sharedPref.getString("TransactionId_FS1", "");
            } else if (stopButtonSequence.equalsIgnoreCase("1")) {
                TransactionId_US = sharedPref.getString("TransactionId", "");
            } else if (stopButtonSequence.equalsIgnoreCase("2")) {
                TransactionId_US = sharedPref.getString("TransactionId_FS3", "");
            } else if (stopButtonSequence.equalsIgnoreCase("3")) {
                TransactionId_US = sharedPref.getString("TransactionId_FS4", "");
            } else {
                //Something went wrong in link selection
                Log.i(TAG, "Something went wrong in link selection");
            }

            if (TransactionId_US != null && !TransactionId_US.isEmpty() && cd.isConnectingToInternet()) {
                Log.i(TAG, "UpdateDiffStatusMessages sent: " + StopPressedStatus + " TransactionId:" + TransactionId_US);
                if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH)
                    CommonUtils.UpgradeTransactionStatusToSqlite(TransactionId_US, StopPressedStatus, this);
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "UpdateDiffStatusMessages Ex:" + e.toString());

        }

    }

    private void CustomDilaogExitApp(final Activity context, String title, String message) {

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
                    //Toast.makeText(AcceptVehicleActivity_new.this, "Done", Toast.LENGTH_SHORT).show();
                    dialogBus.dismiss();
                    finish();
                } else {
                    if (!code.equals(AppConstants.AccessCode)) {
                        Toast.makeText(WelcomeActivity.this, "Code did not match. Please try again", Toast.LENGTH_SHORT).show();
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

    private void reconfigureProcessBelowAndroid10() {

        if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE")) {

            try {

                AppConstants.enableHotspotManuallyWindow = false;
                Constants.hotspotstayOn = false; //hotspot enable/disable flag
                ReconfigureCountdown();
                wifiApManager.setWifiApEnabled(null, false);  //Disabled Hotspot
                //Enable wifi
                WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                wifiManagerMM.setWifiEnabled(true);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        countDownTimerForReconfigure = null;
                        countDownTimerForReconfigureFun();

                    }
                }, 3000);

            } catch (Exception e) {

                Constants.hotspotstayOn = true;
                Log.i(TAG, "Link ReConfiguration process -Step 1 Exception" + e);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Link ReConfiguration process -Step 1 Exception" + e);
            }


        } else {
            AppConstants.colorToastBigFont(WelcomeActivity.this, "Can't update mac address,Hose is busy please retry later.", Color.RED);
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Can't update mac address,Hose is busy please retry later.");
            btnGo.setVisibility(View.GONE);
        }


    }


    private void ChangeWifiState(boolean enable) {

        WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (enable) {
            //Enable wifi
            wifiManagerMM.setWifiEnabled(true);
        } else {
            //Disable wifi
            wifiManagerMM.setWifiEnabled(false);

        }

    }

    //Link Reconfigure code below
    private void LinkReConfigurationProcessStep1() {

        if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE")) {

            try {
                AppConstants.enableHotspotManuallyWindow = false;
                Constants.hotspotstayOn = false; //hotspot enable/disable flag

                //Enable wifi
                ChangeWifiState(true);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        wifiApManager.setWifiApEnabled(null, false); //one try for auto on
                        Log.i(TAG, "Step1 Link ReConfiguration enable wifi manually.");
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Step1 Link ReConfiguration enable wifi manually.");

                        AppConstants.SELECTED_SSID_FOR_MANUALL = AppConstants.CURRENT_SELECTED_SSID;//ReconfigSSID;
                        AppConstants.colorToastBigFont(getApplicationContext(), "Enable Wifi Manually and Connect to " + AppConstants.SELECTED_SSID_FOR_MANUALL + " using wifi list", Color.RED);
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        //mjconf
                        new CountDownTimer(12000, 6000) {

                            public void onTick(long millisUntilFinished) {

                                Log.i(TAG, "Step1 onTick");
                                WifiManager wifiManager = (WifiManager) WelcomeActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                String ssid = "";
                                if (wifiManager.isWifiEnabled()) {
                                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                                    ssid = wifiInfo.getSSID().trim().replace("\"", "");
                                }

                                /*ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);*/

                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + ssid + " =--= " + AppConstants.SELECTED_SSID_FOR_MANUALL); //+" IsWifi Connected: "+mWifi.isConnected()

                            }

                            public void onFinish() {

                                WifiManager wifiManager = (WifiManager) WelcomeActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                String ssid = "";
                                if (wifiManager.isWifiEnabled()) {
                                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                                    ssid = wifiInfo.getSSID().trim().replace("\"", "");
                                }

                                if (ssid.equalsIgnoreCase(AppConstants.SELECTED_SSID_FOR_MANUALL)) {

                                    //proced to reconfigure process
                                    Log.i(TAG, "Step1 onTick ssid connected" + ssid);
                                    AppConstants.colorToastBigFont(WelcomeActivity.this, "Connected to wifi " + AppConstants.SELECTED_SSID_FOR_MANUALL, Color.BLUE);
                                    Log.i(TAG, "Step1 Connected to wifi " + AppConstants.SELECTED_SSID_FOR_MANUALL);
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "Step1 Connected to wifi " + AppConstants.SELECTED_SSID_FOR_MANUALL);

                                    setGlobalWifiConnection();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            LinkReConfigurationProcessStep2();
                                        }
                                    }, 1000);
                                } else {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " Step1 onFinish SSID ==> " + ssid);
                                }
                            }

                        }.start();

                    }
                }, 1000);

            } catch (Exception e) {
                ChangeWifiState(false);//turn wifi off
                Constants.hotspotstayOn = true;
                Log.i(TAG, "Link ReConfiguration process -Step 1 Exception" + e);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Link ReConfiguration process -Step 1 Exception" + e);
            }
        } else {
            AppConstants.colorToastBigFont(WelcomeActivity.this, "Can't update mac address,Hose is busy please retry later.", Color.RED);
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Can't update mac address,Hose is busy please retry later.");
            btnGo.setVisibility(View.GONE);
        }
    }

    private void LinkReConfigurationProcessStep2() {

        try {

            HTTP_URL = "http://192.168.4.1:80/";
            URL_INFO = HTTP_URL + "client?command=info";

            setGlobalWifiConnection();
            String result = new CommandsGET_INFO().execute(URL_INFO).get();
            String mac_address = "";

            Log.i(TAG, "Step2 Link ReConfiguration INFO_Command result:" + result);
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Step2 Link ReConfiguration INFO_Command result:" + result);

            if (result.contains("Version")) {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject joPulsarStat = jsonObject.getJSONObject("Version");
                String sdk_version = joPulsarStat.getString("sdk_version");
                String iot_version = joPulsarStat.getString("iot_version");
                mac_address = joPulsarStat.getString("mac_address");//station_mac_address
                AppConstants.UPDATE_MACADDRESS = mac_address;

                if (mac_address.equals("")) {

                    AppConstants.colorToastBigFont(WelcomeActivity.this, "Reconfiguration process fail..\nCould not get mac address", Color.RED);
                    Log.i(TAG, "Step2 Reconfiguration process fail.. Could not get mac address Info command result:" + result);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Step2 Reconfiguration process fail.. Could not get mac address Info command result:" + result);

                    Constants.hotspotstayOn = true;//Enable hotspot flag
                    //Disable wifi connection
                    WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                    wifiManagerMM.setWifiEnabled(false);
                    //TODO Makesure Hotspot get On here

                } else {

                    //setGlobalWifiConnection();
                    //Set HUB usernam and password to link

                    AppConstants.colorToastBigFont(WelcomeActivity.this, "Setting SSID and PASS to Link", Color.BLUE);
                    Log.i(TAG, "Step2 Setting SSID and PASS to Link");
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Step2 Setting SSID and PASS to Link");

                    HTTP_URL = "http://192.168.4.1:80/";
                    URL_UPDATE_FS_INFO = HTTP_URL + "config?command=wifi";

                    String jsonChangeUsernamePass = "{\"Request\":{\"Station\":{\"Connect_Station\":{\"ssid\":\"" + AppConstants.HubName + "\",\"password\":\"" + AppConstants.HubGeneratedpassword + "\" ,\"sta_connect\":1 }}}}";
                    //String jsonChangeUsernamePass = "{\"Request\":{\"Station\":{\"Connect_Station\":{\"ssid\":\"" + AppConstants.HubName + "\",\"password\":\"" + AppConstants.HubGeneratedpassword + "\"}}}}";

                    new aboveAndroid9_ChangeHotspotSettings().execute(URL_UPDATE_FS_INFO, jsonChangeUsernamePass);

                }
            } else {

                ChangeWifiState(false);
                LinkReConfigurationProcessStep1();
                AppConstants.colorToastBigFont(WelcomeActivity.this, "Step2 Failed to get Info Command ", Color.BLUE);
                Log.i(TAG, "Step2 Failed to get Info Command ");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Step2 Failed to get Info Command ");

                //BackTo Welcome Activity temp comment
                Intent i = new Intent(WelcomeActivity.this, WelcomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                WelcomeActivity.this.startActivity(i);
            }

        } catch (Exception e) {

            ChangeWifiState(false);//turn wifi off
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "  WiFiConnectTask  OnPostExecution --Exception " + e);
        }
    }

    public void LinkReConfigurationProcessStep3(final Context context) {

        ChangeWifiState(false);//turn wifi off
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        wifiApManager.setWifiApEnabled(null, true); //one try for auto on
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (CommonUtils.isHotspotEnabled(context)) {
            LinkReConfigurationProcessStep4();
            //BackTo Welcome Activity
            Intent i = new Intent(context, WelcomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(i);
        } else {
            Log.i(TAG, "Step3 Hotspot is not turned on.");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Step3 Hotspot is not turned on.");
            // New code as per #1894 (Eva) Spare Parts Kit 7/19/22
            new CountDownTimer(5000, 1000) {

                public void onTick(long millisUntilFinished) {

                    if (CommonUtils.isHotspotEnabled(context)) {
                        cd = new ConnectionDetector(WelcomeActivity.this);
                        if (cd.isConnectingToInternet()) {
                            cancel();
                            LinkReConfigurationProcessStep4();
                            //BackTo Welcome Activity
                            Intent i = new Intent(context, WelcomeActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(i);
                        } else {
                            Log.i(TAG, "Step3 Hotspot enabled, But No Internet detected");
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Step3 Hotspot enabled, But No Internet detected");
                        }
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Step3 Waiting for enable hotspot: " + millisUntilFinished / 1000);
                    }
                }

                public void onFinish() {

                    if (CommonUtils.isHotspotEnabled(context)) {

                        cd = new ConnectionDetector(WelcomeActivity.this);
                        if (cd.isConnectingToInternet()) {
                            LinkReConfigurationProcessStep4();
                        } else {
                            Log.i(TAG, "Step3 Hotspot enabled, But No Internet detected");
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Step3 Hotspot enabled, But No Internet detected");
                        }
                    } else {
                        Log.i(TAG, "Step3 Failed to enable hotspot");
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Step3 Failed to enable hotspot");
                    }

                    //BackTo Welcome Activity
                    Intent i = new Intent(context, WelcomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(i);

                }
            }.start();

            // Removed below code as per #1894 (Eva) Spare Parts Kit 7/19/22
            /*Log.i(TAG, "Step3 Enable hotspot manually");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Step3 Enable hotspot manually");
            //AppConstants.colorToastHotspotOn(context, "Enable Mobile Hotspot Manually..", Color.RED);
            Intent tetherSettings = new Intent();//com.smartcom
            tetherSettings.setClassName("com.android.settings", "com.android.settings.TetherSettings");
            context.startActivity(tetherSettings);

            new CountDownTimer(300000, 1000) {

                public void onTick(long millisUntilFinished) {

                    if (CommonUtils.isHotspotEnabled(context)) {
                        Log.i(TAG, "Hotspot detected disable timer..");
                        cd = new ConnectionDetector(WelcomeActivity.this);
                        if (cd.isConnectingToInternet()) {
                            cancel();
                            LinkReConfigurationProcessStep4();
                            //BackTo Welcome Activity
                            Intent i = new Intent(context, WelcomeActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(i);
                        } else {
                            Log.i(TAG, "Step3 Hotspot enabled, But No Internet detected");
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Step3 Hotspot enabled, But No Internet detected");
                        }

                    } else {
                        if (millisUntilFinished > 5000)
                            //AppConstants.colorToastHotspotOn(context, "Please press  Mobile      ^     \nHotspot button.", Color.RED);  //\nWaiting seconds..." + millisUntilFinished / 1000
                            AppConstants.colorToastHotspotOn(context, "Please press  Mobile      ^     \nHotspot button.", ContextCompat.getColor(WelcomeActivity.this, R.color.HotspotOnToastColor), Color.BLACK);
                    }
                }

                public void onFinish() {

                    if (CommonUtils.isHotspotEnabled(context)) {

                        cd = new ConnectionDetector(WelcomeActivity.this);
                        if (cd.isConnectingToInternet()) {
                            LinkReConfigurationProcessStep4();
                        } else {
                            Log.i(TAG, "Step3- Hotspot enabled, But No Internet detected");
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Step3- Hotspot enabled, But No Internet detected");
                        }
                    } else {
                        Log.i(TAG, "Step3 Failed to enable hotspot");
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Step3 Failed to enable hotspot");
                    }

                    //BackTo Welcome Activity
                    Intent i = new Intent(context, WelcomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(i);

                }

            }.start();*/
        }
    }

    private void LinkReConfigurationProcessStep4() {

        AppConstants.colorToastBigFont(WelcomeActivity.this, "Mac address " + AppConstants.UPDATE_MACADDRESS, Color.BLUE);
        Log.i(TAG, "Step4 Mac address " + AppConstants.UPDATE_MACADDRESS);
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "Step4 Mac address " + AppConstants.UPDATE_MACADDRESS);

        //Update mac address to server and mac address status
        try {

            UpdateMacAddressClass authEntityClass1 = new UpdateMacAddressClass();
            authEntityClass1.SiteId = Integer.parseInt(AppConstants.CURRENT_SELECTED_SITEID);
            authEntityClass1.MACAddress = AppConstants.UPDATE_MACADDRESS;
            authEntityClass1.RequestFrom = "AP";
            authEntityClass1.HubName = AppConstants.HubName;

            Gson gson = new Gson();
            final String jsonData = gson.toJson(authEntityClass1);
            saveLinkMacAddressForReconfigure(jsonData);

            setGlobalMobileDatConnection();  //check if needed
            cd = new ConnectionDetector(WelcomeActivity.this);
            if (cd.isConnectingToInternet()) {

                if (ssid_pass_success.trim().isEmpty()) {
                    new aboveAndroid9_UpdateMacAsynTask().execute(jsonData);
                } else {
                    ssid_pass_success = "";
                }

            } else {
                Constants.hotspotstayOn = true;
                AppConstants.colorToastBigFont(WelcomeActivity.this, "No Internet while updating MacAddress to server. Please retry again.", Color.RED);
                Log.i(TAG, "Step4 No Internet while updating MacAddress to server. Please retry again.");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Step4 No Internet while updating MacAddress to server. Please retry again.");
            }

        } catch (Exception e) {
            Constants.hotspotstayOn = true;
            System.out.println(e);
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "  WiFiConnectTask  UpdateMacAddressClass --Exception " + e);
        }

    }

    public class aboveAndroid9_ChangeHotspotSettings extends AsyncTask<String, Void, String> {

        public String resp = "";

        protected String doInBackground(String... param) {

            try {

                MediaType JSON = MediaType.parse("application/json");
                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(20, TimeUnit.SECONDS);
                client.setReadTimeout(20, TimeUnit.SECONDS);
                client.setWriteTimeout(20, TimeUnit.SECONDS);

                RequestBody body = RequestBody.create(JSON, param[1]);
                Request request = new Request.Builder()
                        .url(param[0])
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {

                ChangeWifiState(false);//turn wifi off
                ssid_pass_success = "2";
                resp = "exception";

                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Set SSID and PASS to Link (Link reset) doInBackground -Exception " + e);
                if (OfflineConstants.isOfflineAccess(WelcomeActivity.this)) {
                }
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            try {

                if (result.equalsIgnoreCase("exception")) {

                    ChangeWifiState(false);//turn wifi off
                    AppConstants.colorToastBigFont(getApplicationContext(), "Step2 Failed while changing Hotspot Settings Please try again..", Color.RED);
                    Log.i(TAG, "Step2 Failed while changing Hotspot Settings Please try again.. exception:" + result);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Step2 Failed while changing Hotspot Settings aboveAndroid9. exception:" + result);

                    //mj
                    Intent intent = new Intent(WelcomeActivity.this, WelcomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {

                    //Constants.hotspotstayOn = true;//Enable hotspot flag temp prakash
                    //ChangeWifiState(false);//turn wifi off

                    LinkReConfigurationProcessStep3(WelcomeActivity.this);
                }

                System.out.println(result);
                Log.i(TAG, " Set SSID and PASS to Link (Result" + result);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Set SSID and PASS to Link Result" + result);

            } catch (Exception e) {

                ChangeWifiState(false);//turn wifi off
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Set SSID and PASS to Link (Link reset) onPostExecute -Exception " + e);
            }

        }
    }

    public class aboveAndroid9_UpdateMacAsynTask extends AsyncTask<String, Void, String> {


        public String response = null;

        @Override
        protected String doInBackground(String... param) {

            try {
                ServerHandler serverHandler = new ServerHandler();
                String jsonData = param[0];
                String userEmail = CommonUtils.getCustomerDetails(WelcomeActivity.this).PersonEmail;

                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(WelcomeActivity.this) + ":" + userEmail + ":" + "UpdateMACAddress");
                response = serverHandler.PostTextData(WelcomeActivity.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {
                Constants.hotspotstayOn = true;
                CommonUtils.LogMessage("", "UpdateMACAddress ", ex);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  UpdateMacAsynTask doInBackground--Exception " + ex);
                response = "err";

            }
            return response;
        }

        @Override
        protected void onPostExecute(String serverRes) {


            try {
                if (serverRes.equalsIgnoreCase("err")) {
                    AppConstants.alertBigFinishActivity(WelcomeActivity.this, "Link Re-configuration is partially completed. \nPlease remove app from Recent Apps and start app again");
                    Log.i(TAG, "Step4 Link Re-configuration is partially completed.");
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Step4 Link Re-configuration is partially completed.");
                } else if (serverRes != null) {

                    JSONObject jsonObject1 = new JSONObject(serverRes);
                    String ResponceMessage = jsonObject1.getString("ResponceMessage");
                    if (ResponceMessage.equalsIgnoreCase("success")) {

                        AppConstants.clearSharedPrefByName(WelcomeActivity.this, Constants.MAC_ADDR_RECONFIGURE);
                        Constants.hotspotstayOn = true;
                        AppConstants.colorToastBigFont(WelcomeActivity.this, " Mac Address Updated ", Color.parseColor("#4CAF50"));
                        Log.i(TAG, "Step4 Mac Address Updated");
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Step4 Mac Address Updated");
                        wifiApManager.setWifiApEnabled(null, true);
                        ChangeWifiState(false);
                        //alertHotspotOnOffAfterReconfigure();

                    } else if (ResponceMessage.equalsIgnoreCase("fail")) {

                        Constants.hotspotstayOn = true;
                        AppConstants.colorToastBigFont(WelcomeActivity.this, " Could not Updated mac address ", Color.RED);
                        Log.i(TAG, "Step4 Could not Updated mac address");
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Step4 Could not Updated mac address");
                        wifiApManager.setWifiApEnabled(null, true);
                        ChangeWifiState(false);
                    }

                } else {
                    Log.i(TAG, "Step4 UpdateMacAsynTask Server Response Empty!");
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Step4 UpdateMacAsynTask Server Response Empty!");
                    //CommonUtils.showNoInternetDialog(WelcomeActivity.this);
                }
            } catch (Exception e) {

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  UpdateMacAsynTask onPostExecute--Exception " + e);

            }
        }
    }


    //UDP Link Reconfigure code below
    private void UDPLinkReConfigurationProcessStep1() {

        if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE")) {

            try {
                AppConstants.enableHotspotManuallyWindow = false;
                Constants.hotspotstayOn = false; //hotspot enable/disable flag

                //Enable wifi
                ChangeWifiState(true);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Log.i(TAG, "Step1 UDP Link ReConfiguration enable wifi manually.");
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Step1 UDP Link ReConfiguration enable wifi manually.");

                        AppConstants.SELECTED_SSID_FOR_MANUALL = AppConstants.CURRENT_SELECTED_SSID;//ReconfigSSID;
                        AppConstants.colorToastBigFont(getApplicationContext(), "Enable Wifi Manually and Connect to " + AppConstants.SELECTED_SSID_FOR_MANUALL + " using wifi list", Color.RED);
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));

                        new CountDownTimer(180000, 2000) {

                            public void onTick(long millisUntilFinished) {

                                Log.i(TAG, "Step1 onTick");
                                WifiManager wifiManager = (WifiManager) WelcomeActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                String ssid = "";
                                if (wifiManager.isWifiEnabled()) {
                                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                                    ssid = wifiInfo.getSSID().trim().replace("\"", "");
                                }

                                /*ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);*/

                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + ssid + " === " + AppConstants.SELECTED_SSID_FOR_MANUALL); //+" IsWifi Connected: "+mWifi.isConnected()

                                if (ssid.equalsIgnoreCase(AppConstants.SELECTED_SSID_FOR_MANUALL)) { //mWifi.isConnected() &&

                                    //proced to reconfigure process
                                    cancel();
                                    Log.i(TAG, "Step1 UDP onTick ssid connected" + ssid);
                                    AppConstants.colorToastBigFont(WelcomeActivity.this, "Connected to wifi " + AppConstants.SELECTED_SSID_FOR_MANUALL, Color.BLUE);
                                    Log.i(TAG, "Step1 UDP Connected to wifi " + AppConstants.SELECTED_SSID_FOR_MANUALL);
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "Step1 UDP Connected to wifi " + AppConstants.SELECTED_SSID_FOR_MANUALL);

                                    setGlobalWifiConnection();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            UDPLinkReConfigurationProcessStep2();
                                        }
                                    }, 1000);

                                }
                            }

                            public void onFinish() {

                                Log.i(TAG, "Step1 onFinish");

                                ChangeWifiState(false);
                                Log.i(TAG, "Step1 onFinish ssid Not connected. Please try again..");
                                AppConstants.colorToastBigFont(getApplicationContext(), "Failed to connect to " + AppConstants.SELECTED_SSID_FOR_MANUALL + " Please try again..", Color.RED);
                                Log.i(TAG, "Step1 Failed to connect to " + AppConstants.SELECTED_SSID_FOR_MANUALL + " Please try again..");
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "Step1 Failed to connect to " + AppConstants.SELECTED_SSID_FOR_MANUALL + " Please try again..");

                                //Back to welcome activity
                                Intent i = new Intent(WelcomeActivity.this, WelcomeActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                WelcomeActivity.this.startActivity(i);
                            }

                        }.start();

                    }
                }, 1000);

            } catch (Exception e) {
                ChangeWifiState(false);//turn wifi off
                Constants.hotspotstayOn = true;
                Log.i(TAG, "Link ReConfiguration process -Step 1 Exception" + e);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Link ReConfiguration process -Step 1 Exception" + e);
            }
        } else {
            AppConstants.colorToastBigFont(WelcomeActivity.this, "Can't update mac address,Hose is busy please retry later.", Color.RED);
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Can't update mac address,Hose is busy please retry later.");
            btnGo.setVisibility(View.GONE);
        }
    }

    private void UDPLinkReConfigurationProcessStep2() {

        try {

            String SERVER_IP = "192.168.4.1";
            String mac_address = "";
            String info_result = new UDPClientTask().execute(BTConstants.info_cmd, SERVER_IP).get();
            Log.i(TAG, "BTLink 1: UDPInfoResult>>" + info_result);

            if (info_result.contains("STAMAC:")) {
                String[] split = info_result.split("STAMAC:");
                mac_address = split[1].replaceAll("\"", "").trim();
                AppConstants.UPDATE_MACADDRESS = mac_address;

                Log.i(TAG, "BTLink 1: UDPInfoResult>>" + info_result);

                Log.i(TAG, "Step2 Link UDP ReConfiguration INFO_Command result:" + info_result);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Step2 Link UDP ReConfiguration INFO_Command result:" + info_result);

                if (mac_address.equals("")) {

                    AppConstants.colorToastBigFont(WelcomeActivity.this, "Reconfiguration process fail..\nCould not get mac address", Color.RED);
                    Log.i(TAG, "Step2 UDP Reconfiguration process fail.. Could not get mac address Info command result:" + info_result);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Step2 UDP Reconfiguration process fail.. Could not get mac address Info command result:" + info_result);

                    Constants.hotspotstayOn = true;//Enable hotspot flag
                    //Disable wifi connection
                    WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                    wifiManagerMM.setWifiEnabled(false);
                    //TODO Makesure Hotspot get On here

                } else {

                    //setGlobalWifiConnection();
                    //Set HUB usernam and password to link
                    AppConstants.colorToastBigFont(WelcomeActivity.this, "Setting SSID and PASS to Link", Color.BLUE);
                    Log.i(TAG, "Step2 Setting SSID and PASS to Link");
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Step2 UDP Setting SSID and PASS to Link");

                    HTTP_URL = "http://192.168.4.1:80/";
                    URL_UPDATE_FS_INFO = HTTP_URL + "config?command=wifi";
                    String linkStationCmd = BTConstants.linkstation_cmd + AppConstants.HubName + ";" + AppConstants.HubGeneratedpassword;

                    String linkstation_response = new UDPClientTask().execute(linkStationCmd, SERVER_IP).get();

                    if (linkstation_response.contains(AppConstants.HubGeneratedpassword)) {

                        Constants.hotspotstayOn = true;//Enable hotspot flag
                        ChangeWifiState(false);//turn wifi off

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                LinkReConfigurationProcessStep3(WelcomeActivity.this);
                            }
                        }, 5000);

                        System.out.println(linkstation_response);
                        Log.i(TAG, " Set SSID and PASS to Link (Result" + linkstation_response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Set SSID and PASS to Link Result" + linkstation_response);

                    } else {
                        ChangeWifiState(false);//turn wifi off
                        AppConstants.colorToastBigFont(getApplicationContext(), "Step2 Failed while changing Hotspot Settings Please try again..", Color.RED);
                        Log.i(TAG, "Step2 Failed while changing Hotspot Settings Please try again.. exception:" + linkstation_response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Step2 Failed while changing Hotspot Settings UDPLink. exception:" + linkstation_response);

                        //mj
                        Intent intent = new Intent(WelcomeActivity.this, WelcomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            } else {

                ChangeWifiState(false);
                UDPLinkReConfigurationProcessStep1();
                AppConstants.colorToastBigFont(WelcomeActivity.this, "Step2 Failed to get Info Command ", Color.BLUE);
                Log.i(TAG, "Step2 Failed to get Info Command ");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Step2 Failed to get Info Command ");

                //BackTo Welcome Activity temp comment
                Intent i = new Intent(WelcomeActivity.this, WelcomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                WelcomeActivity.this.startActivity(i);
            }

        } catch (Exception e) {
            ChangeWifiState(false);//turn wifi off
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "  WiFiConnectTask  OnPostExecution --Exception " + e);
        }
    }

    public void startBTSppMain(int serviceIndex) {

        try {
            //Link 1
            if (serviceIndex == 0 || serviceIndex == 1) {
                WelcomeActivity.this.startService(new Intent(this, SerialServiceOne.class));
                WelcomeActivity.this.bindService(new Intent(this, SerialServiceOne.class), this, Context.BIND_AUTO_CREATE);
                Log.i(TAG, "BTLink 1: startBTSppMain");
            }

            //Link 2
            if (serviceIndex == 0 || serviceIndex == 2) {
                WelcomeActivity.this.startService(new Intent(this, SerialServiceTwo.class));
                WelcomeActivity.this.bindService(new Intent(this, SerialServiceTwo.class), this, Context.BIND_AUTO_CREATE);
                Log.i(TAG, "BTLink 2: startBTSppMain");
            }

            //Link 3
            if (serviceIndex == 0 || serviceIndex == 3) {
                WelcomeActivity.this.startService(new Intent(this, SerialServiceThree.class));
                WelcomeActivity.this.bindService(new Intent(this, SerialServiceThree.class), this, Context.BIND_AUTO_CREATE);
                Log.i(TAG, "BTLink 3: startBTSppMain");
            }

            //Link 4
            if (serviceIndex == 0 || serviceIndex == 4) {
                WelcomeActivity.this.startService(new Intent(this, SerialServiceFour.class));
                WelcomeActivity.this.bindService(new Intent(this, SerialServiceFour.class), this, Context.BIND_AUTO_CREATE);
                Log.i(TAG, "BTLink 4: startBTSppMain");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeBTSppMain() {
        //Link 1
        WelcomeActivity.this.stopService(new Intent(this, SerialServiceOne.class));
        Log.i(TAG, "BTLink 1: closeBTSppMain");

        //Link 2
        WelcomeActivity.this.stopService(new Intent(this, SerialServiceTwo.class));
        Log.i(TAG, "BTLink 2: closeBTSppMain");

        //Link 3
        WelcomeActivity.this.stopService(new Intent(this, SerialServiceThree.class));
        Log.i(TAG, "BTLink 3: closeBTSppMain");

        //Link 4
        WelcomeActivity.this.stopService(new Intent(this, SerialServiceFour.class));
        Log.i(TAG, "BTLink 4: closeBTSppMain");
    }

    private boolean checkBTLinkStatus(int position) {
        boolean isConnected = false;
        try {
            switch (position) {
                case 1:
                    if (BTConstants.BTStatusStrOne.equalsIgnoreCase("Connected")) {
                        isConnected = true;
                    }
                    if (isConnected) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "BTLink 1: Link is connected.");
                    }
                    break;
                case 2:
                    if (BTConstants.BTStatusStrTwo.equalsIgnoreCase("Connected")) {
                        isConnected = true;
                    }
                    if (isConnected) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "BTLink 2: Link is connected.");
                    }
                    break;
                case 3:
                    if (BTConstants.BTStatusStrThree.equalsIgnoreCase("Connected")) {
                        isConnected = true;
                    }
                    if (isConnected) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "BTLink 3: Link is connected.");
                    }
                    break;
                case 4:
                    if (BTConstants.BTStatusStrFour.equalsIgnoreCase("Connected")) {
                        isConnected = true;
                    }
                    if (isConnected) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "BTLink 4: Link is connected.");
                    }
                    break;
            }

        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + " BTLink " + position + ": checkBTLinkStatus Exception:>>" + e.getMessage());
        }
        return isConnected;
    }

    public void retryConnect(int position) {
        try {
            switch (position) {
                case 1:
                    if (BTConstants.BTStatusStrOne.equalsIgnoreCase("Connecting...")) {
                        BTConstants.CurrentTransactionIsBT = false;
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "BTLink 1: Retrying to Connect");
                        //Retrying to connect to link
                        BTSPPMain btspp = new BTSPPMain();
                        btspp.activity = WelcomeActivity.this;
                        btspp.connect1();
                        BTConstants.CurrentTransactionIsBT = false;
                    }
                    break;
                case 2:
                    if (BTConstants.BTStatusStrTwo.equalsIgnoreCase("Connecting...")) {
                        BTConstants.CurrentTransactionIsBT = false;
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "BTLink 2: Retrying to Connect");
                        //Retrying to connect to link
                        BTSPPMain btspp = new BTSPPMain();
                        btspp.activity = WelcomeActivity.this;
                        btspp.connect2();
                        BTConstants.CurrentTransactionIsBT = false;
                    }
                    break;
                case 3:
                    if (BTConstants.BTStatusStrThree.equalsIgnoreCase("Connecting...")) {
                        BTConstants.CurrentTransactionIsBT = false;
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "BTLink 3: Retrying to Connect");
                        //Retrying to connect to link
                        BTSPPMain btspp = new BTSPPMain();
                        btspp.activity = WelcomeActivity.this;
                        btspp.connect3();
                        BTConstants.CurrentTransactionIsBT = false;
                    }
                    break;
                case 4:
                    if (BTConstants.BTStatusStrFour.equalsIgnoreCase("Connecting...")) {
                        BTConstants.CurrentTransactionIsBT = false;
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + "BTLink 4: Retrying to Connect");
                        //Retrying to connect to link
                        BTSPPMain btspp = new BTSPPMain();
                        btspp.activity = WelcomeActivity.this;
                        btspp.connect4();
                        BTConstants.CurrentTransactionIsBT = false;
                    }
                    break;
            }

        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(AppConstants.LOG_TXTN_BT + "-" + TAG + " BTLink " + position + ": retryConnect Exception:>>" + e.getMessage());
        }
    }

    private void CheckBTConnection(int selectedItemPos, String selSSID, String selMacAddress) {

        switch (selectedItemPos) {

            case 0:
                //Link one
                if (BTConstants.BTLinkOneStatus) {

                    if (Constants.FS_1STATUS.equalsIgnoreCase("FREE")) {

                        AppConstants.FS_selected = String.valueOf(selectedItemPos);
                        RedirectBtLinkOneToNextScreen(selSSID);

                    } else {
                        BTL1State = 0;
                        BTConstants.CurrentSelectedLinkBT = 0;
                        RestrictHoseSelection("Hose in use.\nPlease try again later");
                    }

                } else {

                    if (!BTConstants.deviceAddress1.isEmpty()) {
                        NearByBTDevices.clear();
                        mBluetoothAdapter.startDiscovery();
                        RestrictHoseSelection("Connecting...");

                        Handler handler = new Handler();
                        int delay = 1000;
                        handler.postDelayed(new Runnable() {
                            public void run() {

                                if (checkBTLinkStatus(1)) {
                                    if (BTConnectionHandler != null) {
                                        BTConnectionHandler.removeCallbacksAndMessages(null);
                                    }
                                    RedirectBtLinkOneToNextScreen(selSSID);
                                } else {
                                    retryConnect(1);

                                    if (BTConnectionHandler != null) {
                                        BTConnectionHandler.removeCallbacksAndMessages(null);
                                    }
                                    RedirectBtLinkOneToNextScreen(selSSID);
                                }
                            }
                        }, delay);

                        /*handler.postDelayed(new Runnable() {
                            public void run() {

                                if (checkBTLinkStatus(1)) {
                                    if (BTConnectionHandler != null) {
                                        BTConnectionHandler.removeCallbacksAndMessages(null);
                                    }
                                    RedirectBtLinkOneToNextScreen(selSSID);
                                } else {
                                    if (BTL1counter == 0) {
                                        BTL1counter++;

                                        retryConnect(1);

                                        handler.postDelayed(this, delay);
                                    } else {
                                        BTL1counter = 0;
                                        BTConstants.CurrentTransactionIsBT = false;
                                        if (BTConnectionHandler != null) {
                                            BTConnectionHandler.removeCallbacksAndMessages(null);
                                        }
                                        RedirectBtLinkOneToNextScreen(selSSID);
                                    }
                                }
                            }
                        }, delay);*/

                    } else {
                        AppConstants.colorToast(getApplicationContext(), "Please make sure BT mac is set.", Color.BLUE);
                    }
                }
                break;
            case 1:
                //Link Two
                if (BTConstants.BTLinkTwoStatus) {
                    if (Constants.FS_2STATUS.equalsIgnoreCase("FREE")) {

                        AppConstants.FS_selected = String.valueOf(selectedItemPos);
                        RedirectBtLinkTwoToNextScreen(selSSID);

                    } else {
                        BTL2State = 0;
                        BTConstants.CurrentSelectedLinkBT = 0;
                        RestrictHoseSelection("Hose in use.\nPlease try again later");
                    }

                } else {

                    if (!BTConstants.deviceAddress2.isEmpty()) {
                        NearByBTDevices.clear();
                        mBluetoothAdapter.startDiscovery();
                        RestrictHoseSelection("Connecting...");

                        Handler handler = new Handler();
                        int delay = 1000;
                        handler.postDelayed(new Runnable() {
                            public void run() {

                                if (checkBTLinkStatus(2)) {
                                    if (BTConnectionHandler != null) {
                                        BTConnectionHandler.removeCallbacksAndMessages(null);
                                    }
                                    RedirectBtLinkTwoToNextScreen(selSSID);
                                } else {
                                    retryConnect(2);

                                    if (BTConnectionHandler != null) {
                                        BTConnectionHandler.removeCallbacksAndMessages(null);
                                    }
                                    RedirectBtLinkTwoToNextScreen(selSSID);
                                }
                            }
                        }, delay);

                        /*handler.postDelayed(new Runnable() {
                            public void run() {

                                if (checkBTLinkStatus(2)) {
                                    if (BTConnectionHandler != null) {
                                        BTConnectionHandler.removeCallbacksAndMessages(null);
                                    }
                                    RedirectBtLinkTwoToNextScreen(selSSID);
                                } else {
                                    if (BTL2counter == 0) {
                                        BTL2counter++;

                                        retryConnect(2);

                                        handler.postDelayed(this, delay);
                                    } else {
                                        BTL2counter = 0;
                                        BTConstants.CurrentTransactionIsBT = false;
                                        if (BTConnectionHandler != null) {
                                            BTConnectionHandler.removeCallbacksAndMessages(null);
                                        }
                                        RedirectBtLinkTwoToNextScreen(selSSID);
                                    }
                                }
                            }
                        }, delay);*/

                    } else {
                        AppConstants.colorToast(getApplicationContext(), "Please make sure BT mac is set.", Color.BLUE);
                    }
                }
                break;
            case 2:

                //Link Three
                if (BTConstants.BTLinkThreeStatus) {
                    if (Constants.FS_3STATUS.equalsIgnoreCase("FREE")) {

                        AppConstants.FS_selected = String.valueOf(selectedItemPos);
                        RedirectBtLinkThreeToNextScreen(selSSID);

                    } else {
                        BTL3State = 0;
                        BTConstants.CurrentSelectedLinkBT = 0;
                        RestrictHoseSelection("Hose in use.\nPlease try again later");
                    }

                } else {

                    if (!BTConstants.deviceAddress3.isEmpty()) {
                        NearByBTDevices.clear();
                        mBluetoothAdapter.startDiscovery();
                        RestrictHoseSelection("Connecting...");

                        Handler handler = new Handler();
                        int delay = 1000;
                        handler.postDelayed(new Runnable() {
                            public void run() {

                                if (checkBTLinkStatus(3)) {
                                    if (BTConnectionHandler != null) {
                                        BTConnectionHandler.removeCallbacksAndMessages(null);
                                    }
                                    RedirectBtLinkThreeToNextScreen(selSSID);
                                } else {
                                    retryConnect(3);

                                    if (BTConnectionHandler != null) {
                                        BTConnectionHandler.removeCallbacksAndMessages(null);
                                    }
                                    RedirectBtLinkThreeToNextScreen(selSSID);
                                }
                            }
                        }, delay);

                        /*handler.postDelayed(new Runnable() {
                            public void run() {

                                if (checkBTLinkStatus(3)) {
                                    if (BTConnectionHandler != null) {
                                        BTConnectionHandler.removeCallbacksAndMessages(null);
                                    }
                                    RedirectBtLinkThreeToNextScreen(selSSID);
                                } else {
                                    if (BTL3counter == 0) {
                                        BTL3counter++;

                                        retryConnect(3);

                                        handler.postDelayed(this, delay);
                                    } else {
                                        BTL3counter = 0;
                                        BTConstants.CurrentTransactionIsBT = false;
                                        if (BTConnectionHandler != null) {
                                            BTConnectionHandler.removeCallbacksAndMessages(null);
                                        }
                                        RedirectBtLinkThreeToNextScreen(selSSID);
                                    }
                                }
                            }
                        }, delay);*/

                    } else {
                        AppConstants.colorToast(getApplicationContext(), "Please make sure BT mac is set.", Color.BLUE);
                    }
                }

                break;
            case 3://Link Four

                if (BTConstants.BTLinkFourStatus) {

                    if (Constants.FS_4STATUS.equalsIgnoreCase("FREE")) {

                        AppConstants.FS_selected = String.valueOf(selectedItemPos);
                        RedirectBtLinkFourToNextScreen(selSSID);

                    } else {
                        BTL4State = 0;
                        BTConstants.CurrentSelectedLinkBT = 0;
                        RestrictHoseSelection("Hose in use.\nPlease try again later");
                    }

                } else {

                    if (!BTConstants.deviceAddress4.isEmpty()) {
                        NearByBTDevices.clear();
                        mBluetoothAdapter.startDiscovery();
                        RestrictHoseSelection("Connecting...");

                        Handler handler = new Handler();
                        int delay = 1000;
                        handler.postDelayed(new Runnable() {
                            public void run() {

                                if (checkBTLinkStatus(4)) {
                                    if (BTConnectionHandler != null) {
                                        BTConnectionHandler.removeCallbacksAndMessages(null);
                                    }
                                    RedirectBtLinkFourToNextScreen(selSSID);
                                } else {
                                    retryConnect(4);

                                    if (BTConnectionHandler != null) {
                                        BTConnectionHandler.removeCallbacksAndMessages(null);
                                    }
                                    RedirectBtLinkFourToNextScreen(selSSID);
                                }
                            }
                        }, delay);

                        /*handler.postDelayed(new Runnable() {
                            public void run() {

                                if (checkBTLinkStatus(4)) {
                                    if (BTConnectionHandler != null) {
                                        BTConnectionHandler.removeCallbacksAndMessages(null);
                                    }
                                    RedirectBtLinkFourToNextScreen(selSSID);
                                } else {
                                    if (BTL4counter == 0) {
                                        BTL4counter++;

                                        retryConnect(4);

                                        handler.postDelayed(this, delay);
                                    } else {
                                        BTL4counter = 0;
                                        BTConstants.CurrentTransactionIsBT = false;
                                        if (BTConnectionHandler != null) {
                                            BTConnectionHandler.removeCallbacksAndMessages(null);
                                        }
                                        RedirectBtLinkFourToNextScreen(selSSID);
                                    }
                                }
                            }
                        }, delay);*/

                    } else {
                        AppConstants.colorToast(getApplicationContext(), "Please make sure BT mac is set.", Color.BLUE);
                    }
                }
                break;
        }

    }

    /*private void CheckUDPConnection(int selectedItemPos, String selSSID, String selMacAddress) {

        AppConstants.ManuallReconfigure = false;
        IpAddress = "";
        try {
            for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                if (selMacAddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                    IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "  CheckUDPConnection getting IpAddress Exception: " + e);
        }

        switch (selectedItemPos) {

            case 0:
                //Link one
                AppConstants.FS_selected = String.valueOf(0);
                if (IpAddress.equals("")) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " Hose not connected");
                    RestrictHoseSelection("Hose not connected");
                } else if (Constants.FS_1STATUS.equalsIgnoreCase("FREE")) { // && IsBusy.equalsIgnoreCase("N")
                    // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                    Constants.AccPersonnelPIN = "";
                    tvSSIDName.setText(selSSID);
                    AppConstants.FS1_CONNECTED_SSID = selSSID;
                    Constants.CurrentSelectedHose = "FS1";
                    BTConstants.CurrentTransactionIsBT = false;
                    btnGo.setVisibility(View.VISIBLE);

                } else {
                    BTConstants.CurrentSelectedLinkBT = 0;
                    RestrictHoseSelection("Hose in use.\nPlease try again later");
                }
                break;
            case 1://Link Two
                break;
            case 2://Link Three
                break;
            case 3://Link Foure
                break;
            default://Something went wrong in link selection please try again.
                break;
        }

    }*/


    public class UDPClientTask extends AsyncTask<String, Void, String> {

        String response = "";

        @SuppressLint("LongLogTag")
        @Override
        protected String doInBackground(String... param) {

            String strcmd = param[0];//"LK_COMM=relay:12345=ON";
            String SERVER_IP = param[1];//"192.168.4.1";

            int port = 80;

            boolean run = true;
            try {

                DatagramSocket udpSocket = new DatagramSocket();
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                byte[] buf = strcmd.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, port);
                udpSocket.send(packet);
                while (run) {
                    try {
                        byte[] message = new byte[8000];
                        DatagramPacket p = new DatagramPacket(message, message.length);
                        Log.i("UDP client: ", "about to wait to receive");
                        udpSocket.setSoTimeout(10000);
                        udpSocket.receive(p);
                        String text = new String(message, 0, p.getLength());
                        Log.d("Received text", text);
                        response = text;

                    } catch (IOException e) {
                        Log.e(" UDP client has IOException", "error: ", e);
                        run = false;
                        udpSocket.close();
                    }
                }
            } catch (SocketException e) {
                Log.e("Socket Open:", "Error:", e);
            } catch (Exception e) {
                Log.e("Exception:", "Error:", e);
            }

            return response;
        }

        @SuppressLint("LongLogTag")
        @Override
        protected void onPostExecute(String res) {

            Log.i(TAG, "Socket response" + res);
        }
    }

    public void ConnectAllAvailableBTLinks() {

        try {
            if (serverSSIDList != null) {
                for (int i = 0; i < serverSSIDList.size(); i++) {
                    String MacAddress = serverSSIDList.get(i).get("MacAddress");
                    String BTMacAddress = serverSSIDList.get(i).get("BTMacAddress");

                    switch (i) {
                        case 0:
                            if (BTMacAddress != null && !BTMacAddress.isEmpty() && !BTConstants.BTLinkOneStatus && CommonFunctions.CheckIfPresentInPairedDeviceList(BTMacAddress) && !BTConstants.BTStatusStrOne.equalsIgnoreCase("Connecting...")) {
                                //Connect to Link one
                                BTSPPMain btspp1 = new BTSPPMain();
                                btspp1.activity = WelcomeActivity.this;
                                btspp1.connect1();
                            }
                            break;
                        case 1://Link Two
                            if (!BTMacAddress.isEmpty() && !BTConstants.BTLinkTwoStatus && CommonFunctions.CheckIfPresentInPairedDeviceList(BTMacAddress) && !BTConstants.BTStatusStrTwo.equalsIgnoreCase("Connecting...")) {
                                //Connect to Link two
                                BTSPPMain btspp2 = new BTSPPMain();
                                btspp2.activity = WelcomeActivity.this;
                                btspp2.connect2();
                            }
                            break;
                        case 2://Link Three
                            if (!BTMacAddress.isEmpty() && !BTConstants.BTLinkThreeStatus && CommonFunctions.CheckIfPresentInPairedDeviceList(BTMacAddress) && !BTConstants.BTStatusStrThree.equalsIgnoreCase("Connecting...")) {
                                //Connect to Link three
                                BTSPPMain btspp2 = new BTSPPMain();
                                btspp2.activity = WelcomeActivity.this;
                                btspp2.connect3();
                            }
                            break;
                        case 3://Link Four
                            if (!BTMacAddress.isEmpty() && !BTConstants.BTLinkFourStatus && CommonFunctions.CheckIfPresentInPairedDeviceList(BTMacAddress) && !BTConstants.BTStatusStrFour.equalsIgnoreCase("Connecting...")) {
                                //Connect to Link Four
                                BTSPPMain btspp2 = new BTSPPMain();
                                btspp2.activity = WelcomeActivity.this;
                                btspp2.connect4();
                            }
                            break;
                        default://Something went wrong in link selection please try again.
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "ConnectAllAvailableBTLinks Exception:" + e.toString());
        }
    }

    public void SetBTLinksMacAddress(int i, String BTMacAddress) {

        try {
            switch (i) {
                case 0:
                    BTConstants.deviceAddress1 = BTMacAddress.toUpperCase();
                    break;
                case 1://Link Two
                    BTConstants.deviceAddress2 = BTMacAddress.toUpperCase();
                    break;
                case 2://Link Three
                    BTConstants.deviceAddress3 = BTMacAddress.toUpperCase();
                    break;
                case 3://Link Four
                    BTConstants.deviceAddress4 = BTMacAddress.toUpperCase();
                    break;
                default://Something went wrong in link selection please try again.
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "SetBTLinksMacAddress Exception:" + e.toString());
        }
    }

    private void SetSSIDIfSingleHose() {

        if (serverSSIDList != null && serverSSIDList.size() == 1 && Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE")) {

            try {
                AppConstants.IsSingleLink = true;
                String ssidFromList = serverSSIDList.get(0).get("WifiSSId");
                String LinkCommunicationType = serverSSIDList.get(0).get("LinkCommunicationType");
                String stringText = tvSSIDName.getText().toString().trim();
                String BTselMacAddress = serverSSIDList.get(0).get("BTMacAddress");
                String selSSID = serverSSIDList.get(0).get("WifiSSId");

                if (!stringText.equalsIgnoreCase(ssidFromList)) {
                    //------------------------
                    String SSID_mac = serverSSIDList.get(0).get("MacAddress");
                    String ReconfigureLink = serverSSIDList.get(0).get("ReconfigureLink");
                    AppConstants.SITE_ID = serverSSIDList.get(0).get("SiteId");
                    if (ReconfigureLink == null) {
                        ReconfigureLink = "";
                    }

                    OfflineConstants.storeCurrentTransaction(WelcomeActivity.this, "", AppConstants.SITE_ID, "", "", "", "", "", AppConstants.currentDateFormat("yyyy-MM-dd HH:mm"), "", "");

                    if (LinkCommunicationType.equalsIgnoreCase("BT") && !ReconfigureLink.equalsIgnoreCase("true")) {
                        //tvSSIDName.setText("Tap here to select hose");
                        //btnGo.setVisibility(View.VISIBLE);
                        SingleBTLinkSelection(selSSID, BTselMacAddress);

                    } else {

                        String Chk_ip = "";
                        if (AppConstants.DetailsListOfConnectedDevices != null && AppConstants.DetailsListOfConnectedDevices.size() > 0) {
                            Chk_ip = AppConstants.DetailsListOfConnectedDevices.get(0).get("ipAddress");
                        } else {
                            getipOverOSVersion();
                        }

                        if (Chk_ip != null && Chk_ip.length() > 3 && !ReconfigureLink.equalsIgnoreCase("true")) {

                            boolean isMacConnected = false;
                            if (AppConstants.DetailsListOfConnectedDevices != null) {
                                for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                    String Chk_mac = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");

                                    if (SSID_mac.equalsIgnoreCase(Chk_mac)) {
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + SSID_mac + ") is connected to hotspot.");
                                        IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                                        isMacConnected = true;
                                        break;
                                    }
                                }
                            }

                            if (!isMacConnected) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "Selected LINK (" + selSSID + " <==> " + SSID_mac + ") is not found in connected devices. " + AppConstants.DetailsListOfConnectedDevices);

                                for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                                    String Chk_mac = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "Checking Mac Address using info command: (" + Chk_mac + ")");

                                    String connectedIp = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");

                                    IpAddress = GetAndCheckMacAddressFromInfoCommand(connectedIp, SSID_mac, Chk_mac);
                                    if (!IpAddress.trim().isEmpty()) {
                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile("===================================================================");
                                        break;
                                    }
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile("===================================================================");
                                }
                            }

                            if (!IpAddress.trim().isEmpty()) {
                                SelectedItemPos = 0;
                                tvSSIDName.setText(serverSSIDList.get(0).get("WifiSSId"));
                                OnHoseSelected_OnClick(Integer.toString(0));
                            }
                        } else {
                            //Toast.makeText(getApplicationContext(), "Auto select fail", Toast.LENGTH_SHORT).show();
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Single Hose: Auto select fail");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void SingleBTLinkSelection(String selSSID, String BTselMacAddress) {
        try {
            if (CommonFunctions.CheckIfPresentInPairedDeviceList(BTselMacAddress)) {
                AppConstants.CURRENT_SELECTED_SSID = selSSID;
                AppConstants.SELECTED_MACADDRESS = BTselMacAddress;
                SetBTLinksMacAddress(0, BTselMacAddress);
                tvSSIDName.setText(selSSID);
                OnHoseSelected_OnClick(Integer.toString(0));
            }
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "SingleBTLinkSelection Exception >> " + e.getMessage());
        }
    }

    private void qrcodebleServiceOff() {

        try {

            if (QRCodeBluetoothMacAddressForBarcode.length() > 0 && !QRCodeBluetoothMacAddressForBarcode.isEmpty())
                stopService(new Intent(WelcomeActivity.this, ServiceQRCode.class));

            if (mMagCardDeviceAddress.length() > 0 && !mMagCardDeviceAddress.isEmpty() && mMagCardDeviceName.contains("MAGCARD_READERV2"))
                stopService(new Intent(WelcomeActivity.this, ServiceMagV2.class));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void qrcodebleServiceOn() {

        try {
            if (QRCodeBluetoothMacAddressForBarcode.length() > 0 && !QRCodeBluetoothMacAddressForBarcode.isEmpty() && !Constants.QR_ReaderStatus.equalsIgnoreCase("QR Connected"))
                startService(new Intent(WelcomeActivity.this, ServiceQRCode.class));

            if (mMagCardDeviceAddress.length() > 0 && !mMagCardDeviceAddress.isEmpty() && mMagCardDeviceName.contains("MAGCARD_READERV2") && !Constants.Mag_ReaderStatus.equalsIgnoreCase("Mag Connected"))
                startService(new Intent(WelcomeActivity.this, ServiceMagV2.class));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ToggleHotspot() {

        {
            //Toggle hotspot programatically
            Boolean toggle_success = true;
            wifiApManager.setWifiApEnabled(null, false);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (CommonUtils.isHotspotEnabled(WelcomeActivity.this)) {
                // Log.i(TAG, "ToggleHotspot Failed to disable hotspot");
            } else {
                Log.i(TAG, "ToggleHotspot hotspot OFF");
            }
            wifiApManager.setWifiApEnabled(null, true);

            if (CommonUtils.isHotspotEnabled(WelcomeActivity.this)) {
                Log.i(TAG, "ToggleHotspot hotspot ON");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "ToggleHotspot hotspot Enabled");
            } else {
                Log.i(TAG, "ToggleHotspot failed to enable hotspot");
            }

        }
    }

    private void IsUiChangeReq() {

        if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE") && Constants.FS_5STATUS.equalsIgnoreCase("FREE") && Constants.FS_6STATUS.equalsIgnoreCase("FREE")) {
            count_uithread++;
        }

    }

    private void IsSingleHoseRefreshReq() {

        try {
            //Implemented this logic on 1444 (Nic)
            if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE") && Constants.FS_5STATUS.equalsIgnoreCase("FREE") && Constants.FS_6STATUS.equalsIgnoreCase("FREE")) {

                if (AppConstants.RefreshSingleHose) {
                    AppConstants.RefreshSingleHose = false;
                    SetSSIDIfSingleHose();
                }

            } else if (Constants.FS_1STATUS.equalsIgnoreCase("BUSY") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE") && Constants.FS_5STATUS.equalsIgnoreCase("FREE") && Constants.FS_6STATUS.equalsIgnoreCase("FREE")) {

                AppConstants.RefreshSingleHose = true;
                //#1508 One Hose System message to show in “Tap Here to Select Hose”.
                if (serverSSIDList != null && serverSSIDList.size() == 1) {
                    try {
                        tvSSIDName.setText(serverSSIDList.get(0).get("WifiSSId"));
                        tvSSIDName.setText("Hose in use.\nPlease try again later");
                        btnGo.setVisibility(View.GONE);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void countDown() {

        new CountDownTimer(6000, 1000) {

            public void onTick(long millisUntilFinished) {
                tv_BTlinkconnection.setText(" Trying to connect to Links, please wait for: " + millisUntilFinished / 1000 + "s");
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                tv_BTlinkconnection.setText("");
            }

        }.start();


    }

    public void ShowAnimatedStatus(String s) {
        try {

            //Handler handler = new Handler(Looper.getMainLooper());
            BTConnectionHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if ((delayMillis / 4) == 100) {
                        st = "Connecting";
                        delayMillis = 100;
                    } else {
                        st = st + ".";
                        delayMillis = delayMillis + 100;
                    }
                    tvSSIDName.setText(st);
                    BTConnectionHandler.postDelayed(this, delayMillis);
                }
            }, delayMillis);
        } catch (Exception ex) {
            ex.printStackTrace();
            tvSSIDName.setText(s);
        }
    }

    private void RestrictHoseSelection(String s) {

        try {
            if (!s.equalsIgnoreCase("Connecting...")) {
                tvSSIDName.setText(s);
            } else {
                ShowAnimatedStatus(s);
            }
            //tvSSIDName.setText(s); // uncomment this if the above code is not in use.
            btnGo.setVisibility(View.GONE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!s.equalsIgnoreCase("Connecting...")) {
                        tvSSIDName.setText("Tap here to select hose");
                    }
                    btnGo.setVisibility(View.GONE);
                }
            }, 6000);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver btreceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if (!NearByBTDevices.contains(deviceHardwareAddress)) {
                    NearByBTDevices.add(deviceHardwareAddress);
                    Log.i(TAG, "BT Scan deviceName:" + deviceName + " MacAddress:" + deviceHardwareAddress);
                }
            }
        }
    };


    private void RedirectBtLinkOneToNextScreen(String selSSID) {

        // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        Constants.AccPersonnelPIN = "";
        tvSSIDName.setText(selSSID);
        AppConstants.FS1_CONNECTED_SSID = selSSID;
        Constants.CurrentSelectedHose = "FS1";
        BTConstants.CurrentTransactionIsBT = true;
        BTConstants.CurrentSelectedLinkBT = 1;
        String ReplaceableHoseName = "";

        try {
            ReplaceableHoseName = serverSSIDList.get(0).get("ReplaceableHoseName");
            if (ReplaceableHoseName == null) {
                ReplaceableHoseName = "";
            }
        } catch (Exception e) {
            ReplaceableHoseName = "";
        }

        String IsHoseNameReplaced = serverSSIDList.get(0).get("IsHoseNameReplaced");
        String SiteId = serverSSIDList.get(0).get("SiteId");
        String HoseId = serverSSIDList.get(0).get("HoseId");

        if (IsHoseNameReplaced != null && IsHoseNameReplaced.equalsIgnoreCase("Y")) {
            BTConstants.BT1NeedRename = false;
            BTConstants.BT1REPLACEBLE_WIFI_NAME = "";
            BTConstants.BT1HOSE_ID = "";
            BTConstants.BT1SITE_ID = "";
        } else {
            BTConstants.BT1NeedRename = true;
            BTConstants.BT1REPLACEBLE_WIFI_NAME = ReplaceableHoseName;
            BTConstants.BT1HOSE_ID = HoseId;
            BTConstants.BT1SITE_ID = SiteId;
        }

        btnGo.setVisibility(View.VISIBLE);
        //AppConstants.goButtonClicked = true;
        goButtonAction(null);


    }

    private void RedirectBtLinkTwoToNextScreen(String selSSID) {

        // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        Constants.AccPersonnelPIN = "";
        tvSSIDName.setText(selSSID);
        AppConstants.FS2_CONNECTED_SSID = selSSID;
        Constants.CurrentSelectedHose = "FS2";
        BTConstants.CurrentTransactionIsBT = true;
        BTConstants.CurrentSelectedLinkBT = 2;
        String ReplaceableHoseName = "";

        try {
            ReplaceableHoseName = serverSSIDList.get(1).get("ReplaceableHoseName");
            if (ReplaceableHoseName == null) {
                ReplaceableHoseName = "";
            }
        } catch (Exception e) {
            ReplaceableHoseName = "";
        }

        String IsHoseNameReplaced = serverSSIDList.get(1).get("IsHoseNameReplaced");
        String SiteId = serverSSIDList.get(1).get("SiteId");
        String HoseId = serverSSIDList.get(1).get("HoseId");


        if (IsHoseNameReplaced != null && IsHoseNameReplaced.equalsIgnoreCase("Y")) {
            BTConstants.BT2NeedRename = false;
            BTConstants.BT2REPLACEBLE_WIFI_NAME = "";
            BTConstants.BT2HOSE_ID = "";
            BTConstants.BT2SITE_ID = "";
        } else {
            BTConstants.BT2NeedRename = true;
            BTConstants.BT2REPLACEBLE_WIFI_NAME = ReplaceableHoseName;
            BTConstants.BT2HOSE_ID = HoseId;
            BTConstants.BT2SITE_ID = SiteId;
        }

        btnGo.setVisibility(View.VISIBLE);
        goButtonAction(null);

    }

    private void RedirectBtLinkThreeToNextScreen(String selSSID) {

        // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        Constants.AccPersonnelPIN = "";
        tvSSIDName.setText(selSSID);
        AppConstants.FS3_CONNECTED_SSID = selSSID;
        Constants.CurrentSelectedHose = "FS3";
        BTConstants.CurrentTransactionIsBT = true;
        BTConstants.CurrentSelectedLinkBT = 3;
        String ReplaceableHoseName = "";

        try {
            ReplaceableHoseName = serverSSIDList.get(2).get("ReplaceableHoseName");
            if (ReplaceableHoseName == null) {
                ReplaceableHoseName = "";
            }
        } catch (Exception e) {
            ReplaceableHoseName = "";
        }

        String IsHoseNameReplaced = serverSSIDList.get(2).get("IsHoseNameReplaced");
        String SiteId = serverSSIDList.get(2).get("SiteId");
        String HoseId = serverSSIDList.get(2).get("HoseId");

        if (IsHoseNameReplaced != null && IsHoseNameReplaced.equalsIgnoreCase("Y")) {
            BTConstants.BT3NeedRename = false;
            BTConstants.BT3REPLACEBLE_WIFI_NAME = "";
            BTConstants.BT3HOSE_ID = "";
            BTConstants.BT3SITE_ID = "";
        } else {
            BTConstants.BT3NeedRename = true;
            BTConstants.BT3REPLACEBLE_WIFI_NAME = ReplaceableHoseName;
            BTConstants.BT3HOSE_ID = HoseId;
            BTConstants.BT3SITE_ID = SiteId;
        }

        btnGo.setVisibility(View.VISIBLE);
        goButtonAction(null);

    }

    private void RedirectBtLinkFourToNextScreen(String selSSID) {

        // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        Constants.AccPersonnelPIN = "";
        tvSSIDName.setText(selSSID);
        AppConstants.FS4_CONNECTED_SSID = selSSID;
        Constants.CurrentSelectedHose = "FS4";
        BTConstants.CurrentTransactionIsBT = true;
        BTConstants.CurrentSelectedLinkBT = 4;
        String ReplaceableHoseName = "";

        try {
            ReplaceableHoseName = serverSSIDList.get(3).get("ReplaceableHoseName");
            if (ReplaceableHoseName == null) {
                ReplaceableHoseName = "";
            }
        } catch (Exception e) {
            ReplaceableHoseName = "";
        }

        String IsHoseNameReplaced = serverSSIDList.get(3).get("IsHoseNameReplaced");
        String SiteId = serverSSIDList.get(3).get("SiteId");
        String HoseId = serverSSIDList.get(3).get("HoseId");

        if (IsHoseNameReplaced != null && IsHoseNameReplaced.equalsIgnoreCase("Y")) {
            BTConstants.BT4NeedRename = false;
            BTConstants.BT4REPLACEBLE_WIFI_NAME = "";
            BTConstants.BT4HOSE_ID = "";
            BTConstants.BT4SITE_ID = "";
        } else {
            BTConstants.BT4NeedRename = true;
            BTConstants.BT4REPLACEBLE_WIFI_NAME = ReplaceableHoseName;
            BTConstants.BT4HOSE_ID = HoseId;
            BTConstants.BT4SITE_ID = SiteId;
        }

        btnGo.setVisibility(View.VISIBLE);
        goButtonAction(null);

    }

    private void ManualLinkUpgrade() {

        if (AppConstants.DetailsServerSSIDList != null && AppConstants.DetailsServerSSIDList.size() > 0) {

            for (int i = 0; i < AppConstants.DetailsServerSSIDList.size(); i++) {

                String ReconfigureLink = AppConstants.DetailsServerSSIDList.get(i).get("ReconfigureLink");
                String selSSID = AppConstants.DetailsServerSSIDList.get(i).get("WifiSSId");
                String IsBusy = AppConstants.DetailsServerSSIDList.get(i).get("IsBusy");
                String selMacAddress = AppConstants.DetailsServerSSIDList.get(i).get("MacAddress");
                String selSiteId = AppConstants.DetailsServerSSIDList.get(i).get("SiteId");
                String hoseID = AppConstants.DetailsServerSSIDList.get(i).get("HoseId");
                String FilePath = AppConstants.DetailsServerSSIDList.get(i).get("FilePath");
                String IsUpgrade = AppConstants.DetailsServerSSIDList.get(i).get("IsUpgrade");
                String FirmwareVersion = AppConstants.DetailsServerSSIDList.get(i).get("FirmwareVersion"); //"Y";
                String FirmwareFileName = serverSSIDList.get(SelectedItemPos).get("FirmwareFileName");

                if (IsUpgrade == null) {
                    IsUpgrade = "";
                }
                if (FirmwareVersion == null) {
                    FirmwareVersion = "";
                }
                if (FirmwareFileName == null) {
                    FirmwareFileName = "";
                }

                if (IsUpgrade.trim().equalsIgnoreCase("Y")) {
                    AppConstants.UP_FilePath = FilePath;
                    AppConstants.UP_Upgrade = true;
                    //AppConstants.UP_Upgrade_File_name = "user1.2048.new.5." + FirmwareVersion + ".bin";
                    if (FirmwareFileName.isEmpty()) {
                        FirmwareFileName = FirmwareVersion + ".bin";
                    }
                    AppConstants.UP_Upgrade_File_name = FirmwareFileName;
                } else {
                    AppConstants.UP_Upgrade = false;
                }

                for (int k = 0; k < AppConstants.DetailsListOfConnectedDevices.size(); k++) {

                    String macAddress = AppConstants.DetailsListOfConnectedDevices.get(k).get("macAddress");
                    String ipAddress = AppConstants.DetailsListOfConnectedDevices.get(k).get("ipAddress");

                    if (ipAddress != null || macAddress != null) {

                        if (selMacAddress.equalsIgnoreCase(macAddress) && IsUpgrade.equalsIgnoreCase("Y") && IsAllLinksFree()) {
                            //LinkUpgrade process start
                            String http_url = "http://" + ipAddress + ":80/";
                            CheckForFirmwareUpgrade(http_url);
                            break;
                        }

                    }

                }
            }

        } else {
            Log.i(TAG, " ManualLinkUpgrade SSID List Empty");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " ManualLinkUpgrade SSID List Empty");
        }

    }

    public boolean IsAllLinksFree() {

        if (WelcomeActivity.OnWelcomeActivity && Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE") && Constants.FS_5STATUS.equalsIgnoreCase("FREE") && Constants.FS_6STATUS.equalsIgnoreCase("FREE")) {
            return true;
        }
        return false;
    }

    private void CheckForFirmwareUpgrade(String http_url) {

        //Check for /FSBin folder if not create one
        File binFolderPath =  new File(String.valueOf(getApplicationContext().getExternalFilesDir(AppConstants.FOLDER_BIN)));
        //File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "FSBin");
        boolean success = true;
        if (!binFolderPath.exists()) {
            success = binFolderPath.mkdirs();
        }
        String LocalPath = binFolderPath + "/" + AppConstants.UP_Upgrade_File_name;
        File f = new File(LocalPath);
        if (f.exists()) {
            Log.e(TAG, "Link upgrade firmware file already exist. Skip download");
            if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " ManualLinkUpgrade firmware file already exist. Skip download");
            BackgroundServiceDownloadFirmware.manualUpgradeStart(http_url, binFolderPath + "/");

        } else {
            if (AppConstants.UP_FilePath != null) {
                new BackgroundServiceDownloadFirmware.ManualDownloadLinkAndReaderFirmware().execute(AppConstants.UP_FilePath, AppConstants.UP_Upgrade_File_name, "UP_Upgrade",http_url, binFolderPath + "/");
            } else {
                Log.e(TAG, "Link upgrade File path null");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " ManualLinkUpgrade File path null");
            }
        }
    }

    private void IsHotspotEnabled() {

        try {
            /*if (!CommonUtils.isHotspotEnabled(this) && !AppConstants.IsBTLinkSelectedCurrently && Constants.hotspotstayOn) {
                wifiApManager = new com.TrakEngineering.FluidSecureHub.WifiHotspot.WifiApManager(this);
                wifiApManager.setWifiApEnabled(null, true); //one try for auto on
            }*/

            if (!AppConstants.IsBTLinkSelectedCurrently) {
                boolean isAllBTLinks = true;
                if (serverSSIDList != null) {
                    for (int i = 0; i < serverSSIDList.size(); i++) {
                        String LinkCommunicationType = serverSSIDList.get(i).get("LinkCommunicationType");
                        if (LinkCommunicationType.equalsIgnoreCase("HTTP")) {
                            isAllBTLinks = false;
                            break;
                        }
                    }
                }
                if (isAllBTLinks) {
                    AppConstants.IsBTLinkSelectedCurrently = true;
                }
            }
            /*if (!CommonUtils.isHotspotEnabled(this) && !AppConstants.IsBTLinkSelectedCurrently && Constants.hotspotstayOn) {
                HotspotEnableErrorCount++;
                //AppConstants.WriteinFile(TAG + " Hotspot is Disabled. HotspotEnableErrorCount >> " + HotspotEnableErrorCount);
                if (HotspotEnableErrorCount > 9) {
                    AppConstants.IsProblemWhileEnableHotspot = true;
                }
            }*/
            //ShowHotspotDisabledErrorMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ShowHotspotDisabledErrorMessage() {
        if (!CommonUtils.isHotspotEnabled(this) && !AppConstants.IsBTLinkSelectedCurrently && AppConstants.IsProblemWhileEnableHotspot && Constants.hotspotstayOn && !AppConstants.ManuallReconfigure) {

            AppConstants.IsProblemWhileEnableHotspot = false;
            HotspotEnableErrorCount = 0; // reset error Count
            AppConstants.WriteinFile("Error occurred while enabling the hotspot.");
            CommonUtils.showCustomMessageDilaog(WelcomeActivity.this, "Error Message", "HotSpot is disabled, please contact customer support.");
            stopService(new Intent(WelcomeActivity.this, BackgroundServiceHotspotCheck.class));
            Intent name = new Intent(WelcomeActivity.this, BackgroundServiceHotspotCheck.class);
            PendingIntent pintent = PendingIntent.getService(getApplicationContext(), 0, name, 0);
            AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            pintent.cancel();
            alarm.cancel(pintent);
        }
    }

    public String getTransactionId() {

        SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
        String TransactionId = "";

        if (AppConstants.FS_selected.equalsIgnoreCase("0")) {
            TransactionId = sharedPref.getString("TransactionId_FS1", "");
        } else if (AppConstants.FS_selected.equalsIgnoreCase("1")) {
            TransactionId = sharedPref.getString("TransactionId", "");
        } else if (AppConstants.FS_selected.equalsIgnoreCase("2")) {
            TransactionId = sharedPref.getString("TransactionId_FS3", "");
        } else if (AppConstants.FS_selected.equalsIgnoreCase("3")) {
            TransactionId = sharedPref.getString("TransactionId_FS4", "");
        } else if (AppConstants.FS_selected.equalsIgnoreCase("4")) {
            TransactionId = sharedPref.getString("TransactionId_FS5", "");
        } else if (AppConstants.FS_selected.equalsIgnoreCase("5")) {
            TransactionId = sharedPref.getString("TransactionId_FS6", "");
        } else {
            //Something went wrong in hose selection.
        }
        return TransactionId;
    }

    public void RemoveTransactionFromInterruptedTxtnPref(String jsonParam, String result) {

        if (jsonParam.equalsIgnoreCase(jsonRelayOff) && result.contains("relay_response")) {
            System.out.println(result);
            String TransactionId = getTransactionId();
            CommonUtils.sharedPrefTxtnInterrupted(WelcomeActivity.this, TransactionId, false);
        }
    }

    public String GetAndCheckMacAddressFromInfoCommand(String connectedIp, String selMacAddress, String MA_ConnectedDevices) {
        String validIpAddress = "";
        try {
            HTTP_URL = "http://" + connectedIp + ":80/";
            URL_INFO = HTTP_URL + "client?command=info";
            String result = "";
            try {
                result = new Command_GET_INFO().execute(URL_INFO).get();
            } catch (Exception e) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Error occurred while getting mac address from info command. >> " + e.getMessage());
                result = "";
                e.printStackTrace();
            }

            if (!result.trim().isEmpty()) {
                validIpAddress = CommonUtils.CheckMacAddressFromInfoCommand(TAG, result, connectedIp, selMacAddress, MA_ConnectedDevices);
            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Info command response is empty. (IP: " + connectedIp + "; MAC Address: " + MA_ConnectedDevices + ")");
            }

        } catch (Exception e) {
            validIpAddress = "";
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "GetAndCheckMacAddressFromInfoCommand Exception >> " + e.getMessage());
            Log.d("Ex", e.getMessage());
        }
        return validIpAddress;
    }

    public class Command_GET_INFO extends AsyncTask<String, Void, String> {

        public String resp = "";

        protected String doInBackground(String... param) {
            try {
                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(15, TimeUnit.SECONDS);
                client.setReadTimeout(15, TimeUnit.SECONDS);
                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();
            } catch (SocketException se) {
                Log.d("Ex", se.getMessage());
            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                System.out.println("APFS_PIPE OUTPUT" + result);
            } catch (Exception e) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  CommandsGET onPostExecute Exception " + e.getMessage());
                System.out.println(e);
            }
        }
    }

    public void SetUpgradeFirmwareDetails(int position, String IsUpgrade, String FirmwareVersion, String FirmwareFileName, String selSiteId, String hoseID) {
        try {
            //Firmware upgrade
            AppConstants.UP_FirmwareVersion = FirmwareVersion;
            /*if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "SetUpgradeFirmwareDetails => IsUpgrade: " + IsUpgrade + ";Is BT Link: " + AppConstants.IsBTLinkSelectedCurrently);*/
            if (IsUpgrade.trim().equalsIgnoreCase("Y")) {
                AppConstants.UP_Upgrade = true;
                //AppConstants.UP_Upgrade_File_name = "user1.2048.new.5." + FirmwareVersion + ".bin";
                if (FirmwareFileName.isEmpty()) {
                    FirmwareFileName = FirmwareVersion + ".bin";
                }
                AppConstants.UP_Upgrade_File_name = FirmwareFileName;
            } else {
                AppConstants.UP_Upgrade = false;
            }

            if (String.valueOf(position).equalsIgnoreCase("0")) {

                AppConstants.UP_SiteId_fs1 = selSiteId;
                AppConstants.UP_HoseId_fs1 = hoseID;
                if (IsUpgrade.trim().equalsIgnoreCase("Y"))
                    AppConstants.UP_Upgrade_fs1 = true;
                else
                    AppConstants.UP_Upgrade_fs1 = false;

            } else if (String.valueOf(position).equalsIgnoreCase("1")) {

                AppConstants.UP_SiteId_fs2 = selSiteId;
                AppConstants.UP_HoseId_fs2 = hoseID;
                if (IsUpgrade.trim().equalsIgnoreCase("Y"))
                    AppConstants.UP_Upgrade_fs2 = true;
                else
                    AppConstants.UP_Upgrade_fs2 = false;

            } else if (String.valueOf(position).equalsIgnoreCase("2")) {

                AppConstants.UP_SiteId_fs3 = selSiteId;
                AppConstants.UP_HoseId_fs3 = hoseID;
                if (IsUpgrade.trim().equalsIgnoreCase("Y"))
                    AppConstants.UP_Upgrade_fs3 = true;
                else
                    AppConstants.UP_Upgrade_fs3 = false;

            } else if (String.valueOf(position).equalsIgnoreCase("3")) {

                AppConstants.UP_SiteId_fs4 = selSiteId;
                AppConstants.UP_HoseId_fs4 = hoseID;
                if (IsUpgrade.trim().equalsIgnoreCase("Y"))
                    AppConstants.UP_Upgrade_fs4 = true;
                else
                    AppConstants.UP_Upgrade_fs4 = false;

            } else if (String.valueOf(position).equalsIgnoreCase("4")) {

                AppConstants.UP_SiteId_fs5 = selSiteId;
                AppConstants.UP_HoseId_fs5 = hoseID;
                if (IsUpgrade.trim().equalsIgnoreCase("Y"))
                    AppConstants.UP_Upgrade_fs5 = true;
                else
                    AppConstants.UP_Upgrade_fs5 = false;

            } else if (String.valueOf(position).equalsIgnoreCase("5")) {

                AppConstants.UP_SiteId_fs6 = selSiteId;
                AppConstants.UP_HoseId_fs6 = hoseID;
                if (IsUpgrade.trim().equalsIgnoreCase("Y"))
                    AppConstants.UP_Upgrade_fs6 = true;
                else
                    AppConstants.UP_Upgrade_fs6 = false;
            }
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Exception in SetUpgradeFirmwareDetails. " + ex.getMessage() + ";position: " + position);
            System.out.println(ex);
        }
    }

    public CharSequence GetSpinnerMessage(String message) {
        try {
            SpannableString ss2 = new SpannableString(message);
            ss2.setSpan(new RelativeSizeSpan(1.2f), 0, ss2.length(), 0);
            return ss2;
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Exception in GetSpinnerMessage. " + ex.getMessage());
            return message;
        }
    }

    public boolean HoseAvailabilityCheckTwoAttempts(ArrayList<String> NearByBTDevices, String deviceAddress) {
        boolean isConnected = false;
        try {
            if (NearByBTDevices.contains(deviceAddress)) {
                isConnected = true;
            } else {
                Thread.sleep(1000);
                if (NearByBTDevices.contains(deviceAddress)) {
                    isConnected = true;
                }
            }
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Exception in HoseAvailabilityTwoAttempts. " + ex.getMessage());
        }
        return isConnected;
    }

    private void Change_P_Type() {
        try {
            BTLinkList.clear();
            if (serverSSIDList != null) {
                for (int i = 0; i < serverSSIDList.size(); i++) {
                    String WifiSSId = serverSSIDList.get(i).get("WifiSSId");
                    String BTMacAddress = serverSSIDList.get(i).get("BTMacAddress");
                    String LinkCommunicationType = serverSSIDList.get(i).get("LinkCommunicationType");

                    if (BTMacAddress != null && !BTMacAddress.isEmpty() && LinkCommunicationType.equalsIgnoreCase("BT") && CommonFunctions.CheckIfPresentInPairedDeviceList(BTMacAddress)) {
                        // Add into BT link list for Oscilloscope functionality
                        HashMap<String, String> map = new HashMap<>();
                        map.put("WifiSSId", WifiSSId);
                        map.put("item", WifiSSId);
                        map.put("BTMacAddress", BTMacAddress);
                        map.put("LinkPosition", String.valueOf(i));
                        BTLinkList.add(map);
                    }
                }
            }
            if (BTLinkList != null) {
                if (BTLinkList.size() > 0) {
                    alertSelectBTLinkList();
                } else {
                    Toast.makeText(getApplicationContext(), "BT LINK not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "BT LINK not found.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Exception in OscilloscopeLinkSelection. " + ex.getMessage());
        }
    }

    public void alertSelectBTLinkList() {
        final Dialog dialog = new Dialog(WelcomeActivity.this);
        dialog.setTitle("FluidSecureHubTest");
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_bt_link_list);

        ListView lvBTHoseNames = (ListView) dialog.findViewById(R.id.lvHoseNames);
        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
        lvBTHoseNames.setVisibility(View.VISIBLE);

        SimpleAdapter adapter = new SimpleAdapter(WelcomeActivity.this, BTLinkList, R.layout.item_hose, new String[]{"item"}, new int[]{R.id.tvSingleItem});
        lvBTHoseNames.setAdapter(adapter);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        lvBTHoseNames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try {
                    dialog.dismiss();

                    String WifiSSId = BTLinkList.get(position).get("WifiSSId");
                    String BTMacAddress = BTLinkList.get(position).get("BTMacAddress");
                    String LinkPosition = BTLinkList.get(position).get("LinkPosition");

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Selected LINK for p_type: " + WifiSSId);

                    if (LinkPosition == null) {
                        LinkPosition = "0";
                    }

                    String s = "Please wait...";
                    SpannableString ss2 = new SpannableString(s);
                    ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
                    ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
                    pdP_Type = new ProgressDialog(WelcomeActivity.this);
                    pdP_Type.setMessage(ss2);
                    pdP_Type.setCancelable(true);
                    pdP_Type.show();

                    CheckBTConnectionForPType(LinkPosition, WifiSSId, BTMacAddress);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        dialog.show();
    }

    private void CheckBTConnectionForPType(String selectedItemPos, String selSSID, String BTMacAddress) {
        try {
            switch (selectedItemPos) {

                case "0":
                    //Link one
                    if (BTConstants.BTLinkOneStatus) {

                        if (Constants.FS_1STATUS.equalsIgnoreCase("FREE")) {

                            RedirectToP_TypeSelection(selSSID, selectedItemPos, BTMacAddress);

                        } else {
                            BTL1State = 0;
                            BTConstants.CurrentSelectedLinkBT = 0;
                            RestrictHoseSelection("Hose in use.\nPlease try again later");
                        }

                    } else {

                        if (!BTConstants.deviceAddress1.isEmpty()) {
                            NearByBTDevices.clear();
                            mBluetoothAdapter.startDiscovery();
                            RestrictHoseSelection("Connecting...");

                            Handler handler = new Handler();
                            int delay = 1000;
                            handler.postDelayed(new Runnable() {
                                public void run() {

                                    if (checkBTLinkStatus(1)) {
                                        if (BTConnectionHandler != null) {
                                            BTConnectionHandler.removeCallbacksAndMessages(null);
                                        }
                                        RedirectToP_TypeSelection(selSSID, selectedItemPos, BTMacAddress);
                                    } else {
                                        retryConnect(1);

                                        if (BTConnectionHandler != null) {
                                            BTConnectionHandler.removeCallbacksAndMessages(null);
                                        }
                                        RedirectToP_TypeSelection(selSSID, selectedItemPos, BTMacAddress);
                                    }
                                }
                            }, delay);

                        } else {
                            AppConstants.colorToast(getApplicationContext(), "Please make sure BT mac is set.", Color.BLUE);
                        }
                    }
                    break;
                case "1":
                    //Link Two
                    if (BTConstants.BTLinkTwoStatus) {
                        if (Constants.FS_2STATUS.equalsIgnoreCase("FREE")) {

                            RedirectToP_TypeSelection(selSSID, selectedItemPos, BTMacAddress);

                        } else {
                            BTL2State = 0;
                            BTConstants.CurrentSelectedLinkBT = 0;
                            RestrictHoseSelection("Hose in use.\nPlease try again later");
                        }

                    } else {

                        if (!BTConstants.deviceAddress2.isEmpty()) {
                            NearByBTDevices.clear();
                            mBluetoothAdapter.startDiscovery();
                            RestrictHoseSelection("Connecting...");

                            Handler handler = new Handler();
                            int delay = 1000;
                            handler.postDelayed(new Runnable() {
                                public void run() {

                                    if (checkBTLinkStatus(2)) {
                                        if (BTConnectionHandler != null) {
                                            BTConnectionHandler.removeCallbacksAndMessages(null);
                                        }
                                        RedirectToP_TypeSelection(selSSID, selectedItemPos, BTMacAddress);
                                    } else {
                                        retryConnect(2);

                                        if (BTConnectionHandler != null) {
                                            BTConnectionHandler.removeCallbacksAndMessages(null);
                                        }
                                        RedirectToP_TypeSelection(selSSID, selectedItemPos, BTMacAddress);
                                    }
                                }
                            }, delay);

                        } else {
                            AppConstants.colorToast(getApplicationContext(), "Please make sure BT mac is set.", Color.BLUE);
                        }
                    }
                    break;
                case "2":

                    //Link Three
                    if (BTConstants.BTLinkThreeStatus) {
                        if (Constants.FS_3STATUS.equalsIgnoreCase("FREE")) {

                            RedirectToP_TypeSelection(selSSID, selectedItemPos, BTMacAddress);

                        } else {
                            BTL3State = 0;
                            BTConstants.CurrentSelectedLinkBT = 0;
                            RestrictHoseSelection("Hose in use.\nPlease try again later");
                        }

                    } else {

                        if (!BTConstants.deviceAddress3.isEmpty()) {
                            NearByBTDevices.clear();
                            mBluetoothAdapter.startDiscovery();
                            RestrictHoseSelection("Connecting...");

                            Handler handler = new Handler();
                            int delay = 1000;
                            handler.postDelayed(new Runnable() {
                                public void run() {

                                    if (checkBTLinkStatus(3)) {
                                        if (BTConnectionHandler != null) {
                                            BTConnectionHandler.removeCallbacksAndMessages(null);
                                        }
                                        RedirectToP_TypeSelection(selSSID, selectedItemPos, BTMacAddress);
                                    } else {
                                        retryConnect(3);

                                        if (BTConnectionHandler != null) {
                                            BTConnectionHandler.removeCallbacksAndMessages(null);
                                        }
                                        RedirectToP_TypeSelection(selSSID, selectedItemPos, BTMacAddress);
                                    }
                                }
                            }, delay);

                        } else {
                            AppConstants.colorToast(getApplicationContext(), "Please make sure BT mac is set.", Color.BLUE);
                        }
                    }

                    break;
                case "3"://Link Four

                    if (BTConstants.BTLinkFourStatus) {

                        if (Constants.FS_4STATUS.equalsIgnoreCase("FREE")) {

                            RedirectToP_TypeSelection(selSSID, selectedItemPos, BTMacAddress);

                        } else {
                            BTL4State = 0;
                            BTConstants.CurrentSelectedLinkBT = 0;
                            RestrictHoseSelection("Hose in use.\nPlease try again later");
                        }

                    } else {

                        if (!BTConstants.deviceAddress4.isEmpty()) {
                            NearByBTDevices.clear();
                            mBluetoothAdapter.startDiscovery();
                            RestrictHoseSelection("Connecting...");

                            Handler handler = new Handler();
                            int delay = 1000;
                            handler.postDelayed(new Runnable() {
                                public void run() {

                                    if (checkBTLinkStatus(4)) {
                                        if (BTConnectionHandler != null) {
                                            BTConnectionHandler.removeCallbacksAndMessages(null);
                                        }
                                        RedirectToP_TypeSelection(selSSID, selectedItemPos, BTMacAddress);
                                    } else {
                                        retryConnect(4);

                                        if (BTConnectionHandler != null) {
                                            BTConnectionHandler.removeCallbacksAndMessages(null);
                                        }
                                        RedirectToP_TypeSelection(selSSID, selectedItemPos, BTMacAddress);
                                    }
                                }
                            }, delay);

                        } else {
                            AppConstants.colorToast(getApplicationContext(), "Please make sure BT mac is set.", Color.BLUE);
                        }
                    }
                    break;
            }
            /*if (BTConstants.BTLinkP_TypeStatus) {

                RedirectToP_TypeSelection(selSSID, BTMacAddress);

            } else {

                if (!BTConstants.deviceAddressP_Type.isEmpty()) {
                    NearByBTDevices.clear();
                    mBluetoothAdapter.startDiscovery();

                    Handler handler = new Handler();
                    int delay = 1000;
                    handler.postDelayed(new Runnable() {
                        public void run() {

                            if (checkBTLinkStatus(7)) {
                                RedirectToP_TypeSelection(selSSID, BTMacAddress);
                            } else {
                                retryConnect(7);

                                RedirectToP_TypeSelection(selSSID, BTMacAddress);
                            }
                        }
                    }, delay);

                } else {
                    AppConstants.colorToast(getApplicationContext(), "Please make sure BT mac is set.", Color.BLUE);
                }
            }*/
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    public void RedirectToP_TypeSelection(String WifiSSId, String LinkPosition, String BTMacAddress) {
        if (pdP_Type != null) {
            pdP_Type.dismiss();
        }
        Intent i = new Intent(WelcomeActivity.this, BT_Link_P_Type_Activity.class);
        i.putExtra("WifiSSId", WifiSSId);
        i.putExtra("BTMacAddress", BTMacAddress);
        i.putExtra("LinkPosition", LinkPosition);
        startActivity(i);
    }

}