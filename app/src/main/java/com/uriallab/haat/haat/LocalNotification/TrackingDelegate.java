package com.uriallab.haat.haat.LocalNotification;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.uriallab.haat.haat.SharedPreferences.LoginSession;
import com.uriallab.haat.haat.Utilities.GPSTracker;
import com.uriallab.haat.haat.Utilities.GlobalVariables;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

import tech.gusavila92.websocketclient.WebSocketClient;

/**
 * Created by Mahmoud on 14/05/2019.
 */

public class TrackingDelegate extends Service {

    private WebSocketClient webSocketClient;
    private WebSocketClient webSocketClientDriver;

    private Timer timer;
    private Timer timerDriver;
    private TimerTask task;
    private TimerTask taskDriver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {

    }

    @Override
    public void onCreate() {
        try {
            Log.e("SERVICE_RUN", "ON_CREATE");
            int delay = 20000; // delay for 5 sec.
            int period = 20000; // repeat every sec.

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
                GlobalVariables.LOCATION_LAT = gpsTracker.getLocation().getLatitude();
                GlobalVariables.LOCATION_LNG = gpsTracker.getLocation().getLongitude();
                createWebSocketClient(getApplicationContext(), gpsTracker.getLocation().getLatitude(), gpsTracker.getLocation().getLongitude());
            }

            timer = new Timer();
            timer.scheduleAtFixedRate(task = new TimerTask() {
                public void run() {
                    Log.e("SERVICE_RUN", "RUNNING");
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            //Code that uses AsyncHttpClient in your case ConsultaCaract()
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                                GPSTracker gpsTracker = new GPSTracker(getApplicationContext());

                                try {
                                    sendMessage(gpsTracker.getLocation().getLatitude(), gpsTracker.getLocation().getLongitude());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    mainHandler.post(myRunnable);
                }
            }, delay, period);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Log.e("SERVICE_RUN", "ON_CREATE");
            int delay = 1000 * 60; // delay for 5 sec.
            int period = 1000 * 60; // repeat every sec.

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
                createWebSocketClientForDriver(getApplicationContext(), gpsTracker.getLocation().getLatitude(), gpsTracker.getLocation().getLongitude());
            }

            timerDriver = new Timer();
            timerDriver.scheduleAtFixedRate(taskDriver = new TimerTask() {
                public void run() {
                    Log.e("SERVICE_RUN", "RUNNING");
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            //Code that uses AsyncHttpClient in your case ConsultaCaract()
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                                GPSTracker gpsTracker = new GPSTracker(getApplicationContext());

                                try {
                                    sendMessageForDriver(gpsTracker.getLocation().getLatitude(), gpsTracker.getLocation().getLongitude());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    mainHandler.post(myRunnable);
                }
            }, delay, period);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("SERVICE_RUN", "onStartCommand");
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            timer.cancel();
            timerDriver.cancel();
            task.cancel();
            taskDriver.cancel();
            webSocketClient.close();
            webSocketClientDriver.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e("SERVICE_RUN", "ON_DESTROY");
    }

    public void createWebSocketClient(final Context context, final double latitude, final double longitude) {
        URI uri;
        try {
            // Connect to local host
            uri = new URI("ws://85.194.65.236:701/driver");
        } catch (URISyntaxException e) {
            Log.e("WebSocket", "URISyntaxException");
            e.printStackTrace();
            return;
        }
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                Log.e("WebSocket", "Session is starting");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("RoomId", LoginSession.getUserData(context).getResult().getUserData().getUserUID());

                    jsonObject.put("Lat", latitude);
                    jsonObject.put("Long", longitude);
                    Log.e("WebSocketLocationSent", latitude + "\t" + longitude);

                } catch (JSONException e) {
                    Log.e("WebSocket", "JSONException\t" + e.getMessage());
                    e.printStackTrace();
                }
                webSocketClient.send(jsonObject.toString());
            }

            @Override
            public void onTextReceived(String s) {
                Log.e("WebSocket", "Message received");
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
            }

            @Override
            public void onCloseReceived() {
                Log.e("WebSocket", "onCloseReceived");
            }
        };
        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(90000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }

    private void sendMessage(double latitude, double longitude) {
        try {
            Log.e("WebSocket", "Button was clicked");

            // Send button id string to WebSocket Server
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("RoomId", LoginSession.getUserData(getApplicationContext()).getResult().getUserData().getUserUID());
                jsonObject.put("Lat", latitude);
                jsonObject.put("Long", longitude);
                Log.e("WebSocketLocationSent", latitude + "\t" + longitude);
            } catch (JSONException e) {
                Log.e("WebSocket", "JSONException\t" + e.getMessage());
                e.printStackTrace();
            }
            webSocketClient.send(jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void createWebSocketClientForDriver(final Context context, final double latitude, final double longitude) {
        URI uri;
        try {
            // Connect to local host
            uri = new URI("ws://85.194.65.236:701/updateDriverLocation");
        } catch (URISyntaxException e) {
            Log.e("WebSocket", "URISyntaxException");
            e.printStackTrace();
            return;
        }
        webSocketClientDriver = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                Log.e("WebSocket", "Session is starting");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("Drivet_id", LoginSession.getUserData(context).getResult().getUserData().getUserUID());

                    jsonObject.put("lat", latitude);
                    jsonObject.put("long", longitude);
                    Log.e("WebSocketLocationSent", latitude + "\t" + longitude);

                } catch (JSONException e) {
                    Log.e("WebSocket", "JSONException\t" + e.getMessage());
                    e.printStackTrace();
                }
                webSocketClientDriver.send(jsonObject.toString());
            }

            @Override
            public void onTextReceived(String s) {
                Log.e("WebSocket", "Message received");
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
            }

            @Override
            public void onCloseReceived() {
                Log.e("WebSocket", "onCloseReceived");
            }
        };
        webSocketClientDriver.setConnectTimeout(10000);
        webSocketClientDriver.setReadTimeout(90000);
        webSocketClientDriver.enableAutomaticReconnection(5000);
        webSocketClientDriver.connect();
    }

    private void sendMessageForDriver(double latitude, double longitude) {
        try {
            Log.e("WebSocket", "Button was clicked");

            // Send button id string to WebSocket Server
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("Drivet_id", LoginSession.getUserData(getApplicationContext()).getResult().getUserData().getUserUID());
                jsonObject.put("lat", latitude);
                jsonObject.put("long", longitude);
                Log.e("WebSocketLocationSent", latitude + "\t" + longitude);
            } catch (JSONException e) {
                Log.e("WebSocket", "JSONException\t" + e.getMessage());
                e.printStackTrace();
            }
            webSocketClientDriver.send(jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}