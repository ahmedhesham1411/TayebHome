package com.uriallab.haat.haat.UI.Activities.utilsAndAccountInfo;

import android.os.Bundle;
import android.view.View;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.DataModels.ComplaintModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.databinding.ActivityComplaintDetailsBinding;
import com.uriallab.haat.haat.viewModels.ComplaintDetailsViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class ComplaintDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityComplaintDetailsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_complaint_details);

        Gson gson = new Gson();
        ComplaintModel.ResultBean.ComplaintsBean itemsBean = gson.fromJson(getIntent().getStringExtra("myjson"), ComplaintModel.ResultBean.ComplaintsBean.class);

        Picasso.get().load(itemsBean.getOrd_Shop_ImgUrl()).into(binding.orderImg);

        binding.userName.setText(itemsBean.getFrom_Full_Name());
        binding.notesId.setText(itemsBean.getComplaint_Desc());
        binding.shopName.setText(itemsBean.getOrd_Shop_Nm());
        binding.complaintId.setText(itemsBean.getComplaintUID()+"");
        binding.complaintDetails.setText(itemsBean.getComplaint_Desc());

        try {
            if (!itemsBean.getComplaint_ImgUrl().equals("error")) {
                Picasso.get().load(APIModel.BASE_URL + itemsBean.getComplaint_ImgUrl()).into(binding.attachImg);
                binding.noAttachment.setVisibility(View.GONE);
                binding.attachImg.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
        }




        if (ConfigurationFile.getCurrentLanguage(this).equals("ar"))
            binding.reasonId.setText(itemsBean.getReason_Ar_Nm());
        else
            binding.reasonId.setText(itemsBean.getReason_En_Nm());

        Picasso.get().load(APIModel.BASE_URL + itemsBean.getFrom_Img_Url()).placeholder(R.drawable.profile).into(binding.profileImg);

        binding.setComplaintVM(new ComplaintDetailsViewModel(this, itemsBean));

        binding.appFeadback.setText(itemsBean.getApp_Feadback());

        if (itemsBean.getComplaint_Status_ID() == 1) {
            binding.details.setText(getString(R.string.waiting_processed));
            binding.details.setTextColor(getResources().getColor(R.color.colorMoov));
        } else {
            binding.details.setText(getString(R.string.proessed));
            binding.details.setTextColor(getResources().getColor(R.color.colorBlue));
        }
    }
}