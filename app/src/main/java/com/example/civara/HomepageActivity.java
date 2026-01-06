package com.example.civara;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;

public class HomepageActivity extends AppCompatActivity {

    private static final String TAG = "HomepageActivity";

    // View Declarations
    private TextView tvWelcomeName;
    private ImageButton btnLogout, btnNotification;
    private View notificationBadge;
    private CardView layoutComplaint, btnSos, btnEvents, btnChatbot;
    private CardView sosAlertBanner;
    private Button btnViewSosMap;
    private BottomNavigationView bottomNav;

    // Firebase & Data
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String activeMapLink = "";
    private ListenerRegistration sosListener;
    private ListenerRegistration notificationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // Initialize Firebase services
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        initViews();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            redirectToLogin();
            return;
        }

        loadUserProfile(user.getUid());
        setupClickListeners();
        setupBottomNavigation();
        setupOnBackPressed();
    }

    private void initViews() {
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
    }

    private void setupClickListeners() {
        layoutComplaint.setOnClickListener(v -> startActivity(new Intent(this, ComplaintDashboardActivity.class)));
        btnEvents.setOnClickListener(v -> startActivity(new Intent(this, EventCalendarActivity.class)));
        btnSos.setOnClickListener(v -> startActivity(new Intent(this, SosActivity.class)));
        btnChatbot.setOnClickListener(v -> Toast.makeText(this, "Opening Civara Bot", Toast.LENGTH_SHORT).show());

        btnNotification.setOnClickListener(v -> {
            notificationBadge.setVisibility(View.GONE);
            startActivity(new Intent(this, NotificationActivity.class));
        });

        btnViewSosMap.setOnClickListener(v -> {
            if (activeMapLink != null && !activeMapLink.isEmpty()) {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(activeMapLink));
                    startActivity(i);
                } catch (Exception e) {
                    Log.e(TAG, "Could not open map link", e);
                    Toast.makeText(this, "Invalid map link.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            redirectToLogin();
        });

        btnChatbot.setOnClickListener(v -> {
            // 1. Create the Intent to move from Homepage to ChatbotActivity
            Intent intent = new Intent(HomepageActivity.this, ChatbotActivity.class);

            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_weather) {
                startActivity(new Intent(this, WeatherActivity.class));
                return true;
            } else if (id == R.id.nav_emergency) {
                startActivity(new Intent(this, SosActivity.class));
                return true;
            } else if (id == R.id.nav_more) {
                startActivity(new Intent(this, MoreActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadUserProfile(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (isFinishing()) return;
                    if (doc != null && doc.exists()) {
                        String name = doc.getString("name");
                        tvWelcomeName.setText(getString(R.string.welcome_message, (name != null ? name : "User")));
                    } else {
                        tvWelcomeName.setText(getString(R.string.welcome_message, "User"));
                    }
                }).addOnFailureListener(e -> Log.e(TAG, "Profile load failed", e));
    }

    private void startListeners() {
        if (sosListener == null) {
            sosListener = db.collection("active_alerts")
                    .addSnapshotListener((value, error) -> {
                        if (error != null) return;
                        boolean hasAlerts = value != null && !value.isEmpty();
                        sosAlertBanner.setVisibility(hasAlerts ? View.VISIBLE : View.GONE);
                        if (hasAlerts) {
                            activeMapLink = value.getDocuments().get(0).getString("mapLink");
                        }
                    });
        }
        if (notificationListener == null) {
            notificationListener = db.collection("notifications")
                    .addSnapshotListener((value, error) -> {
                        if (error != null) return;
                        notificationBadge.setVisibility(value != null && !value.isEmpty() ? View.VISIBLE : View.GONE);
                    });
        }
    }

    private void stopListeners() {
        if (sosListener != null) { sosListener.remove(); sosListener = null; }
        if (notificationListener != null) { notificationListener.remove(); notificationListener = null; }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupOnBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopListeners();
    }
}