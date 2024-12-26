package com.TrakEngineering.FluidSecureHub;

import android.graphics.Color;
import android.content.Context;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.TrakEngineering.FluidSecureHub.EddystoneScanner.EddystoneScannerService;
import com.TrakEngineering.FluidSecureHub.entity.FsnpInfo;
import com.TrakEngineering.FluidSecureHub.server.ServerHandler;
import com.google.gson.Gson;

import org.json.JSONObject;

public class AcceptManualOdoActivityFA extends AppCompatActivity {

    EditText editOdoManually;
    TextView tv_enter_odo;
    Button btnSave,cancelAction;
    ServerHandler serverHandler = new ServerHandler();
    String VehId = "",VehicleNumber = "",InstantIdMacAddress ="",FSNPMacAddress = "";
    private static final String TAG = "AccManualOdoActivityFA";
    ProgressDialog pd = null;
    String PreviousOdo = "", OdoLimit = "", OdometerReasonabilityConditions = "", CheckOdometerReasonable = "";
    String OdoMeter = "";
    public int cnt123 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_manual_odo_f);


        editOdoManually = (EditText)findViewById(R.id.editOdoManually);
        btnSave = (Button)findViewById(R.id.btnSave);
        cancelAction = (Button)findViewById(R.id.cancelAction);
        tv_enter_odo = (TextView) findViewById(R.id.tv_enter_odo);

        VehId = getIntent().getStringExtra("VehicleID");
        FSNPMacAddress = getIntent().getStringExtra("FSNPMacAddress");
        InstantIdMacAddress = getIntent().getStringExtra("InstantIdMacAddress");

        tv_enter_odo.setText("Enter Odometer Manually For Vehicle Number: "+VehicleNumber);

        SharedPreferences sharedPrefODO = AcceptManualOdoActivityFA.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        PreviousOdo = sharedPrefODO.getString("PreviousOdoForFA", "");
        OdoLimit = sharedPrefODO.getString("OdoLimitForFA", "");
        OdometerReasonabilityConditions = sharedPrefODO.getString("OdometerReasonabilityConditionsForFA", "");
        CheckOdometerReasonable = sharedPrefODO.getString("CheckOdometerReasonableForFA", "");


        cancelAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ClearOdometerScreenFlag();
                Intent i = new Intent(AcceptManualOdoActivityFA.this, WelcomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                OdoMeter = editOdoManually.getText().toString().trim();

             /*   if (!editOdoManually.getText().toString().trim().isEmpty()) {

                    //Server call
                    FsnpInfo objEntityClass = new FsnpInfo();
                    objEntityClass.IMEI_UDID = AppConstants.getIMEI(AcceptManualOdoActivityFA.this);
                    objEntityClass.Email = CommonUtils.getCustomerDetailsCC(AcceptManualOdoActivityFA.this).PersonEmail;
                    objEntityClass.VehicleId = VehId;//"B4:E6:2D:86:06:FB";//FSNPMacAddress;
                    objEntityClass.Odometer = OdoMeter;//"53:55:54:4E:4F:5A";//FSTagMacAddress;//

                    Gson gson = new Gson();
                    String jsonData = gson.toJson(objEntityClass);

                    String userEmail = CommonUtils.getCustomerDetailsCC(AcceptManualOdoActivityFA.this).PersonEmail;
                    String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(AcceptManualOdoActivityFA.this) + ":" + userEmail + ":" + "SaveManualVehicleOdometer");

                    System.out.println(TAG + "Response" + jsonData);
                    //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile(TAG +"Response" + jsonData);

                    try {
                        String serverRes = new SaveOdometerManually().execute(jsonData, authString).get();
                        ClearOdometerScreenFlag();
                        Intent i = new Intent(AcceptManualOdoActivityFA.this, WelcomeActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    CommonUtils.showMessageDilaog(AcceptManualOdoActivityFA.this, "Error Message", "Please enter Odometer");
                }*/

                //=======================================================================================

                if (!editOdoManually.getText().toString().trim().isEmpty()) {

                    int C_AccOdoMeter = Integer.parseInt(editOdoManually.getText().toString().trim());
                    CommonUtils.LogMessage(TAG, TAG + "Odo Entered : " + C_AccOdoMeter, null);
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile( TAG+" Odo Entered : " + C_AccOdoMeter);

                    //allValid();

                    int PO = Integer.parseInt(PreviousOdo.trim());
                    int OL = Integer.parseInt(OdoLimit.trim());

                    if (CheckOdometerReasonable.trim().toLowerCase().equalsIgnoreCase("true")) {

                        if (OdometerReasonabilityConditions.trim().equalsIgnoreCase("1")) {

                            if (C_AccOdoMeter >= PO && C_AccOdoMeter <= OL) {
                                //gooooo
                                allValid();
                            } else {
                                cnt123 += 1;

                                if (cnt123 > 3) {
                                    //gooooo
                                    allValid();
                                } else {

                                    if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile( TAG+" Odo Entered" + C_AccOdoMeter+" is not within the reasonability");
                                    editOdoManually.setText("");
                                    AppConstants.colorToastBigFont(AcceptManualOdoActivityFA.this, "The odometer entered is not in reasonability.", Color.BLUE);//Bad odometer! Please try again.
                                }
                            }

                        } else {


                            if (C_AccOdoMeter >= PO && C_AccOdoMeter <= OL) {
                                //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile( TAG+" Odo Entered" + C_AccOdoMeter);
                                ///gooooo
                                allValid();
                            } else {
                                editOdoManually.setText("");
                                if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile( TAG+" Odo Entered" + C_AccOdoMeter+" is not in the reasonability");
                                AppConstants.colorToastBigFont(AcceptManualOdoActivityFA.this, "The odometer entered is not in the reasonability", Color.BLUE);
                            }
                        }
                    } else {

                        //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile( TAG+" Odo Entered" + C_AccOdoMeter);
                        //comment By JB -it  must take ANY number they enter on the 4th try
                        allValid();
                    }


                } else {
                    CommonUtils.showMessageDilaog(AcceptManualOdoActivityFA.this, "Error Message", "Please enter odometer, and try again.");
                }
                //=======================================================================================
            }
        });


    }

    public void allValid(){

        if (EddystoneScannerService.CalledOnce != null) {
            EddystoneScannerService.CalledOnce.clear();
        }


        //Server call
        FsnpInfo objEntityClass = new FsnpInfo();
        objEntityClass.IMEI_UDID = AppConstants.getIMEI(AcceptManualOdoActivityFA.this);
        objEntityClass.Email = CommonUtils.getCustomerDetailsCC(AcceptManualOdoActivityFA.this).PersonEmail;
        objEntityClass.VehicleId = VehId;//"B4:E6:2D:86:06:FB";//FSNPMacAddress;
        objEntityClass.Odometer = OdoMeter;//"53:55:54:4E:4F:5A";//FSTagMacAddress;//

        Gson gson = new Gson();
        String jsonData = gson.toJson(objEntityClass);

        String userEmail = CommonUtils.getCustomerDetailsCC(AcceptManualOdoActivityFA.this).PersonEmail;
        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(AcceptManualOdoActivityFA.this) + ":" + userEmail + ":" + "SaveManualVehicleOdometer" + AppConstants.LANG_PARAM);

        System.out.println(TAG + "Response" + jsonData);
        //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile(TAG +"Response" + jsonData);
        try {
            String serverRes = new SaveOdometerManually().execute(jsonData, authString).get();

            ClearOdometerScreenFlag();

            Thread.sleep(1000);

            //Start same transaction again
            EddystoneScannerService obj = new EddystoneScannerService();
            obj.StartTransactionProcess(FSNPMacAddress, InstantIdMacAddress);

            Intent i = new Intent(AcceptManualOdoActivityFA.this, WelcomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void ClearOdometerScreenFlag(){

        Constants.MANUAL_ODO_SCREEN_FREE = "Yes";
        Constants.FS_1_ODO_SCREEN = "FREE";
        Constants.FS_2_ODO_SCREEN = "FREE";
        Constants.FS_3_ODO_SCREEN = "FREE";
        Constants.FS_4_ODO_SCREEN = "FREE";
        Constants.FS_5_ODO_SCREEN = "FREE";
        Constants.FS_6_ODO_SCREEN = "FREE";


    }

    public class SaveOdometerManually extends AsyncTask<String, Void, String> {


        String jsonData;
        String authString;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            String s= "Updating Odo meter please wait..";
            SpannableString ss2=  new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            pd = new ProgressDialog(AcceptManualOdoActivityFA.this);
            pd.setCancelable(true);
            pd.setMessage(ss2);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();

        }

        @Override
        protected String doInBackground(String... params) {

            String response = "";
            try {

                jsonData = params[0];
                authString = params[1];

                System.out.println("jsonData--" + jsonData);
                System.out.println("authString--" + authString);


                response = serverHandler.PostTextData(AcceptManualOdoActivityFA.this, AppConstants.WEB_URL, jsonData, authString);

                System.out.println("Id..." + jsonData);

            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile(TAG + "  SaveOdometerManually DoInBG Ex-"+e);

            }

            return response;
        }

        @Override
        protected void onPostExecute(String resp) {

            pd.dismiss();

            try {
                JSONObject jsonObj = new JSONObject(resp);
                String ResponceMessage = jsonObj.getString(AppConstants.RES_MESSAGE);
                String ResponceText = jsonObj.getString(AppConstants.RES_TEXT);
                Log.i(TAG ,"  SaveOdometerManually ResponceMessage:"+ResponceMessage+" ResponceText"+ResponceText);
                if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile(TAG + "  SaveOdometerManually ResponceMessage:"+ResponceMessage+" ResponceText"+ResponceText);



            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile(TAG + "  SaveOdometerManually OnPost Ex-");

            }
        }
    }
}
