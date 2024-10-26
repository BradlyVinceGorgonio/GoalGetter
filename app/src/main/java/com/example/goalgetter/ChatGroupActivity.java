package com.example.goalgetter;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatGroupActivity extends AppCompatActivity {

    private static final String TAG = "ChatGroupActivity";
    private RecyclerView messagesRecyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private EditText messageInput;
    private ImageButton sendButton;
    private DatabaseReference messagesRef;
    private FirebaseAuth auth;
    private String currentUserId;
    private String currentUserName;
    private ImageButton backButton; // Add this line


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_group);

        messagesRecyclerView = findViewById(R.id.messages_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(v -> {
            finish();
        });

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not logged in.");
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = currentUser.getUid();
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
                    Log.d(TAG, "Current user name: " + currentUserName);
                } else {
                    Log.e(TAG, "User data not found for ID: " + currentUserId);
                    Toast.makeText(ChatGroupActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching user data: " + databaseError.getMessage());
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
                        Log.d(TAG, "Message sent successfully");
                        messageInput.setText("");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error sending message: " + e.getMessage());
                        Toast.makeText(ChatGroupActivity.this, "Error sending message", Toast.LENGTH_SHORT).show();
                    });
        } else if (currentUserName == null) {
            Log.e(TAG, "Current user name is null");
            Toast.makeText(this, "User name not loaded yet", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMessages() {
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                messagesRecyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading messages: " + databaseError.getMessage());
                Toast.makeText(ChatGroupActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
