package com.example.quickbook.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.quickbook.R;
import com.example.quickbook.data.Task;
import com.example.quickbook.util.NotificationHelper;

import java.util.Calendar;

public class TaskAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "TaskAlarmReceiver";
    public static final String EXTRA_TASK_ID = "com.example.quickbook.EXTRA_TASK_ID";
    public static final String EXTRA_TASK_TITLE = "com.example.quickbook.EXTRA_TASK_TITLE";
    public static final String EXTRA_TASK_REPETITION = "com.example.quickbook.EXTRA_TASK_REPETITION";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);

        int taskId = intent.getIntExtra(EXTRA_TASK_ID, -1);
        String taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE);
        int repetition = intent.getIntExtra(EXTRA_TASK_REPETITION, Task.REPEAT_NONE);

        if (taskId != -1 && taskTitle != null) {
            Log.d(TAG, "Alarm received for task ID: " + taskId);

            // Use translated strings
            String notificationTitle = context.getString(R.string.task_reminder_notification_title);
            String notificationBody = context.getString(R.string.task_reminder_notification_body, taskTitle);

            notificationHelper.sendNotification(
                notificationTitle,
                notificationBody,
                taskId
            );

            // If it's a recurring task, reschedule the next alarm
            if (repetition != Task.REPEAT_NONE) {
                rescheduleAlarm(context, taskId, taskTitle, repetition);
            }
        }
    }

    private void rescheduleAlarm(Context context, int taskId, String taskTitle, int repetition) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TaskAlarmReceiver.class);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        intent.putExtra(EXTRA_TASK_TITLE, taskTitle);
        intent.putExtra(EXTRA_TASK_REPETITION, repetition);

        Calendar calendar = Calendar.getInstance();
        switch (repetition) {
            case Task.REPEAT_DAILY:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case Task.REPEAT_WEEKLY:
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case Task.REPEAT_MONTHLY:
                calendar.add(Calendar.MONTH, 1);
                break;
        }

        long nextAlarmTime = calendar.getTimeInMillis();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            try {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextAlarmTime, pendingIntent);
                Log.d(TAG, "Rescheduled task ID " + taskId + " for " + new java.util.Date(nextAlarmTime));
            } catch (SecurityException e) {
                Log.e(TAG, "Could not reschedule alarm due to security exception. Missing SCHEDULE_EXACT_ALARM permission?", e);
            }
        }
    }
}
