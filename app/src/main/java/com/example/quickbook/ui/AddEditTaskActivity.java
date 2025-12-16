package com.example.quickbook.ui;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quickbook.R;
import com.example.quickbook.data.Task;
import com.example.quickbook.receiver.TaskAlarmReceiver;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditTaskActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "com.example.android.quickbook.ID";
    public static final String EXTRA_REPLY_TITLE = "com.example.android.quickbook.REPLY_TITLE";
    public static final String EXTRA_REPLY_DUEDATE = "com.example.android.quickbook.REPLY_DUEDATE";
    public static final String EXTRA_REPLY_COMPLEXITY = "com.example.android.quickbook.REPLY_COMPLEXITY";
    public static final String EXTRA_REPLY_LOCATION = "com.example.android.quickbook.REPLY_LOCATION";
    public static final String EXTRA_REPLY_REPETITION = "com.example.android.quickbook.REPLY_REPETITION";

    private TextInputEditText mEditTaskView;
    private TextInputEditText mEditDueDateView;
    private TextInputEditText mEditDueTimeView;
    private TextInputEditText mEditLocationView;
    private RatingBar mRatingBarComplexity;
    private AutoCompleteTextView mRepetitionView;
    private int currentTaskId = -1;
    private int currentRepetitionRule = Task.REPEAT_NONE;

    private Calendar dueDateTime = Calendar.getInstance();

    private final ActivityResultLauncher<Intent> requestExactAlarmPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    if (alarmManager.canScheduleExactAlarms()) {
                        Toast.makeText(this, "Permission granted. You can now save your task with a reminder.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Reminders will not work without this permission.", Toast.LENGTH_LONG).show();
                    }
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        mEditTaskView = findViewById(R.id.editTextTaskTitle);
        mEditDueDateView = findViewById(R.id.editTextDueDate);
        mEditDueTimeView = findViewById(R.id.editTextDueTime);
        mEditLocationView = findViewById(R.id.editTextLocation);
        mRatingBarComplexity = findViewById(R.id.ratingBarComplexity);
        mRepetitionView = findViewById(R.id.autoCompleteRepetition);

        mEditDueDateView.setOnClickListener(v -> showDatePickerDialog());
        mEditDueTimeView.setOnClickListener(v -> showTimePickerDialog());

        setupRepetitionSpinner();
        setupSmartInput();

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ID)) {
            loadExistingTaskData(intent);
        } else {
            setTitle(R.string.title_add_task);
            mRepetitionView.setText(mRepetitionView.getAdapter().getItem(Task.REPEAT_NONE).toString(), false);
        }

        final Button button = findViewById(R.id.button_save);
        button.setOnClickListener(view -> saveTask());
    }

    private void setupRepetitionSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.repetition_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRepetitionView.setAdapter(adapter);
        mRepetitionView.setOnItemClickListener((parent, view, position, id) -> {
            currentRepetitionRule = position;
        });
    }

    private void setupSmartInput() {
        mEditTaskView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                parseSmartInput(s.toString());
            }
        });
    }

    private void loadExistingTaskData(Intent intent) {
        setTitle(R.string.title_edit_task);
        currentTaskId = intent.getIntExtra(EXTRA_ID, -1);

        long dueDateMillis = intent.getLongExtra(EXTRA_REPLY_DUEDATE, 0);
        if (dueDateMillis != 0) {
            dueDateTime.setTimeInMillis(dueDateMillis);
            updateDateText();
            updateTimeText();
        }

        mEditTaskView.setText(intent.getStringExtra(EXTRA_REPLY_TITLE));
        mRatingBarComplexity.setRating(intent.getIntExtra(EXTRA_REPLY_COMPLEXITY, 1));
        mEditLocationView.setText(intent.getStringExtra(EXTRA_REPLY_LOCATION));
        int repetition = intent.getIntExtra(EXTRA_REPLY_REPETITION, Task.REPEAT_NONE);
        currentRepetitionRule = repetition;
        mRepetitionView.setText(mRepetitionView.getAdapter().getItem(repetition).toString(), false);
    }

    private void saveTask() {
        if (TextUtils.isEmpty(mEditTaskView.getText())) {
            setResult(RESULT_CANCELED, new Intent());
            finish();
            return;
        }

        long dueDateMillis = 0;
        if (mEditDueDateView.getText().length() > 0) {
            dueDateMillis = dueDateTime.getTimeInMillis();
        }

        if (dueDateMillis > System.currentTimeMillis() && !canScheduleExactAlarms()) {
            showExactAlarmRationaleDialog();
            return; 
        }
        
        Intent replyIntent = new Intent();
        String title = mEditTaskView.getText().toString();
        String location = mEditLocationView.getText().toString();
        int complexity = (int) mRatingBarComplexity.getRating();
        int taskId = (currentTaskId != -1) ? currentTaskId : (int) System.currentTimeMillis();

        replyIntent.putExtra(EXTRA_REPLY_TITLE, title);
        replyIntent.putExtra(EXTRA_REPLY_LOCATION, location);
        replyIntent.putExtra(EXTRA_REPLY_COMPLEXITY, complexity);
        replyIntent.putExtra(EXTRA_REPLY_DUEDATE, dueDateMillis);
        replyIntent.putExtra(EXTRA_REPLY_REPETITION, currentRepetitionRule);
        if (currentTaskId != -1) {
            replyIntent.putExtra(EXTRA_ID, taskId);
        }

        cancelAlarm(taskId);
        if (dueDateMillis > System.currentTimeMillis()) {
            setAlarm(taskId, title, dueDateMillis, currentRepetitionRule);
        }

        setResult(RESULT_OK, replyIntent);
        finish();
    }

    private boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true;
    }

    private void showExactAlarmRationaleDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Special Permission Needed")
            .setMessage("To ensure your task reminders are precise, this app needs special permission to schedule alarms. Please grant it on the next screen.")
            .setPositiveButton("Grant", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.fromParts("package", getPackageName(), null));
                requestExactAlarmPermissionLauncher.launch(intent);
            })
            .setNegativeButton("Later", null)
            .show();
    }

    private void parseSmartInput(String text) {
        String lowerCaseText = text.toLowerCase(Locale.ROOT);

        if (lowerCaseText.contains("amanhã")) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            dueDateTime.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
            dueDateTime.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
            dueDateTime.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
            updateDateText();
        }

        String[] words = lowerCaseText.split(" ");
        for (int i = 0; i < words.length; i++) {
            if ((words[i].equals("às") || words[i].equals("at")) && i + 1 < words.length) {
                String timeWord = words[i + 1].replace("h", "").replace("pm", "").replace("am", "");
                try {
                    int hour;
                    int minute = 0;

                    if (timeWord.contains(":")) {
                        String[] parts = timeWord.split(":");
                        hour = Integer.parseInt(parts[0]);
                        if (parts.length > 1) {
                            minute = Integer.parseInt(parts[1]);
                        }
                    } else {
                        hour = Integer.parseInt(timeWord);
                    }
                    
                    if (lowerCaseText.contains("pm") && hour < 12) {
                        hour += 12;
                    }

                    dueDateTime.set(Calendar.HOUR_OF_DAY, hour);
                    dueDateTime.set(Calendar.MINUTE, minute);
                    updateTimeText();
                    break; 
                } catch (Exception e) {
                    // Ignore parsing error
                }
            }
        }
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    dueDateTime.set(Calendar.YEAR, year);
                    dueDateTime.set(Calendar.MONTH, month);
                    dueDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateText();
                }, dueDateTime.get(Calendar.YEAR), dueDateTime.get(Calendar.MONTH), dueDateTime.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            dueDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            dueDateTime.set(Calendar.MINUTE, minute);
            updateTimeText();
        }, dueDateTime.get(Calendar.HOUR_OF_DAY), dueDateTime.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    private void updateDateText() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        mEditDueDateView.setText(sdf.format(dueDateTime.getTime()));
    }

    private void updateTimeText() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        mEditDueTimeView.setText(sdf.format(dueDateTime.getTime()));
    }

    private void setAlarm(int taskId, String taskTitle, long alarmTimeMillis, int repetitionRule) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TaskAlarmReceiver.class);
        intent.putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, taskId);
        intent.putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, taskTitle);
        intent.putExtra(TaskAlarmReceiver.EXTRA_TASK_REPETITION, repetitionRule);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            try {
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if(alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent);
                    }
                 } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent);
                 }
            } catch (SecurityException e) {
                // Handle case where permission is denied
            }
        }
    }

    private void cancelAlarm(int taskId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TaskAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, taskId, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
