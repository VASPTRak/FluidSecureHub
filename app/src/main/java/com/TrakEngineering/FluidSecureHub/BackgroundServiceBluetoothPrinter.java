package com.TrakEngineering.FluidSecureHub;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import static com.TrakEngineering.FluidSecureHub.BluetoothReaderReciver.ctx;

public class BackgroundServiceBluetoothPrinter extends BackgroundService {

    private String TAG = "BS_BluetoothPrinter ";
    String PrintRecipt = "";
    //--------------------------------------------------------------tst
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;

    // needed for communication to bluetooth device / network
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    //--------------------------------------------------------------

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            super.onStart(intent, startId);
            Bundle extras = intent.getExtras();
            if (extras == null) {
                Log.d("Service", "null");

            } else {

                PrintRecipt = (String) extras.get("printReceipt");
                System.out.println("Temp data for print recipt: "+PrintRecipt);
                new SetBTConnectionPrinter().execute();

            }
        } catch (NullPointerException e) {
            Log.d("Ex", e.getMessage());
            this.stopSelf();
        }

        // return super.onStartCommand(intent, flags, startId);
        return Service.START_NOT_STICKY;
    }

    public class SetBTConnectionPrinter extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {


            try {

                findBT();
                openBT();

                System.out.println("printer. FindBT and OpenBT");
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            try {
                sendData(PrintRecipt);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            closeBT();
                        } catch (Exception e) {
                            System.out.println("Exception"+e);
                        }
                    }
                }, 2000);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    //-----------------------------------------------------tst
    public boolean findBT() {

        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if(mBluetoothAdapter == null) {
                //myLabel.setText("No bluetooth adapter available");
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if(pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {

                    // RPP300 is the name of the bluetooth printer device
                    // we got this name from the list of paired devices
                    String MacAddr = AppConstants.PRINTER_MAC_ADDRESS;
                    //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile( TAG+"  findBT_method" + "BT_PRINTER Mac Address:" + MacAddr);
                    if (MacAddr.equalsIgnoreCase(""))
                    {

                        if (device.getName().equals(AppConstants.BLUETOOTH_PRINTER_NAME)) {//Sony= "C4:3A:BE:79:B1:C5" //HHW-UART-S10
                            mmDevice = device;
                            break;
                        }


                    }else{

                        if (device.getName().equals(AppConstants.BLUETOOTH_PRINTER_NAME) & device.getAddress().equalsIgnoreCase(MacAddr)) {//Sony= "C4:3A:BE:79:B1:C5" //HHW-UART-S10
                            mmDevice = device;
                            break;
                        }else{
                            if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile( TAG+"  findBT_method" + "printer mac address blank");
                            Toast.makeText(ctx,"printer mac address blank",Toast.LENGTH_LONG).show();
                        }


                    }

                }
            }else{
                if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile( TAG+"  findBT_method" + "printer mac address blank");

            }

            //myLabel.setText("Bluetooth device found.");

        }catch(Exception e){
            e.printStackTrace();
        }

        return true;
    }

    public void openBT() throws IOException {
        try {
            //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile( TAG+"  OpenBT_method" + "BT_PRINTER Mac Address:");
            // Standard SerialPortService ID
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            if (!mmSocket.isConnected())
                mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();

            beginListenForData();

            //myLabel.setText("Bluetooth Opened");

        } catch (Exception e) {
            e.printStackTrace();
            //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile("BluetoothPrinter~~~~~~~~~OpenBT_method" + e);
        }
    }

    public void beginListenForData() {
        try {


            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = mmInputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device


                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            workerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
            //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile("BluetoothPrinter~~~~~~~~~BeingListernfordata_method" + e);
        }
    }

    public void sendData(String printReceipt) throws IOException {
        try {

            if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile( TAG+"  SendData_method" + "BT_PRINTER Receipt\n"+printReceipt);
            // the text typed by the user
            String msg = printReceipt+"\n\n\n\n\n";//myTextbox.getText().toString();
            msg += "\n";

            mmOutputStream.write(msg.getBytes());

            // tell the user data were sent
            //myLabel.setText("Data sent.");

        } catch (Exception e) {
            e.printStackTrace();
            //if (AppConstants.GENERATE_LOGS)AppConstants.writeInFile("BluetoothPrinter~~~~~~~~~SendData_method" + e);

        }
    }

    public void closeBT() throws IOException {
        try {
            stopWorker = true;
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}



