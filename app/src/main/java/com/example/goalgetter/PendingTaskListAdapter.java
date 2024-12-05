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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;

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
        // Inflate the appropriate layout based on whether the task is a group task or not
        View view;
        if (viewType == 1) {
            // If it's a group task, use groupmember_list_item.xml
            view = LayoutInflater.from(context).inflate(R.layout.groupmember_list_item, parent, false);
        } else {
            // Otherwise, use list_item.xml
            view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        }
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        PendingTaskList task = taskList.get(position);

        // Set data to the views
        holder.courseNameTextView.setText(task.getCourseName());
        holder.dueDateTextView.setText(task.getDueDate());
        holder.taskTypeTextView.setText(task.getTaskType());
        holder.duetimeTextView.setText(task.getDueTime());

        // Set an onClickListener for each task item
        holder.itemView.setOnClickListener(v -> {
            // Fetch the task ID to fetch the 'isGroup' field from Firestore
            String taskId = task.getTaskID();

            // Fetch the document from Firestore
            db.collection("allTasks").document(taskId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Check the 'isGroup' field
                            boolean isGroup = documentSnapshot.getBoolean("isGroup");

                            // If 'isGroup' is true, open GroupTaskActivity
                            boolean isCompleted = documentSnapshot.getBoolean("isCompleted");
                            String leaderId = documentSnapshot.getString("leaderId");
                            Boolean isApproved = documentSnapshot.getBoolean("isApproved");
                            List<Map<String, Object>> users = (List<Map<String, Object>>) documentSnapshot.get("users");

                            // Get current user's UID
                            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            // Check if the current user is part of the 'users' array
                            boolean isCurrentUserInGroup = false;
                            if (users != null) {
                                for (Map<String, Object> userMap : users) {
                                    String uid = (String) userMap.get("uid");
                                    if (currentUserId.equals(uid)) {
                                        isCurrentUserInGroup = true;
                                        break;
                                    }
                                }
                            }

                            //


                            // Open different activities based on 'isGroup'
                            Intent intent;
                            if (isGroup) {

                                // Leader - Completed
                                if(isCompleted && isApproved && leaderId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                    intent = new Intent(context, ApprovedGroupTask.class);
                                }
                                // Leader - Pending
                                else if(isCompleted && !isApproved && leaderId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                    intent = new Intent(context, LeaderTaskOverview.class);
                                }
                                // Member / Non Member - Pending
                                else if(isCompleted && !isApproved && !leaderId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                    intent = new Intent(context, UnapprovedGroupTask.class);
                                }
                                // Member / Non Member - Completed
                                else if(isCompleted && isApproved && !leaderId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                    intent = new Intent(context, CompletedApprovedGroupTask.class);
                                }

                                // Member - Ongoing
                                else {
                                    intent = new Intent(context, CreatedGroupTask.class);
                                }

                            } else {
                                // Solo - Completed
                                if(isCompleted){
                                    intent = new Intent(context, CompletedSoloTaskOverview.class);
                                }
                                // Solo - Ongoing
                                else{
                                    intent = new Intent(context, DetailedTask.class);
                                }

                            }

                            // Pass the task ID to the new activity
                            intent.putExtra("taskID", taskId);
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, "Task document not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to fetch task details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // Set the priority image based on priorityMode
        if (task.getPriorityMode().equals("Yes")) {
            holder.priorityLevelImageView.setImageResource(R.drawable.flag_duotone_linered); // High priority image
        } else {
            holder.priorityLevelImageView.setImageResource(R.drawable.flag_duotone_line); // Normal priority image
        }

        // Handle delete button click
        if (holder.deleteImageButton != null) {
            holder.deleteImageButton.setOnClickListener(v -> {
                String taskId = task.getTaskID();

                // First, fetch the document from Firestore to get the fileName
                db.collection("allTasks").document(taskId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String fileName = documentSnapshot.getString("fileName");
                                String teamTaskFile = documentSnapshot.getString("TeamTaskFile");

                                // Delete the Firestore document
                                db.collection("allTasks").document(taskId)
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(context, "Task deleted successfully", Toast.LENGTH_SHORT).show();

                                            // If fileName exists, attempt to delete the image from Firebase Storage
                                            if (fileName != null && !fileName.isEmpty()) {
                                                // Try deleting from "solotasksimages/"
                                                StorageReference imageRef = FirebaseStorage.getInstance()
                                                        .getReference().child("solotasksimages/" + fileName);

                                                imageRef.delete()
                                                        .addOnSuccessListener(aVoid1 -> {
                                                            Toast.makeText(context, "Image deleted successfully from solotasksimages", Toast.LENGTH_SHORT).show();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            // If deletion fails in "solotasksimages/", try "task_files/"
                                                            StorageReference fallbackRef = FirebaseStorage.getInstance()
                                                                    .getReference().child("task_files/" + fileName);

                                                            fallbackRef.delete()
                                                                    .addOnSuccessListener(aVoid2 -> {
                                                                        Toast.makeText(context, "Image deleted successfully from task_files", Toast.LENGTH_SHORT).show();
                                                                    })
                                                                    .addOnFailureListener(e2 -> {
                                                                        Toast.makeText(context, "Failed to delete image from both directories: " + e2.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    });
                                                        });
                                            }

                                            // If teamTaskFile exists, attempt to delete the file from Firebase Storage
                                            if (teamTaskFile != null && !teamTaskFile.isEmpty()) {
                                                StorageReference teamTaskRef = FirebaseStorage.getInstance()
                                                        .getReference().child("task_files/" + teamTaskFile);

                                                teamTaskRef.delete()
                                                        .addOnSuccessListener(aVoid1 -> {
                                                            Toast.makeText(context, "Team task file deleted successfully", Toast.LENGTH_SHORT).show();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(context, "Failed to delete team task file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
    @Override
    public int getItemViewType(int position) {
        // Check if the task is a group task, return 1 for group tasks, 0 for non-group tasks
        PendingTaskList task = taskList.get(position);
        return task.isGroup() ? 1 : 0;
    }
}