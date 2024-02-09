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
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.Timer;
import java.util.TimerTask;

public class BT_Link_Oscilloscope_Activity extends AppCompatActivity { // implements ServiceConnection

    private static final String TAG = "BTLink_Oscilloscope ";

    private LineChart myChart;
    private Button btnSet, btnStartScope, btnDisplay, btnReconnect;
    public RadioGroup rdg_p_type;
    public RadioButton rdSelectedType;
    String LinkPosition, WifiSSId;
    int scopeWaitCounter = 0, stopCounter = 0;
    int readCounter = 0;
    //public boolean chartBindStarted = false;
    public Timer timerBtScope;
    public String p_type = "";
    public static ArrayList<Double> BTLinkVoltageReadings = new ArrayList<>();
    ProgressDialog pdMain;

    public ArrayList<Entry> yValues = new ArrayList<>();
    public boolean isScopeRecordStarted = false;

    public BroadcastBlueLinkData broadcastBlueLinkData = null;
    public IntentFilter intentFilter;

    String Request = "", Response = "";
    public boolean flag = true;
    StringBuilder sBuilder = new StringBuilder();

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            UnregisterReceiver();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_link_oscilloscope);

        LinkPosition = getIntent().getExtras().getString("LinkPosition");
        WifiSSId = getIntent().getExtras().getString("WifiSSId");

        getSupportActionBar().setTitle("Oscilloscope for " + WifiSSId);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnSet = findViewById(R.id.btnSet);
        btnStartScope = findViewById(R.id.btnStartScope);
        btnDisplay = findViewById(R.id.btnDisplay);
        //btnDisplay.setAlpha(0.5f);
        //btnDisplay.setEnabled(false);

        rdg_p_type = (RadioGroup) findViewById(R.id.rdg_p_type);
        btnReconnect = findViewById(R.id.btnReconnect);
        btnReconnect.setVisibility(View.INVISIBLE);

        myChart = findViewById(R.id.lineChart);

        myChart.setDragEnabled(true);
        myChart.setScaleEnabled(true);
        myChart.getDescription().setText("");
        myChart.getXAxis().setTextSize(12);
        //myChart.getDescription().setTextSize(12f);

        InitChart();

        if (LinkPosition == null) {
            LinkPosition = "0";
        }

        RegisterReceiver(LinkPosition);

        btnStartScope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Start button clicked");
                    ClearPreviousChartAndData();

                    //btnDisplay.setAlpha(0.5f); // #2240
                    //btnDisplay.setEnabled(false);

                    scopeOnCommand();

                } catch (Exception ex) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Exception in btnStartScope click: " + ex.getMessage());
                }
            }
        });

        btnDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Display button clicked");
                    ClearPreviousChartAndData();

                    scopeReadCommand();
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Read started.");
                    showLoader(getResources().getString(R.string.PleaseWait));

                    timerBtScope = new Timer();
                    TimerTask tt = new TimerTask() {
                        @RequiresApi(api = Build.VERSION_CODES.P)
                        @Override
                        public void run() {
                            if (BTConstants.ScopeStatus.equalsIgnoreCase("DONE")) {
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "Read end. (" + readCounter + " seconds)");
                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        showToast(BT_Link_Oscilloscope_Activity.this, "done");
                                        BindChartData();

                                        if (AppConstants.GenerateLogs)
                                            AppConstants.WriteinFile(TAG + "<Voltage Readings count: " + BTLinkVoltageReadings.size() + ">");

                                        if (BTLinkVoltageReadings.size() > 0) { // To avoid sending empty data to the server.
                                            SendReadingsToServer();
                                        }
                                    }
                                }, 1000);
                                hideLoader();
                                cancel();
                            } else {
                                readCounter++;
                            }

                            /*if (readCounter > 60) { // #2240
                                if (AppConstants.GenerateLogs)
                                    AppConstants.WriteinFile(TAG + "Read process not completed.");
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
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Exception in btnDisplay click: " + ex.getMessage());
                }
            }

        });

        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetP_TypeForLink();
            }
        });

        btnReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Reconnecting to the Link... ");

                showLoader(getResources().getString(R.string.ConnectingToTheLINK) + " " + getResources().getString(R.string.PleaseWait));

                BTSPPMain btspp = new BTSPPMain();
                btspp.activity = BT_Link_Oscilloscope_Activity.this;

                switch (LinkPosition) {
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
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideLoader();
                    }
                }, 7000);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        try {
            UnregisterReceiver();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finish();
    }

    public void showLoader(String message) {

        SpannableString ss2 = new SpannableString(message);
        ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
        ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
        pdMain = new ProgressDialog(BT_Link_Oscilloscope_Activity.this);
        pdMain.setMessage(ss2);
        pdMain.setCancelable(false);
        pdMain.show();
    }

    public void hideLoader() {

        if (pdMain != null) {
            pdMain.dismiss();
        }
    }

    public void ClearPreviousChartAndData() {
        try {
            BTLinkVoltageReadings.clear();
            ResetChart();
            BTConstants.ScopeStatus = "";
            readCounter = 0;
            sBuilder.setLength(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void BindChartData() {
        try {
            GenerateChart();
            myChart.notifyDataSetChanged();
            myChart.invalidate();
            myChart.getAxisLeft().setAxisMinimum(0f);
            myChart.getAxisRight().setAxisMinimum(0f);
            myChart.setVisibleXRange(0, 30);
            //myChart.moveViewToX(BTLinkVoltageReadings.size() - 6);
            myChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void SetP_TypeForLink() {
        try {
            p_type = "";
            int selectedSize = rdg_p_type.getCheckedRadioButtonId();
            rdSelectedType = (RadioButton) findViewById(selectedSize);

            String selectedType = "";
            if (rdSelectedType != null) {
                selectedType = rdSelectedType.getText().toString().trim();
            }
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Selected Type: " + selectedType);

            if (!selectedType.isEmpty()) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Sending p_type command to Link: " + WifiSSId);
                BTSPPMain btspp = new BTSPPMain();
                switch (LinkPosition) {
                    case "0"://Link 1
                        btspp.send1(BTConstants.p_type_command + selectedType);
                        break;
                    case "1"://Link 2
                        btspp.send2(BTConstants.p_type_command + selectedType);
                        break;
                    case "2"://Link 3
                        btspp.send3(BTConstants.p_type_command + selectedType);
                        break;
                    case "3"://Link 4
                        btspp.send4(BTConstants.p_type_command + selectedType);
                        break;
                    case "4"://Link 5
                        btspp.send5(BTConstants.p_type_command + selectedType);
                        break;
                    case "5"://Link 6
                        btspp.send6(BTConstants.p_type_command + selectedType);
                        break;
                    default://Something went wrong in link selection please try again.
                        break;
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (p_type.isEmpty()) {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Unable to set pulser type.");
                            Toast.makeText(BT_Link_Oscilloscope_Activity.this, "Unable to set pulser type. Please click the Reconnect button and try again.", Toast.LENGTH_LONG).show();
                        } else {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Pulser type is set: " + p_type);
                            Toast.makeText(BT_Link_Oscilloscope_Activity.this, "Pulser Type: " + p_type, Toast.LENGTH_SHORT).show();
                            showLoader(getResources().getString(R.string.ConnectingToTheLINK) + " " + getResources().getString(R.string.PleaseWaitSeveralSeconds));

                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "Reconnecting to the Link.");
                                    BTSPPMain btspp = new BTSPPMain();
                                    btspp.activity = BT_Link_Oscilloscope_Activity.this;

                                    switch (LinkPosition) {
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

                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideLoader();
                                        }
                                    }, 100);
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
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Exception in SetP_TypeForLink: " + ex.getMessage());
        }
    }

    public void showToast(Context ctx, String message) {
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

            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Sending scope_ON command to Link: " + WifiSSId);
            //Execute scopeOn Command
            BTSPPMain btspp = new BTSPPMain();
            switch (LinkPosition) {
                case "0"://Link 1
                    btspp.send1(BTConstants.scope_ON_cmd);
                    break;
                case "1"://Link 2
                    btspp.send2(BTConstants.scope_ON_cmd);
                    break;
                case "2"://Link 3
                    btspp.send3(BTConstants.scope_ON_cmd);
                    break;
                case "3"://Link 4
                    btspp.send4(BTConstants.scope_ON_cmd);
                    break;
                case "4"://Link 5
                    btspp.send5(BTConstants.scope_ON_cmd);
                    break;
                case "5"://Link 6
                    btspp.send6(BTConstants.scope_ON_cmd);
                    break;
                default://Something went wrong in link selection please try again.
                    break;
            }

            Timer timerScopeOn = new Timer();
            TimerTask tt = new TimerTask() {
                @RequiresApi(api = Build.VERSION_CODES.P)
                @Override
                public void run() {
                    if (BTConstants.ScopeStatus.equalsIgnoreCase("START") && !isScopeRecordStarted) {
                        isScopeRecordStarted = true;
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Record started.");
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showToast(BT_Link_Oscilloscope_Activity.this, "start");
                                Toast.makeText(BT_Link_Oscilloscope_Activity.this, "Please wait until the \"over\" message is displayed", Toast.LENGTH_LONG).show();
                            }
                        }, 50);
                    }

                    if (!isScopeRecordStarted && scopeWaitCounter > 3) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Failed to start Record.");
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
                        if (BTConstants.ScopeStatus.equalsIgnoreCase("OVER")) {
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Record end. (" + stopCounter + " seconds)");
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
                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "Failed to over Record.");
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
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "BTLink: scopeOnCommand Exception: " + e.getMessage());
        }
    }

    private void showDisplayButton() {
        btnDisplay.setAlpha(1.0f);
        btnDisplay.setEnabled(true);
    }

    public void ResetChart() {
        try {
            yValues.clear();
            if (myChart.getData() != null) {
                myChart.getData().clearValues();
            }
            myChart.clear();
            myChart.notifyDataSetChanged();
            myChart.invalidate();
            InitChart();
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Exception in ResetChart: " + ex.getMessage());
        }
    }

    public void InitChart() {
        try {
            //yValues.add(new Entry(0, 0));
            BTLinkVoltageReadings.clear();
            GenerateChart();
            myChart.getAxisLeft().setAxisMinimum(0f);
            myChart.getAxisRight().setAxisMinimum(0f);
            myChart.setVisibleXRange(0, 30);
            //yValues.clear();
            //myChart.getDescription().setText("START");
            myChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Exception in InitChart: " + ex.getMessage());
        }
    }

    public void GenerateChart() {
        try {

            CreateDataForChart();

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
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Exception in GenerateChart: " + ex.getMessage());
        }
    }

    private void CreateDataForChart() {
        try {
            if (BTLinkVoltageReadings.size() == 0) {
                yValues.clear();
                yValues.add(new Entry(0, 0));
            }

            Log.d(TAG, " Y Values count: " + yValues.size());
        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "CreateDataForChart Exception: " + e.getMessage());
        }
    }

    private void scopeReadCommand() {
        try {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Sending scope_READ command to Link: " + WifiSSId);
            //Execute scopeRead Command
            BTSPPMain btspp = new BTSPPMain();
            switch (LinkPosition) {
                case "0"://Link 1
                    btspp.send1(BTConstants.scope_READ_cmd);
                    break;
                case "1"://Link 2
                    btspp.send2(BTConstants.scope_READ_cmd);
                    break;
                case "2"://Link 3
                    btspp.send3(BTConstants.scope_READ_cmd);
                    break;
                case "3"://Link 4
                    btspp.send4(BTConstants.scope_READ_cmd);
                    break;
                case "4"://Link 5
                    btspp.send5(BTConstants.scope_READ_cmd);
                    break;
                case "5"://Link 6
                    btspp.send6(BTConstants.scope_READ_cmd);
                    break;
                default://Something went wrong in link selection please try again.
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "ScopeReadCommand Exception: " + e.getMessage());
        }
    }

    private void RegisterReceiver(String linkPosition) {

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

    private void UnregisterReceiver() {
        unregisterReceiver(broadcastBlueLinkData);
    }

    public class BroadcastBlueLinkData extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                Bundle notificationData = intent.getExtras();
                String Action = notificationData.getString("Action");
                String actionByPosition = "";
                switch (LinkPosition) {
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

                    /*if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "BTLink_1: Response from Link >>" + Response.trim());*/
                    //Log.i(TAG, "BTLink_1: Scope Response>>" + Response.trim());

                    if (Response.contains("pulser_type")) {
                        BTConstants.ScopeStatus = "";
                        getPulserType(Response.trim());
                    } else if (Response.contains("START")) {
                        BTConstants.ScopeStatus = "START";
                    } else if (Response.contains("OVER")) {
                        BTConstants.ScopeStatus = "OVER";
                    } else if (Response.contains("DONE")) {
                        BTConstants.ScopeStatus = "DONE";
                    }
                    if (Request.equalsIgnoreCase(BTConstants.scope_READ_cmd)) {
                        //Response = Response.replace("}", "},");
                        sBuilder.append(Response.trim());

                        if (sBuilder.toString().contains("DONE")) {
                            BTConstants.ScopeStatus = "DONE";
                            parseScopeReadings(sBuilder.toString());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "BTLink: onReceive Exception: " + e.getMessage());
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
            e.printStackTrace();
        }
    }

    public void parseScopeReadings(String response) {
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
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "onReceive: Unable to parse JSONObject. Response: " + response);
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
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "parseScopeReadings Exception: " + e.getMessage());
        }
    }

    private void scopeCount(String response, int scopeCounter) {
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
            e.printStackTrace();
        }
    }

    private void SendReadingsToServer() {
        try {
            LINKPulserDataEntity objPulserData = new LINKPulserDataEntity();
            objPulserData.SiteId = BTConstants.selectedSiteIdForScope;
            objPulserData.LINKsPulsers = BTLinkVoltageReadings;
            objPulserData.AddedDateTimeFromAPP = AppConstants.currentDateFormat("yyyy-MM-dd HH:mm");

            new SaveLINKPulserData(objPulserData).execute();
        } catch (Exception e) {
            e.printStackTrace();
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
                response = serverHandler.PostTextData(BT_Link_Oscilloscope_Activity.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "SaveLINKPulserData InBackground Exception: " + ex.getMessage());
            }
            return response;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                JSONObject jsonObject = new JSONObject(res);
                String ResponseText = jsonObject.getString("ResponseText");
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "SaveLINKPulserData Response: " + ResponseText);
            } catch (Exception e) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "SaveLINKPulserData onPostExecute Exception: " + e.getMessage());
            }
        }
    }

}