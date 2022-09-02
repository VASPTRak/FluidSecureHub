package com.TrakEngineering.FluidSecureHub;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.TrakEngineering.FluidSecureHub.enity.StatusForUpgradeVersionEntity;
import com.TrakEngineering.FluidSecureHub.enity.UpgradeVersionEntity;
import com.TrakEngineering.FluidSecureHub.enity.UserInfoEntity;
import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.TrakEngineering.FluidSecureHub.WelcomeActivity.wifiApManager;
import static com.TrakEngineering.FluidSecureHub.server.ServerHandler.TEXT;

public class BackgroundServiceKeepDataTransferAlive extends BackgroundService {


    String HTTP_URL_TEST = "";
    String HTTP_URL = "";
    String URL_UPGRADE_START = HTTP_URL + "upgrade?command=start";
    String URL_RESET = HTTP_URL + "upgrade?command=reset";
    String URL_INFO = HTTP_URL + "client?command=info";
    String URL_WIFI = HTTP_URL + "config?command=wifi";//returns connected link information
    private static final String TAG = AppConstants.LOG_MAINTAIN + "-" + "BS_KeepAlive";
    public static ArrayList<HashMap<String, String>> SSIDList = new ArrayList<>();
    public static ArrayList<HashMap<String, String>> DetailslistOfConnectedIP_KDTA = new ArrayList<>();
    public static ArrayList<HashMap<String, String>> DefectiveLinks = new ArrayList<>();
    public ArrayList<String> listOfConnectedMacAddress_KDTA = new ArrayList<String>();

    private static int SERVER_PORT = 80;
    private static String SERVER_IP = "";
    public static boolean IstoggleRequired_KDTA, IstoggleRequired_DA;
    public String ToggleExeTime = "", CurrentTime = "";
    Date date1, date2;
    //public String hotspotConnDT0 = "", hotspotConnDT1 = "", hotspotConnDT2 = "", hotspotConnDT3 = "";
    //public String InfoCmdConnDT0 = "", InfoCmdConnDT1 = "", InfoCmdConnDT2 = "", InfoCmdConnDT3 = "";

    @SuppressLint("LongLogTag")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            super.onStart(intent, startId);
            //Log.e(TAG, "~~~~~start~~~~~");
            //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "~~~~~start~~~~~");
            if (WelcomeActivity.OnWelcomeActivity && Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE") && Constants.FS_5STATUS.equalsIgnoreCase("FREE") && Constants.FS_6STATUS.equalsIgnoreCase("FREE")) {

                ListConnectedHotspotIP_KDTA();
                StartUpgradeProcess();

            } else {
                Log.i(TAG, "Skip keepAlive not on Welcome activity or one of the transaction is running.");
            }

            //Log.e(TAG, "~~~~~stop~~~~~");

        } catch (NullPointerException e) {
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "  onStartCommand Execption " + e);
            Log.d("Ex", e.getMessage());
            this.stopSelf();
        }

        // return super.onStartCommand(intent, flags, startId);
        return Service.START_NOT_STICKY;
    }

    @SuppressLint("LongLogTag")
    public void StartUpgradeProcess() {

        try {

            DefectiveLinks.clear();
            //Log.e(TAG, "~~~~~Second for strt~~~~~");
            if (SSIDList != null && SSIDList.size() > 0) {

                //Log.i(TAG, "Hotspot connected devices: " + String.valueOf(AppConstants.DetailsListOfConnectedDevices.size()));
                for (int i = 0; i < SSIDList.size(); i++) {

                    boolean IsHoseBusy = IsHoseBusyCheckLocally();
                    String ReconfigureLink = SSIDList.get(i).get("ReconfigureLink");
                    String selSSID = SSIDList.get(i).get("WifiSSId");
                    String IsBusy = SSIDList.get(i).get("IsBusy");
                    String selMacAddress = SSIDList.get(i).get("MacAddress");
                    String selSiteId = SSIDList.get(i).get("SiteId");
                    String hoseID = SSIDList.get(i).get("HoseId");
                    String IsUpgrade = SSIDList.get(i).get("IsUpgrade"); //"Y";//
                    String LinkCommunicationType = "HTTP";
                    try {
                        LinkCommunicationType = SSIDList.get(i).get("LinkCommunicationType");
                    } catch (Exception e) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " Exception while getting LinkCommunicationType of WifiSSId (" + selSSID + "). " + e.getMessage());
                    }

                    if (LinkCommunicationType != null) {
                        if (!LinkCommunicationType.equalsIgnoreCase("HTTP")) {
                            continue;
                        }
                    } else {
                        continue;
                    }

                    boolean IsConnectedToHotspot = false;
                    boolean IsinfoCmdSuccess = false;

                    if (!IsFsConnected(selMacAddress)) {

                        IstoggleRequired_KDTA = true;
                        Log.i(TAG, "Link Not Connected ~" + "MacAddress:" + selMacAddress + " SSID:" + selSSID);

                    }

                    for (int k = 0; k < AppConstants.DetailsListOfConnectedDevices.size(); k++) {
                        String Mac_Addr = AppConstants.DetailsListOfConnectedDevices.get(k).get("macAddress");

                        //Check for connected link,If connceted execute info command
                        //also include upgrate link firmware code
                        if (IsHoseBusy && selMacAddress.equalsIgnoreCase(Mac_Addr)) {

                            IsConnectedToHotspot = true;
                            if (i == 0) {
                                SaveDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"hotspotConnDT0");
                                //hotspotConnDT0 = CommonUtils.getTodaysDateTemp();//Link at 0th position
                            } else if (i == 1) {
                                SaveDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"hotspotConnDT1");
                                //hotspotConnDT1 = CommonUtils.getTodaysDateTemp();//Link at 1th position
                            } else if (i == 2) {
                                SaveDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"hotspotConnDT2");
                                //hotspotConnDT2 = CommonUtils.getTodaysDateTemp();//Link at 2th position
                            } else if (i == 3) {
                                SaveDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"hotspotConnDT3");
                                //hotspotConnDT3 = CommonUtils.getTodaysDateTemp();//Link at 3th position
                            }else if (i == 4) {
                                SaveDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"hotspotConnDT4");

                            }else if (i == 5) {
                                SaveDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"hotspotConnDT5");

                            }


                            try {

                                SERVER_IP = AppConstants.DetailsListOfConnectedDevices.get(k).get("ipAddress");
                                if (!SERVER_IP.equalsIgnoreCase("")) {

                                    //new TCPClientTask().execute(SERVER_IP);
                                    //new UDPClientTask().execute(SERVER_IP);
                                    //Http info cmd
                                    HTTP_URL = "http://" + SERVER_IP + ":80/";
                                    URL_INFO = HTTP_URL + "client?command=info";
                                    URL_WIFI = HTTP_URL + "config?command=wifi";



                                    String FSStatus = new CommandsGET().execute(URL_INFO).get();//Info command
                                    Log.i(TAG, "Info cmd Status:" + FSStatus);

                                    if (FSStatus.contains("Version")) {

                                        IsinfoCmdSuccess = true;
                                        if (i == 0) {
                                            SaveDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"InfoCmdConnDT0");
                                            //InfoCmdConnDT0 = CommonUtils.getTodaysDateTemp();//Link at 0th position
                                        } else if (i == 1) {
                                            SaveDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"InfoCmdConnDT1");
                                            //InfoCmdConnDT1 = CommonUtils.getTodaysDateTemp();//Link at 1th position
                                        } else if (i == 2) {
                                            SaveDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"InfoCmdConnDT2");
                                            //InfoCmdConnDT2 = CommonUtils.getTodaysDateTemp();//Link at 2th position
                                        } else if (i == 3) {
                                            SaveDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"InfoCmdConnDT3");
                                            //InfoCmdConnDT3 = CommonUtils.getTodaysDateTemp();//Link at 3th position
                                        } else if (i == 4) {
                                            SaveDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"InfoCmdConnDT4");

                                        } else if (i == 5) {
                                            SaveDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"InfoCmdConnDT5");

                                        }

                                    } else {
                                        IsinfoCmdSuccess = false;
                                    }

                                }

                                //TODO below code for Upgrade firmware
                                /*Log.i(TAG, "HTTP_URL_TEST: " + HTTP_URL_TEST);
                                //If ipaddress is not empty
                                String iot_version = "";

                                URL_INFO = HTTP_URL_TEST + "client?command=info";
                                if (AppConstants.GenerateLogs)
                                    if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " Sending getinfo to hose: " + selSSID);
                                String FSStatus = new CommandsGET().execute(URL_INFO).get();//Info command
                                if (FSStatus.startsWith("{") && FSStatus.contains("Version") && IsUpgrade.equalsIgnoreCase("Y")) {
                                    Log.i(TAG, "Info Command response" + FSStatus);
                                    if (AppConstants.GenerateLogs)
                                        if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " got info response from hose : " + selSSID + "\n" + FSStatus);

                                    HTTP_URL = HTTP_URL_TEST;
                                    URL_UPGRADE_START = HTTP_URL + "upgrade?command=start";
                                    URL_RESET = HTTP_URL + "upgrade?command=reset";

                                    try {

                                        JSONObject jsonObj = new JSONObject(FSStatus);
                                        String userData = jsonObj.getString("Version");
                                        JSONObject jsonObject = new JSONObject(userData);

                                        String sdk_version = jsonObject.getString("sdk_version");
                                        String mac_address = jsonObject.getString("mac_address");
                                        iot_version = jsonObject.getString("iot_version");

                                        DownloadFirmwareFile();//Download firmware file

                                        if (i == 0) {
                                            //Hose one selected
                                            WelcomeActivity.IsUpgradeInprogress_FS1 = true;
                                        } else if (i == 1) {
                                            //Hose two selected
                                            WelcomeActivity.IsUpgradeInprogress_FS2 = true;
                                        } else if (i == 2) {
                                            //Hose three selected
                                            WelcomeActivity.IsUpgradeInprogress_FS3 = true;
                                        } else if (i == 3) {
                                            //Hose four selected
                                            WelcomeActivity.IsUpgradeInprogress_FS4 = true;
                                        } else {
                                            //Something went wrong
                                        }

                                        if (!iot_version.equalsIgnoreCase("")) {
                                            CheckForUpdateFirmware(hoseID, iot_version, String.valueOf(i));
                                        }


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    Log.i(TAG, "Info Command response" + FSStatus);
                                    if (AppConstants.GenerateLogs)
                                        if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " got info response from hose : " + selSSID + "\n" + FSStatus);
                                }*/

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    //CheckFor #728  Inability to Connect to Links
                    if (IsHoseBusy) {
                        CheckInabilityToConnectLinks(i, selSSID, IsConnectedToHotspot, IsinfoCmdSuccess);
                    }

                }

                // Log.e(TAG, "~~~~~Second for end~~~~~");
            } else {
                Log.i(TAG, "SSID List Empty");
                if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + " SSID List Empty");
            }
            if (IsHoseBusyCheckLocally()) {
                int s = DefectiveLinks.size();
                System.out.println("S" + s);

                if (DefectiveLinks != null && DefectiveLinks.size() > 0) {

                    for (int p = 0; p < DefectiveLinks.size(); p++) {

                        String link_name = DefectiveLinks.get(p).get("Selected_SSID");
                        int Differance = Integer.parseInt(DefectiveLinks.get(p).get("diff_min"));
                        String Message = DefectiveLinks.get(p).get("Message");

                        if (Differance > 60){

                           boolean Sendmail = checkSharedPrefDefectiveLink(BackgroundServiceKeepDataTransferAlive.this,link_name);
                           if (Sendmail){
                               setSharedPrefDefectiveLink(BackgroundServiceKeepDataTransferAlive.this,link_name);
                               Log.i(TAG, "Defective links email sent to: "+link_name+" Message: "+Message+" TDifferance: "+Differance);
                               if (AppConstants.GenerateLogs)
                                   //AppConstants.WriteinFile(TAG + "Defective links email sent to: "+link_name+" Message: "+Message+" TDifference: "+Differance);
                                   AppConstants.WriteinFile(TAG + " LINK (" + link_name + ") did not respond to KeepAlive");
                               SendDefectiveLinkInfoEmailAsyncCall(link_name);
                           }
                        }

                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "  StartUpgradeProcess Exception: " + e);
        }


        //Check If hotspot Toggle required
        boolean IsHoseBusy = IsHoseBusyCheckLocally();
        String CurrDate = CommonUtils.getTodaysDateTemp();
        int diff = getDate(CurrDate);

        if (IsHoseBusy && WelcomeActivity.OnWelcomeActivity && IstoggleRequired_DA) {
            //Toggle required by Display meater activity
            //Log.i(TAG,"Toggle ~~ Display MeterActivity");
            if (AppConstants.DisableAllRebootOptions.equalsIgnoreCase("N") && !AppConstants.IsBTLinkSelectedCurrently) {
                ToggleHotspot();
            }
        } else if (IsHoseBusy && WelcomeActivity.OnWelcomeActivity && IstoggleRequired_KDTA && diff > 60) {
            //Toggle required by KeepDataAlive background service activity
            //Log.i(TAG,"Toggle ~~ KeepDataAlive BS");
            if (AppConstants.DisableAllRebootOptions.equalsIgnoreCase("N") && !AppConstants.IsBTLinkSelectedCurrently) {
                ToggleHotspot();
            }
        } else {
            //Log.i(TAG,"Toggle ~~ No Need to toggle");
        }

    }

    public void ListConnectedHotspotIP_KDTA() {


        listOfConnectedMacAddress_KDTA.clear();
        DetailslistOfConnectedIP_KDTA.clear();
        // Log.e(TAG, "~~~~~First for strt~~~~~");

        try {

            for (int k = 0; k < AppConstants.DetailsListOfConnectedDevices.size(); k++) {

                String macAddress = AppConstants.DetailsListOfConnectedDevices.get(k).get("macAddress");
                String ipAddress = AppConstants.DetailsListOfConnectedDevices.get(k).get("ipAddress");

                if (ipAddress != null || macAddress != null) {

                    HashMap<String, String> map = new HashMap<>();
                    map.put("ipAddress", ipAddress);
                    map.put("macAddress", macAddress);

                    DetailslistOfConnectedIP_KDTA.add(map);

                    //Add IP of all connected decices to list
                    listOfConnectedMacAddress_KDTA.add(macAddress);
                }

            }

            // Log.e(TAG, "~~~~~First for end~~~~~");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean isNotNULL(String value) {

        boolean flag = true;
        if (value == null) {
            flag = false;
        } else if (value.trim().isEmpty()) {
            flag = false;
        } else if (value != null && value.trim().equalsIgnoreCase("null")) {
            flag = false;
        }

        return flag;
    }

    public class CommandsGET extends AsyncTask<String, Void, String> {

        public String resp = "";


        protected String doInBackground(String... param) {

            // Log.e(TAG, "~~~~~Http for strt~~~~~");
            try {

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(4, TimeUnit.SECONDS);
                client.setReadTimeout(4, TimeUnit.SECONDS);

                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                request.urlString();
                System.out.println("urlStr" + request.urlString());
                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            try {

                // Log.e(TAG, "~~~~~Http for stop~~~~~RES:"+result);
                System.out.println(result);

            } catch (Exception e) {

                System.out.println(e);
            }

        }
    }

    @SuppressLint("LongLogTag")
    public void CheckForUpdateFirmware(final String hoseid, String iot_version, final String FS_selected) {

        Log.i(TAG, "Upgrade for Hose: " + FS_selected + "\nFirmware Version: " + iot_version + "Hose ID: " + hoseid);
        if (AppConstants.GenerateLogs)
            AppConstants.WriteinFile(TAG + "  Upgrade for Hose: " + FS_selected + "\nFirmware Version: " + iot_version + "Hose ID: " + hoseid);

        SharedPreferences sharedPrefODO = this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String HubId = sharedPrefODO.getString(AppConstants.HubId, "");// HubId equals to personId

        final UpgradeVersionEntity objEntityClass = new UpgradeVersionEntity();
        objEntityClass.IMEIUDID = AppConstants.getIMEI(BackgroundServiceKeepDataTransferAlive.this);
        objEntityClass.Email = CommonUtils.getCustomerDetails_KdtAlive(BackgroundServiceKeepDataTransferAlive.this).PersonEmail;
        objEntityClass.HoseId = hoseid;
        objEntityClass.Version = iot_version;

        Gson gson1 = new Gson();
        String jsonData1 = gson1.toJson(objEntityClass);
        String authString1 = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundServiceKeepDataTransferAlive.this) + ":" + CommonUtils.getCustomerDetails_KdtAlive(BackgroundServiceKeepDataTransferAlive.this).PersonEmail + ":" + "UpgradeCurrentVersionWithUgradableVersion");

        if (hoseid != null && !hoseid.trim().isEmpty()) {

            try {

                //First call which will Update Fs firmware to Server--
                String response = new UpgradeCurrentVersionWithUgradableVersion_test().execute(jsonData1, authString1).get();

                System.out.println("BS__KeepDataTransferAlive--resp" + response);

                JSONObject jsonObject = null;
                jsonObject = new JSONObject(response);

                String ResponceMessage = jsonObject.getString("ResponceMessage");
                String ResponceText = jsonObject.getString("ResponceText");

                if (ResponceMessage.equalsIgnoreCase("success")) {

                    StatusForUpgradeVersionEntity objEntityClass1 = new StatusForUpgradeVersionEntity();
                    objEntityClass1.IMEIUDID = AppConstants.getIMEI(BackgroundServiceKeepDataTransferAlive.this);
                    objEntityClass1.Email = CommonUtils.getCustomerDetails_KdtAlive(BackgroundServiceKeepDataTransferAlive.this).PersonEmail;
                    objEntityClass1.HoseId = hoseid;
                    objEntityClass1.PersonId = HubId;

                    Gson gson = new Gson();
                    String jsonData = gson.toJson(objEntityClass1);

                    String userEmail = CommonUtils.getCustomerDetails_KdtAlive(BackgroundServiceKeepDataTransferAlive.this).PersonEmail;
                    String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundServiceKeepDataTransferAlive.this) + ":" + userEmail + ":" + "IsUpgradeCurrentVersionWithUgradableVersion");

                    //Second call will get Status for firwareupdate
                    new GetUpgrateFirmwareStatus().execute(FS_selected, jsonData, authString);

                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, " UpgradeCurrentVersionWithUgradableVersion 1 " + e);
                //   if (AppConstants.GenerateLogs)AppConstants.WriteinFile( TAG+"  UpgradeCurrentVersionWithUgradableVersion 1 " + e);
            }

        } else {
            Log.i(TAG, "Upgrade fail Hose id empty");
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "  Upgrade fail Hose id empty");
        }

    }

    public class CommandsPOST extends AsyncTask<String, Void, String> {

        public String resp = "";


        @SuppressLint("LongLogTag")
        protected String doInBackground(String... param) {

            try {

                MediaType JSON = MediaType.parse("application/json");

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(15, TimeUnit.SECONDS);
                client.setReadTimeout(15, TimeUnit.SECONDS);
                client.setWriteTimeout(15, TimeUnit.SECONDS);

                RequestBody body = RequestBody.create(JSON, param[1]);

                Request request = new Request.Builder()
                        .url(param[0])
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.i(TAG, " CommandsPOST Exception" + e);
                //   if (AppConstants.GenerateLogs)AppConstants.WriteinFile( TAG+"  CommandsPOST doInBackground Execption " + e);
                stopSelf();
            }


            return resp;
        }

        @SuppressLint("LongLogTag")
        @Override
        protected void onPostExecute(String result) {


            try {

                System.out.println(" OUTPUT" + result);

            } catch (Exception e) {
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  CommandsPOST onPostExecute Execption " + e);
                Log.i(TAG, " CommandsPOST Exception" + e);
                stopSelf();
            }

        }
    }

    public class OkHttpFileUpload extends AsyncTask<String, Void, String> {

        public String resp = "";


        @SuppressLint("LongLogTag")
        protected String doInBackground(String... param) {


            try {
                String LocalPath = param[0];
                String Localcontenttype = param[1];

                MediaType contentype = MediaType.parse(Localcontenttype);

                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(contentype, readBytesFromFile(LocalPath));
                Request request = new Request.Builder()
                        .url(HTTP_URL)//HTTP_URL  192.168.43.210
                        .post(body)
                        .build();


                Response response = client.newCall(request).execute();
                Log.i(TAG, " OkHttpFileUpload doInBackground Response" + response);
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                return response.body().string();

            } catch (Exception e) {
                ChangeUpgradeProcessFlag();
                Log.i(TAG, " OkHttpFileUpload doInBackground Exception" + e);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  OkHttpFileUpload doInBackground Exception" + e);
            }


            return resp;
        }

        @SuppressLint("LongLogTag")
        @Override
        protected void onPostExecute(String result) {
            System.out.println(" resp......." + result);

            // pd.dismiss();
            try {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            String ResetRespo = new CommandsPOST().execute(URL_RESET, "").get();
                            Log.i(TAG, " Reset command Response: " + ResetRespo);
                            //   if (AppConstants.GenerateLogs)AppConstants.WriteinFile( TAG+"  Reset command Response: " + ResetRespo);

                        } catch (Exception e) {

                            Log.i(TAG, " OkHttpFileUpload CommandsPOST Exception" + e);
                            //   if (AppConstants.GenerateLogs)AppConstants.WriteinFile( TAG+"  OkHttpFileUpload CommandsPOST doInBackground Execption " + e);
                        }
                        ChangeUpgradeProcessFlag();
                        System.out.println("AFTER SECONDS 5");
                    }
                }, 5000);


            } catch (Exception e) {
                ChangeUpgradeProcessFlag();
                Log.i(TAG, " OkHttpFileUpload onPostExecute Exception" + e);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  OkHttpFileUpload onPostExecute Exception" + e);

            }

        }
    }

    private static byte[] readBytesFromFile(String filePath) {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {

            File file = new File(filePath);
            bytesArray = new byte[(int) file.length()];

            //read file into bytes[]
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return bytesArray;

    }

    public void ChangeUpgradeProcessFlag() {

        WelcomeActivity.IsUpgradeInprogress_FS1 = false;
        WelcomeActivity.IsUpgradeInprogress_FS2 = false;
        WelcomeActivity.IsUpgradeInprogress_FS3 = false;
        WelcomeActivity.IsUpgradeInprogress_FS4 = false;
        WelcomeActivity.IsUpgradeInprogress_FS5 = false;
        WelcomeActivity.IsUpgradeInprogress_FS6 = false;

    }

    @SuppressLint("LongLogTag")
    public void DownloadFirmwareFile() {

        //File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "FSBin");
        File folder = new File(String.valueOf(getApplicationContext().getExternalFilesDir(AppConstants.FOLDER_BIN)));
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
            // Do something on success
        } else {
            //AppConstants.AlertDialogBox(WelcomeActivity.this, "Please check File is present in FSBin Folder in Internal(Device) Storage");
            System.out.println("Please check File is present in FSBin Folder in Internal(Device) Storage");
            Log.i(TAG, " Please check File is present in FSBin Folder in Internal(Device) Storage");
            //    if (AppConstants.GenerateLogs)AppConstants.WriteinFile( TAG+"  Please check File is present in FSBin Folder in Internal(Device) Storage");
        }

        if (AppConstants.UP_FilePath != null)
            new DownloadFileFromURL().execute(String.valueOf(getApplicationContext().getExternalFilesDir(AppConstants.FOLDER_BIN)), AppConstants.UP_Upgrade_File_name);

    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                // getting file length
                int lenghtOfFile = conection.getContentLength();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file
                OutputStream output = new FileOutputStream(getApplicationContext().getExternalFilesDir(AppConstants.FOLDER_BIN) + "/" + f_url[1]);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }


        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            //pDialog.setProgress(Integer.parseInt(progress[0]));
        }


    }

    public class GetUpgrateFirmwareStatus extends AsyncTask<String, Void, String> {

        String FS_selected;
        String jsonData;
        String authString;


        @SuppressLint("LongLogTag")
        @Override
        protected String doInBackground(String... params) {

            String response = "";
            try {

                FS_selected = params[0];
                jsonData = params[1];
                authString = params[2];

                System.out.println("jsonData--" + jsonData);
                System.out.println("authString--" + authString);


                response = serverHandler.PostTextData(BackgroundServiceKeepDataTransferAlive.this, AppConstants.webURL, jsonData, authString);

                System.out.println("Id..." + jsonData);

            } catch (Exception e) {
                Log.i(TAG, " GetUpgrateFirmwareStatus doInBackground " + e);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  GetUpgrateFirmwareStatus doInBackground " + e);
            }

            return response;
        }

        @SuppressLint("LongLogTag")
        @Override
        protected void onPostExecute(String resp) {

            System.out.println("resp..." + resp);

            try {
                JSONObject jsonObj = new JSONObject(resp);

                String ResponceMessage = jsonObj.getString(AppConstants.RES_MESSAGE);
                String ResponceText = jsonObj.getString(AppConstants.RES_TEXT);

                if (ResponceMessage.equalsIgnoreCase("success")) {

                    if (ResponceText.trim().equalsIgnoreCase("Y")) {

                        Log.i(TAG, " GetUpgrateFirmwareStatus URL_UPGRADE_START: " + URL_UPGRADE_START);
                        Log.i(TAG, " GetUpgrateFirmwareStatus ResponceText: " + ResponceText.trim());

                        String cmpresponse = new CommandsPOST().execute(URL_UPGRADE_START, "").get();
                        Log.i(TAG, " GetUpgrateFirmwareStatus CommandsPOST Response" + cmpresponse);
                        //   if (AppConstants.GenerateLogs)AppConstants.WriteinFile( TAG+"  GetUpgrateFirmwareStatus CommandsPOST Response" + cmpresponse);

                        //upgrade bin
                        String LocalPath = getApplicationContext().getExternalFilesDir(AppConstants.FOLDER_BIN) + "/" + AppConstants.UP_Upgrade_File_name;
                        File f = new File(LocalPath);
                        if (f.exists()) {

                            Log.i(TAG, "~~~OkHttpFileUpload~~~");
                            new OkHttpFileUpload().execute(LocalPath, "application/binary");

                        } else {
                            Toast.makeText(getApplicationContext(), "File Not found " + LocalPath, Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Log.i(TAG, " GetUpgrateFirmwareStatus Upgrade flag No");
                        //AppConstants.UP_Upgrade_fs1 = false;
                        ChangeUpgradeProcessFlag();
                    }

                } else {
                    ChangeUpgradeProcessFlag();
                    Log.i(TAG, " GetUpgrateFirmwareStatus Something Went wrong");
                    //   if (AppConstants.GenerateLogs)AppConstants.WriteinFile( TAG+"  GetUpgrateFirmwareStatus Something Went wrong");
                }


            } catch (Exception e) {

                Log.i(TAG, " GetUpgrateFirmwareStatus onPostExecute " + e);
                //   if (AppConstants.GenerateLogs)AppConstants.WriteinFile( TAG+"  GetUpgrateFirmwareStatus onPostExecute " + e);
            }

        }
    }

    public class UpgradeCurrentVersionWithUgradableVersion_test extends AsyncTask<String, Void, String> {

        String jsonData;
        String authString;


        @SuppressLint("LongLogTag")
        @Override
        protected String doInBackground(String... params) {

            String response = "";
            try {

                jsonData = params[0];
                authString = params[1];

                System.out.println("jsonData--" + jsonData);
                System.out.println("authString--" + authString);


                response = serverHandler.PostTextData(BackgroundServiceKeepDataTransferAlive.this, AppConstants.webURL, jsonData, authString);

                System.out.println("Id..." + jsonData);

            } catch (Exception e) {
                Log.i(TAG, " GetUpgrateFirmwareStatus doInBackground " + e);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  GetUpgrateFirmwareStatus doInBackground " + e);
            }

            return response;
        }

        @SuppressLint("LongLogTag")
        @Override
        protected void onPostExecute(String resp) {

            System.out.println("resp..." + resp);


        }
    }

    public boolean IsHoseBusyCheckLocally() {

        if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE") && Constants.FS_5STATUS.equalsIgnoreCase("FREE") && Constants.FS_6STATUS.equalsIgnoreCase("FREE")) {
            return true;
        }

        return false;
    }

    public class TCPClientTask extends AsyncTask<String, Void, String> {

        String response = "";

        @SuppressLint("LongLogTag")
        @Override
        protected String doInBackground(String... serverip) {

            // Log.e(TAG, "~~~~~TCP for strt~~~~~");
            String SERVER_IP = serverip[0];
            String strcmd = "GET /client?command=info HTTP/1.1\r\nContent-Type: application/json; charset=utf-8\r\nContent-Length: \r\nHost: 192.168.4.1\r\nConnection: Keep-Alive\r\nAccept-Encoding: gzip\r\nUser-Agent: okhttp/3.6.0\r\n\r\n";


            try {
                String host = SERVER_IP;//"192.168.43.210";//url.getHost();
                int port = SERVER_PORT;

                // Open a TCP connection
                Socket socket = new Socket(host, port);
                // Send the request over the socket
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                writer.print(strcmd);
                writer.flush();
                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                StringBuilder InfoRespo = new StringBuilder();
                String next_record = null;
                while ((next_record = reader.readLine()) != null) {

                    InfoRespo.append(next_record);

                }
                //Log.i(TAG, " InfoRespo: " + InfoRespo);
                response = InfoRespo.toString();
                socket.close();
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return response;
        }

        @SuppressLint("LongLogTag")
        @Override
        protected void onPostExecute(String res) {

            //Log.e(TAG, "~~~~~TCP for end~~~~~");
            Log.i(TAG, "Socket response" + res);
            if (!res.contains("Version")) {
                //new UDPClientTask().execute(SERVER_IP);
            }

        }

    }

    public class UDPClientTask extends AsyncTask<String, Void, String> {

        String response = "";

        @SuppressLint("LongLogTag")
        @Override
        protected String doInBackground(String... serverip) {

            String SERVER_IP = serverip[0];
            String strcmd = "/client?command=info"; //"GET /client?command=info HTTP/1.1\r\nContent-Type: application/json; charset=utf-8\r\nContent-Length: \r\nHost: 192.168.4.1\r\nConnection: Keep-Alive\r\nAccept-Encoding: gzip\r\nUser-Agent: okhttp/3.6.0\r\n\r\n";

            try {
                String messageStr = strcmd;
                int server_port = 80;
                InetAddress local = InetAddress.getByName(SERVER_IP);
                int msg_length = messageStr.length();
                byte[] message = messageStr.getBytes();


                DatagramSocket s = new DatagramSocket();

                DatagramPacket p = new DatagramPacket(message, msg_length, local, server_port);
                s.send(p);//properly able to send data. i receive data to server

               /* for (int i = 0; i <= 20; i++) {
                    final int value = i;
                    message = new byte[30000];
                    p = new DatagramPacket(message,message.length );
                    s.receive(p); //keeps on waiting here but i am sending data back from server, but it never receives
                    final byte[] data =  p.getData();;
                    try {

                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }*/
            } catch (Exception ex) {
                ex.printStackTrace();
            }


           /* try {
                String host = SERVER_IP;//"192.168.43.210";//url.getHost();
                int port = SERVER_PORT;


                byte[] message = strcmd.getBytes();

                // Get the internet address of the specified host
                InetAddress address = InetAddress.getByName(host);

                // Initialize a datagram packet with data and address
                DatagramPacket packet = new DatagramPacket(message, message.length,
                        address, port);

                // Create a datagram socket, send the packet through it, close it.
                DatagramSocket dsocket = new DatagramSocket();
                dsocket.send(packet);
                dsocket.close();
                System.out.println("Sent");
            } catch (Exception e) {
                System.err.println(e);
            }*/

            return response;
        }

        @SuppressLint("LongLogTag")
        @Override
        protected void onPostExecute(String res) {

            Log.i(TAG, "Socket response" + res);

        }

    }

    public boolean IsFsConnected(String toMatchString) {

        for (String MacAddress : listOfConnectedMacAddress_KDTA) {
            if (MacAddress.contains(toMatchString))
                return true;
        }
        return false;
    }

    public void ToggleHotspot() {

        ToggleExeTime = CommonUtils.getTodaysDateTemp();//Date Two (d2)

        wifiApManager.setWifiApEnabled(null, false);  //Hotspot Disable

        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        wifiApManager.setWifiApEnabled(null, true);  //Hotspot enabled

        IstoggleRequired_KDTA = false;
        IstoggleRequired_DA = false;
        Log.i(TAG, "ToggleHotspot finish");
        AppConstants.WriteinFile(TAG + "  ToggleHotspot KDTalive");
    }

    private int getDate(String CurrentTime) {

        int DiffTime = 0;
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date1 = sdf.parse(CurrentTime);
            date2 = sdf.parse(ToggleExeTime);

            long diff = date1.getTime() - date2.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            DiffTime = (int) minutes;
            //System.out.println("~~~Difference~~~" + minutes);

        } catch (ParseException e) {
            if (ToggleExeTime.equalsIgnoreCase("")) {
                ToggleExeTime = CommonUtils.getTodaysDateTemp();
            }
            e.printStackTrace();
        } catch (NullPointerException n) {
            n.printStackTrace();
        }

        return DiffTime;
    }


    /*void CheckIfLinkNeedsRename(String URL_WIFI) {

        String LinkInfo = "";
        try {
            Log.i(TAG, "Info cmd LinkInfo:" + LinkInfo);
            LinkInfo = new CommandsGET().execute(URL_WIFI).get();//Info command

            JSONObject jsonObj0 = new JSONObject(LinkInfo);
            JSONObject jsonObj1 = new JSONObject(String.valueOf(jsonObj0.get("Response")));
            JSONObject jsonObj2 = new JSONObject(String.valueOf(jsonObj1.get("Softap")));
            JSONObject jsonObj3 = new JSONObject(String.valueOf(jsonObj2.get("Connect_Softap")));
            String ssid = jsonObj3.getString("ssid");
            String password = jsonObj3.getString("password");
            String channel = jsonObj3.getString("channel");


                String jsonRename = "{\"Request\":{\"Softap\":{\"Connect_Softap\":{\"authmode\":\"WPAPSK/WPA2PSK\",\"channel\":\"" + channel + "\",\"ssid\":\"" + ssid + "\",\"password\":\"123456789\"}}}}";
                new CommandsPOST().execute(URL_WIFI, jsonRename);

        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "Info cmd LinkInfo:" + LinkInfo);
        }

    }*/

    private void CheckInabilityToConnectLinks(int position, String Selected_SSID, boolean IsConnectedToHotspot, boolean IsinfoCmdSuccess) {

        try {
            Log.i(TAG, "CheckDetails \n------------------------------\nSelected SSID:" + Selected_SSID + "\nIsConnectedToHotspot:" + IsConnectedToHotspot + "\nIsinfoCmdSuccess:" + IsinfoCmdSuccess + "\n------------------------");

            String CurrDate = CommonUtils.getTodaysDateTemp();
            String Message = "";
            String InfoCmdConnDT = "";
            String hotspotConnDT = "";
            int diff_min = 0;

            if (position == 0) {
                InfoCmdConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"InfoCmdConnDT0");
                hotspotConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"hotspotConnDT0");
            } else if (position == 1) {
                InfoCmdConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"InfoCmdConnDT1");
                hotspotConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"hotspotConnDT1");
            } else if (position == 2) {
                InfoCmdConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"InfoCmdConnDT2");
                hotspotConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"hotspotConnDT2");
            } else if (position == 3) {
                InfoCmdConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"InfoCmdConnDT3");
                hotspotConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"hotspotConnDT3");
            } else if (position == 4) {
                InfoCmdConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"InfoCmdConnDT4");
                hotspotConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"hotspotConnDT4");
            } else if (position == 5) {
                InfoCmdConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"InfoCmdConnDT5");
                hotspotConnDT = GetDefectiveLinkDateTimeSharedPref(BackgroundServiceKeepDataTransferAlive.this,"hotspotConnDT5");
            }

            if (IsConnectedToHotspot) {

                if (IsinfoCmdSuccess) {
                    //InfoCommand Fail from diff_minutes
                    diff_min = getDiffinMinutes(CurrDate, InfoCmdConnDT);
                    Message = "Link working fine";
                } else {

                    //InfoCommand Fail from diff_minutes
                    diff_min = getDiffinMinutes(CurrDate, InfoCmdConnDT);
                    Message = "Fail at info command";

                }

            } else {

                if (hotspotConnDT.equals("") || InfoCmdConnDT.equals("")) {

                    //link never connected.
                    Message = "Link not connected yet";

                } else {

                    //link not connected to hotspot from diff_minutes
                    diff_min = getDiffinMinutes(CurrDate, hotspotConnDT);
                    Message = "Link not connected to hotspot";

                }

            }

            HashMap<String, String> map = new HashMap<>();
            map.put("Selected_SSID", Selected_SSID);
            map.put("diff_min", String.valueOf(diff_min));
            map.put("Message", Message);

            DefectiveLinks.add(map);

        } catch (Exception e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "CheckInabilityToConnectLinks Exception:" + e);
        }

    }

    private int getDiffinMinutes(String CurrentTime, String SuccessTime) {

        int DiffTime = 0;
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date1 = sdf.parse(CurrentTime);
            date2 = sdf.parse(SuccessTime);

            long diff = date1.getTime() - date2.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            DiffTime = (int) minutes;
            //System.out.println("~~~Difference~~~" + minutes);

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (NullPointerException n) {
            n.printStackTrace();
        }

        return DiffTime;
    }

    private void setSharedPrefDefectiveLink(Context myctx, String ssid) {

        String key = "last_date"+ssid;
        SharedPreferences sharedPref = myctx.getSharedPreferences("DefectiveLink", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, AppConstants.currentDateFormat("dd/MM/yyyy"));
        editor.apply();

    }

    private boolean checkSharedPrefDefectiveLink(Context myctx, String ssid) {

        String key = "last_date"+ssid;
        SharedPreferences sharedPrefODO = myctx.getSharedPreferences("DefectiveLink", Context.MODE_PRIVATE);
        String last_date = sharedPrefODO.getString(key, "");
        String curr_date = AppConstants.currentDateFormat("dd/MM/yyyy");

        if (curr_date.trim().equalsIgnoreCase(last_date.trim())) {
            return false;
        } else {
            return true;
        }

    }

    private void SaveDefectiveLinkDateTimeSharedPref(Context myctx, String linkposition) {

        SharedPreferences sharedPref = myctx.getSharedPreferences("DefectiveLinkDateTime", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(linkposition, CommonUtils.getTodaysDateTemp());
        editor.apply();

    }

    private String GetDefectiveLinkDateTimeSharedPref(Context myctx, String linkposition) {

        SharedPreferences sharedPrefODO = myctx.getSharedPreferences("DefectiveLinkDateTime", Context.MODE_PRIVATE);
        String last_date = sharedPrefODO.getString(linkposition, "");

          return last_date;
    }

    public void SendDefectiveLinkInfoEmailAsyncCall(String linkName) {

        UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetails_KdtAlive(BackgroundServiceKeepDataTransferAlive.this);

        StatusForUpgradeVersionEntity objEntityClass2 = new StatusForUpgradeVersionEntity();
        //objEntityClass2.IMEIUDID = AppConstants.getIMEI(BackgroundServiceKeepDataTransferAlive.this);
        //objEntityClass2.Email = CommonUtils.getCustomerDetails(WelcomeActivity.this).PersonEmail;
        objEntityClass2.HubName = userInfoEntity.PersonName;
        objEntityClass2.SiteName = userInfoEntity.FluidSecureSiteName;
        objEntityClass2.LinkName = linkName;

        Gson gson = new Gson();
        String parm2 = gson.toJson(objEntityClass2);

        String userEmail = CommonUtils.getCustomerDetails_KdtAlive(BackgroundServiceKeepDataTransferAlive.this).PersonEmail;
        //----------------------------------------------------------------------------------
        String parm1 = AppConstants.getIMEI(BackgroundServiceKeepDataTransferAlive.this) + ":" + userEmail + ":" + "DefectiveLinkInfoEmail";
        String authString = "Basic " + AppConstants.convertStingToBase64(parm1);


        RequestBody body = RequestBody.create(TEXT, parm2);
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(AppConstants.webURL)
                .post(body)
                .addHeader("Authorization", authString)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "error in getting response");
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(Response response) throws IOException {

                ResponseBody responseBody = response.body();
                if (!response.isSuccessful()) {
                    throw new IOException("Error response " + response);
                } else {

                    String result = responseBody.string();
                    System.out.println("Result" + result);
                    if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " SendDefectiveLinkInfoEmailAsyncCall ~Result\n" + result);

                    try {

                        JSONObject jsonObjectSite = null;
                        jsonObjectSite = new JSONObject(result);

                        String ResponseMessageSite = jsonObjectSite.getString(AppConstants.RES_MESSAGE);

                        if (ResponseMessageSite.equalsIgnoreCase("success")) {
                            //if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + " SendEmailReaderNotConnectedAsyncCall ~success");
                            System.out.println("SendDefectiveLinkInfoEmail send successfully ");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

        });
    }

}