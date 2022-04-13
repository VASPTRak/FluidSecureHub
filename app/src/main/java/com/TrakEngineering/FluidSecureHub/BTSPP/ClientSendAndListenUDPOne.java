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

public class ClientSendAndListenUDPOne implements Runnable {

    String strcmd = "";
    String SERVER_IP = "";
    Context ct;

    public ClientSendAndListenUDPOne(String info_cmd, String server_ip, Context ctx) {

         strcmd = info_cmd;
         SERVER_IP = server_ip;
         ct = ctx;
    }

    @Override
    public void run() {

        boolean run = true;
        try {

            DatagramSocket udpSocket = new DatagramSocket();
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            byte[] buf = strcmd.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, Constants.PORT);
            udpSocket.send(packet);
            while (run) {
                try {
                    byte[] message = new byte[8000];
                    DatagramPacket p = new DatagramPacket(message, message.length);
                    Log.i("UDP client: ", "about to wait to receive");
                    udpSocket.setSoTimeout(10000);
                    udpSocket.receive(p);
                    String text = new String(message, 0, p.getLength());
                    Log.d("Received text", text);
                    //run = false;

                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction("BroadcastBlueLinkOneData");
                    broadcastIntent.putExtra("Request", strcmd);
                    broadcastIntent.putExtra("Response", text);
                    broadcastIntent.putExtra("Action", "BlueLinkOne");
                    ct.sendBroadcast(broadcastIntent);

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
}
