package com.example.goalgetter;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.view.View;
import android.widget.ImageButton;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreatedGroupTask extends AppCompatActivity {

    EditText taskTitleEditText, descriptionEditText, dateStartedEditText, deadlineEditText, alarmEditText;
    Button ViewGroupTaskFIle, SubmitFileTask, FinishedTaskButton;

    CheckBox priorityModeCheckBox;
    String fileName;
    String leaderFile;
    private static final int PICK_FILE_REQUEST = 1;
    private Uri fileUri;
    private String generatedFileName;
    private StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_created_group_task);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        taskTitleEditText = findViewById(R.id.taskTitleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        dateStartedEditText = findViewById(R.id.dateStartedEditText);
        deadlineEditText = findViewById(R.id.deadlineEditText);
        alarmEditText = findViewById(R.id.alarmEditText);

        priorityModeCheckBox = findViewById(R.id.priorityModeCheckBox);

        ViewGroupTaskFIle = findViewById(R.id.ViewGroupTaskFIle);
        SubmitFileTask = findViewById(R.id.SubmitFileTask);
        FinishedTaskButton = findViewById(R.id.FinishedTaskButton);

        // Initialize Firebase Storage reference
        storageReference = FirebaseStorage.getInstance().getReference();

        // Set an OnClickListener for the file picker
        SubmitFileTask = findViewById(R.id.SubmitFileTask);
        SubmitFileTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
            }
        });

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String taskID = getIntent().getStringExtra("taskID");

        FinishedTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Replace this with your actual file selection logic
                if (fileUri == null) {
                    Toast.makeText(CreatedGroupTask.this, "Please select a file", Toast.LENGTH_SHORT).show();
                    return;
                }


                uploadFileToFirebase();
                String taskID = getIntent().getStringExtra("taskID");
                updateTask(generatedFileName, taskID);

                Intent intent = new Intent(CreatedGroupTask.this, bottomNavigation.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Close the CreateTask activity completely

            }
        });



        fetchTaskData(taskID);
        fetchAndDisplayUserCheckboxes(taskID);

        ViewGroupTaskFIle = findViewById(R.id.ViewGroupTaskFIle);
        ViewGroupTaskFIle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileFromFirebase(leaderFile);
            }
        });

        Log.d("KUPAL", "Received " + taskID);


    }
    private void updateTask( String fileName, String taskID) {
        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Prepare the updated task data
        Map<String, Object> updatedTaskData = new HashMap<>();

        updatedTaskData.put("TeamTaskFile", fileName);
        updatedTaskData.put("isCompleted", true);
        updatedTaskData.put("isApproved", false);

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

                        // Display the data in the EditTexts
                        taskTitleEditText.setText(taskTitle);
                        descriptionEditText.setText(description);
                        dateStartedEditText.setText(dateStart);
                        deadlineEditText.setText(dateDue);
                        alarmEditText.setText(alarmTime);

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





    // Method to open the file picker
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Allow all file types
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST);
    }

    // Handle the result of the file picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();

            // Get the file name
            fileName = getFileName(fileUri);
            Toast.makeText(this, "File selected: " + fileName, Toast.LENGTH_SHORT).show();

            // Set the file name to the button text or another UI element
            SubmitFileTask.setText(fileName);

            // Generate a unique file name
            generatedFileName = UUID.randomUUID().toString();
        }
    }

    // Method to upload the file to Firebase
    private void uploadFileToFirebase() {
        if (fileUri != null) {
            // Define the path in Firebase Storage
            StorageReference fileReference = storageReference.child("task_files/" + generatedFileName);

            // Upload file
            fileReference.putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(CreatedGroupTask.this, "File uploaded successfully!", Toast.LENGTH_SHORT).show();
                            // You can use the generatedFileName here for further processing
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreatedGroupTask.this, "File upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to get the file name from the URI
    private String getFileName(Uri uri) {
        String fileName = null;

        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIndex);
                    }
                } finally {
                    cursor.close();
                }
            }
        }

        // Fallback for URIs that use the "file" scheme
        if (fileName == null) {
            fileName = uri.getLastPathSegment();
        }

        return fileName;
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