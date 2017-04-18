package com.repgate.sales.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;
import com.repgate.sales.R;
import com.repgate.sales.common.AppPreferences;
import com.repgate.sales.common.Constants;
import com.repgate.sales.service.SalesGcmListenerService;

import info.hoang8f.widget.FButton;

public class TutorialsActivity extends Activity implements View.OnClickListener{
    private final static String TAG = "TutorialsActivity";
    public AppPreferences prefs;

    private FButton btnMessaging, btnRequesting;
    private Button btnBack;
    private ImageView imgLogo;
    private TextView txtTitle;
    private BoomMenuButton bmb;

    public static final String API_KEY = "AIzaSyDGWzpWigZintQTSEIe7Jizka7JQXG5XEk";

    //https://www.youtube.com/watch?v=<VIDEO_ID>
    public static final String VIDEO_ID = "waPgV9SSQt4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorials);

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
                        Intent intentMessage = new Intent(TutorialsActivity.this, CreateMessageActivity.class);
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
                        Intent intentRequest = new Intent(TutorialsActivity.this, CreateRequestActivity.class);
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
                        Intent intent = new Intent(TutorialsActivity.this, CalendarActivity.class);
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
                        new AlertDialog.Builder(TutorialsActivity.this)
                                .setTitle(R.string.confirm_title)
                                .setMessage(R.string.are_you_logout)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        prefs.clearUserInformation();

                                        Intent intent = new Intent(TutorialsActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        TutorialsActivity.this.finish();
                                    }
                                })
                                .setNegativeButton(R.string.no, null)
                                .show();
                    }
                });
        bmb.addBuilder(logoutBuilder);

        btnMessaging = (FButton) findViewById(R.id.btnMessaging);
        btnMessaging.setOnClickListener(this);
        btnRequesting = (FButton) findViewById(R.id.btnRequesting);
        btnRequesting.setOnClickListener(this);

        btnBack = (Button) findViewById(R.id.action_back);
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        imgLogo = (ImageView) findViewById(R.id.imgTitle);
        imgLogo.setVisibility(View.GONE);
        imgLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TutorialsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText("Tutorials");

        //Initializing and adding YouTubePlayerFragment
        FragmentManager fm = getFragmentManager();
        String tag = YouTubePlayerFragment.class.getSimpleName();
        YouTubePlayerFragment playerFragment = (YouTubePlayerFragment) fm.findFragmentByTag(tag);
        if (playerFragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            playerFragment = YouTubePlayerFragment.newInstance();
            ft.add(R.id.youtubeContent, playerFragment, tag);
            ft.commit();
        }

        playerFragment.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.cueVideo(VIDEO_ID);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Toast.makeText(TutorialsActivity.this, "Error while initializing YouTubePlayer.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerReceiver() {
        registerReceiver(mMessageReceiver, new IntentFilter(SalesGcmListenerService.ACTION_MESSAGE_NOTIFICATION));
    }

    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intentMessageShares = new Intent(TutorialsActivity.this, MyMessagesActivity.class);
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
            Intent intentRequests = new Intent(TutorialsActivity.this, MyRequestActivity.class);
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

    @Override
    public void onClick(View v) {
        String youtubeUrl = "";
//        switch (v.getId()) {
//            case R.id.btnMessaging:
//                youtubeUrl = "iS1g8G_njx8";
//                break;
//            case R.id.btnRequesting:
//                youtubeUrl = "AIzaSyDD1Zq76Hwm9jpH0RkA26qB-q4mP36PNuI";
//                break;
//            default:
//                youtubeUrl = "AIzaSyDD1Zq76Hwm9jpH0RkA26qB-q4mP36PNuI";
//                break;
//        }

//        if (youtubeUrl != null && !youtubeUrl.isEmpty()) {
//            Intent intentYoutube = new Intent(TutorialsActivity.this, YouTubePlayerActivity.class);
//
//// Youtube video ID (Required, You can use YouTubeUrlParser to parse Video Id from url)
//            intentYoutube.putExtra(YouTubePlayerActivity.EXTRA_VIDEO_ID, youtubeUrl);
//
//// Youtube player style (DEFAULT as default)
//            intentYoutube.putExtra(YouTubePlayerActivity.EXTRA_PLAYER_STYLE, YouTubePlayer.PlayerStyle.MINIMAL);
//
//// Screen Orientation Setting (AUTO for default)
//// AUTO, AUTO_START_WITH_LANDSCAPE, ONLY_LANDSCAPE, ONLY_PORTRAIT
//            intentYoutube.putExtra(YouTubePlayerActivity.EXTRA_ORIENTATION, Orientation.AUTO);
//
//// Show audio interface when user adjust volume (true for default)
//            intentYoutube.putExtra(YouTubePlayerActivity.EXTRA_SHOW_AUDIO_UI, true);
//
//// If the video is not playable, use Youtube app or Internet Browser to play it
//// (true for default)
//            intentYoutube.putExtra(YouTubePlayerActivity.EXTRA_HANDLE_ERROR, true);
//
//// Animation when closing youtubeplayeractivity (none for default)
////                intentYoutube.putExtra(YouTubePlayerActivity.EXTRA_ANIM_ENTER, R.anim.fade_in);
////                intentYoutube.putExtra(YouTubePlayerActivity.EXTRA_ANIM_EXIT, R.anim.fade_out);
//
//            intentYoutube.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intentYoutube);
//        }
        Intent intentTutorials = new Intent(TutorialsActivity.this, ShowTutorialActivity.class);

        switch (v.getId()) {
            case R.id.btnMessaging:
                intentTutorials.putExtra(Constants.PARAM_TUTORIALS_TYPE, Constants.TUTORIAL_MESSAGE);
                break;
            case R.id.btnRequesting:
                intentTutorials.putExtra(Constants.PARAM_TUTORIALS_TYPE, Constants.TUTORIAL_REQUEST);
                break;
            default:
                break;
        }

        startActivity(intentTutorials);
    }
}
