package com.uriallab.haat.haat.UI;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.DataModels.UserModel;
import com.uriallab.haat.haat.LocalNotification.TrackingDelegate;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.SharedPreferences.LoginSession;
import com.uriallab.haat.haat.UI.Activities.AllOffersActivity;
import com.uriallab.haat.haat.UI.Activities.ChatActivity;
import com.uriallab.haat.haat.UI.Activities.ChatDriverActivity;
import com.uriallab.haat.haat.UI.Activities.DriverNewOrderActivity;
import com.uriallab.haat.haat.UI.Fragments.HomeFragment;
import com.uriallab.haat.haat.UI.Fragments.JourneyFragment;
import com.uriallab.haat.haat.UI.Fragments.MoreFragment;
import com.uriallab.haat.haat.UI.Fragments.NotificationFragment;
import com.uriallab.haat.haat.UI.Fragments.OrdersFragment;
import com.uriallab.haat.haat.Utilities.Dialogs;
import com.uriallab.haat.haat.Utilities.GPSTracker;
import com.uriallab.haat.haat.Utilities.GlobalVariables;
import com.uriallab.haat.haat.Utilities.IntentClass;
import com.uriallab.haat.haat.Utilities.LoadingDialog;
import com.uriallab.haat.haat.Utilities.Utilities;
import com.uriallab.haat.haat.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;

import static com.uriallab.haat.haat.Utilities.GlobalVariables.JOURNEY_FRAGMENT_ID;
import static com.uriallab.haat.haat.Utilities.GlobalVariables.JOURNEY_FRAGMENT_TAG;
import static com.uriallab.haat.haat.Utilities.GlobalVariables.MAIN_FRAGMENT_ID;
import static com.uriallab.haat.haat.Utilities.GlobalVariables.MAIN_FRAGMENT_TAG;
import static com.uriallab.haat.haat.Utilities.GlobalVariables.MORE_FRAGMENT_ID;
import static com.uriallab.haat.haat.Utilities.GlobalVariables.MORE_FRAGMENT_TAG;
import static com.uriallab.haat.haat.Utilities.GlobalVariables.NOTIFICATIONS_FRAGMENT_ID;
import static com.uriallab.haat.haat.Utilities.GlobalVariables.NOTIFICATIONS_FRAGMENT_TAG;
import static com.uriallab.haat.haat.Utilities.GlobalVariables.ORDERS_FRAGMENT_ID;
import static com.uriallab.haat.haat.Utilities.GlobalVariables.ORDERS_FRAGMENT_TAG;

public class MainActivity extends AppCompatActivity  {

    private ActivityMainBinding binding;

    private FragmentManager fragmentManager;
    private String selectedFragmentTag;

    private Animation rotateAnimation;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ConfigurationFile.setCurrentLanguage(this, ConfigurationFile.getCurrentLanguage(this));

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, 123);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        GlobalVariables.LOCATION_LAT = location.getLatitude();
                        GlobalVariables.LOCATION_LNG = location.getLongitude();
                        Log.e("fusedLocation", GlobalVariables.LOCATION_LAT + " " + GlobalVariables.LOCATION_LNG);
                        Log.e("fusedLocation", location.getLatitude() + " " + location.getLongitude());
                    }
                });

        if (LoginSession.isLoggedIn(this)){

            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    profileData();

                    updateFcmToken();
                }
            }, 4000);



        }

        fragmentManager = getSupportFragmentManager();
        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate360);

        fragmentManager.addOnBackStackChangedListener(() -> {
            selectedFragmentTag = fragmentManager.getFragments().get(fragmentManager.getFragments().size() - 1).getTag();
            Log.e("backstack fragmentTag", selectedFragmentTag);
            editLayout(selectedFragmentTag);
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(playSoundReceiver,
                new IntentFilter("PlaySound"));

        try {
            Log.e("NotificationKey", getIntent().getExtras().getString("key")
                    + "\t" + getIntent().getExtras().getString("orderID"));

            if (Integer.parseInt(getIntent().getExtras().getString("key")) == -1) {
                editLayout(MAIN_FRAGMENT_TAG);
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pushFragment(MAIN_FRAGMENT_ID, null);
                    }
                }, 500);
            } else if (Integer.parseInt(getIntent().getExtras().getString("key")) == -2) {
                Bundle bundle2 = new Bundle();
                bundle2.putInt("type", 1);
                pushFragment(ORDERS_FRAGMENT_ID, bundle2);
                editLayout(ORDERS_FRAGMENT_TAG);
            } else if (Integer.parseInt(getIntent().getExtras().getString("key")) == 1) {
                pushFragment(JOURNEY_FRAGMENT_ID, null);
                editLayout(JOURNEY_FRAGMENT_TAG);
                Bundle bundle = new Bundle();
                bundle.putString("orderId", getIntent().getExtras().getString("orderID"));
                IntentClass.goToActivity(this, DriverNewOrderActivity.class, bundle);
            } else if (Integer.parseInt(getIntent().getExtras().getString("key")) == 2) {
                pushFragment(JOURNEY_FRAGMENT_ID, null);
                editLayout(JOURNEY_FRAGMENT_TAG);
                Bundle bundle1 = new Bundle();
                bundle1.putString("orderId", getIntent().getExtras().getString("orderID"));
                IntentClass.goToActivity(this, ChatDriverActivity.class, bundle1);
            } else if (Integer.parseInt(getIntent().getExtras().getString("key")) == 3 || Integer.parseInt(getIntent().getExtras().getString("key")) == 4) {
                pushFragment(JOURNEY_FRAGMENT_ID, null);
                editLayout(JOURNEY_FRAGMENT_TAG);
            } else if (Integer.parseInt(getIntent().getExtras().getString("key")) == 5) {
                Bundle bundle2 = new Bundle();
                bundle2.putInt("type", 1);
                pushFragment(ORDERS_FRAGMENT_ID, bundle2);
                editLayout(ORDERS_FRAGMENT_TAG);
                Bundle bundle1 = new Bundle();
                bundle1.putBoolean("isNew", false);
                bundle1.putString("orderId", getIntent().getExtras().getString("orderID"));
                IntentClass.goToActivity(this, AllOffersActivity.class, bundle1);
            } else if (Integer.parseInt(getIntent().getExtras().getString("key")) == 6 || Integer.parseInt(getIntent().getExtras().getString("key")) == 7) {
                Bundle bundle2 = new Bundle();
                bundle2.putInt("type", 1);
                pushFragment(ORDERS_FRAGMENT_ID, bundle2);
                editLayout(ORDERS_FRAGMENT_TAG);
                Bundle bundle1 = new Bundle();
                bundle1.putString("orderId", getIntent().getExtras().getString("orderID"));
                IntentClass.goToActivity(this, ChatActivity.class, bundle1);
            } else if (Integer.parseInt(getIntent().getExtras().getString("key")) == 8) {
                pushFragment(JOURNEY_FRAGMENT_ID, null);
                editLayout(JOURNEY_FRAGMENT_TAG);
                Bundle bundle1 = new Bundle();
                bundle1.putString("orderId", getIntent().getExtras().getString("orderID"));
                IntentClass.goToActivity(this, ChatDriverActivity.class, bundle1);
            } else if (Integer.parseInt(getIntent().getExtras().getString("key")) == 9) {
                Bundle bundle1 = new Bundle();
                bundle1.putInt("type", 2);
                pushFragment(ORDERS_FRAGMENT_ID, bundle1);
                editLayout(ORDERS_FRAGMENT_TAG);
            } else if (Integer.parseInt(getIntent().getExtras().getString("key")) == 10) {
                pushFragment(MAIN_FRAGMENT_ID, null);
                editLayout(MAIN_FRAGMENT_TAG);
                Bundle bundle1 = new Bundle();
                bundle1.putString("orderId", getIntent().getExtras().getString("orderID"));
                IntentClass.goToActivity(this, ChatActivity.class, bundle1);
            } else if (Integer.parseInt(getIntent().getExtras().getString("key")) == 11) {
                pushFragment(MAIN_FRAGMENT_ID, null);
                editLayout(MAIN_FRAGMENT_TAG);
                Bundle bundle1 = new Bundle();
                bundle1.putString("orderId", getIntent().getExtras().getString("orderID"));
                IntentClass.goToActivity(this, ChatDriverActivity.class, bundle1);
            } else if (Integer.parseInt(getIntent().getExtras().getString("key")) == 12) {
                Bundle bundle1 = new Bundle();
                bundle1.putInt("type", 1);
                pushFragment(ORDERS_FRAGMENT_ID, bundle1);
                editLayout(ORDERS_FRAGMENT_TAG);
            } else if (Integer.parseInt(getIntent().getExtras().getString("key")) == 13) {
                pushFragment(NOTIFICATIONS_FRAGMENT_ID, null);
                editLayout(NOTIFICATIONS_FRAGMENT_TAG);
            } else {
                pushFragment(MAIN_FRAGMENT_ID, null);
                editLayout(MAIN_FRAGMENT_TAG);
            }
        } catch (Exception e) {
            Log.e("NotificationKey", "Exception " + e.getMessage());
            pushFragment(MAIN_FRAGMENT_ID, null);
            editLayout(MAIN_FRAGMENT_TAG);
            e.printStackTrace();
        }

        binding.homeLin.setOnClickListener(view -> pushFragment(GlobalVariables.MAIN_FRAGMENT_ID, null));

        binding.myOrders.setOnClickListener(view -> {
            if (LoginSession.isLoggedIn(this)) {
                Bundle bundle1 = new Bundle();
                bundle1.putInt("type", 1);
                pushFragment(GlobalVariables.ORDERS_FRAGMENT_ID, bundle1);
            } else
                Dialogs.showLoginDialog(this);
        });

        binding.notifications.setOnClickListener(view -> {
            if (LoginSession.isLoggedIn(this))
                pushFragment(GlobalVariables.NOTIFICATIONS_FRAGMENT_ID, null);
            else
                Dialogs.showLoginDialog(this);
        });

        binding.myJourney.setOnClickListener(view -> {
            if (LoginSession.isLoggedIn(this))
                pushFragment(GlobalVariables.JOURNEY_FRAGMENT_ID, null);
            else
                Dialogs.showLoginDialog(this);
        });

        binding.myProfile.setOnClickListener(view -> pushFragment(GlobalVariables.MORE_FRAGMENT_ID, null));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 123) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (LoginSession.getUserData(MainActivity.this).getResult().getUserData().isIsDelivery()) {
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startService();
                        }
                    }, 4000);
                }

                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                GlobalVariables.LOCATION_LAT = location.getLatitude();
                                GlobalVariables.LOCATION_LNG = location.getLongitude();

                                Log.e("fusedLocation", GlobalVariables.LOCATION_LAT + " " + GlobalVariables.LOCATION_LNG);
                                Log.e("fusedLocation", location.getLatitude() + " " + location.getLongitude());
                            }
                        });

                finish();
                startActivity(getIntent());
            }
        }
    }

    @Override
    public void onBackPressed() {
        Utilities.hideKeyboard(this);
        pushFragment(MAIN_FRAGMENT_ID, null);
    }

    public void pushFragment(int fragmentId, Bundle bundle) {
        String tag;
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment;

        switch (fragmentId) {
            case MAIN_FRAGMENT_ID:
                tag = MAIN_FRAGMENT_TAG;
                fragment = new HomeFragment();

                int count = fragmentManager.getBackStackEntryCount();
                for (int i = 0; i < count; ++i)
                    fragmentManager.popBackStackImmediate();
                break;
            case ORDERS_FRAGMENT_ID:
                tag = ORDERS_FRAGMENT_TAG;
                fragment = new OrdersFragment();
                fragment.setArguments(bundle);
                break;
            case NOTIFICATIONS_FRAGMENT_ID:
                tag = NOTIFICATIONS_FRAGMENT_TAG;
                fragment = new NotificationFragment();
                fragment.setArguments(bundle);
                break;
            case JOURNEY_FRAGMENT_ID:
                tag = JOURNEY_FRAGMENT_TAG;
                fragment = new JourneyFragment();
                fragment.setArguments(bundle);
                break;
            case MORE_FRAGMENT_ID:
                tag = MORE_FRAGMENT_TAG;
                fragment = new MoreFragment();
                fragment.setArguments(bundle);
                break;

            default:
                try {
                    throw new Exception("Invalid fragment id :: " + fragmentId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
        }

        editLayout(tag);

        selectedFragmentTag = tag;

        fragmentTransaction.replace(R.id.container, fragment, tag).commit();
    }

    public void editLayout(String fragmentTag) {
        selectedFragmentTag = fragmentTag;

        binding.main.clearAnimation();
        binding.notificationImg.clearAnimation();
        binding.ordersImg.clearAnimation();
        binding.profile.clearAnimation();
        binding.journey.clearAnimation();

        binding.main.setImageResource(R.drawable.setting);
        binding.notificationImg.setImageResource(R.drawable.notification);
        binding.ordersImg.setImageResource(R.drawable.orders);
        binding.journey.setImageResource(R.drawable.kkkk);
        binding.profile.setImageResource(R.drawable.profile);

        binding.main.setColorFilter(getResources().getColor(R.color.colorTextHint), PorterDuff.Mode.SRC_ATOP);
        binding.notificationImg.setColorFilter(getResources().getColor(R.color.colorTextHint), PorterDuff.Mode.SRC_ATOP);
        binding.ordersImg.setColorFilter(getResources().getColor(R.color.colorTextHint), PorterDuff.Mode.SRC_ATOP);
        binding.journey.setColorFilter(getResources().getColor(R.color.colorTextHint), PorterDuff.Mode.SRC_ATOP);
        binding.profile.setColorFilter(getResources().getColor(R.color.colorTextHint), PorterDuff.Mode.SRC_ATOP);

        binding.mainTxt.setTextColor(getResources().getColor(R.color.colorTextHint));
        binding.ordersTxt.setTextColor(getResources().getColor(R.color.colorTextHint));
        binding.notificationTxt.setTextColor(getResources().getColor(R.color.colorTextHint));
        binding.journeyTxt.setTextColor(getResources().getColor(R.color.colorTextHint));
        binding.profileTxt.setTextColor(getResources().getColor(R.color.colorTextHint));

//        binding.mainTxt.setVisibility(View.GONE);
//        binding.notificationTxt.setVisibility(View.GONE);
//        binding.ordersTxt.setVisibility(View.GONE);
//        binding.journeyTxt.setVisibility(View.GONE);
//        binding.profileTxt.setVisibility(View.GONE);

        if (fragmentTag.equals(MAIN_FRAGMENT_TAG)) {
            binding.main.startAnimation(rotateAnimation);
            binding.mainTxt.setTextColor(getResources().getColor(R.color.colorGreen));
            binding.main.setColorFilter(getResources().getColor(R.color.colorGreen), PorterDuff.Mode.SRC_ATOP);
        } else if (fragmentTag.equals(ORDERS_FRAGMENT_TAG)) {
            binding.ordersImg.startAnimation(rotateAnimation);
            binding.ordersTxt.setTextColor(getResources().getColor(R.color.colorGreen));
            binding.ordersImg.setColorFilter(getResources().getColor(R.color.colorGreen), PorterDuff.Mode.SRC_ATOP);
        } else if (fragmentTag.equals(NOTIFICATIONS_FRAGMENT_TAG)) {
            binding.notificationImg.startAnimation(rotateAnimation);
            binding.notificationTxt.setTextColor(getResources().getColor(R.color.colorGreen));
            binding.notificationImg.setColorFilter(getResources().getColor(R.color.colorGreen), PorterDuff.Mode.SRC_ATOP);
        } else if (fragmentTag.equals(JOURNEY_FRAGMENT_TAG)) {
            binding.journey.startAnimation(rotateAnimation);
            binding.journeyTxt.setTextColor(getResources().getColor(R.color.colorGreen));
            binding.journey.setColorFilter(getResources().getColor(R.color.colorGreen), PorterDuff.Mode.SRC_ATOP);
        } else if (fragmentTag.equals(MORE_FRAGMENT_TAG)) {
            binding.profile.startAnimation(rotateAnimation);
            binding.profileTxt.setTextColor(getResources().getColor(R.color.colorGreen));
            binding.profile.setColorFilter(getResources().getColor(R.color.colorGreen), PorterDuff.Mode.SRC_ATOP);
        }
    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(playSoundReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver playSoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent

            Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notify);

            final MediaPlayer mMediaPlayer = MediaPlayer.create(MainActivity.this, soundUri);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setLooping(true);

            CountDownTimer cntr_aCounter = new CountDownTimer(15000, 1000) {
                public void onTick(long millisUntilFinished) {

                    mMediaPlayer.start();
                }

                public void onFinish() {
                    //code fire after finish
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                }
            };
            cntr_aCounter.start();
        }
    };

    private void profileData() {
        //final LoadingDialog loadingDialog = new LoadingDialog();
        APIModel.getMethod(this, "Authorization/GetProfile", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                switch (statusCode) {
                    default:
                        APIModel.handleFailure(MainActivity.this, statusCode, responseString, new APIModel.RefreshTokenListener() {
                            @Override
                            public void onRefresh() {
                                profileData();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);

                Type dataType = new TypeToken<UserModel>() {
                }.getType();
                UserModel data = new Gson().fromJson(responseString, dataType);

                LoginSession.setUserData(MainActivity.this, data);


                if (LoginSession.getUserData(MainActivity.this).getResult().getUserData().isIsDelivery()) {
                    binding.myJourney.setVisibility(View.VISIBLE);
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startService();
                        }
                    }, 4000);
                }

                //startService();// TODO: 6/10/2020
            }

            @Override
            public void onStart() {
                super.onStart();
                //Dialogs.showLoading(MainActivity.this, loadingDialog);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                //Dialogs.dismissLoading(loadingDialog);
            }
        });
    }

    private void updateFcmToken() {
        //  final LoadingDialog loadingDialog = new LoadingDialog();

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("User_Fcm", FirebaseInstanceId.getInstance().getToken());
            jsonParams.put("Mobile_Type", "ANDROID");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        APIModel.postMethod(this, "Authorization/UpdateFcm", jsonParams, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                switch (statusCode) {
                    case 401:
                        try {
                            JSONObject jsonObject = new JSONObject(responseString);
                            if (jsonObject.getJSONObject("error").has("message"))
                                Utilities.toastyError(MainActivity.this, jsonObject.getJSONObject("error").getString("message"));
                            else
                                Utilities.toastyError(MainActivity.this, responseString + "    ");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 400:
                        try {
                            JSONObject jsonObject = new JSONObject(responseString);
                            if (jsonObject.getJSONObject("error").has("message"))
                                Utilities.toastyError(MainActivity.this, jsonObject.getJSONObject("error").getString("message"));
                            else
                                Utilities.toastyError(MainActivity.this, responseString + "  ");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        APIModel.handleFailure(MainActivity.this, statusCode, responseString, new APIModel.RefreshTokenListener() {
                            @Override
                            public void onRefresh() {
                                updateFcmToken();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);
            }

            @Override
            public void onStart() {
                super.onStart();
                //    Dialogs.showLoading(MainActivity.this, loadingDialog);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                //    Dialogs.dismissLoading(loadingDialog);
            }
        });
    }

    private void startService() {
        Intent myService = new Intent(this, TrackingDelegate.class);
        startService(myService);
    }
}