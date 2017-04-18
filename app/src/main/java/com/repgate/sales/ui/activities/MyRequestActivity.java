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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextInsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;
import com.repgate.sales.R;
import com.repgate.sales.common.AppPreferences;
import com.repgate.sales.common.Constants;
import com.repgate.sales.data.AllRequestResponseData;
import com.repgate.sales.data.RequestResponseData;
import com.repgate.sales.http.HttpInterface;
import com.repgate.sales.service.SalesGcmListenerService;
import com.repgate.sales.view.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MyRequestActivity extends Activity {
    private final static String TAG = "MyRequestActivity";
    AppPreferences prefs;

    private Button btnHome, btnBack;
    private ImageView imgLogo;
    private TextView txtTitle;
    private PullRefreshLayout request_swipe;
    private BoomMenuButton bmb;

    private String mUserId;

    private RecyclerView recyclerView;
    private SectionedRecyclerViewAdapter sectionAdapter;
    public ArrayList<AllRequestResponseData.RequestData> mRequestArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_request);

        mUserId = getIntent().getStringExtra(Constants.PARAM_USER_ID);

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
                        Intent intentMessage = new Intent(MyRequestActivity.this, CreateMessageActivity.class);
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
                        Intent intentRequest = new Intent(MyRequestActivity.this, CreateRequestActivity.class);
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
                        Intent intent = new Intent(MyRequestActivity.this, CalendarActivity.class);
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
                        new AlertDialog.Builder(MyRequestActivity.this)
                                .setTitle(R.string.confirm_title)
                                .setMessage(R.string.are_you_logout)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        prefs.clearUserInformation();

                                        Intent intent = new Intent(MyRequestActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        MyRequestActivity.this.finish();
                                    }
                                })
                                .setNegativeButton(R.string.no, null)
                                .show();
                    }
                });
        bmb.addBuilder(logoutBuilder);

        btnHome = (Button) findViewById(R.id.action_home);
        btnHome.setVisibility(View.GONE);
        imgLogo = (ImageView) findViewById(R.id.imgTitle);
        imgLogo.setVisibility(View.GONE);
        imgLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyRequestActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText("My Requests");
        btnBack = (Button) findViewById(R.id.action_back);
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mRequestArray = new ArrayList<AllRequestResponseData.RequestData>();

        sectionAdapter = new SectionedRecyclerViewAdapter();
        recyclerView = (RecyclerView)findViewById(R.id.lstRequest);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(sectionAdapter);

        request_swipe = (PullRefreshLayout) findViewById(R.id.request_swipe);
        request_swipe.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadAllRequestInformation();
            }
        });
    }

    private void registerReceiver() {
        registerReceiver(mMessageReceiver, new IntentFilter(SalesGcmListenerService.ACTION_MESSAGE_NOTIFICATION));
    }

    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intentMessageShares = new Intent(MyRequestActivity.this, MyMessagesActivity.class);
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
            request_swipe.setRefreshing(true);
            loadAllRequestInformation();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mMessageReceiver);
        unregisterReceiver(mRequestReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver();
        registerRequestReceiver();

        request_swipe.setRefreshing(true);
        loadAllRequestInformation();
    }

    private void loadAllRequestInformation() {

        int user_id = Integer.valueOf(this.prefs.getUserId());

        Retrofit retrofit = Constants.getRetrofitInstanc();

        HttpInterface.GetAllRequestsInterface httpInterface = retrofit.create(HttpInterface.GetAllRequestsInterface.class);
        Call<AllRequestResponseData> call = httpInterface.getAllRequests(user_id, 0);

        call.enqueue(new Callback<AllRequestResponseData>() {
            @Override
            public void onResponse(Call<AllRequestResponseData> call, Response<AllRequestResponseData> response) {
                request_swipe.setRefreshing(false);
                AllRequestResponseData responseData = response.body();

                if (responseData == null) {
                    new AlertDialog.Builder(MyRequestActivity.this)
                            .setTitle(R.string.error_title)
                            .setMessage(R.string.service_error_msg)
                            .setNegativeButton(R.string.ok, null)
                            .show();
                } else {
                    Log.d(TAG, "success = " + responseData.success);

                    sectionAdapter.removeAllSections();

                    if (responseData.success == true) {

                        if (responseData.data.size() > 0) {
                            mRequestArray.clear();
                            mRequestArray = responseData.data;
                            for (int i = 0; i < mRequestArray.size(); i ++) {
                                sectionAdapter.addSection(new ExpandableContactsSection(mRequestArray.get(i).date,
                                        mRequestArray.get(i).reqs));
                            }
                            sectionAdapter.notifyDataSetChanged();
                        } else {
                            mRequestArray.clear();
                            sectionAdapter.notifyDataSetChanged();

                            new AlertDialog.Builder(MyRequestActivity.this)
                                    .setTitle("")
                                    .setMessage("There is no request.")
                                    .setNegativeButton(R.string.ok, null)
                                    .show();
                        }

                    } else {

                        String message = responseData.error.err_msg;
                        checkErrorMessage(message);
                    }
                }
            }

            @Override
            public void onFailure(Call<AllRequestResponseData> call, Throwable t) {
                request_swipe.setRefreshing(false);

                new AlertDialog.Builder(MyRequestActivity.this)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.network_error_msg)
                        .setNegativeButton(R.string.ok, null)
                        .show();
            }
        });

    }

    class ExpandableContactsSection extends StatelessSection {

        String title;
        public ArrayList<RequestResponseData.DataModel> list;
        boolean expanded = true;
        boolean isNew = false;

        public ExpandableContactsSection(String title, ArrayList<RequestResponseData.DataModel> list) {
            super(R.layout.list_request_header, R.layout.list_request);

            this.title = title;
            this.list = list;
            for (int i = 0; i < this.list.size(); i ++ ) {
                if (this.list.get(i).isNew == true &&
                        !this.list.get(i).changerId.equalsIgnoreCase(prefs.getUserId())) {
                    isNew = true;
                    break;
                }
            }
        }

        @Override
        public int getContentItemsTotal() {
            return expanded? list.size() : 0;
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;

            final RequestResponseData.DataModel request = list.get(position);

            if (Integer.parseInt(request.requestType) == Constants.REQUEST_LUNCH)
                itemHolder.txtType.setText("Lunch");
            else if (Integer.parseInt(request.requestType) == Constants.REQUEST_APPOINTMENT)
                itemHolder.txtType.setText("Appointment");

            itemHolder.txtDate.setText(request.requestDateTime);

            itemHolder.txtRequestSender.setText(request.senderName);

            itemHolder.txtTitle.setText(request.title);

//            if (request.handleStatus.equalsIgnoreCase("1") == true) {
//                itemHolder.imgReqStatus.setBackgroundResource(R.drawable.message_request_green_item);
//            } else if (request.handleStatus.equalsIgnoreCase("2") == true) {
//                itemHolder.imgReqStatus.setBackgroundResource(R.drawable.message_request_yellow_item);
//            } else if (request.handleStatus.equalsIgnoreCase("3") == true) {
//                itemHolder.imgReqStatus.setBackgroundResource(R.drawable.message_request_red_item);
//            } else {
//                itemHolder.imgReqStatus.setBackgroundResource(R.drawable.message_request_darkblue_item);
//            }

            if (request.senderImageUrl.isEmpty() == false) {
                Picasso.with(MyRequestActivity.this)
                        .load(request.senderImageUrl)
                        .placeholder(R.mipmap.default_avatar)
                        .error(R.mipmap.default_avatar)
                        .fit()
                        .into(itemHolder.senderImg);
            } else {
                itemHolder.senderImg.setImageResource(R.mipmap.default_avatar);
            }

            if (request.isNew == true && !request.changerId.equalsIgnoreCase(prefs.getUserId())) {
                itemHolder.txtMissReq.setVisibility(View.VISIBLE);
            } else {
                itemHolder.txtMissReq.setVisibility(View.INVISIBLE);
            }

            itemHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MyRequestActivity.this, ShowRequestActivity.class);
                    intent.putExtra(Constants.PARAM_USER_ID, mUserId);
                    intent.putExtra(Constants.PARAM_REQUEST_ID, request.ID);
//                    intent.putExtra(Constants.PARAM_REQUEST_TITLE, request.title);
//                    intent.putExtra(Constants.PARAM_REQUEST_SENDER_ID, request.senderId);
//                    intent.putExtra(Constants.PARAM_REQUEST_SENDER_NAME, request.senderName);
//                    intent.putExtra(Constants.PARAM_REQUEST_DATE, request.requestDateTime);
//                    intent.putExtra(Constants.PARAM_REQUEST_TYPE, request.requestType);
//                    intent.putExtra(Constants.PARAM_REQUEST_STATUS, request.handleStatus);

                    startActivity(intent);
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            final HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            headerHolder.tvTitle.setText(title);
            if (isNew == true) {
                headerHolder.txtMissReq.setVisibility(View.VISIBLE);
            } else {
                headerHolder.txtMissReq.setVisibility(View.INVISIBLE);
            }

            headerHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    expanded = !expanded;
                    headerHolder.imgArrow.setImageResource(
                            expanded ? R.mipmap.icon_up_arrow : R.mipmap.icon_down_arrow
                    );
                    sectionAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        private final TextView tvTitle;
        private final ImageView imgArrow;
        private final TextView txtMissReq;

        public HeaderViewHolder(View view) {
            super(view);

            rootView = view;
            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            imgArrow = (ImageView) view.findViewById(R.id.imgArrow);
            txtMissReq = (TextView) view.findViewById(R.id.missed_request);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        private final TextView txtRequestSender;
        private final TextView txtType;
        private final TextView txtDate;
        private final TextView txtTitle;
        private final TextView txtMissReq;
        private final RoundedImageView senderImg;

        public ItemViewHolder(View view) {
            super(view);

            rootView = view;
            txtType = (TextView) view.findViewById(R.id.txtType);
            txtDate = (TextView) view.findViewById(R.id.txtDate);
            txtRequestSender = (TextView) view.findViewById(R.id.txtRequestSender);
            txtTitle = (TextView) view.findViewById(R.id.txtTitle);
            senderImg = (RoundedImageView) view.findViewById(R.id.imgPicture);
            txtMissReq = (TextView) view.findViewById(R.id.missed_request);
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
