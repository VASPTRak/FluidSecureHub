package com.TrakEngineering.FluidSecureHub;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.TrakEngineering.FluidSecureHub.server.ServerHandler;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.TrakEngineering.FluidSecureHub.AppConstants.FluidSecureSiteName;
import static com.TrakEngineering.FluidSecureHub.AppConstants.ISVehicleHasFob;
import static com.TrakEngineering.FluidSecureHub.AppConstants.IsGateHub;
import static com.TrakEngineering.FluidSecureHub.AppConstants.IsPersonHasFob;
import static com.TrakEngineering.FluidSecureHub.AppConstants.IsVehicleNumberRequire;
import static com.TrakEngineering.FluidSecureHub.AppConstants.WifiChannelToUse;

public class RegistrationActivity extends AppCompatActivity {

    private Pattern EMAIL_PATTERN;
    private Pattern US_PHONE_PATTERN;
    EditText etFName, etMobile, etCompany;
    AutoCompleteTextView etEmail;
    Button btnSubmit;
    private static String TAG = RegistrationActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        // ----------------------------------------------------------------------------------------------
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        // ----------------------------------------------------------------------------------------------

        getSupportActionBar().setTitle("New HUB Registration");

        etFName = (EditText) findViewById(R.id.etFName);
        etMobile = (EditText) findViewById(R.id.etMobile);
        etCompany = (EditText) findViewById(R.id.etCompany);
        etEmail = (AutoCompleteTextView) findViewById(R.id.etEmail);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        TextView tvVersionNum = (TextView) findViewById(R.id.tvVersionNum);
        tvVersionNum.setText("Version " + CommonUtils.getVersionCode(RegistrationActivity.this));
        AppConstants.WriteinFile(TAG + " App Version: " + CommonUtils.getVersionCode(RegistrationActivity.this) + " " + AppConstants.getDeviceName() + " Android " + Build.VERSION.RELEASE + " ");

        try {
            TelephonyManager tMgr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            String mPhoneNumber = tMgr.getLine1Number();
            if (mPhoneNumber.trim().isEmpty()) {
                boolean isGranted = checkPermission(RegistrationActivity.this, Manifest.permission.READ_PHONE_STATE);
                AppConstants.WriteinFile(TAG + " phone permission: " + isGranted);
            }
            etMobile.setText(mPhoneNumber);
        }catch (Exception e) {
            AppConstants.WriteinFile(TAG + " Exception while getting phone number: " + e.getMessage());
            System.out.println(e.getMessage());
        }

        US_PHONE_PATTERN = Pattern.compile("^(?:(?:\\+?1\\s*(?:[.-]\\s*)?)?(?:\\(\\s*([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9])\\s*\\)|([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9]))\\s*(?:[.-]\\s*)?)?([2-9]1[02-9]|[2-9][02-9]1|[2-9][02-9]{2})\\s*(?:[.-]\\s*)?([0-9]{4})(?:\\s*(?:#|x\\.?|ext\\.?|extension)\\s*(\\d+))?$", Pattern.CASE_INSENSITIVE);

        EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$", Pattern.CASE_INSENSITIVE);
        Account[] accounts = AccountManager.get(this).getAccounts();
        Set<String> emailSet = new HashSet<String>();
        for (Account account : accounts) {
            if (EMAIL_PATTERN.matcher(account.name).matches()) {
                emailSet.add(account.name);
            }
        }
        etEmail.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>(emailSet)));






        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (etFName.getText().toString().trim().isEmpty()) {
                    redToast(RegistrationActivity.this, "Please enter Name");
                    etFName.requestFocus();
                }/* else if (etMobile.getText().toString().trim().isEmpty()) {
                    redToast(RegistrationActivity.this, "Please enter Mobile");
                    etMobile.requestFocus();
                } else if (!US_PHONE_PATTERN.matcher(etMobile.getText().toString().trim()).matches()) {

                    redToast(RegistrationActivity.this, "Please enter valid US contact number in\n(xxx)-xxx-xxxx or xxx-xxx-xxxx format.");
                    etMobile.requestFocus();
                } else if (!etMobile.getText().toString().trim().contains("-")) {
                    redToast(RegistrationActivity.this, "Please enter valid US contact number in \n(xxx)-xxx-xxxx or xxx-xxx-xxxx format.");
                    etMobile.requestFocus();

                } else if (etEmail.getText().toString().trim().isEmpty()) {
                    redToast(RegistrationActivity.this, "Please enter Email");
                    etEmail.requestFocus();
                } else if (!isValidEmail(etEmail.getText().toString().trim())) {
                    redToast(RegistrationActivity.this, "Invalid Email");
                    etEmail.requestFocus();
                } else if (etCompany.getText().toString().trim().isEmpty()) {
                    redToast(RegistrationActivity.this, "Please enter Company");
                    etCompany.requestFocus();
                }*/ else {

                    //------------Collect information for Registration------------------------------
                    //------------------------------------------------------------------------------
                    storeINFO(RegistrationActivity.this, etFName.getText().toString().trim(), etMobile.getText().toString().trim(), etEmail.getText().toString().trim(), AppConstants.getIMEI(RegistrationActivity.this));

                    String userName = etFName.getText().toString().trim();
                    String userMobile = etMobile.getText().toString().trim();
                    String userEmail = etEmail.getText().toString().trim();
                    String userCompany = etCompany.getText().toString().trim();
                    String imeiNumber;

                    if (Build.VERSION.SDK_INT >= 29) {
                        String uUUID = UUID.randomUUID().toString();
                        imeiNumber = uUUID;
                    } else {
                        imeiNumber = AppConstants.getIMEIOnlyForBelowOS10(RegistrationActivity.this);
                    }

                    SplashActivity.writeIMEI_UUIDInFile(RegistrationActivity.this, imeiNumber); ;

                    new RegisterUser(userName, userMobile, userEmail, imeiNumber, AppConstants.DEVICE_TYPE, userCompany).execute();


                    //------------------------------------------------------------------------------


                }
            }
        });
    }

    private boolean checkPermission(Activity context, String permission) {
        int result = ContextCompat.checkSelfPermission(context, permission);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static void redToast(Context ctx, String MSg) {
        Toast toast = Toast.makeText(ctx, " " + MSg + " ", Toast.LENGTH_LONG);
        toast.getView().setBackgroundColor(Color.RED);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }


    private boolean isValidEmail(String email) {
        //String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public void storeINFO(Context context, String name, String mobile, String email, String IMEInum) {
        SharedPreferences pref;

        SharedPreferences.Editor editor;
        pref = context.getSharedPreferences("storeINFO", 0);
        editor = pref.edit();


        // Storing
        editor.putString("name", name);
        editor.putString("mobile", mobile);
        editor.putString("email", email);
        editor.putString("IMEInum", IMEInum);

        editor.commit();


    }

    public void AlertDialogBox(final Context ctx, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {


                        dialog.dismiss();

                        finish();
                    }
                }

        );

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    public class RegisterUser extends AsyncTask<Void, Void, String> {

        private static final String TAG = "RegisterUser :";
        ProgressDialog pd;
        String userName;
        String userMobile;
        String userEmail;
        String imeiNumber;
        String deviceType;
        String userCompany;

        RegisterUser(String userName, String userMobile, String userEmail, String imeiNumber, String deviceType, String userCompany) {
            this.userName = userName;
            this.userMobile = userMobile;
            this.userEmail = userEmail;
            this.imeiNumber = imeiNumber;
            this.deviceType = deviceType;
            this.userCompany = userCompany;
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(RegistrationActivity.this);
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
            pd.show();

        }

        protected String doInBackground(Void... arg0) {
            String resp = "";

            try {

                // String sendData = userName + "#:#" + userMobile + "#:#" + userEmail + "#:#" + imeiNumber + "#:#" + deviceType + "#:#" + userCompany + "#:#" + "AP";
                String sendData = userName + "#:#" + userMobile + "#:#" + "" + "#:#" + imeiNumber + "#:#" + deviceType + "#:#" + "" + "#:#" + "AP";
                AppConstants.WriteinFile(TAG + " Registration details => (" + sendData + ")");
                String AUTH_TOKEN = "Basic " + AppConstants.convertStingToBase64("123:abc:Register");
                ServerHandler serverHandler = new ServerHandler();

                resp = serverHandler.PostTextData(RegistrationActivity.this, AppConstants.webURL, sendData, AUTH_TOKEN);


            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
            }

            return resp;
        }


        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            try {

                CommonUtils.LogMessage(TAG, " RegisterUser :" + result, new Exception("Test for All"));

                JSONObject jsonObj = new JSONObject(result);

                String ResponceMessage = jsonObj.getString(AppConstants.RES_MESSAGE);

                if (ResponceMessage.equalsIgnoreCase("success")) {
                    CommonUtils.SaveUserInPref(RegistrationActivity.this, userName, userMobile, userEmail, "", "", "",
                            "", "", "", "", "", "", FluidSecureSiteName, ISVehicleHasFob, IsPersonHasFob,
                            IsVehicleNumberRequire, Integer.parseInt(WifiChannelToUse), "", "", "", "",
                            "", "", "");

                    AlertDialogBox(RegistrationActivity.this, "Thank you for registering. \n\nYour request has been sent for the approval. You will be able to proceed with the application after your request has been approved by the administrator.");
                } else if (ResponceMessage.equalsIgnoreCase("fail")) {
                    String ResponseText = jsonObj.getString(AppConstants.RES_TEXT);

                    AppConstants.AlertDialogBox(RegistrationActivity.this, ResponseText);

                } else if (ResponceMessage.equalsIgnoreCase("exists")) {
                    AlertDialogBox(RegistrationActivity.this, "Your IMEI Number already EXISTS!");
                } else {
                    AlertDialogBox(RegistrationActivity.this, "Network Error");
                }


            } catch (Exception e) {

                CommonUtils.LogMessage(TAG, " RegisterUser :" + result, e);
            }

        }
    }



}