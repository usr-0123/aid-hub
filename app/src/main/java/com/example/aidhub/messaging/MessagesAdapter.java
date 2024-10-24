package com.example.aidhub.messaging;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aidhub.R;
import com.example.aidhub.users.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private List<MessageModel> messages;
    private String currentUserId;
    private FirebaseAuth firebaseAuth;
    private String senderName;

    public MessagesAdapter(String senderName, List<MessageModel> messages, String currentUserId) {
        this.senderName = senderName;
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageModel message = messages.get(position);

        // Fetch sender's name dynamically based on senderId
        fetchAndDisplaySenderName(holder, message.getSenderId());

        holder.messageTextView.setText(message.getMessage());

        // Format the timestamp to display a readable time
        String formattedTimestamp = formatTimestamp(message.getTimestamp());
        holder.timeTextView.setText(formattedTimestamp);

        // Differentiate between sender and recipient messages
        if (message.getSenderId().equals(currentUserId)) {
            // Message sent by the current user
            holder.itemView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.recipientMessageColor));
            holder.messageTextView.setTextColor(ContextCompat.getColor(holder.messageTextView.getContext(), R.color.white));
        } else {
            // Message received from someone else
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.senderMessageColor));
            holder.itemView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView;
        TextView messageTextView;
        TextView timeTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.senderTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timeTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }

    private String formatTimestamp(String timestamp) {
        long timeMillis = Long.parseLong(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }

    private void fetchAndDisplaySenderName(@NonNull MessageViewHolder holder, String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel user = snapshot.getValue(UserModel.class);
                if (user != null) {
                    String fullName = user.getFirstName() + " " + user.getLastName();
                    holder.senderTextView.setText(fullName);
                } else {
                    holder.senderTextView.setText(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.senderTextView.setText(userId);
            }
        });
    }
}