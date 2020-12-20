package com.uriallab.haat.haat.viewModels;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;
import com.makeramen.roundedimageview.RoundedImageView;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.DataModels.ContactsModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.Utilities.Dialogs;
import com.uriallab.haat.haat.Utilities.IntentClass;
import com.uriallab.haat.haat.Utilities.LoadingDialog;
import com.uriallab.haat.haat.Utilities.Utilities;
import com.uriallab.haat.haat.Utilities.camera.Camera;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;

import androidx.core.app.ActivityCompat;
import androidx.databinding.ObservableInt;

public class ContactUsViewModel {

    public ObservableInt rotation = new ObservableInt(0);

    private Activity activity;

    public RoundedImageView selectedImg;

    private Dialog dialog;

    public String image = "", phone, email, twitter;

    private int type = 1;

    public ContactUsViewModel(Activity activity) {
        this.activity = activity;

        if (ConfigurationFile.getCurrentLanguage(activity).equals("ar")) {
            rotation.set(180);
        }

        getContacts();
    }

    public void call() {
        IntentClass.goTodialPhoneNumber(activity, phone);
    }

    public void whatsapp() {
        IntentClass.goTowhatsApp(activity, phone);
    }

    public void sendMail() {
        IntentClass.sendEmail(activity, email);
    }

    public void twitter() {
        IntentClass.goToLink(activity, twitter);
    }

    public void newTicket() {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_alert_dialog_ticket);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ImageView closeImg = dialog.findViewById(R.id.close_img);

        final TextView askId = dialog.findViewById(R.id.ask_id);
        final TextView complainId = dialog.findViewById(R.id.complain_id);
        final TextView suggestionId = dialog.findViewById(R.id.suggestion_id);
        TextView imgTxt = dialog.findViewById(R.id.img_txt);
        selectedImg = dialog.findViewById(R.id.selected_img);
        final EditText messageEdt = dialog.findViewById(R.id.message_edt);
        final Button addBtn = dialog.findViewById(R.id.add_btn);

        if (ConfigurationFile.getCurrentLanguage(activity).equals("ar")) {
            askId.setBackgroundResource(R.drawable.shape_rounded_blue_left);
            complainId.setBackgroundResource(R.drawable.shape_grey_stroke_blue);
            suggestionId.setBackgroundResource(R.drawable.shape_grey_stroke_blue_left);
            imgTxt.setBackgroundResource(R.drawable.shape_rounded_moov_left);
        } else {
            askId.setBackgroundResource(R.drawable.shape_rounded_blue_right);
            complainId.setBackgroundResource(R.drawable.shape_grey_stroke_blue);
            suggestionId.setBackgroundResource(R.drawable.shape_grey_stroke_blue_right);
            imgTxt.setBackgroundResource(R.drawable.shape_rounded_moov_right);
        }

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utilities.hideKeyboard(activity);

                if (messageEdt.getText().toString().isEmpty())
                    messageEdt.setError(activity.getString(R.string.required));
                else
                    sendMessageRequest(type, messageEdt.getText().toString());
            }
        });

        imgTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                        Camera.showGalleryFromActivity(activity);
                    }
                } else {
                    Camera.showGalleryFromActivity(activity);
                }
            }
        });

        askId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                type = 1;

                askId.setTextColor(activity.getResources().getColor(R.color.colorWhite));
                complainId.setTextColor(activity.getResources().getColor(R.color.colorBlue));
                suggestionId.setTextColor(activity.getResources().getColor(R.color.colorBlue));
                if (ConfigurationFile.getCurrentLanguage(activity).equals("ar")) {
                    askId.setBackgroundResource(R.drawable.shape_rounded_blue_left);
                    complainId.setBackgroundResource(R.drawable.shape_grey_stroke_blue);
                    suggestionId.setBackgroundResource(R.drawable.shape_grey_stroke_blue_left);
                } else {
                    askId.setBackgroundResource(R.drawable.shape_rounded_blue_right);
                    complainId.setBackgroundResource(R.drawable.shape_grey_stroke_blue);
                    suggestionId.setBackgroundResource(R.drawable.shape_grey_stroke_blue_right);
                }
            }
        });

        complainId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                type = 2;

                askId.setTextColor(activity.getResources().getColor(R.color.colorBlue));
                complainId.setTextColor(activity.getResources().getColor(R.color.colorWhite));
                suggestionId.setTextColor(activity.getResources().getColor(R.color.colorBlue));
                if (ConfigurationFile.getCurrentLanguage(activity).equals("ar")) {
                    askId.setBackgroundResource(R.drawable.shape_grey_stroke_blue_right);
                    complainId.setBackgroundColor(activity.getResources().getColor(R.color.colorBlue));
                    suggestionId.setBackgroundResource(R.drawable.shape_grey_stroke_blue_left);
                } else {
                    askId.setBackgroundResource(R.drawable.shape_grey_stroke_blue_left);
                    complainId.setBackgroundColor(activity.getResources().getColor(R.color.colorBlue));
                    suggestionId.setBackgroundResource(R.drawable.shape_grey_stroke_blue_right);
                }
            }
        });

        suggestionId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                type = 3;

                askId.setTextColor(activity.getResources().getColor(R.color.colorBlue));
                complainId.setTextColor(activity.getResources().getColor(R.color.colorBlue));
                suggestionId.setTextColor(activity.getResources().getColor(R.color.colorWhite));
                if (ConfigurationFile.getCurrentLanguage(activity).equals("ar")) {
                    askId.setBackgroundResource(R.drawable.shape_grey_stroke_blue_right);
                    complainId.setBackgroundResource(R.drawable.shape_grey_stroke_blue);
                    suggestionId.setBackgroundResource(R.drawable.shape_rounded_blue_right);
                } else {
                    askId.setBackgroundResource(R.drawable.shape_grey_stroke_blue_left);
                    complainId.setBackgroundResource(R.drawable.shape_grey_stroke_blue);
                    suggestionId.setBackgroundResource(R.drawable.shape_rounded_blue_left);
                }
            }
        });

        closeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void sendMessageRequest(final int type, final String message) {
        final LoadingDialog loadingDialog = new LoadingDialog();

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("Ticket_Type_ID", type);
            jsonParams.put("Ticket_Mssg", message);
            if (!image.equals(""))
                jsonParams.put("Ticket_Img_Url", image);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        APIModel.postMethod(activity, "Setting/PostTicket", jsonParams, new TextHttpResponseHandler() {
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
                                sendMessageRequest(type, message);
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);
                Utilities.toastySuccess(activity, activity.getString(R.string.sent_successfully));
                dialog.dismiss();
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

    private void getContacts() {
        final LoadingDialog loadingDialog = new LoadingDialog();
        APIModel.getMethod(activity, "Setting/ContactUs", new TextHttpResponseHandler() {
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
                                getContacts();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);
                Type dataType = new TypeToken<ContactsModel>() {
                }.getType();
                ContactsModel data = new Gson().fromJson(responseString, dataType);

                phone = data.getResult().getWhatsapp();
                email = data.getResult().getMail();
                twitter = data.getResult().getTwitter();
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