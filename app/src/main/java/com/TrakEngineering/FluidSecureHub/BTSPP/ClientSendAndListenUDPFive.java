package com.TrakEngineering.FluidSecureHub.BTSPP;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ClientSendAndListenUDPFive implements Runnable {

    private static final String TAG = "UDP_Act_Five ";
    String strcmd = "";
    String SERVER_IP = "";
    Context ct;

    public ClientSendAndListenUDPFive(String str_cmd, String server_ip, Context ctx) {

        strcmd = str_cmd;
        SERVER_IP = server_ip;
        ct = ctx;
    }

    @Override
    public void run() {
        StringBuilder sb5 = new StringBuilder();
        boolean run = true;
        try {

            DatagramSocket udpSocket = new DatagramSocket();
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            byte[] buf = strcmd.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, Constants.PORT);
            udpSocket.send(packet);

            while (run) {
                try {

                    byte[] message = new byte[3000];
                    DatagramPacket p = new DatagramPacket(message, message.length);
                    Log.i("UDP client: ", "about to wait to receive");
                    udpSocket.setSoTimeout(10000);
                    udpSocket.receive(p);
                    String Response = new String(message, 0, p.getLength());
                    //SpannableStringBuilder spn = new SpannableStringBuilder(Response + '\n');
                    Log.d("Received text", Response);
                    //AppConstants.writeInFile(TAG + " Link 5: Received text: " + Response);
                    //run = false;

                    if (strcmd.equalsIgnoreCase(BTConstants.INFO_COMMAND) && Response.contains("records")) {
                        BTConstants.IS_NEW_VERSION_LINK_FIVE = true;
                    }

                    if (Response.contains("$$")) {
                        String res = Response.replace("$$", "");
                        if (res.contains("}")) {
                            res = res.substring(0, (res.lastIndexOf("}") + 1)); // To remove extra characters after the last curly bracket (if any)
                        }
                        if (!res.trim().isEmpty()) {
                            sb5.append(res.trim());
                        }
                        sendBroadcastIntentFromLinkFive(sb5.toString());
                        sb5.setLength(0);
                    } else {
                        if (BTConstants.IS_NEW_VERSION_LINK_FIVE) {
                            sb5.append(Response);
                        } else {
                            // For old version Link response
                            sb5.setLength(0);
                            sendBroadcastIntentFromLinkFive(Response + '\n');
                        }
                    }

                } catch (IOException e) {
                    Log.e(" UDP client has IOException", "error: ", e);
                    run = false;
                    udpSocket.close();
                }
            }
        } catch (SocketException e) {
            Log.e("Socket Open:", "Error:", e);
        } catch (Exception e) {
            Log.e("Exception:", "Error:", e);
        }
    }

    public void sendBroadcastIntentFromLinkFive(String resp) {
        //AppConstants.writeInFile(TAG + " Link 5: Final Response: " + resp);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("BroadcastBlueLinkFiveData");
        broadcastIntent.putExtra("Request", strcmd);
        broadcastIntent.putExtra("Response", resp.trim());
        broadcastIntent.putExtra("Action", "BlueLinkFive");
        ct.sendBroadcast(broadcastIntent);
    }

}
