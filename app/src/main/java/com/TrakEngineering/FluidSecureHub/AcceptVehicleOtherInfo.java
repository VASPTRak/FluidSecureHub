package com.TrakEngineering.FluidSecureHub;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.TrakEngineering.FluidSecureHub.offline.EntityHub;
import com.TrakEngineering.FluidSecureHub.offline.OffDBController;
import com.TrakEngineering.FluidSecureHub.offline.OfflineConstants;


public class AcceptVehicleOtherInfo extends AppCompatActivity {

    TextView tv_otherlabel, tv_return, tv_swipekeybord;
    EditText etOther;
    Button btnSave, btnCancel;//AppConstants.OtherLabel
    RelativeLayout footer_keybord;
    String IsOdoMeterRequire = "", IsDepartmentRequire = "", IsPersonnelPINRequire = "", IsOtherRequire = "", OtherLabelVehicle = "";
    String TimeOutinMinute;
    boolean Istimeout_Sec = true;
    private ConnectionDetector cd = new ConnectionDetector(AcceptVehicleOtherInfo.this);
    private static final String TAG = AcceptOtherActivity.class.getSimpleName();
    OffDBController controller = new OffDBController(AcceptVehicleOtherInfo.this);

    @Override
    protected void onResume() {
        super.onResume();

        invalidateOptionsMenu();

        etOther.setText("");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reader, menu);

        menu.findItem(R.id.mconfigure_tld).setVisible(false);
        menu.findItem(R.id.enable_debug_window).setVisible(false);
        menu.findItem(R.id.mclose).setVisible(false);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ActivityHandler.addActivities(6, AcceptOtherActivity.this);

        setContentView(R.layout.activity_accept_vehicle_other_info);

        etOther = (EditText) findViewById(R.id.etOther);
        tv_otherlabel = (TextView) findViewById(R.id.tv_otherlabel);

        btnSave = (Button) findViewById(R.id.btnSave);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        footer_keybord = (RelativeLayout) findViewById(R.id.footer_keybord);
        tv_return = (TextView) findViewById(R.id.tv_return);
        tv_swipekeybord = (TextView) findViewById(R.id.tv_swipekeybord);

        getSupportActionBar().setTitle(AppConstants.BrandName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
            if (Constants.AccVehicleOther_FS1 != null) {
                etOther.setText(Constants.AccVehicleOther_FS1);
            }

        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {

            if (Constants.AccVehicleOther != null) {
                etOther.setText(Constants.AccVehicleOther);
            }
        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {

            if (Constants.AccVehicleOther_FS3 != null) {
                etOther.setText(Constants.AccVehicleOther_FS3);
            }
        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS4")) {

            if (Constants.AccVehicleOther_FS4 != null) {
                etOther.setText(Constants.AccVehicleOther_FS4);
            }
        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS5")) {

            if (Constants.AccVehicleOther_FS5 != null) {
                etOther.setText(Constants.AccVehicleOther_FS5);
            }
        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS6")) {

            if (Constants.AccVehicleOther_FS6 != null) {
                etOther.setText(Constants.AccVehicleOther_FS6);
            }
        }

        SharedPreferences sharedPrefODO = AcceptVehicleOtherInfo.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        IsOdoMeterRequire = sharedPrefODO.getString(AppConstants.IsOdoMeterRequire, "");
        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
        OtherLabelVehicle = sharedPrefODO.getString(AppConstants.ExtraOtherLabel, "ExtraOtherLabel");

        tv_otherlabel.setText(getResources().getString(R.string.EnterHeading) + " " + OtherLabelVehicle);
        etOther.setHint(getResources().getString(R.string.EnterHeading) + " " + OtherLabelVehicle);
        TimeOutinMinute = sharedPrefODO.getString(AppConstants.TimeOut, "1");

        long screenTimeOut = Integer.parseInt(TimeOutinMinute) * 60000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Istimeout_Sec) {
                    Istimeout_Sec = false;
                    AppConstants.ClearEdittextFielsOnBack(AcceptVehicleOtherInfo.this);

                    // ActivityHandler.GetBacktoWelcomeActivity();

                    Intent i = new Intent(AcceptVehicleOtherInfo.this, WelcomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }

            }
        }, screenTimeOut);

        SharedPreferences myPrefkb = this.getSharedPreferences(AppConstants.sharedPref_KeyboardType, 0);
        String KeyboardType = myPrefkb.getString("KeyboardTypeOther", "1");

        try {
            etOther.setInputType(Integer.parseInt(KeyboardType));
        } catch (Exception e) {
            System.out.println("keyboard exception");
            etOther.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Istimeout_Sec = false;

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Entered Vehicle Other Info: " + etOther.getText());

                if (!etOther.getText().toString().trim().isEmpty()) {

                    boolean isascii = isAllASCII(etOther.getText().toString());

                    if (!isascii) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Please enter Valid String.");
                        CommonUtils.AutoCloseCustomMessageDilaog(AcceptVehicleOtherInfo.this, "Message", "Please enter Valid String.");
                    } else {
                        if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {

                            Constants.AccVehicleOther_FS1 = etOther.getText().toString().trim();
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
                            Constants.AccVehicleOther = etOther.getText().toString().trim();
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
                            Constants.AccVehicleOther_FS3 = etOther.getText().toString().trim();
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS4")) {
                            Constants.AccVehicleOther_FS4 = etOther.getText().toString().trim();
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS5")) {
                            Constants.AccVehicleOther_FS5 = etOther.getText().toString().trim();
                        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS6")) {
                            Constants.AccVehicleOther_FS6 = etOther.getText().toString().trim();
                        }

                        SharedPreferences sharedPrefODO = AcceptVehicleOtherInfo.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                        String IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
                        String IsHoursRequire = sharedPrefODO.getString(AppConstants.IsHoursRequire, "");
                        String IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
                        String IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
                        String IsPersonnelPINRequireForHub = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequireForHub, "");
                        String IsExtraOther = sharedPrefODO.getString(AppConstants.IsExtraOther, "");


                        if (IsPersonnelPINRequireForHub.equalsIgnoreCase("True")) {

                            Intent intent = new Intent(AcceptVehicleOtherInfo.this, AcceptPinActivity_new.class);//AcceptPinActivity
                            startActivity(intent);

                        } else if (IsDepartmentRequire.equalsIgnoreCase("True")) {

                            Intent intent = new Intent(AcceptVehicleOtherInfo.this, AcceptDeptActivity.class);
                            startActivity(intent);

                        } else if (IsOtherRequire.equalsIgnoreCase("True")) {

                            Intent intent = new Intent(AcceptVehicleOtherInfo.this, AcceptOtherActivity.class);
                            startActivity(intent);

                        } else {

                            AcceptServiceCall asc = new AcceptServiceCall();
                            asc.activity = AcceptVehicleOtherInfo.this;
                            asc.checkAllFields();
                        }
                    }
                } else {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + " Please enter Other, and try again.");
                    CommonUtils.showMessageDilaog(AcceptVehicleOtherInfo.this, "Error Message", getResources().getString(R.string.RequiredOther).replace("Other", OtherLabelVehicle));
                }

            }
        });

        etOther.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boolean ps = isKeyboardShown(etOther.getRootView());
                if (ps) {
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
                int InputTyp = etOther.getInputType();
                if (InputTyp == 3) {
                    etOther.setInputType(InputType.TYPE_CLASS_TEXT);
                    tv_swipekeybord.setText(getResources().getString(R.string.PressFor123));
                } else {

                    etOther.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_TEXT);
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

        // ActivityHandler.removeActivity(6);
        Istimeout_Sec = false;
        finish();
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

    public void hideKeybord() {

        InputMethodManager imm = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void showKeybord() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }
    private static boolean isAllASCII(String input) {
        boolean isASCII = true;
        for (int i = 0; i < input.length(); i++) {
            int c = input.charAt(i);
            if (c > 0x7F) {
                isASCII = false;
                break;
            }
        }
        return isASCII;
    }
}