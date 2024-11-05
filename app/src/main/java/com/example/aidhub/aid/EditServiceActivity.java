package com.example.aidhub.aid;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aidhub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditServiceActivity extends AppCompatActivity {

    private EditText titleEditText, descriptionEditText;
    private Button saveButton;
    private DatabaseReference servicesRef;
    private String serviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_service);

        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        saveButton = findViewById(R.id.saveButton);

        // Get the service ID from the intent
        serviceId = getIntent().getStringExtra("serviceId");

        // Initialize Firebase reference
        servicesRef = FirebaseDatabase.getInstance().getReference("services");

        // Load the existing service data
        loadServiceData();

        // Save updated service details
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateService();
            }
        });
    }

    private void loadServiceData() {
        servicesRef.child(serviceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ServiceModel service = snapshot.getValue(ServiceModel.class);
                if (service != null) {
                    titleEditText.setText(service.getTitle());
                    descriptionEditText.setText(service.getDescription());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditServiceActivity.this, "Failed to load service data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateService() {
        String updatedTitle = titleEditText.getText().toString();
        String updatedDescription = descriptionEditText.getText().toString();

        if (!updatedTitle.isEmpty() && !updatedDescription.isEmpty()) {
            ServiceModel updatedService = new ServiceModel(serviceId, updatedTitle, updatedDescription);
            servicesRef.child(serviceId).setValue(updatedService)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EditServiceActivity.this, "Service updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(EditServiceActivity.this, "Failed to update service", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
    }
}
