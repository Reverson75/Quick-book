package com.example.quickbook.ui.account;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.quickbook.R;
import com.example.quickbook.security.CredentialsManager;
import com.example.quickbook.ui.LoginActivity;
import com.example.quickbook.worker.EmailSyncWorker;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

    private List<String> permissionsToRequest = new ArrayList<>();
    private CredentialsManager credentialsManager;
    private TextInputEditText keywordsEditText;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> checkExactAlarmPermission());

    private final ActivityResultLauncher<Intent> requestExactAlarmPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
                    if (alarmManager.canScheduleExactAlarms()) {
                        Toast.makeText(getContext(), "Permission for exact alarms granted!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Reminders may be delayed without this permission.", Toast.LENGTH_LONG).show();
                    }
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        credentialsManager = new CredentialsManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.title_settings);

        TextView userEmailTextView = view.findViewById(R.id.textViewUserEmail);
        keywordsEditText = view.findViewById(R.id.editTextKeywords);
        Button changeLanguageButton = view.findViewById(R.id.buttonChangeLanguage);
        Button syncNowButton = view.findViewById(R.id.buttonSyncNow);
        Button managePermissionsButton = view.findViewById(R.id.buttonManagePermissions);
        Button logoutButton = view.findViewById(R.id.buttonLogout);

        userEmailTextView.setText(credentialsManager.getEmail());
        keywordsEditText.setText(credentialsManager.getKeywords());

        changeLanguageButton.setOnClickListener(v -> {
            LanguageSelectionBottomSheet bottomSheet = new LanguageSelectionBottomSheet();
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        });

        syncNowButton.setOnClickListener(v -> {
            saveKeywords();
            Toast.makeText(getContext(), R.string.sync_started, Toast.LENGTH_SHORT).show();
            OneTimeWorkRequest syncWorkRequest = new OneTimeWorkRequest.Builder(EmailSyncWorker.class).build();
            WorkManager.getInstance(requireContext()).enqueue(syncWorkRequest);
        });

        managePermissionsButton.setOnClickListener(v -> checkAndRequestPermissions());

        logoutButton.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.logout)
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton(R.string.logout, (dialog, which) -> {
                        credentialsManager.clearCredentials();
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        if (getActivity() != null) getActivity().finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        saveKeywords();
    }

    private void saveKeywords() {
        if (keywordsEditText != null) {
            credentialsManager.saveKeywords(keywordsEditText.getText().toString());
        }
    }

    private void checkAndRequestPermissions() {
        permissionsToRequest.clear();
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            showPermissionRationaleDialog();
        } else {
            checkExactAlarmPermission();
        }
    }

    private void showPermissionRationaleDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.permission_title)
                .setMessage(R.string.permission_rationale)
                .setPositiveButton(R.string.grant, (dialog, which) -> requestPermissionLauncher.launch(permissionsToRequest.toArray(new String[0])))
                .setNegativeButton(R.string.later, (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                showExactAlarmRationaleDialog();
            }
        }
    }

    private void showExactAlarmRationaleDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Special Permission Needed")
            .setMessage("To ensure your task reminders go off at the precise time, please grant this permission.")
            .setPositiveButton("Grant", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.fromParts("package", requireActivity().getPackageName(), null));
                requestExactAlarmPermissionLauncher.launch(intent);
            })
            .setNegativeButton("Later", null)
            .show();
    }
}
