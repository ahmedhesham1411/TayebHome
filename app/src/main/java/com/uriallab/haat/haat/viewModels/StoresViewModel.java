package com.uriallab.haat.haat.viewModels;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.DataModels.GoogleStoresModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.UI.Activities.Updates.StoresActivity;
import com.uriallab.haat.haat.Utilities.Dialogs;
import com.uriallab.haat.haat.Utilities.GlobalVariables;
import com.uriallab.haat.haat.Utilities.LoadingDialog;

import java.lang.reflect.Type;

import cz.msebera.android.httpclient.Header;

public class StoresViewModel extends ViewModel {

    public ObservableField<String> categoryName = new ObservableField<>();

    public ObservableInt rotation = new ObservableInt(0);

    @SuppressLint("StaticFieldLeak")
    private StoresActivity activity;

    private GoogleStoresModel data;

    public StoresViewModel(StoresActivity activity, String type, String category, boolean isSearch, String searchTxt) {
        this.activity = activity;

        if (ConfigurationFile.getCurrentLanguage(activity).equals("ar")) {
            rotation.set(180);
        }

        if (isSearch) {
            categoryName.set(searchTxt);
            getSearchResult(searchTxt);
        } else {
            categoryName.set(category);
            getStores(type);
        }
    }

   /* public void getStores(final String catType) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            final LoadingDialog loadingDialog = new LoadingDialog();

            double lat = GlobalVariables.LOCATION_LAT;
            double lng = GlobalVariables.LOCATION_LNG;
            String urlType = "";
            if (!catType.equals(""))
                urlType = "&type=" + catType;
            String url = "nearbysearch/json?key=" + activity.getResources().getString(R.string.api_key) +
                    "&location=" + lat + "," + lng +
                    "&rankby=distance" +
                    urlType +
                    "&language=" + ConfigurationFile.getCurrentLanguage(activity);
            APIModel.getMethodForGoogle(activity, url, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.e("response", responseString + "Error");
                    switch (statusCode) {
                        default:
                            APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                                @Override
                                public void onRefresh() {
                                    getStores(catType);
                                }
                            });
                            break;
                    }
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    Log.e("response", responseString);
                    Type dataType = new TypeToken<GoogleStoresModel>() {
                    }.getType();
                    data = new Gson().fromJson(responseString, dataType);

                    try {
                        activity.pageToken = data.getNext_page_token();
                    } catch (Exception e) {
                        activity.pageToken = "";
                        e.printStackTrace();
                    }

                    if (data.getResults().isEmpty()) {
                        activity.binding.noData.setVisibility(View.VISIBLE);
                        activity.binding.storesRecycler.setVisibility(View.GONE);
                    } else {
                        activity.binding.noData.setVisibility(View.GONE);
                        activity.binding.storesRecycler.setVisibility(View.VISIBLE);
                        activity.updateRecycler(data.getResults());
                    }
                }

                @Override
                public void onStart() {
                    super.onStart();
                    activity.isLoading = true;
                    Dialogs.showLoading(activity, loadingDialog);
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    activity.isLoading = false;
                    Dialogs.dismissLoading(loadingDialog);
                *//*    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Dialogs.dismissLoading(loadingDialog);
                        }
                    }, 500);*//*

                }
            });
        }
    }*/

    public void getStores(final String catType) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            final LoadingDialog loadingDialog = new LoadingDialog();

            double lat = GlobalVariables.LOCATION_LAT;
            double lng = GlobalVariables.LOCATION_LNG;
            String urlType = "";
            String aa = catType;
            if (!catType.equals(""))
                urlType = "&type=" + catType;
            String url = "nearbysearch/json?key=" + activity.getResources().getString(R.string.api_key) +
                    "&location=" + lat + "," + lng +
                    "&radius=15000" +
                    urlType +
                    "&language=" + ConfigurationFile.getCurrentLanguage(activity);
            APIModel.getMethodForGoogle(activity, url, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.e("response", responseString + "Error");
                    switch (statusCode) {
                        default:
                            APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                                @Override
                                public void onRefresh() {
                                    getStores(catType);
                                }
                            });
                            break;
                    }
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    Log.e("response", responseString);
                    Type dataType = new TypeToken<GoogleStoresModel>() {
                    }.getType();
                    data = new Gson().fromJson(responseString, dataType);

                    try {
                        activity.pageToken = data.getNext_page_token();
                    } catch (Exception e) {
                        activity.pageToken = "";
                        e.printStackTrace();
                    }

                    if (data.getResults().isEmpty()) {
                        activity.binding.noData.setVisibility(View.VISIBLE);
                        activity.binding.storesRecycler.setVisibility(View.GONE);
                    } else {
                        activity.binding.noData.setVisibility(View.GONE);
                        activity.binding.storesRecycler.setVisibility(View.VISIBLE);
                        activity.updateRecycler(data.getResults());
                    }
                }

                @Override
                public void onStart() {
                    super.onStart();
                    activity.isLoading = true;
                    Dialogs.showLoading(activity, loadingDialog);
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    activity.isLoading = false;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Dialogs.dismissLoading(loadingDialog);
                        }
                    }, 500);

                }
            });
        }
    }

    public void getSearchResult(final String search) {
        final LoadingDialog loadingDialog = new LoadingDialog();
        double lat = GlobalVariables.LOCATION_LAT;
        double lng = GlobalVariables.LOCATION_LNG;
        String urlType = "";

        String url = "textsearch/json?query=" +  search + "&key="  +activity.getResources().getString(R.string.api_key)+
                "&location=" + lat + "," + lng +
                "&rankby=distance" +
                urlType +
                "&language=" + ConfigurationFile.getCurrentLanguage(activity);


     /*   String url = "textsearch/json?query=" + search + "&key=" + activity.getResources().getString(R.string.api_key) +
                "&language=" + ConfigurationFile.getCurrentLanguage(activity);*/
        APIModel.getMethodForGoogle(activity, url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                switch (statusCode) {
                    default:
                        APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                            @Override
                            public void onRefresh() {
                                getStores(search);
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.e("response", responseString);
                Type dataType = new TypeToken<GoogleStoresModel>() {
                }.getType();
                data = new Gson().fromJson(responseString, dataType);

                try {
                    activity.pageToken = data.getNext_page_token();
                } catch (Exception e) {
                    activity.pageToken = "";
                    e.printStackTrace();
                }

                if (data.getResults().isEmpty()) {
                    activity.binding.noData.setVisibility(View.VISIBLE);
                    activity.binding.storesRecycler.setVisibility(View.GONE);
                } else {
                    activity.binding.noData.setVisibility(View.GONE);
                    activity.binding.storesRecycler.setVisibility(View.VISIBLE);
                    activity.updateRecycler(data.getResults());
                }
            }

            @Override
            public void onStart() {
                super.onStart();
                activity.isLoading = true;
                Dialogs.showLoading(activity, loadingDialog);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                activity.isLoading = false;
                Dialogs.dismissLoading(loadingDialog);
             /*   new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Dialogs.dismissLoading(loadingDialog);
                    }
                }, 1000);*/

            }
        });
    }

    public void getStoresPaginate(final String catType, final String pageToken) {

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            final LoadingDialog loadingDialog = new LoadingDialog();
            String urlType = "";
            if (!catType.equals(""))
                urlType = "&type=" + catType;
            String url = "nearbysearch/json?key=" + activity.getResources().getString(R.string.api_key) +
                    "&location=" + GlobalVariables.LOCATION_LAT + "," + GlobalVariables.LOCATION_LNG +
                    "&radius=2500" +
                    "&pagetoken=" + pageToken +
                    urlType +
                    "&language=" + ConfigurationFile.getCurrentLanguage(activity);
            APIModel.getMethodForGoogle(activity, url, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.e("response", responseString + "Error");
                    switch (statusCode) {
                        default:
                            APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                                @Override
                                public void onRefresh() {
                                    getStoresPaginate(catType, pageToken);
                                }
                            });
                            break;
                    }
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    Log.e("response", responseString);
                    Type dataType = new TypeToken<GoogleStoresModel>() {
                    }.getType();
                    data = new Gson().fromJson(responseString, dataType);

                    try {
                        activity.pageToken = data.getNext_page_token();
                    } catch (Exception e) {
                        activity.pageToken = "";
                        e.printStackTrace();
                    }

                    activity.updateRecyclerPaginate(data.getResults());
                }

                @Override
                public void onStart() {
                    super.onStart();
                    activity.isLoading = true;
                    Dialogs.showLoading(activity, loadingDialog);
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    activity.isLoading = false;
                    Dialogs.dismissLoading(loadingDialog);
/*                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Dialogs.dismissLoading(loadingDialog);
                        }
                    }, 500);*/

                }
            });
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
            public void afterTextChanged(final Editable editable) {

                getSearchResult(editable.toString());

              /*  new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getSearchResult(editable.toString());
                    }
                }, 4000);*/

            }
        };
    }

    private boolean isLocationEnabled() {
        final LocationManager manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;
        }
        return false;
    }

    public void back() {
        activity.finish();
    }
}