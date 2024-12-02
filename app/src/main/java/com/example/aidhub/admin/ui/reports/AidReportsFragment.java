package com.example.aidhub.admin.ui.reports;

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
import com.example.aidhub.reports.ReportsAdapter;
import com.example.aidhub.reports.ReportsModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class AidReportsFragment extends Fragment {
    private RecyclerView recyclerView;
    private ReportsAdapter adapter;
    private List<ReportsModel> reportsList;
    private DatabaseReference reportsRef;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_aid_reports, container, false);
        recyclerView = view.findViewById(R.id.reportsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reportsList = new ArrayList<>();
        adapter = new ReportsAdapter(reportsList, getContext());
        recyclerView.setAdapter(adapter);

        reportsRef = FirebaseDatabase.getInstance().getReference("aid_reports");

        fetchReports();

        return view;
    }

    private void fetchReports() {
        reportsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reportsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ReportsModel reportModel = dataSnapshot.getValue(ReportsModel.class);
                    if (reportModel != null) {
                        reportsList.add(reportModel);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to fetch reports.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}