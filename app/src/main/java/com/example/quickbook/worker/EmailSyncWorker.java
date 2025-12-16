package com.example.quickbook.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.quickbook.R;
import com.example.quickbook.data.AppDatabase;
import com.example.quickbook.data.Task;
import com.example.quickbook.data.TaskDao;
import com.example.quickbook.security.CredentialsManager;
import com.example.quickbook.util.NotificationHelper;

import java.util.ArrayList;
import java.util.Properties;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.search.OrTerm;
import jakarta.mail.search.SearchTerm;
import jakarta.mail.search.SubjectTerm;

public class EmailSyncWorker extends Worker {

    private static final String TAG = "EmailSyncWorker";
    private static final int ERROR_NOTIFICATION_ID = -100;

    public EmailSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting email sync work.");

        CredentialsManager credentialsManager = new CredentialsManager(getApplicationContext());
        if (!credentialsManager.isLoggedIn()) {
            Log.w(TAG, "User is not logged in. Skipping email sync.");
            return Result.failure();
        }

        String keywords = credentialsManager.getKeywords();
        if (keywords.isEmpty()) {
            Log.d(TAG, "No keywords configured. Skipping email sync.");
            return Result.success();
        }

        TaskDao taskDao = AppDatabase.getDatabase(getApplicationContext()).taskDao();
        NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());

        final String host = "imap.gmail.com";
        final String username = credentialsManager.getEmail();
        final String password = credentialsManager.getPassword();

        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");

            Session session = Session.getInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect(host, username, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            String[] keywordArray = keywords.split(",");
            ArrayList<SearchTerm> searchTerms = new ArrayList<>();
            for (String keyword : keywordArray) {
                if (!keyword.trim().isEmpty()) {
                    searchTerms.add(new SubjectTerm(keyword.trim()));
                }
            }

            if (searchTerms.isEmpty()) {
                return Result.success();
            }

            OrTerm orTerm = new OrTerm(searchTerms.toArray(new SearchTerm[0]));
            Message[] messages = inbox.search(orTerm);

            Log.d(TAG, "Found " + messages.length + " emails to process.");

            for (Message message : messages) {
                String taskTitle = message.getSubject();
                Task newTask = new Task();
                newTask.setTitle(taskTitle);

                long newTaskId = taskDao.insert(newTask);

                String notificationTitle = getApplicationContext().getString(R.string.new_task_from_email_title);
                notificationHelper.sendNotification(notificationTitle, taskTitle, (int) newTaskId);
            }

            inbox.close(false);
            store.close();

            Log.d(TAG, "Email sync work finished successfully.");
            return Result.success();

        } catch (AuthenticationFailedException e) {
            Log.e(TAG, "Email sync failed: Authentication Failed.", e);
            String errorTitle = getApplicationContext().getString(R.string.email_sync_failed_title);
            String errorBody = getApplicationContext().getString(R.string.email_sync_auth_failed_body);
            notificationHelper.sendNotification(errorTitle, errorBody, ERROR_NOTIFICATION_ID);
            return Result.failure();
        } catch (Exception e) {
            Log.e(TAG, "An unexpected error occurred during email sync", e);
            String errorTitle = getApplicationContext().getString(R.string.email_sync_failed_title);
            String errorBody = getApplicationContext().getString(R.string.email_sync_error_body);
            notificationHelper.sendNotification(errorTitle, errorBody, ERROR_NOTIFICATION_ID);
            return Result.failure();
        }
    }
}
