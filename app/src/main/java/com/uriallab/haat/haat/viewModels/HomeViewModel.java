package com.uriallab.haat.haat.viewModels;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.DataModels.CategoryModel;
import com.uriallab.haat.haat.DataModels.ServerStoresModel;
import com.uriallab.haat.haat.UI.Activities.Updates.HatOnlineStoreActivity;
import com.uriallab.haat.haat.UI.Activities.Updates.StoresActivity;
import com.uriallab.haat.haat.UI.Activities.makeOrder.MakeOrderFirstStepActivity;
import com.uriallab.haat.haat.UI.Fragments.HomeFragment;
import com.uriallab.haat.haat.Utilities.Dialogs;
import com.uriallab.haat.haat.Utilities.GlobalVariables;
import com.uriallab.haat.haat.Utilities.IntentClass;
import com.uriallab.haat.haat.Utilities.LoadingDialog;
import com.uriallab.haat.haat.Utilities.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Mahmoud on 4/19/2020.
 */

public class HomeViewModel extends ViewModel {

    private List<CategoryModel.ResultBean.CategoryBean> listCategory = new ArrayList<>();

    private Activity activity;
    private HomeFragment fragment;

    public HomeViewModel(HomeFragment fragment) {
        activity = fragment.getActivity();
        this.fragment = fragment;

        //getStores();
        getCategoryRequest();
    }

    public void getStores() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // final LoadingDialog loadingDialog = new LoadingDialog();

            final LoadingDialog loadingDialog = new LoadingDialog();

            APIModel.getMethod(activity, "Data/GetStors?lat=" + GlobalVariables.LOCATION_LAT + "&lng=" + GlobalVariables.LOCATION_LNG, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.e("response", responseString + "Error");
                    switch (statusCode) {
                        default:
                            APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                                @Override
                                public void onRefresh() {
                                    getStores();
                                }
                            });
                            break;
                    }
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    Log.e("response", responseString);
                    Type dataType = new TypeToken<ServerStoresModel>() {
                    }.getType();
                    ServerStoresModel data = new Gson().fromJson(responseString, dataType);

                    if (data.getResult().isEmpty()) {
                        fragment.binding.storesRecycler.setVisibility(View.GONE);
                        fragment.binding.famousPlaces.setVisibility(View.GONE);
                    } else {
                        fragment.binding.storesRecycler.setVisibility(View.VISIBLE);
                        fragment.binding.famousPlaces.setVisibility(View.VISIBLE);
                        fragment.updateRecycler(data.getResult());
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

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Bundle bundle = new Bundle();
                        bundle.putString("type", "");
                        bundle.putString("category", "");
                        bundle.putString("searchTxt", editable.toString());
                        bundle.putBoolean("isSearch", true);
                        IntentClass.goToActivity(activity, StoresActivity.class, bundle);
                    }
                }, 2000);
            }
        };
    }

  /*  private void getCategoryRequest() {
        final LoadingDialog loadingDialog = new LoadingDialog();

        APIModel.getMethod(activity, "Client/GetCategory", new TextHttpResponseHandler() {
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
                                getCategoryRequest();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);

                Type dataType = new TypeToken<CategoryModel>() {
                }.getType();
                CategoryModel data = new Gson().fromJson(responseString, dataType);

                try {
                    listCategory.clear();
                    listCategory.addAll(data.getResult().getCategory());
                    listCategory.get(0).setSelected(true);
                    fragment.initCategoryRecycler(listCategory);
                } catch (Exception e) {
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
    }*/
  private void getCategoryRequest() {
      final LoadingDialog loadingDialog = new LoadingDialog();

      APIModel.getMethod(activity, "Client/GetCategory", new TextHttpResponseHandler() {
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
                              getCategoryRequest();
                          }
                      });
                      break;
              }
          }

          @Override
          public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
              Log.e("response", responseString);

              Type dataType = new TypeToken<CategoryModel>() {
              }.getType();
              CategoryModel data = new Gson().fromJson(responseString, dataType);

              try {
                  listCategory.clear();
                  listCategory.addAll(data.getResult().getCategory());
                  listCategory.get(0).setSelected(true);
                  fragment.initCategoryRecycler(listCategory);
              } catch (Exception e) {
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

    public void hatService() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isService", true);
        IntentClass.goToActivity(activity, MakeOrderFirstStepActivity.class, bundle);
    }

    public void hatCard() {
        IntentClass.goToActivity(activity, HatOnlineStoreActivity.class, null);
    }
}