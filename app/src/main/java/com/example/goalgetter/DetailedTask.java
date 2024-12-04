package com.example.goalgetter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.view.LayoutInflater;


public class DetailedTask extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText courseNameEditText, taskTypeEditText, dateStartedEditText, dateDueEditText, alarmEditText, descriptionEditText;
    private CheckBox priorityModeCheckBox;
    private ImageView uploadImageView;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    Button taskCompletedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detailed_task);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Initialize Views
        courseNameEditText = findViewById(R.id.courseNameEditText);
        taskTypeEditText = findViewById(R.id.taskTypeEditText);
        dateStartedEditText = findViewById(R.id.dateStartedEditText);
        dateDueEditText = findViewById(R.id.deadlineEditText);
        alarmEditText = findViewById(R.id.alarmEditText);
        priorityModeCheckBox = findViewById(R.id.priorityModeCheckBox);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        uploadImageView = findViewById(R.id.uploadImageView);

        // Get the taskID from the Intent
        String taskID = getIntent().getStringExtra("taskID");

        // Fetch the task details from Firestore
        fetchTaskDetails(taskID);

        // Handle window insets (system bars)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        taskCompletedButton = findViewById(R.id.taskCompletedButton);
        taskCompletedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirmation dialog before marking the task as completed
                showConfirmationDialog(taskID);

            }
        });
    }


    private void showConfirmationDialog(String taskID){
        // Inflate the custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.confirm_dialog, null);

        // Initialize dialog views
        Button okButton = dialogView.findViewById(R.id.okButton);

        // Create and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set the "OK" button click listener
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (taskID != null) {
                    // Reference to the task document in Firestore
                    DocumentReference taskRef = db.collection("allTasks").document(taskID);

                    // Update the isCompleted field to true
                    taskRef.update("isCompleted", true)
                            .addOnSuccessListener(aVoid -> {
                                // Successfully updated the task as completed
                                Toast.makeText(DetailedTask.this, "Task marked as completed", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(DetailedTask.this, bottomNavigation.class);

                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish(); // Close the activity completely
                            })
                            .addOnFailureListener(e -> {
                                // Handle failure to update the task
                                Toast.makeText(DetailedTask.this, "Error updating task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }

                // Dismiss the dialog after the task is marked as completed
                dialog.dismiss();
            }
        });
    }


    private void fetchTaskDetails(String taskID) {
        db.collection("allTasks").document(taskID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Get task details from the document
                            String courseName = document.getString("courseName");
                            String taskType = document.getString("taskType");
                            String description = document.getString("description"); // Fetch description
                            String dateStarted = document.getString("dateStart");  // Fetch date started
                            String dueDate = document.getString("dateDue");
                            String dueTime = document.getString("alarmTime");
                            String priorityMode = document.getString("priorityMode");
                            String imageFileName = document.getString("fileName");

                            // Set the task details to the EditTexts
                            courseNameEditText.setText(courseName);
                            taskTypeEditText.setText(taskType);
                            dateStartedEditText.setText(dateStarted);  // Display date started
                            dateDueEditText.setText(dueDate);
                            alarmEditText.setText(dueTime);
                            descriptionEditText.setText(description);

                            // Set the CheckBox based on the priorityMode value
                            priorityModeCheckBox.setChecked("Yes".equals(priorityMode));

                            // Load the image from Firebase Storage
                            if (imageFileName != null && !imageFileName.isEmpty()) {
                                StorageReference imageRef = storageReference.child("solotasksimages/" + imageFileName);

                                // Get the download URL and load the image
                                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    // Use Glide to load the image into the ImageView
                                    Glide.with(DetailedTask.this)
                                            .load(uri)
                                            .into(uploadImageView);
                                }).addOnFailureListener(e -> {
                                    Log.e("DetailedTask", "Error loading image", e);
                                });
                            }
                        }
                    } else {
                        Log.d("TaskDetailActivity", "Error getting task details: ", task.getException());
                    }
                });
    }
}
