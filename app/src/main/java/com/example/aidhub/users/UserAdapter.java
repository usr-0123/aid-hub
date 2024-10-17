package com.example.aidhub.users;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aidhub.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private List<UserModel> userList;

    public UserAdapter(Context context, List<UserModel> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = userList.get(position);
        String formattedTime = formatTime(user.getLastSeen());
        holder.userNameTextView.setText(user.getFirstName() + " " + user.getLastName());
        holder.emailTextView.setText(user.getEmail());
        holder.userRoleTextView.setText(user.getUserType());
        holder.phoneTextView.setText(user.getPhone());
        holder.lastSeenTextView.setText(formattedTime);

        holder.itemView.setOnClickListener(v -> {
            showRoleChangeDialog(user);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView;
        TextView emailTextView;
        TextView userRoleTextView;
        TextView phoneTextView;
        TextView lastSeenTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.tvUserName);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            userRoleTextView = itemView.findViewById(R.id.userRoleTextView);
            phoneTextView = itemView.findViewById(R.id.phoneNumberTextView);
            lastSeenTextView = itemView.findViewById(R.id.lastSeenTextView);
        }
    }

    private String formatTime(Long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(time));
    }

    private void showRoleChangeDialog(UserModel user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Change Role");

        String[] roles = {"User", "Admin"};
        int checkedItem = user.getUserType().equals("admin") ? 1 : 0;

        builder.setSingleChoiceItems(roles, checkedItem, (dialog, which) -> {
            String selectedRole = roles[which];
            updateUserRoleInFirebase(user.getUserId(), selectedRole);
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateUserRoleInFirebase(String userId, String newRole) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(userId).child("userType").setValue(newRole)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "User role updated to " + newRole, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to update role", Toast.LENGTH_SHORT).show());
    }

}
