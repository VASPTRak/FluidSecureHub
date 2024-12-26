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

import com.TrakEngineering.FluidSecureHub.entity.AuthEntityClass;
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

    private static final String TAG = "Odo_Activity ";
    private EditText editOdoTenths;
    private TextView tv_swipekeybord, tv_odo;
    private String vehicleNumber;
    private String odometerTenths;
    private ProgressBar progressBar;
    private ConnectionDetector cd = new ConnectionDetector(AcceptOdoActivity.this);

    String IsOdoMeterRequire = "", IsDepartmentRequire = "", IsPersonnelPINRequire = "", IsOtherRequire = "";
    String PreviousOdo = "", OdoLimit = "", OdometerReasonabilityConditions = "", CheckOdometerReasonable = "", LastTransactionFuelQuantity = "0";
    String IsNonValidateVehicle = "";
    String TimeOutinMinute, ScreenNameForOdometer = "odometer",ScreenNameForPersonnel = "PERSONNEL", ScreenNameForVehicle = "VEHICLE";;
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
        AppConstants.ODO_ERROR_CODE = "0";

        editOdoTenths.setText("");


        Istimeout_Sec = true;
        TimeoutOdoScreen();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reader, menu);

        //menu.findItem(R.id.mconfigure_tld).setVisible(false);
        menu.findItem(R.id.enable_debug_window).setVisible(false);
        menu.findItem(R.id.mclose).setVisible(false);
        menu.findItem(R.id.madd_link).setVisible(false);
        menu.findItem(R.id.mreconnect_ble_readers).setVisible(false);
        menu.findItem(R.id.mreboot_reader).setVisible(false);
        menu.findItem(R.id.mcamera_back).setVisible(false);
        menu.findItem(R.id.mcamera_front).setVisible(false);
        menu.findItem(R.id.mshow_reader_status).setVisible(false);
        menu.findItem(R.id.mrestartapp).setVisible(false);

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  ActivityHandler.addActivities(2, AcceptOdoActivity.this);

        setContentView(R.layout.activity_accept_odo);

        getSupportActionBar().setTitle(AppConstants.BRAND_NAME);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        InItGUI();

        SharedPreferences myPrefkb = this.getSharedPreferences(AppConstants.PREF_KEYBOARD_TYPE, 0);
        ScreenNameForOdometer = myPrefkb.getString("ScreenNameForOdometer", "odometer");
        ScreenNameForVehicle = myPrefkb.getString("ScreenNameForVehicle", "Vehicle");
        ScreenNameForPersonnel = myPrefkb.getString("ScreenNameForPersonnel", "Personnel");

        if (ScreenNameForOdometer.trim().isEmpty())
            ScreenNameForOdometer = "odometer";

        //tv_odo.setText("Enter " + ScreenNameForOdometer + " No Tenths");
        String text = getResources().getString(R.string.EnterOdometerHeading).replace("Odometer", ScreenNameForOdometer);
        tv_odo.setText(text);
        editOdoTenths.setHint(text);

        /*SharedPreferences sharedPrefODO = AcceptOdoActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        IsOdoMeterRequire = sharedPrefODO.getString(AppConstants.IS_ODO_METER_REQUIRE, "");
        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IS_DEPARTMENT_REQUIRE, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IS_PERSONNEL_PIN_REQUIRE, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IS_OTHER_REQUIRE, "");

        PreviousOdo = sharedPrefODO.getString("PreviousOdo", "");
        OdoLimit = sharedPrefODO.getString("OdoLimit", "");
        OdometerReasonabilityConditions = sharedPrefODO.getString("OdometerReasonabilityConditions", "");
        CheckOdometerReasonable = sharedPrefODO.getString("CheckOdometerReasonable", "");

        TimeOutinMinute = sharedPrefODO.getString(AppConstants.TIMEOUT, "1");

        long screenTimeOut = Integer.parseInt(TimeOutinMinute) * 60000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Istimeout_Sec) {
                    Istimeout_Sec = false;
                    AppConstants.clearEditTextFieldsOnBack(AcceptOdoActivity.this);
                    Intent intent = new Intent(AcceptOdoActivity.this, WelcomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

            }
        }, screenTimeOut);*/

        vehicleNumber = getIntent().getStringExtra(Constants.VEHICLE_NUMBER);


        if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS1")) {
            if (Constants.ODO_METER_FS1 > 0) {
                editOdoTenths.setText(Constants.ODO_METER_FS1 + "");
            }

        } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS2")) {
            if (Constants.ODO_METER_FS2 > 0) {
                editOdoTenths.setText(Constants.ODO_METER_FS2 + "");
            }

        } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS3")) {
            if (Constants.ODO_METER_FS3 > 0) {
                editOdoTenths.setText(Constants.ODO_METER_FS3 + "");
            }

        } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS4")) {
            if (Constants.ODO_METER_FS4 > 0) {
                editOdoTenths.setText(Constants.ODO_METER_FS4 + "");
            }
        } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS5")) {
            if (Constants.ODO_METER_FS5 > 0) {
                editOdoTenths.setText(Constants.ODO_METER_FS5 + "");
            }
        } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS6")) {
            if (Constants.ODO_METER_FS6 > 0) {
                editOdoTenths.setText(Constants.ODO_METER_FS6 + "");
            }
        }

        String KeyboardType = "2";
        try {
            editOdoTenths.setInputType(Integer.parseInt(KeyboardType));
        } catch (Exception e) {
            System.out.println("keyboard exception");
            editOdoTenths.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        try {
            editOdoTenths.requestFocus();
        } catch (Exception e) {
            System.out.println("keyboard open exception");
        }

        tv_swipekeybord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int InputTyp = editOdoTenths.getInputType();
                if (InputTyp == 2) {
                    editOdoTenths.setInputType(InputType.TYPE_CLASS_TEXT);
                    tv_swipekeybord.setText(getResources().getString(R.string.PressFor123));
                } else {

                    editOdoTenths.setInputType(InputType.TYPE_CLASS_NUMBER);//| InputType.TYPE_CLASS_TEXT
                    tv_swipekeybord.setText(getResources().getString(R.string.PressForABC));
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
        IsOdoMeterRequire = sharedPrefODO.getString(AppConstants.IS_ODO_METER_REQUIRE, "");
        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IS_DEPARTMENT_REQUIRE, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IS_PERSONNEL_PIN_REQUIRE, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IS_OTHER_REQUIRE, "");

        PreviousOdo = sharedPrefODO.getString("PreviousOdo", "");
        OdoLimit = sharedPrefODO.getString("OdoLimit", "0");
        OdometerReasonabilityConditions = sharedPrefODO.getString("OdometerReasonabilityConditions", "");
        CheckOdometerReasonable = sharedPrefODO.getString("CheckOdometerReasonable", "");
        LastTransactionFuelQuantity = sharedPrefODO.getString("LastTransactionFuelQuantity", "0");

        TimeOutinMinute = sharedPrefODO.getString(AppConstants.TIMEOUT, "1");
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
                                CommonUtils.hideKeyboard(AcceptOdoActivity.this);
                                Istimeout_Sec = false;
                                AppConstants.clearEditTextFieldsOnBack(AcceptOdoActivity.this);

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
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Entered Odometer : " + editOdoTenths.getText());

            if (!editOdoTenths.getText().toString().trim().isEmpty()) {

                int C_AccOdoMeter = 0;
                if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS1")) {
                    Constants.ODO_METER_FS1 = Integer.parseInt(editOdoTenths.getText().toString().trim());
                    C_AccOdoMeter = Constants.ODO_METER_FS1;
                } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS2")) {
                    Constants.ODO_METER_FS2 = Integer.parseInt(editOdoTenths.getText().toString().trim());
                    C_AccOdoMeter = Constants.ODO_METER_FS2;
                } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS3")) {
                    Constants.ODO_METER_FS3 = Integer.parseInt(editOdoTenths.getText().toString().trim());
                    C_AccOdoMeter = Constants.ODO_METER_FS3;
                } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS4")) {
                    Constants.ODO_METER_FS4 = Integer.parseInt(editOdoTenths.getText().toString().trim());
                    C_AccOdoMeter = Constants.ODO_METER_FS4;
                } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS5")) {
                    Constants.ODO_METER_FS5 = Integer.parseInt(editOdoTenths.getText().toString().trim());
                    C_AccOdoMeter = Constants.ODO_METER_FS5;
                } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS6")) {
                    Constants.ODO_METER_FS6 = Integer.parseInt(editOdoTenths.getText().toString().trim());
                    C_AccOdoMeter = Constants.ODO_METER_FS6;
                }

                OfflineConstants.storeCurrentTransaction(AcceptOdoActivity.this, "", "", "", editOdoTenths.getText().toString().trim(), "", "", "", "", "", "", "", "");

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
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + getResources().getString(R.string.peOdo0).replace("odometer", ScreenNameForOdometer.toLowerCase()));
                            CommonUtils.showMessageDilaog(AcceptOdoActivity.this, "Error", getResources().getString(R.string.peOdo0).replace("odometer", ScreenNameForOdometer.toLowerCase()));

                        } else if (CheckOdometerReasonable.trim().toLowerCase().equalsIgnoreCase("true")) {

                            if (LastTxtnQuantity > 10 && C_AccOdoMeter == PO && (cnt123 < 3)) {
                                // Must entered different reading if the last transaction fuel quantity is greater than 10.
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + getResources().getString(R.string.prevReading));
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
                                        AppConstants.ODO_ERROR_CODE = "1";
                                    } else {
                                        AppConstants.ODO_ERROR_CODE = "0";
                                    }

                                    if (cnt123 > 3) {
                                        //gooooo
                                        allValid();
                                    } else {

                                        if (AppConstants.GENERATE_LOGS)
                                            AppConstants.writeInFile(TAG + "The " + ScreenNameForOdometer + " you have entered is not within the reasonability that has been set for this " + ScreenNameForVehicle + ". Please contact your Manager.");
                                        editOdoTenths.setText("");
                                        Istimeout_Sec = true;
                                        ResetTimeoutOdoScreen();
                                        CommonUtils.showMessageDilaog(AcceptOdoActivity.this, "Message", getResources().getString(R.string.OdoNotInReasonability).replace("Odometer", ScreenNameForOdometer).replace("Vehicle", ScreenNameForVehicle));
                                    }
                                }
                            } else {

                                if (C_AccOdoMeter >= PO && C_AccOdoMeter <= OL) {
                                    ///gooooo
                                    allValid();
                                } else {
                                    editOdoTenths.setText("");
                                    if (AppConstants.GENERATE_LOGS) {
                                        AppConstants.writeInFile(TAG + "The " + ScreenNameForOdometer + " you have entered is not within the reasonability that has been set for this " + ScreenNameForVehicle + ". Please contact your Manager.");
                                    }
                                    Istimeout_Sec = true;
                                    ResetTimeoutOdoScreen();
                                    CommonUtils.showMessageDilaog(AcceptOdoActivity.this, "Message", getResources().getString(R.string.OdoNotInReasonability).replace("Odometer", ScreenNameForOdometer).replace("Vehicle", ScreenNameForVehicle));
                                }
                            }
                        } else {

                            /*if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + " Odo Entered" + C_AccOdoMeter);*/
                            //comment By JB -it  must take ANY number they enter on the 4th try
                            allValid();

                        }
                    } else {
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "Internet Connection: " + cd.isConnectingToInternet() + "; NETWORK_STRENGTH: " + AppConstants.NETWORK_STRENGTH);
                        //offline-------------------
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "Offline Entered Odometer: " + editOdoTenths.getText());

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

                                        if (AppConstants.GENERATE_LOGS)
                                            AppConstants.writeInFile(TAG + "Offline Previous Odometer : " + previous_odometer);
                                    }

                                    if (AppConstants.OFF_ODO_LIMIT != null && !AppConstants.OFF_ODO_LIMIT.isEmpty()) {

                                        odo_limit = Integer.parseInt(AppConstants.OFF_ODO_LIMIT);
                                        odo_limit = previous_odometer + (odo_limit) * 5;

                                        if (AppConstants.GENERATE_LOGS)
                                            AppConstants.writeInFile(TAG + "Offline Odometer limit: " + odo_limit);

                                    }
                                } catch (Exception e) {
                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(TAG + "Exception in odo saveButtonAction Offline mode. " + e.getMessage());
                                }

                                if (entered_odometer == 0) { // Must be greater than 0.
                                    Istimeout_Sec = true;
                                    ResetTimeoutOdoScreen();
                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(TAG + getResources().getString(R.string.peOdo0).replace("odometer", ScreenNameForOdometer.toLowerCase()));
                                    CommonUtils.showMessageDilaog(AcceptOdoActivity.this, "Message", getResources().getString(R.string.peOdo0).replace("odometer", ScreenNameForOdometer.toLowerCase()));

                                } else if (AppConstants.OFF_ODO_REASONABLE != null && AppConstants.OFF_ODO_REASONABLE.trim().toLowerCase().equalsIgnoreCase("true")) {

                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(TAG + "Offline Odometer Reasonability : " + AppConstants.OFF_ODO_REASONABLE);

                                    if (AppConstants.OFF_ODO_CONDITIONS != null && AppConstants.OFF_ODO_CONDITIONS.trim().equalsIgnoreCase("1")) {

                                        if (AppConstants.GENERATE_LOGS)
                                            AppConstants.writeInFile(TAG + "Offline Odometer conditions : " + AppConstants.OFF_ODO_CONDITIONS);

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
                                                if (AppConstants.GENERATE_LOGS)
                                                    AppConstants.writeInFile(TAG + "Please enter Correct " + ScreenNameForOdometer);
                                                CommonUtils.showMessageDilaog(AcceptOdoActivity.this, "Message", getResources().getString(R.string.IncorrectOdo).replace("Odometer", ScreenNameForOdometer));
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
                                            if (AppConstants.GENERATE_LOGS)
                                                AppConstants.writeInFile(TAG + "Please enter Correct " + ScreenNameForOdometer);
                                            CommonUtils.showMessageDilaog(AcceptOdoActivity.this, "Message", getResources().getString(R.string.IncorrectOdo).replace("Odometer", ScreenNameForOdometer));
                                        }

                                    }
                                } else {
                                    offlineValidOdo();
                                }
                            }

                        } else {
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + "Offline Access not granted to this HUB.");
                            //CommonUtils.AutoCloseCustomMessageDialog(AcceptOdoActivity.this, "Message", "Unable to connect server");
                            Istimeout_Sec = true;
                            ResetTimeoutOdoScreen();
                        }
                    }
                }

            } else {
                Istimeout_Sec = true;
                ResetTimeoutOdoScreen();
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "Please enter " + ScreenNameForOdometer + ", and try again.");
                CommonUtils.showMessageDilaog(AcceptOdoActivity.this, "Error", getResources().getString(R.string.peOdo).replace("Odometer", ScreenNameForOdometer));
            }


        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " Exception occurred in saveButtonAction. " + ex.getMessage());
            Log.e(TAG, ex.getMessage());
        }
    }

    public void offlineValidOdo() {
        CommonUtils.hideKeyboard(AcceptOdoActivity.this);
        if (IsNonValidateVehicle.equalsIgnoreCase("False")) {
            try {
                controller.updateOdometerByVehicleId(AppConstants.OFF_VEHICLE_ID, editOdoTenths.getText().toString().trim());
            } catch (Exception e) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " Exception occurred while updating odometer for vehicle in offline mode. " + e.getMessage());
            }
        }

        SharedPreferences sharedPrefODO = AcceptOdoActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String IsExtraOther = sharedPrefODO.getString(AppConstants.IS_EXTRA_OTHER, "");

        if (AppConstants.OFF_HOUR_REQUIRED.trim().equalsIgnoreCase("y")) {
            Intent intent = new Intent(AcceptOdoActivity.this, AcceptHoursAcitvity.class);
            startActivity(intent);
        } else if (IsExtraOther.trim().toLowerCase().equalsIgnoreCase("True")) {
            Intent intent = new Intent(AcceptOdoActivity.this, AcceptVehicleOtherInfo.class);
            startActivity(intent);
        } else {
            EntityHub obj = controller.getOfflineHubDetails(AcceptOdoActivity.this);
            if (obj.PersonnelPINNumberRequired.equalsIgnoreCase("Y")) {
                Intent intent = new Intent(AcceptOdoActivity.this, AcceptPinActivity_new.class);//AcceptPinActivity
                startActivity(intent);
            } else if (obj.IsDepartmentRequire.equalsIgnoreCase("true") && !obj.HUBType.equalsIgnoreCase("G")) {
                Intent intent = new Intent(AcceptOdoActivity.this, AcceptDeptActivity.class);
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
        CommonUtils.hideKeyboard(AcceptOdoActivity.this);
        SharedPreferences sharedPrefODO = AcceptOdoActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String IsPersonnelPINRequireForHub = sharedPrefODO.getString(AppConstants.IS_PERSONNEL_PIN_REQUIRE_FOR_HUB, "");
        String IsHoursRequire = sharedPrefODO.getString(AppConstants.IS_HOURS_REQUIRE, "");
        String IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IS_DEPARTMENT_REQUIRE, "");
        String IsOtherRequire = sharedPrefODO.getString(AppConstants.IS_OTHER_REQUIRE, "");
        String IsExtraOther = sharedPrefODO.getString(AppConstants.IS_EXTRA_OTHER, "");

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
                String authString = "Basic " + AppConstants.convertStingToBase64(authEntityClass.IMEIUDID + ":" + userEmail + ":" + "AuthorizationSequence" + AppConstants.LANG_PARAM);
                response = serverHandler.PostTextData(AcceptOdoActivity.this, AppConstants.WEB_URL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {

                CommonUtils.LogMessage(TAG, "AuthTestAsynTask ", ex);
            }
            return null;
        }

    }

    @Override
    public void onBackPressed() {
        CommonUtils.hideKeyboard(AcceptOdoActivity.this);
        //ActivityHandler.removeActivity(2);
        AppConstants.SERVER_CALL_IN_PROGRESS_FOR_PIN = false;
        AppConstants.SERVER_CALL_IN_PROGRESS_FOR_VEHICLE = false;
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