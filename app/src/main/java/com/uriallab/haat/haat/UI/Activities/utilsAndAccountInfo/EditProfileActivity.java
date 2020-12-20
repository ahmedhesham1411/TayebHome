package com.uriallab.haat.haat.UI.Activities.utilsAndAccountInfo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.squareup.picasso.Picasso;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.LoginSession;
import com.uriallab.haat.haat.Utilities.RealPathUtil;
import com.uriallab.haat.haat.Utilities.camera.Camera;
import com.uriallab.haat.haat.databinding.ActivityEditProfileBinding;
import com.uriallab.haat.haat.viewModels.EditProfileViewModel;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import static com.uriallab.haat.haat.Utilities.camera.Camera.CAMERA_REQUEST;
import static com.uriallab.haat.haat.Utilities.camera.Camera.GALLERY_REQUEST;
import static com.uriallab.haat.haat.Utilities.camera.Camera.currentPhotoPath;

public class EditProfileActivity extends AppCompatActivity {

    private EditProfileViewModel viewModel;
    private ActivityEditProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile);

        viewModel = new EditProfileViewModel(this);

        iconsColor();

        Picasso.get().load(APIModel.BASE_URL+LoginSession.getUserData(this).getResult().getUserData().getUser_ImgUrl()).
                placeholder(R.drawable.user_img).into(binding.profileImg);

        binding.setEditProfileVM(viewModel);

        List<String> personTypeList = new ArrayList<>();
        personTypeList.add(getResources().getString(R.string.choose_gender));
        personTypeList.add(getResources().getString(R.string.male));
        personTypeList.add(getResources().getString(R.string.female));

        ArrayAdapter<String> adapterPersonType = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, personTypeList);
        adapterPersonType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.genderSpinner.setAdapter(adapterPersonType);

        binding.genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.gender = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (LoginSession.getUserData(this).getResult().getUserData().getUser_GenderID() == 1)
            binding.genderSpinner.setSelection(1);
        else if (LoginSession.getUserData(this).getResult().getUserData().getUser_GenderID() == 2)
            binding.genderSpinner.setSelection(2);
        else
            binding.genderSpinner.setSelection(0);

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

                    binding.profileImg.setImageBitmap(bitmapImage);

                    viewModel.profileImgObservable.set(Camera.convertBitmapToBase64(bitmapImage));

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

                    binding.profileImg.setImageBitmap(bitmapImage);

                    viewModel.profileImgObservable.set(Camera.convertBitmapToBase64(bitmapImage));

                } catch (Exception e) {
                    Log.e("IMAGE", "GALLERY_REQUEST Exception: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void iconsColor() {
        binding.profileImg.setImageResource(R.drawable.user_img);
        binding.arrowName.setImageResource(R.drawable.edit);
        binding.arrowEmail.setImageResource(R.drawable.edit);
        //binding.arrowPhone.setImageResource(R.drawable.edit);
        binding.arrowBirthday.setImageResource(R.drawable.edit);
        binding.spinnerArrow.setImageResource(R.drawable.arrow_left);
        // binding.profileImg.setColorFilter(getResources().getColor(R.color.colorTextHint), PorterDuff.Mode.SRC_ATOP);
        binding.arrowName.setColorFilter(getResources().getColor(R.color.colorTextHint), PorterDuff.Mode.SRC_ATOP);
        binding.arrowEmail.setColorFilter(getResources().getColor(R.color.colorTextHint), PorterDuff.Mode.SRC_ATOP);
        //binding.arrowPhone.setColorFilter(getResources().getColor(R.color.colorTextHint), PorterDuff.Mode.SRC_ATOP);
        binding.arrowBirthday.setColorFilter(getResources().getColor(R.color.colorTextHint), PorterDuff.Mode.SRC_ATOP);
        binding.spinnerArrow.setColorFilter(getResources().getColor(R.color.colorTextHint), PorterDuff.Mode.SRC_ATOP);
    }
}