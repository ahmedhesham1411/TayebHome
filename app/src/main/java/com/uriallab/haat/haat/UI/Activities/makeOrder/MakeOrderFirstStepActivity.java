package com.uriallab.haat.haat.UI.Activities.makeOrder;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.uriallab.haat.haat.DataModels.StoreProductsModel;
import com.uriallab.haat.haat.Interfaces.DeleteImage;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.UI.Adapters.ImagesAdapter;
import com.uriallab.haat.haat.UI.Adapters.StoreProductsAdapter;
import com.uriallab.haat.haat.Utilities.GlobalVariables;
import com.uriallab.haat.haat.Utilities.RealPathUtil;
import com.uriallab.haat.haat.Utilities.camera.Camera;
import com.uriallab.haat.haat.databinding.ActivityMakeOrderFirstStepBinding;
import com.uriallab.haat.haat.viewModels.MakeOrderFirstStepViewModel;

import java.util.ArrayList;
import java.util.List;

import static com.uriallab.haat.haat.Utilities.camera.Camera.GALLERY_MUTI_REQUEST;

public class MakeOrderFirstStepActivity extends AppCompatActivity implements DeleteImage {

    private ActivityMakeOrderFirstStepBinding binding;

    private MakeOrderFirstStepViewModel viewModel;

    private List<Bitmap> imageList = new ArrayList<>();

    private String storeName, shopImg;

    private boolean isService = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_make_order_first_step);

        binding.couponImg.setImageResource(R.drawable.coupon);
        binding.addCoupon.setImageResource(R.drawable.coupon);
        binding.couponImg.setColorFilter(getResources().getColor(R.color.colorBlue), PorterDuff.Mode.SRC_ATOP);
        binding.addCoupon.setColorFilter(getResources().getColor(R.color.colorTextHint), PorterDuff.Mode.SRC_ATOP);

        Bundle bundle = getIntent().getBundleExtra("data");

        if (!bundle.getBoolean("isService")) {
            isService = false;
            //storeName = bundle.getString("storeName");
            shopImg = bundle.getString("shopImg");
        }

        int categoryId = bundle.getInt("categoryId");

        viewModel = new MakeOrderFirstStepViewModel(this, storeName, categoryId, shopImg, isService);

        binding.setMakeOrderVM(viewModel);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (GlobalVariables.FINISH_ACTIVITY)
            finish();
    }

    public void initRecyclerLocalImg() {
        Log.e("ListSize", imageList.size() + " bitmap");
        Log.e("ListSize", viewModel.listImg.size() + " base");
        ImagesAdapter imagesAdapter = new ImagesAdapter(this, imageList, this);
        binding.recyclerAttachment.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerAttachment.setAdapter(imagesAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == GALLERY_MUTI_REQUEST && resultCode == RESULT_OK) {

                //viewModel.listImg.clear();

                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        try {
                            Uri imageUri = clipData.getItemAt(i).getUri();

                            Bitmap bitmapImage;

                            try {
                                bitmapImage = Camera.resizeBitmap(this, BitmapFactory.decodeFile(RealPathUtil.getRealPath(this, imageUri)));
                            } catch (Exception e) {
                                bitmapImage = Camera.resizeBitmap(this, BitmapFactory.decodeFile(Camera.getRealPathFromURI(this, imageUri)));
                                e.printStackTrace();
                            }

                            imageList.add(bitmapImage);

                            viewModel.listImg.add(Camera.convertBitmapToBase64(bitmapImage));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    initRecyclerLocalImg();
                } else {
                    try {
                        Uri imageUri = data.getData();

                        Bitmap bitmapImage;

                        try {
                            bitmapImage = Camera.resizeBitmap(this, BitmapFactory.decodeFile(RealPathUtil.getRealPath(this, imageUri)));
                        } catch (Exception e) {
                            bitmapImage = Camera.resizeBitmap(this, BitmapFactory.decodeFile(Camera.getRealPathFromURI(this, imageUri)));
                            e.printStackTrace();
                        }

                        Log.e("Base64", Camera.convertBitmapToBase64(bitmapImage));
                        imageList.add(bitmapImage);

                        viewModel.listImg.add(Camera.convertBitmapToBase64(bitmapImage));

                        initRecyclerLocalImg();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteImage(int pos) {
        viewModel.listImg.remove(pos);
    }
}