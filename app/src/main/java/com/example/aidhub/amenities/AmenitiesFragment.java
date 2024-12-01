package com.example.aidhub.amenities;

import android.content.Intent;
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
import com.example.aidhub.amenities.form.NewAmenityActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class AmenitiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private AmenityAdapter adapter;
    private List<amenityModel> amenityList;
    private Button newAmenity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_amenities, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewAmenities);
        newAmenity = view.findViewById(R.id.btnAddAmenity);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        amenityList = new ArrayList<>();
        adapter = new AmenityAdapter(amenityList, getContext());
        recyclerView.setAdapter(adapter);

        fetchAmenitiesFromFirebase();

        newAmenity.setOnClickListener(v -> {
            // Navigate to CreateAmenityActivity
            Intent intent = new Intent(getActivity(), NewAmenityActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void fetchAmenitiesFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("amenities");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                amenityList.clear(); // Clear the list to avoid duplication
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    amenityModel amenity = dataSnapshot.getValue(amenityModel.class);
                    if (amenity != null) {
                        amenityList.add(amenity);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
