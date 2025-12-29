package com.example.civara;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AboutUsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private LinearLayout layoutTerms, layoutPrivacyPolicy, layoutLicenses;
    private ImageButton btnWebsite, btnTwitter, btnFacebook, btnInstagram;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set light status bar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_about_us);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        layoutTerms = findViewById(R.id.layoutTerms);
        layoutPrivacyPolicy = findViewById(R.id.layoutPrivacyPolicy);
        layoutLicenses = findViewById(R.id.layoutLicenses);

        btnWebsite = findViewById(R.id.btnWebsite);
        btnTwitter = findViewById(R.id.btnTwitter);
        btnFacebook = findViewById(R.id.btnFacebook);
        btnInstagram = findViewById(R.id.btnInstagram);
    }

    private void setupClickListeners() {
        // Back Button
        btnBack.setOnClickListener(v -> finish());

        // Legal Links (Placeholder Toasts - You can link to your website URLs)
        layoutTerms.setOnClickListener(v -> openBrowser("https://example.com/terms"));
        layoutPrivacyPolicy.setOnClickListener(v -> openBrowser("https://example.com/privacy"));
        layoutLicenses.setOnClickListener(v ->
                Toast.makeText(this, "This app uses Firebase, Glide, and Google Material Components.", Toast.LENGTH_LONG).show()
        );

        // Social Media Buttons
        btnWebsite.setOnClickListener(v -> openBrowser("https://www.civara.com"));
        btnTwitter.setOnClickListener(v -> openBrowser("https://twitter.com/civara_app"));
        btnFacebook.setOnClickListener(v -> openBrowser("https://facebook.com/civara_app"));
        btnInstagram.setOnClickListener(v -> openBrowser("https://instagram.com/civara_app"));
    }

    /**
     * Helper method to open URLs in the device's default web browser
     */
    private void openBrowser(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open link", Toast.LENGTH_SHORT).show();
        }
    }
}