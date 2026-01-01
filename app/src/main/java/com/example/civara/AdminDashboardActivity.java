package com.example.civara;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    private MaterialCardView cardComplaints, cardEvents;
    private ImageButton btnLogout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        cardComplaints = findViewById(R.id.cardManageComplaints);
        cardEvents = findViewById(R.id.cardManageEvents);
        btnLogout = findViewById(R.id.btnLogoutAdmin);

        setupClickListeners();
    }

    private void setupClickListeners() {

        // Opens the Complaint Management screen
        cardComplaints.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(AdminDashboardActivity.this, ManageComplaintsActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Error opening Complaints: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Opens the Event Management/Calendar screen
        cardEvents.setOnClickListener(v -> {
            try {
                // Ensure you have created AddEventActivity or ManageEventsActivity
                Intent intent = new Intent(AdminDashboardActivity.this, AddEventActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Event Page not found. Create AddEventActivity first.", Toast.LENGTH_SHORT).show();
            }
        });

        // Standard Logout logic
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            // Clear activity stack so user can't press back to return to dashboard
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}