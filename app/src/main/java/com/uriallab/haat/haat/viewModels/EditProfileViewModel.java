package com.uriallab.haat.haat.viewModels;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.widget.DatePicker;

import androidx.core.app.ActivityCompat;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.DataModels.UserModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.SharedPreferences.LoginSession;
import com.uriallab.haat.haat.UI.Activities.Auth.ChangePasswordActivity;
import com.uriallab.haat.haat.Utilities.Dialogs;
import com.uriallab.haat.haat.Utilities.IntentClass;
import com.uriallab.haat.haat.Utilities.LoadingDialog;
import com.uriallab.haat.haat.Utilities.Utilities;
import com.uriallab.haat.haat.Utilities.Validations;
import com.uriallab.haat.haat.Utilities.camera.Camera;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

public class EditProfileViewModel {

    public ObservableInt rotation = new ObservableInt(0);

    public ObservableField<String> profileImgObservable = new ObservableField<>("");

    public ObservableField<String> userNameObservable = new ObservableField<>("");
    public ObservableField<String> phoneObservable = new ObservableField<>("");
    public ObservableField<String> emailObservable = new ObservableField<>("");
    public ObservableField<String> birthdayObservable = new ObservableField<>("");
    public ObservableField<String> userNameError = new ObservableField<>();
    public ObservableField<String> phoneError = new ObservableField<>();
    public ObservableField<String> emailError = new ObservableField<>();
    public ObservableField<String> birthdayError = new ObservableField<>();

    public int gender = 0;

    private Activity activity;

    public EditProfileViewModel(Activity activity) {
        this.activity = activity;

        if (ConfigurationFile.getCurrentLanguage(activity).equals("ar"))
            rotation.set(180);

        if (LoginSession.getUserData(activity).getResult().getUserData().getUser_GenderID() == 1)
            gender = 1;
        else if (LoginSession.getUserData(activity).getResult().getUserData().getUser_GenderID() == 2)
            gender = 2;

        userNameObservable.set(LoginSession.getUserData(activity).getResult().getUserData().getUser_Full_Nm());
        emailObservable.set(LoginSession.getUserData(activity).getResult().getUserData().getUser_Mail());
        phoneObservable.set(LoginSession.getUserData(activity).getResult().getUserData().getUser_Phone());
        birthdayObservable.set(LoginSession.getUserData(activity).getResult().getUserData().getUser_BirthDate());
    }

    public void saveProfile() {
        if (validate())
            editRequest();
    }

    public void changePassword() {
        IntentClass.goToActivity(activity, ChangePasswordActivity.class, null);
    }

    private void editRequest() {
        final LoadingDialog loadingDialog = new LoadingDialog();

        JSONObject jsonParams = new JSONObject();
        if (profileImgObservable.get().equals("")){
            try {
                Dialogs.showLoading(activity, loadingDialog);
                String aaa = LoginSession.getUserData(activity).getResult().getUserData().getUser_ImgUrl();

                String ddddd = convertUrlToBase64(APIModel.BASE_URL+aaa);

                jsonParams.put("User_Full_Nm", userNameObservable.get());
                jsonParams.put("User_Phone", phoneObservable.get());
                jsonParams.put("User_Mail", emailObservable.get());
                jsonParams.put("User_BirthDate", birthdayObservable.get());
                jsonParams.put("User_GenderID", gender);
                jsonParams.put("User_ImgUrl", ddddd);
                Dialogs.dismissLoading(loadingDialog);

            }catch (Exception e){}
        }
        else {
            try {
                jsonParams.put("User_Full_Nm", userNameObservable.get());
                jsonParams.put("User_Phone", phoneObservable.get());
                jsonParams.put("User_Mail", emailObservable.get());
                jsonParams.put("User_BirthDate", birthdayObservable.get());
                jsonParams.put("User_GenderID", gender);
                if (!profileImgObservable.get().equals(""))
                    jsonParams.put("User_ImgUrl", profileImgObservable.get());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        APIModel.postMethod(activity, "Authorization/UpdateProfile", jsonParams, new TextHttpResponseHandler() {
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
                                Utilities.toastyError(activity, responseString + "");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                            @Override
                            public void onRefresh() {
                                editRequest();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);
                profileData();
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

        if (userNameObservable.get().isEmpty() || phoneObservable.get().isEmpty() ||
                emailObservable.get().isEmpty() || gender == 0) {

            if (userNameObservable.get().isEmpty())
                userNameError.set(activity.getResources().getString(R.string.required));

            if (emailObservable.get().isEmpty())
                emailError.set(activity.getResources().getString(R.string.required));

            if (phoneObservable.get().isEmpty())
                phoneError.set(activity.getResources().getString(R.string.required));

            if (birthdayObservable.get().isEmpty())
                birthdayError.set(activity.getResources().getString(R.string.enter_birthday));

            if (gender == 0)
                Utilities.toastyRequiredFieldCustom(activity, activity.getString(R.string.choose_gender));

            return false;
        }

        if (!Validations.isValidEmail(emailObservable.get())) {
            emailError.set(activity.getResources().getString(R.string.invalid_mail));
            return false;
        }

        return true;
    }

    private void profileData() {
        final LoadingDialog loadingDialog = new LoadingDialog();
        APIModel.getMethod(activity, "Authorization/GetProfile", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                switch (statusCode) {
                    default:
                        APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                            @Override
                            public void onRefresh() {
                                profileData();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);

                Type dataType = new TypeToken<UserModel>() {
                }.getType();
                UserModel data = new Gson().fromJson(responseString, dataType);

                LoginSession.setUserData(activity, data);

                Utilities.toastySuccess(activity, activity.getString(R.string.edited_successfully));

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

    public void getBirthday() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        // Launch Date Picker Dialog
        DatePickerDialog dpd = new DatePickerDialog(activity,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        // Display Selected date in textbox
                        birthdayObservable.set(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                    }
                }, mYear, mMonth, mDay);
        birthdayError.set(null);
        dpd.show();
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
                Camera.showGalleryFromActivity(activity);
            }
        } else {
            Camera.showGalleryFromActivity(activity);
        }
    }



    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String convert(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP).replace("\n\t", "");
    }

    private String getByteArrayFromImageURL(String url) {

        try {
            URL imageUrl = new URL(url);
            URLConnection ucon = imageUrl.openConnection();
            InputStream is = ucon.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = is.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, read);
            }
            baos.flush();
            return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);


        } catch (Exception e) {
            Log.d("Error", e.toString());
        }
        return null;
    }

    public String convertUrlToBase64(String url) {
        URL newurl;
        Bitmap bitmap;
        String base64 = "";
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            newurl = new URL(url);
            bitmap = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return base64;
    }
}