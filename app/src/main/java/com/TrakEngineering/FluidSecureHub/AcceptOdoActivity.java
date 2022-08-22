package com.TrakEngineering.FluidSecureHub;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.TrakEngineering.FluidSecureHub.enity.AuthEntityClass;
import com.TrakEngineering.FluidSecureHub.offline.EntityHub;
import com.TrakEngineering.FluidSecureHub.offline.OffDBController;
import com.TrakEngineering.FluidSecureHub.offline.OfflineConstants;
import com.TrakEngineering.FluidSecureHub.server.ServerHandler;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AcceptOdoActivity extends AppCompatActivity {

    private static final String TAG = "AcceptOdoActivity ";
    private EditText editOdoTenths;
    private TextView tv_swipekeybord, tv_odo;
    private String vehicleNumber;
    private String odometerTenths;
    private ProgressBar progressBar;
    private ConnectionDetector cd = new ConnectionDetector(AcceptOdoActivity.this);

    String IsOdoMeterRequire = "", IsDepartmentRequire = "", IsPersonnelPINRequire = "", IsOtherRequire = "";
    String PreviousOdo = "", OdoLimit = "", OdometerReasonabilityConditions = "", CheckOdometerReasonable = "", LastTransactionFuelQuantity = "0";
    String IsNonValidateVehicle = "";
    String TimeOutinMinute, ScreenNameForOdometer = "odometer";
    boolean Istimeout_Sec = true;
    List<Timer> ScreenTimerlist = new ArrayList<Timer>();

    public int cnt123 = 0;
    public int off_cnt123 = 0;

    OffDBController controller = new OffDBController(AcceptOdoActivity.this);
    private NetworkReceiver receiver = new NetworkReceiver();

    Timer t, ScreenOutTime;

    @Override
    protected void onResume() {
        super.onResume();

        invalidateOptionsMenu();
        AppConstants.OdoErrorCode = "0";

        editOdoTenths.setText("");


        Istimeout_Sec = true;
        TimeoutOdoScreen();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reader, menu);

        menu.findItem(R.id.mconfigure_tld).setVisible(false);
        menu.findItem(R.id.enable_debug_window).setVisible(false);
        menu.findItem(R.id.mclose).setVisible(false);
        menu.findItem(R.id.mupgrade_normal_link).setVisible(false);
        menu.findItem(R.id.mreconnect_ble_readers).setVisible(false);
        menu.findItem(R.id.mshow_reader_status).setVisible(false);
        menu.findItem(R.id.mreboot_reader).setVisible(false);
        menu.findItem(R.id.mcamera_back).setVisible(false);
        menu.findItem(R.id.mcamera_front).setVisible(false);

        if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {

            menu.findItem(R.id.monline).setVisible(true);
            menu.findItem(R.id.mofline).setVisible(false);

        } else {
            menu.findItem(R.id.monline).setVisible(false);
            menu.findItem(R.id.mofline).setVisible(true);
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  ActivityHandler.addActivities(2, AcceptOdoActivity.this);

        setContentView(R.layout.activity_accept_odo);

        getSupportActionBar().setTitle(AppConstants.BrandName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        InItGUI();

        SharedPreferences myPrefkb = this.getSharedPreferences(AppConstants.sharedPref_KeyboardType, 0);
        ScreenNameForOdometer = myPrefkb.getString("ScreenNameForOdometer", "odometer");

        if (ScreenNameForOdometer.trim().isEmpty())
            ScreenNameForOdometer = "odometer";

        tv_odo.setText("Enter " + ScreenNameForOdometer + " No Tenths");
        editOdoTenths.setHint("Enter " + ScreenNameForOdometer + " No Tenths");

        /*SharedPreferences sharedPrefODO = AcceptOdoActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        IsOdoMeterRequire = sharedPrefODO.getString(AppConstants.IsOdoMeterRequire, "");
        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");

        PreviousOdo = sharedPrefODO.getString("PreviousOdo", "");
        OdoLimit = sharedPrefODO.getString("OdoLimit", "");
        OdometerReasonabilityConditions = sharedPrefODO.getString("OdometerReasonabilityConditions", "");
        CheckOdometerReasonable = sharedPrefODO.getString("CheckOdometerReasonable", "");

        TimeOutinMinute = sharedPrefODO.getString(AppConstants.TimeOut, "1");

        long screenTimeOut = Integer.parseInt(TimeOutinMinute) * 60000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Istimeout_Sec) {
                    Istimeout_Sec = false;
                    AppConstants.ClearEdittextFielsOnBack(AcceptOdoActivity.this);
                    Intent intent = new Intent(AcceptOdoActivity.this, WelcomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

            }
        }, screenTimeOut);*/

        vehicleNumber = getIntent().getStringExtra(Constants.VEHICLE_NUMBER);


        if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
            if (Constants.AccOdoMeter_FS1 > 0) {
                editOdoTenths.setText(Constants.AccOdoMeter_FS1 + "");
            }

        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
            if (Constants.AccOdoMeter > 0) {
                editOdoTenths.setText(Constants.AccOdoMeter + "");
            }

        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
            if (Constants.AccOdoMeter_FS3 > 0) {
                editOdoTenths.setText(Constants.AccOdoMeter_FS3 + "");
            }

        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS4")) {
            if (Constants.AccOdoMeter_FS4 > 0) {
                editOdoTenths.setText(Constants.AccOdoMeter_FS4 + "");
            }
        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS5")) {
            if (Constants.AccOdoMeter_FS5 > 0) {
                editOdoTenths.setText(Constants.AccOdoMeter_FS5 + "");
            }
        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS6")) {
            if (Constants.AccOdoMeter_FS6 > 0) {
                editOdoTenths.setText(Constants.AccOdoMeter_FS6 + "");
            }
        }

        String KeyboardType = "2";
        try {
            editOdoTenths.setInputType(Integer.parseInt(KeyboardType));
        } catch (Exception e) {
            System.out.println("keyboard exception");
            editOdoTenths.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        tv_swipekeybord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int InputTyp = editOdoTenths.getInputType();
                if (InputTyp == 2) {
                    editOdoTenths.setInputType(InputType.TYPE_CLASS_TEXT);
                    tv_swipekeybord.setText("Press for 123");
                } else {

                    editOdoTenths.setInputType(InputType.TYPE_CLASS_NUMBER);//| InputType.TYPE_CLASS_TEXT
                    tv_swipekeybord.setText("Press for ABC");
                }

            }
        });


        // Registers BroadcastReceiver to track network connection changes.
        IntentFilter ifilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver();
        this.registerReceiver(receiver, ifilter);

    }

    public void TimeoutOdoScreen() {

        Log.i("TimeoutoDOScreen", "TimeOut_Start");
        Log.i("TimeoutoDOScreen", String.valueOf(Istimeout_Sec));
        SharedPreferences sharedPrefODO = AcceptOdoActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        IsOdoMeterRequire = sharedPrefODO.getString(AppConstants.IsOdoMeterRequire, "");
        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");

        PreviousOdo = sharedPrefODO.getString("PreviousOdo", "");
        OdoLimit = sharedPrefODO.getString("OdoLimit", "0");
        OdometerReasonabilityConditions = sharedPrefODO.getString("OdometerReasonabilityConditions", "");
        CheckOdometerReasonable = sharedPrefODO.getString("CheckOdometerReasonable", "");
        LastTransactionFuelQuantity = sharedPrefODO.getString("LastTransactionFuelQuantity", "0");

        TimeOutinMinute = sharedPrefODO.getString(AppConstants.TimeOut, "1");
        //long screenTimeOut= (long) (Double.parseDouble(TimeOutinMinute) *60000);
        long screenTimeOut = Integer.parseInt(TimeOutinMinute) * 60000;

        ScreenOutTime = new Timer();
        ScreenTimerlist.add(ScreenOutTime);
        TimerTask ttt = new TimerTask() {
            @Override
            public void run() {
                //do something
                if (Istimeout_Sec) {

                    try {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Istimeout_Sec = false;
                                AppConstants.ClearEdittextFielsOnBack(AcceptOdoActivity.this);


                                Intent i = new Intent(AcceptOdoActivity.this, WelcomeActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                            }
                        });

                        CancelTimerScreenOut();
                    } catch (Exception e) {

                        System.out.println(e);
                    }

                }

            }

            ;
        };
        ScreenOutTime.schedule(ttt, screenTimeOut, 500);


    }

    public void ResetTimeoutOdoScreen() {

        CancelTimerScreenOut();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        TimeoutOdoScreen();
    }

    public String ZR(String zeroString) {
        if (zeroString.trim().equalsIgnoreCase("0"))
            return "";
        else
            return zeroString;

    }


    private void InItGUI() {
        try {
            editOdoTenths = (EditText) findViewById(R.id.editOdoTenths);
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            tv_swipekeybord = (TextView) findViewById(R.id.tv_swipekeybord);
            tv_odo = (TextView) findViewById(R.id.tv_odo);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    public void cancelAction(View v) {

        onBackPressed();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveButtonAction(View view) {
        try {
            Istimeout_Sec = false;
            CommonUtils.LogMessage(TAG, TAG + "Entered Odometer : " + editOdoTenths.getText(), null);
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Entered Odometer : " + editOdoTenths.getText());

            if (!editOdoTenths.getText().toString().trim().isEmpty()) {

                int C_AccOdoMeter = 0;
                if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
                    Constants.AccOdoMeter_FS1 = Integer.parseInt(editOdoTenths.getText().toString().trim());
                    C_AccOdoMeter = Constants.AccOdoMeter_FS1;
                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
                    Constants.AccOdoMeter = Integer.parseInt(editOdoTenths.getText().toString().trim());
                    C_AccOdoMeter = Constants.AccOdoMeter;
                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
                    Constants.AccOdoMeter_FS3 = Integer.parseInt(editOdoTenths.getText().toString().trim());
                    C_AccOdoMeter = Constants.AccOdoMeter_FS3;
                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS4")) {
                    Constants.AccOdoMeter_FS4 = Integer.parseInt(editOdoTenths.getText().toString().trim());
                    C_AccOdoMeter = Constants.AccOdoMeter_FS4;
                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS5")) {
                    Constants.AccOdoMeter_FS5 = Integer.parseInt(editOdoTenths.getText().toString().trim());
                    C_AccOdoMeter = Constants.AccOdoMeter_FS5;
                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS6")) {
                    Constants.AccOdoMeter_FS6 = Integer.parseInt(editOdoTenths.getText().toString().trim());
                    C_AccOdoMeter = Constants.AccOdoMeter_FS6;
                }

                OfflineConstants.storeCurrentTransaction(AcceptOdoActivity.this, "", "", "", editOdoTenths.getText().toString().trim(), "", "", "", "", "", "");

                if (OfflineConstants.isTotalOfflineEnabled(AcceptOdoActivity.this)) {
                    //skip all validation in permanent offline mode
                    offlineValidOdo();

                } else {

                    if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {
                        int PO = Integer.parseInt(PreviousOdo.trim());
                        int OL = Integer.parseInt(OdoLimit.trim());

                        double LastTxtnQuantity = 0;

                        if (LastTransactionFuelQuantity.trim().isEmpty() || LastTransactionFuelQuantity.equalsIgnoreCase("null")) {
                            LastTransactionFuelQuantity = "0";
                        }

                        try {
                            LastTxtnQuantity = Double.parseDouble(LastTransactionFuelQuantity.trim());
                        } catch (Exception e) {
                            LastTxtnQuantity = 0;
                            Log.e(TAG, e.getMessage());
                        }

                        if (C_AccOdoMeter == 0) { // Must be greater than 0.
                            Istimeout_Sec = true;
                            ResetTimeoutOdoScreen();
                            CommonUtils.showMessageDilaog(AcceptOdoActivity.this, "Error", getResources().getString(R.string.peOdo0).replace("odometer", ScreenNameForOdometer.toLowerCase()));

                        } else if (CheckOdometerReasonable.trim().toLowerCase().equalsIgnoreCase("true")) {

                            if (LastTxtnQuantity > 10 && C_AccOdoMeter == PO && (cnt123 < 3)) {
                                // Must entered different reading if the last transaction fuel quantity is greater than 10.
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " Entered a reading that was previously entered");
                                editOdoTenths.setText("");
                                Istimeout_Sec = true;
                                ResetTimeoutOdoScreen();
                                CommonUtils.showMessageDilaog(AcceptOdoActivity.this, "Error Message", getResources().getString(R.string.prevReading));

                                if (OdometerReasonabilityConditions.trim().equalsIgnoreCase("1")) {
                                    cnt123 += 1;
                                }
                            } else if (OdometerReasonabilityConditions.trim().equalsIgnoreCase("1")) { //Allow after 3 Incorrect Entries

                                if (C_AccOdoMeter >= PO && C_AccOdoMeter <= OL) {
                                    //gooooo
                                    allValid();
                                } else {
                                    cnt123 += 1;

                                    if (cnt123 > 2) {
                                        AppConstants.OdoErrorCode = "1";
                                    } else {
                                        AppConstants.OdoErrorCode = "0";
                                    }

                                    if (cnt123 > 3) {
                                        //gooooo
                                        allValid();
                                    } else {

                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + " Entered Odometer (" + C_AccOdoMeter + ") is not within the reasonability");
                                        editOdoTenths.setText("");
                                        Istimeout_Sec = true;
                                        ResetTimeoutOdoScreen();
                                        CommonUtils.showMessageDilaog(AcceptOdoActivity.this, "Message", "The " + ScreenNameForOdometer + " entered is not within the reasonability your manager has assigned, please try again or contact your manager.");
                                    }
                                }
                            } else {

                                if (C_AccOdoMeter >= PO && C_AccOdoMeter <= OL) {
                                    ///gooooo
                                    allValid();
                                } else {
                                    editOdoTenths.setText("");
                                    if (AppConstants.GenerateLogs) {
                                        AppConstants.WriteinFile(TAG + " Entered Odometer (" + C_AccOdoMeter + ") is not within the reasonability");
                                    }
                                    Istimeout_Sec = true;
                                    ResetTimeoutOdoScreen();
                                    CommonUtils.showMessageDilaog(AcceptOdoActivity.this, "Message", "The " + ScreenNameForOdometer + " entered is not within the reasonability your manager has assigned, please try again or contact your manager.");
                                }
                            }
                        } else {

                            /*if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " Odo Entered" + C_AccOdoMeter);*/
                            //comment By JB -it  must take ANY number they enter on the 4th try
                            allValid();

                        }
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Internet Connection: " + cd.isConnectingToInternet() + "; NETWORK_STRENGTH: " + AppConstants.NETWORK_STRENGTH);
                        //offline-------------------
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Offline Entered Odometer: " + editOdoTenths.getText());

                        if (OfflineConstants.isOfflineAccess(AcceptOdoActivity.this)) {

                            IsNonValidateVehicle = controller.getOfflineHubDetails(AcceptOdoActivity.this).IsNonValidateVehicle;
                            if (IsNonValidateVehicle.equalsIgnoreCase("True")) {
                                //skip all validation in non-validated vehicle mode
                                offlineValidOdo();
                            } else {

                                int previous_odometer = 0, odo_limit = 0;
                                int entered_odometer = Integer.parseInt(editOdoTenths.getText().toString().trim());

                                try {

                                    if (AppConstants.OFF_CURRENT_ODO != null && !AppConstants.OFF_CURRENT_ODO.isEmpty()) {

                                        previous_odometer = Integer.parseInt(AppConstants.OFF_CURRENT_ODO);

                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + "Offline Previous Odometer : " + previous_odometer);
                                    }

                                    if (AppConstants.OFF_ODO_Limit != null && !AppConstants.OFF_ODO_Limit.isEmpty()) {

                                        odo_limit = Integer.parseInt(AppConstants.OFF_ODO_Limit);
                                        odo_limit = previous_odometer + (odo_limit) * 5;

                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + "Offline Odometer limit * 5 : " + odo_limit);

                                    }
                                } catch (Exception e) {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "Exception in odo saveButtonAction Offline mode. " + e.getMessage());
                                }

                                if (entered_odometer == 0) { // Must be greater than 0.
                                    Istimeout_Sec = true;
                                    ResetTimeoutOdoScreen();
                                    CommonUtils.showMessageDilaog(AcceptOdoActivity.this, "Message", getResources().getString(R.string.peOdo0).replace("odometer", ScreenNameForOdometer.toLowerCase()));

                                } else if (AppConstants.OFF_ODO_Reasonable != null && AppConstants.OFF_ODO_Reasonable.trim().toLowerCase().equalsIgnoreCase("true")) {

                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "Offline Odometer Reasonability : " + AppConstants.OFF_ODO_Reasonable);

                                    if (AppConstants.OFF_ODO_Conditions != null && AppConstants.OFF_ODO_Conditions.trim().equalsIgnoreCase("1")) {

                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + "Offline Odometer conditions : " + AppConstants.OFF_ODO_Conditions);

                                        if (odo_limit == 0) {

                                            offlineValidOdo();

                                        } else if (entered_odometer >= previous_odometer && entered_odometer <= odo_limit) {

                                            offlineValidOdo();

                                        } else {
                                            //3 attempt
                                            off_cnt123 += 1;

                                            if (off_cnt123 > 3) {

                                                offlineValidOdo();

                                            } else {
                                                Istimeout_Sec = true;
                                                ResetTimeoutOdoScreen();
                                                CommonUtils.showMessageDilaog(AcceptOdoActivity.this, "Message", "Please enter Correct " + ScreenNameForOdometer);
                                                //AppConstants.colorToastBigFont(getApplicationContext(),"Please enter Correct Odometer",Color.RED);
                                            }
                                        }
                                    } else {
                                        if (odo_limit == 0) {

                                            offlineValidOdo();

                                        } else if (entered_odometer >= previous_odometer && entered_odometer <= odo_limit) {

                                            offlineValidOdo();

                                        } else {
                                            Istimeout_Sec = true;
                                            ResetTimeoutOdoScreen();
                                            CommonUtils.showMessageDilaog(AcceptOdoActivity.this, "Message", "Please enter Correct " + ScreenNameForOdometer);
                                            //AppConstants.colorToastBigFont(getApplicationContext(),"Please enter Correct Odometer",Color.RED);
                                        }

                                    }
                                } else {
                                    offlineValidOdo();
                                }
                            }

                        } else {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Offline Access not granted to this HUB.");
                            //CommonUtils.AutoCloseCustomMessageDilaog(AcceptOdoActivity.this, "Message", "Unable to connect server");
                            Istimeout_Sec = true;
                            ResetTimeoutOdoScreen();
                        }
                    }
                }

            } else {
                Istimeout_Sec = true;
                ResetTimeoutOdoScreen();
                CommonUtils.showMessageDilaog(AcceptOdoActivity.this, "Error Message", "Please enter " + ScreenNameForOdometer + ", and try again.");
            }


        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + " Exception occurred in saveButtonAction. " + ex.getMessage());
            Log.e(TAG, ex.getMessage());
        }
    }

    public void offlineValidOdo() {

        if (IsNonValidateVehicle.equalsIgnoreCase("False")) {
            try {
                controller.updateOdometerByVehicleId(AppConstants.OFF_VEHICLE_ID, editOdoTenths.getText().toString().trim());
            } catch (Exception e) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Exception occurred while updating odometer for vehicle in offline mode. " + e.getMessage());
            }
        }

        if (AppConstants.OFF_HOUR_REQUIRED.trim().equalsIgnoreCase("y")) {
            Intent intent = new Intent(AcceptOdoActivity.this, AcceptHoursAcitvity.class);
            startActivity(intent);
        } else {
            EntityHub obj = controller.getOfflineHubDetails(AcceptOdoActivity.this);
            if (obj.PersonnelPINNumberRequired.equalsIgnoreCase("Y")) {
                Intent intent = new Intent(AcceptOdoActivity.this, AcceptPinActivity_new.class);//AcceptPinActivity
                startActivity(intent);
            } else if (obj.IsOtherRequire.equalsIgnoreCase("True") && !obj.HUBType.equalsIgnoreCase("G")) {
                Intent intent = new Intent(AcceptOdoActivity.this, AcceptOtherActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(AcceptOdoActivity.this, DisplayMeterActivity.class);
                startActivity(intent);
            }
        }
    }

    public void allValid() {


        SharedPreferences sharedPrefODO = AcceptOdoActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String IsPersonnelPINRequireForHub = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequireForHub, "");
        String IsHoursRequire = sharedPrefODO.getString(AppConstants.IsHoursRequire, "");
        String IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        String IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
        String IsExtraOther = sharedPrefODO.getString(AppConstants.IsExtraOther, "");

        if (IsHoursRequire.equalsIgnoreCase("True")) {

            Intent i = new Intent(AcceptOdoActivity.this, AcceptHoursAcitvity.class);
            startActivity(i);

        } else if (IsExtraOther.equalsIgnoreCase("True")) {

            Intent intent = new Intent(AcceptOdoActivity.this, AcceptVehicleOtherInfo.class);
            startActivity(intent);

        } else if (IsPersonnelPINRequireForHub.equalsIgnoreCase("True")) {

            Intent i = new Intent(AcceptOdoActivity.this, AcceptPinActivity_new.class);//AcceptPinActivity
            startActivity(i);

        } else if (IsDepartmentRequire.equalsIgnoreCase("True")) {


            Intent i = new Intent(AcceptOdoActivity.this, AcceptDeptActivity.class);
            startActivity(i);

        } else if (IsOtherRequire.equalsIgnoreCase("True")) {

            Intent i = new Intent(AcceptOdoActivity.this, AcceptOtherActivity.class);
            startActivity(i);

        } else {

            AcceptServiceCall asc = new AcceptServiceCall();
            asc.activity = AcceptOdoActivity.this;
            asc.checkAllFields();
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
                String userEmail = CommonUtils.getCustomerDetails(AcceptOdoActivity.this).Email;


                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(authEntityClass.IMEIUDID + ":" + userEmail + ":" + "AuthorizationSequence");
                response = serverHandler.PostTextData(AcceptOdoActivity.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {

                CommonUtils.LogMessage(TAG, "AuthTestAsynTask ", ex);
            }
            return null;
        }

    }

    @Override
    public void onBackPressed() {
        //ActivityHandler.removeActivity(2);
        Istimeout_Sec = false;
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CancelTimerScreenOut();
    }

    @Override
    protected void onStop() {
        super.onStop();
        CancelTimerScreenOut();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        CancelTimerScreenOut();
        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }

    private void CancelTimerScreenOut() {

        for (int i = 0; i < ScreenTimerlist.size(); i++) {
            ScreenTimerlist.get(i).cancel();
        }

    }
}