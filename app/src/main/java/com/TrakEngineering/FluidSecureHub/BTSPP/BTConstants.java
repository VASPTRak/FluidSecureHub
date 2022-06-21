package com.TrakEngineering.FluidSecureHub.BTSPP;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BTConstants {

    public static int SelectedLinkForPaireddevices = 0;
    public static int CurrentSelectedLinkBT = 0;
    public static boolean CurrentTransactionIsBT = false;
    public static boolean BTLinkOneStatus = false;
    public static boolean BTLinkTwoStatus = false;
    public static boolean BTLinkThreeStatus = false;
    public static boolean BTLinkFourStatus = false;
    public static String BTStatusStrOne = "";
    public static String BTStatusStrTwo = "";
    public static String BTStatusStrThree = "";
    public static String BTStatusStrFour = "";
    public static String CurrentCommand_LinkOne = "";
    public static String CurrentCommand_LinkTwo = "";
    public static String CurrentCommand_LinkThree = "";
    public static String CurrentCommand_LinkFour = "";
    public static String deviceAddress1 = "";//80:7D:3A:A4:67:22
    public static String deviceAddress2 = "";//80:7D:3A:A4:67:22
    public static String deviceAddress3 = "";
    public static String deviceAddress4 = "";

    public static String info_cmd = "LK_COMM=info";
    public static String transaction_id_cmd = "LK_COMM=txtnid:";
    public static String relay_on_cmd = "LK_COMM=relay:12345=ON";
    public static String relay_off_cmd = "LK_COMM=relay:12345=OFF";
    public static String linkstation_cmd = "LK_COMM=HUB:";
    public static String fdcheckcommand = "LK_COMM=FD_check"; //BlueLink commands
    public static String namecommand = "LK_COMM=name:";

    public static boolean isNewVersionLinkOne = false;
    public static boolean isNewVersionLinkTwo = false;
    public static boolean isNewVersionLinkThree = false;
    public static boolean isNewVersionLinkFour = false;

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
