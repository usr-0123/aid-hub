package com.example.aidhub.users;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.aidhub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private TextView firstNameTextView, emailTextView, phoneTextView, membershipTextView, userTypeTextView;
    private ImageView profileImageView;
    private Button editBtn;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        // Initialize views
        firstNameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);
        userTypeTextView = view.findViewById(R.id.userTypeTextView);
        membershipTextView = view.findViewById(R.id.createdAtTextView);
        profileImageView = view.findViewById(R.id.profileImageView);
        editBtn = view.findViewById(R.id.edit_details_btn);

        editBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        // Fetch user data from Firebase
        fetchUserProfile();

        return view;
    }

    private void fetchUserProfile() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel user = snapshot.getValue(UserModel.class);

                    if (user != null) {
                        // Display user information
                        firstNameTextView.setText(user.getFirstName() + "" + user.getLastName());
                        emailTextView.setText(user.getEmail());
                        phoneTextView.setText(user.getPhone());
                        membershipTextView.setText(formatDate(user.getCreatedAt()));
                        userTypeTextView.setText(user.getUserType());

                        // Load profile image using Glide
                        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                            Glide.with(requireContext()).load(user.getProfileImage()).into(profileImageView);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }

    public String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
