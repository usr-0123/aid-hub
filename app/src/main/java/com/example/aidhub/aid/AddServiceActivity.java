package com.example.aidhub.aid;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aidhub.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddServiceActivity extends AppCompatActivity {

    private EditText titleEditText, descriptionEditText;
    private Button saveButton;
    private DatabaseReference servicesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);

        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        saveButton = findViewById(R.id.saveButton);

        servicesRef = FirebaseDatabase.getInstance().getReference("services");

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addService();
            }
        });
    }

    private void addService() {
        String title = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String id = servicesRef.push().getKey();

        if (id != null && !title.isEmpty() && !description.isEmpty()) {
            ServiceModel service = new ServiceModel(id, title, description);
            servicesRef.child(id).setValue(service);
            Toast.makeText(this, "Service added successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
    }
}
