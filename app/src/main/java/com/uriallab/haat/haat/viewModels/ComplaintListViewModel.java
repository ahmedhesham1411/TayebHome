package com.uriallab.haat.haat.viewModels;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.DataModels.ComplaintModel;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.UI.Activities.utilsAndAccountInfo.ComplaintListActivity;
import com.uriallab.haat.haat.Utilities.Dialogs;
import com.uriallab.haat.haat.Utilities.LoadingDialog;

import java.lang.reflect.Type;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;

public class ComplaintListViewModel {

    public ObservableBoolean isNoData = new ObservableBoolean(false);
    public ObservableBoolean isFinishedObservable = new ObservableBoolean(false);

    public ObservableInt rotation = new ObservableInt(0);

    private ComplaintListActivity activity;

    public ComplaintListViewModel(ComplaintListActivity activity) {
        this.activity = activity;

        if (ConfigurationFile.getCurrentLanguage(activity).equals("ar"))
            rotation.set(180);

        getComplaints();
    }

    private void getComplaints() {
        final LoadingDialog loadingDialog = new LoadingDialog();
        APIModel.getMethod(activity, "Setting/GetComplaint", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                isNoData.set(true);
                switch (statusCode) {
                    default:
                        APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                            @Override
                            public void onRefresh() {
                                getComplaints();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);

                Type dataType = new TypeToken<ComplaintModel>() {
                }.getType();
                ComplaintModel data = new Gson().fromJson(responseString, dataType);

                if (data.getResult().getComplaints().size() > 0) {
                    isNoData.set(false);
                    activity.initRecycler(data.getResult().getComplaints());
                } else
                    isNoData.set(true);

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

    public void back() {
        activity.finish();
    }
}