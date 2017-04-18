package com.repgate.sales.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;
import com.repgate.sales.R;
import com.repgate.sales.common.AppPreferences;
import com.repgate.sales.common.Constants;
import com.repgate.sales.data.JoinDoctorResponseData;
import com.repgate.sales.data.LoginResponseData;
import com.repgate.sales.http.HttpInterface;
import com.repgate.sales.service.SalesGcmListenerService;
import com.repgate.sales.util.Validation;
import com.squareup.picasso.Picasso;

import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CommunicateActivity extends Activity implements View.OnClickListener {
    private final static String TAG = "CommunicateActivity";
    public AppPreferences prefs;

    private Button btnBack;
    private FButton btnMessages, btnRequests, btnProviders, btnUnJoin;
    private ImageView imgLogo, imgAvatar;
    private TextView txtTitle, txtName, txtSpecialty, txtInterest;
    private LinearLayout specLayaout, interestLayout;
    private BoomMenuButton bmb;

    private String mUserId, mRoleId, mRoleName, mUserName, mLogoUrl, mSpecialty, mInterest;
    private int mMissMsg, mMissReq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communicate);

        prefs = new AppPreferences(this);

        mUserId = getIntent().getStringExtra(Constants.PARAM_USER_ID);
        mRoleId = getIntent().getStringExtra(Constants.PARAM_ROLE_ID);
        mRoleName = getIntent().getStringExtra(Constants.PARAM_ROLE_NAME);
        mUserName = getIntent().getStringExtra(Constants.PARAM_USER_NAME);
        mLogoUrl = getIntent().getStringExtra(Constants.PARAM_USER_AVATAR);
        mSpecialty = getIntent().getStringExtra(Constants.PARAM_USER_SPECIALTY);
        mInterest = getIntent().getStringExtra(Constants.PARAM_USER_INTEREST);
        mMissMsg = getIntent().getIntExtra(Constants.PARAM_USER_MISS_MSG, 0);
        mMissReq = getIntent().getIntExtra(Constants.PARAM_USER_MISS_REQ, 0);

        bmb = (BoomMenuButton) findViewById(R.id.bmb);
        bmb.setPiecePlaceEnum(PiecePlaceEnum.HAM_4);
        bmb.setButtonPlaceEnum(ButtonPlaceEnum.HAM_4);

        HamButton.Builder createMsgBuilder = new HamButton.Builder()
                .normalImageRes(R.mipmap.menu_icon_messege)
                .normalColor(Color.rgb(33, 150,243))
                .normalTextRes(R.string.create_message).listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        Intent intentMessage = new Intent(CommunicateActivity.this, CreateMessageActivity.class);
                        intentMessage.putExtra(Constants.PARAM_USER_ID, prefs.getUserId());
                        startActivity(intentMessage);
                    }
                });
        bmb.addBuilder(createMsgBuilder);

        HamButton.Builder createReqBuilder = new HamButton.Builder()
                .normalImageRes(R.mipmap.menu_icon_request)
                .normalColor(Color.rgb(33, 150,243))
                .normalTextRes(R.string.create_request).listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        Intent intentRequest = new Intent(CommunicateActivity.this, CreateRequestActivity.class);
                        intentRequest.putExtra(Constants.PARAM_USER_ID, prefs.getUserId());
                        startActivity(intentRequest);
                    }
                });
        bmb.addBuilder(createReqBuilder);

        HamButton.Builder scheduleBuilder = new HamButton.Builder()
                .normalImageRes(R.mipmap.menu_icon_schedule)
                .normalColor(Color.rgb(33, 150,243))
                .normalTextRes(R.string.schedule).listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        Intent intent = new Intent(CommunicateActivity.this, CalendarActivity.class);
                        startActivity(intent);
                    }
                });
        bmb.addBuilder(scheduleBuilder);

        HamButton.Builder logoutBuilder = new HamButton.Builder()
                .normalImageRes(R.mipmap.menu_icon_logout)
                .normalColor(Color.GRAY)
                .normalTextRes(R.string.logout).listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        new AlertDialog.Builder(CommunicateActivity.this)
                                .setTitle(R.string.confirm_title)
                                .setMessage(R.string.are_you_logout)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        prefs.clearUserInformation();

                                        Intent intent = new Intent(CommunicateActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        CommunicateActivity.this.finish();
                                    }
                                })
                                .setNegativeButton(R.string.no, null)
                                .show();
                    }
                });
        bmb.addBuilder(logoutBuilder);

        btnBack = (Button) findViewById(R.id.action_back);
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imgLogo = (ImageView) findViewById(R.id.imgTitle);
        imgLogo.setVisibility(View.GONE);
        imgLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CommunicateActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText("Communicate");

        imgAvatar = (ImageView) findViewById(R.id.imgPicture);
        if (mLogoUrl.isEmpty() == false) {

            Picasso.with(this)
                    .load(mLogoUrl)
                    .placeholder(R.mipmap.doctor)
                    .error(R.mipmap.doctor)
                    .fit()
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.mipmap.doctor);
        }

        txtName = (TextView) findViewById(R.id.txtName);
        txtName.setText(mUserName);

        txtSpecialty = (TextView) findViewById(R.id.txtSpecialty);
        if (mSpecialty != null && !mSpecialty.isEmpty())
            txtSpecialty.setText(mSpecialty);
        else
            txtSpecialty.setText("");

        txtInterest = (TextView) findViewById(R.id.txtInterest);
        if (mInterest != null && !mInterest.isEmpty())
            txtInterest.setText(mInterest);
        else
            txtInterest.setText("");

        btnMessages = (FButton) findViewById(R.id.btnMessages);
        btnMessages.setOnClickListener(this);
        btnRequests = (FButton) findViewById(R.id.btnRequests);
        btnRequests.setOnClickListener(this);

        specLayaout = (LinearLayout) findViewById(R.id.contentSpecialty);
        interestLayout = (LinearLayout) findViewById(R.id.contentInterest);

        btnProviders = (FButton) findViewById(R.id.btnProviders);

        if (Integer.parseInt(mRoleId) == Constants.USER_ROLE_FRONT_DESK) {
            btnProviders.setVisibility(View.VISIBLE);
            btnProviders.setOnClickListener(this);
        } else
            btnProviders.setVisibility(View.GONE);

        btnUnJoin = (FButton) findViewById(R.id.btnUnJoin);
        btnUnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUnJoin();
            }
        });

        if (mRoleId.equalsIgnoreCase(String.valueOf(Constants.USER_ROLE_FRONT_DESK)) ||
                mRoleId.equalsIgnoreCase(String.valueOf(Constants.USER_ROLE_PHYSICIAN_ASSISTANT)) ||
                mRoleId.equalsIgnoreCase(String.valueOf(Constants.USER_ROLE_NURSE_PRACTIONER))) {
            specLayaout.setVisibility(View.GONE);
            interestLayout.setVisibility(View.GONE);
        }
        else {
            btnRequests.setVisibility(View.GONE);
            btnUnJoin.setVisibility(View.GONE);
        }
    }

    private void registerReceiver() {
        registerReceiver(mMessageReceiver, new IntentFilter(SalesGcmListenerService.ACTION_MESSAGE_NOTIFICATION));
    }

    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intentMessageShares = new Intent(CommunicateActivity.this, MyMessagesActivity.class);
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
            Intent intentRequests = new Intent(CommunicateActivity.this, MyRequestActivity.class);
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

    private void onUnJoin() {
        int userId = Integer.valueOf(this.prefs.getUserId());
        int peerId = Integer.valueOf(mUserId);

        Retrofit retrofit = Constants.getRetrofitInstanc();

        HttpInterface.UnJoinDoctorInterface unJoinDoctorInterface = retrofit.create(HttpInterface.UnJoinDoctorInterface.class);
        Call<JoinDoctorResponseData> unJoinCall = unJoinDoctorInterface.unJoinDoctor(userId, peerId);

        final ProgressDialog progress = ProgressDialog.show(CommunicateActivity.this, null, "Unjoining in...", true);
        unJoinCall.enqueue(new Callback<JoinDoctorResponseData>() {
            @Override
            public void onResponse(Call<JoinDoctorResponseData> call, Response<JoinDoctorResponseData> response) {
                progress.dismiss();
                JoinDoctorResponseData responseData = response.body();

                if (responseData == null) {
                    new AlertDialog.Builder(CommunicateActivity.this)
                            .setTitle(R.string.error_title)
                            .setMessage(R.string.service_error_msg)
                            .setNegativeButton(R.string.ok, null)
                            .show();
                } else {
                    Log.d(TAG, "success = " + responseData.success);
                    if (responseData.success == true) {

                        // TODO
                        finish();

                    } else {

                        String message = responseData.error.err_msg;
                        checkErrorMessage(message);
                    }
                }
            }

            @Override
            public void onFailure(Call<JoinDoctorResponseData> call, Throwable t) {
                progress.dismiss();

                Log.d(TAG, "onFailure");

                new AlertDialog.Builder(CommunicateActivity.this)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.network_error_msg)
                        .setNegativeButton(R.string.ok, null)
                        .show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnMessages:
                Intent intentMessage = new Intent(this, CreateMessageActivity.class);
                intentMessage.putExtra(Constants.PARAM_MESSAGE_SENDER_ID, mUserId);
                intentMessage.putExtra(Constants.PARAM_MESSAGE_SENDER_NAME, mUserName);
                intentMessage.putExtra(Constants.PARAM_MESSAGE_TYPE, Constants.CREATE_MESSAGE);
                startActivity(intentMessage);
                break;
            case R.id.btnRequests:
                Intent intentRequest = new Intent(this, CreateRequestActivity.class);
                intentRequest.putExtra(Constants.PARAM_REQUEST_RECEIVER_ID, mUserId);
                intentRequest.putExtra(Constants.PARAM_REQUEST_RECEIVER_NAME, mUserName);
                startActivity(intentRequest);
                break;
            case R.id.btnProviders:
                Intent intentHealth = new Intent(this, ProvidersActivity.class);
                String userId = prefs.getUserId();
                intentHealth.putExtra(Constants.PARAM_USER_ID, Integer.valueOf(userId));
                intentHealth.putExtra(Constants.PARAM_ROLE_ID, Constants.USER_ROLE_UNDEFINED);
                startActivity(intentHealth);
                break;
            default:
                break;
        }
    }

    public void checkErrorMessage(String error) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error_title)
                .setMessage(error)
                .setNegativeButton(R.string.ok, null)
                .show();
        return;
    }
}
