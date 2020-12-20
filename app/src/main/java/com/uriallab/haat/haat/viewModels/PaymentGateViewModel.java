package com.uriallab.haat.haat.viewModels;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import com.loopj.android.http.TextHttpResponseHandler;
import com.oppwa.mobile.connect.exception.PaymentException;
import com.oppwa.mobile.connect.payment.PaymentParams;
import com.oppwa.mobile.connect.payment.card.CardPaymentParams;
import com.oppwa.mobile.connect.provider.Transaction;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.UI.Activities.PaymentGateActivity;
import com.uriallab.haat.haat.UI.Activities.ScanCriedtCardActivity;
import com.uriallab.haat.haat.UI.MainActivity;
import com.uriallab.haat.haat.Utilities.Dialogs;
import com.uriallab.haat.haat.Utilities.IntentClass;
import com.uriallab.haat.haat.Utilities.LoadingDialog;
import com.uriallab.haat.haat.Utilities.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class PaymentGateViewModel {

    public ObservableBoolean isSTCPAY = new ObservableBoolean(true);
    public ObservableBoolean isQr = new ObservableBoolean(false);

    public ObservableField<String> moneyObservable = new ObservableField<>();

    public ObservableField<String> phoneNumber = new ObservableField<>("");
    public ObservableField<String> cardNumber = new ObservableField<>("");
    public ObservableField<String> expiryMonth = new ObservableField<>("");
    public ObservableField<String> expiryYear = new ObservableField<>("");
    public ObservableField<String> passwordCode = new ObservableField<>("");
    public ObservableField<String> cardName = new ObservableField<>("");

    private String cardType = "STC_PAY";

    public ObservableInt rotation = new ObservableInt(0);

    private String orderId;

    private String checkoutId;

    Transaction transaction = null;

    private int type, hatCardId, hatCardQuantity;

    private double money;

    private PaymentGateActivity activity;

    public PaymentGateViewModel(PaymentGateActivity activity, String orderId, int type, double money, int hatCardId, int hatCardQuantity) {
        this.activity = activity;
        this.orderId = orderId;
        this.type = type;
        this.hatCardId = hatCardId;
        this.hatCardQuantity = hatCardQuantity;
        this.money = Utilities.round(money, 2);

        moneyObservable.set(Utilities.roundPrice(money) + " " + activity.getString(R.string.currency));

        if (ConfigurationFile.getCurrentLanguage(activity).equals("ar")) {
            rotation.set(180);
        }
    }

    public void pay() {

        if (isSTCPAY.get()) {
            if (isQr.get()) {
                createCheckoutId(true);
            } else {
                if (phoneNumber.get().isEmpty()) {
                    Utilities.toastyError(activity, activity.getString(R.string.enter_phone_number));
                } else {
                    createCheckoutId(true);
                }
            }

        } else {
            if (cardNumber.get().isEmpty() || expiryMonth.get().isEmpty() || expiryYear.get().isEmpty() ||
                    passwordCode.get().isEmpty() || cardName.get().isEmpty()) {

                Utilities.toastyError(activity, activity.getString(R.string.complete_data));

            } else {

                createCheckoutId(false);

            }
        }
    }

    public void createCheckoutId(final boolean isSTC) {
        final LoadingDialog loadingDialog = new LoadingDialog();

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("amount", Utilities.roundPrice(money));
            jsonParams.put("orderUID", orderId);
            jsonParams.put("card_id", hatCardId);
            jsonParams.put("card_quantity", hatCardQuantity);
            if (isSTC) {
                if (isQr.get()) {
                    jsonParams.put("mode", "qr_code");
                } else {
                    jsonParams.put("mode", "mobile");
                    jsonParams.put("mobile", phoneNumber.get());
                }
            } else
                jsonParams.put("method_type", cardType);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        APIModel.postMethod(activity, "Payment/Checkout", jsonParams, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                switch (statusCode) {
                    case 400:
                        try {
                            JSONObject jsonObject = new JSONObject(responseString);
                            if (jsonObject.getJSONObject("error").has("Message"))
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
                            if (jsonObject.getJSONObject("error").has("Message"))
                                Utilities.toastyError(activity, jsonObject.getJSONObject("error").getString("Message"));
                            else
                                Utilities.toastyError(activity, responseString + "");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                            @Override
                            public void onRefresh() {
                                createCheckoutId(isSTC);
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.e("response", responseString);
                try {
                    JSONObject myJson = new JSONObject(responseString);
                    checkoutId = myJson.getJSONObject("result").getString("message");
                    PaymentParams paymentParams = null;
                    try {
                        if (isSTC) {
                            paymentParams = new PaymentParams(
                                    checkoutId,
                                    "STC_PAY"
                            );
                            Log.e("paymentParams", checkoutId + "\n" + "STC_PAY");
                        } else {
                            String expireYear = "20" + expiryYear.get();
                            String cardFinalNumber = cardNumber.get().replace("-", "");
                            Log.e("CARD_NUMBER", cardFinalNumber + "\t" + cardNumber.get());
                            paymentParams = new CardPaymentParams(
                                    checkoutId,
                                    cardType,
                                    cardFinalNumber,
                                    cardName.get(),
                                    expiryMonth.get(),
                                    expireYear,
                                    passwordCode.get()
                            );
                            Log.e("paymentParams", checkoutId + "\n" + cardType + "\t" + cardFinalNumber + "\n" + cardName.get() + "\t" + expiryMonth.get() + "\t" + expireYear);
                        }

                        // Set shopper result URL
                        paymentParams.setShopperResultUrl("haat://result");

                    } catch (PaymentException e) {
                        e.printStackTrace();
                    }

                    try {
                        transaction = new Transaction(paymentParams);
                        activity.binder.submitTransaction(transaction);
                    } catch (PaymentException ee) {
                        /* error occurred */
                    }
                } catch (JSONException e) {
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

    public void checkPayStatus() {
        final LoadingDialog loadingDialog = new LoadingDialog();
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("id", checkoutId);
            if (type != 2) {
                jsonParams.put("orderUID", orderId);
                jsonParams.put("method_type", cardType);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = "Payment/OrderStatus";
        if (type == 2) {
            try {
                jsonParams.put("amount", Utilities.roundPrice(money));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            url = "Payment/CreditStatus";
        } else if (type == 3) {
            url = "Payment/HatCardStatus";
            try {
                jsonParams.put("card_id", hatCardId);
                jsonParams.put("card_quantity", hatCardQuantity);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        APIModel.postMethod(activity, url, jsonParams, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "\t" + statusCode + " Error");
                switch (statusCode) {
                    case 400:
                        try {
                            JSONObject jsonObject = new JSONObject(responseString);
                            if (jsonObject.getJSONObject("error").has("Message"))
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
                            if (jsonObject.getJSONObject("error").has("Message"))
                                Utilities.toastyError(activity, jsonObject.getJSONObject("error").getString("Message"));
                            else
                                Utilities.toastyError(activity, responseString + "");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                            @Override
                            public void onRefresh() {
                                checkPayStatus();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.e("response", responseString);
                try {
                    JSONObject myJson = new JSONObject(responseString);

                    Log.e("response", "true");
                    Utilities.toastySuccess(activity, myJson.getJSONObject("result").getString("Message"));

                    if (type == 3) {
                        Intent intent = new Intent(activity, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("key", "13");
                        activity.startActivity(intent);
                    } else {
                        Utilities.hideKeyboard(activity);
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("isPayed", true);
                        activity.setResult(Activity.RESULT_OK, returnIntent);
                        activity.finish();
                    }

                } catch (
                        JSONException e) {
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

    public void choosePaymentMethod(int type) {
        if (type == 1) {
            cardType = "STC_PAY";
            isSTCPAY.set(true);
        } else {
            cardType = "MADA";
            isSTCPAY.set(false);
            activity.binding.cardPaymentType.setBackgroundResource(R.drawable.mada);
            activity.binding.paymentType2.setBackgroundResource(R.drawable.mada);
        }
    }

    public void chooseCardPaymentMethod(int type) {
        if (type == 1) {
            cardType = "VISA";
            activity.binding.cardPaymentType.setBackgroundResource(R.drawable.visa);
            activity.binding.paymentType2.setBackgroundResource(R.drawable.visa);
        } else if (type == 2) {
            cardType = "MASTER";
            activity.binding.cardPaymentType.setBackgroundResource(R.drawable.mastercard);
            activity.binding.paymentType2.setBackgroundResource(R.drawable.mastercard);
        } else {
            cardType = "MADA";
            activity.binding.cardPaymentType.setBackgroundResource(R.drawable.mada);
            activity.binding.paymentType2.setBackgroundResource(R.drawable.mada);
        }
    }

    public void orderTabClick(int type) {
        if (type == 1) {
            isQr.set(false);
        } else {
            isQr.set(true);
        }
    }

    public void securityCode() {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_alert_security);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView ok = dialog.findViewById(R.id.ok);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void back() {
        Utilities.hideKeyboard(activity);
        activity.finish();
    }


    public void onScanPress() {

        IntentClass.goToActivity(activity, ScanCriedtCardActivity.class, null);

//        Intent scanIntent = new Intent(activity, CardIOActivity.class);
//        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true);
//        scanIntent.putExtra(CardIOActivity.EXTRA_SCAN_EXPIRY, true);
//        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true);
//        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CARDHOLDER_NAME, true);
//        scanIntent.putExtra(CardIOActivity.EXTRA_LANGUAGE_OR_LOCALE, Locale.getDefault());
//        scanIntent.putExtra(CardIOActivity.EXTRA_GUIDE_COLOR, activity.getResources().getColor(R.color.colorAccent));
//        scanIntent.putExtra(CardIOActivity.EXTRA_RETURN_CARD_IMAGE, false);
//        activity.startActivityForResult(scanIntent, 10);
    }
}