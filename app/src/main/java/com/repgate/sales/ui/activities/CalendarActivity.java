package com.repgate.sales.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.gfranks.collapsible.calendar.CollapsibleCalendarView;
import com.github.tibolte.agendacalendarview.AgendaCalendarView;
import com.github.tibolte.agendacalendarview.CalendarPickerController;
import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;
import com.github.tibolte.agendacalendarview.models.DayItem;
import com.repgate.sales.R;
import com.repgate.sales.common.AppPreferences;
import com.repgate.sales.common.Constants;
import com.repgate.sales.data.AllRequestResponseData;
import com.repgate.sales.data.RequestResponseData;
import com.repgate.sales.http.HttpInterface;
import com.repgate.sales.service.SalesGcmListenerService;
import com.repgate.sales.util.DrawableEventRenderer;
import com.repgate.sales.util.Event;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by developer on 4/27/2016.
 */
public class CalendarActivity extends Activity implements CollapsibleCalendarView.Listener<Event> {

    private static final String LOG_TAG = CalendarActivity.class.getSimpleName();

    public AppPreferences prefs;

    private CollapsibleCalendarView mCalendarView;
    private ListView mListView;
    private EventListAdapter mAdapter;

    public ArrayList<AllRequestResponseData.RequestData> mRequestArray;
    public static final int REQUSET_PAYMENT_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        prefs = new AppPreferences(this);

        mCalendarView = (CollapsibleCalendarView) findViewById(R.id.calendar);
        mListView = (ListView) findViewById(R.id.calendar_event_list);

        LocalDate minDate = LocalDate.now().minusYears(1);
        LocalDate maxDate = LocalDate.now().plusYears(1);

        mCalendarView.setMinDate(minDate);
        mCalendarView.setMaxDate(maxDate);
        mCalendarView.setListener(this);
    }

    private void registerReceiver() {
        registerReceiver(mMessageReceiver, new IntentFilter(SalesGcmListenerService.ACTION_MESSAGE_NOTIFICATION));
    }

    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intentMessageShares = new Intent(CalendarActivity.this, MyMessagesActivity.class);
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
            Intent intentRequests = new Intent(CalendarActivity.this, MyRequestActivity.class);
            intentRequests.putExtra(Constants.PARAM_USER_ID, prefs.getUserId());
            startActivity(intentRequests);
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
        loadAllRequestInformation();
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver();
        registerRequestReceiver();

        loadAllRequestInformation();
    }

    private void loadAllRequestInformation() {

        int user_id = Integer.valueOf(this.prefs.getUserId());

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

        HttpInterface.GetAllRequestsInterface httpInterface = retrofit.create(HttpInterface.GetAllRequestsInterface.class);
        Call<AllRequestResponseData> call = httpInterface.getAllRequests(user_id, 0);

        final ProgressDialog progress = ProgressDialog.show(this, null, "Please wait...", true);
        call.enqueue(new Callback<AllRequestResponseData>() {
            @Override
            public void onResponse(Call<AllRequestResponseData> call, Response<AllRequestResponseData> response) {
                progress.dismiss();
                AllRequestResponseData responseData = response.body();

                if (responseData == null) {
                    new AlertDialog.Builder(CalendarActivity.this)
                            .setTitle(R.string.error_title)
                            .setMessage(R.string.service_error_msg)
                            .setNegativeButton(R.string.ok, null)
                            .show();
                } else {
                    Log.d(LOG_TAG, "success = " + responseData.success);
                    if (responseData.success == true) {

                        mRequestArray = responseData.data;

                        mCalendarView.setEvents(getEvents());

                    } else {

                        String message = responseData.error.err_msg;
                        checkErrorMessage(message);

                    }
                }
            }

            @Override
            public void onFailure(Call<AllRequestResponseData> call, Throwable t) {
                progress.dismiss();

                new AlertDialog.Builder(CalendarActivity.this)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.network_error_msg)
                        .setNegativeButton(R.string.ok, null)
                        .show();
            }
        });

    }

    private List<Event> getEvents() {
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < mRequestArray.size(); i ++) {
            for (int ii = 0; ii < mRequestArray.get(i).reqs.size(); ii ++) {
                String ID = mRequestArray.get(i).reqs.get(ii).ID;
                String sender = "From: " + mRequestArray.get(i).reqs.get(ii).senderName;
                String receiver = "To: " + mRequestArray.get(i).reqs.get(ii).receiverName;
                String title = mRequestArray.get(i).reqs.get(ii).title;
                String requestType = mRequestArray.get(i).reqs.get(ii).requestType;
                String requestDateTime = mRequestArray.get(i).reqs.get(ii).requestDateTime;
                String handleStatus = mRequestArray.get(i).reqs.get(ii).handleStatus;

                if (requestDateTime == null)
                    continue;

                String parts[] = requestDateTime.split(" ");
                if (parts.length < 3)
                    continue;
                if (parts[2].equalsIgnoreCase("null"))
                    continue;

                String parts1[] = parts[2].split("-");
                if (parts1.length < 3)
                    continue;
                int year = Integer.parseInt(parts1[0]);
                int month = Integer.parseInt(parts1[1]) - 1;
                int day = Integer.parseInt(parts1[2]);

                Calendar startTime = Calendar.getInstance();
                startTime.set(Calendar.YEAR, year);
                startTime.set(Calendar.MONTH, month);
                startTime.set(Calendar.DAY_OF_MONTH, day);

                Calendar endTime = Calendar.getInstance();
                endTime.set(Calendar.YEAR, year);
                endTime.set(Calendar.MONTH, month);
                endTime.set(Calendar.DAY_OF_MONTH, day);

                String desc = "";
                if (parts.length > 1)
                    desc = parts[0] + " " + parts[1] + " ";

                if (Integer.parseInt(requestType) == Constants.REQUEST_LUNCH)
                    desc = desc + "Lunch";
                else if (Integer.parseInt(requestType) == Constants.REQUEST_APPOINTMENT)
                    desc = desc + "Appointment";

                events.add(new Event(title, sender, receiver, desc, ID, handleStatus, startTime.getTimeInMillis()));
            }
        }
        mCalendarView.toggle();
        return events;
    }

    private class EventListAdapter extends BaseAdapter {

        LayoutInflater inflater;
        private List<Event> mEvents;

        public EventListAdapter(Context context, List<Event> events) {
            inflater = LayoutInflater.from(context);
            mEvents = events;
        }

        public void setEvents(List<Event> events) {
            mEvents = events;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mEvents.size();
        }

        @Override
        public Object getItem(int position) {
            return mEvents.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_schedule_item, null);
                holder = new ViewHolder();
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Event event = (Event) getItem(position);

            holder.txtEventTitle = (TextView) convertView.findViewById(R.id.view_event_title);
            holder.txtEventTitle.setText(event.getTitle());
            holder.txtEventFrom = (TextView) convertView.findViewById(R.id.view_event_from);
            holder.txtEventFrom.setText(event.getSender());
            holder.txtEventTo = (TextView) convertView.findViewById(R.id.view_event_to);
            holder.txtEventTo.setText(event.getReceiver());
            holder.txtEventTime = (TextView) convertView.findViewById(R.id.view_event_time);
            holder.txtEventTime.setText(event.getDateTime());

            holder.imgReqStatus = (ImageView) convertView.findViewById(R.id.imgReqStatus);
            if (event.getStatus().equalsIgnoreCase("1") == true) {
                holder.imgReqStatus.setBackgroundResource(R.drawable.message_request_green_item);
            } else if (event.getStatus().equalsIgnoreCase("2") == true) {
                holder.imgReqStatus.setBackgroundResource(R.drawable.message_request_yellow_item);
            } else if (event.getStatus().equalsIgnoreCase("3") == true) {
                holder.imgReqStatus.setBackgroundResource(R.drawable.message_request_red_item);
            } else {
                holder.imgReqStatus.setBackgroundResource(R.drawable.message_request_darkblue_item);
            }

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CalendarActivity.this, ShowRequestActivity.class);
                    intent.putExtra(Constants.PARAM_USER_ID, prefs.getUserId());
                    intent.putExtra(Constants.PARAM_REQUEST_ID, String.valueOf(event.getEventId()));

                    startActivity(intent);
                }
            });

            return convertView;
        }
    }

    class ViewHolder {
        TextView txtEventTitle;
        TextView txtEventFrom;
        TextView txtEventTo;
        TextView txtEventTime;
        ImageView imgReqStatus;
    }

    @Override
    public void onDateSelected(LocalDate date, List<Event> events) {
        if (mAdapter == null || mListView.getAdapter() == null) {
            mAdapter = new EventListAdapter(this, events);
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.setEvents(events);
        }
    }

    @Override
    public void onMonthChanged(LocalDate date) {
    }

    @Override
    public void onHeaderClick() {
        mCalendarView.toggle();
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
