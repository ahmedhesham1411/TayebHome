package com.uriallab.haat.haat.viewModels;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;

import com.loopj.android.http.TextHttpResponseHandler;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.SharedPreferences.LoginSession;
import com.uriallab.haat.haat.UI.Activities.makeOrder.DelivieringLocationActivity;
import com.uriallab.haat.haat.Utilities.Dialogs;
import com.uriallab.haat.haat.Utilities.LoadingDialog;
import com.uriallab.haat.haat.Utilities.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

public class DeliveringLocationViewModel {

    public ObservableBoolean isFav = new ObservableBoolean(false);

    public ObservableInt rotate = new ObservableInt(0);

    private DelivieringLocationActivity activity;

    public DeliveringLocationViewModel(DelivieringLocationActivity activity) {
        this.activity = activity;

        if (ConfigurationFile.getCurrentLanguage(activity).equals("ar"))
            rotate.set(180);
    }

    public void addToFav() {
        if (LoginSession.isLoggedIn(activity)){
            final LoadingDialog loadingDialog = new LoadingDialog();
            JSONObject jsonParams = new JSONObject();
            try {
                jsonParams.put("Location_Lat", activity.latLngTo.latitude);
                jsonParams.put("Location_Lng", activity.latLngTo.longitude);
                jsonParams.put("Location_Nm", activity.binding.locationTxt.getText().toString());
                if (activity.binding.additionalTxt.getText().toString().isEmpty())
                    jsonParams.put("Favorite_Nm", activity.binding.locationTxt.getText().toString().substring(0, 10));
                else
                    jsonParams.put("Favorite_Nm", activity.binding.additionalTxt.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            APIModel.postMethod(activity, "Client/SetFavoriteLocations", jsonParams, new TextHttpResponseHandler() {
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
                        case 500:
                            try {
                                JSONObject jsonObject = new JSONObject(responseString);
                                if (jsonObject.has("error"))
                                    Utilities.toastyError(activity, jsonObject.getJSONObject("error").getString("Message"));
                                else
                                    Utilities.toastyError(activity, responseString + "  ");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 403:
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
                                    addToFav();
                                }
                            });
                            break;
                    }
                }

                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                    Log.e("response", responseString);

                    isFav.set(true);
                    Utilities.toastySuccess(activity, activity.getString(R.string.added_successfully));
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
        }else {
            Dialogs.showLoginDialog(activity);
        }


    }

    public void confirmLocation() {
        activity.confirmLocation();
    }

    public void back() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("address", "none");
        activity.setResult(Activity.RESULT_OK, returnIntent);
        activity.finish();
    }
}