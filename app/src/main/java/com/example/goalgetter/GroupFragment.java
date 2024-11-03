package com.example.goalgetter;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment {
    private List<User> selectedUsers = new ArrayList<>();
    private FirebaseFirestore firestore;
    private UserAdapter userAdapter;
    private RecyclerView recyclerView;
    private ImageView backgroundImage;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        firestore = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        backgroundImage = view.findViewById(R.id.background_image);
        recyclerView = view.findViewById(R.id.chat_group_recycler_view);

        ImageButton createGroupButton = view.findViewById(R.id.group_create);
        createGroupButton.setOnClickListener(v -> showUserSelectionPopup());

        fetchGroupChats(currentUserId);
        return view;
    }

    private void showUserSelectionPopup() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_select_profiles, null);
        recyclerView = popupView.findViewById(R.id.recycler_view);
        Button selectButton = popupView.findViewById(R.id.select_button);
        EditText groupNameEditText = popupView.findViewById(R.id.group_name_edit_text);

        fetchUsers();

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setView(popupView);
        builder.setCancelable(true);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        selectButton.setOnClickListener(v -> {
            if (selectedUsers.size() < 2) {
                Toast.makeText(getContext(), "Please select at least two users.", Toast.LENGTH_SHORT).show();
            } else {
                String groupName = groupNameEditText.getText().toString().trim();
                if (groupName.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter a group name.", Toast.LENGTH_SHORT).show();
                } else {
                    createGroupWithSelectedUsers(selectedUsers, groupName);
                    dialog.dismiss();
                }
            }
        });
    }

    private void fetchUsers() {
        CollectionReference usersCollection = firestore.collection("students");

        usersCollection.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String uid = document.getId();
                            String email = document.getString("email");
                            String name = document.getString("name");

                            if (!uid.equals(currentUserId)) {
                                users.add(new User(uid, email, name));
                            }
                        }

                        User currentUser = new User(currentUserId, "", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                        selectedUsers.add(currentUser);

                        setupRecyclerView(users);
                    } else {
                        Toast.makeText(getContext(), "Failed to fetch users: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupRecyclerView(List<User> users) {
        userAdapter = new UserAdapter(getContext(), users, new UserAdapter.OnUserSelectedListener() {
            @Override
            public void onUserSelected(User user) {
                selectedUsers.add(user);
            }

            @Override
            public void onUserDeselected(User user) {
                selectedUsers.remove(user);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(userAdapter);
    }

    private void createGroupWithSelectedUsers(List<User> selectedUsers, String groupName) {
        String groupId = firestore.collection("chatGroups").document().getId();
        GroupChat groupChat = new GroupChat(groupId, groupName, selectedUsers, "", 0);

        firestore.collection("chatGroups").document(groupId)
                .set(groupChat)
                .addOnSuccessListener(aVoid -> {
                    Intent intent = new Intent(getContext(), ChatGroupActivity.class);
                    intent.putExtra("groupId", groupId);
                    intent.putExtra("groupName", groupName);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error creating chat group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchGroupChats(String currentUserId) {
        CollectionReference chatGroupsCollection = firestore.collection("chatGroups");

        chatGroupsCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<GroupChat> groupChats = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    GroupChat groupChat = document.toObject(GroupChat.class);
                    for (User user : groupChat.getUsers()) {
                        if (user.getUid().equals(currentUserId)) {
                            groupChats.add(groupChat);
                            break;
                        }
                    }
                }

                if (groupChats.isEmpty()) {
                    backgroundImage.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    backgroundImage.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    setupGroupChatRecyclerView(groupChats);
                }
            } else {
                Toast.makeText(getContext(), "Failed to fetch chat groups: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupGroupChatRecyclerView(List<GroupChat> groupChats) {
        GroupChatAdapter groupChatAdapter = new GroupChatAdapter(getContext(), groupChats, groupChat -> {
            Intent intent = new Intent(getContext(), ChatGroupActivity.class);
            intent.putExtra("groupId", groupChat.getGroupId());
            intent.putExtra("groupName", groupChat.getGroupName());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(groupChatAdapter);
    }
}
