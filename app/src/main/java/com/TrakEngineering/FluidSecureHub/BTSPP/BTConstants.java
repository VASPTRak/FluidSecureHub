package com.TrakEngineering.FluidSecureHub.BTSPP;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BTConstants {

    public static int SELECTED_LINK_FOR_PAIRED_DEVICES = 0;
    public static int CURRENT_SELECTED_BT_LINK_POSITION = 0;
    public static boolean CURRENT_TRANSACTION_IS_BT_1 = false;
    public static boolean CURRENT_TRANSACTION_IS_BT_2 = false;
    public static boolean CURRENT_TRANSACTION_IS_BT_3 = false;
    public static boolean CURRENT_TRANSACTION_IS_BT_4 = false;
    public static boolean CURRENT_TRANSACTION_IS_BT_5 = false;
    public static boolean CURRENT_TRANSACTION_IS_BT_6 = false;
    public static boolean BT_LINK_ONE_STATUS = false;
    public static boolean BT_LINK_TWO_STATUS = false;
    public static boolean BT_LINK_THREE_STATUS = false;
    public static boolean BT_LINK_FOUR_STATUS = false;
    public static boolean BT_LINK_FIVE_STATUS = false;
    public static boolean BT_LINK_SIX_STATUS = false;
    public static String BT_STATUS_STR_ONE = "";
    public static String BT_STATUS_STR_TWO = "";
    public static String BT_STATUS_STR_THREE = "";
    public static String BT_STATUS_STR_FOUR = "";
    public static String BT_STATUS_STR_FIVE = "";
    public static String BT_STATUS_STR_SIX = "";
    public static String CURRENT_COMMAND_LINK_ONE = "";
    public static String CURRENT_COMMAND_LINK_TWO = "";
    public static String CURRENT_COMMAND_LINK_THREE = "";
    public static String CURRENT_COMMAND_LINK_FOUR = "";
    public static String CURRENT_COMMAND_LINK_FIVE = "";
    public static String CURRENT_COMMAND_LINK_SIX = "";
    public static String DEVICE_ADDRESS_1 = "";
    public static String DEVICE_ADDRESS_2 = "";
    public static String DEVICE_ADDRESS_3 = "";
    public static String DEVICE_ADDRESS_4 = "";
    public static String DEVICE_ADDRESS_5 = "";
    public static String DEVICE_ADDRESS_6 = "";
    public static String DEVICE_ADDRESS_FOR_SCOPE = "";
    public static String SELECTED_SITE_ID_FOR_SCOPE = "0";
    public static boolean FOR_OSCILLOSCOPE = false;

    //public static boolean SwitchedBTToUDP1 = false;
    //public static boolean SwitchedBTToUDP2 = false;
    //public static boolean SwitchedBTToUDP3 = false;
    //public static boolean SwitchedBTToUDP4 = false;
    //public static boolean SwitchedBTToUDP5 = false;
    //public static boolean SwitchedBTToUDP6 = false;

    public static boolean IS_BT_SPP_TO_BLE_1 = false; // BT SPP Txn Continued With BLE
    public static boolean IS_BT_SPP_TO_BLE_2 = false;
    public static boolean IS_BT_SPP_TO_BLE_3 = false;
    public static boolean IS_BT_SPP_TO_BLE_4 = false;
    public static boolean IS_BT_SPP_TO_BLE_5 = false;
    public static boolean IS_BT_SPP_TO_BLE_6 = false;

    //public static boolean isHotspotDisabled = false;

    public static String INFO_COMMAND = "LK_COMM=info";
    public static String TRANSACTION_ID_COMMAND = "LK_COMM=txtnid:";
    public static String RELAY_ON_COMMAND = "LK_COMM=relay:12345=ON";
    public static String RELAY_OFF_COMMAND = "LK_COMM=relay:12345=OFF";
    //public static String LINK_STATION_COMMAND = "LK_COMM=HUB:";
    public static String FD_CHECK_COMMAND = "LK_COMM=FD_check";
    public static String RENAME_COMMAND = "LK_COMM=name:";
    public static String UPGRADE_COMMAND = "LK_COMM=upgrade ";
    public static String P_TYPE_COMMAND = "LK_COMM=p_type:";
    public static String GET_P_TYPE_COMMAND = "LK_COMM=p_type?";
    public static String p_type = "";
    public static String SCOPE_ON_COMMAND = "LK_COMM=scope=ON";
    public static String SCOPE_READ_COMMAND = "LK_COMM=scope=READ";
    public static String LAST1_COMMAND = "LK_COMM=last1";
    public static String LAST20_COMMAND = "LK_COMM=last20";
    public static String REBOOT_COMMAND = "LK_COMM=restartnow";
    public static String CHECK_MO_STATUS_COMMAND = "LK_COMM=if_check_mo_status=";
    public static String RESET_MO_CHECK_FLAG_COMMAND = "LK_COMM=mo_check_flag_reset";
    public static String SCOPE_STATUS = "";
    public static String[] P_TYPES = {"1", "2", "3", "4", "5"};
    public static String GET_P_SETTINGS_COMMAND = "LK_COMM=p_settings:pX:???";
    public static String SET_P_SETTINGS_COMMAND = "LK_COMM=p_settings:pX:Y";

    public static String SUPPORTED_LINK_VERSION_FOR_P_TYPE = "1.4.5";
    public static String SUPPORTED_LINK_VERSION_FOR_LAST1 = "1.4.11";
    public static String SUPPORTED_LINK_VERSION_FOR_LAST20 = "1.4.11";
    public static String SUPPORTED_LINK_VERSION_FOR_MO_STATUS = "1.4.12";

    public static boolean RETRY_CONN_FOR_INFO_COMMAND1 = false;
    public static boolean RETRY_CONN_FOR_INFO_COMMAND2 = false;
    public static boolean RETRY_CONN_FOR_INFO_COMMAND3 = false;
    public static boolean RETRY_CONN_FOR_INFO_COMMAND4 = false;
    public static boolean RETRY_CONN_FOR_INFO_COMMAND5 = false;
    public static boolean RETRY_CONN_FOR_INFO_COMMAND6 = false;

    //public static boolean isPTypeCommandExecuted1 = false;
    //public static boolean isPTypeCommandExecuted2 = false;
    //public static boolean isPTypeCommandExecuted3 = false;
    //public static boolean isPTypeCommandExecuted4 = false;
    //public static boolean isPTypeCommandExecuted5 = false;
    //public static boolean isPTypeCommandExecuted6 = false;

    public static boolean IS_NEW_VERSION_LINK_ONE = false;
    public static boolean IS_NEW_VERSION_LINK_TWO = false;
    public static boolean IS_NEW_VERSION_LINK_THREE = false;
    public static boolean IS_NEW_VERSION_LINK_FOUR = false;
    public static boolean IS_NEW_VERSION_LINK_FIVE = false;
    public static boolean IS_NEW_VERSION_LINK_SIX = false;

    public static boolean IS_RELAY_ON_AFTER_RECONNECT_1 = false;
    public static boolean IS_RELAY_ON_AFTER_RECONNECT_2 = false;
    public static boolean IS_RELAY_ON_AFTER_RECONNECT_3 = false;
    public static boolean IS_RELAY_ON_AFTER_RECONNECT_4 = false;
    public static boolean IS_RELAY_ON_AFTER_RECONNECT_5 = false;
    public static boolean IS_RELAY_ON_AFTER_RECONNECT_6 = false;

    public static boolean IS_RECONNECT_CALLED_1 = false;
    public static boolean IS_RECONNECT_CALLED_2 = false;
    public static boolean IS_RECONNECT_CALLED_3 = false;
    public static boolean IS_RECONNECT_CALLED_4 = false;
    public static boolean IS_RECONNECT_CALLED_5 = false;
    public static boolean IS_RECONNECT_CALLED_6 = false;

    public static int RETRY_BT_CONNECTION_LINK_POSITION = -1;

    public static int BT_CONN_FAILED_COUNT_LINK1 = 0;
    public static int BT_CONN_FAILED_COUNT_LINK2 = 0;
    public static int BT_CONN_FAILED_COUNT_LINK3 = 0;
    public static int BT_CONN_FAILED_COUNT_LINK4 = 0;
    public static int BT_CONN_FAILED_COUNT_LINK5 = 0;
    public static int BT_CONN_FAILED_COUNT_LINK6 = 0;

    //Rename BT link one
    public static boolean BT1_NEED_RENAME;
    public static String BT1_REPLACEABLE_WIFI_NAME;
    public static String BT1HOSE_ID;
    public static String BT1SITE_ID;

    //Rename BT LINK Two
    public static boolean BT2_NEED_RENAME;
    public static String BT2_REPLACEABLE_WIFI_NAME;
    public static String BT2HOSE_ID;
    public static String BT2SITE_ID;

    //Rename BT LINK Three
    public static boolean BT3_NEED_RENAME;
    public static String BT3_REPLACEABLE_WIFI_NAME;
    public static String BT3HOSE_ID;
    public static String BT3SITE_ID;

    //Rename BT LINK Four
    public static boolean BT4_NEED_RENAME;
    public static String BT4_REPLACEABLE_WIFI_NAME;
    public static String BT4HOSE_ID;
    public static String BT4SITE_ID;

    //Rename BT LINK Five
    public static boolean BT5_NEED_RENAME;
    public static String BT5_REPLACEABLE_WIFI_NAME;
    public static String BT5HOSE_ID;
    public static String BT5SITE_ID;

    //Rename BT LINK Six
    public static boolean BT6_NEED_RENAME;
    public static String BT6_REPLACEABLE_WIFI_NAME;
    public static String BT6HOSE_ID;
    public static String BT6SITE_ID;

    public static String BT_UPGRADE_STATUS = "";

    //date formatters for Old and New version link
    public static SimpleDateFormat DATE_FORMAT_FOR_OLD_VERSION = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat DATE_FORMAT_FOR_NEW_VERSION = new SimpleDateFormat("yyMMddHHmmss");

    public static String parseDateForNewVersion(String dateString) {
        try {
            return DATE_FORMAT_FOR_NEW_VERSION.format(DATE_FORMAT_FOR_OLD_VERSION.parse(dateString));
        } catch (Exception e) {
            return dateString;
        }
    }

    public static String parseDateForOldVersion(String dateString) {
        try {
            return DATE_FORMAT_FOR_OLD_VERSION.format(DATE_FORMAT_FOR_NEW_VERSION.parse(dateString));
        } catch (Exception e) {
            return dateString;
        }
    }
}
