package com.TrakEngineering.FluidSecureHub;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.TrakEngineering.FluidSecureHub.BTSPP.BTConstants;
import com.TrakEngineering.FluidSecureHub.enity.AuthEntityClass;
import com.TrakEngineering.FluidSecureHub.enity.VehicleRequireEntity;
import com.TrakEngineering.FluidSecureHub.server.ServerHandler;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

public class AcceptVehicleActivity extends AppCompatActivity {

    private static final String TAG = "AcceptVehicleActivity";
    public static String SITE_ID = "0";
    private EditText editVehicleNumber;
    String IsVehicleHasFob = "", IsOdoMeterRequire = "", IsDepartmentRequire = "", IsPersonnelPINRequire = "", IsPersonnelPINRequireForHub = "", IsOtherRequire = "";
    Button btnCancel, btnSave;
    GoogleApiClient mGoogleApiClient;
    public static double CurrentLat = 0, CurrentLng = 0;
    RelativeLayout footer_keybord;
    LinearLayout Linear_layout_Save_back_buttons;
    TextView tv_fob_Reader, tv_or, tv_return, tv_swipekeybord, tv_fob_number, tv_vehicle_no_below, tv_dont_have_fob, tv_enter_vehicle_no;
    LinearLayout Linear_layout_vehicleNumber;
    EditText editFobNumber;
    String TimeOutinMinute;
    boolean Istimeout_Sec = true;
    int FobReadingCount = 0;
    int FobRetryCount = 0;
    long screenTimeOut;
    Timer t, ScreenOutTimeVehicle;
    //String FOLDER_PATH = ""; //Environment.getExternalStorageDirectory().getAbsolutePath() + "/FSBin/";



    @Override
    protected void onResume() {
        super.onResume();

        AppConstants.APDU_FOB_KEY = "";


        if (Constants.CurrentSelectedHose.equals("FS1")) {
            editVehicleNumber.setText(Constants.AccVehicleNumber_FS1);
        } else if (Constants.CurrentSelectedHose.equals("FS2")) {
            editVehicleNumber.setText(Constants.AccVehicleNumber);
        } else if (Constants.CurrentSelectedHose.equals("FS3")) {
            editVehicleNumber.setText(Constants.AccVehicleNumber_FS3);
        } else if (Constants.CurrentSelectedHose.equals("FS4")) {
            editVehicleNumber.setText(Constants.AccVehicleNumber_FS4);
        }

        DisplayScreenInit();
        Istimeout_Sec = true;
        TimeoutVehicleScreen();

        btnSave.setClickable(true);


        t = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                //do something
                System.out.println("Vehi FOK_KEY" + AppConstants.APDU_FOB_KEY);
                if (!AppConstants.APDU_FOB_KEY.equalsIgnoreCase("") && AppConstants.APDU_FOB_KEY.length() > 6) {

                    try {


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                editVehicleNumber.setText("");
                                Istimeout_Sec = false;
                                ScreenOutTimeVehicle.cancel();
                                GetVehicleNuOnFobKeyDetection();

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        CallSaveButtonFunctionality();//Press Enter fun
                                    }
                                }, 2000);
                            }
                        });

                        t.cancel();
                    } catch (Exception e) {

                        System.out.println(e);
                    }

                }

            }

            ;
        };
        t.schedule(tt, 500, 500);


    }

    @Override
    protected void onStop() {
        super.onStop();

        AppConstants.APDU_FOB_KEY = "";
        t.cancel();//Stop timer FOB Key
        ScreenOutTimeVehicle.cancel();//Stop screen out timer
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ActivityHandler.addActivities(1, AcceptVehicleActivity.this);
        setContentView(R.layout.activity_accept_vehicle);

        getSupportActionBar().setTitle(R.string.fs_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        InItGUI();

        SharedPreferences sharedPrefODO = AcceptVehicleActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
        TimeOutinMinute = sharedPrefODO.getString(AppConstants.TimeOut, "1");
        AppConstants.HUB_ID = sharedPrefODO.getString(AppConstants.HubId, "");


        SharedPreferences sharedPref = AcceptVehicleActivity.this.getSharedPreferences(Constants.PREF_COLUMN_SITE, Context.MODE_PRIVATE);
        String dataSite = sharedPref.getString(Constants.PREF_COLUMN_SITE, "");

        SITE_ID = parseSiteData(dataSite);
        AppConstants.SITE_ID = SITE_ID;

        //enable hotspot.
        Constants.hotspotstayOn = true;

        //FOLDER_PATH = getApplicationContext().getExternalFilesDir(AppConstants.FOLDER_BIN) + "/";

        if (AppConstants.UP_Upgrade) {

            //File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "FSBin");
            String binFolderPath = String.valueOf(getApplicationContext().getExternalFilesDir(AppConstants.FOLDER_BIN));
            File folder = new File(binFolderPath);
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdirs();
            }

            if (BTConstants.CurrentTransactionIsBT) {
                AppConstants.UP_Upgrade_File_name = "BT_" + AppConstants.UP_Upgrade_File_name;
            }
            String LocalPath = binFolderPath + "/" + AppConstants.UP_Upgrade_File_name;

            File f = new File(LocalPath);
            if (f.exists()) {
                Log.e(TAG, "Link upgrade firmware file already exist. Skip download");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Link upgrade firmware file (" + AppConstants.UP_Upgrade_File_name + ") already exist. Skip download");
            } else {
                if (AppConstants.UP_FilePath != null) {
                    //new BackgroundServiceDownloadFirmware.DownloadLinkAndReaderFirmware().execute(AppConstants.UP_FilePath, AppConstants.UP_Upgrade_File_name, "UP_Upgrade");
                    new DownloadFileFromURL().execute(AppConstants.UP_FilePath, binFolderPath, AppConstants.UP_Upgrade_File_name);
                } else {
                    Log.e(TAG, "Link upgrade File path null");
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " Link upgrade File path null");
                }
            }
        }

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

        tv_swipekeybord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int InputTyp = editVehicleNumber.getInputType();
                if (InputTyp == 3) {
                    editVehicleNumber.setInputType(InputType.TYPE_CLASS_TEXT);
                    tv_swipekeybord.setText("Press for 123");
                } else {

                    editVehicleNumber.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_TEXT);
                    tv_swipekeybord.setText("Press for ABC");
                }

            }
        });

        tv_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeybord();
            }
        });


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

        String content = "Enter your <br><b>VEHICLE ID</b> in<br> the green box below";

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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
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

    public void saveButtonAction(View view) {
        CallSaveButtonFunctionality();
    }

    public void CallSaveButtonFunctionality() {


        try {


            String V_Number = editVehicleNumber.getText().toString().trim();


            if (!V_Number.isEmpty() || !AppConstants.APDU_FOB_KEY.isEmpty()) {


                String vehicleNumber = "";
                String pinNumber = "";

                if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
                    pinNumber = Constants.AccPersonnelPIN_FS1;
                    vehicleNumber = editVehicleNumber.getText().toString().trim();
                    Constants.AccVehicleNumber_FS1 = vehicleNumber;


                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
                    pinNumber = Constants.AccPersonnelPIN;
                    vehicleNumber = editVehicleNumber.getText().toString().trim();
                    Constants.AccVehicleNumber = vehicleNumber;

                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
                    pinNumber = Constants.AccPersonnelPIN_FS3;
                    vehicleNumber = editVehicleNumber.getText().toString().trim();
                    Constants.AccVehicleNumber_FS3 = vehicleNumber;

                } else {
                    pinNumber = Constants.AccPersonnelPIN_FS4;
                    vehicleNumber = editVehicleNumber.getText().toString().trim();
                    Constants.AccVehicleNumber_FS4 = vehicleNumber;

                }


                VehicleRequireEntity objEntityClass = new VehicleRequireEntity();
                objEntityClass.IMEIUDID = AppConstants.getIMEI(AcceptVehicleActivity.this);
                objEntityClass.VehicleNumber = vehicleNumber;
                objEntityClass.WifiSSId = AppConstants.LAST_CONNECTED_SSID;
                objEntityClass.SiteId = Integer.parseInt(AppConstants.SITE_ID);
                objEntityClass.PersonnelPIN = pinNumber;
                objEntityClass.RequestFromAPP = "AP";
                objEntityClass.FromNewFOBChange = "Y";
                objEntityClass.FOBNumber = AppConstants.APDU_FOB_KEY;
                AppConstants.FOB_KEY_VEHICLE = AppConstants.APDU_FOB_KEY;

                CheckVehicleRequireOdometerEntryAndRequireHourEntry vehTestAsynTask = new CheckVehicleRequireOdometerEntryAndRequireHourEntry(objEntityClass);
                vehTestAsynTask.execute();
                vehTestAsynTask.get();

                String serverRes = vehTestAsynTask.response;

                if (serverRes != null) {


                    JSONObject jsonObject = new JSONObject(serverRes);

                    String ResponceMessage = jsonObject.getString("ResponceMessage");

                    System.out.println("ResponceMessage.." + ResponceMessage);


                    if (ResponceMessage.equalsIgnoreCase("success")) {

                        btnSave.setClickable(false);

                        SharedPreferences sharedPrefODO = AcceptVehicleActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

                        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
                        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
                        IsPersonnelPINRequireForHub = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequireForHub, "");
                        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");

                        IsOdoMeterRequire = jsonObject.getString("IsOdoMeterRequire");
                        String IsHoursRequire = jsonObject.getString("IsHoursRequire");
                        String VehicleNumber = jsonObject.getString("VehicleNumber");
                        String PreviousOdo = jsonObject.getString("PreviousOdo");
                        String OdoLimit = jsonObject.getString("OdoLimit");
                        String OdometerReasonabilityConditions = jsonObject.getString("OdometerReasonabilityConditions");
                        String CheckOdometerReasonable = jsonObject.getString("CheckOdometerReasonable");
                        String LastTransactionFuelQuantity = jsonObject.getString("LastTransactionFuelQuantity").replace(",", ".");
                        if (LastTransactionFuelQuantity.trim().isEmpty() || LastTransactionFuelQuantity.equalsIgnoreCase("null")) {
                            LastTransactionFuelQuantity = "0";
                        }

                        SharedPreferences sharedPref = AcceptVehicleActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(AppConstants.IsOdoMeterRequire, IsOdoMeterRequire);
                        editor.putString(AppConstants.IsHoursRequire, IsHoursRequire);
                        editor.putString("PreviousOdo", PreviousOdo);
                        editor.putString("OdoLimit", OdoLimit);
                        editor.putString("OdometerReasonabilityConditions", OdometerReasonabilityConditions);
                        editor.putString("CheckOdometerReasonable", CheckOdometerReasonable);
                        editor.putString("LastTransactionFuelQuantity", LastTransactionFuelQuantity);
                        editor.commit();


                        if (IsOdoMeterRequire.equalsIgnoreCase("True")) {

                            Intent intent = new Intent(AcceptVehicleActivity.this, AcceptOdoActivity.class);//AcceptPinActivity
                            startActivity(intent);

                        } else if (IsHoursRequire.equalsIgnoreCase("True")) {

                            Intent intent = new Intent(AcceptVehicleActivity.this, AcceptHoursAcitvity.class);
                            startActivity(intent);

                        } else if (IsPersonnelPINRequireForHub.equalsIgnoreCase("True")) {

                            Intent intent = new Intent(AcceptVehicleActivity.this, AcceptPinActivity.class);//AcceptPinActivity
                            startActivity(intent);

                        } else if (IsDepartmentRequire.equalsIgnoreCase("True")) {


                            Intent intent = new Intent(AcceptVehicleActivity.this, AcceptDeptActivity.class);
                            startActivity(intent);

                        } else if (IsOtherRequire.equalsIgnoreCase("True")) {

                            Intent intent = new Intent(AcceptVehicleActivity.this, AcceptOtherActivity.class);
                            startActivity(intent);

                        } else {

                            AcceptServiceCall asc = new AcceptServiceCall();
                            asc.activity = AcceptVehicleActivity.this;
                            asc.checkAllFields();
                        }

                    } else {
                        String ResponceText = jsonObject.getString("ResponceText");

                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Vehicle rejected. Error: " + ResponceText);

                        String ValidationFailFor = jsonObject.getString("ValidationFailFor");
                        if (ValidationFailFor.equalsIgnoreCase("Pin")) {
                            AppConstants.colorToastBigFont(this, ResponceText, Color.RED);
                            Intent i = new Intent(this, AcceptPinActivity.class);
                            startActivity(i);

                        } else {
                            //Empty Fob key & enable edit text and Enter button
                            // AppConstants.FOB_KEY_VEHICLE = "";
                            editVehicleNumber.setEnabled(true);
                            btnSave.setEnabled(true);
                            tv_vehicle_no_below.setText("Enter Vehicle Number:");
                            CommonUtils.showCustomMessageDilaog(AcceptVehicleActivity.this, "Message", ResponceText);
                        }

                    }

                } else {
                    //Empty Fob key & enable edit text and Enter button
                    AppConstants.APDU_FOB_KEY = "";
                    editVehicleNumber.setEnabled(true);
                    btnSave.setEnabled(true);
                    CommonUtils.showNoInternetDialog(AcceptVehicleActivity.this);
                }


            } else {
                //Empty Fob key & enable edit text and Enter button
                AppConstants.APDU_FOB_KEY = "";
                if (IsVehicleHasFob.equalsIgnoreCase("true")) {
                    editVehicleNumber.setEnabled(false);
                } else {
                    editVehicleNumber.setEnabled(true);
                }

                btnSave.setEnabled(true);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Please enter vehicle number or use fob key.");
                CommonUtils.showMessageDilaog(AcceptVehicleActivity.this, "Error Message", "Please enter vehicle number or use fob key.");
            }


        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    public void GetVehicleNuOnFobKeyDetection() {

        try {

            String vehicleNumber = "";
            String pinNumber = "";

            if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
                pinNumber = Constants.AccPersonnelPIN_FS1;
                vehicleNumber = editVehicleNumber.getText().toString().trim();
                Constants.AccVehicleNumber_FS1 = vehicleNumber;


            } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
                pinNumber = Constants.AccPersonnelPIN;
                vehicleNumber = editVehicleNumber.getText().toString().trim();
                Constants.AccVehicleNumber = vehicleNumber;

            } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
                pinNumber = Constants.AccPersonnelPIN_FS3;
                vehicleNumber = editVehicleNumber.getText().toString().trim();
                Constants.AccVehicleNumber_FS3 = vehicleNumber;

            } else {
                pinNumber = Constants.AccPersonnelPIN_FS4;
                vehicleNumber = editVehicleNumber.getText().toString().trim();
                Constants.AccVehicleNumber_FS4 = vehicleNumber;

            }

            VehicleRequireEntity objEntityClass = new VehicleRequireEntity();
            objEntityClass.IMEIUDID = AppConstants.getIMEI(AcceptVehicleActivity.this);
            objEntityClass.VehicleNumber = vehicleNumber;
            objEntityClass.WifiSSId = AppConstants.LAST_CONNECTED_SSID;
            objEntityClass.SiteId = Integer.parseInt(AppConstants.SITE_ID);
            objEntityClass.PersonnelPIN = pinNumber;
            objEntityClass.RequestFromAPP = "AP";
            objEntityClass.FOBNumber = AppConstants.APDU_FOB_KEY;


            CheckVehicleRequireOdometerEntryAndRequireHourEntry vehTestAsynTask = new CheckVehicleRequireOdometerEntryAndRequireHourEntry(objEntityClass);
            vehTestAsynTask.execute();
            vehTestAsynTask.get();

            String serverRes = vehTestAsynTask.response;

            if (serverRes != null) {


                JSONObject jsonObject = new JSONObject(serverRes);

                String ResponceMessage = jsonObject.getString("ResponceMessage");
                System.out.println("ResponceMessage...." + ResponceMessage);


                if (ResponceMessage.equalsIgnoreCase("success")) {


                    IsOdoMeterRequire = jsonObject.getString("IsOdoMeterRequire");
                    String IsHoursRequire = jsonObject.getString("IsHoursRequire");
                    String VehicleNumber = jsonObject.getString("VehicleNumber");
                    String PreviousOdo = jsonObject.getString("PreviousOdo");
                    String OdoLimit = jsonObject.getString("OdoLimit");
                    String OdometerReasonabilityConditions = jsonObject.getString("OdometerReasonabilityConditions");
                    String CheckOdometerReasonable = jsonObject.getString("CheckOdometerReasonable");
                    String LastTransactionFuelQuantity = jsonObject.getString("LastTransactionFuelQuantity").replace(",", ".");
                    if (LastTransactionFuelQuantity.trim().isEmpty() || LastTransactionFuelQuantity.equalsIgnoreCase("null")) {
                        LastTransactionFuelQuantity = "0";
                    }

                    SharedPreferences sharedPref = AcceptVehicleActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(AppConstants.IsOdoMeterRequire, IsOdoMeterRequire);
                    editor.putString(AppConstants.IsHoursRequire, IsHoursRequire);
                    editor.putString("PreviousOdo", PreviousOdo);
                    editor.putString("OdoLimit", OdoLimit);
                    editor.putString("OdometerReasonabilityConditions", OdometerReasonabilityConditions);
                    editor.putString("CheckOdometerReasonable", CheckOdometerReasonable);
                    editor.putString("LastTransactionFuelQuantity", LastTransactionFuelQuantity);
                    editor.commit();

                    editVehicleNumber.setText(VehicleNumber);
                    tv_vehicle_no_below.setText("Vehicle Number: " + VehicleNumber);
                    tv_fob_number.setText("Fob No: " + AppConstants.APDU_FOB_KEY);


                    DisplayScreenFobReadSuccess();


                } else {
                    String ResponceText = jsonObject.getString("ResponceText");
                    String ValidationFailFor = jsonObject.getString("ValidationFailFor");
                    if (ValidationFailFor.equalsIgnoreCase("Pin")) {
                        AppConstants.colorToastBigFont(this, ResponceText, Color.RED);
                        Intent i = new Intent(this, AcceptPinActivity.class);
                        startActivity(i);

                    } else {

                        Istimeout_Sec = true;
                        TimeoutVehicleScreen();
                        tv_enter_vehicle_no.setText("Invalid FOB or Unassigned FOB");
                        tv_enter_vehicle_no.setVisibility(View.VISIBLE);
                        tv_fob_number.setVisibility(View.GONE);
                        tv_fob_Reader.setVisibility(View.GONE);
                        tv_or.setVisibility(View.GONE);
                        tv_vehicle_no_below.setVisibility(View.GONE);
                        tv_dont_have_fob.setVisibility(View.VISIBLE);
                        btnSave.setVisibility(View.VISIBLE);
                        String content = "Enter your <br><b>VEHICLE ID</b> in<br> the green box below";

                        int width = 350;
                        int height = 60;
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
                    }

                }

            } else {
                // CommonUtils.showNoInternetDialog(AcceptVehicleActivity.this);
            }


        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    public class AuthTestAsynTask extends AsyncTask<Void, Void, Void> {

        AuthEntityClass authEntityClass = null;

        public String response = null;

        public AuthTestAsynTask(AuthEntityClass authEntityClass) {
            this.authEntityClass = authEntityClass;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                ServerHandler serverHandler = new ServerHandler();

                Gson gson = new Gson();
                String jsonData = gson.toJson(authEntityClass);
                String userEmail = CommonUtils.getCustomerDetails(AcceptVehicleActivity.this).Email;


                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(authEntityClass.IMEIUDID + ":" + userEmail + ":" + "AuthorizationSequence");
                response = serverHandler.PostTextData(AcceptVehicleActivity.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {

                CommonUtils.LogMessage(TAG, "AuthTestAsynTask ", ex);
            }
            return null;
        }

    }

    public class CheckVehicleRequireOdometerEntryAndRequireHourEntry extends AsyncTask<Void, Void, Void> {

        VehicleRequireEntity vrentity = null;

        public String response = null;

        public CheckVehicleRequireOdometerEntryAndRequireHourEntry(VehicleRequireEntity vrentity) {
            this.vrentity = vrentity;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                ServerHandler serverHandler = new ServerHandler();

                Gson gson = new Gson();
                String jsonData = gson.toJson(vrentity);
                String userEmail = CommonUtils.getCustomerDetails(AcceptVehicleActivity.this).PersonEmail;

                System.out.println("jsonDatajsonDatajsonData" + jsonData);
                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(vrentity.IMEIUDID + ":" + userEmail + ":" + "CheckVehicleRequireOdometerEntryAndRequireHourEntry");
                response = serverHandler.PostTextData(AcceptVehicleActivity.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {

                CommonUtils.LogMessage(TAG, "CheckVehicleRequireOdometerEntryAndRequireHourEntry ", ex);
            }
            return null;
        }

    }


    @Override
    public void onBackPressed() {

        // ActivityHandler.removeActivity(1);
        AppConstants.ClearEdittextFielsOnBack(AcceptVehicleActivity.this);
        Istimeout_Sec = false;
        AppConstants.APDU_FOB_KEY = "";
        finish();
    }


    public void Readfobkey() {

        //for (int i = 0; i < 2; i++) {AppConstants.colorToastBigFont(AcceptVehicleActivity.this, "  Please hold fob up to Reader  ", Color.BLUE);}
        tv_enter_vehicle_no.setText("Please Hold FOB to Reader");
        tv_enter_vehicle_no.setTextColor(Color.parseColor("#ff0000"));


    }

    @SuppressLint("ResourceAsColor")
    public void FobBtnEnable() {

        tv_fob_Reader.setBackgroundColor(Color.parseColor("#3F51B5"));
        tv_fob_Reader.setEnabled(true);
        tv_fob_Reader.setTextColor(Color.parseColor("#FFFFFF"));

    }

    @SuppressLint("ResourceAsColor")
    public void FobBtnDisable() {

        tv_fob_Reader.setBackgroundColor(Color.parseColor("#f5f1f0"));
        tv_fob_Reader.setEnabled(false);
        tv_fob_Reader.setTextColor(R.color.black);

    }

    public void TimeoutVehicleScreen() {

        SharedPreferences sharedPrefODO = AcceptVehicleActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
        TimeOutinMinute = sharedPrefODO.getString(AppConstants.TimeOut, "1");

        screenTimeOut = Integer.parseInt(TimeOutinMinute) * 60000;
        System.out.println("ScreenOutTimeVehicle" + screenTimeOut);

        ScreenOutTimeVehicle = new Timer();
        TimerTask tttt = new TimerTask() {
            @Override
            public void run() {
                //do something
                if (Istimeout_Sec) {

                    try {


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideKeybord();
                                Istimeout_Sec = false;
                                AppConstants.ClearEdittextFielsOnBack(AcceptVehicleActivity.this);

                                // ActivityHandler.GetBacktoWelcomeActivity();

                                Intent i = new Intent(AcceptVehicleActivity.this, WelcomeActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                            }
                        });

                        ScreenOutTimeVehicle.cancel();
                    } catch (Exception e) {

                        System.out.println(e);
                    }

                }

            }

            ;
        };
        ScreenOutTimeVehicle.schedule(tttt, screenTimeOut, 500);


    }

    public void DisplayScreenInit() {


        SharedPreferences sharedPrefODO = AcceptVehicleActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        IsVehicleHasFob = sharedPrefODO.getString(AppConstants.ISVehicleHasFob, "false");

        if (IsVehicleHasFob.equalsIgnoreCase("true")) {

            tv_fob_Reader.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.GONE);

            tv_or.setVisibility(View.GONE);
            tv_dont_have_fob.setVisibility(View.GONE);
            tv_enter_vehicle_no.setVisibility(View.GONE);
            tv_vehicle_no_below.setVisibility(View.GONE);
            tv_fob_number.setVisibility(View.GONE);

            // btnSave.setEnabled(false);
            // editVehicleNumber.setEnabled(false);
            //editVehicleNumber.setVisibility(View.INVISIBLE);
            Linear_layout_Save_back_buttons.setVisibility(View.VISIBLE);

            int width = 0;
            int height = ActionBar.LayoutParams.WRAP_CONTENT;
            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width, height);
            editVehicleNumber.setLayoutParams(parms);


        } else {

            //showKeybord();
            AppConstants.APDU_FOB_KEY = "";
            //editVehicleNumber.setText("");
            tv_enter_vehicle_no.setVisibility(View.GONE);
            tv_vehicle_no_below.setVisibility(View.GONE);
            tv_fob_number.setVisibility(View.GONE);
            editVehicleNumber.setVisibility(View.VISIBLE);
            tv_fob_Reader.setVisibility(View.VISIBLE);
            tv_or.setVisibility(View.VISIBLE);
            tv_dont_have_fob.setVisibility(View.VISIBLE);
            Linear_layout_Save_back_buttons.setVisibility(View.VISIBLE);


        }

    }

    public void DisplayScreenFobReadSuccess() {

        tv_enter_vehicle_no.setVisibility(View.VISIBLE);
        tv_fob_number.setVisibility(View.GONE);
        tv_vehicle_no_below.setVisibility(View.VISIBLE);
        tv_dont_have_fob.setVisibility(View.GONE);
        editVehicleNumber.setVisibility(View.GONE);
        Linear_layout_Save_back_buttons.setVisibility(View.GONE);
        tv_fob_Reader.setVisibility(View.GONE);
        tv_or.setVisibility(View.GONE);

    }


    public void hideKeybord() {

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

    }

    public void showKeybord() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public class DownloadFileFromURL extends AsyncTask<String, String, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(AcceptVehicleActivity.this);
            String message = "Upgrade file download in progress.\nPlease wait several seconds....";
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
                URLConnection conection = url.openConnection();
                conection.connect();
                // getting file length
                int lenghtOfFile = conection.getContentLength();

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



}