package com.example.civara;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class activity_homepage extends AppCompatActivity {

    TextView txtUser;
    Button btnLogout;
    LinearLayout layoutComplaint;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        txtUser = findViewById(R.id.txtUser);
        btnLogout = findViewById(R.id.btnLogout);
        layoutComplaint = findViewById(R.id.layoutComplaint);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            txtUser.setText("Welcome " + user.getEmail());
        }

        // ✅ OPEN COMPLAINT DASHBOARD
        layoutComplaint.setOnClickListener(v -> {
            startActivity(new Intent(
                    activity_homepage.this,
                    ComplaintDashboardActivity.class
            ));
        });

        // ✅ LOGOUT ONLY
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(
                    activity_homepage.this,
                    LoginActivity.class
            ));
            finish();
        });
    }
}
