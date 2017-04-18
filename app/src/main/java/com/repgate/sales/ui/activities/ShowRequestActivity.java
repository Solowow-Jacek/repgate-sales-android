package com.repgate.sales.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.nightonke.boommenu.BoomMenuButton;
import com.repgate.sales.R;
import com.repgate.sales.common.AppPreferences;
import com.repgate.sales.common.Constants;
import com.repgate.sales.data.RequestResponseData;
import com.repgate.sales.data.SendResponseData;
import com.repgate.sales.http.HttpInterface;
import com.repgate.sales.service.SalesGcmListenerService;
import com.repgate.sales.view.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ShowRequestActivity extends Activity implements View.OnClickListener,
        TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener{
    private final static String TAG = "ShowRequestActivity";
    AppPreferences prefs;

    private String mUserId, mDateTime, requestId, title, senderImg, senderName, changerId, requestDate, requestType, status;
    private TextView txtTitleHeader, txtTitle, txtTime, txtStatus, txtSenderName;
    private Button btnBack, btnConfirmAction, btnChangeAction, btnCancelAction, btnDeleteAction;
    private int mActionType;
    private RoundedImageView imgSender;
    private RadioButton rdAppointment, rdLunch;
    private LinearLayout titleLayer;
    private ImageView imgLogo;

    private boolean is_new = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_request);

        prefs = new AppPreferences(this);
        mUserId = getIntent().getStringExtra(Constants.PARAM_USER_ID);
        requestId = getIntent().getStringExtra(Constants.PARAM_REQUEST_ID);

        BoomMenuButton bmb = (BoomMenuButton) findViewById(R.id.bmb);
        bmb.setVisibility(View.GONE);

        imgSender = (RoundedImageView) findViewById(R.id.imgPicture);
        txtSenderName = (TextView) findViewById(R.id.txtNameSender);

        txtTitle = (TextView) findViewById(R.id.txtReqTitle);

        rdAppointment = (RadioButton) findViewById(R.id.rdAppointment);
        rdLunch = (RadioButton) findViewById(R.id.rdLunch);

        txtTime = (TextView) findViewById(R.id.txtDateTime);
        txtTime.setOnClickListener(this);

        txtStatus = (TextView) findViewById(R.id.statusText);

        btnConfirmAction = (Button) findViewById(R.id.btnConfirm);
        btnConfirmAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionType = Constants.REQUEST_ACTION_CONFIRM;
                sendRequest();
            }
        });
        btnChangeAction = (Button) findViewById(R.id.btnChange);
        btnChangeAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionType = Constants.REQUEST_ACTION_CHANGE;
                sendRequest();
            }
        });
        btnCancelAction = (Button) findViewById(R.id.btnCancel);
        btnCancelAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionType = Constants.REQUEST_ACTION_CANCEL;
                sendRequest();
            }
        });

        btnDeleteAction = (Button) findViewById(R.id.action_trash);
        btnDeleteAction.setVisibility(View.VISIBLE);
        btnDeleteAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRequest(requestId);
            }
        });

        btnBack = (Button) findViewById(R.id.action_back);
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imgLogo = (ImageView) findViewById(R.id.imgTitle);
        imgLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowRequestActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        txtTitleHeader = (TextView) findViewById(R.id.txtTitle);
        txtTitleHeader.setVisibility(View.GONE);
    }

    private void registerReceiver() {
        registerReceiver(mMessageReceiver, new IntentFilter(SalesGcmListenerService.ACTION_MESSAGE_NOTIFICATION));
    }

    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intentMessageShares = new Intent(ShowRequestActivity.this, MyMessagesActivity.class);
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
            Intent intentRequests = new Intent(ShowRequestActivity.this, MyRequestActivity.class);
            intentRequests.putExtra(Constants.PARAM_USER_ID, prefs.getUserId());
            startActivity(intentRequests);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver();
        registerRequestReceiver();

        loadRequestInformation();
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mMessageReceiver);
        unregisterReceiver(mRequestReceiver);
    }

    private void loadRequestInformation() {

        int user_id = Integer.valueOf(this.prefs.getUserId());

        Retrofit retrofit = Constants.getRetrofitInstanc();

        HttpInterface.GetRequestInterface httpInterface = retrofit.create(HttpInterface.GetRequestInterface.class);
        Call<RequestResponseData> call = httpInterface.getRequest(user_id, Integer.parseInt(requestId));

        final ProgressDialog progress = ProgressDialog.show(this, null, "Please wait...", true);
        call.enqueue(new Callback<RequestResponseData>() {
            @Override
            public void onResponse(Call<RequestResponseData> call, Response<RequestResponseData> response) {
                progress.dismiss();
                RequestResponseData responseData = response.body();

                if (responseData == null) {
                    new AlertDialog.Builder(ShowRequestActivity.this)
                            .setTitle(R.string.error_title)
                            .setMessage(R.string.service_error_msg)
                            .setNegativeButton(R.string.ok, null)
                            .show();
                } else {
                    Log.d(TAG, "success = " + responseData.success);
                    if (responseData.success == true) {

                        title = responseData.data.title;
                        senderImg = responseData.data.senderImageUrl;
                        senderName = responseData.data.senderName;
                        changerId = responseData.data.changerId;
                        requestDate = responseData.data.requestDateTime;
                        requestType = responseData.data.requestType;
                        status = responseData.data.handleStatus;
                        is_new = responseData.data.isNew;

                        txtTitle.setText(title);
                        if (senderImg != null && senderImg.isEmpty() == false) {
                            Picasso.with(ShowRequestActivity.this)
                                    .load(senderImg)
                                    .placeholder(R.mipmap.default_avatar)
                                    .error(R.mipmap.default_avatar)
                                    .fit()
                                    .into(imgSender);
                        } else {
                            imgSender.setImageResource(R.mipmap.default_avatar);
                        }

                        txtSenderName.setText(senderName);
                        if (Integer.parseInt(requestType) == Constants.REQUEST_LUNCH) {
                            Drawable appointment = getResources().getDrawable(R.mipmap.icon_appointment2);
                            Drawable lunch = getResources().getDrawable(R.mipmap.icon_lunch3);
                            rdAppointment.setCompoundDrawablesWithIntrinsicBounds(null, appointment, null, null);
                            rdLunch.setCompoundDrawablesWithIntrinsicBounds(null, lunch, null, null);
                        } else if (Integer.parseInt(requestType) == Constants.REQUEST_APPOINTMENT) {
                            Drawable appointment = getResources().getDrawable(R.mipmap.icon_appointment3);
                            Drawable lunch = getResources().getDrawable(R.mipmap.icon_lunch2);
                            rdAppointment.setCompoundDrawablesWithIntrinsicBounds(null, appointment, null, null);
                            rdLunch.setCompoundDrawablesWithIntrinsicBounds(null, lunch, null, null);
                        }

                        if (requestDate != null && requestDate.isEmpty() != true && !requestDate.contains("null")) {
                            String parts[] = requestDate.split(" ");
                            txtTime.setText(requestDate);
                        }

                        txtStatus.setText(Constants.statusArray[Integer.parseInt(status)]);

                        if (mUserId.equalsIgnoreCase(changerId)) {
                            Drawable confirm = getResources().getDrawable(R.mipmap.icon_confirm);
                            Drawable change = getResources().getDrawable(R.mipmap.icon_change);
                            Drawable cancel = getResources().getDrawable(R.mipmap.icon_cancel);

                            btnConfirmAction.setEnabled(false);
                            btnConfirmAction.setBackground(confirm);
                            btnChangeAction.setEnabled(false);
                            btnChangeAction.setBackground(change);
                            btnCancelAction.setEnabled(false);
                            btnCancelAction.setBackground(cancel);
                        }

                        if (is_new == true && !changerId.equalsIgnoreCase(prefs.getUserId()))
                            changeRequestReadStatus();

                    } else {

                        String message = responseData.error.err_msg;
                        checkErrorMessage(message);
                    }
                }
            }

            @Override
            public void onFailure(Call<RequestResponseData> call, Throwable t) {
                progress.dismiss();

                new AlertDialog.Builder(ShowRequestActivity.this)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.network_error_msg)
                        .setNegativeButton(R.string.ok, null)
                        .show();
            }
        });
    }

    private void changeRequestReadStatus() {

        int req_id = Integer.valueOf(requestId);

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
        HttpInterface.ChangeRequestReadStatus changeRequestReadStatus = retrofit.create(HttpInterface.ChangeRequestReadStatus.class);
        Call<SendResponseData> call = changeRequestReadStatus.changeRequestReadStatus(req_id);

        final ProgressDialog progress = ProgressDialog.show(this, null, "Please wait...", true);
        call.enqueue(new Callback<SendResponseData>() {
            @Override
            public void onResponse(Call<SendResponseData> call, Response<SendResponseData> response) {
                progress.dismiss();
                SendResponseData responseData = response.body();

                if (responseData == null) {
                    new AlertDialog.Builder(ShowRequestActivity.this)
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
                        int requesteNew = Integer.parseInt(prefs.getUserRequestNew()) - 1;
                        prefs.setUserRequestNew(String.valueOf(requesteNew));
                    }
                }
            }

            @Override
            public void onFailure(Call<SendResponseData> call, Throwable t) {
                progress.dismiss();

                Log.d(TAG, t.toString());

                new AlertDialog.Builder(ShowRequestActivity.this)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.network_error_msg)
                        .setNegativeButton(R.string.ok, null)
                        .show();
            }
        });

    }

    private void sendRequest() {
        int user_id = Integer.valueOf(this.prefs.getUserId());
        String strDateTime = txtTime.getText().toString();

        Retrofit retrofit = Constants.getRetrofitInstanc();

        HttpInterface.HandleRequestInterface sendInterface = retrofit.create(HttpInterface.HandleRequestInterface.class);
        Call<SendResponseData> call = sendInterface.handleRequest(Integer.parseInt(requestId), mActionType, strDateTime, user_id);

        final ProgressDialog progress = ProgressDialog.show(this, null, "Please wait...", true);
        call.enqueue(new Callback<SendResponseData>() {
            @Override
            public void onResponse(Call<SendResponseData> call, Response<SendResponseData> response) {
                progress.dismiss();
                SendResponseData responseData = response.body();

                if (responseData == null) {
                    new AlertDialog.Builder(ShowRequestActivity.this)
                            .setTitle(R.string.error_title)
                            .setMessage(R.string.service_error_msg)
                            .setNegativeButton(R.string.ok, null)
                            .show();
                } else {
                    Log.d(TAG, "success = " + responseData.success);
                    if (responseData.success == true) {

                        new AlertDialog.Builder(ShowRequestActivity.this)
                                .setTitle(R.string.success_title)
                                .setMessage("You sent successfully request to provider")
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

                new AlertDialog.Builder(ShowRequestActivity.this)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.network_error_msg)
                        .setNegativeButton(R.string.ok, null)
                        .show();
            }
        });
    }

    private void deleteRequest(String reqId) {
        int user_id = Integer.valueOf(this.prefs.getUserId());
        int req_id = Integer.valueOf(reqId);

        Retrofit retrofit = Constants.getRetrofitInstanc();

        HttpInterface.DeleteRequestInterface deleteRequestInterface = retrofit.create(HttpInterface.DeleteRequestInterface.class);
        Call<SendResponseData> call = deleteRequestInterface.deleteRequest(user_id, req_id);

        final ProgressDialog progress = ProgressDialog.show(this, null, "Please wait...", true);
        call.enqueue(new Callback<SendResponseData>() {
            @Override
            public void onResponse(Call<SendResponseData> call, Response<SendResponseData> response) {
                progress.dismiss();
                SendResponseData responseData = response.body();

                if (responseData == null) {
                    new AlertDialog.Builder(ShowRequestActivity.this)
                            .setTitle(R.string.error_title)
                            .setMessage(R.string.service_error_msg)
                            .setNegativeButton(R.string.ok, null)
                            .show();
                } else {
                    Log.d(TAG, "success = " + responseData.success);
                    if (responseData.success == true) {

                        new AlertDialog.Builder(ShowRequestActivity.this)
                                .setTitle(R.string.success_title)
                                .setMessage("You deleted successfully a request")
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

                new AlertDialog.Builder(ShowRequestActivity.this)
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
        Calendar now = Calendar.getInstance();

        switch (v.getId()) {
            case R.id.txtDateTime:
                mDateTime = "";
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        ShowRequestActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(getFragmentManager(), "Datepickerdialog");
                break;
            default:
                break;
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        mDateTime = year + "-" + (++monthOfYear) + "-" + dayOfMonth;

        Calendar now = Calendar.getInstance();

        TimePickerDialog tpd = TimePickerDialog.newInstance(
                ShowRequestActivity.this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                false
        );

        tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        ShowRequestActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });
        tpd.setTimeInterval(1, 15, 1);
        tpd.show(getFragmentManager(), "Timepickerdialog");
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        int tmpHourOfDay = hourOfDay > 12 ? hourOfDay - 12 : hourOfDay;
        String timeType = hourOfDay >= 12 ? "pm" : "am";
        String hourString = tmpHourOfDay < 10 ? "0"+tmpHourOfDay : ""+tmpHourOfDay;
        String minuteString = minute < 10 ? "0"+minute : ""+minute;
        String secondString = second < 10 ? "0"+second : ""+second;
        String time = hourString+":"+minuteString + " " + timeType;
        txtTime.setText(time + " " + mDateTime);
    }
}
