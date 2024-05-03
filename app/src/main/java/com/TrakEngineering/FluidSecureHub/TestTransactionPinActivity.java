package com.TrakEngineering.FluidSecureHub;

import static com.TrakEngineering.FluidSecureHub.server.ServerHandler.TEXT;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
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
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class TestTransactionPinActivity extends AppCompatActivity {

    private NetworkReceiver receiver = new NetworkReceiver();
    String KeyboardType = "2"; //ScreenNameForPersonnel = "PERSONNEL", ScreenNameForVehicle = "VEHICLE"

    private static final String TAG = "TestTransaction_Pin ";
    RelativeLayout footer_keyboard;
    LinearLayout Linear_layout_Save_back_buttons;
    TextView tv_title, tv_return, tv_swipekeyboard, tv_warning;
    EditText etPersonnelPin;

    boolean Istimeout_Sec = true;
    Button btnSave, btnCancel;
    String TimeOutinMinute;
    Timer t, ScreenOutTime;

    ConnectionDetector cd = new ConnectionDetector(TestTransactionPinActivity.this);
    List<Timer> TimerList = new ArrayList<Timer>();
    List<Timer> ScreenTimerList = new ArrayList<Timer>();
    public boolean PersonValidationInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_transaction_pin);

        PersonValidationInProgress = false;
        AppConstants.serverCallInProgressForPin = false;

        InItGUI();

        SharedPreferences myPrefkb = this.getSharedPreferences(AppConstants.sharedPref_KeyboardType, 0);
        KeyboardType = myPrefkb.getString("KeyboardTypePerson", "2");
        //ScreenNameForPersonnel = myPrefkb.getString("ScreenNameForPersonnel", "Personnel");
        //ScreenNameForVehicle = myPrefkb.getString("ScreenNameForVehicle", "Vehicle");

        //if (ScreenNameForPersonnel.trim().isEmpty())
        //    ScreenNameForPersonnel = "Personnel";

        tv_return = (TextView) findViewById(R.id.tv_return);
        tv_swipekeyboard = (TextView) findViewById(R.id.tv_swipekeybord);

        etPersonnelPin = (EditText) findViewById(R.id.etPersonnelPin);
        Linear_layout_Save_back_buttons = (LinearLayout) findViewById(R.id.Linear_layout_Save_back_buttons);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnCancel = (Button) findViewById(R.id.btnCancel);

        tv_warning.setText(getResources().getString(R.string.TestTxnPINScreenWarning));
        etPersonnelPin.setText("");

        getSupportActionBar().setTitle(R.string.fs_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        etPersonnelPin.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boolean ps = isKeyboardShown(etPersonnelPin.getRootView());
                if (ps) {
                    footer_keyboard.setEnabled(true);
                    footer_keyboard.setVisibility(View.VISIBLE);
                } else {
                    footer_keyboard.setEnabled(false);
                    footer_keyboard.setVisibility(View.INVISIBLE);
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.hideKeyboard(TestTransactionPinActivity.this);
                onBackPressed();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String pin = etPersonnelPin.getText().toString().trim();

                // As per #2252, Removed PIN from the logs (Only for Test Transaction)
                //if (AppConstants.GenerateLogs)
                //    AppConstants.WriteinFile(TAG + "Entered PIN num: " + pin);

                if (cd.isConnectingToInternet()) {

                    if (!pin.isEmpty()) {
                        new CallSaveButtonFunctionality().execute();
                    } else {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + getResources().getString(R.string.TestTxnPINRequired));
                        CommonUtils.showCustomMessageDilaog(TestTransactionPinActivity.this, "Error Message", getResources().getString(R.string.TestTxnPINRequired));
                    }
                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + getResources().getString(R.string.CheckInternet));
                    AppConstants.colorToastBigFont(TestTransactionPinActivity.this, getResources().getString(R.string.CheckInternet), Color.BLUE);
                }
            }
        });
        String keyboardType = "2";
        try {
            etPersonnelPin.setInputType(Integer.parseInt(keyboardType));
            if (keyboardType.equals("2")) { // Numeric keyboard
                tv_swipekeyboard.setText(getResources().getString(R.string.PressForABC));
            } else {
                tv_swipekeyboard.setText(getResources().getString(R.string.PressFor123));
            }
        } catch (Exception e) {
            System.out.println("keyboard exception");
            etPersonnelPin.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        try {
            etPersonnelPin.requestFocus();
        } catch (Exception e) {
            System.out.println("keyboard open exception");
        }

        tv_swipekeyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int InputTyp = etPersonnelPin.getInputType();
                if (InputTyp == 2) {
                    etPersonnelPin.setInputType(InputType.TYPE_CLASS_TEXT);
                    tv_swipekeyboard.setText(getResources().getString(R.string.PressFor123));
                } else {
                    etPersonnelPin.setInputType(InputType.TYPE_CLASS_NUMBER);
                    tv_swipekeyboard.setText(getResources().getString(R.string.PressForABC));
                }
            }
        });

        tv_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.hideKeyboard(TestTransactionPinActivity.this);
            }
        });

        // Registers BroadcastReceiver to track network connection changes.
        IntentFilter ifilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver();
        this.registerReceiver(receiver, ifilter);

    }

    public class CallSaveButtonFunctionality extends AsyncTask<Void, Void, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            String s = getResources().getString(R.string.PleaseWait);
            SpannableString ss2 = new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            pd = new ProgressDialog(TestTransactionPinActivity.this);
            pd.setMessage(ss2);
            pd.setCancelable(true);
            pd.show();
        }

        protected String doInBackground(Void... arg0) {

            String resp = "";
            try {

                if (!etPersonnelPin.getText().toString().trim().isEmpty()) {

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

                    Istimeout_Sec = false;

                    CheckPinFobEntity objEntityClass = new CheckPinFobEntity();
                    objEntityClass.IMEIUDID = AppConstants.getIMEI(TestTransactionPinActivity.this);
                    objEntityClass.PersonPIN = etPersonnelPin.getText().toString().trim();
                    objEntityClass.PersonFOBNumber = "";
                    objEntityClass.FOBNumber = "";
                    objEntityClass.MagneticCardNumber = "";
                    objEntityClass.Barcode = "";
                    objEntityClass.IsTestTransaction = "y";

                    //if (AppConstants.GenerateLogs)
                    //    AppConstants.WriteinFile(TAG + "PIN Entered Manually: " + etPersonnelPin.getText().toString().trim());

                    Gson gson = new Gson();
                    String jsonData = gson.toJson(objEntityClass);
                    String userEmail = CommonUtils.getCustomerDetails(TestTransactionPinActivity.this).PersonEmail;

                    System.out.println("jsonDatajsonDatajsonData" + jsonData);
                    //----------------------------------------------------------------------------------
                    String authString = "Basic " + AppConstants.convertStingToBase64(objEntityClass.IMEIUDID + ":" + userEmail + ":" + "CheckValidPinOrFOBNUmber" + AppConstants.LANG_PARAM);

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
                    AppConstants.WriteinFile(TAG + " CallSaveButtonFunctionality STE: " + e.getMessage());
                /*if (OfflineConstants.isOfflineAccess(TestTransactionPinActivity.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }*/
                GetBackToWelcomeActivity();

            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "CallSaveButtonFunctionality InBackground Exception: " + e.getMessage());
                /*if (OfflineConstants.isOfflineAccess(TestTransactionPinActivity.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }*/
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

                    String ResponceText = jsonObject.getString("ResponceText");
                    String ResponceMessage = jsonObject.getString("ResponceMessage");
                    String PersonName = jsonObject.getString("PersonName");

                    System.out.println("ResponceText.." + ResponceText);
                    System.out.println("ResponceMessage.." + ResponceMessage);

                    if (ResponceText.equalsIgnoreCase("success")) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Support Test Transaction performed by " + PersonName);

                        btnSave.setClickable(false);

                        AppConstants.serverCallInProgressForPin = true;
                        PersonValidationInProgress = true;
                        AcceptServiceCall asc = new AcceptServiceCall();
                        asc.activity = TestTransactionPinActivity.this;
                        asc.ServerCallName = "AuthorizationsequenceForTestTransaction";
                        asc.IsTestTransaction = "y";
                        asc.checkAllFields();

                    } else {

                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "PIN rejected. Error: " + ResponceMessage);

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

                        DialogRecreate(TestTransactionPinActivity.this, "Message", ResponceMessage);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "CallSaveButtonFunctionality onPostExecute Exception: " + e.getMessage());
                    /*if (OfflineConstants.isOfflineAccess(TestTransactionPinActivity.this)) {
                        AppConstants.NETWORK_STRENGTH = false;
                    }*/
                }
            } else {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Internet Connection: " + cd.isConnectingToInternet());
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "CallSaveButtonFunctionality Response is empty.");
                /*if (OfflineConstants.isOfflineAccess(TestTransactionPinActivity.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }*/
            }
        }
    }

    private void InItGUI() {

        btnSave = (Button) findViewById(R.id.btnSave);
        footer_keyboard = (RelativeLayout) findViewById(R.id.footer_keybord);
        Linear_layout_Save_back_buttons = (LinearLayout) findViewById(R.id.Linear_layout_Save_back_buttons);
        tv_return = (TextView) findViewById(R.id.tv_return);
        tv_swipekeyboard = (TextView) findViewById(R.id.tv_swipekeybord);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_warning = (TextView) findViewById(R.id.tv_warning);

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

    /*public void hideKeybord() {

        InputMethodManager imm = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }*/

    @Override
    protected void onResume() {
        super.onResume();

        if (AppConstants.serverAuthCallCompleted) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "<onResume skipped.>");
            return;
        }

        AppConstants.AUTH_CALL_SUCCESS = false;

        etPersonnelPin.setText("");

        TimeoutPinScreen();

        btnSave.setClickable(true);
        //Set/Reset EnterPin text
        etPersonnelPin.setText("");

        DisplayScreenInit();

    }

    public void TimeoutPinScreen() {
        Log.i("TimeoutPinScreen", "Start");
        SharedPreferences sharedPrefODO = TestTransactionPinActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        AppConstants.HUB_ID = sharedPrefODO.getString(AppConstants.HubId, "");
        TimeOutinMinute = sharedPrefODO.getString(AppConstants.TimeOut, "1");

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
                                CommonUtils.hideKeyboard(TestTransactionPinActivity.this);
                                Istimeout_Sec = false;

                                Intent i = new Intent(TestTransactionPinActivity.this, WelcomeActivity.class);
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

    public void DisplayScreenInit() {

        btnSave.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);

        int width = ActionBar.LayoutParams.MATCH_PARENT;
        int height = ActionBar.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width, height);
        parms.gravity = Gravity.CENTER;
        etPersonnelPin.setLayoutParams(parms);

        Linear_layout_Save_back_buttons.setVisibility(View.VISIBLE);

        etPersonnelPin.setEnabled(true);
        btnSave.setEnabled(true);
        Linear_layout_Save_back_buttons.setVisibility(View.VISIBLE);
        etPersonnelPin.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onPause() {
        super.onPause();
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

        menu.findItem(R.id.mreboot_reader).setVisible(false);
        menu.findItem(R.id.madd_link).setVisible(false);
        menu.findItem(R.id.enable_debug_window).setVisible(false);
        menu.findItem(R.id.mclose).setVisible(false);
        menu.findItem(R.id.mreload).setVisible(false);
        menu.findItem(R.id.mreconnect_ble_readers).setVisible(false);
        menu.findItem(R.id.mshow_reader_status).setVisible(false);
        menu.findItem(R.id.mcamera_back).setVisible(false);
        menu.findItem(R.id.mcamera_front).setVisible(false);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AppConstants.isTestTransaction = false;
        AppConstants.serverCallInProgressForPin = false;
        AppConstants.serverCallInProgressForVehicle = false;
        Istimeout_Sec = false;
        finish();
    }

    public void GetBackToWelcomeActivity() {
        Istimeout_Sec = false;
        AppConstants.ClearEdittextFielsOnBack(TestTransactionPinActivity.this);

        Intent i = new Intent(TestTransactionPinActivity.this, WelcomeActivity.class);
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
}