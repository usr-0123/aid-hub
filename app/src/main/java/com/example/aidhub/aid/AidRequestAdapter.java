package com.example.aidhub.aid;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aidhub.MapsActivity;
import com.example.aidhub.R;
import com.example.aidhub.messaging.MessagingActivity;
import com.example.aidhub.users.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AidRequestAdapter extends RecyclerView.Adapter<AidRequestAdapter.AidRequestViewHolder> {

    private final List<AidRequestModel> aidRequestList;
    private final Context context;
    private final DatabaseReference chatsRef; // Firebase reference for chats
    private final String currentUserId;
    private DatabaseReference userRef;

    public AidRequestAdapter(List<AidRequestModel> aidRequestList, Context context, DatabaseReference chatsRef, String currentUserId) {
        this.aidRequestList = aidRequestList;
        this.context = context;
        this.chatsRef = chatsRef;
        this.currentUserId = currentUserId;

        userRef = FirebaseDatabase.getInstance().getReference("users");
    }

    @NonNull
    @Override
    public AidRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_aid_request, parent, false);
        return new AidRequestViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AidRequestViewHolder holder, int position) {
        AidRequestModel aidRequest = aidRequestList.get(position);
        holder.serviceTextView.setText(aidRequest.getService());
        holder.descriptionTextView.setText(aidRequest.getDescription());
        holder.locationTextView.setText("Location: " + aidRequest.getLatitude() + ", " + aidRequest.getLongitude());

        getUserRole(currentUserId, role -> {

            if ("admin".equals(role) || "Admin".equals(role)) {
                holder.itemView.setOnClickListener(v -> {
                    // Create an AlertDialog builder
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Select an Option");

                    // Options to display
                    String[] options = {"Chat", "Location", "Approve", "Cancel"};

                    // Set the options in the dialog
                    builder.setItems(options, (dialog, which) -> {
                        switch (which) {
                            case 0: // Chat
                                // Handle Chat option
                                createNewChat(aidRequest.getSeekerId());
                                break;

                            case 1: // maps
                                Intent intent = new Intent(context, MapsActivity.class);
                                intent.putExtra("latitude", aidRequest.getLatitude());
                                intent.putExtra("longitude", aidRequest.getLongitude());
                                intent.putExtra("service", aidRequest.getService());
                                intent.putExtra("description", aidRequest.getDescription());
                                intent.putExtra("seekerId", aidRequest.getSeekerId());
                                context.startActivity(intent);
                                break;

                            case 2: // Approve
                                // Update the approved field in the database
                                DatabaseReference aidRequestRef = FirebaseDatabase.getInstance().getReference("aid_requests")
                                        .child(aidRequest.getRequestId()); // Assuming you have a unique requestId field in AidRequestModel

                                aidRequestRef.child("approved").setValue(true)
                                        .addOnSuccessListener(aVoid -> Toast.makeText(context, "Request approved!", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> Toast.makeText(context, "Failed to approve request.", Toast.LENGTH_SHORT).show());
                                Toast.makeText(context, "Approved!", Toast.LENGTH_SHORT).show();
                                break;

                            case 3: // Cancel
                                // Handle Cancel (Do nothing or close dialog)
                                dialog.dismiss();
                                break;
                        }
                    });

                    // Show the dialog
                    builder.show();
                });
            } else {
                holder.itemView.setOnClickListener(v -> {
                    // Create an AlertDialog builder
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Select an Option");

                    // Options to display
                    String[] options = {"Chat", "Location", "Cancel"};

                    // Set the options in the dialog
                    builder.setItems(options, (dialog, which) -> {
                        switch (which) {
                            case 0: // Chat
                                // Handle Chat option
                                createNewChat(aidRequest.getSeekerId());
                                break;

                            case 1: // maps
                                Intent intent = new Intent(context, MapsActivity.class);
                                intent.putExtra("latitude", aidRequest.getLatitude());
                                intent.putExtra("longitude", aidRequest.getLongitude());
                                intent.putExtra("service", aidRequest.getService());
                                intent.putExtra("description", aidRequest.getDescription());
                                intent.putExtra("seekerId", aidRequest.getSeekerId());
                                context.startActivity(intent);
                                break;

                            case 2: // Cancel
                                // Handle Cancel (Do nothing or close dialog)
                                dialog.dismiss();
                                break;
                        }
                    });

                    // Show the dialog
                    builder.show();
                });
            }
        });
    }

    private void createNewChat(String recipientId) {

        if (currentUserId == null || recipientId == null) {
            Toast.makeText(context, "Unable to start chat. Missing user information.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query chats to check if one with both users as members already exists
        chatsRef.orderByChild("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String existingChatId = null;

                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    List<String> members = (List<String>) chatSnapshot.child("members").getValue();
                    if (members != null && members.contains(currentUserId) && members.contains(recipientId)) {
                        existingChatId = chatSnapshot.getKey();
                        break;
                    }
                }

                if (existingChatId != null) {
                    // Chat exists, open the existing chat
                    openChat(existingChatId, recipientId);
                } else {
                    Toast.makeText(context, "Chat does not exists", Toast.LENGTH_SHORT).show();
                    // No existing chat with these members, create a new chat
                    String newChatId = chatsRef.push().getKey();
                    if (newChatId != null) {
                        Map<String, Object> chatData = new HashMap<>();
                        List<String> members = new ArrayList<>();
                        members.add(currentUserId);
                        members.add(recipientId);
                        chatData.put("members", members);
                        chatData.put("createdAt", System.currentTimeMillis());
                        chatData.put("updatedAt", System.currentTimeMillis());

                        chatsRef.child(newChatId).setValue(chatData).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Chat created successfully.", Toast.LENGTH_SHORT).show();
                                openChat(newChatId, recipientId);
                            } else {
                                Toast.makeText(context, "Failed to create chat.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to check existing chats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to open the chat activity
    private void openChat(String chatId, String recipientId) {
        Intent intent = new Intent(context, MessagingActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("selectedUserId", recipientId);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return aidRequestList.size();
    }

    private void getUserRole(String userId, RoleCallback callback) {
        userRef.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                UserModel user = task.getResult().getValue(UserModel.class);

                if (user != null) {
                    if (user.getUserType() != null && !user.getUserType().isEmpty()) {
                        String type = user.getUserType();
                        callback.onRoleFetched(type);
                    }
                } else {
                    callback.onRoleFetched(" ");
                }
            }
        }).addOnFailureListener(e -> {
            callback.onRoleFetched(" ");
            Toast.makeText(context, "Unable to fetch the user role!", Toast.LENGTH_SHORT).show();
        });
    }

    public interface RoleCallback {
        void onRoleFetched(String role);
    }

    public static class AidRequestViewHolder extends RecyclerView.ViewHolder {
        TextView serviceTextView, descriptionTextView, locationTextView;

        public AidRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceTextView = itemView.findViewById(R.id.serviceTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
        }
    }
}
