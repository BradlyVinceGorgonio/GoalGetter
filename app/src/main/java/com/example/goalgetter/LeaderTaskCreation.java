package com.example.goalgetter;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LeaderTaskCreation extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private Uri fileUri;
    private String generatedFileName;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private String chatRoomId;
    private String groupName;
    private List<String> selectedUserIds = new ArrayList<>();

    EditText taskTitleEditText;
    EditText descriptionEditText;
    EditText dateStartedEditText;
    EditText deadlineEditText;
    EditText alarmEditText;
    CheckBox priorityModeCheckBox;

    Button uploadGroupTaskFile;
    Button CreateGroupTaskButton;
    String fileName;

    int mYear, mMonth, mDay;
    int mHour, mMinute;
    AlarmManager alarmManager;
    TimePickerDialog timePickerDialog;
    DatePickerDialog datePickerDialog;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_leader_task_creation);
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




        dateStartedEditText.setOnTouchListener((view, motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                datePickerDialog = new DatePickerDialog(this,
                        (view1, year, monthOfYear, dayOfMonth) -> {
                            dateStartedEditText.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                            datePickerDialog.dismiss();
                        }, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
            return true;
        });

        deadlineEditText.setOnTouchListener((view, motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                datePickerDialog = new DatePickerDialog(this,
                        (view1, year, monthOfYear, dayOfMonth) -> {
                            deadlineEditText.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                            datePickerDialog.dismiss();
                        }, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
            return true;
        });

        alarmEditText.setOnTouchListener((view, motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                // Get Current Time
                final Calendar c = Calendar.getInstance();
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                timePickerDialog = new TimePickerDialog(this,
                        (view12, hourOfDay, minute) -> {
                            alarmEditText.setText(hourOfDay + ":" + minute);
                            timePickerDialog.dismiss();
                        }, mHour, mMinute, false);
                timePickerDialog.show();
            }
            return true;
        });


        uploadGroupTaskFile = findViewById(R.id.uploadGroupTaskFile);
        CreateGroupTaskButton = findViewById(R.id.CreateGroupTaskButton);

        // Initialize Firebase Storage
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        // File selection
        uploadGroupTaskFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open file picker
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*"); // Adjust as needed for specific file types
                startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST);
            }
        });





        chatRoomId = getIntent().getStringExtra("groupChatId");
        groupName = getIntent().getStringExtra("groupChatName");

        Log.d("KUPAL", "onCreate: " + chatRoomId + " KUPAL " + groupName);

        // Fetch users in the selected group chat
        fetchUsersInGroupChat(chatRoomId);



        // LEADER ID only uid of this

        // File upload
        CreateGroupTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskTitle = taskTitleEditText.getText().toString().trim();
                String description = descriptionEditText.getText().toString().trim();
                String dateStarted = dateStartedEditText.getText().toString().trim();
                String deadline = deadlineEditText.getText().toString().trim();
                String alarm = alarmEditText.getText().toString().trim();

                if (taskTitle.isEmpty()) {
                    taskTitleEditText.setError("Task title is required");
                    taskTitleEditText.requestFocus();
                    return;
                }
                if (description.isEmpty()) {
                    descriptionEditText.setError("Description is required");
                    descriptionEditText.requestFocus();
                    return;
                }
                if (dateStarted.isEmpty()) {
                    dateStartedEditText.setError("Start date is required");
                    dateStartedEditText.requestFocus();
                    return;
                }
                if (deadline.isEmpty()) {
                    deadlineEditText.setError("Deadline is required");
                    deadlineEditText.requestFocus();
                    return;
                }
                if (alarm.isEmpty()) {
                    alarmEditText.setError("Alarm time is required");
                    alarmEditText.requestFocus();
                    return;
                }
                if (selectedUserIds.isEmpty()) {
                    Toast.makeText(LeaderTaskCreation.this, "At least one user must be selected", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (fileUri == null) {
                    Toast.makeText(LeaderTaskCreation.this, "Please select a file", Toast.LENGTH_SHORT).show();
                    return;
                }

                uploadFileToFirebase();
                insertTask(taskTitle, description, dateStarted, deadline, alarm, priorityModeCheckBox.isChecked() ? "Yes" : "No", generatedFileName);

            }
        });

    }
    public void insertTask(String taskTitle, String description, String dateStart, String dateDue, String alarmTime, String priorityMode, String fileName) {
        // Get the current user's UID
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUid = auth.getCurrentUser().getUid();

        // Define task data in a map
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("groupId", chatRoomId);
        taskData.put("taskTitle", taskTitle);
        taskData.put("description", description);
        taskData.put("dateStart", dateStart);
        taskData.put("dateDue", dateDue);
        taskData.put("alarmTime", alarmTime);
        taskData.put("priorityMode", priorityMode);
        taskData.put("fileName", fileName);
        taskData.put("leaderId", currentUid); // Add UID of the user who added it
        taskData.put("isGroup", true);       // Assuming this is not a group task
        taskData.put("isCompleted", false);  // Default to not completed
        taskData.put("isApproved", false);
        taskData.put("uids", selectedUserIds);

        // Reference to the "tasks" collection
        db.collection("allTasks")
                .add(taskData)  // Add document with auto-generated ID
                .addOnSuccessListener(documentReference -> {
                    // Retrieve the auto-generated document ID
                    String generatedTaskId = documentReference.getId();

                    // Add the taskId to the task document
                    documentReference.update("taskId", generatedTaskId)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("INSERT_TASK", "Task added with ID: " + generatedTaskId);
                                Intent intent = new Intent(LeaderTaskCreation.this, bottomNavigation.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish(); // Close the CreateTask activity completely

                            })
                            .addOnFailureListener(e -> {
                                Log.e("INSERT_TASK", "Error updating task ID: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("INSERT_TASK", "Error adding task: " + e.getMessage());
                });
    }

    private void fetchUsersInGroupChat(String chatRoomId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("chatGroups").document(chatRoomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<User> usersList = new ArrayList<>();
                        List<Map<String, Object>> users = (List<Map<String, Object>>) documentSnapshot.get("users");

                        if (users != null) {
                            for (Map<String, Object> userMap : users) {
                                String uid = (String) userMap.get("uid");
                                String name = (String) userMap.get("name");
                                String email = (String) userMap.get("email"); // Optional if needed

                                User user = new User(uid, email, name);
                                usersList.add(user);
                            }

                            // Dynamically create checkboxes for the users
                            createDynamicCheckBoxes(usersList);
                        }
                    } else {
                        Log.d("ERROR", "No such chat group with ID: " + chatRoomId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ERROR", "Error fetching group chat users: ", e);
                });
    }
    private void createDynamicCheckBoxes(List<User> usersList) {
        LinearLayout parentLayout = findViewById(R.id.parentLinearLayout); // ID of your LinearLayout container

        for (User user : usersList) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            checkBox.setText(user.getName()); // Display the user's name
            checkBox.setTextSize(16);
            checkBox.setTextColor(getResources().getColor(R.color.black));

            // Set a tag with the user ID for identification
            checkBox.setTag(user.getUid());

            // Add an onCheckedChangeListener to update the selected users list
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String uid = (String) buttonView.getTag();
                if (isChecked) {
                    if (!selectedUserIds.contains(uid)) {
                        selectedUserIds.add(uid);
                    }
                    Log.d("KUPAL", "Selected User ID: " + uid);
                } else {
                    selectedUserIds.remove(uid);
                    Log.d("KUPAL", "Unselected User ID: " + uid);
                }
            });

            // Add the checkbox to the parent layout
            parentLayout.addView(checkBox);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();

            // Get the file name
            fileName = getFileName(fileUri);
            Toast.makeText(this, "File selected: " + fileName, Toast.LENGTH_SHORT).show();

            uploadGroupTaskFile.setText(fileName);

            // Store the file name in a local variable
            generatedFileName = fileName;
        }
    }

    private void uploadFileToFirebase() {
        // Generate a random file name
        generatedFileName = UUID.randomUUID().toString();

        // Define the path in Firebase Storage
        StorageReference fileReference = storageReference.child("task_files/" + generatedFileName);

        // Upload file
        fileReference.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(LeaderTaskCreation.this, "File uploaded successfully!", Toast.LENGTH_SHORT).show();
                        // You can use the generatedFileName here for further processing
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LeaderTaskCreation.this, "File upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
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

        // Fallback for URIs that use "file" scheme
        if (fileName == null) {
            fileName = uri.getLastPathSegment();
        }

        return fileName;
    }
}



