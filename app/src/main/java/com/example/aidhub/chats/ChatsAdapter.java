package com.example.aidhub.chats;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aidhub.databinding.ItemChatBinding;
import com.example.aidhub.users.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatViewHolder> {

    private final List<ChatModel> chatsList;
    private final ChatClickListener chatClickListener;

    public interface ChatClickListener {
        void onChatClick(ChatModel chatModel);
    }

    public ChatsAdapter(List<ChatModel> chatsList, ChatClickListener chatClickListener) {
        this.chatsList = chatsList;
        this.chatClickListener = chatClickListener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatBinding binding = ItemChatBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ChatViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatModel chat = chatsList.get(position);

        // Firebase reference to users
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Get the current user's ID
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Find the other member who is not the current user
        String otherUserId = null;
        for (String memberId : chat.getMembers()) {
            if (!memberId.equals(currentUserId)) {
                otherUserId = memberId;
                break;
            }
        }

        if (otherUserId != null) {
            // Fetch the other user's details from Firebase
            usersRef.child(otherUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserModel otherUser = dataSnapshot.getValue(UserModel.class);

                    if (otherUser != null) {
                        // Display the other user's first name and last name
                        String fullName = otherUser.getFirstName() + " " + otherUser.getLastName();
                        holder.binding.textViewChatId.setText(fullName);
                    } else {
                        holder.binding.textViewChatId.setText("Title");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FirebaseError", "Error fetching user details", databaseError.toException());
                }
            });
        }

        // Set click listener for opening chat
        holder.itemView.setOnClickListener(v -> chatClickListener.onChatClick(chat));
    }


    @Override
    public int getItemCount() {
        return chatsList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatBinding binding;

        public ChatViewHolder(@NonNull ItemChatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}