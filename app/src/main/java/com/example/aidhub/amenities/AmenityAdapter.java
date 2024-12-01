package com.example.aidhub.amenities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aidhub.R;

import java.util.List;

public class AmenityAdapter extends RecyclerView.Adapter<AmenityAdapter.AmenityViewHolder> {

    private List<amenityModel> amenityList;

    public AmenityAdapter(List<amenityModel> amenityList) {
        this.amenityList = amenityList;
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
