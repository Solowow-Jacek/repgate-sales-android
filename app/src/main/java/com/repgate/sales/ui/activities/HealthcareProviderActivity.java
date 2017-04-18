package com.repgate.sales.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.repgate.sales.R;
import com.repgate.sales.common.AppPreferences;
import com.repgate.sales.common.Constants;
import com.repgate.sales.data.MyProvidersResponseData;
import com.repgate.sales.data.ProviderProfileResponseData;
import com.repgate.sales.http.HttpInterface;
import com.repgate.sales.service.SalesGcmListenerService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HealthcareProviderActivity extends Activity {

    private final static String TAG = "HealthcareProviderActivity";
    public AppPreferences prefs;

    public ArrayList<ProviderProfileResponseData.DataModel> mProvArray;
    private ListView mProviderView;
    private ListAdapter mAdapter;
    private Button btnHome;
    private ImageView imgLogo;
    private TextView txtTitle;

    private int mRoleID, mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_healthcare_provider);

        prefs = new AppPreferences(this);

        mProvArray = new ArrayList<ProviderProfileResponseData.DataModel>();

        mUserId = getIntent().getIntExtra(Constants.PARAM_USER_ID, 0);
        mRoleID = getIntent().getIntExtra(Constants.PARAM_ROLE_ID, 0);

        btnHome = (Button) findViewById(R.id.action_home);
        btnHome.setVisibility(View.VISIBLE);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMain = new Intent(HealthcareProviderActivity.this, MainActivity.class);
                //intent.putExtra(Constants.INTENT_PARAM_TOKEN, mDeviceToken);
                startActivity(intentMain);
                finish();
            }
        });
        imgLogo = (ImageView) findViewById(R.id.imgTitle);
        imgLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HealthcareProviderActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText("Providers");

        mProviderView = (ListView) findViewById(R.id.lstProviders);
        mAdapter = new ListAdapter(this);
        mProviderView.setAdapter(mAdapter);
    }

    private void registerReceiver() {
        registerReceiver(mMessageReceiver, new IntentFilter(SalesGcmListenerService.ACTION_MESSAGE_NOTIFICATION));
    }

    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intentMessageShares = new Intent(HealthcareProviderActivity.this, MyMessagesActivity.class);
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
            Intent intentRequests = new Intent(HealthcareProviderActivity.this, MyRequestActivity.class);
            intentRequests.putExtra(Constants.PARAM_USER_ID, prefs.getUserId());
            startActivity(intentRequests);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver();
        registerRequestReceiver();

        loadMyProviderInformation();
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mMessageReceiver);
        unregisterReceiver(mRequestReceiver);
    }

    private void loadMyProviderInformation() {
        Retrofit retrofit = Constants.getRetrofitInstanc();

        HttpInterface.GetMyProviderInterface httpInterface = retrofit.create(HttpInterface.GetMyProviderInterface.class);
        Call<MyProvidersResponseData> call = httpInterface.getMyProviders(mUserId, String.valueOf(Constants.USER_ROLE_UNDEFINED));

        final ProgressDialog progress = ProgressDialog.show(this, null, "Please wait...", true);
        call.enqueue(new Callback<MyProvidersResponseData>() {
            @Override
            public void onResponse(Call<MyProvidersResponseData> call, Response<MyProvidersResponseData> response) {
                progress.dismiss();
                MyProvidersResponseData responseData = response.body();

                if (responseData == null) {
                    new AlertDialog.Builder(HealthcareProviderActivity.this)
                            .setTitle(R.string.error_title)
                            .setMessage(R.string.service_error_msg)
                            .setNegativeButton(R.string.ok, null)
                            .show();
                } else {
                    Log.d(TAG, "success = " + responseData.success);
                    if (responseData.success == true) {

                        mProvArray = responseData.data;

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

                new AlertDialog.Builder(HealthcareProviderActivity.this)
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

    public class ListAdapter extends BaseAdapter {
        LayoutInflater inflater;

        public ListAdapter(Context c) {
            inflater = LayoutInflater.from(c);
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
                convertView = inflater.inflate(R.layout.list_provider, null);
                holder = new ViewHolder();
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ProviderProfileResponseData.DataModel prov = (ProviderProfileResponseData.DataModel) getItem(position);

            holder.imgPhoto = (ImageView) convertView.findViewById(R.id.imgPhoto);
            if (prov.logoUrl.isEmpty() == false) {
                Picasso.with(HealthcareProviderActivity.this)
                        .load(prov.logoUrl)
                        .placeholder(R.mipmap.default_avatar)
                        .error(R.mipmap.default_avatar)
                        .fit()
                        .into(holder.imgPhoto);
            } else {
                holder.imgPhoto.setImageResource(R.mipmap.default_avatar);
            }

            holder.txtName = (TextView) convertView.findViewById(R.id.txtName);
            holder.txtName.setText(prov.displayName);

            holder.txtRole = (TextView) convertView.findViewById(R.id.txtRole);
            final int roleId = Integer.valueOf(prov.role);
            holder.txtRole.setText(Constants.roleArray[roleId]);

            holder.txtMissMsgReq = (TextView) convertView.findViewById(R.id.missed_msg_req);
            if (prov.messageNew > 0 || prov.requestNew > 0)
                holder.txtMissMsgReq.setVisibility(View.VISIBLE);
            else
                holder.txtMissMsgReq.setVisibility(View.INVISIBLE);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HealthcareProviderActivity.this, CommunicateActivity.class);
                    intent.putExtra(Constants.PARAM_USER_ID, prov.ID);
                    intent.putExtra(Constants.PARAM_ROLE_ID, prov.role);
                    intent.putExtra(Constants.PARAM_ROLE_NAME, Constants.roleArray[roleId]);
                    intent.putExtra(Constants.PARAM_USER_NAME, prov.displayName);
                    intent.putExtra(Constants.PARAM_USER_AVATAR, prov.logoUrl);
                    intent.putExtra(Constants.PARAM_USER_SPECIALTY, prov.pSpecialty_name);
                    intent.putExtra(Constants.PARAM_USER_INTEREST, prov.area_of_interest);
                    intent.putExtra(Constants.PARAM_USER_MISS_MSG, prov.messageNew);
                    intent.putExtra(Constants.PARAM_USER_MISS_REQ, prov.requestNew);
                    startActivity(intent);
                }
            });
            return convertView;
        }
    }

    class ViewHolder {
        ImageView imgPhoto;
        ImageView imgLogo;
        TextView txtName;
        TextView txtRole;
        TextView txtMissMsgReq;
    }
}
