package com.example.aidhub.messaging;

import static com.example.aidhub.notification.ChatsNotificationHelper.showChatNotification;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aidhub.R;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class MessagingActivity extends AppCompatActivity {

        private String chatId;
        private String selectedUserId;
        private String currentUserId;
        private TextView chatParticipantNameTextView;
        private RecyclerView recyclerViewMessages;
        private MessagesAdapter messagesAdapter;
        private String participantName;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_messaging);

            // Initialize views
            chatParticipantNameTextView = findViewById(R.id.chatParticipantNameTextView);
            recyclerViewMessages = findViewById(R.id.chatMessagesRecyclerView);
            EditText editTextMessage = findViewById(R.id.messageEditText);
            Button buttonSend = findViewById(R.id.sendMessageButton);

            // Set up RecyclerView
            recyclerViewMessages.setHasFixedSize(true);
            recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
            messagesAdapter = new MessagesAdapter(participantName, new ArrayList<>(), currentUserId);
            recyclerViewMessages.setAdapter(messagesAdapter);

            // Retrieve chat ID and selected user from Intent
            chatId = getIntent().getStringExtra("chatId");
            selectedUserId = getIntent().getStringExtra("selectedUserId");

            // Fetch current user ID from FirebaseAuth
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                currentUserId = currentUser.getUid();
            } else {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                finish();
                return; // Exit the activity if no user is logged in
            }

            if (chatId != null && selectedUserId != null) {
                // Proceed with chat setup
                fetchAndDisplayParticipantName(selectedUserId);
                fetchMessages(chatId);
            } else {
                Toast.makeText(this, "Error loading chat", Toast.LENGTH_SHORT).show();
                finish();
            }

            // Send message on button click
            buttonSend.setOnClickListener(v -> {
                String messageText = editTextMessage.getText().toString().trim();
                if (!messageText.isEmpty()) {
                    sendMessage(messageText, currentUserId, chatId);
                    editTextMessage.setText(""); // Clear the input after sending
                }
            });
        }

        private void fetchAndDisplayParticipantName(String userId) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserModel user = dataSnapshot.getValue(UserModel.class);
                    if (user != null) {
                        participantName = user.getFirstName() + " " + user.getLastName();
                        chatParticipantNameTextView.setText(participantName);
                    } else {
                        Toast.makeText(MessagingActivity.this, "Error fetching users info", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MessagingActivity.this, "Error loading chat", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void sendMessage(String messageText, String senderId, String chatId) {
            // Reference to the messages node in the Firebase database
            DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId);

            // Generate a new message ID
            String messageId = messagesRef.push().getKey();

            List<String> readBy = new ArrayList<>();
            readBy.add(currentUserId);

            // Create a new message object
            MessageModel message = new MessageModel(
                    chatId,
                    "",
                    messageText,
                    senderId,
                    System.currentTimeMillis() + "",
                    "text",
                    readBy
            );

            // Store the message in Firebase under the generated messageId
            messagesRef.child(messageId).setValue(message).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(MessagingActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MessagingActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void fetchMessages(String chatId) {
            DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId);

            messagesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<MessageModel> messageList = new ArrayList<>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        MessageModel message = snapshot.getValue(MessageModel.class);
                        if (message != null) {
                            messageList.add(message);

                            markMessageAsRead(chatId, message.getMessageId());
                        }

                        // Fetch the readBy list
                        List<String> readByList = new ArrayList<>();
                        DataSnapshot readBySnapshot = snapshot.child("readBy");

                        if (readBySnapshot.exists()) { // Check if the "readBy" node exists
                            for (DataSnapshot childSnapshot : readBySnapshot.getChildren()) {
                                String userId = childSnapshot.getValue(String.class);
                                if (userId != null) {
                                    readByList.add(userId);
                                }
                            }
                        }

//                         If the current user hasn't read this report, trigger a notification
                        if (!readByList.contains(currentUserId)) {
                            showChatNotification(MessagingActivity.this, chatId, selectedUserId, participantName, message.getMessage());
                        }
                    }

                    // Pass currentUserId to the adapter
                    displayMessages(messageList, currentUserId);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MessagingActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
                }
            });
        }

//        private void sendChatMessageNotification(String chatId, String selectedUserId, String senderName, String message) {
//            ChatsNotificationHelper.showChatNotification(this, chatId, selectedUserId, senderName, message);
//        }

        private void markMessageAsRead(String chatId, String messageId) {
            // Reference to the specific message's "readBy" field in Firebase
            DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("messages")
                    .child(chatId)
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

                        // Update the "readBy" field in Firebase with the updated list
                        messageRef.setValue(readByList).addOnSuccessListener(aVoid -> {
                            // Optional: Handle success if needed (e.g., showing a read confirmation)
                        }).addOnFailureListener(e -> {
                            // Optional: Handle failure if needed
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle possible errors (e.g., logging or showing a message to the user)
                }
            });
        }

        private void displayMessages(List<MessageModel> messageList, String currentUserId) {
            // Update the adapter with new messages
            messagesAdapter = new MessagesAdapter(participantName, messageList, currentUserId);
            recyclerViewMessages.setAdapter(messagesAdapter);
        }


    }