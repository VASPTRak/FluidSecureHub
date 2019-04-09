package com.TrakEngineering.FluidSecureHub;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.TrakEngineering.FluidSecureHub.offline.OffDBController;
import com.TrakEngineering.FluidSecureHub.LFBle_PIN.DeviceControlActivity_Pin;
import com.TrakEngineering.FluidSecureHub.offline.EntityHub;
import com.TrakEngineering.FluidSecureHub.offline.OfflineConstants;


public class AcceptHoursAcitvity extends AppCompatActivity {

    OffDBController controller = new OffDBController(AcceptHoursAcitvity.this);

    private NetworkReceiver receiver = new NetworkReceiver();

    private static final String TAG = "AcceptHoursAcitvity :";
    private EditText etHours;
    private String vehicleNumber;
    private String odometerTenths;
    private ProgressBar progressBar;
    private ConnectionDetector cd = new ConnectionDetector(AcceptHoursAcitvity.this);

    String OdometerReasonabilityConditions = "", CheckOdometerReasonable = "", PreviousHours = "", HoursLimit = "", IsOdoMeterRequire = "", IsDepartmentRequire = "", IsPersonnelPINRequire = "", IsOtherRequire = "", IsHoursRequire = "";
    String TimeOutinMinute;
    boolean Istimeout_Sec = true;
    public int cnt123 = 0;

    @Override
    protected void onResume() {
        super.onResume();


        //Set/Reset EnterPin text
        if (Constants.CurrentSelectedHose.equals("FS1")) {
            etHours.setText(ZR(String.valueOf(Constants.AccHours_FS1)));
        } else if (Constants.CurrentSelectedHose.equals("FS2")) {
            etHours.setText(ZR(String.valueOf(Constants.AccHours)));
        } else if (Constants.CurrentSelectedHose.equals("FS3")) {
            etHours.setText(ZR(String.valueOf(Constants.AccHours_FS3)));
        } else if (Constants.CurrentSelectedHose.equals("FS4")) {
            etHours.setText(ZR(String.valueOf(Constants.AccHours_FS4)));
        }

    }

    public String ZR(String zeroString) {
        if (zeroString.trim().equalsIgnoreCase("0"))
            return "";
        else
            return zeroString;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  ActivityHandler.addActivities(5, AcceptHoursAcitvity.this);

        setContentView(R.layout.activity_accept_hours_acitvity);
        getSupportActionBar().setTitle(AppConstants.BrandName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        InItGUI();

        SharedPreferences sharedPrefODO = AcceptHoursAcitvity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        IsOdoMeterRequire = sharedPrefODO.getString(AppConstants.IsOdoMeterRequire, "");
        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
        IsHoursRequire = sharedPrefODO.getString(AppConstants.IsHoursRequire, "");


        OdometerReasonabilityConditions = sharedPrefODO.getString("OdometerReasonabilityConditions", "");
        CheckOdometerReasonable = sharedPrefODO.getString("CheckOdometerReasonable", "");
        PreviousHours = sharedPrefODO.getString("PreviousHours", "");
        HoursLimit = sharedPrefODO.getString("HoursLimit", "");

        TimeOutinMinute = sharedPrefODO.getString(AppConstants.TimeOut, "1");

        long screenTimeOut = Integer.parseInt(TimeOutinMinute) * 60000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Istimeout_Sec) {
                    Istimeout_Sec = false;
                    AppConstants.ClearEdittextFielsOnBack(AcceptHoursAcitvity.this);

                    // ActivityHandler.GetBacktoWelcomeActivity();

                    Intent i = new Intent(AcceptHoursAcitvity.this, WelcomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }

            }
        }, screenTimeOut);

        vehicleNumber = getIntent().getStringExtra(Constants.VEHICLE_NUMBER);


        // Registers BroadcastReceiver to track network connection changes.
        IntentFilter ifilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver();
        this.registerReceiver(receiver, ifilter);


    }

    private void InItGUI() {
        try {
            etHours = (EditText) findViewById(R.id.etHours);
            progressBar = (ProgressBar) findViewById(R.id.progressBar);

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


            if (!etHours.getText().toString().trim().isEmpty()) {

                int C_AccHours;
                if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
                    Constants.AccHours_FS1 = Integer.parseInt(etHours.getText().toString().trim());
                    C_AccHours = Constants.AccHours_FS1;
                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
                    Constants.AccHours = Integer.parseInt(etHours.getText().toString().trim());
                    C_AccHours = Constants.AccHours;
                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
                    Constants.AccHours_FS3 = Integer.parseInt(etHours.getText().toString().trim());
                    C_AccHours = Constants.AccHours_FS3;
                } else { //(Constants.CurrentSelectedHose.equalsIgnoreCase("FS4"))
                    Constants.AccHours_FS4 = Integer.parseInt(etHours.getText().toString().trim());
                    C_AccHours = Constants.AccHours_FS4;
                }

                int PO = Integer.parseInt(PreviousHours.trim());
                int OL = Integer.parseInt(HoursLimit.trim());


                OfflineConstants.storeCurrentTransaction(AcceptHoursAcitvity.this, "", "", "", "", etHours.getText().toString().trim(), "", "", "");

                if (cd.isConnectingToInternet()) {
                    if (CheckOdometerReasonable.trim().toLowerCase().equalsIgnoreCase("true")) {

                        if (OdometerReasonabilityConditions.trim().equalsIgnoreCase("1")) {

                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + " Hours: Entered" + C_AccHours);
                            if (C_AccHours >= PO && C_AccHours <= OL) {
                                //gooooo
                                allValid();
                            } else {
                                cnt123 += 1;

                                if (cnt123 > 3) {
                                    //gooooo
                                    allValid();
                                } else {

                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + " Hours: Entered" + C_AccHours + " is not within the reasonability");
                                    etHours.setText("");
                                    AppConstants.colorToastBigFont(getApplicationContext(), "The Hours entered is not within the reasonability your administrator has assigned, please contact your administrator.", Color.RED);//Bad odometer! Please try again.
                                }
                            }

                        } else {


                            if (C_AccHours >= PO && C_AccHours <= OL) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " Hours: Entered" + C_AccHours);
                                ///gooooo
                                allValid();
                            } else {
                                etHours.setText("");
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + " Hours: Entered" + C_AccHours + " is not within the reasonability");
                                AppConstants.colorToastBigFont(getApplicationContext(), "The Hours entered is not within the reasonability your administrator has assigned, please contact your administrator.", Color.RED);
                            }
                        }
                    } else {

                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Hours: Entered" + C_AccHours);
                        //comment By JB -it  must take ANY number they enter on the 4th try
                        allValid();


                    }
                } else {
                    //offline----------------------

                    AppConstants.WriteinFile("Offline Hours : " + etHours.getText().toString().trim());

                    if (OfflineConstants.isOfflineAccess(AcceptHoursAcitvity.this)) {

                        if (AppConstants.OFF_CURRENT_HOUR != null && !AppConstants.OFF_CURRENT_HOUR.isEmpty()) {
                            int current_hour = Integer.parseInt(AppConstants.OFF_CURRENT_HOUR);

                            if (current_hour < Integer.parseInt(etHours.getText().toString().trim())) {

                                EntityHub obj = controller.getOfflineHubDetails(AcceptHoursAcitvity.this);
                                if (obj.PersonnelPINNumberRequired.equalsIgnoreCase("Y")) {
                                    Intent intent = new Intent(AcceptHoursAcitvity.this, DeviceControlActivity_Pin.class);//AcceptPinActivity
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(AcceptHoursAcitvity.this, DisplayMeterActivity.class);
                                    startActivity(intent);
                                }

                            } else {
                                AppConstants.colorToastBigFont(getApplicationContext(), "Entered hours is less than previous hours", Color.RED);
                            }
                        }


                    } else {
                        AppConstants.colorToastBigFont(getApplicationContext(), AppConstants.OFF1, Color.RED);
                    }
                }

            } else {
                CommonUtils.showMessageDilaog(AcceptHoursAcitvity.this, "Error Message", "Please enter Hours");
            }


        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    public void allValid() {

        SharedPreferences sharedPrefODO = AcceptHoursAcitvity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        String IsHoursRequire = sharedPrefODO.getString(AppConstants.IsHoursRequire, "");
        String IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        String IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
        String IsPersonnelPINRequireForHub = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequireForHub, "");


        if (IsPersonnelPINRequireForHub.equalsIgnoreCase("True")) {

            Intent intent = new Intent(AcceptHoursAcitvity.this, DeviceControlActivity_Pin.class);//AcceptPinActivity
            startActivity(intent);

        } else if (IsDepartmentRequire.equalsIgnoreCase("True")) {

            Intent intent = new Intent(AcceptHoursAcitvity.this, AcceptDeptActivity.class);
            startActivity(intent);

        } else if (IsOtherRequire.equalsIgnoreCase("True")) {

            Intent intent = new Intent(AcceptHoursAcitvity.this, AcceptOtherActivity.class);
            startActivity(intent);

        } else {

            AcceptServiceCall asc = new AcceptServiceCall();
            asc.activity = AcceptHoursAcitvity.this;
            asc.checkAllFields();
        }


    }


    @Override
    public void onBackPressed() {
        // ActivityHandler.removeActivity(5);
        Istimeout_Sec = false;
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }
}
