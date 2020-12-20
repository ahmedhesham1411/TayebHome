package com.uriallab.haat.haat.UI.Activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.oppwa.mobile.connect.exception.PaymentError;
import com.oppwa.mobile.connect.exception.PaymentException;
import com.oppwa.mobile.connect.payment.BrandsValidation;
import com.oppwa.mobile.connect.payment.CheckoutInfo;
import com.oppwa.mobile.connect.payment.ImagesRequest;
import com.oppwa.mobile.connect.provider.Connect;
import com.oppwa.mobile.connect.provider.ITransactionListener;
import com.oppwa.mobile.connect.provider.Transaction;
import com.oppwa.mobile.connect.service.ConnectService;
import com.oppwa.mobile.connect.service.IProviderBinder;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.Utilities.IntentClass;
import com.uriallab.haat.haat.Utilities.Utilities;
import com.uriallab.haat.haat.databinding.ActivityPaymentGateBinding;
import com.uriallab.haat.haat.viewModels.PaymentGateViewModel;


public class PaymentGateActivity extends AppCompatActivity implements ITransactionListener {

    public IProviderBinder binder;
    private ServiceConnection serviceConnection;

    private PaymentGateViewModel viewModel;

    public ActivityPaymentGateBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);

        ConfigurationFile.setCurrentLanguage(this, ConfigurationFile.getCurrentLanguage(this));

        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment_gate);

        initBinder();

        Bundle bundle = getIntent().getBundleExtra("data");

        String orderId = bundle.getString("orderID");
        double money = bundle.getDouble("money");
        int type = bundle.getInt("type");
        int hatCardId = bundle.getInt("hatCardId");
        int hatCardQuantity = bundle.getInt("hatCardQuantity");

        viewModel = new PaymentGateViewModel(this, orderId, type, money, hatCardId, hatCardQuantity);

        binding.setPaymentGateVM(viewModel);


        binding.userNameEt.addTextChangedListener(new TextWatcher() {

            private static final int TOTAL_SYMBOLS = 19; // size of pattern 0000-0000-0000-0000
            private static final int TOTAL_DIGITS = 16; // max numbers of digits in pattern: 0000 x 4
            private static final int DIVIDER_MODULO = 5; // means divider position is every 5th symbol beginning with 1
            private static final int DIVIDER_POSITION = DIVIDER_MODULO - 1; // means divider position is every 4th symbol beginning with 0
            private static final char DIVIDER = '-';

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // noop
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // noop
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (!isInputCorrect(s, TOTAL_SYMBOLS, DIVIDER_MODULO, DIVIDER)) {
                        s.replace(0, s.length(), buildCorrectString(getDigitArray(s, TOTAL_DIGITS), DIVIDER_POSITION, DIVIDER));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private boolean isInputCorrect(Editable s, int totalSymbols, int dividerModulo, char divider) {
                boolean isCorrect = s.length() <= totalSymbols; // check size of entered string
                for (int i = 0; i < s.length(); i++) { // check that every element is right
                    if (i > 0 && (i + 1) % dividerModulo == 0) {
                        isCorrect &= divider == s.charAt(i);
                    } else {
                        isCorrect &= Character.isDigit(s.charAt(i));
                    }
                }
                return isCorrect;
            }

            private String buildCorrectString(char[] digits, int dividerPosition, char divider) {
                final StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < digits.length; i++) {
                    if (digits[i] != 0) {
                        formatted.append(digits[i]);
                        if ((i > 0) && (i < (digits.length - 1)) && (((i + 1) % dividerPosition) == 0)) {
                            formatted.append(divider);
                        }
                    }
                }

                return formatted.toString();
            }

            private char[] getDigitArray(final Editable s, final int size) {
                char[] digits = new char[size];
                int index = 0;
                for (int i = 0; i < s.length() && index < size; i++) {
                    char current = s.charAt(i);
                    if (Character.isDigit(current)) {
                        digits[index] = current;
                        index++;
                    }
                }
                return digits;
            }
        });


        binding.userNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 18)
                    requestFocus(1);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        binding.expiryMonth.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 1)
                    requestFocus(2);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        binding.expiryYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 1)
                    requestFocus(3);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        binding.cvv.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 2)
                    requestFocus(4);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });
        binding.holderName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });
    }

    private void requestFocus(int input) {
        String str = "";
        switch (input) {
            case 1:
                str = binding.userNameEt.getText().toString();
                if (!str.isEmpty())
                    binding.expiryMonth.requestFocus();
                break;

            case 2:
                str = binding.expiryMonth.getText().toString();
                if (!str.isEmpty())
                    binding.expiryYear.requestFocus();
                break;

            case 3:
                str = binding.expiryYear.getText().toString();
                if (!str.isEmpty())
                    binding.cvv.requestFocus();
                break;
            case 4:
                str = binding.cvv.getText().toString();
                if (!str.isEmpty())
                    binding.holderName.requestFocus();
                break;
            case 5:
                str = binding.holderName.getText().toString();
                if (!str.isEmpty()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(binding.holderName.getWindowToken(), 0);
                }
                break;
        }
    }

    private void initBinder() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (IProviderBinder) service;

                try {
                    binder.initializeProvider(Connect.ProviderMode.LIVE);
                    binder.addTransactionListener(PaymentGateActivity.this);
                    Log.e("response", "binder\t" + " LIVE");
                } catch (PaymentException ee) {
                    ee.printStackTrace();
                }

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                binder = null;
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, ConnectService.class);

        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unbindService(serviceConnection);
        stopService(new Intent(this, ConnectService.class));
    }

    @Override
    public void brandsValidationRequestSucceeded(BrandsValidation brandsValidation) {
        Log.e("Transaction", "brandsValidationRequestSucceeded");
    }

    @Override
    public void brandsValidationRequestFailed(PaymentError paymentError) {
        Log.e("Transaction", "brandsValidationRequestFailed");
    }

    @Override
    public void imagesRequestSucceeded(ImagesRequest imagesRequest) {
        Log.e("Transaction", "imagesRequestSucceeded");
    }

    @Override
    public void imagesRequestFailed() {
        Log.e("Transaction", "imagesRequestFailed");
    }

    @Override
    public void paymentConfigRequestSucceeded(CheckoutInfo checkoutInfo) {
        Log.e("Transaction", "paymentConfigRequestSucceeded");
    }

    @Override
    public void paymentConfigRequestFailed(PaymentError paymentError) {
        Log.e("Transaction", "paymentConfigRequestFailed");
    }

    @Override
    public void transactionCompleted(final Transaction transaction) {

        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                //Code that uses AsyncHttpClient in your case
                String url = transaction.getRedirectUrl();

                if (url == null) {
                    Log.e("Transaction", "transactionCompleted\t" + url);
                    viewModel.checkPayStatus();
                    //Utilities.toastyError(PaymentGateActivity.this, getResources().getString(R.string.try_again_after_half_hour));
                } else {
                    Log.e("Transaction", "transactionCompleted\t" + url);

                    // TODO: 7/16/2020
                    Bundle bundle = new Bundle();
                    bundle.putString("url", url);
                    IntentClass.goToStartForResult(PaymentGateActivity.this, ConfirmPaymentActivity.class, 101, bundle);

//                    WebView webView = new WebView(PaymentGateActivity.this);
//                    webView.setWebViewClient(new WebViewClient());
//                    webView.loadUrl(url);
//                    binding.checkoutWebview.setVisibility(View.VISIBLE);
//                    binding.linPayment.setVisibility(View.GONE);
//                    binding.checkoutWebview.addView(webView);


//                    try {
//                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                        startActivityForResult(intent, 200);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }


            }
        };
        mainHandler.post(myRunnable);
    }

    @Override
    public void transactionFailed(Transaction transaction, PaymentError paymentError) {
        Log.e("Transaction", "transactionFailed" + "\n" + paymentError.getErrorInfo() + "\n" + paymentError.getErrorMessage() + "\n" + paymentError.getErrorCode());
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //  viewModel.checkPayStatus();

//        if (intent.getScheme().equals("haat")) {
//            String checkoutId = intent.getData().getQueryParameter("id");
//
//          //  binding.checkoutWebview.setVisibility(View.GONE);
//            //binding.linPayment.setVisibility(View.GONE);
//
//            viewModel.checkPayStatus();
//
////            /* request payment status */
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            viewModel.checkPayStatus();
        } else if (requestCode == 200) {
            viewModel.checkPayStatus();
        }

//        if (requestCode == 10) {
//            String resultDisplayStr;
//            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
//                CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
//
//                // Never log a raw card number. Avoid displaying it, but if necessary use getFormattedCardNumber()
//                resultDisplayStr = "Card Number: " + scanResult.getRedactedCardNumber() + "\n";
//
//                viewModel.cardNumber.set(scanResult.getRedactedCardNumber().replace(" ", ""));
//
//                // Do something with the raw number, e.g.:
//                // myService.setCardNumber( scanResult.cardNumber );
//
//                if (scanResult.isExpiryValid()) {
//                    resultDisplayStr += "Expiration Date: " + scanResult.expiryMonth + "/" + scanResult.expiryYear + "\n";
//                    viewModel.expiryMonth.set(String.valueOf(scanResult.expiryMonth));
//                    viewModel.expiryYear.set(String.valueOf(scanResult.expiryYear).substring(2));
//                }
//
//                if (scanResult.cvv != null) {
//                    // Never log or display a CVV
//                    resultDisplayStr += "CVV has " + scanResult.cvv.length() + " digits.\n";
//
//                    viewModel.passwordCode.set(scanResult.cvv);
//                }
//
//                if (scanResult.cardholderName != null) {
//                    // Never log or display a CVV
//                    resultDisplayStr += "CVV has " + scanResult.cardholderName.length() + " .\n";
//
//                    viewModel.cardName.set(scanResult.cardholderName);
//                }
//
//
//                if (scanResult.postalCode != null) {
//                    resultDisplayStr += "Postal Code: " + scanResult.postalCode + "\n";
//                }
//            } else {
//                resultDisplayStr = "Scan was canceled.";
//            }
//            Utilities.toastySuccess(this, resultDisplayStr);
//            // do something with resultDisplayStr, maybe display it in a textView
//            // resultTextView.setText(resultDisplayStr);
//        }
    }

    @Override
    public void onBackPressed() {
        Utilities.hideKeyboard(this);
        Intent returnIntent = new Intent();
        returnIntent.putExtra("isPayed", false);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}