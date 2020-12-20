package com.uriallab.haat.haat.UI.Activities.utilsAndAccountInfo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.Utilities.RealPathUtil;
import com.uriallab.haat.haat.Utilities.camera.Camera;
import com.uriallab.haat.haat.databinding.ActivityContactUsBinding;
import com.uriallab.haat.haat.viewModels.ContactUsViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import static com.uriallab.haat.haat.Utilities.camera.Camera.CAMERA_REQUEST;
import static com.uriallab.haat.haat.Utilities.camera.Camera.GALLERY_REQUEST;
import static com.uriallab.haat.haat.Utilities.camera.Camera.currentPhotoPath;

public class ContactUsActivity extends AppCompatActivity {

    private ActivityContactUsBinding binding;

    private ContactUsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contact_us);

        viewModel = new ContactUsViewModel(this);

        binding.setContactUsVM(viewModel);

        binding.openWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.haat.com.sa"));
                startActivity(browserIntent);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
                try {

                    Bitmap bitmapImage;

                    try {
                        bitmapImage = Camera.resizeBitmap(this, BitmapFactory.decodeFile(RealPathUtil.getRealPath(this, Uri.parse(currentPhotoPath))));
                    } catch (Exception e) {
                        bitmapImage = Camera.resizeBitmap(this, BitmapFactory.decodeFile(Camera.getRealPathFromURI(this, Uri.parse(currentPhotoPath))));
                        e.printStackTrace();
                    }

                    viewModel.selectedImg.setImageBitmap(bitmapImage);

                    viewModel.image = Camera.convertBitmapToBase64(bitmapImage);

                } catch (Exception e) {
                    Log.e("IMAGE", "CAMERA_REQUEST Exception: " + e.getMessage());
                    e.printStackTrace();
                }

            } else if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
                try {
                    Uri imageUri = data.getData();
                    Bitmap bitmapImage;

                    try {
                        bitmapImage = Camera.resizeBitmap(this, BitmapFactory.decodeFile(RealPathUtil.getRealPath(this, imageUri)));
                    } catch (Exception e) {
                        bitmapImage = Camera.resizeBitmap(this, BitmapFactory.decodeFile(Camera.getRealPathFromURI(this, imageUri)));
                        e.printStackTrace();
                    }

                    viewModel.selectedImg.setImageBitmap(bitmapImage);

                    viewModel.image = Camera.convertBitmapToBase64(bitmapImage);

                } catch (Exception e) {
                    Log.e("IMAGE", "GALLERY_REQUEST Exception: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}