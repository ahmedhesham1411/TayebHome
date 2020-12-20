package com.uriallab.haat.haat.UI.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;

import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.Utilities.RealPathUtil;
import com.uriallab.haat.haat.Utilities.Utilities;
import com.uriallab.haat.haat.Utilities.camera.Camera;
import com.uriallab.haat.haat.databinding.ActivityCreateInvoiceBinding;
import com.uriallab.haat.haat.viewModels.CreateInvoiceViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import static com.uriallab.haat.haat.Utilities.camera.Camera.CAMERA_REQUEST;
import static com.uriallab.haat.haat.Utilities.camera.Camera.CAMERA_REQUEST2;
import static com.uriallab.haat.haat.Utilities.camera.Camera.currentPhotoPath;

public class CreateInvoiceActivity extends AppCompatActivity {

    private CreateInvoiceViewModel viewModel;
    private ActivityCreateInvoiceBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_invoice);

        Bundle bundle = getIntent().getBundleExtra("data");

        String name = Utilities.getColoredSpanned(getString(R.string.alert), "#B72222");
        String surName = Utilities.getColoredSpanned(getString(R.string.alert_),"#2FA532");

        binding.alertTxt.setText(Html.fromHtml(name+" "+surName));

        viewModel = new CreateInvoiceViewModel(this, bundle.getDouble("price"), bundle.getString("orderId"));

        binding.setCreateInvoiceVM(viewModel);

      /*  super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_invoice);

        Bundle bundle = getIntent().getBundleExtra("data");

        binding.addImg.setImageResource(R.drawable.coupon);
        binding.addImg.setColorFilter(getResources().getColor(R.color.colorBlue), PorterDuff.Mode.SRC_ATOP);

        viewModel = new CreateInvoiceViewModel(this, bundle.getDouble("price"), bundle.getString("orderId"));

        binding.setCreateInvoiceVM(viewModel);

        binding.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.billImg.setVisibility(View.GONE);
                binding.delete.setVisibility(View.GONE);

                viewModel.imgObservable.set("");
            }
        });*/
    }

 /*   @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == CAMERA_REQUEST2 && resultCode == RESULT_OK) {
                try {

                    Bitmap bitmapImage;

                    try {
                        bitmapImage = Camera.resizeBitmap(this, BitmapFactory.decodeFile(RealPathUtil.getRealPath(this, Uri.parse(currentPhotoPath))));
                    } catch (Exception e) {
                        bitmapImage = Camera.resizeBitmap(this, BitmapFactory.decodeFile(Camera.getRealPathFromURI(this, Uri.parse(currentPhotoPath))));
                        e.printStackTrace();
                    }

                    binding.billImg.setVisibility(View.VISIBLE);
                    binding.delete.setVisibility(View.VISIBLE);
                    binding.billImg.setImageBitmap(bitmapImage);

                    viewModel.imgObservable.set(Camera.convertBitmapToBase64(bitmapImage));

                } catch (Exception e) {
                    Log.e("IMAGE", "CAMERA_REQUEST Exception: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}