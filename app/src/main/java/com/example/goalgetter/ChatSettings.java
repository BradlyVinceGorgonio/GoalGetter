package com.example.goalgetter;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class ChatSettings extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private String chatRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_settings);

        firestore = FirebaseFirestore.getInstance();

        TextView groupNameTextView = findViewById(R.id.group_name_text_view);
        ImageButton backButton = findViewById(R.id.back_button);
        TextView leaveGroupTextView = findViewById(R.id.leavegc);

        String groupName = getIntent().getStringExtra("groupName");
        chatRoomId = getIntent().getStringExtra("groupChatId");

        if (groupName != null) {
            groupNameTextView.setText(groupName);
        }

        backButton.setOnClickListener(v -> finish());

        leaveGroupTextView.setOnClickListener(v -> confirmGroupDeletion());

        ImageView sharedTaskStatusBtn = findViewById(R.id.sharedTaskStatusBtn);
        sharedTaskStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatSettings.this, SharedTaskStatus.class);
                String groupName = getIntent().getStringExtra("groupName");
                String chatRoomId = getIntent().getStringExtra("groupChatId");
                intent.putExtra("groupChatId", chatRoomId);
                intent.putExtra("groupName", groupName);
                startActivity(intent);
            }
        });
    }

    private void confirmGroupDeletion() {
        new AlertDialog.Builder(this)
                .setTitle("Leave Group")
                .setMessage("Are you sure you want to leave and delete this group chat?")
                .setPositiveButton("Yes", (dialog, which) -> deleteGroupChat())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteGroupChat() {
        if (chatRoomId != null) {
            firestore.collection("chatGroups").document(chatRoomId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ChatSettings.this, "Group chat deleted successfully.", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ChatSettings.this, "Failed to delete group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Group ID is missing. Unable to delete group.", Toast.LENGTH_SHORT).show();
        }
    }
}
