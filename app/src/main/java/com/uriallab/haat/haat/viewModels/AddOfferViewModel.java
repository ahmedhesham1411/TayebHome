package com.uriallab.haat.haat.viewModels;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.DataModels.OverviewPointModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.SharedPreferences.LoginSession;
import com.uriallab.haat.haat.UI.Activities.AddOfferActivity;
import com.uriallab.haat.haat.UI.Activities.SentSuccessfullyActivity;
import com.uriallab.haat.haat.Utilities.Dialogs;
import com.uriallab.haat.haat.Utilities.GlobalVariables;
import com.uriallab.haat.haat.Utilities.IntentClass;
import com.uriallab.haat.haat.Utilities.LoadingDialog;
import com.uriallab.haat.haat.Utilities.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class AddOfferViewModel {

    public AddOfferViewModel() {
    }

    public ObservableField<String> offerPrice = new ObservableField<>("");
    public ObservableField<String> totalPrice = new ObservableField<>("");

    public ObservableInt rotate = new ObservableInt(0);

    private AddOfferActivity activity;

    private String orderId, clientId;

    private double appPercentage, tax;

    private LatLng storeLatLng, clientLatLng;

    public AddOfferViewModel(AddOfferActivity activity, String orderId, String clientId, double appPercentage, double tax, LatLng storeLatLng, LatLng clientLatLng) {
        this.activity = activity;
        this.orderId = orderId;
        this.clientId = clientId;
        this.appPercentage = appPercentage;
        this.tax = tax;
        this.storeLatLng = storeLatLng;
        this.clientLatLng = clientLatLng;

        totalPrice.set(" 00 " + activity.getString(R.string.currency));

        if (ConfigurationFile.getCurrentLanguage(activity).equals("ar"))
            rotate.set(180);

        drawDirection();
    }

    public boolean isAcceptedPrice(String price) {
        double priceD;
        try {
            priceD = Double.valueOf(price);
        } catch (Exception e) {
            priceD = 0;
            e.printStackTrace();
        }


        return !(priceD <= 0);
    }

  /*  public void sendOffer() {

        Utilities.hideKeyboard(activity);

        if (offerPrice.get().equals("")) {
            Utilities.toastyRequiredFieldCustom(activity, activity.getString(R.string.enter_price));
        } else if (Double.parseDouble(offerPrice.get()) <= 0) {
            Utilities.toastyRequiredFieldCustom(activity, activity.getString(R.string.enter_price));
        } else {
            final LoadingDialog loadingDialog = new LoadingDialog();

            JSONObject jsonParams = new JSONObject();
            try {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    jsonParams.put("Offer_Price", offerPrice.get());
                    jsonParams.put("Order_ID", orderId);
                    jsonParams.put("Driver_Long", GlobalVariables.LOCATION_LNG);
                    jsonParams.put("Driver_Lat", GlobalVariables.LOCATION_LAT);
                    jsonParams.put("Client_ID", clientId);
                    jsonParams.put("Driver_ID", LoginSession.getUserData(activity).getResult().getUserData().getUserUID());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            APIModel.postMethod(activity, "Driver/SetOrderOffer", jsonParams, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                    Log.e("response", responseString + "Error");
                    switch (statusCode) {
                        case 401:
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
                        case 400:
                            try {
                                JSONObject jsonObject = new JSONObject(responseString);
                                if (jsonObject.has("error"))
                                    Utilities.toastyError(activity, jsonObject.getJSONObject("error").getString("Message"));
                                else
                                    Utilities.toastyError(activity, responseString + "   ");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                                @Override
                                public void onRefresh() {
                                    sendOffer();
                                }
                            });
                            break;
                    }
                }

                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                    Log.e("response", responseString);

                    Bundle bundle = new Bundle();
                    bundle.putInt("sentType", 2);
                    bundle.putString("orderId", orderId);
                    bundle.putString("clientId", clientId);
                    IntentClass.goToActivity(activity, SentSuccessfullyActivity.class, bundle);

                    activity.finish();
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
    }*/

    public void sendOffer() {

        Utilities.hideKeyboard(activity);

        if (offerPrice.get().equals("")) {
            Utilities.toastyRequiredFieldCustom(activity, activity.getString(R.string.enter_price));
        } else if (Double.parseDouble(offerPrice.get()) <= 0) {
            Utilities.toastyRequiredFieldCustom(activity, activity.getString(R.string.enter_price));
        } else {
            final LoadingDialog loadingDialog = new LoadingDialog();

            JSONObject jsonParams = new JSONObject();
            try {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    jsonParams.put("Offer_Price", offerPrice.get());
                    jsonParams.put("Order_ID", orderId);
                    jsonParams.put("Driver_Long", GlobalVariables.LOCATION_LNG);
                    jsonParams.put("Driver_Lat", GlobalVariables.LOCATION_LAT);
                    jsonParams.put("Client_ID", clientId);
                    jsonParams.put("Driver_ID", LoginSession.getUserData(activity).getResult().getUserData().getUserUID());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            APIModel.postMethod(activity, "Driver/SetOrderOffer", jsonParams, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                    Log.e("response", responseString + "Error");
                    switch (statusCode) {
                        case 401:
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
                        case 400:
                            try {
                                JSONObject jsonObject = new JSONObject(responseString);
                                if (jsonObject.has("error"))
                                    Utilities.toastyError(activity, jsonObject.getJSONObject("error").getString("Message"));
                                else
                                    Utilities.toastyError(activity, responseString + "   ");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                                @Override
                                public void onRefresh() {
                                    sendOffer();
                                }
                            });
                            break;
                    }
                }

                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                    Log.e("response", responseString);

                    Bundle bundle = new Bundle();
                    bundle.putInt("sentType", 2);
                    bundle.putString("orderId", orderId);
                    bundle.putString("clientId", clientId);
                    IntentClass.goToActivity(activity, SentSuccessfullyActivity.class, bundle);

                    activity.finish();
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
    public TextWatcher getSearchText() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {
                try {
                    double offerPriceLocal;
                    if (editable.length() == 0) {
                        offerPriceLocal = 0;
                    } else {
                        offerPriceLocal = Double.parseDouble(editable.toString());
                    }

                    double appPer = offerPriceLocal * (appPercentage / 100);
                    double taxes = (offerPriceLocal + appPer) * (tax / 100);
                    double total = Utilities.round(offerPriceLocal, 2) + Utilities.round(appPer, 2) + Utilities.round(taxes, 2);
                    offerPrice.set(editable.toString());
                    totalPrice.set(Utilities.round(total, 2) + " " + activity.getString(R.string.currency));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void back() {
        Utilities.hideKeyboard(activity);
        activity.finish();
    }

    public void drawDirection() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            final LoadingDialog loadingDialog = new LoadingDialog();

            double lat = GlobalVariables.LOCATION_LAT;
            double lng = GlobalVariables.LOCATION_LNG;

            String url = "/directions/json?origin=" +
                    lat + "," + lng +
                    "&destination=" + storeLatLng.latitude + "," + storeLatLng.longitude +
                    "&sensor=false&key=" +
                    activity.getResources().getString(R.string.api_key);

            APIModel.getMethodForGoogle2(activity, url, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
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
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
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
            // setAnimation(activity.mMap, polyz); // TODO: 7/21/2020 animation
            if (polyz != null) {
                PolylineOptions lineOptions = new PolylineOptions();
                lineOptions.addAll(polyz);
                lineOptions.width(5);
                lineOptions.color(ContextCompat.getColor(activity, R.color.colorMoov));
                activity.mMap.addPolyline(lineOptions);
            }

        }

        activity.mMap.addMarker(new MarkerOptions()
                .position(clientLatLng)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromBitmap(activity.getMarkerBitmapFromView(true))));

        activity.mMap.addMarker(new MarkerOptions()
                .position(new LatLng(storeLatLng.latitude, storeLatLng.longitude))
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromBitmap(activity.getMarkerBitmapFromView(false))));
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

    public void setAnimation(GoogleMap myMap, final List<LatLng> directionPoint) {

        myMap.clear();

        Marker marker = myMap.addMarker(new MarkerOptions()
                .position(directionPoint.get(0))
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView())));

        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(directionPoint.get(0), 16));

        animateMarker(myMap, marker, directionPoint, false);
    }

    private void animateMarker(GoogleMap myMap, final Marker marker, final List<LatLng> directionPoint, final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = myMap.getProjection();
        final long duration = 30000;

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
                    marker.setRotation(bearingBetweenLocations(new LatLng(directionPoint.get(i).latitude,
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

    private float bearingBetweenLocations(LatLng latLng1, LatLng latLng2) {

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

    private Bitmap getMarkerBitmapFromView() {

        View customMarkerView = ((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_view_home_marker, null);

        FrameLayout frameCar = customMarkerView.findViewById(R.id.frame_car);
        LinearLayout mainLin = customMarkerView.findViewById(R.id.custom_marker_view);

        mainLin.setRotation(180);

        frameCar.setVisibility(View.GONE);

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