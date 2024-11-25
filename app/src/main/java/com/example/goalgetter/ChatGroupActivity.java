package com.example.goalgetter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.ArrayList;
import java.util.List;

public class ChatGroupActivity extends AppCompatActivity {

    private static final int IMAGE_PICK_CODE = 1000;
    private RecyclerView messagesRecyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private EditText messageInput;
    private ImageButton sendButton, imageButton;
    private DatabaseReference messagesRef;
    private FirebaseAuth auth;
    private String currentUserId;
    private String currentUserName;
    private String chatRoomId;
    private String groupName;
    private TextView groupNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_group);

        messagesRecyclerView = findViewById(R.id.messages_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        imageButton = findViewById(R.id.photo_selector_button);
        groupNameTextView = findViewById(R.id.group_chat_name);

        chatRoomId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");
        groupNameTextView.setText(groupName);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChatGroupActivity.this, ChatSettings.class);
            intent.putExtra("groupChatId", chatRoomId);
            intent.putExtra("groupName", groupName);
            startActivity(intent);
        });

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = currentUser.getUid();
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList, currentUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);

        retrieveCurrentUserName();
        sendButton.setOnClickListener(v -> sendMessage());
        imageButton.setOnClickListener(v -> openGallery());
        loadMessages();

        ImageButton createTaskButton = findViewById(R.id.create_task_button);
        createTaskButton.setVisibility(View.GONE);
        checkLeaderAndUpdateButton(createTaskButton);
        createTaskButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChatGroupActivity.this, LeaderTaskCreation.class);
            intent.putExtra("groupChatId", chatRoomId);
            intent.putExtra("groupChatName", groupName);
            startActivity(intent);
        });
    }

    private void checkLeaderAndUpdateButton(ImageButton createTaskButton) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("chatGroups")
                .document(chatRoomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String leaderId = documentSnapshot.getString("leaderId");
                        if (leaderId != null && leaderId.equals(currentUserId)) {
                            createTaskButton.setVisibility(View.VISIBLE);
                        } else {
                            createTaskButton.setVisibility(View.GONE);
                        }
                    } else {
                        createTaskButton.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> createTaskButton.setVisibility(View.GONE));
    }

    private void retrieveCurrentUserName() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("students")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUserName = documentSnapshot.getString("name");
                    } else {
                        currentUserName = "Unknown User";
                    }
                })
                .addOnFailureListener(e -> {
                    currentUserName = "Unknown User";
                });
    }

    private void sendMessage() {
        String messageContent = messageInput.getText().toString().trim();
        if (!messageContent.isEmpty()) {
            long timestamp = System.currentTimeMillis();
            Message message = new Message(currentUserId, currentUserName, groupName, messageContent, "", timestamp, "text");
            messagesRef.child(chatRoomId).push().setValue(message);
            messageInput.setText("");
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("chat_images");
            StorageReference imageRef = storageRef.child(System.currentTimeMillis() + ".jpg");
            UploadTask uploadTask = imageRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> sendImageMessage(uri.toString()));
            });
        }
    }

    private void sendImageMessage(String imageUrl) {
        long timestamp = System.currentTimeMillis();
        Message message = new Message(currentUserId, currentUserName, groupName, "", imageUrl, timestamp, "image");
        messagesRef.child(chatRoomId).push().setValue(message);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            uploadImageToFirebase(imageUri);
        }
    }

    private void loadMessages() {
        messagesRef.child(chatRoomId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
