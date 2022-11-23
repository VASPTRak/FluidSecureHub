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

public class ClientSendAndListenUDPSix implements Runnable {

    private static final String TAG = "UDP_Act_Six ";
    String strcmd = "";
    String SERVER_IP = "";
    Context ct;

    public ClientSendAndListenUDPSix(String str_cmd, String server_ip, Context ctx) {

        strcmd = str_cmd;
        SERVER_IP = server_ip;
        ct = ctx;
    }

    @Override
    public void run() {
        StringBuilder sb6 = new StringBuilder();
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
                    //AppConstants.WriteinFile(TAG + " Link 6: Received text: " + Response);
                    //run = false;

                    if (strcmd.equalsIgnoreCase(BTConstants.info_cmd) && Response.contains("records")) {
                        BTConstants.isNewVersionLinkSix = true;
                    }

                    if (Response.contains("$$")) {
                        String res = Response.replace("$$", "");
                        if (res.contains("}")) {
                            res = res.substring(0, (res.lastIndexOf("}") + 1)); // To remove extra characters after the last curly bracket (if any)
                        }
                        if (!res.trim().isEmpty()) {
                            sb6.append(res.trim());
                        }
                        sendBroadcastIntentFromLinkSix(sb6.toString());
                        sb6.setLength(0);
                    } else {
                        if (BTConstants.isNewVersionLinkSix) {
                            sb6.append(Response);
                        } else {
                            // For old version Link response
                            sb6.setLength(0);
                            sendBroadcastIntentFromLinkSix(Response + '\n');
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

    public void sendBroadcastIntentFromLinkSix(String resp) {
        //AppConstants.WriteinFile(TAG + " Link 6: Final Response: " + resp);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("BroadcastBlueLinkSixData");
        broadcastIntent.putExtra("Request", strcmd);
        broadcastIntent.putExtra("Response", resp.trim());
        broadcastIntent.putExtra("Action", "BlueLinkSix");
        ct.sendBroadcast(broadcastIntent);
    }

}
