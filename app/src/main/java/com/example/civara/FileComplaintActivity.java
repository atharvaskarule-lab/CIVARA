package com.example.civara;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class FileComplaintActivity extends AppCompatActivity {

    TextInputEditText etDescription;
    Chip chipGarbage, chipRoad, chipWater;
    MaterialCardView btnUploadImage;
    ImageView ivPreview;
    TextView tvUploadStatus;
    Uri imageUri;

    FusedLocationProviderClient fusedLocationClient;
    double latitude = 0.0, longitude = 0.0;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_complaint);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Bind UI
        etDescription = findViewById(R.id.etDescription);
        chipGarbage = findViewById(R.id.chipGarbage);
        chipRoad = findViewById(R.id.chipRoad);
        chipWater = findViewById(R.id.chipWater);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        ivPreview = findViewById(R.id.ivPreview);
        tvUploadStatus = findViewById(R.id.tvUploadStatus);

        findViewById(R.id.btnSubmitComplaint).setOnClickListener(v -> submitComplaint());

        btnUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 100);
        });

        checkLocationPermission();
    }

    private void submitComplaint() {
        String description = etDescription.getText().toString().trim();
        StringBuilder typeBuilder = new StringBuilder();

        if (chipGarbage.isChecked()) typeBuilder.append("Garbage ");
        if (chipRoad.isChecked()) typeBuilder.append("Road Damage ");
        if (chipWater.isChecked()) typeBuilder.append("Water Supply ");

        if (description.isEmpty() || typeBuilder.length() == 0) {
            Toast.makeText(this, "Please select a category and provide a description", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> complaint = new HashMap<>();
        complaint.put("userId", FirebaseAuth.getInstance().getUid());
        complaint.put("description", description);
        complaint.put("type", typeBuilder.toString().trim());
        complaint.put("latitude", latitude);
        complaint.put("longitude", longitude);
        complaint.put("status", "Pending");
        complaint.put("timestamp", System.currentTimeMillis());

        if (imageUri != null) complaint.put("imageUri", imageUri.toString());

        db.collection("complaints").add(complaint).addOnSuccessListener(ref -> {
            Toast.makeText(this, "Complaint Filed Successfully!", Toast.LENGTH_SHORT).show();
            finish();
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
            tvUploadStatus.setText("Image selected successfully");
        }
    }
}