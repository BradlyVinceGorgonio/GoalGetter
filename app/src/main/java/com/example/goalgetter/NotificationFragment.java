package com.example.goalgetter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView notificationRecyclerView;
    private NotificationAdapter notificationAdapter;
    private List<GroupNotification> notificationList;
    private DatabaseReference messagesRef;
    private FirebaseFirestore firestore;
    private String currentUserId;
    private TextView groupNameTextView;
    private ListenerRegistration groupNameListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        notificationRecyclerView = view.findViewById(R.id.notification_recycler_view);
        groupNameTextView = view.findViewById(R.id.group_name);

        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(getContext(), notificationList, notification -> {
            Toast.makeText(getContext(), "Notification clicked: " + notification.getMessageText(), Toast.LENGTH_SHORT).show();
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        notificationRecyclerView.setLayoutManager(layoutManager);
        notificationRecyclerView.setAdapter(notificationAdapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");

        firestore = FirebaseFirestore.getInstance();

        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                    String groupId = groupSnapshot.getKey();
                    fetchGroupNameFromFirestore(groupId);

                    for (DataSnapshot messageSnapshot : groupSnapshot.getChildren()) {
                        Message message = messageSnapshot.getValue(Message.class);
                        if (message != null && !message.getSenderId().equals(currentUserId)) {
                            GroupNotification notification = new GroupNotification(
                                    groupId,
                                    "Group Name Placeholder",
                                    message.getMessageId(),
                                    message.getMessageText(),
                                    message.getSenderName(),
                                    message.getTimestamp()
                            );
                            notificationList.add(notification);
                        }
                    }
                }
                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading notifications: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchGroupNameFromFirestore(String groupId) {
        DocumentReference groupRef = firestore.collection("chatGroups").document(groupId);
        groupNameListener = groupRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Toast.makeText(getContext(), "Error getting group name: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                String groupName = documentSnapshot.getString("groupName");
                if (groupName != null && groupNameTextView != null) {
                    groupNameTextView.setText(groupName);
                }
            } else {
                Toast.makeText(getContext(), "Group not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (groupNameListener != null) {
            groupNameListener.remove();
        }
    }
}
