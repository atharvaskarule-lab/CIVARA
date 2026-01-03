package com.example.civara;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FileComplaintActivity extends AppCompatActivity {

    private TextInputEditText etDescription;
    private Chip chipGarbage, chipRoad, chipWater, chipSanitation, chipStreet, chipPark, chipOtherGeneral;
    private MaterialCardView btnUploadImage;
    private ImageView ivPreview;
    private TextView tvUploadStatus;
    private MaterialButton btnSubmit;
    private Uri imageUri;

    private FusedLocationProviderClient fusedLocationClient;
    private double latitude = 0.0, longitude = 0.0;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_complaint);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Bind UI
        etDescription = findViewById(R.id.etDescription);
        chipGarbage = findViewById(R.id.chipGarbage);
        chipRoad = findViewById(R.id.chipRoad);
        chipWater = findViewById(R.id.chipWater);
        chipSanitation = findViewById(R.id.chipSanitation);
        chipStreet = findViewById(R.id.chipStreet);
        chipPark = findViewById(R.id.chipPark);
        chipOtherGeneral = findViewById(R.id.chipOtherGeneral);
        btnSubmit = findViewById(R.id.btnSubmitComplaint);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        ivPreview = findViewById(R.id.ivPreview);
        tvUploadStatus = findViewById(R.id.tvUploadStatus);

        btnSubmit.setOnClickListener(v -> validateAndSubmit());

        btnUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 100);
        });

        checkLocationPermission();
    }

    private void validateAndSubmit() {
        String description = etDescription.getText().toString().trim();
        List<String> categories = new ArrayList<>();

        if (chipGarbage.isChecked()) categories.add("Garbage");
        if (chipRoad.isChecked()) categories.add("Road Damage");
        if (chipWater.isChecked()) categories.add("Water Supply");
        if (chipSanitation.isChecked()) categories.add("Sanitation");
        if (chipStreet.isChecked()) categories.add("Street Lighting");
        if (chipPark.isChecked()) categories.add("Parks");
        if (chipOtherGeneral.isChecked()) categories.add("Other");

        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            return;
        }
        if (categories.isEmpty()) {
            Toast.makeText(this, "Please select at least one category", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        if (imageUri != null) {
            uploadImageAndSubmit(description, categories);
        } else {
            saveToFirestore(description, categories, null);
        }
    }

    private void uploadImageAndSubmit(String desc, List<String> cats) {
        StorageReference ref = storage.getReference().child("complaints/" + UUID.randomUUID().toString());
        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveToFirestore(desc, cats, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit Complaint");
                    Toast.makeText(this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToFirestore(String description, List<String> categories, String imageUrl) {
        Map<String, Object> complaint = new HashMap<>();
        complaint.put("userId", FirebaseAuth.getInstance().getUid());
        complaint.put("description", description);
        complaint.put("categories", categories);
        complaint.put("latitude", latitude);
        complaint.put("longitude", longitude);
        complaint.put("status", "Pending");
        complaint.put("timestamp", System.currentTimeMillis());
        if (imageUrl != null) complaint.put("imageUrl", imageUrl);

        db.collection("complaints").add(complaint)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Complaint Filed Successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit Complaint");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        } else {
            getLocation();
        }
    }

    private void getLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            });
        } catch (SecurityException e) { e.printStackTrace(); }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            ivPreview.setImageURI(imageUri);
            ivPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
            tvUploadStatus.setText("Image selected");
        }
    }
}