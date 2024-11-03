package com.example.goalgetter;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.goalgetter.R;

public class ChatSettings extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_settings);


        TextView groupNameTextView = findViewById(R.id.group_name_text_view);
        ImageButton backButton = findViewById(R.id.back_button);

        String groupName = getIntent().getStringExtra("groupName");
        if (groupName != null) {
            groupNameTextView.setText(groupName);
        } else {
        }

        backButton.setOnClickListener(v -> finish());
    }

}
