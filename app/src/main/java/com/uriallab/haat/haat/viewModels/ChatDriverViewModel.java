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
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;

import com.eugeneek.smilebar.SmileBar;
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
import com.uriallab.haat.haat.UI.Activities.ChatDriverActivity;
import com.uriallab.haat.haat.UI.Activities.CreateInvoiceActivity;
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

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatDriverViewModel extends ViewModel {

    public ObservableField<String> messageObservable = new ObservableField<>("");

    public ObservableField<String> orderNumber = new ObservableField<>();

    public ObservableInt iconsStatus = new ObservableInt(0);

    private String orderId, chatId, phoneNumber, userName, userImgUrl;

    private ChatDriverActivity activity;

    private double price = 0;

    private Dialog dialog;

    public String userId;

    public ChatDriverViewModel(ChatDriverActivity activity, String orderId) {
        this.activity = activity;
        this.orderId = orderId;

        orderNumber.set(activity.getString(R.string.order_number) + " \n" + orderId);

        if (ConfigurationFile.getCurrentLanguage(activity).equals("ar")) {
            activity.binding.backImg.setRotation(180);
            activity.binding.starBar.setRotationY(180);
        }

        getChat(true);
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

    public void getChat(boolean isFirst) {

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

                Picasso.get().load(APIModel.BASE_URL + data.getResult().getUserData().getUser_ImgUrl()).placeholder(R.drawable.user_img).into(activity.binding.orderImg);
                activity.binding.driverName.setText(data.getResult().getUserData().getUser_Full_Nm());
                activity.binding.ratesNumberVal.setText(String.valueOf(data.getResult().getUserData().getUser_Count_Rate()));
                activity.binding.starBar.setRating(data.getResult().getUserData().getUser_Rate());

                price = data.getResult().getOrder().getFinal_Amt();

                userId = String.valueOf(data.getResult().getUserData().getUserUID());
                userName = String.valueOf(data.getResult().getUserData().getUser_Full_Nm());
                userImgUrl = String.valueOf(data.getResult().getUserData().getUser_ImgUrl());
                phoneNumber = data.getResult().getUserData().getUser_Phone();

                iconsStatus.set(data.getResult().getOrder().getOrd_Driver_StatusID());

                Log.e("iconsStatus", iconsStatus.get() + "");

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

    public void sendReport() {
        activity.sendReport();
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

    public void bill() {
        Bundle bundle = new Bundle();
        bundle.putDouble("price", price);
        bundle.putString("orderId", orderId);
        IntentClass.goToStartForResult(activity, CreateInvoiceActivity.class, 147, bundle);
    }

    public void cameraClick() {
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

    public void finishOrder() {
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
                                finishOrder();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);
                Utilities.toastySuccess(activity, activity.getString(R.string.finished_successfully));
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

    public void sendOrderImg(final String img) {
        final LoadingDialog loadingDialog = new LoadingDialog();

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("ChatID", chatId);
            jsonParams.put("Order_ID", orderId);
            jsonParams.put("Mssge_Text", "");
            jsonParams.put("Mssge_ImgUrl", img);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        APIModel.postMethod(activity, "Driver/SendOrderImg", jsonParams, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                iconsStatus.set(2);
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
                                sendOrderImg(img);
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);
                Utilities.toastySuccess(activity, activity.getString(R.string.sent_successfully));
                iconsStatus.set(3);
                getChat(false);
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
                Camera.showGalleryFromActivity3(activity);
            }
        } else {
            Camera.showGalleryFromActivity3(activity);
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