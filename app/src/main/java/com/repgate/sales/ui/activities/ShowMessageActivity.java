package com.repgate.sales.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nightonke.boommenu.BoomMenuButton;
import com.repgate.sales.R;
import com.repgate.sales.common.AppPreferences;
import com.repgate.sales.common.Constants;
import com.repgate.sales.data.AttachFileData;
import com.repgate.sales.data.SendResponseData;
import com.repgate.sales.http.HttpInterface;
import com.repgate.sales.service.SalesGcmListenerService;
import com.repgate.sales.view.RoundedImageView;
import com.squareup.picasso.Picasso;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ShowMessageActivity extends Activity implements View.OnClickListener {
    private final static String TAG = "ShowMessageActivity";
    AppPreferences prefs;

    private String mUserId, title, senderId, senderName, senderImg, receiverId, receiverName, createAt, pdfLink, videoLink, msgBody, msgId;

    private TextView txtTitleHeader, txtTitle, txtSender, txtDate, txtBody, txtFrom, txtTo;
    private LinearLayout titleLayer, attachLayout;
    private Button btnBack, btnReply, btnForward, btnActDelete;
    private RoundedImageView imgSender;
    private View attachDivider;
    private ImageView imgLogo;
    private boolean is_new = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_message);

        prefs = new AppPreferences(this);
        mUserId = getIntent().getStringExtra(Constants.PARAM_USER_ID);
        msgId = getIntent().getStringExtra(Constants.PARAM_MESSAGE_ID);
        title = getIntent().getStringExtra(Constants.PARAM_MESSAGE_TITLE);
        senderId = getIntent().getStringExtra(Constants.PARAM_MESSAGE_SENDER_ID);
        senderImg = getIntent().getStringExtra(Constants.PARAM_MESSAGE_SENDER_IMAGE);
        senderName = getIntent().getStringExtra(Constants.PARAM_MESSAGE_SENDER_NAME);
        receiverId = getIntent().getStringExtra(Constants.PARAM_MESSAGE_RECEIVER_ID);
        receiverName = getIntent().getStringExtra(Constants.PARAM_MESSAGE_RECEIVER_NAME);
        createAt = getIntent().getStringExtra(Constants.PARAM_MESSAGE_DATE);
        pdfLink = getIntent().getStringExtra(Constants.PARAM_MESSAGE_PDF_ATTACH);
        videoLink = getIntent().getStringExtra(Constants.PARAM_MESSAGE_VIDEO_ATTACH);
        msgBody = getIntent().getStringExtra(Constants.PARAM_MESSAGE_BODY);
        is_new = getIntent().getBooleanExtra(Constants.PARAM_MESSAGE_IS_NEW, false);

        BoomMenuButton bmb = (BoomMenuButton) findViewById(R.id.bmb);
        bmb.setVisibility(View.GONE);

        attachLayout = (LinearLayout) findViewById(R.id.attachLayout);
        attachDivider = findViewById(R.id.attachLayoutDivider);

        imgSender = (RoundedImageView) findViewById(R.id.imgPicture);
        if (senderImg != null && senderImg.isEmpty() == false) {
            Picasso.with(ShowMessageActivity.this)
                    .load(senderImg)
                    .placeholder(R.mipmap.default_avatar)
                    .error(R.mipmap.default_avatar)
                    .fit()
                    .into(imgSender);
        } else {
            imgSender.setImageResource(R.mipmap.default_avatar);
        }

        txtTitle = (TextView) findViewById(R.id.txtContentTitle);
        txtTitle.setText(title);
        txtSender = (TextView) findViewById(R.id.txtNameSender);
        txtSender.setText(senderName);
        txtFrom = (TextView) findViewById(R.id.txtFrom);
        txtFrom.setText(senderName);
        txtTo = (TextView) findViewById(R.id.txtTo);
        txtTo.setText(receiverName);
        txtDate = (TextView) findViewById(R.id.txtDate);
        txtDate.setText(createAt);

        if (pdfLink != null && !pdfLink.isEmpty()) {
            String parts[] = pdfLink.split("hufeixiaomi");
            for (int i = 0; i < parts.length; i ++) {
                String filename = parts[i].substring(parts[i].lastIndexOf("/") + 1);
                AttachFileData attachData = new AttachFileData();
                attachData.fileName = filename;
                if (filename.contains("pdf")) {
                    attachData.fileType = CreateMessageActivity.FILE_TYPE_PDF;
                } else if (filename.contains("png") || filename.contains("jpg")) {
                    attachData.fileType = CreateMessageActivity.FILE_TYPE_IMAGE;
                } else if (filename.contains("mp4")) {
                    attachData.fileType = CreateMessageActivity.FILE_TYPE_VIDEO;
                } else {
                    attachData.fileType = CreateMessageActivity.FILE_TYPE_VIDEO;
                }
                attachData.url = parts[i];

                addAttachView(attachData);
            }
        } else {
            attachLayout.setVisibility(View.GONE);
            attachDivider.setVisibility(View.GONE);
        }

        txtBody = (TextView) findViewById(R.id.txtMsgBody);
        txtBody.setText(msgBody);

        btnBack = (Button) findViewById(R.id.action_back);
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnReply = (Button) findViewById(R.id.btnReply);
        btnReply.setOnClickListener(this);
        btnForward = (Button) findViewById(R.id.btnForward);
        btnForward.setOnClickListener(this);
        btnActDelete = (Button) findViewById(R.id.action_trash);
        btnActDelete.setVisibility(View.VISIBLE);
        btnActDelete.setOnClickListener(this);

        imgLogo = (ImageView) findViewById(R.id.imgTitle);
        imgLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowMessageActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        txtTitleHeader = (TextView) findViewById(R.id.txtTitle);
        txtTitleHeader.setVisibility(View.GONE);

        if (is_new == true && !senderId.equalsIgnoreCase(prefs.getUserId()))
            changeMessageReadStatus();
    }

    private void addAttachView (final AttachFileData attachData) {
        final RelativeLayout subView = (RelativeLayout) LayoutInflater.from(ShowMessageActivity.this).inflate(R.layout.list_attach, null);

        ImageView imgFileIcon = (ImageView) subView.findViewById(R.id.imgFileIcon);
        switch (attachData.fileType) {
            case CreateMessageActivity.FILE_TYPE_PDF:
                imgFileIcon.setImageResource(R.mipmap.icon_pdf);
                break;
            case CreateMessageActivity.FILE_TYPE_IMAGE:
                imgFileIcon.setImageResource(R.mipmap.icon_photo);
                break;
            case CreateMessageActivity.FILE_TYPE_VIDEO:
                imgFileIcon.setImageResource(R.mipmap.icon_video);
                break;
            default:
                imgFileIcon.setImageResource(R.mipmap.icon_pdf);
                break;
        }

        TextView txtFileName = (TextView) subView.findViewById(R.id.txtFileName);
        txtFileName.setText(attachData.fileName);

        ImageView imgDelete = (ImageView) subView.findViewById(R.id.imgDeleteIcon);
        imgDelete.setVisibility(View.GONE);

        subView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(attachData.url));
                startActivity(browserIntent);
            }
        });

        attachLayout.addView(subView, 0);
    }

    private void registerReceiver() {
        registerReceiver(mMessageReceiver, new IntentFilter(SalesGcmListenerService.ACTION_MESSAGE_NOTIFICATION));
    }

    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intentMessageShares = new Intent(ShowMessageActivity.this, MyMessagesActivity.class);
            intentMessageShares.putExtra(Constants.PARAM_USER_ID, prefs.getUserId());
            startActivity(intentMessageShares);
        }
    };

    private void registerRequestReceiver() {
        registerReceiver(mRequestReceiver, new IntentFilter(SalesGcmListenerService.ACTION_REQUEST_NOTIFICATION));
    }

    BroadcastReceiver mRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intentRequests = new Intent(ShowMessageActivity.this, MyRequestActivity.class);
            intentRequests.putExtra(Constants.PARAM_USER_ID, prefs.getUserId());
            startActivity(intentRequests);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver();
        registerRequestReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mMessageReceiver);
        unregisterReceiver(mRequestReceiver);
    }

    private void changeMessageReadStatus() {

        int msg_id = Integer.valueOf(msgId);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://repgate.com/wp-json/wp/v2/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // TODO video attachment
        HttpInterface.ChangeMessageReadStatus changeMessageReadStatus = retrofit.create(HttpInterface.ChangeMessageReadStatus.class);
        Call<SendResponseData> call = changeMessageReadStatus.changeMessageReadStatus(msg_id);

        final ProgressDialog progress = ProgressDialog.show(this, null, "Please wait...", true);
        call.enqueue(new Callback<SendResponseData>() {
            @Override
            public void onResponse(Call<SendResponseData> call, Response<SendResponseData> response) {
                progress.dismiss();
                SendResponseData responseData = response.body();

                if (responseData == null) {
                    new AlertDialog.Builder(ShowMessageActivity.this)
                            .setTitle(R.string.error_title)
                            .setMessage(R.string.service_error_msg)
                            .setNegativeButton(R.string.ok, null)
                            .show();
                } else {
                    Log.d(TAG, "success = " + responseData.success);
                    if (responseData.success == false) {
                        String message = responseData.error.err_msg;
                        checkErrorMessage(message);
                    } else {
                        int messageNew = Integer.parseInt(prefs.getUserMessageNew()) - 1;
                        prefs.setUserMessageNew(String.valueOf(messageNew));
                    }
                }
            }

            @Override
            public void onFailure(Call<SendResponseData> call, Throwable t) {
                progress.dismiss();

                Log.d(TAG, t.toString());

                new AlertDialog.Builder(ShowMessageActivity.this)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.network_error_msg)
                        .setNegativeButton(R.string.ok, null)
                        .show();
            }
        });

    }

    private void deleteMessage(String messageId) {
        int user_id = Integer.valueOf(this.prefs.getUserId());
        int message_id = Integer.valueOf(messageId);

        Retrofit retrofit = Constants.getRetrofitInstanc();

        HttpInterface.DeleteMessageInterface sendInterface = retrofit.create(HttpInterface.DeleteMessageInterface.class);
        Call<SendResponseData> call = sendInterface.deleteMessage(user_id, message_id);

        final ProgressDialog progress = ProgressDialog.show(this, null, "Please wait...", true);
        call.enqueue(new Callback<SendResponseData>() {
            @Override
            public void onResponse(Call<SendResponseData> call, Response<SendResponseData> response) {
                progress.dismiss();
                SendResponseData responseData = response.body();

                if (responseData == null) {
                    new AlertDialog.Builder(ShowMessageActivity.this)
                            .setTitle(R.string.error_title)
                            .setMessage(R.string.service_error_msg)
                            .setNegativeButton(R.string.ok, null)
                            .show();
                } else {
                    Log.d(TAG, "success = " + responseData.success);
                    if (responseData.success == true) {

                        new AlertDialog.Builder(ShowMessageActivity.this)
                                .setTitle(R.string.success_title)
                                .setMessage("You deleted successfully a message")
                                .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .show();
                    } else {
                        String message = responseData.error.err_msg;
                        checkErrorMessage(message);

                    }
                }
            }

            @Override
            public void onFailure(Call<SendResponseData> call, Throwable t) {
                progress.dismiss();

                Log.d(TAG, t.toString());

                new AlertDialog.Builder(ShowMessageActivity.this)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.network_error_msg)
                        .setNegativeButton(R.string.ok, null)
                        .show();
            }
        });
    }

    public void checkErrorMessage(String error) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error_title)
                .setMessage(error)
                .setNegativeButton(R.string.ok, null)
                .show();
        return;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnReply:
                String replyTitle = "RE: " + title;
                String replyMsgBody = "From: " + senderName + "\n";
                replyMsgBody = replyMsgBody + "Sent: " + createAt + "\n";
                replyMsgBody = replyMsgBody + "To: " + receiverName + "\n";
                replyMsgBody = replyMsgBody + "Title: " + title + "\n";
                replyMsgBody = replyMsgBody + "\n" + msgBody;

                Intent replyIntent = new Intent(ShowMessageActivity.this, CreateMessageActivity.class);

                replyIntent.putExtra(Constants.PARAM_MESSAGE_SENDER_ID, senderId);
                replyIntent.putExtra(Constants.PARAM_MESSAGE_SENDER_NAME, senderName);
                replyIntent.putExtra(Constants.PARAM_MESSAGE_TITLE, replyTitle);
                replyIntent.putExtra(Constants.PARAM_MESSAGE_BODY, replyMsgBody);
                replyIntent.putExtra(Constants.PARAM_MESSAGE_TYPE, Constants.REPLY_MESSAGE);

                startActivity(replyIntent);
                break;
            case R.id.btnForward:
                String forwardTitle = "Fwd: " + title;
                String forwardMsgBody = "From: " + senderName + "\n";
                forwardMsgBody = forwardMsgBody + "Sent: " + createAt + "\n";
                forwardMsgBody = forwardMsgBody + "To: " + receiverName + "\n";
                forwardMsgBody = forwardMsgBody + "Title: " + title + "\n";
                forwardMsgBody = forwardMsgBody + "\n" + msgBody;

                Intent forwardIntent = new Intent(ShowMessageActivity.this, CreateMessageActivity.class);

                forwardIntent.putExtra(Constants.PARAM_MESSAGE_SENDER_ID, senderId);
                forwardIntent.putExtra(Constants.PARAM_MESSAGE_SENDER_NAME, senderName);
                forwardIntent.putExtra(Constants.PARAM_MESSAGE_TITLE, forwardTitle);
                forwardIntent.putExtra(Constants.PARAM_MESSAGE_BODY, forwardMsgBody);
                forwardIntent.putExtra(Constants.PARAM_MESSAGE_TYPE, Constants.FORWARD_MESSAGE);
                forwardIntent.putExtra(Constants.PARAM_MESSAGE_PDF_ATTACH, pdfLink);

                startActivity(forwardIntent);
                break;
            case R.id.action_trash:
                deleteMessage(msgId);
                break;
            default:
                break;
        }
    }
}
