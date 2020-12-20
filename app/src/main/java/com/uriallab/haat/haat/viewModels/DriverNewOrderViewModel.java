package com.uriallab.haat.haat.viewModels;

import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;
import com.squareup.picasso.Picasso;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.DataModels.OrderDetailsModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.UI.Activities.AddOfferActivity;
import com.uriallab.haat.haat.UI.Activities.DriverNewOrderActivity;
import com.uriallab.haat.haat.Utilities.Dialogs;
import com.uriallab.haat.haat.Utilities.IntentClass;
import com.uriallab.haat.haat.Utilities.LoadingDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

public class DriverNewOrderViewModel extends ViewModel {

    public ObservableField<String> orderNumber = new ObservableField<>();

    private String orderId;

    public String clientId;

    private double appPercentage, tax;

    private DriverNewOrderActivity activity;

    private double shopLat, shopLng, clientLat, clientLng;

    public DriverNewOrderViewModel(DriverNewOrderActivity activity, String orderId) {
        this.activity = activity;
        this.orderId = orderId;

        orderNumber.set(activity.getString(R.string.order_number) + " \n" + orderId);

        if (ConfigurationFile.getCurrentLanguage(activity).equals("ar")) {
            activity.binding.backImg.setRotation(180);
            activity.binding.starBar.setRotationY(180);
        }

        getOrderDetails();
    }

    private void getOrderDetails() {
        final LoadingDialog loadingDialog = new LoadingDialog();
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("OrderUID", orderId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        APIModel.postMethod(activity, "Driver/GetMyOrderDetails", jsonParams, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                switch (statusCode) {
                    default:
                        APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                            @Override
                            public void onRefresh() {
                                getOrderDetails();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);

                Type dataType = new TypeToken<OrderDetailsModel>() {
                }.getType();
                OrderDetailsModel data = new Gson().fromJson(responseString, dataType);

                Picasso.get().load( APIModel.BASE_URL+data.getResult().getOrder().getClient_Img_Url())
                        .placeholder(R.drawable.user_img).into(activity.binding.orderImg);
                activity.binding.driverName.setText(data.getResult().getOrder().getClient_Full_Name());
                activity.binding.ratesNumberVal.setText(data.getResult().getOrder().getClient_Count_Rate() + "");
                activity.binding.starBar.setRating(data.getResult().getOrder().getClient_Rate());

                try {
                    shopLat = Double.parseDouble(data.getResult().getOrder().getShop_Lat());
                    shopLng = Double.parseDouble(data.getResult().getOrder().getShop_Lng());
                }catch (Exception e){}

                clientLat = Double.parseDouble(data.getResult().getOrder().getClient_Lat());
                clientLng = Double.parseDouble(data.getResult().getOrder().getClient_Lng());

                clientId = String.valueOf(data.getResult().getOrder().getOrd_ClientID());

                appPercentage = data.getResult().getOffer_App_Per();
                tax = data.getResult().getOffer_Vat();

                List<String> chatList = new ArrayList<>();
                chatList.add(data.getResult().getOrder().getOrd_Dtls());
                for (int i = 0; i < data.getResult().getImgs().size(); i++)
                    chatList.add(data.getResult().getImgs().get(i).getImgUrl());

                activity.initRecycler(chatList);
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

    public void sendReport() {
        activity.sendReport();
    }

    public void readyToDeliver() {
        try {
            Bundle bundle = new Bundle();
            bundle.putDouble("latClient", clientLat);
            bundle.putDouble("lngClient", clientLng);
            bundle.putDouble("lat", shopLat);
            bundle.putDouble("lng", shopLng);
            bundle.putDouble("appPercentage", appPercentage);
            bundle.putDouble("tax", tax);
            bundle.putString("orderId", orderId);
            bundle.putString("clientId", clientId);
            IntentClass.goToActivity(activity, AddOfferActivity.class, bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void back() {
        activity.finish();
    }
}