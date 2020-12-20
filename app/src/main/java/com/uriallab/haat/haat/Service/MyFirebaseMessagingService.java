package com.uriallab.haat.haat.Service;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.UI.Activities.SplashActivity;
import com.uriallab.haat.haat.Utilities.Utilities;

import org.json.JSONObject;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "NOTIFICATION_DATA";

    public boolean foregrounded() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "FCM Message Id: " + remoteMessage.getMessageId());
        Log.e(TAG, "FCM Data Message: " + remoteMessage.getData().toString());

        try {
            JSONObject myJson = null;
            String title;
            String description;
            String key, url, text, senderId, chatId, orderId, key2, text_type;

            myJson = new JSONObject(remoteMessage.getData());

            Log.e(TAG, "FCM myJson: " + myJson.toString());

            title = myJson.optString("title");
            description = myJson.optString("body");
            key = myJson.optString("key");

            if (foregrounded()) {
                Log.e(TAG, "foregrounded");
                Activity activity = Utilities.getActivity();
            } else {
                Log.e(TAG, "not foregrounded");
            }

            switch (key) {
                case "1":
                    orderId = myJson.optString("orderID");
                    buildNotif(title, description, "1", orderId);

                    if (ConfigurationFile.getIsNewOrderActive(getApplicationContext())) {
                        playNotificationSound(true);
                        Intent intent = new Intent("RefreshNewJourney");
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    } else
                        playNotificationSound(false);

                    break;
                case "2":
                    orderId = myJson.optString("orderID");
                    buildNotif(title, description, "2", orderId);

                    if (ConfigurationFile.getIsActiveJourney(getApplicationContext())) {
                        playNotificationSound(true);
                        Intent intent = new Intent("RefreshActiveJourney");
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    } else
                        playNotificationSound(false);
                    break;
                case "3":
                    orderId = myJson.optString("orderID");
                    buildNotif(title, description, "3", orderId);
                    break;
                case "4":
                    orderId = myJson.optString("orderID");
                    buildNotif(title, description, "4", orderId);
                    break;
                case "5":

                    if (ConfigurationFile.getIsNewOfferActive(getApplicationContext())) {
                        playNotificationSound(true);
                        Intent intent = new Intent("RefreshNewOffer");
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    } else
                        playNotificationSound(false);

                    if (ConfigurationFile.getIsOffersActive(getApplicationContext())) {
                        playNotificationSound(true);
                        Intent intent = new Intent("RefreshOffers");
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    } else
                        playNotificationSound(false);
                    vibratePhone();

                    orderId = myJson.optString("orderID");
                    buildNotif(title, description, "5", orderId);
                    break;
                case "6":
                    orderId = myJson.optString("orderID");
                    buildNotif(title, description, "6", orderId);
                    break;
                case "7":
                    orderId = myJson.optString("orderID");
                    buildNotif(title, description, "7", orderId);
                    break;
                case "8":
                    orderId = myJson.optString("orderID");
                    buildNotif(title, description, "8", orderId);
                    break;
                case "9":
                    orderId = myJson.optString("orderID");
                    buildNotif(title, description, "9", orderId);
                    break;
                case "10":

                    url = myJson.optString("url");
                    text = myJson.optString("text");
                    senderId = myJson.optString("sencerId");
                    chatId = myJson.optString("chatId");
                    key2 = myJson.optString("key2");
                    text_type = myJson.optString("text_type");

                    if (ConfigurationFile.getIsChatActive(getApplicationContext())) {
                        Intent intent = new Intent("MyData");
                        intent.putExtra("text", text);
                        intent.putExtra("chatId", Integer.valueOf(chatId));
                        intent.putExtra("sencerId", senderId);
                        intent.putExtra("url", url);
                        intent.putExtra("key2", key2);
                        intent.putExtra("text_type", text_type);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    } else {
                        orderId = myJson.optString("orderID");
                        buildNotif(title, description, "10", orderId);
                    }

                    break;
                case "11":

                    url = myJson.optString("url");
                    text = myJson.optString("text");
                    senderId = myJson.optString("sencerId");
                    chatId = myJson.optString("chatId");
                    key2 = myJson.optString("key2");
                    text_type = myJson.optString("text_type");

                    if (ConfigurationFile.getIsDriverChatActive(getApplicationContext())) {
                        Intent intent = new Intent("MyData");
                        intent.putExtra("text", text);
                        intent.putExtra("chatId", Integer.valueOf(chatId));
                        intent.putExtra("sencerId", senderId);
                        intent.putExtra("url", url);
                        intent.putExtra("key2", key2);
                        intent.putExtra("text_type", text_type);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    } else {
                        orderId = myJson.optString("orderID");
                        buildNotif(title, description, "11", orderId);
                    }

                    break;
                case "12":

                    if (ConfigurationFile.getIsNewOfferActive(getApplicationContext())) {
                        playNotificationSound(true);
                        Intent intent = new Intent("RefreshNewOffer");
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    } else
                        playNotificationSound(false);

                    if (ConfigurationFile.getIsOffersActive(getApplicationContext())) {
                        playNotificationSound(true);
                        Intent intent = new Intent("RefreshOffers");
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    } else
                        playNotificationSound(false);

                    orderId = myJson.optString("orderID");
                    buildNotif(title, description, "12", orderId);

                    vibratePhone();

                    break;
                case "13":
                    buildNotif(title, description, "13", "0");

                    break;
                default:
                    buildNotif(title, description, "0", "0");
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void buildNotif(String title, String desc, String type, String orderId) {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL);
        Context c = getApplicationContext();
        Intent intent = new Intent(c, SplashActivity.class);
        intent.putExtra("key", type);
        intent.putExtra("orderId", orderId);
        intent.putExtra("notification", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int num = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(c, num, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
//        bitmap1 = Utilities.getRoundedCornerBitmap(bitmap1, 100);

        String channelId = getString(R.string.notification_channel_id);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setLargeIcon(bitmap1);
            //   builder.setColor(getResources().getColor(R.color.colorWhite));
        } else {
            builder.setSmallIcon(R.mipmap.ic_launcher);
//            builder.setLargeIcon(bitmap2);
        }

        NotificationCompat.BigTextStyle notiStyle = new NotificationCompat.BigTextStyle();
        notiStyle.setBigContentTitle(title);
        notiStyle.bigText(desc);

        builder.setContentTitle(title)
                .setContentText(desc)
                .setContentIntent(pendingIntent)
                .setSound(uri)
                .setAutoCancel(true)
                .setStyle(notiStyle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(c);
        NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_HIGH);
            channel.setLightColor(getColor(R.color.colorAccent));
//            AudioAttributes attributes = new AudioAttributes.Builder()
//                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
//                    .build();
//            channel.setSound(uri, attributes);

            mNotificationManager.createNotificationChannel(channel);

        }
        mNotificationManager.notify(num, builder.build());
    }

    private void playNotificationSound(boolean isActive) {
        try {
            Uri alarmSound;
            if (isActive)
                alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/raw/default_notification");
            else
                alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/raw/notify");
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), alarmSound);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void vibratePhone() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(1500);
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
    }
}