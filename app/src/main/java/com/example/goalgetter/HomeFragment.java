package com.example.goalgetter;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private PendingTaskListAdapter pendingTaskListAdapter;
    private List<PendingTaskList> pendingTaskLists;
    private FirebaseFirestore db;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Initialize RecyclerView and taskList
        recyclerView = view.findViewById(R.id.pendingTaskrecyclerView);
        pendingTaskLists = new ArrayList<>();

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pendingTaskListAdapter = new PendingTaskListAdapter(pendingTaskLists,getContext());
        recyclerView.setAdapter(pendingTaskListAdapter);

        // Fetch tasks from Firestore
        fetchTasksFromFirestore();

        return view;
    }

    private void fetchTasksFromFirestore() {
        // Get the current authenticated user's UID
        String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("allTasks")
                .whereEqualTo("uid", currentUserUID)  // Filter tasks by the current user's UID
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Clear the previous task list to avoid duplicate entries
                        pendingTaskLists.clear();

                        // Iterate through each document in the Firestore collection
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Extract the necessary fields from the document
                            String courseName = document.getString("courseName");
                            String taskType = document.getString("taskType");
                            String dueDate = document.getString("dateDue");
                            String dueTime = document.getString("alarmTime");
                            String priorityMode = document.getString("priorityMode");
                            String UID = document.getString("uid");
                            String taskID = document.getId();  // Firestore document ID

                            // Create a new PendingTaskList object
                            PendingTaskList taskData = new PendingTaskList(priorityMode, courseName, dueDate, taskType, dueTime, UID, taskID);

                            // Add the new task object to the task list
                            pendingTaskLists.add(taskData);
                        }

                        // Notify the adapter that the data has changed
                        pendingTaskListAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("HomeFragment", "Error getting tasks: ", task.getException());
                    }
                });
    }

}