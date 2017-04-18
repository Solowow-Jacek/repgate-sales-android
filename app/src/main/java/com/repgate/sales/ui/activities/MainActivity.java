package com.repgate.sales.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextInsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;
import com.repgate.sales.R;
import com.repgate.sales.common.AppPreferences;
import com.repgate.sales.common.Constants;
import com.repgate.sales.data.AllMessageResponseData;
import com.repgate.sales.data.AllRequestResponseData;
import com.repgate.sales.data.MyProvidersResponseData;
import com.repgate.sales.http.HttpInterface;
import com.repgate.sales.service.SalesGcmListenerService;

import java.util.ArrayList;

import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends Activity implements View.OnClickListener{

    private final static String TAG = "MainActivity";
    public AppPreferences prefs;

    private Button btnTutorial, btnProfile, btnHealthcareProvider, btnMessageShares, btnRequests, btnJoin;
    private RadioButton rbMsg, rbReq, rbSchedule;
    private TextView txtMissMsgs, txtMissReqs;
    private FButton btnSchedule;
    private BoomMenuButton bmb;

    private int miss_msgs = 0, miss_reqs = 0;

    private static final int PERMISSIONS_READ_EXTERNAL_STORAGE_DEVICE_RINGTONE = 210;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = new AppPreferences(this);

        bmb = (BoomMenuButton) findViewById(R.id.bmb);
        bmb.setPiecePlaceEnum(PiecePlaceEnum.HAM_4);
        bmb.setButtonPlaceEnum(ButtonPlaceEnum.HAM_4);

        HamButton.Builder createMsgBuilder = new HamButton.Builder()
                .normalImageRes(R.mipmap.menu_icon_messege)
                .normalColor(Color.rgb(33, 150,243))
                .normalTextRes(R.string.create_message).listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        Intent intentMessage = new Intent(MainActivity.this, CreateMessageActivity.class);
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
                        Intent intentRequest = new Intent(MainActivity.this, CreateRequestActivity.class);
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
                        Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
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
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.confirm_title)
                                .setMessage(R.string.are_you_logout)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        prefs.clearUserInformation();

                                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        MainActivity.this.finish();
                                    }
                                })
                                .setNegativeButton(R.string.no, null)
                                .show();
                    }
                });
        bmb.addBuilder(logoutBuilder);

        btnProfile = (Button) findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(this);

        btnHealthcareProvider = (Button) findViewById(R.id.btnHealthCareProviders);
        btnHealthcareProvider.setOnClickListener(this);

        btnMessageShares = (Button) findViewById(R.id.btnMessagesShares);
        btnMessageShares.setOnClickListener(this);
        btnRequests = (Button) findViewById(R.id.btnRequests);
        btnRequests.setOnClickListener(this);

        btnJoin = (Button) findViewById(R.id.btnJoin);
        btnJoin.setOnClickListener(this);

        btnTutorial = (Button) findViewById(R.id.btnTutorial);
        btnTutorial.setOnClickListener(this);

        txtMissMsgs = (TextView) findViewById(R.id.missed_msgs);
        txtMissReqs = (TextView) findViewById(R.id.missed_reqs);

        displayMissedRequests(miss_reqs);
        displayMissedMessages(miss_msgs);

        startNewActivity(this, "com.estrongs.android.pop");
    }

    public void startNewActivity(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            // Bring user to the market or let them choose an app?
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private void registerReceiver() {
        registerReceiver(mMessageReceiver, new IntentFilter(SalesGcmListenerService.ACTION_MESSAGE_NOTIFICATION));
    }

    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intentMessageShares = new Intent(MainActivity.this, MyMessagesActivity.class);
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
            Intent intentRequests = new Intent(MainActivity.this, MyRequestActivity.class);
            intentRequests.putExtra(Constants.PARAM_USER_ID, prefs.getUserId());
            startActivity(intentRequests);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        ArrayList<String> permissionsList = new ArrayList<String>();

        int contacts = getPackageManager().checkPermission(Manifest.permission.READ_CONTACTS, getPackageName());
        int readStore = getPackageManager().checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, getPackageName());
        int writeStore = getPackageManager().checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, getPackageName());
        int camera = getPackageManager().checkPermission(Manifest.permission.CAMERA, getPackageName());

        if (readStore != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (writeStore != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (camera != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.CAMERA);
        if (contacts != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.READ_CONTACTS);

        if (permissionsList.size() > 0) {
            String[] permissions = new String[permissionsList.size()];
            permissions = permissionsList.toArray(permissions);
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_READ_EXTERNAL_STORAGE_DEVICE_RINGTONE);
        }

        registerReceiver();
        registerRequestReceiver();

        if (prefs.getUserMessageNew() != null
                && !prefs.getUserMessageNew().isEmpty()
                && !prefs.getUserMessageNew().equalsIgnoreCase("0"))
            miss_msgs = Integer.parseInt(prefs.getUserMessageNew());
        else
            miss_msgs = 0;
        displayMissedMessages(miss_msgs);

        if (prefs.getUserRequestNew() != null && !prefs.getUserRequestNew().isEmpty() && !prefs.getUserRequestNew().equalsIgnoreCase("0"))
            miss_reqs = Integer.parseInt(prefs.getUserRequestNew());
        else
            miss_reqs = 0;
        displayMissedRequests(miss_reqs);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (prefs.getUserMessageNew() != null && !prefs.getUserMessageNew().isEmpty())
            miss_msgs = Integer.parseInt(prefs.getUserMessageNew());
        else
            miss_msgs = 0;
        displayMissedMessages(miss_msgs);

        if (prefs.getUserRequestNew() != null && !prefs.getUserRequestNew().isEmpty())
            miss_reqs = Integer.parseInt(prefs.getUserRequestNew());
        else
            miss_reqs = 0;
        displayMissedRequests(miss_reqs);

        loadReceiversInformation(Constants.RECV_OFFICERS);
        loadReceiversInformation(Constants.RECV_PROVIDERS);
    }

    private void loadReceiversInformation(final int recvType) {
        int userId = Integer.parseInt(prefs.getUserId());
        String roleNames = String.valueOf(Constants.USER_ROLE_UNDEFINED);

        if (recvType == Constants.RECV_OFFICERS) {
            roleNames = String.valueOf(Constants.USER_ROLE_FRONT_DESK);
        } else if (recvType == Constants.RECV_PROVIDERS) {
            roleNames = String.valueOf(Constants.USER_ROLE_PHYSICIAN) + ","
                    + String.valueOf(Constants.USER_ROLE_PHYSICIAN_ASSISTANT) + ","
                    + String.valueOf(Constants.USER_ROLE_NURSE_PRACTIONER);
        }

        Retrofit retrofit = Constants.getRetrofitInstanc();

        HttpInterface.GetMyProviderInterface httpInterface = retrofit.create(HttpInterface.GetMyProviderInterface.class);
        Call<MyProvidersResponseData> call = httpInterface.getMyProviders(userId, roleNames);

        final ProgressDialog progress = ProgressDialog.show(this, null, "Please wait...", true);
        call.enqueue(new Callback<MyProvidersResponseData>() {
            @Override
            public void onResponse(Call<MyProvidersResponseData> call, Response<MyProvidersResponseData> response) {
                progress.dismiss();
                MyProvidersResponseData responseData = response.body();

                if (responseData == null) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.error_title)
                            .setMessage(R.string.service_error_msg)
                            .setNegativeButton(R.string.ok, null)
                            .show();
                } else {
                    Log.d(TAG, "success = " + responseData.success);
                    if (responseData.success == true) {

                        if (recvType == Constants.RECV_OFFICERS) {
                            Constants.mOfficersArray = responseData.data;
                        } else if (recvType == Constants.RECV_PROVIDERS) {
                            Constants.mProvidersArray = responseData.data;
                        }

                    } else {

                        String message = responseData.error.err_msg;
                        checkErrorMessage(message);

                    }
                }
            }

            @Override
            public void onFailure(Call<MyProvidersResponseData> call, Throwable t) {
                progress.dismiss();

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.network_error_msg)
                        .setNegativeButton(R.string.ok, null)
                        .show();
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mMessageReceiver);
        unregisterReceiver(mRequestReceiver);
    }

    private void displayMissedMessages(int missedMsgCount) {
        if (missedMsgCount > 0) {
            txtMissMsgs.setText(missedMsgCount + "");
            txtMissMsgs.setVisibility(View.VISIBLE);
        } else {
            txtMissMsgs.clearAnimation();
            txtMissMsgs.setVisibility(View.GONE);
        }
    }

    private void displayMissedRequests(int missedReqCount) {
        if (missedReqCount > 0) {
            txtMissReqs.setText(missedReqCount + "");
            txtMissReqs.setVisibility(View.VISIBLE);
        } else {
            txtMissReqs.clearAnimation();
            txtMissReqs.setVisibility(View.GONE);
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

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnJoin:
                Intent intentJoin = new Intent(MainActivity.this, JoinActivity.class);
                startActivity(intentJoin);
                break;
            case R.id.btnProfile:
                Intent intentProfile = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intentProfile);
                break;
            case R.id.btnHealthCareProviders:
                Intent intentHealth = new Intent(this, ProvidersActivity.class);
                String userId = prefs.getUserId();
                intentHealth.putExtra(Constants.PARAM_USER_ID, Integer.valueOf(userId));
                intentHealth.putExtra(Constants.PARAM_ROLE_ID, Constants.USER_ROLE_FRONT_DESK);
                startActivity(intentHealth);
                break;
            case R.id.btnMessagesShares:
                Intent intentMessageShares = new Intent(this, MyMessagesActivity.class);
                intentMessageShares.putExtra(Constants.PARAM_USER_ID, prefs.getUserId());
                startActivity(intentMessageShares);
                break;
            case R.id.btnRequests:
                Intent intentRequests = new Intent(this, MyRequestActivity.class);
                intentRequests.putExtra(Constants.PARAM_USER_ID, prefs.getUserId());
                startActivity(intentRequests);
                break;
            case R.id.btnTutorial:
                Intent intentTutorials = new Intent(MainActivity.this, TutorialsActivity.class);
                startActivity(intentTutorials);
                break;
            default:
                break;
        }
    }
}
