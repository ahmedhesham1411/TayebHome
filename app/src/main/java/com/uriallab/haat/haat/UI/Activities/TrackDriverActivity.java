package com.uriallab.haat.haat.UI.Activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.databinding.ActivityTrackDriverBinding;
import com.uriallab.haat.haat.viewModels.TrackDriverViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import tech.gusavila92.websocketclient.WebSocketClient;

public class TrackDriverActivity extends AppCompatActivity implements OnMapReadyCallback {

    public LatLng storeLatLng, clientLatLng, driverLatLng;

    public GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private ActivityTrackDriverBinding binding;
    private TrackDriverViewModel viewModel;

    private WebSocketClient webSocketClient;

    public Marker marker;

    private List<LatLng> directionPoint = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_track_driver);

        Bundle bundle = getIntent().getBundleExtra("data");

        createWebSocketClient(bundle.getString("driverID"));

        storeLatLng = new LatLng(bundle.getDouble("storeLat"), bundle.getDouble("storeLng"));
        clientLatLng = new LatLng(bundle.getDouble("clientLat"), bundle.getDouble("clientLng"));
        driverLatLng = new LatLng(bundle.getDouble("driverLat"), bundle.getDouble("driverLng"));

        setUpMapIfNeeded();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        setMapLocation();

        viewModel = new TrackDriverViewModel(this);

        binding.setTrackDriverVM(viewModel);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 99);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 99);
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
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webSocketClient.close();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
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

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(clientLatLng).zoom(16).build();
        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

        viewModel.drawDirection();
    }

    private void setUpMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        String myMapStyle = "[{\"featureType\":\"all\",\"elementType\":\"labels.text.fill\",\"stylers\":[{\"color\":\"#7c93a3\"},{\"lightness\":\"-10\"}]},{\"featureType\":\"administrative.country\",\"elementType\":\"geometry\",\"stylers\":[{\"visibility\":\"on\"}]},{\"featureType\":\"administrative.country\",\"elementType\":\"geometry.stroke\",\"stylers\":[{\"color\":\"#c2d1d6\"}]},{\"featureType\":\"landscape\",\"elementType\":\"geometry.fill\",\"stylers\":[{\"color\":\"#dde3e3\"}]},{\"featureType\":\"road.highway\",\"elementType\":\"geometry.fill\",\"stylers\":[{\"color\":\"#c2d1d6\"}]},{\"featureType\":\"road.highway\",\"elementType\":\"geometry.stroke\",\"stylers\":[{\"color\":\"#a9b4b8\"},{\"lightness\":\"0\"}]},{\"featureType\":\"water\",\"elementType\":\"geometry.fill\",\"stylers\":[{\"color\":\"#a3c7df\"}]}]";
        MapStyleOptions style = new MapStyleOptions(myMapStyle);
        mMap.setMapStyle(style);
        mMap.getUiSettings().setZoomControlsEnabled(false);
    }

    private void createWebSocketClient(final String driverID) {
        try {
            Log.e("WebSocket", "driverID\t" + driverID);
            URI uri;
            try {
                // Connect to local host
                uri = new URI("ws://85.194.65.236:701/client");// TODO: 7/26/2020
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return;
            }
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen() {
                    Log.e("WebSocket", "Session is starting");
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("RoomId", driverID);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    webSocketClient.send(jsonObject.toString());
                }

                @Override
                public void onTextReceived(String s) {
                    Log.e("WebSocket", "Message received");
                    final String message = s;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // Utilities.toastySuccess(TrackDriverActivity.this, message);
                                Log.e("WebSocket", message + " ");

                                JSONObject jsonObject = new JSONObject(message);

                                double lat = Double.parseDouble(jsonObject.getString("Lat"));
                                double lng = Double.parseDouble(jsonObject.getString("Long"));

                                binding.searchingLin.setVisibility(View.GONE);

                                LatLng newLatLng = new LatLng(lat, lng);

                                directionPoint.add(newLatLng);

                                viewModel.drawDirection2(driverLatLng, newLatLng);

                                driverLatLng = newLatLng;

//                                try {
//                                    Log.e("SERVICE_RUN", "ON_CREATE");
//                                    int delay = 1000 * 15; // delay for 5 sec.
//                                    int period = 1000 * 15; // repeat every sec.
//
//                                    Timer timerDriver = new Timer();
//                                    timerDriver.scheduleAtFixedRate(new TimerTask() {
//                                        public void run() {
//                                            Log.e("SERVICE_RUN", "RUNNING");
//                                            Handler mainHandler = new Handler(Looper.getMainLooper());
//                                            Runnable myRunnable = new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    //Code that uses AsyncHttpClient in your case ConsultaCaract()
//                                                    setAnimation(directionPoint);
//
//                                                    directionPoint.clear();
//                                                    directionPoint.add(newLatLng);
//                                                }
//                                            };
//                                            mainHandler.post(myRunnable);
//                                        }
//                                    }, delay, period);
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                @Override
                public void onBinaryReceived(byte[] data) {
                    Log.e("WebSocket", "onBinaryReceived\t" + data.toString());
                }

                @Override
                public void onPingReceived(byte[] data) {
                    Log.e("WebSocket", "onPingReceived\t" + data.toString());
                }

                @Override
                public void onPongReceived(byte[] data) {
                    Log.e("WebSocket", "onPongReceived\t" + data.toString());
                }

                @Override
                public void onException(Exception e) {
                    Log.e("WebSocket", "onException\t" + e.getMessage());
                    createWebSocketClient(driverID);
                    System.out.println(e.getMessage());
                }

                @Override
                public void onCloseReceived() {
                    Log.e("WebSocket", "Closed ");
                    System.out.println("onCloseReceived");
                }
            };
            webSocketClient.setConnectTimeout(10000);
            webSocketClient.setReadTimeout(60000);
            webSocketClient.enableAutomaticReconnection(5000);
            webSocketClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setAnimation(final List<LatLng> directionPoint) {

        if (marker != null)
            marker.remove();

        marker = mMap.addMarker(new MarkerOptions()
                .position(directionPoint.get(0))
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView())));

        // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(directionPoint.get(0), 16));

        animateMarker(mMap, marker, directionPoint, false);
    }

    private void animateMarker(GoogleMap myMap, final Marker marker, final List<LatLng> directionPoint, final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = myMap.getProjection();
        final long duration = 15000;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            int i = 0;

            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                if (i < directionPoint.size())
                    marker.setPosition(directionPoint.get(i));
                try {
                    marker.setRotation(bearingBetweenLocations2(new LatLng(directionPoint.get(i).latitude,
                                    directionPoint.get(i).longitude),
                            new LatLng(directionPoint.get(i + 1).latitude,
                                    directionPoint.get(i + 1).longitude)));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                i++;

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 150);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    private float bearingBetweenLocations2(LatLng latLng1, LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return (float) brng;
    }

    public Bitmap getMarkerBitmapFromView() {

        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_view_car_marker, null);

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