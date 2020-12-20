package com.uriallab.haat.haat.viewModels;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.DataModels.CarModel;
import com.uriallab.haat.haat.DataModels.CarTpeModel;
import com.uriallab.haat.haat.DataModels.CarYearModel;
import com.uriallab.haat.haat.DataModels.CategoryModel;
import com.uriallab.haat.haat.DataModels.DriverRegisterModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.SpinnerModel;
import com.uriallab.haat.haat.UI.Activities.RegisterAsDriver.ThirdStepActivity;
import com.uriallab.haat.haat.UI.Activities.SentSuccessfullyActivity;
import com.uriallab.haat.haat.UI.MainActivity;
import com.uriallab.haat.haat.Utilities.Dialogs;
import com.uriallab.haat.haat.Utilities.LoadingDialog;
import com.uriallab.haat.haat.Utilities.Utilities;
import com.uriallab.haat.haat.Utilities.camera.Camera;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.uriallab.haat.haat.Utilities.GlobalVariables.FINISH_ACTIVITY_REGISTER;

public class DriverRegisterThirdStepViewModel {

    public String type = "";
    public ArrayList<SpinnerModel> categoryList = new ArrayList<>();
    public ObservableField<String> selectedValues = new ObservableField<>("");

    public ObservableInt rotation = new ObservableInt(0);

    public ObservableField<String> plateNumberObservable = new ObservableField<>("");
    public ObservableField<String> plateNumberError = new ObservableField<>();

    public List<String> carList = new ArrayList<>();
    public List<String> carIdList = new ArrayList<>();

    public List<String> carTypeList = new ArrayList<>();
    public List<String> carTypeIdList = new ArrayList<>();

    public List<String> yearList = new ArrayList<>();

    public String car = "0";
    public String carType = "0";
    public String year = "";

    public String profilePic = "";
    public String idPic = "";
    public String licensePic = "";

    public int imgType = 0;

    private ThirdStepActivity activity;

    private DriverRegisterModel driverRegisterModel;

    public DriverRegisterThirdStepViewModel(ThirdStepActivity activity, DriverRegisterModel driverRegisterModel) {
        this.activity = activity;
        this.driverRegisterModel = driverRegisterModel;

        if (ConfigurationFile.getCurrentLanguage(activity).equals("ar"))
            rotation.set(180);

        carList.add(activity.getResources().getString(R.string.car));
        carIdList.add("0");
        carTypeList.add(activity.getResources().getString(R.string.car_type));
        carTypeIdList.add("0");
        yearList.add(activity.getResources().getString(R.string.produce_year));
        carTypeList.add(activity.getResources().getString(R.string.car_type));
        carTypeIdList.add("0");
        getCategoryRequest();
        getCar();

        getCarType();

        getCarYear();
    }

    public void nextStep() {
        if (validate())
            registerRequest();
    }

    private boolean validate() {
        Utilities.hideKeyboard(activity);

        if (selectedValues.get().equals("")) {

            if (selectedValues.get().equals(""))
                Utilities.toastyRequiredFieldCustom(activity, activity.getString(R.string.please_category_));
        }

            if (plateNumberObservable.get().isEmpty() ||
                car.equals("0") || carType.equals("0") || year.equals(activity.getResources().getString(R.string.produce_year)) ||
                licensePic.equals("") || profilePic.equals("") || idPic.equals("")) {

            if (plateNumberObservable.get().isEmpty())
                plateNumberError.set(activity.getResources().getString(R.string.required));

            if (car.equals("0"))
                Utilities.toastyRequiredFieldCustom(activity, activity.getString(R.string.car));

            if (carType.equals("0"))
                Utilities.toastyRequiredFieldCustom(activity, activity.getString(R.string.car_type));

            if (year.equals(activity.getResources().getString(R.string.produce_year)))
                Utilities.toastyRequiredFieldCustom(activity, activity.getString(R.string.produce_year));

            if (licensePic.equals(""))
                Utilities.toastyRequiredFieldCustom(activity, activity.getString(R.string.complete_data));

            if (idPic.equals(""))
                Utilities.toastyRequiredFieldCustom(activity, activity.getString(R.string.complete_data));

            if (profilePic.equals(""))
                Utilities.toastyRequiredFieldCustom(activity, activity.getString(R.string.complete_data));

            return false;
        }

        return true;
    }

    private void registerRequest() {

        final LoadingDialog loadingDialog = new LoadingDialog();

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("User_Full_Nm", driverRegisterModel.getUser_Full_Nm());
            jsonParams.put("User_Phone", driverRegisterModel.getUser_Phone());
            jsonParams.put("User_GenderID", driverRegisterModel.getUser_GenderID());
            jsonParams.put("User_BirthDate", driverRegisterModel.getUser_BirthDate());
            jsonParams.put("User_Mail", driverRegisterModel.getUser_Mail());
            jsonParams.put("User_NationalID", driverRegisterModel.getUser_NationalID());
            jsonParams.put("User_CountryID", driverRegisterModel.getUser_CountryID());
            jsonParams.put("User_CityID", driverRegisterModel.getUser_CityID());
            jsonParams.put("User_RegionID", driverRegisterModel.getUser_RegionID());
            jsonParams.put("User_NationalID_Type", driverRegisterModel.getUser_NationalID_Type());
            jsonParams.put("User_CarTypeID", carType);
            jsonParams.put("User_CarYearID", year);
            jsonParams.put("User_CarID", car);
            jsonParams.put("User_STCPay_Acc", driverRegisterModel.getUser_STCPay_Acc());
            jsonParams.put("User_License_Number", plateNumberObservable.get());
            jsonParams.put("User_ImgUrl", profilePic);
            jsonParams.put("User_NationalID_ImgUrl", idPic);
            jsonParams.put("User_Profession_And_Department_ID", "["+selectedValues.get()+"]"); // [2,4,5]
            jsonParams.put("User_License_ImgUrl", licensePic);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        APIModel.postMethod(activity, "Authorization/DriverRegister", jsonParams, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                switch (statusCode) {
                    case 401:
                        try {
                            JSONObject jsonObject = new JSONObject(responseString);
                            if (jsonObject.getJSONObject("error").has("Message"))
                                Utilities.toastyError(activity, jsonObject.getJSONObject("error").getString("Message"));
                            else
                                Utilities.toastyError(activity, responseString + " ");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 400:
                        String message;
                        try {
                            JSONObject jsonObject = new JSONObject(responseString);
                            if (jsonObject.getJSONObject("error").has("Message"))
                                message = jsonObject.getJSONObject("error").getString("Message");
                            else
                                message = responseString;
                        } catch (JSONException e) {
                            message = responseString;
                            e.printStackTrace();
                        }
                        backToHome(message);
                        break;
                    default:
                        APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                            @Override
                            public void onRefresh() {
                                registerRequest();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);
                String message;
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    if (jsonObject.getJSONObject("result").has("Message"))
                        message = jsonObject.getJSONObject("result").getString("Message");
                    else
                        message = responseString;
                } catch (JSONException e) {
                    message = responseString;
                    e.printStackTrace();
                }

                backToHome(message);

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

    private void getCategoryRequest() {
        final LoadingDialog loadingDialog = new LoadingDialog();
        APIModel.getMethod(activity, "Client/GetCategory", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");

                SpinnerModel stateVO = new SpinnerModel();
                stateVO.setId(0);
                stateVO.setTitle("");
                stateVO.setSelected(false);
                categoryList.add(stateVO);
                activity.customSpinnerAdapter.notifyDataSetChanged();

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
                        APIModel.handleFailure(activity, statusCode, responseString, () -> getCategoryRequest());
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);

                Type dataType = new TypeToken<CategoryModel>() {
                }.getType();
                CategoryModel data = new Gson().fromJson(responseString, dataType);

                SpinnerModel stateVO = new SpinnerModel();
                stateVO.setId(0);
                stateVO.setTitle(activity.getResources().getString(R.string.category_));
                stateVO.setSelected(false);
                categoryList.add(stateVO);

                for (int i = 0; i < data.getResult().getCategory().size(); i++) {
                    stateVO = new SpinnerModel();
                    if (ConfigurationFile.getCurrentLanguage(activity).equals("ar"))
                        stateVO.setTitle(data.getResult().getCategory().get(i).getCategory_Title_AR());
                    else
                        stateVO.setTitle(data.getResult().getCategory().get(i).getCategory_Title_EN());
                    stateVO.setSelected(false);
                    stateVO.setId(data.getResult().getCategory().get(i).getCategoryUID());
                    categoryList.add(stateVO);
                }

                activity.customSpinnerAdapter.notifyDataSetChanged();
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
    private void getCar() {
        final LoadingDialog loadingDialog = new LoadingDialog();
        APIModel.getMethod(activity, "Setting/GetCars", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                switch (statusCode) {
                    default:
                        APIModel.handleFailure(activity, statusCode, responseString, () -> getCar());
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);

                Type dataType = new TypeToken<CarModel>() {
                }.getType();
                CarModel data = new Gson().fromJson(responseString, dataType);

                for (int i = 0; i < data.getResult().getData().size(); i++) {
                    carList.add(data.getResult().getData().get(i).getCare_Type());
                    carIdList.add(data.getResult().getData().get(i).getId());
                }

                activity.carAdapter.notifyDataSetChanged();
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

    private void getCarType() {
        final LoadingDialog loadingDialog = new LoadingDialog();
        APIModel.getMethod(activity, "Setting/GetCarTypes", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                switch (statusCode) {
                    default:
                        APIModel.handleFailure(activity, statusCode, responseString, () -> getCarType());
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);

                Type dataType = new TypeToken<CarTpeModel>() {
                }.getType();
                CarTpeModel data = new Gson().fromJson(responseString, dataType);

                for (int i = 0; i < data.getResult().getData().size(); i++) {
                    carTypeList.add(data.getResult().getData().get(i).getName());
                    carTypeIdList.add(data.getResult().getData().get(i).getId());
                }

                activity.carTypeAdapter.notifyDataSetChanged();
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

    private void getCarYear() {
        final LoadingDialog loadingDialog = new LoadingDialog();
        APIModel.getMethod(activity, "Setting/GetCarYears", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                switch (statusCode) {
                    default:
                        APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                            @Override
                            public void onRefresh() {
                                getCarYear();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);

                Type dataType = new TypeToken<CarYearModel>() {
                }.getType();
                CarYearModel data = new Gson().fromJson(responseString, dataType);

                for (int i = 0; i < data.getResult().getYears().size(); i++) {
                    yearList.add(data.getResult().getYears().get(i).getCareYear());
                }

                activity.carYearAdapter.notifyDataSetChanged();
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

    public void getPhoto(int type) {

        imgType = type;

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

    public void back() {
        Utilities.hideKeyboard(activity);
        activity.finish();
    }

    private void backToHome(String messageTxt) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_alert_dialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView message = dialog.findViewById(R.id.message);
        TextView ok = dialog.findViewById(R.id.ok);

        message.setText(messageTxt);

        ok.setOnClickListener(v -> {
            FINISH_ACTIVITY_REGISTER = true;

            Intent intent = new Intent(activity, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("key", "-1");
            activity.startActivity(intent);

            //activity.finish();
            dialog.dismiss();
        });

        dialog.show();
    }
}