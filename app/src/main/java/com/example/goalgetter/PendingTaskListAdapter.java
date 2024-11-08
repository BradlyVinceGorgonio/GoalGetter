package com.example.goalgetter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PendingTaskListAdapter extends RecyclerView.Adapter<PendingTaskListAdapter.TaskViewHolder> {

    private List<PendingTaskList> taskList;
    private Context context;

    public PendingTaskListAdapter(List<PendingTaskList> taskList, Context context) {
        this.taskList = taskList;
        this.context = context;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        PendingTaskList task = taskList.get(position);

        // Set data to the views
        holder.courseNameTextView.setText(task.getCourseName());
        holder.dueDateTextView.setText("Due date: " + task.getDueDate());
        holder.taskTypeTextView.setText("Task Type: " + task.getTaskType());
        holder.duetimeTextView.setText("Due time: " + task.getDueTime());

        // Set the priority image based on priorityMode
        if (task.getPriorityMode().equals("Yes")) {
            holder.priorityLevelImageView.setImageResource(R.drawable.prioritylevelicred); // High priority image
        } else {
            holder.priorityLevelImageView.setImageResource(R.drawable.prioritylevelic); // Normal priority image
        }

        // You can handle the delete and edit buttons here (no changes needed for these buttons as per your request)
        // For example, setting listeners to handle clicks:
        holder.editImageButton.setOnClickListener(v -> {
            // Handle Edit button click
        });

        holder.deleteImageButton.setOnClickListener(v -> {
            // Handle Delete button click
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        ImageView priorityLevelImageView;
        TextView courseNameTextView, dueDateTextView, taskTypeTextView, duetimeTextView;
        ImageButton editImageButton, deleteImageButton;

        public TaskViewHolder(View itemView) {
            super(itemView);
            priorityLevelImageView = itemView.findViewById(R.id.priorityLevelImageView);
            courseNameTextView = itemView.findViewById(R.id.courseNameTextView);
            dueDateTextView = itemView.findViewById(R.id.dueDateTextView);
            taskTypeTextView = itemView.findViewById(R.id.taskTypeTextView);
            duetimeTextView = itemView.findViewById(R.id.duetimeTextView);
            editImageButton = itemView.findViewById(R.id.editImageButton);
            deleteImageButton = itemView.findViewById(R.id.deleteImageButton);
        }
    }
}