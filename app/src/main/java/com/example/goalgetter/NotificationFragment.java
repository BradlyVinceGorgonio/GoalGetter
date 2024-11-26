package com.example.goalgetter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotificationFragment extends Fragment {

    private RecyclerView notificationRecyclerView;
    private NotificationAdapter notificationAdapter;
    private List<GroupNotification> notificationList;
    private Map<String, GroupNotification> notificationMap;
    private DatabaseReference messagesRef;
    private FirebaseFirestore firestore;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        notificationRecyclerView = view.findViewById(R.id.notification_recycler_view);

        notificationList = new ArrayList<>();
        notificationMap = new HashMap<>();
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
                notificationMap.clear();
                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                    String groupId = groupSnapshot.getKey();
                    fetchGroupNameFromFirestore(groupId);

                    Message latestMessage = null;
                    for (DataSnapshot messageSnapshot : groupSnapshot.getChildren()) {
                        Message message = messageSnapshot.getValue(Message.class);
                        if (message != null && !message.getSenderId().equals(currentUserId)) {
                            if (latestMessage == null || message.getTimestamp() > latestMessage.getTimestamp()) {
                                latestMessage = message;
                            }
                        }
                    }

                    if (latestMessage != null) {
                        GroupNotification notification = new GroupNotification(
                                groupId,
                                "Group Name Placeholder",  // Placeholder, to be updated after fetching group name
                                latestMessage.getMessageId(),
                                latestMessage.getMessageText(),
                                latestMessage.getSenderName(),
                                latestMessage.getTimestamp()
                        );
                        notificationMap.put(groupId, notification);
                    }
                }

                updateNotificationList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading notifications: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchGroupNameFromFirestore(String groupId) {
        DocumentReference groupRef = firestore.collection("chatGroups").document(groupId);
        groupRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String groupName = documentSnapshot.getString("groupName");
                if (groupName != null && notificationMap.containsKey(groupId)) {
                    GroupNotification notification = notificationMap.get(groupId);
                    if (notification != null) {
                        notification.setGroupName(groupName);
                        updateNotificationList();
                    }
                }
            }
        });
    }

    private void updateNotificationList() {
        notificationList.clear();
        notificationList.addAll(notificationMap.values());
        notificationAdapter.notifyDataSetChanged();
    }

    public interface NotificationClickListener {
        void onNotificationClick(GroupNotification notification);
    }

    public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

        private final Context context;
        private final List<GroupNotification> notifications;
        private final NotificationClickListener listener;

        public NotificationAdapter(Context context, List<GroupNotification> notifications, NotificationClickListener listener) {
            this.context = context;
            this.notifications = notifications;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GroupNotification notification = notifications.get(position);

            holder.groupNameTextView.setText(notification.getGroupName());
            holder.messageTextView.setText(notification.getMessageText());
            holder.senderNameTextView.setText(notification.getSenderName());

            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(notification.getTimestamp()));
            holder.timestampTextView.setText(timestamp);

            if (notification.getTimestamp() == getLatestTimestamp(notification.getGroupId())) {
                holder.newMessageIcon.setVisibility(View.VISIBLE);
            } else {
                holder.newMessageIcon.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> listener.onNotificationClick(notification));
        }

        private long getLatestTimestamp(String groupId) {
            long latestTimestamp = 0;
            for (GroupNotification notification : notifications) {
                if (notification.getGroupId().equals(groupId)) {
                    latestTimestamp = Math.max(latestTimestamp, notification.getTimestamp());
                }
            }
            return latestTimestamp;
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView groupNameTextView;
            TextView messageTextView;
            TextView senderNameTextView;
            TextView timestampTextView;
            ImageView newMessageIcon;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                groupNameTextView = itemView.findViewById(R.id.group_name);
                messageTextView = itemView.findViewById(R.id.message_text);
                senderNameTextView = itemView.findViewById(R.id.sender_name);
                timestampTextView = itemView.findViewById(R.id.timestamp);
                newMessageIcon = itemView.findViewById(R.id.new_message_icon);
            }
        }
    }
}
