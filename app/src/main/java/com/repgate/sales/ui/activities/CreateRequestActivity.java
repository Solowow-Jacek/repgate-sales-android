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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.nightonke.boommenu.BoomMenuButton;
import com.repgate.sales.R;
import com.repgate.sales.common.AppPreferences;
import com.repgate.sales.common.Constants;
import com.repgate.sales.data.MyProvidersResponseData;
import com.repgate.sales.data.ProviderProfileResponseData;
import com.repgate.sales.data.SendResponseData;
import com.repgate.sales.http.HttpInterface;
import com.repgate.sales.service.SalesGcmListenerService;
import com.repgate.sales.util.Validation;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CreateRequestActivity extends Activity implements View.OnClickListener,
        TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener{
    private final static String TAG = "CreateRequestActivity";
    public AppPreferences prefs;

    private String  mReceiverId, mReceiverName, mDateTime;
    private TextView txtFromName, txtDateTime, txtProviderId, txtProvider, txtTitle;
    private EditText edtTitle;
    private ListAdapter mAdapter;
    private PopupWindow pw;
    private ListView mlist;
    private int mReqType = Constants.REQUEST_APPOINTMENT;
    private Button btnSend, btnBack;
    private RadioButton rdAppointment, rdLunch;
    private ImageView imgLogo;

    public ArrayList<ProviderProfileResponseData.DataModel> mProvidersArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_request);

        prefs = new AppPreferences(this);
        mReceiverId = getIntent().getStringExtra(Constants.PARAM_REQUEST_RECEIVER_ID);
        mReceiverName = getIntent().getStringExtra(Constants.PARAM_REQUEST_RECEIVER_NAME);

        mProvidersArray = Constants.mOfficersArray;

        imgLogo = (ImageView) findViewById(R.id.imgTitle);
        imgLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateRequestActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setVisibility(View.GONE);

        BoomMenuButton bmb = (BoomMenuButton) findViewById(R.id.bmb);
        bmb.setVisibility(View.GONE);

        txtFromName = (TextView) findViewById(R.id.txtFromName);
        txtFromName.setText(prefs.getUserName());

        edtTitle = (EditText) findViewById(R.id.edtReqTitle);

        txtDateTime = (TextView) findViewById(R.id.txtDateTime);
        txtDateTime.setOnClickListener(this);

        txtProviderId = (TextView) findViewById(R.id.txtProviderId);
        txtProvider = (TextView) findViewById(R.id.lstProviders);
        if (mReceiverName != null && !mReceiverName.isEmpty() &&
                mReceiverId != null && !mReceiverId.isEmpty()) {
            txtProvider.setText(mReceiverName);
            txtProviderId.setText(mReceiverId);
        }

        txtProvider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiatePopUp(mProvidersArray, (TextView)v, (TextView)v, v.getWidth());
            }
        });

        rdAppointment = (RadioButton) findViewById(R.id.rdAppointment);
        rdAppointment.setChecked(true);
        rdAppointment.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    mReqType = Constants.REQUEST_APPOINTMENT;
                    Drawable appointment = getResources().getDrawable(R.mipmap.icon_appointment3);
                    Drawable lunch = getResources().getDrawable(R.mipmap.icon_lunch2);
                    rdAppointment.setCompoundDrawablesWithIntrinsicBounds(null, appointment, null, null);
                    rdLunch.setCompoundDrawablesWithIntrinsicBounds(null, lunch, null, null);
                }
            }
        });
        rdLunch = (RadioButton) findViewById(R.id.rdLunch);
        rdLunch.setChecked(false);
        rdLunch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    mReqType = Constants.REQUEST_LUNCH;
                    Drawable appointment = getResources().getDrawable(R.mipmap.icon_appointment2);
                    Drawable lunch = getResources().getDrawable(R.mipmap.icon_lunch3);
                    rdAppointment.setCompoundDrawablesWithIntrinsicBounds(null, appointment, null, null);
                    rdLunch.setCompoundDrawablesWithIntrinsicBounds(null, lunch, null, null);
                }
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

        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });

        mAdapter = new ListAdapter(this);
    }

    private void registerReceiver() {
        registerReceiver(mMessageReceiver, new IntentFilter(SalesGcmListenerService.ACTION_MESSAGE_NOTIFICATION));
    }

    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intentMessageShares = new Intent(CreateRequestActivity.this, MyMessagesActivity.class);
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
            Intent intentRequests = new Intent(CreateRequestActivity.this, MyRequestActivity.class);
            intentRequests.putExtra(Constants.PARAM_USER_ID, prefs.getUserId());
            startActivity(intentRequests);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver();
        registerRequestReceiver();

//        loadOfficersInformation();
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mMessageReceiver);
        unregisterReceiver(mRequestReceiver);
    }

    @Override
    public void onClick(View v) {
        Calendar now = Calendar.getInstance();

        switch (v.getId()) {
            case R.id.txtDateTime:
                mDateTime = "";
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        CreateRequestActivity.this,
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
                CreateRequestActivity.this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                false
        );
        tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        CreateRequestActivity.this,
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
        String hourString = tmpHourOfDay < 10 ? "0"+tmpHourOfDay : ""+tmpHourOfDay;
        String minuteString = minute < 10 ? "0"+minute : ""+minute;
        String secondString = second < 10 ? "0"+second : ""+second;
        String timeType = hourOfDay >= 12 ? "pm" : "am";
        String time = hourString+":"+minuteString + " " + timeType;
        txtDateTime.setText(time + " " + mDateTime);
    }

    private void sendRequest() {
        if (false == Validation.EmptyValidation(edtTitle, getResources().getString(R.string.input_req_title)))
            return;

        if (false == Validation.EmptyValidation(txtProvider, getResources().getString(R.string.input_req_receiver)))
            return;

        if (false == Validation.EmptyValidation(txtDateTime, getResources().getString(R.string.input_req_datetime)))
            return;

        int user_id = Integer.valueOf(prefs.getUserId());
        int receiver_id = Integer.valueOf(txtProviderId.getText().toString());
        String title = edtTitle.getText().toString();
        String content = "";
        String repDate = txtDateTime.getText().toString();
        int handleStatus = 0;
        String creatAt = "";

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        dateFormatter.setLenient(false);
        Date today = new Date();
        creatAt = dateFormatter.format(today);

        Retrofit retrofit = Constants.getRetrofitInstanc();

        HttpInterface.SendRequestInterface sendInterface = retrofit.create(HttpInterface.SendRequestInterface.class);
        Call<SendResponseData> call = sendInterface.sendRequest(title, content, user_id, receiver_id, mReqType, repDate, handleStatus, creatAt);

        final ProgressDialog progress = ProgressDialog.show(this, null, "Please wait...", true);
        call.enqueue(new Callback<SendResponseData>() {
            @Override
            public void onResponse(Call<SendResponseData> call, Response<SendResponseData> response) {
                progress.dismiss();
                SendResponseData responseData = response.body();

                if (responseData == null) {
                    new AlertDialog.Builder(CreateRequestActivity.this)
                            .setTitle(R.string.error_title)
                            .setMessage(R.string.service_error_msg)
                            .setNegativeButton(R.string.ok, null)
                            .show();
                } else {
                    Log.d(TAG, "success = " + responseData.success);
                    if (responseData.success == true) {

                        new AlertDialog.Builder(CreateRequestActivity.this)
                                .setTitle(R.string.success_title)
                                .setMessage("You sent successfully request to provider")
                                .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(CreateRequestActivity.this, MainActivity.class);
                                        //intent.putExtra(Constants.INTENT_PARAM_TOKEN, mDeviceToken);
                                        startActivity(intent);
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

                new AlertDialog.Builder(CreateRequestActivity.this)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.network_error_msg)
                        .setNegativeButton(R.string.ok, null)
                        .show();
            }
        });

    }

    private void loadOfficersInformation() {
        int userId = Integer.parseInt(prefs.getUserId());
        Retrofit retrofit = Constants.getRetrofitInstanc();

        HttpInterface.GetMyProviderInterface httpInterface = retrofit.create(HttpInterface.GetMyProviderInterface.class);
        Call<MyProvidersResponseData> call = httpInterface.getMyProviders(userId, String.valueOf(Constants.USER_ROLE_FRONT_DESK));

        final ProgressDialog progress = ProgressDialog.show(this, null, "Please wait...", true);
        call.enqueue(new Callback<MyProvidersResponseData>() {
            @Override
            public void onResponse(Call<MyProvidersResponseData> call, Response<MyProvidersResponseData> response) {
                progress.dismiss();
                MyProvidersResponseData responseData = response.body();

                if (responseData == null) {
                    new AlertDialog.Builder(CreateRequestActivity.this)
                            .setTitle(R.string.error_title)
                            .setMessage(R.string.service_error_msg)
                            .setNegativeButton(R.string.ok, null)
                            .show();
                } else {
                    Log.d(TAG, "success = " + responseData.success);
                    if (responseData.success == true) {

                        mProvidersArray = responseData.data;

                        mAdapter.notifyDataSetChanged();

                    } else {

                        String message = responseData.error.err_msg;
                        checkErrorMessage(message);

                    }
                }
            }

            @Override
            public void onFailure(Call<MyProvidersResponseData> call, Throwable t) {
                progress.dismiss();

                new AlertDialog.Builder(CreateRequestActivity.this)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.network_error_msg)
                        .setNegativeButton(R.string.ok, null)
                        .show();
            }
        });

    }

    private void initiatePopUp(final ArrayList<ProviderProfileResponseData.DataModel> items, TextView layout1, TextView tv, int width){
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //get the pop-up window i.e.  drop-down layout
        LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.pop_up_window, null);

        layout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        //get the view to which drop-down layout is to be anchored
        pw = new PopupWindow(layout, tv.getLayoutParams().width, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        //Pop-up window background cannot be null if we want the pop-up to listen touch events outside its window
        pw.setBackgroundDrawable(CreateRequestActivity.this.getDrawable(R.drawable.popup_layout));
        pw.setTouchable(true);

        //let pop-up be informed about touch events outside its window. This  should be done before setting the content of pop-up
        pw.setOutsideTouchable(true);
        pw.setWidth(width);
        //pw.setHeight(1800);

        //dismiss the pop-up i.e. drop-down when touched anywhere outside the pop-up
        pw.setTouchInterceptor(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    pw.dismiss();
                    return true;
                }
                return false;
            }
        });

        //provide the source layout for drop-down
        pw.setContentView(layout);

        //anchor the drop-down to bottom-left corner of 'layout1'
        pw.showAsDropDown(layout1);
        //pw.showAtLocation(layout1, Gravity.CENTER, 0, 0);//R.dimen.activity_horizontal_margin, R.dimen.activity_horizontal_margin);

        //populate the drop-down list
        mlist = (ListView) layout.findViewById(R.id.dropDownList);
        mAdapter.setData(items);
        mlist.setAdapter(mAdapter);
        mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                txtProvider.setText(items.get(position).displayName);
                txtProviderId.setText(items.get(position).ID);
                pw.dismiss();

//                if (mProvidersArray.get(position).block_allow_message.equalsIgnoreCase(Constants.BLOCK_REQUEST_MESSAGE)) {
//
//                    new AlertDialog.Builder(CreateRequestActivity.this)
//                            .setTitle(R.string.error_title)
//                            .setMessage(R.string.req_allow_block_error_msg)
//                            .setNegativeButton(R.string.ok, null)
//                            .show();
//                    btnSend.setEnabled(false);
//                } else {
//                    btnSend.setEnabled(true);
//                }
            }
        });
    }

    public class ListAdapter extends BaseAdapter {
        LayoutInflater inflater;
        public ArrayList<ProviderProfileResponseData.DataModel> mProvArray;

        public ListAdapter(Context c) {
            inflater = LayoutInflater.from(c);
            mProvArray = new ArrayList<ProviderProfileResponseData.DataModel>();
        }

        public void setData (ArrayList<ProviderProfileResponseData.DataModel> items) {
            mProvArray.clear();
            if (items != null && items.size() > 0)
                mProvArray.addAll(items);
        }

        public int getCount() {
            return mProvArray.size();
        }

        public Object getItem(int position) {
            return mProvArray.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_user, null);
                holder = new ViewHolder();
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ProviderProfileResponseData.DataModel rep = (ProviderProfileResponseData.DataModel) getItem(position);

            holder.imgPhoto = (ImageView) convertView.findViewById(R.id.imgPhoto);
            if (rep.logoUrl != null && rep.logoUrl.isEmpty() == false) {
                Picasso.with(CreateRequestActivity.this)
                        .load(rep.logoUrl)
                        .placeholder(R.mipmap.default_avatar)
                        .error(R.mipmap.default_avatar)
                        .fit()
                        .into(holder.imgPhoto);
            } else {
                holder.imgPhoto.setImageResource(R.mipmap.default_avatar);
            }

            holder.txtName = (TextView) convertView.findViewById(R.id.txtName);
            holder.txtName.setText(rep.displayName);

            holder.txtRole = (TextView) convertView.findViewById(R.id.txtRole);
            final int roleId = Integer.valueOf(rep.role);
            holder.txtRole.setText(Constants.roleArray[roleId]);

            return convertView;
        }
    }

    class ViewHolder {
        ImageView imgPhoto;
        ImageView imgLogo;
        TextView txtName;
        TextView txtRole;
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
