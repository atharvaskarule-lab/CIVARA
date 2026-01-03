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
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MoreActivity extends AppCompatActivity {

    private ImageView ivProfilePic;
    private TextView tvProfileName, tvProfileEmail;
    // Added layoutPrivacy here
    private LinearLayout layoutAccount, layoutLogout, layoutAbout,layoutPrivacy,layoutLanguage,layoutTheme,layoutHelpFeedback;
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
        layoutLanguage = findViewById(R.id.layoutLanguage); // Bind Language Layout
        layoutTheme = findViewById(R.id.layoutTheme);
        layoutPrivacy = findViewById(R.id.layoutPrivacy); // Bind Privacy Layout
        layoutLogout = findViewById(R.id.layoutLogout);
        layoutHelpFeedback = findViewById(R.id.layoutHelpFeedback);

        loadUserData();

        // 1. Account Settings
        layoutAccount.setOnClickListener(v -> {
            startActivity(new Intent(MoreActivity.this, AccountActivity.class));
        });

        // 2. About Us
        layoutAbout.setOnClickListener(v -> {
            startActivity(new Intent(MoreActivity.this, AboutUsActivity.class));
        });

        layoutLanguage.setOnClickListener(v -> {
            showLanguageDialog();
        });
        // Click Listener
        layoutTheme.setOnClickListener(v -> {
            showThemeDialog();
        });

        // 3. Privacy Settings (New)
        layoutPrivacy.setOnClickListener(v -> {
            startActivity(new Intent(MoreActivity.this, PrivacyActivity.class));
        });

        // 4. Logout
        layoutLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(MoreActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        layoutHelpFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@civara.com"}); // Tumcha email taka
            intent.putExtra(Intent.EXTRA_SUBJECT, "Civara App - Help & Feedback");

            try {
                startActivity(Intent.createChooser(intent, "Choose an Email client :"));
            } catch (android.content.ActivityNotFoundException ex) {
                // Email app nasel tr error handle kara
            }
        });
    }
    private void showLanguageDialog() {
        String[] languages = {"English", "मराठी (Marathi)", "हिन्दी (Hindi)"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Select Language");
        builder.setItems(languages, (dialog, which) -> {
            switch (which) {
                case 0: // English
                    setLocale("en");
                    break;
                case 1: // Marathi
                    setLocale("mr");
                    break;
                case 2: // Hindi
                    setLocale("hi");
                    break;
            }
        });
        builder.show();
    }
    private void showThemeDialog() {
        String[] themes = {"Light Mode", "Dark Mode", "System Default"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Choose Theme");
        builder.setItems(themes, (dialog, which) -> {
            int mode;
            if (which == 0) mode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
            else if (which == 1) mode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
            else mode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

            // Theme save ani apply kara
            ThemeHelper.applyTheme(this, mode);

            // Activity refresh kara
            recreate();
        });
        builder.show();
    }

    private void setLocale(String langCode) {
        java.util.Locale locale = new java.util.Locale(langCode);
        java.util.Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);

        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Restart activity to apply changes
        Intent intent = new Intent(this, MoreActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();
        tvProfileEmail.setText(mAuth.getCurrentUser().getEmail());

        db.collection("users").document(uid).addSnapshotListener((doc, error) -> {
            if (error != null) return;
            if (doc != null && doc.exists()) {
                tvProfileName.setText(doc.getString("name"));

                String imageString = doc.getString("profileImageUrl");
                if (imageString != null && !imageString.isEmpty()) {
                    // Logic to decode Base64 string
                    decodeAndSetImage(imageString);
                }
            }
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
}