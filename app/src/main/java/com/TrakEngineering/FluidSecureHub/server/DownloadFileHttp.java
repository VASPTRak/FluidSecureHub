package com.TrakEngineering.FluidSecureHub.server;

import android.os.Environment;
import android.util.Log;

import com.TrakEngineering.FluidSecureHub.AppConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Administrator on 18-07-2018.
 */

public class DownloadFileHttp extends NanoHTTPD {
    private final static int PORT = 8550;//8555
    private static String TAG = "DownloadFileHttp";

    public DownloadFileHttp() throws IOException {
        super(PORT);
        start();
        AppConstants.DownloadFileHttpServer = "Started";

    }

    public Response serve(IHTTPSession session) {
        String uri = session.getUri();

        try {
            session.parseBody(new HashMap<String, String>());
        } catch (ResponseException | IOException r) {
            r.printStackTrace();
            if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Response serve 1 --Exception " + r);
        }

        String filenamefromUrl = uri;

        File root = Environment.getExternalStorageDirectory();
        FileInputStream fis = null;

        //File file = new File(root.getAbsolutePath() + "/www/pie.jpg");
        //String _mimeType="image/jpeg";
        // File file = new File(root.getAbsolutePath() + filenamefromUrl);

        File file = new File(root.getAbsolutePath() + "/www" + filenamefromUrl);


        String _mimeType = "application/octet-stream";

        Log.d("Path", root.getAbsolutePath());
        try {
            if (file.exists()) {
                fis = new FileInputStream(file);

            } else{
                Log.d("FOF :", "File Not exists:Path>>/www" + filenamefromUrl);
                if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  FOF File Not exists:Path>>/www" + filenamefromUrl);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (AppConstants.GenerateLogs)AppConstants.WriteinFile(TAG + "  Response serve 2 --Exception " + e);
        }


        return newFixedLengthResponse(Response.Status.OK, _mimeType, fis, file.length());
    }


}