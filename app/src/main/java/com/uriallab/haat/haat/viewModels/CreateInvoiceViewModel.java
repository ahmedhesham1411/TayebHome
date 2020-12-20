package com.uriallab.haat.haat.viewModels;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import com.loopj.android.http.TextHttpResponseHandler;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.Utilities.Dialogs;
import com.uriallab.haat.haat.Utilities.LoadingDialog;
import com.uriallab.haat.haat.Utilities.Utilities;
import com.uriallab.haat.haat.Utilities.camera.Camera;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateInvoiceViewModel {

    public ObservableInt rotation = new ObservableInt(0);

    public ObservableField<String> amountObservable = new ObservableField<>("");
    public ObservableField<String> amountError = new ObservableField<>();

    public ObservableField<String> price = new ObservableField<>();

    public ObservableField<String> totalPrice = new ObservableField<>();

    private double priceT;

    private String orderId;

    private Activity activity;

    public CreateInvoiceViewModel(Activity activity, double priceT, String orderId) {
        this.activity = activity;
        this.priceT = priceT;
        this.orderId = orderId;

        price.set(Utilities.roundPrice(priceT) + " " + activity.getString(R.string.currency));

        totalPrice.set(Utilities.roundPrice(priceT) + " " + activity.getString(R.string.currency));

        if (ConfigurationFile.getCurrentLanguage(activity).equals("ar")) {
            rotation.set(180);
        }
    }

    public void sendInvoice() {

        SendInvoiceRequest();

    }

    private void SendInvoiceRequest() {
        final LoadingDialog loadingDialog = new LoadingDialog();

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("OrderUID", orderId);
            jsonParams.put("Ord_Items_Price", amountObservable.get());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        APIModel.postMethod(activity, "Driver/OutInvoiceOrder", jsonParams, new TextHttpResponseHandler() {
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
                                SendInvoiceRequest();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);
                amountObservable.set("");
                totalPrice.set(price.get());
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



    public void finishOrder() {

        Dialog dialogCall = new Dialog(activity);
        dialogCall.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogCall.setContentView(R.layout.custom_alert_dialog_call);
        dialogCall.setCanceledOnTouchOutside(false);
        dialogCall.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView alert_text = dialogCall.findViewById(R.id.alert_text);
        TextView refuseClick = dialogCall.findViewById(R.id.no_id);
        TextView confirmClick = dialogCall.findViewById(R.id.yes_id);

        refuseClick.setText(activity.getString(R.string.back_));
        confirmClick.setText(activity.getString(R.string.confirm_order));

        alert_text.setText(activity.getString(R.string.call_instructions_));

        refuseClick.setOnClickListener(v -> dialogCall.dismiss());
        confirmClick.setOnClickListener(v -> {
            finishRequest();
            dialogCall.dismiss();
        });
        dialogCall.show();
    }

    private void finishRequest(){
        final LoadingDialog loadingDialog = new LoadingDialog();

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("OrderUID", orderId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        APIModel.postMethod(activity, "Driver/OrderRecived", jsonParams, new TextHttpResponseHandler() {
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
                                finishRequest();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);
                Utilities.toastySuccess(activity, activity.getString(R.string.finished_successfully));
                Intent returnIntent = new Intent();
                returnIntent.putExtra("sent", true);
                activity.setResult(Activity.RESULT_OK, returnIntent);
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

    public TextWatcher getSearchText() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {

                double tPrice = 0;
                try {
                    if (editable.length() == 0) {
                        amountObservable.set(0 + " " + activity.getString(R.string.currency));
                        tPrice = 0 + priceT;
                    } else {
                        amountObservable.set(editable.toString());
                        tPrice = Double.parseDouble(editable.toString()) + priceT;
                    }

                    totalPrice.set(Utilities.roundPrice(tPrice) + " " + activity.getString(R.string.currency));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
    }

    public void back() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("sent", false);
        activity.setResult(Activity.RESULT_OK, returnIntent);
        activity.finish();
    }
}

   /* public ObservableInt rotation = new ObservableInt(0);

    public ObservableField<String> amountObservable = new ObservableField<>("");
    public ObservableField<String> amountError = new ObservableField<>();

    public ObservableField<String> price = new ObservableField<>();

    public ObservableField<String> totalPrice = new ObservableField<>();

    public ObservableField<String> imgObservable = new ObservableField<>("");

    private double priceT;

    private String orderId;

    private Activity activity;

    public CreateInvoiceViewModel(Activity activity, double priceT, String orderId) {
        this.activity = activity;
        this.priceT = priceT;
        this.orderId = orderId;

        price.set(Utilities.roundPrice(priceT) + " " + activity.getString(R.string.currency));

        if (ConfigurationFile.getCurrentLanguage(activity).equals("ar")) {
            rotation.set(180);
        }
    }

    public void sendInvoice() {

        if (validate())
            SendInvoiceRequest();
    }

    private void SendInvoiceRequest() {
        final LoadingDialog loadingDialog = new LoadingDialog();

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("OrderUID", orderId);
            jsonParams.put("Ord_Items_Price", amountObservable.get());
            jsonParams.put("Ord_Items_Inv_Url", imgObservable.get());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        APIModel.postMethod(activity, "Driver/OutInvoiceOrder", jsonParams, new TextHttpResponseHandler() {
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
                                SendInvoiceRequest();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("sent", true);
                activity.setResult(Activity.RESULT_OK, returnIntent);
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

    private boolean validate() {

        Utilities.hideKeyboard(activity);

        if (amountObservable.get().isEmpty()) {
            amountError.set(activity.getResources().getString(R.string.required));
            return false;
        }

        if (imgObservable.get().isEmpty()) {
            Utilities.toastyRequiredFieldCustom(activity, activity.getString(R.string.enter_bill_image));
            return false;
        }

        return true;
    }

    public void getPhoto() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(activity,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(activity,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(activity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        }, 3);
            } else {
                Camera.getPhotoFromCamera2(activity);
            }
        } else {
            Camera.getPhotoFromCamera2(activity);
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
            public void afterTextChanged(Editable editable) {

                double tPrice = 0;
                try {
                    if (editable.length() == 0){
                        amountObservable.set(0 + " " + activity.getString(R.string.currency));
                        tPrice = 0 + priceT;
                    }
                    else{
                        amountObservable.set(editable.toString());
                        tPrice = Double.parseDouble(editable.toString()) + priceT;
                    }

                    totalPrice.set(Utilities.roundPrice(tPrice) + " " + activity.getString(R.string.currency));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
    }

    public void back() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("sent", false);
        activity.setResult(Activity.RESULT_OK, returnIntent);
        activity.finish();
    }
}*/