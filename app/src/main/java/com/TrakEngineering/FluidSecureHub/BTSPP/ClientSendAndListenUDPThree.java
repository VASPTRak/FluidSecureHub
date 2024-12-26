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

public class ClientSendAndListenUDPThree implements Runnable {

    private static final String TAG = "UDP_Act_Three ";
    String strcmd = "";
    String SERVER_IP = "";
    Context ct;

    public ClientSendAndListenUDPThree(String str_cmd, String server_ip, Context ctx) {

        strcmd = str_cmd;
        SERVER_IP = server_ip;
        ct = ctx;
    }

    @Override
    public void run() {
        StringBuilder sb3 = new StringBuilder();
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
                    //AppConstants.writeInFile(TAG + " Link 3: Received text: " + Response);
                    //run = false;

                    if (strcmd.equalsIgnoreCase(BTConstants.INFO_COMMAND) && Response.contains("records")) {
                        BTConstants.IS_NEW_VERSION_LINK_THREE = true;
                    }

                    if (Response.contains("$$")) {
                        String res = Response.replace("$$", "");
                        if (res.contains("}")) {
                            res = res.substring(0, (res.lastIndexOf("}") + 1)); // To remove extra characters after the last curly bracket (if any)
                        }
                        if (!res.trim().isEmpty()) {
                            sb3.append(res.trim());
                        }
                        sendBroadcastIntentFromLinkThree(sb3.toString());
                        sb3.setLength(0);
                    } else {
                        if (BTConstants.IS_NEW_VERSION_LINK_THREE) {
                            sb3.append(Response);
                        } else {
                            // For old version Link response
                            sb3.setLength(0);
                            sendBroadcastIntentFromLinkThree(Response + '\n');
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

    public void sendBroadcastIntentFromLinkThree(String resp) {
        //AppConstants.writeInFile(TAG + " Link 3: Final Response: " + resp);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("BroadcastBlueLinkThreeData");
        broadcastIntent.putExtra("Request", strcmd);
        broadcastIntent.putExtra("Response", resp.trim());
        broadcastIntent.putExtra("Action", "BlueLinkThree");
        ct.sendBroadcast(broadcastIntent);
    }
}
