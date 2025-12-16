package com.example.quickbook.receiver;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.quickbook.data.AppDatabase;
import com.example.quickbook.data.Task;
import com.example.quickbook.data.TaskDao;

import java.util.Calendar;

public class NotificationActionReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationAction";

    public static final String ACTION_MARK_AS_DONE = "com.example.quickbook.ACTION_MARK_AS_DONE";
    public static final String ACTION_SNOOZE = "com.example.quickbook.ACTION_SNOOZE";
    private static final int SNOOZE_MINUTES = 15;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }

        int taskId = intent.getIntExtra(TaskAlarmReceiver.EXTRA_TASK_ID, -1);
        if (taskId == -1) {
            return;
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(taskId);

        Log.d(TAG, "Action received: " + action + " for task ID: " + taskId);

        switch (action) {
            case ACTION_MARK_AS_DONE:
                markTaskAsDone(context, taskId);
                break;
            case ACTION_SNOOZE:
                String taskTitle = intent.getStringExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE);
                int repetition = intent.getIntExtra(TaskAlarmReceiver.EXTRA_TASK_REPETITION, Task.REPEAT_NONE);
                snoozeAlarm(context, taskId, taskTitle, repetition);
                break;
        }
    }

    private void markTaskAsDone(Context context, int taskId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            TaskDao taskDao = AppDatabase.getDatabase(context).taskDao();
            Task task = new Task();
            task.setId(taskId);
            taskDao.delete(task);
            Log.d(TAG, "Task " + taskId + " marked as done and deleted.");
        });
    }

    private void snoozeAlarm(Context context, int taskId, String taskTitle, int repetition) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TaskAlarmReceiver.class);
        intent.putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, taskId);
        intent.putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, taskTitle);
        intent.putExtra(TaskAlarmReceiver.EXTRA_TASK_REPETITION, repetition);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, SNOOZE_MINUTES);
        long snoozeTimeMillis = calendar.getTimeInMillis();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeTimeMillis, pendingIntent);
            Log.d(TAG, "Task " + taskId + " snoozed for " + SNOOZE_MINUTES + " minutes.");
        }
    }
}
