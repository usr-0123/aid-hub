package com.example.aidhub.admin.ui.chats.forms;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aidhub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Check to prevent duplication.

public class NewChatActivity extends AppCompatActivity {
    private Button senderButton, recipientButton, createChatButton;
    private String selectedSenderId, selectedRecipientId; // Stores selected user IDs
    DatabaseReference chatsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_chat);

        senderButton = findViewById(R.id.buttonSelectSender);
        recipientButton = findViewById(R.id.buttonSelectReceiver);
        createChatButton = findViewById(R.id.buttonCreateChat);

        // Initialize Firebase chat reference
        chatsRef = FirebaseDatabase.getInstance().getReference("chats");

        // Set OnClickListener for the sender selection button
        senderButton.setOnClickListener(view -> showUserSelectionDialog(true));

        // Set OnClickListener for the recipient selection button
        recipientButton.setOnClickListener(view -> showUserSelectionDialog(false));

        // Set OnClickListener for the create chat button
        createChatButton.setOnClickListener(view -> {
            if (selectedSenderId != null && selectedRecipientId != null) {
                createNewChat();
            } else {
                Toast.makeText(NewChatActivity.this, "Please select both sender and recipient", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void createNewChat() {
//        chatId = chatsRef.push().getKey(); // Generate a new chat ID
//        if (chatId != null) {
//            Map<String, Object> chatData = new HashMap<>();
//            List<String> members = new ArrayList<>();
//            members.add(selectedSenderId);
//            members.add(selectedRecipientId);
//            chatData.put("members", members);
//            chatData.put("createdAt", System.currentTimeMillis());
//            chatData.put("updatedAt", System.currentTimeMillis());
//
//            chatsRef.child(chatId).setValue(chatData).addOnCompleteListener(task -> {
//                if (task.isSuccessful()) {
//                    Toast.makeText(NewChatActivity.this, "Created chat successfully.", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(NewChatActivity.this, "Failed to create chat.", Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    }

    private void createNewChat() {
        if (selectedSenderId == null || selectedRecipientId == null) {
            Toast.makeText(this, "Please select both sender and recipient", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query chats to check if one with both users as members already exists
        chatsRef.orderByChild("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean chatExists = false;

                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    List<String> members = (List<String>) chatSnapshot.child("members").getValue();
                    if (members != null && members.contains(selectedSenderId) && members.contains(selectedRecipientId)) {
                        chatExists = true;
                        break;
                    }
                }

                if (chatExists) {
                    Toast.makeText(NewChatActivity.this, "Chat already exists between these users.", Toast.LENGTH_SHORT).show();
                } else {
                    // No existing chat with these members, create a new chat
                    String newChatId = chatsRef.push().getKey();
                    if (newChatId != null) {
                        Map<String, Object> chatData = new HashMap<>();
                        List<String> members = new ArrayList<>();
                        members.add(selectedSenderId);
                        members.add(selectedRecipientId);
                        chatData.put("members", members);
                        chatData.put("createdAt", System.currentTimeMillis());
                        chatData.put("updatedAt", System.currentTimeMillis());

                        chatsRef.child(newChatId).setValue(chatData).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(NewChatActivity.this, "Created chat successfully.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(NewChatActivity.this, "Failed to create chat.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NewChatActivity.this, "Failed to check existing chats", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showUserSelectionDialog(boolean isSender) {
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

                // Show the dialog after fetching users
                AlertDialog.Builder builder = new AlertDialog.Builder(NewChatActivity.this);
                builder.setTitle(isSender ? "Select Sender" : "Select Recipient")
                        .setItems(userList.toArray(new String[0]), (dialog, which) -> {
                            String selectedUserId = userIds.get(which);

                            if (isSender) {
                                selectedSenderId = selectedUserId;
                                senderButton.setText(userList.get(which));
                                Toast.makeText(NewChatActivity.this, "Sender selected", Toast.LENGTH_SHORT).show();
                            } else {
                                selectedRecipientId = selectedUserId;
                                recipientButton.setText(userList.get(which));
                                Toast.makeText(NewChatActivity.this, "Recipient selected", Toast.LENGTH_SHORT).show();
                            }
                        });
                builder.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(NewChatActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
