package com.TrakEngineering.FluidSecureHub;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;

public class AlertDialogUtil {

    public static AlertDialog createAlertDialog(Context context, String message, boolean showSpannableString) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("");
        if (showSpannableString) {
            SpannableString ss2 = new SpannableString(message);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            alertDialogBuilder.setMessage(ss2);
        } else {
            alertDialogBuilder.setMessage(message);
        }
        alertDialogBuilder.setCancelable(false);
        AlertDialog alertDialog = alertDialogBuilder.create();
        return alertDialog;
    }

    public static void runAnimatedLoadingDots(Activity activity, String messagePrefix, final AlertDialog alertDialog, boolean showSpannableString) {

        int dotsCount = 0;
        int spaceCount = 5;
        while (alertDialog != null && alertDialog.isShowing()) {
            dotsCount++;
            dotsCount = dotsCount % 5; // looks good w/4 dots

            if (dotsCount > 0) {
                try {
                    Thread.sleep(800);
                } catch (Exception ex) {}

                final StringBuffer updateValue = new StringBuffer(messagePrefix);
                for (int i = 0; i < dotsCount; i++) {
                    updateValue.append('.');
                }
                String PleaseWaitMsgAfterHotspotToggle = activity.getResources().getString(R.string.PleaseWaitAfterHotspotToggle);
                if (messagePrefix.equalsIgnoreCase(PleaseWaitMsgAfterHotspotToggle)) {
                    for (int i = 0; i < (spaceCount - dotsCount); i++) {
                        updateValue.append(' ');
                    }
                    updateValue.append(AppConstants.HS_CONNECTION_TIMEOUT);
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (showSpannableString) {
                            SpannableString ss2 = new SpannableString(updateValue.toString());
                            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
                            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
                            alertDialog.setMessage(ss2);
                        } else {
                            alertDialog.setMessage(updateValue.toString());
                        }
                    }
                });
            }
        }
    }

}
