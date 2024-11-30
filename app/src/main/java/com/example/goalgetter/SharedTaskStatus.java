package com.example.goalgetter;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;


public class SharedTaskStatus extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PendingTaskListAdapter pendingTaskListAdapter;
    private List<PendingTaskList> pendingTaskLists;
    private FirebaseFirestore db;
    private AutoCompleteTextView filterAutoCompleteTextView;
    private String groupName;
    private String chatRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shared_task_status);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        groupName = getIntent().getStringExtra("groupName");
        chatRoomId = getIntent().getStringExtra("groupChatId");
        Log.d("KUPAL", "RECEIVED " + chatRoomId + groupName);

        // Initialize Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Initialize RecyclerView and taskList
        recyclerView = findViewById(R.id.sharedTaskrecyclerView);
        pendingTaskLists = new ArrayList<>();


        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        pendingTaskListAdapter = new PendingTaskListAdapter(pendingTaskLists, this);
        recyclerView.setAdapter(pendingTaskListAdapter);

        filterAutoCompleteTextView = findViewById(R.id.filterSharedTaskStatus);

        // Set up AutoCompleteTextView with filter options
        String[] filters = {"Pending Tasks", "Completed Tasks"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, filters);
        filterAutoCompleteTextView.setAdapter(adapter);

        // Set item click listener for the filter
        filterAutoCompleteTextView.setOnItemClickListener((parentView, selectedItemView, position, id) -> {
            String selectedFilter = (String) parentView.getItemAtPosition(position);
            updateRecyclerView(selectedFilter); // Call the method to update RecyclerView
        });

        // Initial fetch of all tasks
        updateRecyclerView("Pending Tasks");
    }

    private void updateRecyclerView(String filter) {
        // Clear the previous data
        pendingTaskLists.clear();

        switch (filter) {
            //case "All Tasks":
            //AllTasks();
            //break;
            case "Pending Tasks":
                PendingTasks();
                break;
            case "Completed Tasks":
                CompletedTasks();
                break;
        }
    }
    private void PendingTasks() {
        String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy"); // Match your date format

        // First query: Fetch tasks where the user is a leader
        Task<QuerySnapshot> leaderTasksQuery = db.collection("allTasks")
                .whereEqualTo("leaderId", currentUserUID)
                .whereEqualTo("groupId", chatRoomId)
                .whereEqualTo("isApproved", false) // Filter for completed tasks
                .whereEqualTo("isCompleted", true)
                .whereEqualTo("isGroup", true)
                .get();

        // Second query: Fetch tasks where the user is in the "uids" array
        Task<QuerySnapshot> uidsTasksQuery = db.collection("allTasks")
                .whereArrayContains("uids", currentUserUID)
                .whereEqualTo("isApproved", false)
                .whereEqualTo("groupId", chatRoomId)
                .whereEqualTo("isCompleted", true)
                .whereEqualTo("isGroup", true)
                .get();

        // Combine results from both queries
        Tasks.whenAllSuccess(leaderTasksQuery, uidsTasksQuery)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        pendingTaskLists.clear();
                        int taskCount = 0;

                        Set<String> processedTaskIds = new HashSet<>(); // To avoid duplicate tasks
                        List<Object> results = task.getResult();

                        for (Object result : results) {
                            if (result instanceof QuerySnapshot) {
                                QuerySnapshot querySnapshot = (QuerySnapshot) result;

                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    String taskID = document.getId();

                                    // Avoid duplicates
                                    if (!processedTaskIds.contains(taskID)) {
                                        processedTaskIds.add(taskID);

                                        String courseName = document.getString("courseName");
                                        String taskType = document.getString("taskType");
                                        String dueDate = document.getString("dateDue");
                                        String startDate = document.getString("dateStart");
                                        String dueTime = document.getString("alarmTime");
                                        String priorityMode = document.getString("priorityMode");
                                        String UID = document.getString("uid");

                                        try {
                                            Date dateStart = dateFormat.parse(startDate);
                                            Date dateDue = dateFormat.parse(dueDate);

                                            // Create task data and add it to the list
                                            PendingTaskList taskData = new PendingTaskList(priorityMode, courseName, dueDate, taskType, dueTime, UID, taskID, dateDue);
                                            taskData.setDateDue(dateDue); // Add dateDue as a Date in PendingTaskList
                                            pendingTaskLists.add(taskData);
                                            taskCount++;
                                        } catch (ParseException e) {
                                            Log.e("HomeFragment", "Date parsing error: ", e);
                                        }
                                    }
                                }
                            }
                        }

                        // Sort tasks by due date (earliest due date at the top)
                        pendingTaskLists.sort((task1, task2) -> task1.getDateDue().compareTo(task2.getDateDue()));

                        pendingTaskListAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("HomeFragment", "Error combining tasks: ", task.getException());
                    }
                });
    }
    private void CompletedTasks() {
        String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy"); // Match your date format

        // First query: Fetch tasks where the user is a leader
        Task<QuerySnapshot> leaderTasksQuery = db.collection("allTasks")
                .whereEqualTo("leaderId", currentUserUID)
                .whereEqualTo("groupId", chatRoomId)
                .whereEqualTo("isApproved", true) // Filter for completed tasks
                .whereEqualTo("isCompleted", true)
                .whereEqualTo("isGroup", true)
                .get();

        // Second query: Fetch tasks where the user is in the "uids" array
        Task<QuerySnapshot> uidsTasksQuery = db.collection("allTasks")
                .whereArrayContains("uids", currentUserUID)
                .whereEqualTo("groupId", chatRoomId)
                .whereEqualTo("isApproved", true)
                .whereEqualTo("isCompleted", true)
                .whereEqualTo("isGroup", true)
                .get();

        // Combine results from both queries
        Tasks.whenAllSuccess(leaderTasksQuery, uidsTasksQuery)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        pendingTaskLists.clear();
                        int taskCount = 0;

                        Set<String> processedTaskIds = new HashSet<>(); // To avoid duplicate tasks
                        List<Object> results = task.getResult();

                        for (Object result : results) {
                            if (result instanceof QuerySnapshot) {
                                QuerySnapshot querySnapshot = (QuerySnapshot) result;

                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    String taskID = document.getId();

                                    // Avoid duplicates
                                    if (!processedTaskIds.contains(taskID)) {
                                        processedTaskIds.add(taskID);

                                        String courseName = document.getString("courseName");
                                        String taskType = document.getString("taskType");
                                        String dueDate = document.getString("dateDue");
                                        String startDate = document.getString("dateStart");
                                        String dueTime = document.getString("alarmTime");
                                        String priorityMode = document.getString("priorityMode");
                                        String UID = document.getString("uid");

                                        try {
                                            Date dateStart = dateFormat.parse(startDate);
                                            Date dateDue = dateFormat.parse(dueDate);

                                            // Create task data and add it to the list
                                            PendingTaskList taskData = new PendingTaskList(priorityMode, courseName, dueDate, taskType, dueTime, UID, taskID, dateDue);
                                            taskData.setDateDue(dateDue); // Add dateDue as a Date in PendingTaskList
                                            pendingTaskLists.add(taskData);
                                            taskCount++;
                                        } catch (ParseException e) {
                                            Log.e("HomeFragment", "Date parsing error: ", e);
                                        }
                                    }
                                }
                            }
                        }

                        // Sort tasks by due date (earliest due date at the top)
                        pendingTaskLists.sort((task1, task2) -> task1.getDateDue().compareTo(task2.getDateDue()));

                        pendingTaskListAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("HomeFragment", "Error combining tasks: ", task.getException());
                    }
                });
    }



}