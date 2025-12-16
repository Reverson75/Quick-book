package com.example.quickbook.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.quickbook.R;
import com.example.quickbook.security.CredentialsManager;
import com.example.quickbook.ui.LoginActivity;
import com.example.quickbook.worker.EmailSyncWorker;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AccountBottomSheetFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_account, container, false);

        TextView userEmailTextView = view.findViewById(R.id.textViewUserEmail);
        Button syncNowButton = view.findViewById(R.id.buttonSyncNow);
        Button logoutButton = view.findViewById(R.id.buttonLogout);

        CredentialsManager credentialsManager = new CredentialsManager(requireContext());
        userEmailTextView.setText(credentialsManager.getEmail());

        syncNowButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Starting email sync...", Toast.LENGTH_SHORT).show();
            OneTimeWorkRequest syncWorkRequest = new OneTimeWorkRequest.Builder(EmailSyncWorker.class).build();
            WorkManager.getInstance(requireContext()).enqueue(syncWorkRequest);
            dismiss();
        });

        logoutButton.setOnClickListener(v -> {
            credentialsManager.saveCredentials(null, null); // Clear credentials
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            dismiss();
        });

        return view;
    }
}
