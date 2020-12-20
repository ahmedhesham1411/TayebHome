package com.uriallab.haat;

import android.app.Activity;
import android.util.Log;

import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import com.loopj.android.http.TextHttpResponseHandler;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.Utilities.Dialogs;
import com.uriallab.haat.haat.Utilities.LoadingDialog;
import com.uriallab.haat.haat.Utilities.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

public class CancelReasonViewModel {
    public ObservableInt rotation = new ObservableInt(0);

    public ObservableField<String> orderNumber = new ObservableField<>("");
    public ObservableField<String> reason = new ObservableField<>("");
    public ObservableField<String> reasonError = new ObservableField<>();
    public ObservableField<String> orderNumberError = new ObservableField<>();

    private Activity activity;

    public CancelReasonViewModel(Activity activity) {
        this.activity = activity;

        if (ConfigurationFile.getCurrentLanguage(activity).equals("ar")) {
            rotation.set(180);
        }
    }

    public void cancelRequest() {

        orderNumberError.set(null);
        reasonError.set(null);

        if (orderNumber.get().equals("") || reason.get().isEmpty()) {

            if (orderNumber.get().isEmpty()){
                orderNumberError.set(activity.getString(R.string.enter_order_num));
            }

            if (reason.get().isEmpty()){
                reasonError.set(activity.getString(R.string.enter_cancel_reason));
            }

        } else {
            final LoadingDialog loadingDialog = new LoadingDialog();

            JSONObject jsonParams = new JSONObject();
            try {
                jsonParams.put("OrderUID", orderNumber.get());
                jsonParams.put("Ord_DriverCanslationReason", reason.get());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            APIModel.postMethod(activity, "Driver/RemoveOrderWithReason", jsonParams, new TextHttpResponseHandler() {
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
                            APIModel.handleFailure(activity, statusCode, responseString, () -> cancelRequest());
                            break;
                    }
                }

                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                    Log.e("response", responseString);
                    try {
                        JSONObject jsonObject = new JSONObject(responseString);
                        if (jsonObject.has("result"))
                            Utilities.toastySuccess(activity, jsonObject.getJSONObject("result").getString("Message"));
                        else
                            Utilities.toastySuccess(activity, responseString + "    ");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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

    public void back() {
        activity.finish();
    }
}