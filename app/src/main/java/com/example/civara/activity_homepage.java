package com.example.civara;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class activity_homepage extends AppCompatActivity {

    private TextView tvWelcomeName;
    private ImageButton btnLogout;
    private CardView layoutComplaint, btnSos, btnEvents, btnChatbot;

    // Safety Banner Views
    private CardView sosAlertBanner;
    private Button btnViewSosMap;
    private String activeMapLink = "";
    private ListenerRegistration sosListener;

    private BottomNavigationView bottomNav;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_homepage);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // Bind Views
        tvWelcomeName = findViewById(R.id.tvWelcomeName);
        btnLogout = findViewById(R.id.btnLogout);
        layoutComplaint = findViewById(R.id.layoutComplaint);
        btnSos = findViewById(R.id.btnSos);
        btnEvents = findViewById(R.id.btnEvents);
        btnChatbot = findViewById(R.id.btnChatbot);
        bottomNav = findViewById(R.id.bottomNavigation);

        // Safety Banner Binding
        sosAlertBanner = findViewById(R.id.sosAlertBanner);
        btnViewSosMap = findViewById(R.id.btnViewSosMap);

        if (user != null) {
            loadUserProfile(user.getUid());
        } else {
            redirectToLogin();
        }

        setupClickListeners();
        setupBottomNavigation();
        startListeningForEmergencies(); // Start real-time SOS listener
    }

    private void loadUserProfile(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        tvWelcomeName.setText("Hi, " + (name != null ? name : "User") + " 👋");
                    }
                });
    }

    private void startListeningForEmergencies() {
        // This listener checks the "active_alerts" collection in real-time
        sosListener = db.collection("active_alerts")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null && !value.isEmpty()) {
                        // Someone has an active SOS! Show banner
                        activeMapLink = value.getDocuments().get(0).getString("mapLink");
                        sosAlertBanner.setVisibility(View.VISIBLE);
                    } else {
                        // No alerts
                        sosAlertBanner.setVisibility(View.GONE);
                        activeMapLink = "";
                    }
                });
    }

    private void setupClickListeners() {
        layoutComplaint.setOnClickListener(v ->
                startActivity(new Intent(this, ComplaintDashboardActivity.class)));

        btnEvents.setOnClickListener(v ->
                startActivity(new Intent(this, EventCalendarActivity.class)));

        // Fixed SOS: Opens the SosActivity
        btnSos.setOnClickListener(v ->
                startActivity(new Intent(this, SosActivity.class)));

        btnChatbot.setOnClickListener(v ->
                Toast.makeText(this, "Opening Civara Bot", Toast.LENGTH_SHORT).show());

        // Safety Banner Map Button
        btnViewSosMap.setOnClickListener(v -> {
            if (!activeMapLink.isEmpty()) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(activeMapLink));
                startActivity(i);
            }
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            redirectToLogin();
        });
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_weather) {
                startActivity(new Intent(this, WeatherActivity.class));
                return true;
            }
            return false;
        });
    }

    private void redirectToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop listening to Firestore to save battery when app is closed
        if (sosListener != null) {
            sosListener.remove();
        }
    }
}