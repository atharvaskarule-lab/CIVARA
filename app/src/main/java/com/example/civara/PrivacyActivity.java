package com.example.civara;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class PrivacyActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private SwitchMaterial switchLocation, switchPreciseLocation, switchAnonymous, switchActivity, switchPublicProfile;
    private LinearLayout layoutDownloadData, layoutClearData;

    // SharedPreferences for saving switch states
    private static final String PREFS_NAME = "PrivacyPrefs";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_privacy); // Ensure this matches your XML filename

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        initViews();
        loadSettings();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        switchLocation = findViewById(R.id.switchLocation);
        switchPreciseLocation = findViewById(R.id.switchPreciseLocation);
        switchAnonymous = findViewById(R.id.switchAnonymous);
        switchActivity = findViewById(R.id.switchActivity);
        switchPublicProfile = findViewById(R.id.switchPublicProfile);
        layoutDownloadData = findViewById(R.id.layoutDownloadData);
        layoutClearData = findViewById(R.id.layoutClearData);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Save state immediately when a switch is toggled
        switchLocation.setOnCheckedChangeListener((btn, isChecked) -> saveSetting("location", isChecked));
        switchPreciseLocation.setOnCheckedChangeListener((btn, isChecked) -> saveSetting("precise_location", isChecked));
        switchAnonymous.setOnCheckedChangeListener((btn, isChecked) -> saveSetting("anonymous", isChecked));
        switchActivity.setOnCheckedChangeListener((btn, isChecked) -> saveSetting("activity", isChecked));
        switchPublicProfile.setOnCheckedChangeListener((btn, isChecked) -> saveSetting("public_profile", isChecked));

        layoutDownloadData.setOnClickListener(v -> {
            Toast.makeText(this, "Preparing your data archive. Check your email soon.", Toast.LENGTH_LONG).show();
        });

        layoutClearData.setOnClickListener(v -> showClearDataDialog());
    }

    private void saveSetting(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    private void loadSettings() {
        // Load saved values, providing a default if no value exists
        switchLocation.setChecked(sharedPreferences.getBoolean("location", true));
        switchPreciseLocation.setChecked(sharedPreferences.getBoolean("precise_location", false));
        switchAnonymous.setChecked(sharedPreferences.getBoolean("anonymous", false));
        switchActivity.setChecked(sharedPreferences.getBoolean("activity", true));
        switchPublicProfile.setChecked(sharedPreferences.getBoolean("public_profile", true));
    }

    private void showClearDataDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Data?")
                .setMessage("This will permanently delete your activity history and cached preferences. This action cannot be undone.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    // Logic to clear data (e.g., clear SharedPreferences or Firebase local cache)
                    sharedPreferences.edit().clear().apply();
                    loadSettings(); // Reset switches to defaults
                    Toast.makeText(this, "All local data cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}