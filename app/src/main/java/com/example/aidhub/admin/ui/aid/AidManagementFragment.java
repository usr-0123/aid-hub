package com.example.aidhub.admin.ui.aid;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aidhub.R;
import com.example.aidhub.aid.AddServiceActivity;
import com.example.aidhub.aid.ServiceAdapter;
import com.example.aidhub.aid.ServiceModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AidManagementFragment extends Fragment {

    private RecyclerView servicesRecyclerView;
    private FloatingActionButton addServiceButton;
    private ServiceAdapter serviceAdapter;
    private List<ServiceModel> serviceList;
    private DatabaseReference servicesRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_aid_management, container, false);

        servicesRecyclerView = rootView.findViewById(R.id.servicesRecyclerView);
        addServiceButton = rootView.findViewById(R.id.addServiceButton);

        // Initialize Firebase reference
        servicesRef = FirebaseDatabase.getInstance().getReference("services");

        // Set up RecyclerView
        serviceList = new ArrayList<>();
        serviceAdapter = new ServiceAdapter(serviceList, getContext());
        servicesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        servicesRecyclerView.setAdapter(serviceAdapter);

        // Fetch services from Firebase
        fetchServices();

        // Add new service
        addServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to AddServiceActivity to add a new service
                Intent intent = new Intent(getActivity(), AddServiceActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }

    private void fetchServices() {
        servicesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                serviceList.clear();
                for (DataSnapshot serviceSnapshot : snapshot.getChildren()) {
                    ServiceModel service = serviceSnapshot.getValue(ServiceModel.class);
                    if (service != null) {
                        serviceList.add(service);
                    }
                }
                serviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load services.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
