package com.TrakEngineering.FluidSecureHub.wifihotspot;


/**
 * Created by VASP on 9/1/2017.
 */

import java.lang.reflect.Method;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.RequiresApi;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.AppConstants;
import com.TrakEngineering.FluidSecureHub.BuildConfig;
import com.TrakEngineering.FluidSecureHub.CommonUtils;

import static android.content.Context.WIFI_SERVICE;


public class WifiApManager {
    private final WifiManager mWifiManager;
    private Context context;
    private String TAG = "WifiApManager ";

    public static String TabsDefaultHubspotName;
    public static String TabsDefaultHubspotPassword;

    public static  WifiManager.LocalOnlyHotspotReservation mReservation;

    public WifiApManager(Context context) {
        this.context = context;
        mWifiManager = (WifiManager) this.context.getSystemService(WIFI_SERVICE);
    }


    public boolean setWifiApEnabled(WifiConfiguration wifiConfig, boolean enabled) {
        try {

            AppConstants.IS_PROBLEM_WHILE_ENABLE_HOTSPOT = false;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                if (enabled) {
                    //on
                    //turnOnHotspot();
                    Intent intent = new Intent(BuildConfig.APPLICATION_ID + ".wifihotspot.TURN_ON");
                    sendImplicitBroadcast(context,intent);

                } else {
                    //off
                    //turnOffHotspot();
                    Intent intent = new Intent(BuildConfig.APPLICATION_ID + ".wifihotspot.TURN_OFF");
                    sendImplicitBroadcast(context,intent);
                }

                return false;
            } else {
                if (enabled) { // disable WiFi in any case
                    mWifiManager.setWifiEnabled(false);
                }
                Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                return (Boolean) method.invoke(mWifiManager, wifiConfig, enabled);
            }

        } catch (Exception e) {
            AppConstants.writeInFile(" setWifiApEnabled Exception: " + e);
            AppConstants.IS_PROBLEM_WHILE_ENABLE_HOTSPOT = true;
            Log.e(this.getClass().toString(), "", e);
            return false;
        }
    }


    /*@RequiresApi(api = Build.VERSION_CODES.O)
    private void turnOnHotspot() {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        manager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);
                Log.d(TAG, "Wifi Hotspot is on now");
                mReservation = reservation;
                String SSID = reservation.getWifiConfiguration().SSID;
                String Key = reservation.getWifiConfiguration().preSharedKey;

                System.out.println("OOOO -- SSID:" + SSID);
                System.out.println("OOOO -- Key:" + Key);

                TabsDefaultHubspotName = SSID;
                TabsDefaultHubspotPassword = Key;
            }

            @Override
            public void onStopped() {
                super.onStopped();
                Log.d(TAG, "onStopped: ");
            }

            @Override
            public void onFailed(int reason) {
                super.onFailed(reason);
                Log.d(TAG, "onFailed: ");
            }
        }, new Handler());
    }*/

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void turnOffHotspot() {

        if (mReservation != null) {
            mReservation.close();
        }

    }

    private static void sendImplicitBroadcast(Context ctxt, Intent i) {
        PackageManager pm=ctxt.getPackageManager();
        List<ResolveInfo> matches=pm.queryBroadcastReceivers(i, 0);

        for (ResolveInfo resolveInfo : matches) {
            Intent explicit=new Intent(i);
            ComponentName cn=
                    new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName,
                            resolveInfo.activityInfo.name);

            explicit.setComponent(cn);
            ctxt.sendBroadcast(explicit);
        }
    }
}
