package com.TrakEngineering.FluidSecureHub;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by VASP-LAP on 03-05-2016.
 */
public class Constants {

    public static final String PREF_TransactionDetails = "SaveTransactionInSharedPreff";

    public static boolean hotspotstayOn = true;

    public static final String MAC_ADDR_RECONFIGURE = "saveLinkMacAddressForReconfigure";

    final public static String VEHICLE_NUMBER="vehicleNumber";
    final public static String ODO_METER="Odometer";
    final public static String DEPT="dept";
    final public static String PPIN="pin";
    final public static String OTHERR="other";
    final public static String HOURSS="hours";

    public static double Latitude=0;
    public static double Longitude=0;
    public static int PORT = 8080; //80;

    final public static String DATE_FORMAT="MMM dd, yyyy"; // May 24, 2016
    final public static String TIME_FORMAT="hh:mm aa";
    public static final int CONNECTION_CODE = 111;

    public static String CurrFsPass;
    public final static int REQUEST_ENABLE_BT = 1;

    public static String ManualOdoScreenFree = "Yes";
    public static String FA_OdometerRequired = "Yes";
    public static boolean ON_FA_MANUAL_SCREEN;

    public static String QR_ReaderStatus = "QR Waiting..";
    public static String HF_ReaderStatus = "HF Waiting..";
    public static String LF_ReaderStatus = "LF Waiting..";
    public static String Mag_ReaderStatus = "Mag Waiting..";

    public static String FS_1OdoScreen = "FREE";
    public static String FS_2OdoScreen = "FREE";
    public static String FS_3OdoScreen = "FREE";
    public static String FS_4OdoScreen = "FREE";
    public static String FS_5OdoScreen = "FREE";
    public static String FS_6OdoScreen = "FREE";

    public static String FA_Message = "";


    public static String FS_1STATUS = "FREE";
    public static String FS_2STATUS = "FREE";
    public static String FS_3STATUS = "FREE";
    public static String FS_4STATUS = "FREE";
    public static String FS_5STATUS = "FREE";
    public static String FS_6STATUS = "FREE";

    public static String FS_1Gallons = "";
    public static String FS_2Gallons = "";
    public static String FS_3Gallons = "";
    public static String FS_4Gallons = "";
    public static String FS_5Gallons = "";
    public static String FS_6Gallons = "";


    public static String FS_1Pulse = "";
    public static String FS_2Pulse = "";
    public static String FS_3Pulse = "";
    public static String FS_4Pulse = "";
    public static String FS_5Pulse = "";
    public static String FS_6Pulse = "";

    public static final String SHARED_PREF_NAME = "UserInfo";
    public static final String PREF_COLUMN_USER = "UserData";
    public static final String PREF_COLUMN_SITE = "SiteData";
    public static final String PREF_OFF_DB_SIZE = "OfflineDbSize";
    public static final String PREF_COLUMN_GATE_HUB = "GateHub";
    public static final String PREF_TLD_Level = "TLDLevel";
    public static final String PREF_Log_Data = "LogData";
    public static final String PREF_FA_Data = "FAData";
    public static final String PREF_VehiFuel = "SaveVehiFuelInPref";
    public static final String PREF_TldDetails = "SaveTldDetailsInPref";
    public static final String PREF_FS_UPGRADE = "SaveFSUpgrade";
    public static final String PREF_LINK_pumpofftime = "SaveLinkPumpOffTime";
    public static final String PREF_TXTN_INTERRUPTED = "storeTxtnInterrupted";

    public static final int VERSION_CODES_NINE = 28;

    public static boolean IsSignalSrtengthOk = true;
    public static String CurrentSelectedHose = "";
    public static String CurrentNetworkType = "";
    public static String CurrentSignalStrength = "";
    public static String FA_MANUAL_VEHICLE = "";

    public static String GateHubPinNo = "";
    public static String GateHubvehicleNo = "";

    public static String AccPersonnelPIN_FS1;
    public static String AccVehicleNumber_FS1;
    public static String AccDepartmentNumber_FS1;
    public static String AccOther_FS1;
    public static String AccVehicleOther_FS1;
    public static int AccOdoMeter_FS1=0;
    public static int AccHours_FS1;

    public static String AccVehicleNumber;
    public static String AccDepartmentNumber;
    public static String AccPersonnelPIN;
    public static String AccOther;
    public static String AccVehicleOther;
    public static int AccOdoMeter;
    public static int AccHours;

    //For fs number 3
    public static String AccPersonnelPIN_FS3;
    public static String AccVehicleNumber_FS3;
    public static String AccDepartmentNumber_FS3;
    public static String AccOther_FS3;
    public static String AccVehicleOther_FS3;
    public static int AccOdoMeter_FS3=0;
    public static int AccHours_FS3;

    //ForFs number 4
    public static String AccPersonnelPIN_FS4;
    public static String AccVehicleNumber_FS4;
    public static String AccDepartmentNumber_FS4;
    public static String AccOther_FS4;
    public static String AccVehicleOther_FS4;
    public static int AccOdoMeter_FS4=0;
    public static int AccHours_FS4;

    //ForFs number 5
    public static String AccPersonnelPIN_FS5;
    public static String AccVehicleNumber_FS5;
    public static String AccDepartmentNumber_FS5;
    public static String AccOther_FS5;
    public static String AccVehicleOther_FS5;
    public static int AccOdoMeter_FS5=0;
    public static int AccHours_FS5;

    //ForFs number 6
    public static String AccPersonnelPIN_FS6;
    public static String AccVehicleNumber_FS6;
    public static String AccDepartmentNumber_FS6;
    public static String AccOther_FS6;
    public static String AccVehicleOther_FS6;
    public static int AccOdoMeter_FS6=0;
    public static int AccHours_FS6;

    static List<String> BusyVehicleNumberList = new ArrayList<String>();


    //public static String exrSdDir= Environment.getExternalStorageDirectory()+ File.separator;
    public static String exrSdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator;
    public static String logFolderName = "FuelSecureAP";
    public static String LogPath = exrSdDir + logFolderName + File.separator + "Logs";

    private static final int SERVER_PORT = 2901;
    private static final String SERVER_IP = "192.168.4.1";
}
