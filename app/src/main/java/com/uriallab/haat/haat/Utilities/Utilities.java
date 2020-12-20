package com.uriallab.haat.haat.Utilities;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Created by MAHMOUD on 12/12/2018.
 */
public class Utilities {
    public static final int LOCATION_REQUEST_PERMISSION = 6518;
    public static Activity activity;

    public static boolean checkNetworkConnectivity(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static String getColoredSpanned(String text, String color) {
        String input = "<font color=" + color + ">" + text + "</font>";
        return input;
    }

    public static void noInternet(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Internet connection");
        builder.setMessage("Connect to a network");

        builder.setPositiveButton("OK", null);
        builder.show();
    }

    public static String pad(long date) {
        if (date < 9)
            return "0" + date;
        return date + "";
    }

    public static void downLoadImage(ImageView imageView, String url, int drawable) {
        if (url == null || url.trim().isEmpty()) url = "invalid";
        try {
            if (url != null && !url.equals("") && !url.equals("null")) {
                if (drawable != -1)
                    Picasso.get()
                            .load(url)
                            .placeholder(drawable)
                            .into(imageView);
                else
                    Picasso.get()
                            .load(url)
                            .into(imageView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void downLoadCImage(CircleImageView imageView, String url, int drawable) {
        if (url == null || url.trim().isEmpty()) url = "invalid";
        try {
            if (url != null && !url.equals("") && !url.equals("null")) {
                if (drawable != -1)
                    Picasso.get()
                            .load(url)
                            .placeholder(drawable)
                            .into(imageView);
                else
                    Picasso.get()
                            .load(url)
                            .into(imageView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static boolean checkDateTime(Activity activity, String date, String time) {
        Calendar calendar = Calendar.getInstance();
        String current = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH)
                + " " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.MILLISECOND);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date currentDate = simpleDateFormat.parse(current);
            Date selectedDate = simpleDateFormat.parse(date + " " + time);

            if (selectedDate.before(currentDate))
                return false;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static String padNumber(int number) {
        String n = number + "";
        if (number <= 9)
            n = "0" + number;
        return n;
    }

    public static long getDateDifference(Context context, String created) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm aa", Locale.ENGLISH);
        try {
            Date currentDate = simpleDateFormat.parse(simpleDateFormat.format(new Date()));
            Date createdDate = simpleDateFormat.parse(created);
            long difference = (currentDate.getTime() - createdDate.getTime());
            return difference;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static String formatTimeHours(String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm");
        try {
            Date date = simpleDateFormat.parse(time);
            simpleDateFormat = new SimpleDateFormat("hh:mm aa");
            time = simpleDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }


    public static int convertDPtoInt(Activity activity, int padding_in_dp) {
        final float scale = activity.getResources().getDisplayMetrics().density;
        return (int) (padding_in_dp * scale + 0.5f);
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static void hideKeyboard(Activity activity) {
        try {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = activity.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(activity);
            }
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static long getDiffBetweenTime(String current, String other) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
            Date currentDate = simpleDateFormat.parse(current);
            Date otherDate = simpleDateFormat.parse(other);
            return (otherDate.getTime() - currentDate.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long getDiffBetweenTime(String date) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
            Date currentDate = new Date();
            Date otherDate = simpleDateFormat.parse(date);
            return (otherDate.getTime() - currentDate.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String formatTimeToDate(String time) {
        String date = time.substring(0, 10);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
        try {
            Date timeDate = simpleDateFormat.parse(time);
            simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            date = simpleDateFormat.format(timeDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }

    public static boolean checkLocationAvailability(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (lm != null && (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
            return true;
        }
        return false;
    }

    public static Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        float r = 0;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static Bitmap drawBorderCircularBitmap(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int radius = Math.min(h / 2, w / 2);
        Bitmap output = Bitmap.createBitmap(w + 8, h + 8, Bitmap.Config.ARGB_8888);

        Paint p = new Paint();
        p.setAntiAlias(true);

        Canvas c = new Canvas(output);
        c.drawARGB(0, 0, 0, 0);
        p.setStyle(Paint.Style.FILL);

        c.drawCircle((w / 2) + 4, (h / 2) + 4, radius, p);

        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        c.drawBitmap(bitmap, 4, 4, p);
        p.setXfermode(null);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.parseColor("#02aee7"));
        p.setStrokeWidth(3);
        c.drawCircle((w / 2) + 4, (h / 2) + 4, radius, p);

        return output;
    }


    public static Activity getActivity() throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException {
        Class activityThreadClass = Class.forName("android.app.ActivityThread");
        Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
        Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
        activitiesField.setAccessible(true);

        Map<Object, Object> activities = (Map<Object, Object>) activitiesField.get(activityThread);
        if (activities == null)
            return null;

        for (Object activityRecord : activities.values()) {
            Class activityRecordClass = activityRecord.getClass();
            Field pausedField = activityRecordClass.getDeclaredField("paused");
            pausedField.setAccessible(true);
            if (!pausedField.getBoolean(activityRecord)) {
                Field activityField = activityRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Activity activity = (Activity) activityField.get(activityRecord);
                return activity;
            }
        }

        return null;
    }

    public static String roundPrice(String price) {
        if (Double.valueOf(price) == 0)
            return "00.00";
        else
            return roundPrice(Double.valueOf(price));
    }

    public static String roundPrice(double price) {
        return String.format(Locale.ENGLISH, "%.2f", price);
    }

    public static String formatOrderDate(String orderDate) {

        if (isToday(orderDate)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh-mm-ss");
            try {
                Date date = simpleDateFormat.parse(orderDate);
                SimpleDateFormat day = new SimpleDateFormat("hh", Locale.ENGLISH);
                SimpleDateFormat month = new SimpleDateFormat("mm", Locale.ENGLISH);
                SimpleDateFormat year = new SimpleDateFormat("ss", Locale.ENGLISH);
                orderDate = year.format(date) + " " + month.format(date) + " " + day.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return orderDate;
        } else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd");
            try {
                Date date = simpleDateFormat.parse(orderDate);
                SimpleDateFormat day = new SimpleDateFormat("d", Locale.ENGLISH);
                SimpleDateFormat month = new SimpleDateFormat("mm", Locale.ENGLISH);
                SimpleDateFormat year = new SimpleDateFormat("yyyy", Locale.ENGLISH);
                orderDate = year.format(date) + "/" + month.format(date) + "/" + day.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return orderDate;
        }
    }

    public static String formatWorkTimes(String orderDate) {
        {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm aa");
            try {
                Date date = simpleDateFormat.parse(orderDate);
                SimpleDateFormat hour = new SimpleDateFormat("hh", Locale.getDefault());
                SimpleDateFormat minute = new SimpleDateFormat("mm", Locale.getDefault());
                SimpleDateFormat time = new SimpleDateFormat("aa", Locale.getDefault());
                orderDate = hour.format(date) + ":" + minute.format(date) + " " + time.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return orderDate;
        }
    }

    public static String formatOrderTimestampToDate(String timestamp) {
        return formatAdTimestampToDate(Long.valueOf(timestamp));
    }

    public static String formatAdTimestampToDate(long timestamp) {
        Date date = new Date(timestamp * 1000L);

        if (isToday2(timestamp)) {
            String orderDate;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);
                TimeZone tz = TimeZone.getDefault();
                sdf.setTimeZone(tz);
                orderDate = sdf.format(date);
            } catch (Exception e) {
                orderDate = "";
                e.printStackTrace();
            }
            return orderDate;
        } else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d/M/yyyy", Locale.ENGLISH);
            return simpleDateFormat.format(date);
        }
    }

    public static boolean isToday(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        String today = dateFormat.format(new Date());
        return date.equals(today);
    }

    public static boolean isToday2(long date1) {
        Date date = new Date(date1 * 1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH); // the format of your date
        String serverDate = sdf.format(date);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        String today = dateFormat.format(new Date());

        Log.e("TIMESTAMP", "server = " + serverDate + "\ttoday = " + today);

        return (serverDate.equals(today));
    }

    public static void darkenStatusBar(Activity activity, int color) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            activity.getWindow().setStatusBarColor(color);
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        }
    }

    public static void changeStatusBarColor(Activity myActivity, int color, boolean isChange) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = myActivity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    public static void runAnimation(RecyclerView recyclerView, int type) {
        Context context = recyclerView.getContext();
        LayoutAnimationController controller = null;
        if (type == 1) {
            controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_rise_up);
        } else {
            controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_slide_from_right);
        }
        recyclerView.setLayoutAnimation(controller);
        recyclerView.scheduleLayoutAnimation();
    }

    public static String formatOrderDate2(String orderDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
        try {
            Date date = simpleDateFormat.parse(orderDate);
            SimpleDateFormat dayname = new SimpleDateFormat("EEEE", Locale.getDefault());
            SimpleDateFormat day = new SimpleDateFormat("d", Locale.getDefault());
            SimpleDateFormat month = new SimpleDateFormat("MMMM", Locale.getDefault());
            SimpleDateFormat year = new SimpleDateFormat("yyyy", Locale.getDefault());

            orderDate = dayname.format(date) + " " + day.format(date) + " " + month.format(date) + " " + year.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return orderDate;
    }


    public static void settingStatusBarTransparent(Activity activity, boolean iconChange) {

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Window w = activity.getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setNavigationBarColor(Color.WHITE);
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            if (iconChange)
                Utilities.changeStatusBarColor(activity, R.color.colorBlack, true);
        }
    }

    public static void toastyError(Activity activity, String message) {
        Toasty.error(activity, message, Toast.LENGTH_SHORT, true).show();
    }

    public static void toastyError(Context context, String message) {
        Toasty.error(context, message, Toast.LENGTH_SHORT, true).show();
    }

    public static void toastyRequiredField(Activity activity) {
        Toasty.error(activity, activity.getResources().getString(R.string.required), Toast.LENGTH_SHORT, true).show();
    }

    public static void toastyRequiredFieldCustom(Activity activity, String message) {
        Toasty.error(activity, message, Toast.LENGTH_SHORT, true).show();
    }

    public static void toastySuccess(Activity activity, String message) {
        Toasty.success(activity, message, Toast.LENGTH_LONG, true).show();
    }

    public static String getDateFromTimeStamp(Activity activity, long time) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time * 1000);

        Date date = cal.getTime();

        SimpleDateFormat dayname, day, month, year;

        if (ConfigurationFile.getCurrentLanguage(activity).equals("ar")) {
            dayname = new SimpleDateFormat("EEEE", new Locale("ar"));
            day = new SimpleDateFormat("d", new Locale("ar"));
            month = new SimpleDateFormat("MMMM", new Locale("ar"));
            year = new SimpleDateFormat("yyyy", new Locale("ar"));
        } else {
            dayname = new SimpleDateFormat("EEEE", Locale.ENGLISH);
            day = new SimpleDateFormat("d", Locale.ENGLISH);
            month = new SimpleDateFormat("MMMM", Locale.ENGLISH);
            year = new SimpleDateFormat("yyyy", Locale.ENGLISH);
        }

        return dayname.format(date) + " " + day.format(date) + " " + month.format(date) + " " + year.format(date);
    }

    public static String getDateFromTimeStamp2(long time) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time * 1000);

        Date date = cal.getTime();

        SimpleDateFormat hour, day, month, year, minute;

        day = new SimpleDateFormat("d", Locale.ENGLISH);
        month = new SimpleDateFormat("MM", Locale.ENGLISH);
        year = new SimpleDateFormat("yyyy", Locale.ENGLISH);
        hour = new SimpleDateFormat("HH", Locale.ENGLISH);
        minute = new SimpleDateFormat("mm", Locale.ENGLISH);

        return year.format(date) + "-" + month.format(date) + "-" + day.format(date) + " " + hour.format(date) + ":" + minute.format(date);
    }

    public static String mapNumber(String number) {
        String englishNumber;
        englishNumber = number.replace("٠", "0").replace("١", "1")
                .replace("٢", "2").replace("٣", "3")
                .replace("٤", "4").replace("٥", "5")
                .replace("٦", "6").replace("٧", "7")
                .replace("٨", "8").replace("٩", "9");
        return englishNumber;
    }

    public static double getKilometers(double lat1, double long1, double lat2, double long2) {
        Log.e("LATLNGKM", lat1 + "\t" + long1 + "\n" + lat2 + "\t" + long2);
        double PI_RAD = Math.PI / 180.0;
        double phi1 = lat1 * PI_RAD;
        double phi2 = lat2 * PI_RAD;
        double lam1 = long1 * PI_RAD;
        double lam2 = long2 * PI_RAD;

        return 6371.01 * acos(sin(phi1) * sin(phi2) + cos(phi1) * cos(phi2) * cos(lam2 - lam1));
    }

    public static double CalculationByDistance(double initialLat, double initialLong,
                                               double finalLat, double finalLong) {
        int R = 6371; // km
        double dLat = toRadians(finalLat - initialLat);
        double dLon = toRadians(finalLong - initialLong);
        initialLat = toRadians(initialLat);
        finalLat = toRadians(finalLat);

        double a = sin(dLat / 2) * sin(dLat / 2) +
                sin(dLon / 2) * sin(dLon / 2) * cos(initialLat) * cos(finalLat);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static double toRadians(double deg) {
        return deg * (Math.PI / 180);
    }

    public static double distanceBetweenTwoLatLng(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = sin(deg2rad(lat1))
                * sin(deg2rad(lat2))
                + cos(deg2rad(lat1))
                * cos(deg2rad(lat2))
                * cos(deg2rad(theta));
        dist = acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public static LatLng getCurrentLocation(Activity activity) {
        return new LatLng(GlobalVariables.LOCATION_LAT, GlobalVariables.LOCATION_LNG);
    }

    public static LatLng getCurrentLocation(Context activity) {
        return new LatLng(GlobalVariables.LOCATION_LAT, GlobalVariables.LOCATION_LNG);
    }

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static String getTimeAgo(Activity activity, String timeString) {

        //  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        //format.setTimeZone(TimeZone.getTimeZone("GTM+3"));
        Date newDate = null;
        long time;
        try {
            newDate = format.parse(timeString);
            time = newDate.getTime();
            //  Log.e("Time", timeString+"\t"+time);
        } catch (ParseException e) {
            time = 0;
            //  Log.e("Time", "Exception"+timeString+"\t"+time);
            e.printStackTrace();
        }


        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return activity.getResources().getString(R.string.just_now);
        } else if (diff < 2 * MINUTE_MILLIS) {
            return activity.getResources().getString(R.string.a_minute);
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " " + activity.getResources().getString(R.string.minute);
        } else if (diff < 90 * MINUTE_MILLIS) {
            return activity.getResources().getString(R.string.an_hour);
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " " + activity.getResources().getString(R.string.hour);
        } else if (diff < 48 * HOUR_MILLIS) {
            return activity.getResources().getString(R.string.yesterday);
        } else {
            return diff / DAY_MILLIS + " " + activity.getResources().getString(R.string.day_ago);
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}