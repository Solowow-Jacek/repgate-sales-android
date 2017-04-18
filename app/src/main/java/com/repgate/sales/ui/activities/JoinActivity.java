package com.repgate.sales.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.nightonke.boommenu.BoomMenuButton;
import com.repgate.sales.R;
import com.repgate.sales.common.AppPreferences;
import com.repgate.sales.common.Constants;
import com.repgate.sales.data.JoinDoctorResponseData;
import com.repgate.sales.data.UserMembershipResponseData;
import com.repgate.sales.http.HttpInterface;
import com.repgate.sales.service.SalesGcmListenerService;
import com.repgate.sales.util.Validation;

import info.hoang8f.widget.FButton;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class JoinActivity extends Activity implements View.OnClickListener {
    private final static String TAG = "MainActivity";
    public AppPreferences prefs;

    private EditText edtCode, edtCardNum, edtCVCNum;
    private Spinner spnMonth, spnYear;
    private FButton btnJoin;
    private TextView txtAmount, txtTitle;
    private ImageView imgLogo;
    private Boolean isConfirmed = false;

    private static final String DOCTOR_CODE = "doctor_code";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        prefs = new AppPreferences(this);

        imgLogo = (ImageView) findViewById(R.id.imgTitle);
        imgLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JoinActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setVisibility(View.GONE);

        BoomMenuButton bmb = (BoomMenuButton) findViewById(R.id.bmb);
        bmb.setVisibility(View.GONE);

        txtAmount = (TextView) findViewById(R.id.txtAmount);
        txtAmount.setText("");
        txtAmount.setVisibility(View.GONE);
        edtCode = (EditText) findViewById(R.id.edtCode);
//        edtCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                boolean handled = false;
//                if (actionId == EditorInfo.IME_ACTION_SEND) {
//                    joinDoctor();
//                    handled = true;
//                }
//
//                return handled;
//            }
//        });

        edtCardNum = (EditText) findViewById(R.id.edtCardNumber);
        edtCardNum.setVisibility(View.GONE);
        edtCVCNum = (EditText) findViewById(R.id.edtCvcNumber);
        edtCVCNum.setVisibility(View.GONE);

        spnMonth = (Spinner) findViewById(R.id.spnExpireMonth);
        spnMonth.setVisibility(View.GONE);
        spnYear = (Spinner) findViewById(R.id.spnExpireYear);
        spnYear.setVisibility(View.GONE);

        if (savedInstanceState != null) {
            String doctorCode = savedInstanceState.getString(DOCTOR_CODE);
            edtCode.setText(doctorCode);
        }


        btnJoin = (FButton) findViewById(R.id.btnJoin);
//        btnJoin.setText("Confirm");
        btnJoin.setText("Join");
        btnJoin.setOnClickListener(this);
    }

    private void registerReceiver() {
        registerReceiver(mMessageReceiver, new IntentFilter(SalesGcmListenerService.ACTION_MESSAGE_NOTIFICATION));
    }

    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intentMessageShares = new Intent(JoinActivity.this, MyMessagesActivity.class);
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
            Intent intentRequests = new Intent(JoinActivity.this, MyRequestActivity.class);
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

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnJoin:
//                if (isConfirmed == true)
                    joinDoctor();
//                else
//                    confirmDoctor();

                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(DOCTOR_CODE, edtCode.getText().toString());
    }

    private void confirmDoctor() {

        View view = JoinActivity.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) JoinActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // check validation
        if (false == Validation.EmptyValidation(edtCode, getResources().getString(R.string.input_code_msg)))
            return;

//        if (false == Validation.EmptyValidation(edtCardNum, getResources().getString(R.string.input_card_number_msg)))
//            return;
//
//        if (false == Validation.EmptyValidation(edtCVCNum, getResources().getString(R.string.input_cvc_msg)))
//            return;

        String code = edtCode.getText().toString();
        String user_id = prefs.getUserId();

        Retrofit retrofit = Constants.getRetrofitInstanc();

        HttpInterface.ConfirmDoctorInterface confirmDoctorInterface = retrofit.create(HttpInterface.ConfirmDoctorInterface.class);
        Call<UserMembershipResponseData> call = confirmDoctorInterface.confirmDoctor(Integer.parseInt(user_id), code);

        final ProgressDialog progress = ProgressDialog.show(this, null, "Please wait...", true);
        call.enqueue(new Callback<UserMembershipResponseData>() {
            @Override
            public void onResponse(Call<UserMembershipResponseData> call, Response<UserMembershipResponseData> response) {
                progress.dismiss();
                UserMembershipResponseData responseData = response.body();

                if (responseData == null) {
                    new AlertDialog.Builder(JoinActivity.this)
                            .setTitle(R.string.error_title)
                            .setMessage(R.string.service_error_msg)
                            .setNegativeButton(R.string.ok, null)
                            .show();
                } else {
                    Log.d(TAG, "success = " + responseData.success);
                    if (responseData.success == true && !responseData.member_ship_plan.isEmpty()) {

                        // TODO
                        isConfirmed = true;
                        btnJoin.setText("Join");
                        txtAmount.setText("$" + responseData.member_ship_plan);

                    } else {

                        String message = responseData.error.err_msg;
                        checkErrorMessage(message);
                    }
                }

            }

            @Override
            public void onFailure(Call<UserMembershipResponseData> call, Throwable t) {
                progress.dismiss();

                Log.d(TAG, "onFailure");

                new AlertDialog.Builder(JoinActivity.this)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.network_error_msg)
                        .setNegativeButton(R.string.ok, null)
                        .show();
            }
        });

    }

    private void joinDoctor() {

        View view = JoinActivity.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) JoinActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // check validation
        if (false == Validation.EmptyValidation(edtCode, getResources().getString(R.string.input_code_msg)))
            return;

//        if (false == Validation.EmptyValidation(edtCardNum, getResources().getString(R.string.input_card_number_msg)))
//            return;
//
//        if (false == Validation.EmptyValidation(edtCVCNum, getResources().getString(R.string.input_cvc_msg)))
//            return;

        String code = edtCode.getText().toString();
        String user_id = prefs.getUserId();
//        String cardNum = edtCardNum.getText().toString();
//        String cvcNum = edtCVCNum.getText().toString();
//        String month = (String) spnMonth.getSelectedItem();
//        String year = (String) spnYear.getSelectedItem();

        Retrofit retrofit = Constants.getRetrofitInstanc();

        HttpInterface.JoinDoctorInterface joinInterface = retrofit.create(HttpInterface.JoinDoctorInterface.class);
//        Call<JoinDoctorResponseData> call = joinInterface.joinDoctor(code, Integer.parseInt(user_id), cardNum, cvcNum, "", month, year);
        Call<JoinDoctorResponseData> call = joinInterface.joinDoctor(Integer.parseInt(user_id), code);

        final ProgressDialog progress = ProgressDialog.show(this, null, "Please wait...", true);
        call.enqueue(new Callback<JoinDoctorResponseData>() {
            @Override
            public void onResponse(Call<JoinDoctorResponseData> call, Response<JoinDoctorResponseData> response) {
                progress.dismiss();
                JoinDoctorResponseData responseData = response.body();

                if (responseData == null) {
                    new AlertDialog.Builder(JoinActivity.this)
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

                new AlertDialog.Builder(JoinActivity.this)
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
}
