package com.example.goalgetter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class PendingTaskListAdapter extends RecyclerView.Adapter<PendingTaskListAdapter.TaskViewHolder> {

    private List<PendingTaskList> taskList;
    private Context context;
    private FirebaseFirestore db;

    public PendingTaskListAdapter(List<PendingTaskList> taskList, Context context) {
        this.taskList = taskList;
        this.context = context;
        this.db = FirebaseFirestore.getInstance(); // Initialize Firestore
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

        // Set an onClickListener for each task item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailedTask.class);
            intent.putExtra("taskID", task.getTaskID()); // Pass the task ID to the new activity
            context.startActivity(intent);
        });
        // Set the priority image based on priorityMode
        if (task.getPriorityMode().equals("Yes")) {
            holder.priorityLevelImageView.setImageResource(R.drawable.prioritylevelicred); // High priority image
        } else {
            holder.priorityLevelImageView.setImageResource(R.drawable.prioritylevelic); // Normal priority image
        }

        // You can handle the delete and edit buttons here (no changes needed for these buttons as per your request)
        // For example, setting listeners to handle clicks:
     //   holder.editImageButton.setOnClickListener(v -> {
            // Handle Edit button click
      //  });

        holder.deleteImageButton.setOnClickListener(v -> {
            String taskId = task.getTaskID();

            // First, fetch the document from Firestore to get the fileName
            db.collection("allTasks").document(taskId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fileName = documentSnapshot.getString("fileName");

                            // Delete the Firestore document
                            db.collection("allTasks").document(taskId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Task deleted successfully", Toast.LENGTH_SHORT).show();

                                        // If fileName exists, delete the image from Firebase Storage
                                        if (fileName != null && !fileName.isEmpty()) {
                                            StorageReference imageRef = FirebaseStorage.getInstance()
                                                    .getReference().child("solotasksimages/" + fileName);

                                            imageRef.delete()
                                                    .addOnSuccessListener(aVoid1 -> {
                                                        Toast.makeText(context, "Image deleted successfully", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(context, "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        }

                                        // Update the task list and notify the adapter
                                        taskList.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, taskList.size());

                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to delete task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(context, "Task document not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to fetch task details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
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
         //   editImageButton = itemView.findViewById(R.id.editImageButton);
            deleteImageButton = itemView.findViewById(R.id.deleteImageButton);
        }
    }
}