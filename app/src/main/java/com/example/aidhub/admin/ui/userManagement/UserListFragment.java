package com.example.aidhub.admin.ui.userManagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aidhub.R;
import com.example.aidhub.users.UserAdapter;
import com.example.aidhub.users.UserModel;
import com.example.aidhub.users.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class UserListFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<UserModel> userList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_management, container, false);

        recyclerView = view.findViewById(R.id.usersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Fetch users from Firebase Realtime Database
        UserRepository userRepository = new UserRepository();
        userRepository.getAllUsers(users -> {
            userList = users;
            adapter = new UserAdapter(getContext(), userList);
            recyclerView.setAdapter(adapter);
        });

        return view;
    }
}
