package com.example.goalgetter;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;

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
import java.util.HashMap;
import java.util.List;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;

import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;

import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.Map;

public class CalendarFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private PendingTaskListAdapter pendingTaskListAdapter;
    private List<PendingTaskList> pendingTaskLists;
    private List<EventDay> eventDays; // List to store events
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
        eventDays = new ArrayList<>(); // Initialize event list
        pendingTaskListAdapter = new PendingTaskListAdapter(pendingTaskLists, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(pendingTaskListAdapter);

        // Fetch and highlight tasks
        fetchAndHighlightTasks();

        // Automatically fetch tasks for the current day
        Calendar today = Calendar.getInstance();
        String currentDate = dateFormat.format(today.getTime());
        fetchTasksFromFirestore(currentDate); // Fetch tasks for the current day

        // Filter tasks when a date is selected
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clickedDay = eventDay.getCalendar();
            String selectedDate = dateFormat.format(clickedDay.getTime());
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

    private void fetchAndHighlightTasks() {
        String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("allTasks")
                .whereArrayContains("uids", currentUserUID)
                .whereEqualTo("isCompleted", false) // Filter for incomplete tasks
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventDays.clear(); // Clear previous events

                        // Group tasks by due date and check for priorityMode 'Yes' in one loop
                        Map<String, Boolean> priorityMap = new HashMap<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String dueDate = document.getString("dateDue");
                            String priorityMode = document.getString("priorityMode");

                            // Track if any task on this date has priorityMode 'Yes'
                            priorityMap.put(dueDate, "Yes".equals(priorityMode) || priorityMap.getOrDefault(dueDate, false));
                        }

                        // Process each unique due date
                        for (Map.Entry<String, Boolean> entry : priorityMap.entrySet()) {
                            String dueDate = entry.getKey();
                            boolean isPriorityDay = entry.getValue();

                            try {
                                // Parse the due date to a calendar
                                Date dateDue = dateFormat.parse(dueDate);
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(dateDue);

                                // Set the drawable based on priority status
                                int drawableRes = isPriorityDay ? R.drawable.ic_event_day : R.drawable.ic_event_black;
                                Drawable drawable = ContextCompat.getDrawable(getContext(), drawableRes);

                                eventDays.add(new EventDay(calendar, drawable)); // Add the event with the appropriate drawable
                            } catch (ParseException e) {
                                Toast.makeText(getActivity(), "Date parsing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        // Update the calendar with highlighted events
                        calendarView.setEvents(eventDays);
                    } else {
                        Toast.makeText(getActivity(), "Error fetching tasks: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
