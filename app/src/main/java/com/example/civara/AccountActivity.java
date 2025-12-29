package com.example.civara;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPhone, etCity;
    private Button btnSaveChanges, btnChangePhoto, btnDeleteAccount;
    private LinearLayout layoutChangePassword;
    private ImageButton btnBack;
    private ShapeableImageView ivProfileImage;
    private ProgressBar pbLoading;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // IMAGE PICKER
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    processAndUploadImage(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.account_activity);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();
        observeUserData();
    }

    private void initViews() {
        ivProfileImage = findViewById(R.id.ivProfileImage);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etCity = findViewById(R.id.etCity);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        layoutChangePassword = findViewById(R.id.layoutChangePassword);
        btnBack = findViewById(R.id.btnBack);
        pbLoading = findViewById(R.id.pbLoading);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSaveChanges.setOnClickListener(v -> saveChanges());
        btnChangePhoto.setOnClickListener(v -> mGetContent.launch("image/*"));
        layoutChangePassword.setOnClickListener(v -> sendPasswordReset());
    }

    private void observeUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        etEmail.setText(user.getEmail());

        db.collection("users").document(user.getUid())
                .addSnapshotListener((doc, error) -> {
                    if (doc != null && doc.exists()) {
                        if (!etName.hasFocus()) etName.setText(doc.getString("name"));
                        if (!etPhone.hasFocus()) etPhone.setText(doc.getString("phone"));
                        if (!etCity.hasFocus()) etCity.setText(doc.getString("city"));

                        String imageString = doc.getString("profileImageUrl");
                        if (imageString != null && !imageString.isEmpty()) {
                            decodeAndSetImage(imageString);
                        }
                    }
                });
    }

    // New Method: Converts URI to Base64 and saves to Firestore
    private void processAndUploadImage(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

            // Resize to keep Firestore document size small (under 1MB)
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] byteArray = baos.toByteArray();

            String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid)
                    .update("profileImageUrl", encodedImage)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Photo updated!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    // New Method: Converts Base64 string back to an Image
    private void decodeAndSetImage(String completeImageData) {
        try {
            byte[] decodedString = Base64.decode(completeImageData, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ivProfileImage.setPadding(0,0,0,0);
            ivProfileImage.setImageBitmap(decodedByte);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveChanges() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        setLoading(true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", etName.getText().toString().trim());
        updates.put("phone", etPhone.getText().toString().trim());
        updates.put("city", etCity.getText().toString().trim());

        db.collection("users").document(user.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setLoading(boolean isLoading) {
        btnSaveChanges.setText(isLoading ? "" : "Save Changes");
        btnSaveChanges.setEnabled(!isLoading);
        pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void sendPasswordReset() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            mAuth.sendPasswordResetEmail(user.getEmail())
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Reset email sent!", Toast.LENGTH_LONG).show());
        }
    }
}