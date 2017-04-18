package com.repgate.sales.ui.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nightonke.boommenu.BoomMenuButton;
import com.repgate.sales.R;
import com.repgate.sales.common.AppPreferences;
import com.repgate.sales.common.Constants;
import com.repgate.sales.data.AttachFileData;
import com.repgate.sales.data.MyProvidersResponseData;
import com.repgate.sales.data.ProviderProfileResponseData;
import com.repgate.sales.data.SendResponseData;
import com.repgate.sales.data.UploadResponseData;
import com.repgate.sales.http.HttpInterface;
import com.repgate.sales.service.SalesGcmListenerService;
import com.repgate.sales.util.Validation;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CreateMessageActivity extends Activity implements View.OnClickListener {
    private final static String TAG = "CreateMessageActivity";
    public AppPreferences prefs;

    private String mSenderId;
    public ArrayList<ProviderProfileResponseData.DataModel> mRecvsArray;

    private ListAdapter mAdapter;
    private PopupWindow pw;
    private ListView mlist;
    private TextView txtProvider, txtProviderId, txtFromName, txtReplyForward, txtTitle;
    private Button btnBack, btnSendMsg, btnAttachFiles;
    private EditText edtTitle, edtMessage;
    private RadioButton rdOfficer, rdProvider;
    private LinearLayout titleLayer, replyForwardLayout, attachLayout;
    private ImageView imgLogo;

    private String strTitle, strMsgBody, strSenderId, strSenderName, attachLink;
    private int msgType;
    private String attachedURL = "";
    private int attachCnt = 0;

    private static final int REQUEST_FILE_SELECT_CODE = 1000;
    private static final int REQUEST_PDF_FILE_SELECT_CODE = 1001;
    private static final int REQUEST_VIDEO_FILE_SELECT_CODE = 1002;

    public static final int FILE_TYPE_VIDEO = 2;
    public static final int FILE_TYPE_PDF = 1;
    public static final int FILE_TYPE_IMAGE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_message);

        prefs = new AppPreferences(this);
        strSenderId = getIntent().getStringExtra(Constants.PARAM_MESSAGE_SENDER_ID);
        strTitle = getIntent().getStringExtra(Constants.PARAM_MESSAGE_TITLE);
        strMsgBody = getIntent().getStringExtra(Constants.PARAM_MESSAGE_BODY);
        strSenderName = getIntent().getStringExtra(Constants.PARAM_MESSAGE_SENDER_NAME);
        msgType = getIntent().getIntExtra(Constants.PARAM_MESSAGE_TYPE, 0);
        attachLink = getIntent().getStringExtra(Constants.PARAM_MESSAGE_PDF_ATTACH);

        mRecvsArray = Constants.mOfficersArray;

        imgLogo = (ImageView) findViewById(R.id.imgTitle);
        imgLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateMessageActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setVisibility(View.GONE);

        BoomMenuButton bmb = (BoomMenuButton) findViewById(R.id.bmb);
        bmb.setVisibility(View.GONE);

        replyForwardLayout = (LinearLayout) findViewById(R.id.replyForwardLayout);
        if (msgType == Constants.CREATE_MESSAGE || strMsgBody == null || strMsgBody.isEmpty()) {
            replyForwardLayout.setVisibility(View.GONE);
        } else {
            replyForwardLayout.setVisibility(View.VISIBLE);
            txtReplyForward = (TextView) findViewById(R.id.txtReplyForward);
            txtReplyForward.setText(strMsgBody);
        }
        attachLayout = (LinearLayout) findViewById(R.id.attachLayout);
        if (attachLink != null && !attachLink.isEmpty()) {
            attachedURL = attachLink;
            String parts[] = attachLink.split("hufeixiaomi");
            for (int i = 0; i < parts.length; i ++) {
                String filename = parts[i].substring(parts[i].lastIndexOf("/") + 1);
                AttachFileData attachData = new AttachFileData();
                attachData.fileName = filename;
                if (filename.contains("pdf")) {
                    attachData.fileType = CreateMessageActivity.FILE_TYPE_PDF;
                } else if (filename.contains("png") || filename.contains("jpg")) {
                    attachData.fileType = CreateMessageActivity.FILE_TYPE_IMAGE;
                } else if (filename.contains("mp4")) {
                    attachData.fileType = CreateMessageActivity.FILE_TYPE_VIDEO;
                } else {
                    attachData.fileType = CreateMessageActivity.FILE_TYPE_VIDEO;
                }
                attachData.url = parts[i];

                addAttachView(attachData);
            }
        }

        btnAttachFiles = (Button) findViewById(R.id.action_attach);
        btnAttachFiles.setVisibility(View.VISIBLE);
        btnAttachFiles.setOnClickListener(this);
        btnSendMsg = (Button) findViewById(R.id.action_send);
        btnSendMsg.setVisibility(View.VISIBLE);
        btnSendMsg.setOnClickListener(this);

        btnBack = (Button) findViewById(R.id.action_back);
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        txtFromName = (TextView) findViewById(R.id.txtFromName);
        txtFromName.setText(prefs.getUserName());

        edtTitle = (EditText) findViewById(R.id.edtMsgTitle);
        if (strTitle != null && !strTitle.isEmpty())
            edtTitle.setText(strTitle);

        edtMessage = (EditText) findViewById(R.id.edtMsgBody);

        rdOfficer = (RadioButton) findViewById(R.id.officer);
        rdOfficer.setChecked(true);
        rdOfficer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    mRecvsArray = Constants.mOfficersArray;
                }
            }
        });
        rdProvider = (RadioButton) findViewById(R.id.provider);
        rdProvider.setChecked(false);
        rdProvider.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    mRecvsArray = Constants.mProvidersArray;
                }
            }
        });

        txtProviderId = (TextView) findViewById(R.id.txtProviderId);
        txtProvider = (TextView) findViewById(R.id.lstProviders);
        txtProvider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiatePopUp(mRecvsArray, (TextView)v, (TextView)v, v.getWidth());
            }
        });
        if (strSenderName != null && !strSenderName.isEmpty() &&
                strSenderId != null && !strSenderId.isEmpty()) {
            for (int i = 0; i < Constants.mOfficersArray.size(); i ++) {
                int id1 = Integer.valueOf(Constants.mProvidersArray.get(i).ID);
                int id2 = Integer.valueOf(strSenderId);
                if (id1 == id2) {
                    if (Constants.mOfficersArray.get(i).block_allow_message.equalsIgnoreCase(Constants.BLOCK_REQUEST_MESSAGE)) {
                        new AlertDialog.Builder(CreateMessageActivity.this)
                                .setTitle("Block")
                                .setMessage("Provider did block the message.")
                                .setNegativeButton(R.string.ok, null)
                                .show();
                        txtProvider.setText("");
                        txtProviderId.setText("");
                    } else {
                        txtProvider.setText(strSenderName);
                        txtProviderId.setText(strSenderId);
                    }

                    rdOfficer.setChecked(true);
                    rdProvider.setChecked(false);
                    break;
                }
            }

            for (int i = 0; i < Constants.mProvidersArray.size(); i ++) {
                int id1 = Integer.valueOf(Constants.mProvidersArray.get(i).ID);
                int id2 = Integer.valueOf(strSenderId);
                if (id1 == id2) {
                    if (Constants.mProvidersArray.get(i).block_allow_message.equalsIgnoreCase(Constants.BLOCK_REQUEST_MESSAGE)) {
                        new AlertDialog.Builder(CreateMessageActivity.this)
                                .setTitle("Block")
                                .setMessage("Provider did block the message.")
                                .setNegativeButton(R.string.ok, null)
                                .show();
                        txtProvider.setText("");
                        txtProviderId.setText("");
                    } else {
                        txtProvider.setText(strSenderName);
                        txtProviderId.setText(strSenderId);
                    }

                    rdOfficer.setChecked(false);
                    rdProvider.setChecked(true);

                    break;
                }
            }
        }

        mAdapter = new ListAdapter(this);
    }

    private void registerReceiver() {
        registerReceiver(mMessageReceiver, new IntentFilter(SalesGcmListenerService.ACTION_MESSAGE_NOTIFICATION));
    }

    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intentMessageShares = new Intent(CreateMessageActivity.this, MyMessagesActivity.class);
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
            Intent intentRequests = new Intent(CreateMessageActivity.this, MyRequestActivity.class);
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

//    private void loadOfficersInformation(final int recvType) {
//        int userId = Integer.parseInt(prefs.getUserId());
//        String roleNames = String.valueOf(Constants.USER_ROLE_UNDEFINED);
//
//        if (recvType == Constants.RECV_OFFICERS) {
//            roleNames = String.valueOf(Constants.USER_ROLE_FRONT_DESK);
//        } else if (recvType == Constants.RECV_PROVIDERS) {
//            roleNames = String.valueOf(Constants.USER_ROLE_PHYSICIAN) + ","
//                    + String.valueOf(Constants.USER_ROLE_PHYSICIAN_ASSISTANT) + ","
//                    + String.valueOf(Constants.USER_ROLE_NURSE_PRACTIONER);
//        }
//
//        Retrofit retrofit = Constants.getRetrofitInstanc();
//
//        HttpInterface.GetMyProviderInterface httpInterface = retrofit.create(HttpInterface.GetMyProviderInterface.class);
//        Call<MyProvidersResponseData> call = httpInterface.getMyProviders(userId, roleNames);
//
//        final ProgressDialog progress = ProgressDialog.show(this, null, "Please wait...", true);
//        call.enqueue(new Callback<MyProvidersResponseData>() {
//            @Override
//            public void onResponse(Call<MyProvidersResponseData> call, Response<MyProvidersResponseData> response) {
//                progress.dismiss();
//                MyProvidersResponseData responseData = response.body();
//
//                if (responseData == null) {
//                    new AlertDialog.Builder(CreateMessageActivity.this)
//                            .setTitle(R.string.error_title)
//                            .setMessage(R.string.service_error_msg)
//                            .setNegativeButton(R.string.ok, null)
//                            .show();
//                } else {
//                    Log.d(TAG, "success = " + responseData.success);
//                    if (responseData.success == true) {
//
//                        if (recvType == Constants.RECV_OFFICERS) {
//                            mOfficersArray = responseData.data;
//                        } else if (recvType == Constants.RECV_PROVIDERS) {
//                            mProvidersArray = responseData.data;
//                        }
//                        mOfficersArray = responseData.data;
//                        mAdapter.notifyDataSetChanged();
//
//                        if (strSenderName != null && !strSenderName.isEmpty() &&
//                                strSenderId != null && !strSenderId.isEmpty()) {
//                            for (int i = 0; i < mOfficersArray.size(); i ++) {
//                                if (Integer.valueOf(mOfficersArray.get(i).ID) == Integer.valueOf(strSenderId)) {
//                                    if (mOfficersArray.get(i).block_allow_message.equalsIgnoreCase(Constants.BLOCK_REQUEST_MESSAGE)) {
//                                        new AlertDialog.Builder(CreateMessageActivity.this)
//                                                .setTitle("Block")
//                                                .setMessage("Provider did block the message.")
//                                                .setNegativeButton(R.string.ok, null)
//                                                .show();
//                                        txtProvider.setText("");
//                                        txtProviderId.setText("");
//                                    } else {
//                                        txtProvider.setText(strSenderName);
//                                        txtProviderId.setText(strSenderId);
//                                    }
//
//                                    break;
//                                }
//                            }
//                        }
//
//                    } else {
//
//                        String message = responseData.error.err_msg;
//                        checkErrorMessage(message);
//
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<MyProvidersResponseData> call, Throwable t) {
//                progress.dismiss();
//
//                new AlertDialog.Builder(CreateMessageActivity.this)
//                        .setTitle(R.string.error_title)
//                        .setMessage(R.string.network_error_msg)
//                        .setNegativeButton(R.string.ok, null)
//                        .show();
//            }
//        });
//
//    }

    public void checkErrorMessage(String error) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error_title)
                .setMessage(error)
                .setNegativeButton(R.string.ok, null)
                .show();
        return;
    }

    private void initiatePopUp(final ArrayList<ProviderProfileResponseData.DataModel> items, TextView layout1, TextView tv, int width){
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //get the pop-up window i.e.  drop-down layout
        LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.pop_up_window, null);

        layout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        //get the view to which drop-down layout is to be anchored
        pw = new PopupWindow(layout, tv.getLayoutParams().width, LinearLayout.LayoutParams.MATCH_PARENT, true);

        //Pop-up window background cannot be null if we want the pop-up to listen touch events outside its window
        pw.setBackgroundDrawable(CreateMessageActivity.this.getDrawable(R.drawable.popup_layout));
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

                if (items.get(position).block_allow_message.equalsIgnoreCase(Constants.BLOCK_REQUEST_MESSAGE)) {
                    new AlertDialog.Builder(CreateMessageActivity.this)
                            .setTitle("Block")
                            .setMessage("Provider did block the message.")
                            .setNegativeButton(R.string.ok, null)
                            .show();
                    txtProvider.setText("");
                    txtProviderId.setText("");
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_attach:
                if (attachCnt < 10) {
                    showFileChooser();
                } else {
                    new AlertDialog.Builder(CreateMessageActivity.this)
                            .setTitle("Error")
                            .setMessage("Count of attached files have to small than 10.")
                            .setNegativeButton(R.string.ok, null)
                            .show();
                }
                break;
            case R.id.action_send:
                sendMessage();
                break;
            default:
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;

        Uri uri  = data.getData();
        File pdfVideoFile = null;
        int fileType = FILE_TYPE_PDF;
        String fileName = "";

        if (uri == null) {
            new AlertDialog.Builder(CreateMessageActivity.this)
                    .setTitle("Error")
                    .setMessage("File link is null.")
                    .setNegativeButton(R.string.ok, null)
                    .show();

            return;
        }

        if (requestCode == REQUEST_FILE_SELECT_CODE && resultCode == RESULT_OK) {

            try {

                Log.d(TAG, "file path:" + uri.getPath());

                String mimeType = getContentResolver().getType(uri);
                String realPath;

                // upload Video, Photo
                if ( uri.toString().startsWith("file://") ||
                        isDownloadsDocument(uri) ||
                        isDownloadsSkyDrive(uri) ||
                        isDownloadsInternalStorage(uri) ||
                        isDownloadsStorage(uri)) {
                    String sourcePath = getExternalFilesDir(null).toString();
                    fileName = uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1);
                    if (mimeType != null) {
                        if (mimeType.contains("image")) {
                            fileType = FILE_TYPE_IMAGE;
                            fileName = fileName + ".png";
                        }
                        else if (mimeType.contains("video")){
                            fileType = FILE_TYPE_VIDEO;
                            fileName = fileName + ".mp4";
                        } else if (mimeType.contains("pdf")){
                            fileType = FILE_TYPE_PDF;
                            fileName = fileName + ".pdf";
                        } else {
                            new AlertDialog.Builder(CreateMessageActivity.this)
                                    .setTitle(R.string.error_title)
                                    .setMessage("Invalid file.")
                                    .setNegativeButton(R.string.ok, null)
                                    .show();
                            return;
                        }
                    } else {
                        if (fileName.contains("jpg") || fileName.contains("png")) {
                            fileType = FILE_TYPE_IMAGE;
                        } else if (fileName.contains("pdf")) {
                            fileType = FILE_TYPE_PDF;
                        } else if (fileName.contains("mp4")){
                            fileType = FILE_TYPE_VIDEO;
                        } else {
                            new AlertDialog.Builder(CreateMessageActivity.this)
                                    .setTitle(R.string.error_title)
                                    .setMessage("Invalid file.")
                                    .setNegativeButton(R.string.ok, null)
                                    .show();
                            return;
                        }
                    }

                    pdfVideoFile = new File(sourcePath + "/" + fileName);
                    try {
                        ProgressDialog progress = ProgressDialog.show(this, null, "Please wait...", true);
                        copyFileStream(pdfVideoFile, uri, this);
                        progress.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (mimeType != null) {
                        if (mimeType.contains("image")) {
                            realPath = getRealPathFromURI(uri, FILE_TYPE_IMAGE);
                            fileType = FILE_TYPE_IMAGE;
                            fileName = ".png";
                        }
                        else if (mimeType.contains("video")){
                            realPath = getRealPathFromURI(uri, FILE_TYPE_VIDEO);
                            fileType = FILE_TYPE_VIDEO;
                            fileName = ".mp4";
                        } else if (mimeType.contains("pdf")){
                            realPath = getRealPathFromURI(uri, FILE_TYPE_PDF);
                            fileType = FILE_TYPE_PDF;
                            fileName = ".pdf";
                        } else {
                            new AlertDialog.Builder(CreateMessageActivity.this)
                                    .setTitle(R.string.error_title)
                                    .setMessage("Invalid file.")
                                    .setNegativeButton(R.string.ok, null)
                                    .show();
                            return;
                        }

                        pdfVideoFile = new File(realPath);
                        fileName = realPath.substring(realPath.lastIndexOf("/") + 1);
                    } else {
                        new AlertDialog.Builder(CreateMessageActivity.this)
                                .setTitle(R.string.error_title)
                                .setMessage("Invalid file.")
                                .setNegativeButton(R.string.ok, null)
                                .show();
                        return;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        uploadFileToServer(pdfVideoFile, fileName, fileType);

        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String getRealPathFromURI (Uri contentUri, int type) {
        // where id is equal to
        Cursor cursor;
        String filePath;

        cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            filePath = contentUri.getPath();
        } else {
            if (type == FILE_TYPE_IMAGE) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                filePath = cursor.getString(idx);
            } else if (type == FILE_TYPE_VIDEO) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
                filePath = cursor.getString(idx);
            } else {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                filePath = cursor.getString(idx);
            }
        }

        cursor.close();

        return filePath;
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority());
    }

    public static boolean isDownloadsSkyDrive(Uri uri) {
        return "com.microsoft.skydrive.content.StorageAccessProvider".equals(uri.getAuthority());
    }

    public static boolean isDownloadsInternalStorage(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsStorage(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private void copyFileStream(File dest, Uri uri, Context context)
            throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            is.close();
            os.close();
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf|image/*|video/*");

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a file to upload"), REQUEST_FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a file manager.", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFileToServer(final File pdfVideoFile, final String fileName, final int fileType) {

        Retrofit retrofit = Constants.getRetrofitInstanc();

        RequestBody file = RequestBody.create(MediaType.parse("multipart/form-data"), pdfVideoFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", "media" + pdfVideoFile.getName(), file);
        HttpInterface.UploadFileInterface uploadFileInterface = retrofit.create(HttpInterface.UploadFileInterface.class);
        Call<UploadResponseData> call = uploadFileInterface.upload(body);
        final ProgressDialog progress = ProgressDialog.show(this, null, "Please wait...", true);
        call.enqueue(new Callback<UploadResponseData>() {
            @Override
            public void onResponse(Call<UploadResponseData> call, Response<UploadResponseData> response) {
                progress.dismiss();
                UploadResponseData responseData = response.body();

                if (responseData == null) {
                    new AlertDialog.Builder(CreateMessageActivity.this)
                            .setTitle(R.string.error_title)
                            .setMessage(R.string.service_error_msg)
                            .setNegativeButton(R.string.ok, null)
                            .show();
                } else {
                    Log.d(TAG, "success = " + responseData.success);
                    if (responseData.success == true) {

                        final AttachFileData attachData = new AttachFileData();
                        attachData.fileName = fileName;
                        attachData.fileType = fileType;

                        addAttachView(attachData);

                        if (attachCnt == 0) {
                            attachedURL = attachedURL + responseData.data.url;
                        } else {
                            attachedURL = attachedURL + "hufeixiaomi" + responseData.data.url;
                        }
                        attachCnt++;
                        Log.d(TAG, "url = " + attachedURL);

                    } else {
                        String message = responseData.error.err_msg;
                        checkErrorMessage(message);
                    }
                }
            }

            @Override
            public void onFailure(Call<UploadResponseData> call, Throwable t) {
                progress.dismiss();

                Log.d(TAG, t.toString());

                new AlertDialog.Builder(CreateMessageActivity.this)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.network_error_msg)
                        .setNegativeButton(R.string.ok, null)
                        .show();
            }
        });
    }

    private void addAttachView (AttachFileData attachData) {
        final RelativeLayout subView = (RelativeLayout) LayoutInflater.from(CreateMessageActivity.this).inflate(R.layout.list_attach, null);

        ImageView imgFileIcon = (ImageView) subView.findViewById(R.id.imgFileIcon);
        switch (attachData.fileType) {
            case FILE_TYPE_PDF:
                imgFileIcon.setImageResource(R.mipmap.icon_pdf);
                break;
            case FILE_TYPE_IMAGE:
                imgFileIcon.setImageResource(R.mipmap.icon_photo);
                break;
            case FILE_TYPE_VIDEO:
                imgFileIcon.setImageResource(R.mipmap.icon_video);
                break;
            default:
                imgFileIcon.setImageResource(R.mipmap.icon_pdf);
                break;
        }

        TextView txtFileName = (TextView) subView.findViewById(R.id.txtFileName);
        txtFileName.setText(attachData.fileName);

        ImageView imgDelete = (ImageView) subView.findViewById(R.id.imgDeleteIcon);
        imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attachLayout.removeView(subView);
            }
        });

        attachLayout.addView(subView, 0);
    }

    private void sendMessage() {
        if (false == Validation.EmptyValidation(edtTitle, getResources().getString(R.string.input_msg_title)))
            return;

        if (false == Validation.EmptyValidation(txtProvider, getResources().getString(R.string.input_msg_receiver)))
            return;

        if (false == Validation.EmptyValidation(edtMessage, getResources().getString(R.string.input_msg_body)))
            return;

        int sender_id = Integer.valueOf(this.prefs.getUserId());
        int receiver_id = Integer.valueOf(txtProviderId.getText().toString());
        String message = edtMessage.getText().toString();
        String title = edtTitle.getText().toString();

        Retrofit retrofit = Constants.getRetrofitInstanc();

        HttpInterface.CreateMessageInterface sendInterface = retrofit.create(HttpInterface.CreateMessageInterface.class);
        Call<SendResponseData> call = sendInterface.createMessage(sender_id, receiver_id, title, attachedURL, attachedURL, "", "", message);

        final ProgressDialog progress = ProgressDialog.show(this, null, "Please wait...", true);
        call.enqueue(new Callback<SendResponseData>() {
            @Override
            public void onResponse(Call<SendResponseData> call, Response<SendResponseData> response) {
                progress.dismiss();
                SendResponseData responseData = response.body();

                if (responseData == null) {
                    new AlertDialog.Builder(CreateMessageActivity.this)
                            .setTitle(R.string.error_title)
                            .setMessage(R.string.service_error_msg)
                            .setNegativeButton(R.string.ok, null)
                            .show();
                } else {
                    Log.d(TAG, "success = " + responseData.success);
                    if (responseData.success == true) {

                        new AlertDialog.Builder(CreateMessageActivity.this)
                                .setTitle(R.string.success_title)
                                .setMessage("You sent successfully message to doctor")
                                .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(CreateMessageActivity.this, MainActivity.class);
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

                new AlertDialog.Builder(CreateMessageActivity.this)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.network_error_msg)
                        .setNegativeButton(R.string.ok, null)
                        .show();
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
            if (items != null && items != null && items.size() > 0)
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
                Picasso.with(CreateMessageActivity.this)
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
}
