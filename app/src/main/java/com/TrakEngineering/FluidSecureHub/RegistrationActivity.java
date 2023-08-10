package com.TrakEngineering.FluidSecureHub;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.TrakEngineering.FluidSecureHub.entity.ReplaceHUBFromAppEntity;
import com.TrakEngineering.FluidSecureHub.server.ServerHandler;
import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.TrakEngineering.FluidSecureHub.AppConstants.FluidSecureSiteName;
import static com.TrakEngineering.FluidSecureHub.AppConstants.ISVehicleHasFob;
import static com.TrakEngineering.FluidSecureHub.AppConstants.IsPersonHasFob;
import static com.TrakEngineering.FluidSecureHub.AppConstants.IsVehicleNumberRequire;
import static com.TrakEngineering.FluidSecureHub.AppConstants.WifiChannelToUse;

public class RegistrationActivity extends AppCompatActivity {

    private Pattern EMAIL_PATTERN;
    private Pattern US_PHONE_PATTERN;
    EditText etFName, etMobile, etCompany;
    AutoCompleteTextView etEmail;
    Button btnSubmit; //,btnReplaceHub;
    private static String TAG = RegistrationActivity.class.getSimpleName();
    private ConnectionDetector cd = new ConnectionDetector(RegistrationActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        SharedPreferences sharedPref = RegistrationActivity.this.getSharedPreferences("LanguageSettings", Context.MODE_PRIVATE);
        String language = sharedPref.getString("language", "");
        StoreLanguageSettings(language, false);

        // ----------------------------------------------------------------------------------------------
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        // ----------------------------------------------------------------------------------------------

        getSupportActionBar().setTitle(getResources().getString(R.string.NewHUBRegistration));

        etFName = (EditText) findViewById(R.id.etFName);
        etMobile = (EditText) findViewById(R.id.etMobile);
        etCompany = (EditText) findViewById(R.id.etCompany);
        etEmail = (AutoCompleteTextView) findViewById(R.id.etEmail);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        TextView tvVersionNum = (TextView) findViewById(R.id.tvVersionNum);
        tvVersionNum.setText(getResources().getString(R.string.VersionHeading) + ": " + CommonUtils.getVersionCode(RegistrationActivity.this));
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

        etFName.setFocusable(true);
        showKeybord();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (etFName.getText().toString().trim().isEmpty()) {
                    //redToast(RegistrationActivity.this, "Please enter HUB Name");
                    AppConstants.WriteinFile(TAG + " " + getResources().getString(R.string.HUBNameRequired));
                    CommonUtils.showMessageDilaog(RegistrationActivity.this, "Error Message", getResources().getString(R.string.HUBNameRequired));
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
                }*/
                else if (!ValidateHUBName(etFName.getText().toString().trim())) {
                    AppConstants.WriteinFile(TAG + " " + getResources().getString(R.string.HUBNameInvalid));
                    CommonUtils.showMessageDilaog(RegistrationActivity.this, "Error Message", getResources().getString(R.string.HUBNameInvalid));
                    etFName.requestFocus();
                } else {
                    AppConstants.WriteinFile(TAG + " Entered HUB Name: " + etFName.getText().toString());
                    String hubName = etFName.getText().toString().trim();
                    //------------Collect information for Registration------------------------------
                    //------------------------------------------------------------------------------
                    storeINFO(RegistrationActivity.this, hubName, etMobile.getText().toString().trim(), etEmail.getText().toString().trim(), AppConstants.getIMEI(RegistrationActivity.this));

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

                    SplashActivity.writeIMEI_UUIDInFile(RegistrationActivity.this, imeiNumber);

                    new RegisterUser(hubName, userMobile, userEmail, imeiNumber, AppConstants.DEVICE_TYPE, userCompany).execute();

                    //------------------------------------------------------------------------------

                }
            }
        });
    }

    private boolean ValidateHUBName(String hubName) {
        boolean isValid = false;
        try {
            String number;
            if (hubName.toUpperCase().startsWith("HUB")) {
                number = hubName.toUpperCase().replace("HUB", "");
            } else if (hubName.toUpperCase().startsWith("SPARE")) {
                number = hubName.toUpperCase().replace("SPARE", "");
            } else {
                number = hubName;
            }
            String regex = "[0-9]+";
            Pattern pattern = Pattern.compile(regex);
            Matcher m = pattern.matcher(number);

            isValid = m.matches();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isValid;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reader, menu);

        menu.findItem(R.id.mreboot_reader).setVisible(false);
        menu.findItem(R.id.mreconnect_ble_readers).setVisible(false);
        menu.findItem(R.id.mcamera_back).setVisible(false);
        menu.findItem(R.id.mcamera_front).setVisible(false);
        menu.findItem(R.id.mreload).setVisible(false);
        menu.findItem(R.id.btLinkScope).setVisible(false);
        menu.findItem(R.id.monline).setVisible(false);
        menu.findItem(R.id.mofline).setVisible(false);
        menu.findItem(R.id.mclose).setVisible(false);
        //menu.findItem(R.id.mconfigure_tld).setVisible(false);
        menu.findItem(R.id.enable_debug_window).setVisible(false);
        menu.findItem(R.id.madd_link).setVisible(false);
        menu.findItem(R.id.mshow_reader_status).setVisible(false);
        menu.findItem(R.id.mupgrade_normal_link).setVisible(false);
        menu.findItem(R.id.testTransaction).setVisible(false);

        SharedPreferences sharedPref = RegistrationActivity.this.getSharedPreferences("LanguageSettings", Context.MODE_PRIVATE);
        String language = sharedPref.getString("language", "");

        MenuItem itemSp = menu.findItem(R.id.menuSpanish);
        MenuItem itemEng = menu.findItem(R.id.menuEnglish);

        if (language.trim().equalsIgnoreCase("es")) {
            itemSp.setVisible(false);
            itemEng.setVisible(true);
        } else {
            itemSp.setVisible(true);
            itemEng.setVisible(false);
        }
        // Comment below code when uncomment above code
        /*MenuItem itemSp = menu.findItem(R.id.menuSpanish);
        MenuItem itemEng = menu.findItem(R.id.menuEnglish);
        itemSp.setVisible(false);
        itemEng.setVisible(false);*/

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (item.getItemId()) {

            case R.id.mrestartapp:
                AppConstants.WriteinFile(TAG + " Restart app.");
                Intent i = new Intent(RegistrationActivity.this, SplashActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                break;

            case R.id.menuSpanish:
                AppConstants.WriteinFile(TAG + " <Spanish language selected.>");
                StoreLanguageSettings("es", true);
                break;

            case R.id.menuEnglish:
                AppConstants.WriteinFile(TAG + " <English language selected.>");
                StoreLanguageSettings("en", true);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void StoreLanguageSettings(String language, boolean isRecreate) {
        try {
            if (language.trim().equalsIgnoreCase("es"))
                AppConstants.LANG_PARAM = ":es-ES";
            else
                AppConstants.LANG_PARAM = ":en-US";

            DisplayMetrics dm = getBaseContext().getResources().getDisplayMetrics();
            Configuration conf = getBaseContext().getResources().getConfiguration();

            if (language.trim().equalsIgnoreCase("es")) {
                conf.setLocale(new Locale("es"));
            } else if (language.trim().equalsIgnoreCase("en")) {
                conf.setLocale(new Locale("en", "US"));
            } else {
                conf.setLocale(Locale.getDefault());
            }

            getBaseContext().getResources().updateConfiguration(conf, dm);

            SharedPreferences sharedPref = this.getSharedPreferences("LanguageSettings", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("language", language.trim());
            editor.apply();

            if (isRecreate) {
                //recreate();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "<Restarting the activity.>");
                Intent i = new Intent(RegistrationActivity.this, RegistrationActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                this.startActivity(i);
            }
        } catch (Exception e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Exception occurred in StoreLanguageSettings: " + e.getMessage());
        }
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
        toast.getView().setBackgroundColor(Color.BLUE); // Changed RED to BLUE because Eva doesn't want any red message
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

    public void AlertDialogBox(final Context ctx, String message, boolean isRestart) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {

                        dialog.dismiss();

                        if (isRestart) {
                            AppConstants.WriteinFile(TAG + "<Restarting the app after registration.>");
                            Intent i = new Intent(RegistrationActivity.this, SplashActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        } else {
                            finish();
                        }
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
            pd.setMessage(getResources().getString(R.string.PleaseWait));
            pd.setCancelable(false);
            pd.show();

        }

        protected String doInBackground(Void... arg0) {
            String resp = "";

            try {

                // String sendData = userName + "#:#" + userMobile + "#:#" + userEmail + "#:#" + imeiNumber + "#:#" + deviceType + "#:#" + userCompany + "#:#" + "AP";
                String sendData = userName + "#:#" + userMobile + "#:#" + "" + "#:#" + imeiNumber + "#:#" + deviceType + "#:#" + "" + "#:#" + "AP";
                AppConstants.WriteinFile(TAG + " Registration details => (" + sendData + ")");
                String AUTH_TOKEN = "Basic " + AppConstants.convertStingToBase64("123:abc:Register" + AppConstants.LANG_PARAM);
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

                    Log.i(TAG, " Clearing previous offline data after new registration.");
                    CommonUtils.ClearOfflineData(RegistrationActivity.this); // To clear offline data of Links, Vehicle, Personnel and Department.
                    AppConstants.clearSharedPrefByName(RegistrationActivity.this, "OfflineData");
                    AppConstants.WriteinFile(TAG + " Registration successful. Thank you for registering.");
                    AlertDialogBox(RegistrationActivity.this, getResources().getString(R.string.RegistrationSuccess), true);
                } else if (ResponceMessage.equalsIgnoreCase("fail")) {
                    String ResponseText = jsonObj.getString(AppConstants.RES_TEXT);
                    String ValidationFailFor = jsonObj.getString(AppConstants.VALIDATION_FOR_TEXT);

                    if (ValidationFailFor.equalsIgnoreCase("askreplacehub")) {
                        AppConstants.WriteinFile(TAG + " Registration fail: Asking for Replacement");
                        CustomMessage2Input(RegistrationActivity.this, "", getString(R.string.askreplacehub));
                    } else {
                        AppConstants.WriteinFile(TAG + " Registration fail: " + ResponseText);
                        AppConstants.AlertDialogBox(RegistrationActivity.this, ResponseText);
                    }

                } else if (ResponceMessage.equalsIgnoreCase("exists")) {
                    AppConstants.WriteinFile(TAG + " " + getResources().getString(R.string.IMEIAlreadyExist));
                    AlertDialogBox(RegistrationActivity.this, getResources().getString(R.string.IMEIAlreadyExist), false);
                } else {
                    AppConstants.WriteinFile(TAG + " " + getResources().getString(R.string.CheckInternet));
                    AlertDialogBox(RegistrationActivity.this, getResources().getString(R.string.CheckInternet), false);
                }

            } catch (Exception e) {
                AppConstants.WriteinFile(TAG + " RegisterUser Exception: " + e.getMessage());
                CommonUtils.LogMessage(TAG, " RegisterUser :" + result, e);
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void CustomMessage2Input(final Activity context, String title, String message) {

        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                        AppConstants.WriteinFile(TAG + " Replacement confirmation -> Yes");

                        // HUB Replace server call
                        String hubName = etFName.getText().toString().trim();
                        String userName = "";
                        String userPass = "";
                        String imeiNumber = AppConstants.getIMEI(RegistrationActivity.this);
                        String userMobile = etMobile.getText().toString().trim();

                        if (imeiNumber.isEmpty()) {
                            AppConstants.WriteinFile(TAG + " Your IMEI Number is Empty!");
                            AlertDialogBox(RegistrationActivity.this, "Your IMEI Number is Empty!", false);
                        } else if (cd.isConnectingToInternet()) {
                            new ReplaceHUBFromApp().execute(hubName, imeiNumber, userName, userPass, userMobile);
                        } else {
                            CommonUtils.showNoInternetDialog(RegistrationActivity.this);
                        }
                    }
                }
        );

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                        AppConstants.WriteinFile(TAG + " Replacement confirmation -> No");

                        InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }
                }
        );
        androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public class ReplaceHUBFromApp extends AsyncTask<String, Void, String> {

        private static final String TAG = "ReplaceHUBFromApp :";
        public String resp = "";
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(RegistrationActivity.this);
            pd.setMessage(getResources().getString(R.string.PleaseWait));
            pd.setCancelable(false);
            pd.show();

        }

        protected String doInBackground(String... param) {

            try {
                MediaType TEXT = MediaType.parse("application/x-www-form-urlencoded");

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(4, TimeUnit.SECONDS);
                client.setReadTimeout(4, TimeUnit.SECONDS);
                client.setWriteTimeout(4, TimeUnit.SECONDS);

                ReplaceHUBFromAppEntity objEntityClass = new ReplaceHUBFromAppEntity();
                objEntityClass.hubName = param[0];
                objEntityClass.deviceId = param[1];
                objEntityClass.userName = param[2];
                objEntityClass.password = param[3];
                objEntityClass.devicePhoneNumber = param[4];

                Gson gson = new Gson();
                String jsonData = gson.toJson(objEntityClass);
                String userEmail = CommonUtils.getCustomerDetails(RegistrationActivity.this).PersonEmail;

                String authString = "Basic " + AppConstants.convertStingToBase64(objEntityClass.deviceId + ":" + userEmail + ":" + "ReplaceHUBFromApp" + AppConstants.LANG_PARAM);

                RequestBody body = RequestBody.create(TEXT, jsonData);
                Request request = new Request.Builder()
                        .url(AppConstants.webURL)
                        .post(body)
                        .addHeader("Authorization", authString)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (SocketTimeoutException e) {
                e.printStackTrace();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            try {

                JSONObject jsonObj = new JSONObject(result);
                String ResponceMessage = jsonObj.getString("ResponseMessage");

                if (ResponceMessage.equalsIgnoreCase("success")) {
                    //CommonUtils.SaveUserInPref(RegistrationActivity.this, userName, userMobile, userEmail, "","","","","", "", "","","", FluidSecureSiteName,ISVehicleHasFob, IsPersonHasFob,IsVehicleNumberRequire, Integer.parseInt(WifiChannelToUse),"","","");
                    AppConstants.WriteinFile(TAG + " Replacement successful."); // Thank you for registering.
                    AlertDialogBox(RegistrationActivity.this, getResources().getString(R.string.ReplacementSuccess), true);
                } else if (ResponceMessage.equalsIgnoreCase("fail")) {
                    String ResponseText = jsonObj.getString("ResponseText");
                    AppConstants.WriteinFile(TAG + " Replacement fail: " + ResponseText);
                    AppConstants.AlertDialogBox(RegistrationActivity.this, ResponseText);
                } else if (ResponceMessage.equalsIgnoreCase("exists")) {
                    AppConstants.WriteinFile(TAG + " " + getResources().getString(R.string.IMEIAlreadyExist));
                    AlertDialogBox(RegistrationActivity.this, getResources().getString(R.string.IMEIAlreadyExist), false);
                } else {
                    AppConstants.WriteinFile(TAG + " " + getResources().getString(R.string.CheckInternet));
                    AlertDialogBox(RegistrationActivity.this, getResources().getString(R.string.CheckInternet), false);
                }

            } catch (Exception e) {
                CommonUtils.LogMessage(TAG, " ReplaceHUBFromApp Exception:" + result, e);
            }


        }
    }

    public void hideKeybord() {

        InputMethodManager imm = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void showKeybord() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }



}