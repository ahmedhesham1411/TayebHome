package com.uriallab.haat.haat.viewModels;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eugeneek.smilebar.SmileBar;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;
import com.squareup.picasso.Picasso;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.API.ApiClient;
import com.uriallab.haat.haat.API.ApiInterface;
import com.uriallab.haat.haat.DataModels.ChatModel;
import com.uriallab.haat.haat.DataModels.MessageResponseModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.UI.Activities.ChatActivity;
import com.uriallab.haat.haat.UI.Activities.PaymentGateActivity;
import com.uriallab.haat.haat.UI.Activities.TrackDriverActivity;
import com.uriallab.haat.haat.UI.Adapters.CancelReasonAdapter;
import com.uriallab.haat.haat.UI.CancelationReason;
import com.uriallab.haat.haat.Utilities.Dialogs;
import com.uriallab.haat.haat.Utilities.IntentClass;
import com.uriallab.haat.haat.Utilities.LoadingDialog;
import com.uriallab.haat.haat.Utilities.Utilities;
import com.uriallab.haat.haat.Utilities.camera.Camera;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatViewModel extends ViewModel {

    public ObservableBoolean isTracking = new ObservableBoolean(true);

    public ObservableBoolean isRecieved = new ObservableBoolean(false);

    public ObservableField<String> messageObservable = new ObservableField<>("");

    public ObservableField<String> orderNumber = new ObservableField<>();

    private String orderId, chatId, phoneNumber, userName, checkoutId, userImgUrl;

    private ChatActivity activity;

    private double price = 0;

    private int paymentType = 1;

    private Dialog dialog;

    public String userId;

    private LatLng clientLatLng, storeLatLng, driverLatLng;

    private List<CancelationReason.ResultBean.ReasonsBean> cancelReasonList = new ArrayList<>();

    public ObservableInt reasonId = new ObservableInt(0);

    public ChatViewModel(ChatActivity activity, String orderId) {
        this.activity = activity;
        this.orderId = orderId;

        orderNumber.set(activity.getString(R.string.order_number) + " \n" + orderId);

        if (ConfigurationFile.getCurrentLanguage(activity).equals("ar")) {
            activity.binding.backImg.setRotation(180);
            activity.binding.starBar.setRotationY(180);
        }

        getChat(true);

        getCancelReasons();
    }

    public void sendMessageAction() {
        Utilities.hideKeyboard(activity);

        if (!messageObservable.get().equals("")) {
            sendNewMessage(null, 1);
            activity.sendMessage(false);
        }
    }

    public void sendNewMessage(File file, int type) {

        //final LoadingDialog loadingDialog = new LoadingDialog();
        //Dialogs.showLoading(activity, loadingDialog);

        RequestBody chatIdBody = RequestBody.create(MediaType.parse("text/plain"), chatId);
        RequestBody orderIdBody = RequestBody.create(MediaType.parse("text/plain"), orderId);

        ApiInterface apiInterface = ApiClient.getApiClient(activity);
        Call<MessageResponseModel> call;
        if (type == 1) {
            RequestBody messageBody = RequestBody.create(MediaType.parse("text/plain"), messageObservable.get());
            RequestBody typeBody = RequestBody.create(MediaType.parse("text/plain"), "1");
            call = apiInterface.sendMessageTxt(chatIdBody, orderIdBody, typeBody, messageBody);
            Log.e("response", chatIdBody.toString() + "\t" + orderIdBody.toString() + "\t" + typeBody.toString() + "\t" + messageBody.toString());
        } else if (type == 2) {
            RequestBody messageBody = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("message", file.getPath(), messageBody);
            RequestBody typeBody = RequestBody.create(MediaType.parse("text/plain"), "2");
            call = apiInterface.sendMessageFile(chatIdBody, orderIdBody, typeBody, filePart);
            Log.e("response", chatIdBody.toString() + "\t" + orderIdBody + "\t" + typeBody + "\t" + filePart);
        } else {
            RequestBody messageBody = RequestBody.create(MediaType.parse("audio"), file);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("message", file.getPath(), messageBody);
            RequestBody typeBody = RequestBody.create(MediaType.parse("text/plain"), "3");
            call = apiInterface.sendMessageFile(chatIdBody, orderIdBody, typeBody, filePart);
            Log.e("response", chatIdBody.toString() + "\t" + orderIdBody + "\t" + typeBody + "\t" + filePart);
        }

        Log.e("response", APIModel.BASE_URL + "Order/SendMessage");

        messageObservable.set("");

        call.enqueue(new Callback<MessageResponseModel>() {
            @Override
            public void onResponse(Call<MessageResponseModel> call, Response<MessageResponseModel> response) {
                //Dialogs.dismissLoading(loadingDialog);
                if (response.code() == 200) {
                    Log.e("response", response.body().getResult().getMessage());
                    getChat(false);
                } else {
                    try {
                        Log.e("response", response.code() + " " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<MessageResponseModel> call, Throwable t) {
                //Dialogs.dismissLoading(loadingDialog);
                Utilities.toastyError(activity, activity.getString(R.string.something_wrong));
                Log.e("response", t.getMessage() + "\t" + t.getLocalizedMessage() + " onFailure");
            }
        });
    }

    private void getChat(boolean isFirst) {
        final LoadingDialog loadingDialog = new LoadingDialog();
        APIModel.getMethod(activity, "Order/GetChat?orderId=" + orderId, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                switch (statusCode) {
                    case 400:
                        try {
                            // Utilities.toastyError(activity, responseString + "    ");

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
                                Utilities.toastyError(activity, responseString + "");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                            @Override
                            public void onRefresh() {
                                getChat(isFirst);
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);

                Type dataType = new TypeToken<ChatModel>() {
                }.getType();
                ChatModel data = new Gson().fromJson(responseString, dataType);

                try {
                    chatId = String.valueOf(data.getResult().getMessages().get(0).getChatID());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (data.getResult().getOrder().getOrd_Driver_StatusID() == 2 &&
                        !data.getResult().getOrder().isOrd_Payment_Status())
                    isRecieved.set(true);

                Picasso.get().load(APIModel.BASE_URL + data.getResult().getUserData().getUser_ImgUrl()).placeholder(R.drawable.user_img).into(activity.binding.orderImg);
                activity.binding.driverName.setText(data.getResult().getUserData().getUser_Full_Nm());
                activity.binding.ratesNumberVal.setText(String.valueOf(data.getResult().getUserData().getUser_Count_Rate()));
                activity.binding.starBar.setRating(data.getResult().getUserData().getUser_Rate());

                price = data.getResult().getOrder().getFinal_Amt();

                userId = String.valueOf(data.getResult().getUserData().getUserUID());
                userName = String.valueOf(data.getResult().getUserData().getUser_Full_Nm());
                userImgUrl = String.valueOf(data.getResult().getUserData().getUser_ImgUrl());
                phoneNumber = data.getResult().getUserData().getUser_Phone();

                try {
                    storeLatLng = new LatLng(Double.parseDouble(data.getResult().getOrder().getShop_Lat()),
                            Double.parseDouble(data.getResult().getOrder().getShop_Lng()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    clientLatLng = new LatLng(Double.parseDouble(data.getResult().getOrder().getClient_Lat()),
                            Double.parseDouble(data.getResult().getOrder().getClient_Lng()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    driverLatLng = new LatLng(Double.parseDouble(data.getResult().getOrder().getOrd_Driver_Lat()),
                            Double.parseDouble(data.getResult().getOrder().getOrd_Driver_Long()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                activity.initRecycler(data.getResult().getMessages());

            }

            @Override
            public void onStart() {
                super.onStart();
                if (isFirst)
                    Dialogs.showLoading(activity, loadingDialog);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                if (isFirst)
                    Dialogs.dismissLoading(loadingDialog);
            }
        });
    }

    private void getCancelReasons() {
        //final LoadingDialog loadingDialog = new LoadingDialog();
        APIModel.getMethod(activity, "Client/CancellationReasons", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                switch (statusCode) {
                    case 400:
                        try {
                            // Utilities.toastyError(activity, responseString + "    ");

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
                                Utilities.toastyError(activity, responseString + "");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                            @Override
                            public void onRefresh() {
                                getCancelReasons();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);

                Type dataType = new TypeToken<CancelationReason>() {
                }.getType();
                CancelationReason data = new Gson().fromJson(responseString, dataType);

                cancelReasonList.addAll(data.getResult().getReasons());
            }

            @Override
            public void onStart() {
                super.onStart();
                // Dialogs.showLoading(activity, loadingDialog);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                // Dialogs.dismissLoading(loadingDialog);
            }
        });
    }

    public void sendReport() {
        activity.sendReport();
    }

    public void cancelOrder() {
        Dialog dialogCall = new Dialog(activity);
        dialogCall.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogCall.setContentView(R.layout.custom_alert_dialog_cancel_order);
        dialogCall.setCanceledOnTouchOutside(false);
        dialogCall.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView refuseClick = dialogCall.findViewById(R.id.no_id);
        TextView confirmClick = dialogCall.findViewById(R.id.yes_id);
        RecyclerView category_recycler = dialogCall.findViewById(R.id.category_recycler);

        CancelReasonAdapter cancelReasonAdapter = new CancelReasonAdapter(cancelReasonList, reasonId);

        category_recycler.setLayoutManager(new LinearLayoutManager(activity));
        category_recycler.setAdapter(cancelReasonAdapter);

        refuseClick.setOnClickListener(v -> dialogCall.dismiss());
        confirmClick.setOnClickListener(v -> {
            if (reasonId.get() == 0) {
                Utilities.toastyError(activity, activity.getString(R.string.please_select_the_reason));
            } else {
                removeForReason(reasonId.get());
                dialogCall.dismiss();
            }
        });
        dialogCall.show();
    }

    public void callUser() {
        Dialog dialogCall = new Dialog(activity);
        dialogCall.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogCall.setContentView(R.layout.custom_alert_dialog_call);
        dialogCall.setCanceledOnTouchOutside(false);
        dialogCall.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView alert_text = dialogCall.findViewById(R.id.alert_text);
        TextView refuseClick = dialogCall.findViewById(R.id.no_id);
        TextView confirmClick = dialogCall.findViewById(R.id.yes_id);

        refuseClick.setText(activity.getString(R.string.cancel));
        confirmClick.setText(activity.getString(R.string.call));

        alert_text.setText(activity.getString(R.string.call_instructions));

        refuseClick.setOnClickListener(v -> dialogCall.dismiss());
        confirmClick.setOnClickListener(v -> {
            IntentClass.goTodialPhoneNumber(activity, phoneNumber);
            dialogCall.dismiss();
        });
        dialogCall.show();
    }

    public void track() {
        Bundle bundle = new Bundle();
        bundle.putString("driverID", userId);
        bundle.putDouble("storeLat", storeLatLng.latitude);
        bundle.putDouble("storeLng", storeLatLng.longitude);
        bundle.putDouble("clientLat", clientLatLng.latitude);
        bundle.putDouble("clientLng", clientLatLng.longitude);
        bundle.putDouble("driverLat", driverLatLng.latitude);
        bundle.putDouble("driverLng", driverLatLng.longitude);
        IntentClass.goToActivity(activity, TrackDriverActivity.class, bundle);
    }

    public void recieve() {
        isCash();
    }

    public void rate() {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_alert_dialog_rate);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView user_name = dialog.findViewById(R.id.user_name);
        final SmileBar starBar = dialog.findViewById(R.id.starBar);
        final EditText comment_edt = dialog.findViewById(R.id.comment_edt);
        Button rate_btn = dialog.findViewById(R.id.rate_btn);
        CircleImageView userImg = dialog.findViewById(R.id.user_img);

        if (ConfigurationFile.getCurrentLanguage(activity).equals("ar"))
            starBar.setRotationY(180);

        user_name.setText(userName);

        Picasso.get().load(APIModel.BASE_URL + userImgUrl).placeholder(R.drawable.profile).into(userImg);

        rate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRate(comment_edt.getText().toString(), starBar.getRating());
            }
        });
        dialog.show();
    }

    private void isCash() {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_alert_dialog_is_cash);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final TextView cashId = dialog.findViewById(R.id.cash_id);
        final TextView visaId = dialog.findViewById(R.id.visa_id);

        if (ConfigurationFile.getCurrentLanguage(activity).equals("ar")) {
            cashId.setBackgroundResource(R.drawable.shape_rounded_blue_left);
            visaId.setBackgroundResource(R.drawable.shape_grey_stroke_blue_left);
        } else {
            cashId.setBackgroundResource(R.drawable.shape_rounded_blue_right);
            visaId.setBackgroundResource(R.drawable.shape_grey_stroke_blue_right);
        }

        cashId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                paymentType = 1;

                cashId.setTextColor(activity.getResources().getColor(R.color.colorWhite));
                visaId.setTextColor(activity.getResources().getColor(R.color.colorBlue));

                if (ConfigurationFile.getCurrentLanguage(activity).equals("ar")) {
                    cashId.setBackgroundResource(R.drawable.shape_rounded_blue_left);
                    visaId.setBackgroundResource(R.drawable.shape_grey_stroke_blue_left);
                } else {
                    cashId.setBackgroundResource(R.drawable.shape_rounded_blue_right);
                    visaId.setBackgroundResource(R.drawable.shape_grey_stroke_blue_right);
                }
            }
        });

        visaId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                paymentType = 2;

                visaId.setTextColor(activity.getResources().getColor(R.color.colorWhite));
                cashId.setTextColor(activity.getResources().getColor(R.color.colorBlue));

                if (ConfigurationFile.getCurrentLanguage(activity).equals("ar")) {
                    cashId.setBackgroundResource(R.drawable.shape_grey_stroke_blue_right);
                    visaId.setBackgroundResource(R.drawable.shape_rounded_blue_right);
                } else {
                    cashId.setBackgroundResource(R.drawable.shape_grey_stroke_blue_left);
                    visaId.setBackgroundResource(R.drawable.shape_rounded_blue_left);
                }
            }
        });


        Button payBtn = dialog.findViewById(R.id.pay_btn);

        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (paymentType == 1)
                    payWithCash();
                else {
                    Bundle bundle = new Bundle();
                    bundle.putDouble("money", price);
                    bundle.putString("orderID", orderId);
                    bundle.putInt("type", 1);
                    IntentClass.goToStartForResult(activity, PaymentGateActivity.class, 103, bundle);
                }
            }
        });
        dialog.show();
    }

    private void payWithCash() {
        final LoadingDialog loadingDialog = new LoadingDialog();

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("orderUID", orderId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        APIModel.postMethod(activity, "Payment/Cash", jsonParams, new TextHttpResponseHandler() {
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
                                payWithCash();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);
                Utilities.toastySuccess(activity, activity.getString(R.string.pay_successfully));
                rate();
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

    private void removeForReason(int id) {
        final LoadingDialog loadingDialog = new LoadingDialog();

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("OrderUID", orderId);
            jsonParams.put("Ord_ClientCanslationReasonID", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        APIModel.postMethod(activity, "Client/RemoveOrderWithReason", jsonParams, new TextHttpResponseHandler() {
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
                        APIModel.handleFailure(activity, statusCode, responseString, () -> removeForReason(id));
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

    private void sendRate(final String comment, final int rate) {
        final LoadingDialog loadingDialog = new LoadingDialog();

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("Comment_Desc", comment);
            jsonParams.put("Comment_Rate", rate);
            jsonParams.put("Rate_To_UserID", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        APIModel.postMethod(activity, "Setting/PostComment", jsonParams, new TextHttpResponseHandler() {
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
                                sendRate(comment, rate);
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);
                Utilities.toastySuccess(activity, activity.getString(R.string.rated_successfully));
                dialog.dismiss();
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
                Camera.showGalleryFromActivity2(activity);
            }
        } else {
            Camera.showGalleryFromActivity2(activity);
        }
    }

    public TextWatcher getMessageText() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(final CharSequence charSequence, int i, int i1, int i2) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (charSequence.length() > 0)
                            activity.sendMessage(true);
                        else
                            activity.sendMessage(false);
                    }
                }, 1000);
            }

            @Override
            public void afterTextChanged(final Editable editable) {

            }
        };
    }

    public void back() {
        activity.sendMessage(false);
        Utilities.hideKeyboard(activity);
        activity.finish();
    }
}