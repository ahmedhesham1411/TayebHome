package com.uriallab.haat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.databinding.ActivityCancelOrderBinding;

public class CancelOrderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCancelOrderBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_cancel_order);

        binding.setCancelReasonVM(new CancelReasonViewModel(this));
    }
}