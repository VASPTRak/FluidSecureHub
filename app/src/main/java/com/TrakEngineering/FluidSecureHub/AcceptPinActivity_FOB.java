package com.TrakEngineering.FluidSecureHub;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.TrakEngineering.FluidSecureHub.HFCardGAtt.ServiceHFCard;
import com.TrakEngineering.FluidSecureHub.LFCardGAtt.ServiceLFCard;
import com.TrakEngineering.FluidSecureHub.MagCardGAtt.ServiceMagCard;
import com.TrakEngineering.FluidSecureHub.enity.CheckPinFobEntity;
import com.TrakEngineering.FluidSecureHub.enity.VehicleRequireEntity;
import com.TrakEngineering.FluidSecureHub.server.ServerHandler;
import com.example.barcodeml.LivePreviewActivity;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AcceptPinActivity_FOB extends AppCompatActivity {


    private final String TAG = AcceptPinActivity_FOB.class.getSimpleName();
    public AcceptPinActivity_FOB.BroadcastMagCard_dataFromServiceToUI ServiceCardReader_vehicle = null;
    private String mDeviceName;
    private String mDisableFOBReadingForVehicle;
    private String mDeviceAddress;
    private String mMagCardDeviceName;
    private String mMagCardDeviceAddress;
    private String mDeviceName_hf_trak, QRCodeReaderForBarcode, QRCodeBluetoothMacAddressForBarcode;
    private String mDeviceAddress_hf_trak;
    private String HFDeviceName;
    private String HFDeviceAddress;
    private String FobKey = "";
    private String MagCard_FobKey = "";
    private String Barcode_val = "",ScreenNameForPersonnel = "Personnel", ScreenNameForVehicle = "Vehicle", KeyboardType = "2";
    private static Timer t;
    List<Timer> Timerlist = new ArrayList<Timer>();
    Button btnScanForBarcode,btnSave,btnCancel,btnAccessDevice;
    private static final int RC_BARCODE_CAPTURE = 1;
    private TextView tv_Display_msg,tv_pin_no_below,tv_return, tv_swipekeybord;
    private EditText editpinNumber;
    RelativeLayout footer_keybord;
    ConnectionDetector cd = new ConnectionDetector(AcceptPinActivity_FOB.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_pin_fob);

        SharedPreferences myPrefkb = this.getSharedPreferences(AppConstants.sharedPref_KeyboardType, 0);
        KeyboardType = myPrefkb.getString("KeyboardTypePerson", "2");
        ScreenNameForPersonnel = myPrefkb.getString("ScreenNameForPersonnel", "Personnel");
        ScreenNameForVehicle = myPrefkb.getString("ScreenNameForVehicle", "Vehicle");

        SharedPreferences sharedPre2 = AcceptPinActivity_FOB.this.getSharedPreferences("storeBT_FOBDetails", Context.MODE_PRIVATE);

        mDisableFOBReadingForVehicle = sharedPre2.getString("DisableFOBReadingForVehicle", "");
        mDeviceName = sharedPre2.getString("LFBluetoothCardReader", "");
        mDeviceAddress = sharedPre2.getString("LFBluetoothCardReaderMacAddress", "");
        HFDeviceName = sharedPre2.getString("BluetoothCardReader", "");
        HFDeviceAddress = sharedPre2.getString("BTMacAddress", "");
        mDeviceName_hf_trak = sharedPre2.getString("HFTrakCardReader", ""); //
        mDeviceAddress_hf_trak = sharedPre2.getString("HFTrakCardReaderMacAddress", ""); //
        AppConstants.ACS_READER = sharedPre2.getBoolean("ACS_Reader", false);
        mMagCardDeviceName = sharedPre2.getString("MagneticCardReader", ""); //
        mMagCardDeviceAddress = sharedPre2.getString("MagneticCardReaderMacAddress", ""); //
        QRCodeReaderForBarcode = sharedPre2.getString("QRCodeReaderForBarcode", ""); //
        QRCodeBluetoothMacAddressForBarcode = sharedPre2.getString("QRCodeBluetoothMacAddressForBarcode", ""); //

        InitUi();


        editpinNumber.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boolean ps = isKeyboardShown(editpinNumber.getRootView());
                if (ps == true) {
                    footer_keybord.setEnabled(true);
                    footer_keybord.setVisibility(View.VISIBLE);
                } else {
                    footer_keybord.setEnabled(false);
                    footer_keybord.setVisibility(View.INVISIBLE);
                }

            }
        });

        try {
            editpinNumber.setInputType(Integer.parseInt(KeyboardType));
        } catch (Exception e) {
            System.out.println("keyboard exception");
            editpinNumber.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        btnScanForBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AcceptPinActivity_FOB.this, LivePreviewActivity.class);
                startActivityForResult(intent, RC_BARCODE_CAPTURE);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ValidateData()){
                    //save to cloud call
                    CheckPinFobEntity objEntityClass = new CheckPinFobEntity();
                    objEntityClass.IMEIUDID = AppConstants.getIMEI(AcceptPinActivity_FOB.this);
                    objEntityClass.PersonnelPIN = editpinNumber.getText().toString().trim();
                    objEntityClass.FOBNumber = FobKey;
                    objEntityClass.MagneticCardNumber = MagCard_FobKey;
                    objEntityClass.Barcode = Barcode_val;
                    objEntityClass.ReplaceAccessDevice = "n";

                    AppConstants.AddPin = editpinNumber.getText().toString().trim();
                    AppConstants.AddFobKey = FobKey;
                    AppConstants.AddMagCard_FobKey = MagCard_FobKey;
                    AppConstants.AddBarcode_val = Barcode_val;

                    if (cd.isConnectingToInternet()){
                        new CheckPersonFobOnly(objEntityClass).execute();
                    }else{
                        CommonUtils.showNoInternetDialog(AcceptPinActivity_FOB.this);
                    }

                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AcceptPinActivity_FOB.this, FOBReaderActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        tv_swipekeybord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int InputTyp = editpinNumber.getInputType();
                if (InputTyp == 2) {
                    editpinNumber.setInputType(InputType.TYPE_CLASS_TEXT);
                    tv_swipekeybord.setText("Press for 123");
                } else {

                    editpinNumber.setInputType(InputType.TYPE_CLASS_NUMBER);//| InputType.TYPE_CLASS_TEXT
                    tv_swipekeybord.setText("Press for ABC");
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
    protected void onResume() {
        super.onResume();

        //InitScreen();
        editpinNumber.setText("");
        if (Barcode_val.isEmpty()){
            InitScreen();
        }
        resetReaderStatus();//BLE reader status reset
        RegisterBroadcastForReader();//BroadcastReciver for MagCard,HF and LF Readers

        //fob_number.setText("Access Device No: ");
        AppConstants.PinLocal_FOB_KEY = "";
        t = new Timer();
        Timerlist.add(t);
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {

                if (!AppConstants.PinLocal_FOB_KEY.equalsIgnoreCase("")) {

                    //CancelTimer();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            FobreadSuccess();
                        }
                    });
                }
            }

        };
        t.schedule(tt, 1000, 1000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(AcceptPinActivity_FOB.this, FOBReaderActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppConstants.PinLocal_FOB_KEY = "";
        CancelTimer();
        UnRegisterBroadcastForReader();
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppConstants.PinLocal_FOB_KEY = "";
        CancelTimer();
    }

    public void FobreadSuccess(){

        Log.i(TAG,"FobreadSuccess");
        AppConstants.PinLocal_FOB_KEY = "";
        if (MagCard_FobKey != null && !MagCard_FobKey.isEmpty()) {
            Log.i(TAG,"FobreadSuccess MagCard_FobKey"+MagCard_FobKey);
        }else if (FobKey != null && !FobKey.isEmpty()){
            Log.i(TAG,"FobreadSuccess FobKey"+FobKey);
        }else {
            //AppConstants.colorToastBigFont(getApplicationContext(), "Access Device not found", Color.RED);
        }
    }

    public void resetReaderStatus() {

        Constants.QR_ReaderStatus = "QR Waiting..";
        Constants.HF_ReaderStatus = "HF Waiting..";
        Constants.LF_ReaderStatus = "LF Waiting..";
        Constants.Mag_ReaderStatus = "Mag Waiting..";

    }

    private void CancelTimer() {

        for (int i = 0; i < Timerlist.size(); i++) {
            Timerlist.get(i).cancel();
        }

    }

    private void UnRegisterBroadcastForReader() {

        try {

            if (ServiceCardReader_vehicle != null)
                unregisterReceiver(ServiceCardReader_vehicle);
            ServiceCardReader_vehicle = null;

            if (mDeviceName.length() > 0 && !mDeviceAddress.isEmpty() && mDisableFOBReadingForVehicle.equalsIgnoreCase("N"))
                stopService(new Intent(AcceptPinActivity_FOB.this, ServiceLFCard.class));

            if (HFDeviceName.length() > 0 && !HFDeviceAddress.isEmpty() && !AppConstants.ACS_READER && mDisableFOBReadingForVehicle.equalsIgnoreCase("N"))
                stopService(new Intent(AcceptPinActivity_FOB.this, ServiceHFCard.class));

            if (mMagCardDeviceAddress.length() > 0 && !mMagCardDeviceAddress.isEmpty() && mDisableFOBReadingForVehicle.equalsIgnoreCase("N"))
                stopService(new Intent(AcceptPinActivity_FOB.this, ServiceMagCard.class));


        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "UnRegisterBroadcastForReader Exception:" + e.toString());
        }


    }

    private void RegisterBroadcastForReader() {

        try {
            if (ServiceCardReader_vehicle == null) {

                ServiceCardReader_vehicle = new AcceptPinActivity_FOB.BroadcastMagCard_dataFromServiceToUI();
                IntentFilter intentSFilterVEHICLE = new IntentFilter("ServiceToActivityMagCard");
                registerReceiver(ServiceCardReader_vehicle, intentSFilterVEHICLE);

                if (mDeviceName.length() > 0 && !mDeviceAddress.isEmpty() && mDisableFOBReadingForVehicle.equalsIgnoreCase("N")){
                    if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " Register LFReader:"+mDeviceAddress);
                    startService(new Intent(AcceptPinActivity_FOB.this, ServiceLFCard.class));
                }

                if (HFDeviceName.length() > 0 && !HFDeviceAddress.isEmpty() && !AppConstants.ACS_READER && mDisableFOBReadingForVehicle.equalsIgnoreCase("N")){
                    startService(new Intent(AcceptPinActivity_FOB.this, ServiceHFCard.class));
                    if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " Register HFReader:"+HFDeviceAddress);
                }

                if (mMagCardDeviceAddress.length() > 0 && !mMagCardDeviceAddress.isEmpty() && mDisableFOBReadingForVehicle.equalsIgnoreCase("N")) {
                    startService(new Intent(AcceptPinActivity_FOB.this, com.TrakEngineering.FluidSecureHub.MagCardGAtt.ServiceMagCard.class));
                    if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " Register MagReader:"+mMagCardDeviceAddress);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class BroadcastMagCard_dataFromServiceToUI extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle notificationData = intent.getExtras();

            try {

                String Action = notificationData.getString("Action");
                if (Action.equals("HFReader")) {

                    String newData = notificationData.getString("HFCardValue");
                    System.out.println("HFCard data 001 veh----" + newData);
                    if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " " + Action + " Raw data:" + newData);
                    displayData_FOB(newData);

                } else if (Action.equals("LFReader")) {

                    String newData = notificationData.getString("LFCardValue");
                    System.out.println("LFCard data 001 veH----" + newData);
                    if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " " + Action + " Raw data:" + newData);
                    displayData_FOB(newData);

                } else if (Action.equals("MagReader")) {

                    String newData = notificationData.getString("MagCardValue");
                    System.out.println("MagCard data 002~----" + newData);
                    if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " " + Action + " Raw data:" + newData);
                    MagCard_FobKey = "";
                    displayData_MagCard(newData);

                } else if (Action.equals("QRReader")) {
                    String newData = notificationData.getString("QRCodeValue");
                    System.out.println("QRCode data 002~----" + newData);
                    if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " " + Action + " Raw data:" + newData);
                    //Barcode_val = "";
                    if (newData != null) {
                        //Barcode_val = newData.trim();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void displayData_FOB(String data) {

        //print raw reader data in log file
        //if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + "  BroadcastReceiver HF displayData_HF " + data);

        if (data != null && !data.isEmpty()) {

            String Str_data = data.toString().trim();
            Log.i(TAG, "Response Fob:" + Str_data);
            // if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + "Response HF: " + Str_data);
            String Str_check = Str_data.replace(" ", "");

            if (Str_check.contains("FFFFFFFFFFFFFFFFFFFF") || Str_check.contains("FF FF FF FF FF FF FF FF FF FF")) {

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Unable to read fob: " + Str_check);
                CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_FOB.this, "Message", "Unable to read fob.  Please Try again..");
                if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + "Unable to read fob.  Please Try again..");

            } else if (CommonUtils.ValidateFobkey(Str_check) && Str_check.length() > 4) {

                try {

                    if (Str_check.contains("\n")) {

                        String last_val = "";
                        String[] Seperate = Str_data.split("\n");
                        if (Seperate.length > 1) {
                            last_val = Seperate[Seperate.length - 1];
                        }
                        FobKey = last_val.replaceAll("\\s", "");

                    } else {

                        FobKey = Str_data.replaceAll("\\s", "");
                    }

                    if (!FobKey.equalsIgnoreCase("") && FobKey.length() > 5) {
                        //  tv_fob_number.setText("Access Device No: " + FobKey);
                        AppConstants.PinLocal_FOB_KEY = FobKey;
                        Log.i(TAG, "pin FOB:" +  AppConstants.PinLocal_FOB_KEY);
                        //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Local_HF_KEY" + AppConstants.PinLocal_FOB_KEY);
                        tv_Display_msg.setText(R.string.AccessDeviceReadSuccess);
                        //SuccessUi(); //Commented as per #1818 > #6

                        // Recognize Vehicle OR Personnel Access Device
                        VehicleRequireEntity objEntityClass = new VehicleRequireEntity();
                        objEntityClass.IMEIUDID = AppConstants.getIMEI(AcceptPinActivity_FOB.this);
                        objEntityClass.FOBNumber = FobKey;
                        objEntityClass.MagneticCardNumber = "";
                        objEntityClass.Barcode = "";

                        if (cd.isConnectingToInternet()){
                            new RecognizeVehicleORPersonnelAccessDevice(objEntityClass).execute();
                        }else{
                            AppConstants.WriteinFile(TAG + "No Internet please check.");
                            CommonUtils.showNoInternetDialog(AcceptPinActivity_FOB.this);
                        }
                        ///==================================================
                    }

                } catch (Exception ex) {
                    System.out.println(ex);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "  displayData  --Exception " + ex);
                }

            }
        }
    }

    private void displayData_MagCard(String data) {

        //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " displayData_MagCard " + data);

        if (data != null && !data.isEmpty()) {

            String Str_data = data.toString().trim();
            Log.i(TAG, "displayData MagCard:" + Str_data);
            //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  displayData MagCard: " + Str_data);

            String Str_check = Str_data.replace(" ", "");
            if (!CommonUtils.ValidateFobkey(Str_check) || Str_data.contains("FFFFFFFFFFFFFFFFFFFF") || Str_data.contains("FF FF FF FF FF FF FF FF FF FF")) {

                MagCard_FobKey = "";
                CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_FOB.this, "Message", "Unable to read MagCard.  Please Try again..");
                if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + "Unable to read fob.  Please Try again..");

            } else if (Str_check.length() > 5) {

                try {

                    MagCard_FobKey = Str_check;
                    //tv_fobkey.setText(Str_check.replace(" ", ""));
                    //  tv_fob_number.setText("Access Device No: " + MagCard_FobKey);
                    AppConstants.VehicleLocal_FOB_KEY = MagCard_FobKey;
                    //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Local_MagCard_KEY" + AppConstants.VehicleLocal_FOB_KEY);
                    //On Magnatic Fob read success
                    tv_Display_msg.setText(R.string.AccessDeviceReadSuccess);
                    //SuccessUi(); //Commented as per #1818 > #6

                    // Recognize Vehicle OR Personnel Access Device
                    VehicleRequireEntity objEntityClass = new VehicleRequireEntity();
                    objEntityClass.IMEIUDID = AppConstants.getIMEI(AcceptPinActivity_FOB.this);
                    objEntityClass.FOBNumber = "";
                    objEntityClass.MagneticCardNumber = MagCard_FobKey;
                    objEntityClass.Barcode = "";

                    if (cd.isConnectingToInternet()){
                        new RecognizeVehicleORPersonnelAccessDevice(objEntityClass).execute();
                    }else{
                        AppConstants.WriteinFile(TAG + "No Internet please check.");
                        CommonUtils.showNoInternetDialog(AcceptPinActivity_FOB.this);
                    }
                    ///==================================================

                } catch (Exception ex) {
                    MagCard_FobKey = "";
                    System.out.println(ex);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "  displayData Split MagCard  --Exception " + ex);
                }

            }

        } else {
            MagCard_FobKey = "";
        }
    }

    private class ReconnectBleReaders extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {

            try {

                UnRegisterBroadcastForReader();

                Thread.sleep(2000);

                RegisterBroadcastForReader();


            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.ServerCallLogs)
                    AppConstants.WriteinFile(TAG + " ReconnectBleReaders Exception: " + e.toString());
            }

            return null;
        }
    }

    private void InitUi(){

        btnScanForBarcode = (Button) findViewById(R.id.btnScanForBarcode);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnAccessDevice = (Button) findViewById(R.id.btnAccessDevice);
        editpinNumber = (EditText) findViewById(R.id.editpinNumber);
        tv_Display_msg = (TextView) findViewById(R.id.tv_Display_msg);
        tv_pin_no_below = (TextView) findViewById(R.id.tv_pin_no_below);
        tv_return = (TextView) findViewById(R.id.tv_return);
        tv_swipekeybord = (TextView) findViewById(R.id.tv_swipekeybord);
        footer_keybord = (RelativeLayout) findViewById(R.id.footer_keybord);

        String btnAccessDeviceText = getResources().getString(R.string.BtnReadAccessDeviceP);
        btnAccessDeviceText = btnAccessDeviceText.replaceAll("Personnel", ScreenNameForPersonnel);
        btnAccessDevice.setText(btnAccessDeviceText);

    }

    private void SuccessUi(){

        tv_pin_no_below.setVisibility(View.VISIBLE);
        editpinNumber.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.VISIBLE);
        btnAccessDevice.setVisibility(View.INVISIBLE);
        tv_pin_no_below.setText("Enter " + ScreenNameForPersonnel + " PIN that was assigned to this user in the Cloud");

        InputMethodManager inputMethodManager = (InputMethodManager) editpinNumber.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        editpinNumber.requestFocus();
        inputMethodManager.showSoftInput(editpinNumber, 0);


    }

    private void InitScreen(){

        editpinNumber.setText("");
        editpinNumber.setText("");
        tv_Display_msg.setText("");
        FobKey = "";
        MagCard_FobKey = "";
        Barcode_val = "";
        tv_pin_no_below.setVisibility(View.INVISIBLE);
        editpinNumber.setVisibility(View.INVISIBLE);
        btnSave.setVisibility(View.INVISIBLE);
        btnAccessDevice.setVisibility(View.VISIBLE);

    }

    private boolean ValidateData(){

        if (editpinNumber.getText().toString().isEmpty()){
            editpinNumber.setError("Enter " + ScreenNameForPersonnel + " PIN");
            return false;
        }else if (FobKey.isEmpty() && MagCard_FobKey.isEmpty() && Barcode_val.isEmpty()){
            Toast.makeText(this, "Access device value empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {
            if (requestCode == RC_BARCODE_CAPTURE) {
                if (resultCode == CommonStatusCodes.SUCCESS) {
                    if (data != null) {

                        Barcode_val = data.getStringExtra("Barcode").trim();
                        AppConstants.colorToast(getApplicationContext(), "Barcode Read: " + Barcode_val, Color.BLACK);
                        Log.d(TAG, "Barcode read: " + data.getStringExtra("Barcode").trim());
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile("Vehicle Barcode read success: " + Barcode_val);
                        tv_Display_msg.setText("Barcode scan successfully: "+Barcode_val);
                    } else {

                        InitScreen();
                        Log.d(TAG, "No barcode captured, intent data is null");
                    }
                } else {
                    Barcode_val = "";
                    Log.d(TAG, "barcode captured failed");
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG, "barcode exception "+e.toString());
        }
    }

    public class CheckPersonFobOnly extends AsyncTask<Void, Void, String> {

        public String response = null;
        //VehicleRequireEntity vrentity = null;

        //SW: I will use this once server does
        CheckPinFobEntity vfentity = null;

        public CheckPersonFobOnly(CheckPinFobEntity vfentity) {
            this.vfentity = vfentity;
        }

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(AcceptPinActivity_FOB.this);
            pd.setMessage("Please wait...");
            pd.show();

        }

        @Override
        protected String doInBackground(Void... voids) {

            try {
                ServerHandler serverHandler = new ServerHandler();

                Gson gson = new Gson();
                String jsonData = gson.toJson(vfentity);
                String userEmail = CommonUtils.getCustomerDetails(AcceptPinActivity_FOB.this).PersonEmail;

                System.out.println("jsonDatajsonDatajsonData" + jsonData);
                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(vfentity.IMEIUDID + ":" + userEmail + ":" + "CheckPersonFobOnly" + AppConstants.LANG_PARAM);
                response = serverHandler.PostTextData(AcceptPinActivity_FOB.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {
                ex.printStackTrace();
                if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + " CheckPersonFobOnly Exception: "+ex.getMessage());
            }
            return response;
        }

        @Override
        protected void onPostExecute(String serverRes) {
            pd.dismiss();
            System.out.println("onPostExecute-" + serverRes);

            try {

                if (serverRes != null) {


                    JSONObject jsonObject = new JSONObject(serverRes);

                    String ResponceMessage = jsonObject.getString("ResponceMessage");
                    String ResponceText = jsonObject.getString("ResponceText");

                    System.out.println("ResponceMessage.." + ResponceMessage);

                    if (ResponceMessage.equalsIgnoreCase("success")) {
                        InitScreen();
                        //CommonUtils.showCustomMessageDilaog(AcceptPinActivity_FOB.this, "Message", ResponceText);
                        CommonUtils.AutoCloseCustomMessageDilaog(AcceptPinActivity_FOB.this, "Message", ResponceText);
                    } else if (ResponceText.equalsIgnoreCase("success")){
                        InitScreen();
                        CommonUtils.showCustomMessageDilaog(AcceptPinActivity_FOB.this, "Message", ResponceMessage);
                    } else{

                        if (ResponceMessage.equalsIgnoreCase("fail")){
                            String IsPINHavingAccessDevice = "n";
                            if(jsonObject.has("IsPINHavingAccessDevice")) {
                                IsPINHavingAccessDevice = jsonObject.getString("IsPINHavingAccessDevice");
                            }
                            if (IsPINHavingAccessDevice.equalsIgnoreCase("y")){
                                InitScreen();
                                String msg = "The " + ScreenNameForPersonnel + " you have entered already has an Access Device assigned. Would you like to remove the existing device we have on file and use this as a replacement.";
                                CustomMessage2Input(AcceptPinActivity_FOB.this, "Message", msg);
                            }else {
                                //CommonUtils.showCustomMessageDilaog(AcceptPinActivity_FOB.this, "Message", ResponceText);
                                if (ResponceText.toLowerCase().contains("invalid")) {
                                    ResponceText = ResponceText + " Would you like to try again?";
                                    CustomMessageInvalidPINInput(AcceptPinActivity_FOB.this, "Message", ResponceText);
                                } else {
                                    InitScreen();
                                    CommonUtils.showCustomMessageDilaog(AcceptPinActivity_FOB.this, "Message", ResponceText);
                                }
                            }
                        }else{
                            InitScreen();
                            CommonUtils.showCustomMessageDilaog(AcceptPinActivity_FOB.this, "Message", ResponceMessage);
                        }

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                InputMethodManager inputMethodManager = (InputMethodManager) editpinNumber.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                editpinNumber.requestFocus();
                                inputMethodManager.showSoftInput(editpinNumber, 0);
                            }
                        }, 1000);

                    }

                } else {
                    InitScreen();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void CustomMessage2Input(final Activity context, String title, String message) {

        final Dialog dialogBus = new Dialog(context);
        dialogBus.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogBus.setCancelable(false);
        dialogBus.setContentView(R.layout.custom_alertdialougeinput);
        dialogBus.show();

        String newString1 = message.replaceAll("PERSONNEL", "<font color='red'> " + "<U> PERSONNEL </U>" + " </font>");
        String newString = newString1.replaceAll("VEHICLE", "<font color='red'> " + "<U> VEHICLE </U>" + " </font>");

        TextView edt_message = (TextView) dialogBus.findViewById(R.id.edt_message);
        Button btnYes = (Button) dialogBus.findViewById(R.id.btnYes);
        Button btnNo = (Button) dialogBus.findViewById(R.id.btnNo);
        edt_message.setText(Html.fromHtml(newString, Html.FROM_HTML_MODE_LEGACY));

        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogBus.dismiss();

                //save to cloud call
                CheckPinFobEntity objEntityClass = new CheckPinFobEntity();
                objEntityClass.IMEIUDID = AppConstants.getIMEI(AcceptPinActivity_FOB.this);
                objEntityClass.PersonnelPIN = AppConstants.AddPin;
                objEntityClass.FOBNumber = AppConstants.AddFobKey;
                objEntityClass.MagneticCardNumber =  AppConstants.AddMagCard_FobKey;
                objEntityClass.Barcode = AppConstants.AddBarcode_val;
                objEntityClass.ReplaceAccessDevice = "y";

                if (cd.isConnectingToInternet()){
                    new CheckPersonFobOnly(objEntityClass).execute();
                }else{
                    CommonUtils.showNoInternetDialog(AcceptPinActivity_FOB.this);
                }
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogBus.dismiss();

//                editVehicleNumber.requestFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void CustomMessageInvalidPINInput(final Activity context, String title, String message) {

        final Dialog dialogBus = new Dialog(context);
        dialogBus.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogBus.setCancelable(false);
        dialogBus.setContentView(R.layout.custom_alertdialougeinput);
        dialogBus.show();

        //String newString1 = message.replaceAll("PERSONNEL", "<font color='red'> " + "<U> PERSONNEL </U>" + " </font>");
        //String newString = newString1.replaceAll("VEHICLE", "<font color='red'> " + "<U> VEHICLE </U>" + " </font>");

        TextView edt_message = (TextView) dialogBus.findViewById(R.id.edt_message);
        Button btnYes = (Button) dialogBus.findViewById(R.id.btnYes);
        Button btnNo = (Button) dialogBus.findViewById(R.id.btnNo);
        edt_message.setText(message); //Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY)

        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                editpinNumber.setText("");
                dialogBus.dismiss();
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                InitScreen();
                dialogBus.dismiss();

                //editVehicleNumber.requestFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });

    }

    @SuppressLint("LongLogTag")
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

        return isKeyboardShown;
    }

    public void hideKeybord() {

        View view = this.getCurrentFocus();

        if (view != null) {
            InputMethodManager manager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

    public void showKeybord() {


    }

    public class RecognizeVehicleORPersonnelAccessDevice extends AsyncTask<Void, Void, String> {

        public String response = null;
        //VehicleRequireEntity vrentity = null;

        //SW: I will use this once server does
        VehicleRequireEntity vfentity = null;

        public RecognizeVehicleORPersonnelAccessDevice(VehicleRequireEntity vfentity) {
            this.vfentity = vfentity;
        }

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(AcceptPinActivity_FOB.this);
            pd.setMessage("Please wait...");
            pd.show();

        }

        @Override
        protected String doInBackground(Void... voids) {

            try {
                ServerHandler serverHandler = new ServerHandler();

                Gson gson = new Gson();
                String jsonData = gson.toJson(vfentity);
                String userEmail = CommonUtils.getCustomerDetails(AcceptPinActivity_FOB.this).PersonEmail;

                System.out.println("jsonDatajsonDatajsonData" + jsonData);
                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(vfentity.IMEIUDID + ":" + userEmail + ":" + "RecognizeVehicleORPersonnelAccessDevice" + AppConstants.LANG_PARAM);
                response = serverHandler.PostTextData(AcceptPinActivity_FOB.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {
                ex.printStackTrace();
                AppConstants.WriteinFile(TAG + " RecognizeVehicleORPersonnelAccessDevice Exception: "+ex.getMessage());
            }
            return response;
        }

        @Override
        protected void onPostExecute(String serverRes) {
            pd.dismiss();
            System.out.println("onPostExecute-" + serverRes);

            try {

                if (serverRes != null) {

                    JSONObject jsonObject = new JSONObject(serverRes);

                    String ResponceMessage = jsonObject.getString("ResponceMessage");
                    String ResponceText = jsonObject.getString("ResponceText");
                    String VehicleNumber = jsonObject.getString("VehicleNumber");
                    String PersonPin = jsonObject.getString("PersonPin");
                    System.out.println("ResponceMessage.." + ResponceMessage);

                    //InitScreen();
                    AppConstants.WriteinFile(TAG + " RecognizeVehicleORPersonnelAccessDevice Response: "+ResponceText);
                    if (ResponceMessage.equalsIgnoreCase("success")) {
                        if (VehicleNumber.trim().isEmpty() && PersonPin.trim().isEmpty()) {
                            SuccessUi();
                        } else {
                            ResponceText = ResponceText.replaceFirst("vehicle", ScreenNameForVehicle);
                            InitScreen();
                            CommonUtils.showCustomMessageDilaog(AcceptPinActivity_FOB.this, "Message", ResponceText);
                        }
                    } else {
                        CommonUtils.showCustomMessageDilaog(AcceptPinActivity_FOB.this, "Message", ResponceText);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                InitScreen();
                            }
                        }, 2000);

                    }

                } else {
                    InitScreen();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}