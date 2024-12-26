package com.TrakEngineering.FluidSecureHub;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.TrakEngineering.FluidSecureHub.entity.DepartmentValidationEntity;
import com.TrakEngineering.FluidSecureHub.offline.EntityHub;
import com.TrakEngineering.FluidSecureHub.offline.OffDBController;
import com.TrakEngineering.FluidSecureHub.offline.OfflineConstants;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.TrakEngineering.FluidSecureHub.server.ServerHandler.TEXT;

public class AcceptDeptActivity extends AppCompatActivity {

    EditText   etDeptNumber;
    TextView tv_return, tv_swipekeybord, tv_DeptNumberHeader;
    Button btnSave, btnCancel;
    String IsOdoMeterRequire = "", IsDepartmentRequire = "", IsPersonnelPINRequire = "", IsOtherRequire = "";
    public String ScreenNameForDepartment = "Department";
    String TimeOutinMinute;
    boolean Istimeout_Sec=true;
    RelativeLayout footer_keybord;
    Timer t, ScreenOutTime;
    List<Timer> DeptScreenTimerlist = new ArrayList<Timer>();
    ConnectionDetector cd = new ConnectionDetector(AcceptDeptActivity.this);
    OffDBController controller = new OffDBController(AcceptDeptActivity.this);

    private static final String TAG = "AcceptDept ";

    @Override
    protected void onResume() {
        super.onResume();

        invalidateOptionsMenu();
        //Set/Reset EnterPin text
        etDeptNumber.setText("");
        if (btnSave != null) {
            btnSave.setClickable(true);
        }

        Istimeout_Sec = true;
        TimeoutDeptScreen();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ActivityHandler.addActivities(4,AcceptDeptActivity.this);

        setContentView(R.layout.activity_accept_dept);
        tv_DeptNumberHeader = (TextView) findViewById(R.id.tv_DeptNumberHeader);
        etDeptNumber = (EditText) findViewById(R.id.etDeptNumber);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        tv_return = (TextView) findViewById(R.id.tv_return);
        tv_swipekeybord = (TextView) findViewById(R.id.tv_swipekeybord);
        footer_keybord = (RelativeLayout) findViewById(R.id.footer_keybord);


        getSupportActionBar().setTitle(AppConstants.BRAND_NAME);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences pref_ScreenName = this.getSharedPreferences(AppConstants.PREF_KEYBOARD_TYPE, 0);
        ScreenNameForDepartment = pref_ScreenName.getString("ScreenNameForDepartment", "Department");

        if (ScreenNameForDepartment.trim().isEmpty())
            ScreenNameForDepartment = "Department";

        tv_DeptNumberHeader.setText(getResources().getString(R.string.EnterDeptNumber).replace("Department", ScreenNameForDepartment));
        etDeptNumber.setHint(getResources().getString(R.string.EnterDeptNumber).replace("Department", ScreenNameForDepartment));

        etDeptNumber.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boolean ps = isKeyboardShown(etDeptNumber.getRootView());
                if (ps == true) {
                    footer_keybord.setEnabled(true);
                    footer_keybord.setVisibility(View.VISIBLE);
                } else {
                    footer_keybord.setEnabled(false);
                    footer_keybord.setVisibility(View.INVISIBLE);
                }

            }
        });

        if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS1")) {
            if (Constants.DEPARTMENT_NUMBER_FS1 != null) {
                etDeptNumber.setText(Constants.DEPARTMENT_NUMBER_FS1);
            }
        } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS2")) {
            if (Constants.DEPARTMENT_NUMBER_FS2 != null) {
                etDeptNumber.setText(Constants.DEPARTMENT_NUMBER_FS2);
            }
        } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS3")) {
            if (Constants.DEPARTMENT_NUMBER_FS3 != null) {
                etDeptNumber.setText(Constants.DEPARTMENT_NUMBER_FS3);
            }
        } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS4")) {
            if (Constants.DEPARTMENT_NUMBER_FS4 != null) {
                etDeptNumber.setText(Constants.DEPARTMENT_NUMBER_FS4);
            }
        } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS5")) {
            if (Constants.DEPARTMENT_NUMBER_FS5 != null) {
                etDeptNumber.setText(Constants.DEPARTMENT_NUMBER_FS5);
            }
        } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS6")) {
            if (Constants.DEPARTMENT_NUMBER_FS6 != null) {
                etDeptNumber.setText(Constants.DEPARTMENT_NUMBER_FS6);
            }
        }

        SharedPreferences sharedPrefODO = AcceptDeptActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        IsOdoMeterRequire = sharedPrefODO.getString(AppConstants.IS_ODO_METER_REQUIRE, "");
        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IS_DEPARTMENT_REQUIRE, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IS_PERSONNEL_PIN_REQUIRE, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IS_OTHER_REQUIRE, "");

        TimeOutinMinute = sharedPrefODO.getString(AppConstants.TIMEOUT, "1");

        long screenTimeOut= Integer.parseInt(TimeOutinMinute) * 60000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Istimeout_Sec) {
                    CommonUtils.hideKeyboard(AcceptDeptActivity.this);
                    Istimeout_Sec = false;
                    AppConstants.clearEditTextFieldsOnBack(AcceptDeptActivity.this);

                    // ActivityHandler.GetBacktoWelcomeActivity();
                    Intent i = new Intent(AcceptDeptActivity.this, WelcomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            }
        }, screenTimeOut);


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.hideKeyboard(AcceptDeptActivity.this);
                Istimeout_Sec=false;

                CommonUtils.LogMessage(TAG, TAG + "Entered Department : " + etDeptNumber.getText(), null);
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "Entered Department : " + etDeptNumber.getText());

                if (!etDeptNumber.getText().toString().trim().isEmpty()) {

                    if (cd.isConnectingToInternet() && AppConstants.NETWORK_STRENGTH) {
                        new CallSaveButtonValidation().execute();
                    } else {
                        OfflineDepartmentValidation();
                    }

                } else {
                    Istimeout_Sec = true;
                    ResetTimeoutDeptScreen();
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + "Please enter " + ScreenNameForDepartment + ", and try again.");
                    CommonUtils.showMessageDilaog(AcceptDeptActivity.this, "Error Message", getResources().getString(R.string.RequireDeptNumber).replace("Department", ScreenNameForDepartment));
                }
            }
        });

        SharedPreferences myPrefkb = this.getSharedPreferences(AppConstants.PREF_KEYBOARD_TYPE, 0);
        String KeyboardType = myPrefkb.getString("KeyboardTypeDepartment", "2");

        try {
            if (KeyboardType.equals("2")) {
                etDeptNumber.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_TEXT);
            } else {
                etDeptNumber.setInputType(Integer.parseInt(KeyboardType));
            }
        }catch (Exception e)
        {
            System.out.println("keyboard exception");
            etDeptNumber.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        try {
            etDeptNumber.requestFocus();
        } catch (Exception e) {
            System.out.println("keyboard open exception");
        }

        tv_swipekeybord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int InputTyp = etDeptNumber.getInputType();
                if (InputTyp == 2 || InputTyp == 3) {
                    etDeptNumber.setInputType(InputType.TYPE_CLASS_TEXT);
                    tv_swipekeybord.setText(getResources().getString(R.string.PressFor123));
                } else {

                    etDeptNumber.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_TEXT);
                    tv_swipekeybord.setText(getResources().getString(R.string.PressForABC));
                }

            }
        });

        tv_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.hideKeyboard(AcceptDeptActivity.this);
            }
        });
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

    private void TimeoutDeptScreen() {

        SharedPreferences sharedPrefODO = AcceptDeptActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        IsOdoMeterRequire = sharedPrefODO.getString(AppConstants.IS_ODO_METER_REQUIRE, "");
        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IS_DEPARTMENT_REQUIRE, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IS_PERSONNEL_PIN_REQUIRE, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IS_OTHER_REQUIRE, "");

        TimeOutinMinute = sharedPrefODO.getString(AppConstants.TIMEOUT, "1");
        //long screenTimeOut= (long) (Double.parseDouble(TimeOutinMinute) *60000);
        long screenTimeOut = Integer.parseInt(TimeOutinMinute) * 60000;

        ScreenOutTime = new Timer();
        DeptScreenTimerlist.add(ScreenOutTime);
        TimerTask ttt = new TimerTask() {
            @Override
            public void run() {

                //do something
                invalidateOptionsMenu();
                if (Istimeout_Sec) {

                    try {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CommonUtils.hideKeyboard(AcceptDeptActivity.this);
                                Istimeout_Sec = false;
                                AppConstants.clearEditTextFieldsOnBack(AcceptDeptActivity.this);

                                Intent i = new Intent(AcceptDeptActivity.this, WelcomeActivity.class);
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

    public void ResetTimeoutDeptScreen(){


        CancelTimerScreenOut();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        TimeoutDeptScreen();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        CommonUtils.hideKeyboard(AcceptDeptActivity.this);
        // ActivityHandler.removeActivity(4);
        AppConstants.SERVER_CALL_IN_PROGRESS_FOR_PIN = false;
        AppConstants.SERVER_CALL_IN_PROGRESS_FOR_VEHICLE = false;
        Istimeout_Sec=false;
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

    /*public void hideKeybord() {

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }*/

    private class CallSaveButtonValidation extends AsyncTask<Void, Void, String> {

        String deptNumber = etDeptNumber.getText().toString().trim();

        //ProgressDialog pd;
        AlertDialog alertDialog;

        @Override
        protected void onPreExecute() {

            String s = getResources().getString(R.string.PleaseWaitMessage);
            alertDialog = AlertDialogUtil.createAlertDialog(AcceptDeptActivity.this, s, true);
            alertDialog.show();

            Thread thread = new Thread() {
                @Override
                public void run() {
                    AlertDialogUtil.runAnimatedLoadingDots(AcceptDeptActivity.this, s, alertDialog, true);
                }
            };
            thread.start();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String resp = "";
            String pinNumber = "";
            try {

                if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS1")) {
                    Constants.DEPARTMENT_NUMBER_FS1 = etDeptNumber.getText().toString().trim();
                    pinNumber = Constants.PERSONNEL_PIN_FS1;
                } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS2")) {
                    Constants.DEPARTMENT_NUMBER_FS2 = etDeptNumber.getText().toString().trim();
                    pinNumber = Constants.PERSONNEL_PIN_FS2;
                } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS3")) {
                    Constants.DEPARTMENT_NUMBER_FS3 = etDeptNumber.getText().toString().trim();
                    pinNumber = Constants.PERSONNEL_PIN_FS3;
                } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS4")) {
                    Constants.DEPARTMENT_NUMBER_FS4 = etDeptNumber.getText().toString().trim();
                    pinNumber = Constants.PERSONNEL_PIN_FS4;
                } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS5")) {
                    Constants.DEPARTMENT_NUMBER_FS5 = etDeptNumber.getText().toString().trim();
                    pinNumber = Constants.PERSONNEL_PIN_FS5;
                } else if (Constants.CURRENT_SELECTED_HOSE.equalsIgnoreCase("FS6")) {
                    Constants.DEPARTMENT_NUMBER_FS6 = etDeptNumber.getText().toString().trim();
                    pinNumber = Constants.PERSONNEL_PIN_FS6;
                }

                DepartmentValidationEntity objEntityClass = new DepartmentValidationEntity();
                objEntityClass.IMEIUDID = AppConstants.getIMEI(AcceptDeptActivity.this);
                objEntityClass.DepartmentNumber = deptNumber;
                objEntityClass.PersonnelPIN = pinNumber;
                objEntityClass.RequestFromAPP = "AP";

                Gson gson = new Gson();
                String jsonData = gson.toJson(objEntityClass);
                String userEmail = CommonUtils.getCustomerDetails(AcceptDeptActivity.this).PersonEmail;

                System.out.println("jsonDatajsonDatajsonData" + jsonData);
                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(objEntityClass.IMEIUDID + ":" + userEmail + ":" + "ValidateDepartmentNumber" + AppConstants.LANG_PARAM);

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(4, TimeUnit.SECONDS);
                client.setReadTimeout(4, TimeUnit.SECONDS);
                client.setWriteTimeout(4, TimeUnit.SECONDS);


                RequestBody body = RequestBody.create(TEXT, jsonData);
                Request request = new Request.Builder()
                        .url(AppConstants.WEB_URL)
                        .post(body)
                        .addHeader("Authorization", authString)
                        .build();


                Response response = null;
                response = client.newCall(request).execute();
                resp = response.body().string();
                System.out.println("response-----"+resp);
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "CallSaveButtonFunctionality  STE2 " + e);
                if (OfflineConstants.isOfflineAccess(AcceptDeptActivity.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (OfflineConstants.isOfflineAccess(AcceptDeptActivity.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String serverRes){

            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            }

            if (serverRes != null) {

                try {

                    JSONObject jsonObject = new JSONObject(serverRes);

                    String ResponceMessage  = jsonObject.getString("ResponceMessage");

                    System.out.println("ResponceMessage .." + ResponceMessage);

                    if (ResponceMessage.equalsIgnoreCase("success")) {

                        //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile(TAG +" PIN Accepted:" + etPersonnelPin.getText().toString().trim());

                        btnSave.setClickable(false);

                        OfflineConstants.storeCurrentTransaction(AcceptDeptActivity.this, "", "", "", "", "", "", "", "", "", "", "", etDeptNumber.getText().toString().trim());

                        SharedPreferences sharedPrefODO = AcceptDeptActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                        String IsOtherRequire = sharedPrefODO.getString(AppConstants.IS_OTHER_REQUIRE, "");

                        if (IsOtherRequire.equalsIgnoreCase("True")) {

                            Intent intent = new Intent(AcceptDeptActivity.this, AcceptOtherActivity.class);
                            startActivity(intent);

                        } else {
                            AcceptServiceCall asc = new AcceptServiceCall();
                            asc.activity = AcceptDeptActivity.this;
                            asc.checkAllFields();
                        }
                    } else {

                        String ResponceText = jsonObject.getString("ResponceText");

                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "Department rejected. Error: " + ResponceText);

                        String ValidationFailFor = jsonObject.getString("ValidationFailFor");

                        DialogRecreate(AcceptDeptActivity.this,"Message",ResponceText);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (OfflineConstants.isOfflineAccess(AcceptDeptActivity.this)) {
                        AppConstants.NETWORK_STRENGTH = false;
                    }
                }
            } else {
                Log.i(TAG, "CallSaveButtonValidation Server Response Empty!");
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "CallSaveButtonValidation  Server Response Empty!");
            }

        }

        public void DialogRecreate(final Activity context, final String title, final String message) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    new AlertDialog.Builder(context)
                            //.setTitle(title)
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

    }

    private void CancelTimerScreenOut(){

        for (int i = 0; i < DeptScreenTimerlist.size(); i++) {
            DeptScreenTimerlist.get(i).cancel();
        }

    }

    private void OfflineDepartmentValidation() {
        try {
            String deptNumber = etDeptNumber.getText().toString().trim();
            HashMap<String, String> hmap = new HashMap<>();
            hmap = controller.getDepartmentDetailsByDepartmentNumber(deptNumber);

            if (hmap != null && hmap.size() > 0) {
                String DepartmentNumber = hmap.get("DepartmentNumber");

                OfflineConstants.storeCurrentTransaction(AcceptDeptActivity.this, "", "", "", "", "", "", "", "", "", "", "", DepartmentNumber);

                EntityHub obj = controller.getOfflineHubDetails(AcceptDeptActivity.this);
                if (obj.ValidateDepartmentAgainstPIN.equalsIgnoreCase("true")) { // validate with entered PIN
                    if (obj.PersonnelPINNumberRequired.equalsIgnoreCase("Y")) { // check only if the pin screen is required
                        String pinNumber = AppConstants.OFF_PERSON_PIN;
                        HashMap<String, String> pinMap = controller.getPersonnelDetailsByPIN(pinNumber);

                        if (pinMap.size() > 0) {
                            String AssignedDepartments = pinMap.get("AssignedDepartments");
                            if (AssignedDepartments != null && !AssignedDepartments.isEmpty()) {
                                boolean isAllowed = false;

                                String[] depts = AssignedDepartments.split(",");
                                for (String allowedDept : depts) {
                                    if (DepartmentNumber.equalsIgnoreCase(allowedDept)) {
                                        isAllowed = true;
                                        break;
                                    }
                                }

                                if (isAllowed) {
                                    allValid();
                                } else {
                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(TAG + "You are not authorized for this department. (" + DepartmentNumber + ")");
                                    CommonUtils.showMessageDilaog(AcceptDeptActivity.this, "Error Message", getResources().getString(R.string.NotAuthorizedForDept).replace("department", ScreenNameForDepartment));
                                }
                            } else {
                                //allValid(); //?
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + "You are not authorized for this department. (" + DepartmentNumber + ")");
                                CommonUtils.showMessageDilaog(AcceptDeptActivity.this, "Error Message", getResources().getString(R.string.NotAuthorizedForDept).replace("department", ScreenNameForDepartment));
                            }
                        } else {
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + "Pin Number (" + pinNumber + ") not found in offline db.");
                        }
                    } else {
                        allValid();
                    }
                } else {
                    allValid();
                }
            } else {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "Department (" + deptNumber + ") not found in offline db.");
                CommonUtils.showMessageDilaog(AcceptDeptActivity.this, "Error Message", getResources().getString(R.string.InvalidDept).replace("Department", ScreenNameForDepartment));
            }
        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "OfflineDepartmentValidation Exception: " + ex.getMessage());
        }
    }

    public void allValid() {
        CommonUtils.hideKeyboard(AcceptDeptActivity.this);
        EntityHub obj = controller.getOfflineHubDetails(AcceptDeptActivity.this);
        if (obj.IsOtherRequire.equalsIgnoreCase("True") && !obj.HUBType.equalsIgnoreCase("G")) {
            Intent intent = new Intent(AcceptDeptActivity.this, AcceptOtherActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(AcceptDeptActivity.this, DisplayMeterActivity.class);
            startActivity(intent);
        }
    }
}