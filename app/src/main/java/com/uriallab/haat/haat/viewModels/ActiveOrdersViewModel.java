package com.uriallab.haat.haat.viewModels;

import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.DataModels.OrdersModel;
import com.uriallab.haat.haat.UI.Fragments.ActiveOrdersFragment;
import com.uriallab.haat.haat.Utilities.Dialogs;
import com.uriallab.haat.haat.Utilities.LoadingDialog;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.databinding.ObservableBoolean;

public class ActiveOrdersViewModel {

    public ObservableBoolean isNoData = new ObservableBoolean(false);

    private List<OrdersModel.ResultBean.OrdersBean> orderWaitingList = new ArrayList<>();

    private Activity activity;
    private ActiveOrdersFragment fragment;

    public ActiveOrdersViewModel(ActiveOrdersFragment fragment) {
        this.fragment = fragment;
        activity = fragment.getActivity();

        getOrders();

    }

    private void getOrders() {
        final LoadingDialog loadingDialog = new LoadingDialog();
        APIModel.getMethod(activity, "Client/GetOrdersInWating", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                switch (statusCode) {
                    default:
                        APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                            @Override
                            public void onRefresh() {
                                getOrders();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);

                Type dataType = new TypeToken<OrdersModel>() {
                }.getType();
                OrdersModel data = new Gson().fromJson(responseString, dataType);

                orderWaitingList.clear();
                orderWaitingList.addAll(data.getResult().getOrders());
                fragment.updateWaitingList(orderWaitingList);

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

    public void getOrders2() {
        APIModel.getMethod(activity, "Client/GetOrdersInWating", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                switch (statusCode) {
                    default:
                        APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                            @Override
                            public void onRefresh() {
                                getOrders();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);

                Type dataType = new TypeToken<OrdersModel>() {
                }.getType();
                OrdersModel data = new Gson().fromJson(responseString, dataType);

                orderWaitingList.clear();
                orderWaitingList.addAll(data.getResult().getOrders());
                fragment.updateWaitingList(orderWaitingList);

            }

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });
    }
}