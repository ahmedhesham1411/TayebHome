package com.uriallab.haat.haat.UI.Activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.oppwa.mobile.connect.checkout.dialog.CheckoutActivity;
import com.oppwa.mobile.connect.exception.PaymentError;
import com.oppwa.mobile.connect.provider.Transaction;
import com.oppwa.mobile.connect.provider.TransactionType;
import com.uriallab.haat.haat.DataModels.ChatModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.UI.Adapters.ChatAdapter;
import com.uriallab.haat.haat.UI.Fragments.SendReportBottomSheet;
import com.uriallab.haat.haat.Utilities.RealPathUtil;
import com.uriallab.haat.haat.Utilities.Utilities;
import com.uriallab.haat.haat.Utilities.camera.Camera;
import com.uriallab.haat.haat.databinding.ActivityChatDriverBinding;
import com.uriallab.haat.haat.viewModels.ChatDriverViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import tech.gusavila92.websocketclient.WebSocketClient;

import static com.uriallab.haat.haat.Utilities.camera.Camera.CAMERA_REQUEST;
import static com.uriallab.haat.haat.Utilities.camera.Camera.CAMERA_REQUEST2;
import static com.uriallab.haat.haat.Utilities.camera.Camera.CAMERA_REQUEST3;
import static com.uriallab.haat.haat.Utilities.camera.Camera.GALLERY_REQUEST;
import static com.uriallab.haat.haat.Utilities.camera.Camera.GALLERY_REQUEST3;
import static com.uriallab.haat.haat.Utilities.camera.Camera.currentPhotoPath;

public class ChatDriverActivity extends AppCompatActivity {

    public ActivityChatDriverBinding binding;

    private String orderId;

    private List<Bitmap> imageList = new ArrayList<>();

    private SendReportBottomSheet sendReportBottomSheet;

    private List<ChatModel.ResultEntity.MessagesEntity> chatList = new ArrayList<>();
    private ChatAdapter chatAdapter;

    private ChatDriverViewModel viewModel;

    private static final String AUDIO_RECORDER_FILE_EXT_MP3 = ".mp3";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private int currentFormat = 0;
    private MediaRecorder recorder;
    private String[] file_exts = {AUDIO_RECORDER_FILE_EXT_MP3};
    private String filePathUri;

    public WebSocketClient webSocketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_driver);

        Bundle bundle = getIntent().getBundleExtra("data");

        orderId = bundle.getString("orderId");

        ConfigurationFile.setIsDriverChatActive(this, true);

        viewModel = new ChatDriverViewModel(this, orderId);

        binding.setChatVM(viewModel);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("MyData"));

        binding.recordSound.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.e("RECORD", "Start Recording");
                        startRecording();
                        return true;
                    case MotionEvent.ACTION_UP:
                        Log.e("RECORD", "stop Recording");
                        stopRecording();
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendMessage(false);
        Utilities.hideKeyboard(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        createWebSocketClient();
    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        ConfigurationFile.setIsChatActive(this, false);
        sendMessage(false);
        webSocketClient.close();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ConfigurationFile.setIsDriverChatActive(this, false);
        try {
            chatAdapter.playAudioManager.killMediaPlayer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String text = intent.getStringExtra("text");
            int chatId = intent.getExtras().getInt("chatId");
            String sencerId = intent.getStringExtra("sencerId");
            String url = intent.getStringExtra("url");
            String text_type = intent.getStringExtra("text_type");
            String key2 = intent.getStringExtra("key2");
            Log.d("receiver", "Got message: " + text);

            ChatModel.ResultEntity.MessagesEntity chat = new ChatModel.ResultEntity.MessagesEntity();
            chat.setChatID(chatId);
            chat.setMessage(text);
            chat.setMssge_ImgUrl(url);
            chat.setType(text_type);
            chat.setSender_ID(Integer.parseInt(sencerId));
            chat.setMssge_Time("");

            Uri messageSoundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.new_message);
            MediaPlayer mMediaPlayer = MediaPlayer.create(ChatDriverActivity.this, messageSoundUri);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setLooping(false);
            mMediaPlayer.start();

            updateRecycler(chat);
        }
    };

    public void initRecycler(List<ChatModel.ResultEntity.MessagesEntity> chatListModel) {
        chatList.clear();
        chatList.addAll(chatListModel);
        chatAdapter = new ChatAdapter(this, chatList);
        binding.recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerChat.setAdapter(chatAdapter);
        binding.recyclerChat.scrollToPosition(chatList.size() - 1);
    }

    private void updateRecycler(ChatModel.ResultEntity.MessagesEntity chat) {
        try {
            chatList.add(chat);
            chatAdapter.notifyDataSetChanged();
            binding.recyclerChat.scrollToPosition(chatList.size() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendReport() {
        sendReportBottomSheet = new SendReportBottomSheet(ChatDriverActivity.this, orderId, viewModel.userId);
        sendReportBottomSheet.show(getSupportFragmentManager(), "tag");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

                //  imageList.clear();

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

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    sendReportBottomSheet.initRecyclerLocalImg(imageList);
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

                        sendReportBottomSheet.initRecyclerLocalImg(imageList);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
                try {

                    Bitmap bitmapImage;

                    try {
                        bitmapImage = Camera.resizeBitmap(this, BitmapFactory.decodeFile(RealPathUtil.getRealPath(this, Uri.parse(currentPhotoPath))));
                    } catch (Exception e) {
                        bitmapImage = Camera.resizeBitmap(this, BitmapFactory.decodeFile(Camera.getRealPathFromURI(this, Uri.parse(currentPhotoPath))));
                        e.printStackTrace();
                    }
                    imageList.add(bitmapImage);

                    sendReportBottomSheet.initRecyclerLocalImg(imageList);

                } catch (Exception e) {
                    Log.e("IMAGE", "CAMERA_REQUEST Exception: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (requestCode == 147) {
                if (data.getExtras().getBoolean("sent")) {
                    viewModel.rate();
                }
            } else if (requestCode == CheckoutActivity.REQUEST_CODE_CHECKOUT) {
                switch (resultCode) {
                    case CheckoutActivity.RESULT_OK:
                        /* transaction completed */
                        Transaction transaction = data.getParcelableExtra(CheckoutActivity.CHECKOUT_RESULT_TRANSACTION);

                        /* resource path if needed */
                        //   paymentViewModel.checkPayStatus();
                        String resourcePath = data.getStringExtra(CheckoutActivity.CHECKOUT_RESULT_RESOURCE_PATH);
                        Log.e("response", "RESULT_OK\t" + resourcePath + "");
                        if (transaction.getTransactionType() == TransactionType.SYNC) {
                            Log.e("response", "RESULT_OK\t" + transaction.getTransactionType() + "");
                            /* check the result of synchronous transaction */
                        } else {
                            Log.e("response", "RESULT_OK\telse");
                            /* wait for the asynchronous transaction callback in the onNewIntent() */
                        }

                        break;
                    case CheckoutActivity.RESULT_CANCELED:
                        Log.e("response", "RESULT_CANCELED\t");
                        /* shopper canceled the checkout process */
                        break;
                    case CheckoutActivity.RESULT_ERROR:
                        /* error occurred */
                        PaymentError error = data.getParcelableExtra(CheckoutActivity.CHECKOUT_RESULT_ERROR);
                        Log.e("response", "RESULT_ERROR\t" + error + "\n" + error.getErrorMessage() + "\n" + error.getErrorInfo() + "\n" + error.getErrorCode());
                }
            } else if (requestCode == GALLERY_REQUEST3 && resultCode == RESULT_OK) {

                try {
                    Uri imageUri = data.getData();
                    File img;

                    try {
                        img = Camera.compressImageFile(this, new File(RealPathUtil.getRealPath(this, imageUri)));
                        Log.e("IMAGE", RealPathUtil.getRealPath(this, imageUri));
                    } catch (Exception e) {
                        img = Camera.compressImageFile(this, new File(Camera.getRealPathFromURI(this, imageUri)));
                        Log.e("IMAGE", Camera.getRealPathFromURI(this, imageUri));
                        e.printStackTrace();
                    }
                    viewModel.sendNewMessage(img, 2);

                } catch (Exception e) {
                    Log.e("IMAGE", "GALLERY_REQUEST Exception: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (requestCode == CAMERA_REQUEST3 && resultCode == RESULT_OK) {
                try {

                    File img;
                    try {
                        img = Camera.compressImageFile(this, new File(RealPathUtil.getRealPath(this, Uri.parse(currentPhotoPath))));
                    } catch (Exception e) {
                        img = Camera.compressImageFile(this, new File(Camera.getRealPathFromURI(this, Uri.parse(currentPhotoPath))));
                        e.printStackTrace();
                    }
                    Log.e("IMAGE", String.valueOf(currentPhotoPath));
                    viewModel.sendNewMessage(img, 2);

                } catch (Exception e) {
                    Log.e("IMAGE", "CAMERA_REQUEST Exception: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startRecording() {

        if (ActivityCompat.checkSelfPermission(ChatDriverActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ChatDriverActivity.this, new String[]{Manifest.permission.RECORD_AUDIO},
                    10);
        } else {

            binding.recordingTxt.setVisibility(View.VISIBLE);

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            recorder.setAudioEncodingBitRate(16*44100);
            recorder.setAudioSamplingRate(44100);
            recorder.setOutputFile(getFilename());
            recorder.setOnErrorListener(errorListener);
            recorder.setOnInfoListener(infoListener);

            try {
                recorder.prepare();
                recorder.start();
            } catch (IllegalStateException | IOException e) {
                Log.e("RECORD", "Exception start: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String getFilename() {
        String destPath = Objects.requireNonNull(getExternalFilesDir(null)).getAbsolutePath();
        File file = new File(destPath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }


        filePathUri = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + file_exts[currentFormat]);
        Log.e("RECORD", "filePath: " + filePathUri);

        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + file_exts[currentFormat]);
    }

    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            Log.e("RECORD", "Error: " + what + ", " + extra);
        }
    };

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            Log.e("RECORD", "Warning: " + what + ", " + extra);
        }
    };

    private void stopRecording() {
        binding.recordingTxt.setVisibility(View.GONE);
        if (null != recorder) {
            try {
                recorder.stop();
                recorder.reset();
                recorder.release();
            } catch (Exception e) {
                Log.e("String_File", e.getMessage() + "\tException");
            }
            viewModel.sendNewMessage(new File(filePathUri), 3);
            recorder = null;
        }
    }

    public void createWebSocketClient() {
        URI uri;
        try {
            // Connect to local host
            uri = new URI("ws://176.9.164.57:501/typing");
        } catch (URISyntaxException e) {
            Log.e("WebSockettyping", "URISyntaxException");
            e.printStackTrace();
            return;
        }
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                Log.e("WebSockettyping", "Session is starting\tid " + orderId);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("RoomId", orderId);

                    jsonObject.put("typing", false);

                } catch (JSONException e) {
                    Log.e("WebSockettyping", "JSONException\t" + e.getMessage());
                    e.printStackTrace();
                }
                webSocketClient.send(jsonObject.toString());
            }

            @Override
            public void onTextReceived(String s) {
                Log.e("WebSockettyping", "Message received\t" + s);
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = null;

                            jsonObject = new JSONObject(message);
                            boolean isTyping = jsonObject.getBoolean("typing");

                            if (isTyping)
                                binding.typingTxt.setVisibility(View.VISIBLE);
                            else
                                binding.typingTxt.setVisibility(View.GONE);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onBinaryReceived(byte[] data) {
                Log.e("WebSockettyping", "onBinaryReceived\t" + data.toString());
            }

            @Override
            public void onPingReceived(byte[] data) {
                Log.e("WebSockettyping", "onPingReceived\t" + data.toString());
            }

            @Override
            public void onPongReceived(byte[] data) {
                Log.e("WebSockettyping", "onPongReceived\t" + data.toString());
            }

            @Override
            public void onException(Exception e) {
                Log.e("WebSockettyping", "onException\t" + e.getMessage());
            }

            @Override
            public void onCloseReceived() {
                Log.e("WebSockettyping", "onCloseReceived");
            }
        };
        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(90000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }

    public void sendMessage(boolean status) {
        try {
            Log.e("WebSockettyping", "Button was clicked");

            // Send button id string to WebSocket Server
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("RoomId", orderId);
                jsonObject.put("typing", status);
            } catch (JSONException e) {
                Log.e("WebSockettyping", "JSONException\t" + e.getMessage());
                e.printStackTrace();
            }
            webSocketClient.send(jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}