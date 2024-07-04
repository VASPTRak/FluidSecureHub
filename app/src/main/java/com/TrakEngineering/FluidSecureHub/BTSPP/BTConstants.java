package com.TrakEngineering.FluidSecureHub.BTSPP;

import com.TrakEngineering.FluidSecureHub.BuildConfig;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BTConstants {

    public static int SelectedLinkForPaireddevices = 0;
    public static int CurrentSelectedLinkBT = 0;
    public static boolean CurrentTransactionIsBT1 = false;
    public static boolean CurrentTransactionIsBT2 = false;
    public static boolean CurrentTransactionIsBT3 = false;
    public static boolean CurrentTransactionIsBT4 = false;
    public static boolean CurrentTransactionIsBT5 = false;
    public static boolean CurrentTransactionIsBT6 = false;
    public static boolean BTLinkOneStatus = false;
    public static boolean BTLinkTwoStatus = false;
    public static boolean BTLinkThreeStatus = false;
    public static boolean BTLinkFourStatus = false;
    public static boolean BTLinkFiveStatus = false;
    public static boolean BTLinkSixStatus = false;
    public static String BTStatusStrOne = "";
    public static String BTStatusStrTwo = "";
    public static String BTStatusStrThree = "";
    public static String BTStatusStrFour = "";
    public static String BTStatusStrFive = "";
    public static String BTStatusStrSix = "";
    public static String CurrentCommand_LinkOne = "";
    public static String CurrentCommand_LinkTwo = "";
    public static String CurrentCommand_LinkThree = "";
    public static String CurrentCommand_LinkFour = "";
    public static String CurrentCommand_LinkFive = "";
    public static String CurrentCommand_LinkSix = "";
    public static String deviceAddress1 = "";//80:7D:3A:A4:67:22
    public static String deviceAddress2 = "";//80:7D:3A:A4:67:22
    public static String deviceAddress3 = "";
    public static String deviceAddress4 = "";
    public static String deviceAddress5 = "";
    public static String deviceAddress6 = "";
    public static String deviceAddressOscilloscope = "";
    public static String selectedSiteIdForScope = "0";
    public static boolean forOscilloscope = false;

    //public static boolean SwitchedBTToUDP1 = false;
    //public static boolean SwitchedBTToUDP2 = false;
    //public static boolean SwitchedBTToUDP3 = false;
    //public static boolean SwitchedBTToUDP4 = false;
    //public static boolean SwitchedBTToUDP5 = false;
    //public static boolean SwitchedBTToUDP6 = false;

    public static boolean isBTSPPTxnContinuedWithBLE1 = false;
    public static boolean isBTSPPTxnContinuedWithBLE2 = false;
    public static boolean isBTSPPTxnContinuedWithBLE3 = false;
    public static boolean isBTSPPTxnContinuedWithBLE4 = false;
    public static boolean isBTSPPTxnContinuedWithBLE5 = false;
    public static boolean isBTSPPTxnContinuedWithBLE6 = false;

    //public static final String ACTION_SHOW_WIFI_DIALOG = BuildConfig.APPLICATION_ID + ".action.SHOW_WIFI_DIALOG";
    //public static final String ACTION_BT_RECONNECT = BuildConfig.APPLICATION_ID + ".action.BT_RECONNECT";
    //public static boolean isHotspotDisabled = false;

    public static String info_cmd = "LK_COMM=info";
    public static String transaction_id_cmd = "LK_COMM=txtnid:";
    public static String relay_on_cmd = "LK_COMM=relay:12345=ON";
    public static String relay_off_cmd = "LK_COMM=relay:12345=OFF";
    public static String linkstation_cmd = "LK_COMM=HUB:";
    public static String fdcheckcommand = "LK_COMM=FD_check"; //BlueLink commands
    public static String namecommand = "LK_COMM=name:";
    //public static String linkUpgrade_cmd = "upgrade:";
    public static String linkUpgrade_cmd = "LK_COMM=upgrade ";
    public static String p_type_command = "LK_COMM=p_type:";
    public static String get_p_type_command = "LK_COMM=p_type?";
    public static String p_type = "";
    public static String scope_ON_cmd = "LK_COMM=scope=ON";
    public static String scope_READ_cmd = "LK_COMM=scope=READ";
    public static String last1_cmd = "LK_COMM=last1";
    public static String last20_cmd = "LK_COMM=last20";
    public static String reboot_cmd = "LK_COMM=restartnow";
    public static String ScopeStatus = "";
    public static String[] p_types = {"1", "2", "3", "4"};

    public static boolean retryConnForInfoCommand1 = false;
    public static boolean retryConnForInfoCommand2 = false;
    public static boolean retryConnForInfoCommand3 = false;
    public static boolean retryConnForInfoCommand4 = false;
    public static boolean retryConnForInfoCommand5 = false;
    public static boolean retryConnForInfoCommand6 = false;

    //public static boolean isPTypeCommandExecuted1 = false;
    //public static boolean isPTypeCommandExecuted2 = false;
    //public static boolean isPTypeCommandExecuted3 = false;
    //public static boolean isPTypeCommandExecuted4 = false;
    //public static boolean isPTypeCommandExecuted5 = false;
    //public static boolean isPTypeCommandExecuted6 = false;

    public static boolean isNewVersionLinkOne = false;
    public static boolean isNewVersionLinkTwo = false;
    public static boolean isNewVersionLinkThree = false;
    public static boolean isNewVersionLinkFour = false;
    public static boolean isNewVersionLinkFive = false;
    public static boolean isNewVersionLinkSix = false;

    public static boolean isRelayOnAfterReconnect1 = false;
    public static boolean isRelayOnAfterReconnect2 = false;
    public static boolean isRelayOnAfterReconnect3 = false;
    public static boolean isRelayOnAfterReconnect4 = false;
    public static boolean isRelayOnAfterReconnect5 = false;
    public static boolean isRelayOnAfterReconnect6 = false;

    public static boolean isReconnectCalled1 = false;
    public static boolean isReconnectCalled2 = false;
    public static boolean isReconnectCalled3 = false;
    public static boolean isReconnectCalled4 = false;
    public static boolean isReconnectCalled5 = false;
    public static boolean isReconnectCalled6 = false;

    public static int RetryBTConnectionLinkPosition = -1;

    public static int BTConnFailedCountLink1 = 0;
    public static int BTConnFailedCountLink2 = 0;
    public static int BTConnFailedCountLink3 = 0;
    public static int BTConnFailedCountLink4 = 0;
    public static int BTConnFailedCountLink5 = 0;
    public static int BTConnFailedCountLink6 = 0;

    //public static boolean isReturnedFromManualWifiConnect = false;

    //Rename BT link one
    public static boolean BT1NeedRename;
    public static String BT1REPLACEBLE_WIFI_NAME;
    public static String BT1HOSE_ID;
    public static String BT1SITE_ID;

    //Rename BT LINK Two
    public static boolean BT2NeedRename;
    public static String BT2REPLACEBLE_WIFI_NAME;
    public static String BT2HOSE_ID;
    public static String BT2SITE_ID;

    //Rename BT LINK Three
    public static boolean BT3NeedRename;
    public static String BT3REPLACEBLE_WIFI_NAME;
    public static String BT3HOSE_ID;
    public static String BT3SITE_ID;

    //Rename BT LINK Four
    public static boolean BT4NeedRename;
    public static String BT4REPLACEBLE_WIFI_NAME;
    public static String BT4HOSE_ID;
    public static String BT4SITE_ID;

    //Rename BT LINK Five
    public static boolean BT5NeedRename;
    public static String BT5REPLACEBLE_WIFI_NAME;
    public static String BT5HOSE_ID;
    public static String BT5SITE_ID;

    //Rename BT LINK Six
    public static boolean BT6NeedRename;
    public static String BT6REPLACEBLE_WIFI_NAME;
    public static String BT6HOSE_ID;
    public static String BT6SITE_ID;

    public static String BTUpgradeStatus = "";

    //date formatters for Old and New version link
    public static SimpleDateFormat dateFormatForOldVersion = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat dateFormatForNewVersion = new SimpleDateFormat("yyMMddHHmmss");

    public static String parseDateForNewVersion(String dateString) {
        try {
            return dateFormatForNewVersion.format(dateFormatForOldVersion.parse(dateString));
        } catch (Exception e) {
            return dateString;
        }
    }

    public static String parseDateForOldVersion(String dateString) {
        try {
            return dateFormatForOldVersion.format(dateFormatForNewVersion.parse(dateString));
        } catch (Exception e) {
            return dateString;
        }
    }
}
