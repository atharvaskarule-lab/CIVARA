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
    private ImageButton btnLogout, btnNotification;
    private View notificationBadge;
    private CardView layoutComplaint, btnSos, btnEvents, btnChatbot;

    private CardView sosAlertBanner;
    private Button btnViewSosMap;
    private String activeMapLink = "";

    // Storing listeners to remove them in onDestroy
    private ListenerRegistration sosListener;
    private ListenerRegistration notificationListener;

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
        btnNotification = findViewById(R.id.btnNotification);
        notificationBadge = findViewById(R.id.notificationBadge);
        layoutComplaint = findViewById(R.id.layoutComplaint);
        btnSos = findViewById(R.id.btnSos);
        btnEvents = findViewById(R.id.btnEvents);
        btnChatbot = findViewById(R.id.btnChatbot);
        bottomNav = findViewById(R.id.bottomNavigation);
        sosAlertBanner = findViewById(R.id.sosAlertBanner);
        btnViewSosMap = findViewById(R.id.btnViewSosMap);

        if (user != null) {
            loadUserProfile(user.getUid());
            startListeningForEmergencies();
            listenForGlobalNotifications(); // New Badge Logic
        } else {
            redirectToLogin();
        }

        setupClickListeners();
        setupBottomNavigation();
    }

    private void loadUserProfile(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        tvWelcomeName.setText("Hi, " + (name != null ? name : "User") + " 👋");
                    }
                });
    }

    private void startListeningForEmergencies() {
        sosListener = db.collection("active_alerts")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null && !value.isEmpty()) {
                        activeMapLink = value.getDocuments().get(0).getString("mapLink");
                        sosAlertBanner.setVisibility(View.VISIBLE);
                    } else {
                        sosAlertBanner.setVisibility(View.GONE);
                        activeMapLink = "";
                    }
                });
    }

    private void listenForGlobalNotifications() {
        // This listener updates the red badge whenever a new document is added to 'notifications'
        notificationListener = db.collection("notifications")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null && !value.isEmpty()) {
                        notificationBadge.setVisibility(View.VISIBLE);
                    } else {
                        notificationBadge.setVisibility(View.GONE);
                    }
                });
    }

    private void setupClickListeners() {
        layoutComplaint.setOnClickListener(v -> startActivity(new Intent(this, ComplaintDashboardActivity.class)));
        btnEvents.setOnClickListener(v -> startActivity(new Intent(this, EventCalendarActivity.class)));
        btnSos.setOnClickListener(v -> startActivity(new Intent(this, SosActivity.class)));
        btnChatbot.setOnClickListener(v -> Toast.makeText(this, "Opening Civara Bot", Toast.LENGTH_SHORT).show());

        btnNotification.setOnClickListener(v -> {
            notificationBadge.setVisibility(View.GONE); // Clear the badge when clicked
            startActivity(new Intent(this, NotificationActivity.class));
        });

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
        if (sosListener != null) sosListener.remove();
        if (notificationListener != null) notificationListener.remove();
    }
}