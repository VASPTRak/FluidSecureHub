package com.TrakEngineering.FluidSecureHub;

/*import static com.azure.android.maps.control.options.AnimationOptions.animationDuration;
import static com.azure.android.maps.control.options.AnimationOptions.animationType;
import static com.azure.android.maps.control.options.CameraOptions.center;
import static com.azure.android.maps.control.options.CameraOptions.zoom;*/

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.TrakEngineering.FluidSecureHub.wifihotspot.WifiApManager;
import com.TrakEngineering.FluidSecureHub.entity.AddTankFromAPP_entity;
import com.TrakEngineering.FluidSecureHub.entity.UpdateMacAddressClass;
import com.TrakEngineering.FluidSecureHub.offline.OfflineConstants;
import com.TrakEngineering.FluidSecureHub.retrofit.AzureMapApi;
import com.TrakEngineering.FluidSecureHub.server.ServerHandler;
/*import com.azure.android.maps.control.AzureMap;
import com.azure.android.maps.control.AzureMaps;
import com.azure.android.maps.control.Control;
import com.azure.android.maps.control.MapControl;
import com.azure.android.maps.control.controls.ZoomControl;
import com.azure.android.maps.control.events.OnClick;
import com.azure.android.maps.control.layer.SymbolLayer;
import com.azure.android.maps.control.options.AnimationType;
import com.azure.android.maps.control.source.DataSource;*/
import com.github.xizzhu.simpletooltip.ToolTip;
import com.github.xizzhu.simpletooltip.ToolTipView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
//import com.mapbox.geojson.Point;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


public class AddNewLinkToCloud extends AppCompatActivity implements LifecycleObserver, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private AddnewLink_ViewModel addnewlinkViewModel;
    private String TAG = this.getClass().getSimpleName();
    private Spinner Spinner_tankNumber, spin_pulseRatio;
    private EditText edt_linkname, edt_pumpOnTime, edt_pumpOffTime; //, edt_username, edt_enter_password
    private EditText edt_LinkNewName; //, edt_StreetAddress, edt_UnitsMeasured, edt_Pulses
    private Button btn_cancel, btn_done, btnMap; //btnAddNewTank
    private String expression = "^[a-zA-Z0-9-_ ]*$";
    public AlertDialog alertDialog;
    //private CheckBox chkShowHidePassword;
    private ImageButton iBtn_LinkName, iBtn_NewName, iBtn_AddNewTank, iBtn_PumpOnTime, iBtn_PumpOffTime, iBtn_PulseRatio, iBtn_StreetAddress;
    public String selectedLongitude = "0", selectedLatitude = "0";
    public String selectedPulseRatio = "";
    Bundle mySavedInstanceState;
    public int selectedProductPosition = 0;
    public boolean isLocationSelected = false;
    public Dialog addTankDialog;
    private String subscriptionKeyForAzureMap = "";
    //MapControl mapControl;
    //SymbolLayer layer;
    public ArrayAdapter<String> autoAdapter;
    public AutoCompleteTextView tv_SearchAddress;
    ArrayList<String> searchResults = new ArrayList<>();
    ArrayList<HashMap<String, String>> listOfPositions = new ArrayList<>();
    //public boolean isAddressSelected = false;
    public String StreetAddress = "", selectedAddressFromMap = "";
    private boolean configurationProcessStarted = false;

    private ConnectionDetector cd = new ConnectionDetector(AddNewLinkToCloud.this);
    static WifiApManager wifiApManager;
    ProgressDialog loading = null;
    CountDownTimer countDownTimerForConfigure = null;
    public boolean ConfigurationStep1IsInProgress = false;
    public boolean proceedAfterManualWifiConnect = false;
    public boolean enableHotspotAfterNewLinkConfigure = false;
    public boolean skipOnResume = false;
    String HTTP_URL = "";
    String URL_INFO = "";
    String URL_UPDATE_FS_INFO = "";
    int TimeOutInMinutes = 3;
    boolean IsTimeout_Sec = true;
    int pulseRatioSelectionCount = 0, tankSelectionCount = 0;
    WebView webView;

    /*static {
        AzureMaps.setSubscriptionKey("FJ29LaayVFiy20Hp29hEe5mG7F6QTbhfyV6wuWwG7Sg");
    }*/

    @Override
    protected void onResume() {
        super.onResume();

        if (skipOnResume && !proceedAfterManualWifiConnect) {
            skipOnResume = false;
            return;
        }
        if (configurationProcessStarted) {
            ProceedToLinkConfiguration(true);
        }

        if (ConfigurationStep1IsInProgress) {
            String s = getResources().getString(R.string.PleaseWaitForWifiConnect);
            SpannableString ss2 = new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            loading = new ProgressDialog(AddNewLinkToCloud.this);
            loading.setMessage(ss2);
            loading.setCancelable(false);
            loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            loading.show();
        }

        if (proceedAfterManualWifiConnect) {
            proceedAfterManualWifiConnect = false;
            new WiFiConnectTask().execute();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mySavedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_add_new_link_to_cloud);

        wifiApManager = new WifiApManager(this);

        btn_done = (Button) findViewById(R.id.btn_done);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        edt_linkname = (EditText) findViewById(R.id.edt_linkname);
        edt_pumpOnTime = (EditText) findViewById(R.id.edt_pumpOnTime);
        edt_pumpOffTime = (EditText) findViewById(R.id.edt_pumpOffTime);
        //edt_username = (EditText) findViewById(R.id.edt_username);
        //edt_enter_password = (EditText) findViewById(R.id.edt_enter_password);
        Spinner_tankNumber = (Spinner) findViewById(R.id.spin_tanknumber);
        Spinner_tankNumber.setOnItemSelectedListener(this);
        //chkShowHidePassword = (CheckBox) findViewById(R.id.chkShowHidePassword);
        edt_LinkNewName = (EditText) findViewById(R.id.edt_LinkNewName);
        //edt_UnitsMeasured = (EditText) findViewById(R.id.edt_UnitsMeasured);
        //edt_Pulses = (EditText) findViewById(R.id.edt_Pulses);
        //edt_StreetAddress = (EditText) findViewById(R.id.edt_StreetAddress);
        isLocationSelected = false;

        iBtn_LinkName = (ImageButton) findViewById(R.id.iBtn_LinkName);
        iBtn_NewName = (ImageButton) findViewById(R.id.iBtn_NewName);
        //btnAddNewTank = (Button) findViewById(R.id.btnAddNewTank);
        iBtn_AddNewTank = (ImageButton) findViewById(R.id.iBtn_AddNewTank);
        iBtn_PumpOnTime = (ImageButton) findViewById(R.id.iBtn_PumpOnTime);
        iBtn_PumpOffTime = (ImageButton) findViewById(R.id.iBtn_PumpOffTime);
        spin_pulseRatio = (Spinner) findViewById(R.id.spin_pulseRatio);

        spin_pulseRatio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (pulseRatioSelectionCount > 0) {
                    IsTimeout_Sec = false;
                }
                pulseRatioSelectionCount++;
                selectedPulseRatio = spin_pulseRatio.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        iBtn_PulseRatio = (ImageButton) findViewById(R.id.iBtn_PulseRatio);
        iBtn_StreetAddress = (ImageButton) findViewById(R.id.iBtn_StreetAddress);
        btnMap = (Button) findViewById(R.id.btnMap);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AddnewLink_ViewModel(getApplication()));

        // Get the ViewModel.
        addnewlinkViewModel = new ViewModelProvider(this).get(AddnewLink_ViewModel.class);
        LiveData<String> myRandomNumber = addnewlinkViewModel.getNumber();
        myRandomNumber.hasActiveObservers();

        myRandomNumber.observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
               AlertDialogBox(AddNewLinkToCloud.this, s);
                /*if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " SaveLinkFromAPP Response: " + s);*/
            }
        });

        LiveData<Boolean> IsUpdating = addnewlinkViewModel.getIsUpdating();
        IsUpdating.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isUpdating) {
                if (isUpdating){
                    ShowProgressDialog();
                }else{
                    HideProgressDialog();
                }
            }
        });

        // Get Tank and Product details
        new GetTanksAndProducts().execute();

        /*ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, addnewlinkViewModel.getSpinnerList());
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner_tankNumber.setAdapter(aa);*/ // OLD code
        //BindTankList();

        ArrayAdapter<CharSequence> aaPR = ArrayAdapter.createFromResource(this, R.array.pulseRatios, R.layout.spinner_item_list);
        aaPR.setDropDownViewResource(R.layout.spinner_item_list);
        spin_pulseRatio.setAdapter(aaPR);
        spin_pulseRatio.setSelection(1);

        /*btnAddNewTank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertAddNewTankDialog();
            }
        });*/

        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.hideKeyboard(AddNewLinkToCloud.this);
                IsTimeout_Sec = false;
                //AlertDialogBox(AddNewLinkToCloud.this, "New success message");
                if (validateData()) {
                    String UnitsMeasured = "1";
                    String Pulses = "20";
                    try {
                        selectedPulseRatio = spin_pulseRatio.getSelectedItem().toString();
                        if (!selectedPulseRatio.isEmpty()) {
                            String[] split = selectedPulseRatio.split("/");

                            if (split.length > 1) {
                                Pulses = split[0].trim();
                                UnitsMeasured = split[1];
                            }
                        }
                    } catch (Exception e) {
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + " SaveLinkFromAPP Exception while getting PulseRatio: " + e.getMessage());
                    }
                    addnewlinkViewModel.ProcessData(edt_linkname.getText().toString().trim(), edt_pumpOnTime.getText().toString().trim(),
                            edt_pumpOffTime.getText().toString().trim(), edt_LinkNewName.getText().toString().trim(),
                            UnitsMeasured, Pulses, StreetAddress.trim());

                    //, edt_username.getText().toString().trim(), edt_enter_password.getText().toString().trim()
                    //, edt_UnitsMeasured.getText().toString().trim(), edt_Pulses.getText().toString().trim()
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackToWelcomeActivity();
            }
        });

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.hideKeyboard(AddNewLinkToCloud.this);
                IsTimeout_Sec = false;
                setSubscriptionKeyForAzureMap();

                alertMapDialog();
            }
        });

        SetToolTips();

        if (AppConstants.SHOW_WELCOME_DIALOG_FOR_ADD_NEW_LINK) {
            welcomeDialog();
        }

        long screenTimeOut = TimeOutInMinutes * 60000L;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (IsTimeout_Sec) {
                    IsTimeout_Sec = false;

                    BackToWelcomeActivity();
                }
            }
        }, screenTimeOut);

        /*chkShowHidePassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // show password
                    edt_enter_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    // hide password
                    edt_enter_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });*/

    }

    @Override
    public void onBackPressed() {
        IsTimeout_Sec = false;
        BackToWelcomeActivity();
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
        menu.findItem(R.id.enable_debug_window).setVisible(false);
        menu.findItem(R.id.madd_link).setVisible(false);
        menu.findItem(R.id.mshow_reader_status).setVisible(false);
        menu.findItem(R.id.testTransaction).setVisible(false);
        menu.findItem(R.id.forceOfflineList).setVisible(false);

        SharedPreferences sharedPref = AddNewLinkToCloud.this.getSharedPreferences("LanguageSettings", Context.MODE_PRIVATE);
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

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.mrestartapp:
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "<Restart app.>");
                Intent i = new Intent(AddNewLinkToCloud.this, SplashActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                break;

            case R.id.menuSpanish:
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " <Spanish language selected.>");
                AppConstants.LANGUAGE_CHANGED = true;
                StoreLanguageSettings("es");
                break;

            case R.id.menuEnglish:
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " <English language selected.>");
                AppConstants.LANGUAGE_CHANGED = true;
                StoreLanguageSettings("en");
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void StoreLanguageSettings(String language) {
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

            //recreate();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "<Restarting the activity.>");
            Intent i = new Intent(AddNewLinkToCloud.this, AddNewLinkToCloud.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.startActivity(i);

        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Exception occurred in StoreLanguageSettings: " + e.getMessage());
        }
    }

    private void setSubscriptionKeyForAzureMap() {
        try {
            SharedPreferences sharedPref = this.getSharedPreferences(AppConstants.PREF_AZURE_MAP_DETAILS, Context.MODE_PRIVATE);
            subscriptionKeyForAzureMap = sharedPref.getString("SubscriptionKey", "");

            //AzureMaps.setSubscriptionKey(SubscriptionKeyForAzureMap);

            //Set the language to be used by Azure Maps.
            if (!AppConstants.LANG_PARAM.isEmpty()) {
                //AzureMaps.setLanguage(AppConstants.LANG_PARAM.replace(":", ""));
            }
        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " setSubscriptionKeyForAzureMap Exception: " + ex.getMessage());
        }
    }

    public void BindTankList(boolean newTankAdded) {

        ArrayAdapter aa = new ArrayAdapter(this, R.layout.spinner_item_list, addnewlinkViewModel.getTankSpinnerList(this));
        aa.setDropDownViewResource(R.layout.spinner_item_list);
        Spinner_tankNumber.setAdapter(aa);
        if (Spinner_tankNumber.getSelectedItemPosition() == 0) {
            if (CommonUtils.TankDataList.size() > 0) {
                Spinner_tankNumber.setSelection(1);
                addnewlinkViewModel.TankPositionSel = 0;
            }
        }
        if (newTankAdded) {
            if (CommonUtils.TankDataList.size() > 0) {
                Spinner_tankNumber.setSelection(CommonUtils.TankDataList.size()); // last item
                addnewlinkViewModel.TankPositionSel = CommonUtils.TankDataList.size() - 1;
            }
        }
    }

    public void alertAddNewTankDialog() {
        try {
            final Dialog dialog = new Dialog(AddNewLinkToCloud.this);
            dialog.setTitle("");
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_add_new_tank);
            dialog.setCancelable(false);
            addTankDialog = dialog;

            EditText edt_TankNumber = (EditText) dialog.findViewById(R.id.edt_TankNumber);
            EditText edt_TankName = (EditText) dialog.findViewById(R.id.edt_TankName);
            Spinner spin_Product = (Spinner) dialog.findViewById(R.id.spin_Product);

            ArrayAdapter aaPd = new ArrayAdapter(this, R.layout.spinner_item_list, addnewlinkViewModel.getProductList());
            aaPd.setDropDownViewResource(R.layout.spinner_item_list);
            spin_Product.setAdapter(aaPd);

            spin_Product.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    //String selected_val = spin_Product.getSelectedItem().toString();
                    selectedProductPosition = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            Button btnSaveTank = (Button) dialog.findViewById(R.id.btnSaveMap);
            Button btnCancelTank = (Button) dialog.findViewById(R.id.btnCancelMap);

            btnSaveTank.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (validateTankData(edt_TankNumber, edt_TankName)) {
                        String TankNumber = edt_TankNumber.getText().toString();
                        String TankName = edt_TankName.getText().toString();
                        String FuelTypeId = addnewlinkViewModel.getFuelTypeIdByFuelType(selectedProductPosition);

                        new AddTankFromAPP().execute(TankNumber, TankName, FuelTypeId);
                        //dialog.dismiss();
                    }
                }
            });

            btnCancelTank.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " alertAddNewTankDialog Exception: " + ex.getMessage());
        }
    }

    public void alertMapDialog() {
        try {

            final Dialog dialog = new Dialog(AddNewLinkToCloud.this);
            dialog.setTitle("");
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_map);
            dialog.setCancelable(false);

            Window window = dialog.getWindow();
            if (window != null) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            //mapControl = dialog.findViewById(R.id.map_control);
            //mapControl.onCreate(mySavedInstanceState);
            webView = dialog.findViewById((R.id.map_control));

            loadHtmlWithReplacement(webView, this);

            // Address search functionality
            tv_SearchAddress = dialog.findViewById(R.id.tv_SearchAddress);
            autoAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, searchResults);
            tv_SearchAddress.setAdapter(autoAdapter);

            tv_SearchAddress.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    //isAddressSelected = true;
                }


                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!s.toString().isEmpty()) {
                        selectedAddressFromMap = "";
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.toString().length() > 2 && !selectedAddressFromMap.equalsIgnoreCase(s.toString())) { // isAddressSelected) {
                        retrieveAddressDataFromApi(s.toString());
                    }
                }
            });
            //=================================================

            Button btnSaveMap = (Button) dialog.findViewById(R.id.btnSaveMap);
            Button btnCancelMap = (Button) dialog.findViewById(R.id.btnCancelMap);

            btnSaveMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppConstants.LATITUDE = selectedLatitude;
                    AppConstants.LONGITUDE = selectedLongitude;
                    isLocationSelected = true;
                    GetAddressByLatLng(Double.parseDouble(AppConstants.LATITUDE), Double.parseDouble(AppConstants.LONGITUDE));
                    webView.destroy();
                    dialog.dismiss();
                }
            });

            btnCancelMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    webView.destroy();
                    dialog.dismiss();
                }
            });
            webView.onResume();
            //Wait until the map resources are ready.
            /*mapControl.onReady(map -> {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " <map resources are ready.>");
                InitializeMap(map);

                tv_SearchAddress.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //Log.d(TAG, "Selected address is " + searchResults.get(position).toString());
                        selectedAddressFromMap = searchResults.get(position).toString();
                        //isAddressSelected = false;
                        tv_SearchAddress.dismissDropDown();

                        if (listOfPositions != null) {
                            if (listOfPositions.size() > 0) {
                                String latitude = listOfPositions.get(position).get("latitude");
                                String longitude = listOfPositions.get(position).get("longitude");
                                if (latitude != null && longitude != null) {
                                    map.layers.remove(layer);
                                    AddSymbolLayerToMap(map, Double.parseDouble(longitude), Double.parseDouble(latitude));
                                }
                            }
                        }
                    }
                });
            });
            mapControl.onResume();*/

            tv_SearchAddress.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //Log.d(TAG, "Selected address is " + searchResults.get(position).toString());
                    selectedAddressFromMap = searchResults.get(position).toString();
                    //isAddressSelected = false;
                    tv_SearchAddress.dismissDropDown();

                    if (listOfPositions != null) {
                        if (!listOfPositions.isEmpty()) {
                            String latitude = listOfPositions.get(position).get("latitude");
                            String longitude = listOfPositions.get(position).get("longitude");
                            if (latitude != null && longitude != null) {
                                updateMapLocation(Double.parseDouble(latitude), Double.parseDouble(longitude));
                            }
                        }
                    }
                }
            });

            dialog.show();
        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " alertMapDialog Exception: " + ex.getMessage());
        }
    }

    private void loadHtmlWithReplacement(WebView webView, Context context) {
        try {
            InputStream inputStream = context.getAssets().open("map.html");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder htmlBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                htmlBuilder.append(line);
            }

            reader.close();
            inputStream.close();

            String modifiedHtml = htmlBuilder.toString().replace("SUBSCRIPTION_KEY", subscriptionKeyForAzureMap);
            modifiedHtml = modifiedHtml.replace("strLng", String.valueOf(Double.parseDouble(AppConstants.LONGITUDE)));
            modifiedHtml = modifiedHtml.replace("strLat", String.valueOf(Double.parseDouble(AppConstants.LATITUDE)));
            String languageParam = "en-US";
            if (!AppConstants.LANG_PARAM.isEmpty()) {
                languageParam = AppConstants.LANG_PARAM.replace(":", "");
            }
            modifiedHtml = modifiedHtml.replace("languageParam", languageParam);

            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            webView.setWebViewClient(new WebViewClient());
            webView.setWebChromeClient(new WebChromeClient());
            webView.addJavascriptInterface(new MapInterface(this), "Android");

            //Log.d(TAG, "loadHtmlWithReplacement: modifiedHtml: " + modifiedHtml);
            //webView.loadUrl("file:///android_asset/map.html");
            //webView.loadData(modifiedHtml, "text/html", "UTF-8");
            webView.loadDataWithBaseURL(null, modifiedHtml, "text/html", "UTF-8", null);
            //webView.loadDataWithBaseURL(null, "<html><body><h1>Hello World</h1></body></html>", "text/html", "UTF-8", null);

        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " loadHtmlWithReplacement Exception: " + e.getMessage());
        }
    }

    private void updateMapLocation(double lat, double lon) {
        String js = "updateMapLocation(" + lat + ", " + lon + ")";
        webView.evaluateJavascript(js, null);
    }

    public class MapInterface {
        Context mContext;

        MapInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void sendCoordinates(double lng, double lat) {
            Log.d("AzureMap", "Longitude: " + lng + ", Latitude: " + lat);
            selectedLatitude = String.valueOf(lat);
            selectedLongitude = String.valueOf(lng);
        }
    }

    /*private void InitializeMap(AzureMap map) {
        try {
            AddSymbolLayerToMap(map, Double.parseDouble(AppConstants.LONGITUDE), Double.parseDouble(AppConstants.LATITUDE));

            *//*Set the camera of the map.
            map.setCamera(
                    center(Point.fromLngLat(Double.parseDouble(AppConstants.LONGITUDE), Double.parseDouble(AppConstants.LATITUDE))),
                    zoom(15),
                    animationType(AnimationType.FLY),
                    animationDuration(2000)
            );*//*

            // Add controls on MAP
            map.controls.add(
                    new Control[]{
                            new ZoomControl()
                    }
            );

            // Handle events
            map.events.add((OnClick) (lat, lon) -> {
                //Map clicked.
                map.layers.remove(layer);
                AddSymbolLayerToMap(map, lon, lat);
                return true;
            });
        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " InitializeMap Exception: " + ex.getMessage());
        }
    }*/

    /*private void AddSymbolLayerToMap(AzureMap map, double longitude, double latitude) {
        try {
            selectedLatitude = String.valueOf(latitude);
            selectedLongitude = String.valueOf(longitude);
            //Create a data source and add it to the map.
            DataSource source = new DataSource();
            map.sources.add(source);
            //Create a point and add it to the data source.
            source.add(Point.fromLngLat(longitude, latitude));
            //Create a symbol layer to render icons and/or text at points on the map.
            layer = new SymbolLayer(source);
            //Add the layer to the map.
            map.layers.add(layer);

            //Set the camera of the map.
            map.setCamera(
                    center(Point.fromLngLat(longitude, latitude)),
                    zoom(15),
                    animationType(AnimationType.FLY),
                    animationDuration(2000)
            );
        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " AddSymbolLayerToMap Exception: " + ex.getMessage());
        }
    }*/

    private void SetToolTips() {
        try {
            // Set Tooltip for LINK Name
            iBtn_LinkName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CreateToolTip(v, iBtn_LinkName.getContentDescription().toString(), Gravity.BOTTOM);
                }
            });
            //===============================================================
            // Set Tooltip for New Name
            iBtn_NewName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CreateToolTip(v, iBtn_NewName.getContentDescription().toString(), Gravity.BOTTOM);
                }
            });
            //===============================================================
            // Set Tooltip for Pulse Ratio
            iBtn_AddNewTank.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CreateToolTip(v, iBtn_AddNewTank.getContentDescription().toString(), Gravity.BOTTOM);
                }
            });
            //===============================================================
            // Set Tooltip for Pump On Time
            iBtn_PumpOnTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CreateToolTip(v, iBtn_PumpOnTime.getContentDescription().toString(), Gravity.BOTTOM);
                }
            });
            //===============================================================
            // Set Tooltip for Pump Off Time
            iBtn_PumpOffTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CreateToolTip(v, iBtn_PumpOffTime.getContentDescription().toString(), Gravity.BOTTOM);
                }
            });
            //===============================================================
            // Set Tooltip for Pulse Ratio
            iBtn_PulseRatio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CreateToolTip(v, iBtn_PulseRatio.getContentDescription().toString(), Gravity.BOTTOM);
                }
            });
            //===============================================================*/
            // Set Tooltip for Pulse Ratio
            iBtn_StreetAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CreateToolTip(v, iBtn_StreetAddress.getContentDescription().toString(), Gravity.TOP);
                }
            });
            //===============================================================
        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " SetToolTips Exception: " + ex.getMessage());
        }
    }

    public void CreateToolTip(View view, String message, int gravity) {
        try {
            int padding = getResources().getDimensionPixelSize(R.dimen.tooltip_padding);
            int textSize = getResources().getDimensionPixelSize(R.dimen.tooltip_text_size);
            int radius = getResources().getDimensionPixelSize(R.dimen.tooltip_radius);

            ToolTip toolTip = new ToolTip.Builder()
                    .withText(message)
                    .withTextColor(Color.WHITE)
                    .withTextSize(textSize)
                    .withBackgroundColor(getResources().getColor(R.color.colorPrimary))
                    .withPadding(padding, padding, padding, padding)
                    .withCornerRadius(radius)
                    .build();
            ToolTipView toolTipView = new ToolTipView.Builder(AddNewLinkToCloud.this)
                    .withAnchor(view)
                    .withToolTip(toolTip)
                    .withGravity(gravity)
                    .build();
            toolTipView.show();
        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " CreateToolTip Exception: " + ex.getMessage());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

//        ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
//        ((TextView) parent.getChildAt(0)).setTextSize(25);
        if (parent.getId() == R.id.spin_tanknumber) {
            if (tankSelectionCount > 0) {
                IsTimeout_Sec = false;
            }
            tankSelectionCount++;
            if (position == 0) {
                alertAddNewTankDialog();
                if (CommonUtils.TankDataList.size() > 0) {
                    Spinner_tankNumber.setSelection(1);
                    addnewlinkViewModel.TankPositionSel = 0;
                }
            } else {
                addnewlinkViewModel.TankPositionSel = position - 1;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private boolean validateData() {
        String pumpOnTime = edt_pumpOnTime.getText().toString().trim();
        String pumpOffTime = edt_pumpOffTime.getText().toString().trim();

        if (edt_linkname.getText().toString().trim().isEmpty()) {
            showCustomMessageDialog(AddNewLinkToCloud.this, getResources().getString(R.string.LinkNameRequired), "");
            return false;
        } else if (!edt_linkname.getText().toString().trim().matches(expression)) {
            showCustomMessageDialog(AddNewLinkToCloud.this, getResources().getString(R.string.LinkNameInvalid), " (Entered Link Name: " + edt_linkname.getText().toString() + ")");
            return false;
        } else if (!validateLinkNameForMac(edt_linkname.getText().toString().trim())) {
            showCustomMessageDialog(AddNewLinkToCloud.this, getResources().getString(R.string.LinkNameInvalid), " (Entered Link Name: " + edt_linkname.getText().toString() + ")");
            return false;
        } else if (pumpOnTime.isEmpty()) {
            showCustomMessageDialog(AddNewLinkToCloud.this, getResources().getString(R.string.PumpOnTimeRequired), "");
            return false;
        } else if (Integer.parseInt(pumpOnTime) < 30) {
            showCustomMessageDialog(AddNewLinkToCloud.this, getResources().getString(R.string.PumpOnTimeMinLimit), "");
            return false;
        } else if (pumpOffTime.isEmpty()) {
            showCustomMessageDialog(AddNewLinkToCloud.this, getResources().getString(R.string.PumpOffTimeRequired), "");
            return false;
        } else if (Integer.parseInt(pumpOffTime) < 10) {
            showCustomMessageDialog(AddNewLinkToCloud.this, getResources().getString(R.string.PumpOffTimeMinLimit), "");
            return false;
        } else if (!edt_LinkNewName.getText().toString().trim().isEmpty()) {
            if (!edt_LinkNewName.getText().toString().trim().matches(expression)) {
                showCustomMessageDialog(AddNewLinkToCloud.this, getResources().getString(R.string.NewNameInvalid), " (Entered New Name: " + edt_LinkNewName.getText().toString() + ")");
                return false;
            } else if (!isLocationSelected) {
                showCustomMessageDialog(AddNewLinkToCloud.this, getResources().getString(R.string.LocationRequired), "");
                return false;
            }
        } else if (!isLocationSelected) {
            showCustomMessageDialog(AddNewLinkToCloud.this, getResources().getString(R.string.LocationRequired), "");
            return false;
        }
        /*} else if (edt_username.getText().toString().trim().isEmpty()) {
            edt_username.setError("Enter valid data");
            return false;
        } else if (edt_enter_password.getText().toString().trim().isEmpty()) {
            edt_enter_password.setError("Enter valid data");
            return false;*/
        /*} else if (edt_UnitsMeasured.getText().toString().trim().isEmpty()) {
            edt_UnitsMeasured.setError("Enter valid data");
            return false;
        } else if (edt_Pulses.getText().toString().trim().isEmpty()) {
            edt_Pulses.setError("Enter valid data");
            return false;*/
        /*else if (edt_StreetAddress.getText().toString().trim().isEmpty()) {
            showCustomMessageDialog(AddNewLinkToCloud.this, getResources().getString(R.string.AddressRequired));
            return false;
        }*/
        return true;
    }

    private boolean validateLinkNameForMac(String linkName) {
        try {
            String macAddress = "";
            if (!linkName.isEmpty()) {
                if (linkName.toUpperCase().startsWith("FS-")) {
                    return true;
                } else {
                    String[] split = linkName.split("-");

                    if (split.length > 1) {
                        macAddress = split[1];

                        if (macAddress.length() == 12) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true; // Returned true from here, so it will go ahead and validate on the server side.
        }
        return true;
    }

    private boolean validateTankData(EditText edt_TankNumber, EditText edt_TankName) {

        if (edt_TankNumber.getText().toString().trim().isEmpty()) {
            //edt_TankNumber.setError(getResources().getString(R.string.TankNumberRequired));
            showCustomMessageDialog(AddNewLinkToCloud.this, getResources().getString(R.string.TankNumberRequired), "");
            return false;
        } else if (edt_TankName.getText().toString().trim().isEmpty()) {
            //edt_TankName.setError(getResources().getString(R.string.TankNameRequired));
            showCustomMessageDialog(AddNewLinkToCloud.this, getResources().getString(R.string.TankNameRequired), "");
            return false;
        }
        return true;
    }

    public void AlertDialogBox(final Context ctx, String message) {

        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(ctx);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        if (message.contains("success")) {
                            String linkName = edt_linkname.getText().toString().trim();
                            clearEditTextFields();
                            dialog.dismiss();
                            //finish();
                            // Save newly added link into arraylist to configure
                            boolean linkNameExist = false;
                            if (AppConstants.NEWLY_ADDED_LINKS != null) {
                                for (HashMap<String, String> hashMap : AppConstants.NEWLY_ADDED_LINKS) {
                                    if (hashMap.containsValue(linkName)) {
                                        linkNameExist = true;
                                        break;
                                    }
                                }
                            }
                            if (!linkNameExist) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put("SiteId", AppConstants.NEWLY_ADDED_SITE_ID);
                                map.put("LinkName", linkName);
                                AppConstants.NEWLY_ADDED_LINKS.add(map);
                            }
                            //==============================================================

                            AddMultipleLinksDialog();
                        } else {
                            dialog.dismiss();
                        }
                    }
                }
        );

        androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void clearEditTextFields() {
        isLocationSelected = false;
        StreetAddress = "";
        edt_linkname.getText().clear();
        edt_LinkNewName.getText().clear();
        edt_pumpOnTime.setText("60");
        edt_pumpOffTime.setText("60");
    }

    public void ShowProgressDialog(){

        String s = getResources().getString(R.string.PleaseWaitMessage);
        alertDialog = AlertDialogUtil.createAlertDialog(AddNewLinkToCloud.this, s, true);
        alertDialog.show();

        Thread thread = new Thread() {
            @Override
            public void run() {
                AlertDialogUtil.runAnimatedLoadingDots(AddNewLinkToCloud.this, s, alertDialog, true);
            }
        };
        thread.start();
    }

    public void HideProgressDialog(){
        if (alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    public class TankAndProductDetailsIMEI {
        public String IMEIUDID;
    }

    public class GetTanksAndProducts extends AsyncTask<String, Void, String> {

        //ProgressDialog pd;
        AlertDialog alertDialog;

        @Override
        protected void onPreExecute() {

            String s = getResources().getString(R.string.PleaseWaitMessage);
            alertDialog = AlertDialogUtil.createAlertDialog(AddNewLinkToCloud.this, s, true);
            alertDialog.show();

            Thread thread = new Thread() {
                @Override
                public void run() {
                    AlertDialogUtil.runAnimatedLoadingDots(AddNewLinkToCloud.this, s, alertDialog, true);
                }
            };
            thread.start();
        }

        protected String doInBackground(String... params) {

            String resp = "";

            String userEmail = CommonUtils.getCustomerDetails(AddNewLinkToCloud.this).PersonEmail;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(AddNewLinkToCloud.this) + ":" + userEmail + ":" + "GetTanksAndProducts" + AppConstants.LANG_PARAM);

            TankAndProductDetailsIMEI obj = new TankAndProductDetailsIMEI();
            obj.IMEIUDID = AppConstants.getIMEI(AddNewLinkToCloud.this);
            Gson gson = new Gson();
            String jsonData = gson.toJson(obj);

            try {
                OkHttpClient client = new OkHttpClient();
                MediaType TEXT = MediaType.parse("application/text;charset=UTF-8");

                RequestBody body = RequestBody.create(TEXT, jsonData);
                Request request = new Request.Builder()
                        .url(AppConstants.WEB_URL)
                        .post(body)
                        .addHeader("Authorization", authString)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " GetTanksAndProducts InBackground Exception: " + e.getMessage());
                if (OfflineConstants.isOfflineAccess(AddNewLinkToCloud.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String Response) {

            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            }

            try {
                JSONObject jsonObject = new JSONObject(Response);

                /*if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " Response: " + jsonObject);*/
                String ResponseMessage = jsonObject.getString("ResponseMessage");
                if (ResponseMessage.equalsIgnoreCase("success")) {

                    String tanksForLinksObj = jsonObject.getString(AppConstants.RES_TANK_DATA);
                    CommonUtils.BindTankData(tanksForLinksObj, true);
                    BindTankList(false);

                    String productsForTanksObj = jsonObject.getString(AppConstants.RES_PRODUCT_DATA);
                    CommonUtils.BindProductData(productsForTanksObj);

                }

            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " GetTanksAndProducts onPostExecute Exception: " + e.getMessage());
                if (OfflineConstants.isOfflineAccess(AddNewLinkToCloud.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }
        }
    }

    public class AddTankFromAPP extends AsyncTask<String, Void, String> {

        //ProgressDialog pd;
        AlertDialog alertDialog;
        String tankNumber = "";

        @Override
        protected void onPreExecute() {

            String s = getResources().getString(R.string.PleaseWaitMessage);
            alertDialog = AlertDialogUtil.createAlertDialog(AddNewLinkToCloud.this, s, true);
            alertDialog.show();

            Thread thread = new Thread() {
                @Override
                public void run() {
                    AlertDialogUtil.runAnimatedLoadingDots(AddNewLinkToCloud.this, s, alertDialog, true);
                }
            };
            thread.start();
        }

        protected String doInBackground(String... params) {

            String resp = "";

            String userEmail = CommonUtils.getCustomerDetails(AddNewLinkToCloud.this).PersonEmail;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(AddNewLinkToCloud.this) + ":" + userEmail + ":" + "AddTankFromAPP" + AppConstants.LANG_PARAM);

            AddTankFromAPP_entity obj = new AddTankFromAPP_entity();
            obj.TankNumber = params[0];
            obj.TankName = params[1];
            obj.FuelTypeId = params[2];

            Gson gson = new Gson();
            String jsonData = gson.toJson(obj);

            tankNumber = params[0];

            try {
                OkHttpClient client = new OkHttpClient();
                MediaType TEXT = MediaType.parse("application/text;charset=UTF-8");

                RequestBody body = RequestBody.create(TEXT, jsonData);
                Request request = new Request.Builder()
                        .url(AppConstants.WEB_URL)
                        .post(body)
                        .addHeader("Authorization", authString)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " AddTankFromAPP InBackground Exception: " + e.getMessage());
                if (OfflineConstants.isOfflineAccess(AddNewLinkToCloud.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String Response) {

            try {
                JSONObject jsonObject = new JSONObject(Response);

                String ResponseMessage = jsonObject.getString("ResponseMessage");
                String ResponseText = jsonObject.getString("ResponseText");
                if (ResponseMessage.equalsIgnoreCase("success")) {
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + " Tank Added Successfully. (" + tankNumber + ")");
                    String tanksForLinksObj = jsonObject.getString(AppConstants.RES_TANK_DATA);
                    CommonUtils.BindTankData(tanksForLinksObj, false);
                    //showCustomMessageDialog(AddNewLinkToCloud.this, ResponseMessage, ResponseText, addTankDialog);
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            BindTankList(true);
                            if (alertDialog.isShowing()) {
                                alertDialog.dismiss();
                            }
                            if (addTankDialog != null) {
                                addTankDialog.dismiss();
                            }
                        }
                    }, 1000);

                } else {
                    if (alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    }
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + " AddTankFromAPP Response: " + ResponseText);
                    showCustomMessageDialog(AddNewLinkToCloud.this, ResponseText, "");
                }

            } catch (Exception e) {
                if (alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                e.printStackTrace();
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " AddTankFromAPP onPostExecute Exception: " + e.getMessage());
                if (OfflineConstants.isOfflineAccess(AddNewLinkToCloud.this)) {
                    AppConstants.NETWORK_STRENGTH = false;
                }
            }
        }
    }

    public void showCustomMessageDialog(final Activity context, String message, String input) {
        if (AppConstants.GENERATE_LOGS)
            AppConstants.writeInFile(TAG + " " + message + input);
        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();

                        InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }
                }
        );

        androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void GetAddressByLatLng(Double Latitude, Double Longitude) {

        ProgressDialog pdAddress;
        String s = getResources().getString(R.string.PleaseWait);
        SpannableString ss2 = new SpannableString(s);
        ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
        ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
        pdAddress = new ProgressDialog(AddNewLinkToCloud.this);
        pdAddress.setMessage(ss2);
        pdAddress.setCancelable(false);
        pdAddress.show();

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        okhttp3.OkHttpClient.Builder httpClient = new okhttp3.OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .baseUrl(AppConstants.BASE_URL_AZURE_MAP)
                .build();

        AzureMapApi apiInterface;
        apiInterface = retrofit.create(AzureMapApi.class);

        Call<JsonObject> call = apiInterface.GetAddressByLatLng("1.0", subscriptionKeyForAzureMap, Latitude.toString() + ',' + Longitude.toString(), AppConstants.LANG_PARAM.replace(":", ""));

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                /*if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " GetAddressByLatLng onResponse: " + response.body());*/
                if (response.body() != null) {
                    parseResponseAddressByLatLng(pdAddress, response.body().toString());
                } else {
                    pdAddress.dismiss();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " GetAddressByLatLng onFailure: " + t.getMessage());
                pdAddress.dismiss();
            }
        });
    }

    private void parseResponseAddressByLatLng(ProgressDialog pdAddress, String response) {
        try {
            if (!response.isEmpty()) {
                JSONObject jsonObj = new JSONObject(response);
                String addresses = jsonObj.getString("addresses");
                if (addresses != null && !addresses.isEmpty()) {
                    JSONArray jsonArray = new JSONArray(addresses);
                    if (jsonArray.length() > 0) {
                        JSONObject jObj = jsonArray.getJSONObject(0);
                        String address = jObj.getString("address");
                        if (address != null && !address.isEmpty()) {
                            JSONObject addressObj = new JSONObject(address);
                            String freeformAddress = addressObj.getString("freeformAddress");
                            if (freeformAddress != null && !freeformAddress.isEmpty()) {
                                if (pdAddress != null) {
                                    pdAddress.dismiss();
                                }
                                //edt_StreetAddress.setText(freeformAddress);
                                StreetAddress = freeformAddress;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            if (pdAddress != null) {
                pdAddress.dismiss();
            }
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " parseResponseAddressByLatLng Exception: " + ex.getMessage());
        }
    }

    private void retrieveAddressDataFromApi(String query) {

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        okhttp3.OkHttpClient.Builder httpClient = new okhttp3.OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .baseUrl(AppConstants.BASE_URL_AZURE_MAP)
                .build();

        AzureMapApi apiInterface;
        apiInterface = retrofit.create(AzureMapApi.class);

        Call<JsonObject> call = apiInterface.GetAddressSuggestions("true", subscriptionKeyForAzureMap, "1.0", query,
                AppConstants.LANG_PARAM.replace(":", ""), AppConstants.LONGITUDE, AppConstants.LATITUDE, "", "Auto");

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                /*if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " retrieveAddressDataFromApi onResponse: " + response.body());*/
                if (response.body() != null) {
                    parseAddressesFromResponse(response.body().toString());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " retrieveAddressDataFromApi onFailure: " + t.getMessage());
            }
        });
    }

    private void parseAddressesFromResponse(String response) {
        try {
            if (!response.isEmpty()) {
                JSONObject jsonObj = new JSONObject(response);
                String addresses = jsonObj.getString("results");
                if (addresses != null && !addresses.isEmpty()) {
                    searchResults.clear();
                    listOfPositions.clear();
                    JSONArray jsonArray = new JSONArray(addresses);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jObj = jsonArray.getJSONObject(i);
                        String address = jObj.getString("address");
                        if (address != null && !address.isEmpty()) {
                            JSONObject addressObj = new JSONObject(address);
                            String freeformAddress = addressObj.getString("freeformAddress");
                            if (freeformAddress != null && !freeformAddress.isEmpty()) {
                                searchResults.add(freeformAddress);
                            }
                        }

                        String positions = jObj.getString("position");
                        if (positions != null && !positions.isEmpty()) {
                            JSONObject positionObj = new JSONObject(positions);
                            String lat = positionObj.getString("lat");
                            String lon = positionObj.getString("lon");
                            HashMap<String, String> mapPositions = new HashMap<>();
                            mapPositions.put("index", String.valueOf(i));
                            mapPositions.put("latitude", lat);
                            mapPositions.put("longitude", lon);
                            listOfPositions.add(mapPositions);
                        }
                    }
                    autoAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, searchResults);
                    //autoAdapter.notifyDataSetChanged();
                    tv_SearchAddress.setAdapter(autoAdapter);
                    if (selectedAddressFromMap.isEmpty()) {
                        tv_SearchAddress.showDropDown();
                    }
                }
            }
        }  catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " parseAddressesFromResponse Exception: " + ex.getMessage());
        }
    }

    public void welcomeDialog() {

        final Timer timer = new Timer();
        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(AddNewLinkToCloud.this);
        String message = getResources().getString(R.string.AddNewLinkWelcomeMessage);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();

                        if (timer != null) {
                            timer.cancel();
                        }
                    }
                }
        );

        androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                timer.cancel();
            }
        }, 10000);

        alertDialog.show();
    }

    private void AddMultipleLinksDialog() {

        String message = getResources().getString(R.string.AddMultipleLinksMessage);

        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(AddNewLinkToCloud.this);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                        AppConstants.SHOW_WELCOME_DIALOG_FOR_ADD_NEW_LINK = false;
                        finish();
                        Intent in = new Intent(AddNewLinkToCloud.this, AddNewLinkToCloud.class);
                        startActivity(in);
                    }
                }
        );

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                        ProceedToLinkConfiguration(false);
                    }
                }
        );
        androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void ProceedToLinkConfiguration(boolean flagForWait) {
        try {

            int waitingTimeInMillis = 100;
            if (flagForWait) {
                //Thread.sleep(3000);
                waitingTimeInMillis = 1000;
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (AppConstants.NEWLY_ADDED_LINKS != null) {
                        if (AppConstants.NEWLY_ADDED_LINKS.size() > 0) {
                            for (HashMap<String, String> hashMap : AppConstants.NEWLY_ADDED_LINKS) {
                                String linkName = hashMap.get("LinkName");
                                String siteId = hashMap.get("SiteId");
                                String HubLinkCommunication = "HTTP";
                                if (linkName != null && linkName.toUpperCase().contains("FS-")) {
                                    HubLinkCommunication = "HTTP";
                                } else {
                                    HubLinkCommunication = "BT";
                                }

                                if (!configurationProcessStarted) {
                                    configurationProcessStarted = true;
                                }
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + " Proceed to Link Configuration: " + linkName);
                                if (HubLinkCommunication.equalsIgnoreCase("BT")) {
                                    // BT link Configuration
                                    AppConstants.NEWLY_ADDED_LINKS.remove(hashMap);
                                    CommonUtils.AutoCloseBTLinkMessage(AddNewLinkToCloud.this, "", getResources().getString(R.string.BTLinkNotInPairList));

                                } else {
                                    // HTTP link Configuration
                                    AppConstants.CURRENT_NEW_LINK_SELECTED_FOR_CONFIGURE = linkName;
                                    AppConstants.CURRENT_NEW_LINK_SITE_ID = siteId;
                                    enableHotspotAfterNewLinkConfigure = true;

                                    String s = "Starting to configure " + linkName;
                                    SpannableString ss2 = new SpannableString(s);
                                    ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
                                    ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
                                    loading = new ProgressDialog(AddNewLinkToCloud.this);
                                    loading.setMessage(ss2);
                                    loading.setCancelable(false);
                                    loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    loading.show();

                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            AppConstants.NEWLY_ADDED_LINKS.remove(hashMap);
                                            HTTPLinkConfiguration();
                                        }
                                    }, 2000);
                                }
                                break;
                            }
                        } else {
                            BackToWelcomeActivity();
                        }
                    } else {
                        BackToWelcomeActivity();
                    }
                }
            }, waitingTimeInMillis);

        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " ProceedToLinkConfiguration Exception: " + ex.getMessage());
        }
    }

    private void BackToWelcomeActivity() {
        CommonUtils.hideKeyboard(AddNewLinkToCloud.this);
        int waitingTime = 100;
        if(enableHotspotAfterNewLinkConfigure) {
            waitingTime = 2000;
            Constants.HOTSPOT_STAY_ON = true;
            skipOnResume = true;
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " <Enabling hotspot.>");
            wifiApManager.setWifiApEnabled(null, true);  //Hotspot enabled
        }
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (AppConstants.LANGUAGE_CHANGED) {
                    AppConstants.LANGUAGE_CHANGED = false;
                    Intent i = new Intent(AddNewLinkToCloud.this, WelcomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
                finish();
            }
        }, waitingTime);
    }

    private void HTTPLinkConfiguration() {
        try {

            SharedPreferences sharedPref = AddNewLinkToCloud.this.getSharedPreferences("HotSpotDetails", Context.MODE_PRIVATE);
            String HotSpotSSID = sharedPref.getString("HotSpotSSID", "");
            String HotSpotPassword = sharedPref.getString("HotSpotPassword", "");
            if (HotSpotSSID == null) {
                HotSpotSSID = "";
            }
            if (HotSpotPassword == null) {
                HotSpotPassword = "";
            }
            if (HotSpotSSID.isEmpty()) {
                if (loading != null) {
                    loading.dismiss();
                }
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " HotSpot SSID cannot be blank. Please contact Support.");
                showMessageDialog(AddNewLinkToCloud.this, getResources().getString(R.string.HotSpotSSIDCannotBeBlank));
            } else if (HotSpotPassword.isEmpty()) {
                if (loading != null) {
                    loading.dismiss();
                }
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " HotSpot Password cannot be blank. Please contact Support.");
                showMessageDialog(AddNewLinkToCloud.this, getResources().getString(R.string.HotSpotPasswordCannotBeBlank));
            } else {

                configurationProcess();
            }

        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " HTTPLinkConfiguration Exception: " + ex.getMessage());
            ProceedToLinkConfiguration(true); // Continue to configure next link
        }
    }

    private void configurationProcess() {

        try {
            Constants.HOTSPOT_STAY_ON = false; //hotspot enable/disable flag

            if (CommonUtils.isHotspotEnabled(AddNewLinkToCloud.this)) {
                skipOnResume = true;
                wifiApManager.setWifiApEnabled(null, false);  //Disabled Hotspot
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Enable wifi
            ChangeWifiState(true);

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (loading != null) {
                        loading.dismiss();
                    }
                    countDownTimerForConfigure = null;
                    countDownTimerForConfigureFun();

                }
            }, 3000);

        } catch (Exception e) {
            ChangeWifiState(false);//turn wifi off
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " Link Configuration process -Step 1 Exception: " + e.getMessage());
            ProceedToLinkConfiguration(true); // Continue to configure next link
        }
    }

    public void countDownTimerForConfigureFun() {

        String s = getResources().getString(R.string.PleaseWaitForWifiConnect);
        SpannableString ss2 = new SpannableString(s);
        ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
        ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
        loading = new ProgressDialog(AddNewLinkToCloud.this);
        loading.setMessage(ss2);
        loading.setCancelable(false);
        loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loading.show();

        Constants.HOTSPOT_STAY_ON = false; //hotspot enable/disable flag
        if (CommonUtils.isHotspotEnabled(AddNewLinkToCloud.this)) {
            skipOnResume = true;
            wifiApManager.setWifiApEnabled(null, false);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ChangeWifiState(true);

        if (countDownTimerForConfigure == null) {
            countDownTimerForConfigure = new CountDownTimer(30000, 6000) {

                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {

                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String ssid = wifiInfo.getSSID();

                    ssid = ssid.replace("\"", "");

                    if (ssid.equalsIgnoreCase(AppConstants.CURRENT_NEW_LINK_SELECTED_FOR_CONFIGURE)) {
                        if (loading != null)
                            loading.dismiss();
                        new WiFiConnectTask().execute();

                    } else {
                        Constants.HOTSPOT_STAY_ON = false;
                        configurationProcessStarted = false;
                        if (loading != null)
                            loading.dismiss();
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " Unable to auto connect to " + AppConstants.CURRENT_NEW_LINK_SELECTED_FOR_CONFIGURE +". Started manual process..");
                        AppConstants.colorToastBigFont(AddNewLinkToCloud.this, getResources().getString(R.string.UnableToAutoConnect) + " " + AppConstants.CURRENT_NEW_LINK_SELECTED_FOR_CONFIGURE + ". " + getResources().getString(R.string.StartedManualProcess), Color.BLUE);
                        LinkConfigurationProcessStep1();
                    }
                }
            }.start();
        }
    }

    private void ChangeWifiState(boolean enable) {

        skipOnResume = true;
        WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (enable) {
            //Enable wifi
            if (!wifiManagerMM.isWifiEnabled()) {
                wifiManagerMM.setWifiEnabled(true);
            }
        } else {
            //Disable wifi
            if (wifiManagerMM.isWifiEnabled()) {
                wifiManagerMM.setWifiEnabled(false);
            }
        }
    }

    private void LinkConfigurationProcessStep1() {

        try {

            Constants.HOTSPOT_STAY_ON = false; //hotspot enable/disable flag
            if (CommonUtils.isHotspotEnabled(AddNewLinkToCloud.this)) {
                skipOnResume = true;
                wifiApManager.setWifiApEnabled(null, false);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Enable wifi
            ChangeWifiState(true);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " Step1 Link Configuration enable wifi manually.");

                    AppConstants.colorToastBigFont(AddNewLinkToCloud.this, getResources().getString(R.string.EnableWifiManually) + " " + AppConstants.CURRENT_NEW_LINK_SELECTED_FOR_CONFIGURE + " " + getResources().getString(R.string.UsingWifiList), Color.BLUE);
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    configurationProcessStarted = false;
                    ConfigurationStep1IsInProgress = true;
                    //mjconf
                    new CountDownTimer(12000, 6000) {

                        public void onTick(long millisUntilFinished) {

                            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            String ssid = "";
                            if (wifiManager.isWifiEnabled()) {
                                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                                ssid = wifiInfo.getSSID().trim().replace("\"", "");
                            }

                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " Selected SSID: " + AppConstants.CURRENT_NEW_LINK_SELECTED_FOR_CONFIGURE +"; Connected to: " + ssid);
                        }

                        public void onFinish() {
                            ConfigurationStep1IsInProgress = false;
                            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            String ssid = "";
                            if (wifiManager.isWifiEnabled()) {
                                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                                ssid = wifiInfo.getSSID().trim().replace("\"", "");
                            }

                            if (ssid.equalsIgnoreCase(AppConstants.CURRENT_NEW_LINK_SELECTED_FOR_CONFIGURE)) {
                                if (loading != null) {
                                    loading.dismiss();
                                }
                                proceedAfterManualWifiConnect = false;
                                new WiFiConnectTask().execute();

                            } else {
                                if (loading != null) {
                                    loading.dismiss();
                                }
                                /*ChangeWifiState(false);//turn wifi off*/
                                proceedAfterManualWifiConnect = true;
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " Step1 => Selected SSID: " + AppConstants.CURRENT_NEW_LINK_SELECTED_FOR_CONFIGURE +"; Connected to: " + ssid);
                                //ProceedToLinkConfiguration(true); // Continue to configure next link
                            }
                        }
                    }.start();
                }
            }, 2000);

        } catch (Exception e) {
            ChangeWifiState(false);//turn wifi off
            ConfigurationStep1IsInProgress = false;
            Log.i(TAG, "Link Configuration process -Step 1 Exception" + e);
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + " Link Configuration process -Step 1 Exception " + e.getMessage());
            ProceedToLinkConfiguration(true); // Continue to configure next link
        }
    }

    private class WiFiConnectTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " Started configuration process...");
            String s = "Started configuration process. Please wait...";
            SpannableString ss2 = new SpannableString(s);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);

            loading = new ProgressDialog(AddNewLinkToCloud.this);
            loading.setMessage(ss2);
            loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            loading.setCancelable(false);
            loading.show();
        }

        protected String doInBackground(String... asd) {
            return "";
        }

        @Override
        protected void onPostExecute(String s) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    WifiManager wifiManager = (WifiManager) AddNewLinkToCloud.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                    String ssid = wifiInfo.getSSID().replace("\"", "");

                    if (ssid.equalsIgnoreCase(AppConstants.CURRENT_NEW_LINK_SELECTED_FOR_CONFIGURE)) {

                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " Connected to wifi " + AppConstants.CURRENT_NEW_LINK_SELECTED_FOR_CONFIGURE);
                        AppConstants.colorToastBigFont(AddNewLinkToCloud.this, getResources().getString(R.string.ConnectedToWifi) + " " + AppConstants.CURRENT_NEW_LINK_SELECTED_FOR_CONFIGURE, Color.parseColor("#4CAF50"));

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                HTTP_URL = "http://192.168.4.1:80/";
                                URL_INFO = HTTP_URL + "client?command=info";
                                try {
                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " Sending INFO command to Link: " + AppConstants.CURRENT_NEW_LINK_SELECTED_FOR_CONFIGURE);
                                    String result = new CommandsGET_INFO().execute(URL_INFO).get();
                                    String mac_address = "";

                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " InfoCMD- " + ssid + " -Result >> " + result);

                                    if (result.contains("Version")) {
                                        JSONObject jsonObject = new JSONObject(result);
                                        JSONObject joPulsarStat = jsonObject.getJSONObject("Version");
                                        mac_address = joPulsarStat.getString("mac_address");//station_mac_address
                                        AppConstants.NEW_LINK_UPDATE_MAC_ADDRESS = mac_address;

                                        if (mac_address.equals("")) {
                                            if (loading != null) {
                                                loading.dismiss();
                                            }
                                            if (AppConstants.GENERATE_LOGS)
                                                AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " Configuration process fail. Could not get mac address.");
                                            AppConstants.colorToastBigFont(AddNewLinkToCloud.this, "Configuration process fail. Could not get mac address.", Color.BLUE);

                                            //Disable wifi connection
                                            ChangeWifiState(false);
                                            ProceedToLinkConfiguration(true); // Continue to configure next link

                                        } else {

                                            //Set username and password to link
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {

                                                    SharedPreferences sharedPref = AddNewLinkToCloud.this.getSharedPreferences("HotSpotDetails", Context.MODE_PRIVATE);
                                                    String HotSpotSSID = sharedPref.getString("HotSpotSSID", "");
                                                    String HotSpotPassword = sharedPref.getString("HotSpotPassword", "");

                                                    if (AppConstants.GENERATE_LOGS)
                                                        AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " Setting SSID and PASS to Link");
                                                    AppConstants.colorToastBigFont(AddNewLinkToCloud.this, getResources().getString(R.string.SettingSSIDAndPASS), Color.BLUE);

                                                    HTTP_URL = "http://192.168.4.1:80/";
                                                    URL_UPDATE_FS_INFO = HTTP_URL + "config?command=wifi";

                                                    String jsonChangeUsernamePass = "{\"Request\":{\"Station\":{\"Connect_Station\":{\"ssid\":\"" + HotSpotSSID + "\",\"password\":\"" + HotSpotPassword + "\" ,\"sta_connect\":1 }}}}";

                                                    try {
                                                        new CommandsPOST_ChangeHotspotSettings().execute(URL_UPDATE_FS_INFO, jsonChangeUsernamePass);

                                                    } catch (Exception e) {
                                                        if (loading != null) {
                                                            loading.dismiss();
                                                        }
                                                        ChangeWifiState(false);//turn wifi off
                                                        if (AppConstants.GENERATE_LOGS)
                                                            AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " CommandsPOST_ChangeHotspotSettings. Exception: " + e.getMessage());
                                                        ProceedToLinkConfiguration(true); // Continue to configure next link
                                                    }
                                                }
                                            }, 1000);

                                        }
                                    } else {
                                        if (loading != null) {
                                            loading.dismiss();
                                        }
                                        if (AppConstants.GENERATE_LOGS)
                                            AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " Configuration process fail.");
                                        AppConstants.colorToastBigFont(AddNewLinkToCloud.this, "Configuration process fail. Please retry.", Color.BLUE);

                                        //Disable wifi connection
                                        ChangeWifiState(false);
                                        ProceedToLinkConfiguration(true); // Continue to configure next link
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    if (loading != null)
                                        loading.dismiss();
                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " WiFiConnectTask OnPostExecution --Exception: " + e.getMessage());
                                }
                            }
                        }, 1000);

                    } else {
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " WiFiConnectTask => Selected SSID: " + AppConstants.CURRENT_NEW_LINK_SELECTED_FOR_CONFIGURE +"; WiFi Connected to: " + ssid);
                        AppConstants.colorToastBigFont(AddNewLinkToCloud.this, " Selected SSID: " + AppConstants.CURRENT_NEW_LINK_SELECTED_FOR_CONFIGURE +"; WiFi Connected to: " + ssid, Color.BLUE);
                        if (loading != null)
                            loading.dismiss();
                        ChangeWifiState(false);//turn wifi off
                        ProceedToLinkConfiguration(true); // Continue to configure next link
                    }
                }
            }, 5000);

        }
    }

    public class CommandsGET_INFO extends AsyncTask<String, Void, String> {

        public String resp = "";

        protected String doInBackground(String... param) {

            try {

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(15, TimeUnit.SECONDS);
                client.setReadTimeout(15, TimeUnit.SECONDS);
                client.setWriteTimeout(15, TimeUnit.SECONDS);

                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                ChangeWifiState(false);//turn wifi off
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " CommandsGET_INFO InBackground --Exception: " + e.getMessage());
                if (loading != null) {
                    loading.dismiss();
                }
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                System.out.println(" resp......." + result);
                System.out.println("2:" + Calendar.getInstance().getTime());
            } catch (Exception e) {
                ChangeWifiState(false);//turn wifi off
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + " CommandsGET_INFO PostExecute --Exception: " + e.getMessage());
                Log.d("Ex", e.getMessage());
            }
        }
    }

    public class CommandsPOST_ChangeHotspotSettings extends AsyncTask<String, Void, String> {

        public String resp = "";

        protected String doInBackground(String... param) {

            try {
                MediaType JSON = MediaType.parse("application/json");
                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(4, TimeUnit.SECONDS);
                client.setReadTimeout(4, TimeUnit.SECONDS);
                client.setWriteTimeout(4, TimeUnit.SECONDS);

                RequestBody body = RequestBody.create(JSON, param[1]);
                Request request = new Request.Builder()
                        .url(param[0])
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {

                ChangeWifiState(false);//turn wifi off
                e.printStackTrace();
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " Set SSID and PASS to Link (Link reset) InBackground-Exception: " + e.getMessage());
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            try {

                if (result.equalsIgnoreCase("exception")) {
                    ChangeWifiState(false);//turn wifi off
                    AppConstants.colorToastBigFont(AddNewLinkToCloud.this, "Configuration process fail. Please retry.", Color.BLUE);
                    Log.i(TAG, "Step2 Failed while changing Hotspot Settings Please try again.. exception:" + result);
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " Step2 Failed while changing Hotspot Settings Please try again.. exception: " + result);
                    if (loading != null)
                        loading.dismiss();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    ProceedToLinkConfiguration(true); // Continue to configure next link

                } else {

                    ChangeWifiState(false);//turn wifi off
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //wifiApManager.setWifiApEnabled(null, true);

                    Log.i(TAG, " Set SSID and PASS to Link (Result) " + result);
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " Set SSID and PASS to Link Result >> " + result);

                    //============================================================

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            AppConstants.colorToastBigFont(AddNewLinkToCloud.this, getResources().getString(R.string.MacAddressHeading) + " " + AppConstants.NEW_LINK_UPDATE_MAC_ADDRESS, Color.BLUE);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " Mac address: " + AppConstants.NEW_LINK_UPDATE_MAC_ADDRESS);

                            //Update mac address to server and mac address status
                            try {

                                UpdateMacAddressClass authEntityClass1 = new UpdateMacAddressClass();
                                authEntityClass1.SiteId = Integer.parseInt(AppConstants.CURRENT_NEW_LINK_SITE_ID);
                                authEntityClass1.MACAddress = AppConstants.NEW_LINK_UPDATE_MAC_ADDRESS;
                                authEntityClass1.RequestFrom = "AP";
                                authEntityClass1.HubName = AppConstants.HUB_NAME;

                                //------
                                Gson gson = new Gson();
                                final String jsonData = gson.toJson(authEntityClass1);

                                CommonUtils.saveLinkMacAddressForReconfigure(AddNewLinkToCloud.this, jsonData);

                                cd = new ConnectionDetector(AddNewLinkToCloud.this);
                                if (cd.isConnectingToInternet()) {

                                    new UpdateMacAsyncTask().execute(jsonData);

                                } else {
                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + "Please check Internet Connection.");
                                    AppConstants.colorToast(AddNewLinkToCloud.this, getResources().getString(R.string.CheckInternet), Color.BLUE);
                                }
                            } catch (Exception e) {
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " ChangeHotspotSettings UpdateMacAddressClass --Exception: " + e.getMessage());
                            }

                            if (loading != null)
                                loading.dismiss();
                            ProceedToLinkConfiguration(true); // Continue to configure next link
                        }
                    }, 10000);
                }

            } catch (Exception e) {
                ChangeWifiState(false);//turn wifi off
                e.printStackTrace();
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " Set SSID and PASS to Link (Link reset) -Exception: " + e.getMessage());
            }
        }
    }

    public class UpdateMacAsyncTask extends AsyncTask<String, Void, String> {

        public String response = null;

        @Override
        protected String doInBackground(String... param) {

            try {
                ServerHandler serverHandler = new ServerHandler();
                String jsonData = param[0];
                String userEmail = CommonUtils.getCustomerDetails(AddNewLinkToCloud.this).PersonEmail;

                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(AddNewLinkToCloud.this) + ":" + userEmail + ":" + "UpdateMACAddress" + AppConstants.LANG_PARAM);
                response = serverHandler.PostTextData(AddNewLinkToCloud.this, AppConstants.WEB_URL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {
                if (loading != null)
                    loading.dismiss();
                CommonUtils.LogMessage("", "UpdateMACAddress ", ex);
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " UpdateMacAsyncTask InBackground--Exception: " + ex.getMessage());
                response = "err";
            }
            return response;
        }

        @Override
        protected void onPostExecute(String serverRes) {

            try {
                if (serverRes.equalsIgnoreCase("err")) {
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " Link Re-configuration is partially completed.");
                    // AppConstants.alertBigFinishActivity(AddNewLinkToCloud.this, getResources().getString(R.string.PartiallyCompleted)); // Removed this to avoid app close
                } else if (serverRes != null) {

                    JSONObject jsonObject1 = new JSONObject(serverRes);

                    String ResponceMessage = jsonObject1.getString("ResponceMessage");

                    if (ResponceMessage.equalsIgnoreCase("success")) {

                        AppConstants.clearSharedPrefByName(AddNewLinkToCloud.this, Constants.MAC_ADDRESS_RECONFIGURE);

                        if (loading != null)
                            loading.dismiss();
                        AppConstants.colorToastBigFont(AddNewLinkToCloud.this, getResources().getString(R.string.MacAddressUpdated), Color.parseColor("#4CAF50"));
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " Mac Address Updated.");
                        ChangeWifiState(false);

                    } else if (ResponceMessage.equalsIgnoreCase("fail")) {
                        if (loading != null)
                            loading.dismiss();
                        AppConstants.colorToastBigFont(AddNewLinkToCloud.this, getResources().getString(R.string.MacAddressNotUpdated), Color.BLUE);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " MAC address could not be updated.");
                        ChangeWifiState(false);
                    }

                } else {
                    Log.i(TAG, "UpdateMacAsyncTask Server Response Empty!");
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " UpdateMacAsyncTask Server Response Empty!");
                }

            } catch (Exception e) {

                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(AppConstants.LOG_RECONFIG + "-" + TAG + " UpdateMacAsyncTask onPostExecute--Exception: " + e.getMessage());
            }
        }
    }

    public void showMessageDialog(final Activity context, String message) {

        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();

                        ProceedToLinkConfiguration(false); // Continue to configure next link
                    }
                }
        );

        androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
