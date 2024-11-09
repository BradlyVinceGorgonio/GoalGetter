package com.example.goalgetter;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;import java.util.Calendar;

public class HomeFragment extends Fragment {


    private RecyclerView recyclerView;
    private PendingTaskListAdapter pendingTaskListAdapter;
    private List<PendingTaskList> pendingTaskLists;
    private FirebaseFirestore db;
    private AutoCompleteTextView pendingTasksTextView; // Add this reference for the AutoCompleteTextView

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
        pendingTasksTextView = view.findViewById(R.id.pendingTasksTextView);  // Initialize AutoCompleteTextView

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pendingTaskListAdapter = new PendingTaskListAdapter(pendingTaskLists, getContext());
        recyclerView.setAdapter(pendingTaskListAdapter);

        //Display Month Quotes
        displayMonthlyQuote(view);
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
                        int taskCount = 0; // Variable to count the tasks

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

                            // Increment the task count
                            taskCount++;
                        }

                        // Update the AutoCompleteTextView with the task count
                        String taskMessage = "You have a total of " + taskCount + " pending tasks";
                        pendingTasksTextView.setText(taskMessage);

                        // Notify the adapter that the data has changed
                        pendingTaskListAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("HomeFragment", "Error getting tasks: ", task.getException());
                    }
                });
    }
    public void displayMonthlyQuote(View view) {
        // Get current month (0 = January, 11 = December)
        int month = Calendar.getInstance().get(Calendar.MONTH);

        // Initialize the views
        TextView monthTextView = view.findViewById(R.id.monthTextView);
        TextView quoteTextView = view.findViewById(R.id.quoteTextView);



        // Set the month name and quote based on the current month
        String monthName = "";
        String quote = "";

        switch (month) {
            case 0: // January
                monthName = "Month of January: Hope";
                quote = "“Hope is the pillar that holds up the world.”";
                break;
            case 1: // February
                monthName = "Month of February: Love";
                quote = "“Love is the bridge between you and everything.”";
                break;
            case 2: // March
                monthName = "Month of March: Commitment";
                quote = "“Commitment unlocks the doors of imagination.”";
                break;
            case 3: // April
                monthName = "Month of April: Cooperation";
                quote = "“Cooperation is the thorough conviction that nobody can get there unless everybody gets there.”";
                break;
            case 4: // May
                monthName = "Month of May: Ambition";
                quote = "“Ambition is the first step to success.”";
                break;
            case 5: // June
                monthName = "Month of June: Friendliness";
                quote = "“A warm smile is the universal language of kindness.”";
                break;
            case 6: // July
                monthName = "Month of July: Self-discipline";
                quote = "“Self-discipline is the magic power that makes you virtually unstoppable.”";
                break;
            case 7: // August
                monthName = "Month of August: Respect";
                quote = "“Respect is earned. Honesty is appreciated. Trust is gained. Loyalty is returned.”";
                break;
            case 8: // September
                monthName = "Month of September: Aspiration";
                quote = "“Aspire to inspire before we expire.”";
                break;
            case 9: // October
                monthName = "Month of October: Faith and Humility";
                quote = "“Just as love is a verb, so is faith.”";
                break;
            case 10: // November
                monthName = "Month of November: Forgiveness";
                quote = "“Forgiveness is the fragrance that the violet sheds on the heel that has crushed it.”";
                break;
            case 11: // December
                monthName = "Month of December: Charity and Service";
                quote = "“Service to others is the rent you pay for your room here on earth.”";
                break;
        }

        // Set the text for the TextViews
        monthTextView.setText(monthName);
        quoteTextView.setText(quote);
    }
}
