package com.example.aidhub.groups;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aidhub.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {

    private List<GroupModel> groupList;

    public GroupsAdapter(List<GroupModel> groupList) {
        this.groupList = groupList;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        GroupModel group = groupList.get(position);

        holder.groupNameTextView.setText(group.getGroupName());
        holder.groupCreatedAtTextView.setText("Created At: " + formatTimestamp(group.getCreatedAt()));

        // Set an OnClickListener to the group item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), GroupActivity.class);
            // Pass the group name and other details if needed
            intent.putExtra("groupName", group.getGroupName());
            intent.putExtra("groupId", group.getGroupId());
            intent.putExtra("groupAdmin", group.getAdminId());
            v.getContext().startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView groupNameTextView;
        TextView groupAdminTextView;
        TextView groupCreatedAtTextView;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.groupNameTextView);
            groupAdminTextView = itemView.findViewById(R.id.groupAdminTextView);
            groupCreatedAtTextView = itemView.findViewById(R.id.groupCreatedAtTextView);
        }
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}