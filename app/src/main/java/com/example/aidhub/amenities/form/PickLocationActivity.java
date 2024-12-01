package com.example.aidhub.amenities.form;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import com.example.aidhub.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PickLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker currentMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_location);

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Confirm location button
        Button btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        btnConfirmLocation.setOnClickListener(v -> {
            if (currentMarker != null) {
                LatLng selectedLocation = currentMarker.getPosition();

                // Pass the location back to the previous activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", selectedLocation.latitude);
                resultIntent.putExtra("longitude", selectedLocation.longitude);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Default location (e.g., New York)
        LatLng defaultLocation = new LatLng(-1.286389, 36.817223); // Example: Nairobi, Kenya
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));

        // Add marker on tap
        mMap.setOnMapClickListener(latLng -> {
            if (currentMarker != null) {
                currentMarker.remove();
            }
            currentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
        });
    }
}
