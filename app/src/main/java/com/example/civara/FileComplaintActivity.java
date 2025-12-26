package com.example.civara;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FileComplaintActivity extends AppCompatActivity {

    EditText etDescription;
    CheckBox cbGarbage, cbRoadDamage, cbWaterSupply;
    Button btnUploadImage, btnSubmitComplaint;
    Uri imageUri;

    FusedLocationProviderClient fusedLocationClient;
    double latitude = 0.0, longitude = 0.0;

    FirebaseAuth auth;
    FirebaseFirestore db;

    private static final int LOCATION_PERMISSION_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_complaint);

        // 🔹 Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 🔹 UI references
        etDescription = findViewById(R.id.etDescription);
        cbGarbage = findViewById(R.id.cbGarbage);
        cbRoadDamage = findViewById(R.id.cbRoadDamage);
        cbWaterSupply = findViewById(R.id.cbWaterSupply);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnSubmitComplaint = findViewById(R.id.btnSubmitComplaint);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 🔹 Image picker
        btnUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 100);
        });

        // 🔹 Submit complaint
        btnSubmitComplaint.setOnClickListener(v -> submitComplaint());

        // 🔹 Ask for location permission
        checkLocationPermission();
    }

    // ================= LOCATION PERMISSION =================

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST
            );
        } else {
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this,
                        "Location permission denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getLocation() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    // ================= SUBMIT COMPLAINT =================

    private void submitComplaint() {

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this,
                    "Please login first",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String description = etDescription.getText().toString().trim();

        String type = "";
        if (cbGarbage.isChecked()) type += "Garbage ";
        if (cbRoadDamage.isChecked()) type += "Road Damage ";
        if (cbWaterSupply.isChecked()) type += "Water Supply ";

        if (description.isEmpty() || type.isEmpty()) {
            Toast.makeText(this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> complaint = new HashMap<>();
        complaint.put("userId", userId);
        complaint.put("description", description);
        complaint.put("type", type.trim());
        complaint.put("latitude", latitude);
        complaint.put("longitude", longitude);
        complaint.put("status", "Pending");
        complaint.put("timestamp", System.currentTimeMillis());

        if (imageUri != null) {
            complaint.put("imageUri", imageUri.toString());
        }

        db.collection("complaints")
                .add(complaint)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this,
                            "Complaint Submitted",
                            Toast.LENGTH_SHORT).show();

                    // Clear form
                    etDescription.setText("");
                    cbGarbage.setChecked(false);
                    cbRoadDamage.setChecked(false);
                    cbWaterSupply.setChecked(false);

                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // ================= IMAGE PICK RESULT =================

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 &&
                resultCode == RESULT_OK &&
                data != null &&
                data.getData() != null) {

            imageUri = data.getData();
            Toast.makeText(this,
                    "Image Selected",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
