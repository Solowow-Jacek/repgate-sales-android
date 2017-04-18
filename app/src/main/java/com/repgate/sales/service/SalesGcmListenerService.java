package com.repgate.sales.service;

/**
 * Created by developer on 4/12/16.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.repgate.sales.R;
import com.repgate.sales.common.AppPreferences;
import com.repgate.sales.common.Constants;
import com.repgate.sales.ui.activities.MainActivity;

public class SalesGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    public final static String ACTION_MESSAGE_NOTIFICATION = "com.repgate.sales.ACTION_MESSAGE_NOTIFICATION";
    public final static String ACTION_REQUEST_NOTIFICATION = "com.repgate.sales.ACTION_REQUEST_NOTIFICATION";
    public AppPreferences prefs;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        prefs = new AppPreferences(this);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(message);

        // handle push notificaiton
        String senderId = data.getString("senderId");
        String receiverId = data.getString("receiverId");

        int type = Integer.valueOf(data.getString("notification_type"));
        switch (type) {
            case Constants.NOTIFICATION_TYPE_CANCEL_REQUEST:
            case Constants.NOTIFICATION_TYPE_CHANGE_REQUEST:
            case Constants.NOTIFICATION_TYPE_CONFIRM_REQUEST:
            case Constants.NOTIFICATION_TYPE_CREATE_REQUEST:
//                Intent requestIntent = new Intent(this, RepTabActivity.class);
//                requestIntent.putExtra(RepTabActivity.PARAM_RECEIVER_ID, senderId);
//                requestIntent.putExtra(RepTabActivity.PARAM_FOCUS_TAB, 1);
//                requestIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(requestIntent);

                int requestNew = Integer.parseInt(prefs.getUserRequestNew()) + 1;
                prefs.setUserRequestNew(String.valueOf(requestNew));

                Intent intentReq = new Intent(ACTION_REQUEST_NOTIFICATION);
                sendBroadcast(intentReq);
                break;
            case Constants.NOTIFICATION_TYPE_CREATE_MESSAGE:
//                Intent messageIntent = new Intent(this, RepTabActivity.class);
//                messageIntent.putExtra(RepTabActivity.PARAM_RECEIVER_ID, senderId);
//                messageIntent.putExtra(RepTabActivity.PARAM_FOCUS_TAB, 0);
//                messageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(messageIntent);

                int messageNew = Integer.parseInt(prefs.getUserMessageNew()) + 1;
                prefs.setUserMessageNew(String.valueOf(messageNew));

                Intent intentMsg = new Intent(ACTION_MESSAGE_NOTIFICATION);
                sendBroadcast(intentMsg);
                break;
            case Constants.NOTIFICATION_TYPE_JOIN_USER:
                Intent mainIntent = new Intent(this, MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
                break;
        }

        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}

