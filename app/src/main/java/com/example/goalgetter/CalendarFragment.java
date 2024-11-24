package com.example.goalgetter;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CalendarFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private PendingTaskListAdapter pendingTaskListAdapter;
    private List<PendingTaskList> pendingTaskLists;
    private SimpleDateFormat dateFormat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        calendarView = view.findViewById(R.id.calendarView);
        recyclerView = view.findViewById(R.id.recyclerView);
        dateFormat = new SimpleDateFormat("dd-MM-yyyy"); // Date format for filtering
        pendingTaskLists = new ArrayList<>();
        pendingTaskListAdapter = new PendingTaskListAdapter(pendingTaskLists, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(pendingTaskListAdapter);

        // Fetch tasks for the current day on fragment opening
        String currentDate = dateFormat.format(new Date());
        fetchTasksFromFirestore(currentDate);

        // Filter tasks when a date is selected
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String selectedDate = String.format("%02d-%02d-%d", dayOfMonth, month + 1, year);
            fetchTasksFromFirestore(selectedDate);
        });

        // Add new task
        FloatingActionButton floatingActionButton = view.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateTask.class);
            startActivity(intent);
        });

        return view;
    }

    private void fetchTasksFromFirestore(String filterDate) {
        String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("allTasks")
                .whereArrayContains("uids", currentUserUID)
                .whereEqualTo("isCompleted", false) // Filter for incomplete tasks
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        pendingTaskLists.clear();
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
                                Date dateStart = dateFormat.parse(startDate);
                                Date dateDue = dateFormat.parse(dueDate);

                                // Filter tasks based on the filter date
                                if (filterDate != null && dueDate.equals(filterDate)) {
                                    PendingTaskList taskData = new PendingTaskList(priorityMode, courseName, dueDate, taskType, dueTime, UID, taskID, dateDue);
                                    taskData.setDateDue(dateDue); // Add dateDue as a Date in PendingTaskList
                                    pendingTaskLists.add(taskData);
                                }
                            } catch (ParseException e) {
                                Toast.makeText(getActivity(), "Date parsing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        // Sort tasks by due date
                        pendingTaskLists.sort((task1, task2) -> task1.getDateDue().compareTo(task2.getDateDue()));
                        pendingTaskListAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getActivity(), "Error fetching tasks: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
