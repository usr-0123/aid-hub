package com.example.aidhub.aid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aidhub.R;

import java.util.List;

public class AidRequestAdapter extends RecyclerView.Adapter<AidRequestAdapter.AidRequestViewHolder> {

    private final List<AidRequestModel> aidRequestList;

    public AidRequestAdapter(List<AidRequestModel> aidRequestList) {
        this.aidRequestList = aidRequestList;
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
    }

    @Override
    public int getItemCount() {
        return aidRequestList.size();
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
