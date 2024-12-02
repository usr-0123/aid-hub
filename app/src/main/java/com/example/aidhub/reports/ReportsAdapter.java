package com.example.aidhub.reports;

import android.content.Context;
import android.content.Intent;
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
import com.example.aidhub.users.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportsViewHolder> {
    private List<ReportsModel> reportsList;
    private Context context;
    private DatabaseReference usersRef, reportsRef;

    public ReportsAdapter(List<ReportsModel> reportsList, Context context) {
        this.reportsList = reportsList;
        this.context = context;
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
        this.reportsRef = FirebaseDatabase.getInstance().getReference("aid_reports");
    }

    @NonNull
    @Override
    public ReportsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportsViewHolder(view);
    }

    public void onBindViewHolder(@NonNull ReportsViewHolder holder, int position) {
        ReportsModel report = reportsList.get(position);
        holder.service.setText(report.getService());
        holder.description.setText(report.getDescription());
        holder.description.setText(report.getDescription());
        holder.provided_date.setText(report.getProvidedDate());
        holder.reportDescription.setText(report.getReportDescription());

        // Fetch and display user name for seekerId
        fetchAndDisplaySenderName(holder, report.getSeekerId(), fullName -> {
            holder.seeker.setText(fullName);
        });

        // Fetch and display user name for providerId
        fetchAndDisplaySenderName(holder, report.getProviderId(), fullName -> {
            holder.provider.setText(fullName);
        });

        holder.itemView.setOnClickListener(v -> {
            // Create an AlertDialog builder
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Select an Option");

            // Options to display
            String[] options = {"Location", "Delete", "Cancel"};

            // Set the options in the dialog
            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // maps
                        Intent intent = new Intent(context, MapsActivity.class);
                        intent.putExtra("latitude", report.getLatitude());
                        intent.putExtra("longitude", report.getLongitude());
                        intent.putExtra("service", report.getService());
                        intent.putExtra("description", report.getDescription());
                        intent.putExtra("seekerId", report.getSeekerId());
                        context.startActivity(intent);
                        break;


                    case 1: // Delete
                        // Handle Delete
                        deleteReport(report.getRequestId());
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

    public int getItemCount() {
        return reportsList.size();
    }

    public static class ReportsViewHolder extends RecyclerView.ViewHolder {
        TextView description, service, seeker, provider, provided_date, reportDescription;

        public ReportsViewHolder(@NonNull View itemView) {
            super(itemView);
            service = itemView.findViewById(R.id.service);
            description = itemView.findViewById(R.id.description);
            seeker = itemView.findViewById(R.id.seekerId);
            provider = itemView.findViewById(R.id.providerId);
            provided_date = itemView.findViewById(R.id.providedDate);
            reportDescription = itemView.findViewById(R.id.reportDescription);
        }
    }

    // Method to fetch user name by userId
    public void fetchAndDisplaySenderName(@NonNull ReportsViewHolder holder, String userId, FetchUserCallback callback) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel user = snapshot.getValue(UserModel.class);
                if (user != null) {
                    String fullName = user.getFirstName() + " " + user.getLastName();
                    callback.onUserFetched(fullName);
                } else {
                    callback.onUserFetched("Unknown");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onUserFetched("Unknown");
            }
        });
    }

    public interface FetchUserCallback {
        void onUserFetched(String fullName);
    }

    private void deleteReport(String reportId) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Aid Report")
                .setMessage("Are you sure you want to delete this aid report? This action is not reversible.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    reportsRef.child(reportId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                // Notify the user of successful deletion
                                Toast.makeText(context, "Aid report deleted successfully.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                // Handle errors during deletion
                                Toast.makeText(context, "Failed to delete aid report. Please try again.", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
