package com.example.civara;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FileComplaintActivity extends AppCompatActivity {

    private TextInputEditText etDescription;
    private ChipGroup chipGroupType;
    private TextView tvAdminName;
    private MaterialCardView btnUploadImage;
    private ImageView ivPreview;
    private TextView tvUploadStatus;
    private MaterialButton btnSubmit;
    private FrameLayout loadingOverlay;

    private Uri imageUri;
    private String base64Image = "";

    private FusedLocationProviderClient fusedLocationClient;
    private double latitude = 0.0, longitude = 0.0;
    private FirebaseFirestore db;

    private Map<String, String> categoryDescriptions;
    private Map<String, String> categoryAdmins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_complaint);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initializeCategoryData();

        etDescription = findViewById(R.id.etDescription);
        chipGroupType = findViewById(R.id.chipGroupType);
        tvAdminName = findViewById(R.id.tvAdminName);
        btnSubmit = findViewById(R.id.btnSubmitComplaint);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        ivPreview = findViewById(R.id.ivPreview);
        tvUploadStatus = findViewById(R.id.tvUploadStatus);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        chipGroupType.setOnCheckedStateChangeListener((group, checkedChipIds) -> {
            etDescription.setText("");
            tvAdminName.setVisibility(View.GONE);

            if (!checkedChipIds.isEmpty()) {
                Chip selectedChip = findViewById(checkedChipIds.get(0));
                String category = selectedChip.getText().toString();

                String description = categoryDescriptions.get(category);
                if (description != null) {
                    etDescription.setText(description);
                }

                String admin = categoryAdmins.get(category);
                if (admin != null) {
                    tvAdminName.setText("Admin contact: " + admin);
                    tvAdminName.setVisibility(View.VISIBLE);
                }
            }
        });

        btnUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 100);
        });

        btnSubmit.setOnClickListener(v -> validateAndSubmit());
        checkLocationPermission();
    }

    private void initializeCategoryData() {
        categoryDescriptions = new HashMap<>();
        categoryDescriptions.put("Garbage", "Please describe the location and nature of the garbage issue...");
        categoryDescriptions.put("Road Damage", "Please specify the type of road damage...");
        categoryDescriptions.put("Water Supply", "Detail the water supply issue...");
        categoryDescriptions.put("Sanitation", "Describe any sanitation concerns...");
        categoryDescriptions.put("Street Lighting", "Report non-functional street lights...");
        categoryDescriptions.put("Parks And Public's Places", "Explain issues in parks...");
        categoryDescriptions.put("Other General Complaints", "Detailed description of other complaints...");

        categoryAdmins = new HashMap<>();
        categoryAdmins.put("Garbage", "Mr. John Doe");
        categoryAdmins.put("Road Damage", "Ms. Jane Smith");
        categoryAdmins.put("Water Supply", "Dr. Robert Johnson");
        categoryAdmins.put("Sanitation", "Mr. David Lee");
        categoryAdmins.put("Street Lighting", "Ms. Emily White");
        categoryAdmins.put("Parks And Public's Places", "Mr. Michael Brown");
        categoryAdmins.put("Other General Complaints", "General Admin Support");
    }

    private void validateAndSubmit() {
        String description = etDescription.getText().toString().trim();
        List<String> categories = new ArrayList<>();

        for (int id : chipGroupType.getCheckedChipIds()) {
            Chip chip = findViewById(id);
            if (chip != null) {
                String categoryText = chip.getText().toString();
                if (categoryText.equals("Parks And Public's Places")) {
                    categories.add("Parks");
                } else if (categoryText.equals("Other General Complaints")) {
                    categories.add("Other");
                } else {
                    categories.add(categoryText);
                }
            }
        }

        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            return;
        }
        if (categories.isEmpty()) {
            Toast.makeText(this, "Please select at least one category", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        saveToFirestore(description, categories);
    }

    // UPDATED METHOD
    private void saveToFirestore(String description, List<String> categories) {
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            return;
        }

        String currentUserId = user.getUid();
        String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

        Map<String, Object> complaint = new HashMap<>();

        complaint.put("userId", currentUserId);
        complaint.put("name", user.getDisplayName() != null ? user.getDisplayName() : "Anonymous");
        complaint.put("imageUrl", base64Image);
        complaint.put("date", currentDate);
        complaint.put("description", description);
        complaint.put("status", "Pending");
        complaint.put("timestamp", System.currentTimeMillis());
        complaint.put("latitude", latitude);
        complaint.put("longitude", longitude);

        if (!categories.isEmpty()) {
            String selectedCat = categories.get(0);
            complaint.put("category", selectedCat);
            complaint.put("title", selectedCat);
        }

        // Collection name should match ViewComplaintsActivity
        db.collection("complaints").add(complaint)
                .addOnSuccessListener(ref -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    showSuccessDialog();
                })
                .addOnFailureListener(e -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Complaint Filed")
                .setMessage("Your issue has been recorded successfully.")
                .setCancelable(false)
                .setPositiveButton("Finish", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            ivPreview.setImageURI(imageUri);
            ivPreview.setVisibility(View.VISIBLE);
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
                byte[] imageBytes = baos.toByteArray();
                base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                tvUploadStatus.setText("Photo attached");
            } catch (Exception e) {
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            }
        }
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
}