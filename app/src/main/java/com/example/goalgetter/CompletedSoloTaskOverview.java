package com.example.goalgetter;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class CompletedSoloTaskOverview extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText courseNameEditText, taskTypeEditText, dateStartedEditText, dateDueEditText, alarmEditText, descriptionEditText;
    private CheckBox priorityModeCheckBox;
    private ImageView uploadImageView;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    Button deleteCompletedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_completed_solo_task_overview);

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

        deleteCompletedButton = findViewById(R.id.deleteCompletedButton);
        deleteCompletedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskID = getIntent().getStringExtra("taskID"); // Get the taskID from the Intent

                if (taskID != null) {
                    db.collection("allTasks").document(taskID)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    // Retrieve the image file name from the Firestore document
                                    String imageFileName = documentSnapshot.getString("fileName");

                                    // Delete the Firestore document
                                    db.collection("allTasks").document(taskID)
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("DELETE_TASK", "Task document deleted successfully.");

                                                // Delete the image file from Firebase Storage
                                                if (imageFileName != null && !imageFileName.isEmpty()) {
                                                    deleteFileFromStorage("solotasksimages/" + imageFileName);
                                                }

                                                // Show success message and close the activity
                                                finish();
                                                Log.d("DELETE_TASK", "Task and image deleted successfully.");
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("DELETE_TASK", "Failed to delete task document: " + e.getMessage());
                                            });
                                } else {
                                    Log.e("DELETE_TASK", "Task document does not exist.");
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("DELETE_TASK", "Failed to retrieve task document: " + e.getMessage());
                            });
                } else {
                    Log.e("DELETE_TASK", "Task ID is null.");
                }
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
                                    Glide.with(CompletedSoloTaskOverview.this)
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
    // Helper method to delete a file from Firebase Storage
    private void deleteFileFromStorage(String filePath) {
        StorageReference fileRef = storageReference.child(filePath);

        fileRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("DELETE_FILE", "File " + filePath + " deleted successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e("DELETE_FILE", "Failed to delete file " + filePath + ": " + e.getMessage());
                });
    }
}