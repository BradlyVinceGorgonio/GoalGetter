package com.example.goalgetter;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TodoFragment extends Fragment {
    private RecyclerView recyclerView;
    private PendingTaskListAdapter pendingTaskListAdapter;
    private List<PendingTaskList> pendingTaskLists;
    private FirebaseFirestore db;
    private AutoCompleteTextView filterAutoCompleteTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_todo, container, false);

        // Initialize Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Initialize RecyclerView and taskList
        recyclerView = view.findViewById(R.id.itemlist2recyclerView);
        pendingTaskLists = new ArrayList<>();


        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pendingTaskListAdapter = new PendingTaskListAdapter(pendingTaskLists, getContext());
        recyclerView.setAdapter(pendingTaskListAdapter);

        filterAutoCompleteTextView = view.findViewById(R.id.filtertodolist);

        // Set up AutoCompleteTextView with filter options
        String[] filters = {"Pending Tasks", "Completed Tasks", "Group Tasks"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, filters);
        filterAutoCompleteTextView.setAdapter(adapter);

        // Set item click listener for the filter
        filterAutoCompleteTextView.setOnItemClickListener((parentView, selectedItemView, position, id) -> {
            String selectedFilter = (String) parentView.getItemAtPosition(position);
            updateRecyclerView(selectedFilter); // Call the method to update RecyclerView
        });

        // Initial fetch of all tasks
        updateRecyclerView("Pending Tasks");

        updateProgressBar(view);


        return view;
    }
    // Method to update RecyclerView based on the selected filter
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
            case "Group Tasks":
                GroupTasks();
                break;
        }
    }
    private void GroupTasks() {
        String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy"); // Match your date format

        db.collection("allTasks")
                .whereArrayContains("uids", currentUserUID)
                .whereEqualTo("isCompleted", false) // Filter for incomplete tasks
                .whereEqualTo("isGroup", true) // Filter for group tasks
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        pendingTaskLists.clear();
                        int taskCount = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String courseName = document.getString("courseName");
                            String taskType = document.getString("taskType");
                            String dueDate = document.getString("dateDue");
                            String startDate = document.getString("dateStart");
                            String dueTime = document.getString("alarmTime");
                            String priorityMode = document.getString("priorityMode");
                            String UID = document.getString("uid");
                            boolean isGroup = document.getBoolean("isGroup");
                            String taskID = document.getId();

                            try {
                                Date dateStart = dateFormat.parse(startDate);
                                Date dateDue = dateFormat.parse(dueDate);

                                // Add task to the list without filtering by start date
                                PendingTaskList taskData = new PendingTaskList(priorityMode, courseName, dueDate, taskType, dueTime, UID, taskID, dateDue, isGroup);
                                taskData.setDateDue(dateDue); // Add dateDue as a Date in PendingTaskList
                                pendingTaskLists.add(taskData);
                                taskCount++;
                            } catch (ParseException e) {
                                Log.e("HomeFragment", "Date parsing error: ", e);
                            }
                        }

                        // Sort tasks by due date (earliest due date at the top)
                        pendingTaskLists.sort((task1, task2) -> task1.getDateDue().compareTo(task2.getDateDue()));

                        pendingTaskListAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("HomeFragment", "Error getting tasks: ", task.getException());
                    }
                });
    }

    private void PendingTasks() {
        String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy"); // Match your date format

        db.collection("allTasks")
                .whereArrayContains("uids", currentUserUID)
                .whereEqualTo("isCompleted", false) // Filter for incomplete tasks
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        pendingTaskLists.clear();
                        int taskCount = 0;

                        // Get today's date
                        Date currentDate = new Date();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String courseName = document.getString("courseName");
                            String taskType = document.getString("taskType");
                            String dueDate = document.getString("dateDue");
                            String startDate = document.getString("dateStart");
                            String dueTime = document.getString("alarmTime");
                            String priorityMode = document.getString("priorityMode");
                            String UID = document.getString("uid");
                            boolean isGroup = document.getBoolean("isGroup");
                            String taskID = document.getId();

                            try {
                                Date dateStart = dateFormat.parse(startDate);
                                Date dateDue = dateFormat.parse(dueDate);

                                // Check if current date is on or after the start date
                                if (currentDate.compareTo(dateStart) >= 0) {
                                    PendingTaskList taskData = new PendingTaskList(priorityMode, courseName, dueDate, taskType, dueTime, UID, taskID, dateDue, isGroup);
                                    taskData.setDateDue(dateDue); // Add dateDue as a Date in PendingTaskList
                                    pendingTaskLists.add(taskData);
                                    taskCount++;
                                }
                            } catch (ParseException e) {
                                Log.e("HomeFragment", "Date parsing error: ", e);
                            }
                        }

                        // Sort tasks by due date (earliest due date at the top)
                        pendingTaskLists.sort((task1, task2) -> task1.getDateDue().compareTo(task2.getDateDue()));

                        pendingTaskListAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("HomeFragment", "Error getting tasks: ", task.getException());
                    }
                });
    }

    /*
    private void AllTasks() {
        String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy"); // Match your date format

        db.collection("allTasks")
                .whereEqualTo("uid", currentUserUID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        pendingTaskLists.clear();
                        int taskCount = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String courseName = document.getString("courseName");
                            String taskType = document.getString("taskType");
                            String dueDate = document.getString("dateDue");
                            String startDate = document.getString("dateStart");
                            String dueTime = document.getString("alarmTime");
                            String priorityMode = document.getString("priorityMode");
                            String UID = document.getString("uid");
                            String taskID = document.getId();

                            try {
                                // Removed the date filter logic
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

                        // Sort tasks by due date (earliest due date at the top)
                        pendingTaskLists.sort((task1, task2) -> task1.getDateDue().compareTo(task2.getDateDue()));

                        pendingTaskListAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("HomeFragment", "Error getting tasks: ", task.getException());
                    }
                });
    }

     */

    private void CompletedTasks() {
        String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy"); // Match your date format

        db.collection("allTasks")
                .whereArrayContains("uids", currentUserUID)
                .whereEqualTo("isCompleted", true) // Filter for completed tasks
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        pendingTaskLists.clear();
                        int taskCount = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String courseName = document.getString("courseName");
                            String taskType = document.getString("taskType");
                            String dueDate = document.getString("dateDue");
                            String startDate = document.getString("dateStart");
                            String dueTime = document.getString("alarmTime");
                            String priorityMode = document.getString("priorityMode");
                            String UID = document.getString("uid");
                            boolean isGroup = document.getBoolean("isGroup");
                            String taskID = document.getId();

                            try {
                                Date dateStart = dateFormat.parse(startDate);
                                Date dateDue = dateFormat.parse(dueDate);

                                // Create task data and add it to the list
                                PendingTaskList taskData = new PendingTaskList(priorityMode, courseName, dueDate, taskType, dueTime, UID, taskID, dateDue, isGroup);
                                taskData.setDateDue(dateDue); // Add dateDue as a Date in PendingTaskList
                                pendingTaskLists.add(taskData);
                                taskCount++;
                            } catch (ParseException e) {
                                Log.e("HomeFragment", "Date parsing error: ", e);
                            }
                        }

                        // Sort tasks by due date (earliest due date at the top)
                        pendingTaskLists.sort((task1, task2) -> task1.getDateDue().compareTo(task2.getDateDue()));

                        pendingTaskListAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("HomeFragment", "Error getting tasks: ", task.getException());
                    }
                });
    }

    private void updateProgressBar(View view) {
        String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Retrieve all tasks for the user
        db.collection("allTasks")
                .whereArrayContains("uids", currentUserUID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int totalTasks = 0;
                        int completedTasks = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            totalTasks++; // Count total tasks
                            Boolean isCompleted = document.getBoolean("isCompleted");
                            if (isCompleted != null && isCompleted) {
                                completedTasks++; // Count completed tasks
                            }
                        }

                        // Calculate progress percentage
                        int progress = (totalTasks > 0) ? (completedTasks * 100 / totalTasks) : 0;

                        // Update ProgressBar
                        ProgressBar progressBar = view.findViewById(R.id.progressBar);
                        progressBar.setProgress(progress);

                        // Update Indicator TextView
                        TextView indicatorTextView = view.findViewById(R.id.indicatorTextView);
                        indicatorTextView.setText(progress + "%");

                        // Adjust the position of the indicator dynamically
                        progressBar.post(() -> {
                            int progressBarWidth = progressBar.getWidth();
                            int indicatorWidth = indicatorTextView.getWidth();

                            // Convert the 20dp margin to pixels based on screen density
                            float density = view.getContext().getResources().getDisplayMetrics().density;
                            int marginInPixels = (int) (0 * density); // 20dp converted to pixels

                            int indicatorPosition = (int) ((progress / 100.0) * (progressBarWidth - (2 * marginInPixels)));

                            // Clamp position to avoid overflow
                            int clampedPosition = Math.min(
                                    progressBarWidth - indicatorWidth / 2 - marginInPixels,
                                    Math.max(indicatorWidth / 2 + marginInPixels, indicatorPosition)
                            );

                            // Adjust the layout parameters
                            ViewGroup.LayoutParams layoutParams = indicatorTextView.getLayoutParams();
                            if (layoutParams instanceof RelativeLayout.LayoutParams) {
                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layoutParams;
                                params.leftMargin = clampedPosition - (indicatorWidth / 2);
                                indicatorTextView.setLayoutParams(params);
                            } else {
                                Log.w("ProgressBar", "Unsupported layout type. Unable to adjust indicator position.");
                            }
                        });

                        Log.d("ProgressBar", "Progress updated to " + progress + "%");
                    } else {
                        Log.d("HomeFragment", "Error getting tasks: ", task.getException());
                    }
                });
    }






}