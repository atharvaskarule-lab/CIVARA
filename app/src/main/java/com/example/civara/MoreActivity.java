package com.example.civara;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MoreActivity extends AppCompatActivity {

    private ImageView ivProfilePic;
    private TextView tvProfileName, tvProfileEmail;
    private LinearLayout layoutAccount, layoutLogout, layoutAbout, layoutPrivacy, layoutLanguage, layoutTheme, layoutHelpFeedback;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_more);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind Views
        ivProfilePic = findViewById(R.id.ivProfilePic);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        layoutAccount = findViewById(R.id.layoutAccount);
        layoutAbout = findViewById(R.id.layoutAbout);
        layoutLanguage = findViewById(R.id.layoutLanguage);
        layoutTheme = findViewById(R.id.layoutTheme);
        layoutPrivacy = findViewById(R.id.layoutPrivacy);
        layoutLogout = findViewById(R.id.layoutLogout);
        layoutHelpFeedback = findViewById(R.id.layoutHelpFeedback);

        loadUserData();

        // Click Listeners
        layoutAccount.setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));
        layoutAbout.setOnClickListener(v -> startActivity(new Intent(this, AboutUsActivity.class)));
        layoutLanguage.setOnClickListener(v -> showLanguageDialog());
        layoutTheme.setOnClickListener(v -> showThemeDialog());
        layoutPrivacy.setOnClickListener(v -> startActivity(new Intent(this, PrivacyActivity.class)));

        layoutLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(MoreActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Fixed Feedback Listener
        layoutHelpFeedback.setOnClickListener(v -> showFeedbackDialog());
    }

    private void showFeedbackDialog() {
        final android.widget.EditText etFeedback = new android.widget.EditText(this);
        etFeedback.setHint("Write your feedback here...");
        etFeedback.setLines(4);
        etFeedback.setGravity(android.view.Gravity.TOP);

        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = 50;
        params.rightMargin = 50;
        etFeedback.setLayoutParams(params);
        container.addView(etFeedback);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Help & Feedback")
                .setMessage("How can we help you? Please provide your feedback below:")
                .setView(container)
                .setPositiveButton("Send", (dialog, which) -> {
                    String feedbackText = etFeedback.getText().toString().trim();
                    if (!feedbackText.isEmpty()) {
                        saveFeedbackToFirestore(feedbackText);
                    } else {
                        Toast.makeText(this, "Please write something first!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveFeedbackToFirestore(String message) {
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();
        String email = mAuth.getCurrentUser().getEmail();

        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("uid", uid);
        feedbackData.put("email", email);
        feedbackData.put("feedback", message);
        feedbackData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("feedbacks")
                .add(feedbackData)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(MoreActivity.this, "Feedback sent! Thank you.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(MoreActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "मराठी (Marathi)", "हिन्दी (Hindi)"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Language")
                .setItems(languages, (dialog, which) -> {
                    if (which == 0) setLocale("en");
                    else if (which == 1) setLocale("mr");
                    else if (which == 2) setLocale("hi");
                }).show();
    }

    private void showThemeDialog() {
        String[] themes = {"Light Mode", "Dark Mode", "System Default"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Choose Theme")
                .setItems(themes, (dialog, which) -> {
                    int mode = (which == 0) ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO :
                            (which == 1) ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES :
                                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                    ThemeHelper.applyTheme(this, mode);
                    recreate();
                }).show();
    }

    private void setLocale(String langCode) {
        java.util.Locale locale = new java.util.Locale(langCode);
        java.util.Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        Intent intent = new Intent(this, MoreActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        tvProfileEmail.setText(mAuth.getCurrentUser().getEmail());

        db.collection("users").document(uid).addSnapshotListener((doc, error) -> {
            if (error != null || doc == null || !doc.exists()) return;
            tvProfileName.setText(doc.getString("name"));
            String imageString = doc.getString("profileImageUrl");
            if (imageString != null && !imageString.isEmpty()) decodeAndSetImage(imageString);
        });
    }

    private void decodeAndSetImage(String encodedData) {
        try {
            byte[] decodedString = Base64.decode(encodedData, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ivProfilePic.setImageBitmap(decodedByte);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} // Class ends here