package com.example.aidhub.groups;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aidhub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class GroupsFragment extends Fragment {

    private RecyclerView groupsRecyclerView;
    private GroupsAdapter groupsAdapter;
    private List<GroupModel> groupList;
    private Button newGroupButton;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        groupsRecyclerView = view.findViewById(R.id.groupsRecyclerView);
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        groupList = new ArrayList<>();
        groupsAdapter = new GroupsAdapter(groupList);
        groupsRecyclerView.setAdapter(groupsAdapter);
        mAuth = FirebaseAuth.getInstance();

        fetchGroups(mAuth.getCurrentUser().getUid());

        return view;
    }

    private void fetchGroups(String currentUserId) {
        // Reference to the "groups" node in Firebase
        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference("groups");

        // Attach a listener to read the group data
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<GroupModel> groupList = new ArrayList<>();

                // Iterate through all groups in the database
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    // Map the snapshot data to the GroupModel object
                    GroupModel group = groupSnapshot.getValue(GroupModel.class);

                    if (group != null && group.getMembers() != null) {
                        // Check if the current user is in the members list
                        if (group.getMembers().contains(currentUserId)) {
                            group.setGroupId(groupSnapshot.getKey());
                            groupList.add(group);
                        }
                    }
                }

                // Once the groups are fetched, update the RecyclerView
                if (getActivity() != null) {
                    GroupsAdapter adapter = new GroupsAdapter(groupList);
                    groupsRecyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
                Toast.makeText(getContext(), "Failed to load groups", Toast.LENGTH_SHORT).show();
            }
        });
    }

}