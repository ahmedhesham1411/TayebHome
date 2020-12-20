package com.uriallab.haat.haat.UI.Fragments;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.uriallab.haat.haat.LocalNotification.TrackingDelegate;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.LoginSession;
import com.uriallab.haat.haat.Utilities.GlobalVariables;
import com.uriallab.haat.haat.databinding.FragmentJourneyBinding;
import com.uriallab.haat.haat.viewModels.JourniesViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

import tech.gusavila92.websocketclient.WebSocketClient;

/**
 * A simple {@link Fragment} subclass.
 */
public class JourneyFragment extends Fragment {

    public FragmentJourneyBinding binding;

    private JourniesViewModel viewModel;

    private WebSocketClient webSocketClient;

    public JourneyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_journey, container, false);

        viewModel = new JourniesViewModel(this, getChildFragmentManager());

        binding.setOrdersVM(viewModel);

        binding.switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    viewModel.activeDriver(true);

                    startService();

                    // createWebSocketClient();

                } else {
                    viewModel.activeDriver(false);
                }
            }
        });

        return binding.getRoot();
    }

    public void createWebSocketClient() {
        URI uri;
        try {
            // Connect to local host
            uri = new URI("ws://85.194.65.236:701/driver");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                Log.e("WebSocket", "Session is starting");

                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("RoomId", LoginSession.getUserData(getActivity()).getResult().getUserData().getUserUID());

                        jsonObject.put("Lat", GlobalVariables.LOCATION_LAT);
                        jsonObject.put("Long", GlobalVariables.LOCATION_LNG);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    webSocketClient.send(jsonObject.toString());
                }
            }

            @Override
            public void onTextReceived(String s) {
                Log.e("WebSocket", "Message received");
                final String message = s;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //Utilities.toastySuccess(ChatDriverActivity.this, message);
                            Log.e("WebSocket", message + " ");
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

    private void sendMessage() {
        Log.e("WebSocket", "Button was clicked");

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("RoomId", LoginSession.getUserData(getActivity()).getResult().getUserData().getUserUID());

                jsonObject.put("Lat", GlobalVariables.LOCATION_LAT);
                jsonObject.put("Long", GlobalVariables.LOCATION_LNG);
                Log.e("WebSocket", GlobalVariables.LOCATION_LAT + "\t" + GlobalVariables.LOCATION_LNG);

            } catch (JSONException e) {
                Log.e("WebSocket", "JSONException\t" + e.getMessage());
                e.printStackTrace();
            }
            webSocketClient.send(jsonObject.toString());
        }
    }

    public void startService() {
        // TODO: 6/10/2020
        Intent myService = new Intent(getContext(), TrackingDelegate.class);
        getActivity().startService(myService);
    }

}