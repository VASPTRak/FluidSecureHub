package com.TrakEngineering.FluidSecureHub;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.TrakEngineering.FluidSecureHub.entity.CheckPinFobEntity;
import com.TrakEngineering.FluidSecureHub.entity.VehicleRequireEntity;
import com.TrakEngineering.FluidSecureHub.server.ServerHandler;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.common.GooglePlayServicesUtil.getErrorString;

public class AcceptPinActivity extends AppCompatActivity {

    EditText etPersonnelPin;
    TextView tv_or, tv_fob_Reader, tv_fob_number, tv_enter_pin_no, tv_return, tv_swipekeybord, tv_ok, tv_dont_have_fob;
    Button btnSave, btnCancel;
    String IsOdoMeterRequire = "", IsDepartmentRequire = "", IsPersonnelPINRequire = "", IsOtherRequire = "";
    String TimeOutinMinute;
    boolean Istimeout_Sec = true;
    int FobReadingCount = 0;
    int FobRetryCount = 0;
    RelativeLayout footer_keybord;
    LinearLayout Linear_layout_Save_back_buttons;
    Timer t, ScreenOutTime;

    private static final String TAG = "PinActivity ";

    @Override
    protected void onResume() {
        super.onResume();

        //Toast.makeText(getApplicationContext(), "FOK_KEY" + AppConstants.APDU_FOB_KEY, Toast.LENGTH_SHORT).show();
        showKeybord();
        AppConstants.APDU_FOB_KEY = "";
        Istimeout_Sec = true;
        TimeoutPinScreen();

        btnSave.setClickable(true);

        //Set/Reset EnterPin text
        if (Constants.CurrentSelectedHose.equals("FS1")) {
            etPersonnelPin.setText(Constants.AccPersonnelPIN_FS1);
        } else if (Constants.CurrentSelectedHose.equals("FS2")) {
            etPersonnelPin.setText(Constants.AccPersonnelPIN);
        } else if (Constants.CurrentSelectedHose.equals("FS3")) {
            etPersonnelPin.setText(Constants.AccPersonnelPIN_FS3);
        } else if (Constants.CurrentSelectedHose.equals("FS4")) {
            etPersonnelPin.setText(Constants.AccPersonnelPIN_FS4);
        }

        DisplayScreenInit();


        t = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                //do something
                System.out.println("Pin FOK_KEY" + AppConstants.APDU_FOB_KEY);
                if (!AppConstants.APDU_FOB_KEY.equalsIgnoreCase("") && AppConstants.APDU_FOB_KEY.length() > 6) {

                    try {


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                etPersonnelPin.setText("");
                                System.out.println("pin2 FOK_KEY" + AppConstants.APDU_FOB_KEY);
                                ScreenOutTime.cancel();//Stop screenout
                                GetPinNuOnFobKeyDetection();
                                tv_fob_number.setText("Fob No: " + AppConstants.APDU_FOB_KEY);
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
        ScreenOutTime.cancel();//Stop screenout
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();


    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ActivityHandler.addActivities(3, AcceptPinActivity.this);
        setContentView(R.layout.activity_accept_pin);

        etPersonnelPin = (EditText) findViewById(R.id.etPersonnelPin);
        tv_fob_number = (TextView) findViewById(R.id.tv_fob_number);
        tv_enter_pin_no = (TextView) findViewById(R.id.tv_enter_pin_no);
        footer_keybord = (RelativeLayout) findViewById(R.id.footer_keybord);
        Linear_layout_Save_back_buttons = (LinearLayout) findViewById(R.id.Linear_layout_Save_back_buttons);
        tv_return = (TextView) findViewById(R.id.tv_return);
        tv_swipekeybord = (TextView) findViewById(R.id.tv_swipekeybord);
        tv_ok = (TextView) findViewById(R.id.tv_ok);
        tv_dont_have_fob = (TextView) findViewById(R.id.tv_dont_have_fob);//Enter your PERSONNEL ID in the green box below
        //String content = "Enter your<br> <b>PERSONNEL ID </b>in<br> the green box below";
        String content = getResources().getString(R.string.EnterPersonnelId).replace("PERSONNEL", "<br><b>PERSONNEL ID</b><br>");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tv_dont_have_fob.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
            System.out.println(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tv_dont_have_fob.setText(Html.fromHtml(content));
            System.out.println(Html.fromHtml(content));
        }


        btnSave = (Button) findViewById(R.id.btnSave);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        tv_fob_Reader = (TextView) findViewById(R.id.tv_fob_Reader);
        tv_or = (TextView) findViewById(R.id.tv_or);


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
            @Override
            public void onClick(View v) {

                String pin = etPersonnelPin.getText().toString().trim();
                String FKey = AppConstants.APDU_FOB_KEY;

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Entered Pin: " + etPersonnelPin.getText());

                if (FKey.equalsIgnoreCase("")) {

                    CallSaveButtonFunctionality();//Press Enter fun
                } else if (pin.equalsIgnoreCase("") && !FKey.equalsIgnoreCase("")) {
                    GetPinNuOnFobKeyDetection();
                } else if (!pin.equalsIgnoreCase("") && !FKey.equalsIgnoreCase("")) {
                    GetPinNuOnFobKeyDetection();
                }
            }
        });


        tv_swipekeybord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int InputTyp = etPersonnelPin.getInputType();
                if (InputTyp == 3) {
                    etPersonnelPin.setInputType(InputType.TYPE_CLASS_TEXT);
                    tv_swipekeybord.setText(getResources().getString(R.string.PressFor123));
                } else {

                    etPersonnelPin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_TEXT);
                    tv_swipekeybord.setText(getResources().getString(R.string.PressForABC));
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        // ActivityHandler.removeActivity(3);
        Istimeout_Sec = false;
        ScreenOutTime.cancel();//Stop screenout
        //AppConstants.ClearEdittextFielsOnBack(AcceptPinActivity.this); //Clear EditText on move to welcome activity.
        finish();
    }


    public void Readfobkey() {


    }


    public void DisplayScreenInit() {

        etPersonnelPin.setEnabled(true);
        btnSave.setEnabled(true);
        tv_fob_number.setText("");
        tv_ok.setVisibility(View.GONE);
        tv_enter_pin_no.setVisibility(View.GONE);
        tv_fob_Reader.setVisibility(View.VISIBLE);
        tv_or.setVisibility(View.VISIBLE);
        tv_fob_Reader.setVisibility(View.VISIBLE);
        tv_dont_have_fob.setVisibility(View.VISIBLE);
        Linear_layout_Save_back_buttons.setVisibility(View.VISIBLE);
        etPersonnelPin.setVisibility(View.VISIBLE);
        // etPersonnelPin.setText("");

    }

    public void DisplayScreenFobReadSuccess() {

        //Display on success
        tv_fob_number.setVisibility(View.GONE);
        tv_fob_Reader.setVisibility(View.GONE);
        tv_or.setVisibility(View.GONE);
        tv_enter_pin_no.setVisibility(View.VISIBLE);
        tv_ok.setVisibility(View.VISIBLE);
        tv_ok.setText("FOB Read Successfully");
        tv_dont_have_fob.setVisibility(View.GONE);
        etPersonnelPin.setVisibility(View.GONE);
        Linear_layout_Save_back_buttons.setVisibility(View.GONE);

    }

    public void GetPinNuOnFobKeyDetection() {

        try {

            CheckPinFobEntity objEntityClass = new CheckPinFobEntity();
            objEntityClass.IMEIUDID = AppConstants.getIMEI(AcceptPinActivity.this);
            objEntityClass.PersonPIN = String.valueOf(etPersonnelPin.getText());
            objEntityClass.PersonFOBNumber = AppConstants.APDU_FOB_KEY;
            objEntityClass.FromNewFOBChange = "Y";


            AcceptPinActivity.CheckValidPinOrFOBNUmber vehTestAsynTask1 = new CheckValidPinOrFOBNUmber(objEntityClass);
            vehTestAsynTask1.execute();
            vehTestAsynTask1.get();

            String serverRes = vehTestAsynTask1.response;

            if (serverRes != null) {

                JSONObject jsonObject = new JSONObject(serverRes);

                String ResponceMessage = jsonObject.getString("ResponceMessage");
                System.out.println("ResponceMessage..dt.." + ResponceMessage);

                if (ResponceMessage.equalsIgnoreCase("success")) {

                    String PersonFOBNumber = jsonObject.getString("PersonFOBNumber");
                    String PersonPIN = jsonObject.getString("PersonPIN");
                    DisplayScreenFobReadSuccess();
                    tv_enter_pin_no.setText("Personnel Number:" + PersonPIN);
                    System.out.println("PersonFOBNumber.." + PersonFOBNumber + "PersonPin" + PersonPIN);
                    etPersonnelPin.setText(PersonPIN);

                    new Handler().postDelayed(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                        @Override
                        public void run() {
                            CallSaveButtonFunctionality();//Press Enter fun
                        }
                    }, 2000);


                } else {

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "PIN rejected. Error: " + ResponceMessage);

                    Istimeout_Sec = true;
                    TimeoutPinScreen();
                    btnSave.setEnabled(true);
                    tv_fob_number.setText("");
                    tv_fob_number.setVisibility(View.GONE);
                    tv_ok.setVisibility(View.VISIBLE);
                    tv_ok.setText("Invalid FOB or Unassigned FOB");
                    tv_or.setVisibility(View.GONE);
                    tv_fob_Reader.setVisibility(View.GONE);
                    tv_dont_have_fob.setVisibility(View.VISIBLE);
                    //String content = "Enter your<br> <b>PERSONNEL ID </b>in<br> the green box below";
                    String content = getResources().getString(R.string.EnterPersonnelId).replace("PERSONNEL", "<br><b>PERSONNEL ID</b><br>");

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

                    CommonUtils.showCustomMessageDilaog(AcceptPinActivity.this, "Message", ResponceMessage);

                }
            }
        } catch (Exception ex) {
            Log.e("TAG", ex.getMessage());
        }
    }

    public class CheckValidPinOrFOBNUmber extends AsyncTask<Void, Void, String> {

        CheckPinFobEntity vrentity = null;

        public String response = null;

        public CheckValidPinOrFOBNUmber(CheckPinFobEntity vrentity) {
            this.vrentity = vrentity;
        }

        @Override
        protected String doInBackground(Void... voids) {

            try {
                ServerHandler serverHandler = new ServerHandler();

                Gson gson = new Gson();
                String jsonData = gson.toJson(vrentity);
                String userEmail = CommonUtils.getCustomerDetails(AcceptPinActivity.this).PersonEmail;

                System.out.println("jsonData123" + jsonData);
                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(vrentity.IMEIUDID + ":" + userEmail + ":" + "CheckValidPinOrFOBNUmber" + AppConstants.LANG_PARAM);
                response = serverHandler.PostTextData(AcceptPinActivity.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------
                System.out.println("jsonData1234" + response);
            } catch (Exception ex) {

                CommonUtils.LogMessage("TAG", "CheckValidPinOrFOBNUmber ", ex);
            }
            return response;
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void CallSaveButtonFunctionality() {


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
            } else {
                Constants.AccPersonnelPIN_FS4 = etPersonnelPin.getText().toString().trim();
                vehicleNumber = Constants.AccVehicleNumber_FS4;
            }

            Istimeout_Sec = false;

            try {
                VehicleRequireEntity objEntityClass = new VehicleRequireEntity();
                objEntityClass.IMEIUDID = AppConstants.getIMEI(AcceptPinActivity.this);
                objEntityClass.VehicleNumber = vehicleNumber;
                objEntityClass.WifiSSId = AppConstants.LAST_CONNECTED_SSID;
                objEntityClass.SiteId = Integer.parseInt(AppConstants.SITE_ID);
                objEntityClass.PersonnelPIN = etPersonnelPin.getText().toString().trim();
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

                        SharedPreferences sharedPrefODO = AcceptPinActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                        String IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
                        String IsHoursRequire = sharedPrefODO.getString(AppConstants.IsHoursRequire, "");
                        String IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
                        String IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");

                        if (IsDepartmentRequire.equalsIgnoreCase("True")) {


                            Intent intent = new Intent(AcceptPinActivity.this, AcceptDeptActivity.class);
                            startActivity(intent);

                        } else if (IsOtherRequire.equalsIgnoreCase("True")) {

                            Intent intent = new Intent(AcceptPinActivity.this, AcceptOtherActivity.class);
                            startActivity(intent);

                        } else {

                            AcceptServiceCall asc = new AcceptServiceCall();
                            asc.activity = AcceptPinActivity.this;
                            asc.checkAllFields();
                        }
                    } else {
                        String ResponceText = jsonObject.getString("ResponceText");

                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "PIN rejected. Error: " + ResponceText);

                        String ValidationFailFor = jsonObject.getString("ValidationFailFor");

                        if (ValidationFailFor.equalsIgnoreCase("Pin")) {
                            AppConstants.colorToastBigFont(this, ResponceText, Color.BLUE);
                            etPersonnelPin.setText("");
                            recreate();

                        } else if (ValidationFailFor.equalsIgnoreCase("Vehicle")) {

                            Intent i = new Intent(this, AcceptVehicleActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);

                        } else {
                            AppConstants.colorToastBigFont(this, ResponceText, Color.BLUE);
                            etPersonnelPin.setText("");
                            recreate();
                        }

                    }
                }
            } catch (Exception e) {

            }

                   /* if (IsOtherRequire.equalsIgnoreCase("True")) {
                        Intent intent = new Intent(AcceptPinActivity.this, AcceptOtherActivity.class);
                        startActivity(intent);
                    } else {
                        AcceptServiceCall asc = new AcceptServiceCall();
                        asc.activity = AcceptPinActivity.this;
                        asc.checkAllFields();
                    }*/
        } else {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Please enter Personnel Pin, and try again.");
            CommonUtils.showMessageDilaog(AcceptPinActivity.this, "Error Message", "Please enter Personnel Pin, and try again.");
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
                String userEmail = CommonUtils.getCustomerDetails(AcceptPinActivity.this).PersonEmail;

                System.out.println("jsonDatajsonDatajsonData" + jsonData);
                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(vrentity.IMEIUDID + ":" + userEmail + ":" + "CheckVehicleRequireOdometerEntryAndRequireHourEntry" + AppConstants.LANG_PARAM);
                response = serverHandler.PostTextData(AcceptPinActivity.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {

                CommonUtils.LogMessage("AcceptPinActivity", "CheckVehicleRequireOdometerEntryAndRequireHourEntry ", ex);
            }
            return null;
        }

    }


    public void TimeoutPinScreen() {

        SharedPreferences sharedPrefODO = AcceptPinActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        IsOdoMeterRequire = sharedPrefODO.getString(AppConstants.IsOdoMeterRequire, "");
        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
        AppConstants.HUB_ID = sharedPrefODO.getString(AppConstants.HubId, "");

        TimeOutinMinute = sharedPrefODO.getString(AppConstants.TimeOut, "1");
        //long screenTimeOut= (long) (Double.parseDouble(TimeOutinMinute) *60000);
        long screenTimeOut = Integer.parseInt(TimeOutinMinute) * 60000;

        ScreenOutTime = new Timer();
        TimerTask ttt = new TimerTask() {
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
                                AppConstants.ClearEdittextFielsOnBack(AcceptPinActivity.this);
                                // ActivityHandler.GetBacktoWelcomeActivity();

                                Intent i = new Intent(AcceptPinActivity.this, WelcomeActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                            }
                        });

                        ScreenOutTime.cancel();
                    } catch (Exception e) {

                        System.out.println(e);
                    }

                }

            }

            ;
        };
        ScreenOutTime.schedule(ttt, screenTimeOut, 500);


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

        Log.d("TAG", "isKeyboardShown ? " + isKeyboardShown + ", heightDiff:" + heightDiff + ", density:" + dm.density
                + "root view height:" + rootView.getHeight() + ", rect:" + r);

        return isKeyboardShown;
    }

    public void FobRetryLogic() {

    }

    public void hideKeybord() {

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    public void showKeybord() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }


}