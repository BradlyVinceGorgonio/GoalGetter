package com.example.goalgetter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatGroupActivity extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private EditText messageInput;
    private ImageButton sendButton;
    private DatabaseReference messagesRef;
    private FirebaseAuth auth;
    private String currentUserId;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_group);

        messagesRecyclerView = findViewById(R.id.messages_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        retrieveCurrentUserName();

        sendButton.setOnClickListener(v -> sendMessage());
        loadMessages();
    }

    private void retrieveCurrentUserName() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").getValue(String.class);
                } else {
                    Toast.makeText(ChatGroupActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatGroupActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty() && currentUserName != null) {
            String messageId = messagesRef.push().getKey();
            long timestamp = System.currentTimeMillis();

            Message message = new Message(currentUserId, currentUserName, "", messageText, timestamp);

            messagesRef.child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> {

                        messageInput.setText("");
                    })
                    .addOnFailureListener(e -> {

                        Toast.makeText(ChatGroupActivity.this, "Error sending message", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMessages() {
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear(); // Clear the list to avoid duplicates
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }
                messageAdapter.notifyDataSetChanged(); // Notify the adapter to update the RecyclerView
                // Scroll to the bottom of the list
                messagesRecyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatGroupActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
