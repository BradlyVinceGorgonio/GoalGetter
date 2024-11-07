package com.example.goalgetter;

import android.os.Bundle;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import androidx.appcompat.app.AppCompatActivity;

public class FullImageActivity extends AppCompatActivity {
    ImageView fullImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        fullImageView = findViewById(R.id.full_image_view);
        String imageUrl = getIntent().getStringExtra("image_url");

        Picasso.get().load(imageUrl).into(fullImageView);
    }
}
