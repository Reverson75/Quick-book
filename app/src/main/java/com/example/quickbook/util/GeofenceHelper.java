package com.example.quickbook.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;

import com.example.quickbook.receiver.GeofenceBroadcastReceiver;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

public class GeofenceHelper extends ContextWrapper {

    public GeofenceHelper(Context base) {
        super(base);
    }

    public GeofencingRequest getGeofencingRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }

    public Geofence getGeofence(String id, double lat, double lon, float radius, int transitionTypes) {
        return new Geofence.Builder()
                .setCircularRegion(lat, lon, radius)
                .setRequestId(id)
                .setTransitionTypes(transitionTypes)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    public PendingIntent getPendingIntent(int taskId, String taskTitle) {
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        intent.putExtra(GeofenceBroadcastReceiver.EXTRA_TASK_ID, taskId);
        intent.putExtra(GeofenceBroadcastReceiver.EXTRA_TASK_TITLE, taskTitle);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        return PendingIntent.getBroadcast(this, taskId, intent, flags);
    }
}
