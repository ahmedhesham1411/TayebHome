package com.uriallab.haat.haat.UI.Adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;
import com.uriallab.haat.haat.DataModels.GoogleStoresModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.UI.Activities.makeOrder.StoreDetailsActivity;
import com.uriallab.haat.haat.Utilities.GPSTracker;
import com.uriallab.haat.haat.Utilities.GlobalVariables;
import com.uriallab.haat.haat.Utilities.IntentClass;
import com.uriallab.haat.haat.Utilities.Utilities;
import com.uriallab.haat.haat.databinding.ItemStoreBinding;

import java.util.List;
import java.util.Locale;

/**
 * Created by Mahmoud on 4/19/2020.
 */

public class StoresAdapter extends RecyclerView.Adapter<StoresAdapter.StoresViewHolder> {

    private Activity activity;
    private List<GoogleStoresModel.ResultsBean> incomingList;

    private String photoUrl;

    public StoresAdapter(Activity activity, List<GoogleStoresModel.ResultsBean> incomingList) {
        this.activity = activity;
        this.incomingList = incomingList;
    }

    @Override
    public StoresViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemStoreBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.item_store, parent, false);
        return new StoresViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final StoresViewHolder holder, final int position) {

        holder.binding.storeName.setText(incomingList.get(position).getName());

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            double distance = Utilities.getKilometers(GlobalVariables.LOCATION_LAT,
                    GlobalVariables.LOCATION_LNG,
                    incomingList.get(position).getGeometry().getLocation().getLat(),
                    incomingList.get(position).getGeometry().getLocation().getLng());

            holder.binding.storeDistance.setText(Utilities.roundPrice(distance) + "");
        }

        getAddressFromLatLng(activity, holder.binding.storeLocation, new LatLng(
                incomingList.get(position).getGeometry().getLocation().getLat(),
                incomingList.get(position).getGeometry().getLocation().getLng()));

        try {
            photoUrl = "https://maps.googleapis.com/maps/api/place/photo?photoreference=" +
                    incomingList.get(position).getPhotos().get(0).getPhoto_reference()
                    + "&maxheight=400&maxwidth=400&key=AIzaSyAmD_A7N-SI2JbkhGh4xY_OFip7GtQRZfg";
        } catch (Exception e) {
            photoUrl = incomingList.get(position).getIcon();
            e.printStackTrace();
        }

        Picasso.get().load(photoUrl).into(holder.binding.storeImg);

        holder.itemView.setOnClickListener(view -> {

            LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                showCustomDialog();
            }
            else {
                try {
                    GPSTracker gpsTracker = new GPSTracker(activity.getApplicationContext());
                    GlobalVariables.LOCATION_LAT = gpsTracker.getLocation().getLatitude();
                    GlobalVariables.LOCATION_LNG = gpsTracker.getLocation().getLongitude();
                    saveLat(activity.getApplicationContext(), String.valueOf(gpsTracker.getLocation().getLatitude()));
                    saveLng(activity.getApplicationContext(), String.valueOf(gpsTracker.getLocation().getLongitude()));
                    GlobalVariables.LOCATION_LNG = Double.parseDouble(GetLng(activity.getApplicationContext()));
                    GlobalVariables.LOCATION_LAT = Double.parseDouble(GetLat(activity.getApplicationContext()));
                }catch (Exception e){}

                Bundle bundle = new Bundle();
                bundle.putString("placeId", incomingList.get(position).getPlace_id());
                bundle.putBoolean("isFromServer", false);
                IntentClass.goToActivity(activity, StoreDetailsActivity.class, bundle);
            }



        });

    }

    public static void saveLng(Context context, String Token){
        SharedPreferences pref = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("Lng", Token);
        editor.commit();
    }

    public static void saveLat(Context context, String Token){
        SharedPreferences pref = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("Lat", Token);
        editor.commit();
    }

    public static String GetLng(Context context){
        SharedPreferences pref = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        return pref.getString("Lng","31.2731497");
    }
    public static String GetLat(Context context){
        SharedPreferences pref = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        return pref.getString("Lat","29.9434477");
    }

    private void showCustomDialog() {
        ViewGroup viewGroup = activity.findViewById(android.R.id.content);
        final View dialogView = LayoutInflater.from(activity).inflate(R.layout.custom_alert_dialog_gps, viewGroup, false);
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setCancelable(false);
        Button gps_btn= dialogView.findViewById(R.id.gps_btn);

        gps_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                alertDialog.dismiss();
            }
        });
    }

    private void getAddressFromLatLng(final Activity activity, final TextView textView, final LatLng latLng) {
        try {

            Locale aLocale;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (ConfigurationFile.getCurrentLanguage(activity).equals("ar"))
                    aLocale = new Locale.Builder().setLanguage("ar").build();
                else
                    aLocale = new Locale.Builder().setLanguage("en").build();
            } else {
                if (ConfigurationFile.getCurrentLanguage(activity).equals("ar"))
                    aLocale = new Locale("ar");
                else
                    aLocale = new Locale("en");
            }

            Geocoder geo = new Geocoder(activity, aLocale);
            List<Address> addresses = geo.getFromLocation(latLng.latitude, latLng.longitude, 2);
            if (addresses.isEmpty()) {
                textView.setText("Waiting for Location");
            } else {
                if (addresses.size() > 0) {
                    textView.setText(addresses.get(0).getAddressLine(0));
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }
    }

    @Override
    public int getItemCount() {
        return incomingList.size();
    }

    public class StoresViewHolder extends RecyclerView.ViewHolder {

        private ItemStoreBinding binding;

        private StoresViewHolder(ItemStoreBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}