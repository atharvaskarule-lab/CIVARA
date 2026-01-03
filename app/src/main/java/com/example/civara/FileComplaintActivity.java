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
    private Chip chipGarbage, chipRoad, chipWater, chipSanitation, chipStreet, chipPark, chipOtherGeneral;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_complaint);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Bind UI Elements
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
        loadingOverlay = findViewById(R.id.loadingOverlay);

        btnUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 100);
        });

        btnSubmit.setOnClickListener(v -> validateAndSubmit());
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
        saveToFirestore(description, categories);
    }

    private void saveToFirestore(String description, List<String> categories) {
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

        Map<String, Object> complaint = new HashMap<>();
        String name = (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty())
                ? user.getDisplayName() : (user != null ? user.getEmail() : "Anonymous User");

        complaint.put("name", name);
        complaint.put("date", currentDate);
        complaint.put("title", categories.get(0));
        complaint.put("description", description);
        complaint.put("categories", categories);
        complaint.put("status", "Pending");
        complaint.put("latitude", latitude);
        complaint.put("longitude", longitude);
        complaint.put("timestamp", System.currentTimeMillis());
        complaint.put("userId", user != null ? user.getUid() : "anonymous");

        if (!base64Image.isEmpty()) {
            complaint.put("imageUrl", base64Image);
        }

        db.collection("complaints").add(complaint)
                .addOnSuccessListener(ref -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    showSuccessDialog(); // Show Dialog instead of Toast
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
                .setMessage("Your issue has been recorded successfully. You can track the status in the 'My Complaints' section.")
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
            ivPreview.setScaleType(ImageView.ScaleType.CENTER_CROP); // Improved preview look

            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                // Firestore string limit is strict. 30% quality ensures document remains < 1MB.
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