package com.TrakEngineering.FluidSecureHub;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.TrakEngineering.FluidSecureHub.EddystoneScanner.EddystoneScannerService;

public class AcceptManualvehicleActivityFA extends AppCompatActivity {

    String InstantIdMacAddress ="",FSNPMacAddress = "";
    EditText editvehicleManually;
    Button cancelAction,btnSave;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_manualvehicle_f);

        editvehicleManually = (EditText)findViewById(R.id.editvehicleManually);
        cancelAction = (Button) findViewById(R.id.cancelAction);
        btnSave = (Button) findViewById(R.id.btnSave);

        FSNPMacAddress = getIntent().getStringExtra("FSNPMacAddress");
        InstantIdMacAddress = getIntent().getStringExtra("InstantIdMacAddress");

        Constants.ON_FA_MANUAL_SCREEN = true;

        if (!Constants.FA_MANUAL_VEHICLE.isEmpty())
        {
            editvehicleManually.setText(Constants.FA_MANUAL_VEHICLE);
        }

        cancelAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Constants.FA_MANUAL_VEHICLE = "";
                Intent i = new Intent(AcceptManualvehicleActivityFA.this, WelcomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String vh_no = editvehicleManually.getText().toString().trim();
                if (!vh_no.isEmpty()){

                    Constants.FA_MANUAL_VEHICLE = editvehicleManually.getText().toString().trim();
                    //Start same transaction again
                    EddystoneScannerService obj = new EddystoneScannerService();
                    obj.StartTransactionProcess(FSNPMacAddress, InstantIdMacAddress);

                    Intent i = new Intent(AcceptManualvehicleActivityFA.this, WelcomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);

                }else{

                    CommonUtils.SimpleMessageDilaog(AcceptManualvehicleActivityFA.this, "Message", "Please Enter Vehicle Number");
                }



            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Constants.ON_FA_MANUAL_SCREEN = false;
    }
}
