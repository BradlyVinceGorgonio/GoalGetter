package com.example.goalgetter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.GroupChatViewHolder> {

    private final Context context;
    private final List<GroupChat> groupChats;
    private final OnGroupChatClickListener listener;

    public interface OnGroupChatClickListener {
        void onGroupChatClick(GroupChat groupChat);
    }

    public GroupChatAdapter(Context context, List<GroupChat> groupChats, OnGroupChatClickListener listener) {
        this.context = context;
        this.groupChats = groupChats;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_chat, parent, false);
        return new GroupChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupChatViewHolder holder, int position) {
        GroupChat groupChat = groupChats.get(position);
        holder.bind(groupChat, listener);
    }

    @Override
    public int getItemCount() {
        return groupChats.size();
    }

    public static class GroupChatViewHolder extends RecyclerView.ViewHolder {
        private final ImageView profileImage;
        private final TextView groupNameTextView;
        private final TextView latestMessageTextView;
        private final TextView timestampTextView;

        public GroupChatViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            groupNameTextView = itemView.findViewById(R.id.group_name_text_view);
            latestMessageTextView = itemView.findViewById(R.id.latest_message_text_view);
            timestampTextView = itemView.findViewById(R.id.timestamp_text_view);
        }

        public void bind(GroupChat groupChat, OnGroupChatClickListener listener) {
            groupNameTextView.setText(groupChat.getGroupName());

            // Query the latest message from Realtime Database
            Query latestMessageQuery = FirebaseDatabase.getInstance().getReference("messages")
                    .child(groupChat.getGroupId())
                    .orderByChild("timestamp")
                    .limitToLast(1);

            latestMessageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                            String senderId = messageSnapshot.child("senderId").getValue(String.class);
                            String messageText = messageSnapshot.child("messageText").getValue(String.class);
                            Long timestamp = messageSnapshot.child("timestamp").getValue(Long.class);

                            // Fetch sender's name from Firestore
                            fetchSenderName(senderId, senderName -> {
                                String formattedTimestamp = formatTimestamp(timestamp);
                                latestMessageTextView.setText(String.format("%s: %s", senderName, messageText));
                                timestampTextView.setText(formattedTimestamp);
                            });
                        }
                    } else {
                        latestMessageTextView.setText("No messages yet");
                        timestampTextView.setText("");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    latestMessageTextView.setText("Error loading message");
                    timestampTextView.setText("");
                }
            });

            itemView.setOnClickListener(v -> listener.onGroupChatClick(groupChat));
        }

        private void fetchSenderName(String senderId, OnNameFetchedCallback callback) {
            if (senderId == null || senderId.isEmpty()) {
                callback.onNameFetched("Unknown User");
                return;
            }

            FirebaseFirestore.getInstance().collection("students")
                    .document(senderId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            String name = document.getString("name");
                            callback.onNameFetched(name != null && !name.isEmpty() ? name : "Unknown User");
                        } else {
                            callback.onNameFetched("Unknown User");
                        }
                    });
        }

        private String formatTimestamp(Long timestamp) {
            if (timestamp == null) return "";
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a MMM. dd, yyyy", Locale.getDefault());
            return sdf.format(date);
        }

        interface OnNameFetchedCallback {
            void onNameFetched(String name);
        }
    }
}
