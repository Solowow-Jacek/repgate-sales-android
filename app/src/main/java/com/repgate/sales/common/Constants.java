package com.repgate.sales.common;

import android.support.annotation.ColorInt;

import com.repgate.sales.data.ProviderProfileResponseData;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Constants {

    public static int PASSWROD_MIN_LENGTH = 6;

    // Fragment Index
    public static final int FRAGMENT_MY_REPS           = 1;
    public static final int FRAGMENT_MY_PROVIDERS      = 2;
    public static final int FRAGMENT_COMANINES         = 3;
    public static final int FRAGMENT_PROFILE           = 4;

    // Task Index
    public static final int TASK_PHOTO_LOAD            = 1001;

    // Payment Type
    public static final int PAY_REP_YEAR        = 1;
    public static final int PAY_REP_MONTH       = 2;
    public static final int PAY_REP_ADD         = 3;
    public static final int PAY_DOC_MONTH       = 4;

    // Push Notification
    public static final int NOTIFICATION_TYPE_CREATE_MESSAGE    = 1;
    public static final int NOTIFICATION_TYPE_CREATE_REQUEST    = 2;
    public static final int NOTIFICATION_TYPE_CONFIRM_REQUEST   = 3;
    public static final int NOTIFICATION_TYPE_CHANGE_REQUEST    = 4;
    public static final int NOTIFICATION_TYPE_CANCEL_REQUEST    = 5;
    public static final int NOTIFICATION_TYPE_JOIN_USER      = 6;

    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String SEND_TOKEN_TO_UI = "sendTokenToUI";

    public static final String INTENT_PARAM_TOKEN = "intent_param_token";

    //App Type
    public static final int APP_TYPE_HEALTHCARE = 1;
    public static final int APP_TYPE_SALESREP = 2;

    //Request Status
    public static final int REQUEST_STATUS_INIT = 0;
    public static final int REQUEST_STATUS_CONFIRMED = 1;;
    public static final int REQUEST_STATUS_CHANGED = 2;
    public static final int REQUEST_STATUS_CANCELED = 3;

    //Request Action
    public static final int REQUEST_ACTION_CONFIRM = 1;
    public static final int REQUEST_ACTION_CHANGE  = 2;
    public static final int REQUEST_ACTION_CANCEL  = 3;

    //App Role
    public static final int USER_ROLE_UNDEFINED = 0;
    public static final int USER_ROLE_ADMINISTRATOR = 1;
    public static final int USER_ROLE_PHYSICIAN = 2;
    public static final int USER_ROLE_PHYSICIAN_ASSISTANT = 3;
    public static final int USER_ROLE_NURSE_PRACTIONER = 4;
    public static final int USER_ROLE_FRONT_DESK = 5;
    public static final int USER_ROLE_SALES_REP = 6;
    public static final String BLOCK_REQUEST_MESSAGE = "block";
    public static final String ALLOW_REQUEST_MESSAGE = "allow";

    //Device Type
    public static final int USER_DEVICE_TYPE_ANDROID = 1;
    public static final int USER_DEVICE_TYPE_IOS = 2;
    public static final int USER_DEVICE_TYPE_PC = 3;

    public static final int REQUEST_PERMISSION_CAMERA_CODE = 1101;
    public static final int REQUEST_PERMISSION_STORAGE_CODE = 1102;

    public static final int REQUEST_CHOOSE_AVATAR_CODE = 2001;
    public static final int REQUEST_CHOOSE_LOGO_CODE = 2002;
    public static final int REQUEST_TAKE_PHOTO_CODE = 2003;
    public static final int REQUSET_PAYMENT_CODE = 2004;

    public static final String PARAM_ROLE_ID = "param_role_id";
    public static final String PARAM_ROLE_NAME = "param_role_name";
    public static final String PARAM_USER_AVATAR = "param_user_avatar";
    public static final String PARAM_USER_NAME = "param_user_name";
    public static final String PARAM_USER_ID = "param_user_id";
    public static final String PARAM_USER_ADDRESS = "param_user_address";
    public static final String PARAM_USER_PHONE = "param_user_phone";
    public static final String PARAM_USER_SPECIALTY = "param_user_specialty";
    public static final String PARAM_USER_INTEREST = "param_user_interest";
    public static final String PARAM_USER_MISS_MSG = "param_user_miss_msg";
    public static final String PARAM_USER_MISS_REQ = "param_user_miss_req";

    public static final String USER_JOIN_AMOUNT = "5";

    public static final String[] roleArray = new String[]{"UNDEFINED", "ADMINISTRATOR", "PHYSICIAN", "PHYSICIAN ASSISTANT", "NURSE PRACTIONER", "OFFICE", "SALES REP"};

    public static final String PARAM_MESSAGE_ID = "param_message_id";
    public static final String PARAM_MESSAGE_TITLE = "param_message_title";
    public static final String PARAM_MESSAGE_SENDER_ID = "param_message_sender_id";
    public static final String PARAM_MESSAGE_SENDER_IMAGE = "param_message_sender_image";
    public static final String PARAM_MESSAGE_SENDER_ROLE_ID = "param_message_sender_role_id";
    public static final String PARAM_MESSAGE_SENDER_NAME = "param_message_sender_name";
    public static final String PARAM_MESSAGE_RECEIVER_ID = "param_message_receiver_id";
    public static final String PARAM_MESSAGE_RECEIVER_NAME = "param_message_receiver_name";
    public static final String PARAM_MESSAGE_DATE = "param_message_date";
    public static final String PARAM_MESSAGE_PDF_ATTACH = "param_message_pdf_attach";
    public static final String PARAM_MESSAGE_VIDEO_ATTACH = "param_message_video_attach";
    public static final String PARAM_MESSAGE_BODY = "param_message_body";
    public static final String PARAM_MESSAGE_TYPE = "param_message_type";
    public static final String PARAM_MESSAGE_IS_NEW = "param_message_is_new";

    public static final String PARAM_REQUEST_ID = "param_request_id";
    public static final String PARAM_REQUEST_TITLE = "param_request_title";
    public static final String PARAM_REQUEST_SENDER_ID = "param_request_sender_id";
    public static final String PARAM_REQUEST_SENDER_NAME = "param_request_sender_name";
    public static final String PARAM_REQUEST_RECEIVER_ID = "param_request_receiver_id";
    public static final String PARAM_REQUEST_RECEIVER_NAME = "param_request_receiver_name";
    public static final String PARAM_REQUEST_DATE = "param_request_date";
    public static final String PARAM_REQUEST_TYPE = "param_request_type";
    public static final String PARAM_REQUEST_STATUS = "param_request_status";

    public static final String[] reqArray = new String[]{"Lunch", "Appointment"};
    public static final String[] statusArray = new String[]{"No Action", "Confirmed", "Changed", "Canceled"};
    public static final String[] actionArray = new String[]{"No Action", "Confirm", "Change", "Cancel"};

    public static final String PARAM_TUTORIALS_TYPE = "param_tutorials_type";

    public static final int TUTORIAL_MESSAGE = 0;
    public static final int TUTORIAL_REQUEST = 1;

    public static final int CREATE_MESSAGE = 0;
    public static final int REPLY_MESSAGE = 1;
    public static final int FORWARD_MESSAGE = 2;

    public static final int CONFIRM = 1;
    public static final int CHANGE  = 2;
    public static final int CANCEL  = 3;

    public static final int REQUEST_LUNCH = 1;
    public static final int REQUEST_APPOINTMENT  = 2;

    public static final int RECV_OFFICERS = 1;
    public static final int RECV_PROVIDERS = 2;

    public static ArrayList<ProviderProfileResponseData.DataModel> mOfficersArray;
    public static ArrayList<ProviderProfileResponseData.DataModel> mProvidersArray;

    @ColorInt
    public static final int ham_button_color       = 0x337ab7;

    public static Retrofit getRetrofitInstanc() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();

//        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .readTimeout(300, TimeUnit.SECONDS)
                .connectTimeout(300, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://repgate.com/wp-json/wp/v2/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }
}