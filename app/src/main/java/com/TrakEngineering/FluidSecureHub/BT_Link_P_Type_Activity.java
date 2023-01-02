package com.TrakEngineering.FluidSecureHub;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.TrakEngineering.FluidSecureHub.BTSPP.BTConstants;
import com.TrakEngineering.FluidSecureHub.BTSPP.BTSPPMain;

import org.json.JSONObject;

public class BT_Link_P_Type_Activity extends AppCompatActivity {

    private static final String TAG = "BT_Link_P_Type_Activity ";
    TextView tvSSID, tvMAC;
    String LinkPosition, WifiSSId, BTMacAddress;
    Button btnSet;
    public RadioGroup rdg_p_type;
    public RadioButton rdSelectedType;
    public BroadcastBlueLinkData broadcastBlueLinkData = null;
    public IntentFilter intentFilter;
    String Request = "", Response = "";

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
    public void onBackPressed() {
        try {
            UnregisterReceiver();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_link_ptype);

        getSupportActionBar().setTitle("Set P_Type");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        LinkPosition = getIntent().getExtras().getString("LinkPosition");
        WifiSSId = getIntent().getExtras().getString("WifiSSId");
        BTMacAddress = getIntent().getExtras().getString("BTMacAddress");

        tvSSID = (TextView) findViewById(R.id.tvSSID);
        tvMAC = (TextView) findViewById(R.id.tvMAC);
        btnSet = (Button) findViewById(R.id.btnSet);
        rdg_p_type = (RadioGroup) findViewById(R.id.rdg_p_type);

        tvSSID.setText(WifiSSId);
        tvMAC.setText(BTMacAddress.toUpperCase());

        if (LinkPosition == null) {
            LinkPosition = "0";
        }

        RegisterReceiver(LinkPosition);

        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BTConstants.p_type = "";
                SetP_TypeForLink();
            }
        });

    }

    private void SetP_TypeForLink() {
        try {
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
                    AppConstants.WriteinFile(TAG + "Sending p_type command.");
                BTSPPMain btspp = new BTSPPMain();
                //btspp.sendP_Type(BTConstants.p_type_command + selectedType);
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
                        Toast.makeText(BT_Link_P_Type_Activity.this, "Pulsar Type: " + BTConstants.p_type, Toast.LENGTH_LONG).show();
                    }
                }, 1000);
            } else {
                Toast.makeText(BT_Link_P_Type_Activity.this, "Please select Type.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception ex) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "Exception in SetP_TypeForLink: " + ex.getMessage());
        }
    }

    private void RegisterReceiver(String linkPosition) {

        //Register Broadcast receiver
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

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "BTLink: Response from Link >>" + Response.trim());

                    if (Response.contains("pulser_type")) {
                        getPulserType(Response);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "BTLink: onReceive Exception:" + e.getMessage());
            }
        }
    }

    private void getPulserType(String response) {
        try {

            if (response.contains("pulser_type")) {
                JSONObject jsonObj = new JSONObject(response);
                BTConstants.p_type = jsonObj.getString("pulser_type");
            } else {
                BTConstants.p_type = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}