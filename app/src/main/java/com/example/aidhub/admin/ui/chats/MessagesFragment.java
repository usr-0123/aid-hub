package com.example.aidhub.admin.ui.chats;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.aidhub.R;
import com.example.aidhub.admin.ui.chats.forms.NewChatActivity;
import com.example.aidhub.chats.ChatModel;
import com.example.aidhub.chats.ChatsAdapter;
import com.example.aidhub.databinding.FragmentMessagesBinding;
import com.example.aidhub.messaging.MessagingActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MessagesFragment extends Fragment {

    private FragmentMessagesBinding binding;
    private ChatsAdapter chatsAdapter;
    private List<ChatModel> chatsList;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    private Button newChatButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout using ViewBinding
        binding = FragmentMessagesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        // Initialize RecyclerView
        binding.chatsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize chats list
        chatsList = new ArrayList<>();

        // Set up the adapter
        chatsAdapter = new ChatsAdapter(chatsList, this::openChatActivity);
        binding.chatsRecyclerView.setAdapter(chatsAdapter);

        // Fetch chats from Firebase
        fetchChatsFromFirebase();

        newChatButton = root.findViewById(R.id.new_chat_form);

        // Set OnClickListener to navigate to NewGroupActivity
        newChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to NewGroupActivity
                Intent intent = new Intent(getActivity(), NewChatActivity.class);
                startActivity(intent);
            }
        });

        return root;
    }

    private void fetchChatsFromFirebase() {
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("chats");

        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatModel chat = snapshot.getValue(ChatModel.class);
                    if (chat != null && chat.getMembers().contains(currentUserId)) {
                        // Only add chats where the current user is a member
                        chat.setChatId(snapshot.getKey());
                        chatsList.add(chat);
                    }
                }

                if (chatsList.isEmpty()) {
                    Toast.makeText(getContext(), "No chats found", Toast.LENGTH_SHORT).show();
                }

                // Notify adapter after data has been loaded
                chatsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error fetching chats", Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", "Error fetching chats", databaseError.toException());
            }
        });
    }

    private void openChatActivity(ChatModel chatModel) {
        // Open ChatActivity with the selected chat's ID
        Intent intent = new Intent(getActivity(), MessagingActivity.class);
        intent.putExtra("chatId", chatModel.getChatId());
        intent.putExtra("selectedUserId", getOtherUserId(chatModel));

        // Mark messages as read
        markMessagesAsRead(chatModel.getChatId());

        startActivity(intent);
    }

    // Helper method to get the other user in the chat (the one that isn't the current user)
    private String getOtherUserId(ChatModel chatModel) {
        for (String memberId : chatModel.getMembers()) {
            if (!memberId.equals(currentUserId)) {
                return memberId;
            }
        }
        return null;
    }

    private void markMessagesAsRead(String chatId) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child("messages");

        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    String messageId = messageSnapshot.getKey();
                    List<String> readByList = new ArrayList<>();

                    if (messageSnapshot.child("readBy").exists()) {
                        // Get the current list of users who have read the message
                        for (DataSnapshot readBySnapshot : messageSnapshot.child("readBy").getChildren()) {
                            readByList.add(readBySnapshot.getValue(String.class));
                        }
                    }

                    // If current user hasn't read this message, mark it as read
                    if (!readByList.contains(currentUserId)) {
                        readByList.add(currentUserId);

                        // Update the readBy field in Firebase
                        messagesRef.child(messageId).child("readBy").setValue(readByList)
                                .addOnSuccessListener(aVoid -> Log.d("ChatsFragment", "Message marked as read"))
                                .addOnFailureListener(e -> Log.e("ChatsFragment", "Failed to mark message as read", e));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ChatsFragment", "Error fetching messages: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}