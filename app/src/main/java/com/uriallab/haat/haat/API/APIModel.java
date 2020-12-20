package com.uriallab.haat.haat.API;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.uriallab.haat.haat.LocalNotification.TrackingDelegate;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.SharedPreferences.LoginSession;
import com.uriallab.haat.haat.UI.Activities.Auth.LoginActivity;
import com.uriallab.haat.haat.Utilities.IntentClass;
import com.uriallab.haat.haat.Utilities.Utilities;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.KeyStore;

import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.protocol.HTTP;

public class APIModel {

    public final static int take = 20;
    public final static int take_2 = 10;
//    public final static String BASE_URL = "http://apidev.haatapp.com/";
    public final static String BASE_URL = "http://85.194.65.236:600/";
//    public final static String BASE_URL = "https://www.haatapp.com/";
    public final static String BASE_URL_GOOGLE = "https://maps.googleapis.com/maps/api/place/";
    public final static String BASE_URL_GOOGLE2 = "https://maps.googleapis.com/maps/api";
    public final static int FORCE_UPDATE = 451;
    // when user blocked
    public final static int BLOCK = 456;
    // when token expired
    public final static int REFRESH_TOKEN = 401;
    public final static int SUCCESS = 200;
    public final static int CREATED = 201;
    public final static int Failer = 422;
    public final static int BAD_REQUEST = 400;
    public final static int UNAUTHORIZE = 403;
    public final static int SERVER_ERROR = 500;
    public final static int Error = 409;
    public static String version = "v1";
    public static String device_type = "android";

    public static void handleFailure(final Context activity, int statusCode, String errorResponse, final RefreshTokenListener listener) {
        Log.e("fail", statusCode + "--" + errorResponse);
        Type dataType = new TypeToken<MessageResponse>() {
        }.getType();
        MessageResponse responseBody = new MessageResponse();
        try {
            if (statusCode != SERVER_ERROR)
                responseBody = new Gson().fromJson(errorResponse, dataType);

        } catch (Exception e) {
        }
        switch (statusCode) {
            case BAD_REQUEST:
                Utilities.toastyError(activity, responseBody.getMessage() != null ?
                        responseBody.getMessage() : "");
                break;
            case 404:
                Utilities.toastyError(activity, responseBody.getMessage() != null ?
                        responseBody.getMessage() : activity.getString(R.string.no_network));
                break;
            case Failer:
                Utilities.toastyError(activity, responseBody.getMessage() != null ?
                        responseBody.getMessage() : "  ");
                break;
            case Error:
                Utilities.toastyError(activity, responseBody.getMessage() != null ?
                        responseBody.getMessage() : "   ");
                break;
            case UNAUTHORIZE:
                LoginSession.clearData(activity);
                IntentClass.goToActivityAndClear(activity, LoginActivity.class, null);
                Utilities.toastyError(activity, activity.getString(R.string.must_login));
                try {
                    Intent myService = new Intent(activity, TrackingDelegate.class);
                    activity.stopService(myService);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case REFRESH_TOKEN:
                LoginSession.clearData(activity);
                Utilities.toastyError(activity, activity.getString(R.string.session_expired));
                IntentClass.goToActivityAndClear(activity, LoginActivity.class, null);
                try {
                    Intent myService = new Intent(activity, TrackingDelegate.class);
                    activity.stopService(myService);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            default:
                Utilities.toastyError(activity, activity.getString(R.string.no_network));
        }
    }

    public static AsyncHttpClient getMethod(Activity currentActivity, String url, TextHttpResponseHandler textHttpResponseHandler) {
        AsyncHttpClient client = new AsyncHttpClient();
        if (Utilities.checkNetworkConnectivity(currentActivity)) {
            KeyStore trustStore = null;
            try {
                trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
                socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client = new AsyncHttpClient();
                client.setSSLSocketFactory(socketFactory);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (LoginSession.isLoggedIn(currentActivity)) {
                client.addHeader("Authorization", LoginSession.getToken(currentActivity));
                Log.e("Authorization", LoginSession.getToken(currentActivity));
            }
            client.addHeader("lang", ConfigurationFile.getCurrentLanguage(currentActivity));
            client.addHeader("Content-Type", "application/json");
            client.addHeader("Accept", "application/json");
            Log.e("BASE_URL", BASE_URL + url);
            client.get(BASE_URL + url, textHttpResponseHandler);

            return client;
        } else {
            return client;
        }
    }

    public static AsyncHttpClient getMethodForGoogle(Activity currentActivity, String url, TextHttpResponseHandler textHttpResponseHandler) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(200000);
        if (Utilities.checkNetworkConnectivity(currentActivity)) {
            KeyStore trustStore = null;
            try {
                trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
                socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client = new AsyncHttpClient();
                client.setSSLSocketFactory(socketFactory);
            } catch (Exception e) {
                e.printStackTrace();
            }
            client.addHeader("language", ConfigurationFile.getCurrentLanguage(currentActivity));
            client.addHeader("Accept", "application/json");
            client.addHeader("User-Agent", "PostmanRuntime/7.2.0");
            Log.e("BASE_URL", BASE_URL_GOOGLE + url);
            client.get(BASE_URL_GOOGLE + url, textHttpResponseHandler);

            return client;
        } else {
            return client;
        }
    }

    public static AsyncHttpClient getMethodForGoogle2(Activity currentActivity, String url, TextHttpResponseHandler textHttpResponseHandler) {
        AsyncHttpClient client = new AsyncHttpClient();
        if (Utilities.checkNetworkConnectivity(currentActivity)) {
            KeyStore trustStore = null;
            try {
                trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
                socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client = new AsyncHttpClient();
                client.setSSLSocketFactory(socketFactory);
            } catch (Exception e) {
                e.printStackTrace();
            }
            client.addHeader("language", ConfigurationFile.getCurrentLanguage(currentActivity));
            client.addHeader("Accept", "application/json");
            client.addHeader("User-Agent", "PostmanRuntime/7.2.0");
            Log.e("BASE_URL", BASE_URL_GOOGLE2 + url);
            client.get(BASE_URL_GOOGLE2 + url, textHttpResponseHandler);

            return client;
        } else {
            return client;
        }
    }

    public static AsyncHttpClient postMethod(Context currentActivity, String url, JSONObject jsonParams, TextHttpResponseHandler textHttpResponseHandler) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(290000);

      /*  client.setResponseTimeout(290000);
        client.setMaxRetriesAndTimeout(0,290000);*/


        if (Utilities.checkNetworkConnectivity(currentActivity)) {
            KeyStore trustStore = null;
            try {
                trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
                socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client = new AsyncHttpClient();
                client.setSSLSocketFactory(socketFactory);
                client.setTimeout(300000);
             /*   client.setResponseTimeout(290000);
                client.setMaxRetriesAndTimeout(0,290000);*/
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (LoginSession.isLoggedIn(currentActivity)) {
                client.addHeader("Authorization", LoginSession.getToken(currentActivity));
                Log.e("Authorization", LoginSession.getToken(currentActivity));
            }
            client.addHeader("lang", ConfigurationFile.getCurrentLanguage(currentActivity));
            client.addHeader("Content-Type", "application/json");
            client.addHeader("Accept", "application/json");

            StringEntity entity = null;
            entity = new StringEntity(jsonParams.toString(), HTTP.UTF_8);

            client.post(currentActivity, BASE_URL + url, entity, "application/json", textHttpResponseHandler);
            //   client.post(BASE_URL + url, params, textHttpResponseHandler);
            Log.e("params", jsonParams.toString());
            Log.e("BASE_URL", BASE_URL + url);

            return client;
        } else {
            return client;
        }
    }

    public static AsyncHttpClient postMethodNoBaseUrl(Context currentActivity, String url, JSONObject jsonParams, TextHttpResponseHandler textHttpResponseHandler) {
        AsyncHttpClient client = new AsyncHttpClient();

        if (Utilities.checkNetworkConnectivity(currentActivity)) {
            KeyStore trustStore = null;
            try {
                trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
                socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client = new AsyncHttpClient();
                client.setSSLSocketFactory(socketFactory);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (LoginSession.isLoggedIn(currentActivity)) {
                client.addHeader("Authorization", LoginSession.getToken(currentActivity));
                Log.e("Authorization", LoginSession.getToken(currentActivity));
            }
            client.addHeader("lang", ConfigurationFile.getCurrentLanguage(currentActivity));
            client.addHeader("Content-Type", "application/json");
            client.addHeader("Accept", "application/json");

            StringEntity entity = null;
            entity = new StringEntity(jsonParams.toString(), HTTP.UTF_8);

            client.post(currentActivity,  url, entity, "application/json;charset=utf-8", textHttpResponseHandler);
            //   client.post(BASE_URL + url, params, textHttpResponseHandler);
            Log.e("params", jsonParams.toString());
            Log.e("BASE_URL",  url);

            return client;
        } else {
            return client;
        }
    }

    public static AsyncHttpClient postMethodParams(Context currentActivity, String url, RequestParams params, TextHttpResponseHandler textHttpResponseHandler) {
        AsyncHttpClient client = new AsyncHttpClient();

        if (Utilities.checkNetworkConnectivity(currentActivity)) {
            KeyStore trustStore = null;
            try {
                trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
                socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client = new AsyncHttpClient();
                client.setSSLSocketFactory(socketFactory);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (LoginSession.isLoggedIn(currentActivity)) {
                client.addHeader("Authorization", LoginSession.getToken(currentActivity));
                Log.e("Authorization", LoginSession.getToken(currentActivity));
            }
            client.addHeader("lang", ConfigurationFile.getCurrentLanguage(currentActivity));
            client.addHeader("Content-Type", "application/json");
            client.addHeader("Accept", "application/json");
            client.addHeader("version", version + "");
            client.addHeader("User-Agent", "PostmanRuntime/7.2.0");

            client.post(BASE_URL + url, params, textHttpResponseHandler);
            Log.e("params", params.toString());
            Log.e("BASE_URL", BASE_URL + url);

            return client;
        } else {
            return client;
        }
    }

    public static AsyncHttpClient patchMethod(Context currentActivity, String url, JSONObject jsonParams, TextHttpResponseHandler textHttpResponseHandler) {
        AsyncHttpClient client = new AsyncHttpClient();
        if (Utilities.checkNetworkConnectivity(currentActivity)) {
            KeyStore trustStore = null;
            try {
                trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
                socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client = new AsyncHttpClient();
                client.setSSLSocketFactory(socketFactory);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (LoginSession.isLoggedIn(currentActivity)) {
                client.addHeader("Authorization", LoginSession.getToken(currentActivity));
                Log.e("Authorization", LoginSession.getToken(currentActivity));
            }
            client.addHeader("version", version + "");
            client.addHeader("lang", ConfigurationFile.getCurrentLanguage(currentActivity));
            client.addHeader("Accept", "application/json");
            client.addHeader("Content-Type", "application/json; charset=utf-8");
            client.addHeader("accept-charset", "UTF-8");
            client.addHeader("Accept-Encoding", "gzip, deflate, br");
            client.addHeader("Connection", "keep-alive");
            client.addHeader("User-Agent", "PostmanRuntime/7.24.1");

            StringEntity entity = null;
            try {
                entity = new StringEntity(jsonParams.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            client.patch(currentActivity, BASE_URL + url, entity, "application/json", textHttpResponseHandler);
            //   client.post(BASE_URL + url, params, textHttpResponseHandler);
            Log.e("params", jsonParams.toString());
            Log.e("BASE_URL", BASE_URL + url);

            return client;
        } else {
            return client;
        }
    }

    public static AsyncHttpClient putMethod(Activity currentActivity, String url, JSONObject jsonParams, TextHttpResponseHandler textHttpResponseHandler) {
        AsyncHttpClient client = new AsyncHttpClient();
        if (Utilities.checkNetworkConnectivity(currentActivity)) {
            KeyStore trustStore = null;
            try {
                trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
                socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client = new AsyncHttpClient();
                client.setSSLSocketFactory(socketFactory);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (LoginSession.isLoggedIn(currentActivity)) {
                client.addHeader("Authorization", LoginSession.getToken(currentActivity));
                Log.e("Authorization", LoginSession.getToken(currentActivity));
            }
            client.addHeader("lang", ConfigurationFile.getCurrentLanguage(currentActivity));
            client.addHeader("Content-Type", "application/json");
            client.addHeader("Accept", "application/json");

            StringEntity entity = null;
            try {
                entity = new StringEntity(jsonParams.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            client.put(currentActivity, BASE_URL + url, entity, "application/json", textHttpResponseHandler);
            //   client.post(BASE_URL + url, params, textHttpResponseHandler);
            Log.e("params", jsonParams.toString());
            Log.e("BASE_URL", BASE_URL + url);

            return client;
        } else {
            return client;
        }
    }

    public static AsyncHttpClient deleteMethod(Activity currentActivity, String url, JSONObject jsonParams, TextHttpResponseHandler textHttpResponseHandler) {
        AsyncHttpClient client = new AsyncHttpClient();
        if (Utilities.checkNetworkConnectivity(currentActivity)) {
            KeyStore trustStore = null;
            try {
                trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
                socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client = new AsyncHttpClient();
                client.setSSLSocketFactory(socketFactory);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (LoginSession.isLoggedIn(currentActivity)) {
                client.addHeader("Authorization", LoginSession.getToken(currentActivity));
                Log.e("Authorization", LoginSession.getToken(currentActivity));
            }
            client.addHeader("version", version + "");
            client.addHeader("lang", ConfigurationFile.getCurrentLanguage(currentActivity));
            client.addHeader("Accept", "application/json");
            client.addHeader("Content-Type", "application/json; charset=utf-8");
            client.addHeader("accept-charset", "UTF-8");
            client.addHeader("Accept-Encoding", "gzip, deflate, br");
            client.addHeader("Connection", "keep-alive");
            client.addHeader("User-Agent", "PostmanRuntime/7.24.1");

            StringEntity entity = null;
            try {
                entity = new StringEntity(jsonParams.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            client.delete(currentActivity, BASE_URL + url, entity, "application/json", textHttpResponseHandler);

            Log.e("BASE_URL", BASE_URL + url);

            return client;
        } else {
            return client;
        }
    }


    public interface RefreshTokenListener {
        void onRefresh();
    }
}