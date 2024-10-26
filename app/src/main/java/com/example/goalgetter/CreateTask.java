package com.example.goalgetter;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


import java.util.Calendar;

public class CreateTask extends AppCompatActivity {

    private ImageButton backButton;

    EditText courseNameEditText;
    EditText taskTypeEditText;
    EditText descriptionEditText;
    CheckBox priorityModeCheckBox;
    ImageView uploadImageView;
    Button uploadImageButton;

    EditText dateStartedEditText;
    EditText deadlineEditText;
    EditText alarmEditText;




    Button createTaskButton;

    int mYear, mMonth, mDay;
    int mHour, mMinute;
    AlarmManager alarmManager;
    TimePickerDialog timePickerDialog;
    DatePickerDialog datePickerDialog;
    MainActivity activity;


    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 101;
    private Uri imageUri;
    private String imageFileName;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_task);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        courseNameEditText = findViewById(R.id.courseNameEditText);
        taskTypeEditText = findViewById(R.id.taskTypeEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        priorityModeCheckBox = findViewById(R.id.priorityModeCheckBox);
        dateStartedEditText = findViewById(R.id.dateStartedEditText);
        deadlineEditText = findViewById(R.id.deadlineEditText);
        alarmEditText = findViewById(R.id.alarmEditText);



        uploadImageView = findViewById(R.id.uploadImageView);



        createTaskButton = findViewById(R.id.createTaskButton);



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


        uploadImageButton = findViewById(R.id.uploadImageButton);
        uploadImageButton.setOnClickListener(v -> selectImageSource());
















        createTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(courseNameEditText.getText().toString().isEmpty() || taskTypeEditText.getText().toString().isEmpty() || descriptionEditText.getText().toString().isEmpty() || dateStartedEditText.getText().toString().isEmpty() || deadlineEditText.getText().toString().isEmpty() || alarmEditText.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "All fields required", Toast.LENGTH_SHORT).show();
                }
                else if (imageUri == null)
                {
                    Toast.makeText(getApplicationContext(), "No image selected!", Toast.LENGTH_SHORT).show();
                }
                else if (imageUri != null && !courseNameEditText.getText().toString().isEmpty() && !taskTypeEditText.getText().toString().isEmpty() && !descriptionEditText.getText().toString().isEmpty() || !dateStartedEditText.getText().toString().isEmpty() || !deadlineEditText.getText().toString().isEmpty() || !alarmEditText.getText().toString().isEmpty()) {
                    // Retrieving text from EditText views
                    String courseName = courseNameEditText.getText().toString();
                    String taskType = taskTypeEditText.getText().toString();
                    String description = descriptionEditText.getText().toString();

                    // Retrieving the state of the CheckBox
                    boolean isPriorityMode = priorityModeCheckBox.isChecked();
                    String priorityMode = isPriorityMode ? "Yes" : "No";

                    // Retrieving text from other EditText views
                    String dateStarted = dateStartedEditText.getText().toString();
                    String deadline = deadlineEditText.getText().toString();
                    String alarm = alarmEditText.getText().toString();



                    uploadImageToFirebase(imageUri);
                    String fileName = getImageFileName();
                    createTask(courseName, taskType, description, priorityMode, dateStarted, deadline, alarm, fileName);

                    Log.d("UGOKEN", "onCreate: " + courseName + taskType + description + isPriorityMode + dateStarted + deadline + alarm);

                    Log.d("UGOKEN", fileName);

                    Toast.makeText(getApplicationContext(), "Task Uploaded", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(CreateTask.this, bottomNavigation.class);
                    startActivity(intent);
                }
            }
        });


        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            finish();
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



    }

    public void createTask(String courseName, String taskType, String description, String priorityMode,
                           String dateStart, String dateDue, String alarmTime, String fileName) {

        // Get the current user's UID
        String currentUid = auth.getCurrentUser().getUid();

        // Define task data in a map
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("courseName", courseName);
        taskData.put("taskType", taskType);
        taskData.put("description", description);
        taskData.put("priorityMode", priorityMode);
        taskData.put("dateStart", dateStart);
        taskData.put("dateDue", dateDue);
        taskData.put("alarmTime", alarmTime);
        taskData.put("fileName", fileName);

        // Parse dateDue and alarmTime to create Deadline Due timestamp
        try {
            String deadlineString = dateDue + " " + alarmTime; // Combine dateDue and alarmTime
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm"); // Define the format
            Date deadlineDate = format.parse(deadlineString); // Parse into Date object
            Timestamp deadlineDueTimestamp = new Timestamp(deadlineDate); // Convert Date to Timestamp

            // Add Deadline Due timestamp to the task data
            taskData.put("deadlineDue", deadlineDueTimestamp);
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println("Error parsing date and time: " + e.getMessage());
        }

        // Reference to the "solotask" subcollection under the current UID
        db.collection("students")
                .document(currentUid)
                .collection("solotask")
                .add(taskData)  // Add document with auto-generated ID
                .addOnSuccessListener(documentReference -> {
                    // Retrieve the auto-generated document ID
                    String generatedTaskId = documentReference.getId();

                    // You can use generatedTaskId here as needed
                    System.out.println("Task added with ID: " + generatedTaskId);
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error adding task: " + e.getMessage());
                });
    }

    private void selectImageSource() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Camera option
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED) {
                            openCamera();
                        } else {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                        }
                    } else {
                        // Gallery option
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                == PackageManager.PERMISSION_GRANTED) {
                            openGallery();
                        } else {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_REQUEST_CODE);
                        }
                    }
                })
                .show();
    }

    // Step 4: Open Camera
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    // Step 5: Open Gallery
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    // Handle activity results from camera or gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE && data != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imageUri = getImageUriFromBitmap(photo);
                uploadImageView.setImageBitmap(photo); // Display the image from camera
                Toast.makeText(this, "Image selected from camera", Toast.LENGTH_SHORT).show();
            } else if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                imageUri = data.getData();
                uploadImageView.setImageURI(imageUri); // Display the image from gallery
                Toast.makeText(this, "Image selected from gallery", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Step 6: Convert Bitmap (from camera) to Uri
    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    // Step 7: Upload Image to Firebase and get file name
    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            // Generate a random file name
            imageFileName = UUID.randomUUID().toString();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                    .child("solotasksimages/" + imageFileName);

            // Start the upload when "Upload Task" is clicked
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                        // Add further actions if needed, like updating your database with `imageFileName`
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Getter for the uploaded file name
    public String getImageFileName() {
        return imageFileName;
    }

    // Handle permissions result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else if (requestCode == GALLERY_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}