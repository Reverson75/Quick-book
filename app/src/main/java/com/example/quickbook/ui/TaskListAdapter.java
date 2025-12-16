package com.example.quickbook.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quickbook.R;
import com.example.quickbook.data.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class TaskListAdapter extends ListAdapter<Task, TaskListAdapter.TaskViewHolder> {

    private OnItemClickListener listener;

    public TaskListAdapter(@NonNull DiffUtil.ItemCallback<Task> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task current = getItem(position);
        holder.bind(current);
    }

    public Task getTaskAtPosition(int position) {
        return getItem(position);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final TextView dueDateView;
        private final TextView locationView;
        private final RatingBar complexityView;

        public TaskViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.textViewTaskTitle);
            dueDateView = itemView.findViewById(R.id.textViewDueDate);
            locationView = itemView.findViewById(R.id.textViewLocation);
            complexityView = itemView.findViewById(R.id.ratingBarComplexity);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });
        }

        public void bind(Task task) {
            titleView.setText(task.getTitle());
            dueDateView.setText(formatDate(task.getDueDate()));
            if (task.getLocation() != null && !task.getLocation().isEmpty()) {
                locationView.setText(task.getLocation());
                locationView.setVisibility(View.VISIBLE);
            } else {
                locationView.setVisibility(View.GONE);
            }
            complexityView.setRating(task.getComplexity());
        }

        private String formatDate(long dateInMillis) {
            if (dateInMillis == 0) return itemView.getContext().getString(R.string.no_due_date);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.format(new Date(dateInMillis));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Task task);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class TaskDiff extends DiffUtil.ItemCallback<Task> {

        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getDueDate() == newItem.getDueDate()
                    && oldItem.getComplexity() == newItem.getComplexity()
                    && Objects.equals(oldItem.getLocation(), newItem.getLocation());
        }
    }
}
