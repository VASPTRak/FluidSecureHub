package com.TrakEngineering.FluidSecureHub;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.TrakEngineering.FluidSecureHub.enity.UserInfoEntity;
import com.squareup.picasso.Picasso;

import java.util.Locale;

public class ActivitySparehub extends AppCompatActivity {


    private String TAG = " ActivitySparehub ";
    private TextView textDateTime;
    private TextView tvTitle, support_phone, support_email;
    private ImageView FSlogo_img;
    public static String HubType = "";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onResume() {
        super.onResume();

        //Hide keyboard
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppConstants.GenerateLogs = true;
        SharedPreferences sharedPref = ActivitySparehub.this.getSharedPreferences("storeLanguageSetLang", Context.MODE_PRIVATE);
        String lang = sharedPref.getString("lang", "");
        storeLanguageSetLang(ActivitySparehub.this, lang, false);
        setContentView(R.layout.activity_sparehub);

        SharedPreferences sharedPrefODO = this.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        HubType = sharedPrefODO.getString("HubType", "");

        // set User Information
        UserInfoEntity qaz = CommonUtils.getCustomerDetails(ActivitySparehub.this);
        AppConstants.Title = getResources().getString(R.string.Name) + " : " + qaz.PersonName +
                "\n" + getResources().getString(R.string.Mobile) + " : " + qaz.PhoneNumber +
                "\n" + getResources().getString(R.string.Email) + " : " + qaz.PersonEmail;

        getSupportActionBar().setTitle("FOB");
       /* getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);*/

       /* SharedPreferences sharedBrd = ActivitySparehub.this.getSharedPreferences("storeBranding", Context.MODE_PRIVATE);
        String CompanyBrandLogoLink = sharedBrd.getString("CompanyBrandLogoLink", "");
        Picasso.get().load(CompanyBrandLogoLink).into((ImageView) findViewById(R.id.imageView));*/

        Button btn_read_acessdevice = (Button) findViewById(R.id.btn_read_acessdevice);
        Button btn_disconnect = (Button) findViewById(R.id.btn_disconnect);
        TextView tvVersionNum = (TextView) findViewById(R.id.tvVersionNum);
        tvVersionNum.setText("Version " + CommonUtils.getVersionCode(ActivitySparehub.this));
        if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " UserInfo" +AppConstants.Title + "\n AppVersion " + CommonUtils.getVersionCode(ActivitySparehub.this));

        InItGUI();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.first, menu);//Menu Resource, Menu
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /*if (item.getItemId() == android.R.id.home) {
            Intent i = new Intent(ActivitySparehub.this, WelcomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }*/

       /* switch (item.getItemId()) {
            case R.id.menuClose:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    disconnectReader();
                }
                finish();
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }*/
        return super.onOptionsItemSelected(item);
    }


    private void InItGUI() {

        textDateTime = (TextView) findViewById(R.id.textDateTime);

        // Display current date time u
        Thread myThread = null;
        Runnable myRunnableThread = new CountDownRunner(this, textDateTime);
        myThread = new Thread(myRunnableThread);
        myThread.start();

        // set User Information
        UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetails(ActivitySparehub.this);

        AppConstants.Title = "Hub name : " + userInfoEntity.PersonName;//+ "\nMobile : " + userInfoEntity.PhoneNumber + "\nEmail : " + userInfoEntity.PersonEmail
        //AppConstants.HubName = userInfoEntity.PersonName;
        tvTitle = (TextView) findViewById(R.id.textView);
        tvTitle.setText(AppConstants.Title);
        FSlogo_img = (ImageView) findViewById(R.id.FSlogo_img);
        FSlogo_img = (ImageView) findViewById(R.id.FSlogo_img);
        support_phone = (TextView) findViewById(R.id.support_phone);
        support_email = (TextView) findViewById(R.id.support_email);

        IsLogRequiredAndBranding();

    }


    @Override
    public void onBackPressed() {

        finish();
       /* Intent i = new Intent(ActivitySparehub.this, WelcomeActivity.class);
        startActivity(i);*/

    }

    public void IsLogRequiredAndBranding() {

        SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_Log_Data, Context.MODE_PRIVATE);
        AppConstants.GenerateLogs = Boolean.parseBoolean(sharedPref.getString(AppConstants.LogRequiredFlag, "True"));
        String CompanyBrandName = sharedPref.getString(AppConstants.CompanyBrandName, "FluidSecure");
        String CompanyBrandLogoLink = sharedPref.getString(AppConstants.CompanyBrandLogoLink, "");
        String SupportEmail = sharedPref.getString(AppConstants.SupportEmail, "");
        String SupportPhonenumber = sharedPref.getString(AppConstants.SupportPhonenumber, "");

        AppConstants.BrandName = CompanyBrandName;
        support_email.setText(SupportEmail);
        support_phone.setText(SupportPhonenumber);

        getSupportActionBar().setTitle(AppConstants.BrandName);
        //getSupportActionBar().setIcon(R.drawable.fuel_secure_lock);

        if (!CompanyBrandLogoLink.equalsIgnoreCase("")) {
            Picasso.get().load(CompanyBrandLogoLink).into((ImageView) findViewById(R.id.FSlogo_img));
        }

    }

    public static void storeLanguageSetLang(Activity activity, String lang, boolean isRecreate) {

        if (lang.trim().equalsIgnoreCase("es"))
            AppConstants.LANG_PARAM = ":es-ES";
        else
            AppConstants.LANG_PARAM = ":en-US";

        Resources res = activity.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();

        if (lang.trim().equalsIgnoreCase("es"))
            conf.setLocale(new Locale("es"));
        else
            conf.setLocale(Locale.getDefault());

        res.updateConfiguration(conf, dm);


        SharedPreferences sharedPref = activity.getSharedPreferences("storeLanguageSetLang", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("lang", lang.trim());
        editor.apply();


        if (isRecreate)
            activity.recreate();
    }

}