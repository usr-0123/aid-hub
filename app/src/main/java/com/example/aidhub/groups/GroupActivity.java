package com.example.aidhub.groups;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aidhub.R;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText messageInputEditText;
    private Button sendButton;
    private ImageButton attachmentBtn;
    private MessagesAdapter adapter;
    private List<MessageModel> messageList = new ArrayList<>();
    private String groupId, groupName, groupAdminId, currentUserId, senderName;
    private String userRole = "User";
    private static final int PICK_IMAGE_REQUEST = 1;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        groupName = getIntent().getStringExtra("groupName");
        groupId = getIntent().getStringExtra("groupId");
        groupAdminId = getIntent().getStringExtra("groupAdmin");

        userRef = FirebaseDatabase.getInstance().getReference("users");

        userRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel user = snapshot.getValue(UserModel.class);

                if (user != null) {
                    userRole = user.getUserType();
                } else {
                    userRole = "None";
                }

                invalidateOptionsMenu(); // Refresh menu after fetching the role
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                userRole = "User";
                Toast.makeText(GroupActivity.this, "Failed to fetch user type", Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu(); // Refresh menu after fetching the role
            }
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(groupName);
        }

        recyclerView = findViewById(R.id.groupMessagesRecyclerView);
        messageInputEditText = findViewById(R.id.messageInputEditText);
        sendButton = findViewById(R.id.sendButton);
        attachmentBtn = findViewById(R.id.attachmentButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessagesAdapter(senderName, messageList, currentUserId);
        recyclerView.setAdapter(adapter);

        fetchGroupMessages(groupId);

        sendButton.setOnClickListener(v -> {
            String messageText = messageInputEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(messageText)) {
                sendMessageToGroup(messageText, groupId, "text");
                messageInputEditText.setText("");
            } else {
                Toast.makeText(GroupActivity.this, "Enter a message", Toast.LENGTH_SHORT).show();
            }
        });

        attachmentBtn.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (Objects.equals(currentUserId, groupAdminId)) {
            menu.findItem(R.id.action_add_users).setVisible(true);
            menu.findItem(R.id.action_edit_group).setVisible(true);
            menu.findItem(R.id.action_delete_group).setVisible(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    // Group's menu action
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_chat_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_users) {
            showUserSelectionDialog(groupId);
            return true;
        }

        if (id == R.id.action_edit_group) {
            editGroup();
            return true;
        }

        if (id == R.id.action_delete_group) {
            deleteGroup();
            return true;
        }

        if (id == R.id.action_leave_group) {
            leaveGroup();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Menu action methods
    private void editGroup() {
        // Handle edit group action
        Toast.makeText(this, "Edit Group selected", Toast.LENGTH_SHORT).show();
    }

    private void deleteGroup() {
        // Handle edit group action
        Toast.makeText(this, "Delete Group selected", Toast.LENGTH_SHORT).show();
    }

    private void leaveGroup() {
        // Handle edit group action
        Toast.makeText(this, "Leave Group selected", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            String fileName = "images/" + System.currentTimeMillis() + ".jpg";
            StorageReference storageRef = FirebaseStorage.getInstance().getReference(fileName);

            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        sendMessageToGroup(uri.toString(), groupId, "image");
                    })
            ).addOnFailureListener(e ->
                    Toast.makeText(GroupActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void sendMessageToGroup(String content, String groupId, String messageType) {
        DatabaseReference groupMessagesRef = FirebaseDatabase.getInstance().getReference("groupMessages").child(groupId);
        String messageId = groupMessagesRef.push().getKey();
        String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        List<String> readBy = new ArrayList<>();
        readBy.add(senderId);

        MessageModel message = new MessageModel(
                messageId,
                messageType.equals("image") ? content : "",
                messageType.equals("text") ? content : "",
                senderId,
                String.valueOf(System.currentTimeMillis()),
                messageType,
                readBy
        );

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
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
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
                    String userId = userSnapshot.getKey();
                    String firstName = userSnapshot.child("firstName").getValue(String.class);
                    String lastName = userSnapshot.child("lastName").getValue(String.class);
                    String userName = firstName + " " + lastName;

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
}