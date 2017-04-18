package com.repgate.sales.ui.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.repgate.sales.R;
import com.repgate.sales.common.Constants;
import com.repgate.sales.service.RegistrationIntentService;

/**
 * Created by developer on 3/17/16.
 */
public class SplashActivity extends Activity{
    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 1500;

    private static final String TAG = "SplashActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private BroadcastReceiver mTokenBroadcastReceiver;
    private String mDeviceToken;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash);

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
//        new Handler().postDelayed(new Runnable(){
//            @Override
//            public void run() {
//                /* Create an Intent that will start the Menu-Activity. */
//                Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
//                SplashActivity.this.startActivity(mainIntent);
//                SplashActivity.this.finish();
//            }
//        }, SPLASH_DISPLAY_LENGTH);

        mDeviceToken = "";
        mTokenBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mDeviceToken = intent.getStringExtra(Constants.INTENT_PARAM_TOKEN);

                Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                mainIntent.putExtra(Constants.INTENT_PARAM_TOKEN, mDeviceToken);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        };

        registerReceiver();

        if (checkPlayServices()) {
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mTokenBroadcastReceiver);
        super.onDestroy();
    }

    private void registerReceiver() {
        registerReceiver(mTokenBroadcastReceiver, new IntentFilter(Constants.SEND_TOKEN_TO_UI));
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

}
