package com.TrakEngineering.FluidSecureHubTest;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.TrakEngineering.FluidSecureHubTest.LFBle_PIN.DeviceControlActivity_Pin;
import com.TrakEngineering.FluidSecureHubTest.LFBle_vehicle.DeviceControlActivity_vehicle;
import com.TrakEngineering.FluidSecureHubTest.WifiHotspot.WifiApManager;
import com.TrakEngineering.FluidSecureHubTest.enity.AuthEntityClass;
import com.TrakEngineering.FluidSecureHubTest.enity.RenameHose;
import com.TrakEngineering.FluidSecureHubTest.enity.UpdateMacAddressClass;
import com.TrakEngineering.FluidSecureHubTest.enity.UserInfoEntity;
import com.TrakEngineering.FluidSecureHubTest.server.ServerHandler;
import com.acs.bluetooth.Acr1255uj1Reader;
import com.acs.bluetooth.Acr3901us1Reader;
import com.acs.bluetooth.BluetoothReader;
import com.acs.bluetooth.BluetoothReaderGattCallback;
import com.acs.bluetooth.BluetoothReaderManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.TrakEngineering.FluidSecureHubTest.R.id.textView;


public class WelcomeActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private String TAG = " WelcomeActivity ";
    private float density;
    ProgressDialog dialog1;
    public int ConnectCount = 0;

    private TextView textDateTime, tv_fs1_Qty, tv_fs2_Qty, tv_fs3_Qty, tv_fs4_Qty, tv_FS1_hoseName, tv_FS2_hoseName, tv_FS3_hoseName,
            tv_FS4_hoseName, tv_fs1_stop, tv_fs2_stop, tv_fs3_stop, tv_fs4_stop, tv_fs1QTN, tv_fs2QTN, tv_fs3QTN, tv_fs4QTN, tv_fs1_pulseTxt, tv_fs2_pulseTxt, tv_fs3_pulseTxt, tv_fs4_pulseTxt, tv_fs1_Pulse, tv_fs2_Pulse, tv_fs3_Pulse, tv_fs4_Pulse;
    private ImageView imgFuelLogo;
    private TextView tvTitle;
    private Button btnGo, btnRetryWifi;
    private ConnectionDetector cd;
    private double latitude = 0;
    private double longitude = 0;
    TextView tvSSIDName, tv_NFS1, tv_NFS2, tv_NFS3, tv_NFS4;//tv_fs1_pulse
    LinearLayout linearHose, linear_fs_1, linear_fs_2, linear_fs_3, linear_fs_4;
    WifiManager mainWifi;
    StringBuilder sb = new StringBuilder();

    ArrayList<HashMap<String, String>> serverSSIDList = new ArrayList<>();
    ArrayList<HashMap<String, String>> ListOfConnectedDevices = new ArrayList<>();
    public static int SelectedItemPos;
    GoogleApiClient mGoogleApiClient;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    TextView tvLatLng;
    static WifiApManager wifiApManager;
    boolean isTCancelled = false;
    int RetryOneAtemptConnectToSelectedSSSID = 0;
    String ReaderFrequency = "", IsOdoMeterRequire = "", IsDepartmentRequire = "", IsPersonnelPINRequireForHub = "", IsPersonnelPINRequire = "", IsOtherRequire = "";


    //FS For Stopbutton
    String PhoneNumber;
    String consoleString = "", outputQuantity = "0";
    boolean stopTimer = true;
    double minFuelLimit = 0, numPulseRatio = 0;
    double fillqty = 0;
    ProgressDialog loading = null;
    String IpAddress = "";

    String HTTP_URL = "";//"http://192.168.43.153:80/";//for pipe
    String URL_GET_PULSAR_FS1, URL_SET_PULSAR_FS1, URL_WIFI_FS1, URL_RELAY_FS1, URL_GET_PULSAR_FS2, URL_SET_PULSAR_FS2, URL_WIFI_FS2, URL_RELAY_FS2, URL_GET_PULSAR_FS3, URL_SET_PULSAR_FS3, URL_WIFI_FS3, URL_RELAY_FS3, URL_GET_PULSAR_FS4, URL_SET_PULSAR_FS4, URL_WIFI_FS4, URL_RELAY_FS4;
    String HTTP_URL_FS_1 = "", HTTP_URL_FS_2 = "", HTTP_URL_FS_3 = "", HTTP_URL_FS_4 = "";

    String jsonRename;
    String jsonRelayOff = "{\"relay_request\":{\"Password\":\"12345678\",\"Status\":0}}";

    String jsonPulsar = "{\"pulsar_request\":{\"counter_set\":1}}";
    String jsonPulsarOff = "{\"pulsar_request\":{\"counter_set\":0}}";
    String URL_INFO = "";
    String URL_UPDATE_FS_INFO = "";

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


    /* Reader to be connected. */
    private String mDeviceName;
    private String mDeviceAddress;
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

    //============ Bluetooth reader Gatt end==============

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onResume() {
        super.onResume();

        tvSSIDName.setText("Select");
        SelectedItemPos = -1;
        ConnectCount = 0;

        if (mConnectState == BluetoothReader.STATE_DISCONNECTED) {

            //If Reader Disconnected try to connection attempt.
            if (ConnectCount < 1) {

                ConnectCount += 1;
                System.out.println("Count: " + ConnectCount);

                if (mDeviceName != null && mDeviceAddress.contains(":")) {
                    connectReader();
                }
            }
        }


        final IntentFilter intentFilter = new IntentFilter();

        /* Start to monitor bond state change */
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver, intentFilter);

        /* Clear unused dialog. */
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

        btnGo.setClickable(true);

        AppConstants.DetailsListOfConnectedDevices = new ArrayList<>();
        new GetConnectedDevicesIP().execute();
        new GetSSIDUsingLocationOnResume().execute();
        UpdateFSUI_seconds();

        //Write TimeStamp to txt file
        WriteTimestampToFile();

        //Delete log files older than 1 month
        DeleteOldLogFiles();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
          /* Stop to monitor bond state change */
        unregisterReceiver(mBroadcastReceiver);

        /* Disconnect Bluetooth reader */
        disconnectReader();
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        tvSSIDName = (TextView) findViewById(R.id.tvSSIDName);
        tvLatLng = (TextView) findViewById(R.id.tvLatLng);

        tvLatLng.setVisibility(View.GONE);

        SelectedItemPos = -1;

        getSupportActionBar().setTitle(R.string.fs_name);
        getSupportActionBar().setIcon(R.drawable.fuel_secure_lock);

        density = getResources().getDisplayMetrics().density;

        TextView tvVersionNum = (TextView) findViewById(R.id.tvVersionNum);
        tvVersionNum.setText("Version " + CommonUtils.getVersionCode(WelcomeActivity.this));


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();

        InItGUI();

          /* Connect the reader. */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mConnectState == BluetoothReader.STATE_CONNECTED) {

                    disconnectReader();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mDeviceName != null && mDeviceAddress.contains(":")) {
                                connectReader();
                            }
                        }
                    }, 2000);

                } else if (mConnectState == BluetoothReader.STATE_CONNECTED) {
                } else {

                    if (mDeviceName != null && mDeviceAddress.contains(":")) {
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


        new GetConnectedDevicesIP().execute(); //getListOfConnectedDevice();

        //Enable Background service to check hotspot
        EnableHotspotBackgService();


        // set User Information
        UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetails(WelcomeActivity.this);

        AppConstants.Title = "Hub name: " + userInfoEntity.PersonName + "\nSite name: " + userInfoEntity.FluidSecureSiteName;//+ "\nMobile : " + userInfoEntity.PhoneNumber + "\nEmail : " + userInfoEntity.PersonEmail
        AppConstants.HubName = userInfoEntity.PersonName;
        tvTitle = (TextView) findViewById(textView);
        tvTitle.setText(AppConstants.Title);


        wifiApManager = new WifiApManager(this);

        //Set Hotspot name and password
        setHotspotNamePassword(this);

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

                if (AppConstants.BUSY_STATUS)
                    new ChangeBusyStatus().execute();

                String mobDevName = AppConstants.getDeviceName().toLowerCase();
                System.out.println("oooooooooo" + mobDevName);
                if (mobDevName.contains("moto") && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

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
        }, 2000);


        btnRetryWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppConstants.colorToastBigFont(getApplicationContext(), "Please wait for few seconds....", Color.BLUE);
                new WiFiConnectTask().execute();
            }
        });

        SharedPreferences sharedPre2 = WelcomeActivity.this.getSharedPreferences("storeBT_FOBDetails", Context.MODE_PRIVATE);

        mDeviceName = sharedPre2.getString("BluetoothCardReader", "");
        mDeviceAddress = sharedPre2.getString("BTMacAddress", "");

        System.out.println(mDeviceName + "####" + mDeviceAddress);


        /* Update UI. */
        findUiViews();
        updateUi(null);

        /* Set the onClick() event handlers. */
        setOnClickListener();

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


        //////////////////////////////////////////////////////////////////////////////////////////////////////


    }

    public void EnableHotspotBackgService() {


        Calendar cal = Calendar.getInstance();
        Intent name = new Intent(WelcomeActivity.this, BackgroundServiceHotspotCheck.class);
        PendingIntent pintent = PendingIntent.getService(getApplicationContext(), 0, name, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 60000, pintent);
        //scan and enable hotspot if OFF
        Constants.hotspotstayOn = true;

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (loading != null) {
            loading.dismiss();
            Constants.hotspotstayOn = true;
            loading = null;
        }
    }

    //Delete log files older than 1 month
    private void DeleteOldLogFiles(){

        File file = new File(Environment.getExternalStorageDirectory() + "/FSLog");
        CommonUtils.getAllFilesInDir(file);

    }



    public void UpdateFSUI_seconds() {

        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
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


                                    } else {

                                        tv_FS1_hoseName.setText(serverSSIDList.get(0).get("WifiSSId"));
                                        tv_FS2_hoseName.setText(serverSSIDList.get(1).get("WifiSSId"));
                                        tv_FS3_hoseName.setText(serverSSIDList.get(2).get("WifiSSId"));
                                        tv_FS4_hoseName.setText(serverSSIDList.get(3).get("WifiSSId"));

                                        linear_fs_1.setVisibility(View.VISIBLE);
                                        linear_fs_2.setVisibility(View.VISIBLE);

                                        linear_fs_3.setVisibility(View.VISIBLE);
                                        /*LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) linear_fs_3.getLayoutParams();
                                        params.height = match_parent; // In dp
                                        linear_fs_3.setLayoutParams(params);*/

                                        linear_fs_4.setVisibility(View.VISIBLE);
                                        /*LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) linear_fs_4.getLayoutParams();
                                        params1.height = match_parent; // In dp
                                        linear_fs_4.setLayoutParams(params1);*/

                                    }
                                }

                                //===Display Dashboard every Second=====
                                DisplayDashboardEveSecond();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();


    }

    public void WriteTimestampToFile() {

        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(600000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("Write TimeStamp to file");
                                AppConstants.WriteTimeStamp("BackgroundServiceWriteTimestampToFile ~~~~~~~~~");
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();


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
                latitude = mLastLocation.getLatitude();
                // AcceptVehicleActivity.CurrentLat = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
                // AcceptVehicleActivity.CurrentLng = mLastLocation.getLongitude();
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

        textDateTime = (TextView) findViewById(R.id.textDateTime);
        tv_fs1_Qty = (TextView) findViewById(R.id.tv_fs1_Qty);
        tv_fs2_Qty = (TextView) findViewById(R.id.tv_fs2_Qty);
        tv_fs3_Qty = (TextView) findViewById(R.id.tv_fs3_Qty);
        tv_fs4_Qty = (TextView) findViewById(R.id.tv_fs4_Qty);
        tv_FS2_hoseName = (TextView) findViewById(R.id.tv_FS2_hoseName);
        tv_FS1_hoseName = (TextView) findViewById(R.id.tv_FS1_hoseName);
        tv_FS3_hoseName = (TextView) findViewById(R.id.tv_FS3_hoseName);
        tv_FS4_hoseName = (TextView) findViewById(R.id.tv_FS4_hoseName);

        tv_fs1_pulseTxt = (TextView) findViewById(R.id.tv_fs1_pulseTxt);
        tv_fs2_pulseTxt = (TextView) findViewById(R.id.tv_fs2_pulseTxt);
        tv_fs3_pulseTxt = (TextView) findViewById(R.id.tv_fs3_pulseTxt);
        tv_fs4_pulseTxt = (TextView) findViewById(R.id.tv_fs4_pulseTxt);

        tv_fs1_Pulse = (TextView) findViewById(R.id.tv_fs1_Pulse);
        tv_fs2_Pulse = (TextView) findViewById(R.id.tv_fs2_Pulse);
        tv_fs3_Pulse = (TextView) findViewById(R.id.tv_fs3_Pulse);
        tv_fs4_Pulse = (TextView) findViewById(R.id.tv_fs4_Pulse);

        tv_fs1_stop = (TextView) findViewById(R.id.tv_fs1_stop);
        tv_fs2_stop = (TextView) findViewById(R.id.tv_fs2_stop);
        tv_fs3_stop = (TextView) findViewById(R.id.tv_fs3_stop);
        tv_fs4_stop = (TextView) findViewById(R.id.tv_fs4_stop);

        tv_NFS1 = (TextView) findViewById(R.id.tv_NFS1);
        tv_NFS2 = (TextView) findViewById(R.id.tv_NFS2);
        tv_NFS3 = (TextView) findViewById(R.id.tv_NFS3);
        tv_NFS4 = (TextView) findViewById(R.id.tv_NFS4);

        tv_fs1QTN = (TextView) findViewById(R.id.tv_fs1QTN);
        tv_fs2QTN = (TextView) findViewById(R.id.tv_fs2QTN);
        tv_fs3QTN = (TextView) findViewById(R.id.tv_fs3QTN);
        tv_fs4QTN = (TextView) findViewById(R.id.tv_fs4QTN);

        imgFuelLogo = (ImageView) findViewById(R.id.imgFuelLogo);
        linearHose = (LinearLayout) findViewById(R.id.linearHose);
        linear_fs_1 = (LinearLayout) findViewById(R.id.linear_fs_1);
        linear_fs_2 = (LinearLayout) findViewById(R.id.linear_fs_2);
        linear_fs_3 = (LinearLayout) findViewById(R.id.linear_fs_3);
        linear_fs_4 = (LinearLayout) findViewById(R.id.linear_fs_4);

        tv_fs1_stop.setOnClickListener(this);
        tv_fs2_stop.setOnClickListener(this);
        tv_fs3_stop.setOnClickListener(this);
        tv_fs4_stop.setOnClickListener(this);

        btnGo = (Button) findViewById(R.id.btnGo);
        btnRetryWifi = (Button) findViewById(R.id.btnRetryWifi);
    }

    public void selectHoseAction(View v) {

        new GetConnectedDevicesIP().execute();//Refreshed donnected devices list on hose selection.
        refreshWiFiList();
        //alertSelectHoseList(tvLatLng.getText().toString() + "\n");
    }

    public void goButtonAction(View view) {


        try {

            boolean flagGo = false;

            LocationManager locationManager = (LocationManager) WelcomeActivity.this.getSystemService(Context.LOCATION_SERVICE);
            boolean statusOfGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                if (!statusOfGPS) {
                    turnGPSOn();
                } else {
                    flagGo = true;
                }

            } else {
                flagGo = true;

            }


            if (flagGo) {

                if (SelectedItemPos >= 0) {

                    if (serverSSIDList.size() > 0) {

                        String selectedSSID = serverSSIDList.get(SelectedItemPos).get("WifiSSId");
                        String ReplaceableHoseName = serverSSIDList.get(SelectedItemPos).get("ReplaceableHoseName");
                        String IsHoseNameReplaced = serverSSIDList.get(SelectedItemPos).get("IsHoseNameReplaced");
                        String SiteId = serverSSIDList.get(SelectedItemPos).get("SiteId");
                        String HoseId = serverSSIDList.get(SelectedItemPos).get("HoseId");

                        AppConstants.LAST_CONNECTED_SSID = selectedSSID;

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
                            if (cd.isConnectingToInternet()) {

                                //Set FluidSecure Link Busy:
                                String Status_busy = new ChangeBusyStatus().execute().get();
                                JSONObject jsonObject = new JSONObject(Status_busy);
                                String ResponceMessage = jsonObject.getString("ResponceMessage");
                                if (ResponceMessage.equalsIgnoreCase("success")) {

                                    String ResponceText = jsonObject.getString("ResponceText");
                                    System.out.println("eeee1" + ResponceText);
                                    if (ResponceText.equalsIgnoreCase("Y")) {
                                        // AppConstants.colorToastBigFont(WelcomeActivity.this, "Hose in use", Color.RED);
                                        AppConstants.alertBigActivity(WelcomeActivity.this, "Hose in use, Please try After sometime.");
                                    } else {
                                        handleGetAndroidSSID(selectedSSID);
                                    }


                                }


                            } else {
                                CommonUtils.showNoInternetDialog(WelcomeActivity.this);
                            }

                        } else {
                            Toast.makeText(WelcomeActivity.this, "Please try later.", Toast.LENGTH_SHORT).show();
                        }

                        /*
                           // if (ssidList.contains(serverSSIDList.get(SelectedItemPos).get("item"))) {

                        } else {
                            AppConstants.alertBigActivity(WelcomeActivity.this, "Fuel site not available at this location\nPlease try again.");

                            scanLocalWiFi();
                        }*/

                    } else {
                        AppConstants.alertBigActivity(WelcomeActivity.this, "Unable to get Fluid Secure list from server");
                    }
                } else {
                    AppConstants.alertBigActivity(WelcomeActivity.this, "Please select Hose");
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private void handleGetAndroidSSID(String selectedSSID) {

        try {


            UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetails(WelcomeActivity.this);
            //----------------------------------------------------------------------------------

            selectedSSID += "#:#0#:#0";

            System.out.println("selectedSSID.." + selectedSSID);

            GetAndroidSSID getSitListAsynTask = new GetAndroidSSID(userInfoEntity.PersonEmail, selectedSSID);
            getSitListAsynTask.execute();
            getSitListAsynTask.get();

            String siteResponse = getSitListAsynTask.response;

            if (siteResponse != null && !siteResponse.isEmpty()) {

                JSONObject jsonObjectSite = new JSONObject(siteResponse);
                String ResponseMessageSite = jsonObjectSite.getString(AppConstants.RES_MESSAGE);

                if (ResponseMessageSite.equalsIgnoreCase("success")) {


                    String dataSite = jsonObjectSite.getString(AppConstants.RES_DATA_SSID);

                    CommonUtils.SaveDataInPref(WelcomeActivity.this, dataSite, Constants.PREF_COLUMN_SITE);

                    startWelcomeActivity();

                } else if (ResponseMessageSite.equalsIgnoreCase("fail")) {
                    String ResponseTextSite = jsonObjectSite.getString(AppConstants.RES_TEXT);


                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(WelcomeActivity.this);
                    // set title

                    alertDialogBuilder.setTitle("Fuel Secure");
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
            }
            //-------------------------------------------------------------------------
        } catch (Exception ex) {
            CommonUtils.LogMessage(TAG, "handleGetSitListTask", ex);
        }
    }

    private void startWelcomeActivity() {

        btnGo.setClickable(false);

        SharedPreferences sharedPrefODO = WelcomeActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsPersonnelPINRequireForHub = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequireForHub, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");

        //Skip PinActivity and pass pin= "";
        if (Constants.CurrentSelectedHose.equals("FS1")) {
            Constants.AccPersonnelPIN_FS1 = "";
        } else if (Constants.CurrentSelectedHose.equals("FS2")) {
            Constants.AccPersonnelPIN = "";
        } else if (Constants.CurrentSelectedHose.equals("FS3")) {
            Constants.AccPersonnelPIN_FS3 = "";
        } else if (Constants.CurrentSelectedHose.equals("FS4")) {
            Constants.AccPersonnelPIN_FS4 = "";
        }

        Intent intent = new Intent(WelcomeActivity.this, DeviceControlActivity_vehicle.class);
        startActivity(intent);

        /*if (ReaderFrequency.equalsIgnoreCase("hfr")) {
            Intent intent = new Intent(WelcomeActivity.this, AcceptVehicleActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(WelcomeActivity.this, DeviceControlActivity_Pin.class);
            startActivity(intent);

        }*/


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.tv_fs1_stop:

                String selSSID = serverSSIDList.get(0).get("WifiSSId");
                String selMacAddress = serverSSIDList.get(0).get("MacAddress");
                String IpAddress = null;


                for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                    String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                    if (selMacAddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                        IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                        HTTP_URL_FS_1 = "http://" + IpAddress + ":80/";
                    }
                }

                URL_GET_PULSAR_FS1 = HTTP_URL_FS_1 + "client?command=pulsar ";
                URL_SET_PULSAR_FS1 = HTTP_URL_FS_1 + "config?command=pulsar";

                URL_WIFI_FS1 = HTTP_URL_FS_1 + "config?command=wifi";
                URL_RELAY_FS1 = HTTP_URL_FS_1 + "config?command=relay";


                if (IpAddress != "" || IpAddress != null) {

                    stopService(new Intent(WelcomeActivity.this, BackgroundService_AP_PIPE.class));
                    stopButtonFunctionality_FS1();
                    // Constants.FS_1STATUS = "FREE";
                    if (!Constants.BusyVehicleNumberList.equals(null)) {
                        Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS1);
                    }
                    // Toast.makeText(getApplicationContext(), "Fs 1 stop button pressed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Please make sure your connected to FS unit", Toast.LENGTH_SHORT).show();
                }

                //AutoSelect if single hose
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            //#73--Only one FS unit display
                            String selMacAddress = serverSSIDList.get(0).get("MacAddress");
                            if (serverSSIDList != null && serverSSIDList.size() == 1 && !selMacAddress.equals("") && Constants.FS_1STATUS.equalsIgnoreCase("FREE")) {

                                tvSSIDName.setText(serverSSIDList.get(0).get("WifiSSId"));
                                OnHoseSelected_OnClick(Integer.toString(0));

                            }
                        } catch (Exception e) {
                            System.out.println(e);
                            AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "AutoSelect if single hose --Exception " + e);
                        }
                    }
                }, 2000);

                break;

            case R.id.tv_fs2_stop:

                selSSID = serverSSIDList.get(1).get("WifiSSId");
                selMacAddress = serverSSIDList.get(1).get("MacAddress");
                IpAddress = null;


                for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                    String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                    if (selMacAddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                        IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                        HTTP_URL_FS_2 = "http://" + IpAddress + ":80/";
                    }
                }

                URL_GET_PULSAR_FS2 = HTTP_URL_FS_2 + "client?command=pulsar ";
                URL_SET_PULSAR_FS2 = HTTP_URL_FS_2 + "config?command=pulsar";
                URL_WIFI_FS2 = HTTP_URL_FS_2 + "config?command=wifi";
                URL_RELAY_FS2 = HTTP_URL_FS_2 + "config?command=relay";


                if (IpAddress != "" || IpAddress != null) {
                    stopService(new Intent(WelcomeActivity.this, BackgroundService_AP.class));
                    stopButtonFunctionality_FS2();
                    // Constants.FS_2STATUS = "FREE";
                    if (!Constants.BusyVehicleNumberList.equals(null)) {
                        Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber);
                    }
                    // Toast.makeText(getApplicationContext(), "Fs 2 stop button pressed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Please make sure your connected to FS unit", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.tv_fs3_stop:


                selSSID = serverSSIDList.get(2).get("WifiSSId");
                selMacAddress = serverSSIDList.get(2).get("MacAddress");
                IpAddress = null;


                for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                    String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                    if (selMacAddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                        IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                        HTTP_URL_FS_3 = "http://" + IpAddress + ":80/";
                    }
                }

                URL_GET_PULSAR_FS3 = HTTP_URL_FS_3 + "client?command=pulsar ";
                URL_SET_PULSAR_FS3 = HTTP_URL_FS_3 + "config?command=pulsar";
                URL_WIFI_FS3 = HTTP_URL_FS_3 + "config?command=wifi";
                URL_RELAY_FS3 = HTTP_URL_FS_3 + "config?command=relay";


                if (IpAddress != "" || IpAddress != null) {
                    stopService(new Intent(WelcomeActivity.this, BackgroundService_FS_UNIT_3.class));
                    stopButtonFunctionality_FS3();
                    // Constants.FS_3STATUS = "FREE";
                    if (!Constants.BusyVehicleNumberList.equals(null)) {
                        Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS3);
                    }
                    // Toast.makeText(getApplicationContext(), "Fs 2 stop button pressed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Please make sure your connected to FS unit", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.tv_fs4_stop:

                selSSID = serverSSIDList.get(3).get("WifiSSId");
                selMacAddress = serverSSIDList.get(3).get("MacAddress");
                IpAddress = null;


                if (AppConstants.DetailsListOfConnectedDevices != null)
                    for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                        String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                        if (selMacAddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                            IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                            HTTP_URL_FS_4 = "http://" + IpAddress + ":80/";
                        }
                    }

                URL_GET_PULSAR_FS4 = HTTP_URL_FS_4 + "client?command=pulsar ";
                URL_SET_PULSAR_FS4 = HTTP_URL_FS_4 + "config?command=pulsar";
                URL_WIFI_FS4 = HTTP_URL_FS_4 + "config?command=wifi";
                URL_RELAY_FS4 = HTTP_URL_FS_4 + "config?command=relay";


                if (IpAddress != "" || IpAddress != null) {
                    stopService(new Intent(WelcomeActivity.this, BackgroundService_FS_UNIT_4.class));
                    stopButtonFunctionality_FS4();
                    // Constants.FS_4STATUS = "FREE";
                    if (!Constants.BusyVehicleNumberList.equals(null)) {
                        Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS4);
                    }
                    // Toast.makeText(getApplicationContext(), "Fs 2 stop button pressed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Please make sure your connected to FS unit", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public class GetAndroidSSID extends AsyncTask<Void, Void, Void> {

        String Email = null;
        String latLong = null;
        String response = null;

        public GetAndroidSSID(String Email, String latLong) {
            this.Email = Email;
            this.latLong = latLong;
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

                CommonUtils.LogMessage(TAG, "AuthTestAsynTask ", ex);
                AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "GetAndroidSSID --Exception " + ex);
            }
            return null;
        }

    }

    public void onChangeWifiAction(View view) {
        try {

            refreshWiFiList();


        } catch (Exception ex) {
            CommonUtils.LogMessage(TAG, "onChangeWifiAction :", ex);
            AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "onChangeWifiAction --Exception " + ex);
        }
    }

    public void refreshWiFiList() {
        new GetSSIDUsingLocation().execute();
    }

    public void turnGPSOn() {


        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationRequest mLocationRequest1 = new LocationRequest();
        mLocationRequest1.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest)
                .addLocationRequest(mLocationRequest1);


        LocationSettingsRequest mLocationSettingsRequest = builder.build();


        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i("Splash", "All location settings are satisfied.");

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("Splash", "Location settings are not satisfied. Show the user a dialog to" +
                                "upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(WelcomeActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i("Splash", "PendingIntent unable to execute request.");
                            AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "PendingIntent unable to execute request. --Exception " + e);
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("Splash", "Location settings are inadequate, and cannot be fixed here. Dialog " +
                                "not created.");
                        break;
                }
            }
        });


        //Intent in = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        //startActivity(in);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

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

                        break;
                }
                break;
        }
    }

    public class GetSSIDUsingLocation extends AsyncTask<Void, Void, String> {


        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage("Please wait...");
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
                client.setConnectTimeout(10, TimeUnit.SECONDS);
                client.setReadTimeout(10, TimeUnit.SECONDS);
                client.setWriteTimeout(10, TimeUnit.SECONDS);

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
                AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "GetSSIDUsingLocation doInBackground --Exception " + e);
            }


            return resp;
        }


        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            tvLatLng.setText("Current Location :" + Constants.Latitude + "," + Constants.Longitude);

            System.out.println("GetSSIDUsingLocation...." + result);

            try {

                serverSSIDList.clear();
                //AppConstants.DetailsServerSSIDList.clear();

                String errMsg = "";

                if (result != null && !result.isEmpty()) {

                    JSONObject jsonObjectSite = new JSONObject(result);
                    String ResponseMessageSite = jsonObjectSite.getString(AppConstants.RES_MESSAGE);

                    if (ResponseMessageSite.equalsIgnoreCase("success")) {

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
                                String MacAddress = c.getString("MacAddress");
                                String IsBusy = c.getString("IsBusy");
                                String IsUpgrade = c.getString("IsUpgrade");
                                String PulserTimingAdjust = c.getString("PulserTimingAdjust");

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
                                map.put("IsBusy", IsBusy);
                                map.put("IsUpgrade", IsUpgrade);
                                map.put("PulserTimingAdjust", PulserTimingAdjust);

                                if (ResponceMessage.equalsIgnoreCase("success")) {
                                    if (isNotNULL(SiteId) && isNotNULL(HoseId) && isNotNULL(WifiSSId)) {
                                        serverSSIDList.add(map);
                                        AppConstants.DetailsServerSSIDList = serverSSIDList;

                                    }
                                } else {
                                    errMsg = ResponceText;
                                    AppConstants.AlertDialogFinish(WelcomeActivity.this, ResponceText);
                                }
                            }


                        }
                        //HoseList Alert
                        alertSelectHoseList(tvLatLng.getText().toString() + "\n" + errMsg);


                    } else if (ResponseMessageSite.equalsIgnoreCase("fail")) {
                        String ResponseTextSite = jsonObjectSite.getString(AppConstants.RES_TEXT);


                        AppConstants.AlertDialogBox(WelcomeActivity.this, ResponseTextSite);


                    }
                } else {
                    AppConstants.alertBigFinishActivity(WelcomeActivity.this, "Unable to connect server. Please try again later!");
                }


            } catch (Exception e) {

                CommonUtils.LogMessage(TAG, " GetSSIDUsingLocation :" + result, e);
                AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "GetSSIDUsingLocation onPostExecute --Exception " + e);
            }

        }
    }

    public class GetSSIDUsingLocationOnResume extends AsyncTask<Void, Void, String> {


        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage("Please wait...");
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
                client.setConnectTimeout(10, TimeUnit.SECONDS);
                client.setReadTimeout(10, TimeUnit.SECONDS);
                client.setWriteTimeout(10, TimeUnit.SECONDS);

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
                AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "GetSSIDUsingLocation onPostExecute --Exception " + e);
            }


            return resp;
        }


        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            tvLatLng.setText("Current Location :" + Constants.Latitude + "," + Constants.Longitude);

            System.out.println("GetSSIDUsingLocation...." + result);

            try {

                serverSSIDList.clear();
                //AppConstants.DetailsServerSSIDList.clear();

                String errMsg = "";

                if (result != null && !result.isEmpty()) {

                    JSONObject jsonObjectSite = new JSONObject(result);
                    String ResponseMessageSite = jsonObjectSite.getString(AppConstants.RES_MESSAGE);

                    if (ResponseMessageSite.equalsIgnoreCase("success")) {

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
                                String MacAddress = c.getString("MacAddress");
                                String IsBusy = c.getString("IsBusy");
                                String IsUpgrade = c.getString("IsUpgrade");
                                String PulserTimingAdjust = c.getString("PulserTimingAdjust");
                                String BluetoothCardReaderHF = c.getString("BluetoothCardReaderHF");

                                AppConstants.BT_READER_NAME = BluetoothCardReaderHF;


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
                                map.put("IsBusy", IsBusy);
                                map.put("PulserTimingAdjust", PulserTimingAdjust);

                                if (ResponceMessage.equalsIgnoreCase("success")) {
                                    if (isNotNULL(SiteId) && isNotNULL(HoseId) && isNotNULL(WifiSSId)) {
                                        serverSSIDList.add(map);
                                        AppConstants.DetailsServerSSIDList = serverSSIDList;

                                    }
                                } else {
                                    errMsg = ResponceText;
                                    AppConstants.AlertDialogFinish(WelcomeActivity.this, ResponceText);
                                }
                            }


                        }
                        //HoseList Alert
                        //alertSelectHoseList(tvLatLng.getText().toString() + "\n" + errMsg);

                        //#73--Only one FS unit display
                        try {
                            String selMacAddress = serverSSIDList.get(0).get("MacAddress");
                            if (serverSSIDList != null && serverSSIDList.size() == 1 && !selMacAddress.equals("") && Constants.FS_1STATUS.equalsIgnoreCase("FREE")) {

                                tvSSIDName.setText(serverSSIDList.get(0).get("WifiSSId"));
                                OnHoseSelected_OnClick(Integer.toString(0));

                            }
                        } catch (Exception e) {
                            System.out.println(e);
                            AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "GetSSIDUsingLocationOnResume if only one hose autoselect   --Exception " + e);
                        }


                    } else if (ResponseMessageSite.equalsIgnoreCase("fail")) {
                        String ResponseTextSite = jsonObjectSite.getString(AppConstants.RES_TEXT);


                        AppConstants.AlertDialogBox(WelcomeActivity.this, ResponseTextSite);


                    }
                } else {
                    AppConstants.alertBigFinishActivity(WelcomeActivity.this, "Unable to connect server. Please try again later!");
                }


            } catch (Exception e) {

                CommonUtils.LogMessage(TAG, " GetSSIDUsingLocation :" + result, e);
                AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "GetSSIDUsingLocationOnResume --Exception " + e);

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
        dialog.setTitle("Fuel Secure");
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
                //OnHoseSelected_OnClick(Integer.toString(position));
                //new GetConnectedDevicesIP().execute();//Refreshed donnected devices list on hose selection.

                IpAddress = "";
                SelectedItemPos = position;
                String selSSID = serverSSIDList.get(SelectedItemPos).get("WifiSSId");
                String IsBusy = serverSSIDList.get(SelectedItemPos).get("IsBusy");
                String selMacAddress = serverSSIDList.get(SelectedItemPos).get("MacAddress");
                String selSiteId = serverSSIDList.get(SelectedItemPos).get("SiteId");
                String hoseID = serverSSIDList.get(SelectedItemPos).get("HoseId");
                String IsUpgrade = serverSSIDList.get(SelectedItemPos).get("IsUpgrade"); //"Y";
                AppConstants.CURRENT_SELECTED_SSID = selSSID;
                AppConstants.CURRENT_HOSE_SSID = hoseID;
                AppConstants.CURRENT_SELECTED_SITEID = selSiteId;
                AppConstants.SELECTED_MACADDRESS = selMacAddress;
                String IsHoseNameReplaced = serverSSIDList.get(SelectedItemPos).get("IsHoseNameReplaced");
                String ReplaceableHoseName = serverSSIDList.get(SelectedItemPos).get("ReplaceableHoseName");
                String PulserTimingAd = serverSSIDList.get(SelectedItemPos).get("PulserTimingAdjust");
                AppConstants.PulserTimingAdjust = PulserTimingAd;

                //Firmware upgrade
                System.out.println("IsUpgradeIsUpgrade: " + IsUpgrade);

                if (IsUpgrade.trim().equalsIgnoreCase("Y"))
                    AppConstants.UP_Upgrade = true;
                else
                    AppConstants.UP_Upgrade = false;

                if (String.valueOf(position).equalsIgnoreCase("0")) {

                    AppConstants.UP_HoseId_fs1 = hoseID;
                    if (IsUpgrade.trim().equalsIgnoreCase("Y"))
                        AppConstants.UP_Upgrade_fs1 = true;
                    else
                        AppConstants.UP_Upgrade_fs1 = false;

                } else if (String.valueOf(position).equalsIgnoreCase("1")) {

                    AppConstants.UP_HoseId_fs2 = hoseID;
                    if (IsUpgrade.trim().equalsIgnoreCase("Y"))
                        AppConstants.UP_Upgrade_fs2 = true;
                    else
                        AppConstants.UP_Upgrade_fs2 = false;

                } else if (String.valueOf(position).equalsIgnoreCase("2")) {

                    AppConstants.UP_HoseId_fs3 = hoseID;
                    if (IsUpgrade.trim().equalsIgnoreCase("Y"))
                        AppConstants.UP_Upgrade_fs3 = true;
                    else
                        AppConstants.UP_Upgrade_fs3 = false;

                } else {

                    AppConstants.UP_HoseId_fs4 = hoseID;
                    if (IsUpgrade.trim().equalsIgnoreCase("Y"))
                        AppConstants.UP_Upgrade_fs4 = true;
                    else
                        AppConstants.UP_Upgrade_fs4 = false;

                }

                //Rename SSID while mac address updation
                if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {
                    AppConstants.NeedToRenameFS_ON_UPDATE_MAC = false;
                    AppConstants.REPLACEBLE_WIFI_NAME_FS_ON_UPDATE_MAC = "";
                } else {
                    AppConstants.NeedToRenameFS_ON_UPDATE_MAC = true;
                    AppConstants.REPLACEBLE_WIFI_NAME_FS_ON_UPDATE_MAC = ReplaceableHoseName;
                }

                if (selMacAddress.trim().equals("")) {  //MacAddress on server is null

                    if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE")) {

                        try {

                            loading = new ProgressDialog(WelcomeActivity.this);
                            loading.setCancelable(true);
                            loading.setMessage("Updating mac address please wait..");
                            loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            loading.setCancelable(false);
                            loading.show();

                            //Do not enable hotspot.
                            Constants.hotspotstayOn = false;

                            //AppConstants.colorToast(WelcomeActivity.this, "Updating mac address please wait..", Color.RED);
                            wifiApManager.setWifiApEnabled(null, false);  //Hotspot disabled

                            // Toast.makeText(getApplicationContext(),"Enabled WIFI connecting to "+AppConstants.CURRENT_SELECTED_SSID,Toast.LENGTH_LONG).show();

                            WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                            if (!wifiManagerMM.isWifiEnabled()) {
                                wifiManagerMM.setWifiEnabled(true);
                            }

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //new ChangeSSIDofHubStation().execute(); //Connect to selected (SSID) and Rename UserName and password of Fs unit
                                    new WiFiConnectTask().execute(); //1)Connect to selected (SSID) wifi network and 2)change the ssid and password settings to connect to Hub's hotspot 3)Update MackAddress
                                }
                            }, 1000);

                        }catch (Exception e){
                         loading.dismiss();
                         AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "Updating mac address please wait.. --Exception " + e);

                        }


                    } else {
                        AppConstants.colorToastBigFont(WelcomeActivity.this, "Can't update mac address,Hose is busy please retry later.", Color.RED);
                        btnGo.setVisibility(View.GONE);
                    }

                } else {

                    try {
                        for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                            String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                            if (selMacAddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                                IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                            }
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "DetailsListOfConnectedDevices --Empty ");
                    }

                    if (IpAddress.equals("")) {
                        tvSSIDName.setText("Can't select this Hose not connected");
                        btnGo.setVisibility(View.GONE);

                    } else {

                        //Selected position
                        //Toast.makeText(getApplicationContext(), "FS Position" + position, Toast.LENGTH_SHORT).show();
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
                            } else {
                                tvSSIDName.setText("Hose in use.\nPlease try again later");
                                btnGo.setVisibility(View.GONE);

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
                                tvSSIDName.setText("Hose in use.\nPlease try again later");
                                btnGo.setVisibility(View.GONE);
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
                                tvSSIDName.setText("Hose in use.\nPlease try again later");
                                btnGo.setVisibility(View.GONE);
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
                                tvSSIDName.setText("Hose in use.\nPlease try again later");
                                btnGo.setVisibility(View.GONE);
                            }
                        } else {

                            tvSSIDName.setText("Can't select this Hose for current version");
                            btnGo.setVisibility(View.GONE);
                        }
                    }

                }
                dialog.dismiss();

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

    public class GetConnectedDevicesIP extends AsyncTask<String, Void, String> {
        ProgressDialog dialog;


        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(WelcomeActivity.this);
            dialog.setMessage("Fetching connected device info..");
            dialog.setCancelable(false);
            dialog.show();

        }

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
                                boolean isReachable = InetAddress.getByName(
                                        splitted[0]).isReachable(500);  // this is network call so we cant do that on UI thread, so i take background thread.
                                if (isReachable) {
                                    Log.d("Device Information", ipAddress + " : "
                                            + macAddress);
                                }

                                if (ipAddress != null || macAddress != null) {

                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("ipAddress", ipAddress);
                                    map.put("macAddress", macAddress);

                                    ListOfConnectedDevices.add(map);

                                }
                                AppConstants.DetailsListOfConnectedDevices = ListOfConnectedDevices;
                                System.out.println("DeviceConnected" + ListOfConnectedDevices);

                            }

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "GetConnectedDevicesIP 1 --Exception " + e);
                    } finally {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "GetConnectedDevicesIP 2 --Exception " + e);
                        }
                    }
                }
            });
            thread.start();


            return resp;


        }


        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            String strJson = result;


            dialog.dismiss();

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
                AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "CommandsPOST  DoInBackground--Exception " + e);
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

                System.out.println(e);
                AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "CommandsPOST  onPostExecute --Exception " + e);
            }

        }
    }

    public class CommandsGET_INFO extends AsyncTask<String, Void, String> {

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
                AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "CommandsGET_INFO  DoInBackground --Exception " + e);
                Log.d("Ex", e.getMessage());
                Constants.hotspotstayOn = true;
                loading.dismiss();
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            System.out.println(" resp......." + result);
            System.out.println("2:" + Calendar.getInstance().getTime());

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
                loading.dismiss();
                Constants.hotspotstayOn = true;
                Log.d("Ex", e.getMessage());
                AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "CommandsPOST_ChangeHotspotSettings  DoInBackground --Exception " + e);
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {


            try {

                consoleString += "OUTPUT- " + result + "\n";
                //loading.dismiss();


                System.out.println(result);

            } catch (Exception e) {
                loading.dismiss();
                Constants.hotspotstayOn = true;
                System.out.println(e);
                AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "CommandsPOST_ChangeHotspotSettings  onpostExecution --Exception " + e);
            }

        }
    }

    private class WiFiConnectTask extends AsyncTask<String, Void, String> {
        // Do the long-running work in here
        protected String doInBackground(String... asd) {

/*
            //Forget Netwotk
            try {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(android.content.Context.WIFI_SERVICE);
                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                for (WifiConfiguration i : list) {

                    if (i.SSID.equalsIgnoreCase("\"" + AppConstants.CURRENT_SELECTED_SSID + "\"")) {
                        wifiManager.disableNetwork(i.networkId);
                        break;
                    }
                    // wifiManager.removeNetwork(i.networkId);
                    // wifiManager.saveConfiguration();
                }
            }catch (NullPointerException e){System.out.println(e);}*/
            connectToWifiMarsh(AppConstants.CURRENT_SELECTED_SSID);
            // connectCustom(AppConstants.CURRENT_SELECTED_SSID);

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
                    if (!(NetID == -1) && ssid.contains("\"" + AppConstants.CURRENT_SELECTED_SSID + "\"")) {

                        AppConstants.WriteinFile("WelcomeActivity~~~~~~~~~" + "WIFI CONNECTED " + AppConstants.CURRENT_SELECTED_SSID);
                        AppConstants.colorToastBigFont(WelcomeActivity.this, "CONNECTED TO: " + AppConstants.CURRENT_SELECTED_SSID, Color.BLUE);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                AppConstants.colorToastBigFont(WelcomeActivity.this, "  Changing ssid and password settings  ", Color.BLUE);
                                HTTP_URL = "http://192.168.4.1/";
                                URL_UPDATE_FS_INFO = HTTP_URL + "config?command=wifi";

                                String jsonChangeUsernamePass = "{\"Request\":{\"Station\":{\"Connect_Station\":{\"ssid\":\"" + AppConstants.HubName + "\",\"password\":\"" + AppConstants.HubGeneratedpassword + "\" ,\"sta_connect\":1 }}}}";
                                //String jsonChangeUsernamePass = "{\"Request\":{\"Station\":{\"Connect_Station\":{\"ssid\":\"" + AppConstants.HubName + "\",\"password\":\"" + AppConstants.HubGeneratedpassword + "\"}}}}";

                                new CommandsPOST_ChangeHotspotSettings().execute(URL_UPDATE_FS_INFO, jsonChangeUsernamePass);

                                btnRetryWifi.setVisibility(View.GONE);

                            }
                        }, 1000);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                HTTP_URL = "http://192.168.4.1:80/";
                                URL_INFO = HTTP_URL + "client?command=info";
                                try {
                                    String result = new CommandsGET_INFO().execute(URL_INFO).get();

                                    String mac_address = "";
                                    if (result.contains("Version")) {
                                        JSONObject jsonObject = new JSONObject(result);
                                        JSONObject joPulsarStat = jsonObject.getJSONObject("Version");
                                        String sdk_version = joPulsarStat.getString("sdk_version");
                                        String iot_version = joPulsarStat.getString("iot_version");
                                        mac_address = joPulsarStat.getString("mac_address");//station_mac_address

                                        if (mac_address.equals("")) {
                                            loading.dismiss();
                                            Constants.hotspotstayOn = true;
                                            AppConstants.colorToastBigFont(WelcomeActivity.this, "Could not get mac address", Color.RED);
                                            WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                                            if (wifiManagerMM.isWifiEnabled()) {
                                                wifiManagerMM.setWifiEnabled(false);
                                            }
                                            //wifiApManager.setWifiApEnabled(null, true);//enable hotspot

                                        } else {
                                            //Rename FluidSecure Unite
                                            RenameLink();

                                            AppConstants.UPDATE_MACADDRESS = mac_address;
                                            AppConstants.colorToastBigFont(WelcomeActivity.this, "Mac address " + mac_address, Color.BLUE);
                                            WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                                            if (wifiManagerMM.isWifiEnabled()) {
                                                wifiManagerMM.setWifiEnabled(false);
                                            }

                                            // wifiApManager.setWifiApEnabled(null, true);//enable hotspot
                                            // -----------------------------

                                            try {

                                                UpdateMacAddressClass authEntityClass1 = new UpdateMacAddressClass();
                                                authEntityClass1.SiteId = Integer.parseInt(AppConstants.CURRENT_SELECTED_SITEID);
                                                authEntityClass1.MACAddress = AppConstants.UPDATE_MACADDRESS;
                                                authEntityClass1.RequestFrom = "AP";
                                                authEntityClass1.HubName = AppConstants.HubName;

                                                //------
                                                Gson gson = new Gson();
                                                final String jsonData = gson.toJson(authEntityClass1);

                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {


                                                        cd = new ConnectionDetector(WelcomeActivity.this);
                                                        if (cd.isConnectingToInternet()) {

                                                            new UpdateMacAsynTask().execute(jsonData);//,AppConstants.getIMEI(WelcomeActivity.this)
                                                            //Update SSID change status to server
                                                            UpdateSSIDStatusToServer();

                                                        } else {
                                                            AppConstants.colorToast(WelcomeActivity.this, "Please check Internet Connection and retry.", Color.RED);
                                                            // loading.dismiss();
                                                            // new UpdateMacAsynTask().execute(jsonData);
                                                        }

                                                    }
                                                }, 8000);

                                            } catch (Exception e) {
                                                loading.dismiss();
                                                Constants.hotspotstayOn = true;
                                                System.out.println(e);
                                                AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "WiFiConnectTask  UpdateMacAddressClass --Exception " + e);
                                            }

                                        }
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "WiFiConnectTask  OnPostExecution --Exception " + e);
                                }
                            }
                        }, 2000);

                       /* //Check For Rename
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                RenameLink();
                            }
                        }, 3000);
*/


                    } else {
                        //String autoNetworkSwitch = getExternalString(DisplayMeterActivity.this, "com.android.settings","wifi_watchdog_connectivity_check", "Unknown");
                        AppConstants.WriteinFile("WelcomeActivity~~~~~~~~~" + "WIFI NOT CONNECTED " + AppConstants.CURRENT_SELECTED_SSID);
                        RetryOneAtemptConnectToSelectedSSSID += 1;
                        if (RetryOneAtemptConnectToSelectedSSSID < 4) {
                            //gooooo
                            AppConstants.colorToastBigFont(getApplicationContext(), "Attempt:" + RetryOneAtemptConnectToSelectedSSSID + "\nReconnecting to " + AppConstants.CURRENT_SELECTED_SSID, Color.RED);
                            new WiFiConnectTask().execute();
                        } else {

                            btnRetryWifi.setVisibility(View.VISIBLE);

                            if (!isTCancelled)
                                AlertSettings(WelcomeActivity.this, "Unable to connect " + AppConstants.CURRENT_SELECTED_SSID + "!\n\nPlease connect to " + AppConstants.CURRENT_SELECTED_SSID + " manually using the 'WIFI settings' screen.\nThen hit back and click on the 'START' button to continue.");
                        }

                    }
                }
            }, 12000);

        }
    }

    //Connect to wifi with Password
    public void connectToWifiMarsh(String networkSSID) {


        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(android.content.Context.WIFI_SERVICE);
            WifiConfiguration wc = new WifiConfiguration();


            wc.SSID = "\"" + networkSSID + "\"";
            wc.preSharedKey = "\"" + Constants.CurrFsPass + "\"";

            wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);


            wifiManager.setWifiEnabled(true);
            int netId = getExistingNetworkId(networkSSID);

            if (netId == -1) {
                netId = wifiManager.addNetwork(wc);
            }

            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();
        } catch (Exception e) {
            e.printStackTrace();
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
        android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(ctx);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                }

        );

        android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
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
                loading.dismiss();
                Constants.hotspotstayOn = true;
                CommonUtils.LogMessage("", "UpdateMACAddress ", ex);
                AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "UpdateMacAsynTask doInBackground--Exception " + ex);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String serverRes) {

            try {
                if (serverRes != null) {


                    JSONObject jsonObject1 = new JSONObject(serverRes);

                    String ResponceMessage = jsonObject1.getString("ResponceMessage");


                    if (ResponceMessage.equalsIgnoreCase("success")) {


                        loading.dismiss();
                        Constants.hotspotstayOn = true;
                        AppConstants.colorToastBigFont(WelcomeActivity.this, " Mac Address Updated ", Color.parseColor("#4CAF50"));
                        wifiApManager.setWifiApEnabled(null, true);


                    } else if (ResponceMessage.equalsIgnoreCase("fail")) {
                        loading.dismiss();
                        Constants.hotspotstayOn = true;
                        AppConstants.colorToastBigFont(WelcomeActivity.this, " Could not Updated mac address ", Color.RED);
                        wifiApManager.setWifiApEnabled(null, true);
                    }

                } else {
                    CommonUtils.showNoInternetDialog(WelcomeActivity.this);
                }
            } catch (Exception e) {

                AppConstants.WriteinFile("welcomeActivity ~~~~~~~~~" + "UpdateMacAsynTask onPostExecute--Exception " + e);

            }
        }
    }

    public boolean setHotspotNamePassword(Context context) {//String newName, String newKey,
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
            Method getConfigMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
            WifiConfiguration wifiConfig = (WifiConfiguration) getConfigMethod.invoke(wifiManager);


            //Temp###########
           /* String CurrentHotspotName = wifiConfig.SSID;
            String CurrentHotspotPassword = wifiConfig.preSharedKey;
            if (CurrentHotspotName.equals("FS_AP_TEST") && CurrentHotspotPassword.equals("12345678")) {
                //Do nothing
            } else {

                wifiConfig.SSID = "FS_AP_TEST";
                wifiConfig.preSharedKey = "12345678";

               *//* //Disable hotspot
                wifiApManager.setWifiApEnabled(null, false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        //Enable Hotsopt
                        wifiApManager.setWifiApEnabled(null, true);

                    }
                }, 100);*//*

                AppConstants.AlertDialogFinish(WelcomeActivity.this, "Configured Hotspot, Please restart the application");

            }*/

            //Temp##########

            AppConstants.HubGeneratedpassword = PasswordGeneration();
            String CurrentHotspotName = wifiConfig.SSID;
            String CurrentHotspotPassword = wifiConfig.preSharedKey;
            if (CurrentHotspotName.equals(AppConstants.HubName) && CurrentHotspotPassword.equals(AppConstants.HubGeneratedpassword)) {
                //No need to change hotspot username password

            } else {

                wifiConfig.SSID = AppConstants.HubName;
                wifiConfig.preSharedKey = AppConstants.HubGeneratedpassword;

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
    public void stopButtonFunctionality_FS1() {

        //it stops pulsar logic------
        stopTimer = false;


        new CommandsPOST_FS1().execute(URL_RELAY_FS1, jsonRelayOff);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    String cntA = "0", cntB = "0", cntC = "0";

                    for (int i = 0; i < 3; i++) {

                        String result = new GETFINALPulsar_FS1().execute(URL_GET_PULSAR_FS1).get();


                        if (result.contains("pulsar_status")) {

                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject joPulsarStat = jsonObject.getJSONObject("pulsar_status");
                            String counts = joPulsarStat.getString("counts");
                            //String pulsar_status = joPulsarStat.getString("pulsar_status");
                            //String pulsar_secure_status = joPulsarStat.getString("pulsar_secure_status");

                            convertCountToQuantity_fs1(counts);


                            if (i == 2) {

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finalLastStep_fs1();
                                    }
                                }, 1000);


                            }


                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }, 1000);


    }

    public class CommandsPOST_FS1 extends AsyncTask<String, Void, String> {

        public String resp = "";

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
        }

        protected String doInBackground(String... param) {

            System.out.println("url" + HTTP_URL_FS_1);
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
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            try {

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

        new CommandsPOST_FS1().execute(URL_SET_PULSAR_FS1, jsonPulsarOff);

    }

    //=======FS UNIT 2 =========
    public void stopButtonFunctionality_FS2() {

        //it stops pulsar logic------
        stopTimer = false;


        new CommandsPOST_FS2().execute(URL_RELAY_FS2, jsonRelayOff);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    String cntA = "0", cntB = "0", cntC = "0";

                    for (int i = 0; i < 3; i++) {

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
                                }, 1000);


                            }


                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }, 1000);


    }

    public class CommandsPOST_FS2 extends AsyncTask<String, Void, String> {

        public String resp = "";

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
        }

        protected String doInBackground(String... param) {

            System.out.println("url" + HTTP_URL_FS_2);
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
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            try {

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


        new CommandsPOST_FS2().execute(URL_SET_PULSAR_FS2, jsonPulsarOff);


    }

    //=======FS UNIT 3 =========
    public void stopButtonFunctionality_FS3() {

        //it stops pulsar logic------
        stopTimer = false;


        new CommandsPOST_FS3().execute(URL_RELAY_FS3, jsonRelayOff);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    String cntA = "0", cntB = "0", cntC = "0";

                    for (int i = 0; i < 3; i++) {

                        String result = new GETFINALPulsar_FS3().execute(URL_GET_PULSAR_FS3).get();


                        if (result.contains("pulsar_status")) {

                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject joPulsarStat = jsonObject.getJSONObject("pulsar_status");
                            String counts = joPulsarStat.getString("counts");
                            //String pulsar_status = joPulsarStat.getString("pulsar_status");
                            //String pulsar_secure_status = joPulsarStat.getString("pulsar_secure_status");

                            convertCountToQuantity_fs3(counts);


                            if (i == 2) {

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finalLastStep_fs3();
                                    }
                                }, 1000);


                            }


                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }, 1000);


    }

    public class CommandsPOST_FS3 extends AsyncTask<String, Void, String> {

        public String resp = "";

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
        }

        protected String doInBackground(String... param) {

            System.out.println("url" + HTTP_URL_FS_3);
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
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            try {

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

        new CommandsPOST_FS3().execute(URL_SET_PULSAR_FS3, jsonPulsarOff);

    }

    //=======FS UNIT 4 =========
    public void stopButtonFunctionality_FS4() {

        //it stops pulsar logic------
        stopTimer = false;


        new CommandsPOST_FS4().execute(URL_RELAY_FS4, jsonRelayOff);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    String cntA = "0", cntB = "0", cntC = "0";

                    for (int i = 0; i < 3; i++) {

                        String result = new GETFINALPulsar_FS4().execute(URL_GET_PULSAR_FS4).get();


                        if (result.contains("pulsar_status")) {

                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject joPulsarStat = jsonObject.getJSONObject("pulsar_status");
                            String counts = joPulsarStat.getString("counts");
                            //String pulsar_status = joPulsarStat.getString("pulsar_status");
                            //String pulsar_secure_status = joPulsarStat.getString("pulsar_secure_status");

                            convertCountToQuantity_fs4(counts);


                            if (i == 2) {

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finalLastStep_fs4();
                                    }
                                }, 1000);


                            }


                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }, 1000);


    }

    public class CommandsPOST_FS4 extends AsyncTask<String, Void, String> {

        public String resp = "";

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
        }

        protected String doInBackground(String... param) {

            System.out.println("url" + HTTP_URL_FS_4);
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
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            try {

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

        new CommandsPOST_FS4().execute(URL_SET_PULSAR_FS4, jsonPulsarOff);

    }

    public void DisplayDashboardEveSecond() {
        // Toast.makeText(getApplicationContext(),"FS_Count"+FS_Count,Toast.LENGTH_SHORT).show();
        if (Constants.FS_1STATUS.equalsIgnoreCase("FREE")) {

            tv_fs1_Qty.setText(Constants.FS_1Gallons);
            tv_fs1_Pulse.setText(Constants.FS_1Pulse);
            tv_fs1_stop.setClickable(false);

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
            tv_fs1_stop.setClickable(true);
        }

        if (Constants.FS_2STATUS.equalsIgnoreCase("FREE")) {

            tv_fs2_Qty.setText(Constants.FS_2Gallons);
            tv_fs2_Pulse.setText(Constants.FS_2Pulse);
            tv_fs2_stop.setClickable(false);

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
            tv_fs2_stop.setClickable(true);
        }

        if (Constants.FS_3STATUS.equalsIgnoreCase("FREE")) {

            tv_fs3_Qty.setText(Constants.FS_3Gallons);
            tv_fs3_Pulse.setText(Constants.FS_3Pulse);
            tv_fs3_stop.setClickable(false);

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
            tv_fs3_stop.setClickable(true);
        }

        if (Constants.FS_4STATUS.equalsIgnoreCase("FREE")) {

            tv_fs4_Qty.setText(Constants.FS_4Gallons);
            tv_fs4_Pulse.setText(Constants.FS_4Pulse);
            tv_fs4_stop.setClickable(false);

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
            tv_fs4_stop.setClickable(true);
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
            }
        }
    }

    public void RenameLink() {

        HTTP_URL = "http://192.168.4.1/";
        String URL_WIFI = HTTP_URL + "config?command=wifi";
        jsonRename = "{\"Request\":{\"SoftAP\":{\"Connect_SoftAP\":{\"authmode\":\"WPAPSK/WPA2PSK\",\"channel\":6,\"ssid\":\"" + AppConstants.REPLACEBLE_WIFI_NAME_FS_ON_UPDATE_MAC + "\",\"password\":\"123456789\"}}}}";

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


        @Override
        protected void onPreExecute() {


        }

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
            }


            return resp;
        }


        @Override
        protected void onPostExecute(String result) {
            try {

                System.out.println("Wifi renamed on server---" + result);

            } catch (Exception e) {
                System.out.println("eeee" + e);
            }
        }


    }

    public void OnHoseSelected_OnClick(String position) {


        //new GetConnectedDevicesIP().execute();//Refreshed donnected devices list on hose selection.
        String IpAddress = "";
        SelectedItemPos = Integer.parseInt(position);
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

        //Rename SSID while mac address updation
        if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {
            AppConstants.NeedToRenameFS_ON_UPDATE_MAC = false;
            AppConstants.REPLACEBLE_WIFI_NAME_FS_ON_UPDATE_MAC = "";
        } else {
            AppConstants.NeedToRenameFS_ON_UPDATE_MAC = true;
            AppConstants.REPLACEBLE_WIFI_NAME_FS_ON_UPDATE_MAC = ReplaceableHoseName;
        }

        if (selMacAddress.trim().equals("")) {  //MacAddress on server is null

            if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE")) {

                loading = new ProgressDialog(WelcomeActivity.this);
                loading.setCancelable(true);
                loading.setMessage("Updating mac address please wait..");
                loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                loading.setCancelable(false);
                loading.show();

                //Do not enable hotspot.
                Constants.hotspotstayOn = false;

                //AppConstants.colorToast(WelcomeActivity.this, "Updating mac address please wait..", Color.RED);
                wifiApManager.setWifiApEnabled(null, false);  //Hotspot disabled

                // Toast.makeText(getApplicationContext(),"Enabled WIFI connecting to "+AppConstants.CURRENT_SELECTED_SSID,Toast.LENGTH_LONG).show();

                WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                if (!wifiManagerMM.isWifiEnabled()) {
                    wifiManagerMM.setWifiEnabled(true);
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //new ChangeSSIDofHubStation().execute(); //Connect to selected (SSID) and Rename UserName and password of Fs unit
                        new WiFiConnectTask().execute(); //1)Connect to selected (SSID) wifi network and 2)change the ssid and password settings to connect to Hub's hotspot 3)Update MackAddress
                    }
                }, 1000);


            } else {
                AppConstants.colorToastBigFont(WelcomeActivity.this, "Can't update mac address,Hose is busy please retry later.", Color.RED);
                btnGo.setVisibility(View.GONE);
            }

        } else {

            try {
                for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                    String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                    if (selMacAddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                        IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            }

            if (IpAddress.equals("")) {
                tvSSIDName.setText("Can't select this Hose not connected");
                btnGo.setVisibility(View.GONE);

            } else {

                //Selected position
                //Toast.makeText(getApplicationContext(), "FS Position" + position, Toast.LENGTH_SHORT).show();
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
                    } else {
                        tvSSIDName.setText("Hose in use.\nPlease try again later");
                        btnGo.setVisibility(View.GONE);

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
                        tvSSIDName.setText("Hose in use.\nPlease try again later");
                        btnGo.setVisibility(View.GONE);
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
                        tvSSIDName.setText("Hose in use.\nPlease try again later");
                        btnGo.setVisibility(View.GONE);
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
                        tvSSIDName.setText("Hose in use.\nPlease try again later");
                        btnGo.setVisibility(View.GONE);
                    }
                } else {

                    tvSSIDName.setText("Can't select this Hose for current version");
                    btnGo.setVisibility(View.GONE);
                }
            }

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

                // TODO: remove log message
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


                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            transmitEscapeCommend();

                                        }
                                    }, 500);


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
                                AppConstants.APDU_FOB_KEY = fobnum;


                                mTxtResponseApdu.setText(fobnum);

                                System.out.println("Result APDU " + fobnum);

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
                                mTxtEscapeResponse.setText(getResponseString(
                                        response, errorCode));
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
                                } else {
                                    Toast.makeText(WelcomeActivity.this,
                                            "The device is ready to use!",
                                            Toast.LENGTH_SHORT).show();
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
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.reader, menu);

        //Hide Connect/Disconnect Reader titlebar button
        menu.findItem(R.id.menu_connect).setVisible(false);
        menu.findItem(R.id.menu_connecting).setVisible(false);
        menu.findItem(R.id.menu_disconnect).setVisible(false);

//        if (mConnectState == BluetoothReader.STATE_CONNECTED) {
//            menu.findItem(R.id.menu_connect).setVisible(false);
//            menu.findItem(R.id.menu_connecting).setVisible(false);
//            menu.findItem(R.id.menu_disconnect).setVisible(true);
//        } else if (mConnectState == BluetoothReader.STATE_CONNECTING) {
//            menu.findItem(R.id.menu_connect).setVisible(false);
//            menu.findItem(R.id.menu_connecting).setVisible(true);
//            menu.findItem(R.id.menu_disconnect).setVisible(false);
//            menu.findItem(R.id.menu_refresh).setActionView(
//                    R.layout.actionbar_indeterminate_progress);
//        } else {
//            menu.findItem(R.id.menu_connect).setVisible(true);
//            menu.findItem(R.id.menu_connecting).setVisible(false);
//            menu.findItem(R.id.menu_disconnect).setVisible(false);
//        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mClose:

                finish();
                return true;
            case R.id.menu_connect:
                // Connect Bluetooth reader
                Log.v(TAG, "Start to connect!!!");
                if (mDeviceName != null && mDeviceAddress.contains(":")) {
                    connectReader();
                }
                return true;
            case R.id.menu_connecting:
            case R.id.menu_disconnect:
                // Disconnect Bluetooth reader
                Log.v(TAG, "Start to disconnect!!!");
                disconnectReader();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
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
        }

        /* Create a new connection. */
        final BluetoothDevice device = bluetoothAdapter
                .getRemoteDevice(mDeviceAddress);

        if (device == null) {
            Log.w(TAG, "Device not found. Unable to connect.");
            return false;
        }

        /* Connect to GATT server. */
        updateConnectionState(BluetoothReader.STATE_CONNECTING);
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
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
        } else if (errorCode == BluetoothReader.ERROR_COMMAND_FAILED) {
            return "The command failed.";
        }
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

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
        }

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
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            try {

                consoleString += "OUTPUT- " + result + "\n";

                System.out.println(result);

            } catch (Exception e) {

                System.out.println(e);
            }
        }
    }

    public void transmitEscapeCommend(){

            /* Check for detected reader. */
        if (mBluetoothReader == null) {
            System.out.println("card_reader_not_ready");
            return;
        }

           /* Retrieve escape command from edit box. */
        byte escapeCommand[] =  CommonUtils.toByteArray(DEFAULT_1255_ESCAPE_COMMAND);

        if (escapeCommand != null && escapeCommand.length > 0) {
                    /* Clear response field for result of escape command. */
            System.out.println("No Sleep EscapeCommand");

                    /* Transmit escape command. */
            if (!mBluetoothReader.transmitEscapeCommand(escapeCommand)) {
                System.out.println("card_reader_not_ready");
            }
        } else {
            System.out.println("Character format error!");
        }

    }
}