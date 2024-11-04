package com.example.aidhub.groups;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aidhub.R;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.aidhub.messaging.MessageModel;
import com.example.aidhub.messaging.MessagesAdapter;
import com.example.aidhub.notification.MessageNotificationHelper;
import com.example.aidhub.users.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText messageInputEditText;
    private Button sendButton, addUserButton;
    private MessagesAdapter adapter;
    private List<MessageModel> messageList = new ArrayList<>();
    private String groupId;
    private String currentUserId;
    private String senderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        // Fetch current user ID from FirebaseAuth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return; // Exit the activity if no user is logged in
        }

        // Retrieve the group name from the intent
        String groupName = getIntent().getStringExtra("groupName");
        groupId = getIntent().getStringExtra("groupId");

        // Set the group name on the toolbar or a TextView
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(groupName); // Set title on the toolbar
        }

        recyclerView = findViewById(R.id.groupMessagesRecyclerView);
        messageInputEditText = findViewById(R.id.messageInputEditText);
        sendButton = findViewById(R.id.sendButton);

        checkUserTypeAndSetButtonVisibility();

        addUserButton = findViewById(R.id.addUserButton);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessagesAdapter(senderName, messageList, currentUserId);
        recyclerView.setAdapter(adapter);

        // Fetch group messages
        fetchGroupMessages(groupId);

        // Send button listener
        sendButton.setOnClickListener(v -> {
            String messageText = messageInputEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(messageText)) {
                sendMessageToGroup(messageText, groupId);
                messageInputEditText.setText(""); // Clear input field
            } else {
                Toast.makeText(GroupActivity.this, "Enter a message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessageToGroup(String messageText, String groupId) {
        // Get a reference to the group messages node
        DatabaseReference groupMessagesRef = FirebaseDatabase.getInstance().getReference("groupMessages").child(groupId);

        // Generate a unique message ID
        String messageId = groupMessagesRef.push().getKey();

        // Get the current user ID (sender)
        String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        List<String> readBy = new ArrayList<>();
        readBy.add(senderId);

        // Create a message object
        MessageModel message = new MessageModel(
                messageId,
                "",
                messageText,
                senderId,
                System.currentTimeMillis() + "",
                "text",
                readBy
        );

        // Save the message under the groupId
        groupMessagesRef.child(messageId).setValue(message).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(GroupActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(GroupActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchGroupMessages(String groupId) {
        DatabaseReference groupMessagesRef = FirebaseDatabase.getInstance().getReference("groupMessages").child(groupId);

        groupMessagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<MessageModel> messageList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageModel message = snapshot.getValue(MessageModel.class);
                    if (message != null) {
                        messageList.add(message);
                        fetchAndDisplaySenderName(message.getSenderId());

                        // Mark the message as read by the current user
                        markMessageAsRead(groupId, message.getMessageId());
                    }

                    // Fetch the readBy list
                    List<String> readByList = new ArrayList<>();
                    DataSnapshot readBySnapshot = snapshot.child("readBy");

                    if (readBySnapshot.exists()) {
                        for (DataSnapshot childSnapshot : readBySnapshot.getChildren()) {
                            String userId = childSnapshot.getValue(String.class);
                            if (userId != null) {
                                readByList.add(userId);
                            }
                        }
                    }

                    // If the current user hasn't read this report, trigger a notification
                    if (!readByList.contains(currentUserId)) {
                        sendGroupMessageNotification(groupId, senderName, message.getMessage());
                    }
                }

                // Now update the RecyclerView with the new message list
                updateRecyclerView(messageList, currentUserId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GroupActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendGroupMessageNotification(String groupId,String sender, String message) {
        // Use the NotificationHelper to send a notification with the report details
        MessageNotificationHelper.showNotification(this, groupId, sender, message);
    }

    private void markMessageAsRead(String groupId, String messageId) {
        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("groupMessages")
                .child(groupId)
                .child(messageId)
                .child("readBy");

        messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> readByList = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        readByList.add(snapshot.getValue(String.class));
                    }
                }

                // If the current user hasn't read the message, mark it as read
                if (!readByList.contains(currentUserId)) {
                    readByList.add(currentUserId);

                    // Update the readBy field in Firebase
                    messageRef.setValue(readByList).addOnSuccessListener(aVoid -> {
                        // Success handling (optional)
                    }).addOnFailureListener(e -> {
                        // Handle failure (optional)
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    private void updateRecyclerView(List<MessageModel> messages, String currentUserId) {
        MessagesAdapter adapter = new MessagesAdapter(senderName, messages, currentUserId);
        recyclerView.setAdapter(adapter);
    }

    public void addUserToGroup(String groupId, String userId) {
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("groups").child(groupId);

        // Add userId to the members list by using a transaction to prevent overwriting
        groupRef.child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean userAlreadyExists = false;
                int memberCount = (int) dataSnapshot.getChildrenCount(); // Count existing members

                // Check if the user is already a member
                for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                    String existingUserId = memberSnapshot.getValue(String.class);
                    if (existingUserId != null && existingUserId.equals(userId)) {
                        userAlreadyExists = true;
                        break;
                    }
                }

                if (!userAlreadyExists) {
                    // User not already in the group, so we can add them with index as the key
                    groupRef.child("members").child(String.valueOf(memberCount)).setValue(userId)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(GroupActivity.this, "User added to group", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(GroupActivity.this, "Failed to add user", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(GroupActivity.this, "User is already a member of the group", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GroupActivity.this, "Failed to check members", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAndDisplaySenderName(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel user = snapshot.getValue(UserModel.class);
                if (user != null) {
                    senderName = user.getFirstName() + " " + user.getLastName();
                    // call the xml holder here
                } else {
                     Toast.makeText(GroupActivity.this, "Error fetching user details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroupActivity.this, "Error group messages.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUserSelectionDialog(String groupId) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        List<String> userList = new ArrayList<>();
        List<String> userIds = new ArrayList<>();

        // Fetch users from Firebase
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey(); // Get user ID
                    String firstName = userSnapshot.child("firstName").getValue(String.class);
                    String lastName = userSnapshot.child("lastName").getValue(String.class);
                    String userName = firstName + lastName;

                    userList.add(userName);
                    userIds.add(userId);
                }

                // After fetching users, show the dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);
                builder.setTitle("Select Users to Add")
                        .setItems(userList.toArray(new String[0]), (dialog, which) -> {
                            String selectedUserId = userIds.get(which); // Get userId from the selection
                            addUserToGroup(groupId, selectedUserId); // Add the selected user to the group
                        });
                builder.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GroupActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserTypeAndSetButtonVisibility() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel user = snapshot.getValue(UserModel.class);
                if (user != null && ("admin".equals(user.getUserType()) || "Admin".equals(user.getUserType()))) {
                    addUserButton.setVisibility(View.VISIBLE); // Show button for admins
                } else {
                    addUserButton.setVisibility(View.GONE); // Hide button for non-admins
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroupActivity.this, "Failed to fetch user type", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onAddUserButtonClick(View view) {
        showUserSelectionDialog(groupId);
    }
}