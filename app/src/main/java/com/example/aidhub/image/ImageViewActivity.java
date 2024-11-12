package com.example.aidhub.image;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.aidhub.R;

public class ImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_view);

        ImageView imageView = findViewById(R.id.fullScreenImageView);

        String imageUri = getIntent().getStringExtra("imageUri");

        if (imageUri != null) {
            // Load the image using Glide
            Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.ic_menu_gallery)
                .error(R.drawable.ic_menu_gallery)
                .into(imageView);
        }
    }
}