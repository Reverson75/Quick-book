package com.example.quickbook.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.quickbook.R;
import com.example.quickbook.util.NotificationHelper;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceReceiver";
    public static final String EXTRA_TASK_ID = "com.example.quickbook.EXTRA_TASK_ID";
    public static final String EXTRA_TASK_TITLE = "com.example.quickbook.EXTRA_TASK_TITLE";

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "GeofencingEvent has error: " + geofencingEvent.getErrorCode());
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            for (Geofence geofence : triggeringGeofences) {
                int taskId = Integer.parseInt(geofence.getRequestId());
                String taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE);

                if (taskTitle != null) {
                    NotificationHelper notificationHelper = new NotificationHelper(context);
                    String notificationTitle = context.getString(R.string.geofence_notification_title);
                    String notificationBody = context.getString(R.string.geofence_notification_body, taskTitle);
                    notificationHelper.sendNotification(notificationTitle, notificationBody, taskId);
                }
            }
        }
    }
}
