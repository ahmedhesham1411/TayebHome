package com.uriallab.haat.haat.viewModels;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.squareup.picasso.Picasso;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.DataModels.ComplaintModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.SharedPreferences.LoginSession;
import com.uriallab.haat.haat.Utilities.Dialogs;
import com.uriallab.haat.haat.Utilities.LoadingDialog;
import com.uriallab.haat.haat.Utilities.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import de.hdodenhof.circleimageview.CircleImageView;

public class ComplaintDetailsViewModel {

    public ObservableBoolean isFinishedObservable = new ObservableBoolean(false);

    public ObservableField<ComplaintModel.ResultBean.ComplaintsBean> itemsBeanObservable = new ObservableField<>();

    public ObservableInt rotation = new ObservableInt(0);

    private Activity activity;

    public ComplaintDetailsViewModel(Activity activity, ComplaintModel.ResultBean.ComplaintsBean itemsBean) {
        this.activity = activity;

        itemsBeanObservable.set(itemsBean);

        //Toast.makeText(activity, String.valueOf(itemsBean.getComplaint_Status_ID()), Toast.LENGTH_SHORT).show();




        if (itemsBean.getComplaint_Status_ID() == 2 )
            isFinishedObservable.set(true);
        else
            isFinishedObservable.set(false);

        if (ConfigurationFile.getCurrentLanguage(activity).equals("ar"))
            rotation.set(180);


    }

    public void complainAction(final int actionType) {
        final LoadingDialog loadingDialog = new LoadingDialog();

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("ComplaintUID", itemsBeanObservable.get().getComplaintUID());
            jsonParams.put("Client_Feadback_Status", actionType);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        APIModel.postMethod(activity, "Setting/ClientFeadbackComplaint", jsonParams, new TextHttpResponseHandler() {
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
                                complainAction(actionType);
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);
                Utilities.toastySuccess(activity, activity.getString(R.string.sent_successfully));
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

    public void back() {
        activity.finish();
    }
}