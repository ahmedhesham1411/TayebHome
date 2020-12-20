package com.uriallab.haat.haat.viewModels;

import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.DataModels.NotificationsModel;
import com.uriallab.haat.haat.UI.Fragments.NotificationFragment;
import com.uriallab.haat.haat.Utilities.Dialogs;
import com.uriallab.haat.haat.Utilities.LoadingDialog;
import com.uriallab.haat.haat.Utilities.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.ViewModel;

public class NotificationsViewModel extends ViewModel {

    public ObservableBoolean isNoData = new ObservableBoolean(false);

    private Activity activity;
    private NotificationFragment fragment;

    public boolean isLoading = false;

    public int lastPage = 1;

    public NotificationsViewModel(NotificationFragment fragment) {
        activity = fragment.getActivity();
        this.fragment = fragment;
        getNotificationsRequest(1);
    }

    public void getNotificationsRequest(final int nextPage) {
        final LoadingDialog loadingDialog = new LoadingDialog();

        APIModel.getMethod(activity, "Setting/GetNotifications?id=" + nextPage, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                switch (statusCode) {
                    case 400:
                        try {
                            JSONObject jsonObject = new JSONObject(responseString);
                            if (jsonObject.has("error"))
                                Utilities.toastyError(activity, jsonObject.getJSONObject("error").getString("message"));
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
                                getNotificationsRequest(nextPage);
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);

                Type dataType = new TypeToken<NotificationsModel>() {
                }.getType();
                NotificationsModel data = new Gson().fromJson(responseString, dataType);

                lastPage = data.getResult().getEndPage();

                if (data.getResult().getNotfications().size() > 0)
                    if (nextPage==1){
                        fragment.updateRecycler1(data.getResult().getNotfications());

                    }else {
                        fragment.updateRecycler(data.getResult().getNotfications());
                    }
                else
                    isNoData.set(true);
            }

            @Override
            public void onStart() {
                super.onStart();
                isLoading = true;
                Dialogs.showLoading(activity, loadingDialog);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                isLoading = false;
                Dialogs.dismissLoading(loadingDialog);
            }
        });
    }
}