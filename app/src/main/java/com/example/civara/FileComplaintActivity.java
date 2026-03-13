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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
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

    private static final double LOCATION_THRESHOLD = 0.0005; // Approx 50 meters

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

        btnSubmit.setOnClickListener(v -> validateAndCheckDuplicates());
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
        categoryAdmins.put("Garbage", "Mr. Rupesh Bhujade");
        categoryAdmins.put("Road Damage", "Ms. Ayush Karwade");
        categoryAdmins.put("Water Supply", "Dr. Mousmi Joshi");
        categoryAdmins.put("Sanitation", "Mr. Gaurav Patil");
        categoryAdmins.put("Street Lighting", "Ms. Ashlesh Sorte");
        categoryAdmins.put("Parks And Public's Places", "Mr. Sahil Karwade");
        categoryAdmins.put("Other General Complaints", "General Admin Support");
    }

    private void validateAndCheckDuplicates() {
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

        if (latitude == 0.0 && longitude == 0.0) {
            Toast.makeText(this, "Fetching location, please wait...", Toast.LENGTH_SHORT).show();
            getLocation();
            return;
        }

        btnSubmit.setEnabled(false);
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);

        // Check for existing similar complaints in the same area
        String selectedCat = categories.get(0);
        db.collection("complaints")
                .whereEqualTo("category", selectedCat)
                .whereEqualTo("status", "Pending")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot similarComplaint = null;
                        for (DocumentSnapshot doc : task.getResult()) {
                            double compLat = doc.getDouble("latitude");
                            double compLong = doc.getDouble("longitude");
                            
                            if (Math.abs(compLat - latitude) < LOCATION_THRESHOLD && 
                                Math.abs(compLong - longitude) < LOCATION_THRESHOLD) {
                                similarComplaint = doc;
                                break;
                            }
                        }

                        if (similarComplaint != null) {
                            if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                            showDuplicateFoundDialog(similarComplaint.getId(), similarComplaint.getString("description"));
                        } else {
                            saveToFirestore(description, categories);
                        }
                    } else {
                        saveToFirestore(description, categories);
                    }
                });
    }

    private void showDuplicateFoundDialog(String complaintId, String description) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Similar Complaint Found")
                .setMessage("A similar complaint has already been filed at this location:\n\n\"" + description + "\"\n\nWould you like to support/vote for this complaint instead of filing a new one?")
                .setPositiveButton("Vote & Support", (dialog, which) -> {
                    voteForExistingComplaint(complaintId);
                })
                .setNegativeButton("File Anyway", (dialog, which) -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
                    String desc = etDescription.getText().toString().trim();
                    List<String> categories = new ArrayList<>();
                    for (int id : chipGroupType.getCheckedChipIds()) {
                        Chip chip = findViewById(id);
                        if (chip != null) categories.add(chip.getText().toString());
                    }
                    saveToFirestore(desc, categories);
                })
                .setNeutralButton("Cancel", (dialog, which) -> btnSubmit.setEnabled(true))
                .show();
    }

    private void voteForExistingComplaint(String complaintId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        
        String uid = user.getUid();
        db.collection("complaints").document(complaintId)
                .update("voterIds", FieldValue.arrayUnion(uid),
                        "voteCount", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Vote recorded! Thank you for your support.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Error voting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToFirestore(String description, List<String> categories) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            return;
        }

        String currentUserId = user.getUid();
        String userEmail = user.getEmail() != null ? user.getEmail() : "No Email";
        String userName = user.getDisplayName();
        String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

        Map<String, Object> complaint = new HashMap<>();
        complaint.put("userId", currentUserId);
        complaint.put("email", userEmail);
        complaint.put("name", userName != null ? userName : userEmail);
        complaint.put("imageUrl", base64Image);
        complaint.put("date", currentDate);
        complaint.put("description", description);
        complaint.put("status", "Pending");
        complaint.put("timestamp", System.currentTimeMillis());
        complaint.put("latitude", latitude);
        complaint.put("longitude", longitude);
        
        List<String> voters = new ArrayList<>();
        voters.add(currentUserId);
        complaint.put("voterIds", voters);
        complaint.put("voteCount", 1);

        if (!categories.isEmpty()) {
            String selectedCat = categories.get(0);
            complaint.put("category", selectedCat);
            complaint.put("title", selectedCat);
        }

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