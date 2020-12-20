package com.uriallab.haat.haat.UI.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.bumptech.glide.Glide;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.UI.MainActivity;
import com.uriallab.haat.haat.Utilities.GlobalVariables;
import com.uriallab.haat.haat.Utilities.IntentClass;
import com.uriallab.haat.haat.databinding.ActivitySentSuccessfullyBinding;
import com.uriallab.haat.haat.viewModels.SentSuccessfullyViewModel;

import androidx.databinding.DataBindingUtil;

public class SentSuccessfullyActivity extends BaseActivity {

    private String orderId = "0", clientId = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySentSuccessfullyBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_sent_successfully);

        GlobalVariables.FINISH_ACTIVITY = true;

        Bundle bundle = getIntent().getBundleExtra("data");

        if (bundle.getInt("sentType") == 1) {
            Glide.with(this).load(R.drawable.succes_order).into(binding.doneImg);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SentSuccessfullyActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("key", "-2");
                    startActivity(intent);
                }
            }, 5000);
        } else{
            orderId = bundle.getString("orderId");
            clientId = bundle.getString("clientId");
            Glide.with(this).load(R.drawable.succes_order).into(binding.doneImg);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SentSuccessfullyActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("key", "-2");
                    startActivity(intent);
                }
            }, 5000);
          /*  orderId = bundle.getString("orderId");
            clientId = bundle.getString("clientId");
            Glide.with(this).load(R.drawable.succes_order).into(binding.doneImg);*/
        }

        binding.setSentSuccessfullyVM(new SentSuccessfullyViewModel(this, bundle.getInt("sentType"), orderId, clientId));
    }
}