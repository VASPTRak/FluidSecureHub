package com.TrakEngineering.FluidSecureHub;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.TrakEngineering.FluidSecureHub.enity.UpdateMacAddressClass;
import com.TrakEngineering.FluidSecureHub.server.ServerHandler;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter ";

    private ArrayList<String> mImageNames = new ArrayList<>();
    private ArrayList<String> mImageMac = new ArrayList<>();
    private Context mContext;

    public RecyclerViewAdapter(Context context, ArrayList<String> imageNames, ArrayList<String> imagesMac) {
        mImageNames = imageNames;
        mImageMac = imagesMac;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        holder.imageName.setText(mImageNames.get(position));
        holder.image_mac.setText(mImageMac.get(position));
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "DEVICE NAME: " + mImageNames.get(position) + " \nDEVICE MAC: " + mImageMac.get(position));
                Toast.makeText(mContext, "DEVICE NAME: " + mImageNames.get(position) + " \nDEVICE MAC: " + mImageMac.get(position), Toast.LENGTH_SHORT).show();

                if (AppConstants.GenerateLogs)
                    AppConstants.WriteinFile(TAG + "BTLink: UpdateMac Address selected DEVICE NAME:" + mImageNames.get(position) + "DEVICE MAC:" + mImageMac.get(position));
                //String mac = CommonFunctions.ShiftMacAddress(mImageMac.get(position),"S",2);
                UpdateMacAddress(mImageMac.get(position));

                Intent i = new Intent(mContext, WelcomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mImageNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView imageName, image_mac;
        LinearLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            imageName = itemView.findViewById(R.id.image_name);
            image_mac = itemView.findViewById(R.id.image_mac);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }

    private void UpdateMacAddress(String updateMac) {

        UpdateMacAddressClass authEntityClass = new UpdateMacAddressClass();
        authEntityClass.SiteId = Integer.parseInt(AppConstants.CURRENT_SELECTED_SITEID);
        authEntityClass.MACAddress = updateMac;
        authEntityClass.RequestFrom = "AP";
        authEntityClass.HubName = AppConstants.HubName;

        Gson gson = new Gson();
        final String jsonData = gson.toJson(authEntityClass);

        saveLinkMacAddressForReconfigure(jsonData);
        //Update MacAddress.
        UpdateMacToServer();
    }

    public void saveLinkMacAddressForReconfigure(String jsonData) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(Constants.MAC_ADDR_RECONFIGURE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("jsonData", jsonData);
        editor.commit();

    }

    public void UpdateMacToServer() {

        SharedPreferences sharedPref = mContext.getSharedPreferences(Constants.MAC_ADDR_RECONFIGURE, Context.MODE_PRIVATE);
        String jsonData = sharedPref.getString("jsonData", "");
        if (!jsonData.isEmpty())
            new UpdateMacAsynTask().execute(jsonData);

    }

    class UpdateMacAsynTask extends AsyncTask<String, Void, String> {

        public String response = null;

        @Override
        protected String doInBackground(String... param) {

            try {
                ServerHandler serverHandler = new ServerHandler();
                String jsonData = param[0];
                String userEmail = CommonUtils.getCustomerDetailsCC(mContext).PersonEmail;

                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(mContext) + ":" + userEmail + ":" + "UpdateMACAddress");
                response = serverHandler.PostTextData(mContext, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String serverRes) {

            try {
                if (serverRes.equalsIgnoreCase("err")) {
                    // AppConstants.alertBigFinishActivity(this, "Link Re-configuration is partially completed. \nPlease remove app from Recent Apps and start app again");
                } else if (serverRes != null) {

                    JSONObject jsonObject1 = new JSONObject(serverRes);
                    String ResponceMessage = jsonObject1.getString("ResponceMessage");
                    String ResponceText = jsonObject1.getString("ResponceText");

                    if (ResponceMessage.equalsIgnoreCase("success")) {
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + "Mac Address Updated successfully");
                        AppConstants.clearSharedPrefByName(mContext, Constants.MAC_ADDR_RECONFIGURE);
                        AppConstants.colorToastBigFont(mContext, "Mac Address Updated successfully", Color.BLUE);
                    }else if (ResponceMessage.equalsIgnoreCase("fail")){
                        if (AppConstants.GenerateLogs)
                            AppConstants.WriteinFile(TAG + " "+ResponceText);
                        AppConstants.clearSharedPrefByName(mContext, Constants.MAC_ADDR_RECONFIGURE);
                        AppConstants.colorToastBigFont(mContext, " "+ResponceText, Color.RED);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


