package com.uriallab.haat.haat.UI.Activities.Auth;

import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;

import androidx.databinding.DataBindingUtil;

import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.UI.Activities.BaseActivity;
import com.uriallab.haat.haat.databinding.ActivityCodeBinding;
import com.uriallab.haat.haat.viewModels.CodeViewModel;

public class CodeActivity extends BaseActivity {

    public ActivityCodeBinding binding;

    private CodeViewModel viewModel;

//    private UpdateOTPReceiver mUpdateOtpReceiver;
//    private SmsListener mSmsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_code);

        binding.durationTxt.setPaintFlags(binding.durationTxt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        Bundle bundle = getIntent().getBundleExtra("data");

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
//                ActivityCompat.checkSelfPermission(this,
//                        Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{
//                            Manifest.permission.READ_SMS,
//                            Manifest.permission.RECEIVE_SMS
//                    }, 213);
//        }
//
//        final String DISPLAY_MESSAGE_ACTION = getPackageName() + ".CodeSmsReceived";
//        try {
//            unregisterReceiver(mHandleMessageReceiver);
//        } catch (Exception e) {
//        }
//        registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));


        viewModel = new CodeViewModel(this, bundle.getString("code"), bundle.getString("phone"), bundle.getBoolean("isRegistered"));

        binding.setCodeVM(viewModel);

        binding.num1Edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                requestFocus(1);
            }
        });

        binding.num2Edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                requestFocus(2);
            }
        });

        binding.num3Edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                requestFocus(3);
            }
        });

        binding.num4Edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                requestFocus(4);
            }
        });
        binding.num5Edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                requestFocus(5);
            }
        });
    }

//    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent != null && intent.hasExtra("code")) {
//                String yourOTPcode = intent.getStringExtra("code");
//                Log.e("OTP", yourOTPcode);
//            }
//        }
//    };

    public void requestFocus(int input) {
        String str = "";
        switch (input) {
            case 1:
                str = binding.num1Edt.getText().toString();
                if (!str.isEmpty())
                    binding.num2Edt.requestFocus();
                break;

            case 2:
                str = binding.num2Edt.getText().toString();
                if (!str.isEmpty())
                    binding.num3Edt.requestFocus();
                break;

            case 3:
                str = binding.num3Edt.getText().toString();
                if (!str.isEmpty())
                    binding.num4Edt.requestFocus();
                break;
            case 4:
                str = binding.num4Edt.getText().toString();
                if (!str.isEmpty())
                    binding.num5Edt.requestFocus();
                break;
            case 5:
                str = binding.num5Edt.getText().toString();
                if (!str.isEmpty()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(binding.num5Edt.getWindowToken(), 0);
                    viewModel.checkCodeAndGo();
                }
                break;
        }
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        mUpdateOtpReceiver = new UpdateOTPReceiver();
//        registerReceiver(mUpdateOtpReceiver, new IntentFilter("UPDATE_OTP"));
//        registerSMSReceiver();
//    }
//
//    private void registerSMSReceiver() {
//        mSmsReceiver = new SmsListener();
//        registerReceiver(mSmsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
//    }

//    private class UpdateOTPReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent != null) {
//                String msg = intent.getStringExtra("msg");
//                Toast.makeText(CodeActivity.this,"Message: "+msg,Toast.LENGTH_LONG).show();
//                String n1 = msg.substring(1, 2);
//                Log.e("OTP_CHAR", "CHAR1\t"+n1);
//                String n2 = msg.substring(2, 3);
//                Log.e("OTP_CHAR", "CHAR2\t"+n2);
//                String n3 = msg.substring(3, 4);
//                Log.e("OTP_CHAR", "CHAR3\t"+n3);
//                String n4 = msg.substring(4, 5);
//                Log.e("OTP_CHAR", "CHAR4\t"+n4);
//                String n5 = msg.substring(5);
//                Log.e("OTP_CHAR", "CHAR5\t"+n5);
//
//                binding.num1Edt.setText(n1);
//                binding.num2Edt.setText(n2);
//                binding.num3Edt.setText(n3);
//                binding.num4Edt.setText(n4);
//                binding.num5Edt.setText(n5);
//                Log.e("OTP", msg);
//            }
//        }
//    }
}