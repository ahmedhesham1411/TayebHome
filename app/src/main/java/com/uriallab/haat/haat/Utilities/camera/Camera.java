package com.uriallab.haat.haat.Utilities.camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.nanchen.compresshelper.CompressHelper;
import com.uriallab.haat.haat.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;


public class Camera {

    public static AlertDialog desDialog = null;
    public static Uri fileUri;
    public static final int CAMERA_REQUEST = 1888;
    public static final int CAMERA_REQUEST2 = 2888;
    public static final int CAMERA_REQUEST3 = 2889;
    public static final int GALLERY_REQUEST = 100;
    public static final int GALLERY_REQUEST2 = 110;
    public static final int GALLERY_REQUEST3 = 111;
    public static final int GALLERY_MUTI_REQUEST = 101;
    public static final int VIDEO_REQUEST = 999;

    public static String currentPhotoPath;


    public static void showGallery(final Fragment activity) {
        LayoutInflater inflater = (LayoutInflater) activity.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity.getContext());
        View dialogView = inflater.inflate(R.layout.gallery, null);
        builder.setView(dialogView);
        dialogView.findViewById(R.id.Camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage(activity.getActivity());
                desDialog.cancel();
            }
        });

        dialogView.findViewById(R.id.gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activity.startActivityForResult(i, GALLERY_REQUEST);
                desDialog.cancel();
            }
        });

        desDialog = builder.create();
        desDialog.show();
    }

    public static void showGalleryFromActivity(final Activity activity) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = inflater.inflate(R.layout.gallery, null);
        builder.setView(dialogView);
        dialogView.findViewById(R.id.Camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage(activity);
                desDialog.cancel();
            }
        });

        dialogView.findViewById(R.id.gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                activity.startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
                desDialog.cancel();
            }
        });

        desDialog = builder.create();
        desDialog.show();
    }

    public static void showGalleryFromActivity2(final Activity activity) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = inflater.inflate(R.layout.gallery, null);
        builder.setView(dialogView);
        dialogView.findViewById(R.id.Camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage2(activity);
                desDialog.cancel();
            }
        });

        dialogView.findViewById(R.id.gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                activity.startActivityForResult(photoPickerIntent, GALLERY_REQUEST2);
                desDialog.cancel();
            }
        });

        desDialog = builder.create();
        desDialog.show();
    }

    public static void showGalleryFromActivity3(final Activity activity) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = inflater.inflate(R.layout.gallery, null);
        builder.setView(dialogView);
        dialogView.findViewById(R.id.Camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage3(activity);
                desDialog.cancel();
            }
        });

        dialogView.findViewById(R.id.gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                activity.startActivityForResult(photoPickerIntent, GALLERY_REQUEST3);
                desDialog.cancel();
            }
        });

        desDialog = builder.create();
        desDialog.show();
    }

    public static void getPhotoFromCamera2(final Activity activity) {
        captureImage2(activity);
    }

    public static void showGalleryMultiple(final Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        activity.startActivityForResult(intent, GALLERY_MUTI_REQUEST);
    }

    public static void showGalleryMultipleFragment(final Fragment activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        activity.startActivityForResult(intent, GALLERY_MUTI_REQUEST);
    }

    public static void showVideo(final Fragment activity) {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, "Select Video"), VIDEO_REQUEST);
    }

    public static void showVideo(final Activity activity) {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, "Select Video"), VIDEO_REQUEST);
    }

    public static String convert(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP).replace("\n\t", "");
    }

    public static List<String> convert(List<Bitmap> bitmaps) {
        ArrayList<String> imgs = new ArrayList<>();
        for (int i = 0; i < bitmaps.size(); i++) {
            imgs.add(convertBitmapToBase64(bitmaps.get(i)));
        }
        return imgs;
    }

    public static String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP).replace("\n\t", "");
    }

    public static ArrayList<Bitmap> resizeBitmapList(List<Bitmap> bitmaps) {

        ArrayList<Bitmap> newBitmaps = new ArrayList<>();
        for (int i = 0; i < bitmaps.size(); i++) {

            Matrix matrix = new Matrix();
            matrix.postScale(1f, 1f);
            int w = (bitmaps.get(i).getWidth() / 2) - (175);
            int h = (bitmaps.get(i).getHeight() / 2) - (50);

            newBitmaps.add(Bitmap.createBitmap(bitmaps.get(i), w, h, 350, 200, matrix, true));
        }


        return newBitmaps;
    }

    public static Bitmap resizeBitmap(Bitmap bitmap) {

        Matrix matrix = new Matrix();
        matrix.postScale(1f, 1f);
        int w = (bitmap.getWidth() / 2) - (175);
        int h = (bitmap.getHeight() / 2) - (50);

        return Bitmap.createBitmap(bitmap, w, h, 350, 200, matrix, true);
    }

    public static Bitmap convertBase64ToBitmap(String base64) {
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    public static String getRealPathFromURI(Activity activity, Uri contentURI) {
        String result = null;

        Cursor cursor = activity.getContentResolver().query(contentURI, null, null, null, null);

        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            if (cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
            }
            cursor.close();
        }
        return result;
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public static String getPath(Activity activity, Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULL POINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    private static File createImageFile(Activity activity) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (Exception e) {

        }
        return image;
    }

    public static void captureImage(Activity activity) {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile(activity);
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    currentPhotoPath = photoFile.getAbsolutePath();
                    Uri photoURI = FileProvider.getUriForFile(activity,
                            "com.hathat.android.fileprovider",
                            photoFile);

                    List<ResolveInfo> resInfoList = activity.getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        activity.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    activity.startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                }
            } else {
                Toast.makeText(activity, "error_no_camera", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void captureImage2(Activity activity) {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile(activity);
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    currentPhotoPath = photoFile.getAbsolutePath();
                    Uri photoURI = FileProvider.getUriForFile(activity,
                            "com.hathat.android.fileprovider",
                            photoFile);

                    List<ResolveInfo> resInfoList = activity.getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        activity.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    activity.startActivityForResult(takePictureIntent, CAMERA_REQUEST2);
                }
            } else {
                Toast.makeText(activity, "error_no_camera", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void captureImage3(Activity activity) {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile(activity);
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    currentPhotoPath = photoFile.getAbsolutePath();
                    Uri photoURI = FileProvider.getUriForFile(activity,
                            "com.hathat.android.fileprovider",
                            photoFile);

                    List<ResolveInfo> resInfoList = activity.getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        activity.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    activity.startActivityForResult(takePictureIntent, CAMERA_REQUEST3);
                }
            } else {
                Toast.makeText(activity, "error_no_camera", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Bitmap decodeUri(Activity activity, Uri selectedImage) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(activity.getContentResolver()
                .openInputStream(selectedImage), null, o);
        final int REQUIRED_SIZE = 72;
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeStream(activity.getContentResolver()
                .openInputStream(selectedImage), null, o2);
        return bitmap;
    }


    public static Bitmap resizeBitmap(Activity activity, Bitmap bitmapImage) {
        float aspectRatio = bitmapImage.getWidth() /
                (float) bitmapImage.getHeight();
        int width = 150;
        int height = Math.round(width / aspectRatio);
        Bitmap bitmap = Bitmap.createScaledBitmap(bitmapImage, width, height, false);
        Log.e("SIZE_BITMAP", bitmap.getWidth() + " " + bitmap.getHeight());


        int imageWidth = bitmapImage.getWidth();
        int imageHeight = bitmapImage.getHeight();
        int resultWidth;
        int resultHeight;
        int reqWidth = 200;
        int reqHeight = 250;

        int diffWidth = imageWidth - reqWidth;
        int diffHeight = imageHeight - reqHeight;

        if (diffHeight < 0 || diffWidth < 0) {
            resultWidth = imageWidth;
            resultHeight = imageHeight;
        } else {
            if (diffWidth < diffHeight) {
                resultWidth = reqWidth;
                resultHeight = Math.round(((resultWidth * imageHeight) / imageWidth));
            } else {
                resultHeight = reqHeight;
                resultWidth = Math.round(((resultHeight * imageWidth) / imageHeight));
            }
        }
        return Bitmap.createScaledBitmap(bitmapImage, resultWidth, resultHeight, false);
    }

    public static File compressImageFile(Activity activity, File file) {
        return new CompressHelper.Builder(activity)
                .setMaxWidth(720f)
                .setMaxHeight(960f)
                .setQuality(90)
                .setFileName((String.valueOf(System.currentTimeMillis() / 1000)) + ".jpg")
                .setCompressFormat(Bitmap.CompressFormat.JPEG).build()
                .compressToFile(file);
    }
}