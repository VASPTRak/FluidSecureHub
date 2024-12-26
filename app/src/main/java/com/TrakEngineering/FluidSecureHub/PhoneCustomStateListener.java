package com.TrakEngineering.FluidSecureHub;


import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;

public class PhoneCustomStateListener extends PhoneStateListener {

    public int signalSupport = 0;

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);

        signalSupport = signalStrength.getGsmSignalStrength();
        //Log.d(getClass().getCanonicalName(), "------ gsm signal --> " + signalSupport);

        if (signalSupport > 30) {
            Constants.CURRENT_SIGNAL_STRENGTH = "Signal GSM : Good";
            Constants.IS_SIGNAL_STRENGTH_OK = true;

        } else if (signalSupport > 20 && signalSupport < 30) {
            Constants.CURRENT_SIGNAL_STRENGTH = "Signal GSM : Avarage";
            Constants.IS_SIGNAL_STRENGTH_OK = true;

        } else if (signalSupport < 20 && signalSupport > 3) {
            Constants.CURRENT_SIGNAL_STRENGTH = "Signal GSM : Weak";
            Constants.IS_SIGNAL_STRENGTH_OK = false;

        } else if (signalSupport < 3) {
            Constants.CURRENT_SIGNAL_STRENGTH = "Signal GSM : Very weak";
            Constants.IS_SIGNAL_STRENGTH_OK = false;

        }
    }
}
