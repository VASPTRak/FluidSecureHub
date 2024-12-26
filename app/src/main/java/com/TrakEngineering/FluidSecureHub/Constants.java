package com.TrakEngineering.FluidSecureHub;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by VASP-LAP on 03-05-2016.
 */
public class Constants {

    public static final String PREF_TRANSACTION_DETAILS = "SaveTransactionInSharedPref";

    public static boolean HOTSPOT_STAY_ON = true;

    public static final String MAC_ADDRESS_RECONFIGURE = "saveLinkMacAddressForReconfigure";

    final public static String VEHICLE_NUMBER = "vehicleNumber";
    final public static String ODO_METER = "Odometer";
    final public static String DEPT = "dept";
    final public static String PERSON_PIN = "pin";
    final public static String OTHER = "other";
    final public static String HOURS = "hours";

    public static double LATITUDE = 0;
    public static double LONGITUDE = 0;
    public static int PORT = 8080; //80;

    final public static String DATE_FORMAT = "MMM dd, yyyy"; // May 24, 2016
    final public static String TIME_FORMAT = "hh:mm aa";
    public static final int CONNECTION_CODE = 111;

    public static String CURRENT_FS_PASSWORD;
    public final static int REQUEST_ENABLE_BT = 1;

    public static String MANUAL_ODO_SCREEN_FREE = "Yes";
    public static String FA_OdometerRequired = "Yes";
    public static boolean ON_FA_MANUAL_SCREEN;

    public static String QR_READER_STATUS = "QR Waiting..";
    public static String HF_READER_STATUS = "HF Waiting..";
    public static String LF_READER_STATUS = "LF Waiting..";
    public static String MAG_READER_STATUS = "Mag Waiting..";

    public static String FS_1_ODO_SCREEN = "FREE";
    public static String FS_2_ODO_SCREEN = "FREE";
    public static String FS_3_ODO_SCREEN = "FREE";
    public static String FS_4_ODO_SCREEN = "FREE";
    public static String FS_5_ODO_SCREEN = "FREE";
    public static String FS_6_ODO_SCREEN = "FREE";

    public static String FA_Message = "";

    public static String FS_1_STATUS = "FREE";
    public static String FS_2_STATUS = "FREE";
    public static String FS_3_STATUS = "FREE";
    public static String FS_4_STATUS = "FREE";
    public static String FS_5_STATUS = "FREE";
    public static String FS_6_STATUS = "FREE";

    public static String FS_1_GALLONS = "";
    public static String FS_2_GALLONS = "";
    public static String FS_3_GALLONS = "";
    public static String FS_4_GALLONS = "";
    public static String FS_5_GALLONS = "";
    public static String FS_6_GALLONS = "";

    public static String FS_1_PULSE = "00";
    public static String FS_2_PULSE = "00";
    public static String FS_3_PULSE = "00";
    public static String FS_4_PULSE = "00";
    public static String FS_5_PULSE = "00";
    public static String FS_6_PULSE = "00";

    public static final String SHARED_PREF_NAME = "UserInfo";
    public static final String PREF_COLUMN_USER = "UserData";
    public static final String PREF_COLUMN_SITE = "SiteData";
    public static final String PREF_OFF_DB_SIZE = "OfflineDbSize";
    public static final String PREF_COLUMN_GATE_HUB = "GateHub";
    public static final String PREF_TLD_Level = "TLDLevel";
    public static final String PREF_LOG_DATA = "LogData";
    public static final String PREF_FA_DATA = "FAData";
    public static final String PREF_VEHI_FUEL = "SaveVehiFuelInPref";
    public static final String PREF_TLD_DETAILS = "SaveTldDetailsInPref";
    public static final String PREF_FS_UPGRADE = "SaveFSUpgrade";
    public static final String PREF_LINK_pumpofftime = "SaveLinkPumpOffTime";
    public static final String PREF_TXTN_INTERRUPTED = "storeTxtnInterrupted";
    public static final String PREF_CALIBRATION_DETAILS = "CalibrationDetails";
    public static final String PREF_MO_STATUS_DETAILS = "MOStatusDetails";

    public static final int VERSION_CODES_NINE = 28;

    public static boolean IS_SIGNAL_STRENGTH_OK = true;
    public static String CURRENT_SELECTED_HOSE = "";
    public static String CURRENT_NETWORK_TYPE = "";
    public static String CURRENT_SIGNAL_STRENGTH = "";
    public static String FA_MANUAL_VEHICLE = "";

    public static String GATE_HUB_PIN_NUM = "";
    public static String GATE_HUB_VEHICLE_NUM = "";

    public static String PERSONNEL_PIN_FS1;
    public static String VEHICLE_NUMBER_FS1;
    public static String DEPARTMENT_NUMBER_FS1;
    public static String OTHER_FS1;
    public static String VEHICLE_OTHER_FS1;
    public static int ODO_METER_FS1 = 0;
    public static int HOURS_FS1;

    public static String PERSONNEL_PIN_FS2;
    public static String VEHICLE_NUMBER_FS2;
    public static String DEPARTMENT_NUMBER_FS2;
    public static String OTHER_FS2;
    public static String VEHICLE_OTHER_FS2;
    public static int ODO_METER_FS2;
    public static int HOURS_FS2;

    //For fs number 3
    public static String PERSONNEL_PIN_FS3;
    public static String VEHICLE_NUMBER_FS3;
    public static String DEPARTMENT_NUMBER_FS3;
    public static String OTHER_FS3;
    public static String VEHICLE_OTHER_FS3;
    public static int ODO_METER_FS3 = 0;
    public static int HOURS_FS3;

    //ForFs number 4
    public static String PERSONNEL_PIN_FS4;
    public static String VEHICLE_NUMBER_FS4;
    public static String DEPARTMENT_NUMBER_FS4;
    public static String OTHER_FS4;
    public static String VEHICLE_OTHER_FS4;
    public static int ODO_METER_FS4 = 0;
    public static int HOURS_FS4;

    //ForFs number 5
    public static String PERSONNEL_PIN_FS5;
    public static String VEHICLE_NUMBER_FS5;
    public static String DEPARTMENT_NUMBER_FS5;
    public static String OTHER_FS5;
    public static String VEHICLE_OTHER_FS5;
    public static int ODO_METER_FS5 = 0;
    public static int HOURS_FS5;

    //ForFs number 6
    public static String PERSONNEL_PIN_FS6;
    public static String VEHICLE_NUMBER_FS6;
    public static String DEPARTMENT_NUMBER_FS6;
    public static String OTHER_FS6;
    public static String VEHICLE_OTHER_FS6;
    public static int ODO_METER_FS6 = 0;
    public static int HOURS_FS6;

    static List<String> BUSY_VEHICLE_NUMBER_LIST = new ArrayList<String>();

    //public static String EXTERNAL_DIRECTORY= Environment.getExternalStorageDirectory()+ File.separator;
    public static String EXTERNAL_DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator;
    public static String LOG_FOLDER_NAME = "FuelSecureAP";
    public static String LOG_PATH = EXTERNAL_DIRECTORY + LOG_FOLDER_NAME + File.separator + "Logs";

    private static final int SERVER_PORT = 2901;
    private static final String SERVER_IP = "192.168.4.1";
}
