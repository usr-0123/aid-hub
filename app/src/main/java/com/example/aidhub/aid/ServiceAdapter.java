package com.example.aidhub.aid;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aidhub.R;

import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<ServiceModel> serviceList;
    private Context context;

    public ServiceAdapter(List<ServiceModel> serviceList, Context context) {
        this.serviceList = serviceList;
        this.context = context;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceModel service = serviceList.get(position);
        holder.titleTextView.setText(service.getTitle());
        holder.descriptionTextView.setText(service.getDescription());

        // Edit button action
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditServiceActivity.class);
                intent.putExtra("serviceId", service.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView;
        ImageButton editButton;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.serviceTitleTextView);
            descriptionTextView = itemView.findViewById(R.id.serviceDescriptionTextView);
            editButton = itemView.findViewById(R.id.editServiceButton);
        }
    }
}
