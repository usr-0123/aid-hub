package com.example.aidhub.users;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.aidhub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText firstNameEditText, lastNameEditText, emailEditText, phoneEditText;
    private ImageView profileImageView;
    private Button changePhotoButton, saveButton;
    private DatabaseReference databaseReference;
    private StorageReference storageRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        profileImageView = findViewById(R.id.editProfileImageView);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        changePhotoButton = findViewById(R.id.changePhotoButton);
        saveButton = findViewById(R.id.saveButton);

        // Button to save profile changes
        saveButton.setOnClickListener(view -> saveProfileChanges());

        changePhotoButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            Glide.with(this).load(imageUri).placeholder(R.drawable.ic_user_placeholder_foreground).into(profileImageView);
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference attachmentRef = FirebaseStorage.getInstance().getReference()
                .child("profile_images/" + currentUser.getUid() + "/" + imageUri.getLastPathSegment());

        attachmentRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> attachmentRef.getDownloadUrl()
                        .addOnCompleteListener(urlTask -> {
                            if (urlTask.isSuccessful()) {
                                Uri uri = urlTask.getResult();
                                if (uri != null) {
                                    String downloadUrl = uri.toString();
                                    databaseReference.child("profileImage").setValue(downloadUrl);
                                    Toast.makeText(EditProfileActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(EditProfileActivity.this, "Failed to retrieve image URL.", Toast.LENGTH_SHORT).show();
                                Log.e("EditProfileActivity", "Failed to retrieve image URL: " + urlTask.getException());
                            }
                        }))
                .addOnFailureListener(e -> {
                        Toast.makeText(EditProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    Log.e("EditProfileActivity", "Failed to upload image", e);
                });
    }

    private void saveProfileChanges() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update Firebase with the new data
        databaseReference.child("firstName").setValue(firstName);
        databaseReference.child("lastName").setValue(lastName);
        databaseReference.child("email").setValue(email);
        databaseReference.child("phone").setValue(phone);

        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
