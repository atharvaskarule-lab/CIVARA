package com.example.civara;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminDashboardActivity extends AppCompatActivity {

    // 1. Pehle variables declare karein (Jo aapke error mein dikh raha tha)
    private MaterialCardView cardComplaints, cardEvents;
    private ImageButton btnLogout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // 2. Firebase aur Views initialize karein
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // 3. Login Check (Security)
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 4. XML IDs ke saath link karein
        cardComplaints = findViewById(R.id.cardManageComplaints);
        cardEvents = findViewById(R.id.cardManageEvents);
        btnLogout = findViewById(R.id.btnLogoutAdmin);

        // 5. Click Listeners set karein
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Complaints Card Click
        if (cardComplaints != null) {
            cardComplaints.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, ManageComplaintsActivity.class);
                startActivity(intent);
            });
        }

        // Events Card Click
        if (cardEvents != null) {
            cardEvents.setOnClickListener(v -> {
                // Agar AddEventActivity bani hai toh wahan bhejega
                try {
                    Intent intent = new Intent(AdminDashboardActivity.this, AddEventActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "AddEventActivity not found!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Logout Button Click
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                mAuth.signOut();
                Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}