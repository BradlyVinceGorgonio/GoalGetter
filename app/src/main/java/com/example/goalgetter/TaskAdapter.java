package com.example.goalgetter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.courseNameTextView.setText(task.getCourseName());
        holder.dateTextView.setText(task.getDateDue());
        holder.taskTypeTextView.setText(task.getTaskType());
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void filterTasksByDate(int day, int month, int year) {
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : taskList) {
            if (task.isOnDate(day, month, year)) {
                filteredTasks.add(task);
            }
        }
        taskList.clear();
        taskList.addAll(filteredTasks);
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView courseNameTextView, dateTextView, taskTypeTextView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            courseNameTextView = itemView.findViewById(R.id.courseNameTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            taskTypeTextView = itemView.findViewById(R.id.taskTypeTextView);
        }
    }
}
