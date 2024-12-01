package com.example.aidhub.aid;

import static com.example.aidhub.notification.AidNotificationHelper.showAidNotification;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aidhub.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class AidFragment extends Fragment {

    private Spinner serviceSpinner;
    private EditText descriptionEditText;
    private Button requestButton;
    private DatabaseReference servicesRef;
    private List<ServiceModel> serviceList;
    private ArrayAdapter<String> serviceAdapter;
    private String selectedService, seekerId;
    private String userLatitude, userLongitude;
    private RecyclerView aidRequestsRecyclerView;
    private AidRequestAdapter aidRequestAdapter;
    private List<AidRequestModel> aidRequestList;
    private FusedLocationProviderClient fusedLocationClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_aid, container, false);

        // Get the FirebaseAuth instance
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        // Get the currently logged-in user
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            // User is logged in, retrieve user information
            seekerId = currentUser.getUid();
        }

        // Initialize Firebase Database reference
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("chats");

        serviceSpinner = rootView.findViewById(R.id.serviceSpinner);
        descriptionEditText = rootView.findViewById(R.id.descriptionEditText);
        requestButton = rootView.findViewById(R.id.requestButton);

        servicesRef = FirebaseDatabase.getInstance().getReference("services");
        serviceList = new ArrayList<>();
        serviceAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceSpinner.setAdapter(serviceAdapter);

        fetchServices();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        requestLocation();

        requestButton.setOnClickListener(v -> submitRequest());

        aidRequestsRecyclerView = rootView.findViewById(R.id.aidRequestsRecyclerView);
        aidRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        aidRequestList = new ArrayList<>();
        aidRequestAdapter = new AidRequestAdapter(aidRequestList, getContext(), chatsRef, seekerId);
        aidRequestsRecyclerView.setAdapter(aidRequestAdapter);

        fetchAidRequests(); // Fetch and display existing aid requests

        return rootView;
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
                Toast.makeText(getContext(), "Failed to load services.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        userLatitude = String.valueOf(location.getLatitude());
                        userLongitude = String.valueOf(location.getLongitude());
                    } else {
                        Toast.makeText(getContext(), "Last known location is null. Trying to get current location.", Toast.LENGTH_SHORT).show();
                        requestCurrentLocation();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to get location.", Toast.LENGTH_SHORT).show());
    }

    private void requestCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(5000) // 5 seconds interval
                    .setFastestInterval(2000); // 2 seconds interval

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } else {
            Toast.makeText(getContext(), "Location permission required.", Toast.LENGTH_SHORT).show();
        }
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                userLatitude = String.valueOf(location.getLatitude());
                userLongitude = String.valueOf(location.getLongitude());
            }
            fusedLocationClient.removeLocationUpdates(this); // Stop updates after first location is received
        }
    };

    private void submitRequest() {
        selectedService = serviceSpinner.getSelectedItem() != null ? serviceSpinner.getSelectedItem().toString() : null;
        String description = descriptionEditText.getText().toString();

        if (TextUtils.isEmpty(selectedService)) {
            Toast.makeText(getContext(), "Please select a service.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            Toast.makeText(getContext(), "Please describe your aid.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(userLatitude) || TextUtils.isEmpty(userLongitude)) {
            Toast.makeText(getContext(), "Location not selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference("aid_requests");
        String requestId = requestsRef.push().getKey();
        Boolean approved = false;
        List<String> readBy = new ArrayList<>();
        readBy.add(seekerId);

        AidRequestModel aidRequest = new AidRequestModel(requestId, selectedService, description, userLatitude, userLongitude, seekerId, approved, readBy);
        requestsRef.child(requestId).setValue(aidRequest)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Aid request submitted successfully", Toast.LENGTH_SHORT).show();

                    // Clear the fields
                    descriptionEditText.setText("");
                    serviceSpinner.setSelection(0);
                    userLatitude = null;
                    userLongitude = null;
                    seekerId = null;
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to submit aid request", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            } else {
                Toast.makeText(getContext(), "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchAidRequests() {
        DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference("aid_requests");
        requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                aidRequestList.clear();
                for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                    AidRequestModel aidRequest = requestSnapshot.getValue(AidRequestModel.class);
                    if (aidRequest != null && !Boolean.TRUE.equals(aidRequest.getApproved())) {
                        aidRequestList.add(aidRequest);

                        if (aidRequest.getReadBy() !=null && aidRequest.getReadBy().contains(seekerId)) {
                            // Send notification of added request.
                            showAidNotification(getContext(), aidRequest.getService(), aidRequest.getDescription());

                            // Mark this aid as read
                            List<String> readByList = new ArrayList<>();

                            readByList.add(seekerId);

                            if (readByList.isEmpty()) {
                                Toast.makeText(getContext(), "The readBy list is empty.", Toast.LENGTH_SHORT).show();
                            }

                            // Update the "readBy" field in Firebase with the updated list
                            requestsRef.child(aidRequest.getRequestId()).child("readBy").setValue(readByList).addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Aid marked as read.", Toast.LENGTH_SHORT).show();
                            }).addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Unable to mark aid as read.", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                }
                aidRequestAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load aid requests.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

