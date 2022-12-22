package com.TrakEngineering.FluidSecureHub;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.TrakEngineering.FluidSecureHub.BTSPP.BTConstants;

import java.util.ArrayList;
import java.util.Set;

public class PairDeviceActivity extends AppCompatActivity {

    private static final String TAG = "PairDeviceActivity ";

    //vars
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter;
    Button btn_pair_new_device;
    private boolean IsPairNewDeviceClicked = false;
    private ConnectionDetector cd = new ConnectionDetector(PairDeviceActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_device);

        BTConstants.SelectedLinkForPaireddevices = getIntent().getExtras().getInt("linkNumber");
        btn_pair_new_device = (Button) findViewById(R.id.btn_pair_new_device);

        GetPairedDevicesList();
        initRecyclerView();

        btn_pair_new_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mNames.clear();
                mImageUrls.clear();
                IsPairNewDeviceClicked = true;
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
        }

        if (IsPairNewDeviceClicked){
            IsPairNewDeviceClicked = false;
            GetPairedDevicesList();
            initRecyclerView();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reader, menu);
        menu.findItem(R.id.mreconnect_ble_readers).setVisible(false);
        menu.findItem(R.id.mreboot_reader).setVisible(false);
        menu.findItem(R.id.mcamera_back).setVisible(false);
        menu.findItem(R.id.mcamera_front).setVisible(false);
        menu.findItem(R.id.mshow_reader_status).setVisible(false);

        if (cd.isConnectingToInternet()) {

            menu.findItem(R.id.monline).setVisible(true);
            menu.findItem(R.id.mofline).setVisible(false);

        } else {

            menu.findItem(R.id.monline).setVisible(false);
            menu.findItem(R.id.mofline).setVisible(true);
        }

        MenuItem itemSp = menu.findItem(R.id.menuSpanish);
        MenuItem itemEng = menu.findItem(R.id.menuEnglish);
        itemSp.setVisible(false);
        itemEng.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (item.getItemId()) {

            case R.id.mreload:
                this.recreate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void GetPairedDevicesList() {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Get paired devices.
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if (deviceName != null) {
                    if (deviceName.startsWith("FSBT-") || deviceName.startsWith("FSAST-") || deviceName.startsWith("FSDB-") ||
                            deviceName.startsWith("FSAF3-") || deviceName.startsWith("FSAF7-") || deviceName.startsWith("FSAG2-") ||
                            deviceName.startsWith("FSAG3-") || deviceName.startsWith("FSFV-") || deviceName.startsWith("FSFH3-") ||
                            deviceName.startsWith("FSFH7-") || deviceName.startsWith("FSGH2-") || deviceName.startsWith("FSGH3-")) {

                        mImageUrls.add(deviceHardwareAddress);
                        mNames.add(deviceName);
                        Log.i(TAG, "DeviceName:" + deviceName + "\n" + "MacAddress:" + deviceHardwareAddress);
                    }
                }
            }
        }
    }

    private void initRecyclerView() {
        Log.d(TAG, "initRecyclerView: init recyclerview.");
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerv_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mNames, mImageUrls);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}