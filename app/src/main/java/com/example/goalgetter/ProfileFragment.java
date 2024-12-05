package com.example.goalgetter;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import com.example.goalgetter.R;

public class ProfileFragment extends Fragment {

    private TextView fullNameTextView, campusNameTextView, yearLevelTextView, sectionTextView, programTextView, emailTextView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize TextViews
        fullNameTextView = view.findViewById(R.id.fullName);
        campusNameTextView = view.findViewById(R.id.campusName);
        yearLevelTextView = view.findViewById(R.id.yearLevel);
        sectionTextView = view.findViewById(R.id.section);
        programTextView = view.findViewById(R.id.program);
        emailTextView = view.findViewById(R.id.email);

        // Fetch user data from Firestore
        fetchUserData();
    }

    private void fetchUserData() {
        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("students").document(userId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Set the data to the UI
                    fullNameTextView.setText(document.getString("name"));
                    campusNameTextView.setText(document.getString("campus"));
                    yearLevelTextView.setText(document.getString("yearlevel"));
                    sectionTextView.setText(document.getString("section"));
                    programTextView.setText(document.getString("program"));
                    emailTextView.setText(document.getString("email"));
                }
            }
        });
    }
}
