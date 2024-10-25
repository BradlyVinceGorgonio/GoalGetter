package com.example.goalgetter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment {

    private DatabaseReference usersRef;
    private List<String> selectedUsers;
    private FirebaseAuth auth;
    private List<String> userEmails;
    private boolean[] checkedUsers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        selectedUsers = new ArrayList<>();
        userEmails = new ArrayList<>();

        ImageButton groupCreateButton = view.findViewById(R.id.group_create);
        groupCreateButton.setOnClickListener(v -> showUserSelectionDialog());

        return view;
    }

    private void showUserSelectionDialog() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userEmails.clear();
                checkedUsers = new boolean[(int) dataSnapshot.getChildrenCount()];

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String email = userSnapshot.child("email").getValue(String.class);
                    if (email != null) {
                        userEmails.add(email);
                        Log.d("GroupFragment", "Fetched email: " + email);
                    }
                }

                Log.d("GroupFragment", "Fetched user emails: " + userEmails);

                if (userEmails.isEmpty()) {
                    Toast.makeText(getContext(), "No users found.", Toast.LENGTH_SHORT).show();
                } else {
                    showDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("GroupFragment", "Error loading users: " + databaseError.getMessage());
                Toast.makeText(getContext(), "Failed to load users.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Users")
                .setMultiChoiceItems(userEmails.toArray(new String[0]), checkedUsers, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        selectedUsers.add(userEmails.get(which));
                    } else {
                        selectedUsers.remove(userEmails.get(which));
                    }
                })
                .setPositiveButton("Create", (dialog, id) -> createChatGroup())
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss())
                .show();
    }

    private void createChatGroup() {
        if (selectedUsers.isEmpty()) {
            Toast.makeText(getContext(), "No users selected", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getActivity(), ChatGroupActivity.class);
        intent.putStringArrayListExtra("selectedUsers", (ArrayList<String>) selectedUsers);
        startActivity(intent);
    }
}
