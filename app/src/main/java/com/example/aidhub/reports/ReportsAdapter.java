package com.example.aidhub.reports;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
    private DatabaseReference usersRef;

    public ReportsAdapter(List<ReportsModel> reportsList, Context context) {
        this.reportsList = reportsList;
        this.context = context;
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    @NonNull
    @Override
    public ReportsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportsViewHolder(view);
    }

    public void onBindViewHolder(@NonNull ReportsViewHolder holder, int position) {
        ReportsModel report = reportsList.get(position);
        holder.description.setText(report.getDescription());
        holder.description.setText(report.getDescription());
        holder.provided_date.setText(report.getProvidedDate());
        holder.reportDescription.setText(report.getReportDescription());


        // Fetch and display user name for seekerId
        fetchUserName(report.getSeekerId(), new UserNameCallback() {
            @Override
            public void onSuccess(String fullName) {
                holder.seeker.setText("Seeker: " + fullName);
            }

            @Override
            public void onFailure(String errorMessage) {
                holder.seeker.setText("Seeker: Unknown");
                Toast.makeText(context,"Error fetching seeker name", Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch and display user name for seekerId
        fetchUserName(report.getProviderId(), new UserNameCallback() {
            @Override
            public void onSuccess(String fullName) {
                holder.seeker.setText("Provider: " + fullName);
            }

            @Override
            public void onFailure(String errorMessage) {
                holder.seeker.setText("Provider: Unknown");
                Toast.makeText(context,"Error fetching aid provider name", Toast.LENGTH_SHORT).show();
            }
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
    private void fetchUserName(String userId, final UserNameCallback callback) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel user = snapshot.getValue(UserModel.class);
                    if (user != null) {
                        String fullName = user.getFirstName() + " " + user.getLastName();
                        callback.onSuccess(fullName);
                    } else {
                        callback.onFailure("User data is null");
                    }
                } else {
                    callback.onFailure("User ID not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    // Callback interface
    public interface UserNameCallback {
        void onSuccess(String fullName);

        void onFailure(String errorMessage);
    }
}
