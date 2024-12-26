package com.TrakEngineering.FluidSecureHub;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.TrakEngineering.FluidSecureHub.offline.OfflineConstants;

public class ConnectionDetector {

	private Context _context;
	private static final String TAG = ConnectionDetector.class.getSimpleName();
	public ConnectionDetector(Context context){
		this._context = context;
	}

	//----------------------------------------------------------------------------------------------------
    public boolean isConnectingToInternet() {

        if (OfflineConstants.isTotalOfflineEnabled(_context)) {
            return false;
        } else if (isConnecting() && !OfflineConstants.isOfflineAccess(_context)) {
            return true;
        } else if (isConnecting() && IsTypeStable() && Constants.IS_SIGNAL_STRENGTH_OK) { //&& !IsFlightModeOn()
            return true;
        }
        //Constants.CURRENT_NETWORK_TYPE = "Offline";
        return false;
    }

    public boolean IsTypeStable() {

        Constants.CURRENT_NETWORK_TYPE = "";
        //POOR Bandwidth under 150 kbps.
        //MODERATE Bandwidth between 150 and 550 kbps.
        //GOOD Bandwidth over 2000 kbps.
        //EXCELLENT Bandwidth over 2000 kbps.
        //UNKNOWN connection quality cannot be found.

        ConnectivityManager Connectivity = (ConnectivityManager) _context.getSystemService(_context.CONNECTIVITY_SERVICE);
        TelephonyManager mTelephonyManager = (TelephonyManager) _context.getSystemService(_context.TELEPHONY_SERVICE);
        int subType = mTelephonyManager.getNetworkType();
        NetworkInfo info = Connectivity.getActiveNetworkInfo();

        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            AppConstants.writeInFile(TAG + " <NETWORK_TYPE: WIFI>");
            Constants.CURRENT_NETWORK_TYPE = "_wifi on";
            return false;
        } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {

            // check NetworkInfo subtype
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    AppConstants.writeInFile(TAG + " <NETWORK_TYPE: " + subType + ">");
                    Constants.CURRENT_NETWORK_TYPE = "50-100 kbps";
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    AppConstants.writeInFile(TAG + " <NETWORK_TYPE: " + subType + ">");
                    Constants.CURRENT_NETWORK_TYPE = "14-64 kbps";
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    AppConstants.writeInFile(TAG + " <NETWORK_TYPE: " + subType + ">");
                    Constants.CURRENT_NETWORK_TYPE = "100-200 kbps";
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    Constants.CURRENT_NETWORK_TYPE = "400-1000 kbps";
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    Constants.CURRENT_NETWORK_TYPE = "600-1400 kbps";
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    AppConstants.writeInFile(TAG + " <NETWORK_TYPE: " + subType + ">");
                    Constants.CURRENT_NETWORK_TYPE = "100 kbps";
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    Constants.CURRENT_NETWORK_TYPE = "2-14 Mbps";
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    Constants.CURRENT_NETWORK_TYPE = "700-1700 kbps";
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    Constants.CURRENT_NETWORK_TYPE = "1-23 Mbps";
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    Constants.CURRENT_NETWORK_TYPE = "400-7000 kbps";
                    return true; // ~ 400-7000 kbps
                /*
                 * Above API level 7, make sure to set android:targetSdkVersion
                 * to appropriate level to use these
                 */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    Constants.CURRENT_NETWORK_TYPE = "1-2 Mbps";
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    Constants.CURRENT_NETWORK_TYPE = "5 Mbps";
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    Constants.CURRENT_NETWORK_TYPE = "10-20 Mbps";
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    AppConstants.writeInFile(TAG + " <NETWORK_TYPE: " + subType + ">");
                    Constants.CURRENT_NETWORK_TYPE = "25 kbps";
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    Constants.CURRENT_NETWORK_TYPE = "10+ Mbps";
                    return true; // ~ 10+ Mbps
                case TelephonyManager.NETWORK_TYPE_NR: // 5G
                    Constants.CURRENT_NETWORK_TYPE = "100+ Mbps";
                    return true; // ~ 100+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    AppConstants.writeInFile(TAG + " <NETWORK_TYPE: " + subType + ">");
                    Constants.CURRENT_NETWORK_TYPE = "_unknown";
                    return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Checking for all possible internet providers
     * **/
    public boolean isConnecting() {
        boolean isConnected = false;

        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {

            NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();
            isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return isConnected;
    }

    public boolean IsFlightModeOn(){

        if (Settings.System.getInt(_context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0){
            return true;
        }else{
            return false;
        }
    }

	/*
	public boolean isConnectedToServer() throws ExecutionException, InterruptedException {

		ConnectivityCheckTask connectivityCheckTask=new ConnectivityCheckTask(AppConstants.WEB_URL);
		connectivityCheckTask.execute();
		connectivityCheckTask.get();
		return connectivityCheckTask.isConnected;

	}
	*/
}

