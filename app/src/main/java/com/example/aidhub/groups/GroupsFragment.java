package com.example.aidhub.groups;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aidhub.R;
import com.example.aidhub.messaging.MessageModel;
import com.example.aidhub.notification.MessageNotificationHelper;
import com.example.aidhub.users.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class GroupsFragment extends Fragment {

    private RecyclerView groupsRecyclerView;
    private GroupsAdapter groupsAdapter;
    private List<GroupModel> groupList;
    private String currentUserId;
    private Button new_group_button;
    private FirebaseAuth mAuth;
    DatabaseReference userRef, groupsRef, groupMessagesRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        groupsRecyclerView = view.findViewById(R.id.groupsRecyclerView);
        new_group_button = view.findViewById(R.id.new_group_form);
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Reference to the "groups" node in Firebase
        groupsRef = FirebaseDatabase.getInstance().getReference("groups");

        groupList = new ArrayList<>();
        groupsAdapter = new GroupsAdapter(groupList);
        groupsRecyclerView.setAdapter(groupsAdapter);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        new_group_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Create New Group");

                final EditText input = new EditText(view.getContext());
                input.setHint("Enter group name");
                builder.setView(input);

                builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        String groupName = input.getText().toString().trim();
                        if (!groupName.isEmpty()) {
                            checkGroupAvailabilityAndCreate(groupName);
                        } else {
                            Toast.makeText(view.getContext(), "Group name cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        fetchGroups(currentUserId);

        checkUserTypeAndSetButtonVisibility();

        return view;
    }

    private void checkGroupAvailabilityAndCreate(String groupName) {
        groupsRef.orderByChild("groupName").equalTo(groupName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(getContext(), "A group with this name already exists", Toast.LENGTH_SHORT).show();
                } else {
                    createNewGroup(groupName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to check group availability.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserTypeAndSetButtonVisibility() {
        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel user = snapshot.getValue(UserModel.class);
                if (user != null && ("admin".equals(user.getUserType()) || "Admin".equals(user.getUserType()))) {
                    new_group_button.setVisibility(View.VISIBLE);
                } else {
                    new_group_button.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to fetch user type", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewGroup(String groupName) {
        String groupId = groupsRef.push().getKey();
        String adminId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        long createdAt, updatedAt;
        createdAt= System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
        List<String> members = new ArrayList<>();
        members.add(adminId); // Admin is the first member

        GroupModel newGroup = new GroupModel(
                groupId,
                adminId,
                groupName,
                createdAt,
                updatedAt,
                members
        );

        if (groupId != null) {
            groupsRef.child(groupId).setValue(newGroup).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Group created successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to create group.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fetchGroups(String currentUserId) {

        // Attach a listener to read the group data
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<GroupModel> groupList = new ArrayList<>();

                // Iterate through all groups in the database
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    // Map the snapshot data to the GroupModel object
                    GroupModel group = groupSnapshot.getValue(GroupModel.class);

                    if (group != null && group.getMembers() != null) {
                        // Check if the current user is in the members list
                        if (group.getMembers().contains(currentUserId)) {
                            group.setGroupId(groupSnapshot.getKey());
                            fetchGroupMessages(group.getGroupId());
                            groupList.add(group);
                        }
                    }
                }

                // Once the groups are fetched, update the RecyclerView
                if (getActivity() != null) {
                    GroupsAdapter adapter = new GroupsAdapter(groupList);
                    groupsRecyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
                Toast.makeText(getContext(), "Failed to load groups", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchGroupMessages(String groupId) {
        groupMessagesRef = FirebaseDatabase.getInstance().getReference("groupMessages");

        groupMessagesRef.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    MessageModel groupMessageModel = messageSnapshot.getValue(MessageModel.class);

                    if (!groupMessageModel.getReadBy().contains(currentUserId)) {
                        sendGroupMessageNotification(groupId, groupMessageModel.getSenderId(), groupMessageModel.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle errors
            }
        });
    }

    private void sendGroupMessageNotification(String groupId,String sender, String message) {
        // Use the NotificationHelper to send a notification with the report details
        MessageNotificationHelper.showNotification(getContext(), groupId, sender, message);
    }
}