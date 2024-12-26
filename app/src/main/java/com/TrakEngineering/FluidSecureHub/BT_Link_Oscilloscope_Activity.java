package com.TrakEngineering.FluidSecureHub;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.TrakEngineering.FluidSecureHub.BTSPP.BTConstants;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTSPPMain;
import com.TrakEngineering.FluidSecureHub.entity.LINKPulserDataEntity;
import com.TrakEngineering.FluidSecureHub.server.ServerHandler;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class BT_Link_Oscilloscope_Activity extends AppCompatActivity { // implements ServiceConnection

    private static final String TAG = "BTLink_Oscilloscope ";

    private LineChart myChart;
    private Button btnSet, btnStartScope, btnDisplay, btnReconnect;
    //public RadioGroup rdg_p_type;
    //public RadioButton rdSelectedType;
    String linkPosition, WifiSSId;
    int scopeWaitCounter = 0, stopCounter = 0;
    int readCounter = 0;
    //public boolean chartBindStarted = false;
    private Timer timerBtScope;
    private String p_type = "";
    private static ArrayList<Double> BTLinkVoltageReadings = new ArrayList<>();
    ProgressDialog pdMain;
    private Spinner spin_pTypes;
    public String selectedPulserType = "1";

    public ArrayList<Entry> yValues = new ArrayList<>();
    public boolean isScopeRecordStarted = false;

    public BroadcastBlueLinkData broadcastBlueLinkData = null;
    public IntentFilter intentFilter;

    String Request = "", Response = "";
    public boolean flag = true;
    StringBuilder sBuilder = new StringBuilder();

    //======== p_settings ===========//
    private LinearLayout linearLayout_p_settings, linearPSetting_time, linearPSetting_delayTime, linearPSetting_lowAltitude;
    private LinearLayout linearPSetting_highAltitude, linearPSetting_lowSample, linearPSetting_highSample, linearPSetting_lowTotal;
    private LinearLayout linearPSetting_highTotal, linearPSetting_sampleRate;
    private TextView tvTime, tvDelayTime, tvLowAltitude, tvHighAltitude, tvLowSample, tvHighSample, tvLowTotal, tvHighTotal, tvSampleRate;
    private EditText etTime, etDelayTime, etLowAltitude, etHighAltitude, etLowSample, etHighSample, etLowTotal, etHighTotal, etSampleRate;
    //===============================//

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            unregisterBTReceiver();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_link_oscilloscope);

        linkPosition = getIntent().getExtras().getString("LinkPosition");
        WifiSSId = getIntent().getExtras().getString("WifiSSId");

        getSupportActionBar().setTitle("Oscilloscope for " + WifiSSId);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnSet = findViewById(R.id.btnSet);
        btnStartScope = findViewById(R.id.btnStartScope);
        btnDisplay = findViewById(R.id.btnDisplay);
        //btnDisplay.setAlpha(0.5f);
        //btnDisplay.setEnabled(false);

        initiatePSettingsControls();
        hidePSettingsControls();

        //rdg_p_type = (RadioGroup) findViewById(R.id.rdg_p_type);
        spin_pTypes = (Spinner) findViewById(R.id.spin_pTypes);
        btnReconnect = findViewById(R.id.btnReconnect);
        btnReconnect.setText("Reconnect");
        btnReconnect.setVisibility(View.INVISIBLE);

        myChart = findViewById(R.id.lineChart);

        myChart.setDragEnabled(true);
        myChart.setScaleEnabled(true);
        myChart.getDescription().setText("");
        myChart.getXAxis().setTextSize(12);
        //myChart.getDescription().setTextSize(12f);

        initChart();

        if (linkPosition == null) {
            linkPosition = "0";
        }

        registerBTReceiver(linkPosition);

        ArrayAdapter<String> aaPR = new ArrayAdapter<>(this, R.layout.spinner_item_list, BTConstants.P_TYPES);
        aaPR.setDropDownViewResource(R.layout.spinner_item_list);
        spin_pTypes.setAdapter(aaPR);
        //spin_pTypes.setSelection(1);

        spin_pTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedPulserType = spin_pTypes.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        btnStartScope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + "Start button clicked");
                    clearPreviousChartAndData();

                    //btnDisplay.setAlpha(0.5f); // #2240
                    //btnDisplay.setEnabled(false);

                    scopeOnCommand();

                } catch (Exception ex) {
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + "Exception in btnStartScope click: " + ex.getMessage());
                }
            }
        });

        btnDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + "Display button clicked");
                    clearPreviousChartAndData();

                    scopeReadCommand();
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + "Read started.");
                    showLoader(getResources().getString(R.string.PleaseWait));

                    timerBtScope = new Timer();
                    TimerTask tt = new TimerTask() {
                        @RequiresApi(api = Build.VERSION_CODES.P)
                        @Override
                        public void run() {
                            if (BTConstants.SCOPE_STATUS.equalsIgnoreCase("DONE")) {
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + "Read end. (" + readCounter + " seconds)");
                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        showToast(BT_Link_Oscilloscope_Activity.this, "done");
                                        bindChartData();

                                        if (AppConstants.GENERATE_LOGS)
                                            AppConstants.writeInFile(TAG + "<Voltage Readings count: " + BTLinkVoltageReadings.size() + ">");

                                        if (BTLinkVoltageReadings.size() > 0) { // To avoid sending empty data to the server.
                                            sendReadingsToServer();
                                        }
                                    }
                                }, 1000);
                                hideLoader();
                                cancel();
                            } else {
                                readCounter++;
                            }

                            /*if (readCounter > 60) { // #2240
                                if (AppConstants.GENERATE_LOGS)
                                    AppConstants.writeInFile(TAG + "Read process not completed.");
                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(BT_Link_Oscilloscope_Activity.this, "Please try again.", Toast.LENGTH_LONG).show();
                                    }
                                }, 50);
                                hideLoader();
                                cancel();
                            }*/
                        }
                    };
                    timerBtScope.schedule(tt, 1000, 1000);

                } catch (Exception ex) {
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + "Exception in btnDisplay click: " + ex.getMessage());
                }
            }

        });

        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setP_TypeForLink();
            }
        });

        btnReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "<Reconnecting to the Link...>");

                showLoader(getResources().getString(R.string.ConnectingToTheLINK) + " " + getResources().getString(R.string.PleaseWait));

                btLinkReconnection(linkPosition);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideLoader();
                    }
                }, 7000);
            }
        });
    }

    private void initiatePSettingsControls() {
        linearLayout_p_settings = findViewById(R.id.linearLayout_p_settings);
        linearPSetting_time = findViewById(R.id.linearPSetting_time);
        linearPSetting_delayTime = findViewById(R.id.linearPSetting_delayTime);
        linearPSetting_lowAltitude = findViewById(R.id.linearPSetting_lowAltitude);
        linearPSetting_highAltitude = findViewById(R.id.linearPSetting_highAltitude);
        linearPSetting_lowSample = findViewById(R.id.linearPSetting_lowSample);
        linearPSetting_highSample = findViewById(R.id.linearPSetting_highSample);
        linearPSetting_lowTotal = findViewById(R.id.linearPSetting_lowTotal);
        linearPSetting_highTotal = findViewById(R.id.linearPSetting_highTotal);
        linearPSetting_sampleRate = findViewById(R.id.linearPSetting_sampleRate);

        tvTime = findViewById(R.id.tvTime);
        tvDelayTime = findViewById(R.id.tvDelayTime);
        tvLowAltitude = findViewById(R.id.tvLowAltitude);
        tvHighAltitude = findViewById(R.id.tvHighAltitude);
        tvLowSample = findViewById(R.id.tvLowSample);
        tvHighSample = findViewById(R.id.tvHighSample);
        tvLowTotal = findViewById(R.id.tvLowTotal);
        tvHighTotal = findViewById(R.id.tvHighTotal);
        tvSampleRate = findViewById(R.id.tvSampleRate);

        etTime = findViewById(R.id.etTime);
        etDelayTime = findViewById(R.id.etDelayTime);
        etLowAltitude = findViewById(R.id.etLowAltitude);
        etHighAltitude = findViewById(R.id.etHighAltitude);
        etLowSample = findViewById(R.id.etLowSample);
        etHighSample = findViewById(R.id.etHighSample);
        etLowTotal = findViewById(R.id.etLowTotal);
        etHighTotal = findViewById(R.id.etHighTotal);
        etSampleRate = findViewById(R.id.etSampleRate);

        CheckBox chkTime = findViewById(R.id.chkTime);
        CheckBox chkDelayTime = findViewById(R.id.chkDelayTime);
        CheckBox chkLowAltitude = findViewById(R.id.chkLowAltitude);
        CheckBox chkHighAltitude = findViewById(R.id.chkHighAltitude);
        CheckBox chkLowSample = findViewById(R.id.chkLowSample);
        CheckBox chkHighSample = findViewById(R.id.chkHighSample);
        CheckBox chkLowTotal = findViewById(R.id.chkLowTotal);
        CheckBox chkHighTotal = findViewById(R.id.chkHighTotal);
        CheckBox chkSampleRate = findViewById(R.id.chkSampleRate);

        chkTime.setEnabled(false);
        chkDelayTime.setEnabled(false);
        chkLowAltitude.setEnabled(false);
        chkHighAltitude.setEnabled(false);
        chkLowSample.setEnabled(false);
        chkHighSample.setEnabled(false);
        chkLowTotal.setEnabled(false);
        chkHighTotal.setEnabled(false);
        chkSampleRate.setEnabled(false);

        //================== CheckBox Events =========================//
        chkTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Log.d(TAG, "chkTime onCheckedChanged: " + isChecked);
                CommonUtils.hideKeyboard(BT_Link_Oscilloscope_Activity.this);
                if (isChecked) {
                    String enteredTime = etTime.getText().toString().trim();
                    if (enteredTime.isEmpty()) {
                        Toast.makeText(BT_Link_Oscilloscope_Activity.this, "Please enter new value.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "<Previous Value: " + tvTime.getText() + "; New Entered Value: " + enteredTime + "; Selected Pulser Type: " + selectedPulserType + ">");
                        String newValue = "time=" + enteredTime + ";";
                        String command = BTConstants.SET_P_SETTINGS_COMMAND.replace("X", selectedPulserType).replace("Y", newValue);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "Sending set p_settings (Time) command to Link: " + WifiSSId);
                        showLoader(getResources().getString(R.string.PleaseWait));
                        sendBTCommands(linkPosition, command);
                    }
                }
            }
        });

        chkDelayTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Log.d(TAG, "chkDelayTime onCheckedChanged: " + isChecked);
                CommonUtils.hideKeyboard(BT_Link_Oscilloscope_Activity.this);
                if (isChecked) {
                    String enteredDelayTime = etDelayTime.getText().toString().trim();
                    if (enteredDelayTime.isEmpty()) {
                        Toast.makeText(BT_Link_Oscilloscope_Activity.this, "Please enter new value.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "<Previous Value: " + tvDelayTime.getText() + "; New Entered Value: " + enteredDelayTime + "; Selected Pulser Type: " + selectedPulserType + ">");
                        String newValue = "time=" + enteredDelayTime + ";";
                        String command = BTConstants.SET_P_SETTINGS_COMMAND.replace("X", selectedPulserType).replace("Y", newValue);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "Sending set p_settings (DelayTime) command to Link: " + WifiSSId);
                        showLoader(getResources().getString(R.string.PleaseWait));
                        sendBTCommands(linkPosition, command);
                    }
                }
            }
        });

        chkLowAltitude.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Log.d(TAG, "chkLowAltitude onCheckedChanged: " + isChecked);
                CommonUtils.hideKeyboard(BT_Link_Oscilloscope_Activity.this);
                if (isChecked) {
                    String enteredLowAltitude = etLowAltitude.getText().toString().trim();
                    if (enteredLowAltitude.isEmpty()) {
                        Toast.makeText(BT_Link_Oscilloscope_Activity.this, "Please enter new value.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "<Previous Value: " + tvLowAltitude.getText() + "; New Entered Value: " + enteredLowAltitude + "; Selected Pulser Type: " + selectedPulserType + ">");
                        String newValue = "la=" + enteredLowAltitude + ";";
                        String command = BTConstants.SET_P_SETTINGS_COMMAND.replace("X", selectedPulserType).replace("Y", newValue);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "Sending set p_settings (LowAltitude) command to Link: " + WifiSSId);
                        showLoader(getResources().getString(R.string.PleaseWait));
                        sendBTCommands(linkPosition, command);
                    }
                }
            }
        });

        chkHighAltitude.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Log.d(TAG, "chkHighAltitude onCheckedChanged: " + isChecked);
                CommonUtils.hideKeyboard(BT_Link_Oscilloscope_Activity.this);
                if (isChecked) {
                    String enteredHighAltitude = etHighAltitude.getText().toString().trim();
                    if (enteredHighAltitude.isEmpty()) {
                        Toast.makeText(BT_Link_Oscilloscope_Activity.this, "Please enter new value.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "<Previous Value: " + tvHighAltitude.getText() + "; New Entered Value: " + enteredHighAltitude + "; Selected Pulser Type: " + selectedPulserType + ">");
                        String newValue = "ha=" + enteredHighAltitude + ";";
                        String command = BTConstants.SET_P_SETTINGS_COMMAND.replace("X", selectedPulserType).replace("Y", newValue);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "Sending set p_settings (HighAltitude) command to Link: " + WifiSSId);
                        showLoader(getResources().getString(R.string.PleaseWait));
                        sendBTCommands(linkPosition, command);
                    }
                }
            }
        });

        chkLowSample.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Log.d(TAG, "chkLowSample onCheckedChanged: " + isChecked);
                CommonUtils.hideKeyboard(BT_Link_Oscilloscope_Activity.this);
                if (isChecked) {
                    String enteredLowSample = etLowSample.getText().toString().trim();
                    if (enteredLowSample.isEmpty()) {
                        Toast.makeText(BT_Link_Oscilloscope_Activity.this, "Please enter new value.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "<Previous Value: " + tvLowSample.getText() + "; New Entered Value: " + enteredLowSample + "; Selected Pulser Type: " + selectedPulserType + ">");
                        String newValue = "lsn=" + enteredLowSample + ";";
                        String command = BTConstants.SET_P_SETTINGS_COMMAND.replace("X", selectedPulserType).replace("Y", newValue);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "Sending set p_settings (LowSample) command to Link: " + WifiSSId);
                        showLoader(getResources().getString(R.string.PleaseWait));
                        sendBTCommands(linkPosition, command);
                    }
                }
            }
        });

        chkHighSample.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Log.d(TAG, "chkHighSample onCheckedChanged: " + isChecked);
                CommonUtils.hideKeyboard(BT_Link_Oscilloscope_Activity.this);
                if (isChecked) {
                    String enteredHighSample = etHighSample.getText().toString().trim();
                    if (enteredHighSample.isEmpty()) {
                        Toast.makeText(BT_Link_Oscilloscope_Activity.this, "Please enter new value.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "<Previous Value: " + tvHighSample.getText() + "; New Entered Value: " + enteredHighSample + "; Selected Pulser Type: " + selectedPulserType + ">");
                        String newValue = "hsn=" + enteredHighSample + ";";
                        String command = BTConstants.SET_P_SETTINGS_COMMAND.replace("X", selectedPulserType).replace("Y", newValue);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "Sending set p_settings (HighSample) command to Link: " + WifiSSId);
                        showLoader(getResources().getString(R.string.PleaseWait));
                        sendBTCommands(linkPosition, command);
                    }
                }
            }
        });

        chkLowTotal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Log.d(TAG, "chkLowTotal onCheckedChanged: " + isChecked);
                CommonUtils.hideKeyboard(BT_Link_Oscilloscope_Activity.this);
                if (isChecked) {
                    String enteredLowTotal = etLowTotal.getText().toString().trim();
                    if (enteredLowTotal.isEmpty()) {
                        Toast.makeText(BT_Link_Oscilloscope_Activity.this, "Please enter new value.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "<Previous Value: " + tvLowTotal.getText() + "; New Entered Value: " + enteredLowTotal + "; Selected Pulser Type: " + selectedPulserType + ">");
                        String newValue = "ltn=" + enteredLowTotal + ";";
                        String command = BTConstants.SET_P_SETTINGS_COMMAND.replace("X", selectedPulserType).replace("Y", newValue);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "Sending set p_settings (LowTotal) command to Link: " + WifiSSId);
                        showLoader(getResources().getString(R.string.PleaseWait));
                        sendBTCommands(linkPosition, command);
                    }
                }
            }
        });

        chkHighTotal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Log.d(TAG, "chkHighTotal onCheckedChanged: " + isChecked);
                CommonUtils.hideKeyboard(BT_Link_Oscilloscope_Activity.this);
                if (isChecked) {
                    String enteredHighTotal = etHighTotal.getText().toString().trim();
                    if (enteredHighTotal.isEmpty()) {
                        Toast.makeText(BT_Link_Oscilloscope_Activity.this, "Please enter new value.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "<Previous Value: " + tvHighTotal.getText() + "; New Entered Value: " + enteredHighTotal + "; Selected Pulser Type: " + selectedPulserType + ">");
                        String newValue = "htn=" + enteredHighTotal + ";";
                        String command = BTConstants.SET_P_SETTINGS_COMMAND.replace("X", selectedPulserType).replace("Y", newValue);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "Sending set p_settings (HighTotal) command to Link: " + WifiSSId);
                        showLoader(getResources().getString(R.string.PleaseWait));
                        sendBTCommands(linkPosition, command);
                    }
                }
            }
        });

        chkSampleRate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Log.d(TAG, "chkSampleRate onCheckedChanged: " + isChecked);
                CommonUtils.hideKeyboard(BT_Link_Oscilloscope_Activity.this);
                if (isChecked) {
                    String enteredSampleRate = etSampleRate.getText().toString().trim();
                    if (enteredSampleRate.isEmpty()) {
                        Toast.makeText(BT_Link_Oscilloscope_Activity.this, "Please enter new value.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "<Previous Value: " + tvSampleRate.getText() + "; New Entered Value: " + enteredSampleRate + "; Selected Pulser Type: " + selectedPulserType + ">");
                        String newValue = "sr=" + enteredSampleRate + ";";
                        String command = BTConstants.SET_P_SETTINGS_COMMAND.replace("X", selectedPulserType).replace("Y", newValue);
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "Sending set p_settings (SampleRate) command to Link: " + WifiSSId);
                        showLoader(getResources().getString(R.string.PleaseWait));
                        sendBTCommands(linkPosition, command);
                    }
                }
            }
        });
        //====================================================================//

        //================== EditText Events =========================//
        etTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                chkTime.setEnabled(s.length() > 0);
                if (s.length() == 0) {
                    chkTime.setChecked(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etDelayTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                chkDelayTime.setEnabled(s.length() > 0);
                if (s.length() == 0) {
                    chkDelayTime.setChecked(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etLowAltitude.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                chkLowAltitude.setEnabled(s.length() > 0);
                if (s.length() == 0) {
                    chkLowAltitude.setChecked(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etHighAltitude.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                chkHighAltitude.setEnabled(s.length() > 0);
                if (s.length() == 0) {
                    chkHighAltitude.setChecked(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etLowSample.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                chkLowSample.setEnabled(s.length() > 0);
                if (s.length() == 0) {
                    chkLowSample.setChecked(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etHighSample.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                chkHighSample.setEnabled(s.length() > 0);
                if (s.length() == 0) {
                    chkHighSample.setChecked(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etLowTotal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                chkLowTotal.setEnabled(s.length() > 0);
                if (s.length() == 0) {
                    chkLowTotal.setChecked(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etHighTotal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                chkHighTotal.setEnabled(s.length() > 0);
                if (s.length() == 0) {
                    chkHighTotal.setChecked(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etSampleRate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                chkSampleRate.setEnabled(s.length() > 0);
                if (s.length() == 0) {
                    chkSampleRate.setChecked(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        //====================================================================//
    }

    private void hidePSettingsControls() {
        linearLayout_p_settings.setVisibility(View.GONE);
        linearPSetting_time.setVisibility(View.GONE);
        linearPSetting_delayTime.setVisibility(View.GONE);
        linearPSetting_lowAltitude.setVisibility(View.GONE);
        linearPSetting_highAltitude.setVisibility(View.GONE);
        linearPSetting_lowSample.setVisibility(View.GONE);
        linearPSetting_highSample.setVisibility(View.GONE);
        linearPSetting_lowTotal.setVisibility(View.GONE);
        linearPSetting_highTotal.setVisibility(View.GONE);
        linearPSetting_sampleRate.setVisibility(View.GONE);
    }

    private void btLinkReconnection(String linkPosition) {
        try {
            BTSPPMain btspp = new BTSPPMain();
            btspp.activity = BT_Link_Oscilloscope_Activity.this;

            switch (linkPosition) {
                case "0"://Link 1
                    btspp.connect1();
                    break;
                case "1"://Link 2
                    btspp.connect2();
                    break;
                case "2"://Link 3
                    btspp.connect3();
                    break;
                case "3"://Link 4
                    btspp.connect4();
                    break;
                case "4"://Link 5
                    btspp.connect5();
                    break;
                case "5"://Link 6
                    btspp.connect6();
                    break;
                default://Something went wrong in link selection please try again.
                    break;
            }
        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Exception in btLinkReconnection: " + ex.getMessage());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        try {
            unregisterBTReceiver();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finish();
    }

    private void showLoader(String message) {

        SpannableString ss2 = new SpannableString(message);
        ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
        ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
        pdMain = new ProgressDialog(BT_Link_Oscilloscope_Activity.this);
        pdMain.setMessage(ss2);
        pdMain.setCancelable(false);
        pdMain.show();
    }

    private void hideLoader() {
        if (pdMain != null) {
            pdMain.dismiss();
        }
    }

    private void clearPreviousChartAndData() {
        try {
            BTLinkVoltageReadings.clear();
            resetChart();
            BTConstants.SCOPE_STATUS = "";
            readCounter = 0;
            sBuilder.setLength(0);
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Exception in clearPreviousChartAndData: " + e.getMessage());
        }
    }

    private void bindChartData() {
        try {
            generateChart();
            myChart.notifyDataSetChanged();
            myChart.invalidate();
            myChart.getAxisLeft().setAxisMinimum(0f);
            myChart.getAxisRight().setAxisMinimum(0f);
            myChart.setVisibleXRange(0, 30);
            //myChart.moveViewToX(BTLinkVoltageReadings.size() - 6);
            myChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Exception in bindChartData: " + e.getMessage());
        }
    }

    private void setP_TypeForLink() {
        try {
            hidePSettingsControls();
            p_type = "";
            //int selectedPType = rdg_p_type.getCheckedRadioButtonId();
            //rdSelectedType = (RadioButton) findViewById(selectedPType);

            String selectedType = selectedPulserType;
            /*if (rdSelectedType != null) {
                selectedType = rdSelectedType.getText().toString().trim();
            }*/
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Selected Type: " + selectedType);

            if (!selectedType.isEmpty()) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "Sending p_type command to Link: " + WifiSSId);
                sendBTCommands(linkPosition, BTConstants.P_TYPE_COMMAND + selectedType);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (p_type.isEmpty()) {
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + "Unable to set pulser type.");
                            Toast.makeText(BT_Link_Oscilloscope_Activity.this, "Unable to set pulser type. Please click the Reconnect button and try again.", Toast.LENGTH_LONG).show();
                        } else {
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + "Pulser type is set: " + p_type);
                            Toast.makeText(BT_Link_Oscilloscope_Activity.this, "Pulser Type: " + p_type, Toast.LENGTH_SHORT).show();
                            showLoader(getResources().getString(R.string.ConnectingToTheLINK) + " " + getResources().getString(R.string.PleaseWaitSeveralSeconds));

                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (AppConstants.GENERATE_LOGS)
                                        AppConstants.writeInFile(TAG + "<Reconnecting to the Link.>");

                                    btLinkReconnection(linkPosition);

                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            getPSettingsCommand();
                                            hideLoader();
                                        }
                                    }, 5000);
                                }
                            }, 7000);
                        }
                        btnReconnect.setVisibility(View.VISIBLE);
                    }
                }, 2000);
            } else {
                Toast.makeText(BT_Link_Oscilloscope_Activity.this, "Please select Type.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Exception in setP_TypeForLink: " + ex.getMessage());
        }
    }

    private void sendBTCommands(String linkPosition, String command) {
        try {
            BTSPPMain btspp = new BTSPPMain();
            btspp.activity = BT_Link_Oscilloscope_Activity.this;

            switch (linkPosition) {
                case "0"://Link 1
                    btspp.send1(command);
                    break;
                case "1"://Link 2
                    btspp.send2(command);
                    break;
                case "2"://Link 3
                    btspp.send3(command);
                    break;
                case "3"://Link 4
                    btspp.send4(command);
                    break;
                case "4"://Link 5
                    btspp.send5(command);
                    break;
                case "5"://Link 6
                    btspp.send6(command);
                    break;
                default://Something went wrong in link selection please try again.
                    break;
            }
        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Exception in sendBTCommands: " + ex.getMessage());
        }
    }

    private void showToast(Context ctx, String message) {
        Toast toast = Toast.makeText(ctx, " " + message + " ", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, -20, 130);
        toast.show();
    }

    private void scopeOnCommand() {
        try {
            btnReconnect.setVisibility(View.VISIBLE);
            isScopeRecordStarted = false;
            scopeWaitCounter = 0;
            stopCounter = 0;

            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Sending scope_ON command to Link: " + WifiSSId);
            sendBTCommands(linkPosition, BTConstants.SCOPE_ON_COMMAND);

            Timer timerScopeOn = new Timer();
            TimerTask tt = new TimerTask() {
                @RequiresApi(api = Build.VERSION_CODES.P)
                @Override
                public void run() {
                    if (BTConstants.SCOPE_STATUS.equalsIgnoreCase("START") && !isScopeRecordStarted) {
                        isScopeRecordStarted = true;
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "Record started.");
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showToast(BT_Link_Oscilloscope_Activity.this, "start");
                                Toast.makeText(BT_Link_Oscilloscope_Activity.this, "Please wait until the \"over\" message is displayed", Toast.LENGTH_LONG).show();
                            }
                        }, 50);
                    }

                    if (!isScopeRecordStarted && scopeWaitCounter > 3) {
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "Failed to start Record.");
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(BT_Link_Oscilloscope_Activity.this, "Failed to start Record. Please click the Reconnect button and try again.", Toast.LENGTH_LONG).show();
                            }
                        }, 50);
                        cancel();
                    }

                    if (isScopeRecordStarted) {
                        //if (stopCounter < 30) { // #2240
                        if (BTConstants.SCOPE_STATUS.equalsIgnoreCase("OVER")) {
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + "Record end. (" + stopCounter + " seconds)");
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    showToast(BT_Link_Oscilloscope_Activity.this, "over");
                                    //showDisplayButton(); // #2240
                                }
                            }, 100);
                            cancel();
                        } else {
                            stopCounter++;
                        }
                        /*} else {
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + "Failed to over Record.");
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(BT_Link_Oscilloscope_Activity.this, "Please try again.", Toast.LENGTH_LONG).show();
                                }
                            }, 50);
                            cancel();
                        }*/
                    } else {
                        scopeWaitCounter++;
                    }
                }
            };
            timerScopeOn.schedule(tt, 1000, 1000);

        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Exception in scopeOnCommand: " + e.getMessage());
        }
    }

    /*private void showDisplayButton() {
        btnDisplay.setAlpha(1.0f);
        btnDisplay.setEnabled(true);
    }*/

    private void resetChart() {
        try {
            yValues.clear();
            if (myChart.getData() != null) {
                myChart.getData().clearValues();
            }
            myChart.clear();
            myChart.notifyDataSetChanged();
            myChart.invalidate();
            initChart();
        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Exception in resetChart: " + ex.getMessage());
        }
    }

    private void initChart() {
        try {
            //yValues.add(new Entry(0, 0));
            BTLinkVoltageReadings.clear();
            generateChart();
            myChart.getAxisLeft().setAxisMinimum(0f);
            myChart.getAxisRight().setAxisMinimum(0f);
            myChart.setVisibleXRange(0, 30);
            //yValues.clear();
            //myChart.getDescription().setText("START");
            myChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Exception in initChart: " + ex.getMessage());
        }
    }

    private void generateChart() {
        try {
            createDataForChart();

            LineDataSet line = new LineDataSet(yValues, "Readings...");

            line.setColor(Color.RED);
            line.setCircleColor(Color.RED);
            line.setDrawCircles(true);
            line.setDrawCircleHole(true);
            line.setLineWidth(3);
            line.setCircleRadius(5);
            line.setCircleHoleRadius(5);
            //line.setValueTextSize(15);
            //line.setValueTextColor(Color.BLACK);
            line.setDrawValues(false);

            LineData data = new LineData();
            data.addDataSet(line);
            myChart.setData(data);
        } catch (Exception ex) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Exception in generateChart: " + ex.getMessage());
        }
    }

    private void createDataForChart() {
        try {
            if (BTLinkVoltageReadings.isEmpty()) {
                yValues.clear();
                yValues.add(new Entry(0, 0));
            }
            Log.d(TAG, " Y Values count: " + yValues.size());
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Exception in createDataForChart: " + e.getMessage());
        }
    }

    private void scopeReadCommand() {
        try {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Sending scope_READ command to Link: " + WifiSSId);
            sendBTCommands(linkPosition, BTConstants.SCOPE_READ_COMMAND);
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Exception in scopeReadCommand: " + e.getMessage());
        }
    }

    private void registerBTReceiver(String linkPosition) {

        broadcastBlueLinkData = new BroadcastBlueLinkData();
        switch (linkPosition) {
            case "0"://Link 1
                intentFilter = new IntentFilter("BroadcastBlueLinkOneData");
                break;
            case "1"://Link 2
                intentFilter = new IntentFilter("BroadcastBlueLinkTwoData");
                break;
            case "2"://Link 3
                intentFilter = new IntentFilter("BroadcastBlueLinkThreeData");
                break;
            case "3"://Link 4
                intentFilter = new IntentFilter("BroadcastBlueLinkFourData");
                break;
            case "4"://Link 5
                intentFilter = new IntentFilter("BroadcastBlueLinkFiveData");
                break;
            case "5"://Link 6
                intentFilter = new IntentFilter("BroadcastBlueLinkSixData");
                break;
        }
        registerReceiver(broadcastBlueLinkData, intentFilter);

    }

    private void unregisterBTReceiver() {
        unregisterReceiver(broadcastBlueLinkData);
    }

    public class BroadcastBlueLinkData extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                Bundle notificationData = intent.getExtras();
                String Action = notificationData.getString("Action");
                String actionByPosition = "";
                switch (linkPosition) {
                    case "0"://Link 1
                        actionByPosition = "BlueLinkOne";
                        break;
                    case "1"://Link 2
                        actionByPosition = "BlueLinkTwo";
                        break;
                    case "2"://Link 3
                        actionByPosition = "BlueLinkThree";
                        break;
                    case "3"://Link 4
                        actionByPosition = "BlueLinkFour";
                        break;
                    case "4"://Link 5
                        actionByPosition = "BlueLinkFive";
                        break;
                    case "5"://Link 6
                        actionByPosition = "BlueLinkSix";
                        break;
                    default://Something went wrong in link selection please try again.
                        break;
                }

                if (Action.equalsIgnoreCase(actionByPosition)) {

                    Request = notificationData.getString("Request");
                    Response = notificationData.getString("Response");

                    if (Response == null) {
                        Response = "";
                    }

                    /*if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + "Response from Link >>" + Response.trim());*/
                    //Log.i(TAG, "Scope Response>>" + Response.trim());

                    if (Request.contains(BTConstants.P_TYPE_COMMAND) && Response.contains("pulser_type")) {
                        BTConstants.SCOPE_STATUS = "";
                        getPulserType(Response.trim());

                    } else if (Request.equalsIgnoreCase(BTConstants.SCOPE_ON_COMMAND)) {
                        if (Response.contains("START")) {
                            BTConstants.SCOPE_STATUS = "START";
                        } else if (Response.contains("OVER")) {
                            BTConstants.SCOPE_STATUS = "OVER";
                        }

                    } else if (Request.equalsIgnoreCase(BTConstants.SCOPE_READ_COMMAND)) {
                        //Response = Response.replace("}", "},");
                        sBuilder.append(Response.trim());

                        if (sBuilder.toString().contains("DONE")) {
                            BTConstants.SCOPE_STATUS = "DONE";
                            parseScopeReadings(sBuilder.toString());
                        }

                    } else if (Request.contains("p_settings") && Request.contains("???")) {
                        hidePSettingsControls();
                        parseGetPSettingsResponse(Response.trim());

                    } else if (Request.contains("p_settings") && !Request.contains("???")) {
                        if (AppConstants.GENERATE_LOGS)
                            AppConstants.writeInFile(TAG + "Response from Link >>" + Response.trim());
                        Toast.makeText(BT_Link_Oscilloscope_Activity.this, Response.trim(), Toast.LENGTH_SHORT).show();
                        hideLoader();
                    }
                }
            } catch (Exception e) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "Exception in onReceive: " + e.getMessage());
            }
        }
    }

    private void getPulserType(String response) {
        try {
            if (response.contains("pulser_type")) {
                JSONObject jsonObj = new JSONObject(response);
                p_type = jsonObj.getString("pulser_type");
            } else {
                p_type = "";
            }
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Exception in getPulserType: " + e.getMessage());
        }
    }

    private void parseScopeReadings(String response) {
        try {
            // Create valid json format
            // Changing {"scope":01,01,01,…..01,01,01,DONE"} to {"scope":"01,01,01,…..01,01,01,DONE"}
            if (response.contains(":")) {
                response = response.replace(":", ":\"");
            }

            if (response.contains("scope")) {
                JSONObject jsonObj = new JSONObject(response);
                String scopes = jsonObj.getString("scope");

                if (!scopes.isEmpty()) {
                    String[] scope = scopes.split(",");
                    scope = Arrays.copyOf(scope, scope.length - 1); // To remove last entry 'DONE'

                    for (int i = 0; i < scope.length; i++) {
                        try {
                            String scopeValue = scope[i];
                            // Temp Hardcoded values
                            /*if (i == 0) {
                                scopeValue = "01";
                            } else if ((i % 2) == 0) {
                                scopeValue = "28";
                            } else if ((i % 3) == 0) {
                                scopeValue = "17";
                            } else if ((i % 4) == 1) {
                                scopeValue = "29";
                            } else if ((i % 5) == 0) {
                                scopeValue = "13";
                            } else if ((i % 6) == 1) {
                                scopeValue = "05";
                            } else {
                                scopeValue = "01";
                            }*/
                            //=======================

                            BTLinkVoltageReadings.add(Double.parseDouble(scopeValue));
                            yValues.add(new Entry((i + 1), Float.parseFloat(scopeValue)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (AppConstants.GENERATE_LOGS)
                        AppConstants.writeInFile(TAG + "onReceive: Unable to parse JSONObject. Response: " + response);
                }
            }
            /*if (response.endsWith(",")) {
                response = response.substring(0, (response.length() - 1)); // To remove last comma
            }

            String[] scope = response.split(",");
            scope = Arrays.copyOf(scope, scope.length - 1); // To remove last entry {"scope":"DONE"}

            for (int i = 0; i < scope.length; i++) {
                scopeCount(scope[i].trim(), i + 1);
            }*/

        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Exception in parseScopeReadings: " + e.getMessage());
        }
    }

    /*private void scopeCount(String response, int scopeCounter) {
        try {
            String scope;

            if (response.contains("scope")) {
                JSONObject jsonObj = new JSONObject(response);
                scope = jsonObj.getString("scope");

                if (!scope.isEmpty()) {
                    BTLinkVoltageReadings.add(Double.parseDouble(scope));
                    yValues.add(new Entry(scopeCounter, Float.parseFloat(scope)));
                }
            }
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Exception in scopeCount: " + e.getMessage());
        }
    }*/

    private void sendReadingsToServer() {
        try {
            LINKPulserDataEntity objPulserData = new LINKPulserDataEntity();
            objPulserData.SiteId = BTConstants.SELECTED_SITE_ID_FOR_SCOPE;
            objPulserData.LINKsPulsers = BTLinkVoltageReadings;
            objPulserData.AddedDateTimeFromAPP = AppConstants.currentDateFormat("yyyy-MM-dd HH:mm");

            new SaveLINKPulserData(objPulserData).execute();
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Exception in sendReadingsToServer: " + e.getMessage());
        }
    }

    public class SaveLINKPulserData extends AsyncTask<Void, Void, String> {
        LINKPulserDataEntity objPulserData;
        public String response = null;

        public SaveLINKPulserData(LINKPulserDataEntity objPulserData) {
            this.objPulserData = objPulserData;
        }

        @Override
        protected String doInBackground(Void... voids) {

            try {
                ServerHandler serverHandler = new ServerHandler();

                Gson gson = new Gson();
                String jsonData = gson.toJson(objPulserData);

                String userEmail = CommonUtils.getCustomerDetails(BT_Link_Oscilloscope_Activity.this).PersonEmail;

                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BT_Link_Oscilloscope_Activity.this) + ":" + userEmail + ":" + "SaveLINKPulserData" + AppConstants.LANG_PARAM);
                response = serverHandler.PostTextData(BT_Link_Oscilloscope_Activity.this, AppConstants.WEB_URL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "SaveLINKPulserData InBackground Exception: " + ex.getMessage());
            }
            return response;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                JSONObject jsonObject = new JSONObject(res);
                String ResponseText = jsonObject.getString("ResponseText");
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "SaveLINKPulserData Response: " + ResponseText);
            } catch (Exception e) {
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "SaveLINKPulserData onPostExecute Exception: " + e.getMessage());
            }
        }
    }

    private void getPSettingsCommand() {
        try {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Sending get p_settings command to Link: " + WifiSSId);
            sendBTCommands(linkPosition, BTConstants.GET_P_SETTINGS_COMMAND.replace("X", selectedPulserType));
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Exception in getPSettingsCommand: " + e.getMessage());
        }
    }

    private void parseGetPSettingsResponse(String response) {
        try {
            if (response.contains("p" + selectedPulserType)) {
                String DELAY_TIME = "delay_time", LOW_ALTITUDE = "low_altitude", HIGH_ALTITUDE = "high_altitude", LOW_SAMPLE = "low_sample";
                String HIGH_SAMPLE = "high_sample", LOW_TOTAL = "low_total", HIGH_TOTAL = "high_total", SAMPLE_RATE = "sample_rate";

                ArrayList<String> settings = new ArrayList<>();
                JSONObject jsonObj = new JSONObject(response);
                Iterator<String> keys = jsonObj.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = jsonObj.get(key);
                    settings.add(key + ": " + value);
                }
                linearLayout_p_settings.setVisibility(View.VISIBLE);
                for (String setting : settings) {
                    setting = setting.trim();
                    if (selectedPulserType.equals("1")) {
                        linearPSetting_time.setVisibility(View.VISIBLE);
                        tvTime.setText(setting);
                    } else {
                        if (setting.contains(DELAY_TIME)) {
                            linearPSetting_delayTime.setVisibility(View.VISIBLE);
                            tvDelayTime.setText(setting);
                        } else if (setting.contains(LOW_ALTITUDE)) {
                            linearPSetting_lowAltitude.setVisibility(View.VISIBLE);
                            tvLowAltitude.setText(setting);
                        } else if (setting.contains(HIGH_ALTITUDE)) {
                            linearPSetting_highAltitude.setVisibility(View.VISIBLE);
                            tvHighAltitude.setText(setting);
                        } else if (setting.contains(LOW_SAMPLE)) {
                            linearPSetting_lowSample.setVisibility(View.VISIBLE);
                            tvLowSample.setText(setting);
                        } else if (setting.contains(HIGH_SAMPLE)) {
                            linearPSetting_highSample.setVisibility(View.VISIBLE);
                            tvHighSample.setText(setting);
                        } else if (setting.contains(LOW_TOTAL)) {
                            linearPSetting_lowTotal.setVisibility(View.VISIBLE);
                            tvLowTotal.setText(setting);
                        } else if (setting.contains(HIGH_TOTAL)) {
                            linearPSetting_highTotal.setVisibility(View.VISIBLE);
                            tvHighTotal.setText(setting);
                        } else if (setting.contains(SAMPLE_RATE)) {
                            linearPSetting_sampleRate.setVisibility(View.VISIBLE);
                            tvSampleRate.setText(setting);
                        } else {
                            linearLayout_p_settings.setVisibility(View.GONE);
                            if (AppConstants.GENERATE_LOGS)
                                AppConstants.writeInFile(TAG + "<parseGetPSettingsResponse setting: " + setting + "; selectedPulserType: " + selectedPulserType + ">");
                        }
                    }
                }
            } else {
                linearLayout_p_settings.setVisibility(View.GONE);
                if (AppConstants.GENERATE_LOGS)
                    AppConstants.writeInFile(TAG + "<parseGetPSettingsResponse: " + response + ">");
            }
        } catch (Exception e) {
            if (AppConstants.GENERATE_LOGS)
                AppConstants.writeInFile(TAG + "Exception in parseGetPSettingsResponse: " + e.getMessage());
        }
    }

}