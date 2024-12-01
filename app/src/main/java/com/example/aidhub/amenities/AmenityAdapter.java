package com.example.aidhub.amenities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aidhub.MapsActivity;
import com.example.aidhub.R;

import java.util.List;

public class AmenityAdapter extends RecyclerView.Adapter<AmenityAdapter.AmenityViewHolder> {

    private List<amenityModel> amenityList;
    private Context context;

    public AmenityAdapter(List<amenityModel> amenityList, Context context) {
        this.amenityList = amenityList;
        this.context = context;
    }

    @NonNull
    @Override
    public AmenityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_amenity, parent, false);
        return new AmenityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AmenityViewHolder holder, int position) {
        amenityModel amenity = amenityList.get(position);
        holder.tvName.setText(amenity.getName());
        holder.tvCategory.setText(amenity.getCategory());
        holder.tvDescription.setText(amenity.getDescription());

        holder.itemView.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Select an option");
            String[] options = {"Location", "Cancel"};

            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // maps
                        Intent intent = new Intent(context, MapsActivity.class);
                        intent.putExtra("latitude", amenity.getLatitude());
                        intent.putExtra("longitude", amenity.getLongitude());
                        intent.putExtra("service", amenity.getCategory());
                        intent.putExtra("description", amenity.getDescription());
                        context.startActivity(intent);
                        break;

                    case 1: //cancel
                        dialog.dismiss();
                        break;
                }
            });

            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return amenityList.size();
    }

    public static class AmenityViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvDescription;

        public AmenityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}
