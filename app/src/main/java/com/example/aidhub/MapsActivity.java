package com.example.aidhub;

import androidx.fragment.app.FragmentActivity;
import android.content.Intent;
import android.os.Bundle;
import com.example.aidhub.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private double latitude;
    private double longitude;
    private String service;
    private String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve intent extras
        Intent intent = getIntent();
        latitude = Double.parseDouble(intent.getStringExtra("latitude"));
        longitude = Double.parseDouble(intent.getStringExtra("longitude"));
        service = intent.getStringExtra("service");
        description = intent.getStringExtra("description");

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Create a LatLng object with the passed latitude and longitude
        LatLng requestLocation = new LatLng(latitude, longitude);

        // Add a marker with service name and description as the title
        mMap.addMarker(new MarkerOptions()
                .position(requestLocation)
                .title(service)
                .snippet(description));

        // Move the camera to the location and zoom in
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(requestLocation, 15));
    }
}
