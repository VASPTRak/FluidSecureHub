package com.TrakEngineering.FluidSecureHub;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.TrakEngineering.FluidSecureHub.entity.UserInfoEntity;
import com.squareup.picasso.Picasso;

public class FOBReaderActivity extends AppCompatActivity {


    private String TAG = " FOBReaderActivity ";
    private TextView textDateTime, tvCompanyName;
    private TextView tvTitle, support_phone, support_email;
    private ImageView FSlogo_img;
    public static String HubType = "", CompanyName = "", ScreenNameForPersonnel = "PERSON", ScreenNameForVehicle = "VEHICLE";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onResume() {
        super.onResume();

        //Hide keyboard
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    Button btnGoPer, btnGo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppConstants.GenerateLogs = true;
        SharedPreferences sharedPref = FOBReaderActivity.this.getSharedPreferences("LanguageSettings", Context.MODE_PRIVATE);
        String language = sharedPref.getString("language", "");
        CommonUtils.StoreLanguageSettings(FOBReaderActivity.this, language, false);

        setContentView(R.layout.activity_fobreader);

        SharedPreferences sharedPrefODO = this.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        HubType = sharedPrefODO.getString("HubType", "");
        CompanyName = sharedPrefODO.getString("CompanyName", "");

        SharedPreferences myPrefkb = this.getSharedPreferences(AppConstants.sharedPref_KeyboardType, 0);
        ScreenNameForVehicle = myPrefkb.getString("ScreenNameForVehicle", "Vehicle");
        ScreenNameForPersonnel = myPrefkb.getString("ScreenNameForPersonnel", "Person");

        // set User Information
        UserInfoEntity qaz = CommonUtils.getCustomerDetails(FOBReaderActivity.this);
        AppConstants.Title = getResources().getString(R.string.Name) + " : " + qaz.PersonName +
                "\n" + getResources().getString(R.string.Mobile) + " : " + qaz.PhoneNumber +
                "\n" + getResources().getString(R.string.Email) + " : " + qaz.PersonEmail;

        getSupportActionBar().setTitle("FOB");
       /* getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);*/

       /* SharedPreferences sharedBrd = FOBReaderActivity.this.getSharedPreferences("storeBranding", Context.MODE_PRIVATE);
        String CompanyBrandLogoLink = sharedBrd.getString("CompanyBrandLogoLink", "");
        Picasso.get().load(CompanyBrandLogoLink).into((ImageView) findViewById(R.id.imageView));*/

        Button btn_read_acessdevice = (Button) findViewById(R.id.btn_read_acessdevice);
        Button btn_disconnect = (Button) findViewById(R.id.btn_disconnect);
        TextView tvVersionNum = (TextView) findViewById(R.id.tvVersionNum);
        tvVersionNum.setText(getResources().getString(R.string.VersionHeading) + ": " + CommonUtils.getVersionCode(FOBReaderActivity.this));

        if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "UserInfo:\n" +AppConstants.Title + "\nAppVersion : " + CommonUtils.getVersionCode(FOBReaderActivity.this));

        InItGUI();

        btnGoPer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Go for personnel fob reading activity..
                Log.d(TAG, "personnel fob read selected");
                Intent i = new Intent(FOBReaderActivity.this, AcceptPinActivity_FOB.class);
                startActivity(i);
            }
        });

        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Go for Vehicle fob reading activity..
                Log.d(TAG, "vehicle fob read selected");
                Intent i = new Intent(FOBReaderActivity.this, AcceptVehicleActivity_FOB.class);
                startActivity(i);
            }
        });

        btn_read_acessdevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Fob read selected");
                Intent i = new Intent(FOBReaderActivity.this, ReadAccessDevice_Fob.class);
                startActivity(i);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.first, menu);//Menu Resource, Menu

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

        SharedPreferences sharedPref = FOBReaderActivity.this.getSharedPreferences("LanguageSettings", Context.MODE_PRIVATE);
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (item.getItemId()) {

            case R.id.mrestartapp:
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " Restart app.");
                Intent i = new Intent(FOBReaderActivity.this, SplashActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                break;

            case R.id.menuSpanish:
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "<Spanish language selected.>");
                CommonUtils.StoreLanguageSettings(FOBReaderActivity.this, "es", true);
                break;

            case R.id.menuEnglish:
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "<English language selected.>");
                CommonUtils.StoreLanguageSettings(FOBReaderActivity.this, "en", true);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void InItGUI() {

        textDateTime = (TextView) findViewById(R.id.textDateTime);
        btnGo = (Button) findViewById(R.id.btnGo);
        btnGoPer = (Button) findViewById(R.id.btnGoPer);

        // Display current date time u
        Thread myThread = null;
        Runnable myRunnableThread = new CountDownRunner(this, textDateTime);
        myThread = new Thread(myRunnableThread);
        myThread.start();

        // set User Information
        UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetails(FOBReaderActivity.this);

        AppConstants.Title = getResources().getString(R.string.HUBName) + " " + CommonUtils.getSpareHUBNumberByName(userInfoEntity.PersonName); //+ "\nMobile : " + userInfoEntity.PhoneNumber + "\nEmail : " + userInfoEntity.PersonEmail
        //AppConstants.HubName = userInfoEntity.PersonName;
        tvTitle = (TextView) findViewById(R.id.textView);
        tvTitle.setText(AppConstants.Title);

        tvCompanyName = (TextView) findViewById(R.id.tvCompanyName);
        tvCompanyName.setText(getResources().getString(R.string.CompanyName) + " " +  CompanyName);

        String btnGoText = getResources().getString(R.string.FobAssignButtonVehicle);
        btnGoText = btnGoText.replaceAll("Vehicle", ScreenNameForVehicle);
        btnGo.setText(btnGoText);
        /*btnGoText = btnGoText.replace("\n", "<br>");
        btnGoText = btnGoText.replaceAll("Vehicle", "<font color='#FFC0CB'>" + ScreenNameForVehicle + "</font>");
        btnGo.setText(Html.fromHtml(btnGoText, Html.FROM_HTML_MODE_LEGACY));*/

        String btnGoPerText = getResources().getString(R.string.FobAssignButtonPer);
        btnGoPerText = btnGoPerText.replaceAll("Person", ScreenNameForPersonnel);
        btnGoPer.setText(btnGoPerText);
        /*btnGoPerText = btnGoPerText.replace("\n", "<br>");
        btnGoPerText = btnGoPerText.replaceAll("Person", "<font color='#FFC0CB'>" + ScreenNameForPersonnel + "</font>");
        btnGoPer.setText(Html.fromHtml(btnGoPerText, Html.FROM_HTML_MODE_LEGACY));*/

        FSlogo_img = (ImageView) findViewById(R.id.FSlogo_img);
        FSlogo_img = (ImageView) findViewById(R.id.FSlogo_img);
        support_phone = (TextView) findViewById(R.id.support_phone);
        support_email = (TextView) findViewById(R.id.support_email);

        IsLogRequiredAndBranding();

    }


    @Override
    public void onBackPressed() {

        finish();
       /* Intent i = new Intent(FOBReaderActivity.this, WelcomeActivity.class);
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

    /*public static void storeLanguageSetLang(Activity activity, String lang, boolean isRecreate) {

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
    }*/

}