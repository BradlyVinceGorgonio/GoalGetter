package com.example.goalgetter;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.CalendarView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class CalendarFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        calendarView = view.findViewById(R.id.calendarView);
        recyclerView = view.findViewById(R.id.recyclerView);
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(taskAdapter);
        loadTasks();

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            taskAdapter.filterTasksByDate(dayOfMonth, month + 1, year);
        });

        FloatingActionButton floatingActionButton = view.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateTask.class);
            startActivity(intent);
        });

        return view;
    }

    private void loadTasks() {
        String currentUid = auth.getCurrentUser().getUid();
        db.collection("students")
                .document(currentUid)
                .collection("solotask")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task> newTasks = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Task taskObj = document.toObject(Task.class);
                            newTasks.add(taskObj);
                        }
                        taskList.clear();
                        taskList.addAll(newTasks);
                        taskAdapter.notifyDataSetChanged();
                    }
                });
    }
}
