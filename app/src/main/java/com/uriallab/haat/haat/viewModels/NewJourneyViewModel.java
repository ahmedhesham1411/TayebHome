package com.uriallab.haat.haat.viewModels;

import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.DataModels.JourneyModel;
import com.uriallab.haat.haat.UI.Fragments.NewJourneyFragment;
import com.uriallab.haat.haat.Utilities.Dialogs;
import com.uriallab.haat.haat.Utilities.LoadingDialog;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.databinding.ObservableBoolean;

public class NewJourneyViewModel {

    public ObservableBoolean isNoData = new ObservableBoolean(false);

    private List<JourneyModel.ResultBean.OrdersBean> orderWaitingList = new ArrayList<>();

    private Activity activity;
    private NewJourneyFragment fragment;

    public NewJourneyViewModel(NewJourneyFragment fragment) {
        this.fragment = fragment;
        activity = fragment.getActivity();

        getOrders2();
    }

  /*  private void getOrders() {
        final LoadingDialog loadingDialog = new LoadingDialog();
        APIModel.getMethod(activity, "Driver/GetOrdersWaitingApproval", new TextHttpResponseHandler() {
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

                Type dataType = new TypeToken<JourneyModel>() {
                }.getType();
                JourneyModel data = new Gson().fromJson(responseString, dataType);

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
    }*/

    private void getOrders() {
        final LoadingDialog loadingDialog = new LoadingDialog();
        APIModel.getMethod(activity, "Driver/GetOrdersWaitingApproval", new TextHttpResponseHandler() {
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

                Type dataType = new TypeToken<JourneyModel>() {
                }.getType();
                JourneyModel data = new Gson().fromJson(responseString, dataType);

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
        APIModel.getMethod(activity, "Driver/GetOrdersWaitingApproval", new TextHttpResponseHandler() {
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

                Type dataType = new TypeToken<JourneyModel>() {
                }.getType();
                JourneyModel data = new Gson().fromJson(responseString, dataType);

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