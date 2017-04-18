package com.repgate.sales.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.repgate.sales.R;
import com.repgate.sales.common.AppPreferences;
import com.repgate.sales.common.Constants;
import com.repgate.sales.data.SignupResponseData;
import com.repgate.sales.http.HttpInterface;
import com.repgate.sales.util.Validation;

import info.hoang8f.widget.FButton;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignUpActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "SignUpActivity";

    private EditText edtName, edtEmail, edtPasswd;
    private FButton btnNext;

    AppPreferences prefs;

    private String mDeviceToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mDeviceToken = getIntent().getStringExtra(Constants.INTENT_PARAM_TOKEN);


        prefs = new AppPreferences(this);

        edtName = (EditText) findViewById(R.id.edtUsername);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPasswd = (EditText) findViewById(R.id.edtPasswd);
        edtPasswd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    onSignUp();
                    handled = true;
                }

                return handled;
            }
        });

        btnNext = (FButton) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnNext:
                onSignUp();
                break;
        }

    }

    private void onSignUp() {

        if (false == Validation.EmptyValidation(edtName, getResources().getString(R.string.input_name_msg)))
            return;

        if (false == Validation.EmptyValidation(edtEmail, getResources().getString(R.string.input_email_msg)))
            return;

        if (false == Validation.EmptyValidation(edtPasswd, getResources().getString(R.string.input_password_msg)))
            return;

        if (false == Validation.LengthMoreValidation(edtPasswd, Constants.PASSWROD_MIN_LENGTH, getResources().getString(R.string.min_length_password)))
            return;

        String txtName = edtName.getText().toString();
        String txtEmail = edtEmail.getText().toString();
        String txtPasswd = edtPasswd.getText().toString();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://repgate.com/wp-json/wp/v2/")
//                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //Retrofit retrofit = Constants.getRetrofitInstanc();

        HttpInterface.SignupInterface signupInterface = retrofit.create(HttpInterface.SignupInterface.class);
        Call<SignupResponseData> signupCall = signupInterface.Signup(txtEmail, txtPasswd, Constants.USER_ROLE_SALES_REP,
                mDeviceToken, Constants.USER_DEVICE_TYPE_ANDROID);

        final ProgressDialog progress = ProgressDialog.show(this, null, "Please wait...", true);
        signupCall.enqueue(new Callback<SignupResponseData>() {
            @Override
            public void onResponse(Call<SignupResponseData> call, Response<SignupResponseData> response) {
                progress.dismiss();
                SignupResponseData responseData = response.body();

                if (responseData == null) {
                    new AlertDialog.Builder(SignUpActivity.this)
                            .setTitle(R.string.error_title)
                            .setMessage(R.string.service_error_msg)
                            .setNegativeButton(R.string.ok, null)
                            .show();
                } else {
                    Log.d(TAG, "success = " + responseData.success);
                    if (responseData.success == true) {
                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                        intent.putExtra(Constants.INTENT_PARAM_TOKEN, mDeviceToken);
                        startActivity(intent);
                        finish();
                    } else {

                        String message = responseData.error.err_msg;

                        new AlertDialog.Builder(SignUpActivity.this)
                                .setTitle(R.string.error_title)
                                .setMessage(message)
                                .setNegativeButton(R.string.ok, null)
                                .show();
                    }
                }
            }

            @Override
            public void onFailure(Call<SignupResponseData> call, Throwable t) {
                progress.dismiss();

                new AlertDialog.Builder(SignUpActivity.this)
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
