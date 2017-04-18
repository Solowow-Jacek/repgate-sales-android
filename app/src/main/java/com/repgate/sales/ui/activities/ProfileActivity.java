package com.repgate.sales.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.repgate.sales.R;
import com.repgate.sales.base.BaseTask;
import com.repgate.sales.common.AppPreferences;
import com.repgate.sales.common.Constants;
import com.repgate.sales.data.DoctorProfileResponseData;
import com.repgate.sales.data.UploadResponseData;
import com.repgate.sales.http.HttpInterface;
import com.repgate.sales.service.SalesGcmListenerService;
import com.repgate.sales.view.ImagePickerView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import info.hoang8f.widget.FButton;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ProfileActivity extends Activity implements View.OnTouchListener{
    private static final String TAG = "ProfileActivity";
    public AppPreferences prefs;

    private ImageView imgPicture, imgLogo;
    private EditText edtName, edtEmail, edtPhone, edtAddress, edtCompany;
    private TextView txtCode, txtRole, txtTitle;
    private FButton btnSave;
    private Button btnBack;

    private String mPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefs = new AppPreferences(this);

        imgLogo = (ImageView) findViewById(R.id.imgTitle);
        imgLogo.setVisibility(View.GONE);

        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText("My Profile");

        BoomMenuButton bmb = (BoomMenuButton) findViewById(R.id.bmb);
        bmb.setVisibility(View.GONE);

        btnBack = (Button) findViewById(R.id.action_back);
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        edtName = (EditText) findViewById(R.id.edtName);
        edtName.setOnTouchListener(this);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtEmail.setOnTouchListener(this);
        edtPhone = (EditText) findViewById(R.id.edtPhone);
        edtPhone.setOnTouchListener(this);
        edtAddress = (EditText) findViewById(R.id.edtAddr);
        edtAddress.setOnTouchListener(this);
        edtCompany = (EditText) findViewById(R.id.edtCompany);
        edtCompany.setOnTouchListener(this);

        txtCode = (TextView) findViewById(R.id.txtCode);
        txtRole = (TextView) findViewById(R.id.txtRoleName);

        btnSave = (FButton) findViewById(R.id.btnSave);
        btnSave.setEnabled(false);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfile();
            }
        });

        imgPicture = (ImageView) findViewById(R.id.imgPicture);
        imgPicture.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                chooseImageAvatar();

                return false;
            }
        });

        createPictureFolder();

        loadUserInformation();
    }

    private void registerReceiver() {
        registerReceiver(mMessageReceiver, new IntentFilter(SalesGcmListenerService.ACTION_MESSAGE_NOTIFICATION));
    }

    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intentMessageShares = new Intent(ProfileActivity.this, MyMessagesActivity.class);
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
            Intent intentRequests = new Intent(ProfileActivity.this, MyRequestActivity.class);
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

    public void onRequestPermissionsResult(int requestCode, String permission[], int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_PERMISSION_STORAGE_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Write External Storage Permission granted");
                    createPictureFolder();

                } else {
                    finish();
                }
            case Constants.REQUEST_PERMISSION_CAMERA_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Camera Permission granted");
                    createPictureFolder();
                } else {
                    finish();
                }
                break;
        }
    }

    public void createPictureFolder() {
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        String folderName = "Pictures";
        File folder = new File(extStorageDirectory, "/Android/data/" + this.getPackageName() + File.separator + folderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public void chooseImageAvatar() {
        Intent chooseImageIntent = ImagePickerView.getPickImageIntent(this);
        startActivityForResult(chooseImageIntent, Constants.REQUEST_CHOOSE_AVATAR_CODE);
    }

    public void updateAvatarNotification(String filePath) {

        final File imgFile = new File(filePath);

        BaseTask sceneTask = new BaseTask(Constants.TASK_PHOTO_LOAD);
        sceneTask.run(new BaseTask.TaskListener() {
            @Override
            public void onTaskPrepare(int taskId, Object data) {

            }

            @Override
            public Object onTaskRunning(int taskId, Object data) {

                File file = (File) data;

                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = false;
                opts.inPreferredConfig = Bitmap.Config.RGB_565;
                opts.inDither = true;
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);

                int imgWidth = imgPicture.getWidth();
                int imgHeight = imgPicture.getHeight();
                if (imgWidth == 0 || imgHeight == 0)
                    return null;

                bitmap = getResizedBitmap(bitmap, imgWidth, imgHeight);
                return bitmap;
            }

            @Override
            public void onTaskProgress(int taskId, Object... values) {

            }

            @Override
            public void onTaskResult(int taskId, Object result) {

                if (result == null)
                    return;

                Bitmap bitmap = (Bitmap) result;
                imgPicture.setImageBitmap(bitmap);

                uploadAvatarImageToServer(imgFile);
            }

            @Override
            public void onTaskCancelled(int taskId) {

            }
        }, imgFile);
    }

    private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float rate = (height / (float)width);
        float scaleWidth = ((float) newWidth) / width;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleWidth);
        Bitmap scaledBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

        int x = 0, y = 0;
        if (scaledBitmap.getHeight() > newHeight)
            y = (scaledBitmap.getHeight() - newHeight) / 2;
        else
            newHeight = scaledBitmap.getHeight();

        if (scaledBitmap.getWidth() > newWidth)
            x = (scaledBitmap.getWidth() - newWidth) / 2;
        else
            newWidth = scaledBitmap.getWidth();

        Bitmap resizedBitmap = Bitmap.createBitmap(scaledBitmap, x, y, newWidth, newHeight);

        bm.recycle();
        scaledBitmap.recycle();

        return resizedBitmap;
    }

    private void uploadAvatarImageToServer(File imgFile) {

        Retrofit retrofit = Constants.getRetrofitInstanc();

        RequestBody file = RequestBody.create(MediaType.parse("image/png"), imgFile);
        int user_id = Integer.valueOf(prefs.getUserId());

        HttpInterface.UploadAvatarImageInterface uploadInterface = retrofit.create(HttpInterface.UploadAvatarImageInterface.class);
        Call<UploadResponseData> call = uploadInterface.uploadImage(file, user_id);

        final ProgressDialog progress = ProgressDialog.show(this, null, "Please wait...", true);
        call.enqueue(new Callback<UploadResponseData>() {
            @Override
            public void onResponse(Call<UploadResponseData> call, Response<UploadResponseData> response) {
                progress.dismiss();
                UploadResponseData responseData = response.body();

                if (responseData == null) {
                    new AlertDialog.Builder(ProfileActivity.this)
                            .setTitle(R.string.error_title)
                            .setMessage(R.string.service_error_msg)
                            .setNegativeButton(R.string.ok, null)
                            .show();
                } else {
                    Log.d(TAG, "success = " + responseData.success);
                    if (responseData.success == true) {

                        String url = responseData.data.url;
                        Log.d(TAG, "url = " + url);
                        prefs.setUserAvatar(url);

                        ImageLoader.getInstance().displayImage(url, imgPicture);


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

                new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.network_error_msg)
                        .setNegativeButton(R.string.ok, null)
                        .show();
            }
        });
    }

    private void loadUserInformation() {
        String url = prefs.getUserAvatar();
        if (url.isEmpty() == false)
            Picasso.with(this)
                    .load(url)
                    .placeholder(R.mipmap.doctor)
                    .error(R.mipmap.doctor)
                    .fit()
                    .into(imgPicture);
        else
            imgPicture.setImageResource(R.mipmap.doctor);

        String username = prefs.getUserName();
        if (username.isEmpty() == false) {
            edtName.setText(username);
        }

        String email = prefs.getUserEmail();
        edtEmail.setText(email);

        String phone = prefs.getUserPhone();
        edtPhone.setText(phone);

        String address = prefs.getUserAddress();
        edtAddress.setText(address);

        String code = prefs.getUserCode();
        txtCode.setText(code);

        String roleName = Constants.roleArray[Integer.parseInt(prefs.getUserRole())];
        txtRole.setText(", " + roleName);

        String company = prefs.getUserCompany();
        edtCompany.setText(company);
    }

    private boolean saveProfile () {
        int user_id = Integer.valueOf(prefs.getUserId());
        String userName = edtName.getText().toString();
        String phone = edtPhone.getText().toString();
        String address = edtAddress.getText().toString();
        String company = edtCompany.getText().toString();

        Retrofit retrofit = Constants.getRetrofitInstanc();

        HttpInterface.UpdateProfileInterface profileInterface = retrofit.create(HttpInterface.UpdateProfileInterface.class);
        Call<DoctorProfileResponseData> call = profileInterface.updateProfile(user_id,
                userName, phone, address, "", "", "",
                Constants.ALLOW_REQUEST_MESSAGE, Constants.ALLOW_REQUEST_MESSAGE, company);

        final ProgressDialog progress = ProgressDialog.show(this, null, "Please wait...", true);
        call.enqueue(new Callback<DoctorProfileResponseData>() {
            @Override
            public void onResponse(Call<DoctorProfileResponseData> call, Response<DoctorProfileResponseData> response) {
                progress.dismiss();
                DoctorProfileResponseData responseData = response.body();

                if (responseData == null) {
                    new AlertDialog.Builder(ProfileActivity.this)
                            .setTitle(R.string.error_title)
                            .setMessage(R.string.service_error_msg)
                            .setNegativeButton(R.string.ok, null)
                            .show();
                } else {
                    Log.d(TAG, "success = " + responseData.success);
                    if (responseData.success == true) {

                        prefs.saveUserInformation(responseData.data);

                    } else {

                        String message = responseData.error.err_msg;
                        checkErrorMessage(message);

                    }
                }
            }

            @Override
            public void onFailure(Call<DoctorProfileResponseData> call, Throwable t) {
                progress.dismiss();

                new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.network_error_msg)
                        .setNegativeButton(R.string.ok, null)
                        .show();
            }
        });

        return true;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CHOOSE_AVATAR_CODE && resultCode == RESULT_OK) {

            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            String folderName = "Pictures";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd hhmmss");
            sdf.setLenient(false);
            Date today = new Date();
            String fileName = sdf.format(today) + ".png";
            Log.d(TAG, "filename : " + fileName);

            mPhotoPath = extStorageDirectory + "/Android/data/" + getPackageName() + File.separator + folderName + File.separator + fileName;

            Bitmap bitmap = ImagePickerView.getImageFromResult(this, resultCode, data);

            FileOutputStream out = null;
            try {
                File imgFile = new File(mPhotoPath);
                out = new FileOutputStream(imgFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null)
                        out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            updateAvatarNotification(mPhotoPath);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (view.getId()) {
            case R.id.edtName:
            case R.id.edtAddr:
            case R.id.edtCompany:
            case R.id.edtEmail:
            case R.id.edtPhone:
                btnSave.setEnabled(true);
                break;
            default:
                break;
        }

        return false;
    }
}
