package com.example.aidhub.users;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private DatabaseReference databaseReference;

    public UserRepository() {
        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    // Callback interface to return data asynchronously
    public interface UsersCallback {
        void onCallback(List<UserModel> userList);
    }

    // Method to fetch all users from Realtime Database
    public void getAllUsers(final UsersCallback callback) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<UserModel> userList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserModel user = snapshot.getValue(UserModel.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }
                callback.onCallback(userList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
                System.err.println("Error fetching users: " + databaseError.getMessage());
            }
        });
    }
}
