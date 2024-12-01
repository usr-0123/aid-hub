package com.example.aidhub.amenities.form;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import com.example.aidhub.R;
import com.example.aidhub.aid.ServiceModel;
import com.example.aidhub.amenities.amenityModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NewAmenityActivity extends AppCompatActivity {
    Spinner etCategory;
    private EditText etAmenityName, etDescription;
    private Button btnSaveAmenity, btnAmenityLocation;
    DatabaseReference amenitiesReference, servicesRef;
    private List<ServiceModel> serviceList;
    private ArrayAdapter<String> serviceAdapter;
    private static final int PICK_LOCATION_REQUEST = 1;
    private double longitudeVal,latitudeVal = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_amenity);

        etAmenityName = findViewById(R.id.etAmenityName);
        etCategory = findViewById(R.id.etCategory);
        etDescription = findViewById(R.id.etDescription);
        btnSaveAmenity = findViewById(R.id.btnSaveAmenity);
        btnAmenityLocation = findViewById(R.id.btnAmenityLocation);

        amenitiesReference = FirebaseDatabase.getInstance().getReference("amenities");
        servicesRef = FirebaseDatabase.getInstance().getReference("services");
        serviceList = new ArrayList<>();
        serviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        etCategory.setAdapter(serviceAdapter);

        fetchServices();

        btnSaveAmenity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAmenity();
            }
        });

        btnAmenityLocation.setOnClickListener(v -> {
            Intent intent = new Intent(NewAmenityActivity.this, PickLocationActivity.class);
            startActivityForResult(intent, PICK_LOCATION_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_LOCATION_REQUEST && resultCode == RESULT_OK) {
            latitudeVal = data.getDoubleExtra("latitude", 0.0);
            longitudeVal = data.getDoubleExtra("longitude", 0.0);

            Toast.makeText(this, "Location Selected: " + latitudeVal + ", " + longitudeVal, Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchServices() {
        servicesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> serviceNames = new ArrayList<>();
                serviceList.clear();
                for (DataSnapshot serviceSnapshot : snapshot.getChildren()) {
                    ServiceModel service = serviceSnapshot.getValue(ServiceModel.class);
                    if (service != null) {
                        serviceList.add(service);
                        serviceNames.add(service.getTitle());
                    }
                }
                serviceAdapter.clear();
                serviceAdapter.addAll(serviceNames);
                serviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(this, "Failed to load services.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAmenity() {
        if (latitudeVal == 0.0 && longitudeVal == 0.0) {
            Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etAmenityName.getText().toString().trim();
        String category = etCategory.getSelectedItem() != null ? etCategory.getSelectedItem().toString() : null;
        String description = etDescription.getText().toString().trim();
        String latitude = String.valueOf(latitudeVal);
        String longitude = String.valueOf(longitudeVal);

        String amenityId = amenitiesReference.push().getKey();

        amenityModel newAmenity = new amenityModel(amenityId, name, category, description, latitude, longitude);

        // Save to Firebase Realtime Database
        amenitiesReference.child(amenityId).setValue(newAmenity).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Amenity saved successfully!", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity
            } else {
                Toast.makeText(this, "Failed to save amenity. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

    }
}