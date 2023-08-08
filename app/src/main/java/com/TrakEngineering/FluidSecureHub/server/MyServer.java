package com.TrakEngineering.FluidSecureHub.server;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.AppConstants;
import com.TrakEngineering.FluidSecureHub.BackgroundServiceDownloadFirmware;
import com.TrakEngineering.FluidSecureHub.CommonUtils;
import com.TrakEngineering.FluidSecureHub.ConnectionDetector;
import com.TrakEngineering.FluidSecureHub.Constants;
import com.TrakEngineering.FluidSecureHub.WelcomeActivity;
import com.TrakEngineering.FluidSecureHub.entity.FsvmChipInfo;
import com.TrakEngineering.FluidSecureHub.entity.FsvmInfo;
import com.TrakEngineering.FluidSecureHub.entity.TankMonitorEntity;
import com.TrakEngineering.FluidSecureHub.offline.OffDBController;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by andrei on 7/30/15.
 */
public class MyServer extends NanoHTTPD {
    private final static int PORT = 8085;//8085  8550
    public static WelcomeActivity ctx;
    private static String TAG = "MyServer";
    ServerHandler serverHandler = new ServerHandler();
    String UpdateESP32_update = "NO";
    String UpdatePIC_update = "NO";
    String TLD_update = "N";
    boolean isScreenOn = true;
    ConnectionDetector cd;
    OffDBController controller;

    public MyServer() throws IOException {
        super(PORT);
        start();
        Log.i(TAG, " \nRunning! Point your browers to http://localhost:8085/ \n");
        AppConstants.Server_mesage = "Server Running..!!!";
        //if (AppConstants.GenerateLogs)
        //    AppConstants.WriteinFile(TAG + " http server up and running");

    }

    @Override
    public Response serve(IHTTPSession session) {

        cd = new ConnectionDetector(ctx);
        controller = new OffDBController(ctx);

        String RequestFor = null;
        String TldMacAddress = "", IsTLDFirmwareUpgrade = "N", ScheduleTankReading = "4";
        String FSTag = "", FirmwareVersion = "", fsvmData = "", RequestBody = "", ContentLength = "", host = "", ODOK = "", VIN = "";
        String ResMsg = "";
        // Accessfile from Internal storage
        //File f = new File(Environment.getExternalStorageDirectory() + "/FA_FileDownload/Download.txt");
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/FA_FileDownload/Download.txt");
        String mimeType = "text/plain";

        Log.i(TAG, "http server called");
        if (AppConstants.GenerateLogs) AppConstants.WriteinFile(TAG + " http server called");

        RequestFor = session.getHeaders().get("requestfor");

        try {

            if (RequestFor != null && RequestFor.equalsIgnoreCase("TLDUpgrade")) {

                String ProbeAddrAsString = "", Level = "0", TLDFirmwareVersionSave = "";

                try {

                    ProbeAddrAsString = session.getHeaders().get("mac").replaceAll(":", "");//
                    Level = session.getHeaders().get("level");
                    TLDFirmwareVersionSave = session.getHeaders().get("firmware version");

                    if (TLDFirmwareVersionSave == null)
                        TLDFirmwareVersionSave = "";


                } catch (Exception e) {

                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile("Exception in RequestFor:" + e);
                }

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile("TLD_REQ_To_http_server: ProbeAddr: " + ProbeAddrAsString + " Level:" + Level + " TLDFirmwareVersion:" + TLDFirmwareVersionSave);


                TldMacAddress = GetProbeOffByOne(ProbeAddrAsString);//add 1 hex in tlc prob mac


                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "Mac_AddressInHeader:" + ProbeAddrAsString + "\nMac_AddressAfterOff:" + TldMacAddress);

                try {

                    controller.insertTLDReadings(convertStringToMacAddress(TldMacAddress), Level, "0", TLDFirmwareVersionSave, AppConstants.getIMEI(ctx), "", "", "", CommonUtils.getTodaysDateInString(), "", "y");


                    if (AppConstants.DetailsServerSSIDList != null && AppConstants.DetailsServerSSIDList.size() > 0) {

                        for (int i = 0; i < AppConstants.DetailsServerSSIDList.size(); i++) {

                            String PROBEMacAddressWithColun = AppConstants.DetailsServerSSIDList.get(i).get("PROBEMacAddress");
                            String PROBEMacAddress = AppConstants.DetailsServerSSIDList.get(i).get("PROBEMacAddress").replaceAll(":", "");


                            if (AppConstants.GenerateLogs)
                                AppConstants.WriteinFile(TAG + "SSID list ProbeMacAddress:" + PROBEMacAddress + "  TldMacAddress:" + TldMacAddress);

                            if (TldMacAddress.equalsIgnoreCase(PROBEMacAddress)) {


                                String selSiteId = AppConstants.DetailsServerSSIDList.get(i).get("SiteId");

                                String TLDFirmwareFilePath = AppConstants.DetailsServerSSIDList.get(i).get("TLDFirmwareFilePath");

                                IsTLDFirmwareUpgrade = AppConstants.DetailsServerSSIDList.get(i).get("IsTLDFirmwareUpgrade");
                                try {
                                    ScheduleTankReading = AppConstants.DetailsServerSSIDList.get(i).get("ScheduleTankReading");
                                } catch (Exception e) {
                                    if (AppConstants.GenerateLogs)
                                        AppConstants.WriteinFile(TAG + "Exception ScheduleTankReading");
                                }

                                if (cd.isConnecting()) {
                                    TldSaveFun(PROBEMacAddressWithColun, Level, selSiteId, TLDFirmwareVersionSave);//Save TLD data to server
                                }


                                if (IsTLDFirmwareUpgrade.equalsIgnoreCase("Y") && TLDFirmwareFilePath != null && !TLDFirmwareFilePath.isEmpty()) {

                                    String[] parts = TLDFirmwareFilePath.split("/");
                                    String FileName = parts[5].toLowerCase(); // FSVM.bin
                                    new BackgroundServiceDownloadFirmware.DownloadTLDFileFromURL().execute(TLDFirmwareFilePath, FileName);
                                }

                                break;
                            }
                        }

                    } else {


                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " TLDUpgrade SSID List Empty not able to upgrade");
                    }

                } catch (Exception e) {
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "Exception TLDUpgrade AppConstants.DetailsServerSSIDList");
                }


                if (ScheduleTankReading == null || ScheduleTankReading.isEmpty()) {
                    ScheduleTankReading = "4";
                }

                //------------------------------------------------------------------------------------------

                ResMsg = "{\"TLD_update\":\"" + IsTLDFirmwareUpgrade + "\", \"Schedule\":\"" + ScheduleTankReading + "\" , \"current_time\":\"" + AppConstants.currentDateFormat("HH:mm:ss") + "\"}";
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile("TLD_RES_Frm_http_server" + ResMsg);

                System.out.println("TLD-" + ResMsg);


            } else {

                WelcomeActivity.WakeUpScreen(); //WakeUp Screen

                Integer contentLength = Integer.parseInt(session.getHeaders().get("content-length"));
                byte[] buffer = new byte[contentLength];
                session.getInputStream().read(buffer, 0, contentLength);
                Log.i(TAG, " RequestBody: " + new String(buffer));
                fsvmData = new String(buffer);

                Log.i(TAG, "http server fsvmData: " + fsvmData);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + " http server fsvmData: " + fsvmData);

                Map<String, String> param = session.getParms();
                //String B = param.get("data");
                String A = param.get("vehicle");

                String Uri = session.getUri();
                String QueryParameter = session.getQueryParameterString();
                String Post = Uri + QueryParameter;


                FirmwareVersion = session.getHeaders().get("firmware version");
                ContentLength = session.getHeaders().get("content-length");
                host = session.getHeaders().get("host");
                FSTag = session.getHeaders().get("fstag");
                String remote_addr = session.getHeaders().get("remote-addr");
                String httpclientip = session.getHeaders().get("http-client-ip");
                String fsvm_station = session.getHeaders().get("fsvm_station");
                String fstag_ble = session.getHeaders().get("fstag_ble");


                AppConstants.Header_data = "POST: " + Post + "\nHost: " + host + "\nFirmware version: " + FirmwareVersion + "\nContentLength: " + ContentLength + "\nremote_addr:" + remote_addr + "\nhttpclientip" + httpclientip + "\nfsvm_station:" + fsvm_station + "\nfstag_ble:" + fstag_ble;

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  HttpServer Header_data " + AppConstants.Header_data);

                AppConstants.Server_Request = "FsvmData:" + fsvmData + "\nData in param:  " + A;

                if (FSTag != null)
                    FSTag = FSTag.replaceAll(":", "").toLowerCase().trim();


                if (fsvmData != null && fsvmData.contains("VIN")) {

                    try {

                        JSONObject jo = new JSONObject(fsvmData);
                        String Protocol = jo.getString("Protocol");

                        JSONObject formatJo = jo.getJSONObject("format");
                        String pic_version = formatJo.getString("pic_version");
                        String Battery_voltage = formatJo.getString("Battery_voltage");
                        VIN = formatJo.getString("VIN");
                        ODOK = formatJo.getString("Odometer");


                    } catch (Exception e) {
                        System.out.println("FSVM--" + e.getMessage());
                    }


                }

                AppConstants.Server_Request = "FsvmData:" + fsvmData + "\nData in param:  " + A;


                AppConstants.Header_data = "POST: " + Post + "\nHost: " + host + "\nFirmware version: " + FirmwareVersion + "\nContentLength: " + ContentLength + "\nremote_addr:" + remote_addr + "\nhttpclientip" + httpclientip + "\nfsvm_station:" + fsvm_station + "\nfstag_ble:" + fstag_ble;


                try {

                    Log.i(TAG, " FsvmData:" + fsvmData);
                    Log.i(TAG, " Header_data:" + AppConstants.Header_data);
                    FsvmInfo objEntityClass = new FsvmInfo();
                    objEntityClass.IMEIUDID = AppConstants.getIMEI(ctx);
                    objEntityClass.Email = CommonUtils.getCustomerDetailsCC(ctx).PersonEmail;
                    objEntityClass.transactionDate = CommonUtils.getTodaysDateInString();
                    objEntityClass.TransactionFrom = "AP";
                    objEntityClass.CurrentLat = String.valueOf(Constants.Latitude);
                    objEntityClass.CurrentLng = String.valueOf(Constants.Longitude);
                    objEntityClass.VehicleRecurringMSG = fsvmData; //xyz;//
                    objEntityClass.FSTagMacAddress = fstag_ble;//"3C:A5:39:9A:B6:24";//
                    objEntityClass.CurrentFSVMFirmwareVersion = FirmwareVersion;//"3C:A5:39:9A:B6:24";//
                    objEntityClass.FSVMMacAddress = fsvm_station;

                    Gson gson = new Gson();
                    String jsonData = gson.toJson(objEntityClass);

                    String userEmail = CommonUtils.getCustomerDetailsCC(ctx).PersonEmail;
                    String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(ctx) + ":" + userEmail + ":" + "VINAuthorization" + AppConstants.LANG_PARAM);

                    Log.i(TAG, " NanoHTTPD serve response jsonData:" + jsonData);
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "  NanoHTTPD serve response jsonData:" + jsonData);


                    try {

                        String response = new SaveFsvmDataToServer().execute(jsonData, authString).get();
                        Log.i(TAG, "SaveFsvmDataToServer_Response" + response);
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " SaveFsvmDataToServer_Response" + response);

                        if (!response.equals(null) || !response.equals("")) {

                            JSONObject jsonObject = new JSONObject(response);
                            String ResponceMessage = jsonObject.getString("ResponceMessage");
                            String ResponceText = jsonObject.getString("ResponceText");
                            String IsFSVMUpgradable = jsonObject.getString("IsFSVMUpgradable");
                            String VehicleId = jsonObject.getString("VehicleId");
                            String VehicleNumber = jsonObject.getString("VehicleNumber");
                            String FSVMFirmwareVersion = jsonObject.getString("FSVMFirmwareVersion");
                            String FilePath = jsonObject.getString("FilePath"); //http://103.8.126.241:89/FSVMFirmwares/ESP32/0.051/FSVM.bin";
                            String PIC = jsonObject.getString("PIC");
                            String ESP32 = jsonObject.getString("ESP32");

                            if (WelcomeActivity.countFSVMUpgrade <= 2) {

                                if (IsFSVMUpgradable.equalsIgnoreCase("Y") && FilePath != null) {

                                    WelcomeActivity.countFSVMUpgrade = WelcomeActivity.countFSVMUpgrade + 1;

                                    if (WelcomeActivity.countFSVMUpgrade >= 2) {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                WelcomeActivity.countFSVMUpgrade=0;
                                            }
                                        },900000);//15 min
                                    }


                                    String[] parts = FilePath.split("/");
                                    String FileName = parts[6]; // FSVM.bin
                                    new BackgroundServiceDownloadFirmware.DownloadFileFromURL().execute(FilePath, FileName);

                                    if (ESP32.equalsIgnoreCase("Y")) {
                                        AppConstants.ESP32_update = "yes";
                                        AppConstants.PIC_update = "no";

                                        UpdateESP32_update = "YES";
                                        UpdatePIC_update = "NO";
                                    } else if (PIC.equalsIgnoreCase("Y")) {
                                        AppConstants.ESP32_update = "no";
                                        AppConstants.PIC_update = "yes";


                                        UpdatePIC_update = "YES";
                                        UpdateESP32_update = "NO";
                                    }

                                    ResMsg = jsonForFSVMClient(FirmwareVersion, FileName);


                                } else {

                                    ResMsg = "";

                                    UpdateESP32_update = "NO";
                                    UpdatePIC_update = "NO";


                                }
                            }

                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "  FsvmDataAsyncCall --Exception " + e);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if (AppConstants.GenerateLogs)
                        AppConstants.WriteinFile(TAG + "  Response serve 2 --Exception " + e);
                }


            }

        } catch (IOException e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)
                AppConstants.WriteinFile(TAG + "  Response serve 1 --Exception " + e);
        }

        return newFixedLengthResponse(ResMsg, f);

    }

    public String jsonForFSVMClient(String firmwareVersion, String FileName) {
        FsvmChipInfo objclss_esp = new FsvmChipInfo();
        objclss_esp.chip = "ESP32";
        objclss_esp.version = firmwareVersion;
        objclss_esp.server_ip = "192.168.43.1";
        objclss_esp.server_port = "8550";
        objclss_esp.file_name = "/FSVM/" + FileName.trim();

        FsvmChipInfo objclss_pic = new FsvmChipInfo();
        objclss_pic.chip = "pic";
        objclss_pic.version = firmwareVersion;
        objclss_pic.server_ip = "192.168.43.1";
        objclss_pic.server_port = "8550";
        objclss_pic.file_name = "/FSVM/" + FileName.trim();

        FsvmInfo objEntityClass = new FsvmInfo();
        objEntityClass.device = "FSVM";
        if (AppConstants.ESP32_update.equalsIgnoreCase("yes")) {
            objEntityClass.upgrade.add(objclss_esp);
        }

        if (AppConstants.PIC_update.equalsIgnoreCase("yes")) {
            objEntityClass.upgrade.add(objclss_pic);
        }

        String jsonData_fsvm;


        if (AppConstants.ESP32_update.equalsIgnoreCase("no") && AppConstants.PIC_update.equalsIgnoreCase("no")) {

            jsonData_fsvm = "{" + "    \"device\": \"FSVM\"" + "}";

        } else {
            Gson gson = new Gson();
            gson.serializeNulls();
            jsonData_fsvm = gson.toJson(objEntityClass);

        }


        AppConstants.Server_Response = "jsonData_fsvm:" + jsonData_fsvm;

        return jsonData_fsvm;
    }

    public class SaveFsvmDataToServer extends AsyncTask<String, Void, String> {

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


                response = serverHandler.PostTextData(ctx, AppConstants.webURL, jsonData, authString);

                System.out.println("Id..." + jsonData);

            } catch (Exception e) {
                Log.i(TAG, " SaveFsvmDataToServer doInBackground " + e);
                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "  SaveFsvmDataToServer doInBackground " + e);
            }

            return response;
        }

        @SuppressLint("LongLogTag")
        @Override
        protected void onPostExecute(String resp) {

            System.out.println("resp..." + resp);

        }
    }

    private String GetProbeOffByOne(String ProbeStr) {

        String FinalStr = "";
        try {

            if (ProbeStr.length() == 12) {

                String Split1 = ProbeStr.substring(0, 8);
                String Split2 = ProbeStr.substring(8, 12);
                String hexNumber = Split2;

                int decimal = Integer.parseInt(hexNumber, 16);
                System.out.println("Hex value is " + decimal);
                String hexx = CommonUtils.decimal2hex(decimal + 1);

                FinalStr = Split1 + hexx;
                System.out.println("Final string" + FinalStr);

            } else {
                Log.i(TAG, "Probe mac address wrong");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return FinalStr;
    }

    private void TldSaveFun(String PROBEMacAddress, String level, String selSiteId, String TLDFirmwareVersionSave) {


        String CurrentDeviceDate = CommonUtils.getTodaysDateInString();
        TankMonitorEntity obj_entity = new TankMonitorEntity();
        obj_entity.IMEI_UDID = AppConstants.getIMEI(ctx);
        obj_entity.FromSiteId = Integer.parseInt(selSiteId);
        obj_entity.TLD = PROBEMacAddress;
        obj_entity.LSB = "";
        obj_entity.MSB = "";
        obj_entity.TLDTemperature = "";
        obj_entity.ReadingDateTime = CurrentDeviceDate;//PrintDate;
        obj_entity.Response_code = "";//Response_code;
        obj_entity.Level = level;
        obj_entity.FromDirectTLD = "y";
        obj_entity.CurrentTLDVersion = TLDFirmwareVersionSave;

        Gson gson = new Gson();
        String jsonData = gson.toJson(obj_entity);

        String userEmail = CommonUtils.getCustomerDetailsCC(ctx).PersonEmail;
        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(ctx) + ":" + userEmail + ":" + "SaveTankMonitorReading" + AppConstants.LANG_PARAM);

        new BackgroundServiceDownloadFirmware.SaveTLDDataToServer().execute(jsonData, authString);

    }

    public String convertStringToMacAddress(String strMac) {
        String fmac = "";

        if (strMac.trim().length() == 12) {
            char macc[] = strMac.trim().toCharArray();

            for (int i = 0; i < macc.length; i += 2) {

                if (i % 2 == 0)
                    fmac += macc[i] + "" + macc[i + 1] + "";


                fmac += ":";
            }

            fmac = fmac.substring(0, fmac.length() - 1);

            System.out.println("fmac-" + fmac);
        }
        return fmac;
    }

}
