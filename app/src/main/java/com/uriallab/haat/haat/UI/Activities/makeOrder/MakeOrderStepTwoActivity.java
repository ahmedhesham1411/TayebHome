package com.uriallab.haat.haat.UI.Activities.makeOrder;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.uriallab.haat.haat.DataModels.MakeOrderModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.Utilities.GlobalVariables;
import com.uriallab.haat.haat.Utilities.Utilities;
import com.uriallab.haat.haat.databinding.ActivityMakeOrderStepTwoBinding;
import com.uriallab.haat.haat.viewModels.MakeOrderStepTwoViewModel;

import java.util.List;
import java.util.Locale;

public class MakeOrderStepTwoActivity extends AppCompatActivity implements OnMapReadyCallback {

    public ActivityMakeOrderStepTwoBinding binding;

    private MakeOrderStepTwoViewModel viewModel;

    public LatLng latLngTo;
    public GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    public boolean isService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_make_order_step_two);

        MakeOrderModel makeOrderModel = GlobalVariables.makeOrderModel;

        isService = makeOrderModel.isService();

        binding.locationTxt.setSelected(true);
        binding.distenationTxt.setSelected(true);

        setUpMapIfNeeded();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        setMapLocation();

        viewModel = new MakeOrderStepTwoViewModel(this, makeOrderModel);

        binding.setMakeOrderVM(viewModel);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (GlobalVariables.FINISH_ACTIVITY)
            finish();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        99);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        99);
            }
        }
    }

    private void setMapLocation() {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void getFusedLocation() {
        latLngTo = Utilities.getCurrentLocation(this);
        viewModel.userLat = latLngTo.latitude;
        viewModel.userLng = latLngTo.longitude;
        Log.e("Location", latLngTo.latitude + ", " + latLngTo.longitude);
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("RETURN", "return");
            return;
        }

        setUpMap();

        latLngTo = Utilities.getCurrentLocation(this);
        viewModel.userLat = latLngTo.latitude;
        viewModel.userLng = latLngTo.longitude;
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLngTo).zoom(14).build();
        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

        mMap.addMarker(new MarkerOptions()
                .position(latLngTo)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(true))));

        getAddressFromLatLng(binding.locationTxt, latLngTo);
        Log.e("cameraMidLatLng", latLngTo.latitude + "\t" + latLngTo.longitude);
    }

    private void setUpMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String myMapStyle = "[{\"featureType\":\"all\",\"elementType\":\"labels.text.fill\",\"stylers\":[{\"color\":\"#7c93a3\"},{\"lightness\":\"-10\"}]},{\"featureType\":\"administrative.country\",\"elementType\":\"geometry\",\"stylers\":[{\"visibility\":\"on\"}]},{\"featureType\":\"administrative.country\",\"elementType\":\"geometry.stroke\",\"stylers\":[{\"color\":\"#c2d1d6\"}]},{\"featureType\":\"landscape\",\"elementType\":\"geometry.fill\",\"stylers\":[{\"color\":\"#dde3e3\"}]},{\"featureType\":\"road.highway\",\"elementType\":\"geometry.fill\",\"stylers\":[{\"color\":\"#c2d1d6\"}]},{\"featureType\":\"road.highway\",\"elementType\":\"geometry.stroke\",\"stylers\":[{\"color\":\"#a9b4b8\"},{\"lightness\":\"0\"}]},{\"featureType\":\"water\",\"elementType\":\"geometry.fill\",\"stylers\":[{\"color\":\"#a3c7df\"}]}]";
        MapStyleOptions style = new MapStyleOptions(myMapStyle);
        mMap.setMapStyle(style);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        getFusedLocation();
    }

    private void getAddressFromLatLng(TextView textView, LatLng latLng) {
        try {

            Locale aLocale;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (ConfigurationFile.getCurrentLanguage(this).equals("ar"))
                    aLocale = new Locale.Builder().setLanguage("ar").build();
                else
                    aLocale = new Locale.Builder().setLanguage("en").build();
            } else {
                if (ConfigurationFile.getCurrentLanguage(this).equals("ar"))
                    aLocale = new Locale("ar");
                else
                    aLocale = new Locale("en");
            }

            Geocoder geo = new Geocoder(this, aLocale);
            List<Address> addresses = geo.getFromLocation(latLng.latitude, latLng.longitude, 2);
            if (addresses.isEmpty()) {
                textView.setText("Waiting for Location");
            } else {
                if (addresses.size() > 0) {
                    viewModel.addressObservable.set(addresses.get(0).getAddressLine(0));
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 111) {
            if (!data.getExtras().getString("address").equals("none")) {
                viewModel.addressObservable.set(data.getExtras().getString("address"));
                if (!data.getExtras().getString("add_address").equals("none"))
                    viewModel.additionalAddressObservable.set(data.getExtras().getString("add_address"));
                viewModel.userLat = data.getExtras().getDouble("lat");
                viewModel.userLng = data.getExtras().getDouble("lng");

                //viewModel.drawDirection();
            }
        } else if (requestCode == 122) {
            if (!data.getExtras().getString("address").equals("none")) {
                viewModel.distenationObservable.set(data.getExtras().getString("address"));
                if (!data.getExtras().getString("add_address").equals("none"))
                    viewModel.additionalAddressObservable.set(data.getExtras().getString("add_address"));
                viewModel.shopLat = data.getExtras().getDouble("lat");
                viewModel.shopLng = data.getExtras().getDouble("lng");

                viewModel.drawDirection();

            }
        } else if (requestCode == 222) {
            if (!data.getExtras().getString("time").equals("none")) {
                viewModel.timeObservable.set(data.getExtras().getString("time"));
                viewModel.timeIdObservable.set(data.getExtras().getString("timeId"));
            }
        } else if (requestCode == 333) {
            if (!data.getExtras().getString("fav_address").equals("none")) {
                viewModel.addressObservable.set(data.getExtras().getString("fav_address"));

                viewModel.userLat = data.getExtras().getDouble("lat");
                viewModel.userLng = data.getExtras().getDouble("lng");

                //viewModel.drawDirection();
            }
        }
    }

    public Bitmap getMarkerBitmapFromView(boolean isHome) {

        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_view_home_marker, null);

        LinearLayout title_lin = customMarkerView.findViewById(R.id.title_lin);
        ImageView icon_img = customMarkerView.findViewById(R.id.icon_img2);

        title_lin.setVisibility(View.GONE);
        if (isHome)
            icon_img.setImageResource(R.drawable.haat_home_2);
        else
            icon_img.setImageResource(R.drawable.haat_store_2);

        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);

        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }
}