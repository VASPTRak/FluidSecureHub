package com.TrakEngineering.FluidSecureHub;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

public class BackgroundServiceClearOlderPictures extends BackgroundService {


    @SuppressLint("LongLogTag")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            super.onStart(intent, startId);
            Log.e(TAG, "~~~~~start into BackgroundServiceClearOlderPictures~~~~~");

            clearOlderPictures();
            Log.e(TAG, "~~~~~stop BackgroundServiceClearOlderPictures~~~~~");


        } catch (NullPointerException e) {
            if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  onStartCommand Execption " + e);
            Log.d("Ex", e.getMessage());
            this.stopSelf();
        }

        // return super.onStartCommand(intent, flags, startId);
        return Service.START_NOT_STICKY;

    }

    public void clearOlderPictures(){
        Calendar time = Calendar.getInstance();
        //File dir = new File(Environment.getExternalStorageDirectory() + "/FSPictureData");
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/FSPictureData");
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (!file.isDirectory()) {
                    time.add(Calendar.DAY_OF_YEAR,60);
                    Date lastModified = new Date(file.lastModified());
                    if(lastModified.before(time.getTime())) {
                        file.delete();
                        //Toast.makeText(getApplicationContext(), "Image deleted: " + file.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }


}
