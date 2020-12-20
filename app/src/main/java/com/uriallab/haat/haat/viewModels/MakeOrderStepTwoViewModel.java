package com.uriallab.haat.haat.viewModels;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.DataModels.MakeOrderModel;
import com.uriallab.haat.haat.DataModels.OverviewPointModel;
import com.uriallab.haat.haat.DataModels.TimeModel;
import com.uriallab.haat.haat.DatePickerFragment;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.SharedPreferences.LoginSession;
import com.uriallab.haat.haat.UI.Activities.SentSuccessfullyActivity;
import com.uriallab.haat.haat.UI.Activities.makeOrder.DeliveringTimeActivity;
import com.uriallab.haat.haat.UI.Activities.makeOrder.DelivieringLocationActivity;
import com.uriallab.haat.haat.UI.Activities.makeOrder.FavouritesActivity;
import com.uriallab.haat.haat.UI.Activities.makeOrder.MakeOrderStepTwoActivity;
import com.uriallab.haat.haat.Utilities.Dialogs;
import com.uriallab.haat.haat.Utilities.GlobalVariables;
import com.uriallab.haat.haat.Utilities.IntentClass;
import com.uriallab.haat.haat.Utilities.LoadingDialog;
import com.uriallab.haat.haat.Utilities.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MakeOrderStepTwoViewModel {

    public ObservableInt rotate = new ObservableInt(0);

    public ObservableBoolean isService = new ObservableBoolean(false);

    public ObservableField<String> addressObservable = new ObservableField<>("");
    public ObservableField<String> distenationObservable = new ObservableField<>("");
    public ObservableField<String> additionalAddressObservable = new ObservableField<>("");
    public ObservableField<String> timeObservable = new ObservableField<>("");
    public ObservableField<String> dateObservable = new ObservableField<>("");
    public ObservableField<String> timeIdObservable = new ObservableField<>("");

    public double userLat, userLng;

    public double shopLat, shopLng;

    private String storeName, details, shopImg, coupon, catId;

    private List<String> images;

    private MakeOrderStepTwoActivity activity;

    private LatLng currentLatLng;

    public MakeOrderStepTwoViewModel(MakeOrderStepTwoActivity activity, MakeOrderModel makeOrderModel) {
        this.activity = activity;
        storeName = makeOrderModel.getStoreName();
        details = makeOrderModel.getDetails();
        images = makeOrderModel.getImages();
        coupon = makeOrderModel.getCoupon();

        shopImg = makeOrderModel.getShopImg();

        catId = makeOrderModel.getCatId();

        isService.set(makeOrderModel.isService());

        userLat = GlobalVariables.LOCATION_LAT;
        userLng = GlobalVariables.LOCATION_LNG;

        if (ConfigurationFile.getCurrentLanguage(activity).equals("ar"))
            rotate.set(180);

        getTimeRequest();
    }

    public void createOrder() {

        if (LoginSession.isLoggedIn(activity)) {
            if (isService.get()) {
                if (timeObservable.get().equals("") || dateObservable.get().equals("") || addressObservable.get().equals("") || distenationObservable.get().equals("")) {

                    if (timeObservable.get().equals(""))
                        Utilities.toastyRequiredFieldCustom(activity, activity.getString(R.string.please_delivering_time_));

                    if (distenationObservable.get().equals(""))
                        Utilities.toastyRequiredFieldCustom(activity, activity.getString(R.string.receiving_point));

                    if (dateObservable.get().equals(""))
                        Utilities.toastyRequiredFieldCustom(activity, activity.getString(R.string.please_delivering_date_));

                    if (addressObservable.get().equals(""))
                        Utilities.toastyRequiredFieldCustom(activity, activity.getString(R.string.delivering_point));

                } else
                    createOrderRequest();
            } else {
                if (timeObservable.get().equals("") || dateObservable.get().equals("") || addressObservable.get().equals("")) {

                    if (timeObservable.get().equals(""))
                        Utilities.toastyRequiredFieldCustom(activity, activity.getString(R.string.please_delivering_time_));

                    if (dateObservable.get().equals(""))
                        Utilities.toastyRequiredFieldCustom(activity, activity.getString(R.string.please_delivering_date_));

                    if (addressObservable.get().equals(""))
                        Utilities.toastyRequiredFieldCustom(activity, activity.getString(R.string.please_detect_delivering_location));

                } else
                    createOrderRequest();
            }
        } else
            Dialogs.showLoginDialog(activity);
    }

    private void createOrderRequest() {

        Log.e("ClientLatLng", userLat + "\t" + userLng);

        if (userLat == 0.0 || userLng == 0.0) {
            Utilities.toastyError(activity, activity.getString(R.string.detect_delivering_location));
        } else {
            final LoadingDialog loadingDialog = new LoadingDialog();

            JSONObject jsonParams = new JSONObject();
            try {
                JSONArray jsonArray = new JSONArray();

                if (images.size() > 0) {
                    for (int i = 0; i < images.size(); i++) {
                        jsonArray.put(images.get(i));
                    }
                }

                jsonParams.put("Ord_Dtls", details);
                jsonParams.put("Ord_DurationID", timeIdObservable.get());
                jsonParams.put("Ord_Additional_Dta", additionalAddressObservable.get());
                jsonParams.put("Client_Lng", userLng);
                jsonParams.put("Client_Lat", userLat);
                jsonParams.put("Ord_Shop_Nm", storeName);
                jsonParams.put("Ord_Shop_ImgUrl", shopImg);
                jsonParams.put("Ord_Is_Servece", isService.get());
                jsonParams.put("Coupon_Code", coupon);
                jsonParams.put("OrdImgs", jsonArray);
                jsonParams.put("User_Profession_And_Department_ID", catId);
                jsonParams.put("Ord_Work_Date", dateObservable.get());

                if (isService.get()) {
                    jsonParams.put("Shop_Lat", shopLat);
                    jsonParams.put("Shop_Lng", shopLng);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

            APIModel.postMethod(activity, "Client/SendOrder", jsonParams, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                    Log.e("response", responseString + "Error");
/*                    if (responseString == null) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("sentType", 1);
                        IntentClass.goToActivity(activity, SentSuccessfullyActivity.class, bundle);
                    } else {*/
                        switch (statusCode) {
                            case 400:
                                try {
                                    // Utilities.toastyError(activity, responseString + "    ");

                                    JSONObject jsonObject = new JSONObject(responseString);
                                    if (jsonObject.has("error"))
                                        Utilities.toastyError(activity, jsonObject.getJSONObject("error").getString("Message"));
                                    else
                                        Utilities.toastyError(activity, responseString + "    ");

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 500:
                                try {
                                    JSONObject jsonObject = new JSONObject(responseString);
                                    if (jsonObject.has("error"))
                                        Utilities.toastyError(activity, jsonObject.getJSONObject("error").getString("Message"));
                                    else
                                        Utilities.toastyError(activity, responseString + "");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            default:
                                APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                                    @Override
                                    public void onRefresh() {
                                        createOrderRequest();
                                    }
                                });
                                break;
                        }
                    }

                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                    Log.e("response", responseString);
                    Bundle bundle = new Bundle();
                    bundle.putInt("sentType", 1);
                    IntentClass.goToActivity(activity, SentSuccessfullyActivity.class, bundle);
                }

                @Override
                public void onStart() {
                    super.onStart();
                    Dialogs.showLoading(activity, loadingDialog);
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    Dialogs.dismissLoading(loadingDialog);
                }
            });
        }
    }

    public void deliveringLocation(int type) {
        if (type == 1)
            IntentClass.goToStartForResult(activity, DelivieringLocationActivity.class, 111, null);
        else
            IntentClass.goToStartForResult(activity, DelivieringLocationActivity.class, 122, null);
    }

    public void deliveringTime() {
        IntentClass.goToStartForResult(activity, DeliveringTimeActivity.class, 222, null);
    }

    public void dateTime() {
        DatePickerFragment mDatePicker = new DatePickerFragment(activity, dateObservable);
        mDatePicker.show(activity.getSupportFragmentManager(), "Select date");
    }

    public void favourites() {
        if (LoginSession.isLoggedIn(activity))
            IntentClass.goToStartForResult(activity, FavouritesActivity.class, 333, null);
        else
            Dialogs.showLoginDialog(activity);
    }

    public void back() {
        activity.finish();
    }

    private void getTimeRequest() {
        final LoadingDialog loadingDialog = new LoadingDialog();

        APIModel.getMethod(activity, "Client/GetDuration", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                switch (statusCode) {
                    case 400:
                        try {
                            JSONObject jsonObject = new JSONObject(responseString);
                            if (jsonObject.has("error"))
                                Utilities.toastyError(activity, jsonObject.getJSONObject("error").getString("Message"));
                            else
                                Utilities.toastyError(activity, responseString + "    ");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                            @Override
                            public void onRefresh() {
                                getTimeRequest();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);

                Type dataType = new TypeToken<TimeModel>() {
                }.getType();
                TimeModel data = new Gson().fromJson(responseString, dataType);

                if (data.getResult().getDurations().size() > 0) {
                    timeObservable.set(data.getResult().getDurations().get(0).getDuration_Nm());
                    timeIdObservable.set(String.valueOf(data.getResult().getDurations().get(0).getDurationUID()));
                }

            }

            @Override
            public void onStart() {
                super.onStart();
                Dialogs.showLoading(activity, loadingDialog);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                Dialogs.dismissLoading(loadingDialog);
            }
        });
    }

    public void drawDirection() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            final LoadingDialog loadingDialog = new LoadingDialog();

            currentLatLng = new LatLng(userLat, userLng);

            String url = "/directions/json?origin=" +
                    userLat + "," + userLng +
                    "&destination=" + shopLat + "," + shopLng +
                    "&sensor=false&key=" +
                    activity.getResources().getString(R.string.api_key);

            APIModel.getMethodForGoogle2(activity, url, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                    Log.e("response", responseString + "Error");
                    switch (statusCode) {
                        default:
                            APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                                @Override
                                public void onRefresh() {
                                    drawDirection();
                                }
                            });
                            break;
                    }
                }

                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                    Log.e("response", responseString);
                    Type dataType = new TypeToken<OverviewPointModel>() {
                    }.getType();
                    OverviewPointModel data = new Gson().fromJson(responseString, dataType);

                    try {
                        drawDirectionToStop(data.getRoutes().get(0).getOverview_polyline().getPoints());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onStart() {
                    super.onStart();
                    Dialogs.showLoading(activity, loadingDialog);
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    Dialogs.dismissLoading(loadingDialog);
                }
            });
        }
    }

    private void drawDirectionToStop(String overviewPolyline) {
        if (overviewPolyline != null) {
            activity.mMap.clear();

            List<LatLng> polyz = decodeOverviewPolyLinePonts(overviewPolyline);

//            activity.binding.mapOverlayView.removeRoutes();
//            new Route.Builder(activity.binding.mapOverlayView)
//                    .setRouteType(RouteOverlayView.RouteType.PATH)
//                    .setCameraPosition(activity.mMap.getCameraPosition())
//                    .setProjection(activity.mMap.getProjection())
//                    .setLatLngs(polyz)
//                    .setBottomLayerColor(activity.getResources().getColor(R.color.colorBlue))
//                    .setTopLayerColor(activity.getResources().getColor(R.color.colorMoov))
//                    .create();

            if (polyz != null) {
                PolylineOptions lineOptions = new PolylineOptions();
                lineOptions.addAll(polyz);
                lineOptions.width(5);
                lineOptions.color(ContextCompat.getColor(activity, R.color.colorMoov));
                activity.mMap.addPolyline(lineOptions);
            }

            activity.mMap.setOnCameraMoveListener(() -> {
                        activity.binding.mapOverlayView.onCameraMove(activity.mMap.getProjection(), activity.mMap.getCameraPosition());
                    }
            );
        }

        activity.mMap.addMarker(new MarkerOptions()
                .position(currentLatLng)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromBitmap(activity.getMarkerBitmapFromView(true))));
    }

    //This function is to parse the value of "points"
    public List<LatLng> decodeOverviewPolyLinePonts(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        if (encoded != null && !encoded.isEmpty() && encoded.trim().length() > 0) {
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }
        }
        return poly;
    }
}