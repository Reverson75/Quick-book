package com.example.quickbook.ui.account;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.quickbook.R;
import com.example.quickbook.security.CredentialsManager;
import com.example.quickbook.ui.LoginActivity;
import com.example.quickbook.worker.EmailSyncWorker;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "QuickBookPrefs";
    private static final String KEY_RATIONALE_SHOWN = "permissionRationaleShown";
    private List<String> permissionsToRequest = new ArrayList<>();

    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                boolean allGranted = true;
                for (Boolean granted : permissions.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                if (!allGranted) {
                    Toast.makeText(this, "Some features may be disabled due to denied permissions.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TextView userEmailTextView = findViewById(R.id.textViewUserEmail);
        Button syncNowButton = findViewById(R.id.buttonSyncNow);
        Button managePermissionsButton = findViewById(R.id.buttonManagePermissions);
        Button logoutButton = findViewById(R.id.buttonLogout);

        CredentialsManager credentialsManager = new CredentialsManager(this);
        userEmailTextView.setText(credentialsManager.getEmail());

        syncNowButton.setOnClickListener(v -> {
            Toast.makeText(this, R.string.sync_started, Toast.LENGTH_SHORT).show();
            OneTimeWorkRequest syncWorkRequest = new OneTimeWorkRequest.Builder(EmailSyncWorker.class).build();
            WorkManager.getInstance(this).enqueue(syncWorkRequest);
        });

        managePermissionsButton.setOnClickListener(v -> checkAndRequestPermissions());

        logoutButton.setOnClickListener(v -> {
            credentialsManager.saveCredentials(null, null);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void checkAndRequestPermissions() {
        permissionsToRequest.clear();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            showPermissionRationaleDialog();
        }
    }

    private void showPermissionRationaleDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_title)
                .setMessage(R.string.permission_rationale)
                .setPositiveButton(R.string.grant, (dialog, which) -> {
                    requestPermissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
                })
                .setNegativeButton(R.string.later, (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}
