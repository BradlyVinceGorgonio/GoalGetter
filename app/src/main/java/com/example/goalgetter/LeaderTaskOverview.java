package com.example.goalgetter;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderTaskOverview extends AppCompatActivity {

    EditText GroupName, taskTitleEditText, descriptionEditText, dateStartedEditText, deadlineEditText, alarmEditText;
    CheckBox priorityModeCheckBox;
    Button ViewGroupTaskFIle, SubmittedFileTask;

    CardView completeTaskCard, retakeTaskCard;

    String fileName;
    String leaderFile;

    String teamTaskFile;
    private static final int PICK_FILE_REQUEST = 1;
    private Uri fileUri;
    private String generatedFileName;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_leader_task_overview);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String taskID = getIntent().getStringExtra("taskID");

        GroupName = findViewById(R.id.GroupName);
        taskTitleEditText = findViewById(R.id.taskTitleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        dateStartedEditText = findViewById(R.id.dateStartedEditText);
        deadlineEditText = findViewById(R.id.deadlineEditText);
        alarmEditText = findViewById(R.id.alarmEditText);

        priorityModeCheckBox = findViewById(R.id.priorityModeCheckBox);

        ViewGroupTaskFIle = findViewById(R.id.ViewGroupTaskFIle);
        SubmittedFileTask = findViewById(R.id.SubmittedFileTask);

        completeTaskCard = findViewById(R.id.completeTaskCard);
        retakeTaskCard = findViewById(R.id.retakeTaskCard);

        // Initialize Firebase Storage reference
        storageReference = FirebaseStorage.getInstance().getReference();


        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        ViewGroupTaskFIle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileFromFirebase(leaderFile);
            }
        });
        SubmittedFileTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileFromFirebase(teamTaskFile);
            }
        });

        completeTaskCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Update Tasks
                String taskID = getIntent().getStringExtra("taskID");
                updateTask(taskID);

                Intent intent = new Intent(LeaderTaskOverview.this, SharedTaskStatus.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Close the CreateTask activity completely

            }
        });
        retakeTaskCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Update Tasks
                String taskID = getIntent().getStringExtra("taskID");
                reviseTask(taskID);

                Intent intent = new Intent(LeaderTaskOverview.this, SharedTaskStatus.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Close the CreateTask activity completely

            }
        });



        fetchTaskData(taskID);
        //FOR CHECKBOXES
        fetchAndDisplayUserCheckboxes(taskID);



    }
    private void updateTask(String taskID) {
        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Prepare the updated task data
        Map<String, Object> updatedTaskData = new HashMap<>();

        updatedTaskData.put("isApproved", true);

        // Update the document in Firestore
        db.collection("allTasks")
                .document(taskID) // Locate the document by its ID
                .update(updatedTaskData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Task updated successfully!", Toast.LENGTH_SHORT).show();
                    Log.d("UPDATE_TASK", "Task updated successfully with ID: " + taskID);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("UPDATE_TASK", "Error updating task: " + e.getMessage());
                });
    }
    private void reviseTask(String taskID) {
        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Locate the document to fetch file names
        db.collection("allTasks").document(taskID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get file names from the document
                        String teamTaskFile = documentSnapshot.getString("TeamTaskFile");



                        // Reference to the file in Firebase Storage
                        StorageReference fileReference = FirebaseStorage.getInstance().getReference().child("task_files/" + teamTaskFile);

                        // Attempt to delete the file
                        fileReference.delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("DELETE_FILE", "File deleted successfully: " + teamTaskFile);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("DELETE_FILE", "Error deleting file: " + teamTaskFile + " - " + e.getMessage());
                                });

                        // Prepare the updated task data
                        Map<String, Object> updatedTaskData = new HashMap<>();
                        updatedTaskData.put("isApproved", false);
                        updatedTaskData.put("isCompleted", false);

                        // Update the document in Firestore
                        db.collection("allTasks")
                                .document(taskID)
                                .update(updatedTaskData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Task updated and files deleted successfully!", Toast.LENGTH_SHORT).show();
                                    Log.d("UPDATE_TASK", "Task updated successfully with ID: " + taskID);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error updating task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("UPDATE_TASK", "Error updating task: " + e.getMessage());
                                });
                    } else {
                        Toast.makeText(this, "Task not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FETCH_TASK", "Error fetching task: " + e.getMessage());
                });
    }


    private void fetchTaskData(String taskID) {
        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the "allTasks" collection
        db.collection("allTasks")
                .document(taskID) // Use the provided taskID to locate the document
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve task data
                        String taskTitle = documentSnapshot.getString("courseName");
                        String description = documentSnapshot.getString("description");
                        String dateStart = documentSnapshot.getString("dateStart");
                        String dateDue = documentSnapshot.getString("dateDue");
                        String alarmTime = documentSnapshot.getString("alarmTime");
                        String priorityMode = documentSnapshot.getString("priorityMode");
                        leaderFile = documentSnapshot.getString("fileName");
                        teamTaskFile = documentSnapshot.getString("TeamTaskFile");
                        String groupName = documentSnapshot.getString("groupName");

                        // Display the data in the EditTexts
                        taskTitleEditText.setText(taskTitle);
                        descriptionEditText.setText(description);
                        dateStartedEditText.setText(dateStart);
                        deadlineEditText.setText(dateDue);
                        alarmEditText.setText(alarmTime);
                        GroupName.setText(groupName);
                        priorityModeCheckBox.setChecked("Yes".equalsIgnoreCase(priorityMode));

                    } else {
                        Log.e("FETCH_TASK", "No task found with ID: " + taskID);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FETCH_TASK", "Error fetching task: " + e.getMessage());
                });
    }
    private void fetchAndDisplayUserCheckboxes(String taskID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        LinearLayout checkBoxContainer = findViewById(R.id.checkBoxContainer); // A container layout for the checkboxes

        // Clear previous checkboxes to avoid duplicates
        checkBoxContainer.removeAllViews();

        // Fetch task document to get the uids array
        db.collection("allTasks")
                .document(taskID)
                .get()
                .addOnSuccessListener(taskSnapshot -> {
                    if (taskSnapshot.exists()) {
                        // Get the "uids" field
                        List<String> uids = (List<String>) taskSnapshot.get("uids");

                        if (uids != null) {
                            // Iterate through each UID to fetch user names
                            for (String uid : uids) {
                                db.collection("students")
                                        .document(uid)
                                        .get()
                                        .addOnSuccessListener(userSnapshot -> {
                                            if (userSnapshot.exists()) {
                                                // Get the "name" field
                                                String userName = userSnapshot.getString("name");

                                                // Dynamically create a checkbox
                                                CheckBox checkBox = new CheckBox(this);
                                                checkBox.setText(userName); // Set the checkbox label
                                                checkBox.setTag(uid);       // Optional: set UID as tag for later use

                                                // Set the checkbox as checked and un-uncheckable
                                                checkBox.setChecked(true);
                                                checkBox.setEnabled(false);

                                                // Add the checkbox to the container
                                                checkBoxContainer.addView(checkBox);
                                            } else {
                                                Log.e("FETCH_USER", "No user found with UID: " + uid);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("FETCH_USER", "Error fetching user: " + e.getMessage());
                                        });
                            }
                        } else {
                            Log.e("FETCH_TASK", "No uids field found in task");
                        }
                    } else {
                        Log.e("FETCH_TASK", "No task found with ID: " + taskID);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FETCH_TASK", "Error fetching task: " + e.getMessage());
                });
    }
    private void openFileFromFirebase(String filename) {
        // Reference to the file in Firebase Storage
        StorageReference fileReference = FirebaseStorage.getInstance().getReference().child("task_files/" + filename);

        // Try to get the file's download URI
        fileReference.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // Open the file using the URI
                    Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
                    openFileIntent.setDataAndType(uri, getMimeType(uri.toString())); // Get MIME type dynamically
                    openFileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    try {
                        startActivity(openFileIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(this, "No application found to open this file", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to open file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Helper method to get the MIME type of the file
    private String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
}