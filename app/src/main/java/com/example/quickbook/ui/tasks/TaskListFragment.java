package com.example.quickbook.ui.tasks;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quickbook.R;
import com.example.quickbook.data.Task;
import com.example.quickbook.receiver.TaskAlarmReceiver;
import com.example.quickbook.ui.AddEditTaskActivity;
import com.example.quickbook.ui.TaskListAdapter;
import com.example.quickbook.util.GeofenceHelper;
import com.example.quickbook.viewmodel.TaskViewModel;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TaskListFragment extends Fragment {

    private static final String TAG = "TaskListFragment";
    private static final float GEOFENCE_RADIUS = 100;

    private TaskViewModel mTaskViewModel;
    private GeofencingClient mGeofencingClient;
    private GeofenceHelper mGeofenceHelper;
    private TaskListAdapter adapter;

    private ActivityResultLauncher<Intent> newTaskActivityLauncher;
    private ActivityResultLauncher<Intent> editTaskActivityLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTaskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        newTaskActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        handleActivityResult(result.getData(), false);
                    }
                });

        editTaskActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        handleActivityResult(result.getData(), true);
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.title_tasks);

        mGeofencingClient = LocationServices.getGeofencingClient(requireActivity());
        mGeofenceHelper = new GeofenceHelper(requireContext());

        setupRecyclerView(view);
        setupFab(view);

        mTaskViewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            adapter.submitList(tasks);
        });
    }

    private void handleActivityResult(Intent data, boolean isEdit) {
        String title = data.getStringExtra(AddEditTaskActivity.EXTRA_REPLY_TITLE);
        if (title == null) return;

        Task task = new Task();
        task.setTitle(title);
        task.setLocation(data.getStringExtra(AddEditTaskActivity.EXTRA_REPLY_LOCATION));
        task.setComplexity(data.getIntExtra(AddEditTaskActivity.EXTRA_REPLY_COMPLEXITY, 1));
        task.setDueDate(data.getLongExtra(AddEditTaskActivity.EXTRA_REPLY_DUEDATE, 0));
        task.setRepetition(data.getIntExtra(AddEditTaskActivity.EXTRA_REPLY_REPETITION, Task.REPEAT_NONE));

        if (isEdit) {
            int id = data.getIntExtra(AddEditTaskActivity.EXTRA_ID, -1);
            if (id != -1) {
                task.setId(id);
                mTaskViewModel.update(task);
            }
        } else {
            mTaskViewModel.insert(task);
        }
        
        if (task.getLocation() != null && !task.getLocation().isEmpty()) {
            addGeofenceForTask(task);
        }
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        adapter = new TaskListAdapter(new TaskListAdapter.TaskDiff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter.setOnItemClickListener(task -> {
            Intent intent = new Intent(getActivity(), AddEditTaskActivity.class);
            intent.putExtra(AddEditTaskActivity.EXTRA_ID, task.getId());
            intent.putExtra(AddEditTaskActivity.EXTRA_REPLY_TITLE, task.getTitle());
            intent.putExtra(AddEditTaskActivity.EXTRA_REPLY_DUEDATE, task.getDueDate());
            intent.putExtra(AddEditTaskActivity.EXTRA_REPLY_COMPLEXITY, task.getComplexity());
            intent.putExtra(AddEditTaskActivity.EXTRA_REPLY_LOCATION, task.getLocation());
            intent.putExtra(AddEditTaskActivity.EXTRA_REPLY_REPETITION, task.getRepetition());
            editTaskActivityLauncher.launch(intent);
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder t) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Task task = adapter.getTaskAtPosition(viewHolder.getAdapterPosition());
                mTaskViewModel.delete(task);
                cancelAlarm(task.getId());
                mGeofencingClient.removeGeofences(Collections.singletonList(String.valueOf(task.getId())));
                Snackbar.make(view, R.string.task_removed, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> mTaskViewModel.insert(task))
                        .show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void setupFab(View view) {
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddEditTaskActivity.class);
            newTaskActivityLauncher.launch(intent);
        });
    }

    @SuppressLint("MissingPermission")
    private void addGeofenceForTask(Task task) {
        if (getContext() == null || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocationName(task.getLocation(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                Geofence geofence = mGeofenceHelper.getGeofence(String.valueOf(task.getId()), address.getLatitude(), address.getLongitude(), GEOFENCE_RADIUS, Geofence.GEOFENCE_TRANSITION_ENTER);
                PendingIntent pendingIntent = mGeofenceHelper.getPendingIntent(task.getId(), task.getTitle());
                mGeofencingClient.addGeofences(mGeofenceHelper.getGeofencingRequest(geofence), pendingIntent)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Geofence added for task ID: " + task.getId()))
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to add geofence for task ID: " + task.getId(), e));
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding failed for location: " + task.getLocation(), e);
        }
    }

    private void cancelAlarm(int taskId) {
        if (getContext() == null) return;
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), TaskAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), taskId, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
