package com.example.quickbook.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.quickbook.R;
import com.example.quickbook.receiver.NotificationActionReceiver;
import com.example.quickbook.receiver.TaskAlarmReceiver;
import com.example.quickbook.ui.MainActivity;

public class NotificationHelper extends ContextWrapper {

    public static final String CHANNEL_ID = "QuickBookChannel";
    public static final String CHANNEL_NAME = "Quick Book Notifications";
    private NotificationManager mManager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            getManager().createNotificationChannel(channel);
        }
    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public void sendNotification(String title, String body, int taskId) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (taskId > 0) {
            // "Done" action
            Intent doneIntent = new Intent(this, NotificationActionReceiver.class);
            doneIntent.setAction(NotificationActionReceiver.ACTION_MARK_AS_DONE);
            doneIntent.putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, taskId);
            PendingIntent donePendingIntent = PendingIntent.getBroadcast(this, taskId * 10 + 1, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            builder.addAction(R.drawable.ic_done, "Done", donePendingIntent);

            // "Snooze" action
            Intent snoozeIntent = new Intent(this, NotificationActionReceiver.class);
            snoozeIntent.setAction(NotificationActionReceiver.ACTION_SNOOZE);
            snoozeIntent.putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, taskId);
            snoozeIntent.putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, title);
            PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(this, taskId * 10 + 2, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            builder.addAction(R.drawable.ic_snooze, "Snooze 15 min", snoozePendingIntent);
        }

        getManager().notify(taskId, builder.build());
    }
}
