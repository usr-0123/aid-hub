package com.example.aidhub.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.aidhub.R;
import com.example.aidhub.auth.LoginActivity;
import com.example.aidhub.users.UserModel;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aidhub.databinding.ActivityAdminBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityAdminBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference usersRef;
    private TextView navUserEmail, nameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();

        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarAdmin.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.adminNavView;
        View headerView = navigationView.getHeaderView(0);

        navUserEmail = headerView.findViewById(R.id.adminEmailTextView);
        nameTextView = headerView.findViewById(R.id.nameTextView);
        ImageView imageView = headerView.findViewById(R.id.imageView);

        if (currentUser != null) {
            navUserEmail.setText(currentUser.getEmail());    // Load the user's profile image using Glide
            fetchUserDetails(currentUser, navUserEmail, nameTextView, imageView);
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
            R.id.nav_admin_aid,
            R.id.nav_admin_amenities,
            R.id.nav_admin_aid_management,
            R.id.nav_admin_aid_reports,
            R.id.nav_user_management,
            R.id.nav_admin_chats,
            R.id.nav_admin_groups,
            R.id.nav_user_profile
        )
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_admin);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            logoutUser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void fetchUserDetails(FirebaseUser user, TextView emailTextView, TextView nameTextView, ImageView imageView) {
        // Set user email
        emailTextView.setText(user.getEmail());

        // Fetch photo URL from Firestore or other user database
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel user = snapshot.getValue(UserModel.class);

                if (user != null) {
                    nameTextView.setText(user.getFirstName() + " " + user.getLastName());
                    String photoUrl = user.getProfileImage();
                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        Glide.with(imageView.getContext())
                                .load(photoUrl)
                                .circleCrop()
                                .into(imageView);
                    } else {
                        // Set default profile picture if no photo URL exists
                        imageView.setImageResource(R.drawable.ic_user_placeholder_foreground);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(imageView.getContext(), "Failed to fetch user details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_admin);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}